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
import gnu.trove.TIntHashSet;
import gnu.trove.TIntStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    final List<SearchRecord> crucialSteps = getCrucialSteps(traceSteps);

    // Skip certain conflicts if trace end state is already blocking ...
    if (isBlockingTrace(crucialSteps)) {
      return traceSteps;
    }

    // OK, expanded trace is not blocking.
    // We need to try to add steps into certain conflicts and further
    // into blocking, or prove that the rest of the system blocks ...
    final int numSteps = crucialSteps.size();
    SearchRecord record = crucialSteps.get(numSteps - 1);
    final int lastConvertedState = record.getState();
    final int config = mSimplifier.getPreferredInputConfiguration();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    ListBufferTransitionRelation copy =
      new ListBufferTransitionRelation(rel, config);
    mSimplifier.setTransitionRelation(copy);
    mSimplifier.setAppliesPartitionAutomatically(false);
    mSimplifier.run();
    mSimplifier.setTransitionRelation(rel);
    copy = null;

    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final EventEncoding eventEnc = getEventEncoding();
    final int numTraceSteps = traceSteps.size();
    mStartStates = traceSteps.get(numTraceSteps - 1);
    final StateEncoding stateEnc = new StateEncoding();
    mCheckedProposition =
      factory.createEventProxy(":certainconf", EventKind.UNCONTROLLABLE);
    final AutomatonProxy testaut = null;
    List<TraceStepProxy> additionalSteps = null;
    final int startLevel = mSimplifier.getLevel(lastConvertedState);
    final int maxlevel =
      startLevel < 0 ? mSimplifier.getMaxLevel() : startLevel - 2;
    for (int level = 0; level <= maxlevel; level += 2) {
      mTestAutomaton = mSimplifier.createTestAutomaton
        (factory, eventEnc, stateEnc,
         lastConvertedState, mCheckedProposition, level);
      additionalSteps = computeAdditionalSteps();
      if (additionalSteps != null) {
        break;
      }
      stateEnc.clear();
    }
    if (additionalSteps != null) {
      final Collection<AutomatonProxy> automata = getTraceAutomata();
      final List<TraceStepProxy> saturatedSteps =
        getSaturatedTraceSteps(additionalSteps, automata);
      final Iterator<TraceStepProxy> iter = saturatedSteps.iterator();
      iter.next();
      while (iter.hasNext()) {
        final TraceStepProxy step = iter.next();
        final EventProxy event = step.getEvent();
        final int ecode = eventEnc.getEventCode(event);
        final Map<AutomatonProxy,StateProxy> map = step.getStateMap();
        final Map<AutomatonProxy,StateProxy> reducedMap =
          new HashMap<AutomatonProxy,StateProxy>(map);
        reducedMap.remove(testaut);
        final TraceStepProxy reducedStep =
          factory.createTraceStepProxy(event, reducedMap);
        traceSteps.add(reducedStep);
        if (ecode >= 0) {
          final StateProxy state = map.get(testaut);
          final int scode = stateEnc.getStateCode(state);
          record = new SearchRecord(scode, ecode);
          crucialSteps.add(record);
        }
      }
    } else if (startLevel > 0 && (startLevel & 1) != 0) {
      final int endState =
        mSimplifier.findTauReachableState(lastConvertedState, startLevel & ~1);
      record = new SearchRecord(endState, EventEncoding.TAU);
      crucialSteps.add(record);
    }

    mergeTraceSteps(traceSteps, crucialSteps);
    return traceSteps;
  }


  //#######################################################################
  //# Auxiliary Methods
  private boolean isBlockingTrace(final List<SearchRecord> steps)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final EventEncoding enc = getEventEncoding();
    final CompositionalConflictChecker verifier = getConflictChecker();
    final EventProxy marking = verifier.getMarkingProposition();
    final int markingID = enc.getEventCode(marking);
    final int traceEnd = steps.size() - 1;
    final SearchRecord step = steps.get(traceEnd);
    final int state= step.getState();
    if (rel.isMarked(state, markingID)) {
      return false;
    }
    final TIntStack stack = new TIntStack();
    final TIntHashSet visited = new TIntHashSet();
    stack.push(state);
    visited.add(state);
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    while (stack.size() > 0) {
      final int current = stack.pop();
      iter.resetState(current);
      while (iter.advance()) {
        final int succ = iter.getCurrentTargetState();
        if (visited.add(succ)) {
          if (rel.isMarked(succ, markingID)) {
            return false;
          }
          stack.push(succ);
        }
      }
    }
    return true;
  }


  //#########################################################################
  //# Auxiliary Methods
  private ProductDESProxy createLanguageInclusionModel()
  {
    final Map<AutomatonProxy,StateProxy> stepMap = mStartStates.getStateMap();
    final int numAutomata = stepMap.size();
    int numStates = 0;
    for (final AutomatonProxy aut : stepMap.keySet()) {
      numStates += aut.getStates().size();
    }
    mReverseStateMap = new HashMap<StateProxy,StateProxy>(numStates);
    mOriginalAutomata = new ArrayList<AutomatonProxy>(numAutomata);
    mConvertedAutomata = new ArrayList<AutomatonProxy>(numAutomata + 1);
    final AutomatonProxy originalAut = getOriginalAutomaton();
    mOriginalAutomata.add(originalAut);
    mConvertedAutomata.add(mTestAutomaton);
    final Collection<EventProxy> ccevents = mTestAutomaton.getEvents();
    final Collection<EventProxy> events = new THashSet<EventProxy>(ccevents);
    for (final Map.Entry<AutomatonProxy,StateProxy> entry :
         stepMap.entrySet()) {
      final AutomatonProxy aut = entry.getKey();
      if (aut != originalAut) {
        mOriginalAutomata.add(aut);
        final StateProxy init = entry.getValue();
        final AutomatonProxy converted =
          createLanguageInclusionAutomaton(aut, init);
        mConvertedAutomata.add(converted);
        final Collection<EventProxy> local = converted.getEvents();
        events.addAll(local);
      }
    }
    if (mPropertyAutomaton == null) {
      mPropertyAutomaton = createPropertyAutomaton();
    }
    mConvertedAutomata.add(mPropertyAutomaton);
    final List<EventProxy> eventList = new ArrayList<EventProxy>(events);
    Collections.sort(eventList);
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final String name = originalAut.getName();
    final String comment =
      "Automatically generated to expand conflict error trace for '" +
      name + "' from certain conflicts to blocking state.";
    final ProductDESProxy des =
      factory.createProductDESProxy(name, comment, null,
                                    eventList, mConvertedAutomata);
    return des;
  }

  private AutomatonProxy createLanguageInclusionAutomaton
    (final AutomatonProxy aut, final StateProxy init)
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
      final StateProxy newstate =
        factory.createStateProxy(statename, oldstate == init, null);
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
      return relabelTraceSteps(steps);
    }
  }

  private List<TraceStepProxy> relabelTraceSteps
    (final List<TraceStepProxy> steps)
  {
    final int len = steps.size();
    final List<TraceStepProxy> newsteps = new ArrayList<TraceStepProxy>(len);
    newsteps.add(mStartStates);
    final int numSteps = steps.size() - 2; // skip first and last ...
    final Iterator<TraceStepProxy> iter = steps.iterator();
    iter.next();
    for (int i = 0; i < numSteps; i++) {
      final TraceStepProxy oldstep = iter.next();
      final TraceStepProxy newstep = relabelTraceStep(oldstep);
      newsteps.add(newstep);
    }
    return newsteps;
  }

  private TraceStepProxy relabelTraceStep(final TraceStepProxy oldstep)
  {
    final Map<AutomatonProxy,StateProxy> oldmap = oldstep.getStateMap();
    final int size = oldmap.size();
    final Map<AutomatonProxy,StateProxy> newmap =
      new HashMap<AutomatonProxy,StateProxy>(size);
    final Iterator<AutomatonProxy> olditer = mConvertedAutomata.iterator();
    final Iterator<AutomatonProxy> newiter = mOriginalAutomata.iterator();
    while (newiter.hasNext()) {
      final AutomatonProxy oldaut = olditer.next();
      final AutomatonProxy newaut = newiter.next();
      final StateProxy oldstate = oldmap.get(oldaut);
      if (oldstate != null) {
        final StateProxy newstate = mReverseStateMap.get(oldstate);
        if (newstate == null) {
          newmap.put(oldaut, oldstate);
        } else {
          newmap.put(newaut, newstate);
        }
      }
    }
    final AbstractCompositionalModelVerifier verifier = getModelVerifier();
    final ProductDESProxyFactory factory = verifier.getFactory();
    final EventProxy event = oldstep.getEvent();
    return factory.createTraceStepProxy(event, newmap);
  }


  private Collection<AutomatonProxy> getTraceAutomata()
  {
    final int numAutomata = mOriginalAutomata.size();
    final Collection<AutomatonProxy> result =
      new ArrayList<AutomatonProxy>(numAutomata);
    final AutomatonProxy originalAut = getOriginalAutomaton();
    for (final AutomatonProxy aut : mOriginalAutomata) {
      if (aut != originalAut) {
        result.add(aut);
      }
    }
    result.add(mTestAutomaton);
    return result;
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

  private TraceStepProxy mStartStates;
  private EventProxy mCheckedProposition;
  private AutomatonProxy mTestAutomaton;
  private AutomatonProxy mPropertyAutomaton;
  private List<AutomatonProxy> mOriginalAutomata;
  private List<AutomatonProxy> mConvertedAutomata;
  private Map<StateProxy,StateProxy> mReverseStateMap;

}