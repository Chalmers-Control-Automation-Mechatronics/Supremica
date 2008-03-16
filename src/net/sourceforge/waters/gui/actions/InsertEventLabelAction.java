//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   InsertEventLabelAction
//###########################################################################
//# $Id: InsertEventLabelAction.java,v 1.2 2008-03-16 21:27:39 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.GraphEventPanel;
import org.supremica.gui.ide.IDE;


/**
 * The action to create a new event label for a graph. This action merely
 * opens a new cell editor in the current graph's event list. The actual
 * label creation is done when the cell is committed.
 *
 * @author Robi Malik
 */

public class InsertEventLabelAction
  extends WatersGraphAction
{

  //#########################################################################
  //# Constructors
  InsertEventLabelAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "New Label ...");
    putValue(Action.SHORT_DESCRIPTION, "Add an event label to the graph");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
    putValue(Action.SMALL_ICON, IconLoader.ICON_EVENT);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final GraphEventPanel panel = getActiveGraphEventPanel();
    if (panel != null) {
      panel.createEvent();
    }
  }

}
