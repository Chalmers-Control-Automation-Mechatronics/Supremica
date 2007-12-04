//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   DeleteCommand
//###########################################################################
//# $Id: DeleteCommand.java,v 1.2 2007-12-04 03:22:54 robi Exp $
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
  public void execute()
  {
    final SelectionOwner panel = getPanel();
    if (getUpdatesSelection() && mHasBeenExecuted) {
      panel.clearSelection();
    } else {
      final List<Proxy> proxies = InsertInfo.getProxies(mDeletes);
      panel.removeFromSelection(proxies);
      mHasBeenExecuted = true;
    }
    panel.deleteItems(mDeletes);
    if (getUpdatesSelection()) {
      panel.activate();
    }
  }

  public void undo()
  {
    final SelectionOwner panel = getPanel();
    if (getUpdatesSelection()) {
      panel.clearSelection();
    }
    panel.insertItems(mDeletes);
    if (getUpdatesSelection()) {
      final List<Proxy> proxies = InsertInfo.getProxies(mDeletes);
      panel.addToSelection(proxies);
      panel.scrollToVisible(proxies);
      panel.activate();
    }
  }


  //#########################################################################
  //# Data Members
  private final List<InsertInfo> mDeletes;
  private boolean mHasBeenExecuted;

}
