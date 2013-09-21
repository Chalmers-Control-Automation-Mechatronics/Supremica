//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   BlockedEventsRemovalTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;


/**
 * A transition relation simplifier to remove blocked events.
 * This simplifier removes all blocked events found in its input
 * transition relation (i.e., all events with {@link
 * EventEncoding#STATUS_BLOCKED}). These events are marked as unused.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class BlockedEventsRemovalTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructor
  public BlockedEventsRemovalTRSimplifier()
  {
  }

  public BlockedEventsRemovalTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether this simplifier should consider deadlock states when
   * removing selfloops.
   * @see #isDumpStateAware()
   */
  public void setDumpStateAware(final boolean aware)
  {
    mDumpStateAware = aware;
  }

  @Override
  public boolean isDumpStateAware()
  {
    return mDumpStateAware;
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
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  {
    final int mask = EventEncoding.STATUS_BLOCKED | EventEncoding.STATUS_UNUSED;
    final int pattern = mask & ~EventEncoding.STATUS_UNUSED;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    boolean modified = false;
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if ((status & mask) == pattern) {
        rel.removeEvent(e);
        modified = true;
      }
    }
    if (modified) {
      final int config = getPreferredOutputConfiguration();
      if (rel.checkReachability(config)) {
        removeProperSelfLoopEvents();
        rel.removeRedundantPropositions();
        final TRPartition partition =
          TRPartition.createReachabilityPartition(rel);
        setResultPartition(partition);
      }
    }
    return modified;
  }


  //#########################################################################
  //# Data Members
  private boolean mDumpStateAware;

}
