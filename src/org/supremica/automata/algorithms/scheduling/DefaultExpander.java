
/***************** DefaultExpander.java ******************/

/**
 *      The default expander expands all children and returns a list
 */
package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;

class DefaultExpander
	implements Expander
{
	private Automata system;
	private Automata specs;

	//private AutomataOnlineSynchronizer onlineSynchronizer;
	private AutomataSynchronizerExecuter onlineSynchronizer;
	private int[] initialState;

	// indexmap maps automaton.getIndex() to a "linear" index used for...?
	private int[] indexmap;
	private static Logger logger = LoggerFactory.createLogger(DefaultExpander.class);

	// 'system' is the entire system (specs+plants), 'specs' are the expand directions
	public DefaultExpander(Automata system)
		throws Exception
	{

		// all of this index-stuff is driving me *mad*
		system.setIndicies();

		this.system = system;
		this.specs = new Automata();
		this.indexmap = new int[system.size()];    // specs.size would not suffice
		this.initialState = AutomataIndexFormHelper.createState(system.size());

		int index = 0;

		for (Iterator autIt = system.iterator(); autIt.hasNext(); )
		{
			Automaton automaton = (Automaton) autIt.next();
			State currInitialState = automaton.getInitialState();

			initialState[automaton.getIndex()] = currInitialState.getIndex();

			automaton.remapStateIndices();    // Rebuild the maps to have the indices match up - why the f***??

			if (automaton.getType() == AutomatonType.Specification)    // also AutomatonType.Supervisor?
			{
				specs.addAutomaton(automaton);

				indexmap[automaton.getIndex()] = index;    // count only the specs

				logger.debug(automaton.getName() + " indexmap[" + automaton.getIndex() + "] = " + index);

				index++;
			}
			/* debug */
			else
			{
				logger.debug(automaton.getName() + " has index " + automaton.getIndex() + " (not entered into indexmap)");
			}
			/* debug */
		}

		// Generate options from default
		SynchronizationOptions syncOptions = SynchronizationOptions.getDefaultSynchronizationOptions();
		syncOptions.setBuildAutomaton(false);
		syncOptions.setRequireConsistentControllability(false);

		// Get helper
		AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(system, syncOptions);

		// Initialize onlineSynchronizer
		this.onlineSynchronizer = new AutomataSynchronizerExecuter(helper);
		this.onlineSynchronizer.initialize();
		this.onlineSynchronizer.setCurrState(initialState);
		helper.setCoExecuter(onlineSynchronizer);
	}

	public Collection expand(Element elem)
	{
		List list = new LinkedList();
		int depth = elem.getDepth() + 1;

		logger.debug("Element: " + elem.toString());

		for (Iterator autIt = specs.iterator(); autIt.hasNext(); )
		{

			// onlineSynchronizer.setCurrState(elem.getStateArray());       // we operate from this state
			Automaton currAutomaton = (Automaton) autIt.next();

			logger.debug("Expanding automaton: " + currAutomaton.getName() + "(depth: " + depth + ") from state " + elem.toString());

			// We only look through specAutomata
			// if(currAutomaton.isSpecification())  // only do this for specs, plants/resources are not "directions"
			{
				int stateIndex = elem.getStateArray()[currAutomaton.getIndex()];

				// Now we need to find the state with this index in currAutomaton
				State s = currAutomaton.getStateWithIndex(stateIndex);

				logger.debug("Part State: " + currAutomaton.getName() + "[" + stateIndex + ":" + s.getIndex() + "]::" + s.toString());

				// Now, let us iterate over *all* events (locally) enabled in this state
				ArcIterator evit = s.outgoingArcsIterator();
				while (evit.hasNext())
				{
					int[] currState = elem.getStateArray();

					onlineSynchronizer.setCurrState(currState);    // we operate from this state

					LabeledEvent event = evit.nextEvent();

					logger.debug("Event: " + currAutomaton.getName() + "::" + event.toString());

					if (onlineSynchronizer.isEnabled(event))    // if the event is globally enabled
					{
						logger.debug(" is globally enabled");

						// Move in direction Ai
						Element nextState = new ElementObject(onlineSynchronizer.doTransition(currState, event), specs.size());

						nextState.setDepth(depth);
						move(elem, nextState, currAutomaton);

						// attach ptr back to n
						nextState.setParent(elem);

						//* begin debug 
						int sindx = nextState.getStateArray()[currAutomaton.getIndex()];
						State st = currAutomaton.getStateWithIndex(sindx);

						logger.debug("next part state: " + currAutomaton.getName() + "[" + sindx + ":" + st.getIndex() + "]::" + st.toString() + " (" + nextState.toString() + ")");

						//* end debug 
						// put in collection
						list.add(nextState);
					}
				}
			}
		}

		return list;
	}

	public Element getInitialState()
	{
		return new ElementObject(initialState, specs.size());
	}

	public Automata getSpecs()
	{
		return specs;
	}

	// Take a step in the direction of aut (neccessarily a spec)
	// g and Tv are updated as (see p.47) 
	private void move(Element state, Element nxtstate, Automaton aut)
	{
		/* debug */
		if (!aut.isSpecification())
		{
			logger.debug(aut.getName() + " is NOT a spec, cannot move in this 'direction'");
		}
		else
		{
			logger.debug("Moving in 'direction' " + aut.getName());
		}

		/* debug */
		int direction = indexmap[aut.getIndex()];

		for (Iterator autit = specs.iterator(); autit.hasNext(); )
		{
			Automaton automaton = (Automaton) autit.next();
			int autidx = indexmap[automaton.getIndex()];

			if (autidx != direction)
			{

				/* System.out.println("autidx :" + autidx + " direction: " + direction + " Tv.length: " + nxtstate.getTimeArray().length + ", " + state.getTimeArray().length);
				*/
				nxtstate.getTimeArray()[autidx] = Math.max(0, state.getTimeArray()[autidx] - state.getTimeArray()[direction]);

				logger.debug("Tv[" + autidx + "] = max(0, " + state.getTimeArray()[autidx] + " - " + state.getTimeArray()[direction] + ")");
			}
		}

		// Note the time array is of specs-size, but the state array is of system size (confusing? yes!)
		nxtstate.setCost(state.getCost() + state.getTimeArray()[direction]);

		// nxtstate.setTime(direction, aut.getStateWithIndex(nxtstate.getStateArray()[direction]).getCost());
		nxtstate.setTime(direction, aut.getStateWithIndex(nxtstate.getStateArray()[aut.getIndex()]).getCost());
	}
}
