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

import net.sourceforge.waters.analysis.efa.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.SimpleEFAEventDecl;
import net.sourceforge.waters.analysis.efa.SimpleEFATransitionLabel;
import net.sourceforge.waters.analysis.efa.SimpleEFATransitionLabelEncoding;
import net.sourceforge.waters.analysis.efa.SimpleEFAVariable;
import net.sourceforge.waters.analysis.efa.SimpleEFAVariableCollector;
import net.sourceforge.waters.analysis.efa.SimpleEFAVariableContext;
import net.sourceforge.waters.analysis.efa.SimpleEFAVariableFinder;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
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

  public Collection<SimpleEFAComponent> getResidualComponents()
  {
    return mResiduals;
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
      //normalize();
      mTupleStateMap = new THashMap<>();
      final ListBufferTransitionRelation oRel =
       component.getTransitionRelation();
      oRel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      int oVarRange = 1;
      for (final SimpleEFAVariable var : PEVars) {
        oVarRange *= var.getRange().size();
      }
      final int oNbrStates = oRel.getNumberOfReachableStates();
      final int pNbrStates = oVarRange * oNbrStates;
      final SimpleEFATransitionLabelEncoding oLabelEncoding =
       component.getTransitionLabelEncoding();
      final SimpleEFATransitionLabelEncoding pLabelEncoding =
       new SimpleEFATransitionLabelEncoding(oLabelEncoding.size());

      final TIntObjectHashMap<String> oStateNameEncoding = component
       .getStateNameMap();
      final TIntObjectHashMap<String> pStateNameEncoding =
       new TIntObjectHashMap<>(pNbrStates, 0.6f);

      final ListBufferTransitionRelation pRel =
       new ListBufferTransitionRelation(oRel.getName(),
                                        oRel.getKind(),
                                        pNbrStates,
                                        oRel.getNumberOfPropositions(),
                                        pNbrStates,
                                        ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      for (int i = 0; i < pNbrStates; i++) {
        pRel.setReachable(i, false);
      }
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
      final String oStateName = oStateNameEncoding.get(oInitState);
      final ConstraintList pInitExps = getPretty(oInitExps);
      final Tuple initTuple = new Tuple(oInitState, pInitExps);
      final int pInitState = createStateId(initTuple);
      pStateNameEncoding.put(pInitState, getStateName(oStateName, pInitExps));
      pRel.setReachable(pInitState, true);
      pRel.setInitial(pInitState, true);
      pRel.setAllMarkings(pInitState, statePropMap.get(oInitState));
      final Stack<Tuple> stack = new Stack<>();
      stack.push(initTuple);
      final TransitionIterator iter = oRel.createSuccessorsReadOnlyIterator();
      while (!stack.isEmpty()) {
        final Tuple currTuple = stack.pop();
        iter.resetState(currTuple.getState());
        while (iter.advance()) {
          final int currLabelId = iter.getCurrentEvent();
          final SimpleEFATransitionLabel currLabel =
           oLabelEncoding.getTransitionLabel(currLabelId);
          final ConstraintList currConditions =
           getCompleteFormConstraint(currLabel.getConstraint(), PEVars);
          final ConstraintList currValues = currTuple.getConstrains();
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
          final ConstraintList nextValue = new ConstraintList(nextValues);
          final Tuple nextTuple = new Tuple(oCurrTarget, nextValue);
          final SimpleEFAEventDecl[] nextEvent = currLabel.getEvents();
          final SimpleEFATransitionLabel pLabel =
           new SimpleEFATransitionLabel(new ConstraintList(nextConditions),
                                        nextEvent);
          final int nextLabelId = pLabelEncoding.createTransitionLabelId(pLabel);
          final int pCurrSource = getStateId(currTuple);
          int pCurrTarget;
          if (isTupleVisited(nextTuple)) {
            pCurrTarget = getStateId(nextTuple);
          } else {
            pCurrTarget = createStateId(nextTuple);
            final String currStateName = oStateNameEncoding.get(oCurrTarget);
            pStateNameEncoding.put(pCurrTarget,
                                   getStateName(currStateName, nextValue));
            pRel.setReachable(pCurrTarget, true);
            pRel.setAllMarkings(pCurrTarget, statePropMap.get(oCurrTarget));
            stack.push(nextTuple);
          }
          pRel.addTransition(pCurrSource, nextLabelId, pCurrTarget);
        }
      }
      final Collection<SimpleEFAVariable> unprimed = new THashSet<>();
      final Collection<SimpleEFAVariable> primed = new THashSet<>();
      getRemainingVariables(pLabelEncoding, unprimed, primed);
      final THashSet<SimpleEFAVariable> vars = new THashSet<>(unprimed);
      vars.addAll(primed);
      final SimpleEFAComponent residual =
       new SimpleEFAComponent(component.getName() + "-PE",
                              vars,
                              pStateNameEncoding,
                              pLabelEncoding,
                              component.getBlockedEvents(),
                              pRel,
                              component.getKind(),
                              getStateValueMap(),
                              null);
      residual.register();
      residual.setDeterministic(true);
      registerComponent(residual, unprimed, primed);
      final boolean isEFA = vars.isEmpty() ? false : true;
      residual.setIsEFA(isEFA);
      return residual;
    } catch (EvalException | AnalysisException ex) {
      throw ex;
    } finally {
      mTupleStateMap = null;
    }
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
   throws EvalException
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
    if (list.size() < 1) {
      return oStateName;
    }
    return oStateName + "." + list.toString();
  }

  @SuppressWarnings("unused")
  private void normalize()
  {
    //TODO HDS: DNF of the conditions
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

  private TIntObjectHashMap<ConstraintList> getStateValueMap()
   throws AnalysisException
  {
    final TIntObjectHashMap<ConstraintList> map =
     new TIntObjectHashMap<>(mTupleStateMap.size());
    for (final Tuple tuple : mTupleStateMap.keySet()) {
      final Integer state = mTupleStateMap.get(tuple);
      final ConstraintList values = tuple.getConstrains();
      final ConstraintList put = map.put(state, values);
      if (put != null) {
        throw new AnalysisException(
         "PE > getStateValueMap: Found duplicate state");
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
  private class Tuple
  {

    public Tuple(final int state, final ConstraintList constrains)
    {
      mState = state;
      mConstrains = constrains;
    }

    @SuppressWarnings("unused")
    public Tuple()
    {
      this(-1, new ConstraintList());
    }

    public int getState()
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
        if (item.getState() == mState && item.getConstrains()
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
  private final ConstraintPropagator mPropagator;
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleEFAVariableFinder mVarFinder;
  private final SimpleEFAVariableCollector mVarCollector;
  private THashMap<SimpleEFAComponent, THashSet<SimpleEFAVariable>> mCompVarsMap;
  private THashMap<Tuple, Integer> mTupleStateMap;
  private final SimpleEFAVariableContext mVarContext;
  private Collection<SimpleEFAComponent> mResiduals;
}
