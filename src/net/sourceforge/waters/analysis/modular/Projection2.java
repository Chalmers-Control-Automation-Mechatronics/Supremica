//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   Projection2
//###########################################################################
//# $Id: Projection2.java,v 1.5 2008-06-30 21:05:32 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
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
{
  public Projection2(ProductDESProxy model, ProductDESProxyFactory factory,
                     Set<EventProxy> hide, Set<EventProxy> forbidden)
  {
    mModel = model;
    mFactory = factory;
    mHide = hide;
    mForbidden = new HashSet<EventProxy>(forbidden);
    mForbidden.retainAll(mModel.getEvents());
    mNodeLimit = 1000;
    mDisabled = new HashSet<EventProxy>(mModel.getEvents());
    mDisabled.remove(mHide);
    numStates = 1;
  }

  public void setNodeLimit(int stateLimit)
  {
    mNodeLimit = stateLimit;
  }
  
  public AutomatonProxy project()
    throws AnalysisException
  {
    states = new IntMap(mNodeLimit);
    trans = new ArrayList<TransitionProxy>();
    events = mModel.getEvents().toArray(new EventProxy[mModel.getEvents().size()]);
    int numAutomata = mModel.getAutomata().size();
    AutomatonProxy[] aut = mModel.getAutomata().toArray(new AutomatonProxy[numAutomata]);
    eventAutomaton = new int[events.length][numAutomata];
    int stateLength = numAutomata;
    int l = 0;
    // transitions indexed first by automaton then by event then by source state
    transitions = new int[numAutomata][events.length][];
    // go through and put all the events to be hidden to the front
    Map<EventProxy, Integer> eventToIndex = new HashMap<EventProxy, Integer>(events.length);
    for (int i = 0; i < events.length; i++) {
      if (mHide.contains(events[i])) {
        EventProxy temp = events[i];
        events[i] = events[l];
        events[l] = temp;
        l++;
      }
    }
    assert(l == mHide.size());
    //put all the forbidden events directly after the Hidden ones
    for (int i = l; i < events.length; i++) {
      if (mForbidden.contains(events[i])) {
        EventProxy temp = events[i];
        events[i] = events[l];
        events[l] = temp;
        l++;
      }
    }
    for (int i = 0; i < events.length; i++) {
      eventToIndex.put(events[i], i);
    }
    assert(l == mHide.size() + mForbidden.size());
    // this has to be done after events gets it's final ordering to avoid
    // subtle bugs
    for (int i = 0; i < events.length; i++) {
      for (int j = 0; j < aut.length; j++) {
        if (aut[j].getEvents().contains(events[i])) {
          int[] states1 = new int[aut[j].getStates().size()];
          Arrays.fill(states1, -1);
          transitions[j][i] = states1;
        } else {
          transitions[j][i] = null;
        }
      }
    }
    int[] currentState = new int[numAutomata];
    for (int i = 0; i < aut.length; i++) {
      Map<StateProxy, Integer> stateMap = new HashMap<StateProxy, Integer>(aut[i].getStates().size());
      l = 0;
      for (StateProxy s : aut[i].getStates()) {
        if (s.isInitial()) {
          currentState[i] = l;
        }
        stateMap.put(s, l);
        l++;
      }
      assert(l == aut[i].getStates().size());
      for (TransitionProxy t : aut[i].getTransitions()) {
        transitions[i][eventToIndex.get(t.getEvent())]
                   [stateMap.get(t.getSource())] = stateMap.get(t.getTarget());
      }
    }
    for (int i = 0; i < events.length; i++) {
      IntDouble[] list = new IntDouble[numAutomata];
      for (int j = 0; j < aut.length; j++) {
        list[j] = new IntDouble(j, 0);
        if (transitions[j][i] != null) {
          for (int k = 0; k < transitions[j][i].length; k++) {
            if (transitions[j][i][k] != -1) {
              list[j].mDouble++;
            }
          }
          list[j].mDouble /= (double)transitions[j][i].length;
        } else {
          list[j].mDouble = Double.POSITIVE_INFINITY;
        }
      }
      Arrays.sort(list);
      for (int j = 0; j < eventAutomaton[i].length; j++) {
        eventAutomaton[i][j] = list[j].mInt;
      }
    }
    // don't need these anymore
    aut = null;
    eventToIndex = null;
    
    // Time to start building the automaton
    numStates = 1;
    currentState = encode(currentState);
    states.put(currentState, 0);
    unvisited = new ArrayBag(100);
    unvisited.offer(currentState);
    while (!unvisited.isEmpty()) {
      currentState = unvisited.take();
      if (!explore(currentState, true)) {
        explore(currentState, false);
      }
    }
    mCompositionSize = states.values().size();
    states = null;
    currentState = new int[] {0};
    transitions = new int[1][events.length][numStates];
    for (int i = 0; i < transitions[0].length; i++) {
      for (int j = 0; j < transitions[0][i].length; j++) {
        transitions[0][i][j] = -1;
      }
    }
    for (int[] tran : newtrans) {
      transitions[0][tran[1]][tran[0]] = tran[2];
    }
    numStates = 1;
    currentState = actualState(currentState);
    newStates = new StateMap(mNodeLimit);
    newStates.put(currentState, new MemStateProxy(0));
    unvisited.offer(currentState);
    while (!unvisited.isEmpty()) {
      currentState = unvisited.take();
      if (!explore2(currentState, true)) {
        explore2(currentState, false);
      }
    }
    Collection<EventProxy> ev = new ArrayList<EventProxy>(mModel.getEvents());
    ev.removeAll(mHide);

    StringBuffer name = new StringBuffer();
    for (AutomatonProxy a : mModel.getAutomata()) {
      name.append(a.getName());
    }
    AutomatonProxy result =
      mFactory.createAutomatonProxy(name.toString(), ComponentKind.PLANT,
                                    ev, newStates.values(), trans);
    newStates = null;
    trans = null;
    final Minimizer min = new Minimizer(result, mFactory);
    result = min.run();
    return result;
  }

  public boolean explore(int[] state, boolean forbidden)
    throws OverflowException
  {
    boolean result = false;
    int numAutomata = transitions.length;
    int source = states.get(state);
    state = decode(state);
    int min, max;
    if (forbidden) {
      min = mHide.size();
      max = mHide.size() + mForbidden.size();
    } else {
      min = 0;
      max = events.length;
    }
    events:
    for (int i = min; i < max; i++) {
      if (!forbidden) {
        if (mHide.size() >= i && (mHide.size() + mForbidden.size()) < i) {
          continue;
        }
      }
      int[] suc = new int[numAutomata];
      for (int l = 0; l < numAutomata; l++) {
        int automaton = eventAutomaton[i][l];
        if (transitions[automaton][i] != null) {
          suc[automaton] = transitions[automaton][i][state[automaton]];
        } else {
          suc[automaton] = state[automaton];
        }
        if (suc[automaton] == -1) {
          if (l > 0) {
            int t = eventAutomaton[i][l];
            eventAutomaton[i][l] = eventAutomaton[i][l - 1];
            eventAutomaton[i][l - 1] = t;
          }
          continue events;
        }
      }
      result = true;
      mDisabled.remove(events[i]);
      Integer target = states.get(suc);
      if (target == null) {
        target = numStates;
        states.put(suc, target);
        numStates++;
        if (numStates > mNodeLimit * 10) {
          throw new OverflowException(mNodeLimit * 10);
        }
        unvisited.offer(suc);
      }
      newtrans.add(new int[] {source, i, target});
    }
    return result;
  }

  public boolean explore2(int[] state, boolean forbidden)
    throws OverflowException
  {
    int min, max;
    if (forbidden) {
      min = mHide.size();
      max = mHide.size() + mForbidden.size();
    } else {
      min = mHide.size() + mForbidden.size();
      max = events.length;
    }
    boolean result = false;
    //System.out.println("state:" + Arrays.toString(state));
    StateProxy source = newStates.get(state);
    for (int i = min; i < max; i++) {
      List<Integer> successor = new ArrayList<Integer>(state.length);
      for (int j = 0; j < state.length; j++) {
        int s = transitions[0][i][state[j]];
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
      StateProxy target = newStates.get(succ);
      if (target == null) {
        target = new MemStateProxy(numStates);
        newStates.put(succ, target);
        numStates++;
        if (numStates > mNodeLimit) {
          throw new OverflowException(mNodeLimit);
        }
        unvisited.offer(succ);
      }
      trans.add(mFactory.createTransitionProxy(source, events[i], target));
    }
    return result;
  }
  
  public int[] actualState(int[] state)
  {
    int numAutomata = transitions.length;
    int numHidden = mHide.size();
    SortedSet<Integer> setofstates = new TreeSet<Integer>();
    IntBag nextstate = new IntBag(100);
    for (int i = 0; i < state.length; i++) {
      if (setofstates.add(state[i])) {
        nextstate.offer(state[i]);
      }
    }
    // find out what states can be reached from state with hidden events
    while (!nextstate.isEmpty()) {
      int s = nextstate.take();
      events:
      for (int i = 0; i < numHidden; i++) {
        int newstate = transitions[0][i][s];
        if (newstate == -1) {
          continue;
        }
        if (setofstates.add(newstate)) {
          nextstate.offer(newstate);
        }
      }
    }
    int[] result = new int[setofstates.size()];
    int l = 0;
    for (Integer a : setofstates) {
      result[l] = a;
      l++;
    }
    assert(result.length == l);
    return result;
  }
  
  private static class MemStateProxy
    implements StateProxy
  {
    private final int mName;
    
    public MemStateProxy(int name)
    {
      mName = name;
    }
    
    public Collection<EventProxy> getPropositions()
    {
      return Collections.emptySet();
    }
    
    public boolean isInitial()
    {
      return mName == 0;
    }
    
    public MemStateProxy clone()
    {
      return new MemStateProxy(mName);
    }
    
    public String getName()
    {
      return Integer.toString(mName);
    }
    
    public boolean refequals(Object o)
    {
      if (o instanceof NamedProxy) {
        return refequals((NamedProxy) o);
      }
      return false;
    }
    
    public boolean refequals(NamedProxy o)
    {
      if (o instanceof MemStateProxy) {
        final MemStateProxy s = (MemStateProxy) o;
        return s.mName == mName;
      } else {
        return false;
      }
    }
    
    public int refHashCode()
    {
      return mName;
    }
    
    public Object acceptVisitor(final ProxyVisitor visitor)
      throws VisitorException
    {
      final ProductDESProxyVisitor desvisitor =
	(ProductDESProxyVisitor) visitor;
      return desvisitor.visitStateProxy(this);
    }

    public Class<StateProxy> getProxyInterface()
    {
      return StateProxy.class;
    }

    public boolean equalsByContents(final Proxy partner)
    {
      if (partner != null &&
          partner.getProxyInterface() == getProxyInterface()) {
        final StateProxy state = (StateProxy) partner;
        return (getName().equals(state.getName())) &&
               (isInitial() == state.isInitial()) &&
               state.getPropositions().isEmpty();
      } else {
        return false;
      }
    }
    
    public boolean equalsWithGeometry(Proxy o)
    {
      return equalsByContents(o);
    }
    
    public int hashCodeByContents()
    {
      return refHashCode();
    }
    
    public int hashCodeWithGeometry()
    {
      return refHashCode();
    }
    
    public int compareTo(NamedProxy n)
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
    
    public IntBag(int initialSize)
    {
      mLength = 0;
      mInitialSize = initialSize;
      mValues = new int[mInitialSize];
    }
    
    public boolean isEmpty()
    {
      return mLength == 0;
    }
    
    public void offer(int a)
    {
      if (mLength == mValues.length) {
        int[] newArray = new int[mValues.length*2];
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
      int a = mValues[mLength];
      if (mValues.length > mInitialSize && (mLength <= (mValues.length / 4))) {
        int[] newArray = new int[mValues.length / 2];
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
    
    public ArrayBag(int initialSize)
    {
      mLength = 0;
      mInitialSize = initialSize;
      mValues = new int[mInitialSize][];
    }
    
    public boolean isEmpty()
    {
      return mLength == 0;
    }
    
    public void offer(int[] a)
    {
      if (mLength == mValues.length) {
        int[][] newArray = new int[mValues.length*2][];
        // from memory this is actually faster than using the Arrays method for it
        for (int i = 0; i < mValues.length; i++) {
          newArray[i] = mValues[i];
        }
        mValues = newArray;
      }
      mValues[mLength] = a;
      mLength++;
    }
    
    public int[] take()
    {
      mLength--;
      int[] a = mValues[mLength];
      // this shouldn't actually save any memory as the Map will still reference the array but oh well
      mValues[mLength] = null;
      if (mValues.length > mInitialSize && (mLength <= (mValues.length / 4))) {
        int[][] newArray = new int[mValues.length / 2][];
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
    
    public StateMap(int num)
    {
      mMap = new HashMap<IntArray, StateProxy>(num);
    }
    
    public Set<Map.Entry<int[],StateProxy>> entrySet()
    {
      return null; // I don't think i'll be using this method so meh
    }
    
    public StateProxy get(Object o)
    {
      int[] a = (int[]) o;
      return mMap.get(new IntArray(a));
    }
    
    public StateProxy get(int[] a)
    {
      return mMap.get(new IntArray(a));
    }
    
    public StateProxy put(Object o, StateProxy s)
    {
      return mMap.put(new IntArray((int[])o), s);
    }
    
    public StateProxy put(int[] a, StateProxy s)
    {
      return mMap.put(new IntArray(a), s);
    }
    
    public Collection<StateProxy> values()
    {
      return mMap.values();
    }
  }
  
  private static class IntMap
    extends AbstractMap<int[], Integer>
  {
    final Map<IntArray, Integer> mMap;
    
    public IntMap(int num)
    {
      mMap = new HashMap<IntArray, Integer>(num);
    }
    
    public Set<Map.Entry<int[],Integer>> entrySet()
    {
      return null; // I don't think i'll be using this method so meh
    }
    
    public Integer get(Object o)
    {
      int[] a = (int[]) o;
      return mMap.get(new IntArray(a));
    }
    
    public Integer get(int[] a)
    {
      return mMap.get(new IntArray(a));
    }
    
    public Integer put(Object o, Integer s)
    {
      return mMap.put(new IntArray((int[])o), s);
    }
    
    public Integer put(int[] a, Integer s)
    {
      return mMap.put(new IntArray(a), s);
    }
    
    public Collection<Integer> values()
    {
      return mMap.values();
    }
  }
  
  private static class IntArray
  {
    public final int[] mArray;
    
    public IntArray(int[] array)
    {
      mArray = array;
    }
    
    public int hashCode()
    {
      return Arrays.hashCode(mArray);
    }
    
    public boolean equals(Object o)
    {
      IntArray oth = (IntArray)o;
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
    
    public IntDouble(int i, double d)
    {
      mInt = i;
      mDouble = d;
    }
    
    public int compareTo(IntDouble id)
    {
      if (mDouble < id.mDouble) {
        return -1;
      } else if (mDouble > id.mDouble){
        return 1;
      }
      return 0;
    }
  }
  
  private int mCompositionSize = 0;
  private int mNodeLimit;
  private ProductDESProxy mModel;
  private ProductDESProxyFactory mFactory;
  private Set<EventProxy> mHide;
  private Set<EventProxy> mForbidden;
  private Set<EventProxy> mDisabled;
  private Map<int[], Integer> states;
  private Collection<TransitionProxy> trans;
  private EventProxy[] events;
  private int[][][] transitions;
  private List<int[]> newtrans = new ArrayList<int[]>();
  private Map<int[], StateProxy> newStates;
  private int numStates;
  private Bag unvisited;
  private int[][] eventAutomaton;
}
