
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

import org.supremica.log.*;
import org.supremica.properties.SupremicaProperties;

public final class SynchronizationOptions
{
	private static Logger logger = LoggerFactory.createLogger(SynchronizationOptions.class);
	private SynchronizationType syncType;    // Prioritized, Full, Broadcast, Unknown
	private boolean forbidUnconStates;    // mark uc-states as uncontrollable
	private boolean expandForbiddenStates;    // expand beyond an uc-state
	private int initialHashtableSize;
	private boolean expandHashtable;
	private int nbrOfExecuters;
	private boolean buildAutomaton;    // add also the arcs
	private boolean expandEventsUsingPriority;    // ??
	private boolean verboseMode;    // write stuff on debug window
	private boolean requireConsistentControllability;    // check that common events have same controllability
	private boolean requireConsistentImmediate;    // check that common evenst have same immediaticity
	private boolean rememberDisabledEvents;    // redirect disabled transitions to forbidden dump-state
	private boolean dialogOK = false;

	/**
	 * The default options, based on earlier user preferences.
	 */
	public SynchronizationOptions()
		throws IllegalArgumentException
	{
		this(SupremicaProperties.syncNbrOfExecuters(), SynchronizationType.Prioritized, SupremicaProperties.syncInitialHashtableSize(), SupremicaProperties.syncExpandHashtable(), SupremicaProperties.syncForbidUncontrollableStates(), SupremicaProperties.syncExpandForbiddenStates(), 
			 false,    // expandEventsUsingPriority
			 true,    // buildAutomaton
			 SupremicaProperties.verboseMode(), true,    // requireConsistentControllability
			 true,    // requireConsistentImmediate
			 false);    // rememberDisabledEvents
	}

	/**
	 * This is not a good constructor so it is private, it is impossible to read in the code. 
	 * Use the "getDefault..."-methods in this class instead or when they won't suit you, 
	 * modify the necessary options one by one, starting from default! Much more readable and
	 * also more practical when adding new options.
	 */
	private SynchronizationOptions(int nbrOfExecuters, SynchronizationType syncType, int initialHashtableSize, boolean expandHashtable, boolean forbidUnconStates, boolean expandForbiddenStates, boolean expandEventsUsingPriority, boolean buildAutomaton, boolean verboseMode, boolean requireConsistentControllability, boolean requireConsistentImmediate, boolean rememberDisabledEvents)
		throws IllegalArgumentException
	{
		if (syncType == null)
		{
			throw new IllegalArgumentException("synchType must be non-null");
		}

		if (nbrOfExecuters < 1)
		{
			throw new IllegalArgumentException("nbrOfExcuters must be at least 1");
		}

		if (initialHashtableSize < 100)
		{
			throw new IllegalArgumentException("initialHashtableSize must be at least 100");
		}

		this.nbrOfExecuters = nbrOfExecuters;
		this.syncType = syncType;
		this.initialHashtableSize = initialHashtableSize;
		this.expandHashtable = expandHashtable;
		this.forbidUnconStates = forbidUnconStates;
		this.expandForbiddenStates = expandForbiddenStates;
		this.expandEventsUsingPriority = expandEventsUsingPriority;
		this.buildAutomaton = buildAutomaton;
		this.verboseMode = verboseMode;
		this.requireConsistentControllability = requireConsistentControllability;
		this.requireConsistentImmediate = requireConsistentImmediate;
		this.rememberDisabledEvents = rememberDisabledEvents;
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
	
	public void setExpandEventsUsingPriority(boolean set)
	{
		expandEventsUsingPriority = set;
	}

	public boolean expandEventsUsingPriority()
	{
		return expandEventsUsingPriority;
	}

	public void setVerboseMode(boolean set)
	{
		verboseMode = set;
	}

	public boolean verboseMode()
	{
		return verboseMode;
	}

	public boolean requireConsistentControllability()
	{
		return requireConsistentControllability;
	}

	public void setRequireConsistentControllability(boolean require)
	{
		requireConsistentControllability = require;
	}

	public boolean requireConsistentImmediate()
	{
		return requireConsistentImmediate;
	}

	public void setRequireConsistentImmediate(boolean require)
	{
		requireConsistentImmediate = require;
	}

	public boolean rememberDisabledEvents()
	{
		return rememberDisabledEvents;
	}

	public void setRememberDisabledEvents(boolean remember)
	{
		rememberDisabledEvents = remember;
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

	/**
	 * Returns the default options for synchronization. This is the same as
	 * in the default constructor in this class.
	 */
	public static SynchronizationOptions getDefaultSynchronizationOptions()
	{
		return new SynchronizationOptions();
	}

	/**
	 * Returns the default options for verification.
	 */
	public static SynchronizationOptions getDefaultVerificationOptions()
	{
		SynchronizationOptions options = new SynchronizationOptions();
		options.setBuildAutomaton(false);
		return options;
	}

	/**
	 * Returns the default options for synthesis.
	 */
	public static SynchronizationOptions getDefaultSynthesisOptions()
	{
		SynchronizationOptions options = new SynchronizationOptions();
		options.setForbidUncontrollableStates(true);
		return options;
	}
}
