
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

import net.sourceforge.waters.analysis.bdd.BDDPackage;
import net.sourceforge.waters.model.analysis.AbstractAbortable;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.properties.Config;


public class BDDManager extends AbstractAbortable
{
    private static Logger logger = LogManager.getLogger(BDDManager.class);

    static BDDFactory factory;

    public BDDManager()
    {
        this(Config.BDD2_BDD_LIBRARY.getValue());
    }

    public BDDManager(final BDDPackage bddpackage)
    {
        this(bddpackage, Config.BDD2_INITIAL_NODE_TABLE_SIZE.getValue(), Config.BDD2_CACHE_SIZE.getValue());
    }

    public BDDManager(final BDDPackage bddpackage, final int nodenum, final int cachesize)
    {
      if (factory == null) {
        factory = BDDFactory.init(bddpackage.getBDDPackageName(), nodenum, cachesize);
        factory.setMaxIncrease(Config.BDD2_MAX_INCREASE_NODES.getValue());
        factory.setIncreaseFactor(Config.BDD2_INCREASE_FACTOR.getValue());
        factory.setCacheRatio(Config.BDD2_CACHE_RATIO.getValue());
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
          factory.clearAllDomains();
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

    public BDDDomain createDomain(final int size)
    {
        return factory.extDomain(size);
    }

    public BDDVarSet createEmptyVarSet()
    {
        return factory.emptySet();
    }

    public BDD createBDD(final int index, final BDDDomain domain)
    {
        return factory.buildCube(index, domain.vars());
    }
    public BDDPairing makePairing(final BDDDomain[] source, final BDDDomain[] dest)
    {
        final BDDPairing pairing = factory.makePair();
        pairing.set(source, dest);
        return pairing;
    }

    public BDDPairing makePairing(final BDDDomain source, final BDDDomain dest)
    {
        return factory.makePair(source, dest);
    }

    public static void addState(final BDD bdd, final int stateIndex,  final BDDDomain domain)
    {
        final BDD newStateBDD = factory.buildCube(stateIndex, domain.vars());
        bdd.orWith(newStateBDD);
    }

    public static void addTransition(final BDD bdd, final int sourceStateIndex, final BDDDomain sourceDomain, final int destStateIndex, final BDDDomain destDomain, final int eventIndex, final BDDDomain eventDomain)
    {
        // Create a BDD representing the source state
        final BDD sourceBDD = factory.buildCube(sourceStateIndex, sourceDomain.vars());

        // Create a BDD representing the dest state
        final BDD destBDD = factory.buildCube(destStateIndex, destDomain.vars());

        // Create a BDD representing the event
        final BDD eventBDD = factory.buildCube(eventIndex, eventDomain.vars());

        // Add source and dest state
        sourceBDD.andWith(destBDD);

        // Add event to source and dest state
        sourceBDD.andWith(eventBDD);

        // Add the transition to the set of existing transitions
        bdd.orWith(sourceBDD);
    }

    public BDD reachableStates(final BDD initialStates,final BDDTransitions transitions,
                                      final BDDVarSet sourceStateVariables, final BDDVarSet eventVariables,
                                      final BDDPairing destToSourceStatePairing)
                                        throws AnalysisAbortException
    {
        final BDD reachableStatesBDD = initialStates.id();
        BDD previousReachableStatesBDD = null;

        do
        {
            checkAbort();
            // Keep a copy of the previously discovered states
            // This will be used in the termination condition for
            // the operation.
            previousReachableStatesBDD = reachableStatesBDD.id();

            // Compute AND function of reachable states and the transitions.
            // By using this all destination states that can be reached from the current set of
            // reachable states will be in the DestDomainVariables.
            // The source states in the BDD are those states that survived the AND function,
            // which is all states that had an (in the composition) enabled event.
            // The source states are not of interest to us - thus we quantify out them.
            // The AND function and removal of some variables are done in one operation by
            // the relprod command.
            BDD nextStatesAndTransitionsBDD;
            if (transitions instanceof BDDMonolithicTransitions)
            {
                final BDD monolithicTransitionsBDD = ((BDDMonolithicTransitions)transitions).getMonolithicTransitionForwardBDD();

                //logger.debug("Number of nodes in monolithicTransitionsBDD: " + monolithicTransitionsBDD.nodeCount());
                nextStatesAndTransitionsBDD = reachableStatesBDD.relprod(monolithicTransitionsBDD, sourceStateVariables);
                //logger.debug("Number of nodes in nextStatesAndTransitionsBDD: " + nextStatesAndTransitionsBDD.nodeCount());
            }
            else if (transitions instanceof BDDConjunctiveTransitions)
            {
                final BDDConjunctiveTransitions bddConjunctiveTransitions = (BDDConjunctiveTransitions)transitions;
                nextStatesAndTransitionsBDD = reachableStatesBDD.id();
                //logger.debug("New round in reachability");
                for(final Iterator<BDD> transitionBDDIt = bddConjunctiveTransitions.forwardIterator(); transitionBDDIt.hasNext(); )
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
                final BDDDisjunctiveTransitions bddDisjunctiveTransitions = (BDDDisjunctiveTransitions)transitions;
                nextStatesAndTransitionsBDD = reachableStatesBDD;
                for(final Iterator<BDD> transitionBDDIt = bddDisjunctiveTransitions.forwardIterator(); transitionBDDIt.hasNext(); )
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
            final BDD nextStatesBDD = nextStatesAndTransitionsBDD.replace(destToSourceStatePairing);

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

    public BDD coreachableStates(final BDD markedStates,
                                 final BDDTransitions transitions,
                                 final BDDVarSet sourceStateVariables,
                                 final BDDVarSet eventVariables,
                                 final BDDPairing destToSourceStatePairing)
                                   throws AnalysisAbortException
    {
        final BDD coreachableStatesBDD = markedStates.id();
        BDD previousCoreachableStatesBDD = null;

        do
        {
            checkAbort();
            previousCoreachableStatesBDD = coreachableStatesBDD.id();

            BDD previousStatesAndTransitionsBDD;
            if (transitions instanceof BDDMonolithicTransitions)
            {
                final BDD monolithicTransitions = ((BDDMonolithicTransitions)transitions).getMonolithicTransitionBackwardBDD();
                previousStatesAndTransitionsBDD = previousCoreachableStatesBDD.relprod(monolithicTransitions, sourceStateVariables);
            }
            else if (transitions instanceof BDDConjunctiveTransitions)
            {
                final BDDConjunctiveTransitions bddConjunctiveTransitions = (BDDConjunctiveTransitions)transitions;
                previousStatesAndTransitionsBDD = coreachableStatesBDD.id();
                for(final Iterator<BDD> transitionBDDIt = bddConjunctiveTransitions.backwardIterator(); transitionBDDIt.hasNext(); )
                {
                    previousStatesAndTransitionsBDD = previousStatesAndTransitionsBDD.and(transitionBDDIt.next());
                }
                previousStatesAndTransitionsBDD = previousStatesAndTransitionsBDD.exist(sourceStateVariables);
                previousStatesAndTransitionsBDD = previousStatesAndTransitionsBDD.exist(eventVariables);
            }
            else if (transitions instanceof BDDDisjunctiveTransitions)
            {
                final BDDDisjunctiveTransitions bddDisjunctiveTransitions = (BDDDisjunctiveTransitions)transitions;
                previousStatesAndTransitionsBDD = coreachableStatesBDD;
                for(final Iterator<BDD> transitionBDDIt = bddDisjunctiveTransitions.backwardIterator(); transitionBDDIt.hasNext(); )
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

            final BDD previousStatesBDD = previousStatesAndTransitionsBDD.replace(destToSourceStatePairing);
            coreachableStatesBDD.orWith(previousStatesBDD);
        }
        while (!coreachableStatesBDD.equals(previousCoreachableStatesBDD)); // Until no new states are found

        return coreachableStatesBDD;
    }

    public BDD uncontrollableBackward(final BDDAutomata bdda, final BDD forbidden)
    {
        final BDD t_u = ((BDDMonolithicTransitions)bdda.getBDDTransitions()).getMonolithicUncontrollableTransitionBackwardBDD();

        BDD Qk = null;
        BDD Qkn = forbidden;

        do
        {
            Qk = Qkn.id();
            Qkn = Qk.or(image_preImage(bdda,Qk,t_u));
        } while (!Qkn.equals(Qk));

        return Qkn;
    }

    public BDD restrictedBackward(final BDDAutomata bdda, final BDD forbidden)
    {
        final BDD delta_all = ((BDDMonolithicTransitions)bdda.getBDDTransitions()).getMonolithicTransitionBackwardBDD();

        BDD Qkn = bdda.getMarkedStates().and(forbidden.not());
        BDD Qk = null;

        do
        {
          Qk = Qkn.id();
          Qkn = Qk.or(image_preImage(bdda,Qk,delta_all)).and(forbidden.not());
        } while (!Qkn.equals(Qk));

        return Qkn;
    }

    public BDD reachability(final BDDAutomata bdda, final BDD forbidden)
    {
        final BDD delta_all = ((BDDMonolithicTransitions)bdda.getBDDTransitions()).getMonolithicTransitionForwardBDD();

        BDD Qkn = bdda.initialStatesBDD;
        BDD Qk = null;

        do
        {
          Qk = Qkn.id();
          Qkn = Qk.or(image_preImage(bdda,Qk,delta_all)).and(forbidden.not());
        } while (!Qkn.equals(Qk));

        return Qkn;
    }


    public BDD safeStateSynthesis(final BDDAutomata bdda, final BDD forbidden)
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

        return reachability(bdda, Qkn);
    }

    public BDD image_preImage(final BDDAutomata bdda, final BDD states, final BDD transitions)
    {
        BDD nextStates = null;

        nextStates = transitions.relprod(states, bdda.getSourceStateVariables());
        nextStates = nextStates.exist(bdda.getEventVarSet());
        nextStates.replaceWith(bdda.getDest2SourcePairing());

        return nextStates;
    }

}
