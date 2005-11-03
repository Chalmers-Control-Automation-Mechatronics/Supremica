package org.supremica.external.robotCoordination;

import java.util.*;
import org.supremica.log.*;

/**
 * Listens for collisions and builds a list describing the sequence of
 * collisions.
 */
public class CollisionListGenerator 
	implements RobotListener
{
	private static Logger logger = LoggerFactory.createLogger(CollisionListGenerator.class);

	/** List storing info about collisons. */
	List<CollisionData> collisionList = null;

	/** Counts the number of ongoing collisions for each individual zone. */
	int[] collisionCount;

	/** The robot in this simulation. */
	Robot robot;

	/** The list of volumes that are the zones. */
	List<Volume> zones;

	/**
	 * Creates a new CollisionListGenerator, in the robot cell, it is
	 * assumed that the zones are named like "Zone_X" where X is the
	 * number of the zone, spanning from 1 to zones.size().
	 */
	public CollisionListGenerator(Robot robot, List<Volume> zones)
	{
		this.robot = robot;
		this.zones = zones;
		collisionCount = new int[zones.size()];
	}

	/**
	 * Initialization, clears the list, resets the counters and
	 * examines which boxes the robot is currently occupying...
	 */
	public void init()
		throws Exception
	{
		collisionList = new LinkedList<CollisionData>();    

		for (int i=0; i<collisionCount.length; i++)
		{
			collisionCount[i] = 0;
		}

		// Examine which boxes are occupied right now...
		for (Iterator<Volume> zoneIt = zones.iterator(); zoneIt.hasNext(); )
		{
			Volume zone = zoneIt.next();
			logger.info("Inside zone " + zone + "?");
			if (robot.collidesWith(zone))
			{
				logger.info("YES!");
				// We need to keep track of which zones are already
				// occupied at start, we do this by setting the count
				// to -1, this is exploited in the collisionStart
				// method!
				collisionCount[getZoneNumber(zone.getName())] = -1;
			}
		}
	}
	
	/**
	 * Returns the collisionList.
	 */
	public List<CollisionData> getCollisionList()
	{
		return collisionList;
	}

	/**
	 * Since many parts of the robot can enter the robot, we need to
	 * keep count of the number of parts that are currently inside the
	 * robot and only report an "entry" to the zone on the first
	 * occasion!
	 */
	public void collisionStart(Volume volume, int time) 
		throws Exception
	{
		int zone = getZoneNumber(volume.getName());
		if (zone == -1)
			return;

		// Skip adding the collision to the list if we were already
		// inside from the start!
		boolean skip = false;
		if (collisionCount[zone] == -1)
		{
			collisionCount[zone] = 0;
			skip = true;
		}

		// Add value to list only if this was the first entrance
		if (collisionCount[zone]++ == 0 && !skip)
			collisionList.add(new CollisionData(volume, true, time));
	}

	/**
	 * Similarly to collisionStart, we must make sure we only report
	 * the last "exit" from the zone.
	 */
    public void collisionEnd(Volume volume, int time) 
		throws Exception
	{
		int zone = getZoneNumber(volume.getName());
		if (zone == -1)
			return;
		
		// Add value to list only if this was the last exit
		if (--collisionCount[zone] == 0)
			collisionList.add(new CollisionData(volume, false, time));

		assert(!(collisionCount[zone] < 0));
	}

	/**
	 * Parses the zone name for the zone number.
	 */
	private int getZoneNumber(String name)
	{
		int result;
		try
		{
			// Parse name, adjust by -1 since the array indices run
			// from 0 to zones.size()-1 and the zone numbers from 1 to
			// zones.size()...
			result = Integer.parseInt(name.substring(CellExaminer.ZONE_PREFIX.length()))-1;

			if (result >= zones.size())
			{
				result = -1;
			}
		}
		catch (NumberFormatException ex)
		{
			result = -1;
		}

		if (result < 0)
		{
			logger.warn("Caution! Robot " + robot + " unexpectedly collided with the object" + name +
						" which does not appear to be a zone. If this is a static object in the cell" + 
						", more advanced path planning " +
						"is needed to avoid that object. This path should not be used!");
		}

		return result;
	}
}