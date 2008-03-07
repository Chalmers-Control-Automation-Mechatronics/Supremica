//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   EditEventLabelAction
//###########################################################################
//# $Id: EditEventLabelAction.java,v 1.1 2008-03-07 04:11:02 robi Exp $
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

}
