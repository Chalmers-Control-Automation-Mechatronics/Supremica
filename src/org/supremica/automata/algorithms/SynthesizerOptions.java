
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.properties.Config;


public final class SynthesizerOptions
{
    private static Logger logger = LogManager.getLogger(SynthesizerOptions.class);

    private boolean dialogOK = false;
    private SynthesisType synthesisType;
    private SynthesisAlgorithm synthesisAlgorithm;
    private boolean purge;
	private boolean rename;
    private boolean removeUnnecessarySupervisors;
    private boolean maximallyPermissive;
    private boolean maximallyPermissiveIncremental;
    private boolean reduceSupervisors;
    private boolean localizeSupervisors;
    private boolean rememberDisabledUncontrollableEvents;
	private boolean supervisorsAsPlants;

    public boolean oneEventAtATime = false;
    public boolean addOnePlantAtATime = false;


    /**
     * The current options, based on earlier user preferences.
     */
    public SynthesizerOptions()
    {
        this(Config.SYNTHESIS_SYNTHESIS_TYPE.getValue(),
            Config.SYNTHESIS_ALGORITHM_TYPE.getValue(),
            Config.SYNTHESIS_PURGE.getValue(),
            Config.SYNTHESIS_RENAME.getValue(),
            Config.SYNTHESIS_OPTIMIZE.getValue(),
            Config.SYNTHESIS_MAXIMALLY_PERMISSIVE.getValue(),
            Config.SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL.getValue(),
            Config.SYNTHESIS_REDUCE_SUPERVISORS.getValue(),
            Config.SYNTHESIS_LOCALIZE_SUPERVISORS.getValue(),
            Config.SYNTHESIS_SUP_AS_PLANT.getValue());
    }

    /**
     * This is not a good constructor so it is private, it is impossible to read in the code.
     * Use the "getDefault..."-methods in this class instead or when they won't suit you,
     * modify the necessary options one by one, starting from default! Much more readable and
     * also more practical when adding new options.
     */
    private SynthesizerOptions(final SynthesisType synthesisType, final SynthesisAlgorithm synthesisAlgorithm,
    		final boolean purge, final boolean rename, final boolean removeUnnecessarySupervisors, final boolean maximallyPermissive,
    		final boolean maximallyPermissiveIncremental, final boolean reduceSupervisors,
    		final boolean localizeSupervisors, final boolean supervisorsAsPlants)
    {
        this.synthesisType = synthesisType;
        this.synthesisAlgorithm = synthesisAlgorithm;
        this.purge = purge;
		this.rename = rename;
        this.removeUnnecessarySupervisors = removeUnnecessarySupervisors;
        this.maximallyPermissive = maximallyPermissive;
        this.maximallyPermissiveIncremental = maximallyPermissiveIncremental;
        this.reduceSupervisors = reduceSupervisors;
        this.localizeSupervisors = localizeSupervisors;
		this.supervisorsAsPlants = supervisorsAsPlants;
    }

    public boolean isValid()
    {
        final String errorMessage = validOptions();
        if (errorMessage != null)
        {
            logger.error(errorMessage);
            return false;
        }

        return true;
    }

    public String validOptions()
    {
        /* if (synthesisType == null)
        {
            return "Unknown synthesis type.";
        }*/
		assert(synthesisType != null);

        if (synthesisAlgorithm == SynthesisAlgorithm.MONOLITHICBDD)
        {
            if (synthesisType != SynthesisType.NONBLOCKING)
            {
                return("BDD2 algorithms currently only support supNB synthesis.");
            }
        }
        return null;
    }

    public void setDialogOK(final boolean bool)
    {
        dialogOK = bool;
    }

    public boolean getDialogOK()
    {
        return dialogOK;
    }

    public void setSynthesisType(final SynthesisType type)
    {
        synthesisType = type;
    }

    public SynthesisType getSynthesisType()
    {
        return synthesisType;
    }

    public void setSynthesisAlgorithm(final SynthesisAlgorithm algorithm)
    {
        synthesisAlgorithm = algorithm;
    }

    public SynthesisAlgorithm getSynthesisAlgorithm()
    {
        return synthesisAlgorithm;
    }

    public void setPurge(final boolean bool)
    {
        purge = bool;
    }
    public boolean doPurge()
    {
        return purge;
    }
	public void setRename(final boolean bool)
	{
		rename = bool;
	}
	public boolean doRename()
	{
		return rename;
	}

    public void setRememberDisabledUncontrollableEvents(final boolean remember)
    {
        rememberDisabledUncontrollableEvents = remember;
    }

    public boolean doRememberDisabledUncontrollableEvents()
    {
        return rememberDisabledUncontrollableEvents;
    }

    public void setRemoveUnecessarySupervisors(final boolean bool)
    {
        removeUnnecessarySupervisors = bool;
    }

    public boolean getRemoveUnecessarySupervisors()
    {
        return removeUnnecessarySupervisors;
    }

    public void setMaximallyPermissive(final boolean bool)
    {
        maximallyPermissive = bool;
    }

    public boolean getMaximallyPermissive()
    {
        return maximallyPermissive;
    }

    public void setMaximallyPermissiveIncremental(final boolean bool)
    {
        maximallyPermissiveIncremental = bool;
    }

    public boolean getMaximallyPermissiveIncremental()
    {
        return maximallyPermissiveIncremental;
    }

    public void setReduceSupervisors(final boolean bool)
    {
        reduceSupervisors = bool;
    }

    public boolean getReduceSupervisors()
    {
        return reduceSupervisors;
    }

    public void setLocalizeSupervisors(final boolean bool)
    {
        localizeSupervisors = bool;
    }

    public boolean getLocalizeSupervisors()
    {
        return localizeSupervisors;
    }


    /**
     * Stores the current set of options in SupremicaProperties.
	 * MF -- I am not at all sure that we should do this, changing the options
	 * MF -- when synthesizing should only be temporal, or at most hold for the
	 * MF -- current session of Supremica. I do not think that it should change
	 * MF -- the global configuration. But this is now the way we have always done it, so...
     */
    public void saveOptions()
    {
        Config.SYNTHESIS_SYNTHESIS_TYPE.setValue(synthesisType);
        Config.SYNTHESIS_ALGORITHM_TYPE.setValue(synthesisAlgorithm);
        Config.SYNTHESIS_PURGE.setValue(purge);
        Config.SYNTHESIS_RENAME.setValue(rename);
        Config.SYNTHESIS_OPTIMIZE.setValue(removeUnnecessarySupervisors);
        Config.SYNTHESIS_MAXIMALLY_PERMISSIVE.setValue(maximallyPermissive);
        Config.SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL.setValue(maximallyPermissiveIncremental);
        Config.SYNTHESIS_REDUCE_SUPERVISORS.setValue(reduceSupervisors);
        Config.SYNTHESIS_LOCALIZE_SUPERVISORS.setValue(localizeSupervisors);
        Config.SYNTHESIS_SUP_AS_PLANT.setValue(supervisorsAsPlants);
    }

    /**
     * Returns the default options for synthesis---modular synthesis of controllability only.
     */
    public static SynthesizerOptions getDefaultSynthesizerOptions()
    {
        return new SynthesizerOptions(SynthesisType.CONTROLLABLE,
									  SynthesisAlgorithm.MODULAR,
                                      true,		// SYNTHESIS_PURGE
									  false,	// SYNTHESIS_RENAME
									  true,		// SYNTHESIS_OPTIMIZE
									  true,		// SYNTHESIS_MAXIMALLY_PERMISSIVE
									  true,		// SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL
									  true,		// SYNTHESIS_REDUCE_SUPERVISORS
									  true,		// SYNTHESIS_LOCALIZE_SUPERVISORS
									  false);	// SYNTHESIS_SUP_AS_PLANT
    }

//    public static SynthesizerOptions getDefaultSynthesizerOptionsS()
//    {
//
//        return new SynthesizerOptions(SynthesisType.NONBLOCKINGCONTROLLABLE, SynthesisAlgorithm.SYNTHESISA,
//                                      true, true, true, false, false, true, false, false);
//    }
    /**
     * Returns the default options for synthesis.
     */
    public static SynthesizerOptions getDefaultMonolithicCNBSynthesizerOptions()
    {
        final SynthesizerOptions options = getDefaultSynthesizerOptions();
		options.synthesisType = SynthesisType.NONBLOCKING_CONTROLLABLE;
        options.synthesisAlgorithm = SynthesisAlgorithm.MONOLITHIC;
        options.removeUnnecessarySupervisors = true;
        options.reduceSupervisors = true;
        options.localizeSupervisors = true;
		options.supervisorsAsPlants = false; // This has always been the default, but probably always wrong!

        return options;
    }


}
