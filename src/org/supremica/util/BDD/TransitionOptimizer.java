
package org.supremica.util.BDD;


/**
 * The objective of this class is to somehow optimize the transition array used in,
 * for example, PetriNetSupervisor.
 *
 *
 * with optimization we often mean to sacrifice memory for speed by joining multiple transitions into one
 */


public class TransitionOptimizer {

	private PerEventTransition [] org, optimized;

	private TransitionOptimizer(PerEventTransition [] org, BDDAutomaton [] all) {
		this.org = org;


		// simple pair-wise join
		int n = org.length;
		int k = n / 2;
		if( (n % 2) != 0) k++;

		optimized = new PerEventTransition[k];


		for(int i = 0; i < n-1; i += 2)
			optimized[i/2] = new PerEventTransition(org[i], org[i+1], all);


		// the last one if any:
		if((n % 2) != 0) {
			optimized[k-1]  = org[n-1];
			org[n-1] = null; // IMPORTANT: so we dont clean it up!!
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
	}
}
