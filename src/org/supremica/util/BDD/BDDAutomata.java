package org.supremica.util.BDD;

// NEW CUDD: FIXED
import java.io.*;
import java.util.*;

public class BDDAutomata
	extends JBDD
{
    private static int ref_count = 0;

	private boolean has_events = false;
	private Automata original_automata;
	private int components;    // number of automata
	private BDDAutomaton[] automata;
	private int permute_s2sp, permute_sp2s;
	private int keep;    // the global keep

	// the global alphabet
	private Event[] original_events;
	private int events_size;
	private int bdd_total_initial, bdd_total_cube, bdd_total_cubep;

	// vector sizes
	private int size_states, size_events, size_all;

	// BDD sttufs for the alphabet
	private int[] events;

	// private int events_bits;
	private int bdd_events_cube, bdd_events_u, bdd_events_c;

	public BDDAutomata(Automata a)
	{
		super(a.getVariableCount(), Util.suggest_nodecount(a));
		ref_count++;

		// some funny thing with CUDD ...
		BDDAssert.internalCheck((not(getZero()) == getOne()) && (not(getOne()) == getZero()), "[INTERNAL] either  ~1 != 0  or  ~0 != 1");

		Timer timer = new Timer();

		original_automata = a;

		Vector v = a.getAutomata();

		components = v.size();
		automata = new BDDAutomaton[components];

		// first we create all automata  ...
		int i = 0;

		size_states = 0;

		Enumeration e = v.elements();

		while (e.hasMoreElements())
		{
			automata[i] = new BDDAutomaton(this, (Automaton) e.nextElement(), i);			
			size_states += automata[i].getNumStateBits();
			i++;

			// BDDAssert.debug(automata[i-1].getName() + " created");
		}

		check("Automata created");

		// and create the Sigma set
		createEvents();
		check("Events created");

		// ... then we do the second initialization which needs
		// all automata to be presented ( se createPair)
		keep = getOne();

		ref(keep);

		bdd_total_initial = getOne();

		ref(bdd_total_initial);

		bdd_total_cube = getOne();

		ref(bdd_total_cube);

		bdd_total_cubep = getOne();

		ref(bdd_total_cubep);

		for (i = components - 1; i >= 0; --i)
		{
			automata[i].init();

			keep = andTo(keep, automata[i].getKeep());
			bdd_total_initial = andTo(bdd_total_initial, automata[i].getI());
			bdd_total_cube = andTo(bdd_total_cube, automata[i].getCube());
			bdd_total_cubep = andTo(bdd_total_cubep, automata[i].getCubep());

			// check("Createdautomaton " + automata[i].getName() );
		}

		timer.report("BDD automata created");
		check("Automata initilized");

		// gc();
		// System.out.println("CheckPackage returned " + checkPackage());
	}

	private void createEvents()
	{
		BDDAssert.internalCheck(!has_events, "[BDDAutomata.createEvents] multiple calls" + " to createEvents!");

		EventManager em = original_automata.getAlphabeth();

		events_size = em.getSize();
		size_events = Util.log2ceil(events_size);
		original_events = em.getEventVector();

		// int total_s_plus_sp = getVarCount(); // |S| + |S´| , save it for later use!
		// ---------------------------------- get events
		events = new int[size_events];

		for (int i = 0; i < size_events; i++)
		{
			events[i] = createBDD();
		}

		bdd_events_u = getZero();

		ref(bdd_events_u);

		bdd_events_c = getZero();

		ref(bdd_events_c);

		bdd_events_cube = makeSet(events, size_events);

		for (int i = 0; i < events_size; i++)
		{
			original_events[i].code = i;    // quick and dirty event encoding :)

			int bdd_e = Util.getNumber(this, events, original_events[i].code);

			original_events[i].bdd = bdd_e;

			if (!original_events[i].c)
			{
				bdd_events_u = orTo(bdd_events_u, bdd_e);
			}
			else
			{
				bdd_events_c = orTo(bdd_events_c, bdd_e);
			}

			// BDDAssert.debug("event_i: " + original_events[i].name + "/" +original_events[i].code);
			// printSet(original_events[i].bdd);
		}

		has_events = true;

		// ---------------------------------------- create global S->S' and S'->S permutations
		int[] tmp1 = new int[size_states];
		int[] tmp2 = new int[size_states];

		for (int i = 0; i < size_states; i++)
		{
			tmp1[i] = getBDD(i * 2);    // S[i]
			tmp2[i] = getBDD(i * 2 + 1);    // S´[i]
		}

		permute_s2sp = createPair(tmp1, tmp2);
		permute_sp2s = createPair(tmp2, tmp1);
		size_all = 2 * size_states + size_events;
	}

    public BDDAutomaton [] getAutomataVector() {
	return automata;
    }
	public int getSize()
	{
		return components;
	}

	public int getEventCube()
	{
		return bdd_events_cube;
	}

	public int getEventsBits()
	{
		return size_events;
	}

	public int getStateCube()
	{
		return bdd_total_cube;
	}
	;

	public int getStatepCube()
	{
		return bdd_total_cubep;
	}

	public int getStateBits()
	{
		return size_states;
	}

	public int getSigmaU()
	{
		return bdd_events_u;
	}

	public int getSigmaC()
	{
		return bdd_events_c;
	}

	public int getPermuteS2Sp()
	{
		return permute_s2sp;
	}

	public int getPermuteSp2S()
	{
		return permute_sp2s;
	}

	public Event[] getEvents()
	{
		BDDAssert.internalCheck(has_events, "[BDDAutomata.getEvents] call createEvents firs!");

		return original_events;
	}

	public void cleanup()
	{
		for (int i = 0; i < components; i++)
		{
			automata[i].cleanup();
		}

		// printStats();
		kill();
		ref_count--;
	}

	public void dump(PrintStream ps)
	{
		for (int i = 0; i < components; i++)
		{
			automata[i].dump(ps);
		}

		ps.println("-------------------- The alphabet:");
		ps.println("BDD Variables: " + size_events + ", " + events_size + " events");
		ps.println("BDD Sigma_u: " + nodeCount(bdd_events_u) + " nodes, SAT-count = " + satCount(bdd_events_u, size_events));
	}

    // ------------------------------------------------------------------------------
    public static boolean BDDPackageIsBusy() {
	return ref_count > 0;
    }
	// ------------------------ some debugging functions ----------------------------------
	// --------------------------------------------------------- show_states (note: O(n!) complexity)
	public void show_states(int bdd)
	{
		show_states(bdd, null);
	}

	public void show_states(int bdd, boolean[] cares)
	{
		String[] states = new String[components];

		System.out.println("S = {");
		show_states_rec(states, cares, bdd, 0);
		System.out.println("};");
	}

	private void show_states_rec(String[] saved, boolean[] cares, int bdd, int level)
	{
		if (level >= components)
		{
			System.out.print(" < ");

			for (int i = 0; i < components; i++)
			{
				if (saved[i] != null)
				{
					System.out.print(saved[i] + "  ");
				}
			}

			System.out.println(">");

			return;
		}

		saved[level] = null;

		if ((cares != null) && (cares[level] == false))
		{
			show_states_rec(saved, cares, bdd, level + 1);
		}
		else
		{
			int zero = getZero();    // ok not refed
			State[] states = automata[level].getStates();

			for (int i = 0; i < states.length; i++)
			{
				int tmp = and(bdd, states[i].bdd_s);

				if (tmp != zero)
				{
					saved[level] = states[i].name;

					show_states_rec(saved, cares, tmp, level + 1);
				}

				recursiveDeref(tmp);
			}
		}
	}

	// ------------------------------------------------------------------------------
	public void printAutomatonVector()
	{
		System.out.print(" ");

		for (int i = 0; i < components; i++)
		{
			if (i != 0)
			{
				System.out.print(" x ");
			}

			System.out.print(automata[i].getName());
		}

		System.out.println("");
	}

	public void printStateVector(String[] states)
	{
		printStateVector(states, null);
	}

	public void printStateVector(String[] states, String what)
	{
		if (what != null)
		{
			System.out.print(what);
		}

		System.out.print(" <");

		for (int i = 0; i < states.length; i++)
		{
			if (i != 0)
			{
				System.out.print(", ");
			}

			System.out.print((states[i] == null)
							 ? "-"
							 : states[i]);
		}

		System.out.println(">");
	}

	// -----------------------------------------------------------------------------
	public int pickOneState(String[] names, int bdd)
	{
		if (bdd == getZero())
		{
			return bdd;    // nothing to choose
		}

		return pick_one_state_rec(names, bdd, 0);
	}

	private int pick_one_state_rec(String[] names, int bdd, int level)
	{
		if (level >= components)
		{
			ref(bdd);

			return bdd;
		}

		State[] states = automata[level].getStates();
		int zero = getZero();

		for (int i = 0; i < states.length; i++)
		{
			int tmp = and(bdd, states[i].bdd_s);

			if (tmp != zero)
			{
				int ret = pick_one_state_rec(names, tmp, level + 1);

				if (names != null)
				{
					names[level] = states[i].name;
				}

				recursiveDeref(tmp);

				return ret;
			}

			recursiveDeref(tmp);
		}

		return zero;
	}

	// ------------------------------------------------------------------------------
	public void count_states(String name, int bdd)
	{

		/*
		 * // the easy way
		 * int new_bdd = removeDontCareS(bdd);
		 * double states = satCount(new_bdd);
		 * if(states != -1)
		 *   states /= Math.pow(2, size_states + size_events);
		 *   recursiveDeref(new_bdd);
		 */

		// the hard/boring/slow way :(
		Counter c = new Counter();

		count_transitions_rec0(c, bdd, 0);

		long states = c.get();

		System.out.println(name + " " + states);
	}

	private void count_transitions_rec0(Counter c, int bdd, int level)
	{    // for S
		if (level >= components)
		{
			c.increase();

			return;
		}

		State[] states = automata[level].getStates();
		int zero = getZero();

		for (int i = 0; i < states.length; i++)
		{

			// dont know which one is faster in the total recursive run , probably relProd ?
			int tmp = and(bdd, states[i].bdd_s);

			// int tmp = relProd(bdd, states[i].bdd_s, automata[level].getCube());
			if (tmp != zero)
			{
				count_transitions_rec0(c, tmp, level + 1);
			}

			recursiveDeref(tmp);
		}
	}

	// -----------------------------------------------------------
	public void show_events(int bdd, String name)
	{
		System.out.print("Events_" + name + " = { ");

		int zero = getZero();

		for (int i = 0; i < events_size; i++)
		{
			int tmp = and(bdd, original_events[i].bdd);

			if (tmp != zero)
			{
				System.out.print(original_events[i].label + " ");
			}

			recursiveDeref(tmp);
		}

		System.out.println("};");
	}

	// ------------------------------------------------------------------
	public int pickOneEvent(String[] name, int bdd)
	{
		int zero = getZero();

		if (bdd == zero)
		{
			System.out.println("EVENT IS ZERO");

			name = null;

			return bdd;    // nothing to choose
		}

		for (int i = 0; i < events_size; i++)
		{
			int tmp = and(bdd, original_events[i].bdd);

			if (tmp != zero)
			{
				name[0] = original_events[i].label;

				return tmp;
			}

			recursiveDeref(tmp);
		}

		return zero;    /* UNREACHABLE ? */
	}

	// -------------------------------------------------------- show_transitions
	public void show_transitions(int bdd)
	{
		String[] names = new String[components * 2 + 1];    // the last one for events

		for (int i = 0; i < names.length; i++)
		{
			names[i] = null;
		}

		System.out.println("T = {");
		show_transitions_rec0(names, bdd, 0);
		System.out.println("};");
	}

	private void show_transitions_rec0(String[] names, int bdd, int level)
	{    // for S
		if (level >= components)
		{
			show_transitions_rec1(names, bdd, 0);

			return;
		}

		State[] states = automata[level].getStates();
		int zero = getZero();

		for (int i = 0; i < states.length; i++)
		{
			int tmp = and(bdd, states[i].bdd_s);

			if (tmp != zero)
			{
				names[level] = states[i].name;

				show_transitions_rec0(names, tmp, level + 1);
			}

			recursiveDeref(tmp);
		}
	}

	private void show_transitions_rec1(String[] names, int bdd, int level)
	{    // for S'
		if (level >= components)
		{
			show_transitions_rec2(names, bdd);

			return;
		}

		State[] states = automata[level].getStates();
		int zero = getZero();

		for (int i = 0; i < states.length; i++)
		{
			int tmp = and(bdd, states[i].bdd_sp);

			if (tmp != zero)
			{
				names[components + level] = states[i].name;

				show_transitions_rec1(names, tmp, level + 1);
			}

			recursiveDeref(tmp);
		}
	}

	private void show_transitions_rec2(String[] names, int bdd)
	{    // and for Sigma
		final int pos = components * 2;
		int zero = getZero();

		for (int i = 0; i < events_size; i++)
		{
			int tmp = and(bdd, original_events[i].bdd);

			if (tmp != zero)
			{
				names[pos] = original_events[i].label;

				show_transitions_print(names);
			}

			recursiveDeref(tmp);
		}
	}

	private void show_transitions_print(String[] list)
	{    // pretty printer for the transition list
		int i;

		System.out.print("  ( <");

		for (i = 0; i < components; i++)
		{
			if (i != 0)
			{
				System.out.print(",");
			}

			if (list[i] != null)
			{
				System.out.print(list[i]);
			}
		}

		System.out.print(">, ");

		String event = list[components * 2];

		if (event == null)
		{
			event = "??";
		}

		System.out.print(event + "  -> <");

		for (i = 0; i < components; i++)
		{
			if (i != 0)
			{
				System.out.print(",");
			}

			if (list[components + i] != null)
			{
				System.out.print(list[components + i]);
			}
		}

		System.out.println("> ), ");
	}

	// -----------------------------------------------------------------------------
	public void check(String name)
	{
		if (Options.sanity_check_on)
		{
		    // DEBUG: in case checkPackage crashes before it exists and prints 'name'
		    System.out.println("Checking : " + name);
		    BDDAssert.internalCheck(super.checkPackage(), name + ": checkPackage() failed");
		}
	}

	public void stats(PrintStream ps)
	{
		double states = original_automata.getTotalSize();

		/*
		 * int bits = 0;
		 * for(int i = 0; i < components; i++){
		 *   int s = automata[i].getNumStates();
		 *   if(s != 0) bits += automata[i].getNumStateBits();
		 *   else       BDDAssert.warning("Automata " + automata[i].getName() + " has no states!");
		 * }
		 */
		double theo_states = Math.pow(2, size_states);
		double efficiency = (100 * states) / theo_states;

		ps.print("" + size_states + " bits S, efficiency=" + efficiency + "%, ");
		original_automata.stats(ps);
	}

	// ----------------------------------------------------------------------
	public int removeSelfLoops(int bdd)
	{
		ref(bdd);

		int all_loop = getOne();

		ref(all_loop);

		for (int i = 0; i < components; i++)
		{
			all_loop = andTo(all_loop, automata[i].getKeep());
		}

		int dont_keep = not(all_loop);

		recursiveDeref(all_loop);

		bdd = andTo(bdd, dont_keep);

		recursiveDeref(dont_keep);

		return bdd;
	}

	public int removeDontCareS(int bdd)
	{
		ref(bdd);

		for (int i = 0; i < components; i++)
		{
			bdd = andTo(bdd, automata[i].getCareS());
		}

		return bdd;
	}

	public int removeDontCareSp(int bdd)
	{
		ref(bdd);

		for (int i = 0; i < components; i++)
		{
			bdd = andTo(bdd, automata[i].getCareSp());
		}

		return bdd;
	}

	// ------------------------------------------------ work in progress stuffs
	public void internal_test()
	{
		System.out.println(" ---------------- internal_test ---------------");

		Timer timer = new Timer();

		// gc(); // DEBUG
		Supervisor sup = new Supervisor(this, automata);

		timer.report("Supervisor intansiated");

		int r = sup.getReachables();    // gc();
		int u = sup.getReachableUncontrollables();    // gc();
		int d = sup.getDeadlocks();    // gc();
		int c = sup.getCoReachables();    // gc();
		int safe = sup.getSafeStates();    // gc();

		// DEBUG:
		// System.out.print("Deadlocks: ");     show_states(d);
		System.out.println("-------- All done, counting states now -----------");
		count_states("reachables", r);
		count_states("co-reachaboes", c);
		count_states("uncontrollable (reachable)", u);
		count_states("deadlocks", d);
		count_states("safe states", safe);

		/* show any deadlocks, but only the 20 first ones */
		if (d != getZero())
		{
			sup.trace_set("deadlock", d, 20);
		}

		sup.cleanup();

		/*
		 * // ------------------------------ get a prioritized composition
		 * Timer timer = new Timer();
		 * int t_all = getOne();
		 * // initial = getOne();
		 * // cube = getOne();
		 *
		 *
		 * for(int i = components-1; i >= 0; --i) {
		 * BDDAutomaton a = automata[i];
		 *
		 * // initial = andTo(initial, a.getI() );
		 * // cube    = andTo( cube,   a.getCube() );
		 * t_all = andTo(t_all, a.getTpri() );
		 *
		 *
		 * // System.out.print("t_all="); printSet(t_all);
		 * System.out.print((100 * i / components) + "% left ...         \r");
		 * }
		 *
		 * timer.report("Prioritized composition done");
		 * int cube = and(bdd_total_cube, events_cube); ref(cube);
		 *
		 * BDDAssert.debug("Prioritized composition");
		 * // System.out.println("T-all = "); printSet(t_all);
		 * // show_states(t_all, null);
		 * BDDAssert.debug("|prioritizied T-all| = " + nodeCount(t_all));
		 *
		 *
		 * timer.reset();
		 * // test reachability on this t_all:
		 * int r_all = simple_forward(bdd_total_initial, t_all, cube);
		 *
		 * // show_states(r_all, null);
		 * // show_transitions( and(t_all, not(keep)));
		 * // printSet( and(t_all, not(keep)));
		 * timer.report("Reachable states found");
		 *
		 *
		 * timer.reset();
		 * int r_all_partitioned = partitioned_forward();
		 * timer.report("Partitioned forward reachability");
		 *
		 * timer.reset();
		 * count_states("States in r_all", r_all);
		 * timer.report("States counted");
		 */
	}

	private int partitioned_forward()
	{
		int rp = 0, r = bdd_total_initial;
		int[] cubes = new int[components];
		int last = getOne();

		ref(last);

		for (int i = components - 1; i >= 0; --i)
		{
			cubes[i] = and(last, automata[i].getCube());
			last = cubes[i];
		}

		recursiveDeref(getOne());    // 'last' is changed to something else now;)

		int front = r;

		do
		{
			rp = r;

			ref(r);

			for (int i = components - 1; i >= 0; --i)
			{
				BDDAutomaton a = automata[i];
				int tmp2 = relProd(a.getTpri(), front, cubes[i] /* WAS a.getCube() */);

				recursiveDeref(front);

				front = tmp2;
			}

			int tmp2 = exists(front, bdd_events_cube);

			recursiveDeref(front);

			front = replace(tmp2, permute_sp2s);

			recursiveDeref(tmp2);

			r = orTo(r, front);

			recursiveDeref(r);    // what is this ????
		}
		while (rp != r);

		return r;
	}
}
