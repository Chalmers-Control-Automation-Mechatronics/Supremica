

package org.supremica.util.BDD;

// NOTICE: maybe we should ignore the first round (initial states) ???


// Smoothed reachability based on conjunctive transition relations
//
// the way the BDD peek jumps up and down just before fixpoint
// is probably due to a bug in the PCG routines that give a very
// non-optimal "shortest path"...


import java.util.*;

public class SmoothSupervisor extends DisjSupervisor {

    public SmoothSupervisor(BDDAutomata manager, Group p, Group sp) {
	super(manager,p,sp);
    }
    
    public SmoothSupervisor(BDDAutomata manager, BDDAutomaton [] as) {
	super(manager,as);
    }


    // ------------------------------------------------------------------------

    protected void computeReachables() {
	// statistic stuffs
	GrowFrame gf = null;
	if(Options.show_grow)
	    gf = new GrowFrame("Forward reachability (smoothed)");

	// SizeWatch.setOwner("Smoothed FR in Supervisor");

	timer.reset();

	int cube    = manager.getStateCube();
	int permute = manager.getPermuteSp2S();
	int i_all = manager.and(plant.getI(), spec.getI());

	int r_all_p, r_all = i_all;
	manager.ref(i_all); //gets derefed by orTo and finally a deref


	// 0/1 smoothing
	boolean [] remaining = new boolean[size];
	for(int i = 0; i < size; i++) 
	    remaining[i] = true; 


	for(int a = 0; a < size; a++) {
	    remaining[a] = false;
	    int r_all_pp, front_s, front_sp;

	    do {
		r_all_pp = r_all;
		for(int i = 0; i < size ; i++) {
		    if(!remaining[a]) {
			int index = size - i - 1; // INVERSE ORDER
			front_sp = manager.relProd(twave[index], r_all, cube);
			front_s  = manager.replace(front_sp, permute);
			manager.deref(front_sp);
			r_all    = manager.orTo(r_all, front_s);
			manager.deref(front_s);
			if(gf != null)    gf.add( manager.nodeCount( r_all));
		    }
		}
	    } while(r_all != r_all_pp);	    
	    // if(gf != null)    gf.add( manager.nodeCount( r_all));
	}



	// cleanup
	manager.deref(i_all);


	has_reachables = true;
	bdd_reachables = r_all;
	timer.report("Forward reachables found (smoothed)");
	// SizeWatch.report(r_all, "R");
    }


    // -------------------------------------------------------------------------------

    protected void computeCoReachables() {
	GrowFrame gf = null;;
	if(Options.show_grow) gf = new GrowFrame("backward reachability (smoothed)");
	// SizeWatch.setOwner("smoothed BR in Supervisor");
	timer.reset();



	int cube    = manager.getStatepCube();
	int permute1 = manager.getPermuteS2Sp();
	int permute2 = manager.getPermuteSp2S();



	int m_all = GroupHelper.getM(manager, spec, plant);
	// gets derefed in first orTo ??
	int r_all_p, r_all = manager.replace(m_all, permute1);
	manager.deref(m_all);



	// 0/1 smoothing
	boolean [] remaining = new boolean[size];
	for(int i = 0; i < size; i++) 
	    remaining[i] = true; 



	for(int a = 0; a < size; a++) {
	    remaining[a] = false;
	    int r_all_pp, front_s, front_sp;
	    
	    do {
		r_all_pp = r_all;
		for(int i = 0; i < size ; i++) {
		    if(!remaining[a]) {
			int index = size - i - 1; // INVERSE ORDER
			front_s = manager.relProd(twave[index], r_all, cube);
			front_sp = manager.replace( front_s, permute1);
			manager.deref(front_s);
			
			r_all    = manager.orTo(r_all, front_sp);
			manager.deref(front_sp);
			if(gf != null)    gf.add( manager.nodeCount( r_all));
		    }
		}
	    } while(r_all != r_all_pp);
	}
	
    

	int ret = manager.replace(r_all, permute2);

	// cleanup:
	manager.deref(r_all);


	has_coreachables = true;
	bdd_coreachables = ret;
	timer.report("Co-reachables found (smoothed)");
	if(gf != null) gf.stopTimer();
	// SizeWatch.report(bdd_coreachables,"Coreachables");

    }



    // -------------------------------------------------------------
    protected void computeUncontrollables() {
	timer.reset();

	int t_sp = spec.getT();
	int t_p  = plant.getT();
	int cubep_sp = spec.getCubep();
	int cubep_p = plant.getCubep();
	int sigma_u = manager.getSigmaU();
	int sigma_cube = manager.getEventCube();

	int tmp10 = manager.relProd(t_sp, sigma_u, cubep_sp);
	int tmp1  = manager.not(tmp10);
	manager.deref(tmp10);

	int tmp2  = manager.and(tmp1, sigma_u);
	manager.deref(tmp1);

	int cube2 = manager.and(sigma_cube, cubep_p);

	int tmp4 = manager.relProd(t_p, tmp2, cube2);
	manager.deref(tmp2); manager.deref(cube2);

	has_uncontrollables = true;
	bdd_uncontrollables = tmp4;
	timer.report("Uncontrollable states found");

    }
}
