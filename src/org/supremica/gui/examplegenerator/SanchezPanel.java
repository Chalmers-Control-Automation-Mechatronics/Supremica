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

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.SanchezTestCase;

class SanchezPanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	IntegerField int_blocks = null;
	JComboBox<String> choice = null;
	static final String[] choice_items = { "#1: Async prod", "#2: Synch prod",
			"#3: SupC" };

	public SanchezPanel() {
		final JPanel panel = new JPanel(new GridLayout(3, 2));

		add(panel, BorderLayout.NORTH);
		panel.add(new JLabel("Ref: 'A Comparision of Synthesis",
				SwingConstants.RIGHT));
		panel.add(new JLabel(" Tools For...', A. Sanchez et. al.",
				SwingConstants.LEFT));
		panel.add(new JLabel("Number of blocks: "));
		panel.add(int_blocks = new IntegerField("5", 3));
		panel.add(new JLabel("Benchmark: "));
		panel.add(choice = new JComboBox<String> (choice_items));
	}

	public void synthesizeSupervisor(final IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		final int p = int_blocks.get();
		final int type = choice.getSelectedIndex();
		final SanchezTestCase stc = new SanchezTestCase(p, type);

		return stc.getProject();
	}
}
