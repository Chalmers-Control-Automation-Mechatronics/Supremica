
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
import org.supremica.util.SupremicaException;
import org.supremica.external.robotCoordinationABB.CreateXml;
import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.enums.RsKinematicRole;
import org.supremica.automata.*;
import org.supremica.log.*;
import java.util.*;
import javax.swing.JOptionPane;
import org.supremica.gui.*;
import com.inzoom.comjni.Variant;
import com.inzoom.comjni.SafeArray;

//import com.inzoom.comjni.IDispatch;
//import com.inzoom.comjni.IUnknown;
import com.inzoom.comjni.ComJniException;
import com.inzoom.comjni.enums.HResult;

// Frame
import java.awt.*;
import java.awt.event.*;

/**
 * @deprecated Don't keep this class other than for reference.
 */
public class RobotStudioLink
	implements Runnable, DAppEvents
{
	// Initialize jacoZoom
	static
	{
		com.inzoom.comjni.Dll.runRoyaltyFree(643622874);
	}

	private static Logger logger = LoggerFactory.createLogger(RobotStudioLink.class);

	/** The RobotStudio application. */
	private static Application app = null;

	/** The active RopbotStudio station. */
	private static IStation activeStation = null;

	/** The name of the current active station. */
	private static String stationName;

	/** The Supremica gui. */
	private static Gui gui;

	// Constants

	/** The name of the IPart containing the mutex zones. */
	private final static String mutexPartName = "MutexZones";
	private final static String mutexEntityBaseName = "MutexZone";

	// Generated automata
	private static Automata robotAutomata;
	private static Automata mutexAutomata;

	public RobotStudioLink(Gui gui, String stationName)
	{
		RobotStudioLink.gui = gui;
		RobotStudioLink.stationName = stationName;
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

		logger.info("Opening station " + stationName + "...");

		Thread thread = new Thread(new RobotStudioLink(gui, stationName));

		thread.start();
	}

/* FROM VB
Private Sub CommandButton13_Click()
	Dim pOld As path
	Dim tRef As TargetRef

	Set pOld = ActiveStation.Paths("MOV_mv_flaeche")
	Set tRef = ActiveStation.Paths("MOV_mv_flaeche").TargetRefs(4)
	Call SplitPath(pOld, tRef)
End Sub

Sub SplitPath(pOld As path, t As TargetRef)

	Set m = ActiveStation.ActiveMechanism
	Dim pNew As path
	Set pNew = m.Paths.Add
	pNew.name = "Split"

	Dim index As Integer

	' get the index of the target
	For iCount = 1 To pOld.TargetRefs.Count
		If pOld.TargetRefs(iCount) = t Then
			index = iCount
		End If
	Next iCount

	UserOptions.UseRobotConfiguration = False
	MsgBox UserOptions.UseRobotConfiguration

	' copy the targets and delete it from the old path
	Dim tRef As TargetRef
	Dim tempTarget As ITarget
	Dim i As Integer
	i = 1
	While i <= index
		Set tempTarget = pOld.TargetRefs(1).target
		Call pNew.Insert(tempTarget)
		' dont delete the last one
		If Not i = index Then
			Call pOld.TargetRefs(1).Delete
		End If
		i = i + 1
	Wend
End Sub
*/
	public static void pathSplittingTest()
	{

		//IPath path = activeStation.getPaths().item(var("I1A1"));
	}

	/**
	 * This test is supposed to show wether a path contains information
	 * on what mechanism it controls... so to speak. It turned out it doesn't!
	 */
	public static void mechanismFromPathTest()
	{
		try
		{    // Loop through all paths
			IPaths paths = activeStation.getPaths();

			for (int i = 1; i <= paths.getCount(); i++)
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
			logger.error("Test crashed." + ex);
		}
	}

	/**
	 * This test showed that the program browser does not contain information
	 * of any code above the paths...
	 */
	public static void programStructureTest()
	{
		try
		{    // Get active mechanism
			IMechanism mechanism = activeStation.getActiveMechanism();

			// Print current program
			// Find program structure (find the main procedure)
			IABBS4Procedure main = getMainProcedure(mechanism);

			//if (main == null || main.getProcedureCalls().getCount() == 0)
			//      throw new SupremicaException("No main procedure in program.");
			// Loop through the subprodecures of the main procedure in the ABBS4Controller program
			IABBS4ProcedureCalls calls = main.getProcedureCalls();

			for (int i = 1; i <= calls.getCount(); i++)
			{
				String procedureName = calls.item(var(i)).getProcedure().getName();

				logger.info(procedureName);
			}
		}
		catch (Exception ex)
		{
			logger.error("Test crashed." + ex);
		}
	}

	/**
	 * Run some commands, a demo essentially.
	 */
	public void run()
	{

		// Initialize
		init();

		// Wait for user
		int m = JOptionPane.showConfirmDialog(gui.getComponent(), "Generate mutex zones?", "Generate mutex zones", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (m != JOptionPane.OK_OPTION)
		{
			return;
		}

		// Create mutex zones
		// createMutexZones();
		createMutexZonesFromSpan();

		// Wait for user
		int n = JOptionPane.showConfirmDialog(gui.getComponent(), "Extract automata?", "Extract automata", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (n != JOptionPane.OK_OPTION)
		{
			return;
		}

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
				app.setVisible(true);    // It's nice to see what is happening

				activeStation = app.getWorkspace().openStation(stationName, var(true), var(false));
			}
			else
			{
				logger.info("Starting RobotStudio...");

				app = new Application();

				app.addDAppEventsListener(this);
				app.setVisible(true);    // It's nice to see what is happening
				logger.info("RobotStudio started.");

				// Load a certain station
				//String stationName = "C:/temp/RobSuprTestStation/RobSuprTest.stn";
				//String stationName = "C:/Program Files/ABB Robotics/Stations/rsFlexArcR.stn";
				activeStation = app.getWorkspace().openStation(stationName, var(true), var(false));
			}
		}
		catch (Exception e)
		{
			logger.error("Error when initializing RobotStudioLink. " + e);
			e.printStackTrace();
		}
	}

	public static void extractAutomata()
	{
		try
		{

			// Examine mechanisms
			// Ugly fix to make sure everything is stable
			boolean ok = false;

			while (!ok)
			{
				try
				{
					getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleRobot);

					ok = true;
				}
				catch (Exception ex)
				{    // Wait for a while and try again
					Thread.sleep(500);
				}
			}

			LinkedList robots = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleRobot);

			//LinkedList devices = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleDevice);
			//LinkedList tools = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleTool);
			//LinkedList externalAxes = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleExternalAxes);
			// Build automata from the robots
			for (int i = 0; i < robots.size(); i++)
			{

				// Get the i:th robot
				IMechanism robot = (IMechanism) robots.get(i);

				robot.examine();
				robot.unexamine();

				// Clear all selections
				activeStation.getSelections().removeAll();

				// Start controller
				IABBS4Controller controller = startController(robot);

				// Run simulation and gather transition times
				// Add CollisionListener
				Mechanism mech = Mechanism.getMechanismFromUnknown(robot);
				CollisionListener collisionListener = new CollisionListener(controller);

				mech.add_MechanismEventsListener(collisionListener);

				// Add SimulationListener
				ISimulation simulation = activeStation.getSimulations().item(var(1));
				Simulation sim = Simulation.getSimulationFromUnknown(simulation);
				SimulationListener simulationListener = new SimulationListener();

				sim.addDSimulationEventsListener(simulationListener);

				// Start a thread running the simulation in RobotStudio
				simulation.start();

				// Wait for the simulation to stop
				simulationListener.waitForSimulationStop();

				// Generate automaton and add automaton to gui
				double[] targetTimes = collisionListener.getTargetTimes();
				Automaton robotAutomaton = buildAutomaton(robot, targetTimes);

				robotAutomata.addAutomaton(robotAutomaton);
				gui.addAutomaton(robotAutomaton);

				// Adjust mutex automata
				// for (Iterator autIt = mutexAutomata.iterator(); autIt.hasNext();)
				for (int j = 0; j < mutexAutomata.size(); j++)
				{

					//Automaton mutexAutomaton = (Automaton) autIt.next();
					Automaton mutexAutomaton = mutexAutomata.getAutomatonAt(j);
					String zoneName = mutexAutomaton.getName();

					// Get the collision times for this zone recorded during the simulation
					double[][] collisionTimes = collisionListener.getCollisionTimes(zoneName);

					if (collisionTimes != null)
					{

						// Examine the times of target reaching and mutexzone collisions
						synchronize(mutexAutomaton, robotAutomaton, collisionTimes, targetTimes);
					}
				}

				// Shut down the controller
				stopController(robot);

				// Stop listening!
				mech.remove_MechanismEventsListener(collisionListener);
				sim.removeDSimulationEventsListener(simulationListener);
			}
		}
		catch (Exception e)
		{
			logger.error("Error while extracting automata. ");
			e.printStackTrace();
		}

		return;
	}

/*
		//So far the following method is never used
		public static void NEWextractAutomata()
		{
				try
				{       // Examine mechanisms
						boolean ok = false;
						while (!ok) // Ugly fix to make sure everything is stable
						{
								try
								{
										getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleRobot);
										ok = true;
								}
								catch (Exception ex)
								{       // Wait for a while and try again
										Thread.sleep(1000);
								}
						}

						LinkedList robots = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleRobot);

						// Build automata from the robots
						for (int i=0; i<robots.size(); i++)
						{
								IMechanism robot = (IMechanism) robots.get(i);

								// Clear all selections
								activeStation.getSelections().removeAll();

								// Add CollisionListener
								Mechanism mech = Mechanism.getMechanismFromUnknown(robot);
								CollisionListener collisionListener = new CollisionListener();
								mech.add_MechanismEventsListener(collisionListener);
								// Start virtual controller
								activeStation.setActiveMechanism((IMechanism2) robot);
								logger.info("Starting the Virtual Controller...");
								IABBS4Controller controller = robot.startABBS4Controller(true);
								// Wait for controller start
								// collisionListener.waitForControllerStart();
								//D H: Is there a bug in RS2.1? the event afterControllerStarted doesn´t seem firing
								collisionListener.setController(controller);

								// Simulate program path by path
								IABBS4Procedure main = getMainProcedure(robot);
								IABBS4ProcedureCalls calls = main.getProcedureCalls();
								calls.setModuleName("BananApan");
								//for (int j=1; j<=calls.getCount(); j++)
								//{
								//      IPath path = calls.item(var(j)).getProcedure().getPath();
								//      logger.info("Simulatin' path " + path.getName());
								//      runSimulation(path);
								//      break;
								//}

								// Shut down the controller
								//logger.info("Shutting down the Virtual Controller...");
								//controller.shutDown();
								//collisionListener.waitForControllerShutDown();

								// Stop listening!
								//mech.remove_MechanismEventsListener(collisionListener);
						}
			}
			catch(Exception e)
			{
						logger.error("Error while extracting automata. ");
						e.printStackTrace();
			}

				return;
		}
*/

	/**
	 * Runs simulation of certain path presuming the controller is started.
	 */
	private static double runSimulation(IPath path)
		throws Exception
	{

		// Get the active mechanism
		IMechanism mechanism = activeStation.getActiveMechanism();

		// Adjust program
		// Find program structure (find the main procedure)
		IABBS4Procedure main = getMainProcedure(mechanism);

		main.getProcedureCalls().setModuleName("BananApa");

		IABBS4Procedure mainSave = main.getParent().getProcedures().add();

		// Add our path
		//IABBS4ProcedureCalls calls = newMain.getProcedureCalls();
		//calls.add(path.getProcedure());
		// Add SimulationListener
		ISimulation simulation = activeStation.getSimulations().item(var(1));
		Simulation sim = Simulation.getSimulationFromUnknown(simulation);
		SimulationListener simulationListener = new SimulationListener();

		sim.addDSimulationEventsListener(simulationListener);

		// Start a thread running the simulation in RobotStudio
		simulation.start();

		// Wait for the simulation to stop
		simulationListener.waitForSimulationStop();

		// Stop listening!
		sim.removeDSimulationEventsListener(simulationListener);

		// Restore the old main procedure
		//newMain.delete();
		//main.setName("main");
		// Return simulation time...
		return 0;
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

		for (int i = 1; i <= mechanismCount; i++)
		{
			if (mechanisms.item(var(i)).getKinematicRole() == kinematicRole)
			{
				list.add(mechanisms.item(var(i)));
			}
		}

		return list;
	}

	/**
	 * This method generates an Automaton representing the mechanism.
	 * This automata will be a simple linear loop... alternatives
	 * are not an option in RobotStudio... for the moment
	 *
	 * @return Automaton describing the mechanism movement
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

		//if (main == null || main.getProcedureCalls().getCount() == 0)
		//      throw new SupremicaException("No main procedure in program.");
		// Create automaton of plant type and with the mechanism's name
		Automaton automaton = new Automaton(mechanism.getName());

		automaton.setType(AutomatonType.Plant);

		Alphabet alphabet = automaton.getAlphabet();

		// Initial state
		State state = new State("Home");
		state.setInitial(true);
		state.setAccepting(true);
		automaton.addState(state);

		if (targetTimes != null)
		{
			state.setCost(0);
		}

		automaton.setInitialState(state);

		// Loop through the subprodecures of the main procedure in the ABBS4Controller program
		// create 2 arcs and 1 state for every path (procedure)
		State prevState;
		LabeledEvent startEvent;
		LabeledEvent stopEvent;
		IABBS4ProcedureCalls calls = main.getProcedureCalls();

		for (int i = 1; i <= calls.getCount(); i++)
		{
			String pathName = calls.item(var(i)).getProcedure().getPath().getName();

			prevState = state;

			// Create X_start event and from prevState to state
			startEvent = new LabeledEvent(pathName + "_start");

			if (!alphabet.containsEqualEvent(startEvent))
			{
				alphabet.addEvent(startEvent);
			}

			state = new State(pathName);

			// Assign cost
			if (targetTimes != null)
			{
				int cost = (int) (1000 * (targetTimes[2 * i - 2] - targetTimes[2 * i - 1]));

				state.setCost(cost);
			}

			automaton.addState(state);
			automaton.addArc(new Arc(prevState, state, startEvent));

			prevState = state;

			// Create X_finish event and from prevState to state
			stopEvent = new LabeledEvent(pathName + "_finish");

			stopEvent.setControllable(false);

			if (!alphabet.containsEqualEvent(stopEvent))
			{
				alphabet.addEvent(stopEvent);
			}

			if (i == calls.getCount())
			{    // Loop back home
				state = automaton.getInitialState();
			}
			else
			{    // Wait state
				state = new State("Wait" + i);
				automaton.addState(state);

				state.setCost(0);
			}

			automaton.addArc(new Arc(prevState, state, stopEvent));
		}

		logger.info("Automaton built successfully.");

		return automaton;
	}

	/**
	 * Finds and returns the main procedure of a mechanism's program.
	 */
	private static IABBS4Procedure getMainProcedure(IMechanism mechanism)
		throws Exception
	{
		IABBS4Modules modules = mechanism.getController().getModules();

		for (int i = 1; i <= modules.getCount(); i++)
		{
			IABBS4Procedures procedures = modules.item(var(i)).getProcedures();

			for (int j = 1; j <= procedures.getCount(); j++)
			{
				IABBS4Procedure procedure = procedures.item(var(j));

				if (procedure.getName().equals("main"))
				{
					if (procedure.getProcedureCalls().getCount() == 0)
					{
						throw new SupremicaException("Main procedure empty. Robot program not loaded?");
					}

					return procedure;
				}
			}
		}

		// There is no main procedure!! The program isn't properly loaded?
		logger.error("No main procedure found. Robot program not loaded?");

		throw new SupremicaException("No main procedure found. Robot program not loaded?");
	}

	/**
	 * Creates mutex zones that supposedly guarantees no collisions between
	 * robots. Generates span for each robot and then calculates the intersection.
 */
	public static void createMutexZonesFromSpan()
	{
		try
		{
			LinkedList robots = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleRobot);

			//LinkedList devices = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleDevice);
			//LinkedList tools = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleTool);
			//LinkedList externalAxes = getMechanismsWithRole(activeStation, RsKinematicRole.rsKinematicRoleExternalAxes);
			// Generate spans for the robots' motion
			for (int i = 0; i < robots.size(); i++)
			{
				IMechanism robot = (IMechanism) robots.get(i);

				generateSpan(robot);
			}

			//activeStation.setUCS(activeStation);
			// Calculate the intersection of the spans
			for (int i = 0; i < robots.size() - 1; i++)
			{    // Find name of span for robot i
				String iName = ((IMechanism) robots.get(i)).getName() + "_Span";
				IEntity iSpan = activeStation.getParts().item(var(iName)).getEntities().item(var(iName));

				for (int j = i + 1; j < robots.size(); j++)
				{    // Find name of span for robot j
					String jName = ((IMechanism) robots.get(j)).getName() + "_Span";
					IEntity jSpan = activeStation.getParts().item(var(jName)).getEntities().item(var(jName));

					// Calculate the intersection
					IEntities mutexEntities = iSpan.intersect(jSpan, true, mutexEntityBaseName);

					// Will crash here if more than two robots! FIXA!!
					mutexEntities.getParent().setName(mutexPartName);
					activeStation.getSelections().removeAll();
				}
			}

			// Delete the spans

			/*
			for (int i=0;i<robots.size();i++)
			{       // Find span part for robot i
					String iName = ((IMechanism) robots.get(i)).getName() + "_Span";
					activeStation.getParts().item(var(iName)).delete();
			}
			*/

			// Create automata for each entity in the IPart called mutexPartName
			createMutexAutomata();
		}
		catch (Exception ex)
		{
			logger.error("Span generation flunked. " + ex);
		}

		try
		{    // Reset UCS to WCS
			activeStation.setUCS(activeStation);
		}
		catch (Exception iCouldNotCareLess) {}
	}

	/**         * Creates mutex zones that supposedly guarantees no collisions between         * robots. The individual zones are IEntity:s in the IPart named mutexPartName.         * At present, the zones have been specified manually in this code.     */
	public static void createMutexZonesManual()
	{
		if ((app == null) || (activeStation == null))
		{
			logger.error("No connection to RobotStudio.");

			return;
		}

		try
		{    // Make sure there is a part called mutexPartName
			addPart(mutexPartName);

			// CRASHES HERE IF ZONES ALREADY CREATED! FIXA!
			// Create three boxes (IEntity:s), members of the IPart mutexPartName
			createBox(-0.25, 0.125, 0.75, 0, 0, 0, 0.5, 0.5, 0.5, "MutexZoneA");
			createBox(-0.25, -0.625, 0.75, 0, 0, 0, 0.5, 0.5, 0.5, "MutexZoneB");
			createBox(-0.125, -0.125, 0.875, 0, 0, 0, 0.25, 0.25, 0.25, "MutexZoneC");    // 3 equal boxes

			//createBox(-0.1875, 0.1875, 0.75, 0, 0, 0, 0.375, 0.375, 0.375, "MutexZoneA");
			//createBox(-0.1875, -0.5625, 0.75, 0, 0, 0, 0.375, 0.375, 0.375, "MutexZoneB");
			//createBox(-0.1875, -0.1875, 0.75, 0, 0, 0, 0.375, 0.375, 0.375, "MutexZoneC");
			// 4 equal boxes
			//createBox(-0.125, -0.5, 0.75, 0, 0, 0, 0.25, 0.25, 0.25, "MutexZoneA");
			//createBox(-0.125, -0.25, 0.75, 0, 0, 0, 0.25, 0.25, 0.25, "MutexZoneB");
			//createBox(-0.125, 0.00, 0.75, 0, 0, 0, 0.25, 0.25, 0.25, "MutexZoneC");
			//createBox(-0.125, 0.25, 0.75, 0, 0, 0, 0.25, 0.25, 0.25, "MutexZoneD");
			// Create automata for each entity in the IPart called mutexPartName
			createMutexAutomata();
		}
		catch (Exception ex)
		{
			logger.error("Error when creating mutex zones. " + ex);
		}
	}

	/**         * Creates mutex zones that supposedly guarantees no collisions between         * robots. The individual zones are IEntity:s in the IPart named mutexPartName.         * At present, the zones have been specified manually in this code.     */
	public static void createMutexZonesGrid()
	{
		if ((app == null) || (activeStation == null))
		{
			logger.error("No connection to RobotStudio.");

			return;
		}

		try
		{    // Make sure there is a part called mutexPartName
			addPart(mutexPartName);

			// Grid with boxes of size (side * side * side) (m^3)
			//activeStation.setFloorDepth(1);
			//activeStation.setFloorWidth(1);
			//logger.info("Floor depth: " + activeStation.getFloorDepth() + ", floor width: " + activeStation.getFloorWidth() + ".");
			double side = .33;
			double width = 1;    // [m]
			double depth = 1;    // [m]

			//double xSize = activeStation.getFloorWidth();
			double xSize = width;
			double x = Math.round(xSize / side);    //double ySize = activeStation.getFloorDepth();
			double ySize = depth;
			double y = Math.round(ySize / side);
			double zSize = 1.5;
			double z = Math.round(zSize / side);    // Nested for-loop in three dimensions!

			for (int i = 0; i < x; i++)
			{
				for (int j = 0; j < y; j++)
				{
					for (int k = 0; k < z; k++)
					{
						createBox((i - x / 2) * side, (j - y / 2) * side, k * side, 0, 0, 0, side, side, side, "Mutex" + i + j + k);
					}
				}
			}

			// Create automata for each entity in the IPart called mutexPartName
			createMutexAutomata();
		}
		catch (Exception ex)
		{
			logger.error("Error when creating mutex zones. " + ex);
		}
	}

	/**
	 * Creates automata, one for each zone, with two states.
	 */
	private static void createMutexAutomata()
		throws Exception
	{

		// Create automata representing the mutexzones (one per zone)
		IPart mutexPart = activeStation.getParts().item(var(mutexPartName));
		IEntities mutexEntities = mutexPart.getEntities();

		mutexAutomata.clear();

		for (int i = 1; i <= mutexEntities.getCount(); i++)
		{    // Construct automaton
			String entityName = mutexEntities.item(var(i)).getName();
			Automaton automaton = new Automaton(entityName);

			automaton.setType(AutomatonType.Specification);

			// Add two states, Free and Booked
			State state = new State("Free");
			automaton.addState(state);
			state.setAccepting(true);
			automaton.setInitialState(state);

			state = new State("Booked");
			automaton.addState(state);

			mutexAutomata.addAutomaton(automaton);
			gui.addAutomaton(automaton);
		}
	}

	/**
	 * Creates transparent box (intended to be a mutual exclusion volume).
	 *
	 * @param name The name the mutexbox
	 */
	private static void createBox(double tx, double ty, double tz, double rx, double ry, double rz, double wx, double wy, double wz, String name)
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

		IEntity newBox = mutexPart.createSolidBox(transform, wx, wy, wz);

		// Very blue but relatively transparent and with a catchy name
		newBox.setVisible(false);
		newBox.setRelativeTransparency((float) 0.75);

		//newBox.setRelativeTransparency((float) 0.9);
		newBox.setColor(new Variant(new SafeArray(new int[]{ 0, 0, 255 }), false));
		newBox.setName(name);
		newBox.setVisible(true);
	}

	/**
	 * Generates a 3D object (IEntity) representing the span of robot.
	 */
	public static void generateSpan(IMechanism robot)
		throws Exception
	{

		// Start controller
		IABBS4Controller controller = startController(robot);

		// Start simulation listener
		ISimulation simulation = activeStation.getSimulations().item(var(1));
		Simulation sim = Simulation.getSimulationFromUnknown(simulation);
		SpanGenerator simulationListener = new SpanGenerator(robot);

		sim.addDSimulationEventsListener(simulationListener);

		// Start a thread running the simulation in RobotStudio
		simulation.start();

		// Wait for the simulation to stop
		simulationListener.waitForSimulationStop();
		sim.removeDSimulationEventsListener(simulationListener);

		// Shut down controller
		// If you DON'T do this, BOTH robots move!! Coooooool.
		stopController(robot);
	}

	/**
	 * Analyzes automata and times to make sure mutexAutomaton is
	 * synchronized with robotAutomaton.
	 */
	private static void synchronize(Automaton mutexAutomaton, Automaton robotAutomaton, double[][] collisionTimes, double[] targetTimes)
		throws Exception
	{
		int j = 0;
		int k = 0;
		State state = robotAutomaton.getInitialState();
		LabeledEvent event = state.outgoingArcsIterator().nextEvent();
		State freeState = mutexAutomaton.getStateWithName("Free");
		State bookedState = mutexAutomaton.getStateWithName("Booked");

		for (int i = 0; i < collisionTimes.length; i++)
		{

			// Start time
			double startTime = collisionTimes[i][0];

			while (startTime > targetTimes[j])
			{
				j++;
			}

			// Add Free->Booked arc with the event leaving the j-1:th state
			for (; k < j - 1; k++)
			{
				state = state.nextState(event);
				event = state.outgoingArcsIterator().nextEvent();
			}

			mutexAutomaton.getAlphabet().addEvent(event);
			mutexAutomaton.addArc(new Arc(freeState, bookedState, event));

			// End time
			double endTime = collisionTimes[i][1];

			while (endTime > targetTimes[j])
			{
				j++;
			}

			// Add Booked->Free arc with event the j:th state
			for (; k < j; k++)
			{
				state = state.nextState(event);
				event = state.outgoingArcsIterator().nextEvent();
			}

			mutexAutomaton.getAlphabet().addEvent(event);
			mutexAutomaton.addArc(new Arc(bookedState, freeState, event));
		}
	}

	public static void executeRobotAutomaton(Automaton automaton)
	{
		try
		{
			IABBS4Controller controller;

			try
			{
				IMechanism robot = activeStation.getMechanisms().item(var(automaton.getName()));

				controller = startController(robot);
			}
			catch (Exception ex)
			{
				logger.error("Virtual Controller could not be started.");

				return;
			}

			// Dialog with simple execution gui
			new ExecuterWindow(controller, automaton);
		}
		catch (Exception e)
		{
			logger.error("Couldn't find robot or controller.");

			return;
		}
	}

	/**
	 * Starts thread that moves robot along path. The controller must
	 * already be started for the (active) mechanism robot.
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
	public static IMechanism executeEvent(String eventName)
		throws Exception
	{

		// Test if this is a (controllable) "path-starting" event
		if (!eventName.endsWith("_start"))
		{
			throw new SupremicaException("Could not execute event. Event not controllable?");
		}

		// Find path name and the IPath
		String pathName = eventName.substring(0, eventName.length() - 6);    // 6 is the length of "_start"
		IPath path = activeStation.getPaths().item(var(pathName));
		IMechanism robot = getMechanismFromPath(path);

		// Make sure the virtual controller is started for this robot
		startController(robot);

		// Move robot along path
		moveRobotAlongPath(robot, path);

		return robot;
	}

	/**
	 * Starts the IABBS4Controller for robot if it is not already started.
	 *
	 * The lines marked "\\Unnecessary" at the end can be removed but they
	 * are intended to have a function... that doesn't seem to be supported
	 * by RS3.0, reporting starting of the VC, see ControllerListener.
	 */
	public static IABBS4Controller startController(IMechanism robot)
		throws Exception
	{
		activeStation.setActiveMechanism((IMechanism2) robot);

		// Already started?
		IABBS4Controller controller;

		try
		{
			controller = robot.getController();

			return controller;
		}
		catch (Exception e)
		{

			// No controller started for this robot. Start one!
			// Do we have to shut down any controllers that are running?
		}

		// Add ControllerListener to the robot so we can listen to the controller
		Mechanism mech = Mechanism.getMechanismFromUnknown(robot);    //Unnecessary
		ControllerListener controllerListener = new ControllerListener(false);    //Unnecessary

		mech.add_MechanismEventsListener(controllerListener);    //Unnecessary

		// Start virtual controller
		controller = robot.startABBS4Controller(true);

		// We don't have to wait! In RS3.0, the above call won't return until
		// the controller is started!
		// Wait for controller start
		//controllerListener.waitForControllerStart();
		//D H: Is there a bug in RS2.1? the event afterControllerStarted doesn´t seem firing
		//Thread.sleep(3500);
		// We're ready! Stop listening!
		mech.remove_MechanismEventsListener(controllerListener);    //Unnecessary

		// Return the controller
		return controller;
	}

	/**
	 * Stops the controller for robot.
	 */
	public static void stopController(IMechanism robot)
		throws Exception
	{

		// The controller should be up and running!
		IABBS4Controller controller;

		try
		{
			controller = robot.getController();
		}
		catch (Exception e)
		{

			// No controller started for this robot? Strange!
			logger.warn("Attempt to shut down an already shut down controller.");

			return;
		}

		// Add ControllerListener to the robot so we can listen to the controller
		Mechanism mech = Mechanism.getMechanismFromUnknown(robot);
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
		{
			throw new SupremicaException("Path " + path.getName() + " is member of an illegal frame, \"" + frameName + "\".");
		}

		// Get the name of the mechanism, leave out the "<" and the ">Elements".
		String mechName = frameName.substring(1, frameName.length() - 9);    // 9 is the length of ">Elements"

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

	/**
	 * Shuts down RobotStudio.
	 */
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

		// Release objects
		app = null;
		activeStation = null;
		mutexAutomata = null;
		robotAutomata = null;
	}

	// DAppEvents interface methods
	public void selectionChanged()
	{

		//logger.info("Selection changed.");
	}

	public void quit()
	{

		// This is fatal, cause this has never ever happened
		// and I'd like to know if it ever does! (It should!)
		logger.fatal("RobotStudio is shutting down. Please tell Hugo if and how you get this message!");
		clean();
	}

	public int stationBeforeOpen(String Path, boolean[] Cancel)
	{

		//logger.info("Station being opened...");
		return 0;
	}

	public int stationAfterOpen(org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.Station Station)
	{
		logger.info("Station opened.");

		return 0;
	}

	public int stationBeforeSave(org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.Station Station, boolean[] Cancel)
	{

		//logger.info("Station being saved...");
		return 0;
	}

	public int stationAfterSave(org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.Station Station)
	{
		logger.info("Station saved...");

		return 0;
	}

	public int libraryBeforeOpen(String FileName, boolean[] Cancel)
	{

		//logger.info("Library being opened...");
		return 0;
	}

	public int libraryAfterOpen(org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.RsObject RsObject)
	{

		//logger.info("Library opened.");
		return 0;
	}

	public int libraryBeforeSave(org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.RsObject RsObject, boolean[] Cancel)
	{

		//logger.info("Library being saved...");
		return 0;
	}

	public int libraryAfterSave(org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.RsObject RsObject)
	{

		//logger.info("Library saved.");
		return 0;
	}

	public int started()
	{
		logger.info("RobotStudio started.");

		return 0;
	}

	/*
	private static class ExecuterWindow extends _MechanismEventsAdapter implements ActionListener
	{
			State state;

			java.awt.Frame frame;

			Label  l1 = new Label("Event", Label.RIGHT);
			Choice c1 = new Choice();

			Button b1 = new Button("Execute");
			Button b2 = new Button("Quit");

			ExecuterWindow(Automaton automaton)
			{
					frame = new java.awt.Frame();

					frame.setTitle("Executer Window");
					frame.setIconImage(Supremica.cornerImage);
					try
					{
							state = automaton.getInitialState();
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
							String eventName = c1.getSelectedItem();
							c1.removeAll();
							state = state.nextState(state.outgoingArcsIterator().nextEvent());
							update();
							String pathName = state.getName();
							try
							{
									IMechanism robot = executeEvent(eventName);
									Mechanism mech = Mechanism.getMechanismFromUnknown(robot);
									mech.add_MechanismEventsListener(this);
									logger.error("Move it!");
									moveRobotAlongPath(robot, robot.getPaths().item(var(pathName)));
							}
							catch (Exception ex)
							{
									logger.error("Could not move robot. " + ex);
							}
					}
					else if (e.getSource() == b2)
					{
							frame.setVisible(false);
							try
							{
									//IABBS4Controller controller = robot.getController();
									IABBS4Controller controller = activeStation.getActiveMechanism().getController();
									controller.shutDown();
									//logger.info("Sleeping...");
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
	*/
	private static class ExecuterWindow
		extends _MechanismEventsAdapter
		implements ActionListener
	{
		IMechanism robot;
		State state;
		java.awt.Frame frame;
		Label l1 = new Label("Event", Label.RIGHT);
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
			catch (Exception whatever) {}

			frame.setLayout(new FlowLayout());

			// FIXA!!
			frame.add(l1);
			frame.add(c1);
			frame.add(b1);
			frame.add(b2);
			b1.addActionListener(this);
			b2.addActionListener(this);
			update();
			frame.pack();
			frame.setVisible(true);
		}

		public void update()
		{
			c1.removeAll();

			for (ArcIterator arcIt = state.outgoingArcsIterator();
					arcIt.hasNext(); )
			{
				LabeledEvent event = arcIt.nextEvent();
				String name;

				if (!event.isControllable())
				{
					name = "!" + event.getLabel();
				}
				else
				{
					name = event.getLabel();
				}

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
				catch (Exception whatever) {}
			}
			else if (e.getSource() == b2)
			{
				frame.setVisible(false);

				try
				{

					//IABBS4Controller controller = activeStation.getActiveMechanism().getController();
					//controller.shutDown();
					//Thread.sleep(2500);
					//logger.info("Controller shut down");
					stopController(activeStation.getActiveMechanism());
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
			catch (Exception seeIfICare) {}

			return 0;
		}
	}

	private static class RobotMoverThread
		extends Thread
	{
		IMechanism robot;
		IPath path;

		public RobotMoverThread(IMechanism robot, IPath path)
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

	/**
	 * Keeps track of the controller status, is it started or shut down?
	 * The _MechanismEventsAdapter lacks information about the current mechanism
	 * and its controller but this information is supplied in this class...
	 */
	public static class ControllerListener
		extends _MechanismEventsAdapter
	{
		private boolean controllerRunning = false;    // Used in the wait-methods

		/**
		 * Initialize the listener with the isRunning argument.
		 */
		public ControllerListener(boolean isRunning)
		{
			controllerRunning = isRunning;
		}

		/**
		 * Returns when the controller has started
		 *
		 * This method is not used (?) because it's need seems to have
		 * disappeared with RS3.0.
		 */
		public synchronized void waitForControllerStart()
		{
			try
			{
				while (!controllerRunning)
				{
					wait();
				}

				// Make sure the controller is really started before we return
				//Thread.sleep(3000);
			}
			catch (Exception ex)
			{

				//System.out.println("Interrupted! " + ex);
				logger.error("Interrupted! " + ex);
			}

			return;
		}

		/**
		 * Returns when the controller has shut down
		 */
		public synchronized void waitForControllerShutDown()
		{
			try
			{
				while (controllerRunning)
				{
					wait();
				}

				// Make sure the controller is really shut down before we return
				//Thread.sleep(2500);
			}
			catch (Exception ex)
			{

				//System.out.println("Interrupted! " + ex);
				logger.error("Interrupted! " + ex);
			}

			return;
		}

		// Implementation of _MechanismEventsAdapter methods
		public int beforeControllerStarted()
		{

			// This method works fine... but is quite useless here? Post some info...
			logger.info("Starting Virtual Controller...");

			return 0;
		}

		public int afterControllerStarted()
		{

			// This never happens!? (But since RS3.0 we don't seem to need it!?)
			controllerRunning = true;

			logger.fatal("AfterControllerStarted. Tell Hugo you got this message!");
			notify();

			return 0;
		}

		public int afterControllerShutdown()
		{

			// Works fine?
			controllerRunning = false;

			logger.info("Virtual Controller shut down.");
			notify();

			return 0;
		}
	}

	/**
	 * Collision detection stuff.
	 */
	private static class CollisionListener
		extends _MechanismEventsAdapter
	{
		private static Logger logger = LoggerFactory.createLogger(CollisionListener.class);
		private IABBS4Controller controller = null;
		private LinkedList targetTimes = new LinkedList();    // Doubles of targetReached-times
		private LinkedList collisions = new LinkedList();    // CollisionDatas of collisionStart/End-times
		private boolean leavingTarget = true;    // targetReached is invoked twice for every Target!

		// Junk constructed by me
		public CollisionListener(IABBS4Controller controller)
		{
			this.controller = controller;
		}

		public double[] getTargetTimes()
		{

			// Return array of times when targets were reached
			double[] times = new double[targetTimes.size()];

			for (int i = 0; i < times.length; i++)
			{
				times[i] = ((Double) targetTimes.get(i)).doubleValue();
			}

			return times;
		}

		public double[][] getCollisionTimes(String mutexZone)
		{

			// Return array of times corresponding to collisions with mutexZone
			int index = collisions.indexOf(new CollisionData(mutexZone));

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

		/**
		 * Creates a new target at the current position. The target will be
		 * named targetName:x if targetName is not too long (16 characters?).
		 */
		private void createTargetAtTCP(String targetName)
			throws Exception
		{

			// Create a new target
			IMechanism mechanism = controller.getMechanism();
			ITarget newTarget = mechanism.getWorkObjects().item(var(1)).getTargets().add();
			IToolFrame toolFrame = mechanism.getActiveToolFrame();

			newTarget.setTransform(toolFrame.getTransform());

			// Set a catchy name
			boolean ok = false;
			String suffix = ":1";
			int nbr = 1;

			while (!ok)
			{
				try
				{
					newTarget.setName(targetName + suffix);

					ok = true;
				}
				catch (Exception e)
				{
					suffix = ":" + ++nbr;

					if (nbr >= 10)
					{
						ok = true;
					}
				}
			}
		}

		// Implementation of _MechanismEventsAdapter methods
		public int targetReached()
		{
			try
			{
				double motionTime = controller.getMotionTime();

				targetTimes.add(new Double(motionTime));

				if (!leavingTarget)
				{
					logger.info("Target reached at time " + (float) motionTime);
				}

				leavingTarget = !leavingTarget;
			}
			catch (Exception seeIfICare) {}

			return 0;
		}

		public int selected()
		{
			logger.fatal("Selected");

			return 0;
		}

		public int unSelected()
		{
			logger.fatal("Unselected");

			return 0;
		}

		public int tick(float systemTime)
		{

			// This is fatal, cause this has never ever happened
			// and I'd like to know if it ever does!
			logger.fatal("Mechtick: " + systemTime + ", please tell Hugo if and how you get this message.");

			return 0;
		}

		/**
		 * This method is called each time a PART of the robot starts its collision with
		 * something. In general, this does NOT mean that the whole robot has just started colliding.
		 * Therefore, we must count the collision starts...
		 */
		public synchronized int collisionStart(RsObject collidingObject)
		{
			try
			{

				// What did we collide with?
				String objectName = collidingObject.getName();
				CollisionData data;

				if (!collisions.contains(new CollisionData(objectName)))
				{
					data = new CollisionData(objectName);

					data.setCount(0);
					collisions.add(data);
				}
				else
				{
					data = (CollisionData) collisions.get(collisions.indexOf(new CollisionData(objectName)));
				}

				// Is this THE start?
				if (data.getCount() == 0)
				{

					// Log message
					data.setStartTime(controller.getMotionTime());
					logger.info("Start of collision with " + objectName + " at time " + (float) data.startTime);

					// Create a target at the zone border!
					createTargetAtTCP(controller.getMechanism().getName() + "Enter" + objectName);
				}

				// Count the starts...
				data.setCount(data.getCount() + 1);
			}
			catch (Exception whatever) {}

			return 0;
		}

		/**
		 * This method is called each time a PART of the robot stops its collision with
		 * something. In general, this does NOT mean that the whole robot has stopped colliding.
		 * Therefore, we must count the collision ends...
		 */
		public synchronized int collisionEnd(org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.RsObject collidingObject)
		{
			try
			{

				// What did we end colliding with?
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

				// Count the ends
				data.setCount(data.getCount() - 1);

				// Is this THE end?
				if (data.getCount() == 0)
				{
					data.setEndTime(controller.getMotionTime());
					logger.info("End of collision with " + objectName + " at time " + (float) data.endTime);
					createTargetAtTCP(controller.getMechanism().getName() + "Exit" + objectName);
				}
			}
			catch (Exception whatever) {}

			return 0;
		}

		/**
		 * An object containing data concerning the collision with a certain object
		 */
		private class CollisionData
		{

		/** Name of colliding object. */
		private String name;

			/** Number of started collisions. */
			private int count;

			/** Collision starting time. */
			private double startTime;

			/** Collision ending time. */
			private double endTime;
			private LinkedList timeList = new LinkedList();

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

				timeList.add(new double[]{ startTime, endTime });
			}

			public double[][] getTimes()
			{
				double[][] times = new double[timeList.size()][2];

				for (int i = 0; i < timeList.size(); i++)
				{
					times[i] = (double[]) timeList.get(i);
				}

				return times;
			}
		}
	}

	/**
	 * A listener for determining when a simulation is finished.
	 */
	private static class SimulationListener
		extends DSimulationEventsAdapter
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

				//simulationRunning = true;
				while (simulationRunning)
				{
					wait();
				}

				// Make sure the simulation is really over before we return
				//Thread.sleep(2500);
			}
			catch (Exception ex)
			{

				//System.out.println("Interrupted! " + ex);
				logger.error("Interrupted! " + ex);
			}

			return;
		}
	}

	/**
	 * Adds part to activeStation and returns it. If there already was
	 * a part with the same name, it is returned instead.
	 */
	private static IPart addPart(String name)
	{
		IPart part = null;

		try
		{
			try
			{

				// If there already is one, get it!
				activeStation = app.getActiveStation();
				part = activeStation.getParts().item(var(name));
			}
			catch (ComJniException ex)
			{

				// If there was none, create it!
				if (ex.ErrorCode == HResult.E_FAIL)
				{    // No such item, construct one!
					part = activeStation.getParts().add();

					part.setName(name);
				}
				else
				{    //Something is really wrong
					logger.error("Something is wrong! " + ex);
				}
			}
		}
		catch (Exception ex)
		{
			logger.error("Error! " + ex);
		}

		return part;
	}

	/**
	 * A listener that listens throughout a simulation and generates spans.
	 */
	private static class SpanGenerator
		extends SimulationListener
	{
		private IMechanism robot;
		private IPart spanPart;
		private IToolFrame tool0;
		private static RsUnitsUtility ruu;

		// The size of the box surrounding the tooltip
		private static double boxSize = 0.12;    // [m]
		private static double cylinderLength = 1.15;    // [m]
		private static double cylinderRadius = 0.06;    // [m]
		private static double stepSize = boxSize * 3 / 4;    // [m]

		// The margin that should be added to all sides
		private static double margin = 0.05;    // [m]
		private Transform oldTransform;

		public SpanGenerator(IMechanism robot)
			throws Exception
		{
			this.robot = robot;

			if (ruu == null)
			{
				ruu = new RsUnitsUtility();
			}

			// Add part for the span to the station
			spanPart = addPart(robot.getName() + "_Span");

			spanPart.setVisible(true);    // Why? For fun!

			tool0 = robot.getToolFrames().item(var("tool0"));

			//robot.setActiveToolFrame(tool0);
			oldTransform = transformCopy(tool0.getTransform());

			createSpanEntity(oldTransform);
		}

		// Events generated by RobotStudio.ISimulation
		public void tick(double time)
		{

			// In each tick, examine if it is time to generate a new spanEntity
			try
			{
				ITransform newTransform = tool0.getTransform();
				double dx = oldTransform.getX() - newTransform.getX();
				double dy = oldTransform.getY() - newTransform.getY();
				double dz = oldTransform.getZ() - newTransform.getZ();

				if (Math.sqrt(dx * dx + dy * dy + dz * dz) > stepSize)
				{
					createSpanEntity(newTransform);

					oldTransform = transformCopy(newTransform);
				}
			}
			catch (Exception ex)
			{
				logger.error("Error in RobotStudioLink.SpanGenerator.tick " + ex);
			}
		}

		public void stop()
		{

			// Generate the union of the spanEntities
			try
			{
				IEntities spanEntities = spanPart.getEntities();
				IEntity unionEntity;

				if (spanEntities.getCount() >= 2)
				{    // At least two spanEntities
					unionEntity = spanEntities.item(var(2));

					IPart oldPart = null;

					for (int i = spanEntities.getCount(); i > 1; i--)
					{
						IPart part = unionEntity.getParent();

						//logger.info("Joining " + unionEntity.getName() + " and " + spanEntities.item(var(1)).getName());
						unionEntity = unionEntity.join(spanEntities.item(var(1)), false);

						//spanPart.addEntity(unionEntity);
						if (oldPart != null)
						{
							oldPart.delete();
						}

						oldPart = unionEntity.getParent();
					}
				}
				else
				{    // Only one spanEntity
					unionEntity = spanEntities.item(var(1));
				}

				spanPart.delete();
				unionEntity.setName(robot.getName() + "_Span");
				unionEntity.getParent().setName(robot.getName() + "_Span");
				unionEntity.setRelativeTransparency((float) 0.9);
				activeStation.getSelections().removeAll();
			}
			catch (Exception ex)
			{
				logger.error("Span uniting failed! " + ex);
			}

			super.stop();
		}

		private static Transform transformCopy(ITransform transform)
			throws Exception
		{
			Transform copy = new Transform();

			copy.setX(transform.getX());
			copy.setY(transform.getY());
			copy.setZ(transform.getZ());
			copy.setRx(transform.getRx());
			copy.setRy(transform.getRy());
			copy.setRz(transform.getRz());

			return copy;
		}

		private void createSpanEntity(ITransform transform)
			throws Exception
		{

			// Calculate box transform
			activeStation.setUCS(tool0);

			ITransform boxTransform = ruu.wCSToUCS(transform);

			boxTransform.setX(boxTransform.getX() - boxSize / 2 - margin);
			boxTransform.setY(boxTransform.getY() - boxSize / 2 - margin);
			boxTransform.setZ(boxTransform.getZ() - boxSize - margin);

			boxTransform = ruu.uCSToWCS(boxTransform);

			// Calculate cylinder transform
			IPart upperArm = robot.getLinks().item(var("Link4")).getParts().item(var(1));

			activeStation.setUCS(upperArm);

			ITransform cylinderTransform = ruu.wCSToUCS(upperArm.getTransform());

			cylinderTransform.setZ(cylinderTransform.getZ() + 1.195);
			cylinderTransform.setX(cylinderTransform.getX() - 0.3 - margin);
			cylinderTransform.setRy(cylinderTransform.getRy() + Math.PI / 2);

			cylinderTransform = ruu.uCSToWCS(cylinderTransform);

			// Create cylinder around the upper arm
			spanPart.createSolidCylinder(cylinderTransform, cylinderRadius + margin, cylinderLength + 2 * margin);

			// Create box around the tooltip
			spanPart.createSolidBox(boxTransform, boxSize + 2 * margin, boxSize + 2 * margin, boxSize + 2 * margin);
		}
	}

	// used by CreateXml
	public static void configureCreateXml()
	{
		if ((app == null) || (activeStation == null))
		{
			logger.error("No connection to RobotStudio.");

			return;
		}

		CreateXml.configureStation(activeStation);
	}
}

/*  FROM VB!

Dim WithEvents sim As Simulation
Dim mech As Mechanism
Dim coll As New RsCollection
Dim joinedEnt As Entity
Dim wristoldpos As New Transform
Dim j4oldpos As New Transform

Sub Init()
	Set sim = ActiveStation.Simulations(1)
	Set mech = ActiveStation.ActiveMechanism
	wristoldpos.x = 0
	wristoldpos.y = 0
	wristoldpos.z = 0
	coll.Clear
End Sub



Private Sub sim_Stop()
'joine the created entities
Set joinedEnt = coll(1)
Dim i As Integer
Dim ent As Entity
For i = 2 To coll.Count
	On Error Resume Next
	Set ent = joinedEnt.Join(coll(i), False)
	Set joinedEnt = ent
Next i
'set new name
joinedEnt.Parent.Name = mech.Name & "_volume"


End Sub

Private Sub sim_Tick(ByVal Time As Double)
	'create a box around the wrist, use the attached toolframe 'b'
	Dim trans As New Transform
	Dim xdiff, ydiff, zdiff, dist As Double
	Set trans = mech.ToolFrames("b").Transform
	xdiff = (wristoldpos.x - trans.x) * (wristoldpos.x - trans.x)
	ydiff = (wristoldpos.y - trans.y) * (wristoldpos.y - trans.y)
	zdiff = (wristoldpos.z - trans.z) * (wristoldpos.z - trans.z)
	dist = Sqr(xdiff + ydiff + zdiff)
		If dist > 0.05 Then
		Call coll.Add(ActiveStation.Parts.Add.CreateSolidBox(trans, 0.1, 0.1, 0.1))
		wristoldpos.Position = trans.Position
	 End If

	' create a cylinder around upper arm, use the attached toolframe 'a' but compare with the jointaxis
	Set trans = mech.ToolFrames("a").Transform
	Dim axisPos As New Position
	Set axisPos = mech.Joints(4).JointAxis.Position

	xdiff = (j4oldpos.x - axisPos.x) * (j4oldpos.x - axisPos.x)
	ydiff = (j4oldpos.y - axisPos.y) * (j4oldpos.y - axisPos.y)
	zdiff = (j4oldpos.z - axisPos.z) * (j4oldpos.z - axisPos.z)
	dist = Sqr(xdiff + ydiff + zdiff)
		If dist > 0.04 Then
		Call coll.Add(ActiveStation.Parts.Add.CreateSolidCylinder(trans, 0.04, 0.6))
		j4oldpos.Position = axisPos
	 End If

End Sub







*/
