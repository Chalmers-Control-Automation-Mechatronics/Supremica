
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

	case Options.ALGO_CONJUNCTIVE: return new ConjSupervisor(manager,automata);

	case Options.ALGO_DISJUNCTIVE: return new DisjSupervisor(manager,automata);
	case Options.ALGO_DISJUNCTIVE_WORKSET: return new WorksetSupervisor(manager,automata);

	case Options.ALGO_SMOOTHED_MONO: return new SmoothSupervisor(manager,automata);
	case Options.ALGO_SMOOTHED_PATH: return new PathSmoothSupervisor(manager,automata);
	case Options.ALGO_SMOOTHED_KEEP: return new KeepSmoothSupervisor(manager,automata);
	case Options.ALGO_SMOOTHED_PART: return new PartitionSmoothSupervisor(manager,automata);

	}

	// the type is not supported:
	throw new Exception("Current BDD algorithm family not implemented");
    }

    // ----------------------------------------------------------------------------------
       public static Supervisor createSupervisor(BDDAutomata manager, Group plant, Group spec)
	throws Exception
    {
	switch(Options.algo_family) {
	case Options.ALGO_MONOLITHIC: return new Supervisor(manager, plant,spec);

	case Options.ALGO_CONJUNCTIVE: return new ConjSupervisor(manager,plant, spec);

 	case Options.ALGO_DISJUNCTIVE: return new DisjSupervisor(manager,plant, spec);
 	case Options.ALGO_DISJUNCTIVE_WORKSET: return new WorksetSupervisor(manager,plant, spec);

	case Options.ALGO_SMOOTHED_MONO: return new SmoothSupervisor(manager,plant, spec);
	case Options.ALGO_SMOOTHED_PATH: return new PathSmoothSupervisor(manager,plant, spec);
	case Options.ALGO_SMOOTHED_KEEP: return new KeepSmoothSupervisor(manager,plant, spec);
	case Options.ALGO_SMOOTHED_PART: return new PartitionSmoothSupervisor(manager,plant, spec);

	}

	// the type is not supported:
	throw new Exception("Current BDD algorithm family not implemented");
    }
}
