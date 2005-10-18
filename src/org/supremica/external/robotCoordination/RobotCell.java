package org.supremica.external.robotCoordination;

import java.util.*;
import org.supremica.automata.*;
import java.awt.Color;

public interface RobotCell
{
	/**
	 * Initializes simulation environment.
	 */
	public void init()
		throws Exception;

	/**
	 * Returns true if a cell is open, false otherwise.
	 */
	public boolean isOpen();

	/**
	 * Returns linked list of Robot objects.
	 */
	public List<Robot> getRobots()
		throws Exception;

	/**
	 * Finds the index of a robot.
	 */
	public int getRobotIndex(String robotName)
			throws Exception;

	/**
	 * Intersects spans of robots robotA and robotB,	    //	    return generating
	 * zone volumes.
	 */
	public void intersectSpans(Robot robotA, Robot robotB)
		throws Exception;

	/**
	 * Simulates the path from from to to in robot robot and adds viapoints
	 * etc.
	 */
	public void examineCollisions(Robot robot, Position from, Position to)
		throws Exception;

	/**
	 * Generates zone automata from earlier generated spans.
	 */
	public Automata generateZoneAutomata()
		throws Exception;

	/**
	 * Generates robot automata from earlier simulations.
	 */
	public Automata generateRobotAutomata()
		throws Exception;

	/**
	 * Creates a box.
	 */
	public Box createBox(Coordinate coordinate, Color color, double transparency)
		throws Exception;
}
