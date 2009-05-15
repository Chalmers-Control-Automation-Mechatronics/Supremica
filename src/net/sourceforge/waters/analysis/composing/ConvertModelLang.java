//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.composing
//# CLASS:   ConvertModelLang
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.composing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

public class ConvertModelLang {

  //#########################################################################
  //# Constructors
  public ConvertModelLang(){
  }
  
  public ConvertModelLang(final ProductDESProxy        model,
                          final KindTranslator         translator,
                          final ProductDESProxyFactory factory) {
    mModel = model;
    mFactory = factory;
    mTranslator = translator;
    mMadeEvents = new HashMap<EventProxy, Set<EventProxy>>();
  }


  //#########################################################################
  //# Invocation
  public ProductDESProxy run()
  {
    final int numaut = mModel.getAutomata().size();
    final int numevents = mModel.getEvents().size();
    final Collection<AutomatonProxy> newAutomata =
      new ArrayList<AutomatonProxy>(numaut + 1);
    final Set<EventProxy> newEvents = new HashSet<EventProxy>(numevents);
    mMadeEvents.clear();

    for (final AutomatonProxy automaton : mModel.getAutomata()) {
      if (mTranslator.getComponentKind(automaton) == ComponentKind.SPEC) {
        final AutomatonProxy spec = convertSpec(automaton);
        if (spec == automaton) {
          return mModel;
        }
        newAutomata.add(spec);
        newEvents.addAll(automaton.getEvents());
      }
    }
    for (final AutomatonProxy automaton : mModel.getAutomata()) {
      if (mTranslator.getComponentKind(automaton) == ComponentKind.PLANT) {
        final AutomatonProxy plant = convertPlant(automaton);
        newAutomata.add(plant);
        newEvents.addAll(automaton.getEvents());
      }
    }
    final StateProxy onestate =
      mFactory.createStateProxy("s0", true, new HashSet<EventProxy>());
    Set<EventProxy> onePropertyEvents = new HashSet<EventProxy>();
    for (EventProxy e : mMadeEvents.keySet()) {
      onePropertyEvents.addAll(mMadeEvents.get(e));
    }
    AutomatonProxy oneProperty =
      mFactory.createAutomatonProxy(":never",
                                    ComponentKind.PROPERTY,
                                    onePropertyEvents,
                                    Collections.singleton(onestate),
                                    new HashSet<TransitionProxy>());
    newAutomata.add(oneProperty);
    newEvents.addAll(oneProperty.getEvents());
   
    ProductDESProxy newModel = mFactory.createProductDESProxy("convertedModel", newEvents, newAutomata);
    return newModel;
  }

  private AutomatonProxy convertSpec(AutomatonProxy a) {    
    Map<EventProxy, EventProxy> uncont = new HashMap<EventProxy, EventProxy>();
    Map<EventProxy,Set<StateProxy>> enabledStates = new HashMap<EventProxy,Set<StateProxy>>(); 
    Set<EventProxy> enabledEvents = new HashSet<EventProxy>();
    Set<EventProxy> notEnabledEvents = new HashSet<EventProxy>(a.getEvents());  
    
    for (TransitionProxy trans : a.getTransitions()) {
      EventProxy e = trans.getEvent();
      if (enabledStates.get(e)!=null) {
        enabledStates.get(e).add(trans.getSource());
      } else {
          Set<StateProxy> temp = new HashSet<StateProxy>();
          temp.add(trans.getSource());
          enabledStates.put(e,temp);
        }
    }
    for (EventProxy e : enabledStates.keySet()) {
      if (enabledStates.get(e).size()==a.getStates().size()) {
        enabledEvents.add(e);
      } 
    }
    System.out.println(enabledEvents.size()+" enabled events!!!");
    if (enabledEvents.isEmpty()) return a;
    notEnabledEvents.removeAll(enabledEvents);
    Set<EventProxy> pe = new HashSet<EventProxy>();
    for (EventProxy e : notEnabledEvents) { 
      if (e.getKind()==EventKind.PROPOSITION) { 
        pe.add(e);
        continue;
      }         
      EventProxy newEvent = mFactory.createEventProxy(e.getName() + ":" + a.getName(),
                                                      EventKind.UNCONTROLLABLE);
      Set<EventProxy> temp = new HashSet<EventProxy>();
      if (mMadeEvents.containsKey(e)) {          
        temp = mMadeEvents.get(e);
        temp.add(newEvent);
        mMadeEvents.put(e,temp);
      } else {                    
          temp.add(newEvent);
          mMadeEvents.put(e,temp);  
      }  
      uncont.put(e, newEvent); 
    }
    notEnabledEvents.removeAll(pe);
    System.out.println(notEnabledEvents.size()+" not enabled events!!!");
    Set<StateProxy> states = new HashSet<StateProxy>(a.getStates());
    Set<TransitionProxy> transitions =
      new HashSet<TransitionProxy>(a.getTransitions());
    Set<EventProxy> events = new HashSet<EventProxy>(a.getEvents());
    events.addAll(uncont.values());
    //Add selfloops when event is not enabled    
    for (EventProxy e : notEnabledEvents) {
      Set<StateProxy> notEnabledStates = new HashSet<StateProxy>(states);
      if (enabledStates.containsKey(e)) {
        notEnabledStates.removeAll(enabledStates.get(e));
      }
	    for (StateProxy s : notEnabledStates) {
	      transitions.add
	            (mFactory.createTransitionProxy(s, uncont.get(e), s));
	    }
    }
    AutomatonProxy plant =
      mFactory.createAutomatonProxy(a.getName() + ":plant",
                                    ComponentKind.PLANT,
                                    events, states, transitions);
    
    return plant;
  }

  private AutomatonProxy convertPlant(AutomatonProxy a)
  {
    Set<EventProxy> same = new HashSet<EventProxy>(a.getEvents());
    same.retainAll(mMadeEvents.keySet());
    if (same.isEmpty()) {
      return a;
    }
    Collection<TransitionProxy> trans =
      new ArrayList<TransitionProxy>(a.getTransitions());
    Collection<EventProxy> events = new ArrayList<EventProxy>(a.getEvents());
    for(EventProxy e : same) {
      events.addAll(mMadeEvents.get(e));
    }
    for (TransitionProxy t : a.getTransitions()) {
      if (mMadeEvents.containsKey(t.getEvent())) {
        for (EventProxy e : mMadeEvents.get(t.getEvent())) {
          trans.add(mFactory.createTransitionProxy(t.getSource(),
                                                   e,
                                                   t.getSource()));
        }
      }
    }
    return mFactory.createAutomatonProxy
      (a.getName() + ":plant", ComponentKind.PLANT,
       events, a.getStates(), trans);
  }
  
  public EventProxy getOriginalEvent(EventProxy newevent) {
    for (EventProxy e : mMadeEvents.keySet()) {
      if (mMadeEvents.get(e).contains(newevent)) return e;
    }
    return newevent;
  }

  
  private ProductDESProxy             mModel;
  private ProductDESProxyFactory      mFactory;
  private KindTranslator              mTranslator;
  private Map<EventProxy, Set<EventProxy>> mMadeEvents;         
}
