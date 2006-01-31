/********************* Forbidder ***********************/
// Owner: MF
/**
 * Implements modular forbidden states by forbidden event
 * self-loops. Takes Automata and a set of global states
 * and generates a unique forbidden event for each state,
 * and self-loops the states of the respective automata
 * with the forbidden event. The automata are first copied
 * as plants, saturated with uc-events, and then treated as
 * the other plants.
 */

package org.supremica.automata.algorithms;

import org.supremica.log.*;
import org.supremica.automata.Arc;
import org.supremica.automata.State;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.Alphabet;
import org.supremica.automata.ForbiddenEvent;
import org.supremica.automata.algorithms.SearchStates;
import org.supremica.gui.VisualProject;
import org.supremica.gui.MonitorableThread;

public class Forbidder
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.createLogger(Forbidder.class);

	private Automaton[] the_automata;
	private int[] selected_indices;
	private SearchStates search_states;
	private VisualProject the_project;
	private Automaton[] the_specs;
	private int num_automata;

	public Forbidder(Automata automata, int[] selects, SearchStates ss, VisualProject project)
	{
		// this.the_automata = new Automata(automata); // makes a deep copy, hopefully order-preserving
		//** We are not guaranteed that this copy constructor will preserve order,
		//** We have to manage this ourselves, alas! we use an array Automaton[]
		this.copyAutomata(automata);
		this.selected_indices = selects;
		this.search_states = ss;
		this.the_project = project;
		this.the_specs = new Automaton[selected_indices.length];

		// for each automaton, make it a plant, uc-saturate it
		fiddleAutomata();

		// for each global state, create a forbidden event, selfloop it at each corresponding local state
		for(int i = 0; i < selected_indices.length; ++i)
		{
			int index = selected_indices[i];	// get index for global state

			// make project-global unique forbidden event,
			ForbiddenEvent x_event = new ForbiddenEvent(the_project.getUniqueEventLabel("¤ x" + i));
			x_event.setControllable(false);
			logger.debug(x_event.getLabel());

			// for each automaton, find the local state, self-loop the x_event
			for(int a = 0; a < num_automata; ++a)
			{
				/** let addForbiddenEvent handle all this
				// Get automaton
				Automaton automaton = the_automata[a];
				// Add the event
				automaton.getAlphabet().addEvent(x_event);
				// get local state
				State state = search_states.getState(a, index);
				// This should have a corresponding state in automaton
				State curr_state = automaton.getStateWithName(state.getName());
				if(curr_state == null) // if not found, somethig is _seriously_ wrong
				{
					logger.debug("Cannot find state " + state.getName() + " in automaton " + automaton.getName());
				}
				// add self-loop
				automaton.addArc(new Arc(curr_state, curr_state, x_event));
				logger.debug("Selflooping " + automaton.getName() + ": " + curr_state.getName());
				**/
				addForbiddenEvent(a, index, x_event);
			}

			// make a spec that forbids only this event
			the_specs[i] = makeSpec(x_event);
		}

		// add the result to the project
		addResult();
	}

	/**
	 * This one has no selection, this is taken to mean the cross-product between the found states
	 * This is true for fixed-form searches, but not necessarily for free-from(?)
	 **/
	public Forbidder(Automata automata, SearchStates ss, VisualProject project)
	{
		// this.the_automata = new Automata(automata); // makes a deep copy, hopefully order-preserving
		//** We are not guaranteed that this copy constructor will preserve order,
		//** We have to manage this ourselves, alas! we use an array Automaton[]
		this.copyAutomata(automata);
		this.selected_indices = null;
		this.search_states = ss;
		this.the_project = project;
		this.the_specs = new Automaton[1];

		// for each automaton, make it a plant, uc-saturate it
		fiddleAutomata();

		// Now we have a single project-global unique forbidden event,
		ForbiddenEvent x_event = new ForbiddenEvent(the_project.getUniqueEventLabel("¤ xx"));
		x_event.setControllable(false);
		logger.debug(x_event.getLabel());

		// for each automaton, self-loop the x_event at all found local states
		for(int a = 0; a < num_automata; ++a)
		{
			/** let addForbiddenEvent handle all this - slightly inefficient, more secure
			// Get automaton
			Automaton automaton = the_automata[a];
			// Add the event
			automaton.getAlphabet().addEvent(x_event);
			**/

			// for each local state
			for(int index = 0; index < search_states.numberFound(); ++index)
			{
				/** let addForbiddenEvent handle all this
				// get the local state
				State state = search_states.getState(a, index);
				// This should have a corresponding state in automaton
				State curr_state = automaton.getStateWithName(state.getName());
				if(curr_state == null) // if not found, somethig is _seriously_ wrong
				{
					logger.debug("Cannot find state " + state.getName() + " in automaton " + automaton.getName());
				}
				// add self-loop - if not already there
				if(curr_state.doesDefine(x_event) == false)
				{
					automaton.addArc(new Arc(curr_state, curr_state, x_event));
					logger.debug("Selflooping " + automaton.getName() + ": " + curr_state.getName());
				}
				**/
				addForbiddenEvent(a, index, x_event);
			}
		}

		// Add spec with only this event, initial state and no transitions
		the_specs[0] = makeSpec(x_event);
		// add the result to the project
		addResult();
	}

	/**
 	 * Guaranteeing order-preserving copy
 	 * Would a class be useful for others?
	 **/
	private void copyAutomata(Automata automata)
	{
		this.num_automata = automata.nbrOfAutomata();
		this.the_automata = new Automaton[num_automata];

		for(int i = 0; i < num_automata; ++i)
		{
			the_automata[i] = new Automaton(automata.getAutomatonAt(i));
		}
	}

	/**
	 * Set each automaton as plant, uc-saturate and rename them
	 **/
	 private void fiddleAutomata()
	 {
		// for each automaton, make it a plant, uc-saturate it
		for(int i = 0; i < num_automata; ++i)
		{
			Automaton automaton = the_automata[i];
			// set copy to be plant
			automaton.setType(AutomatonType.Plant);
			// uc-saturate copy
			automaton.saturateLoop(automaton.getAlphabet().getUncontrollableAlphabet());
			// rename copy
			String old_name = automaton.getName();
			automaton.setName(the_project.getUniqueAutomatonName("¤ " + old_name));
		}
	 }

	 /**
	  * Add forbidden event, with some sanity checks
	  ***/
	 private void addForbiddenEvent(int a, int index, ForbiddenEvent x_event)
	 {
		// Get automaton
		Automaton automaton = the_automata[a];
		// Add the event - beware, adding an existig event throws exception
		if(automaton.getAlphabet().contains(x_event) == false)
		{
			automaton.getAlphabet().addEvent(x_event);
		}
		// get the local state
		State state = search_states.getState(a, index);
		// This should have a corresponding state in automaton
		State curr_state = automaton.getStateWithName(state.getName());
		if(curr_state == null) // if not found, somethig is _seriously_ wrong
		{
			logger.debug("Cannot find state " + state.getName() + " in automaton " + automaton.getName());
		}
		// add self-loop - if not already there
		if(curr_state.doesDefine(x_event) == false)
		{
			automaton.addArc(new Arc(curr_state, curr_state, x_event));
			logger.debug("Selflooping " + automaton.getName() + ": " + curr_state.getName());
		}
	 }

	/**
	 * Make a spec with a single marked state, and only this forbidden event in its alphabet
	 **/
	private Automaton makeSpec(ForbiddenEvent x_event)
	{
		Automaton spec = new Automaton(x_event.getLabel()); // same name as the event-label
		spec.setType(AutomatonType.Specification);
		spec.getAlphabet().addEvent(x_event);
		State init_state = new State("q0");
		init_state.setInitial(true);
		init_state.setAccepting(true);
		spec.addState(init_state);
		return spec;
	}

	/**
	 * Add result to the project
	 **/
	 private void addResult()
	 {
	 	for(int i = 0; i < num_automata; ++i)
		{
			the_project.addAutomaton(the_automata[i]);
		}
		for(int i = 0; i < the_specs.length; ++i)
		{
			the_project.addAutomaton(the_specs[i]);
		}
	 }
}
