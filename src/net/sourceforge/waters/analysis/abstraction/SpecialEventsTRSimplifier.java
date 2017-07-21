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

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;


/**
 * <P>A transition relation simplifier to remove special events.</P>
 *
 * <P>This transition relation simplifier checks the status of all events
 * in the the transition relation and removes or redirects transitions
 * as follows.</P>
 *
 * <DL>
 * <DT>{@link EventStatus#STATUS_LOCAL}</DT>
 * <DD>All transitions with local events are replaced by silent transitions
 * (with event code {@link EventEncoding#TAU}), and the local events
 * are marked as unused ({@link EventStatus#STATUS_UNUSED}).</DD>
 * <DT>{@link EventStatus#STATUS_BLOCKED}</DT>
 * <DD>All transitions with blocked events are removed from the transition
 * relation, and the blocked events are marked as unused
 * ({@link EventStatus#STATUS_UNUSED}).</DD>
 * <DT>{@link EventStatus#STATUS_FAILING}</DT>
 * <DD>All transitions with failing events are redirected to the dump state
 * of the transition relation ({@link
 * ListBufferTransitionRelation#getDumpStateIndex()}).</DD>
 * <DT>{@link EventStatus#STATUS_SELFLOOP_ONLY}</DT>
 * <DD>All selfloop transitions with selfloop-only events are removed from
 * the transition relation.</DD>
 * </DL>
 *
 * <P>In addition, pure selfloop events, i.e., events that appear on selfloops
 * in all states except the dump state and on no other transitions, are removed
 * from the transition relation.</P>
 *
 * @author Robi Malik
 */

public class SpecialEventsTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructor
  public SpecialEventsTRSimplifier()
  {
  }

  public SpecialEventsTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public boolean isPartitioning()
  {
    return true;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, false);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
    throws AnalysisAbortException
  {
    // Set up ...
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int numEvents = rel.getNumberOfProperEvents();
    final int dump = rel.getDumpStateIndex();
    final boolean forward =
      (rel.getConfiguration() &
       ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0;
    final TransitionIterator iter = forward ?
      rel.createSuccessorsModifyingIterator() :
      rel.createPredecessorsModifyingIterator();
    final TIntArrayList tauTargets = new TIntArrayList();
    boolean modified = false;
    boolean needsReachabilityCheck = false;
    final int[] selfloops = new int[numEvents];
    int numReachable = 0;

    // Remove/redirect transitions ...
    for (int from = 0; from < numStates; from++) {
      if (rel.isReachable(from)) {
        if (from != dump) {
          numReachable++;
        }
        iter.resetState(from);
        int prevE = -1;
        boolean dumped = false;
        while (iter.advance()) {
          checkAbort();
          final int e = iter.getCurrentEvent();
          if (e == EventEncoding.TAU) {
            continue;
          } else if (e != prevE) {
            prevE = e;
            dumped = false;
          }
          final int to = iter.getCurrentToState();
          if (from != to) {
            selfloops[e] = -1;
          } else if (selfloops[e] >= 0) {
            selfloops[e]++;
          }
          final byte status = rel.getProperEventStatus(e);
          if (EventStatus.isLocalEvent(status)) {
            if (from != to) {
              tauTargets.add(to);
            }
            iter.remove();
            modified = true;
          } else if (EventStatus.isBlockedEvent(status)) {
            iter.remove();
            modified = true;
            needsReachabilityCheck |= from != to;
          } else if (EventStatus.isFailingEvent(status)) {
            if (forward) {
              if (dumped) {
                iter.remove();
                modified = needsReachabilityCheck = true;
              } else {
                if (to != dump) {
                  iter.setCurrentToState(dump);
                  addDefaultMarking();
                  modified = needsReachabilityCheck = dumped = true;
                }
              }
            } else {
              if (from != dump) {
                iter.remove();
                rel.addTransition(to, e, dump);
                addDefaultMarking();
                modified = needsReachabilityCheck = true;
              }
            }
          } else if (EventStatus.isSelfloopOnlyEvent(status)) {
            if (from == to) {
              iter.remove();
              modified = true;
            }
          }
        }
        if (!tauTargets.isEmpty()) {
          tauTargets.sort();
          if (forward) {
            rel.addTransitions(from, EventEncoding.TAU, tauTargets);
          } else {
            rel.addTransitions(tauTargets, EventEncoding.TAU, from);
          }
          tauTargets.clear();
          final byte tauStatus = rel.getProperEventStatus(EventEncoding.TAU);
          rel.setProperEventStatus(EventEncoding.TAU,
                                   tauStatus & ~EventStatus.STATUS_UNUSED);
        }
      }
    }

    // Remove pure selfloop, blocked, and local events ...
    // Note: there may be blocked events that appear on no transitions.
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        checkAbort();
        if (selfloops[e] == numReachable) {
          if (EventStatus.isSelfloopOnlyEvent(status)) {
            rel.setProperEventStatus(e, status | EventStatus.STATUS_UNUSED);
          } else {
            rel.removeEvent(e);
          }
          modified = true;
        } else if ((status & (EventStatus.STATUS_LOCAL |
                              EventStatus.STATUS_BLOCKED)) != 0) {
          rel.setProperEventStatus(e, status | EventStatus.STATUS_UNUSED);
          modified = true;
        }
      }
    }

    // Clean up ...
    if (needsReachabilityCheck) {
      final int config = getPreferredOutputConfiguration();
      if (rel.checkReachability(config)) {
        rel.removeTauSelfLoops();
        removeProperSelfLoopEvents();
      }
    }

    return modified;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void addDefaultMarking()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int defaultID = getDefaultMarkingID();
    if (defaultID >= 0 && !rel.isPropositionUsed(defaultID)) {
      rel.setPropositionUsed(defaultID, true);
      for (int s = 0; s < rel.getNumberOfStates(); s++) {
        rel.setMarked(s, defaultID, rel.isReachable(s));
      }
    }
  }

}
