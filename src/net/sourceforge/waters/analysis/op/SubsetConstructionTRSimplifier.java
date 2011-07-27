//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   SubsetConstructionTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.OneEventCachingTransitionIterator;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * A list buffer transition relation implementation of the subset
 * construction algorithm.
 *
 * @author Robi Malik
 */

public class SubsetConstructionTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SubsetConstructionTRSimplifier()
  {
    this(null);
  }

  public SubsetConstructionTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the state limit. The states limit specifies the maximum
   * number of states that will be created.
   * @param limit
   *          The new state limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of states.
   */
  public void setStateLimit(final int limit)
  {
    mStateLimit = limit;
  }

  /**
   * Gets the state limit.
   * @see #setStateLimit(int) setStateLimit()
   */
  public int getStateLimit()
  {
    return mStateLimit;
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions that will be created.
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of transitions.
   */
  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  /**
   * Gets the transition limit.
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  @Override
  public void reset()
  {
    super.reset();
    mSetOffsets = null;
    mStateSetBuffer = null;
    mTransitionBuffer = null;
  }

  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, true);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.op.AbstractTRSimplifier
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (rel.isUsedEvent(EventEncoding.TAU)) {
      mIsDeterministic = false;
      final TauClosure closure =
        rel.createSuccessorsTauClosure(mTransitionLimit);
      mTauIterator = closure.createIterator();
      mEventIterator = closure.createPostEventClosureIterator(-1);
    } else if (!rel.isDeterministic()) {
      mIsDeterministic = false;
      mTauIterator = null;
      final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
      mEventIterator = new OneEventCachingTransitionIterator(iter);
    } else {
      mIsDeterministic = true;
      return;
    }
    final int numStates = rel.getNumberOfStates();
    mSetOffsets = new TIntArrayList(numStates);
    mStateSetBuffer = new IntSetBuffer(numStates);
    final int numEvents = rel.getNumberOfProperEvents();
    mTransitionBuffer = new PreTransitionBuffer(numEvents);
  }

  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    if (mIsDeterministic) {
      return false;
    } else {
      // 1. Collect initial state set.
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final TIntHashSet init = new TIntHashSet();
      for (int state = 0; state < numStates; state++) {
        if (rel.isInitial(state)) {
          if (mTauIterator == null) {
            init.add(state);
          } else {
            checkAbort();
            mTauIterator.resetState(state);
            while (mTauIterator.advance()) {
              final int tausucc = mTauIterator.getCurrentTargetState();
              init.add(tausucc);
            }
          }
        }
      }
      int last = 0;
      if (!init.isEmpty()) {
        final int offset = mStateSetBuffer.add(init);
        mSetOffsets.add(offset);
        last = offset;
      } else if (numStates == 0) {
        return false;
      }

      // 2. Expand subset states.
      final int numEvents = rel.getNumberOfProperEvents();
      final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
      final TIntArrayList current = new TIntArrayList();
      for (int source = 0; source < mSetOffsets.size(); source++) {
        final int set = mSetOffsets.get(source);
        for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
          checkAbort();
          iter.reset(set);
          while (iter.advance()) {
            final int state = iter.getCurrentData();
            mEventIterator.reset(state, event);
            while (mEventIterator.advance()) {
              final int target = mEventIterator.getCurrentTargetState();
              current.add(target);
            }
          }
          final int offset = mStateSetBuffer.add(current);
          if (!current.isEmpty()) {
            if (mTransitionBuffer.size() >= mTransitionLimit) {
              throw new OverflowException(OverflowKind.TRANSITION,
                                          mTransitionLimit);
            }
            current.clear();
            final int target;
            if (offset > last) {
              target = mSetOffsets.size();
              if (target >= mStateLimit) {
                throw new OverflowException(OverflowKind.STATE, mStateLimit);
              }
              mSetOffsets.add(offset);
              last = offset;
            } else {
              target = mSetOffsets.binarySearch(offset);
            }
            mTransitionBuffer.addTransition(source, event, target);
          }
        }
      }

      // 3. Build new transition relation.
      applyResultPartitionAutomatically();
      return true;
    }
  }

  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    if (mSetOffsets != null) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final String name = rel.getName();
      final ComponentKind kind = rel.getKind();
      final int numDetStates = mSetOffsets.size();
      final int numEvents = rel.getNumberOfProperEvents();
      final int numProps = rel.getNumberOfPropositions();
      final int config = getPreferredInputConfiguration();
      final ListBufferTransitionRelation detRel =
        new ListBufferTransitionRelation(name, kind, numEvents,
                                         numProps, numDetStates, config);
      detRel.setInitial(0, true);
      final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
      for (int detstate = 0; detstate < numDetStates; detstate++) {
        long markings = detRel.createMarkings();
        final int offset = mSetOffsets.get(detstate);
        iter.reset(offset);
        while (iter.advance()) {
          final int state = iter.getCurrentData();
          final long stateMarkings = rel.getAllMarkings(state);
          markings = detRel.mergeMarkings(markings, stateMarkings);
        }
        detRel.setAllMarkings(detstate, markings);
      }
      mSetOffsets = null;
      mStateSetBuffer = null;
      mTransitionBuffer.addOutgoingTransitions(detRel);
      mTransitionBuffer = null;
      setTransitionRelation(detRel);
    }
  }

  @Override
  protected void tearDown()
  {
    mTauIterator = mEventIterator = null;
  }


  //#########################################################################
  //# Data Members
  private int mStateLimit = Integer.MAX_VALUE;;
  private int mTransitionLimit = Integer.MAX_VALUE;

  private boolean mIsDeterministic;
  private TransitionIterator mTauIterator;
  private TransitionIterator mEventIterator;
  private TIntArrayList mSetOffsets;
  private IntSetBuffer mStateSetBuffer;
  private PreTransitionBuffer mTransitionBuffer;

}
