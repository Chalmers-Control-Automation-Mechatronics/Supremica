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
		add(automata[i]);
    }

	public Group(BDDAutomata manager, int max_capacity, String name)
	{
		this.manager = manager;
		this.name = name;
		capacity = max_capacity;
		size = 0;
		members = new BDDAutomaton[capacity];

		bdd_i = manager.getOne();
		manager.ref(bdd_i);

		bdd_m = manager.getOne();
		manager.ref(bdd_m);

		bdd_cube = manager.getOne();
		manager.ref(bdd_cube);

		bdd_cubep = manager.getOne();
		manager.ref(bdd_cubep);

		bdd_sigma_u = manager.getZero();
		manager.ref(bdd_sigma_u);

		bdd_sigma = manager.getZero();
		manager.ref(bdd_sigma);

		init();

	}


    private void init() {	
    	// no pre-calculations are valid
	has_t = false;
	has_tu = false;
	has_m = false;
    }
	// Note: strange things will happen if you try to use this object _after_ calling cleanup :)
	public void cleanup()
	{
		reset();

		manager.deref(bdd_i);
		manager.deref(bdd_cube);
		manager.deref(bdd_cubep);
		manager.deref(bdd_sigma_u);
		manager.deref(bdd_sigma);
	}

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

		reset();
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

		// System.out.print("t = "); manager.printSet(t);
		bdd_tu = manager.and(t, bdd_sigma_u);

		// System.out.print("tmp = "); manager.printSet(tmp);
		// bdd_tu = manager.exists( tmp, manager.getEventCube());
		// System.out.print("bd_tu = "); manager.printSet(bdd_tu);
		// manager.deref(tmp);
		has_tu = true;
	}

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
		System.out.print("Group " + name + " = {");

		for (int i = 0; i < size; i++)
		{
			if (i != 0)
			{
				System.out.print(", ");
			}

			System.out.print(members[i].getName());
		}

		System.out.println("};");
	}

	public void show_states(int bdd)
	{
		String[] states = new String[size];

		System.out.println("S_" + name + " = {");
		show_states_rec(states, bdd, 0);
		System.out.println("};");
	}

	private void show_states_rec(String[] saved, int bdd, int level)
	{
		if (level >= size)
		{
			System.out.print(" <");

			for (int i = 0; i < size; i++)
			{
				if (saved[i] != null)
				{
					if (i != 0)
					{
						System.out.print(", ");
					}

					System.out.print(saved[i]);
				}
			}

			System.out.println(">");

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
}
