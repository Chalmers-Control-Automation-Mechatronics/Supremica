package org.supremica.util.BDD;

/**
 * A simple partitioned and smoothing supervisor that supports reachability in subset-of-BDDAutomata.
 * The idea is to have a very simple reachability procedure the is fast to initialize and still gives
 * good smoothing. we can use the other algorithms since they will include ALL automata in BDDAutomata
 * in the dependency sets, which will in the end give us the wrong T-wave...
 *
 * This dissadvantage is that the T-wave may differ each time a SimplePartitionSupervisor is created
 * so it cant be precalculated.
 */
public class SimplePartitionSupervisor
	extends ConjSupervisor
{
	private int[] t_wave;
	private BDDAutomaton[] automata;

	/** Constructor, passes to the base-class */
	public SimplePartitionSupervisor(BDDAutomata manager, BDDAutomaton[] as)
	{
		super(manager, as);

		init_simple();
	}

	public SimplePartitionSupervisor(BDDAutomata manager, Group plant, Group spec)
	{
		super(manager, plant, spec);

		init_simple();
	}

	private void init_simple()
	{

		// GroupHelper gh = new GroupHelper(plant, spec);
		automata = gh.getSortedList();

		int size = gh.getSize();

		// compute partial T-wave now:
		t_wave = new int[size];

		for (int i = 0; i < size; i++)
		{
			DependencySet ds = new DependencySet(manager, automata, automata[i]);

			t_wave[i] = manager.ref(ds.getTwave());

			ds.cleanup();
		}
	}

	/** C++-style Destructor to cleanup unused BDD trees*/
	public void cleanup()
	{
		for (int i = 0; i < t_wave.length; i++)
		{
			manager.deref(t_wave[i]);
		}

		super.cleanup();
	}

	// ------------------------------------------------------
	public String type()
	{
		return "SimplePartitionSupervisor";
	}

	// --[ FORWARD search ]--------------------------------------------

	/** do a forward reachability search from the initial state */
	protected void computeReachables()
	{
		int i_all = manager.and(plant.getI(), spec.getI());

		bdd_reachables = simple_computeReachables(i_all, t_wave);
		has_reachables = true;

		manager.deref(i_all);
	}

	/**
	 * do a FORWARD reachability search.
	 * start from the given (set of) initial state(s)
	 */
	public int getReachables(int initial_states)
	{
		return simple_computeReachables(initial_states, t_wave);
	}

	/**
	 * do a FORWARD reachability search, use only these events;
	 * start from the given initial state(s)
	 */
	public int getReachables(boolean[] events, int intial_states)
	{

		// get the modified twave
		int size = t_wave.length;
		int[] partial_twave = new int[size];

		for (int i = 0; i < size; i++)
		{
			DependencySet ds = new DependencySet(manager, automata, automata[i], events);

			partial_twave[i] = manager.ref(ds.getTwave());

			ds.cleanup();
		}

		int x = simple_computeReachables(intial_states, partial_twave);

		for (int i = 0; i < size; i++)
		{
			manager.deref(partial_twave[i]);
		}

		return x;
	}

	// ----------------------------------------------------------------------
	protected int simple_computeReachables(int initial_states, int[] disj_t)
	{

		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability" + type());

		SizeWatch.setOwner("SimplePartitionSupervisor.computeReachables");

		int num_access = 0, num_advance = 0;

		timer.reset();

		int i, j, size = disj_t.length;
		int i_all = initial_states;
		int r_all_p, r_all = i_all;

		manager.ref(r_all);    //gets derefed by orTo and finally a deref

		i = j = 0;

		limit.reset();

		do
		{
			r_all_p = r_all;

			int tmp = manager.relProd(disj_t[j], r_all, s_cube);
			int front = manager.replace(tmp, perm_sp2s);

			r_all = manager.orTo(r_all, front);

			manager.deref(front);
			manager.deref(tmp);

			if (r_all_p == r_all)
			{
				num_access++;

				if (i > 0)
				{
					num_advance++;
				}

				i = i + 1;
				j = (j + 1) % size;
			}
			else
			{
				i = 0;
			}

			if (gf != null)
			{
				gf.add(r_all);
			}
		}
		while ((i < size) &&!limit.stopped());

		if (gf != null)
		{
			gf.stopTimer();
		}

		timer.report("Forward reachables found (SimplePartition)");

		if (Options.profile_on && (num_access != 0))
		{
			Options.out.println("Pn advances: " + ((100 * num_advance) / num_access) + "%");
		}

		return r_all;
	}

	// --[ BACKWARD search ]--------------------------------------------
	// TODO: must first figure out how to test this (backward no used by the LI algo)

	/*
protected void simple_computeCoReachables(int m_all) {
			// int m_all = GroupHelper.getM(manager, spec, plant);

			GrowFrame gf = BDDGrow.getGrowFrame(manager, "backward reachability" + type());
			int num_access = 0, num_advance = 0; // statistics
			timer.reset();
			SizeWatch.setOwner("SimplePartitionSupervisor.computecoReachables");

			int i,j, size = t_wave.length;


			// gets derefed in first orTo, but replace addes its own ref
			int r_all_p, r_all = manager.replace(m_all, perm_s2sp);

			// manager.ref(r_all); // gets derefed soon

			SizeWatch.report(r_all, "Qm");


			i = j = 0;
			do {
					r_all_p = r_all;

					int tmp = manager.relProd(t_wave[j], r_all, sp_cube);
					int tmp2= manager.replace( tmp, perm_s2sp);
					r_all = manager.orTo(r_all, tmp2);
					manager.deref(tmp2);
					manager.deref(tmp);

					if(r_all_p == r_all) {
							num_access++;
							if(i > 0) num_advance++;

							i =  i + 1;
							j = (j + 1) % size;
					} else i = 0;

					if(gf != null)    gf.add( r_all );

			} while( i < size);

			// move the result from S' to S:
			int ret = manager.replace(r_all, perm_sp2s);

			// cleanup:
			manager.deref(r_all);

			has_coreachables = true;
			bdd_coreachables = ret;

			if(gf != null) gf.stopTimer();
			// SizeWatch.report(bdd_reachables, "Qco");
			timer.report("Co-reachables found (SimplePartition)");
			if(Options.profile_on && num_access != 0) {
					Options.out.println("Pn advances: " + ( (100 * num_advance) / num_access) + "%");
			}
}
*/

/*
		// --- [ testing stuff, dont touch ] --------------------------------------------------------------
		public static void main(String [] args) {

				String model =
"<?xml version='1.0' encoding='ISO-8859-1'?>" +
"<Automata>" +
" <Automaton name='Untitled SM'>" +
"  <Events>" +
"   <Event id='e0' label='e0' controllable='true' prioritized='true' />" +
"   <Event id='e1' label='e1' controllable='true' prioritized='true' />" +
"   <Event id='e2' label='e2' controllable='true' prioritized='true' />" +
"   <Event id='e3' label='e3' controllable='true' prioritized='true' />" +
"  </Events>" +
"  <States>" +
"   <State id='q0' initial='true' x='150' y='91' />" +
"   <State id='q1' x='214' y='155' />" +
"   <State id='q2' x='150' y='219' />" +
"   <State id='q3' x='86' y='155' />" +
"  </States>" +
"  <Transitions>" +
"   <Transition event='e0' source='q0' dest='q1' x='187' y='126' />" +
"   <Transition event='e1' source='q1' dest='q2' x='187' y='190' />" +
"   <Transition event='e2' source='q2' dest='q3' x='123' y='190' />" +
"   <Transition event='e3' source='q3' dest='q0' x='123' y='126' />" +
"  </Transitions>" +
" </Automaton>" +
" <Automaton name='Untitled SM 2'>" +
"  <Events>" +
"   <Event id='e0' label='e0' controllable='true' prioritized='true' />" +
"   <Event id='e3' label='e3' controllable='true' prioritized='true' />" +
"  </Events>" +
"  <States>" +
"   <State id='q0'  initial='true' x='150' y='91' />" +
"   <State id='q1' x='214' y='155' />" +
"   <State id='q2' x='86' y='155' />" +
"  </States>" +
"  <Transitions>" +
"   <Transition event='e0' source='q0' dest='q1' x='187' y='126' />" +
"   <Transition event='e3' source='q0' dest='q2' x='123' y='126' />" +
"  </Transitions>" +
" </Automaton>" +
"</Automata>";

				try {
						org.supremica.automata.IO.ProjectBuildFromXml builder1 = new org.supremica.automata.IO.ProjectBuildFromXml();
						org.supremica.automata.Automata automata1 =  builder1.build( new java.io.ByteArrayInputStream( model.getBytes() ) );





						Builder builder2 = new Builder(automata1);
						BDDAutomata automata2 = builder2.getBDDAutomata();
						BDDAutomaton [] automaton2 = automata2.getAutomataVector();
						SimplePartitionSupervisor sps = new SimplePartitionSupervisor(automata2, automaton2);
						Event[] events = automata2.getEvents();


						int r = sps.getReachables();
						automata2.show_states(r);


						boolean [] events_care = new boolean[ events.length ];
						for(int i = 0; i < events_care.length; i++) {
								IndexedSet.empty(events_care);
								events_care[i] = true;

								System.out.println("With " + events[i].getName() + " enabled: ");
								r = sps.getReachables(events_care);
								automata2.show_states(r);
						}

				} catch(Exception exx) {
						exx.printStackTrace();
				}

		}
		*/
}
