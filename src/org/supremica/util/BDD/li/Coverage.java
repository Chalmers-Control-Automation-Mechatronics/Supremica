
package org.supremica.util.BDD.li;


import org.supremica.util.BDD.*;
import org.supremica.util.BDD.heuristics.*;

// import org.supremica.automata.algorithms.*;
// import org.supremica.automata.*;


/**
 * how large of the complete system has we coveverd?
 * it also keeps track of, for example, event subsets to know how much more we
 * need to add before something is "complete".
 */

// TODO: we should reuse this object, instead of allocation new stuff all the time!

public class Coverage {
	private BaseLI li;
	private BDDAutomaton target;
	private boolean ctrl, last_was_spec;
	private int size;

	private int num_locals; /** num of local events */
	private int num_locals_plant; /** num of local events */
	private boolean [] relevant_plant_events_remain; /** events that should be added before plant has all the relevant events */
	private boolean [] local_events; /** local events */
	private boolean [] local_plant_events; /** local events in the plant */
	private int [] event_usage_plant_only; /** Event usage, but only for plants */

	private int [] event_usage_reminas; /** number of times this event has been in the "rest" of the system*/
	private int [] work_queue; /* automata to be added */
	private boolean [] in_use; /* element in work queue OR used */
	private double [] queue_costs; /* cost of elements in the queue */
	private int queue_size;	/** number of elements in the queue */
	private AutomatonSelectionHeuristic heuristic; /* quese selection herustic */

	public Coverage (BDDAutomaton target, BaseLI li) throws TrivialPass {
		this.li = li;
		this.target = target;
		this.size = li.all.length;

		int num_events = li.ba.getEvents().length;

		// Workset event is the set of (uncontrollable or any) events shared between spec and the plants
		li.workset_events   = Util.duplicate( target.getEventCareSet( li.controllaibilty_test) );
		IndexedSet.intersection(li.workset_events , li.L2.getEventCareSet(li.controllaibilty_test),  li.workset_events);


		if(IndexedSet.cardinality(li.workset_events) == 0)
		{
			throw new TrivialPass("(no intersection Sigma-P/Sigma-Sp)");
		}

		li.considred_events = Util.duplicate( target.getEventCareSet( false) );


		// work queue
		work_queue   = new int[size];
		queue_costs  = new double[size];
		in_use       = new boolean[size];
		queue_size = 0;
		IndexedSet.empty(in_use);


		// local events
		local_events = new boolean[num_events];
		local_plant_events = new boolean[num_events];
		IndexedSet.empty(local_events);
		IndexedSet.empty(local_plant_events);


		// plant events:
		relevant_plant_events_remain = new boolean[num_events];
		IndexedSet.copy(relevant_plant_events_remain , li.workset_events);

		// event usage:
		event_usage_reminas = new int[num_events];
		event_usage_plant_only = new int[num_events];
		Util.set(event_usage_reminas, 0);
		li.L2.addEventUsage( event_usage_reminas );
		Util.set(event_usage_plant_only, event_usage_reminas);

		if(li.controllaibilty_test) {
			li.L1.addEventUsage( event_usage_reminas );
			target.removeEventUsage( event_usage_reminas );
		}

		// heuristic  stuffs
		heuristic = AutomatonSelectionHeuristicFactory.createInstance(li.all, work_queue, queue_costs);
		heuristic.reset(target, li.considred_events, li.workset_events, event_usage_reminas);

		// the first round
		addRelevantAutomata();
	}

	// --------------------------------------------------------------------
	private int addRelevantAutomata() {
		int added = 0;
		for(int i = 0; i < size; i++) {
			if(li.all[i] != target && !in_use[i] && (li.controllaibilty_test || !li.is_spec[i])) {
				if(li.all[i].interact(li.considred_events) ) {
					work_queue[queue_size++] = i;
					in_use[i] = true;
					added++;
					if(Options.debug_on) {
						Options.out.println("  \tLC: added automaton " + li.all[ i ].getName() +
							", type: " + ( li.is_spec[i] ? "Spec" : "Plant")
							// + ", " + Automaton.getType ( li.all[ i ].getType() )
							);

					}
				}
			}
		}

		heuristic.choose(queue_size); // ok, change the ORDER in the queue nice and easy

		return added;
	}

	public BDDAutomaton next() {
		if(queue_size == 0) {
			if( addRelevantAutomata() == 0) {
				return null;
			}
		}

		int picked = heuristic.pick(queue_size--);



		BDDAutomaton a = li.all[ picked ];

		// how does this choice affect us?
		boolean [] new_events = a.getEventCareSet(false) ;
		IndexedSet.union(li.considred_events, new_events, li.considred_events);
		a.removeEventUsage( event_usage_reminas );

		// ok, update the set of events we need to add before all relevants events are represented at least once in the subplant
		last_was_spec = li.is_spec[picked];
		if(!last_was_spec) {
			for(int i = 0; i < new_events.length; i++)
				relevant_plant_events_remain[i] &= !new_events [i];
			a.removeEventUsage( event_usage_plant_only );
		}


		// update the local event stuff:
		num_locals = 0;
		num_locals_plant = 0;
		for(int i = 0; i < event_usage_reminas.length ; i++) {
			local_events[i] = (event_usage_reminas[i] == 0);
			local_plant_events[i] = (event_usage_plant_only[i] == 0);
			if(local_events[i]) num_locals ++;
			if(local_plant_events[i]) num_locals_plant ++;
		}

		return a;
	}

	// ----------------------------------------------------------------------

	public boolean lastWasSpec() { return last_was_spec; }
	public boolean [] getLocalEvents() { return local_events; }
	public boolean [] getLocalPlantEvents() { return local_plant_events; }

	public int getNumOfLocalEvents() { return num_locals; }
	public int getNumOfLocalPlantEvents() { return num_locals_plant; }

	public boolean allPlantEventsIncluded() {
		// if we have had them all, then there is nothing more to add!
		return IndexedSet.cardinality(relevant_plant_events_remain) == 0;
	}

	public void dump() {
		li.ba.getEventManager().dumpSubset("*** LC:workset_events", li.workset_events);
		li.ba.getEventManager().dumpSubset("*** LC:considred_events", li.considred_events);
		li.ba.getEventManager().dumpSubset("*** LC:relevant_plant_events_remain", relevant_plant_events_remain);
		li.ba.getEventManager().dumpSubset("*** LC:local-events", local_events);

	}

}