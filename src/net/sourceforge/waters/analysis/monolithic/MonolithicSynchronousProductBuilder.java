package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TObjectIntHashMap;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AbstractAutomatonBuilder;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.SynchronousProductStateMap;
import net.sourceforge.waters.model.analysis.VerificationResult;
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
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A simple monolithic implementation of the synchronous product algorithm.
 * This implementation supports nondeterministic automata and hiding.
 * States are stored in integer arrays without compression, so it is not
 * recommended to use this implementation to compose a large number of
 * automata.
 *
 * @author Simon Ware, Rachel Francis, Robi Malik
 */

public class MonolithicSynchronousProductBuilder
  extends AbstractAutomatonBuilder
  implements SynchronousProductBuilder
{

  //#########################################################################
  //# Constructors
  public MonolithicSynchronousProductBuilder
    (final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public MonolithicSynchronousProductBuilder
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SynchronousProductBuilder
  public Collection<EventProxy> getPropositions()
  {
    return mUsedPropositions;
  }

  public void setPropositions(final Collection<EventProxy> props)
  {
    mUsedPropositions = props;
  }

  public void addMask(final Collection<EventProxy> hidden,
                      final EventProxy replacement)
  {
    if (mMaskingPairs == null) {
      mMaskingPairs = new LinkedList<MaskingPair>();
    }
    final MaskingPair pair = new MaskingPair(hidden, replacement);
    mMaskingPairs.add(pair);
  }

  public void clearMask()
  {
    mMaskingPairs = null;
  }

  public SynchronousProductStateMap getStateMap()
  {
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final MemStateMap stateMap = new MemStateMap(automata);
    return stateMap;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean run()
    throws AnalysisException
  {
    setUp();

    final int tableSize = Math.min(getNodeLimit(), MAX_TABLE_SIZE);
    mStates = new IntArrayMap(tableSize);
    mStateTuples = new ArrayList<int[]>();
    mTransitionBuffer = new TIntArrayList();
    mNumStates = 0;
    mUnvisited = new ArrayDeque<int[]>(100);
    permutations(mNumAutomata, null, -1, -1);
    mNumInitialStates = mNumStates;
    while (!mUnvisited.isEmpty()) {
      final int[] tuple = mUnvisited.remove();
      explore(tuple);
    }

    final AutomatonProxy aut = createAutomaton();
    tearDown();
    return setAutomatonResult(aut);
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();

    final ProductDESProxy model = getModel();
    final Collection<EventProxy> events = model.getEvents();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    mNumEvents = events.size();
    mNumAutomata = automata.size();

    mEvents = new ArrayList<EventProxy>(mNumEvents);
    TObjectIntHashMap<EventProxy> eventToIndex =
      new TObjectIntHashMap<EventProxy>(mNumInputEvents);
    if (mUsedPropositions == null) {
      mCurrentPropositions = new ArrayList<EventProxy>();
    } else {
      mCurrentPropositions = mUsedPropositions;
    }
    int e = 0;
    for (final EventProxy event : events) {
      if (event.getKind() == EventKind.PROPOSITION) {
        if (mUsedPropositions == null) {
          mCurrentPropositions.add(event);
        }
      } else {
        mEvents.add(event);
        eventToIndex.put(event, e++);
      }
    }
    mNumInputEvents = mNumEvents = mEvents.size();
    if (mMaskingPairs != null) {
      mProjectionMask = new int[mNumInputEvents];
      Arrays.fill(mProjectionMask, -1);
      for (final MaskingPair pair : mMaskingPairs) {
        final EventProxy replacement = pair.getReplacement();
        if (eventToIndex.containsKey(replacement)) {
          e = eventToIndex.get(replacement);
        } else {
          e = mNumEvents++;
          eventToIndex.put(replacement, e);
          mEvents.add(replacement);
        }
        for (final EventProxy hidden : pair.getHiddenEvents()) {
          final int h = eventToIndex.get(hidden);
          mProjectionMask[h] = e;
        }
      }
      mCurrentSuccessors = new TIntHashSet[mNumEvents];
      for (e = 0; e < mNumEvents; e++) {
        mCurrentSuccessors[e] = new TIntHashSet();
      }
    }
    final int numProps = mCurrentPropositions.size();
    mOriginalStates = new StateProxy[mNumAutomata][];
    mAllMarkings = new HashMap<List<EventProxy>,List<EventProxy>>();
    mStateMarkings = new List<?>[mNumAutomata][];
    // transitions indexed first by automaton then by event then by source state
    mTransitions = new int[mNumAutomata][mNumInputEvents][][];
    mTargetTuple = new int[mNumAutomata];
    mNDTuple = new int[mNumAutomata][];

    int a = 0;
    for (final AutomatonProxy aut : automata) {
      final Collection<EventProxy> localEvents = aut.getEvents();
      final List<EventProxy> nonLocalProps =
        new ArrayList<EventProxy>(numProps);
      for (final EventProxy prop : mCurrentPropositions) {
        if (!localEvents.contains(prop)) {
          nonLocalProps.add(prop);
        }
      }
      Collections.sort(nonLocalProps);
      final Collection<StateProxy> states = aut.getStates();
      final int numStates = states.size();
      final TObjectIntHashMap<StateProxy> stateToIndex =
        new TObjectIntHashMap<StateProxy>(numStates);
      final TIntArrayList initials = new TIntArrayList(1);
      int snum = 0;
      mOriginalStates[a] = new StateProxy[numStates];
      mStateMarkings[a] = new List<?>[numStates];
      for (final StateProxy state : states) {
        stateToIndex.put(state, snum);
        mOriginalStates[a][snum] = state;
        if (state.isInitial()) {
          initials.add(snum);
        }
        final Collection<EventProxy> props = state.getPropositions();
        final List<EventProxy> stateProps;
        if (props.isEmpty()) {
          stateProps = nonLocalProps;
        } else {
          stateProps = new ArrayList<EventProxy>(numProps + props.size());
          stateProps.addAll(nonLocalProps);
          for (final EventProxy prop : props) {
            if (mCurrentPropositions.contains(prop)) {
              stateProps.add(prop);
            }
          }
          Collections.sort(stateProps);
        }
        mStateMarkings[a][snum] = getUniqueMarking(stateProps);
        snum++;
      }
      mNDTuple[a] = initials.toNativeArray();
      final TIntArrayList[][] autTransitionLists =
        new TIntArrayList[mNumInputEvents][numStates];
      for (final TransitionProxy trans : aut.getTransitions()) {
        final int event = eventToIndex.get(trans.getEvent());
        final int source = stateToIndex.get(trans.getSource());
        final int target = stateToIndex.get(trans.getTarget());
        TIntArrayList list = autTransitionLists[event][source];
        if (list == null) {
          list = new TIntArrayList(1);
          autTransitionLists[event][source] = list;
        }
        list.add(target);
      }
      for (final EventProxy event : localEvents) {
        if (event.getKind() != EventKind.PROPOSITION) {
          e = eventToIndex.get(event);
          mTransitions[a][e] = new int[numStates][];
          for (int source = 0; source < numStates; source++) {
            final TIntArrayList list = autTransitionLists[e][source];
            if (list != null) {
              mTransitions[a][e][source] = list.toNativeArray();
            }
          }
        }
      }
      a++;
    }
    eventToIndex = null;

    mEventAutomata = new int[mNumInputEvents][];
    final List<IntDouble> list = new ArrayList<IntDouble>(mNumAutomata);
    for (e = 0; e < mNumInputEvents; e++) {
      for (a = 0; a < mNumAutomata; a++) {
        if (mTransitions[a][e] != null) {
          final int numStates = mTransitions[a][e].length;
          int count = 0;
          for (int source = 0; source < numStates; source++) {
            if (mTransitions[a][e][source] != null) {
              count++;
            }
          }
          final double avg = (double) count / (double) numStates;
          final IntDouble pair = new IntDouble(a, avg);
          list.add(pair);
        }
      }
      Collections.sort(list);
      final int count = list.size();
      mEventAutomata[e] = new int[count];
      int i = 0;
      for (final IntDouble pair : list) {
        mEventAutomata[e][i++] = pair.mInt;
      }
      list.clear();
    }
  }

  protected void tearDown()
  {
    mEvents = null;
    mCurrentPropositions = null;
    mProjectionMask = null;
    mOriginalStates = null;
    mAllMarkings = null;
    mStateMarkings = null;
    mTransitions = null;
    mEventAutomata = null;

    mStates = null;
    mStateTuples = null;
    mUnvisited = null;
    mTransitionBuffer = null;

    mNDTuple = null;
    mTargetTuple = null;
    mCurrentSuccessors = null;

    super.tearDown();
  }

  protected void addStatistics(final VerificationResult result)
  {
    result.setNumberOfAutomata(mNumAutomata);
    result.setNumberOfStates(mNumStates);
    result.setNumberOfTransitions(mTransitionBuffer.size() / 3);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void explore(final int[] sourceTuple)
    throws OverflowException
  {
    final int source = mStates.get(sourceTuple);
    if (mCurrentSuccessors != null) {
      for (int e = 0; e < mNumEvents; e++) {
        mCurrentSuccessors[e].clear();
      }
    }
    events:
    for (int e = 0; e < mNumInputEvents; e++) {
      Arrays.fill(mNDTuple, null);
      for (final int a : mEventAutomata[e]) {
        if (mTransitions[a][e] != null) {
          final int[] succ = mTransitions[a][e][sourceTuple[a]];
          if (succ == null) {
            continue events;
          }
          mNDTuple[a] = succ;
        }
      }
      permutations(mNumAutomata, sourceTuple, source, e);
    }
  }

  private void permutations(int a,
                            final int[] sourceTuple,
                            final int source,
                            final int event)
    throws OverflowException
  {
    if (a == 0) {
      addTargetState(source, event, sourceTuple == null); // data in mTuple
    } else {
      a--;
      final int[] codes = mNDTuple[a];
      if (codes == null) {
        mTargetTuple[a] = sourceTuple[a];
        permutations(a, sourceTuple, source, event);
      } else {
        for (int i = 0; i < codes.length; i++) {
          mTargetTuple[a] = codes[i];
          permutations(a, sourceTuple, source, event);
        }
      }
    }
  }

  private void addTargetState(final int source,
                              final int event,
                              final boolean isInitial)
    throws OverflowException
  {
    Integer target = mStates.get(mTargetTuple);
    if (target == null) {
      final int limit = getNodeLimit();
      if (mNumStates >= limit) {
        throw new OverflowException(limit);
      }
      target = mNumStates++;
      final int[] newTuple = Arrays.copyOf(mTargetTuple, mNumAutomata);
      mStates.put(newTuple, target);
      mUnvisited.offer(newTuple);
      mStateTuples.add(newTuple);
    }
    // Only add a transition if not adding in an initial state,
    // and avoid duplicates.
    if (!isInitial) {
      if (mCurrentSuccessors == null ||
          mCurrentSuccessors[event].add(target)) {
        mTransitionBuffer.add(source);
        mTransitionBuffer.add(event);
        mTransitionBuffer.add(target);
      }
    }
  }

  private AutomatonProxy createAutomaton()
  {
    final int numEvents = mNumEvents + mCurrentPropositions.size();
    final Collection<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    if (mMaskingPairs == null) {
      events.addAll(mEvents);
    } else {
      final THashSet<EventProxy> hidden = new THashSet<EventProxy>(mNumEvents);
      for (final MaskingPair pair : mMaskingPairs) {
        hidden.addAll(pair.getHiddenEvents());
      }
      for (final EventProxy event : mEvents) {
        if (!hidden.contains(event)) {
          events.add(event);
        }
      }
    }
    events.addAll(mCurrentPropositions);

    final int numProps = mCurrentPropositions.size();
    final List<StateProxy> states = new ArrayList<StateProxy>(mNumStates);
    for (int code = 0; code < mNumStates; code++) {
      final boolean initial = code < mNumInitialStates;
      final int[] tuple = mStateTuples.get(code);
      final List<EventProxy> marking = new ArrayList<EventProxy>(numProps);
      props:
      for (final EventProxy prop : mCurrentPropositions) {
        for (int a = 0; a < mNumAutomata; a++) {
          final List<EventProxy> stateMarking = getStateMarking(a, tuple[a]);
          if (Collections.binarySearch(stateMarking, prop) < 0) {
            continue props;
          }
        }
        marking.add(prop);
      }
      Collections.sort(marking);
      final List<EventProxy> unique = getUniqueMarking(marking);
      final StateProxy state = new MemStateProxy(code, tuple, unique, initial);
      states.add(state);
    }

    final ProductDESProxyFactory factory = getFactory();
    final int bufferSize = mTransitionBuffer.size();
    final ArrayList<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(bufferSize / 3);
    int t = 0;
    while (t < bufferSize) {
      int code = mTransitionBuffer.get(t++);
      final StateProxy source = states.get(code);
      code = mTransitionBuffer.get(t++);
      final EventProxy event = mEvents.get(code);
      code = mTransitionBuffer.get(t++);
      final StateProxy target = states.get(code);
      transitions.add(factory.createTransitionProxy(source, event, target));
    }

    final StringBuffer buffer = new StringBuffer("{");
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    boolean first = true;
    for (final AutomatonProxy aut : automata) {
      if (first) {
        first = false;
      } else {
        buffer.append(',');
      }
      buffer.append(aut.getName());
    }
    buffer.append('}');
    final String name = buffer.toString();
    final ComponentKind kind = ComponentKind.PLANT;

    return factory.createAutomatonProxy
      (name, kind, events, states, transitions);
  }

  private List<EventProxy> getUniqueMarking(final List<EventProxy> marking)
  {
    final List<EventProxy> found = mAllMarkings.get(marking);
    if (found == null) {
      mAllMarkings.put(marking, marking);
      return marking;
    } else {
      return found;
    }
  }

  @SuppressWarnings("unchecked")
  final List<EventProxy> getStateMarking(final int a, final int code)
  {
    return (List<EventProxy>) mStateMarkings[a][code];
  }


  //#########################################################################
  //# Inner Class MaskingPair
  private static class MaskingPair
  {
    //#######################################################################
    //# Constructor
    private MaskingPair(final Collection<EventProxy> hidden,
                        final EventProxy replacement)
    {
      mHiddenEvents = hidden;
      mReplacement = replacement;
    }

    //#######################################################################
    //# Simple Access
    private Collection<EventProxy> getHiddenEvents()
    {
      return mHiddenEvents;
    }

    private EventProxy getReplacement()
    {
      return mReplacement;
    }

    //#######################################################################
    //# Data Members
    private final Collection<EventProxy> mHiddenEvents;
    private final EventProxy mReplacement;
  }


  //#########################################################################
  //# Inner Class MemStateMap
  private static class MemStateMap implements SynchronousProductStateMap
  {
    //#######################################################################
    //# Constructor
    private MemStateMap(final Collection<AutomatonProxy> automata)
    {
      mInputAutomata = new ArrayList<AutomatonProxy>(automata);
      final int numaut = automata.size();
      mStateLists = new StateProxy[numaut][];
      // Assumes state codes are given by their ordering in the original
      // automata. If this is not good enough, need to provide method
      // setStateList(int a, StateProxy[] states).
      int a = 0;
      for (final AutomatonProxy aut : mInputAutomata) {
        final Collection<StateProxy> states = aut.getStates();
        final int size = states.size();
        mStateLists[a++] = states.toArray(new StateProxy[size]);
      }
    }

    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.model.analysis.SynchronousProductStateMap
    public Collection<AutomatonProxy> getInputAutomata()
    {
      return mInputAutomata;
    }

    public StateProxy getOriginalState(final StateProxy state,
                                       final AutomatonProxy aut)
    {
      final int a = getAutomatonIndex(aut);
      final MemStateProxy memstate = (MemStateProxy) state;
      final int[] tuple = memstate.getStateTuple();
      final int code = tuple[a];
      return mStateLists[a][code];
    }

    //#######################################################################
    //# Auxiliary Methods
    /**
     * Gets the index position of the given automaton in state tuples. Presently
     * linear complexity --- is this good enough?
     */
    private int getAutomatonIndex(final AutomatonProxy aut)
    {
      return mInputAutomata.indexOf(aut);
    }

    //#######################################################################
    //# Data Members
    private final List<AutomatonProxy> mInputAutomata;
    private final StateProxy[][] mStateLists;
  }


  //#########################################################################
  //# Inner Class MemStateProxy
  /**
   * Stores states, encoding the name as an int rather than a long string value.
   */
  private static class MemStateProxy implements StateProxy
  {
    //#######################################################################
    //# Constructor
    private MemStateProxy(final int name, final int[] stateTuple,
                          final Collection<EventProxy> props,
                          final boolean isInitial)
    {
      mName = name;
      mStateTuple = stateTuple;
      mProps = props;
      mIsInitial = isInitial;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.des.StateProxy
    public Collection<EventProxy> getPropositions()
    {
      return mProps;
    }

    public boolean isInitial()
    {
      return mIsInitial;
    }

    public MemStateProxy clone()
    {
      return new MemStateProxy(mName, mStateTuple, mProps, mIsInitial);
    }

    public String getName()
    {
      return "S:" + mName;
    }

    public int[] getStateTuple()
    {
      return mStateTuple;
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
      return getName();
    }

    //#######################################################################
    //# Data Members
    private final int mName;
    private final int[] mStateTuple;
    private final boolean mIsInitial;
    private final Collection<EventProxy> mProps;
  }


  //#########################################################################
  //# Inner Class IntArrayMap
  private static class IntArrayMap extends AbstractMap<int[],Integer>
  {
    private IntArrayMap(final int num)
    {
      mMap = new HashMap<IntArray,Integer>(num);
    }

    public Set<Map.Entry<int[],Integer>> entrySet()
    {
      throw new UnsupportedOperationException
        ("IntArrayMap does not support entrySet()!");
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

    private final Map<IntArray,Integer> mMap;
  }


  //#########################################################################
  //# Inner Class IntArray
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


  //#########################################################################
  //# Inner Class IntDouble
  private static class IntDouble implements Comparable<IntDouble>
  {
    public IntDouble(final int i, final double d)
    {
      mInt = i;
      mDouble = d;
    }

    public int compareTo(final IntDouble pair)
    {
      if (mDouble < pair.mDouble) {
        return -1;
      } else if (mDouble > pair.mDouble) {
        return 1;
      } else {
        return 0;
      }
    }

    private final int mInt;
    private final double mDouble;
  }


  //#########################################################################
  //# Data Members
  private Collection<EventProxy> mUsedPropositions;
  private Collection<MaskingPair> mMaskingPairs;

  private int mNumAutomata;
  private int mNumInputEvents;
  private int mNumEvents;
  private List<EventProxy> mEvents;
  private Collection<EventProxy> mCurrentPropositions;
  private int[] mProjectionMask;
  private StateProxy[][] mOriginalStates;
  private Map<List<EventProxy>,List<EventProxy>> mAllMarkings;
  private List<?>[][] mStateMarkings;
  private int[][][][] mTransitions;
  private int[][] mEventAutomata;

  private int mNumStates;
  private int mNumInitialStates;
  private Map<int[],Integer> mStates;
  private List<int[]> mStateTuples;
  private Queue<int[]> mUnvisited;
  private TIntArrayList mTransitionBuffer;

  private int[][] mNDTuple;
  private int[] mTargetTuple;
  private TIntHashSet[] mCurrentSuccessors;


  //#########################################################################
  //# Class Constants
  private static final int MAX_TABLE_SIZE = 500000;

}
