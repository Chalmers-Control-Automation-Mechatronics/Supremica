
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

import org.supremica.gui.*;
import org.supremica.log.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;
import java.io.*;
import java.util.*;

public class AutomataToSattLineSFC
	extends AutomataToControlBuilderSFC
{
	private static Logger logger = LoggerFactory.createLogger(AutomataToSattLineSFC.class);

	public AutomataToSattLineSFC(Automata automata)
	{
		super(automata);
	}

	public void serialize(String filename)
	{    // Empty
	}

	public void serialize(PrintWriter pw)
	{    // Empty
	}

	public void serialize_s(PrintWriter pw)
	{

		// Start of file header
		Date theDate = new Date();

		//logger.info(theDate.toString());
		pw.println("\"Syntax version 2.19, date: 2001-08-10-10:42:24.724 N\"");
		pw.println("\"Original file date: ---\"");
		pw.print("\"Program date: 2001-08-10-10:42:24.724, name: ");    // Should perhaps get current date and time

		if (automata.getName() != null)
		{
			pw.println(" " + automata.getName().replace('.', '_') + " \"");
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
		pw.println("    ) : MODULEDEFINITION DateCode_ 492916896 ( GroupConn = ProgStationData.");    // Don't know importance of DateCode
		pw.println("GroupProgFast )\n");

		// Start of variable declarations
		pw.println("LOCALVARIABLES");

		Alphabet unionAlphabet = null;

		try
		{
			unionAlphabet = AlphabetHelpers.getUnionAlphabet(automata);
		}
		catch (Exception ex)
		{
			logger.error("Failed getting union of alphabets of the selected automata. Code generation aborted.");

			return;
		}

		// . is not allowed in simple variable names, replaced with _
		// #"@|*: Max line length = 140, max identfier length (variable, step name etc) = 20.
		// Too lazy to fix this now. Issue warning instead...
		boolean firstEvent = true;
		int lineLength = 0;

		for (Iterator alphaIt = unionAlphabet.iterator(); alphaIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) alphaIt.next();

			if (currEvent.getLabel().length() > 20)
			{
				logger.warn("Event label " + currEvent.getLabel() + " too long. SattLine's maximum identifier length is 20. (Please rename event label yourself.)");
			}

			if (firstEvent)
			{
				firstEvent = false;

				pw.print(currEvent.getLabel().replace('.', '_'));
			}
			else
			{
				pw.print(", " + currEvent.getLabel().replace('.', '_'));
			}

			lineLength = lineLength + currEvent.getLabel().length() + 2;

			if (lineLength > 80)
			{
				pw.print("\n");

				lineLength = 0;
			}
		}

		pw.println(": boolean;");
		pw.println("ProgStationData: ProgStationData;\n");

		// End of variable declarations.
		// Start of submodule invocations
		pw.println("SUBMODULES");
		pw.println("ProgStationControl1 Invocation");
		pw.println("( 1.18 , 0.72 , 0.0 , 0.1 , 0.1 ) : ProgStationControl;");

		// End of submodule invocations
		// Start of Module definition.
		pw.println("ModuleDef");
		pw.println("ClippingBounds = ( -10.0 , -10.0 ) ( 10.0 , 10.0 )");
		pw.println("ZoomLimits = 0.0 0.01\n");

		// End of Module definition
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
			Automaton aut = (Automaton) automataIt.next();

			aut.clearVisitedStates();

			transitionCounter = 1;

			if (aut.getName().length() > 16)
			{
				logger.warn("The name of automaton " + aut.getName() + " is too long. Identifiers are limited to 20 characters in SattLine. The new name is Automaton_" + automatonCounter);
				aut.setName("Automaton_" + automatonCounter++);
			}

			// If there is _no_ ordinary, that is, non-fork, arc to the first step in drawing order it is an OPENSEQUENCE.
			// Won't check that for now ...
			// Might want to parameterise COORD so that sequences are not drawn on top of each other.
			State initState = aut.getInitialState();

			if (initState.nbrOfIncomingArcs() > 0)
			{
				pw.println("SEQUENCE " + aut.getName().replace('.', '_') + coord);
			}
			else
			{
				pw.println("OPENSEQUENCE " + aut.getName().replace('.', '_') + coord);
			}

			printSequence(aut, initState, pw);
			aut.clearVisitedStates();

			if (initState.nbrOfIncomingArcs() > 0)
			{
				pw.println("ENDSEQUENCE\n\n");
			}
			else
			{
				pw.println("ENDOPENSEQUENCE\n\n");
			}
		}    // End of automata conversion

		// Event Monitors should be generated here.
		generateEventMonitors(automata, pw);

		// End of Module code
		pw.println("ENDDEF (*BasePicture*);");

		// End of BasePicture
	}

	public void serialize_g(PrintWriter pw)
	{
		pw.println("\" Syntax version 2.19, date: 2001-08-10-10:42:24.724 N \" ");
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
		pw.println("nucleuslib");
	}

	protected String getTransitionConditionPrefix()
	{
		return " WAIT_FOR ";
	}

	protected String getTransitionConditionSuffix()
	{
		return "";
	}
	protected String getCoord()
	{
		// Should perhaps parameterise COORD
		return " COORD -0.5, 0.5 OBJSIZE 0.5, 0.5";
	}

	protected String getActionP1Prefix()
	{
		return "ENTERCODE";
	}

	protected String getActionP1Suffix()
	{
		return "";
	}

	protected String getActionP0Prefix()
	{
		return "EXITCODE";
	}

	protected String getActionP0Suffix()
	{
		return "";
	}
}
