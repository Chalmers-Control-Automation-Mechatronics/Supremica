//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   GraphLayoutAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.ControlledSurface;
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
    final ControlledSurface surface = getActiveControlledSurface();
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
      final ControlledSurface surface = getActiveControlledSurface();
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
