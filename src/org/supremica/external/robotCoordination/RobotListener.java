package org.supremica.external.robotCoordination;

public interface RobotListener 
{
  	/**
	 * Called when the robot starts a collision with an object in the
	 * simulation environment. 
	 *
	 * @param volume The object with which the robot just started
	 * colliding.
	 * @param configuration The configuration of the robot at the
	 * ins tant the collision started.
	 * @param time The time (in milliseconds(?)) for the robot to
	 * move to the configuration where the collision started.
	 */ 
	public void startCollision(Volume volume, Configuration configuration, int time);

 	/**
	 * Called when the robot ends a collision with an object in the
	 * simulation environment. 
	 *
	 * @param volume The object with which the robot just stopped
	 * colliding with (just moved out of the volume occupied by that
	 * object).
	 * @param configuration The configuration of the robot at the
	 * instant the collision ended.
	 * @param time The time (in milliseconds(?)) for the robot to move
	 * to the configuration where the collision ended.
	 */
	public void endCollision(Volume volume, Configuration configuration, int time);
}