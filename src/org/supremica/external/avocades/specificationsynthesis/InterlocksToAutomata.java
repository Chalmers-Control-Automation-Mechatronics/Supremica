package org.supremica.external.avocades.specificationsynthesis;

import java.util.*;
import org.jdom.*;

public class InterlocksToAutomata {

	Element automata = new Element("Automata");
	//Document automataDoc;

	public InterlocksToAutomata() {

	}


	/************************************************************************
	*
	* test
	*
	*************************************************************************/

	public void test(Element root, Element Automata) {
		System.out.println("Hit");

		List ops = root.getChildren("Operation");
		System.out.println("Hit2");
		ArrayList opIds = new ArrayList();
				// Place all op IDs in a ArrayList.
		for(Iterator ILIter = ops.iterator(); ILIter.hasNext(); )
		{
			Element op = (Element) ILIter.next();
			String opId = op.getAttributeValue("opID");
			opIds.add(opId);
		}
		System.out.println("Hit3");

printStringList(opIds);

	}


	/************************************************************************
		*
		* createInterlockAutomata
		*
		*************************************************************************/

		public void createInterlockAutomata(Element EOProot, Element ILroot, Element Automata) {

			//test(PAroot, Automata);

			/*ArrayList procInterlockings = getInterlockingIds(PAroot, "Process_interlocking");
			ArrayList eventInterlockings = getInterlockingIds(PAroot, "Event_interlocking");
			ArrayList robotInterlockings = getInterlockingIds(PAroot, "Robot_interlocking");*/

			// List all operations
			List operationList = EOProot.getChildren("Operation");

			for(Iterator listOperation = operationList.iterator(); listOperation.hasNext(); )
			{
				Element operation = (Element) listOperation.next();
				String operationId = operation.getAttributeValue("id");

				// List all involved processes.
				List processList = operation.getChildren("Process");
				for(Iterator listProcess = processList.iterator(); listProcess.hasNext(); )
				{
					Element process = (Element) listProcess.next();
					String processId = process.getAttributeValue("id");

					// If there is process interlocking for the process, find
					// unsafe states and build automaton.
					/*if (procInterlockings.contains(processId))
					{
						buildProcILAutomata(operationId, processId, PAroot, Automata);
					}

					if (robotInterlockings.contains(processId))
					{
						buildRobotILAutomata(operationId, processId, PAroot, Automata);
					}*/

					// List all involved events.
					List eventList = process.getChildren("Event");
					for(Iterator eventIter = eventList.iterator(); eventIter.hasNext(); )
					{
						Element event = (Element) eventIter.next();
						String eventId = event.getAttributeValue("id");

						// If there is event interlocking for the event, build
						// automaton.
						/*if (eventInterlockings.contains(eventId))
						{
							buildEventILAutomata(operation, eventId, PAroot, Automata);
						}*/
					}
				}
			}
	}


	/************************************************************************
	*
	* getInterlockingIds
	*
	*************************************************************************/

	public ArrayList getInterlockingIds(Element PAroot, String type) {

		List interlockingList = PAroot.getChildren(type);
		ArrayList interlockings = new ArrayList();
		// Place all interlocking IDs in a ArrayList.
		for(Iterator ILIter = interlockingList.iterator(); ILIter.hasNext(); )
		{
			Element interlocking = (Element) ILIter.next();
			String interlockingId = interlocking.getAttributeValue("id");
			interlockings.add(interlockingId);
		}
		return interlockings;
	}


	/************************************************************************
	*
	* getInterlocking
	*
	*************************************************************************/

	public Element getInterlocking(String id, String type, Element PAroot) {

		Element interlockingElement = null;

		List interlockingList = PAroot.getChildren(type);
		for(Iterator listInterlocking = interlockingList.iterator(); listInterlocking.hasNext(); )
		{
			Element interlocking = (Element) listInterlocking.next();
			String interlockingId = interlocking.getAttributeValue("id");

			if (id.equals(interlockingId)) {
				interlockingElement = interlocking;
			}
		}

		return interlockingElement;
	}


	/************************************************************************
	*
	* buildProcILAutomata
	*
	* For process IL, only internal states described as unsafe states
	* are implemented.
	*
	* Process interlocking is not really needed or used so far.
	*
	*************************************************************************/

	public void buildProcILAutomata(String opId, String procId, Element PAroot, Element Automata) {
		Element specificProcIL = getInterlocking(procId, "Process_interlocking", PAroot);

		String safeOrUnsafeProc = specificProcIL.getAttributeValue("type");

		// unsafeStates contains all unsafe states
		ArrayList procUnsafeStates = getUnsafeState(specificProcIL);
		ArrayList procComp = getComponentList(specificProcIL, "proc");
		List triggersRestoresSafes = findTriggersRestoresSafes(procUnsafeStates, procComp, opId, safeOrUnsafeProc, PAroot);
		ArrayList triggers = (ArrayList) triggersRestoresSafes.get(0);
		ArrayList restores = (ArrayList) triggersRestoresSafes.get(1);

		if (! triggers.isEmpty() | ! restores.isEmpty())
		{
			createTrigRestAutomata(triggers, restores, Automata, opId, "proc");
		}
	}

	/************************************************************************
	*
	* buildEventILAutomata
	*
	* EventIL can contain
	* - (internal and) external states,
	* - operations which can be "not ongoing" or "not started"
	*
	* Internal interlocking is only used in manual mode, so it is not included in the
	* generation of a safety specification.
	*
	* It could be used to verify that the EOPs are correct, but in that case the
	* implementation must be changed. The "waitForSafe"-automata does not work correctly
	* for internal interlocks.
	*
	*************************************************************************/

	public void buildEventILAutomata(Element op, String eventId, Element PAroot, Element Automata) {

		String opId = op.getAttributeValue("id");
		Element specificEventIL = getInterlocking(eventId, "Event_interlocking", PAroot);
		Element ILevent = specificEventIL.getChild("Event");
		Element ILrestriction = ILevent.getChild("Restriction");
		Element ILor = ILrestriction.getChild("Or");
		/* Get all ands. Corresponds to the interlocking rows in the Volvo case. */
		List ILandList = ILor.getChildren("And");

		String safeOrUnsafe = specificEventIL.getAttributeValue("type");
		// Find state in operation, before the interresting event
		List execState = getStateBeforeEvent(op, eventId);
		int alt = 0;

		for(Iterator andIter = ILandList.iterator(); andIter.hasNext(); )
		{
			// For each IL row, compare it with the internal state in the
			// operation execution spec, to see if the IL row is
			// relevant.
			alt++;
			Element alternativeState = (Element) andIter.next();
			String tag = " " + eventId;

			// Get list of all components involved in the internal interlocking
			ArrayList intComp = getComponentList(alternativeState, "internal");

			// Get the component states that build the (un)safe state.
			ArrayList internalStates = getStates(alternativeState, "internal");

			boolean relevant = false;
			//System.out.println("execState:" + execState.toString());

			for(Iterator ee = internalStates.iterator(); ee.hasNext(); ) {
				ArrayList internalState = (ArrayList) ee.next();
				//System.out.println("IL, Internal states: "+internalState.toString());
				if (execState.containsAll(internalState)) {
					relevant = true;
					//System.out.println("RELEVANT");
				}
			}

			if( relevant )
			{
				// Internal states not implemented.
				/*if( !internalStates.isEmpty() )
				{
					tag = tag + " int" + alt;
					createIntExtAutomata(internalStates, intComp, opId, tag, safeOrUnsafe, Automata, PAroot);
				}*/

				tag = " " + eventId;
				// At the moment, the algorithm can only handle one external machine.
				ArrayList extComp = getComponentList(alternativeState, "external");
				ArrayList externalStates = getStates(alternativeState, "external");

				if( !externalStates.isEmpty() )
				{
					tag = tag + " ext" + alt;
					System.out.println("buildEventIL");
					createExtAutomata(externalStates, extComp, opId, tag, safeOrUnsafe, Automata, PAroot);
				}


				List notOngoingOp = alternativeState.getChildren("Op_not_ongoing");
				for( Iterator ongoingIter = notOngoingOp.iterator(); ongoingIter.hasNext(); )
				{
					tag = " " + eventId;
					Element ongoing = (Element) ongoingIter.next();
					String ongId = ongoing.getAttributeValue("name");
					tag = tag + " " + ongId + "ong" + alt;
					createOpAutomata(ongoing, opId, tag, Automata, "ong");
				}


				List notStartedOp = alternativeState.getChildren("Op_not_started");
				for( Iterator startedIter = notStartedOp.iterator(); startedIter.hasNext(); )
				{
					tag = " " + eventId;
					Element started = (Element) startedIter.next();
					String nstId = started.getAttributeValue("name");
					tag = tag + " " + nstId + "nst" + alt;
					createOpAutomata(started, opId, tag, Automata, "nst");
				}
			}
		}

	}

	/************************************************************************
	*
	* buildRobotILAutomata
	*
	* RobotIL can contain
	* - external states,
	* - operations that can be "not ongoing" or "not started"
	*
	*************************************************************************/


	public void buildRobotILAutomata(String opId, String procId, Element PAroot, Element Automata) {

		//String opId = op.getAttributeValue("id");

		System.out.println("--------buildRobotILAutomata, op " + opId+" --------");
		Element specificRobotIL = getInterlocking(procId, "Robot_interlocking", PAroot);

		String safeOrUnsafe = specificRobotIL.getAttributeValue("type");

		// Get interlocking
		Element ILevent = specificRobotIL.getChild("Event");
		Element ILrestriction = ILevent.getChild("Restriction");
		Element ILor = ILrestriction.getChild("Or");

		/* Get all ands. Corresponds to the interlocking rows in the Volvo case. */
		List ILandList = ILor.getChildren("And");

		int alt = 0;
		for(Iterator andIter = ILandList.iterator(); andIter.hasNext(); )
		{
			alt++;
			Element alternativeState = (Element) andIter.next();
			String tag;

			// Get list of all components involved in the internal interlocking
			//ArrayList intComp = getComponentList(alternativeState, "internal");

			// Get the component states that build the (un)safe state.
			//ArrayList internalStates = getStates(alternativeState, "internal");

			// At the moment, the algorithm can only handle one external machine.
			ArrayList extComp = getComponentList(alternativeState, "external");
			ArrayList externalStates = getStates(alternativeState, "external");

			if( !externalStates.isEmpty() )
			{
				tag = " ext" + alt;
				createExtAutomata(externalStates, extComp, opId, tag, safeOrUnsafe, Automata, PAroot);
			}


			List notOngoingOp = alternativeState.getChildren("Op_not_ongoing");
			for( Iterator ongoingIter = notOngoingOp.iterator(); ongoingIter.hasNext(); )
			{
				Element ongoing = (Element) ongoingIter.next();
				String ongId = ongoing.getAttributeValue("name");
				tag = " " + ongId + "ong" + alt;
				createOpAutomata(ongoing, opId, tag, Automata, "ong");
			}

			List notStartedOp = alternativeState.getChildren("Op_not_started");
			for( Iterator startedIter = notStartedOp.iterator(); startedIter.hasNext(); )
			{
				Element started = (Element) startedIter.next();
				String nstId = started.getAttributeValue("name");
				tag = " " + nstId + "nst" + alt;
				createOpAutomata(started, opId, tag, Automata, "nst");
			}
		}
	}

	/************************************************************************
	*
	* createExtAutomata
	*
	*************************************************************************/

	public void createExtAutomata(ArrayList states, ArrayList comp, String opId, String tag, String safeOrUnsafe, Element Automata, Element PAroot)
	{
		List trigRestSafe = findTriggersRestoresSafes(states, comp, opId, safeOrUnsafe, PAroot);
		ArrayList triggers = (ArrayList) trigRestSafe.get(0);
		ArrayList restores = (ArrayList) trigRestSafe.get(1);
		ArrayList safes = (ArrayList) trigRestSafe.get(2);
		if (! triggers.isEmpty() | ! restores.isEmpty()) {
			//System.out.println("CreateExt, opId = "+opId);
			createTrigRestAutomata(triggers, restores, Automata, opId, tag);
		}
		if (! safes.isEmpty() | ! restores.isEmpty()) {
			createWaitSafeAutomata(restores, safes, Automata, opId, tag);
		}
	}

	/************************************************************************
	*
	* createOpAutomata
	*
	*************************************************************************/

	public void createOpAutomata(Element op, String operationId, String tag, Element Automata, String type)
	{
		ArrayList triggers = new ArrayList();
		ArrayList restores = new ArrayList();
		String opId = op.getAttributeValue("name");

		triggers.add(opId);
		if(!type.equals("nst"))
		{
			restores.add(opId);
		}

		System.out.println("CreateOp, opId = "+operationId);
		createTrigRestAutomata(triggers, restores, Automata, operationId, tag);
	}


	/************************************************************************
	*
	* getUnsafeState
	*
	* Given the interlocking, find the corresponding unsafe states.
	* Interlocking might for example look like
	* A.on AND B.home OR A.off
	*
	*************************************************************************/

	public ArrayList getUnsafeState(Element interlocking) {

		ArrayList allUnsafeStates = new ArrayList();
		Element event = interlocking.getChild("Event");
		Element restriction = event.getChild("Restriction");
		Element or = restriction.getChild("Or");

		/* Get all ands. Corresponds to the rows in the Volvo case. */
		List andList = or.getChildren("And");
		for(Iterator andIter = andList.iterator(); andIter.hasNext(); )
		{
			Element and = (Element) andIter.next();
			/* Get the states for the first and, e.g. A and B */
			List stateList = and.getChildren("State");
			ArrayList oneUnsafeState = new ArrayList();
			/* For each state (component would be a better word), get its name and id.
			Id corresponds to the unsafe state of the state */
			for(Iterator stateIter = stateList.iterator(); stateIter.hasNext(); )
			{
				Element state = (Element) stateIter.next();

				String stateName = state.getAttributeValue("name");
				String stateId = state.getAttributeValue("id");
				String stateExpression = stateName + stateId;

				oneUnsafeState.add(stateExpression);
			}
			allUnsafeStates.add(oneUnsafeState);
		}

		/* For the example above, the allUnsafeStates ArrayList is Aon,Bhome;Aoff. */
		return allUnsafeStates;
	}

	/************************************************************************
	*
	* getComponentList
	*
	* get a list over components involved in the interlocking
	*
	*************************************************************************/

	public ArrayList getComponentList(Element altState, String intOrExt) {

		ArrayList theComponents = new ArrayList();

		/* Get the states for the first and, e.g. A and B */
		List compList;

		if(intOrExt.equals("internal"))
		{
			compList = altState.getChildren("Internal_state");
		}
		else if(intOrExt.equals("external"))
		{
			compList = altState.getChildren("External_state");
		}
		else if(intOrExt.equals("proc"))
		{
			compList = altState.getChildren("State");
		}
		else
		{
			System.err.println("Unknown state type " + intOrExt);
			return null;
		}

		if(!compList.isEmpty())
		{
			ArrayList oneComp = new ArrayList();
			/* For each state (component would be a better word), get its name and id.
			Id corresponds to the unsafe state of the state */
			for(Iterator compIter = compList.iterator(); compIter.hasNext(); )
			{
				Element comp = (Element) compIter.next();

				String compName = comp.getAttributeValue("name");
				theComponents.add(compName);
			}
		}

		/* For the example above, the allUnsafeStates ArrayList is Aon,Bhome;Aoff. */
		return theComponents;
	}

	/************************************************************************
	*
	* getStates
	*
	*
	* Interlocking might for example look like A.on AND B.home OR A.off
	*
	*************************************************************************/

	public ArrayList getStates(Element altState, String intOrExt) {

		ArrayList theStates = new ArrayList();
		List stateList;

		if(intOrExt.equals("internal"))
		{
			stateList = altState.getChildren("Internal_state");
		}
		else if(intOrExt.equals("external"))
		{
			stateList = altState.getChildren("External_state");
		}
		else
		{
			System.err.println("Unknown state type " + intOrExt);
			return null;
		}

		if(!stateList.isEmpty())
		{
			ArrayList oneState = new ArrayList();
			/* For each state (component would be a better word), get its name and id.
			Id corresponds to the unsafe state of the state */
			for(Iterator stateIter = stateList.iterator(); stateIter.hasNext(); )
			{
				Element state = (Element) stateIter.next();

				String stateName = state.getAttributeValue("name");
				String stateId = state.getAttributeValue("id");
				String stateExpression = stateName + stateId;
				oneState.add(stateExpression);
			}

			theStates.add(oneState);
		}

		/* For the example above, the allUnsafeStates ArrayList is Aon,Bhome;Aoff. */
		return theStates;
	}



	/************************************************************************
	*
	* findTriggersRestoresSafes
	*
	*
	* Interlocking might for example look like A.on AND B.home OR A.off
	*
	************************************************************************/

	public List findTriggersRestoresSafes(ArrayList states, ArrayList comp, String opId, String safeOrUnsafe, Element PAroot)
	{

		if(safeOrUnsafe.equals("safe"))
		{
			return findTriggersRestoresSafesSafe(states, comp, opId, PAroot);
		}
		else if(safeOrUnsafe.equals("unsafe"))
		{
			System.err.println("No new implementation exists");
			//return findTriggersRestoresSafesUnsafe(states, comp, opId, PAroot);
			return null;
		}
		else
		{
			System.err.println("findTriggersRestoresSafes; unknown state type " + safeOrUnsafe);
			return null;
		}
	}




	/************************************************************************
	*
	* findTriggersRestoresSafesSafe

	*
	*
	* Interlocking might for example look like A.on AND B.home OR A.off, and
	* is described as states in which it is safe to execute the event/process
	*
	************************************************************************/

	public List findTriggersRestoresSafesSafe(ArrayList safeStates, ArrayList comp, String opId, Element PAroot)
	{

		//System.out.println("\n****** findTrigRestSafe for operation " + opId + " ******");
		List trigRestSafe = new ArrayList();
		ArrayList triggers = new ArrayList();
		ArrayList restores = new ArrayList();
		ArrayList allSafe = new ArrayList();
		ArrayList executionState = null;
		ArrayList executionComp = null;

		// List of all operations
		List operationList = PAroot.getChildren("Operation");
		for(Iterator operationIter = operationList.iterator(); operationIter.hasNext(); )
		{
			Element operation = (Element) operationIter.next();
			String operationId = operation.getAttributeValue("id");

			boolean trigger = false;

			// Examine all operations but the one for which the safeStates-list is built.
			// An operation can not be a trigger for itself.
			if(operationId != opId )
			{
				// List all involved processes
				List processList = operation.getChildren("Process");

				// For all processes (i.e. steps in the execution spec.)
				Iterator processIter = processList.iterator();

				/* Compare each state in the EOP with each state in the IL. If at least one state in the EOP is not contained in the list of safe states according to the IL, the operation is a trigger. */
				while( processIter.hasNext() )
				{
					Element process = (Element) processIter.next();
					String procId = process.getAttributeValue("id");

					//System.out.println("Ny process, procId= " + procId);

					Element event = process.getChild("Event");
					Element restriction = event.getChild("Restriction");
					Element and = restriction.getChild("And");

					executionState = new ArrayList();
					executionComp = new ArrayList();

					// Build a state-ArrayList for each event
					List stateList = and.getChildren("State");
					for(Iterator listState = stateList.iterator(); listState.hasNext(); )
					{
						Element state = (Element) listState.next();
						String stateName = state.getAttributeValue("name");
						String stateId = state.getAttributeValue("id");
						String stateExpression = stateName + stateId;
						executionState.add(stateExpression);
						executionComp.add(stateName);
					}


					/* As long as we have not found at least one state in the operation that is not in the safe IL */
					/* Why check if executionComp.containsAll?? */
					if( executionComp.containsAll(comp) && !trigger )
					{

						// Now, executionState contains one "global" state in the execution spec
						// of the operation. E.g. A.home S.on.

						// Check if this executionState is in the list containing the safe states
						// for execution of operation opId. If not, the operation operationId is a
						// trigger for opId.

						Iterator e = safeStates.iterator();
						ArrayList oneSafeState = new ArrayList();

						// For all safe states, as long as the execution state is not found.
						while(e.hasNext() && !trigger)
						{
							oneSafeState = (ArrayList) e.next();
							if (!executionState.containsAll(oneSafeState))
							{
								trigger = true;
								//System.out.println("Not a trigger " +operationId +", execState\n" + executionState.toString()+" contains\n" + oneSafeState.toString());
							}
						}
					}
				} // for all processes


				/* Now, executionState contains the last state in the operation. Check if last state in the operation is a safe state, in that case the operation is also a restore operation. */
				/* Why check if executionComp.containsAll?? */
				if(trigger && executionComp.containsAll(comp))
				{
					triggers.add(operationId);
					//System.out.println(operationId + " is a trigger for " + opId);

					// If the operation is a trigger, check if it is also a restore,
					// i.e. if it ends in a safe state.
					// executionState now holds the last state in the execution spec
					// for the operation operationId.
					boolean restore = false;
					Iterator ee = safeStates.iterator();
					while(ee.hasNext() && !restore ) {
						ArrayList oneSafeState = (ArrayList) ee.next();
						if (executionState.containsAll(oneSafeState)) {
							restore = true;
						}
					}
					if(restore) {
						restores.add(operationId);
					}
				}
				else if(executionComp.containsAll(comp))
				{
					allSafe.add(operationId);
				}
			}
		}
		trigRestSafe.add(triggers);
		trigRestSafe.add(restores);
		trigRestSafe.add(allSafe);
		return trigRestSafe;
	}


	/************************************************************************
	*
	* createTrigRestAutomata
	*
	*
	************************************************************************/

	public void createTrigRestAutomata(List triggers, List restores, Element automata, String operationId, String type) {

		int initState = 0;

		System.out.println("createAutomata for opId " +operationId + " type "+ type);

		Element automaton = new Element("Automaton");
		automaton.setAttribute("name", "Safety spec " + operationId + type);
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
		state.setAttribute("accepting", "true");
		states.addContent(state);

		state = new Element("State");
		state.setAttribute("id", "q" + 1);
		state.setAttribute("initial", "false");
		state.setAttribute("accepting", "true");
		states.addContent(state);


		Element event = new Element("Event");
		event.setAttribute("id", "fin" + operationId );
		event.setAttribute("label", "fin" + operationId);
		event.setAttribute("controllable", "true");
		event.setAttribute("prioritized", "true");
		events.addContent(event);

		Element transition = new Element("Transition");
		transition.setAttribute("event", "fin" + operationId);
		transition.setAttribute("source", "q" + 0);
		transition.setAttribute("dest", "q" + 0);
		transitions.addContent(transition);



		//ArrayList triggers = (ArrayList) triggersRestoresSafes.get(0);
		for(Iterator trigIter = triggers.iterator(); trigIter.hasNext(); )
		{
			String trigger = (String) trigIter.next();

			event = new Element("Event");
			event.setAttribute("id", "st" + trigger);
			event.setAttribute("label", "st" + trigger);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			transition = new Element("Transition");
			transition.setAttribute("event", "st" + trigger);
			transition.setAttribute("source", "q" + 0);
			transition.setAttribute("dest", "q" + 1);
			transitions.addContent(transition);

			transition = new Element("Transition");
			transition.setAttribute("event", "st" + trigger);
			transition.setAttribute("source", "q" + 1);
			transition.setAttribute("dest", "q" + 1);
			transitions.addContent(transition);

			// Should there be a self loop for triggerSto as well?
		}

		//ArrayList restores = (ArrayList) triggersRestoresSafes.get(1);
		for(Iterator restIter = restores.iterator(); restIter.hasNext(); )
		{
			String restore = (String) restIter.next();

			event = new Element("Event");
			event.setAttribute("id", "fin" + restore );
			event.setAttribute("label", "fin" + restore);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			transition = new Element("Transition");
			transition.setAttribute("event", "fin" + restore );
			transition.setAttribute("source", "q" + 1);
			transition.setAttribute("dest", "q" + 0);
			transitions.addContent(transition);

		}

		makeDeterministic(automaton);
	}


	/************************************************************************
	*
	* createWaitSafeAutomata
	*
	*
	************************************************************************/

	public void createWaitSafeAutomata(List restores, List safes, Element automata, String operationId, String type) {

		System.out.println("createWaitSafeAutomata for opId " +operationId + " type "+ type);

		Element automaton = new Element("Automaton");
		automaton.setAttribute("name", "Safety spec " + operationId + type + " waitSafe");
		automata.addContent(automaton);

		Element events = new Element("Events");
		automaton.addContent(events);

		Element states = new Element("States");
		automaton.addContent(states);

		Element transitions = new Element("Transitions");
		automaton.addContent(transitions);

		Element state = new Element("State");
		state.setAttribute("id", "q" + 0);
		state.setAttribute("initial", "true");
		state.setAttribute("accepting", "true");
		states.addContent(state);

		state = new Element("State");
		state.setAttribute("id", "q" + 1);
		state.setAttribute("initial", "false");
		state.setAttribute("accepting", "true");
		states.addContent(state);


		Element event = new Element("Event");
		event.setAttribute("id", "fin" + operationId);
		event.setAttribute("label", "fin" + operationId);
		event.setAttribute("controllable", "true");
		event.setAttribute("prioritized", "true");
		events.addContent(event);

		Element transition = new Element("Transition");
		transition.setAttribute("event", "fin" + operationId );
		transition.setAttribute("source", "q" + 1);
		transition.setAttribute("dest", "q" + 1);
		transitions.addContent(transition);

		//ArrayList restores = (ArrayList) triggersRestoresSafes.get(1);
		//ArrayList safes = (ArrayList) triggersRestoresSafes.get(2);

		for(Iterator restIter = restores.iterator(); restIter.hasNext(); )
		{
			String restore = (String) restIter.next();

			event = new Element("Event");
			event.setAttribute("id", "fin" + restore);
			event.setAttribute("label", "fin" + restore);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			transition = new Element("Transition");
			transition.setAttribute("event", "fin" + restore);
			transition.setAttribute("source", "q" + 0);
			transition.setAttribute("dest", "q" + 1);
			transitions.addContent(transition);

			transition = new Element("Transition");
			transition.setAttribute("event", "fin" + restore);
			transition.setAttribute("source", "q" + 1);
			transition.setAttribute("dest", "q" + 1);
			transitions.addContent(transition);

		}

		for(Iterator safeIter = safes.iterator(); safeIter.hasNext(); )
		{
			String safe = (String) safeIter.next();

			event = new Element("Event");
			event.setAttribute("id", "fin" + safe);
			event.setAttribute("label", "fin" + safe);
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			transition = new Element("Transition");
			transition.setAttribute("event", "fin" + safe);
			transition.setAttribute("source", "q" + 0);
			transition.setAttribute("dest", "q" + 1);
			transitions.addContent(transition);

			transition = new Element("Transition");
			transition.setAttribute("event", "fin" + safe);
			transition.setAttribute("source", "q" + 1);
			transition.setAttribute("dest", "q" + 1);
			transitions.addContent(transition);

		}

		makeDeterministic(automaton);
	}



	/************************************************************************
	*
	* makeDeterministic
	*
	*
	************************************************************************/

	public Element makeDeterministic(Element automaton) {

		ArrayList removeState = new ArrayList();

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

						removeState.add(nextDestAttribute);

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
		removeStates(automaton, removeState);
		transitions.removeChildren("bort");
		return automaton;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void removeStates(Element automaton, ArrayList removeState) {

		Element states = automaton.getChild("States");
		List stateList = states.getChildren("State");
		for(Iterator listState = stateList.iterator(); listState.hasNext(); )
		{
			Element stateElement = (Element) listState.next();
			String stateId = (String) stateElement.getAttributeValue("id");

			if (removeState.contains(stateId)) {

				stateElement.setName("bort");
			}
		}
		states.removeChildren("bort");
	}


	/************************************************************************
	*
	* getExecutionStates
	*
	*
	************************************************************************/
	public List getExecutionStates(Element op)
	{

		List execStates = new ArrayList();
		List processList = op.getChildren("Process");
		for(Iterator processIter = processList.iterator(); processIter.hasNext(); )
		{
			Element process = (Element) processIter.next();
			Element event = process.getChild("Event");
			Element restriction = event.getChild("Restriction");
			Element and = restriction.getChild("And");

			List oneState = new ArrayList();

			// Build a state-ArrayList for each event
			List stateList = and.getChildren("State");
			for(Iterator listState = stateList.iterator(); listState.hasNext(); )
			{
				Element state = (Element) listState.next();
				String stateName = state.getAttributeValue("name");
				String stateId = state.getAttributeValue("id");
				String stateExpression = stateName + stateId;
				oneState.add(stateExpression);
			}
			execStates.add(oneState);
		}
		return execStates;
	}


	/************************************************************************
	*
	* getStateBeforeEvent
	*
	*
	************************************************************************/
	public List getStateBeforeEvent(Element op, String eventId)
	{
		List execState = new ArrayList();
		List processList = op.getChildren("Process");
		boolean first = true;
		boolean found = false;
		String newEventId = "", oldEventId = "";

		Iterator processIter = processList.iterator();
		while( processIter.hasNext() && !found)
		{
			Element process = (Element) processIter.next();
			Element event = process.getChild("Event");

			if(first)
			{
				newEventId = event.getAttributeValue("id");
				first = false;
			}
			else
			{
				oldEventId = newEventId;
				newEventId = event.getAttributeValue("id");
				if( newEventId.equals(eventId) )
				{
					found = true;
				}
			}
		}

		Element oldEvent = getEvent(op, oldEventId);
		Element restriction = oldEvent.getChild("Restriction");
		Element and = restriction.getChild("And");
		List stateList = and.getChildren("State");
		for(Iterator listState = stateList.iterator(); listState.hasNext(); )
		{
			Element state = (Element) listState.next();

			String stateName = state.getAttributeValue("name");
			String stateId = state.getAttributeValue("id");
			String stateExpression = stateName + stateId;
			execState.add(stateExpression);
		}
		return execState;
	}

	/************************************************************************
	*
	* getEvent
	*
	*
	************************************************************************/
	public Element getEvent(Element op, String eventId)
	{
		List processList = op.getChildren("Process");
		Iterator processIter = processList.iterator();
		Element theEvent = null;
		boolean found = false;
		while( processIter.hasNext() && !found )
		{
			Element process = (Element) processIter.next();
			Element event = process.getChild("Event");
			String id = event.getAttributeValue("id");
			if(id.equals(eventId))
			{
				found = true;
				theEvent = event;
			}
		}
		return theEvent;
	}


	/*****************************************************/

	public void printSetList(List v)
	{

		int i=1;
		for(Iterator e = v.iterator(); e.hasNext(); )
		{
			Set vv = (Set) e.next();
			System.out.println("Rad " + i);
			i++;
			for(Iterator ee = vv.iterator(); ee.hasNext(); )
			{
				System.out.println(ee.next());
			}
		}
		System.out.println();
	}
/*****************************************************/

	public void printList(List v)
	{
		int i=1;
		for(Iterator e = v.iterator(); e.hasNext(); )
		{
			List vv = (List) e.next();
			System.out.println("Rad " + i);
			i++;
			for(Iterator ee = vv.iterator(); ee.hasNext(); )
			{
				System.out.println(ee.next());
			}
		}
		System.out.println();
	}

/*****************************************************/

	public void printStringList(List v)
	{
		System.out.println();
		int i=1;
		for(Iterator e = v.iterator(); e.hasNext(); )
		{
			//System.out.println("Rad " + i);
			System.out.println(e.next());
			i++;
		}
		System.out.println();
	}

/*****************************************************/

	public void printStringSet(Set v)
	{
		for(Iterator ee = v.iterator(); ee.hasNext(); )
		{
			System.out.println(ee.next());
		}
		System.out.println();
	}
/*****************************************************/

/********************************************************************************
********************************************************************************/

	/************************************************************************
	*
	* checkSelfRestore
	*
	* Not used
	*
	************************************************************************/

	public boolean checkSelfRestore(ArrayList safeStates, ArrayList comp, Element op, String evId, Element PAroot)
	{
		boolean selfRestore = false;
		List procList = op.getChildren("Process");
		String eventId = "";
		Iterator procIter = procList.iterator();
		boolean selfTrigger = false;
		int i = 0;

		while( !eventId.equals(evId) && procIter.hasNext() )
		{
			Element proc = (Element) procIter.next();
			Element event = proc.getChild("Event");
			eventId = event.getAttributeValue("id");
			if( !eventId.equals(evId) )
			{
				Element restr = event.getChild("Restriction");
				Element and = restr.getChild("And");
				List stateList = and.getChildren("State");
				ArrayList execState = new ArrayList();

				for(Iterator stateIter = stateList.iterator(); stateIter.hasNext(); )
				{
					Element state = (Element) stateIter.next();
					String stateName = state.getAttributeValue("name");
					String stateId = state.getAttributeValue("id");
					String stateExpression = stateName + stateId;
					ArrayList oneState = new ArrayList();
					oneState.add(stateExpression);
					execState.add(oneState);

				}

				if( i==0 )
				{
					if( !execState.containsAll(safeStates) )
					{
						selfTrigger = true;
					}
				}
				i++;

				if( selfTrigger && execState.containsAll(safeStates) )
				{
					selfRestore = true;
				}
				else
				{
					selfRestore = false;
				}
			}
		}
		return selfRestore;
	}

	/************************************************************************
	*
	* placeSpecification
	*
	* Not used.
	*
	************************************************************************/

	/*public void placeSpecification(ArrayList restores, String[] triggersArray, Element automaton) {

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
*/

	/************************************************************************
	*
	* findTriggersAndRestoresUnsafe
	*
	* Old implementation. Should be changed as findTriggersRestoresSafesSafe.
	*
	* unsafeStates is an ArrayList containing other "subArrayLists".
	* Each "subArrayList" contains an unsafe state.
	* It is assumed that the total unsafe state can be found in a single operation.
	*************************************************************************/

	public List findTriggersRestoresSafesUnsafe(ArrayList unsafeStates, ArrayList comp, String opId, Element PAroot)
	{
		List triggersAndRestores = new ArrayList();

		ArrayList triggers = new ArrayList();
		ArrayList restores = new ArrayList();
		ArrayList stateArrayList = null;

		// List all operations
		List operationList = PAroot.getChildren("Operation");
		for(Iterator operationIter = operationList.iterator(); operationIter.hasNext(); )
		{
			Element operation = (Element) operationIter.next();
			String operationId = operation.getAttributeValue("id");
			boolean trigger = false;

			/* Examine all operations but the one for which the unsafeStates-list is built.
			   An operation can not be a trigger for itself. */
			if(operationId != opId)
			{
				// List all involved processes
				List processList = operation.getChildren("Process");
				for(Iterator processIter = processList.iterator(); processIter.hasNext(); )
				{
					Element process = (Element) processIter.next();
					Element event = process.getChild("Event");
					Element restriction = event.getChild("Restriction");
					Element and = restriction.getChild("And");

					stateArrayList = new ArrayList();

					// Build a state-ArrayList for each event
					List stateList = and.getChildren("State");
					for(Iterator listState = stateList.iterator(); listState.hasNext(); )
					{
						Element state = (Element) listState.next();
						String stateName = state.getAttributeValue("name");
						String stateId = state.getAttributeValue("id");
						String stateExpression = stateName + stateId;
						stateArrayList.add(stateExpression);
					}

					/* If one of the unsafeStateSubArrayLists in the ArrayList unsafeStates contains
					the unsafe state, the operation is a trigger */
					Iterator e = unsafeStates.iterator();
					while(e.hasNext() && !trigger ) {

						ArrayList oneUnsafeState = (ArrayList) e.next();

						if (stateArrayList.containsAll(oneUnsafeState)) {
							triggers.add(operationId);
							trigger = true;
						}
					}
				}

				/* If the operation is a trigger, check if it is also a restore, i.e. if
				it ends in a safe state. */
				if (trigger)
				{
					boolean restore = true;
					Iterator ee = unsafeStates.iterator();
					while(ee.hasNext() && restore ) {

						ArrayList oneUnsafeState = (ArrayList) ee.next();
						if (stateArrayList.containsAll(oneUnsafeState)) {
							restore = false;
						}
					}

					if(restore) {
						restores.add(operationId);
					}
				}
			}
		}

		triggersAndRestores.add(triggers);
		triggersAndRestores.add(restores);
		return triggersAndRestores;
	}


	/************************************************************************
	*
	* createTrigRestAutomataOld
	*
	* Implementation as it was at first (see
	* @PHDTHESIS{a:hie:2006,
	*   Type =		 {Licentiate thesis},
	*   Author =	 {Kristin Andersson},
	*   School = 	 {Department of Signals and Systems, Chalmers
	*                University of Technology},
	*   Address =	 {G\"oteborg, Sweden},
	*   Title =		 {Hierarchical Control and Restart of Flexible Manufacturing Systems},
	*   Year =		 {2006}
    *  }
	*
	************************************************************************/

	public void createTrigRestAutomataOld(List triggersRestoresSafes, Element automata, String operationId, String type) {

		int initState = 0;

		// Place group in the total group
		Element automaton = new Element("Automaton");
		automaton.setAttribute("name", "Safety spec " + operationId + type);
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
		state.setAttribute("accepting", "true");
		states.addContent(state);

		state = new Element("State");
		state.setAttribute("id", "q" + 1);
		state.setAttribute("initial", "false");
		state.setAttribute("accepting", "true");
		states.addContent(state);

		/*Element event = new Element("Event");
		event.setAttribute("id", operationId + "Sta");
		event.setAttribute("label", operationId + "Sta");
		event.setAttribute("controllable", "true");
		event.setAttribute("prioritized", "true");
		events.addContent(event);*/

		Element event = new Element("Event");
		event.setAttribute("id", operationId + "Sto");
		event.setAttribute("label", operationId + "Sto");
		event.setAttribute("controllable", "false");
		event.setAttribute("prioritized", "true");
		events.addContent(event);

		Element transition = new Element("Transition");
		transition.setAttribute("event", operationId + "Sto");
		transition.setAttribute("source", "q" + 0);
		transition.setAttribute("dest", "q" + 0);
		transitions.addContent(transition);

		/*transition = new Element("Transition");
		transition.setAttribute("event", operationId + "Sto");
		transition.setAttribute("source", "q" + 1);
		transition.setAttribute("dest", "q" + 0);
		transitions.addContent(transition);*/



		ArrayList triggers = (ArrayList) triggersRestoresSafes.get(0);
		for(Iterator trigIter = triggers.iterator(); trigIter.hasNext(); )
		{
			String trigger = (String) trigIter.next();

			event = new Element("Event");
			event.setAttribute("id", trigger + "Sta");
			event.setAttribute("label", trigger + "Sta");
			event.setAttribute("controllable", "true");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			transition = new Element("Transition");
			transition.setAttribute("event", trigger + "Sta");
			transition.setAttribute("source", "q" + 0);
			transition.setAttribute("dest", "q" + 1);
			transitions.addContent(transition);

			transition = new Element("Transition");
			transition.setAttribute("event", trigger + "Sta");
			transition.setAttribute("source", "q" + 1);
			transition.setAttribute("dest", "q" + 1);
			transitions.addContent(transition);

			// Should there be a self loop for triggerSto as well?
		}

		ArrayList restores = (ArrayList) triggersRestoresSafes.get(1);
		for(Iterator restIter = restores.iterator(); restIter.hasNext(); )
		{
			String restore = (String) restIter.next();

			event = new Element("Event");
			event.setAttribute("id", restore + "Sto");
			event.setAttribute("label", restore + "Sto");
			event.setAttribute("controllable", "false");
			event.setAttribute("prioritized", "true");
			events.addContent(event);

			transition = new Element("Transition");
			transition.setAttribute("event", restore + "Sto");
			transition.setAttribute("source", "q" + 1);
			transition.setAttribute("dest", "q" + 0);
			transitions.addContent(transition);

			// The self-loop is actually not needed, since a machine can
			// only execute one operation at a time.
			/*transition = new Element("Transition");
			transition.setAttribute("event", restore + "Sto");
			transition.setAttribute("source", "q" + 0);
			transition.setAttribute("dest", "q" + 0);
			transitions.addContent(transition);*/
		}

		makeDeterministic(automaton);
	}


	/************************************************************************
	*
	* findTriggersAndRestoresSafeOld
	*
	* Old implementation.
	*
	*
	* Interlocking might for example look like A.on AND B.home OR A.off, and
	* is described as states in which it is safe to execute the event/process
	*
	************************************************************************/

	public List findTriggersAndRestoresSafeOld(ArrayList safeStates, ArrayList comp, String opId, Element PAroot)
	{
		//System.out.println("******findTrigRestSafe for operation " + opId + " ******");
		List trigAndRest = new ArrayList();
		ArrayList triggers = new ArrayList();
		ArrayList restores = new ArrayList();
		ArrayList executionState = null;
		ArrayList executionComp = null;

		// List of all operations
		List operationList = PAroot.getChildren("Operation");
		for(Iterator operationIter = operationList.iterator(); operationIter.hasNext(); )
		{
			Element operation = (Element) operationIter.next();
			String operationId = operation.getAttributeValue("id");
			//System.out.println("Studying operation " + operationId);

			boolean trigger = false;

			// Examine all operations but the one for which the safeStates-list is built.
			// An operation can not be a trigger for itself.
			if(operationId != opId )
			{
				// List all involved processes
				List processList = operation.getChildren("Process");
				boolean containsSafeState = false;

				// For all processes (i.e. steps in the execution spec.)
				for(Iterator processIter = processList.iterator(); processIter.hasNext(); )
				{
					boolean triggerProc = true;
					Element process = (Element) processIter.next();
					String procId = process.getAttributeValue("id");
					//System.out.println("Ny process, procId= " + procId);

					Element event = process.getChild("Event");
					Element restriction = event.getChild("Restriction");
					Element and = restriction.getChild("And");

					executionState = new ArrayList();
					executionComp = new ArrayList();

					// Build a state-ArrayList for each event
					List stateList = and.getChildren("State");
					for(Iterator listState = stateList.iterator(); listState.hasNext(); )
					{
						Element state = (Element) listState.next();
						String stateName = state.getAttributeValue("name");
						String stateId = state.getAttributeValue("id");
						String stateExpression = stateName + stateId;
						executionState.add(stateExpression);
						executionComp.add(stateName);
					}

					if( executionComp.containsAll(comp) )
					{
						// Now, executionState contains one "global" state in the execution spec
						// of the operation. E.g. A.home S.on.

						// Check if this executionState is in the list containing the safe states
						// for execution of operation opId. If not, the operation operationId is a
						// trigger for opId.

						Iterator e = safeStates.iterator();
						ArrayList oneSafeState = new ArrayList();
						while(e.hasNext() && triggerProc)
						{
							oneSafeState = (ArrayList) e.next();
							if (executionState.containsAll(oneSafeState)) {
								if(!trigger)
									triggerProc = false;
								containsSafeState = true;
								//System.out.println("Not a trigger " +operationId +", execState\n" + executionState.toString()+" contains\n" + oneSafeState.toString());
							}
						}

					}
					trigger = triggerProc;
				} // for all processes

				if(trigger && executionComp.containsAll(comp) && containsSafeState)
				{
					triggers.add(operationId);
					//System.out.println(operationId + " is a trigger for " + opId);

					// If the operation is a trigger, check if it is also a restore,
					// i.e. if it ends in a safe state.
					// executionState now holds the last state in the execution spec
					// for the operation operationId.
					boolean restore = false;
					Iterator ee = safeStates.iterator();
					while(ee.hasNext() && !restore ) {

						ArrayList oneSafeState = (ArrayList) ee.next();
						if (executionState.containsAll(oneSafeState)) {
							restore = true;
						}
					}
					if(restore) {
						restores.add(operationId);
					}
				}
			}
		}
		trigAndRest.add(triggers);
		trigAndRest.add(restores);
		return trigAndRest;
	}
}