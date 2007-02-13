
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
import org.supremica.properties.Config;

public class BDDManager
{
    private static Logger logger = LoggerFactory.createLogger(BDDManager.class);
    
    static BDDFactory factory;
    
    public BDDManager()
    {
        this(Config.BDD2_BDDLIBRARY.get());
    }
    
    public BDDManager(String bddpackage)
    {
        this(bddpackage, Config.BDD2_INITIALNODETABLESIZE.get(), Config.BDD2_CACHESIZE.get());
    }
    
    public BDDManager(String bddpackage, int nodenum, int cachesize)
    {
        if (factory == null)
        {
            factory = BDDFactory.init(bddpackage, nodenum, cachesize);
        }
    }
    
    public static BDD getZeroBDD()
    {
        return factory.zero();
    }
    
    public static BDD getOneBDD()
    {
        return factory.one();
    }
    
    public static BDDDomain createDomain(int size)
    {
        return factory.extDomain(size);
    }

    public BDDVarSet createEmptyVarSet()
    {
        return factory.emptySet();
    }
     
    
    public static BDD createBDD(int index, BDDDomain domain)
    {
        return factory.buildCube(index, domain.vars());
    }
    public static BDDPairing makePairing(BDDDomain[] source, BDDDomain[] dest)
    {
        BDDPairing pairing = factory.makePair();
        pairing.set(source, dest);
        return pairing;
    }
    
    public static BDDPairing makePairing(BDDDomain source, BDDDomain dest)
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
    
    public static BDD reachableStates(BDD initialStates, BDD transitions, BDDVarSet sourceStateVariables, BDDPairing destToSourceStatePairing)
    {
        BDD reachableStatesBDD = initialStates.id();
        BDD previousReachableStatesBDD = null;
        
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
            BDD nextStatesAndTransitionsBDD = reachableStatesBDD.relprod(transitions, sourceStateVariables);
            
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
        }
        while (!reachableStatesBDD.equals(previousReachableStatesBDD)); // Until no new states are found
        
        return reachableStatesBDD;
    }
    
    public static BDD coreachableStates(BDD markedStates, BDD transitions, BDDVarSet sourceStateVariables, BDDPairing destToSourceStatePairing)
    {
        BDD coreachableStatesBDD = markedStates.id();
        BDD previousCoreachableStatesBDD = null;
        
        do
        {
            previousCoreachableStatesBDD = coreachableStatesBDD.id();
            BDD previousStatesAndTransitionsBDD = coreachableStatesBDD.relprod(transitions, sourceStateVariables);
            BDD previousStatesBDD = previousStatesAndTransitionsBDD.replace(destToSourceStatePairing);
            coreachableStatesBDD.orWith(previousStatesBDD);
        }
        while (!coreachableStatesBDD.equals(previousCoreachableStatesBDD)); // Until no new states are found
        
        return coreachableStatesBDD;
    }
}
