
package org.supremica.automata.algorithms;


// TODO:
// Language Inclusion early termination (see try_local_reachability) failes because we seem to
// consider other Specs to, which makes us (almost) never find local events and have to compute
// the whole chain before we can give a nagative answer.
// fix this!!
// (need to modify):
// ba.getEventManager().getUsageCount(local_event_helper);
// int local_events_current = sup.getLocalEvents(local_event_helper, local_events);
// to only care abou the spec??

import org.supremica.automata.*;
import org.supremica.util.BDD.*;

import java.util.*;

/**
 * IncrementalBDDLanguageInclusion, for incremental modular verification with BDDs
 *
 */

public class IncrementalBDDLanguageInclusion extends BaseBDDLanguageInclusion {

	private boolean [] local_events;
	private int [] local_event_helper;

	// added some stuff that really should be local. but its good for the common code...
	private int bdd_events = -1;
	private int bdd_theta  = -1;
	private int local_events_found = 0;
	private Supervisor sup = null;


	// ---[ interface to the base class ]-----------------------------------------------
    public IncrementalBDDLanguageInclusion(org.supremica.automata.Automata selected,
			       org.supremica.automata.Automata unselected,
			       AutomataSynchronizerHelper.HelperData hd)
	throws Exception
    {
		super(selected, unselected, hd);

		local_events = new boolean[considred_events.length];
		local_event_helper = new int[considred_events.length];
	}


	public IncrementalBDDLanguageInclusion(org.supremica.automata.Automata theAutomata,
			       AutomataSynchronizerHelper.HelperData hd)
	throws Exception
    {
		super(theAutomata, hd);

		local_events = new boolean[considred_events.length];
		local_event_helper = new int[considred_events.length];
	}



	protected boolean check(BDDAutomaton k, AutomataConfiguration ac)
	{

		// events that are intresting for this automata
		boolean [] k_events = k.getEventCareSet(controllaibilty_test);
		IndexedSet.intersection(k_events, considred_events, workset_events);


		if(Options.debug_on) {
			ba.getEventManager().dumpSubset(
				"\n*** Verifiying " + k.getName()  + ", considred events", workset_events);
			Options.out.println("\n");
		}

		if(controllaibilty_test)	return control_check(k, ac, workset_events);
		else						return inclusion_check(k, ac, workset_events);
	}



	// ---[ the actual code ]---------------------------------------------------------

	/**
	 * Check if languake of K is included in the language of the rest of automata?
	 *
	 * INTERNAL:
	 *   check if L(w1) \Sigma \cap L(w2) \subseteq L(w1).
	 */
	 // XXX: I think we have already showed that we dont _need_ to include additional plants (???)
    private boolean inclusion_check(BDDAutomaton k, AutomataConfiguration ac, boolean [] workset_events)
    {

		// get events that are considred and in k
		boolean sane = ac.reset(k, considred_events, workset_events);
		if(!sane) return true; // nothing to check

		work1.empty(); // start with w1 = { k } ...
		work1.add(k);
		work2.empty(); // and w2 = \emptyset, that is L(w2) = \Sigma^* ??

		int bdd_cube_sp = ba.getStatepCube();
		bdd_events = ba.getAlphabetSubsetAsBDD(workset_events);


		// get first round theta:
		int tmp = ba.exists(k.getTpri(), bdd_cube_sp );
		bdd_theta = ba.not(tmp);
		ba.deref(tmp);
		tmp = ba.and(bdd_theta, k.getCareS());
		ba.deref(bdd_theta);
		bdd_theta = ba.and(tmp, bdd_events);
		ba.deref(tmp);

		sup = null;
		local_events_found = 0; // this is needed for try_local_reachability() to work properly
		for(;;) {
			BDDAutomaton next = ac.addone(work1, work2, true);
			if(next == null)
				break;

			if(Options.debug_on) {
				Options.out.println("\n -----------------------------------------------------------\n");
				Options.out.println("Check L(" + work1.toString() + ") subseteq L(" + work2.toString()  + ") ?");
			}

			int bdd_theta_delta = ba.relProd(next.getTpri(), bdd_events, bdd_cube_sp);
			bdd_theta = ba.andTo(bdd_theta, bdd_theta_delta);


			try {
				if(sup != null)  sup.cleanup(); // delete the last one

				sup = SupervisorFactory.createSupervisor(ba, work1, work2);
				int r = sup.getReachables();

				bdd_theta= ba.andTo(bdd_theta, r); // see how much of uc was reachable
				boolean ret = (bdd_theta == ba.getZero());



				if(ret) {
					// show that all and nc-arcs where unreachable
					if(Options.debug_on)
						ba.getEventManager().dumpSubset("*** Removed events", workset_events);

					cleanup_bdds();
					return true;
				}

				// ok, we now it might exists. we can proov that it _does_ exists if
				// we can show that is reachable using local events only!!
				else if(try_local_reachability()) {
					cleanup_bdds();
					return false;
				}


				// *** see if some events are proved to be unrachable and can be removed
				if(event_included(bdd_theta, workset_events, changes) > 0) {

					if(Options.debug_on)
						ba.getEventManager().dumpSubset("*** Removed events", changes);

					// and remove the targets in the queue waiting to be added
					ac.removeTargets(workset_events, changes);

					ba.deref(bdd_events);
					bdd_events = ba.getAlphabetSubsetAsBDD(workset_events);
				}


			} catch(Exception exx) {
				exx.printStackTrace();

				// clean up the mess we made ...
				cleanup_bdds();
				return false;
			}

		}


		// show the shortest trace to the language collision, if the user has enabled it
		if(Options.trace_on && sup != null)
			show_trace(bdd_theta);



		cleanup_bdds();

		return false;
	}






	/**
	 * Check if an automaton is controllable against a set of plants and specs...
	 */

	private boolean control_check(BDDAutomaton k, AutomataConfiguration ac, boolean [] workset_events)
	{


		// get events that are considred and in k
		boolean sane = ac.reset(k, considred_events,  workset_events);
		if(!sane) return true; // nothing to check

		work1.empty(); // start with w1 = { k } ...
		work1.add(k);
		work2.empty(); // and w2 = \emptyset, that is L(w2) = \Sigma^* ??

		int bdd_cube_sp = ba.getStatepCube();
		bdd_events = ba.getAlphabetSubsetAsBDD(workset_events);


		// get first round theta:
		int tmp = ba.exists(k.getTpri(), bdd_cube_sp );
		bdd_theta = ba.not(tmp);
		ba.deref(tmp);
		tmp = ba.and(bdd_theta, k.getCareS());
		ba.deref(bdd_theta);
		bdd_theta = ba.and(tmp, bdd_events);
		ba.deref(tmp);

		sup = null;
		int num_plants = 0;
		local_events_found = 0; // this is needed for try_local_reachability() to work properly

		for(;;) {
			BDDAutomaton next = ac.addone(work1, work2, true);
			if(next == null) break;
			if(next.getType() == org.supremica.util.BDD.Automaton.TYPE_PLANT) num_plants++;

			if(Options.debug_on) {
				Options.out.println("\n -----------------------------------------------------------\n");
				Options.out.println("Check C(" + work2.toString() + ", " + work1.toString()  + ") ?");
			}


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
					// show that all and nc-arcs where unreachable
					if(Options.debug_on)
						ba.getEventManager().dumpSubset("*** Removed events", workset_events);

					// clean up before returning
					cleanup_bdds();
					return true;
				}
				// ok, we now it might exists. we can proov that it _does_ exists if
				// we can show that is reachable using local events only!!
				else if(try_local_reachability()) {
					cleanup_bdds();
					return false;
				}

				// *** see if some events are proved to be unrachable and can be removed
				if(event_included(bdd_theta, workset_events, changes) > 0) {

					if(Options.debug_on)
						ba.getEventManager().dumpSubset("*** Removed events", changes);

					// and remove the targets in the queue waiting to be added
					ac.removeTargets(workset_events, changes);

					ba.deref(bdd_events);
					bdd_events = ba.getAlphabetSubsetAsBDD(workset_events);
				}

			} catch(Exception exx) {
				exx.printStackTrace();
				// clean up the mess we made ...
				cleanup_bdds();
				return false;
			}

		}


		// show the shortest trace to the language collision, if the user has enabled it
		if(Options.trace_on && sup != null)
			show_trace(bdd_theta);

		// do the delayed cleanup!
		cleanup_bdds();

		// if we didnt check any plants, then we are done (spec does not share
		// uncontrollable events with any plant)
		return (num_plants == 0);

	}

	// ----- [ all common code goes here ] -------------------------------------------------------
	private void cleanup_bdds() {
		if(bdd_theta != -1) {
			ba.deref(bdd_theta);
			bdd_theta = -1;
		}
		if(bdd_events != -1) {
			ba.deref(bdd_events);
			bdd_events = -1;
		}
		if(sup != null) {
			sup.cleanup();	// do the delayed cleanup!
			sup = null;
		}
	}
	private void show_trace(int bdd) {
		int tmp2 = ba.exists(bdd, ba.getEventCube());
		sup.trace("Trace", tmp2);
		ba.deref(tmp2);
	}



	private boolean try_local_reachability() {
		// lets prove that these events are really reachable by local events:
		ba.getEventManager().getUsageCount(local_event_helper);
		int local_events_current = sup.getLocalEvents(local_event_helper, local_events);
		if(local_events_current > local_events_found ) {

			if(Options.debug_on) {
				Options.out.println("   LL " + (local_events_current - local_events_found) +
				" new local events found.");
				ba.getEventManager().dumpSubset("   LL Local events", local_events);
			}
			local_events_found  = local_events_current;

			// now check if that state is still reachable given only those events:
			int local_r = sup.getReachables(local_events);
			int tmp_bdd = ba.and(bdd_theta, local_r);
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

				// TODO: looks like this gives an error:
				if(Options.trace_on) show_trace(tmp_bdd);

				ba.deref(tmp_bdd);

				return true;
			}
		}
		return false; // no new local events, no need to compute ??
	}


}



