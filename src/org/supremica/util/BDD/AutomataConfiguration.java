
package org.supremica.util.BDD;

import java.util.*;


/**
 * Wired configuration of automata. designed to be [almost] monotonic increasing.
 *
 *   "Please don't try to understand this code, it's impossible...."
 *
 * OK, now about the sorted queue thing: we _dont_ use a binary heap since the costs
 * are _dynamic_, as the workset may change! got it?
 *
 */


public class AutomataConfiguration {
	private boolean [] selection, type; /** type: true if member of G1 */
	private boolean [] workset_events, current_events; // original Sigma' events and the extended events
	private boolean include_both; /* both g1 and g2 are choosen */
	private int size1, size2, size_all, selected;
	private int [] local_index; /* index for the automata in G1 or G2, whereever it belongs */

	private BDDAutomaton[] all;
	private BDDAutomaton automaton;


	// our priority queue:
	private int [] work_queue; /* automata to be added */
	private double [] queue_costs; /* cost of elements in the queue */
	private int queue_size;
	private boolean queue_sorted; /* if the elemnts in the queue are sorted  (see costs) */

	private int my_index;


	/**
	 *	 "automatic documentation needed"
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
		this.automaton = null;
		this.current_events = null; // to be allocated

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

		/** priority queue for the automata to be added */
		this.work_queue = new int[count];
		this.queue_costs = new double[count];
		resetQueue();
	}

	private int getIndex(BDDAutomaton a)
	{
		for(int i = 0; i < size_all; i++) if(a == all[i] ) return i;
		return -1; /* failure */
	}

	/**
	 * start over
	 * @param automaton [in] the automaton that is the first element
	 * @param event_care [in]events we are intrested in (only common events are of intrest)
	 * @param workset_events [in] events we are intrested in (only common events are of intrest)
	 * @param result [out] events that the algorithm should be intrested in
	 * return TRUE if we should proceed, FALSE if there is no use since the workset is empty
	 */
	public boolean reset(BDDAutomaton automaton, boolean [] event_care,  boolean [] workset_events)
	{

		selected = 1;
		resetQueue();

		this.workset_events = workset_events;
		this.automaton = automaton;
		my_index = getIndex(automaton);


		// alloc on demand...
		if(current_events == null)	current_events = new boolean[event_care.length];


		// make selection[] valid...
		if(!include_both) {
			// this will make us to ignore the automata in G1:
			for(int i = 0; i < size_all; i++) selection[i] = type[i];
		} else {
			// everythings is clean. both g1 and g2 are avialable
			IndexedSet.empty(selection);
		}
		selection[my_index] = true;



		// events used for dependency analysis!!
		IndexedSet.copy(automaton.getEventCareSet(false), current_events );

		if( IndexedSet.cardinality(workset_events) == 0) {
			if(Options.debug_on)
				Options.out.println("No need to check " + automaton.getName() + ", no relevant events here...");
			return false;
		}

		addIfInteractWith(current_events);
		return true;
	}


	/**
	 * Target events changed (some have been removed).
	 * resets and starts from the start
	 */
	public void removeTargets(boolean [] events, boolean [] remove)
	{
		// remove the changed targets
		IndexedSet.diff(events, remove, events);
		IndexedSet.diff(current_events, remove, current_events);

		// clean what we have on the stack:
		emptyQueue();

		// and put those that are syill relevant back again
		addIfInteractWith(current_events);
	}


	/**
	 * with override, it will see if there are any _new_ automata that
	 * can be added (beside those directly in connection when are already in)
	 */
	public BDDAutomaton addone(Group l1, Group l2, boolean override)
	{
		if(empty())
		{
			if(!override || addIfInteractWithMe(current_events) == 0)
			{
				return null;
			}
		}

		int pop = deleteMin();

		if(Options.debug_on)
			Options.out.println("    -- Adding " + all[pop].getName() + "..." );

		// check if it was from g1 or g2
		if(type[pop])	l1.add( all[pop]);
		else			l2.add( all[pop]);

		return all[pop];

	}


	// -- [ interaction stuff ] --------------------------------------------------

	private void addSelection(int i) {
		if(!selection[i] && i != my_index) {
			selection[i] = true;
			selected ++;
			insert(i);
		}
	}

	/**
	 * same as addIfInteractWithMe(), but does not modify event_careset.
	 * Good _only_for the first round (we get only the directly connected automata).
	 *
	 * @param event_careset [in] events that we considred to be of intreset
	 */
	private void addIfInteractWith(boolean [] event_careset)
	{
		for(int i = 0; i < size_all; i++)
			if(!selection[i] && all[i].interact(event_careset) && i != my_index) {
				addSelection(i);
			}
	}

	/**
	 * Find the automata that are intrecting with event_careset (a subset of Sigma).
	 * if not already added, it will add them to the queue.
	 *
	 * @param event_careset [in] events that we considred to be of intreset and [out] modified to include new inserted automata (if any)
	 * @returns number of new automata with dependency found
	 */
	private  int addIfInteractWithMe(boolean [] event_careset)
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

	// -- [ helper ] --------------------------------------------------------

	/** make queue empty */
	private void resetQueue() {
		queue_size = 0;
		queue_sorted = true; /* its empty anyway */
	}

	/** resets queue, but also undoes its members changes to selected and selection[] */
	private void emptyQueue()
	{
		for(int i = 0; i < queue_size; i++)
			selection[ work_queue[i] ] = false;

		selected -= queue_size;

		resetQueue();
	}

	/** returtns the min member in the queue, assuming its  not empty (NOT CHECKED!) */
	private int deleteMin() {
		if(!queue_sorted) sort_queue();
		queue_size--;
		return work_queue[queue_size];
	}

	/** insert a new element */
	private void insert(int i) {
		work_queue[queue_size] = i;
		queue_size++;
		queue_sorted = false;
	}

	/** queue empty ? */
	private boolean empty() { return queue_size == 0; }

	/** queue full ? */
	private boolean full() { return queue_size == size_all;  }


	// -- [ queue ordering heuristics ] ------------------------------------

	private void sort_queue() {

		// ------------------------ first compute the weights
		// **********************************************************
		// ********************* THE ORDERING HERUSITIC STARTS HERE
		int max = 0;
		for(int i = 0; i < queue_size; i++) {
			BDDAutomaton automaton = all[ work_queue[i] ];
			int tmp        = automaton.arcOverlapCount( workset_events );
			int total_arcs =automaton.getNumArcs();

			if(total_arcs == 0) // no arcs???
				queue_costs[i] = 0;
			else
				queue_costs[i] =  ((double) tmp) / ((double) total_arcs);
		}

		// dont like divide by zero!
		double maxd = max < 1 ? 1.0 : (double) max;

		// invert
		for(int i = 0; i < queue_size; i++) {
			queue_costs[i] /= maxd;
		}


		// ************************** END OF ORDERING HERUSITIC
		// **********************************************************

		// ------------------------ now sort the queue

		QuickSort.sort(work_queue, queue_costs, queue_size, false);


/*
		// DEBUG:
		Options.out.println("***************** STUFF SORTED ********************");
		for(int i = 0; i < queue_size; i++)
			Options.out.println("--> " + queue_costs[i] + "   " + all[ work_queue[i] ].getName() );
*/


		queue_sorted = true;
	}

	// -- [ misc. ] --------------------------------------------------------
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