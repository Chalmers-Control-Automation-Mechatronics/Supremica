package org.supremica.util.BDD;

public class Options
{
    /** search algorithm */
    public static final int 
	ALGO_MONOLITHIC = 0,
	ALGO_CONJUNCTIVE = 1,
	ALGO_DISJUNCTIVE = 2,
	ALGO_SMOOTHED = 3
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

	
    // constants
    public static final int LINE_WIDTH = 55;    // # of chars per line?, screen width
    private static final int DEFAULT_MAX_PARTITION_SIZE = 3000; // max nodes/partition

    // options
    public static final boolean use_cudd = false;
    public static boolean debug_on = false;
    public static boolean sanity_check_on = false;
    public static boolean user_alters_PCG = false;
    public static boolean show_grow = false;
    public static boolean trace_on = false;
    public static boolean local_saturation = true;
    public static boolean uc_optimistic = true;
    public static boolean nb_optimistic = true;

    public static int ordering_algorithm = ORDERING_ALGO_NEW_TSP;
    public static int algo_family = ALGO_SMOOTHED;
    public static int count_algo  = COUNT_TREE;
    public static int max_partition_size = DEFAULT_MAX_PARTITION_SIZE;
}
