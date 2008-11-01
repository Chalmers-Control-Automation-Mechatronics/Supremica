//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   ShowGraphAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.ide.IDE;


/**
 * The action to edit a graph in a module.
 * This action displays the graph editor for the automaton currently
 * selected in the module components list.
 *
 * @author Robi Malik
 */

public class ShowGraphAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  ShowGraphAction(final IDE ide)
  {
    this(ide, null);
  }

  ShowGraphAction(final IDE ide, final Proxy arg)
  {
    super(ide);
    mActionArgument = arg;
    putValue(Action.NAME, "Show Graph");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_G);
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    final SimpleComponentSubject comp =
      (SimpleComponentSubject) getActionArgument();
    try {
      root.showEditor(comp);
    } catch (final GeometryAbsentException exception) {
      JOptionPane.showMessageDialog
        (root.getRootWindow(), exception.getMessage());
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      updateEnabledStatus();
    }
  }


  //#########################################################################
  //# Auxilary Methods
  private void updateEnabledStatus()
  {
    final Proxy proxy = getActionArgument();
    final boolean enabled =
      proxy != null && proxy instanceof SimpleComponentSubject;
    setEnabled(enabled);
    if (enabled) {
      putValue(Action.SHORT_DESCRIPTION, "Edit the graph for this automaton");
    } else {
      putValue(Action.SHORT_DESCRIPTION,
               "Edit the graph for the selected automaton");
    }
  }

  private Proxy getActionArgument()
  {
    if (mActionArgument != null) {
      return mActionArgument;
    } else {
      return getSelectionAnchor();
    }
  }


  //#########################################################################
  //# Data Members
  private final Proxy mActionArgument;

}