//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductBuilder;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductResult;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.des.DiagnosabilityChecker;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.DualCounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * @author Nicholas McGrath
 */
public class MonolithicDiagnosabilityVerifier extends AbstractModelVerifier
  implements DiagnosabilityChecker
{

  //#########################################################################
  //# Constructors
  public MonolithicDiagnosabilityVerifier(final ProductDESProxyFactory factory)
  {
    this(factory, IdenticalKindTranslator.getInstance());
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


  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final TRSynchronousProductBuilder spBuilder =
        new TRSynchronousProductBuilder(getModel());
      spBuilder.run();
      final TRSynchronousProductResult spResult =
        spBuilder.getAnalysisResult();
      sp = spResult.getComputedAutomaton();
      spEvents = sp.getEventEncoding();
      mStateMap = spResult.getStateMap();
      rel = sp.getTransitionRelation();
      iterA = rel.createSuccessorsReadOnlyIterator();
      iterB = rel.createSuccessorsReadOnlyIterator();
      final int numEvents = spEvents.getNumberOfProperEvents();
      final int numStates = rel.getNumberOfStates();
      eventObservable = new boolean[numEvents];
      final THashSet<String> faultClasses = new THashSet<>();
      final TIntArrayList initStates = new TIntArrayList();
      for (int i = 0; i < numStates; i++) {
        if (rel.isInitial(i)) {
          initStates.add(i);
        }
      }
      for (int i = EventEncoding.NONTAU; i < numEvents; i++) {
        final EventProxy event = spEvents.getProperEvent(i);
        if (event != null) {
          final String faultClass = getEventFaultClass(event);
          if (faultClass != null) {
            faultClasses.add(faultClass);
          }
          if (event.isObservable()) {
            eventObservable[i] = true;
          } else {
            eventObservable[i] = false;
          }
        }
      }
      mNumberOfFaultClasses = faultClasses.size();
      if (!faultClasses.isEmpty()) {
        mFaultEvent = new boolean[numEvents];
        mStateIndexMap = new TLongIntHashMap(numStates, 0.5f, -1, -1);
        final StateProcessor processor = new VerifierStateProcessor();
        for (final String faultClass : faultClasses) {
          mCurrentFaultClass = faultClass;
          for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
            final EventProxy event = spEvents.getProperEvent(e);
            final String eventFaultClass = getEventFaultClass(event);
            mFaultEvent[e] = faultClass.equals(eventFaultClass);
          }
          mComponentStack.clear();
          mControlStack.clear();
          mLinks.clear();
          mStateIndexMap.clear();
          mIndexStateMap.clear();
          for (int x = 0; x < initStates.size(); x++) {
            for (int y = 0; y <= x; y++) {
              final int initA = initStates.get(y);
              final int initB = initStates.get(x);
              final long verifierInit = (((long) initA) << 32) | initB;
              lastVerInit = getNumberOfStatePairs();
              mStateIndexMap.put(verifierInit, lastVerInit);
              mIndexStateMap.add(verifierInit);
              mControlStack.push(lastVerInit, lastVerInit);
            }
          }
          while (!mControlStack.isEmpty()) {
            final int i = mControlStack.getTopIndex();
            if (!mControlStack.isTopExpanded()) {
              final int dfsIndex = mComponentStack.size() | MSB1;
              setLink(i, dfsIndex);
              mControlStack.setTopIndex(dfsIndex);
              mComponentStack.add(i);
              expand(i, processor);
            } else {
              final int p = mControlStack.getTopParent();
              mControlStack.pop();
              if (!close(i, p)) {
                final CounterExampleProxy counterExample =
                  computeCounterExample();
                return setFailedResult(counterExample);
              }
            }
          }
        }
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
    return setSatisfiedResult();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelVerifier
  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    final int numPairs = getNumberOfStatePairs();
    result.setNumberOfStates(numPairs);
    result.setPeakNumberOfStates(numPairs);
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
      if (!eventObservable[event]) {
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
      if (!eventObservable[event]) {
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
      if (!eventObservable[event]) {
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
      if (!eventObservable[event]) {
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
    final int link = mLinks.get(index);
    if ((link & ~MSB1) == dfsIndex) {
      final long pair = mIndexStateMap.get(index);
      final int a = (int) (pair >>> 32);
      final int b = (int) pair;
      mCurrentSCC.clear();
      int j = mComponentStack.removeAt(mComponentStack.size() - 1);
      mCurrentSCC.add(j);
      setLink(j, -1);
      if (j != index) {
        do {
          j = mComponentStack.removeAt(mComponentStack.size() - 1);
          mCurrentSCC.add(j);
          setLink(j, -1);
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
              if (!eventObservable[event]) {
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
              if (!eventObservable[event]) {
                return false;
              }
            }
          }
        }
      }
    } else {
      final int parentLink = mLinks.get(parentIndex);
      if ((link & ~MSB1) < (parentLink & ~MSB1)) {
        setLink(parentIndex, link);
      }
    }
    return true;
  }

  private CounterExampleProxy computeCounterExample() throws OverflowException
  {
    mControlStack.clear();
    mComponentStack.clear();
    bfsQueue.clear();
    System.gc();
    int sccRoot = 0;
    final StateProcessor processor = new BFSStateProcessor();
    final BackStateProcessor backProcessor = new BackStateProcessor();
    for (int i = 0; i < mLinks.size(); i++) {
      setLink(i, -1);
    }
    for (int i = 0; i <= lastVerInit; i++) {
      bfsQueue.add(i);
      setLink(i, bfsIndex);
      bfsIndex++;
    }
    while (!bfsQueue.isEmpty()) {
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
      final int current = bfsQueue.remove();
      if (current == sccRoot) {
        break;
      }
      expand(current, processor);
    }
    bfsQueue.clear();
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
      expandBack(succA, succB, backProcessor);
      predA = backProcessor.predA;
      predB = backProcessor.predB;
      event = backProcessor.predEvent;
      assert event >= 0;
      if (eventObservable[event]) {
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
      expandBack(succA, succB, backProcessor);
      predA = backProcessor.predA;
      predB = backProcessor.predB;
      event = backProcessor.predEvent;
      if (eventObservable[event]) {
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
    if (mNumberOfFaultClasses == 1) {
      ceComment = "The system is not diagnosable.";
    } else {
      ceComment = "The fault-class '" + mCurrentFaultClass +
        "' is not diagnosable.";
    }
    final TraceProxy tpA =
      getFactory().createTraceProxy(nameA, traceA, loopIndexA);
    final TraceProxy tpB =
      getFactory().createTraceProxy(nameB, traceB, loopIndexB);
    final DualCounterExampleProxy counterExample = getFactory()
      .createDualCounterExampleProxy(ceName, ceComment, null, getModel(),
                                     mStateMap.getInputAutomata(), tpA, tpB);
    return counterExample;
  }

  private void addStep(final int stateIndex, final int eventIndex,
                       final List<TraceStepProxy> trace)
  {
    final Map<AutomatonProxy,StateProxy> autStateMap =
      new HashMap<AutomatonProxy,StateProxy>();
    final Collection<AutomatonProxy> auts = mStateMap.getInputAutomata();
    final StateProxy state = sp.getState(stateIndex);
    final EventProxy event;
    if (eventIndex != -1) {
      event = spEvents.getProperEvent(eventIndex);
    } else {
      event = null;
    }
    for (final AutomatonProxy a : auts) {
      final StateProxy s = mStateMap.getOriginalState(state, a);
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

  private int getNumberOfStatePairs()
  {
    return mIndexStateMap.size();
  }

  private void setLink(final int index, final int value)
  {
    mLinks.set(index, value);
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
      final long next = makePair(a, b);
      if (a >= 0 || b >= 0) {
        int index = mStateIndexMap.get(next);
        if (index < 0) {
          index = getNumberOfStatePairs();
          mStateIndexMap.put(next, index);
          mIndexStateMap.add(next);
          if (index + 1 >= getNodeLimit()) {
            throw new OverflowException(OverflowKind.STATE, getNodeLimit());
          }
          mControlStack.push(index, parentIndex);
        } else {
          final int link = mLinks.get(index);
          if ((link & MSB1) == 0) {
            mControlStack.moveToTop(link, parentIndex);
          } else if (link != -1) {
            final int parentLink = mLinks.get(parentIndex);
            if ((link & ~MSB1) < (parentLink & ~MSB1)) {
              setLink(parentIndex, link);
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
    private boolean inSCC = false;

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
          if (mLinks.get(index) == -1) {
            bfsQueue.add(index);
            setLink(index, bfsIndex);
            bfsIndex++;
          }
        }
      } else {
        if (mCurrentSCC.contains(index)) {
          if (mLinks.get(index) == -1) {
            bfsQueue.add(index);
            setLink(index, bfsIndex);
            bfsIndex++;
          }
        }
      }
    }
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
        if (mLinks.get(index) != -1) {
          if (predIndex == -1) {
            predIndex = index;
            predEvent = event;
            predA = a;
            predB = b;
          } else {
            if (mLinks.get(index) < mLinks.get(predIndex)) {
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
  //# Inner Class ControlStack
  /**
   * <P>The control stack for the iterative version of Tarjan's algorithm.</P>
   *
   * <P>Control stack entries are identified as integers representing an
   * index in an array list that holds the stack data. This is called the
   * <I>stack index</I>, which refers to a block of three  consecutive
   * integers:</P>
   * <OL>
   * <LI>The stack index of the next entry below on the stack,
   *     or -1 for the bottom-most entry.</LI>
   * <LI>The state index, which for unexpanded state pairs is the true state
   *     pair index, and for expanded state pairs its position on the component
   *     stack. Expanded state pairs are tagged by setting the {@link
   *     MonolithicDiagnosabilityVerifier#MSB1 MSB1}.
   *     For unexpanded state pairs, the link entry in {@link
   *     MonolithicDiagnosabilityVerifier#mLinks mLinks} contains the stack
   *     index of the entry above their entry on the stack.</LI>
   * <LI>The parent index, which is the true state pair index of the
   *     state pair from where the current stack entry is being expanded.</LI>
   * </OL>
   * <P>The stack always contains a dummy top entry ({@link #mDummyTop}),
   * with the entry below it ({@link #mUsedTop}) being the first entry
   * to actually contain data. Unused stack entries are collected in a
   * linked list of free entries ({@link #mNextFree}).</P>
   */
  private class ControlStack
  {
    //#######################################################################
    //# Constructor
    /**
     * Creates an empty control stack.
     */
    private ControlStack()
    {
      mStack = new TIntArrayList();
      clear();
    }

    //#######################################################################
    //# Stack Access
    /**
     * Resets the control stack to be empty.
     */
    private void clear()
    {
      mStack.clear();
      mStack.add(-1);
      mStack.add(-1);
      mStack.add(-1);
      mDummyTop = 0;
      mUsedTop = mNextFree = -1;
    }

    /**
     * Adds a new entry to the top of the stack.
     * @param  index  The state index to be stored in the new entry.
     * @param  parent The parent index to be stored in the new entry.
     */
    private void push(final int index, final int parent)
    {
      final int newTop = allocateEntry();
      setStackLink(newTop, mDummyTop);
      setStackIndex(mDummyTop, index);
      setStackParent(mDummyTop, parent);
      mLinks.add(newTop);
      mUsedTop = mDummyTop;
      mDummyTop = newTop;
    }

    /**
     * Removes the top-most entry from the stack.
     */
    private void pop()
    {
      final int newTop = getStackLink(mUsedTop);
      assert newTop != mUsedTop;
      setStackLink(mDummyTop, mNextFree);
      mNextFree = mDummyTop;
      mDummyTop = mUsedTop;
      mUsedTop = newTop;
    }

    /**
     * Changes the given stack entry to become the new top. This method
     * rearranges the stack to the new order and also updates any {@link
     * MonolithicDiagnosabilityVerifier#mLinks mLinks} entries to point to
     * the new predecessors of stack entries affected by the move.
     * @param  stackPosAbove  The stack index of a stack entry above
     *                        the stack entry to become the new top.
     * @param  newParent      A new parent index to be stored in the
     *                        new top of the stack after the move.
     */
    private void moveToTop(final int stackPosAbove, final int newParent)
    {
      if (stackPosAbove != mDummyTop) {
        final int pos = getStackLink(stackPosAbove);
        final int stackPosBelow = getStackLink(pos);
        setStackParent(pos, newParent);
        setStackLink(stackPosAbove, stackPosBelow);
        setStackLink(pos, mUsedTop);
        setStackLink(mDummyTop, pos);
        final int index = getStackIndex(pos);
        setLink(index, mDummyTop);
        final int indexBelow = getStackIndex(stackPosBelow);
        if ((indexBelow & MSB1) == 0) {
          setLink(indexBelow, stackPosAbove);
        }
        final int oldTopIndex = getStackIndex(mUsedTop);
        if ((oldTopIndex & MSB1) == 0) {
          setLink(oldTopIndex, pos);
        }
        mUsedTop = pos;
      }
    }

    /**
     * Returns whether the stack is empty.
     */
    private boolean isEmpty()
    {
      return mUsedTop < 0;
    }

    /**
     * Returns whether the top-most entry of the stack is flagged as
     * expanded.
     * @return <CODE>true</CODE> if the {@link
     *         MonolithicDiagnosabilityVerifier#MSB1 MSB1} of the state
     *         index of the top entry is set.
     * @throws IndexOutOfBoundsException if the stack is empty.
     */
    private boolean isTopExpanded()
    {
      return (getStackIndex(mUsedTop) & MSB1) != 0;
    }

    /**
     * Retrieves the state index of the top-most entry of the stack,
     * without the {@link MonolithicDiagnosabilityVerifier#MSB1 MSB1}.
     * @throws IndexOutOfBoundsException if the stack is empty.
     */
    private int getTopIndex()
    {
      return getStackIndex(mUsedTop) & ~MSB1;
    }

    /**
     * Retrieves the parent index of the top-most entry of the stack.
     * @throws IndexOutOfBoundsException if the stack is empty.
     */
    private int getTopParent()
    {
      return getStackParent(mUsedTop);
    }

    /**
     * Retrieves the parent index of the top-most entry of the stack.
     * @throws IndexOutOfBoundsException if the stack is empty.
     */
    private void setTopIndex(final int entry)
    {
      setStackIndex(mUsedTop, entry);
    }

    //#######################################################################
    //# Indexing
    /**
     * Retrieves the stack link, which refers to the next entry below the
     * stack given stack entry.
     * @param  pos    The stack index of the entry to be checked.
     */
    private int getStackLink(final int pos)
    {
      return mStack.get(pos);
    }

    /**
     * Retrieves the state index of the stack given stack entry,
     * including its {@link MonolithicDiagnosabilityVerifier#MSB1 MSB1}.
     * @param  pos    The stack index of the entry to be checked.
     */
    private int getStackIndex(final int pos)
    {
      return mStack.get(pos + 1);
    }

    /**
     * Retrieves the parent index of the stack given stack entry.
     * @param  pos    The stack index of the entry to be checked.
     */
    private int getStackParent(final int pos)
    {
      return mStack.get(pos + 2);
    }

    /**
     * Sets the stack link, which refers to the next entry below the
     * stack given stack entry.
     * @param  pos    The stack index of the entry to be updated.
     * @param  value  The stack index of the new entry below.
     */
    private void setStackLink(final int pos, final int value)
    {
      mStack.set(pos, value);
    }

    /**
     * Sets the state index of the stack given stack entry.
     * @param  pos    The stack index of the entry to be updated.
     * @param  index  The new state index, including its {@link
     *                MonolithicDiagnosabilityVerifier#MSB1 MSB1}.
     */
    private void setStackIndex(final int pos, final int index)
    {
      mStack.set(pos + 1, index);
    }

    /**
     * Sets the parent index of the stack given stack entry.
     * @param  pos    The stack index of the entry to be updated.
     * @param  parent The new parent index.
     */
    private void setStackParent(final int pos, final int parent)
    {
      mStack.set(pos + 2, parent);
    }

    //#######################################################################
    //# Free Nodes
    /**
     * Allocates a stack entry. This method either enlarges the stack or
     * updates and returns the next available entry from the list of free
     * entry.
     */
    private int allocateEntry()
    {
      if (mNextFree >= 0) {
        final int free = mNextFree;
        mNextFree = getStackLink(free);
        return free;
      } else {
        final int free = mStack.size();
        mStack.add(-1);
        mStack.add(-1);
        mStack.add(-1);
        return free;
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      boolean first = true;
      for (int i = 0; i < mStack.size(); i+= 3) {
        if (first) {
          first = false;
        } else {
          builder.append('\n');
        }
        builder.append(i);
        builder.append(": (");
        final int value = mStack.get(i+1);
        if (value == -1) {
          builder.append(value);
        } else {
          builder.append(value & ~MSB1);
          if ((value & MSB1) != 0) {
            builder.append('*');
          }
        }
        builder.append(',');
        builder.append(mStack.get(i+2));
        builder.append(") -> ");
        builder.append(mStack.get(i));
        if (i == mDummyTop) {
          builder.append(" @dummy");
        } else if (i == mUsedTop) {
          builder.append(" @top");
        } else if (i == mNextFree) {
          builder.append(" @free");
        }
      }
      return builder.toString();
    }

    @SuppressWarnings("unused")
    private void checkIntegrity()
    {
      for (int i = 0; i < mLinks.size(); i++) {
        final int link = mLinks.get(i);
        if ((link & MSB1) == 0) {
          final int stackPosAbove = link;
          final int stackPos = getStackLink(stackPosAbove);
          final int index = getStackIndex(stackPos);
          assert index == i;
        }
      }
    }

    //#######################################################################
    //# Instance Variables
    /**
     * Array list containing stack data.
     */
    private final TIntArrayList mStack;
    /**
     * A fake stack top entry. The dummy top is always defined and can be
     * used as the reference to the entry above any new item pushed on the
     * stack (which needs to be stored in {@link
     * MonolithicDiagnosabilityVerifier#mLinks mLinks}). The stack link
     * of <CODE>mDummyTop</CODE> always points to {@link #mUsedTop}.
     */
    private int mDummyTop;
    /**
     * The top-most stack entry that holds data, or -1 if the stack is
     * empty.
     */
    private int mUsedTop;
    /**
     * The next available unused stack entry, or -1 if all stack entries are
     * in use. If a free entry is available, its stack link refers to the
     * next available free entry.
     */
    private int mNextFree;
  }


  //#########################################################################
  //# Instance Variables
  private boolean[] eventObservable;
  private boolean[] mFaultEvent;
  private int mNumberOfFaultClasses;
  private String mCurrentFaultClass;
  private int bfsIndex = 0;
  private TLongIntHashMap mStateIndexMap;
  private final TLongArrayList mIndexStateMap = new TLongArrayList();
  private final ControlStack mControlStack = new ControlStack();
  private final TIntArrayList mComponentStack = new TIntArrayList();
  private final TIntArrayList mLinks = new TIntArrayList();
  private final TIntHashSet mCurrentSCC = new TIntHashSet();
  private final ArrayDeque<Integer> bfsQueue = new ArrayDeque<Integer>();
  private ListBufferTransitionRelation rel;
  private SynchronousProductStateMap mStateMap;
  private TRAutomatonProxy sp;
  private EventEncoding spEvents;
  private TransitionIterator iterA;
  private TransitionIterator iterB;
  private int lastVerInit;


  //#########################################################################
  //# Class Constants
  private static final int MSB1 = 0x80000000;

}
