

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;
import org.supremica.util.BDD.solvers.*;

import java.util.*;

/**
 * Non-deteministic (?) choice of equally good candidates
 */

public class NDAS_Choice {
	private final int
		ADD_REWARD = +2,
		ADD_PUNISH = -1,	// tabu search magic number?
		ADD_REWARD_FADING = +5;



	/** ring is used in the ring choice to remember the last one and pick one higher */
	private int ndas, ring, max_activity, num_access, num_advance;
	private int [] activity = null, queue2 = null;
	private boolean punish_inactive;


	public NDAS_Choice(int size) {
		ndas = Options.ndas_heuristics;
		max_activity = size;

		if(ndas == Options.NDAS_RL || ndas == Options.NDAS_RL_TABU) {
			// we handle NDAS_ACTIVITY and NDAS_ACTIVITY2 equal beside the small different in advance()
			if(ndas == Options.NDAS_RL_TABU) {
				punish_inactive = true;
				ndas = Options.NDAS_RL;
			} else punish_inactive = false;

			activity = new int[size];
			queue2 = new int[size];

		}
	}


	public void reset() {
		ring = -1;
		num_access =  num_advance = 0;
		if(activity != null) for(int i = 0; i < activity.length; i++) activity[i] = 0;
	}

	public void done() {

		if(Options.profile_on && num_access != 0) {
			Options.out.println("NDAS advances: " + ( (100 * num_advance) / num_access) + "%");
		}

	}
	// --------------------------------------------------------------------------

	public int choose(int [] queue, int size) {
		if(size <= 0) return -1; // ERROR, no choices!

		if(size == 1) return queue[0]; // XXX: this will skip any extra herusitic code below!

		switch(ndas) {
			case Options.NDAS_FIRST:	return queue[0];
			case Options.NDAS_LAST: 	return queue[size-1];
			case Options.NDAS_RANDOM:	return queue[ (int)(Math.random() * size) ];
			// case Options.NDAS_RING:		return queue[ ring++ % size ];
			case Options.NDAS_RING:		return ring_choice(queue, size);
			case Options.NDAS_RL:       return find_best_active(queue, size);
		}

		return -1; // ERROR: unknown option
	}

	public void advance(int automaton, boolean changed)	{

		num_access++;
		if(changed) num_advance++;

		if(ndas == Options.NDAS_RL) {
			if(punish_inactive) {
				// reward and punish
				activity[automaton] += (changed) ? ADD_REWARD : ADD_PUNISH;

				// dont let it grow more than we can handle...
				if(activity[automaton] > max_activity) activity[automaton] = max_activity;
				else if(activity[automaton] < -max_activity) activity[automaton] = -max_activity;

			} else {
				// reward and fade
				for(int i = 0; i < activity.length; i++) activity[i] /= 2;
				if(changed) activity[automaton] += ADD_REWARD_FADING;
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

	// ---------------------------------------------------

	/**
	 * ring choice stuff:
	 * try to choose "next" automata in a particular order (definition order)
	 */
	private int ring_choice(int [] queue, int size) {
		ring  = pick_larger_than(queue, size, ring);
		if(ring == -1) { // no smaller, start from the beginning
			ring = pick_smallest(queue, size);
		}
		return ring;
	}


	/**
	 * return the smallest number larger than "last", -1 if no such
	 */
	private int pick_larger_than(int []queue, int size, int last) {
		int choice = -1;
		for(int i =0; i < size; i++) {
			if(queue[i] > last) {
				if(choice == -1 || choice > queue[i])
					choice = queue[i];
			}
		}
		return choice;
	}

	/** return the firs (smallest) number in the queue. queue may NOT be empty ! */
	private int pick_smallest(int []queue, int size) {
		int choice = queue[0];
		for(int i = 1; i < size; i++) {
			if(choice > queue[i] ) {
				choice = queue[i];
			}
		}
		return choice;
	}

}


