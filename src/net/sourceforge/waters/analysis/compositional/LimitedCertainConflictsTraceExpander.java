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

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;

import net.sourceforge.waters.analysis.abstraction.LimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.MemStateProxy;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * @author Robi Malik
 */

public class LimitedCertainConflictsTraceExpander extends TRTraceExpander
{

  //#######################################################################
  //# Constructors
  protected LimitedCertainConflictsTraceExpander
    (final CompositionalConflictChecker verifier,
     final LimitedCertainConflictsTRSimplifier simplifier,
     final EventProxy tau,
     final AutomatonProxy resultAut,
     final StateEncoding resultStateEnc,
     final AutomatonProxy originalAut,
     final StateEncoding originalStateEnc)
    throws AnalysisException
  {
    super(verifier, tau, null,
          resultAut, resultStateEnc, originalAut, originalStateEnc,
          null, false);
    mSimplifier = simplifier;
    final EventEncoding eventEnc = getEventEncoding();
    final EventProxy marking = verifier.getUsedDefaultMarking();
    final int markingID = eventEnc.getEventCode(marking);
    mSimplifier.setDefaultMarkingID(markingID);
    final KindTranslator translator = verifier.getKindTranslator();
    mKindTranslator = new CertainConflictsKindTranslator(translator);
    mSafetyVerifier = verifier.getCurrentCompositionalSafetyVerifier();
    mSafetyVerifier.setKindTranslator(mKindTranslator);
  }


  //#######################################################################
  //# Simple Access
  CompositionalConflictChecker getConflictChecker()
  {
    return (CompositionalConflictChecker) getModelVerifier();
  }


  //#######################################################################
  //# Invocation
  @Override
  public List<TraceStepProxy> convertTraceSteps
    (final List<TraceStepProxy> traceSteps)
    throws AnalysisException
  {
    int numTraceSteps = traceSteps.size();
    final ListIterator<TraceStepProxy> iter =
      traceSteps.listIterator(numTraceSteps);
    final TraceStepProxy lastStep = iter.previous();
    final AutomatonProxy resultAut = getResultAutomaton();
    final Map<AutomatonProxy,StateProxy> lastStepMap = lastStep.getStateMap();
    mReferenceAutomata = new ArrayList<AutomatonProxy>(lastStepMap.keySet());
    final StateProxy lastState = lastStepMap.get(resultAut);
    List<TraceStepProxy> newTraceSteps;
    int initResult;
    if (isDumpState(resultAut, lastState)) {
      // Trace goes into certain conflicts.
      // Searching starts from the step before entering certain conflicts.
      // Everything afterwards gets removed so it can be replaced
      // by something in the real certain conflicts.
      // Exception is the initial state, when it is certain conflict:
      // then searching starts from all initial states.
      numTraceSteps--;
      initResult = -1;
      mLastTraceStep = null;
      while (iter.hasPrevious()) {
        final TraceStepProxy predStep = iter.previous();
        final Map<AutomatonProxy,StateProxy> predMap = predStep.getStateMap();
        final StateProxy predState = predMap.get(resultAut);
        if (!isDumpState(resultAut, predState)) {
          initResult = getResultAutomatonStateCode(predState);
          mLastTraceStep = predStep;
          break;
        }
        numTraceSteps--;
      }
      newTraceSteps = new ArrayList<TraceStepProxy>(numTraceSteps);
      for (final TraceStepProxy step : traceSteps) {
        if (numTraceSteps-- == 0) {
          break;
        }
        newTraceSteps.add(step);
      }
    } else {
      // Trace does not go into certain conflicts.
      // We still must try to extend it into certain conflicts ...
      mLastTraceStep = lastStep;
      initResult = getResultAutomatonStateCode(lastState);
      newTraceSteps = new ArrayList<TraceStepProxy>(traceSteps);
    }

    // Rerun certain conflicts simplifier
    // to obtain partition and level information ...
    final int config = mSimplifier.getPreferredInputConfiguration();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    ListBufferTransitionRelation copy =
      new ListBufferTransitionRelation(rel, config);
    mSimplifier.setTransitionRelation(copy);
    mSimplifier.setAppliesPartitionAutomatically(false);
    mSimplifier.run();
    copy = null;
    mSimplifier.setTransitionRelation(rel);
    final TRPartition partition = mSimplifier.getResultPartition();
    final int initTest;
    if (partition == null || initResult < 0) {
      initTest = initResult;
    } else {
      final int[] clazz = partition.getStates(initResult);
      assert clazz.length == 1 :
        "Non-certain-conflict state merged into multiple states?";
      initTest = clazz[0];
    }

    // Extend the trace to lowest (= most blocking) possible level ...
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    mCheckedProposition =
      factory.createEventProxy(":certainconf", EventKind.UNCONTROLLABLE);
    int level = mSimplifier.getMaxLevel() | 1;
    SafetyTraceData foundData = null;
    do {
      final SafetyTraceData newData = findNextLevel(initTest, level);
      if (newData == null) {
        break;
      }
      foundData = newData;
      level = (foundData.getLevel() | 1) - 2;
    } while (level >= 0);

    // Fix the initial segment of the trace ...
    final int ccState =
      foundData == null ? -1 : foundData.getTestAutomatonEndState();
    newTraceSteps = relabelInitialTraceSteps(newTraceSteps, partition, ccState);

    // Add the search results to the end of the trace ...
    if (foundData != null) {
      final List<TraceStepProxy> additionalSteps =
        foundData.getAdditionalSteps();
      newTraceSteps.addAll(additionalSteps);
      level = foundData.getLevel();
      if ((level & 1) == 1) {
        // Odd level indicates tau-transitions can go one level further ...
        final int numExtendedSteps = newTraceSteps.size();
        final ListIterator<TraceStepProxy> extendedIter =
          newTraceSteps.listIterator(numExtendedSteps);
        final TraceStepProxy extendedLastStep = extendedIter.previous();
        final List<TraceStepProxy> tauSteps =
          getAdditionalTauSteps(extendedLastStep, level - 1);
        newTraceSteps.addAll(tauSteps);
      }
    }
    return newTraceSteps;
  }


  //#######################################################################
  //# Transition Relation Search
  private boolean isDumpState(final AutomatonProxy aut,
                              final StateProxy state)
  {
    final CompositionalConflictChecker verifier = getConflictChecker();
    final EventProxy marking = verifier.getUsedDefaultMarking();
    if (!aut.getEvents().contains(marking) ||
        state.getPropositions().contains(marking)) {
      return false;
    } else {
      for (final TransitionProxy trans : aut.getTransitions()) {
        if (trans.getSource() == state) {
          return false;
        }
      }
      return true;
    }
  }


  //#########################################################################
  //# Trace Extension
  private List<TraceStepProxy> relabelInitialTraceSteps
    (final List<TraceStepProxy> steps,
     final TRPartition partition,
     final int ccState)
  {
    final int numSteps = steps.size();
    final List<TraceStepProxy> newSteps =
      new ArrayList<TraceStepProxy>(numSteps);
    for (final TraceStepProxy step : steps) {
      final TraceStepProxy newStep =
        relabelInitialTraceStep(step, partition, ccState);
      newSteps.add(newStep);
    }
    return newSteps;
  }

  private TraceStepProxy relabelInitialTraceStep
    (final TraceStepProxy resultStep,
     final TRPartition partition,
     final int ccState)
  {
    final AutomatonProxy resultAut = getResultAutomaton();
    final Map<AutomatonProxy,StateProxy> resultMap = resultStep.getStateMap();
    final int size = resultMap.size();
    final Map<AutomatonProxy,StateProxy> origMap =
      new HashMap<AutomatonProxy,StateProxy>(size);
    for (final Map.Entry<AutomatonProxy,StateProxy> entry :
         resultMap.entrySet()) {
      final AutomatonProxy aut = entry.getKey();
      final StateProxy state = entry.getValue();
      if (aut == resultAut) {
        final AutomatonProxy origAut = getOriginalAutomaton();
        final int resultCode = getResultAutomatonStateCode(state);
        final int origCode;
        if (partition == null) {
          origCode = resultCode;
        } else {
          final int[] clazz = partition.getStates(resultCode);
          origCode = clazz.length == 1 ? clazz[0] : ccState;
        }
        assert origCode >= 0;
        final StateProxy origState = getOriginalAutomatonState(origCode);
        origMap.put(origAut, origState);
      } else {
        origMap.put(aut, state);
      }
    }
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final EventProxy event = resultStep.getEvent();
    return factory.createTraceStepProxy(event, origMap);
  }

  private SafetyTraceData findNextLevel(final int initTest, final int level)
    throws AnalysisException
  {
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final EventEncoding eventEnc = getEventEncoding();
    final StateEncoding testStateEnc = new StateEncoding();
    final AutomatonProxy testAut = mSimplifier.createTestAutomaton
      (factory, eventEnc, testStateEnc,
       initTest, mCheckedProposition, level);
    final ProductDESProxy des = createLanguageInclusionModel(testAut);
    mSafetyVerifier.setModel(des);
    //MarshallingTools.saveModule(des, "xxx.wmod");
    if (mSafetyVerifier.run()) {
      return null;
    } else {
      return new SafetyTraceData(testAut, testStateEnc);
    }
  }

  private List<TraceStepProxy> getAdditionalTauSteps
    (final TraceStepProxy startStep, final int level)
  {
    final AutomatonProxy origAut = getOriginalAutomaton();
    final Map<AutomatonProxy,StateProxy> stepMap = startStep.getStateMap();
    final StateProxy initState = stepMap.get(origAut);
    int scode = getOriginalAutomatonStateCode(initState);

    final int tau = EventEncoding.TAU;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final int size = rel.getNumberOfStates();
    final TIntHashSet visited = new TIntHashSet(size);
    visited.add(scode);
    SearchRecord record = new SearchRecord(scode);
    final Queue<SearchRecord> queue = new ArrayDeque<SearchRecord>();
    queue.add(record);
    search:
    while (true) {
      record = queue.remove();
      scode = record.getState();
      final int slevel = mSimplifier.getLevel(scode);
      iter.reset(scode, tau);
      while (iter.advance()) {
        final int tcode = iter.getCurrentTargetState();
        if (visited.add(tcode)) {
          final int tlevel = mSimplifier.getLevel(tcode);
          if (tlevel < 0 || tlevel > slevel) {
            continue;
          }
          final SearchRecord newRecord =
            new SearchRecord(tcode, 0, tau, record);
          if (tlevel <= level) {
            record = newRecord;
            break search;
          }
          queue.add(newRecord);
        }
      }
    }

    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final List<TraceStepProxy> additionalSteps =
      new LinkedList<TraceStepProxy>();
    final EventProxy tauEvent = getTauEvent();
    final Map<AutomatonProxy,StateProxy> workMap =
      new HashMap<AutomatonProxy,StateProxy>(stepMap);
    do {
      scode = record.getState();
      final StateProxy state = getOriginalAutomatonState(scode);
      workMap.put(origAut, state);
      final TraceStepProxy step =
        factory.createTraceStepProxy(tauEvent, workMap);
      additionalSteps.add(0, step);
      record = record.getPredecessor();
    } while (record.getPredecessor() != null);
    return additionalSteps;
  }


  //#########################################################################
  //# Language Inclusion Model
  private ProductDESProxy createLanguageInclusionModel
    (final AutomatonProxy testAut)
  {
    final int numAutomata = mReferenceAutomata.size();
    int numStates = 0;
    for (final AutomatonProxy aut : mReferenceAutomata) {
      numStates += aut.getStates().size();
    }
    mReverseStateMap = new HashMap<StateProxy,StateProxy>(numStates);
    final List<AutomatonProxy> checkAutomata =
      new ArrayList<AutomatonProxy>(numAutomata + 1);
    final AutomatonProxy resultAut = getResultAutomaton();
    final Map<AutomatonProxy,StateProxy> stepMap =
      mLastTraceStep == null ? null : mLastTraceStep.getStateMap();
    final Collection<EventProxy> ccevents = testAut.getEvents();
    final Collection<EventProxy> events = new THashSet<EventProxy>(ccevents);
    for (final AutomatonProxy aut : mReferenceAutomata) {
      if (aut == resultAut) {
        checkAutomata.add(testAut);
      } else {
        final StateProxy init = stepMap == null ? null : stepMap.get(aut);
        final AutomatonProxy checkAut =
          createLanguageInclusionAutomaton(aut, init);
        checkAutomata.add(checkAut);
        final Collection<EventProxy> local = checkAut.getEvents();
        events.addAll(local);
      }
    }
    if (mPropertyAutomaton == null) {
      mPropertyAutomaton = createPropertyAutomaton();
    }
    checkAutomata.add(mPropertyAutomaton);
    final List<EventProxy> eventList = new ArrayList<EventProxy>(events);
    Collections.sort(eventList);
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final String name = resultAut.getName();
    final String comment =
      "Automatically generated to expand conflict error trace for '" +
      name + "' from certain conflicts to blocking state.";
    final ProductDESProxy des =
      factory.createProductDESProxy(name, comment, null,
                                    eventList, checkAutomata);
    return des;
  }

  private AutomatonProxy createLanguageInclusionAutomaton
    (final AutomatonProxy aut, final StateProxy initState)
  {
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final Collection<EventProxy> oldevents = aut.getEvents();
    final int numevents = oldevents.size();
    final Collection<EventProxy> newevents =
      new ArrayList<EventProxy>(numevents);
    for (final EventProxy event : oldevents) {
      if (mKindTranslator.getEventKind(event) != EventKind.PROPOSITION) {
        newevents.add(event);
      }
    }
    final Collection<StateProxy> oldstates = aut.getStates();
    final int numstates = oldstates.size();
    final Collection<StateProxy> newstates =
      new ArrayList<StateProxy>(numstates);
    final Map<StateProxy,StateProxy> statemap =
      new HashMap<StateProxy,StateProxy>(numstates);
    for (final StateProxy oldstate : oldstates) {
      final String statename = oldstate.getName();
      final boolean init =
        initState == null ? oldstate.isInitial() : oldstate == initState;
      final StateProxy newstate =
        factory.createStateProxy(statename, init, null);
      newstates.add(newstate);
      statemap.put(oldstate, newstate);
      mReverseStateMap.put(newstate, oldstate);
    }
    final Collection<TransitionProxy> oldtransitions = aut.getTransitions();
    final int numtrans = oldtransitions.size();
    final Collection<TransitionProxy> newtransitions =
        new ArrayList<TransitionProxy>(numtrans);
    for (final TransitionProxy oldtrans : oldtransitions) {
      final StateProxy oldsource = oldtrans.getSource();
      final StateProxy newsource = statemap.get(oldsource);
      final StateProxy oldtarget = oldtrans.getTarget();
      final StateProxy newtarget = statemap.get(oldtarget);
      final EventProxy event = oldtrans.getEvent();
      final TransitionProxy newtrans =
        factory.createTransitionProxy(newsource, event, newtarget);
      newtransitions.add(newtrans);
    }
    final String autname = aut.getName();
    return factory.createAutomatonProxy
      (autname, ComponentKind.PLANT, newevents, newstates, newtransitions);
  }

  private AutomatonProxy createPropertyAutomaton()
  {
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final String name = ":never";
    final ComponentKind kind = ComponentKind.PROPERTY;
    final Collection<EventProxy> events =
      Collections.singletonList(mCheckedProposition);
    final StateProxy state = new MemStateProxy(0, true);
    final Collection<StateProxy> states = Collections.singletonList(state);
    return factory.createAutomatonProxy(name, kind, events, states, null);
  }


  //#########################################################################
  //# Inner Class SafetyTraceData
  private class SafetyTraceData
  {
    //#######################################################################
    //# Constructor
    private SafetyTraceData(final AutomatonProxy testAut,
                            final StateEncoding stateEnc)
    {
      mCheckAutomata = mSafetyVerifier.getModel().getAutomata();
      mCheckStateMap = mReverseStateMap;
      mCounterExample = mSafetyVerifier.getCounterExample();
      mTestAutomaton = testAut;
      mTestAutomatonStateEncoding = stateEnc;
      final int state = getTestAutomatonEndState();
      mLevel = mSimplifier.getLevel(state);
      assert mLevel >= 0;
    }

    //#######################################################################
    //# Simple Access
    private int getLevel()
    {
      return mLevel;
    }

    //#######################################################################
    //# Trace Expansion
    private int getTestAutomatonEndState()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final TraceProxy trace = mCounterExample.getTrace();
      final List<TraceStepProxy> steps = trace.getTraceSteps();
      final int numSteps = steps.size();
      final ListIterator<TraceStepProxy> iter = steps.listIterator(numSteps);
      int current = -1;
      back:
      do {
        final TraceStepProxy step = iter.previous();
        final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
        final StateProxy state = stepMap.get(mTestAutomaton);
        if (state != null) {
          current = mTestAutomatonStateEncoding.getStateCode(state);
          break;
        } else if (step.getEvent() == null) {
          final int numStates = rel.getNumberOfStates();
          for (int s = 0; s < numStates; s++) {
            if (rel.isInitial(s)) {
              current = s;
              break back;
            }
          }
        }
      } while (current < 0);
      iter.next();  // Discard result from previous() above
      final TransitionIterator transIter =
        rel.createSuccessorsReadOnlyIterator();
      final EventEncoding eventEnc = getEventEncoding();
      while (iter.hasNext()) {
        final TraceStepProxy step = iter.next();
        final EventProxy event = step.getEvent();
        final int e = eventEnc.getEventCode(event);
        if (e >= 0) {
          transIter.reset(current, e);
          transIter.advance();
          current = transIter.getCurrentTargetState();
        }
      }
      return current;
    }

    private List<TraceStepProxy> getAdditionalSteps()
      throws AnalysisException
    {
      final TraceProxy trace = mCounterExample.getTrace();
      final List<TraceStepProxy> steps = trace.getTraceSteps();
      final List<TraceStepProxy> saturatedSteps =
        getSaturatedTraceSteps(steps, mCheckAutomata);
      final List<TraceStepProxy> relabelledSteps =
        relabelSafetyTraceSteps(saturatedSteps);
      return relabelledSteps;
    }

    private List<TraceStepProxy> relabelSafetyTraceSteps
      (final List<TraceStepProxy> steps)
    {
      final Iterator<TraceStepProxy> iter = steps.iterator();
      int numSteps = steps.size() - 1; // skip last ...
      if (mLastTraceStep != null) {
        iter.next(); // skip first if continuing from a nonempty trace ...
        numSteps--;
      }
      final List<TraceStepProxy> newsteps =
        new ArrayList<TraceStepProxy>(numSteps);
      for (int i = 0; i < numSteps; i++) {
        final TraceStepProxy oldstep = iter.next();
        final TraceStepProxy newstep = relabelSafetyTraceStep(oldstep);
        newsteps.add(newstep);
      }
      return newsteps;
    }

    private TraceStepProxy relabelSafetyTraceStep
      (final TraceStepProxy checkStep)
    {
      final Map<AutomatonProxy,StateProxy> checkMap = checkStep.getStateMap();
      final int size = checkMap.size();
      final Map<AutomatonProxy,StateProxy> refMap =
        new HashMap<AutomatonProxy,StateProxy>(size);
      final Iterator<AutomatonProxy> checkIter = mCheckAutomata.iterator();
      final Iterator<AutomatonProxy> refIter = mReferenceAutomata.iterator();
      while (refIter.hasNext()) {
        final AutomatonProxy checkAut = checkIter.next();
        final AutomatonProxy refAut = refIter.next();
        final StateProxy checkState = checkMap.get(checkAut);
        if (checkAut == mTestAutomaton) {
          final AutomatonProxy origAut = getOriginalAutomaton();
          final int stateCode =
            mTestAutomatonStateEncoding.getStateCode(checkState);
          final StateProxy origState = getOriginalAutomatonState(stateCode);
          refMap.put(origAut, origState);
        } else {
          final StateProxy refState = mCheckStateMap.get(checkState);
          refMap.put(refAut, refState);
        }
      }
      final AbstractCompositionalModelVerifier verifier = getModelVerifier();
      final ProductDESProxyFactory factory = verifier.getFactory();
      final EventProxy event = checkStep.getEvent();
      return factory.createTraceStepProxy(event, refMap);
    }

    //#######################################################################
    //# Data Members
    private final SafetyCounterExampleProxy mCounterExample;
    private final Collection<AutomatonProxy> mCheckAutomata;
    /**
     * The reverse state map that was used when creating the language
     * inclusion model. See {@link #mReverseStateMap}.
     */
    private final Map<StateProxy,StateProxy> mCheckStateMap;
    private final AutomatonProxy mTestAutomaton;
    private final StateEncoding mTestAutomatonStateEncoding;
    private final int mLevel;
  }


  //#########################################################################
  //# Inner Class CertainConflictsKindTranslator
  private class CertainConflictsKindTranslator
    implements KindTranslator
  {

    //#######################################################################
    //# Constructor
    private CertainConflictsKindTranslator(final KindTranslator parent)
    {
      mParentKindTranslator = parent;
    }

    //#######################################################################
    //# Data Members
    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (aut == mPropertyAutomaton) {
        return ComponentKind.SPEC;
      } else {
        return ComponentKind.PLANT;
      }
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      if (mParentKindTranslator.getEventKind(event) == EventKind.PROPOSITION) {
        return EventKind.PROPOSITION;
      } else {
        return EventKind.UNCONTROLLABLE;
      }
    }

    //#######################################################################
    //# Data Members
    private final KindTranslator mParentKindTranslator;
  }


  //#######################################################################
  //# Data Members
  private final LimitedCertainConflictsTRSimplifier mSimplifier;
  private final KindTranslator mKindTranslator;
  private final SafetyVerifier mSafetyVerifier;

  /**
   * The last step of the counterexample being converted, which includes
   * a state outside of certain conflicts in the abstracted automaton.
   * This may be <CODE>null</CODE> in the case of an abstracted automaton
   * that consists just of a single certain-conflicts state.
   */
  private TraceStepProxy mLastTraceStep;
  /**
   * An event used to mark certain conflict states with selfloops in the
   * language inclusion check to determine whether certain conflicts are
   * reachable.
   */
  private EventProxy mCheckedProposition;
  /**
   * A one-state automaton that blocks the {@link #mCheckedProposition}.
   */
  private AutomatonProxy mPropertyAutomaton;
  /**
   * List of automata in the trace to be converted. Contains the abstracted
   * automaton plus any other automata in the model.
   */
  private List<AutomatonProxy> mReferenceAutomata;
  /**
   * A map used when creating the language inclusion model. It relates
   * states of the automata in the language inclusion model to states
   * of the automata in {@link #mReferenceAutomata}.
   */
  private Map<StateProxy,StateProxy> mReverseStateMap;

}
