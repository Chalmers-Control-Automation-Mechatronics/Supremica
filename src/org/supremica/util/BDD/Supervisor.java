package org.supremica.util.BDD;

// NEW CUDD: fixed
import java.util.*;

public class Supervisor
{
	protected Group plant;
	protected Group spec;
	protected BDDAutomata manager;
	protected Timer timer;
	protected boolean has_uncontrollables, has_reachables,
	    has_reachable_uncontrollables, has_coreachables;
	protected int bdd_uncontrollables, bdd_reachables,
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

			manager.deref(bdd_reachable_uncontrollables);
		}

		if (has_uncontrollables)
		{
			has_uncontrollables = false;

			manager.deref(bdd_uncontrollables);
		}

		if (has_reachables)
		{
			has_reachables = false;

			manager.deref(bdd_reachables);
		}

		if (has_coreachables)
		{
			has_coreachables = false;

			manager.deref(bdd_coreachables);
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

	protected void computeUncontrollables()
	{
		timer.reset();

		int sigma_u = manager.and( plant.getSigmaU(), spec.getSigmaU()); // WAS manager.getSigmaU();

		bdd_uncontrollables = computeLanguageDifference(sigma_u);
		has_uncontrollables = true;

		SizeWatch.setOwner("Supervisor.computeUncontrollables");
		SizeWatch.report(sigma_u, "Sigma_u");
		SizeWatch.report(bdd_uncontrollables, "Q_nc");

		manager.deref(sigma_u);

		timer.report("Uncontrollable states found");
	}


    protected int computeLanguageDifference(int considred_events)
    {
	int t_sp = spec.getT();
	int t_p = plant.getT();
	int cubep_sp = spec.getCubep();
	int cubep_p = plant.getCubep();
	int sigma_cube = manager.getEventCube();



	SizeWatch.setOwner("Supervisor.computeLanguageDifference");
	SizeWatch.report(t_sp, "Tsp");
	SizeWatch.report(t_p, "Tp");

	int tmp10 = manager.exists(t_sp, cubep_sp);
	int tmp1 = manager.not(tmp10);
	manager.deref(tmp10);
	SizeWatch.report(tmp1, "~Eq'sp. Tsp");

	int tmp2 = manager.and(tmp1, considred_events);
	manager.deref(tmp1);

	SizeWatch.report(tmp2, "~Eq'sp. Tsp ^ (sigma in some Sigma)");

	int cube2 = manager.and(sigma_cube, cubep_p);
	int tmp4 = manager.relProd(t_p, tmp2, cube2);

	manager.deref(tmp2);
	manager.deref(cube2);

	SizeWatch.report(tmp4, "(Language diff)");

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
	manager.deref(ld);
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
	 *       manager.deref(tmp);
	 *       r_all = manager.orTo(r_all, tmp2);
	 *       manager.deref(tmp2);
	 *       if(gf != null)      gf.add( manager.nodeCount( r_all));
	 *   } while(r_all_p != r_all);
	 *
	 *   manager.deref(t_all);
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

	protected void computeReachables()
	{

		// Note: we remove events from t_all, it is needed for forward reachability
		GrowFrame gf = null;

		if (Options.show_grow)
		{
			gf = new GrowFrame("Forward reachability");
		}

		timer.reset();
		SizeWatch.setOwner("Supervisor.computeReachables");

		int cube = manager.getStateCube();
		int permute = manager.getPermuteSp2S();
		int t_all = manager.relProd(plant.getT(), spec.getT(), manager.getEventCube());
		int i_all = manager.and(plant.getI(), spec.getI());
		int r_all_p, r_all = i_all;

		manager.ref(i_all);    // gets derefed by orTo and finally a recursiveDeref


		SizeWatch.report(t_all, "T");

		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(t_all, r_all, cube);
			int tmp2 = manager.replace(tmp, permute);
			manager.deref(tmp);


			r_all = manager.orTo(r_all, tmp2);
			manager.deref(tmp2);

			if (gf != null)
			{
				gf.add(manager.nodeCount(r_all));
			}
		}
		while (r_all_p != r_all);

		manager.deref(i_all);
		manager.deref(t_all);




		has_reachables = true;
		bdd_reachables = r_all;

		SizeWatch.report(r_all, "R");
		if(gf != null) gf.stopTimer();
		timer.report("Forward reachables found");


	}

    // ----------------------------------------------------

    /**
     * Used for verification: returns unknown amount of unreachables that are controllable
     * returns 0 only if there are no such states.
     * Use this function to VERIFY, not SYNTHESIS anything that has to do
     * with uncontrollable states
     * NOTE: returned value must be de-refed by user
     */
    public int getSomeReachableUncontrollables()
    {
	// since its pre-computed, lets use this one
	if (has_reachable_uncontrollables)
	    {
		manager.ref(bdd_reachable_uncontrollables);
		return bdd_reachable_uncontrollables;
	    }

	// If we are optimistic (we dont expect any reachable uncontrollables, its
	// probably better to go for the complete search (which gurantees controllablilty)
	// then do iterative tests

	if(Options.uc_optimistic) {
	    int u_all = getReachableUncontrollables();
	    manager.ref(u_all); // add extra ref, since user has to deref this later
	    return u_all;
	} else {
	    // note: dont use timer here (get reseted in two places below)
	    int u_some = getUncontrollableStates();
	    return getReachableSubset(u_some); // need no extra ref (not shared)
	}

    }

    // -----------------------------------------------------------
    /**
     * Computes ALL uncotrollable states that are reachable.
     * This implies finding all uncontrollable states and all reachables states (full FR).
     */
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

		SizeWatch.setOwner("Supervisor.computeReachableUncontrollables");
		SizeWatch.report(r_all, "Qr");
		SizeWatch.report(u_all, "Qnc");
		SizeWatch.report(bdd_reachable_uncontrollables, "Qu");

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

	protected void computeCoReachables()
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
		int m_all = GroupHelper.getM(manager,spec, plant);


		SizeWatch.setOwner("Supervisor.computeCoReachables");
		SizeWatch.report(t_all, "T");
		SizeWatch.report(m_all, "Qm");

		// gets derefed in first orTo ??
		int r_all_p, r_all = manager.replace(m_all, permute1);


		// manager.ref(r_all);
		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(t_all, r_all, cube);
			int tmp2 = manager.replace(tmp, permute1);

			manager.deref(tmp);

			r_all = manager.orTo(r_all, tmp2);

			manager.deref(tmp2);

			if (gf != null)
			{
				gf.add(manager.nodeCount(r_all));
			}
		}
		while (r_all_p != r_all);

		manager.deref(t_all);
		manager.deref(m_all);

		int ret = manager.replace(r_all, permute2);
		manager.deref(r_all);


		has_coreachables = true;
		bdd_coreachables = ret;


		if (gf != null)
		{
			gf.stopTimer();
		}
		SizeWatch.report(bdd_coreachables, "Qco");
		timer.report("Co-reachables found");

	}

    // ------------------------------------------------------------------------
    /**
     * having a set x, subset of Q, we compute x intersection Q_reachable.
     * If Q_reachable is not chaced, we compute reachability in out own way
     */
    public int getReachableSubset(int set)
    {    // if already caches, use it
	if (has_reachables)
	    {
		return manager.and(set, bdd_reachables);
	    }

	return computeReachableSubset(set);
    }

    protected int computeReachableSubset(int set) {
	GrowFrame gf = null;

	if (Options.show_grow)
	    {
		gf = new GrowFrame("Forward reachability with constriant");
	    }

	timer.reset();

	int cube = manager.getStateCube();
	int permute = manager.getPermuteSp2S();
	int t_all = manager.relProd(plant.getT(), spec.getT(), manager.getEventCube());
	int i_all = manager.and(plant.getI(), spec.getI());
	int r_all_p, r_all = i_all;

	manager.ref(i_all);    // gets derefed by orTo and finally a recursiveDeref


	SizeWatch.setOwner("Supervisor.computeReachableSubset");
	SizeWatch.report(t_all, "T");
	SizeWatch.report(set, "set");


	do {
	    r_all_p = r_all;

	    int tmp = manager.relProd(t_all, r_all, cube);
	    int tmp2 = manager.replace(tmp, permute);
	    manager.deref(tmp);


	    int intersection = manager.and(set, tmp2);
	    if(intersection != manager.getZero()) {

		// clean up
		manager.deref(r_all);
		manager.deref(t_all);
		manager.deref(i_all);
		manager.deref(tmp2);

		SizeWatch.report(intersection, "set intersection Qr");
		timer.report("Forward reachablility with constraint");
		return intersection;
	    }

	    r_all = manager.orTo(r_all, tmp2);
	    manager.deref(tmp2);

	    if (gf != null)
		{
		    gf.add(manager.nodeCount(r_all));
		}
	}
	while (r_all_p != r_all);


	// clean up
	manager.deref(i_all);
	manager.deref(t_all);

	// since we got reachables anyway, lets save it
	has_reachables = true;
	bdd_reachables = r_all;

	if(gf != null) gf.stopTimer();
	timer.report("Forward reachablility with constraint [none found]");
	SizeWatch.report(bdd_reachables, "Qr");


	// nothing to report, return 0
	int ret = manager.getZero();
	manager.ref(ret);
	return ret;
    }

    // -----------------------------------------------------------------------
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

			manager.deref(bdd);
			manager.deref(next);
			manager.deref(not_next);

			bdd = new_bdd;
		}

		manager.deref(bdd);
	}

	// --------------------------------------------------------------
	public void trace(String what, int to)
	{
		int i_all = manager.and(plant.getI(), spec.getI());

		trace(what, i_all, to);
		manager.deref(i_all);
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

			manager.deref(dumb);
			manager.printStateVector(states, "" + trace_len);

			Vector enames = new Vector(trace_len);
			String[] ename = new String[1];
			Object[] fronts = frontiers.toArray();

			// Note: "here" is in S' not S !!
			for (int i = trace_len - 1; i >= 0; i--)
			{
				int next_all = ((Integer) fronts[i]).intValue();
				int tran0 = manager.and(next_all, here);

				manager.deref(next_all);

				int tran1 = manager.relProd(tran0, t_all, cube);

				manager.deref(tran0);

				int e = manager.pickOneEvent(ename, tran1);

				manager.deref(tran1);
				enames.add(ename[0]);

				int noe = manager.exists(e, ecube);

				manager.deref(e);

				int s = manager.pickOneState(states, noe);

				manager.deref(noe);
				manager.printStateVector(states, "" + i);
				manager.deref(here);

				here = manager.replace(s, permute1);

				manager.deref(s);
			}

			manager.deref(here);
			manager.deref(t_all);

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

			manager.deref(tmp);

			// check to see if we have found "to"
			int in = manager.and(front, to);

			if (in != zero)
			{
				manager.deref(in);
				manager.deref(t_all);

				return true;
			}

			manager.deref(in);

			// save the state for further use
			v.add(new Integer(front));

			r_all = manager.orTo(r_all, front);
		}
		while (r_all_p != r_all);

		manager.deref(t_all);

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

			manager.deref(tmp);

			r_all = manager.orTo(r_all, tmp2);

			manager.deref(tmp2);
		}
		while (r_all_p != r_all);

		manager.deref(t_all);
		manager.deref(good);

		int ret = manager.replace(r_all, permute2);

		manager.deref(r_all);

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

			manager.deref(tmp);

			r_all = manager.orTo(r_all, tmp2);

			manager.deref(tmp2);

			// System.out.print("2"); System.out.flush();
		}
		while (r_all_p != r_all);

		manager.deref(t_all);
		manager.deref(cube);

		int ret = manager.replace(r_all, permute2);

		manager.deref(r_all);

		return ret;
	}

	// --------------------------------------------------------------------------------
	// this is used to generate the supervisor??
	public int getUnsafeTransitions()
	{

		// unsafe_transitions = { (q,sigma,q') |
		//            \delta(q,sigma) = ´q´ \land q \in good_states \land q' \in bad_states }

		int good_states = getSafeStates();

		int bad_states = manager.not(good_states);
		int s2sp = manager.getPermuteS2Sp();
		int bad_statesp = manager.replace(bad_states, s2sp);

		// DEBUG:
		// System.out.println("BAD states are: "); manager.show_states(bad_states);
		// System.out.println("GOOD states are: "); manager.show_states(good_states);

		manager.deref(bad_states);

		int t_all = manager.and(plant.getT(), spec.getT());

		int tmp = manager.and(t_all, good_states);
		tmp = manager.andTo(tmp, bad_statesp);
		manager.deref(t_all);
		manager.deref(good_states);
		manager.deref(bad_statesp);

		// tmp: Q x E -> Q, but we only need the first part, ie: tmp2: Q x E ??
		// (note: this doesnt seem to work :(

		return tmp;

	}

	/** get the list of unsafe transitions */
	public Vector getUnsafeTransitionList()
	{
		int unsafe = getUnsafeTransitions();
		int events_cube = manager.getEventCube();
		Event[] events = manager.getEvents();
		int events_size = events.length;


		Vector results = new Vector();
		for(int i = 0; i < events_size; i++)
		{
			int states_event = manager.relProd( unsafe, events[i].bdd, events_cube);
			IncompleteStateList isl = manager.getIncompleteStateList(states_event, events[i]);
			DisablingPoint dp = new DisablingPoint(isl, events[i]);
			results.add(dp);
			manager.deref(states_event);
		}
		return results;
	}
	/** get the _tree_ of unsafe states */
	public Vector getUnsafeTransitionTree()
	{
		// TODO: do we need to remove E x Q' with relProd, or can we remove Q' before the loop??
		int unsafe = getUnsafeTransitions();
		int cube = manager.and(manager.getEventCube(), manager.getStatepCube() );
		Event[] events = manager.getEvents();
		int events_size = events.length;



		Vector results = new Vector();
		for(int i = 0; i < events_size; i++)
		{
			int states_event = manager.relProd( unsafe, events[i].bdd, cube);

			BDDStateTreeExplorer ste = new BDDStateTreeExplorer(manager);
			IncompleteStateTree ist = ste.getCompleteStateTree(states_event);
			DisablingPoint dp = new DisablingPoint(ist, events[i]);
			results.add(dp);
			manager.deref(states_event);
		}

		manager.deref(cube);

		return results;
	}
	// --------------------------------------------------------------------------------
	public int getSafeStates()
	{

		// note: dont use timer here (get reseted by getReachable)
		GrowFrame gf = null;

/* TEMPORARY OFF:
		if (Options.show_grow)
		{
			gf = new GrowFrame("Safe states: nodeCount(X)");
		}
*/
		int xp, x = getReachableUncontrollables();
		int marked = GroupHelper.getM(manager,spec, plant);

		manager.ref(x);

		// System.out.println("marked = "); manager.show_states(marked); // DEBUG
		do
		{
			xp = x;

			// System.out.println("x = "); manager.show_states(x); // DEBUG

			int qp_k = getBR1(marked, x);
			int not_qp_k = manager.not(qp_k);

			// System.out.println("qp_k = "); manager.show_states(qp_k); // DEBUG

			manager.deref(qp_k);



			int qpp_k = getBR2(not_qp_k);

			//System.out.println("qpp_k = "); manager.show_states(qpp_k); // DEBUG

			x = manager.orTo(x, qpp_k);

			manager.deref(qpp_k);

			if (gf != null)
			{
				gf.add(manager.nodeCount(x));
			}
		}
		while (x != xp);

		int not_x = manager.not(x);

		manager.deref(x);

		// return not_x;
		// note: the only reason we get the reachable portion of it is to count it correctly!
		int not_x_reachable = manager.and(not_x, getReachables());

		manager.deref(not_x);

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

		manager.deref(t_all);

		int t = manager.removeDontCareSp(t_noloops);

		manager.deref(t_noloops);

		int s_go = manager.exists(t, cube);

		manager.deref(t);
		manager.deref(cube);

		int not_s_go = manager.not(s_go);

		manager.deref(s_go);
		timer.report("Deadlock states found");

		int r = getReachables();

		timer.reset();

		int r_not_s_go = manager.and(not_s_go, r);

		manager.deref(not_s_go);
		timer.report("Reachable deadlocks found");

		return r_not_s_go;
	}
}
