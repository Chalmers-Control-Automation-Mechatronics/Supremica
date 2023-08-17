//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.actions;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.dialog.EventAliasEditorDialog;
import net.sourceforge.waters.gui.dialog.ParameterBindingEditorDialog;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
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
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_B);
    putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_NEW_BINDING);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
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
