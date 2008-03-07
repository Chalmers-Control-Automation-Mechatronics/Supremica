//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   GraphEventPanelPopupFactory
//###########################################################################
//# $Id: GraphEventPanelPopupFactory.java,v 1.1 2008-03-07 04:11:02 robi Exp $
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
  //# Shared Menu Items
  void addDefaultMenuItems()
  {
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final IDEAction insert = master.getInsertEventLabelAction();
    popup.add(insert);
    final IDEAction delete = master.getDeleteAction();
    popup.add(delete);
    popup.addSeparator();
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.base.ProxyVisitor
  public Object visitProxy(final Proxy proxy)
  {
    final WatersPopupActionManager master = getMaster(); 
    final JPopupMenu popup = getPopup();
    final IDEAction edit = master.getEditEventLabelAction(proxy);
    popup.add(edit);
    final IDEAction insert = master.getInsertEventLabelAction();
    popup.add(insert);
    final IDEAction delete = master.getDeleteAction(proxy);
    popup.add(delete);
    return null;
  }

}
