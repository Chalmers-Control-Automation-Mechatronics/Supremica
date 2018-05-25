package org.supremica.automata.algorithms.Guard;

import java.io.File;
//###########################################################################
//# Java standard imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//###########################################################################
//# JavaBDD imports
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDVarSet;

//###########################################################################
//# Waters imports
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
  @SuppressWarnings("unused")
  private static Logger logger =
    LogManager.getLogger(BDDExtendedGuardGenerator.class);

  private final ExtendedAutomata theAutomata;
  private final BDDExtendedAutomata automataBDD;
  private final BDDExtendedManager manager;
  private final String eventName;
  private final HashMap<EdgeProxy, String> edge2GuardMap;
  private final Set<ExtendedAutomaton> autGuardVars;

  // all kinds of states and transitions BDDs
  private BDD forwardMonolithicTransitionsBDD;
  private BDD mustAllowedStatesBDD;
  private BDD mustForbiddenStatesBDD;
  private final HashMap<EdgeProxy, BDD> edge2BDDMap;
  private final HashMap<EdgeProxy, BDD> edgesMustAllowedStates;
  private final HashMap<EdgeProxy, BDD> edgesMustForbiddenStates;
  private final HashMap<EdgeProxy, BDD> edgesCareStates;

  // used for avoid computing same guards for edges
  private final HashMap<BDD, HashSet<EdgeProxy>> edgesWithSameForbiddenStates;
  private final HashMap<BDD, HashSet<EdgeProxy>> edgesWithSameAllowedStates;

  private final HashMap<EdgeProxy, String> edge2BestStateSetMap;
  private final HashMap<String, Integer> guard2NbrOfTerms;

  private int nbrOfTerms;

  private final BDD sigmaBDD;
  private BDD statesEnablingSigmaBDD;     // Q^{\sigma}
  private final BDD safeStatesBDD;        // Q_{sup}
  private BDD statesLeading2ForbiddenBDD;
  private BDD safeStatesEnablingSigmaBDD; // Q_{sup}^{\sigma}

  public static final String TRUE = "1";
  public static final String FALSE = "0";

  // formatted logical binary operators
  private final String OR;
  private final String AND;
  private final String EQUAL;
  private final String NEQUAL;

  private boolean allowedForbidden = false;
  private boolean optimalMode = false;
  private boolean applyComplementHeuristics = false;
  private boolean applyIndependentHeuristics = false;
  private boolean generateIDD_PS = false;

  //#########################################################################
  //# Constructor
  public BDDExtendedGuardGenerator(final BDDExtendedAutomata bddAutomata,
                                   final String eventName,
                                   final HashMap<EdgeProxy, BDD> edgeToBDDMap,
                                   final BDD states,
                                   final EditorSynthesizerOptions options)
  {
    theAutomata = bddAutomata.getExtendedAutomata();
    automataBDD = bddAutomata;
    edge2BDDMap = edgeToBDDMap;
    manager = automataBDD.getManager();
    autGuardVars = new HashSet<ExtendedAutomaton>();

    edgesMustAllowedStates = new HashMap<>();
    edgesMustForbiddenStates = new HashMap<>();
    edgesCareStates = new HashMap<>();

    edgesWithSameAllowedStates = new HashMap<>();
    edgesWithSameForbiddenStates = new HashMap<>();

    edge2BestStateSetMap = new HashMap<>();
    edge2GuardMap = new HashMap<>();
    guard2NbrOfTerms = new HashMap<>();

    final CompilerOperatorTable ct = CompilerOperatorTable.getInstance();
    OR     = String.format(" %s ", ct.getOrOperator().getName());
    AND    = String.format(" %s ", ct.getAndOperator().getName());
    EQUAL  = String.format(" %s ", ct.getEqualsOperator().getName());
    NEQUAL = String.format(" %s ", ct.getNotEqualsOperator().getName());

    // options for saving IDD
    automataBDD.setPathRoot(Config.FILE_SAVE_PATH.getAsString() +
                            File.separator);
    generateIDD_PS = options.getSaveIDDInFile();

    switch (options.getExpressionType()) {
    case FORBIDDEN:
      allowedForbidden = false;
      break;
    case ALLOWED:
      allowedForbidden = true;
      break;
    case ADAPTIVE:
      optimalMode = true;
      break;
    }

    this.eventName = eventName;
    final int currEventIndex = automataBDD.getEventIndex(eventName);
    sigmaBDD = manager.createBDD(currEventIndex,
                                 automataBDD.getEventDomain());
    safeStatesBDD = states;

    final SynthesisAlgorithm synAlgo = bddAutomata.getSynthAlg();
    if (synAlgo == SynthesisAlgorithm.MONOLITHICBDD) {

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
      computeEdgeMustAllowedStates();
      computeEdgesMustForbiddenStates();
      computeEdgesCareStates();

    } else if (synAlgo == SynthesisAlgorithm.PARTITIONBDD) {
      disjunctivelyComputeMustAllowedStates();
      disjunctivelyComputeMustForbiddenStates();
    } else {
      assert false;  // never happens
    }

    applyComplementHeuristics = options.getCompHeuristic();
    applyIndependentHeuristics = options.getIndpHeuristic();

    if (optimalMode) {
      for (final EdgeProxy edge: edge2BDDMap.keySet()) {
        if (edge2GuardMap.containsKey(edge))
          continue;
        // first get must allowed guard for edge
        allowedForbidden = true;
        final BDD mustAllowedStates = edgesMustAllowedStates.get(edge);
        final String allowedGuard = generateGuard(edge, mustAllowedStates);
        final int minNbrOfTerms = nbrOfTerms;
        // then get forbidden guards for edge
        allowedForbidden = false;
        final BDD mustForbiddenStates = edgesMustForbiddenStates.get(edge);
        final String forbiddenGuard = generateGuard(edge, mustForbiddenStates);
        // pick the guard having the smaller number of terms
        if (nbrOfTerms <= minNbrOfTerms) {
          edge2BestStateSetMap.put(edge, "FORBIDDEN");
          edge2GuardMap.put(edge, forbiddenGuard);
          guard2NbrOfTerms.put(forbiddenGuard, nbrOfTerms);
          // assign guard to all edges with same forbidden states
          for (final EdgeProxy otherEdge:
            edgesWithSameForbiddenStates.get(mustForbiddenStates)) {
            edge2BestStateSetMap.put(otherEdge, "FORBIDDEN");
            edge2GuardMap.put(otherEdge, forbiddenGuard);
          }
        } else {
          edge2BestStateSetMap.put(edge, "ALLOWED");
          nbrOfTerms = minNbrOfTerms;
          edge2GuardMap.put(edge, allowedGuard);
          guard2NbrOfTerms.put(allowedGuard, nbrOfTerms);
          // assign guard to all edges with same forbidden states
          for (final EdgeProxy otherEdge:
            edgesWithSameAllowedStates.get(mustAllowedStates)) {
            edge2BestStateSetMap.put(otherEdge, "ALLOWED");
            edge2GuardMap.put(otherEdge, allowedGuard);
          }
        }
      }
    } else {
      if (allowedForbidden) {
        for (final EdgeProxy edge: edge2BDDMap.keySet()) {
          if (edge2GuardMap.containsKey(edge))
            continue;
          final BDD mustAllowedStates = edgesMustAllowedStates.get(edge);
          final String allowedGuard = generateGuard(edge, mustAllowedStates);
          edge2BestStateSetMap.put(edge, "ALLOWED");
          edge2GuardMap.put(edge, allowedGuard);
          guard2NbrOfTerms.put(allowedGuard, nbrOfTerms);
         // assign guard to all edges with same allowed states
          for (final EdgeProxy otherEdge:
            edgesWithSameAllowedStates.get(mustAllowedStates)) {
            edge2BestStateSetMap.put(otherEdge, "ALLOWED");
            edge2GuardMap.put(otherEdge, allowedGuard);
          }
        }
      } else {
        for (final EdgeProxy edge: edge2BDDMap.keySet()) {
          final BDD mustForbiddenStates = edgesMustForbiddenStates.get(edge);
          final String forbiddenGuard =
            generateGuard(edge, mustForbiddenStates);
          edge2BestStateSetMap.put(edge, "FORBIDDEN");
          edge2GuardMap.put(edge, forbiddenGuard);
          guard2NbrOfTerms.put(forbiddenGuard, nbrOfTerms);
          // assign guard to all edges with same forbidden states
          for (final EdgeProxy otherEdge:
            edgesWithSameForbiddenStates.get(mustForbiddenStates)) {
            edge2BestStateSetMap.put(otherEdge, "FORBIDDEN");
            edge2GuardMap.put(otherEdge, forbiddenGuard);
          }
        }
      }
    }

    // remove the unnecessary edge-guard entries
    pruneEdge2GuardMap();

    // generate IDD for debugging purposes
    if (generateIDD_PS) {
      generate_IDDs();
    }
  }

  private void pruneEdge2GuardMap()
  {
    final Map<String, Set<EdgeProxy>> guard2EdgesMap =
      new HashMap<>();
    for (final Map.Entry<EdgeProxy, String> e: edge2GuardMap.entrySet()) {
      if (guard2EdgesMap.containsKey(e.getValue())) {
        guard2EdgesMap.get(e.getValue()).add(e.getKey());
      }
      else {
        final Set<EdgeProxy> edges = new HashSet<>();
        edges.add(e.getKey());
        guard2EdgesMap.put(e.getValue(), edges);
      }
    }

    // for debugging purposes, will be cleaned up after the testing.
//    for (final Map.Entry<String, Set<EdgeProxy>> e:
//         guard2EdgesMap.entrySet()) {
//      final String guard = e.getKey();
//      logger.info("The following edges have the same guard: " + guard);
//      for (final EdgeProxy edge: e.getValue()) {
//        logger.info("<" + edge.getSource().getName() + ", "
//                    + edge.getTarget().getName() + ">");
//      }
//    }

    for (final Map.Entry<String,Set<EdgeProxy>> e:
          guard2EdgesMap.entrySet()) {
      final String guard = e.getKey();
      final ArrayList<EdgeProxy> edgeList = new ArrayList<>(e.getValue());
      // sort based on the sizes of existing guards. If two has the same size,
      // sort alphabetically.
      Collections.sort(edgeList, new Comparator<EdgeProxy>() {
        @Override
        public int compare(final EdgeProxy e1, final EdgeProxy e2)
        {
          int e1GuardSize = 0;
          if (e1.getGuardActionBlock() != null) {
            if (e1.getGuardActionBlock().getGuards() != null &&
                !e1.getGuardActionBlock().getGuards().isEmpty()) {
              final SimpleExpressionProxy existingGuard =
                e1.getGuardActionBlock().getGuards().get(0);
              e1GuardSize = existingGuard.getPlainText().length();
            }
          }
          int e2GuardSize = 0;
          if (e2.getGuardActionBlock() != null) {
            if (e2.getGuardActionBlock().getGuards() != null &&
                !e2.getGuardActionBlock().getGuards().isEmpty()) {
              final SimpleExpressionProxy existingGuard =
                e2.getGuardActionBlock().getGuards().get(0);
              e2GuardSize = existingGuard.getPlainText().length();
            }
          }
          if (e1GuardSize == e2GuardSize) {
            final String aut1 =
              automataBDD.getEdge2ExAutomatonMap().get(e1).getName();
            final String aut2 =
              automataBDD.getEdge2ExAutomatonMap().get(e2).getName();
            return aut1.compareTo(aut2);
          }
          else if (e1GuardSize < e2GuardSize) {
            return -1;
          }
          else {
            return 1;
          }
        }
      });
      final String autName =
        automataBDD.getEdge2ExAutomatonMap().get(edgeList.get(0)).getName();
      //Remove redundant edges from edge2GuardMap...
      for (int i=1; i < edgeList.size(); i++) {
        final EdgeProxy edge = edgeList.get(i);
        final String otherAutName =
          automataBDD.getEdge2ExAutomatonMap().get(edge).getName();
        if (!autName.equals(otherAutName)) {
          edge2GuardMap.remove(edge, guard);
          edge2BestStateSetMap.remove(edge);
        }
      }
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

    final BDD transitionsWithoutSigma = forwardMonolithicTransitionsBDD
      .relprod(sigmaBDD, automataBDD.getEventVarSet());

    statesLeading2ForbiddenBDD =
      (transitionsWithoutSigma.and(forbiddenAndReachableStatesBDD))
        .exist(automataBDD.getDestStatesVarSet());
  }

  //Q^sigma_sup
  private void computeMustAllowedSates()
  {
    mustAllowedStatesBDD =
      safeStatesEnablingSigmaBDD.and(statesLeading2ForbiddenBDD.not());
  }

  private void computeEdgeMustAllowedStates() {
    for (final Map.Entry<EdgeProxy,BDD> entry: edge2BDDMap.entrySet()) {
      final EdgeProxy edge = entry.getKey();
      final BDD edgeBDD = entry.getValue();
      final BDD allowedStates = mustAllowedStatesBDD.and(edgeBDD);
      edgesMustAllowedStates.put(edge, allowedStates);
      if (edgesWithSameAllowedStates.containsKey(allowedStates)) {
        edgesWithSameAllowedStates.get(allowedStates).add(edge);
      } else {
        final HashSet<EdgeProxy> edgeSet = new HashSet<>();
        edgeSet.add(edge);
        edgesWithSameAllowedStates.put(allowedStates, edgeSet);
      }
    }
  }

  //Q^sigma & C(Q^sigma_a) & Q_sup
  private void computeMustForbiddenSates()
  {
    mustForbiddenStatesBDD =
      safeStatesEnablingSigmaBDD.and(mustAllowedStatesBDD.not());
  }

  private void computeEdgesMustForbiddenStates() {
    for (final Map.Entry<EdgeProxy,BDD> entry: edge2BDDMap.entrySet()) {
      final EdgeProxy edge = entry.getKey();
      final BDD edgeBDD = entry.getValue();
      final BDD forbiddenStates = mustForbiddenStatesBDD.and(edgeBDD);
      edgesMustForbiddenStates.put(edge, forbiddenStates);
      if (edgesWithSameForbiddenStates.containsKey(forbiddenStates)) {
        edgesWithSameForbiddenStates.get(forbiddenStates).add(edge);
      } else {
        final HashSet<EdgeProxy> edgeSet = new HashSet<>();
        edgeSet.add(edge);
        edgesWithSameForbiddenStates.put(forbiddenStates, edgeSet);
      }
    }
  }

  private void computeEdgesCareStates() {
    for (final Map.Entry<EdgeProxy,BDD> entry: edge2BDDMap.entrySet()) {
      final EdgeProxy e = entry.getKey();
      final BDD careStates =
        edgesMustAllowedStates.get(e).or(edgesMustForbiddenStates.get(e));
      edgesCareStates.put(e, careStates);
    }
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
  public String generateGuard(final EdgeProxy edge, final BDD states)
  {
    nbrOfTerms = 0;
    String localGuard = "";

    final BDD careStatesBDD =
      edgesMustAllowedStates.get(edge).or(edgesMustForbiddenStates.get(edge));

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
  /*
   * Generate IDD files with the purpose of debugging.
   * */
  private void generate_IDDs()
  {
    String fileName = "idd_" + eventName + "_enabled";
    automataBDD.BDD2IDD2PS(statesEnablingSigmaBDD,
                           statesEnablingSigmaBDD,
                           fileName);
    fileName = "iddSafeStates";
    automataBDD.BDD2IDD2PS(safeStatesBDD, safeStatesBDD,
                           fileName);

    fileName = "iddReachableStates";
    automataBDD.BDD2IDD2PS(automataBDD.getReachableStates(),
                           automataBDD.getReachableStates(),
                           fileName);

    fileName = "iddCoreachableStates";
    automataBDD.BDD2IDD2PS(automataBDD.getCoreachableStates(),
                           automataBDD.getCoreachableStates(),
                           fileName);

    fileName = "iddSafe_" + eventName + "_enabled";
    automataBDD.BDD2IDD2PS(safeStatesEnablingSigmaBDD,
                           safeStatesEnablingSigmaBDD,
                           fileName);

    fileName = "idd_" + eventName + "_leadingToForbidden";
    automataBDD.BDD2IDD2PS(statesLeading2ForbiddenBDD,
                           statesLeading2ForbiddenBDD,
                           fileName);
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
  public BDD getStatesEnablingSigma()
  {
    return statesEnablingSigmaBDD;
  }

  public BDD getSatesLeading2ForbiddenBDD()
  {
    return statesLeading2ForbiddenBDD;
  }

  public Set<ExtendedAutomaton> getAutGuardVars()
  {
    return autGuardVars;
  }

  public HashMap<EdgeProxy, String> getEdge2GuardMap()
  {
    return edge2GuardMap;
  }

  public HashMap<String, Integer> getGuard2NbrOfTerms()
  {
    return guard2NbrOfTerms;
  }

  public boolean isGuardTrue(final EdgeProxy edge)
  {
    return edge2GuardMap.get(edge).equals(TRUE);
  }
}
