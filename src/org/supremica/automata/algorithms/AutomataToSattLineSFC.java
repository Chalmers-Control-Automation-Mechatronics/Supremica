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
import org.supremica.gui.*;

import java.io.*;
import java.util.*;
import org.apache.log4j.*;

public class AutomataToSattLineSFC
	implements AutomataSerializer
{
	private static Category thisCategory = LogDisplay.createCategory(AutomataToSattLineSFC.class.getName());
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

		pw.println("LOCALVARIABLES");
		Alphabet unionAlphabet = null;
		try
		{
			unionAlphabet = AlphabetHelpers.getUnionAlphabet(automata);
		}
		catch (Exception ex)
		{
			thisCategory.error("Failed getting union of alphabets of the selected automata. Code generation aborted.");
			return;
		}
		boolean firstEvent = true;
		for (Iterator alphaIt = unionAlphabet.iterator(); alphaIt.hasNext(); )
		{
			Event currEvent = (Event)alphaIt.next();
			if (firstEvent)
			{
				firstEvent = false;
				pw.print(currEvent.getLabel());
			}
			else
			{
				pw.print(", " + currEvent.getLabel());
			}
		}
		pw.println(": boolean;\n");

		// Start of Module definition.

		pw.println("ModuleDef");
		pw.println("ClippingBounds = ( -10.0 , -10.0 ) ( 10.0 , 10.0 )");
		pw.println("ZoomLimits = 0.0 0.01\n");

		//End of Module definition

		// Start of Module code.

		pw.println("ModuleCode\n");

		// Here comes the automata, the tricky part.



		for (Iterator automataIt = automata.iterator(); automataIt.hasNext(); )
		{
			// Each automaton is translated into a SattLine Sequence.
			// A sequence has the following structure. Step - Transition - Step - Transition ...

			// A step may be followed by an ALTERNATIVSEQuence which has ALTERNATIVEBRANCHes.
			// This is the case if there is more than one transition from a state.
			// The difficulty is to know when the alternative branches merge, and if they do it the "SattLine way".

			// A transition may be followed by a PARALLELSEQuence which has PARALLELBRANCHes.
			// This cannot happen for an automaton.


			Automaton aut = (Automaton)automataIt.next();

			// If there is _no_ ordinary, that is, non-fork, arc to the first step in drawing order it is an OPENSEQUENCE.
			// Won't check that for now ...
			// Might want to parameterise COORD so that sequences are not drawn on top of each other.
			pw.println("SEQUENCE " + aut.getName() + " COORD -0.5, 0.5 OBJSIZE 0.5, 0.5");


			State initState = aut.getInitialState();
			straightSequenceOutput(aut, initState, pw);

/*			State destState = null;
			State currState = initState;
			int level = 0;
			pw.println("SEQINITSTEP " + aut.getName() + "_" + initState.getName());
			while (destState != initState && level == 0)
			{
				if (currState.nbrOfOutgoingArcs() > 1)
				{
					pw.println("ALTERNATIVESEQ");
					level = level + 1;
				}

				boolean firstArc = true;

				for (Iterator outgoingArcsIt = currState.outgoingArcsIterator(); outgoingArcsIt.hasNext(); )
				{
					if (firstArc)
					{
						firstArc = false;
					}
					else
					{
						pw.println("ALTERNATIVEBRANCH");
					}

					Arc arc = (Arc)outgoingArcsIt.next();
					Alphabet alpha = aut.getAlphabet();
					try
					{
						Event event = alpha.getEventWithId(arc.getEventId());
						pw.println("SEQTRANSITION " + aut.getName() + "_" + arc.getEventId() + " WAIT_FOR " + event.getLabel());
					}
					catch (Exception ex)
					{
						thisCategory.error("Failed getting event label. Code generation aborted.");
						return;
					}
					// Depth first, should get next state here?
					destState = arc.getToState();
					if (destState != initState)
					{
						pw.println("SEQSTEP " + aut.getName() + "_" + destState.getName());
					}
				}
				// temporary workaround
				if (currState.nbrOfOutgoingArcs() > 1)
				{
					pw.println("ENDALTERNATIVE");
					level = level - 1;
				}

			}
*/
/*			for (Iterator stateIt = aut.stateIterator(); stateIt.hasNext(); )
			{
				State sourceState = (State)stateIt.next();
				if (sourceState.isInitial())
				{
					pw.println("SEQINITSTEP " + aut.getName() + "_" + sourceState.getName());
				}
				else
				{
					pw.println("SEQSTEP " + aut.getName() + "_" + sourceState.getName());
				}

				for (Iterator outgoingArcsIt = sourceState.outgoingArcsIterator(); outgoingArcsIt.hasNext(); )
				{
					Arc arc = (Arc)outgoingArcsIt.next();
					if (sourceState.nbrOfOutgoingArcs() == 1)
					{
						Alphabet alpha = aut.getAlphabet();
						try
						{
							Event event = alpha.getEventWithId(arc.getEventId());
							pw.println("SEQTRANSITION " + aut.getName() + "_" + arc.getEventId() + " WAIT_FOR " + event.getLabel());
						}
						catch (Exception ex)
						{
							return;
						}

					}
					else
					{
						// It is easy to know when ALT.. starts, but not when it ends.
						// Easy way out is to fork. Ugly as hell, but will probably work.
						/*
						pw.println("ALTERNATIVESEQ");
						pw.println("ENDALTERNATIVE");
						State destState = arc.getToState();

					}
				}
			}
*/
			pw.println("ENDSEQUENCE\n\n");
		}

		// End of Module code

		pw.println("ENDDEF (*BasePicture*);");

		// End of BasePicture
	}

	public void serialize_g(PrintWriter pw)
	{
		pw.println("\" Syntax version 2.19, date: 2001-11-20-14:16:07.401 N \" ");

	}

	public void serialize_p(PrintWriter pw)
	{
		pw.println("DistributionData");
		pw.println(" ( Version \"Distributiondata version 1.0\" )");
		pw.println("SourceCodeSystems");
		pw.println(" (  )");
		pw.println("ExecutingSystems");
		pw.println(" (  )");


	}

	public void serialize_l(PrintWriter pw)
	{
		pw.println("");
	}

	private void straightSequenceOutput(Automaton theAutomaton, State theState, PrintWriter pw)
	{

		printStep(theAutomaton, theState, pw);

		if (theState.nbrOfOutgoingArcs() > 1)
		{
			pw.println("ALTERNATIVESEQ");
			//level = level + 1;
		}

		boolean firstArc = true;
		for (Iterator outgoingArcsIt = theState.outgoingArcsIterator(); outgoingArcsIt.hasNext(); )
		{
			if (firstArc)
			{
				firstArc = false;
			}
			else
			{
				pw.println("ALTERNATIVEBRANCH");
			}

			Arc arc = (Arc)outgoingArcsIt.next();
			printTransition(theAutomaton, arc, pw);

			State nextState = arc.getToState();
			// only straight sequences allowed as yet
			if (!nextState.isInitial())
			{
				straightSequenceOutput(theAutomaton, nextState, pw);
			}
			else
			{
				// End of this subsequence
			}
		}
	}

	private void printStep(Automaton theAutomaton, State theState, PrintWriter pw)
	{
		if (theState.isInitial())
		{
			pw.println("SEQINITSTEP " + theAutomaton.getName() + "_" + theState.getName());
		}
		else
		{
			pw.println("SEQSTEP " + theAutomaton.getName() + "_" + theState.getName());
		}
	}

	private void printTransition(Automaton theAutomaton, Arc theArc, PrintWriter pw)
	{
		Alphabet alpha = theAutomaton.getAlphabet();
		try
		{
			Event event = alpha.getEventWithId(theArc.getEventId());
			pw.println("SEQTRANSITION " + theAutomaton.getName() + "_" + theArc.getEventId() + " WAIT_FOR " + event.getLabel());
		}
		catch (Exception ex)
		{
			thisCategory.error("Failed getting event label. Code generation aborted.");
			return;
		}

	}

}
