package org.supremica.util.BDD;

import java.util.*;


/**
 * A cluster is a set of one or more automata. Clusters are used when single automaton are too small
 * to be efficient for computation while the monolithic is too large. Clusters give you a balance between
 * memory usage and speed. Currently in Supremica, clusters are most often singletons.
 */

public class Cluster
{
	private Collection members;    /* list of BDDAutomaton */
	private int twave, twaveu, cube, cubep, keep; /** BDDs */
	private boolean [] event_care; /** what events are included in this cluster ? */
	private int included_events; /** number of events that are used in this cluster, equals the cardinality of the care-set */
	private int state_vector_bits; /** size of the state vector, good for estimating T~ size ?*/
	private BDDAutomata manager;

	/**
	 * Create an empty cluster
	 *
	 */
	public Cluster(BDDAutomata manager)
	{
		this(manager, null);
	}

	/**
	 * create a cluster with one automaton in it
	 */
	public Cluster(BDDAutomata manager, BDDAutomaton a)
	{

		this.manager = manager;
		this.members = new LinkedList();

		this.state_vector_bits = 0;

		// initialize BDDs
		this.twave   = manager.ref(manager.getZero() );
		this.twaveu  = manager.ref(manager.getZero() );
		this.cube    = manager.ref(manager.getOne() );
		this.cubep   = manager.ref(manager.getOne() );
		this.keep    = manager.ref(manager.getOne() );

		// things that have to do with the care-set:
		this.included_events = 0;
		this.event_care = new boolean[ manager.getEvents().length ];
		for(int i = 0; i < event_care.length; i++)
		{
			event_care[i] = false;
		}

		if(a != null)
		{
			addAutomaton(a);
		}


	}

	/**
	 * Add an automaton to this cluster
	 *
	 */
	public void addAutomaton( BDDAutomaton a) {

		if( ! members.contains(a) )
		{
			members.add(a);

			state_vector_bits += a.getNumStateBits();

			// update the bdds
			twave = manager.orTo( twave, a.getDependencySet().getTwave() );
			twaveu = manager.orTo( twaveu, a.getDependencySet().getTwaveUncontrollable() );
			cube = manager.andTo(cube, a.getCube() );
			cubep = manager.andTo(cubep, a.getCubep() );
			keep = manager.andTo(keep, a.getKeep() );

			// update the care set
			boolean [] cs = a.getEventCareSet(false);
			included_events = 0;
			for(int i = 0; i < event_care.length; i++)
			{
				event_care[i] |= cs[i];
				if(event_care[i]) included_events ++;
			}
		}
	}

	/**
	 * this function is called when we are done with this cluster
	 */
	public void cleanup()
	{

		manager.deref(twave);
		manager.deref(twaveu);
		manager.deref(cube);
		manager.deref(cubep);

		members.clear();
	}

	/**
	 * deep copy of a Cluster (with BDD refs and everything)
	 */
	public Cluster copy()
	{
		Cluster ret = new Cluster(manager);
		ret.join(this);

		return ret;
	}



	/**
	 * add the elements of <tt>with</tt> cluster to this cluster
	 */
	public void join(Cluster with)
	{
		for (Iterator it = with.members.iterator(); it.hasNext(); )
		{
			BDDAutomaton a = (BDDAutomaton) it.next();
			addAutomaton(a);
		}
	}


	public boolean interact(Cluster c)
	{
		for (Iterator e1 = members.iterator(); e1.hasNext(); )
		{
			BDDAutomaton a1 = (BDDAutomaton) e1.next();

			for (Iterator e2 = c.members.iterator(); e2.hasNext(); )
			{
				BDDAutomaton a2 = (BDDAutomaton) e2.next();

				if ((a1 != a2) && a1.interact(a2))
				{
					return true;
				}
			}
		}

		return false;
	}

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

	public Iterator iterator()
	{
		return members.iterator();
	}

	public boolean [] getCareSet()
	{
		return event_care;
	}
	public int getAlphabetSize()
	{
		return included_events;
	}

	/** returns the size of the state vector */
	public int getSizeOfS()
	{
		return state_vector_bits;
	}


	/** return the BDD for keep */
	public int getKeep()
	{
		return keep;
	}


	/**
	 * return the number of events that overlap with this other cluster
	 */
	public int sharedEvents(Cluster that) {
		// special case
		if(this == that)
		{
			return getAlphabetSize();
		}

		int ret = 0;
		int size = event_care.length;

		for(int i = 0; i < size; i++)
		{
			if(event_care[i] && that.event_care[i])
			{
				ret ++;
			}
		}

		return ret;
	}

	/**
	 * return the number of events that are not shared between these clusters
	 */
	public int disjointEvents(Cluster that)
	{
		return getAlphabetSize() + that.getAlphabetSize() - 2 * sharedEvents(that);
	}


	/**
	 * return the events that are added if this and <tt>that</tt> cluster are joined
	 */
	public int additionalEvents(Cluster that)
	{
		return that.getAlphabetSize() - sharedEvents(that);
	}

	// ------------------------------------------------


	public String toString()
	{
		Iterator e = members.iterator();

		// if we have only one automaton, return its name
		if (members.size() == 1)
		{
			BDDAutomaton a = (BDDAutomaton) e.next();

			return a.getName();
		}

		// otherwise create a composite name
		StringBuffer buf = new StringBuffer("(");

		for (; e.hasNext(); )
		{
			BDDAutomaton a1 = (BDDAutomaton) e.next();

			buf.append(a1.getName());
			buf.append(" ");
		}

		buf.append(")");

		return buf.toString();
	}
}
;
