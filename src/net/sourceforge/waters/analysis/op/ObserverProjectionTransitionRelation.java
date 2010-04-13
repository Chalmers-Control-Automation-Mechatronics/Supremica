//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   ObserverProjectionTransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class ObserverProjectionTransitionRelation
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new transition relation from the given automaton.
   */
  public ObserverProjectionTransitionRelation(final AutomatonProxy aut)
  {
    this(aut, null);
  }

  /**
   * Creates a new transition relation from the given automaton.
   * @param allProps    The propositions to be used. If non-null, only
   *                    propositions in this collection will be included in
   *                    the new transition relation. If null, all propositions
   *                    of the automaton will be used.
   */
  public ObserverProjectionTransitionRelation
    (final AutomatonProxy aut, final Collection<EventProxy> allProps)
  {
    this(aut, null, allProps);
  }

  /**
   * Creates a new transition relation from the given automaton.
   * @param extraEvents Additional events to be added to the transition
   *                    relation. Any events in this collection will be
   *                    added to the event alphabet of the new transition
   *                    relation, but without any transitions associated
   *                    to them.
   * @param allProps    The propositions to be used. If non-null, only
   *                    propositions in this collection will be included in
   *                    the new transition relation. If null, all propositions
   *                    of the automaton will be used.
   */
  public ObserverProjectionTransitionRelation
    (final AutomatonProxy aut,
     final Collection<EventProxy> extraEvents,
     final Collection<EventProxy> allProps)
  {
    mName = aut.getName();
    mKind = aut.getKind();
    mNumProperEvents = 0;
    int numPropositions = 0;
    Collection<EventProxy> events = aut.getEvents();
    if (extraEvents != null) {
      final int size = events.size() + extraEvents.size();
      final Collection<EventProxy> autEvents = new THashSet<EventProxy>(events);
      final Collection<EventProxy> allEvents = new ArrayList<EventProxy>(size);
      allEvents.addAll(events);
      for (final EventProxy event : extraEvents) {
        if (autEvents.add(event)) {
          allEvents.add(event);
        }
      }
      events = allEvents;
    }
    for (final EventProxy event : events) {
      if (event.getKind() != EventKind.PROPOSITION) {
        mNumProperEvents++;
      } else if (allProps == null || allProps.contains(event)) {
        numPropositions++;
      }
    }
    final int numEvents = mNumProperEvents + numPropositions;
    mEvents = new EventProxy[numEvents];
    mEventToInt = new TObjectIntHashMap<EventProxy>(numEvents);
    int ee = 0;
    int pp = mNumProperEvents;
    for (final EventProxy event : events) {
      if (event.getKind() != EventKind.PROPOSITION) {
        mEvents[ee] = event;
        mEventToInt.put(event, ee++);
      } else if (allProps == null || allProps.contains(event)) {
        mEvents[pp] = event;
        mEventToInt.put(event, pp++);
      }
    }

    final Collection<StateProxy> states = aut.getStates();
    final int numStates = states.size();
    final TObjectIntHashMap<StateProxy> stateToInt =
        new TObjectIntHashMap<StateProxy>(numStates);
    mOriginalStates = new StateProxy[numStates];
    mOriginalStatesMap = new HashMap<StateProxy,Integer>(numStates);
    mIsInitial = new boolean[numStates];
    mStateMarkings = new int[numStates];
    mMarkingDefinitions = new ArrayList<TIntHashSet>();
    mMarkingMap = new TObjectIntHashMap<TIntHashSet>();
    mActiveEvents = new TIntHashSet[numStates];
    mSuccessors = new TIntHashSet[numStates][mNumProperEvents];
    mPredecessors = new TIntHashSet[numStates][mNumProperEvents];

    int statecode = 0;
    for (final StateProxy state : aut.getStates()) {
      stateToInt.put(state, statecode);
      mOriginalStates[statecode] = state;
      mOriginalStatesMap.put(state, statecode);
      mIsInitial[statecode] = state.isInitial();
      final Collection<EventProxy> props = state.getPropositions();
      final int numprops = props.size();
      final TIntHashSet markings = new TIntHashSet(numprops);
      for (final EventProxy prop : props) {
        final int e = mEventToInt.get(prop);
        markings.add(e);
      }
      markState(statecode, markings);
      statecode++;
    }

    for (final TransitionProxy trans : aut.getTransitions()) {
      final int s = stateToInt.get(trans.getSource());
      final int t = stateToInt.get(trans.getTarget());
      final int e = mEventToInt.get(trans.getEvent());
      final TIntHashSet succ = getFromArray(s, e, mSuccessors);
      succ.add(t);
      final TIntHashSet pred = getFromArray(t, e, mPredecessors);
      pred.add(s);
      final TIntHashSet active = getFromArray(s, mActiveEvents);
      active.add(e);
    }
  }

  // #########################################################################
  // # Simple Access
  public String getName()
  {
    return mName;
  }

  public ComponentKind getKind()
  {
    return mKind;
  }

  // #########################################################################
  // # Events Access
  /**
   * Gets the number of non-proposition events used by this transition relation.
   */
  public int getNumberOfProperEvents()
  {
    return mNumProperEvents;
  }

  /**
   * Gets the total number of events (including propositions) used by this
   * transition relation.
   */
  public int getNumberOfEvents()
  {
    return mEvents.length;
  }

  public EventProxy getEvent(final int event)
  {
    return mEvents[event];
  }

  public int getEventInt(final EventProxy event)
  {
    if (mEventToInt.containsKey(event)) {
      return mEventToInt.get(event);
    } else {
      return -1;
    }
  }

  // #########################################################################
  // # States Access
  public int getNumberOfStates()
  {
    return mSuccessors.length;
  }

  public int getNumberOfReachableStates()
  {
    int count = 0;
    for (int s = 0; s < mSuccessors.length; s++) {
      if (hasPredecessors(s)) {
        count++;
      }
    }
    return count;
  }

  public int getStateInt(final StateProxy state)
  {
    return mOriginalStatesMap.get(state);
  }

  public StateProxy[] getOriginalIntToStateMap()
  {
    return mOriginalStates;
  }

  public Map<StateProxy,Integer> getOriginalStateToIntMap()
  {
    return mOriginalStatesMap;
  }

  public TObjectIntHashMap<StateProxy> getResultingStateToIntMap()
  {
    return mResultingStates;
  }

  public boolean isInitial(final int state)
  {
    return mIsInitial[state];
  }

  // Should this list be pre-calculated?
  public TIntArrayList getAllInitialStates()
  {
    final TIntArrayList result = new TIntArrayList();
    for (int state = 0; state < mIsInitial.length; state++) {
      if (mIsInitial[state]) {
        result.add(state);
      }
    }
    return result;
  }

  public int getMarkingsInt(final int state)
  {
    return mStateMarkings[state];
  }

  public TIntHashSet getMarkings(final int state)
  {
    final int m = mStateMarkings[state];
    return mMarkingDefinitions.get(m);
  }

  public boolean isMarked(final int state, final int prop)
  {
    final TIntHashSet markings = getMarkings(state);
    return markings.contains(prop);
  }

  // #########################################################################
  // # State Modifications
  public void makeInitialState(final int state, final boolean initial)
  {
    mIsInitial[state] = initial;
  }

  // #########################################################################
  // # Marking Modifications
  /**
   * Removes all markings from the given state.
   *
   * @param state
   *          ID of the state to be modified.
   */
  public void clearMarkings(final int state)
  {
    final TIntHashSet empty = new TIntHashSet(0);
    markState(state, empty);
  }

  /**
   * Changes a particular marking for a the given state.
   *
   * @param state
   *          ID of the state to be modified.
   * @param prop
   *          ID of proposition identifying the marking to be modified.
   * @param value
   *          Whether the marking should be set (<CODE>true</CODE>) or cleared (
   *          <CODE>false</CODE>) for the given state and proposition.
   */
  public void markState(final int state, final int prop, final boolean value)
  {
    final int m = mStateMarkings[state];
    final TIntHashSet markings = mMarkingDefinitions.get(m);
    if (value != markings.contains(prop)) {
      final int size = markings.size();
      final TIntHashSet newset = new TIntHashSet(size);
      final TIntIterator iter = markings.iterator();
      if (value) {
        while (iter.hasNext()) {
          final int e = iter.next();
          newset.add(e);
        }
        newset.add(prop);
      } else {
        while (iter.hasNext()) {
          final int e = iter.next();
          if (e != prop) {
            newset.add(e);
          }
        }
      }
      markState(state, newset);
    }
  }

  /**
   * Replaces all markings for a given state.
   *
   * @param state
   *          ID of the state to be modified.
   * @param markings
   *          The new markings to be set for the state. The state will be marked
   *          with precisely the propositions whose IDs are in this set.
   */
  public void markState(final int state, final TIntHashSet markings)
  {
    final int m;
    if (mMarkingMap.contains(markings)) {
      m = mMarkingMap.get(markings);
    } else {
      m = mMarkingDefinitions.size();
      mMarkingDefinitions.add(markings);
      mMarkingMap.put(markings, m);
    }
    mStateMarkings[state] = m;
  }

  /**
   * Copies markings from one state to another. This methods add all the
   * markings of the given source state (from) to the given target state (to).
   * The markings of the source state will not be changed, and the target state
   * retains any markings it previously had in addition to the new ones.
   *
   * @param from
   *          ID of source state to copy markings from.
   * @param to
   *          ID of target state to copy markings to.
   */
  public void copyMarkings(final int from, final int to)
  {
    final TIntHashSet fromSet = getMarkings(from);
    final TIntHashSet toSet = getMarkings(to);
    if (fromSet != toSet) {
      boolean containsAll = true;
      TIntIterator iter = fromSet.iterator();
      while (iter.hasNext()) {
        final int e = iter.next();
        if (!toSet.contains(e)) {
          containsAll = false;
          break;
        }
      }
      if (!containsAll) {
        final int size = fromSet.size() + toSet.size();
        final TIntHashSet newset = new TIntHashSet(size);
        iter = fromSet.iterator();
        while (iter.hasNext()) {
          final int e = iter.next();
          newset.add(e);
        }
        iter = toSet.iterator();
        while (iter.hasNext()) {
          final int e = iter.next();
          newset.add(e);
        }
        markState(to, newset);
      }
    }
  }

  /**
   * Adds the given proposition to the event alphabet of this transition
   * relation.
   *
   * @param prop
   *          The event to be added.
   * @param markStates
   *          A flag. If <CODE>true</CODE> all states will be marked with the
   *          new proposition. If <CODE>false</CODE>, no states will be marked.
   * @return The event ID given to the new proposition.
   */
  public int addProposition(final EventProxy prop, final boolean markStates)
  {
    final int propID = mEvents.length;
    final int newNumEvents = propID + 1;
    mEvents = Arrays.copyOf(mEvents, newNumEvents);
    mEvents[propID] = prop;
    mEventToInt.put(prop, propID);
    for (final TIntHashSet set : mMarkingDefinitions) {
      set.add(propID);
    }
    return propID;
  }


  // #########################################################################
  // # Transitions Access
  public int getNumberOfTransitions()
  {
    int count = 0;
    for (int s = 0; s < mSuccessors.length; s++) {
      if (hasPredecessors(s)) {
        for (final TIntHashSet succ : getAllSuccessors(s)) {
          if (succ != null) {
            count += succ.size();
          }
        }
      }
    }
    return count;
  }

  public TIntHashSet getActiveEvents(final int state)
  {
    return mActiveEvents[state];
  }

  public TIntHashSet[] getAllSuccessors(final int state)
  {
    return mSuccessors[state];
  }

  public TIntHashSet getSuccessors(final int state, final int event)
  {
    return mSuccessors[state][event];
  }

  public TIntHashSet[] getAllPredecessors(final int state)
  {
    return mPredecessors[state];
  }

  public TIntHashSet getPredecessors(final int state, final int event)
  {
    return mPredecessors[state][event];
  }

  public boolean hasPredecessors(final int state)
  {
    if (isInitial(state)) {
      return true;
    }
    if (mPredecessors[state] != null) {
      for (int e = 0; e < mPredecessors[state].length; e++) {
        final TIntHashSet preds = mPredecessors[state][e];
        if (preds != null && !preds.isEmpty()) {
          return true;
        }
      }
    }
    return false;
  }

  // #########################################################################
  // # Event Distinction
  /**
   * Determines whether the given event is globally disabled in this transition
   * relation.
   *
   * @param event
   *          The ID of the event to be tested.
   * @return <CODE>true</CODE> if the given event is disabled in every state.
   */
  public boolean isGloballyDisabled(final int event)
  {
    final int numStates = getNumberOfStates();
    for (int state = 0; state < numStates; state++) {
      if (hasPredecessors(state)) {
        final TIntHashSet succs = mSuccessors[state][event];
        if (succs != null) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Determines whether the given event is selflooped in this transition
   * relation.
   *
   * @param event
   *          The ID of the event to be tested.
   * @return <CODE>true</CODE> if the given event is selflooped in every state,
   *         and appears on no other transitions.
   */
  public boolean isPureSelfloopEvent(final int event)
  {
    final int numStates = getNumberOfStates();
    for (int state = 0; state < numStates; state++) {
      if (hasPredecessors(state)) {
        final TIntHashSet succs = mSuccessors[state][event];
        if (succs == null || succs.size() > 1 || !succs.contains(state)) {
          return false;
        }
      }
    }
    return true;
  }

  // #########################################################################
  // # Transitions Modifications
  /**
   * Removes the given event from this transition relation.
   * This method removes the given event including all its transitions
   * from the transition relation.
   * @param  event   The ID of the event to be removed.
   */
  public void removeEvent(final int event)
  {
    mEvents[event] = null;
    for (int state = 0; state < mSuccessors.length; state++) {
      final TIntHashSet succ = mSuccessors[state][event];
      if (succ != null) {
        mSuccessors[state][event] = null;
        final TIntHashSet active = getFromArray(state, mActiveEvents);
        active.remove(event);
      }
      mPredecessors[state][event] = null;
    }
  }

  /**
   * Replaces an event by another.
   * This method replaces all transitions with the given old event ID
   * by transitions with the given new event ID. Both events must be
   * present in the transition relation, and will remain present after
   * this operation.
   * @param  oldID   The ID of the old event to be replaced.
   * @param  newID   The ID of the new event replacing the old event.
   */
  public void replaceEvent(final int oldID, final int newID)
  {
    for (int state = 0; state < mSuccessors.length; state++) {
      if (hasPredecessors(state)) {
        final TIntHashSet successors = getSuccessors(state, oldID);
        if (successors != null) {
          final int[] succarray = successors.toArray();
          for (final int succ : succarray) {
            removeTransition(state, oldID, succ);
            addTransition(state, newID, succ);
          }
        }
      }
    }
  }

  public int mergeEvents(final Collection<EventProxy> events)
  {
    final Iterator<EventProxy> it = events.iterator();
    final EventProxy first = it.next();
    final int f = mEventToInt.get(first);
    while (it.hasNext()) {
      final int next = mEventToInt.get(it.next());
      for (int s = 0; s < mNumProperEvents; s++) {
        TIntHashSet toremove = mSuccessors[s][next];
        if (toremove != null) {
          final TIntHashSet tau = getFromArray(s, f, mSuccessors);
          tau.addAll(toremove.toArray());
          mSuccessors[s][next] = null;
        }
        toremove = mPredecessors[s][next];
        if (toremove != null) {
          final TIntHashSet tau = getFromArray(s, f, mPredecessors);
          tau.addAll(toremove.toArray());
          mPredecessors[s][next] = null;
        }
      }
      mEvents[next] = null;
    }
    return f;
  }

  public void removeOutgoing(final int state, final int event)
  {
    final TIntHashSet succs = mSuccessors[state][event];
    if (succs == null) {
      return;
    }
    final int[] arr = succs.toArray();
    for (int i = 0; i < arr.length; i++) {
      final int suc = arr[i];
      removeTransition(state, event, suc);
    }
  }

  public void removeAllIncoming(final int s)
  {
    clearMarkings(s);
    for (int e = 0; e < mNumProperEvents; e++) {
      final TIntHashSet preds = mPredecessors[s][e];
      if (preds == null) {
        continue;
      }
      final int[] arpreds = preds.toArray();
      for (int i = 0; i < arpreds.length; i++) {
        final int pred = arpreds[i];
        removeTransition(pred, e, s);
      }
    }
  }

  public void moveAllSuccessors(final int from, final int to)
  {
    if (from == to) {
      return;
    }
    copyMarkings(from, to);
    clearMarkings(from);
    for (int e = 0; e < mNumProperEvents; e++) {
      final TIntHashSet succs = mSuccessors[from][e];
      if (succs == null) {
        continue;
      }
      final int[] arsuccs = succs.toArray();
      for (int i = 0; i < arsuccs.length; i++) {
        final int succ = arsuccs[i];
        removeTransition(from, e, succ);
        addTransition(to, e, succ);
      }
    }
  }

  public void addAllSuccessors(final int from, final int to)
  {
    if (from == to) {
      return;
    }
    copyMarkings(from, to);
    for (int e = 0; e < mNumProperEvents; e++) {
      final TIntHashSet succs = mSuccessors[from][e];
      if (succs == null) {
        continue;
      }
      final int[] arsuccs = succs.toArray();
      for (int i = 0; i < arsuccs.length; i++) {
        final int succ = arsuccs[i];
        addTransition(to, e, succ);
      }
    }
  }

  public void moveAllPredeccessors(final int from, final int to)
  {
    if (from == to) {
      return;
    }
    makeInitialState(to, mIsInitial[to] || mIsInitial[from]);
    makeInitialState(from, false);
    for (int e = 0; e < mNumProperEvents; e++) {
      final TIntHashSet preds = mPredecessors[from][e];
      if (preds != null) {
        final int[] arpreds = preds.toArray();
        for (int i = 0; i < arpreds.length; i++) {
          final int pred = arpreds[i];
          removeTransition(pred, e, from);
          addTransition(pred, e, to);
        }
      }
    }
  }

  public void addAllPredeccessors(final int from, final int to)
  {
    if (from == to) {
      return;
    }
    makeInitialState(to, mIsInitial[to] || mIsInitial[from]);
    for (int e = 0; e < mNumProperEvents; e++) {
      final TIntHashSet preds = mPredecessors[from][e];
      if (preds != null) {
        final int[] arpreds = preds.toArray();
        for (int i = 0; i < arpreds.length; i++) {
          final int pred = arpreds[i];
          addTransition(pred, e, to);
        }
      }
    }
  }

  public boolean removeAllSelfLoops(final int e)
  {
    boolean modified = false;
    for (int s = 0; s < mSuccessors.length; s++) {
      final TIntHashSet succs = mSuccessors[s][e];
      if (succs == null) {
        continue;
      }
      if (succs.contains(s)) {
        removeTransition(s, e, s);
        modified = true;
      }
    }
    return modified;
  }

  public void removeTransition(final int s, final int e, final int t)
  {
    final TIntHashSet succ = getFromArray(s, e, mSuccessors);
    final TIntHashSet pred = getFromArray(t, e, mPredecessors);
    succ.remove(t);
    pred.remove(s);
    if (succ.isEmpty()) {
      mSuccessors[s][e] = null;
      final TIntHashSet active = getFromArray(s, mActiveEvents);
      active.remove(e);
    }
    if (pred.isEmpty()) {
      mPredecessors[t][e] = null;
    }
  }

  public void removeAllOutgoing(final int s)
  {
    clearMarkings(s);
    for (int e = 0; e < mNumProperEvents; e++) {
      final TIntHashSet succs = mSuccessors[s][e];
      if (succs == null) {
        continue;
      }
      final int[] arsuccs = succs.toArray();
      for (int i = 0; i < arsuccs.length; i++) {
        final int succ = arsuccs[i];
        removeTransition(s, e, succ);
      }
    }
  }

  public void addTransition(final int s, final int e, final int t)
  {
    final TIntHashSet succ = getFromArray(s, e, mSuccessors);
    final TIntHashSet pred = getFromArray(t, e, mPredecessors);
    succ.add(t);
    pred.add(s);
    final TIntHashSet active = getFromArray(s, mActiveEvents);
    active.add(e);
  }

  public void merge(final int[] statesToMerge)
  {
    final int to = statesToMerge[0];
    for (int i = 1; i < statesToMerge.length; i++) {
      final int from = statesToMerge[i];
      moveAllSuccessors(from, to);
      moveAllPredeccessors(from, to);
    }
  }

  public void merge(final int[] statesToMerge, final int tau)
  {
    final int to = statesToMerge[0];
    for (int i = 1; i < statesToMerge.length; i++) {
      final int from = statesToMerge[i];
      moveAllSuccessors(from, to, tau);
      moveAllPredeccessors(from, to, tau);
    }
  }

  // #########################################################################
  // # Automaton Simplification
  /**
   * Attempts to simplify the automaton by removing redundant selfloop events.
   * This method searches for any events that are selflooped in all states of
   * the transition relation, and removes any such events and the selfloops from
   * the transition relation.
   *
   * @param tau
   *          The ID of a silent event. If this is an event of the transition
   *          relation, it is treated specially. It can also be removed if it is
   *          disabled in all states.
   */
  public void removeSelfLoopEvents(final int tau)
  {
    if (tau >= 0 && isGloballyDisabled(tau)) {
      removeEvent(tau);
    }
    for (int e = 0; e < getNumberOfProperEvents(); e++) {
      if (isPureSelfloopEvent(e)) {
        removeEvent(e);
      }
    }
  }

  /**
   * Repartitions the states of this transition relation. This method is used to
   * merge states after a partition has been obtained through a
   * {@link TransitionRelationSimplifier}.
   *
   * @param partition
   *          The partitioning to be imposed. Each array in the collection
   *          defines the state codes comprising an equivalence class to be
   *          merged into a single state. The first element of each array
   *          identifies the state code to be given to the new merged state.
   * @param tau
   *          The event code of a silent event. If the event is present in the
   *          transition relations, any selfloops with this events obtained
   *          while merging states will be deleted.
   * @param classMap
   *          If non-null, this map will be augmented with the reverse state map
   *          resulting from the merge. Each merged state code will be mapped to
   *          the array of state code present before the merge.
   */
  public void mergePartition(final Collection<int[]> partition, final int tau,
                             final TIntObjectHashMap<int[]> classMap)
  {
    for (final int[] array : partition) {
      final int key = array[0];
      for (int i = 1; i < array.length; i++) {
        final int state = array[i];
        moveAllSuccessors(state, key, tau);
        moveAllPredeccessors(state, key, tau);
      }
      if (classMap != null) {
        classMap.put(key, array);
      }
    }
  }

  // #########################################################################
  // # Automaton Output
  public AutomatonProxy createAutomaton(final ProductDESProxyFactory factory)
  {
    final int numEvents = getNumberOfEvents();
    final int numStates = getNumberOfStates();
    final Collection<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    for (int e = 0; e < mEvents.length; e++) {
      if (mEvents[e] != null) {
        events.add(mEvents[e]);
      }
    }

    final List<MemStateProxy> reachable =
        new ArrayList<MemStateProxy>(numStates);
    mResultingStates = new TObjectIntHashMap<StateProxy>(numStates);
    final StateProxy[] outputMap = new StateProxy[numStates];
    final Collection<TransitionProxy> transitions =
        new ArrayList<TransitionProxy>();
    for (int s = 0; s < numStates; s++) {
      if (hasPredecessors(s)) {
        final boolean init = isInitial(s);
        final TIntHashSet markings = getMarkings(s);
        final int numprops = markings.size();
        final Collection<EventProxy> props =
            new ArrayList<EventProxy>(numprops);
        final TIntIterator iter = markings.iterator();
        while (iter.hasNext()) {
          final int e = iter.next();
          final EventProxy prop = mEvents[e];
          props.add(prop);
        }
        final MemStateProxy state = new MemStateProxy(s, init, props);
        reachable.add(state);
        mResultingStates.put(state, s);
        outputMap[s] = state;
      }
    }
    for (final MemStateProxy source : reachable) {
      final int s = source.getCode();
      for (int e = 0; e < mNumProperEvents; e++) {
        final TIntHashSet succs = mSuccessors[s][e];
        if (succs == null) {
          continue;
        }
        final EventProxy event = mEvents[e];
        final TIntIterator iter = succs.iterator();
        while (iter.hasNext()) {
          final int succ = iter.next();
          final StateProxy target = outputMap[succ];
          final TransitionProxy trans =
              factory.createTransitionProxy(source, event, target);
          transitions.add(trans);
        }
      }
    }
    return factory.createAutomatonProxy(mName, mKind, events, reachable,
                                        transitions);
  }

  // #########################################################################
  // # Auxiliary Methods
  private void moveAllSuccessors(final int from, final int to,
                                 final int suppress)
  {
    if (from == to) {
      return;
    }
    copyMarkings(from, to);
    clearMarkings(from);
    for (int e = 0; e < mNumProperEvents; e++) {
      final TIntHashSet succs = mSuccessors[from][e];
      if (succs == null) {
        continue;
      }
      final int[] arsuccs = succs.toArray();
      for (int i = 0; i < arsuccs.length; i++) {
        final int succ = arsuccs[i];
        removeTransition(from, e, succ);
        if (e != suppress || to != succ) {
          addTransition(to, e, succ);
        }
      }
    }
  }

  private void moveAllPredeccessors(final int from, final int to,
                                    final int suppress)
  {
    if (from == to) {
      return;
    }
    makeInitialState(to, mIsInitial[to] || mIsInitial[from]);
    makeInitialState(from, false);
    for (int e = 0; e < mNumProperEvents; e++) {
      final TIntHashSet preds = mPredecessors[from][e];
      if (preds != null) {
        final int[] arpreds = preds.toArray();
        for (int i = 0; i < arpreds.length; i++) {
          final int pred = arpreds[i];
          removeTransition(pred, e, from);
          if (e != suppress || pred != to) {
            addTransition(pred, e, to);
          }
        }
      }
    }
  }

  private TIntHashSet getFromArray(final int i, final TIntHashSet[] array)
  {
    TIntHashSet intset = array[i];
    if (intset == null) {
      intset = new TIntHashSet();
      array[i] = intset;
    }
    return intset;
  }

  private TIntHashSet getFromArray(final int i, final int j,
                                   final TIntHashSet[][] array)
  {
    TIntHashSet intset = array[i][j];
    if (intset == null) {
      intset = new TIntHashSet();
      array[i][j] = intset;
    }
    return intset;
  }


  // #########################################################################
  // # Inner Class MemStateProxy
  /**
   * Stores states, encoding the name as an int rather than a long string value.
   */
  private static class MemStateProxy implements StateProxy
  {
    // #######################################################################
    // # Constructor
    private MemStateProxy(final int code, final boolean init,
                          final Collection<EventProxy> props)
    {
      mCode = code;
      mIsInitial = init;
      mProps = props;
    }

    // #######################################################################
    // # Simple Access
    int getCode()
    {
      return mCode;
    }

    // #######################################################################
    // # Interface net.sourceforge.waters.model.des.StateProxy
    public String getName()
    {
      return "S:" + mCode;
    }

    public boolean isInitial()
    {
      return mIsInitial;
    }

    public Collection<EventProxy> getPropositions()
    {
      return mProps;
    }

    public MemStateProxy clone()
    {
      return new MemStateProxy(mCode, mIsInitial, mProps);
    }

    public boolean refequals(final NamedProxy o)
    {
      if (o instanceof MemStateProxy) {
        final MemStateProxy s = (MemStateProxy) o;
        return s.mCode == mCode;
      } else {
        return false;
      }
    }

    public int refHashCode()
    {
      return mCode;
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

    // #######################################################################
    // # Overrides for java.lang.Object
    public String toString()
    {
      return getName();
    }

    // #######################################################################
    // # Data Members
    private final int mCode;
    private final boolean mIsInitial;
    private final Collection<EventProxy> mProps;
  }


  // #########################################################################
  // # Data Members
  private final String mName;
  private final ComponentKind mKind;
  private int mNumProperEvents;
  private EventProxy[] mEvents;
  private final TObjectIntHashMap<EventProxy> mEventToInt;
  private final StateProxy[] mOriginalStates;
  private final Map<StateProxy,Integer> mOriginalStatesMap;
  private final boolean[] mIsInitial;
  private final int[] mStateMarkings;
  private final List<TIntHashSet> mMarkingDefinitions;
  private final TObjectIntHashMap<TIntHashSet> mMarkingMap;
  private final TIntHashSet[] mActiveEvents;
  private final TIntHashSet[][] mSuccessors;
  private final TIntHashSet[][] mPredecessors;

  private TObjectIntHashMap<StateProxy> mResultingStates;

}
