
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
import java.util.HashSet;
import java.util.Iterator;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarSet;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;


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
    BDD uncontrollableTransitionBackwardBDD;
    BDD transitionForwardDisjunctiveBDD;
    BDD transitionBackwardDisjunctiveBDD;
    BDD forbiddenStateSet;
    BDD allowedStateSet;
    BDD uncontrollableEventsBDD;

    HashMap<Integer,String> bddIndex2SourceStateName;
    public HashMap<Integer,String> myIndex2stateName;

    String OR = " | ";
    String AND = " & ";
    String O_PAR = "(";
    String C_PAR = ")";
    String EQUALS = " = ";
    String NEQUALS = " != ";

    int nbrOfTerms;
    public boolean allwFrbdnChosen = false;

    BDDAutomaton(final BDDAutomata bddAutomata, final Automaton theAutomaton, final BDDDomain sourceStateDomain, final BDDDomain destStateDomain)
    {

        this.manager = bddAutomata.getBDDManager();

        this.bddAutomata = bddAutomata;
        this.theAutomaton = theAutomaton;

        this.sourceStateDomain = sourceStateDomain;
        this.destStateDomain = destStateDomain;

        bddIndex2SourceStateName = new HashMap<Integer,String>();
        myIndex2stateName = new HashMap<Integer, String>();

        sourceToDestPairing = manager.makePairing(sourceStateDomain, destStateDomain);
        destToSourcePairing = manager.makePairing(destStateDomain, sourceStateDomain);

        transitionForwardBDD = manager.getZeroBDD();
        transitionBackwardBDD = manager.getZeroBDD();
        uncontrollableTransitionBackwardBDD = manager.getZeroBDD();

        transitionForwardDisjunctiveBDD = manager.getZeroBDD();
        transitionBackwardDisjunctiveBDD = manager.getZeroBDD();

        nbrOfTerms = 0;
    }

    public Automaton getAutomaton()
    {
        return theAutomaton;
    }

    public void initialize()
    {
        final BDD initialStates = manager.getZeroBDD();

        final BDD markedStates = manager.getZeroBDD();
        final BDD forbiddenStates = manager.getZeroBDD();

        final Alphabet inverseAlphabet = bddAutomata.getInverseAlphabet(theAutomaton);

        for (final State currState : theAutomaton)
        {
            final Alphabet outgoingUnconEvents = new Alphabet();

            // First create all transitions in this automaton
            for (final Iterator<Arc> arcIt = currState.outgoingArcsIterator(); arcIt.hasNext(); )
            {
                final Arc currArc = arcIt.next();
                if(theAutomaton.isSpecification() && !currArc.getEvent().isControllable() && !outgoingUnconEvents.contains(currArc.getEvent()))
                    outgoingUnconEvents.add(currArc.getEvent());

                addTransition(currArc);
            }

            // Self loop events not in this alphabet
            for (final LabeledEvent event : inverseAlphabet)
            {
                addTransition(currState, currState, event);
            }

            // Then add state properties
            final int stateIndex = bddAutomata.getStateIndex(theAutomaton, currState);

//            System.out.println("Automaton: "+theAutomaton.getName()+"------------- currState: "+currState.getName()+"------- stateIndex"+stateIndex);
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

    void addTransition(final Arc theArc)
    {
        final State sourceState = theArc.getSource();
        final State destState = theArc.getTarget();
        final LabeledEvent theEvent = theArc.getEvent();

        // Add all states that could be reach by only unobservable events including the destState
        for (final State epsilonState : destState.epsilonClosure(true))
        {
            addTransition(sourceState, epsilonState, theEvent);
        }
    }

    void addTransition(final State sourceState, final State destState, final LabeledEvent theEvent)
    {
        final int sourceStateIndex = bddAutomata.getStateIndex(theAutomaton, sourceState);
        final int destStateIndex = bddAutomata.getStateIndex(theAutomaton, destState);
        final int eventIndex = bddAutomata.getEventIndex(theEvent);

        final BDD sourceBDD = BDDManager.factory.buildCube(sourceStateIndex, getSourceStateDomain().vars());
        final Integer myIndex = generateIndex(sourceBDD);

        Integer bddIndex = -1;
        if(!bddIndex2SourceStateName.containsValue(sourceState.getName()))
        {
            myIndex2stateName.put(myIndex, sourceState.getName());

            final BDD.BDDIterator satIt = new BDD.BDDIterator(sourceBDD, getSourceStateDomain().set());
            final BigInteger[] currSat = satIt.nextTuple();
            for(int i=0; i<currSat.length;i++)
            {
                if(currSat[i] != null)
                {
                     bddIndex = currSat[i].intValue();
                     break;
                }
            }

            bddIndex2SourceStateName.put(bddIndex,sourceState.getName());
        }


        BDDManager.addTransition(transitionForwardBDD, sourceStateIndex, sourceStateDomain, destStateIndex, destStateDomain, eventIndex, bddAutomata.getEventDomain());
        BDDManager.addTransition(transitionBackwardBDD, destStateIndex, sourceStateDomain, sourceStateIndex, destStateDomain, eventIndex, bddAutomata.getEventDomain());

        if(!theEvent.isControllable())
            BDDManager.addTransition(uncontrollableTransitionBackwardBDD, destStateIndex, sourceStateDomain, sourceStateIndex, destStateDomain, eventIndex, bddAutomata.getEventDomain());
    }

    public HashSet<Integer> getComplementIndices(final HashSet<Integer> indices)
    {
        final HashSet<Integer> output = new HashSet<Integer>();
        for(final Integer i:myIndex2stateName.keySet())
        {
            if(!indices.contains(i))
                output.add(i);
        }
        return output;
    }

    public HashSet<String> getComplementStateNames(final HashSet<String> stateNames)
    {
        final HashSet<String> output = new HashSet<String>();
        for(final State state: getAutomaton().getStateSet())
        {
            if(!stateNames.contains(state.getName()))
                output.add(state.getName());
        }
        return output;
    }

    public Integer generateIndex(final BDD bdd)
    {
        if(bdd.low().isOne())
        {
            return 0;
        }
        else if(bdd.high().isOne())
        {
            return pow2(bddAutomata.bddVar2bitValue.get(bdd.var()));
        }
        else
        {
            if(!bdd.high().isZero())
            {
                final int temp = generateIndex(bdd.high());
                return (temp+pow2(bddAutomata.bddVar2bitValue.get(bdd.var())));
            }
            else
            {
                return generateIndex(bdd.low());
            }
        }
    }

    public int pow2(final int p)
    {
        return (int)Math.pow(2,p);
    }

/*    public HashMap<Integer,String>[] getVar2logicExprMap(boolean allowedForbidden)
    {
        String[] logExpr = new String[2];
        int index = 0;
        int i=0;
        for(int var:sourceStateDomain.vars())
        {
            logExpr[0] = "";//(";
            logExpr[1] = "";//(";
            for(String stateName:stateName2varsMap[index].keySet())
            {
                i=1;
                if(stateName2varsMap[index].get(stateName).equals("0"))
                    i = 0;

                if(allowedForbidden)
                    logExpr[i] += "Q^"+theAutomaton.getName()+" = "+stateName+OR;
                else
                    logExpr[i] += "Q^"+theAutomaton.getName()+" != "+stateName+OR;
            }

            logExpr[0] = logExpr[0].substring(0,logExpr[0].length()-3);
            logExpr[1] = logExpr[1].substring(0,logExpr[1].length()-3);

            var2logicExprMap[0].put(var,logExpr[0]);//+")");
            var2logicExprMap[1].put(var,logExpr[1]);//+")");

            index++;
        }

        return var2logicExprMap;

    }

    public String pad(String s, int nbrOfBits)
    {
        String result = s;
        if(s.length()<nbrOfBits)
            for(int i=0;i<(nbrOfBits-s.length());i++)
                result = "0"+result;
        return result;
    }*/

    public HashMap<Integer,String> getBDDIndex2SourceStateName()
    {
        return bddIndex2SourceStateName;
    }
/*
    public HashMap<String,String>[] getStateName2varsMap()
    {
        return stateName2varsMap;
    }
 */


    public BDD getForbiddenStateSet()
    {
        return forbiddenStateSet;
    }

    public BDD getAllowedStateSet()
    {
        return allowedStateSet;
    }

    public BDD getUncontrollableEvents()
    {
        return uncontrollableEventsBDD;
    }

    public String varExpr2stateTerm(final BDD varExpr, final BDD statesEnablingSigmaBDD, final BDDVarSet bddvarset, final boolean allowedForbidden)
    {
        forbiddenStateSet = manager.getZeroBDD();
        allowedStateSet = manager.getZeroBDD();

        String stateTerm = O_PAR;
        BDD stateBDD;
        int stateIndex;
        String stateTerm1 = O_PAR;
        String stateTerm2 = O_PAR;

        int n1=0,n2=0;

        for(final State state: theAutomaton.getStateSet())
        {
            stateIndex = bddAutomata.getStateIndex(theAutomaton, state);
            stateBDD = manager.getFactory().buildCube(stateIndex, this.getSourceStateDomain().vars());

            final BDD temp = varExpr.restrict(stateBDD);
            final BDD quantified = statesEnablingSigmaBDD.exist(bddvarset);
            final BDD temp2 = quantified.and(stateBDD);

/*            System.out.println(theAutomaton.getName()+ ": "+ state.getName());
            System.out.println("stateBDD: "+ stateBDD.toString());
//            System.out.println("statesEnablingSigmaBDD: "+ statesEnablingSigmaBDD.toString());
//            System.out.println("statesEnablingSigmaBDD and stateBDD: "+ statesEnablingSigmaBDD.and(stateBDD).toString());
            System.out.println("bddvarset: "+ bddvarset.toString());
//            System.out.println("quantified: "+ quantified.toString());
            System.out.println("temp2: "+ temp2.toString());
            System.out.println("temp: "+ temp.toString());
*/
            if(!temp.equals(varExpr) && !temp.isZero() && !statesEnablingSigmaBDD.and(stateBDD).isZero() && temp2.equals(stateBDD))
            {
 //              System.out.println("RESTRICT: "+ temp.toString());
               n1++;
               if(allowedForbidden)
               {
                    stateTerm1 += "Q_"+theAutomaton.getName()+EQUALS+state.getName()+OR;
                    allowedStateSet = allowedStateSet.or(stateBDD);
               }
               else
               {
                    stateTerm1 += "Q_"+theAutomaton.getName()+NEQUALS+state.getName()+AND;
                    forbiddenStateSet = forbiddenStateSet.or(stateBDD);
               }

            }
            else
            {
                n2++;
                if(allowedForbidden)
                    stateTerm2 += "Q_"+theAutomaton.getName()+NEQUALS+state.getName()+OR;
                else
                    stateTerm2 += "Q_"+theAutomaton.getName()+EQUALS+state.getName()+AND;
            }
        }
        if(!stateTerm1.equals(O_PAR))
            stateTerm1 = stateTerm1.substring(0,stateTerm1.length()-3)+C_PAR;
        else
            stateTerm1 = "";

        if(!stateTerm2.equals(O_PAR))
            stateTerm2 = stateTerm2.substring(0,stateTerm2.length()-3)+C_PAR;
        else
            stateTerm2 = "";

        stateTerm = n2<n1 ? stateTerm2 : stateTerm1;
        nbrOfTerms = n2<n1 ? n2 : n1;

        if(n1 == nbrOfTerms)
            allwFrbdnChosen = true;
        else
            allwFrbdnChosen = false;

 //       stateTerm = stateTerm1;

        return stateTerm;
    }

    public int getNbrOfTerms()
    {
        return nbrOfTerms;
    }

    @Override
    public int hashCode()
    {
        return theAutomaton.hashCode();
    }

    @Override
    public boolean equals(final Object other)
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

    public BDD getUncontrollableTransitionBackwardBDD()
    {
        return uncontrollableTransitionBackwardBDD;
    }

    public BDD getTransitionForwardConjunctiveBDD()
    {
        return getTransitionForwardBDD();
    }

    public BDD getTransitionBackwardConjunctiveBDD()
    {
        return getTransitionBackwardBDD();
    }

    public BDD getTransitionForwardDisjunctiveBDD(final Alphabet sourceAlphabet)
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
