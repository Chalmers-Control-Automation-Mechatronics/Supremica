

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;
import org.supremica.util.BDD.solvers.*;

import java.util.*;

/**
 * Non-deteministic (?) choice of equally good candidates
 */

public class NDAS_Choice {
	private int ndas, ring;
	private int [] activity, queue2;
	private boolean punish_inactive;
	public NDAS_Choice(int size) {
		ndas = Options.ndas_heuristics;
		ring = 0;

		if(ndas == Options.NDAS_ACTIVITY || ndas == Options.NDAS_ACTIVITY2) {
			// we handle NDAS_ACTIVITY and NDAS_ACTIVITY2 equal beside the small different in advance()
			if(ndas == Options.NDAS_ACTIVITY2) {
				punish_inactive = false;
				ndas = Options.NDAS_ACTIVITY;
			} else punish_inactive = true;

			activity = new int[size];
			queue2 = new int[size];
			for(int i = 0; i < size; i++) activity[i] = 0;
		}
	}


	public int choose(int [] queue, int size) {
		if(size <= 0) return -1; // ERROR, no choices!

		if(size == 1) return queue[0]; // XXX: this will skip any extra herusitic code below!

		switch(ndas) {
			case Options.NDAS_FIRST:	return queue[0];
			case Options.NDAS_LAST: 	return queue[size-1];
			case Options.NDAS_RANDOM:	return queue[ (int)(Math.random() * size) ];
			case Options.NDAS_RING:		return queue[ ring++ % size ];
			case Options.NDAS_ACTIVITY: return find_best_active(queue, size);

		}

		return -1; // ERROR: unknown option
	}


	public void advance(int automaton, boolean changed)	{
		if(ndas == Options.NDAS_ACTIVITY) {
			if(punish_inactive) {
				activity[automaton] += (changed) ? + 2 : -1;
			} else {
				for(int i = 0; i < activity.length; i++) activity[i] /= 2;
				if(changed) activity[automaton] += 5;
			}
		}
	}

	// ----------------------------------------

	private int find_best_active(int [] queue, int size) {
		int count = 0;
		int best = Integer.MIN_VALUE;
		for(int i = 0; i < size; i++) {
			int current = queue[i];
			if(activity[current] > best) { best = activity[current] ; count = 0; }
			if(activity[current] == best) queue2[count++] = current;
		}



		BDDAssert.internalCheck(count > 0, "internal error");

		return queue2[ (int)(Math.random() * count) ];
	}
}


