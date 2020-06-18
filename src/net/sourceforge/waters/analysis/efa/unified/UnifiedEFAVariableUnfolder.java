//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TLongIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.analysis.efa.base.UnfoldingVariableContext;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ComponentKind;
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

import org.apache.logging.log4j.Logger;


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
    mUpdateOriginalEventMap = new HashMap<>();
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
      final Logger logger = getLogger();
      logger.debug("Unfolding variable: " + mUnfoldedVariable.getName());
      setUp();
      createUpdateOriginalEventMap();
      expandEvents();
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
  private void createUpdateOriginalEventMap()
  {
    for (final AbstractEFAEvent event : mOriginalEvents) {
      final ConstraintList update = event.getUpdate();
      List<AbstractEFAEvent> eventList =  mUpdateOriginalEventMap.get(update);
      if (eventList == null) {
        eventList = new ArrayList<>();
        mUpdateOriginalEventMap.put(update, eventList);
      }
      eventList.add(event);
    }
  }

  private void expandEvents() throws EvalException
  {
    for (final AbstractEFAEvent event : mOriginalEvents) {
      final ConstraintList update = event.getUpdate();
      final List<AbstractEFAEvent> events = mUpdateOriginalEventMap.get(update);
      if (event == events.get(0)) {
        expandEvents(update);
      }
    }
  }

  private void expandEvents(final ConstraintList update) throws EvalException
  {
    mUnfoldingContext.resetCurrentAndPrimedValue();
    final CompiledRange range = mUnfoldedVariable.getRange();
    mUpdateRenamedEventMap = new HashMap<>(range.size());
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
        final int[] codes = createUnfoldedEvent(update);
        addTransitions(value, codes, value);
      } else {
        value = 0;
        for (final SimpleExpressionProxy expr : range.getValues()) {
          mUnfoldingContext.setCurrentValue(expr);
          mPropagator.init(update);
          mPropagator.propagate();
          final int[] codes = createUnfoldedEvent(update);
          addTransitions(value, codes, value);
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
        final int[] codes = createUnfoldedEvent(update);
        for (int i=0; i<range.size(); i++) {
          addTransitions(i, codes, value);
        }
      } else {
        value = 0;
        for (final SimpleExpressionProxy expr : range.getValues()) {
          mUnfoldingContext.setPrimedValue(expr);
          mPropagator.init(update);
          mPropagator.propagate();
          final int[] codes = createUnfoldedEvent(update);
          for (int i=0; i<range.size(); i++) {
            addTransitions(i, codes, value);
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
            final int[] codes = createUnfoldedEvent(update);
            addTransitions(source, codes, value);
          } else {
            value = 0;
            for (final SimpleExpressionProxy nextExpr : range.getValues()) {
              mUnfoldingContext.setPrimedValue(nextExpr);
              mPropagator.init(update);
              mPropagator.propagate();
              final int[] codes = createUnfoldedEvent(update);
              addTransitions(source, codes, value);
              value++;
            }
          }
        }
        source++;
      }
    }
    switch (mUpdateRenamedEventMap.size()) {
    case 0:
      mPropagator.init(ConstraintList.TRUE);
      createUnfoldedEvent(update);
      // fall through ...
    case 1:
      final int[] finalEventCodes =
        mUpdateRenamedEventMap.values().iterator().next();
      for (final int finalEventCode : finalEventCodes) {
        final RenamedEFAEvent finalEvent =
          (RenamedEFAEvent) mEventEncoding.getEvent(finalEventCode);
        finalEvent.setIndex(-1);
      }
      break;
    default:
      break;
    }
  }

  private int[] createUnfoldedEvent(final ConstraintList update)
    throws EvalException
  {
    if (!mPropagator.isUnsatisfiable()) {
      final IdentifierProxy variableName = mUnfoldedVariable.getVariableName();
      mPropagator.removeVariable(variableName);
      mPropagator.removeUnchangedVariables();
      final ConstraintList simplifiedUpdate = mPropagator.getAllConstraints();
      int[] renamedEventCodes =
        mUpdateRenamedEventMap.get(simplifiedUpdate);
      if (renamedEventCodes == null) {
        final List<AbstractEFAEvent> originalEventList =
          mUpdateOriginalEventMap.get(update);
        renamedEventCodes = new int[originalEventList.size()];
        int index = 0;
        for (final AbstractEFAEvent event : originalEventList) {
          final RenamedEFAEvent newEvent =
            new RenamedEFAEvent(event, simplifiedUpdate, mUpdateRenamedEventMap.size());
          final int code = mEventEncoding.createEventId(newEvent);
          renamedEventCodes[index] = code;
          index++;
        }
        mUpdateRenamedEventMap.put(simplifiedUpdate, renamedEventCodes);
      }
      return renamedEventCodes;
    } else {
      return null;
    }
  }

  private void addTransitions(final int source,
                              final int[] events,
                              final int target)
  {
    if (events != null) {
      for (final int event : events) {
        final long key = (((long) source) << 32) | event;
        int list = mTransitionMap.get(key);
        if (IntListBuffer.NULL == list) {
          list = mTargetStatesBuffer.createList();
          mTransitionMap.put(key, list);
        }
        mTargetStatesBuffer.append(list, target);
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
      (mUnfoldedVariable.getName(), ComponentKind.PLANT,
       numberOfEvents, numberOfMarkings, numberOfStates,
       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    rel.setProperEventStatus(EventEncoding.TAU,
                             EventStatus.STATUS_FULLY_LOCAL |
                             EventStatus.STATUS_UNUSED);
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
      mTargetStatesBuffer.toTIntCollection(list, array);
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
  private Map<ConstraintList,List<AbstractEFAEvent>> mUpdateOriginalEventMap;
  private Map<ConstraintList,int[]> mUpdateRenamedEventMap;
  private TLongIntHashMap mTransitionMap;
  private IntListBuffer mTargetStatesBuffer;
  private UnifiedEFATransitionRelation mTransitionRelation;

}
