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

import org.apache.log4j.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

public class AutomataVerificationWorker
	extends Thread
	implements Stoppable
{
	private static Category thisCategory = LogDisplay.createCategory(AutomataVerificationWorker.class.getName());

	private Supremica workbench = null;
	private Automata theAutomata = null;
	private AutomatonContainer container = null;
	private String newAutomatonName = null;
	private static final int MODE_SYNC = 1;
	private static final int MODE_UPDATE = 2;
	private int mode = MODE_SYNC;
	private Automaton theAutomaton = null;
	private SynchronizationOptions synchronizationOptions;
	private VerificationOptions verificationOptions;

	private boolean stopRequested = false;

	public AutomataVerificationWorker(Supremica workbench,
									  Automata theAutomata,
									  SynchronizationOptions synchronizationOptions, 
									  VerificationOptions verificationOptions)
	{
		this.workbench = workbench;
		this.theAutomata = theAutomata;
		container = workbench.getAutomatonContainer();
		this.newAutomatonName = newAutomatonName;
		this.synchronizationOptions = synchronizationOptions;
		this.verificationOptions = verificationOptions;
		this.start();
	}

	public void run()
	{
			if (verificationOptions.getVerificationType() == 0)
			{   // Controllability verification...
				AutomataVerifier automataVerifier = new AutomataVerifier(theAutomata, synchronizationOptions, verificationOptions);

				ArrayList threadsToStop = new ArrayList();
				threadsToStop.add(automataVerifier);				
				threadsToStop.add(this);
				CancelDialog cancelDialog = new CancelDialog(workbench, threadsToStop);
				automataVerifier.getHelper().setCancelDialog(cancelDialog);
				cancelDialog.updateHeader("Verifying...");
				boolean isControllable;

				// Verify controllability...
				Date startDate = new Date();
				try 
				{
				    isControllable = automataVerifier.execute();
				}
				catch (Exception e)
				{	
					thisCategory.error("Error in AutomataVerificationWorker when verifying automata. " + e);
					return;
				}		
				Date endDate = new Date();

				// Present result...
				if (!stopRequested)
				{
					thisCategory.info("Execution completed after " + (endDate.getTime()-startDate.getTime())/1000.0 + 
									  " seconds.");	
					if (isControllable)
					{
						JOptionPane.showMessageDialog(workbench, "The automata is controllable!", 
													  "Good news", JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane.showMessageDialog(workbench, "The automata is NOT controllable!", 
													  "Bad news", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else
				{
					thisCategory.info("Execution stopped after " + (endDate.getTime()-startDate.getTime())/1000.0 + 
									  " seconds.");	
				}				

				cancelDialog.destroy();
			}
			else if (verificationOptions.getVerificationType() == 1)
			{	// Non-blocking verification...
				thisCategory.error("Option not implemented...");
				return;
			}				
			else if (verificationOptions.getVerificationType() == 2)
			{	// Language inclusion
				if (verificationOptions.getAlgorithmType() == 0)				
				{   // Modular...
				}
				else if (verificationOptions.getAlgorithmType() == 1)				
				{	// Monolithic...
					thisCategory.error("Option not implemented...");
					return;
				}
				else if (verificationOptions.getAlgorithmType() == 2)
				{   // IDD...
					thisCategory.error("Option not implemented...");
					return;
				}				
				else
				{   // Error...
					thisCategory.error("Unavailable option chosen.");
					return;
				}
			}
			else
			{   // Error...
				thisCategory.error("Unavailable option chosen.");
				return;
			}
	}

	public void requestStop()
	{
		stopRequested = true;		
	}
}
