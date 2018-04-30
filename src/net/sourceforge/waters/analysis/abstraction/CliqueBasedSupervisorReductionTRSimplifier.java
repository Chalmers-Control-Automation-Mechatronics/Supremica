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
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
    mInitialCompatible = new TreeSet<Integer>();
    mReducedSupervisor = new ArrayList<>();

    for (int s = 0; s < mNumStates; s++) {
      //set the reduced supervisor to the current set of states
      final TreeSet<Integer> oneStateCompatible = new TreeSet<>();
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
    final List<SortedSet<Integer>> initialCompatibles = getCoversOf(mInitialCompatible);
    for (final SortedSet<Integer> initialCompatible : initialCompatibles) {
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
        final SortedSet<Integer> currentCompatible = mReducedSupervisor.get(startStateIndexOffset);
        if (currentCompatible.containsAll(mInitialCompatible)) { break; }
      }

      //we must now go through each compatible (which is to become a state in the reduced automaton) and determine its successors
      //in doing so, we build up a transition relation. We will use the index of the compatible in mReducedSupervisor to denote its index in the transition buffer
      //however we want the initial state to have index 0 so we will offset all values by that amount
      final PreTransitionBuffer transitionBuffer = new PreTransitionBuffer(mNumProperEvents);
      boolean hasExplicitDumpState = false;

      for (int state = 0; state < supervisorSize; state++) {
        final int compatibleIndex = (state + startStateIndexOffset) % supervisorSize;
        final SortedSet<Integer> compatible = mReducedSupervisor.get(compatibleIndex);

        //get the union of all events among the compatible's states (from the original automaton definition)
        final TIntSet enabledEvents = getEnabledEventsOf(compatible);
        for (final TIntIterator eventIterator = enabledEvents.iterator(); eventIterator.hasNext();) {
          final int event = eventIterator.next();
          final SortedSet<Integer> successorCompatible = new TreeSet<Integer>();

          //for each state in original compatible, retrieve the successor state which forms part of a successor compatible
          for (final Iterator<Integer> compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
            final int targetState = getSuccessorState(compatibleIterator.next(), event);
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
              //if (successorState == state) { continue; }
              final int successorCompatibleIndex = (successorState + startStateIndexOffset) % supervisorSize;
              if (mReducedSupervisor.get(successorCompatibleIndex).containsAll(successorCompatible)) {
                break;
              }
            }
          }

          //TODO: do I need to add a dump state and have transitions to it from each state for each event in the original automaton that's not enabled by that state?
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
        final SortedSet<Integer> compatible = mReducedSupervisor.get(compatibleIndex);
        long newStateMarkings = relation.createMarkings();

        for (final Iterator<Integer> compatibleStateIterator = compatible.iterator(); compatibleStateIterator.hasNext();) {
          final int compatibleState = compatibleStateIterator.next();
          final long oldStateMarkings = oldStateBuffer.getAllMarkings(compatibleState);
          newStateMarkings = relation.mergeMarkings(newStateMarkings, oldStateMarkings);
        }
        relation.setAllMarkings(state, newStateMarkings);
      }

      relation.removeProperSelfLoopEvents();

      return true;
    }
  }

  private void reduce(final SortedSet<Integer> initialCompatible) {
    final List<SortedSet<Integer>> compatibles = new ArrayList<SortedSet<Integer>>();
    compatibles.add(initialCompatible);
    final Deque<SortedSet<Integer>> dependents = getNeighboursOf(initialCompatible);
    final Deque<SortedSet<Integer>> uncoveredDependents = getUncoveredCompatibles(dependents, compatibles);
    reduce(compatibles, uncoveredDependents);
  }

  private void reduce(final List<SortedSet<Integer>> currentSolution, final Deque<SortedSet<Integer>> compatibleDependencies) {
    if (currentSolution.size() >= mReducedSupervisor.size()) {
      return;
    }

    if (compatibleDependencies.isEmpty()) {
      //if we got here we know our solution is better
      mReducedSupervisor = currentSolution;
      return;
    }

    //get the next compatible we need to add
    final SortedSet<Integer> nextCompatible = compatibleDependencies.pop();
    //get all (maximal) cliques covering this compatible
    final List<SortedSet<Integer>> covers = getCoversOf(nextCompatible);
    for (final Iterator<SortedSet<Integer>> coversIterator = covers.iterator(); coversIterator.hasNext();) {
      //try to add this cover of the the compatible we want to add
      final SortedSet<Integer> cover = coversIterator.next();

      //adding this means our supervisor has to cover its successor compatibles
      final Deque<SortedSet<Integer>> dependents = getNeighboursOf(cover);

      //copy the current solution and add the candidate
      final List<SortedSet<Integer>> newSolution = new ArrayList<SortedSet<Integer>>(currentSolution);
      newSolution.add(cover);

      final List<SortedSet<Integer>> coverSet = new ArrayList<>();
      coverSet.addAll(compatibleDependencies);
      coverSet.addAll(newSolution);

      final Deque<SortedSet<Integer>> newCompatibleDependencies = new ArrayDeque<>(compatibleDependencies);
      newCompatibleDependencies.addAll(getUncoveredCompatibles(dependents, coverSet));

      reduce(newSolution, newCompatibleDependencies);
    }
  }

  private List<SortedSet<Integer>> BronKerbosch(final SortedSet<Integer> clique, final TIntList possibleInclusions, final TIntList alreadyChecked) {
    return BronKerbosch(clique, possibleInclusions, alreadyChecked, new ArrayList<SortedSet<Integer>>());
  }

  private List<SortedSet<Integer>> BronKerbosch(final SortedSet<Integer> clique, final TIntList possibleInclusions, final TIntList alreadyChecked, final List<SortedSet<Integer>> cliques) {
    //if we have exhausted all possibilities, we have no more work to do
    if (possibleInclusions.isEmpty() && alreadyChecked.isEmpty()) {
      //add this maximal clique
      cliques.add(clique);
      return cliques;
    }

    //create a copy in case removing items from possibleInclusions affects the iterator mid-way through
    final TIntList originalPossibleInclusions = new TIntArrayList(possibleInclusions);

    for (final TIntIterator inclusionIterator = originalPossibleInclusions.iterator(); inclusionIterator.hasNext();) {
      final int vertex = inclusionIterator.next();
      final TIntList neighbours = getNeighboursOf(vertex);

      //create a copy with the new vertex
      final SortedSet<Integer> newClique = new TreeSet<Integer>(clique);
      newClique.add(vertex);

      //create a copy with a restricted set of neighbours
      final TIntList newPossibleInclusions = new TIntArrayList(possibleInclusions);
      newPossibleInclusions.retainAll(neighbours);

      final TIntList newAlreadyChecked = new TIntArrayList(alreadyChecked);

      //find any maximal cliques based on newCliques and add them to the object referenced by cliques
      BronKerbosch(newClique, newPossibleInclusions, newAlreadyChecked, cliques);

      possibleInclusions.remove(vertex);
      alreadyChecked.add(vertex);
    }

    return cliques;
  }

  private List<SortedSet<Integer>> getCoversOf(final SortedSet<Integer> compatible) {
    final TIntList possibleInclusions = new TIntArrayList();
    //start with all the states as possible inclusions to BronKerbosch
    for (int s = 0; s < mNumStates; s++) {
        possibleInclusions.add(s);
    }

    //keep restricting the possible inclusions to just include neighbours of states in the compatible
    for (final Iterator<Integer> compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
      possibleInclusions.retainAll(getNeighboursOf(compatibleIterator.next()));
    }
    return BronKerbosch(compatible, possibleInclusions, new TIntArrayList());
  }

  private TIntList getNeighboursOf(final int vertex) {
    final TIntList neighbours = new TIntArrayList();
    for (int s = 0; s < mNumStates; s++) {
      //if the two states are compatible and not the same state, add to neighbours
      if (!mIncompatibilityRelation[vertex][s] && s != vertex) {
        neighbours.add(s);
      }
    }
    return neighbours;
  }

  private Deque<SortedSet<Integer>> getNeighboursOf(final SortedSet<Integer> compatible) {
    final Deque<SortedSet<Integer>> neighbours = new ArrayDeque<>();

    final TIntSet enabledEvents = getEnabledEventsOf(compatible);

    //for each event, we see what compatible is generated by taking that event from each state in the original compatible
    for (final TIntIterator eventIterator = enabledEvents.iterator(); eventIterator.hasNext();) {
      final int event = eventIterator.next();
      final SortedSet<Integer> successorCompatible = new TreeSet<Integer>();

      //for each state in original compatible, retrieve the target state which forms part of a compatible
      for (final Iterator<Integer> compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
        final int targetState = getSuccessorState(compatibleIterator.next(), event);
        //if the target exists
        if (targetState != -1) {
          successorCompatible.add(targetState);
        }
      }
      neighbours.add(successorCompatible);
    }
    return neighbours;
  }

  private TIntSet getEnabledEventsOf(final SortedSet<Integer> compatible) {
    final TIntSet enabledEvents = new TIntHashSet();

    //get the union of enabled events across states in the compatible
    for (final Iterator<Integer> compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
      enabledEvents.addAll(getSuccessorEvents(compatibleIterator.next()));
    }
    return enabledEvents;
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

  private Deque<SortedSet<Integer>> getUncoveredCompatibles(final Collection<SortedSet<Integer>> candidates, final Collection<SortedSet<Integer>> existingCompatibles) {
    //use a linked hashset to retain order and ensure no duplicates are found
    final LinkedHashSet<SortedSet<Integer>> uncoveredCompatibles = new LinkedHashSet<>();
    for (final Iterator<SortedSet<Integer>> candidateIterator = candidates.iterator(); candidateIterator.hasNext();) {
      final SortedSet<Integer> candidate = candidateIterator.next();
      boolean isCovered = false;
      for (final Iterator<SortedSet<Integer>> existingCompatibleIterator = existingCompatibles.iterator(); existingCompatibleIterator.hasNext() && !isCovered;) {
        final SortedSet<Integer> existingCompatible = existingCompatibleIterator.next();
        if (existingCompatible.containsAll(candidate)) {
          isCovered = true;
        }
      }
      if (!isCovered) {
        uncoveredCompatibles.add(candidate);
      }
    }
    return new ArrayDeque<SortedSet<Integer>>(uncoveredCompatibles);
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

  private TIntList getSuccessorEvents(final int source) {
    final TransitionIterator successorIterator = getTransitionRelation().createSuccessorsReadOnlyIterator();
    successorIterator.resetState(source);

    final TIntList successorEvents = new TIntArrayList();
    while (successorIterator.advance()) {
      successorEvents.add(successorIterator.getCurrentEvent());
    }

    return successorEvents;
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
  private TreeSet<Integer> mInitialCompatible;
  private int mNumStates;
  private List<SortedSet<Integer>> mReducedSupervisor;
}
