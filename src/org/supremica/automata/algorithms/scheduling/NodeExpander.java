package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizerHelper;
import org.supremica.automata.algorithms.SynchronizationOptions;

public class NodeExpander 
{
    private static Logger logger = LoggerFactory.createLogger(NodeExpander.class);

    /** Decides if the expansion of the nodes should be done using the methods of this class or using Supremicas methods */
    protected boolean manualExpansion;

	/** 
	 * If this variable is true, the expander is forced to choose one of the 
	 * uncontrollable events if present, which means that controllable transitions
	 * are disregarded at expansion if there is at least one outgoing uncontrollable
	 * transition. 
	 */
	protected boolean immediateChoiceAtUncontrollability;

    /** Needed to be able to use Supremicas expansion/synchronization methods */
    protected AutomataSynchronizerExecuter onlineSynchronizer;

    /** Contains maps between states and corresponding indices, in order to compress the used memory and speed up operations */
	protected AutomataIndexForm indexForm;
    
    /** The selected automata to be scheduled */
    protected Automata theAutomata, plantAutomata;

    /** The calling class */
    protected ModifiedAstar sched;

    /** Maps every event that is specified to the corresponing specification automaton, in order to speed up the expansion */
    protected Hashtable<LabeledEvent, Integer> specEventTable;

	/** 
	 * This variable becomes true if at least one uncontrollable event 
	 * is found during the current expansion.
	 */
	private boolean uncontrollableEventFound = false;

	//Tillf (test)
	private Alphabet unbookingAlphabet = null;

	//     /** Needed for manual expansion */
	//     protected int[][][] outgoingEventsTable;

	//     /** Needed for manual expansion */
	//     protected int[][][] nextStateTable;

    /***********************************************************************
     *                Start-up methods                                     *
     ***********************************************************************/

    public NodeExpander(boolean manualExpansion, boolean immediateChoiceAtUncontrollability, Automata theAutomata, ModifiedAstar sched) {
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
			for (int i=0; i<theAutomata.size(); i++)
			{
				theAutomata.getAutomatonAt(i).remapStateIndices();
			}

			initSpecEventTable();
		}
    }

	public NodeExpander(boolean manualExpansion, Automata theAutomata, ModifiedAstar sched)
	{
		this(manualExpansion, true, theAutomata, sched);
	}

    public Collection expandNode(double[] node, int[] activeAutomataIndex) {
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
			Automaton theAuto = theAutomata.getAutomatonAt(i);
	    
			if (theAuto.isSpecification()) 
			{
				Iterator<LabeledEvent> eventIt = theAuto.getAlphabet().iterator();

				while(eventIt.hasNext()) 
				{
					specEventTable.put(eventIt.next(), i);
				}
			}
		}
    }

    public Collection expandNodeManually(double[] node, int[] activeAutomataIndex) 
	{
		uncontrollableEventFound = false;
		ArrayList children = new ArrayList();

		for (int i=0; i<activeAutomataIndex.length; i++)
		{
			int automatonIndex = activeAutomataIndex[i]; 
			int stateIndex = (int)node[automatonIndex];

			State st = theAutomata.getAutomatonAt(automatonIndex).getStateWithIndex(stateIndex);
			Iterator<Arc> arcIt = st.outgoingArcsIterator();

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
				
				Object currSpecIndexObj = specEventTable.get(currEvent);

				// If current event is not booking/unbooking, change the current plants state
				if (currSpecIndexObj == null) 
				{
					children.add(newNode(node, new int[]{i}, new int[]{st.nextState(currEvent).getIndex()}, st.nextState(currEvent).getCost()));
				}
				// Else, change the current plants state together with the state of the appropriate zone
				else 
				{
					int currSpecIndex = ((Integer)currSpecIndexObj).intValue();
					Iterator<State> enabledStatesIt = theAutomata.getAutomatonAt(currSpecIndex).statesThatEnableEventIterator(currEvent.getLabel());

					while (enabledStatesIt.hasNext()) 
					{
						State specState = enabledStatesIt.next();
						if (node[currSpecIndex] == specState.getIndex()) 
						{
							int[] changedIndices = new int[]{activeAutomataIndex[i], currSpecIndex};
							int[] newStateIndices = new int[]{st.nextState(currEvent).getIndex(), specState.nextState(currEvent).getIndex()};

							children.add(newNode(node, changedIndices, newStateIndices, st.nextState(currEvent).getCost()));

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
// 					children.add(newNode(node, new int[]{i}, new int[]{st.nextState(currEvent).getIndex()}, st.nextState(currEvent).getCost()));
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

// 							children.add(newNode(node, changedIndices, newStateIndices, st.nextState(currEvent).getCost()));

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

    public double[] newNode(double[] node, int[] changedIndices, int[] newStateIndices, double newCost) {
		int[] nextStateIndices = new int[theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA];

		for (int k=0; k<nextStateIndices.length; k++) 
		{
			nextStateIndices[k] = (int)node[k];
		}

		for (int i=0; i<changedIndices.length; i++)
		{
			nextStateIndices[changedIndices[i]] = newStateIndices[i];
		}

		double[] newCosts = sched.updateCosts(getCosts(node), changedIndices[0], newCost);

// 		return makeNode(nextStateIndices, makeParentNodeKeys(node), newCosts);
		return makeNode(nextStateIndices, sched.getKey(node), newCosts);
    }

    /***********************************************************************
     *               Methods for node expansion using Supremicas           *
     *               "in-built" synchronizer                               *
     ***********************************************************************/

    protected void initOnlineSynchronizer() 
	{
		//	Get current options
		SynchronizationOptions syncOptions = new SynchronizationOptions();
		syncOptions.setBuildAutomaton(false);
		syncOptions.setRequireConsistentControllability(false);
		
		try 
		{
			AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(theAutomata, syncOptions);
			onlineSynchronizer = new AutomataSynchronizerExecuter(helper);
			onlineSynchronizer.initialize();

			indexForm = helper.getAutomataIndexForm();
			
			// Remapping necessary due to some kind of bug (in AutomataIndexForm?)
			for (int i=0; i<theAutomata.size(); i++)
			{
				theAutomata.getAutomatonAt(i).remapStateIndices();
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    }

    public Collection expandNodeWithSupremica(double[] node, int[] activeAutomataIndex) 
	{
		uncontrollableEventFound = false;
        Hashtable childNodes = new Hashtable();

		int[] currStateIndex = AutomataIndexFormHelper.createState(theAutomata.size());
		for (int i=0; i<currStateIndex.length; i++)
		{
			currStateIndex[i] = (int)node[i];
		}

		int[] currOutgoingEvents = onlineSynchronizer.getOutgoingEvents(currStateIndex);
	
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
						int[] nextStateIndex = onlineSynchronizer.doTransition(currStateIndex, currOutgoingEvents[i]);
		
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
							Integer currKey = sched.getKey(nextStateIndex);
							
							if (!childNodes.contains(currKey)) 
							{
								double newCost = plantAutomata.getAutomatonAt(changedIndex).getStateWithIndex(nextStateIndex[activeAutomataIndex[changedIndex]]).getCost();
								double[] newCosts = sched.updateCosts(getCosts(node), changedIndex, newCost);
								
								childNodes.put(currKey, makeNode(nextStateIndex, sched.getKey(node), newCosts));
							}
						}
					}	
				}
				else
				{
					// The following code makes one expansion and add the child to the hashtable childNodes
					int[] nextStateIndex = onlineSynchronizer.doTransition(currStateIndex, currOutgoingEvents[i]);
					
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
						Integer currKey = sched.getKey(nextStateIndex);
						
						if (!childNodes.contains(currKey)) 
						{
							double newCost = plantAutomata.getAutomatonAt(changedIndex).getStateWithIndex(nextStateIndex[activeAutomataIndex[changedIndex]]).getCost();
							double[] newCosts = sched.updateCosts(getCosts(node), changedIndex, newCost);
							
							childNodes.put(currKey, makeNode(nextStateIndex, sched.getKey(node), newCosts));
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

	// Ändra detta oxo (ClosedNodes.CLOSED_NODE_INFO...)
//     private long[] makeParentNodeKeys(double [] node) 
// 	{
// 		if (node == null) 
// 		{
// 			return new long[]{-1, -1};
// 		}
	
// 		long key = sched.getKey(node);
		
// 		// parentNodeKeys kan strax ändras till att vara int och inte int[].
// 		int nodeArrayIndex = -1; //sched.getClosedNodes().getArrayIndexForNode(key, node);

// 		return new long[]{key, nodeArrayIndex};
//     }

	/**
	 * Combines the state indices of the current state and its parent, together
	 * with the costs (currentCosts, accumulatedCost and estimatedCost) into an
	 * int-array that represent the current node.
	 */
    public double[] makeNode(int[] stateIndices, int parentNodeKey, double[] costs) 
	{
// 		if (parentNodeKey == null) 
// 		{
// 			// Ändra detta
// 			parentNodeKey = new int[2]; //new int[ClosedNodes.CLOSED_NODE_INFO_SIZE];
// 			for (int i=0; i<parentNodeKeys.length; i++)
// 			{
// 				parentNodeKeys[i] = -1;
// 			}
// 		}

// 		double[] newNode = new double[stateIndices.length + parentNodeKeys.length + costs.length];
		double[] newNode = new double[stateIndices.length + 2 + costs.length];
	
		for (int i=0; i<stateIndices.length; i++)
		{
			newNode[i] = stateIndices[i];
		}

// 		for (int i=0; i<parentNodeKeys.length; i++)
// 		{
// 			newNode[i + stateIndices.length] = parentNodeKeys[i];
// 		}

		newNode[stateIndices.length] = parentNodeKey;
		newNode[stateIndices.length + 1] = -1;

		for (int i=0; i<costs.length; i++)
		{
// 			newNode[i + stateIndices.length + parentNodeKeys.length] = costs[i];
			newNode[i + stateIndices.length + 2] = costs[i];
		}

		return newNode;
    }

    public double[] getCosts(double[] node) {
		// Ändra detta
		int startIndex = theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA + 2; //ClosedNodes.CLOSED_NODE_INFO_SIZE;
		double[] costs = new double[node.length - startIndex];
	
		for (int i=0; i<costs.length; i++)
			costs[i] = node[startIndex + i];
	
		return costs;
    }

	public boolean isUncontrollableEventFound()
	{
		return uncontrollableEventFound;
	}
}
   