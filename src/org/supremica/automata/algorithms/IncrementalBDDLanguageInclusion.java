
package org.supremica.automata.algorithms;


// TODO: remove the early-termination test if no more automata exists to be added



import org.supremica.automata.*;
import org.supremica.util.BDD.*;

import java.util.*;

/**
 * IncrementalBDDLanguageInclusion, for incremental modular verification with BDDs
 *
 */




public class IncrementalBDDLanguageInclusion extends BaseBDDLanguageInclusion {



	// added some stuff that really should be local. but its good for the common code...
	private int bdd_events = -1;
	private int bdd_theta  = -1;
	private Supervisor sup = null;


	// ---[ interface to the base class ]-----------------------------------------------
    public IncrementalBDDLanguageInclusion(org.supremica.automata.Automata selected,
			       org.supremica.automata.Automata unselected,
			       AutomataSynchronizerHelper.HelperData hd)
	throws Exception
    {
		super(selected, unselected, hd);
		init2();
	}


	public IncrementalBDDLanguageInclusion(org.supremica.automata.Automata theAutomata,
			       AutomataSynchronizerHelper.HelperData hd)
	throws Exception
    {
		super(theAutomata, hd);
		init2();
	}

	private void init2() {
		if(Options.debug_on) {
			Options.out.println("*** Incremental language containment test considreing " +
				IndexedSet.cardinality(considred_events) + " events." );
		}
	}

	// ------------------------------------------------------------------------------------

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


		// initialize the table of event usage
		initialize_event_usage(k);

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

				sup = SupervisorFactory.createSupervisor(ba, work2, work1);
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
				else if(try_local_reachability(sup, next, bdd_theta)) {
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
			show_trace(sup, bdd_theta);



		cleanup_bdds();

		// if no L, then always  K \subseteq \Sigma^*
		return work2.isEmpty();
	}






	/**
	 * Check if an automaton is controllable against a set of plants and specs...
	 */

	private boolean control_check(BDDAutomaton k, AutomataConfiguration ac, boolean [] workset_events)
	{


		// get events that are considred and in k
		boolean sane = ac.reset(k, considred_events,  workset_events);
		if(!sane) return true; // nothing to check

		// initialize the table of event usage
		initialize_event_usage(k);

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

		for(;;) {
			BDDAutomaton next = ac.addone(work1, work2, true);
			if(next == null) break;

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


				sup = SupervisorFactory.createSupervisor(ba, work2, work1);
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
				else if(try_local_reachability(sup, next, bdd_theta)) {
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
			show_trace(sup, bdd_theta);

		// do the delayed cleanup!
		cleanup_bdds();



		// if no L, then always  K \subseteq \Sigma^*
		return work2.isEmpty();
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
}



