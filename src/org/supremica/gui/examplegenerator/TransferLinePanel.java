//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2019 Knut Akesson, Martin Fabian, Robi Malik
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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.TransferLine;
import org.supremica.util.SupremicaException;

class TransferLinePanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	IntegerField int_cap1 = null;
	IntegerField int_cap2 = null;
	IntegerField int_cells = null;

	// <<<<<<< TestCasesDialog.java
	// IntegerField int_caps = null; // Gromyko, Pistore, Traverno allow
	// arbitrary size of all resources, inc machines
	//
	// =======
	//
	// >>>>>>> 1.47

	public TransferLinePanel() {
		JPanel panel = new JPanel(new GridLayout(4, 2));

		add(panel, BorderLayout.CENTER);
		panel.add(new JLabel("Ref: 'Notes on Control of Discrete",
				SwingConstants.RIGHT));
		panel.add(new JLabel("-Event Systems', W.M. Wonham",
				SwingConstants.LEFT));
		panel.add(new JLabel("Number of cells: "));
		panel.add(int_cells = new IntegerField("3", 5));
		panel.add(new JLabel("Buffer 1 capacity: "));
		panel.add(int_cap1 = new IntegerField("3", 5));
		panel.add(new JLabel("Buffer 2 capacity: "));
		panel.add(int_cap2 = new IntegerField("1", 5));
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		int cap1 = int_cap1.get();
		int cap2 = int_cap2.get();

		if ((cap1 < 1) || (cap2 < 1)) {
			throw new SupremicaException("Buffer capacity must be at least 1");
		}

		TransferLine tl = new TransferLine(int_cells.get(), cap1, cap2, false);

		return tl.getProject();
	}
}
