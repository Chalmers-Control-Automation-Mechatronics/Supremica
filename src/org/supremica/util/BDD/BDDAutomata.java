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


		SizeWatch.setManager(this);

		// some funny thing with CUDD ...
		BDDAssert.internalCheck((not(getZero()) == getOne()) && (not(getOne()) == getZero()),
					"[INTERNAL] either  ~1 != 0  or  ~0 != 1");

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
		// Options.out.println("CheckPackage returned " + checkPackage());
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

	// -------------------------------------------------------------------------------------

    public BDDAutomaton [] getAutomataVector()
    {
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

	public EventManager getEventManager()
	{
		return original_automata.getAlphabeth();
	}

	/** get a BDD for a subset of alphabet */
	public int getAlphabetSubsetAsBDD(boolean [] subset)
	{
		int bdd_subset = ref( getZero() );

		for(int i = 0; i < subset.length; i++)
		{
			if(subset[i])
			bdd_subset = orTo(bdd_subset ,	original_events[i].bdd);
		}
		return bdd_subset;

	}

	// -------------------------------------------------------------------------

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

		Options.out.println("S = {");
		show_states_rec(states, cares, bdd, 0);
		Options.out.println("};");
	}

	private void show_states_rec(String[] saved, boolean[] cares, int bdd, int level)
	{
		if (level >= components)
		{
			Options.out.print(" < ");

			for (int i = 0; i < components; i++)
			{
				if (saved[i] != null)
				{
					Options.out.print(saved[i] + "  ");
				}
			}

			Options.out.println(">");

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

				deref(tmp);
			}
		}
	}

	// ------------------------------------------------------------------------------
	public void printAutomatonVector()
	{
		Options.out.print(" ");

		for (int i = 0; i < components; i++)
		{
			if (i != 0)
			{
				Options.out.print(" x ");
			}

			Options.out.print(automata[i].getName());
		}

		Options.out.println("");
	}

	public void printStateVector(String[] states)
	{
		printStateVector(states, null);
	}

	public void printStateVector(String[] states, String what)
	{
		if (what != null)
		{
			Options.out.print(what);
		}

		Options.out.print(" <");

		for (int i = 0; i < states.length; i++)
		{
			if (i != 0)
			{
				Options.out.print(", ");
			}

			Options.out.print((states[i] == null)
							 ? "-"
							 : states[i]);
		}

		Options.out.println(">");
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

				deref(tmp);

				return ret;
			}

			deref(tmp);
		}

		return zero;
	}

	// ------------------------------------------------------------------------------
	public void count_states(String name, int bdd)
    {
	Options.out.println(name + " " + count_states(bdd));
    }

    public long count_states(int bdd)
	{

	    switch(Options.count_algo) {
	    case Options.COUNT_NONE:
		return 0;

	    case Options.COUNT_TREE:
		// the easy way
		int new_bdd = removeDontCareS(bdd);
		double states = satCount(new_bdd);
		if(states != -1)
		    states /= Math.pow(2, size_states + size_events);
		deref(new_bdd);
		return (long) states;
	    case Options.COUNT_EXACT:
		// the hard/boring/slow way :(
		Counter c = new Counter();

		count_transitions_rec0(c, bdd, 0);

		return c.get();
	    }
	    return 0; // just in case :)
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

			deref(tmp);
		}
	}

	// -----------------------------------------------------------
	public void show_events(int bdd, String name)
	{
		Options.out.print("Events_" + name + " = { ");

		int zero = getZero();

		for (int i = 0; i < events_size; i++)
		{
			int tmp = and(bdd, original_events[i].bdd);

			if (tmp != zero)
			{
				Options.out.print(original_events[i].label + " ");
			}

			deref(tmp);
		}

		Options.out.println("};");
	}

	// ------------------------------------------------------------------
	public int pickOneEvent(String[] name, int bdd)
	{
		int zero = getZero();

		if (bdd == zero)
		{
			Options.out.println("EVENT IS ZERO");

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

			deref(tmp);
		}

		return zero;    /* UNREACHABLE ? */
	}


	// --------------------------------------------------------
	// This is used by the state extraction routins, so we dont
	// push to much stuff on the stack during recusrive calls
	private class StateListRecusionData {
		int current, max;
		String [] names;
		BDDAutomaton [] automata;
		IncompleteStateList result;
		int bdd;
	};

	// -------------------------------------------------------- build incomplete state list for this event
	IncompleteStateList getIncompleteStateList(int bdd, Event event)
	{


		BDDAutomaton [] involved = new BDDAutomaton[components];
		int count = 0;
		for(int i = 0; i < components; i++)
		{
			/* NOT working, we must have the aother automata too!
			if(automata[i].eventUsed(event))
			{
				involved[count++] = automata[i];
			}
			*/
			involved[count++] = automata[i];
		}


		IncompleteStateList ret = new IncompleteStateList(involved, count);

		int tmpbdd = relProd( bdd, event.bdd, bdd_events_cube);
		if(tmpbdd != getZero() && count > 0)
		{
			StateListRecusionData slrd = new StateListRecusionData();
			slrd.names = new String[count];
			slrd.automata = involved;
			slrd.bdd = tmpbdd;
			slrd.result = ret;
			slrd.current = 0;
			slrd.max = count;
			extract_states_rec(slrd);

		}
		deref(tmpbdd);

		return ret;
	}

	/**
	 * Arash says:
	 *
	 * THIS ALGORITHM IS HORREIBLY INEFFICIENT !!!
	 * its BAD, BAD, BAD. If it could get any worse, it would use hungerian notation!
	 *
     */
	private void extract_states_rec(StateListRecusionData slrd)
		// int bdd, int count, String [] names, IncompleteStateList result)
	{
		if(slrd.current >= slrd.max)
		{
			slrd.result.insert(slrd.names);
		} else {
			State[] states = slrd.automata[slrd.current].getStates();
			for (int i = 0; i < states.length; i++)
			{
				int tmp = and(slrd.bdd, states[i].bdd_s);

				if (tmp != zero)
				{
					slrd.names[slrd.current] = states[i].name;
					int old = slrd.bdd; slrd.bdd = tmp; slrd.current++;
					extract_states_rec(slrd);
					slrd.bdd = old; slrd.current--;
				}

				deref(tmp);
			}
		}
	}

	// -------------------------------------------------------- show_transitions
	public void show_transitions(int bdd)
	{
		String[] names = new String[components * 2 + 1];    // the last one for events

		for (int i = 0; i < names.length; i++)
		{
			names[i] = null;
		}

		Options.out.println("T = {");
		show_transitions_rec0(names, bdd, 0);
		Options.out.println("};");
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

			deref(tmp);
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

			deref(tmp);
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

			deref(tmp);
		}
	}

	private void show_transitions_print(String[] list)
	{    // pretty printer for the transition list
		int i;

		Options.out.print("  ( <");

		for (i = 0; i < components; i++)
		{
			if (i != 0)
			{
				Options.out.print(",");
			}

			if (list[i] != null)
			{
				Options.out.print(list[i]);
			}
		}

		Options.out.print(">, ");

		String event = list[components * 2];

		if (event == null)
		{
			event = "??";
		}

		Options.out.print(event + "  -> <");

		for (i = 0; i < components; i++)
		{
			if (i != 0)
			{
				Options.out.print(",");
			}

			if (list[components + i] != null)
			{
				Options.out.print(list[components + i]);
			}
		}

		Options.out.println("> ), ");
	}

	// -----------------------------------------------------------------------------
	public void check(String name)
	{
		if (Options.sanity_check_on)
		{
		    // DEBUG: in case checkPackage crashes before it exists and prints 'name'
		    Options.out.println("Checking : " + name);
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

		 // Note to myself: who the f**k is Theo??? can t remember why i wrote this...
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

		deref(all_loop);

		bdd = andTo(bdd, dont_keep);

		deref(dont_keep);

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
		Options.out.println(" ---------------- internal_test ---------------");

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
		// Options.out.print("Deadlocks: ");     show_states(d);
		Options.out.println("-------- All done, counting states now -----------");
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
		 * // Options.out.print("t_all="); printSet(t_all);
		 * Options.out.print((100 * i / components) + "% left ...         \r");
		 * }
		 *
		 * timer.report("Prioritized composition done");
		 * int cube = and(bdd_total_cube, events_cube); ref(cube);
		 *
		 * BDDAssert.debug("Prioritized composition");
		 * // Options.out.println("T-all = "); printSet(t_all);
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

		deref(getOne());    // 'last' is changed to something else now;)

		int front = r;

		do
		{
			rp = r;

			ref(r);

			for (int i = components - 1; i >= 0; --i)
			{
				BDDAutomaton a = automata[i];
				int tmp2 = relProd(a.getTpri(), front, cubes[i] /* WAS a.getCube() */);

				deref(front);

				front = tmp2;
			}

			int tmp2 = exists(front, bdd_events_cube);

			deref(front);

			front = replace(tmp2, permute_sp2s);

			deref(tmp2);

			r = orTo(r, front);

			deref(r);    // what is this ????
		}
		while (rp != r);

		return r;
	}
}
