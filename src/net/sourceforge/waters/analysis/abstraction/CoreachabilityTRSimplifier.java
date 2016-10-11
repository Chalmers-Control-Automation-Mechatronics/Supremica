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

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * For a given Automaton applies an abstraction rule which removes states from
 * which neither an alpha or omega state can be reached.
 *
 * @author Rachel Francis
 */

public class CoreachabilityTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#######################################################################
  //# Constructors
  public CoreachabilityTRSimplifier()
  {
  }

  public CoreachabilityTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    final int alphaID = getPreconditionMarkingID();
    final int defaultID = getDefaultMarkingID();
    final int numStates = rel.getNumberOfStates();
    final TIntHashSet coreachableStates = new TIntHashSet(numStates);
    final TIntStack unvisitedStates = new TIntArrayStack();
    // Creates a hash set of all states which can reach an omega marked or alpha
    // marked state.
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if ((rel.isMarked(sourceID, defaultID) ||
           rel.isMarked(sourceID, alphaID)) &&
          rel.isReachable(sourceID) &&
          coreachableStates.add(sourceID) ) {
        checkAbort();
        unvisitedStates.push(sourceID);
        while (unvisitedStates.size() > 0) {
          final int newSource = unvisitedStates.pop();
          iter.resetState(newSource);
          while (iter.advance()) {
            final int predID = iter.getCurrentSourceState();
            if (rel.isReachable(predID) && coreachableStates.add(predID)) {
              unvisitedStates.push(predID);
            }
          }
        }
      }
    }
    // Remove states which cannot reach a state marked alpha or omega.
    final int numReachable = coreachableStates.size();
    if (numReachable < numStates) {
      boolean modified = false;
      for (int sourceID = 0; sourceID < numStates; sourceID++) {
        if (rel.isReachable(sourceID) && !coreachableStates.contains(sourceID)) {
          rel.setReachable(sourceID, false);
          modified = true;
        }
      }
      applyResultPartitionAutomatically();
      return modified;
    } else {
      return false;
    }
  }

  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.removeUnreachableTransitions();
    rel.removeTauSelfLoops();
    rel.removeProperSelfLoopEvents();
  }

}
