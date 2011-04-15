//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   SICPropertyBuilder
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.hisc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;
import net.sourceforge.waters.analysis.hisc.HISCAttributes;


/**
 * A converter to translate models representing HISC low level modules
 * into multi-coloured models for checking SIC or LDIC properties V and VI.
 *
 * @author Rachel Francis, Robi Malik
 */

public class SICPropertyBuilder
{

  //#########################################################################
  //# Constructors
  public SICPropertyBuilder(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
    mModel = null;
  }

  public SICPropertyBuilder(final ProductDESProxy model,
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
    mLowLevelAutomata = null;
  }

  public ProductDESProxy getUnchangedModel()
  {
    return mModel;
  }

  public void setMarkingProposition(final EventProxy marking)
  {
    mMarking = marking;
  }

  public EventProxy getMarkingProposition()
  {
    return mMarking;
  }

  public void setGeneralisedPrecondition(final EventProxy marking)
  {
    mPreconditionMarking = marking;
  }

  public EventProxy getGeneralisedPrecondition()
  {
    return mPreconditionMarking;
  }

  public void setSplitting(final boolean split)
  {
    mSplitting = split;
  }

  public boolean getSplitting(){
    return mSplitting;
  }
  public void setDefaultMarkings()
  {
    mMarking =
      mFactory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                                EventKind.PROPOSITION, true);
    final String alphaNm = ":alpha";
    mPreconditionMarking =
      mFactory.createEventProxy(alphaNm, EventKind.PROPOSITION, true);
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
      if (HISCAttributes.getEventType(event.getAttributes()) == HISCAttributes.EventType.ANSWER) {
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
  {
    if (mMarking == null) {
      mMarking =
          mFactory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                                    EventKind.PROPOSITION, true);
    }
    if (mPreconditionMarking == null) {
      final String alphaNm = ":alpha";
      mPreconditionMarking =
          mFactory.createEventProxy(alphaNm, EventKind.PROPOSITION, true);
    }
    final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata();
    final int numaut = oldAutomata.size();
    final List<AutomatonProxy> newAutomata =
      new ArrayList<AutomatonProxy>(numaut);

    // The low level automaton of a model only need modifying once because they
    // don't depend on the answer event.
    if (mLowLevelAutomata == null) {
      mLowLevelAutomata = new ArrayList<AutomatonProxy>(numaut);
      for (final AutomatonProxy aut : oldAutomata) {
        if (!HISCAttributes.isInterface(aut.getAttributes())) {
          newAutomata.add(createSIC5LowLevelAutomaton(aut));
        }
      }
    } else {
      newAutomata.addAll(mLowLevelAutomata);
    }
    // Modifies the model's interfaces dependent on the answer event specified
    for (final AutomatonProxy aut : mModel.getAutomata()) {
      if (HISCAttributes.isInterface(aut.getAttributes())) {
        newAutomata.add(createSIC5InterfaceAutomaton(aut, answer));
      }
    }
    final Collection<EventProxy> events= mModel.getEvents();
    final int numEvents = events.size();
    final List<EventProxy> iface= new ArrayList<EventProxy>(numEvents);
    final List<EventProxy> local= new ArrayList<EventProxy>(numEvents);

    for (final EventProxy event : events) {
      if (event.getKind() == EventKind.PROPOSITION) {
        // skip
      } else if (HISCAttributes.getEventType(event.getAttributes()) == HISCAttributes.EventType.DEFAULT) {
        local.add(event);
      } else {
        iface.add(event);
      }

    }

    if(mSplitting && !local.isEmpty()){
        for(final EventProxy event : local){
          final List<EventProxy> local1= Collections.singletonList(event);
          final String aut_name = "Test:" + event.getName();
          newAutomata.add(createSIC5Test(answer,iface,local1,aut_name ));
         // MarshallingTools.saveModule(newAutomata, "/research/vaibhav/sic5_changes_automaton/1.wmod");
        }
    }
    else{
      newAutomata.add(createSIC5Test(answer,iface,local,"Test:Aut"));
   }


    // removes markings from automaton event alphabet
    final Collection<EventProxy> newEvents =
        removePropositions(mModel.getEvents());
    newEvents.add(mMarking);
    newEvents.add(mPreconditionMarking);
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
    // Test Automaton in case of splitting = false;
    // MarshallingTools.saveProductDES(newModel, "/research/vaibhav/sic5_changes_automaton/"+name+".wmod");
    return newModel;
  }

  /**
   * Builds a model for checking SIC Property VI.
   */
  public ProductDESProxy createSIC6Model()
  {
    assert mMarking != null :
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
      removePropositions(oldEvents, mMarking);
    newEvents.add(mTau);

    final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata();
    final int numaut = oldAutomata.size();
    final List<AutomatonProxy> newAutomata =
      new ArrayList<AutomatonProxy>(numaut);
    for (final AutomatonProxy oldAut : oldAutomata) {
      final AutomatonProxy newAut;
      final Map<String,String> attribs = oldAut.getAttributes();
      if (HISCAttributes.isInterface(attribs)) {
        newAut = createSIC6InterfaceAutomaton(oldAut);
      } else {
        newAut = removePropositions(oldAut, mMarking);
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
  // new sic5 test creator
  private AutomatonProxy createSIC5Test(final EventProxy answer,
                                        final List<EventProxy> iface,
                                        final List<EventProxy> local,
                                        final String name)
  {
    final int num_iface = iface.size();
    final int num_local = local.size();
    final List<EventProxy>newEvents= new ArrayList<EventProxy>(num_iface+num_local);
    newEvents.addAll(iface);
    newEvents.addAll(local);

    // creates the 3 states needed (the 3rd is optional, not needed if only
    // request and answer events exist)
    final List<StateProxy> states = new ArrayList<StateProxy>(2);
    List<EventProxy> propositions = new ArrayList<EventProxy>(1);
    // initial state has the default marking proposition
    propositions.add(mMarking);
    final StateProxy initialState =
        mFactory.createStateProxy("T1", true, propositions);
    states.add(initialState);
    // next state has the precondition marking
    propositions = new ArrayList<EventProxy>(1);
    propositions.add(mPreconditionMarking);
    final StateProxy alphaState =
        mFactory.createStateProxy("T2", false, propositions);
    states.add(alphaState);
    StateProxy t3State = null;

     // creates the transitions needed
    final List<TransitionProxy> transitions = new ArrayList<TransitionProxy>();
    for (final EventProxy event : newEvents) {
      // self loop on the initial state that includes entire event alphabet
      final TransitionProxy transition =
          mFactory.createTransitionProxy(initialState, event, initialState);
      transitions.add(transition);

      // the transition which accepts any request event
      if (event.getAttributes().equals(HISCAttributes.ATTRIBUTES_REQUEST)) {
        final TransitionProxy requestTransition =
            mFactory.createTransitionProxy(initialState, event, alphaState);
        transitions.add(requestTransition);
      }

      // the transitions which accepts any local event (i.e. non request, non
      // answer events)
      else if (!event.getAttributes().equals(HISCAttributes.ATTRIBUTES_ANSWER)
          && !event.getAttributes().equals(HISCAttributes.ATTRIBUTES_REQUEST)) {
        if (states.size() < 3) {
          // third state has no propositions
          propositions = null;
          t3State = mFactory.createStateProxy("T3", false, null);
          states.add(t3State);
        }
        final TransitionProxy localTransition =
            mFactory.createTransitionProxy(alphaState, event, t3State);
        transitions.add(localTransition);
        final TransitionProxy localSelfLoop =
            mFactory.createTransitionProxy(t3State, event, t3State);
        transitions.add(localSelfLoop);
      }

    }

    // creates the two answer transitions
    final TransitionProxy immediateAnswer =
        mFactory.createTransitionProxy(alphaState, answer, initialState);
    transitions.add(immediateAnswer);
    if (t3State != null) {
      final TransitionProxy finallyAnswer =
          mFactory.createTransitionProxy(t3State, answer, initialState);
      transitions.add(finallyAnswer);
    }


    // adds the two marking propositions to the automaton alphabet
    newEvents.add(mMarking);
    newEvents.add(mPreconditionMarking);

    final AutomatonProxy newTestAut =
        mFactory.createAutomatonProxy(name, ComponentKind.SPEC,
                                      newEvents, states, transitions);
    // for mSplitting = false
    //MarshallingTools.saveModule(newTestAut, "/research/vaibhav/sic5_changes_automaton/maip3_syn/6(mustl,mins)/splitting_false/"+name+".wmod");
    //for mSplitting = true
    //MarshallingTools.saveModule(newTestAut, "/research/vaibhav/sic5_changes_automaton/maip3_syn/6(mustl,mins)/splitting_true/"+name+".wmod");
    return newTestAut;

  }


  //# Auxiliary Methods
  /*
   * Creates the test automaton added to the model to check SIC Property V
   * with respect to the given answer event.
  private AutomatonProxy createSIC5Test(final EventProxy answer)
  {
    // removes markings from automaton event alphabet
    final Collection<EventProxy> newEvents =
        removePropositions(mModel.getEvents());


    // creates the 3 states needed (the 3rd is optional, not needed if only
    // request and answer events exist)
    final List<StateProxy> states = new ArrayList<StateProxy>(2);
    List<EventProxy> propositions = new ArrayList<EventProxy>(1);
    // initial state has the default marking proposition
    propositions.add(mMarking);
    final StateProxy initialState =
        mFactory.createStateProxy("T1", true, propositions);
    states.add(initialState);
    // next state has the precondition marking
    propositions = new ArrayList<EventProxy>(1);
    propositions.add(mPreconditionMarking);
    final StateProxy alphaState =
        mFactory.createStateProxy("T2", false, propositions);
    states.add(alphaState);
    StateProxy t3State = null;

    // creates the transitions needed
    final List<TransitionProxy> transitions = new ArrayList<TransitionProxy>();
    for (final EventProxy event : newEvents) {
      // self loop on the initial state that includes entire event alphabet
      final TransitionProxy transition =
          mFactory.createTransitionProxy(initialState, event, initialState);
      transitions.add(transition);

      // the transition which accepts any request event
      if (event.getAttributes().equals(HISCAttributes.ATTRIBUTES_REQUEST)) {
        final TransitionProxy requestTransition =
            mFactory.createTransitionProxy(initialState, event, alphaState);
        transitions.add(requestTransition);
      }

      // the transitions which accepts any local event (i.e. non request, non
      // answer events)
      else if (!event.getAttributes().equals(HISCAttributes.ATTRIBUTES_ANSWER)
          && !event.getAttributes().equals(HISCAttributes.ATTRIBUTES_REQUEST)) {
        if (states.size() < 3) {
          // third state has no propositions
          propositions = null;
          t3State = mFactory.createStateProxy("T3", false, null);
          states.add(t3State);
        }
        final TransitionProxy localTransition =
            mFactory.createTransitionProxy(alphaState, event, t3State);
        transitions.add(localTransition);
        final TransitionProxy localSelfLoop =
            mFactory.createTransitionProxy(t3State, event, t3State);
        transitions.add(localSelfLoop);
      }

    }
    // creates the two answer transitions
    final TransitionProxy immediateAnswer =
        mFactory.createTransitionProxy(alphaState, answer, initialState);
    transitions.add(immediateAnswer);
    if (t3State != null) {
      final TransitionProxy finallyAnswer =
          mFactory.createTransitionProxy(t3State, answer, initialState);
      transitions.add(finallyAnswer);
    }

    // adds the two marking propositions to the automaton alphabet
    newEvents.add(mMarking);
    newEvents.add(mPreconditionMarking);

    final AutomatonProxy newTestAut =
        mFactory.createAutomatonProxy("Test:Aut", ComponentKind.SPEC,
                                      newEvents, states, transitions);


     // final AutomatonProxy newTestAuto =
      mFactory.createAutomatonProxy("Test:Event", ComponentKind.SPEC,
                                    newEvents, states, transitions);
   // String name = "Test:" + getEventName();

    return newTestAut;
  }
   */


  // private String getEventName()
  //{
    // TODO Auto-generated method stub
    //return null;
  //}

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

  private AutomatonProxy createSIC5LowLevelAutomaton
    (final AutomatonProxy aut)
  {
    // removes markings from automaton event alphabet
    final Collection<EventProxy> newEvents = removePropositions(aut.getEvents());
    // removes markings from the states
    final List<StateProxy> newStates = new ArrayList<StateProxy>();
    for (final StateProxy state : aut.getStates()) {
      final List<EventProxy> propositions = new ArrayList<EventProxy>();
      final StateProxy newState =
          mFactory.createStateProxy(state.getName(), state.isInitial(),
                                    propositions);
      newStates.add(newState);
      mStateMap.put(state, newState);
    }
    final Collection<TransitionProxy> newTransitions =
      replaceTransitionStates(aut);
    final AutomatonProxy modifiedLowLevelAutomaton =
        mFactory.createAutomatonProxy(aut.getName(), aut.getKind(), newEvents,
                                      newStates, newTransitions, aut
                                          .getAttributes());
    mLowLevelAutomata.add(modifiedLowLevelAutomaton);
    mStateMap.clear();
    return modifiedLowLevelAutomaton;
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
      final HISCAttributes.EventType type =
        HISCAttributes.getEventType(attribs);
      if (type != HISCAttributes.EventType.DEFAULT) {
        events.add(event);
        numInterfaceEvents++;
      }
    }
    events.add(mTau);
    events.add(mMarking);
    events.add(mPreconditionMarking);

    // Create the 2 states ...
    final List<StateProxy> states = new ArrayList<StateProxy>(2);
    final StateProxy initialState =
      mFactory.createStateProxy("T1", true, null);
    states.add(initialState);
    final List<EventProxy> propositions = new ArrayList<EventProxy>(2);
    propositions.add(mMarking);
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
    if (!oldEvents.contains(mMarking)) {
      return removePropositions(aut, null);
    }
    final Collection<EventProxy> newEvents = removePropositions(oldEvents);
    newEvents.add(mMarking);
    newEvents.add(mPreconditionMarking);
    final Collection<EventProxy> marked = new ArrayList<EventProxy>(2);
    marked.add(mMarking);
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
        if (props.contains(mMarking)) {
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
   * A list of the low level automaton that are created with the new marking
   * rules.
   */
  private List<AutomatonProxy> mLowLevelAutomata;

  /**
   * A map of the original states to the new version of the state (which is
   * either a copy or has a proposition added/removed).
   */
  private final Map<StateProxy,StateProxy> mStateMap =
      new HashMap<StateProxy,StateProxy>();

  /**
   * The default marking proposition
   */
  private EventProxy mMarking;
  /**
   * The precondition marking.
   */
  private EventProxy mPreconditionMarking;
  /**
   * The tau event used for the test automaton for SIC property VI.
   */
  private boolean mSplitting = false;

  private EventProxy mTau;

}
