

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
      
	timer.reset();
	DisjPartition dp = new DisjPartition(manager, plant.getSize() + spec.getSize());
	SizeWatch.setOwner("SmoothSupervisor.computeReachables");
	int cube    = manager.getStateCube();	
	int i_all = manager.and(plant.getI(), spec.getI());

	if(Options.local_saturation) {
	    // compute saturated I
	    int i_first = i_all;
	    i_all = manager.getZero(); manager.ref(i_all);
	    BDDAutomaton [] as = gh.getSortedList();
	    for(int i = 0; i < gh.getSize(); i++) {	    
		DependencySet ds = as[i].getDependencySet();
		int i2 = ds.getReachables( ds.getI());
		int i_others = manager.exists(i_first, ds.getCube());		
		i2 = manager.andTo(i2, i_others);				
		manager.deref(i_others);
		i_all = manager.orTo(i_all, i2);
		manager.deref(i2);
	    }
	    manager.deref(i_first);
	} // End of computing saturated I
	

	int r_all_p, r_all = i_all;
	manager.ref(i_all); //gets derefed by orTo and finally a deref


	// 0/1 smoothing
	boolean [] remaining = new boolean[disj_size];
	for(int i = 0; i < disj_size; i++) 
	    remaining[i] = true; 


	for(int a = 0; a < disj_size; a++) {
	    if(remaining[a]) {	    
		remaining[a] = false;
		dp.add(twave[a]);		
	    }
	    int r_all_pp, front_s, front_sp;

	    do {
		r_all_pp = r_all;		
		int front = dp.image(r_all);
		r_all = manager.orTo(r_all, front);
		manager.deref(front);
		if(gf != null)    gf.add( manager.nodeCount( r_all));

	    } while(r_all != r_all_pp);	    
	}



	// cleanup
	manager.deref(i_all);


	has_reachables = true;
	bdd_reachables = r_all;
	SizeWatch.report(r_all, "Qr");
	timer.report("Forward reachables found (smoothed)");
	// SizeWatch.report(r_all, "R");
    }
    // -------------------------------------------------------------------------------

    protected void computeCoReachables() {
	GrowFrame gf = null;;
	if(Options.show_grow) gf = new GrowFrame("backward reachability (smoothed)");

	timer.reset();
	DisjPartition dp = new DisjPartition(manager, plant.getSize() + spec.getSize());
	SizeWatch.setOwner("SmoothSupervisor.computeCoReachables");

	int cube    = manager.getStatepCube();
	int permute1 = manager.getPermuteS2Sp();
	int permute2 = manager.getPermuteSp2S();


	int m_all = GroupHelper.getM(manager, spec, plant);

	// gets derefed in first orTo ??
	int r_all_p, r_all = manager.replace(m_all, permute1);
	manager.deref(m_all);


	if(Options.local_saturation) {
	    // TODO: compute saturated m_all (r_all right now) 
	}

	SizeWatch.report(r_all, "Qm"); 

	// 0/1 smoothing
	boolean [] remaining = new boolean[disj_size];
	for(int i = 0; i < disj_size; i++) 
	    remaining[i] = true; 




	for(int a = 0; a < disj_size; a++) {
	    if(remaining[a]) {	    
		remaining[a] = false;
		dp.add(twave[a]);
	    }
	    int r_all_pp, front_s, front_sp;

	    do {
		r_all_pp = r_all;		
		int front = dp.preImage(r_all);
		r_all = manager.orTo(r_all, front);
		manager.deref(front);
		if(gf != null)    gf.add( manager.nodeCount( r_all));

	    } while(r_all != r_all_pp);	    
	}


	int ret = manager.replace(r_all, permute2);

	// cleanup:
	manager.deref(r_all);


	has_coreachables = true;
	bdd_coreachables = ret;

	SizeWatch.report(bdd_coreachables, "Qco");
	timer.report("Co-reachables found (smoothed)");
	if(gf != null) gf.stopTimer();
	// SizeWatch.report(bdd_coreachables,"Coreachables");

    }

}
