package org.supremica.util.BDD;

import java.util.*;
import org.supremica.automata.*;

public class Builder {
    private org.supremica.automata.Automata s_automata;
    private Automata automata;
    private BDDAutomata bddautomata = null;
    
    private int convertType(AutomatonType t) {
	String name = t.toString().toLowerCase();
	
	if (name == null) {
	    return Automaton.TYPE_UNKNOWN;
	}

	if (name.equals("plant"))
	    {
		return Automaton.TYPE_PLANT;
	    }


	if (name.equals("specification"))
	    {
		return Automaton.TYPE_SPEC;
	    }

	if (name.equals("supervisor"))
	    {
		return Automaton.TYPE_SUPERVISOR;
	    }

	return Automaton.TYPE_UNKNOWN;
    }

    public Builder(org.supremica.automata.Automata s_automata)
    {
	this.s_automata = s_automata;
	automata = new Automata();

	// build the automata
	for (Iterator it = s_automata.iterator(); it.hasNext(); )
	    {
		org.supremica.automata.Automaton s_a = (org.supremica.automata.Automaton) it.next();

		automata.createAutomaton(s_a.getName());

		Automaton a = automata.getCurrent();

		a.setType(convertType(s_a.getType()));

		// insert events

		// insert events
		for(EventIterator ei = s_a.eventIterator(); ei.hasNext(); ) {
		    LabeledEvent le = (LabeledEvent) ei.next();				
		    String id = le.getLabel(); // le.getId() has DEFAULT ACCESS, why??
		    String label = le.getLabel();
		    if(label == null) label = id;
		    boolean co = le.isControllable();
		    boolean p  = le.isPrioritized();
		    a.addEvent(label, id, co, p);
		}

		// Insert states:
		for (StateIterator sit = s_a.stateIterator(); sit.hasNext(); )
		    {
			org.supremica.automata.State s_st = (org.supremica.automata.State) sit.next();
			String name = s_st.getName();
			String name_id = s_st.getId();

			if ((name == null) || (name.length() == 0))
			    {
				name = name_id;
			    }
			else if ((name_id == null) || (name_id.length() == 0))
			    {
				name_id = name;    // just for fun :)
			    }

			boolean initial = s_st.isInitial();
			boolean marked = s_st.isAccepting();
			boolean forbidden = s_st.isForbidden();

			a.addState(name, name_id, initial, marked, forbidden);

			// Insert ARCS
			for (EventIterator eit = s_a.outgoingEventsIterator(s_st);
			     eit.hasNext(); )
			    {
				LabeledEvent le = (LabeledEvent) eit.next();
				org.supremica.automata.State s_to = s_st.nextState(le);
				String e_name = le.getLabel();    // or getID() ???
				String to_name = s_to.getName();

				a.addArc(e_name, name, to_name);
			    }
		    }

		// Close automaton:
		a.close();
	    }

	// close automata
	automata.close();
    }
    
    public BDDAutomata getBDDAutomata() {
	if (bddautomata == null) {
	    bddautomata = new BDDAutomata(automata);
	}
	
	return bddautomata;
    }
}
