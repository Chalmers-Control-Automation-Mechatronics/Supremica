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

package org.supremica.gui;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;

import org.apache.log4j.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

import att.grappa.*;

public class AutomatonViewer
	extends JFrame
	implements AutomatonListener
{
    private Automaton theAutomaton;
    private Graph theGraph;
    
    private PrintWriter toDotWriter;
    private InputStream fromDotStream;
    
    private JScrollPane currScrollPanel = null;
    private GrappaPanel automatonPanel;
    
    private BorderLayout layout = new BorderLayout();
    private JPanel contentPane;
    private JMenuBar menuBar = new JMenuBar();

    private boolean updateNeeded = false;

    private JCheckBoxMenuItem leftToRightCheckBox = new JCheckBoxMenuItem(
    	"Layout Left to right", WorkbenchProperties.isDotLeftToRight());
    private JCheckBoxMenuItem withCirclesCheckBox = new JCheckBoxMenuItem(
    	"Draw circles", WorkbenchProperties.isDotWithCircles());
    private JCheckBoxMenuItem withLabelsCheckBox = new JCheckBoxMenuItem(
    	"Draw state names", WorkbenchProperties.isDotWithStateLabels());
    private JCheckBoxMenuItem useColorsCheckBox = new JCheckBoxMenuItem(
    	"Draw colors", WorkbenchProperties.isDotUseColors());
    private JCheckBoxMenuItem automaticUpdateCheckBox = new JCheckBoxMenuItem(
    	"Automatic update", WorkbenchProperties.isDotAutomaticUpdate());

    private static Category thisCategory = LogDisplay.createCategory(AutomatonViewer.class.getName());


    private static final double SCALE_RESET = 1.0, SCALE_CHANGE = 1.5, MAX_SCALE = 64.0, MIN_SCALE = 1.0 / 64;
    private double scaleFactor = SCALE_RESET;



    public AutomatonViewer(Automaton theAutomaton)
		throws Exception
	{
		this.theAutomaton = theAutomaton;

		theAutomaton.getListeners().addListener(this);

		setBackground(Color.white);

		contentPane = (JPanel)getContentPane();
		contentPane.setLayout(layout);

		setTitle(theAutomaton.getName());
		setSize(400, 500);

		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
		}
		setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

		addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					setVisible(false);
					dispose();
				}
			});

        setIconImage(Supremica.cornerImage);

		initMenubar();

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
		catch(Exception e)
		{
			thisCategory.error("Error while displaying " + theAutomaton.getName());
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
			catch(Exception e)
			{
				thisCategory.error("Error while displaying " + theAutomaton.getName());
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
		JMenuItem menuZoomIn    = new JMenuItem("Zoom In");
		JMenuItem menuZoomOut   = new JMenuItem("Zoom Out");
		JMenuItem menuZoomReset = new JMenuItem("Reset view");
		menuZoom.add(menuZoomIn);
		menuZoom.add(menuZoomOut);
		menuZoom.add(menuZoomReset);
		

		menuZoomIn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			    scaleFactor *= SCALE_CHANGE;
			    scaleFactor = Math.max(scaleFactor, MIN_SCALE);
			    update();
			}
		    });		       

		menuZoomOut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			    scaleFactor /= SCALE_CHANGE;
			    scaleFactor = Math.min(scaleFactor, MAX_SCALE);
			    update();
			   
			}
		    });	
		
		menuZoomReset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			    if(scaleFactor != SCALE_RESET) {
				scaleFactor = SCALE_RESET;
				update();
			    }
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
				dispose();
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
				updateNeeded = false;
			}
			catch(Exception e)
			{
				thisCategory.error("Error while viewing " + theAutomaton.getName() + "\n");
			}
		}
	}

	public void build()
		throws Exception
	{
		Builder builder = new Builder(this);
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
			throw ex;
		}

		// Send the file to dot
		try
		{
			exporter.serialize(toDotWriter);
		}
		catch (Exception ex)
		{
			thisCategory.error("Exception while serializing automaton", ex);
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
			thisCategory.error("Exception while parsing dot file", ex);
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
			thisCategory.error("Exception while getting dot graph", ex);
			throw ex;
		}

	}

	public void draw()
	{
		//thisCategory.debug("Before creating panel");
		//theGraph.printGraph(System.err);
	    automatonPanel = new GrappaPanel(theGraph);
	    automatonPanel.setScaleToFit(false);
	    automatonPanel.multiplyScaleFactor(scaleFactor);

		//thisCategory.debug("After creating panel");

		JScrollPane scrollPanel = new JScrollPane(automatonPanel);
		JViewport vp = scrollPanel.getViewport();
		vp.setBackground(Color.white);

		//automatonPanel.addGrappaListener(new GrappaAdapter());

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
		// Create the dot process
		Process dotProcess;
		try
		{
			dotProcess = Runtime.getRuntime().exec(
				WorkbenchProperties.getDotExecuteCommand() + " " + arguments);
		}
		catch (IOException ex)
		{
			thisCategory.error("Cannot run dot. Make sure dot is in the path.");
			throw ex;
		}
		OutputStream pOut = dotProcess.getOutputStream();
		BufferedOutputStream pBuffOut = new BufferedOutputStream(pOut);
		toDotWriter = new PrintWriter(pBuffOut);

		fromDotStream = dotProcess.getInputStream();
	}

	public void fileExport_actionPerformed(ActionEvent e)
	{
		String epsString = "eps";
		String mifString = "mif";
		String dotString = "dot";
		// String gifString = "gif";

		Object[] possibleValues = { epsString, mifString, dotString};
		Object selectedValue = JOptionPane.showInputDialog(
			null, "Export as", "Input", JOptionPane.INFORMATION_MESSAGE,
			null, possibleValues, possibleValues[0]);

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
		else
		{
			return;
		}

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
						thisCategory.error("Error while exporting " + currFile.getAbsolutePath() + "\n", ex);
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
	private static final int BUILD = 1;
	private static final int DRAW = 2;
	private int mode = BUILD;

	private static Category thisCategory = LogDisplay.createCategory(Builder.class.getName());

	public Builder(AutomatonViewer theViewer)
	{
		this.theViewer = theViewer;
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
				thisCategory.error("Cannot display the automaton.");
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
}

