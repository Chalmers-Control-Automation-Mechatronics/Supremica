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

public class ConvertModel {

  //#########################################################################
  //# Constructors
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
    Set<AutomatonProxy>  newAutomata = new HashSet<AutomatonProxy>(mModel.getAutomata());
    Set<EventProxy>      newEvents   = new HashSet<EventProxy>(mModel.getEvents());    
    
    for (AutomatonProxy automaton : mModel.getAutomata()) { 
      switch (mTranslator.getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);
                      break;
        case SPEC  :  specs.add(automaton);                                            
                      break;
        default : break;
      }
    } 
    
    newAutomata.removeAll(plants);
    newAutomata.removeAll(specs);
    
    for (AutomatonProxy spec : specs) {      
      AutomatonProxy newSpec = convertSpec(spec);
      newAutomata.add(newSpec);      
    }
    for (AutomatonProxy plant : plants) {      
      AutomatonProxy newPlant = convertPlant(plant,madeEvents);
      newAutomata.add(newPlant);      
    }
    StateProxy onestate =
      mFactory.createStateProxy("one", true, new HashSet<EventProxy>());
    Set<EventProxy> oneSpecEvents = new HashSet<EventProxy>();
    for(EventProxy e : madeEvents.keySet()) {
      oneSpecEvents.addAll(madeEvents.get(e));
    }
    AutomatonProxy oneSpec =
      mFactory.createAutomatonProxy("oneSpec" + ":spec",
                                    ComponentKind.SPEC,
                                    oneSpecEvents,
                                    Collections.singleton(onestate),
                                    new HashSet<TransitionProxy>());
    newAutomata.add(oneSpec);
    newEvents.addAll(oneSpec.getEvents());
        
    //System.out.println(mModel.getAutomata().size()+" "+newAutomata.size());

    ProductDESProxy newModel = mFactory.createProductDESProxy("convertedModel", newEvents, newAutomata);
    return newModel;
  }

  private AutomatonProxy convertSpec(AutomatonProxy a) {    
    Map<EventProxy, EventProxy> uncont = new HashMap<EventProxy, EventProxy>();
    for (EventProxy e : a.getEvents()) {
      if (mTranslator.getEventKind(e) == EventKind.UNCONTROLLABLE) {      
        EventProxy newEvent = mFactory.createEventProxy(e.getName() + ":" + a.getName(),
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
    Map<StateProxy, Set<EventProxy>> stateevents =
      new HashMap<StateProxy, Set<EventProxy>>();
    for (StateProxy s : a.getStates()) {
      stateevents.put(s, new HashSet<EventProxy>());
    }
    for (TransitionProxy t : a.getTransitions()) {
      if (mTranslator.getEventKind(t.getEvent()) ==
          EventKind.UNCONTROLLABLE) {
        stateevents.get(t.getSource()).add(t.getEvent());
      }
    }
    Set<StateProxy> states = new HashSet<StateProxy>(a.getStates());
    Set<TransitionProxy> transitions =
      new HashSet<TransitionProxy>(a.getTransitions());
    Set<EventProxy> events = new HashSet<EventProxy>(a.getEvents());
    events.addAll(uncont.values());
    for (StateProxy s : states) {
      for (EventProxy e: uncont.keySet()) {
        if (!stateevents.get(s).contains(e)) {
          transitions.add
            (mFactory.createTransitionProxy(s, uncont.get(e), s));
        }
      }
    }
    AutomatonProxy plant =
      mFactory.createAutomatonProxy(a.getName() + ":plant",
                                    ComponentKind.PLANT,
                                    events, states, transitions);
    
    return plant;
  }

  private AutomatonProxy convertPlant(AutomatonProxy a,
                                      Map<EventProxy, Set<EventProxy>> uncont) {
    Set<EventProxy> same = new HashSet<EventProxy>(a.getEvents());
    same.retainAll(uncont.keySet());
    if (same.isEmpty()) {
      return a;
    }
    Collection<TransitionProxy> trans =
      new ArrayList<TransitionProxy>(a.getTransitions());
    Collection<EventProxy> events = new ArrayList<EventProxy>(a.getEvents());
    for(EventProxy e : same) {
      events.addAll(uncont.get(e));
    }
    for (TransitionProxy t : a.getTransitions()) {
      if (uncont.containsKey(t.getEvent())) {
        for (EventProxy e : uncont.get(t.getEvent())) {
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

  
  private ProductDESProxy             mModel;
  private ProductDESProxyFactory      mFactory;
  private KindTranslator              mTranslator;
  private Map<EventProxy, Set<EventProxy>> madeEvents;         
}
