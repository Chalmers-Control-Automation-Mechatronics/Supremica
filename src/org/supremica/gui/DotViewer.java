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

//import org.supremica.automata.algorithms.*;
import org.supremica.log.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import att.grappa.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.IO.*;
import org.supremica.gui.texteditor.TextFrame;

public class DotViewer
	extends JFrame
{
	private static Logger logger = LoggerFactory.createLogger(DotViewer.class);

	private Graph theGraph;
	private PrintWriter toDotWriter;
	private InputStream fromDotStream;
	private JScrollPane currScrollPanel = null;
	private GrappaPanel viewerPanel;
	private BorderLayout layout = new BorderLayout();
	private JPanel contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private JToolBar toolBar = new JToolBar();
	private boolean updateNeeded = false;
	protected JCheckBoxMenuItem leftToRightCheckBox = new JCheckBoxMenuItem("Layout Left to right", SupremicaProperties.isDotLeftToRight());
	protected JCheckBoxMenuItem withCirclesCheckBox = new JCheckBoxMenuItem("Draw circles", SupremicaProperties.isDotWithCircles());
	protected JCheckBoxMenuItem withLabelsCheckBox = new JCheckBoxMenuItem("Draw state names", SupremicaProperties.isDotWithStateLabels());
	protected JCheckBoxMenuItem useColorsCheckBox = new JCheckBoxMenuItem("Draw colors", SupremicaProperties.isDotUseColors());
	protected JCheckBoxMenuItem automaticUpdateCheckBox = new JCheckBoxMenuItem("Automatic update", SupremicaProperties.isDotAutomaticUpdate());
	private final static double SCALE_RESET = 1.0, SCALE_CHANGE = 1.5, MAX_SCALE = 64.0, MIN_SCALE = 1.0 / 64;
	private double scaleFactor = SCALE_RESET;
	private Process dotProcess;
	private Builder builder;

	private String objectName = "";

	public DotViewer()
		throws Exception
	{
		setBackground(Color.white);
		contentPane = (JPanel) getContentPane();
		contentPane.setLayout(layout);

		// Set size and center frame, all in one method!
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

	public void initialize()
	{
	}

	public void run()
	{
		setVisible(true);

		try
		{
			build();
		}
		catch (Exception ex)
		{
			logger.error("Error while displaying " + objectName, ex);
			logger.debug(ex.getStackTrace());
		}
	}

	public void updated(Object o)
	{
		update();
	}

	public void setObjectName(String name)
	{
		setTitle(name);
		objectName = name;
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

		/* File.Import, see below
		JMenuItem menuFileImport = new JMenuItem();
		menuFileImport.setText("Import *.dot");

		menuFile.add(menuFileImport);
		*/
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
		/* see below
		menuFileImport.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fileImport_actionPerformed(e);
			}
		});
		*/
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
		automaticUpdateCheckBox.addActionListener(new ActionListener()
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

	public void build()
		throws Exception
	{
		builder = new Builder(this);

		builder.start();
	}

	public void updated(Object update, Object original)
	{
		//System.err.println("updated");
		if (update == original)
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
				logger.error("Error while displaying " + objectName, ex);
				logger.debug(ex.getStackTrace());
			}
		}
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
				setTitle(objectName);

				updateNeeded = false;
			}
			catch (Exception ex)
			{
				logger.error("Error while viewing " + objectName, ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	public void internalBuild()
		throws Exception
	{
		AutomataSerializer serializer = getSerializer();

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
			serializer.serialize(toDotWriter);
		}
		catch (Exception ex)
		{
			logger.error("Exception while serializing ", ex);
			logger.debug(ex.getStackTrace());
			return;
		}
		finally
		{
			toDotWriter.close();
		}

		parseResponse(fromDotStream);
	}

	private void parseResponse(InputStream inputStream)
		throws Exception
	{
		// Parse the response from dot
		Parser parser = new Parser(inputStream);

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
			inputStream.close();
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
		viewerPanel = new GrappaPanel(theGraph);

		viewerPanel.setScaleToFit(false);
		viewerPanel.multiplyScaleFactor(scaleFactor);

		// logger.debug("After creating panel");
		JScrollPane scrollPanel = new JScrollPane(viewerPanel);
		JViewport vp = scrollPanel.getViewport();

		vp.setBackground(Color.white);

		// viewerPanel.addGrappaListener(new GrappaAdapter());
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

	// This class is almost identical to the same named class in ActionMan
	// Should really merge and fixx, but for now.... Should I bother?
	static class ExportDialog
	//	extends JDialog
	{
		private final String epsString = "eps";
		private final String mifString = "mif";
		private final String dotString = "dot";
		private final String pngString = "png";
		private final String svgString = "svg";

		private final Object[] possibleValues =
		{
			epsString, mifString, dotString, pngString, svgString
		};

		private JOptionPane pane = null;
		private JDialog dialog = null;
		private JCheckBox checkbox = null;
		private Object selectedValue = null;

		private String dotArgument = null;
		private JFileChooser fileExporter = null;

		ExportDialog(Frame comp)
		{
			this.pane = new JOptionPane("Export as::",
										JOptionPane.INFORMATION_MESSAGE,
										JOptionPane.OK_CANCEL_OPTION,
										null,	// icon
										null, 	// options
										null);	// initialValue

			pane.setWantsInput(true);
			pane.setSelectionValues(possibleValues);
			pane.setInitialSelectionValue(possibleValues[0]);
			pane.setComponentOrientation(((comp == null) ? JOptionPane.getRootFrame() : comp).getComponentOrientation());
			pane.selectInitialValue();

			this.checkbox = new JCheckBox("Export to debugview");
			pane.add(checkbox);

			// int style = styleFromMessageType(JOptionPane.INFORMATION_MESSAGE);
			dialog = pane.createDialog(comp, "Export");
		}

		public void show()
		{
			dialog.show();
	        dialog.dispose();

			// Is this the right thing to do? It seems to work, but the manuals...
	        if(((Integer)pane.getValue()).intValue() == JOptionPane.CANCEL_OPTION)
	        {
	        	return;
	        }

	        selectedValue = pane.getInputValue();

			if(selectedValue == epsString)
			{
				fileExporter = FileDialogs.getEPSFileExporter();
				dotArgument = "-Tps";
			}
			else if(selectedValue == mifString)
			{
				fileExporter = FileDialogs.getMIFFileExporter();
				dotArgument = "-Tmif";
			}
			else if(selectedValue == dotString)
			{
				fileExporter = FileDialogs.getDOTFileExporter();
				dotArgument = "";
			}
			else if(selectedValue == pngString)
			{
				fileExporter = FileDialogs.getPNGFileExporter();
				dotArgument = "-Tpng";
			}
			else if(selectedValue == svgString)
			{
				fileExporter = FileDialogs.getSVGFileExporter();
				dotArgument = "-Tsvg";
			}
			else
			{
				fileExporter = null;
				dotArgument = null;
			}
			// System.out.println("selectedValue == " + selectedValue.toString());
		}

		public boolean wasCancelled()
		{
			return selectedValue == null;
		}
		public boolean toDebugView()
		{
			return checkbox.isSelected();
		}
		public String getDotArgument()
		{
			return dotArgument;
		}
		public JFileChooser getFileExporter()
		{
			return fileExporter;
		}
		public String getSelectedValue()
		{
			return (String)selectedValue;
		}
	}

	// MF Why isn't this one purely abstract?
	public AutomataSerializer getSerializer()
	{
		return null;
	}

	public void fileExport_actionPerformed(ActionEvent e)
	{
		ExportDialog dlg = new ExportDialog(this);
		dlg.show();
		if(dlg.wasCancelled())	// never mind...
		{
			return;
		}

		// Ugly duplication of code here, but a man can only do so much...
		// (the real reason is that there's no (obvious) conversion from Writer to OutputStream)
		if(dlg.toDebugView())
		{
			try
			{
				AutomataSerializer serializer = getSerializer();
				initializeStreams(dlg.getDotArgument());

				// Send the file to dot
				serializer.serialize(toDotWriter);
				toDotWriter.close();

				// Send the response to the debugview
				BufferedInputStream buffInStream = new BufferedInputStream(fromDotStream);
				TextFrame debugview = new TextFrame("Dot debug output");
				Writer writer = debugview.getPrintWriter();
				int currChar = buffInStream.read();

				while (currChar != -1)
				{
					writer.write(currChar);

					currChar = buffInStream.read();
					// Toolkit.getDefaultToolkit().beep();
				}

				buffInStream.close();
			}
			catch(Exception excp)
			{
				logger.error("Error while writing to the debugview", excp);
				logger.debug(excp.getStackTrace());
			}
			return;
		}

		JFileChooser fileExporter = dlg.getFileExporter();
		// Suggest a reasonable filename based on the name of the automaton...
		fileExporter.setSelectedFile(new File(SupremicaProperties.getFileSavePath() + "/" +  objectName + "." + dlg.getSelectedValue()));

		if (fileExporter.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileExporter.getSelectedFile();

			if (currFile != null)
			{
				if (!currFile.isDirectory())
				{
					try
					{
						AutomataSerializer serializer = getSerializer();

						initializeStreams(dlg.getDotArgument());

						// Send the file to dot
						serializer.serialize(toDotWriter);
						toDotWriter.close();

						// Send the response to a file
						BufferedInputStream buffInStream = new BufferedInputStream(fromDotStream);
						FileOutputStream fw = new FileOutputStream(currFile);
						BufferedOutputStream buffOutStream = new BufferedOutputStream(fw);

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

	/* I wanted a way to load manually altered dot-files and to then export them
	 * This is not the way to do it.
	public void fileImport_actionPerformed(ActionEvent e)
	{
		// Open an inputstream to the file and have it parsed as an ordinary dot response
		try
		{
			FileInputStream fistream = new FileInputStream("D:\\Temp\\User1.dot");
			parseResponse(fistream);
		}
		catch(FileNotFoundException excp)
		{
			logger.error("File not found");
		}
		catch(Exception excp)
		{
			logger.error("Exception parsing the file");
			logger.debug(excp.getStackTrace());
		}
		draw();
	}
	*/
}

class Builder
	extends Thread
{
	private DotViewer theViewer = null;
	private final static int BUILD = 1;
	private final static int DRAW = 2;
	private int mode = BUILD;
	private static Logger logger = LoggerFactory.createLogger(Builder.class);

	public Builder(DotViewer theViewer)
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
				logger.error("Cannot display object.");
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
