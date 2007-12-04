//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventDeclListPopupFactory
//###########################################################################
//# $Id: EventDeclListPopupFactory.java,v 1.2 2007-12-04 03:22:54 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.module.EventDeclProxy;


class EventDeclListPopupFactory
  extends PopupFactory
{

  //#########################################################################
  //# Constructor
  EventDeclListPopupFactory(final WatersPopupActionManager master)
  {
    super(master);
  }


  //#########################################################################
  //# Shared Menu Items
  void addCommonMenuItems()
  {
    super.addCommonMenuItems();
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    popup.addSeparator();
    final IDEAction newevent = master.getInsertEventDeclAction();
    popup.add(newevent);
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
  public Object visitEventDeclProxy(final EventDeclProxy decl)
  {
    return visitProxy(decl);
  }

}
