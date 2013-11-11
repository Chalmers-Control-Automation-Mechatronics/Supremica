//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ComponentsTree
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.datatransfer.DataFlavor;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;

import org.supremica.gui.ide.ModuleContainer;


/**
 * The tree-view panel that shows the components list of a module.
 *
 * @author Carly Hona
 */

public class ComponentsTree extends ModuleTree
{

  //#########################################################################
  //# Constructor
  public ComponentsTree(final ModuleContainer root,
                        final WatersPopupActionManager manager)
  {
    super(root, manager, root.getModule(), root);
    mPopupFactory = new ComponentsTreePopupFactory(manager, root.getModuleContext());
  }

  @Override
  ListSubject<? extends ProxySubject> getRootList()
  {
    return getModuleContainer().getModule().getComponentListModifiable();
  }

  @Override
  String getRootName()
  {
    return "Components";
  }

  @Override
  DataFlavor getSupportedDataFlavor()
  {
    return WatersDataFlavor.COMPONENT;
  }

  private static final long serialVersionUID = 1L;

  @Override
  PopupFactory getPopupFactory()
  {
    return mPopupFactory;
  }

  private final PopupFactory mPopupFactory;

}