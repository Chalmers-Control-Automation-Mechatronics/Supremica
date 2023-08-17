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
import net.sourceforge.waters.gui.dialog.ConditionalEditorDialog;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.plain.module.ConditionalElement;
import net.sourceforge.waters.plain.module.SimpleIdentifierElement;

import org.supremica.gui.ide.IDE;


/**
 * The action to create a new conditional block for a module.
 * This action merely pops up the conditional block creation dialog
 * ({@link ConditionalEditorDialog});
 * the actual conditional block creation is done when the dialog is committed.
 *
 * @author Robi Malik
 */

public class InsertConditionalAction
  extends AbstractInsertAction
{
  //#########################################################################
  //# Constructors
  InsertConditionalAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "New Conditional Block ...");
    putValue(Action.SHORT_DESCRIPTION,
             "Insert a conditional block (IF statement)");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
    putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_NEW_CONDITIONAL);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    if (root != null) {
      final FocusTracker tracker = getFocusTracker();
      final SelectionOwner panel = tracker.getWatersSelectionOwner();
      new ConditionalEditorDialog(root, panel);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.actions.AbstractInsertAction
  @Override
  boolean canInsert(final SelectionOwner panel)
  {
    return panel.canPaste(TRANSFERABLE);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -5317533915428205304L;

  private static final ConditionalElement TEMPLATE =
    new ConditionalElement(new SimpleIdentifierElement(":dummy"));
  private static final Transferable TRANSFERABLE =
    WatersDataFlavor.createTransferable(TEMPLATE);

}
