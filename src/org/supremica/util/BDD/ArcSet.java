

package org.supremica.util.BDD;

import java.util.*;
import java.io.*;

public class ArcSet 
    extends Vector
{
    private int count = 0;
    private boolean closed = false;

    // ------------------------------------------------ stuffs used BEFORE closing!
    private boolean in(String event,String s1, String s2) {
	// TODO : do something smarter than binary search :)	
	for(Enumeration e = elements(); e.hasMoreElements(); ) {
	    Arc a = (Arc) e.nextElement();
	    if(a.event.equals(event) &&a.state1.equals(s1) && a.state2.equals(s2)) 
		return true;
	}		
	return false;
    }

    public void add(String event, String s1, String s2) {
	Assert.assert(!closed, "[ArcSet.add]BAD FUNCTION CALL!");
	Assert.assert(!in(event,s1,s2),
		      "Duplicate arc: " + s1 + " -" + event + "-> " + s2);

	Arc arc= new Arc();
	arc.event = event;
	arc.state1 = s1;
	arc.state2 = s2;
	arc.id = count++;
	arc.e_code = arc.s1_code = arc.s2_code;

	addElement(arc);
    }


    // -------------------------------------------------------
    private Arc [] arcs;
    public int getSize() { return count; }
    public Arc [] getArcVector() {
	Assert.assert(closed, "[ArcSet.getArcVector] BAD FUNCTION CALL!");
	return arcs;
    }
    public Arc getArc(int index) { 
	Assert.assert(closed, "[ArcSet.getArc]BAD FUNCTION CALL!");
	Assert.assert( index >= 0 && index < count, "BAD arc-index");
	return arcs[index];
    }
    public void close(StateSet ss, EventSet es) {
	Assert.assert(!closed, "[ArcSet.close] BAD FUNCTION CALL!");	



	arcs = new Arc[count];
	for(Enumeration e = elements(); e.hasMoreElements(); ) {
	    Arc ev = (Arc) e.nextElement();
	    Event event = es.getEventByName(ev.event);
	    Assert.assert(event != null, "event not found:" + ev.event);

	    ev.e_code  = event.id;
	    ev.s1_code = ss.getIdByName( ev.state1);
	    ev.s2_code = ss.getIdByName( ev.state2);
	    Assert.assert(ev.e_code != Automaton.FAILED,"event not found:" + ev.event);
	    Assert.assert(ev.s1_code !=Automaton.FAILED,"from-state not found:" + ev.state1);
	    Assert.assert(ev.s2_code !=Automaton.FAILED,"to-state not found:" + ev.state2);
	    
	    // mark that this event has been used one time in a transition
	    event.use++;
	    arcs[ev.id] = ev;
	}
	closed = true;
    }


    
    public void dump(PrintStream ps) {
	ps.println("Transitions = {");
	
	for(int i = 0; i < count; i++) {	    
	    ps.println("\t"+arcs[i].state1 + "  --" + arcs[i].event + "-->  " +
		     arcs[i].state2 + ";");
	}    

	ps.println(" };");	


	ps.println();
    }
    
}
