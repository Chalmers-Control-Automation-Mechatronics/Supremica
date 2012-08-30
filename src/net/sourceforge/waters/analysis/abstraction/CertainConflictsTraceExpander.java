package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
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
import net.sourceforge.waters.model.analysis.SafetyVerifier;
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
    logging = false;
  }

  //#######################################################################
  //# Simple Access
  CompositionalConflictChecker getConflictChecker()
  {
    return (CompositionalConflictChecker) getModelVerifier();
  }

  private void l(final String s)
  {
    if (logging)
      System.out.println(s);
  }

  //#######################################################################
  //# Invocation
  @Override
  public List<TraceStepProxy> convertTraceSteps(final List<TraceStepProxy> traceSteps)
    throws AnalysisException
  {
    ///////////////////////////////////////////
    final int config = mSimplifier.getPreferredInputConfiguration();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    l("Converting trace steps for " + rel.getName());
    l("rel has " + rel.getNumberOfReachableStates()
      + " states (it is the Original Automaton)");
    mCertainConfRel = new ListBufferTransitionRelation(rel, config);
    mSimplifier.setTransitionRelation(mCertainConfRel);
    mLevels = mSimplifier.runForCE();
    l(mCertainConfRel + "");
    l("mCertainConfRel has " + mCertainConfRel.getNumberOfReachableStates()
      + " states");
    l("Ran simplifier again, have Levels array");
    ///////////////////////////////////////////
    final int numTraceSteps = traceSteps.size();
    l("initially, numTraceSteps = " + numTraceSteps);
    l("initial tracesteps: " + traceSteps);
    final ListIterator<TraceStepProxy> iter =
      traceSteps.listIterator(numTraceSteps);
    l("iterate through these steps");
    final TraceStepProxy lastStep = iter.previous();
    l("lastStep: " + lastStep);
    final AutomatonProxy resultAut = getResultAutomaton();
    l("retrieved result automaton");
    final Map<AutomatonProxy,StateProxy> lastStepMap = lastStep.getStateMap();
    mReferenceAutomata = new ArrayList<AutomatonProxy>(lastStepMap.keySet());
    final StateProxy lastState = lastStepMap.get(resultAut);
    l("lastState: " + lastState);
    final List<TraceStepProxy> newTraceSteps =
      new ArrayList<TraceStepProxy>(traceSteps);
    final int initResult;
    if (isDumpState(resultAut, lastState)) {
      l("Trace goes into certain conflicts.");
      if (iter.hasPrevious()) {
        // Remove the last step of the trace, so it can be replaced
        // by something in the real certain conflicts ...
        // Searching starts from the step before this ...
        mLastTraceStep = iter.previous();
        l("now LastTraceStep is: " + mLastTraceStep);
        final Map<AutomatonProxy,StateProxy> predMap =
          mLastTraceStep.getStateMap();
        final StateProxy predState = predMap.get(resultAut);
        l("thus predState: " + predState);
        initResult = getResultAutomatonStateCode(predState);
        l("which is state number " + initResult + " in the result Automaton");
      } else {
        l("step is a dump and has no previous, so it is just nothing");
        // Starting from initial state, which is certain conflict ...
        // Searching starts from all initial states ...
        mLastTraceStep = null;
        initResult = -1;
        l("so initResult = -1");
      }
      newTraceSteps.remove(numTraceSteps - 1);
    } else {
      l("Trace does not go into certain conflicts.");
      // We still must try to extend it into certain conflicts ...
      mLastTraceStep = lastStep;
      l("now LastTraceStep is: " + mLastTraceStep);
      initResult = getResultAutomatonStateCode(lastState);
      l("which is state number " + initResult + " in the result Automaton");
    }
    //////////////////////////////////////////////////////////////
    // Rerun certain conflicts simplifier
    // to obtain partition and level information ...

    //final List<int[]> partition = mSimplifier.getResultPartition();
    //final int[] stateMap = createStateMap(partition);
    //final int initTest =
    //   stateMap == null || initResult < 0 ? initResult : stateMap[initResult];
    // newTraceSteps = relabelInitialTraceSteps(newTraceSteps, stateMap);

    // assume inittest == intresult for now
    int initTest = initResult;
    if (initResult > 0 && mSimplifier.getWasOptimisationUsed()) {
      l("Optimisation was used");
      initTest = FindTestAutomatonInit(initResult, newTraceSteps);
    }

    l("set initial state of test automaton to be " + initTest);
    // Extend the trace to lowest (= most blocking) possible level ...
    l("now we try extend the trace to the lowest possible level");
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    mTestAutomatonStateEncoding = new StateEncoding();
    mCheckedProposition =
      factory.createEventProxy(":certainconf", EventKind.UNCONTROLLABLE);
    List<TraceStepProxy> additionalSteps = null;
    final int max = getMaxLevel();
    l("the Maximum level is " + max);
    int level = max;
    if (initTest != -1 && mLevels[initTest] != -1) {
      level = mLevels[initTest];
    }
    //int level = initTest == -1 ? max : Math.min(max, mLevels[initTest]);
    l("initialising level to " + level);
    // int foundLevel = 0;
    int finalTestState = initTest;
    do {
      l("See if we can reach level " + level);
      final int tempState = findNextLevel(initTest, level);
      level = tempState == -1 ? -1 : mLevels[tempState];
      l("Managed to reach level " + level);
      if (level >= 0) {
        //foundLevel = level;
        l("So calling getAdditionalSteps");
        additionalSteps = getAdditionalSteps();
        finalTestState = tempState;
        l("Additional steps: " + additionalSteps.toString());
      }
      level--;
      mTestAutomatonStateEncoding.clear();
    } while (level > -1);

    l("we have gone as far as we can go");
    // convert steps!
    //copy = null;
    //mSimplifier.setTransitionRelation(rel);

    // Add the search results to the end of the trace ...
    if (additionalSteps != null) {
      l("There are additionalSteps");
      newTraceSteps.addAll(additionalSteps);
    }
    l("NewTraceSteps: " + newTraceSteps);

    l("final state: " + finalTestState);

    // get int set from Simplifier
    final int[] stateSet = mSimplifier.getStateSet(finalTestState);
    mOriginalTraceEndState = stateSet[0];
    final TIntArrayList crucialEvents = getEventSteps(newTraceSteps);
    final SearchRecord endRecord = convertEventSteps(crucialEvents);
    final List<SearchRecord> convertedSteps = endRecord.getTrace();
    mergeTraceSteps(newTraceSteps, convertedSteps);
    l("DONE: final steps for original: ");
    l(newTraceSteps + "");
    return newTraceSteps;
  }

  private int FindTestAutomatonInit(final int initResult,
                                    final List<TraceStepProxy> traceSteps)
  {
    //System.out.println("here!!!");
    // iterate trace through mTestAutomaton
    // get events
    final TIntArrayList events = getEventSteps(traceSteps);
    l("there are " + events.size() + " events:");
    //final TIntArrayList toVisit = new TIntArrayList();
    for (int i = 0; i < events.size(); i++) {
      //System.out.print(events.get(i) + ",");
    }
    l("");
    int resultState = -1;

    // 1. Collect initial states
    //if (events.)
    final ListBufferTransitionRelation rel = mCertainConfRel;
    if (rel.getNumberOfReachableStates() == 1) {
      l("only 1 state in rel");

    }
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
      l("called open remove to get " + current);
      final int state = current.getState();
      final int depth = current.getDepth();

      if (!events.isEmpty() && depth != events.size()) {
        final int nextdepth = depth + 1;
        l("nextDepth => " + nextdepth);
        final int event = events.get(depth);
        l("event => " + event);
        iter.reset(state, event);
        l("iter reset to " + state);
        while (iter.advance()) {
          l("advancing iter on event " + event);
          final int target = iter.getCurrentTargetState();
          l("got to " + target);
          final SearchRecord next =
            new SearchRecord(target, nextdepth, event, current);
          if (nextdepth == events.size()) {
            l("found our Desired State! so return record");
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
    l("found state in test automaton: " + resultState);
    // now have automaton number. get the set
    final ArrayList<Integer> stateSet =
      mSimplifier.getStateSetArrayList(resultState);
    // shuffle until initResult is at the front

    l("it's corresponding state set is:");

    for (int i = 0; i < stateSet.size(); i++) {
      //System.out.print(stateSet.get(i) + ",");
    }
    l("");

    l("and result state is " + resultState);
    l("and init state is " + initResult);
    Collections.swap(stateSet, 0, stateSet.indexOf(initResult));
    Collections.sort(stateSet.subList(1, stateSet.size()));
    l("And now it is...");
    for (int i = 0; i < stateSet.size(); i++) {
     // System.out.print(stateSet.get(i) + ",");
    }
    l("");
    l("~~~~~~~~~~~~~~");
    final int[] orderedStateSet = new int[stateSet.size()];
    for (int i = 0; i < stateSet.size(); i++) {
      orderedStateSet[i] = stateSet.get(i);
    }
    final int temp = mSimplifier.findStateFromSet(orderedStateSet);
    l("which corresponds to state number " + temp);
    return temp;
  }

  private TIntArrayList getEventSteps(final List<TraceStepProxy> traceSteps)
  {
    l("~~~~~~~~~~~~~");
    final EventEncoding enc = getEventEncoding();
    final int len = traceSteps.size();
    l("Now need to convert " + len + " traceSteps");
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
    l("Start convertEventSteps");
    l("these are the event steps: ");
    for (int i = 0; i < eventSteps.size(); i++) {
      //System.out.print(eventSteps.get(i) + ",");
    }
    l("");
    l("Also, the final state is " + mOriginalTraceEndState);
    // 1. Collect initial states
    l("Collecting initials");
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
    final Set<SearchRecord> visited = new THashSet<SearchRecord>();
    final int numStates = rel.getNumberOfStates();

    for (int state = 0; state < numStates; state++) {
      if (rel.isInitial(state)) {
        final SearchRecord record = new SearchRecord(state);
        if (state == mOriginalTraceEndState) {
          l("looking at state.. it is the end so return blank record");
          return record;

        }
        visited.add(record);
        open.add(record);
      }
    }
    l("initials collection complete. there are: " + open.size());
    l("and number of events to process: " + eventSteps.size());
    // 2. Breadth-first search
    l("Breadth-First search");
    final int tau = EventEncoding.TAU;
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    while (true) {

      final SearchRecord current = open.remove();
      final int state = current.getState();
      final int depth = current.getDepth();
      l("visiting state " + state + " with depth " + depth);

      if (!eventSteps.isEmpty() && depth != eventSteps.size()) {
        final int nextdepth = depth + 1;
        l("nextDepth => " + nextdepth);
        final int event = eventSteps.get(depth);
        l("event => " + event);
        iter.reset(state, event);
        l("iter reset to " + state);
        while (iter.advance()) {
          l("advancing iter on event " + event);
          final int target = iter.getCurrentTargetState();
          l("got to " + target);
          final SearchRecord next =
            new SearchRecord(target, nextdepth, event, current);
          //if (nextdepth == eventSteps.size() || target == mOriginalTraceEndState) {
          if (target == mOriginalTraceEndState) {
            l("found our Desired State! so return record");
            return next;
          } else if (visited.add(next)) {
            open.add(next);
          }
        }
      }

      l("do a tau search now");
      iter.reset(state, tau);
      while (iter.advance()) {
        l("advancing iter on event tau");
        final int target = iter.getCurrentTargetState();
        l("got to " + target);
        final SearchRecord next =
          new SearchRecord(target, depth, tau, current);
        if (target == mOriginalTraceEndState) {
          l(" found desired end state so return record");
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

  //#########################################################################
  //# Trace Extension
  /*
   * private List<TraceStepProxy> relabelInitialTraceSteps (final
   * List<TraceStepProxy> steps, final int[] stateMap) { final int numSteps =
   * steps.size(); final List<TraceStepProxy> newSteps = new
   * ArrayList<TraceStepProxy>(numSteps); for (final TraceStepProxy step :
   * steps) { final TraceStepProxy newStep = relabelInitialTraceStep(step,
   * stateMap); newSteps.add(newStep); } return newSteps; }
   */

  /*
   * private TraceStepProxy relabelInitialTraceStep (final TraceStepProxy
   * resultStep, final int[] stateMap) { final AutomatonProxy resultAut =
   * getResultAutomaton(); final Map<AutomatonProxy,StateProxy> resultMap =
   * resultStep.getStateMap(); final int size = resultMap.size(); final
   * Map<AutomatonProxy,StateProxy> origMap = new
   * HashMap<AutomatonProxy,StateProxy>(size); for (final
   * Map.Entry<AutomatonProxy,StateProxy> entry : resultMap.entrySet()) {
   * final AutomatonProxy aut = entry.getKey(); final StateProxy state =
   * entry.getValue(); if (aut == resultAut) { final AutomatonProxy origAut =
   * getOriginalAutomaton(); final int resultCode =
   * getResultAutomatonStateCode(state); final int origCode = stateMap != null
   * ? stateMap[resultCode] : resultCode; assert origCode >= 0; final
   * StateProxy origState = getOriginalAutomatonState(origCode);
   * origMap.put(origAut, origState); } else { origMap.put(aut, state); } }
   * final AbstractCompositionalModelVerifier verifier = getModelVerifier();
   * final ProductDESProxyFactory factory = verifier.getFactory(); final
   * EventProxy event = resultStep.getEvent(); return
   * factory.createTraceStepProxy(event, origMap); }
   */
  private int findNextLevel(final int initTest, final int level)
    throws AnalysisException
  {
    // try to reach level - 1
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final EventEncoding eventEnc = getEventEncoding();
    mTestAutomaton =
      mSimplifier.createTestAutomaton(factory, eventEnc,
                                      mTestAutomatonStateEncoding, initTest,
                                      mCheckedProposition, level);
    final ProductDESProxy des = createLanguageInclusionModel();
    mSafetyVerifier.setModel(des);
    //MarshallingTools.saveModule(des, "xxx.wmod");
    if (mSafetyVerifier.run()) {
      return -1;
    } else {
      final SafetyTraceProxy trace = mSafetyVerifier.getCounterExample();
      final int state = getTestAutomatonEndState(trace);
      //return mLevels[state];
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
    //l("rel :" + rel.getNumberOfReachableStates());
    final int numSteps = steps.size();
    // l("There are " + numSteps + " steps");
    final ListIterator<TraceStepProxy> iter = steps.listIterator(numSteps);
    //l("Iterate through these steps: " + steps);
    int current = -1;
    back: do {
      final TraceStepProxy step = iter.previous();
      //l("Looking at step " + step);
      final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
      final StateProxy state = stepMap.get(mTestAutomaton);
      // l("That state is " + state);
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
      if (checkAut == mTestAutomaton) {
        //        final AutomatonProxy origAut = getOriginalAutomaton();
        //        final int stateCode =
        //          mTestAutomatonStateEncoding.getStateCode(checkState);
        //        //final StateProxy origState = getOriginalAutomatonState(stateCode);
        //
        //        final int[] stateSet = mSimplifier.getStateSet(stateCode);
        //        final StateProxy origState = getOriginalAutomatonState(stateSet[0]);
        //
        //        refMap.put(origAut, origState);
      } else {
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
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (aut == mPropertyAutomaton) {
        return ComponentKind.SPEC;
      } else {
        return ComponentKind.PLANT;
      }
    }

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
  private final boolean logging;
}