

package org.supremica.util.BDD;

import java.util.*;

public class DisjSupervisor extends ConjSupervisor {
    protected int [] twave;

    /** Constructor, passes to the base-class */
    public DisjSupervisor(BDDAutomata manager, BDDAutomaton [] as) {
	super(manager,as);
	init_disj();
    }


    /** Constructor, passes to the base-class */
    public DisjSupervisor(BDDAutomata manager, Group plant, Group spec) {
	super(manager, plant, spec);
	init_disj();
    }

    // --------------------------------------------------------
    private void init_disj() {
	twave = gh.getTwave();
    }

    // --------------------------------------------------------
    protected void computeReachables() {
	// statistic stuffs
	GrowFrame gf = null;
	if(Options.show_grow)
	    gf = new GrowFrame("Forward reachability (disjunctive)");

	timer.reset();
	int cube    = manager.getStateCube();
	int permute = manager.getPermuteSp2S();
	int i_all   = manager.and(plant.getI(), spec.getI());

	int r_all_p, r_all = i_all;
	manager.ref(i_all); //gets derefed by orTo and finally a deref

	do {
	    r_all_p = r_all;

	    int front = manager.getZero(); manager.ref(front);
	    for(int i = 0; i < size; i++) {
		int new_states = manager.relProd( twave[i], r_all, cube);		
		front = manager.orTo(front, new_states);
		manager.deref(new_states);
	    }
	    
	    int tmp2 = manager.replace( front, permute);
	    manager.deref(front);
	    r_all = manager.orTo(r_all, tmp2);
	    manager.deref(tmp2);

	    if(gf != null)    gf.add( manager.nodeCount( r_all));
	} while(r_all_p != r_all);

	manager.deref(i_all);

	has_reachables = true;
	bdd_reachables = r_all;
	timer.report("Forward reachables found (disjunctive)");
	// SizeWatch.report(r_all, "R");
    }
    
    protected void computeCoReachables() {
	GrowFrame gf = null;;
	if(Options.show_grow) gf = new GrowFrame("backward reachability (disjuncted)");
	// SizeWatch.setOwner("smoothed BR in Supervisor");
	timer.reset();

	int cube    = manager.getStatepCube();
	int permute1 = manager.getPermuteS2Sp();
	int permute2 = manager.getPermuteSp2S();

	int m_all = GroupHelper.getM(manager, spec, plant);
	// gets derefed in first orTo ??
	int r_all_p, r_all = manager.replace(m_all, permute1);
	manager.deref(m_all);

	do {
	    int front_s = manager.getZero(); manager.ref(front_s);
	    for(int i = 0; i < size ; i++) {
		int new_states = manager.relProd(twave[i], r_all, cube);
		front_s = manager.orTo(front_s, new_states);
		manager.deref(new_states);
	    }
	
	    r_all_p = r_all;
	    int front_sp = manager.replace( front_s, permute1);
	    manager.deref(front_s);
	    
	    r_all    = manager.orTo(r_all, front_sp);
	    manager.deref(front_sp);
	    
	    if(gf != null)    gf.add( manager.nodeCount( r_all));	    
	} while(r_all != r_all_p);
    	
	int ret = manager.replace(r_all, permute2);

	// cleanup:
	manager.deref(r_all);

	has_coreachables = true;
	bdd_coreachables = ret;
	timer.report("Co-reachables found (disjuncted)");
	if(gf != null) gf.stopTimer();
	// SizeWatch.report(bdd_coreachables,"Coreachables");
    }
 
}
