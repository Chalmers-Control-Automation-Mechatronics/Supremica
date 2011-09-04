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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.cpp.analysis.NativeSafetyVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.EventNotFoundException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.TraceChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
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
   * Creates a new conflict checker without a model or marking proposition.
   * @param method
   *          Abstraction procedure used for simplification.
   * @param factory
   *          Factory used for trace construction.
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
   * Creates a new conflict checker to check whether the given model is
   * nonblocking with respect to its default marking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param method
   *          Abstraction procedure used for simplification.
   * @param factory
   *          Factory used for trace construction.
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
      new ProjectionAbstractionProcedure(chain);
    setAbstractionProcedure(proc);
    super.setUp();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mProperties = null;
    mPropertyEvents = null;
  }

  @Override
  protected void initialiseEventsToAutomata()
  {
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    final KindTranslator translator = getKindTranslator();
    mProperties = new ArrayList<AutomatonProxy>(numAutomata);
    final Collection<EventProxy> events = model.getEvents();
    final int numEvents = events.size();
    mPropertyEvents = new THashSet<EventProxy>(numEvents);
    for (final AutomatonProxy aut : automata) {
      if (translator.getComponentKind(aut) == ComponentKind.SPEC) {
        mProperties.add(aut);
        for (final EventProxy event : aut.getEvents()) {
          if (translator.getEventKind(event) != EventKind.PROPOSITION) {
            mPropertyEvents.addAll(aut.getEvents());
          }
        }
      }
    }
    super.initialiseEventsToAutomata();
  }

  @Override
  protected boolean canBeHidden(final EventProxy event)
  {
    return !mPropertyEvents.contains(event);
  }

  @Override
  protected boolean isSubsystemTrivial
    (final Collection<AutomatonProxy> automata)
  {
    for (final AutomatonProxy aut : automata) {
      for (final EventProxy event : aut.getEvents()) {
        if (mPropertyEvents.contains(event)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  protected ProductDESProxy createProductDESProxy
    (final List<EventProxy> events,
     final List<AutomatonProxy> automata,
     final boolean intermediate)
  {
    if (intermediate) {
      return super.createProductDESProxy(events, automata, true);
    } else {
      final int numAutomata = automata.size() + mProperties.size();
      final List<AutomatonProxy> plantsAndSpecs =
        new ArrayList<AutomatonProxy>(numAutomata);
      plantsAndSpecs.addAll(automata);
      plantsAndSpecs.addAll(mProperties);
      final Collection<EventProxy> eventSet = new THashSet<EventProxy>(events);
      final Collection<EventProxy> extraEvents = new LinkedList<EventProxy>();
      for (final AutomatonProxy aut : mProperties) {
        for (final EventProxy event : aut.getEvents()) {
          if (eventSet.add(event)) {
            extraEvents.add(event);
          }
        }
      }
      if (extraEvents.isEmpty()) {
        return super.createProductDESProxy(events, plantsAndSpecs, false);
      } else {
        final int numEvents = events.size() + extraEvents.size();
        final List<EventProxy> allEvents =
          new ArrayList<EventProxy>(numEvents);
        allEvents.addAll(events);
        allEvents.addAll(extraEvents);
        return super.createProductDESProxy(allEvents, plantsAndSpecs, false);
      }
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
  //# Inner Class ProjectionAbstractionProcedure
  private class ProjectionAbstractionProcedure
    extends TRSimplifierAbstractionProcedure
  {
    //#######################################################################
    //# Constructor
    ProjectionAbstractionProcedure
      (final TransitionRelationSimplifier simplifier)
    {
      super(simplifier);
    }

    //#######################################################################
    //# Overrides for TRSimplifierAbstractionProcedure
    @Override
    protected ProjectionStep createStep(final AutomatonProxy input,
                                        final StateEncoding inputStateEnc,
                                        final AutomatonProxy output,
                                        final StateEncoding outputStateEnc,
                                        final EventProxy tau)
    {
      return new ProjectionStep(output, input, tau);
    }
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
      final int numStates = rel.getNumberOfStates();
      for (int state = 0; state < numStates; state++) {
        if (rel.isInitial(state)) {
          final SearchRecord record = new SearchRecord(state);
          if (eventSteps.isEmpty()) {
            return record;
          }
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
            new SearchRecord(target, event, nextdepth, current);
          if (nextdepth == eventSteps.size()) {
            return next;
          }
          open.add(next);
        }
        iter.reset(state, tau);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          final SearchRecord next =
            new SearchRecord(target, tau, depth, current);
          open.add(next);
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
   * Collection of events used in properties. These events cannot be hidden
   * during compositional minimisation.
   */
  private Collection<EventProxy> mPropertyEvents;

}
