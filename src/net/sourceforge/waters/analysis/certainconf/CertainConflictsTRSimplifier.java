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

package net.sourceforge.waters.analysis.certainconf;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.AbstractMarkingTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.MemStateProxy;
import net.sourceforge.waters.analysis.tr.OneEventCachingTransitionIterator;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class CertainConflictsTRSimplifier extends AbstractMarkingTRSimplifier {

  public CertainConflictsTRSimplifier()
  {
  }

  public CertainConflictsTRSimplifier(final ListBufferTransitionRelation rel)
  {
      super(rel);
  }

  @Override
  protected void setUp()  throws AnalysisException {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mOldRel = new ListBufferTransitionRelation(rel, rel.getConfiguration());
    final int numEvents = rel.getNumberOfProperEvents();
    int index = 0;
    for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
      if ((rel.getProperEventStatus(event) & EventStatus.STATUS_UNUSED) == 0) {
        index++;
      }
    }
    mEventIndexes = new int[index];
    index = 0;
    for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
      if ((rel.getProperEventStatus(event) & EventStatus.STATUS_UNUSED) == 0) {
        mEventIndexes[index++] = event;
      }
    }

    final int numStates = rel.getNumberOfStates();
    mSetOffsets = new TIntArrayList(numStates);
    mStateSetBuffer = new IntSetBuffer(numStates, 0, -1);
    mTransitionBuffer = new PreTransitionBuffer(numEvents, mTransitionLimit);
    mFindingCounterExample = false;
  }

  @Override
  protected void tearDown() {
    super.tearDown();
  }

  @Override
  public void reset()
  {
    super.reset();
    mSetOffsets = null;
    mStateSetBuffer = null;
    mTransitionBuffer = null;
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
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractMarkingTRSimplifier
  @Override
  public boolean isDumpStateAware()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier() throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mBadStates = new TIntHashSet();
    mNumBlockingStates = updateBadStates(rel, true, -1);

    if (mNumBlockingStates == 0) {
      return false;
    }
    if ((rel.getProperEventStatus(EventEncoding.TAU) &
         EventStatus.STATUS_UNUSED) == 0) {
      mIsDeterministic = false;
      final TauClosure closure = rel.createSuccessorsTauClosure(mTransitionLimit);
      mTauIterator = closure.createIterator();
      mPostEventIterator = closure.createPostEventClosureIterator(-1);
      mFullEventIterator = closure.createFullEventClosureIterator(-1);
    } else {
      mIsDeterministic = rel.isDeterministic();
      mTauIterator = null;
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      mPostEventIterator = new OneEventCachingTransitionIterator(iter);
      mFullEventIterator = mPostEventIterator;
    }

    // handle deterministic automata separately
    if (mIsDeterministic) {
      if (mNumBlockingStates == 1) {
        return false;
      }
      // run deterministic version of algorithm
      final int dumpstate = findDeterministicCertainConflicts(rel);
      // cleanup and return
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      if (dumpstate > -1) {
        rel.removeOutgoingTransitions(dumpstate);
      }
      rel.checkReachability();
      removeProperSelfLoopEvents();
      return true;
    }

    // Find marked states (considering tau)
    final int numStates = rel.getNumberOfStates();
    final int defaultID = getDefaultMarkingID();
    final TransitionIterator tauPredIter =
      rel.createPredecessorsReadOnlyIterator();
    tauPredIter.resetEvent(EventEncoding.TAU);
    mMarkedStates = new BitSet(numStates);
    final TIntStack stack = new TIntArrayStack(numStates);
    for (int state = 0; state < numStates; state++) {
      if (rel.isMarked(state, defaultID) && !mMarkedStates.get(state)) {
        mMarkedStates.set(state);
        stack.push(state);
        while (stack.size() > 0) {
          final int current = stack.pop();
          tauPredIter.resetState(current);
          while (tauPredIter.advance()) {
            final int pred = tauPredIter.getCurrentSourceState();
            if (!mMarkedStates.get(pred)) {
              mMarkedStates.set(pred);
              stack.push(pred);
            }
          }
        }
      }
    }

    // Collect initial state set.
    final TIntHashSet init = new TIntHashSet();
    for (int state = 0; state < numStates; state++) {
      if (rel.isInitial(state)) {
        if (mTauIterator == null) {
          init.add(state);
        } else {
          checkAbort();
          mTauIterator.resetState(state);
          while (mTauIterator.advance()) {
            final int tausucc = mTauIterator.getCurrentTargetState();
            init.add(tausucc);
          }
        }
      }
    }
    int last = 0;
    // convert to array so we can compute permutations
    final int[] arrInit = init.toArray();
    Arrays.sort(arrInit);
    if (!init.isEmpty()) {
      mNumInitialStates = arrInit.length;
      for (int i = 0; i < arrInit.length; i++) {
        arraySwap(arrInit, 0, i);
        final int offset = mStateSetBuffer.add(arrInit);
        mSetOffsets.add(offset);
        last = offset;
      }
    } else if (numStates == 0) {
      return false;
    }
    // Expand subset states.
    final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
    // TODO Change to TIntArrayList
    final ArrayList<Integer> current = new ArrayList<Integer>();

    for (int source = 0; source < mSetOffsets.size(); source++) {
      final int set = mSetOffsets.get(source);
      for (final int event : mEventIndexes) {
        checkAbort();
        // set iterators
        mPostEventIterator.resetEvent(event);
        iter.reset(set);
        // first state in set
        int firstStateInSet = -1;
        // for each state in current set
        while (iter.advance()) {
          final int state = iter.getCurrentData();
          if (firstStateInSet < 0) {
            firstStateInSet = state;
          }
          mPostEventIterator.resume(state);
          while (mPostEventIterator.advance()) {
            final int target = mPostEventIterator.getCurrentTargetState();
            current.add(target);
          }
        }
        if (current.isEmpty()) {
          continue;
        }
        mFullEventIterator.reset(firstStateInSet, event);
        while (mFullEventIterator.advance()) {
          final int leadingState = mFullEventIterator.getCurrentTargetState();
          // swap leading to the front
          Collections.swap(current, 0, current.indexOf(leadingState));
          // sort remainder of list and convert to array
          Collections.sort(current.subList(1, current.size()));
          final int[] acurrent = new int[current.size()];
          for (int i = 0; i < acurrent.length; i++) {
            acurrent[i] = current.get(i);
          }
          final int offset = mStateSetBuffer.add(acurrent);
          final int target;
          if (offset > last) {
            target = mSetOffsets.size();
            if (target >= mStateLimit) {
              throw new OverflowException(OverflowKind.STATE, mStateLimit);
            }
            mSetOffsets.add(offset);
            last = offset;
          } else {
            target = mSetOffsets.binarySearch(offset);
          }
          // add transitions for each permutation we just added
          mTransitionBuffer.addTransition(source, event, target);
        }
        current.clear();
      }
    }

    // Build new transition relation.
    applyResultPartitionAutomatically();
    return true;
  }

  @Override
  protected void applyResultPartition()
    throws AnalysisException
  {
    if (mSetOffsets != null) {
      ListBufferTransitionRelation rel = getTransitionRelation();
      final int numDetStates = mSetOffsets.size();
      final int numTrans = mTransitionBuffer.size();
      rel.reset(numDetStates, numTrans,
                ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      for (int init = 0; init < mNumInitialStates; init++) {
        rel.setInitial(init, true);
      }
      final int defaultID = getDefaultMarkingID();
      for (int p = 0; p < rel.getNumberOfPropositions(); p++) {
        rel.setPropositionUsed(p, p == defaultID);
      }
      final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
      for (int detstate = 0; detstate < numDetStates; detstate++) {
        final int offset = mSetOffsets.get(detstate);
        iter.reset(offset);
        iter.advance();
        final int state = iter.getCurrentData();
        rel.setMarked(detstate, defaultID, mMarkedStates.get(state));
      }
      mTransitionBuffer.addOutgoingTransitions(rel);
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);
      final int dumpstate = findCertainConflicts(rel);
      rel = getTransitionRelation();
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      if (dumpstate > -1) {
        rel.removeOutgoingTransitions(dumpstate);
      }
      rel.checkReachability();
      removeProperSelfLoopEvents();
      rel.removeTauSelfLoops();
    }
  }

  protected int findDeterministicCertainConflicts(final ListBufferTransitionRelation rel)
    throws AnalysisException
  {
    // mBadStates was already populated at beginning
    final int[] allBadStates = mBadStates.toArray();
    Arrays.sort(allBadStates);
    final int dumpstate = allBadStates[0];
    mBadStatesLevels = new int[rel.getNumberOfStates()];
    for (int i = 0; i < mBadStatesLevels.length; i++) {
      mBadStatesLevels[i] = -1;
    }
    for (final int i : allBadStates) {
      mBadStatesLevels[i] = 0;
    }
    for (int i = 0; i < mBadStates.size(); i++) {
      final TransitionIterator iter =
        rel.createPredecessorsModifyingIterator();
      final int s = allBadStates[i];
      iter.resetState(s);
      while (iter.advance()) {
        final int from = iter.getCurrentSourceState();
        if (i == 0 && !mBadStates.contains(from)) {
          continue;
        }
        if (!mBadStates.contains(from)) {
          rel.addTransition(from, iter.getCurrentEvent(), dumpstate);
          if (rel.isInitial(s)) {
            rel.setInitial(dumpstate, true);
            rel.setInitial(s, false); // its being set afterwards
          }
        }
        iter.remove();
      }
    }
    return dumpstate;
  }

  private int findCertainConflicts(ListBufferTransitionRelation rel)
    throws AnalysisException
  {
    // to determine when no new states have been added to bad states
    boolean brothersAdded = false;
    // keep track of bad states
    mBadStates = new TIntHashSet();
    // level of certain conflict for each bad state (initialise to -1)
    mBadStatesLevels = new int[rel.getNumberOfStates()];
    for (int i = 0; i < mBadStatesLevels.length; i++) {
      mBadStatesLevels[i] = -1;
    }

    int level = 0;
    do {
      // TODO These methods should return whether new bad states have been found.
      final int before = updateBadStates(rel, false, level++);
      if (before == 0) {
        return -1;
      }
      // find brothers of bad states (they'll be added to mBadStates)
      // and go again if new states were added.
      brothersAdded = findBrothers(level) != before;
    } while (brothersAdded);

    // select dump state as first item
    int[] allBadStates = mBadStates.toArray();
    mOptimisationUsed = false;

    if (checkAllBad() &&
        mOldRel.getNumberOfStates() < rel.getNumberOfStates() &&
        mOldRel.getNumberOfTransitions() < rel.getNumberOfTransitions()) {
      mOptimisationUsed = true;
      // stop here if re-running for counterexample (only need to know that optimisation was used)
      if (mFindingCounterExample) {
        return -1;
      }
      // replace rel with oldrel
      setTransitionRelation(mOldRel);
      rel = getTransitionRelation();
      mBadStates = new TIntHashSet();
      // replace bad states with states the original automaton knows about
      for (int i = 0; i < allBadStates.length; i++) {
        final int offset = mSetOffsets.get(allBadStates[i]);
        mBadStates.add(mStateSetBuffer.getSetContents(offset)[0]);
      }
      allBadStates = mBadStates.toArray();
    }
    Arrays.sort(allBadStates);
    if (mFindingCounterExample) {
      return -1;
    }

    final int dumpstate = allBadStates[0];
    rel.setMarked(dumpstate, getDefaultMarkingID(), false);

    // redirect all bad states to the dump state
    for (int i = 0; i < mBadStates.size(); i++) {
      final TransitionIterator iter =
        rel.createPredecessorsModifyingIterator();
      final int badState = allBadStates[i];
      iter.resetState(badState);
      if (rel.isInitial(badState)) {
        rel.setInitial(badState, false);
        rel.setInitial(dumpstate, true);
      }
      while (iter.advance()) {
        final int from = iter.getCurrentSourceState();
        if (i == 0 && !mBadStates.contains(from)) {
          continue;
        }
        if (!mBadStates.contains(from)) {
          rel.addTransition(from, iter.getCurrentEvent(), dumpstate);
        }
        iter.remove();
      }
    }
    return dumpstate;
  }

  private boolean checkAllBad()
  {
    // TODO  Reduce complexity. Presently O(n^4) ...
    boolean returnVal = true;
    mTotalBadStates = new TIntHashSet();
    final int[] allBadStates = mBadStates.toArray();
    for (int i = 0; i < mBadStates.size(); i++) {
      final int badSetoffset = mSetOffsets.get(allBadStates[i]);
      final int[] badSet = mStateSetBuffer.getSetContents(badSetoffset);
      final int originalBad = badSet[0];
      for (int j = 0; j < mStateSetBuffer.size(); j++) {
        final int testoffset = mSetOffsets.get(j);
        final int[] test = mStateSetBuffer.getSetContents(testoffset);
        final int nTest = test[0];
        if (nTest == originalBad) {
          if (!mBadStates.contains(j)) {
            returnVal = false;
          }
        }
      }
      if (!returnVal) {
        return false;
      } else {
        mTotalBadStates.add(i);
      }
    }
    return returnVal;
  }

  private int findBrothers(final int level)
  {
    final int[] arr_mBadStates = mBadStates.toArray();
    for (int i = 0; i < arr_mBadStates.length; i++) {
      final int currentbadoffset = mSetOffsets.get(arr_mBadStates[i]);
      final int[] badStateSet = mStateSetBuffer.getSetContents(currentbadoffset);
      // now permute badStateSet
      // first order it, but remember what was originally first,
      // no need to do that one again
      for (int j = 1; j < badStateSet.length; j++) {
        if (badStateSet[j] < badStateSet[j - 1]) {
          arraySwap(badStateSet, j, j - 1);
        } else {
          break;
        }
      }
      // now permute
      for (int j = 0; j < badStateSet.length; j++) {
        arraySwap(badStateSet, 0, j);
        final int testinbuffer = mStateSetBuffer.get(badStateSet);
        if (testinbuffer > -1) {
          final int stateNum = mSetOffsets.binarySearch(testinbuffer);
          if (mBadStates.add(stateNum)) {
            mBadStatesLevels[stateNum] = level;
          }
        }
      }
    }
    return mBadStates.size();
  }

  private void arraySwap(final int[] arr, final int swap1, final int swap2)
  {
    if (swap1 != swap2) {
      final int temp = arr[swap1];
      arr[swap1] = arr[swap2];
      arr[swap2] = temp;
    }
  }

  /**
   * Updates the values in the variable {@link #mBadStates}.
   *
   * @return The number of states in mBadStates.
   */
  private int updateBadStates(final ListBufferTransitionRelation rel,
                              final boolean intial, final int level)
    throws AnalysisException
  {
    final TransitionIterator prediter =
      rel.createPredecessorsReadOnlyIterator();
    final int defaultID = getDefaultMarkingID();
    final int numStates = rel.getNumberOfStates();
    final TIntHashSet coreachableStates = new TIntHashSet(numStates);
    final TIntStack unvisitedStates = new TIntArrayStack();
    // Creates a hash set of all states which can reach a marked state.
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (rel.isMarked(sourceID, defaultID) &&
          rel.isReachable(sourceID) &&
          !mBadStates.contains(sourceID) &&
          coreachableStates.add(sourceID)) {
        checkAbort();
        unvisitedStates.push(sourceID);
        while (unvisitedStates.size() > 0) {
          final int newSource = unvisitedStates.pop();
          prediter.resetState(newSource);
          while (prediter.advance()) {
            final int predID = prediter.getCurrentSourceState();
            if (rel.isReachable(predID) &&
                !mBadStates.contains(predID) &&
                coreachableStates.add(predID)) {
              unvisitedStates.push(predID);
            }
          }
        }
      }
    }
    // Blacklist states which cannot reach a marked state.
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (rel.isReachable(sourceID) && !coreachableStates.contains(sourceID)) {
        mBadStates.add(sourceID);
        // level == -1 if initial badstates check
        if (level > -1 && mBadStatesLevels[sourceID] == -1) {
          mBadStatesLevels[sourceID] = level;
        }
        // remove marking
        if (!intial && mFindingCounterExample) {
          rel.setMarked(sourceID, defaultID, false);
        }
      }
    }
    return mBadStates.size();
  }

    public int[] runForCE() throws AnalysisException
    {
      setUp();
      mFindingCounterExample = true;
      runSimplifier();
      return mBadStatesLevels;
    }

    public AutomatonProxy createTestAutomaton(final ProductDESProxyFactory factory,
                                              final EventEncoding eventEnc,
                                              final StateEncoding testAutomatonStateEncoding,
                                              final int initTest,
                                              final EventProxy checkedProposition,
                                              final int level)
    {

      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numEvents = eventEnc.getNumberOfEvents();
      final Collection<EventProxy> events = new ArrayList<EventProxy>(numEvents);
      for (int e = 0; e < eventEnc.getNumberOfProperEvents(); e++) {
        if ((rel.getProperEventStatus(e) & EventStatus.STATUS_UNUSED) == 0) {
          final EventProxy event = eventEnc.getProperEvent(e);
          if (event != null) {
            events.add(event);
          }
        }
      }
      events.add(checkedProposition);
      final int numStates = rel.getNumberOfStates();
      int numReachable = 0;
      int numCritical = 0;
      for (int state = 0; state < numStates; state++) {
        if (rel.isReachable(state)) {
          numReachable++;
          if (mBadStatesLevels[state] <= level && mBadStatesLevels[state] > -1) {
            numCritical++;
          }
        }
      }
      final StateProxy[] states = new StateProxy[numStates];
      final List<StateProxy> reachable = new ArrayList<StateProxy>(numReachable);
      final int numTrans = rel.getNumberOfTransitions();
      final Collection<TransitionProxy> transitions =
        new ArrayList<TransitionProxy>(numTrans + numCritical);
      int code = 0;
      for (int state = 0; state < numStates; state++) {
        if (rel.isReachable(state)) {
          final boolean init =
            initTest >= 0 ? state == initTest : rel.isInitial(state);
          final StateProxy memstate = new MemStateProxy(code++, init);
          states[state] = memstate;
          reachable.add(memstate);
          final int info = mBadStatesLevels[state];
          if (info != -1 && info <= level) {
            final TransitionProxy trans =
              factory.createTransitionProxy(memstate, checkedProposition, memstate);
            transitions.add(trans);
          }
        }
      }
      testAutomatonStateEncoding.init(states);
      final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
      while (iter.advance()) {
        final int s = iter.getCurrentSourceState();
        if (rel.isReachable(s)) {
          final int t = iter.getCurrentTargetState();
          final StateProxy source = states[s];
          final int e = iter.getCurrentEvent();
          final EventProxy event = eventEnc.getProperEvent(e);
          final StateProxy target = states[t];
          final TransitionProxy trans =
            factory.createTransitionProxy(source, event, target);
          transitions.add(trans);
        }
      }
      final String name = rel.getName() + ":certainconf:" + level;
      final ComponentKind kind = ComponentKind.PLANT;
      return factory.createAutomatonProxy(name, kind,
                                          events, reachable, transitions);
    }

    public int[] getStateSet(final int state)
    {
      int[] set;
      if (mIsDeterministic)
      {
        set = new int[1];
        set[0] = state;
      } else {
        final int offset = mSetOffsets.get(state);
        set = mStateSetBuffer.getSetContents(offset);
      }

      return set;
    }

    public ArrayList<Integer> getStateSetArrayList(final int state)
    {
        final ArrayList<Integer> ali = new ArrayList<Integer>();
        final int[] set = getStateSet(state);
        for (int i = 0; i < set.length; i++)
        {
          ali.add(set[i]);
        }
        return ali;
    }

    public int findStateFromSet(final int[] stateSet)
    {
      if (mIsDeterministic)
      {
        if (stateSet.length > 1) return -1;
        return stateSet[0];
      } else {
        final int testinbuffer = mStateSetBuffer.get(stateSet);
        if (testinbuffer == -1) return -1;
        return mSetOffsets.binarySearch(testinbuffer);
      }
    }

    public boolean getWasOptimisationUsed()
    {
      return mOptimisationUsed;
    }


    //#########################################################################
    //# Debugging
    @SuppressWarnings("unused")
    private String dumpStateSets()
    {
      if (mSetOffsets == null) {
        return "mSetOffsets == null";
      } else if (mStateSetBuffer == null) {
        return "mStateSetBuffer == null";
      } else {
        final StringWriter writer = new StringWriter();
        final PrintWriter printer = new PrintWriter(writer);
        for (int i = 0; i < mSetOffsets.size(); i++) {
          final int set = mSetOffsets.get(i);
          mStateSetBuffer.dump(printer, set);
          printer.println();
        }
        printer.close();
        return writer.toString();
      }
    }


    //#########################################################################
    //# Data Members
    private final int mStateLimit = Integer.MAX_VALUE;
    private final int mTransitionLimit = Integer.MAX_VALUE;

    private boolean mIsDeterministic;
    private int[] mEventIndexes;
    private TransitionIterator mTauIterator;
    private TransitionIterator mPostEventIterator;
    private TransitionIterator mFullEventIterator;
    private BitSet mMarkedStates;
    private TIntArrayList mSetOffsets;
    private TIntHashSet mBadStates;
    private int[] mBadStatesLevels;
    private IntSetBuffer mStateSetBuffer;
    private PreTransitionBuffer mTransitionBuffer;
    private ListBufferTransitionRelation mOldRel;
    private int mNumInitialStates; // number of initial states
    private boolean mFindingCounterExample;
    private boolean mOptimisationUsed;
    private int mNumBlockingStates;
    private TIntHashSet mTotalBadStates;

}
