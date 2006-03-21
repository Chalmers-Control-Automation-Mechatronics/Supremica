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

    /** Needed to be able to use Supremicas expansion/synchronization methods */
    protected AutomataSynchronizerExecuter onlineSynchronizer;

    /** Contains maps between states and corresponding indices, in order to compress the used memory and speed up operations */
	//     protected AutomataIndexForm indexForm;
    
    /** The selected automata to be scheduled */
    protected Automata theAutomata, plantAutomata;

    /** The calling class */
    protected AbstractAstar sched;

    /** Maps every event that is specified to the corresponing specification automaton, in order to speed up the expansion */
    protected Hashtable<LabeledEvent, Integer> specEventTable;

	//     /** Needed for manual expansion */
	//     protected int[][][] outgoingEventsTable;

	//     /** Needed for manual expansion */
	//     protected int[][][] nextStateTable;

    /***********************************************************************
     *                Start-up methods                                     *
     ***********************************************************************/

    public NodeExpander(boolean manualExpansion, Automata theAutomata, AbstractAstar sched) {
		this.manualExpansion = manualExpansion;
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

    public Collection expandNode(int[] node, int[] activeAutomataIndex) {
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

    protected void initSpecEventTable() {
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

    public Collection expandNodeManually(int[] node, int[] activeAutomataIndex) {
		ArrayList children = new ArrayList();

		for (int i=0; i<activeAutomataIndex.length; i++)
		{
			int automatonIndex = activeAutomataIndex[i]; 
			int stateIndex = node[automatonIndex];

			State st = theAutomata.getAutomatonAt(automatonIndex).getStateWithIndex(stateIndex);
			Iterator<Arc> arcIt = st.outgoingArcsIterator();

			while (arcIt.hasNext()) 
			{
				LabeledEvent currEvent = arcIt.next().getEvent();
				Object currSpecIndexObj = specEventTable.get(currEvent);

				if (currSpecIndexObj == null) 
				{
					children.add(newNode(node, new int[]{i}, new int[]{st.nextState(currEvent).getIndex()}, st.nextState(currEvent).getCost()));
				}
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
	// 	    int minCost = node[AbstractAstar.CURRENT_COSTS_INDEX];
	// 	    int minCostIndex = 0;
	    
	// 	    for (int i=0; i<sched.getActiveLength(); i++) {
	// 		int automatonIndex = sched.getActiveAutomataIndex()[i];
	// 		int stateIndex = node[automatonIndex];
	// 		State st = theAutomata.getAutomatonAt(automatonIndex).getStateWithIndex(stateIndex);
	// 		LabeledEvent outgoingEvent = st.outgoingArcsIterator().nextArc().getEvent();
		
	// 		if (isEnabled(node, outgoingEvent)) {
	// 		    int currCost = node[i + AbstractAstar.CURRENT_COSTS_INDEX];
		    
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

    public int[] newNode(int[] node, int[] changedIndices, int[] newStateIndices, int newCost) {
		int[] nextStateIndices = new int[theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA];

		for (int k=0; k<nextStateIndices.length; k++) 
		{
			nextStateIndices[k] = node[k];
		}

		for (int i=0; i<changedIndices.length; i++)
		{
			nextStateIndices[changedIndices[i]] = newStateIndices[i];
		}

		int[] newCosts = sched.updateCosts(getCosts(node), changedIndices[0], newCost);

		return makeNode(nextStateIndices, makeParentNodeKeys(node), newCosts);
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
			
			// Så fult borde det väl inte vara ändå... Buggen borde tas om hand i AutomataIndexForm tycker man. 
			for (int i=0; i<theAutomata.size(); i++)
				theAutomata.getAutomatonAt(i).remapStateIndices();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    }

    public Collection expandNodeWithSupremica(int[] node, int[] activeAutomataIndex) {
        Hashtable childNodes = new Hashtable();

		int[] currStateIndex = AutomataIndexFormHelper.createState(theAutomata.size());
		for (int i=0; i<currStateIndex.length; i++)
			currStateIndex[i] = node[i];
	
		int[] currOutgoingEvents = onlineSynchronizer.getOutgoingEvents(currStateIndex);
	
		for (int i=0; i<currOutgoingEvents.length; i++) {
			if (onlineSynchronizer.isEnabled(currOutgoingEvents[i])) {
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
		
				if (changedIndex > -1) { // || activeAutomataIndex.length == plantAutomata.size()) {
					Integer currKey = sched.getKey(nextStateIndex);
		    
					if (!childNodes.contains(currKey)) 
					{
						int newCost = plantAutomata.getAutomatonAt(changedIndex).getStateWithIndex(nextStateIndex[activeAutomataIndex[changedIndex]]).getCost();
						int[] newCosts = sched.updateCosts(getCosts(node), changedIndex, newCost);
			
						childNodes.put(currKey, makeNode(nextStateIndex, makeParentNodeKeys(node), newCosts));
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
    private int[] makeParentNodeKeys(int[] node) {
		if (node == null) 
			return new int[]{-1, -1};
	
		int key = sched.getKey(node);
		
		// parentNodeKeys kan strax ändras till att vara int och inte int[].
		int nodeArrayIndex = -1; //sched.getClosedNodes().getArrayIndexForNode(key, node);

		return new int[]{key, nodeArrayIndex};
    }

	/**
	 * Combines the state indices of the current state and its parent, together
	 * with the costs (currentCosts, accumulatedCost and estimatedCost) into an
	 * int-array that represent the current node.
	 */
    public int[] makeNode(int[] stateIndices, int[] parentNodeKeys, int[] costs) {
		if (parentNodeKeys == null) {
			// Ändra detta
			parentNodeKeys = new int[2]; //new int[ClosedNodes.CLOSED_NODE_INFO_SIZE];
			for (int i=0; i<parentNodeKeys.length; i++)
				parentNodeKeys[i] = -1;
		}

		int[] newNode = new int[stateIndices.length + parentNodeKeys.length + costs.length];
	
		for (int i=0; i<stateIndices.length; i++)
			newNode[i] = stateIndices[i];

		for (int i=0; i<parentNodeKeys.length; i++)
			newNode[i + stateIndices.length] = parentNodeKeys[i];

		for (int i=0; i<costs.length; i++)
			newNode[i + stateIndices.length + parentNodeKeys.length] = costs[i];
	
		return newNode;
    }

    public int[] getCosts(int[] node) {
		// Ändra detta
		int startIndex = theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA + 2; //ClosedNodes.CLOSED_NODE_INFO_SIZE;
		int[] costs = new int[node.length - startIndex];
	
		for (int i=0; i<costs.length; i++)
			costs[i] = node[startIndex + i];
	
		return costs;
    }
}
