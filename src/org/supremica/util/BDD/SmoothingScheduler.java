
package org.supremica.util.BDD;

import java.util.*;

public class SmoothingScheduler {

	private int size;
	private BDDAutomata manager;
	private DisjPartition dp;

	public SmoothingScheduler(BDDAutomata manager, DisjPartition dp) {
		this.size    = dp.getNumberOfClusters();
		this.manager = manager;
		this.dp      = dp;

		create_transition_model();
	}

	private void create_transition_model() {
		/*
		Event[] es = manager.getEvents();
		int [] twave = dp.getClusters();
		int logical_zero = manager.getZero();


		for(int p = 0; p  < size; p++)  {
			for(int e = 0; e < es.length; e++) {
				int event = es[e].bdd;
				int event_used = manager.and( event, twave[p]);
				if(event_used != logical_zero) {
					// ok, event used. find the states that it will lead to:


				}
				manager.deref(event_used);
			}
		}
		*/

	}

	public int pop() {
		return 0;
	}
	public void push(int i) {
	}
	public boolean empty() {
		return true;
	}


}
