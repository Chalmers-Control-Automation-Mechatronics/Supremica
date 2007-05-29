
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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

import java.awt.*;
import javax.swing.*;
import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.util.ActionTimer;
import org.supremica.gui.ide.IDEReportInterface;

/**
 * Thread dealing with synthesis.
 *
 *@author  hugo
 *@since November 18, 2004
 */
public class AutomataSynthesisWorker
    extends Thread
    implements Stoppable
{
    private static Logger logger = LoggerFactory.createLogger(AutomataSynthesisWorker.class);
    
    private IDEReportInterface gui;
    private Automata theAutomata;
    private SynthesizerOptions synthOptions;
    
    private ExecutionDialog executionDialog;
    private boolean stopRequested = false;
    private EventQueue eventQueue = new EventQueue();
    
    public AutomataSynthesisWorker(IDEReportInterface gui, Automata theAutomata, SynthesizerOptions options)
    {
        this.gui = gui;
        this.theAutomata = theAutomata;
        this.synthOptions = options;
        
        this.start();
    }
    
    public void run()
    {
        // Initialize the ExecutionDialog
        ArrayList threadsToStop = new ArrayList();
        threadsToStop.add(this);
        executionDialog = new ExecutionDialog(gui.getFrame(), "Synthesizing", threadsToStop);
        executionDialog.setMode(ExecutionDialogMode.SYNTHESIZING);
        
        // OK options?
        String errorMessage = synthOptions.validOptions();
        if (errorMessage != null)
        {
            JOptionPane.showMessageDialog(gui.getFrame(), errorMessage, "Alert", JOptionPane.ERROR_MESSAGE);
            requestStop();
            return;
        }
        
        // Timer
        ActionTimer timer = new ActionTimer();
        timer.start();
        
        // Do the work!!
        Automata result = new Automata();
        // Are there many or just one automaton?
        if (theAutomata.size() > 1)
        {
            // Get default synchronization options
            SynchronizationOptions syncOptions = SynchronizationOptions.getDefaultSynthesisOptions();
                 
            try
            {
                AutomataSynthesizer synthesizer = new AutomataSynthesizer(theAutomata, syncOptions,
                    synthOptions);
                synthesizer.setExecutionDialog(executionDialog);
                threadsToStop.add(synthesizer);
                result.addAutomata(synthesizer.execute());
                threadsToStop.remove(synthesizer);
            }
            catch (Exception ex)
            {
                logger.error("Exception in AutomataSynthesisWorker. " + ex);
                logger.debug(ex.getStackTrace());
            }
        }
        else  // Single automaton
        {
            // Make copy
            Automaton theAutomaton = new Automaton(theAutomata.getFirstAutomaton());
            
            try
            {
                // ARASH: this is IDIOTIC! why didn't we prepare for more than one monolithc algorithm???
                // (this is a dirty fix, should use a factory instead)
                //AutomatonSynthesizer synthesizer = (options.getSynthesisAlgorithm() == SynthesisAlgorithm.MonolithicSingleFixpoint)
                //? new AutomatonSynthesizerSingleFixpoint(theAutomaton, options)
                //: new AutomatonSynthesizer(theAutomaton, options);
                AutomatonSynthesizer synthesizer = new AutomatonSynthesizer(theAutomaton, synthOptions);
                threadsToStop.add(synthesizer);
                synthesizer.synthesize();
                result.addAutomaton(synthesizer.getAutomaton());
                threadsToStop.remove(synthesizer);
            }
            catch (Exception ex)
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
            public void run()
            {
                if (executionDialog != null)
                {
                    executionDialog.setMode(ExecutionDialogMode.HIDE);
                }
            }
        });
        
        // Present result
        if (!stopRequested)
        {
            logger.info("Synthesis completed after " + timer.toString() + ".");
            
            // Add new automata
            try
            {
                gui.addAutomata(result);
            }
            catch (Exception ex)
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
        }
    }
    
    /**
     * Does the actual work.
     *
     * @return The brand new supervisors.     .
     */
    private Automata work()
    {
        Automata result = new Automata();
        
        // Are there many or just one automaton?
        if (theAutomata.size() > 1)
        {
            // Get default synchronization options
            SynchronizationOptions syncOptions = SynchronizationOptions.getDefaultSynthesisOptions();
            
            try
            {
                AutomataSynthesizer synthesizer = new AutomataSynthesizer(theAutomata, syncOptions,
                    synthOptions);
                result.addAutomata(synthesizer.execute());
            }
            catch (Exception ex)
            {
                logger.error("Exception in AutomataSynthesisWorker. " + ex);
                logger.debug(ex.getStackTrace());
            }
        }
        else    // single automaton selected
        {
            Automaton theAutomaton = theAutomata.getFirstAutomaton();
            
            try
            {
                // ARASH: this is IDIOTIC! why didnt we prepare for more than one monolithc algorithm???
                // (this is a dirty fix, should use a factory instead)
                //AutomatonSynthesizer synthesizer = (options.getSynthesisAlgorithm() == SynthesisAlgorithm.MonolithicSingleFixpoint)
                //? new AutomatonSynthesizerSingleFixpoint(theAutomaton, options)
                //: new AutomatonSynthesizer(theAutomaton, options);
                AutomatonSynthesizer synthesizer = new AutomatonSynthesizer(theAutomaton, synthOptions);
                    
                // AutomatonSynthesizer synthesizer = new AutomatonSynthesizer(theAutomaton,synthesizerOptions);
                synthesizer.synthesize();
            }
            catch (Exception ex)
            {
                logger.error("Exception in AutomataSynthesisWorker. Automaton: " + theAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
            }
        }
        
        return result;
    }
    
    /**
     * Method that stops the worker as soon as possible.
     *
     *@see  ExecutionDialog
     */
    public void requestStop()
    {
        stopRequested = true;
        
        logger.debug("AutomataSynthesisWorker requested to stop.");
        
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
