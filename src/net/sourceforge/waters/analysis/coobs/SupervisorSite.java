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

package net.sourceforge.waters.analysis.coobs;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.TRMonolithicCoobservabilityChecker;
import net.sourceforge.waters.model.analysis.des.CoobservabilityChecker;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>A record about a supervisor site for the purpose of verifying
 * coobservability.</P>
 *
 * <P>A supervisor site represents a set of events that can be
 * controlled and/or observed by a particular supervisor. This class
 * only contains the site name as defined through the event attributes
 * and some index details needed by the algorithm in class
 * {@link TRMonolithicCoobservabilityChecker}.</P>
 *
 * @author Robi Malik
 * @see CoobservabilityChecker
 */

public class SupervisorSite implements Comparable<SupervisorSite>
{
  //#########################################################################
  //# Constructor
  public SupervisorSite(final String name,
                        final boolean reference,
                        final int index,
                        final int numAutomata)
  {
    mName = name;
    mReference = reference;
    if (reference) {
      mControlledEvents = mObservedEvents = null;
    } else {
      mControlledEvents = new THashSet<>();
      mObservedEvents = new THashSet<>();
    }
    mIndex = index;
    mComponentIndices = new int[numAutomata];
  }


  //#########################################################################
  //# Simple Access
  public String getName()
  {
    return mName;
  }

  public boolean isReferenceSite()
  {
    return mReference;
  }

  public Set<EventProxy> getControlledEvents()
  {
    return mControlledEvents;
  }

  public void addControlledEvent(final EventProxy event)
  {
    mControlledEvents.add(event);
  }

  public Set<EventProxy> getObservedEvents()
  {
    return mObservedEvents;
  }

  public void addObservedEvent(final EventProxy event)
  {
    mObservedEvents.add(event);
  }

  public int getComponentIndex(final int autIndex)
  {
    return mComponentIndices[autIndex];
  }

  public void setComponentIndex(final int autIndex, final int compIndex)
  {
    mComponentIndices[autIndex] = compIndex;
  }


  //#########################################################################
  //# Interface java.util.Comparable<SupervisorSite>
  @Override
  public int compareTo(final SupervisorSite site)
  {
    return mIndex - site.mIndex;
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return mName;
  }


  //#########################################################################
  //# Instance Variables
  private final String mName;
  private final boolean mReference;
  private Set<EventProxy> mControlledEvents;
  private Set<EventProxy> mObservedEvents;
  private final int mIndex;
  private final int[] mComponentIndices;

}
