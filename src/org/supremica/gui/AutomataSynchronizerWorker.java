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

public class AutomataSynchronizerWorker
	extends Thread
{
	private static Category thisCategory = LogDisplay.createCategory(AutomataSynchronizerWorker.class.getName());

	private Supremica workbench = null;
	private Automata theAutomata = null;
	private AutomatonContainer container = null;
	private String newAutomatonName = null;
	private static final int MODE_SYNC = 1;
	private static final int MODE_UPDATE = 2;
	private int mode = MODE_SYNC;
	private Automaton theAutomaton = null;
	private SynchronizationOptions syncOptions;

	public AutomataSynchronizerWorker(Supremica workbench,
		Automata theAutomata,
		String newAutomatonName,
		SynchronizationOptions syncOptions)
	{
		this.workbench = workbench;
		this.theAutomata = theAutomata;
		container = workbench.getAutomatonContainer();
		this.newAutomatonName = newAutomatonName;
		this.syncOptions = syncOptions;
		this.start();
	}

	public void run()
	{
		if (mode == MODE_SYNC)
		{
			AutomataSynchronizer theSynchronizer;
			try
			{
				theSynchronizer =
					new AutomataSynchronizer(theAutomata, syncOptions);
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception while constructing AutomataSynchronizer");
				return;
			}

			try
			{
				theSynchronizer.execute();
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception while executing AutomataSynchronizer");
				return;
			}
			try
			{
				theAutomaton = theSynchronizer.getAutomaton();
			}
			catch (Exception ex)
			{
				thisCategory.error("Exception in AutomatonSynchronizer while getting the automaton");
				return;
			}
			theAutomaton.setName(newAutomatonName);
			mode = MODE_UPDATE;
			java.awt.EventQueue.invokeLater(this);
		}
		else if (mode == MODE_UPDATE)
		{
			try
			{
				if (theAutomaton != null)
				{
					container.add(theAutomaton);
				}
			}
			catch (Exception ex)
			{
				thisCategory.error("Could not add the new automaton after synchronization");
				return;
			}
		}
	}
}
