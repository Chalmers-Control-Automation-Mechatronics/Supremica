package org.supremica.external.robotCoordination;

import org.supremica.external.comInterfaces.robotstudio_3_0.RobotStudio.*;
import org.supremica.external.comInterfaces.robotstudio_3_0.RobotStudio.enum.RsKinematicRole;
import com.inzoom.comjni.Variant;
import com.inzoom.comjni.SafeArray;
import com.inzoom.comjni.ComJniException;
import com.inzoom.comjni.enum.HResult;
import org.supremica.log.*;
import org.supremica.automata.*;
import java.util.*;
import org.supremica.external.robotCoordinationABB.*;

/**
 * Class collecting the implementations of the robotCoordination-interfaces
 * for RobotStudio.
 */
public class RobotStudioInterface
{
	private static Logger logger = LoggerFactory.createLogger(RobotStudioInterface.class);

	// Initialize jacoZoom
	static
	{
		com.inzoom.comjni.Dll.runRoyaltyFree(643622874);
	}

	// Constants

	/** The name of the IPart containing the mutex zones. */
	private final static String ZONEPART_NAME = "MutexZones";
	private final static String ZONEENTITY_BASENAME = "MutexZone";
	private final static String FREESTATE_NAME = "Free";
	private final static String BOOKEDSTATE_NAME = "Booked";

	/** The suffix of the Part containing the spans. */
	private final static String SPANS_SUFFIX = "_Spans";
	private final static String SPAN_SUFFIX = "_Span";

	/** The name of the module containing the paths */
	private final static String PATHSMODULE_NAME = "Paths";
	private static Variant RS_WHITE;
	private static Variant RS_RED;
	private static Variant RS_GREEN;
	private static Variant RS_BLUE;

	/** The RobotStudio application. */
	private static Application app = null;

	// Domenico stuff
	private static int nbrOfTimesCollision = 0;
	private static LinkedList[] robotCosts;

	// Implementation of the RobotCell interface for use against RobotStudio.
	public static class RSRobotCell
		implements RobotCell, DAppEvents
	{

		/** The active RopbotStudio station. */
		private static Station station = null;
		private Part zones;

		public RSRobotCell(String stationName)
		{
			try
			{
				init();
				openStation(stationName);
				station.setBackgroundColor(RS_WHITE);
				station.setFloorVisible(false);

				// Array of LinkedLists, later containing PathWithCost objects
				robotCosts = new LinkedList[getMechanismsWithRole(RsKinematicRole.rsKinematicRoleRobot).size()];

				for (int i = 0;
						i < getMechanismsWithRole(RsKinematicRole.rsKinematicRoleRobot).size();
						i++)
				{
					robotCosts[i] = new LinkedList();
				}

				try
				{    // Already got a zones part?
					zones = Part.getPartFromUnknown(station.getParts().item(var(ZONEPART_NAME)));
				}
				catch (Exception ex)
				{    // No such part?
					zones = Part.getPartFromUnknown(station.getParts().add());
				}

				zones.setName(ZONEPART_NAME);
			}
			catch (Exception e)
			{
				logger.error("Error when initializing RobotStudio interface. " + e);
				e.printStackTrace();
			}
		}

		public void init()
			throws Exception
		{

			// Create an instance of RobotStudio.Application.
			if (app != null)
			{
				logger.info("RobotStudio already started.");
				app.setVisible(true);    // It's nice to see what is happening
			}
			else
			{
				logger.info("Starting RobotStudio...");

				app = new Application();

				app.addDAppEventsListener(this);
				app.setVisible(true);    // It's nice to see what is happening
				logger.info("RobotStudio started.");
			}

			// Some declarations
			RS_WHITE = new Variant(new SafeArray(new int[]{ 255, 255, 255 }), false);
			RS_RED = new Variant(new SafeArray(new int[]{ 255, 0, 0 }), false);
			RS_GREEN = new Variant(new SafeArray(new int[]{ 0, 255, 0 }), false);
			RS_BLUE = new Variant(new SafeArray(new int[]{ 0, 0, 255 }), false);
		}

		// RobotCell interface methods
		public void openStation(String stationName)
			throws Exception
		{
			IStation iStation = app.getWorkspace().openStation(stationName, var(true), var(false));

			station = Station.getStationFromUnknown(iStation);
		}

		public LinkedList getRobots()
			throws Exception
		{
			LinkedList list = getMechanismsWithRole(RsKinematicRole.rsKinematicRoleRobot);

			// Transform into a list of Robot objects instead
			for (Iterator mechanismIt = list.iterator();
					mechanismIt.hasNext(); )
			{
				IMechanism mech = (IMechanism) mechanismIt.next();
				Mechanism mechanism = Mechanism.getMechanismFromUnknown(mech);
				Robot robot = new RSRobot(mechanism, station);

				list.set(list.indexOf(mech), robot);
			}

			return list;
		}

		public Automata generateZoneAutomata()
			throws Exception
		{
			Automata result = new Automata();

			// Create new automata
			for (int i = 1; i <= zones.getEntities().getCount(); i++)
			{

				// Create new automaton
				String zoneName = zones.getEntities().item(var(i)).getName();
				Automaton aut = new Automaton(zoneName);

				aut.setType(AutomatonType.Specification);

				// Add two states, Free and Booked
				State state = aut.createAndAddUniqueState(null);

				state.setName(FREESTATE_NAME);
				state.setAccepting(true);
				aut.setInitialState(state);

				state = aut.createAndAddUniqueState(null);

				state.setName(BOOKEDSTATE_NAME);

				// Add automaton
				result.addAutomaton(new Automaton(zoneName));
			}

			/*
			// Find the robot paths that determine booking and unbooking
			LinkedList list = getRobots();
			for (Iterator listIt = list.iterator(); listIt.hasNext(); )
			{
					Mechanism mech = ((RSRobot) listIt.next()).getRobotStudioMechanism();
					IPaths paths = mech.getPaths();

					// Yada, yada...
			}
			*/
			return result;
		}

		public Automata generateRobotAutomata()
			throws Exception
		{
			Automata result = new Automata();
			LinkedList list = getRobots();

			for (Iterator listIt = list.iterator(); listIt.hasNext(); )
			{

				// Create new automaton
				Robot robot = (Robot) listIt.next();
				Automaton aut = new Automaton(robot.getName());

				aut.setType(AutomatonType.Plant);

				// Add automaton
				result.addAutomaton(aut);
			}

			return result;
		}

		/**
		 * Finds the index of a robot.
		 */
		public int getRobotIndex(String robotName)
			throws Exception
		{
			LinkedList robots = getMechanismsWithRole(RsKinematicRole.rsKinematicRoleRobot);

			for (int i = 0; i < robots.size(); i++)
			{
				if (((IMechanism) robots.get(i)).getName().equals(robotName))
				{
					return i;
				}
			}

			logger.info(robotName + " not present in cell?");

			return 0;
		}

		/**
		 * Finds the index of a zone named zone.
		 */
		private int getZoneIndex(String zoneName)
			throws Exception
		{
			for (int i = 1; i <= zones.getEntities().getCount(); i++)
			{
				if (zones.getEntities().item(var(i)).getName().equals(zoneName))
				{
					return i;
				}
			}

			logger.debug(zoneName + " not present in Part " + ZONEPART_NAME + "?");

			return 0;
		}

		/**
		 * For each path try to calculate the points in which each robot intersects a mutex zone
		 * (that is an entity in the part named "MutexZones").
		 * Each new point found is added to the corresponding path.
		 */
		public void examineCollisions(Robot robot, Position from, Position to)
		{
			try
			{
				robot.jumpToPosition(from);

				Mechanism mechanism = ((RSRobot) robot).getRobotStudioMechanism();

				station.setActiveMechanism(mechanism);

				// Find targets
				Target fromTarget = ((RSPosition) from).getRobotStudioTarget();
				Target toTarget = ((RSPosition) to).getRobotStudioTarget();

				// Create new path for this motion
				Path path = Path.getPathFromUnknown(mechanism.getPaths().add());

				path.setName(from.getName() + to.getName());
				path.insert(fromTarget);
				path.getTargetRefs().item(var(1)).setMotionType(1);    // Linear motion
				path.insert(toTarget);
				path.getTargetRefs().item(var(2)).setMotionType(1);    // Linear motion

				// Redefine robot program...
				IABBS4Procedure mainProcedure = ((RSRobot) robot).getMainProcedure();

				for (int k = 1;
						k <= mainProcedure.getProcedureCalls().getCount();
						k++)
				{
					mainProcedure.getProcedureCalls().item(var(k)).delete();
				}

				// Add path as only procedure in main
				path.syncToVirtualController(PATHSMODULE_NAME);    // Generate procedure from path
				Thread.sleep(1000);    // The synchronization takes a little while...

				IABBS4Procedure proc = path.getProcedure();

				mainProcedure.getProcedureCalls().add(path.getProcedure());

				// Add SimulationListener
				ISimulation simulation = station.getSimulations().item(var(1));
				Simulation sim = Simulation.getSimulationFromUnknown(simulation);
				SimulationListener simulationListener = new SimulationListener();

				sim.addDSimulationEventsListener(simulationListener);

				//Add MechanismListener
				//MechanismListener mechanismListener = new MechanismListener(path,j,i);
				MechanismListener mechanismListener = new MechanismListener(mechanism, path);

				mechanism.add_MechanismEventsListener(mechanismListener);
				mechanismListener.setController(mechanism.getController());

				// Start a thread running the simulation in RobotStudio
				nbrOfTimesCollision = 1;

				simulation.start();

				// Wait for the simulation to stop
				simulationListener.waitForSimulationStop();
				sim.removeDSimulationEventsListener(simulationListener);
				mechanism.remove_MechanismEventsListener(mechanismListener);
				Thread.sleep(1000);

				// Rearrange the path so that the to-Target is last, after viapoints
				// that may have been added during the simulation!
				path.getTargetRefs().item(var(2)).delete();
				path.insert(toTarget);
				path.getTargetRefs().item(var(path.getTargetRefs().getCount())).setMotionType(1);

				// Clean up
				//path.delete();
			}
			catch (Exception e)
			{
				logger.error("Error when finding the intersection points" + e);
				e.printStackTrace(System.err);

				return;

				//throw new Exception();
			}

			logger.debug("Intersection points and times stored");
		}

		/**
		 * Returns a list of all mechanisms with a certain role in the active station.
		 */
		private LinkedList getMechanismsWithRole(int kinematicRole)
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
		 * Calculates the intersections between each pair of spans.
		 * Puts them into the part called "MutexZones" and then deletes the spans.
		 */
		public void intersectSpans(Robot robotA, Robot robotB)
			throws Exception
		{

			// Get the parts containing the spans
			IPart spansA = station.getParts().item(var(robotA.getName() + SPANS_SUFFIX));
			IPart spansB = station.getParts().item(var(robotB.getName() + SPANS_SUFFIX));

			// Iterate over all spans in robotA and robotB and intersect...
			for (int a = 1; a <= spansA.getEntities().getCount(); a++)
			{
				for (int b = 1; b <= spansB.getEntities().getCount(); b++)
				{
					IEntity spanA = spansA.getEntities().item(var(a));
					IEntity spanB = spansB.getEntities().item(var(b));

					try
					{

						// Note that the intersection between two objects can give
						// more than one object!
						IEntities intersections = spanA.intersect(spanB, true);
						IPart parent = intersections.getParent();

						for (int m = 1; m <= intersections.getCount(); m++)
						{
							intersections.item(var(m)).setName(spanA.getName() + spanB.getName() + "_" + m);
							zones.setTransform(parent.getTransform());
							zones.addEntity(intersections.item(var(m)));
						}

						parent.delete();
					}
					catch (Exception e)
					{

						// Either the spans were disjoint or there was an error. Whatever.
						//logger.info(spanA.getName() + " and " + spanB.getName() + " are disjoint.");
					}
				}
			}
		}

		// DAppEvents interface methods
		public void selectionChanged()
		{

			//logger.info("Selection changed.");
		}

		public void quit()
		{

			// This is fatal, cause this has never ever happened
			// and I'd like to know if it ever does! (It should, Magnus said!)
			logger.fatal("RobotStudio is shutting down. Please tell Hugo " + "if, when and how you got this message!");

			// clean();
		}

		public int stationBeforeOpen(String Path, boolean[] Cancel)
		{

			//logger.info("Station being opened...");
			return 0;
		}

		public int stationAfterOpen(org.supremica.external.comInterfaces.robotstudio_3_0.RobotStudio.Station Station)
		{
			logger.info("Station opened.");

			return 0;
		}

		public int stationBeforeSave(org.supremica.external.comInterfaces.robotstudio_3_0.RobotStudio.Station Station, boolean[] Cancel)
		{

			//logger.info("Station being saved...");
			return 0;
		}

		public int stationAfterSave(org.supremica.external.comInterfaces.robotstudio_3_0.RobotStudio.Station Station)
		{
			logger.info("Station saved...");

			return 0;
		}

		public int libraryBeforeOpen(String FileName, boolean[] Cancel)
		{

			//logger.info("Library being opened...");
			return 0;
		}

		public int libraryAfterOpen(org.supremica.external.comInterfaces.robotstudio_3_0.RobotStudio.RsObject RsObject)
		{

			//logger.info("Library opened.");
			return 0;
		}

		public int libraryBeforeSave(org.supremica.external.comInterfaces.robotstudio_3_0.RobotStudio.RsObject RsObject, boolean[] Cancel)
		{

			//logger.info("Library being saved...");
			return 0;
		}

		public int libraryAfterSave(org.supremica.external.comInterfaces.robotstudio_3_0.RobotStudio.RsObject RsObject)
		{

			//logger.info("Library saved.");
			return 0;
		}

		public int started()
		{
			logger.info("RobotStudio started.");

			return 0;
		}

		private class MechanismListener
			extends _MechanismEventsAdapter
		{
			private IABBS4Controller controller = null;
			private boolean leavingTarget = true;    // targetReached is invoked twice for every Target!
			private boolean controllerStarted = false;    // Used in the wait-method
			private Path path;
			private Mechanism mechanism;

			// Costs for the path in simulation
			private CreateXml.PathWithCosts pathcosts;

			// Dynamic list of objects colliding with the robot
			private LinkedList objectsColliding = new LinkedList();

			// Time for the previous event (either start or end)
			private double previousTime = 0;

			public MechanismListener(Mechanism mechanism, Path path)
			{
				try
				{
					this.path = path;
					this.mechanism = mechanism;
					pathcosts = new CreateXml.PathWithCosts(path.getName());

					pathcosts.insertCost(new Integer(0));
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
					for (int j = 1; j <= zones.getEntities().getCount(); j++)
					{
						IEntity zone = zones.getEntities().item(var(j));
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
						logger.debug(mechanism.getName() + " is already inside the zone " + ((Collider) it.next()).getName() + " at target " + path.getTargetRefs().item(var(1)).getName() + ".");
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
			 * before the simulation starts.
			 */
			private boolean entityCollidesWith(IMechanism robot, IEntity object)
				throws Exception
			{

				// Get the collision sets, containing two sets for entities
				ICollisionSets sets = station.getCollisionSets();

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

			// Events generated by RobotStudio.Mechanism
			public synchronized int targetReached()
			{
				try
				{
					double motionTime = controller.getMotionTime();

					if (!leavingTarget)
					{
						logger.debug("Target reached at time " + (float) motionTime + ".");

						// Set the cost
						Double realCost = new Double((motionTime - previousTime) * 1000);    // [ms]

						pathcosts.insertCost(new Integer(realCost.intValue()));
						robotCosts[getRobotIndex(mechanism.getName())].add(pathcosts);
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

					/* Nä... så är det ju inte, de slutar på "_X" också... där X är int
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
					int indexZone = getZoneIndex(objectName);

					if (indexZone <= 0)
					{
						logger.warn("It appears that " + mechanism.getName() + " has collided with '" + objectName + "'.");

						return 0;

						//throw new Exception("Collision with object in station detected!");
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
						Double realCost = new Double((time - previousTime) * 1000);

						pathcosts.insertCost(new Integer(realCost.intValue()));

						previousTime = time;

						// Create a target here!
						String stringName = "In" + indexZone + "_";

						//stringName = stringName + path.getName() + nbrOfTimesCollision;
						stringName = stringName + nbrOfTimesCollision;

						ITarget viaTarget = createTargetAtTCP(stringName);

						// Insert the new target in the path
						ITargetRef viaTargetRef = path.insert(viaTarget);

						viaTargetRef.setMotionType(1);

						nbrOfTimesCollision++;
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
					int indexZone = getZoneIndex(objectName);

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
						Double realCost = new Double((time - previousTime) * 1000);

						pathcosts.insertCost(new Integer(realCost.intValue()));

						previousTime = time;

						// Create a target here!
						String stringName = "Out" + indexZone + "_";

						//stringName = stringName + path.getName() + nbrOfTimesCollision;
						stringName = stringName + nbrOfTimesCollision;

						ITarget viaTarget = createTargetAtTCP(stringName);

						// Insert the new target in the path
						ITargetRef viaTargetRef = path.insert(viaTarget);

						viaTargetRef.setMotionType(1);

						nbrOfTimesCollision++;

						// Remove from the colliding objects list
						Collider toBeRemoved = getColliderWithName(objectsColliding, objectName);

						if (toBeRemoved != null)
						{
							objectsColliding.remove(toBeRemoved);
						}
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

				logger.debug("Virtual Controller shut down.");
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
						logger.fatal("Please, tell Hugo if you read this message!!");
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
				ITarget newTarget = mechanism.getWorkObjects().item(var(1)).getTargets().add();
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
			 * Object for keeping track of the collisions with an object
			 */
			private class Collider
			{

				//Name of colliding object.
				private String name;

				// Number of started collisions.
				private int count = 0;

				Collider(String name)
				{
					this.name = name;
				}

				Collider(String name, int nbr)
				{
					this(name);

					count = nbr;
				}

				Collider(Collider a)
				{
					this(a.getName(), a.getCount());
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
	}

	// Implementation of the Robot interface for use against RobotStudio.
	private static class RSRobot
		implements Robot
	{

		// Internal variables
		private Mechanism mechanism;
		private Station station;
		private Part spans;

		public RSRobot(Mechanism mechanism, Station station)
		{
			this.mechanism = mechanism;
			this.station = station;

			try
			{
				spans = addPart(mechanism.getName() + SPANS_SUFFIX);
			}
			catch (Exception ex)
			{
				logger.error("Error in RSRobot.java." + ex);
			}
		}

		// Robot interface methods
		public LinkedList getPositions()
			throws Exception
		{
			LinkedList list = new LinkedList();

			// Get targets from RobotStudio, tranform into list of Target:s.
			ITargets robotTargets = mechanism.getWorkObjects().item(var(1)).getTargets();    // takes the targets from Elements
			int nbrOfTargets = robotTargets.getCount();

			for (int i = 1; i <= nbrOfTargets; i++)
			{
				list.add(new RSPosition(robotTargets.item(var(i))));
			}

			return list;
		}

		public Position getHomePosition()
			throws Exception
		{
			ITargets robotTargets = mechanism.getWorkObjects().item(var(1)).getTargets();    // takes the targets from Elements

			return new RSPosition(robotTargets.item(var(1)));
		}

		public void generateSpan(Position from, Position to)
			throws Exception
		{
			jumpToPosition(from);

			// Find targets
			Target fromTarget = ((RSPosition) from).getRobotStudioTarget();
			Target toTarget = ((RSPosition) to).getRobotStudioTarget();

			// Create new path for this motion
			IPath path = mechanism.getPaths().add();

			path.setName(from.getName() + to.getName());
			path.insert(fromTarget);
			path.getTargetRefs().item(var(1)).setMotionType(1);    // Linear motion
			path.insert(toTarget);
			path.getTargetRefs().item(var(2)).setMotionType(1);    // Linear motion

			// Redefine robot program...
			IABBS4Procedure mainProcedure = getMainProcedure();

			for (int k = 1; k <= mainProcedure.getProcedureCalls().getCount();
					k++)
			{
				mainProcedure.getProcedureCalls().item(var(k)).delete();
			}

			// Add path as only procedure in main
			path.syncToVirtualController(PATHSMODULE_NAME);    // Generate procedure from path
			Thread.sleep(1000);    // The synchronization takes a little while...

			IABBS4Procedure proc = path.getProcedure();

			mainProcedure.getProcedureCalls().add(path.getProcedure());

			// Generate span for this path
			// Start simulation listener
			ISimulation simulation = mechanism.getParent().getSimulations().item(var(1));
			Simulation sim = Simulation.getSimulationFromUnknown(simulation);
			SpanGenerator spanGenerator = new SpanGenerator(path.getName());

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

		public void jumpToPosition(Position position)
			throws Exception
		{

			// Find targets
			Target goal = ((RSPosition) position).getRobotStudioTarget();

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

			// We don't have to wait here! In RS2.0 it was necessary, but in RS3.0,
			// the above call won't return until the controller has started!
			// YES IT WILL! Better wait at least a second...
			Thread.sleep(1000);

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
		 * Adds part to activeStation and returns it. If there already was
		 * a part with the same name, it is returned instead.
		 */
		private Part addPart(String name)
		{
			Part part = null;

			try
			{
				try
				{

					// If there already is one, get it!
					part = Part.getPartFromUnknown(station.getParts().item(var(name)));
				}
				catch (ComJniException ex)
				{

					// If there was none, create it!
					if (ex.ErrorCode == HResult.E_FAIL)
					{    // No such item, construct one!
						part = Part.getPartFromUnknown(station.getParts().add());

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
		 * Finds and returns the main procedure of a mechanism program.
		 */
		int tries = 0;

		protected IABBS4Procedure getMainProcedure()
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
			logger.info("No main procedure found! Trying again...");

			if (tries++ == 10)
			{
				return null;
			}

			// Wait a sec and try again...
			Thread.sleep(500);

			return getMainProcedure();
		}

		/**
		 * Keeps track of the controller status, is it started or shut down?
		 * The _MechanismEventsAdapter lacks information about the current mechanism
		 * and its controller but this information is supplied in this class...
		 */
		private class ControllerListener
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
				try
				{
					logger.debug("Starting Virtual Controller for " + getName() + "...");
				}
				catch (Exception whatever) {}

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

				try
				{
					logger.debug("Virtual Controller shut down for " + getName() + ".");
				}
				catch (Exception whatever) {}

				notify();

				return 0;
			}
		}

		/**
		 * A listener that listens throughout a simulation and generates spans.
		 */
		private class SpanGenerator
			extends SimulationListener
		{
			private IToolFrame tool0;
			private String pathName;
			private RsUnitsUtility ruu;

			// The size of the box surrounding the tooltip
			private static final double BOXSIZE = 0.12;    // [m]
			private static final double CYLINDERLENGTH = 1.1;    // [m]
			private static final double CYLINDERRADIUS = 0.06;    // [m]
			private static final double STEPSIZE = BOXSIZE * 3 / 5;    // [m]

			// The MARGIN that should be added to all sides
			private static final double MARGIN = 0.06;    // [m]
			private Transform oldTransform;
			private Part temp;

			/**
			 * The collection of spanapproximations for the current path
			 */
			private IEntities spanEntities;

			public SpanGenerator(String pathName)
				throws Exception
			{
				this.pathName = pathName;

				if (ruu == null)
				{
					ruu = new RsUnitsUtility();
				}

				temp = addPart("Temp");
				spanEntities = temp.getEntities();

				// Create first cover
				tool0 = mechanism.getToolFrames().item(var("tool0"));
				oldTransform = transformCopy(tool0.getTransform());

				//createSpanEntity(oldTransform);
			}

			// Events generated by RobotStudio.Simulation
			public void tick(double time)
			{

				// In each tick, examine if it is time to generate a new spanEntity
				try
				{
					ITransform newTransform = tool0.getTransform();
					double dx = oldTransform.getX() - newTransform.getX();
					double dy = oldTransform.getY() - newTransform.getY();
					double dz = oldTransform.getZ() - newTransform.getZ();

					if (Math.sqrt(dx * dx + dy * dy + dz * dz) > STEPSIZE)
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

			// Called when the simulation stops
			public void stop()
			{

				// Generate the union of the spanEntities
				try
				{

					// Create a final cover
					// createSpanEntity(tool0.getTransform());
					// IEntities spanEntities = spans.getEntities();
					IEntity unionEntity;

					if (spanEntities.getCount() >= 2)
					{    // At least two spanEntities
						unionEntity = spanEntities.item(var(2));

						IPart oldPart = null;

						for (int i = spanEntities.getCount(); i > 1; i--)
						{
							IPart part = unionEntity.getParent();

							//logger.info("Joining " + unionEntity.getName() + " and " + spanEntities.item(var(1)).getName());
							try
							{
								unionEntity = unionEntity.join(spanEntities.item(var(1)), false);
							}
							catch (Exception ex)
							{
								logger.warn("Disjoint entities in span?");

								// logger.info("Problem when joining entities. Disjoint? " + ex);
								continue;
							}

							//spans.addEntity(unionEntity);
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

					//spans.delete();
					unionEntity.setName(pathName + SPAN_SUFFIX);
					unionEntity.setColor(RS_BLUE);
					unionEntity.setRelativeTransparency((float) 0.9);

					IPart parent = unionEntity.getParent();

					//spans.setTransform(parent.getTransform());
					//unionEntity.setTransform(spans.getTransform());
					unionEntity.setTransform(ruu.uCSToWCS(unionEntity.getTransform()));
					spans.addEntity(unionEntity);
					parent.delete();

					//unionEntity.getParent().setName(mechanism.getName() + SPAN_SUFFIX);
					// If there are elements left in temp, make them red!
					temp.setColor(RS_RED);
					temp.setRelativeTransparency((float) 0.9);
					station.getSelections().removeAll();
				}
				catch (Exception ex)
				{
					logger.error("Span uniting failed! " + ex);
				}

				super.stop();
			}

			private Transform transformCopy(ITransform transform)
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

				boxTransform.setX(boxTransform.getX() - BOXSIZE / 2 - MARGIN);
				boxTransform.setY(boxTransform.getY() - BOXSIZE / 2 - MARGIN);
				boxTransform.setZ(boxTransform.getZ() - BOXSIZE - MARGIN);

				boxTransform = ruu.uCSToWCS(boxTransform);

				// Calculate cylinder transform
				IPart upperArm = mechanism.getLinks().item(var("Link4")).getParts().item(var(1));

				station.setUCS(upperArm);

				ITransform cylinderTransform = ruu.wCSToUCS(upperArm.getTransform());

				cylinderTransform.setZ(cylinderTransform.getZ() + 1.195);
				cylinderTransform.setX(cylinderTransform.getX() - 0.25 - MARGIN);
				cylinderTransform.setRy(cylinderTransform.getRy() + Math.PI / 2);

				cylinderTransform = ruu.uCSToWCS(cylinderTransform);

				// Create cylinder around the upper arm
				temp.createSolidCylinder(cylinderTransform, CYLINDERRADIUS + MARGIN, CYLINDERLENGTH + 2 * MARGIN);

				// Create box around the tooltip
				temp.createSolidBox(boxTransform, BOXSIZE + 2 * MARGIN, BOXSIZE + 2 * MARGIN, BOXSIZE + 2 * MARGIN);
			}
		}
	}

	/**
	 * Implementation of the Position-interface for RobotStudio.
	 */
	private static class RSPosition
		implements Position
	{
		Target target;

		public RSPosition(ITarget target)
		{
			try
			{
				this.target = Target.getTargetFromUnknown(target);
			}
			catch (Exception ex)
			{

				// Was there a problem?
				System.err.println("Error in constructor RSPosition." + ex);
			}
		}

		// Target interface methods
		public String getName()
		{
			try
			{

				// Return the name (remove the last two characters ":1" since they are ugly)
				return target.getName().substring(0, target.getName().length() - 2);
			}
			catch (Exception ex)
			{
				System.err.println("Robot has no name? " + ex);
			}

			return "";
		}

		// Other method
		public String toString()
		{
			return "'" + getName() + "'";
		}

		/**
		 * Returns the RobotStudio target.
		 */
		public Target getRobotStudioTarget()
		{
			return target;
		}
	}

	/**
	 * A listener for determining when a simulation is finished.
	 */
	private static class SimulationListener
		extends DSimulationEventsAdapter
	{

		// Boolean for keeping track on when a simulation is running... not 100%!
		boolean simulationRunning = true;

		// Events generated by RobotStudio.ISimulation
		public synchronized void stop()
		{
			simulationRunning = false;

			try
			{
				logger.debug("Simulation finished.");
			}
			catch (Exception whatever) {}

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
	 * Typecast i into Variant, for convenience! (Variant is something like
	 * VB:s variant of the java Object.)
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
}
