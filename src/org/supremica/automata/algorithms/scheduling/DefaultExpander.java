/***************** DefaultExpander.java ******************/
// The default expander expands all children and returns a list
package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.properties.SupremicaProperties;

class DefaultExpander
	implements Expander
{
	private Automata system;
	private Automata specs;
	private AutomataOnlineSynchronizer onlineSynchronizer;
	private int[] initialState;
	private int[] indexmap;

	// 'system' is the entire system (specs+plants), 'specs' are the expand directions
	public DefaultExpander(Automata system)
		throws Exception
	{
		system.setIndicies();

		this.system = system;
		this.specs = new Automata();
		this.indexmap = new int[system.size()];
		this.initialState = AutomataIndexFormHelper.createState(system.size());
		int index = 0;
		
		for(Iterator autIt = system.iterator(); autIt.hasNext(); )
		{
			Automaton automaton = (Automaton) autIt.next();
			State currInitialState = automaton.getInitialState();
			initialState[automaton.getIndex()] = currInitialState.getIndex();
			automaton.remapStateIndices();		// Rebuild the maps to have the indices match up - why the f***??
			if(automaton.getType() == AutomatonType.Specification)
			{
				specs.addAutomaton(automaton);
				indexmap[automaton.getIndex()] = index++;	// count only the specs
			}
		}

		SynchronizationOptions syncOptions = new SynchronizationOptions(SupremicaProperties.syncNbrOfExecuters(), SynchronizationType.Prioritized, SupremicaProperties.syncInitialHashtableSize(), SupremicaProperties.syncExpandHashtable(), SupremicaProperties.syncForbidUncontrollableStates(), SupremicaProperties.syncExpandForbiddenStates(), false, false, false, SupremicaProperties.verboseMode(), false, true);
		AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(system, syncOptions);
		this.onlineSynchronizer = new AutomataOnlineSynchronizer(helper);

		this.onlineSynchronizer.initialize();
		this.onlineSynchronizer.setCurrState(initialState);
		helper.setCoExecuter(onlineSynchronizer);
	}
	
	public Collection expand(Element elem)
	{
		List list = new LinkedList();
		int depth = elem.getDepth() + 1;

		for(Iterator autIt = specs.iterator(); autIt.hasNext(); )
		{
			onlineSynchronizer.setCurrState(elem.getStateArray());	// we operate from this state
			Automaton currAutomaton = (Automaton) autIt.next();
			// We only look through specAutomata
			// if(currAutomaton.isSpecification())	// only do this for specs, plants/resources are not "directions"
			{
				int stateIndex = elem.getStateArray()[currAutomaton.getIndex()];
				// Now we need to find the state with this index in currAutomaton
				State s = currAutomaton.getStateWithIndex(stateIndex);
				
				/* System.out.println("Part State: " + currAutomaton.getName() + "[" + stateIndex + ":" + s.getIndex() + "]::" + s.toString());
				*/
				// Now, let us iterate over all events (locally) enabled in this state
				EventIterator evit = currAutomaton.outgoingEventsIterator(s);
				while(evit.hasNext())
				{
					LabeledEvent event = evit.nextEvent();
					
					/* System.out.print("Event: " + currAutomaton.getName() + "::" + event.toString()); */

					if(onlineSynchronizer.isEnabled(event))	// if the event is globally enabled
					{
						/* System.out.println(" is globally enabled"); */
						
						// Move in direction Ai
						Element nextState = new ElementObject(onlineSynchronizer.doTransition(event), specs.size());
						nextState.setDepth(depth);
						move(elem, nextState, currAutomaton);
						
						// attach ptr back to n
						nextState.setParent(elem);
						
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
	
	// Take a step in the direction of aut
	// g and Tv are updated as (see p.47) 
	private void move(Element state, Element nxtstate, Automaton aut)
	{
		int direction = indexmap[aut.getIndex()]; // specAutomata.getAutomatonIndex(aut); // aut.getIndex();
		for(Iterator autit = specs.iterator(); autit.hasNext(); )
		{
			Automaton automaton = (Automaton)autit.next();
			int autidx = indexmap[automaton.getIndex()]; // specAutomata.getAutomatonIndex(automaton); // automaton.getIndex();
			if(autidx != direction)
			{
				/* System.out.println("autidx :" + autidx + " direction: " + direction + " Tv.length: " + nxtstate.getTimeArray().length + ", " + state.getTimeArray().length);
				*/
				nxtstate.getTimeArray()[autidx] = Math.max(0, state.getTimeArray()[autidx] - state.getTimeArray()[direction]);
				/* System.out.println("Tv[" + autidx + "] = max(0, " + state.getTimeArray()[autidx] + " - " + state.getTimeArray()[direction] + ")");
				*/
			}
		}
		nxtstate.setCost(state.getCost() + state.getTimeArray()[direction]);
		nxtstate.setTime(direction, aut.getStateWithIndex(nxtstate.getStateArray()[direction]).getCost());
	}

}