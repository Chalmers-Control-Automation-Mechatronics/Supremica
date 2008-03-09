//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   UpdateCommand
//###########################################################################
//# $Id: UpdateCommand.java,v 1.5 2008-03-09 21:52:09 robi Exp $
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
   * @param  modified         The list of items modified by the command.
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
    if (name == null) {
      final int size = modified.size() + added.size() + removed.size();
      final List all = new ArrayList<Proxy>(size);
      all.addAll(modified);
      all.addAll(added);
      all.addAll(removed);
      final String named = ProxyNamer.getCollectionClassName(all);
      final String suffix =
        added.isEmpty() && removed.isEmpty() ? "Movement" : "Rearrangement";
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
  public void execute()
  {
    final int size = mModified.size() + mAdded.size();
    final List<Proxy> visible = new ArrayList<Proxy>(size);
    visible.addAll(mModified);
    visible.addAll(mAdded);
    if (!mUpdatesSelection || !mHasBeenExecuted) {
      mPanel.removeFromSelection(mRemoved);
      super.execute();
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

  public void undo()
  {
    final int size = mModified.size() + mRemoved.size();
    final List<Proxy> visible = new ArrayList<Proxy>(size);
    visible.addAll(mModified);
    visible.addAll(mRemoved);
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
  private final boolean mUpdatesSelection;

  private boolean mHasBeenExecuted;

}
