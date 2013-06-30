//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   TauLoopRemovalTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;


public class TauLoopRemovalTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructor
  public TauLoopRemovalTRSimplifier()
  {
  }

  public TauLoopRemovalTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether this simplifier should consider deadlock states when
   * removing selfloops.
   * @see #isDumpStateAware()
   */
  public void setDumpStateAware(final boolean aware)
  {
    mDumpStateAware = aware;
  }

  /**
   * Gets whether this simplifier considers deadlock states when
   * removing selfloops. This setting affects how the simplifier checks for
   * pure selfloop events in the end. If the simplifier is deadlock aware,
   * then events not enabled in deadlock states can be considered as
   * selfloop events and removed from the automaton if selflooped in all
   * other states.
   */
  public boolean isDumpStateAware()
  {
    return mDumpStateAware;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
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
  public void setPropositions(final int preconditionID, final int defaultID)
  {
    mDefaultMarkingID = defaultID;
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, false);
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
    final int numStates = rel.getNumberOfStates();
    mIndex = 1;
    mTarjan = new int[numStates];
    mLowLink = new int[numStates];
    mOnstack = new boolean[numStates];
    mStack = new TIntArrayStack();
    mToBeMerged = new ArrayList<TIntArrayList>();
  }

  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    boolean modified = false;
    for (int s = 0; s < mTarjan.length; s++) {
      if (mTarjan[s] == 0) {
        tarjan(s);
      }
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (modified || !mToBeMerged.isEmpty()) {
      final int numStates = rel.getNumberOfStates();
      List<int[]> partition = new ArrayList<int[]>(numStates);
      final BitSet merged = new BitSet(numStates);
      for (final TIntArrayList merge : mToBeMerged) {
        checkAbort();
        final int[] array = merge.toArray();
        partition.add(array);
        modified |= array.length > 1;
        for (final int s : array) {
          merged.set(s);
        }
      }
      if (modified) {
        for (int s = 0; s < numStates; s++) {
          if (rel.isReachable(s) && !merged.get(s)) {
            checkAbort();
            final int[] array = new int[1];
            array[0] = s;
            partition.add(array);
          }
        }
        setResultPartitionList(partition);
        applyResultPartitionAutomatically();
      } else {
        partition = null;
      }
    }
    return modified;
  }

  @Override
  protected void tearDown()
  {
    mTarjan = null;
    mLowLink = null;
    mOnstack = null;
    mStack = null;
    mToBeMerged = null;
    super.tearDown();
  }

  @Override
  protected void applyResultPartition()
    throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.removeTauSelfLoops();
    if (mDumpStateAware && mDefaultMarkingID >= 0) {
      rel.removeProperSelfLoopEvents(mDefaultMarkingID);
    } else {
      rel.removeProperSelfLoopEvents();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void tarjan(final int state)
  throws AnalysisAbortException
  {
    checkAbort();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mTarjan[state] = mIndex;
    mLowLink[state] = mIndex;
    mIndex++;
    mOnstack[state] = true;
    mStack.push(state);
    final TransitionIterator iter =
      rel.createAnyReadOnlyIterator(state, EventEncoding.TAU);
    while (iter.advance()) {
      final int suc = iter.getCurrentToState();
      if(mOnstack[suc]) {
        mLowLink[state] = mTarjan[suc] < mLowLink[state] ? mTarjan[suc]
                                                         : mLowLink[state];
      } else if (mTarjan[suc] == 0) {
        tarjan(suc);
        mLowLink[state] = mLowLink[suc] < mLowLink[state] ? mLowLink[suc]
                                                          : mLowLink[state];
      }
    }
    if (mTarjan[state] == mLowLink[state]) {
      final TIntArrayList merge = new TIntArrayList();
      while (true) {
        final int pop = mStack.pop();
        merge.add(pop);
        mOnstack[pop] = false;
        if (pop == state) {
          break;
        }
      }
      if (merge.size() > 1) {
        mToBeMerged.add(merge);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mDumpStateAware = false;
  private int mDefaultMarkingID = -1;

  private int mIndex;
  private int[] mTarjan;
  private int[] mLowLink;
  private boolean[] mOnstack;
  private TIntStack mStack;
  private Collection<TIntArrayList> mToBeMerged;

}

