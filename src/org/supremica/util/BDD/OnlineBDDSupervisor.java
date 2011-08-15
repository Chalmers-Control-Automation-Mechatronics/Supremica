package org.supremica.util.BDD;

import org.supremica.automata.*;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.properties.Config;

import java.util.*;
import java.util.List;


public class OnlineBDDSupervisor
{
	private BDDAutomata ba;
	private final int safe_states;
	private HashMap<Automaton, BDDAutomaton> supremicaAut2BDDAutMap;
	private HashMap<String, org.supremica.util.BDD.State> supremicaState2BDDStateMap;
	private final org.supremica.util.BDD.State[] state_vector;
	private Map<String, String> uniqueNameOriginalNameMap;

	public OnlineBDDSupervisor(final BDDAutomata ba, final int safe_states)
	{
		this.ba = ba;
		this.safe_states = safe_states;
		this.state_vector = new org.supremica.util.BDD.State[ba.getSize()];

		for (int i = 0; i < state_vector.length; i++)
		{
			state_vector[i] = null;
		}

		//create unique names to be used with the BDD synthesis
		createUniqueNames();

		try
		{
			setup_mapping();
		}
		catch (final Exception exx)
		{
			cleanup();

			throw new RuntimeException(exx);
		}
	}

	private void createUniqueNames() {
		uniqueNameOriginalNameMap = new HashMap<String, String>();
		for(final BDDAutomaton aut: ba.getAutomataVector()) {
			final Set<State> states = ((Automaton) aut.getModel().getSupremicaModel()).getStateSet();
			final org.supremica.util.BDD.State[] BDDStates = aut.getStates();
			for(final State s: states) {
				final String newName = aut.getName() + s.getName();
				uniqueNameOriginalNameMap.put(newName, s.getName());
				s.setName(newName);
			}
			for(final org.supremica.util.BDD.State s: BDDStates) {
				final String newName = aut.getName() + s.name_id;
				s.name_id = newName;
			}
		}
	}

	public void cleanup()
	{
		if (ba != null)
		{
			ba.cleanup();

			ba = null;
		}
	}

	// ---------------------------------------------------------------
	private void setup_mapping()
	{
		supremicaAut2BDDAutMap =
			new HashMap<Automaton, BDDAutomaton>();
		supremicaState2BDDStateMap =
			new HashMap<String, org.supremica.util.BDD.State>();

                final BDDAutomaton[] bddAutomataArray = ba.getAutomataVector();

		for (int i = 0; i < bddAutomataArray.length; i++)
		{
			bddAutomataArray[i].index = i;

			final org.supremica.util.BDD.Automaton supremicaBddAut =
				bddAutomataArray[i].getModel();

			final org.supremica.automata.Automaton supremicaAut =
				(org.supremica.automata.Automaton)
				supremicaBddAut.getSupremicaModel();

			supremicaAut2BDDAutMap.put(supremicaAut, bddAutomataArray[i]);

			final org.supremica.automata.StateSet states = supremicaAut.getStateSet();
			final org.supremica.util.BDD.StateSet bddStates = supremicaBddAut.getStates();

			for (final Iterator<org.supremica.automata.State> stateIterator = states.iterator(); stateIterator.hasNext(); )
			{
				final org.supremica.automata.State state = stateIterator.next();
				final org.supremica.util.BDD.State bddState = bddStates.getByName(state.getName());

				supremicaState2BDDStateMap.put(state.getName(), bddState);
			}
		}
	}

	private int computeStateBDD()
	{
		int ret = ba.ref(ba.getOne());

		for (int i = 0; i < state_vector.length; i++)
		{
			if (state_vector[i] != null)
			{
				ret = ba.andTo(ret, state_vector[i].bdd_s);
			}
		}

		return ret;
	}

	public String get_state_string()
	{
		String sb = new String();

		sb += "<";

		for (int i = 0; i < state_vector.length; i++)
		{
			if (state_vector[i] != null)
			{
				sb += " ";
				sb += state_vector[i].name;
			}
		}

		sb += " >";

		return sb;
	}

	// -------------------------------------------------------------------------------
	public void setPartialState(final org.supremica.automata.Automaton owner, final org.supremica.automata.State state)
	{
		final org.supremica.util.BDD.BDDAutomaton bddAutomaton = (org.supremica.util.BDD.BDDAutomaton) supremicaAut2BDDAutMap.get(owner);

		if (bddAutomaton != null)
		{
			if (state != null)
			{
				final org.supremica.util.BDD.State bddState =
					(org.supremica.util.BDD.State) supremicaState2BDDStateMap.get(state.getName());

				state_vector[bddAutomaton.index] = bddState;
			}
			else
			{
				state_vector[bddAutomaton.index] = null;
			}
		}
	}

	/**
	 * THIS IS NOT EFFICIENT!
	 * WE NEED A MORE EFFICIENT WAY TO check if bdd_state \in safe_states
	 */
	public boolean isStateSafe()
	{
		final int bdd_state = computeStateBDD();
		final int tmp = ba.restrict(safe_states, bdd_state);
		final boolean ret = (tmp != ba.getZero());

		ba.deref(tmp);
		ba.deref(bdd_state);

		return ret;
	}

	//Create an automaton object representing this supervisor
	public Automaton createAutomaton() {
		final Set<org.supremica.automata.Automaton> automataSet = supremicaAut2BDDAutMap.keySet();
		final org.supremica.automata.Automaton[] automata =
			(org.supremica.automata.Automaton[]) automataSet.toArray(
					new org.supremica.automata.Automaton[0]);
		final int nAutomata = automata.length;
		String supervisorName = "sup(" + automata[0].getName();
		for(int i = 1; i < automata.length; i++) {
			supervisorName += Config.SYNC_AUTOMATON_NAME_SEPARATOR.get() + automata[i].getName();
		}
		supervisorName += ")";
		final Automaton supervisor = new Automaton(supervisorName);
		//supervisor.setName(supervisorName);
		supervisor.setType(AutomatonType.SUPERVISOR);
		supervisor.setComment(supervisorName);

		//the frontier set for the graph traversal
		final Queue<org.supremica.automata.State[]> frontierSet =
			new LinkedList<org.supremica.automata.State[]>();
		//the set of states visited during traversal
		final Set<org.supremica.automata.State> visitedSet =
			new HashSet<org.supremica.automata.State>();
		//mapping events to arcs (transitions)
		final Map<LabeledEvent, Set<org.supremica.automata.Arc>> eventArcSetMap =
			new HashMap<LabeledEvent, Set<org.supremica.automata.Arc>>();
		//mapping state vectors to concatenated states
		final HashMap<org.supremica.automata.State[], org.supremica.automata.State> stateStateConcat =
			new HashMap<org.supremica.automata.State[], org.supremica.automata.State>();


		//create initial state
		final org.supremica.automata.State[] initialState =
			new org.supremica.automata.State[nAutomata];
		for(int i = 0; i < automata.length; i++) {
			final org.supremica.automata.Automaton aut = automata[i];
			setPartialState(aut, aut.getInitialState());
			initialState[i] = aut.getInitialState();
		}
		if(!isStateSafe()) {
			return supervisor;
		} else {
			final org.supremica.automata.State initial =
				concatenateState(initialState);
			initial.setInitial(true);
			supervisor.addState(initial);
			stateStateConcat.put(initialState, initial);
			frontierSet.add(initialState);
		}

		//traverse automata
		while(!frontierSet.isEmpty()) {
			//remove a state from the frontier set
			final org.supremica.automata.State[] currentState = frontierSet.remove();
			final org.supremica.automata.State[] targetState =
				new org.supremica.automata.State[nAutomata];

			final org.supremica.automata.State currentStateConcat =
				stateStateConcat.get(currentState);

			//add to set of visited state
			visitedSet.add(currentStateConcat);

			//get outgoing arcs from current state
			final List<Set<org.supremica.automata.Arc>> arcs =
				new LinkedList<Set<org.supremica.automata.Arc>>();

			for(int i = 0; i < currentState.length; i++) {
				arcs.add(i, currentState[i].getOutgoingArcs());
			}

			//build event -> arcSet map
			eventArcSetMap.clear();
			for(int i = 0; i < currentState.length; i++) {
				for(final org.supremica.automata.Arc arc: arcs.get(i)) {
					final LabeledEvent e = arc.getEvent();
					if(eventArcSetMap.containsKey(e)) {
						eventArcSetMap.get(e).add(arc);
					} else {
						final Set<org.supremica.automata.Arc> newSet =
							new HashSet<org.supremica.automata.Arc>();
						newSet.add(arc);
						eventArcSetMap.put(e, newSet);
					}
				}
			}

			//remove blocked events from the mapping
			final Set<LabeledEvent> blocked = new HashSet<LabeledEvent>();
			outer:
			for(final LabeledEvent e: eventArcSetMap.keySet()) {
				automata:
				for(int i = 0; i < automata.length; i++) {
					if(automata[i].getAlphabet().contains(e)) {
						for(final org.supremica.automata.Arc arc: currentState[i].getOutgoingArcs()) {
							//this automaton has an arc with this event at this state
							//so continue to the next automaton
							if(arc.getEvent().equals(e)) continue automata;
						}
						//no outgoing arc from the current state in this automaton
						//has this event => it is blocked
						blocked.add(e);

						//continue to the next event
						continue outer;
					} else {
						//this automaton does not contain this event meaning it
						//is not blocked. do nothing.
					}
				}
			}
			for(final LabeledEvent e: blocked) {
				eventArcSetMap.remove(e);
			}

			//loop through enabled events and examine subsequent states
			for(final LabeledEvent e: eventArcSetMap.keySet()) {
				//reset targetState
				for(int i = 0; i < nAutomata; i++) {
					targetState[i] = currentState[i];
					setPartialState(automata[i], currentState[i]);
				}

				//get the arcs for this event
				final Set<org.supremica.automata.Arc> partialArcs = eventArcSetMap.get(e);
				if(partialArcs == null || partialArcs.isEmpty()) {
					//This event is blocked. Continue to the next event.
					continue;
				}

				//loop through these arcs and update target state
				for(final org.supremica.automata.Arc arc: partialArcs) {;
					final int index = getStateIndex(currentState, arc.getFromState());
					targetState[index] = arc.getToState();
					setPartialState(automata[index], targetState[index]);
				}

				//examine target state
				final org.supremica.automata.State targetStateConcat =
					concatenateState(targetState);
				if(visitedSet.contains(targetStateConcat)) {
					//this state has already been visited. add arc to supervisor and
					//continue to the next event.
					supervisor.addState(targetStateConcat);
					if(!supervisor.getAlphabet().contains(e)) supervisor.getAlphabet().addEvent(e);
					supervisor.addArc(new org.supremica.automata.Arc
							(currentStateConcat,
							targetStateConcat,
							e));
					continue;
				} else if(!isStateSafe()) {
					//this state is forbidden. continue to the next event.
				} else {
					//new ok state found.
					final org.supremica.automata.State[] clone = targetState.clone();
					stateStateConcat.put(clone, targetStateConcat);
					frontierSet.add(clone);

					supervisor.addState(targetStateConcat);
					if(!supervisor.getAlphabet().contains(e)) supervisor.getAlphabet().addEvent(e);
					supervisor.addArc(new org.supremica.automata.Arc
							(currentStateConcat,
							targetStateConcat,
							e));

				}
			}
		}
		//return result
		return supervisor;
	}

	private int getStateIndex(final State[] state, final State fromState) {
		for(int i = 0; i < state.length; i++) {
			if(fromState.getName() == state[i].getName()) {
				return i;
			}
		}
		System.err.println("OnlineBDDSuperVisor.getStateIndex: state not found");
		return -1;
	}

	private State concatenateState(final State[] state) {
		String stateName = uniqueNameOriginalNameMap.get(state[0].getName());
		boolean marked = state[0].isAccepting();
		for(int i = 1; i < state.length; i++) {
			//get the original name for this partial state
			final String partialName = uniqueNameOriginalNameMap.get(state[i].getName());

			//add partial name to concatenation
			stateName += Config.GENERAL_STATE_SEPARATOR.get() + partialName;

			//check for marking
			marked = marked && state[i].isAccepting();
		}
		final org.supremica.automata.State newState = new org.supremica.automata.State(stateName);
		newState.setAccepting(marked);
		return newState;
	}
}
