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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

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

    mDumpStateIndex = rel.getDumpStateIndex();
    final int supervisedEvent = getSupervisedEvent();

    mNumStates = rel.getNumberOfStates();
    mNumProperEvents = rel.getNumberOfProperEvents();
    final StateOutput[] stateOutputs = new StateOutput[mNumStates];

    mInitialCompatible = new TIntArrayList();
    mReducedSupervisor = new ParetoCompatibleSet();
    mCompatibleCache = new IntSetBuffer(mNumStates);
    //mCoversCache = new TIntObjectHashMap<>();

    for (int s = 0; s < mNumStates; s++) {
      //set the reduced supervisor to the current set of states
      final TIntArrayList oneStateCompatible = new TIntArrayList();
      oneStateCompatible.add(s);
      mReducedSupervisor.add(mCompatibleCache.add(oneStateCompatible));

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
    final TIntCollection initialCompatibleIds = getCoversOf(mInitialCompatible);
    for (final TIntIterator compatibleIdIterator = initialCompatibleIds.iterator(); compatibleIdIterator.hasNext();) {
      final ParetoCompatibleSet dependencies = new ParetoCompatibleSet();
      dependencies.add(compatibleIdIterator.next());
      reduce(new ParetoCompatibleSet(), dependencies);
    }

    //account for the dump state which is included in the initial automaton but not in our compatibles
    if (mReducedSupervisor.size() + 1 >= mNumStates) {
      return false;
    }

    else {
      //we managed to reduce the supervisor
       return buildReducedSupervisor();
    }
  }

  private void reduce(final ParetoCompatibleSet currentSolution, final ParetoCompatibleSet compatibleDependencies) {

    //we can assume our solution is not worst than the current best if we get here
    if (compatibleDependencies.isEmpty()) {
    //if we got here we know our solution is better than the current best
      mReducedSupervisor = currentSolution;
      return;
    }

    //our container for the actual compatible being processed at any given time
    final TIntArrayList compatible = new TIntArrayList();


    //get the id of next compatible we need to add
    final int nextCompatibleId = compatibleDependencies.pop();

    //get all ids of compatibles covering this compatible
    /*TIntCollection coverIds = mCoversCache.get(nextCompatibleId);
    if (coverIds == null) {
      getCompatibleFromCache(nextCompatibleId, compatible);
      coverIds = getCoversOf(compatible);
      mCoversCache.put(nextCompatibleId, coverIds);
    }*/
    getCompatibleFromCache(nextCompatibleId, compatible);
    final TIntCollection coverIds = getCoversOf(compatible);

    //container for successor compatible ids
    final TIntCollection dependentIds = new TIntArrayList();

    for (final TIntIterator coversIterator = coverIds.iterator(); coversIterator.hasNext();) {
      //get the id of a cover
      final int coverId = coversIterator.next();

      //clear containers
      dependentIds.clear();
      compatible.clear();

      //get the actual compatible for the cover id
      getCompatibleFromCache(coverId, compatible);

      //get the compatible ids of its successors
      getSuccessorCompatiblesOf(compatible, dependentIds);

      //copy the current solution and add the cover
      final ParetoCompatibleSet newSolution = new ParetoCompatibleSet(currentSolution);
      newSolution.add(coverId);

      //copy the things we know we need to add, but remove anything covered by the compatible we just added to our solution
      final ParetoCompatibleSet newCompatibleDependencies = new ParetoCompatibleSet(compatibleDependencies);
      newCompatibleDependencies.removeAllDominatedBy(coverId);

      //now go through all the successors of our cover - we need to all of these (or covers of) in our final solution
      for (final TIntIterator dependentsIterator = dependentIds.iterator(); dependentsIterator.hasNext();) {
        //a successor compatible id
        final int dependentId = dependentsIterator.next();

        //if this compatible isn't a subset of anything in our current (and partial solution)
        if (newSolution.undominatedByAll(dependentId)) {
          //try adding it to the list of compatibles we know we still need to cover
          //it may not actually be added if it is already covered by an existing member
          newCompatibleDependencies.add(dependentId);
        }
      }

      if (newSolution.size() + newCompatibleDependencies.size() < mReducedSupervisor.size()) {
        reduce(newSolution, newCompatibleDependencies);
      }
    }
  }

  private boolean buildReducedSupervisor() throws OverflowException {
    //translate set of compatible ids which comprise the reduced supervisor into the compatibles themselves
    final TIntSet[] reducedSupervisor = new TIntHashSet[mReducedSupervisor.size()];
    int i = 0;
    for (final Iterator<Integer> compatibleIdIterator = mReducedSupervisor.iterator(); compatibleIdIterator.hasNext(); i++) {
      reducedSupervisor[i] = new TIntHashSet();
      getCompatibleFromCache(compatibleIdIterator.next(), reducedSupervisor[i]);
    }

    //we have the set of compatibles, now we need to construct a new automaton based off them
    //find a compatible containing the initial set of states (it may could be a cover if a larger clique was found)
    int startStateIndexOffset = 0;
    for (; startStateIndexOffset < reducedSupervisor.length; startStateIndexOffset++) {
      final TIntSet currentCompatible = reducedSupervisor[startStateIndexOffset];
      if (currentCompatible.containsAll(mInitialCompatible)) { break; }
    }

    //we must now go through each compatible (which is to become a state in the reduced automaton) and determine its successors
    //in doing so, we build up a transition relation. We will use the index of the compatible in mReducedSupervisor to denote its index in the transition buffer
    //however we want the initial state to have index 0 so we will offset all values by that amount
    final PreTransitionBuffer transitionBuffer = new PreTransitionBuffer(mNumProperEvents);
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
    final ListBufferTransitionRelation relation = getTransitionRelation();
    final AbstractStateBuffer oldStateBuffer = relation.getStateBuffer();

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

      for (final TIntIterator stateIterator = compatible.iterator(); stateIterator.hasNext();) {
        final long oldStateMarkings = oldStateBuffer.getAllMarkings(stateIterator.next());
        newStateMarkings = relation.mergeMarkings(newStateMarkings, oldStateMarkings);
      }
      relation.setAllMarkings(state, newStateMarkings);
    }

    return true;
  }


  private Collection<TIntArrayList> BronKerbosch(final TIntArrayList clique, final TIntCollection possibleAdditions, final TIntCollection alreadyChecked) {
    final Collection<TIntArrayList> maximalCliques = new ArrayList<TIntArrayList>();
    BronKerbosch(clique, possibleAdditions, alreadyChecked, maximalCliques);
    return maximalCliques;
  }

  private void BronKerbosch(final TIntArrayList clique, final TIntCollection possibleAdditions, final TIntCollection alreadyChecked, final Collection<TIntArrayList> maximalCliquesToFill) {

    //if we have exhausted all possibilities, this must be the largest clique we have seen
    if (possibleAdditions.isEmpty() && alreadyChecked.isEmpty()) {
      maximalCliquesToFill.add(clique);
    }

    //go through each state we haven't tried adding yet
    for (final TIntIterator inclusionIterator = possibleAdditions.iterator(); inclusionIterator.hasNext();) {
      final int candidateState = inclusionIterator.next();

      //create a copy with the new vertex
      final TIntArrayList newClique = new TIntArrayList(clique);
      newClique.add(candidateState);

      //create a copy with a restricted set of neighbours: they have to also be neighbours of the state we are adding
      final TIntCollection newPossibleInclusions = new TIntArrayList();
      for (final TIntIterator inclusionsIterator = possibleAdditions.iterator(); inclusionsIterator.hasNext();) {
        final int oldInclusion = inclusionsIterator.next();
        if (isNeighbour(candidateState, oldInclusion)) {
          newPossibleInclusions.add(oldInclusion);
        }
      }

      //copy the list of states that have been already checked
      final TIntCollection newAlreadyChecked = new TIntArrayList(alreadyChecked);

      //find any maximal cliques based on newCliques and add them to the object referenced by cliques
      BronKerbosch(newClique, newPossibleInclusions, newAlreadyChecked, maximalCliquesToFill);

      //remove the current candidate state from further consideration
      inclusionIterator.remove();

      //add to the list of states we have already checked
      alreadyChecked.add(candidateState);
    }
  }

  private TIntCollection getCoversOf(final TIntArrayList compatible) {


    final TIntCollection possibleAdditions = new TIntArrayList();
    if (compatible.size() > 0) {

      //we need to figure out the initial valid set of states that could be included to our compatible
      //at most, our possible additions will be the set of neighbours to the first state in the compatible
      final TIntCollection neighbours = new TIntArrayList();
      getNeighboursOf(compatible.get(0), neighbours);

      //then we go through each of these neighbours and cull ones that aren't mutually neighbours with the rest
      //of the states in our compatible
      for (final TIntIterator neighbourIterator = neighbours.iterator(); neighbourIterator.hasNext();) {
        final int neighbour = neighbourIterator.next();

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
    final Collection<TIntArrayList> covers = BronKerbosch(compatible, possibleAdditions, new TIntArrayList());

    //init the list of ints that represent ids in the compatible cache to the actual compatible covers.
    final TIntCollection compatibleIds = new TIntArrayList(covers.size());

    //we then need to record each compatible cover we have seen in our cache for future retrieval.
    //to add them to the cache we need to sort the states in each compatible.
    for (final TIntArrayList compatibleCover : covers) {
      compatibleCover.sort();
      compatibleIds.add(mCompatibleCache.add(compatibleCover));
    }
    return compatibleIds;
  }

  private void getCompatibleFromCache(final int compatibleId, final TIntCollection compatibleToFill) {
    compatibleToFill.addAll(mCompatibleCache.getSetContents(compatibleId));
  }

  private boolean isNeighbour(final int state1, final int state2) {
    return !mIncompatibilityRelation[state1][state2] && state1 != state2;
  }

  private void getNeighboursOf(final int state, final TIntCollection neighboursToFill) {
    for (int s = 0; s < mNumStates; s++) {
      //if the two states are compatible and not the same state, add to neighbours
      if (isNeighbour(state, s)) {
        neighboursToFill.add(s);
      }
    }
  }

  private void getSuccessorCompatiblesOf(final TIntArrayList compatible, final TIntCollection successorsToFill) {
    final TIntSet enabledEvents = new TIntHashSet();
    getEnabledEventsOf(compatible, enabledEvents);

    //for each event, we see what compatible is generated by taking that event from each state in the original compatible
    for (final TIntIterator eventIterator = enabledEvents.iterator(); eventIterator.hasNext();) {
      final int event = eventIterator.next();
      final TIntSet successorCompatible = new TIntHashSet();

      //for each state in original compatible, retrieve the target state which forms part of a compatible
      for (int s = 0; s < compatible.size(); s++) {
        final int targetState = getSuccessorState(compatible.get(s), event);
        //if the target exists
        if (targetState != -1) {
          successorCompatible.add(targetState);
        }
      }

      //we now have our successor compatible, but we must document this compatible in our cache.
      //we need to insert into the cache sorted
      final TIntArrayList sortedSuccessor = new TIntArrayList(successorCompatible);
      sortedSuccessor.sort();

      //add the id associated with this compatible to the set of successor compatible ids
      successorsToFill.add(mCompatibleCache.add(sortedSuccessor));
    }
  }

  private void getEnabledEventsOf(final TIntCollection compatible, final TIntCollection eventSetToFill) {
    //get the union of enabled events across states in the compatible
    final TIntList successorEvents = new TIntArrayList();
    for (final TIntIterator compatibleIterator = compatible.iterator(); compatibleIterator.hasNext();) {
      successorEvents.clear();
      getSuccessorEvents(compatibleIterator.next(), successorEvents);
      eventSetToFill.addAll(successorEvents);
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

  private void getSuccessorEvents(final int source, final TIntCollection successorEventsToFill) {
    final TransitionIterator successorIterator = getTransitionRelation().createSuccessorsReadOnlyIterator();
    successorIterator.resetState(source);

    while (successorIterator.advance()) {
      successorEventsToFill.add(successorIterator.getCurrentEvent());
    }
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
  private class ParetoCompatibleSet extends LinkedHashSet<Integer> {

    private static final long serialVersionUID = -4434294284951063515L;

    public ParetoCompatibleSet() {
      super();
    }

    public ParetoCompatibleSet(final HashSet<Integer> compatibleIdSet) {
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
     * is a subset ('dominated') of any compatibles referenced by existing ids in the set.
     * If and only if this condition is met, the id is added to the set.
     * Then, any compatibles referenced by the set that dominated by the newly
     * added member are then removed.
     *
     * @param applicantId the unique id corresponding to a compatible stored in an IntSetBuffer
     * @return true if and only if the compatible referenced by applicantId is not dominated by existing members
     */
    @Override
    public boolean add(final Integer applicantId) {
      if (size() == 0) {
        return super.add(applicantId);
      }

      //check if the applicant is a strict subset of any existing members
      if (!undominatedByAll(applicantId)) { return false; }

      //not dominated by any existing members, we want to add it.
      //now kick out any members the applicant dominates
      removeAllDominatedBy(applicantId);

      return super.add(applicantId);
    }

    /**
     * Checks whether the compatible referenced by xId is a subset of (dominated by) any compatibles already
     * referenced by ids in the set.
     * @param xId The id of a compatible
     * @return true if and only if the compatible is not dominated by any existing compatibles.
     */
    public boolean undominatedByAll(final int xId) {
      for (final Iterator<Integer> compatibleIdsIterator = iterator(); compatibleIdsIterator.hasNext();) {
        if (dominates(compatibleIdsIterator.next(), xId)) { return false; }
      }
      return true;
    }

    /**
     * Removes any compatibles in this set that are subsets of the compatible referenced by xId.
     * @param xId The id of a compatible
     */
    public void removeAllDominatedBy(final int xId) {
      for (final Iterator<Integer> compatibleIdsIterator = iterator(); compatibleIdsIterator.hasNext();) {
        if (dominates(xId, compatibleIdsIterator.next())) { compatibleIdsIterator.remove(); }
      }
    }

    private boolean dominates(final int xId, final int yId) {
      return mCompatibleCache.containsAll(xId, yId);
    }


    @Override
    public String toString()
    {
      final Integer[] compatibleIdBuffer = new Integer[size()];
      this.toArray(compatibleIdBuffer);
      final TIntArrayList[] compatibleBuffer = new TIntArrayList[size()];
      for (int i = 0; i < compatibleIdBuffer.length; i++) {
        compatibleBuffer[i] = new TIntArrayList();
        getCompatibleFromCache(compatibleIdBuffer[i], compatibleBuffer[i]);
      }
      return Arrays.toString(compatibleBuffer);
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
      if (!(o instanceof ParetoCompatibleSet)) {
        return false;
      }

      final ParetoCompatibleSet otherSet = (ParetoCompatibleSet)o;
      if (size() != otherSet.size()) {
        return false;
      }

      return containsAll(otherSet);
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
  private int mNumStates;
  private boolean[][] mIncompatibilityRelation;
  private TIntArrayList mInitialCompatible;
  private IntSetBuffer mCompatibleCache;
  //private TIntObjectHashMap<TIntCollection> mCoversCache;
  private ParetoCompatibleSet mReducedSupervisor;
}
