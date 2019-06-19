//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.DefaultSupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SelfloopSupervisorReductionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.options.BoolParameter;
import net.sourceforge.waters.analysis.options.EnumParameter;
import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.analysis.options.ParameterIDs;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntArrayHashingStrategy;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersHashSet;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractProductDESBuilder;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.analysis.des.NondeterministicDESException;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

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

public class MonolithicSynthesizer extends AbstractProductDESBuilder
  implements SupervisorSynthesizer
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
  //# Configuration
  public void setNonblockingSupported(final boolean support)
  {
    mNonblockingSupported = support;
  }

  public boolean getNonblockingSupported()
  {
    return mNonblockingSupported;
  }

  /**
   * Sets the preferred name (or name prefix) for any supervisors produced as
   * output.
   */
  @Override
  public void setOutputName(final String name)
  {
    mOutputName = name;
  }

  /**
   * Gets the preferred name of supervisors produced as output.
   *
   * @see #setOutputName(String) setOutputName()
   */
  @Override
  public String getOutputName()
  {
    return mOutputName;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mMinimizationChain != null) {
      mMinimizationChain.requestAbort();
    }
    if (mReductionChain != null) {
      mReductionChain.requestAbort();
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
  //# Interface net.sourceforge.waters.model.analysis.SupervisorSynthesizer
  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mConfiguredMarking = marking;
    mUsedMarking = null;
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredMarking;
  }

  @Override
  public void setNondeterminismEnabled(final boolean enable)
  {
    mNondeterminismEnabled = enable;
  }

  @Override
  public void setSupervisorReductionFactory(final SupervisorReductionFactory factory)
  {
    mSupervisorReductionFactory = factory;
  }

  @Override
  public SupervisorReductionFactory getSupervisorReductionFactory()
  {
    return mSupervisorReductionFactory;
  }

  @Override
  public void setSupervisorLocalizationEnabled(final boolean enable)
  {
    mSupervisorLocalizationEnabled = enable;
  }

  @Override
  public boolean getSupervisorLocalizationEnabled()
  {
    return mSupervisorLocalizationEnabled;
  }

  public Collection<EventProxy> getDisabledEvents()
  {
    return mDisabledEvents;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public boolean supportsNondeterminism()
  {
    return mNondeterminismEnabled;
  }

  @Override
  public MonolithicSynthesisResult getAnalysisResult()
  {
    return (MonolithicSynthesisResult) super.getAnalysisResult();
  }

  @Override
  public List<Parameter> getParameters()
  {
    final List<Parameter> list = super.getParameters();
    for (final Parameter param : list) {
      switch (param.getID()) {
      case ParameterIDs.ModelAnalyzer_setDetailedOutputEnabled:
        param.setName("Create supervisor automata");
        param.setDescription("Disable this to suppress the creation of supervisor " +
                             "automata, and only determine whether a supervisor " +
                             "exists.");
        break;
      case ParameterIDs.ModelAnalyzer_setNodeLimit:
        param.setName("State limit");
        param.setDescription("Maximum number of states before aborting.");
        break;
      case ParameterIDs.ModelAnalyzer_setTransitionLimit:
        param.setDescription("Maximum number of transitions before aborting.");
        break;
      default:
        break;
     }
    }
    list.add(new BoolParameter
               (ParameterIDs.MonolithicSynthesizer_setNonblockingSupported,
                "Nonblocking",
                "Synthesize a nonblocking supervisor.", true){
                 @Override
                 public void commitValue()
                 {
                   setNonblockingSupported(getValue());
                 }
               });
    // list.add(new EnumParameter(2, "Marking proposition", "If synthesising a nonblocking supervisor, select the proposition that defines the marked states."))
    list.add(new EnumParameter<SupervisorReductionFactory>
               (ParameterIDs.SupervisorSynthesizer_setSupervisorReductionFactory,
                "Supervisor reduction",
                "Method of supervisor reduction to be used after synthesis",
                DefaultSupervisorReductionFactory.class.getEnumConstants()){
                 @Override
                 public void commitValue()
                 {
                   setSupervisorReductionFactory(getValue());
                 }
               });
    list.add(new BoolParameter
               (ParameterIDs.SupervisorSynthesizer_setSupervisorLocalisationEnabled,
                "Localize supervisors",
                "If using supervisor reduction, create a separate supervisor " +
                "for each controllable event that needs to be disabled.",
                true){
                 @Override
                 public void commitValue()
                 {
                   setSupervisorLocalizationEnabled(getValue());
                 }
               });
    return list;
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
    mTransitionBuffer = new TIntArrayList();
    mUnvisited = new ArrayDeque<int[]>(100);
    mNumStates = 0;

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

    mReductionChain = mSupervisorReductionFactory.createSimplifier();
    if (mReductionChain != null) {
      final int stateLimit = getNodeLimit();
      final int transitionLimit = getTransitionLimit();
      final int markingID = getUsedDefaultMarkingID();
      mReductionChain.setStateLimit(stateLimit);
      mReductionChain.setTransitionLimit(transitionLimit);
      mReductionChain.setDefaultMarkingID(markingID);
      final int config = mReductionChain.getPreferredInputConfiguration();
      final ChainTRSimplifier chain = new ChainTRSimplifier();
      chain.add(new SelfloopSupervisorReductionTRSimplifier());
      final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier();
      bisimulator.setEquivalence
        (ObservationEquivalenceTRSimplifier.Equivalence.
         DETERMINISTIC_MINSTATE);
      bisimulator.setTransitionLimit(transitionLimit);
      chain.add(bisimulator);
      chain.setDefaultMarkingID(markingID);
      chain.setPreferredOutputConfiguration(config);
      mMinimizationChain = chain;
    }
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final MonolithicSynthesisResult result = getAnalysisResult();
    result.setNumberOfAutomata(mNumAutomata);
    result.setNumberOfStates(mNumStates);
    if (mTransitionBuffer != null) {
      result.setNumberOfTransitions(mTransitionBuffer.size() / 3);
    }
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
    mTransitionBuffer = null;
    mTargetTuple = null;
    mMinimizationChain = mReductionChain = null;
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

      if (mNonblockingSupported) {
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
        new ListBufferTransitionRelation(getOutputName(),
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
          //mTransitionRelation.setPropositionUsed(markingID, false);
          if (mMinimizationChain != null) {
            mMinimizationChain.setTransitionRelation(mTransitionRelation);
            mMinimizationChain.run();
          }
          if (mSupervisorLocalizationEnabled ||
              mSupervisorReductionFactory.isSupervisedEventRequired()) {
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

  private void collectLocalizedSupervisors(final List<AutomatonProxy> supervisors)
    throws AnalysisException
  {
    int config = mReductionChain.getPreferredInputConfiguration();
    if (config == 0) {
      config = mTransitionRelation.getConfiguration();
    }
    final IsomorphismChecker checker =
      new IsomorphismChecker(getFactory(), false, false);
    for (int e = EventEncoding.NONTAU; e < mNumProperEvents; e++) {
      final byte status = mTransitionRelation.getProperEventStatus(e);
      if (EventStatus.isControllableEvent(status) && isEverDisabledEvent(e)) {
        // TODO avoid duplicate computation and duplicate supervisors if
        // several events are enabled and disabled in exactly the same states
        mReductionChain.setSupervisedEvent(e);
        final EventEncoding enc = new EventEncoding(mEventEncoding);
        final ListBufferTransitionRelation supervisor =
          new ListBufferTransitionRelation(mTransitionRelation, enc, config);
        removeOtherControllableDisablements(supervisor, e);
        final EventProxy event = mEventEncoding.getProperEvent(e);
        supervisor.setName("sup:" + event.getName());
        mReductionChain.setTransitionRelation(supervisor);
        mReductionChain.run();
        supervisor.removeDumpStateTransitions();
        supervisor.removeRedundantPropositions();
        AutomatonProxy aut = new TRAutomatonProxy(enc, supervisor);
        for (final AutomatonProxy existing : supervisors) {
          if (checker.checkBisimulation(aut, existing)) {
            aut = null;
            break;
          }
        }
        if (aut != null) {
          supervisors.add(aut);
        }
      }
    }
  }

  private boolean isEverDisabledEvent(final int event)
  {
    final int dump = mTransitionRelation.getDumpStateIndex();
    final TransitionIterator iter =
      mTransitionRelation.createAllTransitionsReadOnlyIterator(event);
    while (iter.advance()) {
      if (iter.getCurrentTargetState() == dump) {
        return true;
      }
    }
    return false;
  }

  private void removeOtherControllableDisablements
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
  /**
   * Gets the marking proposition to be used. This method returns the marking
   * proposition specified by the
   * {@link #setConfiguredDefaultMarking(EventProxy) setMarkingProposition()}
   * method, if non-null, or the default marking proposition of the input
   * model.
   *
   * @throws EventNotFoundException
   *           to indicate that the a <CODE>null</CODE> marking was specified,
   *           but input model does not contain any proposition with the
   *           default marking name.
   */
  private EventProxy getUsedDefaultMarking() throws EventNotFoundException
  {
    if (mUsedMarking == null) {
      if (mConfiguredMarking == null) {
        final ProductDESProxy model = getModel();
        mUsedMarking = AbstractConflictChecker.getMarkingProposition(model);
      } else {
        mUsedMarking = mConfiguredMarking;
      }
    }
    return mUsedMarking;
  }

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
    rel.setName(mOutputName);
    final AutomatonProxy aut =
      rel.createAutomaton(getFactory(), mEventEncoding);
    return AutomatonTools.createProductDESProxy(aut, getFactory());
  }


  //#########################################################################
  //# Inner Class AutomatonEventInfo
  private abstract class AutomatonEventInfo implements
    Comparable<AutomatonEventInfo>
  {
    public AutomatonEventInfo(final int aut, final double probability)
    {
      mAut = aut;
      mProbability = probability;
    }

    protected int getAutomaton()
    {
      return mAut;
    }

    protected double getProbability()
    {
      return mProbability;
    }

    private final int mAut;
    private final double mProbability;
  }


  //#########################################################################
  //# Inner Class UncontrollableAutomatonEventInfo
  private class UncontrollableAutomatonEventInfo extends AutomatonEventInfo
  {
    public UncontrollableAutomatonEventInfo(final int aut,
                                            final double probability)
    {
      super(aut, probability);
    }

    @Override
    public int compareTo(final AutomatonEventInfo info)
    {
      if (this.getAutomaton() < mNumPlants && this.getProbability() == 1.0f) {
        return 1;
      } else if (info.mAut < mNumPlants && info.mProbability == 1.0f) {
        return -1;
      } else if ((this.getAutomaton() < mNumPlants && info.mAut < mNumPlants)
                 || (this.getAutomaton() >= mNumPlants && info.mAut >= mNumPlants)) {
        return (this.getProbability() < info.mProbability) ? -1 : 1;
      } else {
        return (this.getAutomaton() < mNumPlants) ? -1 : 1;
      }
    }
  }


  //#########################################################################
  //# Inner Class ControllableAutomatonEventInfo
  private class ControllableAutomatonEventInfo extends AutomatonEventInfo
  {
    public ControllableAutomatonEventInfo(final int aut,
                                          final double probability)
    {
      super(aut, probability);
    }

    @Override
    public int compareTo(final AutomatonEventInfo info)
    {
      if (this.getProbability() < info.getProbability()) {
        return -1;
      } else if (this.getProbability() < info.getProbability()) {
        return 1;
      } else {
        return 0;
      }
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
      throws OverflowException, AnalysisAbortException
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
      throws OverflowException, AnalysisAbortException
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
  private EventProxy mConfiguredMarking;
  private EventProxy mUsedMarking;
  private boolean mNonblockingSupported = true;
  private SupervisorReductionFactory mSupervisorReductionFactory =
    DefaultSupervisorReductionFactory.OFF;
  private boolean mNondeterminismEnabled = false;
  private boolean mSupervisorLocalizationEnabled = false;
  private String mOutputName = "supervisor";
  private Collection<EventProxy> mDisabledEvents;

  private TransitionRelationSimplifier mMinimizationChain;
  private SupervisorReductionSimplifier mReductionChain;

  private List<AutomatonProxy> mAutomata;
  private EventEncoding mEventEncoding;
  private TIntArrayList mTransitionBuffer;

  private int mNumAutomata;
  private int mNumPlants;
  private int mNumStates;
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
