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


	private static IStation station;
	private static Element rootStation;

	// creation of the array of the paths to be taken for the simulations
	private static int[] arrayPaths;

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

	public static void createXmlStation(IStation istation)
		throws Exception
	{
		station = istation;
		stationName = station.getName();

		//testingRS4();     // test

		for (int i=1; i<= station.getMechanisms().getCount(); i++)
			syncModulesAndPaths(station.getMechanisms().item(var(i)));
		logger.info("aaaaa");
		simulationsToCreateZones();
		/*
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
	    	createPathsInRS();
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
		*/

	}

	/**
	 * The function creates in Robot Studio all the possible paths associated with the welding and home points
	 * for each robot (n welding points give n(n+1) possible paths: the order is important P1_P2 != P2_P1)
	 */
	private static void createPathsInRS()
		throws Exception
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
					for (int k=1; k<nbrOfTargets; k++)
					{
						int index = ((j - 1 + k) % nbrOfTargets) + 1;
						String secondName = robotTargets.item(var(index)).getName();
						IPath newPath = station.getPaths().add();
						newPath = robotMechanism.getPaths().add(var(newPath));  // creating the path

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
	 * are in Robot Studio. The function writes them to the xml and adds the right attributes.
	 */
	private static void addRobotPathsXml()
		throws Exception
	{
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
					point.setAttribute("name",nameOfPoint);
					point.setAttribute("type",typeGet(nameOfPoint));
					//point.setAttribute("cost",""""+ cost(i) + """"); trying to get the cost from a vector prebuilt
					path.getChildren().add(point);
				}
				splitPath(thisPath, robotMechanism);
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



	/**
	 * The function makes the following actions:
	 *  - creates an empty MAIN procedure (after cleaning up what was before);
	 *  - adds a new module to the mechanism.
	 * For each path of the robot:
	 *  - synchronizes (within the module created) the path to the Virtual Controller in Robot Studio.
	 */
	private static void syncModulesAndPaths(IMechanism robot)
		throws Exception
	{
		IABBS4Controller controller = org.supremica.automata.algorithms.RobotStudioLink.startController(robot);
		IABBS4Modules modules = controller.getModules();
		IABBS4Procedure mainProcedure = getMainProcedure(robot);
		if(mainProcedure==null)
		{
			IABBS4Module noModule = createIfNotExists(modules,"Program"); //BUG?why does not it work if Program is already present? delete it and then create again
			noModule.delete();
			IABBS4Module mainModule = modules.add(var("Program"),1);

			Variant mainName = var("main");
			//mainProcedure = module.getProcedures().add(mainName,true); // this way does not work!!!!
			mainProcedure = mainModule.getProcedures().add(mainName);
			logger.info("Main procedure added");
		}

		// delete all the procedure calls in the main
		for (int i=1; i<=mainProcedure.getProcedureCalls().getCount(); i++)
			mainProcedure.getProcedureCalls().item(var(i)).delete();

		// adds all the possible paths to the module if not existing
		IABBS4Module pathsModule = createIfNotExists(modules,"pathsModule");
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
				robot.getPaths().item(var(i)).syncToVirtualController(pathsModule.getName());
		}

		controller.shutDown();
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
				logger.info(newModule + " already existing");
				return modules.item(var(j));
			}
		}

		IABBS4Module module = modules.add(var(newModule),1);
		logger.info(newModule + " added");
		return module;
	}

	/**
	 * This function puts in the main procedure of each controller all the possible path-procedures.
	 * Then for each combination of possible paths a simulation is started and a mutexZone is built through the "span" of the robots.
	 * With n robots and with p_i paths for robot i the total number of simulations is the product of p_i: p_1*p_2*...*p_n.
	 * (very slow way to do it but the tick for MechanismListener does not work: moveAlongPath could be otherwise used)
	 */
	private static void simulationsToCreateZones()
	{
		try
		{
			arrayPaths = new int[station.getMechanisms().getCount()];
			simulate(1);
		}
		catch (Exception a)
		{
			logger.error("Error during the simulations");
		}

	}

	// to be finished..........and simplified where possible.............
	/**
	 * Function used in the recursion to simulate all the possible combinations
	 */
	private static void simulate(int robotIndex)
		throws Exception
	{
		int nbrOfRobots = station.getMechanisms().getCount();
		IMechanism robot = station.getMechanisms().item(var(robotIndex));
		int nbrOfPaths = robot.getPaths().getCount();

		logger.info("ok");

		for(int i=1; i<=nbrOfPaths; i++)
		{
			IABBS4Controller controller = org.supremica.automata.algorithms.RobotStudioLink.startController(robot);
			IABBS4Procedure mainProcedure = getMainProcedure(robot);
			IABBS4Module module = createIfNotExists(controller.getModules(), "pathsModule");  // at this point of the code pathsModule should always exist
			logger.info("ok1");
			arrayPaths[robotIndex-1] = i;
			// delete all the procedure calls in the main
			for (int j=1; j<=mainProcedure.getProcedureCalls().getCount(); j++)
				mainProcedure.getProcedureCalls().item(var(j)).delete();
			IABBS4Procedure pathProcedure = module.getProcedures().item(var(arrayPaths[i]));
			mainProcedure.getProcedureCalls().add(pathProcedure);
			controller.shutDown();
			if(robotIndex!=nbrOfRobots)
				simulate(robotIndex+1);
			else
				org.supremica.automata.algorithms.RobotStudioLink.NEWcreateMutexZones();
		}
	}

	private static void doSimulation()
	{
		;
	}

	/**
	 * The test shows that a user cannot add a path to a specified robot with add() and with add(var) can be added so and so
	 */
	private static void testingRS1()
		throws Exception
	{
		// I can get paths associated with a robot by robot.getPaths(); YES!!!!!!!!
		// I can add paths to a specified robot NOOOOOOOOOOOOOOO!!!!!!!!!!!!!!! BUG!!!!!!!

		IMechanism robot = station.getMechanisms().item(var(2));  // mechanism LeftRobot

		//IMechanism robot = station.getMechanisms().item(var(1));  // mechanism RightRobot

		for (int i=1; i<=robot.getWorkObjects().item(var(1)).getTargets().getCount(); i++)
			logger.info(robot.getWorkObjects().item(var(1)).getTargets().item(var(i)).getName());

		//logger.info(newPaths.getName());  // LeftRobotElements

		IPath newPath = station.getPaths().add();
		newPath.setName("ss");
		station.getMechanisms().item(var(2)).getPaths().add(var(newPath));

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


	// testing if it is possible to create a main without cheating(that is call a procedure with neame "main"): it seems impossible!!!!!!!!!
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
	 * Finds and returns the main procedure of a mechanisms program.
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
creation of all the possible paths;
simulation of paths for each robot: building mutex zones;
simulation of paths for each robot: building points of intersection and insertion into the paths;
splitting the paths for the scheduling execution;
*/