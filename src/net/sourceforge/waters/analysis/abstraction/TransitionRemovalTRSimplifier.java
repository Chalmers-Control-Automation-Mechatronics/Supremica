//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A transition relation simplifier to remove observation equivalence
 * redundant transitions.</P>
 *
 * <P><I>Reference.</I><BR>
 * Jaana Eloranta. Minimizing the Number of Transitions with Respect to
 * Observation Equivalence. BIT, <STRONG>31</STRONG>(4), 397-419, 1991.</P>
 *
 * @author Robi Malik
 */

public class TransitionRemovalTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public TransitionRemovalTRSimplifier()
  {
  }

  TransitionRemovalTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be stored.
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow an
   *          unlimited number of transitions.
   */
  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  /**
   * Gets the transition limit.
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }

  /**
   * Sets whether special events are to be considered in abstraction.
   * If enabled, events marked as selfloop-only in all other automata
   * will be treated specially. For such events, it is possible to assume
   * implicit selfloops on all states of the automaton being simplified,
   * potentially giving better state reduction.
   */
  public void setUsingSpecialEvents(final boolean enable)
  {
    mUsingSpecialEvents = enable;
  }

  /**
   * Returns whether special events are considered in abstraction.
   * @see #setUsingSpecialEvents(boolean)
   */
  public boolean isUsingSpecialEvents()
  {
    return mUsingSpecialEvents;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }

  @Override
  public boolean isPartitioning()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    boolean removedSome = false;

    // 1. Check tau transitions
    final int tau = EventEncoding.TAU;
    int config = rel.getConfiguration();
    final TauClosure exploredClosure;
    if ((config & ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0) {
      exploredClosure = rel.createSuccessorsTauClosure(0);
    } else {
      exploredClosure = rel.createPredecessorsTauClosure(0);
    }
    final TransitionIterator iterExplore = exploredClosure.createIterator();
    final TransitionIterator iterCandidate =
      rel.createAllTransitionsModifyingIterator(tau);
    while (iterCandidate.advance()) {
      checkAbort();
      final int from0 = iterCandidate.getCurrentFromState();
      final int to0 = iterCandidate.getCurrentToState();
      // Temporarily change the transition to selfloop so it is not explored.
      iterCandidate.setCurrentToState(from0);
      boolean remove = false;
      iterExplore.resetState(from0);
      while (iterExplore.advance()) {
        if (iterExplore.getCurrentToState() == to0) {
          remove = true;
          break;
        }
      }
      if (remove) {
        iterCandidate.remove();
        removedSome = true;
      } else {
        // If the transition is not deleted, change it back.
        iterCandidate.setCurrentToState(to0);
      }
    }

    // 2. Check proper event transitions
    config = rel.getConfiguration();
    final TauClosure cachedClosure;
    if ((config & ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0) {
      cachedClosure = rel.createSuccessorsTauClosure(mTransitionLimit);
    } else {
      cachedClosure = rel.createPredecessorsTauClosure(mTransitionLimit);
    }
    final TransitionIterator iterPrefix = cachedClosure.createIterator();
    final TransitionIterator iterEvent = rel.createAnyReadOnlyIterator();
    final TransitionIterator iterSuffix = cachedClosure.createIterator();
    final int numEvents = rel.getNumberOfProperEvents();
    iterCandidate.resetEvents(EventEncoding.NONTAU, numEvents);
    while (iterCandidate.advance()) {
      checkAbort();
      final int e = iterCandidate.getCurrentEvent();
      final byte status = rel.getProperEventStatus(e);
      final boolean selflooped =
        mUsingSpecialEvents &&
        EventStatus.isSelfloopOnlyEvent(status);
      final int from0 = iterCandidate.getCurrentFromState();
      final int to0 = iterCandidate.getCurrentToState();
      boolean remove = false;
      iterPrefix.resetState(from0);
      outer:
      while (iterPrefix.advance()) {
        final int p1 = iterPrefix.getCurrentToState();
        if (selflooped && p1 == to0) {
          remove = true;
          break;
        }
        iterEvent.reset(p1, e);
        iterSuffix.reset();
        while (iterEvent.advance()) {
          final int p2 = iterEvent.getCurrentToState();
          if (p1 != from0 || p2 != to0) {
            iterSuffix.resume(p2);
            while (iterSuffix.advance()) {
              if (iterSuffix.getCurrentToState() == to0) {
                remove = true;
                break outer;
              }
            }
          }
        }
      }
      if (remove) {
        iterCandidate.remove();
        removedSome = true;
      }
    }

    return removedSome;
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, false, true, false);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Data Members
  private int mTransitionLimit = Integer.MAX_VALUE;
  private boolean mUsingSpecialEvents = true;

}
