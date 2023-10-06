//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.efa.base.EFASimplifierStatistics;
import net.sourceforge.waters.analysis.efa.base.UnfoldingVariableContext;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

import org.apache.logging.log4j.Logger;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class EFSMVariablePartitionComputer extends AbstractEFSMAlgorithm
{

  //#########################################################################
  //# Constructors
  public EFSMVariablePartitionComputer(final ModuleProxyFactory factory,
                                       final CompilerOperatorTable op)
  {
    createStatistics(false);
    mFactory = factory;
    mCompilerOperatorTable = op;
    mEFSMVariableFinder = new EFSMVariableFinder(op);
    mBisimulator = new ObservationEquivalenceTRSimplifier();
    mBisimulator.setEquivalence
      (ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    mBisimulator.requestAbort();
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    mBisimulator.resetAbort();
  }


  //#########################################################################
  //# Invocation
  public TRPartition computePartition(final EFSMVariable var,
                                      final EFSMSystem system)
    throws EvalException, AnalysisException
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Computing partition for " + var.getName() + " (" +
                   var.getRange().size() + " states) ...");
    }
    final EFASimplifierStatistics statistics = getStatistics();
    statistics.recordStart(var);
    final long start = System.currentTimeMillis();
    mEFSMVariable = var;
    TRPartition partition = null;
    try {
      setUp();
      mEFSMVariableCollector =
        new EFSMVariableCollector(mCompilerOperatorTable,
                                  system.getVariableContext());
      mUnfoldingVariableContext =
        new UnfoldingVariableContext(mCompilerOperatorTable,
                                     system.getVariableContext(), var);
      mConstraintPropagator = new ConstraintPropagator(mFactory,
                                                       mCompilerOperatorTable,
                                                       mUnfoldingVariableContext);
      mRangeValues = var.getRange().getValues();
      final SimpleExpressionProxy efsmVariableName = var.getVariableName();
      final SimpleExpressionProxy efsmVariableNamePrimed = var.getPrimedVariableName();

      // 1. Check whether updates are feasible, and collect them ...
      // An update is feasible if it can be written as A & B,
      // where A contains only x (unfolded variable) and
      // B does not contain x.
      mRelevantUpdates = new ArrayList<ConstraintList>();
      mMergibleUpdates = new THashSet<ConstraintList>();
      final EFSMTransitionRelation efsmTR = var.getTransitionRelation();
      if (efsmTR != null) {
        final EFSMEventEncoding encoding = efsmTR.getEventEncoding();
        if (!checkEventEncoding(encoding)) {
          return null;
        }
      }
      final EFSMEventEncoding selfloops = var.getSelfloops();
      if (!checkEventEncoding(selfloops)) {
        return null;
      }

      // 2. Merge updates if possible ...
      if (!mMergibleUpdates.isEmpty()) {
        final EFSMEventEncoding mergedEventEncoding =
          new EFSMEventEncoding(mRelevantUpdates.size());
        for (final ConstraintList update : mRelevantUpdates) {
          if (!mMergibleUpdates.contains(update)) {
            mergedEventEncoding.createEventId(update);
          }
        }
        final TIntObjectHashMap<List<ConstraintList>> mergedUpdates =
          new TIntObjectHashMap<List<ConstraintList>>();
        if (efsmTR != null) {
          final EFSMEventEncoding encoding = efsmTR.getEventEncoding();
          final ListBufferTransitionRelation rel =
            efsmTR.getTransitionRelation();
          final TransitionIterator iter = rel.createAnyReadOnlyIterator();
          for (int fromState = 0; fromState < rel.getNumberOfStates(); fromState++) {
            iter.resetState(fromState);
            mergedUpdates.clear();
            while (iter.advance()) {
              final int event = iter.getCurrentEvent();
              final ConstraintList update = encoding.getUpdate(event);
              if (mMergibleUpdates.contains(update)) {
                final int toState = iter.getCurrentToState();
                List<ConstraintList> list = mergedUpdates.get(toState);
                if (list == null) {
                  list = new ArrayList<ConstraintList>();
                  mergedUpdates.put(toState, list);
                }
                list.add(update);
              }
              checkAbort();
            }
            final TIntObjectIterator<List<ConstraintList>> hIter =
              mergedUpdates.iterator();
            while (hIter.hasNext()) {
              hIter.advance();
              final List<ConstraintList> updates = hIter.value();
              createMergedUpdate(mergedEventEncoding, updates);
            }
          }
        }
        if (selfloops.size() > 0) {
          final List<ConstraintList> mergibleSelfloops =
            new ArrayList<ConstraintList>(selfloops.size());
          for (int e = EventEncoding.NONTAU; e < selfloops.size(); e++) {
            final ConstraintList update = selfloops.getUpdate(e);
            if (mMergibleUpdates.contains(update)) {
              mergibleSelfloops.add(update);
            }
          }
          createMergedUpdate(mergedEventEncoding, mergibleSelfloops);
          checkAbort();
        }
        final int mergedSize = mergedEventEncoding.size();
        mRelevantUpdates =  new ArrayList<ConstraintList>(mergedSize);
        for (int event = EventEncoding.NONTAU; event < mergedSize; event++) {
          final ConstraintList update = mergedEventEncoding.getUpdate(event);
          mRelevantUpdates.add(update);
        }
      }

      // 3. Create a transition relation for the variable automaton ...
      final String name = var.getName();
      final int numEvents = mRelevantUpdates.size() + 1;
      final int numStates = var.getRange().size();
      final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(name, ComponentKind.PLANT,
                                         numEvents, 0, numStates, config);
      final int rangeSize = mRangeValues.size();
      int event = EventEncoding.NONTAU;
      for (final ConstraintList update : mRelevantUpdates) {
        mEFSMVariableFinder.findVariable(update, mEFSMVariable);
        if (!mEFSMVariableFinder.containsVariable()) {
          // Unprimed variable not in update --- no need for any transitions
        } else if (!mEFSMVariableFinder.containsPrimedVariable()) {
          // Only unprimed variable in update --- refine initial partition
          mUnfoldingVariableContext.setCurrentValue(null);
          mConstraintPropagator.init(update);
          mConstraintPropagator.propagate();
          assert !mConstraintPropagator.isUnsatisfiable();
          final VariableContext context = mConstraintPropagator.getContext();
          SimpleExpressionProxy beforeExpr =
            context.getBoundExpression(efsmVariableName);
          int beforeValue = -1;
          if (beforeExpr != null) {
            final CompiledRange range = mEFSMVariable.getRange();
            beforeValue = range.indexOf(beforeExpr);
          }
          if (beforeValue >= 0) {
            rel.addTransition(beforeValue, event, beforeValue);
            checkAbort();
          } else {
            for (beforeValue = 0; beforeValue < mRangeValues.size(); beforeValue++) {
              beforeExpr = mRangeValues.get(beforeValue);
              mUnfoldingVariableContext.setCurrentValue(beforeExpr);
              mConstraintPropagator.init(update);
              mConstraintPropagator.propagate();
              if (!mConstraintPropagator.isUnsatisfiable()) {
                rel.addTransition(beforeValue, event, beforeValue);
              }
              checkAbort();
            }
          }
        } else {
          // Both primed and unprimed variable in update --- create transitions
          int beforeValue = 0;
          for (final SimpleExpressionProxy beforeExpr : mRangeValues) {
            mUnfoldingVariableContext.setCurrentValue(beforeExpr);
            mUnfoldingVariableContext.setPrimedValue(null);
            mConstraintPropagator.init(update);
            mConstraintPropagator.propagate();
            if (!mConstraintPropagator.isUnsatisfiable()) {
              final VariableContext context =
                mConstraintPropagator.getContext();
              SimpleExpressionProxy afterExpr =
                context.getBoundExpression(efsmVariableNamePrimed);
              int afterValue = -1;
              if (afterExpr != null) {
                final CompiledRange range = mEFSMVariable.getRange();
                afterValue = range.indexOf(afterExpr);
              }
              if (afterValue >= 0) {
                rel.addTransition(beforeValue, event, afterValue);
                checkAbort();
              } else {
                for (afterValue = 0; afterValue < rangeSize; afterValue++) {
                  afterExpr = mRangeValues.get(afterValue);
                  mUnfoldingVariableContext.setPrimedValue(afterExpr);
                  mConstraintPropagator.init(update);
                  mConstraintPropagator.propagate();
                  if (!mConstraintPropagator.isUnsatisfiable()) {
                    rel.addTransition(beforeValue, event, afterValue);
                  }
                  checkAbort();
                }
              }
            }
            beforeValue++;
          }
        }
        event++;
      }

      // 4. Calculate partition using bisimulation algorithm
      mBisimulator.setTransitionRelation(rel);
      if (mBisimulator.run()) {
        partition = mBisimulator.getResultPartition();
        return partition;
      } else {
        return null;
      }
    } finally {
      final long stop = System.currentTimeMillis();
      final long difftime = stop-start;
      recordRunTime(difftime);
      if (partition == null) {
        statistics.recordFinish(var, null);
        logger.debug("NULL result partition");
      } else {
        statistics.recordFinish(var, partition);
        if (logger.isDebugEnabled()) {
          logger.debug("Result partition size " + partition.getNumberOfClasses());
        }
      }
      tearDown();
    }
  }

  private boolean checkEventEncoding(final EFSMEventEncoding encoding)
    throws AnalysisAbortException
  {
    for (int e = EventEncoding.NONTAU; e < encoding.size(); e++) {
      final ConstraintList update = encoding.getUpdate(e);
      if (!checkUpdate(update)) {
        return false;
      }
      checkAbort();
    }
    return true;
  }

  private boolean checkUpdate(final ConstraintList update)
  {
    final Set<EFSMVariable> variables = new THashSet<EFSMVariable>();
    final List<SimpleExpressionProxy> constraints = update.getConstraints();
    final List<SimpleExpressionProxy> relevantConstraints =
      new ArrayList<SimpleExpressionProxy>(constraints.size());
    boolean mergible = true;
    for (final SimpleExpressionProxy expr : constraints) {
      variables.clear();
      mEFSMVariableCollector.collectAllVariables(expr, variables);
      if (variables.contains(mEFSMVariable)){
        if (variables.size() > 1) {
          return false;
        } else {
          relevantConstraints.add(expr);
        }
      } else if (!variables.isEmpty()) {
        mergible = false;
      }
    }
    if (!relevantConstraints.isEmpty()) {
      final ConstraintList relevantUpdate =
        new ConstraintList(relevantConstraints);
      mRelevantUpdates.add(relevantUpdate);
      if (mergible) {
        mMergibleUpdates.add(relevantUpdate);
      }
    }
    return true;
  }

  private void createMergedUpdate(final EFSMEventEncoding mergedEventEncoding,
                                  final List<ConstraintList> updates)
  {
    switch (updates.size()) {
    case 0:
      break;
    case 1:
      final ConstraintList update0 = updates.get(0);
      mergedEventEncoding.createEventId(update0);
      break;
    default:
      final int size = updates.size();
      final boolean[] primed = new boolean[size];
      boolean needsPrime = false;
      int index = 0;
      for (final ConstraintList update : updates) {
        mEFSMVariableFinder.findVariable(update, mEFSMVariable);
        needsPrime |= primed[index++] =
          mEFSMVariableFinder.containsPrimedVariable();
      }
      final Comparator<SimpleExpressionProxy> comparator =
        new ExpressionComparator(mCompilerOperatorTable);
      final List<SimpleExpressionProxy> disjunction =
        new ArrayList<SimpleExpressionProxy>(updates.size());
      index = 0;
      for (final ConstraintList update : updates) {
        SimpleExpressionProxy expr = update.createExpression
          (mFactory, mCompilerOperatorTable.getAndOperator());
        if (needsPrime && !primed[index++]) {
          expr = includeUnchanged(expr);
        }
        disjunction.add(expr);
      }
      final ConstraintList disjunctiveUpdate = new ConstraintList(disjunction);
      disjunctiveUpdate.sort(comparator);
      final SimpleExpressionProxy expr = disjunctiveUpdate.createExpression
        (mFactory, mCompilerOperatorTable.getOrOperator());
      final List<SimpleExpressionProxy> singleton =
        Collections.singletonList(expr);
      final ConstraintList mergedUpdate = new ConstraintList(singleton);
      mergedEventEncoding.createEventId(mergedUpdate);
    }
  }

  private SimpleExpressionProxy includeUnchanged
    (final SimpleExpressionProxy update)
  {
    final ModuleProxyCloner cloner = mFactory.getCloner();
    final SimpleExpressionProxy var =
      (SimpleExpressionProxy) cloner.getClone(mEFSMVariable.getVariableName());
    final SimpleExpressionProxy varPrime =
      (SimpleExpressionProxy) cloner.getClone(mEFSMVariable.getPrimedVariableName());
    final BinaryOperator eqOp = mCompilerOperatorTable.getEqualsOperator();
    final SimpleExpressionProxy eq =
      mFactory.createBinaryExpressionProxy(eqOp, varPrime, var);
    final BinaryOperator andOp = mCompilerOperatorTable.getAndOperator();
    return mFactory.createBinaryExpressionProxy(andOp, update, eq);
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mCompilerOperatorTable;
  private EFSMVariableCollector mEFSMVariableCollector;
  private final EFSMVariableFinder mEFSMVariableFinder;
  private UnfoldingVariableContext mUnfoldingVariableContext;
  private ConstraintPropagator mConstraintPropagator;
  private final ObservationEquivalenceTRSimplifier mBisimulator;

  private EFSMVariable mEFSMVariable;
  private List<ConstraintList> mRelevantUpdates;
  private Set<ConstraintList> mMergibleUpdates;
  private List<? extends SimpleExpressionProxy> mRangeValues;

}
