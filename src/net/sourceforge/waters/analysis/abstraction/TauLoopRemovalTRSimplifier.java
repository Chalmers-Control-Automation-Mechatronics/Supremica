//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.Arrays;

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
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    mControlStack = new TIntArrayList();
    mComponentStack = new TIntArrayStack();
    mOnComponentStack = new boolean[numStates];
    mDFSIndex = new int[numStates];
    mLowLink = new int[numStates];
    mNextDFSIndex = 1;
    mMerging = false;
  }

  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    // 1. Tarjan recursion starting from all states ...
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    for (int s = 0; s < numStates; s++) {
      if (rel.isReachable(s) && mDFSIndex[s] == 0) {
        exploreDFS(s);
      }
    }

    // 2. Merge strongly connected components ...
    if (mMerging) {
      // Component codes are assigned such that components are ordered
      // by their smallest state code in the original encoding.
      final int[] recoding = mDFSIndex;
      Arrays.fill(recoding, -1);
      int nextComponent = 0;
      for (int s = 0; s < numStates; s++) {
        final int lowLink = mLowLink[s];
        if (lowLink == 0) {
          mLowLink[s] = -1;
        } else if (recoding[lowLink - 1] < 0) {
          mLowLink[s] = recoding[lowLink - 1] = nextComponent++;
        } else {
          mLowLink[s] = recoding[lowLink - 1];
        }
      }
      final int dumpIndex = rel.getDumpStateIndex();
      if (mLowLink[dumpIndex] < 0) {
        nextComponent++;
      }
      final TRPartition partition = new TRPartition(mLowLink, nextComponent);
      setResultPartition(partition);
      applyResultPartitionAutomatically();
    }
    return mMerging;
  }

  @Override
  protected void tearDown()
  {
    mControlStack = null;
    mComponentStack = null;
    mOnComponentStack = null;
    mDFSIndex = null;
    mLowLink = null;
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
    rel.removeRedundantPropositions();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void exploreDFS(final int start)
    throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createAnyReadOnlyIterator();
    iter.resetEvent(EventEncoding.TAU);
    mControlStack.add(start);
    mControlStack.add(start);
    do {
      checkAbort();
      int stackIndex = mControlStack.size() - 1;
      int state = mControlStack.get(stackIndex);
      if ((state & FOR_CLOSING) != 0) {
        // Close this state ...
        state &= ~FOR_CLOSING;
        final int parent = mControlStack.get(--stackIndex);
        mControlStack.remove(stackIndex, 2);
        final int lowLink = mLowLink[state];
        if (mDFSIndex[state] == lowLink) {
          while (true) {
            final int popped = mComponentStack.pop();
            mLowLink[popped] = lowLink;
            mOnComponentStack[popped] = false;
            if (popped == state) {
              break;
            }
            mMerging = true;
          }
        } else if (lowLink < mLowLink[parent]) {
          mLowLink[parent] = lowLink;
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
    } while (mControlStack.size() > 0);
  }


  //#########################################################################
  //# Data Members
  private boolean mDumpStateAware = false;
  private int mDefaultMarkingID = -1;

  /**
   * Depth-first search indexes of visited states. Unvisited states have
   * a zero entry. Visited states receive numbers in the order in which they
   * are encountered in depth-search order, starting with&nbsp;1.
   * Unreachable states retain the value&nbsp;0.
   */
  private int[] mDFSIndex;
  /**
   * Depth-first search indexes of the smallest state found in the component
   * of states. Unreachable states retain the value&nbsp;0.
   */
  private int[] mLowLink;
  /**
   * Stack of states currently being processed, which have not yet been
   * assigned to any component.
   */
  private TIntStack mComponentStack;
  /**
   * Booleans set to <CODE>true</CODE> for states currently on the
   * component stack {@link #mComponentStack}.
   */
  private boolean[] mOnComponentStack;
  /**
   * Control stack for iterative Tarjan. Each time a recursive call is
   * initiated, two numbers are put on the stack: first the state number of
   * the <I>parent</I> from which a state is visited, and second the state
   * number of the <I>state</I> to be visited. When a <I>state</I> is first
   * visited, it is expanded, but the entry is not removed from the stack.
   * Instead, the state is flagged with {@link #FOR_CLOSING}. When a
   * <I>state</I> with this flag is encountered, it is checked whether a
   * strongly connected component is completed, and subsequently both the
   * <I>state</I> and <I>parent</I> entries are removed from the control
   * stack.
   */
  private TIntArrayList mControlStack;
  /**
   * Next index to be put into {@link #mDFSIndex} array.
   */
  private int mNextDFSIndex;
  /**
   * Flag set to <CODE>true</CODE> when a strongly connected component
   * consisting of more than one state is found.
   */
  private boolean mMerging;


  //#########################################################################
  //# Class Constants
  private static final int FOR_CLOSING = 0x80000000;

}
