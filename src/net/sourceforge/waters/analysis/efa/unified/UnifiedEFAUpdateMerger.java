//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFAUpdateMerger
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.efa.unified;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


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
                                final UnifiedEFAVariableContext context)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mContext = context;
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
  }

  public void run()
    throws AnalysisException
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
          status |= EventEncoding.STATUS_UNUSED;
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
    throws AnalysisAbortException
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
    throws AnalysisAbortException
  {
    mergeEvents(unusedEvents);
    for (final TIntArrayList events : eventMap.values()) {
      mergeEvents(events);
    }
  }

  private void mergeEvents(final TIntArrayList events)
    throws AnalysisAbortException
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
        final RenamedEFAEvent event =
          (RenamedEFAEvent) encoding.getEvent(eventCode);
        mRemovedEvents.add(event);
        if (original == null) {
          firstEventCode = eventCode;
          original = event.getOriginalEvent();
          index = event.getIndex();
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
  {
    final Set<UnifiedEFAVariable> allPrimed = new THashSet<>();
    for (final ConstraintList update : updates) {
      mVariableCollector.collectAllVariables(update, null, allPrimed);
    }
    final int size = updates.size();
    final Comparator<SimpleExpressionProxy> comparator =
      new ExpressionComparator(mOperatorTable);
    final List<SimpleExpressionProxy> disjunction =
      new ArrayList<SimpleExpressionProxy>(size);
    for (ConstraintList update : updates) {
      final Set<UnifiedEFAVariable> primed = new THashSet<>();
      mVariableCollector.collectAllVariables(update, null, primed);
      final List<SimpleExpressionProxy> unchanged = new ArrayList<>();
      for (final UnifiedEFAVariable var : allPrimed) {
        if (!primed.contains(var)) {
          unchanged.add(createUnchanged(var));
        }
      }
      if (!unchanged.isEmpty()) {
        unchanged.addAll(update.getConstraints());
        update = new ConstraintList(unchanged);
        update.sort(comparator);
      }
      final SimpleExpressionProxy expr = update.createExpression
        (mFactory, mOperatorTable.getAndOperator());
      disjunction.add(expr);
    }
    final ConstraintList disjunctiveUpdate = new ConstraintList(disjunction);
    disjunctiveUpdate.sort(comparator);
    final SimpleExpressionProxy expr = disjunctiveUpdate.createExpression
      (mFactory, mOperatorTable.getOrOperator());
    final List<SimpleExpressionProxy> singleton =
      Collections.singletonList(expr);
    return new ConstraintList(singleton);
  }

  private SimpleExpressionProxy createUnchanged
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

  private UnifiedEFAVariableCollector mVariableCollector;


}