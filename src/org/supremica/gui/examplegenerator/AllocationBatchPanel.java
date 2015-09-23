//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.AllocationBatch;
import org.supremica.util.SupremicaException;

class AllocationBatchPanel extends JPanel implements TestCase, ActionListener {
	private static final long serialVersionUID = 1L;
	JTextField filename;
	JButton browse;

	AllocationBatchPanel() {
		super(new BorderLayout(10, 10));

		JPanel pCenter = new JPanel(new GridLayout(4, 2));

		add(pCenter, BorderLayout.WEST);
		pCenter.add(new JLabel("batch file:  "));
		pCenter.add(filename = new JTextField(20));
		pCenter.add(browse = new JButton("..."));
		browse.addActionListener(this);
		add(pCenter, BorderLayout.CENTER);
		add(new JLabel("Experimental serialized allocation batch"),
				BorderLayout.NORTH);
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		String file = filename.getText();

		if (file.length() > 0) {
			AllocationBatch ab = new AllocationBatch(file);

			return ab.getProject();
		} // else...

		throw new SupremicaException("you must choose a filename");
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if (src == browse) {
			JFileChooser chooser = new JFileChooser();

			chooser.setDialogTitle("Please choose a batch file");

			int returnVal = chooser.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				filename.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		}
	}
}
