package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
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


/**
 * Composes the synchronous product for a given model.
 *
 * @author Simon Ware & Rachel Francis
 */
public class Composer
{
  public Composer(final ProductDESProxy model,
                  final ProductDESProxyFactory factory,
                  final EventProxy marked, final EventProxy preconditionMark)
  {
    mMarked = marked;
    mPreMarking = preconditionMark;
    mModel = model;
    mFactory = factory;
    mNodeLimit = 1000;
    numStates = 1;
  }

  // #########################################################################
  // # Invocation
  public AutomatonProxy run() throws AnalysisException
  {
    int[] currentState = prepareForComposition();

    // Time to start building the automaton
    numStates = 1;
    currentState = encode(currentState);
    mStates.put(currentState, 0);
    if (determineMarked(currentState)) {
      mNewMarked.add(0);
    }
    if (determinePreMarked(currentState)) {
      mNewPreMarked.add(0);
    }
    mUnvisited = new ArrayBag(100);
    mUnvisited.offer(currentState);
    while (!mUnvisited.isEmpty()) {
      currentState = mUnvisited.take();
      // System.out.println(Arrays.toString(currentState));
      // explore(currentState, true);
      // explore(currentState);
      explore(currentState);
    }
    // System.out.println("Composition:" + mStates.values().size());
    mCompositionSize = mStates.values().size();
    mStates = null;
    MemStateProxy[] ns = new MemStateProxy[numStates];
    for (int i = 0; i < ns.length; i++) {
      ns[i] =
          mNewMarked.contains(i) ? new MemStateProxy(i, mMarked)
              : new MemStateProxy(i);
    }
    for (final int[] tran : newtrans) {
      final StateProxy source = ns[tran[0]];
      final EventProxy eveo = mEvents[tran[1]];
      final StateProxy targ = ns[tran[2]];
      mTrans.add(mFactory.createTransitionProxy(source, eveo, targ));
    }
    final StringBuffer name = new StringBuffer();
    for (final AutomatonProxy a : mModel.getAutomata()) {
      name.append(a.getName());
    }
    final Collection<StateProxy> states = new ArrayList<StateProxy>(ns.length);
    for (int i = 0; i < ns.length; i++) {
      states.add(ns[i]);
    }
    final String nam = name.toString();
    final ComponentKind ck = ComponentKind.PLANT;
    final AutomatonProxy result =
        mFactory.createAutomatonProxy(nam, ck, mModel.getEvents(), states,
                                      mTrans);
    mTrans = null;
    ns = null;
    // System.out.println("Project:" + result.getStates().size() +
    // " Composition:" + mCompositionSize);
    // System.out.println("orig:\n" + result);

    /*
     * try { Minimizer min = new Minimizer(result, mFactory); result =
     * min.run(); } catch (Throwable t) { t.printStackTrace(); for
     * (AutomatonProxy auto : mModel.getAutomata()) {
     * System.out.println(auto.getName()); } /*System.exit(1);
     */
    // /
    // System.out.println("new:\n" + result);
    // System.out.println("dumpstate:" + mDumpState);
    return result;
  }

  /**
   * Prepares data structures for the composition to begin. Assigns the initial
   * state. Records the default marked and precondition marked states.
   * Initialises all transitions, with event, source state and the target state
   * they go to.
   *
   * @return represents the index numbers of the initial state of each
   *         automaton.
   */
  private int[] prepareForComposition()
  {
    mNewMarked = new TIntHashSet();
    mStates = new IntMap(mNodeLimit);
    mTrans = new ArrayList<TransitionProxy>();
    mEvents =
        mModel.getEvents().toArray(new EventProxy[mModel.getEvents().size()]);
    final int numAutomata = mModel.getAutomata().size();
    AutomatonProxy[] aut =
        mModel.getAutomata().toArray(new AutomatonProxy[numAutomata]);
    eventAutomaton = new int[mEvents.length][numAutomata];
    // transitions indexed first by automaton then by event then by source state
    mTransitions = new int[numAutomata][mEvents.length][];
    // go through and put all the events to be hidden to the front
    Map<EventProxy,Integer> eventToIndex =
        new HashMap<EventProxy,Integer>(mEvents.length);
    // put all the forbidden events directly after the Hidden ones
    for (int i = 0; i < mEvents.length; i++) {
      eventToIndex.put(mEvents[i], i);
    }
    // this has to be done after events gets it's final ordering to avoid
    // subtle bugs
    for (int i = 0; i < mEvents.length; i++) {
      for (int j = 0; j < aut.length; j++) {
        if (aut[j].getEvents().contains(mEvents[i])) {
          final int[] states1 = new int[aut[j].getStates().size()];
          Arrays.fill(states1, -1);
          mTransitions[j][i] = states1;
        } else {
          mTransitions[j][i] = null;
        }
      }
    }
    final int[] currentState = new int[numAutomata];
    mMarkedStates = new boolean[numAutomata][];
    mPreMarkedStates = new boolean[numAutomata][];
    for (int i = 0; i < aut.length; i++) {
      final Map<StateProxy,Integer> stateMap =
          new HashMap<StateProxy,Integer>(aut[i].getStates().size());
      mMarkedStates[i] = new boolean[aut[i].getStates().size()];
      mPreMarkedStates[i] = new boolean[aut[i].getStates().size()];
      int index = 0;
      final Set<StateProxy> states = aut[i].getStates();
      final boolean containsmarked = aut[i].getEvents().contains(mMarked);
      final boolean containsPremarking =
          aut[i].getEvents().contains(mPreMarking);
      for (final StateProxy s : states) {
        if (s.isInitial()) {
          currentState[i] = index;
        }
        // System.out.println(s.getPropositions() + " " + mMarked);
        mMarkedStates[i][index] =
            containsmarked ? s.getPropositions().contains(mMarked) : true;
        mPreMarkedStates[i][index] =
            containsPremarking ? s.getPropositions().contains(mPreMarking)
                : true;
        stateMap.put(s, index);
        index++;
      }
      assert (index == aut[i].getStates().size());
      final Collection<TransitionProxy> trns = aut[i].getTransitions();
      for (final TransitionProxy t : trns) {
        // System.out.println(transitions[i][eventToIndex.get(t.getEvent()));
        final int eventIndex = eventToIndex.get(t.getEvent());
        final int srcStateIndex = stateMap.get(t.getSource());
        final int targetStateIndex = stateMap.get(t.getTarget());
        mTransitions[i][eventIndex][srcStateIndex] = targetStateIndex;
      }
    }
    for (int i = 0; i < mEvents.length; i++) {
      final IntDouble[] list = new IntDouble[numAutomata];
      for (int j = 0; j < aut.length; j++) {
        list[j] = new IntDouble(j, 0);
        if (mTransitions[j][i] != null) {
          for (int k = 0; k < mTransitions[j][i].length; k++) {
            if (mTransitions[j][i][k] != -1) {
              list[j].mDouble++;
            }
          }
          list[j].mDouble /= (double) mTransitions[j][i].length;
          // TODO: unsure what the point is of the double
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

    return currentState;
  }

  public void setNodeLimit(final int stateLimit)
  {
    mNodeLimit = stateLimit;
  }

  @SuppressWarnings("unused")
  private void markStates()
  {
    final boolean[] reachable = new boolean[mBackTransitions[0].length];
    final IntBag curstates = new IntBag(100);
    // System.out.println("marked:" + Arrays.toString(mNewMarked.toArray()));
    final int[] markedstates = mNewMarked.toArray();
    for (int i = 0; i < markedstates.length; i++) {
      reachable[markedstates[i]] = true;
      curstates.offer(markedstates[i]);
    }
    while (!curstates.isEmpty()) {
      final int cs = curstates.take();
      for (int i = 0; i < mBackTransitions.length; i++) {
        if (mBackTransitions[i][cs] == null) {
          continue;
        }
        final int[] sucs = mBackTransitions[i][cs].toNativeArray();
        for (int j = 0; j < sucs.length; j++) {
          final int suc = sucs[j];
          if (!reachable[suc]) {
            reachable[suc] = true;
            curstates.offer(suc);
          }
        }
      }
    }
    for (int k = 0; k < reachable.length; k++) {
      if (!reachable[k]) {
        System.out.println("not reachable: " + k);
        if (mDumpState == -1) {
          mDumpState = k;
        } else {
          for (int i = 0; i < mBackTransitions.length; i++) {
            if (mBackTransitions[i][k] == null) {
              continue;
            }
            final int[] sucs = mBackTransitions[i][k].toNativeArray();
            for (int j = 0; j < sucs.length; j++) {
              final int suc = sucs[j];
              // that which use to point to this state now points to the dump
              // state
              mTransitions[0][i][suc] = mDumpState;
            }
          }
        }
        for (int i = 0; i < mTransitions[0].length; i++) {
          mTransitions[0][i][k] = -1;
        }
      }
    }
  }

  public boolean explore(int[] state) throws AnalysisException
  {
    boolean result = false;
    final int numAutomata = state.length;
    final int source = mStates.get(state);
    state = decode(state);
    int min, max;
    min = 0;
    max = mEvents.length;
    events: for (int i = min; i < max; i++) {
      final int[] suc = new int[numAutomata];
      for (int l = 0; l < numAutomata; l++) {
        final int automaton = eventAutomaton[i][l];
        if (mTransitions[automaton][i] != null) {
          suc[automaton] = mTransitions[automaton][i][state[automaton]];
        } else {
          suc[automaton] = state[automaton];
        }
        if (suc[automaton] == -1) {
          if (l > 0) {
            final int t = eventAutomaton[i][l];
            eventAutomaton[i][l] = eventAutomaton[i][l - 1];
            eventAutomaton[i][l - 1] = t;
          }
          continue events;
        }
      }
      result = true;
      Integer target = mStates.get(suc);
      // System.out.println("suc:" + Arrays.toString(suc) + "targ:" + target);
      if (target == null) {
        target = numStates;
        mStates.put(suc, target);
        numStates++;
        if (numStates > mNodeLimit * 10) {
          throw new AnalysisException("State Limit Exceeded");
        }
        mUnvisited.offer(suc);
        if (determineMarked(suc)) {
          mNewMarked.add(target);
        }
        if (determinePreMarked(suc)) {
          mNewPreMarked.add(target);
        }
      }
      newtrans.add(new int[] {source, i, target});
    }
    return result;
  }

  /**
   * Determines whether a state of the synchronous product should have the
   * default marking.
   *
   * @param state
   * @return true = marked
   */
  private boolean determineMarked(final int[] state)
  {
    for (int i = 0; i < state.length; i++) {
      if (!mMarkedStates[i][state[i]]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether a state of the synchronous product should have the
   * precondition marking.
   *
   * @param state
   * @return true = marked
   */
  private boolean determinePreMarked(final int[] state)
  {
    for (int i = 0; i < state.length; i++) {
      if (!mPreMarkedStates[i][state[i]]) {
        return false;
      }
    }
    return true;
  }


  private static class MemStateProxy implements StateProxy
  {
    private final int mName;
    private final EventProxy mEvent;

    public MemStateProxy(final int name, final EventProxy event)
    {
      mName = name;
      mEvent = event;
    }

    public MemStateProxy(final int name)
    {
      this(name, null);
    }

    public Collection<EventProxy> getPropositions()
    {
      if (mEvent == null) {
        return Collections.emptySet();
      } else {
        return Collections.singleton(mEvent);
      }
    }

    public boolean isInitial()
    {
      return mName == 0;
    }

    public MemStateProxy clone()
    {
      return new MemStateProxy(mName, mEvent);
    }

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

    public boolean refequals(final NamedProxy o)
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

    public int compareTo(final NamedProxy n)
    {
      return n.getName().compareTo(getName());
    }

    public String toString()
    {
      return "S:" + mName;
    }
  }


  private static interface Bag
  {
    /** is the bag empty */
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
        final int[] newArray = new int[mValues.length * 2];
        // from memory this is actually faster than using the Arrays method for
        // it
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


  private static class ArrayBag implements Bag
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

    public boolean isEmpty()
    {
      return mLength == 0;
    }

    public void offer(final int[] a)
    {
      if (mLength == mValues.length) {
        final int[][] newArray = new int[mValues.length * 2][];
        // from memory this is actually faster than using the Arrays method for
        // it
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
      final int[] a = mValues[mLength];
      // this shouldn't actually save any memory as the Map will still reference
      // the array but oh well
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
   * I'll make this encode it properly later on
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


  @SuppressWarnings("unused")
  private static class StateMap extends AbstractMap<int[],MemStateProxy>
  {
    final Map<IntArray,MemStateProxy> mMap;

    public StateMap(final int num)
    {
      mMap = new HashMap<IntArray,MemStateProxy>(num);
    }

    public Set<Map.Entry<int[],MemStateProxy>> entrySet()
    {
      return null; // I don't think i'll be using this method so meh
    }

    public MemStateProxy get(final Object o)
    {
      final int[] a = (int[]) o;
      return mMap.get(new IntArray(a));
    }

    public MemStateProxy get(final int[] a)
    {
      return mMap.get(new IntArray(a));
    }

    public MemStateProxy put(final Object o, final MemStateProxy s)
    {
      return mMap.put(new IntArray((int[]) o), s);
    }

    public MemStateProxy put(final int[] a, final MemStateProxy s)
    {
      return mMap.put(new IntArray(a), s);
    }

    public Collection<MemStateProxy> values()
    {
      return mMap.values();
    }

    public String toString()
    {
      return mMap.toString();
    }
  }


  private static class IntMap extends AbstractMap<int[],Integer>
  {
    final Map<IntArray,Integer> mMap;

    public IntMap(final int num)
    {
      mMap = new HashMap<IntArray,Integer>(num);
    }

    public Set<Map.Entry<int[],Integer>> entrySet()
    {
      return null; // I don't think i'll be using this method so meh
    }

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
      return mMap.put(new IntArray((int[]) o), s);
    }

    public Integer put(final int[] a, final Integer s)
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

    public IntArray(final int[] array)
    {
      mArray = array;
    }

    public int hashCode()
    {
      return Arrays.hashCode(mArray);
    }

    public boolean equals(final Object o)
    {
      final IntArray oth = (IntArray) o;
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


  private static class IntDouble implements Comparable<IntDouble>
  {
    final public int mInt;
    public double mDouble;

    public IntDouble(final int i, final double d)
    {
      mInt = i;
      mDouble = d;
    }

    public int compareTo(final IntDouble id)
    {
      if (mDouble < id.mDouble) {
        return -1;
      } else if (mDouble > id.mDouble) {
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
  private int mCompositionSize = 0;
  private int mNodeLimit;
  private final ProductDESProxy mModel;
  private final ProductDESProxyFactory mFactory;
  private Map<int[],Integer> mStates;
  private Collection<TransitionProxy> mTrans;
  private EventProxy[] mEvents;
  private int[][][] mTransitions;
  private TIntArrayList[][] mBackTransitions;
  private boolean[][] mMarkedStates;
  private boolean[][] mPreMarkedStates;
  private TIntHashSet mNewMarked;
  private TIntHashSet mNewPreMarked;
  private final List<int[]> newtrans = new ArrayList<int[]>();
  private int numStates;
  private Bag mUnvisited;
  private int[][] eventAutomaton;
  private final EventProxy mMarked;
  private final EventProxy mPreMarking;
  @SuppressWarnings("unused")
  private int mNewDumpState;
  private int mDumpState = -1;
}
