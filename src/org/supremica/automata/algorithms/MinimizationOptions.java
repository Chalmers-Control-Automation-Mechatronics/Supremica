
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

import org.supremica.properties.SupremicaProperties;
import org.supremica.log.*;
import org.supremica.automata.Alphabet;

public final class MinimizationOptions
{
	private static Logger logger = LoggerFactory.createLogger(MinimizationOptions.class);

	private boolean dialogOK = false;
	private EquivalenceRelation equivalenceRelation;
	private boolean alsoTransitions;
	private boolean keepOriginal;
	private boolean ignoreMarking;
	private boolean compositionalMinimization;
	private Alphabet targetAlphabet;

	/**
	 * This constructor returns the options previously chosen by the user as per the state SupremicaProperties.
	 */
	public MinimizationOptions()
	{
		this(SupremicaProperties.minimizationMinimizationType(),
			 SupremicaProperties.minimizationAlsoTransitions(),
			 SupremicaProperties.minimizationKeepOriginal(),
			 SupremicaProperties.minimizationIgnoreMarking());
	}

	/**
	 * This constructor lets you choose exactly what options you want. This is not recommended and is
	 * therefore private. It is better to first use the "getDefault..."-methods and then
	 * modify the options you want (perhaps all of them).
	 */
	private MinimizationOptions(EquivalenceRelation equivalenceRelation, boolean alsoTransitions,
							   boolean keepOriginal, boolean ignoreMarking)
	{
		this.equivalenceRelation = equivalenceRelation;
		this.alsoTransitions = alsoTransitions;
		this.keepOriginal = keepOriginal;
		this.ignoreMarking = ignoreMarking;
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

	public void setTargetAlphabet(Alphabet alpha)
	{
		targetAlphabet = alpha;
	}

	public Alphabet getTargetAlphabet()
	{
		return targetAlphabet;
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
}
