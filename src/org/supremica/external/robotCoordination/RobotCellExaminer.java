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
import org.supremica.external.robotCoordination.RobotStudio.RSRobotCell;
import java.awt.Color;

public class RobotCellExaminer
    extends JDialog
{
	// private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.createLogger(RobotCellExaminer.class);
	private Frame owner = null;
	private JPanel contentPane = null;

	// Robot stuff
	private RobotCell cell;

	// Supremica stuff
	private Automata zoneAutomata;
	private Automata robotAutomata;

	// Demo file
	//final String DEMOSTATION_FILENAME = "C:/temp/RobSuprTestStation/RobSuprTest.stn";
// 	final String DEMOSTATION_FILENAME = "C:/temp/DomStations/DemoSafe.stn";
    String DEMOSTATION_FILENAME = "DemoSafe.stn";

	/**
	 * Dialog for manipulating the simulation environment.
	 */
	public RobotCellExaminer(Frame owner)
	{
		super(owner, "Robot cell examiner", false);
		this.owner = owner;

		if (false && (SupremicaProperties.getFileRSDemoOpenPath() != ""))
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
					boxStrategy(cell);
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
			cell = new RSRobotCell(file);
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
				List<Position> positions = robot.getPositions();
				for (int i = 0; i < positions.size(); i++)
				{
					Position from = (Position) positions.get(i);

					for (int j = i + 1; j < positions.size(); j++)
					{
						Position to = (Position) positions.get(j);

						// Generate span!
						logger.debug("Generating span from " + from + " to " + to + " for " + robot + ".");
						robot.generateSpan(from, to);
					}
				}

				// Finalize
				robot.jumpToPosition(robot.getHomePosition());
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

				// Examine collisions for each "path", i.e. unique pair of positions
				List<Position> positions = robot.getPositions();
				for (int i = 0; i < positions.size(); i++)
				{
					Position from = (Position) positions.get(i);

					for (int j = i + 1; j < positions.size(); j++)
					{
						Position to = (Position) positions.get(j);

						// Examine path for collisions
						logger.info("Examining the motion from " + from + " to " + to + " for " + robot + ".");
						cell.examineCollisions(robot, from, to);
					}
				}

				// Finalize
				robot.jumpToPosition(robot.getHomePosition());
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
	private void boxStrategy(RobotCell cell)
	{
		/*
		// Colors
		final Color RED = Color.RED;
		final Color BLACK = Color.BLACK;
		// Transparency
		final double TRANSPARENCY = 0.25;

		// Hashtable
		// The first boolean says if a robot has already been inside this box at some time
		// The second boolean says if the box has already been examined for the current robot
		final int BITS = 2;
		final int OCCUPIED = 1;
		final int CHECKED = 2;
		//Hashtable<Coordinate, boolean[BITS]> matrix = new Hashtable(1000);
		Hashtable<Coordinate, byte> matrix = new Hashtable(1000);

		// Get the robots
		List<Robot> robots = cell.getRobots();
		
		// List of boxes where collisions may occur
		List<Box> zoneBoxes = new LinkedList<Box>();

		// For every robot...
		for (Iterator<Robot> robIt = robots.iterator(); it.hasNext(); )
		{
			Robot robot = robIt.next();

			// List of boxes that should be examined
			List<Box> boxesToExamine = new LinkedList<Box>();
			// List of the boxes that have already been examined (for resetting the matrix)
			List<Box> boxesExamined = new LinkedList<Box>();
			// List of the boxes on "the surface"
			List<Box> surfaceBoxes = new LinkedList<Box>();

			// Get base coords and build first box
			Coordinate base = robot.getBaseCoordinates();
			Box startBox = cell.createBox(base.x, base.y, base.z, RED, TRANSPARENCY);
			boxesToExamine.add(startBox);

			// Start loop!
			while (boxesToExamine.size() != 0)
			{
				// Get the stats for this box
				Box box = boxesToExamine.remove(0);
				boolean[] stats;
				if (!matrix.containsKey(box))
				{
					stats = new boolean[BITS];
					stats[OCCUPIED] = false;
					stats[CHECKED] = true;
					matrix.put(box, stats);
				}
				else
				{
					stats = matrix.get(box);
					if (stats[CHECKED])
					{
						// Already checked this one...
						continue;
					}					
				}
				boxesExamined.add(box);

				// Inside robot?
				if (robot.collidesWith(box))
				{
					// Remove from simulation environment
					box.delete();

					// Has someone else collided with this one?
					if (stats[OCCUPIED])
					{
						zoneBoxes.add(box);
					}
					else
					{
						stats[OCCUPIED] = true;
					}

					int x = box.getX();
					int y = box.getY();
					int z = box.getZ();

					int newX;
					int newY;
					int newZ;
					Coordinate newCoord;
					Box newBox;

					// Up
					newCoord = new Coordinate(x,y,z+1);
					newBox = cell.createBox(newCoord, RED, TRANSPARENCY);
					boxesToExamine.add(newBox);
					// Down
					newCoord = new Coordinate(x,y,z-1);
					newBox = cell.createBox(newCoord, RED, TRANSPARENCY);
					boxesToExamine.add(newBox);
					// Left
					newCoord = new Coordinate(x,y+1,z);
					newBox = cell.createBox(newCoord, RED, TRANSPARENCY);
					boxesToExamine.add(newBox);
					// Right
					newCoord = new Coordinate(x,y-1,z);
					newBox = cell.createBox(newCoord, RED, TRANSPARENCY);
					boxesToExamine.add(newBox);
					// Forward
					newCoord = new Coordinate(x+1,y,z);
					newBox = cell.createBox(newCoord, RED, TRANSPARENCY);
					boxesToExamine.add(newBox);
					// Back
					newCoord = new Coordinate(x-1,y,z);
					newBox = cell.createBox(newCoord, RED, TRANSPARENCY);
					boxesToExamine.add(newBox);
				}
				else
				{
					// This box is a "surfacebox"
					surfaceBoxes.add(box);
				}
			}

			// Didn't find anything? The base coordinate box was outside?
			if (boxesExamined.size() == 1)
			{
				logger.error("Base coordinate box is not inside robot.");
				return;
			}

			// DO THE SPAN-STUFF HERE!!!
			

			// Clear CHECKED-status from the examined boxes before examinining next robot!
			while (boxesExamined.size() != 0)
			{
				Box box = boxesExamined.remove(0);
				boolean[] stats = matrix.get(box);
				stats[CHECKED] = false;
			}

			// It's over for this robot. Remove the "surfaceboxes"
			while (surfaceBoxes.size() != 0)
			{
				Box box = surfaceBoxes.remove(0);
				matrix.remove(box);
				// box.delete();
			}
		}
		*/
	}
}
