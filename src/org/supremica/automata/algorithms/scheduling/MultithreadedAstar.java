/******************** MultithreadedAstar.java **************************
 *
 * AKs modification of Tobbes modified A* scheduling algorithm  
 * allowing to take care of stochastic transitions, where each 
 * stochastic (uncontrollable) transition gives raise to a new search 
 * thread. 
 */

package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.automata.*;
import org.supremica.gui.ScheduleDialog;
import org.supremica.gui.ActionMan;
import org.supremica.log.*;

public class MultithreadedAstar
	extends ModifiedAstar
{

	private static Logger logger = LoggerFactory.createLogger(MultithreadedAstar.class);

    /**
     * Contains promising search tree nodes, i.e. nodes that might lie on the optimal path.
     * They are "opened" but not yet examined (i.e. not "closed").
     * OPEN list is better represented as a tree, ordered by the estimate values, f(n),
     * while the tree vertices contain the double[] representations of the "opened" nodes, 
	 * together with pointers to the subthreads.
     *
     * double[] node = [state_0_index, ..., state_m_index, -1, 0,
     *               parent_key, current_costs_0, ..., current_costs_k,
     *               accumulated_cost, estimate_value],
     * m being the number of selected automata, k the number of selected plants (robots),
     * current_costs are the Tv(n)-values, accumulated_cost = g(n) and estimate_value = f(n).
     * (-1 and 0 are due to Supremicas AutomataIndexFormHelper information, namely
     * AutomataIndexFormHelper.STATE_EXTRA_DATA).
     */
//     protected TreeSet<MultithreadedNode> openTree;
    
    /**
     * Contains already examined (i.e. "closed") search tree nodes (or rather their int[]-representations
     * @see #openTree ).  For efficiency, CLOSED should be represented
     * as a tree. It is faster to search through than a list and takes
     * less place than a hashtable.  If several nodes, corresponding
     * to the same logical state, have been examined and none of them
     * is guaranteed to be better than the other, all the nodes are
     * stored as one double[]-variable, together with pointers to the
     * subthreads. The closed nodes are unrolled and compared when
     * necessary, for example when a new node is to be added to the
     * closedTree. This is done by
     * @see ModifiedAstar#updateClosedTree(double[] node) .
     */
//     protected TreeMap<Integer, MultithreadedNode> closedTree;

    /** Stores the accepting node of the resulting schedule (with a reference to the ancestor node. */
//     protected MultithreadedNode acceptingNode = null;

	private MultithreadedNode rootNode = null;
	private double branchingProbality;
	private static final double DEFAULT_PROBABILITY = 1;
     
	public MultithreadedAstar(Automata theAutomata, String heuristic, boolean manualExpansion, boolean buildSchedule, boolean isRelaxationProvider, ScheduleDialog gui)
		throws Exception
	{
		this(theAutomata, heuristic, manualExpansion, buildSchedule, isRelaxationProvider, gui, null);
	}

	public MultithreadedAstar(Automata theAutomata, String heuristic, boolean manualExpansion, boolean buildSchedule, boolean isRelaxationProvider, ScheduleDialog gui, MultithreadedNode rootNode)
		throws Exception
    {
        this(theAutomata, heuristic, manualExpansion, buildSchedule, isRelaxationProvider, gui, rootNode, DEFAULT_PROBABILITY);
    }
		
    public MultithreadedAstar(Automata theAutomata, String heuristic, boolean manualExpansion, boolean buildSchedule, boolean isRelaxationProvider, ScheduleDialog gui, MultithreadedNode rootNode, double branchingProbality)
		throws Exception
    {
        super(theAutomata, heuristic, manualExpansion, buildSchedule, isRelaxationProvider, gui);

		this.rootNode = rootNode;
		this.branchingProbality = branchingProbality;
    }

	protected void init()
		throws Exception
	{
		if (rootNode == null)
		{
			super.init();
		}
		else
		{
			initTrees();
			
			// Is needed for synchronization purposes
			isInitialized = true;
		}
	}

// 	/** 
// 	 * Resets or initializes the OPEN and the CLOSED trees.
// 	 */
// 	protected void initTrees()
// 	{
// logger.info("In initTrees");
// 		if (openTree == null)
// 		{
// logger.info("inittar");
// 			openTree = new TreeSet<MultithreadedNode>(new OpenTreeComparator(ESTIMATE_INDEX));
// 			closedTree = new TreeMap<Integer, MultithreadedNode>();
// 		}
// 		else 
// 		{
// logger.info("clearar");
// 			openTree.clear();
// 			closedTree.clear();
// 		}
// 	}

   /**
     * Walks through the tree of possible paths in search for the optimal one,
     * starting from the root node of this thread.
     */
    public void schedule()
		throws Exception
    {
        if (theAutomata == null)
        {
            throw new Exception("Choose several automata to schedule...");
        }
        else if (rootNode == null) // I.e. if this is the main thread...
        {		
			// Initiates the OPEN tree by adding the initial node, corresponding to the initial
			// state of the synchronous composition of the selected automata.
			MultithreadedNode currNode = makeInitialNode();
			openTree.add(currNode);

			while(! openTree.isEmpty())
			{
				if (isRunning)
				{
					iterationCounter++;
                
					// Selects the first node on OPEN. If it is accepting, the search is completed
					currNode = (MultithreadedNode) openTree.first();
					
					if (isAcceptingNode(currNode))
					{
						// This line is needed for correct backward search, performed during the schedule construction
						// IS IT???
						updateClosedTree(currNode);
						break;
					}
                

					// TODO: bättre open-hantering
					// The first open node is removed
					boolean succesfullyRemoved =  openTree.remove(currNode);
					if (! succesfullyRemoved)
					{
						throw new Exception("The node " + printNodeName(currNode) + " was not found on the openTree");
					}
                
					// If the node is not accepting, it goes to the CLOSED tree if there is not a node there already
					// that represents the same logical state and is better than the current node in all
					// "aspects" (lower cost in all directions). If the current node is promising (if it ends up on CLOSED),
					// its successors are found and put on the OPEN tree.
// 					branch(currNode);
					step(currNode);
				}
				else
				{
					return;
				}
			}
			
			if (currNode == null || !isAcceptingNode(currNode))
			{
				throw new RuntimeException("An accepting state could not be found, nr of iterations = " + iterationCounter);
			}

			outputStr += "\tA*-iterations (nr of search calls through the closed tree): " + iterationCounter + "\n";
			outputStr += "\tIn time: " + timer.elapsedTime() + " ms\n";
			outputStr += "\tThe CLOSED tree contains (at the end) " + closedTree.size() + " elements\n";
			outputStr += "\tMax{OPEN.size} = " + maxOpenSize + "\n";
			outputStr += "\t\t" + "g = " + currNode.getBasis()[ACCUMULATED_COST_INDEX];
			
			if (!isRelaxationProvider)
			{
				logger.info(outputStr);
			}
				
			this.acceptingNode = currNode;
			
			schedulingDone = true;				
		}
	}

	// TODO...
		//	protected void branch(double[] banan) {} //ta bort
	/**
     * Updates the closed tree if necessary, i.e. if the currently examined
     * node with better estimate value already is present on that tree.
     * If the node is added to the closed tree, its descendants are put on the
     * open tree, according to the estimated remaining cost value.
     */
    private double step(MultithreadedNode currNode)
    {
        try
        {
			ArrayList subthreads = currNode.getSubthreads();

			if (subthreads == null)
			{
				boolean currNodeIsAddedToClosed = updateClosedTree(currNode);
           
				if (currNodeIsAddedToClosed)
				{
					Iterator childIter = expander.expandNode(currNode, activeAutomataIndex).iterator();
                
					if (!expander.isUncontrollableEventFound())
					{
						while (childIter.hasNext())
						{
							Node nextNode = (Node)childIter.next();
							
							// Calculate the estimate function of the expanded node and store it at the appropriate position
							nextNode.setValueAt(ESTIMATE_INDEX, calcEstimatedCost(nextNode));
							
							openTree.add(nextNode);
						}
					}
					else
					{
						logger.info("uncontrollable");
					}
				}
			}

			return 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

		return 0;
    }

//     /**
//      * This method puts the node "node" in the right place on the closedTree if the tree does not already
//      * contain any nodes that correspond to the same logical states than this node.
//      * Otherwise, the method compares this node to those already examined. If this node is worse
//      * (more expensive in every future direction) that any already examined node, it is discarded.
//      * Conversely, if it is better than every examined node, this node is the only one to be stored in the
//      * closedTree. If there are ties (in some directions one node is better than the other, but in others
//      * it is worse), this node, as well as all the discovered tie nodes are stored in the closedTree
//      * (note that all examined nodes that are always worse than "node" are discarded).
//      *
//      * @param node the new node that might be added to the CLOSED tree.
//      * @return true, if the new node is added to the CLOSED tree
//      *         false, otherwise.
//      */
//     protected boolean updateClosedTree(MultithreadedNode node)
//     {
//         // The nodes corresponding to the same logical state (but different paths from the initial state)
//         // as the new node. They are stored as one double[]-variable in the closedTree.
//         MultithreadedNode correspondingClosedNode = closedTree.remove(new Integer((int)getKey(node)));
        
//         // If the node (or its logical state collegues) has not yet been put on the closedTree,
//         // then it is simply added to CLOSED.
//         if (correspondingClosedNode == null)
//         {
//             closedTree.put(new Integer((int)getKey(node)), node);
//         }
//         else
//         {
// 			double[] nodeBasis = node.getBasis();
// 			double[] correspondingClosedNodeBasis = correspondingClosedNode.getBasis();

//             // The internal indices of the nodes that can be either better or worse (cheaper or more expensive)
//             // than the current node, depending on the future path ("internal index" meaning the node's number in the
//             // correspondingClosedNodes-double[]-array).
//             ArrayList<Integer> tieIndices = new ArrayList<Integer>();
            
//             int nodeLength = nodeBasis.length;
//             int nrOfClosedNodes = correspondingClosedNodeBasis.length / nodeLength;
            
//             // Each "internal" node should be compared to the current node
//             for (int i=0; i<nrOfClosedNodes; i++)
//             {
//                 boolean newNodeIsAlwaysWorse = true;
//                 boolean newNodeIsAlwaysBetter = true;
                
//                 // The comparison is done for Tv_new[i] + g_new <> Tv_old[i] + g_old (forall i)
//                 for (int j=CURRENT_COSTS_INDEX; j<ACCUMULATED_COST_INDEX; j++)
//                 {
//                     double currCostDiff = (nodeBasis[j] + nodeBasis[ACCUMULATED_COST_INDEX]) - (correspondingClosedNodeBasis[j + i*nodeLength] + correspondingClosedNodeBasis[ACCUMULATED_COST_INDEX + i*nodeLength]);
                    
//                     if (currCostDiff < 0)
//                     {
//                         newNodeIsAlwaysWorse = false;
//                     }
//                     else if (currCostDiff > 0)
//                     {
//                         newNodeIsAlwaysBetter = false;
//                     }
//                 }
                
//                 // If the new node is worse than any already examined node in every future direction,
//                 // it is thrown away;
//                 if (newNodeIsAlwaysWorse)
//                 {
//                     closedTree.put(new Integer((int)getKey(node)), correspondingClosedNode);
//                     return false;
//                 }
//                 // else if the examined node is neither worse nor better, its index is added to the tieIndices
//                 else if (!newNodeIsAlwaysWorse && !newNodeIsAlwaysBetter)
//                 {
//                     tieIndices.add(new Integer(i));
//                 }
//             }
            
//             // Only ties (and the new node) are kept for the update of the closedTree.
//             double[] newClosedNodeBasis = new double[(tieIndices.size() + 1)*nodeLength];
            
//             // The tie-nodes (if there are any) are copied to the new closedNode
//             for (int i=0; i<tieIndices.size(); i++)
//             {
//                 int currExaminedNodesIndex = tieIndices.get(i).intValue();
//                 for (int j=0; j<nodeLength; j++)
//                 {
//                     newClosedNodeBasis[j + i*nodeLength] = correspondingClosedNodeBasis[j + currExaminedNodesIndex*nodeLength];
//                 }
//             }
            
//             // The latest addition to the node-family (double[] node) is also added to the new closedNode
//             for (int j=0; j<nodeLength; j++)
//             {
//                 newClosedNodeBasis[j + tieIndices.size()*nodeLength] = nodeBasis[j];
//             }
            
// 			MultithreadedNode newClosedNode = new MultithreadedNode(newClosedNodeBasis, correspondingClosedNode.getSubthreads());
//             closedTree.put(new Integer((int)getKey(node)), newClosedNode);
//         }
        
//         return true;
//     }

	/**
	 * Converts the double[]-representation of the initial node to a 
	 * MultithreadedNode-representation.
	 *
	 * @return the multithreaded node containing the double[]-representation of the initial state.
	 */
	protected MultithreadedNode makeInitialNode()
	{
		return new MultithreadedNode(makeInitialNodeBasis(), null);
	}

 //    /**
//      * Starting from an accepting state/node, this method walks its way back,
//      * using keys stored in PARENT_INDEX to find parents. When this is done,
//      * an event, connecting the two nodes is found and added to the schedule,
//      * while the parent becomes the next node in search of its parent. This
//      * is done until an initial node is found, which completes the construction.
//      */
//     public void buildScheduleAutomaton()
// 		throws Exception
//     {
//         timer.restart();
        
//         Automaton scheduleAuto = new Automaton();
//         scheduleAuto.setComment("Schedule");

// 		double[] currNodeBasis = acceptingNode.getBasis();
        
//         State nextState = new State(printNodeSignature(currNode));
//         nextState.setAccepting(true);
//         scheduleAuto.addState(nextState);
        
//         while (hasParent(currNodeBasis))
//         {
//             try
//             {
//                 if (isRunning)
//                 {
//                     double[] parentBasis = getParent(currNodeBasis);
//                     State currState = new State(printNodeSignature(parentBasis) + "; firing time = " + currNodeBasis[ACCUMULATED_COST_INDEX]);
//                     LabeledEvent event = findCurrentEvent(parentBasis, currNodeBasis);
                    
//                     if (!hasParent(parentBasis))
// 					{
//                         currState.setInitial(true);
//                     }

//                     scheduleAuto.addState(currState);
//                     scheduleAuto.getAlphabet().addEvent(event);
//                     scheduleAuto.addArc(new Arc(currState, nextState, event));
                    
//                     currNodeBasis = parentBasis;
//                     nextState = currState;
//                 }
//                 else
//                 {
//                     return;
//                 }
//             }
//             catch (Exception ex)
//             {
//                 logger.error("ModifiedAstar::buildScheduleAutomaton() --> Could not find the arc between " + printArray(currNodeBasis) + " and its parent" + printNodeName(getParent(currNodeBasis)));
//                 logger.debug(ex.getStackTrace());
                
//                 throw ex;
//             }
//         }
        
//         // If a dummy event has been added to any of the robots alphabets,
//         // it is also added to the schedule, thus making it return from its
//         // accepting to its initial state.
//         if (plantAutomata.getUnionAlphabet().contains(dummyEventName))
//         {
//             LabeledEvent resetEvent =  new LabeledEvent(dummyEventName);
//             scheduleAuto.getAlphabet().addEvent(resetEvent);
//             scheduleAuto.addArc(new Arc(scheduleAuto.getStateWithName(printNodeSignature(acceptingNode.getBasis())), scheduleAuto.getInitialState(), resetEvent));
//         }
        
//         logger.info("Schedule was built in " + timer.elapsedTime() + "ms");
//         ActionMan.getGui().addAutomaton(scheduleAuto);
//     }
}