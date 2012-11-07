//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   MarkingSaturationTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntStack;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * A transition relation simplifier to saturate an automaton with markings.
 * Markings are added to all states from where a marked state can be
 * reached silently, for all propositions in the automaton. This simplifier
 * assumes that the input automaton does not contain any loops of silent
 * transitions.
 *
 * @author Robi Malik
 */

public class MarkingSaturationTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public MarkingSaturationTRSimplifier()
  {
  }

  MarkingSaturationTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
  }

  public boolean isPartitioning()
  {
    return true;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }

  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, false, true);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final int tauID = EventEncoding.TAU;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator all =
      rel.createAllTransitionsReadOnlyIterator(tauID);
    if (!all.advance()) {
      // No tau transitions - no simplification
      return false;
    }

    // For each proposition, visit all marked states. For each of them, do
    // a depth-first search following all tau-transitions, adding markings to
    // all states encountered.
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    final int numStates = rel.getNumberOfStates();
    final TIntHashSet visitedStates = new TIntHashSet();
    final TIntStack unvisitedStates = new TIntStack();
    boolean modified = false;
    for (int prop = 0; prop < rel.getNumberOfPropositions(); prop++) {
      for (int stateID = 0; stateID < numStates; stateID++) {
        if (rel.isReachable(stateID) &&
            rel.isMarked(stateID, prop) &&
            visitedStates.add(stateID)) {
          checkAbort();
          unvisitedStates.push(stateID);
          while (unvisitedStates.size() > 0) {
            final int newStateID = unvisitedStates.pop();
            if (!rel.isMarked(newStateID, prop)) {
              rel.setMarked(newStateID, prop, true);
              modified = true;
            }
            iter.reset(newStateID, tauID);
            while (iter.advance()) {
              final int predID = iter.getCurrentSourceState();
              if (visitedStates.add(predID)) {
                unvisitedStates.push(predID);
              }
            }
          }
        }
      }
      visitedStates.clear();
    }
    return modified;
  }

}
