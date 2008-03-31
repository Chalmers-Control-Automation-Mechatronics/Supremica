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
	private int safe_states;
	private HashMap<Automaton, BDDAutomaton> supremicaAut2BDDAutMap;
	private HashMap<String, org.supremica.util.BDD.State> supremicaState2BDDStateMap;
	private org.supremica.util.BDD.State[] state_vector;
	private Map<String, String> uniqueNameOriginalNameMap;

	public OnlineBDDSupervisor(BDDAutomata ba, int safe_states)
		throws Exception
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
		catch (Exception exx)
		{
			cleanup();

			throw exx;
		}
	}

	private void createUniqueNames() {
		uniqueNameOriginalNameMap = new HashMap<String, String>();
		for(BDDAutomaton aut: ba.getAutomataVector()) {
			Set<State> states = ((Automaton) aut.getModel().getSupremicaModel()).getStateSet();
			org.supremica.util.BDD.State[] BDDStates = aut.getStates();
			for(State s: states) {
				String newName = aut.getName() + s.getName();
				uniqueNameOriginalNameMap.put(newName, s.getName());
				s.setName(newName);
			}
			for(org.supremica.util.BDD.State s: BDDStates) {
				String newName = aut.getName() + s.name_id;
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
		throws Exception
	{
		supremicaAut2BDDAutMap = 
			new HashMap<Automaton, BDDAutomaton>();
		supremicaState2BDDStateMap = 
			new HashMap<String, org.supremica.util.BDD.State>();

		BDDAutomaton[] bddAutomataArray = ba.getAutomataVector();

		for (int i = 0; i < bddAutomataArray.length; i++)
		{
			bddAutomataArray[i].index = i;

			org.supremica.util.BDD.Automaton supremicaBddAut =
				bddAutomataArray[i].getModel();
			
			org.supremica.automata.Automaton supremicaAut =
				(org.supremica.automata.Automaton) 
				supremicaBddAut.getSupremicaModel();

			supremicaAut2BDDAutMap.put(supremicaAut, bddAutomataArray[i]);

			org.supremica.automata.StateSet states = supremicaAut.getStateSet();
			org.supremica.util.BDD.StateSet bddStates = supremicaBddAut.getStates();

			for (Iterator<org.supremica.automata.State> stateIterator = states.iterator(); stateIterator.hasNext(); )
			{
				org.supremica.automata.State state = stateIterator.next();
				org.supremica.util.BDD.State bddState = bddStates.getByName(state.getName());

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
	public void setPartialState(org.supremica.automata.Automaton owner, org.supremica.automata.State state)
	{
		org.supremica.util.BDD.BDDAutomaton bddAutomaton = (org.supremica.util.BDD.BDDAutomaton) supremicaAut2BDDAutMap.get(owner);

		if (bddAutomaton != null)
		{
			if (state != null)
			{
				org.supremica.util.BDD.State bddState = 
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
		int bdd_state = computeStateBDD();
		int tmp = ba.restrict(safe_states, bdd_state);
		boolean ret = (tmp != ba.getZero());

		ba.deref(tmp);
		ba.deref(bdd_state);

		return ret;
	}

	//Create an automaton object representing this supervisor
	public Automaton createAutomaton() {
		Set<org.supremica.automata.Automaton> automataSet = supremicaAut2BDDAutMap.keySet();
		org.supremica.automata.Automaton[] automata = 
			(org.supremica.automata.Automaton[]) automataSet.toArray(
					new org.supremica.automata.Automaton[0]);
		int nAutomata = automata.length;
		Automaton supervisor = new Automaton();
		supervisor.setType(AutomatonType.SUPERVISOR);
		String supervisorName = "sup(" + automata[0].getName();
		for(int i = 1; i < automata.length; i++) {
			supervisorName += Config.SYNC_AUTOMATON_NAME_SEPARATOR.get() + automata[i].getName();
		}
		supervisorName += ")";
		//supervisor.setName(supervisorName);
		supervisor.setComment(supervisorName);
		
		String stateName = "";
		//the frontier set for the graph traversal
		Queue<org.supremica.automata.State[]> frontierSet = 
			new LinkedList<org.supremica.automata.State[]>();
		//the set of states visited during traversal
		Set<org.supremica.automata.State> visitedSet = 
			new HashSet<org.supremica.automata.State>();
		//mapping events to arcs (transitions)
		Map<LabeledEvent, Set<org.supremica.automata.Arc>> eventArcSetMap = 
			new HashMap<LabeledEvent, Set<org.supremica.automata.Arc>>();
		//mapping state vectors to concatenated states
		HashMap<org.supremica.automata.State[], org.supremica.automata.State> stateStateConcat = 
			new HashMap<org.supremica.automata.State[], org.supremica.automata.State>();
		
		
		//create initial state
		org.supremica.automata.State[] initialState =
			new org.supremica.automata.State[nAutomata];
		for(int i = 0; i < automata.length; i++) {
			org.supremica.automata.Automaton aut = automata[i];
			setPartialState(aut, aut.getInitialState());
			initialState[i] = aut.getInitialState();
		}
		if(!isStateSafe()) {
			return supervisor;
		} else {
			org.supremica.automata.State initial = 
				concatenateState(initialState);
			initial.setInitial(true);
			supervisor.addState(initial);
			stateStateConcat.put(initialState, initial);
			frontierSet.add(initialState);
		}
		
		//traverse automata
                int counter = 0;
		while(!frontierSet.isEmpty()) {
                        counter++;
			//remove a state from the frontier set
			org.supremica.automata.State[] currentState = frontierSet.remove();
			org.supremica.automata.State[] targetState = 
				new org.supremica.automata.State[nAutomata];
			
			org.supremica.automata.State currentStateConcat =
				stateStateConcat.get(currentState);
			
			//add to set of visited state
			visitedSet.add(currentStateConcat);
			
			//get outgoing arcs from current state
			List<Set<org.supremica.automata.Arc>> arcs = 
				new LinkedList<Set<org.supremica.automata.Arc>>();
			
			for(int i = 0; i < currentState.length; i++) {
				arcs.add(i, currentState[i].getOutgoingArcs());
			}
			
			//build event -> arcSet map
			eventArcSetMap.clear();
			for(int i = 0; i < currentState.length; i++) {
				for(org.supremica.automata.Arc arc: arcs.get(i)) {
					LabeledEvent e = arc.getEvent();
					if(eventArcSetMap.containsKey(e)) {
						eventArcSetMap.get(e).add(arc);
					} else {
						Set<org.supremica.automata.Arc> newSet = 
							new HashSet<org.supremica.automata.Arc>();
						newSet.add(arc);
						eventArcSetMap.put(e, newSet);
					}
				}
			}
			
			//remove blocked events from the mapping
			Set<LabeledEvent> blocked = new HashSet<LabeledEvent>();
			outer:
			for(LabeledEvent e: eventArcSetMap.keySet()) {
				automata:
				for(int i = 0; i < automata.length; i++) {
					if(automata[i].getAlphabet().contains(e)) {
						for(org.supremica.automata.Arc arc: currentState[i].getOutgoingArcs()) {
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
			for(LabeledEvent e: blocked) {
				eventArcSetMap.remove(e);
			}
			
			//loop through enabled events and examine subsequent states
			for(LabeledEvent e: eventArcSetMap.keySet()) {
				//reset targetState
				for(int i = 0; i < nAutomata; i++) {
					targetState[i] = currentState[i];
					setPartialState(automata[i], currentState[i]);
				}
				
				//get the arcs for this event
				Set<org.supremica.automata.Arc> partialArcs = eventArcSetMap.get(e);
				if(partialArcs == null || partialArcs.isEmpty()) {
					//This event is blocked. Continue to the next event.
					continue;
				}
				
				//loop through these arcs and update target state
				for(org.supremica.automata.Arc arc: partialArcs) {;
					int index = getStateIndex(currentState, arc.getFromState());
					targetState[index] = arc.getToState();
					setPartialState(automata[index], targetState[index]);
				}
				
				//examine target state
				org.supremica.automata.State targetStateConcat = 
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
					org.supremica.automata.State[] clone = targetState.clone();
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

	private int getStateIndex(State[] state, State fromState) {
		for(int i = 0; i < state.length; i++) {
			if(fromState.getName() == state[i].getName()) {
				return i;
			}
		}
		System.err.println("OnlineBDDSuperVisor.getStateIndex: state not found");
		return -1;
	}

	private State concatenateState(State[] state) {
		String stateName = uniqueNameOriginalNameMap.get(state[0].getName());
		boolean marked = state[0].isAccepting();
		for(int i = 1; i < state.length; i++) {
			//get the original name for this partial state
			String partialName = uniqueNameOriginalNameMap.get(state[i].getName());
			
			//add partial name to concatenation
			stateName += Config.GENERAL_STATE_SEPARATOR.get() + partialName;
			
			//check for marking
			marked = marked && state[i].isAccepting();
		}
		org.supremica.automata.State newState = new org.supremica.automata.State(stateName);
		newState.setAccepting(marked);
		return newState;
	}
}
