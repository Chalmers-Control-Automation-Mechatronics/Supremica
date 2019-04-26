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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.CatMouse;


class CatMousePanel extends JPanel implements TestCase, ActionListener
{
	private static final long serialVersionUID = 1L;
	protected final IntegerField int_num = new IntegerField("1", 6);
	protected final IntegerField int_step_cats = new IntegerField("0", 6);
	protected final IntegerField int_numberOfInstances = new IntegerField("1", 6);
	protected final JCheckBox multiple = new JCheckBox("Multiple instances", false);
	protected final JCheckBox selfloops = new JCheckBox("Use forbidden self-loops", false);
	protected final JPanel num_users;
	private final JPanel steps;
	protected final Box theBox;
	protected final JPanel numberOfInstances;

	private static final Logger logger = LogManager.getLogger(CatMousePanel.class);

	public CatMousePanel()
	{
		// super(new GridLayout(2, 1, 10, 10));
		super();

		num_users = new JPanel();
		num_users.add(new JLabel("Number of cats (and mice): "),
				BorderLayout.NORTH);
		num_users.add(int_num, BorderLayout.NORTH);

		final JPanel multiplePanel = new JPanel();
		multiplePanel.add(selfloops);
		multiplePanel.add(multiple);

		selfloops.setToolTipText("Use forbidden self-loops instead of room specs");

		multiple.addActionListener(this);

		steps = new JPanel();
		steps
				.add(
						new JLabel(
								"step (increasement of number of cats (and mice) for each instance): "),
						BorderLayout.NORTH);
		steps.add(int_step_cats, BorderLayout.SOUTH);
		int_step_cats.setEnabled(false);

		numberOfInstances = new JPanel();
		numberOfInstances.add(new JLabel("Number of instances: "),
				BorderLayout.NORTH);
		numberOfInstances.add(int_numberOfInstances, BorderLayout.SOUTH);
		int_numberOfInstances.setEnabled(false);

		theBox = Box.createVerticalBox();
		theBox.add(num_users);
		theBox.add(multiplePanel);
		theBox.add(steps);
		theBox.add(numberOfInstances);
		add(theBox, BorderLayout.NORTH);
	}

	@Override
	public void synthesizeSupervisor(final IDE ide) throws Exception {
		// TODO: implement this one
		logger.warn("Not implemented");
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		if (multiple.isSelected())
		{
			int_step_cats.setEnabled(true);
			int_numberOfInstances.setEnabled(true);
		}
		else
		{
			int_step_cats.setEnabled(false);
			int_numberOfInstances.setEnabled(false);
		}
	}

	@Override
	public Project generateAutomata() throws Exception
	{
		final CatMouse cm = new CatMouse(int_num.get(), selfloops.isSelected());
		return cm.getProject();
	}

	// For debug only
	public static void main(final String[] args)
	{
		final CatMousePanel cmp = new CatMousePanel();
		javax.swing.JOptionPane.showMessageDialog(null, cmp, "CatMousePanel.java", javax.swing.JOptionPane.PLAIN_MESSAGE);
		System.out.println("Number of cats (and mice): " + cmp.int_num.get());
		System.out.println("Use forbidden self-loops: " + (cmp.selfloops.isSelected() ? "true" : "false"));
		System.out.println("Multiple instances: " + (cmp.multiple.isSelected() ? "true" : "false"));
		System.out.println("Step increment of number of cats (and mice): " + cmp.int_step_cats.get());
		System.out.println("Number of instances: " + cmp.int_numberOfInstances.get());
	}
}
