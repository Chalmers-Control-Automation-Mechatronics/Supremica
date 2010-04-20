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
 * @author Robi Malik
 */
public class IntStateBuffer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new state buffer.
   * @throws OverflowException if the event encoding map has more than 30
   *                  propositions.
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


  //#########################################################################
  //# Simple Access
  public int getNumberOfStates()
  {
    return mStateInfo.length;
  }

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
