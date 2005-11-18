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

public class ModifiedAstar 
	extends AbstractAstar {

	private static Logger logger = LoggerFactory.createLogger(ModifiedAstar.class);

    public ModifiedAstar(Automata theAutomata) 
		throws Exception  
	{
		super(theAutomata, "1-product relax");
    }
	
    public ModifiedAstar(Automata theAutomata, String heuristic) 
		throws Exception 
	{
		super(theAutomata, heuristic, true, false);
    }
	
    public ModifiedAstar(Automata theAutomata, String heuristic, boolean manualExpansion, boolean iterativeSearch) 
		throws Exception 
	{
		super(theAutomata, heuristic, manualExpansion, iterativeSearch);
    }

	// NEW_TRY_START
	protected void branch(int[] currNode, boolean newTry) 
	{
		if (newTry == true)
		{
			try 
			{
				if (! isClosed(currNode))
				{
					// TODO: Lägg currNode till closedTree på rätt ställe
				}

				Iterator childIter = expander.expandNode(currNode, activeAutomataIndex).iterator();
				while (childIter.hasNext()) {
					int[] nextNode = (int[])childIter.next();

					openTree.add(nextNode);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean isClosed(int[] node)
	{
		return true;
	}
	// NEW_START_END
	
    protected void branch(int[] currNode) 
	{
		try 
		{
			if (!isOnAList(currNode)) 
			{
				closedNodes.putNode(getKey(currNode), currNode);

				// Tillfälligt bortkommenterat
				// 	    useOneProdRelax = false;
				// 	    if (activeAutomataIndex.length <= 2 || plantAutomata.size() <= 2)
				// 		useOneProdRelax = true;
		
				Iterator childIter = expander.expandNode(currNode, activeAutomataIndex).iterator();
				while (childIter.hasNext()) {
					int[] nextNode = (int[])childIter.next();
	    
					// 	    try {
					//  		    if (!isOnAList(nextNode)) {
					putOnOpenList(nextNode);
					//  		    }
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			
			//tillfälligt
			System.exit(0);
		}
		// 	    else
		// 		logger.info("-- " + printArray(nextNode));
		//     }
    }
	
	/**
	 * Converts the representation of the initial state to the int[]-representation of a node. 
	 * int[] node consists of [states.getIndex() AutomataIndexFormHelper-info (-1 0 normally) 
	 * parentStates.getIndex() states.getCurrentCost() accumulatedCost estimatedCost]. 
	 * int[] initialNode is thus [initialStates.getIndex() AutomataIndexFormHelper-info 
	 * null initialStates.getCost() 0 -1].
	 */
    protected int[] makeInitialNode() 
	{
		int[] initialStates = AutomataIndexFormHelper.createState(theAutomata.size());
		int[] initialCosts = new int[getActiveLength() + 2];

		int nrOfPlants = plantAutomata.size(); 
	
		// Initial state indices are stored
		for (int i=0; i<theAutomata.size(); i++) 
			initialStates[i] = theAutomata.getAutomatonAt(i).getInitialState().getIndex();
	
		// Initial state costs are stored
		for (int i=0; i<nrOfPlants; i++) 
			initialCosts[i] = plantAutomata.getAutomatonAt(i).getInitialState().getCost();

		// The initial accumulated cost is zero
		initialCosts[nrOfPlants] = 0;

		// The initial estimate is set to -1
		initialCosts[nrOfPlants] = -1;

		// The NodeExpander combines the information, together with the parent-information, 
		// which is null for the initial state. 
		return expander.makeNode(initialStates, null, initialCosts);
    }
}