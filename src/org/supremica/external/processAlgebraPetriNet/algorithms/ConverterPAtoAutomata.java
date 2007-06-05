package org.supremica.external.processAlgebraPetriNet.algorithms;



import java.util.*;
import org.jdom.*;

public class ConverterPAtoAutomata {

	Document AutomataDoc;
	Element automata = new Element("Automata");

	public ConverterPAtoAutomata() {

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void convertPAtoAutomata(Document PAdoc) {

		AutomataDoc = new Document(automata);
		Element PAroot = PAdoc.getRootElement();
		Element sim_processes = PAroot.getChild("simultaneity");

		// Place all operations not involved in any group.
		List opList = sim_processes.getChildren("Process");
		for(Iterator listOP = opList.iterator(); listOP.hasNext(); )
		{
			Element single_OP_element = (Element) listOP.next();
			String single_OP_id = single_OP_element.getAttributeValue("Id");

			Element restriction = single_OP_element.getChild("Restriction");

			if (restriction != null) {
				placeAutomatas(single_OP_id, restriction, automata);
			} else {
				placeAutomaton(automata, single_OP_id);
			}
		}

		// Place groups
		placeThem(sim_processes, automata);

		InterlockingOperationAutomatas IOA = new InterlockingOperationAutomatas();
		IOA.createInterlockingOperationAutomatas(PAroot, automata);

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void placeThem(Element sim_processes, Element automata) {


		// Alternative groups
		List alternativeList = sim_processes.getChildren("substitution");
		for(Iterator listAlternative = alternativeList.iterator(); listAlternative.hasNext(); )
		{
			int current_automata_state = 0;
			Element alternative_Element = (Element) listAlternative.next();

			// Creates seperate automatas for restriction of a group.
			Element restriction = alternative_Element.getChild("Restriction");

			if (restriction != null) {
				placeGroupAutomatas(alternative_Element, restriction, automata);
			}

			// Place group in the total group
			Element automaton = new Element("Automaton");
			automaton.setAttribute("name", "Alternative Process ");
			automata.addContent(automaton);

			Element events = new Element("Events");
			automaton.addContent(events);

			Element states = new Element("States");
			automaton.addContent(states);

			Element transitions = new Element("Transitions");
			automaton.addContent(transitions);

			Element state = new Element("State");
			state.setAttribute("id", "q" + current_automata_state);
			String toState = "q" + current_automata_state;
			state.setAttribute("initial", "true");
			states.addContent(state);

			current_automata_state = placeAltGroup(alternative_Element, current_automata_state, automaton);

			placeGroups(alternative_Element, automaton, current_automata_state);
		}


		// Sequence groups
		List sequenceList = sim_processes.getChildren("sequence");
		for(Iterator listSequence = sequenceList.iterator(); listSequence.hasNext(); )
		{
			int current_automata_state = 0;
			Element sequence_Element = (Element) listSequence.next();

			// Creates seperate automatas for restriction of a group.
			Element restriction = sequence_Element.getChild("Restriction");

			if (restriction != null) {
				placeGroupAutomatas(sequence_Element, restriction, automata);
			}

			// Place group in the total group
			Element automaton = new Element("Automaton");
			automaton.setAttribute("name", "Sequence Process ");
			automata.addContent(automaton);

			Element events = new Element("Events");
			automaton.addContent(events);

			Element states = new Element("States");
			automaton.addContent(states);

			Element transitions = new Element("Transitions");
			automaton.addContent(transitions);

			Element state = new Element("State");
			state.setAttribute("id", "q" + current_automata_state);
			String toState = "q" + current_automata_state;
			state.setAttribute("initial", "true");
			states.addContent(state);

			current_automata_state = placeSeqGroup(sequence_Element, current_automata_state, automaton);

			placeGroups(sequence_Element, automaton, current_automata_state);
		}

		// Arbitrary groups
		List arbitraryList = sim_processes.getChildren("exclusiveness");
		for(Iterator listArbitrary = arbitraryList.iterator(); listArbitrary.hasNext(); )
		{
			int current_automata_state = 0;
			Element arbitrary_Element = (Element) listArbitrary.next();

			// Creates seperate automatas for restriction of a group.
			Element restriction = arbitrary_Element.getChild("Restriction");

			if (restriction != null) {
				placeGroupAutomatas(arbitrary_Element, restriction, automata);
			}

			// Place group in the total group
			Element automaton = new Element("Automaton");
			automaton.setAttribute("name", "Arbitrary Process ");
			automata.addContent(automaton);

			Element events = new Element("Events");
			automaton.addContent(events);

			Element states = new Element("States");
			automaton.addContent(states);

			Element transitions = new Element("Transitions");
			automaton.addContent(transitions);

			Element state = new Element("State");
			state.setAttribute("id", "q" + current_automata_state);
			String toState = "q" + current_automata_state;
			state.setAttribute("initial", "true");
			states.addContent(state);

			current_automata_state = placeArbGroup(arbitrary_Element, current_automata_state, automaton);

			placeGroups(arbitrary_Element, automaton, current_automata_state);
		}

		// Parallel groups
		List parallelList = sim_processes.getChildren("simultaneity");
		for(Iterator listParallel = parallelList.iterator(); listParallel.hasNext(); )
		{
			int current_automata_state = 0;
			Element parallel_Element = (Element) listParallel.next();

			// Creates seperate automatas for restriction of a group.
			Element restriction = parallel_Element.getChild("Restriction");

			if (restriction != null) {
				placeGroupAutomatas(parallel_Element, restriction, automata);
			}

			// Place group in the total group
			Element automaton = new Element("Automaton");
			automaton.setAttribute("name", "Parallel Process ");
			automata.addContent(automaton);

			Element events = new Element("Events");
			automaton.addContent(events);

			Element states = new Element("States");
			automaton.addContent(states);

			Element transitions = new Element("Transitions");
			automaton.addContent(transitions);

			Element state = new Element("State");
			state.setAttribute("id", "q" + current_automata_state);
			String toState = "q" + current_automata_state;
			state.setAttribute("initial", "true");
			states.addContent(state);

			current_automata_state = placeParGroup(parallel_Element, current_automata_state, automaton);

			placeGroups(parallel_Element, automaton, current_automata_state);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////// placeGroups ////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void placeGroups(Element group_element, Element automaton, int current_automata_state) {

		// Alternative groups
		List alternativeList = group_element.getChildren("substitution");
		for(Iterator listAlternative = alternativeList.iterator(); listAlternative.hasNext(); )
		{
			Element alternative_Element = (Element) listAlternative.next();

			// Creates seperate automatas for restriction of a group.
			Element restriction = alternative_Element.getChild("Restriction");

			if (restriction != null) {
				placeGroupAutomatas(alternative_Element, restriction, automata);
			}

			// Place group in the total group
			current_automata_state = placeAltGroup(alternative_Element, current_automata_state, automaton);
			placeGroups(alternative_Element, automaton, current_automata_state);
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// Sequence groups
		List sequenceList = group_element.getChildren("sequence");
		for(Iterator listSequence = sequenceList.iterator(); listSequence.hasNext(); )
		{
			Element sequence_Element = (Element) listSequence.next();

			// Creates seperate automatas for restriction of a group.
			Element restriction = sequence_Element.getChild("Restriction");

			if (restriction != null) {
				placeGroupAutomatas(sequence_Element, restriction, automata);
			}

			// Place group in the total group
			current_automata_state = placeSeqGroup(sequence_Element, current_automata_state, automaton);
			placeGroups(sequence_Element, automaton, current_automata_state);
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// Arbitrary groups
		List arbitraryList = group_element.getChildren("exclusiveness");
		for(Iterator listArbitrary = arbitraryList.iterator(); listArbitrary.hasNext(); )
		{
			Element arbitrary_Element = (Element) listArbitrary.next();

			// Creates seperate automatas for restriction of a group.
			Element restriction = arbitrary_Element.getChild("Restriction");

			if (restriction != null) {
				placeGroupAutomatas(arbitrary_Element, restriction, automata);
			}

			// Place group in the total group
			current_automata_state = placeArbGroup(arbitrary_Element, current_automata_state, automaton);
			placeGroups(arbitrary_Element, automaton, current_automata_state);
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// Parallel groups
		List parallelList = group_element.getChildren("simultaneity");
		for(Iterator listParallel = parallelList.iterator(); listParallel.hasNext(); )
		{
			Element parallel_Element = (Element) listParallel.next();

			// Creates seperate automatas for restriction of a group.
			Element restriction = parallel_Element.getChild("Restriction");

			if (restriction != null) {
				placeGroupAutomatas(parallel_Element, restriction, automata);
			}

			// Place group in the total group
			current_automata_state = placeParGroup(parallel_Element, current_automata_state, automaton);
			placeGroups(parallel_Element, automaton, current_automata_state);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public int placeAltGroup(Element group_element, int current_automata_state, Element automaton) {

		Element events = automaton.getChild("Events");
		Element states = automaton.getChild("States");
		Element transitions = automaton.getChild("Transitions");

		String fromState = "q" + current_automata_state;
		current_automata_state = current_automata_state + 1;

		Element state = new Element("State");
		state.setAttribute("id", "q" + current_automata_state);
		String toState = "q" + current_automata_state;
		state.setAttribute("initial", "false");
		states.addContent(state);

		List alternativeList = group_element.getChildren("Process");
		for(Iterator listAlternative = alternativeList.iterator(); listAlternative.hasNext(); )
		{
			Element alternative_Element = (Element) listAlternative.next();
			String alternative = alternative_Element.getAttributeValue("Id");

			Element event = new Element("Event");
			event.setAttribute("id", alternative);
			event.setAttribute("label", alternative);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			Element transition = new Element("Transition");
			transition.setAttribute("event", alternative);
			transition.setAttribute("source", fromState);
			transition.setAttribute("dest", toState);
			transitions.addContent(transition);

			// Creates seperate automatas for restriction of a group.
			Element restriction = alternative_Element.getChild("Restriction");

			if (restriction != null) {
				placeAutomatas(alternative, restriction, automata);
			}
		}
		return current_automata_state;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public int placeSeqGroup(Element group_element, int current_automata_state, Element automaton) {

		Element events = automaton.getChild("Events");
		Element states = automaton.getChild("States");
		Element transitions = automaton.getChild("Transitions");

		List sequenceList = group_element.getChildren("Process");
		for(Iterator listSequence = sequenceList.iterator(); listSequence.hasNext(); )
		{
			Element sequence_Element = (Element) listSequence.next();
			String sequence = sequence_Element.getAttributeValue("Id");

			Element event = new Element("Event");
			event.setAttribute("id", sequence);
			event.setAttribute("label", sequence);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			current_automata_state = current_automata_state + 1;

			Element state = new Element("State");
			state.setAttribute("id", "q" + current_automata_state);
			String toState = "q" + current_automata_state;
			state.setAttribute("initial", "false");
			states.addContent(state);

			Element transition = new Element("Transition");
			transition.setAttribute("event", sequence);
			int state_nr = current_automata_state - 1;
			String fromState = "q" + state_nr;
			transition.setAttribute("source", fromState);
			transition.setAttribute("dest", toState);
			transitions.addContent(transition);

			// Creates seperate automatas for restriction of a group.
			Element restriction = sequence_Element.getChild("Restriction");

			if (restriction != null) {
				placeAutomatas(sequence, restriction, automata);
			}

		}
		return current_automata_state;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public int placeArbGroup(Element group_element, int current_automata_state, Element automaton) {

		Element events = automaton.getChild("Events");
		Element states = automaton.getChild("States");
		Element transitions = automaton.getChild("Transitions");

		int antal_operationer = 0;
		List arbitraryList = group_element.getChildren("Process");
		String[] elements = new String[arbitraryList.size()];
		for(Iterator listArbitrary = arbitraryList.iterator(); listArbitrary.hasNext(); )
		{
			Element arbitrary_Element = (Element) listArbitrary.next();
			String arbitrary = arbitrary_Element.getAttributeValue("Id");

			Element event = new Element("Event");
			event.setAttribute("id", arbitrary);
			event.setAttribute("label", arbitrary);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			elements[antal_operationer] = arbitrary;
			antal_operationer = antal_operationer + 1;

			// Creates seperate automatas for restriction of a group.
			Element restriction = arbitrary_Element.getChild("Restriction");

			if (restriction != null) {
				placeAutomatas(arbitrary, restriction, automata);
			}
		}

		int summa = fac(antal_operationer);
		int final_state = summa*(antal_operationer-1)+1 + current_automata_state;
		int start_state = current_automata_state;

		Element state = new Element("State");
		state.setAttribute("id", "q" + final_state);
		state.setAttribute("initial", "false");
		states.addContent(state);

		int[] indices;

		PermutationGenerator x = new PermutationGenerator (elements.length);
		StringBuffer permutation;
		while (x.hasMore ()) {
			permutation = new StringBuffer ();
			indices = x.getNext ();
			for (int i = 0; i < indices.length; i++) {
				permutation.append (elements[indices[i]]);

				if (i >= antal_operationer-1) {

					Element transition = new Element("Transition");
					transition.setAttribute("event", elements[indices[i]]);
					transition.setAttribute("source", "q" + current_automata_state);
					transition.setAttribute("dest", "q" + final_state);
					transitions.addContent(transition);

				} else if (i < 1) {

					state = new Element("State");
					current_automata_state = current_automata_state + 1;
					state.setAttribute("id", "q" + current_automata_state);
					state.setAttribute("initial", "false");
					states.addContent(state);

					Element transition = new Element("Transition");
					transition.setAttribute("event", elements[indices[i]]);
					int state_nr = current_automata_state - 1;
					String fromState = "q" + state_nr;
					transition.setAttribute("source", "q" + start_state);
					transition.setAttribute("dest", "q" + current_automata_state);
					transitions.addContent(transition);

				} else {
					state = new Element("State");
					current_automata_state = current_automata_state + 1;
					state.setAttribute("id", "q" + current_automata_state);
					state.setAttribute("initial", "false");
					states.addContent(state);

					Element transition = new Element("Transition");
					transition.setAttribute("event", elements[indices[i]]);
					int state_nr = current_automata_state - 1;
					String fromState = "q" + state_nr;
					transition.setAttribute("source", fromState);
					transition.setAttribute("dest", "q" + current_automata_state);
					transitions.addContent(transition);
				}
			}
		}
		makeDeterministic(automaton);
		return final_state;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public int placeParGroup(Element group_element, int current_automata_state, Element automaton) {

		Element events = automaton.getChild("Events");
		Element states = automaton.getChild("States");
		Element transitions = automaton.getChild("Transitions");

		int antal_operationer = 0;
		List parallelList = group_element.getChildren("Process");
		String[] elements = new String[parallelList.size()];
		for(Iterator listParallel = parallelList.iterator(); listParallel.hasNext(); )
		{
			Element parallel_Element = (Element) listParallel.next();
			String parallel = parallel_Element.getAttributeValue("Id");

			Element event = new Element("Event");
			event.setAttribute("id", parallel);
			event.setAttribute("label", parallel);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			elements[antal_operationer] = parallel;
			antal_operationer = antal_operationer + 1;

			// Creates seperate automatas for restriction of a group.
			Element restriction = parallel_Element.getChild("Restriction");

			if (restriction != null) {
				placeAutomatas(parallel, restriction, automata);
			}
		}

		int summa = fac(antal_operationer);
		int final_state = summa*(antal_operationer-1)+1 + current_automata_state;
		int start_state = current_automata_state;

		Element state = new Element("State");
		state.setAttribute("id", "q" + final_state);
		state.setAttribute("initial", "false");
		states.addContent(state);

		int[] indices;

		PermutationGenerator x = new PermutationGenerator (elements.length);
		StringBuffer permutation;
		while (x.hasMore ()) {
			permutation = new StringBuffer ();
			indices = x.getNext ();
			for (int i = 0; i < indices.length; i++) {
				permutation.append (elements[indices[i]]);

				if (i >= antal_operationer-1) {

					Element transition = new Element("Transition");
					transition.setAttribute("event", elements[indices[i]]);
					transition.setAttribute("source", "q" + current_automata_state);
					transition.setAttribute("dest", "q" + final_state);
					transitions.addContent(transition);

				} else if (i < 1) {

					state = new Element("State");
					current_automata_state = current_automata_state + 1;
					state.setAttribute("id", "q" + current_automata_state);
					state.setAttribute("initial", "false");
					states.addContent(state);

					Element transition = new Element("Transition");
					transition.setAttribute("event", elements[indices[i]]);
					int state_nr = current_automata_state - 1;
					String fromState = "q" + state_nr;
					transition.setAttribute("source", "q" + start_state);
					transition.setAttribute("dest", "q" + current_automata_state);
					transitions.addContent(transition);

				} else {
					state = new Element("State");
					current_automata_state = current_automata_state + 1;
					state.setAttribute("id", "q" + current_automata_state);
					state.setAttribute("initial", "false");
					states.addContent(state);

					Element transition = new Element("Transition");
					transition.setAttribute("event", elements[indices[i]]);
					int state_nr = current_automata_state - 1;
					String fromState = "q" + state_nr;
					transition.setAttribute("source", fromState);
					transition.setAttribute("dest", "q" + current_automata_state);
					transitions.addContent(transition);
				}
			}
		}
		makeDeterministic(automaton);
		return final_state;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Element makeDeterministic(Element automaton) {

		Vector taBortState = new Vector();

		Element transitions = automaton.getChild("Transitions");
		List transitionList = transitions.getChildren("Transition");
		for(Iterator listTransition = transitionList.iterator(); listTransition.hasNext(); )
		{
			Element transitionElement = (Element) listTransition.next();
			String eventAttribute = transitionElement.getAttributeValue("event");
			String sourceAttribute = transitionElement.getAttributeValue("source");
			String destAttribute = transitionElement.getAttributeValue("dest");

			boolean firstTime = true;
			String commonDestination = null;

			List nextTransitionList = transitions.getChildren("Transition");
			for(Iterator nextListTransition = nextTransitionList.iterator(); nextListTransition.hasNext(); )
			{
				Element nextTransitionElement = (Element) nextListTransition.next();
				String nextEventAttribute = nextTransitionElement.getAttributeValue("event");
				String nextSourceAttribute = nextTransitionElement.getAttributeValue("source");
				String nextDestAttribute = nextTransitionElement.getAttributeValue("dest");

				if (eventAttribute.equals(nextEventAttribute) && sourceAttribute.equals(nextSourceAttribute)) {

					if (! nextDestAttribute.equals(destAttribute)) {

						String newName = "bort";
						nextTransitionElement.setName(newName);

						taBortState.add(nextDestAttribute);
						changeSourceState(automaton, destAttribute, nextDestAttribute);
					}
				}
			}
		}
		removeStates(automaton, taBortState);
		transitions.removeChildren("bort");
		return automaton;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void changeSourceState(Element automaton, String commonState, String oldState) {

		Element transitions = automaton.getChild("Transitions");
		List transitionList = transitions.getChildren("Transition");
		for(Iterator listTransition = transitionList.iterator(); listTransition.hasNext(); )
		{
			Element transitionElement = (Element) listTransition.next();
			String eventAttribute = transitionElement.getAttributeValue("event");
			String sourceAttribute = transitionElement.getAttributeValue("source");
			String destAttribute = transitionElement.getAttributeValue("dest");

			if (sourceAttribute.equals(oldState)) {
				transitionElement.setAttribute("source", commonState);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void removeStates(Element automaton, Vector taBortState) {

		Element states = automaton.getChild("States");
		List stateList = states.getChildren("State");
		for(Iterator listState = stateList.iterator(); listState.hasNext(); )
		{
			Element stateElement = (Element) listState.next();
			String stateId = (String) stateElement.getAttributeValue("id");

			if (taBortState.contains(stateId)) {

				stateElement.setName("bort");
			}
		}
		states.removeChildren("bort");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public int fac(int n) {
		if (n==0)
			return 1;
		else
			return n*fac(n-1);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Document getDoc() {
		return AutomataDoc;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void placeAutomatas(String single_OP_id, Element restriction, Element automata) {

		boolean onlyOne = true;
		List singleRestrictionList = restriction.getChildren("And");
		for(Iterator listSingleRestriction = singleRestrictionList.iterator(); listSingleRestriction.hasNext(); )
		{
			Element operation_restriction = (Element) listSingleRestriction.next();
			placeAutomatonWithRestriction(automata, operation_restriction, single_OP_id);
			placeAutomatas(single_OP_id, operation_restriction, automata);
			onlyOne = false;
		}
		List alternativeRestrictionList = restriction.getChildren("Or");
		for(Iterator listAlternativeRestriction = alternativeRestrictionList.iterator(); listAlternativeRestriction.hasNext(); )
		{
			Element alternative_restriction = (Element) listAlternativeRestriction.next();
			placeAutomatonWithAltRestriction(automata, alternative_restriction, single_OP_id);
			placeAutomatas(single_OP_id, alternative_restriction, automata);
			onlyOne = false;
		}
		if (onlyOne && restriction.getName().equals("Restriction")) {
			placeAutomatonWithRestriction(automata, restriction, single_OP_id);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void placeAutomaton(Element automata, String single_OP_id) {

		// Create and insert Automata in new Document
		Element automaton = new Element("Automaton");
		automaton.setAttribute("name", "Single Process");
		automata.addContent(automaton);

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////// Skapar events och lägger till dessa ////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// Create and insert Events in Automata
		Element events = new Element("Events");
		automaton.addContent(events);

		// Create and insert OP_Event in Automata
		Element event = new Element("Event");
		event.setAttribute("id", single_OP_id);
		event.setAttribute("label", single_OP_id);
		event.setAttribute("controllable", "true");
		event.setAttribute("prioritized", "true");
		events.addContent(event);

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////// Skapar states och lägger till dessa ////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// Create and insert States in Automata
		Element states = new Element("States");
		automaton.addContent(states);

		// Create and insert State in Automata
		Element initial_state = new Element("State");
		initial_state.setAttribute("id", "q0");
		initial_state.setAttribute("initial", "true");
		states.addContent(initial_state);

		Element state = new Element("State");
		state.setAttribute("id", "q1");
		state.setAttribute("initial", "false");
		states.addContent(state);


		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////// Skapar transitions och lägger till dessa ///////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// Create and insert States in Automata
		Element transitions = new Element("Transitions");
		automaton.addContent(transitions);

		// Create and insert State in Automata
		Element transition = new Element("Transition");
		transition.setAttribute("event", single_OP_id);
		transition.setAttribute("source", "q0");
		transition.setAttribute("dest", "q1");
		transitions.addContent(transition);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void placeAutomatonWithRestriction(Element automata, Element operation_restriction, String single_OP_id) {

		List restrictingOPList = operation_restriction.getChildren("Process");
		for(Iterator listRestrictingOP = restrictingOPList.iterator(); listRestrictingOP.hasNext(); )
		{
			Element automaton = new Element("Automaton");
			automaton.setAttribute("name", "Single Process");
			automata.addContent(automaton);

			Element restricting_OP_element = (Element) listRestrictingOP.next();
			String restricting_OP_id = restricting_OP_element.getAttributeValue("id");

			// Creates evenets
			Element events = new Element("Events");
			automaton.addContent(events);

			Element event = new Element("Event");
			event.setAttribute("id", restricting_OP_id);
			event.setAttribute("label", restricting_OP_id);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			event = new Element("Event");
			event.setAttribute("id", single_OP_id);
			event.setAttribute("label", single_OP_id);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			// Creates states
			Element states = new Element("States");
			automaton.addContent(states);

			Element initial_state = new Element("State");
			initial_state.setAttribute("id", "q0");
			initial_state.setAttribute("initial", "true");
			states.addContent(initial_state);

			Element state = new Element("State");
			state.setAttribute("id", "q1");
			state.setAttribute("initial", "false");
			states.addContent(state);

			state = new Element("State");
			state.setAttribute("id", "q2");
			state.setAttribute("initial", "false");
			states.addContent(state);

			// Creates transitions
			Element transitions = new Element("Transitions");
			automaton.addContent(transitions);

			Element transition = new Element("Transition");
			transition.setAttribute("event", restricting_OP_id);
			transition.setAttribute("source", "q0");
			transition.setAttribute("dest", "q1");
			transitions.addContent(transition);

			transition = new Element("Transition");
			transition.setAttribute("event", single_OP_id);
			transition.setAttribute("source", "q1");
			transition.setAttribute("dest", "q2");
			transitions.addContent(transition);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void placeAutomatonWithAltRestriction(Element automata, Element alternative_restriction, String single_OP_id) {

		// Insert Automata in new Document
		Element automaton = new Element("Automaton");
		automaton.setAttribute("name", "Single Process");
		automata.addContent(automaton);

		// Insert Events in Automata
		Element events = new Element("Events");
		automaton.addContent(events);

		Element event = new Element("Event");
		event.setAttribute("id", single_OP_id);
		event.setAttribute("label", single_OP_id);
		event.setAttribute("controllable", "true");
		event.setAttribute("prioritized", "true");
		events.addContent(event);

		// Insert States in Automata
		Element states = new Element("States");
		automaton.addContent(states);

		Element initial_state = new Element("State");
		initial_state.setAttribute("id", "q0");
		initial_state.setAttribute("initial", "true");
		states.addContent(initial_state);

		Element state = new Element("State");
		state.setAttribute("id", "q1");
		state.setAttribute("initial", "false");
		states.addContent(state);

		state = new Element("State");
		state.setAttribute("id", "q2");
		state.setAttribute("initial", "false");
		states.addContent(state);

		// Insert transitions in Automata
		Element transitions = new Element("Transitions");
		automaton.addContent(transitions);

		// List alternative restriction
		List alternativeOperationList = alternative_restriction.getChildren("Process");
		for(Iterator listAlternativeOperation = alternativeOperationList.iterator(); listAlternativeOperation.hasNext(); )
		{
			Element alternative_operation = (Element)listAlternativeOperation.next();
			String alternative_OP_id = alternative_operation.getAttributeValue("id");

			// Insert Event in Automata
			event = new Element("Event");
			event.setAttribute("id", alternative_OP_id);
			event.setAttribute("label", alternative_OP_id);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			// Insert State in Automata
			Element transition = new Element("Transition");
			transition.setAttribute("event", alternative_OP_id);
			transition.setAttribute("source", "q0");
			transition.setAttribute("dest", "q1");
			transitions.addContent(transition);

		}
		Element transition = new Element("Transition");
		transition.setAttribute("event", single_OP_id);
		transition.setAttribute("source", "q1");
		transition.setAttribute("dest", "q2");
		transitions.addContent(transition);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void placeGroupAutomatas(Element group, Element restriction, Element automata) {

		boolean onlyOne = true;
		List singleRestrictionList = restriction.getChildren("And");
		for(Iterator listSingleRestriction = singleRestrictionList.iterator(); listSingleRestriction.hasNext(); )
		{
			Element operation_restriction = (Element) listSingleRestriction.next();
			placeGroupWithRestriction(automata, operation_restriction, group);
			placeGroupAutomatas(group, operation_restriction, automata);
			onlyOne = false;
		}
		List alternativeRestrictionList = restriction.getChildren("Or");
		for(Iterator listAlternativeRestriction = alternativeRestrictionList.iterator(); listAlternativeRestriction.hasNext(); )
		{
			Element alternative_restriction = (Element) listAlternativeRestriction.next();
			placeGroupWithAltRestriction(automata, alternative_restriction, group);
			placeGroupAutomatas(group, alternative_restriction, automata);
			onlyOne = false;
		}
		if (onlyOne && restriction.getName().equals("Restriction")) {
			placeGroupWithRestriction(automata, restriction, group);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void placeGroupWithRestriction(Element automata, Element operation_restriction, Element group) {

		List restrictingOPList = operation_restriction.getChildren("Process");
		for(Iterator listRestrictingOP = restrictingOPList.iterator(); listRestrictingOP.hasNext(); )
		{
			Element automaton = new Element("Automaton");
			automaton.setAttribute("name", "Single Process");
			automata.addContent(automaton);

			Element restricting_OP_element = (Element) listRestrictingOP.next();
			String restricting_OP_id = restricting_OP_element.getAttributeValue("id");

			// Creates evenets
			Element events = new Element("Events");
			automaton.addContent(events);

			Element event = new Element("Event");
			event.setAttribute("id", restricting_OP_id);
			event.setAttribute("label", restricting_OP_id);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			// Creates states
			Element states = new Element("States");
			automaton.addContent(states);

			Element initial_state = new Element("State");
			initial_state.setAttribute("id", "q0");
			initial_state.setAttribute("initial", "true");
			states.addContent(initial_state);

			Element state = new Element("State");
			state.setAttribute("id", "q1");
			state.setAttribute("initial", "false");
			states.addContent(state);

			// Creates transitions
			Element transitions = new Element("Transitions");
			automaton.addContent(transitions);

			Element transition = new Element("Transition");
			transition.setAttribute("event", restricting_OP_id);
			transition.setAttribute("source", "q0");
			transition.setAttribute("dest", "q1");
			transitions.addContent(transition);

			// Insert the restricted group
			String ElementName = group.getName();
			if (ElementName.equals("substitution")) {
				placeAltGroup(group, 1, automaton);
			}
			if (ElementName.equals("sequence")) {
				placeSeqGroup(group, 1, automaton);
			}
			if (ElementName.equals("exclusiveness")) {
				placeArbGroup(group, 1, automaton);
			}
			if (ElementName.equals("simultaneity")) {
				placeParGroup(group, 1, automaton);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void placeGroupWithAltRestriction(Element automata, Element alternative_restriction, Element group) {

		// Insert Automata in new Document
		Element automaton = new Element("Automaton");
		automaton.setAttribute("name", "Single Process");
		automata.addContent(automaton);

		// Insert Events in Automata
		Element events = new Element("Events");
		automaton.addContent(events);

		// Insert States in Automata
		Element states = new Element("States");
		automaton.addContent(states);

		Element initial_state = new Element("State");
		initial_state.setAttribute("id", "q0");
		initial_state.setAttribute("initial", "true");
		states.addContent(initial_state);

		Element state = new Element("State");
		state.setAttribute("id", "q1");
		state.setAttribute("initial", "false");
		states.addContent(state);

		// Insert transitions in Automata
		Element transitions = new Element("Transitions");
		automaton.addContent(transitions);

		List alternativeOperationList = alternative_restriction.getChildren("Process");
		for(Iterator listAlternativeOperation = alternativeOperationList.iterator(); listAlternativeOperation.hasNext(); )
		{
			// Get alternative restriction
			Element alternative_operation = (Element)listAlternativeOperation.next();
			String alternative_OP_id = alternative_operation.getAttributeValue("id");

			// Create and insert OP_Event in Automata
			Element event = new Element("Event");
			event.setAttribute("id", alternative_OP_id);
			event.setAttribute("label", alternative_OP_id);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			// Create and insert State in Automata
			Element transition = new Element("Transition");
			transition.setAttribute("event", alternative_OP_id);
			transition.setAttribute("source", "q0");
			transition.setAttribute("dest", "q1");
			transitions.addContent(transition);
		}
	}
}