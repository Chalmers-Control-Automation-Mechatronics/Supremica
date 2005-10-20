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
import java.awt.Color;

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

	// Colors
	private final Color RED = Color.RED;
	private final Color ORANGE = Color.ORANGE;
	private final Color GREEN = Color.GREEN;
	private final Color BLACK = Color.BLACK;
	// Transparency
	private final double TRANSPARENCY = 0.9;


	// Demo file
	//final String DEMOSTATION_FILENAME = "C:/temp/RobSuprTestStation/RobSuprTest.stn";
	// 	final String DEMOSTATION_FILENAME = "C:/temp/DomStations/DemoSafe.stn";
    String DEMOSTATION_FILENAME = "DemoSafe.stn";

	/**
	 * Dialog for manipulating the simulation environment.
	 */
	public CellExaminer(Frame owner)
	{
		super(owner, "Robot cell examiner", false);
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
					generateSpans();
					intersectSpans();
					generateAutomata();
					examineCollisions();
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
						logger.error("Error when running boxStrategy: " + ex);
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

				// Examine collisions for each "path", i.e. unique pair of configurations
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
	 * Does the box strategy stuff. 
	 */
	private void boxStrategy(Cell cell)
		throws Exception
	{
		// Discretization parameters
		double dx = 0.2;
		double dy = 0.2;
		double dz = 0.2;
		cell.setBoxDimensions(new double[] {dx,dy,dz});

		// Hashtable
		// The first boolean says if a robot has already been inside this box at some time
		// The second boolean says if the box has already been examined for the current robot
		Hashtable<Coordinate, Status> matrix = new Hashtable(1000);

		// Get the robots
		List<Robot> robots = cell.getRobots();
		
		// List of boxes where collisions may occur
		List<Coordinate> zoneBoxes = new LinkedList<Coordinate>();

		// For every robot...
		for (Iterator<Robot> robIt = robots.iterator(); robIt.hasNext(); )
		{
			Robot robot = robIt.next();

			// List of coordinates of boxes that should be examined
 			List<Coordinate> boxesToExamine = new LinkedList<Coordinate>();
			// List of the coordinates of boxes that have already been examined 
			// (for resetting the matrix)
			List<Coordinate> boxesExamined = new LinkedList<Coordinate>();
			// List of the coordinates of boxes on "the surface"
			List<Coordinate> surfaceBoxes = new LinkedList<Coordinate>();

			// Get base coords and build first box
			Coordinate base = robot.getBaseCoordinates();
			boxesToExamine.add(base);

			// Start loop!
			while (boxesToExamine.size() != 0)
			{
			 	// Get the status for this box
				Coordinate coord = boxesToExamine.remove(0);
				//logger.warn("Examining coordinate " + coord);
				
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
						continue;
				}

				// Inside robot?
				Box box = cell.createBox(coord, ORANGE, TRANSPARENCY);
				if (robot.collidesWith(box))
				{
					// Remove from simulation environment
					box.setColor(RED);
					box.delete();

					// Has someone else collided with this one?
					if (status.occupied)
					{
						zoneBoxes.add(coord);
					}
					status.occupied = true;

					int x = coord.getX();
					int y = coord.getY();
					int z = coord.getZ();

					Coordinate newCoord;
					Box newBox;
					Status newStatus;

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
					surfaceBoxes.add(coord);
				}
 				boxesExamined.add(coord);
			}

			// Didn't find anything? The base coordinate box was outside?
			if (boxesExamined.size() == 1)
			{
			 	logger.error("Base coordinate box is not inside robot.");
				return;
			} 
			
			// DO THE SPAN-STUFF HERE!!!
			/*
			Set collisionSet = new Set();
			for (Iterator<Box> boxIt = surfaceBoxes.iterator(); boxIt.hasNext(); )
			{
				collisionSet.add(boxIt.next());
			}

			// Push boxes for each "path", i.e. unique pair of configurations
			List<Configuration> configurations = robot.getConfigurations();
			// The first loop must start with the home configuration, since the 
			// surfaceboxes always should be pushed "outwards"
			Configuration home = robot.getHomeConfiguration();
			int homeIndex = configurations.indexOf(home);
			if (homeIndex != 0)
			{
				Configuration temp = configurations.get(0);
				configurations.set(0, home);
				configurations.set(homeIndex, temp);
			}
			robot.jumpToConfiguration(home);
			// Now the paths, two nested loops of configurations...
			for (int i = 0; i < configurations.size(); i++)
			{
				Configuration from = (Configuration) configurations.get(i);
				
				for (int j = i + 1; j < configurations.size(); j++)
				{
					Configuration to = (Configuration) configurations.get(j);
					
					// Generate span!
					logger.info("Pushing boxes moving from " + from + " to " + to + " for " + robot + ".");
					//robot.pushBoxes(from, to);
				}
			}
			// Finalize
			robot.jumpToConfiguration(home);
			robot.stop();			
			*/
			
			// It's over for this robot. Remove the "surfaceboxes"
			while (surfaceBoxes.size() != 0)
			{
				Coordinate coord = surfaceBoxes.remove(0);
				matrix.remove(coord);
				cell.destroyBox(coord);
			}

			// Clear checked-status from the examined boxes before examining next robot!
			while (boxesExamined.size() != 0)
			{
				Coordinate coord = boxesExamined.remove(0);
				Status status = matrix.get(coord);
				status.checked = false;
			}			
		}
	}

	private void addBoxToExamine(List<Box> boxesToExamine, 
								 Hashtable<Coordinate, Status> matrix,  
								 Coordinate coord)
		throws Exception
	{
		// If the box is already added for this robot, it will have its checked-status
		// set to true!!
		if (!matrix.containsKey(coord) || !matrix.get(coord).checked)
		{
			Box newBox = cell.createBox(coord, ORANGE, TRANSPARENCY);
			//boxesToExamine.add(boxesToExamine.size(), newBox);
			boxesToExamine.add(newBox);
			
			Status status = new Status();
			status.occupied = false;
			status.checked = true;
			matrix.put(coord, status);
			
		}
	}
}

class Status
{
	protected boolean occupied;
	protected boolean checked;
}
