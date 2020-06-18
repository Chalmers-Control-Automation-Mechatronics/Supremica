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

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionRelation;
import net.sourceforge.waters.analysis.efa.base.EFASimplifierStatistics;
import net.sourceforge.waters.analysis.efa.base.UnfoldingVariableContext;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SingleBindingContext;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;

import org.apache.logging.log4j.Logger;


/**
 * An implementation of partial unfolding for extended finite-state machines.
 * Given an EFSM ({@link AbstractEFATransitionRelation}) and a variable to be removed
 * ({@link EFSMVariable}), a new EFSM is computed by removing the given
 * variable and expanding the states to include the different variables
 * values.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class EFSMPartialUnfolder extends AbstractEFSMAlgorithm
{

  //#########################################################################
  //# Constructors
  public EFSMPartialUnfolder(final ModuleProxyFactory factory,
                             final CompilerOperatorTable op)
  {
    createStatistics(true);
    mFactory = factory;
    mOperatorTable = op;
    mEFSMVariableFinder = new EFSMVariableFinder(op);
    mSimpleExpressionCompiler = new SimpleExpressionCompiler(factory, op);
    mUnsatisfiesUpdate = new UnfoldedUpdateInfo(null);
  }


  //#########################################################################
  //# Configuration
  public void setSourceInfoEnabled(final boolean enabled)
  {
    mSourceInfoEnabled = enabled;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.efa.efsm.AbstractEFSMAlgorithm
  @Override
  List<ConstraintList> getSelfloopedUpdates()
  {
    return mUnfoldedSelfloops;
  }


  //#########################################################################
  //# Invocation
  @Override
  public void setUp() throws AnalysisException
  {
    super.setUp();
    mUnfoldedUpdateCache = new TLongObjectHashMap<UnfoldedUpdateInfo>();
    mKnownAfterValueCache =
      new TLongIntHashMap(0, 0.5f, MISSING_CACHE_ENTRY, MISSING_CACHE_ENTRY);
  }

  @Override
  public void tearDown()
  {
    super.tearDown();
    mUnfoldedUpdateCache = null;
    mKnownAfterValueCache = null;
    mInputSelfloops = null;
    mPartition = null;
    mClassToValue = null;
  }

  EFSMTransitionRelation unfold(final EFSMVariable var,
                                final EFSMSystem system)
    throws EvalException, AnalysisException
  {
    return unfold(var, system, null);
  }

  EFSMTransitionRelation unfold(final EFSMVariable var,
                                final EFSMSystem system,
                                final TRPartition partition)
    throws EvalException, AnalysisException
  {
    EFSMTransitionRelation efsmRel = var.getTransitionRelation();
    if (efsmRel == null) {
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(":dummy", ComponentKind.PLANT, 1, 0, 1,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      rel.setInitial(0, true);
      final EFSMEventEncoding enc = new EFSMEventEncoding();
      final List<EFSMVariable> list = Collections.singletonList(var);
      efsmRel = new EFSMTransitionRelation(rel, enc, list);
    }
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Unfolding: " + var.getName() + " ...");
      logger.debug(efsmRel.getTransitionRelation().getNumberOfStates() + " states");
    }
    final long start = System.currentTimeMillis();
    final EFASimplifierStatistics statistics = getStatistics();
    statistics.recordStart(efsmRel);
    EFSMTransitionRelation result = null;
    try {
      mUnfoldedVariable = var;
      setUp();
      final CompiledRange range = var.getRange();
      mRangeValues = range.getValues();
      mRootContext = system.getVariableContext();
      mPartition = partition;
      if (partition == null) {
        mReducedRangeSize = mRangeValues.size();
      } else {
        mClassToValue = new int[partition.getNumberOfClasses()];
        mReducedRangeSize = 0;
        int clazzNum = 0;
        for (final int[] clazz : partition.getClasses()) {
          if (clazz == null) {
            mClassToValue[clazzNum] = -1;
          } else {
            mClassToValue[clazzNum] = clazz[0];
            mReducedRangeSize++;
          }
          clazzNum++;
        }
      }
      checkAbort();
      mUnfoldingVariableContext =
        new UnfoldingVariableContext(mOperatorTable, mRootContext, mUnfoldedVariable);
      mEFSMVariableCollector = new EFSMVariableCollector(mOperatorTable,
                                                         mRootContext);
      mInputTransitionRelation = efsmRel;
      mInputEventEncoding = efsmRel.getEventEncoding();
      final SimpleExpressionProxy varName = var.getVariableName();
      mConstraintPropagator =
        new ConstraintPropagator(mFactory, mOperatorTable,
                                 mUnfoldingVariableContext);

      final EFSMEventEncoding selfloops = mUnfoldedVariable.getSelfloops();
      if (selfloops.size() > 1) {
        // There are proper selfloop updates. These updates must be considered
        // as selfloops on every state of the unfolded EFSM. Create event IDs
        // for all selfloops and remember them in the list mSelfloops.
        mInputSelfloops = new TIntArrayList();
        mInputEventEncoding = new EFSMEventEncoding(mInputEventEncoding);
        for (int e = EventEncoding.NONTAU; e < selfloops.size(); e++) {
          final ConstraintList update = selfloops.getUpdate(e);
          final int ecode = mInputEventEncoding.createEventId(update);
          mInputSelfloops.add(ecode);
        }
      }
      mInputUpdateInfo = new InputUpdateInfo[mInputEventEncoding.size()];

      final SimpleExpressionProxy initStatePredicate =
        var.getInitialStatePredicate();
      final ListBufferTransitionRelation rel = efsmRel.getTransitionRelation();
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final int numInputStates = rel.getNumberOfStates();
      final TIntArrayList initialValues = new TIntArrayList();
      final TIntArrayList initialStates = new TIntArrayList();
      int variableCounter = 0;
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
      mUnfoldedStateMap = new TLongIntHashMap(numInputStates, 0.5f, -1, -1);
      for (int lowState = 0; lowState < initialStates.size(); lowState++) {
        for (int highValue = 0; highValue < initialValues.size(); highValue++) {
          final long initialPair =
            initialStates.get(lowState)
            | ((long) initialValues.get(highValue) << 32);
          final int code = mUnfoldedStateList.size();
          mUnfoldedStateList.add(initialPair);
          mUnfoldedStateMap.put(initialPair, code);
        }
      }
      final int numInitialStates = mUnfoldedStateList.size();
      mUnfoldedUpdateMap =
        new HashMap<ConstraintList,UnfoldedUpdateInfo>(rel.getNumberOfProperEvents());
      mSourceShift = AutomatonTools.log2(mInputEventEncoding.size());
      final int rangeBits = AutomatonTools.log2(mReducedRangeSize);
      final int encodingSize = mSourceShift + rangeBits +
        AutomatonTools.log2(mRangeValues.size());
      if (encodingSize > 64) {
        final String msg =
          "Unfolded transition encoding requires " + encodingSize +
          " bits, 64 is the maximum!";
        throw new OverflowException(msg);
      }
      mTargetShift = mSourceShift + rangeBits;
      final StateExpander expander1 = new StateExpander() {
        @Override
        void newTransition(final int source,
                           final UnfoldedUpdateInfo info,
                           final long targetPair)
        {
          int target = mUnfoldedStateMap.get(targetPair);
          if (target < 0) {
            target = mUnfoldedStateList.size();
            mUnfoldedStateList.add(targetPair);
            mUnfoldedStateMap.put(targetPair, target);
          }
          info.checkSelfloop(source, target);
        }
      };
      int currentState = 0;
      while (currentState < mUnfoldedStateList.size()) {
        expander1.expandState(currentState);
        currentState++;
      }
      final int numUnfoldedStates = mUnfoldedStateList.size();
      mUnfoldedSelfloops = new ArrayList<ConstraintList>();
      final int numUnfoldedUpdates = getNumberOfRelevantUpdates();
      mUnfoldedEventEncoding = new EFSMEventEncoding(numUnfoldedUpdates);
      mUnfoldedTransitionRelation =
        new ListBufferTransitionRelation(rel.getName(), rel.getKind(),
                                         numUnfoldedUpdates,
                                         rel.getNumberOfPropositions(),
                                         numUnfoldedStates,
                                         rel.getConfiguration());
      final StateExpander expander2 = new StateExpander() {
        @Override
        void newTransition(final int source,
                           final UnfoldedUpdateInfo info,
                           final long targetPair)
        {
          if (info.isNeededInEventEncoding()) {
            final int target = mUnfoldedStateMap.get(targetPair);
            final int event = info.createUnfoldedEventNumber();
            mUnfoldedTransitionRelation.addTransition(source, event, target);
          }
        }
      };

      final List<SimpleNodeProxy> EFSMNodeList = efsmRel.getNodeList();

      List<SimpleNodeProxy> nodeList = null;
      if (mSourceInfoEnabled && EFSMNodeList != null) {
        nodeList = new ArrayList<SimpleNodeProxy>(numUnfoldedStates);
      }
      for (int s = 0; s < numUnfoldedStates; s++) {
        if (s < numInitialStates) {
          mUnfoldedTransitionRelation.setInitial(s, true);
        }
        final long pair = mUnfoldedStateList.get(s);
        final int efsmState = (int) (pair & 0xffffffffL);
        if (rel.isMarked(efsmState, 0)) {
          mUnfoldedTransitionRelation.setMarked(s, 0, true);
        }
        expander2.expandState(s);
        if (mSourceInfoEnabled && EFSMNodeList != null) {
          final int value = (int) (pair >> 32);
          final String name = EFSMNodeList.get(efsmState).getName() + ":" + value;
          final SimpleNodeProxy node = mFactory.createSimpleNodeProxy(name);
          nodeList.add(node);
        }
      }
      final Collection<EFSMVariable> variables =
        new THashSet<EFSMVariable>(efsmRel.getVariables().size());
      mEFSMVariableCollector.collectAllVariables(mUnfoldedEventEncoding, variables);
      result = new EFSMTransitionRelation(mUnfoldedTransitionRelation, mUnfoldedEventEncoding,
                                          variables, nodeList);
      return result;

    } finally {
      final long stop = System.currentTimeMillis();
      final long difftime = stop - start;
      if (result != null) {
        final ListBufferTransitionRelation unfoldedRel =
          result.getTransitionRelation();
        if (logger.isDebugEnabled()) {
          final String msg = String.format
            ("%d states, %d propagator calls, %.3f seconds",
             unfoldedRel.getNumberOfStates(),
             mConstraintPropagator.getNumberOfInvocations(),
             0.001f * difftime);
          logger.debug(msg);
        }
        statistics.recordFinish(result, true);
      } else {
        logger.debug("OVERFLOW while unfolding.");
        statistics.recordOverflow();
      }
      recordRunTime(difftime);
      tearDown();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private UnfoldedUpdateInfo getUnfoldedUpdateInfo(final int event,
                                                   final int beforeClass,
                                                   final int afterValue)
    throws EvalException
  {
    final long key = event |
                     ((long) beforeClass << mSourceShift) |
                     ((long) afterValue<< mTargetShift);
    final UnfoldedUpdateInfo found = mUnfoldedUpdateCache.get(key);
    if (found != null) {
      return found;
    } else {
      mUnfoldingVariableContext.setCurrentValue(getValue(beforeClass));
      mUnfoldingVariableContext.setPrimedValue(mRangeValues.get(afterValue));
      final ConstraintList update = mInputEventEncoding.getUpdate(event);
      mConstraintPropagator.init(update);
      final IdentifierProxy varname = mUnfoldedVariable.getVariableName();
      mConstraintPropagator.removeVariable(varname);
      mConstraintPropagator.propagate();
      if (!mConstraintPropagator.isUnsatisfiable()) {
        mConstraintPropagator.removeUnchangedVariables();
        final ConstraintList unfoldedUpdate =
          mConstraintPropagator.getAllConstraints();
        final UnfoldedUpdateInfo info = createUnfoldedUpdateInfo(unfoldedUpdate);
        mUnfoldedUpdateCache.put(key, info);
        return info;
      } else {
        mUnfoldedUpdateCache.put(key, mUnsatisfiesUpdate);
        return mUnsatisfiesUpdate;
      }
    }
  }

  private int getKnownAfterValue(final int event, final int beforeClass)
    throws EvalException
  {
    final long key = event | ((long) beforeClass << mSourceShift);
    final int foundValue = mKnownAfterValueCache.get(key);
    if (foundValue != MISSING_CACHE_ENTRY) {
      return foundValue;
    } else {
      final ConstraintList update = mInputEventEncoding.getUpdate(event);
      mUnfoldingVariableContext.setCurrentValue(getValue(beforeClass));
      mUnfoldingVariableContext.setPrimedValue(null);
      mConstraintPropagator.init(update);
      mConstraintPropagator.propagate();
      if (mConstraintPropagator.isUnsatisfiable()) {
        mKnownAfterValueCache.put(key, UNSATISFIED_UNFOLDING);
        return UNSATISFIED_UNFOLDING;
      }
      final VariableContext context = mConstraintPropagator.getContext();
      final UnaryExpressionProxy varNamePrimed =
        mUnfoldedVariable.getPrimedVariableName();
      final SimpleExpressionProxy afterExpr =
        context.getBoundExpression(varNamePrimed);
      final int afterValue = getValueIndex(afterExpr);
      if (afterValue >= 0) {
        mKnownAfterValueCache.put(key, afterValue);
        final IdentifierProxy varName = mUnfoldedVariable.getVariableName();
        mConstraintPropagator.removeVariable(varName);
        mConstraintPropagator.removeUnchangedVariables();
        final ConstraintList unfoldedUpdate =
          mConstraintPropagator.getAllConstraints();
        final UnfoldedUpdateInfo info =
          createUnfoldedUpdateInfo(unfoldedUpdate);
        final long eventKey = event |
                              ((long) beforeClass << mSourceShift) |
                              ((long) afterValue << mTargetShift);
        mUnfoldedUpdateCache.put(eventKey, info);
        return afterValue;
      } else {
        mKnownAfterValueCache.put(key, UNKNOWN_AFTER_VALUE);
        return UNKNOWN_AFTER_VALUE;
      }
    }
  }

  private UnfoldedUpdateInfo createUnfoldedUpdateInfo(final ConstraintList update)
  {
    final UnfoldedUpdateInfo found = mUnfoldedUpdateMap.get(update);
    if (found != null) {
      return found;
    } else {
      final UnfoldedUpdateInfo info = new UnfoldedUpdateInfo(update);
      mUnfoldedUpdateMap.put(update, info);
      return info;
    }
  }

  private int getNumberOfRelevantUpdates()
  {
    int result = 1;  // Don't forget tau!
    for (final UnfoldedUpdateInfo info : mUnfoldedUpdateMap.values()) {
      if (info.isNeededInEventEncoding()) {
        result++;
      } else if (info.isRelevantSelfloop()) {
        final ConstraintList update = info.getUpdate();
        mUnfoldedSelfloops.add(update);
      }
    }
    return result;
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

  private SimpleExpressionProxy getValue(final int clazz) {
    final int value;
    if (mClassToValue == null) {
      value = clazz;
    } else {
      value = mClassToValue[clazz];
    }
    return mRangeValues.get(value);
  }

  private int getClazz(final int value)
  {
    if (mPartition == null) {
      return value;
    } else {
      return mPartition.getClassCode(value);
    }
  }


  //#########################################################################
  //# Inner Class StateExpander
  private abstract class StateExpander
  {

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
      throws EvalException, AnalysisAbortException
    {
      final long pair = mUnfoldedStateList.get(source);
      final int sourceState = (int) (pair & 0xffffffffL);
      final int beforeClass = (int) (pair >> 32);
      final SimpleExpressionProxy expr = getValue(beforeClass);
      mUnfoldingVariableContext.setCurrentValue(expr);
      mIterator.resetState(sourceState);
      while (mIterator.advance()) {
        final int event = mIterator.getCurrentEvent();
        final int targetState = mIterator.getCurrentTargetState();
        expand(source, beforeClass, event, targetState);
      }
      if (mInputSelfloops != null) {
        for (int index = 0; index < mInputSelfloops.size(); index ++) {
          final int selfloopEvent = mInputSelfloops.get(index);
          expand(source, beforeClass, selfloopEvent, sourceState);
        }
      }
    }

    private void expand(final int source, final int beforeClass,
                        final int event, final int targetState)
      throws EvalException, AnalysisAbortException
    {

      InputUpdateInfo info = mInputUpdateInfo[event];
      if (info == null) {
        mInputUpdateInfo[event] = info = new InputUpdateInfo(event);
      }
      if (!info.containsUnfoldedVariable()) {
        // update does not contain x (unfolded var)
        UnfoldedUpdateInfo unfoldedInfo = info.getUnfoldedUpdateInfo();
        if (unfoldedInfo != null) {
          // update does not contain x' or after-value is known
          final int afterValue = info.getKnownAfterValue();
          if (afterValue == UNCHANGED_AFTER_VALUE) {
            final long targetPair = targetState | ((long) beforeClass << 32);
            newTransition(source, unfoldedInfo, targetPair);
          } else {
            final int afterClass = getClazz(afterValue);
            final long targetPair = targetState | ((long) afterClass << 32);
            newTransition(source, unfoldedInfo, targetPair);
          }
        } else {
          // update does not contain x but x',
          // and different after-values are possible
          for (int afterValue = 0; afterValue < mRangeValues.size(); afterValue++) {
            unfoldedInfo = getUnfoldedUpdateInfo(event, 0, afterValue);
            if (unfoldedInfo != mUnsatisfiesUpdate) {
              final int afterClass = getClazz(afterValue);
              final long targetPair = targetState | ((long) afterClass << 32);
              newTransition(source, unfoldedInfo, targetPair);
            }
          }
        }
      } else {
        // update contains x
        if (!info.containsUnfoldedPrimedVariable()) {
          // update contains x but not x'
          final UnfoldedUpdateInfo unfoldedInfo =
            getUnfoldedUpdateInfo(event, beforeClass, 0);
          if (unfoldedInfo != mUnsatisfiesUpdate) {
            final long targetPair = targetState | ((long) beforeClass << 32);
            newTransition(source, unfoldedInfo, targetPair);
          }
        } else {
          // update contains x and x'
          int afterValue = getKnownAfterValue(event, beforeClass);
          if (afterValue == UNSATISFIED_UNFOLDING) {
            // no transition
          } else if (afterValue == UNKNOWN_AFTER_VALUE) {
            for (afterValue = 0; afterValue < mRangeValues.size(); afterValue++) {
              final UnfoldedUpdateInfo unfoldedInfo =
                getUnfoldedUpdateInfo(event, beforeClass, afterValue);
              if (unfoldedInfo != mUnsatisfiesUpdate) {
                final int afterClass = getClazz(afterValue);
                final long targetPair = targetState | ((long) afterClass << 32);
                newTransition(source, unfoldedInfo, targetPair);
              }
            }
          } else {
            final UnfoldedUpdateInfo unfoldedInfo =
              getUnfoldedUpdateInfo(event, beforeClass, afterValue);
            if (unfoldedInfo != mUnsatisfiesUpdate) {
              final int afterClass = getClazz(afterValue);
              final long targetPair = targetState | ((long) afterClass << 32);
              newTransition(source, unfoldedInfo, targetPair);
            }
          }
        }
      }
      checkAbort();
    }

    abstract void newTransition(int source,
                                UnfoldedUpdateInfo info,
                                long targetPair);


    //#######################################################################
    //# Instance Variables
    private final TransitionIterator mIterator;
  }


  //#########################################################################
  //# Inner Class InputUpdateInfo
  private class InputUpdateInfo
  {
    //#######################################################################
    //# Constructor
    private InputUpdateInfo(final int event) throws EvalException
    {
      final ConstraintList update = mInputEventEncoding.getUpdate(event);
      mEFSMVariableFinder.findVariable(update, mUnfoldedVariable);
      mContainsUnfoldedVariable = mEFSMVariableFinder.containsVariable();
      mContainsUnfoldedPrimedVariable =
        mEFSMVariableFinder.containsPrimedVariable();
      if (!mContainsUnfoldedVariable) {
        if (!mContainsUnfoldedPrimedVariable) {
          mUnfoldedUpdateInfo = createUnfoldedUpdateInfo(update);
          mKnownAfterValue = UNCHANGED_AFTER_VALUE;
        } else {
          mUnfoldingVariableContext.setPrimedValue(null);
          mConstraintPropagator.init(update);
          mConstraintPropagator.propagate();
          assert !mConstraintPropagator.isUnsatisfiable();
          final VariableContext context = mConstraintPropagator.getContext();
          final UnaryExpressionProxy varNamePrimed =
            mUnfoldedVariable.getPrimedVariableName();
          final SimpleExpressionProxy afterExpr =
            context.getBoundExpression(varNamePrimed);
          final int afterValue = getValueIndex(afterExpr);
          if (afterValue >= 0) {
            mKnownAfterValue = afterValue;
            final IdentifierProxy varName = mUnfoldedVariable.getVariableName();
            mConstraintPropagator.removeVariable(varName);
            mConstraintPropagator.removeUnchangedVariables();
            final ConstraintList unfoldedUpdate =
              mConstraintPropagator.getAllConstraints();
            mUnfoldedUpdateInfo = createUnfoldedUpdateInfo(unfoldedUpdate);
          } else {
            mUnfoldedUpdateInfo = null;
            mKnownAfterValue = UNKNOWN_AFTER_VALUE;
          }
        }
      } else {
        mUnfoldedUpdateInfo = null;
        if (!mContainsUnfoldedPrimedVariable) {
          mKnownAfterValue = UNCHANGED_AFTER_VALUE;
        } else {
          mKnownAfterValue = UNKNOWN_AFTER_VALUE;
        }
      }
    }

    //#######################################################################
    //# Simple Access
    private boolean containsUnfoldedVariable()
    {
      return mContainsUnfoldedVariable;
    }

    private boolean containsUnfoldedPrimedVariable()
    {
      return mContainsUnfoldedPrimedVariable;
    }

    private UnfoldedUpdateInfo getUnfoldedUpdateInfo()
    {
      return mUnfoldedUpdateInfo;
    }

    private int getKnownAfterValue()
    {
      return mKnownAfterValue;
    }

    //#######################################################################
    //# Data Members
    private final boolean mContainsUnfoldedVariable;
    private final boolean mContainsUnfoldedPrimedVariable;
    private final UnfoldedUpdateInfo mUnfoldedUpdateInfo;
    private final int mKnownAfterValue;
  }


  //#########################################################################
  //# Inner Class UnfoldedUpdateInfo
  private class UnfoldedUpdateInfo
  {
    //#######################################################################
    //# Constructor
    private UnfoldedUpdateInfo(final ConstraintList update)
    {
      mUpdate = update;
      mIsPureGuard =
        update == null ? true : !mEFSMVariableFinder.findPrime(update);
      mUnfoldedEventNumber = MISSING_CACHE_ENTRY;
      mNumberOfSelfloops = 0;
      mLastSelfloop = -1;
    }

    //#######################################################################
    //# Simple Access
    private ConstraintList getUpdate()
    {
      return mUpdate;
    }

    private int createUnfoldedEventNumber()
    {
      if (mUnfoldedEventNumber == MISSING_CACHE_ENTRY) {
        mUnfoldedEventNumber = mUnfoldedEventEncoding.createEventId(mUpdate);
        if (mUpdate.isTrue()) {
          mUnfoldedTransitionRelation.setProperEventStatus
           (mUnfoldedEventNumber, EventStatus.STATUS_FULLY_LOCAL);
        } else if (mIsPureGuard) {
          mUnfoldedTransitionRelation.setProperEventStatus
           (mUnfoldedEventNumber, EventStatus.STATUS_SELFLOOP_ONLY);
        }
      }
      return mUnfoldedEventNumber;
    }

    private void checkSelfloop(final int source, final int target)
    {
      if (source != target) {
        mNumberOfSelfloops = mLastSelfloop = -1;
      } else if (source != mLastSelfloop && mNumberOfSelfloops >= 0) {
        mNumberOfSelfloops++;
        mLastSelfloop = source;
      }
    }

    private boolean isNeededInEventEncoding()
    {
      if (mIsPureGuard) {
        return mNumberOfSelfloops < 0;
      } else {
        return mNumberOfSelfloops < mUnfoldedStateList.size();
      }
    }

    private boolean isRelevantSelfloop()
    {
      if (mIsPureGuard) {
        return false;
      } else {
        return mNumberOfSelfloops == mUnfoldedStateList.size();
      }
    }

    //#######################################################################
    //# Data Members
    private final ConstraintList mUpdate;
    private final boolean mIsPureGuard;
    private int mUnfoldedEventNumber;
    private int mNumberOfSelfloops;
    private int mLastSelfloop;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final EFSMVariableFinder mEFSMVariableFinder;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final UnfoldedUpdateInfo mUnsatisfiesUpdate;
  private ConstraintPropagator mConstraintPropagator;
  private EFSMVariableCollector mEFSMVariableCollector;

  private boolean mSourceInfoEnabled;
  private TIntArrayList mInputSelfloops;

  private EFSMVariableContext mRootContext;
  private EFSMTransitionRelation mInputTransitionRelation;
  private EFSMEventEncoding mInputEventEncoding;
  private EFSMVariable mUnfoldedVariable;
  private List<? extends SimpleExpressionProxy> mRangeValues;
  private int mReducedRangeSize;
  private TRPartition mPartition;
  private int[] mClassToValue;
  private int mSourceShift;
  private int mTargetShift;
  private InputUpdateInfo[] mInputUpdateInfo;
  private UnfoldingVariableContext mUnfoldingVariableContext;
  private Map<ConstraintList,UnfoldedUpdateInfo> mUnfoldedUpdateMap;
  private TLongObjectHashMap<UnfoldedUpdateInfo> mUnfoldedUpdateCache;
  private TLongIntHashMap mKnownAfterValueCache;
  private TLongArrayList mUnfoldedStateList;
  private TLongIntHashMap mUnfoldedStateMap;
  private EFSMEventEncoding mUnfoldedEventEncoding;
  private ListBufferTransitionRelation mUnfoldedTransitionRelation;
  private List<ConstraintList> mUnfoldedSelfloops;


  //#########################################################################
  //# Class Constants

  private static final int UNSATISFIED_UNFOLDING = -1;
  private static final int UNKNOWN_AFTER_VALUE = -2;
  private static final int UNCHANGED_AFTER_VALUE = -3;
  private static final int MISSING_CACHE_ENTRY = -4;

}
