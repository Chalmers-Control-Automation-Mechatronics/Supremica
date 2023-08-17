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

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;


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
   * @param  root              The module editor containing the panel.
   *                           Used to display an inserted
   *                           automaton&mdash;disable this feature by
   *                           passing&nbsp;<CODE>null</CODE>.
   */
  public InsertCommand(final Proxy proxy,
                       final SelectionOwner panel,
                       final ModuleWindowInterface root)
  {
    this(proxy, panel, root, true);
  }

  /**
   * Creates a command to insert the given item.
   * @param  proxy             The item to be inserted.
   * @param  panel             The panel that receives the item
   *                           and controls the insertion.
   * @param  root              The module editor containing the panel.
   *                           Used to display an inserted
   *                           automaton&mdash;disable this feature by
   *                           passing&nbsp;<CODE>null</CODE>.
   * @param  updatesSelection  A flag, indicating whether this command should
   *                           update the selection of the panel when
   *                           executed.
   */
  public InsertCommand(final Proxy proxy,
                       final SelectionOwner panel,
                       final ModuleWindowInterface root,
                       final boolean updatesSelection)
  {
    this(getInserts(proxy, panel), panel, root, updatesSelection);
 }

  /**
   * Creates a command to insert the given list of items.
   * @param  inserts           A list items to be inserted.
   * @param  panel             The panel that receives the items
   * @param  root              The module editor containing the panel.
   *                           Used to display an inserted
   *                           automaton&mdash;disable this feature by
   *                           passing&nbsp;<CODE>null</CODE>.
   *                           and controls the insertion.
   */
  public InsertCommand(final List<InsertInfo> inserts,
                       final SelectionOwner panel,
                       final ModuleWindowInterface root)
  {
    this(inserts, panel, root, true);
  }

  /**
   * Creates a command to insert the given list of items.
   * @param  inserts           A list items to be inserted.
   * @param  panel             The panel that receives the items
   *                           and controls the insertion.
   * @param  root              The module editor containing the panel.
   *                           Used to display an inserted
   *                           automaton&mdash;disable this feature by
   *                           passing&nbsp;<CODE>null</CODE>.
   * @param  updatesSelection  A flag, indicating whether this command should
   *                           update the selection of the panel when
   *                           executed.
   */
  public InsertCommand(final List<InsertInfo> inserts,
                       final SelectionOwner panel,
                       final ModuleWindowInterface root,
                       final boolean updatesSelection)
  {
    super(panel, "Insertion", updatesSelection);
    mRoot = root;
    mInserts = inserts;
    final List<Proxy> proxies = InsertInfo.getProxies(mInserts);
    final String named = ProxyNamer.getCollectionClassName(proxies);
    if (named != null) {
      setName(named + " Insertion");
    }
    mHasBeenExecuted = false;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  @Override
  public void execute()
  {
    final SelectionOwner panel = getPanel();
    panel.insertItems(mInserts);
    if (getUpdatesSelection()) {
      final boolean shown = showAutomaton();
      final List<Proxy> selection = getSelectionAfterInsert(mInserts);
      panel.replaceSelection(selection);
      panel.scrollToVisible(selection);
      if (!shown) {
        panel.activate();
      }
    }
  }

  @Override
  public void undo()
  {
    final List<Proxy> selection;
    final SelectionOwner panel = getPanel();
    if (getUpdatesSelection()) {
      panel.clearSelection(false);
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
  //# Simple Access
  @Override
  public List<Proxy> getSelectionAfterInsert()
  {
    return getSelectionAfterInsert(mInserts);
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<InsertInfo> getInserts(final Proxy proxy,
                                             final SelectionOwner panel)
  {
    try {
      final Transferable transferable =
        WatersDataFlavor.createTransferable(proxy);
      return panel.getInsertInfo(transferable);
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final UnsupportedFlavorException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  /**
   * <P>Displays an inserted automaton.
   * This method is used to make sure an automaton that has just been
   * created or pasted is displayed, under the following conditions:</P>
   * <UL>
   * <LI>A non-<CODE>null</CODE> root panel has been specified.</LI>
   * <LI>The command is executed for the first time (no redo).</LI>
   * <LI>The list of inserted items contains a single automaton.</LI>
   * <LI>The automaton can be displayed graphically.</LI>
   * </UL>
   * @return <CODE>true</CODE> if the above conditions are true and an
   *         automaton has been displayed, <CODE>false</CODE> otherwise.
   */
  private boolean showAutomaton()
  {
    if (mRoot == null || mHasBeenExecuted) {
      return false;
    }
    mHasBeenExecuted = true;
    if (mInserts.size() != 1) {
      return false;
    }
    final InsertInfo info = mInserts.get(0);
    if (!(info.getProxy() instanceof SimpleComponentProxy)) {
      return false;
    }
    final Object inspos = info.getInsertPosition();
    if (!(inspos instanceof ListInsertPosition)) {
      return false;
    }
    try {
      final ListInsertPosition linspos = (ListInsertPosition) inspos;
      final ListSubject<? extends ProxySubject> list = linspos.getList();
      final int pos = linspos.getPosition();
      final SimpleComponentSubject subject =
        (SimpleComponentSubject) list.get(pos);
      mRoot.showEditor(subject);
      return true;
    } catch (final GeometryAbsentException exception) {
      return false;
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleWindowInterface mRoot;
  private final List<InsertInfo> mInserts;
  private boolean mHasBeenExecuted;

}
