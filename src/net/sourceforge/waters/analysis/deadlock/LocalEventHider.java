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

import gnu.trove.set.hash.TIntHashSet;


public class LocalEventHider
{
  private final GeneralizedTransitionRelation mTransitionRelation;
  private final int TAU_INDEX = 0;

  public LocalEventHider(final GeneralizedTransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
  }

  public void run(final int[] events)
  {
    //final int[] events = getEventsToHide(des);
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      for (int e = 0; e < events.length; e++) {
//        TIntHashSet succs = mTransitionRelation.getSuccessors(s, events[e]);
        TIntHashSet succs = mTransitionRelation.getFromArray(s, events[e], mTransitionRelation.getSuccessorsArr());

        if (succs == null) {
          continue;
        }

        final int[] array = succs.toArray();
        //succs.clear();
        succs = null;
        final TIntHashSet active = mTransitionRelation.getFromArray(s, mTransitionRelation.getAllActiveEvents());
        active.remove(e);
        succs =  mTransitionRelation.getFromArray(s, TAU_INDEX, mTransitionRelation.getSuccessorsArr());
        for (int ti = 0; ti < array.length; ti++) {
          final int t = array[ti];
          TIntHashSet preds = mTransitionRelation.getFromArray(t, events[e], mTransitionRelation.getPredecessorsArr());
          preds.remove(s);
          if(preds.isEmpty()) {
            preds = null;
          }
         if(t != s) {
          succs.add(t);
          preds =  mTransitionRelation.getFromArray(t, TAU_INDEX, mTransitionRelation.getPredecessorsArr());
          preds.add(s);
          }
        }

      }
    }

    // remove all annotations
    for(int i=0; i<events.length; i++) {
      mTransitionRelation.removeAllAnnotations(events[i]);
      mTransitionRelation.removeEvent(events[i]);
    }

  }

  //#########################################################################
  //# Auxiliary Methods

}
