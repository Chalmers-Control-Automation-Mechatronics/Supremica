//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   InsertBlockedEventListAction
//###########################################################################
//# $Id$
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
 * The action to create a new simple component for a module.
 * This action merely popups the component creation dialog
 * ({@link BlockedEventListEditorDialog});
 * the actual component creation is done when the dialog is committed.
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
      final GraphEditorPanel surface = getActiveControlledSurface();
      final GraphSubject graph = surface.getGraph();
      final boolean enabled = graph.getBlockedEvents() == null;
      setEnabled(enabled);
    }
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final GraphEditorPanel surface = getActiveControlledSurface();
    final Point point = mPoint == null ? surface.getPastePosition() : mPoint;
    final LabelGeometrySubject geo = new LabelGeometrySubject(point);
    final LabelBlockSubject blocked = new LabelBlockSubject(null, geo);
    final InsertCommand cmd = new InsertCommand(blocked, surface);
    cmd.setName("Blocked Event List Creation");
    final UndoInterface undoer = getActiveUndoInterface();
    undoer.executeCommand(cmd);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
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
  public void modelChanged(final ModelChangeEvent event)
  {
    if (event.getKind() == ModelChangeEvent.STATE_CHANGED &&
        event.getSource() == mObservedGraph) {
      updateEnabledStatus();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateEnabledStatus()
  {
    final GraphEditorPanel surface = getActiveControlledSurface();
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
  private Point mPoint;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
