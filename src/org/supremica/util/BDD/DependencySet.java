
package org.supremica.util.BDD;

import java.util.*;
import java.io.*;

public class DependencySet {
    private BDDAutomata manager;
    private BDDAutomaton [] all, dependent;
    private BDDAutomaton me;

    private int bdd_keep_depend, bdd_keep_others, bdd_t_wave;

    public DependencySet(BDDAutomata manager, BDDAutomaton me) {
	this.manager = manager;
	this.all = manager.getAutomataVector();
	this.me  = me;


	// SizeWatch.setOwner( getName());

	// get dependet set, compute bdd_keep_others at the same time

	Vector tmp = new Vector(all.length);
	bdd_keep_others = manager.getOne(); manager.ref(bdd_keep_others);
	for(int i = 0; i < all.length; i++) {
	    if(all[i] != me) {
		if(me.interact(all[i]))
		   tmp.add( all[i]);
		else
		    bdd_keep_others = manager.andTo(bdd_keep_others,
						    all[i].getKeep());
	    }
	}

	// SizeWatch.report(bdd_keep_others, "keep_others");

	// put it into an array
	int len = tmp.size();
	dependent = new BDDAutomaton[len];
	int i = 0;
	for(Enumeration it = tmp.elements(); it.hasMoreElements(); i++) {
	    BDDAutomaton ba = (BDDAutomaton) it.nextElement();
	    dependent[i] = ba;
	}



	// calc Twave
	int follow = manager.getOne(); manager.ref(follow);
	for(i = 0; i < len; i++) {
	    BDDAutomaton a_i =  dependent[len-i-1];
	    int common_events =  manager.and(a_i.getSigma(), me.getSigma());
	    int uncommon_events = manager.not(common_events);

	    int move = manager.and(a_i.getT(), common_events);
	    int keep = manager.and(a_i.getKeep(), uncommon_events);

	    manager.deref(common_events);
	    manager.deref(uncommon_events);

	    int dep_move = manager.or(move,keep);
	    manager.deref(move);
	    manager.deref(keep);

	    // SizeWatch.report(dep_move, "dep-move_" + a_i.getName());
	    follow = manager.andTo(follow, dep_move);
	    manager.deref(dep_move);
	}


       int tmp3 = manager.and(me.getT(), follow);
	manager.deref(follow);



	tmp3 = manager.andTo(tmp3, bdd_keep_others);
	bdd_t_wave = manager.exists(tmp3, manager.getEventCube() );
	manager.deref(tmp3);

	// manager.printSet(bdd_t_wave);

	// SizeWatch.report(bdd_t_wave, "Twave");
    }

    // ------------------------------------------------------------------
    public void cleanup() {
	manager.deref(bdd_keep_others);
	manager.deref(bdd_keep_depend);
	manager.deref(bdd_t_wave);
    }
    // ------------------------------------------------------------------
    public BDDAutomaton [] getSet() { return dependent; }
    public int getTwave() { return bdd_t_wave; }

    // ------------------------------------------------------------------
    public void dump(PrintStream ps) {
	ps.print(me.getName() + " is dependent on { ");
	for(int i = 0; i < dependent.length; i++) {
	    if(i != 0) ps.print(", ");
	    ps.print(dependent[i].getName());
	}
	ps.println(" };");
    }

    public String getName() {
	return "DependencySet_" + me.getName();
    }
}
