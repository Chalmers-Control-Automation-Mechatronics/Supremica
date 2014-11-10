//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   StateBuffer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * A data structure that stores status information for states in a transition
 * relation in a compact way.
 *
 * The state buffer considers a state space represented consisting of integer
 * state code ranging from&nbsp;0 up to the number of states minus&nbsp;1.
 * For each state code, it stores the following status information.
 * Each state can be designated as <I>initial</I> and/or <I>reachable</I>,
 * and can be <I>marked</I> with zero or more propositions.
 *
 * The information is stored packed into the bits of a single integer for
 * each state. This allows for the encoding of up to 30 distinct marking
 * propositions.
 *
 * @see StateEncoding
 *
 * @author Robi Malik
 */

public class IntStateBuffer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new state buffer. This constructor creates a new state buffer
   * with the states in the given encoding. If the state encoding contains
   * a <CODE>null</CODE> state, it is used as a reachable dump state,
   * otherwise an additional unreachable dump state is added to the end of
   * the state space.
   * @param  eventEnc        Event encoding that defines event codes for
   *                         proposition events used as markings of the states.
   * @param  stateEnc        State encoding that defines the assignment of
   *                         state codes for the states in the buffer.
   */
  public IntStateBuffer(final EventEncoding eventEnc,
                        final StateEncoding stateEnc)
  {
    this(eventEnc, stateEnc, null);
  }

  /**
   * Creates a new state buffer.
   * @param  eventEnc        Event encoding that defines event codes for
   *                         proposition events used as markings of the states.
   * @param  stateEnc        State encoding that defines the assignment of
   *                         state codes for the states in the buffer.
   * @param  dumpState       Dump state to be used, or <CODE>null</CODE>.
   *                         If the state encoding contains the indicated
   *                         state, it is used as a reachable dump state,
   *                         otherwise an additional unreachable dump state
   *                         is added to the end of the state space.
   */
  public IntStateBuffer(final EventEncoding eventEnc,
                        final StateEncoding stateEnc,
                        final StateProxy dumpState)
  {
    mPropositionStatus = eventEnc;
    final int numStates = stateEnc.getNumberOfStates();
    mDumpStateIndex = -1;
    for (int s = 0; s < numStates; s++) {
      if (stateEnc.getState(s) == dumpState) {
        mDumpStateIndex = s;
        break;
      }
    }
    if (mDumpStateIndex >= 0) {
      mStateInfo = new int[numStates];
    } else {
      mDumpStateIndex = numStates;
      mStateInfo = new int[numStates + 1];
    }
    for (int s = 0; s < numStates; s++) {
      final StateProxy state = stateEnc.getState(s);
      if (state != null) {
        int info = TAG_REACHABLE;
        if (state.isInitial()) {
          info |= TAG_INITIAL;
        }
        for (final EventProxy prop : state.getPropositions()) {
          final int p = eventEnc.getEventCode(prop);
          if (p >= 0 && eventEnc.isPropositionUsed(p)) {
            info |= (1 << p);
          }
        }
        mStateInfo[s] = info;
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
  public IntStateBuffer(final int size,
                        final EventStatusProvider propStatus)
  {
    this(size + 1, size, propStatus);
    mStateInfo[mDumpStateIndex] = 0;
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
  public IntStateBuffer(final int size,
                        final int dumpIndex,
                        final EventStatusProvider propStatus)
  {
    mPropositionStatus = propStatus;
    mDumpStateIndex = dumpIndex;
    mStateInfo = new int[size];
    Arrays.fill(mStateInfo, TAG_REACHABLE);
  }

  /**
   * Creates a new state buffer that is an identical copy of the given
   * state buffer. This copy constructor constructs a deep copy that does
   * not share any data structures with the given state buffer.
   * @param  buffer     The state buffer to be copied from.
   * @param  propStatus Event status provider to determine the number of
   *                    propositions and which propositions are used.
  */
  public IntStateBuffer(final IntStateBuffer buffer,
                        final EventStatusProvider propStatus)
  {
    mPropositionStatus = propStatus;
    mDumpStateIndex = buffer.mDumpStateIndex;
    final int size = buffer.getNumberOfStates();
    mStateInfo = Arrays.copyOf(buffer.mStateInfo, size);
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the number of states in the buffer.
   */
  public int getNumberOfStates()
  {
    return mStateInfo.length;
  }

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

  /**
   * Determines whether the given state is initial.
   */
  public boolean isInitial(final int state)
  {
    return (mStateInfo[state] & TAG_INITIAL) != 0;
  }

  /**
   * Sets the initial status of the given state.
   */
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
  public boolean isReachable(final int state)
  {
    return (mStateInfo[state] & TAG_REACHABLE) != 0;
  }

  /**
   * Sets the reachability status of the given state.
   * Reachability is merely handled as a flag by this class,
   * to be interpreted by the users.
   */
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
  public boolean isMarked(final int state, final int prop)
  {
    if (mPropositionStatus.isPropositionUsed(prop)) {
      final int pattern = 1 << prop;
      return (mStateInfo[state] & pattern) != 0;
    } else {
      return true;
    }
  }

  /**
   * Gets the total number of markings in this state buffer.
   * Each instance of a proposition marking a reachable state counts
   * as marking.
   */
  public int getNumberOfMarkings()
  {
    final int numProps = mPropositionStatus.getNumberOfPropositions();
    int result = 0;
    for (int prop = 0; prop < numProps; prop++) {
      if (mPropositionStatus.isPropositionUsed(prop)) {
        for (int state = 0; state < getNumberOfStates(); state++) {
          if (isReachable(state) && isMarked(state, prop)) {
            result++;
          }
        }
      }
    }
    return result;
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
  public long getAllMarkings(final int state)
  {
    return mStateInfo[state] & ~TAG_ALL;
  }

  /**
   * Sets the marking of a state.
   * @param  state    ID of the state to be modified.
   * @param  prop     ID of the marking proposition to be modified.
   * @param  value    <CODE>true</CODE> if the state is to be marked,
   *                  <CODE>false</CODE> if it is to be unmarked.
   */
  public void setMarked(final int state, final int prop, final boolean value)
  {
    final int pattern = 1 << prop;
    if (value) {
      mStateInfo[state] |= pattern;
    } else {
      if (!mPropositionStatus.isPropositionUsed(prop)) {
        mPropositionStatus.setPropositionUsed(prop, true);
        for (int s = 0; s < mStateInfo.length; s++) {
          mStateInfo[s] |= pattern;
        }
      }
      mStateInfo[state] &= ~pattern;
    }
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
  public void setAllMarkings(final int state, final long markings)
  {
    mStateInfo[state] = (mStateInfo[state] & TAG_ALL) | (int) markings;
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
  public void clearMarkings(final int state)
  {
    mStateInfo[state] &= TAG_ALL;
  }

  /**
   * Copies markings from one state to another. This method adds all the
   * markings of the given source state to the given destination state.
   * The markings of the source state will not be changed, and the destination
   * state retains any markings it previously had in addition to the new ones.
   */
  public void copyMarkings(final int source, final int dest)
  {
    mStateInfo[dest] |= (mStateInfo[source] & ~TAG_ALL);
  }

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
  public boolean addRedundantPropositions()
  {
    final int numProps = mPropositionStatus.getNumberOfPropositions();
    int pattern = 0;
    for (int p = 0; p < numProps; p++) {
      if (!mPropositionStatus.isPropositionUsed(p)) {
        mPropositionStatus.setPropositionUsed(p, true);
        pattern |= (1 << p);
      }
    }
    if (pattern == 0) {
      return false;
    } else {
      for (int state = 0; state < mStateInfo.length; state++) {
        mStateInfo[state] |= pattern;
      }
      return true;
    }
  }

  /**
   * Checks for each proposition whether is appears on all reachable states,
   * and if so, removes the proposition by marking it as unused.
   * @return <CODE>true</CODE> if at least one proposition was removed,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean removeRedundantPropositions()
  {
    final int numProps = mPropositionStatus.getNumberOfPropositions();
    boolean removed = false;
    for (int p = 0; p < numProps; p++) {
      if (mPropositionStatus.isPropositionUsed(p)) {
        final int pattern = (1 << p) | TAG_REACHABLE;
        boolean redundant = true;
        for (int state = 0; state < mStateInfo.length; state++) {
          if ((mStateInfo[state] & pattern) == TAG_REACHABLE) {
            redundant = false;
            break;
          }
        }
        if (redundant) {
          mPropositionStatus.setPropositionUsed(p, false);
          removed = true;
        }
      }
    }
    return removed;
  }


  //#########################################################################
  //# Advanced Access
  /**
   * Returns whether this state buffer represents an empty state set.
   * @return <CODE>true</CODE> if all states in the buffer are marked
   *         as unreachable, <CODE>false</CODE> otherwise.
   */
  public boolean isEmpty()
  {
    for (final int tags : mStateInfo) {
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
  public int getNumberOfReachableStates()
  {
    int count = 0;
    for (final int tags : mStateInfo) {
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
    final int used = mPropositionStatus.getUsedPropositions();
    printer.print('{');
    int last = -1;
    for (int s = 0; s < mStateInfo.length; s++) {
      final int info = mStateInfo[s] &~ TAG_REACHABLE;
      if (info != 0) {
        if (last >= 0) {
          printer.print(", ");
        }
        last = s;
        if ((info & TAG_INITIAL) != 0) {
          printer.print("->");
        }
        printer.print(s);
        if ((info & used) != 0) {
          final int numProps = mPropositionStatus.getNumberOfPropositions();
          if (numProps == 1) {
            printer.print('*');
          } else {
            printer.print('<');
            boolean first = true;
            for (int p = 0; p < numProps; p++) {
              if ((info & (1 << p) & used) != 0) {
                if (first) {
                  first = false;
                } else {
                  printer.print(",");
                }
                printer.print(p);
              }
            }
            printer.print('>');
          }
        }
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
  private final EventStatusProvider mPropositionStatus;
  private final int[] mStateInfo;
  private int mDumpStateIndex;


  //#########################################################################
  //# Class Constants
  private static final int TAG_INITIAL = 0x80000000;
  private static final int TAG_REACHABLE = 0x40000000;
  private static final int TAG_ALL = TAG_INITIAL | TAG_REACHABLE;

}

