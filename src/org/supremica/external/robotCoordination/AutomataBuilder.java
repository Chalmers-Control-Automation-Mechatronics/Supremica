
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
package org.supremica.external.robotCoordination;

import java.io.*;
import java.util.*;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;
import org.jdom.*;
import org.jdom.input.*;

public class AutomataBuilder
{
	protected Document document;
	protected Automata zones;
	protected Automata paths;
	protected Map robotToWeldingPoints;

	public AutomataBuilder(Document document)
	{
		this.document = document;
		zones = new Automata();
		paths = new Automata();
		robotToWeldingPoints = new HashMap();
	}

	public Automata getAutomata()
	{
		return null;
	}

	protected void buildPath(Automaton currAutomaton, State currState, List remainingPoints)
	{
		if (currAutomaton == null)
		{
			throw new IllegalArgumentException("currAutomaton is null");
		}
		if (currState == null)
		{
			throw new IllegalArgumentException("currState is null");
		}
		if (remainingPoints == null)
		{
			throw new IllegalArgumentException("remainingPoints is null");
		}

		// Break recursion when remainingPoints is empty
		if (remainingPoints.size() == 0)
		{ // Add marked end state
			State newState = currAutomaton.createUniqueState("e_");
			newState.setAccepting(true);
			newState.setCost(0);
		}
	}

	protected void buildWeldingPointMaps()
	{ // Build the maps robotToWeldingPoints
		Element root = document.getRootElement();
		if (!root.getName().equals("RobotCoordination"))
		{
			System.err.println("Wrong xml file format");
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
			System.err.println("Wrong xml file format");
			return;
		}

		String robot = current.getAttributeValue("robot");
		String point = current.getAttributeValue("point");

		if (robot == null || robot.equals(""))
		{
			System.err.println("Empty or no robot attribute");
			return;
		}
		if (point == null || point.equals(""))
		{
			System.err.println("Empty or no point attribute");
		}

		WeldingPoints currPoints;
		if (robotToWeldingPoints.containsKey(robot))
		{
			currPoints = (WeldingPoints)robotToWeldingPoints.get(robot);
		}
		else
		{
			currPoints = new WeldingPoints();
			robotToWeldingPoints.put(robot, currPoints);
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
		if (!current.getName().equals("Move"))
		{
			System.err.println("Wrong xml file format");
			return;
		}
		try
		{
			String source = current.getAttributeValue("source");
			String dest = current.getAttributeValue("target");
			int cost = current.getAttribute("cost").getIntValue();
		}
		catch (DataConversionException e)
		{
			System.err.println(e);
		}
	}

	/**
	 * Return a list with all resources in a Resources element.
	 */
	public List getResources(Element current)
	{
		if (!current.getName().equals("Resources"))
		{
			System.err.println("Wrong xml file format");
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

	public static void main(String[] args)
		throws Exception
	{
		if (args.length <= 0)
		{
			System.err.println("Usage: AutomataBuilder file.xml");
			return;
		}

		SAXBuilder parser = new SAXBuilder();
		Document doc = null;
		try
		{
			doc = parser.build(args[0]);
		}
		// indicates a well-formedness error
		catch (JDOMException e)
		{
			System.out.println(args[0] + " is not well-formed.");
			System.out.println(e.getMessage());
			return;
		}

		AutomataBuilder automataBuilder = new AutomataBuilder(doc);
		automataBuilder.buildWeldingPointMaps();
	}

	public void printAutomata()
	{
		Automata allAutomata = new Automata(paths);
		allAutomata.addAutomata(zones);

		AutomataToXml exporter = new AutomataToXml(allAutomata);
		exporter.serialize(new PrintWriter(System.out));

	}
}


class WeldingPoints
{
	protected List thePoints;

	public WeldingPoints()
	{
		thePoints = new LinkedList();
	}

	public Iterator iterator()
	{
		return thePoints.iterator();
	}

	public WeldingPoint getPoint(String name)
	{
		for (Iterator pointIt = iterator(); pointIt.hasNext(); )
		{
			WeldingPoint currPoint = (WeldingPoint) pointIt.next();
			if (currPoint.equals(name))
			{
				return currPoint;
			}
		}
		return null;
	}

	public WeldingPoint getStartPoint()
	{
		for (Iterator pointIt = iterator(); pointIt.hasNext(); )
		{
			WeldingPoint currPoint = (WeldingPoint) pointIt.next();
			if (currPoint.isStartPoint())
			{
				return currPoint;
			}
		}
		return null;
	}
}

class WeldingPoint
{
	protected String name;
	protected Map nextPoints;
	protected List neededZones;
	protected boolean isStartPoint = false;
	protected boolean isEndPoint = false;


	public WeldingPoint(String name)
	{
		this.name = name;
		nextPoints = new HashMap();
		neededZones = new LinkedList();
	}

	public void setStartPoint(boolean startPoint)
	{
		this.isStartPoint = startPoint;
	}

	public boolean isStartPoint()
	{
		return isStartPoint;
	}

	public void setEndPoint(boolean endPoint)
	{
		this.isEndPoint = endPoint;
	}

	public boolean isEndPoint()
	{
		return isEndPoint;
	}

	public void addNextPoint(WeldingPoint nextPoint, int cost)
	{
		nextPoints.put(nextPoint, new Integer(cost));
	}

	public Iterator nextPointsIterator()
	{
		return nextPoints.keySet().iterator();
	}

	public int getCost(WeldingPoint nextPoint)
	{
		Integer cost = (Integer)nextPoints.get(nextPoint);
		return cost.intValue();
	}

	public int hashCode()
	{
		return name.hashCode();
	}

	public boolean equals(Object other)
	{
		WeldingPoint otherPoint = (WeldingPoint)other;
		return name.equals(otherPoint.name);
	}

	public boolean equals(String other)
	{
		return name.equals(name);
	}
}