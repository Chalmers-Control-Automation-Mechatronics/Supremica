package net.sourceforge.waters.analysis.composing;

import java.util.ArrayList;
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


public class ConvertModel {

  //#########################################################################
  //# Constructors
  public ConvertModel(){
  }

  public ConvertModel(final ProductDESProxy        model,
                      final KindTranslator         translator,
                      final ProductDESProxyFactory factory) {
    mModel = model;
    mFactory = factory;
    mTranslator = translator;
    madeEvents = new HashMap<EventProxy, Set<EventProxy>>();
  }

  public ProductDESProxy run() {
    final Set<AutomatonProxy>  plants      = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy>  specs       = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy>  newAutomata = new HashSet<AutomatonProxy>();
    final Set<EventProxy>      newEvents   = new HashSet<EventProxy>(mModel.getEvents());

    for (final AutomatonProxy automaton : mModel.getAutomata()) {
      switch (mTranslator.getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);
                      break;
        case SPEC  :  specs.add(automaton);
                      break;
        default : break;
      }
    }

    for (final AutomatonProxy spec : specs) {
      final AutomatonProxy newSpec = convertSpec(spec);
      newAutomata.add(newSpec);
    }
    for (final AutomatonProxy plant : plants) {
      final AutomatonProxy newPlant = convertPlant(plant,madeEvents);
      newAutomata.add(newPlant);
    }
    final StateProxy onestate =
      mFactory.createStateProxy("one", true, new HashSet<EventProxy>());
    final Set<EventProxy> oneSpecEvents = new HashSet<EventProxy>();
    for(final EventProxy e : madeEvents.keySet()) {
      oneSpecEvents.addAll(madeEvents.get(e));
    }
    System.out.println("gama Evetns: "+oneSpecEvents.size());
    final AutomatonProxy oneSpec =
      mFactory.createAutomatonProxy("oneSpec" + ":spec",
                                    ComponentKind.SPEC,
                                    oneSpecEvents,
                                    Collections.singleton(onestate),
                                    null,
                                    null);
    newAutomata.add(oneSpec);
    newEvents.addAll(oneSpec.getEvents());

    //System.out.println(mModel.getAutomata().size()+" "+newAutomata.size());

    final ProductDESProxy newModel = mFactory.createProductDESProxy("convertedModel", newEvents, newAutomata);
    return newModel;
  }

  private AutomatonProxy convertSpec(final AutomatonProxy a) {
    final Map<EventProxy, EventProxy> uncont = new HashMap<EventProxy, EventProxy>();
    for (final EventProxy e : a.getEvents()) {
      if (mTranslator.getEventKind(e) == EventKind.UNCONTROLLABLE) {
        final EventProxy newEvent = mFactory.createEventProxy(e.getName() + ":" + a.getName(),
                                                        EventKind.UNCONTROLLABLE);
        Set<EventProxy> temp = new HashSet<EventProxy>();
        if (madeEvents.containsKey(e)) {
          temp = madeEvents.get(e);
          temp.add(newEvent);
          madeEvents.put(e,temp);
        } else {
            temp.add(newEvent);
            madeEvents.put(e,temp);
        }
        uncont.put(e, newEvent);
      }
    }
    final Map<StateProxy, Set<EventProxy>> stateevents =
      new HashMap<StateProxy, Set<EventProxy>>();
    for (final StateProxy s : a.getStates()) {
      stateevents.put(s, new HashSet<EventProxy>());
    }
    for (final TransitionProxy t : a.getTransitions()) {
      if (mTranslator.getEventKind(t.getEvent()) ==
          EventKind.UNCONTROLLABLE) {
        stateevents.get(t.getSource()).add(t.getEvent());
      }
    }
    final Set<StateProxy> states = new HashSet<StateProxy>(a.getStates());
    final Set<TransitionProxy> transitions =
      new HashSet<TransitionProxy>(a.getTransitions());
    final Set<EventProxy> events = new HashSet<EventProxy>(a.getEvents());
    events.addAll(uncont.values());
    for (final StateProxy s : states) {
      for (final EventProxy e: uncont.keySet()) {
        if (!stateevents.get(s).contains(e)) {
          transitions.add
            (mFactory.createTransitionProxy(s, uncont.get(e), s));
        }
      }
    }
    final AutomatonProxy plant =
      mFactory.createAutomatonProxy(a.getName() + ":plant",
                                    ComponentKind.PLANT,
                                    events, states, transitions, null);

    return plant;
  }

  private AutomatonProxy convertPlant(final AutomatonProxy a,
                                      final Map<EventProxy, Set<EventProxy>> uncont) {
    final Set<EventProxy> same = new HashSet<EventProxy>(a.getEvents());
    same.retainAll(uncont.keySet());
    if (same.isEmpty()) {
      return a;
    }
    final Collection<TransitionProxy> trans =
      new ArrayList<TransitionProxy>(a.getTransitions());
    final Collection<EventProxy> events = new ArrayList<EventProxy>(a.getEvents());
    for(final EventProxy e : same) {
      events.addAll(uncont.get(e));
    }
    for (final TransitionProxy t : a.getTransitions()) {
      if (uncont.containsKey(t.getEvent())) {
        for (final EventProxy e : uncont.get(t.getEvent())) {
          trans.add(mFactory.createTransitionProxy(t.getSource(),
                                                   e,
                                                   t.getSource()));
        }
      }
    }
    return mFactory.createAutomatonProxy
      (a.getName() + ":plant", ComponentKind.PLANT,
       events, a.getStates(), trans, null);
  }

  public EventProxy getOriginalEvent(final EventProxy newevent) {
    for (final EventProxy e : madeEvents.keySet()) {
      if (madeEvents.get(e).contains(newevent)) return e;
    }
    return newevent;
  }


  private ProductDESProxy             mModel;
  private ProductDESProxyFactory      mFactory;
  private KindTranslator              mTranslator;
  private Map<EventProxy, Set<EventProxy>> madeEvents;
}
