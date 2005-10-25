package org.supremica.external.robotCoordination;

import java.util.*;
import org.supremica.automata.*;
import java.awt.Color;

public interface Cell
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
	 * Creates a box at the specified coordinte.
	 */
	public Box createBox(Coordinate coordinate)
		throws Exception;

	/**
	 * Destroys a box, if there is no box to destroy, nothing happens.
	 */
	public void destroyBox(Coordinate coordinate)
		throws Exception;

	/**
	 * Sets the parameters for the discretization, the first double is
	 * the delta x step, the second delta y and the last delta z, all
	 * measured in meters.
	 */
	public void setBoxDimensions(double[] dims)
		throws Exception;

	/**
	 * Gets the discretization parameters, the first double is the
	 * delta x step, the second delta y and the last delta z, all
	 * measured in meters.
	 */
	public double[] getBoxDimensions();

	/**
	 * Runs a simulation for a given robot along a path, 
	 * specified by its start and end positions.
	 */
	public void runSimulation(Robot robot, Configuration from, Configuration to)
		throws Exception;

	////////////////////////////////////////////////////////
	// The below methods should be removed or cleaned up! //
	////////////////////////////////////////////////////////

	/**
	 * Finds the index of a robot.
	 */
	public int getRobotIndex(String robotName)
			throws Exception;

	/**
	 * Intersects spans of robots robotA and robotB generating zone
	 * volumes.
	 */
	public void intersectSpans(Robot robotA, Robot robotB)
		throws Exception;

	/**
	 * Simulates the path from from to to in robot robot and adds
	 * viapoints etc.
	 */
	public void examineCollisions(Robot robot, Configuration from, Configuration to)
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
}
