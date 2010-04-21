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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;


// TODO Handle tau uniformly.

// TODO Write documentation.


public class ListBufferTransitionRelation
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new transition relation from the given automaton,
   * using default (temporary) state and event encodings.
   * @throws OverflowException if the automaton's number of states and events
   *         is too large to be encoded in the bit sizes used by the
   *         list buffer implementations.
   * @throws IllegalArgumentException if the given configuration does not
   *         specify an incoming or outgoing transition buffer.
   */
  public ListBufferTransitionRelation(final AutomatonProxy aut,
                                      final int config)
    throws OverflowException
  {
    this(aut, new EventEncoding(aut), new StateEncoding(aut), config);
  }

  /**
   * Creates a new transition relation from the given automaton,
   * using the given state and event encoding.
   * @throws OverflowException if the given number of states and events
   *         is too large to be encoded in the bit sizes used by the
   *         list buffer implementations.
   * @throws IllegalArgumentException if the given configuration does not
   *         specify an incoming or outgoing transition buffer.
   */
  public ListBufferTransitionRelation
    (final AutomatonProxy aut,
     final EventEncoding eventEnc,
     final StateEncoding stateEnc,
     final int config)
    throws OverflowException
  {
    checkConfig(config);
    mName = aut.getName();
    mKind = aut.getKind();
    mStateBuffer = new IntStateBuffer(eventEnc, stateEnc);
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    final List<TransitionProxy> list =
      new ArrayList<TransitionProxy>(transitions);
    final int numEvents = eventEnc.getNumberOfProperEvents();
    final int numStates = stateEnc.getNumberOfStates();
    if ((config & CONFIG_SUCCESSORS) != 0) {
      mSuccessorBuffer =
        new OutgoingTransitionListBuffer(numEvents, numStates);
      mSuccessorBuffer.setUpTransitions(list, eventEnc, stateEnc);
    }
    if ((config & CONFIG_PREDECESSORS) != 0) {
      mPredecessorBuffer =
        new IncomingTransitionListBuffer(numEvents, numStates);
      mPredecessorBuffer.setUpTransitions(list, eventEnc, stateEnc);
    }
    mUsedEvents = new BitSet(numEvents);
    mUsedEvents.set(0, numEvents - 1, true);
  }


  //#########################################################################
  //# Simple Access
  public String getName()
  {
    return mName;
  }

  public void setName(final String name)
  {
    mName = name;
  }

  public ComponentKind getKind()
  {
    return mKind;
  }

  public void setKind(final ComponentKind kind)
  {
    mKind = kind;
  }


  //#########################################################################
  //# State Access
  public int getNumberOfStates()
  {
    return mStateBuffer.getNumberOfStates();
  }

  public int getNumberOfReachableStates()
  {
    return mStateBuffer.getNumberOfReachableStates();
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

  /**
   * Creates a read-only iterator for this transition relation's
   * outgoing transitions.
   * The iterator returned is not initialised, so one of the methods
   * {@link TransitionIterator#reset(int)} or
   * {@link TransitionIterator#reset(int, int)} before it can be used.
   * Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   */
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
   *         configured to use an outgoing transition buffer.
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
   * Creates a read-only iterator for this transition relation's
   * incoming transitions.
   * The iterator returned is not initialised, so one of the methods
   * {@link TransitionIterator#reset(int)} or
   * {@link TransitionIterator#reset(int, int)} before it can be used.
   * Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createPredecessorsIterator()
  {
    if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createReadOnlyIterator();
    } else {
      throw createNoBufferException("predecessor");
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
   *         configured to use an incoming transition buffer.
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
   *         configured to use an incoming transition buffer.
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

  /**
   * Removes the given event from this transition relation.
   * This method removes the given event including all its transitions
   * from the transition relation. The event is marked as unused,
   * and all associated transitions are deleted.
   * @param  event   The ID of the event to be removed.
   */
  public void removeEvent(final int event)
  {
    if (mUsedEvents.get(event)) {
      mUsedEvents.clear(event);
      if (mSuccessorBuffer != null) {
        mSuccessorBuffer.removeEventTransitions(event);
      }
      if (mPredecessorBuffer != null) {
        mPredecessorBuffer.removeEventTransitions(event);
      }
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
  //# Buffer Maintenance
  public void reconfigure(final int config)
  {
    try {
      checkConfig(config);
      final int numEvents = getNumberOfProperEvents();
      final int numStates = getNumberOfStates();
      if (mSuccessorBuffer == null && (config & CONFIG_SUCCESSORS) != 0) {
        if (mPredecessorBuffer != null) {
          mSuccessorBuffer =
            new OutgoingTransitionListBuffer(numEvents, numStates);
          mSuccessorBuffer.setUpTransitions(mPredecessorBuffer);
        } else {
          throw createNoBufferException("predecessor");
        }
      }
      if (mPredecessorBuffer == null && (config & CONFIG_PREDECESSORS) != 0) {
        if (mSuccessorBuffer != null) {
          mPredecessorBuffer =
            new OutgoingTransitionListBuffer(numEvents, numStates);
          mPredecessorBuffer.setUpTransitions(mSuccessorBuffer);
        } else {
          throw createNoBufferException("successor");
        }
      }
      if ((config & CONFIG_SUCCESSORS) == 0) {
        mSuccessorBuffer = null;
      } else if ((config & CONFIG_PREDECESSORS) == 0) {
        mPredecessorBuffer = null;
      }
    } catch (final OverflowException exception) {
      throw new WatersRuntimeException(exception);
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
    for (int e = 0; e < getNumberOfProperEvents(); e++) {
      if (mUsedEvents.get(e)) {
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
          mUsedEvents.clear(tau);
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
    (final ProductDESProxyFactory factory, final EventEncoding eventEnc)
  {
    return createAutomaton(factory, eventEnc, null);
  }

  public AutomatonProxy createAutomaton
    (final ProductDESProxyFactory factory,
     final EventEncoding eventEnc,
     StateEncoding stateEnc)
  {
    final int numEvents = eventEnc.getNumberOfEvents();
    final int numProps = eventEnc.getNumberOfPropositions();
    final Collection<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    for (int e = 0; e < eventEnc.getNumberOfProperEvents(); e++) {
      final EventProxy event = eventEnc.getProperEvent(e);
      if (mUsedEvents.get(e)) {
        events.add(event);
      }
    }
    for (int p = 0; p < numProps; p++) {
      final EventProxy event = eventEnc.getProposition(p);
      events.add(event);
    }

    final int numStates = getNumberOfStates();
    final StateProxy[] states = new StateProxy[numStates];
    final List<StateProxy> reachable = new ArrayList<StateProxy>(numStates);
    final TLongObjectHashMap<Collection<EventProxy>> markingsMap =
      new TLongObjectHashMap<Collection<EventProxy>>();
    int code = 0;
    for (int s = 0; s < numStates; s++) {
      if (isReachable(s)) {
        final boolean init = isInitial(s);
        final long markings = mStateBuffer.getAllMarkings(s);
        Collection<EventProxy> props = markingsMap.get(markings);
        if (props == null) {
          props = new ArrayList<EventProxy>(numProps);
          for (int p = 0; p < numProps; p++) {
            if (isMarked(s, p)) {
              final EventProxy prop = eventEnc.getProposition(p);
              props.add(prop);
            }
          }
          markingsMap.put(markings, props);
        }
        final StateProxy state = new MemStateProxy(code++, init, props);
        states[s] = state;
        reachable.add(state);
      }
    }
    if (stateEnc == null) {
      stateEnc = new StateEncoding(states);
    } else {
      stateEnc.init(states);
    }

    final int numTrans = getNumberOfTransitions();
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(numTrans);
    final TransitionIterator iter = createAllTransitionsIterator();
    while (iter.advance()) {
      final int s = iter.getCurrentSourceState();
      final StateProxy source = stateEnc.getState(s);
      final int e = iter.getCurrentEvent();
      final EventProxy event = eventEnc.getProperEvent(e);
      final int t = iter.getCurrentSourceState();
      final StateProxy target = stateEnc.getState(t);
      final TransitionProxy trans =
        factory.createTransitionProxy(source, event, target);
      transitions.add(trans);
    }
    return factory.createAutomatonProxy(mName, mKind, events, reachable,
                                        transitions);
  }


  //#########################################################################
  //# Auxiliary Methods
  private int getNumberOfProperEvents()
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.getNumberOfEvents();
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.getNumberOfEvents();
    } else {
      throw createNoBufferException();
    }
  }


  //#########################################################################
  //# Errors
  private void checkConfig(final int config)
  {
    if ((config & CONFIG_ALL) == 0) {
      throw new IllegalArgumentException
        (ProxyTools.getShortClassName(this) + " configuration error: " +
         "no incoming or outgoing transition buffer specified!");
    }
  }

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
  private String mName;
  private ComponentKind mKind;

  private final IntStateBuffer mStateBuffer;
  private TransitionListBuffer mSuccessorBuffer;
  private TransitionListBuffer mPredecessorBuffer;
  private final BitSet mUsedEvents;


  //#########################################################################
  //# Class Constants
  public static final int CONFIG_SUCCESSORS = 0x01;
  public static final int CONFIG_PREDECESSORS = 0x02;
  public static final int CONFIG_ALL = CONFIG_SUCCESSORS | CONFIG_PREDECESSORS;

}
