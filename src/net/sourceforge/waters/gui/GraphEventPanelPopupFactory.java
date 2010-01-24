//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   GraphEventPanelPopupFactory
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;


class GraphEventPanelPopupFactory
  extends PopupFactory
{

  //#########################################################################
  //# Constructor
  GraphEventPanelPopupFactory(final WatersPopupActionManager master)
  {
    super(master);
  }


  //#########################################################################
  //# Menu Items
  protected void addItemSpecificMenuItems(final Proxy proxy)
  {
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final IDEAction edit = master.getEditEventLabelAction(proxy);
    popup.add(edit);
    final IDEAction insert = master.getInsertEventLabelAction();
    popup.add(insert);
    final IDEAction delete = master.getDeleteAction(proxy);
    popup.add(delete);
  }

  protected void addDefaultMenuItems()
  {
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final IDEAction insert = master.getInsertEventLabelAction();
    popup.add(insert);
    final IDEAction delete = master.getDeleteAction();
    popup.add(delete);
    popup.addSeparator();
  }

}
