//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
 * A transition relation simplifier to remove non-coreachable states.
 *
 * A state is coreachable if it is possible to reach a marked state from
 * that state. This simplifier removes all states found to be not coreachable
 * together with any transitions linked to them, and all states that become
 * unreachable as a result are also removed. By default, whether a state is
 * marked is determined by the default (omega) marking, but this behaviour
 * can be overridden by a subclass.
 *
 * @author Rachel Francis, Robi Malik
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
  //# Configuration
  /**
   * Sets how the dump state is treated in abstraction.
   * If this setting is false (the default), the dump state is treated
   * as an ordinary non-coreachable state that gets removed.
   * If this setting is true, the dump state is considered as coreachable
   * (but not marked), so it is only removed if it becomes unreachable
   * by the removal of other states.
   */
  public void setKeepingDumpState(final boolean keeping)
  {
    mKeepingDumpState = keeping;
  }

  /**
   * Returns how the dump state is treated in abstraction.
   * @see #setKeepingDumpState(boolean)
   */
  public boolean isKeepingDumpState()
  {
    return mKeepingDumpState;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
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
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    if (isTriviallyUnchanged()) {
      return false;
    }

    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    final int numStates = rel.getNumberOfStates();
    final TIntHashSet coreachableStates = new TIntHashSet(numStates);
    final TIntStack unvisitedStates = new TIntArrayStack();
    // Creates a hash set of all states which can reach a marked state.
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (rel.isReachable(sourceID) &&
          isMarked(sourceID) &&
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
    // Special treatment of dump state.
    if (mKeepingDumpState) {
      final int dumpIndex = rel.getDumpStateIndex();
      if (rel.isReachable(dumpIndex)) {
        iter.resetState(dumpIndex);
        while (iter.advance()) {
          final int s = iter.getCurrentSourceState();
          if (coreachableStates.contains(s)) {
            coreachableStates.add(dumpIndex);
            break;
          }
        }
      }
    }
    // Remove states which cannot reach a marked state.
    boolean modified = false;
    final int numCoreachable = coreachableStates.size();
    if (numCoreachable < numStates) {
      for (int sourceID = 0; sourceID < numStates; sourceID++) {
        if (rel.isReachable(sourceID) &&
            !coreachableStates.contains(sourceID)) {
          rel.setReachable(sourceID, false);
          modified = true;
        }
      }
      if (modified) {
        applyResultPartitionAutomatically();
      }
    }
    return modified;
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


  //#########################################################################
  //# Hooks
  protected boolean isTriviallyUnchanged()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int defaultID = getDefaultMarkingID();
    return !rel.isPropositionUsed(defaultID);
  }

  protected boolean isMarked(final int state)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int defaultID = getDefaultMarkingID();
    return rel.isMarked(state, defaultID);
  }


  //#########################################################################
  //# Data Members
  private boolean mKeepingDumpState = false;

}
