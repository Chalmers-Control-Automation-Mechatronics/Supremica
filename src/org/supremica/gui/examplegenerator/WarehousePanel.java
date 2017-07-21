//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.warehouse.SelectEventsWindow;
import org.supremica.testcases.warehouse.Warehouse;

class WarehousePanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	Warehouse warehouse = new Warehouse();
	IntegerField nbr_events_k = new IntegerField("3", 6);
	IntegerField nbr_events_m = new IntegerField("1", 6);
	SelectEventsWindow selectOperatorEventsWindow = null;
	SelectEventsWindow selectUnobservableEventsWindow = null;

	WarehousePanel() {
		JPanel panel = new JPanel(new GridLayout(3, 2));

		add(panel, BorderLayout.WEST);
		panel.add(new JLabel("Number of operator events (k): "));
		panel.add(nbr_events_k);
		panel.add(new JLabel("Number of supervisor events (m): "));
		panel.add(nbr_events_m);

		JButton selectOperatorEventsButton = new JButton(
				"Select operator events");

		selectOperatorEventsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectOperatorEventsWindow == null) {
					selectOperatorEventsWindow = new SelectEventsWindow(
							warehouse.getTruckAlphabet(),
							"Select operator events", "Select operator events",
							true);
				}

				selectOperatorEventsWindow.actionPerformed(e);

				// ActionMan.fileOpen(ActionMan.getGui());
			}
		});
		panel.add(selectOperatorEventsButton);

		// JButton selectControlEventsButton = new JButton("Select control
		// events");
		// panel.add(selectControlEventsButton);
		JButton selectUnobservableEventsButton = new JButton(
				"Select unobservable events");

		selectUnobservableEventsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectUnobservableEventsWindow == null) {
					selectUnobservableEventsWindow = new SelectEventsWindow(
							warehouse.getTruckAlphabet(),
							"Select unobservable events",
							"Select unobservable events", false);
				}

				selectUnobservableEventsWindow.actionPerformed(e);

				// ActionMan.fileOpen(ActionMan.getGui());
			}
		});
		panel.add(selectUnobservableEventsButton);
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		warehouse.setK(nbr_events_k.get());
		warehouse.setM(nbr_events_m.get());

		// System.err.println("Warehouse doIt");
		return warehouse.getProject();
	}
}
