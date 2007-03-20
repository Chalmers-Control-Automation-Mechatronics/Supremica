
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
package org.supremica.automata.IO;

import java.util.*;
import java.io.*;
import java.net.URL;
import org.supremica.automata.*;
import org.supremica.log.*;

/**
 * Import files from Balemis DES-program, in the .HYB-format
 *
 * The HYB-files contains an enumertion of transitions, defining the automaton
 * For example:
 *
 * lamp_off        c_on            switching_on
 * switching_on    E_on            lamp_on
 *
 * This short excerpt mentions two events, 'c_on' and 'E_on',
 * Controllable and uncontrollable respectively ('c_' stands for
 * 'command' and 'E_' for 'event' (response)). It also mentions three
 * states, 'lamp_off', 'switching_on' and 'lamp_on'. The first state
 * is the initial state. This defines the automaton.
 */
public class ProjectBuildFromHYB
{
	private static Logger logger = LoggerFactory.createLogger(ProjectBuildFromHYB.class);
	private ProjectFactory theProjectFactory = null;
//	private Project currProject = null;
//	private Automaton currAutomaton = null;
	private Alphabet currAlphabet = null;
	protected String automatonName = "Imported from HYB";
//	private InputProtocol inputProtocol = InputProtocol.UnknownProtocol;
	private File thisFile = null;

	public ProjectBuildFromHYB()
	{
		this.theProjectFactory = new DefaultProjectFactory();
	}

	public ProjectBuildFromHYB(ProjectFactory theProjectFactory)
	{
		this.theProjectFactory = theProjectFactory;
	}

	public Project build(URL url)
		throws Exception
	{
		String protocol = url.getProtocol();

		if (protocol.equals("file"))
		{
			//inputProtocol = InputProtocol.FileProtocol;

			String fileName = url.getFile();

			thisFile = new File(fileName);
			automatonName = thisFile.getName();

			int lastdot = automatonName.lastIndexOf(".");

			if (lastdot > 0)
			{
				automatonName = automatonName.substring(0, lastdot);
			}
		}
		else
		{
			//inputProtocol = InputProtocol.UnknownProtocol;

			System.err.println("Unknown protocol: " + protocol);

			return null;
		}

		InputStream stream = url.openStream();

		return build(stream);
	}

	private Project build(InputStream is)
		throws Exception
	{
		InputStreamReader isReader = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isReader);

		// Get the project
		Project currProject = theProjectFactory.getProject();
		Automaton currAutomaton = new Automaton(automatonName);
		currProject.addAutomaton(currAutomaton);
		currAlphabet = currAutomaton.getAlphabet();

		// Init
		boolean initialState = true;

		// Loop over lines
		for (String currLine = reader.readLine(); currLine != null; currLine = reader.readLine())
		{
			// Avoid null lines (can't be?) and comment lines (starting with '#')
			if ((currLine == null) || (currLine.startsWith("#")))
			{
				continue;
			}

			// Each line is a transition, a state, an event and another state
			State fromState;
			LabeledEvent event;
			State toState;

			// Loop over line tokens
			StringTokenizer tokenizer = new StringTokenizer(currLine);
			if (tokenizer.hasMoreTokens()) // This line is not empty
			{
				// Read fromState
				String currToken = tokenizer.nextToken();
				if (currAutomaton.containsStateWithName(currToken))
				{
					fromState = currAutomaton.getStateWithName(currToken);
				}
				else
				{
					fromState = new State(currToken);
					fromState.setAccepting(true);

					// Initialstate?
					if (currAutomaton.nbrOfStates() == 0)
					{
						fromState.setInitial(true);
					}

					currAutomaton.addState(fromState);
				}

				// Read event
				// 'c_' stands for 'command', i.e. issued by supervisor/controller - i.e. controllable
				// 'r_' and 'E_' stands for 'response' and 'event' respectively, appearing in the plant - 
				// i.e. uncontrollable
				currToken = tokenizer.nextToken();
				event = new LabeledEvent(currToken);
				if (currToken.startsWith("c_")) 
				{
					event.setControllable(true);
				}
				else if (currToken.startsWith("r_") || currToken.startsWith("E_"))
				{
					event.setControllable(false);
				}
				else
				{
					logger.warn("Unknown event prefix '" + currToken.substring(0,2)+ 
								"' for event " + currToken + ", treating event as controllable.");
					event.setControllable(true);
				}
				if (!currAlphabet.contains(event))
				{
					currAlphabet.addEvent(event);
				}

				// Read toState
				currToken = tokenizer.nextToken();
				if (currAutomaton.containsStateWithName(currToken))
				{
					toState = currAutomaton.getStateWithName(currToken);
				}
				else
				{
					toState = new State(currToken);
					toState.setAccepting(true);
					currAutomaton.addState(toState);
				}

				// Add transition
				Arc currArc = new Arc(fromState, toState, event);
				currAutomaton.addArc(currArc);

				//logger.info("Add trans " + fromState + " " + toState + " " + event);
			}
		}

		// Return
		return currProject;
	}
}
