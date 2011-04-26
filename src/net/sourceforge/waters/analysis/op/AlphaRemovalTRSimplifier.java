//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   AlphaRemovalTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntStack;
import net.sourceforge.waters.analysis.op.EventEncoding;
import net.sourceforge.waters.analysis.op.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.op.TransitionIterator;


/**
 * A transition relation simplifier implementation of the alpha-removal rule.
 *
 * @author Rachel Francis, Robi Malik
 */

public class AlphaRemovalTRSimplifier
  extends AbstractGeneralisedTRSimplifier
{

  //#########################################################################
  //# Constructors
  AlphaRemovalTRSimplifier()
  {
  }

  AlphaRemovalTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Rule Application
  @Override
  public int getPreferredConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
  }

  public boolean run()
  {
    final int tauID = EventEncoding.TAU;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator all =
      rel.createAllTransitionsReadOnlyIterator(tauID);
    if (!all.advance()) {
      // No tau transitions - no simplification
      return false;
    }

    // Visit all alpha-marked states. For each of them, to a depth-first
    // search, removing alpha-markings from all states other than the start
    // state of the search.
    final int alphaID = getPreconditionMarkingID();
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    final int numStates = rel.getNumberOfStates();
    final TIntHashSet reachableStates = new TIntHashSet();
    final TIntStack unvisitedStates = new TIntStack();
    boolean modified = false;
    for (int stateID = 0; stateID < numStates; stateID++) {
      if (rel.isMarked(stateID, alphaID)) {
        unvisitedStates.push(stateID);
        reachableStates.add(stateID);
        while (unvisitedStates.size() > 0) {
          final int newStateID = unvisitedStates.pop();
          if (newStateID != stateID && rel.isMarked(newStateID, alphaID)) {
            rel.setMarked(newStateID, alphaID, false);
            modified = true;
          }
          iter.reset(newStateID, tauID);
          while (iter.advance()) {
            final int predID = iter.getCurrentSourceState();
            if (reachableStates.add(predID)) {
              unvisitedStates.push(predID);
            }
          }
        }
        reachableStates.clear();
      }
    }
    return modified;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }

}
