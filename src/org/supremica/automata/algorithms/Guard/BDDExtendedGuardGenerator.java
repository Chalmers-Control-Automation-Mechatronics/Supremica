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
import java.util.ArrayList;
import org.supremica.automata.*;
import org.supremica.automata.BDD.*;
import org.supremica.automata.BDD.EFA.*;
import net.sf.javabdd.*;
import org.supremica.log.*;
import net.sourceforge.waters.model.module.NodeProxy;


/**
 *
 * @author Sajed
 */
public class BDDExtendedGuardGenerator {

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

    BDDExtendedAutomata automataBDD;
    BDDExtendedManager manager;

    ExtendedAutomata theAutomata;

    String OR = " | ";
    String AND = " & ";
    String O_PAR = "(";
    String C_PAR = ")";
    String guard = "";
    String TRUE = "1";
    String FALSE = "0";
    String EQUAL = " == ";
    String NEQUAL = " != ";
    String eventName;

    int bddSize;
    int nbrOfTerms;

    boolean allowedForbidden = false;
    boolean optimalMode = false;

    private static Logger logger = LoggerFactory.createLogger(BDDAutomata.class);
    String pathRoot = "C:/Users/sajed/Desktop/MDD_files/";
//    String pathRoot = "G:/Sajed/";

//    HashSet<String> independentStates;

    HashSet<String> test;
    int testIndex = 0;

    /** Creates a new instance of BDDExtendedGuardGenerator */
    public BDDExtendedGuardGenerator(BDDExtendedAutomata bddAutomata, String eventLabel, BDD states, int mode) {
        test = new HashSet<String>();
        theAutomata = bddAutomata.getExtendedAutomata();
        automataBDD = bddAutomata;
        manager = automataBDD.getBDDManager();
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

        eventName = eventLabel;
        int currEventIndex = automataBDD.getEventIndex(eventName);
        sigmaBDD = manager.createBDD(currEventIndex, automataBDD.getEventDomain());

        BDDEdges bddTransitions = automataBDD.getBDDEdges();

        forwardMonolithicTransitionsBDD = ((BDDMonolithicEdges)bddTransitions).getMonolithicEdgesForwardWithEventsBDD();
        backwardMonolithicTransitionsBDD = ((BDDMonolithicEdges)bddTransitions).getMonolithicEdgesBackwardWithEventsBDD();

        safeStatesBDD = states;

        computeStatesEnablingSigma();

        computeSafeStatesEnablingSigma();
//        System.err.println("safe states enabling "+eventName+": "+bddAutomata.nbrOfStatesBDD(safeStatesEnablingSigmaBDD));

        computeStatesLeading2ForbiddenStates();

//        System.err.println("states leading to forbidden states: "+bddAutomata.nbrOfStatesBDD(statesLeading2ForbiddenBDD));

        computeMustAllowedSates();

        computeMustForbiddenSates();

        computeCareStates();

//        BDD2MDD2PS(mustAllowedStatesBDD, eventName);

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
        }
        else
        {
            if(allowedForbidden)
            {
                guard = generateGuard(mustAllowedStatesBDD);
            }
            else
            {
                guard = generateGuard(mustForbiddenStatesBDD);
            }
        }

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

    //Q^sigma
    public void computeStatesEnablingSigma()
    {
        statesEnablingSigmaBDD = forwardMonolithicTransitionsBDD.relprod(sigmaBDD, automataBDD.getDestStatesVarSet());
        statesEnablingSigmaBDD = statesEnablingSigmaBDD.exist(automataBDD.getEventVarSet());
//        return statesEnablingSigmaBDD;
    }

    public void computeStatesLeading2ForbiddenStates()
    {
        BDD  forbiddenAndReachableStatesBDD = automataBDD.getReachableStates().and(safeStatesBDD.not());

        forbiddenAndReachableStatesBDD = forbiddenAndReachableStatesBDD.replace(automataBDD.getSource2DestLocationPairing());
        forbiddenAndReachableStatesBDD = forbiddenAndReachableStatesBDD.replace(automataBDD.getSource2DestVariablePairing());

        BDD transitionsWithSigma = forwardMonolithicTransitionsBDD.relprod(sigmaBDD, automataBDD.getEventVarSet());
        statesLeading2ForbiddenBDD = (transitionsWithSigma.and(forbiddenAndReachableStatesBDD)).exist(automataBDD.getDestStatesVarSet());

//        return statesLeading2ForbiddenBDD;
    }   

    //Q^sigma_sup
    public void computeMustAllowedSates()
    {
        mustAllowedStatesBDD = safeStatesEnablingSigmaBDD.and(statesLeading2ForbiddenBDD.not());
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
          dontCareStatesBDD = careStatesBDD.not();
//        return dontCareStatesBDD;
    }

    public void computeSafeStatesEnablingSigma()
    {
        safeStatesEnablingSigmaBDD = safeStatesBDD.and(statesEnablingSigmaBDD);
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

    public boolean guardIsTrue()
    {
        if(guard.equals(TRUE))
            return true;
        return false;
    }

    public void BDD2MDD2PS(BDD bdd, String fileName)
    {
        MDD2DOT(generateMDD(bdd), pathRoot+fileName+".dot");

        Runtime rt = Runtime.getRuntime();
        try{
            Process proc = rt.exec("cmd /C dot -Tps "+pathRoot+fileName+".dot"+" -o "+pathRoot+fileName+".ps");
            proc.waitFor();
            proc.exitValue();
        }catch(Exception e) {System.out.println(e);}
    }

    public String generateGuard(BDD states)
    {
        nbrOfTerms = 0;
        String guard = "";

        if(states.equals(careStatesBDD))
        {
            guard = allowedForbidden ? TRUE : FALSE;
            nbrOfTerms = 0;
        }
        else if(states.satCount(automataBDD.getSourceStatesVarSet()) == 0)
        {
            guard = allowedForbidden ? FALSE : TRUE;
            nbrOfTerms = 0;
        }
        else
        {
            BDD goodBDD = states.simplify(careStatesBDD.toVarSet());

            if(states.nodeCount() <= goodBDD.nodeCount())
                goodBDD = states;

            if(goodBDD.nodeCount()>0)
            {
                MDD goodMDD = generateMDD(goodBDD);

//                System.out.println(eventName+"   "+(allowedForbidden?"allowed":"forbidden")+"        "+states.nodeCount()+"          "+goodBDD.nodeCount()+"     "+goodMDD.nodeCount());
//                System.out.println(eventName);
//                goodBDD.printDot();

                String fileName = "mdd_"+eventName;
                if(allowedForbidden)
                    fileName += "_allowed";
                else
                    fileName += "_forbidden";

//                BDD2MDD2PS(goodBDD, fileName);

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

        if(bdd.nodeCount() > 0)
        {
            MDDNode root = new MDDNode(bdd.hashCode(),automataBDD.getAutVarName(bdd.var()));
            mdd.addNode(root);
            mdd.setRoot(root);

            BDD varBDD = automataBDD.getBDDforSourceBDDVar(bdd.var());

            traverseBDD(bdd.low(), varBDD.not(), mdd, root);
            traverseBDD(bdd.high(), varBDD, mdd, root);
        }

        return mdd;
    }


    public void traverseBDD(BDD bdd, BDD autStatesBDD, MDD mdd, MDDNode root)
    {       
        if(!bdd.isZero())
        {
            if(bdd.isOne() || !automataBDD.getAutVarName(bdd.var()).equals(root.getName()))
            {
                ArrayList<String> states = bdd2automatonStates(autStatesBDD);

                MDDNode node = mdd.getNode(bdd.hashCode());

                //if node has not been visited
                if(node == null)
                {                    
                    node = new MDDNode(bdd.hashCode(),automataBDD.getAutVarName(bdd.var()));
                    mdd.addNode(node);

                    String edgeID = root.getID()+"*"+node.getID();         
                    if(!states.isEmpty())
                        mdd.addEdge(new MDDEdge(root, node, states, edgeID));

                    BDD varBDD = automataBDD.getBDDforSourceBDDVar(bdd.var());
                    traverseBDD(bdd.low(), varBDD.not(), mdd, node);
                    traverseBDD( bdd.high(), varBDD, mdd, node);

                }
                else
                {
                    if(!states.isEmpty())
                    {
                        String edgeID = root.getID()+"*"+node.getID();
                        MDDEdge edge = mdd.getEdgeWithID(edgeID);
                        if(edge != null)                        
                            edge.getLabelString().addAll(states);                        
                        else
                            mdd.addEdge(new MDDEdge(root, node, states,edgeID));
                    }
                }
            }
            else
            {
                BDD varBDD = automataBDD.getBDDforSourceBDDVar(bdd.var());

                traverseBDD(bdd.low(),  autStatesBDD.and(varBDD.not()), mdd, root);
                traverseBDD(bdd.high(), autStatesBDD.and(varBDD), mdd, root);
            }
        }
    }

    ArrayList<String> bdd2automatonStates(BDD autStatesBDD)
    {
        ArrayList<String> output = new ArrayList<String>();
        int var = autStatesBDD.var();

        if(automataBDD.isSourceLocationVar(var))
        {
            ExtendedAutomaton exAut = automataBDD.getBDDExAutomaton(automataBDD.getAutVarName(var)).getExAutomaton();
            for(NodeProxy location:exAut.getNodes())
            {
                int locationIndex = automataBDD.getLocationIndex(exAut, location);
                BDD locationBDD = manager.getFactory().buildCube(locationIndex, automataBDD.getSourceLocationDomain(exAut.getName()).vars());
                if(!autStatesBDD.and(locationBDD).isZero() && !locationBDD.and(safeStatesEnablingSigmaBDD).isZero())
                {
                    output.add(location.getName());
                }
            }
        }
        else
        {
            int maxValue = automataBDD.getExtendedAutomata().getMaxValueofVar(automataBDD.getAutVarName(var));
            int minValue = automataBDD.getExtendedAutomata().getMinValueofVar(automataBDD.getAutVarName(var));
            for(int i=minValue;i<=maxValue;i++)
            {
                BDD valueBDD = automataBDD.getConstantBDD(automataBDD.getAutVarName(var), i);
                if(!autStatesBDD.and(valueBDD).isZero() && !valueBDD.and(safeStatesEnablingSigmaBDD).isZero())
                {
                    output.add(""+automataBDD.getIndexMap().getValOfIndex(i));
                }
            }

        }

        return output;
    }

    public String generateExpr(MDD mdd)
    {
        ArrayList<String> independentStates = new ArrayList<String>();

        String autExpr = MDD2Expr(mdd, mdd.getNode(1),independentStates);

        if(autExpr != "")
            autExpr += (allowedForbidden?OR:AND);
        for(String is:independentStates)
        {
            autExpr += (is+(allowedForbidden?OR:AND));
            if(is.contains("&"))
                nbrOfTerms+= 2;
            else
                nbrOfTerms++;
        }

        if(autExpr.length() >= 3)
            autExpr = autExpr.substring(0, (autExpr.length()-3));

        return autExpr;
    }

    public String MDD2Expr(MDD mdd, MDDNode node,ArrayList<String> independentStates)
    {
        if(!node.isRoot())
        {
            ArrayList<MDDEdge> conEdges = mdd.node2InEdges(node);
            String autExpr = "";

            for(MDDEdge e: conEdges)
            {
                String autVarName = e.getFromNode().getName();
                BDDExtendedAutomaton bddAut = automataBDD.getBDDExAutomaton(autVarName);
                boolean isAutomaton = (bddAut != null)?true:false;
                ArrayList<String> stateSet = e.getLabelString();
                ArrayList<String> indpStates = new ArrayList<String>();
                String symbol = automataBDD.getLocVarSuffix();

                for(String stateName:stateSet)
                {
                    BDD stateBDD = null;
                    if(isAutomaton)
                    {
                        ExtendedAutomaton exAut = bddAut.getExAutomaton();
//                        System.err.println("---------   "+stateName);
//                        System.err.println(exAut.getName());

                        int stateIndex = automataBDD.getLocationIndex(exAut, exAut.getLocationWithName(stateName));
                        stateBDD = manager.getFactory().buildCube(stateIndex, automataBDD.getSourceLocationDomain(exAut.getName()).vars());
                    }
                    else
                    {
                        stateBDD = automataBDD.getConstantBDD(autVarName, Integer.parseInt(stateName));
                    }

                    if((allowedForbidden?mustForbiddenStatesBDD:mustAllowedStatesBDD).and(stateBDD).nodeCount() == 0)
                    {
                        indpStates.add(stateName);
    //                    indpStates.add((symbol+autVarName.replaceAll(" ", "")+(allowedForbidden?EQUAL:NEQUAL)+stateName.replaceAll(" ", "")));
                    }
                }

                if(indpStates.equals(stateSet) && !e.getFromNode().isRoot())
                {

                    ArrayList<String> complementStates = new ArrayList<String>();
                    complementStates = isAutomaton?bddAut.getComplementLocationNames(indpStates):automataBDD.getComplementValues(autVarName, indpStates);

                    ArrayList<String> stateSetTemp = (ArrayList<String>)indpStates.clone();
                    ArrayList<String> compStatesTemp = (ArrayList<String>)complementStates.clone();

                    ArrayList<String> incrementalSeq1 = new ArrayList<String>();
                    ArrayList<String> incrementalSeq2 = new ArrayList<String>();
                    String inEq1="";
                    String inEq2="";
                    int inEq1Len = Integer.MAX_VALUE;
                    int inEq2Len = Integer.MAX_VALUE;
                    boolean isComp = complementStates.size() < indpStates.size();
                    boolean flag = allowedForbidden ^ isComp;
                    if(!isAutomaton)
                    {
                        incrementalSeq1 = isIncrementalSeq(stateSet);
                        stateSetTemp.removeAll(incrementalSeq1);
                        incrementalSeq2 = isIncrementalSeq(complementStates);
                        compStatesTemp.removeAll(incrementalSeq2);
                        inEq1 = seq2inEqual(incrementalSeq1, autVarName.replaceAll(" ", ""), theAutomata.getMinValueofVar(autVarName),
                            theAutomata.getMaxValueofVar(autVarName), flag);
                        if(!inEq1.isEmpty())
                        {
                            if(!inEq1.contains("&"))
                                inEq1Len = 1+stateSetTemp.size();
                            else
                                inEq1Len = 2+stateSetTemp.size();
                        }
                        inEq2 = seq2inEqual(incrementalSeq2, autVarName.replaceAll(" ", ""), theAutomata.getMinValueofVar(autVarName),
                            theAutomata.getMaxValueofVar(autVarName), flag);
                        if(!inEq2.isEmpty())
                        {
                            if(!inEq2.contains("&"))
                                inEq2Len = 1+compStatesTemp.size();
                            else
                                inEq2Len = 2+compStatesTemp.size();
                        }
                    }

                    ArrayList<Integer> alternatives = new ArrayList<Integer>();
                    alternatives.add(complementStates.size());
                    alternatives.add(stateSet.size());
                    alternatives.add(inEq1Len);
                    alternatives.add(inEq2Len);
                    int index = smallestInEq(alternatives);

                    switch(index)
                    {
                        case 0:
                            for(String stateName:complementStates)
                            {
                                independentStates.add((autVarName.replaceAll(" ", "")+(isAutomaton?symbol:"")+
                                        (allowedForbidden?NEQUAL:EQUAL)+stateName.replaceAll(" ", "")));
                            }
                        break;

                        case 1:
                            for(String stateName:indpStates)
                            {
                                independentStates.add((autVarName.replaceAll(" ", "")+(isAutomaton?symbol:"")+
                                        (allowedForbidden?EQUAL:NEQUAL)+stateName.replaceAll(" ", "")));
                            }
                        break;

                        case 2:
                            for(String stateName:stateSetTemp)
                            {
                                independentStates.add((autVarName.replaceAll(" ", "")+(allowedForbidden?EQUAL:NEQUAL)+stateName.replaceAll(" ", "")));
                            }
                            independentStates.add(inEq1);
                        break;

                        case 3:
                            for(String stateName:compStatesTemp)
                            {
                                independentStates.add((autVarName.replaceAll(" ", "")+(allowedForbidden?NEQUAL:EQUAL)+stateName.replaceAll(" ", "")));
                            }
                            independentStates.add(inEq2);
                        break;
                    }
                    
                    continue;
                }
                else
                {
                    autExpr += ((e.getFromNode().isRoot()?"":O_PAR));
                    ArrayList<String> complementStates = new ArrayList<String>();
                    complementStates = isAutomaton?bddAut.getComplementLocationNames(stateSet):automataBDD.getComplementValues(autVarName, stateSet);

                    ArrayList<String> stateSetTemp = (ArrayList<String>)stateSet.clone();
                    ArrayList<String> compStatesTemp = (ArrayList<String>)complementStates.clone();

                    ArrayList<String> incrementalSeq1 = new ArrayList<String>();
                    ArrayList<String> incrementalSeq2 = new ArrayList<String>();
                    String inEq1="";
                    String inEq2="";
                    int inEq1Len = Integer.MAX_VALUE;
                    int inEq2Len = Integer.MAX_VALUE;
                    boolean isComp = complementStates.size() < stateSet.size();
                    boolean flag = allowedForbidden ^ isComp;
                    if(!isAutomaton)
                    {
                        incrementalSeq1 = isIncrementalSeq(stateSet);
                        stateSetTemp.removeAll(incrementalSeq1);
                        incrementalSeq2 = isIncrementalSeq(complementStates);
                        compStatesTemp.removeAll(incrementalSeq2);
                        inEq1 = seq2inEqual(incrementalSeq1, autVarName.replaceAll(" ", ""), theAutomata.getMinValueofVar(autVarName),
                            theAutomata.getMaxValueofVar(autVarName), flag);
                        if(!inEq1.isEmpty())
                        {
                            if(!inEq1.contains("&"))
                                inEq1Len = 1+stateSetTemp.size();
                            else
                                inEq1Len = 2+stateSetTemp.size();
                        }
                        inEq2 = seq2inEqual(incrementalSeq2, autVarName.replaceAll(" ", ""), theAutomata.getMinValueofVar(autVarName),
                            theAutomata.getMaxValueofVar(autVarName), flag);
                        if(!inEq2.isEmpty())
                        {
                            if(!inEq2.contains("&"))
                                inEq2Len = 1+compStatesTemp.size();
                            else
                                inEq2Len = 2+compStatesTemp.size();
                        }
                    }                    

                    ArrayList<Integer> alternatives = new ArrayList<Integer>();
                    alternatives.add(complementStates.size());
                    alternatives.add(stateSet.size());
                    alternatives.add(inEq1Len);
                    alternatives.add(inEq2Len);
                    int index = smallestInEq(alternatives);

                    switch(index)
                    {
                        case 0:
                            for(String stateName:complementStates)
                            {
                                autExpr += ((autVarName.replaceAll(" ", "")+(isAutomaton?symbol:"")+
                                        (allowedForbidden?NEQUAL:EQUAL)+stateName.replaceAll(" ", ""))+ (allowedForbidden?AND:OR));
                                nbrOfTerms++;
                            }
                        break;

                        case 1:
                            for(String stateName:stateSet)
                            {
                                autExpr += ((autVarName.replaceAll(" ", "")+(isAutomaton?symbol:"")+(allowedForbidden?EQUAL:NEQUAL)+stateName.replaceAll(" ", ""))+ (allowedForbidden?OR:AND));
                                nbrOfTerms++;
                            }
                        break;

                        case 2:
                            for(String stateName:stateSetTemp)
                            {
                                autExpr += ((autVarName.replaceAll(" ", "")+(allowedForbidden?EQUAL:NEQUAL)+stateName.replaceAll(" ", ""))+ (allowedForbidden?OR:AND));
                            }
                            autExpr += (inEq1+(allowedForbidden?OR:AND));
                            nbrOfTerms += inEq1Len;
                        break;

                        case 3:
                            for(String stateName:compStatesTemp)
                            {
                                autExpr += ((autVarName.replaceAll(" ", "")+(allowedForbidden?NEQUAL:EQUAL)+stateName.replaceAll(" ", ""))+ (allowedForbidden?AND:OR));
                            }
                            autExpr += (inEq2+(allowedForbidden?AND:OR));
                            nbrOfTerms += inEq2Len;
                        break;
                    }

/*
                    if(complementStates.size() < stateSet.size())
                    {
                        for(String stateName:complementStates)
                        {
                            autExpr += ((symbol+autVarName.replaceAll(" ", "")+(allowedForbidden?NEQUAL:EQUAL)+stateName.replaceAll(" ", ""))+ (allowedForbidden?AND:OR));
                            nbrOfTerms++;
                        }
                    }
                    else
                    {
                        for(String stateName:stateSet)
                        {
                            autExpr += ((symbol+autVarName.replaceAll(" ", "")+(allowedForbidden?EQUAL:NEQUAL)+stateName.replaceAll(" ", ""))+ (allowedForbidden?OR:AND));
                            nbrOfTerms++;
                        }
                    }
*/
                    autExpr = autExpr.substring(0, autExpr.length()-3);

                    if(stateSet.size() > 1)
                        autExpr = ((e.getFromNode().isRoot()?"":O_PAR)) + autExpr + ((e.getFromNode().isRoot()?"":C_PAR));
                }

                String temp = MDD2Expr(mdd,e.getFromNode(),independentStates);
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
//            addedNodes2Dot.add(0);
            addedNodes2Dot.add(1);
            if(mdd.getNodes().size() > 1)
            {
                generateDot(out, mdd, mdd.getRoot(), addedNodes2Dot);
            }
            out.write("}");
            out.close();
        }
        catch (Exception e)
        {
           logger.error("MBDD to DOT: " + e.getMessage());
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
                    ArrayList<MDDEdge> conEdges = mdd.node2OutEdges(node);
                    for(MDDEdge e: conEdges)
                    {
                        String temp = ""+e.getFromNode().getID()+" -> "+e.getToNode().getID()+" [label=\"";
                        boolean flag = false;
                        for(String stateName :e.getLabelString())
                        {
                            flag = true;
                            temp += (stateName + " | ");
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
            logger.error("MBDD to DOT: " + e.getMessage());
        }

        return false;
    }

    public HashSet<String> generateStates(BDD states)
    {
        BDD.BDDIterator satIt = states.iterator(automataBDD.getSourceStatesVarSet());

        HashSet<String> output = new HashSet<String>();
/*        while(satIt.hasNext())
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
*/
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

    public int smallestInEq(ArrayList<Integer> alternatives)
    {
        int min = Collections.min(alternatives);
        return alternatives.indexOf(min);
    }

    public String seq2inEqual(ArrayList<String> seq, String var, int min, int max, boolean flag)
    {
        if(seq.size()>1)
        {
            int lastIndex = seq.size()-1;
            if(Integer.parseInt(seq.get(0)) == min)
            {
                return (var+(flag?"<=":">")+seq.get(lastIndex));
            }
            else if(Integer.parseInt(seq.get(lastIndex)) == max)
            {
                return (var+(flag?">=":"<")+seq.get(0));
            }
            else
            {
                return ("("+var+(flag?">=":"<")+seq.get(0) + " & " + var+(flag?"<=":">")+seq.get(lastIndex)+")");
            }
        }
        return "";
    }

    public ArrayList<String> isIncrementalSeq(ArrayList<String> values)
    {
        ArrayList<Integer> vals = new ArrayList<Integer>();
        for(String v:values)
            vals.add(Integer.parseInt(v));

        //Bubble sort
        boolean swapped = false;
        do
        {
         swapped = false;
         for(int j=0;j<vals.size()-1;j++)
         {
             if(vals.get(j)>vals.get(j+1))
             {
                 //swap values
                 int temp = vals.get(j+1);
                 vals.set(j+1, vals.get(j));
                 vals.set(j, temp);
                 swapped = true;
             }
         }
        }while(swapped);


        ArrayList<Integer> seq = new ArrayList<Integer>();
        ArrayList<Integer> seqTemp = new ArrayList<Integer>();
        for(int i=1;i<vals.size();i++)
        {
            if(vals.get(i) == (vals.get(i-1)+1))
            {
                seq.add(vals.get(i));
            }
            else
            {
                if(seq.size() > seqTemp.size())
                    seqTemp = (ArrayList<Integer>)seq.clone();

                seq.clear();
                seq.add(vals.get(i));
            }
        }
        if(seqTemp.size()>seq.size())
            seq = (ArrayList<Integer>)seqTemp.clone();

        ArrayList<String> output = new ArrayList<String>();
        for(Integer i:seq)
            output.add(""+i);

        return output;
    }

}
