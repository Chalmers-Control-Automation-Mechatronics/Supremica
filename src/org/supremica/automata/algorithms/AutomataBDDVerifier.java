
package org.supremica.automata.algorithms;


import org.supremica.automata.*;
import org.supremica.util.BDD.*;

import java.util.*;

/**
 * AutomataBDDVerifier, for verification with BDDs
 *
 */

public class AutomataBDDVerifier {
    private AutomataSynchronizerHelper.HelperData hd;
    private  org.supremica.automata.Automata theAutomata;
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
	    // sup = new Supervisor(ba, ba.getAutomataVector());
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
	if(sup != null) sup.cleanup();
	if(ba != null) ba.cleanup();
    }




    /**
     *  BDD liveness check
     *
     * @return TRUE if the system is non-blocking
     */
    public boolean isNonBlocking() {
	boolean is_nonblocking = true;

	int r = sup.getReachables();  
	hd.setNumberOfReachableStates(ba.count_states(r));

	int c = sup.getCoReachables();

	int not_c = ba.not(c);
	int intersection = ba.and(r, not_c);

	hd.setNumberOfDeadlockedStates(ba.count_states(not_c));
	hd. setNumberOfCheckedStates(ba.count_states(intersection));

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


	ba.deref(intersection);
	ba.deref(not_c);

	return is_nonblocking;
    }

    /**
     * BDD controllability check
     *
     * @return TRUE if the system is controllable
     */
    public boolean isControllable() {
	int Q_u = sup.getReachableUncontrollables();
	// Q_u = ba.removeDontCareS(Q_u);



	// get statistics
	int Q_c = sup.getUncontrollableStates();
	int Q_r = sup.getReachables();

	hd.setNumberOfForbiddenStates(ba.count_states(Q_u));
	hd.setNumberOfCheckedStates(ba.count_states(Q_c));
	hd.setNumberOfReachableStates(ba.count_states(Q_r));

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

   /**
     * Language inclusion  check
     *
     * @return TRUE if the system a1 in a1
     */
    public boolean passLanguageInclusion() {
	
	int states = sup.computeReachableLanguageDifference();
	boolean ret = (states == ba.getZero());



	// get statistics

	int Q_r = sup.getReachables();
	hd.setNumberOfCheckedStates(ba.count_states(states));
	hd.setNumberOfReachableStates(ba.count_states(Q_r));

	if(!ret && Options.trace_on) 
	    sup.trace_set("Language Inclusion counterexample", states, 1);            


	ba.deref(states);

	return ret;

    }

    /**
     * Accepts a BDDAutomaton ba if it has the same name as one
     * of the Supremica Automata in theAutomata
     */
    class AutomatonMembership implements GroupMembership {
	private org.supremica.automata.Automata theAutomata;
	AutomatonMembership(org.supremica.automata.Automata a) {
	    theAutomata = a;
	}
	public boolean shouldInclude(BDDAutomaton a) {
	    String name = a.getName();

	    Iterator autIt = theAutomata.iterator();	    
	    while (autIt.hasNext())
		{
		    org.supremica.automata.Automaton supa = (org.supremica.automata.Automaton) autIt.next();
		    if(name.equals(supa.getName()))
			return true;
		}
	    return false;
	}
	
    }

}
