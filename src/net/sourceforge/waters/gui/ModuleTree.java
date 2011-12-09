//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleTree
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

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
import java.util.ArrayList;
import java.util.HashSet;
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
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.RearrangeTreeCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.transfer.ComponentTransferable;
import net.sourceforge.waters.gui.transfer.ConstantAliasTransferable;
import net.sourceforge.waters.gui.transfer.EventAliasTransferable;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.IdentifierTransferable;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.ParameterBindingTransferable;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.TypelessForeachTransferable;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EventAliasSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.IdentifiedSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;


/**
 * The Aliases Tree used to view the constant aliases and the event aliases of
 * a module.
 *
 * @author Carly Hona, Robi Malik
 */
public abstract class ModuleTree extends JTree implements SelectionOwner,
  Autoscroll, TreeSelectionListener, FocusListener
{

  public ModuleTree(final ModuleWindowInterface root,
                    final WatersPopupActionManager manager)
  {
    mRoot = root;
    mModuleContext = mRoot.getModuleContext();
    mPrinter = new PrintVisitor();
    mIsPermanentFocusOwner = true;
    final ModuleSubject module = mRoot.getModuleSubject();
    mModel = new ModuleTreeModel(module, getRootList());
    setModel(mModel);
    final MouseListener handler = new EditorAliasMouseListener();
    addMouseListener(handler);
    addTreeSelectionListener(this);
    addFocusListener(this);

    mAcceptTransferableVisitor = new AcceptTransferableVisitor();
    mExportedDataFlavorVisitor = new ExportedDataFlavorVisitor();
    mDoubleClickVisitor = new DoubleClickVisitor();

    if (getSupportedDataFlavor() == WatersDataFlavor.MODULE_COMPONENT_LIST) {
      setRootVisible(false);
    } else {
      setRootVisible(true);
    }
    setShowsRootHandles(true);
    setAutoscrolls(true);
    // Don't expand/collapse on double-click, never collapse the root.
    setToggleClickCount(0);
    manager.installCutCopyPasteActions(this);

    setTransferHandler(new AliasesPanelTransferHandler());
    setDragEnabled(true);
    setDropMode(DropMode.INSERT);

    setCellRenderer(new MyTreeRenderer());
    expandAll(getRootList());
  }

  //#########################################################################
  //# Simple Access
  public ModuleTreeModel getAliasTreeModel()
  {
    return (ModuleTreeModel) getModel();
  }

  public ModuleWindowInterface getRoot()
  {
    return mRoot;
  }

  public FocusTracker getFocusTracker()
  {
    return mRoot.getRootWindow().getFocusTracker();
  }

  //#########################################################################
  //# Abstract Methods
  abstract ListSubject<? extends ProxySubject> getRootList();

  abstract String getRootName();

  abstract DataFlavor getSupportedDataFlavor();

  abstract PopupFactory getPopupFactory();

  //#######################################################################
  //# Interface java.awt.dnd.Autoscroll
  public Insets getAutoscrollInsets()
  {
    final Rectangle all = getBounds();
    final Rectangle visible = getParent().getBounds();
    return new Insets(visible.y - all.y + 20, 0, all.height - visible.height
                                                 - visible.y + all.y + 20, 0);
  }

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
  public void focusGained(final FocusEvent event)
  {
    if (!event.isTemporary()) {
      mIsPermanentFocusOwner = true;
      repaint();
    }
  }

  public void focusLost(final FocusEvent event)
  {
    if (!event.isTemporary()) {
      mIsPermanentFocusOwner = false;
      repaint();
    }
  }

  //#######################################################################
  //# Interface net.sourceforge.waters.gui.observer.Subject
  public void attach(final Observer observer)
  {
    if (mObservers == null) {
      mObservers = new LinkedList<Observer>();
    }
    mObservers.add(observer);
  }

  public void detach(final Observer observer)
  {
    mObservers.remove(observer);
    if (mObservers.isEmpty()) {
      mObservers = null;
    }
  }

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
  public UndoInterface getUndoInterface(final Action action)
  {
    return mRoot.getUndoInterface();
  }

  public boolean hasNonEmptySelection()
  {
    return !isSelectionEmpty();
  }

  public boolean canSelectMore()
  {
    if (rootVisible) {
      return getSelectionCount() < getRowCount() - 1;
    }
    return getSelectionCount() < getRowCount();
  }

  public boolean isSelected(final Proxy proxy)
  {
    final ModuleTreeModel model = getAliasTreeModel();
    final ProxySubject subject = (ProxySubject) proxy;
    final TreePath path = model.createPath(subject);
    return isPathSelected(path);
  }

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

  public Proxy getSelectionAnchor()
  {
    final TreePath path = getAnchorSelectionPath();
    if (path == null) {
      return mModuleContext.getModule();
    } else {
      return (Proxy) path.getLastPathComponent();
    }
  }

  public Proxy getSelectableAncestor(final Proxy item)
  {
    final ModuleTreeModel model = getAliasTreeModel();
    if (item == model.getRoot()) {
      return null;
    } else {
      return model.getVisibleAncestorInTree(item);
    }
  }

  public void clearSelection(final boolean propagate)
  {
    clearSelection();
  }

  public void replaceSelection(final List<? extends Proxy> items)
  {
    clearSelection();
    addToSelection(items);
  }

  public void addToSelection(final List<? extends Proxy> items)
  {
    final ModuleTreeModel model = getAliasTreeModel();
    for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      final TreePath path = model.createPath(subject);
      addSelectionPath(path);
    }
  }

  public void removeFromSelection(final List<? extends Proxy> items)
  {
    final ModuleTreeModel model = getAliasTreeModel();
    for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      final TreePath path = model.createPath(subject);
      removeSelectionPath(path);
    }
  }

  public ListInsertPosition getInsertPosition(final Proxy proxy)
  {
    final Proxy anchor = getSelectionAnchor();
    ListSubject<? extends ProxySubject> list = mModel.getChildren(anchor);
    if (list == null) {
      list = getRootList();
    }
    return new ListInsertPosition(list, list.size());
  }

  public void insertCreatedItem(final Proxy proxy, final Object insobj)
  {
    final ProxySubject subject = (ProxySubject) proxy;
    final ListInsertPosition inspos = (ListInsertPosition) insobj;
    @SuppressWarnings("unchecked")
    final List<ProxySubject> list = (List<ProxySubject>) inspos.getList();
    final int pos = inspos.getPosition();
    list.add(pos, subject);
  }

  public boolean canCopy(final List<? extends Proxy> items)
  {
    final DataFlavor common = getDataFlavor(items);
    return common != null;
  }

  public Transferable createTransferable(final List<? extends Proxy> items)
  {
    final List<Proxy> reducedList = new ArrayList<Proxy>();
    for (final Proxy proxy : items) {
      boolean hasAncestor = false;
      final ProxySubject subject = (ProxySubject) proxy;
      for (final Proxy ancestor : reducedList) {
        final ProxySubject ancestorSubject = (ProxySubject) ancestor;
        if (SubjectTools.isAncestor(ancestorSubject, subject)) {
          hasAncestor = true;
        }
      }
      if (!hasAncestor) {
        reducedList.add(proxy);
      }
    }
    return getTransferable(reducedList);
  }

  public boolean canPaste(final Transferable transferable)
  {
    Proxy anchor = getSelectionAnchor();
    if (anchor == null) {
      anchor = mModuleContext.getModule();
    }
    DataFlavor flavor =
      mAcceptTransferableVisitor.getFlavorIfDropAllowed(anchor, transferable);
    if (flavor == null) {
      final Proxy parent = mModel.getProperAncestorInTree((Subject) anchor);
      if (parent == null) {
        return false;
      }
      flavor =
        mAcceptTransferableVisitor.getFlavorIfDropAllowed(parent,
                                                          transferable);
    }
    return flavor != null;
  }

  public List<InsertInfo> getInsertInfo(final Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    Proxy anchor = getSelectionAnchor();
    if (anchor == null) {
      anchor = mModuleContext.getModule();
    }
    DataFlavor flavor =
      mAcceptTransferableVisitor.getFlavorIfDropAllowed(anchor, transferable);
    ListSubject<? extends ProxySubject> listInModule =
      mModel.getChildren(anchor);
    Proxy parent = null;
    int pos;
    if (flavor != null) {
      pos = listInModule.size();
    } else {
      parent = mModel.getProperAncestorInTree((Subject) anchor);
      flavor =
        mAcceptTransferableVisitor.getFlavorIfDropAllowed(parent,
                                                          transferable);
      listInModule = mModel.getChildren(parent);
      pos = mModel.getIndexOfChild(parent, anchor) + 1;
      if (flavor == null) {
        return null;
      }
    }
    List<InsertInfo> result = new ArrayList<InsertInfo>(pos);
    result = getInsertInfo(transferable, flavor, listInModule, pos);

    return result;
  }

  public boolean canDelete(final List<? extends Proxy> items)
  {
    return !items.isEmpty();
  }

  @SuppressWarnings("unchecked")
  public List<InsertInfo> getDeletionVictims(final List<? extends Proxy> items)
  {
    final ModuleTreeModel model = getAliasTreeModel();
    final Set<Proxy> set = new HashSet<Proxy>(items);
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

  public void insertItems(final List<InsertInfo> inserts)
  {
    for (final InsertInfo insert : inserts) {
      final ProxySubject victim = (ProxySubject) insert.getProxy();
      final ListInsertPosition inspos =
        (ListInsertPosition) insert.getInsertPosition();
      @SuppressWarnings("unchecked")
      final List<ProxySubject> list = (List<ProxySubject>) inspos.getList();
      final int pos = inspos.getPosition();
      list.add(pos, victim);
      expand(victim);
    }
  }

  public void deleteItems(final List<InsertInfo> deletes)
  {
    final int size = deletes.size();
    final ListIterator<InsertInfo> iter = deletes.listIterator(size);
    while (iter.hasPrevious()) {
      final InsertInfo delete = (InsertInfo) iter.previous();
      final ListInsertPosition inspos =
        (ListInsertPosition) delete.getInsertPosition();
      final ListSubject<? extends ProxySubject> list = inspos.getList();
      final int index = inspos.getPosition();
      list.remove(index);
    }
  }

  public void scrollToVisible(final List<? extends Proxy> list)
  {
    if (list.isEmpty()) {
      return;
    }
    final ModuleTreeModel model = getAliasTreeModel();
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

  public void activate()
  {
    if (!isFocusOwner()) {
      mRoot.showPanel(this);
      requestFocusInWindow();
    }
  }

  public void close()
  {
    final ModuleTreeModel model = getAliasTreeModel();
    model.close();
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
  //# Interface javax.swing.event.TreeSelectionListener
  public void valueChanged(final TreeSelectionEvent event)
  {
    // Why can't the new selection be read immediately ???
    SwingUtilities.invokeLater(new Runnable() {
      public void run()
      {
        fireSelectionChanged();
      }
    });
  }

  //#########################################################################
  //#Auxiliary Methods
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
    if (getSupportedDataFlavor() == WatersDataFlavor.CONSTANT_ALIAS_LIST) {
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

  private Transferable getTransferable(final List<? extends Proxy> items)
  {
    final DataFlavor dataFlavor = getDataFlavor(items);
    if (dataFlavor == WatersDataFlavor.CONSTANT_ALIAS_LIST) {
      return new ConstantAliasTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.MODULE_COMPONENT_LIST) {
      return new ComponentTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.IDENTIFIER_LIST) {
      return new IdentifierTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.EVENT_ALIAS_LIST) {
      return new EventAliasTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.TYPELESS_FOREACH) {
      return new TypelessForeachTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.PARAMETER_BINDING_LIST) {
      return new ParameterBindingTransferable(items);
    } else
      return null;
  }

  private DataFlavor getDataFlavor(final List<? extends Proxy> items)
  {
    DataFlavor common = null;
    for (final Proxy proxy : items) {
      final DataFlavor flavor =
        mExportedDataFlavorVisitor.getDataFlavor(proxy);
      if (common == null) {
        common = flavor;
      } else if (common != flavor) {
        return null;
      }
    }
    return common;
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

  public List<InsertInfo> getInsertInfo(final Transferable transferable,
                                        final DataFlavor flavor,
                                        final ListSubject<? extends ProxySubject> list,
                                        int index) throws IOException,
    UnsupportedFlavorException
  {
    final List<InsertInfo> result = new ArrayList<InsertInfo>(index);
    @SuppressWarnings("unchecked")
    final List<Proxy> transferData =
      (List<Proxy>) transferable.getTransferData(flavor);
    final ModuleProxyCloner cloner =
      ModuleSubjectFactory.getCloningInstance();
    Set<String> names;
    names = new HashSet<String>(transferData.size());
    for (final Proxy proxy : transferData) {
      final Proxy cloned = cloner.getClone(proxy);
      if (cloned instanceof IdentifiedSubject && list == getRootList()) {
        final IdentifiedSubject sub = (IdentifiedSubject) cloned;
        final IdentifierSubject newId =
          mModuleContext.getPastedName(sub, names);
        sub.setIdentifier(newId);
      }
      final ListInsertPosition inspos = new ListInsertPosition(list, index++);
      final InsertInfo info = new InsertInfo(cloned, inspos);
      result.add(info);
    }
    return result;
  }

  @Override
  public void setSelectionInterval(int index0, final int index1)
  {
    //so root not selected with ctrl-a select all
    if (rootVisible) {
      if (index0 == 0) {
        index0 = 1;
      }
    }
    final TreePath[] paths = getPathBetweenRows(index0, index1);
    this.getSelectionModel().setSelectionPaths(paths);
  }


  //#########################################################################
  //# Inner Class EditorAliasMouseListener
  /**
   * A simple mouse listener to trigger opening an editor dialog or
   * expand/collapse tree by double-click, or a popup menu by right-click.
   */
  private class EditorAliasMouseListener extends MouseAdapter
  {
    //#######################################################################
    //# Interface java.awt.MouseListener
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1) {
        final Proxy proxy = (Proxy) getClickedItem(event);
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
  private class DoubleClickVisitor extends AbstractModuleProxyVisitor
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
  //# Inner Class AliasesPanelTransferHandler
  private class AliasesPanelTransferHandler extends TransferHandler
  {

    @Override
    public int getSourceActions(final JComponent c)
    {
      return COPY_OR_MOVE;
    }

    @Override
    public Transferable createTransferable(final JComponent c)
    {
      getFocusTracker().setSourceOfDragOperation(ModuleTree.this);
      return ModuleTree.this.createTransferable(getCurrentSelection());
    }

    @Override
    public void exportDone(final JComponent c, final Transferable t,
                           final int action)
    {
      if (getFocusTracker().getSourceOfDragOperation() == ModuleTree.this) {
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
          for (final InsertInfo delete : deletes) {
            final Proxy proxy = delete.getProxy();
            final InsertInfo insert =
              new InsertInfo(proxy, new ListInsertPosition(mDropList,
                                                           mDropIndex));
            inserts.add(insert);
            mDropIndex++;
          }
          final RearrangeTreeCommand allMoves =
            new RearrangeTreeCommand(inserts, deletes, ModuleTree.this);
          mRoot.getUndoInterface().executeCommand(allMoves);
        }
      }
    }

    @Override
    public boolean canImport(final TransferSupport support)
    {
      if (support.getComponent() instanceof JTree) {
        final Transferable transferable = support.getTransferable();
        if (transferable
          .isDataFlavorSupported(WatersDataFlavor.PARAMETER_BINDING_LIST)) {
          return false;
        }
        final JTree.DropLocation drop =
          (JTree.DropLocation) support.getDropLocation();
        final TreePath path = drop.getPath();
        if (path == null) {
          return false;
        }
        final Proxy parent = (Proxy) path.getLastPathComponent();
        if (support.getDropAction() == MOVE) {
          final Subject parentSub = (Subject) parent;
          final int min = getMinSelectionRow();
          final int max = getMaxSelectionRow();
          for (int initialRow = min; initialRow <= max; initialRow++) {
            if (isRowSelected(initialRow)) {
              final TreePath rowPath = getPathForRow(initialRow);
              final ProxySubject ancestor =
                (ProxySubject) rowPath.getLastPathComponent();
              if (SubjectTools.isAncestor(ancestor, parentSub)) {
                return false;
              }
            }
          }
        }
        final DataFlavor acceptingFlavor =
          mAcceptTransferableVisitor.getFlavorIfDropAllowed(parent,
                                                            transferable);
        if (acceptingFlavor == WatersDataFlavor.IDENTIFIER_LIST
            && transferable
              .isDataFlavorSupported(WatersDataFlavor.EVENT_ALIAS_LIST)) {
          support.setDropAction(COPY);
        }
        if (acceptingFlavor != null) {
          return true;
        }
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
            mAcceptTransferableVisitor
              .getFlavorIfDropAllowed(parentOfDropLoc, transferable);
          try {
            final List<InsertInfo> inserts =
              getInsertInfo(transferable, flavor, mDropList, mDropIndex);
            final InsertCommand allCopies =
              new InsertCommand(inserts, ModuleTree.this);
            mRoot.getUndoInterface().executeCommand(allCopies);
          } catch (final IOException exception) {
            exception.printStackTrace();
          } catch (final UnsupportedFlavorException exception) {
            exception.printStackTrace();
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
  //# Inner Class ExportedDataFlavorVisitor
  private class ExportedDataFlavorVisitor extends AbstractModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private DataFlavor getDataFlavor(final Proxy proxy)
    {
      try {
        return (DataFlavor) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public DataFlavor visitComponentProxy(final ComponentProxy comp)
    {
      return WatersDataFlavor.MODULE_COMPONENT_LIST;
    }

    @Override
    public DataFlavor visitConstantAliasProxy(final ConstantAliasProxy alias)
    {
      return WatersDataFlavor.CONSTANT_ALIAS_LIST;
    }

    @Override
    public DataFlavor visitEventAliasProxy(final EventAliasProxy alias)
    {
      return WatersDataFlavor.EVENT_ALIAS_LIST;
    }

    @Override
    public Object visitExpressionProxy(final ExpressionProxy proxy)
      throws VisitorException
    {
      return WatersDataFlavor.IDENTIFIER_LIST;
    }

    @Override
    public Object visitForeachProxy(final ForeachProxy foreach)
    {
      if (foreach.getBody().isEmpty()) {
        return WatersDataFlavor.TYPELESS_FOREACH;
      } else {
        final List<Proxy> list = foreach.getBody();
        for (int i = 0; i < list.size(); i++) {
          final DataFlavor flavor = getDataFlavor(list.get(i));
          if (flavor != WatersDataFlavor.TYPELESS_FOREACH) {
            return flavor;
          }
        }
      }
      return WatersDataFlavor.TYPELESS_FOREACH;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy proxy)
      throws VisitorException
    {
      return WatersDataFlavor.IDENTIFIER_LIST;
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy proxy)
    {
      return getSupportedDataFlavor();
    }

    @Override
    public DataFlavor visitParameterBindingProxy(final ParameterBindingProxy binding)
    {
      return WatersDataFlavor.PARAMETER_BINDING_LIST;
    }
  }


  //#########################################################################
  //# Inner Class AcceptedDataFlavorVisitor
  private class AcceptTransferableVisitor extends AbstractModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private DataFlavor getFlavorIfDropAllowed(final Proxy dropTarget,
                                              final Transferable transferable)
    {
      mTransferable = transferable;
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

    private boolean isInExpressionList(final ExpressionProxy expression){

      if(expression instanceof EventListExpressionProxy &&
        mTransferable.isDataFlavorSupported(WatersDataFlavor.IDENTIFIER_LIST)){
        final EventListExpressionSubject subject =
          (EventListExpressionSubject) expression;
        final List<Proxy> listOfEvents = subject.getEventList();
        try {
          @SuppressWarnings("unchecked")
          final List<Proxy> transferData =
            (List<Proxy>) mTransferable
              .getTransferData(WatersDataFlavor.IDENTIFIER_LIST);
          for (final Proxy transferredProxy : transferData) {
            if (!ModuleEqualityVisitor.getInstance(false)
              .contains(listOfEvents, transferredProxy)) {
              return false;
            }
          }

        } catch (final UnsupportedFlavorException exception) {
          exception.printStackTrace();
        } catch (final IOException exception) {
          exception.printStackTrace();
        }
      }
      return true;
    }

/*    @SuppressWarnings("unchecked")
    private List<Proxy> getListToBeTransferred(final Transferable transferable,
                                               final DataFlavor flavor,
                                               final Proxy parent)
    {
      List<Proxy> transferData;
      final List<Proxy> result = new ArrayList<Proxy>();
      try {
        transferData = (List<Proxy>) transferable.getTransferData(flavor);
        final List<Proxy> list = (List<Proxy>) mModel.getChildren(parent);
        if (flavor == WatersDataFlavor.IDENTIFIER_LIST) {
          for (final Proxy transferredProxy : transferData) {
            if (!ModuleEqualityVisitor.getInstance(false)
              .contains(list, transferredProxy)) {
              result.add(transferredProxy);
            }
          }
        } else {
          return transferData;
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
    }*/

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
    {
      final EventAliasSubject event = (EventAliasSubject) alias;
      final ExpressionProxy exp = event.getExpression();
      if (!isInExpressionList(exp)) {
        return WatersDataFlavor.IDENTIFIER_LIST;
      }
      return null;
    }

    @Override
    public DataFlavor visitForeachProxy(final ForeachProxy foreach)
    {
      final Subject subject = (Subject) foreach;
      if (SubjectTools.getAncestor(subject, EventAliasSubject.class) != null) {
        return WatersDataFlavor.IDENTIFIER_LIST;
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
    {
      return WatersDataFlavor.PARAMETER_BINDING_LIST;
    }

    public DataFlavor visitParameterBindingProxy(final ParameterBindingProxy binding)
    {
      final ParameterBindingSubject event = (ParameterBindingSubject) binding;
      final ExpressionProxy exp = event.getExpression();
      if (!isInExpressionList(exp) && !(exp instanceof SimpleExpressionProxy)) {
        return WatersDataFlavor.IDENTIFIER_LIST;
      }
      return null;
    }

    private Transferable mTransferable;
  }


  //#########################################################################
  //# Inner Class PrinterVisitor
  private class PrintVisitor extends HTMLPrinter
  {

    //#######################################################################
    //# Invocation
    public String toString(final Proxy proxy, final boolean expanded)
    {
      mExpanded = expanded;
      return toString(proxy);
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
      print("<B>");
      print(getRootName());
      print("</B>");
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
    private boolean mExpanded;
  }


  private class MyTreeRenderer extends DefaultTreeCellRenderer
  {
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
      final String text = mPrinter.toString(proxy, expanded);
      setText(text);
      final String tooltip = mModuleContext.getToolTipText(proxy);
      setToolTipText(tooltip);
      return this;
    }

    private static final long serialVersionUID = 1L;
  }

  //#########################################################################
  //# Data Members
  private final ModuleTreeModel mModel;
  private final ModuleWindowInterface mRoot;
  private List<Observer> mObservers;
  private final ExportedDataFlavorVisitor mExportedDataFlavorVisitor;
  private final ModuleContext mModuleContext;
  private final PrintVisitor mPrinter;
  private boolean mIsPermanentFocusOwner;
  private final DoubleClickVisitor mDoubleClickVisitor;
  private final AcceptTransferableVisitor mAcceptTransferableVisitor;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
