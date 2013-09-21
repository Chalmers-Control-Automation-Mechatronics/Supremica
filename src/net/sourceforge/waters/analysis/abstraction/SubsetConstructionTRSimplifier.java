//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SubsetConstructionTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.util.BitSet;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.IntStateBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.OneEventCachingTransitionIterator;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;


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

  public SubsetConstructionTRSimplifier(final ListBufferTransitionRelation rel)
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

  /**
   * Clears the set of forbidden events.
   * @see #setForbiddenEvent(int,boolean) setForbiddenEvent()
   */
  public void clearForbiddenEvents()
  {
    mForbiddenEvents = null;
  }

  /**
   * Sets whether the given event is to be considered as forbidden.
   * Forbidden events are typically selfloop-only events with the property
   * that state exploration ends as soon as a state with a forbidden event
   * enabled is encountered. When subset construction encounters a state
   * with a forbidden event enabled, it suppresses any further outgoing
   * transitions from that state.
   */
  public void setForbiddenEvent(final int event, final boolean forbidden)
  {
    if (mForbiddenEvents == null) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numEvents = rel == null ? 0 : rel.getNumberOfProperEvents();
      mForbiddenEvents = new BitSet(numEvents);
    }
    mForbiddenEvents.set(event, forbidden);
  }

  /**
   * Returns whether the given event is considered as forbidden.
   * @see #setForbiddenEvent(int,boolean) setForbiddenEvent()
   */
  public boolean isForbiddenEvent(final int event)
  {
    if (mForbiddenEvents == null) {
      return false;
    } else {
      return mForbiddenEvents.get(event);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  @Override
  public boolean isPartitioning()
  {
    return false;
  }

  @Override
  public void reset()
  {
    super.reset();
    mForbiddenEvents = null;
    mSetOffsets = null;
    mStateSetBuffer = null;
    mTransitionBuffer = null;
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, true);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if ((rel.getProperEventStatus(EventEncoding.TAU) &
         EventEncoding.STATUS_UNUSED) == 0) {
      mIsDeterministic = false;
      final TauClosure closure =
        rel.createSuccessorsTauClosure(mTransitionLimit);
      mTauIterator = closure.createIterator();
      mEventIterator = closure.createPostEventClosureIterator(-1);
    } else if (!rel.isDeterministic() || mForbiddenEvents != null) {
      mIsDeterministic = false;
      mTauIterator = null;
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      mEventIterator = new OneEventCachingTransitionIterator(iter);
    } else {
      mIsDeterministic = true;
      return;
    }

    final int numEvents = rel.getNumberOfProperEvents();
    int normalIndex = 0;
    int forbiddenIndex = 0;
    for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
      if ((rel.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
        if (isForbiddenEvent(event)) {
          forbiddenIndex++;
        } else {
          normalIndex++;
        }
      }
    }
    mForbiddenEventIndexes = new int[forbiddenIndex];
    mNormalEventIndexes = new int[normalIndex];
    normalIndex = 0;
    forbiddenIndex = 0;
    for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
      if ((rel.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
        if (isForbiddenEvent(event)) {
          mForbiddenEventIndexes[forbiddenIndex++] = event;
        } else {
          mNormalEventIndexes[normalIndex++] = event;
        }
      }
    }
    if (forbiddenIndex > 0) {
      mForbiddenEventIterator = rel.createSuccessorsReadOnlyIterator();
    }

    final int numStates = rel.getNumberOfStates();
    mSetOffsets = new TIntArrayList(numStates);
    mStateSetBuffer = new IntSetBuffer(numStates);
    mTransitionBuffer = new PreTransitionBuffer(numEvents, mTransitionLimit);
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
      final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
      final TIntArrayList current = new TIntArrayList();
      states:
      for (int source = 0; source < mSetOffsets.size(); source++) {
        final int set = mSetOffsets.get(source);
        for (final int event : mForbiddenEventIndexes) {
          checkAbort();
          iter.reset(set);
          while (iter.advance()) {
            final int state = iter.getCurrentData();
            mForbiddenEventIterator.reset(state, event);
            if (mForbiddenEventIterator.advance()) {
              mTransitionBuffer.addTransition(source, event, source);
              continue states;
            }
          }
        }
        for (final int event : mNormalEventIndexes) {
          checkAbort();
          mEventIterator.resetEvent(event);
          iter.reset(set);
          while (iter.advance()) {
            final int state = iter.getCurrentData();
            mEventIterator.resume(state);
            while (mEventIterator.advance()) {
              final int target = mEventIterator.getCurrentTargetState();
              current.add(target);
            }
          }
          if (!current.isEmpty()) {
            current.sort();
            final int offset = mStateSetBuffer.add(current);
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
      final int numDetStates = mSetOffsets.size();
      final int numProps = rel.getNumberOfPropositions();
      final long usedProps = rel.getUsedPropositions();
      final IntStateBuffer detStates =
        new IntStateBuffer(numDetStates, numProps, usedProps);
      detStates.setInitial(0, true);
      final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
      for (int detstate = 0; detstate < numDetStates; detstate++) {
        long markings = detStates.createMarkings();
        final int offset = mSetOffsets.get(detstate);
        iter.reset(offset);
        while (iter.advance()) {
          final int state = iter.getCurrentData();
          final long stateMarkings = rel.getAllMarkings(state);
          markings = detStates.mergeMarkings(markings, stateMarkings);
        }
        detStates.setAllMarkings(detstate, markings);
      }
      detStates.removeRedundantPropositions();
      mSetOffsets = null;
      mStateSetBuffer = null;
      final int numTrans = mTransitionBuffer.size();
      final int config = getPreferredInputConfiguration();
      rel.reset(detStates, numTrans, config);
      rel.removeEvent(EventEncoding.TAU);
      mTransitionBuffer.addOutgoingTransitions(rel);
      mTransitionBuffer = null;
      if (mForbiddenEvents == null) {
        rel.removeProperSelfLoopEvents();
      } else {
        final TIntArrayList forbiddenVictims =
          new TIntArrayList(mForbiddenEventIndexes.length);
        final int numEvents = rel.getNumberOfProperEvents();
        for (int event = 0; event < numEvents; event++) {
          if (isSelfloopEventExceptInForbiddenStates(event)) {
            if (mForbiddenEvents.get(event)) {
              forbiddenVictims.add(event);
            } else {
              rel.removeEvent(event);
            }
          }
        }
        for (int e = 0; e < forbiddenVictims.size(); e++) {
          final int event = forbiddenVictims.get(e);
          rel.removeEvent(event);
        }
      }
    }
  }

  @Override
  protected void tearDown()
  {
    mForbiddenEventIndexes = mNormalEventIndexes = null;
    mTauIterator = mForbiddenEventIterator = mEventIterator = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean isSelfloopEventExceptInForbiddenStates(final int event)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if ((rel.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      final int numStates = rel.getNumberOfStates();
      states:
      for (int state = 0; state < numStates; state++) {
        if (rel.isReachable(state)) {
          boolean selfloop = false;
          iter.reset(state, event);
          while (iter.advance()) {
            if (iter.getCurrentTargetState() != state) {
              return false;
            } else {
              selfloop = true;
            }
          }
          if (!selfloop) {
            for (final int e : mForbiddenEventIndexes) {
              iter.reset(state, e);
              if (iter.advance()) {
                continue states;
              }
            }
            return false;
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Data Members
  private int mStateLimit = Integer.MAX_VALUE;
  private int mTransitionLimit = Integer.MAX_VALUE;
  private BitSet mForbiddenEvents = null;

  private boolean mIsDeterministic;
  private int[] mForbiddenEventIndexes;
  private int[] mNormalEventIndexes;
  private TransitionIterator mTauIterator;
  private TransitionIterator mEventIterator;
  private TransitionIterator mForbiddenEventIterator;
  private TIntArrayList mSetOffsets;
  private IntSetBuffer mStateSetBuffer;
  private PreTransitionBuffer mTransitionBuffer;

}

