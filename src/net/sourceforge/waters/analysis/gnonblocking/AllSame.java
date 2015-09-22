//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Simon Ware
 */

class AllSame
{
  private final TObjectIntHashMap<EventProxy> mEventNumber;
  private final EventProxy[] mNumberEvent;
  private final int mNumberOfEvents;
  private AutomatonProxy mResult;
  private SortedSet<AutomatonProxy> mNotComposed;

  // #########################################################################
  // # Constructors
  public AllSame(final ProductDESProxy proxy)
  {
    mNumberOfEvents = proxy.getEvents().size();
    mEventNumber = new TObjectIntHashMap<EventProxy>();
    mNumberEvent = new EventProxy[mNumberOfEvents];
    int e = 0;
    for (final EventProxy event : proxy.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION) {continue;}
      mNumberEvent[e] = event;
      mEventNumber.put(event, e);
      e++;
    }
  }

  private boolean[][] calculateAllSame(final AutomatonProxy aut, final KindTranslator kt) throws OverflowException
  {
    final boolean[][] eventssame = new boolean[mNumberOfEvents][mNumberOfEvents];
    // mark all non local events as being not same of local
    for (int e1 = 0; e1 < mNumberOfEvents; e1++) {
      for (int e2 = 0; e2 < mNumberOfEvents; e2++) {
        if (aut.getEvents().contains(mNumberEvent[e1]) !=
            aut.getEvents().contains(mNumberEvent[e2])) {
          eventssame[e1][e2] = false;
        } else {
          eventssame[e1][e2] = true;
        }
      }
    }
    final EventEncoding ee = new EventEncoding(aut, kt);
    final ListBufferTransitionRelation lbtr =
      new ListBufferTransitionRelation(aut, ee,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    for (int s = 0; s < lbtr.getNumberOfStates(); s++) {
      for (int e1 = 0; e1 < ee.getNumberOfProperEvents(); e1++) {
        if (!mEventNumber.containsKey(ee.getProperEvent(e1))) {
          continue;
        }
        final TIntHashSet targets1 = new TIntHashSet();
        final TransitionIterator ti1 = lbtr.createSuccessorsReadOnlyIterator(s, e1);
        final int eventnumberorig1 = mEventNumber.get(ee.getProperEvent(e1));
        while (ti1.advance()) {
          targets1.add(ti1.getCurrentTargetState());
        }
        for (int e2 = 0; e2 < ee.getNumberOfProperEvents(); e2++) {
          if (e1 == e2) {continue;}
          if (!mEventNumber.containsKey(ee.getProperEvent(e2))) {
            continue;
          }
          final int eventnumberorig2 = mEventNumber.get(ee.getProperEvent(e2));
          if (eventssame[eventnumberorig1][eventnumberorig2] == false) {continue;}
          final TIntHashSet targets2 = new TIntHashSet();
          final TransitionIterator ti2 = lbtr.createSuccessorsReadOnlyIterator(s, e2);
          while (ti2.advance()) {
            targets2.add(ti2.getCurrentTargetState());
          }
          eventssame[eventnumberorig1][eventnumberorig2] = !targets2.equals(targets1);
        }
      }
    }
    return eventssame;
  }

  public boolean[][] intersect(final boolean[][][] eventssame)
  {
    for (int e1 = 0; e1 < eventssame[0].length; e1++) {
      for (int e2 = 0; e2 < eventssame[0][e1].length; e2++) {
        for (int i = 1; i < eventssame.length; i++) {
          eventssame[0][e1][e2] = eventssame[0][e1][e2] && eventssame[i][e1][e2];
          if (!eventssame[0][e1][e2]) {break;}
        }
      }
    }
    return eventssame[0];
  }

  public void update(final Set<AutomatonProxy> composed, final Set<AutomatonProxy> notcomposed,
                     final AutomatonProxy result, final ProductDESProxyFactory factory,
                     final Set<EventProxy> hidden, final KindTranslator kt)
  {
    final Map<EventProxy, EventProxy> replacementmap =
      new THashMap<EventProxy, EventProxy>();
    if (notcomposed.isEmpty()) {return;}
    final boolean[][][] eventssamearr = new boolean[notcomposed.size()][][];
    int a = 0;
    for (final AutomatonProxy aut : notcomposed) {
      try {
        eventssamearr[a] = calculateAllSame(aut, kt);
        a++;
      } catch (final OverflowException oe) {
        oe.printStackTrace();
        return;
      }
    }
    final boolean[][] eventssame = intersect(eventssamearr);
    for (final EventProxy e1 : result.getEvents()) {
      if (hidden.contains(e1)) {continue;}
      if (!mEventNumber.containsKey(e1)) {continue;}
      final int evnum1 = mEventNumber.get(e1);
      for (final EventProxy e2 : result.getEvents()) {
        if (hidden.contains(e2)) {continue;}
        if (e1 == e2) {continue;}
        if (!mEventNumber.containsKey(e2)) {continue;}
        final int evnum2 = mEventNumber.get(e2);
        if (eventssame[evnum1][evnum2]) {
          EventProxy replacement = replacementmap.get(e1);
          if (replacement == null) {
            replacement = e1;
            replacementmap.put(e1, replacement);
          }
          replacementmap.put(e2, replacement);
        }
      }
    }
    System.out.println("map");
    System.out.println(replacementmap);
    if (replacementmap.isEmpty()) {return;}
    replace(result, notcomposed, replacementmap, factory);
  }

  public AutomatonProxy getResult()
  {
    return mResult;
  }

  public SortedSet<AutomatonProxy> getNotComposed()
  {
    return mNotComposed;
  }

  public void replace(final AutomatonProxy result, final Set<AutomatonProxy> notcomposed,
                      final Map<EventProxy, EventProxy> replacementmap,
                      final ProductDESProxyFactory factory)
  {
    System.out.println("replacing");
    //System.out.println(result);
    final List<TransitionProxy> newTrans = new ArrayList<TransitionProxy>();
    final Set<EventProxy> removed = new THashSet<EventProxy>();
    for (final TransitionProxy tran : result.getTransitions()) {
      if (replacementmap.containsKey(tran.getEvent())) {
        final TransitionProxy t = factory.createTransitionProxy(tran.getSource(),
                                                          replacementmap.get(tran.getEvent()),
                                                          tran.getTarget());
        newTrans.add(t);
        removed.add(tran.getEvent());
        removed.remove(replacementmap.get(tran.getEvent()));
      } else {
        newTrans.add(tran);
      }
    }
    mResult = factory.createAutomatonProxy(result.getName(), result.getKind(),
                                           result.getEvents(),
                                           result.getStates(), newTrans);
    //System.out.println(mResult);
    //System.exit(1);
    // make automaton
    mNotComposed = new TreeSet<AutomatonProxy>();
    for (final AutomatonProxy aut : notcomposed) {
      final List<TransitionProxy> trans = new ArrayList<TransitionProxy>();
      for (final TransitionProxy tran : result.getTransitions()) {
        if (!removed.contains(tran.getEvent())) {
          newTrans.add(tran);
        }
      }
      mNotComposed.add(factory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                                    aut.getEvents(), aut.getStates(),
                                                    trans));
    }
  }
}









