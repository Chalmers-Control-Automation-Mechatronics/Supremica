//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   SelectionOwner
//###########################################################################
//# $Id$
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
 * net.sourceforge.waters.gui.observer.EditorChangedEvent#SELECTION_CHANGED
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
   * @return <CODE>true</CODE> if a call to {@link #selectAll()} would
   *         change the state of this component.
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
   * Gets the focussed item of the current selection.
   * The focussed item is the item in the component that was selected
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
   * ancestor needs to be passed to the {@link replaceSelection(List<?
   * extends Proxy>) replaceSelection()} or {@link addToSelection(List<?
   * extends Proxy>) addToSelection()} methods, and no attempts should be
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
   * @see    {@link #getSelectableAncestor(Proxy) getSelectableAncestor()}
   */
  public void replaceSelection(List<? extends Proxy> items);

  /**
   * Adds all items in the given list to the selection of this component.
   * The selection is performed directly, without using a command.
   * @param  items   The list of items to be selected. Should only
   *                 include selectable ancestors.
   * @see    {@link #getSelectableAncestor(Proxy) getSelectableAncestor()}
   */
  public void addToSelection(List<? extends Proxy> items);

  /**
   * Removes all items in the given list from the selection of this component.
   * The deselection is performed directly, without using a command.
   * @param  items   The list of items to be deselected. Should only
   *                 include selectable ancestors.
   * @see    {@link #getSelectableAncestor(Proxy) getSelectableAncestor()}
   */
  public void removeFromSelection(List<? extends Proxy> items);

  /**
   * Gets the insert position for a given item.
   * @param  item    The item to be inserted.
   * @return An object that identifies to the panel where and how to insert
   *         the given item if a create or paste operation is completed at
   *         this time.
   */
  public Object getInsertPosition(final Proxy item);

  /**
   * Inserts a new item into the panel.
   * This method is typically called when a dialog to edit a new item is
   * completed. It inserts the created item into the data structure. The
   * insertion is performed directly, without cloning, and without using a
   * command. The item is not selected, this has to be done separately.
   * @param  item   The item to be inserted.
   * @param  inspos An object identifying where and how to insert the
   *                new item. The insert position is typically obtained
   *                using the {@link #getInsertPosition()} method.
   */
  public void insertCreatedItem(final Proxy item, final Object inspos);

  /**
   * Determines whether the contents of the given selection can be
   * converted to a transferable to dragged or placed on the clipboard.
   * @param items   The list of items to be copied, as produced by the
   *                {@link #getCurrentSelection()} method.
   */
  public boolean canCopy(List<? extends Proxy> items);

  /**
   * Converts the given list of selected items into a transferable.
   * This method is typically used by a 'copy' action in order to place
   * the contents of the selection in the clipboard.
   * @param items   The list of items to be converted, as produced by the
   *                {@link #getCurrentSelection()} method. A call to {@link
   *                #canCopy(List<Proxy>) canCopy()} must return
   *                <CODE>true</CODE> for this list, otherwise the result
   *                is undetermined.
   */
  public Transferable createTransferable(List<? extends Proxy> items);

  /**
   * Determines whether the contents of the given transferable can be
   * pasted into this component.
   */
  public boolean canPaste(Transferable transferable);

  /**
   * <P>Copies the contents of the given transferable to a list of items that
   * can be inserted into this component.</P>
   * <P>This method can be used by a paste operation to obtain insert
   * position as required for the {@link #insertItems(List<InsertInfo>)
   * insertItems()} method. A previous call to {@link
   * #canPaste(Transferable) canPaste()} should have returned
   * <CODE>true</CODE>, otherwise the result of this method is
   * undefined.</P>
   * <P>This method may perform user interaction, and the user may cancel
   * the insertion of the transferable contents this point. In this case,
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
   * @return <CODE>true</CODE> if the {@link #getDeletionVictims(List<Proxy>)
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
   * #deleteItems(List<InsertInfo>) deleteItems()}.
   * @param  inserts A list of {@link InsertInfo} objects as returned by
   *                 the {@link #getDeletionVictims(List<Proxy>)
   *                 getDeletionVictims()} method.
   */
  public void insertItems(List<InsertInfo> inserts);

  /**
   * Deletes the given items.
   * This method removes all the items in the given list from the model.
   * The deletion is performed directly, without using a command.
   * The items are not deselected, this has to be done separately.
   * @param  deletes A list of {@link InsertInfo} objects as returned by
   *                 the {@link #getDeletionVictims(List<Proxy>)
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
