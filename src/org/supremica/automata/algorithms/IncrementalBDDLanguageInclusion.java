
package org.supremica.automata.algorithms;


import org.supremica.automata.*;
import org.supremica.util.BDD.*;

import java.util.*;

/**
 * IncrementalBDDLanguageInclusion, for incremental modular verification with BDDs
 *
 */

public class IncrementalBDDLanguageInclusion {
    private AutomataSynchronizerHelper.HelperData hd;
    private org.supremica.automata.Automata theAutomata;

	private boolean [] considred_events, events;
	private boolean controllaibilty_test;
    private BDDAutomata ba = null;
    private BDDAutomaton [] all = null;
  	private Group L1 = null;
  	private Group L2 = null;
  	private Group work1 = null, work2 = null;




    /**
     * creates a verification object for LANGUAGE INCLUSION test.
     * depeding one the type of algorithm used, this call might take a while
     * <b>DONT FORGET TO CALL cleanup() AFTERWARDS!!!</b>
     * @see cleanup()
     */
    public IncrementalBDDLanguageInclusion(org.supremica.automata.Automata selected,
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
    public IncrementalBDDLanguageInclusion(org.supremica.automata.Automata theAutomata,
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


	// internal init for both constructors common code
	private void init() {
		// get our working sets
		work1 = new Group(ba, all.length, "work1");
		work2 = new Group(ba, all.length, "work2");
		// tell the Supervisor classes they shouldnt clean them up when they are done:
		work1.setCleanup(false);
		work2.setCleanup(false);

		// get the intersection of the considred events
		// *** I DONT KNOW IF THIS IS CORRECT!! ***
		// must duplicate since we will change its value just velow
		considred_events = Util.duplicate(L2.getEventCareSet(controllaibilty_test));

		boolean [] tmp = L1.getEventCareSet(controllaibilty_test);
		for(int i = 0; i < tmp.length; i++) considred_events[i] &= tmp[i];

		if(Options.debug_on)
			System.err.println("Incremental language containment test considreing " + Util.count(considred_events) + " events.");

		// temporary vector for the current considred events in our current automata set
		events = new boolean[considred_events.length];
	}


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
     *
     * @return TRUE if the system a1 in a1
     */
    public boolean passLanguageInclusion() {

		boolean save_show_grow = Options.show_grow;
		Options.show_grow = false;

		Event[] alphabet = ba.getEvents();
		BDDAutomaton[] l1 = L1.getMembers();
		int count = L1.getSize();
		boolean result = true;


		// IMPORTANT OPTIMIZATION:
		// only if [Sigma' - (SigmaL1 \cap SigmaL2)] is not empty we need to add from L1 too
		// this is not the case if we are doing a language inclusion test!
		AutomataConfiguration ac = new AutomataConfiguration (L1, L2, !controllaibilty_test);

		for(int i = 0; i < count; i++)
		{
			BDDAutomaton k =  l1[i];

			if(controllaibilty_test)
				result &= control_check(k, ac);
			else
				result &= inclusion_check(k, ac);
			if(!result)
			{
				break;
			}
		}

		Options.show_grow = save_show_grow;
		return result;
    }




	/**
	 * Check if languake of K is included in the language of the rest of automata?
	 *
	 * INTERNAL:
	 *   check if L(w1) \Sigma \cap L(w2) \subseteq L(w1).
	 */
	 // XXX: I think we have already showed that we dont _need_ to include additional plants (???)
    private boolean inclusion_check(BDDAutomaton k, AutomataConfiguration ac)
    {

		ac.reset(k, considred_events, events); // get events that are considred and in k
		if(Options.debug_on)	System.out.println("Verifiying " + ac.toString() );

		work1.empty(); // start with w1 = { k } ...
		work1.add(k);
		work2.empty(); // and w2 = \emptyset, that is L(w2) = \Sigma^* ??

		int bdd_cube_sp = ba.getStatepCube();
		int bdd_events = ba.getAlphabetSubsetAsBDD(events);


		// get first round theta:
		int tmp = ba.exists(k.getTpri(), bdd_cube_sp );
		int bdd_theta = ba.not(tmp);
		ba.deref(tmp);
		tmp = ba.and(bdd_theta, k.getCareS());
		ba.deref(bdd_theta);
		bdd_theta = ba.and(tmp, bdd_events);
		ba.deref(tmp);

		Supervisor sup = null;
		for(;;) {
			BDDAutomaton next = ac.addone(events, work1, work2, true);
			if(next == null)
				break;

			if(Options.debug_on)	System.out.println("Adding " + next.getName() );

			int bdd_theta_delta = ba.relProd(next.getTpri(), bdd_events, bdd_cube_sp);
			bdd_theta = ba.andTo(bdd_theta, bdd_theta_delta);


			try {
				if(sup != null)  sup.cleanup(); // delete the last one

				sup = SupervisorFactory.createSupervisor(ba, work1, work2);
				int r = sup.getReachables();

				bdd_theta= ba.andTo(bdd_theta, r); // see how much of uc was reachable
				boolean ret = (bdd_theta == ba.getZero());

				if(ret) {
					ba.deref(bdd_theta);
					ba.deref(bdd_events);
					sup.cleanup();	// do the delayed cleanup!
					return true;

				}
			} catch(Exception exx) {
				exx.printStackTrace();

				// clean up the mess we made ...
				ba.deref(bdd_theta);
				ba.deref(bdd_events);
				if(sup != null) sup.cleanup();	// do the delayed cleanup!

				return false;
			}

		}


		// show the shortest trace to the language collision, if the user has enabled it
		if(Options.trace_on && sup != null)
		{
			int tmp2 = ba.exists(bdd_theta, ba.getEventCube());
			sup.trace("Trace", tmp2);
			ba.deref(tmp2);
		}



		ba.deref(bdd_theta);
		ba.deref(bdd_events);
		if(sup != null)	sup.cleanup();	// do the delayed cleanup!

		return false;
	}






	/**
	 * Check if an automaton is controllable against a set of plants and specs...
	 */

	private boolean control_check(BDDAutomaton k, AutomataConfiguration ac)
	{

		ac.reset(k, considred_events, events); // get events that are considred and in k
		if(Options.debug_on)	System.out.println("\n*** Verifiying " + ac.toString() );

		work1.empty(); // start with w1 = { k } ...
		work1.add(k);
		work2.empty(); // and w2 = \emptyset, that is L(w2) = \Sigma^* ??

		int bdd_cube_sp = ba.getStatepCube();
		int bdd_events = ba.getAlphabetSubsetAsBDD(events);


		// get first round theta:
		int tmp = ba.exists(k.getTpri(), bdd_cube_sp );
		int bdd_theta = ba.not(tmp);
		ba.deref(tmp);
		tmp = ba.and(bdd_theta, k.getCareS());
		ba.deref(bdd_theta);
		bdd_theta = ba.and(tmp, bdd_events);
		ba.deref(tmp);

		Supervisor sup = null;
		int num_plants = 0;
		for(;;) {
			BDDAutomaton next = ac.addone(events, work1, work2, true);
			if(next == null) break;
			if(next.getType() == org.supremica.util.BDD.Automaton.TYPE_PLANT) num_plants++;

			if(Options.debug_on)	System.out.println("Check C(" + work2.toString() + ", " + work1.toString()  + ") ?");

			int bdd_theta_delta = ba.relProd(next.getTpri(), bdd_events, bdd_cube_sp);
			bdd_theta = ba.andTo(bdd_theta, bdd_theta_delta);


			try {
				if(sup != null){	// delete the last one
					sup.cleanup();
					sup = null;
				}

				sup = SupervisorFactory.createSupervisor(ba, work1, work2);
				int r = sup.getReachables();

				bdd_theta= ba.andTo(bdd_theta, r); // see how much of uc was reachable
				boolean ret = (bdd_theta == ba.getZero());

				if(ret) {
					ba.deref(bdd_theta);
					ba.deref(bdd_events);
					sup.cleanup();	// do the delayed cleanup!
					return true;

				}
			} catch(Exception exx) {
				exx.printStackTrace();
				// clean up the mess we made ...
				ba.deref(bdd_theta);
				ba.deref(bdd_events);
				if(sup != null) sup.cleanup();	// do the delayed cleanup!
				return false;
			}

		}


		// show the shortest trace to the language collision, if the user has enabled it
		if(Options.trace_on && sup != null)
		{
			int tmp2 = ba.exists(bdd_theta, ba.getEventCube());
			sup.trace("Trace", tmp2);
			ba.deref(tmp2);
		}

		ba.deref(bdd_theta);
		ba.deref(bdd_events);
		if(sup != null)	sup.cleanup();	// do the delayed cleanup!

		// if we didnt check any plants, then we are done (spec does not share
		// uncontrollable events with any plant)
		return (num_plants == 0);

	}

}



