

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * interface for heuristics where given the data above, a queue is sorted with
 * (probabley) best automaton at the _end_ of the queue and the least relevant
 * in the beginning
 *
 */

public abstract class AutomatonSelectionHeuristic {

	protected BDDAutomaton [] list;
	protected BDDAutomaton first;

	protected boolean [] partition_events, relevant_events;
	protected int [] queue, event_usage;
	protected double [] queue_costs;

	public AutomatonSelectionHeuristic () {
		// this function was intentionally left blank :)
		// see init()
	}


	public void init(BDDAutomaton [] list, int [] queue, double [] queue_costs) {
		this.list = list;
		this.queue = queue;
		this.queue_costs = queue_costs;
	}

	public void reset(BDDAutomaton first, boolean [] partition_events, boolean [] relevant_events, int [] usage) {
		this.first = first;
		this.partition_events = partition_events;
		this.relevant_events = relevant_events;
		this.event_usage = usage;
	}

	public abstract void choose(int queue_size);


	public int pick(int queue_size) { return queue[queue_size-1]; }

	// for debugging only:
	public boolean [] getRelevantEvents() {
		return relevant_events;
	}
}