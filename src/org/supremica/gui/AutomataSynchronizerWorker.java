
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

import java.util.ArrayList;

import javax.swing.JOptionPane;

import net.sourceforge.waters.model.analysis.Abortable;

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

        // Order this thread to begin execution; the Jvm calls the run method of this thread.
        this.start();
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
                theSynchronizer = new AutomataSynchronizer(theAutomata, syncOptions, Config.SYNTHESIS_SUP_AS_PLANT.get());
            }
            catch (final Exception e)
            {
                timer.stop();

                // -- MF -- should really put up a message box here? Why not let the Gui manage that?
                JOptionPane.showMessageDialog(ide.getFrame(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

                // logger.error(e.getMessage());
                ide.error(e.getMessage());

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
                ide.error("Exception while executing AutomataSynchronizer", ex);
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
                    ide.error("Exception in AutomatonSynchronizer while getting the automaton" + ex);
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
                ide.info("Execution completed after " + timer.toString());
            }
            else
            {
                // Date endDate = new Date();
                timer.stop();

                // logger.info("Execution stopped after " + (endDate.getTime() - startDate.getTime()) / 1000.0 + " seconds!");
                // workbench.info("Execution stopped after " + (endDate.getTime() - startDate.getTime()) / 1000.0 + " seconds!");
                ide.info("Execution stopped after " + timer.toString());
            }

            theSynchronizer.displayInfo();
            executionDialog.setMode(ExecutionDialogMode.HIDE);
        }
        else if (mode == MODE_UPDATE)
        {
            // Display automaton
            try
            {
                if (theAutomaton != null)
                {
                    // -- MF -- container.add(theAutomaton);
                    // workbench.getVisualProjectContainer().getActiveProject().addAutomaton(theAutomaton);
                    ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().addAutomaton(theAutomaton);
                }
            }
            catch (final Exception ex)
            {
                // logger.error("Could not add the new automaton after synchronization");
                // logger.debug(ex.getStackTrace());
                ide.error("Could not add the new automaton after synchronization");

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
    public void resetAbort(){
      abortRequested = false;
    }
}
