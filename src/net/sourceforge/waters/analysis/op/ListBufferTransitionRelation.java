//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   ListBufferTransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntStack;
import gnu.trove.TLongObjectHashMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * A more convenient means to store and retrieve transitions of an automaton.
 *
 * The list buffer transition relation is created from an automaton to
 * index its transitions, making it easier to associate states with
 * transitions, and to modify the transition structure.
 *
 * Transitions are stored in a {@link TransitionListBuffer} in bit-packed form
 * in blocked linked lists. The user may choose to create a buffer for outgoing
 * transitions, which enables quick access to transitions given their source
 * state, or a buffer for incoming transitions, which enables quick access
 * to transitions given their target state, or both.
 *
 * Reconfiguration of the buffer selection is possible, but time-consuming.
 * Some methods require the presence or absence of the incoming or outgoing
 * buffer, see details with each method.
 *
 * The encoding of states and events is defined by the user upon creation
 * of the transition relation, using a {@link StateEncoding} and an
 * {@link EventEncoding}. After construction, the encoding can no longer
 * be changed, except that events can be removed (marked as unused) and
 * states can be marked as unreachable. These removals will be respected
 * when creating an automaton from the transition relation.
 *
 * The transition buffers recognise the silent event code
 * {@link EventEncoding#TAU} and automatically suppress all selfloops using
 * this event.
 *
 * The transition relation also associates with each state its initial
 * status and its propositions in a bit set, using an {@link IntStateBuffer}.
 *
 * @see StateEncoding
 * @see EventEncoding
 * @see IntStateBuffer
 * @see TransitionListBuffer
 *
 * @author Robi Malik
 */

public class ListBufferTransitionRelation
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new transition relation from the given automaton,
   * using default (temporary) state and event encodings.
   * @param  aut         The automaton to be encoded.
   * @param  config      Configuration flags defining which transition buffers
   *                     are to be created. Should be one of
   *                     {@link #CONFIG_SUCCESSORS},
   *                     {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   * @param  translator  Kind translator to distinguish propositions from
   *                     proper events.
   * @throws OverflowException if the automaton's number of states and events
   *         is too large to be encoded in the bit sizes used by the
   *         list buffer implementations.
   */
  public ListBufferTransitionRelation(final AutomatonProxy aut,
                                      final int config,
                                      final KindTranslator translator)
    throws OverflowException
  {
    this(aut, new EventEncoding(aut, translator), config);
  }

  /**
   * Creates a new transition relation from the given automaton,
   * using default (temporary) state encoding.
   * @param  aut      The automaton to be encoded.
   * @param  eventEnc Event encoding to define the assignment of integer
   *                  codes to events in the transition buffers.
   * @param  config   Configuration flags defining which transition buffers
   *                  are to be created. Should be one of
   *                  {@link #CONFIG_SUCCESSORS},
   *                  {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   * @throws OverflowException if the automaton's number of states and events
   *         is too large to be encoded in the bit sizes used by the
   *         list buffer implementations.
   */
  public ListBufferTransitionRelation(final AutomatonProxy aut,
                                      final EventEncoding eventEnc,
                                      final int config)
    throws OverflowException
  {
    this(aut, eventEnc, new StateEncoding(aut), config);
  }

  /**
   * Creates a new transition relation from the given automaton,
   * using the given state and event encoding.
   * @param  aut      The automaton to be encoded.
   * @param  eventEnc Event encoding to define the assignment of integer
   *                  codes to events in the transition buffers.
   * @param  stateEnc State encoding to define the assignment of integer
   *                  codes to events in the transition buffers.
   * @param  config   Configuration flags defining which transition buffers
   *                  are to be created. Should be one of
   *                  {@link #CONFIG_SUCCESSORS},
   *                  {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   * @throws OverflowException if the given number of states and events
   *         is too large to be encoded in the bit sizes used by the
   *         list buffer implementations.
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
    final int numTrans = aut.getTransitions().size();
    if ((config & CONFIG_SUCCESSORS) != 0) {
      mSuccessorBuffer =
        new OutgoingTransitionListBuffer(numEvents, numStates, numTrans);
      mSuccessorBuffer.setUpTransitions(list, eventEnc, stateEnc);
    }
    if ((config & CONFIG_PREDECESSORS) != 0) {
      mPredecessorBuffer =
        new IncomingTransitionListBuffer(numEvents, numStates, numTrans);
      mPredecessorBuffer.setUpTransitions(list, eventEnc, stateEnc);
    }
    mUsedEvents = new BitSet(numEvents);
    final int tau = EventEncoding.TAU;
    final int first = eventEnc.getProperEvent(tau) == null ? tau + 1 : tau;
    mUsedEvents.set(first, numEvents, true);
  }


  /**
   * Creates an empty transition relation. This method creates a transition
   * relation with the given number of states and event encoding, but without
   * any transitions. All states are marked reachable, yet initial states have
   * to be set using {@link #setInitial(int,boolean)}, and transitions have to
   * be added using {@link #addTransition(int,int,int)}.
   * @param  name      A name for the new transition relation.
   * @param  kind      A component kind for the new transition relation.
   * @param  eventEnc  Event encoding to define the assignment of integer
   *                   codes to events in the transition buffers.
   * @param  numStates The number of states to be encoded.
   * @param  config    Configuration flags defining which transition buffers
   *                   are to be created. Should be one of
   *                   {@link #CONFIG_SUCCESSORS},
   *                   {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   * @throws OverflowException if the given number of states and events
   *         is too large to be encoded in the bit sizes used by the
   *         list buffer implementations.
   */
  public ListBufferTransitionRelation
    (final String name,
     final ComponentKind kind,
     final EventEncoding eventEnc,
     final int numStates,
     final int config)
    throws OverflowException
  {
    checkConfig(config);
    mName = name;
    mKind = kind;
    final int numProps = eventEnc.getNumberOfPropositions();
    mStateBuffer = new IntStateBuffer(numStates, numProps);
    final int numEvents = eventEnc.getNumberOfProperEvents();
    if ((config & CONFIG_SUCCESSORS) != 0) {
      mSuccessorBuffer =
        new OutgoingTransitionListBuffer(numEvents, numStates, 0);
    }
    if ((config & CONFIG_PREDECESSORS) != 0) {
      mPredecessorBuffer =
        new IncomingTransitionListBuffer(numEvents, numStates, 0);
    }
    mUsedEvents = new BitSet(numEvents);
    final int tau = EventEncoding.TAU;
    final int first = eventEnc.getProperEvent(tau) == null ? tau + 1 : tau;
    mUsedEvents.set(first, numEvents, true);
  }


  //#########################################################################
  //# Overrides for java.lang.object
  public String toString()
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.toString();
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.toString();
    } else {
      return
        "{" + ProxyTools.getShortClassName(this) + ": no buffer configured.}";
    }
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the name of this transition relation.
   * This name will be given to any automaton created from this
   * transition relation.
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding)
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding,StateEncoding)
   */
  public String getName()
  {
    return mName;
  }

  /**
   * Sets a new name for this transition relation.
   * This name will be given to any automaton created from this
   * transition relation.
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding)
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding,StateEncoding)
   */
  public void setName(final String name)
  {
    mName = name;
  }

  /**
   * Gets the kind of this transition relation.
   * This attribute will be used for any automaton created from this
   * transition relation.
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding)
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding,StateEncoding)
   */
  public ComponentKind getKind()
  {
    return mKind;
  }

  /**
   * Sets the kind of this transition relation.
   * This attribute will be used for any automaton created from this
   * transition relation.
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding)
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding,StateEncoding)
   */
  public void setKind(final ComponentKind kind)
  {
    mKind = kind;
  }


  //#########################################################################
  //# Event Access
  public int getNumberOfProperEvents()
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.getNumberOfEvents();
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.getNumberOfEvents();
    } else {
      throw createNoBufferException();
    }
  }

  public int getNumberOfPropositions()
  {
    return mStateBuffer.getNumberOfPropositions();
  }


  //#########################################################################
  //# State Access
  /**
   * Gets the number of states in the transition relation,
   * including any states set to be unreachable.
   */
  public int getNumberOfStates()
  {
    return mStateBuffer.getNumberOfStates();
  }

  /**
   * Gets the number of reachable states in the transition relation.
   * A state is considered reachable if its reachability flag is set.
   */
  public int getNumberOfReachableStates()
  {
    return mStateBuffer.getNumberOfReachableStates();
  }

  /**
   * Gets the total number of markings in this transition relation.
   * Each instance of a proposition marking a reachable state counts
   * as marking.
   */
  public int getNumberOfMarkings()
  {
    return mStateBuffer.getNumberOfMarkings();
  }

  /**
   * Gets the initial status of the given state.
   * @return <CODE>true</CODE> if the state is an initial state,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isInitial(final int state)
  {
    return mStateBuffer.isInitial(state);
  }

  /**
   * Sets the initial status of the given state.
   * @param  state  The ID of state to be modified.
   * @param  init   <CODE>true</CODE> if the state is to be ab initial state,
   *                <CODE>false</CODE> otherwise.
   */
  public void setInitial(final int state, final boolean init)
  {
    mStateBuffer.setInitial(state, init);
  }

  /**
   * Gets the reachability status of the given state.
   * Each state has a reachability flag associated with it, which is used
   * to suppress unreachable states when creating an automaton from the
   * transition relation. The reachability status is not set automatically;
   * it is to be set by the user when a state is deemed unreachable.
   * @see #setReachable(int,boolean) setReachable()
   * @see #checkReachability()
   * @see #createAutomaton(ProductDESProxyFactory, EventEncoding)
   * @see #createAutomaton(ProductDESProxyFactory, EventEncoding,StateEncoding)
   */
  public boolean isReachable(final int state)
  {
    return mStateBuffer.isReachable(state);
  }

  /**
   * Sets the reachability status of the given state.
   * If a state is set to be unreachable, transitions linked to the state
   * will be removed automatically, and the initial state status is
   * removed.
   * @see #removeTransitions(int) removeTransitions()
   */
  public void setReachable(final int state, final boolean reachable)
  {
    mStateBuffer.setReachable(state, reachable);
    if (!reachable) {
      mStateBuffer.setInitial(state, false);
      removeTransitions(state);
    }
  }


  //#########################################################################
  //# Markings Access
  /**
   * Tests whether a state is marked with a particular proposition.
   * @param state
   *          ID of the state to be tested.
   * @param prop
   *          ID of proposition identifying the marking to be looked up.
   * @return <CODE>true</CODE> if the state is marked, <CODE>false</CODE>
   *         otherwise.
   */
  public boolean isMarked(final int state, final int prop)
  {
    return mStateBuffer.isMarked(state, prop);
  }

  /**
   * Gets a number that identifies the complete set of markings for the
   * given state.
   * @param  state   ID of the state to be examined.
   * @return A marking pattern for the state. The only guarantee about the
   *         number returned is that two states with the same set of markings
   *         will always have the same marking patterns, and states with
   *         different sets of markings will always have different marking
   *         patterns.
   */
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
   *          ID of proposition identifying the marking of which is
   *          to be modified.
   * @param value
   *          Whether the marking should be set (<CODE>true</CODE>) or cleared
   *          (<CODE>false</CODE>) for the given state and proposition.
   */
  public void setMarked(final int state, final int prop, final boolean value)
  {
    mStateBuffer.setMarked(state, prop, value);
  }

  /**
   * Sets all markings for the given state simultaneously.
   * @param  state    ID of the state to be modified.
   * @param  markings A new marking pattern for the state. This pattern
   *                  can be obtained through the method
   *                  {@link #getAllMarkings(int) getAllMarkings()},
   *                  {@link #createMarkings(TIntArrayList) createMarkings()},
   *                  or {@link #mergeMarkings(long,long) mergeMarkings()}.
   */
  public void setAllMarkings(final int state, final long markings)
  {
    mStateBuffer.setAllMarkings(state, markings);
  }

  /**
   * Adds several markings to a given state simultaneously.
   * @param  state    ID of the state to be modified.
   * @param  markings A pattern of additional markings for the state. This
   *                  pattern can be obtained through the method
   *                  {@link #getAllMarkings(int) getAllMarkings()},
   *                  {@link #createMarkings(TIntArrayList) createMarkings()},
   *                  or {@link #mergeMarkings(long,long) mergeMarkings()}.
   * @return <CODE>true</CODE> if the call resulted in markings being changed,
   *         i.e., if the pattern contained a marking not already present
   *         on the state.
   */
  public boolean addMarkings(final int state, final long markings)
  {
    return mStateBuffer.addMarkings(state, markings);
  }

  /**
   * Removes several markings from a given state simultaneously.
   * @param  state    ID of the state to be modified.
   * @param  markings A pattern of markings to be removed from the state.
   *                  This pattern can be obtained through the method
   *                  {@link #getAllMarkings(int) getAllMarkings()},
   *                  {@link #createMarkings(TIntArrayList) createMarkings()},
   *                  or {@link #mergeMarkings(long,long) mergeMarkings()}.
   * @return <CODE>true</CODE> if the call resulted in markings being changed,
   *         i.e., if the pattern contained a marking actually present
   *         on the state.
   */
  public boolean removeMarkings(final int state, final long markings)
  {
    return mStateBuffer.removeMarkings(state, markings);
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
   * Copies markings from one state to another. This method adds all the
   * markings of the given source state to the given destination state.
   * The markings of the source state will not be changed, and the destination
   * state retains any markings it previously had in addition to the new ones.
   */
  public void copyMarkings(final int source, final int dest)
  {
    mStateBuffer.copyMarkings(source, dest);
  }

  /**
   * Creates a markings pattern for the given propositions.
   * @param  props    Collection of proposition IDs defining a state marking.
   * @return A number identifying the given combination of propositions.
   * @see #setAllMarkings(int,long) setAllMarkings()
   */
  public long createMarkings(final TIntArrayList props)
  {
    return mStateBuffer.createMarkings(props);
  }

  /**
   * Adds a marking to the given marking pattern.
   * @param  markings  Marking pattern to be augmented.
   * @param  prop      Code of proposition to be added to pattern.
   * @return A number identifying a marking consisting of all propositions
   *         contained in the given markings, plus the the additional marking.
   * @see #mergeMarkings(long, long)
   * @see #setAllMarkings(int,long) setAllMarkings()
   */
  public long addMarking(final long markings, final int prop)
  {
    return mStateBuffer.addMarking(markings, prop);
  }

  /**
   * Checks whether the given marking pattern contains the given proposition.
   * @param  markings  Marking pattern to be examined.
   * @param  prop      Code of proposition to be tested.
   * @return <CODE>true</CODE> if the marking pattern includes the given
   *         proposition, <CODE>false</CODE> otherwise.
   */
  public boolean isMarked(final long markings, final int prop)
  {
    return mStateBuffer.isMarked(markings, prop);
  }

  /**
   * Combines two marking patterns.
   * @return A number identifying a marking consisting of all propositions
   *         contained in one of the two input marking patterns.
   * @see #setAllMarkings(int,long) setAllMarkings()
   */
  public long mergeMarkings(final long markings1, final long markings2)
  {
    return mStateBuffer.mergeMarkings(markings1, markings2);
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
   * {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int, int)} before it can be used.
   * Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createSuccessorsReadOnlyIterator()
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createReadOnlyIterator();
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
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
  public TransitionIterator createSuccessorsReadOnlyIterator(final int source)
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createReadOnlyIterator(source);
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
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
  public TransitionIterator createSuccessorsReadOnlyIterator(final int source,
                                                             final int event)
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createReadOnlyIterator(source, event);
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Creates a read-only iterator for this transition relation's
   * incoming transitions.
   * The iterator returned is not initialised, so one of the methods
   * {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int, int)} before it can be used.
   * Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createPredecessorsReadOnlyIterator()
  {
    if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createReadOnlyIterator();
    } else {
      throw createNoBufferException(CONFIG_PREDECESSORS);
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
  public TransitionIterator createPredecessorsReadOnlyIterator(final int target)
  {
    if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createReadOnlyIterator(target);
    } else {
      throw createNoBufferException(CONFIG_PREDECESSORS);
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
  public TransitionIterator createPredecessorsReadOnlyIterator(final int target,
                                                               final int event)
  {
    if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createReadOnlyIterator(target, event);
    } else {
      throw createNoBufferException(CONFIG_PREDECESSORS);
    }
  }

  /**
   * Creates a read-only iterator for this transition relation that is set up
   * to iterate over the incoming or outgoing transitions associated with the
   * given state, whichever is available.
   * The iterator returned produces all transitions associated with the given
   * state in the buffer's defined ordering, no matter what event they use.
   * Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createAnyReadOnlyIterator(final int state)
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createReadOnlyIterator(state);
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createReadOnlyIterator(state);
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Creates a read-only iterator for this transition relation that is set up
   * to iterate over the incoming or outgoing transitions associated with the
   * given state and event, whichever is available.
   * The iterator returned produces all transitions associated with
   * the given state and event in the buffer's defined ordering.
   * Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createAnyReadOnlyIterator(final int state,
                                                      final int event)
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createReadOnlyIterator(state, event);
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createReadOnlyIterator(state, event);
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Creates a read-only iterator over all transitions in this transition
   * relation.
   * The iterator returned is set up to return the first transition in
   * this buffer after calling {@link TransitionIterator#advance()}. It does
   * not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}, and
   * being a read-only iterator, it also does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createAllTransitionsReadOnlyIterator()
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
   * Creates a read-only iterator over all transitions with the given event.</P>
   * The iterator returned is set up to return the first transition with
   * the given event after calling {@link TransitionIterator#advance()}. It
   * does not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}, and
   * being a read-only iterator, it also does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createAllTransitionsReadOnlyIterator
    (final int event)
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createAllTransitionsReadOnlyIterator(event);
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createAllTransitionsReadOnlyIterator(event);
    } else {
      throw createNoBufferException();
    }
  }


  /**
   * Creates a read/write iterator for this transition relation's
   * outgoing transitions.
   * The iterator returned is not initialised, so one of the methods
   * {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int, int)} before it can be used.
   * <P><STRONG>Warning.</STRONG> The transition relation should be configured
   * to use only a predecessor buffer. If both buffers are configured, the
   * predecessor buffer will be closed!</P>
   */
  public TransitionIterator createSuccessorsModifyingIterator()
  {
    if (mSuccessorBuffer != null) {
      mPredecessorBuffer = null;
      return mSuccessorBuffer.createModifyingIterator();
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Creates a read/write iterator for this transition relation's
   * incoming transitions.
   * The iterator returned is not initialised, so one of the methods
   * {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int, int)} before it can be used.
   * <P><STRONG>Warning.</STRONG> The transition relation should be configured
   * to use only a predecessor buffer. If both buffers are configured, the
   * successor buffer will be closed!</P>
   */
  public TransitionIterator createPredecessorsModifyingIterator()
  {
    if (mPredecessorBuffer != null) {
      mSuccessorBuffer = null;
      return mPredecessorBuffer.createModifyingIterator();
    } else {
      throw createNoBufferException(CONFIG_PREDECESSORS);
    }
  }

  /**
   * <P>Creates a read/write iterator over all transitions in this transition
   * relation.</P>
   * <P>The iterator returned is set up to return the first transition in
   * this buffer after calling {@link TransitionIterator#advance()}. It does
   * not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}.</P>
   * <P><STRONG>Warning.</STRONG> The transition relation should be configured
   * to use only one transition buffer. If both buffers are configured, the
   * predecessor buffer will be closed!</P>
   */
  public TransitionIterator createAllTransitionsModifyingIterator()
  {
    if (mSuccessorBuffer != null) {
      mPredecessorBuffer = null;
      return mSuccessorBuffer.createAllTransitionsModifyingIterator();
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createAllTransitionsModifyingIterator();
    } else {
      throw createNoBufferException();
    }
  }

  /**
   * <P>Creates a read/write iterator over all transitions with the given
   * event.</P>
   * <P>The iterator returned is set up to return the first transition with the
   * given event after calling {@link TransitionIterator#advance()}. It does
   * not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}.</P>
   * <P><STRONG>Warning.</STRONG> The transition relation should be configured
   * to use only one transition buffer. If both buffers are configured, the
   * predecessor buffer will be closed!</P>
   */
  public TransitionIterator createAllTransitionsModifyingIterator
    (final int event)
  {
    if (mSuccessorBuffer != null) {
      mPredecessorBuffer = null;
      return mSuccessorBuffer.createAllTransitionsModifyingIterator(event);
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createAllTransitionsModifyingIterator(event);
    } else {
      throw createNoBufferException();
    }
  }

  /**
   * Creates an iterator for the tau-closure of this buffer.
   * The iterator returned is not initialised, so the method
   * {@link TransitionIterator#resetState(int)} must be used before it can be
   * used. After initialisation, the first state returned by the iterator is
   * the start state of iteration, and it is followed by all successors
   * reachable by sequences of {@link EventEncoding#TAU} events in depth-first
   * order. The tau-closure iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createSuccessorsTauClosureIterator()
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createTauClosureIterator();
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Creates a iterator for the tau-closure of this buffer that is set up
   * to iterate over all states silently reachable form the given state.
   * The first state returned by the iterator is the given state,
   * and it is followed by all successors reachable by sequences of
   * {@link EventEncoding#TAU} events in depth-first order. The tau-closure
   * iterator is a read-only iterator and does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createSuccessorsTauClosureIterator
    (final int state)
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createTauClosureIterator(state);
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Creates an iterator for the backwards tau-closure of this buffer.
   * The iterator returned is not initialised, so the method
   * {@link TransitionIterator#resetState(int)} must be used before it can be
   * used. After initialisation, the first state returned by the iterator is
   * the start state of iteration, and it is followed by all predecessors
   * reachable by sequences of {@link EventEncoding#TAU} events in depth-first
   * order. The tau-closure iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createPredecessorsTauClosureIterator()
  {
    if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createTauClosureIterator();
    } else {
      throw createNoBufferException(CONFIG_PREDECESSORS);
    }
  }

  /**
   * Creates a iterator for the backwards tau-closure of this buffer
   * that is set up to iterate over all states from which the given
   * state is silently reachable.
   * The first state returned by the iterator is the given state,
   * and it is followed by all predecessors reachable by sequences of
   * {@link EventEncoding#TAU} events in depth-first order. The tau-closure
   * iterator is a read-only iterator and does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createPredecessorsTauClosureIterator
    (final int state)
  {
    if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createTauClosureIterator(state);
    } else {
      throw createNoBufferException(CONFIG_PREDECESSORS);
    }
  }

  /**
   * Checks whether this transition relation represents a deterministic
   * automaton.
   * @throws IllegalStateException if the transition relation is not
   *         configure with a successor buffer.
   */
  public boolean isDeterministic()
  {
    final int numStates = getNumberOfStates();
    boolean hasinit = false;
    for (int state = 0; state < numStates; state++) {
      if (isInitial(state) && isReachable(state)) {
        if (hasinit) {
          return false;
        }
        hasinit = true;
      }
    }
    if (mSuccessorBuffer == null) {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
    final TransitionIterator iter =
      mSuccessorBuffer.createAllTransitionsReadOnlyIterator();
    int state = -1;
    int event = -1;
    while (iter.advance()) {
      if (state == iter.getCurrentSourceState() &&
          event == iter.getCurrentEvent()) {
        return false;
      }
      state = iter.getCurrentSourceState();
      event = iter.getCurrentEvent();
    }
    return true;
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
   * Adds several transitions to one target state to this transition
   * relation. The new transitions are inserted in a defined ordering
   * in the predecessor and/or successor buffers.
   * @param  sources The IDs of the source states of the new transitions.
   * @param  event   The ID of the event of the new transitions.
   * @param  target  The ID of the target state of the new transitions.
   * @return <CODE>true</CODE> if at least one transition was added;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean addTransitions(final TIntArrayList sources,
                                final int event,
                                final int target)
  {
    boolean result;
    if (mPredecessorBuffer != null) {
      result = mPredecessorBuffer.addTransitions(target, event, sources);
    } else {
      result = true;
    }
    if (mSuccessorBuffer != null && result) {
      result = false;
      for (int i = 0; i < sources.size(); i++) {
        final int source = sources.get(i);
        result |= mSuccessorBuffer.addTransition(source, event, target);
      }
    }
    return result;
  }

  /**
   * Adds several transitions from one source state to this transition
   * relation. The new transitions are inserted in a defined ordering
   * in the predecessor and/or successor buffers.
   * @param  source  The ID of the source state of the new transitions.
   * @param  event   The ID of the event of the new transitions.
   * @param  targets The IDs of the target states of the new transitions.
   * @return <CODE>true</CODE> if at least one transition was added;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean addTransitions(final int source,
                                final int event,
                                final TIntArrayList targets)
  {
    boolean result;
    if (mSuccessorBuffer != null) {
      result = mSuccessorBuffer.addTransitions(source, event, targets);
    } else {
      result = true;
    }
    if (mPredecessorBuffer != null && result) {
      result = false;
      for (int i = 0; i < targets.size(); i++) {
        final int target = targets.get(i);
        result |= mPredecessorBuffer.addTransition(target, event, source);
      }
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
   * Removes all transitions associated with the given state.
   * This method removes all transitions indexed under the given state.
   * Depending on the buffer configuration, this does not necessarily
   * remove all transitions linked to the state, only those that are
   * readily accessible.
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean removeTransitions(final int state)
  {
    boolean result = false;
    if (mSuccessorBuffer != null) {
      result = removeOutgoingTransitions(state);
    }
    if (mPredecessorBuffer != null) {
      result |= removeIncomingTransitions(state);
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
      throw createNoBufferException(CONFIG_SUCCESSORS);
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
      throw createNoBufferException(CONFIG_SUCCESSORS);
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
      throw createNoBufferException(CONFIG_PREDECESSORS);
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
      throw createNoBufferException(CONFIG_PREDECESSORS);
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
        throw createNoBufferException(CONFIG_SUCCESSORS);
      }
      copyMarkings(from, to);
      mSuccessorBuffer.copyTransitions(from, to, mPredecessorBuffer);
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
        throw createNoBufferException(CONFIG_PREDECESSORS);
      }
      if (isInitial(from)) {
        setInitial(to, true);
      }
      mPredecessorBuffer.copyTransitions(from, to, mSuccessorBuffer);
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
   * @param  event   The ID of the event to be tested.
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
   * @param  event   The ID of the event to be tested.
   * @return <CODE>true</CODE> if the given event is selflooped in every state,
   *         and appears on no other transitions.
   */
  public boolean isPureSelfloopEvent(final int event)
  {
    final TransitionListBuffer buffer;
    if (mSuccessorBuffer != null) {
      buffer = mSuccessorBuffer;
    } else if (mPredecessorBuffer != null) {
      buffer = mPredecessorBuffer;
    } else {
      throw createNoBufferException();
    }
    final int numStates = getNumberOfStates();
    final TransitionIterator iter = buffer.createReadOnlyIterator();
    for (int state = 0; state < numStates; state++) {
      if (isReachable(state)) {
        iter.reset(state, event);
        if (iter.advance()) {
          do {
            if (iter.getCurrentToState() != state) {
              return false;
            }
          } while (iter.advance());
        } else {
          return false;
        }
      }
    }
    return true;
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
   * @param  oldID   The ID of the old event to be replaced.
   * @param  newID   The ID of the new event replacing the old event.
   */
  public void replaceEvent(final int oldID, final int newID)
  {
    if (mSuccessorBuffer != null) {
      mSuccessorBuffer.replaceEvent(oldID, newID);
    }
    if (mPredecessorBuffer != null) {
      mPredecessorBuffer.replaceEvent(oldID, newID);
    }
    if (mUsedEvents.get(oldID)) {
      mUsedEvents.set(newID);
    }
  }


  //#########################################################################
  //# Buffer Maintenance
  /**
   * Reconfigures the current set of transition buffers.
   * @param  config   Configuration flags defining which transition buffers
   *                  are to be used from now on. Should be one of
   *                  {@link #CONFIG_SUCCESSORS},
   *                  {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   */
  public void reconfigure(final int config)
    throws OverflowException
  {
    checkConfig(config);
    final int numEvents = getNumberOfProperEvents();
    final int numStates = getNumberOfStates();
    if (mSuccessorBuffer == null && (config & CONFIG_SUCCESSORS) != 0) {
      if (mPredecessorBuffer != null) {
        mSuccessorBuffer =
          new OutgoingTransitionListBuffer(numEvents, numStates);
        mSuccessorBuffer.setUpTransitions(mPredecessorBuffer);
      } else {
        throw createNoBufferException(CONFIG_PREDECESSORS);
      }
    }
    if (mPredecessorBuffer == null && (config & CONFIG_PREDECESSORS) != 0) {
      if (mSuccessorBuffer != null) {
        mPredecessorBuffer =
          new IncomingTransitionListBuffer(numEvents, numStates);
        mPredecessorBuffer.setUpTransitions(mSuccessorBuffer);
      } else {
        throw createNoBufferException(CONFIG_SUCCESSORS);
      }
    }
    if ((config & CONFIG_SUCCESSORS) == 0) {
      mSuccessorBuffer = null;
    } else if ((config & CONFIG_PREDECESSORS) == 0) {
      mPredecessorBuffer = null;
    }
  }

  /**
   * Reverses this transition relation.
   * This method reverses all transitions by swapping their source and
   * target. Initial states and markings are not affected by this method.
   * Reversing is implemented by simple swapping the incoming and outgoing
   * transition buffers, so the buffer configuration is also swapped by
   * this method.
   */
  public void reverse()
  {
    final OutgoingTransitionListBuffer newSucc =
      mPredecessorBuffer == null ?
          null : new OutgoingTransitionListBuffer(mPredecessorBuffer);
    final IncomingTransitionListBuffer newPred =
      mSuccessorBuffer == null ?
          null : new IncomingTransitionListBuffer(mSuccessorBuffer);
    mSuccessorBuffer = newSucc;
    mPredecessorBuffer = newPred;
  }


  //#########################################################################
  //# Automaton Simplification
  /**
   * Attempts to simplify the automaton by removing all tau selfloops.
   * If this results in the tau event being disabled, the tau event is
   * marked as unused. Tau events are recognised by their standard
   * code {@link EventEncoding#TAU}.
   * Note that the creation of tau selfloops is suppressed by most transition
   * relation methods, but it may still be useful to call removeTauSelfloops()
   * to mark the tau event unused if possible.
   * @return <CODE>true</CODE> if all transitions with the tau event
   *         were selfloops and have been removed, <CODE>false</CODE>
   *         otherwise.
   */
  public boolean removeTauSelfLoops()
  {
    if (mUsedEvents.get(EventEncoding.TAU)) {
      boolean removable = false;
      if (mSuccessorBuffer != null) {
        removable = mSuccessorBuffer.removeTauSelfloops();
      }
      if (mPredecessorBuffer != null) {
        removable = mPredecessorBuffer.removeTauSelfloops();
      }
      if (removable) {
        mUsedEvents.clear(EventEncoding.TAU);
      }
      return removable;
    } else {
      return false;
    }
  }

  /**
   * Attempts to simplify the automaton by removing redundant selfloop events.
   * This method searches for any non-tau events that appear only as selfloops
   * and are selflooped in all states of the transition relation, marks such
   * events as unused, and removes the selfloops from the transition relation.
   * @return <CODE>true</CODE> if at least one event was removed,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean removeProperSelfLoopEvents()
  {
    final int tau = EventEncoding.TAU;
    boolean modified = false;
    for (int e = 0; e < getNumberOfProperEvents(); e++) {
      if (e != tau && mUsedEvents.get(e) && isPureSelfloopEvent(e)) {
        removeEvent(e);
        modified = true;
      }
    }
    return modified;
  }

  /**
   * Removes all unreachable transitions. A transition is considered
   * unreachable if its source or target state is marked as unreachable.
   * This methods visits every transition in the buffer, checks its
   * reachability, and removes it if unreachable.
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   * @see #setReachable(int, boolean) setReachable()
   */
  public boolean removeUnreachableTransitions()
  {
    boolean modified = false;
    final TransitionIterator iter = createAllTransitionsModifyingIterator();
    while (iter.advance()) {
      if (!isReachable(iter.getCurrentSourceState()) ||
          !isReachable(iter.getCurrentTargetState())) {
        iter.remove();
        modified = true;
      }
    }
    return modified;
  }

  /**
   * Checks for each proposition whether is appears on all reachable states,
   * and if so, removes the proposition by marking it as unused.
   * @return <CODE>true</CODE> if at least one proposition was removed,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean removeRedundantPropositions()
  {
    return mStateBuffer.removeRedundantPropositions();
  }

  /**
   * Repartitions the states of this transition relation. This method is used to
   * merge states after a partition has been obtained through a
   * {@link TransitionRelationSimplifier}.
   * @param partition
   *          The partitioning to be imposed, or <CODE>null</CODE>.
   *          Each array in the list defines the state codes comprising an
   *          equivalence class to be merged into a single state. The index
   *          position in the list identifies the state code to be given to
   *          the new merged state.
   *          An argument of <CODE>null</CODE> indicates a trivial partition,
   *          and has no effect.
   */
  public void merge(final List<int[]> partition)
  {
    if (partition != null) {
      try {
        final int newSize = partition.size();
        if (mSuccessorBuffer != null) {
          mSuccessorBuffer.merge(partition);
        }
        if (mPredecessorBuffer != null) {
          mPredecessorBuffer.merge(partition);
        }
        final int numProps = mStateBuffer.getNumberOfPropositions();
        final long used = mStateBuffer.getUsedPropositions();
        final IntStateBuffer newStateBuffer =
          new IntStateBuffer(newSize, numProps, used);
        int c = 0;
        for (final int[] clazz : partition) {
          boolean init = false;
          long markings = 0;
          for (final int state : clazz) {
            init |= mStateBuffer.isInitial(state);
            markings |= mStateBuffer.getAllMarkings(state);
          }
          newStateBuffer.setInitial(c, init);
          newStateBuffer.setReachable(c, true);
          newStateBuffer.setAllMarkings(c, markings);
          c++;
        }
        mStateBuffer = newStateBuffer;
      } catch (final OverflowException exception) {
        throw new WatersRuntimeException(exception);
      }
    }
  }

  /**
   * Re-evaluates reachability.
   * This method does a full reachability search of the transition relation,
   * and resets the reachability status of all states according to the
   * result. If any states are found to be unreachable, transitions
   * attached to these states are removed.
   * @return <CODE>true</CODE> if the reachability status of at least one
   *         state was changed, <CODE>false</CODE> otherwise.
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an outgoing
   *           transition buffer.
   */
  public boolean checkReachability()
  {
    if (mSuccessorBuffer != null) {
      final int numStates = getNumberOfStates();
      final TIntStack stack = new TIntStack();
      final BitSet reached = new BitSet(numStates);
      for (int s = 0; s < numStates; s++) {
        if (isInitial(s)) {
          stack.push(s);
          reached.set(s);
        }
      }
      final TransitionIterator iter =
        mSuccessorBuffer.createReadOnlyIterator();
      while (stack.size() > 0) {
        final int current = stack.pop();
        iter.resetState(current);
        while (iter.advance()) {
          final int s = iter.getCurrentTargetState();
          if (!reached.get(s)) {
            stack.push(s);
            reached.set(s);
          }
        }
      }
      boolean modified = false;
      for (int s = 0; s < numStates; s++) {
        final boolean oldstatus = isReachable(s);
        final boolean newstatus = reached.get(s);
        if (oldstatus != newstatus) {
          setReachable(s, newstatus);
          modified = true;
        }
      }
      return modified;
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }


  //#########################################################################
  //# Automaton Output
  /**
   * Creates an automaton from this transition relation.
   * This method creates an {@link AutomatonProxy} object that contains all
   * events not marked as unused and all reachable states of the transition
   * relation, and links them with all the transitions stored.
   * @param factory  Factory used from proxy creation.
   * @param eventEnc Event encoding defining what events are to be used
   *                 for the integer codes in the transition relation.
   */
  public AutomatonProxy createAutomaton
    (final ProductDESProxyFactory factory, final EventEncoding eventEnc)
  {
    return createAutomaton(factory, eventEnc, null);
  }

  /**
   * Creates an automaton from this transition relation.
   * This method creates an {@link AutomatonProxy} object that contains all
   * events not marked as unused and all reachable states of the transition
   * relation, and links them with all the transitions stored.
   * @param factory  Factory used from proxy creation.
   * @param eventEnc Event encoding defining what events are to be used
   *                 for the integer codes in the transition relation.
   * @param stateEnc State encoding to be used.
   *                 If non-null and non-empty, the encoding must define state
   *                 objects for the codes of all reachable states with the
   *                 desired initial state attributes and markings. Any
   *                 reachable states defined in the encoding will be used
   *                 in the output automaton.
   *                 If non-null and empty, the method will add to the state
   *                 encoding the states created for the output automaton and
   *                 their assignment to state codes in the transition
   *                 relation.
   */
  public AutomatonProxy createAutomaton
    (final ProductDESProxyFactory factory,
     final EventEncoding eventEnc,
     StateEncoding stateEnc)
  {
    final int numEvents = eventEnc.getNumberOfEvents();
    final int numProps = eventEnc.getNumberOfPropositions();
    final Collection<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    for (int e = 0; e < eventEnc.getNumberOfProperEvents(); e++) {
      if (mUsedEvents.get(e)) {
        final EventProxy event = eventEnc.getProperEvent(e);
        if (event != null) {
          events.add(event);
        }
      }
    }
    for (int p = 0; p < numProps; p++) {
      if (mStateBuffer.isUsedProposition(p)) {
        final EventProxy event = eventEnc.getProposition(p);
        events.add(event);
      }
    }

    final int numStates = getNumberOfStates();
    final StateProxy[] states = new StateProxy[numStates];
    final List<StateProxy> reachable = new ArrayList<StateProxy>(numStates);
    final TLongObjectHashMap<Collection<EventProxy>> markingsMap =
      new TLongObjectHashMap<Collection<EventProxy>>();
    final boolean useStateEnc =
      stateEnc != null && stateEnc.getNumberOfStates() > 0;
    int code = 0;
    for (int s = 0; s < numStates; s++) {
      if (isReachable(s)) {
        final StateProxy state;
        if (useStateEnc) {
          state = stateEnc.getState(s);
        } else {
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
          state = new MemStateProxy(code++, init, props);
        }
        states[s] = state;
        reachable.add(state);
      }
    }
    if (stateEnc == null) {
      stateEnc = new StateEncoding(states);
    } else if (stateEnc.getNumberOfStates() == 0) {
      stateEnc.init(states);
    }

    final int numTrans = getNumberOfTransitions();
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(numTrans);
    final TransitionIterator iter = createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int s = iter.getCurrentSourceState();
      final int t = iter.getCurrentTargetState();
      if (isReachable(s) && isReachable(t)) {
        final StateProxy source = stateEnc.getState(s);
        final int e = iter.getCurrentEvent();
        final EventProxy event = eventEnc.getProperEvent(e);
        final StateProxy target = stateEnc.getState(t);
        final TransitionProxy trans =
          factory.createTransitionProxy(source, event, target);
        transitions.add(trans);
      }
    }
    return factory.createAutomatonProxy(mName, mKind, events, reachable,
                                        transitions);
  }


  //#########################################################################
  //# Debugging
  public void checkIntegrity()
  {
    if (mPredecessorBuffer == null && mSuccessorBuffer == null) {
      throw createNoBufferException();
    }
    if (mPredecessorBuffer != null) {
      mPredecessorBuffer.checkIntegrity();
    }
    if (mSuccessorBuffer != null) {
      mSuccessorBuffer.checkIntegrity();
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

  private IllegalStateException createNoBufferException(final int config)
  {
    switch (config) {
    case CONFIG_SUCCESSORS:
      return new IllegalStateException
        (ProxyTools.getShortClassName(this) +
         " configuration error: successor buffer not initialised!");
    case CONFIG_PREDECESSORS:
      return new IllegalStateException
        (ProxyTools.getShortClassName(this) +
         " configuration error: predecessor buffer not initialised!");
    default:
      return createNoBufferException();
    }
  }


  //#########################################################################
  //# Data Members
  private String mName;
  private ComponentKind mKind;

  private IntStateBuffer mStateBuffer;
  private OutgoingTransitionListBuffer mSuccessorBuffer;
  private IncomingTransitionListBuffer mPredecessorBuffer;
  private final BitSet mUsedEvents;


  //#########################################################################
  //# Class Constants
  /**
   * Configuration setting specifying that the transition relation is to
   * use an outgoing transition buffer.
   */
  public static final int CONFIG_SUCCESSORS = 0x01;
  /**
   * Configuration setting specifying that the transition relation is to
   * use an incoming transition buffer.
   */
  public static final int CONFIG_PREDECESSORS = 0x02;
  /**
   * Configuration setting specifying that the transition relation is to
   * use both an outgoing and an incoming transition buffer.
   */
  public static final int CONFIG_ALL = CONFIG_SUCCESSORS | CONFIG_PREDECESSORS;

}
