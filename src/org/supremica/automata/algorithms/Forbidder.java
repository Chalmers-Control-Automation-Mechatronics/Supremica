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

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.DumpState;
import org.supremica.automata.ForbiddenEvent;
import org.supremica.automata.State;
import org.supremica.gui.VisualProject;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

public class Forbidder
{
    private static Logger logger = LoggerFactory.createLogger(Forbidder.class);

    private static final String FORBIDDEN_EVENT_PREFIX = "x:";	// prefix for forbidden events
    private static final String FORBIDDEN_AUTOMATA_PREFIX = "X:";   // prefix added to name of plant automata with forbidden event selfloops

    private Automaton[] the_automata;
    private final int[] selected_indices;
    private final SearchStates search_states;
    private final VisualProject the_project;
    private final Automaton[] the_specs;
    private int num_automata;
	private boolean use_dump = false; // default is to use self-loop and not dump state

    public Forbidder(final Automata automata, final int[] selects, final SearchStates ss, final VisualProject project, final boolean use_dump)
    {
        this.selected_indices = selects;
        this.search_states = ss;
        this.the_project = project;
        this.the_specs = new Automaton[selected_indices.length];
		this.use_dump = use_dump;

		// this.the_automata = new Automata(automata); // makes a deep copy, hopefully order-preserving
        //** We are not guaranteed that this copy constructor will preserve order,
        //** We have to manage this ourselves, alas! we use an array Automaton[]
        this.copyAutomata(automata);

		/**
		 * From May 2013, we only handle plant states, so fiddleAutomata() is not needed, see below.
		 */
        // for each automaton, make it a plant if not already, then rename
        // fiddleAutomata();

        // for each global state, create a forbidden event, selfloop or dump it at each corresponding local state
        for(int i = 0; i < selected_indices.length; ++i)
        {
            final int index = selected_indices[i];	// get index for global state

            // make project-global unique forbidden event,
            final ForbiddenEvent x_event = new ForbiddenEvent(the_project.getUniqueEventLabel(FORBIDDEN_EVENT_PREFIX + i));
            x_event.setControllable(false);
            logger.debug(x_event.getLabel());

            // for each automaton, find the local state, self-loop the x_event
            for(int a = 0; a < num_automata; ++a)
            {
                /** let addForbiddenEvent handle all this
                 * // Get automaton
                 * Automaton automaton = the_automata[a];
                 * // Add the event
                 * automaton.getAlphabet().addEvent(x_event);
                 * // get local state
                 * State state = search_states.getState(a, index);
                 * // This should have a corresponding state in automaton
                 * State curr_state = automaton.getStateWithName(state.getName());
                 * if(curr_state == null) // if not found, somethig is _seriously_ wrong
                 * {
                 * logger.debug("Cannot find state " + state.getName() + " in automaton " + automaton.getName());
                 * }
                 * // add self-loop
                 * automaton.addArc(new Arc(curr_state, curr_state, x_event));
                 * logger.debug("Selflooping " + automaton.getName() + ": " + curr_state.getName());
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
    public Forbidder(final Automata automata, final SearchStates ss, final VisualProject project, final boolean use_dump)
    {
		this.selected_indices = null;
        this.search_states = ss;
        this.the_project = project;
        this.the_specs = new Automaton[1];
		this.use_dump = use_dump;
        // this.the_automata = new Automata(automata); // makes a deep copy, hopefully order-preserving
        //** We are not guaranteed that this copy constructor will preserve order,
        //** We have to manage this ourselves, alas! we use an array Automaton[]
        this.copyAutomata(automata);

		/**
		 * From May 2013, we only handle plant states, so fiddleAutomata() is not needed, see below.
		 */
        // for each automaton, make it a plant if not already, then rename
        // fiddleAutomata();

        // Now we have a single project-global unique forbidden event,
        final ForbiddenEvent x_event = new ForbiddenEvent(the_project.getUniqueEventLabel(FORBIDDEN_EVENT_PREFIX));
        x_event.setControllable(false);
        logger.debug(x_event.getLabel());

        // for each automaton, self-loop the x_event at all found local states
        for(int a = 0; a < num_automata; ++a)
        {
            /** let addForbiddenEvent handle all this - slightly inefficient, more secure
             * // Get automaton
             * Automaton automaton = the_automata[a];
             * // Add the event
             * automaton.getAlphabet().addEvent(x_event);
             **/

            // for each local state
            for(int index = 0; index < search_states.numberFound(); ++index)
            {
                /** let addForbiddenEvent handle all this
                 * // get the local state
                 * State state = search_states.getState(a, index);
                 * // This should have a corresponding state in automaton
                 * State curr_state = automaton.getStateWithName(state.getName());
                 * if(curr_state == null) // if not found, somethig is _seriously_ wrong
                 * {
                 * logger.debug("Cannot find state " + state.getName() + " in automaton " + automaton.getName());
                 * }
                 * // add self-loop - if not already there
                 * if(curr_state.doesDefine(x_event) == false)
                 * {
                 * automaton.addArc(new Arc(curr_state, curr_state, x_event));
                 * logger.debug("Selflooping " + automaton.getName() + ": " + curr_state.getName());
                 * }
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
    private void copyAutomata(final Automata automata)
    {
        this.num_automata = automata.nbrOfAutomata();
        this.the_automata = new Automaton[num_automata];

        for(int i = 0; i < num_automata; ++i)
        {
            the_automata[i] = new Automaton(automata.getAutomatonAt(i));
            // rename copy - this was moved from fiddleAutomata(), see below
			final String old_name = automata.getAutomatonAt(i).getName();
			assert(old_name != null);
			final String new_name = the_project.getUniqueAutomatonName(FORBIDDEN_AUTOMATA_PREFIX + old_name);
			assert(new_name != null);
            the_automata[i].setName(new_name);
        }
    }

    /**
     * Plantify specs
     * Rename all copies
     **//* As of May 2013, we only handle forbiding plant states. Plantify any spec first.
	 * Reason is that to plantify correctly, you need to have the correct plant alphabet
	 * So, you need to select all teh plants, plus teh specs for plantify to work
	 * But then... you do not really want all those extra plant states if you are only to forbid spec states
	 * So. Plantify the specs firs with the relevant plant alphabet, then you do state forbidding
    private void fiddleAutomata()
    {
		// Collect the uc alphabet
		Alphabet uc_alpha = new Alphabet();
		for(int i = 0; i < num_automata; ++i)
		{
			if(the_automata[i].isPlant())
				uc_alpha.union(the_automata[i].getAlphabet().getUncontrollableAlphabet());
		}
        // for each automaton, make it a plant if not already
        for(int i = 0; i < num_automata; ++i)
        {
            final Automaton automaton = the_automata[i];

            final String old_name = automaton.getName();

			if(automaton.isSpecification() || automaton.isSupervisor())
			{
				Plantifier.plantify(automaton, uc_alpha);
			}
            // rename copy
            automaton.setName(the_project.getUniqueAutomatonName(FORBIDDEN_AUTOMATA_PREFIX + old_name));
        }
    }
	************/

    /**
     * Add forbidden event, with some sanity checks
     ***/
    private void addForbiddenEvent(final int a, final int index, final ForbiddenEvent x_event)
    {
        // Get automaton
        final Automaton automaton = the_automata[a];
        // Add the event - beware, adding an existig event throws exception
        if(automaton.getAlphabet().contains(x_event) == false)
        {
            automaton.getAlphabet().addEvent(x_event);
        }
        // get the local state
        final State state = search_states.getState(a, index);
        // This should have a corresponding state in automaton
        final State curr_state = automaton.getStateWithName(state.getName());
        if(curr_state == null) // if not found, something is _seriously_ wrong
        {
            logger.debug("Cannot find state " + state.getName() + " in automaton " + automaton.getName());
            return;
        }
        // if no transition on this event already there
        if(curr_state.doesDefine(x_event) == false)
        {
			if(use_dump == false) // make self-loop
            {
				automaton.addArc(new Arc(curr_state, curr_state, x_event));
            	logger.debug("Selflooping " + automaton.getName() + ": " + curr_state.getName() + " on event " + x_event.getLabel());
			}
			else // use dump state
			{
				// Get dump state
				final DumpState dump_state = automaton.getDumpState(true);	// true means, create if not there
				automaton.addArc(new Arc(curr_state, dump_state, x_event));
            	logger.debug("Dumping " + automaton.getName() + ": " + curr_state.getName() + " on event " + x_event.getLabel());
			}
        }
    }

    /**
     * Make a spec with a single marked state, and only this forbidden event in its alphabet
     **/
    private Automaton makeSpec(final ForbiddenEvent x_event)
    {
        final Automaton spec = new Automaton(x_event.getLabel()); // same name as the event-label
        spec.setType(AutomatonType.SPECIFICATION);
        spec.getAlphabet().addEvent(x_event);
        final State init_state = new State("x0");
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
