//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
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

/*****************************************************************
 * Experiment to see if I can get to run a Lua script from inside
 * Supremica. LuaJ needs to be available. The code simply opens a
 * FileChooser to allow selecting a *.lua script, and then runs it.
 */
class AnalyzerRunLuaScript
{
	public static void chooseAndRunScript() throws Exception
	{
		javax.swing.JFileChooser jfc =
			new javax.swing.JFileChooser(javax.swing.filechooser.FileSystemView.getFileSystemView().getHomeDirectory());

		int returnValue = jfc.showOpenDialog(null);

		if (returnValue != javax.swing.JFileChooser.APPROVE_OPTION)
			return;

		java.io.File selectedFile = jfc.getSelectedFile();
		String script = selectedFile.getPath();

		// create an environment to run in
		org.luaj.vm2.Globals globals = org.luaj.vm2.lib.jse.JsePlatform.standardGlobals();
		// Use the convenience function on Globals to load a chunk.
		org.luaj.vm2.LuaValue chunk = globals.loadfile(script);
		// Use any of the "call()" or "invoke()" functions directly on the chunk.
		chunk.call(org.luaj.vm2.LuaValue.valueOf(script));
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
        //putValue(Action.SHORT_DESCRIPTION, "Test of new stuff - this time: interleave");
        putValue(Action.SHORT_DESCRIPTION, "Test of new stuff - Run Lua script");
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
		// interleaveExperiment(); // MF
		runLuaScript();

		/*********************************/
        logger.info("Experiment finished.");
    }

	/*
	 *
	 */
	void runLuaScript()
	{
		try
		{
			AnalyzerRunLuaScript.chooseAndRunScript();
		}
		catch(Exception excp)
		{
			System.err.println("Soemthing went wrong, sorry!");
		}
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

