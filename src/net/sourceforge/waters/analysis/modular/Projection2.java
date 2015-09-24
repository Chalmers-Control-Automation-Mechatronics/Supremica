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

package net.sourceforge.waters.analysis.modular;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.tr.IntArrayHashingStrategy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractAutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.NondeterministicDESException;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class Projection2
  extends AbstractAutomatonBuilder
  implements SafetyProjectionBuilder
{

  //#########################################################################
  //# Constructor
  public Projection2(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public Projection2(final ProductDESProxy model,
                     final ProductDESProxyFactory factory,
                     final Set<EventProxy> hide,
                     final Set<EventProxy> forbidden)
  {
    super(model, factory);
    mHide = hide;
    mForbidden = new THashSet<EventProxy>(forbidden);
  }


  //#########################################################################
  //# Configuration
  @Override
  public Set<EventProxy> getHidden()
  {
    return mHide;
  }

  @Override
  public void setHidden(final Set<EventProxy> hidden)
  {
    mHide = hidden;
  }

  @Override
  public Set<EventProxy> getForbidden()
  {
    return mForbidden;
  }

  @Override
  public void setForbidden(final Set<EventProxy> forbidden)
  {
    mForbidden = forbidden;
  }


  //#########################################################################
  //# Invocation
  // @Override -- @Override gives compiler error???
  @Override
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();
      final ProductDESProxy model = getModel();
      final int limit = getNodeLimit();
      final IntArrayHashingStrategy strategy = new IntArrayHashingStrategy();
      mStates = new TObjectIntCustomHashMap<int[]>(strategy);
      mTransitions = new ArrayList<TransitionProxy>();
      mEvents = model.getEvents().toArray(new EventProxy[model.getEvents().size()]);
      mPossible = new boolean[mEvents.length];
      final int numAutomata = model.getAutomata().size();
      AutomatonProxy[] automata = model.getAutomata().toArray(new AutomatonProxy[numAutomata]);
      mEventAutomaton = new int[mEvents.length][numAutomata];
      int l = 0;
      // transitions indexed first by automaton then by event then by source state
      mTransitionTable = new int[numAutomata][mEvents.length][];
      // go through and put all the events to be hidden to the front
      TObjectIntHashMap<EventProxy> eventToIndex =
        new TObjectIntHashMap<EventProxy>(mEvents.length);
      for (int i = 0; i < mEvents.length; i++) {
        if (mHide.contains(mEvents[i])) {
          final EventProxy temp = mEvents[i];
          mEvents[i] = mEvents[l];
          mEvents[l] = temp;
          l++;
        }
      }
      assert(l == mHide.size());
      //put all the forbidden events directly after the Hidden ones
      for (int i = l; i < mEvents.length; i++) {
        if (mForbidden.contains(mEvents[i])) {
          final EventProxy temp = mEvents[i];
          mEvents[i] = mEvents[l];
          mEvents[l] = temp;
          l++;
        }
      }
      for (int i = 0; i < mEvents.length; i++) {
        eventToIndex.put(mEvents[i], i);
      }
      assert(l == mHide.size() + mForbidden.size());
      // this has to be done after events gets it's final ordering to avoid
      // subtle bugs
      for (int i = 0; i < mEvents.length; i++) {
        for (int j = 0; j < automata.length; j++) {
          if (automata[j].getEvents().contains(mEvents[i])) {
            final int[] states1 = new int[automata[j].getStates().size()];
            Arrays.fill(states1, -1);
            mTransitionTable[j][i] = states1;
          } else {
            mTransitionTable[j][i] = null;
          }
        }
      }
      int[] currentState = new int[numAutomata];
      for (int i = 0; i < automata.length; i++) {
        final AutomatonProxy aut = automata[i];
        final TObjectIntHashMap<StateProxy> stateMap =
          new TObjectIntHashMap<StateProxy>(aut.getStates().size());
        l = 0;
        for (final StateProxy s : aut.getStates()) {
          if (s.isInitial()) {
            currentState[i] = l;
          }
          stateMap.put(s, l);
          l++;
        }
        assert(l == aut.getStates().size());
        for (final TransitionProxy trans : aut.getTransitions()) {
          final StateProxy source = trans.getSource();
          final int scode = stateMap.get(source);
          final EventProxy event = trans.getEvent();
          final int ecode = eventToIndex.get(event);
          final StateProxy target = trans.getTarget();
          final int tcode = stateMap.get(target);
          final int current = mTransitionTable[i][ecode][scode];
          if (current < 0) {
            mTransitionTable[i][ecode][scode] = tcode;
          } else if (current != tcode) {
            throw new NondeterministicDESException(aut, source, event);
          }
        }
      }
      for (int i = 0; i < mEvents.length; i++) {
        final IntDouble[] list = new IntDouble[numAutomata];
        for (int j = 0; j < automata.length; j++) {
          list[j] = new IntDouble(j, 0);
          if (mTransitionTable[j][i] != null) {
            for (int k = 0; k < mTransitionTable[j][i].length; k++) {
              if (mTransitionTable[j][i][k] != -1) {
                list[j].mDouble++;
              }
            }
            list[j].mDouble /= mTransitionTable[j][i].length;
          } else {
            list[j].mDouble = Double.POSITIVE_INFINITY;
          }
        }
        Arrays.sort(list);
        for (int j = 0; j < mEventAutomaton[i].length; j++) {
          mEventAutomaton[i][j] = list[j].mInt;
        }
      }
      // don't need these anymore
      automata = null;
      eventToIndex = null;

      // Time to start building the automaton
      mNumberOfStates = 1;
      currentState = encode(currentState);
      mStates.put(currentState, 0);
      mUnvisited = new ArrayBag(100);
      mUnvisited.offer(currentState);
      while (!mUnvisited.isEmpty()) {
        currentState = mUnvisited.take();
        if (!explore(currentState, true)) {
          explore(currentState, false);
        }
      }
      mStates = null;
      currentState = new int[] {0};
      mTransitionTable = new int[1][mEvents.length][mNumberOfStates];
      for (int i = 0; i < mTransitionTable[0].length; i++) {
        for (int j = 0; j < mTransitionTable[0][i].length; j++) {
          mTransitionTable[0][i][j] = -1;
        }
      }
      for (final int[] tran : mNewTransitions) {
        mTransitionTable[0][tran[1]][tran[0]] = tran[2];
      }
      mNumberOfStates = 1;
      currentState = actualState(currentState);
      mNewStates = new StateMap(limit);
      mNewStates.put(currentState, new MemStateProxy(0));
      mUnvisited.offer(currentState);
      while (!mUnvisited.isEmpty()) {
        currentState = mUnvisited.take();
        if (!explore2(currentState, true)) {
          explore2(currentState, false);
        }
      }
      final Collection<EventProxy> ev = new ArrayList<EventProxy>(model.getEvents());
      ev.removeAll(mHide);

      final StringBuilder name = new StringBuilder();
      name.append("proj:");
      final ArrayList<String> names = new ArrayList<String>(model.getAutomata().size());
      for (final AutomatonProxy a : model.getAutomata()) {
        names.add(a.getName());
      }
      Collections.sort(names);
      final int namesize = names.size();
      for (int i=0;i<namesize;i++) {
        name.append(names.get(i));
        if (i<namesize-1) name.append(",");
      }
      final ProductDESProxyFactory factory = getFactory();
      AutomatonProxy result =
        factory.createAutomatonProxy(name.toString(), ComponentKind.PLANT,
                                     ev, mNewStates.values(), mTransitions);
      mNewStates = null;
      mTransitions = null;
      final Minimizer min = new Minimizer(result, factory);
      result = min.run();
      return setAutomatonResult(result);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }


  //#########################################################################
  //# Auxiliary Methods
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ProductDESProxy model = getModel();
    final Collection<EventProxy> events = model.getEvents();
    if (mHide == null) {
      mHide = Collections.emptySet();
    }
    if (mForbidden == null) {
      mForbidden = Collections.emptySet();
    } else {
      mForbidden.retainAll(events);
    }
    mDisabled = new THashSet<EventProxy>(events);
    mDisabled.removeAll(mHide);
    mNumberOfStates = 1;
    mNewTransitions = new ArrayList<int[]>();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mDisabled = null;
    mStates = null;
    mTransitions = null;
    mEvents = null;
    mTransitionTable = null;
    mNewTransitions = null;
    mNewStates = null;
    mUnvisited = null;
    mEventAutomaton = null;
  }


  //#########################################################################
  //# Algorithm
  private boolean explore(int[] state, final boolean forbidden)
    throws OverflowException
  {
    boolean result = false;
    final int numAutomata = mTransitionTable.length;
    final int source = mStates.get(state);
    state = decode(state);
    int min, max;
    if (forbidden) {
      min = mHide.size();
      max = mHide.size() + mForbidden.size();
    } else {
      min = 0;
      max = mEvents.length;
    }
    events:
    for (int i = min; i < max; i++) {
      if (!forbidden) {
        if (mHide.size() >= i && (mHide.size() + mForbidden.size()) < i) {
          continue;
        }
      }
      final int[] suc = new int[numAutomata];
      for (int l = 0; l < numAutomata; l++) {
        final int automaton = mEventAutomaton[i][l];
        if (mTransitionTable[automaton][i] != null) {
          suc[automaton] = mTransitionTable[automaton][i][state[automaton]];
        } else {
          suc[automaton] = state[automaton];
        }
        if (suc[automaton] == -1) {
          if (l > 0) {
            final int t = mEventAutomaton[i][l];
            mEventAutomaton[i][l] = mEventAutomaton[i][l - 1];
            mEventAutomaton[i][l - 1] = t;
          }
          continue events;
        }
      }
      result = true;
      mDisabled.remove(mEvents[i]);
      int target;
      if (mStates.containsKey(suc)) {
        target = mStates.get(suc);
      } else {
        target = mNumberOfStates;
        mStates.put(suc, target);
        mNumberOfStates++;
        final int limit = getNodeLimit();
        if (mNumberOfStates > limit) {
          throw new OverflowException(limit);
        }
        mUnvisited.offer(suc);
      }
      mNewTransitions.add(new int[] {source, i, target});
      mPossible[i] = true;
    }
    return result;
  }

  private boolean explore2(final int[] state, final boolean forbidden)
    throws OverflowException
  {
    int min, max;
    if (forbidden) {
      min = mHide.size();
      max = mHide.size() + mForbidden.size();
    } else {
      min = mHide.size() + mForbidden.size();
      max = mEvents.length;
    }
    boolean result = false;
    //System.out.println("state:" + Arrays.toString(state));
    final ProductDESProxyFactory factory = getFactory();
    final StateProxy source = mNewStates.get(state);
    for (int i = min; i < max; i++) {
      final TIntArrayList successor = new TIntArrayList(state.length);
      for (int j = 0; j < state.length; j++) {
        final int s = mTransitionTable[0][i][state[j]];
        if (s == -1) {
          continue;
        }
        successor.add(s);
      }
      //System.out.println("successor:" + successor);
      if (successor.isEmpty()) {
        continue;
      }
      result = true;
      int[] succ = new int[successor.size()];
      for (int j = 0; j < succ.length; j++) {
        succ[j] = successor.get(j);
      }
      succ = actualState(succ);
      StateProxy target = mNewStates.get(succ);
      if (target == null) {
        target = new MemStateProxy(mNumberOfStates);
        mNewStates.put(succ, target);
        mNumberOfStates++;
        final int limit = getNodeLimit();
        if (mNumberOfStates > limit) {
          throw new OverflowException(limit);
        }
        mUnvisited.offer(succ);
      }
      mTransitions.add(factory.createTransitionProxy(source, mEvents[i], target));
    }
    return result;
  }

  private int[] actualState(final int[] state)
  {
    final int numHidden = mHide.size();
    final SortedSet<Integer> setofstates = new TreeSet<Integer>();
    final IntBag nextstate = new IntBag(100);
    for (int i = 0; i < state.length; i++) {
      if (setofstates.add(state[i])) {
        nextstate.offer(state[i]);
      }
    }
    // find out what states can be reached from state with hidden events
    while (!nextstate.isEmpty()) {
      final int s = nextstate.take();
      for (int i = 0; i < numHidden; i++) {
        final int newstate = mTransitionTable[0][i][s];
        if (newstate == -1) {
          continue;
        }
        if (setofstates.add(newstate)) {
          nextstate.offer(newstate);
        }
      }
    }
    final int[] result = new int[setofstates.size()];
    int l = 0;
    for (final int a : setofstates) {
      result[l] = a;
      l++;
    }
    assert(result.length == l);
    return result;
  }


  //#########################################################################
  //# Inner Class MemStateProxy
  private static class MemStateProxy
    implements StateProxy
  {
    private final int mName;

    public MemStateProxy(final int name)
    {
      mName = name;
    }

    @Override
    public Collection<EventProxy> getPropositions()
    {
      return Collections.emptySet();
    }

    @Override
    public boolean isInitial()
    {
      return mName == 0;
    }

    @Override
    public MemStateProxy clone()
    {
      return new MemStateProxy(mName);
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
      final ProductDESProxyVisitor desvisitor =
	(ProductDESProxyVisitor) visitor;
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
  private int[] encode(final int[] sState)
  {
    return sState;
  }

  private int[] decode(final int[] sState)
  {
    return sState;
  }

  private static class StateMap
    extends AbstractMap<int[], StateProxy>
  {
    final Map<IntArray, StateProxy> mMap;

    public StateMap(final int num)
    {
      mMap = new HashMap<IntArray, StateProxy>(num);
    }

    @Override
    public int size()
    {
      return mMap.size();
    }

    @Override
    public Set<Map.Entry<int[],StateProxy>> entrySet()
    {
      return null; // I don't think i'll be using this method so meh
    }

    @Override
    public StateProxy get(final Object o)
    {
      final int[] a = (int[]) o;
      return mMap.get(new IntArray(a));
    }

    @SuppressWarnings("unused")
	public StateProxy get(final int[] a)
    {
      return mMap.get(new IntArray(a));
    }

    @Override
    public StateProxy put(final int[] a, final StateProxy s)
    {
      return mMap.put(new IntArray(a), s);
    }

    @Override
    public Collection<StateProxy> values()
    {
      return mMap.values();
    }
  }


  //#######################################################################
  //# Inner Class IntArray
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

  private boolean[] mPossible;
  private Set<EventProxy> mHide;
  private Set<EventProxy> mForbidden;
  private Set<EventProxy> mDisabled;
  private TObjectIntCustomHashMap<int[]> mStates;
  private Collection<TransitionProxy> mTransitions;
  private EventProxy[] mEvents;
  private int[][][] mTransitionTable;
  private List<int[]> mNewTransitions = new ArrayList<int[]>();
  private Map<int[], StateProxy> mNewStates;
  private int mNumberOfStates;
  private Bag mUnvisited;
  private int[][] mEventAutomaton;
}
