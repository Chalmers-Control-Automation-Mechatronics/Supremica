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
	private TransitionMap transitionMap = new TransitionMap();

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

			int lastdot = automatonName.lastIndexOf(".");
			if (lastdot > 0)
			{
				automatonName = automatonName.substring(0, lastdot);
			}

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
		currAlphabet = currAutomaton.getAlphabet();
		currProject.addAutomaton(currAutomaton);

		int currParserState = STATE_READ_NUMBER_OF_STATES;
		int numberOfRemainingStates = 0;
		int numberOfRemainingTransitions = 0;

		State currState = null;
		boolean initialState = true;


		String currLine = reader.readLine();
		while (currLine != null)
		{
			StringTokenizer tokenizer = new StringTokenizer(currLine);

			while (tokenizer.hasMoreTokens())
			{
				String currToken = tokenizer.nextToken();
				System.err.println("umdes: \"" + currToken + "\"");

				if (currParserState == STATE_READ_NUMBER_OF_STATES)
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

					if (numberOfRemainingStates < 1)
					{
						logger.error("The automaton must have at least one state (the initial state)");
					}
					 currParserState = STATE_READ_STATE;
				}
				else if ( currParserState == STATE_READ_STATE)
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

					if (marked < 0 || marked > 1)
					{
						logger.error("Marked must be 0 (unmarked) or 1 (marked)");
					}

					try
					{
						numberOfRemainingTransitions = Integer.parseInt(nbrOfTransitionsString);
					}
					catch (NumberFormatException ex)
					{
						logger.error("Expected the number of transitions");
					}

					if (numberOfRemainingTransitions < 0)
					{
						logger.error("The automaton must have a non negative number of transitions");
					}

					// Create and add the state
					currState = currAutomaton.createUniqueState(stateName);
					if (initialState)
					{
						currState.setInitial(true);
						initialState = false;
					}
					if (marked == 1)
					{
						currState.setAccepting(true);
						currState.setForbidden(false);
					}
					else
					{
						currState.setAccepting(false);
						currState.setForbidden(false);
					}
					currAutomaton.addState(currState);

					numberOfRemainingStates--;

					if (numberOfRemainingTransitions > 0)
					{
						 currParserState = STATE_READ_TRANSITION;
					}
					else
					{
						if (numberOfRemainingStates > 0)
						{
							currParserState = STATE_READ_STATE;
						}
						else
						{
							currParserState = STATE_READ_ADDITIONAL_EVENTS;
						}
					}

				}
				else if (currParserState == STATE_READ_TRANSITION)
				{
					String currEvent = currToken;
					String destStateName = tokenizer.nextToken();
					if (currEvent == null)
					{
						logger.error("Expected an event");
					}
					if (destStateName == null)
					{
						logger.error("Expected a destination state");
					}

					boolean currEventControllable = true;
					boolean currEventObservable = true;

					while (tokenizer.hasMoreTokens())
					{
						String optionalParameter = tokenizer.nextToken();
						//System.err.println(currEvent + " " + destStateName + " " + optionalParameter);
						if (optionalParameter.equalsIgnoreCase("c"))
						{
							currEventControllable = true;
						}
						else if (optionalParameter.equalsIgnoreCase("uc"))
						{
							currEventControllable = false;
						}
						else if (optionalParameter.equalsIgnoreCase("o"))
						{
							currEventObservable = true;
						}
						else if (optionalParameter.equalsIgnoreCase("uo"))
						{
							currEventObservable = false;
						}
						else
						{
							logger.warn("Unknown event attribute: " + optionalParameter);
						}
					}

					LabeledEvent currLabeledEvent = new LabeledEvent(currEvent);
					currLabeledEvent.setControllable(currEventControllable);
					currLabeledEvent.setObservable(currEventObservable);
					currLabeledEvent.setPrioritized(true);
					transitionMap.addArc(currState.getName(), destStateName, currLabeledEvent);

					numberOfRemainingTransitions--;

					if (numberOfRemainingTransitions > 0)
					{
						 currParserState = STATE_READ_TRANSITION;
					}
					else
					{
						if (numberOfRemainingStates > 0)
						{
							currParserState = STATE_READ_STATE;
						}
						else
						{
							currParserState = STATE_READ_ADDITIONAL_EVENTS;
						}
					}

				}
				else if (currParserState == STATE_READ_ADDITIONAL_EVENTS)
				{
					if (currToken.equalsIgnoreCase("EVENTS"))
					{ // Do nothing

					}
					else
					{
						String currEvent = currToken;
						if (currAlphabet.containsEventWithLabel(currEvent))
						{
							logger.warn(currEvent + " is already defined");
						}

						boolean currEventControllable = true;
						boolean currEventObservable = true;

						while (tokenizer.hasMoreTokens())
						{
							String optionalParameter = tokenizer.nextToken();
							if (optionalParameter.equalsIgnoreCase("c"))
							{
								currEventControllable = true;
							}
							else if (optionalParameter.equalsIgnoreCase("uc"))
							{
								currEventControllable = false;
							}
							else if (optionalParameter.equalsIgnoreCase("o"))
							{
								currEventObservable = true;
							}
							else if (optionalParameter.equalsIgnoreCase("uo"))
							{
								currEventObservable = false;
							}
							else
							{
								logger.warn("Unknown event attribute: " + optionalParameter);
							}
						}
						LabeledEvent currLabeledEvent = new LabeledEvent(currEvent);
						currLabeledEvent.setControllable(currEventControllable);
						currLabeledEvent.setObservable(currEventObservable);
						currLabeledEvent.setPrioritized(true);
						currAlphabet.addEvent(currLabeledEvent);
					}
				}
			}


			currLine = reader.readLine();
		}

		// Add all transitions and events
		for (Iterator labelIt = transitionMap.labelIterator(); labelIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent)labelIt.next();
			List currList = transitionMap.getTransitions(currEvent);

			// Add the event
			if (currAlphabet.contains(currEvent))
			{
				logger.warn(currEvent.getLabel() + " is already defined");
			}
			currAlphabet.addEvent(currEvent);

			// Add the transition
			for (Iterator transIt = currList.iterator(); transIt.hasNext(); )
			{
				TransitionMap.Transition currTransition = (TransitionMap.Transition)transIt.next();
				String sourceStateName = currTransition.getSourceStateName();
				String destStateName = currTransition.getDestStateName();
				State currSourceState = currAutomaton.getStateWithName(sourceStateName);
				State currDestState = currAutomaton.getStateWithName(destStateName);

				// Create and add the arc
				Arc currArc = new Arc(currSourceState, currDestState, currEvent);
				currAutomaton.addArc(currArc);
			}


		}


		return currProject;
	}

}

class TransitionMap
{
	private HashMap theMap = new HashMap();

	public TransitionMap()
	{

	}

	public void addArc(String sourceState, String destState, LabeledEvent event)
	{
		Transition newTransition = new Transition(sourceState, destState);

		List transitions;
		if (theMap.containsKey(event))
		{
			transitions = (List)theMap.get(event);
		}
		else
		{
			transitions = new LinkedList();
			theMap.put(event, transitions);
		}
		transitions.add(newTransition);
	}

	public Iterator labelIterator()
	{
		Set currSet = theMap.keySet();
		return currSet.iterator();
	}

	public List getTransitions(LabeledEvent event)
	{
		if (theMap.containsKey(event))
		{
			return (List)theMap.get(event);
		}
		else
		{
			return null;
		}
	}

/*
	public boolean containsEvent(String label)
	{
		LabeledEvent tmpEvent = new LabeledEvent(label);
		return theMap.containsKey(tmpEvent);
	}
*/

	class Transition
	{
		private String sourceState = null;
		private String destState = null;

		public Transition(String sourceState, String destState)
		{
			this.sourceState = sourceState;
			this.destState = destState;
		}

		public String getSourceStateName()
		{
			return sourceState;
		}

		public String getDestStateName()
		{
			return destState;
		}
	}
}
