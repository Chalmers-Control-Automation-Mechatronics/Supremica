//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A general compound command for a major changes in a module.</P>
 *
 * <P>In addition to the list of commands performing the actual changes of
 * the model, this class provides the support necessary to update the
 * selection. Three lists can be specified: a list of items modified by the
 * command, and lists of items added and deleted. After executing the
 * command, the selection is changed to contain only the items modified or
 * added. After undoing, the selection contains only the items modified or
 * deleted.</P>
 *
 * <P>The selection is not changed when the command is executed the first
 * time. Also in other cases, selection update can be disabled. However,
 * items will always be deselected prior to deletion.</P>
 *
 * @author Robi Malik
 */

public class UpdateCommand
  extends CompoundCommand
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an update command without additions or deletions.
   * @param  modified         The list of items modified by the command.
   * @param  panel            The panel owning the selection and controlling
   *                          the execution of the command.
   * @param  name             The name to be given to the command,
   *                          or <CODE>null</CODE> to compute a default.
   * @param  updatesSelection Whether the command should attempt to update
   *                          the selection after undo and redo.
   */
  public UpdateCommand(final List<? extends Proxy> modified,
                       final SelectionOwner panel,
                       final String name,
                       final boolean updatesSelection)
  {
    this(modified, emptyList(), emptyList(), panel, name, updatesSelection);
  }

  /**
   * Creates a general update command.
   * @param  modified         The list of items modified by the command,
   *                          which will be selected after undo.
   * @param  added            The list of items added by the command.
   * @param  removed          The list of items removed by the command.
   * @param  panel            The panel owning the selection and controlling
   *                          the execution of the command.
   * @param  name             The name to be given to the command,
   *                          or <CODE>null</CODE> to compute a default.
   * @param  updatesSelection Whether the command should attempt to update
   *                          the selection after undo and redo.
   */
  public UpdateCommand(final List<? extends Proxy> modified,
                       final List<? extends Proxy> added,
                       final List<? extends Proxy> removed,
                       final SelectionOwner panel,
                       final String name,
                       final boolean updatesSelection)
  {
    super(name);
    mModified = modified;
    mAdded = added;
    mRemoved = removed;
    mPanel = panel;
    mUpdatesSelection = updatesSelection;
    mHasBeenExecuted = false;
    String named;
    String suffix;
    if (name == null) {
      if(added.isEmpty()){
        if(removed.isEmpty()){
          suffix = "Movement";
          named = ProxyNamer.getCollectionClassName(modified);
        }
        else{
          suffix = "Deletion";
          named = ProxyNamer.getCollectionClassName(removed);
        }
      }
      else{
        if(removed.isEmpty()){
          suffix = "Insertion";
          named = ProxyNamer.getCollectionClassName(added);
        }
        else{
          suffix = "Rearrangement";
          final int size = modified.size() + added.size() + removed.size();
          final List<Proxy> all = new ArrayList<Proxy>(size);
          all.addAll(modified);
          all.addAll(added);
          all.addAll(removed);
          named = ProxyNamer.getCollectionClassName(all);
        }
      }


      if (named != null) {
        setName(named + ' ' + suffix);
      } else {
        setName(suffix);
      }
    }
  }


  //#########################################################################
  //# Simple Access
  public SelectionOwner getPanel()
  {
    return mPanel;
  }

  public boolean getUpdatesSelection()
  {
    return mUpdatesSelection;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  @Override
  public void execute()
  {
    final List<? extends Proxy> visible;
    if (mAdded.isEmpty()) {
      visible = mModified;
    } else {
      visible = mAdded;
    }
    if (!mUpdatesSelection) {
      mPanel.removeFromSelection(mRemoved);
      super.execute();
      mHasBeenExecuted = true;
    } else if (!mHasBeenExecuted) {
      mPanel.removeFromSelection(mRemoved);
      super.execute();
      if (!mAdded.isEmpty()) {
        mPanel.replaceSelection(mAdded);
      }
      mHasBeenExecuted = true;
    } else if (mRemoved.isEmpty()) {
      super.execute();
      mPanel.replaceSelection(visible);
    } else {
      mPanel.clearSelection(true);
      super.execute();
      mPanel.addToSelection(visible);
    }
    mPanel.scrollToVisible(visible);
    mPanel.activate();
  }

  @Override
  public void undo()
  {
    final List<? extends Proxy> visible;
    if (mRemoved.isEmpty()) {
      visible = mModified;
    } else {
      visible = mRemoved;
    }
    if (!mUpdatesSelection) {
      mPanel.removeFromSelection(mAdded);
      super.undo();
    } else if (mAdded.isEmpty()) {
      super.undo();
      mPanel.replaceSelection(visible);
    } else {
      mPanel.clearSelection(true);
      super.undo();
      mPanel.addToSelection(visible);
    }
    mPanel.scrollToVisible(visible);
    mPanel.activate();
  }

  @Override
  public void setUpdatesSelection(final boolean update)
  {
    super.setUpdatesSelection(update);
    mUpdatesSelection = false;
  }


  //#########################################################################
  //# Auxiliary Static Methods
  private static List<Proxy> emptyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private final List<? extends Proxy> mModified;
  private final List<? extends Proxy> mAdded;
  private final List<? extends Proxy> mRemoved;
  private final SelectionOwner mPanel;
  private boolean mUpdatesSelection;

  private boolean mHasBeenExecuted;

}
