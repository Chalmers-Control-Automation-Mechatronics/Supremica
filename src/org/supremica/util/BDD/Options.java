package org.supremica.util.BDD;

import java.io.*;

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

    /** search algorithm */
    public static final int
	ALGO_MONOLITHIC = 0,
	ALGO_CONJUNCTIVE = 1,
	ALGO_CONJUNCTIVE_LOCAL_EVENT = 2,
	ALGO_DISJUNCTIVE = 3,
	ALGO_DISJUNCTIVE_WORKSET = 4,
	ALGO_SMOOTHED_MONO = 5,
	ALGO_SMOOTHED_MONO_WORKSET = 6,
	ALGO_SMOOTHED_PATH = 7,
	ALGO_SMOOTHED_KEEP = 8,
	ALGO_SMOOTHED_PART = 9,
	ALGO_PETRINET = 10
	;

	/** language controllability/inclusion algorithms */
	public static final int
		INCLUSION_ALGO_MONOLITHIC = 0,
		INCLUSION_ALGO_MODULAR = 1,
		INCLUSION_ALGO_INCREMENTAL = 2
		;


    /** state counting algorithm */
    public static final int
	COUNT_NONE = 0,
	COUNT_TREE = 1,
	COUNT_EXACT = 2;


    /** Automaton ordering algorithm */
    public static final int
	ORDERING_ALGO_OLD_PCG = 0,
	ORDERING_ALGO_RANDOM  = 1,
	ORDERING_ALGO_NEW_TSP = 2;


	/** Automaton selection heuristics */
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
		AS_HEURISTIC_HYBRID = 9
		;



	/** Event (Transition) selection heuristics, see petrinet related stuff */
	public static final String [] ES_HEURISTIC_NAMES =  {
		"Random","Max pending req.","Min pending req.","Max follow","Min follow",
		"Largest cover", "Smallest cover"
	};
	public static final int
		ES_HEURISTIC_RANDOM = 0,
		ES_HEURISTIC_MOST_PENDING = 1,
		ES_HEURISTIC_LEAST_PENDING = 2,
		ES_HEURISTIC_MOST_FOLLOWERS = 3,
		ES_HEURISTIC_LEAST_FOLLOWERS = 4,
		ES_HEURISTIC_MOST_MEMBERS = 5,
		ES_HEURISTIC_LEAST_MEMBERS = 6
	;


	/** BDD grow graf */
	public static final int
		SHOW_GROW_NONE = 0,

		SHOW_GROW_NODES = 1,
		SHOW_GROW_NODES_LOG = 2,
		SHOW_GROW_NODES_DIFF = 3,

		SHOW_GROW_SATCOUNT = 4,
		SHOW_GROW_SATCOUNT_LOG = 5,
		SHOW_GROW_SATCOUNT_DIFF = 6,

		SHOW_GROW_NODES_AND_SATCOUNT_LOG = 7;

    // constants
    public static final int LINE_WIDTH = 55;    // # of chars per line?, screen width
    private static final int DEFAULT_MAX_PARTITION_SIZE = 3000; // max nodes/partition
	public static int max_partition_size = DEFAULT_MAX_PARTITION_SIZE;


    // options
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

	public static int show_grow = SHOW_GROW_NONE; /** type of the BDD graph shown by GrowFrame */


    // algorithms
    public static int inclsuion_algorithm = INCLUSION_ALGO_INCREMENTAL;	/** C/LI algorithm */
    public static int algo_family = ALGO_DISJUNCTIVE_WORKSET; /** reachability algorithm */
    public static int count_algo  = COUNT_TREE; /** state counting, nothing important ... */

    // heurisitcs
	public static int as_heuristics  = AS_HEURISTIC_HYBRID;
	public static int es_heuristics  = ES_HEURISTIC_LEAST_FOLLOWERS;
	public static int ordering_algorithm = ORDERING_ALGO_NEW_TSP;


    // out own out stream, might be changed to point to a file
    public static PrintStream out = System.out;

}
