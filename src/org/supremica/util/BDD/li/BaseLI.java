


package org.supremica.util.BDD.li;


import org.supremica.util.BDD.*;
// import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;


/**
 * base class for the language containment verification (LI/ctrl)
 */

public class BaseLI {

	// statistics stuff: (these are public static to be readed from other pbjects)
	public static int num_specs_checked;
	public static int num_syncs_done;
	public static int max_sync_automata;
	protected int num_automata_added;


	protected AutomataSynchronizerHelper.HelperData hd;
	protected org.supremica.automata.Automata theAutomata;


	/* package */ boolean [] considred_events;	/** this is the set of events that are of interest to the test */
	/* package */ boolean [] workset_events;	/** this is the set of (all) events in the subsysttem */
	/* package */ BDDAutomata ba = null;
	/* package */ BDDAutomaton [] all = null;
	/* package */ boolean [] is_spec; // true if automaton is spec

	/* package */ Group L1 = null;
	/* package */ Group L2 = null;
	/* package */ boolean controllaibilty_test;
	protected int bdd_cube_sp;

	// -------------------------------------------------------------------------------


	/**
	 * creates a verification object for LANGUAGE INCLUSION test.
	 * depeding one the type of algorithm used, this call might take a while
	 * <b>DONT FORGET TO CALL cleanup() AFTERWARDS!!!</b>
	 * @see cleanup()
     */

    public BaseLI(org.supremica.automata.Automata selected,
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


	// -------------------------------------------------------------------------------

    /**
     * creates a verification object for CONTROLLABILITY test.
     * depeding one the type of algorithm used, this call might take a while
     * <b>DONT FORGET TO CALL cleanup() AFTERWARDS!!!</b>
     * @see cleanup()
     */
	public BaseLI(org.supremica.automata.Automata theAutomata, AutomataSynchronizerHelper.HelperData hd) throws Exception {
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

	// -------------------------------------------------------------------------------

    /**
     * C++ style destructor.
     * <b>This function MUST be called before creating any new AutomataBDDVerifier obejcts</b>
     *
     */
    public void cleanup() {

		if(L1 != null) L1.cleanup();
		if(L2 != null) L2.cleanup();
		if(ba != null) ba.cleanup();
    }

	protected void init() {
		for(int i = 0; i < all.length; i++){
			all[i].extra1 = i; // index
			all[i].extra2 = 0; // type
		}

		bdd_cube_sp = ba.getStatepCube();
		is_spec = new boolean[ all.length ];
 		BDDAutomaton[] specs = L1.getMembers();
 		for(int i = 0; i < L1.getSize();i++) specs[i].extra2 = 1;
		for(int i = 0; i < all.length; i++) if(all[i].extra2 == 1) is_spec[i] = true;
	}

	// ------------------------------------------------------

	/**
	 * Modular controllability check
	 * @return TRUE if the system is controllable
	 */

	public boolean isControllable() {
		BDDAssert.internalCheck(controllaibilty_test, "INTERNAL ERROR, someone fucked up...");
		return passLanguageInclusion(); // same code...
	}

	/**
	 * Modular language inclusion check
	 * @return TRUE if the system a1 in a2 (eller var det tvärtom ?)
	 */
	public boolean passLanguageInclusion() {


		if(L2.getSize() == 0) {
			if(Options.debug_on) Options.out.println("*** LC: TRIVIAL PASS (no P)");
			return true;

		}
		BDDAutomaton[] l1 = L1.getMembers();
		int count = L1.getSize();
		boolean result = true;


		num_specs_checked = 0;
		num_syncs_done = 0;
		max_sync_automata = 0;


		for(int i = 0; (i < count) && result; i++)
		{
			BDDAutomaton k =  l1[i];

			if(Options.debug_on) {
				Options.out.println("\n\n----------------------------------------");
				Options.out.println("*** LC:Verifying " + k.getName() );
			}

			try {
				num_automata_added = 1;	// <-- the first one is k itself
				num_specs_checked++;

				Coverage ac = new Coverage(k, this);
				result = check(k, ac);

				if(num_automata_added > max_sync_automata)  max_sync_automata = num_automata_added ;

			} catch(TrivialPass tp) {
				if(Options.debug_on) Options.out.println("*** LC: TRIVIAL PASS: " + tp.getMessage());
			}

		}


		if(Options.profile_on) {
			Options.out.println("*** STATISTICS: "  + num_specs_checked + " specifications checked, " +
				num_syncs_done + " syncs done with at most " + 	max_sync_automata  + " automata.");
		}

		if(Options.debug_on) Options.out.println("*** Language containment test " + (result ? "passed" : "failed") + ".");

		return result;
	}

	/** this functions does nothing but testing the support classes */
	public boolean check(BDDAutomaton Xi, Coverage ac) {
		Options.out.println("should verify " + Xi.getName() );
		ac.dump();

		BDDAutomaton tmp;
		while( (tmp = ac.next() ) != null) {
			Options.out.print("\t ADDING " + tmp.getName() );
			if(ac.allPlantEventsIncluded()) Options.out.print(" ALL PLANTS ADDED");
			if(ac.lastWasSpec() ) Options.out.print(" (WAS SPEC)");
			Options.out.println();
		}

		Options.out.println("--------------------------------------------");

		return true;
	}

}