
package org.supremica.util.BDD;

import java.awt.*;
import java.awt.event.*;

/**
 * This is a sort of 'factory' class for the BDD grow graphs.
 * It will return the corret type of the graph, or null if none
 * needed.
 *
 * the two proxy classes are defined below in this file.
 *
 * TODO:
 * SAT-count is given as a 'double', but then catsed and converted to an 'int'.
 * we might need to use log-scale, otherwise that 'int' will overflow very easy.
 */

public class BDDGrow {
	static GrowFrame getGrowFrame(BDDAutomata manager, String title) {
		switch(Options.show_grow) {
			case Options.SHOW_GROW_NODES: return new BDDNodeGrow(manager, title);
			case Options.SHOW_GROW_NODES_LOG: return new BDDNodeLogGrow(manager, title);
			case Options.SHOW_GROW_NODES_DIFF: return new BDDNodeDiffGrow(manager, title);
			case Options.SHOW_GROW_SATCOUNT: return new BDDSATGrow(manager, title);
			case Options.SHOW_GROW_SATCOUNT_LOG: return new BDDSATLogGrow(manager, title);
			case Options.SHOW_GROW_SATCOUNT_DIFF: return new BDDSATDiffGrow(manager, title);
			default:
				return null;
		}

	}
}

/** show the number of nodes in the trees */
class BDDNodeGrow extends GrowFrame {
	private BDDAutomata manager;
	public BDDNodeGrow(BDDAutomata manager, String title) {
		super("NODE/"+title);
		this.manager = manager;
	}
	public void add(int bdd) { super.add( manager.nodeCount( bdd) ); }
}


/** show the number of nodes in the trees (log scale)*/
class BDDNodeLogGrow extends GrowFrame {
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

/** show the number of nodes in the trees */
class BDDNodeDiffGrow extends GrowFrame {
	private BDDAutomata manager;
	private int last = 0;
	public BDDNodeDiffGrow(BDDAutomata manager, String title) {
		super("NODEdiff/"+title);
		this.manager = manager;
	}
	public void add(int bdd) {
		int curr = manager.nodeCount( bdd);
		super.add( curr - last);
		last = curr;
	}
}

/** show the number of minterms (i.e. the size of SAT) for each tree */
class BDDSATGrow extends GrowFrame {
	private BDDAutomata manager;
	public BDDSATGrow(BDDAutomata manager, String title) {
		super("SAT/"+title);
		this.manager = manager;
	}
	public void add(int bdd) { super.add( (int) manager.count_states( bdd) ); }
}



/** show the [1000 * log_10] number of minterms (i.e. the size of SAT) for each tree */
class BDDSATLogGrow extends GrowFrame {
	private BDDAutomata manager;
	private double log10 =  Math.log(10);
	public BDDSATLogGrow(BDDAutomata manager, String title) {
		super("1000*SATlog/"+title);
		this.manager = manager;
	}
	public void add(int bdd) {
		double d = manager.count_states( bdd);
		if(d > 0) super.add((int) (1000 * Math.log(d) / log10) );
	}
}

/** show the difference in number of minterms (i.e. the size of SAT) for each tree */
class BDDSATDiffGrow extends GrowFrame {
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
