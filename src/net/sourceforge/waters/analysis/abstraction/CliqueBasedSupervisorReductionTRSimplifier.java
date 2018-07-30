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

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;
import gnu.trove.stack.array.TLongArrayStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import net.sourceforge.waters.analysis.tr.AbstractStateBuffer;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;


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

    final ListBufferTransitionRelation relation = getTransitionRelation();

    mDumpState = relation.getDumpStateIndex();
    mNumStates = relation.getNumberOfStates();

    mCompatibleCache = new IntSetBuffer(mNumStates);

    mCompatibleBuffer = new TIntArrayList(mNumStates);
    mDependencyIdBuffer = new TIntArrayList();
    mEnabledEventsBuffer =
      new TIntHashSet(relation.getNumberOfProperEvents());
    mStackBuffer = new TIntArrayStack();
    mAlreadyExaminedIdsBuffer = new TIntHashSet();

    mIncompatibilityRelation =
      getIncompatibilityRelation(getSupervisedEvent());

    final TIntSet initialCompatible = new TIntHashSet();
    mStatesCompatibleWithAll = new TIntHashSet(mNumStates);
    mStateToNeighbourCountMap = new TIntIntHashMap(mNumStates);
    for (int s = 0; s < mNumStates; s++) {
      int numNeighbours = 0;
      for (int i = 0; i < mNumStates; i++) {
        if (!mIncompatibilityRelation[s][i]) {
          numNeighbours++;
        }
      }
      if (relation.isInitial(s)) {
        initialCompatible.add(s);
      }
      if (numNeighbours == mNumStates) {
        mStatesCompatibleWithAll.add(s);
      }
      mStateToNeighbourCountMap.put(s, numNeighbours);
    }
    initialCompatible.addAll(mStatesCompatibleWithAll);
    mInitialCompatibleId = mCompatibleCache.add(initialCompatible);
  }

  @Override
  protected boolean runSimplifier() throws AnalysisException
  {

    //get the set of compatibles that cover the initial state, and we will try to reduce the supervisor using each
    final PriorityQueue<Candidate> searchSpace =
      new PriorityQueue<>(new Comparator<Candidate>() {
        @Override
        public int compare(final Candidate o1, final Candidate o2)
        {
          return Integer.compare(o1.size() + o1.dependenciesSize(),
                                 o2.size() + o2.dependenciesSize());
        }
      });

    //TODO precompute the successors of the compatible formed by the all-compatible states cos we will always need to add these to successors
    final Candidate startingCandidate = new Candidate(mNumStates);
    startingCandidate.addDependency(mInitialCompatibleId);

    searchSpace.add(startingCandidate);

    final Candidate reducedSupervisor = reduce(searchSpace);

    if (reducedSupervisor == null) {
      System.out.println("\nCould not reduce supervisor");
      return false;
    }
    System.out.print("\n#Reduced: " + reducedSupervisor.size() + " #Initial: "
                     + mNumStates);

    if (reducedSupervisor.size() + 1 >= mNumStates) {
      return false;
    } else {
      //we managed to reduce the supervisor
      return buildReducedSupervisor(reducedSupervisor);
    }
  }

  private Candidate reduce(final PriorityQueue<Candidate> searchSpace)
  {
    Candidate reducedSupervisor = null;
    int reducedSupervisorSize = mNumStates;

    while (!searchSpace.isEmpty()) {
      //get the most promising partial solution
      final Candidate candidateSolution = searchSpace.poll();

      System.out.print("\n" + candidateSolution);

      //if solution is complete and more reduced, save it
      if (candidateSolution.size() < reducedSupervisorSize
          && candidateSolution.dependenciesSize() == 0) {
        //if we got here we know our solution is better than the current best
        reducedSupervisor = candidateSolution;
        reducedSupervisorSize = reducedSupervisor.size();
        System.out.print("-- New Best");
        continue;
      }

      //if we are too big skip (we could stop the search if our ordering of the queue were just based on solution size)
      if (candidateSolution.size() + 1 >= reducedSupervisorSize) {
        System.out.print("-- Too big.");
        continue;
      }

      //our container for the actual compatible being processed at any given time
      mCompatibleBuffer.clear();

      //get the id of next compatible we need to add
      final int nextCompatibleId = candidateSolution.popDependency();

      //get all ids of compatibles covering this compatible
      getCompatibleFromCache(nextCompatibleId, mCompatibleBuffer);
      final TIntList coverIds = getCoversOf(mCompatibleBuffer);
      final int coverIdsSize = coverIds.size();

      for (int c = 0; c < coverIdsSize; c++) {
        //get the id of a cover
        final int coverId = coverIds.get(c);

        //copy the current solution and dependencies.
        final Candidate newSolution = new Candidate(candidateSolution);

        //we now need to add the cover and update dependencies to include the cover's successors
        newSolution.add(coverId);

        mCompatibleBuffer.clear();
        mDependencyIdBuffer.clear();
        mEnabledEventsBuffer.clear();

        //get the actual compatible for the cover id
        getCompatibleFromCache(coverId, mCompatibleBuffer);

        //get the compatible ids of its successors
        getSuccessorCompatiblesOf(mCompatibleBuffer, mEnabledEventsBuffer,
                                  mDependencyIdBuffer);

        processNewDependencies(newSolution, mDependencyIdBuffer);

        //now go through all the successors of our cover - we need to all of these (or covers of) in our final solution
        searchSpace.add(newSolution);
      }
    }
    return reducedSupervisor;
  }

  private boolean buildReducedSupervisor(final Candidate reducedSupervisor)
    throws OverflowException
  {

    final ListBufferTransitionRelation relation = getTransitionRelation();
    final AbstractStateBuffer oldStateBuffer = relation.getStateBuffer();

    //translate set of compatible ids which comprise the reduced supervisor into the compatibles themselves
    final TIntSet[] newSupervisor = new TIntHashSet[reducedSupervisor.size()];
    int i = 0;
    for (final TIntIterator compatibleIdIterator =
      reducedSupervisor.iterator(); compatibleIdIterator.hasNext(); i++) {
      newSupervisor[i] = new TIntHashSet();
      getCompatibleFromCache(compatibleIdIterator.next(), newSupervisor[i]);
    }

    final TIntList initialCompatible = new TIntArrayList();
    getCompatibleFromCache(mInitialCompatibleId, initialCompatible);

    //we have the set of compatibles, now we need to construct a new automaton based off them
    //find a compatible containing the initial set of states (it may could be a cover if a larger clique was found)
    int startStateIndexOffset = 0;
    for (; startStateIndexOffset < newSupervisor.length; startStateIndexOffset++) {
      final TIntSet currentCompatible = newSupervisor[startStateIndexOffset];
      if (currentCompatible.containsAll(initialCompatible)) {
        break;
      }
    }

    //we must now go through each compatible (which is to become a state in the reduced automaton) and determine its successors
    //in doing so, we build up a transition relation. We will use the index of the compatible in mReducedSupervisor to denote its index in the transition buffer
    //however we want the initial state to have index 0 so we will offset all values by that amount
    final PreTransitionBuffer transitionBuffer =
      new PreTransitionBuffer(relation.getNumberOfProperEvents());
    boolean hasExplicitDumpState = false;

    for (int s = 0; s < newSupervisor.length; s++) {
      final int compatibleIndex =
        (s + startStateIndexOffset) % newSupervisor.length;
      final TIntSet compatible = newSupervisor[compatibleIndex];

      //get the union of all events among the compatible's states (from the original automaton definition)
      mEnabledEventsBuffer.clear();
      getEnabledEventsOf(compatible, mEnabledEventsBuffer);
      for (final TIntIterator eventIterator =
        mEnabledEventsBuffer.iterator(); eventIterator.hasNext();) {
        final int event = eventIterator.next();
        final TIntSet successorCompatible = new TIntHashSet();

        //for each state in original compatible, retrieve the successor state which forms part of a successor compatible
        for (final TIntIterator compatibleIterator =
          compatible.iterator(); compatibleIterator.hasNext();) {
          final int targetState =
            getSuccessorState(compatibleIterator.next(), event);
          //if the target exists
          if (targetState != -1) {
            successorCompatible.add(targetState);
          }
        }

        //map this successorCompatible to its index in our list of compatibles
        int successorState = 0;

        //map the dump state to a special state index
        if (successorCompatible.size() == 1
            && successorCompatible.contains(mDumpState)) {
          successorState = newSupervisor.length;
          hasExplicitDumpState = true;
        } else {
          for (; successorState < newSupervisor.length; successorState++) {
            final int successorCompatibleIndex =
              (successorState + startStateIndexOffset) % newSupervisor.length;
            if (newSupervisor[successorCompatibleIndex]
              .containsAll(successorCompatible)) {
              break;
            }
          }
        }

        transitionBuffer.addTransition(s, event, successorState);
      }
    }

    //explicitly identify the dump state
    relation.reset(newSupervisor.length + 1, newSupervisor.length,
                   transitionBuffer.size(), getPreferredInputConfiguration());
    //set the dump state as reachable only if there is actually a transition to it from our cliques
    relation.setReachable(newSupervisor.length, hasExplicitDumpState);

    relation.setInitial(0, true);

    transitionBuffer.addOutgoingTransitions(relation);
    relation.removeRedundantPropositions();
    relation.removeEvent(EventEncoding.TAU);
    relation.removeProperSelfLoopEvents();

    for (int s = 0; s < newSupervisor.length; s++) {
      final int compatibleIndex =
        (s + startStateIndexOffset) % newSupervisor.length;
      final TIntSet compatible = newSupervisor[compatibleIndex];
      long newStateMarkings = relation.createMarkings();

      for (final TIntIterator compatibleIterator =
        compatible.iterator(); compatibleIterator.hasNext();) {
        final long oldStateMarkings =
          oldStateBuffer.getAllMarkings(compatibleIterator.next());
        newStateMarkings =
          relation.mergeMarkings(newStateMarkings, oldStateMarkings);
      }
      relation.setAllMarkings(s, newStateMarkings);
    }

    return true;
  }

  /**
   * Determines which of the dependent compatibles only has one maximal cover.
   * These can be added directly to the candidate solution rather than the
   * queue of dependencies. Adding a compatible to the candidate solution may
   * introduce more dependencies, so this is done until saturation.
   *
   * @param candidate
   *          The candidate solution to which we want to add compatibles
   * @param dependentIds
   *          The initial set of compatibles we know we need to cover in our
   *          solution.
   */
  public void processNewDependencies(final Candidate candidate,
                                     final TIntList dependentIds)
  {
    mAlreadyExaminedIdsBuffer.clear();
    mStackBuffer.clear();
    for (int i = 0; i < dependentIds.size(); i++) {
      final int id = dependentIds.get(i);
      mStackBuffer.push(id);
      mAlreadyExaminedIdsBuffer.add(id);
    }
    processNewDependencies(candidate, mStackBuffer, mAlreadyExaminedIdsBuffer);
  }

  /**
   * Determines which of the dependent compatibles only has one maximal cover.
   * These can be added directly to the candidate solution rather than the
   * queue of dependencies. Adding a compatible to the candidate solution may
   * introduce more dependencies, so this is done until saturation.
   *
   * @param candidate
   *          The candidate solution to which we want to add compatibles
   * @param dependentId A compatible id we know we need to cover in our final solution.
   */
  public void processNewDependencies(final Candidate candidate,
                                     final int dependentId)
  {
    mAlreadyExaminedIdsBuffer.clear();
    mStackBuffer.clear();
    mStackBuffer.push(dependentId);
    mAlreadyExaminedIdsBuffer.add(dependentId);
    processNewDependencies(candidate, mStackBuffer, mAlreadyExaminedIdsBuffer);
  }

  public void processNewDependencies(final Candidate candidate,
                                     final TIntStack dependentIdStack,
                                     final TIntSet alreadyProcessedIds) {
    while (dependentIdStack.size() > 0) {
      final int dependentId = dependentIdStack.pop();

      mCompatibleBuffer.clear();
      getCompatibleFromCache(dependentId, mCompatibleBuffer);

      final TIntList singleMaximalCover =
        checkForSingleMaximalCoverOf(mCompatibleBuffer);

      if (singleMaximalCover == null) {
        candidate.addDependency(dependentId);
      } else {
        singleMaximalCover.sort();
        if (candidate.add(mCompatibleCache.add(singleMaximalCover))) {

          mDependencyIdBuffer.clear();
          mEnabledEventsBuffer.clear();
          //get the compatible ids of its successors
          getSuccessorCompatiblesOf(singleMaximalCover, mEnabledEventsBuffer,
                                    mDependencyIdBuffer);

          for (int j = 0; j < mDependencyIdBuffer.size(); j++) {
            final int id = mDependencyIdBuffer.get(j);
            if (alreadyProcessedIds.add(id)) {
              dependentIdStack.push(id);
            }
          }
        }

      }
    }
  }

  private void bronKerboschWithPivot(final TIntList clique,
                                     final TIntList possibleAdditions,
                                     final TIntList alreadyChecked,
                                     final TIntList maximalCliquesIdsToFill)
  {

    //if we have exhausted all possibilities, this must be the largest clique we have seen
    if (possibleAdditions.isEmpty() && alreadyChecked.isEmpty()) {
      clique.sort();
      maximalCliquesIdsToFill.add(mCompatibleCache.add(clique));
      return;
    }

    final int initialPossibleAdditionsSize = possibleAdditions.size();

    int pivot = 0;
    int mostNeighbours = 0;

    for (int i = 0; i < initialPossibleAdditionsSize; i++) {
      final int state = possibleAdditions.get(i);
      final int numNeighbours = mStateToNeighbourCountMap.get(state);
      if (numNeighbours > mostNeighbours) {
        mostNeighbours = numNeighbours;
        pivot = state;
      }
    }

    for (int i = 0; i < alreadyChecked.size(); i++) {
      final int state = alreadyChecked.get(i);
      final int numNeighbours = mStateToNeighbourCountMap.get(state);
      if (numNeighbours > mostNeighbours) {
        mostNeighbours = numNeighbours;
        pivot = state;
      }
    }

    //go through each state we haven't tried adding yet
    for (int a = initialPossibleAdditionsSize - 1; a >= 0; a--) {
      final int addition = possibleAdditions.get(a);

      //any search path trying to add a neighbour of the pivot will already be explored when the pivot is chosen as the addition, so skip
      if (isNeighbour(pivot, addition)) {
        continue;
      }

      //create a copy with the new vertex
      final TIntList newClique = new TIntArrayList(clique);
      newClique.add(addition);

      //create a copy with a restricted set of neighbours: they have to also be neighbours of the state we are adding
      final TIntList newPossibleAdditions =
        new TIntArrayList(possibleAdditions.size());
      for (int i = 0; i < possibleAdditions.size(); i++) {
        final int oldPossibleAddition = possibleAdditions.get(i);
        if (isNeighbour(addition, oldPossibleAddition)) {
          newPossibleAdditions.add(oldPossibleAddition);
        }
      }

      final TIntList newAlreadyChecked =
        new TIntArrayList(alreadyChecked.size());
      for (int i = 0; i < alreadyChecked.size(); i++) {
        final int oldAlreadyChecked = alreadyChecked.get(i);
        if (isNeighbour(addition, oldAlreadyChecked)) {
          newAlreadyChecked.add(oldAlreadyChecked);
        }
      }

      //find any maximal cliques based on newCliques and add them to the object referenced by cliques
      bronKerboschWithPivot(newClique, newPossibleAdditions,
                            newAlreadyChecked, maximalCliquesIdsToFill);

      //remove the current candidate state from further consideration
      possibleAdditions.removeAt(a);

      //add to the list of states we have already checked
      alreadyChecked.add(addition);
    }
  }

  /**
   * Determines whether the specified compatible can be covered by just one
   * maximal compatible. This performs a mini BronKerbosch search that can
   * exit early once it determines there is more than one maximal cover.
   *
   * @param compatible
   *          The compatible we would like check
   * @return null if there is more than one maximal cover, otherwise it
   *         returns the single maximal cover.
   */
  private TIntList checkForSingleMaximalCoverOf(final TIntList compatible)
  {
    final TIntList neighbours = new TIntArrayList(mNumStates);
    getNeighboursOf(compatible, neighbours);
    final int neighboursSize = neighbours.size();

    boolean allMutualNeighbours = true;
    for (int i = 0; i < neighboursSize && allMutualNeighbours; i++) {
      final int n1 = neighbours.get(i);
      for (int j = 0; j < neighboursSize && allMutualNeighbours; j++) {
        final int n2 = neighbours.get(j);
        if (!isNeighbour(n1, n2)) {
          allMutualNeighbours = false;
        }
      }
    }
    if (allMutualNeighbours) {
      neighbours.addAll(compatible);
      return neighbours;
    } else {
      return null;
    }
  }

  private void getNeighboursOf(final TIntList compatible,
                               final TIntList neighboursToFill)
  {
    if (compatible.size() > 0) {
      //we need to figure out the initial valid set of states that could be included to our compatible
      //at most, our possible additions will be the set of neighbours to the first state in the compatible
      getNeighboursOf(compatible.get(0), neighboursToFill);
      final int neighboursSize = neighboursToFill.size();

      //then we go through each of these neighbours and cull ones that aren't mutually neighbours with the rest
      //of the states in our compatible
      for (int n = neighboursSize - 1; n >= 0; n--) {
        final int neighbour = neighboursToFill.get(n);

        boolean allNeighbours = true;
        for (int s = 1; s < compatible.size(); s++) {
          if (!isNeighbour(neighbour, compatible.get(s))) {
            allNeighbours = false;
            break;
          }
        }
        if (!allNeighbours) {
          neighboursToFill.removeAt(n);
        }
      }
    }
  }

  private void getNeighboursOf(final int state,
                               final TIntList neighboursToFill)
  {
    for (int s = 0; s < mNumStates; s++) {
      //if the two states are compatible and not the same state, add to neighbours
      if (isNeighbour(state, s)) {
        neighboursToFill.add(s);
      }
    }
  }

  private TIntList getCoversOf(final TIntList compatible)
  {
    final TIntList possibleAdditions = new TIntArrayList(mNumStates);
    getNeighboursOf(compatible, possibleAdditions);

    //actually get the cover compatibles
    final TIntList maximalCompatibleIds = new TIntArrayList();
    bronKerboschWithPivot(compatible, possibleAdditions, new TIntArrayList(),
                          maximalCompatibleIds);

    return maximalCompatibleIds;
  }

  private void getCompatibleFromCache(final int compatibleId,
                                      final TIntCollection compatibleToFill)
  {
    compatibleToFill.addAll(mCompatibleCache.getSetContents(compatibleId));
  }

  private boolean isNeighbour(final int state1, final int state2)
  {
    return !mIncompatibilityRelation[state1][state2] && state1 != state2;
  }

  private void getSuccessorCompatiblesOf(final TIntList compatible,
                                         final TIntSet enabledEventsBuffer,
                                         final TIntList successorIdsToFill)
  {
    final int compatibleSize = compatible.size();
    getEnabledEventsOf(compatible, enabledEventsBuffer);

    //for each event, we see what compatible is generated by taking that event from each state in the original compatible
    for (final TIntIterator eventIterator =
      enabledEventsBuffer.iterator(); eventIterator.hasNext();) {
      final int event = eventIterator.next();
      final TIntSet successorCompatible = new TIntHashSet();

      //for each state in original compatible, retrieve the target state which forms part of a compatible
      for (int s = 0; s < compatibleSize; s++) {
        final int targetState = getSuccessorState(compatible.get(s), event);
        //if the target exists
        if (targetState != -1) {
          successorCompatible.add(targetState);
        }
      }

      successorCompatible.addAll(mStatesCompatibleWithAll);

      //add the id associated with this compatible to the set of successor compatible ids
      successorIdsToFill.add(mCompatibleCache.add(successorCompatible));
    }
  }

  private void getEnabledEventsOf(final TIntList compatible,
                                  final TIntSet enabledEventsToFill)
  {
    //get the union of enabled events across states in the compatible
    final int compatibleSize = compatible.size();
    for (int s = 0; s < compatibleSize; s++) {
      getSuccessorEvents(compatible.get(s), enabledEventsToFill);
    }
  }

  private void getEnabledEventsOf(final TIntSet compatible,
                                  final TIntSet enabledEventsToFill)
  {
    //get the union of enabled events across states in the compatible
    for (final TIntIterator compatibleIterator =
      compatible.iterator(); compatibleIterator.hasNext();) {
      getSuccessorEvents(compatibleIterator.next(), enabledEventsToFill);
    }
  }

  private int getSuccessorState(final int source, final int event)
  {
    final TransitionIterator successorIterator =
      getTransitionRelation().createSuccessorsReadOnlyIterator();
    successorIterator.reset(source, event);

    if (successorIterator.advance()) {
      return successorIterator.getCurrentTargetState();
    } else {
      return -1;
    }
  }

  private void getSuccessorEvents(final int source,
                                  final TIntSet successorEventsToFill)
  {
    final TransitionIterator successorIterator =
      getTransitionRelation().createSuccessorsReadOnlyIterator();
    successorIterator.resetState(source);

    while (successorIterator.advance()) {
      successorEventsToFill.add(successorIterator.getCurrentEvent());
    }
  }

  private boolean[][] getIncompatibilityRelation(final int supervisedEvent)
  {
    //to avoid having to loop through the entire matrix to assume each state pair is compatible,
    //we will just reverse interpretation of the entire matrix

    final ListBufferTransitionRelation relation = getTransitionRelation();
    final boolean[][] incompatibilityRelation =
      new boolean[mNumStates][mNumStates];
    final TransitionIterator xPredecessorIterator =
      relation.createPredecessorsReadOnlyIterator();
    final TransitionIterator yPredecessorIterator =
      relation.createPredecessorsReadOnlyIterator();

    for (int x = 0; x < mNumStates; x++) {

      final int successorStateX = getSuccessorState(x, supervisedEvent);

      //if this state doesn't care about supervisor event, skip all its pairs
      if (successorStateX == -1) {
        continue;
      }

      for (int y = 0; y < x; y++) {

        //if we have already established this pair is incompatible, skip
        if (incompatibilityRelation[x][y]) {
          continue;
        }

        final int successorStateY = getSuccessorState(y, supervisedEvent);
        if (successorStateY == -1) {
          continue;
        }

        if ((successorStateX == mDumpState && successorStateY != mDumpState)
            || (successorStateX != mDumpState
                && successorStateY == mDumpState)) {
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
                final int predY =
                  yPredecessorIterator.getCurrentSourceState();
                final int predX =
                  xPredecessorIterator.getCurrentSourceState();
                if (predX != predY
                    && !incompatibilityRelation[predX][predY]) {
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

  private long createStatePair(final int x, final int y)
  {
    return (((long) x << 32) | y) & 0xFFFFFFFF;
  }

  private int getXFromStatePair(final long statePair)
  {
    return (int) (statePair >>> 32);
  }

  private int getYFromStatePair(final long statePair)
  {
    return (int) (statePair & 0xFFFFFFFF);
  }


  /**
   * Contains a set of ints that each represent a Compatible (a set of
   * states). Each value is an index into the IntBufferSet, mCompatibleCache
   * and can used to retrieve the corresponding compatible.
   *
   * Ensures that none of the underlying compatibles are strict subsets of any
   * of the other compatibles in this set.
   *
   * Maintains the order of insertions so that the search space can be
   * explored systematically.
   *
   * @author Jordan Schroder
   */
  private class Dependencies
  {

    private final PriorityQueue<Integer> dependenciesQueue;
    private final TIntSet dependenciesSet;

    public Dependencies()
    {
      dependenciesQueue = new PriorityQueue<>(new Comparator<Integer>() {

        @Override
        public int compare(final Integer o1, final Integer o2)
        {
          return Integer.compare(mCompatibleCache.size(o1),
                                 mCompatibleCache.size(o2));
        }
      });
      dependenciesSet = new TIntHashSet();
    }

    public Dependencies(final Dependencies compatibleIdSet)
    {
      this();
      for (final int compatibleId : compatibleIdSet.getQueue()) {
        add(compatibleId);
      }
    }

    public PriorityQueue<Integer> getQueue()
    {
      return dependenciesQueue;
    }

    /**
     * Gets the next compatible id, based on insertion order and removes it
     * from the set.
     *
     * @return The next compatible id to process, or null if the set is empty.
     */
    public int pop()
    {
      final int poppedId = dependenciesQueue.poll();
      dependenciesSet.remove(poppedId);
      return poppedId;
    }

    public int size()
    {
      return dependenciesQueue.size();
    }

    /**
     * Checks whether the compatible associated with the specified id is a
     * subset ('covered') of any compatibles referenced by existing ids in the
     * set. If and only if this condition is met, the id is added to the set.
     * Then, any compatibles referenced by the set that covered by the newly
     * added member are then removed.
     *
     * @param applicantId
     *          the unique id corresponding to a compatible stored in an
     *          IntSetBuffer
     * @return true if and only if the compatible referenced by applicantId is
     *         not covered by existing members
     */
    public boolean add(final int applicantId)
    {
      if (size() == 0) {
        dependenciesQueue.add(applicantId);
        dependenciesSet.add(applicantId);
      }

      //check if the applicant is a strict subset of any existing members
      if (!isUncoveredByAll(applicantId)) {
        return false;
      }

      //not covered by any existing members, we want to add it.
      //now kick out any members the applicant covers
      removeAllCoveredBy(applicantId);

      if (dependenciesSet.add(applicantId)) {
        dependenciesQueue.add(applicantId);
        return true;
      }

      return false;
    }

    /**
     * Checks whether the compatible referenced by xId is a subset of (covered
     * by) any compatibles already referenced by ids in the set.
     *
     * @param xId
     *          The id of a compatible
     * @return true if and only if the compatible is not covered by any
     *         existing compatibles.
     */
    public boolean isUncoveredByAll(final int xId)
    {
      for (final int compatibleId : dependenciesQueue) {
        if (mCompatibleCache.containsAll(compatibleId, xId)) {
          return false;
        }
      }
      return true;
    }

    /**
     * Removes any compatibles in this set that are subsets of the compatible
     * referenced by xId.
     *
     * @param xId
     *          The id of a compatible
     */
    public void removeAllCoveredBy(final int xId)
    {
      for (final Iterator<Integer> compatibleIdsIterator =
        dependenciesQueue.iterator(); compatibleIdsIterator.hasNext();) {
        final int compatibleId = compatibleIdsIterator.next();
        if (mCompatibleCache.containsAll(xId, compatibleId)) {
          compatibleIdsIterator.remove();
          dependenciesSet.remove(compatibleId);
        }
      }
    }

    @Override
    public String toString()
    {
      final Integer[] compatibleIdBuffer = new Integer[size()];
      dependenciesQueue.toArray(compatibleIdBuffer);
      return Arrays.toString(compatibleIdBuffer);
    }

    public String prettyPrint()
    {
      final Integer[] set = new Integer[dependenciesQueue.size()];
      dependenciesQueue.toArray(set);
      final StringBuilder out = new StringBuilder();
      final TIntList compatible = new TIntArrayList();
      out.append("Dependencies\n");
      for (int i = 0; i < set.length; i++) {
        compatible.clear();
        getCompatibleFromCache(set[i], compatible);
        out.append(compatible).append("\n");
      }
      return out.toString();
    }

    @Override
    public int hashCode()
    {
      return dependenciesSet.hashCode();
    }

    @Override
    public boolean equals(final Object o)
    {
      if (!(o instanceof Dependencies)) {
        return false;
      }

      final Dependencies otherSet = (Dependencies) o;
      return dependenciesSet.equals(otherSet.dependenciesSet);
    }
  }


  private class Candidate extends TIntHashSet
  {
    private final Dependencies dependencies;

    public Candidate(final int capacity)
    {
      super(capacity);
      dependencies = new Dependencies();
    }

    public Candidate(final Candidate existingCandidate)
    {
      super(existingCandidate.capacity());
      dependencies = new Dependencies(existingCandidate.dependencies);
      for (final TIntIterator iterator =
        existingCandidate.iterator(); iterator.hasNext();) {
        super.add(iterator.next());
      }

    }

    @Override
    public boolean add(final int val)
    {
      if (super.add(val)) {
        dependencies.removeAllCoveredBy(val);
        return true;
      }
      return false;
    }

    public int popDependency()
    {
      return dependencies.pop();
    }

    public boolean addDependency(final int val)
    {
      if (isUncoveredByAll(val)) {
        return dependencies.add(val);
      }
      return false;
    }

    public int dependenciesSize()
    {
      return dependencies.size();
    }

    private boolean isUncoveredByAll(final int xId)
    {
      for (final TIntIterator compatibleIdsIterator =
        iterator(); compatibleIdsIterator.hasNext();) {
        if (mCompatibleCache.containsAll(compatibleIdsIterator.next(), xId)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public String toString()
    {
      final int[] set = toArray();
      Arrays.sort(set);
      return "C: " + Arrays.toString(set) + " D: " + dependencies.toString();
    }

    @SuppressWarnings("unused")
    public String prettyPrint()
    {
      final int[] set = toArray();
      final StringBuilder out = new StringBuilder();
      final TIntList compatible = new TIntArrayList();
      out.append("Solution\n");
      for (int i = 0; i < set.length; i++) {
        compatible.clear();
        getCompatibleFromCache(set[i], compatible);
        out.append(compatible).append("\n");
      }
      out.append(dependencies.prettyPrint());
      return out.toString();
    }
  }

  //#########################################################################
  //# Data Members
  private int mDumpState;
  private int mNumStates;
  private boolean[][] mIncompatibilityRelation;
  private int mInitialCompatibleId;
  private IntSetBuffer mCompatibleCache;
  private TIntList mCompatibleBuffer;
  private TIntList mDependencyIdBuffer;
  private TIntSet mEnabledEventsBuffer;
  private TIntSet mAlreadyExaminedIdsBuffer;
  private TIntStack mStackBuffer;
  private TIntIntMap mStateToNeighbourCountMap;
  private TIntSet mStatesCompatibleWithAll;
}