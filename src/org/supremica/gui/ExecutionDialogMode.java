//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2019 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

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
