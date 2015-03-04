//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SelfloopSubsumptionTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.BitSet;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A transition relation simplifier to remove selfloops that are
 * redundant by conflict equivalence.</P>
 *
 * <P>Removes selfloops from states&nbsp;<I>t</I>, and if every branch
 * of tau-successors of&nbsp;<I>t</I> contains a state with all the
 * selfloops to be removed. This simplifier requires the input automaton
 * to be tau-loop free.</P>
 *
 * @author Robi Malik
 */

public class SelfloopSubsumptionTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SelfloopSubsumptionTRSimplifier()
  {
  }

  SelfloopSubsumptionTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  @Override
  public boolean isPartitioning()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, false, true, false);
    return setStatistics(stats);
  }

  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int numEvents = rel.getNumberOfProperEvents();
    mProcessedStates = new BitSet(numStates);
    mTauIterator = rel.createSuccessorsReadOnlyIterator();
    mTauIterator.resetEvent(EventEncoding.TAU);
    mEventIterator = rel.createSuccessorsReadOnlyIterator();
    mEventIterator.resetEvents(EventEncoding.NONTAU, numEvents - 1);
    final TauClosure closure = rel.createSuccessorsTauClosure(0);
    mClosureIterator = closure.createFullEventClosureIterator();
    mSelfloopEvents = new TIntArrayList(numEvents - 1);
    mStack = new TIntArrayStack();
  }

  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    boolean removed = false;
    for (int s = 0; s < numStates; s++) {
      if (rel.isReachable(s)) {
        removed |= removeSelfloops(s);
      }
    }
    return removed;
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mProcessedStates = null;
    mTauIterator = null;
    mEventIterator = null;
    mClosureIterator = null;
    mSelfloopEvents = null;
    mStack = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean removeSelfloops(final int source)
  {
    boolean removed = false;
    if (mProcessedStates.get(source)) {
      return false;
    }
    mProcessedStates.set(source);

    // 1. Process tau-successors
    boolean hasTauSuccessor = false;
    mTauIterator.resetState(source);
    while (mTauIterator.advance()) {
      final int t = mTauIterator.getCurrentTargetState();
      removed |= removeSelfloops(t);
      hasTauSuccessor = true;
    }
    if (!hasTauSuccessor) {
      return removed;
    }

    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int defaultID = getDefaultMarkingID();
    if (defaultID >= 0 && rel.isMarked(source, defaultID)) {
      return removed;
    }

    // 2. Collect selfloops
    mEventIterator.resetState(source);
    while (mEventIterator.advance()) {
      if (mEventIterator.getCurrentTargetState() == source) {
        final int e = mEventIterator.getCurrentEvent();
        mSelfloopEvents.add(e);
      } else {
        mSelfloopEvents.clear();
        return removed;
      }
    }
    if (mSelfloopEvents.isEmpty()) {
      return removed;
    }

    // 3. Explore tau-successors
    boolean found = false;
    final TIntHashSet selfloops = new TIntHashSet(mSelfloopEvents);
    final TIntHashSet visited = new TIntHashSet();
    mTauIterator.resetState(source);
    while (mTauIterator.advance()) {
      final int s = mTauIterator.getCurrentTargetState();
      visited.add(s);
      mStack.push(s);
    }
    search:
    while (mStack.size() > 0) {
      // Visit each state ...
      final int s = mStack.pop();
      found = true;
      // If the state has all the selfloops, then done with this state
      mClosureIterator.resetState(s);
      for (int i = 0; i < mSelfloopEvents.size() && found; i++) {
        final int e = mSelfloopEvents.get(i);
        mClosureIterator.resetEvent(e);
        found = false;
        while (mClosureIterator.advance()) {
          if (mClosureIterator.getCurrentTargetState() == s) {
            found = true;
            break;
          }
        }
      }
      if (found) {
        continue search;
      }
      // Otherwise if the state has an outgoing proper event, then fail
      if (defaultID >= 0 && rel.isMarked(s, defaultID)) {
        break search;
      }
      mEventIterator.resetState(s);
      while (mEventIterator.advance()) {
        if (mEventIterator.getCurrentTargetState() != s) {
          break search;
        }
        final int e = mEventIterator.getCurrentEvent();
        if (!selfloops.contains(e)) {
          break search;
        }
      }
      // Otherwise enqueue tau-successors
      mTauIterator.resetState(s);
      while (mTauIterator.advance()) {
        final int t = mTauIterator.getCurrentTargetState();
        mClosureIterator.resetState(t);
        if (visited.add(t)) {
          mStack.push(t);
        }
        found = true;
      }
      // If no tau-successors, then fail
      if (!found) {
        break search;
      }
    }
    // 4. If the search was successful, then remove all the selfloops
    if (found) {
      for (int i = 0; i < mSelfloopEvents.size(); i++) {
        final int e = mSelfloopEvents.get(i);
        rel.removeTransition(source, e, source);
      }
      removed = true;
    }
    mStack.clear();
    mSelfloopEvents.clear();
    return removed;
  }


  //#########################################################################
  //# Data Members
  private BitSet mProcessedStates;
  private TransitionIterator mTauIterator;
  private TransitionIterator mEventIterator;
  private TransitionIterator mClosureIterator;
  private TIntArrayList mSelfloopEvents;
  private TIntArrayStack mStack;

}

