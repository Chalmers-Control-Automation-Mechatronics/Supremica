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
import gnu.trove.map.hash.TIntObjectHashMap;
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
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.AbstractModelVerifier;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Nicholas McGrath
 */
public class MonolithicDiagnosabilityVerifier extends AbstractModelVerifier
{

  //#########################################################################
  //# Constructors
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

  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
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
      rel = sp.getTransitionRelation();
      final int numEvents = spEvents.getNumberOfProperEvents();
      eventObservability = new boolean[numEvents];
      final THashSet<String> faultClasses = new THashSet<String>();
      final String key = "FAULT";
      String value;
      Map<String,String> attrib;
      EventProxy event;
      for(int i= EventEncoding.NONTAU; i<numEvents; i++){
        event = spEvents.getProperEvent(i);
        if(event!=null) {
          attrib = event.getAttributes();
          value = attrib.get(key);
          if(value!=null) {
            faultClassMap.put(i,value);
            if(!faultClasses.contains(value)) {
              faultClasses.add(value);
            }
          }
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
        final long verifierInitState = ((long)initialState<<32)|initialState;
        while(faultClassIter.hasNext()) {
          faultClass = faultClassIter.next();
          Qindex = 0;
          Q.clear();
          Q.put(verifierInitState,Qindex);
          Qindex++;
          stack.clear();
          lowlink.clear();
          lowlink.add(0);
          if(!explore(verifierInitState)) {
            return setFailedResult(null); // not diagnosable
          }
        }
      }

    } finally {
      tearDown();
    }
    return setSatisfiedResult();  // diagnosable
  }





  private boolean explore(final long current) {
    final TransitionIterator iterA = rel.createSuccessorsReadOnlyIterator();
    final TransitionIterator iterB = rel.createSuccessorsReadOnlyIterator();
    final int index = Q.get(current);
    lowlink.set(index, index);
    stack.add(index);
    final int a = (int)(current>>>32);
    final int b = (int)current;
    if(a>=0||b>=0) {
      iterA.resetState(a&MsbMask);
      int e;
      int targetA, targetB;
      int newA, newB;
      while(iterA.advance()) {
        e = iterA.getCurrentEvent();
        targetA = iterA.getCurrentTargetState();
        //if unobservable
        if(!eventObservability[e]) {
          if(faultClass.equals(faultClassMap.get(e))) {
            newA = targetA|Msb1; newB = b;
          }else {
            newA = targetA|(a&Msb1); newB = b;
          }
          if(!newState(newA,newB,index))
            return false;
        //if observable
        }else {
          iterB.reset((b&MsbMask),e);
          while(iterB.advance()) {
            targetB = iterB.getCurrentTargetState();
            if(faultClass.equals(faultClassMap.get(e))) {
              newA = targetA|Msb1; newB = targetB|Msb1;
            }else {
              newA = targetA|(a&Msb1); newB = targetB|(b&Msb1);
            }
            if(!newState(newA,newB,index))
              return false;
          }
        }

      }
      iterA.resetState(b&MsbMask);
      while(iterA.advance()) {
        e = iterA.getCurrentEvent();
        targetB = iterA.getCurrentTargetState();
        //if unobservable
        if(!eventObservability[e]) {
          if(faultClass.equals(faultClassMap.get(e))) {
            newA = a; newB = targetB|Msb1;
          }else {
            newA = a; newB = targetB|(b&Msb1);
          }
          if(!newState(newA,newB,index))
            return false;
        }
      }
    }
    //check for SCC
    if(lowlink.get(index)==index) {
      int j = stack.removeAt(stack.size()-1);
      if(j!=index) {
        do {
          j = stack.removeAt(stack.size()-1);
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
    }
    return true;
  }



  private boolean newState(final int a, final int b, final int parentIndex) {
    int index;
    long next;
    if(a > b) {
      next = (((long)a)<<32) | (b&0xffffffffL);
    }else {
      next = (((long)a)<<32) | (b&0xffffffffL);
    }
    if(!Q.containsKey(next)) {
      Q.put(next, Qindex);
      index = Qindex;
      Qindex++;
      if(Qindex > getNodeLimit()) {
        //throw overflow exception;
      }

      lowlink.add(0);
      if(!explore(next)) {
        return false;
      }
      lowlink.set(parentIndex, Math.min(lowlink.get(parentIndex),lowlink.get(index)));
    }else {
      index = Q.get(next);
      if(stack.contains(index)) {
        lowlink.set(parentIndex, Math.min(lowlink.get(parentIndex),index));
      }
    }
    return true;
  }


  //#########################################################################
  //# Instance Variables
  private boolean[] eventObservability;
  private final TIntObjectHashMap<String> faultClassMap = new TIntObjectHashMap<String>();
  private final TLongIntHashMap Q = new TLongIntHashMap();
  private final TIntArrayList stack = new TIntArrayList();
  private final TIntArrayList lowlink = new TIntArrayList();
  private int Qindex = 0;
  private String faultClass;
  ListBufferTransitionRelation rel;


  //#########################################################################
  //# Constants
  final int Msb1 = -2147483648;
  final int MsbMask = 2147483647;

}

//code to print out verifier transitions

//boolean caf = false;
//boolean cbf = false;
//boolean naf = false;
//boolean nbf = false;
//int na = (int)(next>>>32);
//int nb = (int)next;
//if((a&Msb1) == Msb1) {
//  caf = true;
//}
//if((b&Msb1) == Msb1) {
//  cbf = true;
//}
//if((na&Msb1) == Msb1) {
//  naf = true;
//}
//if((nb&Msb1) == Msb1) {
//  nbf = true;
//}
//final int ca = a&MsbMask;
//final int cb = b&MsbMask;
//na &=MsbMask;
//nb &=MsbMask;
//
//System.out.println("("+ca+" "+caf+", "+cb+" "+cbf+") to ("+na+" "+naf+", "+nb+" "+nbf+")");
