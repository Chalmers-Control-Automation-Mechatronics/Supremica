
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
package org.supremica.gui;



import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;

// import org.apache.log4j.*;
import org.supremica.gui.Gui;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;

import java.util.*;


/**
 * Thread dealing with verification.
 */
public class AutomataVerificationWorker
	extends Thread
	implements Stoppable
{

	// private static Category thisCategory = LogDisplay.createCategory(AutomataVerificationWorker.class.getName());
	// -- MF --      private Supremica workbench = null;
	private Gui workbench = null;
	private Automata theAutomata = null;
	private AutomatonContainer theAutomatonContainer = null;

	// private String newAutomatonName = null;
	// private Automaton theAutomaton = null;
	private SynchronizationOptions synchronizationOptions;
	private VerificationOptions verificationOptions;
	private ExecutionDialog executionDialog;
	private boolean stopRequested = false;
	private EventQueue eventQueue = new EventQueue();

	public AutomataVerificationWorker(	/* Supremica workbench, */Gui workbench, Automata theAutomata, SynchronizationOptions synchronizationOptions, VerificationOptions verificationOptions)
	{

		this.workbench = workbench;
		this.theAutomata = theAutomata;
		theAutomatonContainer = workbench.getAutomatonContainer();

		// this.newAutomatonName = newAutomatonName;
		this.synchronizationOptions = synchronizationOptions;
		this.verificationOptions = verificationOptions;

		this.start();
	}

	public void run()
	{

		Date startDate;
		Date endDate;
		final AutomataVerifier automataVerifier;

		// Cancel dialog initialization...
		final ArrayList threadsToStop = new ArrayList();

		threadsToStop.add(this);
		eventQueue.invokeLater(new Runnable()
		{

			public void run()
			{

				executionDialog = new ExecutionDialog(workbench, "Verifying", threadsToStop);

				executionDialog.setMode(ExecutionDialogMode.verifying);
			}
		});

		if (verificationOptions.getVerificationType() == 0)
		{		// Controllability verification...
			boolean isControllable;

			if (theAutomata.size() < 2)
			{
				JOptionPane.showMessageDialog(workbench.getFrame(), "At least two automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
				requestStop();

				return;
			}

			try
			{
				automataVerifier = new AutomataVerifier(theAutomata, synchronizationOptions, verificationOptions);

				eventQueue.invokeLater(new Runnable()
				{

					public void run()
					{
						automataVerifier.getHelper().setExecutionDialog(executionDialog);
					}
				});

				// automataVerifier.getHelper().setExecutionDialog(executionDialog);
				threadsToStop.add(automataVerifier);
			}
			catch (Exception e)
			{
				requestStop();
				JOptionPane.showMessageDialog(workbench.getFrame(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

				// thisCategory.error(e.getMessage());
				workbench.error(e.getMessage());

				return;
			}

			startDate = new Date();

			try
			{
				if (verificationOptions.getAlgorithmType() == 0)
				{		// Modular...
					isControllable = automataVerifier.modularControllabilityVerification();
				}
				else if (verificationOptions.getAlgorithmType() == 1)
				{		// Monolithic...
					isControllable = automataVerifier.monolithicControllabilityVerification();
				}
				else if (verificationOptions.getAlgorithmType() == 2)
				{		// IDD...
					requestStop();

					// thisCategory.error("Option not implemented...");
					workbench.error("Option not implemented...");

					return;
				}
				else
				{		// Error...
					requestStop();

					// thisCategory.error("Unavailable option chosen.");
					workbench.error("Unavailable option chosen.");

					return;
				}
			}
			catch (Exception e)
			{
				requestStop();

				// thisCategory.error("Error in AutomataVerificationWorker when verifying automata. " + e);
				workbench.error("Error in AutomataVerificationWorker when verifying automata. " + e);

				return;
			}

			endDate = new Date();

			// Present result...
			if (!stopRequested)
			{
				if (isControllable)
				{
					JOptionPane.showMessageDialog(workbench.getFrame(), "The system is controllable!", "Good news", JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					JOptionPane.showMessageDialog(workbench.getFrame(), "The system is NOT controllable!", "Bad news", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
		else if (verificationOptions.getVerificationType() == 1)
		{				// Non-blocking verification...
			requestStop();

			// thisCategory.error("Option not implemented...");
			workbench.error("Option not implemented...");

			return;
		}
		else if (verificationOptions.getVerificationType() == 2)
		{				// Language inclusion
			boolean isIncluded;
			Collection selectedAutomata = workbench.getSelectedAutomataAsCollection();
			Automata automataA = new Automata();
			Automata automataB = new Automata();
			Automaton currAutomaton;
			String currAutomatonName;

			// The automata must have an initial state
			// Put selected automata in automataB and unselected in automataA
			for (int i = 0; i < theAutomatonContainer.getSize(); i++)
			{
				try
				{
					currAutomaton = theAutomatonContainer.getAutomatonAt(i);
				}
				catch (Exception ex)
				{
					requestStop();

					// thisCategory.error("Exception in AutomatonContainer.");
					workbench.error("Exception in AutomatonContainer.");

					return;
				}

				currAutomatonName = currAutomaton.getName();

				if (currAutomaton.getInitialState() == null)
				{
					requestStop();
					JOptionPane.showMessageDialog(workbench.getFrame(), "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);

					return;
				}

				if (selectedAutomata.contains(currAutomaton))
				{
					automataB.addAutomaton(new Automaton(currAutomaton));
				}
				else
				{
					automataA.addAutomaton(new Automaton(currAutomaton));
				}
			}

			if ((automataA.size() < 1) || (automataB.size() < 1))
			{

				// thisCategory.error("At least one automaton must be unselected.");
				workbench.error("At least one automaton must be unselected.");
				requestStop();

				return;
			}

			// Compute the union alphabet of the events in automataA, mark all
			// events in automataA as uncontrollable and the automata as plants
			EventsSet theAlphabets = new EventsSet();
			Alphabet unionAlphabet;
			Iterator automatonIteratorA = automataA.iterator();
			Iterator eventIteratorA;
			Iterator eventIteratorB;

			while (automatonIteratorA.hasNext())
			{
				currAutomaton = (Automaton) automatonIteratorA.next();

				Alphabet currAlphabet = currAutomaton.getAlphabet();

				theAlphabets.add(currAlphabet);
				currAutomaton.setType(AutomatonType.Plant);

				eventIteratorA = currAutomaton.eventIterator();

				while (eventIteratorA.hasNext())
				{
					((org.supremica.automata.Event) eventIteratorA.next()).setControllable(false);
				}
			}

			if (theAlphabets.size() == 1)
			{
				unionAlphabet = (Alphabet) theAlphabets.get(0);
			}
			else
			{
				try
				{
					unionAlphabet = AlphabetHelpers.getUnionAlphabet(theAlphabets, "");
				}
				catch (Exception e)
				{
					requestStop();

					// thisCategory.error("Error when calculating union alphabet. " + e);
					workbench.error("Error when calculating union alphabet. " + e);

					return;
				}
			}

			// Change events in the automata in automataB to uncontrollable if they
			// are included in the union alphabet found above, mark the automata as
			// specifications
			Iterator automatonIteratorB = automataB.iterator();
			org.supremica.automata.Event currEvent;

			while (automatonIteratorB.hasNext())
			{
				currAutomaton = (Automaton) automatonIteratorB.next();

				currAutomaton.setType(AutomatonType.Supervisor);

				eventIteratorB = currAutomaton.eventIterator();

				while (eventIteratorB.hasNext())
				{
					currEvent = (org.supremica.automata.Event) eventIteratorB.next();

					if (unionAlphabet.containsEventWithLabel(currEvent.getLabel()))
					{
						currEvent.setControllable(false);
					}
					else
					{
						currEvent.setControllable(true);
					}
				}
			}

			// After the above preparations, the language inclusion check
			// can be performed as a controllability check...
			automataA.addAutomata(automataB);

			try
			{
				automataVerifier = new AutomataVerifier(automataA, synchronizationOptions, verificationOptions);

				eventQueue.invokeLater(new Runnable()
				{

					public void run()
					{
						automataVerifier.getHelper().setExecutionDialog(executionDialog);
					}
				});

				// automataVerifier.getHelper().setExecutionDialog(executionDialog);
				threadsToStop.add(automataVerifier);
			}
			catch (Exception e)
			{
				requestStop();
				JOptionPane.showMessageDialog(workbench.getFrame(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

				// thisCategory.error(e.getMessage());
				workbench.error(e.getMessage());

				return;
			}

			startDate = new Date();

			try
			{
				if (verificationOptions.getAlgorithmType() == 0)
				{		// Modular...
					isIncluded = automataVerifier.modularControllabilityVerification();
				}
				else if (verificationOptions.getAlgorithmType() == 1)
				{		// Monolithic...
					isIncluded = automataVerifier.monolithicControllabilityVerification();
				}
				else if (verificationOptions.getAlgorithmType() == 2)
				{		// IDD...
					requestStop();

					// thisCategory.error("Option not implemented...");
					workbench.error("Option not implemented...");

					return;
				}
				else
				{		// Error...
					requestStop();

					// thisCategory.error("Unavailable option chosen.");
					workbench.error("Unavailable option chosen.");

					return;
				}
			}
			catch (Exception e)
			{
				requestStop();

				// thisCategory.error("Error in AutomataVerificationWorker when verifying automata. " + e);
				workbench.error("Error in AutomataVerificationWorker when verifying automata. " + e);

				return;
			}

			endDate = new Date();

			// Present result...
			if (!stopRequested)
			{
				if (isIncluded)
				{
					JOptionPane.showMessageDialog(workbench.getFrame(), "The language of the unselected automata is included in the language of the selected automata.", "Good news", JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					JOptionPane.showMessageDialog(workbench.getFrame(), "The language of the unselected automata is NOT included in the language of the selected automata.", "Bad news", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
		else
		{				// Error...
			requestStop();

			// thisCategory.error("Unavailable option chosen.");
			workbench.error("Unavailable option chosen.");

			return;
		}

		// Present result...
		automataVerifier.getHelper().displayInfo();

		if (!stopRequested)
		{

			// thisCategory.info("Execution completed after " + (endDate.getTime()-startDate.getTime())/1000.0 + " seconds.");
			workbench.info("Execution completed after " + (endDate.getTime() - startDate.getTime()) / 1000.0 + " seconds.");
		}
		else
		{

			// thisCategory.info("Execution stopped after " + (endDate.getTime()-startDate.getTime())/1000.0 + " seconds.");
			workbench.info("Execution stopped after " + (endDate.getTime() - startDate.getTime()) / 1000.0 + " seconds.");
		}

		requestStop();
	}

	public void requestStop()
	{

		if (executionDialog != null)
		{
			executionDialog.setMode(ExecutionDialogMode.hide);
		}

		stopRequested = true;
	}
}
