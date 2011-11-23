//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   DeleteCommand
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.RearrangeTreeInfo;
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

public class RearrangeTreeCommand
  extends AbstractEditCommand
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a command to delete the given item and update the selection.
   * @param  move            An insert information record specifying
   *                           the item to be deleted and its position
   *                           when undoing the deletion.
   * @param  panel             The panel that contains the item to be deleted
   *                           and controls the deletion.
   */
  public RearrangeTreeCommand(final RearrangeTreeInfo move, final SelectionOwner panel)
  {
    this(Collections.singletonList(move), panel);
  }

  /**
   * Creates a command to delete the given item.
   * @param  move            An insert information record specifying
   *                           the item to be deleted and its position
   *                           when undoing the deletion.
   * @param  panel             The panel that contains the item to be deleted
   *                           and controls the deletion.
   * @param  updatesSelection  A flag, indicating whether this command should
   *                           update the selection of the panel when
   *                           executed.
   */
  public RearrangeTreeCommand(final RearrangeTreeInfo move,
                       final SelectionOwner panel,
                       final boolean updatesSelection)
  {
    this(Collections.singletonList(move), panel, updatesSelection);
  }

  /**
   * Creates a command to delete the given list of items and update the
   * selection.
   * @param  moves           A list of insert information records specifying
   *                           the items to be deleted and their position
   *                           when undoing the deletion.
   * @param  panel             The panel that contains the items to be deleted
   *                           and controls the deletion.
   */
  public RearrangeTreeCommand(final List<RearrangeTreeInfo> moves,
                       final SelectionOwner panel)
  {
    this(moves, panel, true);
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
  public RearrangeTreeCommand(final List<RearrangeTreeInfo> moves,
                       final SelectionOwner panel,
                       final boolean updatesSelection)
  {
    super(panel, "Move", updatesSelection);
    mDeletes = new ArrayList<InsertInfo>(moves.size());
    mInserts = new ArrayList<InsertInfo>(moves.size());
    for(final RearrangeTreeInfo move : moves){
      final Proxy proxy = move.getProxy();
      final InsertInfo insert = new InsertInfo(proxy , move.getInsertPosition());
      final InsertInfo delete = new InsertInfo(proxy , move.getDeletePosition());
      mInserts.add(insert);
      mDeletes.add(delete);
    }

    //reverse the inserted list so it inserts in the right order
    final List<InsertInfo> list = new ArrayList<InsertInfo>(mInserts.size());
    for(int i = mInserts.size() - 1 ; i >= 0 ; i--){
      list.add(mInserts.get(i));
    }
    mInserts = list;


    // And now for a nice name ...
    final List<Proxy> proxies = RearrangeTreeInfo.getProxies(moves);
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
  private List<InsertInfo> mInserts;

}
