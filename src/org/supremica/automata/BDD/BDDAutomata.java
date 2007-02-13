
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
import org.supremica.log.*;
import org.supremica.util.SupremicaException;
import java.util.*;
import java.io.*;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;
import org.supremica.util.BDD.PCGNode;
import org.supremica.util.BDD.PCG;
import org.supremica.util.BDD.Options;

import org.supremica.util.BDD.solvers.OrderingSolver;

public class BDDAutomata
    implements Iterable<BDDAutomaton>
{
    private static Logger logger = LoggerFactory.createLogger(BDDAutomata.class);
    
    BDDManager manager;
    Automata theAutomata;
    List<BDDAutomaton> theBDDAutomataList = new LinkedList<BDDAutomaton>();
    Map<Automaton, BDDAutomaton> automatonToBDDAutomatonMap = new HashMap<Automaton, BDDAutomaton>();
    AutomataIndexMap theIndexMap;
    Alphabet observableUnionAlphabet;
 
    BDDTransitions bddTransitions = null; 
               
    BDDDomain eventDomain;
    
    BDDVarSet sourceStateVariables = null;
    BDDVarSet destStateVariables = null;
    
    BDDPairing sourceToDestStatePairing = null;
    BDDPairing destToSourceStatePairing = null;

    BDD initialStatesBDD = null;
    BDD markedStatesBDD = null;
    BDD forbiddenStatesBDD = null;
    BDD uncontrollableStatesBDD = null;
    
    BDDAutomata(Automata orgAutomata)
    {
        AutomataSorter automataSorter = new PCGAutomataSorter();
        theAutomata = automataSorter.sortAutomata(orgAutomata);
        manager = new BDDManager();
  
        try
        {
            theIndexMap = new AutomataIndexMap(theAutomata);
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        
        initialStatesBDD = manager.getOneBDD();
        markedStatesBDD = manager.getOneBDD();
        forbiddenStatesBDD = manager.getZeroBDD();
        uncontrollableStatesBDD = manager.getZeroBDD();
        
        initialize();
    }
 
    BDDManager getBDDManager()
    {
        return manager;
    }
    
    void initialize()
    {
        observableUnionAlphabet = theAutomata.getObservableUnionAlphabet();
        eventDomain = manager.createDomain(observableUnionAlphabet.size());
        
        sourceStateVariables = manager.createEmptyVarSet();
        destStateVariables = manager.createEmptyVarSet();
        
        BDDDomain[] sourceStateDomains = new BDDDomain[theAutomata.size()];
        BDDDomain[] destStateDomains = new BDDDomain[theAutomata.size()];
        
        int i = 0;
        for (Automaton automaton : theAutomata)
        {
            int nbrOfStates = automaton.nbrOfStates();
            //System.err.println("nbrOfStates: " + nbrOfStates);
            BDDDomain sourceStateDomain = manager.createDomain(nbrOfStates);
            BDDDomain destStateDomain = manager.createDomain(nbrOfStates);
            BDDAutomaton bddAutomaton = new BDDAutomaton(this, automaton, sourceStateDomain, destStateDomain);
            bddAutomaton.initialize();
 
            sourceStateVariables.unionWith(sourceStateDomain.set());
            destStateVariables.unionWith(destStateDomain.set());
            
            sourceStateDomains[i] = sourceStateDomain;
            destStateDomains[i] = destStateDomain;
            
            add(bddAutomaton);
                        
            i++;
        }
      
        sourceToDestStatePairing = manager.makePairing(sourceStateDomains, destStateDomains);
        destToSourceStatePairing = manager.makePairing(destStateDomains, sourceStateDomains);       
  
        bddTransitions = new BDDMonolithicTransitions(this);
    }   
  
    public Alphabet getInverseAlphabet(Automaton currAutomaton)
    {
        return theAutomata.getInverseAlphabet(currAutomaton);
    }
        
    void add(BDDAutomaton bddAutomaton)
    {
        theBDDAutomataList.add(bddAutomaton);
        automatonToBDDAutomatonMap.put(bddAutomaton.getAutomaton(), bddAutomaton);
    }
    public Iterator<BDDAutomaton> iterator()
    {
        return theBDDAutomataList.iterator();
    }
    
    public BDDVarSet getEventVarSet()
    {
        return eventDomain.set();
    }
    
    public BDDDomain getEventDomain()
    {
        return eventDomain;
    }
     
    public int getAutomatonIndex(Automaton theAutomaton)
    {
        return theIndexMap.getAutomatonIndex(theAutomaton);
    }
    
    public int getStateIndex(Automaton theAutomaton, State theState)
    {
        return theIndexMap.getStateIndex(theAutomaton, theState);
    }
    
    public int getEventIndex(LabeledEvent theEvent)
    {
        return theIndexMap.getEventIndex(theEvent);
    }
 
        public void addInitialStates(BDD initialStates)
    {
        initialStatesBDD = initialStatesBDD.and(initialStates);
    }
    
    public void addMarkedStates(BDD markedStates)
    {
        markedStatesBDD = markedStatesBDD.and(markedStates);
    }
    
    public void addForbiddenStates(BDD forbiddenStates)
    {
        forbiddenStatesBDD = forbiddenStatesBDD.or(forbiddenStates);
    }
    
    public void addUncontrollableStates(BDD uncontrollableStates)
    {
        uncontrollableStatesBDD = uncontrollableStatesBDD.and(uncontrollableStates);
    }
    
    public double numberOfReachableStates()
    {
        BDD reachableStatesBDD = manager.reachableStates(initialStatesBDD, bddTransitions.getMonolithicTransitionForwardBDD(), sourceStateVariables, destToSourceStatePairing);
        return reachableStatesBDD.satCount(sourceStateVariables);
    }
    
    public double numberOfCoreachableStates()
    {
//        System.out.println("initialStates BDD: " + initialStatesBDD.toStringWithDomains());
//        System.out.println("markedStates BDD: " + markedStatesBDD.toStringWithDomains());
        
        BDD coreachableStatesBDD = manager.coreachableStates(markedStatesBDD, bddTransitions.getMonolithicTransitionBackwardBDD(), sourceStateVariables, destToSourceStatePairing);
        return coreachableStatesBDD.satCount(sourceStateVariables);
    }
    
    public double numberOfReachableAndCoreachableStates()
    {
        BDD reachableStatesBDD = manager.reachableStates(initialStatesBDD, bddTransitions.getMonolithicTransitionForwardBDD(), sourceStateVariables, destToSourceStatePairing);
        BDD coreachableStatesBDD = manager.coreachableStates(markedStatesBDD, bddTransitions.getMonolithicTransitionBackwardBDD(), sourceStateVariables, destToSourceStatePairing);
        
        BDD reachableAndCoreachableStatesBDD = reachableStatesBDD.and(coreachableStatesBDD);
        return reachableAndCoreachableStatesBDD.satCount(sourceStateVariables);
    }
    
    public boolean isNonblocking()
    {
        BDD reachableStatesBDD = manager.reachableStates(initialStatesBDD, bddTransitions.getMonolithicTransitionForwardBDD(), sourceStateVariables, destToSourceStatePairing);
        BDD coreachableStatesBDD = manager.coreachableStates(markedStatesBDD, bddTransitions.getMonolithicTransitionBackwardBDD(), sourceStateVariables, destToSourceStatePairing);
        BDD impBDD = reachableStatesBDD.imp(coreachableStatesBDD);
        return impBDD.equals(manager.getOneBDD());
    }
    
    boolean isControllable()
    {
        return false;
    }
    
    boolean isNonblockingAndControllable()
    {
        return false;
    }
    
}
