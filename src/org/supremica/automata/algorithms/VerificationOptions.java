
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

import org.supremica.automata.Automata;
import org.supremica.properties.Config;

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
       this(Config.VERIFY_VERIFICATION_TYPE.get(),
           Config.VERIFY_ALGORITHM_TYPE.get(),
           Config.VERIFY_EXCLUSION_STATE_LIMIT.get(),
           Config.VERIFY_REACHABILITY_STATE_LIMIT.get(),
           Config.VERIFY_ONE_EVENT_AT_A_TIME.get(),
           Config.VERIFY_SKIP_UNCONTROLLABILITY_CHECK.get(),
           Config.VERIFY_NBR_OF_ATTEMPTS.get(),
           Config.VERIFY_SHOW_BAD_TRACE.get());
    }

    /**
     * This is not a good constructor so it is private, it is
     * impossible to read in the code.  Use the "getDefault..."-
     * methods in this class instead and, when they won't suit you,
     * modify the necessary options one by one, starting from default
     * (see below)! Much more readable and also more practical when
     * adding new options.
     */
    private VerificationOptions(final VerificationType verificationType, final VerificationAlgorithm algorithmType, final int exclusionStateLimit, final int reachabilityStateLimit, final boolean oneEventAtATime, final boolean skipUncontrollabilityCheck, final int nbrOfAttempts, final boolean showBadTrace)
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

    public void setDialogOK(final boolean bool)
    {
        dialogOK = bool;
    }

    public boolean getDialogOK()
    {
        return dialogOK;
    }

    public void setVerificationType(final VerificationType type)
    {
        verificationType = type;
    }

    public VerificationType getVerificationType()
    {
        return verificationType;
    }

    public void setAlgorithmType(final VerificationAlgorithm algorithm)
    {
        algorithmType = algorithm;
    }

    public VerificationAlgorithm getAlgorithmType()
    {
        return algorithmType;
    }

    public void setExclusionStateLimit(final int limit)
    {
        exclusionStateLimit = limit;
    }

    public int getExclusionStateLimit()
    {
        return exclusionStateLimit;
    }

    public void setReachabilityStateLimit(final int limit)
    {
        reachabilityStateLimit = limit;
    }

    public int getReachabilityStateLimit()
    {
        return reachabilityStateLimit;
    }

    public void setOneEventAtATime(final boolean bool)
    {
        oneEventAtATime = bool;
    }

    public boolean getOneEventAtATime()
    {
        return oneEventAtATime;
    }

    public void setSkipUncontrollabilityCheck(final boolean bool)
    {
        skipUncontrollabilityCheck = bool;
    }

    public boolean getSkipUncontrollabilityCheck()
    {
        return skipUncontrollabilityCheck;
    }

    public void setNbrOfAttempts(final int nbr)
    {
        nbrOfAttempts = nbr;
    }

    public int getNbrOfAttempts()
    {
        return nbrOfAttempts;
    }

    public void setShowBadTrace(final boolean bool)
    {
        showBadTrace = bool;
    }

    public boolean showBadTrace()
    {
        return showBadTrace;
    }

    public void setInclusionAutomata(final Automata aut)
    {
        inclusionAutomata = aut;
    }

    public Automata getInclusionAutomata()
    {
        return inclusionAutomata;
    }

    /**
     * Stores the current set of options in Config.
     */
    public void saveOptions()
    {
        Config.VERIFY_VERIFICATION_TYPE.setValue(verificationType);
        Config.VERIFY_ALGORITHM_TYPE.setValue(algorithmType);
        Config.VERIFY_EXCLUSION_STATE_LIMIT.set(exclusionStateLimit);
        Config.VERIFY_REACHABILITY_STATE_LIMIT.set(reachabilityStateLimit);
        Config.VERIFY_ONE_EVENT_AT_A_TIME.set(oneEventAtATime);
        Config.VERIFY_SKIP_UNCONTROLLABILITY_CHECK.set(skipUncontrollabilityCheck);
        Config.VERIFY_NBR_OF_ATTEMPTS.set(nbrOfAttempts);
        Config.VERIFY_SHOW_BAD_TRACE.set(showBadTrace);
    }

    /**
     * Returns the default options for controllability verification.
     */
    public static VerificationOptions getDefaultControllabilityOptions()
    {
        final VerificationOptions options = new VerificationOptions();
        options.setVerificationType(VerificationType.CONTROLLABILITY);
        options.setAlgorithmType(VerificationAlgorithm.MODULAR);
        options.setOneEventAtATime(false);
        options.setSkipUncontrollabilityCheck(false);
        return options;
    }

    /**
     * Returns the default options for nonblocking verification.
     */
    public static VerificationOptions getDefaultNonblockingOptions()
    {
        final VerificationOptions options = new VerificationOptions();
        options.setVerificationType(VerificationType.NONBLOCKING);
        options.setAlgorithmType(VerificationAlgorithm.COMPOSITIONAL);
        return options;
    }

    /**
     * Returns the default options for language inclusion verification.
     */
    public static VerificationOptions getDefaultLanguageInclusionOptions()
    {
        final VerificationOptions options = getDefaultControllabilityOptions();
        options.setVerificationType(VerificationType.LANGUAGEINCLUSION);
        return options;
    }

}
