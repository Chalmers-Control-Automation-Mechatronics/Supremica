package org.supremica.util.BDD;

// NEW CUDD: FIXED
import java.io.*;
import java.util.*;

public class BDDAutomaton
	implements WeightedObject
{
	private BDDAutomata manager;
	private Automaton automaton;
    private DependencySet dependency = null;
    /* package */ int index;
	private int num_bits, num_states, num_arcs, membership; /** membership is used to modify plant/spec time */
	private State[] states;
	private Arc[] arcs;
	private Event[] events;

	// BDDs
	private int[] var_s;
	private int[] var_sp;
	private int[] var_spp;
	private int cube_s, cube_sp, cube_spp;
	private int bdd_i, bdd_t, bdd_tu, bdd_t_top, bdd_t_pri;
	private int bdd_m, bdd_f;
	private int bdd_events_p, bdd_events, bdd_events_u;
	private int bdd_keep;
	private int bdd_care_s, bdd_dontcare_s;
	private int bdd_care_sp, bdd_dontcare_sp;
	private int index_first_bdd, index_last_bdd;

	public BDDAutomaton(BDDAutomata manager, Automaton a, int index)
	{
		this.manager   = manager;
		this.automaton = a;
		this.index     = index;
		states = automaton.getStates().getStateVector();
		arcs = automaton.getArcs().getArcVector();
		num_arcs = a.getArcsSize();
		num_states = a.getStatesSize();
		num_bits = Util.log2ceil(num_states);
		var_s = new int[num_bits];
		var_sp = new int[num_bits];
		var_spp = new int[num_bits];
		bdd_keep = manager.getOne();

		manager.ref(bdd_keep);

		for (int i = 0; i < num_bits; i++)
		{
			var_s[i] = manager.createBDD();
			var_sp[i] = manager.createBDD();
			var_spp[i] = manager.createBDD();

			int equal = manager.biimp(var_s[i], var_sp[i]);

			bdd_keep = manager.andTo(bdd_keep, equal);
		}

		index_first_bdd = manager.internal_index( var_s[0] );
		index_last_bdd  = manager.internal_index( var_spp[ num_bits - 1] );
	}

	/**
	 * initialization, phase 2:
	 * now, we also have the global alphabet since all automata has been read */
	public void init()
	{

		events = manager.getEvents();
		cube_s = manager.makeSet(var_s, num_bits);
		cube_sp = manager.makeSet(var_sp, num_bits);
		cube_spp = manager.makeSet(var_spp, num_bits);

		// precalculate STATE -> BDD map
		bdd_m = manager.getZero();
		manager.ref(bdd_m);

		bdd_f = manager.getZero();
		manager.ref(bdd_f);

		bdd_care_s = manager.getZero();
		manager.ref(bdd_care_s);

		bdd_care_sp = manager.getZero();
		manager.ref(bdd_care_sp);

		// interleaved simple state encoding.
		for (int i = 0; i < num_states; i++)
		{
			int code = states[i].code;
			int bdd_s = Util.getNumber(manager, var_s, code);
			int bdd_sp = Util.getNumber(manager, var_sp, code);

			states[i].bdd_s = bdd_s;
			states[i].bdd_sp = bdd_sp;
		}

		// Now, pre-build some important BDDs
		for (int i = 0; i < num_states; i++)
		{
			if (states[i].m)
			{
				bdd_m = manager.orTo(bdd_m, states[i].bdd_s);
			}

			if (states[i].f)
			{
				bdd_f = manager.orTo(bdd_f, states[i].bdd_sp);
			}

			/* to count states correctly, we use only one encoding state -> the care set != entire S */
			// if(!Options.fill_statevars) {
				bdd_care_s = manager.orTo(bdd_care_s, states[i].bdd_s);
				bdd_care_sp = manager.orTo(bdd_care_sp, states[i].bdd_sp);
			//}
		}


		/* use the entire encoding space by having duplicate encodings per state*/
		if(Options.fill_statevars)
		{
			int capacity = 1 << num_bits;
			int unused = capacity - num_states;

			/* find out which encodings are free */

			boolean [] code_used= new boolean[capacity];

			for(int i = 0; i < capacity; i++)
				code_used[i] = false;

			for (int i = 0; i < num_states; i++)
				code_used[states[i].code] = true;


			/* append the unused encoding to some states, untill every code is used */
			for(int i = 0; i < unused; i++)
			{

				int state = i; /* the state to which we add an encoding */

				/* find the next unused code */
				int j = 0;
				for(; code_used[j]; j++) ;
				int code = j;
				code_used[j] = true;

				/* get the BDD for it */
				int bdd_more_s  = Util.getNumber(manager, var_s, code);
				int bdd_more_sp = Util.getNumber(manager, var_sp, code);

				/* append it to some state */
				states[state].bdd_s  = manager.orTo(states[state].bdd_s , bdd_more_s );
				states[state].bdd_sp = manager.orTo(states[state].bdd_sp, bdd_more_sp);
			}
		}


		// get dont-care states, i.e. states allocated but not used in this state-vector
		bdd_dontcare_s = manager.not(bdd_care_s);
		bdd_dontcare_sp = manager.not(bdd_care_sp);

		check("before events");

		// copy BDD encoding and get the prioritized events
		bdd_events = manager.getZero();

		manager.ref(bdd_events);

		bdd_events_p = manager.getZero();

		manager.ref(bdd_events_p);

		bdd_events_u = manager.getZero();

		manager.ref(bdd_events_u);

		Event[] es = automaton.getEvents().getEventVector();

		for (int i = 0; i < es.length; i++)
		{
			if (es[i] != null)
			{
				es[i].bdd = events[i].bdd;    // first, copy the BDD encoding from the alphabet

				// This is our local Sigma
				bdd_events = manager.orTo(bdd_events, es[i].bdd);

				if (es[i].p)    // check if it is prioritized in our automaton
				{
					bdd_events_p = manager.orTo(bdd_events_p, es[i].bdd);
				}

				if (!es[i].c)    // dito for uncontrollable events
				{
					bdd_events_u = manager.orTo(bdd_events_u, es[i].bdd);
				}
			}
		}

		// all setup done, create the BDDs:
		createI();
		createT();
		check("after creating I/T for automaton");

		/*
		 * String name =  "/tmp/dot/" + automaton.getName() + ".dot";
		 * manager.printDot(bdd_tu, name);
		 * Options.out.println("dot -Tps " + name + " -o " + name + ".ps");
		 * Options.out.println("ghostview "+ name + ".ps &");
		 */
	}

	public void cleanup()
	{
		// check("Cleanup");

		manager.deref(cube_s);
		manager.deref(cube_sp);
		manager.deref(cube_spp);

		if(dependency != null) {
		    dependency.cleanup();
		    dependency = null;
		}
	}

	// ------------------------------------------------------------------
    public DependencySet getDependencySet()
    {
		if(dependency == null) dependency = new DependencySet(manager,this);
		return dependency;
    }

	public boolean eventUsed(Event e)
	{
		return automaton.eventUsed(e);
	}

	public int[] getVar()
	{
		return var_s;
	}

	public int[] getVarp()
	{
		return var_sp;
	}

	public int getCube()
	{
		return cube_s;
	}

	public int getCubep()
	{
		return cube_sp;
	}

	public int getCubepp()
	{
		return cube_spp;
	}
	public int getI()
	{
		return bdd_i;
	}    // I(s)

	public int getT()
	{
		return bdd_t;
	}    // T(s,s',e)

	public int getTu()
	{
		return bdd_tu;
	}    // Tu(s,s',e) , e in Sigma_u

	public int getTtop()
	{
		return bdd_t_top;
	}    // T^(s,s',e) , see Knuts "A note on prioritized sync..."

	public int getTpri()
	{
		return bdd_t_pri;
	}    // T prioritized, see Knuts paper again

	public int getM()
	{
		return bdd_m;
	}    // M(s)

	public int getF()
	{
		return bdd_f;
	}    // X(s) or F(s) (forbidden)

	public int getCareS()
	{
		return bdd_care_s;
	}    // subset of S, only states that are used

	public int getDontCareS()
	{
		return bdd_dontcare_s;
	}    // S \ care-S, i.e. states not used

	public int getCareSp()
	{
		return bdd_care_sp;
	}

	public int getDontCareSp()
	{
		return bdd_dontcare_sp;
	}

	public int getSigmaP()
	{
		return bdd_events_p;
	}    // Sigma_p(e) - prioritized events

	public int getSigma()
	{
		return bdd_events;
	}    // Sigma(e) - all events used in this automaton

	public int getSigmaU()
	{
		return bdd_events_u;
	}    // Sigma_u(e) - uncontrollable events

	public int getKeep()
	{
		return bdd_keep;
	}

	public State[] getStates()
	{
		return states;
	}    // the non-symbolic state set + thier BDD coding and more...

	public String getName()
	{
		return automaton.getName();
	}
    public int getIndex()
    {
		return index;
    }

	public Automaton getModel()
	{
		return automaton;
	}

	public int getType()
	{
		return automaton.getType();
	}

	public int getNumStates()
	{
		return num_states;
	}

	public int getNumStateBits()
	{
		return num_bits;
	}

	public int getNumArcs() {
		return num_arcs;
	}

	public int getMembership() {
		return membership;
	}

	public void setMembership(int m) {
		membership = m;
	}

	/** get the first bdd variable [for default order], i.e. the one with lowest index */
	public int getTopBDD() {
		return index_first_bdd;
	}
	/** get the last bdd variable [for default order] i.e. the one with highest index */
	public int getBottomBDD() {
		return index_last_bdd;
	}
	// --------------------------------------------------------

    public boolean interact(BDDAutomaton ba) {
		return automaton.interact(ba.automaton);
    }

    public boolean interact(BDDAutomaton ba, boolean [] careSet) {
		return automaton.interact(ba.automaton, careSet);
    }

    public boolean interact(boolean [] careSet) {
		return automaton.interact(careSet);
    }



	/** returns the number of events that overlapp */
	public int eventOverlapCount(boolean [] events) {
		return automaton.eventOverlapCount(events);
	}

	/** returns the number of events that overlapp in ARCS */
	public int arcOverlapCount(boolean [] events) {
		return automaton.arcOverlapCount(events);
	}


	/**
	 * maps event -> number of times that event was used in a transition
	 * good for heuristics...
     */
	public int [] getEventUsageCount() {
		return automaton.getEventUsageCount();
	}

	public void addEventCareSet(boolean [] events, boolean [] result, boolean uncontrollable_events_only)
	{
		automaton.addEventCareSet(events, result, uncontrollable_events_only);
	}

	public boolean [] getEventCareSet(boolean uncontrollable_events_only)
	{
		return automaton.getEventCareSet(uncontrollable_events_only);
	}

	/**
	 * See Automaton.getEventFlow() for more info :(
	 * After which events just fired, we may do a transition
	 *
	 */
    public boolean [] getEventFlow(boolean forward) {
		return automaton.getEventFlow(forward);
	}

	/**
	 * See Group.removeEventUsage() for more info :(
	 *
	 */
	public void removeEventUsage(int [] count) {
		automaton.removeEventUsage(count);
	}

	/**
	 * See Group.addEventUsage() for more info :(
	 *
	 */
	public void addEventUsage(int [] count) {
		automaton.addEventUsage(count);
	}



	// ------------------------------------------------------------------


	private void createI()
	{
		bdd_i = manager.getOne();

		manager.ref(bdd_i);

		for (int i = 0; i < num_states; i++)
		{
			if (states[i].i)
			{
				int this_state = Util.getNumber(manager, var_s, states[i].code);

				bdd_i = manager.andTo(bdd_i, this_state);
			}
		}
	}

	private void createT()
	{
		bdd_tu = manager.getZero();

		manager.ref(bdd_tu);

		bdd_t = manager.getZero();

		manager.ref(bdd_t);

		for (int i = 0; i < num_arcs; i++)
		{
			int bdd_event = events[arcs[i].e_code].bdd;
			int bdd_state1 = states[arcs[i].s1_code].bdd_s;
			int bdd_state2 = states[arcs[i].s2_code].bdd_sp;
			boolean uncontrollable = !events[arcs[i].e_code].c;
			int this_t = manager.and(bdd_event, bdd_state2);

			this_t = manager.andTo(this_t, bdd_state1);
			bdd_t = manager.orTo(bdd_t, this_t);

			// if uncontrollable, we also add it to bdd_tu:
			if (uncontrollable)
			{
				bdd_tu = manager.orTo(bdd_tu, this_t);
			}

			manager.deref(this_t);
		}

		// ------------------------------------------------------------------------------------
		// create BDD T-top. this is basically the same thing as in Knuts "A note on prioritized
		// sync..." se definition of \delta_i^{\^} at the bottom of page 3. What it does is to
		// add a self-loop if there exists no transition for a given pair of state (current) and
		// event.
		// im not sure about the order of Exists and And  here :)
		bdd_t_top = manager.exists(bdd_t, cube_sp);

		int tmp = manager.not(bdd_t_top);

		manager.deref(bdd_t_top);

		bdd_t_top = tmp;
		tmp = manager.and(bdd_t_top, bdd_keep);

		manager.deref(bdd_t_top);

		bdd_t_top = tmp;
		tmp = manager.or(bdd_t, bdd_t_top);

		manager.deref(bdd_t_top);

		bdd_t_top = tmp;

		// -------------------------------------------------------------------------------
		// create T-prioritized. The total T after prioritized composition is a cunjunction
		// of these T's. The idea is to either use T if the event is prioritized or T-top when not
		int not_sigma_p = manager.not(bdd_events_p);

		tmp = manager.or(bdd_t, not_sigma_p);

		manager.deref(not_sigma_p);

		bdd_t_pri = tmp;
		tmp = manager.exists(bdd_t_pri, cube_sp);

		manager.deref(bdd_t_pri);

		bdd_t_pri = tmp;
		tmp = manager.and(bdd_t_top, bdd_t_pri);

		manager.deref(bdd_t_pri);

		bdd_t_pri = tmp;

		// BDDAssert.debug("BDD_T"); manager.printSet(bdd_t);
		// BDDAssert.debug("BDD_T_TOP");        manager.printSet(bdd_t_top);
		// BDDAssert.debug("BDD_T_PRI"); manager.printSet(bdd_t_pri);
	}

	// -------------------------------------------------------------------
	private void dumpBDD(PrintStream ps, String name, int bdd, int vars)
	{
		ps.println("BDD " + name + ": " + manager.nodeCount(bdd) + " nodes, SAT-count = " + manager.satCount(bdd, vars) + ", refcount = " + manager.internal_refcount(bdd));
	}

	public void dump(PrintStream ps)
	{

		// TODO: does it change when the keep is added??
		int size_states = num_bits;
		int size_events = manager.getEventsBits();
		int size_function = 2 * size_states + size_events;

		ps.println("-------------------- BDD for automaton " + automaton.getName());
		ps.println("BDD Variables: 2 * " + num_bits + ", " + num_states + " states");
		dumpBDD(ps, "I", bdd_i, size_states);
		dumpBDD(ps, "T", bdd_t, size_function);
		dumpBDD(ps, "T_u", bdd_tu, size_function);
		dumpBDD(ps, "Q_m", bdd_m, size_states);
		dumpBDD(ps, "Q_x", bdd_f, size_states);
		dumpBDD(ps, "E_p", bdd_events_p, size_events);
		ps.println();
	}

	private void check(String name)
	{    // for debug only: check automaton integrity:
		if (!Options.sanity_check_on)
		{
			return;    // ignore
		}

		int errors = 0;

		for (int i = 0; i < num_states; i++)
		{
			if (manager.internal_refcount(states[i].bdd_s) == 0)
			{
				System.err.println("State " + i + " has zero refcount: " + states[i].name);

				errors++;
			}

			if (manager.internal_refcount(states[i].bdd_sp) == 0)
			{
				System.err.println("State' " + i + " has zero refcount: " + states[i].name);

				errors++;
			}
		}

		if (errors > 0)
		{
		    System.err.println("TEST '" + name + "' --> BDD structure integrity check failed for: " + automaton.getName());
		    System.exit(20);
		}

		BDDAssert.internalCheck(manager.checkPackage(), name + ": checkPackage() failed");
	}


	public void showEncoding(BDDAutomata ba) {
		BDDAssert.internalCheck(manager == ba, "[INTERNAL ERROR] in BDDAutomaton.showEncoding() ");

		Options.out.println("\n ---------------- BDD encoding of " + getName());
		for(int i = 0; i < states.length; i++) {
			Options.out.println(states[i].name_id );
			ba.printSet(states[i].bdd_s );
		}
		Options.out.println();
	}
	// ---[ WeightedObject stuff ]-----------------------------------------------------
	public Object object() { return this; }
	public double weight() { return getIndex(); }
}
