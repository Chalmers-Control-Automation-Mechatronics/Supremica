package org.supremica.external.robotCoordination;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import org.supremica.log.*;
import org.supremica.gui.*;
import org.supremica.automata.*;
import org.supremica.external.robotCoordination.RobotStudioInterface;

public class RobotCellExaminer
	extends JDialog
{
	private static Logger logger = LoggerFactory.createLogger(RobotCellExaminer.class);
	private Frame owner = null;
	private JPanel contentPane = null;

	// Robot stuff
	private RobotCell cell;

	// Supremica stuff
	private Automata zoneAutomata;
	private Automata robotAutomata;

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
				JFileChooser fileOpener = FileDialogs.getRobotCellFileImporter();

				if (fileOpener.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				{
					File file = fileOpener.getSelectedFile();
					String cellName = file.getAbsolutePath();

					if (cellName.endsWith(".stn"))
					{
						openCell(file, RobotSimulatorType.RobotStudio);
					}
					else
					{
						logger.error("The file " + cellName + " is not a robot station or is " +
									 "not a of a type supported by Supremica.");
					}
				}
				else
				{
					return;
				}
			}
		});
		contentPane.add(openButton);

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

		JButton demoButton = new JButton("Run demo (RobotStudio)");
		demoButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Open file if it exists...
				//File file = new File("C:/temp/RobSuprTestStation/RobSuprTest.stn");
				File file = new File("C:/temp/DomStations/DemoSafe.stn");
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
			cell = new RobotStudioInterface.RSRobotCell(file);
		}
		else
		{
			logger.error("Unknown robot simulation environment specified.");
		}

		// Initialize
		init();
	}

	/**
	 * Generates zones
	 */
	private void generateSpans()
	{
		try
		{
			// For each robot, generate spans
			LinkedList robots = cell.getRobots();
			for (Iterator robotIt = robots.iterator(); robotIt.hasNext(); )
			{
				Robot robot = (Robot) robotIt.next();

				// Initalize
				logger.debug("Starting robot " + robot + ".");
				robot.start();

				// Generate span for each "path", i.e. unique pair of positions
				LinkedList positions = robot.getPositions();
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
		try
		{
			// Intersect spans of robots, pairwise
			LinkedList robots = cell.getRobots();

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
		try
		{
			// For each robot
			LinkedList robots = cell.getRobots();
			for (Iterator robotIt = robots.iterator(); robotIt.hasNext(); )
			{
				Robot robot = (Robot) robotIt.next();

				// Initalize
				robot.start();

				// Examine collisions for each "path", i.e. unique pair of positions
				LinkedList positions = robot.getPositions();
				for (int i = 0; i < positions.size(); i++)
				{
					Position from = (Position) positions.get(i);

					for (int j = i + 1; j < positions.size(); j++)
					{
						Position to = (Position) positions.get(j);

						// Examine path for collisions
						logger.debug("Examining collisions from " + from + " to " + to + " for " + robot + ".");
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
}
