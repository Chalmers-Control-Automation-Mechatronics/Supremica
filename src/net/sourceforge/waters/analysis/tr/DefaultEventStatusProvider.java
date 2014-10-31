//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   DefaultEventStatusProvider
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;



/**
 * A default implementation of the {@link EventStatusProvider} interface.
 * Provides status information for a fixed, unchangeable number of proper
 * events and propositions.
 *
 * @see EventStatus
 *
 * @author Robi Malik
 */

public class DefaultEventStatusProvider
  implements EventStatusProvider
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new status provider for the given number of events and
   * propositions. All events and propositions are initially used, and all
   * status flags are initially clear, except for the tau event
   * ({@link EventEncoding#TAU}), which is marked as
   * {@link EventStatus#STATUS_FULLY_LOCAL}.
   * @param  numEvents  The number of proper events encoded.
   * @param  numProps   The number of propositions encoded.
   * @throws OverflowException to indicate that the number of propositions
   *                    exceeds the supported maximum
   *                    {@link EventStatusProvider#MAX_PROPOSITIONS}.
   */
  public DefaultEventStatusProvider(final int numEvents, final int numProps)
    throws OverflowException
  {
    if (numProps > EventStatusProvider.MAX_PROPOSITIONS) {
      throw new OverflowException
        ("Encoding has " + numProps + " propositions, but " +
         ProxyTools.getShortClassName(this) + " can only handle up to " +
         MAX_PROPOSITIONS + " different propositions!");
    }
    mProperEventStatus = new byte[numEvents];
    mProperEventStatus[EventEncoding.TAU] = EventStatus.STATUS_FULLY_LOCAL;
    mNumberOfPropositions = numProps;
    mUsedPropositions = (1 << numProps) - 1;
  }

  /**
   * Creates a new default event status provider by copying all event status
   * information from the given other event status provider.
   */
  public DefaultEventStatusProvider(final EventStatusProvider provider)
  {
    final int numEvents = provider.getNumberOfProperEvents();
    mProperEventStatus = new byte[numEvents];
    for (int e = 0; e < numEvents; e++) {
      mProperEventStatus[e] = provider.getProperEventStatus(e);
    }
    mNumberOfPropositions = provider.getNumberOfPropositions();
    mUsedPropositions = provider.getUsedPropositions();
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  @Override
  public DefaultEventStatusProvider clone()
  {
    return new DefaultEventStatusProvider(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.tr.EventStatusProvider
  @Override
  public int getNumberOfProperEvents()
  {
    return mProperEventStatus.length;
  }

  @Override
  public byte getProperEventStatus(final int event)
  {
    return mProperEventStatus[event];
  }

  @Override
  public void setProperEventStatus(final int event, final int status)
  {
    mProperEventStatus[event] = (byte) status;
  }

  @Override
  public int getNumberOfPropositions()
  {
    return mNumberOfPropositions;
  }

  @Override
  public boolean isPropositionUsed(final int prop)
  {
    return (mUsedPropositions & (1 << prop)) != 0;
  }

  @Override
  public void setPropositionUsed(final int prop, final boolean used)
  {
    if (used) {
      mUsedPropositions |= (1 << prop);
    } else {
      mUsedPropositions &= ~(1 << prop);
    }
  }

  @Override
  public int getUsedPropositions()
  {
    return mUsedPropositions;
  }

  @Override
  public OrderingInfo getOrderingInfo()
  {
    return null;
  }


  //#########################################################################
  //# Data Members
  private final byte[] mProperEventStatus;
  private final int mNumberOfPropositions;
  private int mUsedPropositions;

}
