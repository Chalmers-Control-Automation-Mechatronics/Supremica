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
    private Automata theAutomata, plantAutomata;

    private ModifiedAstar2 master;

    public NodeExpander(boolean manualExpansion, Automata theAutomata, ModifiedAstar2 master) {
	this.manualExpansion = manualExpansion;
	this.theAutomata = theAutomata;
	this.master = master;

	plantAutomata = theAutomata.getPlantAutomata();

	if (!manualExpansion)
	    initOnlineSynchronizer();
    }

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

    public Collection expandNode(int[] node, int[] activeAutomataIndex) {
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