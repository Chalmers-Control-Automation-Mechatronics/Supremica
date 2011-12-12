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
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;


class AliasesTreePopupFactory
  extends PopupFactory
{

  //#########################################################################
  //# Constructor
  AliasesTreePopupFactory(final WatersPopupActionManager master,
                             final ModuleContext context)
  {
    super(master);
    mVisitor = new AliasesTreePopupVisitor();
    mContext = context;
  }


  //#########################################################################
  //# Shared Menu Items
  protected void addItemSpecificMenuItems(final Proxy proxy)
  {
    try {
      proxy.acceptVisitor(mVisitor);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }

  protected void addCommonMenuItems()
  {
    super.addCommonMenuItems();
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    popup.addSeparator();
    final IDEAction showalias = master.getInsertConstantAliasAction();
    popup.add(showalias);
    final IDEAction newfor = master.getInsertForeachComponentAction();
    if(newfor.isEnabled()){
      popup.add(newfor);
    }
    final IDEAction showcomment = master.getShowModuleCommentAction();
    popup.add(showcomment);
  }


  //#########################################################################
  //# Inner Class AliasesTreePopupVisitor
  private class AliasesTreePopupVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      addPropertiesAndDeleteMenuItems(proxy);
      return null;
    }

  }
  //#######################################################################
  //# Data Members
  private final AliasesTreePopupVisitor mVisitor;
  @SuppressWarnings("unused")
  private final ModuleContext mContext;

}
