
package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;


public class AutomatonSelectionHeuristicFactory {


	public static AutomatonSelectionHeuristic createInstance(BDDAutomaton [] list, int [] queue, double [] queue_costs) {
		AutomatonSelectionHeuristic ash = null;


		switch(Options.as_heuristics) {
			case Options.AS_HEURISTIC_MOST_COMMON_UC_EVENTS:
				ash = new ASH_MostCommonUncontrollableEvents();
				break;
			case Options.AS_HEURISTIC_MOST_COMMON_UC_ARCS:
				ash = new ASH_MostCommonUncontrollableArcs();
				break;
			case Options.AS_HEURISTIC_MOST_COMMON_EVENTS:
				ash = new ASH_MostCommonEvents();
				break;
			case Options.AS_HEURISTIC_MOST_COMMON_ARCS:
				ash = new ASH_MostCommonArcs();
				break;
			case Options.AS_HEURISTIC_FIFO:
				ash = new ASH_Linear(true);
				break;
			case Options.AS_HEURISTIC_STACK:
				ash = new ASH_Linear(false);
				break;
			case Options.AS_HEURISTIC_DISTANCE:
				ash = new ASH_Distance();
				break;
			case Options.AS_HEURISTIC_RANDOM:
				ash = new ASH_Random();
		 		break;
		 	case Options.AS_HEURISTIC_MOST_LOCAL:
		 		ash = new ASH_MostLocal();
		 		break;
		 	case Options.AS_HEURISTIC_HYBRID:
		 		ash = new ASH_Hybrid();
		 		break;
		 	default:
		 		System.err.println("[INTERNAL] BAD AutomatonSelectionHeuristic type...");
		 		return null;
		}


		ash.init(list, queue, queue_costs);
		return ash;
	}
}
