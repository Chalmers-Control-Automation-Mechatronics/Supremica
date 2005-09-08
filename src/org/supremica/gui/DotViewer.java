
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
import java.awt.geom.Rectangle2D;

/**
 * Abstract class for the viewer frame. Implemented by the different viewers.
 *
 * @see AutomatonViewer
 * @see AutomataHierarchyViewer
 */
public abstract class DotViewer
	extends JFrame
	implements DotBuilderGraphObserver
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.createLogger(DotViewer.class);
	private Graph theGraph;

	private JScrollPane currScrollPanel = null;
	private GrappaPanel viewerPanel;
	private BorderLayout layout = new BorderLayout();
	private JPanel contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private JToolBar toolBar = new JToolBar();
	private boolean updateNeeded = false;
	protected JCheckBoxMenuItem leftToRightCheckBox = new JCheckBoxMenuItem("Layout left to right", SupremicaProperties.isDotLeftToRight());
	protected JCheckBoxMenuItem withCirclesCheckBox = new JCheckBoxMenuItem("Draw circles", SupremicaProperties.isDotWithCircles());
	protected JCheckBoxMenuItem withLabelsCheckBox = new JCheckBoxMenuItem("Draw state names", SupremicaProperties.isDotWithStateLabels());
	protected JCheckBoxMenuItem withEventLabelsCheckBox = new JCheckBoxMenuItem("Draw event labels", SupremicaProperties.isDotWithEventLabels());
	protected JCheckBoxMenuItem useStateColorsCheckBox = new JCheckBoxMenuItem("Draw state colors", SupremicaProperties.isDotUseStateColors());
	protected JCheckBoxMenuItem useArcColorsCheckBox = new JCheckBoxMenuItem("Draw arc colors", SupremicaProperties.isDotUseArcColors());
	protected JCheckBoxMenuItem automaticUpdateCheckBox = new JCheckBoxMenuItem("Automatic update", SupremicaProperties.isDotAutomaticUpdate());
	private final static double SCALE_RESET = 1.0, SCALE_CHANGE = 1.5,
								MAX_SCALE = 64.0, MIN_SCALE = 1.0 / 64;
	private double scaleFactor = SCALE_RESET;
	DotBuilder builder;
	private String objectName = "";
	private InputStream dotReturnStream;
	private GraphicsToClipboard toClipboard = null;

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

//	public void initialize() {}

/*
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
*/

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

		// File
		JMenu menuEdit = new JMenu();

		menuEdit.setText("Edit");
		menuEdit.setMnemonic(KeyEvent.VK_E);
		menuBar.add(menuEdit);

		if (SupremicaProperties.isWindows())
		{
			// Edit
			JMenuItem menuEditCopy = new JMenuItem();

			menuEditCopy.setText("Copy");
			menuEditCopy.setMnemonic(KeyEvent.VK_C);
			menuEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));

			menuEdit.add(menuEditCopy);

			menuEditCopy.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					copyToClipboard();
				}
			});
		}

		// Layout
		JMenu menuLayout = new JMenu();

		menuLayout.setText("Layout");
		menuLayout.setMnemonic(KeyEvent.VK_L);
		menuBar.add(menuLayout);
		menuLayout.add(leftToRightCheckBox);
		menuLayout.add(withLabelsCheckBox);
		menuLayout.add(withEventLabelsCheckBox);
		menuLayout.add(withCirclesCheckBox);
		menuLayout.add(useStateColorsCheckBox);
		menuLayout.add(useArcColorsCheckBox);
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

		withEventLabelsCheckBox.addActionListener(new ActionListener()
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
		useStateColorsCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
		});
		useArcColorsCheckBox.addActionListener(new ActionListener()
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
		DotBuilder builder = DotBuilder.getDotBuilder(null, this, getSerializer(), "");
		//builder = new DotBuilder(this);

		//builder.start();
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

	public void copyToClipboard()
	{
		if (toClipboard == null)
		{
			toClipboard = GraphicsToClipboard.getInstance();
		}

		Rectangle2D bb = theGraph.resetBoundingBox();

		double minX = bb.getMinX();
		double maxX = bb.getMaxX();
		double minY = bb.getMinY();
		double maxY = bb.getMaxY();

		logger.debug("minX: " + minX + " maxX: " + maxX + " minY: " + minY + " maxY: " + maxY);

		//create a WMF object
		int width = (int)(maxX - minX) + 1;
		int height = (int)(maxY - minY) + 1;

		// Copy a larger area, approx 10 percent, there seems to be
		// a problem with the size of wmf-data
		width += (int)0.1*width;
		height += (int)0.1*height;

		Graphics theGraphics = toClipboard.getGraphics(width, height);
		viewerPanel.paint(theGraphics);

		toClipboard.copyToClipboard();
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


	public void setGraph(Graph theGraph)
	{
		this.theGraph = theGraph;
		draw();
	}

	public abstract AutomataSerializer getSerializer();

	public void fileExport_actionPerformed(ActionEvent e)
	{
		ExportDialog dlg = new ExportDialog(this);

		dlg.show();

		if (dlg.wasCancelled())    // never mind...
		{
			return;
		}

		// Ugly duplication of code here, but a man can only do so much...
		// (the real reason is that there's no (obvious) conversion from Writer to OutputStream)
		if (dlg.toDebugView())
		{
			AutomataSerializer serializer = getSerializer();

			DotBuilder.getDotBuilder(new DotDebugViewer(), null, serializer, dlg.getDotArgument());
			return;
		}
		else
		{

			JFileChooser fileExporter = dlg.getFileExporter();

			// Suggest a reasonable filename based on the name of the automaton...
			fileExporter.setSelectedFile(new File(SupremicaProperties.getFileSavePath() + "/" + objectName + "." + dlg.getSelectedValue()));

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

							DotBuilder.getDotBuilder(new DotFileViewer(currFile), null, serializer, dlg.getDotArgument());
							SupremicaProperties.setFileSavePath(currFile.getParentFile().getAbsolutePath());

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


	class DotDebugViewer
		implements DotBuilderStreamObserver
	{

		public DotDebugViewer()
		{

		}

		public void setInputStream(InputStream theInputStream)
		{
			BufferedInputStream buffInStream = null;
			try
			{
				buffInStream = new BufferedInputStream(theInputStream);
				TextFrame debugview = new TextFrame("Dot debug output");
				Writer writer = debugview.getPrintWriter();
				int currChar = buffInStream.read();

				while (currChar != -1)
				{
					writer.write(currChar);

					currChar = buffInStream.read();

					// Toolkit.getDefaultToolkit().beep();
				}
				if (buffInStream != null)
				{
					buffInStream.close();
				}
			}
			catch (IOException ex)
			{
				logger.error(ex);
			}
			finally
			{
			}

		}
	}

	class DotFileViewer
		implements DotBuilderStreamObserver
	{
		File theFile;

		public DotFileViewer(File theFile)
		{
			this.theFile = theFile;
		}

		public void setInputStream(InputStream theInputStream)
		{
			BufferedInputStream buffInStream = null;
			BufferedOutputStream buffOutStream = null;

			try
			{
				// Send the response to a file
				buffInStream = new BufferedInputStream(theInputStream);
				FileOutputStream fw = new FileOutputStream(theFile);
				buffOutStream = new BufferedOutputStream(fw);
				int currChar = buffInStream.read();

				while (currChar != -1)
				{
					buffOutStream.write(currChar);

					currChar = buffInStream.read();
				}

				if (buffInStream != null)
				{
					buffInStream.close();
				}
				if (buffOutStream != null)
				{
					buffOutStream.close();
				}

			}

			catch (IOException ex)
			{
				logger.error(ex);
			}
			finally
			{
			}
		}
	}


	// This class is almost identical to the same named class in ActionMan
	// Should really merge and fixx, but for now.... Should I bother?
	static class ExportDialog
	{
		private static final String epsString = "eps";
		private static final String mifString = "mif";
		private static final String dotString = "dot";
		private static final String pngString = "png";
		private static final String svgString = "svg";
		private static final Object[] possibleValues = { epsString, mifString,
														 dotString, pngString,
														 svgString };
		private JOptionPane pane = null;
		private JDialog dialog = null;
		private JCheckBox checkbox = null;
		private Object selectedValue = null;
		private String dotArgument = null;
		private JFileChooser fileExporter = null;

		ExportDialog(Frame comp)
		{
			this.pane = new JOptionPane("Export as::", JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,    // icon
										null,    // options
										null);    // initialValue

			pane.setWantsInput(true);
			pane.setSelectionValues(possibleValues);
			pane.setInitialSelectionValue(possibleValues[0]);
			pane.setComponentOrientation(((comp == null)
										  ? JOptionPane.getRootFrame()
										  : comp).getComponentOrientation());
			pane.selectInitialValue();

			this.checkbox = new JCheckBox("Export to debugview");

			pane.add(checkbox);

			dialog = pane.createDialog(comp, "Export");
		}

		public void show()
		{
			dialog.setVisible(true);
			dialog.dispose();

			// Is this the right thing to do? It seems to work, but the manuals...
			if (((Integer) pane.getValue()).intValue() == JOptionPane.CANCEL_OPTION)
			{
				return;
			}

			selectedValue = pane.getInputValue();

			if (selectedValue == epsString)
			{
				fileExporter = FileDialogs.getEPSFileExporter();
				dotArgument = "-Tps";
			}
			else if (selectedValue == mifString)
			{
				fileExporter = FileDialogs.getMIFFileExporter();
				dotArgument = "-Tmif";
			}
			else if (selectedValue == dotString)
			{
				fileExporter = FileDialogs.getDOTFileExporter();
				dotArgument = "";
			}
			else if (selectedValue == pngString)
			{
				fileExporter = FileDialogs.getPNGFileExporter();
				dotArgument = "-Tpng";
			}
			else if (selectedValue == svgString)
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
			return (String) selectedValue;
		}
	}


}
