
package org.supremica.automata.algorithms;


import org.supremica.automata.*;
import org.supremica.util.BDD.*;

import java.util.*;

/**
 * ModularBDDLanguageInclusion, for verification with BDDs
 *
 */

public class ModularBDDLanguageInclusion {
    private AutomataSynchronizerHelper.HelperData hd;
    private org.supremica.automata.Automata theAutomata;

	private boolean [] considred_events, events;
    private BDDAutomata ba = null;
    private BDDAutomaton [] all = null;
  	private Group L1 = null;
  	private Group L2 = null;
	private boolean controllaibilty_test;
  	private Group work1 = null, work2 = null;




    /**
     * creates a verification object for LANGUAGE INCLUSION test.
     * depeding one the type of algorithm used, this call might take a while
     * <b>DONT FORGET TO CALL cleanup() AFTERWARDS!!!</b>
     * @see cleanup()
     */
    public ModularBDDLanguageInclusion(org.supremica.automata.Automata selected,
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


    public ModularBDDLanguageInclusion(org.supremica.automata.Automata theAutomata,
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

    private void init() {

		// get our working sets
		work1 = new Group(ba, all.length, "work1");
		work2 = new Group(ba, all.length, "work2");

		// tell the Supervisor classes they shouldnt clean them up when they are done:
		work1.setCleanup(false);
		work2.setCleanup(false);

		// get the intersection of the considred events
		// XXX: *** I DONT KNOW IF THIS IS CORRECT!! ***

		considred_events = Util.duplicate(L2.getEventCareSet(controllaibilty_test)); /* must duplicate since we will change its value just velow*/

		boolean [] tmp = L1.getEventCareSet(controllaibilty_test);

		for(int i = 0; i < tmp.length; i++) considred_events[i] &= tmp[i];

		/* temporary vector for the current considred events in our current automata set*/
		events = new boolean[considred_events.length];

		if(Options.debug_on) {
			System.err.println("Modular language containment test considreing " +
				Util.count(considred_events) + " events." );
		}
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

		boolean save_show_grow = Options.show_grow; // temporary disable
		Options.show_grow = false;


		BDDAutomaton[] l1 = L1.getMembers();
		int count = L1.getSize();


		// IMPORTANT OPTIMIZATION:
		// only if [Sigma' - (SigmaL1 \cap SigmaL2)] is not empty we need to add from L1 too
		// this is not the case if we are doing a language inclusion test!
		AutomataConfiguration ac = new AutomataConfiguration (L1, L2, !controllaibilty_test);
		boolean result = true;

		for(int i = 0; i < count; i++)
		{
			BDDAutomaton k =  l1[i];
			if(Options.debug_on)	System.err.println("Checkintg " + k.getName() );
			result &= check(k, ac);
			if(!result)
			{
				break;
			}
		}


		Options.show_grow = save_show_grow; // write back the saved state

		return result;
    }

	/**
	 * Check if languake of K is included in the language of the rest of automata?
	 */
    private boolean check(BDDAutomaton k, AutomataConfiguration ac)
    {
		// after this, events will hold the events in k that are considred.
		// ac should mark the plants/specs with connections to these events

		ac.reset(k, considred_events, events);
		if(Options.debug_on)	System.out.println("Verifiying " + ac.toString() );

		// check if L(w1) \Sigma \cap L(w2) \subseteq L(w1)

		// start with w1 = { k } ...
		work1.empty();
		work1.add(k);

		// and w2 = \emptyset, that is L(w2) = \Sigma^* ??
		work2.empty();

		Supervisor sup = null;
		int states = -1;
		while(ac.addone(events, work1, work2, true) != null) {
			try {
				if(	sup != null) {
					sup.cleanup();
					sup = null;
					if(states != -1) ba.deref(states);
					states = -1;
				}
				sup = SupervisorFactory.createSupervisor(ba, work1, work2);

				if(Options.debug_on)	System.out.println("Checking if " + work2.toString() + " subseteq " + work1.toString() );

				states = sup.computeReachableLanguageDifference();
				boolean ret = (states == ba.getZero());

				if(ret) {
					sup.cleanup();
					ba.deref(states);
					return true;
				}
			} catch(Exception exx) {
				exx.printStackTrace();
				if(sup != null) sup.cleanup();
				if(states != -1) ba.deref(states);
				return false;
			}

		}

		// dump trace ...
		if(Options.trace_on && sup != null && states != -1)
		{
			sup.trace("Trace", states);
		}

		// cleanup
		if(sup != null) sup.cleanup();
		if(states != -1) ba.deref(states);



		// if no plants existed, then we cant fail! [becasue then L(P) = \Sigma^* ]
		return work2.isEmpty();
	}

}
