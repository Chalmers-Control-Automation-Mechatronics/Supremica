//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   RemovalOfAlphaMarkingsRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.BitSet;

import gnu.trove.TIntStack;

import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * For a given Automaton applies an abstraction rule which removes the default
 * marking proposition from states which are not reachable from any state with
 * an alpha marking.
 *
 * @author Rachel Francis
 */

class OmegaRemovalTRSimplifier
  extends AbstractGeneralisedTRSimplifier
{
  //#########################################################################
  //# Constructors
  OmegaRemovalTRSimplifier()
  {
  }

  OmegaRemovalTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  @Override
  public int getPreferredConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  public boolean run()
    throws AnalysisException
  {
    setUp();
    final int alphaID = getPreconditionMarkingID();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final BitSet reachableStates = new BitSet(numStates);
    final TIntStack unvisitedStates = new TIntStack();
    // Create a bit set of all states which are reachable from an
    // alpha-marked state ...
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (rel.isMarked(sourceID, alphaID) && !reachableStates.get(sourceID)) {
        reachableStates.set(sourceID);
        unvisitedStates.push(sourceID);
        while (unvisitedStates.size() > 0) {
          final int newSource = unvisitedStates.pop();
          iter.resetState(newSource);
          while (iter.advance()) {
            final int targetID = iter.getCurrentTargetState();
            if (!reachableStates.get(targetID)) {
              reachableStates.set(targetID);
              unvisitedStates.push(targetID);
            }
          }
        }
      }
    }
    // Remove default marking from all states which were found to be
    // not reachable ...
    final int defaultID = getDefaultMarkingID();
    boolean modified = false;
    int sourceID = reachableStates.nextClearBit(0);
    while (sourceID < numStates) {
      if (rel.isMarked(sourceID, defaultID)) {
        rel.setMarked(sourceID, defaultID, false);
        modified = true;
      }
      sourceID = reachableStates.nextClearBit(sourceID + 1);
    }
    return modified;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }

}
