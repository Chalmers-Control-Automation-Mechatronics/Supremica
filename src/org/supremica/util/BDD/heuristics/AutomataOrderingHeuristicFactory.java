package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;


/**
 *
 * Factory class for the ordering between automata
 *
 */
public class AutomataOrderingHeuristicFactory {


	public static AutomataOrderingHeuristic createInstance(Automata a) throws BDDException {
		AutomataOrderingHeuristic aoh = null;

		switch(Options.ordering_algorithm) {
			case Options.AO_HEURISTIC_PCG:
				aoh = new AOH_PCG();
				break;

			case Options.AO_HEURISTIC_RANDOM:
				aoh = new AOH_Random();
				break;

			// all these use the same engine, the solver is different and the selection is handled internally...
			case Options.AO_HEURISTIC_TSP:
			case Options.AO_HEURISTIC_DFS:
			case Options.AO_HEURISTIC_BFS:
			case Options.AO_HEURISTIC_STCT:
			case Options.AO_HEURISTIC_TSP_STCT:
			case Options.AO_HEURISTIC_TSP_SIFT:
				aoh = new AOH_Solver();
				break;
		 	default:
		 		System.err.println("[INTERNAL] BAD AutomatonSelectionHeuristic type...");
		 		return null;
		}
		aoh.init(a);
		return aoh;
	}

	public static String getName() {
		return Options.ORDERING_ALGORITHM_NAMES[ Options.ordering_algorithm] ;
	}
}
