//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2020 Knut Akesson, Martin Fabian, Robi Malik
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.SynchronizationType;
import org.supremica.properties.Config;

abstract class SynchronizationPanel
	extends JPanel
{
    private static final long serialVersionUID = 1L;

    public abstract void update(SynchronizationOptions s);

	public abstract void regain(SynchronizationOptions s);
}
//--------------------------------------- Standard panel
class SynchronizationDialogStandardPanel
	extends SynchronizationPanel
{
	private static final long serialVersionUID = 1L;
	private final JCheckBox forbidUnconStatesBox;
	private final JCheckBox buildAutomatonBox;
	private final JCheckBox useShortStateNamesBox;
	private final JTextField stateNameSeparator;

	public SynchronizationDialogStandardPanel()
	{
		final Box standardBox = Box.createVerticalBox();

		forbidUnconStatesBox = new JCheckBox("Mark uncontrollable states as forbidden");
		forbidUnconStatesBox.setToolTipText("If checked, uncontrollable states become forbidden " +
											"in the synchronization");
		buildAutomatonBox = new JCheckBox("Build a full automaton model");
		buildAutomatonBox.setToolTipText("If not checked, the only output is statistics about the operation (saves computation)");
		useShortStateNamesBox = new JCheckBox("Use short state names");
		useShortStateNamesBox.setToolTipText("Give the states in the composition short, abstract names instead of keeping the original state names");

		final JLabel stateNameSeparatorLabel = new JLabel("State name separator");
		stateNameSeparator = new JTextField();
		stateNameSeparator.setToolTipText("The name of the synchronized state is a concatenation of the names of the states, separated by this string.");

		standardBox.add(forbidUnconStatesBox);
		standardBox.add(buildAutomatonBox);
		standardBox.add(useShortStateNamesBox);
		standardBox.add(stateNameSeparatorLabel);
		standardBox.add(stateNameSeparator);
		this.add(standardBox);
	}

	@Override
	public void update(final SynchronizationOptions synchronizationOptions)
	{
		forbidUnconStatesBox.setSelected(synchronizationOptions.forbidUncontrollableStates());
		buildAutomatonBox.setSelected(synchronizationOptions.buildAutomaton());
		useShortStateNamesBox.setSelected(synchronizationOptions.useShortStateNames());
		stateNameSeparator.setText(synchronizationOptions.getStateNameSeparator());
	}

	@Override
	public void regain(final SynchronizationOptions synchronizationOptions)
	{
		synchronizationOptions.setForbidUncontrollableStates(forbidUnconStatesBox.isSelected());
		synchronizationOptions.setBuildAutomaton(buildAutomatonBox.isSelected());
		synchronizationOptions.setUseShortStateNames(useShortStateNamesBox.isSelected());
		synchronizationOptions.setStateNameSeparator(stateNameSeparator.getText());
	}
}
//-------------------------------------- Advanced panel
class SynchronizationDialogAdvancedPanel
	extends SynchronizationPanel
	implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private final JComboBox<SynchronizationType> synchronizationTypeBox;
	private final JCheckBox expandForbiddenStatesBox;
	private final JCheckBox rememberDisabledEventsBox;
	private final JCheckBox unobsEventsSyncBox;

	public SynchronizationDialogAdvancedPanel()
	{
		this.setLayout(new BorderLayout());

		this.synchronizationTypeBox = new JComboBox<SynchronizationType>(SynchronizationType.values());
		this.synchronizationTypeBox.setToolTipText("Choose the type of composition");

		this.expandForbiddenStatesBox = new JCheckBox(Config.SYNC_EXPAND_FORBIDDEN_STATES.getShortName());
		this.expandForbiddenStatesBox.setToolTipText("If checked, transitions from forbidden states are " +
												"examined, otherwise, forbidden states are considered terminal");
		this.expandForbiddenStatesBox.addActionListener(this);

		this.rememberDisabledEventsBox = new JCheckBox("Include disabled transitions");
		this.rememberDisabledEventsBox.setToolTipText("Adds transitions to a new 'dump'-state for all transitions in a plant that are disabled by a specification");

		/*
		 * This setting gets its default value from what is set in the Config dialog
		 * Changing it on the SynchroniziationOptions dialog holds only for the current invocation.
		 * (and this is the way it should work for all settings on the dialogs)
		*/
		this.unobsEventsSyncBox = new JCheckBox(Config.SYNC_UNOBS_EVENTS_SYNC.getShortName());
		this.unobsEventsSyncBox.setToolTipText("If checked: " + Config.SYNC_UNOBS_EVENTS_SYNC.getDescription());
		this.unobsEventsSyncBox.setSelected(Config.SYNC_UNOBS_EVENTS_SYNC.getValue());

		final JPanel choicePanel = new JPanel();
		choicePanel.setLayout(new FlowLayout());
		choicePanel.add(synchronizationTypeBox);
		this.add("North", choicePanel);

		final JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new FlowLayout());
		final Box checkBoxBox = Box.createVerticalBox();
		//checkBoxBox.setLayout(new FlowLayout());
		checkBoxBox.add(expandForbiddenStatesBox);
		checkBoxBox.add(rememberDisabledEventsBox);
		checkBoxBox.add(unobsEventsSyncBox);
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

	@Override
	public void update(final SynchronizationOptions synchronizationOptions)
	{
		synchronizationTypeBox.setSelectedItem(synchronizationOptions.getSynchronizationType());
		expandForbiddenStatesBox.setSelected(synchronizationOptions.expandForbiddenStates());
		rememberDisabledEventsBox.setSelected(synchronizationOptions.rememberDisabledEvents());
		this.unobsEventsSyncBox.setSelected(synchronizationOptions.getUnobsEventsSynch());

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

	@Override
	public void regain(final SynchronizationOptions synchronizationOptions)
	{
		synchronizationOptions.setSynchronizationType((SynchronizationType) synchronizationTypeBox.getSelectedItem());
		synchronizationOptions.setExpandForbiddenStates(expandForbiddenStatesBox.isSelected());
		synchronizationOptions.setRememberDisabledEvents(rememberDisabledEventsBox.isSelected());
		synchronizationOptions.setUnobsEventsSynch(this.unobsEventsSyncBox.isSelected());
	}

	@Override
	public void actionPerformed(final ActionEvent e)
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
//-------------------------------------- SynchronizationDialog
public final class SynchronizationDialog
	implements ActionListener
{
	private final JButton okButton;
	private final JButton cancelButton;
	private final SynchronizationOptions synchronizationOptions;
	SynchronizationDialogStandardPanel standardPanel;
	SynchronizationDialogAdvancedPanel advancedPanel;
	private final JDialog dialog;
	private final Frame parentFrame;

	/**
	 * Creates modal dialog box for input of synchronous product options.
	 */
	public SynchronizationDialog(final Frame parentFrame,
	                             final SynchronizationOptions synchronizationOptions)
	{
		dialog = new JDialog(parentFrame, true);    // modal
		this.parentFrame = parentFrame;
		this.synchronizationOptions = synchronizationOptions;

		dialog.setTitle("Synchronization options");

		final Container contentPane = dialog.getContentPane();

		standardPanel = new SynchronizationDialogStandardPanel();
		advancedPanel = new SynchronizationDialogAdvancedPanel();

		final JTabbedPane tabbedPane = new JTabbedPane();

		tabbedPane.addTab("Standard options", null, standardPanel, "Standard options");
		tabbedPane.addTab("Advanced options", null, advancedPanel, "Advanced options");

		// buttonPanel
		final JPanel buttonPanel = new JPanel();

		okButton = addButton(buttonPanel, "OK");
		cancelButton = addButton(buttonPanel, "Cancel");

		contentPane.add("Center", tabbedPane);
		contentPane.add("South", buttonPanel);
		Utility.setDefaultButton(dialog, okButton);
        update();

		dialog.setLocationRelativeTo(parentFrame);
		dialog.pack();
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

	private JButton addButton(final Container container, final String name)
	{
		final JButton button = new JButton(name);

		button.addActionListener(this);
		container.add(button);

		return button;
	}

	public void show()
	{
		dialog.setVisible(true);
	}

	@Override
	public void actionPerformed(final ActionEvent event)
	{
		final Object source = event.getSource();

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

	//----------- Just for testing
	public static void main(final String[] args)
	{
		final SynchronizationOptions options = new SynchronizationOptions();
		final SynchronizationDialog dialog = new SynchronizationDialog(null, options);
		dialog.show();
		System.out.println("getUnobsEventsSynch : " + options.getUnobsEventsSynch());
	}
}
