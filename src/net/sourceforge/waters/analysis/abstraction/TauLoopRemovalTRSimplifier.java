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

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A transition relation simplifier to remove tau loops.</P>
 *
 * <P>This simplifier find strongly connected components of states connected
 * by silent ({@link EventEncoding#TAU}) transitions and merges such
 * strongly connected components into a single state. Strongly
 * connected components are detected by an iterative implementation
 * of Tarjan's algorithm.</P>
 *
 * <P><I>Reference:</I>
 * R. Tarjan, Depth first search and linear graph algorithms. SIAM
 * Journal of Computing, <STRONG>1</STRONG>&nbsp;(2), 146-160, June 1972.</P>
 *
 * @author Robi Malik
 */

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
    mNextDFSIndex = 1;
    mDFSIndex = new int[numStates];
    mLowLink = new int[numStates];
    mOnComponentStack = new boolean[numStates];
    mControlStack = new TIntArrayList();
    mComponentStack = new TIntArrayStack();
    mMerging = false;
    mNextComponentNumber = 0;
    mComponentNumber = new int[numStates];
  }

  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    for (int s = 0; s < mDFSIndex.length; s++) {
      if (!rel.isReachable(s)) {
        mComponentNumber[s] = -1;
      } else if (mDFSIndex[s] == 0) {
        exploreDFS(s);
      }
    }
    if (mMerging) {
      final TRPartition partition =
        new TRPartition(mComponentNumber, mNextComponentNumber);
      setResultPartition(partition);
      applyResultPartitionAutomatically();
    }
    return mMerging;
  }

  @Override
  protected void tearDown()
  {
    mDFSIndex = null;
    mLowLink = null;
    mOnComponentStack = null;
    mComponentStack = null;
    mControlStack = null;
    mComponentNumber = null;
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
  private void exploreDFS(final int root)
    throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createAnyReadOnlyIterator();
    iter.resetEvent(EventEncoding.TAU);
    mControlStack.add(root);
    mControlStack.add(root);
    while (mControlStack.size() > 0) {
      checkAbort();
      int stackIndex = mControlStack.size() - 1;
      int state = mControlStack.get(stackIndex);
      if ((state & FOR_CLOSING) != 0) {
        // Close this state ...
        state &= ~FOR_CLOSING;
        final int parent = mControlStack.get(--stackIndex);
        mControlStack.remove(stackIndex, 2);
        if (mDFSIndex[state] == mLowLink[state]) {
          final int compNumber = mNextComponentNumber++;
          int count = 0;
          while (true) {
            final int popped = mComponentStack.pop();
            mComponentNumber[popped] = compNumber;
            mOnComponentStack[popped] = false;
            count++;
            if (popped == state) {
              break;
            }
          }
          mMerging |= count > 1;
        } else if (mLowLink[state] < mLowLink[parent]) {
          mLowLink[parent] = mLowLink[state];
        }
      } else if (mDFSIndex[state] == 0) {
        // Expand this state ...
        mControlStack.set(stackIndex, state | FOR_CLOSING);
        mDFSIndex[state] = mLowLink[state] = mNextDFSIndex++;
        mOnComponentStack[state] = true;
        mComponentStack.push(state);
        iter.resetState(state);
        while (iter.advance()) {
          final int succ = iter.getCurrentToState();
          if (mOnComponentStack[succ]) {
            if (mLowLink[succ] < mLowLink[state]) {
              mLowLink[state] = mLowLink[succ];
            }
          } else if (mDFSIndex[succ] == 0) {
            mControlStack.add(state);
            mControlStack.add(succ);
          }
        }
      } else {
        // This state has already been expanded and closed. Skip it ...
        mControlStack.remove(--stackIndex, 2);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mDumpStateAware = false;
  private int mDefaultMarkingID = -1;

  private int mNextDFSIndex;
  private int[] mDFSIndex;
  private int[] mLowLink;
  private boolean[] mOnComponentStack;
  private TIntArrayList mControlStack;
  private TIntStack mComponentStack;
  private boolean mMerging;
  private int mNextComponentNumber;
  private int[] mComponentNumber;


  //#########################################################################
  //# Class Constants
  private static final int FOR_CLOSING = 0x80000000;

}
