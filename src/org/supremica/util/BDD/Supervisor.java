package org.supremica.util.BDD;


import java.util.*;

// XXX: sythesis code NOT tested after we added these variables:
//        s_cube, sp_cube, e_cube, perm_s2sp, perm_sp2s;


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


	/** just declare these once for all subclasses ... */
	protected int s_cube, sp_cube, e_cube;
	protected int perm_s2sp, perm_sp2s;
	protected  FrontierSetOptimizer fso;

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
		fso = new FrontierSetOptimizer(manager);

		if (Options.debug_on)
		{
			plant.dump();
			spec.dump();
		}

		has_uncontrollables = has_reachables = has_reachable_uncontrollables = has_coreachables = false;


		e_cube = manager.getEventCube();
		s_cube = manager.getStateCube();
		sp_cube = manager.getStatepCube();
		perm_s2sp = manager.getPermuteS2Sp();
		perm_sp2s = manager.getPermuteSp2S();

	}

	public void cleanup()
	{
		fso.cleanup();

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

		if(plant.getCleanup())	plant.cleanup();
		if(spec.getCleanup())	spec.cleanup();
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
	/**
	 * returns an info string with the some relevant options highlighted
	 *
	 */
	protected String type() {
		StringBuffer sb = new StringBuffer();
		sb.append("-");
		sb.append(Options.REACH_ALGO_NAMES[Options.algo_family]);
		sb.append(": ");

		// frontier set
		if( Options.algo_family == Options.ALGO_MONOLITHIC  ||
			Options.algo_family == Options.ALGO_CONJUNCTIVE
			) {
			sb.append(Options.FRONTIER_STRATEGY_NAMES[Options.frontier_strategy]);
			sb.append("/");
		}

		// event-selection
		if(	Options.algo_family == Options.ALGO_PETRINET ||
			Options.algo_family == Options.ALGO_DISJUNCTIVE_WORKSET  ||
			Options.algo_family == Options.ALGO_SMOOTHED_MONO_WORKSET) {
			sb.append( Options.ES_HEURISTIC_NAMES[Options.es_heuristics]);
			sb.append( " + NDAS:");

			sb.append( Options.NDAS_HEURISTIC_NAMES[Options.ndas_heuristics]);
			sb.append( ". ");
		}


		return sb.toString();
	}

	// ------------------------------------------------------------------------------

	/**
	 * this functions retuns the events that are "local" for the current Plant and Spec.
	 * this means that they are not used outside this set of automata (if there are any
	 * others in the BDDAutomata).
	 *
	 * @param tmp_v temporary int vector (size of the events)
	 * @param are_local true if that event is local
	 * @returns the number of local events
	 */
	public int getLocalEvents(int [] tmp_v, boolean [] are_local) {
		int count = 0;
		int len = tmp_v.length;

		manager.getEventManager().getUsageCount(tmp_v);

		// this trick will exluce those that are never used anywhere!
		for(int i = 0; i < len; i++)
			if(tmp_v[i] == 0) count--;

		plant.removeEventUsage(tmp_v);
		spec.removeEventUsage(tmp_v);

		for(int i = 0; i < len; i++) {
			if(tmp_v[i] < 1) {
				count++;
				are_local[i] = true;
			} else {
				are_local[i] = false;
			}
		}


		return count;
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

		bdd_uncontrollables = computeLanguageDifference(sigma_u, true);
		has_uncontrollables = true;

		SizeWatch.setOwner("Supervisor.computeUncontrollables");
		SizeWatch.report(sigma_u, "Sigma_u");
		SizeWatch.report(bdd_uncontrollables, "Q_nc");

		manager.deref(sigma_u);

		timer.report("Uncontrollable states found");
	}


	/**
	 * compute language difference of plant and spec considreing only the given events.
	 * returns a BDD as a counter example. retunrs < states> if remove_events is trues
	 * otherwise returns <states, events> instead...
	 *
	 */

    protected int computeLanguageDifference(int considred_events, boolean remove_events)
    {
	int t_sp = spec.getT();
	int t_p = plant.getT();
	int cubep_sp = spec.getCubep();
	int cubep_p = plant.getCubep();




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

	int tmp4;
	if(remove_events) {
		int cube2 = manager.and(e_cube, cubep_p);
		tmp4 = manager.relProd(t_p, tmp2, cubep_p);
		manager.deref(cube2);
	} else {
		tmp4 = manager.relProd(t_p, tmp2, cubep_p);
	}

	// DEBUG:
	// manager.printSet(tmp4);

	manager.deref(tmp2);

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
	int ret = computeLanguageDifference(plant.getSigma(), true);
	timer.report("Uncontrollable states found");
	return ret;
    }


    /**
     * <b>Reachable</b> counterexamples to the language inclusions check
     * <br>
     * @retruns a BDD for states that fail the test (see also below)
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


    /**
     * <b>Reachable</b> counterexamples to the language inclusions check.
     *
     * only includes events in 'event_care'-
     * NOTE:<br>
     * the return value of this one is failed reachable states PLUS the
     * corresponding event: ret = BDD<state, event>
     */
 	public int computeReachableLanguageDifference(int event_care) {

		int ld = computeLanguageDifference(event_care, false);


		if(ld == manager.getZero()) {
			// nothing there, so we dont need the intersection with reachables
			// to get the reachable difference
			Options.out.println("Language diff is empty!");
			return ld;
		}


		int r = getReachables();
		int intersection = manager.and(ld,r);
		manager.deref(ld);
		return intersection;
    }

	// --------------------------------------------------------------------------------------------

	/**
	 * do a FORWARD reachability search.
	 * start from the given (set of) initial state(s)
	 */
	public int getReachables(int initial_states) {
		int t_all = manager.relProd(plant.getT(), spec.getT(), e_cube);
		int x = internal_computeReachables(t_all, initial_states);
		manager.deref(t_all);
		return x;
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
		int tmp = manager.and(plant.getT(), event_mask);
		int t_all = manager.relProd(tmp, spec.getT(), e_cube);
		manager.deref(tmp);
		manager.deref(event_mask);
		int x = internal_computeReachables(t_all, intial_states);
		manager.deref(t_all);
		return x;
	}

	/**
	 * do a reachability search, use all events ;
	 * start from the initial state
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
		int t_all = manager.relProd(plant.getT(), spec.getT(), e_cube);
		int i_all = manager.and(plant.getI(), spec.getI());
		bdd_reachables = internal_computeReachables(t_all, i_all);
		has_reachables = true;
		manager.deref(i_all);
		manager.deref(t_all);
	}

	/**
	 * do a reachability search,
	 * use the given transition relation.
	 * use the given initial state(s)
	 */
	protected int internal_computeReachables(int t_all, int i_all) {
	// Note: we remove events from t_all, it is needed for forward reachability
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability" + type());


		timer.reset();
		SizeWatch.setOwner("Supervisor.computeReachables");


		// int i_all = manager.and(plant.getI(), spec.getI());
		int r_all_p, r_all = i_all;

		manager.ref(r_all);    // gets derefed by orTo and finally a recursiveDeref
		int front = manager.ref(r_all); // has its own ref problems...

		SizeWatch.report(t_all, "T");

		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(t_all, front, s_cube);
			int tmp2 = manager.replace(tmp, perm_sp2s);
			manager.deref(tmp);
			manager.deref(front);
			r_all = manager.orTo(r_all, tmp2);
			front = fso.choose(r_all, tmp2); // Takes care of tmp2!


			if (gf != null)
			{
				gf.add( r_all );
			}
		} while (r_all_p != r_all);

		manager.deref(front);

		SizeWatch.report(r_all, "R");
		if(gf != null) gf.stopTimer();
		timer.report("Forward reachables found");
		return r_all;
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
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Backward reachability" + type());


		timer.reset();

		int t_all = manager.relProd(plant.getT(), spec.getT(), e_cube);
		int m_all = GroupHelper.getM(manager,spec, plant);


		SizeWatch.setOwner("Supervisor.computeCoReachables");
		SizeWatch.report(t_all, "T");
		SizeWatch.report(m_all, "Qm");

		// gets derefed in first orTo ??
		int r_all_p, r_all = manager.replace(m_all, perm_s2sp);
		int front = manager.ref(r_all);

		// manager.ref(r_all);
		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(t_all, front, sp_cube);
			int tmp2 = manager.replace(tmp, perm_s2sp);

			manager.deref(tmp);
			manager.deref(front);

			r_all = manager.orTo(r_all, tmp2);
			front = fso.choose(r_all, tmp2); // Takes care of tmp2!



			if (gf != null)
			{
				gf.add( r_all );
			}
		}
		while (r_all_p != r_all);

		manager.deref(front);
		manager.deref(t_all);
		manager.deref(m_all);

		int ret = manager.replace(r_all, perm_sp2s);
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
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability with constriant");


	timer.reset();


	int t_all = manager.relProd(plant.getT(), spec.getT(), e_cube);
	int i_all = manager.and(plant.getI(), spec.getI());
	int r_all_p, r_all = i_all;

	manager.ref(i_all);    // gets derefed by orTo and finally a recursiveDeref


	SizeWatch.setOwner("Supervisor.computeReachableSubset");
	SizeWatch.report(t_all, "T");
	SizeWatch.report(set, "set");


	do {
	    r_all_p = r_all;

	    int tmp = manager.relProd(t_all, r_all, s_cube);
	    int tmp2 = manager.replace(tmp, perm_sp2s);
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
		    gf.add( r_all );
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

		Options.out.println("---------------------------- Tracing '" + what + "' (max " + max + ")");

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


	// XXX: this algo uses all automata in the BDDAutomata, which is sometimes (in modular algorithms, for example)
	//      far larger then the groups (plant and spec) given this supervisor...
	public void trace(String what, int from, int to)
	{
		Vector frontiers = new Vector();

		if (trace_hlp(from, to, frontiers))
		{
			String[] states = new String[manager.getSize()];
			int trace_len = frontiers.size();

			int t_all = manager.and(plant.getT(), spec.getT());
			int here = manager.replace(to, perm_s2sp);

			Options.out.println("\n*** " + what + " (backward trace)");
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

				int tran1 = manager.relProd(tran0, t_all, sp_cube);

				manager.deref(tran0);

				int e = manager.pickOneEvent(ename, tran1);

				manager.deref(tran1);
				enames.add(ename[0]);

				int noe = manager.exists(e, e_cube);

				manager.deref(e);

				int s = manager.pickOneState(states, noe);

				manager.deref(noe);
				manager.printStateVector(states, "" + i);
				manager.deref(here);

				here = manager.replace(s, perm_s2sp);

				manager.deref(s);
			}

			manager.deref(here);
			manager.deref(t_all);

			// ------------------- Show event trace
			Options.out.println("\n*** Events leading to " + what + ":");

			int line_size = 0;

			for (int j = 0; j < enames.size(); j++)
			{
				if (j != 0)
				{
					Options.out.print("-->");

					line_size += 3;
				}

				if (line_size > Options.LINE_WIDTH)
				{
					line_size = 0;

					Options.out.println();
				}

				String e = (String) enames.get(trace_len - j - 1);

				if (e != null)
				{
					line_size += e.length();
				}

				Options.out.print(e);
			}

			Options.out.println("\n");
		}
		else
		{
			Options.out.println("Trace failed for '" + what + "'");
		}
	}

	private boolean trace_hlp(int from, int to, Vector v)
	{
		int t_all = manager.relProd(plant.getT(), spec.getT(), e_cube);
		int r_all_p, r_all = from;
		int zero = manager.getZero();

		manager.ref(r_all);    // gets derefed by orTo and finally a recursiveDeref

		// the last state:
		manager.ref(from);
		v.add(new Integer(from));

		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(t_all, r_all, s_cube);
			int front = manager.replace(tmp, perm_sp2s);

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

		// again, we remove events as soon as possible
		int t_all = manager.relProd(plant.getT(), spec.getT(), e_cube);
		int r_all_p, r_all = manager.replace(marked, perm_s2sp);

		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(t_all, r_all, sp_cube);

			tmp = manager.andTo(tmp, good);    // remove bad stuffs

			int tmp2 = manager.replace(tmp, perm_s2sp);

			manager.deref(tmp);

			r_all = manager.orTo(r_all, tmp2);

			manager.deref(tmp2);
		}
		while (r_all_p != r_all);

		manager.deref(t_all);
		manager.deref(good);

		int ret = manager.replace(r_all, perm_sp2s);

		manager.deref(r_all);

		return ret;
	}

	// -------------------------------------------------------------------------
	// TODO: did we really get rid of events here??
	public int getBR2(int forbidden)
	{

		// note: dont use timer here (get reseted in getSafeStates)


		int t_all = manager.and(plant.getTu(), spec.getTu());
		int i_all = manager.and(plant.getI(), spec.getI());
		int cube = manager.and(sp_cube, e_cube);
		int r_all_p, r_all = manager.replace(forbidden, perm_s2sp);

		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(t_all, r_all, cube);
			int tmp2 = manager.replace(tmp, perm_s2sp);
			manager.deref(tmp);

			r_all = manager.orTo(r_all, tmp2);
			manager.deref(tmp2);
		}
		while (r_all_p != r_all);

		manager.deref(t_all);
		manager.deref(cube);

		int ret = manager.replace(r_all, perm_sp2s);

		manager.deref(r_all);

		return ret;
	}

	// --------------------------------------------------------------------------------
	// this is used to generate the supervisor??
	public int getUnsafeTransitions(int safe_states)
	{

		// unsafe_transitions = { (q,sigma,q') |
		//            \delta(q,sigma) = ´q´ \land q \in good_states \land q' \in bad_states }

		// int good_states = getSafeStates();

		int bad_states = manager.not(safe_states);
		int bad_statesp = manager.replace(bad_states, perm_s2sp);

		manager.deref(bad_states);

		int t_all = manager.and(plant.getT(), spec.getT());

		int tmp = manager.and(t_all, safe_states);
		tmp = manager.andTo(tmp, bad_statesp);
		manager.deref(t_all);
		manager.deref(bad_statesp);

		// tmp: Q x E -> Q, but we only need the first part, ie: tmp2: Q x E ??
		// (note: this doesnt seem to work :(

		return tmp;

	}

	/** get the list of unsafe transitions */
	public Vector getUnsafeTransitionList(int safe_states)
	{
		int unsafe = getUnsafeTransitions(safe_states);
		Event[] events = manager.getEvents();
		int events_size = events.length;


		Vector results = new Vector();
		for(int i = 0; i < events_size; i++)
		{
			int states_event = manager.relProd( unsafe, events[i].bdd, e_cube);
			IncompleteStateList isl = manager.getIncompleteStateList(states_event, events[i]);
			DisablingPoint dp = new DisablingPoint(isl, events[i]);
			results.add(dp);
			manager.deref(states_event);
		}
		manager.deref(unsafe);
		return results;
	}


	/** get the _tree_ of unsafe states */
	public Vector getUnsafeTransitionTree(int safe_states)
	{
		// TODO: do we need to remove E x Q' with relProd, or can we remove Q' before the loop??
		int unsafe = getUnsafeTransitions(safe_states);
		int cube = manager.and(e_cube, sp_cube );
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
		manager.deref(unsafe);

		return results;
	}
	// --------------------------------------------------------------------------------
	public int getSafeStates(boolean nb, boolean c) {
		if(nb && c) return getSafeStatesNBC();
		if(nb) return getSafeStatesNB();
		if(c) return getSafeStatesC();

		return manager.ref( manager.getOne() ); // what the hell are we doing??
	}

	/** return Q[supNB] */
	public int getSafeStatesNB() {
		int forbidden = manager.ref( manager.getZero() );
		int marked = GroupHelper.getM(manager,spec, plant);
		int ret = getBR1(marked, forbidden);

		manager.deref(marked);
		manager.deref(forbidden);

		return ret;
	}

	/** return Q[supC] */
	public int getSafeStatesC() {
		// XXX: dont try getUncontrollableStates(), we would need to conpute reachables to get the border anyway :(
		return getReachableUncontrollables();
	}

	/** return Q[supNBC] */
	public int getSafeStatesNBC()
	{

		// note: dont use timer here (get reseted by getReachable)
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Safe states: nodeCount(X)");

		int xp, x = getReachableUncontrollables();
		int marked = GroupHelper.getM(manager,spec, plant);

		manager.ref(x);

		do
		{
			xp = x;

			int qp_k = getBR1(marked, x);
			int not_qp_k = manager.not(qp_k);
			manager.deref(qp_k);

			int qpp_k = getBR2(not_qp_k);

			x = manager.orTo(x, qpp_k);

			manager.deref(qpp_k);

			if (gf != null)
			{
				gf.add( x );
			}
		}
		while (x != xp);

		manager.deref(marked);
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

		cube = manager.andTo(cube, e_cube);

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
