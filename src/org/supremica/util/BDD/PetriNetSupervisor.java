package org.supremica.util.BDD;



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

	private int size, heuristic; /** Number of events */
	private PerEventTransition [] pet; /** one 'transition' system per event, see comments on the top */
	private int [] workset, queue; /** workset as above, queue is the set of suggested events, see algo again */
	private InteractiveChoice ic = null;

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

		heuristic = Options.es_heuristics;

		if(heuristic == Options.ES_HEURISTIC_INTERACTIVE) {
			ic = new InteractiveChoice("Petri net interactive event selection");
		}
	}

    // -----------------------------------------------------------------
	/** the evil cleanup functions that of you dont call, will come back and kill you children */
    public void cleanup() {
		for(int i = 0; i < size; i++)	pet[i].cleanup();
		super.cleanup();
	}


	/**
	 * The user os always right, so lets ask him :)
	 * as usual, it returns -1  if no more events are found...
	 */
	private int pickOneInteractive(boolean forward) {
		ic.removeAll();

		int queue_size = 0;
		for(int i = 0; i < size; i++)
			if(workset[i] > 0 ) {
				ic.add( pet[i].toString() );
				queue[queue_size++] = i;
			}

		if(queue_size == 0) return -1;
		ic.show();
		return queue[ ic.getSelected() ];
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

		switch(heuristic) {
			case Options.ES_HEURISTIC_INTERACTIVE:
				return pickOneInteractive(forward);

			case Options.ES_HEURISTIC_ANY:
				for(int i = 0; i < size; i++) // anything is ok
					if(workset[i] > 0)  queue[queue_size++] = i;
				break;

			case Options.ES_HEURISTIC_TOPDOWN:
				for(int i = 0; i < size; i++)
					if(workset[i] > 0)  return i;
				break;

			case Options.ES_HEURISTIC_BOTTOMUP:
				for(int i = size-1; i >= 0; i--)
					if(workset[i] > 0)  return i;
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

		GrowFrame gf = BDDGrow.getGrowFrame(manager,"Forward reachability" + type());

		int r_all_p, r_all = i_all;
		manager.ref(r_all);

		for(;;) {
			r_all_p = r_all;

			int i = pick_one_event(true /* forward direction */);
			if(i == -1) break;

			int tmp3 = pet[i].forward(r_all, s_cube, perm_sp2s);
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

		GrowFrame gf = BDDGrow.getGrowFrame(manager,"Backward reachability" + type());

		int r_all_p, r_all = manager.replace(q_m, perm_s2sp);
		manager.ref(r_all);

		for(;;) {
			r_all_p = r_all;

			int i = pick_one_event(false /* backward direction */);
			if(i == -1) break;

			int tmp3 = pet[i].backward(r_all, sp_cube, perm_s2sp);
			r_all = manager.orTo(r_all, tmp3);
			manager.deref(tmp3);

			if(r_all != r_all_p) { // something changed, update the workset
				int [] next = pet[i].getPrevEventCount();
				for(int j = 0; j < size; j++) workset[j] += next[j];
			}
			workset[i] = 0; // we are done with ourselves
			if(gf != null) gf.add( r_all );
		}

		int ret = manager.replace(r_all,perm_sp2s);
		manager.deref(r_all);

		if(gf != null) gf.stopTimer();
		return ret;
	}

}
