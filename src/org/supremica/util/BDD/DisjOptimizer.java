
package org.supremica.util.BDD;

import java.util.*;

/**
 * This class optimize the number and BDD size of disjunctive transition relations
 */


public class DisjOptimizer {

    private GroupHelper gh;
    private BDDAutomata manager;
    private int max_size, size;
    private int [] twave, twave2;
	private Cluster [] clusters;

	// DEBUG public BDDRefCheck refcheck;

    public DisjOptimizer(BDDAutomata manager, GroupHelper gh) {
		this.gh = gh;
		this.manager = manager;

		max_size = size = gh.getSize();

		twave = new int[size];
		twave2 = new int[size];
		clusters = new Cluster[size];

		int [] tmp = gh.getTwave();
		int [] cube = gh.getCube();
		int [] cubep = gh.getCubep();
		BDDAutomaton [] automata = gh.getSortedList();

 		// DEBUG refcheck = new BDDRefCheck(manager, "DisjOptimizer");

		for(int i = 0; i < size; i++) {
			// make this copy our own
			twave[i] = tmp[i];
			manager.ref(twave[i]);

			// THIS solves many problmes (why?)
			// manager.ref( cube[i] );

			clusters[i] = new Cluster(manager, twave[i], cube[i], cubep[i]);
			clusters[i].members.addElement( automata[i] );

			// DEBUG:
			// refcheck.add( twave[i]);
			// refcheck.add( cube[i]);
			// refcheck.add( cubep[i]);

		}

		optimize();
		// DEBUG check("After optimize");

    }


    public void cleanup() {
		// DEBUG check("cleanup()");
		for(int i = 0; i < size; i++) {
			manager.deref(twave[i]);
			clusters[i].cleanup();
		}
    }

	// ----------------------------------------------------

	/** DEBUG
	public void check(String place){
		refcheck.check(place);
		for(int i = 0; i < size; i++) clusters[i].check(place);
	}
	*/

	// ----------------------------------------------------

    public int getSize()
    {
		// DEBUG check("getSize()");
		return size;
    }

    public Cluster [] getClusters()
    {
		// DEBUG check("getClusters()");
		return clusters;
	}
    // ----------------------------------------------------
    /*
    void optimize() {
    // BDD size optimization: DOES NOT WORK

	// TODO: we only need to look at those that are dependent!

	int current = 0;
	for(int i = 0; i < size; i++) {

	    for(int j = i+1; j < size; j++) {
		int l = twave[i]; // lower bound
		int u = twave[j]; // upper bound
		int notu = manager.not(u);
		int c = manager.or(notu, l); // c = l + ~u

		int f = manager.restrict(l, c);
		// int f = manager.constrain(l,c);

		manager.deref(notu);
		manager.deref(c);

		int s0 = manager.nodeCount(l); // old size
		int s1 = manager.nodeCount(f); // new size

		Options.out.println("s0 = " + s0 + ", s1 = " + s1);
		if(f == manager.getZero()) {
		    Options.out.println("f == 0");
		    return;
		} else if(f == manager.getOne()) {
		    Options.out.println("f == 1");
		} else if(s1 < s0) {
		    Options.out.println("|f| < |l|");


		    // START OF DEBUG CODE
		    int less = manager.and(l,f);
		    if(less != l) {
			Options.out.println("f is less than l");
			int add = manager.not(f);
			add = manager.andTo(l, f);
			Util.showBDD(manager,add,"add"+i);
			return;
		    }

		    int both = manager.or(u,l);
		    int more = manager.or(both,f);
		    if(more != both) {
			Options.out.println("f is more than u");
			int notboth = manager.not(both);
			int extra = manager.and(f, both);
			manager.printSet(extra);
			Util.showBDD(manager,f,"f");
			Util.showBDD(manager,both, "both");
			manager.show_transitions(extra);
			return;
		    }

		    manager.deref(less);
		    manager.deref(more);
		    // END OF DEBUG CODE
		}


		manager.deref(f);
	    }
	}
    }
    */


    private void optimize() {
	// BDD undfolding: works, but makes the smotthing useless
	/*
    	int current = 0;
	int max_nodes = 500;


	twave2[current] = twave[0];

	for(int i = 1; i < size; i++) {
	    if(manager.nodeCount(twave[i]) > max_nodes) {
		current ++;
		twave2[current] = twave[i];
	    } else {
		int new_t = manager.or(twave2[current], twave[i]);
		if(manager.nodeCount(new_t) < max_nodes) {
		    // keep
		    manager.deref(twave2[current]);
		    manager.deref(twave[i]);
		    twave2[current] = new_t;
		} else {
		    // next
		    manager.deref(new_t);
		    current++;
		    twave2[current] = twave[i];
		}
	    }
	}


	current ++;

	SizeWatch.setOwner("DisjOptimizer.optimize");
	for(int i = 0; i < size; i++) SizeWatch.report(twave[i],"old Ti");
	for(int i = 0; i < current; i++) SizeWatch.report(twave2[i],"new Ti");

	size = current;
	int [] swap = twave2;
	twave2 = twave;
	twave = swap;

	*/
    }
}
