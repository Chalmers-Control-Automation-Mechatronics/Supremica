
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

public final class SynthesizerOptions
{
	private static Logger logger = LoggerFactory.createLogger(SynthesizerOptions.class);

	private boolean dialogOK = false;
	private SynthesisType synthesisType;
	private SynthesisAlgorithm synthesisAlgorithm;
	private boolean purge;
	private boolean optimize;
	private boolean maximallyPermissive;
	private boolean maximallyPermissiveIncremental;
	private boolean reduceSupervisors;
	private boolean rememberDisabledEvents;

	/**
	 * The current options, based on earlier user preferences.
	 */
	public SynthesizerOptions()
	{
		this(SupremicaProperties.synthesisSynthesisType(), SupremicaProperties.synthesisAlgorithmType(), SupremicaProperties.synthesisPurge(), SupremicaProperties.synthesisOptimize(), SupremicaProperties.synthesisMaximallyPermissive(), SupremicaProperties.synthesisMaximallyPermissiveIncremental(), SupremicaProperties.synthesisReduceSupervisors());
	}

	/**
	 * This is not a good constructor so it is private, it is impossible to read in the code.
	 * Use the "getDefault..."-methods in this class instead or when they won't suit you,
	 * modify the necessary options one by one, starting from default! Much more readable and
	 * also more practical when adding new options.
	 */
	private SynthesizerOptions(SynthesisType synthesisType, SynthesisAlgorithm synthesisAlgorithm, boolean purge, boolean optimize, boolean maximallyPermissive, boolean maximallyPermissiveIncremental, boolean reduceSupervisors)
	{
		this.synthesisType = synthesisType;
		this.synthesisAlgorithm = synthesisAlgorithm;
		this.purge = purge;
		this.optimize = optimize;
		this.maximallyPermissive = maximallyPermissive;
		this.maximallyPermissiveIncremental = maximallyPermissiveIncremental;
		this.reduceSupervisors = reduceSupervisors;
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
		if (synthesisType == SynthesisType.Unknown)
		{
			return "Unknown synthesis type.";
		}

		if (synthesisAlgorithm == SynthesisAlgorithm.Unknown)
		{
			return "Unknown synthesis algorithm.";
		}
		else if (synthesisAlgorithm == SynthesisAlgorithm.IDD)
		{
			return "IDD synthesis is not implemented.";
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

	public void setRememberDisabledEvents(boolean remember)
	{
		rememberDisabledEvents = remember;
	}

	public boolean doRememberDisabledEvents()
	{
		return rememberDisabledEvents;
	}

	public void setOptimize(boolean bool)
	{
		optimize = bool;
	}

	public boolean getOptimize()
	{
		return optimize;
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

	/**
	 * Stores the current set of options in SupremicaProperties.
	 */
	public void saveOptions()
	{
		SupremicaProperties.setSynthesisMaximallyPermissiveIncremental(maximallyPermissiveIncremental);
		SupremicaProperties.setSynthesisReduceSupervisors(reduceSupervisors);
		SupremicaProperties.setSynthesisSynthesisType(synthesisType);
		SupremicaProperties.setSynthesisAlgorithmType(synthesisAlgorithm);
		SupremicaProperties.setSynthesisPurge(purge);
		SupremicaProperties.setSynthesisOptimize(optimize);
		SupremicaProperties.setSynthesisMaximallyPermissive(maximallyPermissive);
		SupremicaProperties.setSynthesisMaximallyPermissiveIncremental(maximallyPermissiveIncremental);
	}

	/**
	 * Returns the default options for synthesis.
	 */
	public static SynthesizerOptions getDefaultSynthesizerOptions()
	{
		return new SynthesizerOptions(SynthesisType.Both, SynthesisAlgorithm.Modular, true, true, true, true, true);
	}
}
