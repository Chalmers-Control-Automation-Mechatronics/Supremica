package org.supremica.util.BDD;

import java.util.*;
import java.io.*;

public class ArcSet
	extends Vector
{
	private int count = 0;
	private boolean closed = false;

	// ------------------------------------------------ stuffs used BEFORE closing!
	private boolean in(String event, String s1, String s2)
	{

		// TODO : do something smarter than binary search :)
		for (Enumeration e = elements(); e.hasMoreElements(); )
		{
			Arc a = (Arc) e.nextElement();

			if (a.event.equals(event) && a.state1.equals(s1) && a.state2.equals(s2))
			{
				return true;
			}
		}

		return false;
	}

  
	public void add(String event, String s1, String s2)
	    throws BDDException
	{
		BDDAssert.internalCheck(!closed, "[ArcSet.add]BAD FUNCTION CALL!");
		BDDAssert.bddAssert(!in(event, s1, s2), "Duplicate arc: " + s1 + " -" + event + "-> " + s2);

		Arc arc = new Arc();

		arc.event = event;
		arc.state1 = s1;
		arc.state2 = s2;
		arc.id = count++;
		arc.e_code = arc.s1_code = arc.s2_code;

		addElement(arc);
	}

	// -------------------------------------------------------
	private Arc[] arcs;

	public int getSize()
	{
		return count;
	}

	public Arc[] getArcVector()
	{
		BDDAssert.internalCheck(closed, "[ArcSet.getArcVector] BAD FUNCTION CALL!");

		return arcs;
	}

	public Vector getArcVector(Event e)
	{
		BDDAssert.internalCheck(!closed, "[ArcSet.getArcVector] BAD FUNCTION CALL!");
		Vector v = new Vector();

		for (Enumeration it = elements(); it.hasMoreElements(); ) {
		    Arc arc = (Arc) it.nextElement();
		    if(arc.event.equals(e.name_id)) v.add(arc);
		}
		return v;
	}

	public Arc getArc(int index)
	{
		BDDAssert.internalCheck(closed, "[ArcSet.getArc]BAD FUNCTION CALL!");
		BDDAssert.internalCheck((index >= 0) && (index < count), "BAD arc-index");

		return arcs[index];
	}

    
	public void saturate(String from, String to, Event e1, EventSet es) 
	{
	    BDDAssert.internalCheck(!closed, "[ArcSet.saturate]BAD FUNCTION CALL!");


	   
	    for (Enumeration e = elements(); e.hasMoreElements(); ) {
		Arc arc = (Arc) e.nextElement();

		// FORWARD SATURATE
		if(arc.state2.equals(from)) {
		    Event e2 = es.getEventByName(arc.event);
		    if(e2.c == e1.c /* && e2.p == e1.p */) {
			if(!in(e2.name_id, arc.state1, to)) {
			    			    
			    Arc new_arc = new Arc();
			    
			    new_arc.event  = e2.name_id;
			    new_arc.state1 = arc.state1;
			    new_arc.state2 = to;
			    new_arc.id     = count++;
			    new_arc.e_code = new_arc.s1_code = new_arc.s2_code;
			    
			    addElement(new_arc); 
			}
		    }
		}



	    }
	}

	

	public void close(StateSet ss, EventSet es)
	    throws BDDException
	{
		BDDAssert.internalCheck(!closed, "[ArcSet.close] BAD FUNCTION CALL!");

		arcs = new Arc[count];

		for (Enumeration e = elements(); e.hasMoreElements(); )
		{
			Arc ev = (Arc) e.nextElement();
			Event event = es.getEventByName(ev.event);

			BDDAssert.bddAssert(event != null, "event not found:" + ev.event);

			ev.e_code = event.id;
			ev.s1_code = ss.getIdByName(ev.state1);
			ev.s2_code = ss.getIdByName(ev.state2);

			BDDAssert.bddAssert(ev.e_code != Automaton.FAILED, "event not found:" + ev.event);
			BDDAssert.bddAssert(ev.s1_code != Automaton.FAILED, "from-state not found:" + ev.state1);
			BDDAssert.bddAssert(ev.s2_code != Automaton.FAILED, "to-state not found:" + ev.state2);

			// mark that this event has been used one time in a transition
			event.use++;

			arcs[ev.id] = ev;
		}

		closed = true;
	}

	public void dump(PrintStream ps)
	{
		ps.println("Transitions = {");

		for (int i = 0; i < count; i++)
		{
			ps.println("\t" + arcs[i].state1 + "  --" + arcs[i].event + "-->  " + arcs[i].state2 + ";");
		}

		ps.println(" };");
		ps.println();
	}
}
