
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

import org.supremica.external.robotCoordination.*;
import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import com.inzoom.comjni.ComJniException;
// import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.Mechanism;
// import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.Station;
// import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.Part;
// import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.IABBS4Procedure;
// import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.IABBS4Controller;
import org.supremica.log.*;
import java.util.List;
import java.util.LinkedList;

/**
 * Implementation of the Robot interface for use against RobotStudio.
 */
public class RSRobot
    implements Robot
{
    private static Logger logger = LoggerFactory.createLogger(RSRobot.class);
    
    // Internal variables
    private Mechanism mechanism;
    private Station station;
    private Part spans;

	/** Keeps track of robots current path */
	private Path activePath = null;

	/** Keeps track of current RobotListener */
	private RobotListener robotListener;
	
    public RSRobot(Mechanism mechanism, Station station)
    {
		this.mechanism = mechanism;
		this.station = station;
		
		try
	    {
			spans = RSCell.addPart(getName() + RSCell.SPANS_SUFFIX);
	    }
		catch (Exception ex)
		{
			logger.error("Error in RSRobot.java." + ex);
		}
    }
	
    /////////////////////////////
    // Robot interface methods //
    /////////////////////////////
		
	public List<Configuration> getConfigurations()
		throws Exception
    {
		LinkedList list = new LinkedList();
	    
		// Get targets from RobotStudio, tranform into list of Target:s.
		ITargets robotTargets = mechanism.getWorkObjects().item(Converter.var(1)).getTargets();    // takes the targets from Elements
		int nbrOfTargets = robotTargets.getCount();
	    
		for (int i = 1; i <= nbrOfTargets; i++)
	    {
			list.add(new RSConfiguration(robotTargets.item(Converter.var(i))));
	    }
	    
		return list;
    }
	
	/**
	 * Examines if this robot collides with the given box in the current configuration.
	 */
    public boolean collidesWith(Box box) 
		throws Exception
	{
		// Please move this method from MechanismListener to somewhere more logical
		return MechanismListener.entityCollidesWith(mechanism, ((RSBox) box).getEntity());
		//return true;
    }
	
    public Coordinate getBaseCoordinates() 
	throws Exception 
    {
	return Converter.toCoordinate(mechanism.getTransform().getX(), mechanism.getTransform().getY(), mechanism.getTransform().getZ());
    }

    public synchronized Configuration createConfigurationAtTCP()
	throws Exception 
    {
	ITargets targets = mechanism.getWorkObjects().item(Converter.var(1)).getTargets();
	ITarget newTarget = targets.add();

	newTarget.setTransform(mechanism.getActiveToolFrame().getTransform());
	newTarget.setName(getName() + "_" + RSCell.TARGET_PREFIX + "_" + targets.getCount());
		
	return new RSConfiguration(newTarget);
    }
	
    public Configuration getHomeConfiguration()
		throws Exception
    {
		// takes the targets from Elements
		ITargets robotTargets = mechanism.getWorkObjects().item(Converter.var(1)).getTargets();    
		
		return new RSConfiguration(robotTargets.item(Converter.var(1)));
    }
	
    public void generateSpan(Configuration from, Configuration to)
		throws Exception
    {
		jumpToConfiguration(from);
		
		// Find targets
		Target fromTarget = ((RSConfiguration) from).getRobotStudioTarget();
		Target toTarget = ((RSConfiguration) to).getRobotStudioTarget();
		
		// Create new path for this motion
		IPath path = mechanism.getPaths().add();
		
		path.setName(from.getName() + to.getName());
		path.insert(fromTarget);
		path.getTargetRefs().item(Converter.var(1)).setMotionType(1);    // Linear motion
		path.insert(toTarget);
		path.getTargetRefs().item(Converter.var(2)).setMotionType(1);    // Linear motion
		
		// Redefine robot program...
		IABBS4Procedure mainProcedure = getMainProcedure();
		
		for (int k = 1; k <= mainProcedure.getProcedureCalls().getCount();
			 k++)
	    {
			mainProcedure.getProcedureCalls().item(Converter.var(k)).delete();
	    }
		
		// Add path as only procedure in main
		path.syncToVirtualController(RSCell.PATHSMODULE_NAME);    // Generate procedure from path
		Thread.sleep(1000);    // The synchronization takes a little while...
		
		IABBS4Procedure proc = path.getProcedure();
		
		mainProcedure.getProcedureCalls().add(path.getProcedure());
		
		// Generate span for this path
		// Start simulation listener
		ISimulation simulation = mechanism.getParent().getSimulations().item(Converter.var(1));
		Simulation sim = Simulation.getSimulationFromUnknown(simulation);
		SpanGenerator spanGenerator = new SpanGenerator(this, path.getName());
		
		sim.addDSimulationEventsListener(spanGenerator);
		
		// Start a thread running the simulation in RobotStudio
		simulation.start();
		
		// Wait for the simulation to stop
		spanGenerator.waitForSimulationStop();
		sim.removeDSimulationEventsListener(spanGenerator);
		Thread.sleep(1000);
		
		// Clean up
		path.delete();
    }
	
    public void hideSpan()
		throws Exception
    {
		//spans.setVisible(false);
		spans.delete();
    }
	
    public String getName()
	throws Exception
    {
	return mechanism.getName();
    }

    public void start()
	throws Exception
    {
	// Start controller if not already started
	startController();
    }

    public void stop()
	throws Exception
    {
	Thread.sleep(1000);

	// Stop controller
	stopController();
    }

    public void jumpToConfiguration(Configuration configuration)
	throws Exception
    {
		// Find targets
		Target goal = ((RSConfiguration) configuration).getRobotStudioTarget();
		
		// Jump to the "from"-target
		mechanism.jumpToTarget(goal);
		
		// Takes a while?
		Thread.sleep(1000);
    }
	
    // Other methods
    public String toString()
    {
	try
	    {
		return "'" + getName() + "'";
	    }
	catch (Exception ex)
	    {
		return "''";
	    }
    }

    public Mechanism getRobotStudioMechanism()
	throws Exception
    {
		return mechanism;
    }

    public void addEntityToSpans(IEntity entity) 
	throws Exception
    {
	spans.addEntity(entity);
    }

    /**
     * Starts the IABBS4Controller for robot if it is not already started.
     */
    private IABBS4Controller startController()
	throws Exception
    {
	station.setActiveMechanism(mechanism);

	// Already started?
	IABBS4Controller controller;

	try
	    {
		controller = mechanism.getController();

		return controller;
	    }
	catch (Exception e)
	    {
		// No controller started for this mechanism. Start one!
		// Do we have to shut down any controllers that are running?
	    }

	// Start virtual controller
	controller = mechanism.startABBS4Controller(true);

	// We don't have to wait here? In RS2.0 it was necessary, but in RS3.0,
	// the above call won't return until the controller has started?
	// YES IT WILL! Better wait at least a second... ¤#&#@¤#%#!
	Thread.sleep(1500);

	// Return the controller
	return controller;
    }

    /**
     * Stops the controller for this mechanism.
     */
    private void stopController()
	throws Exception
    {
	// The controller should be up and running!
	IABBS4Controller controller;

	try
	    {
		controller = mechanism.getController();
	    }
	catch (Exception e)
	    {
		// No controller started for this mechanism? Strange, but no problem!
		return;
	    }

	// Add ControllerListener to the mechanism so we can listen to the controller
	Mechanism mech = Mechanism.getMechanismFromUnknown(mechanism);
	ControllerListener controllerListener = new ControllerListener(true);

	mech.add_MechanismEventsListener(controllerListener);

	// Initialize shut down and wait for completion...
	controller.shutDown();
	controllerListener.waitForControllerShutDown();

	// We're ready! Stop listening!
	mech.remove_MechanismEventsListener(controllerListener);

	// We're ready!
	return;
    }
    

    /**
     * Finds and returns the main procedure of a mechanism program.
     */
    int tries = 0;

    protected IABBS4Procedure getMainProcedure()
	throws Exception
    {
	IABBS4Modules modules = mechanism.getController().getModules();

	for (int i = 1; i <= modules.getCount(); i++)
	    {
		IABBS4Procedures procedures = modules.item(Converter.var(i)).getProcedures();

		for (int j = 1; j <= procedures.getCount(); j++)
		    {
			IABBS4Procedure procedure = procedures.item(Converter.var(j));

			if (procedure.getName().equals("main"))
			    {
				if (procedure.getProcedureCalls().getCount() == 0)
				    {
					logger.info("Main procedure empty");
				    }

				return procedure;
			    }
		    }
	    }

	// There is no main procedure!!
	logger.warn("No main procedure found! Trying again...");

	if (tries++ == 10)
	    {
		return null;
	    }

	// Wait a sec and try again...
	Thread.sleep(500);

	return getMainProcedure();
    }

	/**
	 * Adds a new path to robots main procedure.
	 */
	public void addLinearPath(Configuration from, Configuration to) 
		throws Exception
	{
		// Find targets
		Target fromTarget = ((RSConfiguration) from).getRobotStudioTarget();
		Target toTarget = ((RSConfiguration) to).getRobotStudioTarget();
		
		// Create new path for this motion
		activePath = Path.getPathFromUnknown(mechanism.getPaths().add());
		activePath.setName(from.getName() + "_" + to.getName());
		activePath.insert(fromTarget);
		activePath.getTargetRefs().item(Converter.var(1)).setMotionType(1);    // Linear motion
		activePath.insert(toTarget);
		activePath.getTargetRefs().item(Converter.var(2)).setMotionType(1);    // Linear motion

		// Redefine robot program...
		IABBS4Procedure mainProcedure = getMainProcedure();
		for (int k = 1;
			 k <= mainProcedure.getProcedureCalls().getCount();
			 k++)
		    {
				mainProcedure.getProcedureCalls().item(Converter.var(k)).delete();
		    }
		
		// Add path as only procedure in main
		activePath.syncToVirtualController(RSCell.PATHSMODULE_NAME);    // Generate procedure from path
		Thread.sleep(1000);    // The synchronization takes a little while...
		
		IABBS4Procedure proc = activePath.getProcedure();
		mainProcedure.getProcedureCalls().add(activePath.getProcedure());
	}

	public Path getActivePath() 
	{
		return activePath;
	}

	public void setActivePath(Path activePath)
	{
		this.activePath = activePath;
	}

	public void addRobotListener(RobotListener robotListener) 
		throws Exception
	{
		setRobotListener(robotListener);

		mechanism.add_MechanismEventsListener(new MechListener(this));
	}

	public RobotListener getRobotListener()
	{
		return robotListener;
	}

	public void setRobotListener(RobotListener robotListener)
	{
		this.robotListener = robotListener;
	}
}