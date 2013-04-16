//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   TRTraceExpander
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.HashFunctions;
import gnu.trove.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * @author Robi Malik
 */

public abstract class TRTraceExpander
{
  //#######################################################################
  //# Constructors
  protected TRTraceExpander(final AbstractCompositionalModelVerifier verifier,
                            final EventProxy tau,
                            final AutomatonProxy resultAut,
                            final AutomatonProxy originalAut)
    throws AnalysisException
  {
    this(verifier, tau, resultAut, null, originalAut, null);
  }

  protected TRTraceExpander(final AbstractCompositionalModelVerifier verifier,
                            final EventProxy tau,
                            final AutomatonProxy resultAut,
                            final StateEncoding resultStateEnc,
                            final AutomatonProxy originalAut,
                            final List<int[]> partition)
    throws AnalysisException
  {
    this(verifier, tau, null,
         resultAut, resultStateEnc, originalAut, null, partition, false);
  }

  protected TRTraceExpander(final AbstractCompositionalModelVerifier verifier,
                            final EventProxy tau,
                            final EventProxy preconditionMarking,
                            final AutomatonProxy resultAut,
                            final StateEncoding resultStateEnc,
                            final AutomatonProxy originalAut,
                            final StateEncoding originalStateEnc,
                            final List<int[]> partition,
                            final boolean preconditionMarkingReduced)
    throws AnalysisException
  {
    this(verifier, null, tau, preconditionMarking,
         resultAut, resultStateEnc,
         originalAut, originalStateEnc,
         partition, preconditionMarkingReduced);
  }

  private TRTraceExpander(final AbstractCompositionalModelVerifier verifier,
                          final EventEncoding eventEnc,
                          final EventProxy tau,
                          final EventProxy preconditionMarking,
                          final AutomatonProxy resultAut,
                          final StateEncoding resultStateEnc,
                          final AutomatonProxy originalAut,
                          final StateEncoding originalStateEnc,
                          final List<int[]> partition,
                          final boolean preconditionMarkingReduced)
    throws AnalysisException
  {
    mModelVerifier = verifier;
    if (eventEnc != null) {
      mEventEncoding = eventEnc;
    } else {
      final KindTranslator translator = verifier.getKindTranslator();
      final Collection<EventProxy> props = verifier.getPropositions();
      if (preconditionMarking == null) {
        final int filterMode;
        if (props == null) {
          filterMode = EventEncoding.FILTER_NONE;
        } else {
          filterMode = EventEncoding.FILTER_PROPOSITIONS;
        }
        mEventEncoding =
          new EventEncoding(originalAut, translator, tau, props, filterMode);
        mPreconditionMarkingID = -1;
      } else {
        final Collection<EventProxy> filter;
        if (props.contains(preconditionMarking)) {
          filter = props;
        } else {
          final int size = props.size() + 1;
          filter = new ArrayList<EventProxy>(size);
          filter.addAll(props);
          filter.add(preconditionMarking);
        }
        mEventEncoding =
          new EventEncoding(originalAut, translator, tau, filter,
                            EventEncoding.FILTER_PROPOSITIONS);
        mPreconditionMarkingID =
          mEventEncoding.getEventCode(preconditionMarking);
        if (mPreconditionMarkingID < 0) {
          mPreconditionMarkingID =
            mEventEncoding.addEvent(preconditionMarking, translator, EventEncoding.STATUS_EXTRA_SELFLOOP);
        }
      }
    }
    mTauEvent = tau;
    mResultAutomaton = resultAut;
    mResultStateEncoding = resultStateEnc;
    mOriginalAutomaton = originalAut;
    if (originalStateEnc != null) {
      mOriginalStateEncoding = originalStateEnc;
    } else {
      mOriginalStateEncoding = new StateEncoding(originalAut);
    }
    mPartition = partition;
    mTransitionRelation = new ListBufferTransitionRelation
      (originalAut, mEventEncoding, mOriginalStateEncoding,
       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    if (preconditionMarkingReduced) {
      recoverPreconditionMarking();
    }
  }


  //#######################################################################
  //# Simple Access
  AbstractCompositionalModelVerifier getModelVerifier()
  {
    return mModelVerifier;
  }

  ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  EventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }

  EventProxy getTauEvent()
  {
    return mTauEvent;
  }

  AutomatonProxy getOriginalAutomaton()
  {
    return mOriginalAutomaton;
  }

  AutomatonProxy getResultAutomaton()
  {
    return mResultAutomaton;
  }

  StateProxy getOriginalAutomatonState(final int code)
  {
    return mOriginalStateEncoding.getState(code);
  }

  int getOriginalAutomatonStateCode(final StateProxy state)
  {
    return mOriginalStateEncoding.getStateCode(state);
  }

  int getResultAutomatonStateCode(final StateProxy state)
  {
    return mResultStateEncoding.getStateCode(state);
  }


  //#######################################################################
  //# Invocation
  abstract public List<TraceStepProxy> convertTraceSteps
    (List<TraceStepProxy> traceSteps)
    throws AnalysisException;


  //#######################################################################
  //# Trace Computation
  /**
   * Fills in the target states in the state maps for each step of the trace
   * for the result automaton.
   */
  List<TraceStepProxy> getSaturatedTraceSteps
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata)
  {
    final ProductDESProxyFactory factory = mModelVerifier.getFactory();
    final int numAutomata = automata.size();
    final int numSteps = steps.size();
    final List<TraceStepProxy> convertedSteps =
        new ArrayList<TraceStepProxy>(numSteps);
    final Iterator<TraceStepProxy> iter = steps.iterator();

    final TraceStepProxy firstStep = iter.next();
    final Map<AutomatonProxy,StateProxy> firstMap = firstStep.getStateMap();
    final Map<AutomatonProxy,StateProxy> convertedFirstMap =
      new HashMap<AutomatonProxy,StateProxy>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      final StateProxy state = getInitialState(aut, firstMap);
      convertedFirstMap.put(aut, state);
    }
    final TraceStepProxy convertedFirstStep =
      factory.createTraceStepProxy(null, convertedFirstMap);
    convertedSteps.add(convertedFirstStep);
    Map<AutomatonProxy,StateProxy> previousStepMap = convertedFirstMap;
    while (iter.hasNext()) {
      final TraceStepProxy step = iter.next();
      final EventProxy event = step.getEvent();
      final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
      final Map<AutomatonProxy,StateProxy> convertedStepMap =
        new HashMap<AutomatonProxy,StateProxy>(numAutomata);
      for (final AutomatonProxy aut : automata) {
        final StateProxy prev = previousStepMap.get(aut);
        final StateProxy state = findSuccessor(aut, event, prev, stepMap);
        convertedStepMap.put(aut, state);
      }
      final TraceStepProxy convertedStep =
        factory.createTraceStepProxy(event, convertedStepMap);
      convertedSteps.add(convertedStep);
      previousStepMap = convertedStepMap;
    }
    return convertedSteps;
  }

  List<SearchRecord> getCrucialSteps(final List<TraceStepProxy> traceSteps)
  {
    final int tau = EventEncoding.TAU;
    final int len = traceSteps.size() + 1;
    final List<SearchRecord> crucialSteps = new ArrayList<SearchRecord>(len);
    final Iterator<TraceStepProxy> iter = traceSteps.iterator();
    TraceStepProxy step = iter.next();
    Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
    StateProxy crucialState = stepMap.get(mResultAutomaton);
    int crucialEventID = tau;
    SearchRecord record;
    while (iter.hasNext()) {
      step = iter.next();
      final EventProxy event = step.getEvent();
      final int eventID = mEventEncoding.getEventCode(event);
      if (eventID < 0) {
        // Step of another automaton only --- skip.
      } else if (eventID == tau) {
        // Step by local tau --- skip but record target state.
        stepMap = step.getStateMap();
        crucialState = stepMap.get(mResultAutomaton);
      } else {
        // Step by a proper event ---
        // 1) Add a step to the source state unless initial.
        if (crucialEventID != tau) {
          final int crucialStateID =
            mResultStateEncoding.getStateCode(crucialState);
          record = new SearchRecord(crucialStateID, crucialEventID);
          crucialSteps.add(record);
        }
        // 2) Record new event and target state.
        crucialEventID = eventID;
        stepMap = step.getStateMap();
        crucialState = stepMap.get(mResultAutomaton);
      }
    }
    // Add step to last target state.
    final int crucialStateID = mResultStateEncoding.getStateCode(crucialState);
    record = new SearchRecord(crucialStateID, crucialEventID);
    crucialSteps.add(record);
    // Add final step to reach alpha.
    if (mPreconditionMarkingID >= 0) {
      record = new SearchRecord(-1, 0, tau, null);
      crucialSteps.add(record);
    }
    return crucialSteps;
  }

  void setupTarget(final int targetClass)
  {
    if (targetClass < 0) {
      mTargetSet = null;
    } else if (mPartition == null) {
      mTargetSet = new TIntHashSet(1);
      mTargetSet.add(targetClass);
    } else {
      final int[] targetArray = mPartition.get(targetClass);
      mTargetSet = new TIntHashSet(targetArray);
    }
  }

  boolean isTargetState(final int state)
  {
    if (mTargetSet != null) {
      return mTargetSet.contains(state);
    } else {
      return isTraceEndState(state);
    }
  }

  boolean isTraceEndState(final int state)
  {
    if (mPreconditionMarkingID < 0) {
      return true;
    } else {
      return mTransitionRelation.isMarked(state, mPreconditionMarkingID);
    }
  }

  void mergeTraceSteps(final List<TraceStepProxy> traceSteps,
                       final List<SearchRecord> convertedSteps)
  {
    final int tau = EventEncoding.TAU;
    final ProductDESProxyFactory factory = mModelVerifier.getFactory();
    final ListIterator<TraceStepProxy> stepIter = traceSteps.listIterator();
    final TraceStepProxy initStep = stepIter.next();
    final Iterator<SearchRecord> convertedIter = convertedSteps.iterator();
    final SearchRecord initRecord = convertedIter.next();
    final Map<AutomatonProxy,StateProxy> map =
      new HashMap<AutomatonProxy,StateProxy>(initStep.getStateMap());
    map.remove(mResultAutomaton);
    final int initID = initRecord.getState();
    final StateProxy initState = mOriginalStateEncoding.getState(initID);
    map.put(mOriginalAutomaton, initState);
    final TraceStepProxy newInitStep =
      factory.createTraceStepProxy(null, map);
    stepIter.set(newInitStep);
    TraceStepProxy step = stepIter.hasNext() ? stepIter.next() : null;
    SearchRecord record =
      convertedIter.hasNext() ? convertedIter.next() : null;
    while (step != null || record != null) {
      if (step != null) {
        final EventProxy event = step.getEvent();
        final int eventID = mEventEncoding.getEventCode(event);
        if (eventID == tau) {
          // Skip tau in master trace, will insert later from converted.
          stepIter.remove();
          step = stepIter.hasNext() ? stepIter.next() : null;
          continue;
        } else if (eventID < 0) {
          // Step of another automaton only.
          final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
          map.putAll(stepMap);
          map.remove(mResultAutomaton);
          final TraceStepProxy newStep =
            factory.createTraceStepProxy(event, map);
          stepIter.set(newStep);
          step = stepIter.hasNext() ? stepIter.next() : null;
          continue;
        }
      }
      if (record != null) {
        final int eventID = record.getEvent();
        if (eventID == tau) {
          // Step by local tau only.
          final int stateID = record.getState();
          final StateProxy state = mOriginalStateEncoding.getState(stateID);
          map.put(mOriginalAutomaton, state);
          final TraceStepProxy newStep =
            factory.createTraceStepProxy(mTauEvent, map);
          if (step == null) {
            stepIter.add(newStep);
          } else {
            stepIter.previous();
            stepIter.add(newStep);
            stepIter.next();
          }
          record = convertedIter.hasNext() ? convertedIter.next() : null;
          continue;
        }
      }
      // Step by shared event
      assert step != null;
      assert record != null;
      final EventProxy event = step.getEvent();
      final int stateID = record.getState();
      final StateProxy state = mOriginalStateEncoding.getState(stateID);
      map.put(mOriginalAutomaton, state);
      final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
      map.putAll(stepMap);
      map.remove(mResultAutomaton);
      final TraceStepProxy newStep = factory.createTraceStepProxy(event, map);
      stepIter.set(newStep);
      step = stepIter.hasNext() ? stepIter.next() : null;
      record = convertedIter.hasNext() ? convertedIter.next() : null;
    }
  }


  //#######################################################################
  //# Auxiliary Methods
  /**
   * Finds the initial state of an automaton in a trace.
   * A trace step's map is passed for the case of multiple initial states.
   */
  private StateProxy getInitialState
    (final AutomatonProxy aut, final Map<AutomatonProxy,StateProxy> stepMap)
  {
    // If there is more than one initial state, the trace has the info.
    StateProxy initial = stepMap.get(aut);
    // Otherwise there is only one initial state.
    if (initial == null) {
      for (final StateProxy state : aut.getStates()) {
        if (state.isInitial()) {
          initial = state;
          break;
        }
      }
    }
    return initial;
  }

  /**
   * Finds the successor state in trace, from a given state in an automaton.
   * A trace step's map is passed for the case of multiple successor states.
   */
  private StateProxy findSuccessor(final AutomatonProxy aut,
                                   final EventProxy event,
                                   final StateProxy sourceState,
                                   final Map<AutomatonProxy,StateProxy> stepMap)
  {
    // If there is more than one successor state, the trace has the info.
    final StateProxy targetState = stepMap.get(aut);
    // Otherwise there is only one successor state.
    if (targetState == null) {
      if (aut.getEvents().contains(event)) {
        for (final TransitionProxy trans : aut.getTransitions()) {
          if (trans.getEvent() == event && trans.getSource() == sourceState) {
            return trans.getTarget();
          }
        }
      } else {
        return sourceState;
      }
    }
    return targetState;
  }

  private void recoverPreconditionMarking()
    throws AnalysisException
  {
    if (mPreconditionMarkingID >= 0) {
      final ChainTRSimplifier chain = new ChainTRSimplifier();
      chain.add(new TauLoopRemovalTRSimplifier());
      chain.add(new MarkingRemovalTRSimplifier());
      final int config = chain.getPreferredInputConfiguration();
      ListBufferTransitionRelation rel = new ListBufferTransitionRelation
        (mOriginalAutomaton, mEventEncoding, mOriginalStateEncoding, config);
      chain.setTransitionRelation(rel);
      chain.run();
      rel = chain.getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final List<int[]> partition = chain.getResultPartition();
      if (partition == null) {
        for (int state = 0; state < numStates; state++) {
          if (!rel.isMarked(state, mPreconditionMarkingID)) {
            mTransitionRelation.setMarked(state, mPreconditionMarkingID,
                                          false);
          }
        }
      } else {
        for (int state = 0; state < numStates; state++) {
          if (!rel.isMarked(state, mPreconditionMarkingID)) {
            for (final int member : partition.get(state)) {
              mTransitionRelation.setMarked(member, mPreconditionMarkingID,
                                            false);
            }
          }
        }
      }
    }
  }


  //#########################################################################
  //# Inner Class SearchRecord
  /**
   * A record to store information about a visited state while searching
   * to expand counterexamples.
   */
  static class SearchRecord
  {

    //#######################################################################
    //# Constructors
    SearchRecord(final int state)
    {
      this(state, -1);
    }

    SearchRecord(final int state, final int event)
    {
      this(state, 0, event, null);
    }

    SearchRecord(final int state,
                 final int depth,
                 final int event,
                 final SearchRecord pred)
    {
      mState = state;
      mDepth = depth;
      mEvent = event;
      mPredecessor = pred;
    }

    //#######################################################################
    //# Getters
    int getState()
    {
      return mState;
    }

    int getDepth()
    {
      return mDepth;
    }

    SearchRecord getPredecessor()
    {
      return mPredecessor;
    }

    int getEvent()
    {
      return mEvent;
    }

    //#######################################################################
    //# Trace Construction
    List<SearchRecord> getTrace()
    {
      final List<SearchRecord> trace = new LinkedList<SearchRecord>();
      for (SearchRecord record = this;
           record != null;
           record = record.getPredecessor()) {
        trace.add(0, record);
      }
      return trace;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public String toString()
    {
      return
        "{state=" + mState + "; event=" + mEvent + "; depth=" + mDepth + "}";
    }

    @Override
    public boolean equals(final Object other)
    {
      if (other.getClass() == getClass()) {
        final SearchRecord record = (SearchRecord) other;
        return mState == record.mState && mDepth == record.mDepth;
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      return HashFunctions.hash(mState) + 5 * HashFunctions.hash(mDepth);
    }

    //#######################################################################
    //# Data Members
    private final int mState;
    private final int mDepth;
    private final int mEvent;
    private final SearchRecord mPredecessor;
  }


  //#######################################################################
  //# Data Members
  private final AbstractCompositionalModelVerifier mModelVerifier;

  /**
   * Event encoding for {@link #mTransitionRelation}.
   * Only used when expanding trace.
   */
  private EventEncoding mEventEncoding;
  /**
   * The event that was hidden from the original automaton,
   * or <CODE>null</CODE>.
   */
  private final EventProxy mTauEvent;

  private int mPreconditionMarkingID;

  private final AutomatonProxy mResultAutomaton;
  /**
   * State encoding of the result automaton. Maps state codes in the input
   * transition relation to state objects in the input automaton.
   */
  private final StateEncoding mResultStateEncoding;

  private final AutomatonProxy mOriginalAutomaton;
  /**
   * State encoding of the original automaton. Maps state codes in the input
   * transition relation to state objects in the input automaton.
   */
  private final StateEncoding mOriginalStateEncoding;

  private final List<int[]> mPartition;

  /**
   * Transition relation that was simplified.
   * Only used when expanding trace.
   */
  private final ListBufferTransitionRelation mTransitionRelation;

  private TIntHashSet mTargetSet;

}
