//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Hierarchical and Decentralized Supervisory Control
//# PACKAGE: org.supremica.automata.algorithms.HDS
//# CLASS:   EFAPartialEvaluator
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms.LBSC;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.queue.TIntQueue;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.efa.simple.*;
import net.sourceforge.waters.analysis.tr.DFSIntSearchSpace;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;

/**
 * A not efficient and hairy, yet working, implementation of partial evaluation
 * for EFAs
 * <p>
 * @author Mohammad Reza Shoaei
 */
public class EFAPartialEvaluator extends DefaultModuleProxyVisitor
{

  /**
   * A utility to partially evaluates given variables. The obtained values at
   * each state will be stored as an attribute.
   * <p/>
   * @param factory
   * @param op
   * @param varContext
   */
  public EFAPartialEvaluator(final ModuleProxyFactory factory,
                              final CompilerOperatorTable op,
                              final SimpleEFAVariableContext varContext)
  {
    mCompVarsMap = null;
    mFactory = factory;
    mOperatorTable = op;
    mVarContext = varContext;
    mPropagator = new ConstraintPropagator(mFactory, mOperatorTable, varContext);
    mVarFinder = new SimpleEFAVariableFinder(mOperatorTable);
    mVarCollector = new SimpleEFAVariableCollector(mOperatorTable, varContext);
    mResiduals = new THashSet<>();
    mHelper = new SimpleEFAHelper(mFactory, mOperatorTable);
    mComparator = new ExpressionComparator(op);
    mTupleList = null;
    mLabelEncoding = null;
    mPrimed = null;
    mUnprimed = null;
    mStateMarking = null;
  }

  /**
   * A utility to partially evaluates given variables. The obtained values at
   * each state will be stored as an attribute.
   * <p/>
   * @param varContext
   */
  public EFAPartialEvaluator(final SimpleEFAVariableContext varContext)
  {
    this(ModuleElementFactory.getInstance(), CompilerOperatorTable.getInstance(),
         varContext);
  }

  /**
   * Initialise the PE with given components.
   * <p/>
   * @param components EFA components
   */
  public void init(final Collection<SimpleEFAComponent> components)
  {
    init(setUp(components));
  }

  /**
   * Initialise the PE with a map of component-variables for evaluation.
   * <p/>
   * @param componentVariablesMap A map with key component and set of variables
   *                              as value
   */
  public void init(
   final THashMap<SimpleEFAComponent, THashSet<SimpleEFAVariable>> componentVariablesMap)
  {
    mResiduals.clear();
    mCompVarsMap = componentVariablesMap;
  }

  /**
   * If enabled, obtained values will be appended to the state names. This can
   * be used for transferring the values.
   * <p/>
   * @param enable
   */
  public void setAppendValueToStateName(final boolean enable)
  {
    mAppendValueToStateName = enable;
  }

  /**
   * Returning the residual components.
   * <p/>
   * @return Set of residual components
   */
  public Collection<SimpleEFAComponent> getResidualComponents()
  {
    return mResiduals;
  }

  /**
   * Returning the set of given variables for evaluation.
   * <p/>
   * @return A set of given variables for evaluation
   */
  public Collection<SimpleEFAVariable> getEvaluatedVariables()
  {
    final THashSet<SimpleEFAVariable> list = new THashSet<>(mCompVarsMap.size());
    for (final THashSet<SimpleEFAVariable> vars : mCompVarsMap.values()) {
      list.addAll(vars);
    }
    return list;
  }

  /**
   * Setting a suffix to be used to name the residual components.
   * <p/>
   * @param suffix
   */
  public void setSuffixName(final String suffix)
  {
    mSuffixName = suffix;
  }

  /**
   * Evaluating given components w.r.t. input variables
   * <p/>
   * @return true if at least one component is evaluated; otherwise false
   * <p>
   * @throws EvalException
   * @throws AnalysisException
   */
  public boolean evaluate() throws EvalException, AnalysisException
  {
    boolean successful = false;
    for (final SimpleEFAComponent component : mCompVarsMap.keySet()) {
      final THashSet<SimpleEFAVariable> PEVars = mCompVarsMap.get(component);
      if (PEVars.isEmpty()) {
        continue;
      }
      evaluate(component, PEVars);
      final SimpleEFAComponent pe = createResidualEFA(component, PEVars);
      if (pe != null) {
        successful = true;
        mResiduals.add(pe);
      }
    }
    return successful;
  }

  private THashMap<SimpleEFAComponent, THashSet<SimpleEFAVariable>>
   setUp(final Collection<SimpleEFAComponent> components)
  {
    final THashMap<SimpleEFAComponent, THashSet<SimpleEFAVariable>> map
     = new THashMap<>(components.size());
    for (final SimpleEFAComponent component : components) {
      final THashSet<SimpleEFAVariable> PEVars = new THashSet<>();
      if (!component.isEFA()) {
        map.put(component, PEVars);
        continue;
      }
      for (final SimpleEFAVariable var : mVarContext.getVariables()) {
        // If the variable is only modified by this component
        // or is local in the system
        if (var.isLocalIn(component)) {
          PEVars.add(var);
        }
      }
      map.put(component, PEVars);

    }
    return map;
  }

  private void evaluate(final SimpleEFAComponent component,
                        final Collection<SimpleEFAVariable> PEVars)
   throws EvalException, AnalysisException
  {
    mTupleList = new LinkedList<>();
    pTR = new ArrayList<>();
    pStatePropMap = new TIntLongHashMap();
    mPrimed = new THashSet<>(component.getVariables().size());
    mUnprimed = new THashSet<>(component.getVariables().size());
    mStateMarking = new ArrayList<>();
    final ConstraintList pMarking = getMarkingConstraints(PEVars);
    mHasMarkingValue = false;
    if (!pMarking.isTrue()) {
      mHasMarkingValue = true;
    }
    final ListBufferTransitionRelation oRel = component.getTransitionRelation();
    oRel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final TransitionIterator oIter = oRel.createSuccessorsReadOnlyIterator();
    final SimpleEFAStateEncoding oStateEncoding = component.getStateEncoding();
    final SimpleEFATransitionLabelEncoding oLabelEncoding
     = component.getTransitionLabelEncoding();
    final TIntObjectHashMap<Object[]> oStateInfo
     = getStateInfoMap(oStateEncoding);
    final int oInitStateId = oStateEncoding.getInitialStateId();
    mLabelEncoding = new SimpleEFATransitionLabelEncoding(oLabelEncoding.size());
    final ConstraintList pInitExp
     = mergeByString(getPrettyExpressions(getInitialExpressions(PEVars)),
                     (ConstraintList) oStateInfo.get(oInitStateId)[0]);
    pInitExp.sort(mComparator);
    final int pInitStateId = createStateId(oInitStateId, pInitExp);
    if (mHasMarkingValue) {
      mStateMarking.add(pInitStateId, !execute(pMarking, pInitExp));
    }
    pStatePropMap.put(pInitStateId, oRel.getAllMarkings(oInitStateId));
    final TIntQueue queue = new DFSIntSearchSpace();
    queue.add(pInitStateId);
    System.err.println("PE Start Analyzing > " + component.getName());
    /////////////////////////////////////////////////////////////////////
    while (!queue.isEmpty()) {
      final int currSourceId = queue.element();
      final ConstraintList currSourceValue = getValue(currSourceId);
      oIter.resetState(getState(currSourceId));
      /**
       * ****************************************************************
       */
      while (oIter.advance()) {
        final int currLabelId = oIter.getCurrentEvent();
        final int currTargetId = oIter.getCurrentTargetState();
        @SuppressWarnings("unchecked") final THashSet<SimpleEFAVariable> currSourceVars
         = new THashSet<>();
        mVarCollector.collectAllVariables(currSourceValue, currSourceVars);
        final ConstraintList currTargetValue
         = (ConstraintList) oStateInfo.get(currTargetId)[0];
        if (!currTargetValue.isTrue()) {
          final THashSet<SimpleEFAVariable> currTargetVars = new THashSet<>();
          mVarCollector.collectAllVariables(currTargetValue, currTargetVars);
          currSourceVars.removeAll(currTargetVars);
        }
        ConstraintList currCondition = oLabelEncoding.getTransitionLabel(
         currLabelId).getConstraint();
        currCondition = getCompleteFormExpressions(currCondition,
                                                   currSourceVars);
        final List<SimpleExpressionProxy> nextValues = new ArrayList<>();
        final List<SimpleExpressionProxy> nextConditions = new ArrayList<>();
        final boolean isSatisfiable = execute(PEVars,
                                              currSourceValue,
                                              currCondition,
                                              nextValues,
                                              nextConditions);
        // If is not satisfiable then continue with the next transition.
        if (!isSatisfiable) {
          continue;
        }
        final ConstraintList nextCon = new ConstraintList(nextConditions);
        if (!nextCon.isTrue()) {
          mVarCollector.collectAllVariables(nextCon, mUnprimed, mPrimed);
        }
        ConstraintList nextValue = new ConstraintList(nextValues);
        if (!currTargetValue.isTrue()) {
          nextValue = mergeByString(nextValue, currTargetValue);
        }
        nextValue.sort(mComparator);
        final SimpleEFAEventDecl nextEvent = oLabelEncoding.getTransitionLabel(
         currLabelId).getEvent();
        final int nextLabelId = mLabelEncoding.createTransitionLabelId(
         new SimpleEFATransitionLabel(nextEvent, nextCon));
        int nextStateId = getStateId(currTargetId, nextValue);
        // if it is a new state
        if (nextStateId < 0) {
          nextStateId = createStateId(currTargetId, nextValue);
          pStatePropMap.put(nextStateId, oRel.getAllMarkings(currTargetId));
          if (mHasMarkingValue) {
            mStateMarking.add(nextStateId, !execute(pMarking, nextValue));
          }
          queue.add(nextStateId);
        }
        final int[] tr = new int[]{currSourceId, nextLabelId, nextStateId};
        // Adding the new transition to the list.
        pTR.add(tr);
      }
    }
  }

  private SimpleEFAComponent createResidualEFA(
   final SimpleEFAComponent component,
   final THashSet<SimpleEFAVariable> PEVars)
   throws OverflowException, AnalysisException
  {
    final String pComponentName = component.getName() + mSuffixName;
    final ListBufferTransitionRelation oRel = component.getTransitionRelation();
    final SimpleEFAStateEncoding pStateEncoding = getStateEncoding(component
     .getStateEncoding());
    //getRemainingVariables(mLabelEncoding, mUnprimed, mPrimed);
    final THashSet<SimpleEFAVariable> pVars = new THashSet<>(mUnprimed);
    pVars.addAll(mPrimed);
    // Creating a transition relation (pRel) based on the list of transitions (pTR).
    final ListBufferTransitionRelation rel
     = createTransitionRelation(pComponentName,
                                component.getKind(),
                                mLabelEncoding.size(),
                                oRel.getNumberOfPropositions(),
                                pStateEncoding.size());
    // Creating a residual EFA.
    final SimpleEFAComponent residual = new SimpleEFAComponent(pComponentName,
                                                               pVars,
                                                               pStateEncoding,
                                                               mLabelEncoding,
                                                               component
     .getBlockedEvents(),
                                                               rel,
                                                               component
     .getKind(),
                                                               null);
    // Registering the remaning variables to this component.
    residual.setStructurallyDeterministic(true);
    // Setting the visitor / modifiers of the variables
    residual.setIsEFA(!pVars.isEmpty());
    residual.setUnprimeVariables(new ArrayList<>(mUnprimed));
    residual.setPrimeVariables(new ArrayList<>(mPrimed));
    final THashSet<SimpleEFAVariable> sVars = new THashSet<>(component
     .getStateVariables());
    sVars.addAll(PEVars);
    residual.setStateVariables(new ArrayList<>(sVars));
    residual.register();

    return residual;
  }

  private ListBufferTransitionRelation createTransitionRelation(
   final String pComponentName, final ComponentKind kind, final int nbrLabels,
   final int nbrPropositions, final int nbrStates)
   throws OverflowException
  {
    final ListBufferTransitionRelation rel
     = new ListBufferTransitionRelation(pComponentName, kind, nbrLabels,
                                        nbrPropositions, nbrStates,
                                        ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    if (!pTR.isEmpty()) {
      rel.setInitial(pTR.get(0)[0], true);
      if (mHasMarkingValue) {
        for (int i = 0; i < pTR.size(); i++) {
          final int source = pTR.get(i)[0];
          final int label = pTR.get(i)[1];
          final int target = pTR.get(i)[2];
          rel.setAllMarkings(source, pStatePropMap.get(source));
          rel.setAllMarkings(target, pStatePropMap.get(target));
          final boolean m1 = mStateMarking.get(source)
           && rel.isMarked(source, SimpleEFAHelper.DEFAULT_MARKING_ID);
          final boolean m2 = mStateMarking.get(target)
           && rel.isMarked(target, SimpleEFAHelper.DEFAULT_MARKING_ID);
          rel.setMarked(source, SimpleEFAHelper.DEFAULT_MARKING_ID, m1);
          rel.setMarked(target, SimpleEFAHelper.DEFAULT_MARKING_ID, m2);
          rel.addTransition(source, label, target);
        }
      } else {
        for (int i = 0; i < pTR.size(); i++) {
          rel.setAllMarkings(pTR.get(i)[0], pStatePropMap.get(pTR.get(i)[0]));
          rel.setAllMarkings(pTR.get(i)[2], pStatePropMap.get(pTR.get(i)[2]));
          rel.addTransition(pTR.get(i)[0], pTR.get(i)[1], pTR.get(i)[2]);
        }
      }
    }
    return rel;
  }

  private SimpleEFAStateEncoding getStateEncoding(
   final SimpleEFAStateEncoding oStateEncoding)
  {
    final SimpleEFAStateEncoding pStateEncoding = new SimpleEFAStateEncoding(
     mTupleList.size());
    for (int id = 0; id < mTupleList.size(); id++) {
      final Object[] item = mTupleList.get(id);
      final SimpleEFAState oState = oStateEncoding.getSimpleState((int) item[0]);
      final SimpleEFAState pState = new SimpleEFAState("S" + id,
                                                       false,
                                                       oState.isMarked(),
                                                       oState.isForbidden(),
                                                       oState.getAttributes());
      final String value = printExpressions((ConstraintList) item[1]);
      pState.setStateValue(value);
      pState.addToAttribute(SimpleEFAHelper.DEFAULT_STATEVALUE_STRING, value);
      if (mHasMarkingValue) {
        pState.setMarked(pState.isMarked() && mStateMarking.get(id));
      }
      if (mAppendValueToStateName) {
        pState.setName(getStateName(oState.getName(), (ConstraintList) item[1]));
      }
      pStateEncoding.createSimpleStateId(pState);
    }
    pStateEncoding.getSimpleState(0).setInitial(true);
    return pStateEncoding;
  }

  private boolean execute(final Collection<SimpleEFAVariable> PEVars,
                          final ConstraintList currValues,
                          final ConstraintList currConditions,
                          final List<SimpleExpressionProxy> nextValues,
                          final List<SimpleExpressionProxy> nextConditions)
   throws EvalException
  {
    mPropagator.init(currValues);
    mPropagator.addConstraints(currConditions);
    mPropagator.propagate();
    if (mPropagator.isUnsatisfiable()) {
      return false;
    }
    final ConstraintList eCons = mPropagator.getAllConstraints();
    final List<SimpleExpressionProxy> eExps = new ArrayList<>(eCons
     .getConstraints());
    final List<SimpleExpressionProxy> oExps = new ArrayList<>(currValues
     .getConstraints());
    // Removing all currValues
    final List<SimpleExpressionProxy> cExps = removeExpressions(eExps, oExps);
    for (final SimpleExpressionProxy eExp : cExps) {
      if (mVarFinder.findPrimeVariables(eExp, PEVars)) {
        final Collection<SimpleEFAVariable> primed = new THashSet<>();
        final Collection<SimpleEFAVariable> unprimed = new THashSet<>();
        mVarCollector.collectAllVariables(eExp, unprimed, primed);
        if (unprimed.isEmpty()) {
          final SimpleExpressionProxy nextValue = getNextValue(eExp);
          nextValues.add(nextValue);
        } else {
          nextConditions.add(eExp);
        }
      } else {
        nextConditions.add(eExp);
      }
    }
    return true;
  }

  private boolean execute(final ConstraintList con1,
                          final ConstraintList con2)
   throws EvalException
  {
    mPropagator.init(con1);
    mPropagator.addConstraints(con2);
    mPropagator.propagate();
    return mPropagator.isUnsatisfiable();
  }

  private TIntObjectHashMap<Object[]> getStateInfoMap(
   final SimpleEFAStateEncoding encode)
  {
    final TIntObjectHashMap<Object[]> map = new TIntObjectHashMap<>(encode
     .size());
    for (int id = 0; id < encode.size(); id++) {
      final SimpleEFAState state = encode.getSimpleState(id);
      final ConstraintList oStateValue
       = new ConstraintList(mHelper.parse(state.getStateValue(),
                                          SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR));
      final THashSet<SimpleEFAVariable> oStateVars = new THashSet<>();
      if (!oStateValue.isTrue()) {
        mVarCollector.collectAllVariables(oStateValue, oStateVars);
      }
      map.put(id, new Object[]{oStateValue, oStateVars});
    }
    return map;
  }

  private ConstraintList getMarkingConstraints(
   final Collection<SimpleEFAVariable> vars)
  {
    final THashSet<SimpleExpressionProxy> varMarkings = new THashSet<>();
    for (final SimpleEFAVariable var : vars) {
      for (final VariableMarkingProxy mexp : var.getVariableMarkings()) {
        varMarkings.add(mexp.getPredicate());
      }
    }
    return new ConstraintList(varMarkings);
  }

  private SimpleExpressionProxy getNextValue(final SimpleExpressionProxy eExp)
   throws EvalException
  {
    try {
      final Object result = eExp.acceptVisitor(this);
      if (result != null) {
        return (SimpleExpressionProxy) result;
      } else {
        throw new EvalException("PE > getNextValue: Not the expected result");
      }
    } catch (final VisitorException ex) {
      throw new EvalException("PE > getNextValue: " + ex);
    }
  }

  private ConstraintList getPrettyExpressions(
   final ConstraintList constraint) throws EvalException
  {
    mPropagator.init(constraint);
    mPropagator.propagate();
    return mPropagator.getAllConstraints();
  }

  private ConstraintList getInitialExpressions(
   final Collection<SimpleEFAVariable> vars)
  {
    final List<SimpleExpressionProxy> inits = new ArrayList<>(vars.size());
    for (final SimpleEFAVariable var : vars) {
      final SimpleExpressionProxy exp = var.getInitialStatePredicate();
      inits.add(exp);
    }
    return new ConstraintList(inits);
  }

  private SimpleExpressionProxy getKeepCurrentValue(
   final UnaryExpressionProxy lhs)
  {
    return mFactory.createBinaryExpressionProxy(mOperatorTable
     .getEqualsOperator(), lhs, lhs.getSubTerm());
  }

  private String getStateName(final String oStateName, final ConstraintList list)
  {
    final List<SimpleExpressionProxy> nList = new ArrayList<>();
    for (final SimpleExpressionProxy exp : list.getConstraints()) {
      if (!oStateName.contains(exp.toString())) {
        nList.add(exp);
      }
    }
    if (nList.size() < 1) {
      return oStateName;
    }
    return oStateName + printExpressions(new ConstraintList(nList),
                                         SimpleEFAHelper.DEFAULT_VALUE_OPENING,
                                         SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR,
                                         SimpleEFAHelper.DEFAULT_VALUE_CLOSING);
  }

  private ConstraintList getCompleteFormExpressions(
   final ConstraintList contraints,
   final Collection<SimpleEFAVariable> localVars)
  {
    final List<SimpleExpressionProxy> exps = new ArrayList<>(contraints
     .getConstraints());
    for (final SimpleEFAVariable var : localVars) {
      if (!mVarFinder.findPrimeVariable(contraints, var)) {
        final SimpleExpressionProxy exp = getKeepCurrentValue(var
         .getPrimedVariableName());
        exps.add(exp);
      }
    }
    return new ConstraintList(exps);
  }

  private List<SimpleExpressionProxy> removeExpressions(
   final List<SimpleExpressionProxy> list,
   final List<SimpleExpressionProxy> exps)
   throws EvalException
  {
    final List<SimpleExpressionProxy> result = new ArrayList<>(list.size());
    final List<SimpleExpressionProxy> pair = new ArrayList<>(exps.size() + 1);
    pair.addAll(exps);
    for (final SimpleExpressionProxy item : list) {
      pair.add(item);
      mPropagator.init(new ConstraintList(pair));
      mPropagator.propagate();
      final ConstraintList prop = mPropagator.getAllConstraints();
      if (prop.size() > exps.size()) {
        result.add(item);
      }
      pair.remove(item);
    }
    return result;
  }

  private int getStateId(final int state, final ConstraintList value)
  {
    outter:
    for (int index = 0; index < mTupleList.size(); index++) {
      final Object[] tuple = mTupleList.get(index);
      if ((int) tuple[0] == state) {
        final ConstraintList v = (ConstraintList) tuple[1];
        // Since expressions are sorted
        if (v.toString().equals(value.toString())) {
          return index;
        }
      }
    }
    return -1;
  }

  private int createStateId(final int state, final ConstraintList value)
  {
    final int id = mTupleList.size();
    mTupleList.add(new Object[]{state, value});
    return id;
  }

  private ConstraintList getValue(final int id)
  {
    return (ConstraintList) mTupleList.get(id)[1];
  }

  private int getState(final int id)
  {
    return (int) mTupleList.get(id)[0];
  }

  /*
   private void getRemainingVariables(
   final SimpleEFATransitionLabelEncoding labelEncoding,
   final Collection<SimpleEFAVariable> unprimed,
   final Collection<SimpleEFAVariable> primed)
   {
   final List<SimpleExpressionProxy> list = new ArrayList<>();
   for (final SimpleEFATransitionLabel label :
   labelEncoding.getTransitionLabelsIncludingTau()) {
   list.addAll(label.getConstraint().getConstraints());
   }
   final ConstraintList constraints = new ConstraintList(list);
   mVarCollector.collectAllVariables(constraints, unprimed, primed);
   }
   */
  private ConstraintList mergeByString(
   final ConstraintList con1, final ConstraintList con2)
  {
    if (con2 == null) {
      return con1;
    }
    if (con1.isTrue()) {
      return con2;
    }
    if (con2.isTrue()) {
      return con1;
    }
    final THashSet<SimpleExpressionProxy> sList = new THashSet<>(con1
     .getConstraints());
    sList.addAll(con2.getConstraints());
    final THashMap<String, SimpleExpressionProxy> map = new THashMap<>();
    for (final SimpleExpressionProxy exp : sList) {
      map.put(exp.toString(), exp);
    }
    sList.clear();
    sList.addAll(map.values());
    return new ConstraintList(sList);
  }

  private String printExpressions(final ConstraintList constraints)
  {
    return printExpressions(constraints,
                            "",
                            SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR,
                            "");
  }

  private String printExpressions(final ConstraintList constraints,
                                  final String opening,
                                  final String separator,
                                  final String closing)
  {
    final StringBuilder result = new StringBuilder();
    if (constraints.isTrue()) {
      return "";
    }
    result.append(opening);
    for (final SimpleExpressionProxy exp : constraints.getConstraints()) {
      result.append(exp.toString());
      result.append(separator);
    }
    result.delete(result.length() - separator.length(), result.length());
    result.append(closing);
    return result.toString();
  }

  //#########################################################################
  @Override
  public Object visitIdentifierProxy(final IdentifierProxy ident)
  {
    return null;
  }

  @Override
  public BinaryExpressionProxy visitBinaryExpressionProxy(
   final BinaryExpressionProxy expr)
   throws VisitorException
  {
    final SimpleExpressionProxy lhs = expr.getLeft();
    final SimpleExpressionProxy nlhs = (SimpleExpressionProxy) lhs
     .acceptVisitor(this);
    return mFactory.createBinaryExpressionProxy(expr.getOperator(),
                                                nlhs,
                                                expr.getRight());
  }

  @Override
  public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
  {
    return expr;
  }

  @Override
  public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
   throws VisitorException
  {
    final SimpleExpressionProxy subterm = expr.getSubTerm();
    final UnaryOperator operator = expr.getOperator();
    if (operator == mOperatorTable.getNextOperator()) {
      return subterm;
    }
    final SimpleExpressionProxy result = (SimpleExpressionProxy) subterm
     .acceptVisitor(this);
    return mFactory.createUnaryExpressionProxy(operator, result);
  }

  //#########################################################################
  //# Data Members
  private final ConstraintPropagator mPropagator;
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleEFAVariableFinder mVarFinder;
  private final SimpleEFAVariableCollector mVarCollector;
  private THashMap<SimpleEFAComponent, THashSet<SimpleEFAVariable>> mCompVarsMap;
  private LinkedList<Object[]> mTupleList;
  private final SimpleEFAVariableContext mVarContext;
  private final Collection<SimpleEFAComponent> mResiduals;
  private String mSuffixName = ".PE";
  private final SimpleEFAHelper mHelper;
  private boolean mAppendValueToStateName = false;
  private ArrayList<int[]> pTR = null;
  private SimpleEFATransitionLabelEncoding mLabelEncoding;
  private TIntLongHashMap pStatePropMap;
  private Collection<SimpleEFAVariable> mUnprimed;
  private Collection<SimpleEFAVariable> mPrimed;
  private ArrayList<Boolean> mStateMarking;
  private boolean mHasMarkingValue;
  private final ExpressionComparator mComparator;
}
