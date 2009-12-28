package net.sourceforge.waters.analysis.modular;

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


public class Composer
{
  public Composer(final ProductDESProxy model, final ProductDESProxyFactory factory,
                  final EventProxy marked)
  {
    mMarked = marked;
    mModel = model;
    mFactory = factory;
    mNodeLimit = 1000;
    numStates = 1;
    //System.out.println("hide:" + hide);
    //System.out.println("forbidden;" + forbidden);
  }

  public void setNodeLimit(final int stateLimit)
  {
    mNodeLimit = stateLimit;
  }

  public AutomatonProxy run()
    throws AnalysisException
  {
    mNewMarked = new TIntHashSet();
    states = new IntMap(mNodeLimit);
    trans = new ArrayList<TransitionProxy>();
    events = mModel.getEvents().toArray(new EventProxy[mModel.getEvents().size()]);
    final int numAutomata = mModel.getAutomata().size();
    AutomatonProxy[] aut = mModel.getAutomata().toArray(new AutomatonProxy[numAutomata]);
    for (int i = 0; i < aut.length; i++) {
      System.out.println("Automata" + i + " " + aut[i].getName());
    }
    eventAutomaton = new int[events.length][numAutomata];
    // transitions indexed first by automaton then by event then by source state
    transitions = new int[numAutomata][events.length][];
    // go through and put all the events to be hidden to the front
    Map<EventProxy, Integer> eventToIndex = new HashMap<EventProxy, Integer>(events.length);
    //put all the forbidden events directly after the Hidden ones
    for (int i = 0; i < events.length; i++) {
      eventToIndex.put(events[i], i);
    }
    // this has to be done after events gets it's final ordering to avoid
    // subtle bugs
    for (int i = 0; i < events.length; i++) {
      for (int j = 0; j < aut.length; j++) {
        if (aut[j].getEvents().contains(events[i])) {
          final int[] states1 = new int[aut[j].getStates().size()];
          Arrays.fill(states1, -1);
          transitions[j][i] = states1;
        } else {
          transitions[j][i] = null;
        }
      }
    }
    int[] currentState = new int[numAutomata];
    mMarkedStates = new boolean[numAutomata][];
    for (int i = 0; i < aut.length; i++) {
      final Map<StateProxy, Integer> stateMap = new HashMap<StateProxy, Integer>(aut[i].getStates().size());
      mMarkedStates[i] = new boolean[aut[i].getStates().size()];
      int l = 0;
      final Set<StateProxy> states = aut[i].getStates();
      final boolean containsmarked = aut[i].getEvents().contains(mMarked);
      for (final StateProxy s : states) {
        if (s.isInitial()) {
          currentState[i] = l;
        }
        //System.out.println(s.getPropositions() + " " + mMarked);
        mMarkedStates[i][l] = containsmarked ? s.getPropositions().contains(mMarked)
                                             : true;
        stateMap.put(s, l);
        l++;
      }
      assert(l == aut[i].getStates().size());
      final Collection<TransitionProxy> trns = aut[i].getTransitions();
      for (final TransitionProxy t : trns) {
        //System.out.println(transitions[i][eventToIndex.get(t.getEvent()));
        transitions[i][eventToIndex.get(t.getEvent())]
                   [stateMap.get(t.getSource())] = stateMap.get(t.getTarget());
      }
    }
    //for (int i = 0; i < mMarkedStates.length; i++) {
      //System.out.println("Marked:" + i + " " + Arrays.toString(mMarkedStates[i]));
    //}
    for (int i = 0; i < events.length; i++) {
      final IntDouble[] list = new IntDouble[numAutomata];
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
    if (determineMarked(currentState)) {
      mNewMarked.add(0);
    }
    unvisited = new ArrayBag(100);
    unvisited.offer(currentState);
    while (!unvisited.isEmpty()) {
      currentState = unvisited.take();
      //System.out.println(Arrays.toString(currentState));
      //explore(currentState, true);
      explore(currentState);
      explore(currentState);
    }
    System.out.println("Composition:" + states.values().size());
    mCompositionSize = states.values().size();
    states = null;
    MemStateProxy[] ns = new MemStateProxy[numStates];
    for (int i = 0; i < ns.length; i++) {
      ns[i] = mNewMarked.contains(i) ? new MemStateProxy(i, mMarked)
                                     : new MemStateProxy(i);
    }
    for (final int[] tran : newtrans) {
      final StateProxy source = ns[tran[0]];
      final EventProxy eveo = events[tran[1]];
      final StateProxy targ = ns[tran[2]];
      trans.add(mFactory.createTransitionProxy(source, eveo, targ));
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
    final AutomatonProxy result = mFactory.createAutomatonProxy(nam, ck,
                                                          mModel.getEvents(), states,
                                                          trans);
    trans = null;
    ns = null;
    System.out.println("Project:" + result.getStates().size() + " Composition:" + mCompositionSize);
      //System.out.println("orig:\n" + result);

    /*try {
      Minimizer min = new Minimizer(result, mFactory);
      result = min.run();
    } catch (Throwable t) {
      t.printStackTrace();
      for (AutomatonProxy auto : mModel.getAutomata()) {
        System.out.println(auto.getName());
      }
      /*System.exit(1);*/
    ///
    //System.out.println("new:\n" + result);
    System.out.println("dumpstate:" + mDumpState);
    return result;
  }

  @SuppressWarnings("unused")
  private void markStates()
  {
    final boolean[] reachable = new boolean[mBackTransitions[0].length];
    final IntBag curstates = new IntBag(100);
    //System.out.println("marked:" + Arrays.toString(mNewMarked.toArray()));
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
              // that which use to point to this state now points to the dump state
              transitions[0][i][suc] = mDumpState;
            }
          }
        }
        for (int i = 0; i < transitions[0].length; i++) {
          transitions[0][i][k] = -1;
        }
      }
    }
  }

  public boolean explore(int[] state)
    throws AnalysisException
  {
    boolean result = false;
    final int numAutomata = transitions.length;
    final int source = states.get(state);
    state = decode(state);
    int min, max;
    min = 0;
    max = events.length;
    events:
    for (int i = min; i < max; i++) {
      final int[] suc = new int[numAutomata];
      for (int l = 0; l < numAutomata; l++) {
        final int automaton = eventAutomaton[i][l];
        if (transitions[automaton][i] != null) {
          suc[automaton] = transitions[automaton][i][state[automaton]];
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
      Integer target = states.get(suc);
      //System.out.println("suc:" + Arrays.toString(suc) + "targ:" + target);
      if (target == null) {
        target = numStates;
        states.put(suc, target);
        numStates++;
        if (numStates > mNodeLimit * 10) {
          throw new AnalysisException("State Limit Exceeded");
        }
        unvisited.offer(suc);
        if (determineMarked(suc)) {
          mNewMarked.add(target);
        }
      }
      newtrans.add(new int[] {source, i, target});
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
      final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
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

    public boolean isEmpty()
    {
      return mLength == 0;
    }

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

  @SuppressWarnings("unused")
  private static class StateMap
    extends AbstractMap<int[], MemStateProxy>
  {
    final Map<IntArray, MemStateProxy> mMap;

	public StateMap(final int num)
    {
      mMap = new HashMap<IntArray, MemStateProxy>(num);
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
      return mMap.put(new IntArray((int[])o), s);
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

  private static class IntMap
    extends AbstractMap<int[], Integer>
  {
    final Map<IntArray, Integer> mMap;

    public IntMap(final int num)
    {
      mMap = new HashMap<IntArray, Integer>(num);
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
      return mMap.put(new IntArray((int[])o), s);
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

  private int mCompositionSize = 0;
  private int mNodeLimit;
  private final ProductDESProxy mModel;
  private final ProductDESProxyFactory mFactory;
  private Map<int[], Integer> states;
  private Collection<TransitionProxy> trans;
  private EventProxy[] events;
  private int[][][] transitions;
  private TIntArrayList[][] mBackTransitions;
  private boolean[][] mMarkedStates;
  private TIntHashSet mNewMarked;
  private final List<int[]> newtrans = new ArrayList<int[]>();
  private int numStates;
  private Bag unvisited;
  private int[][] eventAutomaton;
  private final EventProxy mMarked;
  @SuppressWarnings("unused")
  private int mNewDumpState;
  private int mDumpState = -1;
}
