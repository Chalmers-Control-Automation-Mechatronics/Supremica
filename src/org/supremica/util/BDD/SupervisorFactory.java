
/**
 * This factory class creates a Supervisor of the type requested by Options.algo_family
 *
 *
 */
package org.supremica.util.BDD;

import org.supremica.util.SupremicaException;

public class SupervisorFactory
{

	// -----------------------------------------------------------------------------------
	public static Supervisor createSupervisor(BDDAutomata manager, BDDAutomaton[] automata)
		throws Exception
	{
		switch (Options.algo_family)
		{

		case Options.ALGO_MONOLITHIC :
			return new Supervisor(manager, automata);

		case Options.ALGO_CONJUNCTIVE :
			return new ConjSupervisor(manager, automata);

		case Options.ALGO_CONJUNCTIVE_LOCAL_EVENT :
			return new LocalEventSupervisor(manager, automata);

		case Options.ALGO_DISJUNCTIVE :
			return new DisjSupervisor(manager, automata);

		case Options.ALGO_DISJUNCTIVE_WORKSET :
			return new WorksetSupervisor(manager, automata);

		case Options.ALGO_DISJUNCTIVE_STEPSTONE :
			return new StepStoneSupervisor(manager, automata);

		case Options.ALGO_SMOOTHED_MONO :
			return new SmoothSupervisor(manager, automata);

		case Options.ALGO_SMOOTHED_DELAYED_MONO :
			return new DelayedSmoothSupervisor(manager, automata);

		case Options.ALGO_SMOOTHED_DELAYED_STAR_MONO :
			return new DelayedStarSmoothSupervisor(manager, automata);

		case Options.ALGO_SMOOTHED_MONO_WORKSET :
			return new SmoothWorksetSupervisor(manager, automata);

		case Options.ALGO_SMOOTHED_PATH :
			return new PathSmoothSupervisor(manager, automata);

		case Options.ALGO_SMOOTHED_KEEP :
			return new KeepSmoothSupervisor(manager, automata);

		case Options.ALGO_SMOOTHED_PART :
			return new PartitionSmoothSupervisor(manager, automata, true);

		case Options.ALGO_SMOOTHED_PART2 :
			return new PartitionSmoothSupervisor(manager, automata, false);

		case Options.ALGO_PETRINET :
			return new PetriNetSupervisor(manager, automata);
		}

		// the type is not supported:
		throw new SupremicaException("Current BDD algorithm family not implemented");
	}

	// ----------------------------------------------------------------------------------
	public static Supervisor createSupervisor(BDDAutomata manager, Group plant, Group spec)
		throws Exception
	{
		switch (Options.algo_family)
		{

		case Options.ALGO_MONOLITHIC :
			return new Supervisor(manager, plant, spec);

		case Options.ALGO_CONJUNCTIVE :
			return new ConjSupervisor(manager, plant, spec);

		case Options.ALGO_CONJUNCTIVE_LOCAL_EVENT :
			return new LocalEventSupervisor(manager, plant, spec);

		case Options.ALGO_DISJUNCTIVE :
			return new DisjSupervisor(manager, plant, spec);

		case Options.ALGO_DISJUNCTIVE_WORKSET :
			return new WorksetSupervisor(manager, plant, spec);

		case Options.ALGO_DISJUNCTIVE_STEPSTONE :
			return new StepStoneSupervisor(manager, plant, spec);

		case Options.ALGO_SMOOTHED_MONO :
			return new SmoothSupervisor(manager, plant, spec);

		case Options.ALGO_SMOOTHED_DELAYED_MONO :
			return new DelayedSmoothSupervisor(manager, plant, spec);

		case Options.ALGO_SMOOTHED_DELAYED_STAR_MONO :
			return new DelayedStarSmoothSupervisor(manager, plant, spec);

		case Options.ALGO_SMOOTHED_MONO_WORKSET :
			return new SmoothWorksetSupervisor(manager, plant, spec);

		case Options.ALGO_SMOOTHED_PATH :
			return new PathSmoothSupervisor(manager, plant, spec);

		case Options.ALGO_SMOOTHED_KEEP :
			return new KeepSmoothSupervisor(manager, plant, spec);

		case Options.ALGO_SMOOTHED_PART :
			return new PartitionSmoothSupervisor(manager, plant, spec, true);

		case Options.ALGO_SMOOTHED_PART2 :
			return new PartitionSmoothSupervisor(manager, plant, spec, false);

		case Options.ALGO_PETRINET :
			return new PetriNetSupervisor(manager, plant, spec);
		}

		// the type is not supported:
		throw new SupremicaException("Current BDD algorithm family not implemented");
	}

	/**
	 * currently, the smoothing algorithm does not work:
	 * choose another one based on the size of the system
	 */
	public static Supervisor suggestSupervisorForModularReachability(BDDAutomata manager, Group plant, Group spec)
		throws Exception
	{
		int s1 = plant.getSize();
		int s2 = spec.getSize();

		if ((s1 > Options.MAX_MONOLITHIC_GROUP_SIZE) || (s2 > Options.MAX_MONOLITHIC_GROUP_SIZE) || (s1 + s2) > Options.MAX_MONOLITHIC_TOTAL_SIZE)
		{
			return new SimplePartitionSupervisor(manager, plant, spec);

			// return new ConjSupervisor(manager,plant, spec);
			// return new PetriNetSupervisor(manager,plant, spec);
			// System.out.println("\n\n\n----------------- SWITHICHNG\n\n\n\n");
			// return new WorksetSupervisor(manager,plant, spec);
		}
		else
		{
			return new Supervisor(manager, plant, spec);
		}
	}
}
