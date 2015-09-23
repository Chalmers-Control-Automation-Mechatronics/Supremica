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

package org.supremica.gui.automataExplorer;

import org.supremica.automata.algorithms.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import org.supremica.automata.Automata;

public class AutomataStateViewer
        extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private Automata theAutomata;
    private AutomataSynchronizerHelper helper;
    private int[] currState;
    private AutomataEventList forwardEvents;
    private AutomataEventList backwardEvents;
    private AutomataExplorerController controller;
    private AutomataStateDisplayer stateDisplayer;
    private JSplitPane eventSplitter;
    private JSplitPane stateEventSplitter;
    private LinkedList<int[]> prevStates = new LinkedList<int[]>();
    private LinkedList<int[]> nextStates = new LinkedList<int[]>();

    public AutomataStateViewer(AutomataSynchronizerHelper helper)
    {
        setLayout(new BorderLayout());

        theAutomata = helper.getAutomata();
        this.helper = helper;
        forwardEvents = new AutomataEventList(this, helper, true);
        //backwardEvents = new AutomataEventList(this, helper, false);
        eventSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, forwardEvents, backwardEvents);
        stateDisplayer = new AutomataStateDisplayer(this, helper);
        stateEventSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stateDisplayer, eventSplitter);

        add(stateEventSplitter, BorderLayout.CENTER);
    }

    public void initialize()
    {
        eventSplitter.setDividerLocation(0.5);
        stateEventSplitter.setDividerLocation(0.6);
    }

    public void setCurrState(int[] newState)
    {
        setCurrState(newState, false);
    }

    private void setCurrState(int[] newState, boolean isUndo)
    {
        if (!isUndo)
        {
            if (currState != null)
            {
                prevStates.addLast(currState);
            }

            nextStates.clear();
        }

        currState = newState;

        update();
    }

    public void goToInitialState()
    {
        prevStates.clear();

        currState = null;

        helper.getCoExecuter().setCurrState(AutomataExplorerHelper.getInitialState());
        setCurrState(AutomataExplorerHelper.getInitialState(), false);
    }

    public void undoState()
    {
        if (prevStates.size() > 0)
        {
            int[] newState = prevStates.removeLast();

            nextStates.addFirst(currState);
            setCurrState(newState, true);
        }
    }

    public boolean undoEnabled()
    {
        return prevStates.size() > 0;
    }

    public void redoState()
    {
        if (nextStates.size() > 0)
        {
            int[] newState = nextStates.removeFirst();

            prevStates.addLast(currState);
            setCurrState(newState, true);
        }
    }

    public boolean redoEnabled()
    {
        return nextStates.size() > 0;
    }

    public void update()
    {
        // The order of theese is important(?), for states to be properly
        // forbidden...
        forwardEvents.setCurrState(currState);
        //backwardEvents.setCurrState(currState);
        stateDisplayer.setCurrState(currState);
        controller.update();
    }

    public void setController(AutomataExplorerController controller)
    {
        this.controller = controller;
    }
}
