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

package org.supremica.automata.algorithms;
/*
  javac -classpath c:\programs\supremica\build;c:\programs\supremica\lib/unjared RobotStudioLink.java
  java -classpath .;c:\programs\supremica\build;c:\programs\supremica\lib/unjared RobotStudioLink
*/

import org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.*;
import org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.enum.RsKinematicRole;
import org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.enum.RsSimulationState;
import org.supremica.automata.*;
import org.supremica.log.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import org.supremica.gui.*;

import com.inzoom.comjni.Variant;
import com.inzoom.comjni.SafeArray;
//import com.inzoom.comjni.IDispatch;
//import com.inzoom.comjni.IUnknown;
import com.inzoom.comjni.ComJniException;
import com.inzoom.comjni.enum.HResult;

public class RobotStudioLink
	extends Thread
{
	// Initialize jacoZoom
	static
	{
		com.inzoom.comjni.Dll.runRoyaltyFree(643622874);
	}

	private static Logger logger = LoggerFactory.createLogger(RobotStudioLink.class);
	private static Application app = null;
	private Gui gui;
	private String stationName;
	private IStation activeStation;

	// Generated automata
	private Automata robotAutomata;
	private Automata mutexAutomata;

	/*
	public RobotStudioLink()
	{
		this.gui = null;
	}
	*/

	public RobotStudioLink(Gui gui, String stationName)
	{
		this.gui = gui;
		this.stationName = stationName;
		robotAutomata = new Automata();
		mutexAutomata = new Automata();
	}

	/*
 	public static void main(java.lang.String[] args)
 	{
		RobotStudioLink rsl = new RobotStudioLink();
		rsl.test(null);
	}
	*/

	public void run()
	{
		test();
	}

	public void test()
	{
		try
 	   	{
  	   	   	// Create an instance of RobotStudio.Application.
			if (app != null)
			{
  	   	   		logger.info("RobotStudio already started...");
				app.setVisible(true); // It's nice to see what is happening
			}
			else
			{
  	   	   		logger.info("Starting RobotStudio...");
  	   	   		app = new Application();
				app.setVisible(true); // It's nice to see what is happening
  	   	   		logger.info("RobotStudio started.");

				// Load a certain station
				//activeStation = app.getActiveStation();
				//String stationName = "C:/temp/RobSuprTestStation/RobSuprTest.stn";
				//String stationName = "C:/Program Files/ABB Robotics/Stations/rsFlexArcR.stn";
				activeStation = app.getWorkspace().openStation(stationName,var(true),var(false));
			}

			//logger.info("RobotStudio active station: " + activeStation.getName());

			/*
			// Examine all paths
			IPaths paths = activeStation.getPaths();
			int pathCount = paths.getCount();
			for (int i=1;i<=pathCount;i++)
			{
				logger.info("Paths[" + i + "] = " + paths.item(var(i)).getName());
			}
			*/

			/*
			// Examine mechanisms
			IMechanisms mechanisms = activeStation.getMechanisms();
			int mechanismCount = mechanisms.getCount();
			for (int i=1;i<=mechanismCount;i++)
			{
				logger.info("Mechanisms[" + i + "] = " + mechanisms.item(var(i)).getName());
			}
			*/

			// Examine robots
			LinkedList robots = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleRobot);
			LinkedList devices = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleDevice);
			LinkedList tools = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleTool);
			LinkedList externalAxes = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleExternalAxes);

			/*
			// Examine paths in mechanism
			int robotCount = robots.size();
			for (int i=0;i<robotCount;i++)
			{
				IPaths paths = ((IMechanism) robots.get(i)).getPaths();
				int pathCount = paths.getCount();
				for (int j=1;j<=pathCount;j++)
				{
					logger.info("Robots[" + i + "], Paths[" + j + "] = " + paths.item(var(j)).getName());
				}
			}
			*/

			// Create mutex zones
			createMutexZones(robots);

			// Build automata from the robots
			//for (int i=0;i<1;i++)
			for (int i=0;i<robots.size();i++)
			{
				IMechanism robot = (IMechanism) robots.get(i);
				//robot.examine();
				//robot.unexamine();

				// Make sure the virtual controller is running!
				String message = "Make sure the Virtual Controller is \nrunning for the robot " + robot.getName() + "\nand that the correct program is loaded.";
				//String message = "Make sure the Virtual Controller is shut down.";
				int n = JOptionPane.showConfirmDialog(gui.getComponent(), message, "Start controller", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (n != JOptionPane.OK_OPTION)
				{
					logger.info("RobotStudio test cancelled.");
					clean();
					return;
				}

				// Clear all selections
				activeStation.getSelections().removeAll();

				// Start virtual controller
				logger.info("Starting Virtual Controller...");
				IABBS4Controller controller = robot.getController();
				//IABBS4Controller controller = robot.startABBS4Controller(true);
				logger.info("Virtual Controller started.");

				// Run simulation and gather transition times
				// Add MechanismListener
				Mechanism mech = Mechanism.getMechanismFromUnknown(robot);
				MechanismListener mechanismListener = new MechanismListener();
				mechanismListener.setController(controller);
				mech.add_MechanismEventsListener(mechanismListener);
				// Add SimulationListener
				ISimulation simulation = activeStation.getSimulations().item(var(1));
				Simulation sim = Simulation.getSimulationFromUnknown(simulation);
				SimulationListener simulationListener = new SimulationListener();
				sim.addDSimulationEventsListener(simulationListener);
				// Start a thread running the simulation in RobotStudio
				simulation.start();
				// Wait for the simulation to stio
				simulationListener.waitForSimulationStop();
				double[] targetTimes = mechanismListener.getTargetTimes();

				/*
				robot.moveAlongPath(robot.getPaths().item(var(1)));
				*/

				// Generate automaton and add automaton to gui
				Automaton robotAutomaton = buildAutomaton(robot, targetTimes);
				robotAutomata.addAutomaton(robotAutomaton);
				gui.addAutomaton(robotAutomaton);

				// Adjust mutex automaton
				//for (Iterator autIt = mutexAutomata.iterator(); autIt.hasNext();)
				for (int j=0; j<mutexAutomata.size(); j++)
				{
					//Automaton mutexAutomaton = (Automaton) autIt.next();
					Automaton mutexAutomaton = mutexAutomata.getAutomatonAt(j);
					String zoneName = mutexAutomaton.getName();
					double[][] collisionTimes = mechanismListener.getCollisionTimes(zoneName);

					// Exmine the times of target reaching and mutexzone collisions
					synchronize(mutexAutomaton, robotAutomaton, collisionTimes, targetTimes);
				}

				// Stop listening!
				mech.remove_MechanismEventsListener(mechanismListener);
				sim.removeDSimulationEventsListener(simulationListener);
			}
	    }
	    catch(Exception e)
	    {
			logger.error("Error in RobotStudioLink. ");
			e.printStackTrace();
	    }

		clean();
		return;
	}

	/**
	 * Returns a list of all mechanisms with a certain role in station.
	 */
	private static LinkedList getMechanismsWithRole(IStation station, int kinematicRole)
		throws Exception
	{
		LinkedList list = new LinkedList();
		IMechanisms mechanisms = station.getMechanisms();
		int mechanismCount = mechanisms.getCount();
		for (int i=1;i<=mechanismCount;i++)
		{
			if (mechanisms.item(var(i)).getKinematicRole() == kinematicRole)
				list.add(mechanisms.item(var(i)));
		}
		return list;
	}

	/**
	 * This method generates an Automaton representing the mechanism.
	 * This automata will be a simple linear loop... alternatives
	 * are not an option in RobotStudio... for the moment
	 *
	 * @returns Automaton describing the mechanism movement
	 */
	private static Automaton buildAutomaton(IMechanism mechanism)
		throws Exception
	{
		return buildAutomaton(mechanism, null);
	}
	private static Automaton buildAutomaton(IMechanism mechanism, double[] targetTimes)
		throws Exception
	{
		// Find program structure (find the main procedure)
		IABBS4Procedure main = getMainProcedure(mechanism);
		if (main == null || main.getProcedureCalls().getCount() == 0)
			throw new Exception("No main procedure in program.");

		// Create automaton
		Automaton automaton = new Automaton();
		Alphabet alphabet = automaton.getAlphabet();

		// Set automaton name to mechanism name
		automaton.setName(mechanism.getName());
		automaton.setType(AutomatonType.Plant);

		// Initial state
		State state = automaton.createAndAddUniqueState(null);
		state.setName("Home");
		state.setAccepting(true);
		state.setCost(0);
 		automaton.setInitialState(state);

		// Loop through the subprodecures of the main procedure in the ABBS4Controller program
		// create arcs for every path
		State prevState;
		LabeledEvent startEvent;
		LabeledEvent stopEvent;
		IABBS4ProcedureCalls calls = main.getProcedureCalls();
		for (int i=1;i<=calls.getCount();i++)
		{
			String pathName = calls.item(var(i)).getProcedure().getPath().getName();

			prevState = state;

			// Create start event and from prevState to state
			startEvent = new LabeledEvent(pathName + "_start");
			if (!alphabet.containsEqualEvent(startEvent))
				alphabet.addEvent(startEvent);
			state = new State(pathName);
			// Assign cost
			if (targetTimes != null)
			{
				int cost = (int) (1000*(targetTimes[2*i-2]-targetTimes[2*i-1]));
				state.setCost(cost);
			}
			automaton.addState(state);
			automaton.addArc(new Arc(prevState, state, startEvent));

			prevState = state;

			// Create end event and from prevState to state
			stopEvent = new LabeledEvent(pathName + "_finish");
			stopEvent.setControllable(false);
			if (!alphabet.containsEqualEvent(stopEvent))
				alphabet.addEvent(stopEvent);
			if (i==calls.getCount())
			{   // Loop back
				state = automaton.getInitialState();
			}
			else
			{   // Wait state
				state = automaton.createAndAddUniqueState(null);
				state.setCost(0);
				state.setName("Wait" + i);
			}
			automaton.addArc(new Arc(prevState, state, stopEvent));
		}

		logger.info("Automaton built successfully.");
		return automaton;
	}

	/**
	 * Finds and returns the main procedure of a mechanisms program.
	 */
	private static IABBS4Procedure getMainProcedure(IMechanism mechanism)
		throws Exception
	{
		IABBS4Modules modules = mechanism.getController().getModules();
	   	Thread.sleep(2000);
		for (int i=1;i<=modules.getCount();i++)
		{
			//logger.info(modules.item(var(i)).getName());
			IABBS4Procedures procedures = modules.item(var(i)).getProcedures();
			for (int j=1;j<=procedures.getCount();j++)
			{
				//logger.info(" " + procedures.item(var(j)).getName());
				IABBS4Procedure procedure = procedures.item(var(j));
				if (procedure.getName().equals("main"))
				{
					//logger.info("Found it!");
					return procedure;
				}
			}
		}
		return null;
	}

	/**
	 * Creates mutex zones that guarantees no collisions between robots
     */
	public void createMutexZones(LinkedList robots)
		throws Exception
	{
		// Make sure there is a part for mutexzones
		String mutexzones = "MutexZones";
		try
		{
			activeStation.getParts().item(var(mutexzones));
		}
		catch (ComJniException ex)
		{
			if (ex.ErrorCode == HResult.E_FAIL)
			{   // No such item, construct one!
				activeStation.getParts().add().setName(mutexzones);
			}
			else
			{  //Something is really wrong
			   throw ex;
		    }
		}

		// Create two boxes (two IEntity), both members of the IPart "MutexZones"
		createBox(-0.25, 0.125, 0.75, 0, 0, 0, 0.5, 0.5, 0.5, mutexzones, "MutexZone1");
		createBox(-0.25, -0.625, 0.75, 0, 0, 0, 0.5, 0.5, 0.5, mutexzones, "MutexZone2");

		// Create automata representing the mutexzones (one per zone)
		IPart mutexPart = activeStation.getParts().item(var(mutexzones));
		IEntities mutexEntities = mutexPart.getEntities();
		for (int i=1; i<=mutexEntities.getCount(); i++)
		{
			// Construct automaton
			String entityName = mutexEntities.item(var(i)).getName();
			Automaton automaton = new Automaton(entityName);
			automaton.setType(AutomatonType.Specification);

			// Add two states, Free and Booked
			State state = automaton.createAndAddUniqueState(null);
			state.setName("Free");
			state.setAccepting(true);
	 		automaton.setInitialState(state);
			state = automaton.createAndAddUniqueState(null);
			state.setName("Booked");
			mutexAutomata.addAutomaton(automaton);
			gui.addAutomaton(automaton);
		}
	}

	/**
	 * Creates box (intended to be a mutual exclusion volume)
	 *
	 * @par mutexzones The name of the part containing all mutexzones
	 * @par name The name of this certain mutexbox
	 */
	private void createBox(double tx, double ty, double tz, double rx, double ry, double rz, double wx, double wy, double wz, String mutexzones, String name)
		throws Exception
	{
		/*
		IPart newPart;
		try
		{
			newPart = activeStation.getParts().item(var(mutexzones));
		}
		catch (ComJniException ex)
		{
			if (ex.ErrorCode == HResult.E_FAIL)
			{   // No such item
				newPart = null;
			}
			else
			{  //Something is really wrong
			   throw ex;
		    }
		}

		if (newPart == null)
		{
			newPart = activeStation.getParts().add();
			newPart.setName(mutexzones);
		}
		*/

		//IPart mutexPart = activeStation.getParts().add();
		//newPart.setName(name);

		IPart mutexPart = activeStation.getParts().item(var(mutexzones));
		Transform transform = new Transform();
		transform.setX(tx);
		transform.setY(ty);
		transform.setZ(tz);
		transform.setRx(rx);
		transform.setRy(ry);
		transform.setRz(rz);
		IEntity newBox = mutexPart.createSolidBox(transform,wx,wy,wz);

		// Kraftblå but relatively transparent with a catchy name
		newBox.setVisible(false);
		newBox.setColor(new Variant(new SafeArray(new int[]{0,0,255}),false));
		newBox.setRelativeTransparency((float) 0.75);
		newBox.setName(name);
		newBox.setVisible(true);
	}

	/**
	 * Analyzes automata and times to make sure mutexAutomaton is
	 * synchronized with robotAutomaton.
	 */
	private void synchronize(Automaton mutexAutomaton, Automaton robotAutomaton, double[][] collisionTimes, double[] targetTimes)
		throws Exception
	{
		int j=0;
		int k=0;
		State state = robotAutomaton.getInitialState();
		LabeledEvent event = state.outgoingArcsIterator().nextEvent();

		State freeState = mutexAutomaton.getStateWithName("Free");
		State bookedState = mutexAutomaton.getStateWithName("Booked");

		for (int i=0; i<collisionTimes.length; i++)
		{
			// Start time
			double startTime = collisionTimes[i][0];
			while (startTime > targetTimes[j])
				j++;
			// Add Free->Booked arc with the event leaving the j-1:th state
			for(; k<j-1; k++)
			{
				state = state.nextState(event);
				event = state.outgoingArcsIterator().nextEvent();
			}
			mutexAutomaton.getAlphabet().addEvent(event);
			mutexAutomaton.addArc(new Arc(freeState, bookedState, event));

			// End time
			double endTime = collisionTimes[i][1];
			while (endTime > targetTimes[j])
				j++;
			// Add Booked->Free arc with event the j:th state
			for(; k<j; k++)
			{
				state = state.nextState(event);
				event = state.outgoingArcsIterator().nextEvent();
			}
			mutexAutomaton.getAlphabet().addEvent(event);
 			mutexAutomaton.addArc(new Arc(bookedState, freeState, event));
		}
	}

	/**
	 * Typecast i into Variant
	 */
	private static Variant var(int i)
		throws Exception
	{
		return new Variant(i);
	}
	private static Variant var(boolean i)
		throws Exception
	{
		return new Variant(i);
	}
	private static Variant var(String i)
		throws Exception
	{
		return new Variant(i);
	}

	private void clean()
	{
		mutexAutomata = null;
		robotAutomata = null;
		/*
		// Quit RobotStudio after sleeping for a while
		try
 	   	{
			// Sleep 10 seconds
		   	Thread.sleep(10000);
			// Quit
			if (app!=null)
		   		app.quit(false);
		}
	    catch(Exception e)
	    {
			logger.error("Error in RobotStudioLink when exiting RobotStudio.");
	    }
		finally
		{
	      	// Release all remote objects that haven't already been garbage collected.
	      	//com.linar.jintegra.Cleaner.releaseAll();
		}
		*/
	}
}

class MechanismListener extends _MechanismEventsAdapter
{
	private static Logger logger = LoggerFactory.createLogger(MechanismListener.class);

	private IABBS4Controller controller = null;
	private ISimulation simulation = null;
	private LinkedList targetTimes = new LinkedList();
	private LinkedList collisions = new LinkedList();
	private boolean leavingTarget = true;

	// Events generated by RobotStudio.Mechanism
 	public int targetReached()
  	{
		try
		{
			double motionTime = controller.getMotionTime();
			targetTimes.add(new Double(motionTime));
			if (!leavingTarget)
				logger.info("Target reached at time " + motionTime);
			leavingTarget = !leavingTarget;
		}
		catch(Exception seeIfICare)
		{
		}
		return 0;
  	}
	public int tick(float systemTime)
	{
		// This is fatal, cause this has never ever happened
		// and I'd like to know if it ever does!
		logger.fatal("Mechtick: " + systemTime);
		return 0;
	}

  	public synchronized int collisionStart(RsObject collidingObject)
  	{
		try
		{
			String objectName = collidingObject.getName();
			CollisionData data;
			if (!collisions.contains(new CollisionData(objectName)))
			{
				data = new CollisionData();
				data.setName(objectName);
				data.setCount(0);
				collisions.add(data);
			}
			else
			{
				data = (CollisionData) collisions.get(collisions.indexOf(new CollisionData(objectName)));
			}

			if (data.getCount() == 0)
			{
				data.setStartTime(controller.getMotionTime());
				logger.info("Start of collision with " + objectName + " at time " + data.startTime);
			}
			data.setCount(data.getCount()+1);
		}
		catch (Exception whatever)
		{
		}
    	return 0;
  	}
  	public synchronized int collisionEnd(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject collidingObject)
  	{
		try
		{
			String objectName = collidingObject.getName();
			CollisionData data;
			if (!collisions.contains(new CollisionData(objectName)))
			{
				logger.error("Collision ended mysteriously.");
				return 1;
			}
			else
			{
				data = (CollisionData) collisions.get(collisions.indexOf(new CollisionData(objectName)));
			}

			data.setCount(data.getCount()-1);
			if (data.getCount() == 0)
			{
				data.setEndTime(controller.getMotionTime());
				logger.info("End of collision with " + objectName+ " at time " + data.endTime);
			}
		}
		catch (Exception whatever)
		{
		}
  	  	return 0;
  	}

	// Junk constructed by me
	public void setController(IABBS4Controller controller)
	{
		this.controller = controller;
	}

	public double[] getTargetTimes()
	{
		double[] times = new double[targetTimes.size()];
		for (int i=0; i<times.length; i++)
			times[i] = ((Double) targetTimes.get(i)).doubleValue();
		return times;
	}

	public double[][] getCollisionTimes(String mutexZone)
	{
		CollisionData data = (CollisionData) collisions.get(collisions.indexOf(new CollisionData(mutexZone)));
		return data.getTimes();
	}

	/**
	 * An object containing data concerning the collision with a certain object
	 */
	private class CollisionData
	{	/** Name of colliding object. */
		private String name;
		/** Number of started collisions. */
		private int count;
		/** Collision starting time. */
		private double startTime;
		/** Collision ending time. */
		private double endTime;

		private LinkedList timeList = new LinkedList();

		CollisionData()
		{
		}
		CollisionData(String name)
		{
			this.name = name;
		}

		// Object methods
		public String toString()
		{
			return name;
		}
		public boolean equals(Object object)
		{
			return (name.equals(object.toString()));
		}

		// Other methods
		public void setName(String name)
		{
			this.name = name;
		}
		public int getCount()
		{
			return count;
		}
		public void setCount(int count)
		{
			this.count = count;
		}
		public void setStartTime(double time)
		{
			startTime = time;
		}
		public void setEndTime(double time)
		{
			endTime = time;
			timeList.add(new double[]{startTime, endTime});
		}
		public double[][] getTimes()
		{
			double[][] times = new double[timeList.size()][2];
			for (int i=0; i<timeList.size(); i++)
			{
				times[i] = (double[]) timeList.get(i);
			}
			return times;
		}
	}
}

class SimulationListener extends DSimulationEventsAdapter
{
	private static Logger logger = LoggerFactory.createLogger(SimulationListener.class);

	boolean simulationRunning = true;

	// Events generated by RobotStudio.Simulation
  	public synchronized void stop()
  	{
		simulationRunning = false;
		//System.out.println("Simulation stopped!");
		logger.info("Simulation finished.");
		notify();
  	}

  	public void tick(double time)
  	{
		//System.out.println("Simtick: " + time);
  	}

	// Junk constructed by me
  	public synchronized void waitForSimulationStop()
  	{
		while (simulationRunning)
		{
			try
			{
				wait();
			}
			catch (InterruptedException ex)
			{
				//System.out.println("Interrupted! " + ex);
				logger.error("Interrupted! " + ex);
				return;
			}
		}

		try
		{	// Make sure the simulation is really over before we return
			Thread.sleep(1000);
		}
		catch (Exception noProblem)
		{	// That's ok
		}
		return;
	}
}
