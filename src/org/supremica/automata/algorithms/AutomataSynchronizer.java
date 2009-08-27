
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
import org.supremica.util.SupremicaException;

import java.util.*;
import net.sourceforge.waters.model.module.SimpleComponentProxy;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.base.ComponentKind;

public class AutomataSynchronizer
    implements Stoppable
{
    private static Logger logger = LoggerFactory.createLogger(AutomataSynchronizer.class);
    private Automata theAutomata;
    private AutomataSynchronizerHelper synchHelper;
    private SynchronizationOptions syncOptions;
    private ArrayList<AutomataSynchronizerExecuter> synchronizationExecuters;
    private AutomataIndexMap indexMap;
    
    // For stopping execution
    private boolean stopRequested = false;


    private HashMap<Arc,EdgeSubject> arc2edgeTable[];
    private HashMap<String,Integer> autName2indexTable = new HashMap<String, Integer>();
    private int automatonIndex = 0;


    public AutomataSynchronizer(ListSubject<AbstractSubject> components, SynchronizationOptions options)
    {
        Automata automata = removeGuardsActionsFromEFAs(components);
        this.theAutomata = automata;
        this.syncOptions = options;
        synchHelper = new AutomataSynchronizerHelper(automata, options,arc2edgeTable,autName2indexTable);

        initialize();
    }

    public AutomataSynchronizer(Automata automata, SynchronizationOptions options)
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
    {
        this.theAutomata = helper.getAutomata();
        this.syncOptions = helper.getSynchronizationOptions();
        synchHelper = helper;
        
        initialize();
    }
    
    /**
     * Initializes the AutomataSynchronizerExecuter:s based on the AutomataSynchronizerHelper.
     */
    public void initialize()
    {
        // Allocate and initialize the synchronizationExecuters
        int nbrOfExecuters = syncOptions.getNbrOfExecuters();
        synchronizationExecuters = new ArrayList<AutomataSynchronizerExecuter>(nbrOfExecuters);
        for (int i = 0; i < nbrOfExecuters; i++)
        {
            AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(synchHelper);
            synchronizationExecuters.add(currSynchronizationExecuter);
        }
  
        indexMap = synchHelper.getIndexMap();    
    }
    
    public void execute()
    {
        State currInitialState;
        int[] initialState = AutomataIndexFormHelper.createState(theAutomata.size());
        
        // Build the initial state - and the comment
        Iterator<Automaton> autIt = theAutomata.iterator();
        StringBuffer comment = new StringBuffer();
        
        // Set an apropriate comment on the automaton
        while (autIt.hasNext())
        {
            Automaton currAutomaton = autIt.next();
            
            currInitialState = currAutomaton.getInitialState();
            initialState[indexMap.getAutomatonIndex(currAutomaton)] = indexMap.getStateIndex(currAutomaton, currInitialState);
            
            comment.append(currAutomaton.getName());
            comment.append(syncOptions.getAutomatonNameSeparator());
        }
        comment.delete(comment.length() - syncOptions.getAutomatonNameSeparator().length(), comment.length());
        try {
			synchHelper.addState(initialState);
		} catch (SupremicaException e1) {
			throw new RuntimeException(e1);
		}
        synchHelper.addComment(comment.toString());
        
        // Start all the synchronization executers and wait for completetion
        for (AutomataSynchronizerExecuter synchExecuter : synchronizationExecuters)
        {
            synchExecuter.start();
        }
        
        // Wait for completion
        
        try {
			for (AutomataSynchronizerExecuter synchExecuter : synchronizationExecuters) {
			 	synchExecuter.join();
			}
		} catch (InterruptedException e) {
			// Current thread has been interrupted, perhaps
			// due to an exception in one of the executers.
			// Stop all tasks and throw the original exception
			for (AutomataSynchronizerExecuter synchExecuter : synchronizationExecuters) {
	            synchExecuter.requestStop();
	        }
			for (AutomataSynchronizerExecuter synchExecuter : synchronizationExecuters) {
			 	Throwable cause = synchExecuter.getCauseOfInterrupt();
			 	if (cause != null) {
			 		if (cause instanceof RuntimeException) throw (RuntimeException) cause;
			 		else throw new RuntimeException(cause);
			 	}
			}
		}
    }
    
    public void displayInfo()
    {
        synchHelper.printStatistics();
    }
    
    // -- MF -- Added to allow users easy access to the number of synch'ed states
    public long getNumberOfStates()
    {
        return synchHelper.getNumberOfAddedStates();
    }
    
    public Automaton getAutomaton()
    {
        AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(0);
        
        if (currExec.buildAutomaton())
        {
            return synchHelper.getAutomaton();
        }
        else
        {
            return null;
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
        syncOptions = null;
        synchronizationExecuters.clear();
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

    public SimpleComponentProxy getSynchronizedComponent()
    {
        SynchronizationOptions options = SynchronizationOptions.getDefaultSynchronizationOptions();
        AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(theAutomata, options, arc2edgeTable,autName2indexTable);
        AutomataSynchronizer synchronizer = new AutomataSynchronizer(helper);
        synchronizer.execute();
        synchronizer.getAutomaton();
        helper.createExtendedAutomaton();
        return helper.getSynchronizedComponent();
    }

    public Automaton removeGuardsActionsFromEFA(SimpleComponentSubject component)
    {
        Automaton automaton = new Automaton(component.getName());

        if(component.getKind() == ComponentKind.PLANT)
            automaton.setType(AutomatonType.PLANT);
        if(component.getKind() == ComponentKind.SPEC)
            automaton.setType(AutomatonType.SPECIFICATION);
        if(component.getKind() == ComponentKind.SUPERVISOR)
            automaton.setType(AutomatonType.SUPERVISOR);
        if(component.getKind() == ComponentKind.PROPERTY)
            automaton.setType(AutomatonType.PROPERTY);

        State fromState , toState;
        LabeledEvent event;
        boolean initialFlag = true;
        for(EdgeSubject edge : component.getGraph().getEdgesModifiable())
        {
            fromState = automaton.getStateWithName(edge.getSource().getName());
            if(fromState == null)
            {
                fromState = new State(edge.getSource().getName());
                if(initialFlag && edge.getSource().toString().contains("initial"))
                {
                    fromState.setInitial(true);
                    initialFlag = false;
                }
                if(edge.getSource().toString().contains("accepting"))
                {
                    fromState.setAccepting(true);
                }
                automaton.addState(fromState);
            }
            toState = automaton.getStateWithName(edge.getTarget().getName());
            if(toState == null)
            {
                toState = new State(edge.getTarget().getName());
                if(edge.getTarget().toString().contains("accepting"))
                {
                    toState.setAccepting(true);
                }
                automaton.addState(toState);
            }

            ListSubject<AbstractSubject> eventList = edge.getLabelBlock().getEventListModifiable();
            for(AbstractSubject e:eventList)
            {
//                EventDeclSubject eventSubject = (EventDeclSubject)e;
//                SimpleComponentSubject eventSubject = (SimpleComponentSubject)e;
                SimpleIdentifierSubject eventSubject = (SimpleIdentifierSubject)e;
                event = automaton.getAlphabet().getEvent(eventSubject.getName());
                if(event == null)
                {
                    event = new LabeledEvent(eventSubject.getName());
                    automaton.getAlphabet().add(event);
                }
/*                if(eventSubject.getKind() == EventKind.CONTROLLABLE)
                {
                    event.setControllable(true);
                }
                else
                {
                    event.setControllable(false);
                }
 */
                Arc currArc = new Arc(fromState, toState, event);
                arc2edgeTable[automatonIndex].put(currArc, edge);
                automaton.addArc(currArc);
            }

        }

        return automaton;
    }

    public Automata removeGuardsActionsFromEFAs(ListSubject<AbstractSubject> components)
    {
        Automata automata = new Automata();
        HashSet<SimpleComponentSubject> autComps = new HashSet<SimpleComponentSubject>();

        for(AbstractSubject component : components)
            if(component.toString().contains("NODES") && component.toString().contains("EDGES"))
                autComps.add((SimpleComponentSubject)component);

        arc2edgeTable = new HashMap[autComps.size()];

        for(SimpleComponentSubject autComp: autComps)
        {
            autName2indexTable.put(autComp.getName(), automatonIndex);
            arc2edgeTable[automatonIndex] = new HashMap<Arc, EdgeSubject>();
            automata.addAutomaton(removeGuardsActionsFromEFA(autComp));
            automatonIndex++;
        }

        return automata;
    }
}
