/******************** ModifiedAstar.java **************************
 * AKs implementation of Tobbes modified Astar search algo
 * Basically this is a guided tree-search algorithm, like
 *      list processed = 0;             // closed
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.AutomataIndexMap;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.util.ActionTimer;


public class ModifiedAstar
    implements Scheduler
{
    /****************************************************************************************/
    /*                                 VARIABLE SECTION                                     */
    /****************************************************************************************/

    /** The indices of important parameters, that help to find them in the int[]-representions of the nodes. */
    public static int ESTIMATE_INDEX, ACCUMULATED_COST_INDEX, CURRENT_COSTS_INDEX, PARENT_INDEX;

	protected static double ROOT_VALUE = -1;

    /**
     * Contains promising search tree nodes, i.e. nodes that might lie on the optimal path.
     * They are "opened" but not yet examined (i.e. not "closed").
     * OPEN list is better represented as a tree, ordered by the estimate values, f(n),
     * while the tree vertices contain the double[] representations of the "opened" nodes.
     *
     * double[] node = [state_0_index, ..., state_m_index, -1, 0,
     *               parent_node_key, index_of_curr_parent_within_parent_node
     *               (-1 if only one instance of the parent have been opened),
     *               current_costs_0, ..., current_costs_k,
     *               accumulated_cost, estimate_value],
     * m being the number of selected automata, k the number of selected plants (robots),
     * current_costs are the Tv(n)-values, accumulated_cost = g(n) and estimate_value = f(n).
     * (-1 and 0 are due to Supremicas AutomataIndexFormHelper information, namely
     * AutomataIndexFormHelper.STATE_EXTRA_DATA).
     */
    protected TreeSet<Node> openTree;

    /**
     * Contains already examined (i.e. "closed") search tree nodes (or rather their int[]-representations
     * @see #openTree ).
     * For efficiency, CLOSED should be represented as a tree. It is faster to search through
     * than a list and takes less place than a hashtable.
     * If several nodes, corresponding to the same logical state, have been examined and
     * none of them is guaranteed to be better than the other, all the nodes are stored
     * as one double[]-variable. The closed nodes are unrolled and compared when necessary,
     * for example when a new node is to be added to the closedTree. This is done by
     * @see ModifiedAstar#updateClosedTree(Node node) .
     */
    protected TreeMap<Integer, Node> closedTree;

    /** Hashtable containing the estimated cost for each combination of two robots **/
    //     protected Hashtable[] twoProdRelax;

    /** The selected automata */
    protected Automata theAutomata, plantAutomata;

    /**
     * Contains the indices of the selected plants, i.e. activeAutomataIndex[0] contains the
     * index of the zeroth plant in the overall Automata, "theAutomata".
     */
    protected int[] activeAutomataIndex;

    /** Counts the number of iterations */
    public static int iterationCounter;

    /** Starts ticking when the search/walk through the nodes is
     * started. Shows the duration of the scheduling. */
    protected ActionTimer timer;

    /** Handles the expansion of nodes - either manually or using Supremicas methods */
    protected NodeExpander expander;

    /** Responsible for correct indexing of automata, states and events */
    protected AutomataIndexMap indexMap;


    /** Is responsible for the correct relaxation/estimation that guides the A* search */
    protected Relaxer relaxer;

    /** Is used to translate the state indices to unique hash values */
    protected int[] keyMapping;

    /**
     * Decides whether 1-prod-relaxation should be used. Note that this variable is somewhat
     * different from 'String heuristic'. For example, if heuristic = "2-product relax",
     * 1-product relaxation should be carried out first to collect enough information for the
     * 2-product relaxation. During this first phase, useOneProdRelax would be true although
     * heuristic = "2-product relax".
     */
//     protected boolean useOneProdRelax;

	//@Deprecated (use infoMsgs instead)
    ///** This string contains info about the scheduling, such as time, nr of iterations etc. */
    //protected String outputStr =  "";

    /**
     * Which heuristic should be chosen. The value is normally supplied by the
     * user through org.supremica.gui.ScheduleDialog.java.
     * If not, the default value is "1-product relax".
     */
    protected String heuristic = "";

    /** Stores the maximum size of the openTree. */
    protected int maxOpenSize = 0;

    /**
     * If true, an iterative deepening search is used, which can be slower than
     * the simple modified A*, but requires less memory.
     * THIS FEATURE IS HOWEVER NOT IMPLEMENTED.
     */
//     protected boolean iterativeSearch;

    /**
     * If this variable is set to true, the consistensy of the heuristic is guaranteed,
     * at the cost of some extra operations.
     */
    protected boolean consistentHeuristic = false;

    /** Stores the accepting node of the resulting schedule (with a reference to the ancestor node. */
    protected Node acceptingNode = null;

    /** This boolean is true if the scheduler-thread should be (is) running */
    protected volatile boolean isRunning = false;

    /** Decides if the schedule should be built */
    protected boolean buildSchedule;

    protected boolean balanceVelocities;

    /**
     * If this boolean is true, node expansion is done manually
     * (i.e. not using Supremica's synchronization methods)
     */
    protected boolean manualExpansion;

	/*
	 * The thread that performs the search for the optimal solution
	 */
    protected Thread astarThread;

    /**
     * Assures that the scheduling is done (is needed for correct functionning when
     * this thread is started from another thread for relaxation purposes).
     */
    protected volatile boolean schedulingDone;

    /**
     * Assures that the mutex zones of the graph have been initialized
     * (is needed for correct functionning when this thread is started from
     * another thread for relaxation purposes).
     */
    protected volatile boolean isInitialized = false;

    /**
     * The optimal makespan value
     */
    protected int makespan = -1;

    /**
     * Determines if this scheduler is only used for relaxation
     * (and is thus called by another scheduler)
     */
    protected volatile boolean isRelaxationProvider = true;

    /**
     * A dummy event that returns the schedule automaton from its accepting
     * to its initial state. Needed to describe repetitive working cycles.
     */
    String dummyEventName = "reset";

    protected Automaton scheduleAuto = null;

    /**
     * Used to guide the A* towards suboptimal solution. Contains x-weight and
     * y-weight. The formula for adjusting the estimation value (f(n)) is:
     * f(node) * ( 1 + x-weight / y-weight + node-depth)).
     */
    private double[] approximationWeights = null;

    /** The inidices of the aproximation weights. */
    private final static int X_WEIGHT_INDEX = 0;
    private final static int Y_WEIGHT_INDEX = 1;

	protected String infoMsgs = "";
	protected String warnMsgs = "";
	protected String errorMsgs = "";
	protected ArrayList<StackTraceElement[]> debugMsgs = new ArrayList<StackTraceElement[]>();

	private final static String SEPARATOR = "_";	// Subtle change here due to circumstances beyond my (MF) control, dot was hijacked
													// *hopefully* does not break stuff! Compare Milp.java

    /****************************************************************************************/
    /*                                 CONSTUCTORS                                          */
    /****************************************************************************************/

    public ModifiedAstar() {}

    public ModifiedAstar(final Automata theAutomata, final String heuristic, final boolean manualExpansion, final boolean buildSchedule,
            final boolean balanceVelocities, final boolean isRelaxationProvider)
		throws Exception
    {
        this.theAutomata = theAutomata;
        this.heuristic = heuristic;
        this.manualExpansion = manualExpansion;
        this.buildSchedule = buildSchedule;
        this.balanceVelocities = balanceVelocities;
        this.isRelaxationProvider = isRelaxationProvider;

        init();
    }

    public ModifiedAstar(final Automata theAutomata, final String heuristic, final boolean manualExpansion, final boolean buildSchedule, final boolean balanceVelocities)
            throws Exception
    {
        this(theAutomata, heuristic, manualExpansion, buildSchedule, balanceVelocities, false);
    }

    public ModifiedAstar(final Automata theAutomata, final String heuristic, final boolean manualExpansion, final boolean buildSchedule)
            throws Exception
    {
        this(theAutomata, heuristic, manualExpansion, buildSchedule, false, false);
    }

    public ModifiedAstar(final Automata theAutomata, final String heuristic, final boolean manualExpansion, final boolean buildSchedule, final boolean balanceVelocities, final double[] approximationWeights)
            //ScheduleDialog scheduleDialog, double[] approximationWeights)
            throws Exception
    {
        this(theAutomata, heuristic, manualExpansion, buildSchedule, false);

        if (approximationWeights.length != 2)
        {
            throw new Exception("Exactly 2 weights should be used to guide the A* towards a suboptimal solution!");
        }

        this.approximationWeights = new double[approximationWeights.length];
        for (int i = 0; i < approximationWeights.length; i++)
        {
            this.approximationWeights[i] = approximationWeights[i];
        }
    }


    /****************************************************************************************/
    /*                                 INIT METHODS                                         */
    /****************************************************************************************/

    @Override
    public void startSearchThread()
    {
        astarThread = new Thread(this);
        isRunning = true;
        astarThread.start();
    }

    @Override
    public void run()
    {
        try
        {
            final ActionTimer totalTimer = new ActionTimer();

            if (!isRelaxationProvider)
            {
                if (isRunning)
                {
                    totalTimer.restart();
                    schedule();

                    final String totalTimeStr = "Total optimization time = " + totalTimer.elapsedTime() + "ms";
                    infoMsgs += "\t" + totalTimeStr + "\n";
                }

                if (isRunning && buildSchedule)
                {
                    buildScheduleAutomaton();
                }

                if (isRunning && buildSchedule && balanceVelocities)
                {
                    balanceVelocities();
                }

                if (isRunning)
                {
                    requestStop(true);
                }
                else
                {
                    errorMsgs += "Scheduling interrupted, openTree.size = " + openTree.size() + "; closedTree.size = " + closedTree.size();
                }
            }
        }
        catch (final Exception ex)
        {
                errorMsgs += "A_star::schedule() -> " + ex;
                debugMsgs.add(ex.getStackTrace());
        }
    }

    protected void init()
        throws Exception
    {
        if (theAutomata == null)
        {
            return;
        }

        timer = new ActionTimer();
        timer.restart();

        plantAutomata = theAutomata.getPlantAutomata();

		// This Automata instance collects the original copies of such plant automata
		// that are changed due to addition of dummy event. This is needed to restore
		// the original picture at schedule construction.
// 		alteredAutomata = new Automata();

        // Finds a string that is not contained in any plant alphabet,
        // to be used as the label for the dummy event.
        while (plantAutomata.getUnionAlphabet().contains(dummyEventName))
        {
            dummyEventName += "1";
        }

        // Adds a dummy state in all automata that start on an accepting
        // state. By this, the search can be performed and all the plants that return
        // to their initial states are returned simultaneously with help of a dummy
        // "reset"-event, whereafter their working cycles can restart synchronized.
        SchedulingHelper.preparePlantsForScheduling(theAutomata.getPlantAutomata());

        // Creates an instance of a class that is resposible for the expansion of
        // the synchronized states.
        expander = new NodeExpander(manualExpansion, false, theAutomata, this);

        // Fetches the AutomataIndexMap from the NodeExpander
        indexMap = expander.getIndexMap();

        // Creates an instance of the relaxation class (search guidance), as
        // specified by the user through the GUI.
        initRelaxer();

        //Borde r?cka med plantAutomata.size(), fast d? kanske man m?ste ?ndra lite p? andra st?llen ocks?
        keyMapping = new int[theAutomata.size()];
        keyMapping[0] = 1;
        for (int i=1; i<keyMapping.length; i++)
        {
            keyMapping[i] = keyMapping[i-1] * (indexMap.getAutomatonAt(i-1).nbrOfStates() + 1);
        }

        infoMsgs += "Processing times:\n";

        // Initiates the important node indices that are used throughout the search
        initAuxIndices();

        // Initiates the open and the closed trees.
        initTrees();

        final String initTimeStr = "Initialization time = " + timer.elapsedTime() + "ms";
		infoMsgs += "\t" + initTimeStr + "\n";

        isInitialized = true; // Is needed for synchronization purposes
    }

    protected void initAuxIndices()
    {
        activeAutomataIndex = new int[plantAutomata.size()];
        for (int i=0; i<activeAutomataIndex.length; i++)
        {
            activeAutomataIndex[i] = indexMap.getAutomatonIndex(plantAutomata.getAutomatonAt(i));
        }

        PARENT_INDEX = theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA;

        // Detta borde ?ndras till STATIC_VARIBEL = 1 (och inte 2) => 2 ?ndringar
        CURRENT_COSTS_INDEX = PARENT_INDEX + 2; //ClosedNodes.CLOSED_NODE_INFO_SIZE;
        //ACCUMULATED_COST_INDEX = CURRENT_COSTS_INDEX + activeAutomataIndex.length;
        ACCUMULATED_COST_INDEX = CURRENT_COSTS_INDEX + plantAutomata.size();
        ESTIMATE_INDEX = ACCUMULATED_COST_INDEX + 1;
    }

    /**
     * Creates an instance of Relaxer corresponding to the chosen heuristic.
     */
    protected void initRelaxer()
        throws Exception
    {
		if (heuristic.equals(SchedulingConstants.ONE_PRODUCT_RELAXATION))
        {
            relaxer = new OneProductRelaxer(expander, this);
        }
		else if (heuristic.equals(SchedulingConstants.TWO_PRODUCT_RELAXATION))
        {
            // if (theAutomata.getPlantAutomata().size() < 3)
            //             {
            //                 throw new Exception("2-product relax cannot be used for two or less products");
            //             }

            //             useOneProdRelax = false;
            throw new Exception("'2-product relax' is currently too buggishly implemented.");
        }
		else if (heuristic.equals(SchedulingConstants.VIS_GRAPH_TIME_RELAXATION))
        {
            relaxer = new VisibilityGraphRelaxer(expander, this, false);
        }
		else if (heuristic.equals(SchedulingConstants.VIS_GRAPH_NODE_RELAXATION))
        {
            relaxer = new VisibilityGraphRelaxer(expander, this, true);
        }
		else if (heuristic.equals(SchedulingConstants.BRUTE_FORCE_RELAXATION))
        {
            relaxer = new BruteForceRelaxer();
        }
    }

    /**
     * Resets or initializes the OPEN and the CLOSED trees.
     */
	protected void initTrees()
    {
        if (openTree == null)
        {
            openTree = new TreeSet<Node>(new OpenTreeComparator(ESTIMATE_INDEX));
            closedTree = new TreeMap<Integer, Node>();
        }
        else
        {
            openTree.clear();
            closedTree.clear();
        }
    }

	@Override
    public void requestAbort()
    {
        requestStop(false);
    }

    @Override
    public boolean isAborting()
    {
        return !isRunning;
    }

    @Override
    public void resetAbort(){
      isRunning = true;
    }

    public void requestStop(final boolean disposescheduleDialog)
    {
        isRunning = false;
        astarThread = null;
/*
        if (disposescheduleDialog)
        {
            scheduleDialog.done();
        }
 */
    }

    /**
     * Converts the representation of the initial state to the double[]-representation of a node.
     * double[] node consists of [states.getIndex() AutomataIndexFormHelper-info (-1 0 normally)
     * parentStates.getIndex() states.getCurrentCost() accumulatedCost estimatedCost].
     * double[] initialNode is thus [initialStates.getIndex() AutomataIndexFormHelper-info
     * null initialStates.getCost() 0 -1].
     */
    protected double[] makeInitialNodeBasis()
    {
        return expander.makeInitialNodeBasis();
  //       int[] initialStates = AutomataIndexFormHelper.createState(theAutomata.size());
//         double[] initialCosts = new double[getActiveLength() + 2];

//         int nrOfPlants = plantAutomata.size();

//         // Initial state indices are stored
//         for (int i=0; i<theAutomata.size(); i++)
//         {
//             initialStates[i] = theAutomata.getAutomatonAt(i).getInitialState().getIndex();
//         }

//         // Initial state costs are stored
//         for (int i=0; i<nrOfPlants; i++)
//         {
//             initialCosts[i] = plantAutomata.getAutomatonAt(i).getInitialState().getCost();
//         }

//         // The initial accumulated cost is zero
//         initialCosts[nrOfPlants] = 0;

//         // The initial estimate is set to -1
//         initialCosts[nrOfPlants + 1] = -1;

//         // The NodeExpander combines the information, together with the parent-information,
//         // which is null for the initial state.
// 		// 		return expander.makeNode(initialStates, null, initialCosts);
//         return expander.makeNodeBasis(initialStates, -1, initialCosts);
    }

    /**
     * Converts the double[]-representation of the initial node to a Node-representation.
     * In this case, the result is returned in the form of a BasicNode-instance.
     * Overriden in subclasses.
     *
     * @return the basic node containing the double[]-representation of the initial state.
     */
    protected Node makeInitialNode()
    {
        return new BasicNode(makeInitialNodeBasis());
    }


    /****************************************************************************************/
    /*                                 THE A*-ALGORITHM                                     */
    /****************************************************************************************/

    /**
     * Walks through the tree of possible paths in search for the optimal one,
     * starting from the initial node.
     */
    @Override
    public void schedule()
		throws Exception
    {
        if (theAutomata == null)
        {
            throw new Exception("Choose several automata to schedule...");
        }
        else
        {
            scheduleFrom(makeInitialNode());
        }
    }

    public double scheduleFrom(Node currNode)
        throws Exception
    {
        schedulingDone = false;

        timer.restart();
        iterationCounter = 0;

        // Initiates the OPEN tree by adding the initial node, corresponding to the initial
        // state of the synchronous composition of the selected automata.
        openTree.add(currNode);

        //tillf
        // 		Runtime jvm = Runtime.getRuntime();
        // 		ActionTimer cleanUpTimer = new ActionTimer();
        // 		cleanUpTimer.restart();

        while(! openTree.isEmpty())
        {
            if (isRunning)
            {
                //tillf (Throws away the tail of the OPEN list)
                // 			    if (jvm.freeMemory() < jvm.maxMemory() / 100)
                // 				{
                // 					logger.warn("Almost run out of memory, the time since clean up = " + cleanUpTimer.elapsedTime() + "ms. FORCED CLEAN UP STARTED..........");
                // 					logger.info("Before clean up -> openTree.size = " + openTree.size() + "; closedTree.size = " + closedTree.size());
                // 					int cleanUpSize = openTree.size()/4;
                // 					double[] firstNodeToBeThrownAway = null;
                // 					double estimateToBeThrownAway = -1;
                // 					for (Iterator<double[]> it = openTree.iterator(); it.hasNext(); )
                // 					{
                // 						double[] currOpenNode = it.next();
                // 						double currEstimate = currOpenNode[ESTIMATE_INDEX];
                // 						if (currEstimate != estimateToBeThrownAway && cleanUpSize > 0)
                // 						{
                // 							estimateToBeThrownAway = currEstimate;
                // 							firstNodeToBeThrownAway = currOpenNode;
                // 						}
                // 						cleanUpSize--;
                // 					}
                // 					SortedSet<double[]> tailSetToBeThrownAway = openTree.tailSet(firstNodeToBeThrownAway);
                // 					tailSetToBeThrownAway.clear();
                // 					jvm.gc();
                // 					logger.info("After clean up -> openTree.size = " + openTree.size() + "; closedTree.size = " + closedTree.size());

                // 					cleanUpTimer.restart();
                // 				}

                iterationCounter++;

                // Records the maximum size of the openTree
                final int currOpenSize = openTree.size();
                if (currOpenSize > maxOpenSize)
                {
                    maxOpenSize = currOpenSize;
                }

                // Selects the first node on OPEN. If it is accepting, the search is completed
                currNode = openTree.first();

                if (isAcceptingNode(currNode))
                {
                    // This line is needed for correct backward search, performed during the schedule construction
                    // IS IT???
                    updateClosedTree(currNode);
                    this.acceptingNode = currNode;
                    break;
                }

                // The first open node is removed
                final boolean succesfullyRemoved =  openTree.remove(currNode);
                if (! succesfullyRemoved)
                {
                    throw new Exception("The node " + printNodeName(currNode) + " was not found on the openTree");
                }

                // If the node is not accepting, it goes to the CLOSED tree if there is not a node there already
                // that represents the same logical state and is better than the current node in all
                // "aspects" (lower cost in all directions). If the current node is promising (if it ends up on CLOSED),
                // its successors are found and put on the OPEN tree.
                branch(currNode);
            }
            else
            {
                return Double.MAX_VALUE;
            }
        }

        if (currNode == null || ! isAcceptingNode(currNode))
        {
            errorMsgs += "open_count = " + openTree.size() + "; max_open_size = " + maxOpenSize;
            throw new RuntimeException("An accepting state could not be found, nr of iterations = " + iterationCounter);
        }

		infoMsgs += "\tA*-iterations (nr of search calls through the closed tree): " + iterationCounter + "\n";
		infoMsgs += "\tIn time: " + timer.elapsedTime() + " ms\n";
		infoMsgs += "\tThe CLOSED tree contains (at the end) " + closedTree.size() + " elements\n";
		infoMsgs += "\tMax{OPEN.size} = " + maxOpenSize + "\n";
		infoMsgs += "\t\t" + "g = " + acceptingNode.getValueAt(ACCUMULATED_COST_INDEX);

        schedulingDone = true;

        return currNode.getValueAt(ACCUMULATED_COST_INDEX);
    }

    /**
     * Updates the closed tree if necessary, i.e. if the currently examined
     * node with better estimate value already is present on that tree.
     * If the node is added to the closed tree, its descendants are put on the
     * open tree, according to the estimated remaining cost value.
     */
    protected void branch(final Node currNode)
    {
        try
        {
            final boolean currNodeIsAddedToClosed = updateClosedTree(currNode);

            if (currNodeIsAddedToClosed)
            {
                // tillf (test)
				// 				Iterator childIter = expander.expandNodeManually(currNode, activeAutomataIndex, true).iterator();
                final Iterator<Node> childIter = expander.expandNode(currNode, activeAutomataIndex).iterator();


				// 				//tillf (test)
				// 				Collection childColl = expander.expandNode(currNode, activeAutomataIndex);
				// 				if (childColl.size() > 0 && searchDepthCounter++ > 30)
				// 				{
				// 					int cleanUpSize = openTree.size()/searchDepthCounter;
				// 					double[] firstNodeToBeThrownAway = null;
				// 					double estimateToBeThrownAway = -1;
				// 					for (Iterator<double[]> it = openTree.iterator(); it.hasNext(); )
				// 					{
				// 						double[] currOpenNode = it.next();
				// 						double currEstimate = currOpenNode[ESTIMATE_INDEX];
				// 						if (currEstimate != estimateToBeThrownAway && cleanUpSize > 0)
				// 						{
				// 							estimateToBeThrownAway = currEstimate;
				// 							firstNodeToBeThrownAway = currOpenNode;
				// 						}
				// 						cleanUpSize--;
				// 					}
				// 					SortedSet<double[]> tailSetToBeThrownAway = openTree.tailSet(firstNodeToBeThrownAway);
				// 					tailSetToBeThrownAway.clear();
				// 					searchDepthCounter = 0;
				// 				}
				// 				Iterator childIter = childColl.iterator();
                while (childIter.hasNext())
                {
                    final Node nextNode = childIter.next();

                    // Calculate the estimate function of the expanded node and store it at the appropriate position
                    nextNode.setValueAt(ESTIMATE_INDEX, calcEstimatedCost(nextNode));

                    openTree.add(nextNode);
                }
            }
        }
        catch (final Exception ex)
        {
			debugMsgs.add(ex.getStackTrace());
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
     *
     * @param node the new node that might be added to the CLOSED tree.
     * @return true, if the new node is added to the CLOSED tree
     *         false, otherwise.
     */
    protected boolean updateClosedTree(final Node node)
    {
        // The nodes corresponding to the same logical state (but different paths from the initial state)
        // as the new node. They are stored as one double[]-variable in the closedTree.
        final Node correspondingClosedNodes = closedTree.remove(new Integer(getKey(node)));

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
            // correspondingClosedNodes-double[]-array).
            final ArrayList<Integer> tieIndices = new ArrayList<Integer>();

            final int nodeLength = node.getBasis().length;
            final int nrOfClosedNodes = correspondingClosedNodes.getBasis().length / nodeLength;

            // Each "internal" node should be compared to the current node
            for (int i=0; i<nrOfClosedNodes; i++)
            {
                boolean newNodeIsAlwaysWorse = true;
                boolean newNodeIsAlwaysBetter = true;

                // The comparison is done for Tv_new[i] + g_new <> Tv_old[i] + g_old (forall i)
                for (int j=CURRENT_COSTS_INDEX; j<ACCUMULATED_COST_INDEX; j++)
                {
                    final double currCostDiff = (node.getValueAt(j) + node.getValueAt(ACCUMULATED_COST_INDEX)) - (correspondingClosedNodes.getValueAt(j + i*nodeLength) + correspondingClosedNodes.getValueAt(ACCUMULATED_COST_INDEX + i*nodeLength));

                    if (currCostDiff < 0)
                    {
                        newNodeIsAlwaysWorse = false;
                    }
                    else if (currCostDiff > 0)
                    {
                        newNodeIsAlwaysBetter = false;
                    }
                }

                // If the new node is worse than any already examined node in every future direction,
                // it is thrown away;
                if (newNodeIsAlwaysWorse)
                {
                    closedTree.put(new Integer(getKey(node)), correspondingClosedNodes);
                    return false;
                }
                // else if the examined node is neither worse nor better, its index is added to the tieIndices
                else if (! newNodeIsAlwaysWorse && ! newNodeIsAlwaysBetter)
                {
                    tieIndices.add(new Integer(i));
                }
            }

            // Only ties (and the new node) are kept for the update of the closedTree.
            final double[] newClosedNodeBasis = new double[(tieIndices.size() + 1)*nodeLength];

            // The tie-nodes (if there are any) are copied to the new closedNode
            for (int i=0; i<tieIndices.size(); i++)
            {
                final int currExaminedNodesIndex = tieIndices.get(i).intValue();
                for (int j=0; j<nodeLength; j++)
                {
                    newClosedNodeBasis[j + i*nodeLength] = correspondingClosedNodes.getValueAt(j + currExaminedNodesIndex*nodeLength);
                }
            }

            // The latest addition to the node-family (double[] node) is also added to the new closedNode
            for (int j=0; j<nodeLength; j++)
            {
                newClosedNodeBasis[j + tieIndices.size()*nodeLength] = node.getValueAt(j);
            }

			correspondingClosedNodes.setBasis(newClosedNodeBasis);

            closedTree.put(new Integer(getKey(node)), correspondingClosedNodes);
        }

        return true;
    }

    /****************************************************************************************/
    /*                                 HEURISTICS                                           */
    /****************************************************************************************/

    //     protected void preprocess2() {
    // 	for (int i=0; i<plantAutomata.size()-1; i++) {
    // 	    for (int j=i+1; j<plantAutomata.size(); j++) {
    // 		int hashtableIndex = calcHashtableIndex(i,j);

    // 		activeAutomataIndex = new int[2];
    // 		activeAutomataIndex[0] = theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(i));
    // 		activeAutomataIndex[1] = theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(j));

    // 		int schedCounter = 0;

    // 		ArrayList activeNodes = new ArrayList();
    // 		activeNodes.add(makeInitialNode());

    // 		while (!activeNodes.isEmpty()) {
    // 		    int[] currNode = (int[])activeNodes.remove(0);

    // 		    if (! (twoProdRelax[hashtableIndex].containsKey(getKey(currNode)))) {
    // 			activeNodes.addAll(expander.expandNode(currNode, activeAutomataIndex));

    // 			schedCounter++;
    // 			int[] accNode = scheduleFrom(resetCosts(currNode));

    // 			if (accNode != null)
    // 			    twoProdRelax[hashtableIndex].put(getKey(currNode), new Integer(accNode[accNode.length-1]));
    // 		    }
    // 		}
    // 	    }
    // 	}
    //     }

    /**
     * 			Calculates the costs for a two-product relaxation (i.e. as if there
     * 			would only be two robots in the cell) and stores it in the hashtable
     * 			twoProdRelax.
     */
    /*	protected String preprocess2() {
      String outputStr = "";
      Automata plantAutomata = theAutomata.getPlantAutomata();

      for (int i=0; i<plantAutomata.size()-1; i++) {
      for (int j=i+1; j<plantAutomata.size(); j++) {
      int hashtableIndex = calcHashtableIndex(i,j);

      int schedCounter = 0;

      int[] currAutomataIndex = new int[]{theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(i)),
      theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(j))};

      ArrayList activeNodes = new ArrayList();

      activeNodes.add(new Node(makeInitialState()));

      while (!activeNodes.isEmpty()) {
      Node currNode = (Node)activeNodes.remove(0);

      if (! (twoProdRelax[hashtableIndex].containsKey(currNode))) {
      activeNodes.addAll(expandNode(currNode, currAutomataIndex));

      schedCounter++;
      currNode.resetCosts();
      Node accNode = scheduleFrom(currNode, currAutomataIndex);

      if (accNode != null)
      twoProdRelax[hashtableIndex].put(currNode, new Integer(accNode.getAccumulatedCost()));
      else {
      String plantNames = "";
      for (int k=0; k<currAutomataIndex.length-1; k++)
      plantNames += theAutomata.getAutomatonAt(currAutomataIndex[k]).getName() + "||";
	  plantNames += theAutomata.getAutomatonAt(currAutomataIndex[currAutomataIndex.length-1]).getName();

	  logger.warn("?r " + currNode.toStringLight() + " l?st s? in i helvete f?r " + plantNames + "???");
	  }

	  }
	  }

	  String plantNames = "";
	  for (int k=0; k<currAutomataIndex.length-1; k++)
	  plantNames += theAutomata.getAutomatonAt(currAutomataIndex[k]).getName() + "||";
	  plantNames += theAutomata.getAutomatonAt(currAutomataIndex[currAutomataIndex.length-1]).getName();

	  outputStr += "\t\t" + plantNames + ": " + schedCounter + " nodes relaxed\n";
	  }
	  }

	  return outputStr;
	  }
	*/

    //     int getTwoProdRelaxation(int[] node) {
    // 	int estimate = 0;
    // 	int[] currentCosts = expander.getCosts(node);
    // 	int plantAutomataSize = plantAutomata.size();
    // 	activeAutomataIndex = new int[2];

    // 	for (int i=0; i<plantAutomataSize-1; i++) {
    // 	    for (int j=i+1; j<plantAutomataSize; j++) {
    // 		activeAutomataIndex[0] = i;
    // 		activeAutomataIndex[1] = j;

    // 		// Funkar bara om alla noder i den totala synkningen har 2-prod-relaxerats
    // 		// Har de det?????????????
    // 		Object relaxation = twoProdRelax[calcHashtableIndex(i,j)].get(getKey(node));
    // 		if (relaxation != null) {
    // 		    int altEstimate = ((Integer) relaxation).intValue();

    // 		    if (altEstimate > estimate)
    // 			estimate = altEstimate;
    // 		}
    // 	    }
    // 	}

    // 	int minCurrCost = currentCosts[0];
    // 	for (int i=1; i<currentCosts.length; i++) {
    // 	    if (currentCosts[i] < minCurrCost)
    // 		minCurrCost = currentCosts[i];
    // 	}

    // 	return estimate + minCurrCost;
    //     }

    /**
     * Calculates the total estimated cost (makespan) for the node, i.e.
     * the sum of the accumulated cost and the estimate of the remaining
     * cost (f(n) = g(n) + h(n)) is returned. Note that the getRelaxation(node)-
     * method must be defined in the classes, that inherit from this one for
     * correct functionning.
     *
     * @param node the node, whose estimated cost we want to know
     * @return totalEstimatedCost : f(n) = g(n) + h(n)
     */
    public double calcEstimatedCost(final Node node)
        throws Exception
    {
//         double[] parent = getParent(node);

        final double fNode = node.getValueAt(ACCUMULATED_COST_INDEX) + getRelaxation(node);
        //   		double fParent = parent[ESTIMATE_INDEX];

        //  		// The following code inside the if-loop is needed to ensure consistency of the heuristic
        //  		if (fParent > fNode)
        // 		{
        // 			//logger.warn("Consistency ensurance");
        //  			return fParent;
        // 		}

        // 		logger.info("node = " + printArray(node) + "; estimation = " + fNode);

        if (approximationWeights == null)
        {
            return fNode;
        }
        else
        {
            double depth = 0;
            for (int i=0; i < getActiveLength(); i++)
            {
                depth += Math.pow(node.getBasis()[getActiveAutomataIndex()[i]], 2);
            }
            depth = Math.sqrt(depth);

            return fNode * ( 1 + approximationWeights[X_WEIGHT_INDEX] / (approximationWeights[Y_WEIGHT_INDEX] + depth));
        }
    }

    /*
     * This method returns an updated cost vector, containing updated current costs,
     * accumulated cost and estimated cost.
     */
    public double[] updateCosts(final double[] costs, final int changedIndex, final double newCost)
    {
        final double[] newCosts = new double[costs.length];

        for (int i=0; i<costs.length-2; i++)
        {
            if (i == changedIndex)
            {
                newCosts[i] = newCost;
            }
            else
            {
                newCosts[i] = costs[i] - costs[changedIndex];
                if (newCosts[i] < 0)
                {
                    newCosts[i] = 0;
                }
            }
        }

        // The accumulated cost update
        newCosts[newCosts.length-2] = costs[costs.length-2] + costs[changedIndex];

        // The f-update
        newCosts[newCosts.length-1] = costs[costs.length-1];

        return newCosts;
    }


    /****************************************************************************************/
    /*                      METHODS FOR BUILDING THE SCHEDULE AUTOMATON                     */
    /****************************************************************************************/


    /**
     * Starting from an accepting state/node, this method walks its way back,
     * using keys stored in PARENT_INDEX to find parents. When this is done,
     * an event, connecting the two nodes is found and added to the schedule,
     * while the parent becomes the next node in search of its parent. This
     * is done until an initial node is found, which completes the construction.
     */
    @Override
    public void buildScheduleAutomaton()
		throws Exception
    {
        // Create the automaton with a working name "Schedule"
        scheduleAuto = new Automaton("Schedule");
        scheduleAuto.setType(AutomatonType.SUPERVISOR);

        // Start the clock
        timer.restart();

        State nextState = makeStateFromNode(acceptingNode, scheduleAuto, -1);
        nextState.setAccepting(true);
        nextState.setCost(0); // The cost of the accepting state is 0
        scheduleAuto.addState(nextState);

        // We need to keep track of the accepting state for the connection between
        // the initial- and marked states (for schedule repetition)
        final State acceptingState = nextState;

        Node currNode = acceptingNode;

        while (hasParent(currNode))
        {
            try
            {
                if (isRunning)
                {
                    final Node parent = getParent(currNode);

                    final State currState = makeStateFromNode(parent, scheduleAuto,
                            currNode.getValueAt(ACCUMULATED_COST_INDEX));

                    currState.setCost(currNode.getValueAt(ACCUMULATED_COST_INDEX) -
                            parent.getValueAt(ACCUMULATED_COST_INDEX));

                    final LabeledEvent event = findConnectingEvent(parent, currNode);

                    if (!hasParent(parent))
                    {
                        currState.setInitial(true);
                    }

                    scheduleAuto.addState(currState);
                    scheduleAuto.getAlphabet().addEvent(event);
                    scheduleAuto.addArc(new Arc(currState, nextState, event));

                    currNode = parent;
                    nextState = currState;
                }
                else
                {
                    return;
                }
            }
            catch (final Exception ex)
            {
                errorMsgs += "ModifiedAstar::buildScheduleAutomaton() --> Could not find the arc between " +
					printArray(currNode.getBasis()) + " and its parent" + printNodeName(getParent(currNode));
                debugMsgs.add(ex.getStackTrace());

                throw ex;
            }
        }

        // If a dummy event has been added to all robot alphabets,
        // it is also added to the schedule, thus making it return from its
        // accepting to its initial state.
        boolean resetSchedule = true;
        for (final Iterator<Automaton> plantAutIt = plantAutomata.iterator(); plantAutIt.hasNext(); )
        {
            final Automaton plant = plantAutIt.next();
            if (!plant.getAlphabet().contains(dummyEventName))
            {
                resetSchedule = false;
                break;
            }
        }
        if (resetSchedule)
        {
            final LabeledEvent resetEvent =  new LabeledEvent(dummyEventName);
            scheduleAuto.getAlphabet().addEvent(resetEvent);

            for (final Iterator<State> stateIt = scheduleAuto.stateIterator(); stateIt.hasNext(); )
            {
                final State state = stateIt.next();
                if (state.isAccepting())
                {
                    scheduleAuto.addArc(new Arc(state, scheduleAuto.getInitialState(), resetEvent));
                }
            }
        }

        //Connect the final state to the initial in order to run the schedule repeatedly
        for (final Iterator<Arc> arcIt = acceptingState.incomingArcsIterator(); arcIt.hasNext();)
        {
            final Arc inArc = arcIt.next();
            scheduleAuto.addArc(new Arc(inArc.getFromState(), scheduleAuto.getInitialState(), inArc.getEvent()));
        }
        scheduleAuto.getInitialState().setAccepting(true);
        scheduleAuto.getInitialState().setName(scheduleAuto.getInitialState().getName() +
                "; makespan = " + acceptingNode.getValueAt(ACCUMULATED_COST_INDEX));
        scheduleAuto.removeState(acceptingState);

        infoMsgs += "Schedule was built in " + timer.elapsedTime() + "ms";
    }

	/**
	 * Creates a state, making sure that a unique name is given to the new state.
	 */
	protected State makeStateFromNode(final Node node, final Automaton auto, final double firingTime)
	{
		String timeStr = "; firing_time = " + firingTime;
		if (firingTime == -1)
		{
			timeStr = "; cycle_time = " + node.getValueAt(ACCUMULATED_COST_INDEX);
		}
		String stateName = printNodeName(node) + timeStr;

		while (auto.containsStateWithName(stateName))
		{
			stateName += SEPARATOR;	// had "." here, but possibly got broke bvy update of compiler
								// *hopefully* this does not break stuff later (see createXORConstraints() below)
		}

		return new State(stateName);
	}

    /**
     * This method finds several parent candidates to the current node by retrieving
	 * an element from the closedTree that corresponds to the key, stored in the current
	 * nodes PARENT_INDEX. Caution should be made, since the closed element might consist
	 * of several nodes. The true parent is found by comparing the costs for all the nodes
	 * stored in the parent element.
     *
     * @param node - the node whose parent is seeked
     * @return parentNode - the parent of the node 'node'
     */
    protected Node getParent(final Node node)
		throws Exception
    {
        // one object of the closedTree may contain several nodes (that all correspond
        // to the same logical state but different paths).
        final Node parentCandidates = closedTree.get(new Integer((int)node.getValueAt(PARENT_INDEX)));
        if (parentCandidates == null)
        {
            return null;
        }
        final double[] parentCandidatesBasis = parentCandidates.getBasis();

        final int nrOfCandidates = parentCandidatesBasis.length / node.getBasis().length;

        if (nrOfCandidates == 1)
        {
            return parentCandidates;
        }
        // which candidate is the true parent...
        else
        {
            // which plant (robot) fired the transition...
            int activePlantIndex = -1;

            // which cost is the new cost of the firing state...
            double newStateCost = -1;

            // activePlantIndex is found as the index that corresponds to a plant automaton
            // (in the plantAutomata-collection) and differs between this node and its parent
            for (int j=0; j<activeAutomataIndex.length; j++)
            {
                final int currIndex = activeAutomataIndex[j];

                // This might look strange, but it is not. All parent candidates corresponds to
                // the same logical state. In other words, it is enough to do the state indices check
                // for some of the parent candidates, why not the first.
                if (node.getValueAt(currIndex) != parentCandidates.getValueAt(currIndex))
                {
                    activePlantIndex = j;

                    // The new cost (after update) is equal to the cost of the state that is changed
                    // during the transition, i.e. the state corresponding to the firing robot.
                    newStateCost = indexMap.getStateAt(indexMap.getAutomatonAt(currIndex), (int)node.getValueAt(currIndex)).getCost();

                    break;
                }
            }

            final double[] parentCosts = new double[ESTIMATE_INDEX - CURRENT_COSTS_INDEX + 1];

            // The only thing that differs between the candidates is their cost-vectors
            // So, find the parent that gives correct cost update for the current state
            // and return it. Return null if nothing appropriate is found.
            for (int i=0; i<nrOfCandidates; i++)
            {
                // Retrieving the costs of the current parent candidate
                for (int j=0; j<parentCosts.length; j++)
                {
                    parentCosts[j] = parentCandidates.getValueAt(j + CURRENT_COSTS_INDEX + i*node.getBasis().length);
                }

                final double[] newCosts = updateCosts(parentCosts, activePlantIndex, newStateCost);

                // If the cost update (that corresponds to the transition from parent candidate to the current node
                // is equal to the costs of the current node, then the true parent is found.
                boolean isParent = true;
                //for (int j=0; j<newCosts.length; j++)
                // The estimate cost is not important here
                for (int j=0; j<newCosts.length - 1; j++)
                {
                    if (newCosts[j] != node.getValueAt(j + CURRENT_COSTS_INDEX))
                    {
                        isParent = false;
                        break;
                    }
                }

                // If the parent was found, return it.
                if (isParent)
                {
                    final double[] parentBasis = new double[node.getBasis().length];

                    for (int j=0; j<parentBasis.length; j++)
					{
                        parentBasis[j] = parentCandidates.getValueAt(j + i*node.getBasis().length);
                    }

					final Node parent = node.emptyClone();
					parent.setBasis(parentBasis);
                    return parent;
                }
            }

            return null;
        }
    }

    /**
     * Checks if the current node contains a reference to its parent.
     * The reference should be stored in the PARENT_INDEX. If its value
     * is -1, the current node has no recognized parent.
     */
    protected boolean hasParent(final Node node)
    {
        if (node.getValueAt(PARENT_INDEX) == ROOT_VALUE)
		{
            return false;
        }

        return true;
    }

    /**
     * Finds an event between two nodes. In order to do this, the automaton that is
     * responsible for the transition, i.e. the plant automaton, whose indices differ
     * between the two nodes, is found.
     *
     * @param fromNode the "from" end of the seeked transition
     * @param toNode the "to" end of the seeked transition
     * @return connectingEvent - the event between "fromNode" and "toNode"
     */
    protected LabeledEvent findConnectingEvent(final Node fromNode, final Node toNode)
		throws Exception
    {
        for (int i=0; i<theAutomata.size(); i++)
        {
            if (fromNode.getValueAt(i) != toNode.getValueAt(i))
            {
                final Automaton auto = indexMap.getAutomatonAt(i);
                return auto.getLabeledEvent(indexMap.getStateAt(auto, (int)fromNode.getValueAt(i)), indexMap.getStateAt(auto, (int)toNode.getValueAt(i)));
            }
        }

        return null;
    }

    /**
     * Finds an arc between two nodes. In order to do this, the automaton that is
     * responsible for the transition, i.e. the plant automaton, whose indices differ
     * between the two nodes, is found.
     *
     * @param fromNode the "from" end of the seeked transition
     * @param toNode the "to" end of the seeked transition
     * @return connectingEvent - the event between "fromNode" and "toNode"
     */
    protected Arc findConnectingArc(final Node fromNode, final Node toNode)
		throws Exception
    {
        for (int i=0; i<theAutomata.size(); i++)
        {
            if (fromNode.getValueAt(i) != toNode.getValueAt(i))
            {
                final Automaton auto = indexMap.getAutomatonAt(i);
                final State fromState = indexMap.getStateAt(auto, (int)fromNode.getValueAt(i));
                final State toState = indexMap.getStateAt(auto, (int)toNode.getValueAt(i));

                for (final Iterator<Arc> arcsIt = fromState.outgoingArcsIterator(); arcsIt.hasNext(); )
                {
                    final Arc currArc = arcsIt.next();

                    if (currArc.getToState().equals(toState))
                    {
                        return currArc;
                    }
                }
            }
        }

        return null;
    }

    /**
     * This method create an instance of VelocityBalancer, supplying the selected
     * automata (theAutomata) and the newly built schedule, if it exists.
     * VelocityBalancer balances then the velocities of the selected plants.
     */
    protected void balanceVelocities()
        throws Exception
    {
        if (scheduleAuto == null)
        {
            addToMessages("Velocities were not balanced since the schedule could not be found.",
                    SchedulingConstants.MESSAGE_TYPE_ERROR);
            return;
        }

        final Automata autosToBeBalanced = theAutomata.clone();
        autosToBeBalanced.addAutomaton(scheduleAuto);
        new VelocityBalancer(autosToBeBalanced, this);
    }

// This should be done by adding a specification forcing the plants to execute once, I think. /AK
//     private void addDummyAcceptingState(Automaton currPlant)
//     {
//         State currInitialState = currPlant.getInitialState();

//         if (currInitialState.isAccepting())
//         {
// // 			// Record that this plant has been altered
// // 			alteredAutomata.addAutomaton(new Automaton(currPlant));
// // 			logger.info("adding " + currPlant.getName() + " to altered");

//             currInitialState.setAccepting(false);

//             State dummyState = new State("dummy_" + currInitialState.getName());
//             currPlant.addState(dummyState);

//             for (Iterator<Arc> incomingArcIt = currInitialState.incomingArcsIterator(); incomingArcIt.hasNext(); )
//             {
//                 Arc currArc = incomingArcIt.next();

//                 currPlant.addArc(new Arc(currArc.getFromState(), dummyState, currArc.getEvent()));
//             }

//             currInitialStat  e.removeIncomingArcs();

//             LabeledEvent dummyEvent = new LabeledEvent(dummyEventName);
//             currPlant.getAlphabet().addEvent(dummyEvent);
//             currPlant.addArc(new Arc(dummyState, currInitialState, dummyEvent));

//             dummyState.setAccepting(true);
//             dummyState.setCost(0);

//             currPlant.remapStateIndices();
//         }
//     }

    /****************************************************************************************/
    /*                                 AUXILIARY METHODS                                    */
    /****************************************************************************************/

    /**
     * Calculates the relaxation value for the node, i.e. the estimate of the remaining
     * cost, h(n), according to the employed heuristic
     *
     * @param node the node, whose estimated cost we want to know
     * @return remainingEstimatedCost : h(n)
     */
    protected double getRelaxation(final Node node)
		throws Exception
    {
        return relaxer.getRelaxation(node);
    }

    public Relaxer getRelaxer()
    {
        return relaxer;
    }

    public String printArray(final int[] node)
    {
        String s = "[";

        for (int i=0; i<node.length-1; i++)
        {
            s += node[i] + " ";
        }
        s += node[node.length-1] + "]";

        return s;
    }

    public String printArray(final double[] array)
    {
        String s = "[";

        for (int i=0; i<array.length-1; i++)
        {
            s += array[i] + " ";
        }
        s += array[array.length-1] + "]";

        return s;
    }

    public String printNodeSignature(final Node node)
    {

        String s = printNodeName(node) + "; Tv = [";

        // int addIndex = node.getBasis().length - (plantAutomata.size() + 1);
        for (int i=0; i<getActiveLength()-1; i++)
		{
            s += node.getValueAt(i + CURRENT_COSTS_INDEX) +  " ";
		}
        s += node.getValueAt(ACCUMULATED_COST_INDEX-1) + "]; g = ";

        s += node.getValueAt(ACCUMULATED_COST_INDEX);
        s += "; f = " + node.getValueAt(ESTIMATE_INDEX);

        return s;
    }

    public String printNodeName(final Node node)
    {
        String s = "[";

        for (int i=0; i<theAutomata.size(); i++)
        {
			s += indexMap.getStateAt(indexMap.getAutomatonAt(i), (int)node.getValueAt(i)) + " ";
        }
		s = s.trim() + "]";

        return s;
    }

    /**
     * Returns true if all the states that this node represents are accepting.
     *
     * @param node the current node
     * @return true if all the corresponing states are accepting
     */
    protected boolean isAcceptingNode(final Node node)
    {
		// The active automata, i.e. the automata that are currently being scheduled
        // should be in their marked states. The notion of active automata is important
        // when a subset of original plant automata is used, e.g. in relaxations.
        // However, I suspect that there is a nicer way to implement this. /AK
        for (int i=0; i<activeAutomataIndex.length; i++)
        {
            final int index = activeAutomataIndex[i];
			final State currState = indexMap.getStateAt(indexMap.getAutomatonAt(index), (int)node.getValueAt(index));

            if (!currState.isAccepting())
            {
                return false;
            }
        }

        // All the specifications should be in their marked states.
        for (int i=0; i<theAutomata.size(); i++)
        {
            if (indexMap.getAutomatonAt(i).isSpecification())
            {
                if (!indexMap.getStateAt(indexMap.getAutomatonAt(i), (int)node.getValueAt(i)).isAccepting())
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Calculates a key that is used to order the nodes in the closedTree.
     * Every logical state maps uniquely to a key. Note though that the nodes
     * corresponding to the same state get identical keys.
     *
     * @param node the current node
     * @return the key (int) for the node ordering in the closedTree
     */
    public int getKey(final Node node)
    {
        int key = 0;

//         for (int i=0; i<activeAutomataIndex.length; i++)
//         {
//             key += ((int)node.getValueAt(activeAutomataIndex[i]))*keyMapping[activeAutomataIndex[i]];
//         }
        for (final Iterator<Automaton> autIt = theAutomata.iterator(); autIt.hasNext(); )
        {
			final int automatonIndex = indexMap.getAutomatonIndex(autIt.next());
            key += ((int)node.getValueAt(automatonIndex))*keyMapping[automatonIndex];
        }

        return key;
    }

    /**
     * Calculates a key that is used to order the nodes in the closedTree.
     * Every logical state maps uniquely to a key. Note though that the nodes
     * corresponding to the same state get identical keys.
     *
     * @param node the current node
     * @return the key (int) for the node ordering in the closedTree
     */
    public int getKey(final int[] node)
    {
        int key = 0;

        for (int i=0; i<activeAutomataIndex.length; i++)
        {
            key += node[activeAutomataIndex[i]]*keyMapping[activeAutomataIndex[i]];
        }

        return key;
    }

	public int[] getKeyMapping()
	{
		return keyMapping;
	}

    protected double[] resetCosts(final double[] node)
    {
        for (int i = 2*theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA; i<node.length; i++)
        {
            node[i] = 0;
        }

        return node;
    }

    /**
     * 	Calculates an index to Hashtable[] twoProdRelax.
     * @return index of the 2-prod-relax-hashtable corresponing to robot_i and robot_j.
     */
    protected int calcHashtableIndex(final int i, final int j)
    {
        final int size = theAutomata.getPlantAutomata().size();

        return (int) (i*(size - 1.5) - 0.5*(i*i) - 1 + j);
    }

    public int getActiveLength()
    {
        return activeAutomataIndex.length;
    }

    public int[] getActiveAutomataIndex()
    {
        return activeAutomataIndex;
    }

    public void setActiveAutomataIndex(final int[] newActiveAutomataIndex)
    {
        activeAutomataIndex = new int[newActiveAutomataIndex.length];
        for (int i=0; i<activeAutomataIndex.length; i++)
        {
            activeAutomataIndex[i] = newActiveAutomataIndex[i];
        }
    }

    public boolean schedulingDone()
    {
        return schedulingDone;
    }

    public boolean isInitialized()
    {
        return isInitialized;
    }

    public int getMakespan()
    {
        return makespan;
    }

//@Deprecated
//    public String getOutputString()
//    {
//        return outputStr;
//    }
//

// 	public int getMaxOpenSize()
// 	{
// 		return maxOpenSize;
// 	}

	public Automata getAllAutomata()
	{
		return theAutomata;
	}

	public Automata getPlantAutomata()
	{
		return plantAutomata;
	}

	public boolean isManualExpansion()
	{
		return manualExpansion;
	}

	public boolean getBuildSchedule()
	{
		return buildSchedule;
	}

	public String getHeuristic()
	{
		return heuristic;
	}

	public NodeExpander getNodeExpander()
	{
		return expander;
	}

	public AutomataIndexMap getIndexMap()
	{
		return indexMap;
	}

	public void sleep (final long ms)
		throws InterruptedException
	{
		Thread.sleep(ms);
	}

	@Override
  public Automaton getSchedule()
	{
            return scheduleAuto;
	}

    @Override
    public String getMessages(final int msgType)
    {
        switch (msgType)
        {
            case(SchedulingConstants.MESSAGE_TYPE_INFO):
                return infoMsgs;
            case(SchedulingConstants.MESSAGE_TYPE_WARN):
                return warnMsgs;
            case(SchedulingConstants.MESSAGE_TYPE_ERROR):
                return errorMsgs;
            default:
                errorMsgs += "Wrong message type supplied to Milp.getMessages()";
                return null;
        }
    }

	@Override
  public Object[] getDebugMessages()
	{
		return debugMsgs.toArray();
	}

    @Override
    public void addToMessages(final String additionStr, final int messageType)
    {
        switch (messageType)
        {
            case SchedulingConstants.MESSAGE_TYPE_INFO:
                infoMsgs += additionStr;
                break;
            case SchedulingConstants.MESSAGE_TYPE_WARN:
                warnMsgs += additionStr;
                break;
            case SchedulingConstants.MESSAGE_TYPE_ERROR:
                errorMsgs += additionStr;
                break;
            default:
                warnMsgs += "Message type incorrect when adding \"" + additionStr + "\" to the messages";
                break;
        }
    }
}

