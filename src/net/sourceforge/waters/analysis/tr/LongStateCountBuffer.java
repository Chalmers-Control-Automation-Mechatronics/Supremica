//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;


/**
 * A data structure that compactly stores the status information of the states
 * in a transition relation.
 * <p>
 * The state buffer considers a state space represented by <code>long</code>
 * state code ranging from&nbsp;0 up to the number of states minus&nbsp;1.
 * For each state code, it stores the following status information.
 * Each state can be designated as <I>initial</I> and/or <I>reachable</I>,
 * and can have a state-count value from 1 to 2<sup>63</sup>-1.
 *
 * @see StateEncoding
 *
 * @author Robi Malik, Roger Su
 */

public class LongStateCountBuffer extends AbstractStateBuffer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new state buffer.
   * <p>
   * This constructor creates a new state buffer
   * with the states in the given encoding. If the state encoding contains
   * a <CODE>null</CODE> state, it is used as a reachable dump state,
   * otherwise an additional unreachable dump state is added to the end of
   * the state space.
   * @param  stateEnc        State encoding that defines the assignment of
   *                         state codes for the states in the buffer.
   */
  public LongStateCountBuffer(final StateEncoding stateEnc)
  {
    this(stateEnc, null);
  }

  /**
   * Creates a new state buffer.
   * @param  stateEnc        State encoding that defines the assignment of
   *                         state codes for the states in the buffer.
   * @param  dumpState       Dump state to be used, or <CODE>null</CODE>.
   *                         If the state encoding contains the indicated
   *                         state, it is used as a reachable dump state,
   *                         otherwise an additional unreachable dump state
   *                         is added to the end of the state space.
   */
  public LongStateCountBuffer(final StateEncoding stateEnc,
                              final StateProxy dumpState)
  {
    super(stateEnc, dumpState);
    final int numStates = stateEnc.getNumberOfStates();
    // Initialise the array of states.
    if (getDumpStateIndex() >= 0) {
      mStateInfo = new long[numStates];
    } else {
      setDumpStateIndex(numStates);
      mStateInfo = new long[numStates + 1];
    }
    // Handle the initial and reachable flags, and ensure that
    // the state counts should all be 1 instead of the default 0.
    for (int s = 0; s < numStates; s++) {
      final StateProxy state = stateEnc.getState(s);
      if (state != null) {
        long info = TAG_REACHABLE;
        if (state.isInitial()) {
          info |= TAG_INITIAL;
        }
        mStateInfo[s] = info;
      }
      mStateInfo[s] += 1; // Initial state count is one.
    }
  }

  /**
   * Creates a new empty state buffer.
   * <p>
   * This constructor allocates a new state buffer with the given number
   * of states. States are initially marked as reachable, while all other
   * attributes and markings of the states are initialised to be
   * <CODE>false</CODE>. An additional unreachable dump state is added
   * at the end.
   * @param  size       The number of states in the new buffer.
   */
  public LongStateCountBuffer(final int size)
  {
    this(size + 1, size);
    mStateInfo[getDumpStateIndex()] = 0;
  }

  /**
   * Creates a new empty state buffer.
   * <p>
   * This constructor allocates a new state buffer with the given number
   * of states. States are initially marked as reachable, while all other
   * attributes and markings of the states are initialised to be
   * <CODE>false</CODE>. An additional unreachable dump state is added
   * at the end.
   * @param  size       The number of states in the new buffer.
   * @param eventStatus Event status provider to determine the number of
   *                    propositions and which propositions are used.
   */
  public LongStateCountBuffer(final int size, final EventStatusProvider eventStatus)
  {
    this(size);
  }

  /**
   * Creates a new empty state buffer.
   * <p>
   * This constructor allocates a new state buffer with the given number
   * of states. States are initially marked as reachable, while all other
   * attributes and markings of the states are initialised to
   * <CODE>false</CODE>. Also, the count of each state is initialised to 1.
   * @param  size       The number of states in the new buffer.
   * @param  dumpIndex  The index of the dump state in the new buffer.
   *                    The dump state signifies a unmarked state without
   *                    outgoing transitions. It must be specified for
   *                    every state buffer to provide for algorithms that
   *                    redirect transitions to such a state.
   */
  public LongStateCountBuffer(final int size, final int dumpIndex)
  {
    setDumpStateIndex(dumpIndex);
    mStateInfo = new long[size];
    Arrays.fill(mStateInfo, TAG_REACHABLE | 1L);
  }

  /**
   * Creates a new empty state buffer.
   * This constructor allocates a new state buffer with the given number
   * of states. States are initially marked as reachable, while all other
   * attributes and markings of the states are initialised to be
   * <CODE>false</CODE>.
   * @param  size       The number of states in the new buffer.
   * @param  dumpIndex  The index of the dump state in the new buffer.
   *                    The dump state signifies a unmarked state without
   *                    outgoing transitions. It must be specified for
   *                    every state buffer to provide for algorithms that
   *                    redirect transitions to such a state.
   * @param eventStatus Event status provider to determine the number of
   *                    propositions and which propositions are used.
   */
  public LongStateCountBuffer(final int size, final int dumpIndex,
                              final EventStatusProvider eventStatus)
  {
    this(size, dumpIndex);
  }

  /**
   * Creates a new state buffer that is an identical copy of the given
   * state buffer. This copy constructor constructs a deep copy that does
   * not share any data structures with the given state buffer.
   * @param  buffer     The state buffer to be copied from.
  */
  public LongStateCountBuffer(final LongStateCountBuffer buffer)
  {
    setDumpStateIndex(buffer.getDumpStateIndex());
    final int size = buffer.getNumberOfStates();
    mStateInfo = Arrays.copyOf(buffer.mStateInfo, size);
  }

  /**
   * Creates a new state-count buffer corresponding to a given normal
   * state buffer.
   * <p>
   * Since the new state buffer is a state-count buffer and contains no
   * markings, the markings of the original normal state buffer will be
   * discarded.
   *
   * @param buffer     The state buffer to be copied from.
   * @param propStatus Event status provider to determine the number of
   *                   propositions and which propositions are used.
   */
  public LongStateCountBuffer(final AbstractStateBuffer buffer,
                              final EventStatusProvider propStatus)
  {
    setDumpStateIndex(buffer.getDumpStateIndex());
    final int size = buffer.getNumberOfStates();
    mStateInfo = new long[size];
    for (int i = 0; i < size; i++) {
      // Initial flags
      if (buffer.isInitial(i)) {
        mStateInfo[i] |= TAG_INITIAL;
      }
      // Reachable flags
      if (buffer.isReachable(i)) {
        mStateInfo[i] |= TAG_REACHABLE;
      }
      // Default state count
      mStateInfo[i] += 1;
    }
  }


  //#########################################################################
  //# Simple Access
  @Override
  public AbstractStateBuffer clone(final EventStatusProvider propStatus)
  {
    return new LongStateCountBuffer(this);
  }

  @Override
  public AbstractStateBuffer clone(final int size,
                                   final EventStatusProvider propStatus)
  {
    return new LongStateCountBuffer(size);
  }

  @Override
  public AbstractStateBuffer clone(final int size, final int dumpIndex,
                                   final EventStatusProvider propStatus)
  {
    return new LongStateCountBuffer(size, dumpIndex);
  }

  /**
   * Gets the number of states in the buffer.
   */
  @Override
  public int getNumberOfStates()
  {
    return mStateInfo.length;
  }

  /**
   * Determines whether the given state is initial.
   */
  @Override
  public boolean isInitial(final int state)
  {
    return (mStateInfo[state] & TAG_INITIAL) != 0;
  }

  /**
   * Sets the initial status of the given state.
   */
  @Override
  public void setInitial(final int state, final boolean value)
  {
    if (value) {
      mStateInfo[state] |= TAG_INITIAL;
    } else {
      mStateInfo[state] &= ~TAG_INITIAL;
    }
  }

  /**
   * Determines whether the given state is reachable.
   * Reachability is merely handled as a flag by this class,
   * to be interpreted by the users.
   */
  @Override
  public boolean isReachable(final int state)
  {
    return (mStateInfo[state] & TAG_REACHABLE) != 0;
  }

  /**
   * Sets the reachability status of the given state.
   * Reachability is merely handled as a flag by this class,
   * to be interpreted by the users.
   */
  @Override
  public void setReachable(final int state, final boolean value)
  {
    if (value) {
      mStateInfo[state] |= TAG_REACHABLE;
    } else {
      mStateInfo[state] &= ~TAG_REACHABLE;
    }
  }

  /**
   * Checks whether a state is marked.
   * This method reports a state as marked if the indicated proposition is
   * not marked as used (marked by default for proposition not in the
   * automaton alphabet), or if the state is explicitly marked by the
   * proposition.
   * @param  state   ID of the state to be tested.
   * @param  prop    ID of the marking proposition to be tested.
   */
  @Override
  public boolean isMarked(final int state, final int prop)
  {
    return true;
  }

  /**
   * Gets the total number of markings in this state buffer.
   * Each instance of a proposition marking a reachable state counts
   * as marking.
   * @param  countUnused  Whether unused proposition should be counted.
   *                      If <CODE>true</CODE> unused propositions are counted
   *                      as marked in all states; if <CODE>false</CODE>,
   *                      unused propositions are not counted.
   */
  @Override
  public int getNumberOfMarkings(final boolean countUnused)
  {
    return 0;
  }

  /**
   * Gets the number of reachable states marked by the given proposition in
   * this state buffer.
   * @param  prop         The proposition number to be checked.
   * @param  countUnused  Whether unused proposition should be counted.
   *                      If <CODE>true</CODE> unused propositions are counted
   *                      as marked in all states; if <CODE>false</CODE>,
   *                      unused propositions considered as not marked.
   */
  @Override
  public int getNumberOfMarkings(final int prop, final boolean countUnused)
  {
    return 0;
  }

  /**
   * Gets a number that identifies the complete set of markings for the
   * given state.
   * @param  state   ID of the state to be examined.
   * @return A marking pattern for the state. The only guarantee about the
   *         number returned is that two states with the same set of markings
   *         will always have the same marking patterns, and states with
   *         different sets of markings will always have different marking
   *         patterns. Only propositions marked as used are considered.
   * @see #setAllMarkings(int,long) setAllMarkings()
   */
  @Override
  public long getAllMarkings(final int state)
  {
    return 0;
  }

  /**
   * Sets the marking of a state.
   * @param  state    ID of the state to be modified.
   * @param  prop     ID of the marking proposition to be modified.
   * @param  value    <CODE>true</CODE> if the state is to be marked,
   *                  <CODE>false</CODE> if it is to be unmarked.
   */
  @Override
  public void setMarked(final int state, final int prop, final boolean value)
  {

  }

  /**
   * Sets all markings for the given state simultaneously.
   * @param  state    ID of the state to be modified.
   * @param  markings A new marking pattern for the state. This pattern
   *                  can be obtained through the methods
   *                  {@link #getAllMarkings(int) getAllMarkings()},
   *                  {@link #createMarkings(TIntArrayList) createMarkings()},
   *                  or {@link #mergeMarkings(long,long) mergeMarkings()}.
   */
  @Override
  public void setAllMarkings(final int state, final long markings)
  {

  }

  /**
   * Adds several markings to a given state simultaneously.
   * @param  state    ID of the state to be modified.
   * @param  markings A pattern of additional markings for the state. This
   *                  pattern can be obtained through the methods
   *                  {@link #getAllMarkings(int) getAllMarkings()},
   *                  {@link #createMarkings(TIntArrayList) createMarkings()},
   *                  or {@link #mergeMarkings(long,long) mergeMarkings()}.
   * @return <CODE>true</CODE> if the call resulted in markings being changed,
   *         i.e., if the pattern contained a marking not already present
   *         on the state.
   */
  @Override
  public boolean addMarkings(final int state, final long markings)
  {
    if ((mStateInfo[state] & markings) != markings) {
      mStateInfo[state] |= markings;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Removes several markings from a given state simultaneously.
   * @param  state    ID of the state to be modified.
   * @param  markings A pattern of markings to be removed from the state.
   *                  This pattern can be obtained through the methods
   *                  {@link #getAllMarkings(int) getAllMarkings()},
   *                  {@link #createMarkings(TIntArrayList) createMarkings()},
   *                  or {@link #mergeMarkings(long,long) mergeMarkings()}.
   * @return <CODE>true</CODE> if the call resulted in markings being changed,
   *         i.e., if the pattern contained a marking actually present
   *         on the state.
   */
  @Override
  public boolean removeMarkings(final int state, final long markings)
  {
    if ((mStateInfo[state] & markings) != 0) {
      mStateInfo[state] &= ~markings;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Removes all markings from the given state.
   */
  @Override
  public void clearMarkings(final int state)
  {

  }

  /**
   * Copies markings from one state to another. This method adds all the
   * markings of the given source state to the given destination state.
   * The markings of the source state will not be changed, and the destination
   * state retains any markings it previously had in addition to the new ones.
   */
  @Override
  public void copyMarkings(final int source, final int dest)
  {
    mStateInfo[dest] |= (mStateInfo[source] & ~TAG_ALL);
  }

  /**
   * Ensures all propositions are marked as used. All propositions marked
   * as unused are marked as used and added to all states by this method.
   * @return <CODE>true</CODE> if at least one proposition was changed,
   *         <CODE>false</CODE> otherwise.
   */
  @Override
  public boolean addRedundantPropositions()
  {
    return false;
  }

  /**
   * Checks for each proposition whether is appears on all reachable states,
   * and if so, removes the proposition by marking it as unused.
   * @return <CODE>true</CODE> if at least one proposition was removed,
   *         <CODE>false</CODE> otherwise.
   */
  @Override
  public boolean removeRedundantPropositions()
  {
    return false;
  }


  //#########################################################################
  //# Advanced Access
  /**
   * Returns whether this state buffer represents an empty state set.
   * @return <CODE>true</CODE> if all states in the buffer are marked
   *         as unreachable, <CODE>false</CODE> otherwise.
   */
  @Override
  public boolean isEmpty()
  {
    for (final long tags : mStateInfo) {
      if ((tags & TAG_REACHABLE) != 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the number of states currently marked as reachable in this state
   * buffer.
   */
  @Override
  public int getNumberOfReachableStates()
  {
    int count = 0;
    for (final long tags : mStateInfo) {
      if ((tags & TAG_REACHABLE) != 0) {
        count++;
      }
    }
    return count;
  }

  /**
   * Creates a state encoding from this state buffer. This method creates a
   * {@link StateEncoding} with a new {@link StateProxy} objects for all
   * states marked as reachable in the encoding.
   * @param eventEnc
   *          Event encoding defining what propositions are to be used to
   *          encode markings.
   */
  @Override
  public StateEncoding createStateEncoding(final EventEncoding eventEnc)
  {
    final int numProps = eventEnc.getNumberOfPropositions();
    final int numStates = getNumberOfStates();
    final StateProxy[] states = new StateProxy[numStates];
    final TLongObjectHashMap<Collection<EventProxy>> markingsMap =
      new TLongObjectHashMap<Collection<EventProxy>>();
    int code = 0;
    for (int s = 0; s < numStates; s++) {
      if (isReachable(s)) {
        final boolean init = isInitial(s);
        final long markings = getAllMarkings(s);
        Collection<EventProxy> props = markingsMap.get(markings);
        if (props == null) {
          props = new ArrayList<EventProxy>(numProps);
          for (int p = 0; p < numProps; p++) {
            if (isMarked(s, p)) {
              final EventProxy prop = eventEnc.getProposition(p);
              if (prop != null) {
                props.add(prop);
              }
            }
          }
          markingsMap.put(markings, props);
        }
        final StateProxy state = new MemStateProxy(code++, init, props);
        states[s] = state;
      }
    }
    return new StateEncoding(states);
  }

  //#########################################################################
  //# Simple Access: State Count
  /**
   * Gets the state count of a state in this buffer.
   *
   * @param state ID of the state.
   * @return the count of the specified state.
   */
  @Override
  public long getStateCount(final int state)
  {
    return mStateInfo[state] & ~TAG_ALL;
  }

  /**
   * Sets the state count of a state in this buffer.
   *
   * @param state ID of the state.
   * @param count the count of the specified state.
   */
  @Override
  public void setStateCount(final int state, final long count)
    throws OverflowException
  {
    if ((count & TAG_ALL) == 0) {
      // Remove the old data.
      mStateInfo[state] &= TAG_ALL;
      // Add the new data.
      mStateInfo[state] |= count;
    } else {
      // Potential problem: the given count may be too large.
      throw new OverflowException();
    }
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

  @Override
  public void dump(final PrintWriter printer)
  {
    printer.print('{');
    int last = -1;
    for (int s = 0; s < mStateInfo.length; s++) {
      final long info = mStateInfo[s] & ~TAG_REACHABLE;
      if (info != 0) {
        if (last >= 0) {
          printer.print(", ");
        }
        last = s;
        if ((info & TAG_INITIAL) != 0) {
          printer.print("->");
        }
        printer.print(s);
        final long count = info & ~TAG_ALL;
        printer.print(count);
      }
    }
    if (last < mStateInfo.length - 1) {
      if (last < 0) {
        printer.print("0");
      }
      printer.print(" ... ");
      printer.print(mStateInfo.length - 1);
    }
    printer.print('}');
  }


  //#########################################################################
  //# Data Members
  private final long[] mStateInfo;


  //#########################################################################
  //# Class Constants
  private static final long TAG_INITIAL = 0x8000000000000000L;
  private static final long TAG_REACHABLE = 0x4000000000000000L;
  private static final long TAG_ALL = TAG_INITIAL | TAG_REACHABLE;

}
