
package org.supremica.util.BDD;

import java.util.*;
import org.supremica.util.BDD.heuristics.*;



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
	private boolean [] selection, type; /** type: true if member of G1 (i.e. 'spec') */
	private boolean [] workset_events, current_events, tmp_events; // original Sigma' events and the extended events
	private boolean include_both; /* both g1 and g2 are choosen */
	private int size1, size2, size_all, selected;
	private int [] local_index; /* index for the automata in G1 or G2, whereever it belongs */
	private int [] workset_events_to_be_used_in_plant; //  >= 0 if the events has been used in the plant or we dont care about that events

	private BDDAutomaton[] all;
	private BDDAutomaton automaton;


	// our priority queue:
	private int [] work_queue; /* automata to be added */
	private double [] queue_costs; /* cost of elements in the queue */
	private int queue_size;
	private boolean queue_sorted; /* if the elemnts in the queue are sorted  (see costs) */
	private boolean last_was_plant; /* the last automaton was a 'plant' ?? */
	private int my_index, max_size;

	// and the quese selection herustic:
	private AutomatonSelectionHeuristic heuristic;


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
		this.current_events = null; // to be allocated, see reset()

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

		heuristic = AutomatonSelectionHeuristicFactory.createInstance(all, work_queue, queue_costs);
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
	 * @param current_events_usage [in] number of times this events is still used in the "rest" partition
	 * return TRUE if we should proceed, FALSE if there is no use since the workset is empty
	 */
	public boolean reset(BDDAutomaton automaton, boolean [] event_care,  boolean [] workset_events,
						int [] current_events_usage)
	{

		selected = 1;
		resetQueue();

		last_was_plant = false; // nop, automaton must be a spec (see incremental algos to understand)

		this.workset_events = workset_events;
		this.automaton = automaton;
		my_index = getIndex(automaton);


		// alloc on demand...
		if(current_events == null) {
			current_events = new boolean[event_care.length];
			tmp_events = new boolean[event_care.length];
			workset_events_to_be_used_in_plant = new int[event_care.length];
		}



		// make selection[] valid...
		if(!include_both) {
			// this will make us to ignore the automata in G1:
			for(int i = 0; i < size_all; i++) selection[i] = type[i];

			max_size = size_all - IndexedSet.cardinality( selection) + 1;
		} else {
			// everythings is clean. both g1 and g2 are avialable
			IndexedSet.empty(selection);
			max_size = size_all;
		}
		selection[my_index] = true;



		// events used for dependency analysis!!
		IndexedSet.copy(automaton.getEventCareSet(false), current_events );

		if( IndexedSet.cardinality(workset_events) == 0) {
			if(Options.debug_on)
				Options.out.println("No need to check " + automaton.getName() + ", no relevant events here...");
			return false;
		}

		// copy the events that should but have not been used in the plants yet:
		for(int i = 0; i < workset_events.length; i++)
			workset_events_to_be_used_in_plant[i] = workset_events[i] ? 1 : 0;

		addIfInteractWithMe(current_events);

		heuristic.reset(automaton, current_events, workset_events, current_events_usage);
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
		addIfInteractWithMe(current_events);
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
				// Options.out.println("No more automata to add");
				return null;
			}
		}

		int pop = deleteMin();

		if(Options.debug_on)
			Options.out.println("   -- Adding automaton " + all[pop].getName() + " (taken from the queue)..." );

		// check if it was from g1 or g2
		if(type[pop]) {	/** "spec" */
			l1.add( all[pop]);
			last_was_plant  = false;
		} else {	/** "plant" */
			l2.add( all[pop]);
			all[pop].removeEventUsage(workset_events_to_be_used_in_plant);
			last_was_plant  = true;
		}

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
		IndexedSet.copy(event_careset, tmp_events);
		for(int i = 0; i < size_all; i++)
		{
			if(selection[i])
			{
				// NOT WORKING (same results but more computation)
				// IndexedSet.copy(event_careset, tmp_events); // we do this copying to avoid adding recursive dependencies
				// all[i].addEventCareSet(tmp_events, false /* all events*/ );
				// IndexedSet.add(event_careset, tmp_events);

				all[i].addEventCareSet(tmp_events, event_careset, false /* all events*/ );
			}
		}


		int count = 0;
		for(int i = 0; i < size_all; i++)
		{
			if(!selection[i] && i != my_index && all[i].interact(event_careset))
			{
				if(Options.debug_on)
					Options.out.println("   ++ Putting automaton " + all[i].getName() + " on the queue (directly dependent)");

				addSelection(i);
				count ++;
			}
		}
		return count;

	}

	// -- [ helper ] --------------------------------------------------------


	/**
	 * Returns true if the plants toghether include all the considred (uncontrollable for C, all otherwise).
	 * If this is not true, an uncontrollable ARC might be due to a self-loop in the Plants!!
	 * (the real arc not added yet, all other plants are those that just 'agree' by using self-loops)
	 */
	public boolean plantIncludesAllConsidredEvents() {
		int len = workset_events_to_be_used_in_plant.length;
		for(int i = 0; i < len; i++)
			if(workset_events_to_be_used_in_plant[i] > 0)
			return false;

		return true;
	}
	/**
	 * returns false if we can guarantee that there are not more automata to be added.
	 * a positive answer does not mean that we _will_ have an automaton to added however,
	 * since it might be no automaton with the needed dependency left.
	 */
	public boolean moreToGo() {
		return ! ((selected == max_size) && queue_size == 0);
	}

	public boolean lastAutomatonWasPlant() {
		return last_was_plant;
	}

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

	/** retruns the min member in the queue, assuming its  not empty (NOT CHECKED!) */
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
		heuristic.choose(queue_size /*  , workset_events */);
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