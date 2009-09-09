//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.automata.algorithms
//# CLASS:   SynchronizationOptions
//###########################################################################
//# $Id$
//###########################################################################

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
package org.supremica.automata.algorithms;

import org.supremica.log.*;
import org.supremica.properties.Config;
import org.supremica.util.SupremicaException;

public class SynchronizationOptions
{
	private static Logger logger = LoggerFactory.createLogger(SynchronizationOptions.class);
	private SynchronizationType syncType;    // PRIORITIZED, FULL, Broadcast, Unknown
	private boolean forbidUnconStates;    // mark uc-states as uncontrollable
	private boolean expandForbiddenStates;    // expand beyond an uc-state
	private int initialHashtableSize;
	private boolean expandHashtable;
	private int nbrOfExecuters;
	private boolean buildAutomaton;    // Build automaton (also add the arcs)
	private boolean useShortStateNames;    // Generate short, abstract, state names
	private boolean expandEventsUsingPriority;    // ??
	private boolean requireConsistentControllability;    // check that common events have same controllability
	private boolean requireConsistentImmediate;    // check that common evenst have same immediaticity
	private boolean rememberDisabledEvents;    // redirect disabled transitions to forbidden dump-state
    private boolean mEFAMode;
	private boolean dialogOK = false;
	private String automatonNameSeparator;
	private String stateNameSeparator;
    private boolean EFAMode;

	/**
	 * Default constructor
	 *
	 * The parametrisized constructor was removed due to unreadable code.
	 * Use the "getDefault..."-methods in this class instead or when they won't suit you,
	 * modify the necessary options one by one, starting from default! Much more readable and
	 * also more practical when adding new options.
	 * @throws SupremicaException
	 */
	public SynchronizationOptions()
	// throws SupremicaException
	{
		this.nbrOfExecuters = Config.SYNC_NBR_OF_EXECUTERS.get();
		//The following check should ideally be done within SupremicaProperties
		if (this.nbrOfExecuters != 1)
		{
			//			throw new SupremicaException("Error in SupremicaProperties. The property synchNbrOfExecuters must be at least 1.");
		}

		this.syncType = SynchronizationType.PRIORITIZED;
		assert this.syncType != null;

		this.initialHashtableSize = Config.SYNC_INITIAL_HASHTABLE_SIZE.get();
		//The following check should ideally be done within SupremicaProperties
		if (this.initialHashtableSize < 100)
		{
			//			throw new SupremicaException("Error in SupremicaProperties. The property syncInitialHashtableSize must be at least 100");
		}

		this.expandHashtable = Config.SYNC_EXPAND_HASHTABLE.isTrue();
		this.forbidUnconStates = Config.SYNC_FORBID_UNCON_STATES.isTrue();
		this.expandForbiddenStates = Config.SYNC_EXPAND_FORBIDDEN_STATES.isTrue();
		this.expandEventsUsingPriority = false;
		this.buildAutomaton = true;
		this.useShortStateNames = false;
		this.requireConsistentControllability = true;
		this.requireConsistentImmediate = true;
		this.rememberDisabledEvents = false;
		this.automatonNameSeparator = Config.SYNC_AUTOMATON_NAME_SEPARATOR.getAsString();
		this.stateNameSeparator = Config.GENERAL_STATE_SEPARATOR.getAsString();
        this.EFAMode = false;

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

	public void setExpandHashtable(boolean set)
	{
		expandHashtable = set;
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

	public boolean buildAutomaton()
	{
		return buildAutomaton;
	}

	public void setBuildAutomaton(boolean set)
	{
		buildAutomaton = set;
	}

	public boolean useShortStateNames()
	{
		return useShortStateNames;
	}

	public void setUseShortStateNames(boolean set)
	{
		useShortStateNames = set;
	}

    // Added these two methods and the mEFAMode member.
    // I hope I did it correctly. ~~~Robi
    public boolean getEFAMode()
    {
        return mEFAMode;
    }

    public void setEFAMode(final boolean mode)
    {
        mEFAMode = mode;
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
		SynchronizationOptions options = new SynchronizationOptions();
		options.setSynchronizationType(SynchronizationType.FULL);
		// options.setForbidUncontrollableStates(true);  // This is controversial!
		options.setForbidUncontrollableStates(false);   // So why was it like that?
		options.setExpandForbiddenStates(true);
		options.setExpandHashtable(true);
		return options;
	}

	/**
	 * Returns the default options for verification. For example, when performing verification, we do
	 * not want to build the full automaton model. The abstract indexForm representation is enough.
	 * That is why buildAutomaton is set to be false.
	 */
	public static SynchronizationOptions getDefaultVerificationOptions()
	{
		SynchronizationOptions options = getDefaultSynchronizationOptions();
		options.setForbidUncontrollableStates(true); // Doesn't really matter, this is not how controllability is decided
		options.setBuildAutomaton(false);        // We don't want to see the stuff, anyway...
		options.setExpandForbiddenStates(false); // Don't need to do that, then...
		return options;
	}

	/**
	 * Returns the default options for synthesis.
	 */
	public static SynchronizationOptions getDefaultSynthesisOptions()
	{
		SynchronizationOptions options = getDefaultSynchronizationOptions();
		// This is important!
		options.setForbidUncontrollableStates(true); 
		// Don't need to do expand forbidden if we're not using them anyway...
		options.setExpandForbiddenStates(false); 

		// Return result
		return options;
	}

	/**
	 * @param automatonNameSeparator The automatonNameSeparator to set.
	 */
	public void setAutomatonNameSeparator(String automatonNameSeparator)
	{
		this.automatonNameSeparator = automatonNameSeparator;
	}

	/**
	 * @return Returns the automatonNameSeparator.
	 */
	public String getAutomatonNameSeparator()
	{
		assert automatonNameSeparator != null;
		return automatonNameSeparator;
	}
	/**
	 * @return Returns the stateNameSeparator.
	 */
	public String getStateNameSeparator()
	{
		assert stateNameSeparator != null;
		return stateNameSeparator;
	}
	/**
	 * @param stateNameSeparator The stateNameSeparator to set.
	 */
	public void setStateNameSeparator(String stateNameSeparator)
	{
		this.stateNameSeparator = stateNameSeparator;
	}
}
