
package org.supremica.util.BDD;



import org.supremica.util.BDD.heuristics.*;

import java.util.*;


/**
 * The workset algorithms works just like the PetriNet reachability algo, but it treats zappsp
 * each automaton in the same way as a single transition should be treated...
 *
 * It has very good time and memory performance with the "right" heuristics!
 */

public class Workset
{

	private int [] workset, queue;
	private int [][] dependent;
	private boolean [] remaining;// this is for the exclusive stuff
	private int size, heuristic;
	private int workset_count;// sum of workset_i
	private Cluster [] clusters;
	private InteractiveChoice ic = null;
	private NDAS_Choice ndas = null;

	public Workset(Cluster [] clusters, int size, int [][]dependent)
	{
		this.size = size;
		this.dependent = dependent;
		this.workset = new int[size];
		this.queue = new int[size];
		this.clusters = clusters;

		remaining = new boolean[size];
		heuristic = Options.es_heuristics;

		ndas = new NDAS_Choice(size);

		if(heuristic == Options.ES_HEURISTIC_INTERACTIVE) {
			ic = new InteractiveChoice("Workset interactive automaton selection");
		}

	}


	public void init_workset(boolean exclusive) {
		// all automata are enabled from start
		workset_count = size;
		for(int i = 0; i < size; i++) workset[i] = 1;

		if(exclusive) // everything reamining, yet...
			for(int i = 0; i < size; i++) remaining[i] = true;

		ndas.reset();
	}

	public void done() {
		ndas.done();
	}
	// ---------------------------------------------------
	public String getHeuristicName() {
		return Options.ES_HEURISTIC_NAMES[Options.es_heuristics];
	}


	private int pickOneInteractive(boolean exclusive) {
		ic.choice.removeAll();

		int queue_size = 0;
		for(int i = 0; i < size; i++)
			if(( !exclusive || remaining[i]) && workset[i] > 0 ) {
				ic.choice.add( clusters[i].toString() );
				queue[queue_size++] = i;
			}

		ic.show();
		return queue[ ic.getSelected() ];
	}

	/**
	 * choose the next automaton
	 *
	 * This is a rip-off from the PetriNetSupervisor :)
	 */
	public int pickOne() {

		int best, queue_size = 0;

		switch(heuristic) {
			case Options.ES_HEURISTIC_INTERACTIVE:
				return pickOneInteractive(false);


			case Options.ES_HEURISTIC_ANY:
				for(int i = 0; i < size; i++) // anything is ok
					if(workset[i] > 0)  queue[queue_size++] = i;
				break;

			case Options.ES_HEURISTIC_TOPDOWN:
				for(int i = 0; i < size; i++)
					if(workset[i] > 0)  return i;
				break;
			case Options.ES_HEURISTIC_BOTTOMUP:
				for(int i = size-1; i >= 0; i--)
					if(workset[i] > 0)  return i;
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


		return ndas.choose(queue, queue_size);
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

		switch(heuristic) {
			case Options.ES_HEURISTIC_INTERACTIVE:
				return pickOneInteractive(true);

			case Options.ES_HEURISTIC_ANY:
				for(int i = 0; i < size; i++) // anything is ok
					if(remaining[i] && workset[i] > 0)  queue[queue_size++] = i;
				break;

			case Options.ES_HEURISTIC_TOPDOWN:
				for(int i = 0; i < size; i++)
					if(remaining[i] && workset[i] > 0) {
						queue[queue_size++] = i;
						break;
					}
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



		best = ndas.choose(queue, queue_size);
		remaining[best] = false;
		return best;
	}


	// --------------------------------------------------------------------------------

	/**
	 * we are done with this automaton.
	 * if changed is new, then something has changed and we should consider the
	 * affect of this by adding automata that are directly connected with our automaton
	 */
	public void advance(int automaton, boolean changed)
	{
		// workset_count -= workset[automaton];
		workset[automaton] = 0;
		workset_count --;
		ndas.advance(automaton, changed);

		if(changed) {
			int count = dependent[automaton][0];
			// for(int i = 1 ; i <= count; i++) workset[  dependent[automaton][i] ] ++;
			// workset_count += count;
			for(int i = 1 ; i <= count; i++) {
				int a = dependent[automaton][i];
				if(workset[a] == 0) workset_count++;
				workset[a] ++;
			}

		}

	}

	public boolean empty()
	{
		return workset_count <= 0;
	}
}