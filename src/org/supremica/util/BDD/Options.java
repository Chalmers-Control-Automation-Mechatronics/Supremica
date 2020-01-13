package org.supremica.util.BDD;

import java.io.PrintStream;

public class Options
{

    // --- [ constants ] -------------------------------------------------
    // when to start using quick sort
    public static final int MIN_QUICKSORT_THRESHOLD = 8;

    // --- [ options ] -------------------------------------------------
    public static boolean test_integrity = false; /** enables some internal test routines that are usually turned off to save time */

    // our own out stream, might be changed to point to a file
    public static PrintStream out = System.out;
}
