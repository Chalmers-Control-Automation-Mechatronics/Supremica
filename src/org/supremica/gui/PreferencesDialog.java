
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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.util.BDD.Options;    // Arash

public class PreferencesDialog
	extends JDialog
{
	private JPanel contentPane = null;
	private JTabbedPane theTabbedPanel = null;
	private FilePanel theFilePanel = null;
	private CommunicationPanel theCommunicationPanel = null;
	private SimulationPanel theSimulationPanel = null;
	private RobotCoordinationPanel theRobotCoordinationPanel = null;
	/* yes, it has package access! */
	BDDPanel1 theBDDPanel1 = null;
	private BDDPanel2 theBDDPanel2 = null;
	private LayoutPanel theLayoutPanel = null;
	private SynchronizationPropertiesPanel theSynchronizationPanel = null;
	private SoftPLCPanel theSoftPLCPanel = null;
	private PreferencesControllerPanel theControllerPanel = null;
	private Frame owner;

	public PreferencesDialog(Frame owner)
	{
		super(owner, "Preferences", true);

		this.owner = owner;
		contentPane = (JPanel) getContentPane();
		theTabbedPanel = new JTabbedPane();

		contentPane.add(theTabbedPanel, BorderLayout.CENTER);

		theSynchronizationPanel = new SynchronizationPropertiesPanel(this);

		theTabbedPanel.add("Synchronization", theSynchronizationPanel);

		theLayoutPanel = new LayoutPanel(this);

		theTabbedPanel.add("Layout", theLayoutPanel);

		theCommunicationPanel = new CommunicationPanel(this);

		theTabbedPanel.add("Communication", theCommunicationPanel);

		theSoftPLCPanel = new SoftPLCPanel(this);

		theTabbedPanel.add("SoftPLC", theSoftPLCPanel);

		theBDDPanel1 = new BDDPanel1(this);

		theTabbedPanel.add("BDD 1", theBDDPanel1);

		theBDDPanel2 = new BDDPanel2(this);

		theTabbedPanel.add("BDD 2", theBDDPanel2);

		theSimulationPanel = new SimulationPanel(this);

		theTabbedPanel.add("Simulation", theSimulationPanel);

		/*
		theRobotCoordinationPanel = new RobotCoordinationPanel(this);
		theTabbedPanel.add("Robot coordination", theRobotCoordinationPanel);
		*/
		if (SupremicaProperties.fileAllowOpen() || SupremicaProperties.fileAllowSave())
		{
			theFilePanel = new FilePanel(this);

			theTabbedPanel.add("File", theFilePanel);
		}

		theControllerPanel = new PreferencesControllerPanel(this);

		contentPane.add(theControllerPanel, BorderLayout.SOUTH);

		// setSize(400, 350);
		pack();

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
				doCancel();
			}
		});
	}

	public Frame getOwnerFrame()
	{
		return owner;
	}

	public void doCancel()
	{
		setVisible(false);

		//dispose();
	}

	public void doApply()
	{
		if (setAttributes())
		{
			try
			{
				SupremicaProperties.savePropperties();    // write back the changes to the config file too!!
			}
			catch (IOException exx)
			{
				System.err.println("Failed to save changed to config-file: " + exx.getMessage());
			}

			doCancel();    // ok, not really cancel. what we do is to close the dialog
		}
	}

	public void setVisible(boolean toVisible)
	{
		if (toVisible)
		{
			getAttributes();
		}

		super.setVisible(toVisible);
	}

	private void getAttributes()
	{
		if (theFilePanel != null)
		{
			theFilePanel.update();
		}

		theSimulationPanel.update();
		theBDDPanel1.update();
		theBDDPanel2.update();
		theCommunicationPanel.update();
		theLayoutPanel.update();
		theSynchronizationPanel.update();
		theSoftPLCPanel.update();
	}

	private boolean setAttributes()
	{
		if (theBDDPanel1 != null)
		{
			if (!theBDDPanel1.doApply())
			{
				return false;
			}
		}

		if (theBDDPanel2 != null)
		{
			if (!theBDDPanel2.doApply())
			{
				return false;
			}
		}

		if (theFilePanel != null)
		{
			if (!theFilePanel.doApply())
			{
				return false;
			}
		}

		if (!theCommunicationPanel.doApply())
		{
			return false;
		}

		if (!theLayoutPanel.doApply())
		{
			return false;
		}

		if (!theSynchronizationPanel.doApply())
		{
			return false;
		}

		if (!theSoftPLCPanel.doApply())
		{
			return false;
		}

		if (!theSimulationPanel.doApply())
		{
			return false;
		}

		return true;
	}

	public static int getInt(String label, String theIntStr)
	{
		return getInt(label, theIntStr, Integer.MIN_VALUE);
	}

	public static int getInt(String label, String theIntStr, int minValue)
	{
		int theInt = Integer.MIN_VALUE;

		try
		{
			theInt = Integer.parseInt(theIntStr);
		}
		catch (NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(null, label + " must be a number.", "Illegal format", JOptionPane.ERROR_MESSAGE);

			// JOptionPane.showMessageDialog(this, label + " must be a number.", "Illegal format", JOptionPane.ERROR_MESSAGE);
			return Integer.MIN_VALUE;
		}

		if (theInt < minValue)
		{
			JOptionPane.showMessageDialog(null, label + " must be at least " + minValue + ".", "Illegal format", JOptionPane.ERROR_MESSAGE);

			// JOptionPane.showMessageDialog(this, label + " must be at least " + minValue + ".", "Illegal format", JOptionPane.ERROR_MESSAGE);
			return Integer.MIN_VALUE;
		}

		return theInt;
	}
}

class FilePanel
	extends JPanel
{
	private PreferencesDialog theDialog = null;
	private JTextField fileOpenPath = null;
	private JTextField fileSavePath = null;

	public FilePanel(PreferencesDialog theDialog)
	{
		this.theDialog = theDialog;

		Box propertiesBox = new Box(BoxLayout.Y_AXIS);

		add(propertiesBox, BorderLayout.CENTER);

		JLabel fileOpenPathLabel = new JLabel("File open path");

		propertiesBox.add(fileOpenPathLabel);

		fileOpenPath = new JTextField();

		propertiesBox.add(fileOpenPath);

		JLabel fileSavePathLabel = new JLabel("File save path");

		propertiesBox.add(fileSavePathLabel);

		fileSavePath = new JTextField();

		propertiesBox.add(fileSavePath);
	}

	public boolean doApply()
	{
		SupremicaProperties.setFileOpenPath(fileOpenPath.getText());
		SupremicaProperties.setFileSavePath(fileSavePath.getText());

		return true;
	}

	public void update()
	{
		fileOpenPath.setText(SupremicaProperties.getFileOpenPath());
		fileSavePath.setText(SupremicaProperties.getFileSavePath());
	}
}

class CommunicationPanel
	extends JPanel
{
	private PreferencesDialog theDialog = null;
	private JCheckBox useXmlRpc = null, debugXmlRpc = null;;
	private JTextField xmlRpcPort, xmlRpcFilter;
	private JTextField docdbHost, docdbPort, docdbUser, docdbDoc;

	public CommunicationPanel(PreferencesDialog theDialog)
	{

		// super( new BorderLayout());
		this.theDialog = theDialog;

		JPanel ptmp;
		JLabel tmp;
		JPanel panel = new JPanel(new GridLayout(6, 1));

		add(panel, BorderLayout.WEST);
		panel.add(tmp = new JLabel("XML-RPC", SwingConstants.LEFT));
		tmp.setForeground(Color.blue);
		panel.add(tmp = new JLabel("(must restart to take effect)", SwingConstants.CENTER));

		Box propertiesBox = new Box(BoxLayout.Y_AXIS);

		add(propertiesBox, BorderLayout.CENTER);
		panel.add(useXmlRpc = new JCheckBox("Run XML-RPC server"));

		xmlRpcPort = add(panel, "Use port number ", 10);
		xmlRpcFilter = add(panel, "Server IP filter ", 10);
		debugXmlRpc = new JCheckBox("Debug XML-RPC communication");

		if (SupremicaProperties.includeExperimentalAlgorithms())    // debugging XML-RPC is good to only developers
		{
			panel.add(debugXmlRpc);
		}

		panel = new JPanel(new GridLayout(5, 1));

		add(panel, BorderLayout.EAST);
		panel.add(tmp = new JLabel("Document Database", SwingConstants.LEFT));
		tmp.setForeground(Color.blue);

		docdbHost = add(panel, "Sever address", 10);
		docdbPort = add(panel, "Server port ", 10);
		docdbUser = add(panel, "Username", 10);
		docdbDoc = add(panel, "Default document", 10);
	}

	// ----------------------- helper funcs
	private JTextField add(JPanel panel, String txt, int width)
	{
		JPanel ptmp = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		panel.add(ptmp);
		ptmp.add(new JLabel(txt));

		JTextField tmp = new JTextField(10);

		ptmp.add(tmp);

		return tmp;
	}

	// -----------------------
	public boolean doApply()
	{
		int port = PreferencesDialog.getInt("XML-RPC Port", xmlRpcPort.getText(), 1);
		int port2 = PreferencesDialog.getInt("Remote document server port", docdbPort.getText(), 1);

		if ((port == Integer.MIN_VALUE) || (port2 == Integer.MIN_VALUE))
		{
			return false;
		}

		SupremicaProperties.setXmlRpcPort(port);
		SupremicaProperties.setXmlRpcFilter(xmlRpcFilter.getText());
		SupremicaProperties.setXmlRpcActive(useXmlRpc.isSelected());
		SupremicaProperties.setXmlRpcDebug(debugXmlRpc.isSelected());
		SupremicaProperties.setDocDBHost(docdbHost.getText());
		SupremicaProperties.setDocDBPort(port2);
		SupremicaProperties.setDocDBUsername(docdbUser.getText());
		SupremicaProperties.setDocDBDocument(docdbDoc.getText());

		return true;
	}

	public void update()
	{
		useXmlRpc.setSelected(SupremicaProperties.isXmlRpcActive());
		debugXmlRpc.setSelected(SupremicaProperties.isXmlRpcDebugging());
		xmlRpcPort.setText(Integer.toString(SupremicaProperties.getXmlRpcPort()));
		xmlRpcFilter.setText(SupremicaProperties.getXmlRpcFilter());
		docdbPort.setText("" + SupremicaProperties.getDocDBPort());
		docdbHost.setText(SupremicaProperties.getDocDBHost());
		docdbUser.setText(SupremicaProperties.getDocDBUsername());
		docdbDoc.setText(SupremicaProperties.getDocDBDocument());
	}
}

class LayoutPanel
	extends JPanel
{
	private PreferencesDialog theDialog = null;
	private JCheckBox dotLeftToRight = null;
	private JCheckBox dotWithStateLabels = null;
	private JCheckBox dotWithCircles = null;
	private JCheckBox dotUseColors = null;
	private JCheckBox dotUseMultipleLabels = null;
	private JTextField dotCommand = null;
	private JTextField dotMaxNbrOfStates = null;

	public LayoutPanel(PreferencesDialog theDialog)
	{
		this.theDialog = theDialog;

		Box propertiesBox = new Box(BoxLayout.Y_AXIS);

		add(propertiesBox, BorderLayout.CENTER);

		dotLeftToRight = new JCheckBox("Layout from left to right");

		propertiesBox.add(dotLeftToRight);

		dotWithStateLabels = new JCheckBox("Draw state labels");

		propertiesBox.add(dotWithStateLabels);

		dotWithCircles = new JCheckBox("Draw states as circles");

		propertiesBox.add(dotWithCircles);

		dotUseColors = new JCheckBox("Draw with colors");

		propertiesBox.add(dotUseColors);

		dotUseMultipleLabels = new JCheckBox("Draw multiple labels");

		propertiesBox.add(dotUseMultipleLabels);

		JLabel dotCommandLabel = new JLabel("Dot command");

		propertiesBox.add(dotCommandLabel);

		dotCommand = new JTextField();

		propertiesBox.add(dotCommand);

		JLabel dotMaxNbrOfStatesLabel = new JLabel("Maximum number of states without warning");

		propertiesBox.add(dotMaxNbrOfStatesLabel);

		dotMaxNbrOfStates = new JTextField();

		propertiesBox.add(dotMaxNbrOfStates);
	}

	public boolean doApply()
	{
		SupremicaProperties.setDotLeftToRight(dotLeftToRight.isSelected());
		SupremicaProperties.setDotWithStateLabels(dotWithStateLabels.isSelected());
		SupremicaProperties.setDotWithCircles(dotWithCircles.isSelected());
		SupremicaProperties.setDotUseColors(dotUseColors.isSelected());
		SupremicaProperties.setDotUseMultipleLabels(dotUseMultipleLabels.isSelected());
		SupremicaProperties.setDotExecuteCommand(dotCommand.getText());

		int maxNbrOfStates = PreferencesDialog.getInt("Max number of states without warning", dotMaxNbrOfStates.getText(), 0);

		if (maxNbrOfStates == Integer.MIN_VALUE)
		{
			return false;
		}

		SupremicaProperties.setDotMaxNbrOfStatesWithoutWarning(maxNbrOfStates);

		return true;
	}

	public void update()
	{
		dotLeftToRight.setSelected(SupremicaProperties.isDotLeftToRight());
		dotWithStateLabels.setSelected(SupremicaProperties.isDotWithStateLabels());
		dotWithCircles.setSelected(SupremicaProperties.isDotWithCircles());
		dotUseColors.setSelected(SupremicaProperties.isDotUseColors());
		dotUseMultipleLabels.setSelected(SupremicaProperties.isDotUseMultipleLabels());
		dotCommand.setText(SupremicaProperties.getDotExecuteCommand());
		dotMaxNbrOfStates.setText(Integer.toString(SupremicaProperties.getDotMaxNbrOfStatesWithoutWarning()));
	}
}

class SynchronizationPropertiesPanel
	extends JPanel
{
	private PreferencesDialog theDialog = null;
	private JCheckBox forbidUncontrollableStates = null;
	private JCheckBox expandForbiddenStates = null;
	private JCheckBox expandHashtable = null;
	private JCheckBox verboseMode = null;
	private JTextField hashtableSize = null;
	private JTextField nbrOfExecuters = null;

	public SynchronizationPropertiesPanel(PreferencesDialog theDialog)
	{
		this.theDialog = theDialog;

		Box propertiesBox = new Box(BoxLayout.Y_AXIS);

		add(propertiesBox, BorderLayout.CENTER);

		forbidUncontrollableStates = new JCheckBox("Forbid uncontrollable states");

		propertiesBox.add(forbidUncontrollableStates);

		expandForbiddenStates = new JCheckBox("Expand forbidden states");

		propertiesBox.add(expandForbiddenStates);

		expandHashtable = new JCheckBox("Expand hashtable");

		propertiesBox.add(expandHashtable);

		verboseMode = new JCheckBox("Verbose mode");

		propertiesBox.add(verboseMode);

		JLabel hashtableSizeLabel = new JLabel("Initial size of the hashtable");

		propertiesBox.add(hashtableSizeLabel);

		hashtableSize = new JTextField();

		propertiesBox.add(hashtableSize);

		JLabel nbrOfExecutersLabel = new JLabel("Nbr of threads");

		propertiesBox.add(nbrOfExecutersLabel);

		nbrOfExecuters = new JTextField();

		propertiesBox.add(nbrOfExecuters);
	}

	public boolean doApply()
	{
		SupremicaProperties.setSyncForbidUncontrollableStates(forbidUncontrollableStates.isSelected());
		SupremicaProperties.setSyncExpandForbiddenStates(expandForbiddenStates.isSelected());
		SupremicaProperties.setSyncExpandHashtable(expandHashtable.isSelected());
		SupremicaProperties.setVerboseMode(verboseMode.isSelected());

		int size = PreferencesDialog.getInt("Hashtable size", hashtableSize.getText(), 100);

		if (size == Integer.MIN_VALUE)
		{
			return false;
		}

		SupremicaProperties.setSyncInitialHashtableSize(size);

		int nbrOfThreads = PreferencesDialog.getInt("Nbr of threads", nbrOfExecuters.getText(), 1);

		if (nbrOfThreads == Integer.MIN_VALUE)
		{
			return false;
		}

		SupremicaProperties.setSyncNbrOfExecuters(nbrOfThreads);

		return true;
	}

	public void update()
	{
		forbidUncontrollableStates.setSelected(SupremicaProperties.syncForbidUncontrollableStates());
		expandForbiddenStates.setSelected(SupremicaProperties.syncExpandForbiddenStates());
		expandHashtable.setSelected(SupremicaProperties.syncExpandHashtable());
		verboseMode.setSelected(SupremicaProperties.verboseMode());
		hashtableSize.setText(Integer.toString(SupremicaProperties.syncInitialHashtableSize()));
		nbrOfExecuters.setText(Integer.toString(SupremicaProperties.syncNbrOfExecuters()));
	}
}

class PreferencesControllerPanel
	extends JPanel
{
	private PreferencesDialog theDialog = null;
	private JButton applyButton = null;
	private JButton cancelButton = null;

	public PreferencesControllerPanel(PreferencesDialog theDialog)
	{
		this.theDialog = theDialog;

		Box buttonBox = new Box(BoxLayout.X_AXIS);

		applyButton = new JButton("Apply");
		cancelButton = new JButton("Cancel");

		add(applyButton, BorderLayout.CENTER);
		add(cancelButton, BorderLayout.CENTER);

//              buttonBox.add(Box.createHorizontalGlue());
//              buttonBox.add(applyButton);
//              buttonBox.add(Box.createHorizontalGlue());
//              buttonBox.add(cancelButton);
//              buttonBox.add(Box.createHorizontalGlue());
//              add(buttonBox, BorderLayout.NORTH);
		applyButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				apply_actionPerformed(e);
			}
		});
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancel_actionPerformed(e);
			}
		});
	}

	public void cancel_actionPerformed(ActionEvent e)
	{
		theDialog.doCancel();
	}

	public void apply_actionPerformed(ActionEvent e)
	{
		theDialog.doApply();
	}
}

class SoftPLCPanel
	extends JPanel
{
	private PreferencesDialog theDialog = null;
	private JCheckBox useXmlRpc = null;
	private JTextField cycleTime = new JTextField();
	private Vector interfaces = new Vector();
	private JList ioInterfaceList;

	public SoftPLCPanel(PreferencesDialog theDialog)
	{
		this.theDialog = theDialog;

		JPanel contentPane = new JPanel();
		GridBagLayout gridBagLayout1 = new GridBagLayout();
		JLabel jLabel1 = new JLabel("Default cycle time (ms)");
		JLabel jLabel2 = new JLabel("Available IO-interfaces");
		JButton removeButton = new JButton("Remove");
		JButton addButton = new JButton("Add");
		JScrollPane interfaceScrollPane = new JScrollPane();

		interfaces = SupremicaProperties.getSoftplcInterfaces();
		ioInterfaceList = new JList(interfaces);

		contentPane.setLayout(gridBagLayout1);
		ioInterfaceList.setVisibleRowCount(2);
		interfaceScrollPane.getViewport().setView(ioInterfaceList);
		addButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addButton_actionPerformed(e);
			}
		});
		removeButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				removeButton_actionPerformed(e);
			}
		});
		contentPane.add(interfaceScrollPane, new GridBagConstraints(0, 3, 1, 2, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 80, 60, 20), 60, 0));
		contentPane.add(jLabel1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(10, 81, 3, 40), 199, 0));
		contentPane.add(cycleTime, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(8, 82, 23, 0), 181, 0));
		contentPane.add(removeButton, new GridBagConstraints(1, 4, 1, 1, 0.4, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 0, 74, 8), 5, 0));
		contentPane.add(addButton, new GridBagConstraints(1, 3, 1, 1, 0.4, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 0, 7, 40), 0, 0));
		contentPane.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 81, 9, 10), 0, 0));
		add(contentPane, BorderLayout.CENTER);
	}

	void addButton_actionPerformed(ActionEvent e)
	{
		JFileChooser outputDir = new JFileChooser();

		if (outputDir.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = outputDir.getSelectedFile();

			if (currFile != null)
			{
				if (!currFile.isDirectory())
				{
					interfaces.add(new SoftplcInterface(currFile.getPath()));
					ioInterfaceList.updateUI();
				}
			}
		}
	}

	void removeButton_actionPerformed(ActionEvent e)
	{
		interfaces.removeElementAt(ioInterfaceList.getSelectedIndex());
		ioInterfaceList.updateUI();
	}

	public boolean doApply()
	{
		int cycleTimeInt = PreferencesDialog.getInt("Cycle time", cycleTime.getText());

		SupremicaProperties.setSoftplcCycleTime(cycleTimeInt);
		SupremicaProperties.setSoftplcInterfaces(interfaces);

		return true;
	}

	public void update()
	{
		cycleTime.setText(Integer.toString(SupremicaProperties.getSoftplcCycleTime()));
		ioInterfaceList.updateUI();
	}
}

/** BDD specific stuff, what a f**king mess! */
class BDDPanel1
	extends JPanel
{
	private PreferencesDialog theDialog = null;
	private JCheckBox alterPCG, traceOn, ucOptimistic, nbOptimistic;
	/* package access */
	JCheckBox debugOn;
	private JCheckBox localSaturation, encodingFill, sizeWatch, profileOn,
					  burstMode;
	private JComboBox algorithmFamily, dssiHeuristics, ndasHeuristics;
	private JComboBox inclusionAlgorithm, asHeuristics, esHeuristics,
					  frontierStrategy;

	public BDDPanel1(PreferencesDialog theDialog)
	{
		this.theDialog = theDialog;

		JLabel tmp;
		JPanel pLeft = new JPanel(new BorderLayout());

		add(pLeft, BorderLayout.WEST);

		// pLeft.add(tmp = new JLabel("Please don't touch anything", SwingConstants.CENTER), BorderLayout.NORTH);
		// tmp.setForeground(Color.red);
		JPanel pWest = new JPanel(new GridLayout(10, 1));

		pLeft.add(pWest, BorderLayout.CENTER);
		pWest.add(tmp = new JLabel("User interaction and reports:", SwingConstants.LEFT));
		tmp.setForeground(Color.blue);
		pWest.add(alterPCG = new JCheckBox("User is allowed to alter PCG orders", Options.user_alters_PCG));
		pWest.add(traceOn = new JCheckBox("Dump execution trace (SLOW!)", Options.trace_on));
		pWest.add(debugOn = new JCheckBox("Verbose", Options.debug_on));
		pWest.add(profileOn = new JCheckBox("Profile", Options.profile_on));
		pWest.add(sizeWatch = new JCheckBox("report nodcount", Options.size_watch));
		pWest.add(tmp = new JLabel("Computation options:"));
		tmp.setForeground(Color.blue);

		// pWest.add( ucOptimistic = new JCheckBox("Optimisitc on controllability", Options.uc_optimistic));
		// pWest.add( nbOptimistic = new JCheckBox("Optimisitc on liveness", Options.nb_optimistic));
		pWest.add(localSaturation = new JCheckBox("Locally saturate", Options.local_saturation));
		localSaturation.setEnabled(false);
		pWest.add(encodingFill = new JCheckBox("Full encoding of S", Options.fill_statevars));
		encodingFill.setEnabled(false);
		pWest.add(burstMode = new JCheckBox("Burst-mode workset", Options.burst_mode));

		// -------------------------------------------------------
		Box p = new Box(BoxLayout.Y_AXIS);

		add(p, BorderLayout.EAST);

		JPanel pLabel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		p.add(pLabel);
		pLabel.add(tmp = new JLabel("Algorithm selection:"));
		tmp.setForeground(Color.blue);

		JPanel pFrontier = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		p.add(pFrontier);
		pFrontier.add(new JLabel("Frontier choice"));
		pFrontier.add(frontierStrategy = new JComboBox());
		insert(frontierStrategy, Options.FRONTIER_STRATEGY_NAMES, Options.frontier_strategy);

		JPanel pInclusion = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		p.add(pInclusion);
		pInclusion.add(new JLabel("Language containment algorithm"));
		pInclusion.add(inclusionAlgorithm = new JComboBox());
		insert(inclusionAlgorithm, Options.INCLUSION_ALGORITHM_NAMES, Options.inclsuion_algorithm);

		JPanel pFamily = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		p.add(pFamily);
		pFamily.add(new JLabel("Favour reachability algorithm"));
		pFamily.add(algorithmFamily = new JComboBox());
		insert(algorithmFamily, Options.REACH_ALGO_NAMES, Options.algo_family);

		JPanel pHeuristics = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		p.add(pHeuristics);
		pHeuristics.add(new JLabel("Automaton selection heuristic"));
		pHeuristics.add(asHeuristics = new JComboBox());
		insert(asHeuristics, Options.AS_HEURISTIC_NAMES, Options.as_heuristics);

		JPanel pDelayed = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		p.add(pDelayed);
		pDelayed.add(new JLabel("Delayed* insertation heuristic"));
		pDelayed.add(dssiHeuristics = new JComboBox());
		insert(dssiHeuristics, Options.DSSI_HEURISTIC_NAMES, Options.dssi_heuristics);

		pHeuristics = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		p.add(pHeuristics);
		pHeuristics.add(new JLabel("H1: PN-event and workset-automaton selection"));
		pHeuristics.add(esHeuristics = new JComboBox());
		insert(esHeuristics, Options.ES_HEURISTIC_NAMES, Options.es_heuristics);

		JPanel pNdas = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		p.add(pNdas);
		pNdas.add(new JLabel("H2: Non-deteministic selection of equals"));
		pNdas.add(ndasHeuristics = new JComboBox());
		insert(ndasHeuristics, Options.NDAS_HEURISTIC_NAMES, Options.ndas_heuristics);
	}

	// ------------------------------------------
	private void insert(JComboBox cb, String[] names, int def)
	{
		for (int i = 0; i < names.length; i++)
		{
			cb.addItem(names[i]);
		}

		cb.setSelectedIndex(def);
		cb.setMaximumRowCount(20);
	}

	public boolean doApply()
	{

		// SupremicaProperties.updateBDDOptions(true);
		Options.algo_family = algorithmFamily.getSelectedIndex();
		Options.frontier_strategy = frontierStrategy.getSelectedIndex();
		Options.inclsuion_algorithm = inclusionAlgorithm.getSelectedIndex();
		Options.as_heuristics = asHeuristics.getSelectedIndex();
		Options.ndas_heuristics = ndasHeuristics.getSelectedIndex();
		Options.es_heuristics = esHeuristics.getSelectedIndex();
		Options.dssi_heuristics = dssiHeuristics.getSelectedIndex();
		Options.user_alters_PCG = alterPCG.isSelected();

		// Options.uc_optimistic    = ucOptimistic.isSelected();
		// Options.nb_optimistic    = nbOptimistic.isSelected();
		Options.burst_mode = burstMode.isSelected();
		Options.trace_on = traceOn.isSelected();
		Options.profile_on = profileOn.isSelected();
		Options.debug_on = debugOn.isSelected();
		Options.size_watch = sizeWatch.isSelected();
		Options.local_saturation = localSaturation.isSelected();
		Options.fill_statevars = encodingFill.isSelected();

		return true;
	}

	public void update() {}
}

/** more BDD specific stuff, still a mess! */
class BDDPanel2
	extends JPanel
	implements ActionListener
{
	private PreferencesDialog theDialog = null;
	private JComboBox cbReordering, showGrow, countAlgorithm,
					  orderingAlgorithm, encodingAlgorithm;
	private JCheckBox cReorderDynamic, cReorderBuild, cReorderGroup,
					  cReorderGroupFree;
	private JTextField maxPartitionSize, extraLibDir;
	private JButton bProofFile;

	public BDDPanel2(PreferencesDialog theDialog)
	{
		this.theDialog = theDialog;

		// -------------------------------------- LEFT
		JPanel pLeft = new JPanel(new BorderLayout());

		add(pLeft, BorderLayout.WEST);

		// TOP LEFT
		JPanel pTopLeft = new JPanel(new GridLayout(5, 1));

		pLeft.add(pTopLeft, BorderLayout.NORTH);

		JLabel tmp;

		pTopLeft.add(tmp = new JLabel("Automata to BDD conversion", SwingConstants.LEFT));
		tmp.setForeground(Color.blue);

		JPanel pOrdering = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		pOrdering.add(new JLabel("Automaton ordering"));
		pOrdering.add(orderingAlgorithm = new JComboBox());
		insert(orderingAlgorithm, Options.ORDERING_ALGORITHM_NAMES, Options.ordering_algorithm);
		pTopLeft.add(pOrdering);

		JPanel pEncoding = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		pEncoding.add(new JLabel("State encoding"));
		pEncoding.add(encodingAlgorithm = new JComboBox());
		insert(encodingAlgorithm, Options.ENCODING_NAMES, Options.encoding_algorithm);
		pTopLeft.add(pEncoding);
		pTopLeft.add(tmp = new JLabel("Dynamic variable ordering (NOT recommended)", SwingConstants.LEFT));
		tmp.setForeground(Color.blue);

		JPanel ptmp = new JPanel();

		ptmp.add(new JLabel("Dynamic reordering method"));
		ptmp.add(cbReordering = new JComboBox());
		insert(cbReordering, Options.REORDER_ALGO_NAMES, Options.reorder_algo);
		pTopLeft.add(ptmp);

		// CENTER LEFT
		JPanel pLeftLeft = new JPanel(new GridLayout(4, 1));

		pLeft.add(pLeftLeft, BorderLayout.CENTER);
		pLeftLeft.add(cReorderDynamic = new JCheckBox("Enable dymanic reordering", Options.reorder_dyanmic));
		pLeftLeft.add(cReorderBuild = new JCheckBox("Reorder after build", Options.reorder_after_build));
		pLeftLeft.add(cReorderGroup = new JCheckBox("Don't reorder between automata", Options.reorder_with_groups));
		pLeftLeft.add(cReorderGroupFree = new JCheckBox("Don't reorder inside automata", Options.reorder_within_group));

		// -------------------------------- RIGHT
		JPanel pRight = new JPanel(new BorderLayout());

		add(pRight, BorderLayout.EAST);

		// TOP RIGHT
		JPanel pTopRight = new JPanel(new GridLayout(4, 1));

		pRight.add(pTopRight, BorderLayout.NORTH);
		pTopRight.add(tmp = new JLabel("Misc. options:"));
		tmp.setForeground(Color.blue);

		JPanel pCount = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		pTopRight.add(pCount);
		pCount.add(new JLabel("State enumeration algorithm"));
		pCount.add(countAlgorithm = new JComboBox());
		insert(countAlgorithm, Options.COUNT_ALGO_NAMES, Options.count_algo);

		JPanel pPartitionSize = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		pPartitionSize.add(new JLabel("Max BDD nodes/cluster"));
		pPartitionSize.add(maxPartitionSize = new JTextField("" + Options.max_partition_size, 5));
		pTopRight.add(pPartitionSize);

		JPanel pExtraLib = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		pExtraLib.add(new JLabel("Additinal DLL path"));
		pExtraLib.add(extraLibDir = new JTextField("" + Options.extraLibPath, 15));
		pTopRight.add(pExtraLib);

		// BOTTOM RIGHT
		JPanel pBottomRight = new JPanel(new GridLayout(3, 1));

		pRight.add(pBottomRight, BorderLayout.SOUTH);
		pBottomRight.add(tmp = new JLabel("Administrativa:"));
		tmp.setForeground(Color.blue);

		JPanel pGrow = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		pBottomRight.add(pGrow);
		pGrow.add(new JLabel("BDD graphs"));
		pGrow.add(showGrow = new JComboBox());
		insert(showGrow, Options.SHOW_GROW_NAMES, Options.show_grow);

		JPanel pProof = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		pProof.add(bProofFile = new JButton("Set proof file"));
		pProof.add(new JLabel(" (verbose and slow!)"));
		bProofFile.addActionListener(this);
		pBottomRight.add(pProof);
	}

	public boolean doApply()
	{
		int maxsize = PreferencesDialog.getInt("Max cluster size", maxPartitionSize.getText(), 0);

		if (maxsize == Integer.MIN_VALUE)
		{
			return false;
		}

		Options.extraLibPath = extraLibDir.getText();
		Options.max_partition_size = maxsize;
		Options.reorder_algo = cbReordering.getSelectedIndex();
		Options.reorder_dyanmic = cReorderDynamic.isSelected();
		Options.reorder_after_build = cReorderBuild.isSelected();
		Options.reorder_with_groups = cReorderGroup.isSelected();
		Options.reorder_within_group = cReorderGroupFree.isSelected();
		Options.ordering_algorithm = orderingAlgorithm.getSelectedIndex();
		Options.encoding_algorithm = encodingAlgorithm.getSelectedIndex();
		Options.show_grow = showGrow.getSelectedIndex();
		Options.count_algo = countAlgorithm.getSelectedIndex();

		return true;
	}

	public void update() {}

	// ----------------------------------------------------------
	private void insert(JComboBox cb, String[] names, int def)
	{
		for (int i = 0; i < names.length; i++)
		{
			cb.addItem(names[i]);
		}

		cb.setSelectedIndex(def);
		cb.setMaximumRowCount(20);
	}

	// ---------------------------------------------------------
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();

		if (src == bProofFile)
		{
			onSetProofFile();
		}
	}

	private void onSetProofFile()
	{

		// AWT is much better than Swing
		FileDialog fd = new FileDialog(theDialog.getOwnerFrame(), "Choose a proof file", FileDialog.SAVE);

		fd.show();

		if (fd.getFile() != null)
		{
			String path = fd.getDirectory() + fd.getFile();

			try
			{
				FileOutputStream fos = new FileOutputStream(path, true);
				PrintStream ps = new PrintStream(fos);

				Options.out = ps;

				theDialog.theBDDPanel1.debugOn.setSelected(true);    // enable debug!

				Date now = new Date();

				Options.out.println("\n Proof file opened at " + now);
				System.out.println("Proof file: " + path);
			}
			catch (IOException exx)
			{
				System.err.println("Unable to set proof file: " + exx);
			}
		}
	}
}
;

class SimulationPanel
	extends JPanel
{
	private PreferencesDialog theDialog = null;
	private JCheckBox useExternal;
	private JTextField cycleTime;

	public SimulationPanel(PreferencesDialog theDialog)
	{
		this.theDialog = theDialog;

		JPanel contentPane = new JPanel();
		Box propertiesBox = new Box(BoxLayout.Y_AXIS);

		add(propertiesBox, BorderLayout.CENTER);
		propertiesBox.add(new JLabel("Note: event source should be changed BEFORE any simulations are started."));
		propertiesBox.add(useExternal = new JCheckBox("Use external event source"));

		JPanel jp = new JPanel();

		propertiesBox.add(jp);
		jp.add(new JLabel("Simulation cycle time (lower bound)"));
		jp.add(cycleTime = new JTextField("" + SupremicaProperties.getSimulationCycleTime(), 10));
		jp.add(new JLabel("[ms]"));
	}

	public boolean doApply()
	{
		SupremicaProperties.setSimulationIsExternal(useExternal.isSelected());

		int time = PreferencesDialog.getInt("Simulation cycle time", cycleTime.getText(), 0);

		if (time == Integer.MIN_VALUE)
		{
			return false;
		}

		SupremicaProperties.setSimulationCycleTime(time);

		return true;
	}

	public void update()
	{
		useExternal.setSelected(SupremicaProperties.getSimulationIsExternal());
		cycleTime.setText("" + SupremicaProperties.getSimulationCycleTime());
	}
}

class RobotCoordinationPanel
	extends JPanel
{
	private PreferencesDialog theDialog = null;
	private JCheckBox showRobotCoordination;
	private JTextField cycleTime;

	public RobotCoordinationPanel(PreferencesDialog theDialog)
	{
		this.theDialog = theDialog;

		JPanel contentPane = new JPanel();
		Box propertiesBox = new Box(BoxLayout.Y_AXIS);

		add(propertiesBox, BorderLayout.CENTER);
		propertiesBox.add(showRobotCoordination = new JCheckBox("Show robot coordination tools"));
	}

	public boolean doApply()
	{
		SupremicaProperties.setGeneralUseRobotCoordination(showRobotCoordination.isSelected());
		SupremicaProperties.setShowRobotstudioLink(showRobotCoordination.isSelected());
		SupremicaProperties.setShowCoordinationABB(showRobotCoordination.isSelected());

		return true;
	}

	public void update()
	{
		showRobotCoordination.setSelected(SupremicaProperties.generalUseRobotCoordination());
	}
}
