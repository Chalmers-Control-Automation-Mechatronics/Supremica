//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.abstraction;

import java.util.Arrays;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A transition relation simplifier to find special events.</P>
 *
 * <P>Although implemented as a transition relation simplifier, this
 * abstraction never changes its automaton. Its sole purpose is to
 * produce as output a status array that determines for all events in
 * the input transition relation whether the event has a special-event
 * status in the transition relation.</P>
 *
 * <UL>
 * <LI>The status flag {@link EventStatus#STATUS_UNUSED} is set for
 *     events marked as unused in the transition relation.</LI>
 * <LI>The status flag {@link EventStatus#STATUS_CONTROLLABLE} is set for
 *     events marked as controllable in the transition relation.</LI>
 * <LI>The status flag {@link EventStatus#STATUS_BLOCKED} is set for
 *     events that are disabled in all reachable states of the transition
 *     relation.</LI>
 * <LI>The status flag {@link EventStatus#STATUS_FAILING} is set for
 *     events whose transitions all take the transition relation to a
 *     deadlock state, i.e., a state with not outgoing transitions that
 *     is not marked by the default marking.</LI>
 * <LI>The status flag {@link EventStatus#STATUS_SELFLOOP_ONLY} is
 *     set for events that only appear on selfloops in the entire transition
 *     relation.</LI>
 * <LI>The status flag {@link EventStatus#STATUS_ALWAYS_ENABLED} is
 *     set for events that are enabled in every state without an outgoing
 *     tau-transitions, except for deadlock states. This algorithm is based
 *     on the assumption that the input automaton is tau-loop free.</LI>
 * </UL>
 *
 * <P>The check for each status flag except {@link EventStatus#STATUS_UNUSED}
 * and {@link EventStatus#STATUS_CONTROLLABLE} can be turned on and off
 * individually by configuring the simplifier.
 * Only status flags that are turned on will be reported.</P>
 *
 * @author Robi Malik
 */

public class SpecialEventsFinder
  extends AbstractMarkingTRSimplifier
{

  //#######################################################################
  //# Constructors
  public SpecialEventsFinder()
  {
  }

  public SpecialEventsFinder(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether blocked events ({@link EventStatus#STATUS_BLOCKED}) are to
   * be detected.
   */
  public void setBlockedEventsDetected(final boolean enable)
  {
    mBlockedEventsDetected = enable;
  }

  /**
   * Returns whether blocked events ({@link EventStatus#STATUS_BLOCKED}) are
   * detected.
   */
  public boolean isBlockedEventsDetected()
  {
    return mBlockedEventsDetected;
  }

  /**
   * Sets whether failing events ({@link EventStatus#STATUS_FAILING}) are to
   * be detected.
   */
  public void setFailingEventsDetected(final boolean enable)
  {
    mFailingEventsDetected = enable;
  }

  /**
   * Returns whether failing events ({@link EventStatus#STATUS_FAILING}) are
   * detected.
   */
  public boolean isFailingEventsDetected()
  {
    return mFailingEventsDetected;
  }

  /**
   * Sets whether selfloop-only events
   * ({@link EventStatus#STATUS_SELFLOOP_ONLY}) are to be detected.
   */
  public void setSelfloopOnlyEventsDetected(final boolean enable)
  {
    mSelfloopOnlyEventsDetected = enable;
  }

  /**
   * Returns whether selfloop-only events
   * ({@link EventStatus#STATUS_SELFLOOP_ONLY}) are detected.
   */
  public boolean isSelfloopOnlyEventsDetected()
  {
    return mSelfloopOnlyEventsDetected;
  }

  /**
   * Sets whether always enabled events
   * ({@link EventStatus#STATUS_ALWAYS_ENABLED}) are to be detected.
   */
  public void setAlwaysEnabledEventsDetected(final boolean enable)
  {
    mAlwaysEnabledEventsDetected = enable;
  }

  /**
   * Returns whether always enabled events
   * ({@link EventStatus#STATUS_ALWAYS_ENABLED}) are detected.
   */
  public boolean isAlwaysEnabledEventsDetected()
  {
    return mAlwaysEnabledEventsDetected;
  }

  /**
   * Sets whether controllability is considered when checking for
   * always enabled events. If enabled, only uncontrollable events
   * will be flagged as always enabled.
   */
  public void setControllabilityConsidered(final boolean consider)
  {
    mControllabilityConsidered = consider;
  }

  /**
   * Returns whether controllability is considered when checking for
   * always enabled events.
   * @see #setControllabilityConsidered(boolean) setControllabilityConsidered()
   */
  public boolean isControllabilityConsidered()
  {
    return mControllabilityConsidered;
  }


  //#########################################################################
  //# Simple Access
  public byte[] getComputedEventStatus()
  {
    return mComputedEventStatus;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# TransitionRelationSimplifier
  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, false, false, false);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    final int numStates = rel.getNumberOfStates();
    final int defaultID = getDefaultMarkingID();

    // Initialise status array ...
    mComputedEventStatus = new byte[numEvents];
    byte initialStatus = 0;
    if (mBlockedEventsDetected) {
      initialStatus |= EventStatus.STATUS_BLOCKED;
    }
    if (mFailingEventsDetected) {
      initialStatus |= EventStatus.STATUS_FAILING;
    }
    if (mSelfloopOnlyEventsDetected) {
      initialStatus |= EventStatus.STATUS_SELFLOOP_ONLY;
    }
    Arrays.fill(mComputedEventStatus, initialStatus);

    // Check for unused and controllable events ...
    for (int e = EventEncoding.TAU; e < numEvents; e++) {
      checkAbort();
      final byte status = rel.getProperEventStatus(e);
      if (!EventStatus.isUsedEvent(status)) {
        mComputedEventStatus[e] = EventStatus.STATUS_UNUSED;
      }
      if (EventStatus.isControllableEvent(status)) {
        mComputedEventStatus[e] |= EventStatus.STATUS_CONTROLLABLE;
      }
    }

    // Find dump states ...
    byte[] dumpInfo = null;
    boolean canHaveAlwaysEnabledEvents = mAlwaysEnabledEventsDetected;
    if (mFailingEventsDetected || mAlwaysEnabledEventsDetected) {
      dumpInfo = new byte[numStates];
      if ((rel.getConfiguration() &
          ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0) {
        for (int s = 0; s < numStates; s++) {
          checkAbort();
          if (rel.isReachable(s) && !rel.isDeadlockState(s, defaultID)) {
            dumpInfo[s] = NONDUMP;
          }
        }
      } else {
        final TransitionIterator iter =
          rel.createAllTransitionsReadOnlyIterator();
        while (iter.advance()) {
          checkAbort();
          final int s = iter.getCurrentSourceState();
          dumpInfo[s] |= NONDUMP;
          if (iter.getCurrentEvent() == EventEncoding.TAU) {
            dumpInfo[s] |= HASTAU;
          }
        }
        for (int s = 0; s < numStates; s++) {
          if (dumpInfo[s] == DUMP &&
              rel.isReachable(s) &&
              rel.isMarked(s, defaultID)) {
            dumpInfo[s] = NONDUMP;
            canHaveAlwaysEnabledEvents = false;
          }
        }
      }
    }

    // Check for blocked, failing, and selfloop-only events ...
    if (initialStatus != 0) {
      final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
      while (iter.advance()) {
        checkAbort();
        final int e = iter.getCurrentEvent();
        final int s = iter.getCurrentSourceState();
        final int t = iter.getCurrentTargetState();
        if (mFailingEventsDetected && dumpInfo[t] == DUMP) {
          mComputedEventStatus[e] &= EventStatus.STATUS_FAILING;
        } else if (s == t) {
          mComputedEventStatus[e] &= EventStatus.STATUS_SELFLOOP_ONLY;
        } else {
          mComputedEventStatus[e] = 0;
        }
      }
    }

    // Check for always enabled events ...
    if (canHaveAlwaysEnabledEvents) {
      if ((rel.getConfiguration() &
          ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0) {
        // Algorithm for forwards transitions ...
        final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
        final boolean[] disabled = new boolean[numEvents];
        states:
        for (int s = 0; s < numStates; s++) {
          checkAbort();
          if (dumpInfo[s] == NONDUMP) {
            int lastEvent = EventEncoding.TAU;
            iter.resetState(s);
            while (iter.advance()) {
              final int e = iter.getCurrentEvent();
              if (e == EventEncoding.TAU) {
                continue states;
              } else if (e > lastEvent) {
                for (int d = lastEvent + 1; d < e; d++) {
                  disabled[d] = true;
                }
                lastEvent = e;
              }
            }
            for (int d = lastEvent + 1; d < numEvents; d++) {
              disabled[d] = true;
            }
          }
        }
        for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
          checkAbort();
          final byte status = rel.getProperEventStatus(e);
          if (canBeAlwaysEnabledEvent(status) && !disabled[e]) {
            mComputedEventStatus[e] |=
              EventStatus.STATUS_ALWAYS_ENABLED;
          }
        }
      } else {
        // Algorithm for backwards transitions ...
        final TransitionIterator iter =
          rel.createAllTransitionsReadOnlyIterator();
        int numStatesChecked = 0;
        for (int s = 0; s < numStates; s++) {
          checkAbort();
          if (!rel.isReachable(s)) {
            dumpInfo[s] = DUMP;
          } else if (dumpInfo[s] == NONDUMP) {
            numStatesChecked++;
          }
        }
        final boolean[] found = new boolean[numStates];
        for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
          checkAbort();
          final byte status = rel.getProperEventStatus(e);
          if (canBeAlwaysEnabledEvent(status)) {
            Arrays.fill(found, false);
            int numStatesFound = 0;
            iter.resetEvent(e);
            while (iter.advance()) {
              final int s = iter.getCurrentSourceState();
              if (!found[s] && dumpInfo[s] == NONDUMP) {
                found[s] = true;
                numStatesFound++;
              }
            }
            if (numStatesFound == numStatesChecked) {
              mComputedEventStatus[e] |=
                EventStatus.STATUS_ALWAYS_ENABLED;
            }
          }
        }
      }
    }

    return false;
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean canBeAlwaysEnabledEvent(final byte status)
  {
    if (!EventStatus.isUsedEvent(status)) {
      return false;
    } else if (mControllabilityConsidered) {
      return !EventStatus.isControllableEvent(status);
    } else {
      return true;
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mBlockedEventsDetected = false;
  private boolean mFailingEventsDetected = false;
  private boolean mSelfloopOnlyEventsDetected = false;
  private boolean mAlwaysEnabledEventsDetected = false;
  private boolean mControllabilityConsidered = false;

  private byte[] mComputedEventStatus;


  //#########################################################################
  //# Class Constants
  private static final byte DUMP = 0x00;
  private static final byte NONDUMP = 0x01;
  private static final byte HASTAU = 0x02;

}
