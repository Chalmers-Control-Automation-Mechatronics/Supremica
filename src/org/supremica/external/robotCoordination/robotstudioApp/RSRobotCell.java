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

package org.supremica.external.robotCoordination.robotstudioApp;

import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.enums.RsKinematicRole;
import com.inzoom.comjni.enums.HResult;
import com.inzoom.comjni.Variant;
import com.inzoom.comjni.SafeArray;
import com.inzoom.comjni.ComJniException;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.external.robotCoordination.*;
import java.util.*;
import java.io.*;
import java.awt.Color;

/**
 * Implementation of the RobotCell interface for use against RobotStudio.
 */
public class RSRobotCell
    implements RobotCell, DAppEvents
{
    private static Logger logger = LoggerFactory.createLogger(RSRobotCell.class);
	
    // Initialize jacoZoom
    static
    {
    	com.inzoom.comjni.Dll.runRoyaltyFree(643622874);
    }
    // Constants
	
    /** The name of the IPart containing the mutex zones. */
    final static String ZONEPART_NAME = "MutexZones";
	
    final static String ZONEENTITY_BASENAME = "MutexZone";
    final static String FREESTATE_NAME = "Free";
    final static String BOOKEDSTATE_NAME = "Booked";
	
    /** The suffix of the Part containing the spans. */
    final static String SPAN_SUFFIX = "_Span";
    final static String SPANS_SUFFIX = SPAN_SUFFIX + "s";

    /** The name of the module containing the paths */
    final static String PATHSMODULE_NAME = "Paths";

    /** Automata constants */
    final static String STARTPOSITION_NAME = "START";
    final static String FINISHPOSITION_NAME = "FINISH";

    /** Automata constants */
    final static String STARTSTATE_NAME = "start";
    final static String FINISHSTATE_NAME = "finish";
    final static String FINISHEVENT_NAME = "fin";
    final static String UNDERSCORE = "_";

    /** Via point suffix */
    final static String VIAPOINT_SUFFIX = "vp";

    /** Colours */
    static Variant RS_WHITE;
    static Variant RS_RED;
    static Variant RS_GREEN;
    static Variant RS_BLUE;

    /** The RobotStudio application. */
    static Application app = null;
    
    /** The active RobotStudio station. */
    static Station station = null;

    // Domenico stuff
    static int nbrOfTimesCollision = 1;
    static LinkedList[] robotCosts;
    
    /** The active RobotStudio station. */
    static Part zones;

    /** The Box Factory */
    RSBoxFactory boxFactory = null;
    final static double[] boxDimenstions = new double[]{0.5, 0.5, 0.5};

    /** Generated automata */
    private Automata robotAutomata = new Automata();
    private Automata zoneAutomata = new Automata();

    // Running number of added zones
    private int zoneNbr = 1;

    // Tillfälligt (i varje fall på det här stället)
    //AK
    public void tempAvenirs() 
	throws Exception 
    {
	List<Robot> robots = getRobots();
	    
	for (int i=0; i<robots.size(); i++) {
	    logger.error("robot.name = " + robots.get(i).getName());
	    Coordinate base = robots.get(i).getBaseCoordinates();
	    logger.warn("X = " + base.getX() + "; Y = " + base.getY() + "; Z = " + base.getZ());
	}

	createBox(new RSCoordinate(0,0,0), Color.blue, 0);
	createBox(new RSCoordinate(1,1,1), Color.green, 0);
	createBox(new RSCoordinate(1,0,0), Color.cyan, 0);
	createBox(new RSCoordinate(0,0,1), Color.lightGray, 0);
    }
	
    public RSRobotCell(File file)
    {
		try
	    {
			init();
			openStation(file);
			station.setBackgroundColor(RS_WHITE);
			station.setFloorVisible(false);
			
			//AK
			tempAvenirs();
			
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
	
    /////////////////////////////////
    // RobotCell INTERFACE METHODS //
    /////////////////////////////////
		
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
		List<Robot> robots = getRobots();
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
    public List<Robot> getRobots()
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

    public synchronized Box createBox(Coordinate coord, Color color, double transparency) 
	throws Exception
    {
	if (boxFactory == null)
	    boxFactory = new RSBoxFactory(boxDimenstions);

	return boxFactory.createBox(coord, color, transparency);
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
    public Automata buildRobotAutomata(List<Robot> robots)
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
			List<org.supremica.external.robotCoordination.Position> posList = robot.getPositions();
			for (int i=0; i < posList.size(); i++)
			{
				for (int j=0; j < posList.size(); j++)
				{
					if (i != j)
					{
						State state = new State(((org.supremica.external.robotCoordination.Position) posList.get(i)).getName() +
												((org.supremica.external.robotCoordination.Position) posList.get(j)).getName());
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
						String name = ((org.supremica.external.robotCoordination.Position) posList.get(0)).getName() +
							((org.supremica.external.robotCoordination.Position) posList.get(i)).getName();
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
						if (fromState.getName().endsWith(((org.supremica.external.robotCoordination.Position) posList.get(i)).getName()))
						{
							// Just to make sure there is no ambiguity
							if (fromPos != null)
							{
								throw new Exception("Error in RSRobotCell.java, ambigous position names");
							}
							fromPos = ((org.supremica.external.robotCoordination.Position) posList.get(i)).getName();
							
							// Create arc for each possible target position
							for (int j=0; j < posList.size(); j++)
							{
								if (i != j)
								{
									// Create new arc...
									String name = ((org.supremica.external.robotCoordination.Position) posList.get(i)).getName() +
										((org.supremica.external.robotCoordination.Position) posList.get(j)).getName();
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
			List<org.supremica.external.robotCoordination.Position> positions = robot.getPositions();
			// Skip home position (i=1...)
			for (int i=1; i<positions.size(); i++)
			{
				org.supremica.external.robotCoordination.Position pos = (org.supremica.external.robotCoordination.Position) positions.get(i);
				
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
	
    /**
     * Adds part to activeStation and returns it. If there already was
     * a part with the same name, it is returned instead.
     */
    static Part addPart(String name)
    {
		Part part = null;
		
		try
	    {
		try
		    {
			// If there already is one, get it!
			part = Part.getPartFromUnknown(station.getParts().item(RSRobotCell.var(name)));
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
    public int getZoneIndex(String zoneName)
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
    public void examineCollisions(Robot robot, org.supremica.external.robotCoordination.Position from, org.supremica.external.robotCoordination.Position to)
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
		MechanismListener mechanismListener = new MechanismListener(this, mechanism, path);
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
		while (!((org.supremica.external.robotCoordination.Position) richPath.getFirst()).getName().equals(STARTPOSITION_NAME))
		    {
			logger.warn("Removing " + (RichPosition) richPath.getFirst());
			richPath.removeFirst();
		    }
		// Rearrange finish
		int index = 0;
		while (!((org.supremica.external.robotCoordination.Position) richPath.get(++index)).getName().equals(FINISHPOSITION_NAME));
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

		/*
		// If no collisions, not much needs to be done...
		if (richPath.size() == 2)
		{
		// MODIFY ROBOT TARGET AUTOMATON
		if (!to.getName().equals(robot.getHomePosition().getName()))
		{
		Automaton target = robotAutomata.getAutomaton(robot.getName() + UNDERSCORE + to.getName());
		Position fromPos = (Position) richPath.get(0);
		Position toPos = (Position) richPath.get(1);
		LabeledEvent event = new LabeledEvent(fromPos.getName() + toPos.getName());
		Arc arc = new Arc(target.getStateWithName("0"), target.getStateWithName("1"), event);
		target.getAlphabet().addEvent(event);
		target.addArc(arc);
		}

		return;
		}
		*/

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
		    org.supremica.external.robotCoordination.Position secondPos = (org.supremica.external.robotCoordination.Position) richPath.get(1);
		    org.supremica.external.robotCoordination.Position secondLastPos = (org.supremica.external.robotCoordination.Position) richPath.get(richPath.size()-2);
		    org.supremica.external.robotCoordination.Position lastPos = (org.supremica.external.robotCoordination.Position) richPath.get(richPath.size()-1);
		    //assert(richPath.size() > 2);
		    assert(from.getName().equals(firstPos.getName()));
		    assert(to.getName().equals(lastPos.getName()));

		    // Only if there was at least one collision
		    if (richPath.size() > 2)
			{
			    State firstState = new State(firstPos.getName() + secondPos.getName());
			    rob.addState(firstState);
			    State lastState = rob.getStateWithName(from.getName() + to.getName());
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
		    org.supremica.external.robotCoordination.Position lastPos = (org.supremica.external.robotCoordination.Position) richPath.get(0);				     //
		    org.supremica.external.robotCoordination.Position secondLastPos = (org.supremica.external.robotCoordination.Position) richPath.get(1);             //
		    RichPosition secondPos = (RichPosition) richPath.get(richPath.size()-2); //
		    RichPosition firstPos = (RichPosition) richPath.get(richPath.size()-1);  //

		    // Only if there was at least one collision
		    if (richPath.size() > 2)
			{
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
     * Typecast i into Variant, for convenience! (Variant is something like
     * VB:s counterpart of java's Object.)
     */
    static Variant var(int i)
	throws Exception
    {
	return new Variant(i);
    }

    /**
     * Typecast i into Variant, for convenience! (Variant is something like
     * VB:s counterpart of java's Object.)
     */
    static Variant var(boolean i)
	throws Exception
    {
	return new Variant(i);
    }

    /**
     * Typecast i into Variant, for convenience! (Variant is something like
     * VB:s counterpart of java's Object.)
     */
    static Variant var(String i)
	throws Exception
    {
	return new Variant(i);
    }
}