//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFAVariableUnfolder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import gnu.trove.iterator.TLongIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TLongIntHashMap;

import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.analysis.efa.base.UnfoldingVariableContext;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class UnifiedEFAVariableUnfolder extends AbstractEFAAlgorithm
{
  //#########################################################################
  //# Constructors
  UnifiedEFAVariableUnfolder(final ModuleProxyFactory factory,
                             final CompilerOperatorTable op,
                             final UnifiedEFAVariableContext variableContext)
  {
    mFactory = factory;
    mOperatorTable = op;
    mVariableContext = variableContext;
    mVariableFinder = new UnifiedEFAVariableFinder(op);
    mExpressionCompiler = new SimpleExpressionCompiler(mFactory, mOperatorTable);
  }

  //#########################################################################
  //# Configuration

  public void setVariable(final UnifiedEFAVariable var)
  {
    mUnfoldedVariable = var;
  }

  public void setEvents(final List<UnifiedEFAEvent> events)
  {
    mEvents = events;
  }

  public void setTRConfiguration(final int config)
  {
    mTRConfiguration = config;
  }

  public UnifiedEFATransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  //#########################################################################
  //# Invocation
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mUnfoldingContext = new UnfoldingVariableContext
      (mOperatorTable, mVariableContext, mUnfoldedVariable);
    mPropagator =
      new ConstraintPropagator(mFactory, mOperatorTable, mUnfoldingContext);
    mTransitionMap = new TLongIntHashMap(mUnfoldedVariable.getRange().size(),
                                         0.5f, -1, IntListBuffer.NULL);
    mTargetStatesBuffer = new IntListBuffer();
  }

  public void run() throws AnalysisException, EvalException
  {
    try {
      setUp();
      for (final UnifiedEFAEvent event : mEvents) {
        expandEvent(event);
      }

      createTransitionRelation();
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUnfoldingContext = null;
    mPropagator = null;
    mEventEncoding = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void expandEvent(final UnifiedEFAEvent event) throws EvalException
  {
    mUnfoldingContext.resetCurrentAndPrimedValue();
    final ConstraintList update = event.getUpdate();
    mPropagator.init(update);
    mPropagator.propagate();
    final CompiledRange range = mUnfoldedVariable.getRange();
    mVariableFinder.findVariable(update, mUnfoldedVariable);
    if (!mVariableFinder.containsPrimedVariable()) {
      // contains only current state
      final VariableContext context = mPropagator.getContext();
      final IdentifierProxy variableName = mUnfoldedVariable.getVariableName();
      final SimpleExpressionProxy currentExpression =
        context.getBoundExpression(variableName);
      int value = getValueIndex(currentExpression);
      if (value >= 0) {
        final int code = createUnfoldedEvent(event);
        addTransition(value, code, value);
      } else {
        value = 0;
        for (final SimpleExpressionProxy expr : range.getValues()) {
          mUnfoldingContext.setCurrentValue(expr);
          mPropagator.propagate();
          final int code = createUnfoldedEvent(event);
          addTransition(value, code, value);
          value++;
        }
      }
    } else if (!mVariableFinder.containsVariable()) {
      // contains only next state
      final VariableContext context = mPropagator.getContext();
      final UnaryExpressionProxy variableName =
        mUnfoldedVariable.getPrimedVariableName();
      final SimpleExpressionProxy nextExpression =
        context.getBoundExpression(variableName);
      int value = getValueIndex(nextExpression);
      if (value >= 0) {
        final int code = createUnfoldedEvent(event);
        for (int i=0; i<range.size(); i++) {
          addTransition(i, code, value);
        }
      } else {
        value = 0;
        for (final SimpleExpressionProxy expr : range.getValues()) {
          mUnfoldingContext.setPrimedValue(expr);
          mPropagator.propagate();
          final int code = createUnfoldedEvent(event);
          for (int i=0; i<range.size(); i++) {
            addTransition(i, code, value);
          }
          value++;
        }
      }
    } else {
      // contains both current and next states
      int source = 0;
      for (final SimpleExpressionProxy currentExpr : range.getValues()) {
        mUnfoldingContext.resetCurrentAndPrimedValue();
        mUnfoldingContext.setCurrentValue(currentExpr);
        mPropagator.propagate();
        if (!mPropagator.isUnsatisfiable()) {
          final VariableContext context = mPropagator.getContext();
          final UnaryExpressionProxy variableName =
            mUnfoldedVariable.getPrimedVariableName();
          final SimpleExpressionProxy nextExpression =
            context.getBoundExpression(variableName);
          int value = getValueIndex(nextExpression);
          if (value >= 0) {
            final int code = createUnfoldedEvent(event);
            addTransition(source, code, value);
          } else {
            value = 0;
            for (final SimpleExpressionProxy nextExpr : range.getValues()) {
              mUnfoldingContext.setPrimedValue(nextExpr);
              mPropagator.propagate();
              final int code = createUnfoldedEvent(event);
              addTransition(source, code, value);
              value++;
            }
          }
        }
        source++;
      }
    }
  }

  private int createUnfoldedEvent(final UnifiedEFAEvent event)
    throws EvalException
  {
    if (!mPropagator.isUnsatisfiable()) {
      final IdentifierProxy variableName = mUnfoldedVariable.getVariableName();
      mPropagator.removeVariable(variableName);
      final ConstraintList simplifiedUpdate = mPropagator.getAllConstraints();
      final UnifiedEFAEvent newEvent = new UnifiedEFAEvent(event.getEventDecl(),
                                                     simplifiedUpdate);
      return mEventEncoding.createEventId(newEvent);
    } else {
      return -1;
    }
  }

  private void addTransition(final int source, final int event, final int target)
  {
    if (event >= 0) {
      if ((mTRConfiguration & ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0) {
        final long key = (((long) source) << 32) | event;
        int list = mTransitionMap.get(key);
        if (IntListBuffer.NULL == list) {
          list = mTargetStatesBuffer.createList();
          mTransitionMap.put(key, list);
        }
        mTargetStatesBuffer.append(list, target);
      } else {
        final long key = (((long) target) << 32) | event;
        int list = mTransitionMap.get(key);
        if (IntListBuffer.NULL == list) {
          list = mTargetStatesBuffer.createList();
          mTransitionMap.put(key, list);
        }
        mTargetStatesBuffer.append(list, source);
      }
    }
  }


  private int getValueIndex(final SimpleExpressionProxy expr)
  {
    if (expr == null) {
      return -1;
    } else {
      final CompiledRange range = mUnfoldedVariable.getRange();
      return range.indexOf(expr);
    }
  }

  private void createTransitionRelation()
    throws OverflowException, EvalException
  {
    mUnfoldingContext.resetCurrentAndPrimedValue();
    final CompiledRange range = mUnfoldedVariable.getRange();
    final int numberOfEvents = mEventEncoding.size();
    final int numberOfMarkings =
      mUnfoldedVariable.getMarkedStatePredicate()==null ? 0 : 1;
    final int numberOfStates = range.size();
    final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
      (mUnfoldedVariable.getName(), ComponentKind.PLANT, numberOfEvents,
       numberOfMarkings, numberOfStates, mTRConfiguration);
    final SimpleExpressionProxy init =
      mUnfoldedVariable.getInitialStatePredicate();
    final SimpleExpressionProxy marked =
      mUnfoldedVariable.getMarkedStatePredicate();
    int value = 0;
    for (final SimpleExpressionProxy expr : range.getValues()) {
      mUnfoldingContext.setCurrentValue(expr);
      final SimpleExpressionProxy initEval =
        mExpressionCompiler.eval(init, mUnfoldingContext);
      if (mExpressionCompiler.getBooleanValue(initEval)) {
        rel.setInitial(value, true);
      }
      if (marked != null) {
        final SimpleExpressionProxy markedEval =
          mExpressionCompiler.eval(marked, mUnfoldingContext);
        if (mExpressionCompiler.getBooleanValue(markedEval)) {
          rel.setMarked(value, UnifiedEFAEventEncoding.OMEGA, true);
        }
      }
      value++;
    }
    final TIntArrayList array = new TIntArrayList();
    final TLongIntIterator iter = mTransitionMap.iterator();
    while (iter.hasNext()) {
      final long key = iter.key();
      final int state = (int) (key>> 32);
      final int event = (int) (key & 0xffffffffL);
      final int list = iter.value();
      mTargetStatesBuffer.toArrayList(list, array);
      if ((mTRConfiguration & ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0) {
        rel.addTransitions(state, event, array);
      } else {
        rel.addTransitions(array, event, state);
      }
      array.clear();
    }
    mTransitionRelation = new UnifiedEFATransitionRelation(rel, mEventEncoding);
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final UnifiedEFAVariableContext mVariableContext;
  private final UnifiedEFAVariableFinder mVariableFinder;
  private final SimpleExpressionCompiler mExpressionCompiler;

  private UnifiedEFAVariable mUnfoldedVariable;
  private List<UnifiedEFAEvent> mEvents;
  private int mTRConfiguration;

  private UnfoldingVariableContext mUnfoldingContext;
  private ConstraintPropagator mPropagator;
  private UnifiedEFAEventEncoding mEventEncoding;
  private TLongIntHashMap mTransitionMap;
  private IntListBuffer mTargetStatesBuffer;
  private UnifiedEFATransitionRelation mTransitionRelation;

}
