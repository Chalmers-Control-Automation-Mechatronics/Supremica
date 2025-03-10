package org.supremica.automata.algorithms.scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexForm;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.AutomataIndexMap;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizerHelper;
import org.supremica.automata.algorithms.SynchronizationOptions;


public class NodeExpander
{
    /** Decides if the expansion of the nodes should be done using the methods of this class or using Supremicas methods */
    protected boolean manualExpansion;

    /**
     * If this variable is true, the expander is forced to choose one of the
     * uncontrollable events if present, which means that controllable transitions
     * are disregarded at expansion if there is at least one outgoing uncontrollable
     * transition.
     */
    protected boolean immediateChoiceAtUncontrollability;

    /** Needed to be able to use Supremica's expansion/synchronisation methods */
    protected AutomataSynchronizerExecuter onlineSynchronizer;

    /** Contains maps between states and corresponding indices, in order to compress the used memory and speed up operations */
	protected AutomataIndexForm indexForm;

    /** Responsible for correct indexing of automata, states and events. */
    protected AutomataIndexMap indexMap;

    /** The selected automata to be scheduled */
    protected Automata theAutomata, plantAutomata;

    /** The calling class */
    protected ModifiedAstar sched;

    /** Maps every event that is specified to the corresponding specification automaton, in order to speed up the expansion */
    protected Hashtable<LabeledEvent, Integer> specEventTable;

    /**
     * This variable becomes true if at least one uncontrollable event
     * is found during the current expansion.
     */
    private boolean uncontrollableEventFound = false;

    //Tillf (test)
    @SuppressWarnings("unused")
	private final Alphabet unbookingAlphabet = null;

    //     /** Needed for manual expansion */
    //     protected int[][][] outgoingEventsTable;

    //     /** Needed for manual expansion */
    //     protected int[][][] nextStateTable;

    /***********************************************************************
     *                Start-up methods                                     *
     ***********************************************************************/

    public NodeExpander(final boolean manualExpansion, final boolean immediateChoiceAtUncontrollability, final Automata theAutomata, final ModifiedAstar sched)
	{
		this.manualExpansion = manualExpansion;
		this.immediateChoiceAtUncontrollability = immediateChoiceAtUncontrollability;
		this.theAutomata = theAutomata;
		this.sched = sched;

		plantAutomata = theAutomata.getPlantAutomata();

		if (!manualExpansion)
		{
			initOnlineSynchronizer();
		}
		else
		{
// 			for (int i=0; i<theAutomata.size(); i++)
// 			{
// 				indexMap.getAutomatonAt(i).remapStateIndices();
// 			}

			initSpecEventTable();
		}
    }

    public NodeExpander(final boolean manualExpansion, final Automata theAutomata, final ModifiedAstar sched)
    {
            this(manualExpansion, true, theAutomata, sched);
    }

    public Collection<Node> expandNode(final Node node, final int[] activeAutomataIndex)
    {
        if (!manualExpansion)
        {
            return expandNodeWithSupremica(node, activeAutomataIndex);
        }
        else
        {
            return expandNodeManually(node, activeAutomataIndex);
        }
    }


    /***********************************************************************
     *               Methods for node expansion using                      *
     *               "manual" technique (adjusted for scheduling)          *
     ***********************************************************************/

    protected void initSpecEventTable()
	{
		specEventTable = new Hashtable<LabeledEvent, Integer>();

		for (int i=0; i<theAutomata.size(); i++)
		{
			final Automaton theAuto = indexMap.getAutomatonAt(i);

			if (theAuto.isSpecification())
			{
				final Iterator<LabeledEvent> eventIt = theAuto.getAlphabet().iterator();

				while(eventIt.hasNext())
				{
					specEventTable.put(eventIt.next(), i);
				}
			}
		}
    }

    public Collection<Node> expandNodeManually(final Node node, final int[] activeAutomataIndex)
    {
        uncontrollableEventFound = false;
        final ArrayList<Node> children = new ArrayList<Node>();

        for (int i=0; i<activeAutomataIndex.length; i++)
        {
            final int automatonIndex = activeAutomataIndex[i];
            final int stateIndex = (int)node.getValueAt(automatonIndex);

            final State st = indexMap.getStateAt(indexMap.getAutomatonAt(automatonIndex), stateIndex);
            final Iterator<Arc> arcIt = st.outgoingArcsIterator();

            while (arcIt.hasNext())
            {
                LabeledEvent currEvent = arcIt.next().getEvent();

                // If uncontrollable events should be immediately chosen when possible,
                // special treatment is needed when finding the successors of the current state...
                if (immediateChoiceAtUncontrollability)
                {
                    if (uncontrollableEventFound)
                    {
                        // If an outgoing uncontrollable event already has been detected,
                        // loop until another uncontrollable event is found.
                        while (currEvent.isControllable())
                        {
                            if (!arcIt.hasNext())
                            {
                                // If no more uncontrollale events are found, return.
                                return children;
                            }

                            currEvent = arcIt.next().getEvent();
                        }
                    }
                    else
                    {
                        // If the current event is the first uncontrollanble event that is found
                        // during this expansion, then previously collected "controllable"
                        // successors are no longer legitimate successors. Thus the 'children'-list
                        // is cleared.
                        if (!currEvent.isControllable())
                        {
                            uncontrollableEventFound = true;
                            children.clear();
                        }
                    }
                }

                final Object currSpecIndexObj = specEventTable.get(currEvent);

                // If current event is not booking/unbooking, change the current plants state
                if (currSpecIndexObj == null)
                {
                    final Node newNode = node.emptyClone();
                    newNode.setBasis(newNodeBasis(node, new int[]{i}, new int[]{st.nextState(currEvent).getIndex()}, st.nextState(currEvent).getCost()));
                    children.add(newNode);
                }
                // Else, change the current plants state together with the state of the appropriate zone
                else
                {
                    final int currSpecIndex = ((Integer)currSpecIndexObj).intValue();
                    final Iterator<State> enabledStatesIt = indexMap.getAutomatonAt(currSpecIndex).statesThatEnableEventIterator(currEvent.getLabel());

                    while (enabledStatesIt.hasNext())
                    {
                        final State specState = enabledStatesIt.next();
                        if (node.getValueAt(currSpecIndex) == specState.getIndex())
                        {
                            final int[] changedIndices = new int[]{activeAutomataIndex[i], currSpecIndex};
                            final int[] newStateIndices = new int[]{st.nextState(currEvent).getIndex(), specState.nextState(currEvent).getIndex()};

                            final Node newNode = node.emptyClone();
                            newNode.setBasis(newNodeBasis(node, changedIndices, newStateIndices, st.nextState(currEvent).getCost()));
                            children.add(newNode);

                            break;
                        }
                    }
                }
            }
        }

        return children;
    }

	//Tillf (Test)
// 	public Collection expandNodeManually(double[] node, int[] activeAutomataIndex, boolean prioritizeUnbookingEvents)
// 	{
// 		// If this method was called by mistake and the unbooking events are not prioritized
// 		if (!prioritizeUnbookingEvents)
// 		{
// 			return expandNodeManually(node, activeAutomataIndex);
// 		}

// 		// Initialize the unbooking alphabet (containing all the unbooking events) if it is null
// 		if (unbookingAlphabet == null)
// 		{
// 			unbookingAlphabet = new Alphabet();

// 			for (Iterator<Automaton> autIt = theAutomata.getSpecificationAutomata().iterator(); autIt.hasNext(); )
// 			{
// 				Automaton currZone = autIt.next();
// 				unbookingAlphabet.addEvents(currZone.getAlphabet().minus(currZone.getInitialState().activeEvents(false)));
// 			}
// 		}

// 		ArrayList children = new ArrayList();

// 		for (int i=0; i<activeAutomataIndex.length; i++)
// 		{
// 			int automatonIndex = activeAutomataIndex[i];
// 			int stateIndex = (int)node[automatonIndex];

// 			State st = theAutomata.getAutomatonAt(automatonIndex).getStateWithIndex(stateIndex);
// 			Iterator<Arc> arcIt = st.outgoingArcsIterator();

// 			boolean expansionDone = false;
// 			while (arcIt.hasNext())
// 			{
// 				LabeledEvent currEvent = arcIt.next().getEvent();

// 				// If there is an unbooking event, only this event (and the state that it leads to) are returned
// 				if (unbookingAlphabet.contains(currEvent))
// 				{
// 					children.clear();
// 					expansionDone = true;
// 				}


// 				Object currSpecIndexObj = specEventTable.get(currEvent);

// 				if (currSpecIndexObj == null)
// 				{
// 					children.add(newNodeBasis(node, new int[]{i}, new int[]{st.nextState(currEvent).getIndex()}, st.nextState(currEvent).getCost()));
// 				}
// 				else
// 				{
// 					int currSpecIndex = ((Integer)currSpecIndexObj).intValue();
// 					Iterator<State> enabledStatesIt = theAutomata.getAutomatonAt(currSpecIndex).statesThatEnableEventIterator(currEvent.getLabel());

// 					while (enabledStatesIt.hasNext())
// 					{
// 						State specState = enabledStatesIt.next();
// 						if (node[currSpecIndex] == specState.getIndex())
// 						{
// 							int[] changedIndices = new int[]{activeAutomataIndex[i], currSpecIndex};
// 							int[] newStateIndices = new int[]{st.nextState(currEvent).getIndex(), specState.nextState(currEvent).getIndex()};

// 							children.add(newNodeBasis(node, changedIndices, newStateIndices, st.nextState(currEvent).getCost()));

// 							break;
// 						}
// 					}
// 				}

// 				// ... yes, here...
// 				if (expansionDone == true)
// 					return children;
// 			}
// 		}

// 		return children;
//     }


	//     public boolean isEnabled(int[] node, LabeledEvent event) {
	// 	Object currSpecIndexObj = specEventTable.get(event);

	// 	if (currSpecIndexObj == null)
	// 	    return true;
	// 	else {
	// 	    int currSpecIndex = ((Integer)currSpecIndexObj).intValue();
	// 	    StateIterator enabledStatesIt = theAutomata.getAutomatonAt(currSpecIndex).statesThatEnableEventIterator(event.getLabel());

	// 	    while (enabledStatesIt.hasNext()) {
	// 		State specState = enabledStatesIt.nextState();
	// 		if (node[currSpecIndex] == specState.getIndex())
	// 		    return true;
	// 	    }
	// 	}

	// 	return false;
	//     }

	//     public int[] expandToGoal(int[] node) {
	// 	Iterator children = expandNodeManually(node, sched.getActiveAutomataIndex()).iterator();

	// 	while(children.hasNext()) {
	// 	    int[] currChild = (int[])children.next();
	// 	}

	// 	return null;
	//     }

	//     public int[] expandToGoal(int[] node) {
	// 	if (!sched.isAccepting(node)) {
	// 	    int minCost = node[ModifiedAstar.CURRENT_COSTS_INDEX];
	// 	    int minCostIndex = 0;

	// 	    for (int i=0; i<sched.getActiveLength(); i++) {
	// 		int automatonIndex = sched.getActiveAutomataIndex()[i];
	// 		int stateIndex = node[automatonIndex];
	// 		State st = theAutomata.getAutomatonAt(automatonIndex).getStateWithIndex(stateIndex);
	// 		LabeledEvent outgoingEvent = st.outgoingArcsIterator().nextArc().getEvent();

	// 		if (isEnabled(node, outgoingEvent)) {
	// 		    int currCost = node[i + ModifiedAstar.CURRENT_COSTS_INDEX];

	// 		    if (currCost < minCost) {
	// 			minCost = currCost;
	// 			minCostIndex = i;
	// 		    }
	// 		}
	// 	    }

	// 	    logger.warn("" + sched.printArray(node) + "   ger  " + minCost + " som min_cost och " + minCostIndex + " som dess index");
	// 	}

	// 	return null;
	//     }

    public double[] newNodeBasis(final Node node, final int[] changedIndices, final int[] newStateIndices, final double newCost)
	{
		final int[] nextStateIndices = new int[theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA];

		for (int k=0; k<nextStateIndices.length; k++)
		{
			nextStateIndices[k] = (int)node.getValueAt(k);
		}

		for (int i=0; i<changedIndices.length; i++)
		{
			nextStateIndices[changedIndices[i]] = newStateIndices[i];
		}

		final double[] newCosts = sched.updateCosts(getCosts(node), changedIndices[0], newCost);

// 		return makeNodeBasis(nextStateIndices, makeParentNodeKeys(node), newCosts);
		return makeNodeBasis(nextStateIndices, sched.getKey(node), newCosts);
    }

    /***********************************************************************
     *               Methods for node expansion using Supremicas           *
     *               "in-built" synchronizer                               *
     ***********************************************************************/

    protected void initOnlineSynchronizer()
	{
		//	Get current options
		final SynchronizationOptions syncOptions = new SynchronizationOptions();
		syncOptions.setBuildAutomaton(false);
		syncOptions.setRequireConsistentControllability(false);

		try
		{
			final AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(theAutomata, syncOptions, false);
			onlineSynchronizer = new AutomataSynchronizerExecuter(helper);
			onlineSynchronizer.initialize();

			indexForm = helper.getAutomataIndexForm();
			//new
			indexMap = helper.getIndexMap();

			// Remapping necessary due to some kind of bug (in AutomataIndexForm?)
// 			for (int i=0; i<theAutomata.size(); i++)
// 			{
// 				indexMap.getAutomatonAt(i).remapStateIndices();
// 			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
    }

    public Collection<Node> expandNodeWithSupremica(final Node node, final int[] activeAutomataIndex)
    {
        uncontrollableEventFound = false;
        final Hashtable<Integer, Node> childNodes = new Hashtable<Integer, Node>();

        final int[] currStateIndex = AutomataIndexFormHelper.createState(theAutomata.size());
        for (int i=0; i<currStateIndex.length; i++)
        {
            currStateIndex[i] = (int)node.getValueAt(i);
        }

        final int[] currOutgoingEvents = onlineSynchronizer.getOutgoingEvents(currStateIndex);

        for (int i=0; i<currOutgoingEvents.length; i++)
        {
            if (onlineSynchronizer.isEnabled(currOutgoingEvents[i]))
            {
                // If uncontrollable events should be immediately chosen when possible,
                // special treatment is needed when finding the successors of the current state...
                if (immediateChoiceAtUncontrollability)
                {
                    if (uncontrollableEventFound && indexForm.getControllableEventsTable()[currOutgoingEvents[i]])
                    {
                        // If there was an uncontrollable event detected earlier, while the current event
                        // is controllable, do nothing
                        //TODO.... make this nothing-doingness only occur when uc-event can occur right now (before c-sigma in other words)
                    }
                    else
                    {
                        if (!uncontrollableEventFound && !indexForm.getControllableEventsTable()[currOutgoingEvents[i]])
                        {
                            // If the current event is the first uncontrollanble event that is found
                            // during this expansion, then previously collected "controllable"
                            // successors are no longer legitimate successors. Thus the 'children'-list
                            // is cleared.
                            uncontrollableEventFound = true;
                            childNodes.clear();
                        }

                        // The following code makes one expansion and add the child to the hashtable childNodes
                        final int[] nextStateIndex = onlineSynchronizer.doTransition(currStateIndex, currOutgoingEvents[i]);

                        int changedIndex = -1;
                        for (int k=0; k<activeAutomataIndex.length; k++)
                        {
                            if (nextStateIndex[activeAutomataIndex[k]] != currStateIndex[activeAutomataIndex[k]])
                            {
                                changedIndex = k;
                                break;
                            }
                        }

                        if (changedIndex > -1) // || activeAutomataIndex.length == plantAutomata.size())
                        {
                            final Integer currKey = sched.getKey(nextStateIndex);

                            if (!childNodes.contains(currKey))
                            {
                                final double newCost = indexMap.getStateAt(plantAutomata.getAutomatonAt(changedIndex), nextStateIndex[activeAutomataIndex[changedIndex]]).getCost();
                                final double[] newCosts = sched.updateCosts(getCosts(node), changedIndex, newCost);

                                final Node newNode = node.emptyClone();
                                newNode.setBasis(makeNodeBasis(nextStateIndex, sched.getKey(node), newCosts));
                                childNodes.put(currKey, newNode);
                            }
                        }
                    }
                }
                else
                {
                    // The following code makes one expansion and add the child to the hashtable childNodes
                    final int[] nextStateIndex = onlineSynchronizer.doTransition(currStateIndex, currOutgoingEvents[i]);

                    int changedIndex = -1;
                    for (int k=0; k<activeAutomataIndex.length; k++)
                    {
                        if (nextStateIndex[activeAutomataIndex[k]] != currStateIndex[activeAutomataIndex[k]])
                        {
                            changedIndex = k;
                            break;
                        }
                    }

                    if (changedIndex > -1) // || activeAutomataIndex.length == plantAutomata.size())
                    {
                        final Integer currKey = sched.getKey(nextStateIndex);

                        if (!childNodes.contains(currKey))
                        {
                            final double newCost = indexMap.getStateAt(plantAutomata.getAutomatonAt(changedIndex), nextStateIndex[activeAutomataIndex[changedIndex]]).getCost();
                            final double[] newCosts = sched.updateCosts(getCosts(node), changedIndex, newCost);

                            final Node newNode = node.emptyClone();
                            newNode.setBasis(makeNodeBasis(nextStateIndex, sched.getKey(node), newCosts));
                            childNodes.put(currKey, newNode);
                        }
                    }
                }
            }
        }

        return childNodes.values();
    }


    /***********************************************************************
     *                         Auxiliary methods                           *
     ***********************************************************************/

	// �ndra detta oxo (ClosedNodes.CLOSED_NODE_INFO...)
//     private long[] makeParentNodeKeys(double [] node)
// 	{
// 		if (node == null)
// 		{
// 			return new long[]{-1, -1};
// 		}

// 		long key = sched.getKey(node);

// 		// parentNodeKeys kan strax �ndras till att vara int och inte int[].
// 		int nodeArrayIndex = -1; //sched.getClosedNodes().getArrayIndexForNode(key, node);

// 		return new long[]{key, nodeArrayIndex};
//     }

    /**
	 * Combines the state indices of the current state and its parent, together
	 * with the costs (currentCosts, accumulatedCost and estimatedCost) into an
	 * int-array that represent the current node.
	 */
    public double[] makeNodeBasis(final int[] stateIndices, final int parentNodeKey, final double[] costs)
    {
// 		if (parentNodeKey == null)
// 		{
// 			// �ndra detta
// 			parentNodeKey = new int[2]; //new int[ClosedNodes.CLOSED_NODE_INFO_SIZE];
// 			for (int i=0; i<parentNodeKeys.length; i++)
// 			{
// 				parentNodeKeys[i] = -1;
// 			}
// 		}

// 		double[] newNodeBasis = new double[stateIndices.length + parentNodeKeys.length + costs.length];
        final double[] newNodeBasis = new double[stateIndices.length + 2 + costs.length];

        for (int i=0; i<stateIndices.length; i++)
        {
            newNodeBasis[i] = stateIndices[i];
        }

// 		for (int i=0; i<parentNodeKeys.length; i++)
// 		{
// 			newNodeBasis[i + stateIndices.length] = parentNodeKeys[i];
// 		}

        newNodeBasis[ModifiedAstar.PARENT_INDEX] = parentNodeKey;
        newNodeBasis[stateIndices.length + 1] = -1;

        for (int i=0; i<costs.length; i++)
        {
// 			newNodeBasis[i + stateIndices.length + parentNodeKeys.length] = costs[i];
            newNodeBasis[i + stateIndices.length + 2] = costs[i];
        }

        return newNodeBasis;
    }

    public double[] getCosts(final Node node)
    {
            return getCosts(node.getBasis());
    }

    public double[] getCosts(final double[] node)
	{
		// �ndra detta
		final int startIndex = theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA + 2; //ClosedNodes.CLOSED_NODE_INFO_SIZE;
		final double[] costs = new double[node.length - startIndex];

		for (int i=0; i<costs.length; i++)
			costs[i] = node[startIndex + i];

		return costs;
    }

    public boolean isUncontrollableEventFound()
    {
        return uncontrollableEventFound;
    }

    /**
     * Converts the representation of the initial state to the double[]-representation of a node.
     * double[] node consists of [states.getIndex() AutomataIndexFormHelper-info (-1 0 normally)
     * parentStates.getIndex() states.getCurrentCost() accumulatedCost estimatedCost].
     * double[] initialNode is thus [initialStates.getIndex() AutomataIndexFormHelper-info
     * null initialStates.getCost() 0 -1].
     */
    public double[] makeInitialNodeBasis()
    {
        final int nrOfPlants = plantAutomata.size();
        final int[] initialStates = AutomataIndexFormHelper.createState(theAutomata.size());
        final double[] initialCosts = new double[nrOfPlants + 2];

        // Initial state indices are stored
        for (int i=0; i<theAutomata.size(); i++)
        {
            final Automaton currAuto = indexMap.getAutomatonAt(i);
            initialStates[indexMap.getAutomatonIndex(currAuto)] = indexMap.getStateIndex(currAuto, currAuto.getInitialState());
        }

        // Initial state costs are stored
        for (int i=0; i<nrOfPlants; i++)
        {
            initialCosts[i] = plantAutomata.getAutomatonAt(i).getInitialState().getCost();
        }

        // The initial accumulated cost is zero
        initialCosts[nrOfPlants] = 0;

        // The initial estimate is set to -1
        initialCosts[nrOfPlants + 1] = -1;

        // The NodeExpander combines the information, together with the parent-information,
        // which is null for the initial state.
        // 		return expander.makeNode(initialStates, null, initialCosts);
        return makeNodeBasis(initialStates, -1, initialCosts);
    }

    public AutomataIndexMap getIndexMap()
    {
            return indexMap;
    }
}
