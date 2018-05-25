//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import att.grappa.Graph;
import att.grappa.GrappaPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.IO.AutomataSerializer;
import org.supremica.gui.texteditor.TextFrame;
import org.supremica.properties.Config;


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
	private static Logger logger = LogManager.getLogger(DotViewer.class);
	private Graph theGraph;

	private JScrollPane currScrollPanel = null;
	private GrappaPanel viewerPanel;
	private final BorderLayout layout = new BorderLayout();
	private final JPanel contentPane;
	private final JMenuBar menuBar = new JMenuBar();
	private final JToolBar toolBar = new JToolBar();
	private boolean updateNeeded = false;
	protected JCheckBoxMenuItem leftToRightCheckBox = new JCheckBoxMenuItem("Layout left to right", Config.DOT_LEFT_TO_RIGHT.isTrue());
	protected JCheckBoxMenuItem withCirclesCheckBox = new JCheckBoxMenuItem("Draw circles", Config.DOT_WITH_CIRCLES.isTrue());
	protected JCheckBoxMenuItem withLabelsCheckBox = new JCheckBoxMenuItem("Draw state names", Config.DOT_WITH_STATE_LABELS.isTrue());
	protected JCheckBoxMenuItem withEventLabelsCheckBox = new JCheckBoxMenuItem("Draw event labels", Config.DOT_WITH_EVENT_LABELS.isTrue());
	protected JCheckBoxMenuItem useStateColorsCheckBox = new JCheckBoxMenuItem("Draw state colors", Config.DOT_USE_STATE_COLORS.isTrue());
	protected JCheckBoxMenuItem useArcColorsCheckBox = new JCheckBoxMenuItem("Draw arc colors", Config.DOT_USE_ARC_COLORS.isTrue());
	protected JCheckBoxMenuItem automaticUpdateCheckBox = new JCheckBoxMenuItem("Automatic update", Config.DOT_AUTOMATIC_UPDATE.isTrue());
	private final static double SCALE_RESET = 1.0, SCALE_CHANGE = 1.5,
								MAX_SCALE = 64.0, MIN_SCALE = 1.0 / 64;
	private double scaleFactor = SCALE_RESET;
	DotBuilder builder;
	private String objectName = "";
	@SuppressWarnings("unused")
	private InputStream dotReturnStream;

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
			@Override
      public void windowClosing(final WindowEvent e)
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

	public void updated(final Object o)
	{
		update();
	}

	public void setObjectName(final String name)
	{
		setTitle(name);

		objectName = name;
	}

	@Override
  public void setVisible(final boolean toVisible)
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
		final JMenu menuFile = new JMenu();

		menuFile.setText("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuFile);

		// File.Export
		final JMenuItem menuFileExport = new JMenuItem();

		menuFileExport.setText("Export...");
		menuFile.add(menuFileExport);

		/* File.Import, see below
		JMenuItem menuFileImport = new JMenuItem();
		menuFileImport.setText("Import *.dot");

		menuFile.add(menuFileImport);
		*/
		menuFile.addSeparator();

		// File.Close
		final JMenuItem menuFileClose = new JMenuItem();

		menuFileClose.setText("Close");
		menuFile.add(menuFileClose);

		// Layout
		final JMenu menuLayout = new JMenu();

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

		final JMenuItem menuLayoutUpdate = new JMenuItem();

		menuLayoutUpdate.setText("Update");
		menuLayout.add(menuLayoutUpdate);
		menuLayout.add(automaticUpdateCheckBox);

		// Zoom
		final JMenu menuZoom = new JMenu();

		menuZoom.setText("Zoom");
		menuLayout.setMnemonic(KeyEvent.VK_Z);
		menuBar.add(menuZoom);

		final JMenuItem menuZoomIn = new JMenuItem("Zoom In");
		final JMenuItem menuZoomOut = new JMenuItem("Zoom Out");
		final JMenuItem menuZoomReset = new JMenuItem("Reset view");

		menuZoom.add(menuZoomIn);
		menuZoom.add(menuZoomOut);
		menuZoom.add(menuZoomReset);
		menuZoomIn.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				zoomin_actionPerformed(e);
			}
		});
		menuZoomOut.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				zoomout_actionPerformed(e);
			}
		});
		menuZoomReset.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				resetzoom_actionPerformed(e);
			}
		});
		menuFileExport.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				fileExport_actionPerformed(e);
			}
		});

		menuFileClose.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				setVisible(false);
				terminateProcesses();

				//dispose();
			}
		});

		leftToRightCheckBox.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
		});
		withLabelsCheckBox.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
		});

		withEventLabelsCheckBox.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
		});

		withCirclesCheckBox.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
		});
		useStateColorsCheckBox.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
		});
		useArcColorsCheckBox.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
		});
		automaticUpdateCheckBox.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				if (automaticUpdateCheckBox.isSelected())
				{
					update();
				}
			}
		});
		menuLayoutUpdate.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				update();
			}
		});
	}

	private void initToolbar()
	{
		toolBar.setRollover(true);
		contentPane.add(toolBar, BorderLayout.NORTH);

		final Insets tmpInsets = new Insets(0, 0, 0, 0);

		// Create buttons
		final JButton exportButton = new JButton();

		exportButton.setToolTipText("Export");

		final ImageIcon export16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Export16.gif"));

		exportButton.setIcon(export16Img);
		exportButton.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				fileExport_actionPerformed(e);
			}
		});
		exportButton.setMargin(tmpInsets);
		toolBar.add(exportButton, "WEST");
		toolBar.addSeparator();

		final JButton zoominButton = new JButton();

		zoominButton.setToolTipText("Zoom In");

		final ImageIcon zoomin16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/ZoomIn16.gif"));

		zoominButton.setIcon(zoomin16Img);
		zoominButton.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				zoomin_actionPerformed(e);
			}
		});
		zoominButton.setMargin(tmpInsets);
		toolBar.add(zoominButton, "WEST");

		final JButton zoomoutButton = new JButton();

		zoomoutButton.setToolTipText("Zoom Out");

		final ImageIcon zoomout16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/ZoomOut16.gif"));

		zoomoutButton.setIcon(zoomout16Img);
		zoomoutButton.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				zoomout_actionPerformed(e);
			}
		});
		zoomoutButton.setMargin(tmpInsets);
		toolBar.add(zoomoutButton, "WEST");

		final JButton resetzoomButton = new JButton();

		resetzoomButton.setToolTipText("Reset Zoom");

		final ImageIcon resetzoom16Img = new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Zoom16.gif"));

		resetzoomButton.setIcon(resetzoom16Img);
		resetzoomButton.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
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
		@SuppressWarnings("unused")
    final
		DotBuilder builder = DotBuilder.getDotBuilder(null, this, getSerializer(), "");
		//builder = new DotBuilder(this);
		//builder.start();
	}

	public void updated(final Object update, final Object original)
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
			catch (final Exception ex)
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
			catch (final Exception ex)
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
		//** MF Note that there is some bug in Grappa (I think)
		//** MF that sometimes throws a NullPointerException here
		//** MF GrappaPanel, line 109: this.graph = subgraph.getGraph();
		//** MF where "subgraph" is theGraph passed from here
		//** MF Things seem to work anyway, though...

		viewerPanel.setScaleToFit(false);
		viewerPanel.multiplyScaleFactor(scaleFactor);

		// logger.debug("After creating panel");
		final JScrollPane scrollPanel = new JScrollPane(viewerPanel);
		final JViewport vp = scrollPanel.getViewport();

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

	public void zoomin_actionPerformed(final ActionEvent e)
	{
		scaleFactor *= SCALE_CHANGE;
		scaleFactor = Math.max(scaleFactor, MIN_SCALE);

		update();
	}

	public void zoomout_actionPerformed(final ActionEvent e)
	{
		scaleFactor /= SCALE_CHANGE;
		scaleFactor = Math.min(scaleFactor, MAX_SCALE);

		update();
	}

	public void resetzoom_actionPerformed(final ActionEvent e)
	{
		if (scaleFactor != SCALE_RESET)
		{
			scaleFactor = SCALE_RESET;

			update();
		}
	}


	@Override
  public void setGraph(final Graph theGraph)
	{
		this.theGraph = theGraph;
		draw();
	}

	public abstract AutomataSerializer getSerializer();

	public void fileExport_actionPerformed(final ActionEvent e)
	{
		final ExportDialog dlg = new ExportDialog(this);

		dlg.show();

		if (dlg.wasCancelled())    // never mind...
		{
			return;
		}

		// Ugly duplication of code here, but a man can only do so much...
		// (the real reason is that there's no (obvious) conversion from Writer to OutputStream)
		if (dlg.toDebugView())
		{
			final AutomataSerializer serializer = getSerializer();

			DotBuilder.getDotBuilder(new DotDebugViewer(), null, serializer, dlg.getDotArgument());
			return;
		}
		else
		{

			final JFileChooser fileExporter = dlg.getFileExporter();

			// Suggest a reasonable filename based on the name of the automaton...
			fileExporter.setSelectedFile(new File(Config.FILE_SAVE_PATH.get() + "/" + objectName + "." + dlg.getSelectedValue()));

			if (fileExporter.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				final File currFile = fileExporter.getSelectedFile();

				if (currFile != null)
				{
					if (!currFile.isDirectory())
					{
						try
						{
							final AutomataSerializer serializer = getSerializer();

							DotBuilder.getDotBuilder(new DotFileViewer(currFile), null, serializer, dlg.getDotArgument());
							Config.FILE_SAVE_PATH.set(currFile.getParentFile().getAbsolutePath());

						}
						catch (final Exception ex)
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

		@Override
    public void setInputStream(final InputStream theInputStream)
		{
			BufferedInputStream buffInStream = null;
			try
			{
				buffInStream = new BufferedInputStream(theInputStream);
				final TextFrame debugview = new TextFrame("Dot debug output");
				final Writer writer = debugview.getPrintWriter();
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
			catch (final IOException ex)
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

		public DotFileViewer(final File theFile)
		{
			this.theFile = theFile;
		}

		@Override
    public void setInputStream(final InputStream theInputStream)
		{
			BufferedInputStream buffInStream = null;
			BufferedOutputStream buffOutStream = null;

			try
			{
				// Send the response to a file
				buffInStream = new BufferedInputStream(theInputStream);
				final FileOutputStream fw = new FileOutputStream(theFile);
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

			catch (final IOException ex)
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

		ExportDialog(final Frame comp)
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
