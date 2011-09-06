//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CompositionalSafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TObjectByteHashMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.cpp.analysis.NativeSafetyVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.EventNotFoundException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.TraceChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A projecting safety verifier. This safety verifier implements
 * natural projection using subset construction and deterministic
 * minimisation to compute abstractions while composing automata.</P>
 *
 * <P><I>Reference:</I><BR>
 * Simon Ware, Robi Malik. The Use of Language Projection for Compositional
 * Verification of Discrete Event Systems. Proc. 9th International Workshop
 * on Discrete Event Systems, WODES&nbsp;2008, 322-327, G&ouml;teborg,
 * Sweden, 2008.</P>
 *
 * @author Robi Malik
 */

public abstract class CompositionalSafetyVerifier
  extends AbstractCompositionalModelVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new safety verifier without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator that defines event controllability status
   *          and automata types.
   * @param diag
   *          Diagnostics object to produce commented counterexamples.
   */
  public CompositionalSafetyVerifier(final ProductDESProxyFactory factory,
                                     final KindTranslator translator,
                                     final SafetyDiagnostics diag)
  {
    this(null, factory, translator, diag);
  }

  /**
   * Creates a new safety verifier to check the given model.
   * @param model
   *          The model to be checked by this safety verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator that defines event controllability status
   *          and automata types.
   * @param diag
   *          Diagnostics object to produce commented counterexamples.
   */
  public CompositionalSafetyVerifier(final ProductDESProxy model,
                                     final ProductDESProxyFactory factory,
                                     final KindTranslator translator,
                                     final SafetyDiagnostics diag)
  {
    super(model, factory, translator);
    mDiagnostics = diag;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  @Override
  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }

  public SafetyDiagnostics getDiagnostics()
  {
    return mDiagnostics;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  protected void setUp()
    throws AnalysisException
  {
    final int slimit = getInternalStateLimit();
    final int tlimit = getInternalTransitionLimit();
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TransitionRelationSimplifier loopRemover1 =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover1);
    final SubsetConstructionTRSimplifier subset =
      new SubsetConstructionTRSimplifier();
    chain.add(subset);
    subset.setStateLimit(slimit);
    subset.setTransitionLimit(tlimit);
    final TransitionRelationSimplifier loopRemover2 =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover2);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence
      (ObservationEquivalenceTRSimplifier.Equivalence.DETERMINISTIC_MINSTATE);
    bisimulator.setTransitionLimit(tlimit);
    chain.add(bisimulator);
    final AbstractionProcedure proc =
      new ProjectionAbstractionProcedure(chain, subset);
    setAbstractionProcedure(proc);
    super.setUp();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mProperties = null;
    mPropertyEventsMap = null;
    mCollectedPlants = null;
  }

  @Override
  protected void initialiseEventsToAutomata()
    throws OverflowException
  {
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    final KindTranslator translator = getKindTranslator();
    mProperties = new ArrayList<AutomatonProxy>(numAutomata);
    final Collection<EventProxy> events = model.getEvents();
    final int numEvents = events.size();
    mPropertyEventsMap = new TObjectByteHashMap<EventProxy>(numEvents);
    for (final AutomatonProxy aut : automata) {
      if (translator.getComponentKind(aut) == ComponentKind.SPEC &&
          !isTrivialProperty(aut)) {
        mProperties.add(aut);
        final Collection<EventProxy> local = aut.getEvents();
        final int numLocal = local.size();
        final Collection<EventProxy> used =
          new THashSet<EventProxy>(numLocal);
        for (final TransitionProxy trans : aut.getTransitions()) {
          used.add(trans.getEvent());
        }
        for (final EventProxy event : local) {
          if (translator.getEventKind(event) != EventKind.PROPOSITION) {
            final byte mode = used.contains(event) ? REGULAR : FORBIDDEN;
            mPropertyEventsMap.put(event, mode);
          }
        }
      }
    }
    mCollectedPlants = new ArrayList<AutomatonProxy>(numAutomata);
    super.initialiseEventsToAutomata();
  }

  @Override
  protected EventInfo createEventInfo(final EventProxy event)
  {
    return new SafetyEventInfo(event);
  }


  @Override
  protected boolean isSubsystemTrivial
    (final Collection<AutomatonProxy> automata)
  {
    if (mProperties.isEmpty()) {
      return setSatisfiedResult();
    } else {
      for (final AutomatonProxy aut : automata) {
        for (final EventProxy event : aut.getEvents()) {
          if (mPropertyEventsMap.containsKey(event)) {
            return false;
          }
        }
      }
      return true;
    }
  }

  @Override
  protected AbstractionStep removeEvents(final Collection<EventProxy> removed)
    throws OverflowException
  {
    final AbstractionStep step = super.removeEvents(removed);
    if (step != null) {
      final ProductDESProxyFactory factory = getFactory();
      final Set<EventProxy> removedSet = new THashSet<EventProxy>(removed);
      final ListIterator<AutomatonProxy> iter = mProperties.listIterator();
      while (iter.hasNext()) {
        final AutomatonProxy aut = iter.next();
        final Collection<EventProxy> events = aut.getEvents();
        boolean found = false;
        for (final EventProxy event : events) {
          if (removedSet.contains(event)) {
            found = true;
            break;
          }
        }
        if (!found) {
          continue;
        }
        final int numEvents = events.size();
        final Collection<EventProxy> newEvents =
          new ArrayList<EventProxy>(numEvents - 1);
        for (final EventProxy event : events) {
          if (!removedSet.contains(event)) {
            newEvents.add(event);
          }
        }
        final String name = aut.getName();
        final ComponentKind kind = aut.getKind();
        final Collection<StateProxy> states = aut.getStates();
        final Collection<TransitionProxy> transitions = aut.getTransitions();
        final AutomatonProxy newAut = factory.createAutomatonProxy
          (name, kind, newEvents, states, transitions);
        if (isTrivialProperty(newAut)) {
          iter.remove();
        } else {
          step.addAutomatonPair(newAut, aut);
          iter.set(newAut);
        }
      }
      for (final EventProxy event : removed) {
        mPropertyEventsMap.remove(event);
      }
    }
    return step;
  }

  @Override
  protected boolean checkPropertyMonolithically
    (final List<AutomatonProxy> automata)
  throws AnalysisException
  {
    if (!getPostponedSubsystems().isEmpty()) {
      mCollectedPlants.addAll(automata);
      return true;
    } else {
      final int numAutomata =
        mCollectedPlants.size() + automata.size() + mProperties.size();
      final List<AutomatonProxy> plantsAndSpecs =
        new ArrayList<AutomatonProxy>(numAutomata);
      plantsAndSpecs.addAll(mCollectedPlants);
      plantsAndSpecs.addAll(automata);
      plantsAndSpecs.addAll(mProperties);
      final boolean result =
        super.checkPropertyMonolithically(plantsAndSpecs);
      mCollectedPlants.clear();
      if (result) {
        setSatisfiedResult();
      }
      return result;
    }
  }

  @Override
  protected void setupMonolithicVerifier()
    throws EventNotFoundException
  {
    if (getCurrentMonolithicVerifier() == null) {
      final SafetyVerifier configured =
        (SafetyVerifier) getMonolithicVerifier();
      final SafetyVerifier current;
      if (configured == null) {
        final KindTranslator translator = getKindTranslator();
        final ProductDESProxyFactory factory = getFactory();
        current = new NativeSafetyVerifier(translator, mDiagnostics, factory);
      } else {
        current = configured;
      }
      setCurrentMonolithicVerifier(current);
      super.setupMonolithicVerifier();
    }
  }

  @Override
  protected SafetyTraceProxy createTrace
    (final Collection<AutomatonProxy> automata,
     final List<TraceStepProxy> steps)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy model = getModel();
    final String tracename = mDiagnostics.getTraceName(model);
    final CompositionalVerificationResult result = getAnalysisResult();
    final TraceProxy trace = result.getCounterExample();
    final String comment = trace.getComment();
    return factory.createSafetyTraceProxy(tracename,
                                          comment,
                                          null,
                                          model,
                                          automata,
                                          steps);
  }

  @Override
  protected void testCounterExample
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata)
    throws AnalysisException
  {
    // TODO
    TraceChecker.checkCounterExample(steps, automata, true);
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean isTrivialProperty(final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> filter = Collections.emptyList();
    final EventEncoding enc =
      new EventEncoding(aut, translator,
                        filter, EventEncoding.FILTER_PROPOSITIONS);
    final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
      (aut, enc, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    rel.checkReachability();
    final int numStates = rel.getNumberOfStates();
    final int numEvents = rel.getNumberOfProperEvents();
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    for (int s = 0; s < numStates; s++) {
      if (rel.isReachable(s)) {
        for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
          if (!iter.advance()) {
            return false;
          }
        }
      }
    }
    return false;
  }


  //#########################################################################
  //# Inner Class SafetyEventInfo
  private class SafetyEventInfo
    extends EventInfo
  {
    //#######################################################################
    //# Constructor
    private SafetyEventInfo(final EventProxy event)
    {
      super(event);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.analysis.abstraction.
    //# AbstractCompositionalModelVerifier.EventInfo
    @Override
    protected boolean isTau()
    {
      final EventProxy event = getEvent();
      return !mPropertyEventsMap.containsKey(event);
    }

    @Override
    protected boolean isLocal()
    {
      final EventProxy event = getEvent();
      return mPropertyEventsMap.get(event) != REGULAR;
    }
  }


  //#########################################################################
  //# Inner Class ProjectionAbstractionProcedure
  private class ProjectionAbstractionProcedure
    extends TRSimplifierAbstractionProcedure
  {
    //#######################################################################
    //# Constructor
    ProjectionAbstractionProcedure
      (final TransitionRelationSimplifier simplifier,
       final SubsetConstructionTRSimplifier subset)
    {
      super(simplifier);
      mSubsetConstructionTRSimplifier = subset;
    }

    //#######################################################################
    //# Overrides for TRSimplifierAbstractionProcedure
    @Override
    protected AbstractionStep run(final AutomatonProxy aut,
                                  final Collection<EventProxy> local)
      throws AnalysisException
    {
      final TransitionRelationSimplifier simplifier = getSimplifier();
      try {
        EventProxy tau = null;
        for (final EventProxy event : local) {
          if (!mPropertyEventsMap.containsKey(event)) {
            tau = event;
            break;
          }
        }
        final EventEncoding eventEnc = createEventEncoding(aut, tau);
        final StateEncoding inputStateEnc = new StateEncoding(aut);
        final int config = simplifier.getPreferredInputConfiguration();
        final ListBufferTransitionRelation rel =
          new ListBufferTransitionRelation(aut, eventEnc,
                                           inputStateEnc, config);
        for (final EventProxy event : local) {
          if (mPropertyEventsMap.get(event) == FORBIDDEN) {
            final int e = eventEnc.getEventCode(event);
            mSubsetConstructionTRSimplifier.setForbiddenEvent(e, true);
          }
        }
        simplifier.setTransitionRelation(rel);
        simplifier.run();
        final ProductDESProxyFactory factory = getFactory();
        final StateEncoding outputStateEnc = new StateEncoding();
        final AutomatonProxy convertedAut =
          rel.createAutomaton(factory, eventEnc, outputStateEnc);
        return createStep
          (aut, inputStateEnc, convertedAut, outputStateEnc, tau);
      } finally {
        simplifier.reset();
      }
    }

    @Override
    protected ProjectionStep createStep(final AutomatonProxy input,
                                        final StateEncoding inputStateEnc,
                                        final AutomatonProxy output,
                                        final StateEncoding outputStateEnc,
                                        final EventProxy tau)
    {
      return new ProjectionStep(output, input, tau);
    }

    //#######################################################################
    //# Data Members
    private final SubsetConstructionTRSimplifier
      mSubsetConstructionTRSimplifier;
  }


  //#########################################################################
  //# Inner Class ProjectionStep
  /**
   * An abstraction step in which the result automaton is obtained by
   * subset construction (language equivalence).
   */
  private class ProjectionStep extends TRAbstractionStep
  {
    //#######################################################################
    //# Constructor
    /**
     * Creates a new abstraction step record.
     * @param  resultAut         The automaton resulting from abstraction.
     * @param  originalAut       The automaton before abstraction.
     * @param  tau               The event represent silent transitions,
     *                           or <CODE>null</CODE>.
     */
    private ProjectionStep(final AutomatonProxy resultAut,
                           final AutomatonProxy originalAut,
                           final EventProxy tau)
    {
      super(resultAut, originalAut, tau, null);
    }

    //#######################################################################
    //# Trace Computation
    @Override
    protected List<TraceStepProxy> convertTraceSteps
      (final List<TraceStepProxy> traceSteps)
      throws AnalysisException
    {
      setupTraceConversion();
      final TIntArrayList crucialEvents = getEventSteps(traceSteps);
      final SearchRecord endRecord = convertEventSteps(crucialEvents);
      final List<SearchRecord> convertedSteps = endRecord.getTrace();
      mergeTraceSteps(traceSteps, convertedSteps);
      tearDownTraceConversion();
      return traceSteps;
    }

    private TIntArrayList getEventSteps(final List<TraceStepProxy> traceSteps)
    {
      final EventEncoding enc = getEventEncoding();
      final int len = traceSteps.size();
      final TIntArrayList crucialSteps = new TIntArrayList(len);
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
          crucialSteps.add(eventID);
        }
      }
      return crucialSteps;
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
          if (eventSteps.isEmpty()) {
            return record;
          }
          visited.add(record);
          open.add(record);
        }
      }
      // 2. Breadth-first search
      final int tau = EventEncoding.TAU;
      final TransitionIterator iter =
        rel.createSuccessorsReadOnlyIterator();
      while (true) {
        final SearchRecord current = open.remove();
        final int state = current.getState();
        final int depth = current.getDepth();
        final int nextdepth = depth + 1;
        final int event = eventSteps.get(depth);
        iter.reset(state, event);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          final SearchRecord next =
            new SearchRecord(target, nextdepth, event, current);
          if (nextdepth == eventSteps.size()) {
            return next;
          } else if (visited.add(next)) {
            open.add(next);
          }
        }
        iter.reset(state, tau);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          final SearchRecord next =
            new SearchRecord(target, depth, tau, current);
          if (visited.add(next)) {
            open.add(next);
          }
        }
      }
    }
  }


  //#########################################################################
  //# Data Members
  /**
   * Diagnostics object used to determine name and comment for
   * counterexample.
   */
  private final SafetyDiagnostics mDiagnostics;

  /**
   * List of specification (or property) automata in the model.
   * These are kept separate from the plants to exclude them from
   * compositional minimisation. They will only be used in the final
   * monolithic verification step.
   */
  private List<AutomatonProxy> mProperties;

  /**
   * Status information for events used in properties. Events used in
   * properties cannot be hidden during compositional minimisation, but
   * <I>forbidden</I> events can be treated specially. An event is considered
   * as forbidden if it is disabled in all states of some property automaton.
   * These events cannot be replaced by {@link EventEncoding#TAU TAU}, but
   * they are treated specially in subset construction, because successor
   * states reached after these events do not need to be explored. Therefore,
   * forbidden events are assigned the status {@link #FORBIDDEN}, while other
   * property events are assigned the status {@link #REGULAR}.
   */
  private TObjectByteHashMap<EventProxy> mPropertyEventsMap;

  /**
   * List of plants still to be checked.
   * Event-disjoint subsystems that share events with the properties cannot
   * be checked independently. Therefore, automata from event-disjoint
   * subsystems are collected in this list. When the last monolithic check is
   * requested, all plants are combined and checked together against the
   * properties.
   */
  private List<AutomatonProxy> mCollectedPlants;


  //#########################################################################
  //# Class Constants
  /**
   * Status of non-forbidden property events.
   * @see #mPropertyEventsMap
   */
  private static final byte REGULAR = 1;
  /**
   * Status of forbidden property events.
   * @see #mPropertyEventsMap
   */
  private static final byte FORBIDDEN = 2;

}
