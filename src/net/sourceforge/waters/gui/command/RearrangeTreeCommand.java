//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A command for moving a set of selected items.</P>
 *
 * <P>This command can move any collection of items within a component that
 * implements the {@link SelectionOwner} interface. When executed, the
 * command first deletes the items to be moved and then re-inserts them in
 * their new position and selects them, using
 * the {@link SelectionOwner} interface.</P>
 *
 * @author Robi Malik, Carly Hona
 */

public class RearrangeTreeCommand
  extends AbstractEditCommand
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a command to move the given list of items.
   *  @param  inserts          A list of insert information records specifying
   *                           the items to be moved and their position
   *                           after the move operation.
   * @param  deletes           A list of insert information records specifying
   *                           the items to be moved and their position
   *                           before the move operation.
   * @param  panel             The panel that contains the items to be moved
   *                           and controls the operation.
   */
  public RearrangeTreeCommand(final List<InsertInfo> inserts, final List<InsertInfo> deletes,
                       final SelectionOwner panel)
  {
    this(inserts, deletes, panel, true);
  }

  /**
   * Creates a command to move the given list of items.
   *  @param  inserts          A list of insert information records specifying
   *                           the items to be moved and their position
   *                           after the move operation.
   * @param  deletes           A list of insert information records specifying
   *                           the items to be moved and their position
   *                           before the move operation.
   * @param  panel             The panel that contains the items to be moved
   *                           and controls the operation.
   * @param  updatesSelection  A flag, indicating whether this command should
   *                           update the selection of the panel when
   *                           executed.
   */
  public RearrangeTreeCommand(final List<InsertInfo> inserts, final List<InsertInfo> deletes,
                       final SelectionOwner panel,
                       final boolean updatesSelection)
  {
    super(panel, "Move", updatesSelection);
    mInserts = inserts;
    mDeletes = deletes;

    // And now for a nice name ...
    final List<Proxy> proxies = InsertInfo.getProxies(inserts);
    final String named = ProxyNamer.getCollectionClassName(proxies);
    if (named != null) {
      setName(named + " Move");
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    final SelectionOwner panel = getPanel();
    panel.deleteItems(mDeletes);
    panel.insertItems(mInserts);
    updateSelection();
  }

  public void undo()
  {
    final SelectionOwner panel = getPanel();
    panel.deleteItems(mInserts);
    panel.insertItems(mDeletes);
    updateSelection();
  }

//#########################################################################
  //# Auxiliary Methods
  private void updateSelection()
  {
    if (getUpdatesSelection()) {
      final List<Proxy> selection = new ArrayList<Proxy>(mInserts.size());
      for (final InsertInfo insert : mInserts) {
        selection.add(insert.getProxy());
      }
      final SelectionOwner panel = getPanel();
      panel.replaceSelection(selection);
      panel.scrollToVisible(selection);
      panel.activate();
    }
  }

  //#########################################################################
  //# Data Members
  private final List<InsertInfo> mDeletes;
  private final List<InsertInfo> mInserts;

}
