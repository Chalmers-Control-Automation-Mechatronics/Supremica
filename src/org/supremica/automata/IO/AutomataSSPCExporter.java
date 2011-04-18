package org.supremica.automata.IO;

import org.supremica.automata.*;

import java.io.*;
import java.util.*;

/**
 * <pre>
 * Exports from Supremica to SSPC format, as specified in its manual.
 * SSPC is a BDD-based DES tools designed by A. Sanchez et. al.
 *
 * Note that SSPC does not support the FSC-operator ||.
 * to build a full synchronous composition, we suspect that you need to add
 * a CAUSAL_BEHAVIOR file.
 *
 *
 * PS. this file was design for portability, not readability
 * /Arash
 * </pre>
 */
public class AutomataSSPCExporter
{
	private final File file;
	private final String path;
	private final HashMap<Comparable<?>, Comparable<?>> fileMap, eventMap;
	private int event_count = 0, state_count;

	/** save the automata to disk, use 'file' for the system (project) file name */
	public AutomataSSPCExporter(final Automata automata, final String file)
		throws IOException
	{
		this.file = new File(file);
		this.path = this.file.getParent();
		this.fileMap = new HashMap<Comparable<?>, Comparable<?>>();
		this.eventMap = new HashMap<Comparable<?>, Comparable<?>>();

		final PrintWriter system = new PrintWriter(new FileOutputStream(file));
		final PrintWriter plant = new PrintWriter(new FileOutputStream(file + ".plant"));
		final PrintWriter spec = new PrintWriter(new FileOutputStream(file + ".spec"));

		for (final Iterator<?> autIt = automata.iterator(); autIt.hasNext(); )
		{
			final Automaton currAutomaton = (Automaton) autIt.next();
			final String name = getName(currAutomaton);

			system.println(name + ".fsm");
			saveOne(currAutomaton, name);

			if(currAutomaton.isPlant()) {
				plant.println(name + ".fsm");
			} else if(currAutomaton.isSpecification() || currAutomaton.isSupervisor()) {
				spec.println(name + ".fsm");
			}
		}

		plant.close();
		spec.close();
		system.close();
	}

	/** map event-label -> unique-integer */
	private int getEvent(final String name)
	{
		Integer ret = (Integer) eventMap.get(name);

		if (ret == null)
		{
			ret = event_count++;

			eventMap.put(name, ret);
		}

		return ret.intValue();
	}

	/** map Automaton -> filename */
	private String getName(final Automaton a)
	{
		final String ret = (String) fileMap.get(a);

		if (ret == null)
		{
			final String name = trim(a.getName());

			for (int x = 1; ; x++)
			{
				final String name2 = (x == 1)
							   ? name
							   : (name + x);    // try 'X', 'X2', 'X3' etc...
				final File file2 = new File(path, name2 + ".fsm");

				if (!file2.exists())
				{
					fileMap.put(a, name2);

					return name2;
				}
			}
		}

		return ret;
	}

	/** remove bad chars and the extension (hopefully :) */
	private String trim(final String x)
	{
		int len = x.lastIndexOf('.');

		if (len == -1)
		{
			len = x.length();
		}

		final StringBuffer sb = new StringBuffer(len);

		for (int i = 0; i < len; i++)
		{
			final char c = x.charAt(i);

			sb.append(isGood(c)
					  ? c
					  : '_');
		}

		return sb.toString();
	}

	/** what chars we like */
	private boolean isGood(final char c)
	{
		return (((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')) || (c == '_'));
	}

	/** save one automaton to file  */
	private void saveOne(final Automaton a, final String name)
		throws IOException
	{
		final File file2 = new File(path, name + ".fsm");
		final PrintWriter me = new PrintWriter(new FileOutputStream(file2));
		final HashMap<State, Integer> stateMap = new HashMap<State, Integer>(); // String -> Integer map


		me.println("FSM " + name);
		me.println();

		// 1. build the state map
		for (final Iterator<?> states = a.stateIterator(); states.hasNext(); )
		{
			final State state = (State) states.next();
			final Integer num = state_count++;
			stateMap.put(state, num);
		}

		// dump the state/transition list
		for (final Iterator<?> states = a.stateIterator(); states.hasNext(); )
		{
			final State state = (State) states.next();

			final Integer num = stateMap.get(state);
			me.print("\tSTATE " + num.intValue() );


			if (state.isInitial())
			{
				me.print(" INITIAL");
			}

			if (state.isAccepting())
			{
				me.print(" MARKED");
			}

			me.println();

			for (final Iterator<Arc> arcIt = state.outgoingArcsIterator();
					arcIt.hasNext(); )
			{
				final Arc arc = arcIt.next();

				// String tname = arc.getLabel();
				final int tname = getEvent(arc.getLabel());
				final State toState = arc.getToState();
				final Integer toInt = stateMap.get(toState);
				me.println("\t\tTRANSITION " + tname + " TO " + toInt.intValue() );
			}

			me.println();
		}

		// dump uncontrollable list
		int count = 0;
		for (final Iterator<LabeledEvent> ei = a.eventIterator(); ei.hasNext(); )
		{
			final LabeledEvent le = ei.next();

			if (!le.isControllable())
			{
				me.print((count == 0)
						 ? "\t"
						 : ", ");
				me.print("" + getEvent(le.getLabel()));

				count++;
			}
		}

		if (count != 0)
		{
			me.println();
		}

		me.println();
		me.println("END");
		me.flush();
		me.close();
	}
}
