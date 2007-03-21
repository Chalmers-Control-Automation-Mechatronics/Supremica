
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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
package org.supremica.external.robotCoordination;

import java.io.*;
import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;
import org.jdom.*;
import org.jdom.input.*;

public class AutomataBuilder
{
	private static Logger logger = LoggerFactory.createLogger(AutomataBuilder.class);
	protected Document document;
	protected Automata currAutomata;
	protected Automata zones;
	protected Automata paths;
	protected RobotToWeldingSpots robotToWeldingSpots;
	protected ProjectFactory theProjectFactory;

	public AutomataBuilder(ProjectFactory theProjectFactory)
	{
		this.theProjectFactory = theProjectFactory;
		zones = new Automata();
		paths = new Automata();
		robotToWeldingSpots = new RobotToWeldingSpots();
	}

	public Automata build(File file)
		throws Exception
	{
		return build(file, false);
	}

	public Automata build(File file, boolean validate)
		throws Exception
	{

		/*
		if (args.length <= 0)
		{
				System.err.println("Usage: AutomataBuilder file.xml");
				return;
		}
		*/
		SAXBuilder parser = new SAXBuilder();

		document = null;

		try
		{
			document = parser.build(file);
		}

		// indicates a well-formedness error
		catch (JDOMException e)
		{
			logger.error(file + " is not well-formed.");
			logger.debug(e.getMessage());

			return null;
		}

		// AutomataBuilder automataBuilder = new AutomataBuilder(doc);
		currAutomata = theProjectFactory.getProject();

		buildWeldingSpotMaps();

		for (Iterator robotIt = robotToWeldingSpots.robotIterator();
				robotIt.hasNext(); )
		{
			String currRobot = (String) robotIt.next();

			//System.out.println(currRobot);
			Automaton currAutomaton = new Automaton(currRobot);

			currAutomaton.setType(AutomatonType.PLANT);

			State initialState = currAutomaton.createUniqueState("initial");

			initialState.setAccepting(false);
			initialState.setInitial(true);
			currAutomaton.addState(initialState);
			paths.addAutomaton(currAutomaton);

			WeldingSpots currSpots = robotToWeldingSpots.getWeldingSpots(currRobot);
			List remainingSpots = new LinkedList();
			WeldingSpot initialSpot = null;
			WeldingSpot endSpot = null;

			for (Iterator spotIt = currSpots.iterator(); spotIt.hasNext(); )
			{
				WeldingSpot currSpot = (WeldingSpot) spotIt.next();

				if (currSpot.isStartSpot())
				{
					initialSpot = currSpot;
				}
				else if (currSpot.isEndSpot())
				{
					endSpot = currSpot;
				}
				else
				{
					remainingSpots.add(currSpot);
				}
			}

			if (initialSpot == null)
			{
				logger.error("No initial spot");

				return null;
			}

			if (endSpot == null)
			{
				logger.error("No end spot");

				return null;
			}
			else
			{
				buildPath(currAutomaton, initialState, initialSpot, remainingSpots, endSpot);

				//System.out.println("InitialSpot: " + currRobot + " " + initialSpot.getName() + " remSpots: " + remainingSpots.size());
			}
		}

		if (zones != null)
		{
			currAutomata.addAutomata(zones);
		}

		if (paths != null)
		{
			currAutomata.addAutomata(paths);
		}

		return currAutomata;
	}

/*
		public Automata getAutomata()
		{
				return null;
		}
*/
	protected void buildPath(Automaton currAutomaton, State currState, WeldingSpot currSpot, List remainingSpots, WeldingSpot endSpot)
	{
		if (currAutomaton == null)
		{
			throw new IllegalArgumentException("currAutomaton is null");
		}

		if (currState == null)
		{
			throw new IllegalArgumentException("currState is null");
		}

		if (currSpot == null)
		{
			throw new IllegalArgumentException("currSpot is null");
		}

		if (endSpot == null)
		{
			throw new IllegalArgumentException("endSpot is null");
		}

		if (remainingSpots == null)
		{
			throw new IllegalArgumentException("remainingSpots is null");
		}

		Alphabet currAlphabet = currAutomaton.getAlphabet();

		// Terminate if remainingSpots is 0
		// Add the end state pairs and return
		if (remainingSpots.size() == 0)
		{
			State endState = constructAllocationDeallocation(currAutomaton, currState, currSpot, endSpot);

			endState.setAccepting(true);

			return;
		}

		// If there remaining spots then add arcs to all reachable
		// spots from the current spot that are also in the remaining set.
		for (Iterator spotIt = currSpot.nextSpotsIterator(); spotIt.hasNext(); )
		{
			WeldingSpot nextSpot = (WeldingSpot) spotIt.next();

			if (remainingSpots.contains(nextSpot))
			{
				State deallocationState = constructAllocationDeallocation(currAutomaton, currState, currSpot, nextSpot);
				List newRemainingSpots = new LinkedList(remainingSpots);

				newRemainingSpots.remove(nextSpot);
				buildPath(currAutomaton, deallocationState, nextSpot, newRemainingSpots, endSpot);
			}
		}
	}

	protected String computeLabel(String prefix, List zones)
	{

		// Build the string
		StringBuffer allocString = new StringBuffer(prefix);

		for (Iterator zoneIt = zones.iterator(); zoneIt.hasNext(); )
		{
			String currZone = (String) zoneIt.next();

			allocString.append("_" + currZone);
		}

		return allocString.toString();
	}

	/**
	 * Build a pair of states, first an allocation event with corresponding allocation state
	 * followed by a deallocation leading to a deallocation state. The last
	 * deallocation state is returned.
	 */
	protected State constructAllocationDeallocation(Automaton currAutomaton, State sourceState, WeldingSpot sourceSpot, WeldingSpot destSpot)
	{
		Alphabet currAlphabet = currAutomaton.getAlphabet();
		RobotMovement currMovement = sourceSpot.getMovement(destSpot);

		// Create the allocation state
		State allocationSpotState = currAutomaton.createUniqueState("a_" + destSpot.getName());

		currAutomaton.addState(allocationSpotState);
		allocationSpotState.setCost(currMovement.getCost());

		// Add allocation event between sourceState and allocationSpotState
		List allocationZones = currMovement.getPreMoveAllocationZones();
		String allocString = computeLabel("a_" + sourceSpot.getName(), allocationZones);
		LabeledEvent allocationEvent = new LabeledEvent(allocString);

		currAlphabet.addEvent(allocationEvent);

		Arc allocationArc = new Arc(sourceState, allocationSpotState, allocationEvent);

		currAutomaton.addArc(allocationArc);

		// Create the deallocation state
		State deallocationSpotState = currAutomaton.createUniqueState("d_" + destSpot.getName());

		currAutomaton.addState(deallocationSpotState);
		deallocationSpotState.setCost(0);

		// Add deallocation event between allocationSpotState and deallocationSpotState
		List deallocationZones = currMovement.getPostMoveDeallocationZones();
		String deallocString = computeLabel("d_" + destSpot.getName(), deallocationZones);
		LabeledEvent deallocationEvent = new LabeledEvent(deallocString);

		currAlphabet.addEvent(deallocationEvent);

		Arc deallocationArc = new Arc(allocationSpotState, deallocationSpotState, deallocationEvent);

		currAutomaton.addArc(deallocationArc);

		return deallocationSpotState;
	}

	protected void buildWeldingSpotMaps()
	{    // Build the maps robotToWeldingSpots
		Element root = document.getRootElement();

		if (!root.getName().equals("RobotCoordination"))
		{
			logger.error("Wrong xml file format");

			return;
		}

		listTree(root);
	}

	public void listTree(Element current)
	{
		String currName = current.getName();

		if (currName.equals("InitialPosition"))
		{
			doInitialPosition(current);
		}
		else if (currName.equals("Move"))
		{
			doMove(current);
		}
		else
		{
			List children = current.getChildren();

			for (Iterator iterator = children.iterator(); iterator.hasNext(); )
			{
				Element child = (Element) iterator.next();

				listTree(child);
			}
		}
	}

	public void doInitialPosition(Element current)
	{
		if (!current.getName().equals("InitialPosition"))
		{
			logger.error("Wrong xml file format");

			return;
		}

		String robot = current.getAttributeValue("robot");
		String spot = current.getAttributeValue("point");

		if ((robot == null) || robot.equals(""))
		{
			logger.error("Empty or no robot attribute");

			return;
		}

		if ((spot == null) || spot.equals(""))
		{
			logger.error("Empty or no spot attribute");
		}

		WeldingSpots currSpots;

		if (robotToWeldingSpots.containsRobot(robot))
		{
			currSpots = (WeldingSpots) robotToWeldingSpots.getWeldingSpots(robot);
		}
		else
		{
			currSpots = new WeldingSpots();

			robotToWeldingSpots.put(robot, currSpots);
		}

		WeldingSpot currSpot = currSpots.getSpot(spot);

		if (currSpot == null)
		{
			currSpot = new WeldingSpot(spot);

			currSpot.setStartSpot(true);
			currSpots.addSpot(currSpot);
		}
		else
		{
			currSpot.setStartSpot(true);
		}

/*
				List children = current.getChildren();
				for (Iterator iterator = children.iterator(); iterator.hasNext(); )
				{
						Element child = (Element) iterator.next();
						if (child.getName().equals("Resource"))
						{
								String currResource = child.getAttributeValue("name");
								theResources.add(currResource);
						}
				}
*/
	}

	public void doMove(Element current)
	{
		int cost = -1;
		WeldingSpot currSpot = null;
		WeldingSpot destSpot = null;

		if (!current.getName().equals("Move"))
		{
			logger.error("Wrong xml file format");

			return;
		}

		try
		{
			String robot = current.getAttributeValue("robot");
			String source = current.getAttributeValue("source");
			String dest = current.getAttributeValue("target");

			cost = current.getAttribute("cost").getIntValue();

			WeldingSpots currSpots;

			if (robotToWeldingSpots.containsRobot(robot))
			{
				currSpots = (WeldingSpots) robotToWeldingSpots.getWeldingSpots(robot);
			}
			else
			{
				currSpots = new WeldingSpots();

				robotToWeldingSpots.put(robot, currSpots);
			}

			currSpot = currSpots.getSpot(source);

			if (currSpot == null)
			{
				currSpot = new WeldingSpot(source);

				//System.out.println("New spot added: " + source);
				currSpots.addSpot(currSpot);
			}

			destSpot = currSpots.getSpot(dest);

			if (destSpot == null)
			{
				destSpot = new WeldingSpot(dest);

				//System.out.println("New spot added: " + source);
				currSpots.addSpot(destSpot);
			}
		}
		catch (DataConversionException e)
		{
			logger.error(e);
		}

		List preMove = new LinkedList();
		List inMove = new LinkedList();
		List postMove = new LinkedList();
		List children = current.getChildren();

		for (Iterator iterator = children.iterator(); iterator.hasNext(); )
		{
			Element child = (Element) iterator.next();

			if (child.getName().equals("PreMove") || child.getName().equals("InMove") || child.getName().equals("PostMove"))
			{

//                              System.out.println("in xxxMoves");
				List newchildren = child.getChildren();

				for (Iterator childIterator = newchildren.iterator();
						childIterator.hasNext(); )
				{
					Element newchild = (Element) childIterator.next();

					if (newchild.getName().equals("Resources"))
					{

//                                              System.out.println("in Resources");
						List currResources = getResources(newchild);

						if (currResources != null)
						{

							//System.out.println("Nbr of resources: " + currResources.size());
							for (Iterator resourceIt = currResources.iterator();
									resourceIt.hasNext(); )
							{
								String currResource = (String) resourceIt.next();

								if (!zones.containsAutomaton(currResource))
								{
									Automaton currAutomaton = new Automaton(currResource);

									currAutomaton.setType(AutomatonType.SPECIFICATION);

									State idleState = currAutomaton.createUniqueState("idle");

									idleState.setAccepting(true);
									idleState.setInitial(true);
									currAutomaton.addState(idleState);
									zones.addAutomaton(currAutomaton);

									//System.out.println("Mew automaton added: " + currResource);
								}

								if (child.getName().equals("PreMove"))
								{
									preMove.add(currResource);
								}
								else if (child.getName().equals("InMove"))
								{
									inMove.add(currResource);
								}
								else if (child.getName().equals("PostMove"))
								{
									postMove.add(currResource);
								}
								else
								{
									System.err.println("Unknown move type");
								}
							}
						}
					}
				}
			}
		}

		RobotMovement movement = new RobotMovement(preMove, inMove, postMove, cost);

		// Add dest spot to source
		currSpot.addNextSpot(destSpot, movement);
	}

	/**
	 * Return a list with all resources in a Resources element.
	 */
	public List getResources(Element current)
	{
		if (!current.getName().equals("Resources"))
		{
			logger.error("Wrong xml file format");

			return null;
		}

		List theResources = new LinkedList();
		List children = current.getChildren();

		for (Iterator iterator = children.iterator(); iterator.hasNext(); )
		{
			Element child = (Element) iterator.next();

			if (child.getName().equals("Resource"))
			{
				String currResource = child.getAttributeValue("name");

				theResources.add(currResource);
			}
		}

		return theResources;
	}

	protected int getCost()
	{
		return 0;
	}

/*
		public static void main(String[] args)
				throws Exception
		{

		}
*/
	public void printAutomata()
	{
		Automata allAutomata = new Automata(paths);

		allAutomata.addAutomata(zones);

		AutomataToXML exporter = new AutomataToXML(allAutomata);

		exporter.serialize(new PrintWriter(System.out));
	}
}

class RobotToWeldingSpots
{
	protected Map robotToWeldingSpots;

	public RobotToWeldingSpots()
	{
		robotToWeldingSpots = new HashMap();
	}

	public void addWeldingSpot(String theRobot, WeldingSpot theSpot)
	{
		WeldingSpots theSpots = null;

		if (robotToWeldingSpots.containsKey(theRobot))
		{
			theSpots = (WeldingSpots) robotToWeldingSpots.get(theRobot);
		}
		else
		{
			theSpots = new WeldingSpots();
		}

		theSpots.addSpot(theSpot);
	}

	public void put(String robot, WeldingSpots theSpots)
	{
		robotToWeldingSpots.put(robot, theSpots);
	}

	public boolean containsRobot(String robot)
	{
		return robotToWeldingSpots.containsKey(robot);
	}

	public WeldingSpots getWeldingSpots(String robot)
	{
		return (WeldingSpots) robotToWeldingSpots.get(robot);
	}

	public Iterator robotIterator()
	{
		Set robots = robotToWeldingSpots.keySet();

		return robots.iterator();
	}
}

/**
 * Contains the welding spots for a robot. Use one class per robot.
 */
class WeldingSpots
{
	protected List theSpots;

	public WeldingSpots()
	{
		theSpots = new LinkedList();
	}

	public Iterator iterator()
	{
		return theSpots.iterator();
	}

	public void addSpot(WeldingSpot theSpot)
	{
		WeldingSpot existingSpot = getSpot(theSpot.getName());

		if (existingSpot == null)
		{
			theSpots.add(theSpot);
		}

		//
		//else
		//{
		//      existingSpot.addNextSpot(theSpot, 10); // Fix the cost, is this correct??
		//}
	}

	public WeldingSpot getSpot(String name)
	{
		for (Iterator spotIt = iterator(); spotIt.hasNext(); )
		{
			WeldingSpot currSpot = (WeldingSpot) spotIt.next();

			if (currSpot.equals(name))
			{
				return currSpot;
			}
		}

		return null;
	}

	public WeldingSpot getStartSpot()
	{
		for (Iterator spotIt = iterator(); spotIt.hasNext(); )
		{
			WeldingSpot currSpot = (WeldingSpot) spotIt.next();

			if (currSpot.isStartSpot())
			{
				return currSpot;
			}
		}

		return null;
	}
}

class WeldingSpot
{
	protected String name;
	protected Map nextSpots;
	protected List neededZones;
	protected boolean isStartSpot = false;
	protected boolean isEndSpot = false;

	public WeldingSpot(String name)
	{
		this.name = name;
		nextSpots = new HashMap();
		neededZones = new LinkedList();
	}

	public String getName()
	{
		return name;
	}

	public void setStartSpot(boolean startSpot)
	{
		this.isStartSpot = startSpot;
	}

	public boolean isStartSpot()
	{
		return isStartSpot;
	}

	public void setEndSpot(boolean endSpot)
	{
		this.isEndSpot = endSpot;
	}

	public boolean isEndSpot()
	{
		return isEndSpot;
	}

	public void addNextSpot(WeldingSpot nextSpot, RobotMovement movement)
	{
		nextSpots.put(nextSpot, movement);
	}

	public Iterator nextSpotsIterator()
	{
		return nextSpots.keySet().iterator();
	}

	public int getCost(WeldingSpot nextSpot)
	{
		RobotMovement movement = (RobotMovement) nextSpots.get(nextSpot);

		return movement.getCost();

		//Integer cost = (Integer)nextSpots.get(nextSpot);
		//return cost.intValue();
	}

	public RobotMovement getMovement(WeldingSpot nextSpot)
	{
		RobotMovement movement = (RobotMovement) nextSpots.get(nextSpot);

		return movement;
	}

	public int hashCode()
	{
		return name.hashCode();
	}

	public boolean equals(Object other)
	{
		WeldingSpot otherSpot = (WeldingSpot) other;

		return name.equals(otherSpot.name);
	}

	public boolean equals(String other)
	{
		return name.equals(other);
	}
}

class RobotMovement
{
	protected List preMove;
	protected List inMove;
	protected List postMove;
	protected int cost;

	public RobotMovement(List preMove, List inMove, List postMove, int cost)
	{
		this.preMove = new LinkedList(preMove);
		this.inMove = new LinkedList(inMove);
		this.postMove = new LinkedList(postMove);
		this.cost = cost;
	}

	public int getCost()
	{
		return cost;
	}

	public List getPreMoveAllocationZones()
	{    // This are all zones in inMoves and postMoves that are not in preMove
		LinkedList allocateZones = new LinkedList(inMove);

		allocateZones.addAll(postMove);
		allocateZones.removeAll(preMove);

		return allocateZones;
	}

	public List getPostMoveDeallocationZones()
	{    // This are all zones in preMoves and postMoves that are not in postMoves.
		LinkedList deallocateZones = new LinkedList(preMove);

		deallocateZones.addAll(inMove);
		deallocateZones.removeAll(postMove);

		return deallocateZones;
	}
}
