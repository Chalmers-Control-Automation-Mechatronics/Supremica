package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.xsd.base.ComponentKind;

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
import net.sourceforge.waters.model.analysis.AnalysisException;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntArrayList;
import net.sourceforge.waters.analysis.LightWeightGraph;
import java.util.PriorityQueue;
import gnu.trove.TObjectIntHashMap;
import java.util.Stack;
import gnu.trove.THashSet;
import net.sourceforge.waters.analysis.AnnotatedMemStateProxy;


public class NonDeterministicComposer
{
  public NonDeterministicComposer(List<AutomatonProxy> model, ProductDESProxyFactory factory,
                                  EventProxy marked)
  {
    mMarked = marked;
    mModel = model;
    mFactory = factory;
    mNodeLimit = 1000;
    numStates = 1;
    //System.out.println("hide:" + hide);
    //System.out.println("forbidden;" + forbidden);
  }

  public void setNodeLimit(int stateLimit)
  {
    mNodeLimit = stateLimit;
  }
  
  private EventProxy[] unionEvents()
  {
    THashSet<EventProxy> merge = new THashSet<EventProxy>();
    for (AutomatonProxy a : mModel) {merge.addAll(a.getEvents());}
    EventProxy[] events = merge.toArray(new EventProxy[merge.size()]);
    Arrays.sort(events);
    return events;
  }
  
  public AutomatonProxy run()
    throws AnalysisException
  {
    mNewMarked = new TIntArrayList();
    states = new IntMap(mNodeLimit);
    TObjectIntHashMap<EventProxy> eventToIndex = new TObjectIntHashMap<EventProxy>();
    events = unionEvents();
    int numAutomata = mModel.size();
    for (AutomatonProxy a : mModel) {
      System.out.println("Automata: " + a.getName());
      //System.out.println(a);
    }
    eventAutomaton = new int[events.length][numAutomata];
    int stateLength = numAutomata;
    // transitions indexed first by automaton then by event then by source state
    transitions = new int[numAutomata][events.length][][];
    for (int i = 0; i < events.length; i++) {
      eventToIndex.put(events[i], i);
    }
    int[][] currentState = new int[numAutomata][];
    mMarkedStates = new boolean[numAutomata][];
    mIntDisabled = new TIntHashSet[numAutomata][];
    for (int i = 0; i < mModel.size(); i++) {
      AutomatonProxy a = mModel.get(i);
      TObjectIntHashMap<StateProxy> statetoindex =
        new TObjectIntHashMap<StateProxy>(a.getStates().size());
      mMarkedStates[i] = new boolean[a.getStates().size()];
      TIntArrayList cs = new TIntArrayList(1);
      mIntDisabled[i] = new TIntHashSet[a.getStates().size()];
      boolean add = !a.getEvents().contains(mMarked);
      // TODO do this smarter
      int snum = 0;
      for (StateProxy s : a.getStates()) {
        if (add || s.getPropositions().contains(mMarked)) {
          mMarkedStates[i][snum] = true;
        }
        Set<EventProxy> dis = getDisabledEvents(s.getPropositions());
        mIntDisabled[i][snum] = new TIntHashSet();
        for (EventProxy e : dis) {
          if (eventToIndex.contains(e)) {
            mIntDisabled[i][snum].add(eventToIndex.get(e));
          }
          if (e.equals(mMarked)) {
            mMarkedStates[i][snum] = false;
          }
        }
        if (s.isInitial()) {
          cs.add(snum);
        }
        statetoindex.put(s, snum); snum++;
      }
      currentState[i] = cs.toNativeArray();
      // TODO do this smarter later
      TIntArrayList[][] auttransitionslists =
        new TIntArrayList[events.length][a.getStates().size()];
      for (TransitionProxy t : a.getTransitions()) {
        int event = eventToIndex.get(t.getEvent());
        int source = statetoindex.get(t.getSource());
        int target = statetoindex.get(t.getTarget());
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
          TIntArrayList list = auttransitionslists[j][k];
          if (list != null) {
            int[] targs = list.toNativeArray(); transitions[i][j][k] = targs;
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
      IntDouble[] list = new IntDouble[numAutomata];
      for (int j = 0; j < mModel.size(); j++) {
        list[j] = new IntDouble(j, 0);
        if (transitions[j][i] != null) {
          for (int k = 0; k < transitions[j][i].length; k++) {
            if (transitions[j][i][k] != null) {
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
    
    // Time to start building the automaton
    numStates = 0;
    unvisited = new ArrayBag(100);
    permutations(currentState, new int[numAutomata], 0,
                 -1, -1, true);
    while (!unvisited.isEmpty()) {
      int[] cs = unvisited.take();
      //System.out.println(Arrays.toString(currentState));
      //explore(currentState, true);
      explore(cs);
    }
    System.out.println("Composition:" + numStates);
    System.out.println("Transitions:" + newtrans.size());
    mCompositionSize = numStates;
    StateProxy[] states = new StateProxy[numStates];
    for (int i = 0; i < states.length; i++) {
      EventProxy marked = mNewMarked.contains(i) ? mMarked : null;
      states[i] = new AnnotatedMemStateProxy(i, marked, mNewInitial.contains(i));
    }
    ArrayList<TransitionProxy> trans = new ArrayList<TransitionProxy>();
    for (int[] tran : newtrans) {
      StateProxy source = states[tran[0]];
      StateProxy target = states[tran[2]];
      EventProxy event = events[tran[1]];
      trans.add(mFactory.createTransitionProxy(source, event, target));
    }
    StringBuffer name = new StringBuffer();
    for (AutomatonProxy a : mModel) {
      name.append(a.getName());
    }
    String nam = name.toString();
    ComponentKind ck = ComponentKind.PLANT;
    THashSet<EventProxy> ev = new THashSet<EventProxy>(Arrays.asList(events));
    Collection<StateProxy> st = Arrays.asList(states);
    AutomatonProxy result = mFactory.createAutomatonProxy(nam, ck, ev, st,
                                                          trans);
    /*System.out.println("Result");
    System.out.println(result);
    if (result.getStates().size() > 4) {
      System.exit(1);
    }*/
    return result;
  }
  
  private Set<EventProxy> getDisabledEvents(Collection<EventProxy> props)
  {
    for (EventProxy e : props)
    {
      if (e instanceof DisabledEvents) {
        return ((DisabledEvents) e).getDisabled();
      }
    }
    return new THashSet<EventProxy>();
  }
  
  private void addState(int[] successor, int source,
                        int event, boolean isInitial)
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
  
  private void permutations(int[][] suc, int[] perm, int depth,
                            int source, int event, boolean isInitial)
    throws AnalysisException
  {
    if (depth== perm.length) {
      int[] successor = new int[depth];
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
  
  public boolean explore(int[] state)
    throws AnalysisException
  {
    boolean result = false;
    int numAutomata = transitions.length;
    int source = states.get(state);
    events:
    for (int i = 0; i < events.length; i++) {
      int[][] suc = new int[numAutomata][];
      for (int l = 0; l < numAutomata; l++) {
        int automaton = eventAutomaton[i][l];
        if (transitions[automaton][i] != null) {
          suc[automaton] = transitions[automaton][i][state[automaton]];
        } else {
          int s = state[automaton];
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
      int[] perms = new int[numAutomata];
      permutations(suc,perms,0,source,i, false);
    }
    return result;
  }
  
  private boolean determineMarked(int[] suc)
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
  
  public int getDumpState()
  {
    return mDumpState;
  }
  
  private static class Pointer
    implements Comparable<Pointer>
  {
    EventProxy[] mArray;
    int mIndex;
    
    public Pointer(EventProxy[] array)
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
    
    public int compareTo(Pointer p)
    {
      return mArray[mIndex].compareTo(p.mArray[p.mIndex]);
    }
  }
  
  private int mCompositionSize = 0;
  private int mNodeLimit;
  private List<AutomatonProxy> mModel;
  private ProductDESProxyFactory mFactory;
  private Map<int[], Integer> states;
  private Collection<TransitionProxy> trans;
  private EventProxy[] events;
  private int[][][][] transitions;
  private TIntArrayList[][] mBackTransitions;
  private boolean[][] mMarkedStates;
  private TIntArrayList mNewMarked;
  private List<int[]> newtrans = new ArrayList<int[]>();
  private int numStates;
  private Bag unvisited;
  private int[][] eventAutomaton;
  private final EventProxy mMarked;
  private int mNewDumpState;
  private int mDumpState = -1;
  private TIntHashSet[][] mIntDisabled;
  private TIntHashSet mNewInitial = new TIntHashSet();
}
