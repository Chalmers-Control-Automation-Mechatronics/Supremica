//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   SelectionOwner
//###########################################################################
//# $Id: SelectionOwner.java,v 1.1 2007-11-23 02:25:25 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.awt.datatransfer.Transferable;
import java.util.List;

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
   * Gets the current selection.
   * @return The list of currently selected items, in the order they
   *         would be pasted.
   */
  public List<Proxy> getCurrentSelection();

  /**
   * Gets the list of all selectable items.
   * @return The list of items displayed in this component that can be
   *         selected, in the order they would be pasted.
   */
  public List<Proxy> getAllSelectableItems();

  /**
   * Gets the focussed of the current selection.
   * The focussed item is the item in the component that was selected
   * last, which typically is the target of a 'properties' or similar action.
   * @return The single selected item that is to be considered as the anchor,
   *         or <CODE>null</CODE> if no such item can be identified.
   */
  public Proxy getFocussedItem();

  /**
   * Adds all items in the given list to the selection of this component.
   * The selection is performed directly, without using a command.
   * @param items   The list of items to be selected, as produced by the
   *                {@link #getCurrentSelection()} method.
   */
  public void addToSelection(List<Proxy> items);

  /**
   * Removes all items in the given list from the selection of this component.
   * The deselection is performed directly, without using a command.
   * @param items   The list of items to be deselected, as produced by the
   *                {@link #getCurrentSelection()} method.
   */
  public void removeFromSelection(List<Proxy> items);

  /**
   * Converts the given list of selected items into a transferable.
   * This method is typically used by a 'copy' action in order to place
   * the contents of the selection in the clipboard.
   * @param items   The list of items to be converted, as produced by the
   *                {@link #getCurrentSelection()} method.
   */
  public Transferable createTransferable(List<Proxy> items);

  /**
   * Determines whether contents of the given transferable can be pasted into
   * this component.
   */
  public boolean canPaste(Transferable transferable);

  /**
   * Pastes the contents of the given transferable into this component.
   */
  public void paste(Transferable transferable);

  /**
   * Determines whether the selection includes any items that can be
   * deleted.
   * @return <CODE>true</CODE> if the {@link #getDeletionVictims(List<Proxy>)
   *         getDeletionVictims()} method would return a non-empty list
   *         when passed the current selection as input.
   */
  public boolean canDeleteSelection();

  /**
   * Gets the list of items that will actually be deleted in order to
   * delete the items in the given list. Often, the items actually deleted
   * are not the same as the items selected. For example, deleting a
   * parent node automatically deletes all its children, so the children
   * should not be deleted explicitly. There may also be items that cannot
   * be deleted at all, such as root nodes.
   * @param  items   A list of selected items that the user may want
   *                 to delete.
   * @return A list of items to be removed from the model to achieve the
   *         deletion of all the items in the given list that can be
   *         deleted.
   */
  public List<Proxy> getDeletionVictims(List<Proxy> items);

  /**
   * Deletes the given items.
   * This method removes all the items in the given list from the model.
   * The deletion is performed directly, without using a command.
   * The items are not deselected, this has to be done separately.
   */
  public void deleteSelectedVictims(List<Proxy> items);

}
