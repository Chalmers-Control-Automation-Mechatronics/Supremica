package org.supremica.util.BDD;

import java.io.*;

// XXX: dependency set computation is a little bit inefficient.
// it would be nicer if we could use the same dependency matrix for this and automata ordering.

public class DependencySet
{
	private BDDAutomata manager;
	private BDDAutomaton[] all, dependent;
	private BDDAutomaton me;
	private boolean[] map_dependency;
	private int bdd_keep_depend, bdd_keep_others, bdd_t_wave,
				bdd_t_wave_isolated, bdd_t_wave_u;
	private int bdd_i, bdd_cube, bdd_cube_others;

	public DependencySet(BDDAutomata manager, BDDAutomaton me)
	{
		this(manager, manager.getAutomataVector(), me, null);
	}

	public DependencySet(BDDAutomata manager, BDDAutomaton[] all, BDDAutomaton me)
	{
		this(manager, all, me, null);
	}

	public DependencySet(BDDAutomata manager, BDDAutomaton[] all, BDDAutomaton me, boolean[] event_care)
	{
		this.manager = manager;
		this.all = all;
		this.me = me;

		// which events do we care about??
		int event_mask = (event_care == null)
						 ? manager.ref(manager.getOne())
						 : manager.getAlphabetSubsetAsBDD(event_care);

		map_dependency = new boolean[all.length];

		for (int i = 0; i < all.length; i++)
		{
			map_dependency[i] = true;
		}


		// get dependet set, compute bdd_keep_others at the same time
		BDDAutomaton[] queue = new BDDAutomaton[all.length];
		int i, len = 0;

		bdd_keep_others = manager.getOne();

		manager.ref(bdd_keep_others);

		bdd_cube = me.getCube();

		manager.ref(bdd_cube);

		bdd_cube_others = manager.getOne();

		manager.ref(bdd_cube_others);

		for (i = 0; i < all.length; i++)
		{
			if (all[i] != me)
			{

				// ignore event-interaction if it is outside the event-care
				boolean interact = ((event_care == null)
									? me.interact(all[i])
									: me.interact(all[i], event_care));

				if (interact)
				{
					queue[len++] = all[i];

					// tmp.add( all[i]);
					bdd_cube = manager.and(bdd_cube, all[i].getCube());

				}
				else
				{
					bdd_keep_others = manager.andTo(bdd_keep_others, all[i].getKeep());
					bdd_cube_others = manager.andTo(bdd_cube_others, all[i].getCube());
					map_dependency[i] = false;
				}
			}
		}

		SizeWatch.setOwner(getName() + " (" + len + ")"); // XXX: we set owner first here because we need the dependency size!
		SizeWatch.report(bdd_keep_others, "keep_others");

		// get an array with correct size:
		dependent = new BDDAutomaton[len];

		for (i = 0; i < len; i++)
		{
			dependent[i] = queue[i];
		}



		// calc Twave etc
		bdd_i = manager.ref(me.getI());


		BDDArrayOperation bao = new BDDArrayOperation(manager); // NEW
		bao.add(me.getT() ); // NEW


		for (i = 0; i < len; i++)
		{
			// XXX: if we do this (i) in reverse order, thing get much slower for some reason:
			BDDAutomaton a_i = dependent[i];


			// TWave
			int common_events = manager.and(a_i.getSigma(), me.getSigma());
			int uncommon_events = manager.not(common_events);
			int move = manager.and(a_i.getT(), common_events);
			int keep = manager.and(a_i.getKeep(), uncommon_events);

			manager.deref(common_events);
			manager.deref(uncommon_events);

			int dep_move = manager.or(move, keep);

			manager.deref(move);
			manager.deref(keep);
			SizeWatch.report(dep_move, "dep-move_" + a_i.getName());

			bao.add(dep_move); // NEW
			manager.deref(dep_move);

			// I
			bdd_i = manager.andTo(bdd_i, a_i.getI());
		}


		int tmp3 = bao.andAll(); // NEW
		bao.cleanup(); // NEW

		tmp3 = manager.andTo(tmp3, event_mask);    // <-- must remove unused crap!
		bdd_t_wave_isolated = manager.exists(tmp3, manager.getEventCube());    // XXX: why dones this one includes event-variables ??
		bdd_t_wave = manager.relProd(tmp3, bdd_keep_others, manager.getEventCube());

		// now, get the uncontrollable subset of t_wave:
		tmp3 = manager.andTo(tmp3, manager.getSigmaU() );
		bdd_t_wave_u = manager.relProd(tmp3, bdd_keep_others, manager.getEventCube());

		manager.deref(tmp3);
		manager.deref(event_mask);
		SizeWatch.report(bdd_t_wave, "Twave");
		SizeWatch.report(bdd_t_wave_u, "Twave_u");
	}

	// ------------------------------------------------------------------
	public void cleanup()
	{
		manager.deref(bdd_keep_others);
		manager.deref(bdd_keep_depend);
		manager.deref(bdd_t_wave_isolated);
		manager.deref(bdd_t_wave);
		manager.deref(bdd_t_wave_u);
		manager.deref(bdd_i);
		manager.deref(bdd_cube_others);
	}

	// ------------------------------------------------------------------
	public BDDAutomaton[] getSet()
	{
		return dependent;
	}

	public int getCube()
	{
		return bdd_cube;
	}

	public int getCubeOthers()
	{
		return bdd_cube_others;
	}

	public int getTwave()
	{
		return bdd_t_wave;
	}

	public int getTwaveUncontrollable()
	{
		return bdd_t_wave_u;
	}

	public int getTwaveIsolated()
	{
		return bdd_t_wave_isolated;
	}

	public int getI()
	{
		return bdd_i;
	}


	// -----------------------------------------------------------------
	public int getReachables(int start)
	{
		int cube = manager.getStateCube();
		int permute = manager.getPermuteSp2S();
		int q, qp, front;

		front = q = start;

		manager.ref(q);    // orTo
		manager.ref(front);    // deref after orTo

		do
		{
			qp = q;

			int tmp = manager.relProd(bdd_t_wave_isolated, front, cube);
			int tmp2 = manager.replace(tmp, permute);

			manager.deref(tmp);

			q = manager.orTo(q, tmp2);

			manager.deref(front);

			front = tmp2;
		}
		while (q != qp);

		manager.deref(front);

		return q;
	}

	// ------------------------------------------------------------------
	public void dump(PrintStream ps)
	{
		ps.print(me.getName() + " is dependent on { ");

		for (int i = 0; i < dependent.length; i++)
		{
			if (i != 0)
			{
				ps.print(", ");
			}

			ps.print(dependent[i].getName());
		}

		ps.println(" };");
	}

	public String getName()
	{
		return "DependencySet_" + me.getName();
	}
}
