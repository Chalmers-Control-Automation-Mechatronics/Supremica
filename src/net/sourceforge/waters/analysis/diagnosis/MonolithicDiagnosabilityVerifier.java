//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.diagnosis;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductBuilder;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductResult;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TarjanControlStack;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.des.DiagnosabilityChecker;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.analysis.kindtranslator.DefaultVerificationKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.DualCounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.PositiveIntOption;
import net.sourceforge.waters.model.options.StringListOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>An implementation of the diagnosability check algorithm.</P>
 *
 * <P>This verifier checks is given a model with unobservable events,
 * some of which are classified as faults. It determines whether all
 * fault classes are diagnosable, using the verifier-based algorithm by
 * Yoo and Lafortune&nbsp;(2002).</P>
 *
 * <P>Event attributes are used to define which events are faults. Events with
 * the attribute {@link DiagnosabilityAttributeFactory#FAULT_KEY} are faults,
 * and the value of the attribute is the fault class. Different events with
 * the same fault class represent the same fault, and for the system to be
 * diagnosable it is enough if it can be determined whether some event of
 * each fault class has occurred without necessarily specifying the exact
 * event.</P>
 *
 * <P><I>Reference.</I><BR>
 * Tae-Sic Yoo and St&eacute;phane Lafortune. Polynomial-time verification
 * of diagnosability of partially observed discrete-event systems.
 * IEEE Transactions on Automatic Control, <STRONG>47</STRONG>&nbsp;(9),
 * 1491&ndash;1495, 2002.</P>
 *
 * @author Nicholas McGrath
 */
public class MonolithicDiagnosabilityVerifier extends AbstractModelVerifier
  implements DiagnosabilityChecker
{

  //#########################################################################
  //# Constructors
  public MonolithicDiagnosabilityVerifier(final ProductDESProxyFactory factory)
  {
    this(factory, DefaultVerificationKindTranslator.getInstance());
  }

  public MonolithicDiagnosabilityVerifier(final ProductDESProxyFactory factory,
                                          final KindTranslator translator)
  {
    super(factory, translator);
  }

  public MonolithicDiagnosabilityVerifier(final ProductDESProxy model,
                                          final ProductDESProxyFactory factory,
                                          final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Configuration
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  @Override
  public void setFaultClasses(final List<String> faultClasses)
  {
    mConfiguredFaultClasses = faultClasses;
  }

  @Override
  public List<String> getFaultClasses()
  {
    return mConfiguredFaultClasses;
  }

  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_DiagnosabilityChecker_FaultClasses);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_ModelAnalyzer_FinalStateLimit);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_ModelAnalyzer_FinalTransitionLimit);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_DiagnosabilityChecker_FaultClasses)) {
      final StringListOption listOption = (StringListOption) option;
      setFaultClasses(listOption.getValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalStateLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setNodeLimit(intOption.getIntValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalTransitionLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setTransitionLimit(intOption.getIntValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ProductDESProxy des = getModel();

    final Set<String> foundFaultClasses = new LinkedHashSet<>();
    for (final EventProxy event : des.getEvents()) {
      final String faultClass = getEventFaultClass(event);
      if (faultClass != null) {
        foundFaultClasses.add(faultClass);
      }
    }
    if (mConfiguredFaultClasses == null) {
      mUsedFaultClasses = new ArrayList<>(foundFaultClasses);
    } else if (foundFaultClasses.size() < mConfiguredFaultClasses.size()) {
      mUsedFaultClasses = new LinkedList<>(mConfiguredFaultClasses);
      final Logger logger = LogManager.getLogger();
      final ListIterator<String> iter = mUsedFaultClasses.listIterator();
      while (iter.hasNext()) {
        final String faultClass = iter.next();
        if (!foundFaultClasses.contains(faultClass)) {
          logger.warn("Fault class '{}' not found in model, ignored.",
                      faultClass);
          iter.remove();
        }
      }
    } else {
      mUsedFaultClasses = new ArrayList<>(mConfiguredFaultClasses);
    }

    mSynchronousProductBuilder = new TRSynchronousProductBuilder(des);
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      if (mUsedFaultClasses.isEmpty()) {
        return setSatisfiedResult();
      }

      mSynchronousProductBuilder.run();
      final TRSynchronousProductResult spResult =
        mSynchronousProductBuilder.getAnalysisResult();
      mSynchronousProduct = spResult.getComputedAutomaton();
      final EventEncoding enc = mSynchronousProduct.getEventEncoding();
      final ListBufferTransitionRelation rel =
        mSynchronousProduct.getTransitionRelation();
      iterA = rel.createSuccessorsReadOnlyIterator();
      iterB = rel.createSuccessorsReadOnlyIterator();
      final int numEvents = enc.getNumberOfProperEvents();
      final int numStates = rel.getNumberOfStates();
      mEventObservable = new boolean[numEvents];
      final TIntArrayList initStates = new TIntArrayList();
      for (int i = 0; i < numStates; i++) {
        if (rel.isInitial(i)) {
          initStates.add(i);
        }
      }
      for (int i = EventEncoding.NONTAU; i < numEvents; i++) {
        final EventProxy event = enc.getProperEvent(i);
        if (event != null) {
          mEventObservable[i] = event.isObservable();
        }
      }
      mFaultEvent = new boolean[numEvents];
      mStateIndexMap = new TLongIntHashMap(numStates, 0.5f, -1, -1);
      final StateProcessor processor = new VerifierStateProcessor();
      for (final String faultClass : mUsedFaultClasses) {
        boolean gotFault = false;
        for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
          final EventProxy event = enc.getProperEvent(e);
          final String eventFaultClass = getEventFaultClass(event);
          gotFault |= mFaultEvent[e] = faultClass.equals(eventFaultClass);
        }
        if (!gotFault) {
          continue;
        }
        mCurrentFaultClass = faultClass;
        mComponentStack.clear();
        mControlStack.clear();
        mStateIndexMap.clear();
        mIndexStateMap.clear();
        mNumberOfTransitions = 0;
        for (int x = 0; x < initStates.size(); x++) {
          for (int y = 0; y <= x; y++) {
            final int initA = initStates.get(y);
            final int initB = initStates.get(x);
            final long verifierInit = (((long) initA) << 32) | initB;
            lastVerInit = mIndexStateMap.size();
            mStateIndexMap.put(verifierInit, lastVerInit);
            mIndexStateMap.add(verifierInit);
            mControlStack.push(lastVerInit, lastVerInit);
          }
        }
        while (!mControlStack.isEmpty()) {
          checkAbort();
          final int i = mControlStack.getTopIndex();
          if (!mControlStack.isTopExpanded()) {
            final int dfsIndex = mComponentStack.size() | MSB1;
            mControlStack.setLink(i, dfsIndex);
            mControlStack.setTopIndex(dfsIndex);
            mComponentStack.add(i);
            expand(i, processor);
          } else {
            final int p = mControlStack.getTopParent();
            mControlStack.pop();
            if (!close(i, p)) {
              final CounterExampleProxy counterExample =
                computeCounterExample();
              addVerifierStatistics();
              return setFailedResult(counterExample);
            }
          }
        }
        addVerifierStatistics();
      }
      return setSatisfiedResult();
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUsedFaultClasses = null;
    mSynchronousProductBuilder = null;
  }

  @Override
  public DualCounterExampleProxy getCounterExample()
  {
    return (DualCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mSynchronousProductBuilder != null) {
      mSynchronousProductBuilder.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mSynchronousProductBuilder != null) {
      mSynchronousProductBuilder.resetAbort();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void expand(final int index, final StateProcessor process)
    throws OverflowException
  {
    final long state = mIndexStateMap.get(index);
    final int a = (int) (state >>> 32);
    final int b = (int) state;
    iterA.resetState(a & ~MSB1);
    while (iterA.advance()) {
      final int event = iterA.getCurrentEvent();
      final int targetA = iterA.getCurrentTargetState();
      if (!mEventObservable[event]) {
        final int newA, newB;
        if (mFaultEvent[event]) {
          newA = targetA | MSB1;
          newB = b;
        } else {
          newA = targetA | (a & MSB1);
          newB = b;
        }
        process.newState(newA, newB, index);
      } else {
        iterB.reset((b & ~MSB1), event);
        while (iterB.advance()) {
          final int targetB = iterB.getCurrentTargetState();
          final int newA = targetA | (a & MSB1);
          final int newB = targetB | (b & MSB1);
          process.newState(newA, newB, index);
        }
      }
    }
    iterA.resetState(b & ~MSB1);
    while (iterA.advance()) {
      final int event = iterA.getCurrentEvent();
      final int targetB = iterA.getCurrentTargetState();
      if (!mEventObservable[event]) {
        final int newA, newB;
        if (mFaultEvent[event]) {
          newA = a;
          newB = targetB | MSB1;
        } else {
          newA = a;
          newB = targetB | (b & MSB1);
        }
        process.newState(newA, newB, index);
      }
    }
  }

  private void expandBack(final int a, final int b,
                          final BackStateProcessor process)
    throws OverflowException
  {
    process.reset();
    int event;
    int sourceA, sourceB;
    int newA, newB;
    iterA.resetState(a & ~MSB1);
    while (iterA.advance()) {
      event = iterA.getCurrentEvent();
      sourceA = iterA.getCurrentSourceState();
      if (!mEventObservable[event]) {
        if (mFaultEvent[event]) {
          if ((a & MSB1) != 0) {
            newA = sourceA | MSB1;
            newB = b;
            process.newState(newA, newB, event);
            newA = sourceA;
            process.newState(newA, newB, event);
          }
        } else {
          newA = sourceA | (a & MSB1);
          newB = b;
          process.newState(newA, newB, event);
        }
      } else {
        iterB.reset((b & ~MSB1), event);
        while (iterB.advance()) {
          sourceB = iterB.getCurrentSourceState();
          newA = sourceA | (a & MSB1);
          newB = sourceB | (b & MSB1);
          process.newState(newA, newB, event);
        }
      }
    }
    iterA.resetState(b & ~MSB1);
    while (iterA.advance()) {
      event = iterA.getCurrentEvent();
      sourceB = iterA.getCurrentSourceState();
      if (!mEventObservable[event]) {
        if (mFaultEvent[event]) {
          if ((b & MSB1) != 0) {
            newA = a;
            newB = sourceB | MSB1;
            process.newState(newA, newB, event);
            newB = sourceB;
            process.newState(newA, newB, event);
          }
        } else {
          newA = a;
          newB = sourceB | (b & MSB1);
          process.newState(newA, newB, event);
        }
      }
    }
  }

  private boolean close(final int dfsIndex, final int parentIndex)
  {
    final int index = mComponentStack.get(dfsIndex);
    final int link = mControlStack.getLink(index);
    if ((link & ~MSB1) == dfsIndex) {
      final long pair = mIndexStateMap.get(index);
      final int a = (int) (pair >>> 32);
      final int b = (int) pair;
      mCurrentSCC.clear();
      int j = mComponentStack.removeAt(mComponentStack.size() - 1);
      mCurrentSCC.add(j);
      final int index1 = j;
      mControlStack.setLink(index1, -1);
      if (j != index) {
        do {
          j = mComponentStack.removeAt(mComponentStack.size() - 1);
          mCurrentSCC.add(j);
          final int index2 = j;
          mControlStack.setLink(index2, -1);
        } while (j != index);
        return (a & MSB1) == (b & MSB1);
      } else {
        if ((a & MSB1) == (b & MSB1)) {
          return true;
        } else {
          int event;
          iterA.resetState(a & ~MSB1);
          while (iterA.advance()) {
            if (iterA.getCurrentTargetState() == (a & ~MSB1)) {
              event = iterA.getCurrentEvent();
              if (!mEventObservable[event]) {
                return false;
              } else {
                iterB.reset(b & ~MSB1, event);
                while (iterB.advance()) {
                  if (iterB.getCurrentTargetState() == (b & ~MSB1)) {
                    return false;
                  }
                }
              }
            }
          }
          iterA.resetState(b & ~MSB1);
          while (iterA.advance()) {
            if (iterA.getCurrentTargetState() == (b & ~MSB1)) {
              event = iterA.getCurrentEvent();
              if (!mEventObservable[event]) {
                return false;
              }
            }
          }
        }
      }
    } else {
      final int parentLink = mControlStack.getLink(parentIndex);
      if ((link & ~MSB1) < (parentLink & ~MSB1)) {
        mControlStack.setLink(parentIndex, link);
      }
    }
    return true;
  }

  private CounterExampleProxy computeCounterExample()
    throws AnalysisException
  {
    mControlStack.resetLinks();
    mComponentStack.clear();
    bfsQueue.clear();
    System.gc();
    int sccRoot = 0;
    final StateProcessor processor = new BFSStateProcessor();
    final BackStateProcessor backProcessor = new BackStateProcessor();
    for (int i = 0; i <= lastVerInit; i++) {
      bfsQueue.add(i);
      final int index = i;
      final int value = bfsIndex;
      mControlStack.setLink(index, value);
      bfsIndex++;
    }
    while (!bfsQueue.isEmpty()) {
      checkAbort();
      final int current = bfsQueue.remove();
      if (mCurrentSCC.contains(current)) {
        sccRoot = current;
        break;
      }
      expand(current, processor);
    }
    bfsQueue.clear();
    expand(sccRoot, processor);
    while (!bfsQueue.isEmpty()) {
      checkAbort();
      final int current = bfsQueue.remove();
      if (current == sccRoot) {
        break;
      }
      expand(current, processor);
    }
    bfsQueue.clear();
    final ListBufferTransitionRelation rel =
      mSynchronousProduct.getTransitionRelation();
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    iterA = rel.createPredecessorsReadOnlyIterator();
    iterB = rel.createPredecessorsReadOnlyIterator();
    final List<TraceStepProxy> traceA = new ArrayList<TraceStepProxy>();
    final List<TraceStepProxy> traceB = new ArrayList<TraceStepProxy>();
    final long succState = mIndexStateMap.get(sccRoot);
    int succA = ((int) (succState >>> 32));
    int succB = ((int) succState);
    int event, predA, predB, loopIndexA, loopIndexB;
    do {
      checkAbort();
      expandBack(succA, succB, backProcessor);
      predA = backProcessor.predA;
      predB = backProcessor.predB;
      event = backProcessor.predEvent;
      assert event >= 0;
      if (mEventObservable[event]) {
        addStep((succA & ~MSB1), event, traceA);
        addStep((succB & ~MSB1), event, traceB);
      } else {
        if (succA != predA) {
          addStep((succA & ~MSB1), event, traceA);
        } else if (succB != predB) {
          addStep((succB & ~MSB1), event, traceB);
        }
      }
      succA = predA;
      succB = predB;
    } while (backProcessor.predIndex != sccRoot);
    loopIndexA = loopIndexB = 0;
    backProcessor.inSCC = false;
    while (backProcessor.predIndex > lastVerInit) {
      checkAbort();
      expandBack(succA, succB, backProcessor);
      predA = backProcessor.predA;
      predB = backProcessor.predB;
      event = backProcessor.predEvent;
      if (mEventObservable[event]) {
        addStep((succA & ~MSB1), event, traceA);
        addStep((succB & ~MSB1), event, traceB);
        loopIndexA++;
        loopIndexB++;
      } else {
        if (succA != predA) {
          addStep((succA & ~MSB1), event, traceA);
          loopIndexA++;
        } else if (succB != predB) {
          addStep((succB & ~MSB1), event, traceB);
          loopIndexB++;
        }
      }
      succA = predA;
      succB = predB;
    }
    addStep((succA & ~MSB1), -1, traceA);
    addStep((succB & ~MSB1), -1, traceB);
    if (loopIndexA == (traceA.size() - 1)) {
      loopIndexA = -1;
    }
    if (loopIndexB == (traceB.size() - 1)) {
      loopIndexB = -1;
    }
    final String nameA = "faulty";
    final String nameB = "non-faulty";
    final String ceName = getModel().getName() + "-undiagnosable";
    final String ceComment;
    if (mUsedFaultClasses.size() == 1 && mConfiguredFaultClasses == null) {
      ceComment = "The system is not diagnosable.";
    } else {
      ceComment = "The fault-class '" + mCurrentFaultClass +
        "' is not diagnosable.";
    }
    final TraceProxy tpA =
      getFactory().createTraceProxy(nameA, traceA, loopIndexA);
    final TraceProxy tpB =
      getFactory().createTraceProxy(nameB, traceB, loopIndexB);
    final TRSynchronousProductResult spResult =
      mSynchronousProductBuilder.getAnalysisResult();
    final SynchronousProductStateMap stateMap = spResult.getStateMap();
    final DualCounterExampleProxy counterExample = getFactory()
      .createDualCounterExampleProxy(ceName, ceComment, null, getModel(),
                                     stateMap.getInputAutomata(), tpA, tpB);
    return counterExample;
  }

  private void addStep(final int stateIndex, final int eventIndex,
                       final List<TraceStepProxy> trace)
  {
    final TRSynchronousProductResult spResult =
      mSynchronousProductBuilder.getAnalysisResult();
    final SynchronousProductStateMap stateMap = spResult.getStateMap();
    final Map<AutomatonProxy,StateProxy> autStateMap = new HashMap<>();
    final Collection<AutomatonProxy> auts = stateMap.getInputAutomata();
    final StateProxy state = mSynchronousProduct.getState(stateIndex);
    final EventProxy event;
    if (eventIndex != -1) {
      final EventEncoding enc = mSynchronousProduct.getEventEncoding();
      event = enc.getProperEvent(eventIndex);
    } else {
      event = null;
    }
    for (final AutomatonProxy a : auts) {
      final StateProxy s = stateMap.getOriginalState(state, a);
      autStateMap.put(a, s);
    }
    final TraceStepProxy step =
      getFactory().createTraceStepProxy(event, autStateMap);
    trace.add(0, step);
  }

  private String getEventFaultClass(final EventProxy event)
  {
    final Map<String,String> attribs = event.getAttributes();
    return attribs.get(DiagnosabilityAttributeFactory.FAULT_KEY);
  }

  private void addVerifierStatistics()
  {
    final DefaultVerificationResult result =
      (DefaultVerificationResult) getAnalysisResult();
    result.updateNumberOfStates(mIndexStateMap.size());
    result.updateNumberOfTransitions(mNumberOfTransitions);
  }

  private static long makePair(final int a, final int b)
  {
    if (a > b) {
      return (((long) b) << 32) | (a & 0xffffffffL);
    } else {
      return (((long) a) << 32) | (b & 0xffffffffL);
    }
  }


  //#########################################################################
  //# Debugging
  @SuppressWarnings("unused")
  private String showVerifierStateSpace()
  {
    final StringBuilder builder = new StringBuilder();
    final int numStates = mStateIndexMap.size();
    boolean first = true;
    for (int s = 0; s < numStates; s++) {
      if (first) {
        first = false;
      } else {
        builder.append(", ");
      }
      dumpIndexedPair(s, builder);
    }
    return builder.toString();
  }

  @SuppressWarnings("unused")
  private String showCurrentSCC()
  {
    final StringBuilder builder = new StringBuilder();
    dumpSCC(mCurrentSCC, builder);
    return builder.toString();
  }

  private void dumpSCC(final TIntHashSet scc, final StringBuilder builder)
  {
    boolean first = true;
    final TIntIterator iter = scc.iterator();
    while (iter.hasNext()) {
      if (first) {
        first = false;
      } else {
        builder.append(", ");
      }
      final int index = iter.next();
      dumpIndexedPair(index, builder);
    }
  }

  @SuppressWarnings("unused")
  private void printTransition(final int source, final int target)
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("  ");
    dumpTransition(source, target, builder);
    System.err.println(builder.toString());
  }

  private void dumpTransition(final int source,
                              final int target,
                              final StringBuilder builder)
  {
    dumpIndexedPair(source, builder);
    builder.append(" -> ");
    dumpIndexedPair(target, builder);
  }

  @SuppressWarnings("unused")
  private String showIndexedPair(final int index)
  {
    final StringBuilder builder = new StringBuilder();
    dumpIndexedPair(index, builder);
    return builder.toString();
  }

  private void dumpIndexedPair(final int index, final StringBuilder builder)
  {
    builder.append(index);
    builder.append("(");
    final long pair = mIndexStateMap.get(index);
    dumpState((int) (pair & 0x7fffffffL), builder);
    builder.append(',');
    dumpState((int) (pair >> 32), builder);
    builder.append(')');
  }

  private void dumpState(final int code, final StringBuilder builder)
  {
    if ((code & 0x80000000) == 0) {
      builder.append(code);
      builder.append('N');
    } else {
      builder.append(code & 0x7fffffff);
      builder.append('F');
    }
  }


  //#########################################################################
  //# Inner Interface StateProcessor
  abstract interface StateProcessor
  {
    public void newState(final int a, final int b, final int parentIndex)
      throws OverflowException;
  }


  //#########################################################################
  //# Inner Class VerifierStateProcessor
  private class VerifierStateProcessor implements StateProcessor
  {
    @Override
    public void newState(final int a, final int b, final int parentIndex)
      throws OverflowException
    {
      mNumberOfTransitions++;
      if (mNumberOfTransitions++ >= getTransitionLimit()) {
        throw new OverflowException(OverflowKind.TRANSITION, getTransitionLimit());
      }
      final long next = makePair(a, b);
      if (a >= 0 || b >= 0) {
        int index = mStateIndexMap.get(next);
        if (index < 0) {
          index = mIndexStateMap.size();
          mStateIndexMap.put(next, index);
          mIndexStateMap.add(next);
          if (index >= getNodeLimit()) {
            throw new OverflowException(OverflowKind.STATE, getNodeLimit());
          }
          mControlStack.push(index, parentIndex);
        } else {
          final int link = mControlStack.getLink(index);
          if ((link & MSB1) == 0) {
            mControlStack.moveToTop(link, parentIndex);
          } else if (link != -1) {
            final int parentLink = mControlStack.getLink(parentIndex);
            if ((link & ~MSB1) < (parentLink & ~MSB1)) {
              mControlStack.setLink(parentIndex, link);
            }
          }
        }
      }
    }
  }


  //#########################################################################
  //# Inner Class BFSStateProcessor
  private class BFSStateProcessor implements StateProcessor
  {
    //#########################################################################
    //# Interface StateProcessor
    @Override
    public void newState(final int a, final int b, final int parentIndex)
      throws OverflowException
    {
      long next;
      if (a > b) {
        next = (((long) b) << 32) | (a & 0xffffffffL);
      } else {
        next = (((long) a) << 32) | (b & 0xffffffffL);
      }
      final int index = mStateIndexMap.get(next);
      if (!inSCC) {
        if (mStateIndexMap.containsKey(next)) {
          if (mCurrentSCC.contains(index)) {
            bfsQueue.clear();
            bfsQueue.add(index);
            inSCC = true;
          }
          if (mControlStack.getLink(index) == -1) {
            bfsQueue.add(index);
            final int value = bfsIndex;
            mControlStack.setLink(index, value);
            bfsIndex++;
          }
        }
      } else {
        if (mCurrentSCC.contains(index)) {
          if (mControlStack.getLink(index) == -1) {
            bfsQueue.add(index);
            final int value = bfsIndex;
            mControlStack.setLink(index, value);
            bfsIndex++;
          }
        }
      }
    }

    //#########################################################################
    //# Data Members
    private boolean inSCC = false;
  }


  //#########################################################################
  //# Inner Class BackStateProcessor
  private class BackStateProcessor implements StateProcessor
  {
    private boolean inSCC = true;
    private int predA = -1;
    private int predB = -1;
    private int predEvent = -1;
    private int predIndex = -1;

    @Override
    public void newState(final int a, final int b, final int event)
      throws OverflowException
    {
      long next;
      if (a > b) {
        next = (((long) b) << 32) | (a & 0xffffffffL);
      } else {
        next = (((long) a) << 32) | (b & 0xffffffffL);
      }
      final int index = mStateIndexMap.get(next);
      final boolean newInMap = mStateIndexMap.contains(next);
      final boolean newInSCC = mCurrentSCC.contains(index);
      if ((!inSCC && newInMap) || (inSCC && newInSCC)) {
        if (mControlStack.getLink(index) != -1) {
          if (predIndex == -1) {
            predIndex = index;
            predEvent = event;
            predA = a;
            predB = b;
          } else {
            if (mControlStack.getLink(index) <
                mControlStack.getLink(predIndex)) {
              predIndex = index;
              predEvent = event;
              predA = a;
              predB = b;
            }
          }
        }
      }
    }

    public void reset()
    {
      predA = predB = predIndex = -1;
    }
  }


  //#########################################################################
  //# Instance Variables
  private List<String> mConfiguredFaultClasses = null;
  private List<String> mUsedFaultClasses = null;

  private boolean[] mEventObservable;
  private boolean[] mFaultEvent;
  private String mCurrentFaultClass;
  private TLongIntHashMap mStateIndexMap;
  private final TLongArrayList mIndexStateMap =
    new TLongArrayList(INITIAL_STACK_SIZE);
  private final TarjanControlStack mControlStack =
    new TarjanControlStack(INITIAL_STACK_SIZE);
  private final TIntArrayList mComponentStack =
    new TIntArrayList(INITIAL_STACK_SIZE);
  private final TIntHashSet mCurrentSCC = new TIntHashSet();
  private final ArrayDeque<Integer> bfsQueue = new ArrayDeque<Integer>();
  private TRSynchronousProductBuilder mSynchronousProductBuilder;
  private TRAutomatonProxy mSynchronousProduct;
  private TransitionIterator iterA;
  private TransitionIterator iterB;
  private int lastVerInit;
  private int mNumberOfTransitions;
  private int bfsIndex = 0;


  //#########################################################################
  //# Class Constants
  private static final int MSB1 = 0x80000000;
  private static final int INITIAL_STACK_SIZE = 1024;

}
