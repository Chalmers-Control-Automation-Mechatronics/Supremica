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

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import org.supremica.automata.algorithms.Guard.*;


/**
 *
 * @author Sajed
 */
abstract class GuardPanel
	extends JPanel
{
    private static final long serialVersionUID = 1L;

    public abstract void update(GuardOptions s);

	public abstract void regain(GuardOptions s);
}

class GuardDialogStandardPanel
	extends GuardPanel
{
	private static final long serialVersionUID = 1L;
    private final JRadioButton fromAllowedStatesButton;
    private final JRadioButton fromForbiddenStatesButton;
    private final JRadioButton optimalButton;
    private final JComboBox<Object> eventList;
//	private JTextField eventField;

	public GuardDialogStandardPanel(final Vector<Object> events)
	{
		final Box standardBox = Box.createVerticalBox();

		fromAllowedStatesButton = new JRadioButton("From allowed states");
		fromAllowedStatesButton.setToolTipText("Generate the guard from the Allowed state set");

        fromForbiddenStatesButton = new JRadioButton("From forbidden states");
		fromForbiddenStatesButton.setToolTipText("Generate the guard from the Forbidden state set");

        optimalButton = new JRadioButton("Optimal solution");
		optimalButton.setToolTipText("Generate the guard from the state set that yields the best result");

		final JLabel event = new JLabel("Events");
//		eventField = new JTextField(15);
//		eventField.setToolTipText("The name of the desired event");

        final ButtonGroup group = new ButtonGroup();
        group.add(fromAllowedStatesButton);
        group.add(fromForbiddenStatesButton);
        group.add(optimalButton);

        final JPanel expressionTypePanel = new JPanel();
        expressionTypePanel.add(fromAllowedStatesButton);
        expressionTypePanel.add(fromForbiddenStatesButton);
        expressionTypePanel.add(optimalButton);

        eventList = new JComboBox<Object>(events);

        standardBox.add(expressionTypePanel);
		standardBox.add(event);
//        standardBox.add(eventField);
        standardBox.add(eventList);

		this.add(standardBox);
	}

	public void update(final GuardOptions guardOptions)
	{
        if(guardOptions.getExpressionType() == 0)
        {
            fromAllowedStatesButton.setSelected(false);
            fromForbiddenStatesButton.setSelected(true);
            optimalButton.setSelected(false);
        }
        else if(guardOptions.getExpressionType() == 1)
        {
            fromAllowedStatesButton.setSelected(true);
            fromForbiddenStatesButton.setSelected(false);
            optimalButton.setSelected(false);
        }
        else if(guardOptions.getExpressionType() == 2)
        {
            fromAllowedStatesButton.setSelected(false);
            fromForbiddenStatesButton.setSelected(false);
            optimalButton.setSelected(true);
        }

//        eventField.setText(guardOptions.getEvent());
	}

	public void regain(final GuardOptions guardOptions)
	{
        if(fromForbiddenStatesButton.isSelected())
        {
            guardOptions.setExpressionType(0);
        }
        if(fromAllowedStatesButton.isSelected())
        {
            guardOptions.setExpressionType(1);
        }
        if(optimalButton.isSelected())
        {
            guardOptions.setExpressionType(2);
        }

        if(eventList.getSelectedIndex() == 0)
            guardOptions.setEvent("");
        else
            guardOptions.setEvent((String)eventList.getSelectedItem());
	}

}

public class GuardDialog
	implements ActionListener
{
	private final JButton okButton;
	private final JButton cancelButton;
	GuardDialogStandardPanel standardPanel;
        private final GuardOptions guardOptions;
	private final JDialog dialog;
	private final Frame parentFrame;


	public GuardDialog(final Frame parentFrame, final GuardOptions guardOptions, final Vector<Object> events)
	{
		dialog = new JDialog(parentFrame, true);    // modal
		this.parentFrame = parentFrame;
        this.guardOptions = guardOptions;

		dialog.setTitle("Guard options");
		dialog.setSize(new Dimension(400, 200));

		final Container contentPane = dialog.getContentPane();

		standardPanel = new GuardDialogStandardPanel(events);

		final JTabbedPane tabbedPane = new JTabbedPane();

		tabbedPane.addTab("Standard options", null, standardPanel, "Standard options");

		// buttonPanel
		final JPanel buttonPanel = new JPanel();

		okButton = addButton(buttonPanel, "OK");
		cancelButton = addButton(buttonPanel, "Cancel");

		contentPane.add("Center", tabbedPane);
		contentPane.add("South", buttonPanel);
		Utility.setDefaultButton(dialog, okButton);

		final Dimension dim = dialog.getMinimumSize();

		dialog.setLocation(Utility.getPosForCenter(dim));
		dialog.setResizable(false);
		update();
	}


	public void update()
	{
		standardPanel.update(guardOptions);
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

	public void actionPerformed(final ActionEvent event)
	{
		final Object source = event.getSource();

		if (source == okButton)
		{
			standardPanel.regain(guardOptions);

			if (guardOptions.isValid())
			{
				dialog.setVisible(false);
				dialog.dispose();
                guardOptions.setDialogOK(true);
			}
			else
			{
				JOptionPane.showMessageDialog(parentFrame, "Invalid combination", "Alert", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (source == cancelButton)
		{
			guardOptions.setDialogOK(false);    // Already done...
			dialog.setVisible(false);
			dialog.dispose();
		}
	}
}
