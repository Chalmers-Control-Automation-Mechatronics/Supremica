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
    mSimpleExpressionCompiler = new SimpleExpressionCompiler(factory, op);
    mUnfoldingVariableContext = new UnfoldingVariableContext();
    mOccursChecker = OccursChecker.getInstance();
  }

  //#########################################################################

  EFSMTransitionRelation unfold(final EFSMTransitionRelation efsmRel,
                                final EFSMVariable var,
                                final VariableContext rootContext)
    throws EvalException,
    OverflowException
  {
    mInputTransitionRelation = efsmRel;
    mUnfoldVariable = var;
    mRootContext = rootContext;
    mConstraintPropagator =
      new ConstraintPropagator(mFactory, mOperatorTable,
                               mUnfoldingVariableContext);

    final SimpleExpressionProxy initStatePredicate =
      var.getInitialStatePredicate();
    final CompiledRange range = var.getRange();
    mEFSMEvents = efsmRel.getEventEncoding();
    final ListBufferTransitionRelation rel = efsmRel.getTransitionRelation();
    final int numOfStates = rel.getNumberOfStates();
    final SimpleExpressionProxy varName = var.getVariableName();
    mPrimedUnfoldVariable =
      mFactory.createUnaryExpressionProxy(mOperatorTable.getNextOperator(),
                                          varName);
    final TIntArrayList initialValues = new TIntArrayList(range.size());
    mVarBindingContextMap = new ArrayList<BindingContext>(range.size());
    final TIntArrayList initialStates = new TIntArrayList(numOfStates);
    int variableCounter = 0;
    mRangeValues = range.getValues();
    for (final SimpleExpressionProxy rangeValue : mRangeValues) {
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
    final int stateNumber = rel.getNumberOfStates();
    for (int i = 0; i < stateNumber; i++) {
      if (rel.isInitial(i)) {
        initialStates.add(i);
      }
    }
    mUnfoldedStates = new TLongArrayList(numOfStates);
    mStateMap = new TLongIntHashMap(numOfStates);
    mUnfoldedStateCounter = 0;
    for (int lowState = 0; lowState < initialStates.size(); lowState++) {
      for (int highValue = 0; highValue < initialValues.size(); highValue++) {
        final long initialPair =
          initialStates.get(lowState)
            | ((long) initialValues.get(highValue) << 32);
        mUnfoldedStates.add(initialPair);
        mStateMap.put(initialPair, mUnfoldedStateCounter);
        mUnfoldedStateCounter++;
      }
    }
    final int numOfInitialState = mUnfoldedStateCounter;
    int currentState = 0;
    mOutputEventEncoding =
      new EFSMEventEncoding(rel.getNumberOfProperEvents());

    final StateExpander expander1 = new StateExpander() {
      @Override
      void newTransition(final int event, final int targetState, final int targetValue)
      {
        final long newPair = targetState | ((long) targetValue << 32);
        if (!mStateMap.contains(newPair)) {
          mUnfoldedStates.add(newPair);
          mStateMap.put(newPair, mUnfoldedStateCounter);
          mUnfoldedStateCounter++;
        }
      }
    };

    while (currentState < mUnfoldedStates.size()) {
      expander1.expandState(currentState);
      currentState++;
    }
    final ListBufferTransitionRelation newRel =
      new ListBufferTransitionRelation(rel.getName(), rel.getKind(),
                                       mOutputEventEncoding.size(),
                                       rel.getNumberOfPropositions(),
                                       mUnfoldedStateCounter,
                                       rel.getConfiguration());
    final StateExpander expander2 = new StateExpander() {
      @Override
      void newTransition(final int event, final int targetState, final int targetValue)
      {
        final long newPair = targetState | ((long) targetValue << 32);
        final int newTarget= mStateMap.get(newPair);
        newRel.addTransition(mCurrentState, event, newTarget);
      }
    };
    for (int i = 0; i < mUnfoldedStateCounter; i++) {
      if (i < numOfInitialState) {
        newRel.setInitial(i, true);
      }
      final long pair = mUnfoldedStates.get(i);
      final int EFSMState = (int) (pair & 0xffffffffL);
      if (rel.isMarked(EFSMState, 0)) {
        newRel.setMarked(i, 0, true);
      }
      mCurrentState = i;
      expander2.expandState(i);
    }
    return new EFSMTransitionRelation(newRel, mOutputEventEncoding);
  }


  //#########################################################################
  //# Inner Class StateExpander
  private abstract class StateExpander {
    private StateExpander()
    {
      final ListBufferTransitionRelation rel =
        mInputTransitionRelation.getTransitionRelation();
      mIterator = rel.createSuccessorsReadOnlyIterator();
    }

    private void expandState(final int source)
      throws EvalException
    {
      final long pair = mUnfoldedStates.get(source);
      final int EFSMState = (int) (pair & 0xffffffffL);
      final int value = (int) (pair >> 32);
      mUnfoldingVariableContext.setCurrentValue(mRangeValues.get(value));
      mIterator.resetState(EFSMState);
      while (mIterator.advance()) {
        final int event = mIterator.getCurrentEvent();
        final int target = mIterator.getCurrentTargetState();
        final ConstraintList update = mEFSMEvents.getUpdate(event);
        if (mOccursChecker.occurs(mPrimedUnfoldVariable, update)) {
          int primeValue = 0;
          for (final SimpleExpressionProxy rangeValue : mRangeValues) {
            mUnfoldingVariableContext.setPrimedValue(rangeValue);
            mConstraintPropagator.init(update);
            mConstraintPropagator.propagate();
            if (!mConstraintPropagator.isUnsatisfiable()) {
              final ConstraintList allConstraints =
                mConstraintPropagator.getAllConstraints();
              final int newEvent =
                mOutputEventEncoding.createEventId(allConstraints);
              newTransition(newEvent, target, primeValue);
            }
            primeValue++;
          }
        } else {
          mConstraintPropagator.init(update);
          mConstraintPropagator.propagate();
          if (!mConstraintPropagator.isUnsatisfiable()) {
            final ConstraintList allConstraints =
              mConstraintPropagator.getAllConstraints();
            final int newEvent =
              mOutputEventEncoding.createEventId(allConstraints);
            newTransition(newEvent, target, value);
          }
        }
      }

    }



    abstract void newTransition(int event, int targetState, int targetValue);

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
  private VariableContext mRootContext;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private List<BindingContext> mVarBindingContextMap;
  private final UnfoldingVariableContext mUnfoldingVariableContext;

  private EFSMTransitionRelation mInputTransitionRelation;
  private ConstraintPropagator mConstraintPropagator;
  private EFSMVariable mUnfoldVariable;
  private TLongArrayList mUnfoldedStates;
  private TLongIntHashMap mStateMap;
  private EFSMEventEncoding mOutputEventEncoding;
  private List<? extends SimpleExpressionProxy> mRangeValues;
  private EFSMEventEncoding mEFSMEvents;
  private int mUnfoldedStateCounter;
  private int mCurrentState;
  private final OccursChecker mOccursChecker;
  private SimpleExpressionProxy mPrimedUnfoldVariable;
}
