//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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
import org.supremica.log.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import org.supremica.automata.Project;
import org.supremica.automata.Automata;
import org.supremica.automata.LabeledEvent;

public class SimulatorEventList
    extends JPanel
    implements ListDataListener
{
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(SimulatorEventList.class);
    @SuppressWarnings("unused")
	private final boolean showStateId = false;
    @SuppressWarnings("unused")
	private final Automata theAutomata;
    @SuppressWarnings("unused")
	private int[] currState;

//      private SimulatorStateViewer stateViewer;
    private final SimulatorExecuter theExecuter;
    private final SimulatorEventListModel eventsList;
    private final JList<Object> theList;

//      private boolean allowEventSelection = false;
    public SimulatorEventList(final SimulatorExecuter theExecuter, final AutomataSynchronizerHelper helper, final Project theProject)
    {
        setLayout(new BorderLayout());

//              this.stateViewer = stateViewer;
        this.theExecuter = theExecuter;
        this.theAutomata = helper.getAutomata();
        eventsList = new SimulatorEventListModel(theExecuter, helper, theProject);

//              allowEventSelection = true;
        theList = new JList<Object>(eventsList);

        final JScrollPane scrollPanel = new JScrollPane(theList);

        theList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JLabel jLabel = null;

//              if (showDisabledEvents)
//              {
//                      jLabel = new JLabel("Outgoing events");
//              }
//              else
//              {
        jLabel = new JLabel("Enabled events");

//              }
        add(jLabel, BorderLayout.NORTH);
        add(scrollPanel, BorderLayout.CENTER);
        theList.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(final MouseEvent e)
            {
                if (clickable())
                {
                    if (e.getClickCount() == 2)
                    {
                        final int index = theList.locationToIndex(e.getPoint());

                        if (index >= 0)
                        {

                            // KA : These two commands should probably be executed without interruption
                            // Try with a wrapper object
                            final LabeledEvent currEvent = eventsList.getEventAt(index);

                            executeEvent(currEvent);

//                                                      int[] newState = eventsList.getStateAt(index);
//                                                      if (!eventsList.executeEvent(currEvent))
//                                                      {
//                                                              logger.warn("Failed to execute event: " + currEvent.getLabel());
//                                                      }
//                                                      else
//                                                      {
//                                                              executeEvent(currEvent, newState);
//                                                      }
                        }
                    }
                }
            }
        });
    }

    public boolean clickable()
    {
        return !eventsList.isLocked();
    }

    public void setShowStateId(final boolean showStateId)
    {
        eventsList.setShowStateId(showStateId);
    }

//      public void setCurrState(int[] currState)
//      {
//              this.currState = currState;
//
//              theList.clearSelection();
//              update();
//      }
    public void update()
    {
        theList.clearSelection();

//              eventsList.setCurrState(currState);
    }

//      private void updateStateViewer(int[] newState)
//      {
//              stateViewer.setCurrState(newState);
//      }
//
//      private void executeEvent(LabeledEvent event)
//      {
//              stateViewer.executeEvent(event);
//      }
    private void executeEvent(final LabeledEvent currEvent)
    {
        theExecuter.executeEvent(currEvent);
    }

    public SimulatorEventListModel getEventListModel()
    {
        return eventsList;
    }

    public void contentsChanged(final ListDataEvent e)
    {
        update();
    }

    public void intervalAdded(final ListDataEvent e)
    {
        contentsChanged(e);
    }

    public void intervalRemoved(final ListDataEvent e)
    {
        contentsChanged(e);
    }
}
