
package org.supremica.automata.algorithms;


// TODO:
// we must only consider local reachability to P-enabled uc-arcs.
// as today, this is done by only looking at uc-arcs with LOCAL events,
// which are then always P-enabled. This is a waste of resource since:
//
// 1) we must need a lot more automata to get more local events while a simple test
// on each not-yet-added plant P_i in initial state is enough to ensure uc is enabled
//
// 2) we really do not need to look at Sp-events here, how about testing it as soon
// as the event is P-local [THIS SEEMS LIKE A VERY GOOD IDEA!!!]


import org.supremica.util.BDD.*;


/**
 * IncrementalBDDLanguageInclusion, for incremental modular verification with BDDs
 *
 *
 * IMPORTANT:
 *  this algorithm may NOT give the correct counterexample, the BAD state is correct,
 *  but it might choose a wired path to that state.
 *
 */



public class IncrementalBDDLanguageInclusion extends BaseBDDLanguageInclusion {


	// added some stuff that really should be local. but its good for the common code...
	private int bdd_events = -1;
	private int bdd_theta  = -1;
	private int bdd_initial_states  = -1;
	private int bdd_theta_plant = -1;
	private int bdd_theta_spec = -1;

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



	/** returns true if there is nothing to check, false if we must compute the answer */
	private boolean initialize_check(BDDAutomaton k, AutomataConfiguration ac, boolean [] workset_events) {

		// get events that are considred and in k
		boolean sane = ac.reset(k, considred_events, workset_events, current_event_usage);
		if(!sane) return true; // nothing to check



		// initialize the table of event usage
		initialize_event_usage(k);

		work1.empty(); // start with w1 = { k } ...
		work1.add(k);
		work2.empty(); // and w2 = \emptyset, that is L(w2) = \Sigma^* ??

		bdd_events = ba.getAlphabetSubsetAsBDD(workset_events);
		return false;

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
		if(initialize_check(k, ac, workset_events)) return true;

		int bdd_cube_sp = ba.getStatepCube();



		// get first round theta:
		int tmp = ba.exists(k.getTpri(), bdd_cube_sp );
		bdd_theta = ba.not(tmp);
		ba.deref(tmp);
		tmp = ba.and(bdd_theta, k.getCareS());
		ba.deref(bdd_theta);
		bdd_theta = ba.and(tmp, bdd_events);
		ba.deref(tmp);

		// get initial states:
		bdd_initial_states  = k.getI();
		ba.ref(bdd_initial_states);

		sup = null;

		for(;;) {
			num_syncs_done++; // statistic stuffs

			BDDAutomaton next = ac.addone(work1, work2, true);
			if(next == null)
				break;

			// update theta
			int bdd_theta_delta = ba.relProd(next.getTpri(), bdd_events, bdd_cube_sp);
			bdd_theta = ba.andTo(bdd_theta, bdd_theta_delta);

			// update Q_I
			bdd_initial_states = ba.andTo(bdd_initial_states, next.getI() );

			if(Options.debug_on) {
				Options.out.println("\n -----------------------------------------------------------\n");
				Options.out.println("Check L(" + work1.toString() + ") subseteq L(" + work2.toString()  + ") ?");
			}


			try {
				if(sup != null)  sup.cleanup(); // delete the last one

				// sup = SupervisorFactory.createSupervisor(ba, work2, work1);
				sup = SupervisorFactory.suggestSupervisorForModularReachability(ba, work2, work1);
				int r = sup.getReachables(bdd_initial_states);

				bdd_theta= ba.andTo(bdd_theta, r); // see how much of uc was reachable
				boolean ret = (bdd_theta == ba.getZero());



				if(ret) {
					// show that all and nc-arcs where unreachable
					if(Options.debug_on)
						ba.getEventManager().dumpSubset("*** Removed events", workset_events);

					cleanup_bdds();
					return true;
				}

				// ok, we now it might exists. we can prove that it _does_ exists if
				// we can show that is reachable using local events only!!
				// no idea doing this if we have added all automaton that can be added :(
				else if(ac.moreToGo()) {
					// sess discussion about self loops in C-algo
					int locals = try_and_remember_local_reachability(sup, next, bdd_theta, bdd_initial_states);

					if(locals == ba.getZero() ) {
						cleanup_bdds();
						return false;
					} else if(locals == bdd_initial_states) {
						// no new local events where found...
					} else {
						// new local events found and new local-reachable states computed:
						ba.deref(bdd_initial_states);
						bdd_initial_states = locals;
					}

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
		int tmp1, tmp2;

		if(initialize_check(k, ac, workset_events)) return true;
		int bdd_cube_sp = ba.getStatepCube();




		// -------------------- get initial theta halves:
		// for plant
		bdd_theta_plant = ba.getOne();
		ba.ref(bdd_theta_plant);

		// for spec
		tmp1 = ba.exists(k.getTpri(), bdd_cube_sp );
		tmp2 = ba.not(tmp1);
		ba.deref(tmp1);
		bdd_theta_spec = ba.and(tmp2, k.getCareS());
		ba.deref(tmp2);

		// --------------------- get first round theta:
		bdd_theta = bdd_theta_spec;
		ba.ref(bdd_theta);
		// IS THIS NEEDED ? [answer: yes, it will finally be conjoind with theta_spec and there it will do good].
		bdd_theta = ba.andTo(bdd_theta , bdd_events);




		sup = null;

		bdd_initial_states  = k.getI();
		ba.ref(bdd_initial_states);


		for(;;) {
			num_syncs_done++; // statistic stuffs

			BDDAutomaton next = ac.addone(work1, work2, true);
			if(next == null) break;


			// ++new code
			if( ac.lastAutomatonWasPlant() ) {
				int bdd_theta_delta = ba.relProd(next.getTpri(), bdd_events, bdd_cube_sp);
				bdd_theta_plant = ba.andTo(bdd_theta_plant, bdd_theta_delta);
				bdd_theta = ba.andTo(bdd_theta, bdd_theta_delta);
				ba.deref(bdd_theta_delta);
			} else {

				// update theta_Sp
				tmp1 = ba.exists(next.getTpri(), bdd_cube_sp );
				tmp2 = ba.not(tmp1);
				ba.deref(tmp1);
				tmp1 = ba.and(tmp2, next.getCareS() );
				ba.deref(tmp2);

				bdd_theta_spec = ba.orTo(bdd_theta_spec, tmp1);
				ba.deref(tmp1);

				// update theta
				ba.deref(bdd_theta);
				bdd_theta = ba.and(bdd_theta_spec, bdd_theta_plant);
			}

			bdd_initial_states = ba.andTo(bdd_initial_states, next.getI() );



			if(Options.debug_on) {
				Options.out.println("\n -----------------------------------------------------------\n");
				Options.out.println("Check C(" + work2.toString() + ", " + work1.toString()  + ") ?");
			}


			try {
				if(sup != null){	// delete the last one
					sup.cleanup();
					sup = null;
				}


				sup = SupervisorFactory.suggestSupervisorForModularReachability(ba, work2, work1);

				int r = sup.getReachables(bdd_initial_states);

				bdd_theta= ba.andTo(bdd_theta, r); // see how much of uc was reachable
				boolean ret = (bdd_theta == ba.getZero());



				if(ret) {
					// show that all and nc-arcs where unreachable
					if(Options.debug_on)
						ba.getEventManager().dumpSubset("*** Removed events (all)", workset_events);

					// clean up before returning
					cleanup_bdds();
					return true;
				}

				// ok, we now it might exists. we can proov that it _does_ exists if
				// we can show that is reachable using local events only!!
				// no idea doing this if we have added all automaton that can be added :(
				else if(ac.moreToGo()){
					// we dont need to do this to get rid of the damn self-loops:
					// int bdd_theta_relevant = ba.and(bdd_theta, work2.getSigma() );
					// remember that try_and_remember_local_reachability considers only P-enabled uc-arcs


					// resuing local reachables now:
					int locals = try_and_remember_local_reachability(sup, next, bdd_theta, bdd_initial_states);


					if(locals == ba.getZero() ) {
						cleanup_bdds();
						return false;
					} else if(locals == bdd_initial_states) {
						// no new local events where found...
					} else {
						// new local events found and new local-reachable states computed:
						ba.deref(bdd_initial_states);
						bdd_initial_states = locals;
					}
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


		if(bdd_theta_spec != -1) {
			ba.deref(bdd_theta_spec);
			bdd_theta_spec = -1;
		}

		if(bdd_theta_plant != -1) {
			ba.deref(bdd_theta_plant);
			bdd_theta_plant = -1;
		}

		if(bdd_theta != -1) {
			ba.deref(bdd_theta);
			bdd_theta = -1;
		}

		if(bdd_events != -1) {
			ba.deref(bdd_events);
			bdd_events = -1;
		}

		if(bdd_initial_states != -1) {
			ba.deref(bdd_initial_states);
			bdd_initial_states = -1;
		}

		if(sup != null) {
			sup.cleanup();	// do the delayed cleanup!
			sup = null;
		}
	}
}



