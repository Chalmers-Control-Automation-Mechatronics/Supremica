//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;

import org.supremica.gui.ide.IDE;


/**
 * <P>The action associated with the 'select all' menu buttons.</P>
 *
 * <P>This selects all items the panel that currently owns the focus to the
 * system clipboard. To support this action, components including editable
 * items must implement the {@link SelectionOwner#canSelectMore()
 * canSelectMore()}, {@link SelectionOwner#getAllSelectableItems()
 * getAllSelectableItems()}, {@link SelectionOwner#canSelectMore()
 * canSelectMore()}, {@link SelectionOwner#replaceSelection(List)
 * replaceSelection()} methods of the {@link SelectionOwner} interface.</P>
 *
 * @author Robi Malik
 */

public class IDESelectAllAction
  extends IDEAction
{

  //#########################################################################
  //# Constructor
  IDESelectAllAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Select All");
    putValue(Action.SHORT_DESCRIPTION, "Select all items in the panel");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
    putValue(Action.ACCELERATOR_KEY,
	     KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
    setEnabled(false);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final FocusTracker tracker = getFocusTracker();
    final SelectionOwner watersOwner = tracker.getWatersSelectionOwner();
    final JTextComponent swingOwner = tracker.getSwingSelectionOwner();
    if (watersOwner != null) {
      final List<? extends Proxy> all = watersOwner.getAllSelectableItems();
      watersOwner.replaceSelection(all);
    } else if (swingOwner != null) {
      swingOwner.selectAll();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      final FocusTracker tracker = getFocusTracker();
      final SelectionOwner watersOwner = tracker.getWatersSelectionOwner();
      final JTextComponent swingOwner = tracker.getSwingSelectionOwner();
      final boolean enabled;
      if (watersOwner != null) {
        enabled = watersOwner.canSelectMore();
      } else if (swingOwner != null) {
        if (swingOwner.getSelectionStart() > 0) {
          enabled = true;
        } else {
          final int len = swingOwner.getText().length();
          enabled = swingOwner.getSelectionEnd() < len;
        }
      } else {
        enabled = false;
      }
      setEnabled(enabled);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
