
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
package org.supremica.automata.algorithms;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.Stoppable;
import org.supremica.log.*;
import java.util.*;

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
    
    public AutomataSynchronizer(Automata automata, SynchronizationOptions options)
    throws Exception
    {
        this.theAutomata = automata;
        this.syncOptions = options;
        synchHelper = new AutomataSynchronizerHelper(automata, options);
        
        initialize();
    }
    
    /**
     * Creates an AutomataSynchronizer based on an already existing helper.
     */
    public AutomataSynchronizer(AutomataSynchronizerHelper helper)
    throws Exception
    {
        this.theAutomata = helper.getAutomata();
        this.syncOptions = helper.getSynchronizationOptions();
        synchHelper = helper;
        
        initialize();
    }
    
    /**
     * Initializes the AutomataSynchronizerExecuter:s based on the AutomataSynchronizerHelper.
     */
    private void initialize()
    {
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
        
        // Set an apropriate comment on the automaton
        while (autIt.hasNext())
        {
            Automaton currAutomaton = (Automaton) autIt.next();
            
            currInitialState = currAutomaton.getInitialState();
            initialState[currAutomaton.getIndex()] = currInitialState.getIndex();
            
            comment.append(currAutomaton.getName());
            comment.append(syncOptions.getAutomatonNameSeparator());
        }
        comment.delete(comment.length() - syncOptions.getAutomatonNameSeparator().length(), comment.length());
        synchHelper.addState(initialState);
        synchHelper.addComment(comment.toString());
        
        // Start all the synchronization executers and wait for completetion
        for (int i = 0; i < synchronizationExecuters.size(); i++)
        {
            AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);
            
            currExec.start();
        }
        
        // Wait for completion
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
    
    /**
     * Help the garbage collector by clearing variables.
     */
    public void clear()
    {
        theAutomata = null;
        synchHelper = null;
        for (int i = 0; i < synchronizationExecuters.size(); i++)
        {
            AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);
            currExec = null;
        }
        syncOptions = null;
        synchronizationExecuters = null;
    }
    
    public void requestStop()
    {
        stopRequested = true;
        
        for (int i = 0; i < synchronizationExecuters.size(); i++)
        {
            ((AutomataSynchronizerExecuter) synchronizationExecuters.get(i)).requestStop();
        }
    }
    
    public boolean isStopped()
    {
        return stopRequested;
    }
    
    /**
     * Method for synchronizing Automata with default options.
     *
     * @param automata the Automata to be synchronized.
     * @return Automaton representing the synchronous composition.
     */
    public static Automaton synchronizeAutomata(Automata automata)
    throws Exception
    {
        SynchronizationOptions options = SynchronizationOptions.getDefaultSynchronizationOptions();
        return synchronizeAutomata(automata, options);
    }
    
    /**
     * Method for synchronizing Automata with supplied options.
     *
     * @param automata the Automata to be synchronized.
     * @param options the SynchronizationOptions that should be used.
     * @return Automaton representing the synchronous composition.
     */
    public static Automaton synchronizeAutomata(Automata automata, SynchronizationOptions options)
    throws Exception
    {
        AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(automata, options);
        return synchronizeAutomata(helper);
    }
    
    /**
     * Method for synchronizing Automata based on an already existing AutomataSynchronizerHelper.
     * The helper includes the options and the automata to be composed!
     *
     * @param helper the AutomataSynchronizerHelper to be used.
     * @return Automaton representing the synchronous composition.
     */
    public static Automaton synchronizeAutomata(AutomataSynchronizerHelper helper)
    throws Exception
    {
        AutomataSynchronizer synchronizer = new AutomataSynchronizer(helper);
        synchronizer.execute();
        Automaton result = synchronizer.getAutomaton();
        synchronizer.clear();
        
        return result;
    }
}
