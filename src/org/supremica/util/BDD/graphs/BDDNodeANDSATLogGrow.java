package org.supremica.util.BDD.graphs;

import org.supremica.util.BDD.*;

import java.awt.*;
import java.awt.event.*;


/** Show the node grow AND the logaritmic satcount  in two different windows */
public class BDDNodeANDSATLogGrow extends BDDNodeGrow {
	private BDDSATLogGrow satlog = null;

	public BDDNodeANDSATLogGrow(BDDAutomata manager, String title) {
			super(manager, title);
			satlog = new BDDSATLogGrow(manager, title);
	}
	public void add(int bdd) {
		super.add(bdd);
		satlog.add(bdd);
	}

	public void startTimer() {
		super.startTimer();
		// if satlog = null, then we are still in the constructor of (this and )super
		if(satlog != null) satlog.startTimer();
	}
	public void stopTimer() {
		super.stopTimer();
		satlog.stopTimer();
	}
	public void mark(String txt) {
		super.mark(txt);
		satlog.mark(txt);

	}
}
