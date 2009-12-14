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

import java.util.ArrayList;
import java.util.Iterator;

import org.supremica.log.*;
import org.supremica.automata.Arc;
import org.supremica.automata.State;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.ForbiddenEvent;
import org.supremica.automata.Project;

/**
 * For forbidding of undesirable sub-states
 * Adds self-loops to the Automata given in class ModularForbidderInput
 * No clone is performed, events are added to given automata
 * MPZS - Multiple plant zero specification
 * MPSS - Multiple plant single specification
 * MPMS - Multiple plant multiple specification
 * @author patrik
 * @since December 10, 2009
 */
public class ModularForbidder
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.createLogger(ModularForbidder.class);

    private Project project;

    private Automata givenAutomata; //Automata that exists
    private Automata createdAutomata; //Automata that are created

    private ArrayList<ModularForbidderInput.SubState> ss;
    private ArrayList<ForbiddenEvent> alphaEvent;
    private ModularForbidderInput.SubState subState;

    private String extensionPrefix = "@ "; //Prefic used in names/labels for new events and automata

    /**
     * The constructor is the only method called from the outside
     * @param input This class is based on data according to this structure
     * @param project
     */
    public ModularForbidder(ModularForbidderInput input, Project project)
    {
        init(input, project);
    }
    
    private void init(ModularForbidderInput input, Project project)
    {
        this.project = project;

        givenAutomata = input.getTotalAutomata();
        createdAutomata = new Automata();
        ss = input.getSubStates(); //ArrayList<SubState>
        alphaEvent = new ArrayList<ForbiddenEvent>();
    }

    /**
     * Mapping of undesirable sub-states, given in class ModularForbidderInput,
     * with specifications that exists
     * @return Created automata (specifications (and supervisors))
     */
    public Automata execute()
    {
        //Self-loop extension manager
        extensionManager();
      
        //Perform PSC if MPMS
        possibleSynkOfSpecifications();

        //All events can now be prioritized for later synthesis
        restorePriority();
        
        /*-----------------------------------
         * All subStates are now of type MPSP
         * --------------------------------*/

        //Create supervisors for the sub-specifications
        //createSupervisors();
        
        return createdAutomata;
    }
    
    /**
     * Handles all local-states
     * Each automaton is extended with right self-loop event(s).
     */
    private void extensionManager()
    {
        //Go through all sub-states
        Iterator<ModularForbidderInput.SubState> ssit = ss.iterator();
        while(ssit.hasNext())
        {
            subState = ssit.next();
            ArrayList<ModularForbidderInput.LocalState> ls = subState.getLocalStates();

            // Create single project-global unique forbidden event
            ForbiddenEvent x_event = new ForbiddenEvent(project.getUniqueEventLabel(extensionPrefix));
            x_event.setControllable(false);
            x_event.setPrioritized(false); //If we need synk between sub-specificatinos later
            alphaEvent.add(x_event);
            logger.debug(x_event.getLabel());

            //Go through all local-states for current sub-state
            Iterator<ModularForbidderInput.LocalState> lsit = ls.iterator();
            while(lsit.hasNext())
            {
                ModularForbidderInput.LocalState localState = lsit.next();

                //Add self-loop with right event to current local-state
                extendAutomaton(localState.getAutomaton(),localState.getState(), x_event);
            }

            //MPZS
            if(subState.getAutomataInSubState().getSpecificationAutomata().nbrOfAutomata() == 0)
            {
                //create a new specification if this sub-state only has sub-plants
                createSpecification(x_event);
            }

            //set Priority of x_event for MPZS and MPSS for later synchronization
            if(subState.getAutomataInSubState().getSpecificationAutomata().nbrOfAutomata() <= 1)
            {
                x_event.setPrioritized(true);
            }
        }
    }    
    
    /**
     * Handle difference between how plant and specification automaton should be extended with self-loops
     * @param automaton
     * @param state
     * @param x_event
     */
    private void extendAutomaton(Automaton automaton, State state, ForbiddenEvent x_event)
    {
        // Add the event - beware, adding an existig event throws exception
        if(automaton.getAlphabet().contains(x_event) == false)
        {
            automaton.getAlphabet().addEvent(x_event);
        }

        //Add to state if plant, add to all other states if specification
        if(automaton.getType() == AutomatonType.PLANT)
        {
            addSelfLoop(automaton,state,x_event);
            logger.info(automaton.getName()+" is extended");
        }
        else
        {
            Iterator<State> it = automaton.stateIterator();
            while(it.hasNext())
            {
                State currentState = it.next();
                if(!currentState.equalState(state))
                {
                    addSelfLoop(automaton,currentState,x_event);
                }
            }
            logger.info(automaton.getName()+" is extended");
        } 
    }
    
    private void addSelfLoop(Automaton automaton, State state, ForbiddenEvent event)
    {
        automaton.addArc(new Arc(state, state, event));
    }
    
    /**
     * Creates a new specification with one state and one event in alphabet
     * The event is blocked from occurring
     * @param x_event
     */
    private void createSpecification(ForbiddenEvent x_event)
    {
        Automaton spec = new Automaton(x_event.getLabel()); // same name as the event-label
        spec.setType(AutomatonType.SPECIFICATION);
        spec.getAlphabet().addEvent(x_event);
        State init_state = new State("q0");
        init_state.setInitial(true);
        init_state.setAccepting(true);
        spec.addState(init_state);
        logger.info(spec.getName()+" created for event "+x_event.getLabel());
        createdAutomata.addAutomaton(spec);
    }
    
    /**
     * Prioritized synchronization of multiple sub-specifications in sub-state
     * All events are prioritized besides the event connected to the sub-state
     */
    private void possibleSynkOfSpecifications()
    {    
        // Get the initial options
        SynchronizationOptions synchronizationOptions;
        try
        {
            synchronizationOptions = new SynchronizationOptions();
        }
        catch (Exception ex)
        {
            logger.error("Error constructing synchronizationOptions: " + ex.getMessage());
            logger.debug(ex.getStackTrace());
            return;
        }
        
        Iterator<ModularForbidderInput.SubState> it = ss.iterator();
        while(it.hasNext())
        {
            subState = it.next();
            if(subState.getAutomataInSubState().getSpecificationAutomata().nbrOfAutomata() > 1)
            {
                AutomataSynchronizer PSCspec;
                PSCspec = new AutomataSynchronizer(subState.getAutomataInSubState().getSpecificationAutomata(),synchronizationOptions);
                PSCspec.execute();
                
                Automaton newSpec = PSCspec.getAutomaton();
                newSpec.setName(extensionPrefix+newSpec.getName());
                createdAutomata.addAutomaton(newSpec);
            }
        }
    }
    
    /**
     * In order to restore priority after PSC
     */
    private void restorePriority()
    {
        //All x_event have Prioritized(true) from now on
        Iterator<ForbiddenEvent> it = alphaEvent.iterator();
        while(it.hasNext())
        {
            it.next().setPrioritized(true);
        }
    }

    /**
     * To create supervisor for each sub-specification
     */
    private void createSupervisors()
    {
        // Get the initial options
        SynthesizerOptions synthesizerOptions;
        try
        {
            synthesizerOptions = new SynthesizerOptions();
        }
        catch (Exception ex)
        {
            logger.error("Error constructing synthesizerOptions: " + ex.getMessage());
            logger.debug(ex.getStackTrace());
            return;
        }
        
        // Get the initial options
        SynchronizationOptions synchronizationOptions;
        try
        {
            synchronizationOptions = new SynchronizationOptions();
        }
        catch (Exception ex)
        {
            logger.error("Error constructing synchronizationOptions: " + ex.getMessage());
            logger.debug(ex.getStackTrace());
            return;
        }

        //create supervisor
        Automata sup;
        Automata allAutomata = givenAutomata;
        allAutomata.addAutomata(createdAutomata);
        AutomataSynthesizer supSynth = new AutomataSynthesizer(allAutomata, synchronizationOptions, synthesizerOptions); 
        try
        {
            sup = supSynth.execute();
            createdAutomata.addAutomata(sup);
        }
        catch (Exception ex)
        {
            logger.error("Error constructing sup: " + ex.getMessage());
            logger.debug(ex.getStackTrace());
            return;
        }
    }   
}