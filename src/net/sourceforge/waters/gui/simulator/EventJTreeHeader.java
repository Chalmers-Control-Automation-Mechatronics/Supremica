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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public class EventJTreeHeader extends JTableHeader implements ComponentListener
{

  // #####################################################################
  // # Constructor

  public EventJTreeHeader(final JPanel panel)
  {
    parent = panel;
    oldWidth = parent.getSize().getWidth();
    parent.addComponentListener(this);
    this.getColumnModel().addColumn(new TableColumn());
    this.getColumnModel().addColumn(new TableColumn());
    this.getColumnModel().addColumn(new TableColumn());
    if (oldWidth == 0)
      oldWidth = 245;
    final double width = oldWidth;
    this.getColumnModel().getColumn(0).setWidth((int)(width * 0.2));
    this.getColumnModel().getColumn(0).setMaxWidth((int)(width * 0.2));
    this.getColumnModel().getColumn(0).setHeaderValue("Type");
    this.getColumnModel().getColumn(1).setWidth((int)(width * 0.6));
    this.getColumnModel().getColumn(1).setHeaderValue("Name");
    this.getColumnModel().getColumn(2).setWidth((int)(width * 0.2));
    this.getColumnModel().getColumn(2).setMaxWidth((int)(width * 0.2));
    this.getColumnModel().getColumn(2).setHeaderValue("Ebd");
    this.setReorderingAllowed(false);
    this.setVisible(true);
    this.setResizingAllowed(false);
  }

  // ####################################################################3
  // # Interface Component Listener


  public void componentHidden(final ComponentEvent e)
  {
    // Do nothing
  }

  public void componentMoved(final ComponentEvent e)
  {
    // Do nothing
  }

  public void componentResized(final ComponentEvent e)
  {
    final TableColumn firstColumn = this.getColumnModel().getColumn(0);
    final TableColumn secondColumn = this.getColumnModel().getColumn(1);
    final TableColumn thirdColumn = this.getColumnModel().getColumn(2);
    final double width = parent.getWidth();
    final double newWidth = width - firstColumn.getWidth() - thirdColumn.getWidth();
    secondColumn.setWidth((int)newWidth);
     oldWidth = parent.getWidth();
  }

  public void componentShown(final ComponentEvent e)
  {
    // Do nothing
  }

  // ###########################################################################
  // # Data Members

  private double oldWidth;
  private final JPanel parent;

  // ###########################################################################
  // # Class Constants
  private static final long serialVersionUID = 3210675056736810131L;
}
