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

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;


public class DiamondEliminator
{
  private final GeneralizedTransitionRelation mTransitionRelation;
  private final TIntHashSet[] mStatesReachableUnderTau;
  private final TIntHashSet[] mVisibleEvents;
  private final TIntHashSet[][] mStatesReachableUnderVisibleEvents;
  private final TIntHashSet[][] mtauMin;
  private final int TAU_INDEX = 0;

  public DiamondEliminator(final GeneralizedTransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
    mStatesReachableUnderTau =
      new TIntHashSet[transitionrelation.numberOfStates()];
    mStatesReachableUnderVisibleEvents = new TIntHashSet[transitionrelation
      .numberOfStates()][transitionrelation.numberOfEvents()];
    mtauMin = new TIntHashSet[transitionrelation
      .numberOfStates()][transitionrelation.numberOfEvents()];
    mVisibleEvents = new TIntHashSet[transitionrelation.numberOfStates()];
  }

  public GeneralizedTransitionRelation run()
  {
    if (!isThereTau()) {
      return mTransitionRelation;
    }
    GeneralizedTransitionRelation gtr =
      new GeneralizedTransitionRelation(this.mTransitionRelation);
    final TIntHashSet  visitedStates = new TIntHashSet();
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      final TIntHashSet taussuccs =
        this.mTransitionRelation.getSuccessors(s, TAU_INDEX);
      visitedStates.add(s);
      if (taussuccs == null) {
        gtr = this.specialState(s, gtr);
        continue;
      }
      TIntHashSet taus = this.mTransitionRelation
        .getFromArray(s, this.mStatesReachableUnderTau);
      taus.add(s);
      taus = exploreTauReachableStates(s, taus);
      /*
       * final int[] taussuccsArr = taussuccs.toArray(); for (int ti = 0; ti <
       * taussuccsArr.length; ti++) { final int t = taussuccsArr[ti];
       * taus.add(t); }
       */

      //----get visible events (i.e. set of V(N) )
      final TIntHashSet visible =
        this.mTransitionRelation.getFromArray(s, this.mVisibleEvents);
      final TIntIterator iter = taus.iterator();
      while (iter.hasNext()) {
        final int t = iter.next();
        for (int e = 1; e < this.mTransitionRelation.numberOfEvents(); e++) {
          final EventProxy eProxy = this.mTransitionRelation.getEvent(e);
          if (eProxy.getKind() == EventKind.PROPOSITION)
            continue;
          final TIntHashSet activeSuccs =
            this.mTransitionRelation.getSuccessors(t, e);
          if (activeSuccs == null)
            continue;
          visible.add(e);
        }
      }

      // get set of Na = N
      final TIntIterator vIter = visible.iterator();
      while (vIter.hasNext()) {
        final int event = vIter.next();
        final TIntHashSet eNode = this.mTransitionRelation
          .getFromArray(s, event, this.mStatesReachableUnderVisibleEvents);
        final TIntIterator nIter = taus.iterator();
        while (nIter.hasNext()) {
          final int n = nIter.next();
          final TIntHashSet nodes =
            this.mTransitionRelation.getSuccessors(n, event);
          if (nodes == null)
            continue;
          final int[] nodesArr = nodes.toArray();
          for (int ti = 0; ti < nodesArr.length; ti++) {
            final int t = nodesArr[ti];
            eNode.add(t);
          }
        }

        // get set min(Na) ..
        final TIntHashSet tauMinNodes =
          this.mTransitionRelation.getFromArray(s, event, this.mtauMin);
        final TIntIterator eNodeIter = eNode.iterator();
        while (eNodeIter.hasNext()) {
          final int n = eNodeIter.next();
          if (eNode.size() == 1) {
            tauMinNodes.add(n);
          } else {
            final TIntHashSet nodes =
              this.mTransitionRelation.getPredecessors(n, this.TAU_INDEX);
            if (nodes == null)
              tauMinNodes.add(n);
            else {

              final int[] nodesArr = nodes.toArray();
              for (int ti = 0; ti < nodesArr.length; ti++) {
                final int t = nodesArr[ti];
                // is t in eNode !
                if (eNode.contains(t)) {
                  tauMinNodes.add(t);
                }
              }
            }
          }
        }

        // add transition from N to N' in gtr ..
        final TIntIterator tauMinIter = tauMinNodes.iterator();
        while (tauMinIter.hasNext()) {
          final int target = tauMinIter.next();
          gtr.addTransition(s, event, target);
        }
      }
    }
    return gtr;
  }

  //#########################################################################
  //# Auxiliary Methods

  public GeneralizedTransitionRelation specialState(final int state,
                                                    final GeneralizedTransitionRelation gtr)
  {
    boolean isReachable = false;
    for (int e = 1; e < gtr.numberOfEvents(); e++) {
      final TIntHashSet preds = gtr.getPredecessors(state, e);
      if (preds != null) {
        isReachable = true;
        break;
      }
    }

    if (isReachable) {
      for (int e = 1; e < this.mTransitionRelation.numberOfEvents(); e++) {
        final TIntHashSet succs =
          this.mTransitionRelation.getSuccessors(state, e);
        if (succs == null)
          continue;

        final int[] nodesArr = succs.toArray();
        for (int ti = 0; ti < nodesArr.length; ti++) {
          final int t = nodesArr[ti];
          gtr.addTransition(state, e, t);
        }
      }
    }
    return gtr;
  }

  public final TIntHashSet exploreTauReachableStates(final int state,
                                                     final TIntHashSet taus)
  {
    final TIntHashSet taussuccs =
      this.mTransitionRelation.getSuccessors(state, TAU_INDEX);
    if (taussuccs == null) {
      return taus;
    } else {
      final int[] taussuccsArr = taussuccs.toArray();
      for (int ti = 0; ti < taussuccsArr.length; ti++) {
        final int t = taussuccsArr[ti];
        taus.add(t);
        exploreTauReachableStates(t, taus);
      }
    }
    return taus;
  }

  public boolean isThereTau()
  {
    final EventProxy tau = this.mTransitionRelation.getEvent(TAU_INDEX);
    if (!tau.isObservable() && tau.getKind() == EventKind.UNCONTROLLABLE
        && tau.getName().equals(":tau"))
      return true;
    else
      return false;
  }

}
