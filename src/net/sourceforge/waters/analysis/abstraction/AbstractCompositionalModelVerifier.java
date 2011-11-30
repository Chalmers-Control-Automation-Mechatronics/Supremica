//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   AbstractCompositionalModelVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.HashFunctions;
import gnu.trove.THashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.EventNotFoundException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * A general compositional model verifier to be subclassed for different
 * properties. This class adds to the general base class {@link
 * AbstractCompositionalModelAnalyzer} a common {@link #run()} that
 * supports counter example computation.
 *
 * <I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, 48(3), 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. A Compositional Approach for Verifying Generalised
 * Nonblocking, Proc. 7th International Conference on Control and Automation,
 * ICCA'09, 448-453, Christchurch, New Zealand, 2009.<BR>
 *
 * @author Robi Malik
 */

public abstract class AbstractCompositionalModelVerifier
  extends AbstractCompositionalModelAnalyzer
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an abstracting model verifier without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   */
  protected AbstractCompositionalModelVerifier
    (final ProductDESProxyFactory factory,
     final KindTranslator translator)
  {
    super(factory, translator);
  }

  /**
   * Creates an abstracting model verifier without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   * @param selectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          selection methods.
   */
  protected AbstractCompositionalModelVerifier
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final PreselectingMethodFactory preselectingMethodFactory,
     final SelectingMethodFactory selectingMethodFactory)
  {
    super(factory, translator,
          preselectingMethodFactory, selectingMethodFactory);
  }

  /**
   * Creates an abstracting model verifier to check the given model.
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   */
  protected AbstractCompositionalModelVerifier
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator)
  {
    super(model, factory, translator);
  }

  /**
   * Creates an abstracting model verifier to check the given model.
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   * @param selectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          selection methods.
   */
  protected AbstractCompositionalModelVerifier
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final PreselectingMethodFactory preselectingMethodFactory,
     final SelectingMethodFactory selectingMethodFactory)
  {
    super(model, factory, translator,
          preselectingMethodFactory, selectingMethodFactory);
  }


  //#########################################################################
  //# Configuration


  //#########################################################################
  //# Specific Access
  protected void setMonolithicVerifier(final ModelVerifier verifier)
  {
    mMonolithicVerifier = verifier;
  }

  protected ModelVerifier getMonolithicVerifier()
  {
    return mMonolithicVerifier;
  }

  protected void setCurrentMonolithicVerifier(final ModelVerifier verifier)
  {
    mCurrentMonolithicVerifier = verifier;
  }

  protected ModelVerifier getCurrentMonolithicVerifier()
  {
    return mCurrentMonolithicVerifier;
  }

  protected void setupMonolithicVerifier()
    throws EventNotFoundException
  {
    final int nlimit = getMonolithicStateLimit();
    mCurrentMonolithicVerifier.setNodeLimit(nlimit);
    final int tlimit = getMonolithicTransitionLimit();
    mCurrentMonolithicVerifier.setTransitionLimit(tlimit);
    final KindTranslator translator = getKindTranslator();
    mCurrentMonolithicVerifier.setKindTranslator(translator);
  }

  /**
   * Stores a verification result indicating that the property checked
   * is satisfied and marks the run as completed.
   * @return <CODE>true</CODE>
   */
  protected boolean setSatisfiedResult()
  {
    return setBooleanResult(true);
  }

  /**
   * Stores a verification result indicating that the property checked
   * is not satisfied and marks the run as completed.
   * @param  counterexample The counterexample obtained by verification.
   * @return <CODE>false</CODE>
   */
  protected boolean setFailedResult(final TraceProxy counterexample)
  {
    final VerificationResult result = getAnalysisResult();
    result.setCounterExample(counterexample);
    return setBooleanResult(false);
  }


  //#########################################################################
  //# Invocation
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      runCompositionalMinimisation();
      final CompositionalVerificationResult result = getAnalysisResult();
      if (result.isSatisfied()) {
        return true;
      } else {
        final AbstractionProcedure proc = getAbstractionProcedure();
        proc.resetStatistics();
        TraceProxy trace = result.getCounterExample();
        trace = expandTrace(trace);
        return setFailedResult(trace);
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mCurrentMonolithicVerifier != null) {
      mCurrentMonolithicVerifier.requestAbort();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifier
  public boolean isSatisfied()
  {
    final VerificationResult result = getAnalysisResult();
    if (result != null) {
      return result.isSatisfied();
    } else {
      throw new IllegalStateException("Call run() first!");
    }
  }

  public TraceProxy getCounterExample()
  {
    if (isSatisfied()) {
      throw new IllegalStateException("No trace for satisfied property!");
    } else {
      final VerificationResult result = getAnalysisResult();
      return result.getCounterExample();
    }
  }

  @Override
  public CompositionalVerificationResult getAnalysisResult()
  {
    return (CompositionalVerificationResult) super.getAnalysisResult();
  }

  @Override
  protected CompositionalVerificationResult createAnalysisResult()
  {
    return new CompositionalVerificationResult();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  protected void setUp()
    throws AnalysisException
  {
    setupMonolithicVerifier();
    mModifyingSteps = new ArrayList<AbstractionStep>();
    super.setUp();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mCurrentMonolithicVerifier = null;
    mModifyingSteps = null;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected void recordAbstractionStep(final AbstractionStep step)
  {
    mModifyingSteps.add(step);
  }

  @Override
  protected boolean doMonolithicAnalysis
    (final List<AutomatonProxy> automata)
    throws AnalysisException
  {
    if (automata.isEmpty()) {
      return setSatisfiedResult();
    } else {
      final ProductDESProxy des = createProductDESProxy(automata);
      final Logger logger = getLogger();
      if (logger.isDebugEnabled()) {
        final Collection<AutomatonProxy> automata1 = des.getAutomata();
        double estimate = 1.0;
        for (final AutomatonProxy aut : automata1) {
          estimate *= aut.getStates().size();
        }
        logger.debug("Monolithically composing " + automata1.size() +
                     " automata, estimated " + estimate + " states.");
      }
      mCurrentMonolithicVerifier.setModel(des);
      mCurrentMonolithicVerifier.run();
      // Do not clean up before run, keep data just in case of overflow ...
      removeEventsToAutomata(automata);
      final VerificationResult subresult =
        mCurrentMonolithicVerifier.getAnalysisResult();
      recordStatistics(subresult);
      if (subresult.isSatisfied()) {
        return true;
      } else {
        final CompositionalVerificationResult result = getAnalysisResult();
        final TraceProxy trace =
          mCurrentMonolithicVerifier.getCounterExample();
        result.setCounterExample(trace);
        final boolean confirmed = confirmMonolithicCounterExample();
        return !confirmed;
      }
    }
  }

  /**
   * Checks whether the currently stored counterexample for the current
   * subsystem can be considered as a counterexample for the entire system,
   * included completed and postponed subsystems. This method may change
   * the stored counterexample if needed, or remove it if it is found that
   * it cannot be extended.
   * @return <CODE>true</CODE> if the counterexample has successfully
   *         been lifted to the entire system;
   *         <CODE>false</CODE> if the counterexample cannot be lifted to
   *         the entire system and has been removed.
   */
  protected boolean confirmMonolithicCounterExample()
  throws AnalysisException
  {
    return true;
  }

  /**
   * Creates a counterexample. This hook is invoked to create a counterexample
   * from a given list trace steps. It should create trace of the correct type.
   * @param  automata     Automata to be put in the counterexample.
   * @param  steps        List of steps constituting the counterexample.
   */
  protected abstract TraceProxy createTrace
    (final Collection<AutomatonProxy> automata,
     final List<TraceStepProxy> steps);

  /**
   * Checks whether the given sequence of steps forms a correct counterexample
   * for the given automata. This method is used for debugging only.
   */
  protected abstract void testCounterExample
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata)
  throws AnalysisException;


  //#########################################################################
  //# Trace Computation
  private TraceProxy expandTrace(final TraceProxy trace)
    throws AnalysisException
  {
    final List<TraceStepProxy> unsat = trace.getTraceSteps();
    final Collection<AutomatonProxy> currentAutomata = getCurrentAutomata();
    List<TraceStepProxy> traceSteps =
      getSaturatedTraceSteps(unsat, currentAutomata);
    final int size = mModifyingSteps.size();
    final ListIterator<AbstractionStep> iter =
      mModifyingSteps.listIterator(size);
    final Collection<AutomatonProxy> check =
      new THashSet<AutomatonProxy>(currentAutomata);
    //checkCounterExample(traceSteps, check);
    while (iter.hasPrevious()) {
      final AbstractionStep step = iter.previous();
      traceSteps = step.convertTraceSteps(traceSteps);
      check.removeAll(step.getResultAutomata());
      check.addAll(step.getOriginalAutomata());
      //checkCounterExample(traceSteps, check);
    }
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> modelAutomata = model.getAutomata();
    return createTrace(modelAutomata, traceSteps);
  }

  /**
   * Fills in the target states in the state maps for each step of the trace
   * for the result automaton.
   */
  private List<TraceStepProxy> getSaturatedTraceSteps
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata)
  {
    final ProductDESProxyFactory factory = getFactory();
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


  //#########################################################################
  //# Statistics
  private void recordStatistics(final VerificationResult result)
  {
    final CompositionalVerificationResult global = getAnalysisResult();
    global.addMonolithicAnalysisResult(result);
  }


  //#########################################################################
  //# Inner Class TRSimplifierAbstractionProcedure
  protected abstract class TRSimplifierAbstractionProcedure
    extends AbstractionProcedure
  {
    //#######################################################################
    //# Constructor
    protected TRSimplifierAbstractionProcedure
      (final TransitionRelationSimplifier simplifier)
    {
      mSimplifier = simplifier;
    }

    //#######################################################################
    //# Overrides for AbstractionProcedure
    @Override
    protected AbstractionStep run(final AutomatonProxy aut,
                                  final Collection<EventProxy> local)
      throws AnalysisException
    {
      try {
        assert local.size() <= 1 : "At most one tau event supported!";
        final Iterator<EventProxy> iter = local.iterator();
        final EventProxy tau = iter.hasNext() ? iter.next() : null;
        final EventEncoding eventEnc = createEventEncoding(aut, tau);
        final StateEncoding inputStateEnc = new StateEncoding(aut);
        final int config = mSimplifier.getPreferredInputConfiguration();
        final ListBufferTransitionRelation rel =
          new ListBufferTransitionRelation(aut, eventEnc,
                                           inputStateEnc, config);
        final int numStates = rel.getNumberOfStates();
        final int numTrans = rel.getNumberOfTransitions();
        final int numMarkings = rel.getNumberOfMarkings();
        mSimplifier.setTransitionRelation(rel);
        if (mSimplifier.run()) {
          if (rel.getNumberOfReachableStates() == numStates &&
              rel.getNumberOfTransitions() == numTrans &&
              rel.getNumberOfMarkings() == numMarkings) {
            return null;
          }
          rel.removeRedundantPropositions();
          final ProductDESProxyFactory factory = getFactory();
          final StateEncoding outputStateEnc = new StateEncoding();
          final AutomatonProxy convertedAut =
            rel.createAutomaton(factory, eventEnc, outputStateEnc);
          return createStep
            (aut, inputStateEnc, convertedAut, outputStateEnc, tau);
        } else {
          return null;
        }
      } finally {
        mSimplifier.reset();
      }
    }

    @Override
    protected void storeStatistics()
    {
      final CompositionalVerificationResult result = getAnalysisResult();
      result.setSimplifierStatistics(mSimplifier);
    }

    @Override
    protected void resetStatistics()
    {
      mSimplifier.createStatistics();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.Abortable
    public void requestAbort()
    {
      mSimplifier.requestAbort();
    }

    public boolean isAborting()
    {
      return mSimplifier.isAborting();
    }

    //#######################################################################
    //# Simple Access
    protected TransitionRelationSimplifier getSimplifier()
    {
      return mSimplifier;
    }

    //#######################################################################
    //# Auxiliary Methods
    protected EventEncoding createEventEncoding(final AutomatonProxy aut,
                                                final EventProxy tau)
    {
      final KindTranslator translator = getKindTranslator();
      final Collection<EventProxy> props = getPropositions();
      final Collection<EventProxy> filter;
      if (props == null) {
        filter = Collections.emptyList();
      } else {
        filter = props;
      }
      return new EventEncoding(aut, translator, tau, filter,
                               EventEncoding.FILTER_PROPOSITIONS);
    }

    @SuppressWarnings("unused")
    private EventEncoding createEventEncoding(final AutomatonProxy aut,
                                              final Collection<EventProxy> local)
    {
      final KindTranslator translator = getKindTranslator();
      final Collection<EventProxy> props = getPropositions();
      final Collection<EventProxy> filter;
      if (props == null) {
        filter = Collections.emptyList();
      } else {
        filter = props;
      }
      final Collection<EventProxy> autAlphabet = aut.getEvents();
      final Collection<EventProxy> localUncontrollableEvents =
              new ArrayList<EventProxy>(local.size());
      final Collection<EventProxy> localControllableEvents =
              new ArrayList<EventProxy>(local.size());
      final Collection<EventProxy> sharedEvents =
              new ArrayList<EventProxy>(autAlphabet.size() - local.size());
      final Collection<EventProxy> encodedEvents =
              new ArrayList<EventProxy>(autAlphabet.size());
      for(final EventProxy event:autAlphabet){
          if(local.contains(event) && translator.getEventKind(event) ==
                  EventKind.CONTROLLABLE)
              localControllableEvents.add(event);
          else if(local.contains(event) && translator.getEventKind(event) ==
                  EventKind.UNCONTROLLABLE)
              localUncontrollableEvents.add(event);
          else
              sharedEvents.add(event);
      }
      encodedEvents.addAll(localUncontrollableEvents);
      encodedEvents.addAll(localControllableEvents);
      encodedEvents.addAll(sharedEvents);
      return new EventEncoding(encodedEvents, translator, filter,
                               EventEncoding.FILTER_PROPOSITIONS);
    }

    protected abstract AbstractionStep createStep
      (final AutomatonProxy input,
       final StateEncoding inputStateEnc,
       final AutomatonProxy output,
       final StateEncoding outputStateEnc,
       final EventProxy tau);

    //#######################################################################
    //# Data Members
    private final TransitionRelationSimplifier mSimplifier;
  }


  //#########################################################################
  //# Inner Class TRAbstractionStep
  /**
   * An abstraction step that uses a {@link ListBufferTransitionRelation}
   * for trace expansion
   */
  protected abstract class TRAbstractionStep extends AbstractionStep
  {
    //#######################################################################
    //# Constructor
    /**
     * Creates a new abstraction step record.
     * @param  resultAut         The automaton resulting from abstraction.
     * @param  originalAut       The automaton before abstraction.
     * @param  tau               The event represent silent transitions,
     *                           or <CODE>null</CODE>.
     * @param  originalStateEnc  State encoding of input automaton, or
     *                           <CODE>null</CODE> to use a temporary
     *                           encoding.
     */
    protected TRAbstractionStep(final AutomatonProxy resultAut,
                                final AutomatonProxy originalAut,
                                final EventProxy tau,
                                final StateEncoding originalStateEnc)
    {
      super(resultAut, originalAut);
      mTau = tau;
      mOriginalStateEncodingIsTemporary = (originalStateEnc == null);
      mOriginalStateEncoding = originalStateEnc;
    }

    //#######################################################################
    //# Simple Access
    protected EventProxy getTau()
    {
      return mTau;
    }

    protected ListBufferTransitionRelation getTransitionRelation()
    {
      return mTransitionRelation;
    }

    protected EventEncoding getEventEncoding()
    {
      return mEventEncoding;
    }

    //#######################################################################
    //# Trace Computation
    protected void setupTraceConversion()
      throws AnalysisException
    {
      final AutomatonProxy originalAutomaton = getOriginalAutomaton();
      final KindTranslator translator = getKindTranslator();
      final Collection<EventProxy> props = getPropositions();
      final Collection<EventProxy> filter;
      if (props != null) {
        filter = props;
      } else {
        filter = Collections.emptyList();
      }
      mEventEncoding =
        new EventEncoding(originalAutomaton, translator, mTau, filter,
                          EventEncoding.FILTER_PROPOSITIONS);
      if (mOriginalStateEncodingIsTemporary) {
        mOriginalStateEncoding = new StateEncoding(originalAutomaton);
      }
      mTransitionRelation = new ListBufferTransitionRelation
        (originalAutomaton, mEventEncoding, mOriginalStateEncoding,
         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    }

    protected void setupTraceConversion
      (final EventEncoding enc,
       final ListBufferTransitionRelation rel)
    {
      mEventEncoding = enc;
      mTransitionRelation = rel;
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    }

    protected void tearDownTraceConversion()
    {
      if (mOriginalStateEncodingIsTemporary) {
        mOriginalStateEncoding = null;
      }
      mEventEncoding = null;
      mTransitionRelation = null;
    }

    protected void mergeTraceSteps(final List<TraceStepProxy> traceSteps,
                                   final List<SearchRecord> convertedSteps)
    {
      final int tau = EventEncoding.TAU;
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy resultAutomaton = getResultAutomaton();
      final AutomatonProxy originalAutomaton = getOriginalAutomaton();
      final ListIterator<TraceStepProxy> stepIter = traceSteps.listIterator();
      final TraceStepProxy initStep = stepIter.next();
      final Iterator<SearchRecord> convertedIter = convertedSteps.iterator();
      final SearchRecord initRecord = convertedIter.next();
      final Map<AutomatonProxy,StateProxy> map =
        new HashMap<AutomatonProxy,StateProxy>(initStep.getStateMap());
      map.remove(resultAutomaton);
      final int initID = initRecord.getState();
      final StateProxy initState = mOriginalStateEncoding.getState(initID);
      map.put(originalAutomaton, initState);
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
            map.remove(resultAutomaton);
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
            map.put(originalAutomaton, state);
            final TraceStepProxy newStep =
              factory.createTraceStepProxy(mTau, map);
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
        map.put(originalAutomaton, state);
        final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
        map.putAll(stepMap);
        map.remove(resultAutomaton);
        final TraceStepProxy newStep = factory.createTraceStepProxy(event, map);
        stepIter.set(newStep);
        step = stepIter.hasNext() ? stepIter.next() : null;
        record = convertedIter.hasNext() ? convertedIter.next() : null;
      }
    }

    //#######################################################################
    //# Data Members
    /**
     * The event that was hidden from the original automaton,
     * or <CODE>null</CODE>.
     */
    private final EventProxy mTau;
    /**
     * A flag, indicating that the state encoding is only used temporarily
     * during trace expansion.
     */
    private final boolean mOriginalStateEncodingIsTemporary;
    /**
     * State encoding of original automaton. Maps state codes in the input
     * transition relation to state objects in the input automaton.
     */
    private StateEncoding mOriginalStateEncoding;
    /**
     * Transition relation that was simplified.
     * Only used when expanding trace.
     */
    private ListBufferTransitionRelation mTransitionRelation;
    /**
     * Event encoding for {@link #mTransitionRelation}.
     * Only used when expanding trace.
     */
    private EventEncoding mEventEncoding;
  }


  //#########################################################################
  //# Inner Class SearchRecord
  /**
   * A record to store information about a visited state while searching
   * to expand counterexamples.
   */
  protected static class SearchRecord
  {

    //#######################################################################
    //# Constructors
    protected SearchRecord(final int state)
    {
      this(state, -1);
    }

    protected SearchRecord(final int state, final int event)
    {
      this(state, 0, event, null);
    }

    protected SearchRecord(final int state,
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
    protected int getState()
    {
      return mState;
    }

    protected int getDepth()
    {
      return mDepth;
    }

    protected SearchRecord getPredecessor()
    {
      return mPredecessor;
    }

    protected int getEvent()
    {
      return mEvent;
    }

    //#######################################################################
    //# Trace Construction
    protected List<SearchRecord> getTrace()
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


  //#########################################################################
  //# Data Members
  private ModelVerifier mMonolithicVerifier;
  private ModelVerifier mCurrentMonolithicVerifier;
  private List<AbstractionStep> mModifyingSteps;

}
