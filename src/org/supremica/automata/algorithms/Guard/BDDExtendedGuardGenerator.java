package org.supremica.automata.algorithms.Guard;

//###########################################################################
//# Java standard imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//###########################################################################
//# JavaBDD imports
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDVarSet;

//###########################################################################
//# Waters imports
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;

//###########################################################################
//# Supremica imports
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
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
import org.supremica.properties.Config;

public final class BDDExtendedGuardGenerator {

  //#########################################################################
  //# Data Members
  private final ExtendedAutomata theAutomata;
  private final BDDExtendedAutomata automataBDD;
  private final BDDExtendedManager manager;
  private final String eventName;
  private final Set<ExtendedAutomaton> autGuardVars;

  // all kinds of states and transitions BDDs
  private BDD forwardMonolithicTransitionsBDD;
  private BDD mustAllowedStatesBDD;
  private BDD mustForbiddenStatesBDD;
  private BDD dontCareStatesBDD;
  private final BDD sigmaBDD;
  private BDD statesEnablingSigmaBDD;     // Q^{\sigma}
  private final BDD safeStatesBDD;        // Q_{sup}
  private BDD careStatesBDD;
  private BDD statesLeading2ForbiddenBDD;
  private BDD safeStatesEnablingSigmaBDD; // Q_{sup}^{\sigma}

  private String guard = "";
  public static final String TRUE = "1";
  public static final String FALSE = "0";

  // formatted logical binary operators
  private final String OR;
  private final String AND;
  private final String EQUAL;
  private final String NEQUAL;

  // statistics and options
  private int bddSize;
  private int nbrOfTerms;
  private int nbrOfCompHeurs = 0;
  private int nbrOfIndpHeurs = 0;
  private boolean allowedForbidden = false;
  private boolean optimalMode = false;
  private boolean applyComplementHeuristics = false;
  private boolean applyIndependentHeuristics = false;
  private boolean generateIDD_PS = false;
  private String bestStateSet = "";
  private boolean isEventBlocked = false;

  //#########################################################################
  //# Constructor
  public BDDExtendedGuardGenerator(final BDDExtendedAutomata bddAutomata,
                                   final String eventName, final BDD states,
                                   final EditorSynthesizerOptions options)
  {
    theAutomata = bddAutomata.getExtendedAutomata();
    automataBDD = bddAutomata;
    manager = automataBDD.getManager();
    autGuardVars = new HashSet<ExtendedAutomaton>();

    final CompilerOperatorTable ct = CompilerOperatorTable.getInstance();
    OR     = String.format(" %s ", ct.getOrOperator().getName());
    AND    = String.format(" %s ", ct.getAndOperator().getName());
    EQUAL  = String.format(" %s ", ct.getEqualsOperator().getName());
    NEQUAL = String.format(" %s ", ct.getNotEqualsOperator().getName());

    // options for saving IDD
    automataBDD.setPathRoot(Config.FILE_SAVE_PATH.getAsString() + "/");
    generateIDD_PS = options.getSaveIDDInFile();

    switch (options.getExpressionType()) {
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
    sigmaBDD = manager.createBDD(currEventIndex,
                                 automataBDD.getEventDomain());
    safeStatesBDD = states;

    final SynthesisAlgorithm synAlgo = bddAutomata.getSynthAlg();
    if (synAlgo.equals(SynthesisAlgorithm.MONOLITHICBDD)) {

      final BDDMonolithicEdges bddTransitions =
        (BDDMonolithicEdges) automataBDD.getBDDEdges();
      forwardMonolithicTransitionsBDD =
        bddTransitions.getMonolithicEdgesForwardWithEventsBDD();

      // compute the states enabling event
      final BDDVarSet quantifiedVars =
        automataBDD.getDestStatesVarSet().union(automataBDD.getEventVarSet());

      statesEnablingSigmaBDD =
        forwardMonolithicTransitionsBDD.relprod(sigmaBDD, quantifiedVars);

      safeStatesEnablingSigmaBDD = safeStatesBDD.and(statesEnablingSigmaBDD);

      computeStatesLeading2ForbiddenStates();
      computeMustAllowedSates();
      computeMustForbiddenSates();

    } else if (synAlgo.equals(SynthesisAlgorithm.PARTITIONBDD)) {
      disjunctivelyComputeMustAllowedStates();
      disjunctivelyComputeMustForbiddenStates();
    } else {
      assert false;  // never happens
    }

    computeCareStates();

    applyComplementHeuristics = options.getCompHeuristic();
    applyIndependentHeuristics = options.getIndpHeuristic();

    if (optimalMode) {
      allowedForbidden = true;

      final String allowedGuard = generateGuard(mustAllowedStatesBDD);
      final int minNbrOfTerms = nbrOfTerms;
      final int nbrOfCompHeuris = this.nbrOfCompHeurs;
      final int nbrOfIndpHeuris = this.nbrOfIndpHeurs;

      allowedForbidden = false;
      final String forbiddenGuard = generateGuard(mustForbiddenStatesBDD);
      if (nbrOfTerms < minNbrOfTerms) {
        guard = forbiddenGuard;
        bestStateSet = "FORBIDDEN";
      } else {
        guard = allowedGuard;
        nbrOfTerms = minNbrOfTerms;
        this.nbrOfCompHeurs = nbrOfCompHeuris;
        this.nbrOfIndpHeurs = nbrOfIndpHeuris;
        bestStateSet = "ALLOWED";
      }
    } else {
      if (allowedForbidden) {
        guard = generateGuard(mustAllowedStatesBDD);
        bestStateSet = "ALLOWED";
      } else {
        guard = generateGuard(mustForbiddenStatesBDD);
        bestStateSet = "FORBIDDEN";
      }
    }
    // event is blocked in the synchronization process
    if (mustAllowedStatesBDD.isZero() && mustForbiddenStatesBDD.isZero()) {
      guard = FALSE;
      isEventBlocked = true;
    }
    // generate IDD
    if (generateIDD_PS) {
      generate_IDDs();
    }
  }

  //#########################################################################
  //# BDD states and transitions manipulations
  private void computeStatesLeading2ForbiddenStates()
  {
    BDD forbiddenAndReachableStatesBDD =
      automataBDD.getReachableStates().and(safeStatesBDD.not());

    forbiddenAndReachableStatesBDD = forbiddenAndReachableStatesBDD
      .replace(automataBDD.getSourceToDestLocationPairing());
    forbiddenAndReachableStatesBDD = forbiddenAndReachableStatesBDD
      .replace(automataBDD.getSourceToDestVariablePairing());

    final BDD transitionsWithSigma = forwardMonolithicTransitionsBDD
      .relprod(sigmaBDD, automataBDD.getEventVarSet());

    statesLeading2ForbiddenBDD =
      (transitionsWithSigma.and(forbiddenAndReachableStatesBDD))
        .exist(automataBDD.getDestStatesVarSet());
  }

  //Q^sigma_sup
  private void computeMustAllowedSates()
  {
    mustAllowedStatesBDD =
      safeStatesEnablingSigmaBDD.and(statesLeading2ForbiddenBDD.not());
  }

  //Q^sigma & C(Q^sigma_a) & Q_sup
  private void computeMustForbiddenSates()
  {
    mustForbiddenStatesBDD =
      safeStatesEnablingSigmaBDD.and(mustAllowedStatesBDD.not());
  }

  private void computeCareStates()
  {
    careStatesBDD = mustAllowedStatesBDD.or(mustForbiddenStatesBDD);
  }

  //Q & C(mustForbiddenStatesBDD) & C(mustAllowedStatesBDD)
  // OR C(careStatesBDD)
  public void computeDontCareStates()
  {
    dontCareStatesBDD = careStatesBDD.not();
  }

  private void disjunctivelyComputeMustAllowedStates()
  {
    final BDDPartitionAlgoWorker parAlgoWorker =
      automataBDD.getParAlgoWorker();
    final int eventIndex = automataBDD.getEventIndex(eventName);

    final BDD safeStatesAsTargetStates =
      safeStatesBDD.replace(automataBDD.getSourceToDestLocationPairing())
        .replace(automataBDD.getSourceToDestVariablePairing());
    final BDD safeStatesWithEvent = safeStatesAsTargetStates.and(sigmaBDD);

    BDD tmp = manager.getZeroBDD();

    if (parAlgoWorker instanceof BDDPartitionAlgoWorkerEve) {

      final BDD eventTransBDD = parAlgoWorker.getCompBDD(eventIndex);
      tmp = safeStatesWithEvent.and(eventTransBDD)
        .exist(automataBDD.getDestStatesVarSet())
        .exist(automataBDD.getEventVarSet());

    } else if (parAlgoWorker instanceof BDDPartitionAlgoWorkerAut) {
      for (final Iterator<ExtendedAutomaton> autItr =
        automataBDD.getExtendedAutomata().iterator(); autItr.hasNext();) {
        final ExtendedAutomaton aut = autItr.next();
        final int autIndex =
          automataBDD.getIndexMap().getExAutomatonIndex(aut.getName());
        if (automataBDD.getBDDExAutomaton(aut).getCaredEventsIndex()
          .contains(eventIndex)) {

          final BDDPartitionSetAut autPartitions =
            (BDDPartitionSetAut) parAlgoWorker.getPartitions();

          tmp = tmp.or(safeStatesWithEvent
            .and(autPartitions.automatonToCompleteTransitionBDDWithEvents
              .get(autIndex))
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

    final BDDPartitionAlgoWorker parAlgoWorker =
      automataBDD.getParAlgoWorker();
    final int eventIndex = automataBDD.getEventIndex(eventName);
    final BDD reachableStatesAsTargetStates = automataBDD.getReachableStates()
      .replace(automataBDD.getSourceToDestLocationPairing())
      .replace(automataBDD.getSourceToDestVariablePairing());
    final BDD reachableStatesWithEvent =
      reachableStatesAsTargetStates.and(sigmaBDD);

    BDD tmp = manager.getZeroBDD();

    if (parAlgoWorker instanceof BDDPartitionAlgoWorkerEve) {

      final BDD eventTransBDD = parAlgoWorker.getCompBDD(eventIndex);

      tmp = reachableStatesWithEvent.and(eventTransBDD)
        .exist(automataBDD.getDestStatesVarSet())
        .exist(automataBDD.getEventVarSet());

    } else if (parAlgoWorker instanceof BDDPartitionAlgoWorkerAut) {
      for (final Iterator<ExtendedAutomaton> autItr =
        automataBDD.getExtendedAutomata().iterator(); autItr.hasNext();) {
        final ExtendedAutomaton aut = autItr.next();
        final int autIndex =
          automataBDD.getIndexMap().getExAutomatonIndex(aut.getName());
        if (automataBDD.getBDDExAutomaton(aut).getCaredEventsIndex()
          .contains(eventIndex)) {
          final BDDPartitionSetAut autPartitions =
            (BDDPartitionSetAut) parAlgoWorker.getPartitions();
          tmp = tmp.or(reachableStatesWithEvent
            .and(autPartitions.automatonToCompleteTransitionBDDWithEvents
              .get(autIndex))
            .exist(automataBDD.getDestStatesVarSet())
            .exist(automataBDD.getEventVarSet()));
        }
      }
    }

    safeStatesEnablingSigmaBDD = tmp.and(safeStatesBDD);
    mustForbiddenStatesBDD =
      safeStatesEnablingSigmaBDD.and(mustAllowedStatesBDD.not());
    tmp.free();
    reachableStatesWithEvent.free();
  }

  //#########################################################################
  //# Guard
  public String generateGuard(final BDD states)
  {
    nbrOfTerms = 0;
    nbrOfCompHeurs = 0;
    nbrOfIndpHeurs = 0;
    String localGuard = "";

    if (states.equals(careStatesBDD)) {
      localGuard = allowedForbidden ? TRUE : FALSE;
      nbrOfTerms++;
    } else if (states.satCount(automataBDD.getSourceStatesVarSet()) == 0) {
      localGuard = allowedForbidden ? FALSE : TRUE;
      nbrOfTerms++;
    } else {
      BDD goodBDD = states.simplify(careStatesBDD);
      if (states.nodeCount() <= goodBDD.nodeCount()) {
        goodBDD = states;
      }
      if (goodBDD.nodeCount() > 0) {

        final IDD goodIDD =
          automataBDD.generateIDD(goodBDD, safeStatesEnablingSigmaBDD);
        String fileName = "idd_" + eventName;
        if (allowedForbidden) {
          fileName += "_allowed";
        } else {
          fileName += "_forbidden";
        }

        if (generateIDD_PS) {
          automataBDD.BDD2IDD2PS(goodBDD, safeStatesEnablingSigmaBDD,
                                 fileName);
        }
        localGuard = generateExpression(goodIDD);
      }
    }
    return localGuard;
  }

  public String generateExpression(final IDD idd)
  {
    String output = "";
    final HashMap<String,StringIntPair> cache =
      new HashMap<String,StringIntPair>();
    final StringIntPair sip = IDD2expr(idd, cache);
    output = sip.s;
    nbrOfTerms = Math.abs(sip.i);

    return output;
  }

  StringIntPair IDD2expr(final IDD idd,
                         final HashMap<String,StringIntPair> cache)
  {
    int localNbrOfTerms = 0;

    if (idd.isOneTerminal()) {
      return new StringIntPair("", 0);
    }

    final ArrayList<String> terms = new ArrayList<String>();

    for (final IDD iddChild : idd.getChildren()) {
      int inForNbr = 0;
      final String autVarName = idd.getRoot().getName();
      final BDDExtendedAutomaton bddAut =
        automataBDD.getBDDExAutomaton(autVarName);
      final boolean isAutomaton = (bddAut != null) ? true : false;
      if (isAutomaton && !autGuardVars.contains(bddAut.getExAutomaton()))
        autGuardVars.add(bddAut.getExAutomaton());
      StringIntPair expr_Nbr = null;
      final String idChild = iddChild.getRoot().getID();
      if (!applyComplementHeuristics) {
        expr_Nbr = generateStateSetTerm(false, isAutomaton, autVarName,
                                        idd.labelOfChild(iddChild));
      } else {
        expr_Nbr =
          compelmentHeuristic(autVarName, idd.labelOfChild(iddChild));
      }

      final boolean independentApplicable =
        applyIndependentHeuristics &&
        isIndependentHeuristicApplicable(autVarName,
                                         idd.labelOfChild(iddChild));

      String stateExpr = "";
      if (expr_Nbr.i > 0) {
        stateExpr =
          (expr_Nbr.i == 1) ? expr_Nbr.s : ("(" + expr_Nbr.s + ")");
      }
      inForNbr += expr_Nbr.i;

      String expr = "";
      if (cache.get(idChild) == null) //if 'iddChild' is not visited
      {
        if (!independentApplicable) {
          final StringIntPair e_n = IDD2expr(iddChild, cache);
          if (e_n.i >= 0) {
            expr = e_n.s;
            inForNbr += e_n.i;
            cache.put(idChild, e_n);
          } else {
            if (Math.abs(e_n.i) > 0) {
              stateExpr =
                (Math.abs(e_n.i) == 1) ? e_n.s : ("(" + e_n.s + ")");
            }

            if (idd.getChildren().size() > 1 || idd.getParents().isEmpty()) {
              inForNbr = (-e_n.i);
            } else {
              inForNbr = e_n.i;
            }
          }

        } else {
          nbrOfIndpHeurs++;
          if (idd.getChildren().size() == 1) {
            // keep track of the independent term by keeping the number
            // of terms as a negative number
            inForNbr = -inForNbr;
          }
        }
      } else {
        final StringIntPair e_n = cache.get(idChild);
        expr = e_n.s;
        inForNbr += e_n.i;
      }

      if (!expr.isEmpty()) {
        if (!stateExpr.isEmpty()) {
          terms.add("(" + stateExpr + (allowedForbidden ? AND : OR) + expr
                    + ")");
        } else {
          terms.add("(" + expr + ")");
        }
      } else {
        terms.add(stateExpr);
      }
      localNbrOfTerms += inForNbr;
    }

    String expression = "";
    if (!terms.isEmpty()) {
      expression = terms.get(0);
    }
    for (int i = 1; i < terms.size(); i++) {
      expression =
        "(" + expression + (allowedForbidden ? OR : AND) + terms.get(i) + ")";
    }

    return new StringIntPair(expression, localNbrOfTerms);
  }

  StringIntPair generateStateSetTerm(final boolean isComp,
                                     final boolean isAutomaton,
                                     String variable, ArrayList<String> set)
  {
    variable = variable.replaceAll(" ", "");
    int localNbrOfTerms = 0;
    String expr = "";
    final String symbol = automataBDD.getLocVarSuffix();
    final boolean flag = allowedForbidden ^ isComp;

    ArrayList<String> incrementalSeq;
    final ArrayList<String> setTemp = new ArrayList<String>(set);
    String inEq;
    if (!isAutomaton) {
      boolean firstTime = true;
      do {
        incrementalSeq = isIncrementalSeq(setTemp);
        setTemp.removeAll(incrementalSeq);
        inEq = seq2inEqual(incrementalSeq, variable,
                           theAutomata.getMinValueofVar(variable),
                           theAutomata.getMaxValueofVar(variable), flag);
        if (!inEq.isEmpty()) {
          if (inEq.contains(AND) || inEq.contains(OR)) {
            localNbrOfTerms += 2;
          } else {
            localNbrOfTerms += 1;
          }

          set = new ArrayList<String>(setTemp);
          if (firstTime) {
            expr = inEq;
            firstTime = false;
          } else {
            expr += ((flag ? OR : AND) + inEq);
          }
        }

      } while (!inEq.isEmpty() && !setTemp.isEmpty());

      if (!expr.isEmpty()) {
        expr = "(" + expr + ")";
      }
    }

    if (!set.isEmpty()) {
      final String ex =
        variable + (isAutomaton ? symbol : "") + (flag ? EQUAL : NEQUAL)
                        + set.get(0).replaceAll(" ", "");
      if (expr.isEmpty()) {
        expr = ex;
      } else {
        expr += ((flag ? OR : AND) + ex);
      }
      localNbrOfTerms++;
    }
    for (int i = 1; i < set.size(); i++) {
      expr += ((flag ? OR : AND) + variable + (isAutomaton ? symbol : "")
               + (flag ? EQUAL : NEQUAL) + set.get(i).replaceAll(" ", ""));
      localNbrOfTerms++;
    }

    return new StringIntPair(expr, localNbrOfTerms);
  }

  public boolean isIndependentHeuristicApplicable(final String autVarName,
                                                  final ArrayList<String> stateSet)
  {
    final BDDExtendedAutomaton bddAut =
      automataBDD.getBDDExAutomaton(autVarName);
    final boolean isAutomaton = (bddAut != null) ? true : false;
    final ArrayList<String> indpStates = new ArrayList<String>();
    for (final String stateName : stateSet) {
      BDD stateBDD = null;
      if (isAutomaton) {
        final ExtendedAutomaton exAut = bddAut.getExAutomaton();
        final int stateIndex = automataBDD
          .getLocationIndex(exAut, exAut.getLocationWithName(stateName));
        stateBDD = manager.getFactory().buildCube(stateIndex, automataBDD
          .getSourceLocationDomain(exAut.getName()).vars());
      } else {
        stateBDD =
          automataBDD.getConstantBDD(autVarName, Integer.parseInt(stateName));
      }

      if ((allowedForbidden ? mustForbiddenStatesBDD : mustAllowedStatesBDD)
        .and(stateBDD).nodeCount() == 0) {
        indpStates.add(stateName);
        //                    indpStates.add((symbol+autVarName.replaceAll(" ", "")+(allowedForbidden?EQUAL:NEQUAL)+stateName.replaceAll(" ", "")));
      }
    }

    return indpStates.equals(stateSet);
  }

  StringIntPair compelmentHeuristic(final String autVarName,
                                    final ArrayList<String> stateSet)
  {
    int localNbrOfTerms = 0;
    String expr = "";
    final BDDExtendedAutomaton bddAut =
      automataBDD.getBDDExAutomaton(autVarName);
    final boolean isAutomaton = (bddAut != null) ? true : false;

    ArrayList<String> inputStateSet = new ArrayList<String>(stateSet);
    ArrayList<String> complementStates = new ArrayList<String>();
    complementStates =
      isAutomaton ? bddAut.getComplementLocationNames(stateSet)
        : automataBDD.getComplementValues(autVarName, stateSet);
    final boolean isComp = complementStates.size() < stateSet.size();
    StringIntPair e_n = null;
    if (isComp) {
      nbrOfCompHeurs++;
      inputStateSet = new ArrayList<String>(complementStates);
    }

    e_n =
      generateStateSetTerm(isComp, isAutomaton, autVarName, inputStateSet);

    expr = e_n.s;
    localNbrOfTerms += e_n.i;

    return new StringIntPair(expr, localNbrOfTerms);

  }

  public String BDD2Expr(final BDD bdd)
  {
    //        System.out.println(bdd.var()+": "+bdd.hashCode());
    if (bdd.isOne() || bdd.isZero()) {
      return "no expression";
    }
    if (bdd.low().isOne()) {
      if (!bdd.high().isZero()) {
        return "!" + bdd.var() + OR + "(" + BDD2Expr(bdd.high()) + ")";
      } else {
        return "!" + bdd.var();
      }
    } else if (bdd.high().isOne()) {
      if (!bdd.low().isZero()) {
        return "" + bdd.var() + OR + "(" + BDD2Expr(bdd.low()) + ")";
      } else {
        return "" + bdd.var();
      }
    } else if (bdd.low().isZero()) {
      if (!bdd.high().isOne()) {
        return "" + bdd.var() + AND + BDD2Expr(bdd.high());
      } else {
        return "" + bdd.var();
      }
    } else if (bdd.high().isZero()) {
      if (!bdd.low().isOne()) {
        return "!" + bdd.var() + AND + BDD2Expr(bdd.low());
      } else {
        return "!" + bdd.var();
      }
    } else {
      return "((" + "" + bdd.var() + AND + BDD2Expr(bdd.high()) + ")" + OR
             + "(" + "!" + bdd.var() + AND + BDD2Expr(bdd.low()) + "))";
    }
  }

  public int smallestInEq(final ArrayList<Integer> alternatives)
  {
    final int min = Collections.min(alternatives);
    return alternatives.indexOf(min);
  }

  public String seq2inEqual(final ArrayList<String> seq, final String var,
                            final int min, final int max, final boolean flag)
  {
    if (seq.size() > 1) {
      final int lastIndex = seq.size() - 1;
      if (Integer.parseInt(seq.get(0)) == min) {
        return (var + (flag ? "<=" : ">") + seq.get(lastIndex));
      } else if (Integer.parseInt(seq.get(lastIndex)) == max) {
        return (var + (flag ? ">=" : "<") + seq.get(0));
      } else {
        return ("(" + var + (flag ? ">=" : "<") + seq.get(0)
                + (flag ? AND : OR) + var + (flag ? "<=" : ">")
                + seq.get(lastIndex) + ")");
      }
    }
    return "";
  }

  @SuppressWarnings("unchecked")
  public ArrayList<String> isIncrementalSeq(final ArrayList<String> values)
  {
    final ArrayList<Integer> vals = new ArrayList<Integer>();
    for (final String v : values) {
      vals.add(Integer.parseInt(v));
    }

    //Bubble sort
    boolean swapped = false;
    do {
      swapped = false;
      for (int j = 0; j < vals.size() - 1; j++) {
        if (vals.get(j) > vals.get(j + 1)) {
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
    for (int i = 1; i < vals.size(); i++) {
      if (((vals.get(i) - vals.get(i - 1)) == 1)) {
        seq.add(vals.get(i));
      } else {
        if (seq.size() > seqTemp.size()) {
          seqTemp = (ArrayList<Integer>) seq.clone();
        }

        seq.clear();
        seq.add(vals.get(i));
      }
    }
    if (seqTemp.size() > seq.size()) {
      seq = (ArrayList<Integer>) seqTemp.clone();
    }

    final ArrayList<String> output = new ArrayList<String>();
    for (final Integer i : seq) {
      output.add("" + i);
    }

    return output;
  }

  //#########################################################################
  //# IDD
  private void generate_IDDs()
  {
    String fileName = "idd_" + eventName + "_enabled";
    automataBDD.BDD2IDD2PS(statesEnablingSigmaBDD, statesEnablingSigmaBDD,
                           fileName);
    fileName = "iddSafeStates";
    automataBDD.BDD2IDD2PS(safeStatesBDD, safeStatesBDD, fileName);

    fileName = "iddReachableStates";
    automataBDD.BDD2IDD2PS(automataBDD.getReachableStates(),
                           automataBDD.getReachableStates(), fileName);

    fileName = "iddCoreachableStates";
    automataBDD.BDD2IDD2PS(automataBDD.getCoreachableStates(),
                           automataBDD.getCoreachableStates(), fileName);

    fileName = "iddSafe_" + eventName + "_enabled";
    automataBDD.BDD2IDD2PS(safeStatesEnablingSigmaBDD, safeStatesEnablingSigmaBDD, fileName);

    fileName = "idd_" + eventName + "_leadingToForbidden";
    automataBDD.BDD2IDD2PS(statesLeading2ForbiddenBDD, statesLeading2ForbiddenBDD, fileName);

    fileName = "idd_" + eventName + "_allowed";
    automataBDD.BDD2IDD2PS(mustAllowedStatesBDD, mustAllowedStatesBDD, fileName);

    fileName = "idd_" + eventName + "_forbidden";
    automataBDD.BDD2IDD2PS(mustForbiddenStatesBDD, mustForbiddenStatesBDD, fileName);
  }

  //#########################################################################
  //# Internal class
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

  //#########################################################################
  //# Simple Access
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
    if (guard.equals(TRUE)) {
      return true;
    }
    return false;
  }

  public Set<ExtendedAutomaton> getAutGuardVars()
  {
    return autGuardVars;
  }
}
