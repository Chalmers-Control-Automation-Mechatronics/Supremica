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

import org.supremica.automata.*;
import org.supremica.gui.ScheduleDialog;
import org.supremica.log.*;
import org.supremica.util.ActionTimer;

public class ModifiedAstar 
	extends AbstractAstar {

	private static Logger logger = LoggerFactory.createLogger(ModifiedAstar.class);

    public ModifiedAstar(Automata theAutomata, boolean buildSchedule, ScheduleDialog gui) 
		throws Exception  
	{
		super(theAutomata, "1-product relax", buildSchedule, gui);
    }
	
    public ModifiedAstar(Automata theAutomata, String heuristic, boolean buildSchedule, ScheduleDialog gui) 
		throws Exception 
	{
		super(theAutomata, heuristic, true, false, buildSchedule, gui);
    }
	
    public ModifiedAstar(Automata theAutomata, String heuristic, boolean manualExpansion, boolean iterativeSearch, boolean buildSchedule, ScheduleDialog gui) 
		throws Exception 
	{
		super(theAutomata, heuristic, manualExpansion, iterativeSearch, buildSchedule, gui);
    }

	public ModifiedAstar(Automata theAutomata, boolean manualExpansion, boolean buildSchedule, ScheduleDialog gui)
		throws Exception
	{
		super(theAutomata, manualExpansion, buildSchedule, gui);
	}

	/**
	 * The defalut relaxation, used for estimation of the remaining cost. This method is overriden 
	 * in the subclasses. Is used if no relaxation is chosen (i.e. brute force search).
	 */
	int getRelaxation(int[] node)
		throws Exception
	{
		return 0; 
	}

	/**
	 * Updates the closed tree if necessary, i.e. if the currently examined
	 * node with better estimate value already is present on that tree.
	 * If the node is added to the closed tree, its descendants are put on the 
	 * open tree, according to the estimated remaining cost value.
	 */
	protected void branch(int[] currNode) 
	{
		try 
		{
			boolean currNodeIsAddedToClosed = updateClosedTree(currNode);
			
			if (currNodeIsAddedToClosed)
			{
				Iterator childIter = expander.expandNode(currNode, activeAutomataIndex).iterator();
				while (childIter.hasNext()) 
				{
					int[] nextNode = (int[])childIter.next();
					logger.info("nextNode = " + printArray(nextNode));
					
					// Calculate the estimate function of the expanded node and store it at the appropriate position
					nextNode[ESTIMATE_INDEX] = calcEstimatedCost(nextNode);
					
					openTree.add(nextNode);
				}
			}
			else
				logger.info("currNodeIsAddedToClosed");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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
		initialCosts[nrOfPlants + 1] = -1;

		// The NodeExpander combines the information, together with the parent-information, 
		// which is null for the initial state. 
		return expander.makeNode(initialStates, null, initialCosts);
    }
}