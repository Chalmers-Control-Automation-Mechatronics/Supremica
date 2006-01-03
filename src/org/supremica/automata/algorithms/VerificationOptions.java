
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

import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.Automata;

public final class VerificationOptions
{
	private boolean dialogOK = false;

	// Options
	private VerificationType verificationType;
	private VerificationAlgorithm algorithmType;
	private int exclusionStateLimit;
	private int reachabilityStateLimit;
	private boolean oneEventAtATime;
	private boolean skipUncontrollabilityCheck;
	private int nbrOfAttempts;
	private boolean showBadTrace;

	private Automata inclusionAutomata = null;

	/**
	 * The current options, based on earlier user preferences.
	 */
	public VerificationOptions()
	{
		this(SupremicaProperties.verifyVerificationType(), SupremicaProperties.verifyAlgorithmType(), SupremicaProperties.verifyExclusionStateLimit(), SupremicaProperties.verifyReachabilityStateLimit(), SupremicaProperties.verifyOneEventAtATime(), SupremicaProperties.verifySkipUncontrollabilityCheck(), SupremicaProperties.verifyNbrOfAttempts(), SupremicaProperties.verifyShowBadTrace());
	}

	/**
	 * This is not a good constructor so it is private, it is impossible to read in the code.
	 * Use the "getDefault..."-methods in this class instead and, when they won't suit you,
	 * modify the necessary options one by one, starting from default! Much more readable and
	 * also more practical when adding new options.
	 */
	private VerificationOptions(VerificationType verificationType, VerificationAlgorithm algorithmType, int exclusionStateLimit, int reachabilityStateLimit, boolean oneEventAtATime, boolean skipUncontrollabilityCheck, int nbrOfAttempts, boolean showBadTrace)
	{
		this.verificationType = verificationType;
		this.algorithmType = algorithmType;
		this.exclusionStateLimit = exclusionStateLimit;
		this.reachabilityStateLimit = reachabilityStateLimit;
		this.oneEventAtATime = oneEventAtATime;
		this.skipUncontrollabilityCheck = skipUncontrollabilityCheck;
		this.nbrOfAttempts = nbrOfAttempts;
		this.showBadTrace = showBadTrace;
	}

	public void setDialogOK(boolean bool)
	{
		dialogOK = bool;
	}

	public boolean getDialogOK()
	{
		return dialogOK;
	}

	public void setVerificationType(VerificationType type)
	{
		verificationType = type;
	}

	public VerificationType getVerificationType()
	{
		return verificationType;
	}

	public void setAlgorithmType(VerificationAlgorithm algorithm)
	{
		algorithmType = algorithm;
	}

	public VerificationAlgorithm getAlgorithmType()
	{
		return algorithmType;
	}

	public void setExclusionStateLimit(int limit)
	{
		exclusionStateLimit = limit;
	}

	public int getExclusionStateLimit()
	{
		return exclusionStateLimit;
	}

	public void setReachabilityStateLimit(int limit)
	{
		reachabilityStateLimit = limit;
	}

	public int getReachabilityStateLimit()
	{
		return reachabilityStateLimit;
	}

	public void setOneEventAtATime(boolean bool)
	{
		oneEventAtATime = bool;
	}

	public boolean getOneEventAtATime()
	{
		return oneEventAtATime;
	}

	public void setSkipUncontrollabilityCheck(boolean bool)
	{
		skipUncontrollabilityCheck = bool;
	}

	public boolean getSkipUncontrollabilityCheck()
	{
		return skipUncontrollabilityCheck;
	}

	public void setNbrOfAttempts(int nbr)
	{
		nbrOfAttempts = nbr;
	}

	public int getNbrOfAttempts()
	{
		return nbrOfAttempts;
	}

	public void setShowBadTrace(boolean bool)
	{
		showBadTrace = bool;
	}

	public boolean showBadTrace()
	{
		return showBadTrace;
	}

	public void setInclusionAutomata(Automata aut)
	{
		inclusionAutomata = aut;
	}

	public Automata getInclusionAutomata()
	{
		return inclusionAutomata;
	}

	/**
	 * Stores the current set of options in SupremicaProperties.
	 */
	public void saveOptions()
	{
		SupremicaProperties.setVerifyVerificationType(verificationType);
		SupremicaProperties.setVerifyAlgorithmType(algorithmType);
		SupremicaProperties.setVerifyExclusionStateLimit(exclusionStateLimit);
		SupremicaProperties.setVerifyReachabilityStateLimit(reachabilityStateLimit);
		SupremicaProperties.setVerifyOneEventAtATime(oneEventAtATime);
		SupremicaProperties.setVerifySkipUncontrollabilityCheck(skipUncontrollabilityCheck);
		SupremicaProperties.setVerifyNbrOfAttempts(nbrOfAttempts);
		SupremicaProperties.setVerifyShowBadTrace(showBadTrace);
	}

	/**
	 * Returns the default options for controllability verification.
	 */
	public static VerificationOptions getDefaultControllabilityOptions()
	{
		VerificationOptions options = new VerificationOptions();
		options.setVerificationType(VerificationType.Controllability);
		options.setAlgorithmType(VerificationAlgorithm.Modular);
		options.setOneEventAtATime(false);
		options.setSkipUncontrollabilityCheck(false);
		return options;
	}

	/**
	 * Returns the default options for nonblocking verification.
	 */
	public static VerificationOptions getDefaultNonblockingOptions()
	{
		VerificationOptions options = new VerificationOptions();
		options.setVerificationType(VerificationType.Nonblocking);
		options.setAlgorithmType(VerificationAlgorithm.Modular);
		return options;
	}

	/**
	 * Returns the default options for language inclusion verification.
	 */
	public static VerificationOptions getDefaultLanguageInclusionOptions()
	{
		VerificationOptions options = getDefaultControllabilityOptions();
		options.setVerificationType(VerificationType.LanguageInclusion);
		return options;
	}
}
