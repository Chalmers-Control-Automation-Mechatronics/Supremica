//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   InsertCommand
//###########################################################################
//# $Id: InsertCommand.java,v 1.5 2008-03-07 04:11:02 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A general command for inserting a list of items.</P>
 *
 * <P>This command can insert any collection of items to a component that
 * implements the {@link SelectionOwner} interface. When executed, the
 * command first inserts the items and then selects them, using the {@link
 * SelectionOwner} interface. When undone, the items are deselected and
 * deleted.</P>
 *
 * @author Robi Malik
 */

public class InsertCommand
  extends AbstractEditCommand
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a command to insert the given item and update the selection.
   * @param  proxy             The item to be inserted.
   * @param  panel             The panel that receives the item
   *                           and controls the insertion.
   */
  public InsertCommand(final Proxy proxy,
                       final SelectionOwner panel)
  {
    this(proxy, panel, true);
  }

  /**
   * Creates a command to insert the given item.
   * @param  proxy             The item to be inserted.
   * @param  panel             The panel that receives the item
   *                           and controls the insertion.
   * @param  updatesSelection  A flag, indicating whether this command should
   *                           update the selection of the panel when
   *                           executed.
   */
  public InsertCommand(final Proxy proxy,
                       final SelectionOwner panel,
                       final boolean updatesSelection)
  {
    super(panel, updatesSelection);
    final Object inspos = panel.getInsertPosition(proxy);
    final InsertInfo insert = new InsertInfo(proxy, inspos);
    mInserts = Collections.singletonList(insert);
    final String name = ProxyNamer.getItemClassName(proxy) + " Creation";
    setName(name);
  }

  /**
   * Creates a command to insert the given list of items.
   * @param  inserts   A list items to be inserted.
   * @param  panel     The panel that receives the items
   *                   and controls the insertion.
   */
  public InsertCommand(final List<InsertInfo> inserts,
                       final SelectionOwner panel)
  {
    super(panel, "Insertion");
    mInserts = inserts;
    final List<Proxy> proxies = InsertInfo.getProxies(mInserts);
    final String named = ProxyNamer.getCollectionClassName(proxies);
    if (named != null) {
      setName(named + " Insertion");
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    final SelectionOwner panel = getPanel();
    panel.insertItems(mInserts);
    if (getUpdatesSelection()) {
      final List<Proxy> selection = getSelectionAfterInsert(mInserts);
      panel.replaceSelection(selection);
      panel.scrollToVisible(selection);
      panel.activate();
    }
  }

  public void undo()
  {
    final List<Proxy> selection;
    final SelectionOwner panel = getPanel();
    if (getUpdatesSelection()) {
      panel.clearSelection();
      selection = getSelectionAfterDelete(mInserts);
    } else {
      selection = null;
    }
    panel.deleteItems(mInserts);
    if (selection != null) {
      panel.replaceSelection(selection);
      panel.scrollToVisible(selection);
      panel.activate();
    }
  }


  //#########################################################################
  //# Data Members
  private final List<InsertInfo> mInserts;

}
