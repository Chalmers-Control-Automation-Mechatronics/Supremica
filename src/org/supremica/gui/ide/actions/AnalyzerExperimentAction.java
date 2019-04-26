//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2019 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.model.analysis.Abortable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataListener;
import org.supremica.automata.AutomataListeners;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.algorithms.EnumerateStates;
import org.supremica.automata.algorithms.EquivalenceRelation;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.gui.AutomataMinimizationWorker;
import org.supremica.gui.AutomataSynchronizerWorker;
import org.supremica.gui.ide.SupremicaAnalyzerPanel;


/**
 * This is a first try of doing proper interleave.
 * It turns out that this can be done by using actions already available. It goes like this:
 * <OL>
 * <LI>Make all events unobservable (watch out for already unobservable events, such as tau events!)</LI>
 * <LI>Synchronise ordinarily</LI>
 * <LI>Set the observability property back of all events touched under 1 above</LI>
 * <LI>Do language preserving minimisation</LI>
 * </OL>
 *
 * This is the worker that takes care of things once the synchronised result has been added
 * Note that it does not work the same way as the other Automata*Worker, as this one is really only
 * a listener, that reacts to what it hears.
 *
 * @author Martin Fabian
 */
class AutomataInterleaveWorker implements AutomataListener
{
	private static final Logger logger = LogManager.getLogger(AutomataInterleaveWorker.class);

	private final IDEActionInterface IDE;
	private final SupremicaAnalyzerPanel analyzerPanel;
	private final Alphabet changedAlphas;

	private int currentMode;
	private static final int SYNC_MODE = 0x4F;	// In the first mode we do synch
	private static final int MIN_MODE = 0xFAB1A4E;	// In the second mode we do lang eq minimization

	public AutomataInterleaveWorker(final IDEActionInterface ide)
	{
		this.IDE = ide;
		this.analyzerPanel = this.IDE.getActiveDocumentContainer().getSupremicaAnalyzerPanel();
		this.currentMode = SYNC_MODE;

	    final Automata selectedAutomata = this.analyzerPanel.getSelectedAutomata();
		final Alphabet alpha = selectedAutomata.getUnionAlphabet();
		this.changedAlphas = new Alphabet();	// Here we store the ones we change

		for(final LabeledEvent event : alpha)
		{
			if(event.isObservable())	// If this event is obs, set it un-obs and remember to set it back
			{
				event.setObservable(false);
				changedAlphas.add(event);
			}
		}

		// Add the listener that is to do the work once the synchronized automaton has been added
		final Automata allAutomata = this.analyzerPanel.getAllAutomata(); // This really returns the visualProject
		allAutomata.addListener(this);

		// Now synchronize
		final SynchronizationOptions synchronizationOptions = new SynchronizationOptions();
		synchronizationOptions.setUnobsEventsSynch(false); // Make sure unobs non-tau events do NOT synch!!
		synchronizationOptions.setUseShortStateNames(true); // No need to have teh long names, is there...?
		synchronizationOptions.setForbidUncontrollableStates(false); // Do not mark any states as forbidden

		final AutomataSynchronizerWorker asw = new AutomataSynchronizerWorker(ide.getIDE(), selectedAutomata, "", synchronizationOptions);
		asw.start();	// Start the synch thread and just let it roam
	}

	@Override
	public void automatonAdded(final Automata automata, final Automaton automaton)
	{
		logger.debug("AutomataInterleaveWorker: Automaton " + automaton.getName() + " added!");

		if(this.currentMode == SYNC_MODE)
		{
			// Now reset the observability of the events that were previously changed
			for(final LabeledEvent event : this.changedAlphas)
			{
				event.setObservable(true);
			}

			// Now we gonna do Language Eq Minimization on this automaton
			// Probably have to do the same thing as we did for synchronizing
			final MinimizationOptions options = new MinimizationOptions();
			options.setMinimizationType(EquivalenceRelation.LANGUAGEEQUIVALENCE);
			options.setAlsoTransitions(true);
			options.setIgnoreMarking(false);
			options.setKeepOriginal(false);	// true keeps SYNC_MODE result in addition to adding MIN_MODE result
											// false replaces SYNC_MODE result with MIN_MODE result

			this.currentMode = MIN_MODE;

			final Automata selectedAutomata = new Automata();
			selectedAutomata.addAutomaton(automaton);
			// Start the worker thread and let it roam free
			final AutomataMinimizationWorker amw = new AutomataMinimizationWorker(IDE.getFrame(), selectedAutomata, analyzerPanel.getVisualProject(), options);
			amw.start();
		}
		else if(this.currentMode == MIN_MODE)
		{
			/*
			 * We assume that with keepOriginal == false, we get notified in the following order:
			 * synchronization result added in SYNC_MODE
			 * synchronization result removed in MIN_MODE
			 * minimization result added in MIN_MODE
			*/

			// We wait for just this one, so remove us now that we're done
			final AutomataListeners listeners = automata.getListeners();
			listeners.removeListener(this);

			// Enumerate all states as q0, q1 etc, with q0 being the initial state
			final Automata selectedAutomata = new Automata();
			selectedAutomata.addAutomaton(automaton);
			final EnumerateStates enumerateStates = new EnumerateStates(selectedAutomata, "q");
			enumerateStates.execute();
		}
	}

	@Override
	public void automatonRemoved(final Automata automata, final Automaton automaton)
	{
		assert this.currentMode == MIN_MODE : "AutomataInterleaveWorker expected automatonRemoved notification only in MIN_MODE!";

		logger.debug("AutomataInterleaveWorker: automaton " + automaton.getName() + " removed");
	}

	@Override
	public void automatonRenamed(final Automata automata, final Automaton automaton)
	{
		// We did not expect this one, so remove us and report error
		final AutomataListeners listeners = automata.getListeners();
		listeners.removeListener(this);
		logger.error("AutomataInterleaveWorker: automatonRenamed not expected");
	}

	@Override
	public void actionsOrControlsChanged(final Automata automata)
	{
		// We did not expect this one, so remove us and report error
		final AutomataListeners listeners = automata.getListeners();
		listeners.removeListener(this);
		logger.error("AutomataInterleaveWorker: actionsOrControlsChanged not expected");
	}

	@Override
	public void updated(final Object o)
	{
		// Have no clue how to handle this, don't even know what the Object is
		throw new UnsupportedOperationException("AutomataInterleaveWorker: updated not supported");
	}
}


/******************************************************************
 * A new action
 */
public class AnalyzerExperimentAction
    extends IDEAction
{
    private static final Logger logger = LogManager.getLogger(AnalyzerExperimentAction.class);
    private static final long serialVersionUID = 1L;

    public AnalyzerExperimentAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Experiment");
        putValue(Action.SHORT_DESCRIPTION, "Test of new stuff - this time: interleave");
        //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        //putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Remove16.gif")));
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    /**
     * The code that is run when the action is invoked.
     */
    @Override
    public void doAction()
    {
        logger.info("Experiment started...");

		/*************************************/

		// splitExperiment();	// not by MF, see below
		// justAddAutomatonExperiment();
		// addAutomatonThreadedExperiment();
		interleaveExperiment();
		// interleaveThreadedExperiment();

		/*********************************/
        logger.info("Experiment finished.");
    }

	/*
	 * Try to synch the selected automata and then check that the new automaton is there - does not work correctly
	*/
	void interleaveExperiment()	//-- MF
	{
        final Automata selectedAutomata = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getSelectedAutomata();
		if(!selectedAutomata.sanityCheck(ide.getIDE(), 2, true, false, false, false))
		{
			return;
		}
		// Input is sane, go ahead just do it...
		new AutomataInterleaveWorker(ide);

	/**************** Try to let the InterleaveListener do all the work
		final AnalyzerPanel analyzerPanel = ide.getActiveDocumentContainer().getAnalyzerPanel();
		final Automata allAutomata = analyzerPanel.getAllAutomata(); // This really returns the visualProject
		// final int nbrAutomataBefore = allAutomata.nbrOfAutomata();
        final Automata selectedAutomata = analyzerPanel.getSelectedAutomata();

		final Alphabet alpha = selectedAutomata.getUnionAlphabet();
		final Alphabet changed_alpha = new Alphabet();	// Here we store the ones we change

		for(final LabeledEvent event : alpha)
		{
			if(event.isObservable())	// If this event is obs, set it un-obs and remember to set it back
			{
				event.setObservable(false);
				changed_alpha.add(event);
			}
		}

		// Add the listener that is to do the work once the synchronized automaton has been added
		final AutomataListener inlisten = new InterleaveListener(ide, analyzerPanel, changed_alpha);
		allAutomata.addListener(inlisten);

		// Now synchronize
		final SynchronizationOptions synchronizationOptions = new SynchronizationOptions();	// Make sure unobs non-tau events do NOT synch!
		final AutomataSynchronizerWorker asw = new AutomataSynchronizerWorker(ide.getIDE(), selectedAutomata, "", synchronizationOptions);

		asw.start();	// Start the synch thread and wait for it to finish
		* **********************************************************************/
	/************** We don't need this, just let asw roam free and we get informed when the synch result has been added
		while(asw.isAlive())
		{
			try
			{
				asw.join(); // Wait for it to finish
			}
			catch (InterruptedException excp) // May be thrown by join (and sleep, and wait)
			{
				// Apparently this is the way to do it
				// See http://www.yegor256.com/2015/10/20/interrupted-exception.html
				Thread.currentThread().interrupt();
				throw new RuntimeException(excp);
			}
		}
		* *************************************************************/
/******************************** this is not the way to do it!
		// Now, somehow get the newly created automaton, the synch result
		// For now we just check that something was added, as we expect
		final int nbrAutomataAfter = analyzerPanel.getAllAutomata().nbrOfAutomata();
		// The diff between nbrAutomataBefore and nbrAutomataAfter should be a single automaton, the one we look for
		final int num = nbrAutomataAfter - nbrAutomataBefore;
		System.out.println("Num all automata before: " + nbrAutomataBefore);
		System.out.println("Num all automata after: " + nbrAutomataAfter);
		if(num != 1)
		{
			// Something is seriously wrong, either no automaton or more than one was created by the synch...
			logger.error("Experiment error! " + num + " number of automata was created, we expected one, and only one!");
		}

		// The above does not work correctly, the automaton is added by the synch worker, but apparently AFTER
		// the join() has happened, so the num of automata before and after are always(?) the same
		// Probably some race condition - have to rethink this
********************************/
	}


	/*
	 * So justAddAutomatonExperiment finally works as expected. Now let's try the threaded variant
	 * Also this seems now to work fine
	*/
	void addAutomatonThreadedExperiment()	//-- MF
	{
		final Automata allAutomataBefore = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getAllAutomata();
		final int nbrAutomataBefore = allAutomataBefore.nbrOfAutomata();

		final AnalyzerAddAutomatonJustForExperiment adder = new AnalyzerAddAutomatonJustForExperiment(ide);
		adder.start();

		while(adder.isAlive())
		{
			try
			{
				adder.join();	// Wait for the adding to finish
			}
			catch(final InterruptedException excp)
			{
				// Apparently this is the way to do it
				// See http://www.yegor256.com/2015/10/20/interrupted-exception.html
				Thread.currentThread().interrupt();
				throw new RuntimeException(excp);
			}
		}

		// Now we check if the automaton has really been added, there should be one more than before.
		final Automata allAutomataAfter = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getAllAutomata();
		// The diff between nbrAutomataBefore and nbrAutomataAfter should be a single automaton, the one we look for
		// final int num = allAutomataAfter.nbrOfAutomata() - allAutomataBefore.nbrOfAutomata();
		final int num = allAutomataAfter.nbrOfAutomata() - nbrAutomataBefore;
		System.out.println("Num all automata before: " + nbrAutomataBefore);
		System.out.println("Num all automata after: " + allAutomataAfter.nbrOfAutomata());
		if(num != 1)
		{
			// Something is seriously wrong, either no automaton or more than one was created...
			System.out.println("Experiment error! " + num + " number of automata was created, we expected one, and only one!");

		}
	}

	/*
	 * Something is not working as expected when it comes to waiting for the worker thread
	 * to finish using join(), so this is to run some simple test to try to determine how
	 * this whole thing should work.
	 * It turns out that it is the getAllAutomata() that does not work as expected! It does not give you
	 * a list of all Automata, it gives you a reference to the visualProject, which means that in the code
	 * below, allAutomataBefore and allAutomataAfter refer to the exact same object, and hence the nbrOfAutomata
	 * will be the same when checked at the end! This was tricky!!
	*/
	void justAddAutomatonExperiment()	//-- MF
	{
		final Automata allAutomataBefore = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getAllAutomata();
		final int nbrAutomataBefore = allAutomataBefore.nbrOfAutomata();

		/* Does it work if we just add the automaton here without starting that other thread? */
			System.out.println("Adding automaton...");
			final Automaton new_auto = new Automaton("Mxyzptlk");
			final boolean done = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().addAutomaton(new_auto);
			System.out.println("Added automaton: " + done);
		/* NO! It still doesn't work. done is true, but num below still becomes 0! WTF? */
		/* But OK, so this means this is not a racing issue, but it simple does not work the way you expect it to */

		// Now we check if the automaton has really been added, there should be one more than before.
		final Automata allAutomataAfter = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getAllAutomata();
		// The diff between nbrAutomataBefore and nbrAutomataAfter should be a single automaton, the one we look for
		// final int num = allAutomataAfter.nbrOfAutomata() - allAutomataBefore.nbrOfAutomata();
		final int num = allAutomataAfter.nbrOfAutomata() - nbrAutomataBefore;
		System.out.println("Num all automata before: " + nbrAutomataBefore);
		System.out.println("Num all automata after: " + allAutomataAfter.nbrOfAutomata());
		if(num != 1)
		{
			// Something is seriously wrong, either no automaton or more than one was created...
			System.out.println("Experiment error! " + num + " number of automata was created, we expected one, and only one!");

		}

	}

	/* ****************************************************************
	 * This is some kind of "split", unclear who wrote it but apparently it was not good
	 * enough to get a menu option of its own...
	void splitExperiment()
	{
            // "DECOMPOSE" INDIVUDUAL AUTOMATA
            for (final Automaton automaton: selectedAautomata)
            {
                final Automata result = AutomatonSplit.split(automaton);

                try
                {
                    ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().addAutomata(result);
                }
                catch (final Exception ex)
                {
                    logger.debug("SplitAction::actionPerformed() -- ", ex);
                    logger.debug(ex.getStackTrace());
                }
            }

	}
	* **************/
}

/**
 * These were used for all kinds of experimentation when I (MF) thought that the bug was in my code when it really was
 * in AnalyzerPanel.getAllAutomata().
 */
class AnalyzerAddAutomatonJustForExperiment
	extends Thread
	implements Abortable
{
	@SuppressWarnings("unused")
  private static final Logger logger = LogManager.getLogger(AnalyzerAddAutomatonJustForExperiment.class);

	private boolean abortRequested;
	@SuppressWarnings("unused")
  private final IDEActionInterface IDE;
	private final SupremicaAnalyzerPanel analyzerPanel;

	public AnalyzerAddAutomatonJustForExperiment(final IDEActionInterface ide)
	{
		this.IDE = ide;
		this.analyzerPanel = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel();
	}

	@Override
	public void run()
	{
//		try
//		{
//			System.out.println("Going to sleep for 1000 ms ...");
//			Thread.sleep(1000);
			System.out.println("Adding automaton...");
			final Automaton new_auto = new Automaton("Mxyzptlk");
			final boolean done = analyzerPanel.addAutomaton(new_auto);	// Does indeed return true!
			System.out.println("Added automaton: " + done);
//			System.out.println("\nGoing to sleep again for 1000 ms ...");
//			Thread.sleep(1000);
//			System.out.println("Just woke up...");
//		}
//		catch(InterruptedException excp) // May be thrown by sleep
//		{
//			// Apparently this is the way to do it
//			// See http://www.yegor256.com/2015/10/20/interrupted-exception.html
//			Thread.currentThread().interrupt();
//			throw new RuntimeException(excp);
//		}
	}

    @Override
    public void requestAbort()
    {
		abortRequested = true;
    }

    @Override
    public boolean isAborting()
    {
        return abortRequested;
    }

    @Override
    public void resetAbort()
	{
      abortRequested = false;
    }
}

//==============================================================

class AnalyzerInterleaveWorker extends Thread implements Abortable
{
	private boolean abortRequested;
	private final IDEActionInterface IDE;
	private final SupremicaAnalyzerPanel analyzerPanel;

	public AnalyzerInterleaveWorker(final IDEActionInterface ide)
	{
		this.IDE = ide;
		this.analyzerPanel = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel();
	}

	@Override
	public void run()
	{
		final int nbrAutomataBefore = analyzerPanel.getAllAutomata().nbrOfAutomata();
        final Automata selectedAutomata = analyzerPanel.getSelectedAutomata();

		final Alphabet alpha = selectedAutomata.getUnionAlphabet();
		final Alphabet changed_alpha = new Alphabet();	// Here we store the ones we change

		for(final LabeledEvent event : alpha)
		{
			if(event.isObservable())	// If this event is obs, set it un-obs and remember to set it back
			{
				event.setObservable(false);
				changed_alpha.add(event);
			}
		}

		// Now synchronize
		final SynchronizationOptions synchronizationOptions = new SynchronizationOptions();	// Make sure unobs non-tau events do NOT synch!
		final AutomataSynchronizerWorker asw = new AutomataSynchronizerWorker(IDE.getIDE(), selectedAutomata, "", synchronizationOptions);
		if(asw != null)
		{
			asw.start();	// Start this thread and wait for it to finish

			while(asw.isAlive())
			{
				try
				{
					asw.join(); // Wait for it to finish
				}
				catch (final InterruptedException excp)
				{
					// Apparently this is the way to do it
					// See http://www.yegor256.com/2015/10/20/interrupted-exception.html
					Thread.currentThread().interrupt();
					throw new RuntimeException(excp);
				}
			}
		}

		// Now, somehow get the newly created automaton, the synch result
		// For now we just check that something was added, as we expect
		final int nbrAutomataAfter = analyzerPanel.getAllAutomata().nbrOfAutomata();
		// The diff between nbrAutomataBefore and nbrAutomataAfter should be a single automaton, the one we look for
		final int num = nbrAutomataAfter - nbrAutomataBefore;
		System.out.println("Num all automata before: " + nbrAutomataBefore);
		System.out.println("Num all automata after: " + nbrAutomataAfter);
		if(num != 1)
		{
			// Something is seriously wrong, either no automaton or more than one was created by the synch...
			throw new RuntimeException("Experiment error! " + num + " number of automata was created, we expected one, and only one!");
		}

		// If all is fine, a single automaton was added as we expected it to, set the observability back for all the events that were changed
	}

    @Override
    public void requestAbort()
    {
        abortRequested = true;
    }

    @Override
    public boolean isAborting()
    {
        return abortRequested;
    }

    @Override
    public void resetAbort()
	{
      abortRequested = false;
    }

}
