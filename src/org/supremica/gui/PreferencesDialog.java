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

public class PreferencesDialog
	extends JDialog
{
	private JPanel contentPane = null;
	private JTabbedPane theTabbedPanel = null;

	private FilePanel theFilePanel = null;
	private CommunicationPanel theCommunicationPanel = null;
	private LayoutPanel theLayoutPanel = null;
	private SynchronizationPanel theSynchronizationPanel = null;
	private PreferencesControllerPanel theControllerPanel = null;

	public PreferencesDialog(Frame owner)
	{
		super(owner, "Preferences", true);

		contentPane = (JPanel)getContentPane();
		theTabbedPanel = new JTabbedPane();
		contentPane.add(theTabbedPanel, BorderLayout.CENTER);

		theSynchronizationPanel = new SynchronizationPanel(this);
		theTabbedPanel.add("Synchronization", theSynchronizationPanel);

		theLayoutPanel = new LayoutPanel(this);
		theTabbedPanel.add("Layout", theLayoutPanel);

		theCommunicationPanel = new CommunicationPanel(this);
		theTabbedPanel.add("Communication", theCommunicationPanel);

		theFilePanel = new FilePanel(this);
		theTabbedPanel.add("File", theFilePanel);

		theControllerPanel = new PreferencesControllerPanel(this);
		contentPane.add(theControllerPanel, BorderLayout.SOUTH);

		setSize(400, 350);

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

	public void doCancel()
	{
		setVisible(false);
		dispose();
	}

	public void doApply()
	{
		if (setAttributes())
		{
			doCancel();
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
		theFilePanel.update();
		theCommunicationPanel.update();
		theLayoutPanel.update();
		theSynchronizationPanel.update();
	}

	private boolean setAttributes()
	{
		if (!theFilePanel.doApply())
		{
			return false;
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
		return true;
	}

	int getInt(String label, String theIntStr)
	{
		return getInt(label, theIntStr, Integer.MIN_VALUE);
	}

	int getInt(String label, String theIntStr, int minValue)
	{
		int theInt = Integer.MIN_VALUE;
		try
		{
			theInt = Integer.parseInt(theIntStr);
		}
		catch (NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(this, label + " must be a number.", "Illegal format", JOptionPane.ERROR_MESSAGE);
			return Integer.MIN_VALUE;
		}
		if (theInt < minValue)
		{
			JOptionPane.showMessageDialog(this, label + " must be at least " + minValue + ".", "Illegal format", JOptionPane.ERROR_MESSAGE);
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

		JLabel fileOpenPathLabel =
			new JLabel("File open path");
		propertiesBox.add(fileOpenPathLabel);

		fileOpenPath = new JTextField();
		propertiesBox.add(fileOpenPath);

		JLabel fileSavePathLabel =
			new JLabel("File save path");
		propertiesBox.add(fileSavePathLabel);

		fileSavePath = new JTextField();
		propertiesBox.add(fileSavePath);
	}

	public boolean doApply()
	{
		WorkbenchProperties.setFileOpenPath(fileOpenPath.getText());
		WorkbenchProperties.setFileSavePath(fileSavePath.getText());

		return true;
	}

	public void update()
	{
		fileOpenPath.setText(WorkbenchProperties.getFileOpenPath());
		fileSavePath.setText(WorkbenchProperties.getFileSavePath());
	}
}

class CommunicationPanel
	extends JPanel
{
	private PreferencesDialog theDialog = null;

	private JCheckBox useXmlRpc = null;
	private JTextField xmlRpcPort = null;

	public CommunicationPanel(PreferencesDialog theDialog)
	{
		this.theDialog = theDialog;

        Box propertiesBox = new Box(BoxLayout.Y_AXIS);

		add(propertiesBox, BorderLayout.CENTER);

		useXmlRpc =
			new JCheckBox("Run XML-RPC server");
		propertiesBox.add(useXmlRpc);

		JLabel xmlRpcPortLabel =
			new JLabel("Use port number");
		propertiesBox.add(xmlRpcPortLabel);

		xmlRpcPort = new JTextField();
		propertiesBox.add(xmlRpcPort);
	}

	public boolean doApply()
	{
		WorkbenchProperties.setXmlRpcActive(useXmlRpc.isSelected());

		int port = theDialog.getInt("XML-RPC Port", xmlRpcPort.getText(), 1);
		if (port == Integer.MIN_VALUE)
		{
			return false;
		}
		WorkbenchProperties.setXmlRpcPort(port);

		return true;
	}

	public void update()
	{
		useXmlRpc.setSelected(WorkbenchProperties.isXmlRpcActive());
		xmlRpcPort.setText(
			Integer.toString(WorkbenchProperties.getXmlRpcPort()));
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

		dotLeftToRight =
			new JCheckBox("Layout from left to right");
		propertiesBox.add(dotLeftToRight);

		dotWithStateLabels =
			new JCheckBox("Draw state labels");
		propertiesBox.add(dotWithStateLabels);

		dotWithCircles =
			new JCheckBox("Draw states as circles");
		propertiesBox.add(dotWithCircles);

		dotUseColors =
			new JCheckBox("Draw with colors");
		propertiesBox.add(dotUseColors);

		dotUseMultipleLabels =
			new JCheckBox("Draw multiple labels");
		propertiesBox.add(dotUseMultipleLabels);

		JLabel dotCommandLabel =
			new JLabel("Dot command");
		propertiesBox.add(dotCommandLabel);

		dotCommand = new JTextField();
		propertiesBox.add(dotCommand);

		JLabel dotMaxNbrOfStatesLabel =
			new JLabel("Maximum number of states without warning");
		propertiesBox.add(dotMaxNbrOfStatesLabel);

		dotMaxNbrOfStates = new JTextField();
		propertiesBox.add(dotMaxNbrOfStates);
	}

	public boolean doApply()
	{
		WorkbenchProperties.setDotLeftToRight(
			dotLeftToRight.isSelected());
		WorkbenchProperties.setDotWithStateLabels(
			dotWithStateLabels.isSelected());
		WorkbenchProperties.setDotWithCircles(
			dotWithCircles.isSelected());
		WorkbenchProperties.setDotUseColors(
			dotUseColors.isSelected());
		WorkbenchProperties.setDotUseMultipleLabels(
			dotUseMultipleLabels.isSelected());

		WorkbenchProperties.setDotExecuteCommand(dotCommand.getText());

		int maxNbrOfStates = theDialog.getInt("Max number of states without warning", dotMaxNbrOfStates.getText(), 0);
		if (maxNbrOfStates == Integer.MIN_VALUE)
		{
			return false;
		}
		WorkbenchProperties.setDotMaxNbrOfStatesWithoutWarning(maxNbrOfStates);

		return true;
	}

	public void update()
	{
		dotLeftToRight.setSelected(
			WorkbenchProperties.isDotLeftToRight());
		dotWithStateLabels.setSelected(
			WorkbenchProperties.isDotWithStateLabels());
		dotWithCircles.setSelected(
			WorkbenchProperties.isDotWithCircles());
		dotUseColors.setSelected(
			WorkbenchProperties.isDotUseColors());
		dotUseMultipleLabels.setSelected(
			WorkbenchProperties.isDotUseMultipleLabels());
		dotCommand.setText(WorkbenchProperties.getDotExecuteCommand());
		dotMaxNbrOfStates.setText(
			Integer.toString(WorkbenchProperties.getDotMaxNbrOfStatesWithoutWarning()));
	}
}

class SynchronizationPanel
	extends JPanel
{
	private PreferencesDialog theDialog = null;

	private JCheckBox forbidUncontrollableStates = null;
	private JCheckBox expandForbiddenStates = null;
	private JCheckBox expandHashtable = null;
	private JTextField hashtableSize = null;
	private JTextField nbrOfExecuters = null;

	public SynchronizationPanel(PreferencesDialog theDialog)
	{
		this.theDialog = theDialog;

        Box propertiesBox = new Box(BoxLayout.Y_AXIS);

		add(propertiesBox, BorderLayout.CENTER);

		forbidUncontrollableStates =
			new JCheckBox("Forbid uncontrollable states");
		propertiesBox.add(forbidUncontrollableStates);

		expandForbiddenStates =
			new JCheckBox("Expand forbidden states");
		propertiesBox.add(expandForbiddenStates);

		expandHashtable =
			new JCheckBox("Expand hashtable");
		propertiesBox.add(expandHashtable);

		JLabel hashtableSizeLabel =
			new JLabel("Initial size of the hashtable");
		propertiesBox.add(hashtableSizeLabel);

		hashtableSize = new JTextField();
		propertiesBox.add(hashtableSize);

		JLabel nbrOfExecutersLabel =
			new JLabel("Nbr of threads");
		propertiesBox.add(nbrOfExecutersLabel);

		nbrOfExecuters = new JTextField();
		propertiesBox.add(nbrOfExecuters);
	}

	public boolean doApply()
	{
		WorkbenchProperties.setSyncForbidUncontrollableStates(
			forbidUncontrollableStates.isSelected());
		WorkbenchProperties.setSyncExpandForbiddenStates(
			expandForbiddenStates.isSelected());
		WorkbenchProperties.setSyncExpandHashtable(
			expandHashtable.isSelected());

		int size = theDialog.getInt("Hashtable size", hashtableSize.getText(), 100);
		if (size == Integer.MIN_VALUE)
		{
			return false;
		}
		WorkbenchProperties.setSyncInitialHashtableSize(size);

		int nbrOfThreads = theDialog.getInt("Nbr of threads", nbrOfExecuters.getText(), 1);
		if (nbrOfThreads == Integer.MIN_VALUE)
		{
			return false;
		}
		WorkbenchProperties.setSyncNbrOfExecuters(nbrOfThreads);

		return true;
	}

	public void update()
	{
		forbidUncontrollableStates.setSelected(
			WorkbenchProperties.syncForbidUncontrollableStates());
		expandForbiddenStates.setSelected(
			WorkbenchProperties.syncExpandForbiddenStates());
		expandHashtable.setSelected(
			WorkbenchProperties.syncExpandHashtable());
		hashtableSize.setText(
			Integer.toString(WorkbenchProperties.syncInitialHashtableSize()));
		nbrOfExecuters.setText(
			Integer.toString(WorkbenchProperties.syncNbrOfExecuters()));
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

		add(buttonBox, BorderLayout.CENTER);

		applyButton = new JButton("Apply");
		cancelButton = new JButton("Cancel");

		buttonBox.add(applyButton);
		buttonBox.add(Box.createVerticalGlue());
		buttonBox.add(cancelButton);

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