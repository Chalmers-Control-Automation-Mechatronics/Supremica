package org.supremica.util.BDD;

import org.supremica.util.BDD.heuristics.*;

/**
 * The workset algorithms works just like the PetriNet reachability algo, but it treats zappsp
 * each automaton in the same way as a single transition should be treated...
 *
 * It has very good time and memory performance with the "right" heuristics!
 */
public class Workset
{
	private int[] workset, queue;
	private int[][] dependent;
	private boolean[] remaining;    // this is for the exclusive stuff
	private int size, heuristic;
	private int workset_count;    // sum of workset_i
	private int last_choice; // the last cluster that was chosen
	private Cluster[] clusters;
	private InteractiveChoice ic = null;
	private NDAS_Choice ndas = null;

	public Workset(Cluster[] clusters, int size, int[][] dependent)
	{
		this.size = size;
		this.dependent = dependent;
		this.workset = new int[size];
		this.queue = new int[size];
		this.clusters = clusters;
		remaining = new boolean[size];
		heuristic = Options.es_heuristics;
		ndas = new NDAS_Choice(size);

		if (heuristic == Options.ES_HEURISTIC_INTERACTIVE)
		{
			ic = new InteractiveChoice("Workset interactive automaton selection");
		}
	}

	public void init_workset(boolean exclusive)
	{


		workset_count = size;	// all automata are enabled from start
		last_choice = -1; // invalid, yet

		for (int i = 0; i < size; i++)
		{
			workset[i] = 1;
		}

		if (exclusive)    // everything reamining, yet...
		{
			for (int i = 0; i < size; i++)
			{
				remaining[i] = true;
			}
		}

		ndas.reset();
	}

	public void done()
	{
		ndas.done();
	}

	// ---------------------------------------------------
	public String getHeuristicName()
	{
		return Options.ES_HEURISTIC_NAMES[Options.es_heuristics];
	}

	private int pickOneInteractive(boolean exclusive)
	{
		ic.removeAll();

		int queue_size = 0;

		for (int i = 0; i < size; i++)
		{
			if ((!exclusive || remaining[i]) && (workset[i] > 0))
			{
				ic.add(clusters[i].toString());

				queue[queue_size++] = i;
			}
		}

		ic.show();

		return queue[ic.getSelected()];
	}


	// ----[ The H1 heuristics are all here ] -------------------------------------
	/** let the user choose one */
	private int h1_interactive(boolean exclusive)
	{
		queue[0] = pickOneInteractive(exclusive);
		return 1;
	}

	/** pass ALL enabled clusters */
	private int h1_all(boolean exclusive)
	{
		int queue_size = 0;
		for (int i = 0; i < size; i++)
		{
			if (workset[i] > 0 && (!exclusive || remaining[i]) )
			{
				queue[queue_size++] = i;
			}
		}
		return queue_size;
	}


	/**
	 * choose the ones that share most number of events in their alphabet, compared
	 * to the last one
	 */

	private int h1_most_shared_events(boolean exclusive)
	{
		if(last_choice == -1)
		{
			return h1_all(exclusive);
		}

		int queue_size = 0;
		int best = 0;
		Cluster last = clusters[last_choice];

		for (int i = 0; i < size; i++)
		{
			if (workset[i] > 0 && (!exclusive || remaining[i]))
			{
				int added = last.sharedEvents(clusters[i]);
				if (best < added )
				{
					best = added;
					queue_size = 0;
				}

				if (added == best)
				{
					queue[queue_size++] = i;
				}
			}
		}
		return queue_size;

	}

	/**
	 * choose the ones that have least number of "new" events in their alphabet, compared
	 * to the last one
	 */
	private int h1_least_additional_events(boolean exclusive) {
		if(last_choice == -1)
		{
			return h1_all(exclusive);
		}

		int queue_size = 0;
		int best = Integer.MAX_VALUE;
		Cluster last = clusters[last_choice];

		for (int i = 0; i < size; i++)
		{
			if (workset[i] > 0 && (!exclusive || remaining[i]) )
			{
				int added = last.additionalEvents(clusters[i]);
				if (best > added )
				{
					best = added;
					queue_size = 0;
				}

				if (added == best)
				{
					queue[queue_size++] = i;
				}
			}
		}
		return queue_size;
	}

	/** return the first cluster in the given order */
	private int h1_topdown(boolean reverse, boolean exclusive)
	{
		for (int i = 0; i < size; i++)
		{
			int index = reverse ? (size - i -1) : i;
			if (workset[index] > 0 && (!exclusive || remaining[index]) )
			{
				queue[0] = index;
				return 1;
			}
		}
		return 0; // will not happen
	}


	/** one of the largest requested (most affected so far) */
	private int h1_most_pending(boolean reverse, boolean exclusive)
	{
		int best = reverse ? Integer.MAX_VALUE : 0;
		int queue_size = 0;

		for (int i = 0; i < size; i++)
		{
			if (workset[i] > 0  && (!exclusive || remaining[i]) )
			{
				if ( (!reverse && (best < workset[i])) ||  (reverse && (best > workset[i])) )
				{
					best = workset[i];
					queue_size = 0;
				}

				if (best == workset[i])
				{
					queue[queue_size++] = i;
				}
			}
		}
		return queue_size;
	}


	/** the one with the largest automata dependent on */
	private int h1_most_followers(boolean reverse, boolean exclusive)
	{
		int queue_size = 0;
		int best = reverse ? Integer.MAX_VALUE : 0;

		for (int i = 0; i < size; i++)
		{
			if (workset[i] > 0 && (!exclusive || remaining[i]) )
			{
				int c = dependent[i][0];

				if ( (!reverse && (best < c)) || (reverse && (best > c)) )
				{
					best = c;
					queue_size = 0;
				}

				if (best == c)
				{
					queue[queue_size++] = i;
				}
			}
		}
		return queue_size;
	}

	/** and here is where they are called from: */

	private int h1_heuristic(int h1, boolean exclusive)
	{
			switch (h1)
			{
			case Options.ES_HEURISTIC_INTERACTIVE :
				return h1_interactive(exclusive);
			case Options.ES_HEURISTIC_ANY:
				return h1_all(exclusive);
			case Options.ES_MOST_SHARED_EVENTS:
				return h1_most_shared_events(exclusive);
			case Options.ES_LEAST_ADDITIONAL_EVENTS:
				return h1_least_additional_events(exclusive);
			case Options.ES_HEURISTIC_TOPDOWN :
				return h1_topdown(false, exclusive);
			case Options.ES_HEURISTIC_BOTTOMUP :
				return h1_topdown(true, exclusive);
			case Options.ES_HEURISTIC_MOST_PENDING :
				return h1_most_pending(false, exclusive);
			case Options.ES_HEURISTIC_LEAST_PENDING :
				return h1_most_pending(true, exclusive);
			case Options.ES_HEURISTIC_MOST_MEMBERS :
				return h1_most_followers(false, exclusive);
			case Options.ES_HEURISTIC_LEAST_MEMBERS :
				return h1_most_followers(true, exclusive);
			default:
				System.err.println("unknown h1 heuristic: " + h1);
				return 0; // ERROR
			}
	}
	// ----------------------------------------------------
	/**
	 * choose the next automaton
	 */
	public int pickOne()
	{
		int queue_size = h1_heuristic(heuristic, false);
		Options.out.println("DEBUG: took " + queue_size + " out of " + workset_count);
		last_choice = ndas.choose(queue, queue_size);
		return last_choice;
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
		int queue_size = h1_heuristic(heuristic, true);
		last_choice = ndas.choose(queue, queue_size);
		remaining[last_choice] = false;

		return last_choice;
	}

	// --------------------------------------------------------------------------------

	/**
	 * we how worked but we may not be DONE with this automaton yet (no fixpoint reached?).
	 * if changed is new, then something has changed and we should consider the
	 * affect of this by adding automata that are directly connected with our automaton
	 */
	public void nonfixpoint_advance(int automaton, boolean changed)
	{
		if(changed)
		{
			record_change(automaton);
		}
		else
		{
			workset[automaton] = 0;
			workset_count--;
		}

		ndas.advance(automaton, changed);
	}

	/**
	 * we are done with this automaton.
	 * if changed is new, then something has changed and we should consider the
	 * affect of this by adding automata that are directly connected with our automaton
	 */
	public void advance(int automaton, boolean changed)
	{
		workset[automaton] = 0;
		workset_count--;
		if(changed)
		{
			record_change(automaton);
		}

		ndas.advance(automaton, changed);
	}


	// a change in "automaton" was seen, track the consequences by adding the dependent automata to the workset
	private void record_change(int automaton)
	{
		int count = dependent[automaton][0];

		// for(int i = 1 ; i <= count; i++) workset[  dependent[automaton][i] ] ++;
		// workset_count += count;
		for (int i = 1; i <= count; i++)
		{
			int a = dependent[automaton][i];

			if (workset[a] == 0)
			{
				workset_count++;
			}

			workset[a]++;
		}
	}

	public boolean empty()
	{
		return workset_count <= 0;
	}
}
