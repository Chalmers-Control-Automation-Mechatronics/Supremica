//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Hierarchical and Decentralized Supervisory Control
//# PACKAGE: org.supremica.automata.algorithms.HDS
//# CLASS:   EFAPartialEvaluator
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms.HDS;

import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import net.sourceforge.waters.analysis.efa.EFAHelper;
import net.sourceforge.waters.analysis.efa.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.SimpleEFAEventDecl;
import net.sourceforge.waters.analysis.efa.SimpleEFAState;
import net.sourceforge.waters.analysis.efa.SimpleEFAStateEncoding;
import net.sourceforge.waters.analysis.efa.SimpleEFATransitionLabel;
import net.sourceforge.waters.analysis.efa.SimpleEFATransitionLabelEncoding;
import net.sourceforge.waters.analysis.efa.SimpleEFAVariable;
import net.sourceforge.waters.analysis.efa.SimpleEFAVariableCollector;
import net.sourceforge.waters.analysis.efa.SimpleEFAVariableContext;
import net.sourceforge.waters.analysis.efa.SimpleEFAVariableFinder;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class EFAPartialEvaluator extends DefaultModuleProxyVisitor
{

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
    mHelper = new EFAHelper(mFactory, mOperatorTable);
  }

  public EFAPartialEvaluator(final SimpleEFAVariableContext varContext)
  {
    this(ModuleElementFactory.getInstance(), CompilerOperatorTable.getInstance(),
         varContext);
  }

  public void init(final Collection<SimpleEFAComponent> components)
  {
    mResiduals.clear();
    setUp(components);
  }

  public void init(
   final THashMap<SimpleEFAComponent, THashSet<SimpleEFAVariable>> componentVariablesMap)
  {
    mResiduals.clear();
    mCompVarsMap = componentVariablesMap;
  }

  public void setReadStateNameAsValue(boolean enable)
  {
    mReadStateNameAsValue = enable;
  }

  public void setAppendValueToStateName(boolean enable)
  {
    mAppendValueToStateName = enable;
  }

  public Collection<SimpleEFAComponent> getResidualComponents()
  {
    return mResiduals;
  }

  public Collection<SimpleEFAVariable> getEvaluatedVariables()
  {
    final THashSet<SimpleEFAVariable> list = new THashSet<>(mCompVarsMap.size());
    for (final THashSet<SimpleEFAVariable> vars : mCompVarsMap.values()) {
      list.addAll(vars);
    }
    return list;
  }

  public void evaluate()
   throws EvalException, AnalysisException
  {
    for (final SimpleEFAComponent component : mCompVarsMap.keySet()) {
      final THashSet<SimpleEFAVariable> PEVars = mCompVarsMap.get(component);
      if (PEVars.isEmpty()) {
        continue;
      }
      final SimpleEFAComponent pe = evaluate(component, PEVars);
      if (pe != null) {
        mResiduals.add(pe);
      }
    }
  }

  private void setUp(final Collection<SimpleEFAComponent> components)
  {
    mCompVarsMap = new THashMap<>(components.size());
    for (final SimpleEFAComponent component : components) {
      final THashSet<SimpleEFAVariable> currLocalVars = new THashSet<>();
      if (!component.isEFA()) {
        mCompVarsMap.put(component, currLocalVars);
        continue;
      }
      for (final SimpleEFAVariable var : mVarContext.getVariables()) {
        if (var.isLocalIn(component.getIdentifier())) {
          currLocalVars.add(var);
        }
      }
      mCompVarsMap.put(component, currLocalVars);
    }
  }

  private SimpleEFAComponent evaluate(final SimpleEFAComponent component,
                                      final Collection<SimpleEFAVariable> PEVars)
   throws EvalException, AnalysisException
  {
    try {
      mTupleStateMap = new THashMap<>();
      final String pComponentName = component.getName() + mSuffixName;
      final SimpleEFAStateEncoding oStateEncoding = component.getStateEncoding();
      final TIntObjectHashMap<ConstraintList> oStateValueMap =
       createStateValueMap(oStateEncoding);
      boolean hasStateValue = false;
      if (!oStateValueMap.isEmpty()) {
        hasStateValue = true;
      }
      final ListBufferTransitionRelation oRel =
       component.getTransitionRelation();
      oRel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      int oVarRange = 1;
      for (final SimpleEFAVariable var : PEVars) {
        oVarRange *= var.getRange().size();
      }
      final int oNbrStates = oRel.getNumberOfReachableStates();
      final int pNbrStates = oVarRange * oNbrStates;
      System.err.println("PE Stimated Nbr State: " + pNbrStates);
      final SimpleEFATransitionLabelEncoding oLabelEncoding =
       component.getTransitionLabelEncoding();
      final SimpleEFATransitionLabelEncoding pLabelEncoding =
       new SimpleEFATransitionLabelEncoding(oLabelEncoding.size());
      final SimpleEFAStateEncoding pStateEncoding =
       new SimpleEFAStateEncoding();
      final ArrayList<Integer[]> pTR = new ArrayList<>();
      final TIntLongMap statePropMap = new TIntLongHashMap(oNbrStates);
      int oInitState = -1;
      for (int s = 0; s < oRel.getNumberOfStates(); s++) {
        if (oRel.isReachable(s)) {
          statePropMap.put(s, oRel.getAllMarkings(s));
          if (oRel.isInitial(s)) {
            oInitState = s;
          }
        } else {
          statePropMap.put(s, Long.MAX_VALUE);
        }
      }
      if (oInitState < 0) {
        throw new AnalysisException(
         "PE > evaluate: Initial state cannot be found.");
      }
      final ConstraintList oInitExps = getInitialExpressions(PEVars);
      final SimpleEFAState oState = oStateEncoding.getSimpleState(oInitState);
      ConstraintList pInitExps = getPretty(oInitExps);
      if (hasStateValue) {
        final ConstraintList oValue = oStateValueMap.get(oInitState);
        pInitExps = mergeString(pInitExps, oValue);
      }
      final Tuple initTuple = new Tuple(oInitState, pInitExps);
      final int pInitState = createStateId(initTuple);
      final SimpleEFAState pState = new SimpleEFAState(oState.getSimpleNode());
      if (mAppendValueToStateName) {
        pState.setName(getStateName(oState.getName(), pInitExps));
      } else {
        pState.setName("S" + pInitState);
      }
      pState.mergeToAttribute(DEFAULT_STATEVALUE_STRING,
                              print(pInitExps, "", DEFAULT_VALUE_SEPARATOR, ""),
                              DEFAULT_VALUE_SEPARATOR);
      pState.setMarkings(statePropMap.get(oInitState));
      pStateEncoding.put(pState, pInitState);
      final Stack<Tuple> stack = new Stack<>();
      stack.push(initTuple);
      final TransitionIterator iter = oRel.createSuccessorsReadOnlyIterator();
      System.err.println("PE Start Analyzing");
      while (!stack.isEmpty()) {
        final Tuple currTuple = stack.pop();
        iter.resetState(currTuple.getStateId());
        while (iter.advance()) {
          final int currLabelId = iter.getCurrentEvent();
          final SimpleEFATransitionLabel currLabel =
           oLabelEncoding.getTransitionLabel(currLabelId);
          final ConstraintList currConditions =
           getCompleteFormConstraint(currLabel.getConstraint(), PEVars);
          ConstraintList currValues = currTuple.getConstrains();
          if (hasStateValue) {
            final int oCurrSource = iter.getCurrentSourceState();
            final ConstraintList oValue = oStateValueMap.get(oCurrSource);
            currValues = mergeConstraints(currValues, oValue);
          }
          final List<SimpleExpressionProxy> nextValues = new ArrayList<>();
          final List<SimpleExpressionProxy> nextConditions = new ArrayList<>();
          final boolean isSatisfiable = execute(PEVars,
                                                currValues,
                                                currConditions,
                                                nextValues,
                                                nextConditions);
          if (!isSatisfiable) {
            continue;
          }
          final int oCurrTarget = iter.getCurrentTargetState();
          ConstraintList nextValue = new ConstraintList(nextValues);
          if (hasStateValue) {
            final ConstraintList oValue = oStateValueMap.get(oCurrTarget);
            nextValue = mergeConstraints(nextValue, oValue);
          }
          final Tuple nextTuple = new Tuple(oCurrTarget, nextValue);
          final SimpleEFAEventDecl[] nextEvent = currLabel.getEvents();
          final SimpleEFATransitionLabel pLabel =
           new SimpleEFATransitionLabel(new ConstraintList(nextConditions),
                                        nextEvent);
          final int nextLabelId = pLabelEncoding.createTransitionLabelId(pLabel);
          final int pCurrSource = getStateId(currTuple);
          final int pCurrTarget;
          if (isTupleVisited(nextTuple)) {
            pCurrTarget = getStateId(nextTuple);
          } else {
            pCurrTarget = createStateId(nextTuple);
            final SimpleEFAState oCurrTargetState =
             oStateEncoding.getSimpleState(oCurrTarget);
            final SimpleEFAState pCurrTargetState =
             new SimpleEFAState(oCurrTargetState.getSimpleNode());
            pCurrTargetState.setInitial(false);
            if (mAppendValueToStateName) {
              pCurrTargetState.setName(getStateName(oCurrTargetState.getName(),
                                                    nextValue));
            } else {
              pCurrTargetState.setName("S" + pCurrTarget);
            }

            pCurrTargetState.mergeToAttribute(DEFAULT_STATEVALUE_STRING,
                                              print(nextValue, "",
                                                    DEFAULT_VALUE_SEPARATOR, ""),
                                              DEFAULT_VALUE_SEPARATOR);
            pCurrTargetState.setMarkings(statePropMap.get(oCurrTarget));
            pStateEncoding.put(pCurrTargetState, pCurrTarget);
            stack.push(nextTuple);
          }
          final Integer[] tr = new Integer[]{pCurrSource, nextLabelId,
                                             pCurrTarget};
          pTR.add(tr);
        }
      }
      System.err.println("PE Finish Analyzing");
      final Collection<SimpleEFAVariable> unprimed = new THashSet<>();
      final Collection<SimpleEFAVariable> primed = new THashSet<>();
      getRemainingVariables(pLabelEncoding, unprimed, primed);
      final THashSet<SimpleEFAVariable> pVars = new THashSet<>(unprimed);
      pVars.addAll(primed);
      final ListBufferTransitionRelation pRel =
       createTransitionRelation(pStateEncoding,
                                pTR,
                                pComponentName,
                                oRel.getKind(),
                                pLabelEncoding.size(),
                                oRel.getNumberOfPropositions(),
                                pStateEncoding.size());
      System.err.println("PE Finish TR");
      final SimpleEFAComponent residual =
       new SimpleEFAComponent(pComponentName,
                              pVars,
                              pStateEncoding,
                              pLabelEncoding,
                              component.getBlockedEvents(),
                              pRel,
                              component.getKind(),
                              null);
      System.err.println("PE Finish EFA");
      residual.register();
      residual.setDeterministic(true);
      registerComponent(residual, unprimed, primed);
      final boolean isEFA = !pVars.isEmpty();
      residual.setIsEFA(isEFA);
      return residual;
    } finally {
      mTupleStateMap = null;
    }
  }

  private ListBufferTransitionRelation createTransitionRelation(
   final SimpleEFAStateEncoding pStateEncoding,
   final ArrayList<Integer[]> pTR,
   final String pComponentName,
   final ComponentKind kind,
   final int nbrLabels,
   final int nbrPropositions,
   final int nbrStates)
   throws OverflowException
  {
    final ListBufferTransitionRelation pRel =
     new ListBufferTransitionRelation(pComponentName, kind, nbrLabels,
                                      nbrPropositions, nbrStates,
                                      ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    for (int i = 0; i < pTR.size(); i++) {
      final Integer[] tr = pTR.get(i);
      final int from = tr[0];
      final int label = tr[1];
      final int to = tr[2];
      if (i == 0) {
        pRel.setInitial(from, true);
      }
      pRel.setAllMarkings(from, pStateEncoding.getSimpleState(from)
       .getMarkings());
      pRel.setAllMarkings(to, pStateEncoding.getSimpleState(to).getMarkings());
      pRel.addTransition(from, label, to);
    }
    return pRel;
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
    final List<SimpleExpressionProxy> eExps =
     new ArrayList<>(eCons.getConstraints());
    final List<SimpleExpressionProxy> oExps =
     new ArrayList<>(currValues.getConstraints());
    // Removing all currValues
    final List<SimpleExpressionProxy> cExps =
     removeExpressions(eExps, oExps);
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

  private ConstraintList getPretty(final ConstraintList constraint)
   throws EvalException
  {
    mPropagator.init(constraint);
    mPropagator.propagate();
    return mPropagator.getAllConstraints();
  }

  private ConstraintList getInitialExpressions(
   final Collection<SimpleEFAVariable> vars)
  {
    final List<SimpleExpressionProxy> inits =
     new ArrayList<>(vars.size());
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
    return oStateName + print(new ConstraintList(nList),
                              DEFAULT_VALUE_OPENING,
                              DEFAULT_VALUE_SEPARATOR,
                              DEFAULT_VALUE_CLOSING);
  }

  private ConstraintList getCompleteFormConstraint(
   final ConstraintList contraints,
   final Collection<SimpleEFAVariable> localVars)
  {
    final List<SimpleExpressionProxy> exps =
     new ArrayList<>(contraints.getConstraints());
    for (final SimpleEFAVariable var : localVars) {
      if (!mVarFinder.findPrimeVariable(contraints, var)) {
        final SimpleExpressionProxy exp =
         getKeepCurrentValue(var.getPrimedVariableName());
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

  private int getStateId(final Tuple tuple)
   throws AnalysisException
  {
    if (!isTupleVisited(tuple)) {
      throw new AnalysisException(
       "PE > getStateId: map does not contain the tuple "
       + tuple.toString());
    }
    return mTupleStateMap.get(tuple);
  }

  private int createStateId(final Tuple tuple)
   throws AnalysisException
  {
    if (isTupleVisited(tuple)) {
      throw new AnalysisException("PE > createStateId: map contains the tuple "
       + tuple.toString());
    }
    final int stateId = mTupleStateMap.size();
    mTupleStateMap.put(tuple, stateId);
    return stateId;
  }

  private boolean isTupleVisited(final Tuple tuple)
  {
    return mTupleStateMap.containsKey(tuple);
  }

  private void getRemainingVariables(
   final SimpleEFATransitionLabelEncoding labelEncoding,
   final Collection<SimpleEFAVariable> unprimed,
   final Collection<SimpleEFAVariable> primed)
  {
    final List<SimpleExpressionProxy> list = new ArrayList<>();
    for (final SimpleEFATransitionLabel label : labelEncoding.getTransitionLabels()) {
      list.addAll(label.getConstraint().getConstraints());
    }
    final ConstraintList constraints = new ConstraintList(list);
    mVarCollector.collectAllVariables(constraints, unprimed, primed);
  }

  private TIntObjectHashMap<ConstraintList> createStateValueMap(
   final SimpleEFAStateEncoding stateEncoding)
  {
    final TIntObjectHashMap<ConstraintList> map =
     new TIntObjectHashMap<>(stateEncoding.size());
    for (final SimpleEFAState state : stateEncoding.getSimpleStates()) {
      final int stateId = stateEncoding.getStateId(state);
      final ConstraintList stateValue;
      final String value =
       state.getAttribute(DEFAULT_STATEVALUE_STRING);
      if (value != null && !value.trim().isEmpty()) {
        final String[] str = value.split(DEFAULT_VALUE_SEPARATOR);
        final List<SimpleExpressionProxy> exps = mHelper.parse(str);
        stateValue = new ConstraintList(exps);
        map.put(stateId, stateValue);
      } else {
        if (mReadStateNameAsValue) {
          final String str = state.getName();
          if (str.contains(DEFAULT_VALUE_OPENING)
           && str.contains(DEFAULT_VALUE_CLOSING)) {
            final List<SimpleExpressionProxy> exps =
             mHelper.parseString(str,
                                 DEFAULT_VALUE_OPENING,
                                 DEFAULT_VALUE_CLOSING);
            stateValue = new ConstraintList(exps);
            map.put(stateId, stateValue);
          }
        }
      }
    }
    return map;
  }

  private void registerComponent(final SimpleEFAComponent component,
                                 final Collection<SimpleEFAVariable> unprimed,
                                 final Collection<SimpleEFAVariable> primed)
  {
    for (final SimpleEFAVariable var : unprimed) {
      var.addVisitor(component);
    }
    for (final SimpleEFAVariable var : primed) {
      var.addModifier(component);
    }
  }

  private ConstraintList mergeString(final ConstraintList con1, final ConstraintList con2)
  {
    if (con2 == null) {
      return con1;
    }
    final THashSet<String> sList = new THashSet<>();
    for (final SimpleExpressionProxy con : con1.getConstraints()) {
      sList.add(con.toString());
    }
    for (final SimpleExpressionProxy con : con2.getConstraints()) {
      sList.add(con.toString());
    }
    final List<SimpleExpressionProxy> exps =
     mHelper.parse(sList.toArray(new String[0]));
    return new ConstraintList(exps);
  }

  private ConstraintList mergeConstraints(final ConstraintList con1,
                                          final ConstraintList con2)
  {
    if (con2 == null) {
      return con1;
    }
    final List<SimpleExpressionProxy> list = new ArrayList<>(con1.getConstraints());
    list.addAll(con2.getConstraints());
    return new ConstraintList(list);
  }

  public void setSuffixName(final String suffix)
  {
    mSuffixName = suffix;
  }

  private String print(final ConstraintList constraints,
                       final String opening,
                       final String separator,
                       final String closing)
  {
    final StringBuffer result = new StringBuffer();
    if (constraints.getConstraints().isEmpty()) {
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
    final SimpleExpressionProxy nlhs =
     (SimpleExpressionProxy) lhs.acceptVisitor(this);
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
    final SimpleExpressionProxy result =
     (SimpleExpressionProxy) subterm.acceptVisitor(this);
    return mFactory.createUnaryExpressionProxy(operator, result);
  }

  //#########################################################################
  //# Inner Class tuple
  class Tuple
  {

    Tuple(final int state, final ConstraintList constrains)
    {
      mState = state;
      mConstrains = constrains;
    }

    @SuppressWarnings("unused")
    Tuple()
    {
      this(-1, new ConstraintList());
    }

    public int getStateId()
    {
      return mState;
    }

    public ConstraintList getConstrains()
    {
      return mConstrains;
    }

    @Override
    public boolean equals(final Object obj)
    {
      if (obj instanceof Tuple) {
        final Tuple item = (Tuple) obj;
        if (item.getStateId() == mState && item.getConstrains()
         .equals(mConstrains)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 29 * hash + this.mState;
      hash = 29 * hash + Objects.hashCode(this.mConstrains);
      return hash;
    }

    @Override
    public String toString()
    {
      return "(" + mState + "," + mConstrains.toString() + ")";
    }
    //#########################################################################
    //# Data Members
    final private int mState;
    final private ConstraintList mConstrains;
  }
  //#########################################################################
  //# Data Members
  public final static String DEFAULT_STATEVALUE_STRING = "PE:";
  public final static String DEFAULT_VALUE_OPENING = "<";
  public final static String DEFAULT_VALUE_CLOSING = ">";
  public final static String DEFAULT_VALUE_SEPARATOR = ",";
  private final ConstraintPropagator mPropagator;
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleEFAVariableFinder mVarFinder;
  private final SimpleEFAVariableCollector mVarCollector;
  private THashMap<SimpleEFAComponent, THashSet<SimpleEFAVariable>> mCompVarsMap;
  private THashMap<Tuple, Integer> mTupleStateMap;
  private final SimpleEFAVariableContext mVarContext;
  private final Collection<SimpleEFAComponent> mResiduals;
  private String mSuffixName = ".PE";
  private final EFAHelper mHelper;
  private boolean mReadStateNameAsValue = false;
  private boolean mAppendValueToStateName = true;
}
