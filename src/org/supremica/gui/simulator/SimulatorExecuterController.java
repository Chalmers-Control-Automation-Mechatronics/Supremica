//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2020 Knut Akesson, Martin Fabian, Robi Malik
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class SimulatorExecuterController
    extends JPanel
{
    private static final long serialVersionUID = 1L;

    private SimulatorStateViewer stateViewer;

//      private Automata theAutomata;
//      private JButton undoButton;
//      private JButton redoButton;
    private JCheckBox executeUncontrollableEvents;
    private JCheckBox executeControllableEvents;

    public SimulatorExecuterController(SimulatorStateViewer stateViewer, boolean executerIsExternal)
    {
        setLayout(new BorderLayout());

        this.stateViewer = stateViewer;

//              this.theAutomata = synchHelper.getAutomata();
        Box redoBox = new Box(BoxLayout.Y_AXIS);

//              ImageIcon forwardImg = new ImageIcon(SimulatorExecuterController.class.getResource("/toolbarButtonGraphics/navigation/Forward24.gif"));
//              ImageIcon backwardImg = new ImageIcon(SimulatorExecuterController.class.getResource("/toolbarButtonGraphics/navigation/Back24.gif"));
//              ImageIcon homeImg = new ImageIcon(SimulatorExecuterController.class.getResource("/toolbarButtonGraphics/navigation/Home24.gif"));
        executeUncontrollableEvents = new JCheckBox("Automatically execute uncontrollable events");
        executeControllableEvents = new JCheckBox("Automatically execute controllable events");

//              undoButton = new JButton(backwardImg);
//              undoButton.setToolTipText("Back");
//              redoButton = new JButton(forwardImg);
//              redoButton.setToolTipText("Forward");
//              JButton resetButton = new JButton(homeImg);
//              resetButton.setToolTipText("Go to the initial state");
        redoBox.add(executeUncontrollableEvents);
        redoBox.add(executeControllableEvents);

//              redoBox.add(Box.createHorizontalGlue());
//              redoBox.add(Box.createHorizontalGlue());
////            redoBox.add(undoButton);
//              redoBox.add(Box.createHorizontalGlue());
////            redoBox.add(redoButton);
//              redoBox.add(Box.createHorizontalGlue());
////            redoBox.add(resetButton);
//              redoBox.add(Box.createHorizontalGlue());
//              redoBox.add(Box.createHorizontalGlue());
        add(redoBox, BorderLayout.NORTH);

        if (executerIsExternal)
        {
            executeUncontrollableEvents.setEnabled(false);
            executeControllableEvents.setEnabled(false);
        }

        executeUncontrollableEvents.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                executeUncontrollableEvents_actionPerformed(e);
            }
        });
        executeControllableEvents.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                executeControllableEvents_actionPerformed(e);
            }
        });

//              undoButton.addActionListener(new ActionListener()
//              {
//                      public void actionPerformed(ActionEvent e)
//                      {
//                              undo_actionPerformed(e);
//                      }
//              });
//              redoButton.addActionListener(new ActionListener()
//              {
//                      public void actionPerformed(ActionEvent e)
//                      {
//                              redo_actionPerformed(e);
//                      }
//              });
//              resetButton.addActionListener(new ActionListener()
//              {
//                      public void actionPerformed(ActionEvent e)
//                      {
//                              reset_actionPerformed(e);
//                      }
//              });
    }

//      public void reset_actionPerformed(ActionEvent e)
//      {
//              stateViewer.goToInitialState();
//
//              // stateViewer.initialize();
//      }
//
//      public void undo_actionPerformed(ActionEvent e)
//      {
//              stateViewer.undoState();
//      }
//
//      public void redo_actionPerformed(ActionEvent e)
//      {
//              stateViewer.redoState();
//      }
    public void executeUncontrollableEvents_actionPerformed(ActionEvent e)
    {
        stateViewer.executeUncontrollableEvents(executeUncontrollableEvents.isSelected());
    }

    public void executeControllableEvents_actionPerformed(ActionEvent e)
    {
        stateViewer.executeControllableEvents(executeControllableEvents.isSelected());
    }

//      public void update()
//      {
//              undoButton.setEnabled(stateViewer.undoEnabled());
//              redoButton.setEnabled(stateViewer.redoEnabled());
//      }
}
