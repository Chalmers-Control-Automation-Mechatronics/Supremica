
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
package org.supremica.automata.IO;

import org.supremica.log.*;
import org.supremica.automata.*;
import java.io.*;
import java.util.*;

public class AutomataToSattLineSFCForBallProcess
	extends AutomataToSattLineSFC
{
	private static Logger logger = LoggerFactory.createLogger(AutomataToSattLineSFCForBallProcess.class);

	public AutomataToSattLineSFCForBallProcess(Project theProject)
	{
		this(theProject, (BallProcessHelper)BallProcessHelper.getInstance());
	}

	public AutomataToSattLineSFCForBallProcess(Project theProject, IEC61131Helper theHelper)
	{
		super(theProject, theHelper);
	}

	public void serialize(String filename)
	{    // Empty
	}

	public void serialize(PrintWriter pw)
	{    // Empty
	}

	public void serialize_s(File theFile, String filename)
		throws Exception
	{
		PrintWriter theWriter = new PrintWriter(new FileWriter(theFile));
		/* Macro for printing of Ball Process type definitions, variables
		and the LabIO sub module handling the interaction with the physical
		Ball Process. We assume that there are no user defined variables.
		Otherwise, they can be declared later. */
		theHelper.printBeginProgram(theWriter, filename);

		/* Here should the user program be handled.
		 Here comes the automata, implemented in AutomataToControlBuilderSFC.
		 Note, timers are not yet supported. Can probably be implemented using
		 the cost of a state, or a special uncontrollable event 'timer'. */
		automatonConverter(theProject, theWriter);

		// Event Monitors should be generated here.
		// Implemented in AutomataToControlBuilderSFC.
		generateEventMonitors(theProject, theWriter);

		// Printing end of file line(s).
		theHelper.printEndProgram(theWriter);
		theWriter.close();
	}

	public void serialize_g(File theFile, String filename)
		throws Exception
	{
		PrintWriter theWriter = new PrintWriter(new FileWriter(theFile));
		((BallProcessHelper) theHelper).printGFile(theWriter, filename);
		theWriter.close();
	}
	public void serialize_l(File theFile, String filename)
		throws Exception
	{
		PrintWriter theWriter = new PrintWriter(new FileWriter(theFile));
		((BallProcessHelper) theHelper).printLFile(theWriter, filename);
		theWriter.close();
	}
	public void serialize_p(File theFile, String filename)
		throws Exception
	{
		PrintWriter theWriter = new PrintWriter(new FileWriter(theFile));
		((BallProcessHelper) theHelper).printPFile(theWriter, filename);
		theWriter.close();
	}

	protected void printEventMonitorAction(LabeledEvent theEvent, PrintWriter pw)
	{
		/* We should not replace '.' with '_'. Needed for IP.xxx and (RE)SET_OP.yyy.
		 No other such events are assumed. Petter must make sure of that in his
		 PM for the assignment. */
		pw.println(theHelper.getActionP1Prefix() + theEvent.getLabel() +  theHelper.getAssignmentOperator() + "True;" + theHelper.getActionP1Suffix());
		pw.println(theHelper.getActionP0Prefix() + theEvent.getLabel() +  theHelper.getAssignmentOperator() + "False;" + theHelper.getActionP0Suffix());
	}

	protected String computeGenerationCondition(Project theProject, Alphabet theExtConfAlphabet, LabeledEvent theEvent)
	{
		StringBuffer theCondition = new StringBuffer();
		boolean firstAutomaton = true;
		boolean nextAutomaton = false;
		int lineLength = 0;

		/* We create the uncontrollable disablement condition first. */
		if (theExtConfAlphabet.nbrOfUncontrollableEvents() > 0)
		{
			String theUcCondition = ucDisablementCondition(theExtConfAlphabet);
			theCondition.append(theUcCondition);
		}

		if (theCondition.lastIndexOf("\n") == -1)
		{
			lineLength = 31 + theCondition.length();
		}
		else
		{
			lineLength = theCondition.length() - theCondition.lastIndexOf("\n");
		}

		/* And then the actual generation condition. */
		for (Iterator autIt = theProject.iterator(); autIt.hasNext(); )
		{
			Automaton aut = (Automaton) autIt.next();
			Alphabet theAlphabet = aut.getAlphabet();

			if (theAlphabet.containsEventWithLabel(theEvent.getLabel()))
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
						LabeledEvent arcEvent = anArc.getEvent(); // (LabeledEvent) aut.getEvent(anArc.getEventId());

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
								lineLength = lineLength + 5;
							}

							if (firstState)
							{
								firstState = false;

								theCondition.append("(" + aut.getName().replace('.', '_') + "__" + sourceState.getName() + ".X");
								lineLength = lineLength + 5 + aut.getName().length() + sourceState.getName().length();
								logger.debug("Current transition condition: " + theCondition);
							}
							else
							{
								theCondition.append(" OR " + aut.getName().replace('.', '_') + "__" + sourceState.getName() + ".X");
								lineLength = lineLength + 8 + aut.getName().length() + sourceState.getName().length();
							}
						}
					}
					catch (Exception ex)
					{

						// This should not happen since the event exists in the automaton.
						logger.error("Failed getting event label. Code generation erroneous. " + ex);
						logger.debug(ex.getStackTrace());
						return theCondition.toString();
					}
					if (lineLength > 100)
					{
						theCondition.append("\n");
						lineLength = 0;
					}
				}

				if (!stateFound)
				{
					return "False";
				}
				else
				{
					theCondition.append(")");
					lineLength = lineLength + 1;
				}

				nextAutomaton = true;
			}
		}

		return theCondition.toString();
	}

	protected String ucDisablementCondition(Alphabet theAlphabet)
	{
		/* Must fix 140 character line length limit. There are at least 31
		 characters before the transition condition. There are none after it.
		 Let's make the transition condition limit 100 characters.
		 We should furthermore not replace '.' with '_'.
		 And we should preferably not include the timer event,
		 but that's a tad bit more difficult since we don't have
		 its precondition. Even so, it may not be useable since
		 the step timer variable retains its value until the step
		 is reactivated. Maybe we should just ignore it. */

		StringBuffer theCondition = new StringBuffer("");

		/* QUICK AND DIRTY HACK preventing locking behaviour: !event AND NOT !event.
		   This is safe assuming that the event monitors remain last in execution order. */

		/*boolean firstUcEvent = true;
		theCondition.append("NOT (");
		int lineLength = 36; // 31 + 5

		for (Iterator ucEventIt = theAlphabet.uncontrollableEventIterator(); ucEventIt.hasNext(); )
		{
			LabeledEvent theUcEvent = (LabeledEvent) ucEventIt.next();
			if (firstUcEvent)
			{
				firstUcEvent = false;
			}
			else
			{
				theCondition.append(" OR ");
				lineLength = lineLength + 4;
			}
			if (theUcEvent.getLabel().equalsIgnoreCase("timer"))
			{
				theCondition.append("False");
			}
			else
			{
				theCondition.append(theUcEvent.getLabel());
				lineLength = lineLength + theUcEvent.getLabel().length();
			}
			if (lineLength > 100)
			{
				theCondition.append("\n");
				lineLength = 0;
			}
		}
		theCondition.append(") AND ");*/
		return theCondition.toString();
	}

	protected String computeCeaseCondition(Project theProject, LabeledEvent theEvent)
	{
		StringBuffer theCondition = new StringBuffer();
		boolean firstAutomaton = true;
		boolean nextAutomaton = false;
		int lineLength = 31;

		/* This version takes care of line length limits and assumes that the
		   ordering of the SFCs doesn't change. Then we can allow self loops.
		   In general, that is not the case for IT/DA PLCs, such as Satt Line. */

		for (Iterator autIt = theProject.iterator(); autIt.hasNext(); )
		{
			Automaton aut = (Automaton) autIt.next();
			Alphabet theAlphabet = aut.getAlphabet();

			if (theAlphabet.containsEventWithLabel(theEvent.getLabel()))
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
						LabeledEvent arcEvent = anArc.getEvent(); // (LabeledEvent) aut.getEvent(anArc.getEventId());

						if (arcEvent.getLabel().equals(theEvent.getLabel()))
						{

							// The event labels this arc. Get preset (a singleton!).
							logger.debug("The event labels arc");

							stateFound = true;

							// State sourceState = (State) anArc.getFromState(); Use postset instead.
							State destinationState = (State) anArc.getToState();

							if (firstAutomaton)
							{
								firstAutomaton = false;

								// theCondition.append("NOT ("); Not necessary since postset
								// lineLength = lineLength + 5;
							}
							else if (nextAutomaton)
							{
								nextAutomaton = false;

								// theCondition.append(" OR ");
								// lineLength = lineLength + 4;
								theCondition.append(" AND ");
								lineLength = lineLength + 5;
							}

							if (firstState)
							{
								firstState = false;

								// theCondition.append("(" + aut.getName().replace('.', '_') + "__" + sourceState.getName() + ".X");
								// lineLength = lineLength + 5 + aut.getName().length() + sourceState.getName().length();
								theCondition.append("(" + aut.getName().replace('.', '_') + "__" + destinationState.getName() + ".X");
								lineLength = lineLength + 5 + aut.getName().length() + destinationState.getName().length();
								logger.debug("Current transition condition: " + theCondition);
							}
							else
							{
								// theCondition.append(" AND " + aut.getName().replace('.', '_') + "__" + sourceState.getName() + ".X");
								// lineLength = lineLength + 9 + aut.getName().length() + sourceState.getName().length();
								theCondition.append(" OR " + aut.getName().replace('.', '_') + "__" + destinationState.getName() + ".X");
								lineLength = lineLength + 8 + aut.getName().length() + destinationState.getName().length();
							}
						}
					}
					catch (Exception ex)
					{

						// This should not happen since the event exists in the automaton.
						logger.error("Failed getting event label. Code generation erroneous. " + ex);
						logger.debug(ex.getStackTrace());
						return theCondition.toString();
					}
					if (lineLength > 100)
					{
						theCondition.append("\n");
						lineLength = 0;
					}
				}

				if (!stateFound)
				{
					return "False";
				}
				else
				{
					theCondition.append(")");
					lineLength = lineLength + 1;
				}

				nextAutomaton = true;
			}
		}

		// theCondition.append(")");
		theCondition.append("");

		return theCondition.toString();
	}

	protected void printTransition(Automaton theAutomaton, Arc theArc, PrintWriter pw)
	{
		/* Again, we should not replace '.' with '_' in event labels. */
		try
		{
			LabeledEvent event = theArc.getEvent(); // theAutomaton.getEvent(theArc.getEventId());

			if (event.getLabel().equalsIgnoreCase("timer"))
			{
				pw.println("SEQTRANSITION " + theAutomaton.getName().replace('.', '_') + "_Tr" + transitionCounter++ + theHelper.getTransitionConditionPrefix() + theAutomaton.getName().replace('.', '_') + "__" + theArc.getFromState().getName() + ".T > 1000" + theHelper.getTransitionConditionSuffix());
			}
			else if (event.getLabel().equalsIgnoreCase("timermätlyft"))
			{
				pw.println("SEQTRANSITION " + theAutomaton.getName().replace('.', '_') + "_Tr" + transitionCounter++ + theHelper.getTransitionConditionPrefix() + theAutomaton.getName().replace('.', '_') + "__" + theArc.getFromState().getName() + ".T > 1000" + theHelper.getTransitionConditionSuffix());
			}
			else if (event.getLabel().equalsIgnoreCase("timerhiss"))
			{
				pw.println("SEQTRANSITION " + theAutomaton.getName().replace('.', '_') + "_Tr" + transitionCounter++ + theHelper.getTransitionConditionPrefix() + theAutomaton.getName().replace('.', '_') + "__" + theArc.getFromState().getName() + ".T > 1000" + theHelper.getTransitionConditionSuffix());
			}
			else
			{
				pw.println("SEQTRANSITION " + theAutomaton.getName().replace('.', '_') + "_Tr" + transitionCounter++ + theHelper.getTransitionConditionPrefix() + event.getLabel() + theHelper.getTransitionConditionSuffix());
			}
		}
		catch (Exception ex)
		{
			logger.error("Failed getting event label. Code generation aborted. " + ex);
			logger.debug(ex.getStackTrace());
			return;
		}
	}
}
