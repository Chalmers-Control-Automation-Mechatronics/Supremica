//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.tr;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.marshaller.MarshallingTools;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.logging.log4j.Logger;


/**
 * <P>A more convenient means to store and retrieve transitions of an
 * automaton.</P>
 *
 * <P>The list buffer transition relation is created from an automaton to index
 * its transitions, making it easier to associate states with transitions, and
 * to modify the transition structure.</P>
 *
 * <P>Transitions are stored in a {@link TransitionListBuffer} in bit-packed
 * form in blocked linked lists. The user may choose to create a buffer for
 * outgoing transitions, which enables quick access to transitions given their
 * source state, or a buffer for incoming transitions, which enables quick
 * access to transitions given their target state, or both.
 * Reconfiguration of the buffer selection is possible, but time-consuming.
 * Some methods require the presence or absence of the incoming or outgoing
 * buffer, see details with each method.</P>
 *
 * <P>Access to the transitions is possible through transition iterators
 * ({@link TransitionIterator}), which are obtained using methods such as
 * {@link #createSuccessorsReadOnlyIterator()}. Depending on the buffer
 * configuration, it is possible to iterate over all transitions, or all
 * transitions with a given source or target state, or all transitions with a
 * given source or target state and event, etc.</P>
 *
 * <P>The encoding of states and events is defined by the user upon creation
 * of the transition relation, using a {@link StateEncoding} and an
 * {@link EventEncoding}. After construction, the encodings can no longer be
 * changed, except that the status of events changed, events can be removed
 * (by setting their status to be unused), and states can be marked as
 * unreachable.</P>
 *
 * <P>The transition buffers recognise the silent event code
 * {@link EventEncoding#TAU} and automatically suppress all selfloops using
 * this event.</P>
 *
 * <P>The transition relation associates with each state its initial
 * status, and its propositions in a bit set, using an {@link IntStateBuffer}.
 * There also is special support for a <I>dump state</I>, which is a unique
 * non-accepting state without outgoing transitions.</P>
 *
 * @see StateEncoding
 * @see EventEncoding
 * @see IntStateBuffer
 * @see TransitionListBuffer
 * @see TransitionIterator
 *
 * @author Robi Malik, Roger Su
 */
public class ListBufferTransitionRelation implements EventStatusProvider
{
  //#########################################################################
  //# Constructors
  /**
   * Creates a new transition relation from the given automaton, using default
   * (temporary) state encoding.
   *
   * @param aut
   *          The automaton to be encoded.
   * @param eventEnc
   *          Event encoding to define the assignment of integer codes to
   *          events in the transition buffers.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          created. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   * @throws OverflowException
   *           if the automaton's number of states and events is too large to
   *           be encoded in the bit sizes used by the list buffer
   *           implementations.
   */
  public ListBufferTransitionRelation(final AutomatonProxy aut,
                                      final EventEncoding eventEnc,
                                      final int config)
    throws OverflowException
  {
    this(aut, eventEnc, null, config);
  }

  /**
   * Creates a new transition relation from the given automaton, using the
   * given state and event encoding.
   *
   * @param aut
   *          The automaton to be encoded.
   * @param eventEnc
   *          Event encoding to define the assignment of integer codes to
   *          events in the transition buffers.
   * @param stateEnc
   *          State encoding to define the assignment of integer codes to
   *          events in the transition buffers, or <CODE>null</CODE> to
   *          use a default state encoding.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          created. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   * @throws OverflowException
   *           to indicate that the given number of states and events is too
   *           large to be encoded in the bit sizes used by the transition
   *           list buffer implementation.
   */
  public ListBufferTransitionRelation(final AutomatonProxy aut,
                                      final EventEncoding eventEnc,
                                      final StateEncoding stateEnc,
                                      final int config)
    throws OverflowException
  {
    this(aut, eventEnc, stateEnc, null, config);
  }

  /**
   * Creates a new transition relation from the given automaton, using the
   * given state and event encoding.
   * @param aut
   *          The automaton to be encoded.
   * @param eventEnc
   *          Event encoding to define the assignment of integer codes to
   *          events in the transition buffers.
   * @param stateEnc
   *          State encoding to define the assignment of integer codes to
   *          events in the transition buffers, or <CODE>null</CODE> to use a
   *          default state encoding.
   * @param dumpState
   *          Dump state to be used, or <CODE>null</CODE>. If the state
   *          encoding contains the indicated state, it is used as a reachable
   *          dump state, otherwise an additional unreachable dump state is
   *          added to the end of the state space.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          created. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   * @throws OverflowException
   *           to indicate that the given number of states and events is too
   *           large to be encoded in the bit sizes used by the transition
   *           list buffer implementation.
   */
  public ListBufferTransitionRelation(final AutomatonProxy aut,
                                      final EventEncoding eventEnc,
                                      StateEncoding stateEnc,
                                      final StateProxy dumpState,
                                      final int config)
    throws OverflowException
  {
    checkConfig(config);
    mName = aut.getName();
    mKind = aut.getKind();
    // Put events in eventEnc
    final Set<EventProxy> events = new THashSet<>(aut.getEvents());
    if (stateEnc == null) {
      stateEnc = new StateEncoding(aut);
    }
    // Copy transitions so transition list buffer constructors can sort ...
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    final List<TransitionProxy> list = new ArrayList<>(transitions);
    final int numStates = stateEnc.getNumberOfStates() + 1;
    final int numTrans = aut.getTransitions().size();
    if ((config & CONFIG_COUNT_LONG) != 0) {
      mStateBuffer = new LongStateCountBuffer(stateEnc, dumpState);
    } else {
      mStateBuffer = new IntStateBuffer(eventEnc, stateEnc, dumpState);
    }
    mEventStatus = eventEnc;
    if ((config & CONFIG_SUCCESSORS) != 0) {
      mSuccessorBuffer =
        new OutgoingTransitionListBuffer(numStates, mEventStatus, numTrans);
      mSuccessorBuffer.setUpTransitions(events, list, eventEnc, stateEnc);
    }
    if ((config & CONFIG_PREDECESSORS) != 0) {
      mPredecessorBuffer =
        new IncomingTransitionListBuffer(numStates, mEventStatus,  numTrans);
      mPredecessorBuffer.setUpTransitions(events, list, eventEnc, stateEnc);
    }
  }

  /**
   * Creates an empty transition relation. This method creates a transition
   * relation with the given number of states and event encoding, but without
   * any transitions. All states are marked reachable, with an unreachable
   * dump state added at the end. Initial states have to be set using
   * {@link #setInitial(int,boolean)}, and transitions have to be added
   * using {@link #addTransition(int,int,int)}.
   *
   * @param name
   *          A name for the new transition relation.
   * @param kind
   *          A component kind for the new transition relation.
   * @param eventEnc
   *          Event encoding to define the assignment of integer codes to
   *          events in the transition buffers.
   * @param numStates
   *          The number of states to be encoded, not including the dump
   *          state that will be added at the end.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          created. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   * @throws OverflowException
   *           to indicate that the given number of states and events is too
   *           large to be encoded in the bit sizes used by the transition
   *           list buffer implementation.
   */
  public ListBufferTransitionRelation(final String name,
                                      final ComponentKind kind,
                                      final EventEncoding eventEnc,
                                      final int numStates,
                                      final int config)
    throws OverflowException
  {
    this(name, kind, eventEnc, numStates + 1, numStates, config);
    setReachable(numStates, false);
  }

  /**
   * Creates an empty transition relation. This method creates a transition
   * relation with the given number of states and event encoding, but without
   * any transitions. All states are marked reachable, yet initial states have
   * to be set using {@link #setInitial(int,boolean)}, and transitions have to
   * be added using {@link #addTransition(int,int,int)}.
   *
   * @param name
   *          A name for the new transition relation.
   * @param kind
   *          A component kind for the new transition relation.
   * @param eventEnc
   *          Event encoding to define the assignment of integer codes to
   *          events in the transition buffers.
   * @param numStates
   *          The number of states to be encoded.
   * @param dumpIndex
   *          The index of the dump state in the new transition relation.
   *          The dump state signifies a unmarked state without outgoing
   *          transitions.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          created. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   * @throws OverflowException
   *           to indicate that the given number of states and events is too
   *           large to be encoded in the bit sizes used by the transition
   *           list buffer implementation.
   */
  public ListBufferTransitionRelation(final String name,
                                      final ComponentKind kind,
                                      final EventEncoding eventEnc,
                                      final int numStates,
                                      final int dumpIndex,
                                      final int config)
    throws OverflowException
  {
    checkConfig(config);
    mName = name;
    mKind = kind;
    if ((config & CONFIG_COUNT_LONG) != 0) {
      mStateBuffer = new LongStateCountBuffer(numStates, dumpIndex);
    } else {
      mStateBuffer = new IntStateBuffer(numStates, dumpIndex, eventEnc);
    }
    mEventStatus = eventEnc;
    if ((config & CONFIG_SUCCESSORS) != 0) {
      mSuccessorBuffer =
        new OutgoingTransitionListBuffer(numStates, eventEnc, 0);
    }
    if ((config & CONFIG_PREDECESSORS) != 0) {
      mPredecessorBuffer =
        new IncomingTransitionListBuffer(numStates, eventEnc, 0);
    }
  }

  /**
   * Creates an empty transition relation. This method creates a transition
   * relation with the given numbers of states and events, but without
   * any transitions. All states are marked reachable, with an unreachable
   * dump state added at the end. Initial states have to be set using
   * {@link #setInitial(int,boolean)}, and transitions have to be added
   * using {@link #addTransition(int,int,int)}.
   *
   * @param name
   *          The name of the new transition relation.
   * @param kind
   *          The automaton type to be recorded for the new transition
   *          relation.
   * @param numProperEvents
   *          The number of proper events (i.e., not propositions) used by the
   *          new transition relation, including tau.
   * @param numPropositions
   *          The number of propositions used by the new transition relation.
   * @param numStates
   *          The number of states to be encoded, not including the dump
   *          state that will be added at the end.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          created. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   * @throws OverflowException
   *           if given numbers of states and events are too large to be
   *           encoded in the bit sizes used by the list buffer
   *           implementations.
   */
  public ListBufferTransitionRelation(final String name,
                                      final ComponentKind kind,
                                      final int numProperEvents,
                                      final int numPropositions,
                                      final int numStates,
                                      final int config)
    throws OverflowException
  {
    this(name, kind, numProperEvents, numPropositions,
         numStates + 1, numStates, config);
    setReachable(numStates, false);
  }

  /**
   * Creates an empty transition relation. This method creates a transition
   * relation with the given numbers of states and events, but without
   * any transitions. All states are marked reachable, yet initial states have
   * to be set using {@link #setInitial(int,boolean)}, and transitions have to
   * be added using {@link #addTransition(int,int,int)}.
   *
   * @param name
   *          The name of the new transition relation.
   * @param kind
   *          The automaton type to be recorded for the new transition
   *          relation.
   * @param numProperEvents
   *          The number of proper events (i.e., not propositions) used by the
   *          new transition relation, including tau.
   * @param numPropositions
   *          The number of propositions used by the new transition relation.
   * @param numStates
   *          The number of states to be created.
   * @param dumpIndex
   *          The index of the dump state in the new transition relation.
   *          The dump state signifies a unmarked state without outgoing
   *          transitions.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          created. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   * @throws OverflowException
   *           if given numbers of states and events are too large to be
   *           encoded in the bit sizes used by the list buffer
   *           implementations.
   */
  public ListBufferTransitionRelation(final String name,
                                      final ComponentKind kind,
                                      final int numProperEvents,
                                      final int numPropositions,
                                      final int numStates,
                                      final int dumpIndex,
                                      final int config)
    throws OverflowException
  {
    assert numProperEvents > 0 :
      "ListBufferTransitionRelation needs at least one proper event (tau)!";
    checkConfig(config);
    mName = name;
    mKind = kind;
    mEventStatus =
      new DefaultEventStatusProvider(numProperEvents, numPropositions);
    mEventStatus.setProperEventStatus(EventEncoding.TAU,
                                      EventStatus.STATUS_FULLY_LOCAL);
    if ((config & CONFIG_COUNT_LONG) != 0) {
      mStateBuffer = new LongStateCountBuffer(numStates, dumpIndex);
    } else {
      mStateBuffer = new IntStateBuffer(numStates, dumpIndex, mEventStatus);
    }
    if ((config & CONFIG_SUCCESSORS) != 0) {
      mSuccessorBuffer =
        new OutgoingTransitionListBuffer(numStates ,mEventStatus, 0);
    }
    if ((config & CONFIG_PREDECESSORS) != 0) {
      mPredecessorBuffer =
        new IncomingTransitionListBuffer(numStates, mEventStatus, 0);
    }
  }

  /**
   * Creates a new transition relation that contains the same states and
   * transitions as the given transition relation. This copy constructor
   * constructs a deep copy that does not share any data structures with the
   * given transition relation.
   *
   * @param rel
   *          The transition relation to be copied.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          created in the copy. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   */
  public ListBufferTransitionRelation(final ListBufferTransitionRelation rel,
                                      final int config)
  {
    this(rel, rel.mEventStatus.clone(), config);
  }

  /**
   * Creates a new transition relation that contains the same states and
   * transitions as the given transition relation. This copy constructor
   * constructs a deep copy that does not share any data structures with the
   * given transition relation.
   *
   * @param rel
   *          The transition relation to be copied.
   * @param eventStatus
   *          The event status provider (event encoding) used for the
   *          copied transition relation. Transitions with events that are
   *          not present or marked as unused are not copied, and selfloops
   *          by events marked as selfloop-only are also suppressed.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          created in the copy. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   */
  public ListBufferTransitionRelation(final ListBufferTransitionRelation rel,
                                      final EventStatusProvider eventStatus,
                                      final int config)
  {
    this(rel,
         eventStatus,
         rel.mStateBuffer.clone(eventStatus),
         config);
  }

  /**
   * Creates a new transition relation that contains the same states and
   * transitions as the given transition relation. This copy constructor
   * constructs a deep copy that does not share any data structures with the
   * given transition relation.
   *
   * @param rel
   *          The transition relation to be copied.
   * @param eventStatus
   *          The event status provider (event encoding) used for the
   *          copied transition relation. Transitions with events that are
   *          not present or marked as unused are not copied, and selfloops
   *          by events marked as selfloop-only are also suppressed.
   * @param stateBuffer
   *          The state buffer representing the state space for the new
   *          transition relation, which is used instead of the state buffer
   *          of the transition relation to be copied. It must contain at
   *          least as many states as the transition relation to be copied.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          created in the copy. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   */
  public ListBufferTransitionRelation(final ListBufferTransitionRelation rel,
                                      final EventStatusProvider eventStatus,
                                      final AbstractStateBuffer stateBuffer,
                                      final int config)
  {
    checkConfig(config);
    mName = rel.getName();
    mKind = rel.getKind();
    mEventStatus = eventStatus;
    mStateBuffer = stateBuffer;
    final int numStates = mStateBuffer.getNumberOfStates();
    try {
      if ((config & CONFIG_SUCCESSORS) != 0) {
        mSuccessorBuffer =
          new OutgoingTransitionListBuffer(numStates, mEventStatus, 0);
        if (rel.mSuccessorBuffer != null) {
          mSuccessorBuffer.setUpTransitions(rel.mSuccessorBuffer);
        } else {
          mSuccessorBuffer.setUpTransitions(rel.mPredecessorBuffer);
        }
      }
      if ((config & CONFIG_PREDECESSORS) != 0) {
        mPredecessorBuffer =
          new IncomingTransitionListBuffer(numStates, mEventStatus, 0);
        if (rel.mPredecessorBuffer != null) {
          mPredecessorBuffer.setUpTransitions(rel.mPredecessorBuffer);
        } else {
          mPredecessorBuffer.setUpTransitions(rel.mSuccessorBuffer);
        }
      }
    } catch (final OverflowException exception) {
      // Can't have overflow because states and events have already been
      // encoded successfully in rel.
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  @Override
  public ListBufferTransitionRelation clone()
  {
    int config = 0;
    if (mSuccessorBuffer != null) {
      config |= CONFIG_SUCCESSORS;
    }
    if (mPredecessorBuffer != null) {
      config |= CONFIG_PREDECESSORS;
    }
    if ((config & CONFIG_COUNT_LONG) != 0) {
      config |= CONFIG_COUNT_LONG;
    }
    return new ListBufferTransitionRelation(this, config);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.tr.EventStatusProvider
  @Override
  public int getNumberOfProperEvents()
  {
    return mEventStatus.getNumberOfProperEvents();
  }

  @Override
  public byte getProperEventStatus(final int event)
  {
    return mEventStatus.getProperEventStatus(event);
  }

  @Override
  public void setProperEventStatus(final int event, final int status)
  {
    mEventStatus.setProperEventStatus(event, status);
  }

  @Override
  public int getNumberOfPropositions()
  {
    return mEventStatus.getNumberOfPropositions();
  }

  @Override
  public boolean isPropositionUsed(final int prop)
  {
    return mEventStatus.isPropositionUsed(prop);
  }

  @Override
  public void setPropositionUsed(final int prop, final boolean used)
  {
    mEventStatus.setPropositionUsed(prop, used);
  }

  @Override
  public int getUsedPropositions()
  {
    return mEventStatus.getUsedPropositions();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the name of this transition relation. This name will be given to any
   * automaton created from this transition relation.
   *
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding)
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding,StateEncoding)
   */
  public String getName()
  {
    return mName;
  }

  /**
   * Sets a new name for this transition relation. This name will be given to
   * any automaton created from this transition relation.
   *
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding)
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding,StateEncoding)
   */
  public void setName(final String name)
  {
    mName = name;
  }

  /**
   * Gets the kind of this transition relation. This attribute will be used
   * for any automaton created from this transition relation.
   *
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding)
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding,StateEncoding)
   */
  public ComponentKind getKind()
  {
    return mKind;
  }

  /**
   * Sets the kind of this transition relation. This attribute will be used
   * for any automaton created from this transition relation.
   *
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding)
   * @see #createAutomaton(ProductDESProxyFactory,EventEncoding,StateEncoding)
   */
  public void setKind(final ComponentKind kind)
  {
    mKind = kind;
  }


  //#########################################################################
  //# State Access
  /**
   * Returns whether this transition relation is empty.
   * @return <CODE>true</CODE> if all states are marked as unreachable,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isEmpty()
  {
    return mStateBuffer.isEmpty();
  }

  /**
   * Gets the number of states in the transition relation, including any
   * states set to be unreachable.
   */
  public int getNumberOfStates()
  {
    return mStateBuffer.getNumberOfStates();
  }

  /**
   * Gets the number of reachable states in the transition relation. A state
   * is considered reachable if its reachability flag is set.
   */
  public int getNumberOfReachableStates()
  {
    return mStateBuffer.getNumberOfReachableStates();
  }

  /**
   * Gets the total state count of this transition relation. The total
   * state count is the sum of state counts of each individual state.
   */
  public long getTotalStateCount()
  {
    long total = 0;
    for (int i = 0; i < mStateBuffer.getNumberOfReachableStates(); i++) {
      total += mStateBuffer.getStateCount(i);
    }
    return total;
  }

  /**
   * Gets the index of the dump state in this transition relation. The dump
   * state signifies a unmarked state without outgoing transitions. It is set
   * for every transition relation to provide for algorithms that redirect
   * transitions to such a state.
   */
  public int getDumpStateIndex()
  {
    return mStateBuffer.getDumpStateIndex();
  }

  /**
   * Gets the total number of markings in this transition relation. Each
   * instance of a proposition marking a reachable state counts as marking.
   * @param  countUnused  Whether unused proposition should be counted.
   *                      If <CODE>true</CODE> unused propositions are counted
   *                      as marked in all states; if <CODE>false</CODE>,
   *                      unused propositions are not counted.
   */
  public int getNumberOfMarkings(final boolean countUnused)
  {
    return mStateBuffer.getNumberOfMarkings(countUnused);
  }

  /**
   * Gets the number of reachable states marked by the given proposition in
   * this transition relation.
   * @param  prop         The proposition number to be checked.
   * @param  countUnused  Whether unused proposition should be counted.
   *                      If <CODE>true</CODE> unused propositions are counted
   *                      as marked in all states; if <CODE>false</CODE>,
   *                      unused propositions are not counted.
   */
  public int getNumberOfMarkings(final int prop, final boolean countUnused)
  {
    return mStateBuffer.getNumberOfMarkings(prop, countUnused);
  }

  /**
   * Gets the initial status of the given state.
   *
   * @return <CODE>true</CODE> if the state is an initial state,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isInitial(final int state)
  {
    return mStateBuffer.isInitial(state);
  }

  /**
   * Sets the initial status of the given state.
   *
   * @param state
   *          The ID of state to be modified.
   * @param init
   *          <CODE>true</CODE> if the state is to be ab initial state,
   *          <CODE>false</CODE> otherwise.
   */
  public void setInitial(final int state, final boolean init)
  {
    mStateBuffer.setInitial(state, init);
  }

  /**
   * Returns the state code of the first initial state in this transition
   * relation, or <CODE>-1</CODE> if the transition relation has no initial
   * state.
   */
  public int getFirstInitialState()
  {
    for (int s = 0; s < getNumberOfStates(); s++) {
      if (isInitial(s)) {
        return s;
      }
    }
    return -1;
  }

  /**
   * Gets the reachability status of the given state. Each state has a
   * reachability flag associated with it, which is used to suppress
   * unreachable states when creating an automaton from the transition
   * relation. The reachability status is not set automatically; it is to be
   * set by the user when a state is deemed unreachable.
   *
   * @see #setReachable(int,boolean) setReachable()
   * @see #checkReachability()
   * @see #createAutomaton(ProductDESProxyFactory, EventEncoding)
   * @see #createAutomaton(ProductDESProxyFactory,
   *      EventEncoding,StateEncoding)
   */
  public boolean isReachable(final int state)
  {
    return mStateBuffer.isReachable(state);
  }

  /**
   * Sets the reachability status of the given state. If a state is set to be
   * unreachable, transitions linked to the state will be removed
   * automatically, and the initial state status is removed.
   *
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
   * This method reports a state as marked if the indicated proposition is
   * not marked as used (marked by default for proposition not in the
   * automaton alphabet), or if the state is explicitly marked by the
   * proposition.
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
   * Gets a number that identifies the complete set of markings for the given
   * state.
   *
   * @param state
   *          ID of the state to be examined.
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
   *          ID of proposition identifying the marking of which is to be
   *          modified.
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
   *
   * @param state
   *          ID of the state to be modified.
   * @param markings
   *          A new marking pattern for the state. This pattern can be
   *          obtained through the methods {@link #getAllMarkings(int)
   *          getAllMarkings()}, {@link #createMarkings(TIntArrayList)
   *          createMarkings()}, or {@link #mergeMarkings(long,long)
   *          mergeMarkings()}.
   */
  public void setAllMarkings(final int state, final long markings)
  {
    mStateBuffer.setAllMarkings(state, markings);
  }

  /**
   * Adds several markings to a given state simultaneously.
   *
   * @param state
   *          ID of the state to be modified.
   * @param markings
   *          A pattern of additional markings for the state. This pattern can
   *          be obtained through the methods {@link #getAllMarkings(int)
   *          getAllMarkings()}, {@link #createMarkings(TIntArrayList)
   *          createMarkings()}, or {@link #mergeMarkings(long,long)
   *          mergeMarkings()}.
   * @return <CODE>true</CODE> if the call resulted in markings being changed,
   *         i.e., if the pattern contained a marking not already present on
   *         the state.
   */
  public boolean addMarkings(final int state, final long markings)
  {
    return mStateBuffer.addMarkings(state, markings);
  }

  /**
   * Removes several markings from a given state simultaneously.
   *
   * @param state
   *          ID of the state to be modified.
   * @param markings
   *          A pattern of markings to be removed from the state. This pattern
   *          can be obtained through the methods {@link #getAllMarkings(int)
   *          getAllMarkings()}, {@link #createMarkings(TIntArrayList)
   *          createMarkings()}, or {@link #mergeMarkings(long,long)
   *          mergeMarkings()}.
   * @return <CODE>true</CODE> if the call resulted in markings being changed,
   *         i.e., if the pattern contained a marking actually present on the
   *         state.
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
   * markings of the given source state to the given destination state. The
   * markings of the source state will not be changed, and the destination
   * state retains any markings it previously had in addition to the new ones.
   */
  public void copyMarkings(final int source, final int dest)
  {
    mStateBuffer.copyMarkings(source, dest);
  }

  /**
   * Creates a markings pattern representing an empty set of propositions.
   */
  public long createMarkings()
  {
    return mStateBuffer.createMarkings();
  }

  /**
   * Creates a markings pattern for the given propositions.
   *
   * @param props
   *          Collection of proposition IDs defining a state marking.
   * @return A number identifying the given combination of propositions.
   * @see #setAllMarkings(int,long) setAllMarkings()
   */
  public long createMarkings(final TIntArrayList props)
  {
    return mStateBuffer.createMarkings(props);
  }

  /**
   * Adds a marking to the given marking pattern.
   *
   * @param markings
   *          Marking pattern to be augmented.
   * @param prop
   *          Code of proposition to be added to pattern.
   * @return A number identifying a marking consisting of all propositions
   *         contained in the given markings, plus the the additional marking.
   * @see #mergeMarkings(long, long) mergeMarkings()
   * @see #setAllMarkings(int,long) setAllMarkings()
   */
  public long addMarking(final long markings, final int prop)
  {
    return mStateBuffer.addMarking(markings, prop);
  }

  /**
   * Checks whether the given marking pattern contains the given proposition.
   *
   * @param markings
   *          Marking pattern to be examined.
   * @param prop
   *          Code of proposition to be tested.
   * @return <CODE>true</CODE> if the marking pattern includes the given
   *         proposition, <CODE>false</CODE> otherwise.
   */
  public boolean isMarked(final long markings, final int prop)
  {
    return mStateBuffer.isMarked(markings, prop);
  }

  /**
   * Combines two marking patterns.
   *
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
   * Creates a read-only iterator for this transition relation's outgoing
   * transitions. The iterator returned is not initialised, so one of the
   * methods {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int, int)} must be called before it can
   * be used. Being a read-only iterator, the iterator returned by this method
   * does not implement the {@link TransitionIterator#remove()} method.
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
   *
   * @throws IllegalStateException
   *           if the transition relation is not configure to use an outgoing
   *           transition buffer.
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
   * the given state and event in the buffer's defined ordering. Being a
   * read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   *
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an outgoing
   *           transition buffer.
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
   * Creates a read-only iterator for this transition relation to iterate
   * over transitions associated with events with the given status flags.
   * The iterator returned is not initialised and needs to be reset using
   * the {@link TransitionIterator#resetState(int) resetState()} method.
   * Then it will produce all outgoing transitions from a given state
   * with events of the specified status.
   * @param flags
   *          Event status flags to specify the type of events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createSuccessorsReadOnlyIteratorByStatus
    (final int...flags)
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createReadOnlyIteratorByStatus(flags);
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Creates a read-only iterator for this transition relation's incoming
   * transitions. The iterator returned is not initialised, so one of the
   * methods {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int, int)} before it can be used. Being a
   * read-only iterator, it does not implement the
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
   *
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an incoming
   *           transition buffer.
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
   * the given state and event in the buffer's defined ordering. Being a
   * read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   *
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an incoming
   *           transition buffer.
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
   * Creates a read-only iterator for this transition relation to iterate
   * over transitions associated with events with the given status flags.
   * The iterator returned is not initialised and needs to be reset using
   * the {@link TransitionIterator#resetState(int) resetState()} method.
   * Then it will produce all incoming transitions to a given state
   * with events of the specified status.
   * @param flags
   *          Event status flags to specify the type of events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createPredecessorsReadOnlyIteratorByStatus
    (final int...flags)
  {
    if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createReadOnlyIteratorByStatus(flags);
    } else {
      throw createNoBufferException(CONFIG_PREDECESSORS);
    }
  }

  /**
   * Creates a read-only iterator for this transition relation that is set up
   * to iterate over the incoming or outgoing transitions associated with the
   * given state, whichever is available. The iterator returned is not
   * initialised, so one of the methods
   * {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int, int)} before it can be used. Being a
   * read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createAnyReadOnlyIterator()
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createReadOnlyIterator();
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createReadOnlyIterator();
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Creates a read-only iterator for this transition relation that is set up
   * to iterate over the incoming or outgoing transitions associated with the
   * given state, whichever is available. The iterator returned produces all
   * transitions associated with the given state in the buffer's defined
   * ordering, no matter what event they use. Being a read-only iterator, it
   * does not implement the {@link TransitionIterator#remove()} method.
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
   * given state and event, whichever is available. The iterator returned
   * produces all transitions associated with the given state and event in the
   * buffer's defined ordering. Being a read-only iterator, it does not
   * implement the {@link TransitionIterator#remove()} method.
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
   * Creates a read-only iterator for this transition relation to iterate
   * over transitions associated with events with the given status flags.
   * The iterator returned is not initialised and needs to be reset using
   * the {@link TransitionIterator#resetState(int) resetState()} method.
   * Then it will produce all outgoing or outgoing (whichever available)
   * transitions of a given state with events of the specified status.
   * @param flags
   *          Event status flags to specify the type of events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createAnyReadOnlyIteratorByStatus
    (final int...flags)
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createReadOnlyIteratorByStatus(flags);
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createReadOnlyIteratorByStatus(flags);
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Creates a read-only iterator over all transitions in this transition
   * relation. The iterator returned is set up to return the first transition
   * in this buffer after calling {@link TransitionIterator#advance()}. It
   * does not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}, and being a read-only
   * iterator, it also does not implement the
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
   * Creates a read-only iterator over all transitions with the given
   * event. The iterator returned is set up to return the first transition
   * with the given event after calling {@link TransitionIterator#advance()}.
   * It does not implement the methods
   * {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int,int)}, and being a read-only
   * iterator, it also does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createAllTransitionsReadOnlyIterator(final int event)
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
   * Creates a read-only iterator for this transition relation to iterate
   * over transitions associated with events with the given status flags.
   * The iterator returned is set up to return the first transition in
   * this buffer after calling {@link TransitionIterator#advance()}. It does
   * not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}, and
   * being a read-only iterator, it also does not implement the
   * {@link TransitionIterator#remove()} method.
   * @param flags
   *          Event status flags to specify the type of events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createAllTransitionsReadOnlyIteratorByStatus
    (final int...flags)
  {
    if (mSuccessorBuffer != null) {
      return mSuccessorBuffer.createAllTransitionsReadOnlyIteratorByStatus(flags);
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createAllTransitionsReadOnlyIteratorByStatus(flags);
    } else {
      throw createNoBufferException();
    }
  }

  /**
   * <P>Creates a read/write iterator for this transition relation's outgoing
   * transitions.</P>
   * <P>The iterator returned is not initialised, so one of the
   * methods {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int, int)} before it can be used.</P>
   * <P><STRONG>Warning.</STRONG> The transition relation should be configured to
   * use only a predecessor buffer. If both buffers are configured, the
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
   * <P>Creates a read/write iterator for this transition relation to iterate
   * over transitions associated with events with the given status flags.</P>
   * <P>The iterator returned is not initialised and needs to be reset using
   * the {@link TransitionIterator#resetState(int) resetState()} method.
   * Then it will produce all outgoing transitions from a given state
   * with events of the specified status.</P>
   * <P><STRONG>Warning.</STRONG> The transition relation should be configured to
   * use only a predecessor buffer. If both buffers are configured, the
   * predecessor buffer will be closed!</P>
   * @param flags
   *          Event status flags to specify the type of events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createSuccessorsModifyingIteratorByStatus
    (final int...flags)
  {
    if (mSuccessorBuffer != null) {
      mPredecessorBuffer = null;
      return mSuccessorBuffer.createModifyingIteratorByStatus(flags);
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * <P>Creates a read/write iterator for this transition relation's incoming
   * transitions.</P>
   * <P>The iterator returned is not initialised, so one of the
   * methods {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int, int)} before it can be used.</P>
   * <P><STRONG>Warning.</STRONG> The transition relation should be configured to
   * use only a predecessor buffer. If both buffers are configured, the
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
   * <P>Creates a read/write iterator for this transition relation to iterate
   * over transitions associated with events with the given status flags.</P>
   * <P>The iterator returned is not initialised and needs to be reset using
   * the {@link TransitionIterator#resetState(int) resetState()} method.
   * Then it will produce all incoming transitions to a given state
   * with events of the specified status.</P>
   * <P><STRONG>Warning.</STRONG> The transition relation should be configured to
   * use only a predecessor buffer. If both buffers are configured, the
   * successor buffer will be closed!</P>
   * @param flags
   *          Event status flags to specify the type of events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createPredecessorsModifyingIteratorByStatus
    (final int...flags)
  {
    if (mPredecessorBuffer != null) {
      mSuccessorBuffer = null;
      return mPredecessorBuffer.createModifyingIteratorByStatus(flags);
    } else {
      throw createNoBufferException(CONFIG_PREDECESSORS);
    }
  }

  /**
   * <P>
   * Creates a read/write iterator over all transitions in this transition
   * relation.
   * </P>
   * <P>
   * The iterator returned is set up to return the first transition in this
   * buffer after calling {@link TransitionIterator#advance()}. It does not
   * implement the methods {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int,int)}.
   * </P>
   * <P>
   * <STRONG>Warning.</STRONG> The transition relation should be configured to
   * use only one transition buffer. If both buffers are configured, the
   * predecessor buffer will be closed!
   * </P>
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
   * <P>The iterator returned is set up to return the first transition with
   * the given event after calling {@link TransitionIterator#advance()}. It
   * does not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}.</P>
   * <P><STRONG>Warning.</STRONG> The transition relation should be configured to
   * use only one transition buffer. If both buffers are configured, the
   * predecessor buffer will be closed!</P>
   */
  public TransitionIterator createAllTransitionsModifyingIterator(final int event)
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
   * <P>Creates a read/write iterator for this transition relation to iterate
   * over transitions associated with events with the given status flags.</P>
   * <P>The iterator returned is set up to return the first transition in this
   * buffer after calling {@link TransitionIterator#advance()}. It does not
   * implement the methods {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int,int)}.</P>
   * <P><STRONG>Warning.</STRONG> The transition relation should be configured to
   * use only one transition buffer. If both buffers are configured, the
   * predecessor buffer will be closed!</P>
   * @param flags
   *          Event status flags to specify the type of events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createAllTransitionsModifyingIteratorByStatus
    (final int...flags)
  {
    if (mSuccessorBuffer != null) {
      mPredecessorBuffer = null;
      return mSuccessorBuffer.createAllTransitionsModifyingIteratorByStatus(flags);
    } else if (mPredecessorBuffer != null) {
      return mPredecessorBuffer.createAllTransitionsModifyingIteratorByStatus(flags);
    } else {
      throw createNoBufferException();
    }
  }

  /**
   * Obtains the tau-closure of the successors of this transition relation.
   * @param limit
   *          The maximum number of transitions that can be stored. If the
   *          number of transitions already in the transition relation plus
   *          the number of computed tau transitions exceeds the limit,
   *          precomputation is aborted and transitions will be produced on
   *          the fly by iterators. A limit of&nbsp;0 forces the tau closure
   *          always to be computed on the fly.
   * @return A {@link TauClosure} object, which can be used to obtain a
   *         {@link TransitionIterator} over the tau-closure of the successor
   *         transition relation.
   */
  public TauClosure createSuccessorsTauClosure(final int limit)
  {
    if (mSuccessorBuffer != null) {
      return new TauClosure(mSuccessorBuffer, limit);
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Obtains a local-event closure over successors of this transition
   * relation with a specific event type.
   * @param limit
   *          The maximum number of transitions that can be stored. If the
   *          number of transitions already in the transition relation plus
   *          the number of computed tau transitions exceeds the limit,
   *          precomputation is aborted and transitions will be produced on
   *          the fly by iterators. It limit of&nbsp;0 forces the tau closure
   *          always to be computed on the fly.
   * @param flags
   *          Status flags defining the events considered as local
   *          (i.e., tau) by this tau-closure. The arguments are specified
   *          in the same way as as passed to the {@link
   *          StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @return A {@link TauClosure} object, which can be used to obtain a
   *         {@link TransitionIterator} over the tau-closure of the
   *         successor transition relation.
   */
  public TauClosure createSuccessorsClosure(final int limit,
                                            final int... flags)
  {
    if (mSuccessorBuffer != null) {
      final TransitionIterator iter =
        mSuccessorBuffer.createReadOnlyIteratorByStatus(flags);
      return new TauClosure(mSuccessorBuffer, iter, limit);
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Obtains the tau-closure of the predecessors of this transition relation.
   * @param limit
   *          The maximum number of transitions that can be stored. If the
   *          number of transitions already in the transition relation plus
   *          the number of computed tau transitions exceeds the limit,
   *          precomputation is aborted and transitions will be produced on
   *          the fly by iterators. It limit of&nbsp;0 forces the tau closure
   *          always to be computed on the fly.
   * @return A {@link TauClosure} object, which can be used to obtain a
   *         {@link TransitionIterator} over the tau-closure of the
   *         predecessor transition relation.
   */
  public TauClosure createPredecessorsTauClosure(final int limit)
  {
    if (mPredecessorBuffer != null) {
      return new TauClosure(mPredecessorBuffer, limit);
    } else {
      throw createNoBufferException(CONFIG_PREDECESSORS);
    }
  }

  /**
   * Obtains a local-event closure over predecessors of this transition
   * relation with a specific event type.
   * @param limit
   *          The maximum number of transitions that can be stored. If the
   *          number of transitions already in the transition relation plus
   *          the number of computed tau transitions exceeds the limit,
   *          precomputation is aborted and transitions will be produced on
   *          the fly by iterators. It limit of&nbsp;0 forces the tau closure
   *          always to be computed on the fly.
   * @param flags
   *          Status flags defining the events considered as local
   *          (i.e., tau) by this tau-closure. The arguments are specified
   *          in the same way as as passed to the {@link
   *          StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @return A {@link TauClosure} object, which can be used to obtain a
   *         {@link TransitionIterator} over the tau-closure of the
   *         successor transition relation.
   */
  public TauClosure createPredecessorsClosure(final int limit,
                                              final int... flags)
  {
    if (mPredecessorBuffer != null) {
      final TransitionIterator iter =
        mPredecessorBuffer.createReadOnlyIteratorByStatus(flags);
      return new TauClosure(mPredecessorBuffer, iter, limit);
    } else {
      throw createNoBufferException(CONFIG_PREDECESSORS);
    }
  }

  /**
   * Returns whether the given state is a deadlock state.
   * A deadlock state is a state that is not marked and has no outgoing
   * transitions. This method uses the successor transition buffer to
   * check for outgoing transitions and can only be called if the transition
   * relation is configured for successors.
   * @param state
   *          The state to be checked.
   * @param prop
   *          The proposition to determine whether a state is marked.
   *          If the transition relation does not use this proposition,
   *          then there are no deadlock states; otherwise states marked
   *          with this proposition cannot be deadlock states.
   * @return <CODE>true</CODE> if the state is not marked and has no
   *         outgoing transitions.
   */
  public boolean isDeadlockState(final int state, final int prop)
  {
    if (prop < 0 || isMarked(state, prop)) {
      return false;
    } else if (mSuccessorBuffer != null) {
      return !mSuccessorBuffer.hasTransitions(state);
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Checks whether this transition relation represents a deterministic
   * automaton. A transition relation is deterministic if it has at most
   * one initial state, and if there exists at most one successor state
   * for any given source state and event. Note that this method treats the
   * silent event ({@link EventEncoding#TAU}) as an ordinary event, so a
   * transition relation with silent transitions may be reported as
   * deterministic.
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
      final TIntHashSet keys = new TIntHashSet(numStates);
      final TransitionIterator iter =
        mPredecessorBuffer.createAllTransitionsReadOnlyIterator();
      while (iter.advance()) {
        final int state = iter.getCurrentSourceState();
        final int event = iter.getCurrentEvent();
        final int key = mPredecessorBuffer.makeKey(state, event);
        if (!keys.add(key)) {
          return false;
        }
      }
    } else {
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
    }
    return true;
  }


  //#########################################################################
  //# Direct Access to State and Transition Buffers
  public AbstractStateBuffer getStateBuffer()
  {
    return mStateBuffer;
  }

  /**
   * Gets the successor transition buffer of this transition relation,
   * or <CODE>null</CODE> if not configured.
   */
  public OutgoingTransitionListBuffer getSuccessorBuffer()
  {
    return mSuccessorBuffer;
  }

  /**
   * Gets the predecessor transition buffer of this transition relation,
   * or <CODE>null</CODE> if not configured.
   */
  public IncomingTransitionListBuffer getPredecessorBuffer()
  {
    return mPredecessorBuffer;
  }


  //#########################################################################
  //# Transition Modifications
  /**
   * Adds a transition to this transition relation. The new transition is
   * inserted in a defined ordering in the predecessor and/or successor
   * buffers.
   *
   * @param source
   *          The ID of the source state of the new transition.
   * @param event
   *          The ID of the event of the new transition.
   * @param target
   *          The ID of the target state of the new transition.
   * @return <CODE>true</CODE> if a transition was added, i.e., if it was not
   *         already present in the buffer; <CODE>false</CODE> otherwise.
   */
  public boolean addTransition(final int source, final int event,
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
   * Adds several transitions to one target state to this transition relation.
   * The new transitions are inserted in a defined ordering in the predecessor
   * and/or successor buffers.
   *
   * @param sources
   *          The IDs of the source states of the new transitions.
   * @param event
   *          The ID of the event of the new transitions.
   * @param target
   *          The ID of the target state of the new transitions.
   * @return <CODE>true</CODE> if at least one transition was added;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean addTransitions(final TIntArrayList sources, final int event,
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
   * relation. The new transitions are inserted in a defined ordering in the
   * predecessor and/or successor buffers.
   *
   * @param source
   *          The ID of the source state of the new transitions.
   * @param event
   *          The ID of the event of the new transitions.
   * @param targets
   *          The IDs of the target states of the new transitions.
   * @return <CODE>true</CODE> if at least one transition was added;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean addTransitions(final int source, final int event,
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
   *
   * @param source
   *          The ID of the source state of the transition to be removed.
   * @param event
   *          The ID of the event of the transition to be removed.
   * @param target
   *          The ID of the target state of the transition to be removed.
   * @return <CODE>true</CODE> if a transition was removed, i.e., if it was
   *         actually present in the buffer; <CODE>false</CODE> otherwise.
   */
  public boolean removeTransition(final int source, final int event,
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
   * Removes all transitions associated with the given state. This method
   * removes all transitions indexed under the given state. Depending on the
   * buffer configuration, this does not necessarily remove all transitions
   * linked to the state, only those that are readily accessible.
   *
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
   * Removes all outgoing transitions associated with the given source state.
   *
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an outgoing
   *           transition buffer.
   */
  public boolean removeOutgoingTransitions(final int source)
  {
    if (mSuccessorBuffer != null) {
      boolean removed = false;
      final TransitionIterator iter =
        mSuccessorBuffer.createModifyingIterator(source);
      while (iter.advance()) {
        if (mPredecessorBuffer != null) {
          final int event = iter.getCurrentEvent();
          final int target = iter.getCurrentToState();
          mPredecessorBuffer.removeTransition(target, event, source);
        }
        iter.remove();
        removed = true;
      }
      return removed;
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Removes all outgoing transitions associated with the given source state
   * and event.
   *
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an outgoing
   *           transition buffer.
   */
  public boolean removeOutgoingTransitions(final int source, final int event)
  {
    if (mSuccessorBuffer != null) {
      boolean removed = false;
      final TransitionIterator iter =
        mSuccessorBuffer.createModifyingIterator(source, event);
      while (iter.advance()) {
        if (mPredecessorBuffer != null) {
          final int target = iter.getCurrentToState();
          mPredecessorBuffer.removeTransition(target, event, source);
        }
        iter.remove();
        removed = true;
      }
      return removed;
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Removes all incoming transitions associated with the given target state.
   *
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an incoming
   *           transition buffer.
   */
  public boolean removeIncomingTransitions(final int target)
  {
    if (mPredecessorBuffer != null) {
      boolean removed = false;
      final TransitionIterator iter =
        mPredecessorBuffer.createModifyingIterator(target);
      while (iter.advance()) {
        if (mSuccessorBuffer != null) {
          final int event = iter.getCurrentEvent();
          final int source = iter.getCurrentToState();
          mSuccessorBuffer.removeTransition(source, event, target);
        }
        iter.remove();
        removed = true;
      }
      return removed;
    } else {
      throw createNoBufferException(CONFIG_PREDECESSORS);
    }
  }

  /**
   * Removes all incoming transitions associated with the given target state
   * and event.
   *
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an incoming
   *           transition buffer.
   */
  public boolean removeIncomingTransitions(final int target, final int event)
  {
    if (mPredecessorBuffer != null) {
      boolean removed = false;
      final TransitionIterator iter =
        mPredecessorBuffer.createModifyingIterator(target, event);
      while (iter.advance()) {
        if (mSuccessorBuffer != null) {
          final int source = iter.getCurrentToState();
          mSuccessorBuffer.removeTransition(source, event, target);
        }
        iter.remove();
        removed = true;
      }
      return removed;
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
        throw createNoBufferException(CONFIG_SUCCESSORS);
      }
      copyMarkings(from, to);
      mSuccessorBuffer.copyTransitions(from, to, mPredecessorBuffer);
    }
  }

  /**
   * <P>
   * Copies all outgoing transitions from each of the given 'from' states
   * to the given 'to' state.
   * </P>
   * <P>
   * This method copies all markings and regular transitions from the 'from'
   * states to the 'to' state. It suppresses duplicates, and ordering is
   * preserved such that outgoing transitions originally associated with the
   * 'from' state appear earlier in the resultant list.
   * </P>
   *
   * @param from
   *          List of IDs of state containing transitions and markings
   *          to be copied.
   * @param to
   *          ID of state receiving transitions and markings.
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an outgoing
   *           transition buffer.
   */
  public void copyOutgoingTransitions(final TIntArrayList from, final int to)
  {
    if (!from.isEmpty()) {
      if (mSuccessorBuffer == null) {
        throw createNoBufferException(CONFIG_SUCCESSORS);
      }
      for (int i = 0; i < from.size(); i++) {
        copyMarkings(from.get(i), to);
      }
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
   * 'to' state. Furthermore, if the 'from' state is an initial state, the
   * 'to' state will be set to an initial state as well. This method
   * suppresses duplicates, and ordering is preserved such that incoming
   * transitions originally associated with the 'from' state appear earlier in
   * the resultant list.
   * </P>
   *
   * @param from
   *          ID of state containing transitions and initial state status to
   *          be copied.
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
   * 'to' state. Furthermore, if the 'from' state is an initial state, the
   * 'to' state will be set to an initial state as well. This method
   * suppresses duplicates, and ordering is preserved such that incoming
   * transitions originally associated with the 'from' state appear earlier in
   * the resultant list. After copying, all incoming transitions are removed
   * from the 'from' state, which then is marked as unreachable.
   * </P>
   * <P>
   * <STRONG>Warning.</STRONG> This method closes the outgoing transition
   * buffer, if it is open.
   * </P>
   *
   * @param from
   *          ID of state containing transitions and initial state status to
   *          be moved.
   * @param to
   *          ID of state receiving transitions and initial state status.
   * @throws IllegalStateException
   *           if the transition relation is not configured to use an incoming
   *           transition buffer.
   */
  public void moveIncomingTransitions(final int from, final int to)
  {
    copyIncomingTransitions(from, to);
    setReachable(from, false);
  }

  /**
   * Determines whether the given event is globally disabled in this
   * transition relation.
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
   * @param event
   *          The ID of the event to be tested.
   * @return <CODE>true</CODE> if the given event is selflooped in every
   *         state, and appears on no other transitions.
   */
  public boolean isProperSelfloopEvent(final int event)
  {
    final TransitionIterator iter;
    if (mSuccessorBuffer != null) {
      iter = mSuccessorBuffer.createReadOnlyIterator();
    } else if (mPredecessorBuffer != null) {
      iter = mPredecessorBuffer.createReadOnlyIterator();
    } else {
      throw createNoBufferException();
    }
    final int numStates = getNumberOfStates();
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
   * Determines whether the given event is selflooped in all non-deadlock
   * states of this transition relation. A deadlock state is a state that is
   * not marked and has no outgoing transitions. This method uses the
   * successor transition buffer to check for outgoing transitions and can
   * only be called if the transition relation is configured for successors.
   * @param event
   *          The ID of the event to be tested.
   * @param prop
   *          The proposition to determine whether a state is marked.
   *          If the transition relation does not use this proposition,
   *          then there are no deadlock states; otherwise states marked
   *          with this proposition cannot be deadlock states.
   * @return <CODE>true</CODE> if the given event appears on at least one
   *         transition and is selflooped in every non-deadlock state,
   *         and appears on no other transitions.
   */
  public boolean isProperSelfloopEvent(final int event, final int prop)
  {
    if (mSuccessorBuffer != null) {
      final TransitionIterator iter = mSuccessorBuffer.createReadOnlyIterator();
      final int numStates = getNumberOfStates();
      boolean hasTransition = false;
      for (int state = 0; state < numStates; state++) {
        if (isReachable(state) && !isDeadlockState(state, prop)) {
          iter.reset(state, event);
          if (iter.advance()) {
            do {
              if (iter.getCurrentToState() != state) {
                return false;
              }
            } while (iter.advance());
            hasTransition = true;
          } else {
            return false;
          }
        }
      }
      return hasTransition;
    } else {
      throw createNoBufferException(CONFIG_SUCCESSORS);
    }
  }

  /**
   * Removes the given event from this transition relation. This method
   * removes the given event including all its transitions from the
   * transition relation. All associated transitions are deleted,
   * and the event is marked as unused.
   *
   * @param event
   *          The ID of the event to be removed.
   */
  public void removeEvent(final int event)
  {
    final byte status = mEventStatus.getProperEventStatus(event);
    if (EventStatus.isUsedEvent(status)) {
      mEventStatus.setProperEventStatus(event,
                                        status | EventStatus.STATUS_UNUSED);
      if (mSuccessorBuffer != null) {
        mSuccessorBuffer.removeEventTransitions(event);
      }
      if (mPredecessorBuffer != null) {
        mPredecessorBuffer.removeEventTransitions(event);
      }
    }
  }

  /**
   * Replaces an event by another. This method replaces all transitions with
   * the given old event ID by transitions with the given new event ID. Both
   * events must be present in the transition relation, and will remain
   * present after this operation. Any new transitions with the new event ID
   * are inserted after any transitions already present in the transition
   * buffers.
   *
   * @param oldID
   *          The ID of the old event to be replaced.
   * @param newID
   *          The ID of the new event replacing the old event.
   */
  public void replaceEvent(final int oldID, final int newID)
  {
    if (mSuccessorBuffer != null) {
      mSuccessorBuffer.replaceEvent(oldID, newID);
    }
    if (mPredecessorBuffer != null) {
      mPredecessorBuffer.replaceEvent(oldID, newID);
    }
    final byte oldStatus = mEventStatus.getProperEventStatus(oldID);
    if (EventStatus.isUsedEvent(oldStatus)) {
      final byte newStatus = mEventStatus.getProperEventStatus(newID);
      mEventStatus.setProperEventStatus
        (newID, newStatus & ~EventStatus.STATUS_UNUSED);
    }
  }


  //#########################################################################
  //# Buffer Maintenance
  /**
   * Gets the current configuration of this transition relation. The
   * configuration determines whether transitions can be accessed easily in
   * forwards or backwards direction.
   *
   * @return One of {@link #CONFIG_SUCCESSORS}, {@link #CONFIG_PREDECESSORS},
   *         or {@link #CONFIG_ALL}.
   */
  public int getConfiguration()
  {
    int config = 0;
    if (mSuccessorBuffer != null) {
      config = CONFIG_SUCCESSORS;
    }
    if (mPredecessorBuffer != null) {
      config |= CONFIG_PREDECESSORS;
    }
    return config;
  }

  /**
   * Reconfigures the current set of state and transition buffers.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          used from now on. Should be a combination of the flags
   *          {@link #CONFIG_SUCCESSORS}, {@link #CONFIG_PREDECESSORS},
   *          {@link #CONFIG_COUNT_LONG}, and {@link #CONFIG_COUNT_OFF}.
   *          If neither of the bits {@link #CONFIG_SUCCESSORS} or
   *          {@link #CONFIG_PREDECESSORS} is set, the transition buffer
   *          configuration is unchanged, otherwise it is changed precisely
   *          to the indicated combination.
   *          If neither of the bits {@link #CONFIG_COUNT_LONG} or
   *          {@link #CONFIG_COUNT_OFF} is set, the state buffer is unchanged,
   *          otherwise it is changed to the indicated type. It is an error
   *          of both {@link #CONFIG_COUNT_LONG} and {@link #CONFIG_COUNT_OFF}
   *          are set.
   */
  public void reconfigure(final int config)
  {
    final int numStates = getNumberOfStates();

    // Change transition buffers
    if ((config & CONFIG_ALL) != 0) {
      try {
        if (mSuccessorBuffer == null && (config & CONFIG_SUCCESSORS) != 0) {
          if (mPredecessorBuffer != null) {
            mSuccessorBuffer =
              new OutgoingTransitionListBuffer(numStates, mEventStatus);
            mSuccessorBuffer.setUpTransitions(mPredecessorBuffer);
          } else {
            throw createNoBufferException(CONFIG_PREDECESSORS);
          }
        }
        if (mPredecessorBuffer == null && (config & CONFIG_PREDECESSORS) != 0) {
          if (mSuccessorBuffer != null) {
            mPredecessorBuffer =
              new IncomingTransitionListBuffer(numStates, mEventStatus);
            mPredecessorBuffer.setUpTransitions(mSuccessorBuffer);
          } else {
            throw createNoBufferException(CONFIG_SUCCESSORS);
          }
        }
      } catch (final OverflowException exception) {
        // Can't have overflow because states and events have already been
        // encoded successfully.
        throw new WatersRuntimeException(exception);
      }
      if ((config & CONFIG_SUCCESSORS) == 0) {
        mSuccessorBuffer = null;
      } else if ((config & CONFIG_PREDECESSORS) == 0) {
        mPredecessorBuffer = null;
      }
    }

    // Change the type of the state buffer, if necessary.
    if ((config & CONFIG_COUNT_LONG) != 0) {
      if (!(mStateBuffer instanceof LongStateCountBuffer)) {
        // Normal buffer -> State-count buffer.
        mStateBuffer = new LongStateCountBuffer(mStateBuffer, mEventStatus);
      }
    } else if ((config & CONFIG_COUNT_OFF) != 0) {
      if (!(mStateBuffer instanceof IntStateBuffer)) {
        // State-count buffer -> Normal buffer.
        mStateBuffer = new IntStateBuffer(mStateBuffer, mEventStatus);
      }
    }
  }

  /**
   * Reverses this transition relation. This method reverses all transitions
   * by swapping their source and target. Initial states and markings are not
   * affected by this method. Reversing is implemented by simple swapping the
   * incoming and outgoing transition buffers, so the buffer configuration is
   * also swapped by this method.
   */
  public void reverse()
  {
    final OutgoingTransitionListBuffer newSucc =
      mPredecessorBuffer == null ? null
        : new OutgoingTransitionListBuffer(mPredecessorBuffer);
    final IncomingTransitionListBuffer newPred =
      mSuccessorBuffer == null ? null
        : new IncomingTransitionListBuffer(mSuccessorBuffer);
    mSuccessorBuffer = newSucc;
    mPredecessorBuffer = newPred;
  }

  /**
   * Resets this transition relation to use a different state set.
   * This method replaces the state set with a new state set of the
   * indicated size, and clears all transitions from the transition relation.
   * @param numStates
   *          The new number of states. All states will be marked as reachable.
   * @param dumpIndex
   *          The index of the dump state in the new transition relation.
   *          The dump state signifies a unmarked state without outgoing
   *          transitions.
   * @param numTrans
   *          Estimated number of transitions.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          used from now on. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   */
  public void reset(final int numStates,
                    final int dumpIndex,
                    final int numTrans,
                    final int config)
    throws OverflowException
  {
    if ((config & CONFIG_COUNT_LONG) != 0) {
      mStateBuffer = new LongStateCountBuffer(numStates, dumpIndex);
    } else {
      mStateBuffer = new IntStateBuffer(numStates, dumpIndex, mEventStatus);
    }
    reset(numTrans, config);
  }

  /**
   * Resets this transition relation to use a different state set.
   * This method replaces the state set with a new state set of the
   * indicated size, and clears all transitions from the transition relation.
   * @param numStates
   *          The new number of states. All states will be marked as reachable
   *          and an unreachable dump state will be added at the end.
   * @param numTrans
   *          Estimated number of transitions.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          used from now on. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   */
  public void reset(final int numStates,
                    final int numTrans,
                    final int config)
    throws OverflowException
  {
    if ((config & CONFIG_COUNT_LONG) != 0) {
      mStateBuffer = new LongStateCountBuffer(numStates);
    } else {
      mStateBuffer = new IntStateBuffer(numStates, mEventStatus);
    }
    reset(numTrans, config);
  }

  /**
   * Clears all transitions from the transition relation.
   * This method clears all transition buffers and replaces them by
   * new empty buffers.
   * @param numTrans
   *          Estimated new number of transitions.
   * @param config
   *          Configuration flags defining which transition buffers are to be
   *          used from now on. Should be one of {@link #CONFIG_SUCCESSORS},
   *          {@link #CONFIG_PREDECESSORS}, or {@link #CONFIG_ALL}.
   */
  public void reset(final int numTrans, final int config)
    throws OverflowException
  {
    checkConfig(config);
    final int numStates = getNumberOfStates();
    if ((config & CONFIG_SUCCESSORS) != 0) {
      mSuccessorBuffer =
        new OutgoingTransitionListBuffer(numStates, mEventStatus, numTrans);
    } else {
      mSuccessorBuffer = null;
    }
    if ((config & CONFIG_PREDECESSORS) != 0) {
      mPredecessorBuffer =
        new IncomingTransitionListBuffer(numStates, mEventStatus, numTrans);
    } else {
      mPredecessorBuffer = null;
    }
  }


  //#########################################################################
  //# Automaton Simplification
  /**
   * Attempts to simplify the automaton by removing all tau selfloops. If this
   * results in the tau event being disabled, the tau event is marked as
   * unused. Tau events are recognised by their standard code
   * {@link EventEncoding#TAU}. Note that the creation of tau selfloops is
   * suppressed by most transition relation methods, but it may still be
   * useful to call removeTauSelfloops() to mark the tau event unused if
   * possible.
   *
   * @return <CODE>true</CODE> if all transitions with the tau event were
   *         selfloops and have been removed, <CODE>false</CODE> otherwise.
   */
  public boolean removeTauSelfLoops()
  {
    boolean removedSome = false;
    for (int e = 0; e < getNumberOfProperEvents(); e++) {
      final byte status = mEventStatus.getProperEventStatus(e);
      if ((status & EventStatus.STATUS_UNUSED) == 0 &&
          (status & EventStatus.STATUS_SELFLOOP_ONLY) != 0) {
        boolean removable = false;
        if (mSuccessorBuffer != null) {
          removable = mSuccessorBuffer.removeTauSelfloops(e);
        }
        if (mPredecessorBuffer != null) {
          removable = mPredecessorBuffer.removeTauSelfloops(e);
        }
        if (removable) {
          mEventStatus.setProperEventStatus
            (e, status | EventStatus.STATUS_UNUSED);
          removedSome = true;
        }
      }
    }
    return removedSome;
  }

  /**
   * Attempts to simplify the automaton by removing redundant selfloop events.
   * This method searches for any non-tau events that appear only as selfloops
   * and are selflooped in all states of the transition relation, marks such
   * events as unused, and removes the selfloops from the transition relation.
   *
   * @return <CODE>true</CODE> if at least one event was removed,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean removeProperSelfLoopEvents()
  {
    final int numEvents = getNumberOfProperEvents();
    boolean modified = false;
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = mEventStatus.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status) && isProperSelfloopEvent(e)) {
        removeEvent(e);
        modified = true;
      }
    }
    return modified;
  }

  /**
   * <P>Attempts to simplify the automaton by removing redundant selfloop
   * events while considering deadlock states.</P>
   * <P>This method searches for any non-tau events that appear on at least
   * one transition and appear only as selfloops and are selflooped in all
   * non-deadlock states of the transition relation. These events are marked
   * as unused and removed from the transition relation.</P>
   * <P>A deadlock state is a state that is not marked and has no outgoing
   * transitions.</P>
   * @param prop
   *          The proposition to determine whether a state is marked.
   *          If the transition relation does not use this proposition,
   *          then there are no deadlock states; otherwise states marked
   *          with this proposition cannot be deadlock states.
   * @return <CODE>true</CODE> if at least one event was removed,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean removeProperSelfLoopEvents(final int prop)
  {
    if (!isPropositionUsed(prop)) {
      return removeProperSelfLoopEvents();
    }
    final int numEvents = getNumberOfProperEvents();
    boolean modified = false;
    if (mSuccessorBuffer != null) {
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final byte status = mEventStatus.getProperEventStatus(e);
        if (EventStatus.isUsedEvent(status) && isProperSelfloopEvent(e, prop)) {
          removeEvent(e);
          modified = true;
        }
      }
    } else if (mPredecessorBuffer != null) {
      final int numStates = getNumberOfStates();
      final BitSet nonDeadlockStates = new BitSet(numStates);
      final TransitionIterator iterAll =
        mPredecessorBuffer.createAllTransitionsReadOnlyIterator();
      while (iterAll.advance()) {
        final int s = iterAll.getCurrentSourceState();
        nonDeadlockStates.set(s);
      }
      final TransitionIterator iter =
        mPredecessorBuffer.createReadOnlyIterator();
      events:
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final byte status = mEventStatus.getProperEventStatus(e);
        if (EventStatus.isUsedEvent(status)) {
          boolean hasTransition = false;
          for (int s = 0; s < numStates; s++) {
            if (isReachable(s) &&
                (nonDeadlockStates.get(s) || isMarked(s, prop))) {
              iter.reset(s, e);
              if (iter.advance()) {
                do {
                  if (iter.getCurrentToState() != s) {
                    continue events;
                  }
                } while (iter.advance());
                hasTransition = true;
              } else {
                continue events;
              }
            }
          }
          if (hasTransition) {
            removeEvent(e);
            modified = true;
          }
        }
      }
    } else {
      throw createNoBufferException();
    }
    return modified;
  }

  /**
   * Removes all unreachable transitions. A transition is considered
   * unreachable if its source or target state is marked as unreachable. This
   * methods visits every transition in the buffer, checks its reachability,
   * and removes it if unreachable.
   *
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   * @see #setReachable(int, boolean) setReachable()
   */
  public boolean removeUnreachableTransitions()
  {
    boolean modified = false;
    final TransitionIterator iter = createAllTransitionsModifyingIterator();
    while (iter.advance()) {
      if (!isReachable(iter.getCurrentSourceState())
          || !isReachable(iter.getCurrentTargetState())) {
        iter.remove();
        modified = true;
      }
    }
    return modified;
  }

  /**
   * Removes all transitions to deadlock states.
   * A deadlock state is a state that is not marked and has no outgoing
   * transitions. This method uses the successor transition buffer to
   * check for outgoing transitions and can only be called if the transition
   * relation is configured for successors.
   * @param prop
   *          The proposition to determine whether a state is marked.
   *          If the transition relation does not use this proposition,
   *          then there are no deadlock states; otherwise states marked
   *          with this proposition cannot be deadlock states.
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   * @see #isDeadlockState(int, int) isDeadlockState()
   */
  public boolean removeDeadlockStateTransitions(final int prop)
  {
    if (prop < 0) {
      return false;
    } else {
      boolean removed = false;
      final TransitionIterator iter = createAllTransitionsModifyingIterator();
      while (iter.advance()) {
        final int target = iter.getCurrentTargetState();
        if (isDeadlockState(target, prop)) {
          iter.remove();
          removed = true;
        }
      }
      if (removed) {
        final int numStates = getNumberOfStates();
        for (int s = 0; s < numStates; s++) {
          if (isDeadlockState(s, prop)) {
            setReachable(s, false);
          }
        }
      }
      return removed;
    }
  }

  /**
   * Removes all transitions to the dump state.
   * This method removes all transitions to the transition relation's
   * designated dump state, and marks the dump state as unreachable.
   * @return <CODE>true</CODE> if at least one transition was removed,
   *         <CODE>false</CODE> otherwise.
   * @see #getDumpStateIndex()
   */
  public boolean removeDumpStateTransitions()
  {
    final int dump = getDumpStateIndex();
    final TransitionIterator iter = createAllTransitionsModifyingIterator();
    boolean removed = false;
    while (iter.advance()) {
      if (iter.getCurrentTargetState() == dump) {
        iter.remove();
        removed = true;
      }
    }
    setReachable(dump, false);
    return removed;
  }

  /**
   * Ensures all propositions are marked as used. All propositions marked
   * as unused are marked as used and added to all states by this method.
   * @return <CODE>true</CODE> if at least one proposition was changed,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean addRedundantPropositions()
  {
    return mStateBuffer.addRedundantPropositions();
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
   * Repartitions the states of this transition relation.
   * <p>
   * This method implements state merging by automaton quotient. It is used
   * to merge states after a partition has been obtained through a
   * {@link TransitionRelationSimplifier}.
   *
   * @param partition The partition to be imposed, or <CODE>null</CODE>.
   *
   * @throws OverflowException when the available space to store the state
   *                           count becomes insufficient.
   */
  public void merge(final TRPartition partition) throws OverflowException
  {
    if (partition != null) {
      int dumpClass = partition.getClassCode(getDumpStateIndex());
      if (dumpClass < 0) {
        dumpClass = partition.getUnusedClass();
      }
      final int newSize = partition.getNumberOfClasses();
      final AbstractStateBuffer newStateBuffer;
      final int extraStates;
      // Make a new state buffer, same as the original type.
      if (dumpClass >= 0) {
        newStateBuffer = mStateBuffer.clone(newSize, dumpClass, mEventStatus);
        extraStates = 0;
      } else {
        newStateBuffer = mStateBuffer.clone(newSize, mEventStatus);
        extraStates = 1;
      }
      // Make new transition list buffers.
      if (mSuccessorBuffer != null) {
        mSuccessorBuffer.merge(partition, extraStates);
      }
      if (mPredecessorBuffer != null) {
        mPredecessorBuffer.merge(partition,  extraStates);
      }
      // Merge
      int stateID = 0;
      for (final int[] clazz : partition.getClasses()) {
        if (clazz == null) {
          newStateBuffer.setReachable(stateID, false);
        } else {
          boolean initial = false;
          long markings = 0;
          long count = 0;
          /*
           * Initial  --- Applicable to both buffer types.
           * Markings --- Only for normal state buffer.
           * Count    --- Only for state-count buffer.
           *
           * Since all type-specific methods are made compatible --- for
           * example, all the marking methods for state-count buffer does
           * nothing, and similarly for normal state buffers --- there is
           * no need to test which type of buffer it is.
           */
          for (final int state : clazz) {
            initial |= mStateBuffer.isInitial(state);
            markings |= mStateBuffer.getAllMarkings(state);
            count += mStateBuffer.getStateCount(state);
          }
          newStateBuffer.setInitial(stateID, initial);
          newStateBuffer.setAllMarkings(stateID, markings);
          newStateBuffer.setStateCount(stateID, count);
        }
        stateID++;
      }
      mStateBuffer = newStateBuffer;
    }
  }

  /**
   * Re-evaluates reachability. This method does a full reachability search of
   * the transition relation, and resets the reachability status of all states
   * according to the result. If any states are found to be unreachable,
   * transitions attached to these states are removed.
   * @param  config  Preferred output configuration, either {@link
   *                 #CONFIG_SUCCESSORS}, {@link #CONFIG_PREDECESSORS},
   *                 or {@link #CONFIG_ALL}. If the transition
   *                 relation is not configured to use an outgoing
   *                 transition buffer, it will be reconfigured to the
   *                 given configuration plus an outgoing
   *                 transition buffer.
   * @return <CODE>true</CODE> if the reachability status of at least one
   *         state was changed, <CODE>false</CODE> otherwise.
   */
  public boolean checkReachability(final int config)
  {
    if (mSuccessorBuffer == null) {
      reconfigure(config | CONFIG_SUCCESSORS);
    }
    return checkReachability();
  }

  /**
   * Re-evaluates reachability. This method does a full reachability search of
   * the transition relation, and resets the reachability status of all states
   * according to the result. If any states are found to be unreachable,
   * transitions attached to these states are removed.
   *
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
      final TIntStack stack = new TIntArrayStack();
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
        } else if (!newstatus) {
          modified |= removeTransitions(s);
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
   * Creates a state encoding for this transition relation. This method
   * creates a {@link StateEncoding} with a new {@link StateProxy} objects for
   * all states marked as reachable.
   * @param eventEnc
   *          Event encoding defining what propositions are to be used to
   *          encode markings.
   */
  public StateEncoding createStateEncoding(final EventEncoding eventEnc)
  {
    return mStateBuffer.createStateEncoding(eventEnc);
  }

  /**
   * Creates an automaton from this transition relation. This method creates
   * an {@link AutomatonProxy} object that contains all events not marked as
   * unused and all reachable states of the transition relation, and links
   * them with all the transitions stored.
   *
   * @param factory
   *          Factory used for proxy creation.
   * @param eventEnc
   *          Event encoding defining what events are to be used for the
   *          integer codes in the transition relation.
   */
  public AutomatonProxy createAutomaton(final ProductDESProxyFactory factory,
                                        final EventEncoding eventEnc)
  {
    return createAutomaton(factory, eventEnc, null);
  }

  /**
   * Creates an automaton from this transition relation. This method creates
   * an {@link AutomatonProxy} object that contains all events not marked as
   * unused and all reachable states of the transition relation, and links
   * them with all the transitions stored.
   *
   * @param factory
   *          Factory used for proxy creation.
   * @param eventEnc
   *          Event encoding defining what events are to be used for the
   *          integer codes in the transition relation.
   * @param stateEnc
   *          State encoding to be used. If non-null and non-empty, the
   *          encoding must define state objects for the codes of all
   *          reachable states with the desired initial state attributes and
   *          markings. Any reachable states defined in the encoding will be
   *          used in the output automaton. If non-null and empty, the method
   *          will add to the state encoding the states created for the output
   *          automaton and their assignment to state codes in the transition
   *          relation.
   */
  public AutomatonProxy createAutomaton(final ProductDESProxyFactory factory,
                                        final EventEncoding eventEnc,
                                        StateEncoding stateEnc)
  {
    assert getNumberOfProperEvents() == eventEnc.getNumberOfProperEvents();
    final int numEvents = eventEnc.getNumberOfEvents();
    final int numProps = eventEnc.getNumberOfPropositions();
    final Collection<EventProxy> events =
      new ArrayList<EventProxy>(numEvents);
    for (int e = 0; e < eventEnc.getNumberOfProperEvents(); e++) {
      final byte status = mEventStatus.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        final EventProxy event = eventEnc.getProperEvent(e);
        if (event != null) {
          events.add(event);
        }
      }
    }
    for (int p = 0; p < numProps; p++) {
      if (mEventStatus.isPropositionUsed(p)) {
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
              if (isPropositionUsed(p) && isMarked(s, p)) {
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
  @Override
  public String toString()
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    dump(printer);
    return writer.toString();
  }

  public void dump(final PrintWriter printer)
  {
    mStateBuffer.dump(printer);
    printer.println();
    if (mSuccessorBuffer != null) {
      mSuccessorBuffer.dump(printer);
    } else if (mPredecessorBuffer != null) {
      mPredecessorBuffer.dump(printer);
    } else {
      printer.print("{" + ProxyTools.getShortClassName(this) +
                    ": no buffer configured.}");
    }
    printer.println();
  }

  public void logSizes(final Logger logger)
  {
    if (logger.isDebugEnabled()) {
      int numEvents = 0;
      for (int e = EventEncoding.NONTAU; e < getNumberOfProperEvents(); e++) {
        final byte status = getProperEventStatus(e);
        if (EventStatus.isUsedEvent(status)) {
          numEvents++;
        }
      }
      int numProps = 0;
      for (int p = 0; p < getNumberOfPropositions(); p++) {
        if (isPropositionUsed(p)) {
          numProps++;
        }
      }
      logger.debug
        (getNumberOfReachableStates() + " states, " +
         getNumberOfTransitions() + " transitions, " +
         numEvents + " proper events, " +
         getNumberOfMarkings(false) + "(" + numProps + ") markings.");
    }
  }

  /**
   * Checks whether the transition relation's event status provider
   * is the same object as the argument.
   * @throws AssertionError to indicate that the transition relation
   *         does not have the expected event status provider.
   */
  public void checkEventStatusProvider(final EventStatusProvider expected)
  {
    assert mEventStatus == expected;
  }

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

  public void saveModule(final String filename)
  {
    try {
      final ProductDESProxyFactory factory =
        ProductDESElementFactory.getInstance();
      final int numEvents = getNumberOfProperEvents();
      final int numProps = getNumberOfPropositions();
      final Collection<EventProxy> events =
        new ArrayList<EventProxy>(numEvents + numProps);
      if (numProps == 1) {
        final String name = EventDeclProxy.DEFAULT_MARKING_NAME;
        final EventProxy prop =
          factory.createEventProxy(name, EventKind.PROPOSITION);
        events.add(prop);
      } else {
        for (int p = 0; p < numProps; p++) {
          final EventProxy prop =
            factory.createEventProxy("p" + p, EventKind.PROPOSITION);
          events.add(prop);
        }
      }
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final byte status = getProperEventStatus(e);
        final EventKind kind = EventStatus.isControllableEvent(status) ?
          EventKind.CONTROLLABLE : EventKind.UNCONTROLLABLE;
        final boolean local = EventStatus.isLocalEvent(status);
        final EventProxy event =
          factory.createEventProxy("e" + e, kind, !local);
        events.add(event);
      }
      final KindTranslator translator = IdenticalKindTranslator.getInstance();
      final byte status = mEventStatus.getProperEventStatus(EventEncoding.TAU);
      final EventProxy tau;
      if (EventStatus.isUsedEvent(status)) {
        tau = factory.createEventProxy("tau", EventKind.UNCONTROLLABLE, false);
      } else {
        tau = null;
      }
      final EventEncoding enc = new EventEncoding(events, translator, tau);
      final AutomatonProxy aut = createAutomaton(factory, enc);
      MarshallingTools.saveModule(aut, filename);
    } catch (final OverflowException exception) {
      throw new WatersRuntimeException(exception);
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
    return new IllegalStateException(ProxyTools.getShortClassName(this) +
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

  private final EventStatusProvider mEventStatus;
  private AbstractStateBuffer mStateBuffer;
  private OutgoingTransitionListBuffer mSuccessorBuffer;
  private IncomingTransitionListBuffer mPredecessorBuffer;


  //#########################################################################
  //# Class Constants
  /**
   * Configuration setting specifying that the transition relation is to use
   * an outgoing transition buffer.
   */
  public static final int CONFIG_SUCCESSORS = 0x01;
  /**
   * Configuration setting specifying that the transition relation is to use
   * an incoming transition buffer.
   */
  public static final int CONFIG_PREDECESSORS = 0x02;
  /**
   * Configuration setting specifying that the transition relation is to use
   * a {@link IntStateBuffer}, which supports marking but not state counts.
   * This is the default when creating a new transition relation.
   */
  public static final int CONFIG_COUNT_OFF = 0x04;
  /**
   * Configuration setting specifying that the transition relation is to use
   * a {@link LongStateCountBuffer}, which ignores the markings and
   * propositions, whilst retaining the state count.
   */
  public static final int CONFIG_COUNT_LONG = 0x08;
  /**
   * Configuration setting specifying that the transition relation is to use
   * both outgoing and incoming transition buffers.
   */
  public static final int CONFIG_ALL = CONFIG_SUCCESSORS
                                     | CONFIG_PREDECESSORS;
  /**
   * Configuration setting specifying that the transition relation is to use
   * an outgoing transition buffer, and is to count the states.
   */
  public static final int CONFIG_S_C = CONFIG_SUCCESSORS | CONFIG_COUNT_LONG;
}
