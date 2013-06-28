//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CertainConflictsTraceExpander
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.certainconf.CertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.MemStateProxy;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Jared Lindsey
 */

public class CertainConflictsTraceExpander extends TRTraceExpander
{

  //#######################################################################
  //# Constructors
  protected CertainConflictsTraceExpander(final CompositionalConflictChecker verifier,
                                          final CertainConflictsTRSimplifier simplifier,
                                          final EventProxy tau,
                                          final AutomatonProxy resultAut,
                                          final StateEncoding resultStateEnc,
                                          final AutomatonProxy originalAut,
                                          final StateEncoding originalStateEnc)
    throws AnalysisException
  {
    super(verifier, tau, null, resultAut, resultStateEnc, originalAut,
          originalStateEnc, null, false);
    mSimplifier = simplifier;
    final EventEncoding eventEnc = getEventEncoding();
    final EventProxy marking = verifier.getUsedDefaultMarking();
    final int markingID = eventEnc.getEventCode(marking);
    mSimplifier.setDefaultMarkingID(markingID);
    final KindTranslator translator = verifier.getKindTranslator();
    mKindTranslator = new CertainConflictsKindTranslator(translator);
    mSafetyVerifier = verifier.getCurrentCompositionalSafetyVerifier();
    mSafetyVerifier.setKindTranslator(mKindTranslator);
    mLevels = null;
    mCertainConfRel = null;
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
  public List<TraceStepProxy> convertTraceSteps(final List<TraceStepProxy> traceSteps)
    throws AnalysisException
  {
    final int config = mSimplifier.getPreferredInputConfiguration();
    final ListBufferTransitionRelation rel = getTransitionRelation();

    mCertainConfRel = new ListBufferTransitionRelation(rel, config);
    mSimplifier.setTransitionRelation(mCertainConfRel);
    mLevels = mSimplifier.runForCE();

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

    int initTest = initResult;
    if (initResult > 0 && mSimplifier.getWasOptimisationUsed()) {
      initTest = FindTestAutomatonInit(initResult, newTraceSteps);
    }

    // Extend the trace to lowest (= most blocking) possible level ...
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    mTestAutomatonStateEncoding = new StateEncoding();
    mCheckedProposition =
      factory.createEventProxy(":certainconf", EventKind.UNCONTROLLABLE);
    List<TraceStepProxy> additionalSteps = null;
    final int max = getMaxLevel();
    int level = max;
    if (initTest != -1 && mLevels[initTest] != -1) {
      level = mLevels[initTest];
    }
    int finalTestState = initTest;
    do {
      final int tempState = findNextLevel(initTest, level);
      level = tempState == -1 ? -1 : mLevels[tempState];
      if (level >= 0) {
        additionalSteps = getAdditionalSteps();
        finalTestState = tempState;
      }
      level--;
      mTestAutomatonStateEncoding.clear();
    } while (level > -1);

    // Add the search results to the end of the trace ...
    if (additionalSteps != null) {
      newTraceSteps.addAll(additionalSteps);
    }
    // get int set from Simplifier
    final int[] stateSet = mSimplifier.getStateSet(finalTestState);
    mOriginalTraceEndState = stateSet[0];
    final TIntArrayList crucialEvents = getEventSteps(newTraceSteps);
    final SearchRecord endRecord = convertEventSteps(crucialEvents);
    final List<SearchRecord> convertedSteps = endRecord.getTrace();
    mergeTraceSteps(newTraceSteps, convertedSteps);
    return newTraceSteps;
  }

  private int FindTestAutomatonInit(final int initResult,
                                    final List<TraceStepProxy> traceSteps)
  {
    // iterate trace through mTestAutomaton
    // get events
    final TIntArrayList events = getEventSteps(traceSteps);
    int resultState = -1;

    // 1. Collect initial states
    final ListBufferTransitionRelation rel = mCertainConfRel;
    final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
    final Set<SearchRecord> visited = new THashSet<SearchRecord>();
    final int numStates = rel.getNumberOfStates();

    for (int state = 0; state < numStates; state++) {
      if (rel.isInitial(state)) {
        final SearchRecord record = new SearchRecord(state);
        visited.add(record);
        open.add(record);
      }
    }

    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    back: while (true) {

      final SearchRecord current = open.remove();
      final int state = current.getState();
      final int depth = current.getDepth();

      if (!events.isEmpty() && depth != events.size()) {
        final int nextdepth = depth + 1;
        final int event = events.get(depth);
        iter.reset(state, event);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          final SearchRecord next =
            new SearchRecord(target, nextdepth, event, current);
          if (nextdepth == events.size()) {
            resultState = target;
            break back;
          } else if (visited.add(next)) {
            open.add(next);
          }
        }
      } else {
        resultState = 0;
        break back;
      }
    }
    // now have automaton number. get the set
    final ArrayList<Integer> stateSet =
      mSimplifier.getStateSetArrayList(resultState);
    // shuffle until initResult is at the front
    Collections.swap(stateSet, 0, stateSet.indexOf(initResult));
    Collections.sort(stateSet.subList(1, stateSet.size()));

    final int[] orderedStateSet = new int[stateSet.size()];
    for (int i = 0; i < stateSet.size(); i++) {
      orderedStateSet[i] = stateSet.get(i);
    }
    final int temp = mSimplifier.findStateFromSet(orderedStateSet);
    return temp;
  }

  private TIntArrayList getEventSteps(final List<TraceStepProxy> traceSteps)
  {
    final EventEncoding enc = getEventEncoding();
    final int len = traceSteps.size();
    final TIntArrayList eventSteps = new TIntArrayList(len);
    if (len == 0)
      return eventSteps;
    final Iterator<TraceStepProxy> iter = traceSteps.iterator();
    TraceStepProxy step = iter.next();
    while (iter.hasNext()) {
      step = iter.next();
      final EventProxy event = step.getEvent();
      final int eventID = enc.getEventCode(event);
      if (eventID <= 0) {
        // Step of another automaton only or tau --- skip.
      } else {
        // Step by a proper event ---
        eventSteps.add(eventID);
      }
    }

    return eventSteps;
  }

  private SearchRecord convertEventSteps(final TIntArrayList eventSteps)
  {
    // 1. Collect initial states
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
    final Set<SearchRecord> visited = new THashSet<SearchRecord>();
    final int numStates = rel.getNumberOfStates();

    for (int state = 0; state < numStates; state++) {
      if (rel.isInitial(state)) {
        final SearchRecord record = new SearchRecord(state);
        if (state == mOriginalTraceEndState) {
          return record;

        }
        visited.add(record);
        open.add(record);
      }
    }
    // 2. Breadth-first search
    final int tau = EventEncoding.TAU;
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    while (true) {

      final SearchRecord current = open.remove();
      final int state = current.getState();
      final int depth = current.getDepth();

      if (!eventSteps.isEmpty() && depth != eventSteps.size()) {
        final int nextdepth = depth + 1;
        final int event = eventSteps.get(depth);
        iter.reset(state, event);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          final SearchRecord next =
            new SearchRecord(target, nextdepth, event, current);
          if (target == mOriginalTraceEndState && nextdepth == eventSteps.size()) {
            return next;
          } else if (visited.add(next)) {
            open.add(next);
          }
        }
      }

      iter.reset(state, tau);
      while (iter.advance()) {
        final int target = iter.getCurrentTargetState();
        final SearchRecord next =
          new SearchRecord(target, depth, tau, current);
        if (target == mOriginalTraceEndState) {
          return next;
        } else if (visited.add(next)) {
          open.add(next);
        }
      }
    }
  }

  //#######################################################################
  //# Transition Relation Search
  private boolean isDumpState(final AutomatonProxy aut, final StateProxy state)
  {
    final CompositionalConflictChecker verifier = getConflictChecker();
    final EventProxy marking = verifier.getUsedDefaultMarking();
    if (!aut.getEvents().contains(marking)
        || state.getPropositions().contains(marking)) {
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

  private int findNextLevel(final int initTest, final int level)
    throws AnalysisException
  {
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final EventEncoding eventEnc = getEventEncoding();
    mTestAutomaton =
      mSimplifier.createTestAutomaton(factory, eventEnc,
                                      mTestAutomatonStateEncoding, initTest,
                                      mCheckedProposition, level);
    final ProductDESProxy des = createLanguageInclusionModel();
    mSafetyVerifier.setModel(des);
    if (mSafetyVerifier.run()) {
      return -1;
    } else {
      final SafetyTraceProxy trace = mSafetyVerifier.getCounterExample();
      final int state = getTestAutomatonEndState(trace);
      return state;
    }
  }

  private int getMaxLevel()
  {
    int max = mLevels[0];
    for (int i = 0; i < mLevels.length; i++) {
      if (mLevels[i] > max)
        max = mLevels[i];
    }
    return max;
  }

  private int getTestAutomatonEndState(final List<TraceStepProxy> steps)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();

    final int numSteps = steps.size();
    final ListIterator<TraceStepProxy> iter = steps.listIterator(numSteps);
    int current = -1;
    back: do {
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
    final TransitionIterator transIter =
      rel.createSuccessorsReadOnlyIterator();
    final EventEncoding eventEnc = getEventEncoding();
    iter.next();
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

  private int getTestAutomatonEndState(final SafetyTraceProxy trace)
  {
    return getTestAutomatonEndState(trace.getTraceSteps());
  }

  private List<TraceStepProxy> getAdditionalSteps() throws AnalysisException
  {
    final SafetyTraceProxy trace = mSafetyVerifier.getCounterExample();
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    final List<TraceStepProxy> saturatedSteps =
      getSaturatedTraceSteps(steps, mCheckAutomata);
    final List<TraceStepProxy> relabelledSteps =
      relabelSafetyTraceSteps(saturatedSteps);
    return relabelledSteps;
  }

  private List<TraceStepProxy> relabelSafetyTraceSteps(final List<TraceStepProxy> steps)
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

  private TraceStepProxy relabelSafetyTraceStep(final TraceStepProxy checkStep)
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
      if (checkAut != mTestAutomaton) {
        final StateProxy refState = mReverseStateMap.get(checkState);
        refMap.put(refAut, refState);
      }
    }
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final EventProxy event = checkStep.getEvent();
    return factory.createTraceStepProxy(event, refMap);
  }

  //#########################################################################
  //# Language Inclusion Model
  private ProductDESProxy createLanguageInclusionModel()
  {
    final int numAutomata = mReferenceAutomata.size();
    int numStates = 0;
    for (final AutomatonProxy aut : mReferenceAutomata) {
      numStates += aut.getStates().size();
    }
    mReverseStateMap = new HashMap<StateProxy,StateProxy>(numStates);
    mCheckAutomata = new ArrayList<AutomatonProxy>(numAutomata + 1);
    final AutomatonProxy resultAut = getResultAutomaton();
    final Map<AutomatonProxy,StateProxy> stepMap =
      mLastTraceStep == null ? null : mLastTraceStep.getStateMap();
    final Collection<EventProxy> ccevents = mTestAutomaton.getEvents();
    final Collection<EventProxy> events = new THashSet<EventProxy>(ccevents);
    for (final AutomatonProxy aut : mReferenceAutomata) {
      if (aut == resultAut) {
        mCheckAutomata.add(mTestAutomaton);
      } else {
        final StateProxy init = stepMap == null ? null : stepMap.get(aut);
        final AutomatonProxy checkAut =
          createLanguageInclusionAutomaton(aut, init);
        mCheckAutomata.add(checkAut);
        final Collection<EventProxy> local = checkAut.getEvents();
        events.addAll(local);
      }
    }
    if (mPropertyAutomaton == null) {
      mPropertyAutomaton = createPropertyAutomaton();
    }
    mCheckAutomata.add(mPropertyAutomaton);
    final List<EventProxy> eventList = new ArrayList<EventProxy>(events);
    Collections.sort(eventList);
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final String name = resultAut.getName();
    final String comment =
      "Automatically generated to expand conflict error trace for '" + name
        + "' from certain conflicts to blocking state.";
    final ProductDESProxy des =
      factory.createProductDESProxy(name, comment, null, eventList,
                                    mCheckAutomata);
    return des;
  }

  private AutomatonProxy createLanguageInclusionAutomaton(final AutomatonProxy aut,
                                                          final StateProxy initState)
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
    return factory.createAutomatonProxy(autname, ComponentKind.PLANT,
                                        newevents, newstates, newtransitions);
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
  //# Inner Class CertainConflictsKindTranslator
  private class CertainConflictsKindTranslator implements KindTranslator
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
  private final CertainConflictsTRSimplifier mSimplifier;
  private final KindTranslator mKindTranslator;
  private final SafetyVerifier mSafetyVerifier;

  private TraceStepProxy mLastTraceStep;
  private EventProxy mCheckedProposition;
  private AutomatonProxy mTestAutomaton;
  private StateEncoding mTestAutomatonStateEncoding;
  private AutomatonProxy mPropertyAutomaton;
  private List<AutomatonProxy> mReferenceAutomata;
  private List<AutomatonProxy> mCheckAutomata;
  private Map<StateProxy,StateProxy> mReverseStateMap;
  private int[] mLevels;
  private ListBufferTransitionRelation mCertainConfRel;
  private int mOriginalTraceEndState;
}

