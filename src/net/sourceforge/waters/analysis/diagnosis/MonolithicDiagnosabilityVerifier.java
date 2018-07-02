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
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**

  //#################################################
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

  @Override
  public boolean isCounterExampleEnabled() {
    return false;
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
      final TRAutomatonProxy sp = spResult.getComputedAutomaton();
      final EventEncoding spEvents = sp.getEventEncoding();
      final ListBufferTransitionRelation rel = sp.getTransitionRelation();
      iterA = rel.createSuccessorsReadOnlyIterator();
      iterB = rel.createSuccessorsReadOnlyIterator();
      final int numEvents = spEvents.getNumberOfProperEvents();
      eventObservability = new boolean[numEvents];
      eventFaultClass = new String[numEvents];
      final THashSet<String> faultClasses = new THashSet<String>();
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
            eventObservability[i] = true;
          }else {
            eventObservability[i] = false;
          }
        }
      }
      if(!faultClasses.isEmpty()) {
        final TObjectHashIterator<String> faultClassIter = faultClasses.iterator();
        final int initialState = rel.getFirstInitialState();
        final long verifierInitState = (((long)initialState)<<32)|initialState;
        while(faultClassIter.hasNext()) {
          faultClass = faultClassIter.next();
          compStack.clear();
          contStack.clear();
          link.clear();
          stateIndexMap.clear();
          indexStateMap.clear();
          stateCount = 0;
          stateIndexMap.put(verifierInitState,stateCount);
          indexStateMap.add(verifierInitState);
          stateCount++;
          link.add(2);
          contStack.add(0);
          contStack.add(0);
          contStack.add(0);
          contStack.add(0);
          contStackTop = 2;

          while(contStackTop!=0) {
            final int i = contStack.get(contStackTop);
            final int p = contStack.get(contStackTop+1);

            if((i&Msb1)==0) {
              expand(i);
            }else{
              contStackTop-=2;
              while(contStack.get(contStackTop)==-1) {
                contStackTop-=2;
              }
              if((i&MsbMask)<compStack.size()) {
                if(!close((i&MsbMask),p)) {
                  return setFailedResult(null);
                }
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
    return setSatisfiedResult();  // diagnosable
  }



  private void expand(final int index) throws OverflowException
  {
    final long state = indexStateMap.get(index);
    final int a = (int)(state>>>32);
    final int b = (int)state;
    contStack.set(link.get(index), compStack.size()|Msb1);
    link.set(index, (compStack.size()|Msb1));
    compStack.add(index);
    if(a>=0||b>=0) {
      iterA.resetState(a&MsbMask);
      int event;
      int targetA, targetB;
      int newA, newB;
      while(iterA.advance()) {
        event = iterA.getCurrentEvent();
        targetA = iterA.getCurrentTargetState();
        //if unobservable
        if(!eventObservability[event]) {
          if(faultClass.equals(eventFaultClass[event])) {
            newA = targetA|Msb1;
            newB = b;
          }else {
            newA = targetA|(a&Msb1);
            newB = b;
          }
          newState(newA,newB,index);
        //if observable
        }else {
          iterB.reset((b&MsbMask),event);
          while(iterB.advance()) {
            targetB = iterB.getCurrentTargetState();
            if(faultClass.equals(eventFaultClass[event])) {
              newA = targetA|Msb1;
              newB = targetB|Msb1;
            }else {
              newA = targetA|(a&Msb1);
              newB = targetB|(b&Msb1);
            }
            newState(newA,newB,index);
          }
        }

      }
      iterA.resetState(b&MsbMask);
      while(iterA.advance()) {
        event = iterA.getCurrentEvent();
        targetB = iterA.getCurrentTargetState();
        //if unobservable
        if(!eventObservability[event]) {
          if(faultClass.equals(eventFaultClass[event])) {
            newA = a;
            newB = targetB|Msb1;
          }else {
            newA = a;
            newB = targetB|(b&Msb1);
          }
          newState(newA,newB,index);
        }
      }
    }
  }



  private void newState(final int a, final int b, final int parentIndex)
    throws OverflowException
  {
    long next;
    if(a > b) {
      next = (((long)a)<<32) | (b&0xffffffffL);
    }else {
      next = (((long)a)<<32) | (b&0xffffffffL);
    }
    int index = stateIndexMap.get(next);
    if(!stateIndexMap.containsKey(next)) {
      stateIndexMap.put(next, stateCount);
      indexStateMap.add(next);
      index = stateCount;
      stateCount++;
      if(stateCount >= getNodeLimit()) {
        throw new OverflowException(getNodeLimit());
      }
      contStackTop+=2;
      if(contStackTop==contStack.size()) {//if stack has not shrunk
        contStack.add(index);
        contStack.add(parentIndex);
      }else {
        contStack.set(contStackTop, index);
        contStack.set(contStackTop+1,parentIndex);
      }
      link.add(contStackTop);
    }else if((link.get(index)&Msb1)==0){
      contStack.set(link.get(index), -1);
      contStackTop+=2;
      if(contStackTop==contStack.size()) {
        contStack.add(index);
        contStack.add(parentIndex);
      }else {
        contStack.set(contStackTop, index);
        contStack.set(contStackTop+1,parentIndex);
      }
      link.set(index, contStackTop);
    }else if(link.get(index)!=-1) {
      int newParentLink = Math.min((link.get(parentIndex)&MsbMask), (link.get(index)&MsbMask));
      newParentLink |= (link.get(parentIndex)&Msb1);
      link.set(parentIndex, newParentLink);
    }
  }



  private boolean close(final int dfsIndex, final int parentIndex) {
    final int index = compStack.get(dfsIndex);
    final long state = indexStateMap.get(index);
    final int a = (int)(state>>>32);
    final int b = (int)state;

    if((link.get(index)&MsbMask)==dfsIndex) {
      int j = compStack.removeAt(compStack.size()-1);
      link.set(j, -1);
      if(j!=index) {
        do {
          j = compStack.removeAt(compStack.size()-1);
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
          int e;
          iterA.resetState(a&MsbMask);
          while(iterA.advance()) {
            if(iterA.getCurrentTargetState()==(a&MsbMask)) {
              e = iterA.getCurrentEvent();
              if(!eventObservability[e]) {
                return false;
              }else {
                iterB.reset((b&MsbMask),e);
                while(iterB.advance()) {
                  if(iterB.getCurrentTargetState()==(b&MsbMask)) {
                    return false;
                  }
                }
              }
            }
          }
          iterB.resetState(b&MsbMask);
          while(iterB.advance()) {
            if(iterB.getCurrentTargetState()==(b&MsbMask)) {
              e = iterB.getCurrentEvent();
              if(!eventObservability[e]) {
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


  //#########################################################################
  //# Instance Variables
  private boolean[] eventObservability;
  private String[] eventFaultClass;
  private final TLongIntHashMap stateIndexMap = new TLongIntHashMap();
  private final TLongArrayList indexStateMap = new TLongArrayList();
  private final TIntArrayList compStack = new TIntArrayList();
  private final TIntArrayList contStack = new TIntArrayList();
  private final TIntArrayList link = new TIntArrayList();
  private int contStackTop;
  private int stateCount = 0;
  private String faultClass;
  private TransitionIterator iterA;
  private TransitionIterator iterB;


  //#########################################################################
  //# Constants
  final int Msb1 = -2147483648;
  final int MsbMask = 2147483647;
  final int OPEN = 1;
  final int EXPANDED = 2;
  final int CLOSED = 3;

  //#########################################################################
  //# Inner Class ControllStack
  private class ControllStack{

    private final TIntArrayList stack;
    private final TIntArrayList freeNodes;
    private int top;

    public ControllStack(){
      final int top = 0;
      stack = new TIntArrayList();
      freeNodes = new TIntArrayList();
      stack.add(0);
      stack.add(0);
      stack.add(-1);

    }

    public void push(final int entry1, final int entry2) {
      if(freeNodes.isEmpty()) {
        final int newTop = stack.size();
        stack.add(entry1);
        stack.add(entry2);
        stack.add(top);
        top = newTop;
      }else {
        final int newTop = freeNodes.get(freeNodes.size()-1);
        freeNodes.removeAt(freeNodes.size()-1);
        stack.set(newTop, entry1);
        stack.set(newTop+1, entry2);
        stack.set(newTop+2, top);
        top = newTop;
      }

    }

    public void pop() {
      final int oldTop = top;
      top = stack.get(oldTop+2);
      if((oldTop+3) == stack.size()) {
        stack.remove(oldTop, 3);
      }else {
        freeNodes.add(oldTop);
      }
      int endIndex = stack.size()-3;
      while(freeNodes.contains(endIndex)) {
        stack.remove(endIndex, 3);
        freeNodes.remove(endIndex);
        endIndex = stack.size()-3;
      }
    }

    public void moveToTop(final int nextIndex) {
      final int index = stack.get(nextIndex+2);
      final int prevIndex = stack.get(index+2);
      stack.set(nextIndex+2, prevIndex);
      stack.set(index+2, top);
      top = index;
    }

    public int getNextTopIndex() {
      if(freeNodes.isEmpty())
        return stack.size();
      else
        return freeNodes.get(freeNodes.size()-1);
    }

    public int getTopIndex() { return top; }

    public int getTopEntry1() { return (stack.get(top)&MsbMask); }

    public int getTopEntry2() { return stack.get(top+1); }

    public void setTopEntry1(final int entry1) { stack.set(top, entry1); }

    public void setTopEntry2(final int entry2) { stack.set(top+1, entry2); }

    public boolean isEmpty() {
      if(top==0)
        return true;
      else
        return false;
    }

    public boolean isTopExpanded() {
      final int entry1 = stack.get(top);
      if((entry1&Msb1)!=0)
        return true;
      else
        return false;
    }


  }







}