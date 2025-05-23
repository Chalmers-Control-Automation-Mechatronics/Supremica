//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.SpecialEventsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SuWonhamSupervisorReductionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SubsetConstructionTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntArrayHashingStrategy;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRSynchronousProductStateMap;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersHashSet;
import net.sourceforge.waters.model.analysis.AbortRequester;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.ProxyResult;
import net.sourceforge.waters.model.analysis.des.AbstractSupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.NondeterministicDESException;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The monolithic synthesis algorithm.
 *
 * This algorithm computes the least restrictive controllable and nonblocking
 * sublanguage for a given {@link ProductDESProxy}, and returns the result in
 * the form of a single automaton.
 *
 * @author Fangqian Qiu, Robi Malik
 */

public class MonolithicSynthesizerNormality
  extends AbstractSupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  public MonolithicSynthesizerNormality(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public MonolithicSynthesizerNormality(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public MonolithicSynthesizerNormality(final ProductDESProxy model,
                               final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort(final AbortRequester sender)
  {
    super.requestAbort(sender);
    if (mSupervisorSimplifier != null) {
      mSupervisorSimplifier.requestAbort(sender);
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mSupervisorSimplifier != null) {
      mSupervisorSimplifier.resetAbort();
    }
  }


  //#########################################################################
  //# Specific Access
  public Collection<EventProxy> getDisabledEvents()
  {
    return mDisabledEvents;
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected void setUp() throws AnalysisException
  {
    int[][][][] transitions;
    int[][][][] reverseTransitions;
    int[][] eventAutomata;
    int[][] reverseEventAutomata;
    int[][] ndTuple1;
    int[][] ndTuple2;
    int[][] ndTuple3;

    super.setUp();

    final ProductDESProxy model = getModel();

    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> events = model.getEvents();
    final EventProxy marking = getUsedDefaultMarking();
    final Collection<EventProxy> filter = Collections.singleton(marking);
    mEventEncoding =
      new EventEncoding(events, translator, filter,
                        EventEncoding.FILTER_PROPOSITIONS);
    final EventProxy tau =
      getFactory().createEventProxy(":tau", EventKind.UNCONTROLLABLE, false);
    mEventEncoding.addSilentEvent(tau);
    mEventEncoding.sortProperEvents(EventStatus.STATUS_CONTROLLABLE);
    mNumProperEvents = mEventEncoding.getNumberOfProperEvents();

    //Set all unobservable events as local. Used for normality checking further on
    for(int e = EventEncoding.NONTAU; e < mNumProperEvents; e++){
     final EventProxy event = mEventEncoding.getProperEvent(e);
     if(!event.isObservable()){
       byte status = mEventEncoding.getProperEventStatus(e);
       status |= EventStatus.STATUS_LOCAL;
       mEventEncoding.setProperEventStatus(e, status);
     }
    }

    ArrayList<AutomatonProxy> plants = new ArrayList<AutomatonProxy>();
    ArrayList<AutomatonProxy> specs = new ArrayList<AutomatonProxy>();
    for (final AutomatonProxy aut : model.getAutomata()) {
      if (translator.getComponentKind(aut) == ComponentKind.PLANT) {
        plants.add(aut);
      } else if (translator.getComponentKind(aut) == ComponentKind.SPEC) {
        specs.add(aut);
      }
    }
    mNumAutomata = plants.size() + specs.size();
    mNumPlants = plants.size();
    mAutomata = new ArrayList<AutomatonProxy>(mNumAutomata);
    mAutomata.addAll(plants);
    mAutomata.addAll(specs);
    plants = specs = null;

    mOriginalStates = new StateProxy[mNumAutomata][];
    mStateMarkings = new boolean[mNumAutomata][];
    mTargetTuple = new int[mNumAutomata];

    //transitions indexed first by automaton then by event then by source state
    transitions = new int[mNumAutomata][mNumProperEvents + 1][][];
    reverseTransitions = new int[mNumAutomata][mNumProperEvents + 1][][];
    eventAutomata = new int[mNumProperEvents + 1][];
    reverseEventAutomata = new int[mNumProperEvents + 1][];
    ndTuple1 = new int[mNumAutomata][];

    int a = 0;
    for (final AutomatonProxy aut : mAutomata) {
      final Collection<EventProxy> localEvents = aut.getEvents();
      final Collection<StateProxy> states = aut.getStates();
      final int numStates = states.size();
      final TObjectIntHashMap<StateProxy> stateToIndex =
        new TObjectIntHashMap<StateProxy>(numStates);
      final TIntArrayList initials = new TIntArrayList(1);
      int snum = 0;
      mOriginalStates[a] = new StateProxy[numStates];
      mStateMarkings[a] = new boolean[numStates];
      if (!localEvents.contains(marking)) {
        Arrays.fill(mStateMarkings[a], true);
      }
      for (final StateProxy state : states) {
        stateToIndex.put(state, snum);
        mOriginalStates[a][snum] = state;
        if (state.isInitial()) {
          if (!initials.isEmpty() && !supportsNondeterminism()) {
            throw new NondeterministicDESException(aut, state);
          }
          initials.add(snum);
        }
        final Collection<EventProxy> props = state.getPropositions();
        if (props.contains(marking)) {
          mStateMarkings[a][snum] = true;
        }
        snum++;
      }
      ndTuple1[a] = initials.toArray();
      final TIntArrayList[][] autTransitionLists =
        new TIntArrayList[mNumProperEvents + 1][numStates];
      final TIntArrayList[][] autTransitionListsRvs =
        new TIntArrayList[mNumProperEvents + 1][numStates];
      for (final TransitionProxy trans : aut.getTransitions()) {
        final EventProxy event = trans.getEvent();
        final int e = mEventEncoding.getEventCode(event);
        final StateProxy source = trans.getSource();
        final int s = stateToIndex.get(source);
        final StateProxy target = trans.getTarget();
        final int t = stateToIndex.get(target);
        TIntArrayList list = autTransitionLists[e][s];
        TIntArrayList listRvs = autTransitionListsRvs[e][t];
        if (list == null) {
          list = new TIntArrayList(1);
          autTransitionLists[e][s] = list;
        } else if (!supportsNondeterminism()) {
          throw new NondeterministicDESException(aut, source, event);
        }
        list.add(t);
        if (listRvs == null) {
          listRvs = new TIntArrayList(1);
          autTransitionListsRvs[e][t] = listRvs;
        }
        listRvs.add(s);
      }
      for (final EventProxy event : localEvents) {
        if (translator.getEventKind(event) != EventKind.PROPOSITION) {
          final int e = mEventEncoding.getEventCode(event);
          transitions[a][e] = new int[numStates][];
          reverseTransitions[a][e] = new int[numStates][];
          for (int source = 0; source < numStates; source++) {
            final TIntArrayList list = autTransitionLists[e][source];
            if (list != null) {
              transitions[a][e][source] = list.toArray();
            }
          }
          for (int target = 0; target < numStates; target++) {
            final TIntArrayList listRvs = autTransitionListsRvs[e][target];
            if (listRvs != null) {
              reverseTransitions[a][e][target] = listRvs.toArray();
            }
          }
        }
      }
      a++;
    }
    eventAutomata = new int[mNumProperEvents][];
    reverseEventAutomata = new int[mNumProperEvents][];
    final List<AutomatonEventInfo> list =
      new ArrayList<AutomatonEventInfo>(mNumAutomata);
    final List<AutomatonEventInfo> listRvs =
      new ArrayList<AutomatonEventInfo>(mNumAutomata);
    for (int e = EventEncoding.NONTAU; e < mNumProperEvents; e++) {
      final byte status = mEventEncoding.getProperEventStatus(e);
      final boolean controllable = EventStatus.isControllableEvent(status);
      for (a = 0; a < mNumAutomata; a++) {
        if (transitions[a][e] != null) {
          final int numStates = transitions[a][e].length;
          int count = 0;
          for (int source = 0; source < numStates; source++) {
            if (transitions[a][e][source] != null) {
              count++;
            }
          }
          final double avg = (double) count / (double) numStates;
          if (controllable) {
            final ControllableAutomatonEventInfo pair =
              new ControllableAutomatonEventInfo(a, avg);
            list.add(pair);
          } else {
            final UncontrollableAutomatonEventInfo pair =
              new UncontrollableAutomatonEventInfo(a, avg);
            list.add(pair);
          }
        }
        if (reverseTransitions[a][e] != null) {
          final int numStates = reverseTransitions[a][e].length;
          int countRvs = 0;
          for (int target = 0; target < numStates; target++) {
            if (reverseTransitions[a][e][target] != null) {
              countRvs++;
            }
          }
          final double avgRvs = (double) countRvs / (double) numStates;
          final ControllableAutomatonEventInfo pairRvs =
            new ControllableAutomatonEventInfo(a, avgRvs);
          listRvs.add(pairRvs);
        }
      }
      Collections.sort(list);
      Collections.sort(listRvs);
      final int count = list.size();
      final int countRvs = listRvs.size();
      eventAutomata[e] = new int[count];
      reverseEventAutomata[e] = new int[countRvs];
      int i = 0;
      for (final AutomatonEventInfo info : list) {
        eventAutomata[e][i++] = info.getAutomaton();
      }
      int j = 0;
      for (final AutomatonEventInfo info : listRvs) {
        reverseEventAutomata[e][j++] = info.getAutomaton();
      }
      list.clear();
      listRvs.clear();
    }

    final int tableSize = Math.min(getNodeLimit(), MAX_TABLE_SIZE);
    mNonCoreachableStates = new BitSet(mNumStates);
    mReachableStates = new BitSet(mNumStates);
    mBadStates = new BitSet(mNumStates);
    mSafeStates = new BitSet(mNumStates);
    final IntArrayHashingStrategy strategy = new IntArrayHashingStrategy();
    mGlobalVisited =
      new TObjectIntCustomHashMap<int[]>(strategy, tableSize, 0.5f, -1);
    mLocalVisited = new WatersHashSet<int[]>(strategy);
    mGlobalStack = new ArrayDeque<int[]>();
    mLocalStack = new ArrayDeque<int[]>();
    mBackTrace = new ArrayDeque<int[]>();
    mStateTuples = new ArrayList<int[]>();
    mTransitionBuffer = new TIntArrayList();
    mUnvisited = new ArrayDeque<int[]>(100);
    mNumStates = 0;

    final int[] sizes = StateTupleEncoding.getAutomataSizes(mAutomata);
    mSTEncoding = new StateTupleEncoding(sizes);

    // We have sorted the events so all uncontrollables come first.
    final int numEvents = mEventEncoding.getNumberOfProperEvents();
    int firstControllable = numEvents;
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = mEventEncoding.getProperEventStatus(e);
      if (EventStatus.isControllableEvent(status)) {
        firstControllable = e;
        break;
      }
    }
    final int lastUncontrollable = firstControllable - 1;
    mCtrlInitialReachabilityExplorer =
      new CtrlInitialReachabilityExplorer(eventAutomata, transitions,
                                          ndTuple1, firstControllable,
                                          mNumProperEvents - 1);
    mInitialIsBad = false;
    mCtrlInitialReachabilityExplorer.permutations(mNumAutomata, null, -1);
    ndTuple2 = Arrays.copyOf(ndTuple1, mNumAutomata);
    ndTuple3 = Arrays.copyOf(ndTuple1, mNumAutomata);
    mNumInitialStates = mNumStates;

    mUnctrlInitialReachabilityExplorer =
      new UnctrlInitialReachabilityExplorer(eventAutomata, transitions,
                                            ndTuple1, EventEncoding.NONTAU,
                                            lastUncontrollable);
    mCoreachabilityExplorer =
      new CoreachabilityExplorer(reverseEventAutomata, reverseTransitions,
                                 ndTuple1, EventEncoding.NONTAU,
                                 mNumProperEvents - 1);
    mBackwardsUncontrollableExplorer =
      new BackwardsUncontrollableExplorer(reverseEventAutomata,
                                          reverseTransitions, ndTuple1,
                                          EventEncoding.NONTAU,
                                          lastUncontrollable);
    mReachabilityExplorer =
      new ReachabilityExplorer(eventAutomata, transitions, ndTuple2,
                               EventEncoding.NONTAU, mNumProperEvents - 1);
    mFinalStateExplorer =
      new FinalStateExplorer(eventAutomata, transitions, ndTuple3,
                             EventEncoding.NONTAU, mNumProperEvents - 1);
    mSupervisorSimplifier = new SuWonhamSupervisorReductionTRSimplifier();
    mDisabledEvents = new THashSet<>();
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final ProxyResult<ProductDESProxy> result = getAnalysisResult();
    result.setNumberOfAutomata(mNumAutomata);
    result.setNumberOfStates(mNumStates);
    if (mTransitionBuffer != null) {
      result.setNumberOfTransitions(mTransitionBuffer.size() / 3);
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mCtrlInitialReachabilityExplorer = null;
    mUnctrlInitialReachabilityExplorer = null;
    mCoreachabilityExplorer = null;
    mBackwardsUncontrollableExplorer = null;
    mReachabilityExplorer = null;
    mFinalStateExplorer = null;
    mSupervisorSimplifier = null;
    mOriginalStates = null;
    mStateMarkings = null;
    mEventEncoding = null;
    mGlobalVisited = null;
    mStateTuples = null;
    mUnvisited = null;
    mTransitionBuffer = null;
    mTargetTuple = null;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final int[] sentinel = new int[1];
      while (!mGlobalStack.isEmpty()) {
        final int[] encodedRoot = mGlobalStack.pop();
        final int r = mGlobalVisited.get(encodedRoot);
        if (mSafeStates.get(r) || mBadStates.get(r)) {
          continue;
        }
        mLocalStack.push(encodedRoot);
        mLocalVisited.add(encodedRoot);
        mBackTrace.push(sentinel);
        boolean safe = true;
        while (!mLocalStack.isEmpty()) {
          if (mLocalStack.peek() == mBackTrace.peek()) {
            mLocalStack.pop();
            mBackTrace.pop();
          } else {
            final int[] current = mLocalStack.peek();
            mBackTrace.push(current);
            safe = mUnctrlInitialReachabilityExplorer.explore(current);
            if (!safe) {
              break;
            }
          }
        }
        if (safe) {
          for (final int[] current : mLocalVisited) {
            final int n = addEncodedNewState(current);
            mSafeStates.set(n);
          }
          for (final int[] current : mLocalVisited) {
            mCtrlInitialReachabilityExplorer.explore(current);
          }
          mBackTrace.clear();
        } else {
          mLocalStack.clear();
          int[] current = mBackTrace.pop();
          while (current != sentinel) {
            int n = mGlobalVisited.get(current);
            if (n >= 0) {
              if (n < mNumInitialStates) {
                mInitialIsBad = true;
                return setBooleanResult(false);
              }
            } else {
              n = addEncodedNewState(current);
            }
            mBadStates.set(n);
            current = mBackTrace.pop();
          }
        }
        mLocalVisited.clear();
      }

      if (getUsedDefaultMarking() != null) {
        mMustContinue = false;
        do {
          // mark non-coreachable states (trim)
          mNonCoreachableStates.set(0, mNumStates);
          for (int t = 0; t < mNumStates; t++) {
            if (!mBadStates.get(t)) {
              final int[] tuple = new int[mNumAutomata];
              final int[] encodedTuple = mStateTuples.get(t);
              mSTEncoding.decode(encodedTuple, tuple);
              if (isMarkedState(tuple)) {
                mUnvisited.add(encodedTuple);
                mNonCoreachableStates.set(t, false);
                while (!mUnvisited.isEmpty()) {
                  final int[] s = mUnvisited.remove();
                  mCoreachabilityExplorer.explore(s);
                }
              }
            }
          }

          for (int b = 0; b < mNumInitialStates; b++) {
            if (!mBadStates.get(b) && mNonCoreachableStates.get(b)) {
              mInitialIsBad = true;
              return setBooleanResult(false);
            }
          }
          // mark uncontrollable states
          mMustContinue = false;
          mUnvisited.clear();
          for (int state = 0; state < mNumStates; state++) {
            if (!mBadStates.get(state) && mNonCoreachableStates.get(state)) {
              mBadStates.set(state);
              mUnvisited.offer(mStateTuples.get(state));
              while (!mUnvisited.isEmpty()) {
                final int[] s = mUnvisited.remove();
                mBackwardsUncontrollableExplorer.explore(s);
                if (mInitialIsBad) {
                  return setBooleanResult(false);
                }
              }
            }
          }
        } while (mMustContinue);
      }

      // reachability search (count the number of reachable states)
      mNumGoodStates = 0;
      mUnvisited.clear();
      mReachabilityExplorer.permutations(mNumAutomata, null, -1);
      while (!mUnvisited.isEmpty()) {
        final int[] s = mUnvisited.remove();
        if (mGlobalVisited.containsKey(s)) {
          mReachabilityExplorer.explore(s);
        }
      }
      for (int b = 0; b < mNumInitialStates; b++) {
        if (!mReachableStates.get(b)) {
          mInitialIsBad = true;
          return setBooleanResult(false);
        }
      }

      // re-encode states (make only one bad state)
      final int markingID = 0;
      mTransitionRelation =
        new ListBufferTransitionRelation("supervisor",
                                         ComponentKind.SUPERVISOR,
                                         mEventEncoding,
                                         mNumGoodStates,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      mStateMap = new int[mNumStates];
      int index = 0;
      final int[] tuple = new int[mNumAutomata];
      for (int i = 0; i < mNumStates; i++) {
        if (mReachableStates.get(i)) {
          mStateMap[i] = index++;
          mSTEncoding.decode(mStateTuples.get(i), tuple);
          if (isMarkedState(tuple)) {
            mTransitionRelation.setMarked(index - 1, markingID, true);
          }
          if (i < mNumInitialStates) {
            mTransitionRelation.setInitial(index - 1, true);
          }
        } else {
          mStateMap[i] = mNumGoodStates;// the index of the bad state
        }
      }

      // final search (add transitions)
      mUnvisited.clear();
      mGoodStates = new BitSet();
      mFinalStateExplorer.permutations(mNumAutomata, null, -1);
      while (!mUnvisited.isEmpty()) {
        final int[] s = mUnvisited.remove();
        if (mGlobalVisited.containsKey(s)) {
          mFinalStateExplorer.explore(s);
        }
      }

      mNonCoreachableStates = null;
      mBadStates = null;
      mSafeStates = null;
      mReachableStates = null;

      //Event hiding
      SpecialEventsTRSimplifier eventSimplifier =
        new SpecialEventsTRSimplifier(mTransitionRelation);
      eventSimplifier.run();
      final TRAutomatonProxy eventsHiddenAut =
        new TRAutomatonProxy(mEventEncoding, mTransitionRelation);
      eventSimplifier = null;

      //Powerset construction
      final SubsetConstructionTRSimplifier subsetSimplifier =
        new SubsetConstructionTRSimplifier();
      final EventEncoding copy = new EventEncoding(mEventEncoding);
      final ListBufferTransitionRelation powersetRel =
        new ListBufferTransitionRelation(mTransitionRelation, copy,
                                         subsetSimplifier.getPreferredInputConfiguration());
      powersetRel.setName("powerset_events_hidden");
      subsetSimplifier.setTransitionRelation(powersetRel);
      subsetSimplifier.run();
      final TRAutomatonProxy powerSetAut = new TRAutomatonProxy(copy, powersetRel);

      //Synchronous composition
      Collection<AutomatonProxy> automata = new ArrayList<AutomatonProxy>(2);
      automata.add(powerSetAut);
      automata.add(eventsHiddenAut);
      final ProductDESProxy autToSync =
        getFactory().createProductDESProxy("synchronous_comp",mEventEncoding.getUsedEvents(),automata);
      final TRSynchronousProductBuilder builder =
        new TRSynchronousProductBuilder(autToSync);
      builder.setEventEncoding(mEventEncoding);
      builder.setPruningDeadlocks(true);
      builder.run();
      automata = null;

      //Synthesis step
      final TRAutomatonProxy syncModel = builder.getComputedAutomaton();
      mTransitionRelation = syncModel.getTransitionRelation();
      final TRSynchronousProductStateMap resultMap = builder.getAnalysisResult().getStateMap();

      int numDeletions = 1;
      final int dumpStateIndex = mTransitionRelation.getDumpStateIndex();
      final int[] sourceTuple = new int[2]; //0 = source set, 1 = source state?
      final int[] sourceTupleDash = new int[2];
      final int markedStateCode =
        mEventEncoding.getEventCode(getUsedDefaultMarking());
      while (numDeletions > 0) {
        numDeletions = 0;
        //Observability step
        mTransitionRelation.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        final TransitionIterator successorReadOnlyIter =
          mTransitionRelation.createSuccessorsReadOnlyIteratorByStatus(EventStatus.STATUS_CONTROLLABLE);
        //Loop through each state in sync product
        for(int source=0; source < mTransitionRelation.getNumberOfStates(); source++){
          //Get the controllable outgoing transitions for given state
          successorReadOnlyIter.resetState(source);
          while(successorReadOnlyIter.advance()){
            if(successorReadOnlyIter.getCurrentTargetState() == dumpStateIndex){
              final int event = successorReadOnlyIter.getCurrentEvent();
              resultMap.getOriginalState(source, sourceTuple);
              //Get sourceSet from pair
              final int[] sourceSet = subsetSimplifier.getSourceSet(sourceTuple[0]);
              for(final int s : sourceSet){
                if(s != sourceTuple[1]){
                  sourceTupleDash[0] = sourceTuple[0];
                  sourceTupleDash[1] = s;
                  final int newState = resultMap.getComposedState(sourceTupleDash);
                  final TransitionIterator succIterSameEvent =
                    mTransitionRelation.createSuccessorsModifyingIterator();
                  succIterSameEvent.reset(newState, event);
                  if (succIterSameEvent.advance()) {
                    succIterSameEvent.setCurrentToState(dumpStateIndex);
                    while(succIterSameEvent.advance()){
                      succIterSameEvent.remove();
                      //Increment removed state/transition counter
                      numDeletions++;
                    }
                  }
                }
              }
            }
          }
        }

        //Nonblocking step
        final TIntHashSet coreachableStates = new TIntHashSet(mTransitionRelation.getNumberOfStates());
        final TIntStack open = new TIntArrayStack();

        //For each state: If it is reachable and accepting, add it to open
        for(int state=0; state<mTransitionRelation.getNumberOfStates(); state++){
          if(mTransitionRelation.isReachable(state) && mTransitionRelation.isMarked(state, markedStateCode)){
            open.push(state);
            coreachableStates.add(state);
          }
        }

        //Create a backwards transition iterator
        mTransitionRelation.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);
        final TransitionIterator predecessorIterator = mTransitionRelation.createPredecessorsModifyingIterator();

        //Add all states that can reach a coreachable state to the set
        while(open.size() != 0){
          final int targetState = open.pop();
          predecessorIterator.resetState(targetState);
          while(predecessorIterator.advance()){
            final int sourceState = predecessorIterator.getCurrentSourceState();
            if(!coreachableStates.contains(sourceState)){
              open.push(predecessorIterator.getCurrentSourceState());
              coreachableStates.add(predecessorIterator.getCurrentSourceState());
            }
          }
        }

        //Change any transition that points to a non-coreachable state to dump
        mTransitionRelation.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        final TransitionIterator allTransIter = mTransitionRelation
          .createAllTransitionsModifyingIterator();
        while(allTransIter.advance()){
          final int targetState = allTransIter.getCurrentTargetState();
          if(!coreachableStates.contains(targetState) && targetState != dumpStateIndex){
            allTransIter.setCurrentToState(dumpStateIndex);
            //Increment removed state/transition counter
            numDeletions++;
          }
        }

        //Controllability step
        final TransitionIterator successorIterator =
          mTransitionRelation.createSuccessorsModifyingIterator();
        for(int state=0; state<mTransitionRelation.getNumberOfStates(); state++){
          //If a state is initial and not coreachable, return failed result
          //This is part of the non-blocking check above but is more efficient here
          if (mTransitionRelation.isInitial(state) && !coreachableStates.contains(state)){
            return setBooleanResult(false);
          }
          successorIterator.resetState(state);
          while(successorIterator.advance()){
           if(successorIterator.getCurrentTargetState() == dumpStateIndex){
             final int event = successorIterator.getCurrentEvent();
             final byte status = mEventEncoding.getProperEventStatus(event);
             if(!EventStatus.isControllableEvent(status)){
               //If an initial state is removed as not controllable, synthesis has failed.
               if (mTransitionRelation.isInitial(state)){
                 return setBooleanResult(false);
               }
               //Remove all out-going transitions
               final TransitionIterator stateSuccessorIterator =
                 mTransitionRelation.createSuccessorsModifyingIterator();
               stateSuccessorIterator.resetState(state);
               while(stateSuccessorIterator.advance()){
                 stateSuccessorIterator.remove();
               }
               //Remove marking
               mTransitionRelation.removeMarkings(state, markedStateCode);
               //Increment removed state/transition counter
               numDeletions++;
             }
           }
          }
        }

        //Check and set state reachability
        mTransitionRelation.checkReachability();
      }

      // Disable transitions in powerset relation to build supervisor
      final int psDumpIndex = powersetRel.getDumpStateIndex();
      final int spDumpIndex = mTransitionRelation.getDumpStateIndex();
      final TransitionIterator spIter =
        mTransitionRelation.createAllTransitionsModifyingIteratorByStatus
          (EventStatus.STATUS_CONTROLLABLE);
      final TransitionIterator psIter =
        powersetRel.createSuccessorsModifyingIterator();
      while (spIter.advance()) {
        if (spIter.getCurrentTargetState() == spDumpIndex) {
          final int spState = spIter.getCurrentSourceState();
          resultMap.getOriginalState(spState, sourceTuple);
          final int psState = sourceTuple[0];
          final int e = spIter.getCurrentEvent();
          psIter.reset(psState, e);
          if (psIter.advance() &&
              psIter.getCurrentTargetState() != psDumpIndex) {
            // redirect to psDumpIndex for supervisor reduction
            psIter.remove();
          }
        }
      }

      mTransitionRelation = powersetRel;
      mEventEncoding = copy;

      // Return result
      if (isDetailedOutputEnabled()) {
        mTransitionRelation.removeDumpStateTransitions();
        mTransitionRelation.checkReachability();
        mTransitionRelation.removeProperSelfLoopEvents();
        mTransitionRelation.removeRedundantPropositions();
        final ProductDESProxy result =
          createDESProxy(mTransitionRelation, mEventEncoding);
        return setProxyResult(result);
      } else {
        return setBooleanResult(true);
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      tearDown();
      final Logger logger = LogManager.getLogger();
      logger.debug("<out of memory>");
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final StackOverflowError error) {
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean isMarkedState(final int[] tuple)
  {
    for (int a = 0; a < mNumAutomata; a++) {
      if (!mStateMarkings[a][tuple[a]]) {
        return false;
      }
    }
    return true;
  }

  private int addDecodedNewState(final int[] decodedTuple)
    throws OverflowException
  {
    final int[] encoded = new int[mSTEncoding.getNumberOfWords()];
    mSTEncoding.encode(decodedTuple, encoded);
    return addEncodedNewState(encoded);
  }

  private int addEncodedNewState(final int[] encodedTuple)
    throws OverflowException
  {
    if (mGlobalVisited.containsKey(encodedTuple)) {
      return mGlobalVisited.get(encodedTuple);
    } else {
      final int code = mNumStates++;
      final int limit = getNodeLimit();
      if (mNumStates >= limit) {
        throw new OverflowException(limit);
      }
      mGlobalVisited.put(encodedTuple, code);
      mStateTuples.add(encodedTuple);
      return code;
    }
  }

  private ProductDESProxy createDESProxy(final ListBufferTransitionRelation rel,
                                         final EventEncoding encoding)
    throws EventNotFoundException
  {
    if (rel.getName() == null) {
      rel.setName(getSupervisorNamePrefix());
    }
    final AutomatonProxy aut =
      rel.createAutomaton(getFactory(), encoding);
    return AutomatonTools.createProductDESProxy(aut, getFactory());
  }


  //#########################################################################
  //# Inner Class AutomatonEventInfo
  private abstract class AutomatonEventInfo implements
    Comparable<AutomatonEventInfo>
  {
    //#######################################################################
    //# Constructor
    public AutomatonEventInfo(final int aut, final double probability)
    {
      mAutomaton = aut;
      mProbability = probability;
    }

    //#######################################################################
    //# Simple Access
    int getAutomaton()
    {
      return mAutomaton;
    }

    double getProbability()
    {
      return mProbability;
    }

    boolean isPlant()
    {
      return mAutomaton < mNumPlants;
    }

    //#######################################################################
    //# Interface java.util.Comparable<AutomatonEventInfo>
    @Override
    public int compareTo(final AutomatonEventInfo info)
    {
      if (mProbability < info.mProbability) {
        return -1;
      } else if (mProbability > info.mProbability) {
        return 1;
      } else {
        return mAutomaton - info.mAutomaton;
      }
    }

    //#######################################################################
    //# Data Members
    private final int mAutomaton;
    private final double mProbability;
  }


  //#########################################################################
  //# Inner Class UncontrollableAutomatonEventInfo
  private class UncontrollableAutomatonEventInfo extends AutomatonEventInfo
  {
    //#######################################################################
    //# Constructor
    private UncontrollableAutomatonEventInfo(final int aut,
                                             final double probability)
    {
      super(aut, probability);
    }

    //#######################################################################
    //# Interface java.util.Comparable<AutomatonEventInfo>
    @Override
    public int compareTo(final AutomatonEventInfo info)
    {
      assert info instanceof UncontrollableAutomatonEventInfo;
      final boolean thisPlant = isPlant() && getProbability() < 1.0;
      final boolean thatPlant = info.isPlant() && info.getProbability() < 1.0;
      if (thisPlant != thatPlant) {
        return thisPlant ? -1 : 1;
      } else {
        return super.compareTo(info);
      }
    }
  }


  //#########################################################################
  //# Inner Class ControllableAutomatonEventInfo
  private class ControllableAutomatonEventInfo extends AutomatonEventInfo
  {
    //#######################################################################
    //# Constructor
    private ControllableAutomatonEventInfo(final int aut,
                                           final double probability)
    {
      super(aut, probability);
    }
  }


  //#########################################################################
  //# Inner Class StateExplorer
  private abstract class StateExplorer
  {
    //#########################################################################
    //# Constructor
    StateExplorer(final int[][] theEventAutomata,
                  final int[][][][] theTransitions,
                  final int[][] theNDTuple, final int theFirstEvent,
                  final int theLastEvent)
    {
      mmFirstEvent = theFirstEvent;
      mmLastEvent = theLastEvent;
      mmEventAutomata = theEventAutomata;
      mmTransitions = theTransitions;
      mmNDTuple = theNDTuple;
      mmDecodedTuple = new int[mNumAutomata];
    }

    //#########################################################################
    //# State Exploration
    boolean explore(final int[] encodedTuple)
      throws AnalysisAbortException
    {
      checkAbort();
      mSTEncoding.decode(encodedTuple, mmDecodedTuple);
      events:
      for (int e = mmFirstEvent; e <= mmLastEvent; e++) {
        Arrays.fill(mmNDTuple, null);
        for (final int a : mmEventAutomata[e]) {
          if (mmTransitions[a][e] != null) {
            final int[] succ = mmTransitions[a][e][mmDecodedTuple[a]];
            if (succ == null) {
              handleBlocked(mmDecodedTuple, e, a);
              continue events;
            }
            mmNDTuple[a] = succ;
          }
        }
        if (!permutations(mNumAutomata, mmDecodedTuple, e)) {
          return false;
        }
      }
      return true;
    }

    boolean permutations(int a, final int[] decodedSource, final int event)
      throws OverflowException
    {
      if (a == 0) {
        if (!processNewState(decodedSource, event, decodedSource == null)) {
          return false;
        }
      } else {
        a--;
        final int[] codes = mmNDTuple[a];
        if (codes == null) {
          mTargetTuple[a] = decodedSource[a];
          if (!permutations(a, decodedSource, event)) {
            return false;
          }
        } else {
          for (int i = 0; i < codes.length; i++) {
            mTargetTuple[a] = codes[i];
            if (!permutations(a, decodedSource, event)) {
              return false;
            }
          }
        }
      }
      return true;
    }

    abstract boolean processNewState(final int[] decodedSource,
                                     final int event,
                                     final boolean isInitial)
      throws OverflowException;

    void handleBlocked(final int[] decodedSource, final int e, final int a)
    {
    }

    //#########################################################################
    //# Data Members
    protected final int[][] mmEventAutomata;
    protected final int[][][][] mmTransitions;
    protected final int[][] mmNDTuple;
    protected int mmFirstEvent;
    protected int mmLastEvent;
    protected int[] mmDecodedTuple;
  }


  //#########################################################################
  //# Inner Class CtrlInitialReachabilityExplorer
  private class CtrlInitialReachabilityExplorer extends StateExplorer
  {
    public CtrlInitialReachabilityExplorer(final int[][] eventAutomata,
                                           final int[][][][] transitions,
                                           final int[][] NDTuple,
                                           final int firstEvent,
                                           final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    @Override
    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
      throws OverflowException
    {
      final int currentNumStates = mNumStates;
      final int t = addDecodedNewState(mTargetTuple);
      if (currentNumStates != mNumStates) {
        mGlobalStack.push(mStateTuples.get(t));
      }
      return true;
    }
  }


  //#########################################################################
  //# Inner Class UnctrlInitialReachabilityExplorer
  private class UnctrlInitialReachabilityExplorer extends StateExplorer
  {
    public UnctrlInitialReachabilityExplorer(final int[][] eventAutomata,
                                             final int[][][][] transitions,
                                             final int[][] NDTuple,
                                             final int firstEvent,
                                             final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    @Override
    public boolean explore(final int[] encodedTuple)
      throws AnalysisAbortException
    {
      checkAbort();
      mSTEncoding.decode(encodedTuple, mmDecodedTuple);
      events: for (int e = mmFirstEvent; e <= mmLastEvent; e++) {
        Arrays.fill(mmNDTuple, null);
        for (final int a : mmEventAutomata[e]) {
          if (mmTransitions[a][e] != null) {
            final int[] succ = mmTransitions[a][e][mmDecodedTuple[a]];
            if (succ == null) {
              if (a >= mNumPlants) {
                return false;
              } else {
                continue events;
              }
            }
            mmNDTuple[a] = succ;
          }
        }
        if (!permutations(mNumAutomata, mmDecodedTuple, e)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
      throws OverflowException
    {
      final int[] encoded = new int[mSTEncoding.getNumberOfWords()];
      mSTEncoding.encode(mTargetTuple, encoded);
      if (mLocalVisited.contains(encoded)) {
        return true;
      } else {
        final int s = mGlobalVisited.get(encoded);
        if (s < 0) {
          // nothing ...
        } else if (mBadStates.get(s)) {
          return false;
        } else if (mSafeStates.get(s)) {
          return true;
        }
      }
      mLocalVisited.add(encoded);
      mLocalStack.push(encoded);
      return true;
    }
  }


  //#########################################################################
  //# Inner Class CoreachabilityExplorer
  private class CoreachabilityExplorer extends StateExplorer
  {
    public CoreachabilityExplorer(final int[][] eventAutomata,
                                  final int[][][][] transitions,
                                  final int[][] NDTuple,
                                  final int firstEvent, final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    /*
     * @Override public boolean isUsedEvent(final int e) { final EventProxy
     * event = mEventEncoding.getProperEvent(e); final Map<String,String>
     * attribs = event.getAttributes(); return
     * !attribs.containsKey("synthesis:exceptional"); }
     */

    @Override
    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
    {
      final int[] encoded = new int[mSTEncoding.getNumberOfWords()];
      mSTEncoding.encode(mTargetTuple, encoded);
      if (mGlobalVisited.containsKey(encoded)) {
        final int s = mGlobalVisited.get(encoded);
        if (!mBadStates.get(s) && mNonCoreachableStates.get(s)) {
          mUnvisited.offer(mStateTuples.get(s));
          mNonCoreachableStates.set(s, false);
        }
      }
      return true;
    }
  }


  //#########################################################################
  //# Inner Class SuccessorStatesExplorer
  private class BackwardsUncontrollableExplorer extends StateExplorer
  {
    public BackwardsUncontrollableExplorer(final int[][] eventAutomata,
                                           final int[][][][] transitions,
                                           final int[][] NDTuple,
                                           final int firstEvent,
                                           final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    @Override
    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
    {
      final int[] encoded = new int[mSTEncoding.getNumberOfWords()];
      mSTEncoding.encode(mTargetTuple, encoded);
      if (mGlobalVisited.containsKey(encoded)) {
        final int s = mGlobalVisited.get(encoded);
        if (!mBadStates.get(s)) {
          mUnvisited.offer(mStateTuples.get(s));
          mBadStates.set(s);
          mMustContinue = true;
        }
      }
      return true;
    }
  }


  //#########################################################################
  //# Inner Class ReachabilityExplorer
  private class ReachabilityExplorer extends StateExplorer
  {
    public ReachabilityExplorer(final int[][] eventAutomata,
                                final int[][][][] transitions,
                                final int[][] NDTuple, final int firstEvent,
                                final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    @Override
    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
      throws OverflowException
    {
      final int[] encoded = new int[mSTEncoding.getNumberOfWords()];
      mSTEncoding.encode(mTargetTuple, encoded);
      final int target = mGlobalVisited.get(encoded);
      if (!mBadStates.get(target) && !mReachableStates.get(target)) {
        mReachableStates.set(target);
        mUnvisited.offer(mStateTuples.get(target));
        mNumGoodStates++;
      }
      return true;
    }
  }


  //#########################################################################
  //# Inner Class FinalStateExplorer
  private class FinalStateExplorer extends StateExplorer
  {

    //#######################################################################
    //# Constructor
    private FinalStateExplorer(final int[][] eventAutomata,
                               final int[][][][] transitions,
                               final int[][] NDTuple, final int firstEvent,
                               final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
      mmEncodedBuffer = new int[mSTEncoding.getNumberOfWords()];
    }

    //#######################################################################
    //# State Exploration
    @Override
    boolean processNewState(final int[] decodedSource,
                            final int e,
                            final boolean isInitial)
      throws OverflowException
    {
      int source = 0;
      if (!isInitial) {
        mSTEncoding.encode(decodedSource, mmEncodedBuffer);
        source = mGlobalVisited.get(mmEncodedBuffer);
        source = mStateMap[source];
      }
      mSTEncoding.encode(mTargetTuple, mmEncodedBuffer);
      int target = mGlobalVisited.get(mmEncodedBuffer);
      if (mReachableStates.get(target) && !mGoodStates.get(target)) {
        mGoodStates.set(target);
        mUnvisited.offer(mStateTuples.get(target));
      }
      if (!isInitial) {
        target = mStateMap[target];
        if (target == mTransitionRelation.getDumpStateIndex()) {
          addDumpTransition(source, e);
        } else {
          mTransitionRelation.addTransition(source, e, target);
        }
      }
      return true;
    }

    @Override
    void handleBlocked(final int[] decodedSource, final int e, final int a)
    {
      if (a >= mNumPlants) {
        addDumpTransition(decodedSource, e);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private void addDumpTransition(final int[] decodedSource,
                                   final int e)
    {
      mSTEncoding.encode(decodedSource, mmEncodedBuffer);
      final int source = mGlobalVisited.get(mmEncodedBuffer);
      addDumpTransition(mStateMap[source], e);
    }

    private void addDumpTransition(final int source, final int e)
    {
      final int target = mTransitionRelation.getDumpStateIndex();
      mTransitionRelation.addTransition(source, e, target);
      mTransitionRelation.setReachable(target, true);
      final EventProxy event = mEventEncoding.getProperEvent(e);
      mDisabledEvents.add(event);
    }

    //#######################################################################
    //# Data Members
    private final int[] mmEncodedBuffer;
  }


  //#########################################################################
  //# Data Members
  private Collection<EventProxy> mDisabledEvents;

  private List<AutomatonProxy> mAutomata;
  private EventEncoding mEventEncoding;
  private TIntArrayList mTransitionBuffer;

  private int mNumAutomata;
  private int mNumPlants;
  private int mNumStates;
  private int mNumInitialStates;
  private int mNumProperEvents;

  private StateProxy[][] mOriginalStates;
  private boolean[][] mStateMarkings;

  private StateTupleEncoding mSTEncoding;
  private List<int[]> mStateTuples;
  private TObjectIntCustomHashMap<int[]> mGlobalVisited;
  private Deque<int[]> mGlobalStack;
  private Deque<int[]> mLocalStack;
  private Deque<int[]> mBackTrace;
  private Set<int[]> mLocalVisited;
  private Queue<int[]> mUnvisited;
  private int[] mTargetTuple;

  private BitSet mNonCoreachableStates;
  private BitSet mBadStates;
  private BitSet mSafeStates;
  private BitSet mReachableStates;
  private boolean mInitialIsBad;
  private boolean mMustContinue;

  private StateExplorer mCtrlInitialReachabilityExplorer;
  private StateExplorer mUnctrlInitialReachabilityExplorer;
  private StateExplorer mCoreachabilityExplorer;
  private StateExplorer mBackwardsUncontrollableExplorer;
  private StateExplorer mReachabilityExplorer;
  private FinalStateExplorer mFinalStateExplorer;
  private ListBufferTransitionRelation mTransitionRelation;

  private SuWonhamSupervisorReductionTRSimplifier mSupervisorSimplifier;

  private int mNumGoodStates;
  private BitSet mGoodStates;
  private int[] mStateMap;


  //#########################################################################
  //# Class Constants
  private static final int MAX_TABLE_SIZE = 500000;

}
