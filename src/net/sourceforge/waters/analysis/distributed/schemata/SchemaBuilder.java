//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


public class SchemaBuilder
{
  private SchemaBuilder()
  {
  }

  /**
   * Build a schema for a Waters ProductDESProxy.
   */
  public static ProductDESSchema build(final ProductDESProxy des, KindTranslator translator)
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
    EventSchema[] des_events = build_event_schemata(events, translator);
    AutomatonSchema[] des_automata = build_automata(des.getAutomata(), emap, translator);

    return new ProductDESSchema(des.getName(), des_automata, des_events);
  }

  private static AutomatonSchema[] build_automata(Set<AutomatonProxy> automata,
						  Map<EventProxy,Integer> emap,
						  KindTranslator translator)
  {
    AutomatonSchema[] schemata = new AutomatonSchema[automata.size()];
    int i = 0;
    for (AutomatonProxy automaton : automata)
      {
	schemata[i] = build_automaton(automaton, emap, translator, i);
	  i++;
      }

    return schemata;
  }


  private static AutomatonSchema build_automaton(final AutomatonProxy aut, 
						 Map<EventProxy,Integer> emap,
						 KindTranslator translator,
						 int id)
  { 
    String name = aut.getName();
    int[] events = map_events(aut.getEvents(), emap);
    int kind = component_kind_convert(translator.getComponentKind(aut));

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

    return new AutomatonSchema(name, events, states, kind, transitions, id);
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
	i++;
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

  private static EventSchema[] build_event_schemata(final EventProxy[] events, 
						    KindTranslator translator)
  {
    EventSchema[] evs = new EventSchema[events.length];

    for (int i = 0; i < events.length; i++)
      {
	evs[i] = build_event(events[i], translator);
      }

    return evs;
  }

  private static EventSchema build_event(final EventProxy event, 
					 KindTranslator translator)
  {
    String name = event.getName();
    boolean observable = event.isObservable();
    int kind = event_kind_convert(translator.getEventKind(event));

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
