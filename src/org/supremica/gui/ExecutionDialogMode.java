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
 * Haradsgatan 26A
 * 431 42 Molndal
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

public class ExecutionDialogMode
{
	public static ExecutionDialogMode synchronizing = 
		new ExecutionDialogMode("Synchronizing...", "Number of states:", true, false);
	public static ExecutionDialogMode verifying = 
		//new ExecutionDialogMode("Verifying...", "Number of states:", true, false);
		new ExecutionDialogMode("Verifying...", "", true, false);
	public static ExecutionDialogMode synthesizing = 
		new ExecutionDialogMode("Synthesizing...", "Number of states:", true, false);
	public static ExecutionDialogMode buildingStates = 
		new ExecutionDialogMode("Building automaton...", "Building states", false, true);
	public static ExecutionDialogMode buildingTransitions = 
		new ExecutionDialogMode("Building automaton...", "Building transitions", false, true);
	public static ExecutionDialogMode matchingStates = 
		new ExecutionDialogMode("Matching States...", "", false, true);
	public static ExecutionDialogMode verifyingNonblocking = 
		new ExecutionDialogMode("Verifying nonblocking...", "", false, true);
	public static ExecutionDialogMode verifyingMutualNonblockingFirstRun = 
		new ExecutionDialogMode("Verifying mutual nonblocking...", "First run", false, true);
	public static ExecutionDialogMode verifyingMutualNonblockingSecondRun = 
		new ExecutionDialogMode("Verifying mutual nonblocking...", "Second run", false, true);
	public static ExecutionDialogMode hide = 
		new ExecutionDialogMode("Hide", "", false, false);
	public static ExecutionDialogMode uninitialized = 
		new ExecutionDialogMode("Uninitialized", "", false, false);
	private final String id;
	private String text;
	private boolean show_value;
	private boolean show_progress;

	private ExecutionDialogMode(String id, String txt, boolean value, boolean progress)
	{
		this.id = id;
		this.text = txt;
		this.show_value = value;
		this.show_progress = progress;
	}

	public String getId()
	{
		return id;
	}

	public String getText()
	{
		return text;
	}

	public boolean showValue()
	{
		return show_value;
	}

	public boolean showProgress()
	{
		return show_progress;
	}
}
