package org.supremica.util.BDD;

/**
 * This is the set of all transitions for a single event, that is, if the event is 'my_event'
 * T(q, q') = { <q,q'> |  delta(q, myevent) = q' }
 *
 */
public class PerEventTransition
{
	private BDDAutomata manager;
	private int bdd_t, bdd_keep; /** the generated T and keep BDDs */
	private int members, next_events, prev_events, events_used;
	private int others_s2sp, others_sp2s;
	private boolean[] automata_cover;
	private int[] next_event, prev_event;
	private int[] event_list; /** list of events associated with this transition, usually a singlton */
	private String name;


	// -----------------------------------------------------------------------------------

	/**
	 * create a transition model as a combination of two previously created transitions
	 */

	/* package */ PerEventTransition(PerEventTransition p1, PerEventTransition p2, BDDAutomaton[] all) {
		// TODO: assert if p1 != p2 and if the managers are the same!

		this.manager = p1.manager;
		this.events_used = p1.events_used + p2.events_used;


		this.name = p1.name + "+" + p2.name;


		int ac = all.length;
		int ec = p1.next_event.length;

		automata_cover = new boolean[ ac];
		next_event = new int[ec];
		prev_event = new int[ec];

		for (int i = 0; i < ec; i++)
		{
			next_event[i] = prev_event[i] = 0;
		}


		// get the combined lists of events:
		int idx = 0;
		event_list = new int[events_used];
		for(int i = 0; i < p1.events_used; i++)  event_list[idx++] = p1.event_list[i];
		for(int i = 0; i < p2.events_used; i++)  event_list[idx++] = p2.event_list[i];



		IntArray ia1 = new IntArray(), ia2 = new IntArray();

		// get ready...
		this.members = 0;
		this.bdd_keep = manager.ref( manager.getOne() );
		int k1 = manager.ref( manager.getOne() ); // p1 stay, p2 moves
		int k2 = manager.ref( manager.getOne() ); // p2 stay, p1 moves

		// and search the automata list for things that concerns either p1 or p2
		for(int i = 0; i < ac; i++) {
			automata_cover [i] = p1.automata_cover[i] | p2.automata_cover[i];

			if(automata_cover[i]) {
				members++;


				// get flow for this automata
				boolean[] f1 = all[i].getEventFlow(true);
				boolean[] f2 = all[i].getEventFlow(false);


				for (int j = 0; j < ec; j++)
				{
					if (f1[j])
					{
						next_event[j]++;
					}

					if (f2[j])
					{
						prev_event[j]++;
					}
				}


				// see if one is moving but the other one is not:
				if(!p1.automata_cover[i]) k1 = manager.andTo(k1, all[i].getKeep());
				if(!p2.automata_cover[i]) k2 = manager.andTo(k2, all[i].getKeep());

			} else {

				bdd_keep = manager.andTo(bdd_keep, all[i].getKeep());

				// need its events for our special permutation
				int[] var = all[i].getVar(), varp = all[i].getVarp();
				int len = var.length;

				for (int j = 0; j < len; j++)
				{
					ia1.add(var[j]);
					ia2.add(varp[j]);
				}
			}
		}

		// the new T works like this: (p1 moves OR p2 moves)
		// but this means that p1 must add keep w.r.t. p2 and vice versa!

		int t1 = manager.and(p1.bdd_t, k1);
		int t2 = manager.and(p2.bdd_t, k2);
		manager.deref(k1);
		manager.deref(k2);

		this.bdd_t = manager.or(t1, t2);
		manager.deref(t1);
		manager.deref(t2);



		// permutation that does the same job as "keep"
		int[] others_s = ia1.copy(), others_sp = ia2.copy();

		others_s2sp = manager.createPair(others_s, others_sp);
		others_sp2s = manager.createPair(others_sp, others_s);


		next_events = ec - Util.countEQ(next_event, 0);
		prev_events = ec - Util.countEQ(prev_event, 0);

		/*
		// DEBUG:
		p1.dump();
		p2.dump();
		Options.out.println("GIVES ==>");
		this.dump();
		Options.out.println();
		*/
	}



	// -----------------------------------------------------------------------------------


	/**
	 * create a transition for a single event
	 */

	/* package */ PerEventTransition(BDDAutomata manager, BDDAutomaton[] all, int event, boolean[] careset)
	{
		this.manager = manager;
		this.events_used = 1;

		// save the event we are working with
		this.event_list = new int[1];
		this.event_list[0] = event;


		EventManager alphabet = manager.getEventManager();
		Event[] all_events = alphabet.getEventVector();
		Event theEvent = all_events[event];

		name = theEvent.label;

		int ec = all_events.length;
		int ac = all.length;
		int tmp = theEvent.getBDD();

		manager.ref(tmp);

		bdd_keep = manager.getOne();

		manager.ref(bdd_keep);

		automata_cover = new boolean[ac];
		next_event = new int[ec];
		prev_event = new int[ec];

		for (int i = 0; i < ec; i++)
		{
			next_event[i] = prev_event[i] = 0;
		}

		IntArray ia1 = new IntArray(), ia2 = new IntArray();

		members = 0;

		for (int j = 0; j < ac; j++)
		{
			boolean[] care = all[j].getEventCareSet(false);

			if (care[event])
			{
				automata_cover[j] = true;

				members++;

				// update T
				tmp = manager.andTo(tmp, all[j].getTpri());

				// get flow
				boolean[] f1 = all[j].getEventFlow(true);
				boolean[] f2 = all[j].getEventFlow(false);

				for (int i = 0; i < ec; i++)
				{
					if (f1[i])
					{
						next_event[i]++;
					}

					if (f2[i])
					{
						prev_event[i]++;
					}
				}
			}
			else
			{
				automata_cover[j] = false;
				bdd_keep = manager.andTo(bdd_keep, all[j].getKeep());

				// need its events for our special permutation
				int[] var = all[j].getVar(), varp = all[j].getVarp();
				int len = var.length;

				for (int i = 0; i < len; i++)
				{
					ia1.add(var[i]);
					ia2.add(varp[i]);
				}
			}
		}

		bdd_t = manager.exists(tmp, manager.getEventCube());

		manager.deref(tmp);

		// permutation that does the same job as "keep"
		int[] others_s = ia1.copy(), others_sp = ia2.copy();

		others_s2sp = manager.createPair(others_s, others_sp);
		others_sp2s = manager.createPair(others_sp, others_s);

		if (careset != null)
		{
			for (int i = 0; i < ec; i++)
			{
				if (!careset[i])
				{
					next_event[i] = prev_event[i] = 0;
				}
			}
		}

		next_events = ec - Util.countEQ(next_event, 0);
		prev_events = ec - Util.countEQ(prev_event, 0);
	}

	public void cleanup()
	{
		manager.deletePair(others_s2sp);
		manager.deletePair(others_sp2s);
		manager.deref(bdd_t);
		manager.deref(bdd_keep);
	}

	// ----------------------------------------------------------

	/**
	 * This is the BDD for the local T, without the keep
	 */
	public int getLocalT()
	{
		return bdd_t;
	}

	/**
	 * This is the keep constraint that completes the local T
	 */
	public int getKeep()
	{
		return bdd_keep;
	}

	/**
	 * number of automata involved in this local transition
	 */
	public int getNumberOfAutomata()
	{
		return members;
	}

	public int getNumberOfNextEvents()
	{
		return next_events;
	}

	public int getNumberOfPrevEvents()
	{
		return prev_events;
	}

	public boolean[] getAutomatonCover()
	{
		return automata_cover;
	}

	public int[] getNextEventCount()
	{
		return next_event;
	}

	public int[] getPrevEventCount()
	{
		return prev_event;
	}

	public String toString()
	{
		return name;
	}

	// ---------------------------------------------------------------------------
	// reachability stuff:
	// not that we do another type of Image computation here.
	// I dont know if its is more efficient, but we use permutation S --> S'
	// instead of keep.
	// If this is more efficient, it should be introduced to the other algos,
	// such as smoothing, too.
	//
	// It is also possible that it is more efficient for forward-reachability but
	// slower for backward-reachability since in the latter case we push BDDs upwards.
	// I assume I must analyze this more carefully someday.

	/** returns { q' | E q in front. \delta(q, my_sigma) = q' } */
	public int forward_one(int front, int cube_s)
	{
		int front2 = manager.replace(front, others_s2sp);
		int tmp3 = manager.relProd(front2, bdd_t, cube_s);

		manager.deref(front2);

		return tmp3;
	}

	/** returns Reachables(qi, {my_sigma} ) */
	public int forward(int qi, int cube_s, int permute_sp2s)
	{
		int ret_old, ret = qi;

		manager.ref(ret);

		int front = manager.replace(ret, others_s2sp);

		do
		{
			ret_old = ret;

			int tmp1 = manager.relProd(front, bdd_t, cube_s);

			manager.deref(front);

			int tmp2 = manager.replace(tmp1, permute_sp2s);

			manager.deref(tmp1);

			ret = manager.orTo(ret, tmp2);

			if (ret_old != ret)    // skip this if we are done anyway
			{
				front = manager.replace(tmp2, others_s2sp);
			}

			manager.deref(tmp2);
		}
		while (ret_old != ret);

		return ret;
	}

	/** returns CoReachables(qm', {my_sigma} ) */
	public int backward(int qm, int cube_sp, int permute_s2sp)
	{
		int ret_old, ret = qm;

		manager.ref(ret);

		int front = manager.replace(ret, others_sp2s);

		do
		{
			ret_old = ret;

			int tmp1 = manager.relProd(front, bdd_t, cube_sp);

			manager.deref(front);

			int tmp2 = manager.replace(tmp1, permute_s2sp);

			manager.deref(tmp1);

			ret = manager.orTo(ret, tmp2);

			if (ret_old != ret)    // skip this if we are done anyway
			{
				front = manager.replace(tmp2, others_sp2s);
			}

			manager.deref(tmp2);
		}
		while (ret_old != ret);

		return ret;
	}


	// -----------------------------------------------------------------------------------
	/**
	 * call this when the evets has been modified, for example renamed by joining
	 * multiple transitions into a new one.
	 *
	 *<p>perm: N -> 0 ...new_size-1 (where N is an old transition)
	 *
	 * this must be called after a join
	 */

	/* package */ void renameEvent(int [] perm, int new_size)
	{
		int []tmp1 = new int[new_size];
		int []tmp2 = new int[new_size];

		// start clean
		for(int i = 0; i < new_size; i++)
		{
			tmp1[i] = tmp2[i] = 0;
		}

		// ... and fil it up
		for(int i = 0; i < next_event.length; i++)
		{
			tmp1[ perm[i] ] += next_event[i];
			tmp2[ perm[i] ] += prev_event[i];
		}

		next_event = tmp1;
		prev_event = tmp2;

		// XXX: do not touch the event_list! that the original and must be kept intact!
	}

	/**
	 * fill my changes. used for renaming
	 * @see #renameEvent
	 */
	/* package */ void fillPerm(int [] perm, int my_id)
	{
		for(int i = 0; i < event_list.length; i++)
		{
			perm[ event_list[i] ] = my_id;
		}
	}

	// ------------------------------------------------------------------------------------
	public void dump()
	{
		Options.out.println("Transition system " + name + ", size=" + members + ", events="+events_used);

		Options.out.print("Automata cover:");
		for(int i = 0; i < automata_cover.length; i++) if(automata_cover[i]) Options.out.print(" " + i);
		Options.out.println();

		Options.out.print("Event-list:");
		for(int i = 0; i < event_list.length; i++) Options.out.print(" " + event_list[i]);
		Options.out.println();


		Options.out.print("" + next_events + " next-events:");
		for(int i = 0; i < next_event.length; i++) if(next_event[i] > 0) Options.out.print(" " + i + ":" + next_event[i]);
		Options.out.println();

		Options.out.print("" + prev_events + " prev-events:");
		for(int i = 0; i < prev_event.length; i++) if(prev_event[i] > 0) Options.out.print(" " + i + ":" + prev_event[i]);
		Options.out.println();

		Options.out.println();
	}

}
