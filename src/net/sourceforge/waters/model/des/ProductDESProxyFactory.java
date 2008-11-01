//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   ProductDESProxyFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;


public interface ProductDESProxyFactory
{
  /**
   * Creates a new automaton.
   * @param name         The name of the new automaton.
   * @param kind         The kind (<I>plant</I>, <I>specification</I>, etc.)
   *                     of the new automaton.
   * @param events       The event alphabet of the new automaton,
   *                     or <CODE>null</CODE> if empty.
   * @param states       The list of states of the new automaton,
   *                     or <CODE>null</CODE> if empty.
   * @param transitions  The list of transitions of the new automaton,
   *                     or <CODE>null</CODE> if empty.
   */
  public AutomatonProxy createAutomatonProxy
      (String name,
       ComponentKind kind,
       Collection<? extends EventProxy> events,
       Collection<? extends StateProxy> states,
       Collection<? extends TransitionProxy> transitions);

  /**
   * Creates a new automaton using default values.
   * This method creates an automaton with empty lists of events,
   * states, and transitions.
   * @param name         The name of the new automaton.
   * @param kind         The kind (<I>plant</I>, <I>specification</I>, etc.)
   *                     of the new automaton.
   */
  public AutomatonProxy createAutomatonProxy
      (String name,
       ComponentKind kind);

  /**
   * Creates a new conflict trace.
   * @param  name         The name to be given to the new trace.
   * @param  comment      A comment describing the new trace,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  des          The product DES for which this trace is
   *                      generated.
   * @param  automata     The set of automata for the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @param  steps        The list of trace steps consituting the
   *                      new trace. This list may not be empty, because
   *                      the first step must always represent the
   *                      initial state.
   * @param  kind         The type of this conflict trace,
   *                      one of {@link ConflictKind#CONFLICT},
   *                      {@link ConflictKind#DEADLOCK}, or
   *                      {@link ConflictKind#LIVELOCK}.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  public ConflictTraceProxy createConflictTraceProxy
    (String name,
     String comment,
     URI location,
     ProductDESProxy des,
     Collection<? extends AutomatonProxy> automata,
     List<? extends TraceStepProxy> steps,
     ConflictKind kind);

  /**
   * Creates a new conflict trace using default values. This constructor
   * provides a simple interface to create a trace for a deterministic
   * product DES. It creates a trace with a <CODE>null</CODE> file
   * location, with no comment, with a set of automata equal to that of the
   * product DES, and without any state information in the trace steps.
   * @param  name         The name to be given to the new trace.
   * @param  des          The product DES for which the new trace is
   *                      generated.
   * @param  events       The list of events constituting the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @param  kind         The type of this conflict trace,
   *                      one of {@link ConflictKind#CONFLICT},
   *                      {@link ConflictKind#DEADLOCK}, or
   *                      {@link ConflictKind#LIVELOCK}.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  public ConflictTraceProxy createConflictTraceProxy
    (String name,
     ProductDESProxy des,
     List<? extends EventProxy> events,
     ConflictKind kind);

  /**
   * Creates a new event.
   * @param name         The name of the new event.
   * @param kind         The kind of the new event.
   * @param observable   The observability status of the new event.
   */
  public EventProxy createEventProxy
      (String name,
       EventKind kind,
       boolean observable);

  /**
   * Creates a new event using default values.
   * This method creates an observable event.
   * @param name         The name of the new event.
   * @param kind         The kind of the new event.
   */
  public EventProxy createEventProxy
      (String name,
       EventKind kind);

  /**
   * Creates a new loop trace.
   * @param  name         The name to be given to the new trace.
   * @param  comment      A comment describing the new trace,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  des          The product DES for which this trace is
   *                      generated.
   * @param  automata     The set of automata for the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @param  steps        The list of trace steps consituting the
   *                      new trace. This list may not be empty, because
   *                      the first step must always represent the
   *                      initial state.
   * @param  index        The loop index of the new trace.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  public LoopTraceProxy createLoopTraceProxy
    (String name,
     String comment,
     URI location,
     ProductDESProxy des,
     Collection<? extends AutomatonProxy> automata,
     List<? extends TraceStepProxy> steps,
     int index);

  /**
   * Creates a new loop trace using default values.  This constructor
   * provides a simple interface to create a trace for a deterministic
   * product DES. It creates a trace with a <CODE>null</CODE> file
   * location, with no comment, with a set of automata equal to that of
   * the product DES, and without any state information in the trace steps.
   * @param  name         The name to be given to the new trace.
   * @param  des          The product DES for which the new trace is
   *                      generated.
   * @param  events       The list of events constituting the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @param  index        The loop index of the new trace.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  public LoopTraceProxy createLoopTraceProxy
    (String name,
     ProductDESProxy des,
     List<? extends EventProxy> events,
     int index);

  /**
   * Creates a new product DES.
   * @param name         The name of the new product DES.
   * @param comment      The comment to be associated with the new product DES,
   *                     or <CODE>null</CODE>.
   * @param location     The file location of the new product DES,
   *                     or <CODE>null</CODE>.
   * @param events       The event alphabet of the new product DES,
   *                     or <CODE>null</CODE> if empty.
   * @param automata     The list of automata of the new product DES,
   *                     or <CODE>null</CODE> if empty.
   */
  public ProductDESProxy createProductDESProxy
      (String name,
       String comment,
       URI location,
       Collection<? extends EventProxy> events,
       Collection<? extends AutomatonProxy> automata);

  /**
   * Creates a new product DES using default values.
   * This method creates a product DES with a <CODE>null</CODE> file location,
   * and with no associated comment.
   * @param name         The name of the new product DES.
   * @param events       The event alphabet of the new product DES,
   *                     or <CODE>null</CODE> if empty.
   * @param automata     The list of automata of the new product DES,
   *                     or <CODE>null</CODE> if empty.
   */
  public ProductDESProxy createProductDESProxy
      (String name,
       Collection<? extends EventProxy> events,
       Collection<? extends AutomatonProxy> automata);

  /**
   * Creates a new product DES using default values.
   * This method creates a product DES with a <CODE>null</CODE> file location,
   * and empty lists of events and automata.
   * @param name         The name of the new product DES.
   */
  public ProductDESProxy createProductDESProxy
      (String name);

  /**
   * Creates a new safety trace.
   * @param  name         The name to be given to the new trace.
   * @param  comment      A comment describing the new trace,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  des          The product DES for which this trace is
   *                      generated.
   * @param  automata     The set of automata for the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @param  steps        The list of trace steps consituting the
   *                      new trace. This list may not be empty, because
   *                      the first step must always represent the
   *                      initial state.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  public SafetyTraceProxy createSafetyTraceProxy
    (String name,
     String comment,
     URI location,
     ProductDESProxy des,
     Collection<? extends AutomatonProxy> automata,
     List<? extends TraceStepProxy> steps);

  /**
   * Creates a new safety trace using default values. This method provides
   * a simple interface to create a trace for a deterministic product DES. 
   * It creates a trace with a <CODE>null</CODE> file location, with no
   * comment, with a set of automata equal to that of the product DES, and
   * without any state information in the trace steps.
   * @param  name         The name to be given to the new trace.
   * @param  des          The product DES for which the new trace is
   *                      generated.
   * @param  events       The list of events constituting the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  public SafetyTraceProxy createSafetyTraceProxy
    (String name,
     ProductDESProxy des,
     List<? extends EventProxy> events);

  /**
   * Creates a new safety trace using default values.  This method
   * provides a simple interface to create a controllability error trace
   * for a deterministic product DES. It creates a trace with a
   * <CODE>null</CODE> file location, with the name of the product DES
   * catenated with <CODE>&quot:uncontrollable&quot;</CODE>, with a set of
   * automata equal to that of the product DES, and without any state
   * information in the trace steps.
   * @param  des          The product DES for which the new trace is
   *                      generated.
   * @param  events       The list of events constituting the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  public SafetyTraceProxy createSafetyTraceProxy
    (ProductDESProxy des,
     List<? extends EventProxy> events);

  /**
   * Creates a new state.
   * @param name         The name of the new state.
   * @param initial      The initial status of the new state.
   * @param propositions The list of propositions of the new state,
   *                     or <CODE>null</CODE> if empty.
   */
  public StateProxy createStateProxy
      (String name,
       boolean initial,
       Collection<? extends EventProxy> propositions);

  /**
   * Creates a new state using default values.
   * This method creates a state that is not initial and has no propositions.
   * @param name         The name of the new state.
   */
  public StateProxy createStateProxy
      (String name);

  /**
   * Creates a new trace step.
   * @param  event        The event associated with the new step,
   *                      or <CODE>null</CODE>.
   * @param  statemap     The map that maps automata mentioned by the trace
   *                      to their states reached after the step represented
   *                      by this object. This map is copied when creating
   *                      the step object, so later changes to it will have
   *                      no impact on the object. This parameter may be
   *                      <CODE>null</CODE> for an empty map.
   */
  public TraceStepProxy createTraceStepProxy
    (EventProxy event,
     Map<AutomatonProxy,StateProxy> statemap);

  /**
   * Creates a new trace step with an empty state map.
   * @param  event        The event associated with the new step.
   */
  public TraceStepProxy createTraceStepProxy(EventProxy event);

  /**
   * Creates a new transition.
   * @param source       The source state of the new transition.
   * @param event        The event labelling the new transition.
   * @param target       The target state of the new transition.
   */
  public TransitionProxy createTransitionProxy
      (StateProxy source,
       EventProxy event,
       StateProxy target);

}
