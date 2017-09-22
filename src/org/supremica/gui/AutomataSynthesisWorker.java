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
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import net.sourceforge.waters.model.analysis.Abortable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.AutomataSynthesizer;
import org.supremica.automata.algorithms.AutomatonSynthesizer;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesizerOptions;
import org.supremica.gui.ide.actions.IDEActionInterface;
import org.supremica.util.ActionTimer;


/**
 * Thread dealing with synthesis.
 *
 * @author Hugo Flordal
 * @since November 18, 2004
 */
public class AutomataSynthesisWorker
    extends Thread
    implements Abortable
{
    private static Logger logger = LogManager.getLogger(AutomataSynthesisWorker.class);

    private final IDEActionInterface ide;
    private final Automata theAutomata;
    private final SynthesizerOptions synthOptions;

    private ExecutionDialog executionDialog;
    private boolean abortRequested = false;
    @SuppressWarnings("unused")
	private final EventQueue eventQueue = new EventQueue();
    private ActionTimer timer;
    private Automata result;

    public AutomataSynthesisWorker(final IDEActionInterface gui, final Automata theAutomata, final SynthesizerOptions options)
    {
        this.ide = gui;
        this.theAutomata = theAutomata;
        this.synthOptions = options;

        this.start();
    }

    @Override
    public void run()
    {
        // Initialize the ExecutionDialog
        final ArrayList<Abortable> threadsToStop = new ArrayList<Abortable>();
        threadsToStop.add(this);
        if(ide != null){
            executionDialog = new ExecutionDialog(ide.getFrame(), "Synthesizing", threadsToStop);
            executionDialog.setMode(ExecutionDialogMode.SYNTHESIZING);
        }

        // OK options?
        final String errorMessage = synthOptions.validOptions();
        if (errorMessage != null)
        {
            if(ide != null)
                JOptionPane.showMessageDialog(ide.getFrame(), errorMessage, "Alert", JOptionPane.ERROR_MESSAGE);
            requestAbort();
            return;
        }
        timer = new ActionTimer();
        // Timer
        timer.start();

        // Do the work!!
        result = new Automata();
        // Are there many or just one automaton?
        if (theAutomata.size() > 1 ||
            synthOptions.getSynthesisAlgorithm() ==
            SynthesisAlgorithm.MONOLITHIC_WATERS)
        {
            // Get default synchronization options
            final SynchronizationOptions syncOptions = SynchronizationOptions.getDefaultSynthesisOptions();

            try
            {
                final AutomataSynthesizer synthesizer = new AutomataSynthesizer(theAutomata, syncOptions, synthOptions);
                if(ide != null)
                    synthesizer.setExecutionDialog(executionDialog);
                threadsToStop.add(synthesizer);

                result.addAutomata(synthesizer.execute());
                threadsToStop.remove(synthesizer);
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomataSynthesisWorker. " + ex);
                logger.debug(ex.getStackTrace());
            }
        }
        else  // Single automaton
        {
            // Make copy
            final Automaton theAutomaton = new Automaton(theAutomata.getFirstAutomaton());

            try
            {
                final AutomatonSynthesizer synthesizer = new AutomatonSynthesizer(theAutomaton, synthOptions);
                threadsToStop.add(synthesizer);
                synthesizer.synthesize();
                result.addAutomaton(synthesizer.getAutomaton());
                threadsToStop.remove(synthesizer);
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomataSynthesisWorker. Automaton: " + theAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
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
                if(ide != null)
                {
                    if (executionDialog != null)
                    {
                        executionDialog.setMode(ExecutionDialogMode.HIDE);
                    }
                }
            }
        });

        // Present result
        if (!abortRequested)
        {
            logger.info("Synthesis completed after " + timer.toString() + ".");

            // Add new automata
            if(ide != null)
            {
                try
                {
                    ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().addAutomata(result);
                }
                catch (final Exception ex)
                {
                    logger.error(ex);
                }
            }
        }
        else
        {
            logger.info("Execution stopped after " + timer.toString());
        }

        if(ide != null)
        {
            // We're finished! Make sure to kill the ExecutionDialog!
            if (executionDialog != null)
            {
                executionDialog.setMode(ExecutionDialogMode.HIDE);
            }
        }
    }

    public Automaton getSupervisor()
    {
        return result.getFirstAutomaton();
    }

    public String getTime()
    {
        return timer.toString();
    }

    public BigDecimal getTimeSeconds()
    {
        final String[] result = (getTime()).split("\\s");
        Float out = (float)-1;
        if(result.length==2)
            out = (new Float(result[0]))/1000;
        else
        {
            Float f1=new Float(-1),f2=new Float(-1);
            if(result[1].equals("seconds"))
                f1 = new Float(result[0]);
            else if(result[1].equals("minutes"))
                f1 = new Float(result[0])*60;
            else if(result[1].equals("hours"))
                f1 = new Float(result[0])*120;

            if(result[3].equals("milliseconds"))
                f2 = new Float(result[2])/ 1000;
            else if(result[3].equals("seconds"))
                f2 = new Float(result[2]);
            else if(result[3].equals("minutes"))
                f2 = new Float(result[2])*60;

            out = (f1+f2);
        }

        BigDecimal bd = new BigDecimal(out);
        bd = bd.setScale(3,BigDecimal.ROUND_DOWN);

        return bd;

    }

    /**
     * Method that stops the worker as soon as possible.
     *
     *@see  ExecutionDialog
     */
    @Override
    public void requestAbort()
    {
        abortRequested = true;

        logger.debug("AutomataSynthesisWorker requested to stop.");

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
