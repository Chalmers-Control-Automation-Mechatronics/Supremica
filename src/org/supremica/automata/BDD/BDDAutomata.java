
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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarSet;

import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.OverflowException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexMap;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.util.ArrayHelper;


public class BDDAutomata
    implements Iterable<BDDAutomaton>, Abortable
{
    private static Logger logger = LogManager.getLogger(BDDAutomata.class);
    private boolean isAborting;

    BDDManager manager;
    Automata theAutomata;
    List<BDDAutomaton> theBDDAutomataList = new LinkedList<BDDAutomaton>();
    Map<Automaton, BDDAutomaton> automatonToBDDAutomatonMap = new HashMap<Automaton, BDDAutomaton>();
    public Map<Integer, String> bddVar2AutName = new HashMap<Integer, String>();
    public  Map<Integer, Integer> bddVar2bitValue = new HashMap<Integer, Integer>();
    public Map<String, Integer> aut2nbrOfBits = new HashMap<String, Integer>();
    Map<String,BDDDomain> autName2SourceStateDomain = new HashMap<String, BDDDomain>();
    Map<Integer,BDD> bddVar2BDD = new HashMap<Integer, BDD>();

    AutomataIndexMap theIndexMap;
    Alphabet unionAlphabet;

    BDDTransitions bddTransitions = null;

    BDDDomain eventDomain;
    BDDDomain[] tempStateDomains = null;
    BDDDomain[] sourceStateDomains = null;
    BDDDomain[] destStateDomains = null;

    BDDVarSet sourceStateVariables = null;
    BDDVarSet destStateVariables = null;

    BDDPairing tempToDestStatePairing = null;
    BDDPairing sourceToTempStatePairing = null;
    BDDPairing destToSourceStatePairing = null;

    BDD initialStatesBDD = null;
    BDD markedStatesBDD = null;
    BDD forbiddenStatesBDD = null;
    BDD uncontrollableStatesBDD = null;

    BDD reachableStatesBDD = null;
    BDD coreachableStatesBDD = null;
    BDD reachableAndCoreachableStatesBDD = null;
    BDD safeStatesBDD = null;

    BDD uncontrollableEventsBDD = null;

    double nbrOfReachableStates = -1;
    double nbrOfCoreachableStates = -1;
    double nbrOfReachableAndCoreachableStates = -1;
    double nbrOfBlockingStates = -1;
    double nbrOfSafeStates = -1;


    public BDDAutomata(final Automata orgAutomata)
    {
        final AutomataSorter automataSorter = new PCGAutomataSorter();

        theAutomata = automataSorter.sortAutomata(orgAutomata);

        manager = new BDDManager();

        try
        {
            theIndexMap = new AutomataIndexMap(theAutomata);
        }
        catch (final Exception e)
        {
            logger.error(e);
        }

        initialStatesBDD = manager.getOneBDD();
        markedStatesBDD = manager.getOneBDD();
        forbiddenStatesBDD = manager.getZeroBDD();

        uncontrollableStatesBDD = manager.getZeroBDD();
        uncontrollableEventsBDD = manager.getZeroBDD();

        initialize();
    }

    void initialize()
    {
        unionAlphabet = theAutomata.getUnionAlphabet();
        eventDomain = manager.createDomain(unionAlphabet.size());
        eventDomain.setName("Events");

        sourceStateVariables = manager.createEmptyVarSet();
        destStateVariables = manager.createEmptyVarSet();

        tempStateDomains = new BDDDomain[theAutomata.size()];
        sourceStateDomains = new BDDDomain[theAutomata.size()];
        destStateDomains = new BDDDomain[theAutomata.size()];

        int i = 0;
        for (final Automaton automaton : theAutomata)
        {
            final int nbrOfStates = automaton.nbrOfStates();
            final BDDDomain tempStateDomain = manager.createDomain(nbrOfStates);
            final BDDDomain sourceStateDomain = manager.createDomain(nbrOfStates);
            final BDDDomain destStateDomain = manager.createDomain(nbrOfStates);
            final BDDAutomaton bddAutomaton = new BDDAutomaton(this, automaton, sourceStateDomain, destStateDomain);

            autName2SourceStateDomain.put(automaton.getName(),sourceStateDomain);

            final int[] vars = sourceStateDomain.vars();
            final int nbrOfVars = vars.length;
            aut2nbrOfBits.put(automaton.getName(), nbrOfVars);

            for(int h=0;h<nbrOfVars;h++)
            {
                final int[] var = new int[1];
                var[0] = vars[h];
                bddVar2BDD.put(vars[h], manager.getFactory().buildCube(1, var));
                bddVar2AutName.put(vars[h], automaton.getName());
                bddVar2bitValue.put(vars[h], h);
            }

            bddAutomaton.initialize();

            sourceStateVariables.unionWith(sourceStateDomain.set());
            destStateVariables.unionWith(destStateDomain.set());
            sourceStateDomains[i] = sourceStateDomain;
            sourceStateDomains[i].setName(automaton.getName());
            destStateDomains[i] = destStateDomain;
            destStateDomains[i].setName(automaton.getName());
            tempStateDomains[i] = tempStateDomain;

            add(bddAutomaton);
            i++;
        }

        sourceToTempStatePairing = manager.makePairing(sourceStateDomains, tempStateDomains);
        tempToDestStatePairing = manager.makePairing(tempStateDomains, destStateDomains);
        destToSourceStatePairing = manager.makePairing(destStateDomains, sourceStateDomains);

        bddTransitions = new BDDTransitionFactory(this).createTransitions();

//        System.out.println("number of transitions: "+((BDDMonolithicTransitions)bddTransitions).transitionForwardBDD.pathCount());
    }

    public BDD getMonolithicStates()
    {
        return ((BDDMonolithicTransitions)bddTransitions).getMonolithicTransitionForwardBDD().exist(destStateVariables).exist(eventDomain.set());
    }

    public BDD getBDDforBDDVar(final int bddVar)
    {
        return bddVar2BDD.get(bddVar);
    }

    public BDDManager getBDDManager()
    {
        return manager;
    }

    public Automata getAutomata()
    {
        return theAutomata;
    }

    public BDDAutomaton getBDDAutomaton(final Automaton theAutomaton)
    {
        return automatonToBDDAutomatonMap.get(theAutomaton);
    }

    public BDDAutomaton getBDDAutomaton(final String autName)
    {
        for(final Automaton aut: theAutomata)
            if(aut.getName().equals(autName))
                return automatonToBDDAutomatonMap.get(aut);
        return null;
    }

    public BDDPairing getDest2SourcePairing()
    {
        return destToSourceStatePairing;
    }

    public BDDVarSet getSourceStateVariables()
    {
        return sourceStateVariables;
    }

    public BDDVarSet getDestStateVariables()
    {
        return destStateVariables;
    }

    public BDD getUncontrollableEvents()
    {
        return uncontrollableEventsBDD;
    }

    public BDDTransitions getBDDTransitions()
    {
        return bddTransitions;
    }

    public void done()
    {
        if (manager != null)
        {
            manager.done();
        }
    }

    @Override
    protected void finalize()
    {
        done();
    }

    public Alphabet getInverseAlphabet(final Automaton currAutomaton)
    {
        return theAutomata.getInverseAlphabet(currAutomaton);
    }


    public BDDVarSet getSourceStateVars(final Automaton aut)
    {
        BDDVarSet bddvarset = null;
        int i = 0;

        for(final Automaton inAut: theAutomata)
        {
            if(aut.equalAutomaton(inAut))
            {
                bddvarset = sourceStateDomains[i].set();
                return bddvarset;
            }
            i++;
        }
        return bddvarset;
    }

    public BDDDomain getSourceStateDomain(final String autName)
    {
        return autName2SourceStateDomain.get(autName);
    }

    void add(final BDDAutomaton bddAutomaton)
    {
        theBDDAutomataList.add(bddAutomaton);
        automatonToBDDAutomatonMap.put(bddAutomaton.getAutomaton(), bddAutomaton);
    }
    @Override
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

    public int getAutomatonIndex(final Automaton theAutomaton)
    {
        return theIndexMap.getAutomatonIndex(theAutomaton);
    }

    public int getStateIndex(final Automaton theAutomaton, final State theState)
    {
        return theIndexMap.getStateIndex(theAutomaton, theState);
    }

    public int getEventIndex(final LabeledEvent theEvent)
    {
        return theIndexMap.getEventIndex(theEvent);
    }

    public void addInitialStates(final BDD initialStates)
    {
        initialStatesBDD = initialStatesBDD.and(initialStates);
    }

    public void addMarkedStates(final BDD markedStates)
    {
        markedStatesBDD = markedStatesBDD.and(markedStates);
    }

    public BDD getMarkedStates()
    {
        return markedStatesBDD;
    }

    public void addForbiddenStates(final BDD forbiddenStates)
    {
        forbiddenStatesBDD = forbiddenStatesBDD.or(forbiddenStates);
    }

    public void addUncontrollableStates(final BDD uncontrollableStates)
    {
        uncontrollableStatesBDD = uncontrollableStatesBDD.and(uncontrollableStates);
    }

    public double numberOfReachableStates() throws AnalysisAbortException, OverflowException
    {
        if (nbrOfReachableStates < 0)
        {
            getReachableStates();
        }
        return nbrOfReachableStates;
    }

    public double numberOfCoreachableStates() throws AnalysisAbortException, OverflowException
    {
        if (nbrOfCoreachableStates < 0)
        {
            getCoreachableStates();
        }
        return nbrOfCoreachableStates;
    }

    public double numberOfBlockingStates() throws AnalysisAbortException, OverflowException
    {
        if (nbrOfBlockingStates < 0)
        {
            getReachableAndCoreachableStates();
        }
        return nbrOfBlockingStates;
    }

    public double numberOfReachableAndCoreachableStates() throws AnalysisAbortException, OverflowException
    {
        if (nbrOfReachableAndCoreachableStates < 0)
        {
            getReachableAndCoreachableStates();
        }
        return nbrOfReachableAndCoreachableStates;
    }

    public boolean isNonblocking() throws AnalysisAbortException, OverflowException
    {
        final BDD reachableStatesBDD = getReachableStates();
        final BDD coreachableStatesBDD = getCoreachableStates();
        final BDD impBDD = reachableStatesBDD.imp(coreachableStatesBDD);
        return impBDD.equals(manager.getOneBDD());
    }

    public BDD getReachableStates() throws AnalysisAbortException, OverflowException
    {
        if (reachableStatesBDD == null)
        {
            reachableStatesBDD = manager.reachableStates(initialStatesBDD, bddTransitions, sourceStateVariables, eventDomain.set(), destToSourceStatePairing);
            // satCount seems use wrong sourceStateVariables!!
            nbrOfReachableStates = reachableStatesBDD.satCount(sourceStateVariables);
        }
        return reachableStatesBDD;
    }

    BDD getCoreachableStates() throws AnalysisAbortException, OverflowException
    {
        if (coreachableStatesBDD == null)
        {
            coreachableStatesBDD = manager.coreachableStates(markedStatesBDD, bddTransitions, sourceStateVariables, eventDomain.set(), destToSourceStatePairing);
            nbrOfCoreachableStates = coreachableStatesBDD.satCount(sourceStateVariables);
        }
        return coreachableStatesBDD;
    }

    public BDD getReachableAndCoreachableStates() throws AnalysisAbortException, OverflowException
    {
        if (reachableAndCoreachableStatesBDD == null)
        {
            final BDD reachableStatesBDD = getReachableStates();
            final BDD coreachableStatesBDD = getCoreachableStates();

            reachableAndCoreachableStatesBDD = reachableStatesBDD.and(coreachableStatesBDD);

            nbrOfReachableAndCoreachableStates = reachableAndCoreachableStatesBDD.satCount(sourceStateVariables);
            nbrOfBlockingStates = nbrOfReachableStates - nbrOfReachableAndCoreachableStates;
        }

        return reachableAndCoreachableStatesBDD;
    }

    public Automaton getSupervisor() throws AnalysisAbortException, OverflowException
    {
        final Automaton supervisorAutomaton = new Automaton("Sup");
        final BDD reachableAndCoreachableStatesBDD = getReachableAndCoreachableStates();
        reachableAndCoreachableStatesBDD.exist(destStateVariables);

        // Create all events
      //  Alphabet newAlphabet = supervisorAutomaton.getAlphabet();
        //newAlphabet.addAll(unionAlphabet);

        final int[] sourceStateDomainIndicies = new int[sourceStateDomains.length];
        for (int i = 0; i < sourceStateDomains.length; i++)
        {
            logger.info("Source state domain " + i + ": " + ArrayHelper.arrayToString(sourceStateDomains[i].vars()));
            sourceStateDomainIndicies[i] = sourceStateDomains[i].getIndex();
        }

        logger.info("sourceStateDomainIndicies: " + ArrayHelper.arrayToString(sourceStateDomainIndicies));

        final int[] stateArray = new int[sourceStateDomains.length];
        // Create all states
        for ( final BDD.BDDIterator satIt = new BDD.BDDIterator(reachableAndCoreachableStatesBDD, sourceStateVariables); satIt.hasNext(); )
        {
            final BigInteger[] currSat = satIt.nextTuple();
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
//            StringBuilder StringBuilder = new StringBuilder("[");
//            for (int i = 0; i < currState.length; i++)
//            {
//                StringBuilder.append(currState[i]);
//                if (i < currState.length - 1)
//                {
//                    StringBuilder.append(" ");
//                }
//            }
//            StringBuilder.append("]");
//            logger.info("state: " + StringBuilder.toString());
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

    public BDDDomain[] getSourceStateDomains()
    {
        return sourceStateDomains;
    }

    public BDDDomain[] getDestStateDomains()
    {
        return destStateDomains;
    }

    public BDD getForbiddenStates()
    {
        return forbiddenStatesBDD;
    }

    @Override
    public void requestAbort()
    {
      // TODO Auto-generated method stub
      isAborting = true;
      manager.requestAbort();
    }

    @Override
    public boolean isAborting()
    {
      return isAborting;
    }

    @Override
    public void resetAbort()
    {
      isAborting = false;
    }

}
