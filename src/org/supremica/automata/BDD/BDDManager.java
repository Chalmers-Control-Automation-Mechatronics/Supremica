
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

import java.util.Iterator;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarSet;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;


public class BDDManager
{
    private static Logger logger = LoggerFactory.createLogger(BDDManager.class);
    
    static BDDFactory factory;
    
    public BDDManager()
    {
        this(BDDLibraryType.fromDescription(Config.BDD2_BDDLIBRARY.getAsString()));
    }
    
    public BDDManager(BDDLibraryType bddpackage)
    {
        this(bddpackage, Config.BDD2_INITIALNODETABLESIZE.get(), Config.BDD2_CACHESIZE.get());
    }
    
    public BDDManager(BDDLibraryType bddpackage, int nodenum, int cachesize)
    {
        if (factory == null)
        {
            factory = BDDFactory.init(bddpackage.getLibraryname(), nodenum, cachesize);
            factory.setMaxIncrease(Config.BDD2_MAXINCREASENODES.get());
            factory.setIncreaseFactor(Config.BDD2_INCREASEFACTOR.get());
            factory.setCacheRatio(Config.BDD2_CACHERATIO.get());
            
        }
    }
    
    public BDDFactory getFactory()
    {
        return factory;
    }
    
    public void done()
    {
        if (factory != null)
        {
            factory.done();
            factory = null;
        }
    }
    
    public BDD getZeroBDD()
    {
        return factory.zero();
    }
    
    public BDD getOneBDD()
    {
        return factory.one();
    }
    
    public BDDDomain createDomain(int size)
    {
        return factory.extDomain(size);
    }

    public BDDVarSet createEmptyVarSet()
    {
        return factory.emptySet();
    }
   
    public BDD createBDD(int index, BDDDomain domain)
    {
        return factory.buildCube(index, domain.vars());
    }
    public BDDPairing makePairing(BDDDomain[] source, BDDDomain[] dest)
    {
        BDDPairing pairing = factory.makePair();
        pairing.set(source, dest);
        return pairing;
    }
    
    public BDDPairing makePairing(BDDDomain source, BDDDomain dest)
    {
        return factory.makePair(source, dest);
    }
    
    public static void addState(BDD bdd, int stateIndex,  BDDDomain domain)
    {
        BDD newStateBDD = factory.buildCube(stateIndex, domain.vars());
        bdd.orWith(newStateBDD);                 
    }
    
    public static void addTransition(BDD bdd, int sourceStateIndex, BDDDomain sourceDomain, int destStateIndex, BDDDomain destDomain, int eventIndex, BDDDomain eventDomain)
    {
        // Create a BDD representing the source state
        BDD sourceBDD = factory.buildCube(sourceStateIndex, sourceDomain.vars());  
        
        // Create a BDD representing the dest state
        BDD destBDD = factory.buildCube(destStateIndex, destDomain.vars());
        
        // Create a BDD representing the event
        BDD eventBDD = factory.buildCube(eventIndex, eventDomain.vars());
        
        // Add source and dest state
        sourceBDD.andWith(destBDD);
        
        // Add event to source and dest state
        sourceBDD.andWith(eventBDD);
        
        // Add the transition to the set of existing transitions
        bdd.orWith(sourceBDD);
    }

    public static BDD reachableStates(BDD initialStates, BDDTransitions transitions, BDDVarSet sourceStateVariables, BDDVarSet eventVariables, BDDPairing destToSourceStatePairing)
    {
        BDD reachableStatesBDD = initialStates.id();
        BDD previousReachableStatesBDD = null;
        
        logger.debug("In reachableStates");
        do
        {
            // Keep a copy of the previously discovered states
            // This will be used in the terminatiion condition for
            // the operation.
            previousReachableStatesBDD = reachableStatesBDD.id();
            
            // Compute AND function of rechable states and the transitions.
            // By using this all dest states that can be reached from the current set of
            // reachable states will be in the DestDomainVariables.
            // The source states in the BDD are those states that survived the AND function,
            // which is all states that had an (in the composition) enabled event.
            // The source states are not of interest to us - thus we quantify out them.
            // The AND function and removal of some variables are done in one operation by
            // the relprod command.
            BDD nextStatesAndTransitionsBDD;
            if (transitions instanceof BDDMonolithicTransitions)
            {
                BDD monolithicTransitionsBDD = ((BDDMonolithicTransitions)transitions).getMonolithicTransitionForwardBDD();
              
                //logger.debug("Number of nodes in monolithicTransitionsBDD: " + monolithicTransitionsBDD.nodeCount());
                nextStatesAndTransitionsBDD = reachableStatesBDD.relprod(monolithicTransitionsBDD, sourceStateVariables);
                //logger.debug("Number of nodes in nextStatesAndTransitionsBDD: " + nextStatesAndTransitionsBDD.nodeCount());
            }
            else if (transitions instanceof BDDConjunctiveTransitions)
            {
                BDDConjunctiveTransitions bddConjunctiveTransitions = (BDDConjunctiveTransitions)transitions;
                nextStatesAndTransitionsBDD = reachableStatesBDD.id();
                //logger.debug("New round in reachability");                       
                for(Iterator<BDD> transitionBDDIt = bddConjunctiveTransitions.forwardIterator(); transitionBDDIt.hasNext(); )
                {
                    nextStatesAndTransitionsBDD = nextStatesAndTransitionsBDD.and(transitionBDDIt.next());         
                    //logger.debug("Number of nodes in nextStatesAndTransitionsBDD: " + nextStatesAndTransitionsBDD.nodeCount());       
                }
                nextStatesAndTransitionsBDD = nextStatesAndTransitionsBDD.exist(sourceStateVariables);
                nextStatesAndTransitionsBDD = nextStatesAndTransitionsBDD.exist(eventVariables);
                //logger.debug("Number of nodes in nextStatesAndTransitionsBDD: " + nextStatesAndTransitionsBDD.nodeCount());       
            }
            else if (transitions instanceof BDDDisjunctiveTransitions)
            {
                BDDDisjunctiveTransitions bddDisjunctiveTransitions = (BDDDisjunctiveTransitions)transitions;
                nextStatesAndTransitionsBDD = reachableStatesBDD;
                for(Iterator<BDD> transitionBDDIt = bddDisjunctiveTransitions.forwardIterator(); transitionBDDIt.hasNext(); )
                {
                    nextStatesAndTransitionsBDD = nextStatesAndTransitionsBDD.or(transitionBDDIt.next());         
                }
                nextStatesAndTransitionsBDD = nextStatesAndTransitionsBDD.exist(sourceStateVariables);
            }
            else
            {
                logger.error("Unknown BDDTransition class: " + transitions.getClass());
                return null;
            }
                
                
            // Now all states that could be reached from the current set of states
            // are in the DestDomainVariables. Now we need to move them from DestDomainVariables
            // to SourceDomainVariables so they are comparaable with our previous reachable states.
            // This is done with the replace operation.
            BDD nextStatesBDD = nextStatesAndTransitionsBDD.replace(destToSourceStatePairing);
            
            // The next operation is to compute the union of the previous reachable
            // states and the states reachble, in one iteration, from the reachable
            // states. This is simply done by doing an OR between the
            // previously reachable states and the newly found states.
            reachableStatesBDD.orWith(nextStatesBDD);
            
            logger.debug("Number of nodes in reachableStatesBDD: " + reachableStatesBDD.nodeCount());
        }
        while (!reachableStatesBDD.equals(previousReachableStatesBDD)); // Until no new states are found
        
        return reachableStatesBDD;
    }
    
    public static BDD coreachableStates(BDD markedStates, BDDTransitions transitions, BDDVarSet sourceStateVariables, BDDVarSet eventVariables, BDDPairing destToSourceStatePairing)
    {
        BDD coreachableStatesBDD = markedStates.id();
        BDD previousCoreachableStatesBDD = null;
        
        do
        {
            previousCoreachableStatesBDD = coreachableStatesBDD.id();
            
            BDD previousStatesAndTransitionsBDD;
            if (transitions instanceof BDDMonolithicTransitions)
            {
                BDD monolithicTransitions = ((BDDMonolithicTransitions)transitions).getMonolithicTransitionBackwardBDD();
                previousStatesAndTransitionsBDD = previousCoreachableStatesBDD.relprod(monolithicTransitions, sourceStateVariables);
            }
            else if (transitions instanceof BDDConjunctiveTransitions)
            {
                BDDConjunctiveTransitions bddConjunctiveTransitions = (BDDConjunctiveTransitions)transitions;
                previousStatesAndTransitionsBDD = coreachableStatesBDD.id();
                for(Iterator<BDD> transitionBDDIt = bddConjunctiveTransitions.backwardIterator(); transitionBDDIt.hasNext(); )
                {
                    previousStatesAndTransitionsBDD = previousStatesAndTransitionsBDD.and(transitionBDDIt.next());      
                }
                previousStatesAndTransitionsBDD = previousStatesAndTransitionsBDD.exist(sourceStateVariables);
                previousStatesAndTransitionsBDD = previousStatesAndTransitionsBDD.exist(eventVariables);
            }
            else if (transitions instanceof BDDDisjunctiveTransitions)
            {
                BDDDisjunctiveTransitions bddDisjunctiveTransitions = (BDDDisjunctiveTransitions)transitions;
                previousStatesAndTransitionsBDD = coreachableStatesBDD;
                for(Iterator<BDD> transitionBDDIt = bddDisjunctiveTransitions.backwardIterator(); transitionBDDIt.hasNext(); )
                {
                    previousStatesAndTransitionsBDD.or(transitionBDDIt.next());      
                }
                previousStatesAndTransitionsBDD = previousStatesAndTransitionsBDD.exist(sourceStateVariables);
            }
            else
            {
                logger.error("Unknown BDDTransition class: " + transitions.getClass());
                return null;
            }           
            
            BDD previousStatesBDD = previousStatesAndTransitionsBDD.replace(destToSourceStatePairing);
            coreachableStatesBDD.orWith(previousStatesBDD);
        }
        while (!coreachableStatesBDD.equals(previousCoreachableStatesBDD)); // Until no new states are found
        
        return coreachableStatesBDD;
    }

    public static BDD prelimUncontrollableStates(BDDAutomata bdda)
    {

        BDD uncontrollableStatesBDD = null;

        BDD commonUnconEvents = bdda.getSpecsUncontrollableEvents().and(bdda.getPlantsUncontrollableEvents());
        BDD outgoingTransitions = ((BDDMonolithicTransitions)bdda.getBDDTransitions()).getMyMonolithicTransitionForwardBDD().exist(bdda.getDestStateVariables());
//        BDD outgoingTransitions = ((BDDMonolithicTransitions)bdda.bddTransitions).transitionForwardBDD.exist(bdda.getDestStateVariables());
        BDD nonexistingOutgTrans = outgoingTransitions.not();
        BDD nonexistingOutgTransWithUnconEvents = nonexistingOutgTrans.and(commonUnconEvents);

//        nonexistingOutgTransWithUnconEvents.printDot();
//        System.out.println("number of transitions: "+outgoingTransitions.pathCount());


        uncontrollableStatesBDD = bdda.getPlantsForwardTransitions().and(bdda.getPlantsSelfLoopsBDD().not()).relprod(nonexistingOutgTransWithUnconEvents,bdda.getEventVarSet());
//        uncontrollableStatesBDD = bdda.getPlantsForwardTransitions().relprod(nonexistingOutgTransWithUnconEvents,bdda.getEventVarSet());

        uncontrollableStatesBDD = uncontrollableStatesBDD.exist(bdda.getDestStateVariables());

        return uncontrollableStatesBDD;
    }
    
/*    public BDD uncontrollableBackward(BDDAutomata bdda, BDD forbidden)
    {
        
        BDD delta_all = bdda.getPlantsForwardTransitions().and(bdda.getSpecsForwardTransitions());
        BDD t_u = delta_all.relprod(bdda.getUncontrollableEvents(), bdda.getEventVarSet());
        
        BDD r_all_p, r_all = forbidden.replace(makePairing(bdda.getSourceStateDomains(),bdda.getDestStateDomains()));
        
        BDD front = r_all.id();
                
        do
        {
            r_all_p = r_all;
            
            BDD tmp = t_u.relprod(front, bdda.getDestStateVariables());
            BDD tmp2 = tmp.replace(makePairing(bdda.getSourceStateDomains(),bdda.getDestStateDomains()));
            
 //           tmp.free();
 //           front.free();
            
            r_all = r_all.or(tmp2);
//            front = fso.choose(r_all, tmp2);    // Takes care of tmp2!
        } while (!r_all_p.equals(r_all));
        
//        front.free();
//        t_u.free();
        
        
        BDD ret = r_all.replace(makePairing(bdda.getDestStateDomains(),bdda.getSourceStateDomains()));
//        r_all.free();
        
        return ret;
    }*/

    public BDD uncontrollableBackward(BDDAutomata bdda, BDD forbidden)
    {
        BDD delta_all = ((BDDMonolithicTransitions)bdda.getBDDTransitions()).getMyMonolithicTransitionBackwardBDD();
        BDD t_u = delta_all.and(bdda.getUncontrollableEvents());

        BDD Qk = null;
        BDD Qkn = forbidden;

        do
        {
            Qk = Qkn.id();
            Qkn = Qk.or(preImage(bdda,Qk,t_u));
        } while (!Qkn.equals(Qk));

        return Qkn;
    }

    public BDD restrictedBackward(BDDAutomata bdda, BDD forbidden)
    {
        BDD delta_all = ((BDDMonolithicTransitions)bdda.getBDDTransitions()).getMonolithicTransitionBackwardBDD();

        BDD Qkn = bdda.getMarkedStates().and(forbidden.not());
        BDD Qk = null;

        do
        {
          Qk = Qkn.id();
          Qkn = Qk.or(preImage(bdda,Qk,delta_all)).and(forbidden.not());
        } while (!Qkn.equals(Qk));

        return Qkn;
    }

    public BDD safeStateSynthesis(BDDAutomata bdda, BDD forbidden)
    {
        BDD Qkn = forbidden;
        BDD Qk = null;

        BDD Q1 = null;
        BDD Q2 = null;

        do
        {
            Qk = Qkn.id();
            Q1 = restrictedBackward(bdda, Qk);
            Q2 = uncontrollableBackward(bdda, Q1.not());
            Qkn = Qk.or(Q2);
        }while((!Qkn.equals(Qk)));

        return Qkn.not();
    }

    public BDD preImage(BDDAutomata bdda, BDD states, BDD backwardTransitions)
    {
        BDD preStates = null;

        preStates = backwardTransitions.relprod(states, bdda.getSourceStateVariables());
        preStates = preStates.exist(bdda.getEventVarSet());
        preStates.replaceWith(bdda.getDest2SourcePairing());

        return preStates;
    } 
    
}
