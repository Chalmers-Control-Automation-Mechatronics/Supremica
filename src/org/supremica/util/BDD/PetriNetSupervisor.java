
package org.supremica.util.BDD;


import org.supremica.util.BDD.heuristics.*;
import org.supremica.util.BDD.graphs.*;


/**
 * Petri net supervisor does reachability somehow like Petri Nets (surrprise!).
 * The basic algorithm is
 *
 *    workset := all transitions
 *    reachables := initial marking
 *    WHILE workset NOT EMPTY
 * [*]  i := choose and remove one transition from workset
 *      new_markings := t_i[reachables>
 *      reachables := reachables UNION new_markings
 *      IF reachables has chagned THEN
 *         workset := workset UNION {all transitions that "follow" transition t_i }
 *      END IF
 *    END WHILE
 *
 *
 * now, we have events instead of transition (i.e. not 100% correct  imitation of the algo above).
 * also, we have several heuristics for choosing 'i' (position marked with [*]).
 * each heuristic suggests a set of new events, of which one we will choose by RANDOM.
 *
 *
 * TODO:
 *   1. we need a fast random function!
 *   2. show that the randomness helps making the algo more efficient.
 *
 */
public class PetriNetSupervisor
	extends ConjSupervisor
{
	private int size, heuristic;

	/** Number of events */
	private PerEventTransition[] pet;

	/** one 'transition' system per event, see comments on the top */
	private int[] workset, queue;

	/** this is our H2 heuristc */
	private NDAS_Choice ndas;

	/** workset as above, queue is the set of suggested events, see algo again */
	private InteractiveChoice ic = null;

	/** H1 fill graph */
	private LevelGraph levelGraph = null;

	/** Constructor, passes to the base-class */
	public PetriNetSupervisor(BDDAutomata manager, Group plant, Group spec)
	{
		super(manager, plant, spec);

		pn_init();
	}

	/** Constructor, passes to the base-class */
	public PetriNetSupervisor(BDDAutomata manager, BDDAutomaton[] automata)
	{
		super(manager, automata);

		pn_init();
	}

	/** initialize by building and pre-.computing/pre-allocating some  stuff */
	private void pn_init()
	{
		EventManager alphabet = manager.getEventManager();
		BDDAutomaton[] all = gh.getSortedList();
		Event[] events = alphabet.getEventVector();

		size = events.length;
		pet = new PerEventTransition[size];

		SizeWatch.setOwner("PetriNetSupervisor");

		for (int i = 0; i < size; i++)
		{
			pet[i] = new PerEventTransition(manager, all, i, null);
			SizeWatch.report(pet[i].getLocalT() , pet[i].toString() );
		}

		// optimze it!
		pet = TransitionOptimizer.optimize(pet, manager, all);
		size = pet.length; // may have changed!




		workset = new int[size];
		queue = new int[size];    // use by the heuristics
		ndas = new NDAS_Choice(size); // use by the heuristics
		heuristic = Options.es_heuristics;

		if (heuristic == Options.ES_HEURISTIC_INTERACTIVE)
		{
			ic = new InteractiveChoice("Petri net interactive event selection");
		}
			else if(heuristic == Options.ES_ADAPTIVE)
		{
			// XXX: no adaptive heuristics exists here yet, lets switch to something that we know works most of the time:
			heuristic = Options.ES_HEURISTIC_MOST_MEMBERS;
		}

	}

	// -----------------------------------------------------------------

	/** the evil cleanup functions that of you dont call, will come back and kill you children */
	public void cleanup()
	{
		for (int i = 0; i < size; i++)
		{
			pet[i].cleanup();
		}

		super.cleanup();
	}

	/**
	 * The user os always right, so lets ask him :)
	 * as usual, it returns -1  if no more events are found...
	 */
	private int pickOneInteractive(boolean forward)
	{
		ic.removeAll();

		int queue_size = 0;

		for (int i = 0; i < size; i++)
		{
			if (workset[i] > 0)
			{
				ic.add(pet[i].toString());

				queue[queue_size++] = i;
			}
		}

		if (queue_size == 0)
		{
			return -1;
		}

		ic.setVisible(true);

		return queue[ic.getSelected()];
	}

	//---------------------------------------------------------------------------

	/**
	 *
	 * Automaton selection heuristics, this is where all the action takes place
	 *
	 * DESCRIPTION:
	 * we have a set of avialbale events (those i where workset[i] == true).
	 * we choose a set of events that give the highest score with our heuristics.
	 * we put those on the 'queue' and finally choose one RANDOMLY.
	 *
	 * if there are no good events, we return -1.
	 * this should only happen when "there exists no i s.t. workset[i] == true".
	 */
	private int pick_one_event(boolean forward)
	{
		int index = -1, best, queue_size = 0;

		if(levelGraph != null) {
			int c = 0;
			for (int i = 0; i < size; i++)
				if (workset[i] > 0 )
					c++;

			levelGraph.add_workset(c);
		}

		// the idea is to put the 'best' events in a queue and choose one on random
		switch (heuristic)
		{

		case Options.ES_HEURISTIC_INTERACTIVE :
			return pickOneInteractive(forward);

		case Options.ES_HEURISTIC_ANY :
			for (int i = 0; i < size; i++)    // anything is ok
			{
				if (workset[i] > 0)
				{
					queue[queue_size++] = i;
				}
			}
			break;

		case Options.ES_HEURISTIC_TOPDOWN :
			for (int i = 0; i < size; i++)
			{
				if (workset[i] > 0)
				{
					return i;
				}
			}
			break;

		case Options.ES_HEURISTIC_BOTTOMUP :
			for (int i = size - 1; i >= 0; i--)
			{
				if (workset[i] > 0)
				{
					return i;
				}
			}
			break;

		case Options.ES_HEURISTIC_MOST_PENDING :    // largest usage
			best = 0;

			for (int i = 0; i < size; i++)
			{
				if (workset[i] > 0)
				{
					if (workset[i] > best)
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
			break;

		case Options.ES_HEURISTIC_LEAST_PENDING :    // smallest usage larger than zero
			best = Integer.MAX_VALUE;

			for (int i = 0; i < size; i++)
			{
				if (workset[i] > 0)
				{
					if (workset[i] < best)
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
			break;

		case Options.ES_HEURISTIC_MOST_MEMBERS :
			best = 0;

			for (int i = 0; i < size; i++)
			{
				if (workset[i] > 0)
				{
					int c = pet[i].getNumberOfAutomata();

					if (c > best)
					{
						best = c;
						queue_size = 0;
					}

					if (c == best)
					{
						queue[queue_size++] = i;
					}
				}
			}
			break;

		case Options.ES_HEURISTIC_LEAST_MEMBERS :
			best = Integer.MAX_VALUE;

			for (int i = 0; i < size; i++)
			{
				if (workset[i] > 0)
				{
					int c = pet[i].getNumberOfAutomata();

					if (c < best)
					{
						best = c;
						queue_size = 0;
					}

					if (c == best)
					{
						queue[queue_size++] = i;
					}
				}
			}
			break;

		// These last two does not exactly work the same way their automata counterparts work.
		// So we chose something that seemed to work good enough
		case Options.ES_LEAST_ADDITIONAL_EVENTS :
			best = 0;

			for (int i = 0; i < size; i++)
			{
				if (workset[i] > 0)
				{
					int c = forward
							? pet[i].getNumberOfNextEvents()
							: pet[i].getNumberOfPrevEvents();

					if (c > best)
					{
						best = c;
						queue_size = 0;
					}

					if (c == best)
					{
						queue[queue_size++] = i;
					}
				}
			}
			break;

		case Options.ES_MOST_SHARED_EVENTS :
			best = Integer.MAX_VALUE;

			for (int i = 0; i < size; i++)
			{
				if (workset[i] > 0)
				{
					int c = forward
							? pet[i].getNumberOfNextEvents()
							: pet[i].getNumberOfPrevEvents();

					if (c < best)
					{
						best = c;
						queue_size = 0;
					}

					if (c == best)
					{
						queue[queue_size++] = i;
					}
				}
			}
			break;
		}


		return nd_pick(queue_size);
	}



	//---------------------------------------------------------------------------
	/* same as NDAS-choice or H2 in automata heuristics */
	private int nd_pick(int queue_size)
	{
		if(queue_size < 1) return -1;


		// return queue[(int) (Math.random() * queue_size)];
		return ndas.choose(queue, queue_size);
	}

	/* register changes in the same way as workset/H2 do */
	private void register_change(int trans, boolean changed, boolean forward)
	{


		if (changed)
		{
			// something changed, update the workset
			int [] next = forward ? pet[trans].getNextEventCount() : pet[trans].getPrevEventCount();
			for (int j = 0; j < size; j++)
			{
				workset[j] += next[j];
			}
		}
		workset[trans] = 0;    // we are done with ourselves

		// for heuristics, update score
		ndas.advance(trans, changed);
	}
	//---------------------------------------------------------------------------
	private void init_search()
	{

		// start with all enevts (transitions?) enabled
		for (int i = 0; i < size; i++)
		{
			workset[i] = 1;
		}


		if(Options.show_level_graph)
		{
			levelGraph = new LevelGraph(size);
		}

		// reset H2 heuristics
		ndas.reset(levelGraph);


	}
	private void stop_search()
	{
		// just report the NDAS statistics
		ndas.done();
	}
	//---------------------------------------------------------------------------

	/** forward reachability stub functions */
	protected void computeReachables()
	{
		int i_all = manager.and(plant.getI(), spec.getI());

		bdd_reachables = internal_computeReachablesPN(i_all);
		has_reachables = true;

		manager.deref(i_all);
	}

	/** implementation of the forward reachability */
	private int internal_computeReachablesPN(int i_all)
	{

		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability" + type());
		int r_all_p, r_all = i_all;

		if(Options.profile_on) {
			Options.out.println("PN_forward  Level-1 dep: " + getLevel1Dependency(true));
		}


		manager.ref(r_all);
		limit.reset();

		init_search();

		while (!limit.stopped())
		{
			r_all_p = r_all;

			int i = pick_one_event(true /* forward direction */);

			if (i == -1)
			{
				break;
			}

			int tmp3 = pet[i].forward(r_all, s_cube, perm_sp2s);
			r_all = manager.orTo(r_all, tmp3);
			manager.deref(tmp3);

			register_change(i, r_all != r_all_p, true);

			if (gf != null)
			{
				gf.add(r_all);
			}
		}

		if (gf != null)
		{
			gf.stopTimer();
		}

		stop_search();

		return r_all;
	}

	// --------------------------------------------------------------------------------

	/** backwardreachability stub functions */
	protected void computeCoReachables()
	{
		int m_all = GroupHelper.getM(manager, spec, plant);

		bdd_coreachables = internal_computeCoReachablesPN(m_all);
		has_coreachables = true;

		manager.deref(m_all);
	}

	/** implementation of the backward reachability */
	private int internal_computeCoReachablesPN(int q_m)
	{

		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Backward reachability" + type());
		int r_all_p, r_all = manager.replace(q_m, perm_s2sp);


		if(Options.profile_on) {
			Options.out.println("PN_backward Level-1 dep: " + getLevel1Dependency(false));
		}

		manager.ref(r_all);
		limit.reset();

		init_search();
		while (!limit.stopped())
		{
			r_all_p = r_all;

			int i = pick_one_event(false /* backward direction */);

			if (i == -1)
			{
				break;
			}

			int tmp3 = pet[i].backward(r_all, sp_cube, perm_s2sp);
			r_all = manager.orTo(r_all, tmp3);
			manager.deref(tmp3);


			register_change(i, r_all != r_all_p, false);

			if (gf != null)
			{
				gf.add(r_all);
			}
		}

		int ret = manager.replace(r_all, perm_sp2s);

		manager.deref(r_all);

		if (gf != null)
		{
			gf.stopTimer();
		}

		stop_search();

		return ret;
	}
	// -----------------------------------------------

	public DependencyData getLevel1Dependency(boolean go_forward) {
		DependencyData dd = new DependencyData();
		dd.fromPerEventTransitions(pet, size, go_forward);
		return dd;
	}
}
