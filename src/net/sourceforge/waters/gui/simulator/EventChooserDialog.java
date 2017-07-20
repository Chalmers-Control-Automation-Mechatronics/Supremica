//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.simulator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.util.IconLoader;


public class EventChooserDialog extends JDialog
{
  // #######################################################################
  // # Constructor

  public EventChooserDialog(final JFrame owner, final JLabel[] labels, final SimulatorStep[] correspondingEvent)
  {
    super(owner, "Multiple Options available", true);
    cancelled = true;
    final JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    mList = new JList<JLabel>(labels);
    mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mList.setSelectedIndex(0);
    mList.setCellRenderer(new ListCellRenderer<JLabel>(){
      public Component getListCellRendererComponent(final JList<? extends JLabel> list, final JLabel value,
          final int index, final boolean isSelected, final boolean cellHasFocus)
      {
        final JLabel output = (JLabel) value;
        output.setOpaque(true);
        if (isSelected)
          output.setBackground(EditorColor.BACKGROUND_FOCUSSED);
        else
          output.setBackground(EditorColor.BACKGROUNDCOLOR);
        return output;
      }
    });
    mList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    eventList = correspondingEvent;
    final JScrollPane scrollPane = new JScrollPane(mList);
    panel.add(scrollPane, BorderLayout.CENTER);
    final JPanel buttonPanel = new JPanel();
    final JButton selectButton = new JButton("Select Event");
    final JButton cancelButton = new JButton("Cancel");
    final double maximumWidth = Math.max(selectButton.getPreferredSize().getWidth(), cancelButton.getPreferredSize().getWidth());
    selectButton.setPreferredSize(new Dimension((int)maximumWidth, (int)selectButton.getPreferredSize().getHeight()));
    cancelButton.setPreferredSize(new Dimension((int)maximumWidth, (int)cancelButton.getPreferredSize().getHeight()));
    final GridBagLayout layout = new GridBagLayout();
    final int[] a = new int[]{(int)((DEFAULT_LIST_WIDTH - maximumWidth * 2) / 2),
      (int)maximumWidth,
      (int)maximumWidth,
      (int)((DEFAULT_LIST_WIDTH - maximumWidth * 2) / 2)};
    layout.columnWidths = a;
    scrollPane.setPreferredSize(new Dimension((int)labels[0].getPreferredSize().getWidth() + 2 * LIST_BORDER_SIZE, DEFAULT_ROW_HEIGHT * correspondingEvent.length + 20));
    buttonPanel.setLayout(layout);
    buttonPanel.add(new JLabel()); // To keep the empty tile empty
    buttonPanel.add(selectButton);
    buttonPanel.add(cancelButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);
    final JLabel topLabel = new JLabel("Select the Event you wish to fire");
    topLabel.setIcon(IconLoader.ICON_EVENT);
    panel.add(topLabel, BorderLayout.NORTH);
    this.add(panel);
    this.setLocation(DEFAULT_STARTING_LOCATION);
    selectButton.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(final MouseEvent evt)
      {
        if (EventChooserDialog.this.getSelectedStep() != null)
        {
          cancelled = false;
          EventChooserDialog.this.dispose();
        }
      }
    });
    cancelButton.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(final MouseEvent evt)
      {
        EventChooserDialog.this.dispose();
      }
    });
    mList.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(final MouseEvent evt)
      {
        if (evt.getClickCount() == 2 && EventChooserDialog.this.getSelectedStep() != null)
        {
          cancelled = false;
          EventChooserDialog.this.dispose();
        }
      }
    });
    mList.addKeyListener(new KeyListener()
    {
      public void keyPressed(final KeyEvent e)
      {
        if (e.getKeyCode() == 10) // <ENTER> Key
        {
          cancelled = false;
          EventChooserDialog.this.dispose();
        }
      }

      public void keyReleased(final KeyEvent e)
      {
        // Do Nothing
      }

      public void keyTyped(final KeyEvent e)
      {
        // Do Nothing
      }

    });
    this.pack();
  }

  // ####################################################################
  // # Simple Access

  public SimulatorStep getSelectedStep()
  {
    return (SimulatorStep)eventList[mList.getSelectedIndex()];
  }
  public boolean wasCancelled()
  {
    return cancelled;
  }

  // ####################################################################
  // # Data Members

  private final JList<JLabel> mList;
  private boolean cancelled;
  private final SimulatorStep[] eventList;

  // ####################################################################
  // # Class Constants
  private static final long serialVersionUID = -4465845587624430860L;
  private static final int DEFAULT_LIST_WIDTH = 250;
  private static final int DEFAULT_ROW_HEIGHT = 20;
  private static final Point DEFAULT_STARTING_LOCATION = new Point(100, 100);
  private static final int LIST_BORDER_SIZE = 5;
}
