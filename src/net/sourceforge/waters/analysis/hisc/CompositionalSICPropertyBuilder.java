//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.hisc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;
import net.sourceforge.waters.analysis.hisc.HISCAttributeFactory;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;


/**
 * A converter to translate models representing HISC low level modules
 * into multi-coloured models for checking SIC or LDIC properties V and VI.
 *
 * @author Rachel Francis, Robi Malik
 */

public class CompositionalSICPropertyBuilder
{

  //#########################################################################
  //# Constructors
  public CompositionalSICPropertyBuilder(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
    mModel = null;
  }

  public CompositionalSICPropertyBuilder(final ProductDESProxy model,
                                         final ProductDESProxyFactory factory)
  {
    mModel = model;
    mFactory = factory;
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the HISC model to be converted.
   */
  public void setInputModel(final ProductDESProxy model)
  {
    mModel = model;
  }

  public ProductDESProxy getUnchangedModel()
  {
    return mModel;
  }

  public void setInputMarking(final EventProxy marking)
  {
    mInputMarking = marking;
  }

  public EventProxy getInputMarking()
  {
    return mInputMarking;
  }

  public void setOutputMarking(final EventProxy marking)
  {
    mOutputMarking = marking;
  }

  public EventProxy getOutputMarking()
  {
    return mOutputMarking;
  }

  public void setGeneralisedPrecondition(final EventProxy marking)
  {
    mPreconditionMarking = marking;
  }

  public EventProxy getGeneralisedPrecondition()
  {
    return mPreconditionMarking;
  }

  public void setOutputMarkings()
  {
    final String omega = ":omega";
    mOutputMarking =
      mFactory.createEventProxy(omega, EventKind.PROPOSITION, true);
    final String alpha = ":alpha";
    mPreconditionMarking =
      mFactory.createEventProxy(alpha, EventKind.PROPOSITION, true);
  }


  //#########################################################################
  //# Invocation
  /**
   * Gets all the answer events that belong to the model.
   */
  public Collection<EventProxy> getAnswerEvents()
  {
    final Set<EventProxy> allEvents = mModel.getEvents();
    final List<EventProxy> answerEvents = new ArrayList<EventProxy>(0);
    for (final EventProxy event : allEvents) {
      if (HISCAttributeFactory.getEventType(event.getAttributes()) ==
          HISCAttributeFactory.EventType.ANSWER) {
        answerEvents.add(event);
      }
    }
    return answerEvents;
  }

  /**
   * Builds a model for checking SIC Property V with respect to the
   * given answer event.
   */
  public ProductDESProxy createSIC5Model(final EventProxy answer)
  throws AnalysisException
  {
    if (mOutputMarking == null) {
      final String omega = ":omega";
      mOutputMarking = mFactory.createEventProxy(omega, EventKind.PROPOSITION);
    }
    if (mPreconditionMarking == null) {
      final String alpha = ":alpha";
      mPreconditionMarking =
        mFactory.createEventProxy(alpha, EventKind.PROPOSITION);
    }
    final Collection<EventProxy> props = new ArrayList<EventProxy>(2);
    props.add(mOutputMarking);
    props.add(mPreconditionMarking);

    final Collection<EventProxy> oldEvents = mModel.getEvents();
    final int numEvents = oldEvents.size();
    final List<EventProxy> newEvents = new ArrayList<EventProxy>(numEvents);
    newEvents.addAll(props);
    final List<EventProxy> iface = new ArrayList<EventProxy>();
    for (final EventProxy event : oldEvents) {
      if (event.getKind() != EventKind.PROPOSITION) {
        newEvents.add(event);
        final Map<String,String> attribs = event.getAttributes();
        switch (HISCAttributeFactory.getEventType(attribs)) {
        case REQUEST:
        case ANSWER:
        case LOWDATA:
          iface.add(event);
          break;
        default:
          break;
        }
      }
    }

    final SynchronousProductBuilder builder =
      new MonolithicSynchronousProductBuilder(mFactory);
    builder.setPropositions(props);
    final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata();
    final int numaut = oldAutomata.size();
    final List<AutomatonProxy> newAutomata =
      new ArrayList<AutomatonProxy>(numaut + 1);
    final AutomatonProxy testaut =
      createConstantSIC5Test(answer, iface, "test:sic5");
    newAutomata.add(testaut);
    for (final AutomatonProxy aut : oldAutomata) {
      final Map<String,String> attribs = aut.getAttributes();
      if (HISCAttributeFactory.isInterface(attribs)) {
        final AutomatonProxy iaut = createSIC5InterfaceAutomaton(aut, answer);
        newAutomata.add(iaut);
      } else {
        final AutomatonProxy laut =
          createCompositionalSIC5Automaton(aut, answer, iface, builder);
        newAutomata.add(laut);
      }
    }
    final String desname = mModel.getName();
    final String ansname = answer.getName();
    final String name = desname + '-' + ansname.replace(':', '-');
    final String comment =
      "Automatically generated from '" + desname +
      "' to check SIC Property V with respect to answer event '" + ansname +
      "'.";
    final ProductDESProxy newModel =
        mFactory.createProductDESProxy(name, comment, null, newEvents,
                                       newAutomata);
    // MarshallingTools.saveModule(newModel, "xxx.wmod");
    return newModel;
  }

  /**
   * Builds a model for checking SIC Property VI.
   */
  public ProductDESProxy createSIC6Model()
  {
    assert mInputMarking != null :
      "SIC Property VI conversion requires default marking of input model!";
    if (mPreconditionMarking == null) {
      final String alphaNm = ":alpha";
      mPreconditionMarking =
          mFactory.createEventProxy(alphaNm, EventKind.PROPOSITION, true);
    }
    if (mTau == null) {
      mTau =
        mFactory.createEventProxy(":sic6", EventKind.UNCONTROLLABLE, false);
    }

    final Collection<EventProxy> oldEvents = mModel.getEvents();
    final Collection<EventProxy> newEvents =
      removePropositions(oldEvents, mInputMarking);
    newEvents.add(mTau);

    final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata();
    final int numaut = oldAutomata.size();
    final List<AutomatonProxy> newAutomata =
      new ArrayList<AutomatonProxy>(numaut);
    for (final AutomatonProxy oldAut : oldAutomata) {
      final AutomatonProxy newAut;
      final Map<String,String> attribs = oldAut.getAttributes();
      if (HISCAttributeFactory.isInterface(attribs)) {
        newAut = createSIC6InterfaceAutomaton(oldAut);
      } else {
        newAut = removePropositions(oldAut, mInputMarking);
      }
      newAutomata.add(newAut);
    }
    final AutomatonProxy testAut = createSIC6Test();
    newAutomata.add(testAut);

    final String desname = mModel.getName();
    final String name = desname + "-sic6";
    final String comment =
      "Automatically generated from '" + desname +
      "' to check SIC Property VI.";
    return mFactory.createProductDESProxy(name, comment, null,
                                          newEvents, newAutomata);
  }

  public ConflictTraceProxy convertTraceToOriginalModel
    (final ConflictTraceProxy conflictTrace, final EventProxy answer)
  {
    // creates a map of the original model's automaton names to the object for
    // that automaton and a map of the names of the states for that automaton to
    // the state proxy objects
    final Map<String,AutomatonProxy> autMap =
      new HashMap<String,AutomatonProxy>();
    final Map<String,Map<String,StateProxy>> stateMap =
      new HashMap<String,Map<String,StateProxy>>();
    for (final AutomatonProxy aut : mModel.getAutomata()) {
      final String autName = aut.getName();
      autMap.put(autName, aut);
      final Map<String,StateProxy> innerStateMap =
        new HashMap<String,StateProxy>();
      final Set<StateProxy> states = aut.getStates();
      for (final StateProxy state : states) {
        final String statenm = state.getName();
        innerStateMap.put(statenm, state);
      }
      stateMap.put(autName, innerStateMap);
    }
    final List<TraceStepProxy> traceSteps = conflictTrace.getTraceSteps();
    final List<TraceStepProxy> convertedSteps = new ArrayList<TraceStepProxy>();
    for (final TraceStepProxy step : traceSteps) {
      final EventProxy event = step.getEvent();
      if (event != mTau || event == null) {
        final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
        final Map<AutomatonProxy,StateProxy> convertedStepMap =
          new HashMap<AutomatonProxy,StateProxy>();
        for (final Map.Entry<AutomatonProxy,StateProxy> entry :
             stepMap.entrySet()) {
          final AutomatonProxy convertedAut = entry.getKey();
          final String autName = convertedAut.getName();
          final AutomatonProxy originalAut = autMap.get(autName);
          if (originalAut != null) {
            final StateProxy convertedState = entry.getValue();
            final String stateName = convertedState.getName();
            final StateProxy originalState =
              stateMap.get(autName).get(stateName);
            convertedStepMap.put(originalAut, originalState);
          }
        }
        final TraceStepProxy convertedStep =
          mFactory.createTraceStepProxy(step.getEvent(), convertedStepMap);
        convertedSteps.add(convertedStep);
      }
    }
    final String modelname = mModel.getName();
    final String tracename;
    String comment;
    if (answer != null) {
      tracename = modelname + "-sic5";
      comment = modelname + " does not satisfy SIC Property V. " +
        "The answer event " + answer.getName() +
        " can no longer be executed, although it is required by the interface.";
    } else {
      tracename = modelname + "-sic6";
      comment = modelname + " does not satisfy SIC Property VI.";
    }
    final ConflictKind kind = conflictTrace.getKind();
    final ConflictTraceProxy convertedTrace =
      mFactory.createConflictTraceProxy(tracename, comment, null,
                                        mModel, mModel.getAutomata(),
                                        convertedSteps, kind);
    return convertedTrace;
  }


  //#########################################################################
  //# Auxiliary Methods
  private AutomatonProxy createCompositionalSIC5Automaton
    (final AutomatonProxy aut,
     final EventProxy answer,
     final List<EventProxy> iface,
     final SynchronousProductBuilder builder)
  throws AnalysisException
  {
    final Collection<EventProxy> events = aut.getEvents();
    int numEvents = events.size();
    final List<EventProxy> local = new ArrayList<EventProxy>(numEvents);
    final Collection<EventProxy> props = builder.getPropositions();
    numEvents += iface.size() + props.size();
    final List<EventProxy> global = new ArrayList<EventProxy>(numEvents);
    global.addAll(props);
    global.addAll(iface);
    for (final EventProxy event : events) {
      if (event.getKind() == EventKind.PROPOSITION) {
        global.add(event);
      } else {
        final Map<String,String> attribs = event.getAttributes();
        switch (HISCAttributeFactory.getEventType(attribs)) {
        case DEFAULT:
          local.add(event);
          global.add(event);
          break;
        default:
          break;
        }
      }
    }
    final AutomatonProxy testaut = createLocalSIC5Test(answer, iface, local, "test");
    final String name = aut.getName();
    final ComponentKind kind = aut.getKind();
    final List<AutomatonProxy> automata = new ArrayList<AutomatonProxy>(2);
    automata.add(aut);
    automata.add(testaut);
    final ProductDESProxy des =
      mFactory.createProductDESProxy(name, global, automata);
    builder.setModel(des);
    builder.setOutputName(name);
    builder.setOutputKind(kind);
    builder.run();
    return builder.getComputedAutomaton();
  }

  private AutomatonProxy createConstantSIC5Test(final EventProxy answer,
                                                final List<EventProxy> iface,
                                                final String name)
  {
    final int numEvents = iface.size() + 2;
    final List<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    events.addAll(iface);

    // Create the 2 states ...
    final List<StateProxy> states = new ArrayList<StateProxy>(2);
    // initial state has the default marking proposition
    List<EventProxy> propositions = Collections.singletonList(mOutputMarking);
    final StateProxy initialState =
        mFactory.createStateProxy("T1", true, propositions);
    states.add(initialState);
    // next state has the precondition marking
    propositions = Collections.singletonList(mPreconditionMarking);
    final StateProxy alphaState =
        mFactory.createStateProxy("T2", false, propositions);
    states.add(alphaState);

    // Create the transitions ...
    final List<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(2 * numEvents);
    for (final EventProxy event : events) {
      // Selfloop on the initial state that includes entire event alphabet ...
      TransitionProxy trans =
        mFactory.createTransitionProxy(initialState, event, initialState);
      transitions.add(trans);
      // The transition which accepts any request event ...
      final Map<String,String> attribs = event.getAttributes();
      final HISCAttributeFactory.EventType etype =
        HISCAttributeFactory.getEventType(attribs);
      if (etype == HISCAttributeFactory.EventType.REQUEST) {
        trans = mFactory.createTransitionProxy(initialState, event, alphaState);
        transitions.add(trans);
      }
    }
    // The answer transition ...
    final TransitionProxy trans =
        mFactory.createTransitionProxy(alphaState, answer, initialState);
    transitions.add(trans);
    events.add(mOutputMarking);
    events.add(mPreconditionMarking);
    final AutomatonProxy result =
        mFactory.createAutomatonProxy(name, ComponentKind.SPEC,
                                      events, states, transitions);
    return result;
  }

  private AutomatonProxy createLocalSIC5Test(final EventProxy answer,
                                             final List<EventProxy> iface,
                                             final List<EventProxy> local,
                                             final String name)
  {
    final int numEvents = iface.size() + local.size() + 1;
    final List<EventProxy> events =
      new ArrayList<EventProxy>(numEvents);
    events.addAll(iface);
    events.addAll(local);

    // Create the 2 states ...
    final List<StateProxy> states = new ArrayList<StateProxy>(3);
    // Initial state ...
    final StateProxy initialState =
        mFactory.createStateProxy("T1", true, null);
    states.add(initialState);
    // Second state has the precondition marking ...
    final Collection<EventProxy> propositions =
      Collections.singletonList(mPreconditionMarking);
    final StateProxy alphaState =
        mFactory.createStateProxy("T2", false, propositions);
    states.add(alphaState);

    // Create the transitions ...
    final List<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(2 * numEvents);
    for (final EventProxy event : events) {
      // Selfloop on the initial state that includes entire event alphabet ...
      TransitionProxy trans =
        mFactory.createTransitionProxy(initialState, event, initialState);
      transitions.add(trans);
      final Map<String,String> attribs = event.getAttributes();
      final HISCAttributeFactory.EventType etype =
        HISCAttributeFactory.getEventType(attribs);
      switch (etype) {
      case REQUEST:
        // The transition which accepts any request event ...
        trans = mFactory.createTransitionProxy(initialState, event, alphaState);
        transitions.add(trans);
        break;
      case DEFAULT:
        // The transition which accepts any local event ...
        trans = mFactory.createTransitionProxy(alphaState, event, initialState);
        transitions.add(trans);
        break;
      default:
        break;
      }
    }
    // The answer transition ...
    final TransitionProxy trans =
      mFactory.createTransitionProxy(alphaState, answer, initialState);
    transitions.add(trans);
    events.add(mPreconditionMarking);
    final AutomatonProxy result =
        mFactory.createAutomatonProxy(name, ComponentKind.SPEC,
                                      events, states, transitions);
    return result;
  }

  private AutomatonProxy createSIC5InterfaceAutomaton
    (final AutomatonProxy aut, final EventProxy answer)
  {
    // removes all marking propositions from the event alphabet (all
    // states are required to be marked with the default marking proposition,
    // so by removing the marking all states are implicitly marked)
    final Collection<EventProxy> newEvents = removePropositions(aut.getEvents());
    newEvents.add(mPreconditionMarking);

    // collects all the transitions that use the specified answer event
    final List<TransitionProxy> answerTransitions =
        new ArrayList<TransitionProxy>();
    for (final TransitionProxy transition : aut.getTransitions()) {
      if (transition.getEvent().equals(answer)) {
        answerTransitions.add(transition);
      }
    }

    final List<StateProxy> newStates = new ArrayList<StateProxy>();
    for (final StateProxy state : aut.getStates()) {
      // removes all marking propositions from all states by creating an empty
      // list of propositions
      final List<EventProxy> newPropositions = new ArrayList<EventProxy>();

      // mark states that have the specified answer event enabled with the
      // precondition marking proposition :alpha
      for (final TransitionProxy transition : answerTransitions) {
        if (transition.getSource() == state) {
          if (!newPropositions.contains(mPreconditionMarking)) {
            newPropositions.add(mPreconditionMarking);
          }
          break;
        }
      }
      final StateProxy newState =
          mFactory.createStateProxy(state.getName(), state.isInitial(),
                                    newPropositions);
      newStates.add(newState);
      mStateMap.put(state, newState);
    }
    final Collection<TransitionProxy> newTransitions =
      replaceTransitionStates(aut);
    mStateMap.clear();
    return mFactory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                         newEvents, newStates, newTransitions,
                                         aut.getAttributes());
  }

  /**
   * Creates the test automaton added to the model to check SIC Property VI.
   */
  private AutomatonProxy createSIC6Test()
  {
    // Collect alphabet: Interface events plus tau, alpha, omega.
    int numInterfaceEvents = 0;
    final Collection<EventProxy> allEvents = mModel.getEvents();
    final List<EventProxy> events = new ArrayList<EventProxy>();
    for (final EventProxy event : allEvents) {
      final Map<String,String> attribs = event.getAttributes();
      final HISCAttributeFactory.EventType type =
        HISCAttributeFactory.getEventType(attribs);
      if (type != HISCAttributeFactory.EventType.DEFAULT) {
        events.add(event);
        numInterfaceEvents++;
      }
    }
    events.add(mTau);
    events.add(mInputMarking);
    events.add(mPreconditionMarking);

    // Create the 2 states ...
    final List<StateProxy> states = new ArrayList<StateProxy>(2);
    final StateProxy initialState =
      mFactory.createStateProxy("T1", true, null);
    states.add(initialState);
    final List<EventProxy> propositions = new ArrayList<EventProxy>(2);
    propositions.add(mInputMarking);
    propositions.add(mPreconditionMarking);
    final StateProxy alphaState =
      mFactory.createStateProxy("T2", false, propositions);
    states.add(alphaState);

    // Create the transitions ...
    final List<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(numInterfaceEvents + 1);
    // 1. Selfloops on initial state for all interface events.
    for (int e = 0; e < numInterfaceEvents; e++) {
      final EventProxy event = events.get(e);
      final TransitionProxy trans =
        mFactory.createTransitionProxy(initialState, event, initialState);
      transitions.add(trans);
    }
    // 2. tau-transition from initial state to alpha state.
    final TransitionProxy trans =
      mFactory.createTransitionProxy(initialState, mTau, alphaState);
    transitions.add(trans);

    return mFactory.createAutomatonProxy("Test:Aut", ComponentKind.SPEC,
                                         events, states, transitions);
  }

  private AutomatonProxy createSIC6InterfaceAutomaton(final AutomatonProxy aut)
  {
    final Collection<EventProxy> oldEvents = aut.getEvents();
    if (!oldEvents.contains(mInputMarking)) {
      return removePropositions(aut, null);
    }
    final Collection<EventProxy> newEvents = removePropositions(oldEvents);
    newEvents.add(mInputMarking);
    newEvents.add(mPreconditionMarking);
    final Collection<EventProxy> marked = new ArrayList<EventProxy>(2);
    marked.add(mInputMarking);
    marked.add(mPreconditionMarking);
    final Collection<StateProxy> oldStates = aut.getStates();
    final int numStates = oldStates.size();
    final Collection<StateProxy> newStates =
      new ArrayList<StateProxy>(numStates);
    for (final StateProxy oldState : oldStates) {
      final Collection<EventProxy> props = oldState.getPropositions();
      if (props.isEmpty()) {
        newStates.add(oldState);
        mStateMap.put(oldState, oldState);
      } else {
        final String name = oldState.getName();
        final boolean init = oldState.isInitial();
        final StateProxy newState;
        if (props.contains(mInputMarking)) {
          newState = mFactory.createStateProxy(name, init, marked);
        } else {
          newState = mFactory.createStateProxy(name, init, null);
        }
        newStates.add(newState);
        mStateMap.put(oldState, newState);
      }
    }
    final Collection<TransitionProxy> newTransitions =
      replaceTransitionStates(aut);
    mStateMap.clear();
    final String name = aut.getName();
    final ComponentKind kind = aut.getKind();
    return mFactory.createAutomatonProxy
      (name, kind, newEvents, newStates, newTransitions);
  }

  /**
   * Creates a new automaton by removing propositions from the given
   * automaton.
   * @param keep  A proposition to be kept. If <CODE>null</CODE>, all
   *              propositions will be removed.
   */
  private AutomatonProxy removePropositions(final AutomatonProxy aut,
                                            final EventProxy keep)
  {
    final Collection<EventProxy> oldEvents = aut.getEvents();
    final Collection<EventProxy> newEvents =
      removePropositions(oldEvents, keep);
    if (oldEvents.size() == newEvents.size()) {
      return aut;
    }
    final Collection<StateProxy> oldStates = aut.getStates();
    final int numStates = oldStates.size();
    final Collection<StateProxy> newStates =
      new ArrayList<StateProxy>(numStates);
    for (final StateProxy oldState : oldStates) {
      final Collection<EventProxy> props = oldState.getPropositions();
      if (props.isEmpty()) {
        newStates.add(oldState);
        mStateMap.put(oldState, oldState);
      } else {
        final String name = oldState.getName();
        final boolean init = oldState.isInitial();
        final StateProxy newState = mFactory.createStateProxy(name, init, null);
        newStates.add(newState);
        mStateMap.put(oldState, newState);
      }
    }
    final Collection<TransitionProxy> newTransitions =
      replaceTransitionStates(aut);
    mStateMap.clear();
    final String name = aut.getName();
    final ComponentKind kind = aut.getKind();
    return mFactory.createAutomatonProxy
      (name, kind, newEvents, newStates, newTransitions);
  }

  /**
   * Removes all propositions from a given event alphabet.
   */
  private Collection<EventProxy> removePropositions
    (final Collection<EventProxy> events)
  {
    return removePropositions(events, null);
  }

  /**
   * Removes propositions from a given event alphabet.
   * @param keep  A proposition to be kept. If <CODE>null</CODE>, all
   *              propositions will be removed.
   */
  private Collection<EventProxy> removePropositions
    (final Collection<EventProxy> events, final EventProxy keep)
  {
    final int numevents = events.size();
    final List<EventProxy> newEvents = new ArrayList<EventProxy>(numevents);
    for (final EventProxy event : events) {
      switch (event.getKind()) {
      case PROPOSITION:
        if (event == keep) {
          newEvents.add(event);
        }
        break;
      default:
        newEvents.add(event);
        break;
      }
    }
    return newEvents;
  }

  /**
   * Replaces the source and target states of a transition with the new version
   * of the states stored in the map {@link #mStateMap}.
   */
  private Collection<TransitionProxy> replaceTransitionStates
    (final AutomatonProxy aut)
  {
    final Collection<TransitionProxy> oldTransitions = aut.getTransitions();
    final int numTransitions = oldTransitions.size();
    final List<TransitionProxy> newTransitions =
        new ArrayList<TransitionProxy>(numTransitions);
    for (final TransitionProxy transition : oldTransitions) {
      final StateProxy source = mStateMap.get(transition.getSource());
      final StateProxy target = mStateMap.get(transition.getTarget());
      final TransitionProxy newTransition =
          mFactory.createTransitionProxy(source, transition.getEvent(), target);
      newTransitions.add(newTransition);
    }
    return newTransitions;
  }


  //#########################################################################
  //# Data Members
  /**
   * The model which is being changed.
   */
  private ProductDESProxy mModel;

  private final ProductDESProxyFactory mFactory;

  /**
   * A map of the original states to the new version of the state (which is
   * either a copy or has a proposition added/removed).
   */
  private final Map<StateProxy,StateProxy> mStateMap =
      new HashMap<StateProxy,StateProxy>();

  /**
   * The default marking proposition used by the input model.
   */
  private EventProxy mInputMarking;
  /**
   * The default marking proposition used by the output model.
   */
  private EventProxy mOutputMarking;
  /**
   * The precondition marking.
   */
  private EventProxy mPreconditionMarking;
  /**
   * The tau event used for the test automaton for SIC property VI.
   */
  private EventProxy mTau;

}
