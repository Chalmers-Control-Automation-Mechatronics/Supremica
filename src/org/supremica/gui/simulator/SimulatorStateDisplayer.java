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

package org.supremica.gui.simulator;

import org.supremica.automata.algorithms.*;
import java.awt.*;
import javax.swing.*;
import org.supremica.automata.AutomataIndexFormHelper;

public class SimulatorStateDisplayer
	extends JPanel
{
    private static final long serialVersionUID = 1L;

//      private SimulatorStateViewer stateViewer;
//      private Automata theAutomata;
	private JCheckBox isInitialBox = new JCheckBox("initial");
	private JCheckBox isAcceptingBox = new JCheckBox("accepting");
	private JCheckBox isForbiddenBox = new JCheckBox("forbidden");
	private JLabel stateCost = new JLabel();
	private JLabel stateId = new JLabel();
	private JLabel stateName = new JLabel();
	private AutomataSynchronizerHelper helper;

	public SimulatorStateDisplayer(AutomataSynchronizerHelper helper)
	{
		setLayout(new BorderLayout());

//              this.stateViewer = stateViewer;
//              this.theAutomata = helper.getAutomata();
		this.helper = helper;

		JLabel header = new JLabel("Current composite state");

		add(header, BorderLayout.NORTH);

		Box statusBox = new Box(BoxLayout.Y_AXIS);

		isInitialBox.setEnabled(false);
		isInitialBox.setBackground(Color.white);
		statusBox.add(isInitialBox);
		isAcceptingBox.setEnabled(false);
		isAcceptingBox.setBackground(Color.white);
		statusBox.add(isAcceptingBox);
		isForbiddenBox.setEnabled(false);
		isForbiddenBox.setBackground(Color.white);
		statusBox.add(isForbiddenBox);
		statusBox.add(stateCost);
		statusBox.add(stateId);
		statusBox.add(stateName);

		JScrollPane boxScroller = new JScrollPane(statusBox);

		add(boxScroller, BorderLayout.CENTER);

		JViewport vp = boxScroller.getViewport();

		vp.setBackground(Color.white);
	}

	public void setCurrState(int[] currState)
	{
		helper.addStatus(currState);

		if (!helper.getCoExecuter().isControllable())
		{
			helper.setForbidden(currState, true);
		}

		isInitialBox.setSelected(AutomataIndexFormHelper.isInitial(currState));
		isAcceptingBox.setSelected(AutomataIndexFormHelper.isAccepting(currState));
		isForbiddenBox.setSelected(AutomataIndexFormHelper.isForbidden(currState));
	}
}
