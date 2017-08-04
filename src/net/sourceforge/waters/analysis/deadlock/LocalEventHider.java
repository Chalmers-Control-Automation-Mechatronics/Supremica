//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.deadlock;

import java.util.Set;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.EventKind;

import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;


public class LocalEventHider
{
  private final GeneralizedTransitionRelation mTransitionRelation;
  private final int TAU=0;

  public LocalEventHider(final GeneralizedTransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
  }

  public void run(final ProductDESProxy des)
  {
    for(int s=0; s< mTransitionRelation.numberOfStates(); s++) {

      for (final EventProxy ep : getEventsToHide(des)) {
        final int epIndex = mTransitionRelation.getEventInt(ep);
        final TIntHashSet succs = mTransitionRelation.getSuccessors(s, epIndex);
        if(succs ==null) {
          return;
        }
        mTransitionRelation.setSuccessors(s, epIndex, TAU);
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods

  private Set<EventProxy> getEventsToHide(final ProductDESProxy des) {
    final Set<EventProxy> events = mTransitionRelation.getEvents();
    final Set<EventProxy> localEvents = new THashSet<EventProxy>();

    for (final EventProxy ep : events) {
      // we need not equal to tau and not observable ? instead !
      if (ep.getKind() == EventKind.CONTROLLABLE && !ep.isObservable()) {
        localEvents.add(ep);
      }
    }
    return localEvents;
  }

}
