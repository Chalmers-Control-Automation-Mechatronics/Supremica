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
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.properties.Config;


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
    String TRUE = "True";
    String FALSE = "False";
    String EQUAL = " == ";
    String NEQUAL = " != ";
    String eventName;

    int bddSize;
    private int nbrOfTerms;
    private int nbrOfCompHeurs = 0;
    private int nbrOfIndpHeurs = 0;

    private boolean allowedForbidden = false;
    private boolean optimalMode = false;
    private boolean applyComplementHeuristics = false;
    private boolean applyIndependentHeuristics = false;
    private boolean generateIDD_PS = false;

    private static Logger logger = LoggerFactory.createLogger(BDDAutomata.class);
    String pathRoot = "";//C:/Users/sajed/Desktop/MDD_files/";

//    HashSet<String> independentStates;


    /** Creates a new instance of BDDExtendedGuardGenerator */
    public BDDExtendedGuardGenerator(final BDDExtendedAutomata bddAutomata, final String eventLabel, final BDD states, EditorSynthesizerOptions options) {
        theAutomata = bddAutomata.getExtendedAutomata();
        automataBDD = bddAutomata;
        manager = automataBDD.getBDDManager();
        pathRoot = Config.FILE_SAVE_PATH.getAsString()+"/";
        generateIDD_PS = options.getSaveIDDInFile();

        switch(options.getExpressionType())
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
        final int currEventIndex = automataBDD.getEventIndex(eventName);
        sigmaBDD = manager.createBDD(currEventIndex, automataBDD.getEventDomain());

        final BDDEdges bddTransitions = automataBDD.getBDDEdges();

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

        applyComplementHeuristics = options.getCompHeuristic();
        applyIndependentHeuristics = options.getIndpHeuristic();


        if(optimalMode)
        {
            allowedForbidden = true;
            final String allowedGuard = generateGuard(mustAllowedStatesBDD);
            final int minNbrOfTerms = nbrOfTerms;
            int nbrOfCompHeurs = this.nbrOfCompHeurs;
            int nbrOfIndpHeurs = this.nbrOfIndpHeurs;

            allowedForbidden = false;
            final String forbiddenGuard = generateGuard(mustForbiddenStatesBDD);
            if(nbrOfTerms < minNbrOfTerms)
            {
                guard = forbiddenGuard;
            }
            else
            {
                guard = allowedGuard;
                nbrOfTerms = minNbrOfTerms;
                this.nbrOfCompHeurs = nbrOfCompHeurs;
                this.nbrOfIndpHeurs = nbrOfIndpHeurs;
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
/*
        ArrayList<IDD> children = new ArrayList<IDD>();
        ArrayList<ArrayList<String>> labels = new ArrayList<ArrayList<String>>();
        ArrayList<String> label = new ArrayList<String>();
        IDD idd1 = new IDD(new IDDNode("1", "1"));

        children = new ArrayList<IDD>();
        children.add(idd1);
        labels = new ArrayList<ArrayList<String>>();
        label.add("g");
        labels.add(label);
        IDD idd2 = new IDD(new IDDNode("2", "E"),children,labels);

        children = new ArrayList<IDD>();
        children.add(idd1);
        labels = new ArrayList<ArrayList<String>>();
        label = new ArrayList<String>();
        label.add("h");
        labels.add(label);
        IDD idd3 = new IDD(new IDDNode("3", "F"),children,labels);

        children = new ArrayList<IDD>();
        children.add(idd2);
        children.add(idd3);
        labels = new ArrayList<ArrayList<String>>();
        label = new ArrayList<String>();
        label.add("e");
        labels.add(label);
        label = new ArrayList<String>();
        label.add("f");
        labels.add(label);
        IDD idd4 = new IDD(new IDDNode("4", "D"),children,labels);

        children = new ArrayList<IDD>();
        children.add(idd4);
        labels = new ArrayList<ArrayList<String>>();
        label = new ArrayList<String>();
        label.add("c");
        labels.add(label);
        IDD idd5 = new IDD(new IDDNode("5", "B"),children,labels);

        children = new ArrayList<IDD>();
        children.add(idd4);
        labels = new ArrayList<ArrayList<String>>();
        label = new ArrayList<String>();
        label.add("d");
        labels.add(label);
        IDD idd6 = new IDD(new IDDNode("6", "C"),children,labels);

        children = new ArrayList<IDD>();
        children.add(idd5);
        children.add(idd6);
        labels = new ArrayList<ArrayList<String>>();
        label = new ArrayList<String>();
        label.add("a");
        labels.add(label);
        label = new ArrayList<String>();
        label.add("b");
        labels.add(label);
        IDD idd7 = new IDD(new IDDNode("7", "A"),children,labels);

        HashMap<String, String> memory = new HashMap<String, String>();
        System.out.println(generateExpression(idd7, memory));
*/
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

    public int getNbrOfCompHeuris()
    {
        return nbrOfCompHeurs;
    }

    public int getNbrOfIndpHeuris()
    {
        return nbrOfIndpHeurs;
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

        final BDD transitionsWithSigma = forwardMonolithicTransitionsBDD.relprod(sigmaBDD, automataBDD.getEventVarSet());
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

    public void BDD2IDD2PS(final BDD bdd, final String fileName)
    {
        generateDOT(generateIDD(bdd), pathRoot+fileName+".dot");

        final Runtime rt = Runtime.getRuntime();
        try{
            final Process proc = rt.exec("cmd /C dot -Tps "+pathRoot+fileName+".dot"+" -o "+pathRoot+fileName+".ps");
            proc.waitFor();
            proc.exitValue();
        }catch(final Exception e) {System.out.println(e);}
    }

    public String generateGuard(final BDD states)
    {
        nbrOfTerms = 0;
        nbrOfCompHeurs = 0;
        nbrOfIndpHeurs = 0;
        String guard = "";

        if(states.equals(careStatesBDD))
        {
            guard = allowedForbidden ? TRUE : FALSE;
            nbrOfTerms ++;
        }
        else if(states.satCount(automataBDD.getSourceStatesVarSet()) == 0)
        {
            guard = allowedForbidden ? FALSE : TRUE;
            nbrOfTerms ++;
        }
        else
        {
            BDD goodBDD = states.simplify(careStatesBDD.toVarSet());

            if(states.nodeCount() <= goodBDD.nodeCount())
                goodBDD = states;

            if(goodBDD.nodeCount()>0)
            {
                final IDD goodIDD = generateIDD(goodBDD);
//                System.out.println(goodIDD.nbrOfNodes());
//                System.out.println(eventName+"   "+(allowedForbidden?"allowed":"forbidden")+"        "+states.nodeCount()+"          "+goodBDD.nodeCount()+"     "+goodMDD.nodeCount());
//                System.out.println(eventName);
//                goodBDD.printDot();

                String fileName = "idd_"+eventName;
                if(allowedForbidden)
                    fileName += "_allowed";
                else
                    fileName += "_forbidden";

                if(generateIDD_PS)
                    BDD2IDD2PS(goodBDD, fileName);
                
                guard = generateExpression(goodIDD);
            }
        }

        return guard;
    }
    
   public IDD generateIDD(final BDD bdd)
    {   
        HashMap<Integer, IDD> visitedNodes = new HashMap<Integer, IDD>();
        visitedNodes.put(1, new IDD(new IDDNode("1", "1")));
        IDD idd = null;
        if(bdd.isZero())
            idd = new IDD(new IDDNode("0","0"));
        
        if(bdd.isOne())
            idd = new IDD(new IDDNode("1","1"));
        
        if(bdd.nodeCount() > 0)
        {
            final IDDNode root = new IDDNode(""+bdd.hashCode(),automataBDD.getAutVarName(bdd.var()));
            idd = new IDD(root);            
            final BDD varBDD = automataBDD.getBDDforSourceBDDVar(bdd.var());

            BDD2IDD(bdd.low(), varBDD.not(), idd, visitedNodes);
            BDD2IDD(bdd.high(), varBDD, idd, visitedNodes);
        }

        return idd;
    }

    public void BDD2IDD(final BDD bdd, final BDD autStatesBDD, IDD idd, HashMap<Integer, IDD> visitedNodes)
    {
        if(!bdd.isZero())
        {
            if(bdd.isOne() || !automataBDD.getAutVarName(bdd.var()).equals(idd.getRoot().getName()))
            {
                final ArrayList<String> states = bdd2automatonStates(autStatesBDD);

                IDD nextIDD = visitedNodes.get(bdd.hashCode());

                //if node has not been visited
                if(nextIDD == null)
                {
                    IDDNode node = new IDDNode(""+bdd.hashCode(),automataBDD.getAutVarName(bdd.var()));
                    nextIDD = new IDD(node);

                    final BDD varBDD = automataBDD.getBDDforSourceBDDVar(bdd.var());
                    BDD2IDD(bdd.low(), varBDD.not(), nextIDD, visitedNodes);
                    BDD2IDD( bdd.high(), varBDD, nextIDD, visitedNodes);

                    if(!states.isEmpty())
                        idd.addChild(nextIDD, states);

                    visitedNodes.put(bdd.hashCode(), nextIDD);
                }
                else
                {
                    if(!states.isEmpty())
                    {
                        if(idd.labelOfChild(nextIDD) != null)//'idd' has a child with root 'node'
                            idd.labelOfChild(nextIDD).addAll(states);
                        else
                            idd.addChild(nextIDD, states);
                    }
                }
            }
            else
            {
                final BDD varBDD = automataBDD.getBDDforSourceBDDVar(bdd.var());

                BDD2IDD(bdd.low(),  autStatesBDD.and(varBDD.not()), idd, visitedNodes);
                BDD2IDD(bdd.high(), autStatesBDD.and(varBDD), idd, visitedNodes);
            }
        }
    }

    ArrayList<String> bdd2automatonStates(final BDD autStatesBDD)
    {
        final ArrayList<String> output = new ArrayList<String>();
        final int var = autStatesBDD.var();

        if(automataBDD.isSourceLocationVar(var))
        {
            final ExtendedAutomaton exAut = automataBDD.getBDDExAutomaton(automataBDD.getAutVarName(var)).getExAutomaton();
            for(final NodeProxy location:exAut.getNodes())
            {
                final int locationIndex = automataBDD.getLocationIndex(exAut, location);
                final BDD locationBDD = manager.getFactory().buildCube(locationIndex, automataBDD.getSourceLocationDomain(exAut.getName()).vars());
                if(!autStatesBDD.and(locationBDD).isZero() && !locationBDD.and(safeStatesEnablingSigmaBDD).isZero())
                {
                    output.add(location.getName());
                }
            }
        }
        else
        {
            final int maxValue = automataBDD.getExtendedAutomata().getMaxValueofVar(automataBDD.getAutVarName(var));
            final int minValue = automataBDD.getExtendedAutomata().getMinValueofVar(automataBDD.getAutVarName(var));
            for(int i=minValue;i<=maxValue;i++)
            {
                final BDD valueBDD = automataBDD.getConstantBDD(automataBDD.getAutVarName(var), i);
                if(!autStatesBDD.and(valueBDD).isZero() && !valueBDD.and(safeStatesEnablingSigmaBDD).isZero())
                {
                    output.add(""+automataBDD.getIndexMap().getValOfIndex(i));
                }
            }

        }

        return output;
    }
 

    public StringIntPair generateStateSetTerm(boolean isComp, boolean isAutomaton, String variable, ArrayList<String> set, String op1, String op2, String ineq1, String ineq2)
    {
        variable = variable.replaceAll(" ", "");
        int localNbrOfTerms = 0;
        String expr = "";
        final String symbol = automataBDD.getLocVarSuffix();

        ArrayList<String> incrementalSeq = new ArrayList<String>();
        ArrayList<String> setTemp = (ArrayList<String>)set.clone();
        String inEq="";
        int inEqLength = Integer.MAX_VALUE;
        if(!isAutomaton)
        {
            incrementalSeq = isIncrementalSeq(set);
            setTemp.removeAll(incrementalSeq);
            inEq = seq2inEqual(incrementalSeq, variable, theAutomata.getMinValueofVar(variable), theAutomata.getMaxValueofVar(variable), allowedForbidden ^ isComp);
            if(!inEq.isEmpty())
            {
                if(inEq.contains("&"))
                {
                    inEqLength = 2+setTemp.size();
                }
                else
                {
                    inEqLength = 1+setTemp.size();
                }
            }
        }

        if(inEqLength < set.size())
        {
            set = (ArrayList<String>)setTemp.clone();
            expr += ((allowedForbidden?op1:op2)+inEq);
            localNbrOfTerms += (inEqLength-setTemp.size());
        }

        if(!set.isEmpty())
        {
            expr = variable+(isAutomaton?symbol:"")+(allowedForbidden?ineq1:ineq2)+set.get(0).replaceAll(" ", "");
            localNbrOfTerms++;
        }
        for(int i = 1; i < set.size(); i++)
        {
            expr += ((allowedForbidden?op1:op2) + variable+(isAutomaton?symbol:"")+(allowedForbidden?ineq1:ineq2)+set.get(i).replaceAll(" ", ""));
            localNbrOfTerms++;
        }

        return new StringIntPair(expr, localNbrOfTerms);
    }

    public boolean isIndependentHeuristicApplicable(String autVarName, ArrayList<String> stateSet)
    {
        final BDDExtendedAutomaton bddAut = automataBDD.getBDDExAutomaton(autVarName);
        final boolean isAutomaton = (bddAut != null)?true:false;
        final ArrayList<String> indpStates = new ArrayList<String>();
        for(final String stateName:stateSet)
        {
            BDD stateBDD = null;
            if(isAutomaton)
            {
                final ExtendedAutomaton exAut = bddAut.getExAutomaton();
                final int stateIndex = automataBDD.getLocationIndex(exAut, exAut.getLocationWithName(stateName));
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

        return indpStates.equals(stateSet);
    }
 
    public StringIntPair compelmentHeuristic(String autVarName, ArrayList<String> stateSet)
    {
        int localNbrOfTerms = 0;
        String expr = "";
        final BDDExtendedAutomaton bddAut = automataBDD.getBDDExAutomaton(autVarName);
        final boolean isAutomaton = (bddAut != null)?true:false;

        ArrayList<String> complementStates = new ArrayList<String>();
        complementStates = isAutomaton?bddAut.getComplementLocationNames(stateSet):automataBDD.getComplementValues(autVarName, stateSet);

        final boolean isComp = complementStates.size() < stateSet.size();
        StringIntPair e_n = null;
        if(isComp)
        {
            nbrOfCompHeurs++;
            e_n = generateStateSetTerm(isComp, isAutomaton, autVarName, complementStates, AND, OR, NEQUAL, EQUAL);
        }
        else
        {
            e_n = generateStateSetTerm(isComp, isAutomaton, autVarName, stateSet, OR, AND, EQUAL, NEQUAL);
        }

        expr = e_n.s;
        localNbrOfTerms += e_n.i;

        return new StringIntPair(expr,localNbrOfTerms);

    }

    public String generateExpression(IDD idd)
    {
        String output = "";
        HashMap<String, StringIntPair> cache = new HashMap<String, StringIntPair>();
        StringIntPair sip = IDD2expr(idd, cache);
        output = sip.s;
        nbrOfTerms = Math.abs(sip.i);

        return output;
    }

    public StringIntPair IDD2expr(IDD idd, HashMap<String, StringIntPair> cache)
    {
        int localNbrOfTerms = 0;

        if(idd.isOneTerminal())
        {
            return new StringIntPair("", 0);
        }

        ArrayList<String> terms = new ArrayList<String>();

        for(IDD iddChild:idd.getChildren())
        {
            int inForNbr = 0;
            final String autVarName = idd.getRoot().getName();
            final BDDExtendedAutomaton bddAut = automataBDD.getBDDExAutomaton(autVarName);
            final boolean isAutomaton = (bddAut != null)?true:false;
            StringIntPair expr_Nbr = null;
            String idChild = iddChild.getRoot().getID();
            if(!applyComplementHeuristics)
            {
                expr_Nbr = generateStateSetTerm(false, isAutomaton, autVarName, idd.labelOfChild(iddChild), OR, AND, EQUAL, NEQUAL);
            }
            else
            {
                expr_Nbr = compelmentHeuristic(autVarName, idd.labelOfChild(iddChild));
            }

            boolean independentApplicable = applyIndependentHeuristics && isIndependentHeuristicApplicable(autVarName, idd.labelOfChild(iddChild));

            String stateExpr = (expr_Nbr.i==1)?expr_Nbr.s:("("+expr_Nbr.s+")");
            inForNbr += expr_Nbr.i;

            String expr = "";
            if(cache.get(idChild) == null) //if 'iddChild' is not visisted
            {
                if(!independentApplicable)
                {
                    StringIntPair e_n = IDD2expr(iddChild,cache);
                    if(e_n.i >= 0)
                    {
                        expr = e_n.s;
                        inForNbr += e_n.i;
                        cache.put(idChild, e_n);
                    }
                    else
                    {
                        stateExpr = (Math.abs(e_n.i)==1)?e_n.s:("("+e_n.s+")");
                        if(idd.getChildren().size() > 1 || idd.getParents().isEmpty())
                        {
                            inForNbr = (-e_n.i);
                        }                        
                        else
                            inForNbr = e_n.i;
                    }

                }
                else
                {
                    nbrOfIndpHeurs++;
                    if(idd.getChildren().size() == 1)
                        inForNbr = -inForNbr;
                }
            }
            else
            {
                StringIntPair e_n = cache.get(idChild);
                expr = e_n.s;
                inForNbr += e_n.i;
            }

            if(!expr.isEmpty())
            {
                terms.add("("+stateExpr +(allowedForbidden?AND:OR)+expr+")");
            }
            else
            {
                terms.add(stateExpr);
            }

            localNbrOfTerms += inForNbr;

        }

        String expression = "";
        if(!terms.isEmpty())
            expression = terms.get(0);
        for(int i = 1;i<terms.size();i++)
        {
            expression = "("+expression + (allowedForbidden?OR:AND) + terms.get(i)+")";
        }

        return new StringIntPair(expression, localNbrOfTerms);
    }

 


    public void generateDOT(IDD idd, final String path)
    {
        try
        {
            final FileWriter fstream = new FileWriter(path);
            final BufferedWriter out = new BufferedWriter(fstream);
            out.write("digraph G {");
            out.newLine();
            out.write("size = \"7.5,10\"");
            out.newLine();
//            out.write("0 [shape=box, label=\"0\", style=filled, shape=box, height=0.3, width=0.3];");
//            out.newLine();
            out.write("1 [shape=box, label=\"1\", style=filled, shape=box, height=0.3, width=0.3];");
            out.newLine();
            final HashSet<IDD> visited = new HashSet<IDD>();
//            addedNodes2Dot.add(0);
            if(idd.nbrOfNodes() > 1)
            {
                IDD2DOT(out, idd, visited);
            }
            out.write("}");
            out.close();
        }
        catch (final Exception e)
        {
           logger.error("IDD to DOT: " + e.getMessage());
        }
    }


    public void IDD2DOT(BufferedWriter out, IDD idd, final HashSet<IDD> visited)
    {
        IDDNode root = idd.getRoot();
        try
        {
            if(!visited.contains(idd))
            {
                out.write(""+root.getID()+" [label=\""+root.getName()+"\"];");
                out.newLine();
                visited.add(idd);

                for(IDD child:idd.getChildren())
                {
                    String temp = ""+root.getID()+" -> "+child.getRoot().getID()+" [label=\"";
                    ArrayList<String> label = idd.labelOfChild(child);
                    if(label.size() > 0)
                        temp += label.get(0);
                    for(int i = 1;i < label.size(); i++)
                    {
                        temp += ("|" + label.get(i));
                    }

                    out.write(temp+"\"];");
                    out.newLine();
                    IDD2DOT(out, child, visited);

                }      
            }
        }
        catch (final Exception e)
        {
            logger.error("IDD to DOT: " + e.getMessage());
        }
    }

    public String BDD2Expr(final BDD bdd)
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

    public int smallestInEq(final ArrayList<Integer> alternatives)
    {
        final int min = Collections.min(alternatives);
        return alternatives.indexOf(min);
    }

    public String seq2inEqual(final ArrayList<String> seq, final String var, final int min, final int max, final boolean flag)
    {
        if(seq.size()>1)
        {
            final int lastIndex = seq.size()-1;
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

    @SuppressWarnings("unchecked")
    public ArrayList<String> isIncrementalSeq(final ArrayList<String> values)
    {
        final ArrayList<Integer> vals = new ArrayList<Integer>();
        for(final String v:values)
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
                 final int temp = vals.get(j+1);
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

        final ArrayList<String> output = new ArrayList<String>();
        for(final Integer i:seq)
            output.add(""+i);

        return output;
    }

    class StringIntPair
    {
        private String s;
        private int i;
        StringIntPair(String s, int i)
        {
            this.s = s;
            this.i = i;
        }
    }

}
