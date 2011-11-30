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
