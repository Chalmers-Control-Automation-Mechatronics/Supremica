//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.gui;

import gnu.trove.set.hash.THashSet;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.RearrangeTreeCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EventAliasSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.IdentifiedSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import org.supremica.gui.ide.ModuleContainer;


/**
 * The Tree used to view the constant aliases, event aliases, components and
 * propositions of a module.
 *
 * @author Carly Hona, Robi Malik
 */
public abstract class ModuleTree
  extends JTree
  implements SelectionOwner, Autoscroll, TreeSelectionListener, FocusListener
{

  public ModuleTree(final ModuleContainer rootWindow,
                    final WatersPopupActionManager manager,
                    final ProxySubject root,
                    final UndoInterface undo)
  {
    mModuleContainer = rootWindow;
    mRoot = root;
    mUndoInterface = undo;
    mModuleContext = mModuleContainer.getModuleContext();
    mIsPermanentFocusOwner = true;
    mModel = new ModuleTreeModel(root, getRootList());
    setModel(mModel);
    final MouseListener handler = new ModuleTreeMouseListener();
    addMouseListener(handler);
    addTreeSelectionListener(this);
    addFocusListener(this);

    mListVisitor = new GetListVisitor();
    mAcceptTransferableVisitor = new AcceptTransferableVisitor();
    mDoubleClickVisitor = new DoubleClickVisitor();

    if (getSupportedDataFlavor() == WatersDataFlavor.COMPONENT) {
      setRootVisible(false);
    } else {
      setRootVisible(true);
    }
    setShowsRootHandles(true);
    setAutoscrolls(true);
    // Two extra pixels, hopefully enough to underline errors ...
    setRowHeight(IconAndFontLoader.getWatersIconSize() + 2);
    // Don't expand/collapse on double-click, never collapse the root.
    setToggleClickCount(0);
    manager.installCutCopyPasteActions(this);

    setTransferHandler(new ModuleTreeTransferHandler());
    setDragEnabled(true);
    setDropMode(DropMode.INSERT);

    setCellRenderer(new ModuleTreeRenderer());
    expandAll(getRootList());

    ToolTipManager.sharedInstance().registerComponent(this);
  }


  //#########################################################################
  //# Simple Access
  public ModuleTreeModel getModuleTreeModel()
  {
    return (ModuleTreeModel) getModel();
  }

  public ModuleContainer getModuleContainer()
  {
    return mModuleContainer;
  }

  public ProxySubject getRoot()
  {
    return mRoot;
  }

  public FocusTracker getFocusTracker()
  {
    return mModuleContainer.getIDE().getFocusTracker();
  }

  private boolean isSourceOfDrag()
  {
    return getFocusTracker().getWatersSelectionOwner() == this;
  }


  //#########################################################################
  //# Abstract Methods
  abstract ListSubject<? extends ProxySubject> getRootList();

  abstract String getRootName();

  abstract DataFlavor getSupportedDataFlavor();

  abstract PopupFactory getPopupFactory();

  boolean shouldForceCopy(final DataFlavor flavor,
                          final Transferable transferable)
  {
    return false;
  }


  //#######################################################################
  //# Interface java.awt.dnd.Autoscroll
  @Override
  public Insets getAutoscrollInsets()
  {
    final Rectangle all = getBounds();
    final Rectangle visible = getParent().getBounds();
    return new Insets(visible.y - all.y + 20, 0, all.height - visible.height
                                                 - visible.y + all.y + 20, 0);
  }

  @Override
  public void autoscroll(final Point cursorLocn)
  {
    int realrow = getClosestRowForLocation(cursorLocn.x, cursorLocn.y);
    final Rectangle panel = getBounds();
    if (cursorLocn.y + panel.y <= 20) {
      if (realrow < 1) {
        realrow = 0;
      } else {
        realrow--;
      }
    } else if (realrow < getRowCount() - 1) {
      realrow++;
    }
    scrollRowToVisible(realrow);
  }


  //#########################################################################
  //# Interface java.awt.event.FocusListener
  @Override
  public void focusGained(final FocusEvent event)
  {
    if (!event.isTemporary()) {
      mIsPermanentFocusOwner = true;
      repaint();
    }
  }

  @Override
  public void focusLost(final FocusEvent event)
  {
    if (!event.isTemporary()) {
      mIsPermanentFocusOwner = false;
      repaint();
    }
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.gui.observer.Subject
  @Override
  public void attach(final Observer observer)
  {
    if (mObservers == null) {
      mObservers = new LinkedList<Observer>();
    }
    mObservers.add(observer);
  }

  @Override
  public void detach(final Observer observer)
  {
    if (mObservers != null) {
      mObservers.remove(observer);
      if (mObservers.isEmpty()) {
        mObservers = null;
      }
    }
  }

  @Override
  public void fireEditorChangedEvent(final EditorChangedEvent event)
  {
    if (mObservers != null) {
      // Just in case they try to register or deregister observers
      // in response to the update ...
      final List<Observer> copy = new ArrayList<Observer>(mObservers);
      for (final Observer observer : copy) {
        observer.update(event);
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.transfer.SelectionOwner
  @Override
  public UndoInterface getUndoInterface(final Action action)
  {
    return mUndoInterface;
  }

  @Override
  public boolean hasNonEmptySelection()
  {
    return !isSelectionEmpty();
  }

  @Override
  public boolean canSelectMore()
  {
    if (rootVisible) {
      return getSelectionCount() < getRowCount() - 1;
    }
    return getSelectionCount() < getRowCount();
  }

  @Override
  public boolean isSelected(final Proxy proxy)
  {
    final ModuleTreeModel model = getModuleTreeModel();
    final ProxySubject subject = (ProxySubject) proxy;
    final TreePath path = model.createPath(subject);
    return isPathSelected(path);
  }

  @Override
  public List<Proxy> getCurrentSelection()
  {
    final int count = getSelectionCount();
    final List<Proxy> result = new ArrayList<Proxy>(count);
    final int min = getMinSelectionRow();
    final int max = getMaxSelectionRow();
    for (int row = min; row <= max; row++) {
      if (isRowSelected(row)) {
        final TreePath path = getPathForRow(row);
        final Proxy proxy = (Proxy) path.getLastPathComponent();
        result.add(proxy);
      }
    }
    return result;
  }

  @Override
  public List<Proxy> getAllSelectableItems()
  {
    final int total = getRowCount();
    final List<Proxy> result = new ArrayList<Proxy>(total);
    for (int row = (rootVisible ? 1 : 0); row < total; row++) {
      final TreePath path = getPathForRow(row);
      final Proxy proxy = (Proxy) path.getLastPathComponent();
      result.add(proxy);
    }
    return result;
  }

  @Override
  public Proxy getSelectionAnchor()
  {
    final TreePath path = getAnchorSelectionPath();
    if (path == null) {
      return mModuleContext.getModule();
    } else {
      return (Proxy) path.getLastPathComponent();
    }
  }

  @Override
  public Proxy getSelectableAncestor(final Proxy item)
  {
    final ModuleTreeModel model = getModuleTreeModel();
    if (item == model.getRoot()) {
      return null;
    } else {
      return model.getVisibleAncestorInTree(item);
    }
  }

  @Override
  public void clearSelection(final boolean propagate)
  {
    clearSelection();
  }

  @Override
  public void replaceSelection(final List<? extends Proxy> items)
  {
    clearSelection();
    addToSelection(items);
  }

  @Override
  public void addToSelection(final List<? extends Proxy> items)
  {
    final ModuleTreeModel model = getModuleTreeModel();
    for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      final TreePath path = model.createPath(subject);
      addSelectionPath(path);
    }
  }

  @Override
  public void removeFromSelection(final List<? extends Proxy> items)
  {
    final ModuleTreeModel model = getModuleTreeModel();
    for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      final TreePath path = model.createPath(subject);
      removeSelectionPath(path);
    }
  }

  @Override
  public boolean canPaste(final Transferable transferable)
  {
    Proxy anchor = getSelectionAnchor();
    if (anchor == null) {
      anchor = mModuleContext.getModule();
    }
    DataFlavor flavor = mAcceptTransferableVisitor.getFlavorIfDropAllowed
        (anchor, transferable, true);
    if (flavor == null) {
      final Proxy parent = mModel.getProperAncestorInTree((Subject) anchor);
      if (parent == null) {
        return false;
      }
      flavor = mAcceptTransferableVisitor.getFlavorIfDropAllowed
        (parent, transferable, true);
    }
    return flavor != null;
  }

  @Override
  public List<InsertInfo> getInsertInfo(final Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    Proxy anchor = getSelectionAnchor();
    if (anchor == null) {
      anchor = mModuleContext.getModule();
    }
    DataFlavor flavor = mAcceptTransferableVisitor.getFlavorIfDropAllowed
      (anchor, transferable, true);
    Proxy parent = anchor;
    while (flavor == null) {
      anchor = parent;
      parent = mModel.getProperAncestorInTree((Subject) anchor);
      if (parent == null) {
        return null;
      }
      flavor = mAcceptTransferableVisitor.getFlavorIfDropAllowed
        (parent, transferable, true);
    };
    final ListSubject<? extends ProxySubject> listInModule =
      mModel.getChildren(parent);
    final int pos;
    if (parent == anchor) {
      pos = listInModule.size();
    } else {
      pos = mModel.getIndexOfChild(parent, anchor) + 1;
    }
    return getInsertInfo(transferable, flavor, listInModule, parent, pos);
  }

  @Override
  public boolean canDelete(final List<? extends Proxy> items)
  {
    return !items.isEmpty();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InsertInfo> getDeletionVictims(final List<? extends Proxy> items)
  {
    final ModuleTreeModel model = getModuleTreeModel();
    final Set<Proxy> set = new THashSet<>(items);
    final List<InsertInfo> result = new LinkedList<InsertInfo>();
    outer: for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      ProxySubject parent = model.getProperAncestorInTree(subject);
      if (parent != null) {
        while (parent.getParent() != null) {
          if (set.contains(parent)) {
            continue outer;
          }
          parent = model.getProperAncestorInTree(parent);
        }
        final ListSubject<? extends ProxySubject> list =
          (ListSubject<? extends ProxySubject>) subject.getParent();
        final int pos = list.indexOf(proxy);
        final ListInsertPosition inspos = new ListInsertPosition(list, pos);
        final InsertInfo delete = new InsertInfo(subject, inspos);
        result.add(delete);
      }
    }
    return result;
  }

  @Override
  public void insertItems(final List<InsertInfo> inserts)
  {
    for (final InsertInfo insert : inserts) {
      final ProxySubject item = (ProxySubject) insert.getProxy();
      final ListInsertPosition inspos =
        (ListInsertPosition) insert.getInsertPosition();
      @SuppressWarnings("unchecked")
      final List<ProxySubject> list = (List<ProxySubject>) inspos.getList();
      final int pos = inspos.getPosition();
      list.add(pos, item);
      expand(item);
    }
  }

  @Override
  public void deleteItems(final List<InsertInfo> deletes)
  {
    final int size = deletes.size();
    final ListIterator<InsertInfo> iter = deletes.listIterator(size);
    while (iter.hasPrevious()) {
      final InsertInfo delete = iter.previous();
      final ListInsertPosition inspos =
        (ListInsertPosition) delete.getInsertPosition();
      final ListSubject<? extends ProxySubject> list = inspos.getList();
      final int index = inspos.getPosition();
      list.remove(index);
    }
  }

  @Override
  public void scrollToVisible(final List<? extends Proxy> list)
  {
    if (list.isEmpty()) {
      return;
    }
    final ModuleTreeModel model = getModuleTreeModel();
    final Iterator<? extends Proxy> iter = list.iterator();
    final Proxy next = iter.next();
    final ProxySubject first;
    if (next != model.getRoot() || isRootVisible()) {
      first = (ProxySubject) next;
    } else if (iter.hasNext()) {
      first = (ProxySubject) iter.next();
    } else {
      return;
    }
    final TreePath firstpath = model.createPath(first);
    final Rectangle rect = getPathBounds(firstpath);
    final int size = list.size();
    if (size > 1) {
      final ProxySubject last =
        (ProxySubject) list.listIterator(size).previous();
      final TreePath lastpath = model.createPath(last);
      final Rectangle lastrect = getPathBounds(lastpath);
      final int y = lastrect.y + lastrect.height;
      rect.height = y - rect.y;
    }
    scrollRectToVisible(rect);
  }

  @Override
  public void activate()
  {
    if (!isFocusOwner()) {
      mModuleContainer.getEditorPanel().showPanel(this);
      requestFocusInWindow();
    }
  }

  @Override
  public void close()
  {
    final ModuleTreeModel model = getModuleTreeModel();
    model.close();
    ToolTipManager.sharedInstance().unregisterComponent(this);
  }

  private Proxy getClickedItem(final MouseEvent event)
  {
    final TreePath path = getPathForLocation(event.getX(), event.getY());
    return path == null ? null : (Proxy) path.getLastPathComponent();
  }

  private void fireSelectionChanged()
  {
    final EditorChangedEvent event = new SelectionChangedEvent(this);
    fireEditorChangedEvent(event);
  }


  //#########################################################################
  //# Overrides for javax.swing.JTree
  @Override
  public String getToolTipText(final MouseEvent event)
  {
    final TreePath path = getClosestPathForLocation(event.getX(), event.getY());
    if (path == null) {
      return null;
    }
    final Proxy location = (Proxy) path.getLastPathComponent();
    return mModuleContext.getToolTipText(location);
  }


  //#########################################################################
  //# Interface javax.swing.event.TreeSelectionListener
  @Override
  public void valueChanged(final TreeSelectionEvent event)
  {
    // Why can't the new selection be read immediately ???
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run()
      {
        fireSelectionChanged();
      }
    });
  }


  //#########################################################################
  //# Auxiliary Methods
  private void expandOrCollapseRoot()
  {
    final TreePath path = getPathForRow(0);
    if (isExpanded(path)) {
      collapsePath(path);
    } else {
      expandPath(path);
    }
  }

  private void expandAll(final List<? extends ProxySubject> list)
  {
    if (getSupportedDataFlavor() == WatersDataFlavor.CONSTANT_ALIAS) {
      expandPath(getPathForRow(0));
    } else {
      for (final ProxySubject subject : list) {
        expand(subject);
      }
    }
  }

  private void expand(final ProxySubject subject)
  {
    if (subject instanceof ForeachSubject) {
      final ForeachSubject foreach = (ForeachSubject) subject;
      final TreePath path = mModel.createPath(subject);
      expandPath(path);
      final List<? extends ProxySubject> body = foreach.getBodyModifiable();
      expandAll(body);
    }
  }

  private boolean hasAncestorInSelection(final ProxySubject proxy,
                                         final List<Proxy> proxies)
  {
    for (int i = 0; i < proxies.size(); i++) {
      if (SubjectTools.isAncestor((ProxySubject) proxies.get(i), proxy)) {
        return true;
      }
    }
    return false;
  }

  private String getPrintString(final Proxy proxy, final boolean expanded)
  {
    try {
      final StringWriter writer = new StringWriter();
      final PrintVisitor printer = new PrintVisitor(writer, expanded);
      printer.pprint(proxy);
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public List<InsertInfo> getInsertInfo(final Transferable transferable,
                                        final DataFlavor flavor,
                                        final ListSubject<? extends ProxySubject> list,
                                        final Proxy dropTarget, int index)
    throws IOException, UnsupportedFlavorException
  {
    final List<Proxy> transferData =
      mListVisitor.getListOfAcceptedItems(dropTarget, transferable, flavor);
    final ModuleProxyCloner cloner =
      ModuleSubjectFactory.getCloningInstance();
    final ModuleSubject module = mModuleContainer.getModule();
    final ListSubject<EventDeclSubject> edeclList =
      module.getEventDeclListModifiable();
    int edeclPos = edeclList.size();
    final Set<String> names = new THashSet<>(transferData.size());
    final List<InsertInfo> result = new ArrayList<>(transferData.size());
    for (final Proxy proxy : transferData) {
      final Proxy cloned;
      final ListInsertPosition inspos;
      if (proxy instanceof EventDeclProxy) {
        // Special treatment for event declarations pasted with an automaton
        final EventDeclProxy decl = (EventDeclProxy) proxy;
        if (mModuleContext.getEventDecl(decl.getName()) != null) {
          continue;
        }
        final IdentifierProxy ident = decl.getIdentifier();
        if (ident instanceof IndexedIdentifierProxy) {
          final IndexedIdentifierProxy indexed = (IndexedIdentifierProxy) ident;
          if (mModuleContext.getEventDecl(indexed.getName()) != null) {
            continue;
          }
        } else if (!(ident instanceof SimpleIdentifierProxy)) {
          continue;
        }
        cloned = cloner.getClone(decl);
        inspos = new ListInsertPosition(edeclList, edeclPos++);
      } else {
        cloned = cloner.getClone(proxy);
        if (cloned instanceof IdentifiedSubject && list == getRootList()) {
          final IdentifiedSubject sub = (IdentifiedSubject) cloned;
          final IdentifierSubject newId =
            mModuleContext.getPastedName(sub, names);
          sub.setIdentifier(newId);
        }
        inspos = new ListInsertPosition(list, index++);
      }
      final InsertInfo info = new InsertInfo(cloned, inspos);
      result.add(info);
    }
    return result;
  }

  @Override
  public void setSelectionInterval(int index0, final int index1)
  {
    //so root not selected with ctrl-a select all
    if (rootVisible && index0 == 0) {
      index0 = 1;
    }
    if (index0 <= index1) {
      super.setSelectionInterval(index0, index1);
    } else {
      super.clearSelection();
    }
  }

  private void executeCommand(final Command command){
    if(mUndoInterface == null){
      command.execute();
    }
    else{
      mUndoInterface.executeCommand(command);
    }
  }


  //#########################################################################
  //# Inner Class ModuleTreeMouseListener
  /**
   * A simple mouse listener to trigger opening an editor dialog or
   * expand/collapse tree by double-click, or a popup menu by right-click.
   */
  private class ModuleTreeMouseListener extends MouseAdapter
  {
    //#######################################################################
    //# Interface java.awt.MouseListener
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1) {
        final Proxy proxy = getClickedItem(event);
        if (proxy == null || proxy instanceof ModuleProxy) {
          clearSelection();
        }
        if (event.getClickCount() == 2) {
          mDoubleClickVisitor.invokeDoubleClickAction(proxy);
        }
      }
    }

    @Override
    public void mousePressed(final MouseEvent event)
    {
      requestFocusInWindow();
      maybeShowPopup(event);
    }

    @Override
    public void mouseReleased(final MouseEvent event)
    {
      maybeShowPopup(event);
    }

    //#######################################################################
    //# Auxiliary Methods
    private void maybeShowPopup(final MouseEvent event)
    {
      final Proxy clicked = getClickedItem(event);
      if ((clicked == null || clicked instanceof ModuleProxy)
          && event.getButton() == MouseEvent.BUTTON1) {
        clearSelection();
      }
      getPopupFactory().maybeShowPopup(ModuleTree.this, event, clicked);

    }

  }


  //#########################################################################
  //# Inner Class DoubleClickVisitor
  private class DoubleClickVisitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private void invokeDoubleClickAction(final Proxy proxy)
    {
      if (proxy != null) {
        try {
          proxy.acceptVisitor(this);
        } catch (final VisitorException exception) {
          throw exception.getRuntimeException();
        }
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    /**
     * The default action pops up the properties dialog for the clicked item,
     * if a properties dialog is available. Otherwise it does nothing.
     */
    @Override
    public Object visitProxy(final Proxy proxy)
    {
      final WatersPopupActionManager manager = getPopupFactory().getMaster();
      final IDEAction action = manager.getPropertiesAction(proxy);
      manager.invokeMouseClickAction(action);
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    /**
     * Do nothing for identifiers and other expressions. Slightly faster than
     * the default.
     */
    @Override
    public Object visitExpressionProxy(final ExpressionProxy proxy)
      throws VisitorException
    {
      return null;
    }

    @Override
    public Object visitInstanceProxy(final InstanceProxy inst)
    {
      final WatersPopupActionManager manager = getPopupFactory().getMaster();
      final String name = inst.getModuleName();
      final ModuleProxy module = mModuleContext.getModule();
      final IDEAction action = manager.getGotoModuleAction(module, name);
      manager.invokeMouseClickAction(action);
      return null;
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy proxy)
      throws VisitorException
    {
      expandOrCollapseRoot();
      return null;
    }

    @Override
    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      final WatersPopupActionManager manager = getPopupFactory().getMaster();
      final IDEAction action = manager.getShowGraphAction(comp);
      manager.invokeMouseClickAction(action);
      return null;
    }
  }


  //#########################################################################
  //# Inner Class ModuleTreeTransferHandler
  private class ModuleTreeTransferHandler extends TransferHandler
  {

    @Override
    public int getSourceActions(final JComponent c)
    {
      return COPY_OR_MOVE;
    }

    @Override
    public Transferable createTransferable(final JComponent c)
    {
      final List<? extends Proxy> selection = getCurrentSelection();
      return WatersDataFlavor.createTransferable(selection);
    }

    @Override
    public void exportDone(final JComponent c, final Transferable t,
                           final int action)
    {
      if (isSourceOfDrag() && mDropList != null) {
        final int count = getSelectionCount();
        final List<InsertInfo> inserts = new ArrayList<InsertInfo>(count);
        final List<Proxy> proxies = new ArrayList<Proxy>();
        final int min = getMinSelectionRow();
        final int max = getMaxSelectionRow();
        if (action == MOVE) {
          int indexOfPrevious = -1;
          int minIndex = -1;
          boolean isContiguous = true;
          final List<InsertInfo> deletes = new ArrayList<InsertInfo>(count);
          int counter = 0;
          for (int initialRow = min; initialRow <= max; initialRow++) {
            if (isRowSelected(initialRow)) {
              final TreePath path = getPathForRow(initialRow);
              final ProxySubject proxy =
                (ProxySubject) path.getLastPathComponent();
              @SuppressWarnings("unchecked")
              final ListSubject<? extends ProxySubject> sourceList =
                (ListSubject<? extends ProxySubject>) proxy.getParent();
              final int index = sourceList.indexOf(proxy);
              if (mDropList == sourceList) {
                if (indexOfPrevious == -1) {
                  indexOfPrevious = index;
                  minIndex = index;
                } else if (index != indexOfPrevious + 1) {
                  isContiguous = false;
                } else {
                  indexOfPrevious++;
                }
              } else {
                isContiguous = false;
              }
              if (!hasAncestorInSelection(proxy, proxies)) {
                proxies.add(proxy);
                final InsertInfo delete =
                  new InsertInfo(proxy, new ListInsertPosition(sourceList,
                                                               index));
                deletes.add(delete);
                if (mDropList == sourceList && index < mDropIndex) {
                  counter++;
                }
              }
            }
          }
          mDropIndex -= counter;
          if (isContiguous) {
            if (mDropIndex == minIndex) {
              return;
            }
          }
          final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
          for (final InsertInfo delete : deletes) {
            final Proxy proxy = delete.getProxy();
            final ListInsertPosition delpos =
              (ListInsertPosition) delete.getInsertPosition();
            if (delpos.getList() == mDropList ||
                !eq.contains(mDropList, proxy)) {
              final ListInsertPosition inspos =
                new ListInsertPosition(mDropList, mDropIndex);
              final InsertInfo insert = new InsertInfo(proxy, inspos);
              inserts.add(insert);
              mDropIndex++;
            }
          }
          final RearrangeTreeCommand allMoves =
            new RearrangeTreeCommand(inserts, deletes, ModuleTree.this);
          executeCommand(allMoves);
        }
      }
      mDropList = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean canImport(final TransferSupport support)
    {
      if (support.getComponent() instanceof JTree) {
        final Transferable transferable = support.getTransferable();
        if (!isSourceOfDrag() && support.getDropAction() == MOVE) {
          support.setDropAction(COPY);
        }
        final JTree.DropLocation drop =
          (JTree.DropLocation) support.getDropLocation();
        final TreePath dropPath = drop.getPath();
        if (dropPath == null) {
          return false;
        }
        final ProxySubject dropTarget =
          (ProxySubject) dropPath.getLastPathComponent();
        boolean moveWithin = support.getDropAction() == MOVE;
        if (moveWithin) {
          final int min = getMinSelectionRow();
          final int max = getMaxSelectionRow();
          for (int row = min; row <= max; row++) {
            if (isRowSelected(row)) {
              final TreePath selPath = getPathForRow(row);
              final ProxySubject selItem =
                (ProxySubject) selPath.getLastPathComponent();
              if (SubjectTools.isAncestor(selItem, dropTarget)) {
                return false;
              }
              final Subject selParent = SubjectTools.getProxyParent(selItem);
              moveWithin &= (selParent == dropTarget);
            }
          }
        }
        final DataFlavor acceptingFlavor =
          mAcceptTransferableVisitor.getFlavorIfDropAllowed
            (dropTarget, transferable, !moveWithin);
        if (acceptingFlavor == null) {
          return false;
        }
        if (shouldForceCopy(acceptingFlavor, transferable)) {
          support.setDropAction(COPY);
        }
        if (dropTarget instanceof NodeSubject) {
          try {
            final List<Proxy> result =
              (List<Proxy>) transferable.getTransferData(WatersDataFlavor.IDENTIFIER);
            if (!mModuleContext.canDropOnNode(result)) {
              return false;
            }
          } catch (final UnsupportedFlavorException exception) {
            throw new WatersRuntimeException(exception);
          } catch (final IOException exception) {
            throw new WatersRuntimeException(exception);
          }
        }
        return true;
      }
      return false;
    }

    @Override
    public boolean importData(final TransferSupport support)
    {
      if (!canImport(support)) {
        return false;
      } else {
        final JTree.DropLocation dropLoc =
          (JTree.DropLocation) support.getDropLocation();
        mDropIndex = dropLoc.getChildIndex();
        final ProxySubject parentOfDropLoc =
          (ProxySubject) dropLoc.getPath().getLastPathComponent();
        mDropList = mModel.getChildren(parentOfDropLoc);
        if (mDropIndex < 0) {
          mDropIndex = mDropList.size();
        }
        if (support.getDropAction() == COPY) {
          final Transferable transferable = support.getTransferable();
          final DataFlavor flavor =
            mAcceptTransferableVisitor.getFlavorIfDropAllowed
              (parentOfDropLoc, transferable, false);
          try {
            final List<InsertInfo> inserts =
              getInsertInfo(transferable, flavor, mDropList, parentOfDropLoc,
                            mDropIndex);
            final InsertCommand allCopies =
              new InsertCommand(inserts, ModuleTree.this, mModuleContainer.getEditorPanel());
            executeCommand(allCopies);

          } catch (final IOException exception) {
            throw new WatersRuntimeException(exception);
          } catch (final UnsupportedFlavorException exception) {
            throw new WatersRuntimeException(exception);
          }
        }
      }
      return true;
    }

    private static final long serialVersionUID = 1L;
    private int mDropIndex;
    private ListSubject<? extends ProxySubject> mDropList;
  }


  //#########################################################################
  //# Inner Class GetListVisitor
  private class GetListVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private List<Proxy> getListOfAcceptedItems(final Proxy dropTarget,
                                               final Transferable transferable,
                                               final DataFlavor flavor)
    {
      mTransferable = transferable;
      mFlavor = flavor;
      try {
        @SuppressWarnings("unchecked")
        final List<Proxy> list = (List<Proxy>) dropTarget.acceptVisitor(this);
        return list;
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mTransferable = null;
      }
    }

    @SuppressWarnings("unchecked")
    private List<Proxy> modifyList(final ListSubject<? extends ProxySubject> parentsList)
    {
      List<Proxy> transferData;
      final List<Proxy> result = new ArrayList<Proxy>();
      try {
        final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
        transferData = (List<Proxy>) mTransferable.getTransferData(mFlavor);
        for (final Proxy transferredProxy : transferData) {
          if (!eq.contains(parentsList, transferredProxy)) {
            result.add(transferredProxy);
          }
        }
      } catch (final UnsupportedFlavorException exception) {
        exception.printStackTrace();
      } catch (final IOException exception) {
        exception.printStackTrace();
      }
      if (result.isEmpty()) {
        return null;
      }
      return result;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public List<Proxy> visitProxy(final Proxy alias)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public List<Proxy> visitEventAliasProxy(final EventAliasProxy alias)
    {
      final EventAliasSubject event = (EventAliasSubject) alias;
      final EventListExpressionSubject exp =
        (EventListExpressionSubject) event.getExpression();
      return modifyList(exp.getEventIdentifierListModifiable());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Proxy> visitForeachProxy(final ForeachProxy foreach)
    {
      try {
        final ForeachSubject subject = (ForeachSubject) foreach;
        if (SubjectTools.getAncestor(subject, EventAliasSubject.class,
                                     ParameterBindingSubject.class) != null) {
          return modifyList(subject.getBodyModifiable());
        } else {
          return (List<Proxy>) mTransferable
            .getTransferData(getSupportedDataFlavor());
        }
      } catch (final UnsupportedFlavorException exception) {
        exception.printStackTrace();
      } catch (final IOException exception) {
        exception.printStackTrace();
      }
      return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Proxy> visitModuleProxy(final ModuleProxy proxy)
    {
      try {
        return (List<Proxy>) mTransferable
          .getTransferData(getSupportedDataFlavor());
      } catch (final UnsupportedFlavorException exception) {
        exception.printStackTrace();
      } catch (final IOException exception) {
        exception.printStackTrace();
      }
      return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Proxy> visitInstanceProxy(final InstanceProxy inst)
    {
      try {
        return (List<Proxy>) mTransferable
          .getTransferData(WatersDataFlavor.PARAMETER_BINDING);
      } catch (final UnsupportedFlavorException exception) {
        exception.printStackTrace();
      } catch (final IOException exception) {
        exception.printStackTrace();
      }
      return null;
    }

    @Override
    public List<Proxy> visitParameterBindingProxy(final ParameterBindingProxy binding)
    {
      final ParameterBindingSubject event = (ParameterBindingSubject) binding;
      final EventListExpressionSubject exp =
        (EventListExpressionSubject) event.getExpression();
      return modifyList(exp.getEventIdentifierListModifiable());
    }

    @Override
    public List<Proxy> visitSimpleNodeProxy(final SimpleNodeProxy node){
      final SimpleNodeSubject sub = (SimpleNodeSubject)node;
      final PlainEventListSubject list = sub.getPropositions();
      return modifyList(list.getEventIdentifierListModifiable());
    }


    private Transferable mTransferable;
    private DataFlavor mFlavor;
  }


  //#########################################################################
  //# Inner Class AcceptedDataFlavorVisitor
  private class AcceptTransferableVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private DataFlavor getFlavorIfDropAllowed(final Proxy dropTarget,
                                              final Transferable transferable,
                                              final boolean checkDuplicates)
    {
      mTransferable = transferable;
      mCheckingForDuplicateNames = checkDuplicates;
      try {
        final DataFlavor flavor = (DataFlavor) dropTarget.acceptVisitor(this);
        if (transferable.isDataFlavorSupported(flavor)) {
          return flavor;
        }
        return null;
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mTransferable = null;
      }
    }

    private boolean isInList(final List<Proxy> listOfEvents)
      throws VisitorException
    {
      if (mTransferable.isDataFlavorSupported(WatersDataFlavor.IDENTIFIER)) {
        try {
          final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
          @SuppressWarnings("unchecked")
          final List<Proxy> transferData = (List<Proxy>)
            mTransferable.getTransferData(WatersDataFlavor.IDENTIFIER);
          for (final Proxy transferredProxy : transferData) {
            if (!eq.contains(listOfEvents, transferredProxy)) {
              return false;
            }
          }
        } catch (final UnsupportedFlavorException | IOException exception) {
          throw wrap(exception);
        }
      }
      return true;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public DataFlavor visitProxy(final Proxy alias)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public DataFlavor visitEventAliasProxy(final EventAliasProxy alias)
      throws VisitorException
    {
      final EventAliasSubject event = (EventAliasSubject) alias;
      final ExpressionProxy exp = event.getExpression();
      if (exp instanceof EventListExpressionProxy) {
        if (!isInList(((EventListExpressionProxy) exp).getEventIdentifierList())) {
          return WatersDataFlavor.IDENTIFIER;
        }
      }
      return null;
    }

    @Override
    public DataFlavor visitForeachProxy(final ForeachProxy foreach)
      throws VisitorException
    {
      final Subject subject = (Subject) foreach;
      if (SubjectTools.getAncestor(subject, EventAliasSubject.class,
                                   ParameterBindingSubject.class) != null) {
        if (!isInList(foreach.getBody())) {
          return WatersDataFlavor.IDENTIFIER;
        }
        return null;
      } else {
        return getSupportedDataFlavor();
      }
    }

    @Override
    public DataFlavor visitModuleProxy(final ModuleProxy proxy)
    {
      return getSupportedDataFlavor();
    }

    @Override
    public DataFlavor visitInstanceProxy(final InstanceProxy inst)
      throws VisitorException
    {
      if (!mCheckingForDuplicateNames) {
        return WatersDataFlavor.PARAMETER_BINDING;
      } else if (mTransferable.isDataFlavorSupported
                  (WatersDataFlavor.PARAMETER_BINDING)) {
        try {
          final InstanceSubject instSubject = (InstanceSubject) inst;
          final IndexedListSubject<ParameterBindingSubject> bindings =
            instSubject.getBindingListModifiable();
          @SuppressWarnings("unchecked")
          final List<ParameterBindingProxy> data = (List<ParameterBindingProxy>)
            mTransferable.getTransferData(WatersDataFlavor.PARAMETER_BINDING);
          for (final ParameterBindingProxy binding : data) {
            final String name = binding.getName();
            if (bindings.containsName(name)) {
              return null;
            }
          }
          return WatersDataFlavor.PARAMETER_BINDING;
        } catch (final UnsupportedFlavorException exception) {
          throw new VisitorException(exception);
        } catch (final IOException exception) {
          throw new VisitorException(exception);
        }
      } else {
        return null;
      }
    }

    @Override
    public DataFlavor visitParameterBindingProxy
      (final ParameterBindingProxy binding)
      throws VisitorException
    {
      final ExpressionProxy expr = binding.getExpression();
      if (expr instanceof EventListExpressionProxy) {
        final EventListExpressionProxy elist = (EventListExpressionProxy) expr;
        if (!isInList(elist.getEventIdentifierList())) {
          return WatersDataFlavor.IDENTIFIER;
        }
      }
      return null;
    }

    @Override
    public DataFlavor visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      return WatersDataFlavor.IDENTIFIER;
    }

    //#######################################################################
    //# Data Members
    private Transferable mTransferable;
    private boolean mCheckingForDuplicateNames;
  }


  //#########################################################################
  //# Inner Class PrinterVisitor
  private class PrintVisitor extends HTMLPrinter
  {

    //#######################################################################
    //# Constructors
    public PrintVisitor(final Writer writer, final boolean expanded)
    {
      super(writer, mModuleContext);
      mExpanded = expanded;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitEventAliasProxy(final EventAliasProxy alias)
      throws VisitorException
    {
      final ExpressionProxy exp = alias.getExpression();
      if (exp instanceof EventListExpressionProxy) {
        print(alias.getName());
        print(" =");
        if (!mExpanded) {
          print(" ...");
        }
      } else {
        super.visitEventAliasProxy(alias);
      }
      return null;
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      printHTML("<B>");
      print(getRootName());
      printHTML("</B>");
      return null;
    }

    @Override
    public Object visitParameterBindingProxy(final ParameterBindingProxy alias)
      throws VisitorException
    {
      final ExpressionProxy exp = alias.getExpression();
      if (exp instanceof EventListExpressionProxy) {
        print(alias.getName());
        print(" =");
        if (!mExpanded) {
          print(" ...");
        }
      } else {
        super.visitParameterBindingProxy(alias);
      }
      return null;
    }

    //#######################################################################
    //# Data Members
    private final boolean mExpanded;

  }


  //#######################################################################
  //# Inner Class ModuleTreeRenderer
  private class ModuleTreeRenderer extends DefaultTreeCellRenderer
  {

    //#######################################################################
    //# Interface javax.swing.TreeCellRenderer
    @Override
    public Component getTreeCellRendererComponent(final JTree tree,
                                                  final Object value,
                                                  final boolean selected,
                                                  final boolean expanded,
                                                  final boolean leaf,
                                                  final int row,
                                                  final boolean hasFocus)
    {
      if (selected) {
        if (mIsPermanentFocusOwner) {
          setBackgroundSelectionColor(EditorColor.BACKGROUND_FOCUSSED);
          setBorderSelectionColor(EditorColor.BACKGROUND_FOCUSSED);
        } else {
          setBackgroundSelectionColor(EditorColor.BACKGROUND_NOTFOCUSSED);
          setBorderSelectionColor(EditorColor.BACKGROUND_NOTFOCUSSED);
        }
      }
      super.getTreeCellRendererComponent(tree, value, selected, expanded,
                                         leaf, row, hasFocus);
      final Proxy proxy = (Proxy) value;
      final Icon icon = mModuleContext.getIcon(proxy);
      setIcon(icon);
      final String text = getPrintString(proxy, expanded);
      setText(text);

      return this;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;
  }


  //#########################################################################
  //# Data Members
  private final ModuleTreeModel mModel;
  private final ModuleContainer mModuleContainer;
  private final UndoInterface mUndoInterface;
  private List<Observer> mObservers;
  private final ModuleContext mModuleContext;
  private boolean mIsPermanentFocusOwner;
  private final DoubleClickVisitor mDoubleClickVisitor;
  private final AcceptTransferableVisitor mAcceptTransferableVisitor;
  private final GetListVisitor mListVisitor;
  private final ProxySubject mRoot;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
