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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.DataConversionException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileWriter;
import java.util.*;

import org.supremica.automata.*;
import org.supremica.log.*;
import org.supremica.gui.*;


import org.supremica.external.comInterfaces.robotstudio_2_1.RobotStudio.*;
import org.supremica.external.comInterfaces.robotstudio_2_1.RobotStudio.enum.RsKinematicRole;
import org.supremica.external.comInterfaces.robotstudio_2_1.RobotStudio.enum.RsSimulationState;

import com.inzoom.comjni.Variant;
import com.inzoom.comjni.SafeArray;
import com.inzoom.comjni.ComJniException;
import com.inzoom.comjni.enum.HResult;

// Frame
import java.awt.*;
import java.awt.event.*;

public class CreateXml
{

	private static Logger logger = LoggerFactory.createLogger(CreateXml.class);
	/** The name of the current active station. */
	private static String stationName = null;

	private static Document stationDocument = null;
    private static File file = null;

	private static String nameOfRobot = null;

	private static String mutexZonesName = "MutexZones";
	private static String mutexZoneName = "MutexZone";
	private static String pathName = "Path";
	private static String robotName = "Robot";

	// Strings characterizing the types in the xml file
	private static String homeType = "home";
	private static String weldType = "wp";
	private static String enterType = "enterZone";
	private static String exitType = "exitZone";

	private static int nbrOfTimesCollision = 0;

	private static LinkedList costsList = new LinkedList();

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

	public static void configureStation(IStation istation)
	{
		try
		{
			station = istation;
			stationName = station.getName();
		}
		catch (Exception e)
		{
			logger.error("Error during the configuration of the station");
			return;
		}
	}

		/*
		createPathsInRS();

		for (int i=1; i<= station.getMechanisms().getCount(); i++)
		{
			try
			{
				syncModulesAndPaths(station.getMechanisms().item(var(i)));
			}
			catch (Exception e)
			{
				logger.error("Error when synchronizing the paths for robot " + i);
				return;
			}
		}


		try
		{
			simulationsToCreateZones();
		}
		catch (Exception e)
		{
			logger.error("Error during the simulations");
			return;
		}

		try
		{
			addViaToPaths();
		}
		catch(Exception e)
		{
			logger.error("Error adding via points to the path");
			e.printStackTrace();
			return;
		}

		try
		{
			file = new File(stationName);
		    for(int j=1; file.exists(); j++)
				file = new File(stationName + j);

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
			addRobotPathsXml();
		}

		catch (Exception e)
		{
			logger.error("Error while adding Mutex Zones and Robot Paths to the xml file");
			return;
		}

		try
		{
			save();
		}
		catch (Exception e)
		{
			System.out.println("Error while saving...");
			return;
		}

	}
    */
	/*
	public static void openStation(Gui x, String name)
	{
		gui = x;
		stationName = name;
		try
		{
	  		// Create an instance of RobotStudio.Application.
			if (app != null)
			{
	  	   	   	logger.info("RobotStudio already started...");
				app.setVisible(true);
				station = app.getWorkspace().openStation(stationName,var(true),var(false));
			}
			else
			{
	  	   	   	logger.info("Starting RobotStudio...");
	  	   	   	app = new Application();
				//app.addDAppEventsListener(this);
				app.setVisible(true);
	  	   	   	logger.info("RobotStudio started.");

				// Load a certain station
				station = app.getWorkspace().openStation(stationName,var(true),var(false));
			}
		}
		catch(Exception e)
		{
			logger.error("Error when initializing RobotStudioLink. " + e);
			e.printStackTrace();
	    }
	}
	*/

	/**
	 * The function creates in Robot Studio all the possible linear paths associated with the welding and home points
	 * for each robot (n welding points give n(n+1) possible paths: the order is important P1_P2 != P2_P1)
	 */
	public static void createPathsInRS()
	{
		logger.info("Creating paths among points...");
		try
		{
			for(int i=1; i<=station.getMechanisms().getCount(); i++)
			{
				IMechanism robotMechanism = station.getMechanisms().item(var(i));
				ITargets robotTargets = robotMechanism.getWorkObjects().item(var(1)).getTargets(); // takes the targets from Elements
				int nbrOfTargets = robotTargets.getCount();
				if(nbrOfTargets>1)
				{
					for (int j=1; j<=nbrOfTargets; j++)
					{
						String firstName = robotTargets.item(var(j)).getName();
						firstName = firstName.substring(0, firstName.length()-2);   // cut off ":1" at the end of each target
						for (int k=1; k<nbrOfTargets; k++)
						{
							int index = ((j - 1 + k) % nbrOfTargets) + 1;
							String secondName = robotTargets.item(var(index)).getName();
							secondName = secondName.substring(0, secondName.length()-2);

							IPath newPath = robotMechanism.getPaths().add();   // BUG: I cannot add a path to the robot i want!
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
				//logger.info("npaths" + robotMechanism.getPaths().getCount());
			}
		}
		catch (Exception e)
		{
			logger.error("Error occurred during paths creation");
			return;
		}
		logger.info("Paths created in Robot Studio");
	}

	public static void buildXmlFile()
	{
		logger.info("Building the xml file");
		try
		{
			file = new File(stationName + ".xml");
		    for(int j=1; file.exists(); j++)
				file = new File(stationName + j + ".xml");

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
			addRobotPathsXml();
		}

		catch (Exception e)
		{
			logger.error("Error while adding Mutex Zones and Robot Paths to the xml file");
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
		logger.info("Xml file built successfully");
	}

	private static void addMutexZonesXml()
		throws Exception
	{
		IPart mutexZonesPart = station.getParts().item(var(mutexZonesName));
		Element mutexZones = new Element(mutexZonesName);
	 	rootStation.getChildren().add(mutexZones);
		for (int i=1; i<=mutexZonesPart.getEntities().getCount() ;i++)
		{
			IEntity zone = mutexZonesPart.getEntities().item(var(i));
	 		Element mutexZone = new Element(mutexZoneName);
	 		mutexZones.getChildren().add(mutexZone);
	 		mutexZone.setAttribute("name",zone.getName());
		}
	}

	/**
	 * Creates all the paths in the xml file: all the paths and the intersection points
	 * are in Robot Studio. The function writes them into the xml file and adds the right attributes.
	 */
	private static void addRobotPathsXml()
		throws Exception
	{

		ListIterator pathIterator = costsList.listIterator();
		for(int i=1; i<=station.getMechanisms().getCount(); i++)
		{
			Element robotElement = new Element(robotName);
			rootStation.getChildren().add(robotElement);
			IMechanism robotMechanism = station.getMechanisms().item(var(i));
			nameOfRobot = robotMechanism.getName();
			robotElement.setAttribute("name",nameOfRobot);

			int nbrOfPaths = robotMechanism.getPaths().getCount();
			for(int j=1; j<=nbrOfPaths; j++)
			{
				LinkedList pathWithCost = (LinkedList) pathIterator.next();
				ListIterator costIterator = pathWithCost.listIterator();

				IPath thisPath = robotMechanism.getPaths().item(var(j));
				Element path = new Element(pathName);
				String nameOfPath = thisPath.getName();
				path.setAttribute("name",nameOfPath);
				robotElement.getChildren().add(path);

				// insert main points
				for(int k=1; k<=thisPath.getTargetRefs().getCount(); k++)
				{
					Element point = new Element("Point");
					String nameOfPoint = thisPath.getTargetRefs().item(var(k)).getTarget().getName();
					//logger.info(nameOfPoint);  // debug phase
					point.setAttribute("name",zoneOrPointGet(nameOfPoint));          // this is the name of the zone
					point.setAttribute("type",typeGet(nameOfPoint));

					//int supremicaCost = ((Integer) costIterator.next()).intValue();
					String xmlCost = ((Integer) costIterator.next()).toString();
					point.setAttribute("cost",xmlCost);
					path.getChildren().add(point);
				}
				//splitPath(thisPath, robotMechanism);
			}
		}
	}


	// every path has to start with "Home" or "Wp" point
	private static void splitPath(IPath ipath, IMechanism robotMechanism)
		throws ComJniException, Exception
	{
		ITargetRefs targetRefs = ipath.getTargetRefs();
		int nbrTargets = ipath.getTargetRefs().getCount();
		if (nbrTargets < 2)
		{
			logger.error("Path containing less than two targets");
			return;
		}
		if (nbrTargets == 2)
			return;


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

		for (int i=1; i<nbrTargets; i++)
		{
			IPath newPath = station.getPaths().add();
			newPath = robotMechanism.getPaths().add(var(newPath));
			newPath.setName(ipath.getName() + i);
			newPath.insert(targetRefs.item(var(i)).getTarget());
			newPath.getTargetRefs().item(var(1)).setMotionType(1);  //setMotionType(int pVal); pVal = 1 is LinearMotion
			newPath.insert(targetRefs.item(var(i+1)).getTarget());
			newPath.getTargetRefs().item(var(2)).setMotionType(1);
		}
	}

//------ the following part of code results in having a part called mutexZones with the mutex zones -------------

	/**
	 * The function makes the following actions:
	 *  - creates an empty MAIN procedure (after cleaning up what there was before);
	 *  - adds a new module to the mechanism.
	 * For each path of the robot:
	 *  - synchronizes (within the module created) the path to the Virtual Controller in Robot Studio.
	 */
	private static void syncModulesAndPaths(IMechanism robot)
		throws Exception
	{

		IABBS4Controller controller = org.supremica.automata.algorithms.RobotStudioLink.startController(robot);
		Thread.sleep(2500);
		logger.info("Controller for " + robot.getName() + " started");


		IABBS4Modules modules = controller.getModules();
		IABBS4Procedure mainProcedure = getMainProcedure(robot);
		if(mainProcedure==null)
		{
			IABBS4Module noModule = createIfNotExists(modules,"Program"); //BUG? why does not it work if Program is already present? delete it and then create again
			noModule.delete();
			IABBS4Module mainModule = modules.add(var("Program"),1);

			Variant mainName = var("main");
			//mainProcedure = module.getProcedures().add(mainName,true); // this way does not work!!!!
			mainProcedure = mainModule.getProcedures().add(mainName);
			Thread.sleep(2000);
			logger.info("Main procedure added");
		}

		// delete all the procedure calls in the main
		for (int i=1; i<=mainProcedure.getProcedureCalls().getCount(); i++)
			mainProcedure.getProcedureCalls().item(var(i)).delete();

		// adds all the possible paths to the module if not existing
		IABBS4Module pathsModule = createIfNotExists(modules,"Paths");
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


		controller.shutDown();
		Thread.sleep(2500);
		logger.info("Controller for " + robot.getName() + " shut down");

	}

	/**
	 * Function that creates a new module in the program if it does not exist, otherwise returns
	 * the module already existing
	 */
	private static IABBS4Module createIfNotExists(IABBS4Modules modules, String newModule)
		throws Exception
	{
		for (int j=1; j<=modules.getCount(); j++)
		{
			if(modules.item(var(j)).getName().equals(newModule))
			{
				//logger.info(newModule + " already existing");
				return modules.item(var(j));
			}
		}

		IABBS4Module module = modules.add(var(newModule),1);
		logger.info(newModule + " added");
		return module;
	}


	/**
	 * This function puts in the main procedure of each controller all the possible path-procedures.
	 * Then for each pair of possible paths a simulation is started and a mutexZone is built through the "spans" of the robots.
	 * With n robots and with p_i paths for robot i the total number of simulations is .......
	 * (very slow way to do it but the tick for MechanismListener does not work: moveAlongPath could be otherwise used)
	 */
	public static void createSpansInRS()
	{
		try
		{
			for(int i=1; i<=station.getMechanisms().getCount(); i++)
			{
				IMechanism2 robot = station.getMechanisms().item(var(i));
				station.setActiveMechanism(robot);

				syncModulesAndPaths(robot);
				// Trying to make faster simulations: it does not work properly
				/*
				IABBS4Controller controller = org.supremica.automata.algorithms.RobotStudioLink.startController(robot);
				// in startController we sleep
				logger.info("Controller for " + robot.getName() + " started");
				// at this point pathsModule should be always present
				IABBS4Modules modules = controller.getModules();
				IABBS4Module module = createIfNotExists(modules, "pathsModule");
				IABBS4Procedure mainProcedure = getMainProcedure(robot);
				*/



				IPart spansPart = station.getParts().add();
				spansPart.setName(robot.getName() + "_Spans");
				spansPart.getTransform().setX(0);
				spansPart.getTransform().setY(0);
				spansPart.getTransform().setZ(0);
				spansPart.getTransform().setRx(0);
				spansPart.getTransform().setRy(0);
				spansPart.getTransform().setRz(0);

				int nbrOfPaths = robot.getPaths().getCount();
				/*
				for(int j=1; j<=nbrOfPaths; j++)
				{
					logger.info(module.getProcedures().item(var(j)).getName());
					logger.info(robot.getPaths().item(var(j)).getName());
					logger.info(" ");
				}
				*/
				for(int j=1; j<=nbrOfPaths; j++)
				{


					IABBS4Controller controller = org.supremica.automata.algorithms.RobotStudioLink.startController(robot);
					// in startController we sleep
					logger.info("Controller for " + robot.getName() + " started");
					// at this point pathsModule should be always present

					IABBS4Modules modules = controller.getModules();
					IABBS4Module module = createIfNotExists(modules, "Paths");
					IABBS4Procedure mainProcedure = getMainProcedure(robot);


					//logger.info(" " + mainProcedure.getProcedureCalls().getCount());
					IABBS4Procedure pathProcedure = module.getProcedures().item(var(j));

					//IPath associatedPath = pathProcedure.getPath();
					IPath associatedPath = robot.getPaths().item(var(pathProcedure.getName()));
					ITarget startPoint = associatedPath.getTargetRefs().item(var(1)).getTarget();
					//Thread.sleep(1000);
					//logger.info(associatedPath.getName() + " and " + pathProcedure.getName());
					logger.info("from " + startPoint.getName() + " to " + associatedPath.getTargetRefs().item(var(2)).getTarget().getName());


					// go to the start point for path j otherwise the simulation listens what should not listen
					robot.jumpToTarget(startPoint);
					Thread.sleep(1000);
					//robot.moveToTargetRef(associatedPath.getTargetRefs().item(var(1)));   // stuck!!!!!

					// see if the robot really reached the target!!!!!!!!!!!!!!!!!!!!!!!

					// deleting all procedures in the main
					for (int k=1; k<=mainProcedure.getProcedureCalls().getCount(); k++)
						mainProcedure.getProcedureCalls().item(var(k)).delete();

					// add path procedure to the main
					mainProcedure.getProcedureCalls().add(pathProcedure);
					//Thread.sleep(2500);
					generateSpanD(controller,robot,pathProcedure.getName(),spansPart);
				}

				// to be commented if we stop for each path's simulation
				/*
				robot.jumpToTarget(getHomePoint(robot));
				controller.shutDown();
				Thread.sleep(2500);
				logger.info("Controller for " + robot.getName() + " shut down");
				*/
			}
		}
		catch (Exception e)
		{
			logger.error("Error during the simulations");
			return;
		}
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
			logger.error("MutexZones not found");
			return;
		}


			/*
			IPart mutex = null;
			for(int i=1; i<=station.getParts().getCount(); i++)
				if(station.getParts().item(var(i)).getName().equals("MutexZones"))
				{
					mutex = station.getParts().item(var(i));
					break;
				}
			if (mutex==null)
			{
				logger.error("MutexZones not found");
				return;
			}
			*/
			try
			{
				mutexZonesFromSpans(mutex);
			}
			catch (Exception e)
			{
				logger.error("Error when intersecting the spans");
			}
	}

	/**
	 * The function constructs the intersections between each pair of spans.
	 * Puts them into the part called "MutexZones" and then deletes the spans.
	 */
	private static void mutexZonesFromSpans(IPart mutex)
		throws Exception
	{

		//station.setActiveMechanism((IMechanism2) station.getMechanisms().item(var(1)));
		for (int i=2; i<=station.getParts().getCount(); i++)
		{
			IPart thisPart = station.getParts().item(var(i));
			if (feasible(thisPart))
			{
				for (int j=1; j<i; j++)
				{
					IPart toBeIntersected = station.getParts().item(var(j));
					if(feasible(toBeIntersected))
					{
						for (int k=1; k<=thisPart.getEntities().getCount(); k++)
						{
							for(int l=1; l<=toBeIntersected.getEntities().getCount(); l++)
							{
								IEntity temp1 = thisPart.getEntities().item(var(k));
								//logger.info(temporary.getName());
								IEntity temp2 = toBeIntersected.getEntities().item(var(l));
								//logger.info(temp.getName());
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
								}
								if (!disjoint)
								{
									// the intersection between two objects can give more than one objects (e.g.: torus around a cube)
									IEntities intersections = temp1.intersect(temp2, true);
									IPart oldIntersectionPart = intersections.getParent();
									for(int m=1; m<=intersections.getCount(); m++)
									{
										intersections.item(var(m)).setName(temp1.getName() + temp2.getName() + "_" + m);
										//RsUnitsUtility rUU = new RsUnitsUtility();
										//rUU.uCSToWCS(oldIntersectionPart.getTransform());
										//rUU.uCSToWCS(intersections.item(var(m)).getTransform());
										//rUU.uCSToWCS(intersections.item(var(m)).getTransform());
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
			for (int i=1; i<=station.getParts().getCount(); i++)
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
			logger.error("Error during the deletion of the spans");
		}

	}

	private static boolean feasible(IPart part)
	{
		try
		{
			for (int i=1; i<=station.getMechanisms().getCount(); i++)
			{
				if(part.getName().startsWith(station.getMechanisms().item(var(i)).getName()) &&
					part.getName().endsWith("Spans"))
					return true;
			}
		}
		catch (Exception e)
		{
			logger.error("Error in the spans parts");
		}
		return false;
	}

//----------------------------------------------------------------------------------------------------//

	/**
	 * The test shows that a user cannot add a path to a specified robot with add() and with add(var) can be added out of paths
	 */
	private static void testingRS1()
		throws Exception
	{
		// I can get paths associated with a robot by robot.getPaths(); YES!!!!!!!!

		//IMechanism robot = station.getMechanisms().item(var(2));  // mechanism LeftRobot

		IMechanism robot = station.getMechanisms().item(var(1));  // mechanism RightRobot

		//for (int i=1; i<=robot.getWorkObjects().item(var(1)).getTargets().getCount(); i++)
		//	logger.info(robot.getWorkObjects().item(var(1)).getTargets().item(var(i)).getName());

		//logger.info(newPaths.getName());  // LeftRobotElements

		/*
		IPaths paths = robot.getPaths();
		logger.info("nbrOfPaths " + paths.getCount());
		IPath newPath = paths.add();
		newPath.setName("ss");
		*/


		IPath newPath = robot.getPaths().add();
		newPath.setName("ss");
		robot.getPaths().add(var(newPath));

		for (int i=1; i<=robot.getPaths().getCount(); i++)
		{
			logger.info(robot.getPaths().item(var(i)).getName());
		}

		//logger.info(newPath.getParent().getName());  // RightRobotElements!!!!!!!!!!!!!!
		//logger.info("cc" + robot.getPaths().item(var(1)).getName());
		//logger.info("cc" + robot.getPaths().getCount());

	}

	/**
	 * This function shows the structure of the program browser in Robot Studio for a specified IABBS4Controller
	 */
	private static void testingRS2()
		throws Exception
	{
		IABBS4Controller controller;
		IMechanism RightRobot;
		try
		{
			RightRobot = station.getMechanisms().item(var(1)); // RightRobot
			controller = org.supremica.automata.algorithms.RobotStudioLink.startController(RightRobot);
			IABBS4Modules modules = controller.getModules();
			logger.info("Nbr of modules: " + modules.getCount());
			for (int i=1; i<=modules.getCount(); i++)
			{
				IABBS4Procedures procedures = modules.item(var(i)).getProcedures();
				logger.info(modules.item(var(i)).getName() + "   type: " + modules.item(var(i)).getType() + "  nbr of procedures: " + procedures.getCount());
				for(int j=1; j<=procedures.getCount(); j++)
				{
					IABBS4Procedure thisProcedure = procedures.item(var(j));
					logger.info("	" + thisProcedure.getName());
					if(thisProcedure.getName().equals("main"))
						for(int k=1; k<=thisProcedure.getProcedureCalls().getCount(); k++)
							logger.info("		" + thisProcedure.getProcedureCalls().item(var(k)).getName());
				}
			}

			//org.supremica.automata.algorithms.RobotStudioLink.moveRobotAlongPath(LeftRobot, path);
		}
		catch (Exception e)
		{
			logger.error("Test Error");
			return;
		}
	}

	// trying to write every path in main
	private static void testingRS3()
		throws Exception
	{
		IABBS4Controller controller;
		IMechanism rightRobot;

		try
		{
			rightRobot = station.getMechanisms().item(var(1)); // RightRobot
			controller = org.supremica.automata.algorithms.RobotStudioLink.startController(rightRobot);
			IABBS4Modules modules = controller.getModules();
			IPaths paths = rightRobot.getPaths();
			for (int i=1; i<=paths.getCount(); i++)
			{
				IPath thisPath = paths.item(var(i));
				String thisPathName = thisPath.getName();
				String newModuleName = thisPathName + "_module";

				// just to change the name
				for(int isOnly=0;isOnly==0;)
				{
					int nbrOfEqualNames = 2;
					for (int j=1; j<=modules.getCount(); j++)
					{
						if(modules.item(var(j)).getName().equals(newModuleName) && modules.item(var(j)).getType()==1)
						{
							newModuleName = thisPathName + "_module" + nbrOfEqualNames;
							j=1;
							nbrOfEqualNames++;
						}
						else
							if(j==modules.getCount())
								isOnly=1;
					}
				}


				IABBS4Module module = modules.add(var(newModuleName),1);
				thisPath.syncToVirtualController(module.getName());    // creates directly a path-procedure in the module
				IABBS4Procedure pathProcedure = module.getProcedures().item(var(i));

				IABBS4Procedure mainProcedure = getMainProcedure(rightRobot);
				// deleting the main to have empty main
				// adding a new procedure to the main
				mainProcedure.getProcedureCalls().add(pathProcedure);
			}
		}
		catch (Exception e)
		{
			logger.error("error adding procedures");
			return;
		}
	}


	// testing if it is possible to create a main without cheating (that is without calling a procedure with name "main")
	// It seems impossible!!!!!!!!!
	private static void testingRS4()
		throws Exception
	{
		IMechanism robot = station.getMechanisms().item(var(1));
		IABBS4Controller controller = org.supremica.automata.algorithms.RobotStudioLink.startController(robot);
		if(getMainProcedure(robot) == null)
		{
			int alreadyExists = 0;
			IABBS4Modules modules = controller.getModules();
			IABBS4Module module = modules.item(var(1));
			for (int j=1; j<=modules.getCount(); j++)
			{
				if(modules.item(var(j)).getName().equals("Programmmm") && modules.item(var(j)).getType()==1)
				{
					alreadyExists=1;
					module = modules.item(var(j));
					logger.info("Module already exisisting");
				}
			}
			if(alreadyExists==0)
				module = controller.getModules().add(var("Programmmm"),1);
			logger.info("fine1");
			Variant mainName = var("main");
			//IABBS4Procedure procc = module.getProcedures().add(name,true); // this way does not work!!!!
			IABBS4Procedure procc = module.getProcedures().add(mainName);
		}
	}


	// practicing with intersections, parts and entities in Robot Studio-
	// If the objects are disjoint RS throws an Exception instead of returning a null value!!!
	private static void testingRS5()
			throws Exception
	{
		double dim = 1.5;
		IEntity thisBox1 = createBoxD(2,2,2,2,2,2,2,2,2,"box1");
		//IEntity thisBox2 = createBoxD(1,1,1,1,1,1,1,1,1,"box2");
		IEntity thisBox2 = createBoxD(dim,dim,dim,dim,dim,dim,dim,dim,dim,"box2");
		int disj = 1;
		try
		{
			IEntity trial = thisBox1.join(thisBox2,true);
			//trial.delete();
			trial.getParent().delete();
		}
		catch (Exception e)
		{
			logger.info("Objects disjoint");
			disj = 0;
		}
		if(disj==1)
		{
			IEntity gg = thisBox1.join(thisBox2,true);
			IPart p = gg.getParent();
			p.setName("Union");
			IEntities inters = thisBox1.intersect(thisBox2, true);
			inters.getParent().setName("Intersections");
		}
	}


//--------------------------------------------------------------------------------------------------------
// For each path try to calculate the points in which each robot intersects a mutex zone (that is an entity
// in the part named "MutexZones").
// Each new point found is added to the corresponding path.

	public static void addViaPointsInRS()
	{
		try
		{
			for(int i=1; i<=station.getMechanisms().getCount(); i++)
			{
				IMechanism robot = station.getMechanisms().item(var(i));
				station.setActiveMechanism((IMechanism2) robot);

				// nbr of paths = nbr of procedures in "Paths"
				for (int j=1; j<=robot.getPaths().getCount(); j++)
				{
					logger.info("Starting the Virtual Controller...");
					IABBS4Controller controller = org.supremica.automata.algorithms.RobotStudioLink.startController(robot);
					logger.info("Controller for " + robot.getName() + " started");


					//IABBS4Module module = controller.getModules().item(var("Paths"));   // pathsModule is not allowed as a name
					IABBS4Module module = controller.getModules().item(var("Paths"));
					//logger.info(module.getName());
					IABBS4Procedure mainProcedure = getMainProcedure(robot);
					//logger.info(mainProcedure.getName());

					// Delete everything in the main
					for (int k=1; k<=mainProcedure.getProcedureCalls().getCount(); k++)
						mainProcedure.getProcedureCalls().item(var(k)).delete();

					// Add path procedure to the main
					IABBS4Procedure pathProcedure = module.getProcedures().item(var(j));
					//logger.info(pathProcedure.getName());

					mainProcedure.getProcedureCalls().add(pathProcedure);

					IPath path = robot.getPaths().item(var(pathProcedure.getName()));

					//IPath path = pathProcedure.getPath();  problems!!!!
					//logger.info(path.getName());

					// Take the arrival point
					ITargetRefs arrivalTargets = path.getTargetRefs();
					ITargetRef arrivalTargetRef = arrivalTargets.item(var(2));
					ITarget arrivalTarget = arrivalTargetRef.getTarget();
					String arrivalTargetName = arrivalTarget.getName();
					//logger.info(arrivalTargetName);
					// delete it
					path.getTargetRefs().item(var(2)).delete();

					// Take the starting point
					ITarget startTarget = path.getTargetRefs().item(var(1)).getTarget();

					// Start the simulation from the appropriate point
					robot.jumpToTarget(startTarget);

					station.getSelections().removeAll();

					// Add MechanismListener
					Mechanism mech = Mechanism.getMechanismFromUnknown(robot);
					MechanismListener mechanismListener = new MechanismListener(path,j);
					mech.add_MechanismEventsListener(mechanismListener);

					// Set the virtual controller
					station.setActiveMechanism((IMechanism2) robot);
					mechanismListener.setController(controller);

					// Add SimulationListener
					ISimulation simulation = station.getSimulations().item(var(1));
					Simulation sim = Simulation.getSimulationFromUnknown(simulation);
					SimulationListener simulationListener = new SimulationListener();
					sim.addDSimulationEventsListener(simulationListener);

					// Start a thread running the simulation in RobotStudio
					nbrOfTimesCollision = 1;
					simulation.start();

					// Wait for the simulation to stop
					simulationListener.waitForSimulationStop();

					// Rearrange the path
					path.insert(arrivalTarget);
					path.getTargetRefs().item(var(path.getTargetRefs().getCount())).setMotionType(1);

					// At the end of the simulations go to Home position that should be free from every span
					if (j==robot.getPaths().getCount())
						robot.jumpToTarget(getHomePoint(robot));

					// Shut down the controller
					logger.info("Shutting down the Virtual Controller...");
					controller.shutDown();
					mechanismListener.waitForControllerShutDown();

					// Stop listening!
					mech.remove_MechanismEventsListener(mechanismListener);
					sim.removeDSimulationEventsListener(simulationListener);
				}
			}

			/*
			// print the costs obtained
			for(ListIterator it = costsList.listIterator();it.hasNext();)
			{

				for(ListIterator iter = ((LinkedList) it.next()).listIterator(); iter.hasNext();)
				{
					Integer cost = (Integer) iter.next();
					logger.info("Cost = " + cost.intValue());
				}
				logger.info(" ");
			}
			*/
		}
		catch (Exception e)
		{
		}

	}

//-------------- from robot studio link---------------------------------------------

	/*
	private static void runSimulation(IPath path, IABBS4Controller controller)
			throws Exception
		{
			// Get the active mechanism
			IMechanism mechanism = station.getActiveMechanism();
			ISimulation simulation = station.getSimulations().item(var(1));
			Simulation sim = Simulation.getSimulationFromUnknown(simulation);
			SimulationListener simulationListener = new SimulationListener();
			sim.addDSimulationEventsListener(simulationListener);

			// Start a thread running the simulation in RobotStudio
			simulation.start();

			// Wait for the simulation to stop
			simulationListener.waitForSimulationStop();

			// Shut down the controller
			logger.info("Shutting down the Virtual Controller...");
			controller.shutDown();
			// Stop listening!
			sim.removeDSimulationEventsListener(simulationListener);
	}
	*/

	private static class MechanismListener extends _MechanismEventsAdapter
		{
			private static Logger logger = LoggerFactory.createLogger(MechanismListener.class);

			private IABBS4Controller controller = null;
			private LinkedList targetTimes = new LinkedList(); // Doubles of targetReached-times
			private LinkedList collisions = new LinkedList();  // CollisionDatas of collisionStart/End-times
			private boolean leavingTarget = true;       // targetReached is invoked twice for every Target!
			private boolean controllerStarted = false;  // Used in the wait-methods

			private IPath path = null;
			private int pathIndex = 0;

			private LinkedList costs = new LinkedList();
			private double previousTime = 0;
			private Double realCost;

			public MechanismListener(IPath path, int pathIndex)
			{
				this.path = path;
				this.pathIndex = pathIndex;
				costs.add(new Integer(0));
			}
			// Events generated by RobotStudio.Mechanism
			public int targetReached()
			{
				try
				{
					double motionTime = controller.getMotionTime();
					targetTimes.add(new Double(motionTime));
					if (!leavingTarget)
					{
						logger.info("Target reached at time " + (float) motionTime);

						// set the cost
						Double realCost = new Double((motionTime - previousTime)*1000);   // [ms]
						costs.add(new Integer(realCost.intValue()));

						// a target is the end of a path (in this version of the code)
						costsList.add(new LinkedList(costs));
					}
					leavingTarget = !leavingTarget;
				}
				catch(Exception seeIfICare)
				{
				}
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
				logger.fatal("Mechtick: " + systemTime + ", please tell Hugo if you get this message.");
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

					// Is this the start?
					if (data.getCount() == 0)
					{
						data.setStartTime(controller.getMotionTime());
						logger.info("Start of collision with " + objectName + " at time " + (float) data.startTime);
						//createTargetAtTCP("In" /*+ path.getName()*/ + objectName + nbrOfTimesCollision);

						// set the cost
						realCost = new Double((data.startTime - previousTime)*1000);
						costs.add(new Integer(realCost.intValue()));
						previousTime = data.startTime;

						int indexZone = takeIndexZone(objectName);
						String stringName = "In_" + indexZone /*+ "_Path" + pathIndex*/ + "_" + nbrOfTimesCollision;
						ITarget viaTarget = createTargetAtTCP(stringName);
						ITargetRef viaTargetRef = path.insert(viaTarget);
						viaTargetRef.setMotionType(1);
						nbrOfTimesCollision++;
					}
					data.setCount(data.getCount()+1);
				}
				catch (Exception whatever)
				{
				}
				return 0;
			}
			public synchronized int collisionEnd(org.supremica.external.comInterfaces.robotstudio_2_1.RobotStudio.RsObject collidingObject)
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

					// Is this the end?
					data.setCount(data.getCount()-1);
					if (data.getCount() == 0)
					{
						data.setEndTime(controller.getMotionTime());
						logger.info("End of collision with " + objectName + " at time " + (float) data.endTime);

						// set the cost
						realCost = new Double((data.endTime - previousTime)*1000);
						costs.add(new Integer(realCost.intValue()));
						previousTime = data.endTime;

						int indexZone = takeIndexZone(objectName);
						String stringName = "Out_" + indexZone + /*+ "_Path" + pathIndex*/ "_" + nbrOfTimesCollision;
						ITarget viaTarget = createTargetAtTCP(stringName);
						ITargetRef viaTargetRef = path.insert(viaTarget);
						viaTargetRef.setMotionType(1);
						nbrOfTimesCollision++;
					}
				}
				catch (Exception whatever)
				{
				}
				return 0;
			}
			/*public int beforeControllerStarted()
			{
				logger.info("BeforeControllerStarted.");
				return 0;
			}*/
			public int afterControllerStarted()
			{
				controllerStarted = true;
				logger.info("Virtual Controller started.");
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
					Thread.sleep(3000);
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
					return;
				}
			}

			/**
		 	 * Creates a new target at the current position. The target will be
		 	 * named targetName:x if targetName is not too long (16 characters?).
			 */
			private ITarget createTargetAtTCP(String targetName)
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
						if (nbr>=10)
							ok = true;
					}
				}

				return newTarget;
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

	private static class SimulationListener extends DSimulationEventsAdapter
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

	/*
	private static void makeIntersections()
		throws Exception
	{
		IMechanism robot = station.getMechanisms().item(var(1));

		// Clear all selections
		station.getSelections().removeAll();

		// Run simulation and gather transition times
		// Add MechanismListener
		Mechanism mech = Mechanism.getMechanismFromUnknown(robot);
		org.supremica.automata.algorithms.RobotStudioLink.MechanismListener mechanismListener = new org.supremica.automata.algorithms.RobotStudioLink.MechanismListener();
		mech.add_MechanismEventsListener(mechanismListener);

		// Start virtual controller
		station.setActiveMechanism((IMechanism2) robot);
		logger.info("Starting the Virtual Controller...");
		IABBS4Controller controller = robot.startABBS4Controller(true);
		mechanismListener.setController(controller);

		// Add SimulationListener
		ISimulation simulation = station.getSimulations().item(var(1));
		Simulation sim = Simulation.getSimulationFromUnknown(simulation);
		SimulationListener simulationListener = new SimulationListener();
		sim.addDSimulationEventsListener(simulationListener);

		// Start a thread running the simulation in RobotStudio
		nbrOfTimesCollision = 1;
		simulation.start();

		// Wait for the simulation to stop
		simulationListener.waitForSimulationStop();

		// Generate automaton and add automaton to gui
		double[] targetTimes = mechanismListener.getTargetTimes();

		// Shut down the controller
		logger.info("Shutting down the Virtual Controller...");
		controller.shutDown();
		mechanismListener.waitForControllerShutDown();

		// Stop listening!
		mech.remove_MechanismEventsListener(mechanismListener);
		sim.removeDSimulationEventsListener(simulationListener);

	}
	*/
//--------------------------------------------------------------------------------------------------------

	public static void generateSpanD(IABBS4Controller controller, IMechanism robot,String procName,IPart spansPart)
			throws Exception
	{
			// Start simulation listener
			ISimulation simulation = robot.getParent().getSimulations().item(var(1));
			Simulation sim = Simulation.getSimulationFromUnknown(simulation);
			SpanGeneratorD simulationListener = new SpanGeneratorD(robot,procName,spansPart);
			sim.addDSimulationEventsListener(simulationListener);
			// Start a thread running the simulation in RobotStudio
			simulation.start();
			// Wait for the simulation to stop
			simulationListener.waitForSimulationStop();
			sim.removeDSimulationEventsListener(simulationListener);

			// cut off to have faster simulations
			controller.shutDown();
			Thread.sleep(2500);
			logger.info("Controller shut down");

	}

	// from Hugo's code with some modification
	private static class SpanGeneratorD extends SimulationListener
	{
			private static IStation station;
			private IMechanism robot;
			private IPart spanPart;
			private IToolFrame tool0;

			private IPart spansPart;
			private String procName;

			private RsUnitsUtility ruu;

			// The size of the box surrounding the tooltip
			private static double boxSize = 0.12; // [m]
			private static double cylinderLength = 0.85; // [m]
			private static double cylinderRadius = 0.06; // [m]
			private static double stepSize = boxSize*3/4; // [m]
			// The margin that should be added to the approximations
			private static double margin = 0.05; // [m]

			private Transform oldTransform;

			public SpanGeneratorD(IMechanism robot, String procName, IPart spansPart)
				throws Exception
			{
				station = robot.getParent();;
				this.robot = robot;
				this.procName = procName;
				this.spansPart = spansPart;
				if (ruu == null)
					ruu = new RsUnitsUtility();
				spanPart = station.getParts().add();
				spanPart.setVisible(true);
				spanPart.setName(robot.getName() + "_Span");
				tool0 = robot.getToolFrames().item(var("tool0"));
				//robot.setActiveToolFrame(tool0);
				oldTransform = transformCopy(tool0.getTransform());
				logger.info("Simulation for path " + procName + " started");
				// sometimes it keeps the past span so I skip the first entity created. Ok also this does not work!!!
				createSpanEntity(oldTransform);    //to be fixed when the controller is always on during simulations
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
					if (Math.sqrt(dx*dx+dy*dy+dz*dz) > stepSize)
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
					{	// At least two spanEntities
						unionEntity = spanEntities.item(var(2));
						IPart oldPart = null;
						for (int i=spanEntities.getCount(); i>1; i--)
						{
							//IPart part = unionEntity.getParent();
							//logger.info("Joining " + unionEntity.getName() + " and " + spanEntities.item(var(1)).getName());


							boolean disjoint = false;
							try
							{
								//Thread.sleep(2000);
								IEntity temp = unionEntity.join(spanEntities.item(var(1)), true);
								temp.getParent().delete();
							}
							catch (Exception e)
							{
								/*
								logger.error(e.getStackTrace());
								logger.error(spanEntities.item(var(1)).getName());
								logger.error(unionEntity.getName());
								logger.error("Joining failed! " + e);
								*/
								logger.info(unionEntity.getName() + spanEntities.item(var(1)).getName() + " are disjoint");
								// this means that the spans are disjoint: it can happen for a cylinder and a box
								// I don't want it to happen because the span should be accurate (almost continous)
								disjoint = true;
							}

							if(!disjoint)
								unionEntity = unionEntity.join(spanEntities.item(var(1)), false);

							//spanPart.addEntity(unionEntity);
							if (oldPart != null && !disjoint)
								oldPart.delete();
							oldPart = unionEntity.getParent();
						}
					}
					else
					{	// Only one spanEntity
						unionEntity = spanEntities.item(var(1));
					}
					spanPart.delete();
					unionEntity.setName("Span" + procName);
					unionEntity.getParent().setName(robot.getName() + procName);
					unionEntity.setRelativeTransparency((float) 0.7);

					//if(spansPart.getName().equals("LeftRobot_Spans"))
					//	station.setUCS(tool0);
					unionEntity.setTransform(ruu.uCSToWCS(unionEntity.getTransform()));
					station.getSelections().removeAll();
					IPart toBeDeleted = unionEntity.getParent();
					//toBeDeleted.setTransform(ruu.uCSToWCS(toBeDeleted.getTransform()));
					//ruu.uCSToWCS(unionEntity.getTransform());
					//spansPart.setTransform(unionEntity.getParent().getTransform());

					/*
					spansPart.setTransform(toBeDeleted.getTransform());
					*/

					spansPart.addEntity(unionEntity);
					toBeDeleted.delete();
				}
				catch (Exception ex)
				{
					logger.error("Span uniting failed! " + ex);
					return;
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
				boxTransform.setX(boxTransform.getX()-boxSize/2-margin);
				boxTransform.setY(boxTransform.getY()-boxSize/2-margin);
				boxTransform.setZ(boxTransform.getZ()-boxSize-margin);
				boxTransform = ruu.uCSToWCS(boxTransform);

				// Calculate cylinder transform
				IPart upperArm = robot.getLinks().item(var("Link4")).getParts().item(var(1));
				station.setUCS(upperArm);
				ITransform cylinderTransform = ruu.wCSToUCS(upperArm.getTransform());
				cylinderTransform.setZ(cylinderTransform.getZ()+1.195/*-margin*/);
				cylinderTransform.setRy(cylinderTransform.getRy()+Math.PI/2/*-margin*/);
				cylinderTransform = ruu.uCSToWCS(cylinderTransform);

				// Create cylinder around the arm
				spanPart.createSolidCylinder(cylinderTransform,cylinderRadius+margin,cylinderLength+2*margin);

				// Create box around the tooltip
				spanPart.createSolidBox(boxTransform,boxSize+2*margin,boxSize+2*margin,boxSize+2*margin);
			}
	}

//--------------------------------------------------------------------------------------------------------

	// Utilities
	private static ITarget getHomePoint(IMechanism robot)
		throws Exception
	{
		ITargets robotTargets = robot.getWorkObjects().item(var(1)).getTargets();
		for(int i=1; i<=robotTargets.getCount(); i++)
			if(robotTargets.item(var(i)).getName().startsWith("Home"))
				return robotTargets.item(var(i));

		logger.info("No Home point found");
		return null;
	}

	private static int takeIndexZone(String zone)
		throws Exception
	{
		IPart mutex = null;
		for(int i=1; i<=station.getParts().getCount(); i++)
			if(station.getParts().item(var(i)).getName().equals("MutexZones"))
			{
				mutex = station.getParts().item(var(i));
				break;
			}
		if (mutex==null)
		{
			logger.error("MutexZones not found");
			return 0;
		}

		for(int i=1; i<=mutex.getEntities().getCount(); i++)
			if(mutex.getEntities().item(var(i)).getName().equals(zone))
				return i;
		logger.info(zone + " not present in MutexZones");
		return 0;
	}


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
			IEntity newBox = mutexPart.createSolidBox(transform,wx,wy,wz);

			// Very blue but relatively transparent and with a catchy name
			newBox.setRelativeTransparency((float) 0.75);
			//newBox.setRelativeTransparency((float) 0.9);
			newBox.setColor(new Variant(new SafeArray(new int[]{0,0,255}),false));
			newBox.setName(name);
			newBox.setVisible(true);
			return newBox;
	}

	private static void save()
		throws IOException
	{
		XMLOutputter outputter = new XMLOutputter();
	    FileWriter writer = new FileWriter(file);
	    outputter.output(stationDocument,writer);
	    writer.close();
	}

	private static double getAxisPosition(ITargetRef point, String axis)
		throws ComJniException
	{
		if (axis.equals("X"))
			return point.getTarget().getTransform().getX();
		if (axis.equals("Y"))
			return point.getTarget().getTransform().getY();
		if (axis.equals("Z"))
			return point.getTarget().getTransform().getZ();
		logger.error("Axis not existing");
		return 0.07;                // just return a value
	}

	// adding the type attribute (code simulating a switch)
	private static String typeGet(String pointName)
		throws ComJniException
	{
		if (pointName.startsWith("Home"))
			return homeType;
		if (pointName.startsWith("In"))
			return enterType;
		if (pointName.startsWith("Out"))
			return exitType;

		return "Wp";
	}

	private static String zoneOrPointGet(String nameOfPoint)
		throws Exception
	{
		if (nameOfPoint.startsWith("In") || nameOfPoint.startsWith("Out"))
		{
			int first_ = nameOfPoint.indexOf("_");
			int second_ = nameOfPoint.indexOf("_",first_ + 1);
			String zone = nameOfPoint.substring(first_ + 1,second_);
			IPart mutexPart = station.getParts().item(var(mutexZonesName));
			for(int i=1; i<=mutexPart.getEntities().getCount(); i++)
			{
				String iString = (new Integer(i)).toString();
				if(iString.equals(zone))
					return mutexPart.getEntities().item(var(i)).getName();
			}
			return null;
		}
		return nameOfPoint.substring(0,nameOfPoint.length()-2);
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
		for (int i=1;i<=modules.getCount();i++)
		{
			IABBS4Procedures procedures = modules.item(var(i)).getProcedures();
			for (int j=1;j<=procedures.getCount();j++)
			{
				IABBS4Procedure procedure = procedures.item(var(j));
				if (procedure.getName().equals("main"))
				{

					if (procedure.getProcedureCalls().getCount() == 0)
						logger.info("Main procedure empty");

					return procedure;
				}
			}
		}
		// There is no main procedure!! The program isn't properly loaded?
		logger.info("No main procedure found.");
		return null;
	}
}


/**
creation of all the possible paths;    // at this point the user can delete paths for some his own reason
simulation of paths for each robot: building mutex zones;
simulation of paths for each robot: building points of intersection and insertion into the paths;
splitting the paths for the execution;
executing the best solution.
*/