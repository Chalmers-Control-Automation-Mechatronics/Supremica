package org.supremica.util.BDD;

import java.io.*;

public class Options
{
    /** search algorithm */
    public static final int
	ALGO_MONOLITHIC = 0,
	ALGO_CONJUNCTIVE = 1,
	ALGO_DISJUNCTIVE = 2,
	ALGO_DISJUNCTIVE_WORKSET = 3,

	ALGO_SMOOTHED_MONO = 4,
	ALGO_SMOOTHED_MONO_WORKSET = 5,
	ALGO_SMOOTHED_PATH = 6,
	ALGO_SMOOTHED_KEEP = 7,
	ALGO_SMOOTHED_PART = 8
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
		AS_HEURISTIC_MOST_COMMON_UC_EVENTS = 3,
		AS_HEURISTIC_MOST_COMMON_UC_ARCS = 4;

    // constants
    public static final int LINE_WIDTH = 55;    // # of chars per line?, screen width
    private static final int DEFAULT_MAX_PARTITION_SIZE = 3000; // max nodes/partition

    // options
    public static final boolean use_cudd = false;
    public static boolean fill_statevars = false;
    public static boolean profile_on = false; // misc profiling stuff
    public static boolean debug_on = false; // proof generating and general debug
    public static boolean size_watch = false; // BDD sizewatch
    public static boolean sanity_check_on = false;
    public static boolean user_alters_PCG = false;
    public static boolean show_grow = false;
    public static boolean show_encoding = false; // dump variable encoding, not workin very good :(
    public static boolean trace_on = false;
    public static boolean local_saturation = false;
    public static boolean uc_optimistic = true;
    public static boolean nb_optimistic = true;


    public static int ordering_algorithm = ORDERING_ALGO_NEW_TSP;
    public static int algo_family = ALGO_SMOOTHED_MONO;
    public static int count_algo  = COUNT_TREE;
    public static int max_partition_size = DEFAULT_MAX_PARTITION_SIZE;
    public static int inclsuion_algorithm = INCLUSION_ALGO_MODULAR;
	public static int as_heuristics  = AS_HEURISTIC_MOST_COMMON_UC_ARCS;

    // out own out stream, might be changed to point to a file
    public static PrintStream out = System.out;

}
