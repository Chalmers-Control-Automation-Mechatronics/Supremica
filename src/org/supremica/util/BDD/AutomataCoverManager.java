

package org.supremica.util.BDD;

import java.util.*;



public class AutomataCoverManager {
	private BDDAutomata manager;
	private PerEventTransitionSystem pets;
	private Vector covers;
	private boolean [] vector_cover;
	public AutomataCoverManager(BDDAutomata manager, BDDAutomaton []automata) {

		this.manager = manager;



		EventManager alphabet = manager.getEventManager();
		int events = alphabet.getSize();
		vector_cover = new boolean[events];
		for(int i = 0; i < events; i++) vector_cover[i] = false;

		covers = new Vector();
		int len = automata.length;


		for(int i = 0; i < len; i++) {
			AutomataCover ac = new AutomataCover(manager);
			ac.add(automata[i]);
			covers.add(ac);

			ac.queue_create();
			BDDAutomaton add = ac.queue_pop();

			// Options.out.print("Adding members to " +  automata[i].getName()  + ": ");
			for(int j = 0; j < 5 && add != null && ac.getSharedCount() != 0; j++) {
				// Options.out.print(add.getName() + " ");
				ac.add(add);
				add = ac.queue_pop();
			}
			// Options.out.println();

			ac.addLocalEventsCover(vector_cover);
		}

		// remove duplicates...


		IndexedSet.negate(vector_cover);
		alphabet.dumpSubset("events not included",vector_cover);
		pets = new PerEventTransitionSystem (manager, automata, vector_cover);
	}

	public void cleanup() {
	   for (Enumeration e = covers.elements() ; e.hasMoreElements() ;) {
		   AutomataCover ac = (AutomataCover)e.nextElement();
		   ac.cleanup();
		 }
		 pets.cleanup();
	}

	public void dump() {
		for (Enumeration e = covers.elements() ; e.hasMoreElements() ;) {
		   AutomataCover ac = (AutomataCover)e.nextElement();
		   ac.dump();
		 }
	}

	public int forward_reachability(int initial) {
		GrowFrame gf1 = BDDGrow.getGrowFrame(manager, "AutomataCoverManager.forward_reachability [global]");
		GrowFrame gf2 = BDDGrow.getGrowFrame(manager, "AutomataCoverManager.forward_reachability [local]");


		int ret = initial; manager.ref(ret);
		int front, new_, old = ret;

		do {
			old = ret;

			front = ret; manager.ref(front);
			new_ = manager.getZero();
			manager.ref(new_);

			for (Enumeration e = covers.elements() ; e.hasMoreElements() ;) {
				AutomataCover ac = (AutomataCover)e.nextElement();
				int tmp = ac.forward_reachability(front);
				new_ = manager.orTo(new_, tmp);
				manager.deref(tmp);

				if (gf2 != null)
				{
					gf2.add(front);
				}
			}


			// and for those who are not localy saturated yet:
			int tmp = pets.forward_reachability(front, gf2);
			new_ = manager.orTo(new_, tmp);
			manager.deref(tmp);

			ret = manager.orTo(ret, new_);
			manager.deref(front);
			front = new_;

			if (gf1 != null)
			{
				gf1.add(ret);
			}
		} while(old != ret);


		manager.deref(front);

		if (gf1 != null)
		{
			gf1.stopTimer();
			gf2.stopTimer();
		}

		return ret;
	}


}