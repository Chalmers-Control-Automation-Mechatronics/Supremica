//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   InsertConstantAliasAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;

import net.sourceforge.waters.gui.EventAliasEditorDialog;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.ParameterBindingEditorDialog;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.ParameterBindingTransferable;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
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
    if(root != null){
    final SelectionOwner watersOwner = root.getComponentsPanel();
      setEnabled(watersOwner.canPaste(TEMPLATE_TRANSFERABLE));
    }
    else{
      setEnabled(false);
    }
  }



  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static ParameterBindingTransferable TEMPLATE_TRANSFERABLE =
    new ParameterBindingTransferable(new ParameterBindingElement(":dummy",
                                     new SimpleIdentifierElement(":dummy")));

}
