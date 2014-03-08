//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Hierarchical and Decentralized Supervisory Control
//# PACKAGE: org.supremica.automata.algorithms.HDS
//# CLASS:   EFAPartialEvaluator
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms.HDS;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import net.sourceforge.waters.analysis.efa.simple.*;
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
 * A not efficient and hairy, yet working, implementation of partial evaluation
 * for EFA
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
    mStateVars = new THashSet<>();
    mHelper = new SimpleEFAHelper(mFactory, mOperatorTable);
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
    mStateVars.clear();
    mCompVarsMap = componentVariablesMap;
    for (final SimpleEFAComponent comp : mCompVarsMap.keySet()) {
      mStateVars.addAll(comp.getStateVariables());
    }
  }

  /**
   * If enabled, the state names are treated as values.
   * <p/>
   * @param enable
   */
  public void setReadStateNameAsValue(final boolean enable)
  {
    mReadStateNameAsValue = enable;
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

  private THashMap<SimpleEFAComponent, THashSet<SimpleEFAVariable>> setUp(
   final Collection<SimpleEFAComponent> components)
  {
    final THashMap<SimpleEFAComponent, THashSet<SimpleEFAVariable>> map
     = new THashMap<>(components.size());
    for (final SimpleEFAComponent component : components) {
      final THashSet<SimpleEFAVariable> currLocalVars = new THashSet<>();
      if (!component.isEFA()) {
        map.put(component, currLocalVars);
        continue;
      }
      for (final SimpleEFAVariable var : mVarContext.getVariables()) {
        // If the variable is only modified by this component 
        // or is local in the system
        if (var.isOnlyModifiedBy(component) || var.isLocalIn(component)) {
          currLocalVars.add(var);
        }
      }

      map.put(component, currLocalVars);

    }
    return map;
  }

  /**
   * Evaluating given components w.r.t. input variables
   * <p/>
   * @return true if at least one component is evaluated; otherwise false
   * <p>
   * @throws EvalException
   * @throws AnalysisException
   */
  public boolean evaluate()
   throws EvalException, AnalysisException
  {
    boolean successful = false;
    for (final SimpleEFAComponent component : mCompVarsMap.keySet()) {
      final THashSet<SimpleEFAVariable> PEVars = mCompVarsMap.get(component);
      if (PEVars.isEmpty()) {
        continue;
      }
      final SimpleEFAComponent pe = evaluate(component, PEVars);
      if (pe != null) {
        successful = true;
        mResiduals.add(pe);
      }
    }
    return successful;
  }

  private SimpleEFAComponent evaluate(final SimpleEFAComponent component,
                                      final Collection<SimpleEFAVariable> PEVars)
   throws EvalException, AnalysisException
  {
    try {
      // From now on, 'o' stands for original and 'p' for partial
      mTupleStateMap = new THashMap<>();
      // New name.
      final String pComponentName = component.getName() + mSuffixName;
      // Old state encoding.
      final SimpleEFAStateEncoding oStateEncoding = component.getStateEncoding();
      // Creating values either from states attributes or states name
      // (if mReadStateNameAsValue is true).
      final TIntObjectHashMap<ConstraintList> oStateValueMap =
       createStateValueMap(oStateEncoding);
      // Getting transition relation.
      final ListBufferTransitionRelation oRel =
       component.getTransitionRelation();
      oRel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      // Getting label encoding.
      final SimpleEFATransitionLabelEncoding oLabelEncoding =
       component.getTransitionLabelEncoding();
      // Setting up new label encoding.
      final SimpleEFATransitionLabelEncoding pLabelEncoding =
       new SimpleEFATransitionLabelEncoding(oLabelEncoding.size());
      // Setting up new state encoding.
      final SimpleEFAStateEncoding pStateEncoding =
       new SimpleEFAStateEncoding();
      // Setting up a temporary transition relation where each transition is an
      // array of 3 with 0 -> From state, 1 -> Label, and 2 -> To state ids.
      pTR = new ArrayList<>();
      // Setting up a mapping from states to their marking values.
      pStatePropMap = new TIntLongHashMap();
      // Finding initial state.
      int oInitState = -1;
      for (int s = 0; s < oRel.getNumberOfStates(); s++) {
        if (oRel.isInitial(s)) {
          oInitState = s;
        }
      }
      // If no intial state has been found.
      if (oInitState < 0) {
        throw new AnalysisException(
         "PE > evaluate: Initial state cannot be found.");
      }
      // Finding all initial expression, i.e., variables initial expression.
      final ConstraintList oInitExps = getInitialExpressions(PEVars);
      // Getting original state.
      final SimpleEFAState oState = oStateEncoding.getSimpleState(oInitState);
      // Making initial expressions pretty .
      ConstraintList pInitExps = getPrettyExpressions(oInitExps);

      // If there is any value.
      boolean hasStateValue = false;
      // PEVars which are not state vars so on every transition 
      // we must keep current their values, i.e., x'=x
      final THashSet<SimpleEFAVariable> notInStateValue = new THashSet<>(PEVars);
      notInStateValue.removeAll(component.getStateVariables());
      if (!oStateValueMap.isEmpty()) {
        hasStateValue = true;
        final ConstraintList oValue = oStateValueMap.get(oInitState);
        // Merging current expressions with original expressions.
        pInitExps = mergeByString(pInitExps, oValue);
      }
      // Creating a tuple of original state and initial expressions.
      final Tuple initTuple = new Tuple(oInitState, pInitExps);
      // Getting a new state id, here one can use StateEncoding createStateId!
      final int pInitState = createStateId(initTuple);
      // Creating a new state based on original state.
      final SimpleEFAState pState = new SimpleEFAState(oState.getSimpleNode());
      // If we have to append the evaluated values to states name, by default it
      // is true.
      if (mAppendValueToStateName) {
        pState.setName(getStateName(oState.getName(), pInitExps));
      } else {
        pState.setName("S" + pInitState);
      }
      // Merging current expressions to the original expressions as a new attribute.
      pState.mergeToAttribute(SimpleEFAHelper.DEFAULT_STATEVALUE_STRING,
                              printExpressions(pInitExps, "",
                                               SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR,
                                               ""),
                              SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR);
      // Put the original state marking in the map.
      pStatePropMap.put(oInitState, oRel.getAllMarkings(oInitState));
      pStateEncoding.put(pState, pInitState);
      // Creating a stack of tuples.
      final Stack<Tuple> stack = new Stack<>();
      stack.push(initTuple);
      // Iterating over transitions in oRel.
      final TransitionIterator iter = oRel.createSuccessorsReadOnlyIterator();
      System.err.println("PE Start Analyzing > " + component.getName());
      while (!stack.isEmpty()) {
        final Tuple currTuple = stack.pop();
        // Getting all the outgoing transitions of the current state
        iter.resetState(currTuple.getStateId());
        // For every out goinig transitions of the current state do as follows.
        while (iter.advance()) {
          final int currLabelId = iter.getCurrentEvent();
          final SimpleEFATransitionLabel currLabel = oLabelEncoding
           .getTransitionLabel(currLabelId);

          final ConstraintList currValues = currTuple.getConstrains();
          ConstraintList currCondition = currLabel.getConstraint();

          final List<SimpleExpressionProxy> nextValues = new ArrayList<>();
          final List<SimpleExpressionProxy> nextConditions = new ArrayList<>();

          if (!notInStateValue.isEmpty()) {
            currCondition = getCompleteFormExpressions(currCondition,
                                                       notInStateValue);
          }

          // Executing (evaluting) the current conditions w.r.t. current values
          // to get next values (nextValues) and residual conditions (nextConditions).
          final boolean isSatisfiable = execute(PEVars,
                                                currValues,
                                                currCondition,
                                                nextValues,
                                                nextConditions);
          // If is not satisfiable then continue with the next transition.
          if (!isSatisfiable) {
            continue;
          }

          final int oCurrTarget = iter.getCurrentTargetState();
          ConstraintList nextValue = new ConstraintList(nextValues);
          if (hasStateValue) {
            final ConstraintList oValue = oStateValueMap.get(oCurrTarget);
            nextValue = mergeByString(nextValue, oValue);
          }
          // Creating the next tuple to process.
          final Tuple nextTuple = new Tuple(oCurrTarget, nextValue);
          final SimpleEFAEventDecl nextEvent = currLabel.getEvent();
          final SimpleEFATransitionLabel pLabel =
           new SimpleEFATransitionLabel(nextEvent,
                                        new ConstraintList(nextConditions));
          final int nextLabelId = pLabelEncoding.createTransitionLabelId(pLabel);
          final int pCurrSource = getStateId(currTuple);
          final int pCurrTarget;
          if (isTupleVisited(nextTuple)) {
            // If we already visited this tuple.
            pCurrTarget = getStateId(nextTuple);
          } else {
            // If it is a new tuple.
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
            // Adding values to the pCurrTargetState.
            pCurrTargetState.mergeToAttribute(
             SimpleEFAHelper.DEFAULT_STATEVALUE_STRING,
             printExpressions(nextValue, "",
             SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR, ""),
             SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR);
            pStatePropMap.put(oCurrTarget, oRel.getAllMarkings(oCurrTarget));
            pStateEncoding.put(pCurrTargetState, pCurrTarget);
            stack.push(nextTuple);
          }
          final int[] tr = new int[]{pCurrSource, nextLabelId, pCurrTarget};
          // Adding the new transition to the list.
          pTR.add(tr);
        }
      }
      System.err.println("PE Finish Analyzing");
      final Collection<SimpleEFAVariable> unprimed = new THashSet<>();
      final Collection<SimpleEFAVariable> primed = new THashSet<>();
      // Getting the remaning variables on residual transitions.
      getRemainingVariables(pLabelEncoding, unprimed, primed);
      mStateVars.addAll(PEVars);
      final THashSet<SimpleEFAVariable> pVars = new THashSet<>(unprimed);
      pVars.addAll(primed);
      // Creating a transition relation (pRel) based on the list of transitions (pTR).
      createTransitionRelation(pComponentName,
                               oRel.getKind(),
                               pLabelEncoding.size(),
                               oRel.getNumberOfPropositions(),
                               pStateEncoding.size());
      System.err.println("PE Finish TR");
      // Creating a residual EFA.
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
      // Registering the remaning variables to this component.
      residual.setStructurallyDeterministic(true);
      // Setting the visitor / modifiers of the variables
      residual.setIsEFA(!pVars.isEmpty());
      residual.setUnprimeVariables(new ArrayList<>(unprimed));
      residual.setPrimeVariables(new ArrayList<>(primed));
      residual.setStateVariables(new ArrayList<>(mStateVars));
      residual.register();

      return residual;
    } finally {
      mTupleStateMap = null;
      pStatePropMap = null;
      pRel = null;
    }
  }

  private void createTransitionRelation(final String pComponentName,
                                        final ComponentKind kind,
                                        final int nbrLabels,
                                        final int nbrPropositions,
                                        final int nbrStates)
   throws OverflowException
  {
    pRel = new ListBufferTransitionRelation(pComponentName, kind, nbrLabels,
                                            nbrPropositions, nbrStates,
                                            ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    if (!pTR.isEmpty()) {
      pRel.setInitial(pTR.get(0)[0], true);
      for (int i = 0; i < pTR.size(); i++) {
        pRel.setAllMarkings(pTR.get(i)[0], pStatePropMap.get(pTR.get(i)[0]));
        pRel.setAllMarkings(pTR.get(i)[2], pStatePropMap.get(pTR.get(i)[2]));
        pRel.addTransition(pTR.get(i)[0], pTR.get(i)[1], pTR.get(i)[2]);
      }
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

  private ConstraintList getPrettyExpressions(final ConstraintList constraint)
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
    return oStateName + printExpressions(new ConstraintList(nList),
                                         SimpleEFAHelper.DEFAULT_VALUE_OPENING,
                                         SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR,
                                         SimpleEFAHelper.DEFAULT_VALUE_CLOSING);
  }

  private ConstraintList getCompleteFormExpressions(
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
    for (final Tuple tp : mTupleStateMap.keySet()) {
      if (tp.equals(tuple)) {
        return mTupleStateMap.get(tp);
      }
    }
    return -1;
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
    for (final Tuple tp : mTupleStateMap.keySet()) {
      if (tp.equals(tuple)) {
        return true;
      }
    }
    return false;
  }

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

  private TIntObjectHashMap<ConstraintList> createStateValueMap(
   final SimpleEFAStateEncoding stateEncoding)
  {
    final TIntObjectHashMap<ConstraintList> map =
     new TIntObjectHashMap<>(stateEncoding.size());
    for (final SimpleEFAState state : stateEncoding.getSimpleStates()) {
      final int stateId = stateEncoding.getStateId(state);
      final ConstraintList stateValue;
      final String value = state.getAttribute(SimpleEFAHelper.DEFAULT_STATEVALUE_STRING);
      if (value != null && !value.trim().isEmpty()) {
        final String[] str = value
         .split(SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR);
        final List<SimpleExpressionProxy> exps = mHelper.parse(str);
        stateValue = new ConstraintList(exps);
        map.put(stateId, stateValue);
      } else {
        if (mReadStateNameAsValue) {
          final String str = state.getName();
          if (str.contains(SimpleEFAHelper.DEFAULT_VALUE_OPENING)
           && str.contains(SimpleEFAHelper.DEFAULT_VALUE_CLOSING)) {
            final List<SimpleExpressionProxy> exps =
             mHelper.parseString(str, SimpleEFAHelper.DEFAULT_VALUE_OPENING, SimpleEFAHelper.DEFAULT_VALUE_CLOSING);
            stateValue = new ConstraintList(exps);
            map.put(stateId, stateValue);
          }
        }
      }
    }
    return map;
  }

  private ConstraintList mergeByString(final ConstraintList con1,
                                       final ConstraintList con2)
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
    final HashMap<String, SimpleExpressionProxy> map = new HashMap<>();
    for (final SimpleExpressionProxy exp : sList) {
      map.put(exp.toString(), exp);
    }
    sList.clear();
    sList.addAll(map.values());
    return new ConstraintList(sList);
  }

//  private ConstraintList mergeByExpressions(final ConstraintList con1,
//                                            final ConstraintList con2)
//  {
//    NOT IMPLEMENTED YET
//  }

  private String printExpressions(final ConstraintList constraints,
                                  final String opening,
                                  final String separator,
                                  final String closing)
  {
    final StringBuilder result = new StringBuilder();
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

    Tuple()
    {
      this(-1, ConstraintList.TRUE);
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
        if (item.getStateId() == mState) {
          final ConstraintList iCon = item.getConstrains();
          for (final SimpleExpressionProxy exp : mConstrains.getConstraints()) {
            if (!iCon.contains(exp)) {
              return false;
            }
          }
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
  private THashSet<SimpleEFAVariable> mStateVars;
  private THashMap<Tuple, Integer> mTupleStateMap;
  private final SimpleEFAVariableContext mVarContext;
  private final Collection<SimpleEFAComponent> mResiduals;
  private String mSuffixName = ".PE";
  private final SimpleEFAHelper mHelper;
  private boolean mReadStateNameAsValue = false;
  private boolean mAppendValueToStateName = false;
  private TIntLongHashMap pStatePropMap = null;
  private ArrayList<int[]> pTR = null;
  private ListBufferTransitionRelation pRel = null;
}
