



package org.supremica.automata.algorithms;


import org.supremica.automata.*;
import org.supremica.util.BDD.*;

import java.util.*;

/**
 * Base class, for BDD language containment verification
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

		if(Options.debug_on) {
			Options.out.println("*** Modular language containment test considreing " +
				IndexedSet.cardinality(considred_events) + " events." );
		}
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



	/** this is where all the action will be... */
	protected abstract boolean check(BDDAutomaton k, AutomataConfiguration ac);
}