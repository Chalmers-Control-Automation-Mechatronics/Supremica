//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ComponentsTreePopupFactory
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.module.SimpleComponentProxy;


class ComponentsTreePopupFactory
  extends PopupFactory
{

  //#########################################################################
  //# Constructor
  ComponentsTreePopupFactory(final WatersPopupActionManager master)
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
    final IDEAction newaut = master.getInsertSimpleComponentAction();
    popup.add(newaut);
    final IDEAction newvar = master.getInsertVariableAction();
    popup.add(newvar);
    final IDEAction showcomment = master.getShowModuleCommentAction();
    popup.add(showcomment);
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
  public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
  {
    visitProxy(comp);
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final IDEAction editgraph = master.getShowGraphAction(comp);
    popup.add(editgraph);
    return null;
  }

}
