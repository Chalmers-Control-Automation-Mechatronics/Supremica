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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.DiningPhilosophers;


class PhilosPanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	IntegerField int_num = new IntegerField("5", 6);
	JCheckBox l_take = new JCheckBox("take left fork", true);
	JCheckBox r_take = new JCheckBox("take right fork", true);
	JCheckBox l_put = new JCheckBox("put left fork", true);
	JCheckBox r_put = new JCheckBox("put right fork", true);
	JCheckBox animation = new JCheckBox("Include animation (5 philos)", false);
	JCheckBox memory = new JCheckBox("Forks have memory", false);
	JCheckBox multiple = new JCheckBox("Multiple instances", false);
	Util util = new Util();

	public PhilosPanel() {
		// super(new GridLayout(2, 1, 10, 10));
		super();

		JPanel cont = new JPanel();
		// cont.setLayout(new BoxLayout());
		cont.setBorder(BorderFactory.createTitledBorder("Controllability"));
		cont.add(l_take);
		cont.add(r_take);
		cont.add(l_put);
		cont.add(r_put);

		JPanel num_users = new JPanel();
		num_users.add(new JLabel("Number of philosophers and forks: "),
				BorderLayout.NORTH);
		num_users.add(int_num, BorderLayout.NORTH);

		JPanel animationPanel = new JPanel();
		animationPanel.add(animation);
		animationPanel.add(memory);
		animationPanel.add(multiple);

		Box theBox = Box.createVerticalBox();
		theBox.add(cont);
		theBox.add(num_users);
		theBox.add(animationPanel);
		add(theBox, BorderLayout.NORTH);
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		DiningPhilosophers dp = new DiningPhilosophers(int_num.get(), l_take
				.isSelected(), r_take.isSelected(), l_put.isSelected(), r_put
				.isSelected(), animation.isSelected(), memory.isSelected());
		/*
		 * Iterator<LabeledEvent> uit;
		 * for(int i=0;i<dp.getProject().nbrOfAutomata();i++) {
		 * System.out.println("i: "+i); uit =
		 * dp.getProject().getAutomatonAt(i).getAlphabet().getUncontrollableAlphabet().iterator();
		 * while(uit.hasNext()) System.out.println(""+uit.next()); }
		 */
		return dp.getProject();
	}
}





