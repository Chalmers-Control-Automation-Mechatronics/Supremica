package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class SchemaBuilder
{
  private SchemaBuilder()
  {
  }

  /**
   * Build a schema for a Waters ProductDESProxy.
   */
  public static ProductDESSchema build(final ProductDESProxy des)
  {
    //From the set of events, build an array of EventProxy
    //instances, which gives them a consistent (arbitrary) ordering.
    //This is then used to build a map from EventProxy instance to
    //array indexes, which are used in the rest of the schema when
    //referring to events. An array of EventSchema objects is created
    //from this too.
    Set<EventProxy> model_event_set = des.getEvents();
    EventProxy[] events = new EventProxy[model_event_set.size()];
    int i = 0;
    for (EventProxy e : model_event_set)
      {
	events[i] = e;
	i++;
      }

    Map<EventProxy,Integer> emap = build_eventmap(events);
    EventSchema[] des_events = build_event_schemata(events);
    AutomatonSchema[] des_automata = build_automata(des.getAutomata(), emap);

    return new ProductDESSchema(des.getName(), des_automata, des_events);
  }

  private static AutomatonSchema[] build_automata(Set<AutomatonProxy> automata,
						  Map<EventProxy,Integer> emap)
  {
    AutomatonSchema[] schemata = new AutomatonSchema[automata.size()];
    int i = 0;
    for (AutomatonProxy automaton : automata)
      {
	schemata[i] = build_automaton(automaton, emap);
	  i++;
      }

    return schemata;
  }


  private static AutomatonSchema build_automaton(final AutomatonProxy aut, 
						 Map<EventProxy,Integer> emap)
  { 
    String name = aut.getName();
    int[] events = map_events(aut.getEvents(), emap);
    int kind = component_kind_convert(aut.getKind());

    //Like with events in the DES, build an array of states in the
    //model which will be used to give a definite ordering. Build a
    //map which will be used to look up states when building
    //transitions.
    Set<StateProxy> au_state_set = aut.getStates();
    StateProxy[] au_states = new StateProxy[au_state_set.size()];
    int i = 0;
    for (StateProxy s : au_state_set) {
      au_states[i] = s;
      i++;
    }
    Map<StateProxy,Integer> smap = build_statemap(au_states);
    StateSchema[] states = build_states(au_states, emap);
    TransitionSchema[] transitions = build_transitions(aut.getTransitions(), 
						       smap, emap);

    return new AutomatonSchema(name, events, states, kind, transitions);
  }

  private static TransitionSchema[] build_transitions
    (Collection<TransitionProxy> ts,
     Map<StateProxy,Integer> smap,
     Map<EventProxy,Integer> emap)
  {
    TransitionSchema[] transitions = new TransitionSchema[ts.size()];
    int i = 0;
    for (TransitionProxy t : ts)
      {
	transitions[i] = build_transition(t, smap, emap);
      }

    return transitions;
  }

  private static TransitionSchema build_transition(TransitionProxy t,
						   Map<StateProxy,Integer> smap,
						   Map<EventProxy,Integer> emap)
  {
    int source = smap.get(t.getSource());
    int target = smap.get(t.getTarget());
    int event = emap.get(t.getEvent());

    return new TransitionSchema(source, target, event);
  }

  private static Map<StateProxy,Integer> build_statemap(StateProxy[] states)
  {
    Map<StateProxy,Integer> smap = new HashMap<StateProxy,Integer>();
    for (int i = 0; i < states.length; i++)
      {
	smap.put(states[i], i);
      }

    return smap;
  }

  private static StateSchema[] build_states(StateProxy[] states,
					    Map<EventProxy,Integer> emap)
  {
    StateSchema[] ss = new StateSchema[states.length];

    for (int i = 0; i < states.length; i++)
      {
	ss[i] = build_state(states[i], emap);
      }

    return ss;
  }

  private static StateSchema build_state(StateProxy state,
					 Map<EventProxy,Integer> emap)
  {
    String name = state.getName();
    boolean initial = state.isInitial();
    int[] propositions = map_events(state.getPropositions(), emap);

    return new StateSchema(name, initial, propositions);
  }

  private static int[] map_events(Collection<EventProxy> events,
				  Map<EventProxy,Integer> emap)
  {
    int[] evs = new int[events.size()];
    int i = 0;

    for (EventProxy e : events)
      {
	evs[i] = emap.get(e);
	i++;
      }

    return evs;
  }

  private static EventSchema[] build_event_schemata(final EventProxy[] events)
  {
    EventSchema[] evs = new EventSchema[events.length];

    for (int i = 0; i < events.length; i++)
      {
	evs[i] = build_event(events[i]);
      }

    return evs;
  }

  private static EventSchema build_event(final EventProxy event)
  {
    String name = event.getName();
    boolean observable = event.isObservable();
    int kind = event_kind_convert(event.getKind());

    return new EventSchema(name, kind, observable);
  }

  private static int event_kind_convert(EventKind kind)
  {
    switch (kind)
      {
      case CONTROLLABLE:
	return EventSchema.CONTROLLABLE;
      case UNCONTROLLABLE:
	return EventSchema.UNCONTROLLABLE;
      case PROPOSITION:
	return EventSchema.PROPOSITION;
      }

    //No good default case. This shouldn't happen!
    //TODO: Better way to handle this.
    assert false: "Invalid event kind! This is bad.";
    return -1;
  }

  private static int component_kind_convert(ComponentKind kind)
  {
    switch (kind)
      {
      case PLANT:
	return AutomatonSchema.PLANT;
      case SPEC:
	return AutomatonSchema.SPECIFICATION;
      case PROPERTY:
	return AutomatonSchema.PROPERTY;
      case SUPERVISOR:
	return AutomatonSchema.SUPERVISOR;
      }

    //No good default case. This shouldn't happen!
    //TODO: Better way to handle this.
    assert false: "Invalid event kind! This is bad.";
    return -1;
  }

  private static Map<EventProxy,Integer> build_eventmap(final EventProxy[] events)
  {
    Map<EventProxy,Integer> emap = new HashMap<EventProxy,Integer>();

    for (int i = 0; i < events.length; i++)
      {
	emap.put(events[i], i);
      }

    return emap;
  }
}