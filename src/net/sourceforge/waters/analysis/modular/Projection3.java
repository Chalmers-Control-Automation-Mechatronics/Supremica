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

package net.sourceforge.waters.analysis.modular;

import java.io.File;
import java.io.FileWriter;
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

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractAutomatonBuilder;
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

  public boolean run()
    throws AnalysisException
  {
    long timer = 0;
    try {
      setUp();
      final int limit = getNodeLimit();
      final ProductDESProxy model = getModel();
      states = new HashMap<StateTuple, StateTuple>(limit);
      trans = new ArrayList<TransitionProxy>();
      events = model.getEvents().toArray(new EventProxy[model.getEvents().size()]);
      if(mPrintData){
        addOutputData(1, events.length +"");
      }
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
      if(mPrintData){
        addOutputData(2,mHide.size() + "");
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
      int stateCounter = 0;
      for (int i = 0; i < aut.length; i++) {
        stateCounter += aut[i].getStates().size();
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
      if(mPrintData){
        addOutputData(0, stateCounter+"");
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

      if(mPrintData){
        timer = System.currentTimeMillis();
      }
      // Time to start building the automaton
      mNumberOfStates = 1;
      StateTuple currentState = new StateTuple(setStates);
      currentState.setName(0);
      states.put(currentState, currentState);
      mCoupleQueue = new ArrayDeque<StateTuple>(100);
      mCoupleQueue.offer(currentState);
      int silentTransitionCounter = 0;
      int size = 0;
        try{
          while (!mCoupleQueue.isEmpty()) {
            currentState = mCoupleQueue.remove();
            if (!exploreSyncProduct(currentState, true)) {
              exploreSyncProduct(currentState, false);
            }
          }
          size = states.size();
          timer = System.currentTimeMillis() - timer;
          states = null;
          currentState = null;
          mTransitions = new int[1][events.length][mNumberOfStates];
          for (int i = 0; i < mTransitions[0].length; i++) {
            for (int j = 0; j < mTransitions[0][i].length; j++) {
              mTransitions[0][i][j] = -1;
            }
          }
          for (final int[] tran : newtrans) {
            //calculate the number of silent transitions in the sync product
            if(tran[1] < mHide.size()){
              silentTransitionCounter++;
            }
            mTransitions[0][tran[1]][tran[0]] = tran[2];
          }
      } finally{
        if(mPrintData){
          //calculate and add to the output the time it took for synchronous product
          if(size == 0){
            size = states.size();
            timer = System.currentTimeMillis() - timer;
          }
          addOutputData(4, timer + "");
          addOutputData(5, size + "");
          addOutputData(3, silentTransitionCounter + "");
          timer = System.currentTimeMillis();
        }
      }
      mNumberOfStates = 1;
      currentState = null;
      setStates = null;
      mDeterministicQueue = new ArrayDeque<DeterministicState>(100);
      final Set<Integer> createState = new HashSet<Integer>();
      createState.add(0);
      DeterministicState detState = new DeterministicState(createState);
      switch(mAlgorithm){
        //case GRAPH: graph not implemented yet
        case STATE: detState = Memo_TotalState_Closure(detState); break;
        case SUBSET: detState = Memo_Subset_Closure(detState); break;
        default: throw new IllegalStateException("Unknown algorithm " + mAlgorithm);
      }
      newStates = new CollectionDeterministic(limit);
      newStates.insert(detState, 0);
      mDeterministicQueue.offer(detState);
      try{
        while (!mDeterministicQueue.isEmpty()) {
          detState = mDeterministicQueue.remove();
          if (!exploreSubsetConstruction(detState, true)) {
            exploreSubsetConstruction(detState, false);
          }
        }
      } finally {
        if(mPrintData){
          //calculate and add to the output the time it took for deterministic automata
          addOutputData(6,(System.currentTimeMillis() - timer) + "");
          addOutputData(7, newStates.getSize()+"");
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
                                     ev, newStates.getAllStateProxy(), trans);
      mTransitions = null;
      newStates = null;
      trans = null;
      if(mPrintData){
        timer = System.currentTimeMillis();
      }
      final Minimizer min = new Minimizer(result, factory);
      result = min.run();
      if(mPrintData){
        addOutputData(8, (System.currentTimeMillis() - timer)+"");
        addOutputData(9, result.getStates().size()+"");
      }
      return setAutomatonResult(result);
    } finally {
      tearDown();
      if(mPrintData)
        printOutput();
    }
  }

  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    mStartTime = System.currentTimeMillis();
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
    mDetRecord = new HashMap<DeterministicState, DeterministicState>();
    mNonDetRecord = new HashMap<Integer, DeterministicState>();
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
    if(mPrintData){
      addOutputData(10, (System.currentTimeMillis() - mStartTime) + "");
    }
  }

  @Override
  protected void addStatistics(){
    super.addStatistics();

  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return true;
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean exploreSyncProduct(final StateTuple state,
                                     final boolean forbidden)
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
      final StateTuple successor = new StateTuple(suc);
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
        final int limit = getNodeLimit();
        if (mNumberOfStates > limit) {
          throw new OverflowException(limit);
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
      switch(mAlgorithm){
      //case GRAPH: graph not implemented yet
      case STATE: succ = Memo_TotalState_Closure(succ); break;
      case SUBSET: succ = Memo_Subset_Closure(succ); break;
      default: throw new IllegalStateException("Unknown algorithm " + mAlgorithm);
    }
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

  public DeterministicState Subset_Closure(final DeterministicState state)
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

  public DeterministicState State_Closure(final int state)
  {
    final int numHidden = mHide.size();
    final Set<Integer> setofstates = new TreeSet<Integer>();
    final IntBag nextstate = new IntBag(100);
    //for each state in det state ( given det state with 1 element)
    setofstates.add(state);
    nextstate.offer(state);
    // find out what states can be reached from state with hidden events
    //while ther is an unmarked state
    while (!nextstate.isEmpty()) {
      //take the next state
      final int s = nextstate.take();
      //for each event to be hidden
      for (int i = 0; i < numHidden; i++) {
        //get the target state for the hidden events from source state
        final int newstate = mTransitions[0][i][s];
        //if target state is = -1, then source state cannot get to target directly using hidden event
        if (newstate == -1) {
          continue;
        }
        //if the set of states does not have target state, add it and then add it to the unmarked states
        if (setofstates.add(newstate)) {
          nextstate.offer(newstate);
        }
      }
    }
    //create the new determinstic state and return it
    final DeterministicState result = new DeterministicState(setofstates);
    return result;
  }

  //per non-det state use this method
  private DeterministicState Memo_TotalState_Closure(final DeterministicState d){
    DeterministicState result = mDetRecord.get(d);
    if(result == null){
      DeterministicState temp;
      //for each not deterministic state in the det state set
      for(final int i : d.getSetState()){
        temp = Memo_SingleState_Closure(i);
        result = DeterministicState.merge(result, temp);
      }
      mDetRecord.put(d, result);
    }
    return result;
  }

  private DeterministicState Memo_SingleState_Closure (final int s){
    //if we have already seen this state before, then return the results we already found
    if(mNonDetRecord.containsKey(s)){
     return mNonDetRecord.get(s);
    } else {
      //else find the resulting set of states, and reference it with key origonal state (s)
      final DeterministicState result = State_Closure(s);
      mNonDetRecord.put(s, result);
      return result;
    }
  }

  private DeterministicState Memo_Subset_Closure (final DeterministicState d){
    if(mDetRecord.containsKey(d)){
     return mDetRecord.get(d);
    } else {
      final DeterministicState result = Subset_Closure(d);
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

  public void setOutputStream(final boolean set){
    mPrintData = set;
    if(mPrintData){
      mFileName = "Projection3"+mAlgorithm+"Results.csv";
      mRowResults = new String[]{"#States","#Events","#Hidden","#Sync.Trans",
                   "Sync.Time","Syne.States","Det.Time",
                   "Det.States","Min.Time","Min.States","Overall"};
      final File mFile = new File(mFileName);
      if(!mFile.exists()){
        try
        {
          final FileWriter writer = new FileWriter(mFile, true);
          for(final String result : mRowResults)
          {
            writer.append(result);
            writer.append(',');
          }
          writer.append('\n');
          writer.close();
          mRowResults = new String[]{"#States","#Events","#Hidden","#Sync.Trans",
                                     "Sync.Time","Syne.States","Det.Time",
                                     "Det.States","Min.Time","Min.States","Overall"};
        }
        catch(final Exception e)
        {
          e.printStackTrace();
        }
      }
    }
  }

  private void addOutputData(final int index, final String data){
    mRowResults[index] = data;
  }

  private void printOutput(){
    //append to string builder end of line character
    //get whole row of results
    try
    {
      final File mFile = new File(mFileName);
      final FileWriter writer = new FileWriter(mFile, true);
      for(final String result : mRowResults)
      {
        writer.append(result);
        writer.append(',');
      }
      writer.append('\n');
      writer.flush();
      writer.close();
      mRowResults = new String[]{"#States","#Events","#Hidden","#Sync.Trans",
                                 "Sync.Time","Syne.States","Det.Time",
                                 "Det.States","Min.Time","Min.States","Overall"};
    }
    catch(final Exception e)
    {
      e.printStackTrace();
    }
  }

  public enum Method {
    //method will determine which algorithm will be used.
    //graph not implemented yet.
    //GRAPH,
    STATE,
    SUBSET
    };

    public void setMethod(final Method method){
      mAlgorithm = method;
    }


  private String[] mRowResults;
  private long mStartTime;
  private String mFileName;
  private boolean mPrintData;
  private boolean[] mPossible;
  private Method mAlgorithm;
  private Set<EventProxy> mHide;
  private Set<EventProxy> mForbidden;
  private Set<EventProxy> mDisabled;
  private Map<StateTuple, StateTuple> states;
  private Collection<TransitionProxy> trans;
  private EventProxy[] events;
  private int[][][] mTransitions;
  private List<int[]> newtrans = new ArrayList<int[]>();
  private CollectionDeterministic newStates;
  private int mNumberOfStates;
  private Deque<StateTuple> mCoupleQueue;
  private Deque<DeterministicState> mDeterministicQueue;
  private Map<DeterministicState, DeterministicState> mDetRecord;
  private Map<Integer, DeterministicState> mNonDetRecord;
  private int[][] eventAutomaton;
}
