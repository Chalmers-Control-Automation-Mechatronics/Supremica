
package org.supremica.automata.algorithms;


import org.supremica.automata.*;
import org.supremica.util.BDD.*;

/**
 * AutomataBDDVerifier, for verification with BDDs
 *
 */

public class AutomataBDDVerifier {
    private  org.supremica.automata.Automata theAutomata;
    private BDDAutomata ba;
    private Supervisor sup;

    /**
     * creates a verification object
     * depeding one the type of algorithm used, this call might take a while
     * <b>DONT FORGET TO CALL cleanup() AFTERWARDS!!!</b>
     * @see cleanup()
     */
    public AutomataBDDVerifier(org.supremica.automata.Automata theAutomata) {
	this.theAutomata = theAutomata;

	Builder bu = new Builder(theAutomata);
	ba = bu.getBDDAutomata();
	sup = new Supervisor(ba, ba.getAutomataVector());
	
    }

    /**
     * C++ style destructor.
     * <b>This function MUST be called before creating any new AutomataBDDVerifier obejcts</b>
     *
     */ 
    public void cleanup() {
	sup.cleanup();
	ba.cleanup();
    }

    // ------------------------------------------
    /**
     * Monolithic BDD liveness check
     *
     * @return TRUE if the system is non-blocking
     */
    public boolean isNonBlocking() {
	boolean is_nonblocking = true;
	int r = sup.getReachables();  
	int c = sup.getCoReachables();

	int not_c = ba.not(c);
	int intersection = ba.and(r, not_c);


	// DEBUG
	// System.out.println("Reachables:" );
	// ba.show_states(r);
	
	// System.out.println("Co-Reachables:");
	// ba.show_states(c);

	if(intersection != ba.getZero()) {
	    is_nonblocking = false;
	    if(Options.trace_on) {
	    // show one trace to a blocking state
		sup.trace_set("non-blocking",intersection, 1);
	    }
	}

	ba.recursiveDeref(intersection);
	ba.recursiveDeref(not_c);
	return is_nonblocking;
    }

    /**
     * Monolithic BDD controllability check
     *
     * @return TRUE if the system is controllable
     */
    public boolean isControllable() {
	int Q_u = sup.getReachableUncontrollables();
	Q_u = ba.removeDontCareS(Q_u);
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
	

	return is_controllable;

    }

}
