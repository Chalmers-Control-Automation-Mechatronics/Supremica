
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
import org.supremica.log.*;
import org.supremica.properties.SupremicaProperties;

public final class SynchronizationOptions
{
	private static Logger logger = LoggerFactory.createLogger(SynchronizationOptions.class);
	private SynchronizationType syncType;
	private boolean forbidUnconStates;
	private boolean expandForbiddenStates;
	private int initialHashtableSize;
	private boolean expandHashtable;
	private int nbrOfExecuters;
	private boolean terminateIfUnconStates;
	private boolean buildAutomaton;
	private boolean expandEventsUsingPriority;
	private boolean verboseMode;
	private boolean dialogOK = false;

	public SynchronizationOptions()
		throws Exception
	{
		this(SupremicaProperties.syncNbrOfExecuters(), SynchronizationType.Prioritized, SupremicaProperties.syncInitialHashtableSize(), SupremicaProperties.syncExpandHashtable(), SupremicaProperties.syncForbidUncontrollableStates(), SupremicaProperties.syncExpandForbiddenStates(), false, false, true, SupremicaProperties.verboseMode());
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
		this.terminateIfUnconStates = terminateIfUnconState;
		this.expandEventsUsingPriority = expandEventsUsingPriority;
		this.buildAutomaton = buildAutomaton;
		this.verboseMode = verboseMode;
	}

	public void setDialogOK(boolean bool)
	{
		dialogOK = bool;
	}

	public boolean getDialogOK()
	{
		return dialogOK;
	}

	public int getNbrOfExecuters()
	{
		return nbrOfExecuters;
	}

	public SynchronizationType getSynchronizationType()
	{
		return syncType;
	}

	public void setSynchronizationType(SynchronizationType type)
	{
		syncType = type;
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

	public void setForbidUncontrollableStates(boolean set)
	{
		forbidUnconStates = set;
	}

	public boolean expandForbiddenStates()
	{
		return expandForbiddenStates;
	}

	public void setExpandForbiddenStates(boolean set)
	{
		expandForbiddenStates = set;
	}

	public boolean terminateIfUncontrollableState()
	{
		return terminateIfUnconStates;
	}

	public void setTerminateIfUncontrollableState(boolean set)
	{
		terminateIfUnconStates = set;
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

	public void setBuildAutomaton(boolean set)
	{
		buildAutomaton = set;
	}

	public boolean isValid()
	{
		if (syncType == null)
		{
			return false;
		}

		if (nbrOfExecuters < 1)
		{
			return false;
		}

		if (initialHashtableSize < 100)
		{
			return false;
		}

		return true;
	}
}
