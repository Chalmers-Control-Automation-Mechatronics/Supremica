package org.supremica.external.robotCoordination;

import java.util.*;

public interface Robot
{
	/**
	 * Returns list of the positions for this robot.
	 */
	public LinkedList getPositions()
		throws Exception;

	/**
	 * Returns the home position.
	 */
	public Position getHomePosition()
		throws Exception;

	/**
	 * Generates span for a pair of positions.
	 */
	public void generateSpan(Position from, Position to)
		throws Exception;

	/**
	 * Hides span in smulation environment (just for esthetics)
	 */
	public void hideSpan()
		throws Exception;

	/**
	 * Returns the name of the robot.
	 */
	public String getName()
		throws Exception;

	/**
	 * Initialization of robot, before simulations start.
	 */
	public void start()
		throws Exception;

	/**
	 * Finalization of robot, after completed simulation.
	 */
	public void stop()
		throws Exception;

	/**
	 * Moves the robot to position.
	 */
	public void jumpToPosition(Position position)
		throws Exception;
}
