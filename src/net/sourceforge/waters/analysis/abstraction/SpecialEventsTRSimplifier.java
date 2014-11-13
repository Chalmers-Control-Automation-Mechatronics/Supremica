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
    throws AnalysisAbortException
  {
    // Set up ...
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
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

    // Remove/redirect transitions ...
    for (int from = 0; from < numStates; from++) {
      iter.resetState(from);
      while (iter.advance()) {
        checkAbort();
        final int e = iter.getCurrentEvent();
        if (e == EventEncoding.TAU) {
          continue;
        }
        final int to = iter.getCurrentToState();
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
            if (to != dump) {
              iter.setCurrentToState(dump);
              modified = needsReachabilityCheck = true;
            }
          } else {
            if (from != dump) {
              iter.remove();
              rel.addTransition(to, e, dump);
              modified = needsReachabilityCheck = true;
            }
          }
        } else if (EventStatus.isOutsideOnlySelfloopEvent(status)) {
          if (from == to) {
            iter.remove();
            modified = true;
          }
        }
      }
      if (!tauTargets.isEmpty()) {
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

    // Remove events ...
    //   (Check even if not modified, as there may be blocked events
    //   that appear on no transitions.
    final int numEvents = rel.getNumberOfProperEvents();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      checkAbort();
      final byte status = rel.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status) &&
          (status &
           (EventStatus.STATUS_LOCAL | EventStatus.STATUS_BLOCKED)) != 0) {
        rel.setProperEventStatus(e, status | EventStatus.STATUS_UNUSED);
        modified = true;
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
