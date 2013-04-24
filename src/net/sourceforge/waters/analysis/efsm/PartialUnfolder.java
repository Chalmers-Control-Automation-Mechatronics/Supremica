//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   PartialUnfolder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import gnu.trove.TIntArrayList;
import gnu.trove.TLongArrayList;
import gnu.trove.TLongIntHashMap;

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
import net.sourceforge.waters.model.compiler.context.OccursChecker;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SingleBindingContext;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * An implementation of partial unfolding for extended finite-state
 * machines. Given an EFSM ({@link EFSMTransitionRelation}) and a
 * variable to be removed ({@link EFSMVariable}), a new EFSM is computed
 * by removing the given variable and expanding the states to include
 * the different variables values.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class PartialUnfolder
{

  //#########################################################################
  //# Constructors
  public PartialUnfolder(final ModuleProxyFactory factory,
                         final CompilerOperatorTable op)
  {
    mFactory = factory;
    mOperatorTable = op;
    mOccursChecker = OccursChecker.getInstance();
    mSimpleExpressionCompiler = new SimpleExpressionCompiler(factory, op);
    mUnfoldingVariableContext = new UnfoldingVariableContext();
  }


  //#########################################################################
  //# Invocation
  EFSMTransitionRelation unfold(final EFSMTransitionRelation efsmRel,
                                final EFSMVariable var,
                                final VariableContext rootContext)
    throws EvalException, OverflowException
  {
    mInputTransitionRelation = efsmRel;
    mUnfoldedVariable = var;
    mRootContext = rootContext;
    mConstraintPropagator =
      new ConstraintPropagator(mFactory, mOperatorTable,
                               mUnfoldingVariableContext);

    final SimpleExpressionProxy initStatePredicate =
      var.getInitialStatePredicate();
    final CompiledRange range = var.getRange();
    mInputEventEncoding = efsmRel.getEventEncoding();
    final ListBufferTransitionRelation rel = efsmRel.getTransitionRelation();
    final int numInputStates = rel.getNumberOfStates();
    final SimpleExpressionProxy varName = var.getVariableName();
    mUnfoldedVariableNamePrimed = mFactory.createUnaryExpressionProxy
      (mOperatorTable.getNextOperator(), varName);
    final TIntArrayList initialValues = new TIntArrayList();
    final TIntArrayList initialStates = new TIntArrayList();
    int variableCounter = 0;
    mRangeValues = range.getValues();
    for (final SimpleExpressionProxy rangeValue : mRangeValues) {
      final BindingContext context =
        new SingleBindingContext(varName, rangeValue, mRootContext);
      final SimpleExpressionProxy boolValue =
        mSimpleExpressionCompiler.eval(initStatePredicate, context);
      if (mSimpleExpressionCompiler.getBooleanValue(boolValue)) {
        initialValues.add(variableCounter);
      }
      variableCounter++;
    }
    for (int s = 0; s < numInputStates; s++) {
      if (rel.isInitial(s)) {
        initialStates.add(s);
      }
    }
    mUnfoldedStateList = new TLongArrayList(numInputStates);
    mUnfoldedStateMap = new TLongIntHashMap(numInputStates);
    for (int lowState = 0; lowState < initialStates.size(); lowState++) {
      for (int highValue = 0; highValue < initialValues.size(); highValue++) {
        final long initialPair = initialStates.get(lowState) |
          ((long) initialValues.get(highValue) << 32);
        final int code = mUnfoldedStateList.size();
        mUnfoldedStateList.add(initialPair);
        mUnfoldedStateMap.put(initialPair, code);
      }
    }
    final int numInitialStates = mUnfoldedStateList.size();
    mUnfoldedEventEncoding =
      new EFSMEventEncoding(rel.getNumberOfProperEvents());

    final StateExpander expander1 = new StateExpander() {
      @Override
      void newTransition(final int source, final int event, final long pair)
      {
        if (!mUnfoldedStateMap.contains(pair)) {
          final int code = mUnfoldedStateList.size();
          mUnfoldedStateList.add(pair);
          mUnfoldedStateMap.put(pair, code);
        }
      }
    };
    int currentState = 0;
    while (currentState < mUnfoldedStateList.size()) {
      expander1.expandState(currentState);
      currentState++;
    }
    final int numUnfoldedStates = mUnfoldedStateList.size();

    final ListBufferTransitionRelation unfoldedRel =
      new ListBufferTransitionRelation(rel.getName(), rel.getKind(),
                                       mUnfoldedEventEncoding.size(),
                                       rel.getNumberOfPropositions(),
                                       numUnfoldedStates,
                                       rel.getConfiguration());
    final StateExpander expander2 = new StateExpander() {
      @Override
      void newTransition(final int source, final int event, final long pair)
      {
        final int target = mUnfoldedStateMap.get(pair);
        unfoldedRel.addTransition(source, event, target);
      }
    };
    for (int s = 0; s < numUnfoldedStates; s++) {
      if (s < numInitialStates) {
        unfoldedRel.setInitial(s, true);
      }
      final long pair = mUnfoldedStateList.get(s);
      final int efsmState = (int) (pair & 0xffffffffL);
      if (rel.isMarked(efsmState, 0)) {
        unfoldedRel.setMarked(s, 0, true);
      }
      expander2.expandState(s);
    }
    return new EFSMTransitionRelation(unfoldedRel, mUnfoldedEventEncoding);
  }


  //#########################################################################
  //# Inner Class StateExpander
  private abstract class StateExpander {

    //#######################################################################
    //# Constructor
    private StateExpander()
    {
      final ListBufferTransitionRelation rel =
        mInputTransitionRelation.getTransitionRelation();
      mIterator = rel.createSuccessorsReadOnlyIterator();
    }

    //#######################################################################
    //# Access
    private void expandState(final int source)
      throws EvalException
    {
      final long pair = mUnfoldedStateList.get(source);
      final int sourceState = (int) (pair & 0xffffffffL);
      final int sourceValue = (int) (pair >> 32);
      mUnfoldingVariableContext.setCurrentValue(mRangeValues.get(sourceValue));
      mIterator.resetState(sourceState);
      while (mIterator.advance()) {
        final int event = mIterator.getCurrentEvent();
        final int targetState = mIterator.getCurrentTargetState();
        final ConstraintList update = mInputEventEncoding.getUpdate(event);
        if (mOccursChecker.occurs(mUnfoldedVariableNamePrimed, update)) {
          int targetValue = 0;
          for (final SimpleExpressionProxy rangeValue : mRangeValues) {
            mUnfoldingVariableContext.setPrimedValue(rangeValue);
            mConstraintPropagator.init(update);
            mConstraintPropagator.propagate();
            if (!mConstraintPropagator.isUnsatisfiable()) {
              final ConstraintList unfoldedUpdate =
                mConstraintPropagator.getAllConstraints();
              final int unfoldedEvent =
                mUnfoldedEventEncoding.createEventId(unfoldedUpdate);
              final long targetPair = targetState | ((long) targetValue << 32);
              newTransition(source, unfoldedEvent, targetPair);
            }
            targetValue++;
          }
        } else {
          mConstraintPropagator.init(update);
          mConstraintPropagator.propagate();
          if (!mConstraintPropagator.isUnsatisfiable()) {
            final ConstraintList unfoldedUpdate =
              mConstraintPropagator.getAllConstraints();
            final int unfoldedEvent =
              mUnfoldedEventEncoding.createEventId(unfoldedUpdate);
            final long targetPair = targetState | ((long) sourceValue << 32);
            newTransition(source, unfoldedEvent, targetPair);
          }
        }
      }
    }

    abstract void newTransition(int source, int event, long pair);

    //#######################################################################
    //# Instance Variables
    private final TransitionIterator mIterator;
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
    public SimpleExpressionProxy getBoundExpression
      (final SimpleExpressionProxy varname)
    {
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      if (eq.equals(varname, mUnfoldedVariable.getVariableName())) {
        return mCurrentValue;
      } else if (varname instanceof UnaryExpressionProxy) {
        final UnaryExpressionProxy unary = (UnaryExpressionProxy) varname;
        final UnaryOperator op = unary.getOperator();
        if (op == mOperatorTable.getNextOperator()) {
          final SimpleExpressionProxy subterm = unary.getSubTerm();
          if (eq.equals(subterm, mUnfoldedVariable.getVariableName())) {
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

    //#######################################################################
    //# Simple Access
    private void setCurrentValue(final SimpleExpressionProxy current)
    {
      mCurrentValue = current;
    }

    private void setPrimedValue(final SimpleExpressionProxy primed)
    {
      mPrimedValue = primed;
    }

    //#######################################################################
    //# Data Members
    private SimpleExpressionProxy mCurrentValue;
    private SimpleExpressionProxy mPrimedValue;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final OccursChecker mOccursChecker;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final UnfoldingVariableContext mUnfoldingVariableContext;

  private VariableContext mRootContext;
  private EFSMTransitionRelation mInputTransitionRelation;
  private EFSMEventEncoding mInputEventEncoding;
  private EFSMVariable mUnfoldedVariable;
  private SimpleExpressionProxy mUnfoldedVariableNamePrimed;
  private List<? extends SimpleExpressionProxy> mRangeValues;
  private TLongArrayList mUnfoldedStateList;
  private TLongIntHashMap mUnfoldedStateMap;
  private EFSMEventEncoding mUnfoldedEventEncoding;
  private ConstraintPropagator mConstraintPropagator;

}
