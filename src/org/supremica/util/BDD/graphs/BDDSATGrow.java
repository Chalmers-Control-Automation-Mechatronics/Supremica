


package org.supremica.util.BDD.graphs;

import org.supremica.util.BDD.*;

/** show the number of minterms (i.e. the size of SAT) for each tree */
public class BDDSATGrow extends GrowFrame {
	private BDDAutomata manager;
	public BDDSATGrow(BDDAutomata manager, String title) {
		super("SAT/"+title);
		this.manager = manager;
	}
	public void add(int bdd) { super.add( (int) manager.count_states( bdd) ); }
}


