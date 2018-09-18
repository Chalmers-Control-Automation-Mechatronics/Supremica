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
public class MonolithicDiagnosabilityVerifier
  extends AbstractModelVerifier
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
      final TRSynchronousProductBuilder spBuilder = new TRSynchronousProductBuilder(getModel());
      spBuilder.run();
      final TRSynchronousProductResult spResult = spBuilder.getAnalysisResult();
      sp = spResult.getComputedAutomaton();
      spEvents = sp.getEventEncoding();
      spStateMap = spResult.getStateMap();
      rel = sp.getTransitionRelation();
      iterA = rel.createSuccessorsReadOnlyIterator();
      iterB = rel.createSuccessorsReadOnlyIterator();
      final int numEvents = spEvents.getNumberOfProperEvents();
      final int numStates = rel.getNumberOfStates();
      eventObservable = new boolean[numEvents];
      eventFaultClass = new String[numEvents];
      final THashSet<String> faultClasses = new THashSet<String>();
      final TIntArrayList initStates = new TIntArrayList();
      for(int i = 0; i < numStates; i++){
        if(rel.isInitial(i)){
          initStates.add(i);
        }
      }
      String faultLabel;
      Map<String,String> attrib;
      EventProxy event;
      for(int i= EventEncoding.NONTAU; i<numEvents; i++){
        event = spEvents.getProperEvent(i);
        if(event!=null) {
          attrib = event.getAttributes();
          faultLabel = attrib.get(DiagnosabilityAttributeFactory.FAULT_KEY);
          if(faultLabel!=null) {
            faultClasses.add(faultLabel);
          }
          eventFaultClass[i] = faultLabel;
          if(event.isObservable()) {
            eventObservable[i] = true;
          }else {
            eventObservable[i] = false;
          }
        }
      }
      if(!faultClasses.isEmpty()) {
        final TObjectHashIterator<String> faultClassIter = faultClasses.iterator();
        final StateProcessor processor = new VerifierStateProcessor();
        while(faultClassIter.hasNext()) {
          faultClass = faultClassIter.next();
          compStack.clear();
          contStack.clear();
          link.clear();
          stateIndexMap.clear();
          indexStateMap.clear();
          stateCount = 0;
          for(int x=0; x<initStates.size(); x++) {
            for(int y=0; y<=x; y++) {
              final int initA = initStates.get(y);
              final int initB = initStates.get(x);
              final long verifierInit = (((long)initA)<<32)|initB;
              stateIndexMap.put(verifierInit,stateCount);
              indexStateMap.add(verifierInit);
              contStack.push(stateCount,stateCount);
              lastVerInit = stateCount;
              stateCount++;
            }
          }
          while(!contStack.isEmpty()) {
            final int i = contStack.getTopIndex();
            final int p = contStack.getTopParent();
            if(!contStack.isTopExpanded()) {
              contStack.setTopIndex(compStack.size()|Msb1);
              link.set(i, (compStack.size()|Msb1));
              compStack.add(i);
              expand(i,processor);
            }else{
              contStack.pop();
              if(!close(i,p)) {
                final CounterExampleProxy counterExample = computeCounterExample();
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

  private void expand(final int index, final StateProcessor process) throws OverflowException
  {
    final long state = indexStateMap.get(index);
    final int a = (int)(state>>>32);
    final int b = (int)state;
    int event;
    int targetA, targetB;
    int newA, newB;
    iterA.resetState(a&MsbMask);
    while(iterA.advance()) {
      event = iterA.getCurrentEvent();
      targetA = iterA.getCurrentTargetState();
      if(!eventObservable[event]) {
        if(faultClass.equals(eventFaultClass[event])) {
          newA = targetA|Msb1;
          newB = b;
        }else {
          newA = targetA|(a&Msb1);
          newB = b;
        }
        process.newState(newA,newB,index);
      }else {
        iterB.reset((b&MsbMask),event);
        while(iterB.advance()) {
          targetB = iterB.getCurrentTargetState();
          if(!faultClass.equals(eventFaultClass[event])) {
            newA = targetA|(a&Msb1);
            newB = targetB|(b&Msb1);
            process.newState(newA,newB,index);
          }
        }
      }
    }
    iterA.resetState(b&MsbMask);
    while(iterA.advance()) {
      event = iterA.getCurrentEvent();
      targetB = iterA.getCurrentTargetState();
      if(!eventObservable[event]) {
        if(faultClass.equals(eventFaultClass[event])) {
          newA = a;
          newB = targetB|Msb1;
        }else {
          newA = a;
          newB = targetB|(b&Msb1);
        }
        process.newState(newA,newB,index);
      }
    }
  }

  private void expandBack(final int a,final int b, final BackStateProcessor process)
    throws OverflowException
  {
    process.reset();
    int event;
    int sourceA, sourceB;
    int newA, newB;
    iterA.resetState(a&MsbMask);
    while(iterA.advance()) {
      event = iterA.getCurrentEvent();
      sourceA = iterA.getCurrentSourceState();
      if(!eventObservable[event]) {
        if(faultClass.equals(eventFaultClass[event])) {
          if((a&Msb1)!=0) {
            newA = sourceA|Msb1;
            newB = b;
            process.newState(newA,newB,event);
            newA = sourceA;
            process.newState(newA,newB,event);
          }
        }else {
          newA = sourceA|(a&Msb1);
          newB = b;
          process.newState(newA,newB,event);
        }
      }else {
        iterB.reset((b&MsbMask),event);
        while(iterB.advance()) {
          sourceB = iterB.getCurrentSourceState();
          if(!faultClass.equals(eventFaultClass[event])) {
            newA = sourceA|(a&Msb1);
            newB = sourceB|(b&Msb1);
            process.newState(newA,newB,event);
          }
        }
      }
    }
    iterA.resetState(b&MsbMask);
    while(iterA.advance()) {
      event = iterA.getCurrentEvent();
      sourceB = iterA.getCurrentSourceState();
      if(!eventObservable[event]) {
        if(faultClass.equals(eventFaultClass[event])) {
          if((b&Msb1)!=0) {
            newA = a;
            newB = sourceB|Msb1;
            process.newState(newA,newB,event);
            newB = sourceB;
            process.newState(newA,newB,event);
          }
        }else {
          newA = a;
          newB = sourceB|(b&Msb1);
          process.newState(newA,newB,event);
        }
      }
    }
  }

  private boolean close(final int dfsIndex, final int parentIndex){
    final int index = compStack.get(dfsIndex);
    final long state = indexStateMap.get(index);
    final int a = (int)(state>>>32);
    final int b = (int)state;
    if((link.get(index)&MsbMask)==dfsIndex) {
      scc.clear();
      int j = compStack.removeAt(compStack.size()-1);
      scc.add(j);
      link.set(j, -1);
      if(j!=index) {
        do {
          j = compStack.removeAt(compStack.size()-1);
          scc.add(j);
          link.set(j, -1);
        }while(j!=index);
        if((a&Msb1)==(b&Msb1)) {
          return true;
        }else {
          return false;
        }
      }else {
        if((a&Msb1)==(b&Msb1)) {
          return true;
        }else {
          int event;
          iterA.resetState(a&MsbMask);
          while(iterA.advance()) {
            if(iterA.getCurrentTargetState()==(a&MsbMask)) {
              event = iterA.getCurrentEvent();
              if(!eventObservable[event]) {
                return false;
              }else {
                iterB.reset((b&MsbMask),event);
                while(iterB.advance()) {
                  if(iterB.getCurrentTargetState()==(b&MsbMask)) {
                    return false;
                  }
                }
              }
            }
          }
          iterA.resetState(b&MsbMask);
          while(iterA.advance()) {
            if(iterA.getCurrentTargetState()==(b&MsbMask)) {
              event = iterA.getCurrentEvent();
              if(!eventObservable[event]) {
                return false;
              }
            }
          }
        }
      }
    }else {
      int newParentLink = Math.min((link.get(parentIndex)&MsbMask), (link.get(index)&MsbMask));
      newParentLink |= (link.get(parentIndex)&Msb1);
      link.set(parentIndex, newParentLink);
    }
    return true;
  }

  private CounterExampleProxy computeCounterExample()
    throws OverflowException
  {
    contStack.clear();
    compStack.clear();
    bfsQueue.clear();
    System.gc();
    int sccRoot = 0;
    final StateProcessor processor = new BFSStateProcessor();
    final BackStateProcessor backProcessor = new BackStateProcessor();
    for(int i = 0; i < link.size(); i++) {
      link.set(i, -1);
    }
    for(int i = 0; i <= lastVerInit; i++) {
      bfsQueue.add(i);
      link.set(i, bfsIndex);
      bfsIndex++;
    }
    while(!bfsQueue.isEmpty()) {
      final int current = bfsQueue.remove();
      if(scc.contains(current)) {
        sccRoot = current;
        break;
      }
      expand(current,processor);
    }
    bfsQueue.clear();
    expand(sccRoot,processor);
    while(!bfsQueue.isEmpty()) {
      final int current = bfsQueue.remove();
      if(current == sccRoot) {
        break;
      }
      expand(current,processor);
    }
    bfsQueue.clear();
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    iterA = rel.createPredecessorsReadOnlyIterator();
    iterB = rel.createPredecessorsReadOnlyIterator();
    final List<TraceStepProxy> traceA = new ArrayList<TraceStepProxy>();
    final List<TraceStepProxy> traceB = new ArrayList<TraceStepProxy>();
    final long succState = indexStateMap.get(sccRoot);
    int succA = ((int)(succState>>>32));
    int succB = ((int)succState);
    int event,predA,predB,loopIndexA,loopIndexB;
    do{
      expandBack(succA,succB,backProcessor);
      predA = backProcessor.predA;
      predB = backProcessor.predB;
      event = backProcessor.predEvent;
      if(eventObservable[event]) {
        addStep((succA&MsbMask),event,traceA);
        addStep((succB&MsbMask),event,traceB);
      }else {
        if(succA!=predA) {
          addStep((succA&MsbMask),event,traceA);
        }else if(succB!=predB) {
          addStep((succB&MsbMask),event,traceB);
        }
      }
      succA = predA;
      succB = predB;
    }while(backProcessor.predIndex!=sccRoot);
    loopIndexA = loopIndexB = 0;
    backProcessor.inSCC=false;
    while(backProcessor.predIndex>lastVerInit) {
      expandBack(succA,succB,backProcessor);
      predA = backProcessor.predA;
      predB = backProcessor.predB;
      event = backProcessor.predEvent;
      if(eventObservable[event]) {
        addStep((succA&MsbMask),event,traceA);
        addStep((succB&MsbMask),event,traceB);
        loopIndexA++;loopIndexB++;
      }else {
        if(succA!=predA) {
          addStep((succA&MsbMask),event,traceA);
          loopIndexA++;
        }else if(succB!=predB) {
          addStep((succB&MsbMask),event,traceB);
          loopIndexB++;
        }
      }
      succA = predA;
      succB = predB;
    }
    addStep((succA&MsbMask),-1,traceA);
    addStep((succB&MsbMask),-1,traceB);
    if(loopIndexA==(traceA.size()-1)) {
      loopIndexA = -1;
    }
    if(loopIndexB==(traceB.size()-1)) {
      loopIndexB = -1;
    }
    final String nameA = "faulty";
    final String nameB = "non-faulty";
    final String ceName = getModel().getName()+"-undiagnosable";
    final String ceComment = "The fault-class "+faultClass+" is not diagnosable.";
    final TraceProxy tpA
      = getFactory().createTraceProxy(nameA, traceA, loopIndexA);
    final TraceProxy tpB
      = getFactory().createTraceProxy(nameB, traceB, loopIndexB);
    final DualCounterExampleProxy counterExample
      = getFactory().createDualCounterExampleProxy(ceName, ceComment, null, getModel(), spStateMap.getInputAutomata(), tpA, tpB);
    return counterExample;
  }

  private void addStep(final int stateIndex, final int eventIndex, final List<TraceStepProxy> trace)
  {
    final Map<AutomatonProxy,StateProxy> autStateMap = new HashMap<AutomatonProxy,StateProxy>();
    final Collection<AutomatonProxy> auts = spStateMap.getInputAutomata();
    final StateProxy state = sp.getState(stateIndex);
    final EventProxy event;
    if(eventIndex != -1) {
      event = spEvents.getProperEvent(eventIndex);
    }else {
      event = null;
    }
    for(final AutomatonProxy a : auts) {
      final StateProxy s = spStateMap.getOriginalState(state, a);
      autStateMap.put(a, s);
    }
    final TraceStepProxy step = getFactory().createTraceStepProxy(event, autStateMap);
    trace.add(0, step);
  }


  //#########################################################################
  //# Instance Variables
  private boolean[] eventObservable;
  private String[] eventFaultClass;
  private int stateCount = 0;
  private int bfsIndex = 0;
  private String faultClass;
  private final TLongIntHashMap stateIndexMap = new TLongIntHashMap();
  private final TLongArrayList indexStateMap = new TLongArrayList();
  private final ControllStack contStack = new ControllStack();
  private final TIntArrayList compStack = new TIntArrayList();
  private final TIntArrayList link = new TIntArrayList();
  private final TIntHashSet scc = new TIntHashSet();
  private final ArrayDeque<Integer> bfsQueue = new ArrayDeque<Integer>();
  private ListBufferTransitionRelation rel;
  private SynchronousProductStateMap spStateMap;
  private TRAutomatonProxy sp;
  EventEncoding spEvents;
  private TransitionIterator iterA;
  private TransitionIterator iterB;
  private int lastVerInit;


  //#########################################################################
  //# Constants
  final int Msb1 = -2147483648;
  final int MsbMask = 2147483647;

  abstract interface StateProcessor{
    public void newState(final int a, final int b,final int parentIndex)
      throws OverflowException;
  }

  private class VerifierStateProcessor
    implements StateProcessor
  {
    @Override
    public void newState(final int a, final int b, final int parentIndex)
      throws OverflowException
    {
      long next;
      if(a > b) {
        next = (((long)b)<<32)|(a&0xffffffffL);
      }else {
        next = (((long)a)<<32)|(b&0xffffffffL);
      }
      if(a>=0||b>=0) {
        int index = stateIndexMap.get(next);
        if(!stateIndexMap.containsKey(next)) {
          stateIndexMap.put(next, stateCount);
          indexStateMap.add(next);
          index = stateCount;
          stateCount++;
          if(stateCount >= getNodeLimit()) {
            throw new OverflowException(getNodeLimit());
          }
          contStack.push(index, parentIndex);
        }else if((link.get(index)&Msb1)==0){
          contStack.moveToTop(link.get(index),parentIndex);
        }else if(link.get(index)!=-1) {
          int newParentLink = Math.min((link.get(parentIndex)&MsbMask), (link.get(index)&MsbMask));
          newParentLink |= (link.get(parentIndex)&Msb1);
          link.set(parentIndex, newParentLink);
        }
      }
    }
  }

  private class BFSStateProcessor
    implements StateProcessor
  {
    private boolean inSCC = false;
    @Override
    public void newState(final int a, final int b, final int parentIndex)
      throws OverflowException
    {
      long next;
      if(a > b) {
        next = (((long)b)<<32)|(a&0xffffffffL);
      }else {
        next = (((long)a)<<32)|(b&0xffffffffL);
      }
      final int index = stateIndexMap.get(next);
      if(!inSCC){
        if(stateIndexMap.containsKey(next)) {
          if(scc.contains(index)) {
            bfsQueue.clear();
            bfsQueue.add(index);
            inSCC = true;
          }
          if(link.get(index)==-1) {
            bfsQueue.add(index);
            link.set(index, bfsIndex);
            bfsIndex++;
          }
        }
      }else{
        if(scc.contains(index)) {
          if(link.get(index)==-1) {
            bfsQueue.add(index);
            link.set(index, bfsIndex);
            bfsIndex++;
          }
        }
      }
    }
  }

  private class BackStateProcessor
    implements StateProcessor
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
      if(a > b) {
        next = (((long)b)<<32)|(a&0xffffffffL);
      }else {
        next = (((long)a)<<32)|(b&0xffffffffL);
      }
      final int index = stateIndexMap.get(next);
      final boolean newInMap = stateIndexMap.contains(next);
      final boolean newInSCC = scc.contains(index);
      if((!inSCC && newInMap) || (inSCC && newInSCC)) {
        if(link.get(index)!=-1) {
          if(predIndex==-1) {
            predIndex=index;
            predEvent=event;
            predA = a;
            predB = b;
          }else {
            if(link.get(index)<link.get(predIndex)) {
              predIndex=index;
              predEvent=event;
              predA = a;
              predB = b;
            }
          }
        }
      }
    }
    public void reset() { predA = predB = predIndex = -1; }
  }

  //#########################################################################
  //# Inner Class ControllStack
  private class ControllStack{

    private final TIntArrayList stack;
    private final TIntArrayList freeNodes;
    private int top;

    public ControllStack(){
      top = -1;
      stack = new TIntArrayList();
      freeNodes = new TIntArrayList();
    }

    public void push(final int index, final int parent) {
      if(freeNodes.isEmpty()) {
        final int newTop = stack.size();
        stack.add(index);
        stack.add(parent);
        stack.add(top);
        top = newTop;
      }else {
        final int newTop = freeNodes.get(freeNodes.size()-1);
        freeNodes.removeAt(freeNodes.size()-1);
        stack.set(newTop, index);
        stack.set(newTop+1, parent);
        stack.set(newTop+2, top);
        top = newTop;
      }
      if(freeNodes.isEmpty()) {
        link.add(stack.size());
      }else {
        link.add(freeNodes.get(freeNodes.size()-1));
      }
    }

    public void pop() {
      final int oldTop = top;
      freeNodes.add(oldTop);
      top = stack.get(oldTop+2);
      stack.set(oldTop+2, -1);
    }

    public void moveToTop(final int nextIndex, final int parentIndex) {
      if(nextIndex!=stack.size()) {
        final int index = stack.get(nextIndex+2);
        if(index!=-1) {
          final int prevIndex = stack.get(index+2);
          stack.set(nextIndex+2, prevIndex);
          stack.set(index+2, top);
          stack.set(index+1, parentIndex);
          top = index;
          if(freeNodes.isEmpty()) {
            link.set(stack.get(index), stack.size());
          }else {
            link.set(stack.get(index), freeNodes.get(freeNodes.size()-1));
          }
        }
      }
    }

    public void clear() {
      stack.clear();
      freeNodes.clear();
      top = -1;
    }

    public int getTopIndex() { return (stack.get(top)&MsbMask); }
    public int getTopParent() { return stack.get(top+1); }
    public void setTopIndex(final int entry1) { stack.set(top, entry1); }
    public boolean isEmpty() { return (top==-1); }
    public boolean isTopExpanded() { return ((stack.get(top)&Msb1)!=0); }
  }

}
