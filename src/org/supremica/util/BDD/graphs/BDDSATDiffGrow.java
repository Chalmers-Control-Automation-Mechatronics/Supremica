


package org.supremica.util.BDD.graphs;

import org.supremica.util.BDD.*;

import java.awt.*;
import java.awt.event.*;

/** show the difference in number of minterms (i.e. the size of SAT) for each tree */
public class BDDSATDiffGrow extends GrowFrame {
	private BDDAutomata manager;
	private double last = 0;
	public BDDSATDiffGrow(BDDAutomata manager, String title) {
		super("SATdiff/"+title);
		this.manager = manager;
	}
	public void add(int bdd) {
		double d = manager.count_states( bdd);
		super.add( (int)( d - last));
		last = d;
	}
}







