

package org.supremica.automata.algorithms;

import org.supremica.util.BDD.*;


/**
 * Base class, for BDD language containment verification
 *
 */


 /**
  *
  * Important note about the local-event detection algorithm:
  *
  * if we are doing language inclusion, then we dont need to add additional Specs to the curent
  * search. this means that we must _not_ considers these automata dyring local-event detection.
  * for example if Sigma of p1,sp1,sp2 = {a}, {b}, {b} respectively. then the local events of
  * p1 || sp is {a,b}. since sp2 is not going to get added anyway, we ignore the presence
  * of event 'b' in it!
  *
  */

public abstract class BaseBDDLanguageInclusion {
	protected AutomataSynchronizerHelper.HelperData hd;
	protected org.supremica.automata.Automata theAutomata;

	protected boolean [] considred_events, /* events, */ changes, workset_events;
	protected BDDAutomata ba = null;
	protected BDDAutomaton [] all = null;

	protected Group L1 = null;
	protected Group L2 = null;
	protected boolean controllaibilty_test;
	protected Group work1 = null, work2 = null;



	// statistic stuff
	protected int num_specs_tested = 0;
	protected int num_syncs_done = 0;
	protected int max_sync_depth = 0;

	// local event stuff:
	/** true if an event is local, valid only after a call to register_automaton_addition() */
	protected boolean [] local_events, local_events_plants;

	/** temporary storage for local events */
	protected int [] current_event_usage, current_event_usage_plants;

	/** number of automata that use each event */
	protected int [] event_usage_count, event_usage_count_plants;

	/** how many local events we had last time. to detect if any new has been added */
	protected int local_events_found = 0, local_events_found_plants = 0;

	/** bd for the local events */
	protected int bdd_local_events, bdd_local_events_plants;

    /**
     * creates a verification object for LANGUAGE INCLUSION test.
     * depeding one the type of algorithm used, this call might take a while
     * <b>DONT FORGET TO CALL cleanup() AFTERWARDS!!!</b>
     * @see cleanup()
     */
 	public BaseBDDLanguageInclusion(org.supremica.automata.Automata selected,
			       org.supremica.automata.Automata unselected,
			       AutomataSynchronizerHelper.HelperData hd)
	throws Exception
    {
		this.hd     = hd;
		this.controllaibilty_test = false;
		theAutomata = new org.supremica.automata.Automata();
		theAutomata.addAutomata(selected);
		theAutomata.addAutomata(unselected);


		try {
			Builder bu = new Builder(theAutomata);
			ba = bu.getBDDAutomata();
			all = ba.getAutomataVector();

			L1 = new Group(ba, all, new AutomatonMembership(selected), "Selected");
			L2 = new Group(ba, all, new AutomatonMembership(unselected), "Unselected");

			init();
		} catch(Exception pass) {
			cleanup();
			throw pass;
		}
    }





    /**
     * creates a verification object for CONTROLLABILITY test.
     * depeding one the type of algorithm used, this call might take a while
     * <b>DONT FORGET TO CALL cleanup() AFTERWARDS!!!</b>
     * @see cleanup()
     */
	public BaseBDDLanguageInclusion(org.supremica.automata.Automata theAutomata,
		AutomataSynchronizerHelper.HelperData hd)
		throws Exception
		{
		this.hd     = hd;
		this.controllaibilty_test = true;
		this.theAutomata = theAutomata;

		try {
			Builder bu = new Builder(theAutomata);
			ba = bu.getBDDAutomata();
			all = ba.getAutomataVector();

			L1 = new Group(ba, all, new AutomatonTypeMembership(false), "Spec");
			L2 = new Group(ba, all, new AutomatonTypeMembership(true), "Plant");

			init();

		} catch(Exception pass) {
			cleanup();
			throw pass;
		}
	}

	// ----------------------------------------------------------------------------------

    /**
     * C++ style destructor.
     * <b>This function MUST be called before creating any new AutomataBDDVerifier obejcts</b>
     *
     */
    public void cleanup() {
		if(Options.profile_on)	showStatistics();

		if(bdd_local_events != -1) ba.deref(bdd_local_events);
		if(bdd_local_events_plants != -1) ba.deref(bdd_local_events_plants);

		if(work1 != null) work1.cleanup();
		if(work2 != null) work2.cleanup();
		if(L1 != null) L1.cleanup();
		if(L2 != null) L2.cleanup();
		if(ba != null) ba.cleanup();
    }

	// ----------------------------------------------------------------------------------
	/** constructors common init code */
	protected void init() {

		bdd_local_events = -1;
		bdd_local_events_plants = -1;

		// get our working sets
		work1 = new Group(ba, all.length, "work1");
		work2 = new Group(ba, all.length, "work2");

		// tell the Supervisor classes they shouldnt clean them up when they are done:
		work1.setCleanup(false);
		work2.setCleanup(false);


		// XXX: get the intersection of the considred events
		// XXX: *** I DONT KNOW IF THIS IS CORRECT!! ***
		considred_events = L2.getEventCareSet(controllaibilty_test);
		considred_events = Util.duplicate(considred_events); // must duplicate since we will change its value just below
		IndexedSet.intersection(L1.getEventCareSet(controllaibilty_test), considred_events, considred_events);


		// temporary vector for the current considred events in our current automata set
		// events = new boolean[considred_events.length];
		changes = new boolean[considred_events.length]; // and one for the changes
		workset_events = new boolean[considred_events.length]; // and one the current spec


		local_events = new boolean[considred_events.length];
		local_events_plants = new boolean[considred_events.length];

		current_event_usage = new int[considred_events.length];
		current_event_usage_plants = new int[considred_events.length];

		event_usage_count = new int[considred_events.length];
		event_usage_count_plants = new int[considred_events.length];

	}

	// --------------------------------------------------------------------------------------


	/**
	 * Modular controllability check
	 * @return TRUE if the system is controllable
	 */

	public boolean isControllable() {
		BDDAssert.internalCheck(controllaibilty_test, "INTERNAL ERROR, someone fucked up...");
		return passLanguageInclusion(); // almost same shity code...
	}


   /**
	 * Modular language inclusion check
	 * @return TRUE if the system a1 in a2 (eller var det tvärtom ?)
	 */
	public boolean passLanguageInclusion() {

		// temporary disable some stuff...
		int save_show_grow = Options.show_grow;
		Options.show_grow = Options.SHOW_GROW_NONE;


		BDDAutomaton[] l1 = L1.getMembers();
		int count = L1.getSize();
		boolean result = true;


		// IMPORTANT OPTIMIZATION:
		// only if [Sigma' - (SigmaL1 \cap SigmaL2)] is not empty we need to add from L1 too
		// this is not the case if we are doing a language inclusion test!
		AutomataConfiguration ac = new AutomataConfiguration (L1, L2, controllaibilty_test);

		init_local_event_detection();

		for(int i = 0; i < count; i++)
		{
			BDDAutomaton k =  l1[i];

			num_specs_tested++;
			int old_syncs = num_syncs_done;
			result = check(k, ac);

			int syncs_done = num_syncs_done - old_syncs;
			if(syncs_done > max_sync_depth) max_sync_depth = syncs_done;

			if(!result)
			{
				if(Options.debug_on) Options.out.println(k.getName() + " FAILED language containment test.\n");
				break;
			} else {
				if(Options.debug_on) Options.out.println(k.getName() + " passed language containment test.\n");
			}
		}


		// change back what we disabled:
		Options.show_grow = save_show_grow;

		return result;
	}

	// ------ [LOCAL event detection ] -------------------------------------------------------------

	/** intiailize event-counting related stuff for detecting local events */
	private void init_local_event_detection() {
		// clean up previous rounds shit
		if(bdd_local_events != -1) ba.deref(bdd_local_events);
		if(bdd_local_events_plants != -1) ba.deref(bdd_local_events_plants);

		bdd_local_events_plants = bdd_local_events = ba.getOne();
		ba.ref(bdd_local_events);
		ba.ref(bdd_local_events_plants);


		// update event_usage_count and event_usage_count_plants
		ba.getEventManager().getUsageCount(event_usage_count);

		if(!controllaibilty_test) { // include only plants, we dont need spec
			L1.removeEventUsage(event_usage_count);

			// now, both are the same
			for(int i = 0; i <  event_usage_count.length; i++)
				event_usage_count_plants[i] = event_usage_count[i];
		} else {
			// not equal, event_usage_count_plants is now less or equal to event_usage_count
			for(int i = 0; i <  event_usage_count.length; i++)
				event_usage_count_plants[i] = event_usage_count[i];
			L1.removeEventUsage(event_usage_count_plants);
		}



		// initialize local_events_found!! some events might not have been used at all!
		local_events_found = local_events_found_plants = 0;
		int len = event_usage_count.length;
		for(int i = 0; i < len; i++) {
			if(event_usage_count[i] == 0) local_events_found++;
			if(event_usage_count_plants[i] == 0) local_events_found_plants++;
		}
	}


	/**
	 * initialize the local-event detection algorithm, must be called first!
	 * sp is ALWAYS assumbed to be Spec!
	 */
	protected void initialize_event_usage(BDDAutomaton sp) {
		int len = event_usage_count.length;

		for(int i = 0; i < len; i++) {
			current_event_usage[i] = event_usage_count[i];
			current_event_usage_plants[i] = event_usage_count_plants[i];
		}

		// since sp is Spec, this wont affect current_event_usage
		if(controllaibilty_test) {
			// if language inclusion, we ignore Specs.
			// see init_local_event_detection() and the comments at the top
			sp.removeEventUsage(current_event_usage);
		}


	}

	/**
	 * a step in a local event computation algorithm.
	 * returns the number of local events after 'at' being added.
	 * also sets the 'local_events[]' member variable for further usage
	 */
	protected int register_automaton_addition(BDDAutomaton at) {
		at.removeEventUsage(current_event_usage);

		int len = current_event_usage.length;
		int count = 0;

		for(int i = 0; i < len; i++) {
			if(current_event_usage[i] < 1) {
				count++;
				local_events[i] = true;
			} else {
				local_events[i] = false;
			}
		}

		return count;
	}

	/**
	 * same as <tt>register_automaton_addition</tt>, but only for plants
	 * BDDAutomaton MUST BE A PLANT!!
	 * @see register_automaton_addition
	 */
	protected int register_plant_automaton_addition(BDDAutomaton at) {
		at.removeEventUsage(current_event_usage_plants);

		int len = current_event_usage_plants.length;
		int count = 0;

		for(int i = 0; i < len; i++) {
			if(current_event_usage_plants[i] < 1) {
				count++;
				local_events_plants[i] = true;
			} else {
				local_events_plants[i] = false;
			}
		}

		return count;
	}
	// -------------------------------------------------------------------------------


	/**
	 * we just added an automaton , see if this changed our local events ?
	 * returns true if new local events were found.
	 * take care of updating counter-vectors and the bdds
	 */
	private boolean handle_addition(BDDAutomaton at) {
		int local_events_current = register_automaton_addition(at);
		if(local_events_current > local_events_found ) {
			if(Options.debug_on) {
				Options.out.println("   LL " + (local_events_current - local_events_found) +
				" new local events found.");
				ba.getEventManager().dumpSubset("   LL Local events", local_events);
			}
			local_events_found  = local_events_current;

			// update its BDD:
			ba.deref(bdd_local_events);
			bdd_local_events = ba.getAlphabetSubsetAsBDD(local_events);
			return true;
		}
		return false;
	}


	/**
	 * we just added a PALNT automaton , see if this changed our plant-local events ?
	 * returns true if new plant-local events were found
	 * take care of updating counter-vectors and the bdds
	 */
	private boolean handle_plant_addition(BDDAutomaton at) {

		int local_events_plants_current = register_plant_automaton_addition(at);
		if( local_events_plants_current > local_events_found_plants) {
			if(Options.debug_on) {
				Options.out.println("   LL " + (local_events_plants_current - local_events_found_plants) +
				" new plant-local events found.");
				ba.getEventManager().dumpSubset("   LL plant-local events", local_events_plants);
			}
			local_events_found_plants  = local_events_plants_current;

			// update its BDD:
			ba.deref(bdd_local_events_plants);
			bdd_local_events_plants = ba.getAlphabetSubsetAsBDD(local_events_plants);
			return true;
		}

		return false;
	}



	/**
	 * check that by adding another automaton 'at', at least state in bdd_uc
	 * is reachable by local-events _only_.
	 */

	protected boolean try_local_reachability(Supervisor sup, BDDAutomaton at, boolean is_plant, int bdd_uc) {
		boolean changed = handle_addition(at);
		if(is_plant) handle_plant_addition(at);

		if(changed) {
			// now check if that state is still reachable given only those events:
			int local_r = sup.getReachables(local_events);
			int tmp1 = ba.and(bdd_uc, local_r);
			ba.deref(local_r);

			int tmp2 = ba.and(tmp1, bdd_local_events);
			ba.deref(tmp1);

			boolean not_exist = (tmp2 == ba.getZero());

			if(not_exist) {
				// ok, nothing to worry about, we will carry one. just clean up the mess
				if(Options.debug_on)
					Options.out.println("Could not prove reachability using local events, continuing...");
				ba.deref(tmp2);
				return false;
			} else {
				// we have proved that the a bad state was reachable using LOCAL EVENTS!
				// no need to proceed, we now we are screwed ;(
				if(Options.debug_on)
					Options.out.println("A 'bad' state was proved to be reachable by _local events_. we are done!");


				// XXX: looks like this gives an error.
				// this is since when tracing back, show_trace will try _any_ way back to
				// the initial state which may look wierd since we said we only look at local states
				if(Options.trace_on) show_trace(sup, tmp2);

				ba.deref(tmp2);

				return true;
			}
		}
		return false; // no new local events, no need to compute ??
	}


	/**
	 * same as try_local_reachability(), but uses the previous local reachables and stores
	 * the current ones.
	 *
	 * NOTE: we assume a controllability-style test were the spec is _already proved to block
	 *       the half-transitions in bdd_uc!
	 *
	 * returns bdd-ZERO if to indicate that we have reached a uc_state (and thus no need to care about
	 * local reachables) or the new local-reachables as a BDD
	 *
	 */
	protected int try_and_remember_local_reachability(Supervisor sup, BDDAutomaton at,
		boolean is_plant,
		int bdd_uc, int initial_states)
		{

		boolean changed = handle_addition(at);
		if(is_plant) handle_plant_addition(at);

		if(changed) {
			// make sure the transition itself is always P-enabled.
			// this is done by taking does who cannot be blocked anymore:
			// "all relevant automata alread added --> event is local"
			// NOTE 1: This is just a lower-bound APPROXIMATION, folks!
			// NOTE 2: It is sufficient to check only the plants, specs are
			//         already _proved_ to block this event

			// int bdd_uc_local = ba.and(bdd_uc, bdd_local_events);
			int bdd_uc_local = ba.and(bdd_uc, bdd_local_events_plants);


			// now check if that state is still reachable given only those events:
			int local_r = sup.getReachables(local_events);
			int tmp_bdd = ba.and(bdd_uc_local, local_r);
			boolean not_exist = (tmp_bdd == ba.getZero());
			ba.deref(bdd_uc_local);



			if(not_exist) {
				// ok, nothing to worry about, we will carry one. just clean up the mess
				if(Options.debug_on)
					Options.out.println("Could not prove reachability using local events, continuing...");
				ba.deref(tmp_bdd);
				return local_r;
			} else {
				// we have proved that the a bad state was reachable using LOCAL EVENTS!
				// no need to proceed, we now we are screwed ;(
				if(Options.debug_on)
					Options.out.println("A 'bad' state was proved to be reachable by _local events_. we are done!");

				if(Options.trace_on) show_trace(sup, tmp_bdd);

				ba.deref(tmp_bdd);
				ba.deref(local_r); // throw this one, we wont need it anymore
				return ba.getZero();
			}
		}
		return initial_states; // no new local events, no need to compute ??
	}


	// ---------------------------------------------------------------------------------------


	// --------------------------------------------------------------------------------
	/**
	 * given a S x Sigma BDD, check if (some of) events are included.
	 * returns the number of changes
	 */
	protected int event_included(int bdd, boolean [] care, boolean [] removed) {

		// XXX: this is _NOT_ efficient!!
		int bdd_es = ba.exists(bdd, ba.getStateCube() );

		int count = 0;
		int len = care.length;
		int zero = ba.getZero();
		Event [] es = ba.getEvents();

		for(int i = 0; i < len; i++) {
			if(care[i]) {
				// XXX: should we use and() or restrict()
				int tmp = ba.and(bdd_es, es[i].getBDD() );
				if( (removed[i] = (tmp == zero)))
					count ++;
				ba.deref(tmp);
			} else {
				removed[i] = false;
			}
		}


		ba.deref(bdd_es);
		return count;
	}

	// --------------------------------------------------------------------------------
	/**
	 * show a trace to a reachable BDD.
	 * XXX: this part is not modular yet. it is much slower than the verification itself!
	 *      not very accurate either :(
	 */
	protected void show_trace(Supervisor sup, int bdd) {
		int tmp2 = ba.exists(bdd, ba.getEventCube());
		sup.trace("Trace", tmp2);
		ba.deref(tmp2);
	}

	/** show some stats, make everyone happy etc... */
	public void showStatistics() {
		Options.out.println(
			"Algorithm statistics: " +
			num_syncs_done  + " syncs, " +
			num_specs_tested + " tests, maximum sync depth = " +
			(max_sync_depth + 1) /* because max_sync_depth starts at _zero_ :( */
		);
	}
	// ---------------------------------------------------------------------------------------

	/** this is where all the action will be happening... */
	protected abstract boolean check(BDDAutomaton k, AutomataConfiguration ac);


}