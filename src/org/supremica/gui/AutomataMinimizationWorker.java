
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
import javax.swing.*;

import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.algorithms.minimization.*;
import org.supremica.automata.Automata;
import org.supremica.automata.Project;
import org.supremica.automata.Automaton;
import org.supremica.util.ActionTimer;

/**
 * Thread dealing with minimization.
 *
 *@author hugo
 *@since November 11, 2004
 */
public class AutomataMinimizationWorker
    extends Thread
    implements Stoppable
{
    private static Logger logger = LoggerFactory.createLogger(AutomataMinimizationWorker.class);

    private final Frame frame;
    private final Automata theAutomata;
    private final Project theProject;
    private final MinimizationOptions options;

    // For the stopping
    private ExecutionDialog executionDialog;
    private boolean stopRequested = false;
    private final ArrayList<Stoppable> threadsToStop = new ArrayList<Stoppable>();

    public AutomataMinimizationWorker(final Frame frame, final Automata theAutomata, final Project theProject, final MinimizationOptions options)
    {
        this.frame = frame;
        this.theAutomata = theAutomata;
        this.theProject = theProject;
        this.options = options;

        this.start();
    }

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
            requestStop();
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
                    threadsToStop.add(minimizer);
                    final Automaton newAutomaton =
                        minimizer.getMinimizedAutomaton(options);
                    threadsToStop.remove(minimizer);
                    if (stopRequested)
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
                catch (final Exception ex)
                {
                    logger.error("Exception in AutomatonMinimizerWorker. Automaton: " +
                        currAutomaton.getName() + " " + ex);
                    logger.debug(ex.getStackTrace());
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
                threadsToStop.add(minimizer);
                minimizer.setExecutionDialog(executionDialog);
                final Automata newAutomata = minimizer.getCompositionalMinimization(options);
                threadsToStop.remove(minimizer);

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
                requestStop();
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
            public void run()
            {
                if (executionDialog != null)
                {
                    executionDialog.setMode(ExecutionDialogMode.HIDE);
                }
            }
        });

        // How did it go?
        if (!stopRequested)
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
    public void requestStop()
    {
        stopRequested = true;

        for (final Iterator<Stoppable> exIt = threadsToStop.iterator(); exIt.hasNext(); )
        {
            exIt.next().requestStop();
        }
        threadsToStop.clear();

        logger.debug("AutomataMinimizationWorker requested to stop.");

        if (executionDialog != null)
        {
            executionDialog.setMode(ExecutionDialogMode.HIDE);
        }
    }

    public boolean isStopped()
    {
        return stopRequested;
    }
}
