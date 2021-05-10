//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui.automataExplorer;

import org.supremica.automata.algorithms.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.automata.Automata;

public class AutomataEventList
        extends JPanel
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private final boolean forward;
    @SuppressWarnings("unused")
	private final boolean showStateId = false;
    @SuppressWarnings("unused")
	private final Automata theAutomata;
    private int[] currState;
    private final AutomataStateViewer stateViewer;
    private final AutomataEventListModel eventsList;
    private final JList<Object> theList;

    public AutomataEventList(final AutomataStateViewer stateViewer, final AutomataSynchronizerHelper helper, final boolean forward)
    {
        setLayout(new BorderLayout());

        this.stateViewer = stateViewer;
        this.theAutomata = helper.getAutomata();
        this.forward = forward;
        eventsList = new AutomataEventListModel(helper, forward);
        theList = new JList<Object>(eventsList);

        final JScrollPane scrollPanel = new JScrollPane(theList);

        theList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        String label;

        if (forward)
        {
            label = "Outgoing events";
        }
        else
        {
            label = "Incoming events";
        }

        final JLabel jLabel = new JLabel(label);

        // jLabel.setOpaque(true);
        // jLabel.setBackground(Color.yellow);
        add(jLabel, BorderLayout.NORTH);
        add(scrollPanel, BorderLayout.CENTER);
        theList.addMouseListener(new MouseAdapter()
        {
			@Override
            public void mouseClicked(final MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
                {
					System.out.println("!mouseClicked: " + e.toString());
                    final int index = theList.locationToIndex(e.getPoint());

                    if (index >= 0)
                    {
                        final int[] newState = eventsList.getStateAt(currState, index);

                        updateStateViewer(newState);
                    }
                }
            }
        });	
    }

    public void setShowStateId(final boolean showStateId)
    {
        eventsList.setShowStateId(showStateId);
    }

    public void setCurrState(final int[] currState)
    {
        this.currState = currState;

        theList.clearSelection();
        update();
    }

    public void update()
    {
        eventsList.setCurrState(currState);
    }

    private void updateStateViewer(final int[] newState)
    {
        stateViewer.setCurrState(newState);
    }
}
