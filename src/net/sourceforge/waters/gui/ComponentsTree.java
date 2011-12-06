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


/**
 * The tree-view panel that shows the components list of a module.
 *
 * @author Carly Hona
 */

public class ComponentsTree extends ModuleTree
{

  //#########################################################################
  //# Constructor
  public ComponentsTree(final ModuleWindowInterface root,
                        final WatersPopupActionManager manager)
  {
    super(root, manager);
    mPopupFactory = new ComponentsTreePopupFactory(manager, root.getModuleContext());
  }

  ListSubject<? extends ProxySubject> getRootList()
  {
    return getRoot().getModuleSubject().getComponentListModifiable();
  }

  String getRootName()
  {
    return "Components";
  }

  DataFlavor getSupportedDataFlavor()
  {
    return WatersDataFlavor.MODULE_COMPONENT_LIST;
  }

  private static final long serialVersionUID = 1L;

  PopupFactory getPopupFactory()
  {
    return mPopupFactory;
  }

  private final PopupFactory mPopupFactory;

}