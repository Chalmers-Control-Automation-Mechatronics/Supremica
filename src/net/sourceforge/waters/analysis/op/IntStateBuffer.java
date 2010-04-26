//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   StateBuffer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.List;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.xsd.base.EventKind;


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
 * @see {@link StateEncoding}
 *
 * @author Robi Malik
 */

public class IntStateBuffer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new state buffer.
   * @param  eventEnc   Event encoding that defines event codes for proposition
   *                    events used as markings of the states.
   * @param  stateEnc   State encoding that defines the assignment of state
   *                    codes for the states in the buffer.
   * @throws OverflowException if the event encoding map has more than 30
   *                    propositions.
   */
  public IntStateBuffer(final EventEncoding eventEnc,
                        final StateEncoding stateEnc)
    throws OverflowException
  {
    final int numProps = eventEnc.getNumberOfPropositions();
    if (numProps > MAX_PROPOSITIONS) {
      throw new OverflowException
        ("Encoding has " + numProps + " propositions, but " +
         ProxyTools.getShortClassName(this) + " can only handle up to " +
         MAX_PROPOSITIONS + " different propositions!");
    }
    int tags0 = TAG_REACHABLE;
    final List<EventProxy> extra = eventEnc.getExtraSelfloops();
    if (extra != null) {
      for (final EventProxy event : extra) {
        if (event.getKind() == EventKind.PROPOSITION) {
          final int code = eventEnc.getEventCode(event);
          tags0 |= (1 << code);
        }
      }
    }
    final int numStates = stateEnc.getNumberOfStates();
    mStateInfo = new int[numStates];
    int i = 0;
    for (final StateProxy state : stateEnc.getStates()) {
      int tags = tags0;
      if (state.isInitial()) {
        tags |= TAG_INITIAL;
      }
      for (final EventProxy event : state.getPropositions()) {
        final int code = eventEnc.getEventCode(event);
        if (code >= 0) {
          tags |= 1 << code;
        }
      }
      mStateInfo[i++] = tags;
    }
  }

  /**
   * Creates a new empty state buffer.
   * This constructor allocates a new state buffer with the given number
   * of states. All attributes and markings of the states are initialised
   * to be <CODE>false</CODE>.
   * @param  size       The number of states in the new buffer.
   */
  public IntStateBuffer(final int size)
  {
    mStateInfo = new int[size];
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
   * @param  state   ID of the state to be tested.
   * @param  prop    ID of the marking proposition to be tested.
   */
  public boolean isMarked(final int state, final int prop)
  {
    return (mStateInfo[state] & (1 << prop)) != 0;
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
    if (value) {
      mStateInfo[state] |= 1 << prop;
    } else {
      mStateInfo[state] &= ~(1 << prop);
    }
  }

  /**
   * Sets all markings for the given state simultaneously.
   * @param  state    ID of the state to be modified.
   * @param  markings A new marking pattern for the state. This pattern
   *                  can be obtained through the {@link #getAllMarkings(int)
   *                  getAllMarkings()} method.
   */
  public void setAllMarkings(final int state, final long markings)
  {
    mStateInfo[state] = (mStateInfo[state] & TAG_ALL) | (int) markings;
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


  //#########################################################################
  //# Advanced Access
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


  //#########################################################################
  //# Data Members
  private final int[] mStateInfo;


  //#########################################################################
  //# Class Constants
  private static final int TAG_INITIAL = 0x80000000;
  private static final int TAG_REACHABLE = 0x40000000;
  private static final int TAG_ALL = TAG_INITIAL | TAG_REACHABLE;
  private static final int MAX_PROPOSITIONS = 30;

}
