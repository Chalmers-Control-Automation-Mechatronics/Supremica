

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;
import org.supremica.util.BDD.solvers.*;

import java.util.*;

/**
 * ordering, decided by a solver
 */

public class AOH_Solver extends AutomataOrderingHeuristic  {
	private int [] order;

	public int [] ordering() { return order; }

	public void init(Automata a) throws BDDException {
		Vector automata = a.getAutomata();
		PCG pcg = new PCG(automata);


    	int [][]weightMatrix = a.getCommunicationMatrix();
		OrderingSolver os = new OrderingSolver(automata.size() );

		int i = 0;
		for (Enumeration e = automata.elements(); e.hasMoreElements(); i++) {
			Automaton a1 = (Automaton) e.nextElement();
			os.addNode(a1, weightMatrix[i], i-1);
		}
		order = os.getGoodOrder();
	}
}


