//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SilentContinuationTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import gnu.trove.HashFunctions;
import gnu.trove.TIntHashSet;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersIntHashingStrategy;
import net.sourceforge.waters.analysis.tr.WatersIntIntHashMap;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * <P>An implementation of the <I>Silent Continuation Rule</I>.</P>
 *
 * <P>This rule merges all states that are incoming equivalent and have
 * at least one outgoing silent transition.</P>
 *
 * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional Verification
 * in Supervisory Control. SIAM Journal of Control and Optimization,
 * 48(3), 1914-1938, 2009.</P>
 *
 * @author Robi Malik
 */

public class EnabledEventsSilentContinuationTRSimplifier
  extends AbstractTRSimplifier
{

  //#######################################################################
  //# Constructors
  public EnabledEventsSilentContinuationTRSimplifier()
  {
  }

  public EnabledEventsSilentContinuationTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be stored.
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow an
   *          unlimited number of transitions.
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

  public void setNumberOfEnabledEvents(final int numEnabledEvents)
  {

    mNumberOfEnabledEvents = numEnabledEvents;
  }
  public int getNumberOfEnabledEvents()
  {
    return mNumberOfEnabledEvents;

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

  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, false);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (!rel.isUsedEvent(EventEncoding.TAU) && mNumberOfEnabledEvents == 0) {
      return false;
    }
    final int numStates = rel.getNumberOfStates();
    final BitSet candidates = new BitSet(numStates);

    //The old stuff
    //final TransitionIterator iter =                                 //This is typical transition relation
    //  rel.createAllTransitionsReadOnlyIterator(EventEncoding.TAU);      //loop over all TAU TAU TAU transitions in  tr buffer

    final TransitionIterator iter =
      rel.createAllTransitionsReadOnlyIterator();
    iter.resetEvents(0, mNumberOfEnabledEvents);        //Iterating over events with outgoing Tau or always enabled events.

    while (iter.advance()) {        //for each transition
      final int source = iter.getCurrentSourceState();      //find source state
      candidates.set(source);                       //candidates will have one bit for each state with outgoing transition
    }           //great big string of 0s and 1s which say if state has an outgoing transition or not
    final int numCandidates = candidates.cardinality();
    if (numCandidates == 0) {           //if there are no outgoing transitions
      return false;                     //can't simplify
    }
    final WatersIntHashingStrategy strategy = //something in hashingstrategy does the rule //group together incoming equivalence states
      new IncomingEquivalenceStateHash();
    final WatersIntIntHashMap map =
      new WatersIntIntHashMap(numCandidates, IntListBuffer.NULL, strategy);
    final IntListBuffer prepartition = new IntListBuffer();
    final int[] lists = new int [numStates];
    for (int state = candidates.nextSetBit(0); state >= 0;
         state = candidates.nextSetBit(state + 1)) {
      checkAbort();
      int list = map.get(state);
      if (list == IntListBuffer.NULL) {
        list = prepartition.createList();
        map.put(state, list);
      }
      prepartition.append(list, state);
      lists[state] = list;
    }                                               //end of incoming equivalence stuff
    final int numClasses = map.size();          //how many equivalence classes have been found
    if (numClasses == numCandidates) {        //candidates was above      //just for incoming equivalence.
      return false;                     //there are no incoming equivalence states
    } else {
      final int numSingles = numStates - numCandidates; //states which are not incoming equivalent to anything but themselves
      final List<int[]> partition=
        new ArrayList<int[]>(numClasses + numSingles);
      for (int state = 0; state < numStates; state++) {
        checkAbort();
        final int list = lists[state];
        if (list != IntListBuffer.NULL) {
          final int first = prepartition.getFirst(list);
          if (state == first) {
            final int[] clazz = prepartition.toArray(list);
            partition.add(clazz);
          }
        } else if (rel.isReachable(state)) {
          final int[] clazz = new int[1];
          clazz[0] = state;
          partition.add(clazz);
        }
      }
      setResultPartitionList(partition);
      applyResultPartitionAutomatically();
      return true;
    }
  }

  @Override
  protected void applyResultPartition()
    throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.removeTauSelfLoops();
    rel.removeProperSelfLoopEvents();
  }


  //#########################################################################
  //# Inner Class IncomingEquavalenceStateHash
  private class IncomingEquivalenceStateHash
    implements WatersIntHashingStrategy
  {

    //#######################################################################
    //# Constructor
    private IncomingEquivalenceStateHash()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final TauClosure closure = rel.createPredecessorsTauClosure(mTransitionLimit);
      mBackwardsTauClosureIterator1 = closure.createIterator();
      mBackwardsEventIterator = rel.createPredecessorsReadOnlyIterator();
      mBackwardsTauClosureIterator2 = closure.createIterator();
      mCurrentSet1 = new TIntHashSet();
      mCurrentSet2 = new TIntHashSet();
      mPreviousRoot = -1;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.abstraction.WatersIntHashingStrategy
    public int computeHashCode(final int root)
    {
      if (root == mPreviousRoot) {
        return mCachedResult;
      } else {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int numProperEvents = rel.getNumberOfProperEvents();
        final int numStates = rel.getNumberOfStates();
        final int shift = AutomatonTools.log2(numStates);
        int result = 0;
        mCurrentSet1.clear();
        mBackwardsTauClosureIterator1.resetState(root);
        while (mBackwardsTauClosureIterator1.advance()) {
          final int state1 =
            mBackwardsTauClosureIterator1.getCurrentSourceState();
          if (rel.isInitial(state1)) {
            final int code = numProperEvents << shift;
            if (mCurrentSet1.add(code)) {
              result += HashFunctions.hash(code);
            }
          }
          mBackwardsEventIterator.resetState(state1);
          while (mBackwardsEventIterator.advance()) {
            final int event = mBackwardsEventIterator.getCurrentEvent();
            if (event != EventEncoding.TAU) {
              final int eshift = event << shift;
              final int state2 =
                mBackwardsEventIterator.getCurrentSourceState();
              mBackwardsTauClosureIterator2.resetState(state2);
              while (mBackwardsTauClosureIterator2.advance()) {
                final int state =
                  mBackwardsTauClosureIterator2.getCurrentSourceState();
                final int code = state | eshift;
                if (mCurrentSet1.add(code)) {
                  result += HashFunctions.hash(code);
                }
              }
            }
          }
        }
        mCurrentSet1.clear();
        mPreviousRoot = root;
        mCachedResult = result;
        return result;
      }
    }

    public boolean equals(final int root1, final int root2) //checks if two states can be merged by rule
    {
      try {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int numProperEvents = rel.getNumberOfProperEvents();
        final int numStates = rel.getNumberOfStates();
        final int shift = AutomatonTools.log2(numStates);   //shifts number of states forward along
        mCurrentSet1.clear();
        mCurrentSet2.clear();
        mBackwardsTauClosureIterator1.resetState(root1);  //moves backward along tau events gives you all the states it finds
        while (mBackwardsTauClosureIterator1.advance()) {
          final int state1 =
            mBackwardsTauClosureIterator1.getCurrentSourceState();
          if (rel.isInitial(state1)) {
            final int code = numProperEvents << shift;
            mCurrentSet1.add(code);
          }
          mBackwardsEventIterator.resetState(state1);
          while (mBackwardsEventIterator.advance()) {
            final int event = mBackwardsEventIterator.getCurrentEvent();
            if (event != EventEncoding.TAU) {
              final int eshift = event << shift;
              final int state2 =
                mBackwardsEventIterator.getCurrentSourceState();
              mBackwardsTauClosureIterator2.resetState(state2);
              while (mBackwardsTauClosureIterator2.advance()) {
                final int state =
                  mBackwardsTauClosureIterator2.getCurrentSourceState();
                final int code = state | eshift;
                mCurrentSet1.add(code);
              }
            }
          }
        }
        mBackwardsTauClosureIterator1.resetState(root2);
        while (mBackwardsTauClosureIterator1.advance()) {
          final int state1 =
            mBackwardsTauClosureIterator1.getCurrentSourceState();
          if (rel.isInitial(state1)) {
            final int code = numProperEvents << shift;
            if (!mCurrentSet1.contains(code)) {
              return false;
            }
            mCurrentSet2.add(code);
          }
          mBackwardsEventIterator.resetState(state1);
          while (mBackwardsEventIterator.advance()) {
            final int event = mBackwardsEventIterator.getCurrentEvent();
            if (event != EventEncoding.TAU) {
              final int eshift = event << shift;
              final int state2 =
                mBackwardsEventIterator.getCurrentSourceState();
              mBackwardsTauClosureIterator2.resetState(state2);
              while (mBackwardsTauClosureIterator2.advance()) {
                final int state =
                  mBackwardsTauClosureIterator2.getCurrentSourceState();
                final int code = state | eshift;
                if (!mCurrentSet1.contains(code)) {
                  return false;
                }
                mCurrentSet2.add(code);
              }
            }
          }
        }
        return mCurrentSet1.size() == mCurrentSet2.size();
      } finally {
        mCurrentSet1.clear();
        mCurrentSet2.clear();
      }
    }



    //#######################################################################
    //# Data Members
    private final TransitionIterator mBackwardsTauClosureIterator1;
    private final TransitionIterator mBackwardsEventIterator;
    private final TransitionIterator mBackwardsTauClosureIterator2;
    private final TIntHashSet mCurrentSet1;
    private final TIntHashSet mCurrentSet2;

    private int mPreviousRoot;
    private int mCachedResult;


    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#######################################################################
  //# Data Members
  private int mTransitionLimit = Integer.MAX_VALUE;
  private int mNumberOfEnabledEvents;
}
