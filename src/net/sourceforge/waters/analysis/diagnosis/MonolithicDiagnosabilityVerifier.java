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
import gnu.trove.map.hash.TLongIntHashMap;

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
  /**
   * @param factory
   * @param translator
   */
  public MonolithicDiagnosabilityVerifier(final ProductDESProxyFactory factory,
                                          final KindTranslator translator)
  {
    super(factory, translator);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param model
   * @param factory
   * @param translator
   */
  public MonolithicDiagnosabilityVerifier(final ProductDESProxy model,
                                          final ProductDESProxyFactory factory,
                                          final KindTranslator translator)
  {
    super(model, factory, translator);
    // TODO Auto-generated constructor stub
  }


  //#########################################################################
  //# Configuration
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
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
      if(!spBuilder.run()) {
        //throw exception
      }
      final TRSynchronousProductResult spResult = spBuilder.getAnalysisResult();
      final TRAutomatonProxy sp = spResult.getComputedAutomaton();
      final ListBufferTransitionRelation rel = sp.getTransitionRelation();
      final EventEncoding spEvents = sp.getEventEncoding();

      iterA = rel.createSuccessorsReadOnlyIterator();
      iterB = rel.createSuccessorsReadOnlyIterator();

      final int numEvents = spEvents.getNumberOfEvents();
      eventProperties = new int[numEvents];
      final TIntArrayList faultTypes = new TIntArrayList();
      final String key = "FAULT";
      String value;
      Map<String,String> attrib;
      EventProxy event;
      int faultType = 0;

      for(int i=0; i<numEvents; i++){
        event = spEvents.getProperEvent(i);
        if(event!=null) {
          attrib = event.getAttributes();
          value = attrib.get(key);
          faultType = 0;
          if(value!=null) {
            try {
              faultType = Integer.parseInt(value);
            }catch(final NumberFormatException ex) {
              faultType = 0;
            }
          }
          if(faultType!=0 && !faultTypes.contains(faultType)) {
            faultTypes.add(faultType);
          }
          if(event.isObservable()) {
            eventProperties[i] = (Msb1 | faultType);
          }else {
            eventProperties[i] = faultType;
          }
        }else {
          eventProperties[i] = 0;
        }
      }
      if(!faultTypes.isEmpty()) {
        final TIntIterator faultIter = faultTypes.iterator();
        final int initialState = rel.getFirstInitialState();
        final long verifierInitState = ((long)initialState<<32)|initialState;
        do {
          faultType = faultIter.next();
          Qindex = 0;
          Q.clear();
          Q.put(verifierInitState,Qindex);
          Qindex++;
          stack.clear();
          lowlink.clear();
          lowlink.add(0);
          if(!explore(verifierInitState,faultType)) {
            return setFailedResult(null);
          }
        }while(faultIter.hasNext());
      }

    } finally {
      tearDown();
    }
    return setSatisfiedResult();  // diagnosable
  }

  private boolean explore(final long current, final int faultType) {
    final int i = Q.get(current);
    lowlink.set(i, i);
    stack.add(i);
    final int a = (int)(current>>>32);
    final int b = (int)current;
    if(a>=0||b>=0) {
      iterA.resetState(a&MsbMask);
      int e;
      int targetA, targetB;
      int eProperties;
      int j;
      long next = 0;
      boolean nextFound = false;
      while(iterA.advance()) {
        e = iterA.getCurrentEvent();
        targetA = iterA.getCurrentTargetState();
        eProperties = eventProperties[e];
        //if unobservable
        if(eProperties>=0) {
          nextFound = true;
          if(eProperties == faultType) {
            next = newState((targetA|Msb1),b);
          }else {
            next = newState((targetA|(a&Msb1)),b);
          }
        //if observable
        }else {
          iterB.reset((b&MsbMask),e);
          while(iterB.advance()) {
            nextFound = true;
            targetB = iterB.getCurrentTargetState();
            if((eProperties&MsbMask)==faultType) {
              next = newState((targetA|Msb1),(targetB|Msb1));
            }else {
              next = newState((targetA|(a&Msb1)),(targetB|(b&Msb1)));
            }
          }
        }
        //if nextFound process next
        if(nextFound) {
          if(!Q.containsKey(next)) {
            Q.put(next, Qindex);
            j = Qindex;
            Qindex++;
            lowlink.add(0);
            if(!explore(next,faultType)) {
              return false;
            }
            lowlink.set(i, Math.min(lowlink.get(i), lowlink.get(j)));
          }else {
            j = Q.get(next);
            if(stack.contains(j)) {
              lowlink.set(i, Math.min(lowlink.get(i),j));
            }
          }
        }
        nextFound = false;
      }
      iterB.resetState(b&MsbMask);
      while(iterB.advance()) {
        e = iterB.getCurrentEvent();
        targetB = iterA.getCurrentTargetState();
        eProperties = eventProperties[e];
        //if unobservable
        if(eProperties>=0) {
          nextFound = true;
          if(eProperties == faultType) {
            next = newState(a,(targetB|Msb1));
          }else {
            next = newState(a,(targetB|(b&Msb1)));
          }
        }
        //if nextFound process next
        if(nextFound) {
          if(!Q.containsKey(next)) {
            Q.put(next, Qindex);
            j = Qindex;
            Qindex++;
            lowlink.add(0);
            if(!explore(next,faultType)) {
              return false;
            }
            lowlink.set(i, Math.min(lowlink.get(i), lowlink.get(j)));
          }else {
            j = Q.get(next);
            if(stack.contains(j)) {
              lowlink.set(i, Math.min(lowlink.get(i),j));
            }
          }
        }
        nextFound = false;
      }
    }
    //check for SCC
    if(lowlink.get(i)==i) {
      int j = stack.removeAt(stack.size()-1);
      if(j!=i) {
        do {
          j = stack.removeAt(stack.size()-1);
        }while(j!=i);
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
              if(eventProperties[e]>=0) {
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
              if(eventProperties[e]>=0) {
                return false;
              }
            }
          }
        }
      }
    }
    return true;
  }



  private long newState(final int a, final int b) {
    if(a > b) {
      return ((long)b<<32)|a;
    }else {
      return ((long)a<<32)|b;
    }
  }


  //#########################################################################
  //# Instance Variables
  private int[] eventProperties;
  private final TLongIntHashMap Q = new TLongIntHashMap();
  private final TIntArrayList stack = new TIntArrayList();
  private final TIntArrayList lowlink = new TIntArrayList();
  private int Qindex = 0;
  private TransitionIterator iterA;
  private TransitionIterator iterB;
  //#########################################################################
  //# Constants
  final int Msb1 = -2147483648;
  final int MsbMask = 2147483647;

}
