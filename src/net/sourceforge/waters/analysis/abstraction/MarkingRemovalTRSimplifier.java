//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   AlphaRemovalTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * A transition relation simplifier implementation to minimise marking in
 * an automaton. Markings are removed from all states from which a marked
 * state can be silently reached. This generalises the alpha-removal rule
 * to all propositions. This simplifier assumes that the input automaton does
 * not contain any loops of silent transitions.
 *
 * @author Rachel Francis, Robi Malik
 */

public class MarkingRemovalTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public MarkingRemovalTRSimplifier()
  {
  }

  MarkingRemovalTRSimplifier(final ListBufferTransitionRelation rel)
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

  @Override
  public boolean isPartitioning()
  {
    return true;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }

  @Override
  public boolean isReducedMarking(final int propID)
  {
    return mReducedMarkings[propID];
  }

  @Override
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

    // For each proposition, visit all marked states. For each of them, do a
    // depth-first search, removing markings from all states encountered
    // except the start state of the state.
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    final int numStates = rel.getNumberOfStates();
    final TIntHashSet visitedStates = new TIntHashSet();
    final TIntStack unvisitedStates = new TIntArrayStack();
    final int numProps = rel.getNumberOfPropositions();
    boolean modified = false;
    mReducedMarkings = new boolean[numProps];
    for (int prop = 0; prop < rel.getNumberOfPropositions(); prop++) {
      for (int stateID = 0; stateID < numStates; stateID++) {
        if (rel.isMarked(stateID, prop)) {
          checkAbort();
          iter.reset(stateID, tauID);
          while (iter.advance()) {
            final int predID = iter.getCurrentSourceState();
            if (visitedStates.add(predID)) {
              unvisitedStates.push(predID);
            }
          }
          while (unvisitedStates.size() > 0) {
            checkAbort();
            final int newStateID = unvisitedStates.pop();
            if (rel.isMarked(newStateID, prop)) {
              rel.setMarked(newStateID, prop, false);
              mReducedMarkings[prop] = true;
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
      modified |= mReducedMarkings[prop];
      visitedStates.clear();
    }
    return modified;
  }


  //#########################################################################
  //# Data Members
  private boolean[] mReducedMarkings;

}

