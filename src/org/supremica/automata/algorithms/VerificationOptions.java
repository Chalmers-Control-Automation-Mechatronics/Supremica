
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

import org.supremica.gui.WorkbenchProperties;

public final class VerificationOptions
{
	private boolean dialogOK = false;
	private int verificationType;
	private int algorithmType;
	private int exclusionStateLimit;
	private int reachabilityStateLimit;
	private boolean oneEventAtATime;
	private boolean skipUncontrollabilityCheck;

	public VerificationOptions()
	{
		this(WorkbenchProperties.verifyVerificationType(), WorkbenchProperties.verifyAlgorithmType(), WorkbenchProperties.verifyExclusionStateLimit(), WorkbenchProperties.verifyReachabilityStateLimit(), WorkbenchProperties.verifyOneEventAtATime(), WorkbenchProperties.verifySkipUncontrollabilityCheck());
	}

	public VerificationOptions(int verificationType, int algorithmType, int exclusionStateLimit, int reachabilityStateLimit, boolean oneEventAtATime, boolean skipUncontrollabilityCheck)
	{
		this.verificationType = verificationType;
		this.algorithmType = algorithmType;
		this.exclusionStateLimit = exclusionStateLimit;
		this.reachabilityStateLimit = reachabilityStateLimit;
		this.oneEventAtATime = oneEventAtATime;
		this.skipUncontrollabilityCheck = skipUncontrollabilityCheck;
	}

	public void setDialogOK(boolean bool)
	{
		dialogOK = bool;
	}

	public boolean getDialogOK()
	{
		return dialogOK;
	}

	public void setVerificationType(int index)
	{
		verificationType = index;

		WorkbenchProperties.setVerifyVerificationType(index);
	}

	public int getVerificationType()
	{
		return verificationType;
	}

	public void setAlgorithmType(int index)
	{
		algorithmType = index;

		WorkbenchProperties.setVerifyAlgorithmType(index);
	}

	public int getAlgorithmType()
	{
		return algorithmType;
	}

	public void setExclusionStateLimit(int limit)
	{
		exclusionStateLimit = limit;

		WorkbenchProperties.setVerifyExclusionStateLimit(limit);
	}

	public int getExclusionStateLimit()
	{
		return exclusionStateLimit;
	}

	public void setReachabilityStateLimit(int limit)
	{
		reachabilityStateLimit = limit;

		WorkbenchProperties.setVerifyReachabilityStateLimit(limit);
	}

	public int getReachabilityStateLimit()
	{
		return reachabilityStateLimit;
	}

	public void setOneEventAtATime(boolean bool)
	{
		oneEventAtATime = bool;

		WorkbenchProperties.setVerifyOneEventAtATime(bool);
	}

	public boolean getOneEventAtATime()
	{
		return oneEventAtATime;
	}

	public void setSkipUncontrollabilityCheck(boolean bool)
	{
		skipUncontrollabilityCheck = bool;

		WorkbenchProperties.setVerifySkipUncontrollabilityCheck(bool);
	}

	public boolean getSkipUncontrollabilityCheck()
	{
		return skipUncontrollabilityCheck;
	}
}
