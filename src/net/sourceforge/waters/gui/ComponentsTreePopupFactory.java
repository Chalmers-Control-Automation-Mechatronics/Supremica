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
    mVisitor = new ComponentsTreePopupVisitor();
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
    final IDEAction newaut = master.getInsertSimpleComponentAction();
    popup.add(newaut);
    final IDEAction newvar = master.getInsertVariableAction();
    popup.add(newvar);
    final IDEAction showcomment = master.getShowModuleCommentAction();
    popup.add(showcomment);
  }


  //#########################################################################
  //# Inner Class GraphPopupVisitor
  private class ComponentsTreePopupVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      addPropertiesAndDeleteMenuItems(proxy);
      return null;
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
      final IDEAction languageInclusion = master.getLanguageIncusionAction(comp);
      popup.add(languageInclusion);
      return null;
    }

  }


  //#######################################################################
  //# Data Members
  private final ComponentsTreePopupVisitor mVisitor;
  private final ModuleContext mContext;

}
