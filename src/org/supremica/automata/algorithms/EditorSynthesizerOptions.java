
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

import org.supremica.properties.Config;
import org.supremica.log.*;

public final class EditorSynthesizerOptions
{
    private static Logger logger = LoggerFactory.createLogger(SynthesizerOptions.class);

    private boolean dialogOK = false;
    private SynthesisType synthesisType;
    private SynthesisAlgorithm synthesisAlgorithm;
    private boolean purge;
    private boolean removeUnnecessarySupervisors;
    private boolean maximallyPermissive;
    private boolean maximallyPermissiveIncremental;
    private boolean reduceSupervisors;
    private boolean generateGuard;
    private boolean reachability;
    private boolean rememberDisabledUncontrollableEvents;

    private boolean bddExtractSupervisor;

    public boolean oneEventAtATime = false;
    public boolean addOnePlantAtATime = false;


    //Guard options
	private String event;
	private int expressionType;    // 0: the guard expression will be generated from the forbidden states; 1: from allowed states; 2: Adaptive case

    /**
     * The current options, based on earlier user preferences.
     */
    public EditorSynthesizerOptions()
    {
        this((SynthesisType) Config.SYNTHESIS_SYNTHESIS_TYPE.get(),
            (SynthesisAlgorithm) Config.SYNTHESIS_ALGORITHM_TYPE.get(),
            Config.SYNTHESIS_PURGE.get(),
            Config.SYNTHESIS_OPTIMIZE.get(),
            Config.SYNTHESIS_MAXIMALLY_PERMISSIVE.get(),
            Config.SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL.get(),
            Config.SYNTHESIS_REDUCE_SUPERVISORS.get(),
            Config.BDD_SYNTHESIS_EXTRACT_AUTOMATON.get(),
            Config.SYNTHESIS_GUARD.get(),
            Config.SYNTHESIS_REACHABILITY.get());
    }

    /**
     * This is not a good constructor so it is private, it is impossible to read in the code.
     * Use the "getDefault..."-methods in this class instead or when they won't suit you,
     * modify the necessary options one by one, starting from default! Much more readable and
     * also more practical when adding new options.
     */
    private EditorSynthesizerOptions(SynthesisType synthesisType, SynthesisAlgorithm synthesisAlgorithm,
    		boolean purge, boolean removeUnnecessarySupervisors, boolean maximallyPermissive,
    		boolean maximallyPermissiveIncremental, boolean reduceSupervisors, boolean bddExtractSupervisor, boolean generateGuard, boolean reachability)
    {
        this.synthesisType = synthesisType;
        this.synthesisAlgorithm = synthesisAlgorithm;
        this.purge = purge;
        this.removeUnnecessarySupervisors = removeUnnecessarySupervisors;
        this.maximallyPermissive = maximallyPermissive;
        this.maximallyPermissiveIncremental = maximallyPermissiveIncremental;
        this.reduceSupervisors = reduceSupervisors;
        this.bddExtractSupervisor = bddExtractSupervisor;
        this.generateGuard = generateGuard;
        this.reachability = reachability;

        this.event = "";
		this.expressionType = 2;
    }

    public boolean isValid()
    {
        String errorMessage = validOptions();
        if (errorMessage != null)
        {
            logger.error(errorMessage);
            return false;
        }

        return true;
    }

    public String validOptions()
    {
        if (synthesisType == null)
        {
            return "Unknown synthesis type.";
        }

        //At present, it is only possible to synthesize with BDDs.
/*
        if (synthesisAlgorithm == SynthesisAlgorithm.BDD)
        {
            if ((synthesisType != SynthesisType.NONBLOCKINGCONTROLLABLE) &&
                (synthesisType != SynthesisType.CONTROLLABLE) &&
                (synthesisType != SynthesisType.NONBLOCKING))
            {
                return("BDD algorithms currently only support supNB+C synthesis.");
            }
        }

        if (synthesisAlgorithm == SynthesisAlgorithm.MONOLITHICBDD)
        {
            if (synthesisType != SynthesisType.NONBLOCKING)
            {
                return("BDD2 algorithms currently only support supNB synthesis.");
            }
        }
 */
        return null;
    }

    public void setDialogOK(boolean bool)
    {
        dialogOK = bool;
    }

    public boolean getDialogOK()
    {
        return dialogOK;
    }

    public void setSynthesisType(SynthesisType type)
    {
        synthesisType = type;
    }

    public SynthesisType getSynthesisType()
    {
        return synthesisType;
    }

    public void setSynthesisAlgorithm(SynthesisAlgorithm algorithm)
    {
        synthesisAlgorithm = algorithm;
    }

    public SynthesisAlgorithm getSynthesisAlgorithm()
    {
        return synthesisAlgorithm;
    }

    public void setPurge(boolean bool)
    {
        purge = bool;
    }

    public boolean doPurge()
    {
        return purge;
    }

    public void setRememberDisabledUncontrollableEvents(boolean remember)
    {
        rememberDisabledUncontrollableEvents = remember;
    }

    public boolean doRememberDisabledUncontrollableEvents()
    {
        return rememberDisabledUncontrollableEvents;
    }

    public void setRemoveUnecessarySupervisors(boolean bool)
    {
        removeUnnecessarySupervisors = bool;
    }

    public boolean getRemoveUnecessarySupervisors()
    {
        return removeUnnecessarySupervisors;
    }

    public void setExtractSupervisor(boolean extract)
    {
        bddExtractSupervisor = extract;
    }

    public boolean doExtractSupervisor()
    {
        return bddExtractSupervisor;
    }

    public void setMaximallyPermissive(boolean bool)
    {
        maximallyPermissive = bool;
    }

    public boolean getMaximallyPermissive()
    {
        return maximallyPermissive;
    }

    public void setMaximallyPermissiveIncremental(boolean bool)
    {
        maximallyPermissiveIncremental = bool;
    }

    public boolean getMaximallyPermissiveIncremental()
    {
        return maximallyPermissiveIncremental;
    }

    public void setReduceSupervisors(boolean bool)
    {
        reduceSupervisors = bool;
    }

    public boolean getReduceSupervisors()
    {
        return reduceSupervisors;
    }

    public boolean getGenerateGuard()
    {
        return generateGuard;
    }

    public boolean getReachability()
    {
        return reachability;
    }

    public void setGenerateGuard(boolean bool)
    {
        generateGuard = bool;
    }

    public void setReachability(boolean bool)
    {
        reachability = bool;
    }


    /**
     * Stores the current set of options in SupremicaProperties.
     */
    public void saveOptions()
    {
        Config.SYNTHESIS_SYNTHESIS_TYPE.set(synthesisType);
        Config.SYNTHESIS_ALGORITHM_TYPE.set(synthesisAlgorithm);
//        Config.SYNTHESIS_PURGE.set(purge);
//        Config.SYNTHESIS_OPTIMIZE.set(removeUnnecessarySupervisors);
        Config.SYNTHESIS_MAXIMALLY_PERMISSIVE.set(maximallyPermissive);
        Config.SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL.set(maximallyPermissiveIncremental);
//        Config.SYNTHESIS_REDUCE_SUPERVISORS.set(reduceSupervisors);
        Config.BDD_SYNTHESIS_EXTRACT_AUTOMATON.set(bddExtractSupervisor);
        Config.SYNTHESIS_GUARD.set(generateGuard);

    }

    /**
     * Returns the default options for synthesis.
     */
    public static EditorSynthesizerOptions getDefaultSynthesizerOptions()
    {
        return new EditorSynthesizerOptions(SynthesisType.CONTROLLABLE, SynthesisAlgorithm.BDD, true, true, true, true, true, false,true,true);
    }

    /**
     * Returns the default options for synthesis.
     */
    public static EditorSynthesizerOptions getDefaultMonolithicCNBSynthesizerOptions()
    {
        EditorSynthesizerOptions options = getDefaultSynthesizerOptions();
		options.synthesisType = SynthesisType.NONBLOCKINGCONTROLLABLE;
        options.synthesisAlgorithm = SynthesisAlgorithm.BDD;
        options.removeUnnecessarySupervisors = false;
        options.reduceSupervisors = false;
        options.generateGuard = true;
        options.reachability = true;

        options.setExpressionType(2);
		options.setEvent("");
        
        return options;
    }

    //Guard options

	public int getExpressionType()
    {
        return expressionType;
    }

    public void setExpressionType(int set)
	{
		expressionType = set;
	}

    public String getEvent()
    {
        return event;
    }

    public void setEvent(String set)
	{
		event = set;
	}


}
