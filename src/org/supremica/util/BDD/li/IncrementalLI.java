


package org.supremica.util.BDD.li;


import org.supremica.util.BDD.*;
// import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;


public class IncrementalLI extends BaseLI {


    public IncrementalLI(org.supremica.automata.Automata selected, org.supremica.automata.Automata unselected, AutomataSynchronizerHelper.HelperData hd) throws Exception {
		super(selected, unselected, hd);
	}

	public IncrementalLI(org.supremica.automata.Automata automata, AutomataSynchronizerHelper.HelperData hd) throws Exception {
		super(automata, hd);
	}


	// -----------------------------------------------------------------------------------

	private int bdd_sigma_w = -1;
	private int bdd_theta_i = -1;
	private int bdd_theta_j = -1;
	private int bdd_theta   = -1;
	private int bdd_initial = -1;
	private int bdd_local_events  = -1;
	private int bdd_local_events_plant = -1;

	private Supervisor  sup = null;
	private Group Ip = null;
	private Group Jp = null;

	public void cleanup() {
		bdd_cleanup();
		super.cleanup();
	}

	private void bdd_cleanup() {
		if(bdd_sigma_w != -1) { ba.deref(bdd_sigma_w ); bdd_sigma_w  = -1; }
		if(bdd_theta_i != -1) { ba.deref(bdd_theta_i ); bdd_theta_i  = -1; }
		if(bdd_theta_j != -1) { ba.deref(bdd_theta_j ); bdd_theta_j  = -1; }
		if(bdd_theta   != -1) { ba.deref(bdd_theta   ); bdd_theta    = -1; }
		if(bdd_initial != -1) { ba.deref(bdd_initial ); bdd_initial  = -1; }

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




		init_theta(Xi);	// setup \Theta

		bdd_initial = ba.ref( Xi.getI() );

		int last_locals = 0;
		int last_plant_locals = 0;
		bdd_local_events_plant = ba.ref( ba.getZero() );
		bdd_local_events = ba.ref( ba.getZero() );

		BDDAutomaton next;
		while( (next = ac.next() ) != null) {
			num_automata_added++;
			num_syncs_done++;

			if(Options.debug_on) Options.out.println("\t +++ Adding " + next.getName() + "\n");

			update_theta(next, ac.lastWasSpec() );


			// Extend our set of initial states
			bdd_initial = ba.andTo(bdd_initial , next.getI() );

			if(sup != null) { sup.cleanup(); sup = null; }
			try {
				sup = SupervisorFactory.suggestSupervisorForModularReachability(ba, Jp, Ip);
			} catch(Exception exx) {
				exx.printStackTrace();
				return false;
			}


			// More verbose crap
			if(Options.debug_on) ac.dump();

			// test over-approximated reachability of bad states (as in theta)
			int Qr = sup.getReachables(bdd_initial);
			bdd_theta = ba.andTo(bdd_theta, Qr);
			boolean ret = (bdd_theta == ba.getZero());

			// all nc-arcs where considred and found to be unreachable
			if(ret && ac.allPlantEventsIncluded()) {
				if(Options.debug_on) ba.getEventManager().dumpSubset("*** Removed events (all)", workset_events);
				return true;
			}


			// remove events not used anymore. NOTE: this snippet is not as inefficient as it looks :)
			int theta_sigma = ba.exists(bdd_theta, ba.getStateCube() );
			Event [] es =  ba.getEvents();
			int changed = 0;
			for(int i = 0; i < workset_events.length; i++) {
				if(workset_events[i]) {
					int tmp = ba.and( theta_sigma, es[i].getBDD() );
					boolean empty = (tmp == ba.getZero() );
					ba.deref(tmp);
					if(empty) {
						changed++;
						workset_events[i] = false;
					}
				}
			}
			ba.deref( theta_sigma );
			if(changed > 0) {
				ba.deref(bdd_sigma_w );
				bdd_sigma_w = ba.getAlphabetSubsetAsBDD(workset_events);
				bdd_theta_i = ba.andTo(bdd_theta_i, bdd_sigma_w);
				bdd_theta_j = ba.andTo(bdd_theta_j, bdd_sigma_w);
				// bdd_theta alread updated.
				if(Options.debug_on) Options.out.println("*** Removed " + changed + " events from workset-events.");
			}





			// update P-local events
			int new_plant_locals = ac.getNumOfLocalPlantEvents();
			if(last_plant_locals  <  new_plant_locals ) {
				boolean [] plocals = ac.getLocalPlantEvents();
				if(Options.debug_on) ba.getEventManager().dumpSubset("*** IncrLC:(" +
							(new_plant_locals - last_locals) + " new) PlantLocalEvents", plocals);
				last_plant_locals = new_plant_locals;

				if(bdd_local_events_plant  != -1) ba.deref(bdd_local_events_plant);
				bdd_local_events_plant = ba.getAlphabetSubsetAsBDD(ac.getLocalPlantEvents());
			}


			// update local events, if any new ones, check if we have early termination!
			int new_locals = ac.getNumOfLocalEvents();
			if(last_locals < new_locals ) {
				boolean [] locals = ac.getLocalEvents();
				if(Options.debug_on) ba.getEventManager().dumpSubset("*** IncrLC:(" + (new_locals - last_locals) + " new) LocalEvents", locals);
				last_locals = new_locals;


				if(bdd_local_events  != -1) ba.deref(bdd_local_events);
				bdd_local_events = ba.getAlphabetSubsetAsBDD(locals);

				// Early termination check starts here
				int Qreachable = sup.getReachables(locals);
				int bdd_theta_enabled = ba.and(bdd_theta, bdd_local_events_plant);
				int tmp_bdd = ba.and(bdd_theta_enabled, Qreachable);
				boolean not_exist = (tmp_bdd == ba.getZero());

				ba.deref(tmp_bdd);
				ba.deref(bdd_theta_enabled);
				ba.deref(bdd_initial);

				bdd_initial = Qreachable;

				if(!not_exist ) {
					if(Options.debug_on) {
						Options.out.println("*** IncrLC:A bad state was reachable by a _local_ string");
						ba.show_events(bdd_local_events, "LocalEvents");
						ba.show_events(bdd_local_events_plant, "PlantLocalEvents");
					}
					return false;
				}

			} else if(last_locals > 0) {
				// maybe the initial state is among them, no need for any local transitions then?
				int tmp = ba.and(bdd_initial, bdd_theta);
				if(tmp != ba.getZero() ) {
					int tmp2 = ba.and(tmp, bdd_local_events_plant);
					boolean enabled = tmp2 != ba.getOne();
					ba.deref(tmp2);
					if(enabled) {
						ba.deref(tmp);
						if(Options.debug_on)  Options.out.println("*** IncrLC:The initial state is in Theta and P-enabled");
						return false;
					}
				}
				ba.deref(tmp);
			}


		}

		if(Options.debug_on) Options.out.println("Nothing more to add. Automaton must be uncontrollable");
		return false;
	}

	// ---------------------------------------------------------------------------------------

	/** init Theta for the first round */
	private void init_theta(BDDAutomaton Xi) {

		bdd_sigma_w = ba.getAlphabetSubsetAsBDD(workset_events);

		if(controllaibilty_test) {
			bdd_theta_j = ba.ref( bdd_sigma_w) ;

			int tmp1 = ba.exists(Xi.getTpri(), bdd_cube_sp );
			int tmp2 = ba.not(tmp1);
			ba.deref(tmp1);
			bdd_theta_i = ba.and(tmp2, Xi.getCareS());
			ba.deref(tmp2);

			bdd_theta = ba.and(bdd_theta_i, bdd_theta_j);
		} else {
			// if not ctrl-test, we wont need theta_i and theta_j
			int tmp1 = ba.exists(Xi.getTpri(), bdd_cube_sp );
			int tmp2 = ba.not(tmp1); 	ba.deref(tmp1);
			tmp1 = ba.and(tmp2, Xi.getCareS()); ba.deref(tmp2);

			bdd_theta = ba.and(tmp1, bdd_sigma_w );
		}
	}

	/** update Theta when a new automaton has been added */
	private void update_theta(BDDAutomaton next, boolean is_spec) {
		if(controllaibilty_test) {
			if(is_spec) {
				Ip.add(next);
				// update theta_Sp
				int tmp1 = ba.exists(next.getTpri(), bdd_cube_sp );
				int tmp2 = ba.not(tmp1); 					ba.deref(tmp1);
				tmp1 = ba.and(tmp2, next.getCareS() ); 	ba.deref(tmp2);
				bdd_theta_i = ba.orTo(bdd_theta_i, tmp1);

			} else {
				Jp.add(next);
				int tmp1 = ba.exists(next.getTpri(), bdd_cube_sp );
				int tmp2 = ba.and(tmp1, bdd_sigma_w); 	ba.deref(tmp1);
				bdd_theta_j = ba.andTo(bdd_theta_j, tmp2);		ba.deref(tmp2);
			}
			ba.deref(bdd_theta);
			bdd_theta = ba.and(bdd_theta_i, bdd_theta_j);
		} else {
			// if not ctrl-test, then it is only plants we will add
			Jp.add(next);
			int tmp1 = ba.exists(next.getTpri(), bdd_cube_sp );
			int tmp2 = ba.and(tmp1, bdd_sigma_w); 	ba.deref(tmp1);
			bdd_theta = ba.andTo(bdd_theta, tmp2);	ba.deref(tmp2);
		}
	}
}
