
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

	protected String ucDisablementCondition(Alphabet theAlphabet)
	{
		/* See above, we should not replace '.' with '_'. */

		StringBuffer theCondition = new StringBuffer();
		boolean firstUcEvent = true;
		theCondition.append("NOT (");


		for (Iterator ucEventIt = theAlphabet.uncontrollableEventIterator(); ucEventIt.hasNext(); )
		{
			LabeledEvent theUcEvent = (LabeledEvent) ucEventIt.next();
			if (firstUcEvent)
			{
				firstUcEvent = false;
			}
			else
			{
				theCondition.append(" AND ");
			}
			theCondition.append(theUcEvent.getLabel());
		}
		theCondition.append(") AND ");
		return theCondition.toString();
	}

	protected void printTransition(Automaton theAutomaton, Arc theArc, PrintWriter pw)
	{
		/* Again, we should not replace '.' with '_' in event labels. */
		try
		{
			LabeledEvent event = theArc.getEvent(); // theAutomaton.getEvent(theArc.getEventId());

			if (event.getLabel().equals("timer"))
			{
				pw.println("SEQTRANSITION " + theAutomaton.getName().replace('.', '_') + "_Tr" + transitionCounter++ + theHelper.getTransitionConditionPrefix() + theAutomaton.getName().replace('.', '_') + "__" + theArc.getFromState().getId() + ".T > 1000" + theHelper.getTransitionConditionSuffix());
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
