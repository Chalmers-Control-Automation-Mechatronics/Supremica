
package org.supremica.automata.algorithms;

import org.supremica.util.BDD.*;

/**
 * ModularBDDLanguageInclusion, for verification with BDDs.
 *
 */


public class ModularBDDLanguageInclusion extends BaseBDDLanguageInclusion {

	private int bdd_events  = -1;
	private int bdd_bad = -1;
	private Supervisor sup = null;

	// ---[ interface to the base class ]-----------------------------------------------

    public ModularBDDLanguageInclusion(org.supremica.automata.Automata selected,
			       org.supremica.automata.Automata unselected,
			       AutomataSynchronizerHelper.HelperData hd)
	throws Exception
    {
		super(selected, unselected, hd);
		init2();
    }


    public ModularBDDLanguageInclusion(org.supremica.automata.Automata theAutomata,
		AutomataSynchronizerHelper.HelperData hd)
		throws Exception
	{
		super(theAutomata, hd);
		init2();
	}

	private void init2() {

		if(Options.debug_on) {
			Options.out.println("*** Modular language containment test considreing " +
				IndexedSet.cardinality(considred_events) + " events." );
		}
	}



	// ---[ the actual code ]---------------------------------------------------------


	/**
	 * Check if languake of K is included in the language of the rest of automata?
	 */
    protected boolean check(BDDAutomaton k, AutomataConfiguration ac)
    {
		// events that are intresting for this automata
		boolean [] k_events = k.getEventCareSet(controllaibilty_test);
		IndexedSet.intersection(k_events, considred_events, workset_events);

		if(Options.debug_on) {
			ba.getEventManager().dumpSubset(
				"\n*** Verifiying " + k.getName()  + ", considering events", workset_events);
			Options.out.println("\n");
		}

		// get events that are considred and in k
		boolean sane = ac.reset(k, considred_events,  workset_events, current_event_usage);
		if(!sane) return true; // nothing to check

		// initialize the table of event usage
		initialize_event_usage(k);


		// check if L(w1) \Sigma \cap L(w2) \subseteq L(w1)
		work1.empty();	// start with w1 = { k } ...
		work1.add(k);
		work2.empty();	// and w2 = \emptyset, that is L(w2) = \Sigma^* ??


		int bdd_cube_sp = ba.getStatepCube();
		bdd_events = ba.getAlphabetSubsetAsBDD(workset_events);


		sup = null; // you never know ...

		for(;;) {
			num_syncs_done++; // statistic stuffs

			BDDAutomaton next = ac.addone(work1, work2, true);
			if(next == null) break;

			boolean was_plant = ac.lastAutomatonWasPlant();

			if(Options.debug_on) {
				Options.out.println("\n -----------------------------------------------------------\n");
				Options.out.println("Check C(" + work2.toString() + ", " + work1.toString()  + ") ?");
			}



			try {
				if(	sup != null) {
					sup.cleanup();
					sup = null;
				}

				sup = SupervisorFactory.suggestSupervisorForModularReachability(ba, work2, work1);



				bdd_bad = sup.computeReachableLanguageDifference(bdd_events);
				boolean ret = (bdd_bad == ba.getZero());

				// proof non-reachability:
				if(ret) {
					cleanup_bdds();
					return true;
				}

				// proof reachability by local events and thus reachability globally:
				// no idea doing this if we have added all automaton that can be added :(
				if(ac.moreToGo()  && try_local_reachability(sup, next, was_plant, bdd_bad)) {
					cleanup_bdds();
					return false;
				}


			// XXX: this messes up things. events that are not in P (yet) will be removed and declared
			//      ok while they are not! this is due to or "dirty trick" in the function
			// Supervisor.computeLanguageDifference(...) :
			// tmp2 = manager.andTo(tmp2, plant.getSigma());
			// anyway, it seems that we can do without it...

			// XXX: On Second Thought, I am not sure if above is correct

/*
				// *** see if some events are proved to be unrachable and can be removed
				if(event_included(bdd_bad, workset_events, changes) > 0) {

					if(Options.debug_on)
						ba.getEventManager().dumpSubset("*** Removed events", changes);

					// and remove the targets in the queue waiting to be added
					ac.removeTargets(workset_events, changes);

					ba.deref(bdd_events);
					bdd_events = ba.getAlphabetSubsetAsBDD(workset_events);
				}

*/

			} catch(Exception exx) {
				exx.printStackTrace();
				cleanup_bdds();
				return false;
			}

		}



		// show the shortest trace to the language collision, if the user has enabled it
		if(Options.trace_on && sup != null) {
			show_trace(sup, bdd_bad);
		}

		// do the delayed cleanup!
		cleanup_bdds();

		return work2.isEmpty();		// if no L, then always  K \subseteq \Sigma^*
	}


	private void  cleanup_bdds() {
		if(bdd_bad != -1) {
			ba.deref(bdd_bad);
			bdd_bad = -1;
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
