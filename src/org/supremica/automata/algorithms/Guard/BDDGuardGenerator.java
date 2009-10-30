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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDVarSet;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.automata.BDD.BDDAutomata;
import org.supremica.automata.BDD.BDDAutomaton;
import org.supremica.automata.BDD.BDDManager;
import org.supremica.automata.BDD.BDDMonolithicTransitions;
import org.supremica.automata.BDD.BDDTransitions;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


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

    boolean allowedForbidden = false;
    boolean optimalMode = false;
    int mode;
    
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
       
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(BDDAutomata.class);
//    String pathRoot = "C:/Users/sajed/Desktop/";
    String pathRoot = "G:/Sajed/";

    //declarations for new implentation
    HashMap<Integer, HashSet<Integer>> nodeHash2stateIndices;
    HashMap<String, HashSet<String>> toOneStatesMap;
    HashMap<String, HashSet<String>> toOneStatesSigmaMap;
    HashMap<String, Integer> automaton2MBDDLevel;
    HashMap<Integer, String> MBDDLevel2automaton;
    HashSet<String> independentStates;
    boolean generalBDD = false;

    /** Creates a new instance of BDDGuardGenerator */
    public BDDGuardGenerator(BDDAutomata bddAutomata, String eventLabel, BDD safeStates, int mode) {
        theAutomata = bddAutomata.getAutomata();
        automataBDD = bddAutomata;
        manager = automataBDD.getBDDManager();
        this.mode = mode;
        switch(mode)
        {
            case 0:
                allowedForbidden = false;
                break;
            case 1:
                allowedForbidden = true;
                break;
            case 2:
                optimalMode = true;
                break;
        }

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

        safeStatesBDD = safeStates;

        if(logEnabled)
            System.out.println("number of safe states: "+safeStatesBDD.satCount(automataBDD.getSourceStateVariables()));
        
        computeStatesEnablingSigma();

        computeSafeStatesEnablingSigma();

//        generateStates(safeStatesEnablingSigmaBDD);

//        System.out.println("states enabling sigma:");


/*        System.out.println("reachable states:");
        printStates(automataBDD.getReachableStates());
        automataBDD.getReachableStates().printDot();
        System.out.println("reachable guard: "+generateReachGuard(automataBDD.getReachableStates()));
*/

        computeStatesLeading2ForbiddenStates();
/*        System.out.println("states leading to forbidden states:");
        generateStates(statesLeading2ForbiddenBDD);*/

        computeMustAllowedSates();
//        System.out.println("States where "+eventLabel+" must be enabled:");
//        mustAllowedStatesBDD.printDot();
//        generateStates(mustAllowedStatesBDD);

        computeMustForbiddenSates();
//        System.out.println("States where "+eventLabel+" must be disabled:");
//        mustForbiddenStatesBDD.printDot();
//        generateStates(mustForbiddenStatesBDD);

        computeCareStates();
//        System.out.println("care states:");
//        generateStates(careStatesBDD);

//        computeDontCareStates();
//        mustForbiddenStatesBDD.simplify(careStatesBDD.toVarSet()).printDot();
//        mustAllowedStatesBDD.printDot();

/*        if(mustAllowedStatesBDD.nodeCount()>0)
        {
            MDD mdd = generateMDD(mustAllowedStatesBDD);
            MDD2DOT(mdd, pathRoot+"mdd_"+eventLabel+".dot");
        }
*/

//        generateStates(manager.safeStateSynthesis(automataBDD, prelUnconStates.or(automataBDD.getForbiddenStates())).and(automataBDD.getReachableStates()));

        if(optimalMode)
        {
            allowedForbidden = true;
            String allowedGuard = generateGuard(mustAllowedStatesBDD);
            int minNbrOfTerms = nbrOfTerms;

            allowedForbidden = false;
            String forbiddenGuard = generateGuard(mustForbiddenStatesBDD);
            if(nbrOfTerms <= minNbrOfTerms)
            {
                guard = forbiddenGuard;
            }
            else
            {
                guard = allowedGuard;
                nbrOfTerms = minNbrOfTerms;
            }

            System.out.println("Optimal guard for event "+event.getName()+": "+guard);
        }
        else
        {
            if(allowedForbidden)
            {
                System.out.println("Allowed guard for event "+event.getName()+": "+generateGuard(mustAllowedStatesBDD));
            }
            else
            {
                System.out.println("Forbidden guard for event "+event.getName()+": "+generateGuard(mustForbiddenStatesBDD));
            }
        }

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
    
    //Q^sigma & C(Q^sigma_a) & Q_sup
    public void computeMustForbiddenSates()
    {
        mustForbiddenStatesBDD = safeStatesEnablingSigmaBDD.and(mustAllowedStatesBDD.not());
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

    public void computeSafeStatesEnablingSigma()
    {
        safeStatesEnablingSigmaBDD = safeStatesBDD.and(automataBDD.getReachableStates());
        safeStatesEnablingSigmaBDD = safeStatesEnablingSigmaBDD.and(statesEnablingSigmaBDD);
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

    public String generateGuard(BDD states)
    {
        nbrOfTerms = 0;
        String guard = "";
        BDDVarSet careVarSet = careStatesBDD.toVarSet();

        nodeHash2stateIndices = new HashMap<Integer, HashSet<Integer>>();
        toOneStatesMap = new HashMap<String, HashSet<String>>();
        toOneStatesSigmaMap = new HashMap<String, HashSet<String>>();

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
 //               generalBDD = true;
 //               MDD safeStatesEnablingSigmaMDD = generateMDD(safeStatesEnablingSigmaBDD);
 //               MDD2DOT(safeStatesEnablingSigmaMDD, pathRoot+"MDD_files/mddSigma_"+event.getName()+".dot");
 //               generalBDD = false;
                MDD goodMDD = generateMDD(goodBDD);
 //               MDD2DOT(goodMDD, pathRoot+"MDD_files/mdd_"+event.getName()+".dot");

                guard = generateExpr(goodMDD);
            }
        }

        return guard;
    }

    public MDD generateMDD(BDD bdd)
    {
        MDD mdd = new MDD();
//        mdd.addNode(new MDDNode(0,"0"));
        mdd.addNode(new MDDNode(1,"1"));


        automaton2MBDDLevel = new HashMap<String, Integer>();
        MBDDLevel2automaton = new HashMap<Integer, String>();

        if(bdd.nodeCount() > 0)
        {
            MDDNode root = new MDDNode(bdd.hashCode(),automataBDD.bddVar2AutName.get(bdd.var()));
            mdd.addNode(root);
            mdd.setRoot(root);

            automaton2MBDDLevel.put(root.getName(), bdd.level());
            MBDDLevel2automaton.put(bdd.var(), root.getName());
            LinkedList<String> ll0 = new LinkedList<String>();
            ll0.add(bdd.var()+":"+0);
            traverseBDD(bdd, bdd.low(), ll0, mdd, root);

            LinkedList<String> ll1 = new LinkedList<String>();
            ll1.add(bdd.var()+":"+1);
            traverseBDD(bdd, bdd.high(), ll1, mdd, root);
        }

        return mdd;
    }

    @SuppressWarnings("unchecked")
	public boolean traverseBDD(BDD lastBDDAut, BDD bdd, LinkedList<String> assignment, MDD mdd, MDDNode root)
    {
        int lastVar = Integer.parseInt(assignment.getLast().substring(0, assignment.getLast().length()-2));

        if(bdd.isZero())
            return true;
        else if(bdd.isOne())
        {
            HashSet<String> states;
            states = assignment2states(lastBDDAut, assignment, automataBDD.bddVar2AutName.get(lastVar));

            MDDNode node = mdd.getNode(bdd.hashCode());

            automaton2MBDDLevel.put(node.getName(), bdd.level());
            MBDDLevel2automaton.put(bdd.level(), node.getName());

            mdd.addEdge(new MDDEdge(root, node, null,states));

            return true;
        }
        else if(!automataBDD.bddVar2AutName.get(bdd.var()).equals(automataBDD.bddVar2AutName.get(lastVar)))
        {
            HashSet<String> states = assignment2states(lastBDDAut, assignment, automataBDD.bddVar2AutName.get(lastVar));
            MDDNode node = mdd.getNode(bdd.hashCode());
            lastBDDAut = bdd;

            if(node == null)
            {
                node = new MDDNode(bdd.hashCode(),automataBDD.bddVar2AutName.get(bdd.var()));

                mdd.addNode(node);

                automaton2MBDDLevel.put(node.getName(), bdd.level());
                MBDDLevel2automaton.put(bdd.level(), node.getName());

                mdd.addEdge(new MDDEdge(root, node, null,states));

                LinkedList<String> ll0 = new LinkedList<String>();
                ll0.add(bdd.var()+":"+0);
                traverseBDD(lastBDDAut, bdd.low(), ll0, mdd, node);

                LinkedList<String> ll1 = new LinkedList<String>();
                ll1.add(bdd.var()+":"+1);
                traverseBDD(lastBDDAut, bdd.high(), ll1, mdd, node);
                
            }
            else
            {
                mdd.addEdge(new MDDEdge(root, node, null,states));
            }
        }
        else
        {
            LinkedList<String> ll0 = (LinkedList<String>) assignment.clone();
            LinkedList<String> ll1 = (LinkedList<String>) assignment.clone();
            ll0.add(bdd.var()+":"+0);
            ll1.add(bdd.var()+":"+1);

            traverseBDD(lastBDDAut, bdd.low(),  ll0, mdd, root);
            traverseBDD(lastBDDAut, bdd.high(), ll1, mdd, root);
        }

        return false;

    }

    HashSet<String> assignment2states(BDD bdd, LinkedList<String> assignment, String autName)
    {
        HashSet<String> output = new HashSet<String>();

        BDD.BDDIterator satIt = bdd.iterator(automataBDD.getSourceStateVars(automataBDD.getAutomata().getAutomaton(autName)));
        HashSet<BDD> truePaths = new HashSet<BDD>();
        while(satIt.hasNext())
        {
            BDD truePathBDD = satIt.nextBDD();
            if(!truePaths.contains(truePathBDD))
            {
                truePaths.add(truePathBDD);

            }
        }
        for(BDD b:truePaths)
        {
            String bddString = b.toString();

            boolean match = true;
            Iterator<String> it = assignment.descendingIterator();
            while(it.hasNext())
            {
                String nextIt = it.next();
                if(!bddString.contains(nextIt))
                {
                    match = false;
                    break;
                }
            }

            if(match)
            {
                BDDAutomaton bddAut = automataBDD.getBDDAutomaton(autName);
                for(State state:bddAut.getAutomaton().getStateSet())
                {
                    int stateIndex = automataBDD.getStateIndex(bddAut.getAutomaton(), state);
                    BDD stateBDD = manager.getFactory().buildCube(stateIndex, bddAut.getSourceStateDomain().vars());
                    if(b.and(stateBDD).nodeCount() > 0 && (stateBDD.and(safeStatesEnablingSigmaBDD).nodeCount() > 0))
                    {
                        output.add(state.getName());
                    }
                }
            }
        }

        return output;
    }

    public String generateExpr(MDD mdd)
    {
        independentStates = new HashSet<String>();

        HashSet<MDDEdge> conEdges = mdd.node2InEdges(mdd.getNode(1));
        String autExpr = "";

        for(MDDEdge e: conEdges)
        {
            String autName = e.getFromNode().getName();
            BDDAutomaton bddAut = automataBDD.getBDDAutomaton(autName);

            HashSet<String> stateSet = e.getLabelString();
            HashSet<String> indpStates = new HashSet<String>();
            for(String stateName:stateSet)
            {
                int stateIndex = automataBDD.getStateIndex(bddAut.getAutomaton(), bddAut.getAutomaton().getStateWithName(stateName));
                BDD stateBDD = manager.getFactory().buildCube(stateIndex, bddAut.getSourceStateDomain().vars());

                if((allowedForbidden?mustForbiddenStatesBDD:mustAllowedStatesBDD).and(stateBDD).nodeCount() == 0)
                {
                    indpStates.add(("Q_"+autName+(allowedForbidden?" = ":" != ")+stateName));
                }
            }

            if(indpStates.size() == stateSet.size() && !e.getFromNode().isRoot())
            {
//                System.out.println("generateExpr: Independent state found at automaton "+autName+"!!!");
                independentStates.addAll(indpStates);
                continue;
            }
            else
            {
                autExpr += ((e.getFromNode().isRoot()?"":O_PAR));

                boolean compFlag = false;
                if(bddAut.getComplementStateNames(stateSet).size() < stateSet.size())
                {
                    stateSet = bddAut.getComplementStateNames(e.getLabelString());
                    compFlag = true;
                }
                if(compFlag)
                {
                    for(String stateName:stateSet)
                    {
                        autExpr += (("Q_"+autName+(allowedForbidden?" != ":" = ")+stateName)+ (allowedForbidden?AND:OR));
                        nbrOfTerms++;
                    }
                }
                else
                {
                    for(String stateName:stateSet)
                    {
                        autExpr += (("Q_"+autName+(allowedForbidden?" = ":" != ")+stateName)+ (allowedForbidden?OR:AND));
                        nbrOfTerms++;
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
        {
            autExpr += (is+(allowedForbidden?OR:AND));
            nbrOfTerms++;
        }

        if(autExpr.length() >= 3)
            autExpr = autExpr.substring(0, (autExpr.length()-3));

        return autExpr;
    }

    public String MDD2Expr(MDD mdd, MDDNode node)
    {
        if(!node.isRoot())
        {
            HashSet<MDDEdge> conEdges = mdd.node2InEdges(node);
            String autExpr = "";
            for(MDDEdge e: conEdges)
            {
                String autName = e.getFromNode().getName();
                BDDAutomaton bddAut = automataBDD.getBDDAutomaton(autName);

                HashSet<String> stateSet = e.getLabelString();
                HashSet<String> indpStates = new HashSet<String>();
                for(String stateName:stateSet)
                {
                    int stateIndex = automataBDD.getStateIndex(bddAut.getAutomaton(), bddAut.getAutomaton().getStateWithName(stateName));
                    BDD stateBDD = manager.getFactory().buildCube(stateIndex, bddAut.getSourceStateDomain().vars());

                    if((allowedForbidden?mustForbiddenStatesBDD:mustAllowedStatesBDD).and(stateBDD).nodeCount() == 0)
                    {
                        indpStates.add(("Q_"+autName+(allowedForbidden?" = ":" != ")+stateName));
                    }
                }

                if(indpStates.size() == stateSet.size() && !e.getFromNode().isRoot())
                {
//                    System.out.println("MDD2Expr: Independent state found at automaton "+autName+"!!!");
                    independentStates.addAll(indpStates);
                    continue;
                }
                else
                {
                    autExpr += ((e.getFromNode().isRoot()?"":O_PAR));
                    boolean compFlag = false;
                    if(bddAut.getComplementStateNames(e.getLabelString()).size() < stateSet.size())
                    {
                        stateSet = bddAut.getComplementStateNames(e.getLabelString());
                        compFlag = true;
                    }
                    if(compFlag)
                    {
                        for(String stateName:stateSet)
                        {
                            autExpr += (("Q_"+autName+(allowedForbidden?" != ":" = ")+stateName)+ (allowedForbidden?AND:OR));
                            nbrOfTerms++;
                        }
                    }
                    else
                    {
                        for(String stateName:stateSet)
                        {
                            autExpr += (("Q_"+autName+(allowedForbidden?" = ":" != ")+stateName)+ (allowedForbidden?OR:AND));
                            nbrOfTerms++;
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

            if(autExpr.length() >= 3)
                autExpr = autExpr.substring(0, (autExpr.length()-3));

            return autExpr;
        }
        else
            return "";
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
//            out.write("0 [shape=box, label=\"0\", style=filled, shape=box, height=0.3, width=0.3];");
//            out.newLine();
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
                    HashSet<MDDEdge> conEdges = mdd.node2OutEdges(node);
                    for(MDDEdge e: conEdges)
                    {
                        String temp = ""+e.getFromNode().getID()+" -> "+e.getToNode().getID()+" [label=\"";
                        boolean flag = false;
                        for(String stateName :e.getLabelString())
                        {
                            flag = true;
                            temp += (stateName + " , ");
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

    public HashSet<String> generateStates(BDD states)
    {
        BDD.BDDIterator satIt = states.iterator(automataBDD.getSourceStateVariables());

        HashSet<String> output = new HashSet<String>();
        while(satIt.hasNext())
        {
            BDD truePathBDD = satIt.nextBDD();
            HashSet<String> subOutput = new HashSet<String>();
            boolean firstTime = true;
            for(Automaton aut:theAutomata)
            {
                HashSet<String> tempOutput = new HashSet<String>();
                for(State state:aut.getStateSet())
                {
                    int stateIndex = automataBDD.getStateIndex(aut, state);
                    BDD stateBDD = manager.getFactory().buildCube(stateIndex, automataBDD.getBDDAutomaton(aut).getSourceStateDomain().vars());
                    if(truePathBDD.and(stateBDD).nodeCount() > 0)
                    {
                        if(!firstTime)
                        {
                            for(String element:subOutput)
                            {
                                String temp = element + "." + state.getName();
                                tempOutput.add(temp);
                            }
                        }
                        else
                        {
                            subOutput.add(state.getName());
                        }
                    }
                }
                if(!firstTime)
                    subOutput = tempOutput;

                firstTime = false;
            }
            output.addAll(subOutput);
        }

        for(String element:output)
        {
            System.out.println(element);
        }

        return output;
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
