
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.automata.algorithms.*;

abstract class SynchronizationPanel
	extends JPanel
{
	public abstract void update(SynchronizationOptions s);

	public abstract void regain(SynchronizationOptions s);
}

class SynchronizationDialogStandardPanel
	extends SynchronizationPanel
{
	private static final long serialVersionUID = 1L;
	private JCheckBox forbidUnconStatesBox;
	private JCheckBox buildAutomatonBox;
	private JCheckBox useShortStateNamesBox;
	private JTextField stateNameSeparator;

	public SynchronizationDialogStandardPanel()
	{
		Box standardBox = Box.createVerticalBox();

		forbidUnconStatesBox = new JCheckBox("Mark uncontrollable states as forbidden");
		forbidUnconStatesBox.setToolTipText("If checked, uncontrollable states become forbidden " +
											"in the synchronization");
		buildAutomatonBox = new JCheckBox("Build a full automaton model");
		buildAutomatonBox.setToolTipText("If not checked, the only output is statistics about the operation (saves computation)");
		useShortStateNamesBox = new JCheckBox("Use short state names");
		useShortStateNamesBox.setToolTipText("Give the states in the composition short, abstract names instead of keeping the original state names");

		JLabel stateNameSeparatorLabel = new JLabel("State name separator");
		stateNameSeparator = new JTextField();
		stateNameSeparator.setToolTipText("The name of the synchronized state is a concatenation of the names of the states, separated by this string.");

		standardBox.add(forbidUnconStatesBox);
		standardBox.add(buildAutomatonBox);
		standardBox.add(useShortStateNamesBox);
		standardBox.add(stateNameSeparatorLabel);
		standardBox.add(stateNameSeparator);
		this.add(standardBox);
	}

	public void update(SynchronizationOptions synchronizationOptions)
	{
		forbidUnconStatesBox.setSelected(synchronizationOptions.forbidUncontrollableStates());
		buildAutomatonBox.setSelected(synchronizationOptions.buildAutomaton());
		useShortStateNamesBox.setSelected(synchronizationOptions.useShortStateNames());
		stateNameSeparator.setText(synchronizationOptions.getStateNameSeparator());
	}

	public void regain(SynchronizationOptions synchronizationOptions)
	{
		synchronizationOptions.setForbidUncontrollableStates(forbidUnconStatesBox.isSelected());
		synchronizationOptions.setBuildAutomaton(buildAutomatonBox.isSelected());
		synchronizationOptions.setUseShortStateNames(useShortStateNamesBox.isSelected());
		synchronizationOptions.setStateNameSeparator(stateNameSeparator.getText());
	}
}

class SynchronizationDialogAdvancedPanel
	extends SynchronizationPanel
	implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private JComboBox synchronizationTypeBox;
	private JCheckBox expandForbiddenStatesBox;
	private JCheckBox rememberDisabledEventsBox;

	public SynchronizationDialogAdvancedPanel()
	{
		this.setLayout(new BorderLayout());

		synchronizationTypeBox = new JComboBox(SynchronizationType.toArray());
		synchronizationTypeBox.setToolTipText("Choose the type of composition");
		expandForbiddenStatesBox = new JCheckBox("Expand forbidden states");
		expandForbiddenStatesBox.setToolTipText("If checked, transitions from forbidden states are " +
												"examined, otherwise, the states are considered terminal");
		expandForbiddenStatesBox.addActionListener(this);
		rememberDisabledEventsBox = new JCheckBox("Include disabled transitions");
		rememberDisabledEventsBox.setToolTipText("Adds transitions to a new 'dump'-state for all transitions in a plant that are disabled by a specification");

		JPanel choicePanel = new JPanel();
		choicePanel.setLayout(new FlowLayout());
		choicePanel.add(synchronizationTypeBox);
		this.add("North", choicePanel);

		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new FlowLayout());
		Box checkBoxBox = Box.createVerticalBox();
		//checkBoxBox.setLayout(new FlowLayout());
		checkBoxBox.add(expandForbiddenStatesBox);
		checkBoxBox.add(rememberDisabledEventsBox);
		checkBoxPanel.add(checkBoxBox);
		this.add("Center", checkBoxPanel);

		/*
		Box advancedBox = Box.createVerticalBox();

		synchronizationTypeBox = new JComboBox(SynchronizationType.toArray());
		synchronizationTypeBox.setToolTipText("Choose the type of composition");
		expandForbiddenStatesBox = new JCheckBox("Expand forbidden states");
		expandForbiddenStatesBox.setToolTipText("If cheched, transitions from forbidden states are " +
												"examined, otherwise, the states are considered terminal");
		expandForbiddenStatesBox.addActionListener(this);
		rememberDisabledEventsBox = new JCheckBox("Include disabled transitions");
		rememberDisabledEventsBox.setToolTipText("Adds transitions to a new 'dump'-state for all transitions in a plant that are disabled by a specification");

		advancedBox.add(synchronizationTypeBox);
		advancedBox.add(expandForbiddenStatesBox);
		advancedBox.add(rememberDisabledEventsBox);
		this.add(advancedBox);
		*/
	}

	public void update(SynchronizationOptions synchronizationOptions)
	{
		synchronizationTypeBox.setSelectedItem(synchronizationOptions.getSynchronizationType());
		expandForbiddenStatesBox.setSelected(synchronizationOptions.expandForbiddenStates());
		rememberDisabledEventsBox.setSelected(synchronizationOptions.rememberDisabledEvents());

		if (!expandForbiddenStatesBox.isSelected())
		{
			rememberDisabledEventsBox.setEnabled(false);
			rememberDisabledEventsBox.setSelected(false);
		}
		else
		{
			rememberDisabledEventsBox.setEnabled(true);
		}
	}

	public void regain(SynchronizationOptions synchronizationOptions)
	{
		synchronizationOptions.setSynchronizationType((SynchronizationType) synchronizationTypeBox.getSelectedItem());
		synchronizationOptions.setExpandForbiddenStates(expandForbiddenStatesBox.isSelected());
		synchronizationOptions.setRememberDisabledEvents(rememberDisabledEventsBox.isSelected());
	}

	public void actionPerformed(ActionEvent e)
	{
		if (!expandForbiddenStatesBox.isSelected())
		{
			rememberDisabledEventsBox.setEnabled(false);
			rememberDisabledEventsBox.setSelected(false);
		}
		else
		{
			rememberDisabledEventsBox.setEnabled(true);
		}
	}
}

public class SynchronizationDialog
	implements ActionListener
{
	private JButton okButton;
	private JButton cancelButton;
	private SynchronizationOptions synchronizationOptions;
	SynchronizationDialogStandardPanel standardPanel;
	SynchronizationDialogAdvancedPanel advancedPanel;
	private JDialog dialog;
	private Frame parentFrame;

	/**
	 * Creates modal dialog box for input of synthesizer options.
	 */
	public SynchronizationDialog(Frame parentFrame, SynchronizationOptions synchronizationOptions)
	{
		dialog = new JDialog(parentFrame, true);    // modal
		this.parentFrame = parentFrame;
		this.synchronizationOptions = synchronizationOptions;

		dialog.setTitle("Synchronization options");
		dialog.setSize(new Dimension(400, 300));

		Container contentPane = dialog.getContentPane();

		standardPanel = new SynchronizationDialogStandardPanel();
		advancedPanel = new SynchronizationDialogAdvancedPanel();

		JTabbedPane tabbedPane = new JTabbedPane();

		tabbedPane.addTab("Standard options", null, standardPanel, "Standard options");
		tabbedPane.addTab("Advanced options", null, advancedPanel, "Advanced options");

		// buttonPanel
		JPanel buttonPanel = new JPanel();

		okButton = addButton(buttonPanel, "OK");
		cancelButton = addButton(buttonPanel, "Cancel");

		contentPane.add("Center", tabbedPane);
		contentPane.add("South", buttonPanel);
		Utility.setDefaultButton(dialog, okButton);

		// ** MF ** Fix to get the frigging thing centered
		Dimension dim = dialog.getMinimumSize();

		dialog.setLocation(Utility.getPosForCenter(dim));
		dialog.setResizable(false);
		update();
	}

	/**
	 * Updates the information in the dialog from what is recorded in synchronizationOptions.
	 * @see SynchronizationOptions
	 */
	public void update()
	{
		standardPanel.update(synchronizationOptions);
		advancedPanel.update(synchronizationOptions);
	}

	private JButton addButton(Container container, String name)
	{
		JButton button = new JButton(name);

		button.addActionListener(this);
		container.add(button);

		return button;
	}

	public void show()
	{
		dialog.setVisible(true);
	}

	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();

		if (source == okButton)
		{
			standardPanel.regain(synchronizationOptions);
			advancedPanel.regain(synchronizationOptions);
			synchronizationOptions.setDialogOK(true);

			if (synchronizationOptions.isValid())
			{
				dialog.setVisible(false);
				dialog.dispose();
			}
			else
			{
				JOptionPane.showMessageDialog(parentFrame, "Invalid combination", "Alert", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (source == cancelButton)
		{
			synchronizationOptions.setDialogOK(false);    // Already done...
			dialog.setVisible(false);
			dialog.dispose();
		}
	}
}
