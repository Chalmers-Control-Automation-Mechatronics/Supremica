
package org.supremica.util.BDD;

import java.util.*;




// TODO: to do this right, we need a priority queue instead of a stack
public class AutomataConfiguration {
	private boolean [] selection, type; /** type: true if member of G1 */
	private boolean include_both; /* both g1 and g2 are choosen */
	private int size1, size2, size_all, selected;
	private int [] local_index; /* index for the automata in G1 or G2, whereever it belongs */
	private int [] work_stack; /* automata to be added */
	private int stack_top;
	private BDDAutomaton[] all;
	private BDDAutomaton automaton;


	private int my_index;

	/**
	 *
	 * if g1 == null, it is not included (good for language inclusion tests!)
	 *
	 */

	public AutomataConfiguration(Group g1, Group g2, boolean include_both)
	{

		BDDAutomaton[] tmp;
		int count = 0;
		int size1 = g1.getSize();
		int size2 = g2.getSize();
		size_all = size1 + size2;


		this.selection = new boolean[size_all];
		this.type = new boolean[size_all];
		this.local_index = new int[size_all];
		this.all = new BDDAutomaton[size_all];
		this.include_both = include_both;



		// insert g1
		tmp = g1.getMembers();
		for(int i = 0; i < size1; i++)  {
			all[count] = tmp[i];
			type[count] = true;
			selection[count] = false;
			local_index[count] = i;
			count++;
		}

		tmp = g2.getMembers();
		for(int i = 0; i < size2; i++)  {
			all[count] = tmp[i];
			type[count] = false;
			selection[count] = false;
			local_index[count] = i;
			count++;
		}

		this.my_index = -1; /* invalid */

		/** stack for the automata to be added */
		this.work_stack = new int[count];
		this.stack_top = 0;
	}

	private int getIndex(BDDAutomaton a)
	{
		for(int i = 0; i < size_all; i++) if(a == all[i] ) return i;
		return -1; /* failure */
	}

	public void reset(BDDAutomaton automaton, boolean [] event_care, boolean [] result)
	{
		this.my_index = getIndex(automaton);
		this.automaton = automaton;


		this.selected = 1;
		this.stack_top = 0;



		if(!include_both) {
			// this will make us to ignore the automata in G1:
			for(int i = 0; i < size_all; i++) selection[i] = type[i];
		} else {
			// everythings is clean. both g1 and g2 are avialable
			for(int i = 0; i < size_all; i++) selection[i] = false;
		}

		selection[my_index] = true;

		// we only care about events that are in the original care set AND in out alphabet
		boolean [] ret  = automaton.getEventCareSet(false);
		for(int i = 0; i < ret.length; i++) result[i] = ret[i] &  event_care[i];


		addIfInteractWith(result);
	}

	/**
	 * with override, it will see if there are any _new_ automata that
	 * can be added (beside those directly in connection when are already in)
	 */
	public BDDAutomaton addone(boolean [] events, Group l1, Group l2, boolean override)
	{
		if(stack_top == 0)
		{
			if(override || addIfInteractWithMe(events) == 0)
			{
				return null;
			}
		}
		int pop = work_stack[--stack_top];

		// System.out.println("Adding " + all[pop].getName() + " to " + automaton.getName() );

		// check if it was from g1 or g2
		if(type[pop])	l1.add( all[pop]);
		else			l2.add( all[pop]);

		return all[pop];

	}

	public void addSelection(int i) {
		if(!selection[i] && i != my_index) {
			selection[i] = true;
			// System.out.println("INSERTING " + all[i].getName() + " at position " + stack_top);
			work_stack[stack_top++] = i;
			selected ++;
		}
	}

	public void addIfInteractWith(boolean [] event_careset)
	{
		for(int i = 0; i < size_all; i++)
			if(!selection[i] && all[i].interact(event_careset) && i != my_index)
				addSelection(i);
	}
	public int addIfInteractWithMe(boolean [] event_careset)
	{
		// get new care set:
		for(int i = 0; i < size_all; i++)
			if(selection[i])
				all[i].addEventCareSet(event_careset, true /* all events*/ );

		int count = 0;
		for(int i = 0; i < size_all; i++)
		{
			if(!selection[i] && i != my_index && all[i].interact(event_careset))
			{
				addSelection(i);
				count ++;
			}
		}
		return count;

	}

	public boolean empty() { return selected == 0; }
	public boolean full() { return selected == size_all;  }

	public String toString()
	{
		StringBuffer bf = new StringBuffer();
		bf.append(automaton.getName() + ": {");
		for(int i = 0; i < size_all; i++) {
			if(selection[i])
				bf.append( all[i].getName() + " ");
		}

		bf.append("};");
		return bf.toString();
	}

};