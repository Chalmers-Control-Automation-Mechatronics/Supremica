


package org.supremica.util.BDD;

import java.util.*;

/**
 * Disjunctive transition relation
 * NO CLUSTRING exists yet, so these algorithms may be SLOWER than the
 * monolitich versions!
 */
public class ConjSupervisor 
    extends Supervisor
{


    /** Constructor, passes to the base-class */
    public ConjSupervisor(BDDAutomata manager, Group plant, Group spec) {
	super(manager, plant, spec);
    }
    /** Constructor, passes to the base-class */
    public ConjSupervisor(BDDAutomata manager, BDDAutomaton[] automata) {
	super(manager, automata);
    }

    // TODO

    // protected int computeLanguageDifference(int considred_events) 
    //	public int getBR1(int marked, int forbidden)
    // public int getBR2(int forbidden)
    // public int getDeadlocks()
    // TODO: trace functions maybe?



    /** compute language difference without building the total T (or the two T:s in our case)*/
    /* NOT WORKING 
    protected int computeLanguageDifference(int considred_events) 
    {
	BDDAutomaton [] Sp = spec.getMembers();
	BDDAutomaton [] P  = plant.getMembers();
	int Sp_size = spec.getSize();
	int P_size  = plant.getSize();
	int sigma_cube = manager.getEventCube();

	int x1 = manager.getOne();
	manager.ref(x1);
	for(int j = 0; j < Sp_size; j++) {
	    // TODO: this should use relProd for forall/and
	    int tmp = manager.not( Sp[j].getTpri());
	    int tmp2 = manager.and(tmp, considred_events);
	    int tmp3 = manager.forall(tmp2, Sp[j].getCubep());
	    x1 = manager.andTo(x1,tmp3);
	    manager.recursiveDeref(tmp3);
	    manager.recursiveDeref(tmp2);
	    manager.recursiveDeref(tmp);
	}

	int x2 = manager.getOne();
	manager.ref(x2);
	for(int j = 0; j < P_size; j++) {
	    int tmp = manager.exists(P[j].getTpri(), P[j].getCubep());
	    x2 = manager.andTo(x2, tmp);
	    manager.recursiveDeref(tmp);
	}

	int ret = manager.relProd(x1,x2, sigma_cube);
	manager.recursiveDeref(x1);
	manager.recursiveDeref(x2);

	return ret;
    }
    */


    protected void computeReachables()
    {
	
	// Note: we remove events from t_all, it is needed for forward reachability
	GrowFrame gf = null;
	
	if (Options.show_grow)
	    gf = new GrowFrame("Conjunctive forward reachability");
	
	
	timer.reset();

	BDDAutomaton [] Sp = spec.getMembers();
	BDDAutomaton [] P  = plant.getMembers();
	int Sp_size = spec.getSize();
	int P_size  = plant.getSize();

	int cube = manager.and(manager.getStateCube(), manager.getEventCube());
	int permute = manager.getPermuteSp2S();
	int i_all = manager.and(plant.getI(), spec.getI());
	int r_all_p, r_all = i_all;	
	manager.ref(i_all);    // gets derefed by orTo and finally a recursiveDeref

	int front = r_all;
	manager.ref(front); // gets derefed by andTo
	

	// get the ordred automata list!
	GroupHelper gh = new GroupHelper(plant, spec);
	int [] tpri = gh.getTpri();
	int size = gh.getSize();

	do {
	    r_all_p = r_all;
	    

	    // apply the conjunctive transition relations in reverse order
	    // (to build the BDD bottom up ?)
	    for(int i = size-1; i >= 0; --i) 
		front = manager.andTo( front, tpri[i]);

	    int tmp = manager.exists(front, cube);
	    manager.recursiveDeref(front);


	    int tmp2 = manager.replace(tmp, permute);
	    manager.recursiveDeref(tmp);


	    r_all = manager.orTo(r_all, tmp2);
	    front = tmp2; // dont deref!
	    
	    if (gf != null)
		gf.add(manager.nodeCount(r_all));
	} while (r_all_p != r_all);
	
	manager.recursiveDeref(cube);
	manager.recursiveDeref(i_all);
	manager.recursiveDeref(front);
	
	has_reachables = true;
	bdd_reachables = r_all;
	
	timer.report("[Conjunctive] forward reachables found");
    }



    	protected void computeCoReachables()
	{
		GrowFrame gf = null;;

		if (Options.show_grow)
		{
			gf = new GrowFrame("[Conjunctive] backward reachability");
		}

		timer.reset();

		int cube = manager.and(manager.getStatepCube(), manager.getEventCube());
		int permute1 = manager.getPermuteS2Sp();
		int permute2 = manager.getPermuteSp2S();


		
		// get the ordred automata list!
		GroupHelper gh = new GroupHelper(plant, spec);
		int [] tpri = gh.getTpri();
		int size = gh.getSize();


		int m_all = GroupHelper.getM(manager, spec, plant);

		// gets derefed in first orTo ??
		int r_all_p, r_all = manager.replace(m_all, permute1);    

		int front = r_all;
		manager.ref(front);

		do
		{
			r_all_p = r_all;
			
			for(int i = size-1; i >= 0; --i) 
			    front = manager.andTo( front, tpri[i]);
			
			int tmp = manager.exists(front, cube);
			manager.recursiveDeref(front);

			int tmp2 = manager.replace(tmp, permute1);
			manager.recursiveDeref(tmp);
			
			r_all = manager.orTo(r_all, tmp2);
			front = tmp2;


			if (gf != null)
			{
				gf.add(manager.nodeCount(r_all));
			}
		}
		while (r_all_p != r_all);

		manager.recursiveDeref(m_all);
		manager.recursiveDeref(cube);

		int ret = manager.replace(r_all, permute2);

		manager.recursiveDeref(r_all);

		
		has_coreachables = true;
		bdd_coreachables = ret;

		timer.report("[Conjunctive] Co-reachables found");

		if (gf != null)
		{
			gf.stopTimer();
		}
	}

}
                   
