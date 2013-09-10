//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFAVariableUnfolder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TLongIntHashMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.analysis.efa.base.UnfoldingVariableContext;
import net.sourceforge.waters.analysis.tr.EventEncoding;
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
  public UnifiedEFAVariableUnfolder(final ModuleProxyFactory factory,
                                    final CompilerOperatorTable optable,
                                    final UnifiedEFAVariableContext context)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mVariableContext = context;
    mVariableFinder = new UnifiedEFAVariableFinder(optable);
    mExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, mOperatorTable);
  }


  //#########################################################################
  //# Configuration
  public void setUnfoldedVariable(final UnifiedEFAVariable var)
  {
    mUnfoldedVariable = var;
  }

  public UnifiedEFAVariable getUnfoldedVariable()
  {
    return mUnfoldedVariable;
  }

  public void setOriginalEvents(final List<AbstractEFAEvent> events)
  {
    mOriginalEvents = events;
  }

  public List<AbstractEFAEvent> getOriginalEvents()
  {
    return mOriginalEvents;
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
    mEventEncoding = new UnifiedEFAEventEncoding(mUnfoldedVariable.getName());
  }

  public void run() throws AnalysisException, EvalException
  {
    try {
      setUp();
      for (final AbstractEFAEvent event : mOriginalEvents) {
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

  public UnifiedEFATransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void expandEvent(final AbstractEFAEvent event) throws EvalException
  {
    mUnfoldingContext.resetCurrentAndPrimedValue();
    final ConstraintList update = event.getUpdate();
    final CompiledRange range = mUnfoldedVariable.getRange();
    mEventUpdateMap = new HashMap<ConstraintList,RenamedEFAEvent>(range.size());
    if (!mVariableFinder.findVariable(update, mUnfoldedVariable)) {
      return;
    } else if (!mVariableFinder.containsPrimedVariable()) {
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
          mPropagator.init(update);
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
          mPropagator.init(update);
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
        mPropagator.init(update);
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
              mPropagator.init(update);
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
    if (mEventUpdateMap.size() == 1) {
      final RenamedEFAEvent finalEvent =
        mEventUpdateMap.values().iterator().next();
      finalEvent.setIndex(-1);
    }
  }

  private int createUnfoldedEvent(final AbstractEFAEvent event)
    throws EvalException
  {
    if (!mPropagator.isUnsatisfiable()) {
      final IdentifierProxy variableName = mUnfoldedVariable.getVariableName();
      mPropagator.removeVariable(variableName);
      mPropagator.removeUnchangedVariables();
      final ConstraintList simplifiedUpdate = mPropagator.getAllConstraints();
      final AbstractEFAEvent foundEvent = mEventUpdateMap.get(simplifiedUpdate);
      if (foundEvent == null) {
        final RenamedEFAEvent newEvent =
          new RenamedEFAEvent(event, simplifiedUpdate, mEventUpdateMap.size());
        mEventUpdateMap.put(simplifiedUpdate, newEvent);
        return mEventEncoding.createEventId(newEvent);
      } else {
        return mEventEncoding.getEventId(foundEvent);
      }
    } else {
      return -1;
    }
  }

  private void addTransition(final int source, final int event, final int target)
  {
    if (event >= 0) {
      final long key = (((long) source) << 32) | event;
      int list = mTransitionMap.get(key);
      if (IntListBuffer.NULL == list) {
        list = mTargetStatesBuffer.createList();
        mTransitionMap.put(key, list);
      }
      mTargetStatesBuffer.append(list, target);
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
      (mUnfoldedVariable.getName(), ComponentKind.PLANT,
       numberOfEvents, numberOfMarkings, numberOfStates,
       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    rel.setProperEventStatus(EventEncoding.TAU,
                             EventEncoding.STATUS_FULLY_LOCAL |
                             EventEncoding.STATUS_UNUSED);
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
    final long[] keys = mTransitionMap.keys();
    Arrays.sort(keys);
    for (final long key : keys) {
      final int state = (int) (key >> 32);
      final int event = (int) (key & 0xffffffffL);
      final int list = mTransitionMap.get(key);
      mTargetStatesBuffer.toArrayList(list, array);
      rel.addTransitions(state, event, array);
      array.clear();
    }
    rel.checkReachability();
    rel.removeProperSelfLoopEvents();
    rel.removeRedundantPropositions();
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
  private List<AbstractEFAEvent> mOriginalEvents;

  private UnfoldingVariableContext mUnfoldingContext;
  private ConstraintPropagator mPropagator;
  private UnifiedEFAEventEncoding mEventEncoding;
  private Map<ConstraintList,RenamedEFAEvent> mEventUpdateMap;
  private TLongIntHashMap mTransitionMap;
  private IntListBuffer mTargetStatesBuffer;
  private UnifiedEFATransitionRelation mTransitionRelation;

}
