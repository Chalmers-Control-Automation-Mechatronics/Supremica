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

package org.supremica.gui.simulator;

import org.supremica.automata.algorithms.*;
import java.awt.*;
import javax.swing.*;
import org.supremica.automata.Project;
import org.supremica.automata.Automata;

public class SimulatorStateViewer
    extends JPanel

//      implements SignalObserver
{
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
	private Automata theAutomata;
    private Project theProject;
    @SuppressWarnings("unused")
	private AutomataSynchronizerHelper helper;

//      private int[] currState;
    private SimulatorEventList forwardEvents;

//      private SimulatorEventList backwardEvents;
    @SuppressWarnings("unused")
	private SimulatorExecuterController controller;
    private SimulatorStateDisplayer stateDisplayer;

//      private JSplitPane eventSplitter;
    private JSplitPane stateEventSplitter;

//      private LinkedList prevStates = new LinkedList();
//      private LinkedList nextStates = new LinkedList();
    @SuppressWarnings("unused")
	private SimulatorExecuter simulator;
    private EventExecuter theExecuter;
    private boolean executerIsExternal;

    public SimulatorStateViewer(SimulatorExecuter simulator, AutomataSynchronizerHelper helper, boolean executerIsExternal)
    {
        setLayout(new BorderLayout());

        this.simulator = simulator;
        theProject = simulator.getProject();
        theAutomata = helper.getAutomata();
        this.helper = helper;
        forwardEvents = new SimulatorEventList(simulator, helper, theProject);

        //backwardEvents = new SimulatorEventList(this, helper, theProject, theAnimationSignals, true);
        //eventSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, forwardEvents, backwardEvents);
        stateDisplayer = new SimulatorStateDisplayer(helper);
        stateEventSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stateDisplayer, forwardEvents);

        add(stateEventSplitter, BorderLayout.CENTER);

        //theAnimationSignals.registerInterest(this);
        this.executerIsExternal = executerIsExternal;
        theExecuter = executerIsExternal
            ? new ExternalEventExecuter(simulator, forwardEvents.getEventListModel())
            : new EventExecuter(simulator, forwardEvents.getEventListModel());
    }

    public void initialize()
    {

//              eventSplitter.setDividerLocation(0.5);
        stateEventSplitter.setDividerLocation(0.6);
        theExecuter.start();
    }

    /** we do not use the built in executer /Arash */
    public boolean isExternal()
    {
        return executerIsExternal;
    }

//      public void setCurrState(int[] newState)
//      {
//              setCurrState(newState, false);
//      }
//      private void setCurrState(int[] newState, boolean isUndo)
//      {
//              if (!isUndo)
//              {
//                      if (currState != null)
//                      {
//                              prevStates.addLast(currState);
//                      }
//
//                      nextStates.clear();
//              }
//
//              currState = newState;
//
//              update();
//      }
//      public void goToInitialState()
//      {
//              prevStates.clear();
//
//              currState = null;
//
//              helper.getCoExecuter().setCurrState(SimulatorExecuterHelper.getInitialState());
//              setCurrState(SimulatorExecuterHelper.getInitialState(), false);
//              simulator.resetAnimation();
//      }
//
//      public void undoState()
//      {
//              if (prevStates.size() > 0)
//              {
//                      int[] newState = (int[]) prevStates.removeLast();
//
//                      nextStates.addFirst(currState);
//                      setCurrState(newState, true);
//              }
//      }
//      public boolean undoEnabled()
//      {
//              return prevStates.size() > 0;
//      }
//
//      public void redoState()
//      {
//              if (nextStates.size() > 0)
//              {
//                      int[] newState = (int[]) nextStates.removeFirst();
//
//                      prevStates.addLast(currState);
//                      setCurrState(newState, true);
//              }
//      }
//
//      public boolean redoEnabled()
//      {
//              return nextStates.size() > 0;
//      }
//      public void update()
//      {
    // The order of theese are changed, for states to be properly forbidden...
//              forwardEvents.setCurrState(currState);
//              backwardEvents.setCurrState(currState);
//              stateDisplayer.setCurrState(currState);
//              controller.update();
//      }
//      public void signalUpdated()
//      {
//              update();
//      }
//      public void executeEvent(LabeledEvent event, int[] newState)
//      {
//              simulator.executeEvent(event);
//              setCurrState(newState);
//      }
    public void setController(SimulatorExecuterController controller)
    {
        this.controller = controller;
    }

    public void executeControllableEvents(boolean doExecute)
    {
        theExecuter.executeControllableEvents(doExecute);
    }

    public void executeUncontrollableEvents(boolean doExecute)
    {
        theExecuter.executeUncontrollableEvents(doExecute);
    }

    public void close()
    {
        if (theExecuter != null)
        {
            theExecuter.requestStop();
        }
    }

//      public SimulatorExecuter getSimulatorExecuter()
//      {
//              return simulator;
//      }
}
