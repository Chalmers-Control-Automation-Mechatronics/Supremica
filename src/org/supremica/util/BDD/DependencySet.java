
package org.supremica.util.BDD;

import java.util.*;
import java.io.*;

public class DependencySet {
    private BDDAutomata manager;
    private BDDAutomaton [] all, dependent;    
    private BDDAutomaton me;
    private boolean [] map_dependency;
    private int bdd_keep_depend, bdd_keep_others, bdd_t_wave, bdd_t_wave_isolated;
    private int bdd_i, bdd_cube, bdd_cube_others;
    

    public DependencySet(BDDAutomata manager, BDDAutomaton me) {
	this.manager = manager;
	this.all = manager.getAutomataVector();
	this.me  = me;

	map_dependency = new boolean[all.length];
	for(int i = 0; i < all.length; i++) 
	    map_dependency[i] = true;


	SizeWatch.setOwner( "DependencySet -> "+ getName());

	// get dependet set, compute bdd_keep_others at the same time

	Vector tmp = new Vector(all.length);
	bdd_keep_others = manager.getOne(); manager.ref(bdd_keep_others);
	bdd_cube = me.getCube(); manager.ref(bdd_cube);
	bdd_cube_others = manager.getOne(); manager.ref(bdd_cube_others);
	for(int i = 0; i < all.length; i++) {
	    if(all[i] != me) {
		if(me.interact(all[i])) {
		   tmp.add( all[i]);
		   bdd_cube = manager.and(bdd_cube, all[i].getCube());
		} else {
		    bdd_keep_others = manager.andTo(bdd_keep_others,
						    all[i].getKeep());
		    bdd_cube_others = manager.and(bdd_cube_others, all[i].getCube());
		    map_dependency[i] = false;
		}
	    }
	}

	SizeWatch.report(bdd_keep_others, "keep_others");

	// put it into an array
	int len = tmp.size();
	dependent = new BDDAutomaton[len];
	int i = 0;
	for(Enumeration it = tmp.elements(); it.hasMoreElements(); i++) {
	    BDDAutomaton ba = (BDDAutomaton) it.nextElement();
	    dependent[i] = ba;
	}



	// calc Twave etc
	int follow = manager.getOne(); manager.ref(follow);
	bdd_i = me.getI(); manager.ref(bdd_i);
	

	for(i = 0; i < len; i++) {
	    BDDAutomaton a_i =  dependent[len-i-1];

	    // TWave
	    int common_events =  manager.and(a_i.getSigma(), me.getSigma());
	    int uncommon_events = manager.not(common_events);

	    int move = manager.and(a_i.getT(), common_events);
	    int keep = manager.and(a_i.getKeep(), uncommon_events);

	    manager.deref(common_events);
	    manager.deref(uncommon_events);

	    int dep_move = manager.or(move,keep);
	    manager.deref(move);
	    manager.deref(keep);

	    SizeWatch.report(dep_move, "dep-move_" + a_i.getName());
	    follow = manager.andTo(follow, dep_move);
	    manager.deref(dep_move);

	    // I
	    bdd_i = manager.andTo(bdd_i, a_i.getI());
	}


       
       int tmp3 = manager.and(me.getT(), follow);
	manager.deref(follow);


	bdd_t_wave_isolated = manager.exists(tmp3,  manager.getEventCube());
	tmp3 = manager.andTo(tmp3, bdd_keep_others);
	bdd_t_wave = manager.exists(tmp3, manager.getEventCube() );
	manager.deref(tmp3);

	// manager.printSet(bdd_t_wave);

	SizeWatch.report(bdd_t_wave, "Twave");
    }

    // -----------------------------------------------------------------


    // ------------------------------------------------------------------
    public void cleanup() {
	manager.deref(bdd_keep_others);
	manager.deref(bdd_keep_depend);
	manager.deref(bdd_t_wave_isolated);
	manager.deref(bdd_t_wave);
	manager.deref(bdd_i);
	manager.deref(bdd_cube_others);
	
    }
    // ------------------------------------------------------------------
    public BDDAutomaton [] getSet() { return dependent; }
    public int getCube() { return bdd_cube; }
    public int getCubeOthers() { return bdd_cube_others; }
    public int getTwave() { return bdd_t_wave; }
    public int getTwaveIsolated() { return bdd_t_wave_isolated; }
    public int getI() { return bdd_i; }

    // -----------------------------------------------------------------
    public int getReachables(int start) {	
	int cube = manager.getStateCube();
	int permute = manager.getPermuteSp2S();
	int q,qp, front;

	front = q = start;
	manager.ref(q); // orTo
	manager.ref(front); // deref after orTo

	do {
	    qp = q;
	    int tmp = manager.relProd(bdd_t_wave_isolated, front, cube);
	    int tmp2 = manager.replace(tmp, permute);
	    manager.deref(tmp);
	    q = manager.orTo(q, tmp2);

	    manager.deref(front);
	    front = tmp2;	    
	} while(q != qp);

	manager.deref(front);

	return q;
    }
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
