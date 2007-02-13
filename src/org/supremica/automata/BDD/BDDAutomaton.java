
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
package org.supremica.automata.BDD;

import net.sf.javabdd.*;
import java.util.*;
import org.supremica.automata.*;

public class BDDAutomaton
{
    Automaton theAutomaton;
    BDDAutomata bddAutomata;
    BDDManager manager;
    
    BDDDomain sourceStateDomain;
    BDDDomain destStateDomain;
    BDDPairing sourceToDestPairing;
    BDDPairing destToSourcePairing;
    BDD transitionForwardBDD;
    BDD transitionBackwardBDD;
    BDD transitionForwardDisjunctiveBDD;
    BDD transitionBackwardDisjunctiveBDD;
    
    BDDAutomaton(BDDAutomata bddAutomata, Automaton theAutomaton, BDDDomain sourceStateDomain, BDDDomain destStateDomain)
    {
        this.manager = bddAutomata.getBDDManager();
        
        this.bddAutomata = bddAutomata;
        this.theAutomaton = theAutomaton;
        
        this.sourceStateDomain = sourceStateDomain;
        this.destStateDomain = destStateDomain;
        
        sourceToDestPairing = manager.makePairing(sourceStateDomain, destStateDomain);
        destToSourcePairing = manager.makePairing(destStateDomain, sourceStateDomain);
        
        transitionForwardBDD = manager.getZeroBDD();
        transitionBackwardBDD = manager.getZeroBDD();
        
        transitionForwardDisjunctiveBDD = manager.getZeroBDD();
        transitionBackwardDisjunctiveBDD = manager.getZeroBDD();
    }
    
    public Automaton getAutomaton()
    {
        return theAutomaton;
    }
    
    public void initialize()
    {
        BDD initialStates = manager.getZeroBDD();
        
        BDD markedStates = manager.getZeroBDD();
        BDD forbiddenStates = manager.getZeroBDD();
        
        Alphabet inverseAlphabet = bddAutomata.getInverseAlphabet(theAutomaton);
        
        for (State currState : theAutomaton)
        {
            // First create all transitions in this automaton
            for (Iterator<Arc> arcIt = currState.outgoingArcsIterator(); arcIt.hasNext(); )
            {
                Arc currArc = arcIt.next();
                addTransition(currArc);
            }
            
            // Self loop events not in this alphabet
            for (LabeledEvent event : inverseAlphabet)
            {
                addTransition(currState, currState, event);
            }
            
            // Then add state properties
            int stateIndex = bddAutomata.getStateIndex(theAutomaton, currState);
            if (currState.isInitial())
            {
               BDDManager.addState(initialStates, stateIndex, sourceStateDomain);
            }
            if (currState.isAccepting())
            {
                BDDManager.addState(markedStates, stateIndex, sourceStateDomain);
            }
            if (currState.isForbidden())
            {
               BDDManager.addState(forbiddenStates, stateIndex, sourceStateDomain);
            }
        }
        
        bddAutomata.addInitialStates(initialStates);
        bddAutomata.addMarkedStates(markedStates);
        bddAutomata.addForbiddenStates(forbiddenStates);
    }
    
    void addTransition(Arc theArc)
    {
        State sourceState = theArc.getSource();
        State destState = theArc.getTarget();
        LabeledEvent theEvent = theArc.getEvent();
        
        // Add all states that could be reach by only unobservable events including the destState
        for (State epsilonState : destState.epsilonClosure(true))
        {
            addTransition(sourceState, epsilonState, theEvent);
        }
    }
    
    void addTransition(State sourceState, State destState, LabeledEvent theEvent)
    {
        int sourceStateIndex = bddAutomata.getStateIndex(theAutomaton, sourceState);
        int destStateIndex = bddAutomata.getStateIndex(theAutomaton, destState);
        int eventIndex = bddAutomata.getEventIndex(theEvent);
        BDDManager.addTransition(transitionForwardBDD, sourceStateIndex, sourceStateDomain, destStateIndex, destStateDomain, eventIndex, bddAutomata.getEventDomain());
        BDDManager.addTransition(transitionBackwardBDD, destStateIndex, sourceStateDomain, sourceStateIndex, destStateDomain, eventIndex, bddAutomata.getEventDomain());
    }
    
    
    public int hashCode()
    {
        return theAutomaton.hashCode();
    }
    
    public boolean equals(Object other)
    {
        return theAutomaton.equals(other);
    }
    
    public BDDDomain getSourceStateDomain()
    {
        return sourceStateDomain;
    }
    
    public BDDDomain getDestStateDomain()
    {
        return destStateDomain;
    }
    
    public BDDPairing getSourceToDestPairing()
    {
        return sourceToDestPairing;
    }
    
    public BDDPairing getDestToSourcePairing()
    {
        return destToSourcePairing;
    }
    
    public BDD getTransitionForwardBDD()
    {
        return transitionForwardBDD;
    }
    
    public BDD getTransitionBackwardBDD()
    {
        return transitionBackwardBDD;
    }
    
    public BDD getTransitionForwardConjunctiveBDD()
    {
        return getTransitionForwardBDD();
    }
    
    public BDD getTransitionBackwardConjunctiveBDD()
    {
        return getTransitionBackwardBDD();
    }
    
    public BDD getTransitionForwardDisjunctiveBDD(Alphabet sourceAlphabet)
    {
/*
        BDD initialStates = manager.zero();
 
        BDD markedStates = manager.zero();
        BDD forbiddenStates = manager.zero();
 
        Alphabet intersectAlphabet = new Alphabet(theAutomaton.getAlphabet());
        Alphabet selfLoopAlphabet =
 
        intersectAlphabet.intersect(sourceAlphabet);
        // The events in intersectAlphabet are the real events
        // all other events should be self-looped
 
        for (State currState : theAutomaton)
        {
            // First create all transitions in this automaton
            for (Iterator<Arc> arcIt = currState.outgoingArcsIterator(); arcIt.hasNext(); )
            {
                Arc currArc = arcIt.next();
 
                addTransition(currArc);
            }
 
            // Then add state properties
            int stateIndex = manager.getStateIndex(theAutomaton, currState);
            if (currState.isInitial())
            {
                manager.addState(initialStates, stateIndex, sourceStateDomain);
            }
            if (currState.isAccepting())
            {
                manager.addState(markedStates, stateIndex, sourceStateDomain);
            }
            if (currState.isForbidden())
            {
                manager.addState(forbiddenStates, stateIndex, sourceStateDomain);
            }
        }
 
        //manager.addInitialStates(initialStates);
        //manager.addMarkedStates(markedStates);
        //manager.addForbiddenStates(forbiddenStates);
 */
        return null;
    }
    
    public BDD getTransitionForwardDisjunctiveBDD()
    {
        return null;
    }
    
    public BDD getTransitionBackwardDisjunctiveBDD()
    {
        return null;
    }
}
