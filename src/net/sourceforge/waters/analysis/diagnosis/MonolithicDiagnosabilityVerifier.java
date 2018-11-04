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

import gnu.trove.iterator.hash.TObjectHashIterator;
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
      eventFaultClass = new String[numEvents];
      final THashSet<String> faultClasses = new THashSet<String>();
      final TIntArrayList initStates = new TIntArrayList();
      for (int i = 0; i < numStates; i++) {
        if (rel.isInitial(i)) {
          initStates.add(i);
        }
      }
      String faultLabel;
      Map<String,String> attrib;
      EventProxy event;
      for (int i = EventEncoding.NONTAU; i < numEvents; i++) {
        event = spEvents.getProperEvent(i);
        if (event != null) {
          attrib = event.getAttributes();
          faultLabel = attrib.get(DiagnosabilityAttributeFactory.FAULT_KEY);
          if (faultLabel != null) {
            faultClasses.add(faultLabel);
          }
          eventFaultClass[i] = faultLabel;
          if (event.isObservable()) {
            eventObservable[i] = true;
          } else {
            eventObservable[i] = false;
          }
        }
      }
      if (!faultClasses.isEmpty()) {
        final TObjectHashIterator<String> faultClassIter =
          faultClasses.iterator();
        final StateProcessor processor = new VerifierStateProcessor();
        while (faultClassIter.hasNext()) {
          faultClass = faultClassIter.next();
          mComponentStack.clear();
          mControlStack.clear();
          mLinks.clear();
          mStateIndexMap.clear();
          mIndexStateMap.clear();
          stateCount = 0;
          for (int x = 0; x < initStates.size(); x++) {
            for (int y = 0; y <= x; y++) {
              final int initA = initStates.get(y);
              final int initB = initStates.get(x);
              final long verifierInit = (((long) initA) << 32) | initB;
              mStateIndexMap.put(verifierInit, stateCount);
              mIndexStateMap.add(verifierInit);
              mControlStack.push(stateCount, stateCount);
              lastVerInit = stateCount;
              stateCount++;
            }
          }
          while (!mControlStack.isEmpty()) {
            final int i = mControlStack.getTopIndex();
            final int p = mControlStack.getTopParent();
            if (!mControlStack.isTopExpanded()) {
              mControlStack.setTopIndex(mComponentStack.size() | MSB1);
              mLinks.set(i, (mComponentStack.size() | MSB1));
              mComponentStack.add(i);
              expand(i, processor);
            } else {
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
    } catch (final OverflowException overflow) {
      throw setExceptionResult(overflow);
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
    return setSatisfiedResult();
  }

  private void expand(final int index, final StateProcessor process)
    throws OverflowException
  {
    final long state = mIndexStateMap.get(index);
    final int a = (int) (state >>> 32);
    final int b = (int) state;
    int event;
    int targetA, targetB;
    int newA, newB;
    iterA.resetState(a & MSB_MASK);
    while (iterA.advance()) {
      event = iterA.getCurrentEvent();
      targetA = iterA.getCurrentTargetState();
      if (!eventObservable[event]) {
        if (faultClass.equals(eventFaultClass[event])) {
          newA = targetA | MSB1;
          newB = b;
        } else {
          newA = targetA | (a & MSB1);
          newB = b;
        }
        process.newState(newA, newB, index);
      } else {
        iterB.reset((b & MSB_MASK), event);
        while (iterB.advance()) {
          targetB = iterB.getCurrentTargetState();
          if (!faultClass.equals(eventFaultClass[event])) {
            newA = targetA | (a & MSB1);
            newB = targetB | (b & MSB1);
            process.newState(newA, newB, index);
          }
        }
      }
    }
    iterA.resetState(b & MSB_MASK);
    while (iterA.advance()) {
      event = iterA.getCurrentEvent();
      targetB = iterA.getCurrentTargetState();
      if (!eventObservable[event]) {
        if (faultClass.equals(eventFaultClass[event])) {
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
    iterA.resetState(a & MSB_MASK);
    while (iterA.advance()) {
      event = iterA.getCurrentEvent();
      sourceA = iterA.getCurrentSourceState();
      if (!eventObservable[event]) {
        if (faultClass.equals(eventFaultClass[event])) {
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
        iterB.reset((b & MSB_MASK), event);
        while (iterB.advance()) {
          sourceB = iterB.getCurrentSourceState();
          if (!faultClass.equals(eventFaultClass[event])) {
            newA = sourceA | (a & MSB1);
            newB = sourceB | (b & MSB1);
            process.newState(newA, newB, event);
          }
        }
      }
    }
    iterA.resetState(b & MSB_MASK);
    while (iterA.advance()) {
      event = iterA.getCurrentEvent();
      sourceB = iterA.getCurrentSourceState();
      if (!eventObservable[event]) {
        if (faultClass.equals(eventFaultClass[event])) {
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
    final long state = mIndexStateMap.get(index);
    final int a = (int) (state >>> 32);
    final int b = (int) state;
    if ((mLinks.get(index) & MSB_MASK) == dfsIndex) {
      scc.clear();
      int j = mComponentStack.removeAt(mComponentStack.size() - 1);
      scc.add(j);
      mLinks.set(j, -1);
      if (j != index) {
        do {
          j = mComponentStack.removeAt(mComponentStack.size() - 1);
          scc.add(j);
          mLinks.set(j, -1);
        } while (j != index);
        if ((a & MSB1) == (b & MSB1)) {
          return true;
        } else {
          return false;
        }
      } else {
        if ((a & MSB1) == (b & MSB1)) {
          return true;
        } else {
          int event;
          iterA.resetState(a & MSB_MASK);
          while (iterA.advance()) {
            if (iterA.getCurrentTargetState() == (a & MSB_MASK)) {
              event = iterA.getCurrentEvent();
              if (!eventObservable[event]) {
                return false;
              } else {
                iterB.reset((b & MSB_MASK), event);
                while (iterB.advance()) {
                  if (iterB.getCurrentTargetState() == (b & MSB_MASK)) {
                    return false;
                  }
                }
              }
            }
          }
          iterA.resetState(b & MSB_MASK);
          while (iterA.advance()) {
            if (iterA.getCurrentTargetState() == (b & MSB_MASK)) {
              event = iterA.getCurrentEvent();
              if (!eventObservable[event]) {
                return false;
              }
            }
          }
        }
      }
    } else {
      int newParentLink = Math.min((mLinks.get(parentIndex) & MSB_MASK),
                                   (mLinks.get(index) & MSB_MASK));
      newParentLink |= (mLinks.get(parentIndex) & MSB1);
      mLinks.set(parentIndex, newParentLink);
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
      mLinks.set(i, -1);
    }
    for (int i = 0; i <= lastVerInit; i++) {
      bfsQueue.add(i);
      mLinks.set(i, bfsIndex);
      bfsIndex++;
    }
    while (!bfsQueue.isEmpty()) {
      final int current = bfsQueue.remove();
      if (scc.contains(current)) {
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
      if (eventObservable[event]) {
        addStep((succA & MSB_MASK), event, traceA);
        addStep((succB & MSB_MASK), event, traceB);
      } else {
        if (succA != predA) {
          addStep((succA & MSB_MASK), event, traceA);
        } else if (succB != predB) {
          addStep((succB & MSB_MASK), event, traceB);
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
        addStep((succA & MSB_MASK), event, traceA);
        addStep((succB & MSB_MASK), event, traceB);
        loopIndexA++;
        loopIndexB++;
      } else {
        if (succA != predA) {
          addStep((succA & MSB_MASK), event, traceA);
          loopIndexA++;
        } else if (succB != predB) {
          addStep((succB & MSB_MASK), event, traceB);
          loopIndexB++;
        }
      }
      succA = predA;
      succB = predB;
    }
    addStep((succA & MSB_MASK), -1, traceA);
    addStep((succB & MSB_MASK), -1, traceB);
    if (loopIndexA == (traceA.size() - 1)) {
      loopIndexA = -1;
    }
    if (loopIndexB == (traceB.size() - 1)) {
      loopIndexB = -1;
    }
    final String nameA = "faulty";
    final String nameB = "non-faulty";
    final String ceName = getModel().getName() + "-undiagnosable";
    final String ceComment =
      "The fault-class " + faultClass + " is not diagnosable.";
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
      long next;
      if (a > b) {
        next = (((long) b) << 32) | (a & 0xffffffffL);
      } else {
        next = (((long) a) << 32) | (b & 0xffffffffL);
      }
      if (a >= 0 || b >= 0) {
        int index = mStateIndexMap.get(next);
        if (!mStateIndexMap.containsKey(next)) {
          mStateIndexMap.put(next, stateCount);
          mIndexStateMap.add(next);
          index = stateCount;
          stateCount++;
          if (stateCount >= getNodeLimit()) {
            throw new OverflowException(getNodeLimit());
          }
          mControlStack.push(index, parentIndex);
        } else if ((mLinks.get(index) & MSB1) == 0) {
          mControlStack.moveToTop(mLinks.get(index), parentIndex);
        } else if (mLinks.get(index) != -1) {
          int newParentLink = Math.min((mLinks.get(parentIndex) & MSB_MASK),
                                       (mLinks.get(index) & MSB_MASK));
          newParentLink |= (mLinks.get(parentIndex) & MSB1);
          mLinks.set(parentIndex, newParentLink);
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
          if (scc.contains(index)) {
            bfsQueue.clear();
            bfsQueue.add(index);
            inSCC = true;
          }
          if (mLinks.get(index) == -1) {
            bfsQueue.add(index);
            mLinks.set(index, bfsIndex);
            bfsIndex++;
          }
        }
      } else {
        if (scc.contains(index)) {
          if (mLinks.get(index) == -1) {
            bfsQueue.add(index);
            mLinks.set(index, bfsIndex);
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
      final boolean newInSCC = scc.contains(index);
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
  //# Inner Class ControllStack
  private class ControllStack
  {

    //#######################################################################
    //# Constructor
    private ControllStack()
    {
      mTop = -1;
      mStack = new TIntArrayList();
      mFreeNodes = new TIntArrayList();
    }

    //#######################################################################
    //# Stack Access
    private void push(final int index, final int parent)
    {
      if (mFreeNodes.isEmpty()) {
        final int newTop = mStack.size();
        mStack.add(index);
        mStack.add(parent);
        mStack.add(mTop);
        mTop = newTop;
      } else {
        final int newTop = mFreeNodes.get(mFreeNodes.size() - 1);
        mFreeNodes.removeAt(mFreeNodes.size() - 1);
        mStack.set(newTop, index);
        mStack.set(newTop + 1, parent);
        mStack.set(newTop + 2, mTop);
        mTop = newTop;
      }
      if (mFreeNodes.isEmpty()) {
        mLinks.add(mStack.size());
      } else {
        mLinks.add(mFreeNodes.get(mFreeNodes.size() - 1));
      }
    }

    private void pop()
    {
      final int oldTop = mTop;
      mFreeNodes.add(oldTop);
      mTop = mStack.get(oldTop + 2);
      mStack.set(oldTop + 2, -1);
    }

    private void moveToTop(final int nextIndex, final int parentIndex)
    {
      if (nextIndex != mStack.size()) {
        final int index = mStack.get(nextIndex + 2);
        if (index != -1) {
          final int prevIndex = mStack.get(index + 2);
          mStack.set(nextIndex + 2, prevIndex);
          mStack.set(index + 2, mTop);
          mStack.set(index + 1, parentIndex);
          mTop = index;
          if (mFreeNodes.isEmpty()) {
            mLinks.set(mStack.get(index), mStack.size());
          } else {
            mLinks.set(mStack.get(index),
                       mFreeNodes.get(mFreeNodes.size() - 1));
          }
        }
      }
    }

    private void clear()
    {
      mStack.clear();
      mFreeNodes.clear();
      mTop = -1;
    }

    private int getTopIndex()
    {
      return (mStack.get(mTop) & MSB_MASK);
    }

    private int getTopParent()
    {
      return mStack.get(mTop + 1);
    }

    private void setTopIndex(final int entry1)
    {
      mStack.set(mTop, entry1);
    }

    private boolean isEmpty()
    {
      return (mTop == -1);
    }

    private boolean isTopExpanded()
    {
      return ((mStack.get(mTop) & MSB1) != 0);
    }

    //#######################################################################
    //# Instance Variables
    private final TIntArrayList mStack;
    private final TIntArrayList mFreeNodes;
    private int mTop;
  }


  //#########################################################################
  //# Instance Variables
  private boolean[] eventObservable;
  private String[] eventFaultClass;
  private int stateCount = 0;
  private int bfsIndex = 0;
  private String faultClass;
  private final TLongIntHashMap mStateIndexMap = new TLongIntHashMap();
  private final TLongArrayList mIndexStateMap = new TLongArrayList();
  private final ControllStack mControlStack = new ControllStack();
  private final TIntArrayList mComponentStack = new TIntArrayList();
  private final TIntArrayList mLinks = new TIntArrayList();
  private final TIntHashSet scc = new TIntHashSet();
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
  private static final int MSB_MASK = ~MSB1;

}
