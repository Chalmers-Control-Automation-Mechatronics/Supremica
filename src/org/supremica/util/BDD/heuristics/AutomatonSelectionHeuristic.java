

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
	protected int [] queue;
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

	public abstract void choose(int queue_size, boolean [] workset_events);
}