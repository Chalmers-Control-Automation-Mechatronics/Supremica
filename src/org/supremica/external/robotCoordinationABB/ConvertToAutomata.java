
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
package org.supremica.external.robotCoordinationABB;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.DataConversionException;
import org.jdom.input.SAXBuilder;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.supremica.automata.*;
import org.supremica.log.*;
import org.supremica.gui.*;

public class ConvertToAutomata
{
	private static Logger logger = LoggerFactory.createLogger(ConvertToAutomata.class);

	// name of the file opened
	private static String fileName;

	// Automata for robot and zones
	private static Automata mZonesAutomata = new Automata();
	private static Automata robotAutomata = new Automata();

	// state from which creating arcs and states
	private static State previousState;

	// name of the robot and of the automaton rpresenting it
	private static String robotName;

	// state in the tree automaton from which inserting the remaining motion
	private static State cutStateTree;

	// state in the motion automaton from which inserting
	private static State cutStateBranch;

	/*
	public static void main(String[] args)
			throws JDOMException{
					conversionToAutomata("trialformat.xml");

	}
	*/

	// class method
	public static void conversionToAutomata(File stationFile)
	{
		Document station = null;

		try
		{
			SAXBuilder builder = new SAXBuilder();

			station = builder.build(stationFile);    // Document and Element define behavior for an XML Document

			// Methods allow the user to obtain the value of the element's textual content, obtain its attributes
			// and get its children
		}
		catch (JDOMException a)
		{
			logger.error(stationFile + ": wrong format file");
			logger.debug(a.getMessage());

			return;
		}
		catch (IOException a)
		{
			logger.error(stationFile + ": wrong format file");
			logger.debug(a.getMessage());

			return;
		}

		Element stationName = station.getRootElement();

		// creating the automata that represent the mutex zones without events (only states)
		mZonesAutomata.clear();
		convertMutexZonesIntoAutomata(stationName);

		// creating the automata that represent the robot
		robotAutomata.clear();

		int nbrOfRobot = 0;

		for (Iterator listIt = stationName.getChildren().iterator();
				listIt.hasNext(); )
		{
			Element thisRobot = (Element) listIt.next();

			if (thisRobot.getName().equals("Robot"))
			{
				convertRobotIntoAutomaton(thisRobot);

				nbrOfRobot++;
			}
		}

		fileName = stationFile.getName();

		if (nbrOfRobot == 0)
		{
			logger.info("No robot present in " + fileName);
		}
	}

	/**
	 * Creates the automata representing the Mutual Exclusion (Mutex) Zones in the input file.
	 * In this phase they consist of only two states (Free and Booked) without events. Events
	 * will be added during the creation of the robot automata.
	 */
	private static void convertMutexZonesIntoAutomata(Element elemento)
	{
		Element elem = new Element("temporary");    // it will contain the element MutexZones

		for (Iterator listIt = elemento.getChildren().iterator(); ; )
		{
			if (!listIt.hasNext())
			{
				logger.info("MutexZones not present in " + fileName);
			}

			elem = (Element) listIt.next();

			if (elem.getName().equals("MutexZones"))
			{
				break;
			}
		}

		// creating automata in Supremica for mutex zones without events
		if (elem.getChildren().size() > 0)
		{
			for (Iterator listIt = elem.getChildren().iterator();
					listIt.hasNext(); )
			{
				Element zone = (Element) listIt.next();

				// creating the automaton
				Automaton mZoneAutom = new Automaton(nameGet(zone));

				mZoneAutom.setType(AutomatonType.Specification);

				// creating the states: free (initial,marked) - booked
				State state = mZoneAutom.createAndAddUniqueState("Free_" + nameGet(zone));

				state.setAccepting(true);
				mZoneAutom.setInitialState(state);

				state = mZoneAutom.createAndAddUniqueState("Booked_" + nameGet(zone));

				mZonesAutomata.addAutomaton(mZoneAutom);
				ActionMan.getGui().addAutomaton(mZoneAutom);
			}

			logger.info("Automata representing the mutex zones built successfully");
		}
		else
		{
			logger.info("MutexZones is an empty set");
		}
	}

	/**
	 * Creates the automaton representing one robot of the input file. For each allowed motion for
	 * the robot the automaton (that will look like  a tree) will have a branch that represents it.
	 * This is done by creating a simple automaton for each motion and then by adding it to the already
	 * built tree automaton (that represents the motions considered at that moment).
	 *
	 * See "createMotionAutomaton"
	 */
	private static void convertRobotIntoAutomaton(Element el)
	{
		robotName = el.getAttribute("name").getValue();

		Automaton robAutom = new Automaton(robotName);

		robAutom.setType(AutomatonType.Plant);

		// creating the initial state for the robot
		State homeState = robAutom.createAndAddUniqueState(robotName + "home");

		homeState.setCost(0);
		robAutom.setInitialState(homeState);

		// searching AllowedMotions
		Element allowedMotions = new Element("temporary");

		if (el.getChildren().size() > 0)
		{
			for (Iterator listIt = el.getChildren().iterator(); ; )
			{
				allowedMotions = (Element) listIt.next();

				if (allowedMotions.getName().equals("AllowedMotions"))
				{
					break;
				}

				if (!listIt.hasNext())
				{
					logger.info("AllowedMotions not present in " + fileName);

					return;
				}
			}
		}
		else
		{
			logger.info("AllowedMotions not present in " + fileName);

			return;
		}

		// create an automaton for each motion and build the "tree" robot
		for (Iterator listIt = allowedMotions.getChildren().iterator();
				listIt.hasNext(); )
		{
			Element motion = (Element) listIt.next();
			Automaton tempAutomaton = createMotionAutomaton(motion, el);

			findCutState(robAutom.getInitialState(), tempAutomaton.getInitialState());
			link(robAutom, cutStateTree, cutStateBranch);
		}

		robotAutomata.addAutomaton(robAutom);
		ActionMan.getGui().addAutomaton(robAutom);
		logger.info("Automaton representing " + robotName + " built successfully");
	}

	/**
	 * For each allowed motion creates a temporary automaton that models it. The function also
	 * adds a dummy marked state to be used by the scheduling algorithm (a cost in the final marked
	 * state is never considered when scheduling). In this phase events are added to the automata
	 * for the Mutex Zones.
	 *
	 * See "createArcsStates".
	 */
	private static Automaton createMotionAutomaton(Element mot, Element thisRobot)
	{
		Automaton motionAutomaton = new Automaton("temporary");

		motionAutomaton.setType(AutomatonType.Plant);

		State homeState = motionAutomaton.createAndAddUniqueState(null);

		motionAutomaton.setInitialState(homeState);
		homeState.setCost(0);

		previousState = homeState;

		if (mot.getChildren().size() == 0)
		{
			logger.info("No path defined for motion " + mot.getName());

			return null;
		}

		for (Iterator listIt = mot.getChildren().iterator(); listIt.hasNext(); )
		{
			Element path = (Element) listIt.next();
			Element thisPath = searchPath(thisRobot, nameGet(path));

			if (thisPath == null)
			{
				logger.error("Path " + nameGet(path) + " not present");
			}

			List sons = thisPath.getChildren();

			for (int j = 0; j < sons.size() - 1; j++)
			{
				Element st1 = (Element) sons.get(j);
				Element st2 = (Element) sons.get(j + 1);

				try
				{
					createArcsStates(motionAutomaton, nameGet(path), st1, st2);
				}
				catch (DataConversionException e)
				{
					logger.error("Error while converting to (int)");

					return null;
				}
				catch (Exception e)
				{
					logger.error("Error while creating arcs and states");
					e.printStackTrace();

					return null;
				}
			}
		}

		// adding a dummy state: Supremica scheduling algorithm doesn't care about costs in the last state
		State state = motionAutomaton.createAndAddUniqueState(null);

		state.setCost(0);
		state.setAccepting(true);

		LabeledEvent event = new LabeledEvent("Dummy" + nameGet(mot));    // every motion in the input file has a different name
		Alphabet alphabet = motionAutomaton.getAlphabet();

		alphabet.addEvent(event);
		motionAutomaton.addArc(new Arc(previousState, state, event));

		// ActionMan.getGui().addAutomaton(motionAutomaton);
		return motionAutomaton;
	}

	/**
	 * Recursive function that returns the state from which the automaton (branch) modeling
	 * the new motion will be added (to the tree automaton). It searches for the events in common with
	 * the ones in the motion automaton and, when an event in the branch is not found in the tree, it
	 * returns the corresponding state.
	 */
	private static void findCutState(State tree, State branch)
	{
		for (ArcIterator arcIt = tree.outgoingArcsIterator(); arcIt.hasNext(); )
		{
			Arc currArcTree = arcIt.nextArc();
			LabeledEvent arcEvent = currArcTree.getEvent();

			if (branch.nextState(arcEvent) != null)
			{
				findCutState(tree.nextState(arcEvent), branch.nextState(arcEvent));

				return;
			}
		}

		cutStateTree = tree;
		cutStateBranch = branch;

		return;
	}

	/**
	 * Links the new part of the automaton to the state found through "findCutState".
	 */
	private static void link(Automaton aut, State stRoot, State added)
	{
		Alphabet alphabet = aut.getAlphabet();
		ArcIterator arcIt = added.outgoingArcsIterator();

		while (arcIt.hasNext())
		{
			Arc currentArc = arcIt.nextArc();
			LabeledEvent arcEvent = currentArc.getEvent();

			if (!alphabet.containsEqualEvent(arcEvent))
			{
				alphabet.addEvent(arcEvent);
			}

			State state = aut.createAndAddUniqueState(null);

			added = added.nextState(arcEvent);

			state.setCost(added.getCost());
			state.setAccepting(added.isAccepting());
			aut.addArc(new Arc(stRoot, state, arcEvent));

			arcIt = added.outgoingArcsIterator();
			stRoot = state;
		}
	}

	/**
	 * From two consecutive "critical" points builds a part of the automaton and adds
 * events to the automata for the Mutex Zones
 *
 * Model used here:
	 *              - events to go untill the limit of a zone;
	 *              - events to weld;
	 *              - book and unbook events.
	 */
	private static void createArcsStates(Automaton a, String whichPath, Element firstP, Element secondP)
		throws DataConversionException
	{
		if ((typeGet(firstP).equals("enterZone") && typeGet(secondP).equals("home")) || (typeGet(firstP).equals("home") && typeGet(secondP).equals("out")))
		{
			logger.error("Initial state not free");

			return;

			// throw new Exception("Initial state not free");
		}

		Alphabet alphabet = a.getAlphabet();

		if (typeGet(firstP).equals("enterZone"))
		{
			LabeledEvent event = new LabeledEvent(robotName + "_" + whichPath + "_book" + nameGet(firstP));

			if (!alphabet.containsEqualEvent(event))
			{
				alphabet.addEvent(event);
			}

			State state = a.createAndAddUniqueState(null);
			int cost = secondP.getAttribute("cost").getIntValue();

			state.setCost(cost);
			a.addArc(new Arc(previousState, state, event));

			if (mZonesAutomata.getNbrOfAutomata() > 0)
			{
				Automaton synchroAutomaton = mZonesAutomata.getAutomaton(nameGet(firstP));

				try
				{
					if (synchroAutomaton == null)
					{
						throw new Exception();
					}
				}
				catch (Exception e)
				{
					logger.error("No automaton named " + nameGet(firstP) + " exists");

					return;
				}

				State free = synchroAutomaton.getStateWithName("Free_" + nameGet(firstP));    // or getInitialState();
				State booked = synchroAutomaton.getStateWithName("Booked_" + nameGet(firstP));
				Alphabet zoneAlphabet = synchroAutomaton.getAlphabet();

				if (!zoneAlphabet.containsEqualEvent(event))
				{
					zoneAlphabet.addEvent(event);
					synchroAutomaton.addArc(new Arc(free, booked, event));
				}
			}

			previousState = state;

			return;
		}

		if (typeGet(firstP).equals("exitZone"))
		{
			LabeledEvent event = new LabeledEvent(robotName + "_" + whichPath + "_unbook" + nameGet(firstP));

			if (!alphabet.containsEqualEvent(event))
			{
				alphabet.addEvent(event);
			}

			State state = a.createAndAddUniqueState(null);
			int cost = secondP.getAttribute("cost").getIntValue();

			state.setCost(cost);
			a.addArc(new Arc(previousState, state, event));

			if (mZonesAutomata.getNbrOfAutomata() > 0)
			{
				Automaton synchroAutomaton = mZonesAutomata.getAutomaton(nameGet(firstP));
				State free = synchroAutomaton.getStateWithName("Free_" + nameGet(firstP));
				State booked = synchroAutomaton.getStateWithName("Booked_" + nameGet(firstP));
				Alphabet zoneAlphabet = synchroAutomaton.getAlphabet();

				if (!zoneAlphabet.containsEqualEvent(event))
				{
					zoneAlphabet.addEvent(event);
					synchroAutomaton.addArc(new Arc(booked, free, event));
				}
			}

			previousState = state;

			return;
		}

		if (typeGet(firstP).equals("wp"))
		{
			LabeledEvent event;

			//if (!typeGet(secondP).equals("home"))
			event = new LabeledEvent(robotName + "_" + whichPath + "_weld");    // comprehend going to the limit of the zone

			// else
			//      event = new LabeledEvent(robotName + "_" + whichPath);
			if (!alphabet.containsEqualEvent(event))
			{
				alphabet.addEvent(event);
			}

			State state = a.createAndAddUniqueState(null);
			int cost = firstP.getAttribute("cost").getIntValue() + secondP.getAttribute("cost").getIntValue();

			state.setCost(cost);
			a.addArc(new Arc(previousState, state, event));

			previousState = state;

			return;
		}

		State state = a.createAndAddUniqueState(null);
		LabeledEvent event = new LabeledEvent(robotName + "_" + whichPath);

		if (!alphabet.containsEqualEvent(event))
		{
			alphabet.addEvent(event);
		}

		int cost = secondP.getAttribute("cost").getIntValue();

		state.setCost(cost);
		a.addArc(new Arc(previousState, state, event));

		previousState = state;

		return;
	}

	/**
	 * Utility: Returns the Element that refers to the Path in robot rob identified by s
	 */
	private static Element searchPath(Element rob, String s)
	{
		if (rob.getChildren().size() > 0)
		{
			for (Iterator listIt = rob.getChildren().iterator();
					listIt.hasNext(); )
			{
				Element next = (Element) listIt.next();

				if (next.getName().equals("Path") && nameGet(next).equals(s))
				{
					return next;
				}
			}
		}

		return null;
	}

	/**
	 * Utility: Returns the type of the Element (type: enterZone, exitZone, wp, home)
	 */
	private static String typeGet(Element el)
	{
		return el.getAttribute("type").getValue();
	}

	/**
	 * Utility: Returns the name of the Element
	 */
	private static String nameGet(Element el)
	{
		return el.getAttribute("name").getValue();
	}
}

/*
		private static void meltAutomataTree(Automaton tree, Automaton branch)
		{
				findCutState(tree.getInitialState(), branch.getInitialState());
				link(tree, cutStateTree, cutStateBranch);
		}

		private static void prova()
		{
				Automata selectedAutomata = ActionMan.getGui().getSelectedAutomata();
				Automaton eccolo = selectedAutomata.getAutomatonAt(0);
				Automaton eccolo2 = selectedAutomata.getAutomatonAt(1);
				meltAutomataTree(eccolo,eccolo2);
		}
}



// HANDLE EXCEPTIONS


		BufferedReader inStation = new BufferedReader(new FileReader("trialformat.xml"));
		String prova = inStation.readLine();
		StringTokenizer token = new StringTokenizer(prova);
		String cosa = new String(token.nextToken());
		System.out.println(cosa)
*/
