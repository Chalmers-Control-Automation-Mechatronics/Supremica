
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
package org.supremica.external.robotCoordinationABB;


import org.supremica.util.SupremicaException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.*;
import org.supremica.automata.*;
import org.supremica.log.*;
import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import com.inzoom.comjni.Variant;
import com.inzoom.comjni.SafeArray;
import com.inzoom.comjni.ComJniException;
import com.inzoom.comjni.enums.HResult;

public class CreateXml
{
	private static Logger logger = LoggerFactory.createLogger(CreateXml.class);

	/** The name of the current active station. */
	private static String stationName = null;
	private static Document stationDocument = null;
	private static File file = null;
	private static String nameOfRobot = null;

	// Strings characterizing the RobotStudio Station
	private final static String mutexZonesName = "MutexZones";
	private final static String mutexZoneName = "MutexZone";
	private final static String pathName = "Path";
	private final static String robotName = "Robot";

	// Strings characterizing the types in the xml file
	private final static String homeType = "home";
	private final static String weldType = "wp";
	private final static String enterType = "enterZone";
	private final static String exitType = "exitZone";
	private static int nbrOfTimesCollision = 0;

	// List containing costs for the xml file
	private static LinkedList[] robotCosts;
	private static IStation station;
	private static Element rootStation;

	/*
	public static void main(String[] args) throws JDOMException
	{
			stationName = "MyStation";
			createXmlStation();
			addSome("MutexZones");
			try
			{
					save();
			}
			catch (Exception e)
			{
					System.out.println("Error while saving...");
			}

	}
	*/
	public static void buildXmlFile()
	{
		logger.info("Building the xml file...");

		try
		{
			file = new File(stationName + ".xml");

			for (int j = 1; file.exists(); j++)
			{
				file = new File(stationName + j + ".xml");
			}

			rootStation = new Element(stationName);
			stationDocument = new Document(rootStation);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}

		try
		{
			addMutexZonesXml();
		}
		catch (Exception e)
		{
			logger.error("Error while adding Mutex Zones to the xml file.");

			return;
		}

		try
		{
			addRobotPathsXml();
		}
		catch (Exception e)
		{
			logger.error("Error while adding Robot Paths to the xml file.");

			return;
		}

		try
		{
			save();
		}
		catch (Exception e)
		{
			logger.error("Error while saving...");

			return;
		}

		logger.info("Xml file built successfully.");
	}

	private static void addMutexZonesXml()
		throws Exception
	{
		IPart mutexZonesPart = station.getParts().item(var(mutexZonesName));
		Element mutexZones = new Element(mutexZonesName);

		rootStation.getChildren().add(mutexZones);

		for (int i = 1; i <= mutexZonesPart.getEntities().getCount(); i++)
		{
			IEntity zone = mutexZonesPart.getEntities().item(var(i));
			Element mutexZone = new Element(mutexZoneName);

			mutexZones.getChildren().add(mutexZone);
			mutexZone.setAttribute("name", zone.getName());
		}
	}

	/**
	 * Creates all the paths in the xml file: all the paths and the intersection points
	 * are in Robot Studio. The function writes them into the xml file and adds the right attributes
	 * according to the model decided.
	 */
	private static void addRobotPathsXml()
		throws Exception
	{
		for (int i = 1; i <= station.getMechanisms().getCount(); i++)
		{
			Element robotElement = new Element(robotName);

			rootStation.getChildren().add(robotElement);

			IMechanism robotMechanism = station.getMechanisms().item(var(i));

			nameOfRobot = robotMechanism.getName();

			robotElement.setAttribute("name", nameOfRobot);

			int nbrOfPaths = robotMechanism.getPaths().getCount();

			for (ListIterator it = robotCosts[i - 1].listIterator();
					it.hasNext(); )
			{
				PathWithCosts thispath = (PathWithCosts) it.next();
				LinkedList costs = thispath.getCosts();
				IPath thisPath = robotMechanism.getPaths().item(var(thispath.getName()));
				Element path = new Element(pathName);
				String nameOfPath = thisPath.getName();

				path.setAttribute("name", nameOfPath);
				robotElement.getChildren().add(path);

				// insert main points
				ListIterator costIterator = costs.listIterator();

				for (int k = 1; k <= thisPath.getTargetRefs().getCount(); k++)
				{
					Element point = new Element("Point");
					String nameOfPoint = thisPath.getTargetRefs().item(var(k)).getTarget().getName();

					//logger.info(nameOfPoint);  // debug phase
					point.setAttribute("name", zoneOrPointGet(nameOfPoint));    // this is the name of the zone
					point.setAttribute("type", typeGet(nameOfPoint));

					String xmlCost = null;

					try
					{
						xmlCost = (String) costIterator.next();

						point.setAttribute("cost", xmlCost);
						path.getChildren().add(point);
					}
					catch (Exception e)
					{
						logger.error("Error while storing the costs into the xml file " + xmlCost);

						throw new SupremicaException();
					}
				}
			}

			try
			{
				addMotionsXml(robotElement);
			}
			catch (Exception e)
			{
				logger.error("Error adding motions to the xml file.");

				throw new SupremicaException();
			}
		}
	}

	/**
	 * Creates all the possible motions from Home to Home and add them to the xml file
	 */
	private static void addMotionsXml(Element robotElementMotion)
		throws Exception
	{
		String robotName = robotElementMotion.getAttribute("name").getValue();
		IMechanism thisRobot = station.getMechanisms().item(var(robotName));

		// Skip the points added during the simulations
		ITargets targets = thisRobot.getWorkObjects().item(var(1)).getTargets();
		int nbrOfPoints = targets.getCount();
		LinkedList realTargets = new LinkedList();
		int nbrOfRealTargets = 0;

		for (int i = 1; i <= nbrOfPoints; i++)
		{
			String targetName = targets.item(var(i)).getName();

			//targetName = targetName.substring(0,targetName.length()-2);          // cut off ":1"
			if (!(targetName.startsWith("Home") || targetName.startsWith("In") || targetName.startsWith("Out")))
			{
				realTargets.add(targetName);

				nbrOfRealTargets++;
			}
		}

		Element motions = new Element("AllowedMotions");

		robotElementMotion.getChildren().add(motions);

		// Initialization for the array of Strings with the targets names
		String[] elements = new String[nbrOfRealTargets];
		int nbrOfTargets = 0;

		for (ListIterator it = realTargets.listIterator(); it.hasNext(); )
		{
			elements[nbrOfTargets] = (String) it.next();

			nbrOfTargets++;
		}

		// Create permutation of targets since each target has to be reached
		PermutationGenerator targetsOrder = new PermutationGenerator(nbrOfTargets);
		int motionIndex = 0;

		while (targetsOrder.hasMore())
		{
			Element motion = new Element("Motion");

			motion.setAttribute("name", thisRobot.getName() + motionIndex);

			// Add Home point at the beginning and at the end of path's targets
			String[] path = new String[nbrOfTargets + 2];
			String homepoint = getHomePoint(thisRobot).getName();

			path[0] = homepoint;    //.substring(0,homepoint.length()-2);   // cut off ":1"
			path[nbrOfTargets + 1] = path[0];

			int[] indices = targetsOrder.getNext();

			for (int j = 1; j <= nbrOfTargets; j++)
			{
				path[j] = elements[indices[j - 1]];
			}

			// Add paths to motions
			boolean existingPath = true;

			for (int l = 0; l < nbrOfTargets + 1; l++)
			{

				//String pathString = new String();
				// pathString = path[l] + path[l+1];    // not all the paths are named P1P2
				String pathString = searchPath(thisRobot, path[l], path[l + 1]);

				if (pathString != null)
				{
					Element pathElement = new Element("Path");

					pathElement.setAttribute("name", pathString);
					motion.getChildren().add(pathElement);
				}
				else
				{
					existingPath = false;
				}
			}

			if (existingPath)
			{
				motionIndex++;

				motions.getChildren().add(motion);
			}
		}
	}

	/**
	 * The function makes the following actions:
	 *  - creates an empty MAIN procedure (after cleaning up what there was before in the main);
	 *  - adds a new module to the mechanism.
	 * For each path of the robot:
	 *  - synchronizes (within the module created) the path to the Virtual Controller in Robot Studio.
	 */
	private static void syncModulesAndPaths(IMechanism robot)
		throws Exception
	{
		logger.info("Starting VC for " + robot.getName() + " ...");

		IABBS4Controller controller = org.supremica.automata.algorithms.RobotStudioLink.startController(robot);

		Thread.sleep(5500);
		logger.info("VC started.");

		IABBS4Modules modules = controller.getModules();
		IABBS4Procedure mainProcedure = getMainProcedure(robot);

		if (mainProcedure == null)
		{

			//IABBS4Module mainModule = createIfNotExists(modules,"Program");
			IABBS4Module mainModule = createIfNotExists(modules, "Program");    //BUG? why does not it work if Program is already present? delete it and then create again

			//noModule.delete();
			//IABBS4Module mainModule = modules.add(var("Program"),1);
			Thread.sleep(1000);

			Variant mainName = var("main");

			//mainProcedure = module.getProcedures().add(mainName,true); // this way does not work!!!!
			mainProcedure = mainModule.getProcedures().add(mainName);

			Thread.sleep(2000);

			//logger.info("Main procedure added.");
		}

		// delete all the procedure calls in the main
		while (mainProcedure.getProcedureCalls().getCount() > 0)
		{
			mainProcedure.getProcedureCalls().item(var(1)).delete();
		}

		/*
		for (int i=1; i<=mainProcedure.getProcedureCalls().getCount(); i++)
				mainProcedure.getProcedureCalls().item(var(i)).delete();
		*/

		// adds all the possible paths to the module if not existing
		IABBS4Module pathsModule = createIfNotExists(modules, "Paths");

		while (pathsModule.getProcedures().getCount() > 0)
		{
			pathsModule.getProcedures().item(var(1)).delete();
		}

		for (int i = 1; i <= robot.getPaths().getCount(); i++)
		{
			robot.getPaths().item(var(i)).syncToVirtualController(pathsModule.getName());
		}

		/*
		for(int i=1; i<=robot.getPaths().getCount(); i++)
		{
				int alreadyExists = 0;
				for(int j=1; j<=pathsModule.getProcedures().getCount(); j++)
				{
						if(pathsModule.getProcedures().item(var(j)).getName().equals(robot.getPaths().item(var(i)).getName()))
						{
								alreadyExists=1;
								break;
						}
				}
				if(alreadyExists==0)
				{
						//logger.info(pathsModule.getName());
						robot.getPaths().item(var(i)).syncToVirtualController(pathsModule.getName());
						//Thread.sleep(500);
				}
		}
		*/
		controller.shutDown();
		Thread.sleep(1500);
		logger.info("VC for " + robot.getName() + " shut down.");
	}

	/**
	 * When a RobotStudio station is opened the part MutexZone is created if not present.
	 */
	public static void configureStation(IStation istation)
	{
		try
		{
			station = istation;
			stationName = station.getName();
			robotCosts = new LinkedList[station.getMechanisms().getCount()];

			for (int i = 0; i < station.getMechanisms().getCount(); i++)
			{
				robotCosts[i] = new LinkedList();
			}

			try
			{
				station.getParts().item(var(mutexZonesName));
			}
			catch (ComJniException ex)
			{
				if (ex.ErrorCode == HResult.E_FAIL)
				{
					station.getParts().add().setName(mutexZonesName);
				}
				else
				{
					logger.error("Something is wrong! " + ex);
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Error during the configuration of the station.");

			return;
		}
	}

	/**
	 * The function creates in Robot Studio all the possible linear paths associated with the welding and home points
	 * for each robot (n welding points give n(n+1) possible paths: the order is important P1_P2 != P2_P1)
	 */
	public static void createPathsInRS()
	{
		logger.info("Creating paths between points...");

		try
		{
			for (int i = 1; i <= station.getMechanisms().getCount(); i++)
			{
				IMechanism robotMechanism = station.getMechanisms().item(var(i));
				ITargets robotTargets = robotMechanism.getWorkObjects().item(var(1)).getTargets();    // takes the targets from Elements
				int nbrOfTargets = robotTargets.getCount();

				if (nbrOfTargets > 1)
				{
					for (int j = 1; j <= nbrOfTargets; j++)
					{
						String firstName = robotTargets.item(var(j)).getName();

						firstName = firstName.substring(0, firstName.length() - 2);    // cut off ":1" at the end of each target

						for (int k = 1; k < nbrOfTargets; k++)
						{
							int index = ((j - 1 + k) % nbrOfTargets) + 1;
							String secondName = robotTargets.item(var(index)).getName();

							secondName = secondName.substring(0, secondName.length() - 2);

							IPath newPath = robotMechanism.getPaths().add();

							newPath = robotMechanism.getPaths().add(var(newPath));

							// specifying the characteristics
							newPath.setName(firstName + secondName);
							newPath.insert(robotTargets.item(var(j)));
							newPath.getTargetRefs().item(var(1)).setMotionType(1);
							newPath.insert(robotTargets.item(var(index)));
							newPath.getTargetRefs().item(var(2)).setMotionType(1);
						}
					}
				}
				else
				{
					logger.info("No welding points or no home position defined for robot " + robotMechanism.getName());
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Error occurred during paths creation.");

			return;
		}

		logger.info("Paths created successfully in RobotStudio.");
	}

	/**
	 * This function puts in the main procedure of each controller all the possible path-procedures.
	 * Then for each pair of possible paths a simulation is started and a mutexZone is built through the "spans" of the robots.
	 * With n robots and with p_i paths for robot i the total number of simulations is (see thesis)
	 * (very slow way to do it but the tick for MechanismListener does not work: moveAlongPath could be otherwise used)
	 */
	public static void createSpansInRS()
	{
		try
		{
			for (int i = 1; i <= station.getMechanisms().getCount(); i++)
			{
				LinkedList spansCreated = new LinkedList();
				IMechanism2 robot = station.getMechanisms().item(var(i));

				station.setActiveMechanism(robot);

				try
				{
					syncModulesAndPaths(robot);    // if we start when the zones are already created
				}
				catch (Exception e)
				{
					logger.error("Error during the synchronization with VC. " + e);

					return;
				}

				logger.info("Synchronization succeded.");

				IPart spansPart = station.getParts().add();

				spansPart.setName(robot.getName() + "_Spans");
				spansPart.getTransform().setX(0);
				spansPart.getTransform().setY(0);
				spansPart.getTransform().setZ(0);
				spansPart.getTransform().setRx(0);
				spansPart.getTransform().setRy(0);
				spansPart.getTransform().setRz(0);

				int nbrOfPaths = robot.getPaths().getCount();
				boolean goHome = false;

				for (int j = 1; j <= nbrOfPaths; j++)
				{
					IABBS4Controller controller = org.supremica.automata.algorithms.RobotStudioLink.startController(robot);

					logger.info("VC for " + robot.getName() + " started.");
					Thread.sleep(2500);

					IABBS4Modules modules = controller.getModules();
					IABBS4Module module = createIfNotExists(modules, "Paths");

					// After the synchronization the main should exist.
					IABBS4Procedure mainProcedure = getMainProcedure(robot);
					IABBS4Procedure pathProcedure = module.getProcedures().item(var(j));

					//IPath associatedPath = pathProcedure.getPath();
					IPath associatedPath = robot.getPaths().item(var(pathProcedure.getName()));
					ITarget startPoint = associatedPath.getTargetRefs().item(var(1)).getTarget();
					String startPointName = startPoint.getName();
					String endPointName = associatedPath.getTargetRefs().item(var(2)).getTarget().getName();

					// in this version assume the spans of two symmetric paths equal
					if (alreadySwept(spansCreated, startPointName, endPointName))
					{
						controller.shutDown();
						Thread.sleep(1500);
						logger.info("VC for " + robot.getName() + " shut down");
					}
					else
					{
						spansCreated.add(associatedPath);

						//Thread.sleep(1000);
						//logger.info(associatedPath.getName() + " and " + pathProcedure.getName());
						String firstPointString = startPointName.substring(0, startPointName.length() - 2);
						String secondPointString = endPointName.substring(0, endPointName.length() - 2);

						logger.info("Simulating movement for " + robot.getName() + " from " + firstPointString + " to " + secondPointString);

						// go to the start point for path j otherwise the simulation listens what should not listen
						robot.jumpToTarget(startPoint);

						// see if the robot really reached the target!!!!!!!!!!!!!!!!!!!!!!!
						//Thread.sleep(1000);
						//robot.moveToTargetRef(associatedPath.getTargetRefs().item(var(1)));   // stuck!!!!!
						// deleting all procedures in the main
						for (int k = 1;
								k <= mainProcedure.getProcedureCalls().getCount();
								k++)
						{
							mainProcedure.getProcedureCalls().item(var(k)).delete();
						}

						// add path procedure to the main
						mainProcedure.getProcedureCalls().add(pathProcedure);

						//Thread.sleep(2500);
						if (j == nbrOfPaths)
						{
							goHome = true;
						}

						generateSpan(controller, pathProcedure.getName(), spansPart, goHome);
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Error during the simulations. " + e);
			e.printStackTrace(System.err);

			return;
		}

		logger.info("Spans created successfully.");
	}

	public static void createMutexZonesInRS()
	{
		IPart mutex = null;

		try
		{
			mutex = station.getParts().item(var("MutexZones"));
		}
		catch (Exception e)
		{
			logger.error("MutexZones not found.");

			return;
		}

		try
		{
			mutexZonesFromSpans(mutex);
		}
		catch (Exception e)
		{
			logger.error("Error when intersecting the spans.");
		}

		logger.info("Mutex Zones created successfully.");
	}

	/**
	 * The function constructs the intersections between each pair of spans.
	 * Puts them into the part called "MutexZones" and then deletes the spans.
	 */
	private static void mutexZonesFromSpans(IPart mutex)
		throws Exception
	{

		//station.setActiveMechanism((IMechanism2) station.getMechanisms().item(var(1)));
		for (int i = 2; i <= station.getParts().getCount(); i++)
		{
			IPart thisPart = station.getParts().item(var(i));

			if (feasible(thisPart))
			{
				for (int j = 1; j < i; j++)
				{
					IPart toBeIntersected = station.getParts().item(var(j));

					if (feasible(toBeIntersected))
					{
						for (int k = 1;
								k <= thisPart.getEntities().getCount(); k++)
						{
							for (int l = 1;
									l <= toBeIntersected.getEntities().getCount();
									l++)
							{
								IEntity temp1 = thisPart.getEntities().item(var(k));
								IEntity temp2 = toBeIntersected.getEntities().item(var(l));
								boolean disjoint = false;

								try
								{
									logger.info("Intersecting " + temp1.getName() + " and " + temp2.getName());

									IEntities ents = temp1.intersect(temp2, true);

									ents.getParent().delete();
								}
								catch (Exception e)
								{
									logger.info(temp1.getName() + " and " + temp2.getName() + " disjoint");

									disjoint = true;

									//break;
								}

								if (!disjoint)
								{

									// the intersection between two objects can give more than one objects (e.g.: torus around a cube)
									IEntities intersections = temp1.intersect(temp2, true);
									IPart oldIntersectionPart = intersections.getParent();

									for (int m = 1;
											m <= intersections.getCount();
											m++)
									{
										intersections.item(var(m)).setName(temp1.getName() + temp2.getName() + "_" + m);
										mutex.setTransform(oldIntersectionPart.getTransform());
										mutex.addEntity(intersections.item(var(m)));
									}

									oldIntersectionPart.delete();
								}
							}
						}
					}
				}
			}
		}

		// deleting the spans created
		try
		{
			for (int i = 1; i <= station.getParts().getCount(); i++)
			{
				IPart thisPart = station.getParts().item(var(i));

				if (feasible(thisPart))
				{
					thisPart.delete();

					i--;
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Error during the deletion of the spans.");
		}
	}

	/**
	 * For each path try to calculate the points in which each robot intersects a mutex zone
	 * (that is an entity in the part named "MutexZones").
		 * Each new point found is added to the corresponding path.
		 */
	public static void addViaPointsInRS()
	{
		try
		{
			for (int i = 1; i <= station.getMechanisms().getCount(); i++)

			// int i=2; // try one robot per time
			{
				IMechanism robot = station.getMechanisms().item(var(i));

				station.setActiveMechanism((IMechanism2) robot);
				syncModulesAndPaths(robot);

				// Nbr of paths = nbr of procedures in "Paths"
				for (int j = 1; j <= robot.getPaths().getCount(); j++)
				{
					logger.info("Starting VC for " + robot.getName() + "...");

					IABBS4Controller controller = org.supremica.automata.algorithms.RobotStudioLink.startController(robot);

					logger.info("VC started.");

					//IABBS4Module module = controller.getModules().item(var("Paths"));   // pathsModule is not allowed as a name
					IABBS4Module module = controller.getModules().item(var("Paths"));
					IABBS4Procedure mainProcedure = getMainProcedure(robot);

					// Delete everything in the main
					for (int k = 1;
							k <= mainProcedure.getProcedureCalls().getCount();
							k++)
					{
						mainProcedure.getProcedureCalls().item(var(k)).delete();
					}

					// Add path procedure to the main
					IABBS4Procedure pathProcedure = module.getProcedures().item(var(j));

					mainProcedure.getProcedureCalls().add(pathProcedure);

					//Thread.sleep(1000);
					// Take the arrival point
					// IPath path = pathProcedure.getPath();  problems!!!!
					IPath path = robot.getPaths().item(var(pathProcedure.getName()));
					ITarget arrivalTarget = path.getTargetRefs().item(var(2)).getTarget();

					// Delete it
					path.getTargetRefs().item(var(2)).delete();

					// Take the starting point
					ITarget startTarget = path.getTargetRefs().item(var(1)).getTarget();

					// Start the simulation from the appropriate point
					robot.jumpToTarget(startTarget);
					station.getSelections().removeAll();
					station.setActiveMechanism((IMechanism2) robot);

					// Add SimulationListener
					ISimulation simulation = station.getSimulations().item(var(1));
					Simulation sim = Simulation.getSimulationFromUnknown(simulation);
					SimulationListener simulationListener = new SimulationListener();

					sim.addDSimulationEventsListener(simulationListener);

					//Add MechanismListener
					Mechanism mech = Mechanism.getMechanismFromUnknown(robot);
					MechanismListener mechanismListener = new MechanismListener(path, j, i);

					mech.add_MechanismEventsListener(mechanismListener);

					// Set the virtual controller
					station.setActiveMechanism((IMechanism2) robot);
					mechanismListener.setController(controller);

					// Start a thread running the simulation in RobotStudio
					nbrOfTimesCollision = 1;

					simulation.start();

					// Wait for the simulation to stop
					simulationListener.waitForSimulationStop();

					// Rearrange the path
					path.insert(arrivalTarget);
					path.getTargetRefs().item(var(path.getTargetRefs().getCount())).setMotionType(1);

					// Shut down the controller
					controller.shutDown();
					Thread.sleep(1500);
					logger.info("VC for " + robot.getName() + " shut down.");

					//mechanismListener.waitForControllerShutDown();   // just waits!!!
					// Stop listening!
					mech.remove_MechanismEventsListener(mechanismListener);
					sim.removeDSimulationEventsListener(simulationListener);

					// At the end of the simulations go to Home position that should be free from any span
					if ((j == robot.getPaths().getCount()) &&!arrivalTarget.getName().equals(getHomePoint(robot).getName()))
					{
						logger.info("Starting VC for " + robot.getName() + "...");

						controller = org.supremica.automata.algorithms.RobotStudioLink.startController(robot);

						logger.info("VC started.");
						robot.jumpToTarget(getHomePoint(robot));

						// Shut down the controller
						controller.shutDown();
						Thread.sleep(1500);
						logger.info("VC for " + robot.getName() + " shut down.");
					}
				}
			}

			/*print the costs obtained
			for(int i=0; i<station.getMechanisms().getCount(); i++)
			{
					for(ListIterator it = robotCosts[i].listIterator(); it.hasNext();)
							((PathWithCosts) it.next()).printCosts();
					logger.info(" ");
			}
			*/
		}
		catch (Exception e)
		{
			logger.error("Error when finding the intersection points" + e);
			e.printStackTrace(System.err);

			return;

			//throw new SupremicaException();
		}

		logger.info("Intersection points and times stored");
	}

	/**
	 * Given the selected automaton in Supremica takes each event in the automaton and associates it
	 * with a sub-path in RobotStudio. The association is made decoding the coded names of the events
	 * made in ConvertToAutomata.java
	 */
	public static void executeScheduledAutomaton(Automaton automaton)
	{
		try
		{

			// the number of robots is known
			int nbrOfRobots = station.getMechanisms().getCount();

			// the name of the current robot
			int previousRobot = 0;

			// for each robot the name of the current path
			String[] previousPaths = new String[nbrOfRobots];

			// for each current path the index of the split-subpath
			int[] nextSplitPathArray = new int[nbrOfRobots];
			IABBS4Controller controller = null;
			State state = automaton.getInitialState();

			for (ArcIterator arcIterator = state.outgoingArcsIterator();
					arcIterator.hasNext();
					arcIterator = state.outgoingArcsIterator())
			{
				LabeledEvent event = arcIterator.nextEvent();
				String eventName = event.getLabel();
				int robotIndex = takeRobotIndexFromEvent(eventName);

				logger.info("Event: " + eventName);

				if (robotIndex > 0)
				{
					IMechanism robot = station.getMechanisms().item(var(robotIndex));

					if (robotIndex != previousRobot)
					{
						if (previousRobot != 0)
						{
							controller.shutDown();
							Thread.sleep(1500);
						}

						previousRobot = robotIndex;
						controller = org.supremica.automata.algorithms.RobotStudioLink.startController(robot);
					}

					String nextPathName = takePathNameFromEvent(eventName);

					if (nextPathName.equals(previousPaths[robotIndex - 1]))
					{
						nextSplitPathArray[robotIndex - 1]++;
					}
					else
					{
						splitPath(robot.getPaths().item(var(nextPathName)), robot);

						nextSplitPathArray[robotIndex - 1] = 1;
					}

					// update the path to be executed
					previousPaths[robotIndex - 1] = nextPathName;

					// take the sub-path to be executed
					String movingPathName = nextPathName + "_" + nextSplitPathArray[robotIndex - 1];
					IPath movingPath = robot.getPaths().item(var(movingPathName));

					// execute the sub-path
					org.supremica.automata.algorithms.RobotStudioLink.moveRobotAlongPath(robot, movingPath);
					Thread.sleep(3500);    // it should wait for a response from RobotStudio:see Hugo's code RobotStudioLink
				}

				state = state.nextState(event);
			}

			controller.shutDown();
			Thread.sleep(2000);
		}
		catch (Exception e)
		{
			logger.error("Error executing scheduled automaton");

			return;
		}

		logger.info("Scheduled automaton executed");
	}

	// every path has to start with "Home" or "Wp" point
	private static void splitPath(IPath ipath, IMechanism robotMechanism)
		throws Exception
	{
		ITargetRefs targetRefs = ipath.getTargetRefs();
		int nbrTargets = ipath.getTargetRefs().getCount();

		if (nbrTargets < 2)
		{
			logger.error("Path containing less than two targets");

			return;
		}

		if (nbrTargets == 2)
		{
			ipath.setName(ipath.getName() + "_1");

			return;
		}

		/*
		// checking if the format is correct
		if ((!typeGet(targetRefs.item(var(1)).getTarget().getName()).equals("Home")
				&& !typeGet(targetRefs.item(var(1)).getTarget().getName()).equals("Wp"))
			||
			(!typeGet(targetRefs.item(var(nbrTargets)).getTarget().getName()).equals("Home")
			&& !typeGet(targetRefs.item(var(nbrTargets)).getTarget().getName()).equals("Wp")))
		{
				logger.info("The path does not start and finish with home or welding point");
				//return;
		}
		*/
		for (int i = 1; i < nbrTargets; i++)
		{
			IPath newPath = station.getPaths().add();

			newPath = robotMechanism.getPaths().add(var(newPath));

			newPath.setName(ipath.getName() + "_" + i);
			newPath.insert(targetRefs.item(var(i)).getTarget());
			newPath.getTargetRefs().item(var(1)).setMotionType(1);    //setMotionType(int pVal); pVal = 1 is LinearMotion
			newPath.insert(targetRefs.item(var(i + 1)).getTarget());
			newPath.getTargetRefs().item(var(2)).setMotionType(1);
		}
	}

	/**
	 * This function summarizes all the functions: after a station is opened the function builds the automata
	 * in Supremica.
	 */
	public static void demoCoordination()
	{
		createPathsInRS();
		createSpansInRS();
		createMutexZonesInRS();
		addViaPointsInRS();
		buildXmlFile();
		ConvertToAutomata.conversionToAutomata(file);
	}

//-------------- from robotStudioLink with modification---------------------------------------------
	private static class MechanismListener
		extends _MechanismEventsAdapter
	{
		private static Logger logger = LoggerFactory.createLogger(MechanismListener.class);
		private IABBS4Controller controller = null;
		private boolean leavingTarget = true;    // targetReached is invoked twice for every Target!
		private boolean controllerStarted = false;    // Used in the wait-method
		private IPath path = null;
		private int pathIndex = 0;
		private int robotIndex = 0;

		// Costs for the path in simulation
		private PathWithCosts pathcosts;

		// Dynamic list of objects colliding with the robot
		private LinkedList objectsColliding = new LinkedList();

		// Time for the previous event (either start or end)
		private double previousTime = 0;
		private boolean initialized = false;

		public MechanismListener(IPath path, int pathIndex, int i)
		{
			try
			{
				initialized = false;
				this.path = path;
				this.pathIndex = pathIndex;
				robotIndex = i - 1;
				pathcosts = new PathWithCosts(path.getName());

				pathcosts.insertCost(new Integer(0));
			}
			catch (Exception e)
			{
				logger.error("Error initializing mechanism");

				return;
			}

			try
			{
				IPart mutex = station.getParts().item(var(mutexZonesName));

				for (int j = 1; j <= mutex.getEntities().getCount(); j++)
				{
					IEntity entity = mutex.getEntities().item(var(j));
					String entityName = entity.getName();

					if (entityCollidesWith(station.getMechanisms().item(var(i)), entity))
					{
						objectsColliding.add(new ObjectColliding(entityName));
					}
				}

				initialized = true;

				// Print the objects already colliding
				for (ListIterator it = objectsColliding.listIterator();
						it.hasNext(); )
				{
					logger.info(((ObjectColliding) it.next()).getName());
				}
			}
			catch (Exception e)
			{
				logger.error("Error in finding the objects already colliding");

				return;
			}
		}

		// Events generated by RobotStudio.Mechanism
		public synchronized int targetReached()
		{
			try
			{
				double motionTime = controller.getMotionTime();

				if (!leavingTarget)
				{
					logger.info("Target reached at time " + (float) motionTime);

					// Set the cost
					Double realCost = new Double((motionTime - previousTime) * 1000);    // [ms]

					pathcosts.insertCost(new Integer(realCost.intValue()));
					robotCosts[robotIndex].add(pathcosts);
				}

				leavingTarget = !leavingTarget;
			}
			catch (Exception seeIfICare)
			{
				logger.error("Error with targetReached event");
			}

			return 0;
		}

		public synchronized int collisionStart(RsObject collidingObject)
		{
			try
			{

				//while (!initialized);
				// basic information
				String objectName = collidingObject.getName();
				double time = controller.getMotionTime();

				logger.info("startcollision " + time + " " + objectName);

				ObjectColliding data = listContains(objectsColliding, objectName);

				if ((time >= 0) && (data == null))
				{
					data = new ObjectColliding(objectName);

					logger.info("a1");
					objectsColliding.add(data);
					logger.info("Start of collision with " + data.getName() + " at time " + (float) time + ".");

					// set the cost for the automata
					Double realCost = new Double((time - previousTime) * 1000);

					logger.info("a2");
					pathcosts.insertCost(new Integer(realCost.intValue()));
					logger.info("a3");

					previousTime = time;

					// create a target with its own name (can be equal to a target's name for another robot)
					int indexZone = takeIndexZone(objectName);

					logger.info("a4");

					if (indexZone <= 0)
					{
						throw new SupremicaException();
					}

					logger.info("a5");

					String stringName = "In_" + indexZone + "_";

					stringName = stringName + pathIndex + nbrOfTimesCollision;

					ITarget viaTarget = createTargetAtTCP(stringName);

					// insert the new target in the right position in the path
					logger.info("a6");

					ITargetRef viaTargetRef = path.insert(viaTarget);

					logger.info("a7");
					viaTargetRef.setMotionType(1);
					logger.info("a8");

					nbrOfTimesCollision++;

					logger.info("a9");
				}

				data.setCount(data.getCount() + 1);
				logger.info("a10");

				//logger.info("counter " + data.getName() + ":  " +  data.getCount());
			}
			catch (Exception e)
			{
				logger.error("Error with event 'collisionStart'." + e);
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

				//logger.info("endcollision " + time + " " + objectName);
				ObjectColliding data = listContains(objectsColliding, objectName);

				if (data == null)
				{
					logger.error("Collision ended mysteriously.");

					return 0;
				}

				data.setCount(data.getCount() - 1);

				if (data.getCount() == 0)
				{
					logger.info("End of collision with " + data.getName() + " at time " + (float) time + ".");

					// set the cost
					Double realCost = new Double((time - previousTime) * 1000);

					pathcosts.insertCost(new Integer(realCost.intValue()));

					previousTime = time;

					// create a target with its own name (can be equal to a target's name for another robot)
					int indexZone = takeIndexZone(objectName);
					String stringName = "Out_" + indexZone + "_";

					stringName = stringName + pathIndex + nbrOfTimesCollision;

					ITarget viaTarget = createTargetAtTCP(stringName);

					// insert the new target in the right position in the path
					ITargetRef viaTargetRef = path.insert(viaTarget);

					viaTargetRef.setMotionType(1);

					nbrOfTimesCollision++;

					// remove from the objects colliding
					ObjectColliding toBeRemoved = listContains(objectsColliding, objectName);

					if (toBeRemoved != null)
					{
						objectsColliding.remove(toBeRemoved);
					}
				}

				//logger.info("counter " + data.getName() + ":  " + data.getCount());
			}
			catch (Exception whatever)
			{
				logger.error("Error with the event collisionEnd" + whatever);
			}

			return 0;
		}

		public int afterControllerStarted()
		{
			controllerStarted = true;

			logger.info("Virtual Controller started.");
			logger.fatal("Please, tell Hugo if you read this message");
			notify();

			return 0;
		}

		public int afterControllerShutdown()
		{
			controllerStarted = false;

			logger.info("Virtual Controller shut down.");
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
					logger.fatal("Please, tell Hugo if you read this message");
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

		private synchronized ITarget createTargetAtTCP(String targetName)
			throws Exception
		{

			// Create a new target
			IMechanism mechanism = controller.getMechanism();
			ITarget newTarget = mechanism.getWorkObjects().item(var(1)).getTargets().add();
			IToolFrame toolFrame = mechanism.getActiveToolFrame();

			newTarget.setTransform(toolFrame.getTransform());

			//newTarget.setName(targetName);
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

			return newTarget;
		}

		private class ObjectColliding
		{

			//Name of colliding object.
			private String name;

			// Number of started collisions.
			private int count;

			ObjectColliding(String object)
			{
				name = object;
				count = 0;
			}

			ObjectColliding(String name, int nbr)
			{
				this.name = name;
				count = nbr;
			}

			ObjectColliding(ObjectColliding a)
			{
				name = a.name;
				count = a.count;
			}

			public synchronized int getCount()
			{
				return count;
			}

			public synchronized void setCount(int nbr)
			{
				count = nbr;
			}

			public String getName()
			{
				return name;
			}
		}

		private ObjectColliding listContains(LinkedList collisions, String objectName)
		{
			try
			{
				for (ListIterator it = collisions.listIterator();
						it.hasNext(); )
				{
					ObjectColliding collide = (ObjectColliding) it.next();

					if (collide.getName().equals(objectName))
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

//-------------------------------------------------------------------------------------------------
	private static class SimulationListener
		extends DSimulationEventsAdapter
	{
		private static Logger logger = LoggerFactory.createLogger(SimulationListener.class);
		boolean simulationRunning = true;

		// Events generated by RobotStudio.ISimulation
		public synchronized void stop()
		{
			try
			{
				simulationRunning = false;

				logger.info("Simulation finished.");
				notify();
			}
			catch (Exception a)
			{
				logger.error("Error");
			}
		}

		// trying to check every endcollision if exists a real collision
		public synchronized void tick(double time)
		{

/* // This method resulted in a compiler warning,
						try
						{
								;
						}
						catch (Exception e)
						{
								logger.error("Error");
								return;
						}
*/
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
				Thread.sleep(1500);
			}
			catch (Exception ex)
			{

				//System.out.println("Interrupted! " + ex);
				logger.error("Interrupted! " + ex);
			}

			return;
		}
	}

//--------------------------------------------------------------------------------------------------------
	public static void generateSpan(IABBS4Controller controller, String procName, IPart spansPart, boolean goHome)
	{
		try
		{
			IMechanism3 robot = controller.getMechanism();

			// Start simulation listener
			ISimulation simulation = robot.getParent().getSimulations().item(var(1));
			Simulation sim = Simulation.getSimulationFromUnknown(simulation);
			SpanGenerator simulationListener = new SpanGenerator(robot, procName, spansPart);

			sim.addDSimulationEventsListener(simulationListener);

			// Start a thread running the simulation in RobotStudio
			simulation.start();

			// Wait for the simulation to stop
			simulationListener.waitForSimulationStop();
			sim.removeDSimulationEventsListener(simulationListener);

			if (goHome)
			{
				robot.jumpToTarget(getHomePoint(robot));
			}

			controller.shutDown();
			Thread.sleep(1500);
			logger.info("VC for " + robot.getName() + " shut down.");
		}
		catch (Exception e)
		{
			logger.error("Error generating span " + e);
		}
	}

	private static class SpanGenerator
		extends SimulationListener
	{
		private static IStation station;
		private IMechanism3 robot;
		private IPart spanPart;
		private IToolFrame tool0;
		private IPart spansPart;
		private String procName;
		private RsUnitsUtility ruu;

		// The size of the box surrounding the tooltip
		private static double boxSize = 0.12;    // [m]
		private static double cylinderLength = 0.85;    // [m]
		private static double cylinderRadius = 0.06;    // [m]
		private static double stepSize = boxSize * 3 / 4;    // [m]

		// The margin that should be added to the approximations
		private static double margin = 0.06;    // [m]
		private Transform oldTransform;

		public SpanGenerator(IMechanism3 robot, String procName, IPart spansPart)
			throws Exception
		{
			station = robot.getParent();
			this.robot = robot;
			this.procName = procName;
			this.spansPart = spansPart;

			if (ruu == null)
			{
				ruu = new RsUnitsUtility();
			}

			spanPart = station.getParts().add();
			tool0 = robot.getToolFrames().item(var("tool0"));
			oldTransform = transformCopy(tool0.getTransform());

			createSpanEntity(oldTransform);
		}

		// Events generated by RobotStudio.ISimulation
		public synchronized void tick(double time)
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
				logger.error("Error in SpanGenerator.tick " + ex);
			}
		}

		// in this version the union of entities is made on-line, because RobotStudio seems very sensible in
		// terms of time about the number of entities present in it.
		public synchronized void stop()
		{
			try
			{
				IEntity unionEntity = spanPart.getEntities().item(var(1));

				unionEntity.setName( /*robot.getName() +*/procName);
				unionEntity.setRelativeTransparency((float) 0.7);
				unionEntity.setTransform(ruu.uCSToWCS(unionEntity.getTransform()));
				spansPart.addEntity(unionEntity);
				spanPart.delete();
			}
			catch (Exception e)
			{
				logger.error("Error adding the span");
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
			station.setUCS(tool0);

			ITransform boxTransform = ruu.wCSToUCS(transform);

			boxTransform.setX(boxTransform.getX() - boxSize / 2 - margin);
			boxTransform.setY(boxTransform.getY() - boxSize / 2 - margin);
			boxTransform.setZ(boxTransform.getZ() - boxSize / 2 - margin);

			boxTransform = ruu.uCSToWCS(boxTransform);

			// Calculate cylinder transform
			IPart upperArm = robot.getLinks().item(var("Link4")).getParts().item(var(1));

			station.setUCS(upperArm);

			ITransform cylinderTransform = ruu.wCSToUCS(upperArm.getTransform());

			cylinderTransform.setZ(cylinderTransform.getZ() + 1.195);
			cylinderTransform.setRy(cylinderTransform.getRy() + Math.PI / 2);

			cylinderTransform = ruu.uCSToWCS(cylinderTransform);

			IPart part = station.getParts().add();

			// Create cylinder around the arm
			IEntity cyl = part.createSolidCylinder(cylinderTransform, cylinderRadius + margin, cylinderLength + 2 * margin);

			// Create box around the tooltip
			IEntity box = part.createSolidBox(boxTransform, boxSize + 2 * margin, boxSize + 2 * margin, boxSize + 2 * margin);
			IEntity boxCyl = box.join(cyl, false);
			IPart dd = boxCyl.getParent();

			if (spanPart.getEntities().getCount() > 0)
			{
				IEntity union = null;

				try
				{
					union = spanPart.getEntities().item(var(1)).join(boxCyl, false);
				}
				catch (Exception e)
				{

					//sometimes logger.error("Error joining"); I do NOT KNOW WHY ?!?!
					part.delete();
					dd.delete();

					return;
				}

				dd.delete();

				IPart pp = union.getParent();

				spanPart.delete();

				spanPart = pp;
			}
			else
			{
				IPart pp = spanPart;

				spanPart = boxCyl.getParent();

				pp.delete();
			}

			part.delete();
		}
	}

	//-------------------------UTILITIES-----------------------------------------------------------------------

	/**
	 * Function used to check if the robot collides with some object before the simulation starts.
	 */
	private static boolean entityCollidesWith(IMechanism robot, IEntity object)
		throws Exception
	{
		ICollisionSets sets = station.getCollisionSets();

		sets.setMode(1);

		ICollisionSet set = sets.add();

		set.setName("CollisionsBeforeTheStart");
		set.setActive(true);

		ICollisionObjects rob = set.getObjectsA();
		RsObject robObject = RsObject.getRsObjectFromUnknown(robot);

		rob.add(robObject);

		ICollisionObjects objects = set.getObjectsB();
		RsObject entObject = RsObject.getRsObjectFromUnknown(object);

		objects.add(entObject);
		sets.setHighlightCollisions(true);    // only to see what happens

		boolean collide = sets.checkCollisions();

		sets.setHighlightCollisions(false);
		set.delete();

		// the following statement allows the event collisionStart to happen for all the entities of the robot
		sets.setMode(2);

		//logger.info(robot.getName() + " " + object.getName() + " " + collide);
		return collide;
	}

	private static String searchPath(IMechanism robot, String p1, String p2)
		throws Exception
	{
		for (int i = 1; i <= robot.getPaths().getCount(); i++)
		{
			IPath path = robot.getPaths().item(var(i));
			String startPoint = path.getTargetRefs().item(var(1)).getTarget().getName();
			String finishPoint = path.getTargetRefs().item(var(path.getTargetRefs().getCount())).getTarget().getName();

			if (startPoint.equals(p1) && finishPoint.equals(p2))
			{
				return new String(path.getName());
			}
		}

		logger.info("Path from " + p1 + " to " + p2 + " not present in RobotStudio");

		return null;
	}

	/**
	 * Function that creates a new module in the program if it does not exist, otherwise returns
	 * the module already existing
	 */
	private static IABBS4Module createIfNotExists(IABBS4Modules modules, String newModule)
		throws Exception
	{
		for (int j = 1; j <= modules.getCount(); j++)
		{
			IABBS4Module tempModule = modules.item(var(j));

			if (tempModule.getName().equals(newModule))
			{

				//logger.info(newModule + " already existing");
				return tempModule;
			}
		}

		//IABBS4Module module = modules.add(var(newModule),1);
		modules.add(var(newModule), 1);

		// wait for the creation of the module
		while (true)
		{
			for (int j = 1; j <= modules.getCount(); j++)
			{
				if (modules.item(var(j)).getName().equals(newModule))
				{
					logger.info(newModule + " added");

					return modules.item(var(j));
				}
			}
		}
	}

	///
	//private static class PathWithCosts
	public static class PathWithCosts
	{
		String pathName;
		LinkedList costsInPath;

		PathWithCosts() {}

		///
		//PathWithCosts(String name)
		public PathWithCosts(String name)
		{
			pathName = new String(name);
			costsInPath = new LinkedList();
		}

		public synchronized void insertCost(Integer costFromSimulation)
		{
			costsInPath.add(new String(costFromSimulation.toString()));
		}

		public synchronized LinkedList getCosts()
		{
			return costsInPath;
		}

		public String getName()
		{
			return pathName;
		}

		public String toString()
		{

			//logger.info("Costs for path " + pathName);
			//for(ListIterator it = costsInPath.listIterator(); it.hasNext();)
			//      logger.info((String) it.next());
			String string = "Costs for path " + pathName;

			for (ListIterator it = costsInPath.listIterator(); it.hasNext(); )
			{
				string = string + ((String) it.next());
			}

			return string;
		}
	}

	private static boolean feasible(IPart part)
	{
		try
		{
			for (int i = 1; i <= station.getMechanisms().getCount(); i++)
			{
				if (part.getName().startsWith(station.getMechanisms().item(var(i)).getName()) && part.getName().endsWith("Spans"))
				{
					return true;
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Error in the spans parts");
		}

		return false;
	}

	private static boolean alreadySwept(LinkedList spans, String a, String b)
		throws Exception
	{
		for (ListIterator it = spans.listIterator(); it.hasNext(); )
		{
			IPath p = (IPath) it.next();
			String firstPoint = p.getTargetRefs().item(var(1)).getTarget().getName();
			String lastPoint = p.getTargetRefs().item(var(2)).getTarget().getName();

			if (firstPoint.equals(b) && lastPoint.equals(a))
			{
				return true;
			}
		}

		return false;
	}

	private static String takePathNameFromEvent(String eventName)
	{
		int first = eventName.indexOf("_");
		int last = eventName.indexOf("_", first + 1);

		if (last > 1)
		{
			return new String(eventName.substring(first + 1, last));
		}

		return new String(eventName.substring(first + 1, eventName.length()));
	}

	private static int takeRobotIndexFromEvent(String eventName)
	{
		try
		{
			int last = eventName.indexOf("_");

			if (last > 1)
			{
				for (int i = 1; i <= station.getMechanisms().getCount(); i++)
				{
					if (eventName.substring(0, last).equals(station.getMechanisms().item(var(i)).getName()))
					{
						return i;
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Error decoding event's name");
		}

		return 0;
	}

	private static ITarget getHomePoint(IMechanism robot)
		throws Exception
	{
		ITargets robotTargets = robot.getWorkObjects().item(var(1)).getTargets();

		for (int i = 1; i <= robotTargets.getCount(); i++)
		{
			if (robotTargets.item(var(i)).getName().startsWith("Home"))
			{
				return robotTargets.item(var(i));
			}
		}

		logger.info("No Home point found");

		return null;
	}

	private static int takeIndexZone(String zone)
		throws Exception
	{
		IPart mutex = null;

		for (int i = 1; i <= station.getParts().getCount(); i++)
		{
			if (station.getParts().item(var(i)).getName().equals("MutexZones"))
			{
				mutex = station.getParts().item(var(i));

				break;
			}
		}

		if (mutex == null)
		{
			logger.error("MutexZones not found");

			return 0;
		}

		for (int i = 1; i <= mutex.getEntities().getCount(); i++)
		{
			if (mutex.getEntities().item(var(i)).getName().equals(zone))
			{
				return i;
			}
		}

		logger.info(zone + " not present in MutexZones");

		return 0;
	}

	private static void save()
		throws IOException
	{
		XMLOutputter outputter = new XMLOutputter("     ", true);
		FileWriter writer = new FileWriter(file);

		outputter.output(stationDocument, writer);
		writer.close();
	}

	private static double getAxisPosition(ITargetRef point, String axis)
		throws ComJniException
	{
		if (axis.equals("X"))
		{
			return point.getTarget().getTransform().getX();
		}

		if (axis.equals("Y"))
		{
			return point.getTarget().getTransform().getY();
		}

		if (axis.equals("Z"))
		{
			return point.getTarget().getTransform().getZ();
		}

		logger.error("Axis not existing");

		return 0.07;    // just return a value
	}

	// adding the type attribute (code simulating a switch)
	private static String typeGet(String pointName)
		throws ComJniException
	{
		if (pointName.startsWith("Home"))
		{
			return homeType;
		}

		if (pointName.startsWith("In"))
		{
			return enterType;
		}

		if (pointName.startsWith("Out"))
		{
			return exitType;
		}

		return weldType;
	}

	private static String zoneOrPointGet(String nameOfPoint)
		throws Exception
	{
		if (nameOfPoint.startsWith("In") || nameOfPoint.startsWith("Out"))
		{
			int first_ = nameOfPoint.indexOf("_");
			int second_ = nameOfPoint.indexOf("_", first_ + 1);
			String zone = nameOfPoint.substring(first_ + 1, second_);
			IPart mutexPart = station.getParts().item(var(mutexZonesName));

			for (int i = 1; i <= mutexPart.getEntities().getCount(); i++)
			{
				String iString = (new Integer(i)).toString();

				if (iString.equals(zone))
				{
					return mutexPart.getEntities().item(var(i)).getName();
				}
			}

			return null;
		}

		return nameOfPoint.substring(0, nameOfPoint.length() - 2);
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

	private static Variant var(IPath i)
		throws Exception
	{
		return new Variant(i);
	}

	/**
	 * Finds and returns the main procedure of a mechanism program.
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
						logger.info("Main procedure empty");
					}

					return procedure;
				}
			}
		}

		// There is no main procedure!!
		logger.info("No main procedure found.");

		return null;
	}

//----------------------------------------------------------------------------------------------------//
	private static IEntity createBoxD(double tx, double ty, double tz, double rx, double ry, double rz, double wx, double wy, double wz, String name)
		throws Exception
	{

		// Create a new IEntity
		IPart mutexPart = station.getParts().add();

		mutexPart.setName(name);

		Transform transform = new Transform();

		transform.setX(tx);
		transform.setY(ty);
		transform.setZ(tz);
		transform.setRx(rx);
		transform.setRy(ry);
		transform.setRz(rz);

		IEntity newBox = mutexPart.createSolidBox(transform, wx, wy, wz);

		// Very blue but relatively transparent and with a catchy name
		newBox.setRelativeTransparency((float) 0.75);

		//newBox.setRelativeTransparency((float) 0.9);
		newBox.setColor(new Variant(new SafeArray(new int[]{ 0, 0, 255 }), false));
		newBox.setName(name);
		newBox.setVisible(true);

		return newBox;
	}

//--------------------------------------------------------------------------------------------------------
}

/**
creation of all the possible paths;    // at this point the user can delete paths for some his own reason
simulation of paths for each robot: building mutex zones;
simulation of paths for each robot: building points of intersection and insertion into the paths;
splitting the paths for the execution;
executing the best solution.
*/
