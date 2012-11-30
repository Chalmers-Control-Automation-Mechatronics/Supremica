//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   InsertConstantAliasAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import javax.swing.Action;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.dialog.EventAliasEditorDialog;
import net.sourceforge.waters.gui.dialog.ParameterBindingEditorDialog;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.module.ParameterBindingElement;
import net.sourceforge.waters.plain.module.SimpleIdentifierElement;

import org.supremica.gui.ide.IDE;


/**
 * The action to create a new event alias for a module.
 * This action merely popups the event alias creation dialog
 * ({@link EventAliasEditorDialog});
 * the actual alias creation is done when the dialog is committed.
 *
 * @author Carly Hona
 */

public class InsertParameterBindingAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  InsertParameterBindingAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "New Parameter Binding ...");
    putValue(Action.SHORT_DESCRIPTION, "Add parameter binding to the module");
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    if (root != null) {
      new ParameterBindingEditorDialog(root);
    }
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case SELECTION_CHANGED:
      updateEnabledStatus();
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateEnabledStatus()
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    if (root != null) {
      final SelectionOwner panel = root.getComponentsPanel();
      final boolean enabled = panel.canPaste(TRANSFERABLE);
      setEnabled(enabled);
    } else {
      setEnabled(false);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static ParameterBindingProxy TEMPLATE =
    new ParameterBindingElement(":dummy",
                                new SimpleIdentifierElement(":dummy"));
  private static Transferable TRANSFERABLE =
    WatersDataFlavor.createTransferable(TEMPLATE);

}
