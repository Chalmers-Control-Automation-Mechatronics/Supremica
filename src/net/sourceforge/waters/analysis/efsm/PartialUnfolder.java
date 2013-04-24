//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   PartialUnfolder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import gnu.trove.TIntArrayList;
import gnu.trove.TLongArrayList;
import gnu.trove.TLongIntHashMap;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SingleBindingContext;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class PartialUnfolder
{

  //#########################################################################
  //# Constructors
  public PartialUnfolder(final ModuleProxyFactory factory,
                         final CompilerOperatorTable op,
                         final VariableContext binding)
  {
    mFactory = factory;
    mRootContext = binding;
    mOperatorTable = op;
    mSimpleExpressionCompiler = new SimpleExpressionCompiler(factory, op);
    mUnfoldingVariableContext = new UnfoldingVariableContext();
    mConstraintPropagator =
      new ConstraintPropagator(mFactory, mOperatorTable,
                               mUnfoldingVariableContext);
  }

  //#########################################################################

  EFSMTransitionRelation unfold(final EFSMTransitionRelation EFSMRel,
                                final EFSMVariable var) throws EvalException,
    OverflowException
  {
    final List<SimpleNodeProxy> nodeList = EFSMRel.getNodeList();
    final SimpleExpressionProxy initStatePredicate =
      var.getInitialStatePredicate();
    final CompiledRange range = var.getRange();
    final EFSMEventEncoding efsmEvents = EFSMRel.getEventEncoding();
    final SimpleExpressionProxy varName = var.getVariableName();
    final TIntArrayList initialValues = new TIntArrayList(range.size());
    mVarBindingContextMap = new ArrayList<BindingContext>(range.size());
    final TIntArrayList initialStates = new TIntArrayList(nodeList.size());
    int variableCounter = 0;
    final List<? extends SimpleExpressionProxy> rangeValues =
      range.getValues();
    for (final SimpleExpressionProxy rangeValue : rangeValues) {
      final BindingContext context =
        new SingleBindingContext(varName, rangeValue, mRootContext);
      final SimpleExpressionProxy simpleEval =
        mSimpleExpressionCompiler.eval(initStatePredicate, context);
      final boolean isInitial =
        mSimpleExpressionCompiler.getBooleanValue(simpleEval);
      if (isInitial) {
        initialValues.add(variableCounter);
      }
      mVarBindingContextMap.add(variableCounter, context);
      variableCounter++;
    }
    final ListBufferTransitionRelation rel = EFSMRel.getTransitionRelation();
    final int stateNumber = rel.getNumberOfStates();
    for (int i = 0; i < stateNumber; i++) {
      if (rel.isInitial(i)) {
        initialStates.add(i);
      }
    }
    final TLongArrayList unfoldedStates = new TLongArrayList(nodeList.size());
    final TLongIntHashMap stateMap = new TLongIntHashMap(nodeList.size());
    int unfoldedStateCounter = 0;
    for (int lowState = 0; lowState < initialStates.size(); lowState++) {
      for (int highValue = 0; highValue < initialValues.size(); highValue++) {
        final long initialPair =
          initialStates.get(lowState)
            | ((long) initialValues.get(highValue) << 32);
        unfoldedStates.add(initialPair);
        stateMap.put(initialPair, unfoldedStateCounter);
        unfoldedStateCounter++;
      }
    }
    final int numOfInitialState = unfoldedStateCounter;
    int currentState = 0;
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final EFSMEventEncoding outputEventEncoding =
      new EFSMEventEncoding(rel.getNumberOfProperEvents());

    while (currentState < unfoldedStates.size()) {
      final long pair = unfoldedStates.get(currentState);
      final int EFSMState = (int) (pair & 0xffffffffL);
      final int value = (int) (pair >> 32);
      mUnfoldingVariableContext.setCurrentValue(rangeValues.get(value));
      iter.resetState(EFSMState);
      while (iter.advance()) {
        final int event = iter.getCurrentEvent();
        final int target = iter.getCurrentTargetState();
        final ConstraintList update = efsmEvents.getUpdate(event);
        int primeValue = 0;
        for (final SimpleExpressionProxy rangeValue : rangeValues) {
          mUnfoldingVariableContext.setPrimedValue(rangeValue);
          mConstraintPropagator.init(update);
          mConstraintPropagator.propagate();
          if (!mConstraintPropagator.isUnsatisfiable()) {
            final ConstraintList allConstraints =
              mConstraintPropagator.getAllConstraints();
            outputEventEncoding.createEventId(allConstraints);
            final long newPair = target | ((long) primeValue << 32);
            if (!stateMap.contains(newPair)) {
              unfoldedStates.add(newPair);
              stateMap.put(newPair, unfoldedStateCounter);
              unfoldedStateCounter++;
            }
          }
          primeValue++;
        }
      }
      currentState++;
    }
    final ListBufferTransitionRelation newRel =
      new ListBufferTransitionRelation(rel.getName(), rel.getKind(),
                                       outputEventEncoding.size(),
                                       rel.getNumberOfPropositions(),
                                       unfoldedStateCounter,
                                       rel.getConfiguration());

    for (int i = 0; i < unfoldedStateCounter; i++) {
      if (i < numOfInitialState) {
        newRel.setInitial(i, true);
      }
      final long pair = unfoldedStates.get(i);
      final int EFSMState = (int) (pair & 0xffffffffL);
      if (rel.isMarked(EFSMState, 0)) {
        newRel.setMarked(i, 0, true);
      }
      final int value = (int) (pair >> 32);
      mUnfoldingVariableContext.setCurrentValue(rangeValues.get(value));
      iter.resetState(EFSMState);
      while (iter.advance()) {
        final int event = iter.getCurrentEvent();
        final int target = iter.getCurrentTargetState();
        final ConstraintList update = efsmEvents.getUpdate(event);
        int primeValue = 0;
        for (final SimpleExpressionProxy rangeValue : rangeValues) {
          mUnfoldingVariableContext.setPrimedValue(rangeValue);
          mConstraintPropagator.init(update);
          mConstraintPropagator.propagate();
          if (!mConstraintPropagator.isUnsatisfiable()) {
            final ConstraintList allConstraints =
              mConstraintPropagator.getAllConstraints();
            final int newEvent = outputEventEncoding.getEventId(allConstraints);
            final long newPair = target | ((long) primeValue << 32);
            final int newTarget = stateMap.get(newPair);
              newRel.addTransition(i, newEvent, newTarget);
          }
          primeValue++;
        }
      }
      currentState++;
    }
    return new EFSMTransitionRelation(newRel, outputEventEncoding);
  }


  //#########################################################################
  //# Inner Class EFSMVariableContext
  /**
   * A variable context for EFSM compilation. Contains ranges of all
   * variables, and identifies enumeration atoms.
   */
  class UnfoldingVariableContext implements VariableContext
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
    {
      return mRootContext.getVariableRange(varname);
    }

    @Override
    public SimpleExpressionProxy getBoundExpression(final SimpleExpressionProxy varname)
    {
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      if (eq.equals(varname, mUnfoldVariable.getVariableName())) {
        return mCurrentValue;
      } else if (varname instanceof UnaryExpressionProxy) {
        final UnaryExpressionProxy unary = (UnaryExpressionProxy) varname;
        final UnaryOperator op = unary.getOperator();
        if (op == mOperatorTable.getNextOperator()) {
          final SimpleExpressionProxy subterm = unary.getSubTerm();
          if (eq.equals(subterm, mUnfoldVariable.getVariableName())) {
            return mPrimedValue;
          }
        }
      }
      return mRootContext.getBoundExpression(varname);
    }

    @Override
    public final boolean isEnumAtom(final IdentifierProxy ident)
    {
      return mRootContext.isEnumAtom(ident);
    }

    @Override
    public ModuleBindingContext getModuleBindingContext()
    {
      return mRootContext.getModuleBindingContext();
    }

    @Override
    public int getNumberOfVariables()
    {
      return mRootContext.getNumberOfVariables();
    }

    private void setCurrentValue(final SimpleExpressionProxy current)
    {
      mCurrentValue = current;
    }

    private void setPrimedValue(final SimpleExpressionProxy primed)
    {
      mPrimedValue = primed;
    }

    //#########################################################################
    //# Data Members
    private SimpleExpressionProxy mCurrentValue;
    private SimpleExpressionProxy mPrimedValue;

  }

  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final VariableContext mRootContext;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private List<BindingContext> mVarBindingContextMap;
  private final UnfoldingVariableContext mUnfoldingVariableContext;

  private final ConstraintPropagator mConstraintPropagator;
  private EFSMVariable mUnfoldVariable;

}
