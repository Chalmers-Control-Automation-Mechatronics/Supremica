package org.supremica.util.BDD;

import java.util.*;
import java.io.*;

public class Automaton
	implements PCGNode
{
	public static final int FAILED = -1,    // universal FAIL code
	    TYPE_UNKNOWN = 0, TYPE_PLANT = 1, TYPE_SPEC = 2, TYPE_SUPERVISOR = 3
	;

	public static String getType(int type)
	{
		switch (type)
		{

		case TYPE_SUPERVISOR :
			return "supervisor";

		case TYPE_SPEC :
			return "spec";

		case TYPE_PLANT :
			return "plant";

		case TYPE_UNKNOWN :
			return "unknown";

		default :
			return "ERROR";
		}
	}

	private int type;
	private boolean closed;
	private String name;
	private StateSet stateSet = new StateSet();
	private EventSet eventSet = new EventSet();
	private ArcSet arcSet = new ArcSet();
	private EventManager alphabet;
	private Object original; // the Supremica automata

	private int [][] eventFlowMatrix = null;
	private boolean [] care_set, care_set_uc; // Events that we have in our alphabeth, and are uncontrollabe
	private int [] event_usage; // how many times each event was used





	// ------------------------------------------------------------------------------------------

	public Automaton(String name, EventManager alphabet, Object original)
	{
		this.alphabet = alphabet;
		this.name = name;
		this.original = original;
		type = TYPE_UNKNOWN;
		closed = false;
	}


	// ------------------------------------------------------------

	// From PCGNode:
	public int getSize()
	{
		return stateSet.count;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public int getType()
	{
		return type;
	}

	public void addState(String name, String id, boolean i, boolean m, boolean x)
	    throws BDDException
	{
		if (!closed)
		{
			stateSet.add(name, id, i, m, x);
		}
	}

	public void addEvent(String label, String id, boolean c, boolean p)
	    throws BDDException
	{
		if (!closed)
		{
			eventSet.add(alphabet, label, id, c, p, this);
		}
	}

	public void addArc(String name, String from, String to)
	    throws BDDException
	{
		if (!closed)
		{
			arcSet.add(name, from, to);
		}
	}

	// ------------------------------------------------------------

	/** returns the supremica-automata this object was built from */
	public Object getSupremicaModel()
	{
		return original;
	}

	// ------------------------------------------------------------

	private void saturate(Vector v)
	{
	    Enumeration it = v.elements();
	    while(it.hasMoreElements()) {
		Event e = (Event) it.nextElement();
		    saturate(e);
	    }
	}
	private void saturate(Event e)
	{

	    Vector v = arcSet.getArcVector(e);
	    Enumeration it = v.elements();
	    while(it.hasMoreElements()) {
		Arc a = (Arc) it.nextElement();
		arcSet.saturate(a.state1, a.state2, e, eventSet);
	    }
	}


	// ------------------------------------------------------------
	public void close()
	    throws BDDException
	{


		// TODO: this function seems to be extremly inefficient, more linear searchs??
		closed = true;

		// Dont call close yet (we dont know the size of global alphabet yet)
		// eventSet.close();

	}

	public void close2()
	    throws BDDException
	{


	    // find my local events
	    if(Options.local_saturation) {
			Vector v = alphabet.getLocalEvents(this);
			saturate(v);
	    }



	    arcSet.close(stateSet, eventSet, alphabet.getSize());
	    stateSet.close();

		// EventManager.close() has been called and we can proceed
		eventSet.close(alphabet);

		// Options.out.println("closing " + name);
		// arcSet.close(stateSet, eventSet);
		// Options.out.println("done!");

		care_set = eventSet.getEventCareSet(false);
		care_set_uc = eventSet.getEventCareSet(true);
		event_usage = arcSet.getEventUsageCount();



		// DEBUG: dump careset
		// alphabet.dumpSubset("  Regular events for " + getName(), care_set);
		// alphabet.dumpSubset("  UC events for " + getName(), care_set_uc);


	}

	// ------------------------------------------------------------

	/** maps event -> number of times that event was used in a transition */
	public int [] getEventUsageCount() {
		return arcSet.getEventUsageCount();
	}

    public boolean interact(Automaton a)
    {
		// return eventSet.overlap(a.eventSet);

		int es = care_set.length;
		for(int i = 0; i < es; i++)
			if(a.care_set[i] && care_set[i])
				return true;
		return false;
    }

	public boolean interact(Automaton a, boolean [] cares)
	{
		// return eventSet.overlap(a.eventSet, careSet);

		int es = care_set.length;
		for(int i = 0; i < es; i++)
			if(cares[i] && a.care_set[i] && care_set[i])
				return true;
		return false;
    }

    public boolean interact(boolean [] cares)
    {
		// return eventSet.overlap(careSet);
		int es = care_set.length;
		for(int i = 0; i < es; i++)
			if(cares[i] && care_set[i])
				return true;
		return false;
    }

	/** returns the number of events that overlapp */
	public int eventOverlapCount(boolean [] events) {
		int count = 0;
		int es = events.length;
		for(int i = 0; i < es; i++)
			if(events[i] && care_set[i])
				count++;
		return count;
	}


	/** returns the number of ARCS that overlapp  with these events*/
		public int arcOverlapCount(boolean [] events) {
			int [] usage = arcSet.getEventUsageCount();
			int count = 0;
			int es = events.length;
			for(int i = 0; i < es; i++)
				if(events[i] && care_set[i])
					count += usage[i];
			return count;
	}

	// ---------------------------------------------------------------

    public void addEventCareSet(boolean [] events, boolean [] result, boolean uncontrollable_events_only)
    {
		// eventSet.addEventCareSet(events, uncontrollable_events_only);
		boolean [] es = getEventCareSet(uncontrollable_events_only);
		int size= es.length;
		for(int i = 0; i < size; i++)
			result[i] = events[i] | es[i];
	}

	public boolean [] getEventCareSet(boolean uncontrollable_events_only)
	{
		return uncontrollable_events_only ? care_set_uc : care_set;
	}


	// ----------------------------------------------------------------------

	public String getName()
	{
		return name;
	}

	public int getStatesSize()
	{
		return stateSet.getSize();
	}

	public int getEventsSize()
	{
		return eventSet.getSize();
	}

	public int getArcsSize()
	{
		return arcSet.getSize();
	}

	public StateSet getStates()
	{
		return stateSet;
	}

	public EventSet getEvents()
	{
		return eventSet;
	}

	public ArcSet getArcs()
	{
		return arcSet;
	}

	public int getStateVectorSize()
	{
		return Util.log2ceil(getStatesSize());
	}

	public boolean eventUsed(Event e)
	{
		return eventSet.overlap(e);
	}

	// -----------------------------------------------------------

	// _estimaed_ communication between two automata.
	// current estimation is the avreage number of common events
	// used in transitions in each automaton
	public int getCommunicationComplexity(Automaton other)
	    throws BDDException
	{
		int ret = 0;
		Event[] e1s = this.eventSet.getEventVector();
		Event[] e2s = other.eventSet.getEventVector();
		int size = e1s.length;

		for (int i = 0; i < size; i++)
		{
			if ((e1s[i] != null) && (e2s[i] != null))
			{
				if ((e1s[i].use > 0) && (e2s[i].use > 0))
				{
					ret += (e1s[i].use + e2s[i].use);
				}
			}
		}

		return ret / 2;    // to get the average
	}





	/**
	 * See Group.removeEventUsage() for more info :(
	 *
	 */
	public void removeEventUsage(int [] count) {
		int len = count.length;
		for(int i = 0; i <  len; i++)
			if(care_set[i]) count[i] --;
	}

	/**
	 * See Group.addEventUsage() for more info :(
	 *
	 */
	public void addEventUsage(int [] count) {
		int len = count.length;
		for(int i = 0; i <  len; i++)
			if(care_set[i]) count[i] ++;

	}

	//  ---------[ these are here to be used in a eventflow/workset supervisor some day :)  ]---
	/**
	 * get event flow matrix.
	 * m(s1,s2)  is the number of times s1 is followed by s1 in the automata
	 */
	public int [][]getEventFlowMatrix()
	{

		if(eventFlowMatrix == null)
			computeEventFlowMatrix();
		return eventFlowMatrix;

	}

	private void computeEventFlowMatrix()
	{
		int i, j;
		int events = alphabet.getSize();
		eventFlowMatrix = new int[events][events];
		for(i = 0; i < events; i++) for(j = 0; j < events; j++) eventFlowMatrix[i][j] = 0;

		Arc[] arcs = arcSet.getArcVector();
		int arcs_count = arcSet.getSize();
		for(int as = 0; as < arcs_count; as ++) {
			Arc a1 = arcs[as];
			for (Enumeration e = a1.next.elements(); e.hasMoreElements(); ) {
				Arc a2 = (Arc) e.nextElement();
				i = a1.o_event.id;
				j = a2.o_event.id;
				eventFlowMatrix[i][j]++;
			}
		}
	}

	/**
	 * After which events just fired, we may do a transition
	 *
	 */
	public boolean [] getEventFlow(boolean forward) {
		int events = alphabet.getSize();
		boolean [] ret = new boolean[events];
		int [][]m = getEventFlowMatrix();

		for(int i = 0; i < events; i++) {
			int tmp = 0;
			for(int j = 0; j < events; j++) {
				tmp += forward ? m[i][j] : m[j][i];
			}
			ret[i] = tmp > 0;
		}
		return ret;
	}

	// -----------------------------------------------------------
	public void dump(PrintStream ps)
	{
		ps.println("--- Automaton " + name + "  (" + getType(type) + ")");
		stateSet.dump(ps);
		eventSet.dump(ps);
		arcSet.dump(ps);
	}
}
