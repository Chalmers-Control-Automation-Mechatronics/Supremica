//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.GraphSubject;
import org.supremica.gui.ide.IDE;


public class GraphLayoutAction
  extends WatersGraphAction
  implements ModelObserver
{

  //#########################################################################
  //# Constructor
  GraphLayoutAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Layout Graph");
    putValue(Action.SHORT_DESCRIPTION,
             "Automatically layout the current graph");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
    putValue(Action.ACCELERATOR_KEY,
	     KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final GraphEditorPanel surface = getActiveGraphEditorPanel();
    if (surface != null) {
      surface.runEmbedder(true);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
    case SUBPANEL_SWITCH:
      final GraphEditorPanel surface = getActiveGraphEditorPanel();
      if (surface == null) {
        setEnabled(false);
        observeGraph(null);
      } else {
        final GraphSubject graph = surface.getGraph();
        observeGraph(graph);
        updateEnabledStatus();
      }
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  public void modelChanged(final ModelChangeEvent event)
  {
    if (event.getSource() == mObservedGraph.getNodesModifiable()) {
      switch (event.getKind()) {
      case ModelChangeEvent.ITEM_ADDED:
        if (event.getValue() instanceof GroupNodeProxy) {
          setEnabled(false);
        }
        break;
      case ModelChangeEvent.ITEM_REMOVED:
        if (event.getValue() instanceof GroupNodeProxy) {
          updateEnabledStatus();
        }
        break;
      default:
        break;
      }
    }
  }

  public int getModelObserverPriority()
  {
    return ModelObserver.DEFAULT_PRIORITY;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void observeGraph(final GraphSubject graph)
  {
    if (graph != mObservedGraph) {
      if (mObservedGraph != null) {
        mObservedGraph.getNodesModifiable().removeModelObserver(this);
      }
      mObservedGraph = graph;
      if (mObservedGraph != null) {
        mObservedGraph.getNodesModifiable().addModelObserver(this);
      }
    }
  }

  private void updateEnabledStatus()
  {
    for (final NodeProxy node : mObservedGraph.getNodes()) {
      if (node instanceof GroupNodeProxy) {
        setEnabled(false);
        return;
      }
    }
    setEnabled(true);
  }


  //#########################################################################
  //# Data Members
  private GraphSubject mObservedGraph;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
