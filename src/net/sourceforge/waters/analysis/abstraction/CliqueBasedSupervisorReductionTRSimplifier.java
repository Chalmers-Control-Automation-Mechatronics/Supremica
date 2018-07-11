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
import gnu.trove.impl.HashFunctions;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.array.TLongArrayStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Random;

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

    final ListBufferTransitionRelation rel = getTransitionRelation();

    mDumpState = rel.getDumpStateIndex();
    final int supervisedEvent = getSupervisedEvent();

    mNumStates = rel.getNumberOfStates();
    final StateOutput[] stateOutputs = new StateOutput[mNumStates];

    final TIntList initialCompatible = new TIntArrayList();
    mReducedSupervisor = new CompatibleSet(mNumStates);
    mCompatibleCache = new IntSetBuffer(mNumStates);
    //mCoversCache = new TIntObjectHashMap<>();

    mCompatibleBuffer = new TIntArrayList(mNumStates);
    mDependencyIdBuffer = new TIntArrayList();
    mEnabledEventsBuffer = new TIntHashSet(rel.getNumberOfProperEvents());

    for (int s = 0; s < mNumStates; s++) {
      //set the reduced supervisor to the current set of states
      final TIntList oneStateCompatible = new TIntArrayList();
      oneStateCompatible.add(s);
      mReducedSupervisor.add(mCompatibleCache.add(oneStateCompatible));

      if (rel.isInitial(s)) {
        initialCompatible.add(s);
      }

      final int successorState = getSuccessorState(s, supervisedEvent);
      if (successorState == -1) {
        stateOutputs[s] = StateOutput.IGNORE;
      } else if (successorState == mDumpState) {
        stateOutputs[s] = StateOutput.DISABLE;
      } else {
        stateOutputs[s] = StateOutput.ENABLE;
      }
    }

    mInitialCompatibleId = mCompatibleCache.add(initialCompatible);
    mIncompatibilityRelation = getIncompatibilityRelation(stateOutputs);
  }


  @Override
  protected boolean runSimplifier() throws AnalysisException
  {
    //get the set of compatibles that cover the initial state, and we will try to reduce the supervisor using each
    final PriorityQueue<CompatibleSet> searchSpace = new PriorityQueue<>();
    final CompatibleDependenciesSet dependencies = new CompatibleDependenciesSet();
    dependencies.add(mInitialCompatibleId);
    searchSpace.add(new CompatibleSet(new TIntHashSet(), dependencies));
    reduce(searchSpace);
    System.out.print("\n#Reduced: " + mReducedSupervisor.size() + " #Initial: " + mNumStates);

    //account for the dump state which is included in the initial automaton but not in our compatibles
    if (mReducedSupervisor.size() + 1 >= mNumStates) {
      return false;
    }

    else {
      //we managed to reduce the supervisor
       return buildReducedSupervisor();
    }
  }

  private void reduce(final PriorityQueue<CompatibleSet> searchSpace) {
    while (true) {
      //get the most promising partial solution
      final CompatibleSet currentSolution = searchSpace.poll();

      //if we are empty, done
      if (currentSolution == null) {
        System.out.print("\nExhausted search space");
        break;
      }

      final CompatibleDependenciesSet currentDependencies = currentSolution.getDependencies();
      System.out.print("\nC: " + currentSolution.toString() + " D: " + currentDependencies + " L: " + currentSolution.size());

      //if we are too big skip (we could stop the search if our ordering of the queue were just based on solution size)
      if (currentSolution.size() + 1 >= mReducedSupervisor.size()) {
        System.out.print("-- Too big.");
        continue;
      }

      //if solution is complete and more reduced, save it
      if (currentSolution.size() < mReducedSupervisor.size() && currentDependencies.isEmpty()) {
      //if we got here we know our solution is better than the current best
        mReducedSupervisor = currentSolution;
        System.out.print("-- New Best");
        continue;
      }

      //our container for the actual compatible being processed at any given time
      mCompatibleBuffer.clear();

      //get the id of next compatible we need to add
      final int nextCompatibleId = currentDependencies.pop();

      //get all ids of compatibles covering this compatible
      getCompatibleFromCache(nextCompatibleId, mCompatibleBuffer);
      final TIntList coverIds = getCoversOf(mCompatibleBuffer);
      final int coverIdsSize = coverIds.size();

      for (int c = 0; c < coverIdsSize; c++) {
        //get the id of a cover
        final int coverId = coverIds.get(c);

        mCompatibleBuffer.clear();
        mDependencyIdBuffer.clear();
        mEnabledEventsBuffer.clear();

        //get the actual compatible for the cover id
        getCompatibleFromCache(coverId, mCompatibleBuffer);

        //get the compatible ids of its successors
        getSuccessorCompatiblesOf(mCompatibleBuffer, mEnabledEventsBuffer, mDependencyIdBuffer);
        final int dependentIdsSize = mDependencyIdBuffer.size();

        //copy the current solution and dependencies.
        final CompatibleSet newSolution = new CompatibleSet(currentSolution, currentDependencies);
        final CompatibleDependenciesSet newDependencies = newSolution.getDependencies();

        //we now need to add the cover and update dependencies to include the cover's successors
        newSolution.add(coverId);
        //now that we have added the associate compatible to the partial solution, anything covered by it in the dependencies is no longer needed
        newDependencies.removeAllCoveredBy(coverId);

        //now go through all the successors of our cover - we need to all of these (or covers of) in our final solution
        for (int d = 0; d < dependentIdsSize; d++) {
          //a successor compatible id
          final int dependentId = mDependencyIdBuffer.get(d);

          //if this compatible isn't a subset of anything in our current (and partial solution)
          if (newSolution.isUncoveredByAll(dependentId)) {
            //try adding it to the list of compatibles we know we still need to cover
            //it may not actually be added if it is already covered by an existing member
            newDependencies.add(dependentId);
          }
        }

        searchSpace.add(newSolution);
      }
    }
  }

  private boolean buildReducedSupervisor() throws OverflowException {
    final ListBufferTransitionRelation relation = getTransitionRelation();
    final AbstractStateBuffer oldStateBuffer = relation.getStateBuffer();

    //translate set of compatible ids which comprise the reduced supervisor into the compatibles themselves
    final TIntSet[] reducedSupervisor = new TIntHashSet[mReducedSupervisor.size()];
    int i = 0;
    for (final TIntIterator compatibleIdIterator = mReducedSupervisor.iterator(); compatibleIdIterator.hasNext(); i++) {
      reducedSupervisor[i] = new TIntHashSet();
      getCompatibleFromCache(compatibleIdIterator.next(), reducedSupervisor[i]);
    }

    final TIntList initialCompatible = new TIntArrayList();
    getCompatibleFromCache(mInitialCompatibleId, initialCompatible);

    //we have the set of compatibles, now we need to construct a new automaton based off them
    //find a compatible containing the initial set of states (it may could be a cover if a larger clique was found)
    int startStateIndexOffset = 0;
    for (; startStateIndexOffset < reducedSupervisor.length; startStateIndexOffset++) {
      final TIntSet currentCompatible = reducedSupervisor[startStateIndexOffset];
      if (currentCompatible.containsAll(initialCompatible)) { break; }
    }

    //we must now go through each compatible (which is to become a state in the reduced automaton) and determine its successors
    //in doing so, we build up a transition relation. We will use the index of the compatible in mReducedSupervisor to denote its index in the transition buffer
    //however we want the initial state to have index 0 so we will offset all values by that amount
    final PreTransitionBuffer transitionBuffer = new PreTransitionBuffer(relation.getNumberOfProperEvents());
    boolean hasExplicitDumpState = false;
    final TIntSet enabledEvents = new TIntHashSet();

    for (int state = 0; state < reducedSupervisor.length; state++) {
      final int compatibleIndex = (state + startStateIndexOffset) % reducedSupervisor.length;
      final TIntSet compatible = reducedSupervisor[compatibleIndex];

      //get the union of all events among the compatible's states (from the original automaton definition)
      enabledEvents.clear();
      getEnabledEventsOf(compatible, enabledEvents);
      for (final TIntIterator eventIterator = enabledEvents.iterator(); eventIterator.hasNext();) {
        final int event = eventIterator.next();
        final TIntSet successorCompatible = new TIntHashSet();

        //for each state in original compatible, retrieve the successor state which forms part of a successor compatible
        for (final TIntIterator compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
          final int targetState = getSuccessorState(compatibleIterator.next(), event);
          //if the target exists
          if (targetState != -1) {
            successorCompatible.add(targetState);
          }
        }

        //map this successorCompatible to its index in our list of compatibles
        int successorState = 0;

        //map the dump state to a special state index
        if (successorCompatible.size() == 1 && successorCompatible.contains(mDumpState)) {
          successorState = reducedSupervisor.length;
          hasExplicitDumpState = true;
        }
        else {
          for (; successorState < reducedSupervisor.length; successorState++) {
            final int successorCompatibleIndex = (successorState + startStateIndexOffset) % reducedSupervisor.length;
            if (reducedSupervisor[successorCompatibleIndex].containsAll(successorCompatible)) {
              break;
            }
          }
        }

        transitionBuffer.addTransition(state, event, successorState);
      }
    }

    //explicitly identify the dump state
    relation.reset(reducedSupervisor.length + 1, reducedSupervisor.length, transitionBuffer.size(), getPreferredInputConfiguration());
    //set the dump state as reachable only if there is actually a transition to it from our cliques
    relation.setReachable(reducedSupervisor.length, hasExplicitDumpState);

    relation.setInitial(0, true);

    transitionBuffer.addOutgoingTransitions(relation);
    relation.removeRedundantPropositions();
    relation.removeEvent(EventEncoding.TAU);
    relation.removeProperSelfLoopEvents();

    for (int state = 0; state < reducedSupervisor.length; state++) {
      final int compatibleIndex = (state + startStateIndexOffset) % reducedSupervisor.length;
      final TIntSet compatible = reducedSupervisor[compatibleIndex];
      long newStateMarkings = relation.createMarkings();

      for (final TIntIterator compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
        final long oldStateMarkings = oldStateBuffer.getAllMarkings(compatibleIterator.next());
        newStateMarkings = relation.mergeMarkings(newStateMarkings, oldStateMarkings);
      }
      relation.setAllMarkings(state, newStateMarkings);
    }

    return true;
  }


  private void BronKerbosch(final TIntList clique, final TIntList possibleAdditions, final TIntList alreadyChecked, final TIntList maximalCliquesIdsToFill) {

    //if we have exhausted all possibilities, this must be the largest clique we have seen
    if (possibleAdditions.isEmpty() && alreadyChecked.isEmpty()) {
      clique.sort();
      maximalCliquesIdsToFill.add(mCompatibleCache.add(clique));
      return;
    }

    final int initialPossibleAdditionsSize = possibleAdditions.size();

    //introduce for more pruning
    final int pivotIndex = sRandom.nextInt(initialPossibleAdditionsSize + alreadyChecked.size());
    final int pivot = pivotIndex < initialPossibleAdditionsSize ? possibleAdditions.get(pivotIndex) : alreadyChecked.get(pivotIndex - initialPossibleAdditionsSize);

    //go through each state we haven't tried adding yet
    for (int a = possibleAdditions.size() - 1; a >= 0; a--) {
      final int addition = possibleAdditions.get(a);

      //any search path trying to add a neighbour of the pivot will already be explored when the pivot is chosen as the addition, so skip
      if (isNeighbour(pivot, addition)) { continue; }

      //create a copy with the new vertex
      final TIntList newClique = new TIntArrayList(clique);
      newClique.add(addition);

      //create a copy with a restricted set of neighbours: they have to also be neighbours of the state we are adding
      final TIntList newPossibleAdditions = new TIntArrayList(possibleAdditions.size());
      for (int i = 0; i < possibleAdditions.size(); i++) {
        final int oldPossibleAddition = possibleAdditions.get(i);
        if (isNeighbour(addition, oldPossibleAddition)) {
          newPossibleAdditions.add(oldPossibleAddition);
        }
      }

      final TIntList newAlreadyChecked = new TIntArrayList(alreadyChecked.size());
      for (int i = 0; i < alreadyChecked.size(); i++) {
        final int oldAlreadyChecked  = alreadyChecked.get(i);
        if (isNeighbour(addition, oldAlreadyChecked)) {
          newAlreadyChecked.add(oldAlreadyChecked);
        }
      }

      //find any maximal cliques based on newCliques and add them to the object referenced by cliques
      BronKerbosch(newClique, newPossibleAdditions, newAlreadyChecked, maximalCliquesIdsToFill);

      //remove the current candidate state from further consideration
      possibleAdditions.removeAt(a);

      //add to the list of states we have already checked
      alreadyChecked.add(addition);
    }
  }

  private TIntList getCoversOf(final TIntList compatible) {
    final TIntList possibleAdditions = new TIntArrayList();
    if (compatible.size() > 0) {

      //we need to figure out the initial valid set of states that could be included to our compatible
      //at most, our possible additions will be the set of neighbours to the first state in the compatible
      final TIntList neighbours = new TIntArrayList();
      getNeighboursOf(compatible.get(0), neighbours);
      final int neighboursSize = neighbours.size();

      //then we go through each of these neighbours and cull ones that aren't mutually neighbours with the rest
      //of the states in our compatible
      for (int n = 0; n < neighboursSize; n++) {
        final int neighbour = neighbours.get(n);

        boolean allNeighbours = true;
        for (int s = 1; s < compatible.size() && allNeighbours; s++) {
          if (!isNeighbour(neighbour, compatible.get(s))) {
            allNeighbours = false;
          }
        }
        if (allNeighbours) {
          possibleAdditions.add(neighbour);
        }
      }
    }

    //actually get the cover compatibles
    final TIntList maximalCompatibleIds = new TIntArrayList();
    BronKerbosch(compatible, possibleAdditions, new TIntArrayList(), maximalCompatibleIds);

    return maximalCompatibleIds;
  }

  private void getCompatibleFromCache(final int compatibleId, final TIntCollection compatibleToFill) {
    compatibleToFill.addAll(mCompatibleCache.getSetContents(compatibleId));
  }

  private boolean isNeighbour(final int state1, final int state2) {
    return !mIncompatibilityRelation[state1][state2] && state1 != state2;
  }

  private void getNeighboursOf(final int state, final TIntList neighboursToFill) {
    for (int s = 0; s < mNumStates; s++) {
      //if the two states are compatible and not the same state, add to neighbours
      if (isNeighbour(state, s)) {
        neighboursToFill.add(s);
      }
    }
  }

  private void getSuccessorCompatiblesOf(final TIntList compatible, final TIntSet enabledEventsBuffer, final TIntList successorIdsToFill) {
    final int compatibleSize = compatible.size();
    getEnabledEventsOf(compatible, enabledEventsBuffer);

    //for each event, we see what compatible is generated by taking that event from each state in the original compatible
    for (final TIntIterator eventIterator = enabledEventsBuffer.iterator(); eventIterator.hasNext();) {
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

      //we now have our successor compatible, but we must document this compatible in our cache.
      //we need to insert into the cache sorted
      final TIntList sortedSuccessor = new TIntArrayList(successorCompatible);
      sortedSuccessor.sort();

      //add the id associated with this compatible to the set of successor compatible ids
      successorIdsToFill.add(mCompatibleCache.add(sortedSuccessor));
    }
  }

  private void getEnabledEventsOf(final TIntList compatible, final TIntSet enabledEventsToFill) {
    //get the union of enabled events across states in the compatible
    final int compatibleSize = compatible.size();
    for (int s = 0; s < compatibleSize; s++) {
      getSuccessorEvents(compatible.get(s), enabledEventsToFill);
    }
  }

  private void getEnabledEventsOf(final TIntSet compatible, final TIntSet enabledEventsToFill) {
    //get the union of enabled events across states in the compatible
    for (final TIntIterator compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
      getSuccessorEvents(compatibleIterator.next(), enabledEventsToFill);
    }
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

  private void getSuccessorEvents(final int source, final TIntSet successorEventsToFill) {
    final TransitionIterator successorIterator = getTransitionRelation().createSuccessorsReadOnlyIterator();
    successorIterator.resetState(source);

    while (successorIterator.advance()) {
      successorEventsToFill.add(successorIterator.getCurrentEvent());
    }
  }


  private boolean[][] getIncompatibilityRelation(final StateOutput[] stateOutputs) {
    //to avoid having to loop through the entire matrix to assume each state pair is compatible,
    //we will just reverse interpretation of the entire matrix

    final ListBufferTransitionRelation relation = getTransitionRelation();
    final boolean[][] incompatibilityRelation = new boolean[mNumStates][mNumStates];
    final TransitionIterator xPredecessorIterator = relation.createPredecessorsReadOnlyIterator();
    final TransitionIterator yPredecessorIterator = relation.createPredecessorsReadOnlyIterator();

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
                if (predX != predY && !incompatibilityRelation[predX][predY]) {
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

  /**
   * Determines whether a compatible (referenced by its index in mCompatibleCache)
   * is a superset of another compatible
   * @param xId The compatible we want the cover status of
   * @param yId The compatible we check is being covered (or not)
   * @return true if the first compatible is a superset of the second, otherwise false.
   */
  private boolean isCover(final int xId, final int yId) {
    return mCompatibleCache.containsAll(xId, yId);
  }

  /**
   * Contains a set of ints that each represent a Compatible (a set of states).
   * Each value is an index into the IntBufferSet, mCompatibleCache and can
   * used to retrieve the corresponding compatible.
   *
   * Ensures that none of the underlying compatibles are strict subsets of any
   * of the other compatibles in this set.
   *
   * Maintains the order of insertions so that the search space can be explored
   * systematically.
   * @author Jordan Schroder
   */
  private class CompatibleDependenciesSet extends LinkedHashSet<Integer> {

    private static final long serialVersionUID = -4434294284951063515L;

    public CompatibleDependenciesSet() {
      super();
    }

    public CompatibleDependenciesSet(final Collection<Integer> compatibleIdSet) {
      super(compatibleIdSet);
    }

    /**
     * Gets the next compatible id, based on insertion order and removes it from the set.
     * @return The next compatible id to process, or -1 if the set is empty.
     */
    public int pop() {
      final Iterator<Integer> iterator = iterator();
      if (iterator.hasNext()) {
        final Integer popped = iterator.next();
        iterator.remove();
        return popped;
      }
      return -1;
    }

    /**
     * Checks whether the compatible associated with the specified id
     * is a subset ('covered') of any compatibles referenced by existing ids in the set.
     * If and only if this condition is met, the id is added to the set.
     * Then, any compatibles referenced by the set that covered by the newly
     * added member are then removed.
     *
     * @param applicantId the unique id corresponding to a compatible stored in an IntSetBuffer
     * @return true if and only if the compatible referenced by applicantId is not covered by existing members
     */
    @Override
    public boolean add(final Integer applicantId) {
      if (size() == 0) {
        return super.add(applicantId);
      }

      //check if the applicant is a strict subset of any existing members
      if (!isUncoveredByAll(applicantId)) { return false; }

      //not covered by any existing members, we want to add it.
      //now kick out any members the applicant covers
      removeAllCoveredBy(applicantId);
      return super.add(applicantId);
    }

    /**
     * Checks whether the compatible referenced by xId is a subset of (covered by) any compatibles already
     * referenced by ids in the set.
     * @param xId The id of a compatible
     * @return true if and only if the compatible is not covered by any existing compatibles.
     */
    public boolean isUncoveredByAll(final int xId) {
      for (final Iterator<Integer> compatibleIdsIterator = iterator(); compatibleIdsIterator.hasNext();) {
        if (isCover(compatibleIdsIterator.next(), xId)) { return false; }
      }
      return true;
    }

    /**
     * Removes any compatibles in this set that are subsets of the compatible referenced by xId.
     * @param xId The id of a compatible
     */
    public void removeAllCoveredBy(final int xId) {
      for (final Iterator<Integer> compatibleIdsIterator = iterator(); compatibleIdsIterator.hasNext();) {
        if (isCover(xId, compatibleIdsIterator.next())) { compatibleIdsIterator.remove(); }
      }
    }


    @Override
    public String toString()
    {
      final Integer[] compatibleIdBuffer = new Integer[size()];
      this.toArray(compatibleIdBuffer);
      return Arrays.toString(compatibleIdBuffer);
    }

    @Override
    public int hashCode()
    {
      int hash = 0;
      for (final Iterator<Integer> compatibleIdsIterator = iterator(); compatibleIdsIterator.hasNext();) {
        hash += HashFunctions.hash(compatibleIdsIterator.next());
      }
      return hash;
    }

    @Override
    public boolean equals(final Object o)
    {
      if (!(o instanceof CompatibleDependenciesSet)) {
        return false;
      }

      final CompatibleDependenciesSet otherSet = (CompatibleDependenciesSet)o;
      if (size() != otherSet.size()) {
        return false;
      }

      return containsAll(otherSet);
    }
  }

  private class CompatibleSet extends TIntHashSet implements Comparable<CompatibleSet> {
    private final CompatibleDependenciesSet dependenciesSet;

    public CompatibleSet() {
      super();
      dependenciesSet = new CompatibleDependenciesSet();
    }

    public CompatibleSet(final int capacity) {
      super(capacity);
      dependenciesSet = new CompatibleDependenciesSet();
    }

    private CompatibleSet(final TIntCollection existingCompatibles, final Collection<Integer> existingDependencies) {
      super(existingCompatibles);
      dependenciesSet = new CompatibleDependenciesSet(existingDependencies);
    }

    public CompatibleSet(final CompatibleSet compatibleSet) {
      this(compatibleSet, compatibleSet.dependenciesSet);
    }

    public CompatibleDependenciesSet getDependencies() {
      return dependenciesSet;
    }

    public boolean isUncoveredByAll(final int xId) {
      for (final TIntIterator compatibleIdsIterator = iterator(); compatibleIdsIterator.hasNext();) {
        if (isCover(compatibleIdsIterator.next(), xId)) { return false; }
      }
      return true;
    }

    @Override
    public String toString()
    {
      final int[] set = toArray();
      Arrays.sort(set);
      return Arrays.toString(set);
    }

    @Override
    public int compareTo(final CompatibleSet anotherSet)
    {
      final Integer ourSize = size() + dependenciesSet.size();
      final Integer theirSize = anotherSet.size() + anotherSet.getDependencies().size();
      return ourSize.compareTo(theirSize);
    }
  }

  private enum StateOutput {
    ENABLE,
    DISABLE,
    IGNORE
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

  //private TIntObjectHashMap<TIntCollection> mCoversCache;
  private CompatibleSet mReducedSupervisor;

  private static Random sRandom = new Random(1);
}
