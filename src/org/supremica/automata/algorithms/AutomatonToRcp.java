

// ++ ARASH

/**
 * Exprimental transformation to resource allocation model for BDD computation 
 * 
 *
 */

package org.supremica.automata.algorithms;

import java.util.*;
import java.io.*;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.EventLabel;


public class AutomatonToRcp 
    implements AutomataSerializer
{


    // allocation struct
    private class Allocation { 
	String owner;
	String resource;
	boolean book;
	public Allocation(String o, String r, boolean b) {
	    owner    = o;
	    resource = r;
	    book     = b;
	}
    }



    private Automaton aut;
    private String autname;

    public AutomatonToRcp(Automaton aut)   {
	this.aut = aut;
    }


    public void serialize(PrintWriter pw)
	throws Exception
    {
	pw.println("// Autogen by Supremica [AutomatonToRcp.java]");
	pw.println("module " +  /* WAS: aut.getName() */ autname + ";");

	String startState = null;

	// print states
	pw.print("states { ");
	boolean first = true;
	Iterator states = aut.stateIterator();
	while(states.hasNext() ) {
	    State state = (State) states.next();
	    if(first) first = false;
	    else pw.print(", ");
	    pw.print(state.getName() );
	    if(state.isInitial()) startState = state.getName();
	}
	pw.println(" };");
	

	// get resources:
	HashSet resSet = new HashSet();
	Iterator events = aut.eventIterator();	
	while(events.hasNext()) {
	    EventLabel event = (EventLabel) events.next();	    
	    Allocation al = getAllocation(event.getLabel());
	    resSet.add(al.resource);
	}


	// print resources
	pw.print("resources { ");
	Iterator resources = resSet.iterator();
	first = true;
	while(resources.hasNext()) {
	    if(first) first = false;
	    else pw.print(", ");
	    String res = (String) resources.next();
	    pw.print( res);
	}
	pw.println(" };\n"); // close and add extra space



	// print transitions:

	states = aut.stateIterator();

	while (states.hasNext()) {
	    State sourceState = (State) states.next();
	    String sourceName = sourceState.getName();

	    Iterator outgoingArcs = sourceState.outgoingArcsIterator();
	    while (outgoingArcs.hasNext()) {
		Arc arc = (Arc) outgoingArcs.next();
		State destState = arc.getToState();
		
		String event = aut.getAlphabet().getEventWithId(arc.getEventId()).getLabel();
		Allocation al = getAllocation(event);
		pw.println(sourceName + " { " +
			   (al.book ? "" : "~") + al.resource +
			   " } -> " + destState.getName() + 
			   ";"
			   // "\t\t\t//  " + (al.book ? "allocated" : "freed" ) + " by " + al.owner
			   );
			 
	    }
	}

	pw.println("\n"); // add extra space

	
	// print initial and marked state
	if(startState == null) throw new Exception("no initial state found");
	
	for(int i = 0; i < 2; i++) {
	    pw.print( (i == 0 ? "initial_" : "marked_") +
		      " = " + startState);

	    resources = resSet.iterator();
	    while(resources.hasNext()) {
		String res = (String) resources.next();
		pw.print( " & " + res);
	    }
	    pw.println(";");
	}    

	// ... and,  we are done
	pw.flush();
	pw.close();	

    }

    public void serialize(String fileName)
	throws Exception
    {

	// get the realname (aut.getName is not sufficient)
	File fil = new File(fileName);	
	autname = fil.getName();
	int pos = autname.indexOf('.');
	if(pos != -1) autname = autname.substring(0,pos);

	serialize(new PrintWriter(new FileWriter(fileName)));
    }


    private Allocation getAllocation(String event)
	throws Exception
    {
	// format : ("Book" | "Unbook") '(' resource ',' owner ')'

	int first = event.indexOf('(');
	if(first < 1) throw new Exception("bad allocation event (type): " + event); // -1 (not found) or 0 (no type desc)
	
	String type = event.substring(0, first);	

	// get type
	boolean btype = false;
	if(type.equals("Book"))            btype = true;  // <-- book event
	else if(type.equals("Unbook"))     btype = false; // <-- unbook event
	else throw new Exception("bad allocation event (type=" + type + ") : " + event);

	
	
	int second = event.indexOf(',', first);
	
	if(second <= first) throw new Exception("bad allocation event (res): " + event);
	
	String res = event.substring(first +1, second);

	int third = event.indexOf(')', second);
	if(third <= second)  throw new Exception("bad allocation event (owner): " + event);

	String owner = event.substring(second +1, third);
	
	return new Allocation(owner, res,btype);
    }
}

// -- ARASH
