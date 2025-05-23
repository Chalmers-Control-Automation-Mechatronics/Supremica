//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.des;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.ItemNotFoundException;


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
   * @param attribs      The attribute map for the new automaton.
   */
  public AutomatonProxy createAutomatonProxy
      (String name,
       ComponentKind kind,
       Collection<? extends EventProxy> events,
       Collection<? extends StateProxy> states,
       Collection<? extends TransitionProxy> transitions,
       Map<String,String> attribs);

  /**
   * Creates a new automaton without attributes.
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
   * Creates a new conflict counterexample.
   * @param  name         The name to be given to the new counterexample.
   * @param  comment      A comment describing the new counterexample,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  des          The product DES for which this counterexample is
   *                      generated.
   * @param  automata     The set of automata for the new counterexample,
   *                      or <CODE>null</CODE> if empty.
   * @param  trace        The trace that defines the new counterexample.
   * @param  kind         The type of this conflict counterexample,
   *                      one of {@link ConflictKind#CONFLICT},
   *                      {@link ConflictKind#DEADLOCK}, or
   *                      {@link ConflictKind#LIVELOCK}.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  public ConflictCounterExampleProxy createConflictCounterExampleProxy
    (String name,
     String comment,
     URI location,
     ProductDESProxy des,
     Collection<? extends AutomatonProxy> automata,
     TraceProxy trace,
     ConflictKind kind);

  /**
   * Creates a new conflict counterexample using default values. This method
   * provides a simple interface to create a counterexample for a deterministic
   * product DES. It creates a counterexample with a <CODE>null</CODE> file
   * location, with no comment, with a set of automata equal to that of the
   * product DES, and without any state information in the trace steps.
   * @param  name         The name to be given to the new counterexample.
   * @param  des          The product DES for which the new counterexample is
   *                      generated.
   * @param  events       The list of events constituting the new
   *                      counterexample, or <CODE>null</CODE> if empty.
   * @param  kind         The type of this conflict counterexample,
   *                      one of {@link ConflictKind#CONFLICT},
   *                      {@link ConflictKind#DEADLOCK}, or
   *                      {@link ConflictKind#LIVELOCK}.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  public ConflictCounterExampleProxy createConflictCounterExampleProxy
    (String name,
     ProductDESProxy des,
     List<? extends EventProxy> events,
     ConflictKind kind);


  /**
   * Creates a new coobservability counterexample.
   * @param  name         The name to be given to the new counterexample.
   * @param  comment      A comment describing the new counterexample,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  des          The product DES for which this counterexample is
   *                      generated.
   * @param  automata     The set of automata for the new counterexample,
   *                      or <CODE>null</CODE> if empty.
   * @param  traces       The list of traces constituting the new counterexample.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  public CoobservabilityCounterExampleProxy createCoobservabilityCounterExampleProxy
    (String name,
     String comment,
     URI location,
     ProductDESProxy des,
     Collection<? extends AutomatonProxy> automata,
     List<TraceProxy> traces);


  /**
   * Creates a new dual counterexample.
   * @param  name         The name to be given to the new counterexample.
   * @param  comment      A comment describing the new counterexample,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  des          The product DES for which this counterexample is
   *                      generated.
   * @param  automata     The set of automata for the new counterexample,
   *                      or <CODE>null</CODE> if empty.
   * @param  trace1       The first of the two traces that define the dual
   *                      counterexample.
   * @param  trace2       The second of the two traces that define the dual
   *                      counterexample.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  public DualCounterExampleProxy createDualCounterExampleProxy
    (String name,
     String comment,
     URI location,
     ProductDESProxy des,
     Collection<? extends AutomatonProxy> automata,
     TraceProxy trace1,
     TraceProxy trace2);


  /**
   * Creates a new event.
   * @param name         The name of the new event.
   * @param kind         The kind of the new event.
   * @param observable   The observability status of the new event.
   * @param attribs      The attribute map for the new event.
   */
  public EventProxy createEventProxy
      (String name,
       EventKind kind,
       boolean observable,
       Map<String,String> attribs);

  /**
   * Creates a new event without attributes.
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
   * Creates a new loop counterexample.
   * @param  name         The name to be given to the new counterexample.
   * @param  comment      A comment describing the new counterexample,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  des          The product DES for which this counterexample is
   *                      generated.
   * @param  automata     The set of automata for the new counterexample,
   *                      or <CODE>null</CODE> if empty.
   * @param  trace        The trace that defines the new counterexample.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  public LoopCounterExampleProxy createLoopCounterExampleProxy
    (String name,
     String comment,
     URI location,
     ProductDESProxy des,
     Collection<? extends AutomatonProxy> automata,
     TraceProxy trace);

  /**
   * Creates a new loop counterexample using default values. This method
   * provides a simple interface to create a counterexample for a deterministic
   * product DES. It creates a counterexample with a <CODE>null</CODE> file
   * location, with no comment, with a set of automata equal to that of
   * the product DES, and without any state information in the trace steps.
   * @param  name         The name to be given to the new counterexample.
   * @param  des          The product DES for which the new counterexample is
   *                      generated.
   * @param  events       The list of events constituting the new
   *                      counterexample, or <CODE>null</CODE> if empty.
   * @param  index        The loop index of the new counterexample.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  public LoopCounterExampleProxy createLoopCounterExampleProxy
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
   * Creates a new safety counterexample.
   * @param  name         The name to be given to the new counterexample.
   * @param  comment      A comment describing the new counterexample,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  des          The product DES for which this counterexample is
   *                      generated.
   * @param  automata     The set of automata for the new counterexample,
   *                      or <CODE>null</CODE> if empty.
   * @param  trace        The trace that defines the new counterexample.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  public SafetyCounterExampleProxy createSafetyCounterExampleProxy
    (String name,
     String comment,
     URI location,
     ProductDESProxy des,
     Collection<? extends AutomatonProxy> automata,
     TraceProxy trace);

  /**
   * Creates a new safety counterexample using default values. This method
   * provides a simple interface to create a counterexample for a
   * deterministic product DES. It creates a counterexample with a
   * <CODE>null</CODE> file location, with no comment, with a set of automata
   * equal to that of the product DES, and without any state information in
   * the trace steps.
   * @param  name         The name to be given to the new counterexample.
   * @param  des          The product DES for which the new counterexample is
   *                      generated.
   * @param  events       The list of events constituting the new counterexample,
   *                      or <CODE>null</CODE> if empty.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  public SafetyCounterExampleProxy createSafetyCounterExampleProxy
    (String name,
     ProductDESProxy des,
     List<? extends EventProxy> events);

  /**
   * Creates a new safety counterexample using default values. This method
   * provides a simple interface to create a controllability counterexample
   * for a deterministic product DES. It creates a trace with a
   * <CODE>null</CODE> file location, with the name of the product DES
   * concatenated with <CODE>&quot;uncontrollable&quot;</CODE>, with a set of
   * automata equal to that of the product DES, and without any state
   * information in the trace steps.
   * @param  des          The product DES for which the new counterexample is
   *                      generated.
   * @param  events       The list of events constituting the new
   *                      counterexample, or <CODE>null</CODE> if empty.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  public SafetyCounterExampleProxy createSafetyCounterExampleProxy
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
   * Creates a new trace.
   * @param  name         The name to be given to the new trace.
   * @param  steps        The list of trace steps constituting the
   *                      new trace. This list may not be empty, because
   *                      the first step must always represent the
   *                      initial state.
   * @param  loopIndex    The loop index of the new trace.
   */
  public TraceProxy createTraceProxy(String name,
                                     List<? extends TraceStepProxy> steps,
                                     int loopIndex);

  /**
   * Creates a new trace with default settings.
   * This method creates a trace, which may include a cycle, using an
   * empty string as its name.
   * @param  steps        The list of trace steps constituting the
   *                      new trace. This list may not be empty, because
   *                      the first step must always represent the
   *                      initial state.
   * @param  loopIndex    The loop index of the new trace.
   */
  public TraceProxy createTraceProxy(List<? extends TraceStepProxy> steps,
                                     int loopIndex);

  /**
   * Creates a new trace with default settings.
   * This method creates a trace without cycle, using an empty string as
   * its name.
   * @param  steps        The list of trace steps constituting the
   *                      new trace. This list may not be empty, because
   *                      the first step must always represent the
   *                      initial state.
   */
  public TraceProxy createTraceProxy(List<? extends TraceStepProxy> steps);

  /**
   * Creates a new trace using default values. This method
   * provides a simple interface to create a trace for a deterministic
   * system. It creates a trace, which may include a cycle, without any state
   * information in the trace steps and using an empty string as its name.
   * @param  events       The list of events constituting the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @param  loopIndex    The loop index of the new trace.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  public TraceProxy createTraceProxyDeterministic
    (List<? extends EventProxy> events, int loopIndex);

  /**
   * Creates a new trace using default values. This method
   * provides a simple interface to create a trace for a deterministic
   * system. It creates a trace without cycle, without any state
   * information in the trace steps and using an empty string as its name.
   * @param  events       The list of events constituting the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  public TraceProxy createTraceProxyDeterministic
    (List<? extends EventProxy> events);


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
