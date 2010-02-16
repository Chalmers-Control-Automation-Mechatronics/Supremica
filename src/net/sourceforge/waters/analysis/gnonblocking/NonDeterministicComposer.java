package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TObjectIntHashMap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.AnnotatedMemStateProxy;
import net.sourceforge.waters.analysis.modular.DisabledEvents;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * Composes the synchronous product for a given model (including non
 * deterministic models).
 *
 * @author Simon Ware & Rachel Francis
 */
public class NonDeterministicComposer
{
  public NonDeterministicComposer(final List<AutomatonProxy> model,
                                  final ProductDESProxyFactory factory,
                                  final EventProxy marked,
                                  final EventProxy preconditionMark)
  {
    mMarked = marked;
    mPreMarking = preconditionMark;
    mModel = model;
    mFactory = factory;
    mNodeLimit = 1000;
    numStates = 1;
  }

  public void setNodeLimit(final int stateLimit)
  {
    mNodeLimit = stateLimit;
  }

  private EventProxy[] unionEvents()
  {
    final THashSet<EventProxy> merge = new THashSet<EventProxy>();
    for (final AutomatonProxy a : mModel) {
      merge.addAll(a.getEvents());
    }
    final EventProxy[] events = merge.toArray(new EventProxy[merge.size()]);
    Arrays.sort(events);
    return events;
  }

  public AutomatonProxy run() throws AnalysisException
  {
    mNewMarked = new TIntArrayList();
    mNewPreMarked = new TIntArrayList();
    states = new IntMap(mNodeLimit);
    TObjectIntHashMap<EventProxy> eventToIndex =
        new TObjectIntHashMap<EventProxy>();
    events = unionEvents();
    final int numAutomata = mModel.size();
    for (final AutomatonProxy a : mModel) {
      System.out.println("Automata: " + a.getName());
      // System.out.println(a);
    }
    eventAutomaton = new int[events.length][numAutomata];
    // transitions indexed first by automaton then by event then by source state
    transitions = new int[numAutomata][events.length][][];
    for (int i = 0; i < events.length; i++) {
      eventToIndex.put(events[i], i);
    }
    final int[][] currentState = new int[numAutomata][];
    mMarkedStates = new boolean[numAutomata][];
    mPreMarkedStates = new boolean[numAutomata][];
    mIntDisabled = new TIntHashSet[numAutomata][];
    for (int i = 0; i < mModel.size(); i++) {
      final AutomatonProxy a = mModel.get(i);
      final TObjectIntHashMap<StateProxy> statetoindex =
          new TObjectIntHashMap<StateProxy>(a.getStates().size());
      mMarkedStates[i] = new boolean[a.getStates().size()];
      mPreMarkedStates[i] = new boolean[a.getStates().size()];
      final TIntArrayList cs = new TIntArrayList(1);
      mIntDisabled[i] = new TIntHashSet[a.getStates().size()];
      final boolean addMarking = !a.getEvents().contains(mMarked);
      final boolean addPreMarking = !a.getEvents().contains(mPreMarking);
      // TODO do this smarter
      int snum = 0;
      for (final StateProxy s : a.getStates()) {
        if (addMarking || s.getPropositions().contains(mMarked)) {
          mMarkedStates[i][snum] = true;
        }
        if (addPreMarking || s.getPropositions().contains(mPreMarking)) {
          mPreMarkedStates[i][snum] = true;
        }
        final Set<EventProxy> dis = getDisabledEvents(s.getPropositions());
        mIntDisabled[i][snum] = new TIntHashSet();
        for (final EventProxy e : dis) {
          if (eventToIndex.contains(e)) {
            mIntDisabled[i][snum].add(eventToIndex.get(e));
          }
          if (e.equals(mMarked)) {
            mMarkedStates[i][snum] = false;
          } else if (e.equals(mPreMarking)) {
            mPreMarkedStates[i][snum] = false;
          }
        }
        if (s.isInitial()) {
          cs.add(snum);
        }
        statetoindex.put(s, snum);
        snum++;
      }
      currentState[i] = cs.toNativeArray();
      // TODO do this smarter later
      final TIntArrayList[][] auttransitionslists =
          new TIntArrayList[events.length][a.getStates().size()];
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
        if (!a.getEvents().contains(events[j])) {
          continue;
        }
        transitions[i][j] = new int[a.getStates().size()][];
        for (int k = 0; k < auttransitionslists[j].length; k++) {
          final TIntArrayList list = auttransitionslists[j][k];
          if (list != null) {
            final int[] targs = list.toNativeArray();
            transitions[i][j][k] = targs;
          }
        }
      }
    }
    // no longer used
    eventToIndex = null;
    // for (int i = 0; i < mMarkedStates.length; i++) {
    // System.out.println("Marked:" + i + " " +
    // Arrays.toString(mMarkedStates[i]));
    // }
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
          list[j].mDouble /= (double) transitions[j][i].length;
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
    permutations(currentState, new int[numAutomata], 0, -1, -1, true);
    while (!unvisited.isEmpty()) {
      final int[] cs = unvisited.take();
      // System.out.println(Arrays.toString(currentState));
      // explore(currentState, true);
      explore(cs);
    }
    System.out.println("Composition:" + numStates);
    System.out.println("Transitions:" + newtrans.size());
    mCompositionSize = numStates;
    final StateProxy[] states = new StateProxy[numStates];
    for (int i = 0; i < states.length; i++) {
      final EventProxy marked = mNewMarked.contains(i) ? mMarked : null;
      states[i] =
          new AnnotatedMemStateProxy(i, marked, mNewInitial.contains(i));
    }
    final ArrayList<TransitionProxy> trans = new ArrayList<TransitionProxy>();
    for (final int[] tran : newtrans) {
      final StateProxy source = states[tran[0]];
      final StateProxy target = states[tran[2]];
      final EventProxy event = events[tran[1]];
      trans.add(mFactory.createTransitionProxy(source, event, target));
    }
    final StringBuffer name = new StringBuffer();
    for (final AutomatonProxy a : mModel) {
      name.append(a.getName());
    }
    final String nam = name.toString();
    final ComponentKind ck = ComponentKind.PLANT;
    final THashSet<EventProxy> ev =
        new THashSet<EventProxy>(Arrays.asList(events));
    final Collection<StateProxy> st = Arrays.asList(states);
    final AutomatonProxy result =
        mFactory.createAutomatonProxy(nam, ck, ev, st, trans);
    /*
     * System.out.println("Result"); System.out.println(result); if
     * (result.getStates().size() > 4) { System.exit(1); }
     */
    return result;
  }

  private Set<EventProxy> getDisabledEvents(final Collection<EventProxy> props)
  {
    for (final EventProxy e : props) {
      if (e instanceof DisabledEvents) {
        return ((DisabledEvents) e).getDisabled();
      }
    }
    return new THashSet<EventProxy>();
  }

  private void addState(final int[] successor, final int source,
                        final int event, final boolean isInitial)
      throws AnalysisException
  {
    Integer target = states.get(successor);
    // System.out.println("suc:" + Arrays.toString(suc) + "targ:" + target);
    if (target == null) {
      target = numStates;
      states.put(successor, target);
      numStates++;
      if (numStates > mNodeLimit) {
        throw new AnalysisException("State Limit Exceeded");
      }
      unvisited.offer(successor);
      if (determineMarked(successor)) {
        mNewMarked.add(target);
      }
      if (isInitial) {
        mNewInitial.add(target);
      }
    }
    // only add a transition if not adding in an initial state
    if (!isInitial) {
      newtrans.add(new int[] {source, event, target});
    }
  }

  private void permutations(final int[][] suc, final int[] perm,
                            final int depth, final int source, final int event,
                            final boolean isInitial) throws AnalysisException
  {
    if (depth == perm.length) {
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

  public boolean explore(final int[] state) throws AnalysisException
  {
    final boolean result = false;
    final int numAutomata = transitions.length;
    final int source = states.get(state);
    events: for (int i = 0; i < events.length; i++) {
      final int[][] suc = new int[numAutomata][];
      for (int l = 0; l < numAutomata; l++) {
        final int automaton = eventAutomaton[i][l];
        if (transitions[automaton][i] != null) {
          suc[automaton] = transitions[automaton][i][state[automaton]];
        } else {
          final int s = state[automaton];
          if (!mIntDisabled[automaton][s].contains(s)) {
            suc[automaton] = new int[] {state[automaton]};
          }
        }
        if (suc[automaton] == null) {
          /*
           * if (l > 0) { int t = eventAutomaton[i][l]; eventAutomaton[i][l] =
           * eventAutomaton[i][l - 1]; eventAutomaton[i][l - 1] = t; }
           */
          continue events;
        }
      }
      final int[] perms = new int[numAutomata];
      permutations(suc, perms, 0, source, i, false);
    }
    return result;
  }

  private boolean determineMarked(final int[] suc)
  {
    // System.out.println("state:" + Arrays.toString(suc));
    for (int i = 0; i < suc.length; i++) {
      if (!mMarkedStates[i][suc[i]]) {
        return false;
      }
    }
    return true;
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

  /*
   * private static class Pointer implements Comparable<Pointer> { EventProxy[]
   * mArray; int mIndex;
   *
   * public Pointer(EventProxy[] array) { mArray = array; mIndex = 0; }
   *
   * public boolean increment() { mIndex++; return mIndex < mArray.length; }
   *
   * public EventProxy getCurrent() { return mArray[mIndex]; }
   *
   * public int compareTo(Pointer p) { return
   * mArray[mIndex].compareTo(p.mArray[p.mIndex]); } }
   */

  @SuppressWarnings("unused")
  private int mCompositionSize = 0;
  private int mNodeLimit;
  private final List<AutomatonProxy> mModel;
  private final ProductDESProxyFactory mFactory;
  private Map<int[],Integer> states;
  @SuppressWarnings("unused")
  private Collection<TransitionProxy> trans;
  private EventProxy[] events;
  private int[][][][] transitions;
  @SuppressWarnings("unused")
  private TIntArrayList[][] mBackTransitions;
  private boolean[][] mMarkedStates;
  private boolean[][] mPreMarkedStates;
  private TIntArrayList mNewMarked;
  @SuppressWarnings("unused")
  private TIntArrayList mNewPreMarked;
  private final List<int[]> newtrans = new ArrayList<int[]>();
  private int numStates;
  private Bag unvisited;
  private int[][] eventAutomaton;
  private final EventProxy mMarked;
  private final EventProxy mPreMarking;
  @SuppressWarnings("unused")
  private int mNewDumpState;
  private final int mDumpState = -1;
  private TIntHashSet[][] mIntDisabled;
  private final TIntHashSet mNewInitial = new TIntHashSet();
}
