
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
import net.sf.javabdd.*;
import java.util.*;
import org.supremica.automata.*;
import org.supremica.util.ArrayHelper;

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
    BDD forbiddenStateSet;
    BDD allowedStateSet;
    BDD selfLoopsBDD;
    BDD uncontrollableEventsBDD;
    
    HashMap<Integer,String> bddIndex2SourceStateName;
    public HashMap<Integer,String> myIndex2stateName;
    public HashMap<String,HashSet<Integer>> enablingSigmaMap;
/*    HashMap<String,String>[] stateName2varsMap;
    HashMap<Integer,String>[] var2logicExprMap;
 */   
    String OR = " | ";
    String AND = " & ";
    String O_PAR = "(";
    String C_PAR = ")";
    String EQUALS = " = ";
    String NEQUALS = " != ";

    boolean isTransSelfLoop = false;
    
    
    int nbrOfTerms;
    public boolean allwFrbdnChosen = false;
    
    BDDAutomaton(BDDAutomata bddAutomata, Automaton theAutomaton, BDDDomain sourceStateDomain, BDDDomain destStateDomain)
    {
/*        System.out.println("automaton name: "+theAutomaton.getName());
        System.out.println("source state domain vars: ");
        int[] vars = sourceStateDomain.vars();
        for(int i = 0;i<vars.length;i++)
            System.out.println(""+vars[i]);
*/
        this.manager = bddAutomata.getBDDManager();
        
        this.bddAutomata = bddAutomata;
        this.theAutomaton = theAutomaton;
        
        this.sourceStateDomain = sourceStateDomain;
        this.destStateDomain = destStateDomain;
        
        bddIndex2SourceStateName = new HashMap<Integer,String>();
        enablingSigmaMap = new HashMap<String, HashSet<Integer>>();
        myIndex2stateName = new HashMap<Integer, String>();
 /*       
        stateName2varsMap = new HashMap[sourceStateDomain.varNum()];
        for(int i=0;i<sourceStateDomain.varNum();i++)
                stateName2varsMap[i] = new HashMap<String,String>();

        var2logicExprMap = new HashMap[2];
        var2logicExprMap[0] = new HashMap<Integer,String>();
        var2logicExprMap[1] = new HashMap<Integer,String>();
 */       
        sourceToDestPairing = manager.makePairing(sourceStateDomain, destStateDomain);
        destToSourcePairing = manager.makePairing(destStateDomain, sourceStateDomain);
        
        transitionForwardBDD = manager.getZeroBDD();
        transitionBackwardBDD = manager.getZeroBDD();
        
        transitionForwardDisjunctiveBDD = manager.getZeroBDD();
        transitionBackwardDisjunctiveBDD = manager.getZeroBDD();

        uncontrollableEventsBDD = manager.getZeroBDD();
        selfLoopsBDD = manager.getZeroBDD();

        nbrOfTerms = 0;
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

//        System.out.println("Automaton "+theAutomaton.getName());
        for (State currState : theAutomaton)
        {
            isTransSelfLoop = false;
            // First create all transitions in this automaton
            for (Iterator<Arc> arcIt = currState.outgoingArcsIterator(); arcIt.hasNext(); )
            {
                Arc currArc = arcIt.next();
                addTransition(currArc);
            }

            isTransSelfLoop = true;
            // Self loop events not in this alphabet
            for (LabeledEvent event : inverseAlphabet)
            {
                addTransition(currState, currState, event);
            }

            // Then add state properties
            int stateIndex = bddAutomata.getStateIndex(theAutomaton, currState);
            
//            System.out.println("Automaton: "+theAutomaton.getName()+"------------- currState: "+currState.getName()+"------- stateIndex"+stateIndex);
            if (currState.isInitial())
            {
               BDDManager.addState(initialStates, stateIndex, sourceStateDomain);
            }
            if (currState.isAccepting())
            {
                BDDManager.addState(markedStates, stateIndex, sourceStateDomain);
/*                System.out.println("in BDDAutomaton: "+theAutomaton.getName());
                for ( BDD.BDDIterator satIt = new BDD.BDDIterator(markedStates, sourceStateDomain.set()); satIt.hasNext(); ) 
                {
                    BigInteger[] currSat = satIt.nextTuple();
                    System.out.println("currStat: " + ArrayHelper.arrayToString(currSat));
                }                
*/               
            }
            if (currState.isForbidden())
            {
               BDDManager.addState(forbiddenStates, stateIndex, sourceStateDomain);
            }
        }

        Iterator <LabeledEvent> eventItr = theAutomaton.eventIterator();
        BDD sigmaBDD;
        int currEventIndex;
        while(eventItr.hasNext())
        {
            LabeledEvent event = eventItr.next();
            currEventIndex = bddAutomata.getEventIndex(event);
            sigmaBDD = manager.createBDD(currEventIndex, bddAutomata.getEventDomain());
            if(!event.isControllable())
                uncontrollableEventsBDD.orWith(sigmaBDD);
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

//        System.out.println("state name: "+sourceState.getName());
//        System.out.println("sourceStateIndex: "+sourceStateIndex);

        BDD sourceBDD = manager.factory.buildCube(sourceStateIndex, getSourceStateDomain().vars());
        Integer myIndex = generateIndex(sourceBDD);

        if(!isTransSelfLoop)
        {
            if(!enablingSigmaMap.containsKey(theEvent.getName()))
            {
                HashSet<Integer> s = new HashSet<Integer>();
                s.add(myIndex);
                enablingSigmaMap.put(theEvent.getName(), s);
            }
            else
            {
                enablingSigmaMap.get(theEvent.getName()).add(myIndex);
            }
            
        }

        Integer bddIndex = -1;
//        String varsBits = "";
        if(!bddIndex2SourceStateName.containsValue(sourceState.getName()))
        {
//            sourceBDD.printDot();
            
            myIndex2stateName.put(myIndex, sourceState.getName());


            BDD.BDDIterator satIt = new BDD.BDDIterator(sourceBDD, getSourceStateDomain().set());
            BigInteger[] currSat = satIt.nextTuple();
//            System.out.println("currSat: " + ArrayHelper.arrayToString(currSat));
            for(int i=0; i<currSat.length;i++)
            {
                if(currSat[i] != null)
                {
                     bddIndex = currSat[i].intValue();
//                     varsBits = pad(currSat[i].toString(2),sourceStateDomain.varNum());
                     break;
                }
            }
            
            bddIndex2SourceStateName.put(bddIndex,sourceState.getName());
//            System.out.println("state name: "+sourceState.getName());
//            System.out.println("BDD index: "+bddIndex);
            
 /*           for(int i=0;i<sourceStateDomain.varNum();i++)
            {
                stateName2varsMap[i].put(sourceState.getName(),""+varsBits.charAt(i));
            }*/
        }

        if(isTransSelfLoop)
        {
            BDDManager.addTransition(selfLoopsBDD, sourceStateIndex, sourceStateDomain, destStateIndex, destStateDomain, eventIndex, bddAutomata.getEventDomain());
        }

        BDDManager.addTransition(transitionForwardBDD, sourceStateIndex, sourceStateDomain, destStateIndex, destStateDomain, eventIndex, bddAutomata.getEventDomain());
        BDDManager.addTransition(transitionBackwardBDD, destStateIndex, sourceStateDomain, sourceStateIndex, destStateDomain, eventIndex, bddAutomata.getEventDomain());
    }

    public HashSet<Integer> getComplementIndices(HashSet<Integer> indices)
    {
        HashSet<Integer> output = new HashSet<Integer>();
        for(Integer i:myIndex2stateName.keySet())
        {
            if(!indices.contains(i))
                output.add(i);
        }
        return output;
    }

    public Integer generateIndex(BDD bdd)
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
                int temp = generateIndex(bdd.high());
                return (temp+pow2(bddAutomata.bddVar2bitValue.get(bdd.var())));
            }
            else
            {
                return generateIndex(bdd.low());
            }
        }
    }

    public int pow2(int p)
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

    public BDD getSelfLoopsBDD(){
        return selfLoopsBDD;
    }

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

    public String varExpr2stateTerm(BDD varExpr, BDD statesEnablingSigmaBDD, BDDVarSet bddvarset, boolean allowedForbidden)
    {
        forbiddenStateSet = manager.getZeroBDD();
        allowedStateSet = manager.getZeroBDD();
        
        String stateTerm = O_PAR;
        BDD stateBDD;
        int stateIndex;
        String stateTerm1 = O_PAR;
        String stateTerm2 = O_PAR;
        
        int n1=0,n2=0;
        
        for(State state: theAutomaton.getStateSet())
        {
            stateIndex = bddAutomata.getStateIndex(theAutomaton, state);
            stateBDD = manager.getFactory().buildCube(stateIndex, this.getSourceStateDomain().vars());
                        
            BDD temp = varExpr.restrict(stateBDD);
            BDD quantified = statesEnablingSigmaBDD.exist(bddvarset);
            BDD temp2 = quantified.and(stateBDD);
            
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
