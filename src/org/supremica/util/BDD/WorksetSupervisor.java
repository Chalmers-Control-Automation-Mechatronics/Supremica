package org.supremica.util.BDD;

import java.util.*;


/**
 *
 * This is an optimization of the simple disjunctive algorithm when we choose a
 * new automaton to traverse only if something has happened earlier that may affect
 * that automata.
 *
 *  algorithm:
 *
 *  \Delta = { all automata }
 * while \Delta \neq \emptyset {
 *     "pick_one" automaton i from \Delta
 *     R´:= R;
 *     R := Forward(R, \wave{\delta}^i);
 *     if (R \neq R') {
 *	      \Delta := \Delta \cup "affect"(i);
 *     }
 * }
 *
 * in this implementation, we use these simple assumptions
 * affect(automaton A) = {set of all automata that share events ( "interacts" ) with A (see also DependencySet) }
 * pick_one(from \Delta) = pick the automata that has been  "affected" min/most number of times
 *                        ( see function pick_one for this)
 *
 * For better performance, we use the disjunctive clusters instead of the automata itself here
 *
 */

public class WorksetSupervisor extends DisjSupervisor
{

	/**
	 * dependency matrix:  n = dependent[automata][0] is the number of dependent automata.
     * dependent[automata][1] .. dependent[automata][n] are the dependent automata.
	 */
	private int [][] dependent ;
	private int size;
	private BDDAutomaton [] bas;
	private Cluster [] clusters;

    /** Constructor, passes to the base-class */
    public WorksetSupervisor(BDDAutomata manager, BDDAutomaton [] as) {
		super(manager,as);
		init_worksets();
    }


    /** Constructor, passes to the base-class */
    public WorksetSupervisor(BDDAutomata manager, Group plant, Group spec) {
		super(manager, plant, spec);
		init_worksets();
    }

    /** C++-style Destructor to cleanup unused BDD trees*/
    public void cleanup() {
		super.cleanup();
    }


	/** this build ups the dependency matrix, once for all */
	private void init_worksets() {
		size = dop.getSize();
		clusters = dop.getClusters();
		int count;

		dependent = new int[size][size+1];

		for(int i = 0; i < size; i++) dependent[i][0] = 0;

		for(int i = 0; i < size; i++) {
			for(int j = i+1; j < size; j++) {
				if(clusters[i].interact(clusters[j])) {
					dependent[i][1 + dependent[i][0]++] = j;
					dependent[j][1 + dependent[j][0]++] = i;
				}
			}
		}
	}


	/** workset to start from: all automata are enabled */
	protected Workset createWorkset()
	{
		return new Workset(size, dependent);
	}


    protected void computeReachables() {

		// statistic stuffs
		GrowFrame gf = null;
		if(Options.show_grow) gf = new GrowFrame("Forward reachability (workset smoothed)");
		timer.reset();

		MonotonicPartition dp = new MonotonicPartition(manager, plant.getSize() + spec.getSize());

		SizeWatch.setOwner("WorksetSupervisor.computeReachables");


		Workset workset = createWorkset();

		int cube = manager.getStateCube();
		int permute = manager.getPermuteSp2S();
		int i_all   = manager.and(plant.getI(), spec.getI());
		int r_all_p, r_all = i_all;


		while(!workset.empty()) {
			int p = workset.pickOne();
			int r_all_org = r_all;
			// System.out.println("-->" + p);
			do {
				r_all_p = r_all;
				int tmp = manager.relProd(clusters[p].getTwave() , r_all, cube);
				int tmp2 = manager.replace(tmp, permute);
				manager.deref(tmp);
				r_all = manager.orTo(r_all, tmp2);
				manager.deref(tmp2);

				if (gf != null)	gf.add(manager.nodeCount(r_all));
			} while(r_all_p != r_all);

			workset.advance(p, r_all != r_all_org);
		}

		manager.deref(i_all);
		has_reachables = true;
		bdd_reachables = r_all;

		SizeWatch.report(bdd_reachables, "Qr");
		timer.report("Forward reachables found (workset smoothed)");
	}


   protected void computeCoReachables() {

		// statistic stuffs
		GrowFrame gf = null;
		if(Options.show_grow) gf = new GrowFrame("Backward reachability (workset smoothed)");
		timer.reset();

		SizeWatch.setOwner("WorksetSupervisor.computeReachables");

		MonotonicPartition dp = new MonotonicPartition(manager, plant.getSize() + spec.getSize());
		SizeWatch.setOwner("WorksetSupervisor.computeReachables");


		Workset workset = createWorkset();

		int cube = manager.getStatepCube();
		int permute1 = manager.getPermuteS2Sp();
		int permute2 = manager.getPermuteSp2S();
		int m_all = GroupHelper.getM(manager,spec, plant);
		int r_all_p, r_all = manager.replace(m_all, permute1);
		manager.deref(m_all);




		while(!workset.empty()) {
			int p = workset.pickOne();
			int r_all_org = r_all;
			do {
				r_all_p = r_all;
				int tmp = manager.relProd(clusters[p].getTwave(), r_all, cube);
				int tmp2 = manager.replace(tmp, permute1);
				manager.deref(tmp);
				r_all = manager.orTo(r_all, tmp2);
				manager.deref(tmp2);

				if (gf != null)	gf.add(manager.nodeCount(r_all));
			} while(r_all_p != r_all);


			workset.advance(p, r_all != r_all_org);
		}


		int ret = manager.replace(r_all, permute2);
		manager.deref(r_all);

		has_coreachables = true;
		bdd_coreachables = ret;

		SizeWatch.report(bdd_reachables, "Qr");
		timer.report("Backward reachables found (workset smoothed)");
	}
}