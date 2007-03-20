
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
import java.math.BigInteger;
import org.supremica.util.ArrayHelper;
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
    Alphabet unionAlphabet;
 
    BDDTransitions bddTransitions = null; 
               
    BDDDomain eventDomain;
    BDDDomain[] sourceStateDomains = null;
    BDDDomain[] destStateDomains = null;
        
    BDDVarSet sourceStateVariables = null;
    BDDVarSet destStateVariables = null;
    
    BDDPairing sourceToDestStatePairing = null;
    BDDPairing destToSourceStatePairing = null;

    BDD initialStatesBDD = null;
    BDD markedStatesBDD = null;
    BDD forbiddenStatesBDD = null;
    BDD uncontrollableStatesBDD = null;
    
    BDD reachableStatesBDD = null;
    BDD coreachableStatesBDD = null;
    BDD reachableAndCoreachableStatesBDD = null;
    
    double nbrOfReachableStates = -1;
    double nbrOfCoreachableStates = -1;
    double nbrOfReachableAndCoreachableStates = -1;
    double nbrOfBlockingStates = -1;
    
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
    
    public Automata getAutomata()
    {
        return theAutomata;
    }
    
    public BDDAutomaton getBDDAutomaton(Automaton theAutomaton)
    {
        return automatonToBDDAutomatonMap.get(theAutomaton);
    }
    
    void initialize()
    {
        unionAlphabet = theAutomata.getUnionAlphabet();
        eventDomain = manager.createDomain(unionAlphabet.size());
        eventDomain.setName("Events");
        
        sourceStateVariables = manager.createEmptyVarSet();
        destStateVariables = manager.createEmptyVarSet();
        
        sourceStateDomains = new BDDDomain[theAutomata.size()];
        destStateDomains = new BDDDomain[theAutomata.size()];
        
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
            sourceStateDomains[i].setName(automaton.getName());
            destStateDomains[i] = destStateDomain;
            destStateDomains[i].setName(automaton.getName());           
            
            add(bddAutomaton);
                        
            i++;
        }
      
        sourceToDestStatePairing = manager.makePairing(sourceStateDomains, destStateDomains);
        destToSourceStatePairing = manager.makePairing(destStateDomains, sourceStateDomains);       
  
        bddTransitions = new BDDTransitionFactory(this).createTransitions();
    }   
    
    public void done()
    {
        if (manager != null)
        {
            manager.done();
        }
    }
  
    protected void finalize()
    {
        done();
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
        if (nbrOfReachableStates < 0)
        {
            getReachableStates();
        }
        return nbrOfReachableStates;
    }
    
    public double numberOfCoreachableStates()
    {
        if (nbrOfCoreachableStates < 0)
        {
            getCoreachableStates();
        }     
        return nbrOfCoreachableStates;
    }
    
    public double numberOfBlockingStates()
    {
        if (nbrOfBlockingStates < 0)
        {
            getReachableAndCoreachableStates();
        }     
        return nbrOfBlockingStates;        
    }
    
    public double numberOfReachableAndCoreachableStates()
    {
        if (nbrOfReachableAndCoreachableStates < 0)
        {
            getReachableAndCoreachableStates();
        }     
        return nbrOfReachableAndCoreachableStates;   
    }
    
    public boolean isNonblocking()
    {
        BDD reachableStatesBDD = getReachableStates();
        BDD coreachableStatesBDD = getCoreachableStates();
        BDD impBDD = reachableStatesBDD.imp(coreachableStatesBDD);
        return impBDD.equals(manager.getOneBDD());
    }
    
    BDD getReachableStates()
    {
        if (reachableStatesBDD == null)
        {
            reachableStatesBDD = manager.reachableStates(initialStatesBDD, bddTransitions, sourceStateVariables, eventDomain.set(), destToSourceStatePairing);
            nbrOfReachableStates = reachableStatesBDD.satCount(sourceStateVariables);              
        }
        return reachableStatesBDD;
    }
 
    BDD getCoreachableStates()
    {
        if (coreachableStatesBDD == null)
        {
            coreachableStatesBDD = manager.coreachableStates(markedStatesBDD, bddTransitions, sourceStateVariables, eventDomain.set(), destToSourceStatePairing);
            nbrOfCoreachableStates = coreachableStatesBDD.satCount(sourceStateVariables);
        }
        return coreachableStatesBDD;
    }  
    
    BDD getReachableAndCoreachableStates()
    {
        if (reachableAndCoreachableStatesBDD == null)
        {
            BDD reachableStatesBDD = getReachableStates();
            BDD coreachableStatesBDD = getCoreachableStates();
            
            reachableAndCoreachableStatesBDD = reachableStatesBDD.and(coreachableStatesBDD);   
        
            nbrOfReachableAndCoreachableStates = reachableAndCoreachableStatesBDD.satCount(sourceStateVariables);  
            nbrOfBlockingStates = nbrOfReachableStates - nbrOfReachableAndCoreachableStates;
        }
    
        return reachableAndCoreachableStatesBDD;
    } 
    
    public Automaton getSupervisor()
    {
        Automaton supervisorAutomaton = new Automaton("Sup");
        BDD reachableAndCoreachableStatesBDD = getReachableAndCoreachableStates();
        reachableAndCoreachableStatesBDD.exist(destStateVariables);
        
        // Create all events
        Alphabet newAlphabet = supervisorAutomaton.getAlphabet();
        //newAlphabet.addAll(unionAlphabet);

        int[] sourceStateDomainIndicies = new int[sourceStateDomains.length];
        for (int i = 0; i < sourceStateDomains.length; i++)
        {
            logger.info("Source state domain " + i + ": " + ArrayHelper.arrayToString(sourceStateDomains[i].vars()));
            sourceStateDomainIndicies[i] = sourceStateDomains[i].getIndex();
        } 
        
        logger.info("sourceStateDomainIndicies: " + ArrayHelper.arrayToString(sourceStateDomainIndicies));
        
        int[] stateArray = new int[sourceStateDomains.length];
        // Create all states
        for ( BDD.BDDIterator satIt = new BDD.BDDIterator(reachableAndCoreachableStatesBDD, sourceStateVariables); satIt.hasNext(); ) 
        {
            BigInteger[] currSat = satIt.nextTuple();
            for (int i = 0; i < sourceStateDomainIndicies.length; i++)
            {
                stateArray[i] = currSat[sourceStateDomainIndicies[i]].intValue();
            }
            logger.info("current state: " + ArrayHelper.arrayToString(stateArray));
        }
//        // Create all states
//        for (BDD.AllSatIterator stateIt = reachableAndCoreachableStatesBDD.allsat(); stateIt.hasNext(); ) 
//        {
//            byte[] currState = stateIt.nextSat();
//            StringBuffer stringBuffer = new StringBuffer("[");
//            for (int i = 0; i < currState.length; i++)
//            {
//                stringBuffer.append(currState[i]);
//                if (i < currState.length - 1)
//                {
//                    stringBuffer.append(" ");
//                }
//            }
//            stringBuffer.append("]");
//            logger.info("state: " + stringBuffer.toString()); 
//        }        
        return supervisorAutomaton;
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
