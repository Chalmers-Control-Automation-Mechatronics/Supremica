
package org.supremica.util.BDD.li;


import org.supremica.util.BDD.*;
import org.supremica.automata.algorithms.*;


/** Modular LI algo */
public class ModularLI extends BaseLI {


	private int bdd_bad = -1;			/** temporary variable for potentially bad states */
	private int bdd_sigma_w = -1;		/** \Sigma' (sigma workset)*/
	private int bdd_local_events  = -1;
	private int bdd_local_events_plant = -1;

	private Supervisor  sup = null;
	private Group Ip = null;
	private Group Jp = null;


    public ModularLI(org.supremica.automata.Automata selected, org.supremica.automata.Automata unselected, AutomataSynchronizerHelper.HelperData hd) throws Exception {
		super(selected, unselected, hd);
	}

	public ModularLI(org.supremica.automata.Automata automata, AutomataSynchronizerHelper.HelperData hd) throws Exception {
		super(automata, hd);
	}


	public void cleanup() {
		bdd_cleanup();
		super.cleanup();
	}


	private void bdd_cleanup() {
		if(bdd_bad != -1) { ba.deref(bdd_bad); bdd_bad = -1; }
		if(bdd_sigma_w != -1) { ba.deref(bdd_sigma_w); bdd_sigma_w = -1; }
		if(bdd_local_events != -1) { ba.deref(bdd_local_events ); bdd_local_events  = -1; }
		if(bdd_local_events_plant != -1) { ba.deref(bdd_local_events_plant ); bdd_local_events_plant  = -1; }

		if(sup != null) { sup.cleanup(); sup = null; }
		if(Ip  != null) { Ip.cleanup();  Ip  = null; }
		if(Jp  != null) { Jp.cleanup();  Jp  = null; }

	}


	public boolean check(BDDAutomaton Xi, Coverage ac) {
		bdd_cleanup(); // clean up previous rounds shit


		// ----[ init all variables here ] -------------------------------
		Ip = new Group(ba, L1.getSize(), "I'");
		Jp = new Group(ba, L2.getSize(), "J'");
		Ip.add(Xi);

		Ip.setCleanup( false); // DONT let a supervisor clean me up!
		Jp.setCleanup( false); // dito


		int last_locals = 0;
		int last_plant_locals = 0;
		bdd_local_events_plant = ba.ref( ba.getZero() );
		bdd_local_events       = ba.ref( ba.getZero() );
		bdd_sigma_w            = ba.getAlphabetSubsetAsBDD(workset_events);

		BDDAutomaton next;
		while( (next = ac.next() ) != null) {
			num_automata_added++;
			num_syncs_done++;


			if(Options.debug_on) Options.out.println("\t +++ Adding " + next.getName() + "\n");
			if(ac.lastWasSpec() )	Ip.add(next);
			else					Jp.add(next);


			if(sup != null) { sup.cleanup(); sup = null; }
			try {
				sup = SupervisorFactory.suggestSupervisorForModularReachability(ba, Jp, Ip);
			} catch(Exception exx) {
				exx.printStackTrace();
				return false;
			}


			// More verbose crap
			if(Options.debug_on) {
				Options.out.println("\n\n\n After adding " + next.getName() + ":\n");
				ac.dump();
			}

			// some gymnatics needed to get the VALID subset of bdd_sigma_w (see computeReachableLanguageDifference() for reason)
			int bdd_sigma = ba.and( Ip.getSigma() , Jp.getSigma() );
			int bdd_workset_valid = ba.and( bdd_sigma, bdd_sigma_w);
			ba.deref(bdd_sigma);

			bdd_bad = sup.computeReachableLanguageDifference(bdd_workset_valid);
			ba.deref(bdd_workset_valid);

			boolean ret = (bdd_bad == ba.getZero());


			// all nc-arcs were considred and found to be unreachable
			if(ret && ac.allPlantEventsIncluded()) {
				if(Options.debug_on) ba.getEventManager().dumpSubset("*** Removed events (all)", workset_events);
				return true;
			}



			// update P-local events, this is used to check for enabled half-transitions...
			int new_plant_locals = ac.getNumOfLocalPlantEvents();
			if(last_plant_locals  <  new_plant_locals ) {
				boolean [] plocals = ac.getLocalPlantEvents();
				last_plant_locals = new_plant_locals;

				if(bdd_local_events_plant  != -1) ba.deref(bdd_local_events_plant);
				bdd_local_events_plant = ba.getAlphabetSubsetAsBDD(ac.getLocalPlantEvents());

				if(Options.debug_on) {
					ba.getEventManager().dumpSubset("*** ModLC:(" + (new_plant_locals - last_locals) + " new) PlantLocalEvents", plocals);
				}
			}


			// update local events, if any new ones, check if we have early termination!
			int new_locals = ac.getNumOfLocalEvents();
			if(last_locals < new_locals ) {
				boolean [] locals = ac.getLocalEvents();
				if(Options.debug_on) ba.getEventManager().dumpSubset("*** ModLC:(" + (new_locals - last_locals) + " new) LocalEvents", locals);
				last_locals = new_locals;


				if(bdd_local_events  != -1) ba.deref(bdd_local_events);
				bdd_local_events = ba.getAlphabetSubsetAsBDD(locals);

				// Early termination check starts here
				int bdd_bad_enabled = ba.and(bdd_bad, bdd_local_events_plant);


				if(bdd_bad_enabled == ba.getZero() ) {
					// we cant proof the a half transition is enabled, better to skipp it all
					if(Options.debug_on) {
						Options.out.println("No enabled half-transitions, skipping local-reachability");
					}

				} else {
					// we can proof that some half transition are enabled, lets see if they are "bad" too:

					int Qreachable = sup.getReachables(locals);
					int tmp_bdd = ba.and(Qreachable, bdd_bad_enabled);
					ba.deref(Qreachable);

					if(tmp_bdd != ba.getZero() ) {
						if(Options.debug_on) {
							Options.out.println("*** ModLC:A bad state was reachable by a _local_ string");
							ba.show_events(bdd_local_events, "LocalEvents");
							ba.show_events(bdd_local_events_plant, "PlantLocalEvents");
							dump_failure(tmp_bdd);
						}
						ba.deref(tmp_bdd);
						return false;
					}

					ba.deref(tmp_bdd);
				}

				ba.deref(bdd_bad_enabled);

			}

		}

		if(Options.debug_on) {
			Options.out.println("Nothing more to add. Automaton must be uncontrollable");
			dump_failure(bdd_bad);
		}

		return false;
	}

}
