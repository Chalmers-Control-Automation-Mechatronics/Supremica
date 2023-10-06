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

package net.sourceforge.waters.gui.command;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A command for deleting a set of selected items.</P>
 *
 * <P>This command can delete any collection of items from components that
 * implement the {@link SelectionOwner} interface. When executed, the
 * command first deselects and then deletes the items to be deleted, using
 * the {@link SelectionOwner} interface.  When undone, the items are added
 * back and afterwards selected.</P>
 *
 * @author Robi Malik
 */

public class DeleteCommand
  extends AbstractEditCommand
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a command to delete the given item and update the selection.
   * @param  delete            An insert information record specifying
   *                           the item to be deleted and its position
   *                           when undoing the deletion.
   * @param  panel             The panel that contains the item to be deleted
   *                           and controls the deletion.
   */
  public DeleteCommand(final InsertInfo delete, final SelectionOwner panel)
  {
    this(Collections.singletonList(delete), panel);
  }

  /**
   * Creates a command to delete the given item.
   * @param  delete            An insert information record specifying
   *                           the item to be deleted and its position
   *                           when undoing the deletion.
   * @param  panel             The panel that contains the item to be deleted
   *                           and controls the deletion.
   * @param  updatesSelection  A flag, indicating whether this command should
   *                           update the selection of the panel when
   *                           executed.
   */
  public DeleteCommand(final InsertInfo delete,
                       final SelectionOwner panel,
                       final boolean updatesSelection)
  {
    this(Collections.singletonList(delete), panel, updatesSelection);
  }

  /**
   * Creates a command to delete the given list of items and update the
   * selection.
   * @param  deletes           A list of insert information records specifying
   *                           the items to be deleted and their position
   *                           when undoing the deletion.
   * @param  panel             The panel that contains the items to be deleted
   *                           and controls the deletion.
   */
  public DeleteCommand(final List<InsertInfo> deletes,
                       final SelectionOwner panel)
  {
    this(deletes, panel, true);
  }

  /**
   * Creates a command to delete the given list of items.
   * @param  deletes           A list of insert information records specifying
   *                           the items to be deleted and their position
   *                           when undoing the deletion.
   * @param  panel             The panel that contains the items to be deleted
   *                           and controls the deletion.
   * @param  updatesSelection  A flag, indicating whether this command should
   *                           update the selection of the panel when
   *                           executed.
   */
  public DeleteCommand(final List<InsertInfo> deletes,
                       final SelectionOwner panel,
                       final boolean updatesSelection)
  {
    super(panel, "Deletion", updatesSelection);
    mDeletes = deletes;

    // And now for a nice name ...
    final List<Proxy> proxies = InsertInfo.getProxies(mDeletes);
    final String named = ProxyNamer.getCollectionClassName(proxies);
    if (named != null) {
      setName(named + " Deletion");
    }
    mHasBeenExecuted = false;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  @Override
  public void execute()
  {
    final List<Proxy> selection;
    final SelectionOwner panel = getPanel();
    if (getUpdatesSelection() && mHasBeenExecuted) {
      panel.clearSelection(true);
      selection = getSelectionAfterDelete(mDeletes);
    } else {
      final List<Proxy> deselect = InsertInfo.getProxies(mDeletes);
      panel.removeFromSelection(deselect);
      mHasBeenExecuted = true;
      selection = null;
    }
    panel.deleteItems(mDeletes);
    if (selection != null) {
      panel.replaceSelection(selection);
      panel.scrollToVisible(selection);
    }
    if (getUpdatesSelection()) {
      panel.activate();
    }
  }

  @Override
  public void undo()
  {
    final SelectionOwner panel = getPanel();
    panel.insertItems(mDeletes);
    if (getUpdatesSelection()) {
      final List<Proxy> selection = getSelectionAfterInsert(mDeletes);
      panel.replaceSelection(selection);
      panel.scrollToVisible(selection);
      panel.activate();
    }
  }


  //#########################################################################
  //# Data Members
  private final List<InsertInfo> mDeletes;
  private boolean mHasBeenExecuted;

}
