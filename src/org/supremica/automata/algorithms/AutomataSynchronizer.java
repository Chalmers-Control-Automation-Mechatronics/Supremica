
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

import org.supremica.automata.*;
import org.supremica.automata.algorithms.Stoppable;
import org.supremica.log.*;
import java.util.*;
import java.io.PrintWriter;

public class AutomataSynchronizer
	implements Stoppable
{
	private static Logger logger = LoggerFactory.createLogger(AutomataSynchronizer.class);
	private Automata theAutomata;
	private AutomataSynchronizerHelper synchHelper;
	private SynchronizationOptions syncOptions;
	private ArrayList synchronizationExecuters;

	// For stopping execution
	private boolean stopRequested = false;

	public AutomataSynchronizer(Automata theAutomata, SynchronizationOptions syncOptions)
		throws Exception
	{
		this.theAutomata = theAutomata;
		this.syncOptions = syncOptions;
		synchHelper = new AutomataSynchronizerHelper(theAutomata, syncOptions);

		// Allocate and initialize the synchronizationExecuters
		int nbrOfExecuters = syncOptions.getNbrOfExecuters();

		synchronizationExecuters = new ArrayList(nbrOfExecuters);

		for (int i = 0; i < nbrOfExecuters; i++)
		{
			AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(synchHelper);
			
			synchronizationExecuters.add(currSynchronizationExecuter);
		}
	}

	public void execute()
		throws Exception
	{
		State currInitialState;
		int[] initialState = AutomataIndexFormHelper.createState(theAutomata.size());

		// Build the initial state - and the comment
		Iterator autIt = theAutomata.iterator();
		StringBuffer comment = new StringBuffer();;

		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			currInitialState = currAutomaton.getInitialState();
			initialState[currAutomaton.getIndex()] = currInitialState.getIndex();

			comment.append(currAutomaton.getName());
			comment.append(" || ");
		}

		comment.delete(comment.length()-4, comment.length());
		synchHelper.addState(initialState);
		synchHelper.addComment(comment.toString());

		// Start all the synchronization executers and wait for completetion
		for (int i = 0; i < synchronizationExecuters.size(); i++)
		{
			AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);
			currExec.start();
		}

		for (int i = 0; i < synchronizationExecuters.size(); i++)
		{
			((AutomataSynchronizerExecuter) synchronizationExecuters.get(i)).join();
		}

	}

	public void displayInfo()
	{
		synchHelper.displayInfo();
	}

	// -- MF -- Added to allow users easy access to the number of synch'ed states
	public long getNumberOfStates()
	{
		return synchHelper.getNumberOfAddedStates();
	}

	public Automaton getAutomaton()
		throws Exception
	{
		AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(0);

		try
		{
			if (currExec.buildAutomaton())
			{
				// System.out.println(synchHelper.getAutomaton() == null);
				return synchHelper.getAutomaton();
			}
			else
			{
				return null;
			}
		}
		catch (Exception ex)
		{
			logger.error(ex.toString());
			logger.debug(ex.getStackTrace());
			throw ex;
		}
	}

	public AutomataSynchronizerHelper getHelper()
	{
		return synchHelper;
	}

	/* GAAAH! The garbage collection is too slow... and this won't make it faster... /Hugo.
	public void clear()
	{
		synchHelper = null;
		for (int i = 0; i < synchronizationExecuters.size(); i++)
		{
			AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);
			
			currExec = null;
		}
		synchronizationExecuters = null;
	}
	*/

	public void requestStop()
	{
		stopRequested = true;

		for (int i = 0; i < synchronizationExecuters.size(); i++)
		{
			((AutomataSynchronizerExecuter) synchronizationExecuters.get(i)).requestStop();
		}
	}

	/**
	 * Standard method for synchronizing automata with default options.
	 */
	public static Automaton synchronizeAutomata(Automata theAutomata)
		throws Exception
	{
		SynchronizationOptions syncOptions;
		syncOptions = SynchronizationOptions.getDefaultSynchronizationOptions();

		AutomataSynchronizer synchronizer = new AutomataSynchronizer(theAutomata, syncOptions);
		synchronizer.execute();
		
		return synchronizer.getAutomaton();
	}
}
