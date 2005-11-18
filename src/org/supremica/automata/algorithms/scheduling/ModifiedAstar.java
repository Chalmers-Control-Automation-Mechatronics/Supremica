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
	protected void branch(int[] currNode) 
	{
		try 
		{
			// 				if (! isClosed(currNode))
			// 				{
			// 					// TODO: Lägg currNode till closedTree på rätt ställe
			// 				}
						
			updateClosedTree(currNode);
			
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
	
	/**
	 * This method puts the node "node" in the right place on the closedTree if the tree does not already
	 * contain any nodes that correspond to the same logical states than this node. 
	 * Otherwise, the method compares this node to those already examined. If this node is worse 
	 * (more expensive in every future direction) that any already examined node, it is discarded. 
	 * Conversely, if it is better than every examined node, this node is the only one to be stored in the 
	 * closedTree. If there are ties (in some directions one node is better than the other, but in others
	 * it is worse), this node, as well as all the discovered tie nodes are stored in the closedTree
	 * (note that all examined nodes that are always worse than "node" are discarded).
	 */
	private void updateClosedTree(int[] node)
	{
		// The nodes corresponding to the same logical state (but different paths from the initial state)
		// as the new node. They are stored as one int[]-variable in the closedTree.
		int[] correspondingClosedNodes = closedTree.remove(new Integer(getKey(node)));

		// If the node (or its logical state collegues) has not yet been put on the closedTree, 
		// then it is simply added to CLOSED.
		if (correspondingClosedNodes == null)
		{
			closedTree.put(new Integer(getKey(node)), node);
		}
		else
		{
			// The internal indices of the nodes that can be either better or worse (cheaper or more expensive)
			// than the current node, depending on the future path ("internal index" meaning the node's number in the 
			// correspondingClosedNodes-int[]-array).
			ArrayList<Integer> tieIndices = new ArrayList<Integer>();

			int nodeLength = node.length;
			int nrOfClosedNodes = correspondingClosedNodes.length / nodeLength;

			// Each "internal" node should be compared to the current node 
			for (int i=0; i<nrOfClosedNodes; i++)
			{
				boolean newNodeIsAlwaysWorse = true;
				boolean newNodeIsAlwaysBetter = true;

				// The comparison is done for Tv_new[i] + g_new <> Tv_old[i] + g_old (forall i)
				for (int j=currCostIndex; j<accCostIndex; j++)
				{
					int currCostDiff = (node[j] + node[accCostIndex]) - (correspondingClosedNodes[j + i*nodeLength] + correspondingClosedNodes[accCostIndex + i*nodeLength]);

					if (currCostDiff < 0)
						newNodeIsAlwaysWorse = false;
					else if (currCostDiff > 0)
						newNodeIsAlwaysBetter = false;
				}

				// If the new node is worse than any already examined node in every future direction,
				// it is thrown away;
				if (newNodeIsAlwaysWorse)
				{
					closedTree.put(new Integer(getKey(node)), correspondingClosedNodes);
					return;
				}
				// else if the examined node is neither worse nor better, its index is added to the tieIndices
				else if (! newNodeIsAlwaysWorse && ! newNodeIsAlwaysBetter)
					tieIndices.add(new Integer(i));					
			}

			// Only ties (and the new node) are kept for the update of the closedTree. 
			int[] newClosedNode = new int[(tieIndices.size() + 1)*nodeLength];

			// The tie-nodes (if there are any) are copied to the new closedNode
			for (int i=0; i<tieIndices.size(); i++)
			{
				int currExaminedNodesIndex = tieIndices.get(i).intValue();
				for (int j=0; j<nodeLength; j++) 
				{
					newClosedNode[j + i*nodeLength] = correspondingClosedNodes[j + currExaminedNodesIndex*nodeLength];
				}
			}
			
			// The latest addition to the node-family (int[] node) is also added to the new closedNode
			for (int j=0; j<nodeLength; j++)
			{
				newClosedNode[j + tieIndices.size()*nodeLength] = node[j];
			}

			closedTree.put(new Integer(getKey(node)), newClosedNode);
		}
	}
	// NEW_START_END
		
//     protected void branch(int[] currNode) 
// 	{
// 		try 
// 		{
// 			if (!isOnAList(currNode)) 
// 			{
// 				closedNodes.putNode(getKey(currNode), currNode);

// 				// Tillfälligt bortkommenterat
// 				// 	    useOneProdRelax = false;
// 				// 	    if (activeAutomataIndex.length <= 2 || plantAutomata.size() <= 2)
// 				// 		useOneProdRelax = true;
		
// 				Iterator childIter = expander.expandNode(currNode, activeAutomataIndex).iterator();
// 				while (childIter.hasNext()) {
// 					int[] nextNode = (int[])childIter.next();
	    
// 					// 	    try {
// 					//  		    if (!isOnAList(nextNode)) {
// 					putOnOpenList(nextNode);
// 					//  		    }
// 				}
// 			}
// 		}
// 		catch (Exception e) 
// 		{
// 			e.printStackTrace();
			
// 			//tillfälligt
// 			System.exit(0);
// 		}
// 		// 	    else
// 		// 		logger.info("-- " + printArray(nextNode));
// 		//     }
//     }
	
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