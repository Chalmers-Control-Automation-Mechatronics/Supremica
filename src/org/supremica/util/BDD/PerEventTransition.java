

package org.supremica.util.BDD;

/**
 * This is the set of all transitions for a single event, that is, if the event is 'my_event'
 * T(q, q') = { <q,q'> |  delta(q, myevent) = q' }
 *
 */


public class PerEventTransition {
	private BDDAutomata manager;
	private BDDAutomaton [] all;
	private int bdd_t, bdd_keep;
	private int event_index, members, next_events, prev_events;
	private int others_s2sp, others_sp2s;
	private boolean [] automata_cover;
	private int [] next_event, prev_event;
	private String name;

	PerEventTransition(BDDAutomata manager, BDDAutomaton [] all, int event, boolean [] careset) {
		this.manager = manager;
		this.event_index = event;
		this.all = all;

		EventManager alphabet = manager.getEventManager();
		Event [] all_events = alphabet.getEventVector();
		Event theEvent= all_events[event];


		name = theEvent.label;

		int ec = all_events.length;
		int ac = all.length;


		int tmp = theEvent.getBDD();	manager.ref(tmp);
		bdd_keep = manager.getOne();	manager.ref(bdd_keep);

		automata_cover = new boolean[ac];
		next_event = new int[ec];
		prev_event = new int[ec];
		for(int i = 0; i < ec; i++) next_event[i] = prev_event[i] = 0;


		IntArray ia1 = new IntArray (),  ia2 = new IntArray();

		members = 0;
		for(int j = 0; j < ac; j++) {
			boolean [] care = all[j].getEventCareSet(false);
			if(care[event]) {
				automata_cover[j] = true;
				members ++;

				// update T
				tmp = manager.andTo(tmp, all[j].getTpri() );

				// get flow
				boolean [] f1 = all[j].getEventFlow(true);
				boolean [] f2 = all[j].getEventFlow(false);
				for(int i = 0; i < ec; i++) {
					if(f1[i]) next_event[i]++;
					if(f2[i]) prev_event[i]++;
				}

			} else {
				automata_cover[j] = false;
				bdd_keep = manager.andTo(bdd_keep, all[j].getKeep() );

				// need its events for our special permutation
				int [] var = all[j].getVar(), varp = all[j].getVarp();
				int len = var.length;
				for(int i = 0; i < len; i++) {
					ia1.add(var[i]);
					ia2.add(varp[i]);
				}
			}
		}

		bdd_t = manager.exists(tmp, manager.getEventCube() );
		manager.deref(tmp);

		// permutation that does the same job as "keep"
		int [] others_s = ia1.copy(), others_sp = ia2.copy();
		others_s2sp = manager.createPair(others_s, others_sp);
		others_sp2s = manager.createPair(others_sp, others_s);


		if(careset != null) {
			for(int i = 0; i < ec; i++)
				if(!careset[i]) next_event[i] = prev_event[i] = 0;
		}

		next_events = ec - Util.countEQ(next_event, 0);
		prev_events = ec - Util.countEQ(prev_event, 0);
	}

	public void cleanup() {
		manager.deletePair(others_s2sp);
		manager.deletePair(others_sp2s);

		manager.deref(bdd_t);
		manager.deref(bdd_keep);
	}

	public int getNumberOfAutomata() { return members; }
	public int getNumberOfNextEvents() { return next_events; }
	public int getNumberOfPrevEvents() { return prev_events; }

	public boolean [] getAutomatonCover() { return automata_cover; }
	public int [] getNextEventCount() { return next_event; }
	public int [] getPrevEventCount() { return prev_event; }

	public String toString () { return name; }

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
	public int forward_one(int front, int cube_s) {
		int front2 = manager.replace(front, others_s2sp);
		int tmp3 = manager.relProd( front2, bdd_t, cube_s);
		manager.deref(front2);
		return tmp3;
	}

	/** returns Reachables(qi, {my_sigma} ) */
	public int forward(int qi, int cube_s, int permute_sp2s) {
		int ret_old, ret = qi; manager.ref(ret);
		int front = manager.replace(ret, others_s2sp);

		do {
			ret_old = ret;
			int tmp1 = manager.relProd( front, bdd_t, cube_s);
			manager.deref(front);

			int tmp2 = manager.replace(tmp1, permute_sp2s);
			manager.deref(tmp1);

			ret = manager.orTo(ret, tmp2);

			if(ret_old != ret) // skip this if we are done anyway
				front = manager.replace(tmp2, others_s2sp);

			manager.deref(tmp2);

		} while(ret_old != ret);
		return ret;
	}

	/** returns CoReachables(qm', {my_sigma} ) */
	public int backward(int qm, int cube_sp, int permute_s2sp) {
		int ret_old, ret = qm; manager.ref(ret);
		int front = manager.replace(ret, others_sp2s);

		do {
			ret_old = ret;
			int tmp1 = manager.relProd( front, bdd_t, cube_sp);
			manager.deref(front);

			int tmp2 = manager.replace(tmp1, permute_s2sp);
			manager.deref(tmp1);

			ret = manager.orTo(ret, tmp2);

			if(ret_old != ret) // skip this if we are done anyway
				front = manager.replace(tmp2, others_sp2s);

			manager.deref(tmp2);

		} while(ret_old != ret);
		return ret;
	}
}
