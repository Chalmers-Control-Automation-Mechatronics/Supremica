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

	public Automaton(String name, EventManager alphabet)
	{
		this.alphabet = alphabet;
		this.name = name;
		type = TYPE_UNKNOWN;
		closed = false;
	}

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

	    arcSet.close(stateSet, eventSet);
	    stateSet.close();


		// EventManager.close() has been called and we can proceed
		eventSet.close(alphabet);

		// System.out.println("closing " + name);
		// arcSet.close(stateSet, eventSet);
		// System.out.println("done!");
	}

	// ------------------------------------------------------------
    public boolean interact(Automaton a) {
	return eventSet.overlap(a.eventSet);
    }
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

	// -----------------------------------------------------------
	public void dump(PrintStream ps)
	{
		ps.println("--- Automaton " + name + "  (" + getType(type) + ")");
		stateSet.dump(ps);
		eventSet.dump(ps);
		arcSet.dump(ps);
	}

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
}
