
package org.supremica.util.BDD;


import java.util.*;

/**
 * The objective of this class is to somehow optimize the transition array used in,
 * for example, PetriNetSupervisor.
 *
 *
 * with optimization we often mean to sacrifice memory for speed by joining multiple transitions into one
 */


// TODO: add more join heuristics:

public class TransitionOptimizer {


		private final static int
			MAX_AUTOMATA_COVER = 40, // the largest automata cover
			MAX_PARTITION_NODECOUNT = 10000; // the largest local T

	private PerEventTransition [] org, optimized;
	private BDDAutomaton [] all;
	private BDDAutomata manager;

	private TransitionOptimizer(PerEventTransition [] org, BDDAutomata manager, BDDAutomaton [] all)
		throws Exception
		{
		this.org = org;
		this.all = all;
		this.manager = manager;


		switch(Options.transition_optimizer_algo) {
			case Options.TRANSITION_OPTIMIZER_RANDOM:
				do_random();
				break;
			case Options.TRANSITION_OPTIMIZER_KNAPSACK:
				do_knapsack(false);
				break;
			case Options.TRANSITION_OPTIMIZER_KNAPSACK_SAFE:
				do_knapsack(true);
				break;
			case Options.TRANSITION_OPTIMIZER_AUTOMATON:
				do_automaton();
				break;
			default:
				throw new Exception("[TransitionOptimizer] UNKNOWN optimization method.");

		}

		// MUST ALWAYS DO THIS!!!
		update_labels(); // XXX: should we move this to the static caller function "optimize()" ??

		// see if we did correct
		verify_optimization();
	}


	private void cleanup()
	{

		// we own org, so lets clean it up!
		for(int i = 0; i < org.length; i++)
		{
				org[i].cleanup();
		}

	}

	// ---------------------------------------------------------
	/** OPTIMZE: join randomly, just for fun ... */
	private void do_random() {

		// simple pair-wise join
		int n = org.length;
		int k = n / 2;
		if( (n % 2) != 0) k++;

		int [] p = Util.permutate(n);

		optimized = new PerEventTransition[k];


		for(int i = 0; i < n-1; i += 2)
			optimized[i/2] = new PerEventTransition(org[ p[i] ], org[ p[i+1] ], all);


		// the last one if any:
		if((n % 2) != 0) {
			optimized[k-1] = new PerEventTransition( org[ p[n-1] ] ); // clone
		}
	}

	// ---------------------------------------------------------
	private void do_knapsack(boolean safe) {
		int n = org.length;

		// make a deep copy of the original
		PerEventTransition [] copy = new PerEventTransition[n];
		for(int i = 0; i < n; i++) copy[i] = new PerEventTransition(org[i]);


		// sort it, largest one first
		QuickSort.sort(copy, true);

		for(int i = 0; i < n; i++)
		{
			if(copy[i] == null) continue;

			// we wont let the weight get bigger than this!
			double max = copy[i].weight() * 1.5; // XXX: but what if the weight is already very large, say 100% ?
			boolean done = false;
			do {

				// find the one with the lowest ADDITION cost
				int best_index = -1;
				double best_cost = Double.MAX_VALUE;
				for(int j = n-1; j > i; j--) {
					if(copy[j] == null) continue;

					double add_cost = PerEventTransition.estimateAddCost( copy[i], copy[j]);
					if(add_cost < best_cost) {
						best_cost = add_cost;
						best_index = j;
					}
				}

				// if anything good found, add it to our current transition
				if(best_index != -1) {
					PerEventTransition tmp = new PerEventTransition(copy[i], copy[best_index], all);
					copy[i].cleanup();	copy[i] = tmp;
					copy[best_index].cleanup();	copy[best_index] = null;

					// are we getting to fat?
					if(safe)  done = should_stop(copy[i]);

				} else {

					// indicate that nothing more could be found!
					done = true;
				}

			} while(!done && (copy[i].weight() < max) );

		}


		// and create the optimized version
		int count = 0;
		for(int i = 0; i < n; i++)
		{
			if(copy[i] != null) count++;
		}
		optimized = new PerEventTransition[count];
		count = 0;
		for(int i = 0; i < n; i++)
		{
			if(copy[i] != null) optimized[count++] = copy[i];
		}
	}


	// ---------------------------------------------------------

	private void do_automaton() {
		int n = org.length;

		// list of events already used
		boolean [] used = new boolean[n];
		for(int i = 0; i < n; i++) used[i] = false;


		LinkedList new_list = new LinkedList();

		for(int i = 0; i < all.length; i++) {
			int[] usage = all[i].getEventUsageCount();

			PerEventTransition partition = null;
			boolean finish = false;
			for(int j = 0; (j < usage.length) && !finish; j++) {
				if(!used[j] && usage[j] > 0) {
					used[j] = true;
					if(partition == null) partition = new PerEventTransition(org[j]);
					else {
						PerEventTransition tmp = partition;
						partition = new PerEventTransition(partition, org[j], all);
						tmp.cleanup();
						finish = should_stop(partition);
					}
				}
			}

			if(partition != null) new_list.add(partition);
		}

		// add those we havent added ealier
		for(int i = 0; i < n; i++) {
			if(!used[i]) new_list.add( new PerEventTransition(org[i]) );
		}


		// create an array of it
		optimized = new PerEventTransition[new_list.size()];
		int idx = 0;
		for(Iterator it = new_list.iterator(); it.hasNext() ; ) {
			optimized[idx++] = (PerEventTransition) it.next();
		}
	}

	// returns true when we thinks that nothing more should be added to this partition
	private boolean should_stop(PerEventTransition pe)
	{
		if(pe.getNumberOfAutomata() > MAX_AUTOMATA_COVER) return true;
		if( manager.nodeCount( pe.getLocalT() ) > MAX_PARTITION_NODECOUNT) return true;
		return false;
	}
	// ---------------------------------------------------------


	/**
	 * we must - ALWAYS - update our next/pre lists w.r.t tO the new PerEventTransition:s
	 */
	private void update_labels()
	{
		int n = org.length;
		int k = optimized.length;


		// 1. get the new names
		int [] perm  = new int[n]; // no need to clear. it all will be overwritten
		for(int i = 0; i < k; i++)
		{
			optimized[i].fillPerm(perm, i);
		}

		// 2. update each system with these new names
		for(int i = 0; i < k; i++)
		{
			optimized[i].renameEvent(perm, k, i);
		}


		// DEBUG
		for(int i = 0; i < n; i++)  {
			Options.out.print("" + i + " ==> ");
			org[i].dump();
		}
		for(int i = 0; i < k; i++) {
			Options.out.print("" + i + " ==> ");
			optimized[i].dump();
		}


	}

	// ---------------------------------------------------------
	/**
	 * return the optimized transition systems...
	 *
	 */
	private PerEventTransition [] getAnswer()
	{
		return optimized;
	}

	// ------------------------------------------------------------------------
	// simple tests to verify the integrity of the optimizer
	private void verify_optimization() {

		// see if we added them all
		int cover = 0;
		for(int i = 0; i < optimized.length; i++) cover += optimized[i].getNumberOfIncludedEvents();
		if(cover != org.length) {
			System.err.println("NOT ALL EVENTS INCLUDED: " + cover + " vs " + org.length);
			System.exit(20);
		}

	}

	// ---------------------------------------------------------------------------

	public static PerEventTransition [] optimize(PerEventTransition [] org, BDDAutomata manager, BDDAutomaton [] all) {

		// dont optimze at all
		if(Options.transition_optimizer_algo == Options.TRANSITION_OPTIMIZER_NONE)
		{
			return org;
		}


		try {
			// optimize it and free the original
			Timer timer = new Timer("TransitionOptimizer");
			TransitionOptimizer top = new TransitionOptimizer(org, manager, all);
			PerEventTransition [] ret = top.getAnswer();
			top.cleanup();
			timer.report("Optimization finished");


			// show some stats
			SizeWatch.setOwner("TransitionOptimizer");
			for (int i = 0; i < ret.length; i++)
			{
				SizeWatch.report(ret[i].getLocalT(), ret[i].toString() );
			}

			return ret;


		} catch(Exception exx) {
			System.err.println(exx);
			return org; // use the original if optimization failed
		}
	}
}
