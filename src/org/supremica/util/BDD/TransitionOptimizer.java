
package org.supremica.util.BDD;


/**
 * The objective of this class is to somehow optimize the transition array used in,
 * for example, PetriNetSupervisor.
 *
 *
 * with optimization we often mean to sacrifice memory for speed by joining multiple transitions into one
 */


// TODO: add more join heuristics:
// 1. greedy
// 2. free [add as long as the cover does not grow]
// 3. some cover heuristc??

public class TransitionOptimizer {

	private PerEventTransition [] org, optimized;
	private BDDAutomaton [] all;

	private TransitionOptimizer(PerEventTransition [] org, BDDAutomaton [] all)
		throws Exception
		{
		this.org = org;
		this.all = all;


		switch(Options.transition_optimizer_algo) {
			case Options.TRANSITION_OPTIMIZER_RANDOM:
				do_random();
				break;
			default:
				throw new Exception("[TransitionOptimizer] UNKNOWN optimization method.");

		}

		// MUST ALWAYS DO THIS!!!
		update_labels(); // XXX: should we move this to the static caller function??

	}


	private void cleanup()
	{

		// we own org, so lets clean it up!
		for(int i = 0; i < org.length; i++)
		{
			if(org[i] != null)  // we might set it to null to indicate that we have kept this one (for some reason)...
			{
				org[i].cleanup();
			}
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

			optimized[k-1]  = org[ p[n-1] ];
			org[p[n-1] ] = null; // IMPORTANT: so we dont clean it up!!
		}
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

		/*
		// DEBUG
		for(int i = 0; i < n; i++) if(org[i] != null) {
			Options.out.print("" + i + " ==> ");
			org[i].dump();
		}
		for(int i = 0; i < k; i++) {
			Options.out.print("" + i + " ==> ");
			optimized[i].dump();
		}
		*/

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


	// ---------------------------------------------------------------------------

	public static PerEventTransition [] optimize(PerEventTransition [] org, BDDAutomaton [] all) {

		// dont optimze at all
		if(Options.transition_optimizer_algo == Options.TRANSITION_OPTIMIZER_NONE)
		{
			return org;
		}


		try {
			// optimize it and free the original
			TransitionOptimizer top = new TransitionOptimizer(org, all);
			PerEventTransition [] ret = top.getAnswer();
			top.cleanup();


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
