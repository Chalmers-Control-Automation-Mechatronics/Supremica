
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import java.io.*;
import java.util.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

public class AutomataToXml
	implements AutomataSerializer
{
	private Automata automata;
	private Automaton automaton;
	private boolean canonical;
	private boolean includeCost = false;
	private boolean debugMode = false;
	private final static int majorFileVersion = 0;
	private final static int minorFileVersion = 9;

	public AutomataToXml(Automata automata)
	{
		this.automata = automata;
		canonical = false;
	}

	public AutomataToXml(Automaton automaton)
	{
		this.automata = new Automata();

		this.automata.addAutomaton(automaton);

		canonical = false;
	}

	public void serialize(PrintWriter pw)
	{
		pw.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		pw.print("<Automata");

		if (automata.getName() != null)
		{
			pw.print(" name=\"" + normalize(automata.getName()) + "\" ");
		}

		pw.print(" major=\"" + majorFileVersion + "\" ");
		pw.print(" minor=\"" + minorFileVersion + "\" ");

		if (SupremicaProperties.generalUseSecurity())
		{
			pw.print(" owner=\"" + automata.getOwner() + "\"");
			pw.print(" hash=\"" + automata.getHash() + "\"");
		}

		pw.println(">");

		Iterator automataIt = automata.iterator();

		while (automataIt.hasNext())
		{
			Automaton aut = (Automaton) automataIt.next();

			pw.println("<Automaton name=\"" + aut.getName() + "\" type=\"" + aut.getType().toString() + "\">");

			// Print all events
			pw.println("\t<Events>");

			Iterator eventIt = aut.eventIterator();

			while (eventIt.hasNext())
			{
				LabeledEvent event = (LabeledEvent) eventIt.next();

				pw.print("\t\t<Event id=\"" + normalize(event.getId()) + "\" label=\"" + normalize(event.getLabel()) + "\"");

				if (!event.isControllable())
				{
					pw.print(" controllable=\"false\"");
				}

				if (!event.isPrioritized())
				{
					pw.print(" prioritized=\"false\"");
				}

				if (event.isImmediate())
				{
					pw.print(" immediate=\"true\"");
				}

				if (debugMode)
				{
					pw.print(" synchIndex=" + event.getSynchIndex());
				}

				pw.println("/>");
			}

			pw.println("\t</Events>");

			// Print all states
			pw.println("\t<States>");

			Iterator stateIt = aut.stateIterator();

			while (stateIt.hasNext())
			{
				State state = (State) stateIt.next();

				pw.print("\t\t<State id=\"" + normalize(state.getId()) + "\"");

				if (!state.getId().equals(state.getName()))
				{
					pw.print(" name=\"" + normalize(state.getName()) + "\"");
				}

				if (state.isInitial())
				{
					pw.print(" initial=\"true\"");
				}

				if (state.isAccepting())
				{
					pw.print(" accepting=\"true\"");
				}

				if (state.isForbidden())
				{
					pw.print(" forbidden=\"true\"");
				}

				if (includeCost)
				{
					int value = state.getCost();

					if (value != State.UNDEF_COST)
					{
						pw.print(" cost=\"" + value + "\"");
					}
				}

				if (debugMode)
				{
					pw.print(" synchIndex=" + state.getIndex());
				}

				// printIntArray(pw, ((StateRegular)state).getOutgoingEventsIndicies());
				pw.println("/>");
			}

			pw.println("\t</States>");

			// Print all transitions
			pw.println("\t<Transitions>");

			stateIt = aut.stateIterator();

			while (stateIt.hasNext())
			{
				State sourceState = (State) stateIt.next();
				Iterator outgoingArcsIt = sourceState.outgoingArcsIterator();

				while (outgoingArcsIt.hasNext())
				{
					Arc arc = (Arc) outgoingArcsIt.next();
					State destState = arc.getToState();

					pw.print("\t\t<Transition source=\"" + normalize(sourceState.getId()));
					pw.print("\" dest=\"" + normalize(destState.getId()));
					pw.println("\" event=\"" + normalize(arc.getEventId()) + "\"/>");
				}
			}

			pw.println("\t</Transitions>");
			pw.println("</Automaton>");
		}

		pw.println("</Automata>");
		pw.flush();
		pw.close();
	}

	public void serialize(String fileName)
		throws IOException
	{
		serialize(new PrintWriter(new FileWriter(fileName)));
	}

	private String normalize(String s)
	{
		StringBuffer str = new StringBuffer();
		int len = (s != null)
				  ? s.length()
				  : 0;

		for (int i = 0; i < len; i++)
		{
			char ch = s.charAt(i);

			switch (ch)
			{

			case '<' :
			{
				str.append("&lt;");

				break;
			}
			case '>' :
			{
				str.append("&gt;");

				break;
			}
			case '&' :
			{
				str.append("&amp;");

				break;
			}
			case '"' :
			{
				str.append("&quot;");

				break;
			}
			case '\r' :
			case '\n' :
			{
				if (canonical)
				{
					str.append("&#");
					str.append(Integer.toString(ch));
					str.append(';');

					break;
				}

				// else, default append char
			}
			default :
			{
				str.append(ch);
			}
			}
		}

		return str.toString();
	}

	void printIntArray(PrintWriter pw, int[] theArray)
	{
		for (int i = 0; i < theArray.length; i++)
		{
			if (i == 0)
			{
				pw.print(theArray[i]);
			}
			else
			{
				pw.print(" " + theArray[i]);
			}
		}
	}
}
