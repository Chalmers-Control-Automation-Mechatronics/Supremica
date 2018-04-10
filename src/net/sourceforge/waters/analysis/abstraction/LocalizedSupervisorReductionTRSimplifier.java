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
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.array.TLongArrayStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

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
    mPredecessorIterator = rel.createPredecessorsReadOnlyIterator();
    mSuccessorIterator = rel.createSuccessorsReadOnlyIterator();

    //mNumProperEvents = rel.getNumberOfProperEvents();

    final int dumpState = rel.getDumpStateIndex();
    final int supervisedEvent = getSupervisedEvent();

    mNumStates = rel.getNumberOfStates();
    mStateOutputs = new StateOutput[mNumStates];
    mInitialStates = new TIntArrayList();
    mReducedSupervisor = new ArrayList<>();

    for (int s = 0; s < mNumStates; s++) {
      //set the reduced supervisor to the current set of states
      mReducedSupervisor.add(new TIntArrayList(new int[] {s}));

      if (rel.isInitial(s)) {
        mInitialStates.add(s);
      }

      final int successorState = getSuccessorState(s, supervisedEvent);
      if (successorState == -1) {
        mStateOutputs[s] = StateOutput.IGNORE;
      } else if (successorState == dumpState) {
        mStateOutputs[s] = StateOutput.DISABLE;
      } else {
        mStateOutputs[s] = StateOutput.ENABLE;
      }
    }

    mIncompatibilityRelation = getIncompatibilityRelation();
  }


  @Override
  protected boolean runSimplifier() throws AnalysisException
  {
    if (mInitialStates.size() == 0) {
      return false;
    }

    //get the set of compatibles that cover the initial state, and we will try to reduce the supervisor using each
    final List<TIntList> initialCompatibles = getCoversOf(mInitialStates);
    for (final TIntList initialCompatible : initialCompatibles) {
      reduce(initialCompatible);
    }
    return false;
  }

  private void reduce(final TIntList initialCompatible) {
    final List<TIntList> compatibles = new ArrayList<TIntList>();
    compatibles.add(initialCompatible);
    reduce(compatibles, getNeighboursOf(initialCompatible));
  }

  private void reduce(final List<TIntList> compatibles, final Deque<TIntList> compatibleDependencies) {
    if (compatibles.size() >= mReducedSupervisor.size()) {
      return;
    }

    if (compatibleDependencies.isEmpty()) {
      //if we got here we know our solution is better
      mReducedSupervisor = compatibles;
      return;
    }

    final TIntList nextCompatible = compatibleDependencies.pop();
    final List<TIntList> covers = getCoversOf(nextCompatible);
    for (final Iterator<TIntList> coversIterator = covers.iterator(); coversIterator.hasNext();) {
      final TIntList cover = coversIterator.next();
      @SuppressWarnings("unused")
      final Deque<TIntList> dependents = getNeighboursOf(cover);
      //TODO: decide how to some-what efficient determine which dependents are not covered by anything in compatibleDependencies already
      //reduce(compatibles + cover, compatibleDependencies + that)
    }
  }

  private enum StateOutput {
    ENABLE,
    DISABLE,
    IGNORE
  }

  private List<TIntList> BronKerbosch(final TIntList clique, final TIntList possibleInclusions, final TIntList alreadyChecked) {
    final List<TIntList> cliques = new ArrayList<>();

    //if we have exhausted all possibilities, we have no more work to do
    if (possibleInclusions.isEmpty() && alreadyChecked.isEmpty()) {
      cliques.add(clique);
      return cliques;
    }

    //create a copy in case removing items from possibleInclusions affects the iterator mid-way through
    final TIntList originalPossibleInclusions = new TIntArrayList(possibleInclusions);

    for (final TIntIterator inclusionIterator = originalPossibleInclusions.iterator(); inclusionIterator.hasNext();) {
      final int vertex = inclusionIterator.next();
      final TIntList neighbours = getNeighboursOf(vertex);

      //create a copy
      final TIntList newClique = new TIntArrayList(clique);
      newClique.add(vertex);

      //create a copy
      final TIntList newPossibleInclusions = new TIntArrayList(possibleInclusions);
      newPossibleInclusions.retainAll(neighbours);

      final TIntList newAlreadyChecked = new TIntArrayList(alreadyChecked);

      cliques.addAll(BronKerbosch(newClique, newPossibleInclusions, newAlreadyChecked));

      possibleInclusions.remove(vertex);
      alreadyChecked.add(vertex);
    }

    return cliques;
  }

  private List<TIntList> getCoversOf(final TIntList compatible) {
    final TIntList possibleInclusions = new TIntArrayList();
    //start with all the states as possible inclusions to BronKerbosch
    for (int s = 0; s < mNumStates; s++) {
      possibleInclusions.add(s);
    }

    //keep restricting the possible inclusions to just include neighbours of states in the compatible
    for (final TIntIterator compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
      possibleInclusions.retainAll(getNeighboursOf(compatibleIterator.next()));
    }
    return BronKerbosch(compatible, possibleInclusions, new TIntArrayList());
  }

  private TIntList getNeighboursOf(final int vertex) {
    final TIntList neighbours = new TIntArrayList();
    for (int s = 0; s < mNumStates; s++) {
      //if the two states are compatible and not the same state, add to neighbours
      if (mIncompatibilityRelation[vertex][s] && s != vertex) {
        neighbours.add(s);
      }
    }
    return neighbours;
  }

  private Deque<TIntList> getNeighboursOf(final TIntList compatible) {
    final Deque<TIntList> neighbours = new ArrayDeque<>();
    final TIntSet enabledEvents = new TIntHashSet();

    //get the union of enabled events across states in the compatible
    for (final TIntIterator compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
      enabledEvents.addAll(getSuccessorEvents(compatibleIterator.next()));
    }

    //for each event, we see what compatible is generated by taking that event from each state in the original compatible
    for (final TIntIterator eventIterator = enabledEvents.iterator(); eventIterator.hasNext();) {
      final int event = eventIterator.next();
      final TIntList reachableCompatible = new TIntArrayList();

      //for each state in original compatible, retrieve the target state which forms part of a compatible
      for (final TIntIterator compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
        final int targetState = getSuccessorState(compatibleIterator.next(), event);
        //if the target exists
        if (targetState != -1) {
          reachableCompatible.add(targetState);
        }
      }
      neighbours.add(reachableCompatible);
    }
    return neighbours;
  }

  private boolean[][] getIncompatibilityRelation() {
    //to avoid having to loop through the entire matrix to assume each state pair is compatible,
    //we will just reverse interpretation of the entire matrix

    final boolean[][] incompatibilityRelation = new boolean[mNumStates][];
    for (int x = 0; x < mNumStates; x++) {
      if (incompatibilityRelation[x] == null) {
        incompatibilityRelation[x]= new boolean[mNumStates];
      }

      final StateOutput outputX = mStateOutputs[x];

      //if this state doesn't care about supervisor event, skip all its pairs
      if (outputX.equals(StateOutput.IGNORE)) { continue; }

      for (int y = 0; y < x; y++) {

        //if we have already established this pair is incompatible, skip
        if (incompatibilityRelation[x][y]) { continue; }

        final StateOutput outputY = mStateOutputs[y];
        if (outputY.equals(StateOutput.IGNORE)) { continue; }

        if (!outputX.equals(outputY)) {
          //not compatible
          final TLongArrayStack incompatiblesToMark = new TLongArrayStack();
          //top 32 bits is x, bottom 32 is y
          incompatiblesToMark.push(createStatePair(x, y));

          while (incompatiblesToMark.size() > 0) {

            //get the next state pair from the set
            final long pairToMark = incompatiblesToMark.pop();
            final int markX = getXFromStatePair(pairToMark);
            final int markY = getYFromStatePair(pairToMark);

            //make sure these rows exist in the matrix exists
            if (incompatibilityRelation[markX] == null) {
              incompatibilityRelation[markX] = new boolean[mNumStates];
            }
            if (incompatibilityRelation[markY] == null) {
              incompatibilityRelation[markY] = new boolean[mNumStates];
            }

            //mark the pair and its reverse as incompatible
            incompatibilityRelation[markX][markY] = true;
            incompatibilityRelation[markY][markX] = true;

            final TIntList sharedEvents = getPredecessorEvents(markY);

            //take the intersection of events from x and y
            sharedEvents.retainAll(getPredecessorEvents(markX));

            for (final TIntIterator eventIter = sharedEvents.iterator(); eventIter.hasNext();) {
              final int event = eventIter.next();

              for (final TIntIterator xIter = getPredecessorStates(x, event).iterator(); xIter.hasNext();) {
                final int xPred = xIter.next();
                for (final TIntIterator yIter = getPredecessorStates(y, event).iterator(); yIter.hasNext();) {
                  final int yPred = yIter.next();
                  if (!incompatibilityRelation[xPred][yPred]) {
                    incompatiblesToMark.push(createStatePair(xPred, yPred));
                  }
                }
              }
            }
          }
        }
      }
    }
    return incompatibilityRelation;
  }

  private long createStatePair(final int x, final int y) {
    final long val = x << 32;
    return val + y;
  }

  private int getXFromStatePair(final long statePair) {
    return (int)(statePair >> 32);
  }

  private int getYFromStatePair(final long statePair) {
    return (int)statePair;
  }

  private int getSuccessorState(final int source, final int event)
  {
    mSuccessorIterator.reset(source, event);
    if (mSuccessorIterator.advance()) {
      return mSuccessorIterator.getCurrentTargetState();
    } else {
      return -1;
    }
  }

  private TIntList getPredecessorStates(final int source, final int event) {
    mPredecessorIterator.reset(source, event);

    final TIntList predecessors = new TIntArrayList();
    while (mPredecessorIterator.advance()) {
      predecessors.add(mPredecessorIterator.getCurrentTargetState());
    }
    return predecessors;
  }

  private TIntList getPredecessorEvents(final int source) {
    mPredecessorIterator.resetState(source);

    final TIntList predecessorEvents = new TIntArrayList();
    while (mPredecessorIterator.advance()) {
      predecessorEvents.add(mPredecessorIterator.getCurrentEvent());
    }
    return predecessorEvents;
  }

  private TIntList getSuccessorEvents(final int source) {
    mSuccessorIterator.resetState(source);

    final TIntList successorEvents = new TIntArrayList();
    while (mSuccessorIterator.advance()) {
      successorEvents.add(mSuccessorIterator.getCurrentEvent());
    }

    return successorEvents;
  }

  //#########################################################################
  //# Data Members
  //private int mNumProperEvents;
  private boolean[][] mIncompatibilityRelation;
  private TransitionIterator mPredecessorIterator;
  private TransitionIterator mSuccessorIterator;
  private TIntList mInitialStates;
  private int mNumStates;
  private StateOutput[] mStateOutputs;
  private List<TIntList> mReducedSupervisor;
}
