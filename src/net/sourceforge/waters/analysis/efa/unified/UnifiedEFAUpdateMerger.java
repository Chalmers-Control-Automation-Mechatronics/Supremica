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
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.hash.THashSet;
import gnu.trove.strategy.HashingStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersHashSet;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A tool to recombine events that have been renamed by
 * {@link UnifiedEFAConflictChecker}.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

class UnifiedEFAUpdateMerger extends AbstractEFAAlgorithm
{
  //#########################################################################
  //# Constructors
  public UnifiedEFAUpdateMerger(final ModuleProxyFactory factory,
                                final CompilerOperatorTable optable,
                                final UnifiedEFAVariableContext context,
                                final AbstractEFAEvent dummy)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mContext = context;
    mDummyRoot = dummy;
  }


  //#########################################################################
  //# Configuration
  public void setTransitionRelation(final UnifiedEFATransitionRelation tr)
  {
    mTransitionRelation = tr;
  }

  public UnifiedEFATransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public void setCandidateEvents(final List<AbstractEFAEvent> events)
  {
    mCandidateEvents = events;
  }

  public List<AbstractEFAEvent> getUnfoldedEvents()
  {
    return mCandidateEvents;
  }

  public List<AbstractEFAEvent> getAddedEvents()
  {
    return mAddedEvents;
  }


  public List<AbstractEFAEvent> getRemovedEvents()
  {
    return mRemovedEvents;
  }

  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    mVariableCollector =
      new UnifiedEFAVariableCollector(mOperatorTable, mContext);
    mRemovedEvents = new ArrayList<>(mCandidateEvents.size());
    mAddedEvents = new ArrayList<>(mCandidateEvents.size() / 2);
    mEqualityVisitor = new ModuleEqualityVisitor(false);
    mPropagator = new ConstraintPropagator(mFactory, mOperatorTable, mContext);
  }

  public void run()
    throws AnalysisException, EvalException
  {
    try {
      setUp();
      if (mCandidateEvents.size() > 1) {
        mergeEventsWithEqualUpdate();
        if (mCandidateEvents.size() > 1) {
          final Map<Set<UnifiedEFAVariable>, List<AbstractEFAEvent>> groups =
            groupEventsWithEqualVariables();
          mergeEvents(groups);
        }
      }
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mVariableCollector = null;
    mEqualityVisitor = null;
    mPropagator = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void mergeEventsWithEqualUpdate()
  {
    final Map<ConstraintList, List<AbstractEFAEvent>> updateMap = new HashMap<>();
    for (final AbstractEFAEvent event : mCandidateEvents) {
      final ConstraintList update = event.getUpdate();
      List<AbstractEFAEvent> events = updateMap.get(update);
      if (events == null) {
        events = new LinkedList<>();
        updateMap.put(update, events);
      }
      events.add(event);
    }
    final UnifiedEFAEventEncoding encoding = mTransitionRelation.getEventEncoding();
    final ListBufferTransitionRelation rel = mTransitionRelation.getTransitionRelation();
    for (final List<AbstractEFAEvent> mergible : updateMap.values()) {
      if (mergible.size() > 1) {
        final AbstractEFAEvent newEvent = mergible.remove(0);
        final int newCode = encoding.getEventId(newEvent);
        for (final AbstractEFAEvent oldEvent : mergible) {
          final int oldCode = encoding.getEventId(oldEvent);
          rel.replaceEvent(oldCode, newCode);
          byte status = rel.getProperEventStatus(oldCode);
          status |= EventStatus.STATUS_UNUSED;
          rel.setProperEventStatus(oldCode, status);
          mRemovedEvents.add(oldEvent);
          mCandidateEvents.remove(oldEvent);
        }
      }
    }
  }

  private Map<Set<UnifiedEFAVariable>, List<AbstractEFAEvent>> groupEventsWithEqualVariables()
  {
    final Map<Set<UnifiedEFAVariable>, List<AbstractEFAEvent>> variableMap = new HashMap<>();
    for (final AbstractEFAEvent candidate : mCandidateEvents) {
      final Set<UnifiedEFAVariable> vars = new THashSet<>();
      final ConstraintList update = candidate.getUpdate();
      mVariableCollector.collectAllVariables(update, vars);
      List<AbstractEFAEvent> events = variableMap.get(vars);
      if (events == null) {
        events = new ArrayList<>();
        variableMap.put(vars, events);
      }
      events.add(candidate);
    }
    return variableMap;
  }

  private void mergeEvents
    (final Map<Set<UnifiedEFAVariable>, List<AbstractEFAEvent>> variableMap)
    throws AnalysisAbortException, EvalException
  {
    final Collection<List<AbstractEFAEvent>> groups = variableMap.values();
    final UnifiedEFAEventEncoding encoding =
      mTransitionRelation.getEventEncoding();
    final ListBufferTransitionRelation rel =
      mTransitionRelation.getTransitionRelation();
    final TransitionIterator iter =
      rel.createAllTransitionsReadOnlyIterator();
    for (final List<AbstractEFAEvent> group : groups) {
      if (group.size() > 1) {
        final Map<TLongArrayList,TIntArrayList> eventMap = new HashMap<>();
        final TIntArrayList unusedEvents = new TIntArrayList();
        for (final AbstractEFAEvent event : group) {
          checkAbort();
          final int code = encoding.getEventId(event);
          if (mTransitionRelation.isUsedEvent(code)) {
            final TLongArrayList transitions = new TLongArrayList();
            iter.resetEvent(code);
            while (iter.advance()) {
              final long source = iter.getCurrentSourceState();
              final long target = iter.getCurrentTargetState();
              final long states = (source << 32) | target;
              transitions.add(states);
            }
            TIntArrayList list = eventMap.get(transitions);
            if (list == null) {
              list = new TIntArrayList();
              eventMap.put(transitions, list);
            }
            list.add(code);
          } else {
            unusedEvents.add(code);
          }
        }
        mergeEvents(eventMap, unusedEvents);
      }
    }
  }

  private void mergeEvents(final Map<TLongArrayList,TIntArrayList> eventMap,
                           final TIntArrayList unusedEvents)
    throws AnalysisAbortException, EvalException
  {
    mergeEvents(unusedEvents);
    for (final TIntArrayList events : eventMap.values()) {
      mergeEvents(events);
    }
  }

  private void mergeEvents(final TIntArrayList events)
    throws AnalysisAbortException, EvalException
  {

    if (events.size() > 1) {
      checkAbort();
      final UnifiedEFAEventEncoding encoding =
        mTransitionRelation.getEventEncoding();
      final ListBufferTransitionRelation rel =
        mTransitionRelation.getTransitionRelation();
      final List<ConstraintList> updates = new ArrayList<>(events.size());
      AbstractEFAEvent original = null;
      int index = -1;
      int firstEventCode = -1;
      for (int i = 0; i < events.size(); i++) {
        final int eventCode = events.get(i);
        final AbstractEFAEvent event = encoding.getEvent(eventCode);
        mRemovedEvents.add(event);
        if (original == null) {
          firstEventCode = eventCode;
          original = event.getOriginalEvent();
          if (original == null) {
            original = mDummyRoot;
          } else {
            final RenamedEFAEvent renamed = (RenamedEFAEvent)event;
            index = renamed.getIndex();
          }
        } else {
          rel.removeEvent(eventCode);
        }
        updates.add(event.getUpdate());
      }
      final ConstraintList mergedUpdate = createMergedUpdate(updates);
      final RenamedEFAEvent newEvent =
        new RenamedEFAEvent(original, mergedUpdate, index);
      encoding.replaceEvent(firstEventCode, newEvent);
      mAddedEvents.add(newEvent);
    }
  }

  private ConstraintList createMergedUpdate(final List<ConstraintList> updates)
    throws EvalException
  {
    // 1. Find literals common to all updates
    final Set<UnifiedEFAVariable> allPrimedVars = new THashSet<>();
    for (final ConstraintList update : updates) {
      mVariableCollector.collectAllVariables(update, null, allPrimedVars);
    }
    // 2. Make sure any primed variables in the combined update are kept
    //    unchanged in any part missing them.
    // 3. Find literals common to all updates
    final List<ConstraintList> extendedUpdates =
      new ArrayList<ConstraintList>(updates.size());
    final Set<UnifiedEFAVariable> primedVars = new THashSet<>();
    final HashingStrategy<Proxy> strategy =
      mEqualityVisitor.getTObjectHashingStrategy();
    Set<SimpleExpressionProxy> commonLiterals = null;
    for (ConstraintList update : updates) {
      mVariableCollector.collectAllVariables(update, null, primedVars);
      if (primedVars.size() < allPrimedVars.size()) {
        mPropagator.init(update);
        for (final UnifiedEFAVariable var : allPrimedVars) {
          if (!primedVars.contains(var)) {
            final SimpleExpressionProxy literal = createUnchangedLiteral(var);
            mPropagator.addConstraint(literal);
          }
        }
        mPropagator.propagate();
        update = mPropagator.getAllConstraints();
      }
      primedVars.clear();
      extendedUpdates.add(update);
      final List<SimpleExpressionProxy> literals = update.getConstraints();
      if (commonLiterals == null) {
        commonLiterals = new WatersHashSet<>(literals.size(), strategy);
        commonLiterals.addAll(literals);
      } else {
        final Set<SimpleExpressionProxy> newLiterals =
          new WatersHashSet<>(commonLiterals.size(), strategy);
        for (final SimpleExpressionProxy exp : literals) {
          if (commonLiterals.contains(exp)) {
            newLiterals.add(exp);
          }
        }
        commonLiterals = newLiterals;
      }
    }
    // 4. Create combined update.
    mPropagator.reset();
    for (final ConstraintList update : extendedUpdates) {
      if (!commonLiterals.isEmpty()) {
        final List<SimpleExpressionProxy> allCommonLiterals =
          update.getConstraints();
        final List<SimpleExpressionProxy> nonCommonLiterals =
          new ArrayList<>();
        for (final SimpleExpressionProxy list : allCommonLiterals) {
          if (!commonLiterals.contains(list)) {
            nonCommonLiterals.add(list);
          }
        }
        final ConstraintList newUpdate = new ConstraintList(nonCommonLiterals);
        mPropagator.addNegation(newUpdate);
      } else {
        mPropagator.addNegation(update);
      }
    }
    mPropagator.propagate();
    if (mPropagator.isUnsatisfiable()) {
      mPropagator.reset();
    } else {
      final ConstraintList update = mPropagator.getAllConstraints();
      mPropagator.reset();
      mPropagator.addNegation(update);
    }
    mPropagator.addConstraints(commonLiterals);
    // Make sure all primed variables are included even in case
    // of simplification. Variables simplified away change arbitrarily.
    for (final UnifiedEFAVariable var : allPrimedVars) {
      final UnaryExpressionProxy primed = var.getPrimedVariableName();
      mPropagator.addPrimedVariables(primed);
    }
    mPropagator.propagate();
    return mPropagator.getAllConstraints();
  }

  private SimpleExpressionProxy createUnchangedLiteral
    (final UnifiedEFAVariable var)
  {
    final ModuleProxyCloner cloner = mFactory.getCloner();
    final SimpleExpressionProxy varName =
      (SimpleExpressionProxy) cloner.getClone(var.getVariableName());
    final SimpleExpressionProxy varPrime =
      (SimpleExpressionProxy) cloner.getClone(var.getPrimedVariableName());
    final BinaryOperator eqOp = mOperatorTable.getEqualsOperator();
    return mFactory.createBinaryExpressionProxy(eqOp, varPrime, varName);
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final UnifiedEFAVariableContext mContext;

  private UnifiedEFATransitionRelation mTransitionRelation;
  private List<AbstractEFAEvent> mCandidateEvents;
  private List<AbstractEFAEvent> mRemovedEvents;
  private List<AbstractEFAEvent> mAddedEvents;
  private ConstraintPropagator mPropagator;

  private UnifiedEFAVariableCollector mVariableCollector;
  private ModuleEqualityVisitor mEqualityVisitor;
  private final AbstractEFAEvent mDummyRoot;

}
