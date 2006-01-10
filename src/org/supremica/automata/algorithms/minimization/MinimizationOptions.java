
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
package org.supremica.automata.algorithms.minimization;

import org.supremica.properties.SupremicaProperties;
import org.supremica.log.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.algorithms.EquivalenceRelation;

public final class MinimizationOptions
{
	private static Logger logger = LoggerFactory.createLogger(MinimizationOptions.class);

	private boolean dialogOK = false;
	/**
	 * The equivalence relation that the minimization should be performed with respect to.
	 */
	private EquivalenceRelation equivalenceRelation;
	/**
	 * When set, the number of transitions is minimized as well.
	 */
	private boolean alsoTransitions;
	/**
	 * When set, the automata that are supplied to the minimization are kept and
	 * the (destructive) minimization is performed on a copy instead.
	 */
	private boolean keepOriginal;
	/**
	 * With this option set, marking is ignored, i.e. marked and nonmarked states are
	 * not considered any different.
	 */
	private boolean ignoreMarking;
	/**
	 * When skipLast is set, in compositional minimization, the last minimization step
	 * is skipped and a non-minimized automaton is returned. This is good for for example
	 * for verification of nonblocking when the minimal result is not really of interest,
	 * only the nonblocking status of the end result.
	 */
	private boolean skipLast;
	/**
	 * If many automata are present, when this option is set, they are composed and minimized
	 * rather than being minimized individually.
	 */
	private boolean compositionalMinimization;
	/**
	 * Which strategy should be used for selecting the next step in the compositional minimization?
	 */
	private MinimizationStrategy minimizationStrategy;
	/**
	 * Which heuristic should be used.
	 */
	private MinimizationHeuristic minimizationHeuristic;
	/**
	 * The target alphabet, the events that are not in this alphabet will be hidden in the
	 * final result.
	 */
	private Alphabet targetAlphabet;

	/** Use conflict equivalence rule A? */
	private boolean useRuleA = true;
	/** Use conflict equivalence rule AA? */
	private boolean useRuleAA = true;
	/** Use conflict equivalence rule B? */
	private boolean useRuleB = true;
	/** Use conflict equivalence rule F? */
	private boolean useRuleF = true;

	/**
	 * This constructor returns the options previously chosen by the user as per the
	 * current state of SupremicaProperties.
	 */
	public MinimizationOptions()
	{
		this(SupremicaProperties.minimizationMinimizationType(),
			 SupremicaProperties.minimizationAlsoTransitions(),
			 SupremicaProperties.minimizationKeepOriginal(),
			 SupremicaProperties.minimizationIgnoreMarking(),
			 SupremicaProperties.minimizationStrategy(),
			 SupremicaProperties.minimizationHeuristic());
	}

	/**
	 * This constructor lets you choose exactly what options you want. This is not recommended and is
	 * therefore private. It is better to first use the "getDefault..."-methods and then
	 * modify the options that you want to change.
	 */
	private MinimizationOptions(EquivalenceRelation equivalenceRelation, boolean alsoTransitions,
								boolean keepOriginal, boolean ignoreMarking, MinimizationStrategy strategy,
								MinimizationHeuristic heuristic)
	{
		this.equivalenceRelation = equivalenceRelation;
		this.alsoTransitions = alsoTransitions;
		this.keepOriginal = keepOriginal;
		this.ignoreMarking = ignoreMarking;
		this.minimizationStrategy = strategy;
		this.minimizationHeuristic = heuristic;
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
		if (equivalenceRelation == EquivalenceRelation.ConflictEquivalence)
		{
			if (ignoreMarking)
			{
				String message = "Invalid minimization options chosen. Conflict equivalence " +
					"implies that the marking must not be ignored.";
				return message;
			}
		}

		if (compositionalMinimization)
		{
			if (targetAlphabet == null)
			{
				String message = "Null target alphabet selected for compositional minimization.";
				return message;
			}
		}

		if (targetAlphabet != null && targetAlphabet.nbrOfEpsilonEvents() != 0)
		{
			String message = "There should not be epsilon events in the target alphabet.";  // (But it's not dangerous or anything...)
			return message;
		}

		if (minimizationStrategy != MinimizationStrategy.AtLeastOneLocal && 
			(minimizationHeuristic == MinimizationHeuristic.FewestAutomata || 
			 minimizationHeuristic == MinimizationHeuristic.MostAutomata))
		{
			String message = "Inapropriate choice of minimization heuristic.";
			return message;
		}

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

	public void setMinimizationType(EquivalenceRelation rel)
	{
		equivalenceRelation = rel;
	}
	public EquivalenceRelation getMinimizationType()
	{
		return equivalenceRelation;
	}

	public void setAlsoTransitions(boolean bool)
	{
		alsoTransitions = bool;
	}
	public boolean getAlsoTransitions()
	{
		return alsoTransitions;
	}

	public void setKeepOriginal(boolean bool)
	{
		keepOriginal = bool;
	}
	public boolean getKeepOriginal()
	{
		return keepOriginal;
	}

	public void setIgnoreMarking(boolean bool)
	{
		ignoreMarking = bool;
	}
	public boolean getIgnoreMarking()
	{
		return ignoreMarking;
	}

	public void setCompositionalMinimization(boolean bool)
	{
		compositionalMinimization = bool;
	}
	public boolean getCompositionalMinimization()
	{
		return compositionalMinimization;
	}

	public void setMinimizationStrategy(MinimizationStrategy strategy)
	{
		minimizationStrategy = strategy;
	}
	public MinimizationStrategy getMinimizationStrategy()
	{
		return minimizationStrategy;
	}

	public void setMinimizationHeuristic(MinimizationHeuristic heuristic)
	{
		minimizationHeuristic = heuristic;
	}
	public MinimizationHeuristic getMinimizationHeuristic()
	{
		return minimizationHeuristic;
	}

	public void setSkipLast(boolean bool)
	{
		skipLast = bool;
	}
	public boolean getSkipLast()
	{
		return skipLast;
	}

	public void setTargetAlphabet(Alphabet alpha)
	{
		targetAlphabet = alpha;
	}
	public Alphabet getTargetAlphabet()
	{
		return targetAlphabet;
	}

	public void setUseRuleA(boolean bool)
	{
		useRuleA = bool;
	}
	public boolean getUseRuleA()
	{
		return useRuleA;
	}
	public void setUseRuleAA(boolean bool)
	{
		useRuleAA = bool;
	}
	public boolean getUseRuleAA()
	{
		return useRuleAA;
	}
	public void setUseRuleB(boolean bool)
	{
		useRuleB = bool;
	}
	public boolean getUseRuleB()
	{
		return useRuleB;
	}
	public void setUseRuleF(boolean bool)
	{
		useRuleF = bool;
	}
	public boolean getUseRuleF()
	{
		return useRuleF;
	}

	/**
	 * Stores the current set of options in SupremicaProperties.
	 */
	public void saveOptions()
	{
		SupremicaProperties.setMinimizationMinimizationType(equivalenceRelation);
		SupremicaProperties.setMinimizationAlsoTransitions(alsoTransitions);
		SupremicaProperties.setMinimizationKeepOriginal(keepOriginal);
		SupremicaProperties.setMinimizationIgnoreMarking(ignoreMarking);
		SupremicaProperties.setMinimizationStrategy(minimizationStrategy);
		SupremicaProperties.setMinimizationHeuristic(minimizationHeuristic);
	}

	/**
	 * Returns the default options for minimization - this is with respect to observation equivalence.
	 */
	public static MinimizationOptions getDefaultMinimizationOptions()
	{
		MinimizationOptions options = new MinimizationOptions();
		options.setMinimizationType(EquivalenceRelation.LanguageEquivalence);
		options.setAlsoTransitions(true);
		options.setKeepOriginal(true);
		options.setIgnoreMarking(false);
		return options;
	}

	/**
	 * Returns the default options for nonblocking verification - this is with respect to conflict equivalence.
	 */
	public static MinimizationOptions getDefaultNonblockingOptions()
	{
		MinimizationOptions options = MinimizationOptions.getDefaultMinimizationOptions();
		options.setMinimizationType(EquivalenceRelation.ConflictEquivalence);
		options.setAlsoTransitions(false);
		options.setKeepOriginal(false);
		options.setCompositionalMinimization(true);
		options.setSkipLast(true);
		options.setTargetAlphabet(new Alphabet());
		//options.setMinimizationStrategy(MinimizationStrategy.FewestTransitionsFirst);
		//options.setMinimizationHeuristic(MinimizationHeuristic.MostLocal);
		return options;
	}

	/**
	 * Returns the default options for verification, presumably nonblocking?
	 */
	public static MinimizationOptions getDefaultVerificationOptions()
	{
		return getDefaultNonblockingOptions();
	}
}
