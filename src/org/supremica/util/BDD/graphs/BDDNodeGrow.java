
package org.supremica.util.BDD.graphs;

import org.supremica.util.BDD.*;

import java.awt.*;
import java.awt.event.*;


/** show the number of nodes in the trees */
public class BDDNodeGrow extends GrowFrame {
	private BDDAutomata manager;
	public static BDDNodeGrow last = null;
	public BDDNodeGrow(BDDAutomata manager, String title) {
		super("NODE/"+title);
		this.manager = manager;
		last = this;
	}

	public void add(int bdd) { super.add( manager.nodeCount( bdd) ); }
	public void stopTimer() {
		super.stopTimer();
		// compute graph cost: \Sum_i (y_i) ^2
		double cost = 0;
		int max = vars.getSize();

		for(int i = 0; i < max; i++) cost += (vars.get(i)) << 1;


		String extra = "; total cost: " + cost;
		if(last_value > 0){
			int over = ((100 * max_value) / last_value) - 100;
			if(over != 0) extra = extra + "; overshoot: " + over + "%";
		}

		status.setText(status.getText() + extra);
		title = title + extra;


	}
}
