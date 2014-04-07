//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   AbstractCompositionalSynthesizer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
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
 * Sahar Mohajerani, Robi Malik, Simon Ware, Martin Fabian. On the Use of
 * Observation Equivalence in Synthesis Abstraction. Proc. 3rd IFAC Workshop
 * on Dependable Control of Discrete Systems, DCDS&nbsp;2011,
 * Saarbr&uuml;cken, Germany, 2011.<BR>
 * Sahar Mohajerani, Robi Malik, Martin Fabian. Nondeterminism Avoidance in
 * Compositional Synthesis of Discrete Event Systems, Proc. 7th International
 * Conference on Automation Science and Engineering, CASE&nbsp;2011, Trieste,
 * Italy.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public abstract class AbstractCompositionalSynthesizer extends
  AbstractCompositionalModelAnalyzer implements SupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a compositional synthesiser to compute a supervisor for the given
   * model.
   *
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   */
  public AbstractCompositionalSynthesizer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureFactory abstractionFactory,
     final PreselectingMethodFactory preselectingMethodFactory)
  {
    super(model, factory, translator, abstractionFactory,
          preselectingMethodFactory);
    setPruningDeadlocks(true);
    setFailingEventsEnabled(true);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelBuilder
  @Override
  public void setOutputName(final String name)
  {
    mOutputName = name;
  }

  @Override
  public String getOutputName()
  {
    return mOutputName;
  }

  @Override
  public ProductDESProxy getComputedProxy()
  {
    final ProductDESResult result = getAnalysisResult();
    return result.getComputedProductDES();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ProductDESBuilder
  @Override
  public ProductDESProxy getComputedProductDES()
  {
    return getComputedProxy();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  public CompositionalSynthesisResult getAnalysisResult()
  {
    return (CompositionalSynthesisResult) super.getAnalysisResult();
  }

  @Override
  protected void setUp() throws AnalysisException
  {
    final EventProxy defaultMarking = createDefaultMarking();
    setPropositionsForMarkings(defaultMarking, null);
    super.setUp();
  }


  //#########################################################################
  //# Hooks
  @Override
  protected AutomatonProxy plantify(final AutomatonProxy spec)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> events = spec.getEvents();
    final int numEvents = events.size();
    final Collection<EventProxy> uncontrollables =
      new ArrayList<EventProxy>(numEvents);
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
        uncontrollables.add(event);
      }
    }
    final EventEncoding eventEnc =
      new EventEncoding(uncontrollables, translator);
    final StateEncoding stateEnc = new StateEncoding(spec);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(
                                       spec,
                                       eventEnc,
                                       stateEnc,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final int numStates = rel.getNumberOfStates();
    final Collection<StateProxy> states =
      new ArrayList<StateProxy>(numStates + 1);
    states.addAll(spec.getStates());
    StateProxy dump = null;
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(spec.getTransitions());
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final ProductDESProxyFactory factory = getFactory();
    for (int s = 0; s < numStates; s++) {
      final StateProxy state = stateEnc.getState(s);
      for (final EventProxy event : uncontrollables) {
        final int e = eventEnc.getEventCode(event);
        iter.reset(s, e);
        if (!iter.advance()) {
          if (dump == null) {
            dump = factory.createStateProxy(":dump");
            states.add(dump);
          }
          final TransitionProxy trans =
            factory.createTransitionProxy(state, event, dump);
          transitions.add(trans);
        }
      }
    }

    final String name = spec.getName();
    final EventProxy defaultMarking = getUsedDefaultMarking();
    if (dump != null & !events.contains(defaultMarking)) {
      final Collection<TransitionProxy> newTransitions =
        new ArrayList<TransitionProxy>();
      final Collection<EventProxy> newEvents =
        new ArrayList<EventProxy>(numEvents + 1);
      newEvents.addAll(events);
      newEvents.add(defaultMarking);
      final Collection<StateProxy> newStates =
        new ArrayList<StateProxy>(numStates + 1);
      final HashMap<StateProxy,StateProxy> mapStates =
        new HashMap<StateProxy,StateProxy>(numStates + 1);
      for (final StateProxy state : spec.getStates()) {
        final Collection<EventProxy> propositions = state.getPropositions();
        final Collection<EventProxy> newPropostions =
          new ArrayList<EventProxy>(propositions.size() + 1);
        newPropostions.addAll(propositions);
        newPropostions.add(defaultMarking);
        final StateProxy newState =
          factory.createStateProxy(state.getName(), state.isInitial(),
                                   newPropostions);
        newStates.add(newState);
        mapStates.put(state, newState);
      }
      newStates.add(dump);
      mapStates.put(dump, dump);
      for (final TransitionProxy trans : transitions) {
        final StateProxy sourceState = trans.getSource();
        final StateProxy targetState = trans.getTarget();
        final EventProxy event = trans.getEvent();
        final TransitionProxy newTransition =
          factory.createTransitionProxy(mapStates.get(sourceState), event,
                                        mapStates.get(targetState));
        newTransitions.add(newTransition);
      }
      return factory.createAutomatonProxy(name, ComponentKind.PLANT,
                                          newEvents, newStates,
                                          newTransitions);
    } else {
      return factory.createAutomatonProxy(name, ComponentKind.PLANT, events,
                                          states, transitions);
    }
  }

  @Override
  protected SynthesisEventInfo createEventInfo(final EventProxy event)
  {
    return new SynthesisEventInfo(event);
  }

  @Override
  protected boolean isPermissibleCandidate(final List<AutomatonProxy> automata)
  {
    return super.isPermissibleCandidate(automata)
           && automata.size() < getCurrentAutomata().size();
  }

  /**
   * Returns whether failure events are considered in abstraction.
   * @return <CODE>true</CODE> if failure events have been enabled by
   *         configuration (which they are by default).
   */
  @Override
  protected boolean isUsingFailingEvents()
  {
    return isFailingEventsEnabled();
  }


  //#########################################################################
  //# Debugging
  void reportAbstractionResult(final AutomatonProxy aut,
                               final AutomatonProxy dist)
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      /*
       * final boolean nonblocking = AnalysisTools.isNonBlocking(aut); final
       * String msg1 = "Simplified automaton is " + (nonblocking ?
       * "nonblocking." : "BLOCKING."); logger.debug(msg1);
       */
      if (dist != null) {
        final String msg2 =
          "Creating distinguisher '" + dist.getName() + "' with "
            + dist.getStates().size() + " states.";
        logger.debug(msg2);
      }
    }
  }

  void reportSupervisor(final String kind,
                        final ListBufferTransitionRelation sup)
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled() && sup != null) {
      final String msg =
        "Got " + kind + " supervisor '" + sup.getName() + "' with " +
        sup.getNumberOfReachableStates() + " states.";
      logger.debug(msg);
    }
  }


  //#########################################################################
  //# Inner Class SynthesisEventInfo
  /**
   * An event information record for compositional synthesis. In compositional
   * synthesis, there are no tau events, yet all events are subject to
   * selfloop removal. Also, only uncontrollable events are considered
   * as failing.
   */
  protected class SynthesisEventInfo extends EventInfo
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
    protected boolean canBeTau()
    {
      return false;
    }

    @Override
    protected boolean isSubjectToSelfloopRemoval()
    {
      return true;
    }

    @Override
    protected boolean isFailing()
    {
      if (super.isFailing()) {
        final KindTranslator translator = getKindTranslator();
        final EventProxy event = getEvent();
        return translator.getEventKind(event) == EventKind.UNCONTROLLABLE;
      } else {
        return false;
      }
    }
  }


  //#########################################################################
  //# Data Members
  private String mOutputName;

}
