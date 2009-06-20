/*
 * BDDGuardGenerator.java
 *
 * Created on September 11, 2008, 12:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.Guard;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import net.sourceforge.waters.model.des.StateProxy;
import org.supremica.automata.*;
import org.supremica.automata.BDD.*;
import net.sf.javabdd.*;
import org.supremica.log.*;
import org.supremica.util.ArrayHelper;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.util.HashMap;

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
    BDD safeStatesEnablingSigmaBDD;
    
    BDDAutomata automataBDD;
    BDDManager manager;

    Automata theAutomata;
    
    boolean allowedForbidden;
    
    String OR = " | ";
    String AND = " & ";
    String O_PAR = "(";
    String C_PAR = ")";
    int nbrOfTerms;
    String guard = "";
    String TRUE = "True";
    String FALSE = "False";
    LabeledEvent event;

    int bddSize;
    long runTime;

    boolean logEnabled = false;
       
    private static Logger logger = LoggerFactory.createLogger(BDDAutomata.class);

    //declarations for new implentation
    HashMap<Integer, HashSet<Integer>> nodeHash2stateIndices = new HashMap<Integer, HashSet<Integer>>();
    HashMap<String, HashSet<String>> toOneStatesMap = new HashMap<String, HashSet<String>>();
    HashMap<String, HashSet<String>> toOneStatesSigmaMap = new HashMap<String, HashSet<String>>();
    HashMap<String, Integer> automaton2MBDDLevel;
    HashMap<Integer, String> MBDDLevel2automaton;
    HashSet<String> independentStates;
    boolean isSigmaBDD;

    /** Creates a new instance of BDDGuardGenerator */
    public BDDGuardGenerator(BDDAutomata bddAutomata, String eventLabel, BDD safeStates, boolean allowedForbidden) {
        long time = System.currentTimeMillis();
        theAutomata = bddAutomata.getAutomata();
        automataBDD = bddAutomata;
        manager = automataBDD.getBDDManager();
        this.allowedForbidden = allowedForbidden;
        event = new LabeledEvent(eventLabel);
        int currEventIndex = automataBDD.getEventIndex(event);
        sigmaBDD = manager.createBDD(currEventIndex, automataBDD.getEventDomain());
        
        BDDTransitions bddTransitions = automataBDD.getBDDTransitions();
        //new BDDTransitionFactory(automataBDD).createTransitions();
        
/*        BDDVarSet transitionVariables = automataBDD.getSourceStateVariables();
        transitionVariables = transitionVariables.union(automataBDD.getEventVarSet());       
        transitionVariables = transitionVariables.union(automataBDD.getDestStateVariables());
*/
        ((BDDMonolithicTransitions)bddTransitions).getMonolithicTransitionBackwardBDD();
        forwardMonolithicTransitionsBDD = ((BDDMonolithicTransitions)bddTransitions).getMyMonolithicTransitionForwardBDD();
        backwardMonolithicTransitionsBDD = ((BDDMonolithicTransitions)bddTransitions).getMyMonolithicTransitionBackwardBDD();

//        safeStatesBDD = automataBDD.getReachableAndCoreachableStates();

//        BDD prelUnconStates = manager.prelimUncontrollableStates(automataBDD);
//        BDD forbiddenStates = prelUnconStates.or(automataBDD.getForbiddenStates());
//        safeStatesBDD = manager.safeStateSynthesis(automataBDD, forbiddenStates).and(automataBDD.getReachableStates());
//        printStates(safeStatesBDD);

        safeStatesBDD = safeStates;

        if(logEnabled)
            System.out.println("number of safe states: "+safeStatesBDD.satCount(automataBDD.getSourceStateVariables()));
        
        computeStatesEnablingSigma();

        safeStatesEnablingSigmaBDD = statesEnablingSigmaBDD.and(safeStatesBDD);
//        System.out.println("states enabling sigma:");
//        printStates(statesEnablingSigmaBDD);


/*        System.out.println("reachable states:");
        printStates(automataBDD.getReachableStates());
        automataBDD.getReachableStates().printDot();
        System.out.println("reachable guard: "+generateReachGuard(automataBDD.getReachableStates()));
*/

        computeStatesLeading2ForbiddenStates();
/*        System.out.println("states leading to forbidden states:");
        printStates(statesLeading2ForbiddenBDD);*/

        computeMustAllowedSates();
//        System.out.println("states must be allowed states:");
//        mustAllowedStatesBDD.printDot();
//        printStates(mustAllowedStatesBDD);
//        BDD2Expr(mustAllowedStatesBDD);

        computeMustForbiddenSates();
//        System.out.println("states must be forbidden:");
//        mustForbiddenStatesBDD.printDot();
//        printStates(mustForbiddenStatesBDD);

        computeCareStates();
//        System.out.println("care states:");
//        printStates(getCareStatesBDD);

//        computeDontCareStates();
//        mustForbiddenStatesBDD.simplify(careStatesBDD.toVarSet()).printDot();
//        mustAllowedStatesBDD.printDot();

/*        if(mustAllowedStatesBDD.nodeCount()>0)
        {
            MDD mdd = generateMDD(mustAllowedStatesBDD);
            MDD2DOT(mdd, "C:/Users/sajed/Desktop/mdd_"+eventLabel+".dot");
        }
*/

//        printStates(manager.safeStateSynthesis(automataBDD, prelUnconStates.or(automataBDD.getForbiddenStates())).and(automataBDD.getReachableStates()));
        
        nbrOfTerms = 0;
        if(allowedForbidden)
        {
            guard = generateGuard(mustAllowedStatesBDD);
            System.out.println("Allowed guard for event "+event.getName()+": "+guard);
            System.out.println("NEW Allowed guard for event "+event.getName()+": "+generateGuardNEW(mustAllowedStatesBDD));
        }
        else
        {
            guard = generateGuard(mustForbiddenStatesBDD);
            System.out.println("Forbidden guard for event "+event.getName()+": "+guard);
            System.out.println("NEW Forbidden guard for event "+event.getName()+": "+generateGuardNEW(mustForbiddenStatesBDD));
        }

        runTime = System.currentTimeMillis()-time;
        if(logEnabled)
        {
            System.out.println("Number of terms in the expression: "+nbrOfTerms);
            System.out.println("The guard was generated in "+runTime+" millisecs");
        }
        
/*        Iterator<BDDAutomaton> bddAutIt = automataBDD.iterator();
        while(bddAutIt.hasNext())
        {
            BDDAutomaton bddAut = bddAutIt.next();
            if(bddAut.enablingSigmaMap.containsKey(event.getName()))
                System.out.println(bddAut.getAutomaton().getName()+": "+bddAut.enablingSigmaMap.get(event.getName()));
        }
*/
//        freeBDDs();

    }

    public long getRunTime()
    {
        return runTime;
    }
    
    public String getGuard()
    {
        return guard;
    }

    public int getBDDSize()
    {
        return bddSize;
    }

    public int getNbrOfTerms()
    {
        return nbrOfTerms;
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
    
    public BDD getMustAllowedStates()
    {
        return mustAllowedStatesBDD;
    }
    
    public BDD getMustForbiddenStates()
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
    
    public boolean guardIsTrueOrFalse()
    {
        if(guard.equals(TRUE) || guard.equals(FALSE))
            return true;
        return false;
    }

    public String generateGuardNEW(BDD states)
    {
        String guard = "";
        BDDVarSet careVarSet = careStatesBDD.toVarSet();
        if(states.equals(careStatesBDD))
        {
            guard = allowedForbidden ? TRUE : FALSE;
            nbrOfTerms = 0;
        }
        else if(states.satCount(automataBDD.getSourceStateVariables()) == 0)
        {
            guard = allowedForbidden ? FALSE : TRUE;
            nbrOfTerms = 0;
        }
        else
        {
            BDD goodBDD = states.simplify(careVarSet);

            if(states.nodeCount() <= goodBDD.nodeCount())
                goodBDD = states;

            if(logEnabled)
                goodBDD.printDot();

            if(goodBDD.nodeCount()>0)
            {
                isSigmaBDD = true;
                MDD safeStatesEnablingSigmaMDD = generateMBDD(safeStatesEnablingSigmaBDD);
                isSigmaBDD = false;
//                MDD2DOT(safeStatesEnablingSigmaMDD, "C:/Users/sajed/Desktop/MDD_files/mddSigma_"+event.getName()+".dot");
                
                toOneStatesSigmaMap = statesInWayToNode(safeStatesEnablingSigmaMDD, safeStatesEnablingSigmaMDD.getNode(1), toOneStatesSigmaMap);


/*                System.out.println("************************");
                for(String aut:toOneStatesSigmaMap.keySet())
                {
                    if(aut.equals("SeqR3325"))
                    {
                        System.out.println(aut+": ");
                        for(String st:toOneStatesSigmaMap.get(aut))
                            System.out.print(st+" , ");
                        System.out.println("");
                        System.out.println("########");
                    }
                }*/

                MDD goodMDD = generateMBDD(goodBDD);
                MDD otherMDD;
                if(allowedForbidden)
                    otherMDD = generateMBDD(mustForbiddenStatesBDD);
                else
                    otherMDD = generateMBDD(mustAllowedStatesBDD);

//                computeToOneStates(otherMDD, otherMDD.getNode(1));

                toOneStatesMap = statesInWayToNode(otherMDD, otherMDD.getNode(1), toOneStatesMap);

                guard = generateExpr(goodMDD);

//                MDD2DOT(goodMDD, "C:/Users/sajed/Desktop/MDD_files/mdd_"+event.getName()+".dot");
//                MDD2DOT(otherMDD, "C:/Users/sajed/Desktop/MDD_files/otherMDD_"+event.getName()+".dot");
            }
        }

        return guard;
    }
    
    public String generateGuard(BDD states)
    {
        //printStates(states);
        //states.printDot();
        boolean flag2 = true;
        String guard = "";
        BDDVarSet careVarSet = careStatesBDD.toVarSet();
        if(states.equals(careStatesBDD))
        {
            guard = allowedForbidden ? TRUE : FALSE;
            nbrOfTerms = 0;
        }
        else if(states.satCount(automataBDD.getSourceStateVariables()) == 0)
        {
            guard = allowedForbidden ? FALSE : TRUE;
            nbrOfTerms = 0;
        }
        else
        {
            BDD goodBDD = states.simplify(careVarSet);
            
            if(states.nodeCount() <= goodBDD.nodeCount())
                goodBDD = states;

            if(logEnabled)
                goodBDD.printDot();

            String stateTerm = "";

            BDD oneSat;
            BDD temp = goodBDD;
            String partialGuard = "";
            int partialNbrOfTerms = 0;

            while(!temp.toString().equals(""))    
            {
//                System.out.println("temp: "+temp.toString());
                flag2 = true;
                oneSat = temp.satOne();
                partialGuard = O_PAR;
                partialNbrOfTerms = 0;

                for(Automaton aut: theAutomata)
                {
                    BDDVarSet bddvarset = automataBDD.getInverseSourceStateVars(aut);
//                    System.out.println("satOOOOOOOOOOOOne: "+ oneSat.toString());
                    BDDAutomaton bddAutomaton = automataBDD.getBDDAutomaton(aut);
                    stateTerm = bddAutomaton.varExpr2stateTerm(oneSat, statesEnablingSigmaBDD.and(safeStatesBDD), bddvarset, allowedForbidden);
//                    System.out.println("stateTermmmmmmmmmm: "+stateTerm);
                                       
                    if(!stateTerm.equals(""))
                    {
                        if(allowedForbidden)
                        {
                            if(!bddAutomaton.getAllowedStateSet().isZero() && bddAutomaton.getAllowedStateSet().and(getMustForbiddenStates()).isZero())
                            {
                                partialGuard = stateTerm;
                                partialNbrOfTerms = bddAutomaton.getNbrOfTerms();
                                flag2 = false;
                                break;
                            }
                        }
                        else
                        {
                            if(!bddAutomaton.getForbiddenStateSet().isZero() &&  bddAutomaton.getForbiddenStateSet().and(getMustAllowedStates()).isZero())
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

                if(flag2)
                {
                    if(partialGuard.length()>2)
                        partialGuard = partialGuard.substring(0, partialGuard.length()-3);

                    partialGuard += C_PAR;
                }

                boolean alreadyExists = guard.contains(partialGuard);

                if(!temp.toString().equals("") && !alreadyExists)
                {
                    guard += partialGuard;
                    nbrOfTerms += partialNbrOfTerms;
                }
                
                temp = temp.and(oneSat.not());
                
                if(!temp.toString().equals("") && !alreadyExists)
                {
                    if(allowedForbidden)
                        guard += OR;
                    else
                        guard += AND;
                }

            }
        }

        String[] st = guard.split(" ");
        if(st[st.length-1].equals(OR.trim()) || st[st.length-1].equals(AND.trim()))
        {
            guard = guard.substring(0, guard.length()-3);
        }

        return guard;
    }

    public MDD generateMBDD(BDD bdd)
    {
        MDD mdd = new MDD();

        automaton2MBDDLevel = new HashMap<String, Integer>();
        MBDDLevel2automaton = new HashMap<Integer, String>();

        if(bdd.nodeCount() > 0)
        {
            MDDNode root = new MDDNode(bdd.hashCode(),automataBDD.bddVar2AutName.get(bdd.var()));
            mdd.addNode(root);
            mdd.setRoot(root);

            automaton2MBDDLevel.put(root.getName(), bdd.level());
            MBDDLevel2automaton.put(bdd.var(), root.getName());

            HashSet<Integer> initSet = new HashSet<Integer>();
            initSet.add(0);
            int bitValue = automataBDD.bddVar2bitValue.get(bdd.var());
            HashMap<Integer,String> map = automataBDD.getBDDAutomaton(root.getName()).myIndex2stateName;
            if(bitValue != 0)
            {
                for(int i = 1; i<=(pow2(bitValue)-1);i++)
                {
                    if(map.get(i) != null)
                    initSet.add(i);
                }
            }
/*            System.out.println("#######");
            System.out.println(bitValue+": ");

            for(Integer i: initSet)
                System.out.print(i+" , ");
            System.out.println("");
*/

            traverseBDD(bdd.low(), initSet, bdd.var(), 0, mdd, root);
            traverseBDD(bdd.high(), initSet, bdd.var(), 1, mdd, root);
        }
        else
        {
            mdd.addNode(new MDDNode(0,"0"));
            mdd.addNode(new MDDNode(1,"1"));
        }


        return mdd;
    }

    public boolean traverseBDD(BDD bdd, HashSet<Integer> set, int lastVar, int LowHigh, MDD mdd, MDDNode root)
    {
        if(bdd.isOne() || bdd.isZero() || !automataBDD.bddVar2AutName.get(bdd.var()).equals(automataBDD.bddVar2AutName.get(lastVar)))
        {

            HashSet<Integer> states = modifyStateIndices(set, LowHigh, lastVar, lastVar, true, bdd.hashCode());

            MDDNode node = mdd.getNode(bdd.hashCode());

            if(node == null)
            {
                if(bdd.isOne() || bdd.isZero())
                    node = new MDDNode(bdd.hashCode(),""+bdd.hashCode());
                else
                    node = new MDDNode(bdd.hashCode(),automataBDD.bddVar2AutName.get(bdd.var()));

                mdd.addNode(node);

                automaton2MBDDLevel.put(node.getName(), bdd.level());
                MBDDLevel2automaton.put(bdd.level(), node.getName());

/*                HashSet<Integer> ts = new HashSet<Integer>();
                for(Integer stIndex:states)
                {
                    ts = new HashSet<Integer>();
                    ts.add(stIndex);
                    mdd.addEdge(new MDDEdge(root, node, ts));
                }
*/

                mdd.addEdge(new MDDEdge(root, node, states));

                if(bdd.isOne() || bdd.isZero())
                {
                    return true;
                }
                else
                {
                    set = new HashSet<Integer>();
                    set.add(0);
                    traverseBDD(bdd.low(), set, bdd.var(), 0, mdd, node);
                    traverseBDD(bdd.high(), set, bdd.var(), 1, mdd, node);
                }
            }
            else
            {
/*                HashSet<Integer> ts = new HashSet<Integer>();
                for(Integer stIndex:states)
                {
                    ts = new HashSet<Integer>();
                    ts.add(stIndex);
                    mdd.addEdge(new MDDEdge(root, node, ts));
                }
*/
                mdd.addEdge(new MDDEdge(root, node, states));
            }
        }
        else
        {

            traverseBDD(bdd.low(), modifyStateIndices(set, LowHigh, lastVar, bdd.var(),false, bdd.hashCode()), bdd.var(), 0, mdd, root);

            traverseBDD(bdd.high(), modifyStateIndices(set, LowHigh, lastVar, bdd.var(),false, bdd.hashCode()), bdd.var(), 1, mdd, root);
//            if(!isSigmaBDD && fuckFlag && automataBDD.bddVar2AutName.get(lastVar).equals("SeqR3325"))
//                System.out.println("333333EXITTEDDDD MSI");
        }

        return false;

    }

    public HashSet<Integer> modifyStateIndices(HashSet<Integer> set, int b, int stVar, int fiVar, boolean nbrOfBitsFlag, int bddHashCode)
    {
        HashSet<Integer> output = new HashSet<Integer>();
        HashSet<Integer> temp = new HashSet<Integer>();
        int st = 0;
        BDDAutomaton bddAut = automataBDD.getBDDAutomaton(automataBDD.bddVar2AutName.get(fiVar));
        if(stVar != 0)
        {
            st = automataBDD.bddVar2bitValue.get(stVar);
            bddAut = automataBDD.getBDDAutomaton(automataBDD.bddVar2AutName.get(stVar));
        }
        Automaton aut = bddAut.getAutomaton();

        int fi = automataBDD.bddVar2bitValue.get(fiVar)-1;
        if(nbrOfBitsFlag)
        {
            fi = automataBDD.aut2nbrOfBits.get(automataBDD.bddVar2AutName.get(fiVar))-1;
        }

/*        if(!isSigmaBDD && fuckFlag && aut.getName().equals("SeqR3325"))
        {
            for(Integer i:set)
			{
                System.out.println("is: "+i);
            }
            System.out.println("branch: "+b);
            System.out.println("stVar: "+stVar);
            System.out.println("fiVar: "+fiVar);
            System.out.println("st: "+st);
            System.out.println("fi: "+fi);
        }*/
        HashMap<Integer,String> map = bddAut.myIndex2stateName;

        int element;

		if(b == 1)
		{
			for(Integer i:set)
			{
                element = i+pow2(st);
                temp.add(element);
			}
		}
        else
            temp = set;

        if(fi == (st+1))
        {
            for(Integer i:temp)
            {
                if(map.get(i) != null)
                {
                    if(isSigmaBDD || bddHashCode == 0 || toOneStatesSigmaMap.get(aut.getName()).contains(map.get(i)))
                        output.add(i);
                }
                
                element = i+pow2(st+1);
                if(map.get(element) != null)
                {
                    if(isSigmaBDD || bddHashCode == 0 || toOneStatesSigmaMap.get(aut.getName()).contains(map.get(element)))
                        output.add(element);
                }
            }
        }
        else if(fi > (st+1))
        {
            for(Integer i:temp)
            {
                for(int c =0; c <= (pow2(st+1)+pow2(fi)); c += pow2(st+1))
                {
                    element = i + c;
                    if(map.get(element) != null)
                    {
    /*                    int stateIndex = automataBDD.getStateIndex(aut, aut.getStateWithName(map.get(element)));
                        BDD stateBDD = manager.getFactory().buildCube(stateIndex, bddAut.getSourceStateDomain().vars());
                        BDD quantified = safeStatesEnablingSigmaBDD.exist(automataBDD.getInverseSourceStateVars(aut));
                        BDD temp2 = quantified.and(stateBDD);
                        BDD tBDD = safeStatesEnablingSigmaBDD.restrict(stateBDD);*/
    //                    if((!tBDD.equals(safeStatesEnablingSigmaBDD) && !tBDD.isZero()) || bddHashCode == 0 )
    //                    if((!safeStatesEnablingSigmaBDD.and(stateBDD).isZero() && temp2.equals(stateBDD)) || bddHashCode == 0)
                        if(isSigmaBDD || bddHashCode == 0 || toOneStatesSigmaMap.get(aut.getName()).contains(map.get(element)))
                            output.add(element);
                    }
                }

            }
        }
        else if(fi == st)
        {
            for(Integer i:temp)
            {
                if(isSigmaBDD || bddHashCode == 0 || toOneStatesSigmaMap.get(aut.getName()).contains(map.get(i)))
                    output.add(i);
            }
        }
        else
            System.out.println("Exception in modifyStateIndices!!!!!");

        return output;
    }


    public int pow2(int p)
    {
        return (int)Math.pow(2,p);
    }

    public void MDD2DOT(MDD mdd, String path)
    {
        try
        {
            FileWriter fstream = new FileWriter(path);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("digraph G {");
            out.newLine();
            out.write("size = \"7.5,10\"");
            out.newLine();
            out.write("0 [shape=box, label=\"0\", style=filled, shape=box, height=0.3, width=0.3];");
            out.newLine();
            out.write("1 [shape=box, label=\"1\", style=filled, shape=box, height=0.3, width=0.3];");
            out.newLine();
            HashSet<Integer> addedNodes2Dot = new HashSet<Integer>();
            addedNodes2Dot.add(0);
            addedNodes2Dot.add(1);
            if(mdd.nodes.size() > 2)
            {
                generateDot(out, mdd, mdd.getRoot(), addedNodes2Dot);
            }
            out.write("}");
            out.close();
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }

    }

    public boolean generateDot(BufferedWriter out, MDD mdd, MDDNode node, HashSet<Integer> added)
    {
        try
        {
            if(!added.contains(node.getID()))
            {
                out.write(""+node.getID()+" [label=\""+node.getName()+"\"];");
                out.newLine();
                added.add(node.getID());

                if(node.getID() != 0 && node.getID() != 1)
                {
                    HashSet<MDDEdge> conEdges = mdd.fromNode2edges(node);
                    for(MDDEdge e: conEdges)
                    {
                        String temp = ""+e.getFromNode().getID()+" -> "+e.getToNode().getID()+" [label=\"";
                        boolean flag = false;
                        for(Integer index:e.getLabel())
                        {
                            flag = true;
                            temp += (automataBDD.getBDDAutomaton(e.getFromNode().getName()).myIndex2stateName.get(index) + " , ");
                        }
                        if(flag)
                            temp = temp.substring(0, temp.length()-3);
                        out.write(temp+"\"];");
                        out.newLine();
                        generateDot(out, mdd, e.getToNode(), added);
                    }
                }
                else
                {
                    return true;
                }
            }
            else
                return true;
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }

        return false;
    }

    public String generateExpr(MDD mdd)
    {
        independentStates = new HashSet<String>();

        HashSet<MDDEdge> conEdges = mdd.toNode2edges(mdd.getNode(1));
        String autExpr = "";

        for(MDDEdge e: conEdges)
        {
            String autName = e.getFromNode().getName();
            BDDAutomaton bddAut = automataBDD.getBDDAutomaton(autName);
            
            HashSet<Integer> stateSet = e.getLabel();

            HashSet<String> indpStates = new HashSet<String>();
            for(Integer i:stateSet)
            {
                String stateName = bddAut.myIndex2stateName.get(i);
                if(!toOneStatesMap.get(autName).contains(stateName))
                    indpStates.add(("Q_"+autName+(allowedForbidden?" = ":" != ")+stateName));
            }
            if(indpStates.size() == stateSet.size() && !e.getFromNode().isRoot())
            {
                independentStates.addAll(indpStates);
                continue;
            }
            else
            {
                autExpr += ((e.getFromNode().isRoot()?"":O_PAR));

                boolean compFlag = false;
                if(bddAut.getComplementIndices(e.getLabel()).size() < stateSet.size())
                {
                    stateSet = bddAut.getComplementIndices(e.getLabel());
                    compFlag = true;
                }
                if(compFlag)
                {
                    for(Integer i:stateSet)
                    {
                        autExpr += (("Q_"+autName+(allowedForbidden?" != ":" = ")+bddAut.myIndex2stateName.get(i))+ (allowedForbidden?AND:OR));
                    }
                }
                else
                {
                    for(Integer i:stateSet)
                    {
                        autExpr += (("Q_"+autName+(allowedForbidden?" = ":" != ")+bddAut.myIndex2stateName.get(i))+ (allowedForbidden?OR:AND));
                    }
                }
                autExpr = autExpr.substring(0, autExpr.length()-3);
                if(stateSet.size() > 1)
                    autExpr = ((e.getFromNode().isRoot()?"":O_PAR)) + autExpr + ((e.getFromNode().isRoot()?"":C_PAR));
            }

            String temp = MDD2Expr(mdd,e.getFromNode());
            if(!temp.equals(""))
            {
                autExpr +=  (allowedForbidden?AND:OR) + O_PAR + temp + C_PAR + ((e.getFromNode().isRoot()?"":C_PAR)) + (allowedForbidden?OR:AND);
            }
            else
            {
                autExpr += ((e.getFromNode().isRoot()?"":C_PAR)) + (allowedForbidden?OR:AND);
            }

        }

        for(String is:independentStates)
            autExpr += (is+(allowedForbidden?OR:AND));

        autExpr = autExpr.substring(0, (autExpr.length()-3));

        return autExpr;
    }

    public String MDD2Expr(MDD mdd, MDDNode node)
    {
        if(!node.isRoot())
        {
            HashSet<MDDEdge> conEdges = mdd.toNode2edges(node);
            String autExpr = "";

            for(MDDEdge e: conEdges)
            {
                String autName = e.getFromNode().getName();
                BDDAutomaton bddAut = automataBDD.getBDDAutomaton(autName);

                HashSet<Integer> stateSet = e.getLabel();
                HashSet<String> indpStates = new HashSet<String>();
                for(Integer i:stateSet)
                {
                    String stateName = bddAut.myIndex2stateName.get(i);
                    if(!toOneStatesMap.get(autName).contains(stateName))
                        indpStates.add(("Q_"+autName+(allowedForbidden?" = ":" != ")+stateName));
                }
                if(indpStates.size() == stateSet.size() && !e.getFromNode().isRoot())
                {
                    independentStates.addAll(indpStates);
                    continue;                    
                }
                else
                {
                    autExpr += ((e.getFromNode().isRoot()?"":O_PAR));
                    boolean compFlag = false;
                    if(bddAut.getComplementIndices(e.getLabel()).size() < stateSet.size())
                    {
                        stateSet = bddAut.getComplementIndices(e.getLabel());
                        compFlag = true;
                    }
                    if(compFlag)
                    {
                        for(Integer i:stateSet)
                        {
                            autExpr += (("Q_"+autName+(allowedForbidden?" != ":" = ")+bddAut.myIndex2stateName.get(i))+ (allowedForbidden?AND:OR));
                        }
                    }
                    else
                    {
                        for(Integer i:stateSet)
                        {
                            autExpr += (("Q_"+autName+(allowedForbidden?" = ":" != ")+bddAut.myIndex2stateName.get(i))+ (allowedForbidden?OR:AND));
                        }
                    }
                    autExpr = autExpr.substring(0, autExpr.length()-3);
                    if(stateSet.size() > 1)
                        autExpr = ((e.getFromNode().isRoot()?"":O_PAR)) + autExpr + ((e.getFromNode().isRoot()?"":C_PAR));
                }

                String temp = MDD2Expr(mdd,e.getFromNode());
                if(!temp.equals(""))
                {
                    autExpr += (allowedForbidden?AND:OR) + O_PAR + temp + C_PAR + ((e.getFromNode().isRoot()?"":C_PAR)) + (allowedForbidden?OR:AND);
                }
                else
                {
                    autExpr += ((e.getFromNode().isRoot()?"":C_PAR)) + (allowedForbidden?OR:AND);
                }
            }
            autExpr = autExpr.substring(0, (autExpr.length()-3));
            return autExpr;
        }
        else
            return "";
    }

    public HashMap<String, HashSet<String>> statesInWayToNode(MDD mdd, MDDNode node, HashMap<String, HashSet<String>> map)
    {
        if(!node.isRoot())
        {
            HashSet<MDDEdge> conEdges = mdd.toNode2edges(node);
            for(MDDEdge e: conEdges)
            {
               String fNodeName = e.getFromNode().getName();

               NEWskippedAutomata(automaton2MBDDLevel.get(node.getName()),automaton2MBDDLevel.get(fNodeName), map);

               BDDAutomaton bddAut = automataBDD.getBDDAutomaton(e.getFromNode().getName());
               if(!map.containsKey(fNodeName))
               {
                   HashSet<String> s = new HashSet<String>();
                   for(Integer i:e.getLabel())
                       s.add(bddAut.myIndex2stateName.get(i));
                   map.put(fNodeName, s);
               }
               else
               {
                   for(Integer i:e.getLabel())
                       map.get(fNodeName).add(bddAut.myIndex2stateName.get(i));
               }

               statesInWayToNode(mdd, e.getFromNode(),map);
            }

        }
        for(Automaton a:automataBDD.getAutomata())
        {
            if(!map.keySet().contains(a.getName()))
            {
               HashSet<String> s = new HashSet<String>();
               for(State state:a.getStateSet())
                   s.add(state.getName());
               map.put(a.getName(), s);
            }
        }

        return map;

    }

    public void NEWskippedAutomata(int fromAutomatonLevel,int toAutomatonLevel, HashMap<String, HashSet<String>> map)
    {
        for(Integer level : MBDDLevel2automaton.keySet())
        {
            if((level < fromAutomatonLevel) && (level > toAutomatonLevel))
            {
               String autName = MBDDLevel2automaton.get(level);

               Automaton aut = automataBDD.getAutomata().getAutomaton(autName);

               if(!map.containsKey(autName))
               {
                   HashSet<String> s = new HashSet<String>();
                   for(State st:aut.getStateSet())
                       s.add(st.getName());
                   map.put(autName, s);
               }
               else
               {
                   for(State st:aut.getStateSet())
                       map.get(autName).add(st.getName());
               }

            }
        }
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
            String currStatName = "";
            int automatonIndex = -1;
            for (int i = 0; i < sourceStateDomainIndicies.length; i++)
            {
                stateArray[i] = currSat[sourceStateDomainIndicies[i]].intValue();
                automatonIndex = automaton2indexMap.get(sourceStateDomains[i].getName());

                currStatName += bddIndex2SourceStateName[automatonIndex].get(stateArray[i]);
                if(i != (sourceStateDomainIndicies.length-1))
                    currStatName += ".";
            }

            System.out.println(currStatName);
        }
    }

    public String BDD2Expr(BDD bdd)
    {
//        System.out.println(bdd.var()+": "+bdd.hashCode());
        if(bdd.isOne() || bdd.isZero())
            return "no expression";
        if(bdd.low().isOne())
        {
            if(!bdd.high().isZero())
                return "!"+bdd.var() + OR + "("+BDD2Expr(bdd.high())+")";
            else
                return "!"+bdd.var();
        }
        else if(bdd.high().isOne())
        {
            if(!bdd.low().isZero())
                return ""+bdd.var() + OR + "("+BDD2Expr(bdd.low())+")";
            else
                return ""+bdd.var();
        }
        else if(bdd.low().isZero())
        {
            if(!bdd.high().isOne())
                return ""+bdd.var() + AND + BDD2Expr(bdd.high());
            else
                return ""+bdd.var();
        }
        else if(bdd.high().isZero())
        {
            if(!bdd.low().isOne())
                return "!"+bdd.var() + AND + BDD2Expr(bdd.low());
            else
                return "!"+bdd.var();
        }
        else
        {
            return "(("+ ""+bdd.var() + AND + BDD2Expr(bdd.high())+")" + OR + "("+ "!"+bdd.var() + AND + BDD2Expr(bdd.low())+"))";
        }
    }

}
