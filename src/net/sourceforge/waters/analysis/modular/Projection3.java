//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   Projection2
//###########################################################################
//# $Id: Projection2.java 5886 2010-08-08 22:29:32Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.model.analysis.AbstractAutomatonBuilder;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class Projection3
extends AbstractAutomatonBuilder
implements SafetyProjectionBuilder
{

  public Projection3(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public Projection3(final ProductDESProxy model,
                     final ProductDESProxyFactory factory,
                     final Set<EventProxy> hide,
                     final Set<EventProxy> forbidden)
  {
    super(model, factory);
    mHide = hide;
    mForbidden = new HashSet<EventProxy>(forbidden);
  }

  public Set<EventProxy> getHidden()
  {
    return mHide;
  }

  public void setHidden(final Set<EventProxy> hidden)
  {
    mHide = hidden;
  }

  public Set<EventProxy> getForbidden()
  {
    return mForbidden;
  }

  public void setForbidden(final Set<EventProxy> forbidden)
  {
    mForbidden = forbidden;
  }

  public void setNodeLimit(final int stateLimit)
  {
    mNodeLimit = stateLimit;
  }

  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();
      final ProductDESProxy model = getModel();
      states = new HashMap<StateCouple, StateCouple>(mNodeLimit);
      trans = new ArrayList<TransitionProxy>();
      events = model.getEvents().toArray(new EventProxy[model.getEvents().size()]);
      mPossible = new boolean[events.length];
      final int numAutomata = model.getAutomata().size();
      AutomatonProxy[] aut = model.getAutomata().toArray(new AutomatonProxy[numAutomata]);
      eventAutomaton = new int[events.length][numAutomata];
      int l = 0;
      // transitions indexed first by automaton then by event then by source state
      mTransitions = new int[numAutomata][events.length][];
      // go through and put all the events to be hidden to the front
      Map<EventProxy, Integer> eventToIndex = new HashMap<EventProxy, Integer>(events.length);
      for (int i = 0; i < events.length; i++) {
        if (mHide.contains(events[i])) {
          final EventProxy temp = events[i];
          events[i] = events[l];
          events[l] = temp;
          l++;
        }
      }
      assert(l == mHide.size());
      //put all the forbidden events directly after the Hidden ones
      for (int i = l; i < events.length; i++) {
        if (mForbidden.contains(events[i])) {
          final EventProxy temp = events[i];
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
            final int[] states1 = new int[aut[j].getStates().size()];
            Arrays.fill(states1, -1);
            mTransitions[j][i] = states1;
          } else {
            mTransitions[j][i] = null;
          }
        }
      }
      int[] setStates = new int[numAutomata];
      for (int i = 0; i < aut.length; i++) {
        final Map<StateProxy, Integer> stateMap = new HashMap<StateProxy, Integer>(aut[i].getStates().size());
        l = 0;
        for (final StateProxy s : aut[i].getStates()) {
          if (s.isInitial()) {
            setStates[i] = l;
          }
          stateMap.put(s, l);
          l++;
        }
        assert(l == aut[i].getStates().size());
        for (final TransitionProxy t : aut[i].getTransitions()) {
          mTransitions[i][eventToIndex.get(t.getEvent())]
                          [stateMap.get(t.getSource())] = stateMap.get(t.getTarget());
        }
      }
      for (int i = 0; i < events.length; i++) {
        final IntDouble[] list = new IntDouble[numAutomata];
        for (int j = 0; j < aut.length; j++) {
          list[j] = new IntDouble(j, 0);
          if (mTransitions[j][i] != null) {
            for (int k = 0; k < mTransitions[j][i].length; k++) {
              if (mTransitions[j][i][k] != -1) {
                list[j].mDouble++;
              }
            }
            list[j].mDouble /= (double)mTransitions[j][i].length;
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
      mNumberOfStates = 1;
      StateCouple currentState = new StateCouple(setStates);
      currentState.setName(0);
      states.put(currentState, currentState);
      mCoupleQueue = new ArrayDeque<StateCouple>(100);
      mCoupleQueue.offer(currentState);
      while (!mCoupleQueue.isEmpty()) {
        currentState = mCoupleQueue.remove();
        if (!exploreSyncProduct(currentState, true)) {
          exploreSyncProduct(currentState, false);
        }
      }
      states = null;
      currentState = null;
      mTransitions = new int[1][events.length][mNumberOfStates];
      for (int i = 0; i < mTransitions[0].length; i++) {
        for (int j = 0; j < mTransitions[0][i].length; j++) {
          mTransitions[0][i][j] = -1;
        }
      }
      for (final int[] tran : newtrans) {
        mTransitions[0][tran[1]][tran[0]] = tran[2];
      }
      mNumberOfStates = 1;
      currentState = null;
      setStates = null;
      mDeterministicQueue = new ArrayDeque<DeterministicState>(100);
      final Set<Integer> createState = new HashSet<Integer>();
      createState.add(0);
      DeterministicState detState = new DeterministicState(createState);
      detState = closure(detState);
      newStates = new CollectionDeterministic(mNodeLimit);
      newStates.insert(detState, 0);
      mDeterministicQueue.offer(detState);
      while (!mDeterministicQueue.isEmpty()) {
        mDeterministicQueue.remove();
        if (!exploreSubsetConstruction(detState, true)) {
          exploreSubsetConstruction(detState, false);
        }
      }
      final Collection<EventProxy> ev = new ArrayList<EventProxy>(model.getEvents());
      ev.removeAll(mHide);

      final StringBuffer name = new StringBuffer();
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
                                     ev, newStates.getAllStateProxy(), trans);
      newStates = null;
      trans = null;
      final Minimizer min = new Minimizer(result, factory);
      result = min.run();
      return setAutomatonResult(result);
    } finally {
      tearDown();
    }
  }

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
    mDisabled = new HashSet<EventProxy>(events);
    mDisabled.removeAll(mHide);
    mNumberOfStates = 1;
    newtrans = new ArrayList<int[]>();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mDisabled = null;
    states = null;
    trans = null;
    events = null;
    mTransitions = null;
    newtrans = null;
    newStates = null;
    mCoupleQueue = null;
    mDeterministicQueue = null;
    eventAutomaton = null;
  }

  public boolean exploreSyncProduct(final StateCouple state, final boolean forbidden)
    throws OverflowException
  {
    boolean result = false;
    final int numAutomata = mTransitions.length;
    final int source = states.get(state).getName();
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
      final int[] suc = new int[numAutomata];
      for (int l = 0; l < numAutomata; l++) {
        final int automaton = eventAutomaton[i][l];
        if (mTransitions[automaton][i] != null) {
          suc[automaton] = mTransitions[automaton][i][state.getState(automaton)];
        } else {
          suc[automaton] = state.getState(automaton);
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
      final StateCouple successor = new StateCouple(suc);
      mDisabled.remove(events[i]);
      Integer target;
      if(states.containsKey(successor)){
         target = states.get(successor).getName();
      } else {
        target = -1;
      }
      if (target == -1) {
        target = mNumberOfStates;
        successor.setName(target);
        states.put(successor, successor);
        mNumberOfStates++;
        if (mNumberOfStates > mNodeLimit) {
          throw new OverflowException(mNodeLimit);
        }
        mCoupleQueue.offer(successor);
      }
      newtrans.add(new int[] {source, i, target});
      mPossible[i] = true;
    }
    return result;
  }

  public boolean exploreSubsetConstruction(final DeterministicState state, final boolean forbidden)
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
    final ProductDESProxyFactory factory = getFactory();
    final StateProxy source = newStates.getStateProxy(state);
    for (int i = min; i < max; i++) {
      final Set<Integer> successor = new HashSet<Integer>(state.size());
      for (int j = 0; j < state.size(); j++) {
        final int s = mTransitions[0][i][state.getState(j)];
        if (s == -1) {
          continue;
        }
        successor.add(s);
      }
      if (successor.isEmpty()) {
        continue;
      }
      result = true;
      DeterministicState succ = new DeterministicState(successor);
      succ = closure(succ);
      StateProxy target = newStates.getStateProxy(succ);
      if (target == null) {
        newStates.insert(succ, mNumberOfStates);
        target = newStates.getStateProxy(succ);
        mNumberOfStates++;
        final int limit = getNodeLimit();
        if (mNumberOfStates > limit) {
          throw new OverflowException(limit);
        }
        mDeterministicQueue.offer(succ);
      }
      trans.add(factory.createTransitionProxy(source, events[i], target));
    }
    return result;
  }

  public DeterministicState closure(final DeterministicState state)
  {
    final int numHidden = mHide.size();
    final Set<Integer> setofstates = new TreeSet<Integer>();
    final IntBag nextstate = new IntBag(100);
    for (int i = 0; i < state.size(); i++) {
      if (setofstates.add(state.getState(i))) {
        nextstate.offer(state.getState(i));
      }
    }
    // find out what states can be reached from state with hidden events
    while (!nextstate.isEmpty()) {
      final int s = nextstate.take();
      for (int i = 0; i < numHidden; i++) {
        final int newstate = mTransitions[0][i][s];
        if (newstate == -1) {
          continue;
        }
        if (setofstates.add(newstate)) {
          nextstate.offer(newstate);
        }
      }
    }
    final DeterministicState result = new DeterministicState(setofstates);
    return result;
  }

  public DeterministicState state_closure(final int state)
  {
    final int numHidden = mHide.size();
    final Set<Integer> setofstates = new TreeSet<Integer>();
    final IntBag nextstate = new IntBag(100);
    nextstate.offer(state);
    // find out what states can be reached from state with hidden events
    while (!nextstate.isEmpty()) {
      final int s = nextstate.take();
      for (int i = 0; i < numHidden; i++) {
        final int newstate = mTransitions[0][i][s];
        if (newstate == -1) {
          continue;
        }
        if (setofstates.add(newstate)) {
          nextstate.offer(newstate);
        }
      }
    }
    final DeterministicState result = new DeterministicState(setofstates);
    return result;
  }

  //per non-det state use this method
  private DeterministicState silent_closure(final DeterministicState d){
    DeterministicState result = mDetRecord.get(d);
    if(result == null){
      DeterministicState temp;
      for(int i = 0; i < d.size(); i++){
        temp = memo_singleState_closure(d.getState(i));
        result = DeterministicState.merge(result, temp);
      }
    }
    return result;
  }

  private DeterministicState memo_singleState_closure (final int s){
    if(mNonDetRecord.containsKey(s)){
     return mNonDetRecord.get(s);
    } else {
      final DeterministicState result = state_closure(s);
      mNonDetRecord.put(s, result);
      return result;
    }
  }

  //per-subset method
  private DeterministicState memo_DetStates_closure (final DeterministicState d){
    if(mDetRecord.containsKey(d)){
     return mDetRecord.get(d);
    } else {
      final DeterministicState result = closure(d);
      mDetRecord.put(d,result);
      return result;
    }
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

  private boolean[] mPossible;
  private int mNodeLimit;
  private Set<EventProxy> mHide;
  private Set<EventProxy> mForbidden;
  private Set<EventProxy> mDisabled;
  private Map<StateCouple, StateCouple> states;
  private Collection<TransitionProxy> trans;
  private EventProxy[] events;
  private int[][][] mTransitions;
  private List<int[]> newtrans = new ArrayList<int[]>();
  private CollectionDeterministic newStates;
  private int mNumberOfStates;
  private Deque<StateCouple> mCoupleQueue;
  private Deque<DeterministicState> mDeterministicQueue;
  private Map<DeterministicState, DeterministicState> mDetRecord;
  private Map<Integer, DeterministicState> mNonDetRecord;
  private int[][] eventAutomaton;
}
