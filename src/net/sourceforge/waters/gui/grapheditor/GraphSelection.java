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

package net.sourceforge.waters.gui.grapheditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NestedBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.NestedBlockSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


/**
 * <P>A collection of selected items in a {@link GraphEditorPanel}.</P>
 *
 * <P>A graph selection object is a collection of current selected items,
 * all assumed to be of type {@link ProxySubject}. The graph editor panel
 * holds one object of this class to record its current selection. It can
 * also create copies to backup and restore previous selections.</P>
 *
 * <P>The graph editor supports different types of selection to allow for
 * various drag operations. For example, it is possible to select individual
 * labels within a label block (to rearrange within a label block) or to
 * select entire labels blocks. Adding an item to a selection can change the
 * mode and result in the addition of extra items. For example, if the
 * selection consists of labels within a label block, and a node is added,
 * this results in a selection consisting of the label block and a node, with
 * the previously selected labels no longer being selected. The possible
 * modes are represented through the enumeration {@link SelectionMode}.</P>
 *
 * @author Robi Malik
 */

public class GraphSelection implements Iterable<ProxySubject>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty graoh selection.
   */
  public GraphSelection()
  {
  }

  /**
   * Creates a graph selection consisting of the given items.
   * The selection produced by this constructor may contain additional items
   * if it is not possible to select only the given items.
   */
  public GraphSelection(final List<? extends ProxySubject> initialItems)
  {
    selectAll(initialItems);
  }

  /**
   * Creates a graph selection consisting of the same items as the given
   * selection.
   * @param selection The graph selection to be duplicated.
   */
  public GraphSelection(final GraphSelection selection)
  {
    mMode = selection.mMode;
    mSelectedItems.addAll(selection.mSelectedItems);
    mSelectedLabelBlock = selection.mSelectedLabelBlock;
    mAnchor = selection.mAnchor;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  /**
   * Returns whether this selection is equal to another.
   * Two selections are equal if they have the same modes and contain
   * the same items in the same order.
   */
  @Override
  public boolean equals(final Object other)
  {
    if (other.getClass() == getClass()) {
      final GraphSelection selection = (GraphSelection) other;
      if (selection.mMode != mMode ||
          selection.mSelectedItems.size() != mSelectedItems.size()) {
        return false;
      } else {
        final Iterator<ProxySubject> iter = mSelectedItems.iterator();
        final Iterator<ProxySubject> otherIter =
          selection.mSelectedItems.iterator();
        while (iter.hasNext()) {
          if (iter.next() != otherIter.next()) {
            return false;
          }
        }
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    int result = mMode.hashCode();
    for (final ProxySubject item : mSelectedItems) {
      result *= 5;
      result += item.hashCode();
    }
    return result;
  }


  //#########################################################################
  //# Interface java.lang.Iterable<ProxySubject>
  /**
   * Returns an iterator over the list of currently selected items in the
   * order in which they were added to the selection. The iteration includes
   * only actually selected items, not items that are only rendered as
   * selected.
   * @see #isRenderedSelected(ProxySubject) isRenderedSelected()
   */
  @Override
  public Iterator<ProxySubject> iterator()
  {
    return new SelectionIterator(this);
  }


  //#########################################################################
  //# Selection Access
  /**
   * Returns a list containing the currently selected items in the order in
   * which they were added to the selection. This list contains only actually
   * selected items, not items that are only rendered as selected.
   * @see #isRenderedSelected(ProxySubject) isRenderedSelected()
   */
  public List<ProxySubject> asList()
  {
    return new ArrayList<>(mSelectedItems);
  }

  /**
   * Resets the selection to be empty.
   * @see #clearLabelSelection()
   */
  public boolean clear()
  {
    final boolean result = mMode != SelectionMode.EMPTY;
    mMode = SelectionMode.EMPTY;
    mSelectedItems.clear();
    mAnchor = mSelectedLabelBlock = null;
    return result;
  }

  /**
   * Resets the selection of labels within a label block.
   * If the selection consists of event labels within a label block
   * (mode {@link SelectionMode#EVENT_LABELS}), then the selection
   * is changed to consist of only the label block ({@link
   * SelectionMode#SUBGRAPH_SINGLE}). Otherwise this method clears the
   * selection.
   * @see #clear()
   */
  public boolean clearLabelSelection()
  {
    if (mMode == SelectionMode.EVENT_LABELS) {
      mSelectVisitor.select(mSelectedLabelBlock);
      return true;
    } else {
      return clear();
    }
  }

  /**
   * Removes the given item from the selection.
   * @return <CODE>true</CODE> if the selection was changed by this call.
   */
  public boolean deselect(final Proxy proxy)
  {
    final List<Proxy> list = Collections.singletonList(proxy);
    return deselectAll(list);
  }

  /**
   * Removes the given items from the selection.
   * @return <CODE>true</CODE> if the selection was changed by this call.
   */
  public boolean deselectAll(final Collection<? extends Proxy> proxies)
  {
    if (mSelectedItems.removeAll(proxies)) {
      switch (mSelectedItems.size()) {
      case 0:
        mMode = SelectionMode.EMPTY;
        mAnchor = mSelectedLabelBlock = null;
        return true;
      case 1:
        if (mMode == SelectionMode.SUBGRAPH_MULTIPLE) {
            mMode = SelectionMode.SUBGRAPH_SINGLE;
        }
        updateLabelAnchor();
        final ProxySubject item = mSelectedItems.iterator().next();
        if (item instanceof LabelBlockSubject) {
          mSelectedLabelBlock = (LabelBlockSubject) item;
        }
        return true;
      default:
        updateLabelAnchor();
        return true;
      }
    } else {
      return false;
    }
  }

  /**
   * Returns the current selection mode.
   * @see SelectionMode
   */
  public SelectionMode getMode()
  {
    return mMode;
  }

  /**
   * <P>Returns a detailed specification of what graph need to be deleted to
   * delete the currently selected items.</P>
   * <P>This includes all items in the selection; additionally, when a
   * deleting nodes ({@link NodeProxy}), all attached edges ({@link EdgeProxy})
   * must also be deleted and are included in the result.</P>
   * @return A list of {@link InsertInfo} objects containing the items to
   *         be deleted and their parents/positions in the graph.
   */
  public List<InsertInfo> getDeletionVictims()
  {
    switch (mMode) {
    case SUBGRAPH_SINGLE:
    case SUBGRAPH_MULTIPLE:
    {
      final InsertInfoVisitor visitor = new InsertInfoVisitor();
      final ProxySubject first = mSelectedItems.iterator().next();
      final GraphSubject graph =
        SubjectTools.getAncestor(first, GraphSubject.class);
      for (final EdgeSubject edge : graph.getEdgesModifiable()) {
        if (isSelected(edge) ||
            isSelected(edge.getSource()) ||
            isSelected(edge.getTarget())) {
          visitor.addInsertInfo(edge, graph);
        }
      }
      // Now for the other stuff ...
      for (final ProxySubject item : mSelectedItems) {
        visitor.addInsertInfo(item);
      }
      return visitor.getInsertInfoList();
    }
    case EVENT_LABELS:
    {
      final InsertInfoVisitor visitor = new InsertInfoVisitor();
      visitor.addInsertInfo(mSelectedLabelBlock);
      return visitor.getInsertInfoList();
    }
    default:
      return Collections.emptyList();
    }
  }

  /**
   * Returns the number of currently selected items.
   * The count includes only actually selected items, not items that are
   * only rendered as selected.
   */
  public int getNumberOfSelectedItems()
  {
    return mSelectedItems.size();
  }

  /**
   * Returns the currently selected label block.
   * @return In selection mode {@link SelectionMode#EVENT_LABELS
   *         EVENT_LABELS}, this method returns the label block containing the
   *         selected event labels.
   *         In selection mode {@link SelectionMode#SUBGRAPH_SINGLE
   *         SUBGRAPH_SINGLE}, this method returns the selected item,
   *         if it is a label block.
   *         In all other cases, the result is is <CODE>null</CODE>.
   */
  public LabelBlockSubject getSelectedLabelBlock()
  {
    return mSelectedLabelBlock;
  }

  /**
   * Returns the selection anchor. The anchor typically is the last item
   * added to the selection, which is used as the start point for drag-select
   * operations. The selection anchor may be <CODE>null</CODE> even for a
   * non-empty selection, if the previous anchor gets deselected while other
   * items are still selected.
   * @return The selection anchor or <CODE>null</CODE>.
   * @see #shiftSelect(Proxy) shiftSelect()
   */
  public ProxySubject getSelectionAnchor()
  {
    return mAnchor;
  }

  /**
   * Returns whether the selection is currently empty.
   */
  public boolean isEmpty()
  {
    return mMode == SelectionMode.EMPTY;
  }

  /**
   * <P>Returns whether the given item is rendered as selected.
   * In addition to selected items, other graphical elements may be
   * rendered as selected although not selected themselves:</P>
   * <UL>
   * <LI>In modes {@link SelectionMode#SUBGRAPH_SINGLE SUBGRAPH_SINGLE} and
   *     {@link SelectionMode#SUBGRAPH_MULTIPLE SUBGRAPH_MULTIPLE}, the names
   *     of any selected simple nodes ({@link SimpleNodeProxy}) are rendered
   *     as selected.</LI>
   * <LI>In mode {@link SelectionMode#EVENT_LABELS EVENT_LABELS}, the label
   *     block containing the selected event labels is rendered as selected.
   *     Also, the children of any selected nested block ({@link
   *     NestedBlockProxy}) are rendered as selected although not included
   *     in the list of selected items.</LI>
   * </UL>
   * @see #isSelected(Proxy) isSelected()
   */
  public boolean isRenderedSelected(final ProxySubject item)
  {
    return mRenderVisitor.isRenderedSelected(item);
  }

  /**
   * <P>Returns whether the given item is selected. Only items explicitly
   * included in the list of selected items are considered as selected,
   * not items that are merely rendered as selected.
   * @see #isRenderedSelected(ProxySubject) isRenderedSelected()
   */
  public boolean isSelected(final Proxy proxy)
  {
    return mSelectedItems.contains(proxy);
  }

  /**
   * Returns whether the given label block is considered as selected.
   * In mode {@link SelectionMode#EVENT_LABELS EVENT_LABELS}, this method
   * returns <CODE>true</CODE> if the given label block is the label block
   * containing the currently selected event labels. Otherwise, the method
   * determines whether the label block is currently selected.
   * @see #isSelected(Proxy) isSelected()
   */
  public boolean isSelectedLabelBlock(final LabelBlockProxy block)
  {
    switch (mMode) {
    case SUBGRAPH_SINGLE:
    case SUBGRAPH_MULTIPLE:
      return isSelected(block);
    case EVENT_LABELS:
      return block == mSelectedLabelBlock;
    default:
      return false;
    }
  }

  /**
   * Returns whether the given item is the only selected item.
   * @return <CODE>true</CODE> if the mode is {@link
   *         SelectionMode#SUBGRAPH_SINGLE SUBGRAPH_SINGLE} and the
   *         given item is selected.
   * @see #isSelected(Proxy) isSelected()
   */
  public boolean isSingleSelectedItem(final Proxy proxy)
  {
    return mMode == SelectionMode.SUBGRAPH_SINGLE && isSelected(proxy);
  }

  /**
   * Changes the selection to consist of only the given item.
   * @return <CODE>true</CODE> if the selection was changed by this call.
   */
  public boolean replace(final Proxy proxy)
  {
    final List<Proxy> list = Collections.singletonList(proxy);
    return replace(list);
  }

  /**
   * Changes the selection to consist of only the given items.
   * This method clears the selection and then adds the given items to
   * the selection. The resulting selection may contain additional items
   * if it is not possible to select only the given items.
   * @return <CODE>true</CODE> if the selection was changed by this call.
   */
  public boolean replace(final List<? extends Proxy> proxies)
  {
    final SelectionMode oldKind = mMode;
    final List<Proxy> oldList = new ArrayList<>(mSelectedItems);
    clear();
    selectAll(proxies);
    if (oldKind != mMode || oldList.size() != mSelectedItems.size()) {
      return true;
    } else {
      final Iterator<Proxy> oldIter = oldList.iterator();
      final Iterator<ProxySubject> iter = mSelectedItems.iterator();
      while (oldIter.hasNext()) {
        if (oldIter.next() != iter.next()) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Adds the given item to the current selection.
   * This may change the selection mode and add other items, if it is not
   * possible to add only the given item.
   * @return <CODE>true</CODE> if the selection was changed by this call.
   */
  public boolean select(final Proxy proxy)
  {
    final ProxySubject item = (ProxySubject) proxy;
    return mSelectVisitor.select(item);
  }

  /**
   * Adds the given items to the current selection.
   * This may change the selection mode and add other items, if it is not
   * possible to add only the given items.
   * @return <CODE>true</CODE> if the selection was changed by this call.
   */
  public boolean selectAll(final List<? extends Proxy> proxies)
  {
    boolean result = false;
    for (final Proxy proxy : proxies) {
      result |= select(proxy);
    }
    return result;
  }

  /**
   * Adds a range of items to the selection.
   * In mode {@link SelectionMode#EVENT_LABELS EVENT_LABELS}, if the given
   * item is in the selected label block and the selection anchor is set,
   * this method adds all items between the selection anchor and the given
   * item to the selection.
   * Otherwise this method has the same effect as selecting the given item.
   * @return <CODE>true</CODE> if the selection was changed by this call.
   * @see #select(Proxy) select()
   */
  public boolean shiftSelect(final Proxy proxy)
  {
    final ProxySubject item = (ProxySubject) proxy;
    if (mMode == SelectionMode.EVENT_LABELS &&
        mAnchor != null &&
        SubjectTools.isAncestor(mSelectedLabelBlock, item)) {
      final Set<ProxySubject> old = mSelectedItems;
      mSelectedItems = new LinkedHashSet<>();
      mShiftSelectVisitor.selectRange(mAnchor, item);
      return !mSelectedItems.equals(old);
    } else {
      return select(proxy);
    }
  }

  /**
   * Toggles the selection status of the given item.
   * If the given item is currently selected, it is deselected,
   * otherwise it is selected.
   * @see #deselect(Proxy) select()
   * @see #select(Proxy) select()
   */
  public void toggle(final Proxy proxy)
  {
    if (isSelected(proxy)) {
      deselect(proxy);
    } else {
      select(proxy);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void deselectDescendants(final ProxySubject item)
  {
    final Iterator<ProxySubject> iter = mSelectedItems.iterator();
    while (iter.hasNext()) {
      final ProxySubject descendant = iter.next();
      if (item != descendant && SubjectTools.isAncestor(item, descendant)) {
        iter.remove();
      }
    }
  }

  private boolean selectLabelBlockItem(final ProxySubject item)
  {
    ProxySubject parent = item;
    do {
      if (mSelectedItems.contains(parent)) {
        return false;
      }
      parent = SubjectTools.getProxyParent(parent);
    } while (parent instanceof NestedBlockSubject);
    return mSelectedItems.add(item);  // = true
  }

  private void updateLabelAnchor()
  {
    if (mAnchor != null && !mRenderVisitor.isRenderedSelected(mAnchor)) {
      mAnchor = null;
    }
  }


  //#########################################################################
  //# Inner Class SelectionIterator
  private static class SelectionIterator implements Iterator<ProxySubject>
  {
    //#######################################################################
    //# Constructor
    public SelectionIterator(final GraphSelection selection)
    {
      mIterator = selection.mSelectedItems.iterator();
    }

    //#######################################################################
    //# Interface java.util.Iterator<ProxySubject>
    @Override
    public boolean hasNext()
    {
      return mIterator.hasNext();
    }

    @Override
    public ProxySubject next()
    {
      return mIterator.next();
    }

    //#######################################################################
    //# Data Members
    private final Iterator<ProxySubject> mIterator;
  }


  //#########################################################################
  //# Inner Class SelectVisitor
  private class SelectVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private boolean select(final ProxySubject item)
    {
      try {
        return (Boolean) item.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    /**
     * Select something that is neither a label nor label geometry.
     * Label blocks are partially handled by this method.
     */
    @Override
    public Boolean visitProxy(final Proxy proxy)
    {
      final ProxySubject item = (ProxySubject) proxy;
      mAnchor = item;
      switch (mMode) {
      case EMPTY:
        mMode = SelectionMode.SUBGRAPH_SINGLE;
        return mSelectedItems.add(item); // = true
      case SUBGRAPH_SINGLE:
        if (mSelectedItems.add(item)) {
          mMode = SelectionMode.SUBGRAPH_MULTIPLE;
          mSelectedLabelBlock = null;
          return true;
        } else {
          return false;
        }
      case SUBGRAPH_MULTIPLE:
        return mSelectedItems.add(item);
      case EVENT_LABELS:
        mSelectedItems.clear();
        mSelectedItems.add(mSelectedLabelBlock);
        mSelectedItems.add(item);
        mMode = mSelectedItems.size() == 1 ?
          SelectionMode.SUBGRAPH_SINGLE : SelectionMode.SUBGRAPH_MULTIPLE;
        return true;
      case NODE_LABELS:
        final List<ProxySubject> labels = new ArrayList<>(mSelectedItems);
        mSelectedItems.clear();
        for (final ProxySubject label : labels) {
          final SimpleNodeSubject node =
            SubjectTools.getAncestor(label, SimpleNodeSubject.class);
          mSelectedItems.add(node);
        }
        mSelectedItems.add(item);
        mMode = mSelectedItems.size() == 1 ?
          SelectionMode.SUBGRAPH_SINGLE : SelectionMode.SUBGRAPH_MULTIPLE;
        return true;
      default:
        throw new IllegalStateException("Unknow selection kind " + mMode + "!");
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ModuleProxyVisitor
    @Override
    public Boolean visitIdentifierProxy(final IdentifierProxy proxy)
    {
      final IdentifierSubject ident = (IdentifierSubject) proxy;
      return visitLabelBlockItem(ident);
    }

    @Override
    public Boolean visitLabelBlockProxy(final LabelBlockProxy proxy)
    {
      if (visitProxy(proxy)) {
        if (mMode == SelectionMode.SUBGRAPH_SINGLE) {
          mSelectedLabelBlock = (LabelBlockSubject) proxy;
        }
        return true;
      } else {
        return false;
      }
    }

    @Override
    public Object visitLabelGeometryProxy(final LabelGeometryProxy proxy)
    {
      final LabelGeometrySubject geo = (LabelGeometrySubject) proxy;
      switch (mMode) {
      case EMPTY:
        mMode = SelectionMode.NODE_LABELS;
        // fall through ...
      case NODE_LABELS:
        return mSelectedItems.add(geo);
      default:
        final SimpleNodeSubject node =
          SubjectTools.getAncestor(geo, SimpleNodeSubject.class);
        return visitProxy(node);
      }
    }

    @Override
    public Boolean visitNestedBlockProxy(final NestedBlockProxy proxy)
    {
      final NestedBlockSubject block = (NestedBlockSubject) proxy;
      return visitLabelBlockItem(block);
    }

    //#######################################################################
    //# Auxiliary Methods
    private Boolean visitLabelBlockItem(final ProxySubject item)
    {
      final LabelBlockSubject block =
        SubjectTools.getAncestor(item, LabelBlockSubject.class);
      switch (mMode) {
      case EMPTY:
        mMode = SelectionMode.EVENT_LABELS;
        mSelectedLabelBlock = block;
        mAnchor = item;
        mSelectedItems.add(item);
        return true;
      case EVENT_LABELS:
        if (mSelectedLabelBlock == block) {
          mAnchor = item;
          if (selectLabelBlockItem(item)) {
            deselectDescendants(item);
            return true;
          } else {
            return false;
          }
        } else {
          return visitLabelBlockProxy(block);
        }
      case SUBGRAPH_SINGLE:
        if (mSelectedLabelBlock == block) {
          mMode = SelectionMode.EVENT_LABELS;
          mAnchor = item;
          mSelectedItems.clear();
          return mSelectedItems.add(item); // = true
        } else {
          return visitLabelBlockProxy(block);
        }
      default:
        return visitLabelBlockProxy(block);
      }
    }
  }


  //#########################################################################
  //# Inner Class ShiftSelectVisitor
  private class ShiftSelectVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private void selectRange(final ProxySubject first, final ProxySubject last)
    {
      try {
        mFirst = first;
        mLast = last;
        mSelecting = false;
        mSelectedLabelBlock.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mFirst = mLast = null;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ModuleProxyVisitor
    @Override
    public Boolean visitIdentifierProxy(final IdentifierProxy proxy)
    {
      final IdentifierSubject ident = (IdentifierSubject) proxy;
      return visitLabelBlockItem(ident);
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy block)
      throws VisitorException
    {
      for (final Proxy proxy : block.getEventIdentifierList()) {
        final boolean more = (Boolean) proxy.acceptVisitor(this);
        if (!more) {
          break;
        }
      }
      return null;
    }

    @Override
    public Boolean visitNestedBlockProxy(final NestedBlockProxy proxy)
      throws VisitorException
    {
      final NestedBlockSubject block = (NestedBlockSubject) proxy;
      boolean more = visitLabelBlockItem(block);
      if (more) {
        for (final Proxy child : block.getBody()) {
          more = (Boolean) child.acceptVisitor(this);
          if (!more) {
            break;
          }
        }
      }
      return more;
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean visitLabelBlockItem(final ProxySubject item)
    {
      if (!mSelecting) {
        if (item == mFirst) {
          mSelecting = true;
          selectLabelBlockItem(item);
        } else if (item == mLast) {
          mLast = mFirst;
          mFirst = item;
          mSelecting = true;
          selectLabelBlockItem(item);
        }
        return true;
      } else {
        selectLabelBlockItem(item);
        if (item == mLast) {
          mSelecting = false;
          return false;
        } else {
          return true;
        }
      }
    }

    //#######################################################################
    //# Data Members
    private ProxySubject mFirst;
    private ProxySubject mLast;
    private boolean mSelecting;
  }


  //#########################################################################
  //# Inner Class RenderVisitor
  private class RenderVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private boolean isRenderedSelected(final ProxySubject item)
    {
      try {
        return (Boolean) item.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public Boolean visitProxy(final Proxy proxy)
    {
      return mSelectedItems.contains(proxy);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ModuleProxyVisitor
    @Override
    public Boolean visitIdentifierProxy(final IdentifierProxy proxy)
    {
      return visitLabelBlockItem(proxy);
    }

    @Override
    public Boolean visitLabelBlockProxy(final LabelBlockProxy proxy)
    {
      final LabelBlockSubject block = (LabelBlockSubject) proxy;
      switch (mMode) {
      case SUBGRAPH_SINGLE:
      case SUBGRAPH_MULTIPLE:
        return mSelectedItems.contains(block);
      case EVENT_LABELS:
        return mSelectedLabelBlock == block;
      default:
        return false;
      }
    }

    @Override
    public Boolean visitLabelGeometryProxy(final LabelGeometryProxy proxy)
    {
      final LabelGeometrySubject geo = (LabelGeometrySubject) proxy;
      switch (mMode) {
      case SUBGRAPH_SINGLE:
      case SUBGRAPH_MULTIPLE:
        final SimpleNodeSubject node =
          SubjectTools.getAncestor(geo, SimpleNodeSubject.class);
        return mSelectedItems.contains(node);
      case NODE_LABELS:
        return mSelectedItems.contains(geo);
      default:
        return false;
      }
    }

    @Override
    public Boolean visitNestedBlockProxy(final NestedBlockProxy proxy)
    {
      return visitLabelBlockItem(proxy);
    }

    //#######################################################################
    //# Auxiliary Methods
    private Boolean visitLabelBlockItem(final Proxy proxy)
    {
      ProxySubject item = (ProxySubject) proxy;
      do {
        if (mSelectedItems.contains(item)) {
          return true;
        }
        item = SubjectTools.getProxyParent(item);
      } while (item instanceof NestedBlockSubject);
      return false;
    }
  }


  //#########################################################################
  //# Inner Class InsertInfoVisitor
  private class InsertInfoVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Constructor
    private InsertInfoVisitor()
    {
      mInsertInfoList = new ArrayList<>(getNumberOfSelectedItems());
    }

    //#######################################################################
    //# Invocation
    private void addInsertInfo(final Proxy proxy)
    {
      try {
        proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    private List<InsertInfo> getInsertInfoList()
    {
      return mInsertInfoList;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public Object visitProxy(final Proxy proxy)
    {
      final ProxySubject item = (ProxySubject) proxy;
      final ProxySubject parent = SubjectTools.getProxyParent(item);
      addInsertInfo(proxy, parent);
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ModuleProxyVisitor
    @Override
    public Object visitEdgeProxy(final EdgeProxy proxy)
    {
      // skip
      return null;
    }

    @Override
    public Object visitGuardActionBlockProxy(final GuardActionBlockProxy proxy)
    {
      final GuardActionBlockSubject ga = (GuardActionBlockSubject) proxy;
      final ProxySubject parent = SubjectTools.getProxyParent(ga);
      if (!isSelected(parent)) {
        addInsertInfo(proxy, parent);
      }
      return null;
    }

    @Override
    public Boolean visitIdentifierProxy(final IdentifierProxy proxy)
    {
      // skip
      return null;
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy proxy)
      throws VisitorException
    {
      final LabelBlockSubject block = (LabelBlockSubject) proxy;
      switch (mMode) {
      case SUBGRAPH_SINGLE:
      case SUBGRAPH_MULTIPLE:
        final ProxySubject parent = SubjectTools.getProxyParent(block);
        if (parent instanceof GraphSubject) {
          addInsertInfo(proxy, parent);
        } else if (!isSelected(parent)) {
          final ListSubject<AbstractSubject> elist =
            block.getEventIdentifierListModifiable();
          addInsertInfo(elist, true);
        }
        return null;
      case EVENT_LABELS:
        final ListSubject<AbstractSubject> elist =
          block.getEventIdentifierListModifiable();
        addInsertInfo(elist, false);
        return null;
      default:
        return null;
      }
    }

    @Override
    public Boolean visitNestedBlockProxy(final NestedBlockProxy proxy)
      throws VisitorException
    {
      final NestedBlockSubject block = (NestedBlockSubject) proxy;
      final ListSubject<AbstractSubject> elist = block.getBodyModifiable();
      addInsertInfo(elist, false);
      return null;
    }

    //#######################################################################
    //# Auxiliary Methods
    private void addInsertInfo(final Proxy proxy, final ProxySubject parent)
    {
      final GraphInsertPosition insPos = new GraphInsertPosition(parent);
      final InsertInfo info = new InsertInfo(proxy, insPos);
      mInsertInfoList.add(info);
    }

    private void addInsertInfo(final ListSubject<AbstractSubject> elist,
                               final boolean all)
      throws VisitorException
    {
      for (int i = 0; i < elist.size(); i++) {
        final ProxySubject item = elist.get(i);
        if (all || isSelected(item)) {
          final ListInsertPosition insPos = new ListInsertPosition(elist, i);
          final InsertInfo info = new InsertInfo(item, insPos);
          mInsertInfoList.add(info);
        } else {
          item.acceptVisitor(this);
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final List<InsertInfo> mInsertInfoList;
  }


  //#########################################################################
  //# Inner Enumeration SelectionKind
  /**
   * Enumeration of possible selection modes.
   */
  public enum SelectionMode {
    /**
     * An empty selection. In this mode, the set {@link #mSelectedItems} is
     * empty and the {@link #mSelectedLabelBlock} is <CODE>null</CODE>.
     */
    EMPTY,
    /**
     * <P>A selection of a single graphical element, e.g., node, edge, or
     * label block. In this mode, the set {@link GraphSelection#mSelectedItems
     * mSelectedItems} contains exactly one element. If the selected item is a
     * label block, it is recorded as the {@link
     * GraphSelection#mSelectedLabelBlock mSelectedLabelBlock}.</P>
     *
     * <P>The addition of another graphical element changes the mode to
     * {@link #SUBGRAPH_MULTIPLE}, while the addition of an event label
     * changes it to {@link #EVENT_LABELS}.</P>
     */
    SUBGRAPH_SINGLE,
    /**
     * A selection consisting of more than one graphical element, e.g., nodes,
     * edges, or label block. In this mode, the set {@link
     * GraphSelection#mSelectedItems mSelectedItems} contains more than one
     * selected elements, and the {@link GraphSelection#mSelectedLabelBlock
     * mSelectedLabelBlock} is <CODE>null</CODE>.
     */
    SUBGRAPH_MULTIPLE,
    /**
     * <P>A selection consisting of more elements of a label block.
     * The selected elements may be event identifiers ({@link IdentifierProxy})
     * or nested blocks ({@link NestedBlockProxy}), all in the same label block
     * ({@link LabelBlockProxy}). In this mode, the set {@link
     * GraphSelection#mSelectedItems mSelectedItems} contains one or more
     * event identifiers or nested blocks, and the {@link
     * GraphSelection#mSelectedLabelBlock mSelectedLabelBlock} is the label
     * block containing them.</P>
     *
     * <P>If the selection contains a nested block, its children elements are
     * implicitly selected, but not contained in {@link
     * GraphSelection#mSelectedItems mSelectedItems}. The
     * same holds for the label block.</P>
     *
     * <P>If another graphical element is added to the selection, then the
     * selection mode changes to {@link #SUBGRAPH_MULTIPLE}: the labels are
     * deselected, and the label block is selected together with the added
     * graphical element. If a label in another label block is added to the
     * selection, the selection mode also changes to {@link
     * #SUBGRAPH_MULTIPLE}, a selection consisting of two label blocks.</P>
     */
    EVENT_LABELS,
    /**
     * A selection consisting of one or more node labels. In this mode,
     * the set {@link GraphSelection#mSelectedItems mSelectedItems} contains
     * the label geometries ({@link LabelGeometryProxy}) of the simple nodes
     * (@link SimpleNodeProxy}) whose names are selected, and the {@link
     * GraphSelection#mSelectedLabelBlock mSelectedLabelBlock} is
     * <CODE>null</CODE>. If a graphical element other than a node label is
     * added to the selection, then the selection mode changes to {@link
     * #SUBGRAPH_MULTIPLE}, consisting of the simple associated with the
     * labels in the selection plus the newly selected item.
     */
    NODE_LABELS
  }


  //#########################################################################
  //# Data Members
  private final SelectVisitor mSelectVisitor = new SelectVisitor();
  private final ShiftSelectVisitor mShiftSelectVisitor =
    new ShiftSelectVisitor();
  private final RenderVisitor mRenderVisitor = new RenderVisitor();

  /**
   * The current mode of selection.
   */
  private SelectionMode mMode = SelectionMode.EMPTY;
  /**
   * The set of currently selected items. This set only contains items
   * that are actually selected&mdash;implicitly selected items that
   * are merely rendered as selected are not contained. The set maintains its
   * elements in the order in which they are added to the selection.
   * @see SelectionMode
   * @see #isRenderedSelected(ProxySubject) isRenderedSelected()
   */
  private Set<ProxySubject> mSelectedItems = new LinkedHashSet<>();
  /**
   * The currently selected label block or <CODE>null</CODE>
   * In selection mode {@link SelectionMode#EVENT_LABELS}, this variable
   * records the label block containing the selected event labels, which is
   * rendered as selected.
   * In selection mode {@link SelectionMode#SUBGRAPH_SINGLE}, this variable
   * contains the selected item, if it is a label block.
   * In all other cases, it is <CODE>null</CODE>.
   */
  private LabelBlockSubject mSelectedLabelBlock = null;
  /**
   * The selection anchor.
   * @see #getSelectionAnchor()
   * @see #shiftSelect(Proxy) shiftSelect()
   */
  private ProxySubject mAnchor = null;

}
