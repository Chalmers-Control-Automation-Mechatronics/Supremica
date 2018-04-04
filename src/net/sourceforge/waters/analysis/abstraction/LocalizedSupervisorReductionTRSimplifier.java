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

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;
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

    //to avoid having to loop through the entire matrix to assume each state pair is compatible,
    //we will just reverse interpretation of the entire matrix
    final boolean[][] incompatibilityMatrix = new boolean[numStates][];
    for (int x = 0; x < numStates; x++) {
      incompatibilityMatrix[x]= new boolean[numStates];
      final StateOutput outputX = mStateOutputs[x];

      //if this state doesn't care about supervisor event, skip all its pairs
      if (outputX.equals(StateOutput.IGNORE)) { continue; }

      for (int y = 0; y < x; y++) {

        //if we have already established this pair is incompatible, skip
        if (incompatibilityMatrix[x][y]) { continue; }

        final StateOutput outputY = mStateOutputs[y];
        if (outputY.equals(StateOutput.IGNORE)) { continue; }

        if (!outputX.equals(outputY)) {
          //not compatible
          final THashSet<StatePair> incompatiblesToMark = new THashSet<StatePair>();
          incompatiblesToMark.add(new StatePair(x, y));

          while (!incompatiblesToMark.isEmpty()) {
            //get an arbitrary state pair from the set
            final StatePair pairToMark = incompatiblesToMark.iterator().next();

            //mark the pair and its reverse as incompatible
            incompatibilityMatrix[pairToMark.x][pairToMark.y] = true;
            incompatibilityMatrix[pairToMark.y][pairToMark.x] = true;

            final TIntSet sharedEvents = getPredecessorEvents(pairToMark.y);

            //take the intersection of events from x and y
            sharedEvents.retainAll(getPredecessorEvents(pairToMark.x));

            final TIntIterator eventIter = sharedEvents.iterator();
            while (eventIter.hasNext()) {
              final int event = eventIter.next();
              final TIntSet xPredecessors = getPredecessorStates(x, event);
              final TIntSet yPredecessors = getPredecessorStates(y, event);

              final TIntIterator xIter = xPredecessors.iterator();
              while (xIter.hasNext()) {
                final int xPred = xIter.next();
                final TIntIterator yIter = yPredecessors.iterator();
                while (yIter.hasNext()) {
                  final int yPred = yIter.next();
                  if (!incompatibilityMatrix[xPred][yPred]) {
                    incompatiblesToMark.add(new StatePair(xPred, yPred));
                  }
                }
              }
            }
          }
        }
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

  private class StatePair {
    private final int x, y;

    public StatePair(final int x, final int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public int hashCode()
    {
      //using http://szudzik.com/ElegantPairing.pdf
      //we can't map 2 x 32 bit ints to an int without collisions (need a long for perfect)
      return x >= y ? x * x + x + y : x + y * y;
    }

    @Override
    public boolean equals(final Object obj)
    {
      if (obj == this) { return true; }
      if (!(obj instanceof StatePair)) { return false; }

      final StatePair pair = (StatePair)obj;

      return pair.x == x && pair.y == y;
    }
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
  @SuppressWarnings("unused")
  private int mNumProperEvents;
  private StateOutput[] mStateOutputs;
}
