package org.supremica.util.BDD;

/**
 * This class optimize the number and BDD size of disjunctive transition relations.
 *
 * The idea is to join/splitt disjunctive transition relations to make things easier
 * for the BDD engine, but we havent figured out how to do that yet :(
 *
 */

public class DisjOptimizer
{
	private GroupHelper gh;
	private BDDAutomata manager;
	private int max_size, size;
	private int[] twave, twave_u;
	private Cluster[] clusters;

	// DEBUG public BDDRefCheck refcheck;
	public DisjOptimizer(BDDAutomata manager, GroupHelper gh)
	{
		this.gh = gh;
		this.manager = manager;
		max_size = size = gh.getSize();
		twave = new int[size];
		twave_u = new int[size];
		clusters = new Cluster[size];

		int[] tmp = gh.getTwave();
		int[] tmp2 = gh.getTwaveUncontrollable();
		BDDAutomaton[] automata = gh.getSortedList();

		// DEBUG refcheck = new BDDRefCheck(manager, "DisjOptimizer");
		for (int i = 0; i < size; i++)
		{

			// make this copy our own
			twave[i] = manager.ref( tmp[i] );
			twave_u[i] = manager.ref( tmp2[i] );

			clusters[i] = new Cluster(manager, automata[i]);
		}

		optimize();
	}

	public void cleanup()
	{

		// DEBUG check("cleanup()");
		for (int i = 0; i < size; i++)
		{
			manager.deref(twave[i]);
			manager.deref(twave_u[i]);
			clusters[i].cleanup();
		}
	}

	// ----------------------------------------------------

	public int getSize()
	{
		return size;
	}

	public Cluster[] getClusters()
	{
		return clusters;
	}


	// ----------------------------------------------------

	/**
	 * optimize the disjunctive transfer functions, by some algorithm.
	 */
	void optimize()
	{

		// no optimization??
		if(Options.disj_optimizer_algo == Options.DISJ_OPTIMIZER_NONE)
		{
			for(int i = 0; i < size; i++)
			{
				clusters[i].setActive(true);
			}
			return;
		}

		// this is the re-ordered copy we will use:
		Cluster [] copy = new Cluster [size];

		// now, get the order: first, see if it is random
		if(Options.disj_optimizer_algo == Options.DISJ_OPTIMIZER_RANDOM)
		{

			int [] perm = Util.permutate(size);
			for(int i = 0; i < size; i++)
			{
				copy[i] = clusters[ perm[i]];
			}
		}
		else  // ot if it is not random:
		{
			double [] weight = new double[size];
			for(int i = 0; i < size; i++)
			{
				copy[i] = clusters[i];
				if(Options.disj_optimizer_algo == Options.DISJ_OPTIMIZER_STATE_VECTOR_SIZE)
				{
					weight[i] = clusters[i].getSizeOfS();
				}
				else if(Options.disj_optimizer_algo == Options.DISJ_OPTIMIZER_DEPENDENCY_SIZE)
				{
					weight[i] = clusters[i].getDependencySize();
				}
				else if(Options.disj_optimizer_algo == Options.DISJ_OPTIMIZER_INV_DEPENDENCY_SIZE)
				{
					weight[i] = 1 / clusters[i].getDependencySize(); // it is never zero (includes itself, remember)?
				}

				else
				{
					// should not happen:
					throw new RuntimeException("INTERNAL ERROR: bad optimizer algo: " + Options.disj_optimizer_algo);
				}
			}

			// now, sort it with whatever that weight is:
			QuickSort.sort(copy, weight, size, true);

		}




		// create a set for the added events
		boolean [] events_have = new boolean[ manager.getEvents().length ];
		IndexedSet.empty(events_have);

		// and do the insertation
		int current = 0;
		for(int i = 0; i < size; i++)
		{
			boolean [] new_events = copy[i].getCareSet();

			if(IndexedSet.subseteq(new_events, events_have))
			{
				copy[i].setActive(false);
				if(Options.debug_on)
				{
					Options.out.println("Cluster removed by optimizer: " + copy[i]);
				}
			}
			else
			{
				IndexedSet.add(events_have, new_events);
				copy[i].setActive(true);
			}
		}
	}

}


