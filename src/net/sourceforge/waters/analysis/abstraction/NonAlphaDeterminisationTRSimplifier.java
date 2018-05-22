//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;

import gnu.trove.list.array.TIntArrayList;


/**
 * <P>A list buffer transition relation implementation of the
 * <I>Non-alpha Determinisation Rule</I>.</P>
 *
 * <P>The <I>Non-alpha Determinisation Rule</I> merges states that are
 * reverse weak bisimulation equivalent, provided they do not have the
 * precondition marking.</P>
 *
 * <P>This implementation supports both standard and generalised nonblocking
 * variants of the abstraction. If no precondition marking is configured,
 * all states without an outgoing silent ({@link EventEncoding#TAU})
 * transition are considered having the precondition marking.</P>
 *
 * <P><I>Reference:</I>
 * Robi Malik, Ryan Leduc. A Compositional Approach for Verifying
 * Generalised Nonblocking, Proc. 7th International Conference on Control and
 * Automation, ICCA'09, 448-453, Christchurch, New Zealand, 2009.</P>
 *
 * @author Rachel Francis, Robi Malik
 */

public class NonAlphaDeterminisationTRSimplifier
  extends ReverseObservationEquivalenceTRSimplifier
{

  //#########################################################################
  //# Constructors
  public NonAlphaDeterminisationTRSimplifier()
  {
    this(null);
  }

  public NonAlphaDeterminisationTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    if (getPreconditionMarkingID() < 0) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      mTauIterator = rel.createSuccessorsReadOnlyIterator();
      mTauIterator.resetEvent(EventEncoding.TAU);
    }
  }

  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    if (hasNonPreconditionMarkedStates()) {
      return super.runSimplifier();
    } else {
      return false;
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mTauIterator = null;
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction
  //# ReverseObservationEquivalenceTRSimplifier
  /**
   * Creates an initial partition. This includes a separate equivalence class
   * for every state marked alpha, and an equivalence class which contains all
   * the remaining states.
   */
  @Override
  protected TRPartition createInitialPartition()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final List<int[]> initialPartition = new ArrayList<int[]>();
    final TIntArrayList remainingStates = new TIntArrayList();
    for (int state = 0; state < numStates; state++) {
      if (!rel.isReachable(state)) {
        continue;
      } else if (isAlphaMarked(state)) {
        // create a separate equivalence class for every state marked alpha
        final int[] alphaClass = new int[1];
        alphaClass[0] = state;
        initialPartition.add(alphaClass);
      } else {
        // create an equivalence class for all other states
        remainingStates.add(state);
      }
    }
    final int[] remainingStatesArray = remainingStates.toArray();
    initialPartition.add(remainingStatesArray);
    return new TRPartition(initialPartition, numStates);
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Checks whether the transition relation has at least two states <I>not</I>
   * marked with the precondition marking. If all but one states are marked,
   * there is no need to try and simplify.
   */
  private boolean hasNonPreconditionMarkedStates()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (getPreconditionMarkingID() < 0 &&
        (rel.getProperEventStatus(EventEncoding.TAU) &
         EventStatus.STATUS_UNUSED) != 0) {
      return false;
    }
    final int numStates = rel.getNumberOfStates();
    int numNonAlphaStates = 0;
    for (int state = 0; state < numStates; state++) {
      if (!isAlphaMarked(state)) {
        if (++numNonAlphaStates >= 2) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isAlphaMarked(final int state)
  {
    if (mTauIterator == null) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int alphaID = getPreconditionMarkingID();
      return rel.isMarked(state, alphaID);
    } else {
      mTauIterator.resetState(state);
      return !mTauIterator.advance();
    }
  }


  //#########################################################################
  //# Data Members
  private TransitionIterator mTauIterator;

}
