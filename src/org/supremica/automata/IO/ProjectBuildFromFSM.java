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
import java.net.MalformedURLException;
import org.supremica.automata.*;
import org.supremica.automata.execution.*;
import org.supremica.log.*;


/**
 * Import UMDES files, http://www.eecs.umich.edu/umdes/
 * From the UMDES documentation:
 *
 * Individual FSM
 * The default for each event (transition) is controllable and
 * observable (c and o). If an event is uncontrollable and unobservable,
 * you may specify so after the new state with `uc' and `uo'.
 * If the event is either uncontrollable but observable or unobservable
 * but controllable, you may simply state the variable (`uc' or `uo')
 * that describe the negative characteristic. Please note that you may
 * ignore `uc' and `uo' completely if you choose to create the unobservable
 * events file manually by writing this text file. If you choose to use the
 * routine write_uo, you have to state the event properties in the machine.fsm
 * files. Also, the program called "add_prop" adds the uc and uo
 * properties to all events in the FSM file based on the events.uo and
 * event.uc inputs.
 *
 * 4
 * {# States}
 *
 *
 * VC	1/0	4
 * {State}  {Marked/Unmarked} {# Transitions}
 * SC1	VSC
 * {Event} {New State}
 * CV VC
 * OV VO
 * SO1 VSO
 *
 *
 * VO 0 4
 * ... ...
 * ... ...
 * Optionally, additional events not appearing in transitions can
 * be added to a machine. To do this, after the last state and transition,
 * add a new line begining with the key work EVENTS After this line
 * additional events can be listed in the format for an event list.
 * i.e. To add uncontrollable and unobservable event 'a' to an FSM,
 * add the following at the end of the file
 *
 * EVENTS
 * a uc uo
 *
 */
public class ProjectBuildFromFSM
{
	private static Logger logger = LoggerFactory.createLogger(ProjectBuildFromFSM.class);

	private ProjectFactory theProjectFactory = null;
	private Project currProject = null;
	private Automaton currAutomaton = null;
	private Alphabet currAlphabet = null;
	protected String automatonName = "Imported from UMDES";

	private InputProtocol inputProtocol = InputProtocol.UnknownProtocol;
	private File thisFile = null;

	private static int STATE_READ_NUMBER_OF_STATES = 1;
	private static int STATE_READ_STATE = 2;
	private static int STATE_READ_TRANSITION = 3;
	private static int STATE_READ_ADDITIONAL_EVENTS = 4;

	public ProjectBuildFromFSM()
	{
		this.theProjectFactory = new DefaultProjectFactory();
	}

	public ProjectBuildFromFSM(ProjectFactory theProjectFactory)
	{
		this.theProjectFactory = theProjectFactory;
	}

	public Project build(URL url)
		throws Exception
	{
		String protocol = url.getProtocol();

		if (protocol.equals("file"))
		{
			inputProtocol = InputProtocol.FileProtocol;
			String fileName = url.getFile();
			thisFile = new File(fileName);
			automatonName = thisFile.getName();

		}
		else if (protocol.equals("jar"))
		{
			inputProtocol = InputProtocol.JarProtocol;
		}
		else
		{
			inputProtocol = InputProtocol.UnknownProtocol;
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
		Project currProject = theProjectFactory.getProject();

		Automaton currAutomaton = new Automaton(automatonName);
		currProject.addAutomaton(currAutomaton);

		int currState = STATE_READ_NUMBER_OF_STATES;
		int numberOfRemainingStates = 0;
		int numberOfRemainingTransitions = 0;


		String currLine = reader.readLine();
		while (currLine != null)
		{
			StringTokenizer tokenizer = new StringTokenizer(currLine);

			while (tokenizer.hasMoreTokens())
			{
				String currToken = tokenizer.nextToken();
				System.err.println("umdes: \"" + currToken + "\"");

				if (currState == STATE_READ_NUMBER_OF_STATES)
				{
					try
					{
						numberOfRemainingStates = Integer.parseInt(currToken);
					}
					catch (NumberFormatException ex)
					{
						logger.error("Expected the number of states. Read: " + currToken);
						throw ex;
					}
					currState = STATE_READ_STATE;
				}
				else if (currState == STATE_READ_STATE)
				{
					String stateName = currToken;
					String markedString = tokenizer.nextToken();
					String nbrOfTransitionsString = tokenizer.nextToken();
					if (stateName == null)
					{
						logger.error("Expected a state name");
					}
					if (markedString == null)
					{
						logger.error("Expected the marking of the state: 0 or 1");
					}
					if (nbrOfTransitionsString == null)
					{
						logger.error("Expected the number of transitions");
					}

					int marked = -1;
					try
					{
						marked = Integer.parseInt(markedString);
					}
					catch (NumberFormatException ex)
					{
						logger.error("Expected the marking of the state");
					}

					try
					{
						numberOfRemainingTransitions = Integer.parseInt(nbrOfTransitionsString);
					}
					catch (NumberFormatException ex)
					{
						logger.error("Expected the number of transitions");
					}
					currState = STATE_READ_TRANSITION;

				}
				else if (currState == STATE_READ_TRANSITION)
				{
					//currState = STATE_READ_STATE;


				}
				else if (currState == STATE_READ_ADDITIONAL_EVENTS)
				{

				}
				//println(tokenizer.nextToken());
			}


			currLine = reader.readLine();
		}
		return currProject;
	}

	protected void readState()
	{

	}

	protected void readTransition()
	{

	}


}
