package org.supremica.util.BDD;

import java.util.*;
import org.supremica.automata.*;

public class Builder
{
	private org.supremica.automata.Automata s_automata;
	private Automata automata;
	private BDDAutomata bddautomata = null;

	private int convertType(AutomatonType t)
	{
		String name = t.toString().toLowerCase();

		if (name == null)
		{
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

		// XXX: this is really playing with fire. we dont know if interface is a plant or spec
		if (name.equals("interface"))
		{

			// return Automaton.TYPE_SUPERVISOR;
			return Automaton.TYPE_PLANT;
		}

		return Automaton.TYPE_UNKNOWN;
	}

	public Builder(org.supremica.automata.Automata s_automata)
		throws BDDException
	{
		this(s_automata, null);
	}

	public Builder(org.supremica.automata.Automata s_automata, org.supremica.automata.Alphabet alphabet)
		throws BDDException
	{
		this.s_automata = s_automata;
		automata = new Automata();

		// build the automata
		for (Iterator it = s_automata.iterator(); it.hasNext(); )
		{
			org.supremica.automata.Automaton s_a = (org.supremica.automata.Automaton) it.next();

			automata.createAutomaton(s_a, s_a.getName());

			Automaton a = automata.getCurrent();

			a.setType(convertType(s_a.getType()));

			// insert events
			for (Iterator<LabeledEvent> ei = s_a.eventIterator(); ei.hasNext(); )
			{
				LabeledEvent le = ei.next();

				if ((alphabet != null) &&!alphabet.contains(le))
				{
					continue;
				}

				String id = le.getLabel();    // le.getId() has DEFAULT ACCESS, why??
				String label = le.getLabel();

				if (label == null)
				{
					label = id;
				}

				boolean co = le.isControllable();
				boolean p = le.isPrioritized();

				a.addEvent(label, id, co, p);
			}

			// Insert states:
			for (Iterator<org.supremica.automata.State> sit = s_a.stateIterator(); sit.hasNext(); )
			{
				org.supremica.automata.State s_st = sit.next();
				String name = s_st.getName();

				//String name_id = s_st.getId(); // Sorry, this is private info!
				String name_id = name;

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

				// add arcs
				for(Iterator<org.supremica.automata.Arc> ait = s_st.outgoingArcsIterator(); 
					ait.hasNext(); )
				{
					org.supremica.automata.Arc arc = ait.next();
					org.supremica.automata.State s_to = arc.getToState();
					org.supremica.automata.LabeledEvent event = arc.getEvent();

					String e_name = event.getLabel();    // or getID() ???
					String to_name = s_to.getName();

					a.addArc(e_name, name, to_name);
				}


			}

			// Close automaton:
			a.close();
		}

		// close automata
		automata.close();

		// DEBUG:
		// automata.dump(Options.out);
	}

	public BDDAutomata getBDDAutomata()
		throws BDDException
	{
		if (bddautomata == null)
		{
			if (BDDAutomata.BDDPackageIsBusy())
			{
				throw new BDDException("The BDD packages is used by another task!");
			}

			bddautomata = new BDDAutomata(automata);
		}

		return bddautomata;
	}
}
