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

    public static final int LINE_WIDTH = 55;    // # of chars per line?, screen width
    public static final boolean use_cudd = false;
    public static boolean debug_on = false;
    public static boolean sanity_check_on = false;
    public static boolean user_alters_PCG = false;
    public static boolean show_grow = true;
    public static boolean trace_on = false;
    public static int algo_family = ALGO_SMOOTHED;
    public static int count_algo  = COUNT_TREE;
}
