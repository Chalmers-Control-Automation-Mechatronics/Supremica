

package org.supremica.util.BDD.graphs;

import org.supremica.util.BDD.*;
import java.awt.*;
import java.awt.event.*;

/** show the number of nodes in the trees (log scale)*/
public class BDDNodeLogGrow extends GrowFrame {
	private BDDAutomata manager;
	private double log10 =  Math.log(10);
	public BDDNodeLogGrow(BDDAutomata manager, String title) {
		super("1000*NODElog/"+title);
		this.manager = manager;
	}
	public void add(int bdd) {
		double d = manager.nodeCount( bdd);
		super.add( d > 0 ? (int)(1000*Math.log(d) / log10 ) : 0 );
	}
}

