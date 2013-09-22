//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   UnifiedEFAUpdateMerger
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.efa.unified;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

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

  public void setUnfoldedEvents(final List<AbstractEFAEvent> events)
  {
    mOriginalEvents = events;
  }

  public List<AbstractEFAEvent> getUnfoldedEvents()
  {
    return mOriginalEvents;
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
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mRemovedEvents = new ArrayList<>();
    mAddedEvents = new ArrayList<>();
    mEventMap = new HashMap<>(mOriginalEvents.size());
    mVariableCollector = new UnifiedEFAVariableCollector(mOperatorTable, mContext);
  }

  public void run() throws AnalysisException, EvalException
  {
    try {
      setUp();
      findIdenticalEvents();
      mergeEvents();
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mVariableCollector = null;
    mEventMap = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void findIdenticalEvents()
  {
    final ListBufferTransitionRelation rel = mTransitionRelation.getTransitionRelation();
    final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
    for (final AbstractEFAEvent event : mOriginalEvents) {
      final TLongArrayList transitions = new TLongArrayList();
      final int code = mTransitionRelation.getEventEncoding().getEventId(event);
      iter.resetEvent(code);
      while (iter.advance()) {
        final long source = iter.getCurrentSourceState();
        final long target = iter.getCurrentTargetState();
        final long states = source<<32 | target;
        transitions.add(states);
      }
      TIntArrayList list = mEventMap.get(transitions);
      if (list == null) {
        list = new TIntArrayList();
        mEventMap.put(transitions, list);
      }
      list.add(code);
    }
  }

  private void mergeEvents()
  {
    final UnifiedEFAEventEncoding encoding =
      mTransitionRelation.getEventEncoding();
    final ListBufferTransitionRelation rel =
      mTransitionRelation.getTransitionRelation();
    for (final TIntArrayList events : mEventMap.values()) {
      if (events.size() > 1) {
        final List<ConstraintList> updates = new ArrayList<>(events.size());
        AbstractEFAEvent original = null;
        int index = -1;
        int firstEventCode = -1;
        for (int i = 0; i < events.size(); i++) {
          final int eventCode = events.get(i);
          final RenamedEFAEvent event = (RenamedEFAEvent) encoding.getEvent(eventCode);
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
        Collections.sort(unchanged, comparator);
        update = new ConstraintList(unchanged);
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
  private UnifiedEFAVariableCollector mVariableCollector;
  private UnifiedEFATransitionRelation mTransitionRelation;
  private List<AbstractEFAEvent> mOriginalEvents;
  private Map<TLongArrayList, TIntArrayList> mEventMap;
  private List<AbstractEFAEvent> mRemovedEvents;
  private List<AbstractEFAEvent> mAddedEvents;
}
