



package org.supremica.automata.algorithms;


import org.supremica.automata.*;
import org.supremica.util.BDD.*;

import java.util.*;

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




	// local event stuff:
	protected boolean [] local_events;	/** true if an event is local, valid only after a call to register_automaton_addition() */
	private int [] current_event_usage; 	/** temporary storage for local events */
	protected int [] event_usage_count; /** number of automata that use each event */
	protected int local_events_found = 0; /** how many local events we had last time. to detect if any new has been added */

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
		if(work1 != null) work1.cleanup();
		if(work2 != null) work2.cleanup();
		if(L1 != null) L1.cleanup();
		if(L2 != null) L2.cleanup();
		if(ba != null) ba.cleanup();
    }

	// ----------------------------------------------------------------------------------
	/** constructors common init code */
	protected void init() {

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

		init_local_event_detection();
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
		boolean save_show_grow = Options.show_grow;
		Options.show_grow = false;

		int save_algo = Options.algo_family;
		Options.algo_family = Options.ALGO_MONOLITHIC; // good for smaller tests!




		BDDAutomaton[] l1 = L1.getMembers();
		int count = L1.getSize();
		boolean result = true;


		// IMPORTANT OPTIMIZATION:
		// only if [Sigma' - (SigmaL1 \cap SigmaL2)] is not empty we need to add from L1 too
		// this is not the case if we are doing a language inclusion test!
		AutomataConfiguration ac = new AutomataConfiguration (L1, L2, controllaibilty_test);

		for(int i = 0; i < count; i++)
		{
			BDDAutomaton k =  l1[i];

			result = check(k, ac);
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
		Options.algo_family = save_algo;

		return result;
	}

	// ------ [LOCAL event detection ] -------------------------------------------------------------

	/** intiailize event-counting related stuff for detecting local events */
	private void init_local_event_detection() {
		local_events = new boolean[considred_events.length];
		current_event_usage = new int[considred_events.length];
		event_usage_count = new int[considred_events.length];

		ba.getEventManager().getUsageCount(event_usage_count);
		if(!controllaibilty_test) {
			// include only plants, we dont need spec
			L1.removeEventUsage(event_usage_count);
		}

		// initialize local_events_found!! some events might not have been used at all!
		local_events_found = 0;
		int len = event_usage_count.length;
		for(int i = 0; i < len; i++) if(event_usage_count[i] == 0) local_events_found++;

	}

	/** initialize the local-event detection algorithm, must be called first! */
	protected void initialize_event_usage(BDDAutomaton sp) {
		int len = event_usage_count.length;
		for(int i = 0; i < len; i++)
			current_event_usage[i] = event_usage_count[i];


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
		for(int i = 0; i < len; i++)
		if(current_event_usage[i] < 1) {
			count++;
			local_events[i] = true;
		} else {
			local_events[i] = false;
		}

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
	// ---------------------------------------------------------------------------------------

	/**
	 * check that by adding another automaton 'at', at least state in bdd_uc
	 * is reachable by local-events _only_.
	 */
	protected boolean try_local_reachability(Supervisor sup, BDDAutomaton at, int bdd_uc) {
		int local_events_current = register_automaton_addition(at);

		if(local_events_current > local_events_found ) {

			if(Options.debug_on) {
				Options.out.println("   LL " + (local_events_current - local_events_found) +
				" new local events found.");
				ba.getEventManager().dumpSubset("   LL Local events", local_events);
			}
			local_events_found  = local_events_current;

			// now check if that state is still reachable given only those events:
			int local_r = sup.getReachables(local_events);
			int tmp_bdd = ba.and(bdd_uc, local_r);
			boolean not_exist = (tmp_bdd == ba.getZero());



			ba.deref(local_r);

			if(not_exist) {
				// ok, nothing to worry about, we will carry one. just clean up the mess
				if(Options.debug_on)
					Options.out.println("Could not prove reachability using local events, continuing...");
				ba.deref(tmp_bdd);
				return false;
			} else {
				// we have proved that the a bad state was reachable using LOCAL EVENTS!
				// no need to proceed, we now we are screwed ;(
				if(Options.debug_on)
					Options.out.println("A 'bad' state was proved to be reachable by _local events_. we are done!");

				// XXX: looks like this gives an error.
				// this is since when tracing back, show_trace will try _any_ way back to
				// the initial state which may look wierd since we said we only look at local states
				if(Options.trace_on) show_trace(sup, tmp_bdd);

				ba.deref(tmp_bdd);

				return true;
			}
		}
		return false; // no new local events, no need to compute ??
	}

	// ---------------------------------------------------------------------------------------

	/** this is where all the action will be... */
	protected abstract boolean check(BDDAutomaton k, AutomataConfiguration ac);
}