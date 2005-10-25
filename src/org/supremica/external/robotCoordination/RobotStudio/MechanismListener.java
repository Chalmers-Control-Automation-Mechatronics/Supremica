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

package org.supremica.external.robotCoordination.RobotStudio;

import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.enums.RsKinematicRole;
import com.inzoom.comjni.Variant;
import com.inzoom.comjni.SafeArray;
import com.inzoom.comjni.ComJniException;
import com.inzoom.comjni.enums.HResult;
import org.supremica.log.*;
import org.supremica.automata.*;
import java.util.*;
import java.io.*;

/**
 * Listener for detecting when targets are reached and when collisions
 * begin and end.
 */
public class MechanismListener
    extends _MechanismEventsAdapter
{
    private static Logger logger = LoggerFactory.createLogger(MechanismListener.class);
    
    protected IABBS4Controller controller = null;
    protected boolean leavingTarget = true;    // targetReached is invoked twice for every Target!
    protected boolean controllerStarted = false;    // Used in the wait-method
    protected Path path;
    protected Mechanism mechanism;
    protected RSCell theCell;

	protected RSRobot robot;
	
    // Domenico stuff
    // Costs for the path in simulation
    //protected CreateXml.PathWithCosts pathcosts;
    // Time for the previous event (either start or end)
    // protected double previousTime = 0;
	
    // Dynamic list of objects colliding with the robot
    private LinkedList objectsColliding = new LinkedList();
	
    // A list of the configurations and zones passed
    LinkedList<RichConfiguration> posList = new LinkedList<RichConfiguration>();
	
    /**
     * Returns the list of visited configurations including zone info.
     */
    public LinkedList<RichConfiguration> getRichPath()
    {
		return posList;
    }

	public MechanismListener(RSRobot robot) 
	{
		this.robot = robot;
	}
	
	// 	public MechanismListener(RSCell theCell, Mechanism mechanism, Path path)
	public MechanismListener(RSCell theCell, RSRobot robot)
    {
		try
	    {
			this.robot = robot;
			this.theCell = theCell;
			this.path = robot.getActivePath();
			this.mechanism = robot.getRobotStudioMechanism();

			// pathcosts = new CreateXml.PathWithCosts(path.getName());
			// pathcosts.insertCost(new Integer(0));
	    }
		catch (Exception e)
	    {
			logger.error("Error initializing mechanism");

			return;
	    }

		// Try to find zones that the robot is currently inside
		// ("already colliding with")
		try
		{
			for (int j = 1; j <= RSCell.zones.getEntities().getCount(); j++)
			{
				IEntity zone = RSCell.zones.getEntities().item(Converter.var(j));
				String zoneName = zone.getName();
			
				if (entityCollidesWith(mechanism, zone))
				{
					objectsColliding.add(new Collider(zoneName));
				}
			}
		
			// Print the objects already colliding
			for (ListIterator it = objectsColliding.listIterator();
				 it.hasNext(); )
			{
			
				logger.info(mechanism.getName() + " is already inside the zone " + ((Collider) it.next()).getName() + " at target " + path.getTargetRefs().item(Converter.var(1)).getName() + ".");		
			}
		}
		catch (Exception e)
		{
			logger.error("Error when looking for already colliding objects");
		
			return;
		}
    }
	
    /**
     * Function used to check if the robot collides with some object
     * (before the simulation starts).
     * @return true if robot and object are intersecting at their current
     * configuration, false otherwise
     */
    protected static boolean entityCollidesWith(IMechanism robot, IEntity object)
		throws Exception
    {
		// Get the collision sets, containing two sets for entities
		ICollisionSets sets = RSCell.station.getCollisionSets();
		
		// Mode 1 is the collision detection mode using the collision sets
		// Mode 2 is the collision detection mode using the active mechanism
		sets.setMode(1);
		
		// Our collision set
		ICollisionSet set = sets.add();
		set.setName("CollisionsBeforeTheStart");
		set.setActive(true);
		
		// ObjectsA
		ICollisionObjects rob = set.getObjectsA();
		RsObject robObject = RsObject.getRsObjectFromUnknown(robot);
		rob.add(robObject);
		
		// ObjectsB
		ICollisionObjects objects = set.getObjectsB();
		RsObject entObject = RsObject.getRsObjectFromUnknown(object);
		objects.add(entObject);
		
		// Check if ObjectsA collide with ObjectsB
		sets.setHighlightCollisions(true);    // To see what happens
		boolean collide = sets.checkCollisions();
		sets.setHighlightCollisions(false);
		
		// Forget it
		set.delete();
		
		// See above
		sets.setMode(2);
		
		return collide;
    }
	
	///////////////////////////////////////////////
    // Events generated by RobotStudio.Mechanism //
	///////////////////////////////////////////////

		public synchronized int targetReached()
    {
		try
	    {
			double motionTime = controller.getMotionTime();
			
			if (!leavingTarget)
			{
				// Log
				//logger.debug("Target reached at time " + (float) motionTime + ".");
				
				// Set the cost
				//Double realCost = new Double((motionTime - previousTime) * 1000);    // [ms]
				//pathcosts.insertCost(new Integer(realCost.intValue()));
				//robotCosts[getRobotIndex(mechanism.getName())].add(pathcosts);
				
				// Remember
				posList.add(new RichConfiguration(RSCell.FINISHCONFIGURATION_NAME, motionTime, null, null));
			}
			else
			{
				// Remember
				posList.add(new RichConfiguration(RSCell.STARTCONFIGURATION_NAME, motionTime, null, null));
			}
			
			leavingTarget = !leavingTarget;
	    }
		catch (Exception ex)
		{
			logger.error("Error in event targetReached. " + ex);
		}
		
		return 0;
    }
	
    public synchronized int collisionStart(RsObject collidingObject)
    {
		try
	    {
			// basic information
			String objectName = collidingObject.getName();
			double time = controller.getMotionTime();
			
			// Don't care about the spans!!
			
			/*
			  Nä... så här är det ju inte, de slutar på "_X" också... där X är int
			  if (objectName.endsWith(SPAN_SUFFIX))
			  {
			  return 0;
			  }
			*/
			
			// Did this happen at a positive valued time?
			if (time < 0)
		    {
				logger.error("Collision at negative time detected with " + objectName + ".");
				
				return 0;
		    }
			
			// Wasn't this a zone?
			int indexZone = theCell.getZoneIndex(objectName);
			if (indexZone <= 0)
		    {
				logger.warn("It appears that " + mechanism.getName() + " has collided with '" + objectName + "'.");
				
				return 0;
				
				//throw new SupremicaException("Collision with object in station detected!");
		    }
			
			// Have we collided with this fellow before?
			Collider data = getColliderWithName(objectsColliding, objectName);
			
			// Only for new collisions
			if (data == null)
		    {
				data = new Collider(objectName);
				objectsColliding.add(data);
				logger.debug("Start of collision with " + objectName + " at time " + (float) time + ".");
				
				// set the cost for the automata
				// Double realCost = new Double((time - previousTime) * 1000);
				// pathcosts.insertCost(new Integer(realCost.intValue()));
				// previousTime = time;
				
				// Create a target here!
				//String viaPointName = "In" + indexZone + "_";
				//viaPointName = viaPointName + path.getName() + nbrOfTimesCollision;
				//String viaPointName = "In" + objectName + "_";
				//viaPointName = viaPointName + nbrOfTimesCollision;
				int robotIndex = theCell.getRobotIndex(mechanism.getName());
				String viaPointName = mechanism.getName().substring(5) + RSCell.VIAPOINT_SUFFIX + RSCell.nbrOfTimesCollision;
				ITarget viaTarget = createTargetAtTCP(viaPointName);
				
				// Insert the new target in the path
				ITargetRef viaTargetRef = path.insert(viaTarget);
				viaTargetRef.setMotionType(1);
				
				RSCell.nbrOfTimesCollision++;
				
				// Remember
				posList.add(new RichConfiguration(viaPointName, time, objectName, null));
		    }
			
			// Count the "ins"
			data.setCount(data.getCount() + 1);
	    }
		catch (Exception e)
	    {
			logger.error("Error in collisionStart. " + e);
			e.printStackTrace(System.err);
	    }
		
		return 0;
    }
	
    public synchronized int collisionEnd(RsObject collidingObject)
    {
		try
	    {
			// basic information
			String objectName = collidingObject.getName();
			double time = controller.getMotionTime();
			
			// Wasn't this a zone?
			int indexZone = theCell.getZoneIndex(objectName);
			
			if (indexZone <= 0)
		    {
				return 0;
		    }
			
			Collider data = getColliderWithName(objectsColliding, objectName);
			
			if (data == null)
		    {
				logger.error("Collision ended mysteriously (without starting).");
				
				return 0;
		    }
			
			// Count the "outs"
			data.setCount(data.getCount() - 1);
			
			// Was that the last "out", then the collision has really ended!
			if (data.getCount() == 0)
		    {
				logger.debug("End of collision with " + data.getName() + " at time " + (float) time + ".");
				
				// Set the cost [s]
				// Double realCost = new Double((time - previousTime) * 1000);
				// pathcosts.insertCost(new Integer(realCost.intValue()));
				// previousTime = time;
				
				// Create a target here!
				//String viaPointName = "Out" + indexZone + "_";
				//viaPointName = viaPointName + path.getName() + RSCell.nbrOfTimesCollision;
				//String viaPointName = "Out" + objectName + "_";
				//viaPointName = viaPointName + RSCell.nbrOfTimesCollision;
				String viaPointName = mechanism.getName().substring(5) + RSCell.VIAPOINT_SUFFIX + RSCell.nbrOfTimesCollision;
				ITarget viaTarget = createTargetAtTCP(viaPointName);
				
				// Insert the new target in the path
				ITargetRef viaTargetRef = path.insert(viaTarget);
				viaTargetRef.setMotionType(1);
				
				RSCell.nbrOfTimesCollision++;
				
				// Remove from the colliding objects list
				Collider toBeRemoved = getColliderWithName(objectsColliding, objectName);
				if (toBeRemoved != null)
			    {
					objectsColliding.remove(toBeRemoved);
			    }
				
				// Remember
				posList.add(new RichConfiguration(viaPointName, time, null, objectName));
		    }
	    }
		catch (Exception e)
	    {
			logger.error("Error in collisionEnd. " + e);
	    }
		
		return 0;
    }
	
    public int afterControllerStarted()
    {
		controllerStarted = true;
		
		logger.fatal("Virtual Controller started.");
		logger.fatal("Please, tell Hugo if you read this message!");
		notify();
		
		return 0;
    }
	
    public int afterControllerShutdown()
    {
		controllerStarted = false;
		
		try
	    {
			double motionTime = controller.getMotionTime();
			logger.info("Shutdown at " + motionTime);
	    }
		catch (Exception ex)
	    {
			//logger.error("Error in afterControllerShutdown. " + ex);
	    }
		
		// This is fatal since it has never happened before and I want to know
		// if it ever does...
		//logger.fatal("Virtual Controller shut down."); // Now it does (RS 3.1).
		notify();
		
		return 0;
    }
	
    public synchronized void setController(IABBS4Controller controller)
    {
	this.controller = controller;
    }
	
    public synchronized void waitForControllerShutDown()
    {
		try
	    {
			while (controllerStarted)
		    {
				wait();
		    }
			
			// Make sure the controller is really shut down before we return
			Thread.sleep(2000);
	    }
		catch (Exception ex)
	    {
			//System.out.println("Interrupted! " + ex);
			logger.error("Interrupted! " + ex);
			
			return;
	    }
    }
	
    public int tick(float systemTime)
    {
		// This is fatal, cause this has never ever happened
		// and I'd like to know if it ever does!
		logger.fatal("Mechtick: " + systemTime + ", please tell Hugo if you get this message.");
		
		return 0;
    }
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPRECATED.... Use Robot.createConfigurationAtTCP() instead. (Is only used in this class (in 2 places)).
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Create new target where the TCP currently resides.
     * Note that a target can not have a name that is longer
     * than 16 characters (including the ":1"-suffix!) (RS3.0).
     */
    private synchronized ITarget createTargetAtTCP(String targetName)
		throws Exception
    {
		// Create a new target
		IMechanism mechanism = controller.getMechanism();
		ITarget newTarget = mechanism.getWorkObjects().item(Converter.var(1)).getTargets().add();
		IToolFrame toolFrame = mechanism.getActiveToolFrame();
		
		newTarget.setTransform(toolFrame.getTransform());
		
		// Set a catchy name
		boolean ok = false;
		int nbr = 1;
		
		while (!ok)
			
	    {
			try
		    {
				newTarget.setName(targetName + ":" + nbr++);
				
				ok = true;
		    }
			catch (Exception e)
		    {
				if (nbr >= 10)
			    {
					ok = true;
			    }
		    }
	    }
		
		return newTarget;
    }
	
    /**
     * Finds and returns Collider named name in the LinkedList collisions
     */
    private Collider getColliderWithName(LinkedList collisions, String name)
    {
		try
	    {
			for (ListIterator it = collisions.listIterator();
				 it.hasNext(); )
		    {
				Collider collide = (Collider) it.next();
				
				if (collide.getName().equals(name))
			    {
					return collide;
			    }
		    }
	    }
		catch (Exception e)
	    {
			logger.error("Error checking the objects colliding");
	    }
		
		return null;
    }
}