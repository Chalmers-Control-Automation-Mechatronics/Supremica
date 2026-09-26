/******************* AutomatonToADS.java **********************
 * Conversion from Supremica format to PiTCT .ads format
 */
package org.supremica.automata.IO;

import java.util.*;
import java.io.*;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;


public class AutomatonToADS
	implements AutomataSerializer
{
	// PiTCT assumes that the (single!) initial state has index 0
	// If this is not the case, temporarily swap indices to make it so
	private final int initIndex;
	private final State zeroState; // The state originally with index 0
	private final Map<LabeledEvent, Integer> eventMap = new HashMap<>();

	private final Automaton aut;

	public AutomatonToADS(final Automaton aut)
	{
		this.aut = aut;

		// Cache these things to be able to set the initial state to index 0
		// and to reset things back to what they originally were
		this.zeroState = aut.getStateWithIndex(0);
		this.initIndex = aut.getInitialState().getIndex();
	}

	@Override
	public void serialize(final PrintWriter pw)
		throws Exception
	{
		pw.println(aut.getName()); // PiTCT .ads start with the name on first line
		pw.println();
		pw.println("State size (State set will be (0,1....,size-1)):");
		pw.println(aut.nbrOfStates());
		pw.println();
		pw.println("Marker states:");

		swapIndices(); // Could guard this by zeroState.getIndex() != 0, but why bother

		printMarkedStates(pw);

		pw.println("\n\nVocal states:\n"); // No idea what this is but it is there

		// For the events we cannot use the getIndex() as it always returns -1
		// Have to build a map from event to number
		buildEventMap();

		pw.println("Transitions:");
		printTransitions(pw);

		swapBack();
	}

	@Override
	public void serialize(String fileName)
		throws Exception
	{
		final PrintWriter pw = new PrintWriter(new FileWriter(fileName));
		serialize(pw);
		pw.flush();
		pw.close();
	}

	///////////////////////////////// Private parts below

	private void printMarkedStates(final PrintWriter pw)
	{
		// Find the marked (accepting) states and write their indices
		final Iterator<State> states_it = aut.stateIterator();
		while (states_it.hasNext())
		{
			State state = (State) states_it.next();
			if (state.isAccepting())
			{
				pw.print(state.getIndex());
				pw.print(" ");
			}
		}
	}

	private void buildEventMap()
	{
		int countOdd = 11;	// PiTCT considers controllable events to be odd numbers
		int countEven = 10;	// Uncontrollabel events are even numbers

		final Iterator<LabeledEvent> events = aut.eventIterator();
		while (events.hasNext())
		{
			final LabeledEvent event = events.next();
			if (event.isControllable())
			{
				eventMap.put(event, countOdd);
				countOdd += 2;
			}
			else
			{
				eventMap.put(event, countEven);
				countEven += 2;
			}
		}
	}

	private void printTransitions(final PrintWriter pw)
	{
		final Iterator<State> states = aut.stateIterator();
		while (states.hasNext())
		{
			final State sourceState = states.next();
			final Iterator<Arc> outgoingArcs = sourceState.outgoingArcsIterator();
			while (outgoingArcs.hasNext())
			{
				final Arc arc = (Arc) outgoingArcs.next();
				final State destState = arc.getToState();
				final LabeledEvent event = arc.getEvent();
				final StringBuffer buf = new StringBuffer();
				buf.append(sourceState.getIndex());
				buf.append(" ");
				buf.append(eventMap.get(event));
				buf.append(" ");
				buf.append(destState.getIndex());
				pw.println(buf.toString());
			}
		}
	}

	// PiTCT expects the (single!) initial state to be numbered 0
	private void swapIndices()
	{
		// Give zeroState the initIndex
		this.zeroState.setIndex(this.initIndex);
		// And give the init state index 0
		aut.getInitialState().setIndex(0);
	}
	private void swapBack()
	{
		// Set back zeroState index to 0
		this.zeroState.setIndex(0);
		// Set back the init state index
		aut.getInitialState().setIndex(this.initIndex);
	}

	private String sanityCheck()
	{
		if (!aut.hasInitialState())
		{
			return "This automaton has no initial state";
		}
		return null;
	}
}