//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;

import net.sourceforge.waters.gui.GraphEventPanel;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.base.Proxy;
import org.supremica.gui.ide.IDE;


/**
 * The action to edit an event label for a graph. This action merely
 * opens a cell editor in the current graph's event list. The actual
 * label change is done when the cell is committed.
 *
 * @author Robi Malik
 */

public class EditEventLabelAction
  extends WatersGraphAction
{

  //#########################################################################
  //# Constructors
  EditEventLabelAction(final IDE ide)
  {
    this(ide, null);
  }

  EditEventLabelAction(final IDE ide, final Proxy arg)
  {
    super(ide);
    mActionArgument = arg;
    putValue(Action.NAME, "Edit Label");
    if (arg == null) {
      putValue(Action.SHORT_DESCRIPTION,
	       "Edit the currently selected event label for the graph");
    } else {
      putValue(Action.SHORT_DESCRIPTION,
	       "Edit this event label for the entire graph");
    }
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final GraphEventPanel panel = getActiveGraphEventPanel();
    final Proxy arg = getActionArgument();
    if (panel != null && arg != null) {
      panel.editEvent(arg);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      updateEnabledStatus();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.actions.IDEAction
  public Proxy getSelectionAnchor()
  {
    final GraphEventPanel panel = getActiveGraphEventPanel();
    if (panel != null && panel.isFocusOwner()) {
      return panel.getSelectionAnchor();
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Auxilary Methods
  private void updateEnabledStatus()
  {
    final boolean enabled = getActionArgument() != null;
    setEnabled(enabled);
  }

  private Proxy getActionArgument()
  {
    if (mActionArgument != null) {
      return mActionArgument;
    } else {
      return getSelectionAnchor();
    }
  }


  //#########################################################################
  //# Data Members
  private final Proxy mActionArgument;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
