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

package org.supremica.gui;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import net.sourceforge.waters.model.analysis.Abortable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.gui.ide.actions.IDEActionInterface;
import org.supremica.properties.Config;
import org.supremica.util.ActionTimer;

public class AutomataSynchronizerWorker
    extends Thread
    implements Abortable
{
    private IDEActionInterface ide = null;
    private Automata theAutomata = null;
    private final static int MODE_SYNC = 1;
    private final static int MODE_UPDATE = 2;
    private int mode = MODE_SYNC;
    private Automaton theAutomaton = null;
    private final SynchronizationOptions syncOptions;
    private boolean abortRequested = false;

    public AutomataSynchronizerWorker(final IDEActionInterface workbench, final Automata theAutomata, final String newAutomatonName, final SynchronizationOptions syncOptions)
    {
        this.ide = workbench;
        this.theAutomata = theAutomata;
        this.syncOptions = syncOptions;

        //// Order this thread to begin execution; the Jvm calls the run method of this thread.
        // this.start();	// This is bad practice, probably works in this case, but if someone sub-classes AutomataSynchronizerWorker all hell breaks loose
							// The proper way is that the caller starts (see org.supremica.gui.ide.actions.AnalyzerSynchronizerAction.java)
    }

    @Override
    public void run()
    {
        if (mode == MODE_SYNC)
        {
            final ActionTimer timer = new ActionTimer();
            timer.start();

            AutomataSynchronizer theSynchronizer;

            try
            {
                theSynchronizer = new AutomataSynchronizer(theAutomata, syncOptions, Config.SYNTHESIS_SUP_AS_PLANT.getValue());
            }
            catch (final Exception e)
            {
                timer.stop();

                // -- MF -- should really put up a message box here? Why not let the Gui manage that?
                JOptionPane.showMessageDialog(ide.getFrame(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

                // logger.error(e.getMessage());
                LOGGER.error(e.getMessage());

                return;
            }

            // Initialize execution dialog
            final ArrayList<Abortable> threadsToStop = new ArrayList<Abortable>();
            threadsToStop.add(theSynchronizer);
            threadsToStop.add(this);
            final ExecutionDialog executionDialog = new ExecutionDialog(ide.getFrame(), "Synchronizing", threadsToStop);
            theSynchronizer.getHelper().setExecutionDialog(executionDialog);
            executionDialog.setMode(ExecutionDialogMode.SYNCHRONIZING);

            // Synchronize automaton
            try
            {
                theSynchronizer.execute();
            }
            catch (final Exception ex)
            {
                timer.stop();
                LOGGER.error("Exception while executing AutomataSynchronizer", ex);
                // logger.error("Exception while executing AutomataSynchronizer");
                // logger.debug(ex.getStackTrace());
                return;
            }

            // Build automaton
            if (!abortRequested && syncOptions.buildAutomaton())
            {
                try
                {
                    theAutomaton = theSynchronizer.getAutomaton();
                }
                catch (final Exception ex)
                {
                    timer.stop();

                    // -- MF -- logger.error("Exception in AutomatonSynchronizer while getting the automaton" + ex);
                    LOGGER.error("Exception in AutomatonSynchronizer while getting the automaton" + ex);
                    ex.printStackTrace();

                    // logger.debug(ex.getStackTrace());
                    return;
                }
            }

            // Present result
            if (!abortRequested)
            {
                mode = MODE_UPDATE;

                java.awt.EventQueue.invokeLater(this);

                // Date endDate = new Date();
                timer.stop();

                // logger.info("Execution completed after " + (endDate.getTime() - startDate.getTime()) / 1000.0 + " seconds.");
                // workbench.info("Execution completed after " + (endDate.getTime() - startDate.getTime()) / 1000.0 + " seconds.");
                LOGGER.info("Execution completed after " + timer.toString());
            }
            else
            {
                // Date endDate = new Date();
                timer.stop();

                // logger.info("Execution stopped after " + (endDate.getTime() - startDate.getTime()) / 1000.0 + " seconds!");
                // workbench.info("Execution stopped after " + (endDate.getTime() - startDate.getTime()) / 1000.0 + " seconds!");
                LOGGER.info("Execution stopped after " + timer.toString());
            }

            theSynchronizer.displayInfo();
            executionDialog.setMode(ExecutionDialogMode.HIDE);
        }
        else if (mode == MODE_UPDATE)
        {
            // Add the automaton to the panel
            try
            {
                if (theAutomaton != null)
                {
					// If an automaton with the same name already exists, this silently throws away the result. Not good.
                    ide.getIDE().getActiveDocumentContainer().getSupremicaAnalyzerPanel().addAutomaton(theAutomaton);
                }
            }
            catch (final Exception ex)
            {
                // logger.error("Could not add the new automaton after synchronization");
                // logger.debug(ex.getStackTrace());
                LOGGER.error("Could not add the new automaton after synchronization");
                return;
            }
        }
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

    private static final Logger LOGGER = LogManager.getLogger();
}
