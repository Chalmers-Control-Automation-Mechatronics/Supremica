
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
import org.supremica.automata.EventLabel;
import java.io.*;
import java.util.*;

public class AutomataToSattLineSFC
	implements AutomataSerializer
{
	private static Logger logger = LoggerFactory.createLogger(AutomataToSattLineSFC.class);
	private Automata automata;
	private Automaton automaton;
	private boolean debugMode = false;
	private int transitionCounter = 0;
	private int eventMonitorCounter = 0;
	private int automatonCounter = 1;

	public AutomataToSattLineSFC(Automata automata)
	{
		this.automata = automata;
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

		logger.info(theDate.toString());
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
			EventLabel currEvent = (EventLabel) alphaIt.next();

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
				pw.println("SEQUENCE " + aut.getName().replace('.', '_') + " COORD -0.5, 0.5 OBJSIZE 0.5, 0.5");
			}
			else
			{
				pw.println("OPENSEQUENCE " + aut.getName().replace('.', '_') + " COORD -0.5, 0.5 OBJSIZE 0.5, 0.5");
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

	private void generateEventMonitors(Automata theAutomata, PrintWriter pw)
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
			EventLabel theEvent = (EventLabel) alphaIt.next();

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

	private Alphabet extendedConflict(Automata theAutomata, EventLabel theEvent, Alphabet iteratorAlphabet)
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
				EventLabel confEvent = (EventLabel) alphaIt.next();

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

	private Alphabet computeConflict(Automata theAutomata, EventLabel theEvent)
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
						EventLabel arcEvent = (EventLabel) aut.getEvent(anArc.getEventId());

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
										EventLabel currArcEvent = (EventLabel) aut.getEvent(currArc.getEventId());
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

	private void printEventMonitor(Automata theAutomata, Alphabet theAlphabet, PrintWriter pw)
	{

		// Step 1. Initialise. Create initial step
		int stepCounter = 0;
		boolean firstEvent = true;

		transitionCounter = 1;

		// eventMonitorCounter++;
		pw.println("SEQUENCE EventMonitor_" + ++eventMonitorCounter + " COORD -0.5, 0.5 OBJSIZE 0.5, 0.5");
		pw.println("SEQINITSTEP EM" + eventMonitorCounter + "_" + stepCounter++);

		// Step 2. For each event e in theAlphabet
		if (theAlphabet.size() > 1)
		{
			pw.println("ALTERNATIVESEQ");
		}

		for (Iterator eventIt = theAlphabet.iterator(); eventIt.hasNext(); )
		{
			EventLabel currEvent = (EventLabel) eventIt.next();

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

			pw.println("SEQTRANSITION EM" + eventMonitorCounter + "_Tr" + transitionCounter++ + " WAIT_FOR " + transitionCondition);

			// (b) Create step with action e
			pw.println("SEQSTEP EM" + eventMonitorCounter + "_" + stepCounter++);
			pw.println("ENTERCODE");
			pw.println(currEvent.getLabel().replace('.', '_') + " = True;");
			pw.println("EXITCODE");
			pw.println(currEvent.getLabel().replace('.', '_') + " = False;");

			// (c) Create transition t' with t'.C = not preset()
			transitionCondition = computeCeaseCondition(theAutomata, currEvent);

			pw.println("SEQTRANSITION EM" + eventMonitorCounter + "_Tr" + transitionCounter++ + " WAIT_FOR " + transitionCondition);
		}

		if (theAlphabet.size() > 1)
		{
			pw.println("ENDALTERNATIVE");
		}

		pw.println("ENDSEQUENCE\n\n");
		logger.debug("Printing Event Monitor");
	}

	private String computeGenerationCondition(Automata theAutomata, EventLabel theEvent)
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
						EventLabel arcEvent = (EventLabel) aut.getEvent(anArc.getEventId());

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

	private String computeCeaseCondition(Automata theAutomata, EventLabel theEvent)
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
						EventLabel arcEvent = (EventLabel) aut.getEvent(anArc.getEventId());

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

	private void printSequence(Automaton theAutomaton, State theState, PrintWriter pw)
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

	private void printStep(Automaton theAutomaton, State theState, PrintWriter pw)
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

	private void printTransition(Automaton theAutomaton, Arc theArc, PrintWriter pw)
	{
		try
		{
			EventLabel event = theAutomaton.getEvent(theArc.getEventId());

			pw.println("SEQTRANSITION " + theAutomaton.getName().replace('.', '_') + "_Tr" + transitionCounter + " WAIT_FOR " + event.getLabel().replace('.', '_'));

			transitionCounter++;
		}
		catch (Exception ex)
		{
			logger.error("Failed getting event label. Code generation aborted.");

			return;
		}
	}

	private void printFork(Automaton theAutomaton, State theState, PrintWriter pw)
	{
		pw.println("SEQFORK " + theAutomaton.getName().replace('.', '_') + "__" + theState.getId() + " SEQBREAK");
	}
}
