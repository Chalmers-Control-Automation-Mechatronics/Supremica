//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   PartialUnfolder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
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

import org.apache.log4j.Logger;


/**
 * An implementation of partial unfolding for extended finite-state machines.
 * Given an EFSM ({@link EFSMTransitionRelation}) and a variable to be removed
 * ({@link EFSMVariable}), a new EFSM is computed by removing the given
 * variable and expanding the states to include the different variables
 * values.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class PartialUnfolder extends AbstractEFSMAlgorithm
{

  //#########################################################################
  //# Constructors
  public PartialUnfolder(final ModuleProxyFactory factory,
                         final CompilerOperatorTable op)
  {
    createStatistics(true);
    mFactory = factory;
    mOperatorTable = op;
    mEFSMVariableFinder = new EFSMVariableFinder(op);
    mSimpleExpressionCompiler = new SimpleExpressionCompiler(factory, op);
  }


  //#########################################################################
  //# Configuration
  public void setSourceInfoEnabled(final boolean enabled)
  {
    mSourceInfoEnabled = enabled;
  }


  //#########################################################################
  //# Invocation
  @Override
  public void setUp() throws AnalysisException
  {
    super.setUp();
  }

  @Override
  public void tearDown()
  {
    super.tearDown();
    mSelfloops = null;
    mValueToClass = null;
    mClassToValue = null;
  }

  EFSMTransitionRelation unfold(final EFSMTransitionRelation efsmRel,
                                final EFSMVariable var,
                                final EFSMSystem system)
    throws EvalException, AnalysisException
  {
    return unfold(efsmRel, var, system, null);
  }

  EFSMTransitionRelation unfold(final EFSMTransitionRelation efsmRel,
                                final EFSMVariable var,
                                final EFSMSystem system,
                                final List<int[]> partition)
    throws EvalException, AnalysisException
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Unfolding: " + efsmRel.getName() + " \\ " + var.getName() + " ...");
      logger.debug(efsmRel.getTransitionRelation().getNumberOfStates() + " states");
    }
    final long start = System.currentTimeMillis();
    final EFSMSimplifierStatistics statistics = getStatistics();
    statistics.recordStart(efsmRel);
    EFSMTransitionRelation result = null;
    try {
      mUnfoldedVariable = var;
      setUp();
      final CompiledRange range = var.getRange();
      mRangeValues = range.getValues();
      mUnfoldedEventCache =
        new TLongIntHashMap(0, 0.5f, MISSING_CACHE_ENTRY, MISSING_CACHE_ENTRY);
      mKnownAfterValueCache =
        new TLongIntHashMap(0, 0.5f, MISSING_CACHE_ENTRY, MISSING_CACHE_ENTRY);
      mRootContext = system.getVariableContext();
      if (partition == null) {
        mReducedRangeSize = mRangeValues.size();
      } else {
        mValueToClass = new int[mRangeValues.size()];
        mClassToValue = new int[partition.size()];
        int clazzNum = 0;
        for (final int[] clazz : partition) {
          mClassToValue[clazzNum] = clazz[0];
          for (final int i : clazz) {
            mValueToClass[i] = clazzNum;
          }
          clazzNum++;
        }
        mReducedRangeSize = partition.size();
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
        mSelfloops = new TIntArrayList();
        mInputEventEncoding = new EFSMEventEncoding(mInputEventEncoding);
        for (int e = EventEncoding.NONTAU; e < selfloops.size(); e++) {
          final ConstraintList update = selfloops.getUpdate(e);
          final int ecode = mInputEventEncoding.createEventId(update);
          mSelfloops.add(ecode);
        }
      }
      mUpdateInfo = new UpdateInfo[mInputEventEncoding.size()];

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
      mUnfoldedStateMap = new TLongIntHashMap(numInputStates);
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
      mUnfoldedEventEncoding =
        new EFSMEventEncoding(rel.getNumberOfProperEvents());
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

      final List<SimpleNodeProxy> EFSMNodeList = efsmRel.getNodeList();

      List<SimpleNodeProxy> nodeList = null;
      if (mSourceInfoEnabled && EFSMNodeList != null) {
        nodeList = new ArrayList<SimpleNodeProxy>(numUnfoldedStates);
      }
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
      result = new EFSMTransitionRelation(unfoldedRel, mUnfoldedEventEncoding,
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
            ("%d states, %d propagator calls, %.3f seconds\n",
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
  private int getUnfoldedEvent(final int event, final int beforeClass,
                               final int afterValue)
    throws EvalException
  {
    final long key = event |
                     ((long) beforeClass << mSourceShift) |
                     ((long) afterValue<< mTargetShift);
    final int foundValue = mUnfoldedEventCache.get(key);
    if (foundValue != MISSING_CACHE_ENTRY) {
      return foundValue;
    } else {
      mUnfoldingVariableContext.setCurrentValue(getValue(beforeClass));
      mUnfoldingVariableContext.setPrimedValue(mRangeValues.get(afterValue));
      final ConstraintList update = mInputEventEncoding.getUpdate(event);
      mConstraintPropagator.init(update);
      final IdentifierProxy varname = mUnfoldedVariable.getVariableName();
      mConstraintPropagator.removeVariable(varname);
      mConstraintPropagator.propagate();
      if (!mConstraintPropagator.isUnsatisfiable()) {
        final ConstraintList unfoldedUpdate =
          mConstraintPropagator.getAllConstraints();
        final int newEvent =
          mUnfoldedEventEncoding.createEventId(unfoldedUpdate);
        mUnfoldedEventCache.put(key, newEvent);
        return newEvent;
      } else {
        mUnfoldedEventCache.put(key, UNSATISFIED_UNFOLDING);
        return UNSATISFIED_UNFOLDING;
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
        final ConstraintList unfoldedUpdate =
          mConstraintPropagator.getAllConstraints();
        final int newEvent = mUnfoldedEventEncoding.createEventId(unfoldedUpdate);
        final long eventKey = event |
                              ((long) beforeClass << mSourceShift) |
                              ((long) afterValue << mTargetShift);
        mUnfoldedEventCache.put(eventKey, newEvent);
        return afterValue;
      } else {
        mKnownAfterValueCache.put(key, UNKNOWN_AFTER_VALUE);
        return UNKNOWN_AFTER_VALUE;
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
    if (mValueToClass == null) {
      return value;
    } else {
      return mValueToClass[value];
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
      if (mSelfloops != null) {
        for (int index = 0; index < mSelfloops.size(); index ++) {
          final int selfloopEvent = mSelfloops.get(index);
          expand(source, beforeClass, selfloopEvent, sourceState);
        }
      }
    }

    private void expand(final int source, final int beforeClass,
                        final int event, final int targetState)
      throws EvalException, AnalysisAbortException
    {

      UpdateInfo info = mUpdateInfo[event];
      if (info == null) {
        mUpdateInfo[event] = info = new UpdateInfo(event);
      }
      if (!info.containsUnfoldedVariable()) {
        // update does not contain x (unfolded var)
        int unfoldedEvent = info.getUnfoldedEventNumber();
        if (unfoldedEvent >= 0) {
          // update does not contain x' or after-value is known
          final int afterValue = info.getKnownAfterValue();
          if (afterValue == UNCHANGED_AFTER_VALUE) {
            final long targetPair = targetState | ((long) beforeClass << 32);
            newTransition(source, unfoldedEvent, targetPair);
          } else {
            final int afterClass = getClazz(afterValue);
            final long targetPair = targetState | ((long) afterClass << 32);
            newTransition(source, unfoldedEvent, targetPair);
          }
        } else {
          // update does not contain x but x',
          // and different after-values are possible
          for (int afterValue = 0; afterValue < mRangeValues.size(); afterValue++) {
            unfoldedEvent = getUnfoldedEvent(event, 0, afterValue);
            if (unfoldedEvent >= 0) {
              final int afterClass = getClazz(afterValue);
              final long targetPair = targetState | ((long) afterClass << 32);
              newTransition(source, unfoldedEvent, targetPair);
            }
          }
        }
      } else {
        // update contains x
        if (!info.containsUnfoldedPrimedVariable()) {
          // update contains x but not x'
          final int unfoldedEvent = getUnfoldedEvent(event, beforeClass, 0);
          if (unfoldedEvent >= 0) {
            final long targetPair = targetState | ((long) beforeClass << 32);
            newTransition(source, unfoldedEvent, targetPair);
          }
        } else {
          // update contains x and x'
          int afterValue = getKnownAfterValue(event, beforeClass);
          if (afterValue == UNSATISFIED_UNFOLDING) {
            // no transition
          } else if (afterValue == UNKNOWN_AFTER_VALUE) {
            for (afterValue = 0; afterValue < mRangeValues.size(); afterValue++) {
              final int unfoldedEvent =
                getUnfoldedEvent(event, beforeClass, afterValue);
              if (unfoldedEvent >= 0) {
                final int afterClass = getClazz(afterValue);
                final long targetPair = targetState | ((long) afterClass << 32);
                newTransition(source, unfoldedEvent, targetPair);
              }
            }
          } else {
            final int unfoldedEvent =
              getUnfoldedEvent(event, beforeClass, afterValue);
            if (unfoldedEvent >= 0) {
              final int afterClass = getClazz(afterValue);
              final long targetPair = targetState | ((long) afterClass << 32);
              newTransition(source, unfoldedEvent, targetPair);
            }
          }
        }
      }
      checkAbort();
    }

    abstract void newTransition(int source, int event, long pair);


    //#######################################################################
    //# Instance Variables
    private final TransitionIterator mIterator;
  }


  //#########################################################################
  //# Inner Class UpdateInfo
  private class UpdateInfo
  {
    //#######################################################################
    //# Constructor
    private UpdateInfo(final int event) throws EvalException
    {
      final ConstraintList update = mInputEventEncoding.getUpdate(event);
      mEFSMVariableFinder.findVariable(update, mUnfoldedVariable);
      mContainsUnfoldedVariable = mEFSMVariableFinder.containsVariable();
      mContainsUnfoldedPrimedVariable =
        mEFSMVariableFinder.containsPrimedVariable();
      if (!mContainsUnfoldedVariable) {
        if (!mContainsUnfoldedPrimedVariable) {
          mUnfoldedEventNumber = mUnfoldedEventEncoding.createEventId(update);
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
            final ConstraintList unfoldedUpdate =
              mConstraintPropagator.getAllConstraints();
            mUnfoldedEventNumber =
              mUnfoldedEventEncoding.createEventId(unfoldedUpdate);
          } else {
            mUnfoldedEventNumber = MISSING_CACHE_ENTRY;
            mKnownAfterValue = UNKNOWN_AFTER_VALUE;
          }
        }
      } else {
        mUnfoldedEventNumber = MISSING_CACHE_ENTRY;
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

    private int getUnfoldedEventNumber()
    {
      return mUnfoldedEventNumber;
    }

    private int getKnownAfterValue()
    {
      return mKnownAfterValue;
    }

    //#######################################################################
    //# Data Members
    private final boolean mContainsUnfoldedVariable;
    private final boolean mContainsUnfoldedPrimedVariable;
    private int mUnfoldedEventNumber;
    private int mKnownAfterValue;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final EFSMVariableFinder mEFSMVariableFinder;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private boolean mSourceInfoEnabled;
  private TIntArrayList mSelfloops;

  private TLongIntHashMap mUnfoldedEventCache;
  private TLongIntHashMap mKnownAfterValueCache;
  private UpdateInfo[] mUpdateInfo;
  private int mSourceShift;
  private int mTargetShift;
  private EFSMVariableContext mRootContext;
  private UnfoldingVariableContext mUnfoldingVariableContext;
  private EFSMTransitionRelation mInputTransitionRelation;
  private EFSMEventEncoding mInputEventEncoding;
  private EFSMVariable mUnfoldedVariable;
  private List<? extends SimpleExpressionProxy> mRangeValues;
  private int mReducedRangeSize;
  private int[] mValueToClass;
  private int[] mClassToValue;
  private TLongArrayList mUnfoldedStateList;
  private TLongIntHashMap mUnfoldedStateMap;
  private EFSMEventEncoding mUnfoldedEventEncoding;
  private ConstraintPropagator mConstraintPropagator;
  private EFSMVariableCollector mEFSMVariableCollector;


  //#########################################################################
  //# Class Constants
  private static final int UNSATISFIED_UNFOLDING = -1;
  private static final int UNKNOWN_AFTER_VALUE = -2;
  private static final int UNCHANGED_AFTER_VALUE = -3;
  private static final int MISSING_CACHE_ENTRY = -4;

}
