package org.supremica.util.BDD;

import java.util.*;

public class Cluster
{
	public Vector members;    /* list of BDDAutomaton */
	public int twave, twaveu, cube, cubep;
	private BDDAutomata manager;

	// DEBUG private BDDRefCheck refcheck;
	public Cluster(BDDAutomata manager, int t, int tu, int c, int cp)
	{
		this.manager = manager;
		this.members = new Vector();
		this.twave   = manager.ref( t );
		this.twaveu  = manager.ref( tu );
		this.cube    = manager.ref( c );
		this.cubep   = manager.ref( cp);
	}

	public void cleanup()
	{

		// DEBUG
		// check("cleanup");
		manager.deref(twave);
		manager.deref(twaveu);
		manager.deref(cube);
		manager.deref(cubep);
	}

	/** deep copy of a Cluster (with BDD refs and everything) */
	public Cluster copy()
	{
		Cluster ret = new Cluster(manager, twave, twaveu, cube, cubep);
		Util.append(ret.members, this.members);
		return ret;
	}

	/* !!!! NOTE: this function is compleletly UNTESTED :(  !!!!*/
	public void join(Cluster with)
	{

		// System.err.println("UNTESTED joining of " +toString() + " AND " + with.toString() );
		Util.append(members, with.members);

		cube = manager.andTo(cube, with.cube);
		cubep = manager.andTo(cubep, with.cubep);
		twave = manager.orTo(twave, with.twave);
		twaveu = manager.orTo(twaveu, with.twaveu);
	}

	public boolean interact(Cluster c)
	{
		for (Enumeration e1 = members.elements(); e1.hasMoreElements(); )
		{
			BDDAutomaton a1 = (BDDAutomaton) e1.nextElement();

			for (Enumeration e2 = c.members.elements(); e2.hasMoreElements(); )
			{
				BDDAutomaton a2 = (BDDAutomaton) e2.nextElement();

				if ((a1 != a2) && a1.interact(a2))
				{
					return true;
				}
			}
		}

		return false;
	}

	// ------------------------------------------------

	/** DEBUG
	public void check(String place)
	{
			refcheck.check(place);
	}
	*/
	// ------------------------------------------------
	public int getTwave()
	{
		return twave;
	}

	public int getTwaveUncontrollable()
	{
		return twaveu;
	}
	public int getCube()
	{
		return cube;
	}

	public int getCubep()
	{
		return cubep;
	}

	/** number of nodes in T~ */
	public int getBDDSize()
	{
		return manager.nodeCount(twave);
	}

	// ------------------------------------------------
	public String toString()
	{
		if (members.size() == 1)
		{
			BDDAutomaton a = (BDDAutomaton) members.firstElement();

			return a.getName();
		}

		StringBuffer buf = new StringBuffer();

		buf.append("(");

		for (Enumeration e = members.elements(); e.hasMoreElements(); )
		{
			BDDAutomaton a1 = (BDDAutomaton) e.nextElement();

			buf.append(a1.getName());
			buf.append(" ");
		}

		buf.append(")");

		return buf.toString();
	}
}
;
