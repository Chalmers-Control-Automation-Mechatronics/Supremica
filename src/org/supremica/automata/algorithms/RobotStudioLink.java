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

// Frame
import java.awt.*;
import java.awt.event.*;

public class RobotStudioLink
	extends Thread
	implements DAppEvents
{
	// Initialize jacoZoom
	static
	{
		com.inzoom.comjni.Dll.runRoyaltyFree(643622874);
	}

	private static Logger logger = LoggerFactory.createLogger(RobotStudioLink.class);

	private static Application app = null;
	private static Gui gui;
	private static IStation activeStation;
	private static String stationName;

	// Generated automata
	private static Automata robotAutomata;
	private static Automata mutexAutomata;

	public RobotStudioLink(Gui gui, String stationName)
	{
		this.gui = gui;
		this.stationName = stationName;
	}

	/*
 	public static void main(java.lang.String[] args)
 	{
		RobotStudioLink rsl = new RobotStudioLink();
		rsl.test(null);
	}
	*/

	public static void test(Gui gui)
	{
		String stationName = "C:/temp/RobSuprTestStation/RobSuprTest.stn";
		RobotStudioLink rsl = new RobotStudioLink(gui, stationName);
		rsl.init();

		//programStructureTest();

		//rsl.mechanismFromPathTest();

		//pathSplittingTest();
	}

	public static void pathSplittingTest()
	{
		//IPath path = activeStation.getPaths().item(var("I1A1"));
	}

	/**
	 * This test is supposed to show wether a path contains information
	 * on what mechanism it controls... so to speak.
	 */
	public static void mechanismFromPathTest()
	{
		try
		{	// Loop through all paths
			IPaths paths = activeStation.getPaths();
			for (int i=1; i<=paths.getCount(); i++)
			{
				IPath currPath = paths.item(var(i));
				logger.info(currPath.getName());
				logger.info("Parent: " + currPath.getParent().getName());
				logger.info("Parent type: " + currPath.getParent().getObjectType());
				logger.info("Parentparent: " + currPath.getParent().getParent().getName());
				logger.info("Parentparent type: " + currPath.getParent().getParent().getObjectType());
				IMechanism robot = getMechanismFromPath(currPath);
				logger.info("Mechanism: " + robot.getName());
			}
		}
		catch (Exception ex)
		{
			logger.error("Test crasched." + ex);
		}
	}

	/**
	 * This test showed that the program browser does not contain information
	 * of any code above the paths...
	 */
	public static void programStructureTest()
	{
		try
		{	// Get active mechanism
			IMechanism mechanism = activeStation.getActiveMechanism();

			// Print current program
			// Find program structure (find the main procedure)
			IABBS4Procedure main = getMainProcedure(mechanism);
			if (main == null || main.getProcedureCalls().getCount() == 0)
				throw new Exception("No main procedure in program.");

			// Loop through the subprodecures of the main procedure in the ABBS4Controller program
			IABBS4ProcedureCalls calls = main.getProcedureCalls();
			for (int i=1;i<=calls.getCount();i++)
			{
				String procedureName = calls.item(var(i)).getProcedure().getName();
				logger.info(procedureName);
			}
		}
		catch (Exception ex)
		{
			logger.error("Test crasched." + ex);
		}
	}

	public void run()
	{
		// Initialize
		init();

		// Create mutex zones
		createMutexZones();

		// Extract Automata
		extractAutomata();
	}

	/**
	 * Initialize the connection between RobotStudio and Supremica
	 */
	public void init()
	{
		robotAutomata = new Automata();
		mutexAutomata = new Automata();

		try
 	   	{
  	   	   	// Create an instance of RobotStudio.Application.
			if (app != null)
			{
  	   	   		logger.info("RobotStudio already started...");
				app.setVisible(true); // It's nice to see what is happening
				activeStation = app.getWorkspace().openStation(stationName,var(true),var(false));
			}
			else
			{
  	   	   		logger.info("Starting RobotStudio...");
  	   	   		app = new Application();
				app.addDAppEventsListener(this);
				app.setVisible(true); // It's nice to see what is happening
  	   	   		logger.info("RobotStudio started.");

				// Load a certain station
				//String stationName = "C:/temp/RobSuprTestStation/RobSuprTest.stn";
				//String stationName = "C:/Program Files/ABB Robotics/Stations/rsFlexArcR.stn";
				activeStation = app.getWorkspace().openStation(stationName,var(true),var(false));
			}
		}
	    catch(Exception e)
	    {
			logger.error("Error when initializing RobotStudioLink. " + e);
			e.printStackTrace();
	    }
	}

	public void extractAutomata()
	{
		try
		{	// Examine mechanisms
			boolean ok = false;
			while (!ok) // Ugly fix to make sure everything is stable
			{
				try
				{
					getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleRobot);
					ok = true;
				}
				catch (Exception ex)
				{	// Wait for a while and try again
					Thread.sleep(1000);
				}
			}
			LinkedList robots = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleRobot);
			//LinkedList devices = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleDevice);
			//LinkedList tools = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleTool);
			//LinkedList externalAxes = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleExternalAxes);

			// Build automata from the robots
			//for (int i=0;i<1;i++)
			for (int i=0;i<robots.size();i++)
			{
				IMechanism robot = (IMechanism) robots.get(i);

				//robot.examine();
				//robot.unexamine();

				/*
				// Make sure the virtual controller is running!
				String message = "Make sure that the Virtual Controller is \nrunning for the robot " + robot.getName() + " and\nthat the correct program is loaded.";
				//String message = "Make sure the Virtual Controller is shut down.";
				int n = JOptionPane.showConfirmDialog(gui.getComponent(), message, "Start controller", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (n != JOptionPane.OK_OPTION)
				{
					logger.info("RobotStudio test cancelled.");
					return;
				}
				*/

				// Clear all selections
				activeStation.getSelections().removeAll();

				//IABBS4Controller controller = robot.getController();
				//IABBS4Controller controller = robot.startABBS4Controller(true);

				// Run simulation and gather transition times

				// Add MechanismListener
				Mechanism mech = Mechanism.getMechanismFromUnknown(robot);
				MechanismListener mechanismListener = new MechanismListener();
				mech.add_MechanismEventsListener(mechanismListener);
				// Start virtual controller
				activeStation.setActiveMechanism((IMechanism2) robot);
				logger.info("Starting the Virtual Controller...");
				IABBS4Controller controller = robot.startABBS4Controller(true);
				// Wait for controller start
				mechanismListener.waitForControllerStart();
				mechanismListener.setController(controller);
				// Add SimulationListener
				ISimulation simulation = activeStation.getSimulations().item(var(1));
				Simulation sim = Simulation.getSimulationFromUnknown(simulation);
				SimulationListener simulationListener = new SimulationListener();
				sim.addDSimulationEventsListener(simulationListener);
				// Start a thread running the simulation in RobotStudio
				simulation.start();
				// Wait for the simulation to stop
				simulationListener.waitForSimulationStop();

				/*
					robot.moveAlongPath(robot.getPaths().item(var(1)));
				*/

				// Generate automaton and add automaton to gui
				double[] targetTimes = mechanismListener.getTargetTimes();
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
					if (collisionTimes != null)
					{
						// Examine the times of target reaching and mutexzone collisions
						synchronize(mutexAutomaton, robotAutomaton, collisionTimes, targetTimes);
					}
				}

				// Shut down the controller
				logger.info("Shutting down the Virtual Controller...");
				controller.shutDown();
				mechanismListener.waitForControllerShutDown();

				// Stop listening!
				mech.remove_MechanismEventsListener(mechanismListener);
				sim.removeDSimulationEventsListener(simulationListener);
			}

	    }
	    catch(Exception e)
	    {
			logger.error("Error while extracting automata. ");
			e.printStackTrace();
	    }

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
		// create arcs for every path (procedure)
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
			{   // Loop back home
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
	   	//Thread.sleep(2000);
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
		// There is no main procedure!! The program isn't properly loaded!
		logger.error("No main procedure found. Robot program not loaded?");
		return null;
	}

	/**
	 * Creates mutex zones that guarantees no collisions between robots
     */
	public static void createMutexZones()
	{
		try
		{
			// Make sure there is a part for mutexPartName
			final String mutexPartName = "MutexZones"; // This name should be fixed!
			try
			{
				activeStation = app.getActiveStation();
				activeStation.getParts().item(var(mutexPartName));
			}
			catch (ComJniException ex)
			{
				if (ex.ErrorCode == HResult.E_FAIL)
					{   // No such item, construct one!
						activeStation.getParts().add().setName(mutexPartName);
				}
				else
				{  //Something is really wrong
				   logger.error("Something is wrong! " + ex);
			    }
			}

			// Create two boxes (two IEntity), both members of the IPart "MutexZones"
			createBox(-0.25, 0.125, 0.75, 0, 0, 0, 0.5, 0.5, 0.5, mutexPartName, "MutexZone1");
			createBox(-0.25, -0.625, 0.75, 0, 0, 0, 0.5, 0.5, 0.5, mutexPartName, "MutexZone2");
			createBox(-0.125, -0.125, 0.875, 0, 0, 0, 0.25, 0.25, 0.25, mutexPartName, "MutexZone3");

			// Create automata representing the mutexzones (one per zone)
			IPart mutexPart = activeStation.getParts().item(var(mutexPartName));
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
		catch (Exception ex)
		{
			logger.error("Error when creating mutex zones. " + ex);
		}
	}

	/**
	 * Creates box (intended to be a mutual exclusion volume)
	 *
	 * @par mutexPartName The name of the part containing all mutexzones
	 * @par name The name of this certain mutexbox
	 */
	private static void createBox(double tx, double ty, double tz, double rx, double ry, double rz, double wx, double wy, double wz, String mutexPartName, String name)
		throws Exception
	{
		// Create a new IEntity
		IPart mutexPart = activeStation.getParts().item(var(mutexPartName));
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
	private static void synchronize(Automaton mutexAutomaton, Automaton robotAutomaton, double[][] collisionTimes, double[] targetTimes)
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

	public static void executeRobotAutomaton(Automaton robotAutomaton)
	{
		try
		{
			/*
			// Make sure the virtual controller is running!
			String message = "Make sure that the Virtual Controller is \nrunning for the robot " + robotAutomaton.getName() + ".";
			int n = JOptionPane.showConfirmDialog(gui.getComponent(), message, "Start controller", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (n != JOptionPane.OK_OPTION)
			{
				logger.info("Execution cancelled.");
				return;
			}
			*/

			IABBS4Controller controller;
			try
			{
				IMechanism robot = activeStation.getMechanisms().item(var(robotAutomaton.getName()));
				activeStation.setActiveMechanism((IMechanism2) robot);
				logger.info("Starting Virtual Controller...");
				controller = robot.startABBS4Controller(true);
				logger.info("Sleeping...");
				Thread.sleep(5000);
				logger.info("Virtual Controller started.");
			}
			catch (Exception ex)
			{
				logger.error("Vitrual Controller could not be started.");
				return;
			}
			//IABBS4Controller controller = robot.getController();

			new ExecuterWindow(controller, robotAutomaton);
		}
		catch (Exception e)
		{
			logger.error("Couldn't find robot or controller.");
			return;
		}

	}

	/**
	 * Starts thread that moves robot along path. The controller must be
	 * started for the (active) mechanism robot.
	 */
	public static void moveRobotAlongPath(IMechanism robot, IPath path)
		throws Exception
	{
		RobotMoverThread thread = new RobotMoverThread(robot, path);
		thread.start();
	}

	/**
	 * Moves robot along path, both the robot and the path are extracted from
	 * the name of the event.
	 *
	 * @param eventName Name of the event, must be on the format "<path>_start".
	 */
	public static void executeEvent(String eventName)
		throws Exception
	{
		// Test if this is a (controllable) "path-starting" event
		if (!eventName.endsWith("_start"))
			throw new Exception("Could not execute event. Event not controllable?");

		// Find path name and the IPath
		String pathName = eventName.substring(0,eventName.length()-6); // 6 is the length of "_start"
		IPath path = activeStation.getPaths().item(var(pathName));
		IMechanism robot = getMechanismFromPath(path);

		// Move robot along path
		moveRobotAlongPath(robot, path);
	}

	/**
	 * Returns interface to the mechanism that corresponds to a given path.
	 * This method is cheating, it aquires the correct mechanism by analysing
	 * strings. I couldn't find any other way...
	 *
	 * @param path The path of interest
	 */
	private static IMechanism getMechanismFromPath(IPath path)
		throws Exception
	{
		// Get parent name and test if it's correctly formatted
		String frameName = path.getParent().getName();
		if (!frameName.endsWith(">Elements"))
			throw new Exception("Path member of illegal frame, \"" + frameName +"\".");

		// Get the name of the mechanism
		String mechName = frameName.substring(1,frameName.length()-9); // 9 is the length of ">Elements"

		// Return corresponding mechanism
		return activeStation.getMechanisms().item(var(mechName));
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

	public static void kill()
	{
		if (app != null)
		{
			try
			{
				app.quit(false);
			}
			catch (Exception ex)
			{
				logger.info("RobotStudio has already shut down.");
			}
		}
		clean();
	}

	private static void clean()
	{
		/*
		// Quit RobotStudio
		try
 	   	{
			// Quit
	   		app.quit(false);
		}
	    catch(Exception e)
	    {
			logger.error("Error in RobotStudioLink when exiting RobotStudio.");
	    }
		*/

		// Release objects
		app = null;
		activeStation = null;
		mutexAutomata = null;
		robotAutomata = null;
	}

	// DAppEvents interface methods
  	public void selectionChanged()
  	{
  	}
  	public void quit()
  	{
		// This is fatal, cause this has never ever happened
		// and I'd like to know if it ever does!
		logger.fatal("RobotStudio is shutting down.");
		clean();
  	}
  	public int stationBeforeOpen(String Path,boolean[] Cancel)
  	{
    	return 0;
  	}
  	public int stationAfterOpen(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station Station)
  	{
		logger.info("Station opened.");
    	return 0;
  	}
  	public int stationBeforeSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station Station,boolean[] Cancel)
  	{
    	return 0;
  	}
  	public int stationAfterSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station Station)
  	{
    	return 0;
  	}
  	public int libraryBeforeOpen(String FileName,boolean[] Cancel)
  	{
		logger.info("Library opened.");
    	return 0;
  	}
	public int libraryAfterOpen(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject RsObject)
  	{
    	return 0;
  	}
  	public int libraryBeforeSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject RsObject,boolean[] Cancel)
  	{
    	return 0;
  	}
  	public int libraryAfterSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject RsObject)
  	{
    	return 0;
  	}
  	public int started()
  	{
		logger.info("RobotStudio started.");
    	return 0;
  	}

	private static class ExecuterWindow extends _MechanismEventsAdapter implements ActionListener
	{
		IMechanism robot;
		State state;

		java.awt.Frame frame;

		Label  l1 = new Label("Event", Label.RIGHT);
		Choice c1 = new Choice();

		Button b1 = new Button("Execute");
		Button b2 = new Button("Quit");

		ExecuterWindow(IABBS4Controller controller, Automaton robotAutomaton)
		{
			frame = new java.awt.Frame();

			frame.setTitle("Executer Window");
			frame.setIconImage(Supremica.cornerImage);
			try
			{
				robot = controller.getMechanism();
				state = robotAutomaton.getInitialState();
				Mechanism mech = Mechanism.getMechanismFromUnknown(robot);
				mech.add_MechanismEventsListener(this);
			}
			catch (Exception whatever){}
			frame.setLayout(new FlowLayout());
			// FIXA!!
			frame.add(l1); frame.add(c1);

			frame.add(b1); frame.add(b2);
			b1.addActionListener(this);
			b2.addActionListener(this);

			update();
			frame.pack();
			frame.setVisible(true);
		}

		public void update()
		{
			c1.removeAll();
			for (ArcIterator arcIt=state.outgoingArcsIterator(); arcIt.hasNext();)
			{
				LabeledEvent event = arcIt.nextEvent();
				String name;
				if (!event.isControllable())
					name = "!" + event.getLabel();
				else
					name = event.getLabel();
				c1.add(name);
			}
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == b1)
			{
				c1.removeAll();
				String eventName = c1.getSelectedItem();
				state = state.nextState(state.outgoingArcsIterator().nextEvent());
				update();
				String pathName = state.getName();
				try
				{
					moveRobotAlongPath(robot, robot.getPaths().item(var(pathName)));
				}
				catch (Exception whatever)
				{
				}
			}
			else if (e.getSource() == b2)
			{
				frame.setVisible(false);
				try
				{
					IABBS4Controller controller = robot.getController();
					controller.shutDown();
					logger.info("Sleeping...");
					Thread.sleep(5000);
					logger.info("Controller shut down");
				}
				catch (Exception ex)
				{
					logger.error("Controller did not shut down properly.");
				}
			}
		}

		// Extension of the _MechanismEventsAdapter
		boolean leavingTarget = true;
	 	public int targetReached()
	  	{
			try
			{
				if (!leavingTarget)
				{
					state = state.nextState(state.outgoingArcsIterator().nextEvent());
					update();
				}
				leavingTarget = !leavingTarget;
			}
			catch(Exception seeIfICare)
			{
			}
			return 0;
	  	}
	}

	private static class RobotMoverThread extends Thread
	{
		IMechanism robot;
		IPath path;

		public RobotMoverThread (IMechanism robot, IPath path)
		{
			this.robot = robot;
			this.path = path;
		}

		public void run()
		{
			try
			{
				robot.moveAlongPath(path);
			}
			catch (Exception ex)
			{
				System.err.println("Error during robot movement.");
			}
		}
	}
}

class MechanismListener extends _MechanismEventsAdapter
{
	private static Logger logger = LoggerFactory.createLogger(MechanismListener.class);

	private IABBS4Controller controller = null;
	private LinkedList targetTimes = new LinkedList(); // Doubles of targetReached-times
	private LinkedList collisions = new LinkedList();  // CollisionDatas of collisionStart/End-times
	private boolean leavingTarget = true;       // targetReached is invoked twice for every Target!
	private boolean controllerStarted = false;  // Used in the wait-methods

	// Events generated by RobotStudio.Mechanism
 	public int targetReached()
  	{
		try
		{
			double motionTime = controller.getMotionTime();
			targetTimes.add(new Double(motionTime));
			if (!leavingTarget)
				logger.info("Target reached at time " + (float) motionTime);
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
				logger.info("Start of collision with " + objectName + " at time " + (float) data.startTime);
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
				logger.info("End of collision with " + objectName+ " at time " + (float) data.endTime);
			}
		}
		catch (Exception whatever)
		{
		}
  	  	return 0;
  	}
	public synchronized int afterControllerStarted()
	{
		controllerStarted = true;
		logger.info("Virtual Controller started.");
		notify();
		return 0;
	}
	public synchronized int afterControllerShutdown()
	{
		controllerStarted = false;
		logger.info("Virtual Controller shut down.");
		notify();
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
		//CollisionData data = (CollisionData) collisions.get(collisions.indexOf(new CollisionData(mutexZone)));
		int index = collisions.indexOf(new CollisionData(mutexZone));
		//int index = collisions.indexOf(mutexZone);
		if (index >= 0)
		{
			CollisionData data = (CollisionData) collisions.get(index);
			return data.getTimes();
		}
		else
		{
			return null;
		}
	}
	public synchronized void waitForControllerStart()
	{
		try
		{
			while (!controllerStarted)
			{
				wait();
			}

			// Make sure the controller is really started before we return
			Thread.sleep(2500);
		}
		catch (Exception ex)
		{
			//System.out.println("Interrupted! " + ex);
			logger.error("Interrupted! " + ex);
		}
		return;
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
			Thread.sleep(2500);
		}
		catch (Exception ex)
		{
			//System.out.println("Interrupted! " + ex);
			logger.error("Interrupted! " + ex);
		}
		return;
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

	// Events generated by RobotStudio.ISimulation
  	public synchronized void stop()
  	{
		simulationRunning = false;
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
		try
		{
			simulationRunning = true;
			while (simulationRunning)
			{
				wait();
			}

			// Make sure the simulation is really over before we return
			Thread.sleep(1000);
		}
		catch (Exception ex)
		{
			//System.out.println("Interrupted! " + ex);
			logger.error("Interrupted! " + ex);
		}
		return;
	}
}
