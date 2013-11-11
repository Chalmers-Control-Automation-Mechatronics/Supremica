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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.NodeSubject;

import org.supremica.gui.ide.ModuleContainer;


/**
 * The Propositions Tree used to view the propositions of a node
 *
 * @author Carly Hona
 */
public class PropositionsTree extends ModuleTree
{
  public PropositionsTree(final ModuleContainer rootWindow,
                          final WatersPopupActionManager manager,
                          final NodeSubject root, final UndoInterface undo)
  {
    super(rootWindow, manager, root, undo);
    mPopupFactory = new PropositionsTreePopupFactory(manager, rootWindow.getModuleContext());
    setRootVisible(false);
    addKeyListener(new KeySpy());
  }

  @Override
  ListSubject<? extends ProxySubject> getRootList()
  {
    return ((NodeSubject) getRoot())
      .getPropositions().getEventIdentifierListModifiable();
  }

  @Override
  String getRootName()
  {
    return "Propositions";
  }

  @Override
  DataFlavor getSupportedDataFlavor()
  {
    return WatersDataFlavor.IDENTIFIER;
  }

  @Override
  PopupFactory getPopupFactory()
  {
    return mPopupFactory;
  }

  private class KeySpy implements KeyListener
  {

    @Override
    public void keyTyped(final KeyEvent e)
    {
    }

    @Override
    public void keyPressed(final KeyEvent e)
    {
    }

    @Override
    public void keyReleased(final KeyEvent e)
    {
      if(e.getKeyCode() == KeyEvent.VK_DELETE){
        deleteItems(getDeletionVictims(getCurrentSelection()));
      }

    }

  }

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private final PopupFactory mPopupFactory;

}
