package org.supremica.util.BDD;

// NEW CUDD: fixed
import java.util.*;

public class Supervisor
{
	private Group plant;
	private Group spec;
	private BDDAutomata manager;
	private Timer timer;
	private boolean has_uncontrollables, has_reachables,
					has_reachable_uncontrollables, has_coreachables;
	private int bdd_uncontrollables, bdd_reachables,
				bdd_reachable_uncontrollables, bdd_coreachables;

	public Supervisor(BDDAutomata manager, Group plant, Group spec)
	{
		this.manager = manager;
		this.plant = plant;
		this.spec = spec;

		init();
	}

	public Supervisor(BDDAutomata manager, BDDAutomaton[] automata)
	{
		this.manager = manager;
		plant = new Group(manager, automata.length, "Plant");
		spec = new Group(manager, automata.length, "Spec");

		for (int i = 0; i < automata.length; i++)
		{
			if (automata[i].getType() == Automaton.TYPE_SPEC)
			{
				spec.add(automata[i]);
			}
			else if (automata[i].getType() == Automaton.TYPE_PLANT)
			{
				plant.add(automata[i]);
			}
		}

		init();
	}

	private void init()
	{
		timer = new Timer("Supervisor");

		if (Options.debug_on)
		{
			plant.dump();
			spec.dump();
		}

		has_uncontrollables = has_reachables = has_reachable_uncontrollables = has_coreachables = false;
	}

	public void cleanup()
	{
		if (has_reachable_uncontrollables)
		{
			has_reachable_uncontrollables = false;

			manager.recursiveDeref(bdd_reachable_uncontrollables);
		}

		if (has_uncontrollables)
		{
			has_uncontrollables = false;

			manager.recursiveDeref(bdd_uncontrollables);
		}

		if (has_reachables)
		{
			has_reachables = false;

			manager.recursiveDeref(bdd_reachables);
		}

		if (has_coreachables)
		{
			has_coreachables = false;

			manager.recursiveDeref(bdd_coreachables);
		}

		plant.cleanup();
		spec.cleanup();
	}

	// ------------------------------------------------------------------------------
	public BDDAutomata getManager()
	{
		return manager;
	}

	public Group getP()
	{
		return plant;
	}

	public Group getSp()
	{
		return spec;
	}

	// ------------------------------------------------------------------------------
	public int getUncontrollableStates()
	{
		if (!has_uncontrollables)
		{
			computeUncontrollables();
		}

		return bdd_uncontrollables;
	}
    
	private void computeUncontrollables()
	{
		timer.reset();

		int sigma_u = manager.and( plant.getSigmaU(), spec.getSigmaU()); // WAS manager.getSigmaU();

		bdd_uncontrollables = computeLanguageDifference(sigma_u);
		has_uncontrollables = true;

		manager.recursiveDeref(sigma_u);

		timer.report("Uncontrollable states found");
	}


    protected int computeLanguageDifference(int considred_events) 
    {
	int t_sp = spec.getT();
	int t_p = plant.getT();
	int cubep_sp = spec.getCubep();
	int cubep_p = plant.getCubep();
	int sigma_cube = manager.getEventCube();
	int tmp10 = manager.exists(t_sp, cubep_sp);
	int tmp1 = manager.not(tmp10);
	
	manager.recursiveDeref(tmp10);
	
	int tmp2 = manager.and(tmp1, considred_events);	
	manager.recursiveDeref(tmp1);
	int cube2 = manager.and(sigma_cube, cubep_p);
	int tmp4 = manager.relProd(t_p, tmp2, cube2);
	
	manager.recursiveDeref(tmp2);
	manager.recursiveDeref(cube2);	
	return tmp4;
	
    }

    /**
     * We use the same base as our controllability routine to check if Plant includes Spec
     * <b>important note</b>: the plant and spec need not to be the actual plant and spec!
     * we only use this conevntion to reuse the controllability code!!
     */ 
    private int getLanguageDifference() 
    {
	timer.reset();		
	int ret = computeLanguageDifference(plant.getSigma());
	timer.report("Uncontrollable states found");
	return ret;
    }


    /**
     * <b>Reachable</b> counterexamples to the language inclusions check
     *
     *
     */

    public int computeReachableLanguageDifference() {
	int ld = getLanguageDifference();
	if(ld == manager.getZero()) {
	    // nothing there, so we dont need the intersection with reachables 
	    // to get the reachable difference
	    return ld;
	}
	
	int r = getReachables();
	int intersection = manager.and(ld,r);
	manager.recursiveDeref(ld);
	return intersection;
    }



	// --------------------------------------------------------------------------------------------

	/*
	 * public int getReachables(int from, GrowFrame gf) {
	 *   int cube    = manager.getStateCube();
	 *   int permute = manager.getPermuteSp2S();
	 *   int t_all = manager.relProd(plant.getT(), spec.getT(), manager.getEventCube());
	 *   int r_all_p, r_all = from;
	 *   manager.ref(r_all); // gets derefed by orTo and finally a recursiveDeref
	 *
	 *   do {
	 *       r_all_p = r_all;
	 *       int tmp = manager.relProd(t_all, r_all, cube);
	 *       int tmp2 = manager.replace( tmp, permute);
	 *       manager.recursiveDeref(tmp);
	 *       r_all = manager.orTo(r_all, tmp2);
	 *       manager.recursiveDeref(tmp2);
	 *       if(gf != null)      gf.add( manager.nodeCount( r_all));
	 *   } while(r_all_p != r_all);
	 *
	 *   manager.recursiveDeref(t_all);
	 *   return r_all;
	 * }
	 */
	public int getReachables()
	{    // get reachables from I
		if (!has_reachables)
		{
			computeReachables();
		}

		return bdd_reachables;
	}

	private void computeReachables()
	{

		// Note: we remove events from t_all, it is needed for forward reachability
		GrowFrame gf = null;

		if (Options.show_grow)
		{
			gf = new GrowFrame("Forward reachability");
		}

		timer.reset();

		int cube = manager.getStateCube();
		int permute = manager.getPermuteSp2S();
		int t_all = manager.relProd(plant.getT(), spec.getT(), manager.getEventCube());
		int i_all = manager.and(plant.getI(), spec.getI());
		int r_all_p, r_all = i_all;

		manager.ref(i_all);    // gets derefed by orTo and finally a recursiveDeref

		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(t_all, r_all, cube);
			int tmp2 = manager.replace(tmp, permute);

			manager.recursiveDeref(tmp);

			r_all = manager.orTo(r_all, tmp2);

			manager.recursiveDeref(tmp2);

			if (gf != null)
			{
				gf.add(manager.nodeCount(r_all));
			}
		}
		while (r_all_p != r_all);

		manager.recursiveDeref(i_all);
		manager.recursiveDeref(t_all);

		has_reachables = true;
		bdd_reachables = r_all;

		timer.report("Forward reachables found");

		/*
		 * GrowFrame gf = null;
		 * if(Options.show_grow) gf = new GrowFrame("Forward reachability");
		 *
		 * timer.reset();
		 * int i_all = manager.and(plant.getI(), spec.getI());
		 *
		 * bdd_reachables = getReachables(i_all, gf);
		 * has_reachables = true;
		 * manager.recursiveDeref(i_all);
		 * timer.report("Forward reachables found");
		 */
	}

	public int getReachableUncontrollables()
	{
		if (!has_reachable_uncontrollables)
		{
			computeReachableUncontrollables();
		}

		return bdd_reachable_uncontrollables;
	}

	private void computeReachableUncontrollables()
	{

		// note: dont use timer here (get reseted in two places below)
		int r_all = getReachables();
		int u_all = getUncontrollableStates();

		bdd_reachable_uncontrollables = manager.and(r_all, u_all);
		has_reachable_uncontrollables = true;
	}

	// ---------------------------------------------------------------------
	public int getCoReachables()
	{    // get CoReachables from M
		if (!has_coreachables)
		{
			computeCoReachables();
		}

		return bdd_coreachables;
	}

    /*
      // DONT KNOW IF THESE ARE NEEDED
    protected int getTotalT() {
	int ret = 0;
	if(plant.isEmpty()) {
	    ret = spec.getT();
	    manager.ref(ret);
	} else if(spec.isEmpty()) {
	    ret = plant.getT();
	    manager.ref(ret);
	} else {
	    ret = manager.and(spec.getT(), plant.getT());
	}
	return ret;
    }

    protected int getTotalTNoEvents() {
	int ret = 0;
	if(plant.isEmpty()) 
	    ret = manager.exists(spec.getT(),  manager.getEventCube());
	else if(spec.isEmpty()) 
	    ret = manager.exists(plant.getT(),  manager.getEventCube());
	else 
	    ret = manager.relProd(spec.getT(), plant.getT(), manager.getEventCube() );

	return ret;
    }
    */
	private void computeCoReachables()
	{
		GrowFrame gf = null;;

		if (Options.show_grow)
		{
			gf = new GrowFrame("backward reachability");
		}

		timer.reset();

		int cube = manager.getStatepCube();
		int permute1 = manager.getPermuteS2Sp();
		int permute2 = manager.getPermuteSp2S();
		int t_all = manager.relProd(plant.getT(), spec.getT(), manager.getEventCube());

		// This one is tricky:
		// if spec is empty, then we cant assume that all events in P are marked
		// because then everything is marked (there is no spec, remember?)
		int m_all = spec.isEmpty() ? plant.getM() : spec.getM();


		// gets derefed in first orTo ??
		int r_all_p, r_all = manager.replace(m_all, permute1);    


		// manager.ref(r_all);
		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(t_all, r_all, cube);
			int tmp2 = manager.replace(tmp, permute1);

			manager.recursiveDeref(tmp);

			r_all = manager.orTo(r_all, tmp2);

			manager.recursiveDeref(tmp2);

			if (gf != null)
			{
				gf.add(manager.nodeCount(r_all));
			}
		}
		while (r_all_p != r_all);

		manager.recursiveDeref(t_all);

		int ret = manager.replace(r_all, permute2);

		manager.recursiveDeref(r_all);

		has_coreachables = true;
		bdd_coreachables = ret;

		timer.report("Co-reachables found");

		if (gf != null)
		{
			gf.stopTimer();
		}
	}

	public void trace_set(String what, int bdd, int max)
	{
		if (bdd == manager.getZero())
		{
			return;
		}

		System.out.println("---------------------------- Tracing '" + what + "' (max " + max + ")");

		int zero = manager.getZero();

		manager.ref(bdd);

		for (int i = 0; (i < max) && (bdd != zero); i++)
		{
			int next = manager.pickOneState(null, bdd);

			trace(what + " " + (i + 1), next);

			int not_next = manager.not(next);
			int new_bdd = manager.and(bdd, not_next);

			manager.recursiveDeref(bdd);
			manager.recursiveDeref(next);
			manager.recursiveDeref(not_next);

			bdd = new_bdd;
		}

		manager.recursiveDeref(bdd);
	}

	// --------------------------------------------------------------
	public void trace(String what, int to)
	{
		int i_all = manager.and(plant.getI(), spec.getI());

		trace(what, i_all, to);
		manager.recursiveDeref(i_all);
	}

	public void trace(String what, int from, int to)
	{
		Vector frontiers = new Vector();

		if (trace_hlp(from, to, frontiers))
		{
			String[] states = new String[manager.getSize()];
			int trace_len = frontiers.size();
			int ecube = manager.getEventCube();
			int cube = manager.getStatepCube();
			int permute1 = manager.getPermuteS2Sp();
			int permute2 = manager.getPermuteSp2S();

			// int t_all = manager.relProd(plant.getT(), spec.getT(), manager.getEventCube());
			int t_all = manager.and(plant.getT(), spec.getT());
			int here = manager.replace(to, permute1);

			System.out.println("\n*** " + what + " (backward trace)");
			manager.printAutomatonVector();

			// show the last state:
			int dumb = manager.pickOneState(states, to);

			manager.recursiveDeref(dumb);
			manager.printStateVector(states, "" + trace_len);

			Vector enames = new Vector(trace_len);
			String[] ename = new String[1];
			Object[] fronts = frontiers.toArray();

			// Note: "here" is in S' not S !!
			for (int i = trace_len - 1; i >= 0; i--)
			{
				int next_all = ((Integer) fronts[i]).intValue();
				int tran0 = manager.and(next_all, here);

				manager.recursiveDeref(next_all);

				int tran1 = manager.relProd(tran0, t_all, cube);

				manager.recursiveDeref(tran0);

				int e = manager.pickOneEvent(ename, tran1);

				manager.recursiveDeref(tran1);
				enames.add(ename[0]);

				int noe = manager.exists(e, ecube);

				manager.recursiveDeref(e);

				int s = manager.pickOneState(states, noe);

				manager.recursiveDeref(noe);
				manager.printStateVector(states, "" + i);
				manager.recursiveDeref(here);

				here = manager.replace(s, permute1);

				manager.recursiveDeref(s);
			}

			manager.recursiveDeref(here);
			manager.recursiveDeref(t_all);

			// ------------------- Show event trace
			System.out.println("\n*** Events leading to " + what + ":");

			int line_size = 0;

			for (int j = 0; j < enames.size(); j++)
			{
				if (j != 0)
				{
					System.out.print("-->");

					line_size += 3;
				}

				if (line_size > Options.LINE_WIDTH)
				{
					line_size = 0;

					System.out.println();
				}

				String e = (String) enames.get(trace_len - j - 1);

				if (e != null)
				{
					line_size += e.length();
				}

				System.out.print(e);
			}

			System.out.println("\n");
		}
		else
		{
			System.out.println("Trace failed for '" + what + "'");
		}
	}

	private boolean trace_hlp(int from, int to, Vector v)
	{
		int cube = manager.getStateCube();
		int permute = manager.getPermuteSp2S();
		int t_all = manager.relProd(plant.getT(), spec.getT(), manager.getEventCube());
		int r_all_p, r_all = from;
		int zero = manager.getZero();

		manager.ref(r_all);    // gets derefed by orTo and finally a recursiveDeref

		// the last state:
		manager.ref(from);
		v.add(new Integer(from));

		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(t_all, r_all, cube);
			int front = manager.replace(tmp, permute);

			manager.recursiveDeref(tmp);

			// check to see if we have found "to"
			int in = manager.and(front, to);

			if (in != zero)
			{
				manager.recursiveDeref(in);
				manager.recursiveDeref(t_all);

				return true;
			}

			manager.recursiveDeref(in);

			// save the state for further use
			v.add(new Integer(front));

			r_all = manager.orTo(r_all, front);
		}
		while (r_all_p != r_all);

		manager.recursiveDeref(t_all);

		return false;    // to not found
	}

	// --------------------------------------------------------------
	// TODO: se the note on getBR2()
	public int getBR1(int marked, int forbidden)
	{

		// note: dont use timer here (get reseted in getSafeStates)
		int good = manager.not(forbidden);
		int cube = manager.getStatepCube();
		int permute1 = manager.getPermuteS2Sp();
		int permute2 = manager.getPermuteSp2S();

		// again, we remove events as soon as possible
		int t_all = manager.relProd(plant.getT(), spec.getT(), manager.getEventCube());
		int r_all_p, r_all = manager.replace(marked, permute1);

		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(t_all, r_all, cube);

			tmp = manager.andTo(tmp, good);    // remove bad stuffs

			int tmp2 = manager.replace(tmp, permute1);

			manager.recursiveDeref(tmp);

			r_all = manager.orTo(r_all, tmp2);

			manager.recursiveDeref(tmp2);
		}
		while (r_all_p != r_all);

		manager.recursiveDeref(t_all);
		manager.recursiveDeref(good);

		int ret = manager.replace(r_all, permute2);

		manager.recursiveDeref(r_all);

		return ret;
	}

	// -------------------------------------------------------------------------
	// TODO: did we really get rid of events here??
	public int getBR2(int forbidden)
	{

		// note: dont use timer here (get reseted in getSafeStates)
		int sigma_2 = manager.getEventCube();
		int state_2 = manager.getStatepCube();
		int permute1 = manager.getPermuteS2Sp();
		int permute2 = manager.getPermuteSp2S();
		int t_all = manager.and(plant.getTu(), spec.getTu());
		int i_all = manager.and(plant.getI(), spec.getI());
		int cube = manager.and(state_2, sigma_2);
		int r_all_p, r_all = manager.replace(forbidden, permute1);

		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(t_all, r_all, cube);
			int tmp2 = manager.replace(tmp, permute1);

			manager.recursiveDeref(tmp);

			r_all = manager.orTo(r_all, tmp2);

			manager.recursiveDeref(tmp2);

			// System.out.print("2"); System.out.flush();
		}
		while (r_all_p != r_all);

		manager.recursiveDeref(t_all);
		manager.recursiveDeref(cube);

		int ret = manager.replace(r_all, permute2);

		manager.recursiveDeref(r_all);

		return ret;
	}

	// --------------------------------------------------------------------------------
	public int getSafeStates()
	{

		// note: dont use timer here (get reseted by getReachable)
		GrowFrame gf = null;

		if (Options.show_grow)
		{
			gf = new GrowFrame("Safe states: nodeCount(X)");
		}

		int xp, x = getReachableUncontrollables();
		int marked = spec.getM();    // we assume all states in P are marked

		manager.ref(x);

		do
		{
			xp = x;

			int qp_k = getBR1(marked, x);
			int not_qp_k = manager.not(qp_k);

			manager.recursiveDeref(qp_k);

			int qpp_k = getBR2(not_qp_k);

			x = manager.orTo(x, qpp_k);

			manager.recursiveDeref(qpp_k);

			if (gf != null)
			{
				gf.add(manager.nodeCount(x));
			}
		}
		while (x != xp);

		int not_x = manager.not(x);

		manager.recursiveDeref(x);

		// return not_x;
		// note: the only reason we get the reachable portion of it is to count it correctly!
		int not_x_reachable = manager.and(not_x, getReachables());

		manager.recursiveDeref(not_x);

		if (gf != null)
		{
			gf.stopTimer();
		}

		return not_x_reachable;
	}

	// ---------------------------------------------------------------------------------
	public int getDeadlocks()
	{
		timer.reset();

		int t_all = manager.and(plant.getT(), spec.getT());
		int cube = manager.and(spec.getCubep(), plant.getCubep());

		cube = manager.andTo(cube, manager.getEventCube());

		int t_noloops = manager.removeSelfLoops(t_all);

		manager.recursiveDeref(t_all);

		int t = manager.removeDontCareSp(t_noloops);

		manager.recursiveDeref(t_noloops);

		int s_go = manager.exists(t, cube);

		manager.recursiveDeref(t);
		manager.recursiveDeref(cube);

		int not_s_go = manager.not(s_go);

		manager.recursiveDeref(s_go);
		timer.report("Deadlock states found");

		int r = getReachables();

		timer.reset();

		int r_not_s_go = manager.and(not_s_go, r);

		manager.recursiveDeref(not_s_go);
		timer.report("Reachable deadlocks found");

		return r_not_s_go;
	}
}
