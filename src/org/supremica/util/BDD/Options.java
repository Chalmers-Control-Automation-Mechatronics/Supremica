package org.supremica.util.BDD;

import java.io.*;
import org.supremica.properties.SupremicaProperties;

public class Options
{

	/**
	 * The number of automata in a group or total before we switch from the
	 * monolithic supervisor -- see SupervisorFactory.createNonDisjSupervisor()
	 */
	public static final int
		MAX_MONOLITHIC_GROUP_SIZE = 4,
		MAX_MONOLITHIC_TOTAL_SIZE = 6
		;


	// current directory is always a good start
	public static String extraLibPath = ".";



	/** Variable re-ordering method. NOTE: must use the same order as the constants in JBDD !!*/
	public static final String [] REORDER_ALGO_NAMES =  {
		"None",  "Win2", "Win3", "Sift", "Random"
	};
	/** check if dynamic variable ordering is enabled anywhere */
	public static final boolean reorderEnabled() {
		return (reorder_algo != JBDD.REORDER_NONE) && (reorder_dyanmic || reorder_after_build);
	}
	public static int reorder_algo = JBDD.REORDER_SIFT;
	public static boolean reorder_dyanmic = false;  /** enable on the fly reordering */
	public static boolean reorder_after_build = false; /** reorder once after the automata is built */
	public static boolean reorder_with_groups = false; /** set each automata to a variable group */
	public static boolean reorder_within_group = false; /** allow ordering inside the group ?  */





	/** REACHABILITY search algorithms */
	public static final String [] REACH_ALGO_NAMES =  {
		"monotonic", "Conjunctive", "LatticeWalk", "Disjunctive",
		"Smoothed: Workset",  "Smoothed: Monotonic", "Smoothed: Delayed Monotonic",
		"Smoothed: Delayed* Monotonic","Smoothed: Monotonic/Workset",
		"Smoothed: Path (V1)", "Smoothed: Keep (V2)", "Smoothed: Partitioned (P1)",
		"PetriNet-style"
	};
    /** constants for the above*/
    public static final int
	ALGO_MONOLITHIC = 0,
	ALGO_CONJUNCTIVE = 1,
	ALGO_CONJUNCTIVE_LOCAL_EVENT = 2,
	ALGO_DISJUNCTIVE = 3,
	ALGO_DISJUNCTIVE_WORKSET = 4,
	ALGO_SMOOTHED_MONO = 5,
	ALGO_SMOOTHED_DELAYED_MONO = 6,
	ALGO_SMOOTHED_DELAYED_STAR_MONO = 7,
	ALGO_SMOOTHED_MONO_WORKSET = 8,
	ALGO_SMOOTHED_PATH = 9,
	ALGO_SMOOTHED_KEEP = 10,
	ALGO_SMOOTHED_PART = 11,
	ALGO_PETRINET = 12;
	public static int algo_family = ALGO_DISJUNCTIVE_WORKSET; /** reachability algorithm */



	/** language controllability/inclusion algorithms */
	public static final String [] INCLUSION_ALGORITHM_NAMES = {
		"Monolithic      ","Modular","Incremental"};
	public static final int
		INCLUSION_ALGO_MONOLITHIC = 0,
		INCLUSION_ALGO_MODULAR = 1,
		INCLUSION_ALGO_INCREMENTAL = 2;
    public static int inclsuion_algorithm = INCLUSION_ALGO_INCREMENTAL;	/** C/LI algorithm */



    /** state counting algorithm */
    public static final String [] COUNT_ALGO_NAMES = {
		"No counting         ","Tree SAT", "Exact (slow!)" };
    public static final int
		COUNT_NONE = 0,
		COUNT_TREE = 1,
		COUNT_EXACT = 2;
	public static int count_algo  = COUNT_TREE; /** state counting, nothing important ... */



    /** Automaton ordering algorithm */
    public static final String [] ORDERING_ALGORITHM_NAMES = {
		"Random (!)", "PCG search",  "modified TSP", "Topological sort (DFS)    " };
    public static final int
    	AO_HEURISTIC_RANDOM = 0,
    	AS_HEURISTIC_PCG = 1,
    	AS_HEURISTIC_TSP = 2,
    	AS_HEURISTIC_DFS = 3;
	public static int ordering_algorithm = AS_HEURISTIC_TSP;


	/** Automaton selection heuristics  */
	public static final String [] AS_HEURISTIC_NAMES =  {
		"Random", "BDD/Stack", "BDD/FIFO", "BDD/distance",
		"Most common uc-events",	"Most common uc-arcs",
		"Most common events",	"Most common arcs",
		"Most local events",	"Hybrid"
	};
	public static final int
		AS_HEURISTIC_RANDOM = 0,
		AS_HEURISTIC_STACK = 1,
		AS_HEURISTIC_FIFO = 2,
		AS_HEURISTIC_DISTANCE = 3,
		AS_HEURISTIC_MOST_COMMON_UC_EVENTS = 4,
		AS_HEURISTIC_MOST_COMMON_UC_ARCS = 5,
		AS_HEURISTIC_MOST_COMMON_EVENTS = 6,
		AS_HEURISTIC_MOST_COMMON_ARCS = 7,
		AS_HEURISTIC_MOST_LOCAL = 8,
		AS_HEURISTIC_HYBRID = 9;
	public static int as_heuristics  = AS_HEURISTIC_HYBRID;


	/** The choice between R and frontier set in image computation */
	public static final String [] FRONTIER_STRATEGY_NAMES = {
			"Random R/front  ", "R", "front", "front \\ R", "min[front, R]"
			};

	public static final int
		FRONTIER_STRATEGY_RANDOM = 0,
		FRONTIER_STRATEGY_R = 1,
		FRONTIER_STRATEGY_FRONT = 2,
		FRONTIER_STRATEGY_FRONT_MINUS_R = 3,
		FRONTIER_STRATEGY_MIN = 4
		// FRONTIER_STRATEGY_RESTRICT = 5,
		// FRONTIER_STRATEGY_CONSTRAIN = 6,
		;
	// public static int frontier_strategy  = FRONTIER_STRATEGY_R;
	public static int frontier_strategy  = FRONTIER_STRATEGY_FRONT_MINUS_R;



	/** Event (Transition) selection heuristics, see petrinet related stuff */
	public static final String [] ES_HEURISTIC_NAMES =  {
		"Random", "Top-down", "Max pending req.","Min pending req.","Max follow","Min follow",
		"Largest cover", "Smallest cover"
	};
	public static final int
		ES_HEURISTIC_RANDOM = 0,
		ES_HEURISTIC_TOPDOWN = 1,
		ES_HEURISTIC_MOST_PENDING = 2,
		ES_HEURISTIC_LEAST_PENDING = 3,
		ES_HEURISTIC_MOST_FOLLOWERS = 4,
		ES_HEURISTIC_LEAST_FOLLOWERS = 5,
		ES_HEURISTIC_MOST_MEMBERS = 6,
		ES_HEURISTIC_LEAST_MEMBERS = 7;
	public static int es_heuristics  = ES_HEURISTIC_LEAST_FOLLOWERS;


	/** BDD grow graph */
	public static final String SHOW_GROW_NAMES [] = {
		"None", "Node count","logNode (log scale)   ","Node delta",
		"SAT count","logSAT",
		"SAT delta", "Nodes & logSAT"
		};
	public static final int
		SHOW_GROW_NONE = 0,
		SHOW_GROW_NODES = 1,
		SHOW_GROW_NODES_LOG = 2,
		SHOW_GROW_NODES_DIFF = 3,
		SHOW_GROW_SATCOUNT = 4,
		SHOW_GROW_SATCOUNT_LOG = 5,
		SHOW_GROW_SATCOUNT_DIFF = 6,
		SHOW_GROW_NODES_AND_SATCOUNT_LOG = 7;
	public static int show_grow = SHOW_GROW_NONE; /** type of the BDD graph shown by GrowFrame */


	/** insertation heuristic for Delayed* smoothing algorithm */
	public static final String [] DSSI_HEURISTIC_NAMES =  {
		"Random", "Stack", "FIFO", "Smallest BDD", "Largest BDD"
	};
	public static final int
		DSSI_RANDOM = 0,
		DSSI_STACK = 1,
		DSSI_FIFO = 2,
		DSSI_SMALLEST_BDD = 3,
		DSSI_LARGEST_BDD = 4;
	public static int dssi_heuristics = DSSI_STACK;




    // --- [ constants ] -------------------------------------------------
    public static final int LINE_WIDTH = 55;    // # of chars per line?, screen width
    private static final int DEFAULT_MAX_PARTITION_SIZE = 3000; // max nodes/partition
	public static int max_partition_size = DEFAULT_MAX_PARTITION_SIZE;


    // --- [ options ] -------------------------------------------------
	public static boolean developer_mode = true;
    public static final boolean use_cudd = false;
    public static boolean fill_statevars = false;
    public static boolean profile_on = false; // misc profiling stuff
    public static boolean debug_on = false; // proof generating and general debug
    public static boolean size_watch = false; // BDD sizewatch
    public static boolean sanity_check_on = false;
    public static boolean user_alters_PCG = false;
    public static boolean show_encoding = false; // dump variable encoding, not workin very good :(
    public static boolean trace_on = false;
    public static boolean local_saturation = false;
    public static boolean uc_optimistic = true;
    public static boolean nb_optimistic = true;



    // out own out stream, might be changed to point to a file
    public static PrintStream out = System.out;

}
