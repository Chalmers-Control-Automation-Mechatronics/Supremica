


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


    
    private GroupHelper gh;
    private int [] tpri;
    private int size;

    /** Constructor, passes to the base-class */
    public ConjSupervisor(BDDAutomata manager, Group plant, Group spec) {
	super(manager, plant, spec);
	init_conj();
    }
    /** Constructor, passes to the base-class */
    public ConjSupervisor(BDDAutomata manager, BDDAutomaton[] automata) {
	super(manager, automata);
	init_conj();
    }

    // -----------------------------------------------------------------
    private void init_conj() {
	// get the ordred automata list!
	gh = new GroupHelper(plant, spec);
	tpri = gh.getTpri();
	size = gh.getSize();
    }

    // -----------------------------------------------------------------
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
	    manager.deref(tmp3);
	    manager.deref(tmp2);
	    manager.deref(tmp);
	}

	int x2 = manager.getOne();
	manager.ref(x2);
	for(int j = 0; j < P_size; j++) {
	    int tmp = manager.exists(P[j].getTpri(), P[j].getCubep());
	    x2 = manager.andTo(x2, tmp);
	    manager.deref(tmp);
	}

	int ret = manager.relProd(x1,x2, sigma_cube);
	manager.deref(x1);
	manager.deref(x2);

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
		

	do {
	    r_all_p = r_all;
	    

	    // apply the conjunctive transition relations in reverse order
	    // (to build the BDD bottom up ?)
	    for(int i = 0; i < size; i++) 
		front = manager.andTo( front, tpri[i]);

	    int tmp = manager.exists(front, cube);
	    manager.deref(front);


	    int tmp2 = manager.replace(tmp, permute);
	    manager.deref(tmp);


	    r_all = manager.orTo(r_all, tmp2);
	    front = tmp2; // dont deref!
	    
	    if (gf != null)
		gf.add(manager.nodeCount(r_all));
	} while (r_all_p != r_all);
	
	manager.deref(cube);
	manager.deref(i_all);
	manager.deref(front);
	
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
	      

		int m_all = GroupHelper.getM(manager, spec, plant);

		// gets derefed in first orTo ??
		int r_all_p, r_all = manager.replace(m_all, permute1);    

		int front = r_all;
		manager.ref(front);

		do
		{
			r_all_p = r_all;
			
			for(int i = i = 0; i < size; i++) 
			    front = manager.andTo( front, tpri[i]);
			
			int tmp = manager.exists(front, cube);
			manager.deref(front);

			int tmp2 = manager.replace(tmp, permute1);
			manager.deref(tmp);
			
			r_all = manager.orTo(r_all, tmp2);
			front = tmp2;


			if (gf != null)
			{
				gf.add(manager.nodeCount(r_all));
			}
		}
		while (r_all_p != r_all);

		manager.deref(m_all);
		manager.deref(cube);

		int ret = manager.replace(r_all, permute2);

		manager.deref(r_all);

		
		has_coreachables = true;
		bdd_coreachables = ret;

		timer.report("[Conjunctive] Co-reachables found");

		if (gf != null)
		{
			gf.stopTimer();
		}
	}

}
                   
