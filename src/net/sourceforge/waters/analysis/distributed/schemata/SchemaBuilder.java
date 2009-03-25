package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.base.ComponentKind;


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
    ProductDESSchema schema = new ProductDESSchema();

    schema.name = des.getName();

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
    schema.events = build_event_schemata(events);
    
    schema.automata = build_automata(des.getAutomata, emap);

    return schema;
  }

  private static AutomatonSchema[] build_automata(Set<AutomatonProxy> automata,
						  Map<EventProxy,Integer> emap)
  {
    AutomatonSchema[] schemata = new AutomatonSchema[automata.size()];
    int i = 0;
    for (AutomatonProxy automaton : automata)
      {
	schemata[i] = build_automaton(automaton, emap)
	  i++;
      }

    return schemata;
  }


  private static AutomatonSchema build_automaton(final AutomatonProxy aut, 
						   Map<EventProxy,Integer> emap)
  {
    AutomatonSchema as = new AutomatonSchema();
    
    as.name = au.getName();
    as.events = map_events(au.getEvents(), emap);
    as.kind = component_kind_convert(au.getKind());

    //Like with events in the DES, build an array of states in the
    //model which will be used to give a definite ordering. Build a
    //map which will be used to look up states when building
    //transitions.
    Set<StateProxy> au_state_set = au.getStates();
    StateProxy[] au_states = new StateProxy[au_state_set.size()];
    int i = 0;
    for (StateProxy s : au_state_set)
      {
	au_states[i] = s;
	i++;
      }

    Map<StateProxy,Integer> smap = build_statemap(au_states);
    as.states = build_states(au_states, emap);
    as.transitions = build_transitions(au.getTransitions(), smap, emap);

    return as;
  }

  private static TransitionSchema[] build_transitions(Collection<TransitionProxy> ts,
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
    TransitionSchema transition = new TransitionSchema();
    transition.source = smap.get(t.getSource());
    transition.target = smap.get(t.getTarget());
    transition.event = emap.get(t.getEvent());

    return transition;
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
    StateSchema[] ss = new StateSchema[states.length()];

    for (int i = 0; i < states.length; i++)
      {
	ss[i] = build_state(s, emap);
      }

    return ss;
  }

  private static StateSchema build_state(StateProxy state,
					 Map<EventProxy,Integer> emap)
  {
    StateSchema s = new StateSchema();
    
    s.name = state.getName();
    s.initial = state.getInitial();
    s.propositions = map_events(state.getPropositions(), emap);

    return s;
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
    EventSchema ev = new EventSchema();
    ev.name = event.getName();
    ev.observable = event.getObservable();
    ev.kind = event_kind_convert(event.getKind());

    return ev;
  }

  private static int event_kind_convert(EventKind kind)
  {
    switch (kind)
      {
      case EventKind.CONTROLLABLE:
	return EventSchema.CONTROLLABLE;
      case EventKind.UNCONTROLLABLE:
	return EventSchema.UNCONTROLLABLE;
      case EventKind.PROPOSITION:
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
      case ComponentKind.PLANT:
	return AutomatonSchema.PLANT;
      case ComponentKind.SPECIFICATION:
	return AutomatonSchema.SPECIFICATION;
      case ComponentKind.PROPERTY:
	return AutomatonSchema.PROPERTY;
      case ComponentKind.SUPERVISOR:
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