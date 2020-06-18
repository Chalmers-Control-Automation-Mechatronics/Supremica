//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import java.io.PrintWriter;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * An abstract data structure that compactly stores the status information
 * of states in a transition relation.
 * <p>
 * This superclass supports the following two subclasses:
 * <ul>
 * <li>{@link IntStateBuffer} which stores the states along with their
 * markings in the 32 bits of an <code>integer</code>;</li>
 * <li>{@link LongStateCountBuffer} which uses a <code>long</code> to
 * store the states along with their state counts, but ignores all the
 * markings.</li>
 * </ul>
 *
 * @see StateEncoding
 *
 * @author Robi Malik, Roger Su
 */

public abstract class AbstractStateBuffer
{

  //#########################################################################
  //# Constructors
  public AbstractStateBuffer()
  {

  }

  /**
   * Creates a new state buffer. This constructor creates a new state buffer
   * with the states in the given encoding. If the state encoding contains
   * a <CODE>null</CODE> state, it is used as a reachable dump state,
   * otherwise an additional unreachable dump state is added to the end of
   * the state space.
   * @param  stateEnc        State encoding that defines the assignment of
   *                         state codes for the states in the buffer.
   */
  public AbstractStateBuffer(final StateEncoding stateEnc)
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
  public AbstractStateBuffer(final StateEncoding stateEnc,
                             final StateProxy dumpState)
  {
    final int numStates = stateEnc.getNumberOfStates();
    mDumpStateIndex = -1;
    for (int s = 0; s < numStates; s++) {
      if (stateEnc.getState(s) == dumpState) {
        mDumpStateIndex = s;
        break;
      }
    }
  }

  /**
   * Creates a new empty state buffer.
   * This constructor allocates a new state buffer with the given number
   * of states. States are initially marked as reachable, while all other
   * attributes and markings of the states are initialised to be
   * <CODE>false</CODE>. An additional unreachable dump state is added
   * at the end.
   * @param  size       The number of states in the new buffer.
   * @param  propStatus Event status provider to determine the number of
   *                    propositions and which propositions are used.
   */
  public AbstractStateBuffer(final int size,
                             final EventStatusProvider propStatus)
  {

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
   * @param  propStatus Event status provider to determine the number of
   *                    propositions and which propositions are used.
   */
  public AbstractStateBuffer(final int size,
                        final int dumpIndex,
                        final EventStatusProvider propStatus)
  {

  }


  //#########################################################################
  //# Simple Access
  public abstract AbstractStateBuffer clone(EventStatusProvider propStatus);

  public abstract AbstractStateBuffer clone(final int size,
                                            EventStatusProvider propStatus);

  public abstract AbstractStateBuffer clone(final int size,
                                            final int dumpIndex,
                                            EventStatusProvider propStatus);

  /**
   * Gets the number of states in the buffer.
   */
  public abstract int getNumberOfStates();

  /**
   * Gets the index of the dump state in this state buffer. The dump state
   * signifies a unmarked state without outgoing transitions. It is set
   * for every state buffer to provide for algorithms that redirect
   * transitions to such a state.
   */
  public int getDumpStateIndex()
  {
    return mDumpStateIndex;
  }

  public void setDumpStateIndex(final int i)
  {
    mDumpStateIndex = i;
  }

  /**
   * Determines whether the given state is initial.
   */
  public abstract boolean isInitial(final int state);

  /**
   * Sets the initial status of the given state.
   */
  public abstract void setInitial(final int state, final boolean value);

  /**
   * Determines whether the given state is reachable.
   * Reachability is merely handled as a flag by this class,
   * to be interpreted by the users.
   */
  public abstract boolean isReachable(final int state);

  /**
   * Sets the reachability status of the given state.
   * Reachability is merely handled as a flag by this class,
   * to be interpreted by the users.
   */
  public abstract void setReachable(final int state, final boolean value);


  //#########################################################################
  //# Simple Access: Markings
  /**
   * Checks whether a state is marked.
   * This method reports a state as marked if the indicated proposition is
   * not marked as used (marked by default for proposition not in the
   * automaton alphabet), or if the state is explicitly marked by the
   * proposition.
   * @param  state   ID of the state to be tested.
   * @param  prop    ID of the marking proposition to be tested.
   */
  public abstract boolean isMarked(final int state, final int prop);

  /**
   * Checks whether the given marking pattern contains the given proposition.
   * @param  markings  Marking pattern to be examined.
   * @param  prop      Code of proposition to be tested.
   * @return <CODE>true</CODE> if the marking pattern includes the given
   *         proposition, <CODE>false</CODE> otherwise.
   */
  public boolean isMarked(final long markings, final int prop)
  {
    return (markings & (1L << prop)) != 0;
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
  public abstract int getNumberOfMarkings(final boolean countUnused);

  /**
   * Gets the number of reachable states marked by the given proposition in
   * this state buffer.
   * @param  prop         The proposition number to be checked.
   * @param  countUnused  Whether unused proposition should be counted.
   *                      If <CODE>true</CODE> unused propositions are counted
   *                      as marked in all states; if <CODE>false</CODE>,
   *                      unused propositions considered as not marked.
   */
  public abstract int getNumberOfMarkings(final int prop, final boolean countUnused);

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
  public abstract long getAllMarkings(final int state);

  /**
   * Adds a marking to a given marking pattern.
   * @param  markings  Marking pattern to be augmented.
   * @param  prop      Code of proposition to be added to pattern.
   * @return A number identifying a marking consisting of all propositions
   *         contained in the given markings, plus the the additional marking.
   * @see #mergeMarkings(long, long)
   * @see #setAllMarkings(int,long) setAllMarkings()
   */
  public long addMarking(final long markings, final int prop)
  {
    return markings | (1L << prop);
  }

  /**
   * Sets the marking of a state.
   * @param  state    ID of the state to be modified.
   * @param  prop     ID of the marking proposition to be modified.
   * @param  value    <CODE>true</CODE> if the state is to be marked,
   *                  <CODE>false</CODE> if it is to be unmarked.
   */
  public abstract void setMarked(final int state, final int prop, final boolean value);

  /**
   * Sets all markings for the given state simultaneously.
   * @param  state    ID of the state to be modified.
   * @param  markings A new marking pattern for the state. This pattern
   *                  can be obtained through the methods
   *                  {@link #getAllMarkings(int) getAllMarkings()},
   *                  {@link #createMarkings(TIntArrayList) createMarkings()},
   *                  or {@link #mergeMarkings(long,long) mergeMarkings()}.
   */
  public abstract void setAllMarkings(final int state, final long markings);

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
  public abstract boolean addMarkings(final int state, final long markings);

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
  public abstract boolean removeMarkings(final int state, final long markings);

  /**
   * Removes all markings from the given state.
   */
  public abstract void clearMarkings(final int state);

  /**
   * Copies markings from one state to another. This method adds all the
   * markings of the given source state to the given destination state.
   * The markings of the source state will not be changed, and the destination
   * state retains any markings it previously had in addition to the new ones.
   */
  public abstract void copyMarkings(final int source, final int dest);

  /**
   * Creates markings pattern representing an empty set of propositions.
   */
  public long createMarkings()
  {
    return 0;
  }

  /**
   * Creates markings pattern for the given propositions.
   * @param  props    Collection of proposition IDs defining a state marking.
   * @return A number identifying the given combination of propositions.
   * @see #setAllMarkings(int,long) setAllMarkings()
   */
  public long createMarkings(final TIntArrayList props)
  {
    int result = 0;
    for (int p = 0; p < props.size(); p++) {
      result |= 1 << props.get(p);
    }
    return result;
  }

  /**
   * Combines two marking patterns.
   * @return A number identifying a marking consisting of all propositions
   *         contained in one of the two input marking patterns.
   * @see #addMarking(long, int)
   * @see #setAllMarkings(int,long) setAllMarkings()
   */
  public long mergeMarkings(final long markings1, final long markings2)
  {
    return markings1 | markings2;
  }

  /**
   * Ensures all propositions are marked as used. All propositions marked
   * as unused are marked as used and added to all states by this method.
   * @return <CODE>true</CODE> if at least one proposition was changed,
   *         <CODE>false</CODE> otherwise.
   */
  public abstract boolean addRedundantPropositions();

  /**
   * Checks for each proposition whether is appears on all reachable states,
   * and if so, removes the proposition by marking it as unused.
   * @return <CODE>true</CODE> if at least one proposition was removed,
   *         <CODE>false</CODE> otherwise.
   */
  public abstract boolean removeRedundantPropositions();


  //#########################################################################
  //# Simple Access: State Count
  /**
   * Gets the state count of a particular state in this buffer.
   *
   * @param state ID of the particular state.
   * @return the count of the specified state.
   */
  public abstract long getStateCount(final int state);

  /**
   * Sets the state count of a particular state in this buffer.
   *
   * @param state ID of the particular state.
   * @param count the count of the specified state.
   */
  public abstract void setStateCount(final int state, final long count)
    throws OverflowException;


  //#########################################################################
  //# Advanced Access
  /**
   * Returns whether this state buffer represents an empty state set.
   * @return <CODE>true</CODE> if all states in the buffer are marked
   *         as unreachable, <CODE>false</CODE> otherwise.
   */
  public abstract boolean isEmpty();

  /**
   * Gets the number of states currently marked as reachable in this state
   * buffer.
   */
  public abstract int getNumberOfReachableStates();

  /**
   * Creates a state encoding from this state buffer. This method creates a
   * {@link StateEncoding} with a new {@link StateProxy} objects for all
   * states marked as reachable in the encoding.
   * @param eventEnc
   *          Event encoding defining what propositions are to be used to
   *          encode markings.
   */
  public abstract StateEncoding createStateEncoding(final EventEncoding eventEnc);


  //#########################################################################
  //# Debugging
  public abstract void dump(PrintWriter printer);

  //#########################################################################
  //# Data Members
  private int mDumpStateIndex;

}
