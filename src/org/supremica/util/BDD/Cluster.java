
package org.supremica.util.BDD;

import java.util.*;



public class Cluster {
	public Vector members; /* list of BDDAutomaton */
	public int twave, cube, cubep;
	private BDDAutomata manager;

	public Cluster(BDDAutomata manager, int t, int c, int cp) {
		this.manager = manager;
		members = new Vector();

		this.twave = t;
		this.cube  = c;
		this.cubep = cp;

		manager.ref(this.twave);
		manager.ref(this.cube);
		manager.ref(this.cubep);
	}

	public void cleanup() {
		manager.deref(twave);
		manager.deref(cube);
		manager.deref(cubep);
	}


	/** deep copy of a Cluster (with BDD refs and everything) */
	public Cluster copy() {
		Cluster ret = new Cluster(manager, twave, cube, cubep);
		Util.append( ret.members, this.members);
		return ret;

	}

	/* !!!! NOTE: this function is compleletly UNTESTED :(  !!!!*/
	public void join(Cluster with) {
		Util.append( members, with.members);
		cube  = manager.andTo(cube,  with.cube);
		cubep = manager.andTo(cubep, with.cubep);
		twave = manager.orTo (twave, with.twave);
	}


	public boolean interact(Cluster c) {

		for(Enumeration e1 = members.elements(); e1.hasMoreElements(); ) {
			BDDAutomaton a1 = (BDDAutomaton) e1.nextElement();
			for(Enumeration e2 = c.members.elements(); e2.hasMoreElements(); ) {
				BDDAutomaton a2 = (BDDAutomaton) e2.nextElement();
				if(a1 != a2 && a1.interact(a2)) return true;
			}
		}

		return false;
	}


	public int getTwave()
	{
		return twave;
	}

	public int getCube()
	{
		return cube;
	}

	public int getCubep()
	{
		return cubep;
	}

};
