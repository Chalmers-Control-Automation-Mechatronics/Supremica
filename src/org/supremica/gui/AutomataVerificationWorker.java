//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2023 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.awt.EventQueue;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sourceforge.waters.model.analysis.AbortRequester;
import net.sourceforge.waters.model.analysis.Abortable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.algorithms.AutomataVerifier;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.VerificationAlgorithm;
import org.supremica.automata.algorithms.VerificationOptions;
import org.supremica.automata.algorithms.VerificationType;
import org.supremica.automata.algorithms.bbsd.BBSDDiagnosabilityVerification;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.gui.ide.IDE;
import org.supremica.util.ActionTimer;

/**
 * Thread dealing with verification.
 *
 * @author Knut &Aring;kesson
 * @since November 28, 2001
 *
 * MF fix issue #138 (Feb 2022), add log of the used strategy and heuristics
 */
public class AutomataVerificationWorker
    extends Thread
    implements Abortable
{
    private static final Logger logger = LogManager.getLogger(AutomataVerificationWorker.class);

    private Automata theAutomata = null;

    private final JFrame mOwner;
    private final VerificationOptions verificationOptions;
    private final SynchronizationOptions synchronizationOptions;
    private final MinimizationOptions minimizationOptions;
    private ExecutionDialog executionDialog;
    private boolean abortRequested = false;
    @SuppressWarnings("unused")
	private final EventQueue eventQueue = new EventQueue();

    public AutomataVerificationWorker(final JFrame owner,
                                      final Automata theAutomata,
                                      final VerificationOptions verificationOptions,
                                      final SynchronizationOptions synchronizationOptions,
                                      final MinimizationOptions minimizationOptions)
    {
        mOwner = owner;
        this.theAutomata = theAutomata;
        this.verificationOptions = verificationOptions;
        this.synchronizationOptions = synchronizationOptions;
        this.minimizationOptions = minimizationOptions;

        this.start();
    }

    @Override
    public void run()
    {
        final AutomataVerifier automataVerifier;
        boolean verificationSuccess;
        String successMessage;
        String failureMessage;

        // Examine the validity of the chosen options
        final String errorMessage = AutomataVerifier.validOptions(theAutomata, verificationOptions);
        if (errorMessage != null)
        {
            JOptionPane.showMessageDialog(mOwner, errorMessage,
                "Alert", JOptionPane.ERROR_MESSAGE);
            requestAbort(AbortRequester.USER);
            return;
        }

        // Perform verification according to the VerificationType.
        final VerificationType vtype =
          verificationOptions.getVerificationType();
        switch (vtype)
		{
			case CONTROLLABILITY:
			case INVERSECONTROLLABILITY:
				// Controllability verification...
				successMessage = "The system is controllable!";
				failureMessage = "The system is NOT controllable!";
				break;
			case CONTROLLABILITYNONBLOCKING:
				// Controllability + nonblocking verification...
				successMessage = "The system is controllable and nonblocking!";
				failureMessage = "The system is uncontrollable or blocking!";
				break;
			case NONBLOCKING:
				// Nonblocking verification...
				successMessage = "The system is nonblocking!";
				failureMessage = "The system is blocking!";
				break;
			case LANGUAGEINCLUSION:
				// Language inclusion verification...
				successMessage = "The language of the unselected automata is \n" +
								 "included in the language of the selected automata.";
				failureMessage = "The language of the unselected automata is NOT\n" +
								 "included in the language of the selected automata.";
				// In language inclusion, not only the currently selected automata are used!
				//theAutomata = workbench.getAllAutomata(); // They are sent through the options instead
				break;
			case OP:
				// OP-verifier ...
				successMessage = "The observer property is satisfied.";
				failureMessage = "The observer property is NOT satisfied.";
				break;
			case DIAGNOSABILITY:
				// Diagnosability verification ...
				successMessage = "The system is diagnosable.";
				failureMessage = "The system is NOT diagnosable.";
				break;
			default:
				// Error... this can't happen!
				requestAbort(AbortRequester.USER);
				logger.error("Error in AutomataVerificationWorker. Unavailable option chosen... " +
							 "this can't happen.\nPlease send bug report to bugs@supremica.org.");
			  return;
        }

        // Did some initialization go wrong?
        if (abortRequested)
        {
            return;
        }

        // Initialize the AutomataVerifier
        try
        {
            automataVerifier = new AutomataVerifier(theAutomata, verificationOptions,
                synchronizationOptions, minimizationOptions);
        }
        catch (final Exception ex)
        {
            requestAbort(AbortRequester.USER);
            JOptionPane.showMessageDialog(mOwner, ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            logger.error(ex.getMessage());
            logger.debug(ex.getStackTrace());

            return;
        }

        // Initialize the ExecutionDialog
        final ArrayList<Abortable> threadsToStop = new ArrayList<Abortable>();
        threadsToStop.add(this);
        threadsToStop.add(automataVerifier);
        executionDialog = new ExecutionDialog(mOwner, "Verifying", threadsToStop);
        executionDialog.setMode(ExecutionDialogMode.VERIFYING);
        automataVerifier.setExecutionDialog(executionDialog);

        // Solve the problem and measure the time it takes!
        final ActionTimer timer = new ActionTimer();
        verificationSuccess = false;
        try {
          timer.start();
          verificationSuccess = automataVerifier.verify();
          timer.stop();
        }
        catch (final Exception ex)
        {
          this.requestAbort(AbortRequester.USER);
        }
        // Add test result from Diagnosability verification
        if (vtype == VerificationType.DIAGNOSABILITY &&
            verificationOptions.getAlgorithmType() == VerificationAlgorithm.BBSD)
		{
            final Automata result = BBSDDiagnosabilityVerification.getFinalAutomata();
            if (result != null && mOwner instanceof IDE) {
              final IDE ide = (IDE) mOwner;
              ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().addAutomata(result);
            }
        }

        threadsToStop.clear();

        // Make sure(?) the ExecutionDialog is hidden!
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (executionDialog != null)
                {
                    executionDialog.setMode(ExecutionDialogMode.HIDE);
                }
            }
        });

        // Present the result
        if (!abortRequested)
        {
            // Show message dialog with result
            if (verificationSuccess)
            {
                JOptionPane.showMessageDialog(mOwner, successMessage, "Good news", JOptionPane.INFORMATION_MESSAGE);
                logger.info(successMessage);
            }
            else
            {
                JOptionPane.showMessageDialog(mOwner, failureMessage, "Bad news", JOptionPane.ERROR_MESSAGE);
                logger.info(failureMessage);
            }

            automataVerifier.displayInfo();
            logger.info("Verification completed after " + timer.toString());
            // MF fix issue #138, add log of the used strategy and heuristics
            logger.info("Heuristsics: " + minimizationOptions.getMinimizationStrategy().toStringAbbreviated() + " / " + minimizationOptions.getMinimizationHeuristic().toStringAbbreviated());
            logger.debug("Spurious debug message, does it show?");
        }
        else
        {
            JOptionPane.showMessageDialog(mOwner, "Execution stopped after " + timer.toString(), "Execution stopped", JOptionPane.INFORMATION_MESSAGE);
            automataVerifier.displayInfo();
            logger.info("Execution stopped after " + timer.toString());
        }

        // We're finished! Bail out! Make sure to kill the ExecutionDialog!
        if (executionDialog != null)
        {
            executionDialog.setMode(ExecutionDialogMode.HIDE);
            executionDialog = null;
        }
    }

    /**
     * Method that stops AutomataVerificationWorker as soon as possible.
     *
     *@see  ExecutionDialog
     */
    @Override
    public void requestAbort(final AbortRequester sender)
    {
        abortRequested = true;

        logger.debug("AutomataVerificationWorker is requested to stop.");

        if (executionDialog != null)
        {
            executionDialog.setMode(ExecutionDialogMode.HIDE);
        }
    }

    @Override
    public boolean isAborting()
    {
        return abortRequested;
    }

    @Override
    public void resetAbort(){
      abortRequested = false;
    }

}
