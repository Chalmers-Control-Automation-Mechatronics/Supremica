
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

public class AutomataToControlBuilderSFC
	implements AutomataSerializer
{
	private static Logger logger = LoggerFactory.createLogger(AutomataToControlBuilderSFC.class);
	private String fileName = null;
	protected Automata automata;
	protected Automaton automaton;
	protected boolean debugMode = false;
	protected int transitionCounter = 0;
	protected int eventMonitorCounter = 0;
	protected int automatonCounter = 1;
	protected String coord = getCoord();
	protected String transitionConditionPrefix = getTransitionConditionPrefix();
	protected String transitionConditionSuffix = getTransitionConditionSuffix();
	protected String actionP1Prefix = getActionP1Prefix();
	protected String actionP1Suffix = getActionP1Suffix();
	protected String actionP0Prefix = getActionP0Prefix();
	protected String actionP0Suffix = getActionP0Suffix();

	public AutomataToControlBuilderSFC(Automata automata)
	{
		this.automata = automata;
	}

	public void serialize(String fileName)
	{    // Empty
	}

	public void serialize(PrintWriter pw)
	{    // Empty
	}

	public void serializeApp(File theFile, String filename)
	{
		try
		{
			FileWriter theWriter = new FileWriter(theFile);
			PrintWriter thePrintWriter = new PrintWriter(theWriter);
			String theFileName = theFile.getName();
			fileName = theFileName.substring(0, theFileName.length() - 4);
			serializeApp(thePrintWriter, filename);
			thePrintWriter.close();
		}
		catch (Exception ex)
		{
			logger.error("Exception while generating ControlBuilder code");
		}
	}

	public void serializeApp(PrintWriter pw, String filename)
	{

		// Start of file header
		Date theDate = new Date();

		// Should perhaps get current date and time, but how do I format it?
		//logger.info(theDate.toString());
		pw.println("HEADER SyntaxVersion_ '3.1' ChangedDate_ '2002-01-25-22:20:41.631'");
		pw.println("OfficialDate_ '2002-01-25-22:20:41.631'");
		pw.println("ProductVersion_ '2.2-0'");
		pw.println("FileName_ ''");
		pw.println("FileHistory_");
		pw.println("(* This source code unit was created 2002-01-25 22:20 by Supremica. *)");
		pw.println("ENDDEF");

		// End of file header
		// Start of Program invocation
		pw.println(filename);
		pw.println("Invocation ( 0.0 , 0.0 , 0.0 , 1.0 , 1.0 )");
		pw.println(": ROOT_MODULE");
		// Use generic Program1 for now
		pw.println("PROGRAM Program1 : SINGLE_PROGRAM");

		// Start of variable declarations
		pw.println("VAR");

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
		// #"@|*: Max identfier length (variable, step name etc) = 32.
		for (Iterator alphaIt = unionAlphabet.iterator(); alphaIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) alphaIt.next();

			if (currEvent.getLabel().length() > 32)
			{
				logger.warn("Event label " + currEvent.getLabel() + " too long. ControlBuilder's maximum identifier length is 32. CB will truncate, duplicates possible. (Please rename event label yourself.)");
			}

			pw.println(currEvent.getLabel().replace('.', '_') + " : bool;");
		}

		pw.println("END_VAR\n");

		// End of variable declarations.

		// Here comes the automata, the tricky part.
		for (Iterator automataIt = automata.iterator(); automataIt.hasNext(); )
		{

			// Each automaton is translated into a ControlBuilder Sequence.
			// A sequence has the following structure. Step - Transition - Step - Transition ...
			// A step may be followed by an ALTERNATIVSEQuence which has ALTERNATIVEBRANCHes.
			// This is the case if there is more than one transition from a state.
			// The difficulty is to know when the alternative branches merge, and if they do it the "ControlBuilder way".
			// A transition may be followed by a PARALLELSEQuence which has PARALLELBRANCHes.
			// This cannot happen for an automaton.
			Automaton aut = (Automaton) automataIt.next();

			aut.clearVisitedStates();

			transitionCounter = 1;

			if (aut.getName().length() > 28)
			{
				logger.warn("The name of automaton " + aut.getName() + " is too long. Identifiers are limited to 32 characters in ControlBuilder. The new name is Automaton_" + automatonCounter);
				aut.setName("Automaton_" + automatonCounter++);
			}

			// If there is _no_ ordinary, that is, non-fork, arc to the first step in drawing order it is an OPENSEQUENCE.
			// Is this reaaly correct? Won't check that for now ...
			// OPENSEQUENCE might not be supported in ControlBuilder
			// COORD must be same for all sequences? Should probably be obsoleted.
			State initState = aut.getInitialState();

			if (initState.nbrOfIncomingArcs() > 0)
			{
				pw.println("SEQUENCE " + aut.getName().replace('.', '_') + "  (SeqControl)" + coord);
			}
			else
			{
				pw.println("OPENSEQUENCE " + aut.getName().replace('.', '_') + "  (SeqControl)" + coord);
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

		// End of Program code
		pw.println("END_PROGRAM;\n");

		pw.println("ModuleDef");
		pw.println("ClippingBounds := ( -10.0 , -10.0 ) ( 10.0 , 10.0 )");
		pw.println("ZoomLimits := 0.0 0.01\n");
		// End of Module definition

		pw.println("END_MODULE");

	}

	public void serializePrj(File theFile, String filename)
		throws Exception
	{
		PrintWriter theWriter = new PrintWriter(new FileWriter(theFile));
		serializePrj(theWriter, filename);
		theWriter.close();
	}

	public void serializePrj(PrintWriter pw, String filename)
	{
		pw.println("'2002-01-11-16:24:38.775'");
		pw.println("Header");
		pw.println(" ( SyntaxVersion '3.0'");
		pw.println("   SavedDate '2002-01-11-16:47:35.825'");
		pw.println("   ChangedDate '2002-01-11-16:24:38.775'");
		pw.println("   FileName '" + filename + "'\n\n  )");
		pw.println("FileUnits");
		pw.println(" ( Application");
		pw.println("    ( Name '" + filename + "'");
		pw.println("      Directory '' ) )");
		pw.println("ControlSystem");
		pw.println(" ( Name\n Directory '' )");
		pw.println("ColorTable");
		pw.println(" ( ColorModel HLS\n )");
	}

	protected void generateEventMonitors(Automata theAutomata, PrintWriter pw)
	{

		// Step 1. Get alphabet
		Alphabet unionAlphabet = null;

		try
		{
			unionAlphabet = AlphabetHelpers.getUnionAlphabet(theAutomata);
		}
		catch (Exception ex)
		{
			logger.error("Failed getting union of alphabets of the selected automata. Code generation aborted.");

			return;
		}

		Alphabet testAlphabet = new Alphabet(unionAlphabet);

		// Step 2. Pick an event
		for (Iterator alphaIt = unionAlphabet.iterator(); alphaIt.hasNext(); )
		{
			LabeledEvent theEvent = (LabeledEvent) alphaIt.next();

			if (testAlphabet.containsEventWithLabel(theEvent.getLabel()))
			{

				// Step 3. Compute ExtendedConflict(event)
				logger.debug(theEvent.getLabel());

				Alphabet extConfAlphabet = extendedConflict(theAutomata, theEvent, testAlphabet);

				testAlphabet.minus(extConfAlphabet);
				logger.debug(Integer.toString(testAlphabet.size()));

				// Step 4. Compute EventMonitor()
				printEventMonitor(theAutomata, extConfAlphabet, pw);
			}
		}    // Step 5. Terminate if event set exhausted
	}

	protected Alphabet extendedConflict(Automata theAutomata, LabeledEvent theEvent, Alphabet iteratorAlphabet)
	{

		// Step 1. Initialise. C = {theEvent}, D = empty.
		Alphabet theExtConfAlphabet = new Alphabet();
		Alphabet testAlphabet = new Alphabet();

		try
		{
			theExtConfAlphabet.addEvent(theEvent);
		}
		catch (Exception ex)
		{

			// This should not happen since theExtConfAlphabet is empty.
			logger.error("Failed adding event when computing extended conflict. Code generation erroneous");

			return theExtConfAlphabet;
		}

		boolean ready = false;

		while (!ready)
		{

			// Step 2. Pick e in C \ D.
			for (Iterator alphaIt = iteratorAlphabet.iterator(); alphaIt.hasNext(); )
			{
				LabeledEvent confEvent = (LabeledEvent) alphaIt.next();

				if (theExtConfAlphabet.containsEventWithLabel(confEvent.getLabel()) &&!testAlphabet.containsEventWithLabel(confEvent.getLabel()))
				{

					// Step 3. Let C = C + Conflict(e), D = D + {e}.
					Alphabet conflictAlphabet = computeConflict(theAutomata, confEvent);

					theExtConfAlphabet.plus(conflictAlphabet);

					try
					{
						testAlphabet.addEvent(confEvent);
					}
					catch (Exception ex)
					{

						// This should not happen since testAlphabet didn't contain the event.
						logger.error("Failed adding event when computing extended conflict. Code generation erroneous");

						return theExtConfAlphabet;
					}
				}
			}

			// Step 4. If C = D return, else repeat from step 2.
			if (theExtConfAlphabet.size() == testAlphabet.size())
			{
				ready = true;

				logger.debug("Finished computing extended conflict");
			}
		}

		return theExtConfAlphabet;
	}

	protected Alphabet computeConflict(Automata theAutomata, LabeledEvent theEvent)
	{
		Alphabet confAlphabet = new Alphabet();

		try
		{
			confAlphabet.addEvent(theEvent);
		}
		catch (Exception ex)
		{

			// This should not happen since confAlphabet is empty.
			logger.error("Failed adding event when computing conflict.");

			return confAlphabet;
		}

		// Iterera över automaterna, finns händelsen i en automat så måste vi hitta
		// samtliga tillstånd som har en övergång med händelsen. För varje tillstånd
		// itereras över utgående bågar för att hitta motsvarande händelser.
		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton aut = (Automaton) autIt.next();

			logger.debug(aut.getName().replace('.', '_'));

			if (aut.containsEventWithLabel(theEvent.getLabel()))
			{
				logger.debug("The event " + theEvent.getLabel() + " exsits in the automaton " + aut.getName().replace('.', '_'));

				// The event exists in this automaton. What arcs?
				for (Iterator arcIt = aut.arcIterator(); arcIt.hasNext(); )
				{
					Arc anArc = (Arc) arcIt.next();

					try
					{
						LabeledEvent arcEvent = (LabeledEvent) aut.getEvent(anArc.getEventId());

						if (arcEvent.getLabel().equals(theEvent.getLabel()))
						{
							logger.debug("Event " + theEvent.getLabel() + " labels arc");

							// The event labels this arc. Get conflicting arcs.
							State sourceState = (State) anArc.getFromState();

							if (!sourceState.isVisited())
							{

								// It is only necessary to get the conflicting transitions for this state once?
								sourceState.setVisited(true);

								for (Iterator outgoingIt = sourceState.outgoingArcsIterator(); outgoingIt.hasNext(); )
								{
									Arc currArc = (Arc) outgoingIt.next();

									try
									{
										LabeledEvent currArcEvent = (LabeledEvent) aut.getEvent(currArc.getEventId());
										Alphabet dummyAlphabet = new Alphabet();

										try
										{
											dummyAlphabet.addEvent(currArcEvent);
											logger.debug("Event " + currArcEvent.getLabel() + " is in conflict with " + theEvent.getLabel());
											confAlphabet.plus(dummyAlphabet);
										}
										catch (Exception ex)
										{

											// This should not happen since dummyAlphabet is empty.
											logger.error("Failed adding event when computing conflict.");

											return confAlphabet;
										}
									}
									catch (Exception ex)
									{

										// This should not happen since the event exists in the automaton.
										logger.error("Failed getting event label. Code generation erroneous.");

										return confAlphabet;
									}
								}
							}
						}
					}
					catch (Exception ex)
					{

						// This should not happen since the event exists in the automaton.
						logger.error("Failed getting event label. Code generation erroneous.");

						return confAlphabet;
					}
				}
			}
		}

		return confAlphabet;
	}

	protected void printEventMonitor(Automata theAutomata, Alphabet theAlphabet, PrintWriter pw)
	{

		// Step 1. Initialise. Create initial step
		int stepCounter = 0;
		boolean firstEvent = true;

		transitionCounter = 1;

		// eventMonitorCounter++;
		pw.println("SEQUENCE EventMonitor_" + ++eventMonitorCounter + coord);
		pw.println("SEQINITSTEP EM" + eventMonitorCounter + "_" + stepCounter++);

		// Step 2. For each event e in theAlphabet
		if (theAlphabet.size() > 1)
		{
			pw.println("ALTERNATIVESEQ");
		}

		for (Iterator eventIt = theAlphabet.iterator(); eventIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) eventIt.next();

			if (firstEvent)
			{
				firstEvent = false;
			}
			else
			{
				pw.println("ALTERNATIVEBRANCH");
			}

			// (a) Create transition t with t.C = preset()
			String transitionCondition = computeGenerationCondition(theAutomata, currEvent);

			pw.println("SEQTRANSITION EM" + eventMonitorCounter + "_Tr" + transitionCounter++ + transitionConditionPrefix + transitionCondition + transitionConditionSuffix);

			// (b) Create step with action e
			pw.println("SEQSTEP EM" + eventMonitorCounter + "_" + stepCounter++);
			pw.println(actionP1Prefix + currEvent.getLabel().replace('.', '_') + " := True;" + actionP1Suffix);
			pw.println(actionP0Prefix + currEvent.getLabel().replace('.', '_') + " := False;" + actionP0Suffix);

			// (c) Create transition t' with t'.C = not preset()
			transitionCondition = computeCeaseCondition(theAutomata, currEvent);

			pw.println("SEQTRANSITION EM" + eventMonitorCounter + "_Tr" + transitionCounter++ + transitionConditionPrefix + transitionCondition + transitionConditionSuffix);
		}

		if (theAlphabet.size() > 1)
		{
			pw.println("ENDALTERNATIVE");
		}

		pw.println("ENDSEQUENCE\n\n");
		logger.debug("Printing Event Monitor");
	}

	protected String computeGenerationCondition(Automata theAutomata, LabeledEvent theEvent)
	{
		StringBuffer theCondition = new StringBuffer();
		boolean firstAutomaton = true;
		boolean nextAutomaton = false;

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton aut = (Automaton) autIt.next();

			if (aut.containsEventWithLabel(theEvent.getLabel()))
			{

				// The event exists in this automaton
				logger.debug("The event " + theEvent.getLabel() + " exists in " + aut.getName().replace('.', '_'));

				boolean stateFound = false;
				boolean firstState = true;

				for (Iterator arcIt = aut.arcIterator(); arcIt.hasNext(); )
				{
					Arc anArc = (Arc) arcIt.next();

					try
					{
						LabeledEvent arcEvent = (LabeledEvent) aut.getEvent(anArc.getEventId());

						if (arcEvent.getLabel().equals(theEvent.getLabel()))
						{

							// The event labels this arc. Get preset (a singleton!).
							logger.debug("The event labels arc");

							stateFound = true;

							State sourceState = (State) anArc.getFromState();

							if (firstAutomaton)
							{
								firstAutomaton = false;
							}
							else if (nextAutomaton)
							{
								nextAutomaton = false;

								theCondition.append(" AND ");
							}

							if (firstState)
							{
								firstState = false;

								theCondition.append("(" + aut.getName().replace('.', '_') + "__" + sourceState.getId() + ".X");
								logger.debug("Current transition condition: " + theCondition);
							}
							else
							{
								theCondition.append(" OR " + aut.getName().replace('.', '_') + "__" + sourceState.getId() + ".X");
							}
						}
					}
					catch (Exception ex)
					{

						// This should not happen since the event exists in the automaton.
						logger.error("Failed getting event label. Code generation erroneous.");

						return theCondition.toString();
					}
				}

				if (!stateFound)
				{
					return "False";
				}
				else
				{
					theCondition.append(")");
				}

				nextAutomaton = true;
			}
		}

		return theCondition.toString();
	}

	protected String computeCeaseCondition(Automata theAutomata, LabeledEvent theEvent)
	{
		StringBuffer theCondition = new StringBuffer();
		boolean firstAutomaton = true;
		boolean nextAutomaton = false;

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton aut = (Automaton) autIt.next();

			if (aut.containsEventWithLabel(theEvent.getLabel()))
			{

				// The event exists in this automaton
				logger.debug("The event " + theEvent.getLabel() + " exists in " + aut.getName().replace('.', '_'));

				boolean stateFound = false;
				boolean firstState = true;

				for (Iterator arcIt = aut.arcIterator(); arcIt.hasNext(); )
				{
					Arc anArc = (Arc) arcIt.next();

					try
					{
						LabeledEvent arcEvent = (LabeledEvent) aut.getEvent(anArc.getEventId());

						if (arcEvent.getLabel().equals(theEvent.getLabel()))
						{

							// The event labels this arc. Get preset (a singleton!).
							logger.debug("The event labels arc");

							stateFound = true;

							State sourceState = (State) anArc.getFromState();

							if (firstAutomaton)
							{
								firstAutomaton = false;

								theCondition.append("NOT (");
							}
							else if (nextAutomaton)
							{
								nextAutomaton = false;

								theCondition.append(" OR ");
							}

							if (firstState)
							{
								firstState = false;

								theCondition.append("(" + aut.getName().replace('.', '_') + "__" + sourceState.getId() + ".X");
								logger.debug("Current transition condition: " + theCondition);
							}
							else
							{
								theCondition.append(" AND " + aut.getName().replace('.', '_') + "__" + sourceState.getId() + ".X");
							}
						}
					}
					catch (Exception ex)
					{

						// This should not happen since the event exists in the automaton.
						logger.error("Failed getting event label. Code generation erroneous.");

						return theCondition.toString();
					}
				}

				if (!stateFound)
				{
					return "False";
				}
				else
				{
					theCondition.append(")");
				}

				nextAutomaton = true;
			}
		}

		theCondition.append(")");

		return theCondition.toString();
	}

	protected void printSequence(Automaton theAutomaton, State theState, PrintWriter pw)
	{
		printStep(theAutomaton, theState, pw);
		theState.setVisited(true);

		int endAlternativeLevel = 0;
		boolean alternativeEnded = false;

		if (theState.nbrOfOutgoingArcs() > 1)
		{
			pw.println("ALTERNATIVESEQ");

			endAlternativeLevel = theState.nbrOfOutgoingArcs();
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

				endAlternativeLevel--;

				// logger.debug("endAlternativeLevel = " + endAlternativeLevel);
			}

			Arc arc = (Arc) outgoingArcsIt.next();

			printTransition(theAutomaton, arc, pw);

			State nextState = arc.getToState();

			if (!nextState.isVisited())
			{
				printSequence(theAutomaton, nextState, pw);
			}
			else if (!nextState.isInitial())
			{
				printFork(theAutomaton, nextState, pw);
			}
			else if (endAlternativeLevel == 1)
			{
				pw.println("ENDALTERNATIVE");    // End of this subsequence

				alternativeEnded = true;

				// logger.debug("EndAlternative");
			}
		}

		if ((endAlternativeLevel == 1) &&!alternativeEnded)
		{
			pw.println("ENDALTERNATIVE");    // End of this subsequence

			// logger.debug("EndAlternative");
		}
	}

	protected void printStep(Automaton theAutomaton, State theState, PrintWriter pw)
	{
		if (theState.isInitial())
		{
			pw.println("SEQINITSTEP " + theAutomaton.getName().replace('.', '_') + "__" + theState.getId());
		}
		else
		{
			pw.println("SEQSTEP " + theAutomaton.getName().replace('.', '_') + "__" + theState.getId());
		}
	}

	protected void printTransition(Automaton theAutomaton, Arc theArc, PrintWriter pw)
	{
		try
		{
			LabeledEvent event = theAutomaton.getEvent(theArc.getEventId());

			pw.println("SEQTRANSITION " + theAutomaton.getName().replace('.', '_') + "_Tr" + transitionCounter++ + transitionConditionPrefix + event.getLabel().replace('.', '_') + transitionConditionSuffix);
		}
		catch (Exception ex)
		{
			logger.error("Failed getting event label. Code generation aborted.");

			return;
		}
	}

	protected void printFork(Automaton theAutomaton, State theState, PrintWriter pw)
	{
		pw.println("SEQFORK " + theAutomaton.getName().replace('.', '_') + "__" + theState.getId() + " SEQBREAK");
	}

	protected String getTransitionConditionPrefix()
	{
		return " TRANSITIONCODEBLOCK\nSTRUCTUREDTEXT\n";
	}

	protected String getTransitionConditionSuffix()
	{
		return "\nEND_CODEBLOCK";
	}

	protected String getCoord()
	{
		return " COORD 0.0, 0.0 OBJSIZE 1.0, 1.0";
	}

	protected String getActionP1Prefix()
	{
		return "ENTERCODEBLOCK STRUCTUREDTEXT\n";
	}

	protected String getActionP1Suffix()
	{
		return "\nEND_CODEBLOCK";
	}

	protected String getActionP0Prefix()
	{
		return "EXITCODEBLOCK STRUCTUREDTEXT\n";
	}

	protected String getActionP0Suffix()
	{
		return "\nEND_CODEBLOCK";
	}
}
