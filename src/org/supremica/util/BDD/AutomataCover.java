package org.supremica.util.BDD;

import org.supremica.util.BDD.heuristics.*;

public class AutomataCover
{
	private BDDAutomata manager;
	private BDDAutomaton[] all;
	@SuppressWarnings("unused")
	private AutomatonSelectionHeuristic euristics = null;
	private String name;
	private int local_count, notused_count, shared_count, bdd_t, bdd_cubes_me,
				bdd_cubes_rest, bdd_local_t;
	private int[] eventUsageCount;
	private boolean[] eventIsLocal, eventCare, automataCover;
	private boolean has_local_t;
	private int size;

	/** number of automata in this cover */

	public AutomataCover(BDDAutomata manager)
	{
		this.manager = manager;
		this.all = manager.getAutomataVector();
		this.has_local_t = false;
		size = 0;
		bdd_t = manager.getOne();

		manager.ref(bdd_t);

		bdd_cubes_me = manager.getOne();

		manager.ref(bdd_cubes_me);

		bdd_cubes_rest = manager.getStateCube();

		manager.ref(bdd_cubes_rest);

		EventManager alphabet = manager.getEventManager();
		int events = alphabet.getSize();

		eventUsageCount = new int[events];
		eventIsLocal = new boolean[events];
		eventCare = new boolean[events];
		automataCover = new boolean[all.length];

		for (int i = 0; i < events; i++)
		{
			eventCare[i] = false;
		}

		for (int i = 0; i < all.length; i++)
		{
			automataCover[i] = false;
		}

		alphabet.getUsageCount(eventUsageCount);

		notused_count = Util.countEQ(eventUsageCount, 0);
		name = "";

		updateLocalEvents();
	}

/*
		public AutomataCover(AutomataCover c1, AutomataCover c2) {
				this.manager = c1.manager;
				this.has_local_t = false;
				this.name = c1.name + " " + c2.name;
				this.size = c1.size + c2.size;

				bdd_t = manager.and(c1.bdd_t, c2.bdd_t);
				bdd_cubes_me = manager.and(c1.bdd_cubes_me , c2.bdd_cubes_me);
				bdd_cubes_rest = manager.exists(manager.getStateCube(), bdd_cubes_me );

				EventManager alphabet = manager.getEventManager();
				int events = alphabet.getSize();
				eventUsageCount = new int[events];
				eventIsLocal = new boolean[events];

				alphabet.getUsageCount(eventUsageCount);
				notused_count = c1.notused_count;

				for(int i = 0; i < events; i++) {
						eventUsageCount[i] -=  c1.eventUsageCount[i] + c2.eventUsageCount[i];
						eventCare[i] = c1.eventCare[i] | c2.eventCare[i];
				}

				IndexedSet.union(c1.automataCover, c2.automataCover, automataCover);

				updateLocalEvents();
		}

*/
	public void cleanup()
	{
		manager.deref(bdd_t);
		manager.deref(bdd_cubes_me);
		manager.deref(bdd_cubes_rest);

		if (has_local_t)
		{
			has_local_t = false;

			manager.deref(bdd_local_t);
		}
	}

	// ---------------------------------------------
	public int getSize()
	{
		return size;
	}

	public int getLocalCount()
	{
		return local_count - notused_count;
	}

	public int getNotUsedCount()
	{
		return notused_count;
	}

	public int getSharedCount()
	{
		return shared_count;
	}

	/** decide whether the given cover is a subset of/equal to us */
	public boolean subsetorEqual(AutomataCover ac)
	{

		/*
		// use events
		int len = eventIsLocal.length;
		for(int i = 0; i < len; i++)
				if(ac.eventIsLocal[i]  && ! eventIsLocal[i])
						return false;
		return true;
		*/

		// use automatas
		int len = automataCover.length;

		for (int i = 0; i < len; i++)
		{
			if (ac.automataCover[i] &&!automataCover[i])
			{
				return false;
			}
		}

		return true;
	}

	private boolean updateLocalEvents()
	{
		int len = eventIsLocal.length;
		int old_local_count = local_count;

		local_count = 0;
		shared_count = 0;

		for (int i = 0; i < len; i++)
		{
			if (eventUsageCount[i] == 0)
			{
				eventIsLocal[i] = true;

				local_count++;
			}
			else
			{
				eventIsLocal[i] = false;
			}

			if (eventCare[i] &&!eventIsLocal[i])
			{
				shared_count++;
			}
		}

		// local T no more valid (this was called from add() or the constructor
		if (has_local_t)
		{
			has_local_t = false;

			manager.deref(bdd_local_t);
		}

		return old_local_count != local_count;
	}

	public void add(BDDAutomaton a)
	{
		name = name + " " + a.getName();
		automataCover[a.getIndex()] = true;
		bdd_cubes_me = manager.andTo(bdd_cubes_me, a.getCube());

		int tmp = manager.exists(bdd_cubes_rest, a.getCube());

		manager.deref(bdd_cubes_rest);

		bdd_cubes_rest = tmp;
		bdd_t = manager.andTo(bdd_t, a.getTpri());

		IndexedSet.union(eventCare, a.getEventCareSet(false), eventCare);
		a.removeEventUsage(eventUsageCount);
		updateLocalEvents();

		size++;
	}

	public void addLocalEventsCover(boolean[] v)
	{
		IndexedSet.add(v, eventIsLocal);
	}

	// -----------------------------------------------------
	private int getLocalT()
	{
		if (!has_local_t)
		{
			computeLocalT();
		}

		return bdd_local_t;
	}

	private void computeLocalT()
	{
		int events = manager.getAlphabetSubsetAsBDD(eventIsLocal);
		int old_local_t,
			local_t = manager.relProd(bdd_t, events, manager.getEventCube());

		// Options.out.println("bdd_t"); Options.out.flush(); manager.printSet(bdd_t);
		// Options.out.println("local_t"); Options.out.flush(); manager.printSet(local_t);
		int cube_sp = manager.getStatepCube();
		int permute1 = manager.getPermuteS2Sp();
		manager.getPermuteSp2S();
		int permute3 = manager.getPermuteSpp2Sp();
		int permute4 = manager.getPermuteSp2Spp();

		do
		{
			old_local_t = local_t;

			// get T(s', s'') from T(s, s')
			int t_s_spp = manager.replace(local_t, permute4);
			int t_sp_spp = manager.replace(t_s_spp, permute1);

			manager.deref(t_s_spp);

			// T^2 = T or (E a. T(q,a) and T'(a,q'))
			int tmp1 = manager.relProd(local_t, t_sp_spp, cube_sp);
			int tmp2 = manager.replace(tmp1, permute3);

			manager.deref(tmp1);
			manager.deref(t_sp_spp);

			local_t = manager.orTo(local_t, tmp2);

			manager.deref(tmp2);
		}
		while (old_local_t != local_t);

		manager.deref(events);

		//Options.out.println("local_t (saturated"); Options.out.flush(); manager.printSet(local_t);
		int k1 = manager.getKeep();
		int k2 = manager.exists(k1, bdd_cubes_me);

		local_t = manager.andTo(local_t, k2);

		// Options.out.println("bdd_cubes_me"); Options.out.flush(); manager.printSet(bdd_cubes_me);
		// Options.out.println("k2"); Options.out.flush(); manager.printSet(k2);
		// Options.out.println("local_t (keept"); Options.out.flush(); manager.printSet(local_t);
		manager.deref(k2);

		bdd_local_t = local_t;
		has_local_t = true;
	}

	// --------------------------------------------------------------------------
	int forward_reachability(int Q_i)
	{
		int local_t = getLocalT();

		// Options.out.println("local_t (forward_reachability)"); Options.out.flush(); manager.printSet(local_t);
		manager.getPermuteS2Sp();
		int permute2 = manager.getPermuteSp2S();
		int cube_s = manager.getStateCube();
		int old_r, r = Q_i;

		manager.ref(r);

		do
		{
			old_r = r;

			int tmp1 = manager.relProd(local_t, r, cube_s);
			int tmp2 = manager.replace(tmp1, permute2);

			manager.deref(tmp1);

			// Options.out.println("r (forward_reachability)"); Options.out.flush(); manager.printSet(r);
			// Options.out.println("tmp2 (forward_reachability)"); Options.out.flush(); manager.printSet(tmp2);
			r = manager.orTo(r, tmp2);

			manager.deref(tmp2);
		}
		while (old_r != r);

		return r;
	}

	public boolean include(BDDAutomaton a)
	{
		int index = a.getIndex();

		return automataCover[index];
	}

	// -----------------------------------------------------------------
	private int[] queue;
	private int queue_size;

	public BDDAutomaton queue_pop()
	{
		if (queue_size == 0)
		{
			return null;
		}

		queue_size--;

		return all[queue[queue_size]];
	}

	public void queue_create()
	{
		int len = all.length;
		double[] queue_costs = new double[len];

		queue = new int[len];
		queue_size = 0;

		// get those that are dependent:
		int max = 0;

		for (int i = 0; i < len; i++)
		{
			BDDAutomaton automaton = all[i];

			if (!include(automaton))
			{
				int overlap = automaton.eventOverlapCount(eventCare);

				if (overlap > 0)
				{
					queue[queue_size] = i;
					queue_costs[queue_size] = overlap;

					if (max > queue_costs[queue_size])
					{
						max = (int) queue_costs[queue_size];
					}

					queue_size++;
				}
			}
		}

		if (queue_size == 0)
		{
			Options.out.println("Cover (" + name + ") is full, nothing more will be added");

			return;
		}

		if (max == 0)
		{
			max = 1;
		}

		// one step look-ahead
		len = eventUsageCount.length;

		for (int i = 0; i < queue_size; i++)
		{
			boolean[] this_use = all[queue[i]].getEventCareSet(false);
			int count = 0;

			for (int j = 0; j < len; j++)
			{
				if ((eventUsageCount[i] == 1) && this_use[j])
				{
					count++;
				}
			}

			queue_costs[i] += max * count;
		}

		// sort
		QuickSort.sort(queue, queue_costs, queue_size, false);
	}

	// -----------------------------------------------------------------
	public void dump()
	{
		manager.getEventManager().dumpSubset("Cover " + name + " local events ", eventIsLocal);
	}
}
