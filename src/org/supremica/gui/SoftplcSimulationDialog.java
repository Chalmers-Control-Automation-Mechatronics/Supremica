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

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

import org.supremica.properties.Config;

public class SoftplcSimulationDialog
	extends JDialog
{
	private static final long serialVersionUID = 1L;
	private boolean ok = false;
	private final JComboBox<Object> interfaces;
	private final Vector<Object> interfacesVector = new Vector<Object>();

	public SoftplcSimulationDialog(final Frame frame, final String title, final boolean modal)
	{
		super(frame, title, modal);

		final JPanel panel1 = new JPanel();
		final GridBagLayout gridBagLayout1 = new GridBagLayout();
		final JLabel cycleTimeLabel = new JLabel("Cycle time (ms)");
		final JLabel runSimulationLabel = new JLabel("Run simulation...");
		final JTextField cycleTime = new JTextField();
		final JLabel interfaceLabel = new JLabel("I/O interface");
		final JButton cancelButton = new JButton("Cancel");
		final JButton simulateButton = new JButton("Simulate");
		final JLabel tempLabel = new JLabel(" ");

		interfacesVector.add(Config.SOFTPLC_INTERFACES.get()); // TO DO - Divide string into multiple entries - they are separated by colon
		interfaces = new JComboBox<Object>(interfacesVector);

		try
		{
			panel1.setLayout(gridBagLayout1);
			runSimulationLabel.setFont(new java.awt.Font("Dialog", 1, 18));
			cancelButton.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(final ActionEvent e)
				{
					cancelButton_actionPerformed(e);
				}
			});
			simulateButton.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(final ActionEvent e)
				{
					simulateButton_actionPerformed(e);
				}
			});
			interfaces.setBackground(Color.white);
			panel1.setBackground(Color.white);
			getContentPane().add(panel1);
			panel1.add(cycleTimeLabel, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(7, 0, 0, 0), 0, 0));
			panel1.add(cycleTime, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 67, 0));
			panel1.add(interfaces, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			panel1.add(interfaceLabel, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(12, 0, 4, 0), 0, 0));
			panel1.add(runSimulationLabel, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(7, 2, 4, 0), 77, 12));
			panel1.add(cancelButton, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(17, 0, 25, 0), 0, 0));
			panel1.add(simulateButton, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(19, 0, 28, 0), 0, 0));
			panel1.add(tempLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 29, 0));
			pack();
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public boolean showDialog()
	{
		this.setVisible(true);

		return ok;
	}

	public org.supremica.gui.SoftplcInterface getIOInterface()
	{
		return (org.supremica.gui.SoftplcInterface) interfacesVector.get(interfaces.getSelectedIndex());
	}

	void cancelButton_actionPerformed(final ActionEvent e)
	{
		this.setVisible(false);

		ok = false;
	}

	void simulateButton_actionPerformed(final ActionEvent e)
	{
		this.setVisible(false);

		ok = true;
	}
}
