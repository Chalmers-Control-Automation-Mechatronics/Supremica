
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
package org.supremica.gui;

import org.supremica.automata.algorithms.*;
import org.supremica.log.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import att.grappa.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonListener;
import org.supremica.automata.State;
import org.supremica.automata.IO.*;

public class AutomatonViewer
	extends JFrame
	implements AutomatonListener
{
	private static Logger logger = LoggerFactory.createLogger(AutomatonViewer.class);

	private Automaton theAutomaton;
	private Graph theGraph;
	private PrintWriter toDotWriter;
	private InputStream fromDotStream;
	private JScrollPane currScrollPanel = null;
	private GrappaPanel automatonPanel;
	private BorderLayout layout = new BorderLayout();
	private JPanel contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private JToolBar toolBar = new JToolBar();
	private boolean updateNeeded = false;
	private JCheckBoxMenuItem leftToRightCheckBox = new JCheckBoxMenuItem("Layout Left to right", SupremicaProperties.isDotLeftToRight());
	private JCheckBoxMenuItem withCirclesCheckBox = new JCheckBoxMenuItem("Draw circles", SupremicaProperties.isDotWithCircles());
	private JCheckBoxMenuItem withLabelsCheckBox = new JCheckBoxMenuItem("Draw state names", SupremicaProperties.isDotWithStateLabels());
	private JCheckBoxMenuItem useColorsCheckBox = new JCheckBoxMenuItem("Draw colors", SupremicaProperties.isDotUseColors());
	private JCheckBoxMenuItem automaticUpdateCheckBox = new JCheckBoxMenuItem("Automatic update", SupremicaProperties.isDotAutomaticUpdate());
	private final static double SCALE_RESET = 1.0, SCALE_CHANGE = 1.5, MAX_SCALE = 64.0, MIN_SCALE = 1.0 / 64;
	private double scaleFactor = SCALE_RESET;
	private Process dotProcess;
	private Builder builder;

	public AutomatonViewer(Automaton theAutomaton)
		throws Exception
	{
		this.theAutomaton = theAutomaton;

		theAutomaton.getListeners().addListener(this);
		setBackground(Color.white);

		contentPane = (JPanel) getContentPane();

		contentPane.setLayout(layout);
		setTitle(theAutomaton.getName());
//		setSize(400, 500);
//
//		// Center the window
//		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		Dimension frameSize = getSize();
//
//		if (frameSize.height > screenSize.height)
//		{
//			frameSize.height = screenSize.height;
//		}
//
//		if (frameSize.width > screenSize.width)
//		{
//			frameSize.width = screenSize.width;
//		}
//
//		setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
//		setIconImage(Supremica.cornerImage);

		Utility.setupFrame(this, 400, 500);

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				setVisible(false);
				terminateProcesses();
				//dispose();
			}
		});
		initMenubar();
		initToolbar();

		updateNeeded = true;
	}

	public void initialize() {}

	public void run()
	{
		setVisible(true);

		try
		{
			build();
		}
		catch (Exception ex)
		{
			logger.error("Error while displaying " + theAutomaton.getName(), ex);
			logger.debug(ex.getStackTrace());
		}
	}

	public void updated(Object o)
	{
		if (o == theAutomaton)
		{
			try
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
			catch (Exception ex)
			{
				logger.error("Error while displaying " + theAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	public void stateAdded(Automaton aut, State q)
	{
		updated(aut);
	}

	public void stateRemoved(Automaton aut, State q)
	{
		updated(aut);
	}

	public void arcAdded(Automaton aut, Arc a)
	{
		updated(aut);
	}

	public void arcRemoved(Automaton aut, Arc a)
	{
		updated(aut);
	}

	public void attributeChanged(Automaton aut)
	{
		updated(aut);
	}

	public void automatonRenamed(Automaton aut, String oldName)
	{
		updated(aut);
	}

	public void setVisible(boolean toVisible)
	{
		super.setVisible(toVisible);

		if (updateNeeded)
		{
			update();
		}
	}

	private void initMenubar()
	{
		setJMenuBar(menuBar);

		// File
		JMenu menuFile = new JMenu();

		menuFile.setText("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuFile);

		// File.Export
		JMenuItem menuFileExport = new JMenuItem();

		menuFileExport.setText("Export...");
		menuFile.add(menuFileExport);
		menuFile.addSeparator();

		// File.Close
		JMenuItem menuFileClose = new JMenuItem();

		menuFileClose.setText("Close");
		menuFile.add(menuFileClose);

		// Layout
		JMenu menuLayout = new JMenu();

		menuLayout.setText("Layout");
		menuLayout.setMnemonic(KeyEvent.VK_L);
		menuBar.add(menuLayout);
		menuLayout.add(leftToRightCheckBox);
		menuLayout.add(withLabelsCheckBox);
		menuLayout.add(withCirclesCheckBox);
		menuLayout.add(useColorsCheckBox);
		menuLayout.addSeparator();

		JMenuItem menuLayoutUpdate = new JMenuItem();

		menuLayoutUpdate.setText("Update");
		menuLayout.add(menuLayoutUpdate);
		menuLayout.add(automaticUpdateCheckBox);

		// Zoom
		JMenu menuZoom = new JMenu();

		menuZoom.setText("Zoom");
		menuLayout.setMnemonic(KeyEvent.VK_Z);
		menuBar.add(menuZoom);

		JMenuItem menuZoomIn = new JMenuItem("Zoom In");
		JMenuItem menuZoomOut = new JMenuItem("Zoom Out");
		JMenuItem menuZoomReset = new JMenuItem("Reset view");

		menuZoom.add(menuZoomIn);
		menuZoom.add(menuZoomOut);
		menuZoom.add(menuZoomReset);
		menuZoomIn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				zoomin_actionPerformed(e);
			}
		});
		menuZoomOut.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				zoomout_actionPerformed(e);
			}
		});
		menuZoomReset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				resetzoom_actionPerformed(e);
			}
		});
		menuFileExport.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fileExport_actionPerformed(e);
			}
		});
		menuFileClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
				terminateProcesses();
				//dispose();
			}
		});
		leftToRightCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
		});
		withLabelsCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
		});
		withCirclesCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
		});
		useColorsCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
		});
		menuLayoutUpdate.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				update();
			}
		});
	}

	private void initToolbar()
	{
		toolBar.setRollover(true);
		contentPane.add(toolBar, BorderLayout.NORTH);

		Insets tmpInsets = new Insets(0, 0, 0, 0);

		// Create buttons
		JButton exportButton = new JButton();

		exportButton.setToolTipText("Export");

		ImageIcon export16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Export16.gif"));

		exportButton.setIcon(export16Img);
		exportButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fileExport_actionPerformed(e);
			}
		});
		exportButton.setMargin(tmpInsets);
		toolBar.add(exportButton, "WEST");

		toolBar.addSeparator();

		JButton zoominButton = new JButton();

		zoominButton.setToolTipText("Zoom In");

		ImageIcon zoomin16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/ZoomIn16.gif"));

		zoominButton.setIcon(zoomin16Img);
		zoominButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				zoomin_actionPerformed(e);
			}
		});
		zoominButton.setMargin(tmpInsets);
		toolBar.add(zoominButton, "WEST");

		JButton zoomoutButton = new JButton();

		zoomoutButton.setToolTipText("Zoom Out");

		ImageIcon zoomout16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/ZoomOut16.gif"));

		zoomoutButton.setIcon(zoomout16Img);
		zoomoutButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				zoomout_actionPerformed(e);
			}
		});
		zoomoutButton.setMargin(tmpInsets);
		toolBar.add(zoomoutButton, "WEST");

		JButton resetzoomButton = new JButton();

		resetzoomButton.setToolTipText("Reset Zoom");

		ImageIcon resetzoom16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Zoom16.gif"));

		resetzoomButton.setIcon(resetzoom16Img);
		resetzoomButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				resetzoom_actionPerformed(e);
			}
		});
		resetzoomButton.setMargin(tmpInsets);
		toolBar.add(resetzoomButton, "WEST");
	}

	public void update()
	{
		if (!isVisible())
		{
			updateNeeded = true;
		}
		else
		{
			try
			{
				build();
				setTitle(theAutomaton.getName());

				updateNeeded = false;
			}
			catch (Exception ex)
			{
				logger.error("Error while viewing " + theAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	public void build()
		throws Exception
	{
		builder = new Builder(this);

		builder.start();
	}

	public void internalBuild()
		throws Exception
	{
		AutomatonToDot exporter = new AutomatonToDot(theAutomaton);

		exporter.setLeftToRight(leftToRightCheckBox.isSelected());
		exporter.setWithLabels(withLabelsCheckBox.isSelected());
		exporter.setWithCircles(withCirclesCheckBox.isSelected());
		exporter.setUseColors(useColorsCheckBox.isSelected());

		try
		{
			initializeStreams("");
		}
		catch (Exception ex)
		{
			toDotWriter.close();
			logger.debug(ex.getStackTrace());
			throw ex;
		}

		// Send the file to dot
		try
		{
			exporter.serialize(toDotWriter);
		}
		catch (Exception ex)
		{
			logger.error("Exception while serializing automaton", ex);
			logger.debug(ex.getStackTrace());
			return;
		}
		finally
		{
			toDotWriter.close();
		}

		// Parse the response from dot
		Parser parser = new Parser(fromDotStream);

		try
		{
			parser.parse();
		}
		catch (Exception ex)
		{
			logger.error("Exception while parsing dot file", ex);
			logger.debug(ex.getStackTrace());
			throw ex;
		}
		finally
		{
			fromDotStream.close();
		}


		try
		{
			theGraph = parser.getGraph();
		}
		catch (Exception ex)
		{
			logger.error("Exception while getting dot graph", ex);
			logger.debug(ex.getStackTrace());
			throw ex;
		}
	}

	public void stopProcess()
	{
		if (dotProcess != null)
		{
			dotProcess.destroy();
			updateNeeded = true;
		}
	}

	public void terminateProcesses()
	{
		if (builder != null)
		{
			builder.stopProcess();
		}
	}

	public void draw()
	{

		// logger.debug("Before creating panel");
		// theGraph.printGraph(System.err);
		automatonPanel = new GrappaPanel(theGraph);

		automatonPanel.setScaleToFit(false);
		automatonPanel.multiplyScaleFactor(scaleFactor);

		// logger.debug("After creating panel");
		JScrollPane scrollPanel = new JScrollPane(automatonPanel);
		JViewport vp = scrollPanel.getViewport();

		vp.setBackground(Color.white);

		// automatonPanel.addGrappaListener(new GrappaAdapter());
		if (currScrollPanel != null)
		{
			contentPane.remove(currScrollPanel);
		}

		contentPane.add(scrollPanel, BorderLayout.CENTER);
		contentPane.revalidate();

		currScrollPanel = scrollPanel;
	}

	private void initializeStreams(String arguments)
		throws Exception
	{
		try
		{
			dotProcess = Runtime.getRuntime().exec(SupremicaProperties.getDotExecuteCommand() + " " + arguments);
		}
		catch (IOException ex)
		{
			logger.error("Cannot run dot. Make sure dot is in the path.");
			logger.debug(ex.getStackTrace());
			throw ex;
		}

		OutputStream pOut = dotProcess.getOutputStream();
		BufferedOutputStream pBuffOut = new BufferedOutputStream(pOut);

		toDotWriter = new PrintWriter(pBuffOut);
		fromDotStream = dotProcess.getInputStream();
	}

	public void zoomin_actionPerformed(ActionEvent e)
	{
		scaleFactor *= SCALE_CHANGE;
		scaleFactor = Math.max(scaleFactor, MIN_SCALE);

		update();
	}

	public void zoomout_actionPerformed(ActionEvent e)
	{
		scaleFactor /= SCALE_CHANGE;
		scaleFactor = Math.min(scaleFactor, MAX_SCALE);

		update();
	}

	public void resetzoom_actionPerformed(ActionEvent e)
	{
		if (scaleFactor != SCALE_RESET)
		{
			scaleFactor = SCALE_RESET;

			update();
		}
	}

	public void fileExport_actionPerformed(ActionEvent e)
	{
		String epsString = "eps";
		String mifString = "mif";
		String dotString = "dot";
		String pngString = "png";
		String svgString = "svg";


		// String gifString = "gif";
		Object[] possibleValues = { epsString, mifString, pngString, svgString, dotString };
		Object selectedValue = JOptionPane.showInputDialog(null, "Export as", "Export", JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);

		if (selectedValue == null)
		{
			return;
		}

		int exportMode = -1;

		if (selectedValue == epsString)
		{
			exportMode = 1;
		}
		else if (selectedValue == mifString)
		{
			exportMode = 2;
		}
		else if (selectedValue == dotString)
		{
			exportMode = 3;
		}
		else if (selectedValue == pngString)
		{
			exportMode = 4;
		}
		else if (selectedValue == svgString)
		{
			exportMode = 5;
		}

		JFileChooser fileExporter = null;
		String dotArgument = null;

		if (exportMode == 1)
		{
			fileExporter = FileDialogs.getEPSFileExporter();
			dotArgument = "-Tps";
		}
		else if (exportMode == 2)
		{
			fileExporter = FileDialogs.getMIFFileExporter();
			dotArgument = "-Tmif";
		}
		else if (exportMode == 3)
		{
			fileExporter = FileDialogs.getDOTFileExporter();
			dotArgument = "";
		}
		else if (exportMode == 4)
		{
			fileExporter = FileDialogs.getPNGFileExporter();
			dotArgument = "-Tpng";
		}
		else if (exportMode == 5)
		{
			fileExporter = FileDialogs.getSVGFileExporter();
			dotArgument = "-Tsvg";
		}
		else
		{
			return;
		}

		// Suggest a reasonable filename based on the name of the automaton...
		fileExporter.setSelectedFile(new File(SupremicaProperties.getFileSavePath() + "/" +  theAutomaton.getName() + "." + selectedValue));

		if (fileExporter.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileExporter.getSelectedFile();

			if (currFile != null)
			{
				if (!currFile.isDirectory())
				{
					try
					{
						AutomatonToDot exporter = new AutomatonToDot(theAutomaton);

						exporter.setLeftToRight(leftToRightCheckBox.isSelected());
						exporter.setWithLabels(withLabelsCheckBox.isSelected());
						exporter.setUseColors(useColorsCheckBox.isSelected());
						initializeStreams(dotArgument);

						// Send the file to dot
						exporter.serialize(toDotWriter);
						toDotWriter.close();

						// Send the response to a file
						FileOutputStream fw = new FileOutputStream(currFile);
						BufferedOutputStream buffOutStream = new BufferedOutputStream(fw);
						BufferedInputStream buffInStream = new BufferedInputStream(fromDotStream);
						int currChar = buffInStream.read();

						while (currChar != -1)
						{
							buffOutStream.write(currChar);

							currChar = buffInStream.read();
						}

						buffInStream.close();
						buffOutStream.close();
					}
					catch (Exception ex)
					{
						logger.error("Error while exporting " + currFile.getAbsolutePath() + "\n", ex);
						logger.debug(ex.getStackTrace());
						return;
					}
				}
			}
		}
	}
}

class Builder
	extends Thread
{
	private AutomatonViewer theViewer = null;
	private final static int BUILD = 1;
	private final static int DRAW = 2;
	private int mode = BUILD;
	private static Logger logger = LoggerFactory.createLogger(Builder.class);

	public Builder(AutomatonViewer theViewer)
	{
		this.theViewer = theViewer;
		setPriority(Thread.MIN_PRIORITY);
	}

	public void run()
	{
		if (mode == BUILD)
		{
			try
			{
				theViewer.internalBuild();
			}
			catch (Exception ex)
			{
				logger.error("Cannot display the automaton.");
				logger.debug(ex.getStackTrace());
				return;
			}

			mode = DRAW;

			java.awt.EventQueue.invokeLater(this);
		}
		else if (mode == DRAW)
		{
			theViewer.draw();
		}
	}

	public void stopProcess()
	{
		if (theViewer != null)
		{
			theViewer.stopProcess();
		}
	}
}
