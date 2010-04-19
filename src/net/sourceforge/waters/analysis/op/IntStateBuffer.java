//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   StateBuffer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TObjectIntHashMap;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;

/**
 * @author Robi Malik
 */
public class IntStateBuffer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new state buffer.
   * @param  states   The original state objects to define the buffer states.
   *                  State codes will be assigned in the order in which they
   *                  appear in this collection.
   * @param  propMap  A map defining the encoding of propositions. The map
   *                  should assign an integer in the range from 0 to&nbsp;29
   *                  to each proposition to be represented. Propositions
   *                  without map entry will be suppressed. The map should
   *                  only contain relevant propositions, otherwise the number
   *                  of propositions will be estimated incorrectly.
   * @throws OverflowException if the proposition map has more than 30 entries.
   */
  public IntStateBuffer(final Collection<StateProxy> states,
                        final TObjectIntHashMap<EventProxy> propMap)
    throws OverflowException
  {
    this(states, propMap, propMap.size());
  }

  /**
   * Creates a new state buffer.
   * @param  states   The original state objects to define the buffer states.
   *                  State codes will be assigned in the order in which they
   *                  appear in this collection.
   * @param  propMap  A map defining the encoding of propositions. The map
   *                  should assign an integer in the range from 0
   *                  to&nbsp;(numProps-1) to each proposition to be
   *                  represented. Propositions without map entry will be
   *                  suppressed. It may contain entries for other events.
   * @param  numProps The number of propositions to be represented.
   * @throws OverflowException if the numProps parameter is greater or
   *                  equal to&nbsp;30.
   */
  public IntStateBuffer(final Collection<StateProxy> states,
                        final TObjectIntHashMap<EventProxy> propMap,
                        final int numProps)
    throws OverflowException
  {
    if (numProps > MAX_PROPOSITIONS) {
      throw new OverflowException
        (ProxyTools.getShortClassName(this) + " can only handle up to " +
         MAX_PROPOSITIONS + " different propositions!");
    }
    mStateInfo = new int[states.size()];
    int i = 0;
    for (final StateProxy state : states) {
      int tags = state.isInitial() ? TAG_ALL : TAG_REACHABLE;
      for (final EventProxy prop : state.getPropositions()) {
        if (propMap.containsKey(prop)) {
          tags |= 1 << propMap.get(prop);
        }
      }
      mStateInfo[i++] = tags;
    }
  }


  //#########################################################################
  //# Simple Access
  public boolean isInitial(final int state)
  {
    return (mStateInfo[state] & TAG_INITIAL) != 0;
  }

  public void setInitial(final int state, final boolean value)
  {
    if (value) {
      mStateInfo[state] |= TAG_INITIAL;
    } else {
      mStateInfo[state] &= ~TAG_INITIAL;
    }
  }

  public boolean isReachable(final int state)
  {
    return (mStateInfo[state] & TAG_REACHABLE) != 0;
  }

  public void setReachable(final int state, final boolean value)
  {
    if (value) {
      mStateInfo[state] |= TAG_REACHABLE;
    } else {
      mStateInfo[state] &= ~TAG_REACHABLE;
    }
  }

  public boolean isMarked(final int state, final int prop)
  {
    return (mStateInfo[state] & (1 << prop)) != 0;
  }

  public long getAllMarkings(final int state)
  {
    return mStateInfo[state] & ~TAG_ALL;
  }

  public void setMarked(final int state, final int prop, final boolean value)
  {
    if (value) {
      mStateInfo[state] |= 1 << prop;
    } else {
      mStateInfo[state] &= ~(1 << prop);
    }
  }

  public void clearMarkings(final int state)
  {
    mStateInfo[state] &= TAG_ALL;
  }

  public void copyMarkings(final int source, final int dest)
  {
    mStateInfo[dest] |= (mStateInfo[source] & ~TAG_ALL);
  }

  public void addProposition(final int prop, final boolean markStates)
    throws OverflowException
  {
    if (prop >= MAX_PROPOSITIONS) {
      throw new OverflowException
        (ProxyTools.getShortClassName(this) + " can only handle up to " +
         MAX_PROPOSITIONS + " different propositions!");
    } else if (markStates) {
      final int tag = 1 << prop;
      for (int i = 0; i < mStateInfo.length; i++) {
        mStateInfo[i] |= tag;
      }
    }
  }


  //#########################################################################
  //# Advanced Access
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
