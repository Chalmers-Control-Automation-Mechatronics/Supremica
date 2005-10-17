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
import org.supremica.external.robotCoordination.robotstudioApp.RSRobotCell;

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
	// final String DEMOSTATION_FILENAME = "C:/temp/RobSuprTestStation/RobSuprTest.stn";
	final String DEMOSTATION_FILENAME = "C:\\jobb\\RobotStudio-files\\DomStations\\DemoSafe.stn";

	/**
	 * Dialog for manipulating the simulation environment.
	 */
	public RobotCellExaminer(Frame owner)
	{
		super(owner, "Robot cell examiner", false);
		this.owner = owner;

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
					String cellName = file.getAbsolutePath();

					// Choose simulator based on file type...
					if (cellName.endsWith(".stn"))
					{
						openCell(file, RobotSimulatorType.RobotStudio);
					}
					else
					{
						logger.error("The file " + cellName + " is not a robot station or is " +
									 "not a of a type supported by Supremica.");
						return;
					}
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

		// Demo button (only if demo file exists)
		File file = new File(DEMOSTATION_FILENAME);
		if (file.exists())
		{
			JButton demoButton = new JButton("Run demo (RobotStudio)");
			demoButton.addActionListener(new ActionListener()
			{
					public void actionPerformed(ActionEvent e)
					{
						// Open file if it exists...
						File file = new File(DEMOSTATION_FILENAME);
						if (file.exists())
						{
							openCell(file, RobotSimulatorType.RobotStudio);
							generateSpans();
							intersectSpans();
							generateAutomata();
							examineCollisions();
						}
						else
						{
							logger.error("File " + file + " does not exist.");
						}
					}
			});
			contentPane.add(demoButton);
		}

		// Box strategy demo button 
		JButton demoButton = new JButton("Box strategy demo");
		demoButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Is there an open file?
					if (cell == null || !cell.isOpen())
					{
						// Open file if it exists...
						File file = new File(DEMOSTATION_FILENAME);
						if (file.exists())
						{
							openCell(file, RobotSimulatorType.RobotStudio);
							
						}
						else
						{
							logger.error("File " + file + " does not exist.");
							return;
						}						
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
	private void openCell(File file, RobotSimulatorType simType)
	{
		// Which simulation software is used?
		// This is the only "application specific" part of this class!
		if (simType == RobotSimulatorType.RobotStudio)
		{
			// Here, it would be a good thing to examine if RobotStudio is
			// properly installed...

			// Open cell
			cell = new RSRobotCell(file);
		}
		else
		{
			logger.error("Unknown robot simulation environment specified.");
		}

		// Set the project name based on the file name...
		ActionMan.getGui().getVisualProjectContainer().getActiveProject().setName(file.getName());

		// Initialize
		init();
	}

	/**
	 * Generates zones
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
		List<Robot> robots = cell.getRobots();
		
		// For every robot...
		for (Iterator<Robot> robIt = robots.iterator(); it.hasNext(); )
		{
			Robot robot = robIt.next();

			// List of boxes that should be examined
			List<Box> boxesToExamine = new LinkedList<Box>();
			// List of the boxes that the robot is occupying
			List<Box> boxesOccupied = new LinkedList<Box>();

			// Get base coords and build first box
			Coordinate base = robot.getBaseCoordinates();
			Box box = cell.createBox(base);

			// Start loop!
			while (boxesToExamine.size() != 0)
			{
				Box box = boxesToExamine.remove(0);
			}
		}

			// Special box inside the robot
			if (Robot.collidesWith(box))
			{
				boxesToExamine.add(box);
			}
			else
			{
				logger.error("Base coordinate box is not inside robot.");
				return;
			}
		*/
	}
}
