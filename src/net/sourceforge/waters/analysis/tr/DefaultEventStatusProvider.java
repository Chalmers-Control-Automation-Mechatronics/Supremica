//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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


  //#########################################################################
  //# Data Members
  private final byte[] mProperEventStatus;
  private final int mNumberOfPropositions;
  private int mUsedPropositions;

}
