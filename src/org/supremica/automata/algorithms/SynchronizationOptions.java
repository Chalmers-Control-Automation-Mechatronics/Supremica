
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
package org.supremica.automata.algorithms;

import org.supremica.gui.*;
import org.apache.log4j.*;

public final class SynchronizationOptions
{
	private static Category thisCategory = LogDisplay.createCategory(SynchronizationOptions.class.getName());
	private final SynchronizationType syncType;
	private final boolean forbidUnconStates;
	private final boolean expandForbiddenStates;
	private final int initialHashtableSize;
	private final boolean expandHashtable;
	private final int nbrOfExecuters;
	private final boolean terminateIfUnconState;
	private final boolean buildAutomaton;
	private final boolean expandEventsUsingPriority;
	private final boolean verboseMode;

	public SynchronizationOptions()
		throws Exception
	{
		this(WorkbenchProperties.syncNbrOfExecuters(), SynchronizationType.Prioritized, WorkbenchProperties.syncInitialHashtableSize(), WorkbenchProperties.syncExpandHashtable(), WorkbenchProperties.syncForbidUncontrollableStates(), WorkbenchProperties.syncExpandForbiddenStates(), false, false, true, WorkbenchProperties.verboseMode());
	}

	public SynchronizationOptions(int nbrOfExecuters, SynchronizationType syncType, int initialHashtableSize, boolean expandHashtable, boolean forbidUnconStates, boolean expandForbiddenStates, boolean terminateIfUnconState, boolean expandEventsUsingPriority, boolean buildAutomaton, boolean verboseMode)
		throws Exception
	{
		if (syncType == null)
		{
			throw new Exception("synchType must be non-null");
		}

		if (nbrOfExecuters < 1)
		{
			throw new Exception("nbrOfExcuters must be at least 1");
		}

		if (initialHashtableSize < 100)
		{
			throw new Exception("initialHashtableSize must be at least 100");
		}

		this.nbrOfExecuters = nbrOfExecuters;
		this.syncType = syncType;
		this.initialHashtableSize = initialHashtableSize;
		this.expandHashtable = expandHashtable;
		this.forbidUnconStates = forbidUnconStates;
		this.expandForbiddenStates = expandForbiddenStates;
		this.terminateIfUnconState = terminateIfUnconState;
		this.expandEventsUsingPriority = expandEventsUsingPriority;
		this.buildAutomaton = buildAutomaton;
		this.verboseMode = verboseMode;
	}

	public int getNbrOfExecuters()
	{
		return nbrOfExecuters;
	}

	public SynchronizationType getSynchronizationType()
	{
		return syncType;
	}

	public int getInitialHashtableSize()
	{
		return initialHashtableSize;
	}

	public boolean expandHashtable()
	{
		return expandHashtable;
	}

	public boolean forbidUncontrollableStates()
	{
		return forbidUnconStates;
	}

	public boolean expandForbiddenStates()
	{
		return expandForbiddenStates;
	}

	public boolean terminateIfUncontrollableState()
	{
		return terminateIfUnconState;
	}

	public boolean expandEventsUsingPriority()
	{
		return expandEventsUsingPriority;
	}

	public boolean verboseMode()
	{
		return verboseMode;
	}

	/**
	 Build an Automaton after synchronization.
	*/
	public boolean buildAutomaton()
	{
		return buildAutomaton;
	}
}
