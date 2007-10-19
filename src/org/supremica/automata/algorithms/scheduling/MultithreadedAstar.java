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
import org.supremica.util.ActionTimer;

public class MultithreadedAstar
    extends ModifiedAstar
{    
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
    
    private MultithreadedAstar parentThread = null;
    private MultithreadedNode rootNode = null;
    private double branchingProbality;
    private static final double DEFAULT_PROBABILITY = 1;
    private static final double UNSUCCESSFUL_SEARCH_VALUE = Double.NEGATIVE_INFINITY;
    private ArrayList<Node> scheduleInfo;
// 	private int nrOfSubthreads = 0;
// 	private int maxClosedSize = 0;
    
    public MultithreadedAstar(Automata theAutomata, String heuristic, boolean manualExpansion, boolean buildSchedule, boolean isRelaxationProvider)
    throws Exception
    {
        this(theAutomata, heuristic, manualExpansion, buildSchedule, isRelaxationProvider, null);
    }
    
    public MultithreadedAstar(Automata theAutomata, String heuristic, boolean manualExpansion, boolean buildSchedule, boolean isRelaxationProvider, MultithreadedNode rootNode)
    throws Exception
    {
        this(theAutomata, heuristic, manualExpansion, buildSchedule, isRelaxationProvider, rootNode, DEFAULT_PROBABILITY);
    }
    
    public MultithreadedAstar(Automata theAutomata, String heuristic, boolean manualExpansion, boolean buildSchedule, boolean isRelaxationProvider, MultithreadedNode rootNode, double branchingProbality)
    throws Exception
    {
        super(theAutomata, heuristic, manualExpansion, buildSchedule, isRelaxationProvider);
        
        this.rootNode = rootNode;
        this.branchingProbality = branchingProbality;
        
        if (rootNode != null)
        {
            this.isRelaxationProvider = true;
        }
    }
    
    public MultithreadedAstar(MultithreadedAstar parentThread, MultithreadedNode rootNode, double branchingProbality)
    {
        this.parentThread = parentThread;
        theAutomata = parentThread.getAllAutomata();
        plantAutomata = parentThread.getPlantAutomata();
        heuristic = parentThread.getHeuristic();
        manualExpansion = parentThread.isManualExpansion();
        buildSchedule = parentThread.getBuildSchedule();
        isRelaxationProvider = true; // If there is a parent thread, then this thread is a subthread, i.e. a relaxationProvider
        expander = parentThread.getNodeExpander();
        relaxer = parentThread.getRelaxer();
        keyMapping = parentThread.getKeyMapping();
        activeAutomataIndex = parentThread.getActiveAutomataIndex();
        indexMap = parentThread.getIndexMap();
        
        this.rootNode = rootNode;
        this.branchingProbality = branchingProbality;
    }
    
    protected void init()
    throws Exception
    {
        if (rootNode == null)
        {
            super.init();
            
            // Initiates the OPEN tree by adding the initial node, corresponding to the initial
            // state of the synchronous composition of the selected automata.
            openTree.add(makeInitialNode());
        }
        else
        {
            initTrees();
            
            openTree.add(rootNode);
        }
    }
    
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
            ActionTimer localTimer = new ActionTimer();
            localTimer.restart();
            while(! openTree.isEmpty())
            {
                if (isRunning)
                {
                    // If the node is not accepting, it goes to the CLOSED tree if there is not a node there already
                    // that represents the same logical state and is better than the current node in all
                    // "aspects" (lower cost in all directions). If the current node is promising (if it ends up on CLOSED),
                    // its successors are found and put on the OPEN tree.
                    double currEstimatedCost = step();
                    if (acceptingNode != null)
                    {
                        break;
                    }
                }
                else
                {
                    return;
                }
            }
            
            infoMsgs += "\tA*-iterations (nr of search calls through the closed trees): " + iterationCounter + "\n";
            infoMsgs += "\tIn time: " + localTimer.elapsedTime() + " ms\n";
// 			infoMsgs += "\tThe CLOSED tree contains (at the end) " + closedTree.size() + " elements\n";
// 			infoMsgs += "\tMax{OPEN.size} = " + maxOpenSize + "\n";
            if (acceptingNode != null)
            {
                infoMsgs += "\t\t" + "g = " + acceptingNode.getBasis()[ESTIMATE_INDEX];
            }
            else
            {
                errorMsgs += "An accepting state could not be found";
            }
                        
            schedulingDone = true;
        }
        else // I.e. if this is a subthread...
        {
// 			warnMsgs += "Adding " + printNodeName(rootNode) + " to a new open list";
// 			openTree.add(rootNode);
        }
    }
    
    /**
     * Updates the closed tree if necessary, i.e. if the currently examined
     * node with better estimate value already is present on that tree.
     * If the node is added to the closed tree, its descendants are put on the
     * open tree, according to the estimated remaining cost value.
     */
    private synchronized double step()
    throws Exception
    {
        if (openTree == null || openTree.size() == 0)
        {
            // Should return -INFINITY in subthreads. If this is the main
            // thread on the other hand, then the search was unsuccessful.
            if (isRelaxationProvider)
            {
                return Double.NEGATIVE_INFINITY;
            }
            else
            {
                throw new RuntimeException("The OPEN list is empty, while an accepting state was not found, nr of iterations = " + iterationCounter);
            }
        }
        
        // Selects the first node on OPEN. If it is accepting, the search is completed
        MultithreadedNode currNode = (MultithreadedNode) openTree.first();
        
        if (isAcceptingNode(currNode))
        {
            // This line is needed for correct backward search, performed during the schedule construction
            // IS IT???
            updateClosedTree(currNode);
            this.acceptingNode = currNode;
// 			this.acceptingNode.setValueAt(ACCUMULATED_COST_INDEX, currNode.getValueAt(ESTIMATE_INDEX));
            collectScheduleInfo();
            return currNode.getValueAt(ESTIMATE_INDEX);
        }
        
// 		// Records the maximum size of the openTree
// 		int currOpenSize = openTree.size();
// 		if (currOpenSize > maxOpenSize)
// 		{
// 			maxOpenSize = currOpenSize;
// 		}
        
        // TODO: better open-handling
        // The first open node is removed
        boolean succesfullyRemoved =  openTree.remove(currNode);
        if (! succesfullyRemoved)
        {
            throw new Exception("The node " + printNodeName(currNode) + " was not found on the openTree");
        }
        
        ArrayList subthreads = currNode.getSubthreads();
        
        if (subthreads == null)
        {
            // Iterations are counted only in active subthreads
            iterationCounter++;
            
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
                    
                    return currNode.getValueAt(ESTIMATE_INDEX);
                }
                else
                {
                    double currEstimatedCost = 0;
                    while (childIter.hasNext())
                    {
                        MultithreadedNode subRootNode = (MultithreadedNode)childIter.next();
                        subRootNode.setValueAt(PARENT_INDEX, ROOT_VALUE);
                        
                        Arc connectingArc = findConnectingArc(currNode, subRootNode);
                        
                        MultithreadedAstar subthread = new MultithreadedAstar(this, subRootNode, connectingArc.getProbability());
                        
                        currNode.addSubthread(subthread);
                        
                        // TODO... Synchronization could be improved
                        subthread.startSearchThread();
                        while (!subthread.isInitialized())
                        {
                            sleep(1);
                        }
                        
                        double currStepValue = subthread.step();
                        if (currStepValue == UNSUCCESSFUL_SEARCH_VALUE)
                        { // If any of the subthreads leads to a deadlock, all (uncontrollable) subthreads are dismissed
                            currEstimatedCost = UNSUCCESSFUL_SEARCH_VALUE;
                            break;
                        }
                        else
                        {
                            currEstimatedCost += subthread.getBranchingProbability() * currStepValue;
                        }
                    }
                    
                    // Add the node to the open tree only if it might lead to a marked state
                    if (currEstimatedCost != UNSUCCESSFUL_SEARCH_VALUE)
                    {
                        currNode.setValueAt(ESTIMATE_INDEX, currEstimatedCost);
                        openTree.add(currNode);
                    }
                    
                    return currEstimatedCost;
                }
            }
            else // If the currently opened node is worse than a previously examined node, open the next in turn
            {
                return step();
            }
        }
        else // Current node has already at least one subthread
        {
            double currEstimatedCost = 0;
            for (MultithreadedAstar subthread : currNode.getSubthreads())
            {
                double currStepValue = subthread.step();
                if (currStepValue == UNSUCCESSFUL_SEARCH_VALUE)
                { // If any of the subthreads leads to a deadlock, all (uncontrollable) subthreads are dismissed
                    currEstimatedCost = UNSUCCESSFUL_SEARCH_VALUE;
                    break;
                }
                else
                {
                    currEstimatedCost += subthread.getBranchingProbability() * currStepValue;
                }
            }
            
            // Add the node to the open tree only if it might lead to a marked state
            if (currEstimatedCost != UNSUCCESSFUL_SEARCH_VALUE)
            {
                currNode.setValueAt(ESTIMATE_INDEX, currEstimatedCost);
                openTree.add(currNode);
            }
            
            return currEstimatedCost;
        }
    }
    
    /**
     * Returns true if all the states that this node represents are accepting.
     *
     * @param node the current node
     * @return true if all the corresponing states are accepting
     */
    protected boolean isAcceptingNode(MultithreadedNode node)
    {
        if (node.getSubthreads() == null)
        {
            return super.isAcceptingNode(node);
        }
        else
        {
            for (MultithreadedAstar subthread : node.getSubthreads())
            {
                if (!subthread.isAcceptingThread())
                {
                    return false;
                }
            }
            
            return true;
        }
    }
    
    public boolean isAcceptingThread()
    {
        if (acceptingNode != null)
        {
            return true;
        }
        
        return false;
    }
    
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
    
    public double getBranchingProbability()
    {
        return branchingProbality;
    }
    
    private void collectScheduleInfo()
    throws Exception
    {
        if (scheduleInfo == null)
        {
            scheduleInfo = new ArrayList<Node>();
            Node currNode = acceptingNode;
            
            while (hasParent(currNode))
            {
                scheduleInfo.add(0, currNode);
                currNode = getParent(currNode);
            }
            
            scheduleInfo.add(0, currNode);
        }
    }
    
    /**
     * Starting from an accepting state/node, this method walks its way back,
     * using keys stored in PARENT_INDEX to find parents. When this is done,
     * an event, connecting the two nodes is found and added to the schedule,
     * while the parent becomes the next node in search of its parent. This
     * is done until an initial node is found, which completes the construction.
     */
    public void buildScheduleAutomaton()
        throws Exception
    {   
        if (acceptingNode != null)
        {
            timer.restart();
            
            scheduleAuto = new Automaton("Schedule");
            
            buildScheduleFromArray(scheduleInfo);
            
            // If a dummy event has been added to any of the robots alphabets,
            // it is also added to the schedule, thus making it return from its
            // accepting to its initial state.
            if (plantAutomata.getUnionAlphabet().contains(dummyEventName))
            {
                LabeledEvent resetEvent = new LabeledEvent(dummyEventName);
                scheduleAuto.getAlphabet().addEvent(resetEvent);
                
                for (Iterator<State> stateIt = scheduleAuto.stateIterator(); stateIt.hasNext(); )
                {
                    State accState = stateIt.next();
                    if (accState.isAccepting())
                    {
                        scheduleAuto.addArc(new Arc(accState, scheduleAuto.getInitialState(), resetEvent));
                    }
                }
            }
            
            infoMsgs += "Schedule was built in " + timer.elapsedTime() + "ms";
        }
    }
    
    public String buildScheduleFromArray(ArrayList<Node> scheduleInfo)
    throws Exception
    {
        // Only a state that do not have successors should use -1
        // as the firing time. Otherwise, the accumulated time of the
        // successor is the firing time of the current state.
        // Note that in case of subthreads, the current state is left
        // immediately following some uncontrollable choice event. Thus
        // firing_time = accumulated_time.
        double firingTime = -1;
        if (scheduleInfo.size() > 1)
        {
            firingTime = scheduleInfo.get(1).getValueAt(ACCUMULATED_COST_INDEX);
        }
        else if (((MultithreadedNode) scheduleInfo.get(0)).getSubthreads() != null)
        {
            firingTime = scheduleInfo.get(0).getValueAt(ACCUMULATED_COST_INDEX);
        }
        State currState = makeStateFromNode(scheduleInfo.get(0), scheduleAuto, firingTime);
        String rootName = currState.getName();
        
        if (scheduleAuto.nbrOfStates() == 0)
        {
            currState.setInitial(true);
        }
        
        scheduleAuto.addState(currState);
        
        for (int i=1; i<scheduleInfo.size(); i++)
        {
            // Only a state that do not have successors should use -1
            // as the firing time. Otherwise, the accumulated time of the
            // successor is the firing time of the current state.
            // Note that in case of subthreads, the current state is left
            // immediately following some uncontrollable choice event. Thus
            // firing_time = accumulated_time.
            firingTime = -1;
            if (i < scheduleInfo.size() - 1)
            {
                firingTime = scheduleInfo.get(i+1).getValueAt(ACCUMULATED_COST_INDEX);
            }
            else if (((MultithreadedNode) scheduleInfo.get(i)).getSubthreads() != null)
            {
                firingTime = scheduleInfo.get(i).getValueAt(ACCUMULATED_COST_INDEX);
            }
            State nextState = makeStateFromNode(scheduleInfo.get(i), scheduleAuto, firingTime);
            scheduleAuto.addState(nextState);
            
            LabeledEvent event = findConnectingEvent(scheduleInfo.get(i-1), scheduleInfo.get(i));
            if (!scheduleAuto.getAlphabet().contains(event))
            {
                scheduleAuto.getAlphabet().addEvent(event);
            }
            scheduleAuto.addArc(new Arc(currState, nextState, event));
            
            currState = nextState;
        }
        
        ArrayList<MultithreadedAstar> subthreads = ((MultithreadedNode) scheduleInfo.get(scheduleInfo.size()-1)).getSubthreads();
        if (subthreads == null)
        {
            currState.setAccepting(true);
        }
        else
        {
            for (MultithreadedAstar subthread : subthreads)
            {
                String subRootName = buildScheduleFromArray(subthread.getScheduleInfo());
                State subRootState = scheduleAuto.getStateWithName(subRootName);
                
                LabeledEvent event = findConnectingEvent(scheduleInfo.get(scheduleInfo.size()-1), subthread.getScheduleInfo().get(0));
                if (!scheduleAuto.getAlphabet().contains(event))
                {
                    scheduleAuto.getAlphabet().addEvent(event);
                }
                scheduleAuto.addArc(new Arc(currState, subRootState, event));
            }
        }
        
        return rootName;
    }
    
    public ArrayList<Node> getScheduleInfo()
    {
        return scheduleInfo;
    }
}