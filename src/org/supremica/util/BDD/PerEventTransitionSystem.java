
package org.supremica.util.BDD;


public class PerEventTransitionSystem {
	private BDDAutomata manager;
	private int size;
	private PerEventTransition [] pet;

	PerEventTransitionSystem(BDDAutomata manager, BDDAutomaton[] automaton, boolean [] events) {
		this.manager = manager;
		this.size = IndexedSet.cardinality(events);

		int ec = events.length;




		pet = new PerEventTransition[size];


		int offset = 0;
		for(int i = 0; i < ec; i++)
			if(events[i])
				pet[offset++] = new PerEventTransition(manager, automaton, i, null);
	}

	public void cleanup() {
		for(int i = 0; i < size; i++)
		pet[i].cleanup();
	}

	// ---------------------------------------------------------
	public int forward_reachability(int Q_i, GrowFrame gf) {
		int cube_sp = manager.getStatepCube();
		int cube_s = manager.getStateCube();
		int sp2s = manager.getPermuteSp2S();

		int r_all_p, r_all = Q_i, front = Q_i;
		manager.ref(r_all);
		manager.ref(front);

		do {
			r_all_p = r_all;

			int tmp2 = manager.getZero();  manager.ref(tmp2);
			for(int i = 0; i < size; i++) {
				int tmp3 = pet[i].forward_one(front, cube_s);
				tmp2 = manager.orTo(tmp2,  tmp3);
				manager.deref(tmp3);
			}

			manager.deref(front);
			front = manager.replace( tmp2, sp2s);
			manager.deref(tmp2);
			r_all = manager.orTo(r_all, front);

			if(gf != null) gf.add( r_all);

		} while (r_all_p != r_all);

		manager.deref(front);
		return r_all;

	}
}
