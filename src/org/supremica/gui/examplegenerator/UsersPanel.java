//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2023 Knut Akesson, Martin Fabian, Robi Malik
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
import javax.swing.Box;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.Users;

class UsersPanel extends JPanel implements TestCase
{
	private static final long serialVersionUID = 1L;
	IntegerField int_num = null;
	IntegerField int_rsc = null;
	JCheckBox req = new JCheckBox("request (a)");
	JCheckBox acc = new JCheckBox("access  (b)", true);
	JCheckBox rel = new JCheckBox("release (c)");
	private static Logger logger = LogManager.getLogger(UsersPanel.class);

	public UsersPanel()
    {
		JPanel cont = new JPanel();

		cont.setBorder(BorderFactory.createTitledBorder("Controllability"));
		cont.add(req);
		cont.add(acc);
		cont.add(rel);

		JPanel num_users = new JPanel();
		num_users.add(new JLabel("Number of resources: "));
		num_users.add(int_rsc = new IntegerField("1", 6));
		num_users.add(new JLabel("Number of users: "));
		num_users.add(int_num = new IntegerField("3", 6));

        Box theBox = Box.createVerticalBox();
		theBox.add(cont);
		theBox.add(num_users);
		add(theBox, BorderLayout.NORTH);

	}
	@Override
	public void synthesizeSupervisor(IDE ide)
	{
		logger.warn("No direct synthesis in this test case");
	}

	/*
	 * Only one project is created, so we rely on default implementation of howMany()
	 * and disregard the n sent to generateAutomata
	 */
	@Override
	public Project generateAutomata(int n) throws Exception
	{
		Users users = new Users(int_num.get(), int_rsc.get(), req.isSelected(),
				acc.isSelected(), rel.isSelected());

		return users.getProject();
	}
}
