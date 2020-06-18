//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.modular;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.annotation.AnnotatedMemStateProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


public class BlockedEvents
{
  public BlockedEvents(final List<AutomatonProxy> model,
		       		   final ProductDESProxyFactory factory,
                       final EventProxy marked)
  {
    mMarked = marked;
    mModel = model;
    mFactory = factory;
    mNodeLimit = 1000;
    numStates = 1;
    containsmarked = false;
  }

  public void setNodeLimit(final int stateLimit)
  {
    mNodeLimit = stateLimit;
  }

  private EventProxy[] unionEvents()
  {
    final THashSet<EventProxy> merge = new THashSet<EventProxy>();
    for (final AutomatonProxy a : mModel) {merge.addAll(a.getEvents());}
    final EventProxy[] events = merge.toArray(new EventProxy[merge.size()]);
    Arrays.sort(events);
    return events;
  }

  private Set<EventProxy> getDisabledEvents(final Collection<EventProxy> props)
  {
    for (final EventProxy e : props)
    {
      if (e instanceof DisabledEvents) {
        return new THashSet<EventProxy>(((DisabledEvents) e).getDisabled());
      }
    }
    return new THashSet<EventProxy>();
  }

  private int[] geteventnotinautomaton(final AutomatonProxy aut)
  {
    final int size = events.length - aut.getEvents().size();
    final int[] arr = new int[size];
    int i = 0;
    for (int e = 0; e < events.length; e++) {
      if (!aut.getEvents().contains(events[e])) {
        arr[i] = e; i++;
      }
    }
    return arr;
  }

  private AutomatonProxy createAutomaton(final boolean[][] eventOccurs,
                                         final int[][][] transitionsrel,
                                         final Set<EventProxy>[] disabled,
                                         final boolean[] initialStates,
                                         final boolean[] markedStates,
                                         final AutomatonProxy automaton)
  {
    final Collection<TransitionProxy> trans = new ArrayList<TransitionProxy>();
    final List<StateProxy> states = new ArrayList<StateProxy>();
    final int[] eventsnotinaut = geteventnotinautomaton(automaton);
    for (int s = 0; s < disabled.length; s++) {
      for (int ei = 0; ei < eventsnotinaut.length; ei++) {
        final int e = eventsnotinaut[ei];
        if (!eventOccurs[e][s]) {
          final EventProxy ev = events[e];
          disabled[s].add(ev);
        }
      }
      final Collection<EventProxy> props = new THashSet<EventProxy>();
      if (markedStates[s] && mMarked != null) {props.add(mMarked);}
      //if (!disabled[s].isEmpty()) {props.add(new DisabledEvents(disabled[s]));}
      final StateProxy ns = new AnnotatedMemStateProxy(s, props, initialStates[s]);
      states.add(ns);
    }
    boolean removed = false;
    for (int e = 0; e < transitionsrel.length; e++) {
      final int[][] tr = transitionsrel[e];
      if (tr == null) {continue;}
      for (int s = 0; s < tr.length; s++) {
        if (tr[s] != null && eventOccurs[e][s]) {
          for (int ti = 0; ti < tr[s].length; ti++) {
            final int t = tr[s][ti];
            trans.add(mFactory.createTransitionProxy(states.get(s), events[e],
                                                     states.get(t)));
          }
        } else if (tr[s] != null && !eventOccurs[e][s]) {
          //System.out.println(Arrays.toString(tr[s]));
          removed = true;
        }
      }
    }
    final String name = automaton.getName();
    final ComponentKind ck = automaton.getKind();
    final Collection<EventProxy> ev = automaton.getEvents();
    final AutomatonProxy aut = mFactory.createAutomatonProxy(name, ck, ev,
                                                       states, trans);
    if (removed) {
      System.out.println("removed: " + aut.getName());
      /*System.out.println("bef");
      System.out.println(automaton);
      System.out.println("aft");
      System.out.println(aut);*/
    }
    return aut;
  }

  @SuppressWarnings("unchecked")
  public List<AutomatonProxy> run()
    throws AnalysisException
  {
    mNewMarked = new TIntArrayList();
    states = new IntMap(mNodeLimit);
    TObjectIntHashMap<EventProxy> eventToIndex =
      new TObjectIntHashMap<EventProxy>();
    events = unionEvents();
    final int numAutomata = mModel.size();
    eventAutomaton = new int[events.length][numAutomata];
    // transitions indexed first by automaton then by event
    // then by source state
    transitions = new int[numAutomata][events.length][][];
    for (int i = 0; i < events.length; i++) {
      eventToIndex.put(events[i], i);
    }
    mEventToIndex = eventToIndex;
    final int[][] currentState = new int[numAutomata][];
    mMarkedStates = new boolean[numAutomata][];
    mDisabled = new Set[numAutomata][];
    mIntDisabled = new TIntHashSet[numAutomata][];
    mEventActivated = new boolean[numAutomata][events.length][];
    mInitialStates = new boolean[numAutomata][];
    for (int i = 0; i < mModel.size(); i++) {
      final AutomatonProxy a = mModel.get(i);
      final TObjectIntHashMap<StateProxy> statetoindex =
        new TObjectIntHashMap<StateProxy>(a.getStates().size());
      mMarkedStates[i] = new boolean[a.getStates().size()];
      mInitialStates[i] = new boolean[a.getStates().size()];
      mDisabled[i] = new Set[a.getStates().size()];
      mIntDisabled[i] = new TIntHashSet[a.getStates().size()];
      final TIntArrayList cs = new TIntArrayList(1);
      final boolean add = !a.getEvents().contains(mMarked);
      containsmarked = containsmarked || add;
      // TODO do this smarter
      int snum = 0;
      for (final StateProxy s : a.getStates()) {
        if (add || s.getPropositions().contains(mMarked)) {
          mMarkedStates[i][snum] = true;
        }
        final Set<EventProxy> dis = getDisabledEvents(s.getPropositions());
        mDisabled[i][snum] = dis;
        mIntDisabled[i][snum] = new TIntHashSet();
        for (final EventProxy e : dis) {
          if (eventToIndex.contains(e)) {
            mIntDisabled[i][snum].add(eventToIndex.get(e));
          }
          if (e.equals(mMarked)) {
            mMarkedStates[i][snum] = false;
          }
        }
        if (s.isInitial()) {
          cs.add(snum); mInitialStates[i][snum] = true;
        }
        statetoindex.put(s, snum); snum++;
      }
      currentState[i] = cs.toArray();
      // TODO do this smarter later
      final TIntArrayList[][] auttransitionslists =
        new TIntArrayList[events.length][a.getStates().size()];
      for (int e = 0; e < events.length; e++) {
        mEventActivated[i][e] = new boolean[a.getStates().size()];
      }
      for (final TransitionProxy t : a.getTransitions()) {
        final int event = eventToIndex.get(t.getEvent());
        final int source = statetoindex.get(t.getSource());
        final int target = statetoindex.get(t.getTarget());
        TIntArrayList list = auttransitionslists[event][source];
        if (list == null) {
          list = new TIntArrayList(1);
          auttransitionslists[event][source] = list;
        }
        list.add(target);
      }
      for (int j = 0; j < auttransitionslists.length; j++) {
        if (!a.getEvents().contains(events[j])) {continue;}
        transitions[i][j] = new int[a.getStates().size()][];
        for (int k = 0; k < auttransitionslists[j].length; k++) {
          final TIntArrayList list = auttransitionslists[j][k];
          if (list != null) {
            final int[] targs = list.toArray(); transitions[i][j][k] = targs;
          }
        }
      }
    }
    //no longer used
    eventToIndex = null;
    //for (int i = 0; i < mMarkedStates.length; i++) {
      //System.out.println("Marked:" + i + " " + Arrays.toString(mMarkedStates[i]));
    //}
    for (int i = 0; i < events.length; i++) {
      final IntDouble[] list = new IntDouble[numAutomata];
      for (int j = 0; j < mModel.size(); j++) {
        list[j] = new IntDouble(j, 0);
        if (transitions[j][i] != null) {
          for (int k = 0; k < transitions[j][i].length; k++) {
            if (transitions[j][i][k] != null) {
              list[j].mDouble++;
            }
          }
          list[j].mDouble /= transitions[j][i].length;
        } else {
          list[j].mDouble = Double.POSITIVE_INFINITY;
        }
      }
      Arrays.sort(list);
      for (int j = 0; j < eventAutomaton[i].length; j++) {
        eventAutomaton[i][j] = list[j].mInt;
      }
    }

    // Time to start building the automaton
    numStates = 0;
    unvisited = new ArrayBag(100);
    permutations(currentState, new int[numAutomata], 0,
                 -1, -1, true);
    while (!unvisited.isEmpty()) {
      final int[] cs = unvisited.take();
      //System.out.println(Arrays.toString(currentState));
      //explore(currentState, true);
      explore(cs);
    }
    final List<AutomatonProxy> automata = new ArrayList<AutomatonProxy>(numAutomata);
    for (int a = 0; a < mModel.size(); a++) {
      automata.add(createAutomaton(mEventActivated[a], transitions[a],
                                   mDisabled[a], mInitialStates[a],
                                   mMarkedStates[a], mModel.get(a)));
    }
    return automata;
  }

  private void addState(final int[] successor, final int source,
                        final int event, final boolean isInitial)
    throws AnalysisException
  {
    Integer target = states.get(successor);
    //System.out.println("suc:" + Arrays.toString(suc) + "targ:" + target);
    if (target == null) {
      target = numStates;
      states.put(successor, target);
      numStates++;
      if (numStates > mNodeLimit) {
        throw new AnalysisException("State Limit Exceeded");
      }
      unvisited.offer(successor);
      if (containsmarked) {
        if (determineMarked(successor)) {
          for (int a = 0; a < successor.length; a++) {
            final int s = successor[a];
            final int e = mEventToIndex.get(mMarked);
            mEventActivated[a][e][s] = true;
          }
        }
      }
    }
  }

  private void permutations(final int[][] suc, final int[] perm, final int depth,
                            final int source, final int event, final boolean isInitial)
    throws AnalysisException
  {
    if (depth== perm.length) {
      final int[] successor = new int[depth];
      for (int i = 0; i < perm.length; i++) {
        successor[i] = suc[i][perm[i]];
      }
      addState(successor, source, event, isInitial);
      return;
    }
    for (int i = 0; i < suc[depth].length; i++) {
      perm[depth] = i;
      permutations(suc, perm, depth + 1, source, event, isInitial);
    }
  }

  public boolean explore(final int[] state)
    throws AnalysisException
  {
    final boolean result = false;
    final int numAutomata = transitions.length;
    final int source = states.get(state);
    events:
    for (int i = 0; i < events.length; i++) {
      final int[][] suc = new int[numAutomata][];
      for (int l = 0; l < numAutomata; l++) {
        final int automaton = eventAutomaton[i][l];
        if (transitions[automaton][i] != null) {
          suc[automaton] = transitions[automaton][i][state[automaton]];
        } else {
          final int s = state[automaton];
          if (!mIntDisabled[automaton][s].contains(s)) {
            suc[automaton] = new int[]{state[automaton]};
          }
        }
        if (suc[automaton] == null) {
          /*if (l > 0) {
            int t = eventAutomaton[i][l];
            eventAutomaton[i][l] = eventAutomaton[i][l - 1];
            eventAutomaton[i][l - 1] = t;
          }*/
          continue events;
        }
      }
      for (int a = 0; a < state.length; a++) {
        final int s = state[a]; mEventActivated[a][i][s] = true;
      }
      final int[] perms = new int[numAutomata];
      permutations(suc,perms,0,source,i, false);
    }
    return result;
  }

  private boolean determineMarked(final int[] suc)
  {
    //System.out.println("state:" + Arrays.toString(suc));
    for (int i = 0; i < suc.length; i++) {
      if (!mMarkedStates[i][suc[i]]) {
        return false;
      }
    }
    return true;
  }

  private static interface Bag
  {
    /** is the bag empty*/
    public boolean isEmpty();

    /** add a new item to the bag */
    public void offer(int[] a);

    /** remove an arbitrary item from the bag */
    public int[] take();
  }

  @SuppressWarnings("unused")
  private static class IntBag
  {
    private int mLength;
    private final int mInitialSize;
    private int[] mValues;

	public IntBag(final int initialSize)
    {
      mLength = 0;
      mInitialSize = initialSize;
      mValues = new int[mInitialSize];
    }

    public boolean isEmpty()
    {
      return mLength == 0;
    }

    public void offer(final int a)
    {
      if (mLength == mValues.length) {
        final int[] newArray = new int[mValues.length*2];
        // from memory this is actually faster than using the Arrays method for it
        for (int i = 0; i < mValues.length; i++) {
          newArray[i] = mValues[i];
        }
        mValues = newArray;
      }
      mValues[mLength] = a;
      mLength++;
    }

    public int take()
    {
      mLength--;
      final int a = mValues[mLength];
      if (mValues.length > mInitialSize && (mLength <= (mValues.length / 4))) {
        final int[] newArray = new int[mValues.length / 2];
        for (int i = 0; i < mLength; i++) {
          newArray[i] = mValues[i];
        }
        mValues = newArray;
      }
      return a;
    }
  }

  private static class ArrayBag
    implements Bag
  {
    private int mLength;
    private final int mInitialSize;
    private int[][] mValues;

    public ArrayBag(final int initialSize)
    {
      mLength = 0;
      mInitialSize = initialSize;
      mValues = new int[mInitialSize][];
    }

    @Override
    public boolean isEmpty()
    {
      return mLength == 0;
    }

    @Override
    public void offer(final int[] a)
    {
      if (mLength == mValues.length) {
        final int[][] newArray = new int[mValues.length*2][];
        // from memory this is actually faster than using the Arrays method for it
        for (int i = 0; i < mValues.length; i++) {
          newArray[i] = mValues[i];
        }
        mValues = newArray;
      }
      mValues[mLength] = a;
      mLength++;
    }

    @Override
    public int[] take()
    {
      mLength--;
      final int[] a = mValues[mLength];
      // this shouldn't actually save any memory as the Map will still reference the array but oh well
      mValues[mLength] = null;
      if (mValues.length > mInitialSize && (mLength <= (mValues.length / 4))) {
        final int[][] newArray = new int[mValues.length / 2][];
        for (int i = 0; i < mLength; i++) {
          newArray[i] = mValues[i];
        }
        mValues = newArray;
      }
      return a;
    }
  }

  /**
   *  I'll make this encode it properly later on
   *
   */
  @SuppressWarnings("unused")
  private int[] encode(final int[] sState)
  {
    return sState;
  }

  @SuppressWarnings("unused")
  private int[] decode(final int[] sState)
  {
    return sState;
  }

  private static class IntMap
    extends AbstractMap<int[], Integer>
  {
    final Map<IntArray, Integer> mMap;

    public IntMap(final int num)
    {
      mMap = new HashMap<IntArray, Integer>(num);
    }

    @Override
    public Set<Map.Entry<int[],Integer>> entrySet()
    {
      return null; // I don't think i'll be using this method so meh
    }

    @Override
    public Integer get(final Object o)
    {
      final int[] a = (int[]) o;
      return mMap.get(new IntArray(a));
    }

    @SuppressWarnings("unused")
	public Integer get(final int[] a)
    {
      return mMap.get(new IntArray(a));
    }

    @SuppressWarnings("unused")
	public Integer put(final Object o, final Integer s)
    {
      return mMap.put(new IntArray((int[])o), s);
    }

    @Override
    public Integer put(final int[] a, final Integer s)
    {
      return mMap.put(new IntArray(a), s);
    }

    @Override
    public Collection<Integer> values()
    {
      return mMap.values();
    }
  }

  private static class IntArray
  {
    public final int[] mArray;

    public IntArray(final int[] array)
    {
      mArray = array;
    }

    @Override
    public int hashCode()
    {
      return Arrays.hashCode(mArray);
    }

    @Override
    public boolean equals(final Object o)
    {
      final IntArray oth = (IntArray)o;
      if (oth.mArray.length != mArray.length) {
        return false;
      }
      for (int i = 0; i < mArray.length; i++) {
        if (mArray[i] != oth.mArray[i]) {
          return false;
        }
      }
      return true;
    }

    @Override
    public String toString()
    {
      return Arrays.toString(mArray);
    }
  }

  private static class IntDouble
    implements Comparable<IntDouble>
  {
    final public int mInt;
    public double mDouble;

    public IntDouble(final int i, final double d)
    {
      mInt = i;
      mDouble = d;
    }

    @Override
    public int compareTo(final IntDouble id)
    {
      if (mDouble < id.mDouble) {
        return -1;
      } else if (mDouble > id.mDouble){
        return 1;
      }
      return 0;
    }
  }

  public int getDumpState()
  {
    return mDumpState;
  }

  @SuppressWarnings("unused")
  private static class Pointer
    implements Comparable<Pointer>
  {
    EventProxy[] mArray;
    int mIndex;

	public Pointer(final EventProxy[] array)
    {
      mArray = array;
      mIndex = 0;
    }

    public boolean increment()
    {
      mIndex++;
      return mIndex < mArray.length;
    }

    public EventProxy getCurrent()
    {
      return mArray[mIndex];
    }

    @Override
    public int compareTo(final Pointer p)
    {
      return mArray[mIndex].compareTo(p.mArray[p.mIndex]);
    }
  }

  private TIntHashSet[][] mIntDisabled;
  private Set<EventProxy>[][] mDisabled;
  private boolean[][][] mEventActivated;
  @SuppressWarnings("unused")
  private final int mCompositionSize = 0;
  private int mNodeLimit;
  private final List<AutomatonProxy> mModel;
  private final ProductDESProxyFactory mFactory;
  private Map<int[], Integer> states;
  @SuppressWarnings("unused")
  private Collection<TransitionProxy> trans;
  private EventProxy[] events;
  private int[][][][] transitions;
  @SuppressWarnings("unused")
  private TIntArrayList[][] mBackTransitions;
  private boolean[][] mInitialStates;
  private boolean[][] mMarkedStates;
  @SuppressWarnings("unused")
  private TIntArrayList mNewMarked;
  @SuppressWarnings("unused")
  private final List<int[]> newtrans = new ArrayList<int[]>();
  private int numStates;
  private Bag unvisited;
  private int[][] eventAutomaton;
  private final EventProxy mMarked;
  @SuppressWarnings("unused")
  private int mNewDumpState;
  private final int mDumpState = -1;
  @SuppressWarnings("unused")
  private final TIntHashSet mNewInitial = new TIntHashSet();
  private boolean containsmarked;
  private TObjectIntHashMap<EventProxy> mEventToIndex;
}
