
package org.supremica.util.BDD;

import java.util.*;

public class Workset
{

	private int [] workset, queue;
	private int [][] dependent;
	private boolean [] remaining;// this is for the exclusive stuff
	private int size;
	private int workset_count;// sum of workset_i

	public Workset(int size, int [][]dependent)
	{
		this.size = size;
		this.dependent = dependent;
		this.workset = new int[size];
		this.queue = new int[size];

		remaining = new boolean[size];

	}


	public void init_workset(boolean exclusive) {
		// all automata are enabled from start
		workset_count = size;
		for(int i = 0; i < size; i++) workset[i] = 1;

		if(exclusive) // everything reamining, yet...
			for(int i = 0; i < size; i++) remaining[i] = true;
	}

	public String getHeuristicName() {
		switch(Options.es_heuristics) {
			case Options.ES_HEURISTIC_RANDOM:
				return "random";
			case Options.ES_HEURISTIC_MOST_PENDING:
				return "most pending";
			case Options.ES_HEURISTIC_LEAST_PENDING:
				return "least pending";
			case Options.ES_HEURISTIC_MOST_MEMBERS:
			case Options.ES_HEURISTIC_MOST_FOLLOWERS:
				return "most followers";
			case Options.ES_HEURISTIC_LEAST_MEMBERS:
			case Options.ES_HEURISTIC_LEAST_FOLLOWERS:
				return "least followers";
			default:
				return "(unknown??)";
		}
	}

	/**
	 * choose the next automaton
	 *
	 * This is a rip-off from the PetriNetSupervisor :)
	 */
	public int pickOne() {

		int best, queue_size = 0;

		switch(Options.es_heuristics) {
			case Options.ES_HEURISTIC_RANDOM:
				for(int i = 0; i < size; i++) // anything is ok
					if(workset[i] > 0)  queue[queue_size++] = i;
				break;
			case Options.ES_HEURISTIC_MOST_PENDING:
				// one of the largest one (most affected so far)
				best = 0;
				for(int i = 0; i < size; i++) {
					if(workset[i] > 0) {
						if(best < workset[i]) {
							best = workset[i];
							queue_size = 0;
						}
						if(best == workset[i]) queue[queue_size++] = i;
					}
				}
				break;

			case Options.ES_HEURISTIC_LEAST_PENDING:
			// one of the smallest one (least affected so far)
				best = Integer.MAX_VALUE;
				for(int i = 0; i < size; i++) {
					if(workset[i] > 0) {
						if(best >  workset[i]) {
							best = workset[i];
							queue_size = 0;
						}
						if(best == workset[i]) queue[queue_size++] = i;
					}
				}
				break;
			case Options.ES_HEURISTIC_MOST_MEMBERS:
			case Options.ES_HEURISTIC_MOST_FOLLOWERS:
				best = 0;
				for(int i = 0; i < size; i++) {
					if(workset[i] > 0) {
						int c = dependent[i][0];
						if(best < c) {
							best = c;
							queue_size = 0;
						}
						if(best == c) queue[queue_size++] = i;
					}
				}

			break;
			case Options.ES_HEURISTIC_LEAST_MEMBERS:
			case Options.ES_HEURISTIC_LEAST_FOLLOWERS:
				best = Integer.MAX_VALUE;;
				for(int i = 0; i < size; i++) {
					if(workset[i] > 0) {
						int c = dependent[i][0];
						if(best > c) {
							best = c;
							queue_size = 0;
						}
						if(best == c) queue[queue_size++] = i;
					}
				}
			break;
		}


		// choose one by random
		if(queue_size > 1) {
			return queue[ (int)(Math.random() * queue_size) ];
		} else {
			return (queue_size == 1) ? queue[0] : -1;
		}
	}


	/**
	 * this does the same thing as pickOne, but chosses each automaton/cluster only once.
	 * this is used in monotonic algorithms where each automaton is added only once
	 *
	 * again, this is another rip-off from the PetriNetSupervisor :)
	 * (if i wanst me, me should sue myself)
	 *
	 */

	public int pickOneExcelsuive()
	{


		int best, queue_size = 0;

		switch(Options.es_heuristics) {
			case Options.ES_HEURISTIC_RANDOM:
				for(int i = 0; i < size; i++) // anything is ok
					if(remaining[i] && workset[i] > 0)  queue[queue_size++] = i;
				break;
			case Options.ES_HEURISTIC_MOST_PENDING:
				// one of the largest one (most affected so far)
				best = 0;
				for(int i = 0; i < size; i++) {
					if(remaining[i] && workset[i] > 0 ) {
						if(best < workset[i]) {
							best = workset[i];
							queue_size = 0;
						}
						if(best == workset[i]) queue[queue_size++] = i;
					}
				}
				break;

			case Options.ES_HEURISTIC_MOST_MEMBERS:
			case Options.ES_HEURISTIC_MOST_FOLLOWERS:
				best = 0;
				for(int i = 0; i < size; i++) {
					if(remaining[i] && workset[i] > 0) {
						int c = dependent[i][0];
						if(best < c) {
							best = c;
							queue_size = 0;
						}
						if(best == c) queue[queue_size++] = i;
					}
				}

			break;
			// -------------------------------------------------------------
			case Options.ES_HEURISTIC_LEAST_PENDING:
			// one of the smallest one (least affected so far)
				best = Integer.MAX_VALUE;
				for(int i = 0; i < size; i++) {
					if(remaining[i] && workset[i] > 0) {
						if(best >  workset[i]) {
							best = workset[i];
							queue_size = 0;
						}
						if(best == workset[i]) queue[queue_size++] = i;
					}
				}
				break;

			case Options.ES_HEURISTIC_LEAST_MEMBERS:
			case Options.ES_HEURISTIC_LEAST_FOLLOWERS:
				best = Integer.MAX_VALUE;;
				for(int i = 0; i < size; i++) {
					if(remaining[i] && workset[i] > 0 ) {
						int c = dependent[i][0];
						if(best > c) {
							best = c;
							queue_size = 0;
						}
						if(best == c) queue[queue_size++] = i;
					}
				}
			break;
		}


		if(queue_size > 1) {
			// choose one by random
			best = queue[ (int)(Math.random() * queue_size) ];
		} else {
			if(queue_size == 0) return -1;
			best = queue[0]; // no neead to call random, since "random() * 0 = 0"
		}

		remaining[best] = false;
		return best;
	}

	/**
	 * we are done with this automaton.
	 * if changed is new, then something has changed and we should consider the
	 * affect of this by adding automata that are directly connected with our automaton
	 */
	public void advance(int automaton, boolean changed)
	{
		workset_count -= workset[automaton];
		workset[automaton] = 0;

		if(changed) {
			int count = dependent[automaton][0];
			for(int i = 0 ; i < count; i++) workset[  dependent[automaton][i + 1] ] ++;
			workset_count += count;
		}

	}

	public boolean empty()
	{
		return workset_count <= 0;
	}
}