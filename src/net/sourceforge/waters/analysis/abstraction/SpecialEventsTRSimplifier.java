//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SpecialEventsTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;


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
 * <DT>{@link EventStatus#STATUS_OUTSIDE_ONLY_SELFLOOP}</DT>
 * <DD>All selfloop transitions with selfloop-only events are removed from
 * the transition relation.</DD>
 * </DL>
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
  {
    // Set up ...
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int dump = rel.getDumpStateIndex();
    final TIntArrayList tauTargets = new TIntArrayList();
    boolean modified = false;
    boolean needsReachabilityCheck = false;

    // Remove/redirect transitions ...
    if ((rel.getConfiguration() &
         ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0) {
      // Algorithm for forward transitions ....
      final TransitionIterator iter = rel.createSuccessorsModifyingIterator();
      for (int s = 0; s < numStates; s++) {
        iter.resetState(s);
        while (iter.advance()) {
          final int e = iter.getCurrentEvent();
          if (e == EventEncoding.TAU) {
            continue;
          }
          final int t = iter.getCurrentTargetState();
          final byte status = rel.getProperEventStatus(e);
          if (EventStatus.isLocalEvent(status)) {
            if (s != t) {
              tauTargets.add(t);
            }
            iter.remove();
            modified = true;
          } else if (EventStatus.isBlockedEvent(status)) {
            iter.remove();
            modified = true;
            needsReachabilityCheck |= s != t;
          } else if (EventStatus.isFailingEvent(status)) {
            if (t != dump) {
              iter.setCurrentToState(dump);
              modified = needsReachabilityCheck = true;
            }
          } else if (EventStatus.isOutsideOnlySelfloopEvent(status)) {
            if (s == t) {
              iter.remove();
              modified = true;
            }
          }
        }
        if (!tauTargets.isEmpty()) {
          rel.addTransitions(s, EventEncoding.TAU, tauTargets);
          tauTargets.clear();
          final byte tauStatus = rel.getProperEventStatus(EventEncoding.TAU);
          rel.setProperEventStatus(EventEncoding.TAU,
                                   tauStatus & ~EventStatus.STATUS_UNUSED);
        }
      }
    } else {
      // Algorithm for backward transitions ....
      final TransitionIterator iter = rel.createPredecessorsModifyingIterator();
      for (int t = 0; t < numStates; t++) {
        iter.resetState(t);
        while (iter.advance()) {
          final int e = iter.getCurrentEvent();
          if (e == EventEncoding.TAU) {
            continue;
          }
          final int s = iter.getCurrentSourceState();
          final byte status = rel.getProperEventStatus(e);
          if (EventStatus.isLocalEvent(status)) {
            if (s != t) {
              tauTargets.add(s);
            }
            iter.remove();
            modified = true;
          } else if (EventStatus.isBlockedEvent(status)) {
            iter.remove();
            modified = true;
            needsReachabilityCheck |= s != t;
          } else if (EventStatus.isFailingEvent(status)) {
            if (t != dump) {
              iter.remove();
              rel.addTransition(s, e, dump);
              needsReachabilityCheck = modified = true;
            }
          } else if (EventStatus.isOutsideOnlySelfloopEvent(status)) {
            if (s == t) {
              iter.remove();
              modified = true;
            }
          }
        }
        if (!tauTargets.isEmpty()) {
          rel.addTransitions(tauTargets, EventEncoding.TAU, t);
          tauTargets.clear();
          final byte tauStatus = rel.getProperEventStatus(EventEncoding.TAU);
          rel.setProperEventStatus(EventEncoding.TAU,
                                   tauStatus & ~EventStatus.STATUS_UNUSED);
        }
      }
    }

    // Remove events ...
    if (modified) {
      final int numEvents = rel.getNumberOfProperEvents();
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final byte status = rel.getProperEventStatus(e);
        if ((status &
             (EventStatus.STATUS_LOCAL | EventStatus.STATUS_BLOCKED)) != 0) {
          rel.setProperEventStatus(e, status | EventStatus.STATUS_UNUSED);
        }
      }
    }

    // Clean up ...
    if (needsReachabilityCheck) {
      final int config = getPreferredOutputConfiguration();
      if (rel.checkReachability(config)) {
        removeProperSelfLoopEvents();
      }
    }

    return modified;
  }

}
