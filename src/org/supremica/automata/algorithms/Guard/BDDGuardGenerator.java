/*
 * BDDGuardGenerator.java
 *
 * Created on September 11, 2008, 12:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.Guard;

import java.util.*;
import net.sourceforge.waters.model.des.StateProxy;
import org.supremica.automata.*;
import org.supremica.automata.BDD.*;
import net.sf.javabdd.*;
import org.supremica.log.*;
import org.supremica.util.ArrayHelper;
import java.math.BigInteger;

/**
 *
 * @author Sajed
 */
public class BDDGuardGenerator {
    
    BDD mustAllowedStatesBDD;
    BDD mustForbiddenStatesBDD;
    BDD dontCareStatesBDD;
    BDD sigmaBDD; 
    BDD statesEnablingSigmaBDD;
    BDD safeStatesBDD;
    BDD careStatesBDD;
    BDD statesLeading2ForbiddenBDD;
    BDD forwardMonolithicTransitionsBDD;
    BDD backwardMonolithicTransitionsBDD;
    
    BDDAutomata automataBDD;
    BDDManager manager;

    Automata theAutomata;
    
    boolean allowedForbidden;
    
    String OR = " | ";
    String AND = " & ";
    String O_PAR = "";
    String C_PAR = "";
    int nbrOfTerms;
    String guard = "";
       
    private static Logger logger = LoggerFactory.createLogger(BDDAutomata.class);
    
    /** Creates a new instance of BDDGuardGenerator */
    public BDDGuardGenerator(Automata theAutomata, String eventLabel, boolean allowedForbidden) {
        long time = System.currentTimeMillis();
        this.theAutomata = theAutomata;
        automataBDD = new BDDAutomata(theAutomata);
        manager = automataBDD.getBDDManager();
        this.allowedForbidden = allowedForbidden;
        LabeledEvent event = new LabeledEvent(eventLabel);
        int currEventIndex = automataBDD.getEventIndex(event);
        sigmaBDD = manager.createBDD(currEventIndex, automataBDD.getEventDomain());
        
        BDDTransitions bddTransitions = new BDDTransitionFactory(automataBDD).createTransitions(); 
        
/*        BDDVarSet transitionVariables = automataBDD.getSourceStateVariables();
        transitionVariables = transitionVariables.union(automataBDD.getEventVarSet());       
        transitionVariables = transitionVariables.union(automataBDD.getDestStateVariables());
*/
        ((BDDMonolithicTransitions)bddTransitions).getMonolithicTransitionBackwardBDD();
        forwardMonolithicTransitionsBDD = ((BDDMonolithicTransitions)bddTransitions).getMyMonolithicTransitionForwardBDD();
        backwardMonolithicTransitionsBDD = ((BDDMonolithicTransitions)bddTransitions).getMyMonolithicTransitionBackwardBDD();

        safeStatesBDD = automataBDD.getReachableAndCoreachableStates();
        System.out.println("number of safe states: "+safeStatesBDD.satCount(automataBDD.getSourceStateVariables()));
        
        computeStatesEnablingSigma();
/*        System.out.println("states enabling sigma:");        
          printStates(statesEnablingSigmaBDD);
*/ 
        computeStatesLeading2ForbiddenStates();
/*        System.out.println("states leading to forbidden states:");
        printStates(statesLeading2ForbiddenBDD);*/
        computeMustAllowedSates();
/*        System.out.println("states must be allowed states:");        
        printStates(mustAllowedStatesBDD);*/
        computeMustForbiddenSates();
/*        System.out.println("states must be forbidden:");        
        printStates(mustForbiddenStatesBDD);*/
        
        computeCareStates();
//        computeDontCareStates();
//        mustForbiddenStatesBDD.simplify(careStatesBDD.toVarSet()).printDot();
//        mustAllowedStatesBDD.printDot();
        
        nbrOfTerms = 0;
        if(allowedForbidden)
        {
            guard = generateGuard(mustAllowedStatesBDD);
            System.out.println("Allowed guard for event "+event.getName()+": "+guard);
        }
        else
        {
            guard = generateGuard(mustForbiddenStatesBDD);
            System.out.println("Forbidden guard for event "+event.getName()+": "+guard);
        }
        System.out.println("Number of terms in the expression: "+nbrOfTerms);
        System.out.println("The guard was generated in "+(System.currentTimeMillis()-time)+" millisecs");
        
//        freeBDDs();

    }
    
    public String getGuard()
    {
        return guard;
    }
    
    public void freeBDDs()
    {    
        mustAllowedStatesBDD.free();
        mustForbiddenStatesBDD.free();
//        dontCareStatesBDD.free();
        sigmaBDD.free(); 
        statesEnablingSigmaBDD.free();
        safeStatesBDD.free();
        careStatesBDD.free();
        statesLeading2ForbiddenBDD.free();
        forwardMonolithicTransitionsBDD.free();
        backwardMonolithicTransitionsBDD.free();
    }
    
    //Q^sigma
    public void computeStatesEnablingSigma()
    {
        statesEnablingSigmaBDD = forwardMonolithicTransitionsBDD.relprod(sigmaBDD, automataBDD.getDestStateVariables());        
        statesEnablingSigmaBDD = statesEnablingSigmaBDD.exist(automataBDD.getEventVarSet());
        
//        return statesEnablingSigmaBDD;
    }
    
    public void computeStatesLeading2ForbiddenStates()
    {
        BDD forbiddenAndReachableStatesBDD = automataBDD.getReachableStates();
        forbiddenAndReachableStatesBDD = forbiddenAndReachableStatesBDD.and(safeStatesBDD.not());      
/*        
        System.out.println("forbidden and reachable states:");
        printStates(forbiddenAndReachableStatesBDD);     
*/      
        BDD targetSigmaStatesBDD;
        targetSigmaStatesBDD = statesEnablingSigmaBDD.relprod(forwardMonolithicTransitionsBDD.and(sigmaBDD), automataBDD.getEventVarSet());
        targetSigmaStatesBDD = targetSigmaStatesBDD.exist(automataBDD.getSourceStateVariables());
        targetSigmaStatesBDD.replaceWith(manager.makePairing(automataBDD.getDestStateDomains(),automataBDD.getSourceStateDomains()));
/*       
        System.out.println("target sigma states:");
        printStates(targetSigmaStatesBDD);
*/       
        forbiddenAndReachableStatesBDD.andWith(targetSigmaStatesBDD);
        
/*        System.out.println("forbidden and reachable statesssssssssssssss:");
        printStates(forbiddenAndReachableStatesBDD);
*/
        statesLeading2ForbiddenBDD = backwardMonolithicTransitionsBDD.relprod(forbiddenAndReachableStatesBDD, automataBDD.getSourceStateVariables());        
        statesLeading2ForbiddenBDD = statesLeading2ForbiddenBDD.exist(automataBDD.getEventVarSet());      
        statesLeading2ForbiddenBDD.replaceWith(manager.makePairing(automataBDD.getDestStateDomains(),automataBDD.getSourceStateDomains()));
        
        statesLeading2ForbiddenBDD = statesLeading2ForbiddenBDD.and(statesEnablingSigmaBDD);
        
//        return statesLeading2ForbiddenBDD;
    }
    
    //Q^sigma_sup
    public void computeMustAllowedSates()
    {
        mustAllowedStatesBDD = safeStatesBDD.and(statesEnablingSigmaBDD);
        mustAllowedStatesBDD.andWith(statesLeading2ForbiddenBDD.not());
        
//        return mustAllowedStatesBDD;
    }
    
    //Q^sigma & C(Q^sigma_sup) & Q_reach & Q_sup
    public void computeMustForbiddenSates()
    {
        mustForbiddenStatesBDD = safeStatesBDD.and(automataBDD.getReachableStates());
        mustForbiddenStatesBDD = mustForbiddenStatesBDD.and(statesEnablingSigmaBDD);
        mustForbiddenStatesBDD.andWith(mustAllowedStatesBDD.not());
    
//        return mustForbiddenStatesBDD;
    }
    
    public void computeCareStates()
    {
        careStatesBDD = mustAllowedStatesBDD.or(mustForbiddenStatesBDD);
    }
    
    //Q & C(mustForbiddenStatesBDD) & C(mustAllowedStatesBDD)   OR C(careStatesBDD)
    public void computeDontCareStates()
    {
//        dontCareStatesBDD = mustForbiddenStatesBDD.not().and(mustAllowedStatesBDD.not();
          dontCareStatesBDD = careStatesBDD.not();
        
//        return dontCareStatesBDD;
    }
    
    public BDD getCareStates()
    {
        return careStatesBDD;
    }
    
    public BDD getMustAllowedSates()
    {
        return mustAllowedStatesBDD;
    }
    
    public BDD getMustForbiddenSates()
    {
        return mustForbiddenStatesBDD;
    }
        
    public BDD getDontCareStates()
    {
        return dontCareStatesBDD;
    }
    
    public BDD getStatesEnablingSigma()
    {
        return statesEnablingSigmaBDD;
    }
    
    public BDD getSatesLeading2ForbiddenBDD()
    {
        return statesLeading2ForbiddenBDD;
    }

    public void printStates(BDD states)
    {
        HashMap<String,Integer> automaton2indexMap;
  //    HashMap<Integer,String>[][] var2logicExprAutomta;
        HashMap<Integer,String>[] bddIndex2SourceStateName;
        
        automaton2indexMap = new HashMap<String,Integer>();
        bddIndex2SourceStateName = new HashMap[theAutomata.size()];
        int index = 0;
        for(Automaton aut:theAutomata)
        {
            BDDAutomaton currBDDAutomaton = automataBDD.getBDDAutomaton(aut);
            automaton2indexMap.put(theAutomata.getAutomatonAt(index).getName(),index);
            bddIndex2SourceStateName[index] = currBDDAutomaton.getBDDIndex2SourceStateName();        
            index++;
        }
        
        BDDDomain[] sourceStateDomains = automataBDD.getSourceStateDomains();

        int[] sourceStateDomainIndicies = new int[sourceStateDomains.length];
        for (int i = 0; i < sourceStateDomains.length; i++)
        {

//            logger.info("Source state domain " + sourceStateDomains[i].getName() + ": " + ArrayHelper.arrayToString(sourceStateDomains[i].vars()));
            sourceStateDomainIndicies[i] = sourceStateDomains[i].getIndex();
        } 
        
//        logger.info("sourceStateDomainIndicies: " + ArrayHelper.arrayToString(sourceStateDomainIndicies));
        
        int[] stateArray = new int[sourceStateDomains.length];
        
        
        // Create all states
        for ( BDD.BDDIterator satIt = new BDD.BDDIterator(states, automataBDD.getSourceStateVariables()); satIt.hasNext(); ) 
        {
            BigInteger[] currSat = satIt.nextTuple();
//            System.out.println("currStat: " + ArrayHelper.arrayToString(currSat));
            String currStatName = "";
            int automatonIndex = -1;
            for (int i = 0; i < sourceStateDomainIndicies.length; i++)
            {
                stateArray[i] = currSat[sourceStateDomainIndicies[i]].intValue();
                automatonIndex = automaton2indexMap.get(sourceStateDomains[i].getName());
                
                currStatName += bddIndex2SourceStateName[automatonIndex].get(stateArray[i]);
                if(i != (sourceStateDomainIndicies.length-1))
                    currStatName += ".";
//                currStatName += stateIndex2StateMap[automatonIndex].get(stateArray[i])+".";
            }
            
//            System.out.println("current state: " + ArrayHelper.arrayToString(stateArray));
            System.out.println(currStatName);
        }      
    }
    
    public boolean guardIsTRUE()
    {
        if(guard.equals("True"))
            return true;
        return false;
    }
    
    public String generateGuard(BDD states)
    {
//        printStates(states);
        String guard = "";
        if(states.equals(careStatesBDD))
        {
            guard = allowedForbidden ? "True" : "False";
            nbrOfTerms = 0;
        }
        else if(states.satCount(automataBDD.getSourceStateVariables()) == 0)
        {
            guard = allowedForbidden ? "False" : "True";
            nbrOfTerms = 0;
        }
        else
        {
            BDD goodBDD = states.simplify(careStatesBDD.toVarSet());
            if(states.nodeCount() <= goodBDD.nodeCount())
                goodBDD = states;
            
//            System.out.println("sat: "+oneSat.toString());
//            System.out.println("good bdd: "+goodBDD.toString());
            
            String stateTerm = "";

            BDD oneSat;
            BDD temp = goodBDD;
            String partialGuard = "";
            int partialNbrOfTerms = 0;
            boolean flag2 = true;
//            while(!temp.isZero())
            while(!temp.toString().equals(""))    
            {
                flag2 = true;
                oneSat = temp.satOne();
                partialGuard = O_PAR;
                partialNbrOfTerms = 0;

                for(Automaton aut: theAutomata)
                {
                    BDDVarSet bddvarset = null;
                    boolean flag = true;
                    
                    //Buld a variable set including all variables in the automata except 'aut'
                    for(Automaton inAut: theAutomata)
                    {
                        if(flag)
                        {
                            if(!aut.equals(inAut))
                            {
                                bddvarset = automataBDD.getBDDAutomaton(inAut).getSourceStateDomain().set();
                                flag = false;
                            }
                        }
                        else
                        {
                            if(!aut.equals(inAut))
                                bddvarset = bddvarset.union(automataBDD.getBDDAutomaton(inAut).getSourceStateDomain().set());
                        }
                        
                    }
                    
                    BDDAutomaton bddAutomaton = automataBDD.getBDDAutomaton(aut);
                    stateTerm = bddAutomaton.varExpr2stateTerm(oneSat, statesEnablingSigmaBDD.and(safeStatesBDD), bddvarset, allowedForbidden);
                                       
                    if(!stateTerm.equals(""))
                    {
                        if(allowedForbidden)
                        {
                            if(!bddAutomaton.getAllowedStateSet().isZero() && bddAutomaton.getAllowedStateSet().and(getMustForbiddenSates()).isZero())
                            {
                                partialGuard = stateTerm;
                                partialNbrOfTerms = bddAutomaton.getNbrOfTerms();
                                flag2 = false;
                                break;
                            }
                        }
                        else
                        {
                            if(!bddAutomaton.getForbiddenStateSet().isZero() &&  bddAutomaton.getForbiddenStateSet().and(getMustAllowedSates()).isZero())
                            {
                                partialGuard = stateTerm;
                                partialNbrOfTerms = bddAutomaton.getNbrOfTerms();
                                flag2 = false;
                                break;
                            }
                        }

                        partialNbrOfTerms += bddAutomaton.getNbrOfTerms();
                        if(allowedForbidden)
                            partialGuard += stateTerm + AND;
                        else
                            partialGuard += stateTerm + OR;
                    }

                }
                nbrOfTerms += partialNbrOfTerms;

                if(flag2)
                {
                    if(partialGuard.length()>2)
                        partialGuard = partialGuard.substring(0, partialGuard.length()-3);

                    partialGuard += C_PAR;
                }
               
                if(!temp.toString().equals(""))    
                {
                    guard += partialGuard;
                }
                
                temp = temp.and(oneSat.not());
                
                if(!temp.toString().equals(""))
                {
                    if(allowedForbidden)
                        guard += OR;
                    else
                        guard += AND;
                }
            }
            
        }
        
        
        return guard;
    }
 /*   
    public int var2AutomatonIndex(int var)
    {
        int index = -1;
        
        BDDDomain[] sourceStateDomains = automataBDD.getSourceStateDomains();

        for (int i = 0; i < sourceStateDomains.length; i++)
        {
           int[] vars = sourceStateDomains[i].vars();
           
           for(int v : vars)
               if(v == var)
                   return automaton2indexMap.get(sourceStateDomains[i].getName());
        }         
        
        return index;
    }
*/

}
