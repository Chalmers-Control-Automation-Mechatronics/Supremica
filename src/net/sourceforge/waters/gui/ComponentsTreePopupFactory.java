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
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;


class ComponentsTreePopupFactory
  extends PopupFactory
{

  //#########################################################################
  //# Constructor
  ComponentsTreePopupFactory(final WatersPopupActionManager master,
                             final ModuleContext context)
  {
    super(master);
    mContext = context;
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
  public Object visitInstanceProxy(final InstanceProxy inst)
  {
    visitProxy(inst);
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final String name = inst.getModuleName();
    final ModuleProxy module = mContext.getModule();
    final IDEAction gotomod = master.getGotoModuleAction(module, name);
    popup.add(gotomod);
    return null;
  }

  public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
  {
    visitProxy(comp);
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final IDEAction editgraph = master.getShowGraphAction(comp);
    popup.add(editgraph);
    return null;
  }


  //#######################################################################
  //# Data Members
  private final ModuleContext mContext;

}
