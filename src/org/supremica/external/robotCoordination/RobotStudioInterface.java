package org.supremica.external.robotCoordination;

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
    private final static String SPAN_SUFFIX = "_Span";
    private final static String SPANS_SUFFIX = SPAN_SUFFIX + "s";

    /** The name of the module containing the paths */
    private final static String PATHSMODULE_NAME = "Paths";

    /** Automata constants */
    private final static String STARTPOSITION_NAME = "START";
    private final static String FINISHPOSITION_NAME = "FINISH";

    /** Automata constants */
    private final static String STARTSTATE_NAME = "start";
    private final static String FINISHSTATE_NAME = "finish";
    private final static String FINISHEVENT_NAME = "fin";
    private final static String UNDERSCORE = "_";

    /** Via point suffix */
    private final static String VIAPOINT_SUFFIX = "vp";

    /** Colours */
    private static Variant RS_WHITE;
    private static Variant RS_RED;
    private static Variant RS_GREEN;
    private static Variant RS_BLUE;

    /** The RobotStudio application. */
    private static Application app = null;

    // Domenico stuff
    private static int nbrOfTimesCollision = 1;
    private static LinkedList[] robotCosts;

    // Implementation of the RobotCell interface for use against RobotStudio.
    public static class RSRobotCell
		implements RobotCell, DAppEvents
    {
		/** The active RobotStudio station. */
		private static Station station = null;
		private Part zones;

		/** Generated automata */
		private Automata robotAutomata = new Automata();
		private Automata zoneAutomata = new Automata();

		// Running number of added zones
		private int zoneNbr = 1;

		public RSRobotCell(File file)
		{
			try
			{
				init();
				openStation(file);
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
				{
					// Already got a zones part?
					zones = Part.getPartFromUnknown(station.getParts().item(var(ZONEPART_NAME)));
				}
				catch (Exception ex)
				{
					// No such part?
					zones = Part.getPartFromUnknown(station.getParts().add());
					zones.setName(ZONEPART_NAME);
				}
			}
			catch (Exception e)
			{
				logger.error("Error when initializing RobotStudio interface. " + e);
				e.printStackTrace();
			}
		}

		/**
		 * Initializes RobotStudio
		 */
		public void init()
			throws Exception
		{
			// Create an instance of RobotStudio.Application.
			if (applicationIsRunning())
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

		/**
		 * Returns true if an application is already running, false otherwise.
		 * if quit() worked as it should, we could simply check if (app == null).
		 *
		 * @see #quit()
		 */
		private boolean applicationIsRunning()
		{
			// Try if it is possible to get any info from app, if not, either app
			// is null or not running!
			try
			{
				// Try a simple method, if there is no exception,
				// there is!
				app.getVisible();
				return true;
			}
			catch (Exception ex)
			{
				// Something is wrong!
				return false;
			}
		}

		/////////////////////////////////
		// RobotCell INTERFACE METHODS //
		/////////////////////////////////

		/**
		 * Opens station in the RobotStudio environment
		 */
			public void openStation(File file)
				throws Exception
		{
			String stationName = file.getAbsolutePath();
			IStation iStation = app.getWorkspace().openStation(stationName, var(true), var(false));
			station = Station.getStationFromUnknown(iStation);

			// Build robot automata
			LinkedList robots = getRobots();
			robotAutomata = buildRobotAutomata(robots);
		}

		/**
		 * Examine if there is an open station.
		 */
		public boolean isOpen()
		{
			try
			{
				// See if there is an active station, if there is no exception,
				// there is!
				app.getActiveStation();
				return true;
			}
			catch (Exception ex)
			{
				// Something is wrong!
				return false;
			}
		}

		/**
		 * Returns a linked list of Robot objects.
		 */
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
			/*
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
			  return result;
			*/
			return zoneAutomata;
		}

		/**
		 * Generates the structure of the robot automata models. NOTE: Not the complete models!
		 */
		public Automata buildRobotAutomata(LinkedList robots)
			throws Exception
		{
			Automata robotAut = new Automata();

			// Iterate over the robots...
			for (Iterator robotIt = robots.iterator(); robotIt.hasNext();)
			{
				Robot robot = (Robot) robotIt.next();

				// ONE AUTOMATON FOR THE ROBOT ITSELF //
				Automaton aut = new Automaton(robot.getName());
				aut.setType(AutomatonType.Plant);
				// Build the states...
				State initial = new State(STARTSTATE_NAME);
				initial.setInitial(true);
				aut.addState(initial);
				initial.setCost(0);
				State marked = new State(FINISHSTATE_NAME);
				marked.setAccepting(true);
				aut.addState(marked);
				marked.setCost(0);
				LinkedList posList = robot.getPositions();
				for (int i=0; i < posList.size(); i++)
				{
					for (int j=0; j < posList.size(); j++)
					{
						if (i != j)
						{
							State state = new State(((Position) posList.get(i)).getName() +
													((Position) posList.get(j)).getName());
							aut.addState(state);
							state.setCost(0);
						}
					}
				}
				// Build transitions...
				for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext();)
				{
					State fromState = stateIt.nextState();
					// Initial?
					if (fromState.getName().equals(STARTSTATE_NAME))
					{
						// Skip the 0:th element here... its assumed to be the home state
						for (int i=1; i < posList.size(); i++)
						{
							// Create new arc...
							String name = ((Position) posList.get(0)).getName() +
								((Position) posList.get(i)).getName();
							State toState = aut.getStateWithName(name);
							LabeledEvent event = new LabeledEvent(name);
							if (!aut.getAlphabet().contains(event))
							{
								aut.getAlphabet().addEvent(event);
							}

							Arc arc = new Arc(fromState, toState, event);
							aut.addArc(arc);
						}
					}
					else if (fromState.getName().equals(FINISHSTATE_NAME))
					{
						// No outgoing from final state...
					}
					else
					{
						String fromPos = null;
						// Skip the 0:th element here... its assumed to be the home state
						for (int i=0; i < posList.size(); i++)
						{
							if (fromState.getName().endsWith(((Position) posList.get(i)).getName()))
							{
								// Just to make sure there is no ambiguity
								if (fromPos != null)
								{
									throw new Exception("Error in RobotStudioInterface.java, ambigous position names");
								}
								fromPos = ((Position) posList.get(i)).getName();

								// Create arc for each possible target position
								for (int j=0; j < posList.size(); j++)
								{
									if (i != j)
									{
										// Create new arc...
										String name = ((Position) posList.get(i)).getName() +
											((Position) posList.get(j)).getName();
										State toState = aut.getStateWithName(name);

										// Special treatment if were dealing with the home position
										if (i==0)
										{
											name = FINISHEVENT_NAME;
											toState = aut.getStateWithName(FINISHSTATE_NAME);
										}

										// Create event
										LabeledEvent event = new LabeledEvent(name);
										if (!aut.getAlphabet().contains(event))
										{
											aut.getAlphabet().addEvent(event);
										}

										// Add arc
										Arc arc = new Arc(fromState, toState, event);
										aut.addArc(arc);

										// Only once if this was the home position (ugly hack... whatever)
										if (i==0)
										{
											break;
										}
									}
								}
							}
						}
					}
				}
				aut.setComment("This automaton is not ready generated!");
				// Add automaton
				robotAut.addAutomaton(aut);

				// ONE AUTOMATON PER TARGET //
				LinkedList positions = robot.getPositions();
				// Skip home position (i=1...)
				for (int i=1; i<positions.size(); i++)
				{
					Position pos = (Position) positions.get(i);

					aut = new Automaton(robot.getName() + UNDERSCORE + pos.getName());
					State notVisited = new State("0");
					notVisited.setInitial(true);
					State visited = new State("1");
					visited.setAccepting(true);
					aut.addState(notVisited);
					aut.addState(visited);
					aut.setType(AutomatonType.Plant);

					/*
					// Add transitions
					for (int j=0; j<positions.size(); j++)
					{
						if (i==j)
						{
							continue;
						}

						// Create event
						String name = ((Position) positions.get(j)).getName() + pos.getName();
						LabeledEvent event = new LabeledEvent(name);
						if (!aut.getAlphabet().contains(event)) // Is always true?
						{
							aut.getAlphabet().addEvent(event);
						}

						// Add arc
						Arc arc = new Arc(notVisited, visited, event);
						aut.addArc(arc);
					}
					*/

					aut.setComment("This automaton is not ready generated!");
					robotAut.addAutomaton(aut);
				}

				/*
				// ONE AUTOMATON FOR THE SEQUENCE LENGTH //
				aut = new Automaton(robot.getName() + "_seq");
				aut.setType(AutomatonType.Plant);
				aut.setComment("This automaton is not ready generated!");
				robotAut.addAutomaton(aut);
				*/
			}

			return robotAut;
		}

		public Automata generateRobotAutomata()
			throws Exception
		{
			/*
			  Automata result = new Automata();

			  // Iterate over robots
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
			*/
			return robotAutomata;
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

			logger.error(robotName + " not present in cell?");

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
		 * (that is an entity in the part named ZONEPART_NAME).
		 * Each new point found is added to the corresponding path.
		 */
		public void examineCollisions(Robot robot, Position from, Position to)
		{
			try
			{
				// Init
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

				// Add SimulationListener (for detecting when simulation is finished)
				ISimulation simulation = station.getSimulations().item(var(1));
				Simulation sim = Simulation.getSimulationFromUnknown(simulation);
				SimulationListener simulationListener = new SimulationListener();
				sim.addDSimulationEventsListener(simulationListener);

				//Add MechanismListener (for generating targets and detecting collisions)
				//MechanismListener mechanismListener = new MechanismListener(path,j,i);
				MechanismListener mechanismListener = new MechanismListener(mechanism, path);
				mechanism.add_MechanismEventsListener(mechanismListener);
				mechanismListener.setController(mechanism.getController());

				// Start a thread running the simulation in RobotStudio
				//nbrOfTimesCollision = 1;
				simulation.start();

				// Wait for the simulation to stop
				simulationListener.waitForSimulationStop();
				sim.removeDSimulationEventsListener(simulationListener);

				// Get the result, a list of collision times + info!
				LinkedList richPath = mechanismListener.getRichPath();

				// Stop the mechanismlistener
				mechanism.remove_MechanismEventsListener(mechanismListener);
				Thread.sleep(1000);

				// Rearrange the path (in RobotStudio) so that the to-Target is last,
				// after viapoints that may have been added during the simulation!
				path.getTargetRefs().item(var(2)).delete();
				path.insert(toTarget);
				path.getTargetRefs().item(var(path.getTargetRefs().getCount())).setMotionType(1);

				// Print richPath
				/*
				for (Iterator posIt = richPath.iterator(); posIt.hasNext();)
				{
					logger.info((RichPosition) posIt.next());
				}
				*/

				// Rearrange richPath so that the start and finish states are first and last
				// There is a problem since the current RobotStudio version (3.1) sometimes
				// adds collisions that are not really there (after strange jumps) and also
				// does not always put the final state (representing the reaching of the last
				// target of the path) last.
				// Rearrange start
				while (!((Position) richPath.getFirst()).getName().equals(STARTPOSITION_NAME))
				{
					logger.warn("Removing " + (RichPosition) richPath.getFirst());
					richPath.removeFirst();
				}
				// Rearrange finish
				int index = 0;
				while (!((Position) richPath.get(++index)).getName().equals(FINISHPOSITION_NAME));
				if (index<richPath.size()-1)
				{
					assert(((RichPosition) richPath.get(index)).getTime() >= ((RichPosition) richPath.getLast()).getTime());
					logger.warn("Moving finish from pos " + index + " to last.");
					RichPosition realFinish = (RichPosition) richPath.get(index);
					richPath.remove(index);
					richPath.addLast(realFinish);
				}
				// Change names
				String fromName = fromTarget.getName();
				String toName = toTarget.getName();
				fromName = fromName.substring(0,fromName.length()-2); // Last two are ":1"
				toName = toName.substring(0,toName.length()-2);       // Last two are ":1"
				((RichPosition) richPath.getFirst()).setName(fromName);
				((RichPosition) richPath.getLast()).setName(toName);

				// Print richPath
				/*
				for (Iterator posIt = richPath.iterator(); posIt.hasNext();)
				{
					logger.fatal((RichPosition) posIt.next());
				}
				*/

				// If no collisions, return!
				if (richPath.size() == 2)
				{
					return;
				}

				// The richPath should be used to generate the automata!
				// Modify zone automata
				for (int i=0; i< richPath.size(); i++)
				{
					RichPosition currPos = (RichPosition) richPath.get(i);

					// On the border of entering or exiting a zone?
					String inZone = currPos.getEnterZone();
					String outZone = currPos.getLeaveZone();

					// Detect error...
					if (inZone != null && outZone != null)
					{
						logger.fatal("Exiting and entering zone at the same time detected? " +
									 "This is impossible, tell Hugo!");
					}

					// Modify zone automaton
					if (inZone != null || outZone != null)
					{
						String nextPos;
						String prevPos;
						if (inZone != null)
						{
							nextPos = ((RichPosition) richPath.get(i+1)).getName();
							prevPos = ((RichPosition) richPath.get(i-1)).getName();
						}
						else
						{
							// Other way around...
							inZone = outZone;
							nextPos = ((RichPosition) richPath.get(i-1)).getName();
							prevPos = ((RichPosition) richPath.get(i+1)).getName();
						}

						Automaton zone = zoneAutomata.getAutomaton(inZone);
						Alphabet zoneAlpha = zone.getAlphabet();

						// Book event
						LabeledEvent bookEvent = new LabeledEvent(currPos.getName() + nextPos);
						zoneAlpha.addEvent(bookEvent);
						Arc arc = new Arc(zone.getStateWithName(FREESTATE_NAME),
										  zone.getStateWithName(BOOKEDSTATE_NAME),
										  bookEvent);
						zone.addArc(arc);

						// Unbook event (other direction)
						LabeledEvent unbookEvent = new LabeledEvent(currPos.getName() + prevPos);
						zoneAlpha.addEvent(unbookEvent);
						arc = new Arc(zone.getStateWithName(BOOKEDSTATE_NAME),
									  zone.getStateWithName(FREESTATE_NAME),
									  unbookEvent);
						zone.addArc(arc);
					}
				}

				// Modify Robot Automaton

				// Forward direction
				{
					// MODIFY ROBOT AUTOMATON ITSELF
					Automaton rob = robotAutomata.getAutomaton(robot.getName());
					RichPosition firstPos = (RichPosition) richPath.get(0);
					Position secondPos = (Position) richPath.get(1);
					Position secondLastPos = (Position) richPath.get(richPath.size()-2);
					Position lastPos = (Position) richPath.get(richPath.size()-1);
					assert(richPath.size() > 2);
					assert(from.getName().equals(firstPos.getName()));
					assert(to.getName().equals(lastPos.getName()));
					State firstState = new State(firstPos.getName() + secondPos.getName());
					rob.addState(firstState);
					State lastState =  rob.getStateWithName(from.getName() + to.getName());
					lastState.setName(secondLastPos.getName() + lastPos.getName());

					// Set cost for first state, must be int, so it now is the number of milliseconds
					firstState.setCost((int) (1000*firstPos.getTime()));

					// Modify original arcs
					LabeledEvent firstEvent = new LabeledEvent(firstPos.getName() + secondPos.getName());
					rob.getAlphabet().addEvent(firstEvent);
					//logger.info("Nbr of incoming: " + lastState.nbrOfIncomingArcs());
					LinkedList toBeRemoved = new LinkedList();
					for (ArcIterator arcIt = lastState.incomingArcsIterator(); arcIt.hasNext(); )
					{
						Arc currArc = arcIt.nextArc();
						toBeRemoved.add(currArc);

						Arc newArc = new Arc(currArc.getFromState(), firstState, firstEvent);
						rob.addArc(newArc);
					}
					while (toBeRemoved.size() > 0)
					{
						rob.removeArc(((Arc) toBeRemoved.remove(0)));
					}
					LabeledEvent oldEvent = rob.getAlphabet().getEvent(from.getName() + to.getName());
					rob.getAlphabet().removeEvent(oldEvent);

					// Add sequence
					State currState = firstState;
					for (int i=1; i<richPath.size()-1; i++)
					{
						RichPosition currPos = (RichPosition) richPath.get(i);
						RichPosition nextPos = (RichPosition) richPath.get(i+1);

						// Add new arc and stuff
						State nextState;
						if (i == richPath.size()-2)
						{
							nextState = lastState;
						}
						else
						{
							nextState = new State(currPos.getName() + nextPos.getName());
							rob.addState(nextState);
						}
						LabeledEvent event = new LabeledEvent(currPos.getName() + nextPos.getName());
						rob.getAlphabet().addEvent(event);
						Arc arc = new Arc(currState, nextState, event);
						rob.addArc(arc);

						// Set cost, must be int, so it now is the number of milliseconds
						nextState.setCost((int) (1000*(nextPos.getTime() - currPos.getTime())));

						currState = nextState;
					}

					// MODIFY ROBOT TARGET AUTOMATON
					if (!to.getName().equals(robot.getHomePosition().getName()))
					{
						Automaton target = robotAutomata.getAutomaton(robot.getName() + UNDERSCORE + to.getName());
						LabeledEvent event = new LabeledEvent(secondLastPos.getName() + lastPos.getName());
						Arc arc = new Arc(target.getStateWithName("0"), target.getStateWithName("1"), event);
						target.getAlphabet().addEvent(event);
						target.addArc(arc);
					}
				}
				// Backwards direction (lines with // in the end have been changed)
				{
					// MODIFY ROBOT AUTOMATON ITSELF
					Automaton rob = robotAutomata.getAutomaton(robot.getName());
					Position lastPos = (Position) richPath.get(0);				     //
					Position secondLastPos = (Position) richPath.get(1);             //
					RichPosition secondPos = (RichPosition) richPath.get(richPath.size()-2); //
					RichPosition firstPos = (RichPosition) richPath.get(richPath.size()-1);  //
					State firstState = new State(firstPos.getName() + secondPos.getName());
					rob.addState(firstState);
					State lastState =  rob.getStateWithName(to.getName() + from.getName());  //
					lastState.setName(secondLastPos.getName() + lastPos.getName());

					// Set cost for first state, must be int, so it now is the number of milliseconds
					firstState.setCost((int) (1000*(firstPos.getTime() - secondPos.getTime()))); //

					// Modify original arcs
					LabeledEvent firstEvent = new LabeledEvent(firstPos.getName() + secondPos.getName());
					rob.getAlphabet().addEvent(firstEvent);
					// logger.info("Nbr of incoming: " + lastState.nbrOfIncomingArcs());
					//ArcIterator arcIt = lastState.incomingArcsIterator();
					LinkedList toBeRemoved = new LinkedList();
					//for (Arc currArc = arcIt.nextArc(); arcIt.hasNext(); currArc = arcIt.nextArc())
					for (ArcIterator arcIt = lastState.incomingArcsIterator(); arcIt.hasNext(); )
					{
						Arc currArc = arcIt.nextArc();
						toBeRemoved.add(currArc);

						Arc newArc = new Arc(currArc.getFromState(), firstState, firstEvent);
						rob.addArc(newArc);
					}
					while (toBeRemoved.size() > 0)
					{
						rob.removeArc(((Arc) toBeRemoved.remove(0)));
					}
					LabeledEvent oldEvent = rob.getAlphabet().getEvent(to.getName() + from.getName()); //
					rob.getAlphabet().removeEvent(oldEvent);

					// Add sequence
					State currState = firstState;
					for (int i=richPath.size()-2; i>0; i--) //
					{
						RichPosition currPos = (RichPosition) richPath.get(i);
						RichPosition nextPos = (RichPosition) richPath.get(i-1); //

						// Add new arc and stuff
						State nextState;
						if (i == 1) //
						{
							nextState = lastState;
						}
						else
						{
							nextState = new State(currPos.getName() + nextPos.getName());
							rob.addState(nextState);
						}
						LabeledEvent event = new LabeledEvent(currPos.getName() + nextPos.getName());
						rob.getAlphabet().addEvent(event);
						Arc arc = new Arc(currState, nextState, event);
						rob.addArc(arc);

						// Set cost, must be int, so it now is the number of milliseconds
						nextState.setCost((int) (1000*(currPos.getTime() - nextPos.getTime()))); //

						currState = nextState;
					}

					// MODIFY ROBOT TARGET AUTOMATON
					if (!from.getName().equals(robot.getHomePosition().getName())) //
					{
						Automaton target = robotAutomata.getAutomaton(robot.getName() + UNDERSCORE + from.getName()); //
						LabeledEvent event = new LabeledEvent(secondLastPos.getName() + lastPos.getName());
						Arc arc = new Arc(target.getStateWithName("0"), target.getStateWithName("1"), event);
						target.getAlphabet().addEvent(event);
						target.addArc(arc);
					}
				}

				// Clean up
				//path.delete();
			}
			catch (Exception e)
			{
				logger.error("Error when examining collisions. " + e);
				// e.printStackTrace(System.err);

				return;

				//throw new SupremicaException();
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
		 * Puts them into the part called ZONEPART_NAME and then deletes the spans.
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
						// more than one object! The boolean is for keeping / not keeping original
						IEntities intersections = spanA.intersect(spanB, true);
						IPart parent = intersections.getParent();

						for (int m = 1; m <= intersections.getCount(); m++)
						{
							//intersections.item(var(m)).setName(spanA.getName() + spanB.getName() + "_" + m);
							String zoneName = "Zone" + zoneNbr++;
							intersections.item(var(m)).setName(zoneName);
							zones.setTransform(parent.getTransform());
							zones.addEntity(intersections.item(var(m)));

							// Add new automaton
							Automaton aut = new Automaton(zoneName);
							aut.setType(AutomatonType.Specification);

							// Add two states, Free and Booked
							State state = new State(FREESTATE_NAME);
							state.setAccepting(true);
							state.setInitial(true);
							aut.addState(state);
							// aut.setInitialState(state);
							state = new State(BOOKEDSTATE_NAME);
							aut.addState(state);

							// Add automaton
							zoneAutomata.addAutomaton(aut);
						}

						// Delete this span
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

		//////////////////////////////////
		// DAppEvents interface methods //
		//////////////////////////////////

		public void selectionChanged()
		{
			//logger.info("Selection changed.");
		}

		public void quit()
		{
			// This is fatal, cause this has never ever happened
			// and I'd like to know if it ever does! (Magnus said it should!)
			logger.fatal("RobotStudio is shutting down. Please tell Hugo " + "if, when and how you got this message!");

			// clean();
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

		/**
		 * Listener for detecting when targets are reached and when collisions
		 * begin and end.
		 */
		private class MechanismListener
			extends _MechanismEventsAdapter
		{
			private IABBS4Controller controller = null;
			private boolean leavingTarget = true;    // targetReached is invoked twice for every Target!
			private boolean controllerStarted = false;    // Used in the wait-method
			private Path path;
			private Mechanism mechanism;

			// Domenico stuff
			// Costs for the path in simulation
			//private CreateXml.PathWithCosts pathcosts;
			// Time for the previous event (either start or end)
			// private double previousTime = 0;

			// Dynamic list of objects colliding with the robot
			private LinkedList objectsColliding = new LinkedList();

			// A list of the positions and zones passed
			LinkedList posList = new LinkedList();

			/**
			 * Returns the list of visited positions including zone info.
			 */
			public LinkedList getRichPath()
			{
				return posList;
			}

			public MechanismListener(Mechanism mechanism, Path path)
			{
				try
				{
					this.path = path;
					this.mechanism = mechanism;

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
						logger.info(mechanism.getName() + " is already inside the zone " + ((Collider) it.next()).getName() + " at target " + path.getTargetRefs().item(var(1)).getName() + ".");
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
			 * position, false otherwise
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
						// Log
						//logger.debug("Target reached at time " + (float) motionTime + ".");

						// Set the cost
						//Double realCost = new Double((motionTime - previousTime) * 1000);    // [ms]
						//pathcosts.insertCost(new Integer(realCost.intValue()));
						//robotCosts[getRobotIndex(mechanism.getName())].add(pathcosts);

						// Remember
						posList.add(new RichPosition(FINISHPOSITION_NAME, motionTime, null, null));
					}
					else
					{
						// Remember
						posList.add(new RichPosition(STARTPOSITION_NAME, motionTime, null, null));
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
					int indexZone = getZoneIndex(objectName);
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
						int robotIndex = getRobotIndex(mechanism.getName());
						String viaPointName = mechanism.getName().substring(5) + VIAPOINT_SUFFIX + nbrOfTimesCollision;
						ITarget viaTarget = createTargetAtTCP(viaPointName);

						// Insert the new target in the path
						ITargetRef viaTargetRef = path.insert(viaTarget);
						viaTargetRef.setMotionType(1);

						nbrOfTimesCollision++;

						// Remember
						posList.add(new RichPosition(viaPointName, time, objectName, null));
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
						// Double realCost = new Double((time - previousTime) * 1000);
						// pathcosts.insertCost(new Integer(realCost.intValue()));
						// previousTime = time;

						// Create a target here!
						//String viaPointName = "Out" + indexZone + "_";
						//viaPointName = viaPointName + path.getName() + nbrOfTimesCollision;
						//String viaPointName = "Out" + objectName + "_";
						//viaPointName = viaPointName + nbrOfTimesCollision;
						String viaPointName = mechanism.getName().substring(5) + VIAPOINT_SUFFIX + nbrOfTimesCollision;
						ITarget viaTarget = createTargetAtTCP(viaPointName);

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

						// Remember
						posList.add(new RichPosition(viaPointName, time, null, objectName));
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
					logger.error("Error in afterControllerShutdown. " + ex);
				}

				// This is fatal since it has never happened before and I want to know
				// if it ever does...
				logger.fatal("Virtual Controller shut down.");
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

		private class RichPosition
			implements Position
		{
			/** The name of this position */
			String name;
			/** The time it took to get here */
			double timeToPos;
			/** The name of the zone (if any) just about to be entered */
			String enterZone = null;
			/** The name of the zone (if any) just left */
			String leaveZone = null;

			RichPosition(String name, double timeToPos, String enterZone, String leaveZone)
			{
				this.name = name;
				this.timeToPos = timeToPos;
				this.enterZone = enterZone;
				this.leaveZone = leaveZone;
			}

			public String toString()
			{
				return name + " at " + timeToPos + ", enter: " + enterZone + ", leave: " + leaveZone;
			}

			public String getName()
			{
				return name;
			}

			public void setName(String name)
			{
				this.name = name;
			}

			public double getTime()
			{
				return timeToPos;
			}

			public String getEnterZone()
			{
				return enterZone;
			}

			public String getLeaveZone()
			{
				return leaveZone;
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
				spans = addPart(getName() + SPANS_SUFFIX);
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

			/**
			 * RS class that does transform calculations.
			 */
			private RsUnitsUtility ruu = new RsUnitsUtility();

			// The size of the box surrounding the tooltip
			private static final double BOXSIZE = 0.12;             // [m]
			private static final double CYLINDERLENGTH = 1.1;       // [m]
			private static final double CYLINDERRADIUS = 0.06;      // [m]
			private static final double STEPSIZE = BOXSIZE * 3 / 4; // [m]
			// The MARGIN that should be added to all sides
			private static final double MARGIN = 0.05;              // [m]

			private Transform oldTransform;
			private Part temp;

			/**
			 * The collection of span approximations for the current path
			 */
			private IEntities spanEntities;

			public SpanGenerator(String pathName)
				throws Exception
			{
				this.pathName = pathName;

				/*
				if (ruu == null)
				{
					ruu = new RsUnitsUtility();
				}
				*/

				temp = addPart("Temp");
				spanEntities = temp.getEntities();

				// Create first cover
				tool0 = mechanism.getToolFrames().item(var("tool0"));
				oldTransform = transformCopy(tool0.getTransform());

				//createSpanEntity(oldTransform);
			}

			////////////////////////////////////////////////
			// Events generated by RobotStudio.Simulation //
			////////////////////////////////////////////////

			/**
			 * Called on each time instant.
			 */
			public void tick(double time)
			{
				// In each tick, examine if it is time to generate a new spanEntity
				try
				{
					ITransform newTransform = tool0.getTransform();
					double dx = oldTransform.getX() - newTransform.getX();
					double dy = oldTransform.getY() - newTransform.getY();
					double dz = oldTransform.getZ() - newTransform.getZ();

					// If tool0 has moved far enough, create a new span approximation!
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

			/**
			 * Called when the simulation stops
			 */
			public void stop()
			{
				// Generate the union of the spanEntities
				try
				{
					// Create a final cover
					// createSpanEntity(tool0.getTransform());
					// IEntities spanEntities = spans.getEntities();
					IEntity unionEntity;

					// Only if there are at least two spanEntities
					if (spanEntities.getCount() >= 2)
					{
						unionEntity = spanEntities.item(var(2));
						IPart oldPart = null;
						boolean shutup = false;

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
								if (!shutup)
								{
									logger.warn("Disjoint entities in span?");
									shutup = true;
								}

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
					{
						// Only one spanEntity
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

					// If there are elements left in temp, make them red!
					temp.setColor(RS_RED);
					temp.setRelativeTransparency((float) 0.9);
					station.getSelections().removeAll();
					if (temp.getEntities().getCount() > 0)
					{
						logger.warn("RobotStudio struck problems when calculating the span for " +
									getName() + ". Do not trust the solution!");
					}
					// If temp is empty, delete it!
					// temp.delete();
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

		////////////////////////////////
		// Position interface methods //
		////////////////////////////////

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

			logger.debug("Simulation finished.");

			notify();
		}

		public void tick(double time)
		{
			//System.out.println("Simtick: " + time);
		}

		////////////////////////////
		// Junk constructed by me //
		////////////////////////////

		/**
		 * This method is designed to return when the current simulation has stopped.
		 */
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
     * VB:s counterpart of java's Object.)
     */
    private static Variant var(int i)
		throws Exception
    {
		return new Variant(i);
    }

    /**
     * Typecast i into Variant, for convenience! (Variant is something like
     * VB:s counterpart of java's Object.)
     */
    private static Variant var(boolean i)
		throws Exception
    {
		return new Variant(i);
    }

    /**
     * Typecast i into Variant, for convenience! (Variant is something like
     * VB:s counterpart of java's Object.)
     */
    private static Variant var(String i)
		throws Exception
    {
		return new Variant(i);
    }
}
