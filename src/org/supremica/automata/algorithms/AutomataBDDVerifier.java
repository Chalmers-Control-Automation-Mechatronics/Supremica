
package org.supremica.automata.algorithms;

import org.supremica.util.BDD.*;

/**
 * AutomataBDDVerifier, for verification with BDDs
 *
 */

public class AutomataBDDVerifier {
    private AutomataSynchronizerHelper.HelperData hd;
    private org.supremica.automata.Automata theAutomata;
    private BDDAutomata ba = null;
    private Supervisor sup = null;


    /**
     * creates a verification object
     * depeding one the type of algorithm used, this call might take a while
     * <b>DONT FORGET TO CALL cleanup() AFTERWARDS!!!</b>
     * @see cleanup()
     */
    public AutomataBDDVerifier(org.supremica.automata.Automata theAutomata,
			        AutomataSynchronizerHelper.HelperData hd)
	throws Exception
    {
		this.theAutomata = theAutomata;
		this.hd          = hd;

		try {
			Builder bu = new Builder(theAutomata);
			ba = bu.getBDDAutomata();
			sup = SupervisorFactory.createSupervisor(ba, ba.getAutomataVector());
		} catch(Exception pass) {
			cleanup();
			throw pass;
		}

    }


    /**
     * creates a verification object, uses only the given (subset of )Alphabet
     *
     * depeding one the family of the reachability algorithm used, this call might take a while
     *
     * <b>DONT FORGET TO CALL cleanup() AFTERWARDS!!!</b>
     * @see cleanup()
     */
    public AutomataBDDVerifier(org.supremica.automata.Automata theAutomata,
    	org.supremica.automata.Alphabet alphabet,
		AutomataSynchronizerHelper.HelperData hd)
	throws Exception
    {
		this.theAutomata = theAutomata;
		this.hd          = hd;

		try {
			Builder bu = new Builder(theAutomata, alphabet);
			ba = bu.getBDDAutomata();
			sup = SupervisorFactory.createSupervisor(ba, ba.getAutomataVector());
		} catch(Exception pass) {
			cleanup();
			throw pass;
		}

    }

    /**
     * creates a verification object for LANGUAGE INCLUSION test.
     * depeding one the type of algorithm used, this call might take a while
     * <b>DONT FORGET TO CALL cleanup() AFTERWARDS!!!</b>
     * @see cleanup()
     */
    public AutomataBDDVerifier(org.supremica.automata.Automata selected,
			       org.supremica.automata.Automata unselected,
			       AutomataSynchronizerHelper.HelperData hd)
	throws Exception
    {
		this.hd     = hd;
		theAutomata = new org.supremica.automata.Automata();
		theAutomata.addAutomata(selected);
		theAutomata.addAutomata(unselected);

		try {
			Builder bu = new Builder(theAutomata);
			ba = bu.getBDDAutomata();
			BDDAutomaton [] all = ba.getAutomataVector();

			Group g2 = new Group(ba, all, new AutomatonMembership(selected), "Selected");
			Group g1 = new Group(ba, all, new AutomatonMembership(unselected), "Unselected");

			sup = SupervisorFactory.createSupervisor(ba, g1, g2);
			// no need to cleanup g1 and g2 (cleaned up by sup)

		} catch(Exception pass) {
			cleanup();
			throw pass;
		}
    }

    /**
     * C++ style destructor.
     * <b>This function MUST be called before creating any new AutomataBDDVerifier obejcts</b>
     *
     */
    public void cleanup() {
		// Note: g1 and g2 are cleanup by sup
		if(sup != null) sup.cleanup();
		if(ba != null) ba.cleanup();
    }


	// ------------------------------------------------------------------------------------
	/* allows access to some stuff we really shouldnt access :) */
	public Supervisor  getSupervisor() { return sup; }
	public BDDAutomata  getBDDAutomata() { return ba; }
	public org.supremica.automata.Automata  getAutomata() { return theAutomata; }

	// -------------------------------------------------------------------------------

    /**
     *  BDD liveness check
     *
     * @return TRUE if the system is non-blocking
     */
    public boolean isNonBlocking() {
		boolean is_nonblocking = true;

		int r = sup.getReachables();
		if(hd != null)
		{
			hd.setNumberOfReachableStates((long)ba.count_states(r));
		}

		int c = sup.getCoReachables();

		int not_c = ba.not(c);
		int intersection = ba.and(r, not_c);

		if(hd != null)
		{
			hd.setNumberOfDeadlockedStates((long)ba.count_states(not_c));
			hd.setNumberOfCheckedStates((long)ba.count_states(intersection));
		}

		if(intersection != ba.getZero()) {
			is_nonblocking = false;
			if(Options.trace_on) {
			// show one trace to a blocking state
			sup.trace_set("blocking",intersection, 1);
			}
		}


		ba.deref(intersection);
		ba.deref(not_c);

		return is_nonblocking;
    }

    /**
     * BDD controllability check, monolithic
     *
     * @return TRUE if the system is controllable
     */
    public boolean isControllable() {
		int Q_u = sup.getSomeReachableUncontrollables();
		// int Q_u = sup.getReachableUncontrollables();
		// hd.setNumberOfForbiddenStates(ba.count_states(Q_u));

		boolean is_controllable = ba.getZero() == Q_u;
		if(!is_controllable) {
			if(Options.trace_on) {
			// show all uncontrollable states...
			// System.out.println("Uncontrollable states");
			// ba.show_states(Q_u);
			// ... and show how to get to one such state
			sup.trace_set("uncontrollable", Q_u, 1);
			}
		}

		ba.deref(Q_u); // we own this BDD
		return is_controllable;
    }

   /**
     * Language inclusion  check
     *
     * @return TRUE if the system a1 in a2 ?
     */
    public boolean passLanguageInclusion() {

		int states = sup.computeReachableLanguageDifference();
		boolean ret = (states == ba.getZero());



		// get statistics

		int Q_r = sup.getReachables();
		if(hd != null)
		{
			hd.setNumberOfCheckedStates((long)ba.count_states(states));
			hd.setNumberOfReachableStates((long)ba.count_states(Q_r));
		}

		if(!ret && Options.trace_on)
			sup.trace_set("Language Inclusion counterexample", states, 1);


		ba.deref(states);
		return ret;

    }




}
