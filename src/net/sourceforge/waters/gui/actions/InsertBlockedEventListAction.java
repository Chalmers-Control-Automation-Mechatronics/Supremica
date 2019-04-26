//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;

import org.supremica.gui.ide.IDE;


/**
 * The action to create a blocked events list in an automaton.
 *
 * @author Robi Malik
 */

public class InsertBlockedEventListAction
  extends WatersGraphAction
  implements ModelObserver
{

  //#########################################################################
  //# Constructors
  InsertBlockedEventListAction(final IDE ide)
  {
    this (ide, null);
  }

  InsertBlockedEventListAction(final IDE ide, final Point point)
  {
    super(ide);
    putValue(Action.NAME, "Add Blocked Events List");
    putValue(Action.SHORT_DESCRIPTION,
             "Add a blocked events list to the automaton");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_B);
    mObservedGraph = null;
    mPoint = point;
    if (point == null) {
      setEnabled(false);
    } else {
      final GraphEditorPanel surface = getActiveGraphEditorPanel();
      final GraphSubject graph = surface.getGraph();
      final boolean enabled = graph.getBlockedEvents() == null;
      setEnabled(enabled);
    }
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final GraphEditorPanel surface = getActiveGraphEditorPanel();
    final Point point = mPoint == null ? surface.getPastePosition() : mPoint;
    final LabelGeometrySubject geo = new LabelGeometrySubject(point);
    final LabelBlockSubject blocked = new LabelBlockSubject(null, geo);
    final InsertCommand cmd = new InsertCommand(blocked, surface, null);
    cmd.setName("Blocked Event List Creation");
    final UndoInterface undoer = getActiveUndoInterface();
    undoer.executeCommand(cmd);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
    case SUBPANEL_SWITCH:
      updateEnabledStatus();
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  @Override
  public void modelChanged(final ModelChangeEvent event)
  {
    if (event.getKind() == ModelChangeEvent.STATE_CHANGED &&
        event.getSource() == mObservedGraph) {
      updateEnabledStatus();
    }
  }

  @Override
  public int getModelObserverPriority()
  {
    return ModelObserver.DEFAULT_PRIORITY;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateEnabledStatus()
  {
    final GraphEditorPanel surface = getActiveGraphEditorPanel();
    if (surface == null) {
      setObservedGraph(null);
      setEnabled(false);
    } else {
      final GraphSubject graph = surface.getGraph();
      final boolean enabled = graph.getBlockedEvents() == null;
      setObservedGraph(graph);
      setEnabled(enabled);
    }
  }

  private void setObservedGraph(final GraphSubject graph)
  {
    if (mObservedGraph != graph) {
      if (mObservedGraph != null) {
        mObservedGraph.removeModelObserver(this);
      }
      mObservedGraph = graph;
      if (mObservedGraph != null) {
        mObservedGraph.addModelObserver(this);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private GraphSubject mObservedGraph;
  private final Point mPoint;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
