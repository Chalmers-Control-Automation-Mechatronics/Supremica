//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   Projection2
//###########################################################################
//# $Id: Projection2.java 5752 2010-06-04 04:53:20Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


public class Projection3 extends AbstractAutomatonBuilder
{

  public Projection3(final ProductDESProxy model,
                     final ProductDESProxyFactory factory,
                     final Set<EventProxy> hide,
                     final Set<EventProxy> forbidden)
  {
    super(model, factory);
    mHide = hide;
    mForbidden = new HashSet<EventProxy>(forbidden);
    mForbidden.retainAll(model.getEvents());
    mNodeLimit = 1000;
    mDisabled = new HashSet<EventProxy>(model.getEvents());
    mDisabled.remove(mHide);
    numStates = 1;
  }

  public void setNodeLimit(final int stateLimit)
  {
    mNodeLimit = stateLimit;
  }

  public boolean run()
    throws AnalysisException
  {
    try{
      setUp();
      final ProductDESProxy model = getModel();
      //initalize stuff
      //will be mapping int[](states), to integers
      states = new CollectionDeterministic(mNodeLimit);
      //list of all transitions
      trans = new ArrayList<TransitionProxy>();
      //array all events in whole model
      events = model.getEvents().toArray(new EventProxy[model.getEvents().size()]);
      //unsure
      mPossible = new boolean[events.length];
      //int of number of different automata in given model
      final int numAutomata = model.getAutomata().size();
      //array of all the different automata
      AutomatonProxy[] aut = model.getAutomata().toArray(new AutomatonProxy[numAutomata]);
      //array if event is used in particular automaton
      eventAutomaton = new int[events.length][numAutomata];
      //counter to keep track of where we are while moving hidden/forbidden events to front
      //of the event array
      int l = 0;
      // transitions indexed first by automaton then by event then by source state
      mTransitions = new int[numAutomata][events.length][];
      //maps an event proxy to a number, so we can track all events using numbers instead
      Map<EventProxy, Integer> eventToIndex = new HashMap<EventProxy, Integer>(events.length);
      // go through and put all the events to be hidden to the front
      for (int i = 0; i < events.length; i++) {
        if (mHide.contains(events[i])) {
          final EventProxy temp = events[i];
          events[i] = events[l];
          events[l] = temp;
          l++;
        }
      }
      //checks to makes sure we found all the hidden events
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
      //give each event a different number value
      for (int i = 0; i < events.length; i++) {
        eventToIndex.put(events[i], i);
      }
      //check we found all hidden and forbidden events
      assert(l == mHide.size() + mForbidden.size());
      // this has to be done after events gets it's final ordering to avoid
      // subtle bugs
      //for each event
      for (int i = 0; i < events.length; i++) {
        //for each automaton
        for (int j = 0; j < aut.length; j++) {
          //if automaton uses said event, set all the target state values to -1, else null
          if (aut[j].getEvents().contains(events[i])) {
            final int[] states1 = new int[aut[j].getStates().size()];
            Arrays.fill(states1, -1);
            mTransitions[j][i] = states1;
          } else {
            mTransitions[j][i] = null;
          }
        }
      }
      //array which will cover all states in given model
      DeterministicState currentState = new DeterministicState(numAutomata);
      //for each automaton
      for (int i = 0; i < aut.length; i++) {
        //for each state, map it a unique integer value,
        //so we use integers as opposed to stateProxy
        final Map<StateProxy, Integer> stateMap = new HashMap<StateProxy, Integer>(aut[i].getStates().size());
        //keep track of where we are in all states
        l = 0;
        //for each state in current automaton
        for (final StateProxy s : aut[i].getStates()) {
          if (s.isInitial()) {
            //if state is initial state, put it at the front of current state array
            currentState.insert(i, l);
          }
          //give state its unique integer value
          stateMap.put(s,l);
          l++;
        }
        //assert we have looked over all states in automaton
        assert(l == aut[i].getStates().size());
        //for each transition in the current automaton
        for (final TransitionProxy t : aut[i].getTransitions()) {
          //set transitions array
          //[current automaton]
          //[integer value from eventMap for said transition]
          //[integer value from stateMap for said transition]
          // set transition value to target state.
          mTransitions[i][eventToIndex.get(t.getEvent())]
                     [stateMap.get(t.getSource())] = stateMap.get(t.getTarget());
        }
      }
      //for all events in model
      for (int i = 0; i < events.length; i++) {
        final IntDouble[] list = new IntDouble[numAutomata];
        //for each automaton in model
        for (int j = 0; j < aut.length; j++) {
          //for each automaton, give it a double value of 0
          list[j] = new IntDouble(j, 0);
          //if said event is in the automaton, else set its double value to infinity
          if (mTransitions[j][i] != null) {
            //for each state in the automaton
            for (int k = 0; k < mTransitions[j][i].length; k++) {
              //if there is a transition from source state k
              if (mTransitions[j][i][k] != -1) {
                //increase automaton double value by 1
                list[j].mDouble++;
              }
            }
            //divide number of transitions by the total number of states in the automaton
            //stores the probability of event i being enabled in automaton j
            list[j].mDouble /= (double)mTransitions[j][i].length;
          } else {
            list[j].mDouble = Double.POSITIVE_INFINITY;
          }
        }
        //sort the list by lowest probability first
        Arrays.sort(list);
        //for the current event event in model,
        //for each automaton which uses the event, input its corresponding int value
        //inputs are ordered from state least using event to most
        for (int j = 0; j < eventAutomaton[i].length; j++) {
          eventAutomaton[i][j] = list[j].mInt;
        }
      }
      // don't need these anymore
      aut = null;
      eventToIndex = null;

      // Time to start building the automaton
      //number states in deterministic state machine
      numStates = 1;
      //encode was intended for bit packing the number states, not so interesting atm
      //encode all states in current state (atm start states)
      //encode atm returns same state as entered
      currentState = encode(currentState);
      //input set start states into States -> its value is its state number
      states.insert(0, currentState);
      //create marked array
      unvisited = new DeterministicBag(100);
      //add current state to marked states
      unvisited.offer(currentState);
      //while there is an unmarked subset T in States
      while (!unvisited.isEmpty()) {
        //get a state from unmarked -> this marks the state
        currentState = unvisited.take();
        //explore sync product for each subset of states
        if (!exploreSyncProduct(currentState, true)) {
          exploreSyncProduct(currentState, false);
        }
      }
      //set all state to null
      states = new CollectionDeterministic(mNodeLimit);;
      //clear current state to nothing
      currentState = new DeterministicState(1);
      //set mTransitions to having only 1 automaton, number events and number sates
      //this is for the new deterministic state machine
      mTransitions = new int[1][events.length][numStates];
      //for each event in automaton
      for (int i = 0; i < mTransitions[0].length; i++) {
        //for each state in automaton
        for (int j = 0; j < mTransitions[0][i].length; j++) {
          //set target state to -1
          mTransitions[0][i][j] = -1;
        }
      }
      //for each transition in new trans
      for (final int[] tran : newtrans) {
        //set the transiton
        //mTransition
        //            [0]       <- deterministic automaton
        //            [tran[1]] <- transition event
        //            [tran[0]] <- det source state
        //            [tran[2]] <- det target state
        mTransitions[0][tran[1]][tran[0]] = tran[2];
      }
      //set number states = 1
      numStates = 1;
      //set current state to its new actual state
      currentState = actualState(currentState);
      //put current state in newState, with value
      currentState.setProxy(0);
      states.insert(0, currentState);
      //add current state to unmarked states
      unvisited.offer(currentState);
      //while there is an unmarked state
      while (!unvisited.isEmpty()) {
        //take a state, unmarking it
        currentState = unvisited.take();
        //explore current state
        if (!exploreSubsetConstruction(currentState, true)) {
          exploreSubsetConstruction(currentState, false);
        }
      }
      //collection of evenyProxy using all events
      final Collection<EventProxy> ev = new ArrayList<EventProxy>(model.getEvents());
      //remove all hidden events from event list
      ev.removeAll(mHide);

      //create a name for new automaton
      final StringBuffer name = new StringBuffer();
      //add "proj:" to name
      name.append("proj:");
      //list of names for all automaton in whole model
      final ArrayList<String> names = new ArrayList<String>(model.getAutomata().size());
      //for each automaton in whole model
      for (final AutomatonProxy a : model.getAutomata()) {
        //add the name of current automaton to the list
        names.add(a.getName());
      }
      //sort all the names in the list
      Collections.sort(names);
      //amount of all names
      final int namesize = names.size();
      //for each name
      for (int i=0;i<namesize;i++) {
        //append automaton name to new det name
        name.append(names.get(i));
        //add "," after each name
        if (i<namesize-1) name.append(",");
      }
      final ProductDESProxyFactory factory = getFactory();
      //build resulting deterministic automaton
      AutomatonProxy result =
        factory.createAutomatonProxy(name.toString(), ComponentKind.PLANT,
                                      ev, states.getAllStateProxy(), trans);
      //set unused things to null for garbage colllector
      states = null;
      trans = null;
      //minimize result
      final Minimizer min = new Minimizer(result, factory);
      result = min.run();
      //return new projected automaton
      return setAutomatonResult(result);
    } finally {
      tearDown();
    }
  }

  public boolean exploreSyncProduct(DeterministicState state, final boolean forbidden)
    throws OverflowException
  {
    boolean result = false;
    //total number of automata
    final int numAutomata = mTransitions.length;
    //deterministic state number, of state working on
    final int source = states.getName(state);
    //decode state returns same thing as entered
    state = decode(state);
    //min and max values
    int min, max;
    if (forbidden) {
      //if forbidden true, min = the size of hidden events
      min = mHide.size();
      //max = size hidden and forbidden events
      max = mHide.size() + mForbidden.size();
    } else {
      //min  = 0
      min = 0;
      //max = all events
      max = events.length;
    }
    events:
      //for each event between min and max
    for (int i = min; i < max; i++) {
      //if forbidden not true
      if (!forbidden) {
        //dont understnd this fully
        //if i >= hidden but (hidden + forbidden) < i <== does not make sense
        if (mHide.size() >= i && (mHide.size() + mForbidden.size()) < i) {
          continue;
        }
      }
      //array of successor states
      final DeterministicState suc = new DeterministicState(numAutomata);
      //for each automata in model
      for (int l = 0; l < numAutomata; l++) {
        //for specified automaton ...
        final int automaton = eventAutomaton[i][l];
        //if automaton uses event i
        if (mTransitions[automaton][i] != null) {
          //add sate to automaton
          //mTransitions
          //   ...[automaton]       <- current automaton working on
          //   ...[i]               <- current event working on
          //   ...[state[automaton]]<- returns specific state in det state which uses event i in automaton
          //mTransitions[automaton][i][state[automaton]] returns target state of soucre using event i
          suc.insert(automaton, mTransitions[automaton][i][state.getState(automaton)]);
        } else {
          //else add self to successor state
          suc.insert(automaton, state.getState(automaton));
        }
        //if successor of specific automaton = -1
        if (suc.getState(automaton) == -1) {
          //if current automaton is a valid automaton
          if (l > 0) {
            //create temp state
            //switch eventAutomaton[i][l] and eventAutomaton[i][l-1] around
            final int t = eventAutomaton[i][l];
            eventAutomaton[i][l] = eventAutomaton[i][l - 1];
            eventAutomaton[i][l - 1] = t;
          }
          //continue events until there are no successor state which return -1
          continue events;
        }
      }
      result = true;
      //remove from disabled events the current event
      mDisabled.remove(events[i]);
      // target state of current det state is its successor states of non-det automaton
      Integer target = states.getName(suc);
      //if there are no target states from source state
      if (target == -1) {
        target = numStates;
        //set target state name to current value
        //add det state to state map with value target
        states.insert(target, suc);
        //increment total number of states
        numStates++;
        if (numStates > mNodeLimit) {
          throw new OverflowException(mNodeLimit);
        }
        //add newly created state to unmarked
        unvisited.offer(suc);
      }
      //add new transition to list, from det source, using event i, det target
      newtrans.add(new int[] {source, i, target});
      //set event i in possible to true
      mPossible[i] = true;
    }
    return result;
  }

  public boolean exploreSubsetConstruction(final DeterministicState state, final boolean forbidden)
    throws OverflowException
  {
    //min/max variables
    int min, max;
    if (forbidden) {
    //if forbidden true, min = the size of hidden events
      min = mHide.size();
      //max = size hidden and forbidden events
      max = mHide.size() + mForbidden.size();
    } else {
      //min  = mHide.size() + mForbidden.size()
      min = mHide.size() + mForbidden.size();
      //max = all events
      max = events.length;
    }
    boolean result = false;
    final ProductDESProxyFactory factory = getFactory();
    //System.out.println("state:" + Arrays.toString(state));
    //create state proxy source for new state
    final StateProxy source = states.getState(state).getProxy();
    //for each event between min and max
    for (int i = min; i < max; i++) {
      //list of successor states
      DeterministicState successor = new DeterministicState(state.size());
      //for each state
      for (int j = 0; j < state.size(); j++) {
        //target state given automaton, event i and source state
        final int s = mTransitions[0][i][state.getState(j)];
        //if source state does not use event to get to target state ignore next step
        if (s == -1) {
          continue;
        }
        //add target state to successor states
        successor.insert(j, s);
      }
      //if successor is empty
      //System.out.println("successor:" + successor);
      if (successor.size() == 0) {
        continue;
      }
      //found result
      result = true;
      //get actual states involved in succ states
      successor = actualState(successor);
      //create target stateProxy
      StateProxy target = states.getState(successor).getProxy();
      //if target is null
      if (target == null) {
        //target = new MemStateProxy given name
        successor.setProxy(numStates);
        target = successor.getProxy();
        //add array to map with value target
        states.insert(numStates, successor);
        //increment number states
        numStates++;
        if (numStates > mNodeLimit) {
          throw new OverflowException(mNodeLimit);
        }
        //add successor state to unmarked bag
        unvisited.offer(successor);
      }
      //create transition in det automaton from source using event i, to target
      trans.add(factory.createTransitionProxy(source, events[i], target));
    }
    return result;
  }

  //t_closure
  public DeterministicState actualState(final DeterministicState state)
  {
    //number hidden events
    final int numHidden = mHide.size();
    //sorted set of states
    final DeterministicState setofstates = new DeterministicState(state.size());
    //bag of next states
    final IntBag nextstate = new IntBag(100);
    //for each non-det state in state
    for (int i = 0; i < state.size(); i++) {
      //if current state is not in the bag, add it
      if (setofstates.insert(i, state.getState(i))) {
        nextstate.offer(state.getState(i));
      }
    }
    // find out what states can be reached from state with hidden events
    while (!nextstate.isEmpty()) {
      //take non-det state s from bag
      final int s = nextstate.take();
      //for each hidden event
      for (int i = 0; i < numHidden; i++) {
        //target state of current automaton, using event i, and with source state s
        final int newstate = mTransitions[0][i][s];
        // if source state does not have event going to target ignore
        if (newstate == -1) {
          continue;
        }
        //if target state is not in the bag, add it
        if (setofstates.insert(i, newstate)) {
          nextstate.offer(newstate);
        }
      }
    }
    //resulting state after hidden events worked out
    final DeterministicState result = setofstates.copy();
    return result;
  }

  private static interface Bag
  {
    /** is the bag empty*/
    public boolean isEmpty();

    /** add a new item to the bag */
    public void offer(DeterministicState a);

    /** remove an arbitrary item from the bag */
    public DeterministicState take();
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

  private static class DeterministicBag
    implements Bag
  {
    private final int mInitialSize;
    private final CollectionDeterministic mValues;

    public DeterministicBag(final int initialSize)
    {
      mInitialSize = initialSize;
      mValues = new CollectionDeterministic(mInitialSize);
    }

    public boolean isEmpty()
    {
      return mValues.getSize() == 0;
    }

    public void offer(final DeterministicState a)
    {
      mValues.insert(mValues.getSize(), a);
    }

    public DeterministicState take()
    {
      final DeterministicState a = mValues.takeState();
      return a;
    }
  }

  /**
   *  I'll make this encode it properly later on
   *
   */
  private DeterministicState encode(final DeterministicState sState)
  {
    return sState;
  }

  private DeterministicState decode(final DeterministicState sState)
  {
    return sState;
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
  private final Set<EventProxy> mHide;
  private final Set<EventProxy> mForbidden;
  private final Set<EventProxy> mDisabled;
  private CollectionDeterministic states;
  private Collection<TransitionProxy> trans;
  private EventProxy[] events;
  private int[][][] mTransitions;
  private final List<int[]> newtrans = new ArrayList<int[]>();
  private int numStates;
  private Bag unvisited;
  private int[][] eventAutomaton;
}
