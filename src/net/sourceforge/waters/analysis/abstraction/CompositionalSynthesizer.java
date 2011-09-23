//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CompositionalSynthesizer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TLongObjectHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.MemStateProxy;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * An implementation of the compositional synthesis algorithm.
 *
 * <I>References:</I><BR>
 * Sahar Mohajerani, Robi Malik, Simon Ware, Martin Fabian.
 * On the Use of Observation Equivalence in Synthesis Abstraction.
 * Proc. 3rd IFAC Workshop on Dependable Control of Discrete Systems,
 * DCDS&nbsp;2011, Saarbr&uuml;cken, Germany, 2011.<BR>
 * Sahar Mohajerani, Robi Malik, Martin Fabian.
 * Nondeterminism Avoidance in Compositional Synthesis of Discrete Event
 * Systems, Proc. 7th International Conference on Automation Science and
 * Engineering, CASE&nbsp;2011, Trieste, Italy.
 *
 * @author Sahar Mohajerani, Robi Malik
 */


public class CompositionalSynthesizer
  extends AbstractCompositionalModelAnalyzer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a compositional synthesiser without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   */
  public CompositionalSynthesizer
    (final ProductDESProxyFactory factory,
     final KindTranslator translator)
  {
    super(factory, translator);
  }

  /**
   * Creates a compositional synthesiser without a model.
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
  public CompositionalSynthesizer
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final PreselectingMethodFactory preselectingMethodFactory,
     final SelectingMethodFactory selectingMethodFactory)
  {
    super(factory, translator,
          preselectingMethodFactory, selectingMethodFactory);
  }

  /**
   * Creates a compositional synthesiser to compute a supervisor for the
   * given model.
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   */
  public CompositionalSynthesizer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator)
  {
    super(model, factory, translator);
  }

  /**
   * Creates a compositional synthesiser to compute a supervisor for the
   * given model.
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
  public CompositionalSynthesizer
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
  public void setMarkingProposition(final EventProxy marking)
  {
    mDefaultMarking = marking;
    mUsedDefaultMarking = null;
  }

  public EventProxy getMarkingProposition()
  {
    return mDefaultMarking;
  }


  //#########################################################################
  //# Invocation
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      runCompositionalMinimisation();
      final CompositionalSynthesisResult result = getAnalysisResult();
      if (!result.isFinished()) {
        result.setSatisfied(true);
      }
      final Logger logger = getLogger();
      logger.debug("CompositionalSynthesizer done.");
      return result.isSatisfied();
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
    /*
    if (mCurrentMonolithicVerifier != null) {
      mCurrentMonolithicVerifier.requestAbort();
    }
    */
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  protected void setUp()
    throws AnalysisException
  {
    if (mDefaultMarking == null) {
      final ProductDESProxy model = getModel();
      mUsedDefaultMarking =
        AbstractConflictChecker.getMarkingProposition(model);
    } else {
      mUsedDefaultMarking = mDefaultMarking;
    }
    final Collection<EventProxy> props =
      Collections.singletonList(mUsedDefaultMarking);
    setPropositions(props);

    final ChainTRSimplifier chain = new ChainTRSimplifier();
    /*
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
     */
    final HalfWaySynthesisTRSimplifier halfWay =
      new HalfWaySynthesisTRSimplifier();
    chain.add(halfWay);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence
      (ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION);
    chain.add(bisimulator);
    final SynthesisAbstractionTRSimplifier synthesisAbstraction= new
            SynthesisAbstractionTRSimplifier();
    final int limit = getInternalTransitionLimit();
    synthesisAbstraction.setTransitionLimit(limit);
    chain.add(synthesisAbstraction);
    final AbstractionProcedure proc =
      new SynthesisAbstractionProcedure(chain, synthesisAbstraction, halfWay);
    setAbstractionProcedure(proc);

    super.setUp();
    proc.storeStatistics();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUsedDefaultMarking = null;
  }

  @Override
  protected CompositionalSynthesisResult createAnalysisResult()
  {
    return new CompositionalSynthesisResult();
  }

  @Override
  public CompositionalSynthesisResult getAnalysisResult()
  {
    return (CompositionalSynthesisResult) super.getAnalysisResult();
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected SynthesisEventInfo createEventInfo(final EventProxy event)
  {
    return new SynthesisEventInfo(event);
  }

  @Override
  protected void recordAbstractionStep(final AbstractionStep step)
  {
    final CompositionalSynthesisResult result = getAnalysisResult();
    final ProductDESProxyFactory factory = getFactory();
    if(step instanceof SynthesisAbstractionStep){
      final SynthesisAbstractionStep synStep = (SynthesisAbstractionStep) step;
      final EventEncoding eventEnc = synStep.getEventEncoding();
      final ListBufferTransitionRelation distinguisher =
        synStep.getDistinguisher();
      if(distinguisher != null){
        final String name = "hsup:" + distinguisher.getName();
        distinguisher.setName(name);
        distinguisher.setKind(ComponentKind.SUPERVISOR);
        final AutomatonProxy autDistinguisher =
          distinguisher.createAutomaton(factory, eventEnc);
        result.addSupervisor(autDistinguisher);
      }
    } else if(step instanceof EventRemovalStep){
      final List<AutomatonProxy> before = step.getOriginalAutomata();
      final List<AutomatonProxy> after = step.getResultAutomata();
      final Iterator <AutomatonProxy> beforeIterator = before.iterator();
      final Iterator <AutomatonProxy> afterIterator = after.iterator();
      while (beforeIterator.hasNext()) {
        final AutomatonProxy beforeAutomaton = beforeIterator.next();
        final AutomatonProxy afterAutomaton = afterIterator.next();
        final int sizeBefore = getNumControllableEvents(beforeAutomaton);
        final int sizeAfter = getNumControllableEvents(afterAutomaton);
        if (sizeBefore != sizeAfter) {
          final String name = "dis:" + beforeAutomaton.getName();
          final Collection <EventProxy> events = beforeAutomaton.getEvents();
          final Collection <StateProxy> states = beforeAutomaton.getStates();
          final Collection <TransitionProxy> transitions =
            beforeAutomaton.getTransitions();
          final AutomatonProxy distinguisher =
            factory.createAutomatonProxy(name, ComponentKind.SUPERVISOR,
                                         events, states, transitions);
          result.addSupervisor(distinguisher);
        }
      }
    }
  }

  private int getNumControllableEvents(final AutomatonProxy aut)
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> alphabet = aut.getEvents();
    int size = 0;
    for(final EventProxy event:alphabet){
      if(translator.getEventKind(event) == EventKind.CONTROLLABLE){
        size++;
      }
    }
   return size;
  }

  @Override
  protected boolean doMonolithicAnalysis
    (final List<AutomatonProxy> automata)
    throws AnalysisException
  {
    final AutomatonProxy automaton;
    switch (automata.size()) {
    case 0:
      return true;
    case 1:
      automaton = automata.get(0);
      break;
    default:
      final Logger logger = getLogger();
      if (logger.isDebugEnabled()) {
        double estimate = 1.0;
        for (final AutomatonProxy aut : automata) {
          estimate *= aut.getStates().size();
        }
        logger.debug("Monolithically composing " + automata.size() +
                     " automata, estimated " + estimate + " states.");
      }
      final MonolithicSynchronousProductBuilder syncBuilder =
        getCurrentSynchronousProductBuilder();
      final ProductDESProxy des = createProductDESProxy(automata);
      syncBuilder.setModel(des);
      final int limit = getMonolithicStateLimit();
      syncBuilder.setNodeLimit(limit);
      syncBuilder.run();
      automaton = syncBuilder.getComputedProxy();
      break;
    }

    final AutomatonProxy supervisor =
      HalfWaySynthesisTRSimplifier.synthesise(automaton, mUsedDefaultMarking,
                                              getFactory(), getKindTranslator());
    final CompositionalSynthesisResult result = getAnalysisResult();
    if (supervisor.getStates().isEmpty()){
      result.setSatisfied(false);
      return false;
    } else {
      result.addSupervisor(supervisor);
      return true;
    }
  }


  //#########################################################################
  //# Renaming
  @SuppressWarnings("unused")
  private AutomatonProxy createDeterministicAutomaton
    (final ListBufferTransitionRelation original,
     final ListBufferTransitionRelation simplified,
     final Collection<int[]> partition,
     final EventEncoding eventEnc)
  {
    final ProductDESProxyFactory factory = getFactory();
    final int numOfStates= original.getNumberOfStates();
    final int[] recoding = new int[numOfStates];
    int code = 0;
    for (final int[] clazz : partition) {
      for (final int state : clazz) {
        recoding[state] = code;
      }
      code++;
    }

    final int numOfEvents = eventEnc.getNumberOfProperEvents();
    final Map<EventProxy, List<EventProxy>> eventMap =
      new HashMap<EventProxy, List<EventProxy>>(numOfEvents);
    final TransitionIterator iter = original.createSuccessorsReadOnlyIterator();
    for (int event = 0; event<numOfEvents; event ++){
      if(original.isUsedEvent(event)){
        int maxCount = 0;
        for(final int[] clazz : partition){
          final TIntHashSet successors = new TIntHashSet();
          for(final int state : clazz){
            iter.reset(state, event);
            while (iter.advance()){
              final int target = iter.getCurrentTargetState();
              final int targetClass = recoding [target];
              successors.add(targetClass);
            }
          }
          final int count = successors.size();
          if (count > maxCount){
            maxCount = count;
          }
        }
        if (maxCount > 1){
          final List <EventProxy> replacement =
            new ArrayList <EventProxy>(maxCount);
          final EventProxy eventProxy = eventEnc.getProperEvent(event);
          for (int i = 0; i < maxCount; i++){
            final EventProxy newEvent = factory.createEventProxy
              (eventProxy.getName() + ":" + i,
               eventProxy.getKind(), eventProxy.isObservable());
            replacement.add(newEvent);
          }
          eventMap.put(eventProxy, replacement);
        }
      }
    }

    // Create distinguisher and simplified automaton alphabet
    final Collection<EventProxy> distinguisherEvents =
      createAlphabet(original, eventEnc, eventMap);
    final Collection<EventProxy> simplifiedEvents =
      createAlphabet(simplified, eventEnc, eventMap);

    // Create distinguisher and simplified automaton states
    final StateProxy[] distinguisherStatesArray =
      createStates(original, eventEnc);
    final StateProxy[] simplifiedStatesArray =
      createStates(simplified, eventEnc);
    final Collection<StateProxy> distinguisherStates =
      getNotNullStates(distinguisherStatesArray);
    final Collection<StateProxy> simplifiedStates =
      getNotNullStates(simplifiedStatesArray);

    // Create distinguisher and simplified automaton transitions
    final Collection <TransitionProxy> distinguisherTransitions =
      new ArrayList <TransitionProxy> (original.getNumberOfTransitions());
    final TransitionIterator allIter =
      original.createAllTransitionsReadOnlyIterator();
    for (int event = 0; event<numOfEvents; event ++){
      if(original.isUsedEvent(event)){
        final EventProxy eventProxy = eventEnc.getProperEvent(event);
        if (eventMap.get(eventProxy) == null){
          allIter.resetEvent(event);
          while (iter.advance()) {
            final int s = iter.getCurrentSourceState();
            final int t = iter.getCurrentTargetState();
            if (original.isReachable(s) && original.isReachable(t)) {
              final StateProxy source = distinguisherStatesArray[s];
              final int e = iter.getCurrentEvent();
              final StateProxy target = distinguisherStatesArray[t];
              final TransitionProxy trans =
                factory.createTransitionProxy(source, eventProxy, target);
              distinguisherTransitions.add(trans);
            }
          }
        } else {
          final List<EventProxy> replacement= eventMap.get(eventProxy);
          for(final int[] clazz : partition){
            final TIntObjectHashMap <EventProxy> successors =
              new TIntObjectHashMap<EventProxy>(replacement.size());
            int next = 0;
            for(final int state : clazz){
              iter.reset(state, event);
              while(iter.advance()){
                final int target = iter.getCurrentTargetState();
                final int targetClass = recoding[target];
                EventProxy replacementEventProxy = successors.get(targetClass);
                if (replacementEventProxy == null) {
                  replacementEventProxy = replacement.get(next);
                  next++;
                  successors.put(next, replacementEventProxy);
                }
                final StateProxy source = distinguisherStatesArray[state];
                final int e = iter.getCurrentEvent();
                final StateProxy targetState = distinguisherStatesArray[target];
                final TransitionProxy trans =
                  factory.createTransitionProxy(source,
                                                replacementEventProxy,
                                                targetState);
                distinguisherTransitions.add(trans);
              }
            }
          }
        }
      }
    }
    return null;
  }

  private Collection<EventProxy> createAlphabet
    (final ListBufferTransitionRelation rel,
     final EventEncoding eventEnc,
     final Map<EventProxy,List<EventProxy>> eventMap)
  {
    final int numOfEvents = eventEnc.getNumberOfProperEvents();
    final Collection<EventProxy> events =
      new ArrayList<EventProxy>(numOfEvents);
    for (int event = 0; event < numOfEvents; event++) {
      if (rel.isUsedEvent(event)) {
        final EventProxy eventProxy = eventEnc.getProperEvent(event);
        if (eventMap.get(eventProxy) != null){
          events.addAll(eventMap.get(eventProxy));
        } else {
          events.add(eventProxy);
        }
      }
    }
    for (int p = 0; p < rel.getNumberOfPropositions(); p++) {
      if (rel.isUsedProposition(p)) {
        final EventProxy event = eventEnc.getProposition(p);
        events.add(event);
      }
    }
    return events;
  }

  private StateProxy [] createStates(final ListBufferTransitionRelation rel,
                                     final EventEncoding eventEnc)
  {
    final int numOfStates= rel.getNumberOfStates();
    final int numProps = rel.getNumberOfPropositions();
    final StateProxy[] states = new StateProxy[numOfStates];
    final TLongObjectHashMap<Collection<EventProxy>> markingsMap =
      new TLongObjectHashMap<Collection<EventProxy>>();
    int code = 0;
    for (int s = 0; s < numOfStates; s++) {
      if (rel.isReachable(s)) {
        final StateProxy state;
        final boolean init = rel.isInitial(s);
        final long markings = rel.getAllMarkings(s);
        Collection<EventProxy> props = markingsMap.get(markings);
        if (props == null) {
          props = new ArrayList<EventProxy>(numProps);
          for (int p = 0; p < numProps; p++) {
            if (rel.isMarked(s, p)) {
              final EventProxy prop = eventEnc.getProposition(p);
              props.add(prop);
            }
          }
          markingsMap.put(markings, props);
        }
        state = new MemStateProxy(code++, init, props);
        states[s] = state;
      }
    }
    return states;
  }

  private Collection<StateProxy> getNotNullStates(final StateProxy[] states)
  {
    final List<StateProxy> reachable =
      new ArrayList<StateProxy>(states.length);
    for (final StateProxy state : states) {
      if (state != null) {
        reachable.add(state);
      }
    }
    return reachable;
  }


  //#########################################################################
  //# Inner Class SynthesisAbstractionProcedure
  protected class SynthesisAbstractionProcedure
    extends AbstractionProcedure
  {
    //#######################################################################
    //# Constructor
    protected SynthesisAbstractionProcedure
      (final TransitionRelationSimplifier simplifier,
       final SynthesisAbstractionTRSimplifier synthesisAbstraction,
       final HalfWaySynthesisTRSimplifier halfWaySynthesisSimplifier)
    {
      mSimplifier = simplifier;
      mSynthesisAbstraction = synthesisAbstraction;
      mHalfWaySynthesisSimplifier = halfWaySynthesisSimplifier;
    }

    //#######################################################################
    //# Overrides for AbstractionProcedure
    @Override
    protected AbstractionStep run(final AutomatonProxy aut,
                                  final Collection<EventProxy> local)
      throws AnalysisException
    {
      final TransitionRelationSimplifier simplifier = getSimplifier();
      try {
        final EventEncoding eventEnc = createEventEncoding(aut, local);
        final StateEncoding inputStateEnc = createStateEncoding(aut);
        final int config = simplifier.getPreferredInputConfiguration();
        final ListBufferTransitionRelation rel =
          new ListBufferTransitionRelation(aut, eventEnc,
                                           inputStateEnc, config);
        final int numStates = rel.getNumberOfStates();
        final int numTrans = rel.getNumberOfTransitions();
        final int numMarkings = rel.getNumberOfMarkings();
        simplifier.setTransitionRelation(rel);
        if (simplifier.run()) {
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
          return new SynthesisAbstractionStep(convertedAut, aut,
                                              mHalfWaySynthesisSimplifier.
                                              getDistinguisher(), eventEnc);
        } else {
          return null;
        }
      } finally {
        simplifier.reset();
      }
    }

    @Override
    protected void storeStatistics()
    {
      final CompositionalSynthesisResult result = getAnalysisResult();
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
    private EventEncoding createEventEncoding
      (final AutomatonProxy aut,
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
      final Collection<EventProxy> sharedUncontrollableEvents =
        new ArrayList<EventProxy>(autAlphabet.size() - local.size());
      final Collection<EventProxy> localControllableEvents =
        new ArrayList<EventProxy>(local.size());
      final Collection<EventProxy> sharedControllableEvents =
        new ArrayList<EventProxy>(autAlphabet.size() - local.size());
      final Collection<EventProxy> encodedEvents =
        new ArrayList<EventProxy>(autAlphabet.size());
      for (final EventProxy event : autAlphabet) {
        if (local.contains(event)) {
          if (translator.getEventKind(event) == EventKind.CONTROLLABLE) {
            localControllableEvents.add(event);
          } else {
            localUncontrollableEvents.add(event);
          }
        } else {
          if (translator.getEventKind(event) == EventKind.CONTROLLABLE) {
            sharedControllableEvents.add(event);
          } else {
            sharedUncontrollableEvents.add(event);
          }
        }
      }
      final int lastUncontrollableLocalEvent =
        localUncontrollableEvents.size();
      final int lastControllableLocalEvent =
        localControllableEvents.size() + lastUncontrollableLocalEvent;
      final int lastUncontrollableSharedEvent =
        local.size() + sharedUncontrollableEvents.size();
      mSynthesisAbstraction.setLastLocalControllableEvent
        (lastControllableLocalEvent);
      mSynthesisAbstraction.setLastLocalUncontrollableEvent
        (lastUncontrollableLocalEvent);
      mSynthesisAbstraction.setLastSharedUncontrollableEvent
        (lastUncontrollableSharedEvent);
      mHalfWaySynthesisSimplifier.setLastLocalUncontrollableEvent
      (lastUncontrollableLocalEvent);
      mHalfWaySynthesisSimplifier.setLastLocalControllableEvent
      (lastControllableLocalEvent);
      mHalfWaySynthesisSimplifier.setLastSharedUncontrollableEvent
      (lastUncontrollableSharedEvent);
      encodedEvents.addAll(localUncontrollableEvents);
      encodedEvents.addAll(localControllableEvents);
      encodedEvents.addAll(sharedUncontrollableEvents);
      encodedEvents.addAll(sharedControllableEvents);
      final EventEncoding encoding = new EventEncoding(encodedEvents, translator,
                                                 filter,
                        EventEncoding.FILTER_PROPOSITIONS);
      mHalfWaySynthesisSimplifier.
        setDefaultMarkingID(encoding.getEventCode(mUsedDefaultMarking));
      mSynthesisAbstraction.
        setDefaultMarkingID(encoding.getEventCode(mUsedDefaultMarking));
      return encoding;
    }

    private StateEncoding createStateEncoding(final AutomatonProxy aut)
    {
      final StateEncoding encoding = new StateEncoding(aut);
      encoding.setNumberOfExtraStates(1);
      return encoding;
    }


    //#######################################################################
    //# Data Members
    private final TransitionRelationSimplifier mSimplifier;
    private final SynthesisAbstractionTRSimplifier mSynthesisAbstraction;
    private final HalfWaySynthesisTRSimplifier mHalfWaySynthesisSimplifier;
  }

  //#########################################################################
  //# Inner Class SynthesisAbstractionStep
  /**
   * An abstraction step representing synthesis abstraction and/or
   * halfway synthesis of a single automaton.
   */
  protected class SynthesisAbstractionStep
    extends AbstractionStep
  {
    //#######################################################################
    //# Constructor
    protected SynthesisAbstractionStep(final AutomatonProxy result,
                                       final AutomatonProxy original,
                                       final ListBufferTransitionRelation dis,
                                       final EventEncoding coding)
    {
      super(result, original);
      mDistinguisher = dis;
      mEventEncoding = coding;
    }

    //#######################################################################
    //# Simple Access
    ListBufferTransitionRelation getDistinguisher()
    {
      return mDistinguisher;
    }

    EventEncoding getEventEncoding()
    {
      return mEventEncoding;
    }

    //#######################################################################
    //# Data Members
    private final ListBufferTransitionRelation mDistinguisher;
    private final EventEncoding mEventEncoding;
  }


  //#########################################################################
  //# Inner Class SynthesisEventInfo
  /**
   * An event information record for compositional synthesis.
   * In compositional synthesis, there are no tau events, yet all events
   * are subject to selfloop removal.
   */
  protected static class SynthesisEventInfo extends EventInfo
  {
    //#######################################################################
    //# Constructor
    protected SynthesisEventInfo(final EventProxy event)
    {
      super(event);
    }

    //#######################################################################
    //# Event Status
    @Override
    protected boolean isTau()
    {
      return false;
    }

    @Override
    protected boolean isSubjectToSelfloopRemoval()
    {
      return true;
    }
  }


  //#########################################################################
  //# Data Members
  private EventProxy mDefaultMarking;
  private EventProxy mUsedDefaultMarking;

}
