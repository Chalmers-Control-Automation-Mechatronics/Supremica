//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.ReplaceInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A general command for replacing a list of items.</P>
 *
 * <P>This command can replace any collection of items in a component that
 * implements the {@link SelectionOwner} interface. When executed, the
 * command first deletes the old values of the replaced items and then
 * inserts the new values, using the {@link SelectionOwner} interface.
 * Items with associated {@link ListInsertPosition} are handled by direct
 * list manipulation. After completion of the operation, the new values of
 * the items get selected.</P>
 *
 * @author Robi Malik
 */

public class ReplaceCommand
  extends AbstractEditCommand
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a command to perform the given replacements und update the
   * selection.
   * @param  replacements      The list of replacements to be performed.
   * @param  panel             The panel that contains the items
   *                           and controls the operation.
   * @param  name              The name to be given to the command,
   *                           or <CODE>null</CODE>.
   */
  public ReplaceCommand(final List<ReplaceInfo> replacements,
			final SelectionOwner panel,
			final String name)
  {
    this(replacements, panel, name, true);
  }

  /**
   * Creates a command to perform the given replacements.
   * @param  replacements      The list of replacements to be performed.
   * @param  panel             The panel that contains the items
   *                           and controls the operation.
   * @param  name              The name to be given to the command,
   *                           or <CODE>null</CODE>.
   * @param  updatesSelection  A flag, indicating whether this command should
   *                           update the selection of the panels when
   *                           executed.
   */
  public ReplaceCommand(final List<ReplaceInfo> replacements,
			final SelectionOwner panel,
			final String name,
			final boolean updatesSelection)
  {
    super(panel, updatesSelection);
    mReplacements = replacements;
    if (name == null) {
      final List<Proxy> list = ReplaceInfo.getOldProxies(replacements);
      final String altname = ProxyNamer.getCollectionClassName(list);
      if (altname != null) {
	setName(altname + " Replacement");
      }
    } else {
      setName(name);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    execute(false);
  }

  public void undo()
  {
    execute(true);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void execute(final boolean undoing)
  {
    final boolean selecting = getUpdatesSelection();
    final int size = mReplacements.size();
    final SelectionOwner panel = getPanel();
    if (selecting) {
      panel.clearSelection(true);
    } else {
      final List<Proxy> deselect =
        ReplaceInfo.getOldProxies(mReplacements, undoing);
      panel.removeFromSelection(deselect);
    }
    final List<InsertInfo> deletes = new ArrayList<InsertInfo>(size);
    final List<InsertInfo> inserts = new ArrayList<InsertInfo>(size);
    final List<Proxy> selects =
      selecting ? new ArrayList<Proxy>(size) : null;
    for (final ReplaceInfo replacement : mReplacements) {
      final Object pos = replacement.getReplacePosition();
      final Proxy neo = replacement.getNewProxy(undoing);
      final Proxy old = replacement.getOldProxy(undoing);
      if (pos instanceof ListInsertPosition) {
        if (!deletes.isEmpty()) {
          panel.deleteItems(deletes);
          deletes.clear();
          panel.insertItems(inserts);
          inserts.clear();
        }
        final ListInsertPosition listpos = (ListInsertPosition) pos;
        final List<?> untyped = listpos.getList();
        @SuppressWarnings("unchecked")
        final List<Proxy> list = (List<Proxy>) untyped;
        final int index = listpos.getPosition();
        list.set(index, neo);
      } else {
        final InsertInfo delete = new InsertInfo(old, pos);
        deletes.add(delete);
        final InsertInfo insert = new InsertInfo(neo, pos);
        inserts.add(insert);
      }
      if (selecting) {
        selects.add(neo);
      }
    }
    if (!deletes.isEmpty()) {
      panel.deleteItems(deletes);
      panel.insertItems(inserts);
    }
    if (selecting) {
      panel.addToSelection(selects);
      panel.scrollToVisible(selects);
      panel.activate();
    }
  }


  //#########################################################################
  //# Data Members
  private final List<ReplaceInfo> mReplacements;

}








