package org.supremica.util.BDD;


import java.util.*;

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

	private int size; /** Number of events */
	private PerEventTransition [] pet; /** one 'transition' system per event, see comments on the top */
	private int [] workset, queue; /** workset as above, queue is the set of suggested events, see algo again */

    /** Constructor, passes to the base-class */
    public PetriNetSupervisor(BDDAutomata manager, Group plant, Group spec) {
		super(manager, plant, spec);
		pn_init();
    }
    /** Constructor, passes to the base-class */
    public PetriNetSupervisor(BDDAutomata manager, BDDAutomaton[] automata) {
		super(manager, automata);
		pn_init();
    }

	/** initialize by building and pre-.computing/pre-allocating some  stuff */
	private void pn_init() {
		EventManager alphabet = manager.getEventManager();
		BDDAutomaton [] all = gh.getSortedList();
		Event [] events = alphabet.getEventVector();
		size = events.length;

		pet = new PerEventTransition[size];

		for(int i = 0; i < size; i++)
			pet[i] = new PerEventTransition(manager, all, i,  null);

		workset = new int[size];

		queue = new int[size]; // use by the heuristics
	}

    // -----------------------------------------------------------------
	/** the evil cleanup functions that of you dont call, will come back and kill you children */
    public void cleanup() {
		for(int i = 0; i < size; i++)	pet[i].cleanup();
		super.cleanup();
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
	private int pick_one_event(boolean forward) {
		int index = -1, best, queue_size = 0;

		// the idea is to put the 'best' events in a queue and choose one on random

		switch(Options.es_heuristics) {
			case Options.ES_HEURISTIC_RANDOM:
				for(int i = 0; i < size; i++) // anything is ok
					if(workset[i] > 0)  queue[queue_size++] = i;
				break;
			case Options.ES_HEURISTIC_MOST_PENDING: // largest usage
				best = 0;
				for(int i = 0; i < size; i++) {
					if(workset[i] > 0) {
						if(workset[i] > best) {
							best = workset[i];
							queue_size = 0;
						}
						if(best == workset[i]) queue[queue_size++] = i;
					}
				}
				break;
			case Options.ES_HEURISTIC_LEAST_PENDING: // smallest usage larger than zero
				best = Integer.MAX_VALUE;
				for(int i = 0; i < size; i++) {
					if(workset[i] > 0){
						if(workset[i] < best) {
							best = workset[i];
							queue_size = 0;
						}
						if(best == workset[i]) queue[queue_size++] = i;
					}
				}
				break;

			case Options.ES_HEURISTIC_MOST_FOLLOWERS:
				best = 0;
				for(int i = 0; i < size; i++) {
					if(workset[i] > 0) {
						int c = forward ? pet[i].getNumberOfNextEvents() : pet[i].getNumberOfPrevEvents();
						if(c > best) {
							best = c;
							queue_size = 0;
						}
						if(c == best) queue[queue_size++] = i;
					}
				}
			break;
			case Options.ES_HEURISTIC_LEAST_FOLLOWERS:
				best = Integer.MAX_VALUE;
				for(int i = 0; i < size; i++) {
					if(workset[i] > 0) {
						int c = forward ? pet[i].getNumberOfNextEvents() : pet[i].getNumberOfPrevEvents();
						if(c < best) {
							best = c;
							queue_size = 0;
						}
						if(c == best) queue[queue_size++] = i;
					}
				}
			break;

			case Options.ES_HEURISTIC_MOST_MEMBERS:
				best = 0;
				for(int i = 0; i < size; i++) {
					if(workset[i] > 0) {
						int c = pet[i].getNumberOfAutomata();
						if(c > best) {
							best = c;
							queue_size = 0;
						}
						if(c == best) queue[queue_size++] = i;
					}
				}

			break;
			case Options.ES_HEURISTIC_LEAST_MEMBERS:
				best = Integer.MAX_VALUE;
				for(int i = 0; i < size; i++) {
					if(workset[i] > 0) {
						int c = pet[i].getNumberOfAutomata();
						if(c < best) {
							best = c;
							queue_size = 0;
						}
						if(c == best) queue[queue_size++] = i;
					}
				}
			break;
		}

		return (queue_size > 0) ? queue[ (int)(Math.random() * queue_size)] : -1;
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
	private int internal_computeReachablesPN(int i_all) {

		// start with all events
		for(int i = 0; i < size; i++) workset[i] = 1;

		GrowFrame gf = BDDGrow.getGrowFrame(manager,
			"[PetriNetSupervisor] forward reachability /" + Options.ES_HEURISTIC_NAMES[Options.es_heuristics]);


		 int cube_s = manager.getStateCube();
		int sp2s = manager.getPermuteSp2S();

		int r_all_p, r_all = i_all;
		manager.ref(r_all);

		for(;;) {
			r_all_p = r_all;

			int i = pick_one_event(true /* forward direction */);
			if(i == -1) break;

			int tmp3 = pet[i].forward(r_all, cube_s, sp2s);
			r_all = manager.orTo(r_all, tmp3);
			manager.deref(tmp3);

			if(r_all != r_all_p) {
				int [] next = pet[i].getNextEventCount();
				for(int j = 0; j < size; j++) workset[j] += next[j];
			}
			workset[i] = 0; // we are done with ourselfs

			if(gf != null) gf.add( r_all );
		}


		if(gf != null) gf.stopTimer();
		return r_all;

	}


	// --------------------------------------------------------------------------------
	/** backwardreachability stub functions */
	protected void computeCoReachables()
	{
		int m_all = GroupHelper.getM(manager,spec, plant);
		bdd_coreachables = internal_computeCoReachablesPN(m_all);
		has_coreachables = true;
		manager.deref(m_all);
	}

	/** implementation of the backward reachability */
	private int internal_computeCoReachablesPN(int q_m)
	{
		for(int i = 0; i < size; i++) workset[i] = 1; 	// start with all events

		GrowFrame gf = BDDGrow.getGrowFrame(manager,
			"[PetriNetSupervisor] backward reachability /" + Options.ES_HEURISTIC_NAMES[Options.es_heuristics]);


		int cube_sp = manager.getStatepCube();

		int permute1 = manager.getPermuteS2Sp();
		int permute2 = manager.getPermuteSp2S();

		int r_all_p, r_all = manager.replace(q_m, permute1);
		manager.ref(r_all);

		for(;;) {
			r_all_p = r_all;

			int i = pick_one_event(false /* backward direction */);
			if(i == -1) break;

			int tmp3 = pet[i].backward(r_all, cube_sp, permute1);
			r_all = manager.orTo(r_all, tmp3);
			manager.deref(tmp3);

			if(r_all != r_all_p) { // something changed, update the workset
				int [] next = pet[i].getPrevEventCount();
				for(int j = 0; j < size; j++) workset[j] += next[j];
			}
			workset[i] = 0; // we are done with ourselves
			if(gf != null) gf.add( r_all );
		}

		int ret = manager.replace(r_all,permute2);
		manager.deref(r_all);

		if(gf != null) gf.stopTimer();
		return ret;
	}

}
