//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleTree
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.datatransfer.DataFlavor;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.NodeSubject;


/**
 * The Propositions Tree used to view the propositions of a node
 *
 * @author Carly Hona
 */
public class PropositionsTree extends ModuleTree
{
  public PropositionsTree(final ModuleWindowInterface rootWindow,
                          final WatersPopupActionManager manager,
                          final NodeSubject root, final UndoInterface undo)
  {
    super(rootWindow, manager, root, undo);
    mPopupFactory = new AliasesTreePopupFactory(manager, rootWindow.getModuleContext());
    setRootVisible(false);
  }

  ListSubject<? extends ProxySubject> getRootList()
  {
    return ((NodeSubject) getRoot()).getPropositions().getEventListModifiable();
  }

  String getRootName()
  {
    return "Propositions";
  }

  DataFlavor getSupportedDataFlavor()
  {
    // TODO Auto-generated method stub
    return WatersDataFlavor.IDENTIFIER;
  }

  PopupFactory getPopupFactory()
  {
    return mPopupFactory;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private final PopupFactory mPopupFactory;

}
