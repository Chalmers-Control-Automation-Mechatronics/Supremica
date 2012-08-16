//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   LimitedCertainConflictsTraceExpander
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.THashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.MemStateProxy;
import net.sourceforge.waters.analysis.tr.StateEncoding;
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
    // Skip certain conflicts if trace end state is already blocking ...
    final int numTraceSteps = traceSteps.size();
    final ListIterator<TraceStepProxy> iter =
      traceSteps.listIterator(numTraceSteps);
    final TraceStepProxy lastStep = iter.previous();
    final AutomatonProxy resultAut = getResultAutomaton();
    final Map<AutomatonProxy,StateProxy> lastStepMap = lastStep.getStateMap();
    mReferenceAutomata = new ArrayList<AutomatonProxy>(lastStepMap.keySet());
    final StateProxy lastState = lastStepMap.get(resultAut);
    List<TraceStepProxy> newTraceSteps =
      new ArrayList<TraceStepProxy>(traceSteps);
    final int initResult;
    if (isDumpState(resultAut, lastState)) {
      // Trace goes into certain conflicts.
      // Remove the last step of the trace, so it can be replaced ...
      if (iter.hasPrevious()) {
        mLastTraceStep = iter.previous();
        final Map<AutomatonProxy,StateProxy> predMap =
          mLastTraceStep.getStateMap();
        final StateProxy predState = predMap.get(resultAut);
        initResult = getResultAutomatonStateCode(predState);
      } else {
        mLastTraceStep = null;
        initResult = -1;
      }
      newTraceSteps.remove(numTraceSteps - 1);
    } else {
      // Trace does not go into certain conflicts.
      // We still must try to extend it into certain conflicts ...
      mLastTraceStep = lastStep;
      initResult = getResultAutomatonStateCode(lastState);
    }

    // Try to add steps into certain conflicts and further
    // into blocking, or prove that the rest of the system blocks ...
    final int config = mSimplifier.getPreferredInputConfiguration();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    ListBufferTransitionRelation copy =
      new ListBufferTransitionRelation(rel, config);
    mSimplifier.setTransitionRelation(copy);
    mSimplifier.setAppliesPartitionAutomatically(false);
    mSimplifier.run();
    copy = null;
    mSimplifier.setTransitionRelation(rel);
    final List<int[]> partition = mSimplifier.getResultPartition();
    final int[] stateMap = createStateMap(partition);
    final int initTest =
      stateMap == null || initResult < 0 ? initResult : stateMap[initResult];
    newTraceSteps = relabelInitialTraceSteps(newTraceSteps, stateMap);

    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final EventEncoding eventEnc = getEventEncoding();
    mTestAutomatonStateEncoding = new StateEncoding();
    mCheckedProposition =
      factory.createEventProxy(":certainconf", EventKind.UNCONTROLLABLE);
    List<TraceStepProxy> additionalSteps = null;
    final int maxLevel = mSimplifier.getMaxLevel();
    assert maxLevel >= 0;
    for (int level = 0; level <= maxLevel; level += 2) {
      mTestAutomaton = mSimplifier.createTestAutomaton
        (factory, eventEnc, mTestAutomatonStateEncoding,
         initTest, mCheckedProposition, level);
      additionalSteps = computeAdditionalSteps();
      if (additionalSteps != null) {
        break;
      }
      mTestAutomatonStateEncoding.clear();
    }

    if (additionalSteps != null) {
      newTraceSteps.addAll(additionalSteps);
    }
    return newTraceSteps;

    /*
    if (additionalSteps != null) {
      newTraceSteps.addAll(additionalSteps);
    } else if (startLevel > 0 && (startLevel & 1) != 0) {
      final int endStateCode =
        mSimplifier.findTauReachableState(lastStateCode, startLevel & ~1);
      origState = getOriginalAutomatonState(endStateCode);
      final Map<AutomatonProxy,StateProxy> newMap =
        new HashMap<AutomatonProxy,StateProxy>(lastStepMap);
      newMap.remove(resultAut);
      final AutomatonProxy origAut = getOriginalAutomaton();
      newMap.put(origAut, origState);
      final EventProxy tau = getTauEvent();
      final TraceStepProxy newStep =
        factory.createTraceStepProxy(tau, newMap);
      traceSteps.add(newStep);
    }
    */
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

  /*
  private int findCertainConflictsState(final TraceStepProxy predStep,
                                        final TraceStepProxy lastStep)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    int maxLevel = -1;
    int state = -1;
    if (predStep == null) {
      final int numStates = rel.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        if (rel.isInitial(s)) {
          final int level = mSimplifier.getLevel(s);
          if (level > maxLevel) {
            maxLevel = level;
            state = s;
          }
        }
      }
    } else {
      final AutomatonProxy resultAut = getResultAutomaton();
      final Map<AutomatonProxy,StateProxy> predStepMap = predStep.getStateMap();
      final StateProxy predState = predStepMap.get(resultAut);
      final int predStateCode = getResultAutomatonStateCode(predState);
      final EventProxy event = lastStep.getEvent();
      final EventEncoding eventEnc = getEventEncoding();
      final int eventCode = eventEnc.getEventCode(event);
      final TransitionIterator iter =
        rel.createSuccessorsReadOnlyIterator(predStateCode, eventCode);
      while (iter.advance()) {
        final int s = iter.getCurrentTargetState();
        final int level = mSimplifier.getLevel(s);
        if (level > maxLevel) {
          maxLevel = level;
          state = s;
        }
      }
    }
    return state;
  }
  */


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
      "Automatically generated to expand conflict error trace for '" +
      name + "' from certain conflicts to blocking state.";
    final ProductDESProxy des =
      factory.createProductDESProxy(name, comment, null,
                                    eventList, mCheckAutomata);
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

  private List<TraceStepProxy> computeAdditionalSteps()
    throws AnalysisException
  {
    final ProductDESProxy des = createLanguageInclusionModel();
    mSafetyVerifier.setModel(des);
    //MarshallingTools.saveModule(des, "xxx.wmod");
    if (mSafetyVerifier.run()) {
      return null;
    } else {
      final SafetyTraceProxy trace = mSafetyVerifier.getCounterExample();
      final List<TraceStepProxy> steps = trace.getTraceSteps();
      final List<TraceStepProxy> saturatedSteps =
        getSaturatedTraceSteps(steps, mCheckAutomata);

      final List<TraceStepProxy> relabelledSteps =
        relabelSafetyTraceSteps(saturatedSteps);
      return relabelledSteps;
    }
  }

  private int[] createStateMap(final List<int[]> partition)
  {
    if (partition != null) {
      final int mapSize = partition.size();
      final int[]stateMap = new int[mapSize];
      int c = 0;
      for (final int[] clazz : partition) {
        if (clazz.length == 1) {
          stateMap[c++] = clazz[0];
        } else {
          stateMap[c++] = -1;
        }
      }
      return stateMap;
    } else {
      return null;
    }

  }
  private List<TraceStepProxy> relabelInitialTraceSteps
    (final List<TraceStepProxy> steps, final int[] stateMap)
  {
    final int numSteps = steps.size();
    final List<TraceStepProxy> newSteps =
      new ArrayList<TraceStepProxy>(numSteps);
    for (final TraceStepProxy step : steps) {
      final TraceStepProxy newStep = relabelInitialTraceStep(step, stateMap);
      newSteps.add(newStep);
    }
    return newSteps;
  }

  private TraceStepProxy relabelInitialTraceStep
    (final TraceStepProxy resultStep, final int[] stateMap)
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
        final int origCode =
          stateMap != null ? stateMap[resultCode] : resultCode;
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
        final AutomatonProxy origAut = getOriginalAutomaton();
        final int stateCode =
          mTestAutomatonStateEncoding.getStateCode(checkState);
        final StateProxy origState = getOriginalAutomatonState(stateCode);
        refMap.put(origAut, origState);
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
  private final LimitedCertainConflictsTRSimplifier mSimplifier;
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

}