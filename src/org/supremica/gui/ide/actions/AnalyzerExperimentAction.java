//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.gui.AutomataSynchronizerWorker;


public class AnalyzerExperimentAction
    extends IDEAction
{
    private static final Logger logger = LogManager.getLogger(AnalyzerExperimentAction.class);
    private static final long serialVersionUID = 1L;

    public AnalyzerExperimentAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(false);

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

		final Automata allAutomataBefore = ide.getActiveDocumentContainer().getAnalyzerPanel().getAllAutomata();
        final Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();

        // EXPERIMENT!
        {
		/* This is some kind fo "split", unclear who wrote it but apparently it was not good
		 * to get a menu option of its own...

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
		 * **************/

		/* This is a first try of doing proper interleave -- MF
		 * It turns out that this can be done by using actions already available. It goes like this:
		   1. Make all events unobservable (watch out for already unobservable events, such as tau events!)
		   2. Synchronise ordinarily
		   3. Set the obervability property back of all events touched under 1 above
		   4. Do language preserving minimization
		 **/
			final Alphabet alpha = selectedAutomata.getUnionAlphabet();
			final Alphabet changed_alpha = new Alphabet();	// Here we store the ones we change

			for(final LabeledEvent event : alpha)
			{
				if(event.isObservable())
				{
					event.setObservable(false);
					changed_alpha.add(event);
				}
			}

			// Now synchronize
			final SynchronizationOptions synchronizationOptions = new SynchronizationOptions();
			final AutomataSynchronizerWorker asw = new AutomataSynchronizerWorker(ide.getIDE(), selectedAutomata, "", synchronizationOptions);
			if(asw != null)
			{
				asw.start();	// Start this thread and wait for it to finish

				// while(asw.isAlive())
				{
					try
					{
						asw.join(); // For some reason this join seems not to work...
									// the num of automata after is the same as the num of automata before
									// But the synched automaton IS added to the Analyzer
					}
					catch (final InterruptedException excp)
					{
						logger.error("InterruptedException: " + excp);
					}
				}
			}

			// Now, somehow get the newly created automaton, the synch result
			final Automata allAutomataAfter = ide.getActiveDocumentContainer().getAnalyzerPanel().getAllAutomata();
			// The diff between allAutomataBefore and allAutomataAfter shoudl be a single automaton, the one we look for
			final int num = allAutomataAfter.nbrOfAutomata() - allAutomataBefore.nbrOfAutomata();
			if(num != 1)
			{
				// Something is seriously wrong, either no automaton or more than one was created by the synch...
				logger.error("Experiment error! " + num + " number of automata was created, we expected one, and only one!");
			}


        }

        logger.info("Experiment finished.");
    }
}
