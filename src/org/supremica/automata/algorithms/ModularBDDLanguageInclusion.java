
package org.supremica.automata.algorithms;


import org.supremica.automata.*;
import org.supremica.util.BDD.*;

import java.util.*;

/**
 * ModularBDDLanguageInclusion, for verification with BDDs
 *
 */

public class ModularBDDLanguageInclusion {
    private AutomataSynchronizerHelper.HelperData hd;
    private org.supremica.automata.Automata theAutomata;

	private boolean [] considred_events, events;
    private BDDAutomata ba = null;
    private BDDAutomaton [] all = null;
  	private Group L1 = null;
  	private Group L2 = null;

  	private Group work1 = null, work2 = null;




    /**
     * creates a verification object for LANGUAGE INCLUSION test.
     * depeding one the type of algorithm used, this call might take a while
     * <b>DONT FORGET TO CALL cleanup() AFTERWARDS!!!</b>
     * @see cleanup()
     */
    public ModularBDDLanguageInclusion(org.supremica.automata.Automata selected,
			       org.supremica.automata.Automata unselected,
			       AutomataSynchronizerHelper.HelperData hd,
			       boolean controllaibilty_test)
	throws Exception
    {
		this.hd     = hd;
		theAutomata = new org.supremica.automata.Automata();
		theAutomata.addAutomata(selected);
		theAutomata.addAutomata(unselected);


		try {
			Builder bu = new Builder(theAutomata);
			ba = bu.getBDDAutomata();
			all = ba.getAutomataVector();

			L1 = new Group(ba, all, new AutomatonMembership(selected), "Selected");
			L2 = new Group(ba, all, new AutomatonMembership(unselected), "Unselected");

			// get our working sets
			work1 = new Group(ba, all.length, "work1");
			work2 = new Group(ba, all.length, "work2");

			// tell the Supervisor classes they shouldnt clean them up when they are done:
			work1.setCleanup(false);
			work2.setCleanup(false);

			// get the intersection of the considred events
			// *** I DONT KNOW IF THIS IS CORRECT!! ***
			/* must duplicate since we will change its value just velow*/
			considred_events = Util.duplicate(L2.getEventCareSet(controllaibilty_test));

			boolean [] tmp = L1.getEventCareSet(controllaibilty_test);
			for(int i = 0; i < tmp.length; i++) considred_events[i] &= tmp[i];

			/* temporary vector for the current considred events in our current automata set*/
			events = new boolean[considred_events.length];


		} catch(Exception pass) {
			cleanup();
			throw pass;
		}
    }

    /**
     * C++ style destructor.
     * <b>This function MUST be called before creating any new AutomataBDDVerifier obejcts</b>
     *
     */
    public void cleanup() {
		if(work1 != null) work1.cleanup();
		if(work2 != null) work2.cleanup();
		if(L1 != null) L1.cleanup();
		if(L2 != null) L2.cleanup();
		if(ba != null) ba.cleanup();
    }




   /**
     * Modular language inclusion check
     *
     * @return TRUE if the system a1 in a1
     */
    public boolean passLanguageInclusion() {


		BDDAutomaton[] l1 = L1.getMembers();
		int count = L1.getSize();


		boolean result = true;
		AutomataConfiguration ac = new AutomataConfiguration (L1, L2);
		for(int i = 0; i < count; i++)
		{
			BDDAutomaton k =  l1[i];

			result &= check(k, ac);
			if(!result)
			{
				break;
			}
		}

		return result;
    }

	/**
	 * Check if languake of K is included in the language of the rest of automata?
	 */
    private boolean check(BDDAutomaton k, AutomataConfiguration ac)
    {

		// after this, events will hold the events in k that are considred.
		// ac should mark the plants/specs with connections to these events

		ac.reset(k, considred_events, events);
		System.out.println("Verifiying " + ac.toString() );

		// check if L(w1) \Sigma \cap L(w2) \subseteq L(w1)

		// start with w1 = { k } ...
		work1.empty();
		work1.add(k);

		// and w2 = \emptyset, that is L(w2) = \Sigma^* ??
		work2.empty();



		while(ac.addone(events, work1, work2, true)) {

			Supervisor sup = null;
			try {
				sup = SupervisorFactory.createSupervisor(ba, work1, work2);

				System.out.println("Checking if " + work2.toString() + " subseteq " + work1.toString() );

				int states = sup.computeReachableLanguageDifference();
				boolean ret = (states == ba.getZero());
				sup.cleanup();
				if(ret) return true;
			} catch(Exception exx) {
				exx.printStackTrace();
				if(sup != null) sup.cleanup();
				return false;
			}

		}

		return false;

		// temp
		// while(ac.addIfInteractWithMe(k_careset) > 0 )
		//	;

	}

}




// TODO: to do this right, we need a priority queue instead of a stack
class AutomataConfiguration {
	private boolean [] selection, type; /* type: true if member of G1 */
	private int size1, size2, size_all, selected;
	private int [] local_index; /* index for the automata in G1 or G2, whereever it belongs */
	private int [] work_stack; /* automata to be added */
	private int stack_top;
	private BDDAutomaton[] all;
	private BDDAutomaton automaton;


	private int my_index;

	public AutomataConfiguration(Group g1, Group g2)
	{
		int size1 = g1.getSize();
		int size2 = g2.getSize();

		size_all = size1 + size2;


		selection = new boolean[size_all];
		type = new boolean[size_all];
		local_index = new int[size_all];
		all = new BDDAutomaton[size_all];

		int count = 0;

		// insert g1
		BDDAutomaton[] tmp = g1.getMembers();
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
		for(int i = 0; i < size_all; i++) selection[i] = false;

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
	public boolean addone(boolean [] events, Group l1, Group l2, boolean override)
	{
		if(stack_top == 0)
		{
			if(override || addIfInteractWithMe(events) == 0)
			{
				return false;
			}
		}
		int pop = work_stack[--stack_top];

		// System.out.println("Adding " + all[pop].getName() + " to " + automaton.getName() );

		// check if it was from g1 or g2
		if(type[pop])	l1.add( all[pop]);
		else			l2.add( all[pop]);

		return true;

	}

	public void addSelection(int i) {
		if(!selection[i] && i != my_index) {
			selection[i] = true;
			// System.out.println("INSERTING " + all[i].getName() + " at position " + stack_top);
			work_stack[stack_top++] = i;
			selected ++;
		}
	}

/*
	public void removeSelection(int i) {
		if(selection[i]) {
			selection[i] = false;
			selected --;
		}
	}
*/

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