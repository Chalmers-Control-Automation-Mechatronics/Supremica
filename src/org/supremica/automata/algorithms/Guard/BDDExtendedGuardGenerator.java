/*
 * BDDGuardGenerator.java
 *
 * Created on September 11, 2008, 12:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author Sajed Miremadi, Zhennan Fei
 */
package org.supremica.automata.algorithms.Guard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.javabdd.BDD;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.BDD.BDDAutomata;
import org.supremica.automata.BDD.EFA.BDDEdges;
import org.supremica.automata.BDD.EFA.BDDExtendedAutomata;
import org.supremica.automata.BDD.EFA.BDDExtendedAutomaton;
import org.supremica.automata.BDD.EFA.BDDExtendedManager;
import org.supremica.automata.BDD.EFA.BDDMonolithicEdges;
import org.supremica.automata.BDD.EFA.BDDPartitionAlgoWorker;
import org.supremica.automata.BDD.EFA.BDDPartitionAlgoWorkerAut;
import org.supremica.automata.BDD.EFA.BDDPartitionAlgoWorkerEve;
import org.supremica.automata.BDD.EFA.BDDPartitionSetAut;
import org.supremica.automata.BDD.EFA.IDD;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;

/**
 *
 * @author Sajed
 */
public final class BDDExtendedGuardGenerator {

    BDD mustAllowedStatesBDD;
    BDD mustForbiddenStatesBDD;
    BDD dontCareStatesBDD;
    BDD sigmaBDD;
    BDD statesEnablingSigmaBDD;
    BDD safeStatesBDD;
    BDD careStatesBDD;
    BDD statesLeading2ForbiddenBDD;
    BDD forwardMonolithicTransitionsBDD;
//    BDD backwardMonolithicTransitionsBDD;
    BDD safeStatesEnablingSigmaBDD;
    BDDExtendedAutomata automataBDD;
    BDDExtendedManager manager;
    ExtendedAutomata theAutomata;
    // private final String STATE_DELIMITER = ".";
    private final String OR = " " + CompilerOperatorTable.getInstance().getOrOperator().getName() + " ";
    private final String AND = " " + CompilerOperatorTable.getInstance().getAndOperator().getName() + " ";
    @SuppressWarnings("unused")
    private final String O_PAR = "(";
    @SuppressWarnings("unused")
    private final String C_PAR = ")";
    String guard = "";
    public String TRUE = "1";
    public String FALSE = "0";
    private final String EQUAL = " " + CompilerOperatorTable.getInstance().getEqualsOperator().getName() + " ";
    private final String NEQUAL = " " + CompilerOperatorTable.getInstance().getNotEqualsOperator().getName() + " ";
    private final String eventName;
    int bddSize;
    private int nbrOfTerms;
    private int nbrOfCompHeurs = 0;
    private int nbrOfIndpHeurs = 0;
    private boolean allowedForbidden = false;
    private boolean optimalMode = false;
    private boolean applyComplementHeuristics = false;
    private boolean applyIndependentHeuristics = false;
    private boolean generateIDD_PS = false;
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.createLogger(BDDAutomata.class);
    String bestStateSet = "";
    private boolean isEventBlocked = false;
    @SuppressWarnings("unused")
    private final EditorSynthesizerOptions options;

    /** Creates a new instance of BDDExtendedGuardGenerator */
    public BDDExtendedGuardGenerator(final BDDExtendedAutomata bddAutomata, final String eventName, final BDD states, final EditorSynthesizerOptions options) {
        theAutomata = bddAutomata.getExtendedAutomata();
        automataBDD = bddAutomata;
        manager = automataBDD.getManager();
        automataBDD.setPathRoot(Config.FILE_SAVE_PATH.getAsString() + "/");
        generateIDD_PS = options.getSaveIDDInFile();

        this.options = options;
        /*
        final ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
        final ExpressionParser parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
        SimpleExpressionSubject testExpr = null;
        SimpleExpressionSubject careExpr = null;
        try{
        String exprString = "(x2&((!x1|(!x3|x4))&(!(!x3|x4)|x1))) | (!(x2|((!x4|x1)&(x1|x3))))";
        String careString = "(!x2&x3&x4)|(x2&(!x3|x4)&(!x4|x3))";
        testExpr = (SimpleExpressionSubject)(parser.parse(exprString,Operator.TYPE_BOOLEAN));
        careExpr = (SimpleExpressionSubject)(parser.parse(careString,Operator.TYPE_BOOLEAN));
        }catch(ParseException e){}
        manager.guard2BDD(testExpr).printDot();
        manager.guard2BDD(testExpr).simplify(manager.guard2BDD(careExpr).toVarSet()).printDot();
         */

        switch (options.getExpressionType())
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

        this.eventName = eventName;
        final int currEventIndex = automataBDD.getEventIndex(eventName);
        sigmaBDD = manager.createBDD(currEventIndex, automataBDD.getEventDomain());
        safeStatesBDD = states;


        if (bddAutomata.getSynthAlg().equals(SynthesisAlgorithm.MONOLITHICBDD))
		{
            final BDDEdges bddTransitions = automataBDD.getBDDEdges();

            forwardMonolithicTransitionsBDD = (((BDDMonolithicEdges) bddTransitions).getMonolithicEdgesForwardWithEventsBDD());
//            if(options.getOptimization())
//            {
//                forwardMonolithicTransitionsBDD = forwardMonolithicTransitionsBDD.exist(
//                    bddAutomata.getSourceVariableDomain(bddAutomata.getIndexMap().getVariableIndexByName(bddAutomata.getExtendedAutomata().getGlobalClockName())).set());
//            }


            computeStatesEnablingSigma();

            if (generateIDD_PS)
			{
                String fileName = "idd_" + eventName + "_enabled";
                bddAutomata.BDD2IDD2PS(statesEnablingSigmaBDD, statesEnablingSigmaBDD, fileName);

                fileName = "iddSafeStates";
                bddAutomata.BDD2IDD2PS(safeStatesBDD, safeStatesBDD, fileName);


                fileName = "iddReachableStates";
                bddAutomata.BDD2IDD2PS(automataBDD.getReachableStates(), automataBDD.getReachableStates(), fileName);

                fileName = "iddCoreachableStates";
                bddAutomata.BDD2IDD2PS(automataBDD.getCoreachableStates(), automataBDD.getCoreachableStates(), fileName);

            }

            computeSafeStatesEnablingSigma();
//        System.err.println("safe states enabling "+eventName+": "+bddAutomata.nbrOfStatesBDD(safeStatesEnablingSigmaBDD));

            if (generateIDD_PS)
			{
                final String fileName = "iddSafe_" + eventName + "_enabled";
                bddAutomata.BDD2IDD2PS(safeStatesEnablingSigmaBDD, safeStatesEnablingSigmaBDD, fileName);
            }

            computeStatesLeading2ForbiddenStates();

            if (generateIDD_PS)
			{
                final String fileName = "idd_" + eventName + "_leadingToForbidden";
                bddAutomata.BDD2IDD2PS(statesLeading2ForbiddenBDD, statesLeading2ForbiddenBDD, fileName);
            }

            computeMustAllowedSates();

            if (generateIDD_PS)
			{
                final String fileName = "idd_" + eventName + "_allowed";
                bddAutomata.BDD2IDD2PS(mustAllowedStatesBDD, mustAllowedStatesBDD, fileName);
            }

            computeMustForbiddenSates();

            if (generateIDD_PS)
			{
                final String fileName = "idd_" + eventName + "_forbidden";
                bddAutomata.BDD2IDD2PS(mustForbiddenStatesBDD, mustForbiddenStatesBDD, fileName);
            }
        } else if (bddAutomata.getSynthAlg().equals(SynthesisAlgorithm.PARTITIONBDD)) {
            disjunctivelyComputeMustAllowedStates();
            disjunctivelyComputeMustForbiddenStates();
        }

        computeCareStates();

        applyComplementHeuristics = options.getCompHeuristic();
        applyIndependentHeuristics = options.getIndpHeuristic();

        if (optimalMode)
		{
            allowedForbidden = true;

            final String allowedGuard = generateGuard(mustAllowedStatesBDD);
            final int minNbrOfTerms = nbrOfTerms;
            final int nbrOfCompHeuris = this.nbrOfCompHeurs;
            final int nbrOfIndpHeuris = this.nbrOfIndpHeurs;

            allowedForbidden = false;
            final String forbiddenGuard = generateGuard(mustForbiddenStatesBDD);
            if (nbrOfTerms < minNbrOfTerms)
			{
                guard = forbiddenGuard;
                bestStateSet = "FORBIDDEN";
            }
			else
			{
                guard = allowedGuard;
                nbrOfTerms = minNbrOfTerms;
                this.nbrOfCompHeurs = nbrOfCompHeuris;
                this.nbrOfIndpHeurs = nbrOfIndpHeuris;
                bestStateSet = "ALLOWED";
            }
        }
		else
		{
            if (allowedForbidden)
			{
                guard = generateGuard(mustAllowedStatesBDD);
                bestStateSet = "ALLOWED";
            }
			else
			{
                guard = generateGuard(mustForbiddenStatesBDD);
                bestStateSet = "FORBIDDEN";
            }
        }

        //The event is blocked in the synchronization process
        if (mustAllowedStatesBDD.satCount(automataBDD.getSourceStatesVarSet()) == 0
                && mustForbiddenStatesBDD.satCount(automataBDD.getSourceStatesVarSet()) == 0)
		{
            guard = FALSE;
            isEventBlocked = true;
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

    public boolean isEventBlocked()
	{
        return isEventBlocked;
    }

    public String getBestStateSet()
	{
        return bestStateSet;
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
    private void computeStatesEnablingSigma()
	{
        statesEnablingSigmaBDD = forwardMonolithicTransitionsBDD.relprod(sigmaBDD, automataBDD.getDestStatesVarSet());
        statesEnablingSigmaBDD = statesEnablingSigmaBDD.exist(automataBDD.getEventVarSet());
//        return statesEnablingSigmaBDD;
    }

    private void computeStatesLeading2ForbiddenStates()
	{
        BDD forbiddenAndReachableStatesBDD = automataBDD.getReachableStates().and(safeStatesBDD.not());

        forbiddenAndReachableStatesBDD = forbiddenAndReachableStatesBDD.replace(automataBDD.getSourceToDestLocationPairing());
        forbiddenAndReachableStatesBDD = forbiddenAndReachableStatesBDD.replace(automataBDD.getSourceToDestVariablePairing());

        final BDD transitionsWithSigma = forwardMonolithicTransitionsBDD.relprod(sigmaBDD, automataBDD.getEventVarSet());

        statesLeading2ForbiddenBDD = (transitionsWithSigma.and(forbiddenAndReachableStatesBDD)).exist(automataBDD.getDestStatesVarSet());

//        return statesLeading2ForbiddenBDD;
    }

    //Q^sigma_sup
    private void computeMustAllowedSates()
	{
        mustAllowedStatesBDD = safeStatesEnablingSigmaBDD.and(statesLeading2ForbiddenBDD.not());
//        BDD destSafeSates = safeStatesBDD.replace(automataBDD.getSourceToDestLocationPairing());
//        destSafeSates = destSafeSates.replace(automataBDD.getSourceToDestVariablePairing());
//        final BDD transitionsWithSigma = forwardMonolithicTransitionsBDD.relprod(sigmaBDD, automataBDD.getEventVarSet());
//        mustAllowedStatesBDD = transitionsWithSigma.and(destSafeSates).and(safeStatesEnablingSigmaBDD);
//        mustAllowedStatesBDD = mustAllowedStatesBDD.exist(automataBDD.getEventVarSet()).exist(automataBDD.getDestStatesVarSet());
    }

    //Q^sigma & C(Q^sigma_a) & Q_sup
    private void computeMustForbiddenSates()
	{
        mustForbiddenStatesBDD = safeStatesEnablingSigmaBDD.and(mustAllowedStatesBDD.not());
//        return mustForbiddenStatesBDD;
    }

    private void computeCareStates()
	{
        careStatesBDD = mustAllowedStatesBDD.or(mustForbiddenStatesBDD);
    }

    //Q & C(mustForbiddenStatesBDD) & C(mustAllowedStatesBDD)   OR C(careStatesBDD)
    public void computeDontCareStates()
	{
        dontCareStatesBDD = careStatesBDD.not();
//        return dontCareStatesBDD;
    }

    private void computeSafeStatesEnablingSigma()
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
        if (guard.equals(TRUE))
		{
            return true;
        }
        return false;
    }

    public boolean guardIsFalse()
    {
      if (guard.equals(FALSE))
         return true;
      return false;
    }

    public String generateGuard(final BDD states)
	{
        nbrOfTerms = 0;
        nbrOfCompHeurs = 0;
        nbrOfIndpHeurs = 0;
        String localGuard = "";


        if (states.equals(careStatesBDD))
		{
            localGuard = allowedForbidden ? TRUE : FALSE;
            nbrOfTerms++;
        }
		else if (states.satCount(automataBDD.getSourceStatesVarSet()) == 0)
		{
            localGuard = allowedForbidden ? FALSE : TRUE;
            nbrOfTerms++;
        }
		else
		{
            BDD goodBDD = states.simplify(careStatesBDD);
//            BDD goodBDD = states.simplify(careStatesBDD.exist(automataBDD.getTestVarSet()).toVarSet());
//            BDD goodBDD = states.exist(automataBDD.getTestVarSet());

            if (states.nodeCount() <= goodBDD.nodeCount())
			{
                goodBDD = states;
            }

            if (goodBDD.nodeCount() > 0)
			{
                final IDD goodIDD = automataBDD.generateIDD(goodBDD, safeStatesEnablingSigmaBDD);
//                System.out.println(goodIDD.nbrOfNodes());
//                System.out.println(eventName+"   "+(allowedForbidden?"allowed":"forbidden")+"        "+states.nodeCount()+"          "+goodBDD.nodeCount()+"     "+goodMDD.nodeCount());
//                System.out.println(eventName);
//                goodBDD.printDot();

                String fileName = "idd_" + eventName;
                if (allowedForbidden)
				{
                    fileName += "_allowed";
                }
				else
				{
                    fileName += "_forbidden";
                }

                if (generateIDD_PS)
				{
                    automataBDD.BDD2IDD2PS(goodBDD, safeStatesEnablingSigmaBDD, fileName);
                }

                localGuard = generateExpression(goodIDD);
            }
        }

        return localGuard;
    }

    StringIntPair generateStateSetTerm(final boolean isComp, final boolean isAutomaton, String variable, ArrayList<String> set)
	{
        variable = variable.replaceAll(" ", "");
        int localNbrOfTerms = 0;
        String expr = "";
        final String symbol = automataBDD.getLocVarSuffix();
        final boolean flag = allowedForbidden ^ isComp;

        ArrayList<String> incrementalSeq;
        final ArrayList<String> setTemp = new ArrayList<String>(set);
        String inEq;
        if (!isAutomaton)
		{
            boolean firstTime = true;
            do
			{
                incrementalSeq = isIncrementalSeq(setTemp);
                setTemp.removeAll(incrementalSeq);
                inEq = seq2inEqual(incrementalSeq, variable, theAutomata.getMinValueofVar(variable), theAutomata.getMaxValueofVar(variable), flag);
                if (!inEq.isEmpty())
				{
                    if (inEq.contains(AND) || inEq.contains(OR))
					{
                        localNbrOfTerms += 2;
                    }
					else
					{
                        localNbrOfTerms += 1;
                    }

                    set = new ArrayList<String>(setTemp);
                    if (firstTime)
					{
                        expr = inEq;
                        firstTime = false;
                    }
					else
					{
                        expr += ((flag ? OR : AND) + inEq);
                    }
                }

            } while (!inEq.isEmpty() && !setTemp.isEmpty());

            if (!expr.isEmpty())
			{
                expr = "(" + expr + ")";
            }
        }

        if (!set.isEmpty())
		{
            final String ex = variable + (isAutomaton ? symbol : "") + (flag ? EQUAL : NEQUAL) + set.get(0).replaceAll(" ", "");
            if (expr.isEmpty())
			{
                expr = ex;
            }
			else
			{
                expr += ((flag ? OR : AND) + ex);
            }
            localNbrOfTerms++;
        }
        for (int i = 1; i < set.size(); i++)
		{
            expr += ((flag ? OR : AND) + variable + (isAutomaton ? symbol : "") + (flag ? EQUAL : NEQUAL) + set.get(i).replaceAll(" ", ""));
            localNbrOfTerms++;
        }

        return new StringIntPair(expr, localNbrOfTerms);
    }

    public boolean isIndependentHeuristicApplicable(final String autVarName, final ArrayList<String> stateSet)
	{
        final BDDExtendedAutomaton bddAut = automataBDD.getBDDExAutomaton(autVarName);
        final boolean isAutomaton = (bddAut != null) ? true : false;
        final ArrayList<String> indpStates = new ArrayList<String>();
        for (final String stateName : stateSet)
		{
            BDD stateBDD = null;
            if (isAutomaton)
			{
                final ExtendedAutomaton exAut = bddAut.getExAutomaton();
                final int stateIndex = automataBDD.getLocationIndex(exAut, exAut.getLocationWithName(stateName));
                stateBDD = manager.getFactory().buildCube(stateIndex, automataBDD.getSourceLocationDomain(exAut.getName()).vars());
            }
			else
			{
                stateBDD = automataBDD.getConstantBDD(autVarName, Integer.parseInt(stateName));
            }

            if ((allowedForbidden ? mustForbiddenStatesBDD : mustAllowedStatesBDD).and(stateBDD).nodeCount() == 0)
			{
                indpStates.add(stateName);
//                    indpStates.add((symbol+autVarName.replaceAll(" ", "")+(allowedForbidden?EQUAL:NEQUAL)+stateName.replaceAll(" ", "")));
            }
        }

        return indpStates.equals(stateSet);
    }

    StringIntPair compelmentHeuristic(final String autVarName, final ArrayList<String> stateSet)
	{
        int localNbrOfTerms = 0;
        String expr = "";
        final BDDExtendedAutomaton bddAut = automataBDD.getBDDExAutomaton(autVarName);
        final boolean isAutomaton = (bddAut != null) ? true : false;

        ArrayList<String> inputStateSet = new ArrayList<String>(stateSet);
        ArrayList<String> complementStates = new ArrayList<String>();
        complementStates = isAutomaton ? bddAut.getComplementLocationNames(stateSet) : automataBDD.getComplementValues(autVarName, stateSet);
        final boolean isComp = complementStates.size() < stateSet.size();
        StringIntPair e_n = null;
        if (isComp)
		{
            nbrOfCompHeurs++;
            inputStateSet = new ArrayList<String>(complementStates);
        }

        e_n = generateStateSetTerm(isComp, isAutomaton, autVarName, inputStateSet);

        expr = e_n.s;
        localNbrOfTerms += e_n.i;

        return new StringIntPair(expr, localNbrOfTerms);

    }

    public String generateExpression(final IDD idd)
	{
        String output = "";
        final HashMap<String, StringIntPair> cache = new HashMap<String, StringIntPair>();
        final StringIntPair sip = IDD2expr(idd, cache);
        output = sip.s;
        nbrOfTerms = Math.abs(sip.i);

        return output;
    }

    StringIntPair IDD2expr(final IDD idd, final HashMap<String, StringIntPair> cache)
	{
        int localNbrOfTerms = 0;

        if (idd.isOneTerminal())
		{
            return new StringIntPair("", 0);
        }

        final ArrayList<String> terms = new ArrayList<String>();

        for (final IDD iddChild : idd.getChildren())
		{
            int inForNbr = 0;
            final String autVarName = idd.getRoot().getName();
            final BDDExtendedAutomaton bddAut = automataBDD.getBDDExAutomaton(autVarName);
            final boolean isAutomaton = (bddAut != null) ? true : false;
            StringIntPair expr_Nbr = null;
            final String idChild = iddChild.getRoot().getID();
            if (!applyComplementHeuristics)
			{
                expr_Nbr = generateStateSetTerm(false, isAutomaton, autVarName, idd.labelOfChild(iddChild));
            }
			else
			{
                expr_Nbr = compelmentHeuristic(autVarName, idd.labelOfChild(iddChild));
            }

            final boolean independentApplicable = applyIndependentHeuristics && isIndependentHeuristicApplicable(autVarName, idd.labelOfChild(iddChild));

            String stateExpr = "";
            if (expr_Nbr.i > 0)
			{
                stateExpr = (expr_Nbr.i == 1) ? expr_Nbr.s : ("(" + expr_Nbr.s + ")");
            }
            inForNbr += expr_Nbr.i;

            String expr = "";
            if (cache.get(idChild) == null) //if 'iddChild' is not visisted
            {
                if (!independentApplicable)
				{
                    final StringIntPair e_n = IDD2expr(iddChild, cache);
                    if (e_n.i >= 0)
					{
                        expr = e_n.s;
                        inForNbr += e_n.i;
                        cache.put(idChild, e_n);
                    }
					else
					{
                        if (Math.abs(e_n.i) > 0)
						{
                            stateExpr = (Math.abs(e_n.i) == 1) ? e_n.s : ("(" + e_n.s + ")");
                        }

                        if (idd.getChildren().size() > 1 || idd.getParents().isEmpty())
						{
                            inForNbr = (-e_n.i);
                        }
						else
						{
                            inForNbr = e_n.i;
                        }
                    }

                }
				else
				{
                    nbrOfIndpHeurs++;
                    if (idd.getChildren().size() == 1)
					{
                        inForNbr = -inForNbr; // keep track of the independet term by keeping the number of terms as a negative number
                    }
                }
            }
			else
			{
                final StringIntPair e_n = cache.get(idChild);
                expr = e_n.s;
                inForNbr += e_n.i;
            }

            if (!expr.isEmpty())
			{
                if (!stateExpr.isEmpty())
				{
                    terms.add("(" + stateExpr + (allowedForbidden ? AND : OR) + expr + ")");
                }
				else
				{
                    terms.add("(" + expr + ")");
                }
            }
			else
			{
                terms.add(stateExpr);
            }

            localNbrOfTerms += inForNbr;

        }

        String expression = "";
        if (!terms.isEmpty())
		{
            expression = terms.get(0);
        }
        for (int i = 1; i < terms.size(); i++)
		{
            expression = "(" + expression + (allowedForbidden ? OR : AND) + terms.get(i) + ")";
        }

        return new StringIntPair(expression, localNbrOfTerms);
    }

    public String BDD2Expr(final BDD bdd)
	{
//        System.out.println(bdd.var()+": "+bdd.hashCode());
        if (bdd.isOne() || bdd.isZero())
		{
            return "no expression";
        }
        if (bdd.low().isOne())
		{
            if (!bdd.high().isZero())
			{
                return "!" + bdd.var() + OR + "(" + BDD2Expr(bdd.high()) + ")";
            }
			else
			{
                return "!" + bdd.var();
            }
        }
		else if (bdd.high().isOne())
		{
            if (!bdd.low().isZero())
			{
                return "" + bdd.var() + OR + "(" + BDD2Expr(bdd.low()) + ")";
            }
			else
			{
                return "" + bdd.var();
            }
        }
		else if (bdd.low().isZero())
		{
            if (!bdd.high().isOne())
			{
                return "" + bdd.var() + AND + BDD2Expr(bdd.high());
            }
			else
			{
                return "" + bdd.var();
            }
        }
		else if (bdd.high().isZero())
		{
            if (!bdd.low().isOne())
			{
                return "!" + bdd.var() + AND + BDD2Expr(bdd.low());
            }
			else
			{
                return "!" + bdd.var();
            }
        }
		else
		{
            return "((" + "" + bdd.var() + AND + BDD2Expr(bdd.high()) + ")" + OR + "(" + "!" + bdd.var() + AND + BDD2Expr(bdd.low()) + "))";
        }
    }

    public int smallestInEq(final ArrayList<Integer> alternatives)
	{
        final int min = Collections.min(alternatives);
        return alternatives.indexOf(min);
    }

    public String seq2inEqual(final ArrayList<String> seq, final String var, final int min, final int max, final boolean flag)
	{
        if (seq.size() > 1)
		{
            final int lastIndex = seq.size() - 1;
            if (Integer.parseInt(seq.get(0)) == min)
			{
                return (var + (flag ? "<=" : ">") + seq.get(lastIndex));
            }
			else if (Integer.parseInt(seq.get(lastIndex)) == max)
			{
                return (var + (flag ? ">=" : "<") + seq.get(0));
            }
			else
			{
                return ("(" + var + (flag ? ">=" : "<") + seq.get(0) + (flag ? AND : OR) + var + (flag ? "<=" : ">") + seq.get(lastIndex) + ")");
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    public ArrayList<String> isIncrementalSeq(final ArrayList<String> values)
	{
        final ArrayList<Integer> vals = new ArrayList<Integer>();
        for (final String v : values)
		{
            vals.add(Integer.parseInt(v));
        }

        //Bubble sort
        boolean swapped = false;
        do
		{
            swapped = false;
            for (int j = 0; j < vals.size() - 1; j++)
			{
                if (vals.get(j) > vals.get(j + 1))
				{
                    //swap values
                    final int temp = vals.get(j + 1);
                    vals.set(j + 1, vals.get(j));
                    vals.set(j, temp);
                    swapped = true;
                }
            }
        } while (swapped);


        ArrayList<Integer> seq = new ArrayList<Integer>();
        ArrayList<Integer> seqTemp = new ArrayList<Integer>();
        seq.add(vals.get(0));
        for (int i = 1; i < vals.size(); i++)
		{
            if (((vals.get(i) - vals.get(i - 1)) == 1))
			{
                seq.add(vals.get(i));
            }
			else
			{
                if (seq.size() > seqTemp.size())
				{
                    seqTemp = (ArrayList<Integer>) seq.clone();
                }

                seq.clear();
                seq.add(vals.get(i));
            }
        }
        if (seqTemp.size() > seq.size())
		{
            seq = (ArrayList<Integer>) seqTemp.clone();
        }

        final ArrayList<String> output = new ArrayList<String>();
        for (final Integer i : seq)
		{
            output.add("" + i);
        }

        return output;
    }

    private void disjunctivelyComputeMustAllowedStates()
	{

        final BDDPartitionAlgoWorker parAlgoWorker = automataBDD.getParAlgoWorker();
        final int eventIndex = automataBDD.getEventIndex(eventName);

        final BDD safeStatesAsTargetStates = safeStatesBDD.replace(automataBDD.getSourceToDestLocationPairing()).replace(automataBDD.getSourceToDestVariablePairing());
        final BDD safeStatesWithEvent = safeStatesAsTargetStates.and(sigmaBDD);

        BDD tmp = manager.getZeroBDD();

        if (parAlgoWorker instanceof BDDPartitionAlgoWorkerEve)
		{

            final BDD eventTransBDD = parAlgoWorker.getCompBDD(eventIndex);
            tmp = safeStatesWithEvent.and(eventTransBDD)
                    .exist(automataBDD.getDestStatesVarSet()).exist(automataBDD.getEventVarSet());

        }
		else if (parAlgoWorker instanceof BDDPartitionAlgoWorkerAut)
		{
            for (final Iterator<ExtendedAutomaton> autItr = automataBDD.getExtendedAutomata().iterator(); autItr.hasNext();)
			{
                final ExtendedAutomaton aut = autItr.next();
                final int autIndex = automataBDD.getIndexMap().getExAutomatonIndex(aut.getName());
                if (automataBDD.getBDDExAutomaton(aut).getCaredEventsIndex().contains(eventIndex))
				{

                    final BDDPartitionSetAut autPartitions = (BDDPartitionSetAut)parAlgoWorker.getPartitions();

                    tmp = tmp.or(safeStatesWithEvent.and(autPartitions.automatonToCompleteTransitionBDDWithEvents.get(autIndex))
                            .exist(automataBDD.getDestStatesVarSet())
                            .exist(automataBDD.getEventVarSet()));
                }
            }

        }

        mustAllowedStatesBDD = tmp.and(safeStatesBDD);
        safeStatesWithEvent.free();
        tmp.free();
    }

    private void disjunctivelyComputeMustForbiddenStates()
	{

        final BDDPartitionAlgoWorker parAlgoWorker = automataBDD.getParAlgoWorker();
        final int eventIndex = automataBDD.getEventIndex(eventName);
        final BDD reachableStatesAsTargetStates = automataBDD.getReachableStates().replace(automataBDD.getSourceToDestLocationPairing()).replace(automataBDD.getSourceToDestVariablePairing());
        final BDD reachableStatesWithEvent = reachableStatesAsTargetStates.and(sigmaBDD);

        BDD tmp = manager.getZeroBDD();

        if (parAlgoWorker instanceof BDDPartitionAlgoWorkerEve)
		{

            final BDD eventTransBDD = parAlgoWorker.getCompBDD(eventIndex);

            tmp = reachableStatesWithEvent.and(eventTransBDD)
                    .exist(automataBDD.getDestStatesVarSet()).exist(automataBDD.getEventVarSet());

        }
		else if (parAlgoWorker instanceof BDDPartitionAlgoWorkerAut)
		{
            for (final Iterator<ExtendedAutomaton> autItr = automataBDD.getExtendedAutomata().iterator(); autItr.hasNext();)
			{
                final ExtendedAutomaton aut = autItr.next();
                final int autIndex = automataBDD.getIndexMap().getExAutomatonIndex(aut.getName());
                if (automataBDD.getBDDExAutomaton(aut).getCaredEventsIndex().contains(eventIndex))
				{
                    final BDDPartitionSetAut autPartitions = (BDDPartitionSetAut)parAlgoWorker.getPartitions();
                    tmp = tmp.or(reachableStatesWithEvent.and(autPartitions.automatonToCompleteTransitionBDDWithEvents.get(autIndex))
                            .exist(automataBDD.getDestStatesVarSet()).exist(automataBDD.getEventVarSet()));
                }
            }
        }

        safeStatesEnablingSigmaBDD = tmp.and(safeStatesBDD);
        mustForbiddenStatesBDD = safeStatesEnablingSigmaBDD.and(mustAllowedStatesBDD.not());
        tmp.free();
        reachableStatesWithEvent.free();
    }

    class StringIntPair
	{

        private final String s;
        private final int i;

        StringIntPair(final String s, final int i)
		{
            this.s = s;
            this.i = i;
        }
    }
}
