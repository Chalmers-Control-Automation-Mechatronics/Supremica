/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

package org.supremica.automata.algorithms;

import org.supremica.automata.*;

import java.io.*;
import java.util.*;

public class AutomataToSattLineSFC
	implements AutomataSerializer
{
	private Automata automata;
	private Automaton automaton;
	private boolean debugMode = false;


	public AutomataToSattLineSFC(Automata automata)
	{
		this.automata = automata;
	}

	public void serialize(String filename)
	{ // Empty

	}

	public void serialize(PrintWriter pw)
	{ // Empty

	}

	public void serialize_s(PrintWriter pw)
	{

		// Start of file header

		pw.println("\"Syntax version 2.19, date: 2001-08-10-10:42:24.724 N\"");
		pw.println("\"Original file date: ---\"");
		pw.print("\"Program date: 2001-08-10-10:42:24.724, name: "); // Should perhaps get current date and time
		if (automata.getName() != null)
		{
			pw.println(" " + automata.getName() + " \"");
		}
		else
		{
			pw.println("\"");
		}
		pw.println("(* This program unit was created by Supremica. *)");
		pw.println("");

		// End of file header

		// Start of BasePicture Invocation

		pw.println("BasePicture Invocation");
		pw.println("   ( 0.0 , 0.0 , 0.0 , 1.0 , 1.0 ");
		pw.println("    ) : MODULEDEFINITION DateCode_ 492916896"); // Don't know importance of DateCode
		pw.println("\n");

		// End of basePicture Invocation

		// Start of Module definition.

		pw.println("ModuleDef");
		pw.println("ClippingBounds = ( -10.0 , -10.0 ) ( 10.0 , 10.0 )");
		pw.println("ZoomLimits = 0.0 0.01");

		//End of Module definition

		// Start of Module code.
		// Here comes the automata, the tricky part.

		Iterator automataIt = automata.iterator();
		while (automataIt.hasNext())
		{
			Automaton aut = (Automaton)automataIt.next();

			// Each automaton is translated into a SattLine Sequence.
			// A sequence has the following structure. Step - Transition - Step - Transition ...
			// A step may be followed by an ALTERNATIVSEQuence which has ALTERNATIVEBRANCHes.
			// This is the case if there is more than one transition from a state.
			// A transition may be followed by a PARALLELSEQuence which has PARALLELBRANCHes.
			// This cannot happen for an automaton.

			// If there is _no_ ordinary, that is, non-fork, arc to the first step in drawing order it is an OPENSEQUENCE.
			// Won't check that for now ...

			// Might want to parameterise COORD so that sequences are not drawn on top of each other.
			pw.println("SEQUENCE" + aut.getName() + " COORD -0.3, 0.5 OBJSIZE 0.5, 0.5\"");

			Iterator eventIt = aut.eventIterator();
			while (eventIt.hasNext())
			{
				Event event = (Event)eventIt.next();
				//pw.print("\t\t<Event id=\"" + normalize(event.getId()) + "\" label=\"" + normalize(event.getLabel()) + "\"");
				if (!event.isControllable())
					pw.print(" controllable=\"false\"");
				if (!event.isPrioritized())
					pw.print(" prioritized=\"false\"");
				if (event.isImmediate())
					pw.print(" immediate=\"true\"");
				if (debugMode)
					pw.print(" synchIndex=" + event.getSynchIndex());
				pw.println("/>");
			}
			pw.println("\t</Events>");

			// Print all states
			pw.println("\t<States>");
			Iterator stateIt2 = aut.stateIterator();
			while (stateIt2.hasNext())
			{
				State state = (State)stateIt2.next();
				//pw.print("\t\t<State id=\"" + normalize(state.getId()) + "\"");
				if (!state.getId().equals(state.getName()))
				//	pw.print(" name=\"" + normalize(state.getName()) + "\"");
				if (state.isInitial())
					pw.print(" initial=\"true\"");
				if (state.isAccepting())
					pw.print(" accepting=\"true\"");
				if (state.isForbidden())
					pw.print(" forbidden=\"true\"");
				int value = state.getCost();
				if (value != State.UNDEF_COST)
					pw.print(" cost=\"" + value + "\"");
				if (debugMode)
					pw.print(" synchIndex=" + state.getIndex());
     			// printIntArray(pw, ((StateRegular)state).getOutgoingEventsIndicies());
				pw.println("/>");
			}
			pw.println("\t</States>");


			// Print all transitions
			pw.println("\t<Transitions>");
			// stateIt = aut.stateIterator();
			// while (stateIt.hasNext())
			for (Iterator stateIt = aut.stateIterator(); stateIt.hasNext(); )
			{
				State sourceState = (State)stateIt.next();
				Iterator outgoingArcsIt = sourceState.outgoingArcsIterator();
				while (outgoingArcsIt.hasNext())
				{
					Arc arc = (Arc)outgoingArcsIt.next();
					State destState = arc.getToState();
				//	pw.print("\t\t<Transition source=\"" + normalize(sourceState.getId()));
				//	pw.print("\" dest=\"" + normalize(destState.getId()));
				//	pw.println("\" event=\"" + normalize(arc.getEventId()) + "\"/>");
				}
			}
			pw.println("\t</Transitions>");
			pw.println("</Automaton>");
		}

		pw.flush();
		pw.close();
	}

	public void serialize_g(PrintWriter pw)
	{
		pw.println("\" Syntax version 2.19, date: 2001-11-20-14:16:07.401 N \" ");

		pw.flush();
		pw.close();
	}

	public void serialize_p(PrintWriter pw)
	{
		pw.println("DistributionData");
		pw.println(" ( Version \"Distributiondata version 1.0\" )");
		pw.println("SourceCodeSystems");
		pw.println(" (  )");
		pw.println("ExecutingSystems");
		pw.println(" (  )");

		pw.flush();
		pw.close();

	}

	public void serialize_l(PrintWriter pw)
	{

	}

	public void serialize_s(String fileName)
		throws IOException
	{
		serialize(new PrintWriter(new FileWriter(fileName)));
	}
}
