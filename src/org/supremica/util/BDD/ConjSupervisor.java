


package org.supremica.util.BDD;

import java.util.*;

/**
 * Disjunctive transition relation
 * NO CLUSTRING exists yet, so these algorithms may be SLOWER than the
 * monolithic versions!
 */
public class ConjSupervisor
    extends Supervisor
{


    private ConjPartition conj_partition;
    protected GroupHelper gh;
    protected int [] tpri;
    protected int conj_size;

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
		conj_partition = null; // not needed yet
		// get the ordred automata list!
		gh = new GroupHelper(plant, spec);
		tpri = gh.getTpri();
		conj_size = gh.getSize();
    }

    // -----------------------------------------------------------------
    public void cleanup() {
		if(conj_partition != null) {
			conj_partition.cleanup();
			conj_partition = null;
		}
		super.cleanup();
    }
    protected ConjPartition getConjPartition() {
		if(conj_partition == null) computeConjPartition();
		return conj_partition;
    }

    private void computeConjPartition() {
		conj_partition = new ConjPartition(manager, conj_size);
		for(int i = 0; i < conj_size; i++)
		    conj_partition.add ( tpri[i]);
		conj_partition.report(); // show some states
    }
    // -----------------------------------------------------------------
    // TODO

    // protected int computeLanguageDifference(int considred_events)
    //	public int getBR1(int marked, int forbidden)
    // public int getBR2(int forbidden)
    // public int getDeadlocks()
    // TODO: trace functions maybe?




	protected int computeLanguageDifference(int considred_events, boolean remove_events)
    {
		int cubep_sp = spec.getCubep();
		int cubep_p = plant.getCubep();
		int tmp;

		SizeWatch.setOwner("ConjSupervisor.computeLanguageDifference");




		int work;

		// if(plant.getSize() > spec.getSize()) {
			int t_sp = spec.getT();

			SizeWatch.report(t_sp, "Tsp");

			tmp = manager.exists(t_sp, cubep_sp);
			work = manager.not(tmp);
			manager.deref(tmp);
			SizeWatch.report(work, "~Eq'sp. Tsp");


			BDDAutomaton[] ps = plant.getMembers();
			int psize = plant.getSize();
			for(int i = 0; i < psize; i++) {
				tmp = manager.exists(ps[i].getTpri(), cubep_p);
				work = manager.andTo(work, tmp);
				manager.deref(tmp);
			}



	/*
		} else {
			// THIS WONT WORK! (why??)
			int t_p = plant.getT();
			SizeWatch.report(t_p, "Tp");
			work = manager.exists(t_p, cubep_p);

			BDDAutomaton[] sps = spec.getMembers();
			int spsize = spec.getSize();
			for(int i = 0; i < spsize; i++) {
				int tmp = manager.exists(sps[i].getTpri(), cubep_sp);
				int tmp2 = manager.not(tmp);
				manager.deref(tmp);
				work = manager.orTo(work, tmp2);
				manager.deref(tmp2);
			}
		}
	*/

		/*
		// THIS IS PROBABLY WORSE THAN THE MONOLITHIC VERSION (is it?)
		int work2 = manager.getOne();
		manager.ref(work2);

		BDDAutomaton [] all  = gh.getSortedList();
		boolean [] isPlant   = gh.getSortedType();
		int size = all.length;

		// XXX: maybe its more efficient to AND against considred_events after the loop??
		for(int i = 0; i < size; i++) {
			// Options.out.println("Adding " + all[i].getName() + (isPlant[i] ? " plant" : " spec"));
			// manager.printSet(work);
			if(isPlant[i]) {
				int tmp = manager.relProd(all[i].getTpri(), considred_events, cubep_p);
				work = manager.andTo(work, tmp);
				manager.deref(tmp);
			} else {
				int tmp = manager.exists(all[i].getTpri(), cubep_sp);
				work2 = manager.andTo(work2, tmp);
				manager.deref(tmp);
			}
		}


		// not delta_sp and stuff
		int tmp2 = manager.not(work2);
		manager.deref(work2);
		int tmp3 = manager.and(tmp2, considred_events);
		manager.deref(tmp2);
		work = manager.andTo(work, tmp3);
		manager.deref(tmp3);
		*/


		// restrict it to the considred events...
		work = manager.and(work, considred_events);


		// this is a _DIRTY_ trick to remove added self-loops (false)contribution!
		// this is needed when doing modular computation, i.e. when plant + spec < all automata
		work = manager.andTo(work, plant.getSigma());


		if(remove_events) {
			tmp = manager.exists(work, e_cube);
			manager.deref(work);
			work = tmp;
		}

		SizeWatch.report(work, "(Language diff)");

		return work;

    }


	// ------------------------------------------------------------------------


	/**
	 * do a FORWARD reachability search.
	 * start from the given (set of) initial state(s)
	 */
	public int getReachables(int initial_states) {
		return internal_computeReachablesConj(initial_states, -1);
	}

	/**
	 * do a FORWARD reachability search, use only these events;
	 * start from the initial state
	 */
	public int getReachables(boolean [] events) {
		int i_all = manager.and(plant.getI(), spec.getI());
		int ret = getReachables(events, i_all);
		manager.deref(i_all);
		return ret;
	}


	/**
	 * do a FORWARD reachability search, use only these events;
	 * start from the given initial state(s)
	 */
	public int getReachables(boolean [] events, int intial_states) {
		int event_mask = manager.getAlphabetSubsetAsBDD(events);
		int x = internal_computeReachablesConj(intial_states, event_mask);
		manager.deref(event_mask);
		return x;
	}

    protected void computeReachables()
    {
		int i_all = manager.and(plant.getI(), spec.getI());
		int ret = internal_computeReachablesConj(i_all, -1);
		manager.deref(i_all);
		has_reachables = true;
		bdd_reachables = ret;
	}

	/**
	 * compute reachable states using disjunctive-partioning.
	 * start from initial state(s) i_all
	 * consider only events in event_mask (-1 means consider all)
	 */
	private int internal_computeReachablesConj(int i_all, int event_mask)
	{

		// Note: we remove events from t_all, it is needed for forward reachability
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Conjunctive forward reachability");


		timer.reset();
		ConjPartition cp = getConjPartition();
		SizeWatch.setOwner("ConjSupervisor.computeReachables");

		int r_all_p, r_all = i_all, front = i_all;
		manager.ref(i_all);    // gets derefed by orTo and finally a recursiveDeref
		manager.ref(front); // gets derefed

		do {
			r_all_p = r_all;

			int tmp2;
			if(event_mask == -1)	tmp2 = cp.image(front);
			else					tmp2 = cp.image(front, event_mask);

			r_all = manager.orTo(r_all, tmp2);
			manager.deref(front);
			front = tmp2;

			if (gf != null)
			gf.add(r_all);
		} while (r_all_p != r_all);

		manager.deref(front);



		if(gf != null) gf.stopTimer();
		SizeWatch.report(bdd_reachables, "Qr");
		timer.report("[Conjunctive] forward reachables found");
		return r_all;
    }


	// -------------------------------------------------------------------------------------
    protected void computeCoReachables()
	{
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "[Conjunctive] backward reachability");


		timer.reset();
		ConjPartition cp = getConjPartition();
		SizeWatch.setOwner("ConjSupervisor.computeReachables");

		int m_all = GroupHelper.getM(manager, spec, plant);
		int r_all_p, r_all = manager.replace(m_all, perm_s2sp);  // r_all refed
		int front = r_all;
		manager.ref(front); // gets derefed
		manager.deref(m_all);


		SizeWatch.report(r_all, "Qm");

		do
		{
			r_all_p = r_all;

			int tmp = cp.preImage(front);
			r_all = manager.orTo(r_all, tmp);
			manager.deref(front);
			front = tmp;


			if (gf != null)
			{
				gf.add(r_all);
			}
		}
		while (r_all_p != r_all);

		manager.deref(front);

		int ret = manager.replace(r_all, perm_sp2s);
		manager.deref(r_all);

		has_coreachables = true;
		bdd_coreachables = ret;

		timer.report("[Conjunctive] Co-reachables found");
		SizeWatch.report(bdd_coreachables, "Qco");

		if (gf != null)
		{
			gf.stopTimer();
		}
	}

}

