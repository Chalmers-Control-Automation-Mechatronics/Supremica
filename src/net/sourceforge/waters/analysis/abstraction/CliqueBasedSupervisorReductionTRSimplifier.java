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
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.analysis.tr.AbstractStateBuffer;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;

/**
 * @author Jordan Schroder
 */
public class CliqueBasedSupervisorReductionTRSimplifier
  extends AbstractSupervisorReductionTRSimplifier
{
  public CliqueBasedSupervisorReductionTRSimplifier()
  {
  }

  public CliqueBasedSupervisorReductionTRSimplifier(final ListBufferTransitionRelation rel)
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

    //mNumProperEvents = rel.getNumberOfProperEvents();

    mDumpStateIndex = rel.getDumpStateIndex();
    final int supervisedEvent = getSupervisedEvent();

    mNumStates = rel.getNumberOfStates();
    mNumProperEvents = rel.getNumberOfProperEvents();
    final StateOutput[] stateOutputs = new StateOutput[mNumStates];
    mInitialCompatible = new Compatible();
    mReducedSupervisor = new ArrayList<>();

    for (int s = 0; s < mNumStates; s++) {
      //set the reduced supervisor to the current set of states
      final Compatible oneStateCompatible = new Compatible();
      oneStateCompatible.add(s);
      mReducedSupervisor.add(oneStateCompatible);

      if (rel.isInitial(s)) {
        mInitialCompatible.add(s);
      }

      final int successorState = getSuccessorState(s, supervisedEvent);
      if (successorState == -1) {
        stateOutputs[s] = StateOutput.IGNORE;
      } else if (successorState == mDumpStateIndex) {
        stateOutputs[s] = StateOutput.DISABLE;
      } else {
        stateOutputs[s] = StateOutput.ENABLE;
      }
    }

    mIncompatibilityRelation = getIncompatibilityRelation(stateOutputs);
  }


  @Override
  protected boolean runSimplifier() throws AnalysisException
  {
    if (mInitialCompatible.size() == 0) {
      return false;
    }

    //get the set of compatibles that cover the initial state, and we will try to reduce the supervisor using each
    final List<Compatible> initialCompatibles = getCoversOf(mInitialCompatible);
    for (final Compatible initialCompatible : initialCompatibles) {
      reduce(initialCompatible);
    }

    //account for the dump state which is included in the initial automaton but not in our compatibles
    if (mReducedSupervisor.size() + 1 >= mNumStates) {
      return false;
    }

    else {
      //we managed to reduce the supervisor
      final int supervisorSize = mReducedSupervisor.size();
      //we have the set of compatibles, now we need to construct a new automaton based off them
      //find a compatible containing the initial set of states (it may could be a cover if a larger clique was found)
      int startStateIndexOffset = 0;
      for (; startStateIndexOffset < supervisorSize; startStateIndexOffset++) {
        final Compatible currentCompatible = mReducedSupervisor.get(startStateIndexOffset);
        if (currentCompatible.containsAll(mInitialCompatible)) { break; }
      }

      //we must now go through each compatible (which is to become a state in the reduced automaton) and determine its successors
      //in doing so, we build up a transition relation. We will use the index of the compatible in mReducedSupervisor to denote its index in the transition buffer
      //however we want the initial state to have index 0 so we will offset all values by that amount
      final PreTransitionBuffer transitionBuffer = new PreTransitionBuffer(mNumProperEvents);
      boolean hasExplicitDumpState = false;
      final TIntSet enabledEvents = new TIntHashSet();

      for (int state = 0; state < supervisorSize; state++) {
        final int compatibleIndex = (state + startStateIndexOffset) % supervisorSize;
        final Compatible compatible = mReducedSupervisor.get(compatibleIndex);

        //get the union of all events among the compatible's states (from the original automaton definition)
        enabledEvents.clear();
        getEnabledEventsOf(compatible, enabledEvents);
        for (final TIntIterator eventIterator = enabledEvents.iterator(); eventIterator.hasNext();) {
          final int event = eventIterator.next();
          final Compatible successorCompatible = new Compatible();

          //for each state in original compatible, retrieve the successor state which forms part of a successor compatible
          for (final TIntIterator stateIterator = compatible.iterator(); stateIterator.hasNext();) {
            final int targetState = getSuccessorState(stateIterator.next(), event);
            //if the target exists
            if (targetState != -1) {
              successorCompatible.add(targetState);
            }
          }

          //map this successorCompatible to its index in our list of compatibles
          int successorState = 0;

          //map the dump state to a special state index
          if (successorCompatible.size() == 1 && successorCompatible.contains(mDumpStateIndex)) {
            successorState = supervisorSize;
            hasExplicitDumpState = true;
          }
          else {
            for (; successorState < supervisorSize; successorState++) {
              final int successorCompatibleIndex = (successorState + startStateIndexOffset) % supervisorSize;
              if (mReducedSupervisor.get(successorCompatibleIndex).containsAll(successorCompatible)) {
                break;
              }
            }
          }

          transitionBuffer.addTransition(state, event, successorState);
        }
      }
      final ListBufferTransitionRelation relation = getTransitionRelation();
      final AbstractStateBuffer oldStateBuffer = relation.getStateBuffer();

      //explicitly identify the dump state
      relation.reset(supervisorSize + 1, supervisorSize, transitionBuffer.size(), getPreferredInputConfiguration());
      //set the dump state as reachable only if there is actually a transition to it from our cliques
      relation.setReachable(supervisorSize, hasExplicitDumpState);

      relation.setInitial(0, true);

      relation.removeRedundantPropositions();
      relation.removeEvent(EventEncoding.TAU);
      transitionBuffer.addOutgoingTransitions(relation);

      for (int state = 0; state < supervisorSize; state++) {
        final int compatibleIndex = (state + startStateIndexOffset) % supervisorSize;
        final Compatible compatible = mReducedSupervisor.get(compatibleIndex);
        long newStateMarkings = relation.createMarkings();

        for (final TIntIterator stateIterator = compatible.iterator(); stateIterator.hasNext();) {
          final int compatibleState = stateIterator.next();
          final long oldStateMarkings = oldStateBuffer.getAllMarkings(compatibleState);
          newStateMarkings = relation.mergeMarkings(newStateMarkings, oldStateMarkings);
        }
        relation.setAllMarkings(state, newStateMarkings);
      }

      relation.removeProperSelfLoopEvents();

      return true;
    }
  }

  private void reduce(final Compatible initialCompatible) {
    final List<Compatible> currentSolution = new ArrayList<Compatible>();
    currentSolution.add(initialCompatible);

    final Deque<Compatible> dependents = new ArrayDeque<>();
    getSuccessorCompatiblesOf(initialCompatible, dependents);

    final Deque<Compatible> uncoveredDependents = new ArrayDeque<>();
    fillUncoveredCompatibles(dependents, currentSolution, uncoveredDependents);

    if (currentSolution.size() + uncoveredDependents.size() < mReducedSupervisor.size()) {
      reduce(currentSolution, uncoveredDependents);
    }
  }

  private void reduce(final List<Compatible> currentSolution, final Deque<Compatible> compatibleDependencies) {
    //we can assume our solution is not worst than the current best if we get here

    if (compatibleDependencies.isEmpty()) {
    //if we got here we know our solution is better than the current best
      mReducedSupervisor = currentSolution;
      return;
    }

    //get the next compatible we need to add
    final Compatible nextCompatible = compatibleDependencies.pop();
    //get all (maximal) cliques covering this compatible
    final List<Compatible> covers = getCoversOf(nextCompatible);

    final Collection<Compatible> dependents = new ArrayDeque<>();
    final Collection<Compatible> coverSet = new ArrayList<>();

    for (final Iterator<Compatible> coversIterator = covers.iterator(); coversIterator.hasNext();) {
      //try to add this cover of the the compatible we want to add
      final Compatible cover = coversIterator.next();

      //adding this means our supervisor has to cover its successor compatibles
      dependents.clear();
      getSuccessorCompatiblesOf(cover, dependents);

      //copy the current solution and add the candidate
      final List<Compatible> newSolution = new ArrayList<Compatible>(currentSolution);
      newSolution.add(cover);

      coverSet.clear();
      coverSet.addAll(compatibleDependencies);
      coverSet.addAll(newSolution);

      final Deque<Compatible> newCompatibleDependencies = new ArrayDeque<>(compatibleDependencies);
      fillUncoveredCompatibles(dependents, coverSet, newCompatibleDependencies);

      if (newSolution.size() + newCompatibleDependencies.size() < mReducedSupervisor.size()) {
        reduce(newSolution, newCompatibleDependencies);
      }
    }
  }

  private List<Compatible> BronKerbosch(final Compatible clique, final TIntList possibleInclusions, final TIntList alreadyChecked) {
    return BronKerbosch(clique, possibleInclusions, alreadyChecked, new ArrayList<Compatible>());
  }

  private List<Compatible> BronKerbosch(final Compatible clique, final TIntList possibleInclusions, final TIntList alreadyChecked, final List<Compatible> cliques) {
    //if we have exhausted all possibilities, this must be the largest clique we have seen
    if (possibleInclusions.isEmpty() && alreadyChecked.isEmpty()) {
      //add this maximal clique
      cliques.add(clique);
      return cliques;
    }

    //create a copy in case removing items from possibleInclusions affects the iterator mid-way through
    final TIntList originalPossibleInclusions = new TIntArrayList(possibleInclusions);

    for (final TIntIterator inclusionIterator = originalPossibleInclusions.iterator(); inclusionIterator.hasNext();) {
      final int vertex = inclusionIterator.next();

      //create a copy with the new vertex
      final Compatible newClique = new Compatible(clique);
      newClique.add(vertex);

      //create a copy with a restricted set of neighbours
      final TIntList newPossibleInclusions = new TIntArrayList();
      for (final TIntIterator stateIterator = possibleInclusions.iterator(); stateIterator.hasNext();) {
        final int state = stateIterator.next();
        if (isNeighbour(vertex, state)) {
          newPossibleInclusions.add(state);
        }
      }

      final TIntList newAlreadyChecked = new TIntArrayList(alreadyChecked);

      //find any maximal cliques based on newCliques and add them to the object referenced by cliques
      BronKerbosch(newClique, newPossibleInclusions, newAlreadyChecked, cliques);

      possibleInclusions.remove(vertex);
      alreadyChecked.add(vertex);
    }

    return cliques;
  }

  private List<Compatible> getCoversOf(final Compatible compatible) {
    final TIntList possibleInclusions = new TIntArrayList();
    if (compatible.size() > 0) {
      final TIntList neighbours = getNeighboursOf(compatible.iterator().next());
      for (final TIntIterator neighbourIterator = neighbours.iterator(); neighbourIterator.hasNext();) {
        final int neighbour = neighbourIterator.next();
        final TIntIterator stateIterator = compatible.iterator();
        stateIterator.next();
        boolean allNeighbours = true;
        while (stateIterator.hasNext()) {
          final int state = stateIterator.next();
          if (!isNeighbour(neighbour, state)) {
            allNeighbours = false;
          }
        }
        if (allNeighbours) {
          possibleInclusions.add(neighbour);
        }
      }
    }

    return BronKerbosch(compatible, possibleInclusions, new TIntArrayList());
  }

  private boolean isNeighbour(final int x, final int y) {
    return !mIncompatibilityRelation[x][y] && x != y;
  }

  private TIntList getNeighboursOf(final int x) {
    final TIntList neighbours = new TIntArrayList();
    for (int s = 0; s < mNumStates; s++) {
      //if the two states are compatible and not the same state, add to neighbours
      if (isNeighbour(x, s)) {
        neighbours.add(s);
      }
    }
    return neighbours;
  }

  private void getSuccessorCompatiblesOf(final Compatible compatible, final Collection<Compatible> successorsToFill) {
    final TIntSet enabledEvents = new TIntHashSet();
    getEnabledEventsOf(compatible, enabledEvents);

    //for each event, we see what compatible is generated by taking that event from each state in the original compatible
    for (final TIntIterator eventIterator = enabledEvents.iterator(); eventIterator.hasNext();) {
      final int event = eventIterator.next();
      final Compatible successorCompatible = new Compatible();

      //for each state in original compatible, retrieve the target state which forms part of a compatible
      for (final TIntIterator compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
        final int targetState = getSuccessorState(compatibleIterator.next(), event);
        //if the target exists
        if (targetState != -1) {
          successorCompatible.add(targetState);
        }
      }
      successorsToFill.add(successorCompatible);
    }
  }

  private void fillUncoveredCompatibles(final Collection<Compatible> candidates, final Collection<Compatible> existingCompatibles, final Collection<Compatible> uncoveredCompatibles) {
    for (final Iterator<Compatible> candidateIterator = candidates.iterator(); candidateIterator.hasNext();) {
      final Compatible candidate = candidateIterator.next();
      boolean isCovered = false;
      for (final Iterator<Compatible> existingCompatibleIterator = existingCompatibles.iterator(); existingCompatibleIterator.hasNext() && !isCovered;) {
        final Compatible existingCompatible = existingCompatibleIterator.next();
        if (existingCompatible.containsAll(candidate)) {
          isCovered = true;
        }
      }
      if (!isCovered) {
        uncoveredCompatibles.add(candidate);
      }
    }
  }

  private void getEnabledEventsOf(final Compatible compatible, final TIntSet eventSetToFill) {
    //get the union of enabled events across states in the compatible
    final TIntList successorEvents = new TIntArrayList();
    for (final TIntIterator compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
      successorEvents.clear();
      getSuccessorEvents(compatibleIterator.next(), successorEvents);
      eventSetToFill.addAll(successorEvents);
    }
  }

  private class Compatible {
    private final TIntSet stateSet;

    public Compatible() {
      stateSet = new TIntHashSet();
    }

    public Compatible(final Compatible existingCompatible) {
      this();
      stateSet.addAll(existingCompatible.stateSet);
    }

    public boolean add(final int state) {
      return stateSet.add(state);
    }

    public boolean contains(final int state) {
      return stateSet.contains(state);
    }

    public boolean containsAll(final Compatible otherCompatible) {
      return stateSet.containsAll(otherCompatible.stateSet);
    }

    public int size() {
      return stateSet.size();
    }

    public TIntIterator iterator() {
      return stateSet.iterator();
    }

    @Override
    public boolean equals(final Object obj)
    {
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      final Compatible otherCompatible = (Compatible)obj;
      return stateSet.equals(otherCompatible.stateSet);
    }

    @Override
    public int hashCode()
    {
      return  347 * stateSet.hashCode();
    }

    @Override
    public String toString()
    {
      final int[] sortedStateBuffer = new int[stateSet.size()];
      stateSet.toArray(sortedStateBuffer);
      Arrays.sort(sortedStateBuffer);
      return sortedStateBuffer.toString();
    }
  }

  private boolean[][] getIncompatibilityRelation(final StateOutput[] stateOutputs) {
    //to avoid having to loop through the entire matrix to assume each state pair is compatible,
    //we will just reverse interpretation of the entire matrix

    final boolean[][] incompatibilityRelation = new boolean[mNumStates][mNumStates];
    final TransitionIterator xPredecessorIterator = getTransitionRelation().createPredecessorsReadOnlyIterator();
    final TransitionIterator yPredecessorIterator = getTransitionRelation().createPredecessorsReadOnlyIterator();

    for (int x = 0; x < mNumStates; x++) {

      final StateOutput outputX = stateOutputs[x];

      //if this state doesn't care about supervisor event, skip all its pairs
      if (outputX.equals(StateOutput.IGNORE)) { continue; }

      for (int y = 0; y < x; y++) {

        //if we have already established this pair is incompatible, skip
        if (incompatibilityRelation[x][y]) { continue; }

        final StateOutput outputY = stateOutputs[y];
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

            //mark the pair and its reverse as incompatible
            incompatibilityRelation[markX][markY] = true;
            incompatibilityRelation[markY][markX] = true;

            yPredecessorIterator.resetState(markY);

            while (yPredecessorIterator.advance()) {
              final int event = yPredecessorIterator.getCurrentEvent();
              xPredecessorIterator.reset(markX, event);

              while (xPredecessorIterator.advance()) {
                final int predY = yPredecessorIterator.getCurrentSourceState();
                final int predX = xPredecessorIterator.getCurrentSourceState();
                if (!incompatibilityRelation[predX][predY]) {
                  incompatiblesToMark.push(createStatePair(predX, predY));
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
    return (((long)x << 32) | y) & 0xFFFFFFFF;
  }

  private int getXFromStatePair(final long statePair) {
    return (int)(statePair >>> 32);
  }

  private int getYFromStatePair(final long statePair) {
    return (int)(statePair & 0xFFFFFFFF);
  }

  private int getSuccessorState(final int source, final int event)
  {
    final TransitionIterator successorIterator = getTransitionRelation().createSuccessorsReadOnlyIterator();
    successorIterator.reset(source, event);

    if (successorIterator.advance()) {
      return successorIterator.getCurrentTargetState();
    } else {
      return -1;
    }
  }

  private void getSuccessorEvents(final int source, final TIntList successorEventsToFill) {
    final TransitionIterator successorIterator = getTransitionRelation().createSuccessorsReadOnlyIterator();
    successorIterator.resetState(source);

    while (successorIterator.advance()) {
      successorEventsToFill.add(successorIterator.getCurrentEvent());
    }
  }

  private enum StateOutput {
    ENABLE,
    DISABLE,
    IGNORE
  }

  //#########################################################################
  //# Data Members
  private int mNumProperEvents;
  private int mDumpStateIndex;
  private boolean[][] mIncompatibilityRelation;
  private Compatible mInitialCompatible;
  private int mNumStates;
  private List<Compatible> mReducedSupervisor;
}
