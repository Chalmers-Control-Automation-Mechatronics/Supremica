package org.supremica.external.robotCoordination;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import org.supremica.log.*;

import org.supremica.gui.*;
import org.supremica.automata.*;
import org.supremica.properties.*;
import org.supremica.external.robotCoordination.RobotStudio.RSCell;
import org.supremica.util.ActionTimer;
import java.awt.Toolkit;
import java.awt.Color;

/**
 * GUI and algorithms for examining robot cells.
 */
public class CellExaminer
    extends JDialog
{
    // private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.createLogger(CellExaminer.class);
    private Frame owner = null;
    private JPanel contentPane = null;

    // Robot stuff
    private Cell cell;

    // Supremica stuff
    private Automata zoneAutomata;
    private Automata robotAutomata;

    // Automata constants
    public final static String STARTSTATE_NAME = "start";
    public final static String FINISHSTATE_NAME = "finish";
    public final static String FINISHEVENT_NAME = "fin";
    public final static String FREESTATE_NAME = "Free";
    public final static String BOOKEDSTATE_NAME = "Booked";
    public final static String UNDERSCORE = "_";

    /** The prefix of a zone's name. */
    static final String ZONE_PREFIX = "Zone_";

    // Colors
    static final Color RED = Color.RED;
    static final Color ORANGE = Color.ORANGE;
    static final Color GREEN = Color.GREEN;
    static final Color BLUE = Color.BLUE;
    static final Color BLACK = Color.BLACK;
    // Transparency
    static final double TRANSPARENCY = 0.9;

    // Demo file
    // final String DEMOSTATION_FILENAME = "C:/temp/RobSuprTestStation/RobSuprTest.stn";
    // final String DEMOSTATION_FILENAME = "C:/temp/DomStations/DemoSafe.stn";
    String DEMOSTATION_FILENAME = "DemoSafe.stn";
    //String DEMOSTATION_FILENAME = "DemoSafeWithZones.stn";

    /**
     * Dialog for manipulating the simulation environment.
     */
    public CellExaminer(Frame owner)
    {
	super(owner, "Cell examiner", false);
	this.owner = owner;

	if (SupremicaProperties.getFileRSDemoOpenPath() != null)
	    DEMOSTATION_FILENAME = SupremicaProperties.getFileRSDemoOpenPath() + File.separator + DEMOSTATION_FILENAME;
	else
	    DEMOSTATION_FILENAME = "C:\\temp\\DomStations" + File.separator + DEMOSTATION_FILENAME;

	contentPane = (JPanel) getContentPane();
	contentPane.setLayout(new FlowLayout());

	JButton openButton = new JButton("Open cell");
	openButton.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    // Let the user choose a file
		    JFileChooser fileOpener = FileDialogs.getRobotCellFileImporter();
		    if (fileOpener.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		    {
			File file = fileOpener.getSelectedFile();
			openCell(file);
		    }
		    else
		    {
			return;
		    }
		}
	    });
	contentPane.add(openButton);

	/*
	  JButton spanButton = new JButton("Generate spans");
	  spanButton.addActionListener(new ActionListener()
	  {
	  public void actionPerformed(ActionEvent e)
	  {
	  generateSpans();
	  }
	  });
	  contentPane.add(spanButton);

	  JButton cutButton = new JButton("Intersect spans");
	  cutButton.addActionListener(new ActionListener()
	  {
	  public void actionPerformed(ActionEvent e)
	  {
	  intersectSpans();
	  }
	  });
	  contentPane.add(cutButton);

	  JButton collisionButton = new JButton("Examine collisions");
	  collisionButton.addActionListener(new ActionListener()
	  {
	  public void actionPerformed(ActionEvent e)
	  {
	  examineCollisions();
	  }
	  });
	  contentPane.add(collisionButton);
	*/

	// Span generation demo button
	File file = new File(DEMOSTATION_FILENAME);
	JButton demoButton = new JButton("Span generation demo");
	demoButton.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    // If there is an open file, don't do this!
		    if (cell == null || !cell.isOpen())
		    {
			// Open demo file if it exists...
			File file = new File(DEMOSTATION_FILENAME);
			openCell(file);
		    }

		    // Run the demo!
		    try
		    {
			ActionTimer timer = new ActionTimer();
			timer.start();

			generateSpans();
			intersectSpans();
			generateAutomata();
			examineCollisions();

			timer.stop();
			logger.info("Execution completed after " + timer.toString());
		    }
		    catch (Exception ex)
		    {
			logger.error("Error when running span generation demo: " + ex);
			logger.warn(ex.getStackTrace());
		    }
		}
	    });
	contentPane.add(demoButton);

	// Box strategy demo button
	demoButton = new JButton("Box strategy demo");
	demoButton.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    // If there is an open file, don't do this!
		    if (cell == null || !cell.isOpen())
		    {
			// Open demo file if it exists...
			File file = new File(DEMOSTATION_FILENAME);
			openCell(file);
		    }

		    //Run the demo!
		    try
		    {
			boxStrategy(cell);
		    }
		    catch (Exception ex)
		    {
			logger.error("Error when running box strategy demo: " + ex);
			logger.warn(ex.getStackTrace());
		    }
		}
	    });
	contentPane.add(demoButton);

	pack();
	init();
    }

    /**
     * Should be called each time a new station is opened.
     */
    private void init()
    {
	//zoneAutomata = new Automata();
	//robotAutomata = new Automata();
    }

    // ACTIONS

    /**
     * Initiates simulation environment and opens cell with certain name.
     */
    private void openCell(File file)
    {
	if (!file.exists())
	{
	    logger.error("File " + file + " does not exist.");
	    return;
	}

	String cellName = file.getAbsolutePath();

	// Choose simulator based on file type...
	if (cellName.endsWith(".stn"))
	{
	    // A RobotStudio station!
	    // Here, it would be a good thing to examine if RobotStudio is
	    // properly installed...

	    // Open cell
	    cell = new RSCell(file);
	}
	else
	{
	    logger.error("The file " + cellName + " is not a robot station or is " +
			 "not of a type supported by Supremica.");
	    return;
	}

	// Set the project name based on the file name...
	ActionMan.getGui().getVisualProjectContainer().getActiveProject().setName(file.getName());

	// Initialize
	init();
    }

    /**
     * Generates spans
     */
    private void generateSpans()
    {
	// Make sure a cell is open!
	if (cell == null || !cell.isOpen())
	{
	    logger.error("No cell opened.");
	    return;
	}

	try
	{
	    // For each robot, generate spans
	    List<Robot> robots = cell.getRobots();
	    for (Iterator robotIt = robots.iterator(); robotIt.hasNext(); )
	    {
		Robot robot = (Robot) robotIt.next();

		// Initalize
		logger.debug("Starting robot " + robot + ".");
		robot.start();

		// Generate span for each "path", i.e. unique pair of positions
		List<Configuration> configurations = robot.getConfigurations();
		for (int i = 0; i < configurations.size(); i++)
		{
		    Configuration from = (Configuration) configurations.get(i);

		    for (int j = i + 1; j < configurations.size(); j++)
		    {
			Configuration to = (Configuration) configurations.get(j);

			// Generate span!
			logger.debug("Generating span from " + from + " to " + to + " for " + robot + ".");
			robot.generateSpan(from, to);
		    }
		}

		// Finalize
		robot.jumpToConfiguration(robot.getHomeConfiguration());
		robot.stop();
	    }
	}
	catch (Exception ex)
	{
	    logger.error("Error when generating spans. " + ex);
	}
    }

    /**
     * Intersect spans
     */
    private void intersectSpans()
    {
	if (cell == null || !cell.isOpen())
	{
	    logger.error("No cell opened.");
	    return;
	}

	try
	{
	    // Intersect spans of robots, pairwise
	    List<Robot> robots = cell.getRobots();

	    for (int i = 0; i < robots.size(); i++)
	    {
		Robot robotA = (Robot) robots.get(i);

		for (int j = i + 1; j < robots.size(); j++)
		{
		    Robot robotB = (Robot) robots.get(j);

		    // Intersect spans!
		    cell.intersectSpans(robotA, robotB);
		}

		// Hide finished spans
		robotA.hideSpan();
	    }
	}
	catch (Exception ex)
	{
	    logger.error("Error when intersecting spans. " + ex);
	}
    }

    /**
     * Generates zone automata
     */
    private void generateAutomata()
    {
	try
	{
	    zoneAutomata = cell.generateZoneAutomata();
	    robotAutomata = cell.generateRobotAutomata();

	    ActionMan.getGui().addAutomata(robotAutomata);
	    ActionMan.getGui().addAutomata(zoneAutomata);
	}
	catch (Exception ex)
	{
	    logger.error("Error when generating zone automata. " + ex);
	}
    }

    /**
     * Run simulation for collisions
     */
    private void examineCollisions()
    {
	if (cell == null || !cell.isOpen())
	{
	    logger.error("No cell opened.");
	    return;
	}
	
	try
	{
	    // For each robot
	    List<Robot> robots = cell.getRobots();
	    for (Iterator robotIt = robots.iterator(); robotIt.hasNext(); )
	    {
		Robot robot = (Robot) robotIt.next();

		// Initalize
		robot.start();

		// Examine collisions for each "path", i.e. unique
		// pair of configurations
		List<Configuration> configurations = robot.getConfigurations();
		for (int i = 0; i < configurations.size(); i++)
		{
		    Configuration from = (Configuration) configurations.get(i);

		    for (int j = i + 1; j < configurations.size(); j++)
		    {
			Configuration to = (Configuration) configurations.get(j);

			// Examine path for collisions
			logger.info("Examining the motion from " + from + " to " + to + " for " + robot + ".");
			cell.examineCollisions(robot, from, to);
		    }
		}

		// Finalize
		robot.jumpToConfiguration(robot.getHomeConfiguration());
		robot.stop();
	    }
	}
	catch (Exception ex)
	{
	    logger.error("Error when simulating for collisions. " + ex);
	}
    }

    /**
     * Does the box strategy stuff. The span of a robot is
     * approximated as neighbouring boxes. Which boxes are occupied by
     * a robot during its motion is calculated first by starting at a
     * known coordinate inside the robot and examining for the
     * corresponding box whether it "collides" with the robot or
     * not. If it collides, all the neighbouring boxes are exained in
     * the same fashion. During this process, only the boxes that are
     * found to be entirely outside the robot from the start are saved
     * and represented in the simulation environment. After this, the
     * robot moves through its entire repertiore of motions and each
     * time the robot collides with a box, that box is deleted and a
     * part of the "span" while a new layer of boxes is added
     * "outside" the removed box.
     */
    private void boxStrategy(Cell cell)
	throws Exception
    {
	// Make sure a cell is open!
	if (cell == null || !cell.isOpen())
	{
	    logger.error("No cell opened.");
	    return;
	}

	ActionTimer timer = new ActionTimer();
	timer.start();

	// Discretization parameters
	double dx = 0.35;
	double dy = 0.35;
	double dz = 0.35;
	cell.setBoxDimensions(new double[] {dx,dy,dz});

	//////////////////////////
	// GENERATE THE BOXSPAN //
	//////////////////////////

	// Hashtable associating coordinates with status of the corresponding box
	Hashtable<Coordinate, Status> matrix = new Hashtable(1000);

	// List of boxes where collisions may occur
	List<Coordinate> zoneboxes = new LinkedList<Coordinate>();

	// Get the robots
	List<Robot> robots = cell.getRobots();
	// For every robot...
	for (Iterator<Robot> robIt = robots.iterator(); robIt.hasNext(); )
	{
	    ActionTimer robottimer = new ActionTimer();
	    robottimer.start();

	    Robot robot = robIt.next();
	    Configuration home = robot.getHomeConfiguration();

	    // Initialize robot
	    robot.start();
	    robot.jumpToConfiguration(home);

	    ///////////////////////////////////////
		// BUILD BOXES FOR THE HOME POSITION //
		///////////////////////////////////////

		// List of coordinates of boxes that should be examined
		//List<Coordinate> boxesToExamine = new LinkedList<Coordinate>();
		SortedSet<Coordinate> boxesToExamine = new TreeSet<Coordinate>();
		// List of the coordinates of boxes that have already been examined
		// (for resetting the matrix)
		List<Coordinate> boxesExamined = new LinkedList<Coordinate>();
		// List of the coordinates of boxes on "the surface"
		List<Coordinate> surfaceboxes = new LinkedList<Coordinate>();

		// Get base coords and build first box
		Coordinate base = robot.getBaseCoordinates();
		boxesToExamine.add(base);

		// Add neighbouring boxes to boxes that are colliding with the robot
		// Start loop! 
	    loop: while (boxesToExamine.size() != 0)
		{
		    // Get the status for this box
		    Coordinate coord = boxesToExamine.first();
		    boxesToExamine.remove(coord);

		    // Examine status and set this box as checked (we will
		    // soon check it!)
		    Status status;
		    if (!matrix.containsKey(coord))
		    {
			status = new Status();
			status.occupied = false;
			status.checked = true;
			matrix.put(coord, status);
		    }
		    else
		    {
			status = matrix.get(coord);
			// If it's already checked, move on!
			if (status.checked)
			    continue loop;
			status.checked = true;
		    }
		    Box box = cell.createBox(coord);
		    //box.setColor(ORANGE);
		    box.setTransparency(TRANSPARENCY);

		    // Inside robot?
		    if (robot.collidesWith(box))
		    {
			// Remove from simulation environment
			box.setColor(RED);
			box.delete();

			// Has someone else collided with this one?
			if (status.occupied)
			{
			    zoneboxes.add(coord);
			}
			status.occupied = true;

			int x = coord.getX();
			int y = coord.getY();
			int z = coord.getZ();

			Coordinate newCoord;
			Box newBox;

			// Down
			newCoord = new Coordinate(x,y,z-1);
			boxesToExamine.add(newCoord);
			// Right
			newCoord = new Coordinate(x,y-1,z);
			boxesToExamine.add(newCoord);
			// Back
			newCoord = new Coordinate(x-1,y,z);
			boxesToExamine.add(newCoord);
			// Forward
			newCoord = new Coordinate(x+1,y,z);
			boxesToExamine.add(newCoord);
			// Left
			newCoord = new Coordinate(x,y+1,z);
			boxesToExamine.add(newCoord);
			// Up
			newCoord = new Coordinate(x,y,z+1);
			boxesToExamine.add(newCoord);
		    }
		    else
		    {
			// This box is a "surfacebox"
			box.setColor(GREEN);
			//box.delete();
			surfaceboxes.add(coord);
		    }
		    boxesExamined.add(coord);
		}

		// Didn't find anything? The base coordinate box was outside?
		if (boxesExamined.size() == 1)
		{
		    logger.error("Base coordinate box is not inside robot.");
		    return;
		}

		//////////////////////////////////////////////////////////
		// BUILD BOXES FOR THE REST OF THE SPAN (FOR ALL PATHS) //
		//////////////////////////////////////////////////////////

		// Listen to the robot (for collisions - a part of the box span generation!)
		RobotListener listener = new BoxSpanGenerator(cell, robot, matrix, zoneboxes, surfaceboxes);
		robot.setRobotListener(listener);

		// Push boxes for each "path", i.e. unique pair of configurations
		List<Configuration> configurations = robot.getConfigurations();
		// The first loop must start with the home configuration, since the
		// surfaceboxes always should be pushed "outwards"
		if (!home.getName().equals(configurations.get(0).getName()))
		{
		    throw new Exception("The home target should always be the first target.");
		}
		// Now the paths, two nested loops of configurations...
		for (int i = 0; i < configurations.size(); i++)
		{
		    Configuration from = (Configuration) configurations.get(i);

		    for (int j = i + 1; j < configurations.size(); j++)
		    {
			Configuration to = (Configuration) configurations.get(j);

			// Generate span!
			logger.info("Pushing boxes moving from " + from + " to " + to + " for " + robot + ".");
			robot.jumpToConfiguration(from);
			cell.runSimulation(robot, from, to);
		    }
		}
		// Finalize the robot
		robot.setRobotListener(null);
		robot.jumpToConfiguration(home);
		cell.runSimulation(robot, home, home);
		robot.stop();

		//////////////
		// CLEAN UP //
		//////////////

		// Clear checked-status from the examined boxes before examining next robot!
		while (boxesExamined.size() != 0)
		{
		    Coordinate coord = boxesExamined.remove(0);
		    Status status = matrix.get(coord);
		    status.checked = false;
		}

		// It's over for this robot. Remove the "surfaceboxes"
		int surfaceboxCount = surfaceboxes.size();
		while (surfaceboxes.size() != 0)
		{
		    Coordinate coord = surfaceboxes.remove(0);
		    try
		    {
			cell.destroyBox(coord);
		    }
		    catch (Exception ex)
		    {
			// Box already deleted during the "pushing" (I hope).
			surfaceboxCount--;
		    }

		    // If the box has been occupied, then it's important to keep the matrix info!
		    Status status = matrix.get(coord);
		    status.checked = false;
		    if (status.occupied == false)
		    {
			matrix.remove(coord);
		    }

		    //Box box = cell.createBox(coord);
		    //box.setColor(GREEN);
		    //box.setTransparency(TRANSPARENCY);
		}
		robottimer.stop();
		logger.info("Execution completed for robot " + robot + " after " + robottimer + ".");
		logger.info("Amount of surfaceboxes: " + surfaceboxCount);
	}

	// Don't need this one anymore
	matrix.clear();

	// Show the result, the zoneboxes! Create a list with the
	// zones for the next part of the algorithm...
	List<Volume> zones = new LinkedList<Volume>();
	int zoneCount = 0;
	while (zoneboxes.size() != 0)
	{
	    Coordinate coord = zoneboxes.remove(0);
	    if (zoneboxes.contains(coord))
		continue;

	    Box box = cell.createBox(coord);
	    box.setColor(BLUE);
	    box.setTransparency(TRANSPARENCY);
	    box.setName(ZONE_PREFIX + ++zoneCount);
	    zones.add(box);
	}
	timer.stop();
	logger.info("Execution completed after " + timer.toString());
	logger.info("Amount of zoneboxes: " + zoneCount);
		
	//////////////////////
	// REFINE THE ZONES //
	//////////////////////

	// Just do it!

	/////////////////////////////////////////////
	    // TEST FOR COLLISIONS WITH THE ZONE BOXES //
	    /////////////////////////////////////////////

	    // Use the old collision detection method (ugly stuff)
	    zoneAutomata = buildBaseZoneAutomata(zones, robots);
	    robotAutomata = buildBaseRobotAutomata(robots);
	    ((RSCell) cell).zoneAutomata = zoneAutomata;
	    ((RSCell) cell).robotAutomata = robotAutomata;
	    ActionMan.getGui().addAutomata(robotAutomata);
	    ActionMan.getGui().addAutomata(zoneAutomata);
	    examineCollisions();

	    /*
	    // For every robot...
	    for (Iterator<Robot> robIt = robots.iterator(); robIt.hasNext(); )
	    {
	    ActionTimer robottimer = new ActionTimer();
	    robottimer.start();

	    Robot robot = robIt.next();
	    Configuration home = robot.getHomeConfiguration();

	    // Initialize robot
	    robot.start();
	    robot.jumpToConfiguration(home);

	    // Add a listener to the robot for finding the list of
	    // collisions for every path
	    CollisionListGenerator listener = new CollisionListGenerator(robot, zones);
	    robot.setRobotListener(listener);
			
	    // Try each "path", i.e. unique pair of configurations
	    List<Configuration> configurations = robot.getConfigurations();
	    // Now the paths, two nested loops of configurations...
	    for (int i = 0; i < configurations.size(); i++)
	    {
	    Configuration from = (Configuration) configurations.get(i);
				
	    for (int j = i + 1; j < configurations.size(); j++)
	    {
	    Configuration to = (Configuration) configurations.get(j);
					
	    // Generate collisionList!
	    logger.info("Examining the path from " + from + " to " + to + 
	    " for robot " + robot + " for collisions.");
	    robot.jumpToConfiguration(from);
	    listener.init();
	    cell.runSimulation(robot, from, to);
					
	    // Show the list!
	    List<CollisionData> collisionList = listener.getCollisionList();
	    while (collisionList.size() != 0)
	    {
	    CollisionData data = collisionList.remove(0);
	    logger.fatal("Collision: " + data);
	    }
	    }
	    }
	    // Finalize the robot
	    robot.setRobotListener(null);
	    robot.jumpToConfiguration(home);
	    cell.runSimulation(robot, home, home); // jumpToConfiguration does not work properly!
	    robot.stop();

	    // Report result
	    robottimer.stop();
	    logger.info("Execution completed for robot " + robot + " after " + robottimer + ".");
	    }
	    */
    }

    /**
     * Builds the base structure of the zone automata. A "free"-state
     * and for every robot a "booked"-state.
     */
    private static Automata buildBaseZoneAutomata(List<Volume> zones, List<Robot> robots)
	throws Exception
    {
	Automata automata = new Automata();

	// Loop over zones...
	for (Iterator<Volume> zoneIt = zones.iterator(); zoneIt.hasNext(); )
	{
	    // Add new automaton
	    Automaton aut = new Automaton(zoneIt.next().getName());
	    aut.setType(AutomatonType.Specification);
			
	    // Add two states, Free and Booked
	    State state = new State(FREESTATE_NAME);
	    state.setAccepting(true);
	    state.setInitial(true);
	    aut.addState(state);
	    for (Iterator<Robot> robIt = robots.iterator(); robIt.hasNext(); )
	    {
		state = new State(BOOKEDSTATE_NAME + UNDERSCORE + robIt.next().getName());
		aut.addState(state);							
	    }
			
	    // Add automaton
	    automata.addAutomaton(aut);
	}		

	return automata;
    }

    /**
     * Builds the base structure of the robot automata. One state for
     * every target and every path, with apropriate events connecting
     * them.
     */
    private static Automata buildBaseRobotAutomata(List<Robot> robots)
	throws Exception
    {
	Automata automata = new Automata();
		
	// Iterate over the robots...
	for (Iterator<Robot> robotIt = robots.iterator(); robotIt.hasNext();)
	{
	    Robot robot = robotIt.next();
			
	    ////////////////////////////////////////
	    // ONE AUTOMATON FOR THE ROBOT ITSELF //
	    ////////////////////////////////////////

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
	    List<Configuration> posList = robot.getConfigurations();
	    for (int i=0; i < posList.size(); i++)
	    {
		for (int j=0; j < posList.size(); j++)
		{
		    if (i != j)
		    {
			State state = new State((posList.get(i)).getName() +
						(posList.get(j)).getName());
			aut.addState(state);
			state.setCost(0);
		    }
		}
	    }
	    // Build transitions...
	    for (Iterator<State> stateIt = aut.stateIterator(); stateIt.hasNext();)
	    {
		State fromState = stateIt.next();
		// Initial?
		if (fromState.getName().equals(STARTSTATE_NAME))
		{
		    // Skip the 0:th element here... its assumed to be the home state
		    for (int i=1; i < posList.size(); i++)
		    {
			// Create new arc...
			String name = (posList.get(0)).getName() +
			    (posList.get(i)).getName();
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
			if (fromState.getName().endsWith((posList.get(i)).getName()))
			{
			    // Just to make sure there is no ambiguity
			    if (fromPos != null)
			    {
				throw new Exception("Error in RSCell.java, ambigous configuration names");
			    }
			    fromPos = (posList.get(i)).getName();
							
			    // Create arc for each possible target configuration
			    for (int j=0; j < posList.size(); j++)
			    {
				if (i != j)
				{
				    // Create new arc...
				    String name = (posList.get(i)).getName() +
					(posList.get(j)).getName();
				    State toState = aut.getStateWithName(name);
									
				    // Special treatment if were dealing with the home configuration
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
									
				    // Only once if this was the home configuration (ugly hack... whatever)
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
	    aut.setComment("This automaton is not finished!");
	    // Add automaton
	    automata.addAutomaton(aut);
			
	    //////////////////////////////
	    // ONE AUTOMATON PER TARGET //
	    //////////////////////////////

	    List<Configuration> configurations = robot.getConfigurations();
	    // Skip home configuration (i=1...)
	    for (int i=1; i<configurations.size(); i++)
	    {
		Configuration pos = configurations.get(i);
				
		aut = new Automaton(robot.getName() + UNDERSCORE + pos.getName());
		State notVisited = new State("0");
		notVisited.setInitial(true);
		State visited = new State("1");
		visited.setAccepting(true);
		aut.addState(notVisited);
		aut.addState(visited);
		aut.setType(AutomatonType.Specification);				
				
		aut.setComment("This automaton is not ready generated!");
		automata.addAutomaton(aut);
	    }
	}
		
	return automata;
    }

    public static void beep()
    {
	Toolkit.getDefaultToolkit().beep();
    }
}
