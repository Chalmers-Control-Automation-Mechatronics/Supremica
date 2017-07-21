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

package org.supremica.gui;

import java.awt.EventQueue;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.base.ProxyTools;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.algorithms.minimization.AutomataMinimizer;
import org.supremica.automata.algorithms.minimization.AutomatonMinimizer;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.ActionTimer;

/**
 * Thread dealing with minimization.
 *
 *@author hugo
 *@since November 11, 2004
 */
public class AutomataMinimizationWorker
    extends Thread
    implements Abortable
{
    private static Logger logger = LoggerFactory.createLogger(AutomataMinimizationWorker.class);

    private final Frame frame;
    private final Automata theAutomata;
    private final Project theProject;
    private final MinimizationOptions options;

    // For the stopping
    private ExecutionDialog executionDialog;
    private boolean abortRequested = false;
    private final ArrayList<Abortable> threadsToAbort = new ArrayList<Abortable>();

    public AutomataMinimizationWorker(final Frame frame, final Automata theAutomata, final Project theProject, final MinimizationOptions options)
    {
        this.frame = frame;
        this.theAutomata = theAutomata;
        this.theProject = theProject;
        this.options = options;

        this.start();
    }

    @Override
    public void run()
    {
        // Initialize the ExecutionDialog
        //threadsToStop.add(this);
        executionDialog = new ExecutionDialog(frame, "Minimizing", this);
        // Depending on number of automata it will look different
        if (theAutomata.size() > 1)
        {
            executionDialog.setMode(ExecutionDialogMode.MINIMIZING);
        }
        else
        {
            executionDialog.setMode(ExecutionDialogMode.MINIMIZINGSINGLE);
        }

        // OK options?
        final String errorMessage = options.validOptions();
        if (errorMessage != null)
        {
            JOptionPane.showMessageDialog(frame, errorMessage, "Alert", JOptionPane.ERROR_MESSAGE);
            requestAbort();
            return;
        }

        // Timer
        final ActionTimer timer = new ActionTimer();
        timer.start();

        // The result...
        final Automata result = new Automata();

        // Minimize either compositionally (involves composing automata and hiding local events) or
        // not (just minimise the components individually).
        if (!options.getCompositionalMinimization())
        {
            if (theAutomata.size() > 1)
            {
                executionDialog.initProgressBar(0, theAutomata.size());
            }

            // Iterate over automata and minimize each individually
            int i = 0;
            final Iterator<?> autIt = theAutomata.iterator();
            while (autIt.hasNext())
            {
                Automaton currAutomaton = (Automaton) autIt.next();

                // Do we have to care about the original?
                if (options.getKeepOriginal())
                {
                    // We need a copy since we might need to fiddle with the original
                    currAutomaton = new Automaton(currAutomaton);
                }

                // Minimize this one
                try
                {
                    final AutomatonMinimizer minimizer = new AutomatonMinimizer(currAutomaton);

                    if (theAutomata.size() == 1)
                    {
                        minimizer.setExecutionDialog(executionDialog);
                    }
                    threadsToAbort.add(minimizer);
                    final Automaton newAutomaton =
                        minimizer.getMinimizedAutomaton(options);
                    threadsToAbort.remove(minimizer);
                    if (abortRequested)
                    {
                        break;
                    }
                    newAutomaton.setComment
                        ("min(" + newAutomaton.getName() + ")");
                    newAutomaton.setName(null);
                    // Update execution dialog
                    if (executionDialog != null)
                    {
                        executionDialog.setProgress(++i);
                    }

                    result.addAutomaton(newAutomaton);
                }
                catch (final Exception exception)
                {
                  final String eclassname =
                    ProxyTools.getShortClassName(exception);
                  final String classname = ProxyTools.getShortClassName(this);
                  logger.error(eclassname + " in " + classname +
                               ", automaton " + currAutomaton.getName() +
                               ": " + exception.getMessage());
                  logger.debug(exception.getStackTrace());
                }
                if (!options.getKeepOriginal())
                {
                    theProject.removeAutomaton(currAutomaton);
                }
            }
        }
        else
        {
            // Compositional minimization!
            try
            {
                // Do we have to care about the original?
                Automata task = theAutomata;
                if (options.getKeepOriginal())
                {
                    // We need a copy since we might need to fiddle with the original
                    task = new Automata(theAutomata);
                }

                final AutomataMinimizer minimizer = new AutomataMinimizer(task);
                threadsToAbort.add(minimizer);
                minimizer.setExecutionDialog(executionDialog);
                final Automata newAutomata = minimizer.getCompositionalMinimization(options);
                threadsToAbort.remove(minimizer);

                // Minimized!
                if (newAutomata != null)
                {
                    result.addAutomata(newAutomata);
                }
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomatonMinimizer when compositionally minimizing " +
                    theAutomata + " " + ex);
                logger.debug(ex.getStackTrace());
                requestAbort();
            }

            if (!options.getKeepOriginal())
            {
                theProject.removeAutomata(theAutomata);
            }
        }

        // Timer
        timer.stop();

        // Hide execution dialog
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

        // How did it go?
        if (!abortRequested)
        {
            logger.info("Execution completed after " + timer.toString());

            // Add new automata
            try
            {
                theProject.addAutomata(result);
            }
            catch (final Exception ex)
            {
                logger.error(ex);
            }
        }
        else
        {
            logger.info("Execution stopped after " + timer.toString());
        }

        // We're finished! Make sure to kill the ExecutionDialog!
        if (executionDialog != null)
        {
            executionDialog.setMode(ExecutionDialogMode.HIDE);
            executionDialog = null;
        }
    }

    /**
     * Method that stops AutomataMinimizationWorker as soon as possible.
     *
     *@see  ExecutionDialog
     */
    @Override
    public void requestAbort()
    {
        abortRequested = true;

        for (final Iterator<Abortable> exIt = threadsToAbort.iterator(); exIt.hasNext(); )
        {
            exIt.next().requestAbort();
        }
        threadsToAbort.clear();

        logger.debug("AutomataMinimizationWorker requested to stop.");

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
