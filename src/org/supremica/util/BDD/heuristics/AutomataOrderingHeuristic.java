

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * interface for automata-ordering heuristics
 *
 */

public abstract class AutomataOrderingHeuristic {
	public abstract int [] ordering();
	public abstract void init(Automata a)  throws BDDException ;
}