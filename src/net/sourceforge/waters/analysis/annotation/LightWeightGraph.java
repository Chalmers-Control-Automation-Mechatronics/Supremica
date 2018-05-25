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

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.plain.base.NamedElement;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class LightWeightGraph
  extends NamedElement
  implements AutomatonProxy
{
  private static final long serialVersionUID = 1L;

  private final int[][][] mTransitions;
  private final EventProxy[] mEvents;
  private final boolean[] mMarked;
  private final EventProxy mMark;
  private final ProductDESProxyFactory mFactory;
  private final int mStateNum;
  private Set<EventProxy> mEventSet;
  private Set<StateProxy> mStateSet;
  private Collection<TransitionProxy> mTransitionSet;

  public LightWeightGraph(final String name, final int[][] transitions, final int statenum,
                          final EventProxy[] events, final int[] marked, final EventProxy mark,
                          final ProductDESProxyFactory factory)
  {
    super(name);
    mStateNum = statenum;
    mTransitions = null; //transitions;
    mEvents = events;
    mMarked = new boolean[mStateNum];
    for (int i = 0; i < marked.length; i++) {mMarked[marked[i]] = true;}
    mMark = mark;
    mFactory = factory;
  }

  public LightWeightGraph(final String name, final int[][] transitions, final int statenum,
                          final EventProxy[] events, final boolean[] marked, final EventProxy mark,
                          final ProductDESProxyFactory factory)
  {
    super(name);
    mStateNum = statenum;
    mTransitions = null; //transitions;
    mEvents = events;
    mMarked = marked;
    mMark = mark;
    mFactory = factory;
  }

  public LightWeightGraph(final AutomatonProxy graph, final ProductDESProxyFactory factory,
                          final EventProxy mark)
  {
    super(graph.getName());
    mStateNum = graph.getStates().size();
    StateProxy[] states = new StateProxy[mStateNum];
    states = graph.getStates().toArray(states);
    mEvents = graph.getEvents().toArray(new EventProxy[0]);
    Arrays.sort(mEvents);
    mMarked = new boolean[mStateNum];
    mTransitions = new int[mEvents.length][mStateNum][];
    mMark = mark;
    final TObjectIntHashMap<StateProxy> statemap =
      new TObjectIntHashMap<StateProxy>(mStateNum);
    final TObjectIntHashMap<EventProxy> eventmap =
      new TObjectIntHashMap<EventProxy>(mEvents.length);
    // puts initial state at the front
    for (int i = 0; i < states.length; i++) {
      if (states[i].isInitial()) {
        final StateProxy t = states[0]; states[0] = states[i]; states[i] = t; break;
      }
    }
    // sets up the state map and marked states
    for (int i = 0; i < states.length; i++) {
      statemap.put(states[i], i);
      if (states[i].getPropositions().contains(mMark)) {mMarked[i] = true;}
    }
    // set up the event map
    for (int i = 0; i < mEvents.length; i++) {eventmap.put(mEvents[i], i);}
    final TIntArrayList[][] temptransitions = new TIntArrayList[mEvents.length][mStateNum];
    for (final TransitionProxy t : graph.getTransitions()) {
      final int source = statemap.get(t.getSource());
      final int target = statemap.get(t.getTarget());
      final int event = eventmap.get(t.getEvent());
      TIntArrayList targs = temptransitions[event][source];
      if (targs == null) {
        targs = new TIntArrayList(); temptransitions[event][source] = targs;
      }
      targs.add(target);
    }
    for (int i = 0; i < temptransitions.length; i++) {
      for (int j = 0; j < temptransitions[i].length; j++) {
        final TIntArrayList t = temptransitions[i][j];
        if (t == null) {continue;}
        mTransitions[i][j] = t.toArray();
      }
    }
    mFactory = factory;
  }

  @Override
  public LightWeightGraph clone()
  {
    assert(false);
    return null;
    //return new LightWeightGraph(getName(), null /*mTransitions*/, mStateNum, mEvents,
    //                            mMarked, mMark, mFactory);
  }

  @Override
  public Set<EventProxy> getEvents()
  {
    if (mEventSet == null) {
      mEventSet = new HashSet<EventProxy>(Arrays.asList(mEvents));
    }
    return mEventSet;
  }

  /*private int[][] compress(int[][] transitions)
  {
    int[] active = new int[mStateNum];
    TObjectIntHashMap<int[]> map = new TObjectIntHashMap<int[]>(ArrayHash.ARRAYHASH);
    for (int i = 0; i < transitions.length; i++) {
      int count = map.get(transitions[i]);
      if (count == 0) {
        active[transitions[i][0]]++;
      }
      count++;
      map.put(transitions[i], count);
    }
    final int[][][] newtransitions = new int[mStateNum][][];
    for (int i = 0; i < active.length; i++) {
      newtransitions[i] = new int[active[i]][];
      active[i] = 0; // now use this to count which one we are up to
    }
    map.forEachEntry(new TObjectIntProcedure() {
      public boolean execute(int[] arr, int val)
      {
        int source = arr[0];
        int event = active[source];
        active[source]++;

        return true;
      }
    })
  }*/

  private void setup()
  {
    System.out.println("Setup");
    final StateProxy[] states = new StateProxy[mStateNum];
    int j = 0;
    final boolean[] marked = mMarked;
    for (int i = 0; i < states.length; i++) {
      if (marked[i]) {
        j++;
      }
      states[i] = new MemStateProxy(i, mMark);
    }
    mStateSet = new HashSet<StateProxy>(Arrays.asList(states));
    mTransitionSet = new ArrayList<TransitionProxy>(mTransitions.length);
    for (int i = 0; i < mTransitions.length; i++) {
      final EventProxy e = mEvents[i];
      for (j = 0; j < mTransitions[i].length; j++) {
        final StateProxy s = states[j];
        if (mTransitions[i][j] != null) {
          for (int k = 0; k < mTransitions[i][j].length; k++) {
            final StateProxy t = states[mTransitions[i][j][k]];
            mTransitionSet.add(mFactory.createTransitionProxy(s, e, t));
          }
        }
      }
    }
  }

  @Override
  public Set<StateProxy> getStates()
  {
    if (mStateSet == null) {
      setup();
    }
    return mStateSet;
  }

  @Override
  public Collection<TransitionProxy> getTransitions()
  {
    if (mTransitionSet == null) {
      setup();
    }
    return mTransitionSet;
  }

  @Override
  public Map<String,String> getAttributes()
  {
    return null;
  }

  @Override
  public ComponentKind getKind()
  {
    return ComponentKind.SPEC;
  }

  public int[][] getLightTransitions()
  {
    return new int[0][0];
    //return mTransitions;
  }

  public boolean[] getMarkedStates()
  {
    return mMarked;
  }

  public EventProxy getMarked()
  {
    return mMark;
  }

  public EventProxy[] getLightEvents()
  {
    return mEvents;
  }

  public int getStateNum()
  {
    return mStateNum;
  }

  @Override
  public int hashCode()
  {
    return System.identityHashCode(this);
  }

  @Override
  public boolean equals(final Object o)
  {
    return System.identityHashCode(this) == System.identityHashCode(o);
  }

  private static class MemStateProxy
    implements StateProxy
  {
    private final int mName;
    private final EventProxy mEvent;

    public MemStateProxy(final int name, final EventProxy event)
    {
      mName = name;
      mEvent = event;
    }

    @Override
    public Collection<EventProxy> getPropositions()
    {
      if (mEvent == null) {
        return Collections.emptySet();
      } else {
        return Collections.singleton(mEvent);
      }
    }

    @Override
    public boolean isInitial()
    {
      return mName == 0;
    }

    @Override
    public MemStateProxy clone()
    {
      return new MemStateProxy(mName, mEvent);
    }

    @Override
    public String getName()
    {
      return Integer.toString(mName);
    }

    @SuppressWarnings("unused")
	public boolean refequals(final Object o)
    {
      if (o instanceof NamedProxy) {
        return refequals((NamedProxy) o);
      }
      return false;
    }

    @Override
    public boolean refequals(final NamedProxy o)
    {
      if (o instanceof MemStateProxy) {
        final MemStateProxy s = (MemStateProxy) o;
        return s.mName == mName;
      } else {
        return false;
      }
    }

    @Override
    public int refHashCode()
    {
      return mName;
    }

    @Override
    public Object acceptVisitor(final ProxyVisitor visitor)
      throws VisitorException
    {
      final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
      return desvisitor.visitStateProxy(this);
    }

    @Override
    public Class<StateProxy> getProxyInterface()
    {
      return StateProxy.class;
    }

    @Override
    public int compareTo(final NamedProxy n)
    {
      return n.getName().compareTo(getName());
    }

    @Override
    public String toString()
    {
      return "S:" + mName;
    }
  }

  @Override
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitAutomatonProxy(this);
  }

  @Override
  public Class<AutomatonProxy> getProxyInterface()
  {
    return AutomatonProxy.class;
  }

}
