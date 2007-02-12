//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorSurfacePopupMenu
//###########################################################################
//# $Id: EditorSurfacePopupMenu.java,v 1.6 2007-02-12 21:38:49 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.event.*;
import javax.swing.*;

import org.supremica.util.VPopupMenu;

import net.sourceforge.waters.subject.module.LabelBlockSubject;

/**
 * Popup menu for editing general stuff regarding the selected objects.
 * This menu shows up when the user pushes the right mouse button in the
 * graph window, while the cursor is not over any item.
 */

class EditorSurfacePopupMenu
  extends VPopupMenu
  implements ActionListener
{

  //#########################################################################
  //# Constructor
  public EditorSurfacePopupMenu(final EditorWindowInterface master)
  {
    mMaster = master;
    mDeleteItem = new JMenuItem("Delete selected objects");
    mDeleteItem.addActionListener(this);
    add(mDeleteItem);
    mCreateEventItem = new JMenuItem("Create new event ...");
    mCreateEventItem.addActionListener(this);
    add(mCreateEventItem);
    mCreateBlockedEventList = new JMenuItem("Create blocked event list");
    mCreateBlockedEventList.addActionListener(this);
    add(mCreateEventItem);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event) 
  {
    final Object source = event.getSource();
    if (source == mDeleteItem) {
      final ControlledSurface surface = mMaster.getControlledSurface();
      surface.doDeleteSelected();
      hide();
    } else if (source == mCreateEventItem) {
      mMaster.createEvent();
      hide();
    } else if (source == mCreateBlockedEventList) {
      mMaster.getControlledSurface().getGraph().setBlockedEvents(new LabelBlockSubject());
      hide();
    }
    mMaster.repaint();
  }


  //#########################################################################
  //# Data Members
  private final EditorWindowInterface mMaster;
  private final JMenuItem mDeleteItem;
  private final JMenuItem mCreateEventItem;
  private final JMenuItem mCreateBlockedEventList;

}
