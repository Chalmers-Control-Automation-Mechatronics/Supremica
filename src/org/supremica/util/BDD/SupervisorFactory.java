
/**
 * This factory class creates a Supervisor of the type requested by Options.algo_family
 *
 *
 */

package org.supremica.util.BDD;

public class SupervisorFactory {

    // -----------------------------------------------------------------------------------
    public static Supervisor createSupervisor(BDDAutomata manager, BDDAutomaton[] automata) 
	throws Exception
    {
	switch(Options.algo_family) {
	case Options.ALGO_MONOLITHIC: return new Supervisor(manager, automata);	    
	}

	// the type is not supported:
	throw new Exception("Current algorithm family not implemented");	
    }

    // ----------------------------------------------------------------------------------
       public static Supervisor createSupervisor(BDDAutomata manager, Group plant, Group spec)
	throws Exception
    {
	switch(Options.algo_family) {
	case Options.ALGO_MONOLITHIC: return new Supervisor(manager, plant,spec);	    
	}

	// the type is not supported:
	throw new Exception("Current algorithm family not implemented");	
    }
}
