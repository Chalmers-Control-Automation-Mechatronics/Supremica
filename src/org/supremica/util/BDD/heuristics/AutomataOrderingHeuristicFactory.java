


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
			case Options.AS_HEURISTIC_PCG:
				aoh = new AOH_PCG();
				break;

			case Options.AO_HEURISTIC_RANDOM:
				aoh = new AOH_Random();
				break;

			case Options.AS_HEURISTIC_TSP:
				aoh = new AOH_TSP();
				break;
		 	default:
		 		System.err.println("[INTERNAL] BAD AutomatonSelectionHeuristic type...");
		 		return null;
		}


		aoh.init(a);
		return aoh;
	}
}
