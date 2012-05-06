//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   InsertForeachComponentAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import net.sourceforge.waters.gui.ForeachEditorDialog;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.plain.module.ForeachElement;
import net.sourceforge.waters.plain.module.SimpleIdentifierElement;
import org.supremica.gui.ide.IDE;


/**
 * The action to create a new foreach block for a module.
 * This action merely pops up the foreach block creation dialog
 * ({@link ForeachEditorDialog});
 * the actual forach block creation is done when the dialog is committed.
 *
 * @author Robi Malik
 */

public class InsertForeachAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  InsertForeachAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "New Foreach Block ...");
    putValue(Action.SHORT_DESCRIPTION, "Add a foreach block to the module");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    if (root != null) {
      final FocusTracker tracker = getFocusTracker();
      final SelectionOwner panel = tracker.getWatersSelectionOwner();

      new ForeachEditorDialog(root, panel);
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
    final FocusTracker tracker = getFocusTracker();
    final SelectionOwner panel = tracker.getWatersSelectionOwner();
    if (panel != null) {
      final boolean enabled = panel.canPaste(TRANSFERABLE);
      setEnabled(enabled);
    } else {
      setEnabled(false);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

  private static final ForeachProxy TEMPLATE =
    new ForeachElement(":dummy", new SimpleIdentifierElement(":dummy"));
  private static final Transferable TRANSFERABLE =
    WatersDataFlavor.createTransferable(TEMPLATE);

}
