//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CompositionalSynthesizer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.EventKind;


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


public abstract class CompositionalSynthesizer
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
  protected CompositionalSynthesizer
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
  protected CompositionalSynthesizer
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
  protected CompositionalSynthesizer
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
  protected CompositionalSynthesizer
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
  //# Invocation
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      runCompositionalMinimisation();
      final CompositionalSynthesisResult result = getAnalysisResult();
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
    // TODO Add other abstractions ...
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    /*
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
     */
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
      new SynthesisAbstractionProcedure(chain, synthesisAbstraction);
    setAbstractionProcedure(proc);
    proc.storeStatistics();

    // TODO More initialisation ?
    /*
    setupMonolithicVerifier();
    */

    super.setUp();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    // TODO Clean up ...
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
    // TODO Add any distinguishers to analysis result ...
  }

  @Override
  protected boolean doMonolithicAnalysis
    (final List<AutomatonProxy> automata)
    throws AnalysisException
  {
    // TODO Invoke monolithic synthesis and store result ...
    return false;
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
       final SynthesisAbstractionTRSimplifier synthesisAbstraction)
    {
      mSimplifier = simplifier;
      mSynthesisAbstraction = synthesisAbstraction;
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
        final StateEncoding inputStateEnc = new StateEncoding(aut);
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
          return new SynthesisAbstractionStep(aut, convertedAut);
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
      mSynthesisAbstraction.setLastControllableLocalEvent
        (lastControllableLocalEvent);
      mSynthesisAbstraction.setLastUncontrollableLocalEvent
        (lastUncontrollableLocalEvent);
      mSynthesisAbstraction.setLastUncontrollableSharedEvent
        (lastUncontrollableSharedEvent);
      encodedEvents.addAll(localUncontrollableEvents);
      encodedEvents.addAll(localControllableEvents);
      encodedEvents.addAll(sharedUncontrollableEvents);
      encodedEvents.addAll(sharedControllableEvents);
      return new EventEncoding(encodedEvents, translator, filter,
                               EventEncoding.FILTER_PROPOSITIONS);
    }

    //#######################################################################
    //# Data Members
    private final TransitionRelationSimplifier mSimplifier;
    private final SynthesisAbstractionTRSimplifier mSynthesisAbstraction;
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
                                       final AutomatonProxy original)
    {
      super(result, original);
    }
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

}
