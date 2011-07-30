//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CoreachabilityTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntStack;

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
    final TIntHashSet reachableStates = new TIntHashSet(numStates);
    final TIntStack unvisitedStates = new TIntStack();
    // Creates a hash set of all states which can reach an omega marked or alpha
    // marked state.
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if ((rel.isMarked(sourceID, defaultID) ||
           rel.isMarked(sourceID, alphaID)) &&
          rel.isReachable(sourceID) &&
          reachableStates.add(sourceID) ) {
        checkAbort();
        unvisitedStates.push(sourceID);
        while (unvisitedStates.size() > 0) {
          final int newSource = unvisitedStates.pop();
          iter.resetState(newSource);
          while (iter.advance()) {
            final int predID = iter.getCurrentSourceState();
            if (rel.isReachable(predID) && reachableStates.add(predID)) {
              unvisitedStates.push(predID);
            }
          }
        }
      }
    }
    // Remove states which cannot reach a state marked alpha or omega.
    final int numReachable = reachableStates.size();
    if (numReachable < numStates) {
      boolean modified = false;
      for (int sourceID = 0; sourceID < numStates; sourceID++) {
        if (rel.isReachable(sourceID) && !reachableStates.contains(sourceID)) {
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
