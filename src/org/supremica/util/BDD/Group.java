package org.supremica.util.BDD;

// NEW CUDD: FIXED
public class Group
{
	private int capacity, size;
	private BDDAutomaton[] members;
	private BDDAutomata manager;
	private String name;
	private int bdd_i, bdd_cube, bdd_cubep, bdd_sigma, bdd_sigma_u, bdd_t, bdd_m, bdd_tu;
	private boolean has_t, has_tu, has_m;
	/* if this flags is set, the Supervisor derivats should not attempt to cleanup this group after they are done,
	 * since it is probably owned by an incremental algorithm that will _reuse_ this group in the next run and
	 * hopefully :) clean it up when the algorithm terminates ...*/
	private boolean cleanup_flag = true;


    public Group(BDDAutomata manager, BDDAutomaton [] automata, String name)
    {
		this(manager, automata, null, name);
    }
    public Group(BDDAutomata manager, BDDAutomaton [] automata, GroupMembership member, String name)
    {
		this(manager, automata.length, name);
		init();
		for(int i = 0; i < automata.length; i++)
		    if(member == null || member.shouldInclude(automata[i]))
		    {
				add(automata[i]);
			}
    }

	public Group(BDDAutomata manager, int max_capacity, String name)
	{
		this.manager = manager;
		this.name = name;
		capacity = max_capacity;
		size = 0;
		members = new BDDAutomaton[capacity];
		init();

	}



	/* ------------------------------------------------------------------ */
	/** empty the group and start from the beginning */
	public void empty() {
		cleanup();
		init();
		size = 0;
	}

    private void init() {

    	// no pre-calculations are valid
		has_t = false;
		has_tu = false;
		has_m = false;

		bdd_i = manager.getOne();
		manager.ref(bdd_i);

		bdd_cube = manager.getOne();
		manager.ref(bdd_cube);

		bdd_cubep = manager.getOne();
		manager.ref(bdd_cubep);

		bdd_sigma_u = manager.getZero();
		manager.ref(bdd_sigma_u);

		bdd_sigma = manager.getZero();
		manager.ref(bdd_sigma);



    }

	/**
	 * standard cleanup code
	 * Note: strange things will happen if you try to use this object _after_ calling cleanup :)
   	 */
	public void cleanup()
	{
		reset();

		manager.deref(bdd_i);
		manager.deref(bdd_cube);
		manager.deref(bdd_cubep);
		manager.deref(bdd_sigma_u);
		manager.deref(bdd_sigma);
	}

	private void reset()
	{

		// something changed, pre-calculations are not valid anymore
		if (has_t)
		{
			manager.deref(bdd_t);
			has_t = false;
		}

		if (has_tu)
		{
			manager.deref(bdd_tu);
			has_tu = false;
		}

		if(has_m)
		{
			manager.deref(bdd_m);
			has_m = false;
		}
	}

	/** ---------------------------------------------------------------- */

	public void add(BDDAutomaton a)
	{
		BDDAssert.internalCheck(size < capacity, "[Group.add] Group size exceeded");

		members[size] = a;

		size++;

		bdd_i = manager.andTo(bdd_i, a.getI());
		bdd_cube = manager.andTo(bdd_cube, a.getCube());
		bdd_cubep = manager.andTo(bdd_cubep, a.getCubep());
		bdd_sigma   = manager.orTo(bdd_sigma, a.getSigma());
		bdd_sigma_u = manager.orTo(bdd_sigma_u, a.getSigmaU());

		// new optimization: dont recompute everything when we add a new automaya!
		// try to modify the previous answer instead!

		// WAS: reset();

		if(has_t) {
			bdd_t = manager.andTo(bdd_t, a.getTpri());
		}

		if(has_m) {
			bdd_m = manager.andTo(bdd_m, a.getM());
		}

		if(has_tu) {
			// not this one! it cant be computed incrementally!:
			// int t = getT();
			// bdd_tu = manager.and(t, bdd_sigma_u);
			manager.deref(bdd_tu);
			has_tu = false;
		}
	}

	/** ---------------------------------------------------------------- */
    public boolean isEmpty()
    {
		return size == 0;
    }

	public int getSize()
	{
		return size;
	}

	public int getCapacity()
	{
		return capacity;
	}

	public BDDAutomaton[] getMembers()
	{
		return members;
	}

	public int getI()
	{
		return bdd_i;
	}


	public int getSigma()
	{
		return bdd_sigma;
	}

	public int getSigmaU()
	{
		return bdd_sigma_u;
	}

	public int getCube()
	{
		return bdd_cube;
	}

	public int getCubep()
	{
		return bdd_cubep;
	}


	/** careSet[i] is true if the Event is used by any automata in this group */
	public boolean [] getEventCareSet(boolean uncontrollable_events_only)
	{
		int es = manager.getEvents().length;
		boolean [] ret = new boolean [es] ;
		for(int i = 0; i < es; i++) ret[i] = false;

		for (int i = 0; i < size; i++)
			members[i].addEventCareSet(ret, uncontrollable_events_only);

		return ret;
	}

    // --------------------------------------------------------------------------
	public int getM()
	{
	    if(!has_m)
		{
		    computeM();
		}

		return bdd_m;
	}

    private void computeM() {
		bdd_m = manager.getOne();

		for (int i = size - 1; i >= 0; --i)
			bdd_m = manager.andTo(bdd_m, members[i].getM());

		has_m = true;
	}
	// -------------------------------------------------------------------
	public int getTu()
	{
		if (!has_tu)
		{
			computeTu();
		}

		return bdd_tu;
	}

	private void computeTu()
	{
		int t = getT();

		// Options.out.print("t = "); manager.printSet(t);
		bdd_tu = manager.and(t, bdd_sigma_u);

		// Options.out.print("tmp = "); manager.printSet(tmp);
		// bdd_tu = manager.exists( tmp, manager.getEventCube());
		// Options.out.print("bd_tu = "); manager.printSet(bdd_tu);
		// manager.deref(tmp);
		has_tu = true;
	}

	// ---------------------------------------------------------------------
	/** to signal Supervisor etc if it should cleanup the Group when it terminates */
	public void setCleanup(boolean cu) { cleanup_flag  = cu; }

	/** to signal Supervisor etc if it should cleanup the Group when it terminates */
	public boolean getCleanup() { return cleanup_flag ; }


	// ---------------------------------------------------------------------


	public int getT()
	{
		if (has_t)
		{
			return bdd_t;
		}

		// else ..
		computeT();

		return bdd_t;
	}

	// pre-calculation routines:
	private void computeT()
	{
		if (size == 0)
		{

			// Note: getZero() wouldn't work here as it will be used in
			// AND operations later (t_al == plang.getT() & spec.getT())
			bdd_t = manager.getOne();

			manager.ref(bdd_t);
		}
		/*
		else if (size == 1)
		{
			bdd_t = members[0].getT();

			manager.ref(bdd_t);    // because we will deref it later here (see reset)
		}
		*/
		else
		{
			bdd_t = manager.getOne();

			manager.ref(bdd_t);

			Timer timer = new Timer(name);

			for (int i = size - 1; i >= 0; --i)
			{
				bdd_t = manager.andTo(bdd_t, members[i].getTpri());
			}

			timer.report("Prioritized T for group built");
		}

		has_t = true;
	}


	// --[ event analysis stuff ]-----------------------------------------------------

	/**
	 * subtracts the number of automata using each event from the original number
	 * found in the vector.
	 *
	 * This is used to find local events:
	 *   1. find the number of automata using each event, put them in 'count'
	 *   2. removeEventUsage() for some group G
	 *   3. if some position in count[] equals zero then that event is not used outside G.
	 *      3.b)  if that event is used at all, then it must be local to group G!
	 */
	public void removeEventUsage(int [] count) {
		for (int i = 0; i < size; i++)
			members[i].removeEventUsage(count);
	}

	/**
	 * Not used anywhere yet.
	 * its here just to complete removeEventUsage() above ...
	 *
	 * [no kidding]
	 */
	public void addEventUsage(int [] count) {
		for (int i = 0; i < size; i++)
			members[i].addEventUsage(count);
	}
	// --------------------------------------------------------------------------------

	public int forward_reachables()
	{
		int permute = manager.getPermuteSp2S();
		int ec = manager.getEventCube();
		int cube = manager.and(bdd_cube, manager.getEventCube());
		int t = getT();
		Timer timer = new Timer(name);
		int rp, r = bdd_i;

		manager.ref(r);    // since its used in orTo

		do
		{
			rp = r;

			int tmp1 = manager.relProd(t, r, cube);
			int tmp2 = manager.replace(tmp1, permute);

			manager.deref(tmp1);

			r = manager.orTo(r, tmp2);
		}
		while (rp != r);

		timer.report("Reachables states in group found");

		// cleanup:
		manager.deref(cube);

		return r;
	}

	public int removeDontCare(int bdd)
	{
		for (int i = size - 1; i >= 0; --i)
		{
			bdd = manager.andTo(bdd, members[i].getCareS());
		}

		return bdd;
	}

	// -------------------------------------- diagnostics ------------------------------
	public void dump()
	{
		Options.out.print("Group " + name + " = {");

		for (int i = 0; i < size; i++)
		{
			if (i != 0)
			{
				Options.out.print(", ");
			}

			Options.out.print(members[i].getName());
		}

		Options.out.println("};");
	}

	public void show_states(int bdd)
	{
		String[] states = new String[size];

		Options.out.println("S_" + name + " = {");
		show_states_rec(states, bdd, 0);
		Options.out.println("};");
	}

	private void show_states_rec(String[] saved, int bdd, int level)
	{
		if (level >= size)
		{
			Options.out.print(" <");

			for (int i = 0; i < size; i++)
			{
				if (saved[i] != null)
				{
					if (i != 0)
					{
						Options.out.print(", ");
					}

					Options.out.print(saved[i]);
				}
			}

			Options.out.println(">");

			return;
		}

		saved[level] = null;

		State[] states = members[level].getStates();
		int zero = manager.getZero();

		for (int i = 0; i < states.length; i++)
		{
			int tmp = manager.and(bdd, states[i].bdd_s);

			if (tmp != zero)
			{
				saved[level] = states[i].name;

				show_states_rec(saved, tmp, level + 1);
			}

			manager.deref(tmp);
		}
	}

	// ---------------------------------------------------------------------------

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (int i = 0; i < size; i++) {
			if(i != 0) sb.append(", ");
			sb.append( members[i].getName() );
		}
		sb.append("}");
		return sb.toString();
	}
}
