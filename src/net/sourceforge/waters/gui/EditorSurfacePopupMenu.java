//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorSurfacePopupMenu
//###########################################################################
//# $Id: EditorSurfacePopupMenu.java,v 1.3 2006-11-03 15:01:56 torda Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.event.*;
import java.util.List;
import javax.swing.*;


import org.supremica.util.VPopupMenu;


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
  public EditorSurfacePopupMenu(final EditorWindowInterface master,
				final List selectedObjects)
  {
    mMaster = master;
    mSelectedObjects = selectedObjects;

    mDeleteItem = new JMenuItem("Delete selected objects");
    mDeleteItem.addActionListener(this);
    add(mDeleteItem);
    mCreateEventItem = new JMenuItem("Create new event ...");
    mCreateEventItem.addActionListener(this);
    add(mCreateEventItem);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event) 
  {
    final Object source = event.getSource();
    if (source == mDeleteItem) {
      final ControlledSurface surface = mMaster.getControlledSurface();
      surface.deleteSelected();
      hide();
    } else if (source == mCreateEventItem) {
      mMaster.createEvent();
      hide();
    }
    mMaster.repaint();
  }


  //#########################################################################
  //# Data Members
  private final List mSelectedObjects;
  private final EditorWindowInterface mMaster;
  private final JMenuItem mDeleteItem;
  private final JMenuItem mCreateEventItem;

}
