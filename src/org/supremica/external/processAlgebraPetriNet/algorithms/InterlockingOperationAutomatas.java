package org.supremica.external.processAlgebraPetriNet.algorithms;


import java.util.*;
import org.jdom.*;

public class InterlockingOperationAutomatas {

	Element automata = new Element("Automata");

	public InterlockingOperationAutomatas() {

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void createInterlockingOperationAutomatas(Element PAroot, Element Automata) {

		Vector interlockings = new Vector();
		Element interlocking = null;

		// Add all interlockings to the vector
		List interlockingList = PAroot.getChildren("Process_interlocking");
		for(Iterator listInterlocking = interlockingList.iterator(); listInterlocking.hasNext(); )
		{
			interlocking = (Element) listInterlocking.next();
			String interlockingId = interlocking.getAttributeValue("id");
			interlockings.add(interlockingId);
		}


		// List all operations
		List operationList = PAroot.getChildren("Operation");
		for(Iterator listOperation = operationList.iterator(); listOperation.hasNext(); )
		{
			Element operation = (Element) listOperation.next();
			String operationId = operation.getAttributeValue("id");

			// List all involved processes
			List processList = operation.getChildren("Process");
			for(Iterator listProcess = processList.iterator(); listProcess.hasNext(); )
			{
				Element process = (Element) listProcess.next();
				String processId = process.getAttributeValue("id");

				if (interlockings.contains(processId)) {
					Element specificInterlocking = getInterlocking(processId, PAroot);
					Vector unsafeState = getUnsafeState(specificInterlocking);

					List triggersAndRestores = findTriggersAndRestores(unsafeState, PAroot);

					Vector triggers = (Vector) triggersAndRestores.get(0);
					Vector restores = (Vector) triggersAndRestores.get(1);
					if (! triggers.isEmpty() | ! restores.isEmpty()) {

						createAutomata(triggersAndRestores, Automata, operationId);

					}
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void createAutomata(List triggersAndRestores, Element automata, String operationId) {

		int initState = 0;

		// Place group in the total group
		Element automaton = new Element("Automaton");
		automaton.setAttribute("name", "Safety specification");
		automata.addContent(automaton);

		Element events = new Element("Events");
		automaton.addContent(events);

		Element states = new Element("States");
		automaton.addContent(states);

		Element transitions = new Element("Transitions");
		automaton.addContent(transitions);

		Element state = new Element("State");
		state.setAttribute("id", "q" + initState);
		state.setAttribute("initial", "true");
		states.addContent(state);

	    state = new Element("State");
		state.setAttribute("id", "q" + 1);
		state.setAttribute("initial", "false");
		states.addContent(state);

		Element event = new Element("Event");
		event.setAttribute("id", operationId + "Sta");
		event.setAttribute("label", operationId + "Sta");
		event.setAttribute("controllable", "true");
		event.setAttribute("prioritized", "true");
		events.addContent(event);

		event = new Element("Event");
		event.setAttribute("id", operationId + "Sto");
		event.setAttribute("label", operationId + "Sto");
		event.setAttribute("controllable", "true");
		event.setAttribute("prioritized", "true");
		events.addContent(event);

		Element transition = new Element("Transition");
		transition.setAttribute("event", operationId + "Sta");
		transition.setAttribute("source", "q" + 0);
		transition.setAttribute("dest", "q" + 1);
		transitions.addContent(transition);

		transition = new Element("Transition");
		transition.setAttribute("event", operationId + "Sto");
		transition.setAttribute("source", "q" + 1);
		transition.setAttribute("dest", "q" + 0);
		transitions.addContent(transition);

		Vector triggers = (Vector) triggersAndRestores.get(0);
		String[] triggersArray = new String[triggers.size()];
		int i;
		for(i=0; i<triggers.size(); i=i+1) {
			String trigger = (String) triggers.get(i);
			triggersArray[i] = trigger;

			event = new Element("Event");
			event.setAttribute("id", trigger + "Sta");
			event.setAttribute("label", trigger + "Sta");
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);
		}

		Vector restores = (Vector) triggersAndRestores.get(1);
		int j;
		for(j=0; j<restores.size(); j=j+1) {
			String restore = (String) restores.get(j);

			event = new Element("Event");
			event.setAttribute("id", restore + "Sto");
			event.setAttribute("label", restore + "Sto");
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);
		}


		placeSpecification(restores, triggersArray, automaton);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void placeSpecification(Vector restores, String[] triggersArray, Element automaton) {

		int current_automata_state = 1;

		Element events = automaton.getChild("Events");
		Element states = automaton.getChild("States");
		Element transitions = automaton.getChild("Transitions");

		int[] indices;
		PermutationGenerator x = new PermutationGenerator (triggersArray.length);
		StringBuffer permutation;
		while (x.hasMore ()) {

			permutation = new StringBuffer ();
			indices = x.getNext ();
			for (int i = 0; i < indices.length; i++) {
				permutation.append (triggersArray[indices[i]]);

				if (i < 1) {
					Element state = new Element("State");
					current_automata_state = current_automata_state + 1;
					state.setAttribute("id", "q" + current_automata_state);
					state.setAttribute("initial", "false");
					states.addContent(state);

					Element transition = new Element("Transition");
					transition.setAttribute("event", triggersArray[indices[i]] + "Sta");
					transition.setAttribute("source", "q" + 0);
					transition.setAttribute("dest", "q" + current_automata_state);
					transitions.addContent(transition);

					int k;
					for (k=0; k<restores.size(); k=k+1) {
						String restore = (String) restores.get(k);

						transition = new Element("Transition");
						transition.setAttribute("event", restore + "Sto");
						transition.setAttribute("source", "q" + current_automata_state);
						transition.setAttribute("dest", "q" + 0);
						transitions.addContent(transition);
					}
				} else {
					Element state = new Element("State");
					current_automata_state = current_automata_state + 1;
					state.setAttribute("id", "q" + current_automata_state);
					state.setAttribute("initial", "false");
					states.addContent(state);

					Element transition = new Element("Transition");
					transition.setAttribute("event", triggersArray[indices[i]] + "Sta");
					int state_nr = current_automata_state - 1;
					String fromState = "q" + state_nr;
					transition.setAttribute("source", fromState);
					transition.setAttribute("dest", "q" + current_automata_state);
					transitions.addContent(transition);

					int l;
					for (l=0; l<restores.size(); l=l+1) {
						String restore = (String) restores.get(l);

						transition = new Element("Transition");
						transition.setAttribute("event", restore + "Sto");
						state_nr = current_automata_state - 1;
						fromState = "q" + state_nr;
						transition.setAttribute("source", "q" + current_automata_state);
						transition.setAttribute("dest", "q" + 0);
						transitions.addContent(transition);
					}
				}
			}
		}
		makeDeterministic(automaton);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public List findTriggersAndRestores(Vector unsafeState, Element PAroot) {

		List triggersAndRestores = new ArrayList();

		Vector triggers = new Vector();
		Vector restores = new Vector();
		Vector stateVector = null;

		// List all operations
		List operationList = PAroot.getChildren("Operation");
		for(Iterator listOperation = operationList.iterator(); listOperation.hasNext(); )
		{
			boolean trigger = false;
			Element operation = (Element) listOperation.next();
			String operationId = operation.getAttributeValue("id");

			// List all involved processes
			List processList = operation.getChildren("Process");
			for(Iterator listProcess = processList.iterator(); listProcess.hasNext(); )
			{
				Element process = (Element) listProcess.next();
				Element event = process.getChild("Event");
				Element restriction = event.getChild("Restriktion");
				Element and = restriction.getChild("And");

				stateVector = new Vector();

				List stateList = and.getChildren("State");
				for(Iterator listState = stateList.iterator(); listState.hasNext(); )
				{
					Element state = (Element) listState.next();

					String stateName = state.getAttributeValue("name");
					String stateId = state.getAttributeValue("id");

					String stateExpression = stateName + stateId;

					stateVector.add(stateExpression);
				}

				if (stateVector.containsAll(unsafeState)) {
					triggers.add(operationId);
					trigger = true;
				}
			}
			if (! stateVector.containsAll(unsafeState) && trigger) {
				restores.add(operationId);
			}
		}
		triggersAndRestores.add(triggers);
		triggersAndRestores.add(restores);
		return triggersAndRestores;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Element getInterlocking(String Id, Element PAroot) {

		Element interlockingElement = null;

		List interlockingList = PAroot.getChildren("Process_interlocking");
		for(Iterator listInterlocking = interlockingList.iterator(); listInterlocking.hasNext(); )
		{
			Element interlocking = (Element) listInterlocking.next();
			String interlockingId = interlocking.getAttributeValue("id");

			if (Id.equals(interlockingId)) {
				interlockingElement = interlocking;
			}
		}
		return interlockingElement;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Vector getUnsafeState(Element interlocking) {

		Vector unsafeStateVector = new Vector();

		Element event = interlocking.getChild("Event");
		Element restriction = event.getChild("Restriction");
		Element or = restriction.getChild("Or");
		Element and = or.getChild("And");

		List stateList = and.getChildren("State");
		for(Iterator listState = stateList.iterator(); listState.hasNext(); )
		{
			Element state = (Element) listState.next();

			String stateName = state.getAttributeValue("name");
			String stateId = state.getAttributeValue("id");
			String stateExpression = stateName + stateId;

			unsafeStateVector.add(stateExpression);
		}
		return unsafeStateVector;
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

						String initState = "q" + 0;

						List nextnextTransitionList = transitions.getChildren("Transition");
						for(Iterator nextnextListTransition = nextnextTransitionList.iterator(); nextnextListTransition.hasNext(); )
						{
							Element nextnextTransitionElement = (Element) nextnextListTransition.next();
							String nextnextEventAttribute = nextnextTransitionElement.getAttributeValue("event");
							String nextnextSourceAttribute = nextnextTransitionElement.getAttributeValue("source");
							String nextnextDestAttribute = nextnextTransitionElement.getAttributeValue("dest");

							if (nextnextSourceAttribute.equals(nextDestAttribute) && nextnextDestAttribute.equals(initState)) {
								newName = "bort";
								nextnextTransitionElement.setName(newName);
							}
						}
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
}