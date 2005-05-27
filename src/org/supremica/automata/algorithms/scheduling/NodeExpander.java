package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizerHelper;
import org.supremica.automata.algorithms.SynchronizationOptions;

public class NodeExpander {
    private static Logger logger = LoggerFactory.createLogger(NodeExpander.class);

    private boolean manualExpansion;
    private AutomataSynchronizerExecuter onlineSynchronizer;
    private AutomataIndexForm indexForm;
    private Automata theAutomata, plantAutomata;
    private ModifiedAstar2 master;
    private Hashtable specEventTable;
    private int[][][] outgoingEventsTable;
    private int[][][] nextStateTable;

    /***********************************************************************
     *                Start-up methods                                     *
     ***********************************************************************/

    public NodeExpander(boolean manualExpansion, Automata theAutomata, ModifiedAstar2 master) {
	this.manualExpansion = manualExpansion;
	this.theAutomata = theAutomata;
	this.master = master;

	plantAutomata = theAutomata.getPlantAutomata();

	if (!manualExpansion)
	    initOnlineSynchronizer();
	else {
	    initAutomataIndexForm();
	    initSpecEventTable();
	}
    }

    public Collection expandNode(int[] node, int[] activeAutomataIndex) {
	if (!manualExpansion) 
	    return expandNodeWithSupremica(node, activeAutomataIndex);
	else
	    return expandNodeManually(node, activeAutomataIndex);
    }


    /***********************************************************************
     *               Methods for node expansion using                      *
     *               "manual" technique (adjusted for scheduling)          *
     ***********************************************************************/

    private void initAutomataIndexForm() {
	try {
	    //	Get current options
	    SynchronizationOptions syncOptions = new SynchronizationOptions();
	    syncOptions.setBuildAutomaton(false);
	    syncOptions.setRequireConsistentControllability(false);

	    AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(theAutomata, syncOptions);
	    indexForm = helper.getAutomataIndexForm();

	    for (int i=0; i<theAutomata.size(); i++)
		theAutomata.getAutomatonAt(i).remapStateIndices();

	    outgoingEventsTable = indexForm.getOutgoingEventsTable();
	    nextStateTable = indexForm.getNextStateTable();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void initSpecEventTable() {
	specEventTable = new Hashtable();

	for (int i=0; i<theAutomata.size(); i++) {
	    Automaton theAuto = theAutomata.getAutomatonAt(i);
	    
	    if (theAuto.isSpecification()) {
		EventIterator eventIt = theAuto.getAlphabet().iterator();

		while(eventIt.hasNext()) 
		    specEventTable.put(eventIt.nextEvent(), new Integer(i));
	    }
	}
    }

    public Collection expandNodeManually(int[] node, int[] activeAutomataIndex) {
	ArrayList children = new ArrayList();

	for (int i=0; i<activeAutomataIndex.length; i++) {
	    int automatonIndex = activeAutomataIndex[i]; 
	    int stateIndex = node[automatonIndex];

	    State st = theAutomata.getAutomatonAt(automatonIndex).getStateWithIndex(stateIndex);
	    ArcIterator arcIt = st.outgoingArcsIterator();
	
	    while (arcIt.hasNext()) {
		LabeledEvent currEvent = arcIt.nextArc().getEvent();
		int currSpecIndex = ((Integer) specEventTable.get(currEvent)).intValue();
		StateIterator enabledStatesIt = theAutomata.getAutomatonAt(currSpecIndex).statesThatEnableEventIterator(currEvent.getLabel());

		while (enabledStatesIt.hasNext()) {
		    State specState = enabledStatesIt.nextState();
		    if (node[currSpecIndex] == specState.getIndex()) {
			int nextPlantStateIndex = st.nextState(currEvent).getIndex();
			int nextSpecStateIndex = specState.nextState(currEvent).getIndex();

 		     	int[] nextStateIndices = new int[theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA];
			for (int k=0; k<nextStateIndices.length; k++) 
			    nextStateIndices[k] = node[k];
			nextStateIndices[i] = nextPlantStateIndex;
			nextStateIndices[currSpecIndex] = nextSpecStateIndex;

			int[] newCosts = updateCosts(getCosts(node), i, st.nextState(currEvent).getCost());

			children.add(makeNode(nextStateIndices, node, newCosts));

			break;
		    }
		}
	    }
	}

	//	logger.warn("children = " + children);

	return children;
    }

    /***********************************************************************
     *               Methods for node expansion using Supremicas           *
     *               "in-built" synchronizer                               *
     ***********************************************************************/

    /**
     * @param initialState
     */
    private void initOnlineSynchronizer() {
	//	Get current options
	SynchronizationOptions syncOptions = new SynchronizationOptions();
	syncOptions.setBuildAutomaton(false);
	syncOptions.setRequireConsistentControllability(false);
	
	try {
	    AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(theAutomata, syncOptions);
	    onlineSynchronizer = new AutomataSynchronizerExecuter(helper);
	    onlineSynchronizer.initialize();
	    
	    // Så fult borde det väl inte vara ändå... Buggen borde tas om hand i AutomataIndexForm tycker man. 
	    for (int i=0; i<theAutomata.size(); i++)
		theAutomata.getAutomatonAt(i).remapStateIndices();
	} catch (Exception e) {
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
		for (int k=0; k<activeAutomataIndex.length; k++) {
		    if (nextStateIndex[activeAutomataIndex[k]] != currStateIndex[activeAutomataIndex[k]]) {
			changedIndex = k;
			break;
		    }
		}
		
		if (changedIndex > -1) { // || activeAutomataIndex.length == plantAutomata.size()) {
		    Integer currKey = master.getKey(nextStateIndex);
		    
		    if (!childNodes.contains(currKey)) {
 			int newCost = plantAutomata.getAutomatonAt(changedIndex).getStateWithIndex(nextStateIndex[activeAutomataIndex[changedIndex]]).getCost();
 			int[] newCosts = updateCosts(getCosts(node), changedIndex, newCost);
			
 			childNodes.put(currKey, makeNode(nextStateIndex, node, newCosts));
		    }
		}
	    }
	}
	
	return childNodes.values();
    }


    /***********************************************************************
     *                         Auxiliary methods                           *
     ***********************************************************************/

    public int[] makeNode(int[] stateIndices, int[] parentNode, int[] costs) {
	int[] newNode = new int[stateIndices.length + theAutomata.size() + costs.length];
	
	for (int i=0; i<stateIndices.length; i++)
	    newNode[i] = stateIndices[i];
	if (parentNode != null) {
	    for (int i=0; i<theAutomata.size(); i++)
		newNode[i + stateIndices.length] = parentNode[i];
	}
	else {
	    for (int i=0; i<theAutomata.size(); i++)
		newNode[i + stateIndices.length] = -1;
	}
	for (int i=0; i<costs.length; i++)
	    newNode[i + stateIndices.length + theAutomata.size()] = costs[i];
	
	return newNode;
    }

    public int[] getCosts(int[] node) {
	int[] costs = new int[plantAutomata.size() + 1];
	int startIndex = 2*theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA;
	
	for (int i=0; i<costs.length; i++)
	    costs[i] = node[startIndex + i];
	
	return costs;
    }
    
    private int[] updateCosts(int[] costs, int changedIndex, int newCost) {
	int[] newCosts = new int[costs.length];
	
	for (int i=0; i<costs.length-1; i++) {
	    if (i == changedIndex)
		newCosts[i] = newCost;
	    else {
		newCosts[i] = costs[i] - costs[changedIndex]; 
		if (newCosts[i] < 0)
		    newCosts[i] = 0;
	    }
	}
	
	newCosts[newCosts.length-1] = costs[costs.length-1] + costs[changedIndex];
	
	return newCosts;
    }
}