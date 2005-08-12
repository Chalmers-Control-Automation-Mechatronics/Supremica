/******************** ModifiedAstar.java **************************
 * AKs implementation of Tobbes modified Astar search algo
 * Basically this is a guided tree-search algorithm, like
 *
 *      list processed = 0;                             // closed
 *      list waiting = initial_state;   // open
 *
 *      while still waiting
 *      {
 *              choose an element from waiting  // the choice is guided by heuristics
 *              generate successors of this element
 *              if a successor is not already waiting or processed
 *                      put it on waiting
 *              place the element on processed, remove it from waiting
 *      }
 */
package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.util.ActionTimer;

public class ModifiedAstar extends Scheduler {

    public ModifiedAstar(Automata theAutomata) throws Exception  {
	super(theAutomata, "1-product relax");
    }

    public ModifiedAstar(Automata theAutomata, String heuristic) throws Exception {
	super(theAutomata, heuristic, true, false);
    }

    public ModifiedAstar(Automata theAutomata, String heuristic, boolean manualExpansion, boolean iterativeSearch) throws Exception {
	super(theAutomata, heuristic, manualExpansion, iterativeSearch);
    }

    protected void branch(int[] currNode) {
	closedNodes.putNode(getKey(currNode), currNode);

	// Tillf�lligt bortkommenterat
	// 	    useOneProdRelax = false;
	// 	    if (activeAutomataIndex.length <= 2 || plantAutomata.size() <= 2)
	// 		useOneProdRelax = true;
	
	
	Iterator childIter = expander.expandNode(currNode, activeAutomataIndex).iterator();
	while (childIter.hasNext()) {
	    int[] nextNode = (int[])childIter.next();
	    
	    if (!isOnAList(nextNode))
		putOnOpenList(nextNode);
	}
    }
}