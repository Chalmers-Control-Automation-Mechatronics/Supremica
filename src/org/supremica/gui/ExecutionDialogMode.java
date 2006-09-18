
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.gui;

/**
 * Different modes that the exececution dialog can display.
 */
public class ExecutionDialogMode
{
    // Arguments are ExecutionDialogMode(String ID, String txt, boolean value, boolean progress)
    
    // Keep these in alphabetical order for convenience
    public static final ExecutionDialogMode HIDE = new ExecutionDialogMode("Hide", "", false, false);
    public static final ExecutionDialogMode MINIMIZING = new ExecutionDialogMode("Minimizing...", "", false, true);
    public static final ExecutionDialogMode MINIMIZINGSINGLE = new ExecutionDialogMode("Minimizing...", "Number of partitions", true, false);
    public static final ExecutionDialogMode SYNCHRONIZING = new ExecutionDialogMode("Synchronizing...", "Number of states:", true, false);
    public static final ExecutionDialogMode SYNCHRONIZINGBUILDINGSTATES = new ExecutionDialogMode("Building automaton...", "Building states", false, true);
    public static final ExecutionDialogMode SYNCHRONIZINGBUILDINGTRANSITIONS = new ExecutionDialogMode("Building automaton...", "Building transitions", false, true);
    public static final ExecutionDialogMode SYNTHESIZING = new ExecutionDialogMode("Synthesizing...", "", false, true);
    public static final ExecutionDialogMode SYNTHESISOPTIMIZING = new ExecutionDialogMode("Optimizing...", "", false, true);
    public static final ExecutionDialogMode SYNTHESISREDUCING = new ExecutionDialogMode("Reducing...", "", false, true);
    public static final ExecutionDialogMode VERIFYING = new ExecutionDialogMode("Verifying...", "", true, false);
    public static final ExecutionDialogMode VERIFYINGNONBLOCKING = new ExecutionDialogMode("Verifying nonblocking...", "", false, true);
    public static final ExecutionDialogMode VERIFYINGMUTUALNONBLOCKINGFIRSTRUN = new ExecutionDialogMode("Verifying mutual nonblocking...", "First run", false, true);
    public static final ExecutionDialogMode VERIFYINGMUTUALNONBLOCKINGSECONDRUN = new ExecutionDialogMode("Verifying mutual nonblocking...", "Second run", false, true);
    public static final ExecutionDialogMode UNINITIALIZED = new ExecutionDialogMode("Uninitialized", "", false, false);
    
    private final String ID;
    private final String TEXT;
    private final boolean SHOWVALUE;
    private final boolean SHOWPROGRESS;
    
    private ExecutionDialogMode(String id, String txt, boolean value, boolean progress)
    {
        this.ID = id;
        this.TEXT = txt;
        this.SHOWVALUE = value;
        this.SHOWPROGRESS = progress;
    }
    
    public String getId()
    {
        return ID;
    }
    
    public String getText()
    {
        return TEXT;
    }
    
    public boolean showValue()
    {
        return SHOWVALUE;
    }
    
    public boolean showProgress()
    {
        return SHOWPROGRESS;
    }
}
