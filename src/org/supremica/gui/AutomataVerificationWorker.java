/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

import org.supremica.log.*;
import org.supremica.automata.algorithms.*;

import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.VisualProjectContainer;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.EventsSet;
import org.supremica.automata.LabeledEvent;
import org.supremica.util.ActionTimer;

/**
 * Thread dealing with verification.
 *
 *@author  ka
 *@created  November 28, 2001
 */
public class AutomataVerificationWorker
	extends Thread
	implements Stoppable
{
	private static Logger logger = LoggerFactory.createLogger(AutomataVerificationWorker.class);
	// -- MF --      private Supremica workbench = null;
	private Gui workbench = null;
	private Automata theAutomata = null;
	private VisualProjectContainer theVisualProjectContainer = null;

	// private String newAutomatonName = null;
	// private Automaton theAutomaton = null;
	private SynchronizationOptions synchronizationOptions;
	private VerificationOptions verificationOptions;
	private ExecutionDialog executionDialog;
	private boolean stopRequested = false;
	private EventQueue eventQueue = new EventQueue();

	// Make sure these match what's defined in VerificationDialogStandardPanel
	private static final int MONOLITHIC = 0;
	private static final int MODULAR = 1;
	private static final int IDD = 2;

	public AutomataVerificationWorker(Gui workbench, Automata theAutomata, SynchronizationOptions synchronizationOptions, VerificationOptions verificationOptions)
	{
		this.workbench = workbench;
		this.theAutomata = theAutomata;
		theVisualProjectContainer = workbench.getVisualProjectContainer();

		// this.newAutomatonName = newAutomatonName;
		this.synchronizationOptions = synchronizationOptions;
		this.verificationOptions = verificationOptions;

		this.start();
	}

	public void run()
	{
		final AutomataVerifier automataVerifier;
		Date startDate;
		Date endDate;

		boolean verificationSuccess;
		String successMessage;
		String failureMessage;

		// Initialize the ExecutionDialog
		final ArrayList threadsToStop = new ArrayList();
		threadsToStop.add(this);
		eventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				executionDialog = new ExecutionDialog(workbench.getFrame(), "Verifying", threadsToStop);
				executionDialog.setMode(ExecutionDialogMode.hide);
			}
		});

		// Examine the validity of the chosen options
		String errorMessage = AutomataVerifier.validOptions(theAutomata, verificationOptions);
		if (errorMessage != null)
		{
			JOptionPane.showMessageDialog(workbench.getFrame(), errorMessage, "Alert", JOptionPane.ERROR_MESSAGE);
			requestStop();
			return;
		}

		// Perform verification according to the VerificationType.
		if (verificationOptions.getVerificationType() == VerificationType.Controllability)
		{
			// Controllability verification...
			successMessage = "The system is controllable!";
			failureMessage = "The system is NOT controllable!";
		}
		else if (verificationOptions.getVerificationType() == VerificationType.Nonblocking)
		{
			// Non-blocking verification...
			successMessage = "The system is non-blocking!";
			failureMessage = "The system is blocking!";
		}
		else if (verificationOptions.getVerificationType() == VerificationType.LanguageInclusion)
		{
			// Language inclusion verification...
			successMessage = "The language of the unselected automata is \n" +
				             "included in the language of the selected automata.";
			failureMessage = "The language of the unselected automata is \n" +
				             "NOT included in the language of the selected automata.";

			// In language inclusion, not only the currently selected automata are used!
			theAutomata = workbench.getVisualProjectContainer().getActiveProject();

			// Reservation!!
			JOptionPane.showMessageDialog(workbench.getFrame(),
										  "Note that the language inclusion check is guaranteed to \n" +
										  "work only when the alphabets of the respective automata \n" +
										  "sets are equal. Also know that this is prefix-closed \n" +
										  "language verification only.", "Important information!",
										  JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			// Error... this can't happen!
			requestStop();
			logger.error("Unavailable option chosen... this can't happen...\n" +
						 "don't ever do whatever you just did again, please.");
			return;
		}

		// Did some initialization go wrong?
		if (stopRequested)
		{
			return;
		}

		// Initialize the AutomataVerifier
		try
		{
			automataVerifier = new AutomataVerifier(theAutomata, synchronizationOptions, verificationOptions);
			eventQueue.invokeLater(new Runnable()
				{
					public void run()
					{
						executionDialog.setMode(ExecutionDialogMode.verifying);
						automataVerifier.getHelper().setExecutionDialog(executionDialog);
					}
				});
			threadsToStop.add(automataVerifier);
		}
		catch (Exception ex)
		{
			requestStop();
			JOptionPane.showMessageDialog(workbench.getFrame(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			logger.error(ex.getMessage());
			logger.debug(ex.getStackTrace());
			return;
		}

		// Are further preparations needed?
	    if (verificationOptions.getVerificationType() == VerificationType.LanguageInclusion)
		{
			// Treat the unselected automata as plants (and the rest as supervisors, implicitly)
			automataVerifier.prepareForLanguageInclusion(workbench.getUnselectedAutomata());
		}

		// Solve the problem!
		// startDate = new Date();
		ActionTimer timer = new ActionTimer();
		timer.start();
		verificationSuccess = automataVerifier.verify();
		// endDate = new Date();
		timer.stop();

		// Present the result
		if (!stopRequested)
		{
			if (verificationSuccess)
			{
				JOptionPane.showMessageDialog(workbench.getFrame(), successMessage, "Good news", JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(workbench.getFrame(), failureMessage, "Bad news", JOptionPane.INFORMATION_MESSAGE);
			}
			automataVerifier.getHelper().displayInfo();
			// logger.info("Execution completed after " + (endDate.getTime()-startDate.getTime())/1000.0 + " seconds.");
			logger.info("Execution completed after " + timer.toString());
		}
		else
		{
			automataVerifier.getHelper().displayInfo();
			// logger.info("Execution stopped after " + (endDate.getTime()-startDate.getTime())/1000.0 + " seconds.");
			logger.info("Execution stopped after " + timer.toString());
		}

		// We're finished! Bail out! Make sure to kill the ExecutionDialog!
		if (executionDialog != null)
		{
			executionDialog.setMode(ExecutionDialogMode.hide);
		}
	}

	/**
	 * Method called from external class stopping AutomataVerifier as soon as possible.
	 *
	 *@see  ExecutionDialog
	 */
	public void requestStop()
	{
		logger.debug("AutomataVerificationWorker requested to stop.");
		if (executionDialog != null)
		{
			executionDialog.setMode(ExecutionDialogMode.hide);
		}

		stopRequested = true;
	}
}
