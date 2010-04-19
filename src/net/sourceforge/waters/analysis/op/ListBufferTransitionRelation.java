//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   ListBufferTransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TLongObjectHashMap;
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;


public class ListBufferTransitionRelation
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new transition relation from the given automaton.
   */
  public ListBufferTransitionRelation(final AutomatonProxy aut)
    throws OverflowException
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
  public ListBufferTransitionRelation
    (final AutomatonProxy aut, final Collection<EventProxy> allProps)
    throws OverflowException
  {
    mName = aut.getName();
    mKind = aut.getKind();

    final Collection<EventProxy> events = aut.getEvents();
    final int numEvents = events.size();
    mEvents = new ArrayList<EventProxy>(numEvents);
    mPropositions = new ArrayList<EventProxy>(numEvents);
    mEventsMap = new TObjectIntHashMap<EventProxy>(numEvents);
    for (final EventProxy event : events) {
      switch (event.getKind()) {
      case CONTROLLABLE:
      case UNCONTROLLABLE:
        final int e = mEvents.size();
        mEventsMap.put(event, e);
        mEvents.add(event);
        break;
      case PROPOSITION:
        if (allProps == null || allProps.contains(event)) {
          final int p = mPropositions.size();
          mEventsMap.put(event, p);
          mPropositions.add(event);
        }
        break;
      default:
        break;
      }
    }

    final Collection<StateProxy> states = aut.getStates();
    final int numStates = states.size();
    final int numProps = mPropositions.size();
    mOriginalStates = new StateProxy[numStates];
    mOriginalStatesMap = new TObjectIntHashMap<StateProxy>(numStates);
    int s = 0;
    for (final StateProxy state : states) {
      mOriginalStates[s] = state;
      mOriginalStatesMap.put(state, s);
      s++;
    }
    mStateBuffer = new IntStateBuffer(states, mEventsMap, numProps);

    /*
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    mSuccessorBuffer =
      new TransitionListBuffer(transitions, mOriginalStatesMap, mEventToInt);
    */
  }


  //#########################################################################
  //# Simple Access
  public String getName()
  {
    return mName;
  }

  public ComponentKind getKind()
  {
    return mKind;
  }


  // #########################################################################
  // # Event Access
  /**
   * Gets the number of non-proposition events used by this transition relation.
   */
  public int getNumberOfProperEvents()
  {
    return mEvents.size();
  }

  /**
   * Gets the number of proposition events used by this transition relation.
   */
  public int getNumberOfPropositions()
  {
    return mPropositions.size();
  }

  /**
   * Gets the total number of events (including propositions) used by this
   * transition relation.
   */
  public int getNumberOfEvents()
  {
    return getNumberOfProperEvents() + getNumberOfPropositions();
  }

  /**
   * Gets the non-proposition event with the given ID.
   */
  public EventProxy getProperEvent(final int e)
  {
    return mEvents.get(e);
  }

  /**
   * Gets the proposition event with the given ID.
   */
  public EventProxy getProposition(final int e)
  {
    return mPropositions.get(e);
  }

  /**
   * Gets the event ID of the given event.
   * @param  event  The event to be examined.
   *                May be a proposition or a non-proposition event.
   * @return ID of event or proposition, or <CODE>-1</CODE> if the event
   *         does not appear in the transition relation.
   */
  public int getEventInt(final EventProxy event)
  {
    if (mEventsMap.containsKey(event)) {
      return mEventsMap.get(event);
    } else {
      return -1;
    }
  }


  //#########################################################################
  //# State Access
  public int getNumberOfStates()
  {
    return mOriginalStates.length;
  }

  public int getNumberOfReachableStates()
  {
    return mStateBuffer.getNumberOfReachableStates();
  }

  public int getStateInt(final StateProxy state)
  {
    return mOriginalStatesMap.get(state);
  }

  public StateProxy[] getOriginalIntToStateMap()
  {
    return mOriginalStates;
  }

  public TObjectIntHashMap<StateProxy> getOriginalStateToIntMap()
  {
    return mOriginalStatesMap;
  }

  public boolean isInitial(final int state)
  {
    return mStateBuffer.isInitial(state);
  }

  public void setInitial(final int state, final boolean init)
  {
    mStateBuffer.setInitial(state, init);
  }

  public boolean isReachable(final int state)
  {
    return mStateBuffer.isReachable(state);
  }

  public void setReachable(final int state, final boolean reachable)
  {
    mStateBuffer.setReachable(state, reachable);
  }


  //#########################################################################
  //# Markings Access
  public boolean isMarked(final int state, final int prop)
  {
    return mStateBuffer.isMarked(state, prop);
  }

  public long getAllMarkings(final int state)
  {
    return mStateBuffer.getAllMarkings(state);
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
  public void setMarked(final int state, final int prop, final boolean value)
  {
    mStateBuffer.setMarked(state, prop, value);
  }

  /**
   * Removes all markings from the given state.
   *
   * @param state
   *          ID of the state to be modified.
   */
  public void clearMarkings(final int state)
  {
    mStateBuffer.clearMarkings(state);
  }

  /**
   * Copies markings from one state to another. This methods add all the
   * markings of the given source state (from) to the given target state (to).
   * The markings of the source state will not be changed, and the target state
   * retains any markings it previously had in addition to the new ones.
   *
   * @param source
   *          ID of source state to copy markings from.
   * @param dest
   *          ID of target state to copy markings to.
   */
  public void copyMarkings(final int source, final int dest)
  {
    mStateBuffer.copyMarkings(source, dest);
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
   * @throws OverflowException if adding a proposition would exceed the
   *          the capacity of the underlying buffer.
   */
  public int addProposition(final EventProxy prop, final boolean markStates)
    throws OverflowException
  {
    final int code = mPropositions.size();
    mStateBuffer.addProposition(code, markStates);
    mPropositions.add(prop);
    return code;
  }


  //#########################################################################
  //# Transition Access
  /**
   * Gets the total number of transitions currently stored in this transition
   * relation. As the number of transitions is not stored, this method is of
   * linear complexity.
   */
  public int getNumberOfTransitions()
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.getNumberOfTransitions();
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.getNumberOfTransitions();
    } else {
      throw createNoBufferException();
    }
  }

  public TransitionIterator createSuccessorsIterator()
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createReadOnlyIterator();
    } else {
      throw createNoBufferException("successor");
    }
  }

  /**
   * Creates a read-only iterator for this transition relation that is set up
   * to iterate over the outgoing transitions associated with the given state.
   * The iterator returned produces all transitions associated with the given
   * state in the buffer's defined ordering, no matter what event they use.
   * Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   * @throws IllegalStateException if the transition relation is not
   *         configure to use an outgoing transition buffer.
   */
  public TransitionIterator createSuccessorsIterator(final int source)
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createReadOnlyIterator(source);
    } else {
      throw createNoBufferException("successor");
    }
  }

  /**
   * Creates a read-only iterator for this transition relation that is set up
   * to iterate over the outgoing transitions associated with the given state
   * and event. The iterator returned produces all transitions associated with
   * the given state and event in the buffer's defined ordering.
   * Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   * @throws IllegalStateException if the transition relation is not
   *         configure to use an outgoing transition buffer.
   */
  public TransitionIterator createSuccessorsIterator(final int source,
                                                     final int event)
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createReadOnlyIterator(source, event);
    } else {
      throw createNoBufferException("successor");
    }
  }

  /**
   * Creates a read-only iterator for this transition relation that is set up
   * to iterate over the incoming transitions associated with the given state.
   * The iterator returned produces all transitions associated with the given
   * state in the buffer's defined ordering, no matter what event they use.
   * Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   * @throws IllegalStateException if the transition relation is not
   *         configure to use an incoming transition buffer.
   */
  public TransitionIterator createPredecessorsIterator(final int target)
  {
    if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createReadOnlyIterator(target);
    } else {
      throw createNoBufferException("predecessor");
    }
  }

  /**
   * Creates a read-only iterator for this transition relation that is set up
   * to iterate over the incoming transitions associated with the given state
   * and event. The iterator returned produces all transitions associated with
   * the given state and event in the buffer's defined ordering.
   * Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   * @throws IllegalStateException if the transition relation is not
   *         configure to use an incoming transition buffer.
   */
  public TransitionIterator createPredecessorsIterator(final int target,
                                                       final int event)
  {
    if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createReadOnlyIterator(target, event);
    } else {
      throw createNoBufferException("predecessor");
    }
  }

  /**
   * Creates a read-only iterator over all transitions in this transition
   * relation.
   * The iterator returned is set up to return the first transition in
   * this buffer after calling {@link TransitionIterator#advance()}.
   * It does not implement the methods {@link TransitionIterator#reset(int)}
   * or {@link TransitionIterator#reset(int,int)}, and
   * being a read-only iterator, it also does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createAllTransitionsIterator()
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createAllTransitionsReadOnlyIterator();
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createAllTransitionsReadOnlyIterator();
    } else {
      throw createNoBufferException();
    }
  }

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
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.isGloballyDisabled(event);
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.isGloballyDisabled(event);
    } else {
      throw createNoBufferException();
    }
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
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.isPureSelfloopEvent(event);
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.isPureSelfloopEvent(event);
    } else {
      throw createNoBufferException();
    }
  }


  //#########################################################################
  //# Transition Modifications
  /**
   * Adds a transition to this transition relation. The new transition is
   * inserted in a defined ordering in the predecessor and/or successor
   * buffers.
   * @param  source The ID of the source state of the new transition.
   * @param  event  The ID of the event of the new transition.
   * @param  target The ID of the target state of the new transition.
   * @return <CODE>true</CODE> if a transition was added, i.e., if it was
   *         not already present in the buffer;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean addTransition(final int source,
                               final int event,
                               final int target)
  {
    boolean result = false;
    if (mSuccessorBuffer != null) {
      result = mSuccessorBuffer.addTransition(source, event, target);
    }
    if (mPredecessorBuffer != null) {
      result = mPredecessorBuffer.addTransition(target, event, source);
    }
    return result;
  }

  /**
   * Removes a transition from this transition relation.
   * @param  source The ID of the source state of the transition to be removed.
   * @param  event  The ID of the event of the transition to be removed.
   * @param  target The ID of the target state of the transition to be removed.
   * @return <CODE>true</CODE> if a transition was removed, i.e., if it was
   *         actually present in the buffer;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean removeTransition(final int source,
                                  final int event,
                                  final int target)
  {
    boolean result = false;
    if (mSuccessorBuffer != null) {
      result = mSuccessorBuffer.removeTransition(source, event, target);
    }
    if (mPredecessorBuffer != null) {
      result = mPredecessorBuffer.removeTransition(target, event, source);
    }
    return result;
  }

  /**
   * Removes all outgoing transitions associated with the given source
   * state.
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException if the transition relation is not
   *         configured to use an outgoing transition buffer.
   */
  public boolean removeOutgoingTransitions(final int source)
  {
    if (mSuccessorBuffer != null) {
      boolean remove = true;
      final TransitionIterator iter =
        mSuccessorBuffer.createModifyingIterator(source);
      while (iter.advance()) {
        if (mPredecessorBuffer != null) {
          final int event = iter.getCurrentEvent();
          final int target = iter.getCurrentToState();
          mPredecessorBuffer.removeTransition(target, event, source);
        }
        iter.remove();
        remove = true;
      }
      return remove;
    } else {
      throw createNoBufferException("successor");
    }
  }

  /**
   * Removes all outgoing transitions associated with the given source
   * state and event.
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException if the transition relation is not
   *         configured to use an outgoing transition buffer.
   */
  public boolean removeOutgoingTransitions(final int source, final int event)
  {
    if (mSuccessorBuffer != null) {
      boolean remove = true;
      final TransitionIterator iter =
        mSuccessorBuffer.createModifyingIterator(source, event);
      while (iter.advance()) {
        if (mPredecessorBuffer != null) {
          final int target = iter.getCurrentToState();
          mPredecessorBuffer.removeTransition(target, event, source);
        }
        iter.remove();
        remove = true;
      }
      return remove;
    } else {
      throw createNoBufferException("successor");
    }
  }


  /**
   * Removes all incoming transitions associated with the given target
   * state.
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException if the transition relation is not
   *         configured to use an incoming transition buffer.
   */
  public boolean removeIncomingTransitions(final int target)
  {
    if (mPredecessorBuffer != null) {
      boolean remove = true;
      final TransitionIterator iter =
        mPredecessorBuffer.createModifyingIterator(target);
      while (iter.advance()) {
        if (mSuccessorBuffer != null) {
          final int event = iter.getCurrentEvent();
          final int source = iter.getCurrentToState();
          mSuccessorBuffer.removeTransition(source, event, target);
        }
        iter.remove();
        remove = true;
      }
      return remove;
    } else {
      throw createNoBufferException("predecessor");
    }
  }

  /**
   * Removes all incoming transitions associated with the given target
   * state and event.
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException if the transition relation is not
   *         configured to use an incoming transition buffer.
   */
  public boolean removeIncomingTransitions(final int target, final int event)
  {
    if (mPredecessorBuffer != null) {
      boolean remove = true;
      final TransitionIterator iter =
        mPredecessorBuffer.createModifyingIterator(target, event);
      while (iter.advance()) {
        if (mSuccessorBuffer != null) {
          final int source = iter.getCurrentToState();
          mSuccessorBuffer.removeTransition(source, event, target);
        }
        iter.remove();
        remove = true;
      }
      return remove;
    } else {
      throw createNoBufferException("predecessor");
    }
  }

  /**
   * <P>
   * Copies all outgoing transitions from the given 'from' state to the given
   * 'to' state.
   * </P>
   * <P>
   * This method copies all markings and regular transitions from the 'from'
   * state to the 'to' state. It suppresses duplicates, and ordering is
   * preserved such that outgoing transitions originally associated with the
   * 'from' state appear earlier in the resultant list.
   * </P>
   * <P>
   * <STRONG>Warning.</STRONG> This method closes the incoming transition
   * buffer, if it is open.
   * </P>
   *
   * @param from
   *          ID of state containing transitions and markings to be copied.
   * @param to
   *          ID of state receiving transitions and markings.
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an outgoing
   *           transition buffer.
   */
  public void copyOutgoingTransitions(final int from, final int to)
  {
    if (from != to) {
      if (mSuccessorBuffer == null) {
        throw createNoBufferException("successor");
      }
      copyMarkings(from, to);
      mPredecessorBuffer = null;
      mSuccessorBuffer.copyTransitions(from, to);
    }
  }

  /**
   * <P>
   * Moves all outgoing transitions from the given 'from' state to the given
   * 'to' state.
   * </P>
   * <P>
   * This method copies all markings and regular transitions from the 'from'
   * state to the 'to' state. It suppresses duplicates, and ordering is
   * preserved such that outgoing transitions originally associated with the
   * 'from' state appear earlier in the resultant list. After copying, all
   * outgoing transitions and markings are removed from the 'from' state.
   * </P>
   * <P>
   * <STRONG>Warning.</STRONG> This method closes the incoming transition
   * buffer, if it is open.
   * </P>
   *
   * @param from
   *          ID of state containing transitions and markings to be moved.
   * @param to
   *          ID of state receiving transitions and markings.
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an outgoing
   *           transition buffer.
   */
  public void moveOutgoingTransitions(final int from, final int to)
  {
    copyOutgoingTransitions(from, to);
    removeOutgoingTransitions(to);
  }

  /**
   * <P>
   * Copies all incoming transitions from the given 'from' state to the given
   * 'to' state.
   * </P>
   * <P>
   * This method copies all regular transitions from the 'from' state to the
   * 'to' state. Furthermore, if the 'from' state is an initial state, the 'to'
   * state will be set to an initial state as well. This method suppresses
   * duplicates, and ordering is preserved such that incoming transitions
   * originally associated with the 'from' state appear earlier in the resultant
   * list.
   * </P>
   * <P>
   * <STRONG>Warning.</STRONG> This method closes the outgoing transition
   * buffer, if it is open.
   * </P>
   *
   * @param from
   *          ID of state containing transitions and initial state status to be
   *          copied.
   * @param to
   *          ID of state receiving transitions and initial state status.
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an incoming
   *           transition buffer.
   */
  public void copyIncomingTransitions(final int from, final int to)
  {
    if (from != to) {
      if (mPredecessorBuffer == null) {
        throw createNoBufferException("predecessor");
      }
      if (isInitial(from)) {
        setInitial(to, true);
      }
      mSuccessorBuffer = null;
      mPredecessorBuffer.copyTransitions(from, to);
    }
  }

  /**
   * <P>
   * Moves all incoming transitions from the given 'from' state to the given
   * 'to' state.
   * </P>
   * <P>
   * This method copies all regular transitions from the 'from' state to the
   * 'to' state. Furthermore, if the 'from' state is an initial state, the 'to'
   * state will be set to an initial state as well. This method suppresses
   * duplicates, and ordering is preserved such that incoming transitions
   * originally associated with the 'from' state appear earlier in the resultant
   * list. After copying, all incoming transitions are removed from the 'from'
   * state, which then is marked as unreachable.
   * </P>
   * <P>
   * <STRONG>Warning.</STRONG> This method closes the outgoing transition
   * buffer, if it is open.
   * </P>
   *
   * @param from
   *          ID of state containing transitions and initial state status to be
   *          moved.
   * @param to
   *          ID of state receiving transitions and initial state status.
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an incoming
   *           transition buffer.
   */
  public void moveIncomingTransitions(final int from, final int to)
  {
    copyIncomingTransitions(from, to);
    removeIncomingTransitions(to);
    setReachable(from, false);
  }

  /**
   * Removes the given event from this transition relation.
   * This method removes the given event including all its transitions
   * from the transition relation. The event's entry in the event list
   * is set to <CODE>null</CODE>, and all associated transitions are
   * deleted.
   * @param  event   The ID of the event to be removed.
   */
  public void removeEvent(final int event)
  {
    mEvents.set(event, null);
    if (mSuccessorBuffer != null) {
      mSuccessorBuffer.removeEventTransitions(event);
    }
    if (mPredecessorBuffer != null) {
      mPredecessorBuffer.removeEventTransitions(event);
    }
  }

  /**
   * Replaces an event by another.
   * This method replaces all transitions with the given old event ID
   * by transitions with the given new event ID. Both events must be
   * present in the transition relation, and will remain present after
   * this operation. Any new transitions with the new event ID are inserted
   * after any transitions already present in the transition buffers.
   * @param  old     The ID of the old event to be replaced.
   * @param  new     The ID of the new event replacing the old event.
   */
  public void replaceEvent(final int oldID, final int newID)
  {
    if (mSuccessorBuffer != null) {
      mSuccessorBuffer.replaceEvent(oldID, newID);
    }
    if (mPredecessorBuffer != null) {
      mPredecessorBuffer.replaceEvent(oldID, newID);
    }
  }


  //#########################################################################
  //# Automaton Simplification
  /**
   * Attempts to simplify the automaton by removing redundant selfloop events.
   * This method searches for any events that are selflooped in all states of
   * the transition relation, and removes any such events and the selfloops from
   * the transition relation.
   *
   * @param tau
   *          The ID of a silent event. If this is an event of the transition
   *          relation, it is treated specially. Any selfloops with this event
   *          are removed, and if this results in the event being disabled in
   *          all states, it is removed.
   */
  public void removeSelfLoopEvents(final int tau)
  {
    if (tau >= 0 && mEvents.get(tau) != null && isGloballyDisabled(tau)) {
      removeEvent(tau);
    }
    for (int e = 0; e < getNumberOfProperEvents(); e++) {
      if (mEvents.get(e) == null) {
        // skip ...
      } else if (e == tau) {
        boolean removable = false;
        if (mSuccessorBuffer != null) {
          removable = mSuccessorBuffer.removeTauSelfloops(tau);
        }
        if (mPredecessorBuffer != null) {
          removable = mPredecessorBuffer.removeTauSelfloops(tau);
        }
        if (removable) {
          mEvents.set(tau, null);
        }
      } else if (isPureSelfloopEvent(e)) {
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
   *          The partitioning to be imposed. Each array in the list
   *          defines the state codes comprising an equivalence class to be
   *          merged into a single state. The index position in the list
   *          identifies the state code to be given to the new merged state.
   * @param tau
   *          The event code of a silent event. If the event is present in the
   *          transition relations, any selfloops with this events obtained
   *          while merging states will be deleted.
   */
  public void merge(final List<int[]> partition, final int tau)
  {
    if (mSuccessorBuffer != null) {
      mSuccessorBuffer.merge(partition, tau);
    }
    if (mPredecessorBuffer != null) {
      mPredecessorBuffer.merge(partition, tau);
    }
  }


  //#########################################################################
  //# Automaton Output
  public AutomatonProxy createAutomaton
    (final ProductDESProxyFactory factory,
     final TObjectIntHashMap<StateProxy> outputMap)
  {
    final int numEvents = getNumberOfEvents();
    final Collection<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    for (final EventProxy event : mEvents) {
      if (event != null) {
        events.add(event);
      }
    }
    for (final EventProxy event : mPropositions) {
      if (event != null) {
        events.add(event);
      }
    }

    final int numProps = getNumberOfPropositions();
    final int numStates = getNumberOfStates();
    final List<MemStateProxy> reachable =
        new ArrayList<MemStateProxy>(numStates);
    final StateProxy[] outputArray = new StateProxy[numStates];
    final TLongObjectHashMap<Collection<EventProxy>> markingsMap =
      new TLongObjectHashMap<Collection<EventProxy>>();
    for (int s = 0; s < numStates; s++) {
      if (isReachable(s)) {
        final boolean init = isInitial(s);
        final long markings = mStateBuffer.getAllMarkings(s);
        Collection<EventProxy> props = markingsMap.get(markings);
        if (props == null) {
          props = new ArrayList<EventProxy>(numProps);
          for (int p = 0; p < numProps; p++) {
            if (isMarked(s, p)) {
              final EventProxy prop = mPropositions.get(p);
              props.add(prop);
            }
          }
          markingsMap.put(markings, props);
        }
        final MemStateProxy state = new MemStateProxy(s, init, props);
        reachable.add(state);
        outputArray[s] = state;
        if (outputMap != null) {
          outputMap.put(state, s);
        }
      }
    }

    final int numTrans = getNumberOfTransitions();
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(numTrans);
    final TransitionIterator iter = createAllTransitionsIterator();
    while (iter.advance()) {
      final int s = iter.getCurrentSourceState();
      final StateProxy source = outputArray[s];
      final int e = iter.getCurrentEvent();
      final EventProxy event = mEvents.get(e);
      final int t = iter.getCurrentSourceState();
      final StateProxy target = outputArray[t];
      final TransitionProxy trans =
        factory.createTransitionProxy(source, event, target);
      transitions.add(trans);
    }
    return factory.createAutomatonProxy(mName, mKind, events, reachable,
                                        transitions);
  }


  //#########################################################################
  //# Errors
  private IllegalStateException createNoBufferException()
  {
    return new IllegalStateException
      (ProxyTools.getShortClassName(this) +
       " configuration error: no transition buffer!");
  }

  private IllegalStateException createNoBufferException(final String name)
  {
    return new IllegalStateException
      (ProxyTools.getShortClassName(this) +
       " configuration error: " + name + " buffer not initialised!");
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
    private MemStateProxy(final int code, final boolean init,
                          final Collection<EventProxy> props)
    {
      mCode = code;
      mIsInitial = init;
      mProps = props;
    }

    //#######################################################################
    //# Simple Access
    @SuppressWarnings("unused")
    int getCode()
    {
      return mCode;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.des.StateProxy
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

    //#######################################################################
    //# Overrides for java.lang.Object
    public String toString()
    {
      return getName();
    }

    //#######################################################################
    //# Data Members
    private final int mCode;
    private final boolean mIsInitial;
    private final Collection<EventProxy> mProps;
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final ComponentKind mKind;
  private final List<EventProxy> mEvents;
  private final List<EventProxy> mPropositions;
  private final TObjectIntHashMap<EventProxy> mEventsMap;
  private final StateProxy[] mOriginalStates;
  private final TObjectIntHashMap<StateProxy> mOriginalStatesMap;

  private final IntStateBuffer mStateBuffer;
  private TransitionListBuffer mSuccessorBuffer;
  private TransitionListBuffer mPredecessorBuffer;

}
