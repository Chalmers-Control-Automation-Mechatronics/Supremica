package org.supremica.util.BDD;

import org.supremica.util.BDD.encoding.*;

// NEW CUDD: FIXED
import java.io.*;

public class BDDAutomata
	extends JBDD
{
	private static int ref_count = 0;
	private boolean has_events = false;
	private Automata original_automata;
	private int nrOfAutomata;    // number of automata
	private BDDAutomaton[] bddAutomata;
	private int permute_s2sp, permute_sp2s, permute_spp2sp, permute_sp2spp;
	private int keep;    // the global keep

	// the global alphabet
	private Event[] original_events;
	private int events_size;
	private int bdd_total_initial;
	private int bdd_dont_care; // nonexisting states
	private int bdd_total_cube, bdd_total_cubep, bdd_total_cubepp;
	
	// vector sizes
	private int size_states, size_events;

	// BDD sttufs for the alphabet
	private int[] events;

	// private int events_bits;
	private int bdd_events_cube, bdd_events, bdd_events_u, bdd_events_c;

	// we dont like to hang the system, so we decrease our priority:
	private int saved_priority;

	/** this is used in recursive show_transition calls, dont touch */
	private boolean show_transition_is_half;

	public BDDAutomata(Automata a)
	{
		super(a.getVariableCount(), Util.suggest_nodecount(a));
//            	super(a.getVariableCount(), 10);
                
		ref_count++;

		// if(Options.developer_mode) {
		Thread t = Thread.currentThread();

		saved_priority = t.getPriority();

		t.setPriority(Thread.MIN_PRIORITY);

		// }
		SizeWatch.setManager(this);
		NodeCountStatistics.getInstance().setManager(this);
		NodeCountStatistics.getInstance().reset();
		
		// some funny thing with CUDD ...
		int check0 = not(getZero());
		int check1 = not(getOne());

		BDDAssert.internalCheck((check0 == getOne()) && (check1 == getZero()), "[INTERNAL] either  ~1 != 0  or  ~0 != 1");
		deref(check0);
		deref(check1);

		Timer timer = new Timer();

		this.original_automata = a;

		this.nrOfAutomata = a.getAutomata().size();
		this.bddAutomata = new BDDAutomaton[nrOfAutomata];

		// Dynamic reordering stuff:
		reorder_setMethod(Options.reorder_algo);

		if (Options.reorder_dynamic)
		{
			reorder_enableDyanamic(true);
		}

		// lets change the state encoding now:
		Encoding enc = EncodingFactory.getEncoder();
		size_states = 0;
		for (Automaton automaton : a.getAutomata())
		{
			enc.encode(automaton);
			size_states += automaton.nrOfBitsNeededForStateEncoding();
		}



		// first, we create all the variables
		int[] currentStateVariables = new int[size_states];	// S
		int[] nextStateVariables = new int[size_states];	// S'
		int[] nextNextStateVariables = new int[size_states];	// S'' (meaning nextNext??)

		if(Options.interleaved_variables)
		{
			for (int i = 0; i < size_states; i++)
			{
				currentStateVariables[i] = createBDD();
				nextStateVariables[i] = createBDD();
				nextNextStateVariables[i] = createBDD();
			}
		}
		else
		{
			for (int i = 0; i < size_states; i++)	currentStateVariables[i] = createBDD();
			for (int i = 0; i < size_states; i++)	nextStateVariables[i] = createBDD();
			for (int i = 0; i < size_states; i++)	nextNextStateVariables[i] = createBDD();
		}

		// and create global S->S' and S'->S permutations
		permute_s2sp = createPair(currentStateVariables, nextStateVariables);
		permute_sp2s = createPair(nextStateVariables, currentStateVariables);
		permute_spp2sp = createPair(nextNextStateVariables, nextStateVariables);
		permute_sp2spp = createPair(nextStateVariables, nextNextStateVariables);

		// then we create all automata...
		int i = 0;

		size_states = 0;

		for (Automaton explicitAutomaton : a.getAutomata())
		{
			bddAutomata[i] = new BDDAutomaton(this, explicitAutomaton, i, currentStateVariables, nextStateVariables, nextNextStateVariables, size_states);
			size_states += bddAutomata[i].getNumStateBits();
			i++;

			// BDDAssert.debug(automata[i-1].getName() + " created");
		}

		check("Automata created");

		// and create the Sigma set
		createEvents();
		check("Events created");

		// ... then we do the second initialization which needs
		// all automata to be presented ( se createPair)
		keep = ref ( getOne() );
		bdd_total_initial = ref ( getOne() );
		bdd_total_cube = ref ( getOne() );
		bdd_total_cubep = ref ( getOne() );
		bdd_total_cubepp = ref ( getOne() );
		bdd_dont_care = ref( getZero() );

		for (i = nrOfAutomata - 1; i >= 0; --i)
		{
			bddAutomata[i].init();
			bdd_dont_care = orTo(bdd_dont_care, bddAutomata[i].getDontCareS() );

			keep = andTo(keep, bddAutomata[i].getKeep());
			bdd_total_initial = andTo(bdd_total_initial, bddAutomata[i].getI());
			bdd_total_cube = andTo(bdd_total_cube, bddAutomata[i].getCube());
			bdd_total_cubep = andTo(bdd_total_cubep, bddAutomata[i].getCubep());
			bdd_total_cubepp = andTo(bdd_total_cubepp, bddAutomata[i].getCubepp());

			// check("Createdautomaton " + automata[i].getName() );
			if (Options.reorderEnabled() && Options.reorder_with_groups)
			{
				reorder_createVariableGroup(bddAutomata[i].getTopBDD(), bddAutomata[i].getBottomBDD(), Options.reorder_within_group);
			}
		}

		// see if we must reorder after build:
		if (Options.reorderEnabled() && Options.reorder_after_build)
		{
			reorder_now();
		}

		timer.report("BDD automata created");
		check("Automata initilized");

		// gc();
		// Options.out.println("CheckPackage returned " + checkPackage());
		if (Options.show_encoding)
		{
			for (i = 0; i < nrOfAutomata; i++)
			{
				bddAutomata[i].showEncoding(this);
			}

			original_automata.getAlphabeth().showEncoding(this);
		}
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

		// if we have dynamic reordering:
		if (Options.reorderEnabled() && Options.reorder_with_groups)
		{
			int index1 = internal_index(events[0]);
			int index2 = internal_index(events[size_events - 1]);

			reorder_createVariableGroup(index1, index2, Options.reorder_within_group);
		}

		bdd_events_u = getZero();

		ref(bdd_events_u);

		bdd_events_c = getZero();

		ref(bdd_events_c);

		bdd_events_cube = makeSet(events, size_events);

		for (int i = 0; i < events_size; i++)
		{
			original_events[i].code = i;    // quick and dirty event encoding :)

			int bdd_e = Util.createBddForNumber(this, events, original_events[i].code);

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

		bdd_events = or(bdd_events_c, bdd_events_u);
		has_events = true;
	}

	// -------------------------------------------------------------------------------------
	/**
	 * compute and return the set of globally forbidden (but not always reachable!) states.
	 *
	 * the returned bdd is ref-counted and must be deref-counted by the user
	 */
	public int computeF()
	{
		int x = ref( getZero() );
		for(int i = 0; i < bddAutomata.length; i++)
		{
			x = orTo(x, bddAutomata[i].getF() );
		}
		return x;
	}
	// -------------------------------------------------------------------------------------
	public BDDAutomaton[] getAutomataVector()
	{
		return bddAutomata;
	}

	public int getSize()
	{
		return nrOfAutomata;
	}

	public long getReorderingTime()
	{
		return original_automata.getReorderingTime();
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

	public int getDontCareS()
	{
		return bdd_dont_care;
	}

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

	public int getSigma()
	{
		return bdd_events;
	}

	public int getSigmaC()
	{
		return bdd_events_c;
	}

	/** BDD for (forall i in state-variables) (v_i <--> v'_i) , i.e. (S <-> S') */
	public int getKeep()
	{
		return keep;
	}

	public int getPermuteS2Sp()
	{
		return permute_s2sp;
	}

	public int getPermuteSp2S()
	{
		return permute_sp2s;
	}

	public int getPermuteSpp2Sp()
	{
		return permute_spp2sp;
	}

	public int getPermuteSp2Spp()
	{
		return permute_sp2spp;
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
	public int getAlphabetSubsetAsBDD(boolean[] subset)
	{
		int bdd_subset = ref(getZero());

		for (int i = 0; i < subset.length; i++)
		{
			if (subset[i])
			{
				bdd_subset = orTo(bdd_subset, original_events[i].bdd);
			}
		}

		return bdd_subset;
	}

	// -------------------------------------------------------------------------
	public void cleanup()
	{
		check("cleanup");

		for (int i = 0; i < nrOfAutomata; i++)
		{
			bddAutomata[i].cleanup();
		}

		if (has_events)
		{
			deref(bdd_events_u);
			deref(bdd_events_c);
			deref(bdd_events);
		}

		deletePair(permute_s2sp);
		deletePair(permute_sp2s);
		deletePair(permute_spp2sp);
		deletePair(permute_sp2spp);

		// printStats();
		kill();

		ref_count--;

		// restore our priority
		// if(Options.developer_mode) {
		Thread t = Thread.currentThread();

		t.setPriority(saved_priority);

		// }
	}

	public void dump(PrintStream ps)
	{
		for (int i = 0; i < nrOfAutomata; i++)
		{
			bddAutomata[i].dump(ps);
		}

		ps.println("-------------------- The alphabet:");
		ps.println("BDD Variables: " + size_events + ", " + events_size + " events");
		ps.println("BDD Sigma_u: " + nodeCount(bdd_events_u) + " nodes, SAT-count = " + satCount(bdd_events_u, size_events));
	}

	// ------------------------------------------------------------------------------
	public static boolean BDDPackageIsBusy()
	{
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
		String[] states = new String[nrOfAutomata];

		Options.out.println("S = {");
		show_states_rec(states, cares, bdd, 0);
		Options.out.println("};");
	}

	private void show_states_rec(String[] saved, boolean[] cares, int bdd, int level)
	{
		// no need to continue if there is nothing there anyway...
		if (bdd == getZero())
		{
			return;
		}

		// termination condition
		if (level >= nrOfAutomata)
		{
			Options.out.print(" < ");

			for (int i = 0; i < nrOfAutomata; i++)
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
			if (dont_care(bdd, bddAutomata[level].getCareS(), bddAutomata[level].getCube()))
			{
				saved[level] = "-";

				show_states_rec(saved, cares, bdd, level + 1);

				return;
			}

			int zero = getZero();    // ok not refed
			State[] states = bddAutomata[level].getStates();

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

		for (int i = 0; i < nrOfAutomata; i++)
		{
			if (i != 0)
			{
				Options.out.print(" x ");
			}

			Options.out.print(bddAutomata[i].getName());
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
		if (level >= nrOfAutomata)
		{
			ref(bdd);

			return bdd;
		}

		State[] states = bddAutomata[level].getStates();
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
		String accuracy = (Options.count_algo == Options.COUNT_EXACT) ? "(exactly)" : "(estimated)";
		Options.out.println(name + " " + count_states(bdd) + " " + accuracy);
	}

	public long count_states(int bdd)
	{
		switch (Options.count_algo)
		{			
		case Options.COUNT_NONE :
			return 0;
			
		case Options.COUNT_TREE :
			
			// the easy way
			int new_bdd = removeDontCareS(bdd);
			double states = satCount(new_bdd);
			
			// System.out.println("states= " + states + ", div = 2^" + (2 * size_states + size_events));
			if (states != -1)
			{
				states /= Math.pow(2, 2 * size_states + size_events);
			}

			deref(new_bdd);

			return (long) states;

		case Options.COUNT_EXACT :
			// the hard/boring/slow way :(
			Counter c = new Counter();
			count_states_exact(c, bdd, 0);
			return c.get();
		}
		
		return 0;    // just in case :)
	}

	/* Counts states by recursively enumerating all state combinations and prune the search tree
	 * whenever possible. E.g. consider two automata. If state <p2,?> is not in set,
	 * then we no not need to check global states <p2,q1>, <p2,q2> etc.
	 * Searches through the hole Cartesian state space in worst case
	 */
	private void count_states_exact(Counter nrOfStatesFound, int inputStatesAsBdd, int automatonIndex)
	{    // for S
		if (automatonIndex >= nrOfAutomata)
		{	// A state is found, therefore increase counter
			nrOfStatesFound.increase();
			return;
		}

		State[] states = bddAutomata[automatonIndex].getStates();
		int zero = getZero();

		for (int i = 0; i < states.length; i++)
		{
			// dont know which one is faster in the total recursive run , probably relProd ?
		//	final int inputStatesThatContainThisState = and(inputStatesAsBdd, states[i].bdd_s);
			final int inputStatesThatContainThisState = relProd(inputStatesAsBdd, states[i].bdd_s, bddAutomata[automatonIndex].getCube());
	//		final int inputStatesThatContainThisState = relProd(inputStatesAsBdd, states[i].bdd_s, getStateCube());
			// if state is in set then try next automaton, otherwise prune search tree
			if (inputStatesThatContainThisState != zero)
			{
				count_states_exact(nrOfStatesFound, inputStatesThatContainThisState, automatonIndex + 1);
			}

			deref(inputStatesThatContainThisState);
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
	private class StateListRecusionData
	{
		int current, max;
		String[] names;
		BDDAutomaton[] automata;
		IncompleteStateList result;
		int bdd;
	}
	;

	// -------------------------------------------------------- build incomplete state list for this event
	IncompleteStateList getIncompleteStateList(int bdd, Event event)
	{
		BDDAutomaton[] involved = new BDDAutomaton[nrOfAutomata];
		int count = 0;

		for (int i = 0; i < nrOfAutomata; i++)
		{

			/* NOT working, we must have the aother automata too!
			if(automata[i].eventUsed(event))
			{
					involved[count++] = automata[i];
			}
			*/
			involved[count++] = bddAutomata[i];
		}

		IncompleteStateList ret = new IncompleteStateList(involved, count);
		int tmpbdd = relProd(bdd, event.bdd, bdd_events_cube);

		if ((tmpbdd != getZero()) && (count > 0))
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
		if (slrd.current >= slrd.max)
		{
			slrd.result.insert(slrd.names);
		}
		else
		{
			State[] states = slrd.automata[slrd.current].getStates();

			for (int i = 0; i < states.length; i++)
			{
				int tmp = and(slrd.bdd, states[i].bdd_s);

				if (tmp != zero)
				{
					slrd.names[slrd.current] = states[i].name;

					int old = slrd.bdd;

					slrd.bdd = tmp;

					slrd.current++;

					extract_states_rec(slrd);

					slrd.bdd = old;

					slrd.current--;
				}

				deref(tmp);
			}
		}
	}

	// -------------------------------------------------------- show_transitions

	/**
	 * for a transition T: Q x E --> Q, the half-transition is T': subset Q x E
	 *
	 */
	public void show_half_transitions(int bdd)
	{
		show_transition_is_half = true;

		show_transitions_internal(bdd);
	}

	/**
	 * print the notation of this transition in the global state space
	 * this function can be very slow for medium and large functions
	 */
	public void show_transitions(int bdd)
	{
		show_transition_is_half = false;

		show_transitions_internal(bdd);
	}

	private void show_transitions_internal(int bdd)
	{
		String[] names = new String[nrOfAutomata * 2 + 1];    // the last one for events

		for (int i = 0; i < names.length; i++)
		{
			names[i] = null;
		}

		Options.out.println(show_transition_is_half
							? "T-half = {"
							: "T = {");
		show_transitions_rec0(names, bdd, 0);
		Options.out.println("};");
	}

	/** show the first part (State from) of a transition */
	private void show_transitions_rec0(String[] names, int bdd, int level)
	{
		if (bdd == getZero())
		{
			return;
		}

		// for S
		if (level >= nrOfAutomata)
		{
			if (show_transition_is_half)
			{
				show_transitions_rec2(names, bdd);
			}
			else
			{
				show_transitions_rec1(names, bdd, 0);
			}

			return;
		}

		// see if it is dont care!
		if (dont_care(bdd, bddAutomata[level].getCareS(), bddAutomata[level].getCube()))
		{
			names[level] = "-";

			show_transitions_rec0(names, bdd, level + 1);

			return;
		}

		// show exactly what it was:
		State[] states = bddAutomata[level].getStates();
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

	/** show the second part (State to) of a transition */
	private void show_transitions_rec1(String[] names, int bdd, int level)
	{
		if (bdd == getZero())
		{
			return;
		}

		// for S'
		if (level >= nrOfAutomata)
		{
			show_transitions_rec2(names, bdd);

			return;
		}

		// see if it doesnt matter
		if (dont_care(bdd, bddAutomata[level].getCareSp(), bddAutomata[level].getCubep()))
		{
			names[nrOfAutomata + level] = "-";

			show_transitions_rec1(names, bdd, level + 1);

			return;
		}

		State[] states = bddAutomata[level].getStates();
		int zero = getZero();

		for (int i = 0; i < states.length; i++)
		{
			int tmp = and(bdd, states[i].bdd_sp);

			if (tmp != zero)
			{
				names[nrOfAutomata + level] = states[i].name;

				show_transitions_rec1(names, tmp, level + 1);
			}

			deref(tmp);
		}
	}

	/** show the event part of a transition */
	private void show_transitions_rec2(String[] names, int bdd)
	{    // and for Sigma
		final int pos = nrOfAutomata * 2;
		int zero = getZero();

		if (bdd == zero)
		{
			return;
		}

		// see if it is a dont care!
		if (dont_care(bdd, bdd_events, bdd_events_cube))
		{
			names[pos] = "-";

			show_transitions_print(names);

			return;
		}

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

		Options.out.print("  [ (");

		for (i = 0; i < nrOfAutomata; i++)
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

		Options.out.print("),  ");

		String event = list[nrOfAutomata * 2];

		if (event == null)
		{
			event = "??";
		}

		if (show_transition_is_half)
		{
			Options.out.println(event + "  ]");

			return;
		}

		Options.out.print(event + "       ==>>      (");

		for (i = 0; i < nrOfAutomata; i++)
		{
			if (i != 0)
			{
				Options.out.print(",");
			}

			if (list[nrOfAutomata + i] != null)
			{
				Options.out.print(list[nrOfAutomata + i]);
			}
		}

		Options.out.println(") ], ");
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

		for (int i = 0; i < nrOfAutomata; i++)
		{
			all_loop = andTo(all_loop, bddAutomata[i].getKeep());
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

		for (int i = 0; i < nrOfAutomata; i++)
		{
			bdd = andTo(bdd, bddAutomata[i].getCareS());
		}

		return bdd;
	}

	public int removeDontCareSp(int bdd)
	{
		ref(bdd);

		for (int i = 0; i < nrOfAutomata; i++)
		{
			bdd = andTo(bdd, bddAutomata[i].getCareSp());
		}

		return bdd;
	}

	// ----------------------------------------------------------------

	/**
	 * see if the set/function/whatever 'bdd' includes all elements of 'values',
	 * that is, this is a dont-care situation (= any element of 'values' would do)
	 * the domain of 'values' must be given with its 'cube'
	 *
	 * NOTE: THIS FUNCTION IS NOT VERY EFFICIENT!!!
	 */
	private boolean dont_care(int bdd, int values, int cube)
	{
		if (bdd == getOne())
		{
			return true;
		}

		int tmp = relProd(bdd, values, cube);

		if (tmp == bdd)
		{
			deref(tmp);

			return true;
		}

		deref(tmp);

		return false;
	}
}
