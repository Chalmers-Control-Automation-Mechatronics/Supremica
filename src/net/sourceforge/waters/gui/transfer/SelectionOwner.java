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

package net.sourceforge.waters.gui.transfer;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A common interface for all panels that maintain a list of selected
 * items.</P>
 *
 * <P>A selection owner also must implement the {@link Subject} interface
 * and provide {@link
 * net.sourceforge.waters.gui.observer.EditorChangedEvent.Kind#SELECTION_CHANGED
 * SELECTION_CHANGED} events whenever the selection changes.</P>
 *
 * @author Robi Malik
 */

public interface SelectionOwner
  extends Subject
{

  /**
   * Gets the undo interface to be used for commands applied to this panel.
   * @param  action  The action to be undone.
   * @return The undo interface to be used, or <CODE>null</CODE> if the
   *         panel does not support undo of the specified action.
   */
  public UndoInterface getUndoInterface(final Action action);

  /**
   * Determines whether any items are selected in this component.
   */
  public boolean hasNonEmptySelection();

  /**
   * Determines whether this component contains more items that could be
   * selected.
   */
  public boolean canSelectMore();

  /**
   * Determines whether the given item is selected in this panel.
   */
  public boolean isSelected(Proxy proxy);

  /**
   * Gets the current selection.
   * @return A copy of the list of currently selected items, in the order
   *         they would be pasted.
   */
  public List<? extends Proxy> getCurrentSelection();

  /**
   * Gets the list of all selectable items.
   * @return The list of items displayed in this component that can be
   *         selected, in the order they would be pasted.
   */
  public List<? extends Proxy> getAllSelectableItems();

  /**
   * Gets the focused item of the current selection.
   * The focused item is the item in the component that was selected
   * last, which typically is the target of a 'properties' or similar action.
   * @return The single selected item that is to be considered as the anchor,
   *         or <CODE>null</CODE> if no such item can be identified.
   */
  public Proxy getSelectionAnchor();

  /**
   * Determines the closest ancestor of the given item that can be selected
   * in this panel. Usually, this method simply returns the given item, but
   * some panels may not be able to display the item directly, but only
   * indirectly through one of its ancestors. In such cases, the selectable
   * ancestor needs to be passed to the {@link #replaceSelection(List)
   * replaceSelection()} or {@link #addToSelection(List) addToSelection()}
   * methods, and no attempts should be
   * made to select the item itself.
   * @param  item    The item the user wants to select.
   * @return The best ancestor that can be selected in this panel, or
   *         <CODE>null</CODE> if the item cannot be selected at all.
   */
  public Proxy getSelectableAncestor(final Proxy item);

  /**
   * Resets the current selection to be empty.
   * @param  propagate  A flag, indicating whether any associated panels
   *                    should have their selection cleared as well.
   *                    This is used by the {@link
   *                    net.sourceforge.waters.gui.GraphEventPanel
   *                    GraphEventPanel}, which in some cases needs to
   *                    clear the selection in the associated graph as
   *                    well.
   */
  public void clearSelection(boolean propagate);

  /**
   * Replaces the component's current selection by the given list.
   * The selection is performed directly, without using a command.
   * @param  items   The list of items to be selected. Should only
   *                 include selectable ancestors.
   * @see    #getSelectableAncestor(Proxy) getSelectableAncestor()
   */
  public void replaceSelection(List<? extends Proxy> items);

  /**
   * Adds all items in the given list to the selection of this component.
   * The selection is performed directly, without using a command.
   * @param  items   The list of items to be selected. Should only
   *                 include selectable ancestors.
   * @see    #getSelectableAncestor(Proxy) getSelectableAncestor()
   */
  public void addToSelection(List<? extends Proxy> items);

  /**
   * Removes all items in the given list from the selection of this component.
   * The deselection is performed directly, without using a command.
   * @param  items   The list of items to be deselected. Should only
   *                 include selectable ancestors.
   * @see    #getSelectableAncestor(Proxy) getSelectableAncestor()
   */
  public void removeFromSelection(List<? extends Proxy> items);

  /**
   * Determines whether the contents of the given transferable can be
   * pasted into this component.
   */
  public boolean canPaste(Transferable transferable);

  /**
   * <P>Copies the contents of the given transferable to a list of items that
   * can be inserted into this component.</P>
   * <P>This method can be used by a paste operation to obtain insert
   * position as required for the {@link #insertItems(List)
   * insertItems()} method. A previous call to {@link
   * #canPaste(Transferable) canPaste()} should have returned
   * <CODE>true</CODE>, otherwise the result of this method is
   * undefined.</P>
   * <P>This method may perform user interaction, and the user may cancel
   * the insertion of the transferable contents at this point. In this case,
   * <CODE>null</CODE> is returned.</P>
   * @return A list of {@link InsertInfo} objects identifying the items
   *         to be removed from the model, plus any information needed
   *         to insert them appropriately; or <CODE>null</CODE> if the
   *         user has cancelled the insertion.
   */
  public List<InsertInfo> getInsertInfo(Transferable transferable)
    throws IOException, UnsupportedFlavorException;

  /**
   * Determines whether the given selection includes any items that cannot be
   * deleted.
   * @param items   A list of items to be deleted, as produced by the
   *                {@link #getCurrentSelection()} method.
   * @return <CODE>true</CODE> if the {@link #getDeletionVictims(List)
   *         getDeletionVictims()} method would return a non-empty list
   *         when passed the given selection as input.
   */
  public boolean canDelete(List<? extends Proxy> items);

  /**
   * <P>Gets the list of items that will actually be deleted in order to
   * delete the items in the given list.</P>
   * <P>Often, the items actually deleted are not the same as the items
   * selected. For example, deleting a parent node automatically deletes
   * all its children, so the children should not be deleted
   * explicitly. There may also be items that cannot be deleted at all,
   * such as root nodes.</P>
   * <P>This method may perform user interaction, and the user may cancel
   * the deletion of the specified items at this point. In this case,
   * <CODE>null</CODE> is returned.</P>
   * @param  items   A list of selected items that the user may want
   *                 to delete.
   * @return A list of {@link InsertInfo} objects identifying the items
   *         to be removed from the model, plus any information needed
   *         to later undo their deletion; or <CODE>null</CODE> if the
   *         user has cancelled the deletion.
   */
  public List<InsertInfo> getDeletionVictims(List<? extends Proxy> items);

  /**
   * Inserts the given items.
   * This method inserts the given deleted items into the model. This can
   * be used to undo the effects of a previous call to {@link
   * #deleteItems(List) deleteItems()}.
   * @param  inserts A list of {@link InsertInfo} objects as returned by
   *                 the {@link #getDeletionVictims(List)
   *                 getDeletionVictims()} method.
   */
  public void insertItems(List<InsertInfo> inserts);

  /**
   * Deletes the given items.
   * This method removes all the items in the given list from the model.
   * The deletion is performed directly, without using a command.
   * The items are not deselected, this has to be done separately.
   * @param  deletes A list of {@link InsertInfo} objects as returned by
   *                 the {@link #getDeletionVictims(List)
   *                 getDeletionVictims()} method.
   */
  public void deleteItems(List<InsertInfo> deletes);

  /**
   * Scrolls and unfolds the display to make sure that as many as possible
   * of the items in the given list are displayed. This generally only
   * works when the component is contained in a {@link
   * javax.swing.JScrollPane JScrollPane}.
   */
  public void scrollToVisible(List<? extends Proxy> items);

  /**
   * Activates this panel. This method ensures that this panel is visible
   * and editable in the GUI, and has the keyboard focus.
   */
  public void activate();

  /**
   * Cleans up. This method is called by the GUI to notify that the panel
   * has been closed. It should unregister all listeners on external
   * components and perform any other cleanup that may be necessary. The
   * panel does not have to support any other methods once
   * <CODE>close()</CODE> has been called.
   */
  public void close();

}
