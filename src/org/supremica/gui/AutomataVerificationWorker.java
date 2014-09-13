
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
package org.supremica.gui;

import java.awt.EventQueue;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import net.sourceforge.waters.model.analysis.Abortable;

import org.supremica.automata.Automata;
import org.supremica.automata.algorithms.AutomataVerifier;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.VerificationOptions;
import org.supremica.automata.algorithms.VerificationType;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.gui.ide.IDEReportInterface;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.ActionTimer;

/**
 * Thread dealing with verification.
 *
 * @author  ka
 * @since November 28, 2001
 */
public class AutomataVerificationWorker
    extends Thread
    implements Abortable
{
    private static Logger logger = LoggerFactory.createLogger(AutomataVerificationWorker.class);

    private IDEReportInterface workbench = null;
    private Automata theAutomata = null;
    //private VisualProjectContainer theVisualProjectContainer = null;

    // private String newAutomatonName = null;
    // private Automaton theAutomaton = null;
    private final VerificationOptions verificationOptions;
    private final SynchronizationOptions synchronizationOptions;
    private final MinimizationOptions minimizationOptions;
    private ExecutionDialog executionDialog;
    private boolean abortRequested = false;
    @SuppressWarnings("unused")
	private final EventQueue eventQueue = new EventQueue();

    public AutomataVerificationWorker(final IDEReportInterface workbench, final Automata theAutomata,
        final VerificationOptions verificationOptions,
        final SynchronizationOptions synchronizationOptions,
        final MinimizationOptions minimizationOptions)
    {
        this.workbench = workbench;
        this.theAutomata = theAutomata;
        //theVisualProjectContainer = workbench.getVisualProjectContainer();

        // this.newAutomatonName = newAutomatonName;
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
            JOptionPane.showMessageDialog(workbench.getFrame(), errorMessage,
                "Alert", JOptionPane.ERROR_MESSAGE);
            requestAbort();

            return;
        }

        // Perform verification according to the VerificationType.
        final VerificationType vtype =
          verificationOptions.getVerificationType();
        switch (vtype) {
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
        default:
          // Error... this can't happen!
          requestAbort();
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
            requestAbort();
            JOptionPane.showMessageDialog(workbench.getFrame(), ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            logger.error(ex.getMessage());
            logger.debug(ex.getStackTrace());

            return;
        }

        // Initialize the ExecutionDialog
        final ArrayList<Abortable> threadsToStop = new ArrayList<Abortable>();
        threadsToStop.add(this);
        threadsToStop.add(automataVerifier);
        executionDialog = new ExecutionDialog(workbench.getFrame(), "Verifying", threadsToStop);
        executionDialog.setMode(ExecutionDialogMode.VERIFYING);
        automataVerifier.setExecutionDialog(executionDialog);

        // Solve the problem and measure the time it takes!
        final ActionTimer timer = new ActionTimer();
        timer.start();
        verificationSuccess = automataVerifier.verify();
        timer.stop();

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
                JOptionPane.showMessageDialog(workbench.getFrame(), successMessage, "Good news", JOptionPane.INFORMATION_MESSAGE);
                logger.info(successMessage);
            }
            else
            {
                JOptionPane.showMessageDialog(workbench.getFrame(), failureMessage, "Bad news", JOptionPane.ERROR_MESSAGE);
                logger.info(failureMessage);
            }

            automataVerifier.displayInfo();
            logger.info("Execution completed after " + timer.toString());
        }
        else
        {
            JOptionPane.showMessageDialog(workbench.getFrame(), "Execution stopped after " + timer.toString(), "Execution stopped", JOptionPane.INFORMATION_MESSAGE);
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
    public void requestAbort()
    {
        abortRequested = true;

        logger.debug("AutomataVerificationWorker requested to stop.");

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
