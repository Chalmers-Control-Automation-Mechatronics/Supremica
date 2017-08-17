package org.supremica.util.BDD;

import java.io.PrintStream;

public class Options
{
    /** Automaton ordering algorithm */
    public static final String[] ORDERING_ALGORITHM_NAMES = { "Random (!)",
    "PCG search",
    "modified TSP",
    "Topological sort (DFS)",
    "Topological sort (BFS)",
    "STCT: simulated annealing",
    "TSP + STCT:SA ",
    "TSP + sifting",
    "Aloul's FORCE",
    "Aloul's FORCE + win4",
    };
    public static final int AO_HEURISTIC_RANDOM = 0, AO_HEURISTIC_PCG = 1,
        AO_HEURISTIC_TSP = 2, AO_HEURISTIC_DFS = 3,
        AO_HEURISTIC_BFS = 4, AO_HEURISTIC_STCT = 5,
        AO_HEURISTIC_TSP_STCT = 6, AO_HEURISTIC_TSP_SIFT = 7,
        AO_HEURISTIC_FORCE = 8, AO_HEURISTIC_FORCE_WIN4 = 9;
    public static int ordering_algorithm = AO_HEURISTIC_FORCE;

    // --- [ constants ] -------------------------------------------------
    // when to start using quick sort
    public static final int MIN_QUICKSORT_THRESHOLD = 8;

    // --- [ options ] -------------------------------------------------
    public static boolean profile_on = false;    // misc profiling stuff
    public static boolean debug_on = false;    // proof generating and general debug
    public static boolean test_integrity = false; /** enables some internal test routines that are usually turned off to save time */

    // our own out stream, might be changed to point to a file
    public static PrintStream out = System.out;
}
