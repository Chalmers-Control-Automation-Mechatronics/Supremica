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

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;

/**
 * @author Jordan Schroder
 */
public class LocalizedSupervisorReductionTRSimplifier
  extends AbstractSupervisorReductionTRSimplifier
{
  public LocalizedSupervisorReductionTRSimplifier()
  {
  }

  public LocalizedSupervisorReductionTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }

  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mNumProperEvents = rel.getNumberOfProperEvents();

    final int dumpState = rel.getDumpStateIndex();
    final int supervisedEvent = getSupervisedEvent();

    final int numStates = rel.getNumberOfStates();
    mStateOutputs = new StateOutput[numStates];

    for (int s = 0; s < numStates; s++) {
      final int successorState = getSuccessorState(s, supervisedEvent);
      if (successorState == -1) {
        mStateOutputs[s] = StateOutput.IGNORE;
      } else if (successorState == dumpState) {
        mStateOutputs[s] = StateOutput.DISABLE;
      } else {
        mStateOutputs[s] = StateOutput.ENABLE;
      }
    }
  }

  @Override
  protected boolean runSimplifier() throws AnalysisException
  {
    // TODO Auto-generated method stub
    return false;
  }

  private enum StateOutput {
    ENABLE,
    DISABLE,
    IGNORE
  }

  private int getSuccessorState(final int source, final int event)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    iter.reset(source, event);
    if (iter.advance()) {
      return iter.getCurrentTargetState();
    } else {
      return -1;
    }
  }

  private TIntSet getPredecessorStates(final int source, final int event) {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    iter.reset(source, event);

    final TIntHashSet predecessors = new TIntHashSet();
    while (iter.advance()) {
      predecessors.add(iter.getCurrentTargetState());
    }
    return predecessors;
  }

  private TIntSet getPredecessorEvents(final int source) {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    iter.resetState(source);

    final TIntHashSet predecessors = new TIntHashSet();
    while (iter.advance()) {
      predecessors.add(iter.getCurrentEvent());
    }
    return predecessors;
  }

  //#########################################################################
  //# Data Members
  private int mNumProperEvents;
  private StateOutput[] mStateOutputs;
}
