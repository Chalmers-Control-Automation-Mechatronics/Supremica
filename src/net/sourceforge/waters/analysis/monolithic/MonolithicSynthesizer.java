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

import net.sourceforge.waters.analysis.abstraction.SupervisorReductionSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntArrayHashingStrategy;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersHashSet;
import net.sourceforge.waters.model.analysis.AbortRequester;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractSupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
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

public class MonolithicSynthesizer extends AbstractSupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  public MonolithicSynthesizer(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public MonolithicSynthesizer(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public MonolithicSynthesizer(final ProductDESProxy model,
                               final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Specific Access
  public Collection<EventProxy> getDisabledEvents()
  {
    return mDisabledEvents;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort(final AbortRequester sender)
  {
    super.requestAbort(sender);
    if (mMinimizationChain != null) {
      mMinimizationChain.requestAbort(sender);
    }
    if (mReductionChain != null) {
      mReductionChain.requestAbort(sender);
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mMinimizationChain != null) {
      mMinimizationChain.resetAbort();
    }
    if (mReductionChain != null) {
      mReductionChain.resetAbort();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public MonolithicSynthesisResult getAnalysisResult()
  {
    return (MonolithicSynthesisResult) super.getAnalysisResult();
  }



  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  public MonolithicSynthesisResult createAnalysisResult()
  {
    return new MonolithicSynthesisResult(this);
  }

  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();

    int[][][][] transitions;
    int[][][][] reverseTransitions;
    int[][] eventAutomata;
    int[][] reverseEventAutomata;
    int[][] ndTuple1;
    int[][] ndTuple2;
    int[][] ndTuple3;

    final ProductDESProxy model = getModel();

    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> events = model.getEvents();
    final EventProxy marking = getUsedDefaultMarking();
    final Collection<EventProxy> filter = Collections.singleton(marking);
    mEventEncoding =
      new EventEncoding(events, translator, filter,
                        EventEncoding.FILTER_PROPOSITIONS);
    mEventEncoding.sortProperEvents(EventStatus.STATUS_CONTROLLABLE);
    mNumProperEvents = mEventEncoding.getNumberOfProperEvents();

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
    transitions = new int[mNumAutomata][mNumProperEvents][][];
    reverseTransitions = new int[mNumAutomata][mNumProperEvents][][];
    eventAutomata = new int[mNumProperEvents][];
    reverseEventAutomata = new int[mNumProperEvents][];
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
        new TIntArrayList[mNumProperEvents][numStates];
      final TIntArrayList[][] autTransitionListsRvs =
        new TIntArrayList[mNumProperEvents][numStates];
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
    mNumControllableEvents = 0;
    final List<AutomatonEventInfo> list =
      new ArrayList<AutomatonEventInfo>(mNumAutomata);
    final List<AutomatonEventInfo> listRvs =
      new ArrayList<AutomatonEventInfo>(mNumAutomata);
    for (int e = EventEncoding.NONTAU; e < mNumProperEvents; e++) {
      final byte status = mEventEncoding.getProperEventStatus(e);
      final boolean controllable = EventStatus.isControllableEvent(status);
      if (controllable) {
        mNumControllableEvents++;
      }
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
    mUnvisited = new ArrayDeque<int[]>(100);
    mNumStates = mNumTransitions = 0;

    final int[] sizes = StateTupleEncoding.getAutomataSizes(mAutomata);
    mSTEncoding = new StateTupleEncoding(sizes);

    // We have sorted the events so all uncontrollables come first.
    final int numEvents = mEventEncoding.getNumberOfProperEvents();
    final int firstControllable = numEvents - mNumControllableEvents;
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
    mDisabledEvents = new THashSet<>();

    final boolean localisation = isSupervisorLocalizationEnabled();
    final double maxIncrease = getSupervisorReductionMaxIncrease();
    mReductionChain = getSupervisorReductionFactory().
      createSupervisorReducer(localisation, maxIncrease);
    if (mReductionChain != null) {
      final int stateLimit = getNodeLimit();
      final int transitionLimit = getTransitionLimit();
      final int markingID = getUsedDefaultMarkingID();
      mReductionChain.setStateLimit(stateLimit);
      mReductionChain.setTransitionLimit(transitionLimit);
      mReductionChain.setDefaultMarkingID(markingID);
      mMinimizationChain =
        getSupervisorReductionFactory().createInitialMinimizer(false);
      if (mMinimizationChain != null) {
        mMinimizationChain.setStateLimit(stateLimit);
        mMinimizationChain.setTransitionLimit(transitionLimit);
        mMinimizationChain.setDefaultMarkingID(markingID);
        final int config = mReductionChain.getPreferredInputConfiguration();
        mMinimizationChain.setPreferredOutputConfiguration(config);
      }
    }
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final MonolithicSynthesisResult result = getAnalysisResult();
    result.setTotalNumberOfEvents(mNumProperEvents - 1); // do not include tau
    result.setNumberOfAutomata(mNumAutomata);
    result.setNumberOfStates(mNumStates);
    result.setNumberOfTransitions(mNumTransitions);
    if (mMinimizationChain != null) {
      result.addSimplifierStatistics(mMinimizationChain);
    }
    if (mReductionChain != null) {
      result.addSimplifierStatistics(mReductionChain);
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
    mOriginalStates = null;
    mStateMarkings = null;
    mEventEncoding = null;
    mGlobalVisited = null;
    mStateTuples = null;
    mUnvisited = null;
    mTargetTuple = null;
    mMinimizationChain = mReductionChain = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean run() throws AnalysisException
  {
    final Logger logger = LogManager.getLogger();

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
        new ListBufferTransitionRelation(getSupervisorNamePrefix(),
                                         ComponentKind.SUPERVISOR,
                                         mEventEncoding,
                                         mNumGoodStates,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      mStateMap = new int[mNumStates];
      int index = 0;
      final int[] tuple = new int[mNumAutomata];
      for (int i = 0; i < mNumStates; i++) {
        if (mReachableStates.get(i)) {
          mStateMap[i] = index;
          if (i < mNumInitialStates) {
            mTransitionRelation.setInitial(index, true);
          }
          // TODO The markings are set based on the input model.
          // They can only be trusted when supervisor reduction is disabled.
          mSTEncoding.decode(mStateTuples.get(i), tuple);
          if (isMarkedState(tuple)) {
            mTransitionRelation.setMarked(index, markingID, true);
          }
          index++;
        } else {
          mStateMap[i] = mNumGoodStates; // the index of the bad state
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

      if (isDetailedOutputEnabled()) {
        ProductDESProxy des = null;
        if (mReductionChain == null) {
          // no supervisor reduction
          mTransitionRelation.removeDumpStateTransitions();
          mTransitionRelation.removeProperSelfLoopEvents();
          mTransitionRelation.removeRedundantPropositions();
          des = createDESProxy(mTransitionRelation);
        } else {
          logger.debug("Reducing supervisor ...");
          //mTransitionRelation.setPropositionUsed(markingID, false);
          if (mMinimizationChain != null) {
            mMinimizationChain.setTransitionRelation(mTransitionRelation);
            mMinimizationChain.run();
            mTransitionRelation = mMinimizationChain.getTransitionRelation();
          }
          if (isSupervisorLocalizationEnabled()) {
            // localised supervisors, one per controllable event
            final List<AutomatonProxy> localizedSupervisors =
              new ArrayList<>(mNumControllableEvents);
            collectLocalizedSupervisors(localizedSupervisors);
            des = AutomatonTools.createProductDESProxy
              ("localised_sup", localizedSupervisors, getFactory());
          } else {
            // one-step supervisor reduction
            mReductionChain.setSupervisedEvent(-1);
            mReductionChain.setTransitionRelation(mTransitionRelation);
            mReductionChain.run();
            mTransitionRelation.removeDumpStateTransitions();
            mTransitionRelation.removeRedundantPropositions();
            des = createDESProxy(mTransitionRelation);
          }
        }
        return setProxyResult(des);
      } else {
        return setBooleanResult(true);
      }

    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      tearDown();
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

  private void collectLocalizedSupervisors(final List<AutomatonProxy> supervisors)
    throws AnalysisException
  {
    final Logger logger = LogManager.getLogger();
    int config = mReductionChain.getPreferredInputConfiguration();
    if (config == 0) {
      config = mTransitionRelation.getConfiguration();
    }
    final IsomorphismChecker checker =
      new IsomorphismChecker(getFactory(), false, false);
    final String prefix = getSupervisorNamePrefix() + ":";
    for (int e = EventEncoding.NONTAU; e < mNumProperEvents; e++) {
      if (isEverDisabledControllableEvent(mTransitionRelation, e)) {
        // TODO avoid duplicate computation and duplicate supervisors if
        // several events are enabled and disabled in exactly the same states
        mReductionChain.setSupervisedEvent(e);
        final EventEncoding enc = new EventEncoding(mEventEncoding);
        ListBufferTransitionRelation supervisor =
          new ListBufferTransitionRelation(mTransitionRelation, enc, config);
        removeOtherControllableDisablements(supervisor, e);
        final EventProxy event = mEventEncoding.getProperEvent(e);
        final String eventName = event.getName();
        logger.debug("Localised supervisor for event {} ...", eventName);
        supervisor.setName(prefix + eventName);
        mReductionChain.setTransitionRelation(supervisor);
        mReductionChain.run();
        supervisor = mReductionChain.getTransitionRelation();
        supervisor.removeDumpStateTransitions();
        supervisor.removeRedundantPropositions();
        AutomatonProxy aut = new TRAutomatonProxy(enc, supervisor);
        if (!supervisors.isEmpty()) {
          logger.debug("Comparing result with previous supervisors ...");
          for (final AutomatonProxy existing : supervisors) {
            if (checker.checkBisimulation(aut, existing)) {
              logger.debug("Suppressing duplicate supervisor.");
              aut = null;
              break;
            }
          }
        }
        if (aut != null) {
          supervisors.add(aut);
        }
      }
    }
  }


  //#########################################################################
  //# Static Methods for Supervisor Localisation
  public static boolean isEverDisabledControllableEvent
    (final ListBufferTransitionRelation rel, final int e)
  {
    final byte status = rel.getProperEventStatus(e);
    if (EventStatus.isControllableEvent(status) &&
        EventStatus.isUsedEvent(status)) {
      final int dump = rel.getDumpStateIndex();
      final TransitionIterator iter =
        rel.createAllTransitionsReadOnlyIterator(e);
      while (iter.advance()) {
        if (iter.getCurrentTargetState() == dump) {
          return true;
        }
      }
    }
    return false;
  }

  public static void removeOtherControllableDisablements
    (final ListBufferTransitionRelation rel, final int supervisedEvent)
  {
    final int dump = rel.getDumpStateIndex();
    final TransitionIterator iter =
      rel.createAllTransitionsModifyingIteratorByStatus
        (EventStatus.STATUS_CONTROLLABLE);
    while (iter.advance()) {
      if (iter.getCurrentTargetState() == dump &&
          iter.getCurrentEvent() != supervisedEvent) {
        iter.remove();
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private int getUsedDefaultMarkingID() throws EventNotFoundException
  {
    final EventProxy marking = getUsedDefaultMarking();
    return mEventEncoding.getEventCode(marking);
  }

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

  private ProductDESProxy createDESProxy(final ListBufferTransitionRelation rel)
    throws EventNotFoundException
  {
    if (rel.getName() == null) {
      rel.setName(getSupervisorNamePrefix());
    }
    final AutomatonProxy aut =
      rel.createAutomaton(getFactory(), mEventEncoding);
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
    public UncontrollableAutomatonEventInfo(final int aut,
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
    public ControllableAutomatonEventInfo(final int aut,
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
    public StateExplorer(final int[][] theEventAutomata,
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

    public boolean permutations(int a, final int[] decodedSource,
                                final int event) throws OverflowException
    {
      if (a == 0) {
        mNumTransitions++;
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

    public abstract boolean processNewState(final int[] decodedSource,
                                            final int event,
                                            final boolean isInitial)
      throws OverflowException;

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
    }

    //#######################################################################
    //# State Exploration
    @Override
    public boolean processNewState(final int[] decodedSource, final int e,
                                   final boolean isInitial)
      throws OverflowException
    {
      final int[] encoded = new int[mSTEncoding.getNumberOfWords()];
      int source = 0;
      if (!isInitial) {
        mSTEncoding.encode(decodedSource, encoded);
        source = mGlobalVisited.get(encoded);
        source = mStateMap[source];
      }
      mSTEncoding.encode(mTargetTuple, encoded);
      int target = mGlobalVisited.get(encoded);
      if (mReachableStates.get(target) && !mGoodStates.get(target)) {
        mGoodStates.set(target);
        mUnvisited.offer(mStateTuples.get(target));
      }
      target = mStateMap[target];
      if (!isInitial) {
        mTransitionRelation.addTransition(source, e, target);
        if (target == mTransitionRelation.getDumpStateIndex()) {
          mTransitionRelation.setReachable(target, true);
          final EventProxy event = mEventEncoding.getProperEvent(e);
          mDisabledEvents.add(event);
        }
      }
      return true;
    }
  }


  //#########################################################################
  //# Debugging
  @SuppressWarnings("unused")
  private int[] showTuple(final int[] encodedTuple)
  {
    final int[] tuple = new int[mNumAutomata];
    mSTEncoding.decode(encodedTuple, tuple);
    return tuple;
  }

  @SuppressWarnings("unused")
  private String showStateTuple(final int[] tuple)
  {
    String msg = "";
    for (int i = 0; i < tuple.length; i++) {
      final AutomatonProxy aut = mAutomata.get(i);
      final ComponentKind kind = getKindTranslator().getComponentKind(aut);
      final StateProxy state = mOriginalStates[i][tuple[i]];
      msg +=
        kind.toString() + " " + aut.getName() + " : [" + tuple[i] + "] "
          + state.getName() + "\n";
    }
    return msg;
  }


  //#########################################################################
  //# Data Members
  private Collection<EventProxy> mDisabledEvents;

  private TransitionRelationSimplifier mMinimizationChain;
  private SupervisorReductionSimplifier mReductionChain;

  private List<AutomatonProxy> mAutomata;
  private EventEncoding mEventEncoding;
  //private TIntArrayList mTransitionBuffer;

  private int mNumAutomata;
  private int mNumPlants;
  private int mNumStates;
  private int mNumTransitions;
  private int mNumInitialStates;
  private int mNumProperEvents;
  private int mNumControllableEvents;

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

  private int mNumGoodStates;
  private BitSet mGoodStates;
  private int[] mStateMap;


  //#########################################################################
  //# Class Constants
  private static final int MAX_TABLE_SIZE = 500000;

}
