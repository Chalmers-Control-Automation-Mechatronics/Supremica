//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   HidingTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;


/**
 * A transition relation simplifier that implements hiding.
 * This simplifier replaces all local events found in its input
 * transition relation (i.e., all events with {@link
 * EventEncoding#STATUS_LOCAL}) with silent events
 * ({@link EventEncoding#TAU}).
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class BlockedEventsRemovalTRSimplifier
  extends AbstractTRSimplifier
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
    final int mask = EventEncoding.STATUS_BLOCKED | EventEncoding.STATUS_UNUSED;
    final int pattern = mask & ~EventEncoding.STATUS_UNUSED;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    boolean modified = false;
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if ((status & mask) == pattern) {
        modified |= rel.removeEvent(e, false);
      }
    }
    return modified;
  }

}
