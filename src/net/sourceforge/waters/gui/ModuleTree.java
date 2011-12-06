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
import javax.swing.JDialog;
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
import net.sourceforge.waters.gui.transfer.IdentifierTransferable;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
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
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.ConstantAliasSubject;
import net.sourceforge.waters.subject.module.EventAliasSubject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.VariableComponentSubject;


/**
 * The Aliases Tree used to view the constant aliases and the event aliases
 * of a module.
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

    mAcceptedDataFlavorVisitor = new AcceptedDataFlavorVisitor();
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
    if(rootVisible){
      return getSelectionCount() < getRowCount()-1;
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
    final Proxy anchor = getSelectionAnchor();
    final DataFlavor flavor =
      mAcceptedDataFlavorVisitor.getDataFlavor(anchor);
    return transferable.isDataFlavorSupported(flavor);
  }

  public List<InsertInfo> getInsertInfo(final Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    final Proxy anchor = getSelectionAnchor();
    final DataFlavor flavor =
      mAcceptedDataFlavorVisitor.getDataFlavor(anchor);

    final ListSubject<? extends ProxySubject> listInModule = mModel.getChildren(anchor);
    if(listInModule == null){
      return null;
    }

    final int pos = listInModule.size();
    List<InsertInfo> result = new ArrayList<InsertInfo>(pos);
    if (transferable.isDataFlavorSupported(flavor)) {
      result = getInsertInfo(transferable, flavor, listInModule, pos);
    }
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
  private void expandOrCollapseRoot(final List<? extends ProxySubject> list)
  {
    final TreePath path = getPathForRow(0);
    if (isExpanded(path)) {
      collapsePath(path);
    } else {
      expandAll(list);
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
    } else if(dataFlavor == WatersDataFlavor.MODULE_COMPONENT_LIST){
      return new ComponentTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.IDENTIFIER_LIST) {
      return new IdentifierTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.EVENT_ALIAS_LIST) {
      return new EventAliasTransferable(items);
    }else if(dataFlavor == WatersDataFlavor.TYPELESS_FOREACH){
      return new TypelessForeachTransferable(items);
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

  public List<InsertInfo> getInsertInfo(final Transferable transferable, final DataFlavor flavor,
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
      for (final Proxy proxy : transferData) {
        final Proxy cloned = cloner.getClone(proxy);
        final ListInsertPosition inspos =
          new ListInsertPosition(list, index++);
        final InsertInfo info = new InsertInfo(cloned, inspos);
        result.add(info);
      }
    return result;
  }

  @Override
  public void setSelectionInterval(int index0, final int index1){
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
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1) {
        final Proxy proxy = (Proxy) getClickedItem(event);
        if (proxy == null || proxy instanceof ModuleProxy) {
          clearSelection();
        }
        if (event.getClickCount() == 2) {
          if (proxy instanceof ModuleProxy) {
            expandOrCollapseRoot(getRootList());
          } else if (proxy != null) {
            if(!rootVisible){
              mDoubleClickVisitor.invokeDoubleClickAction(proxy, event);
            } else {
              @SuppressWarnings("unused")
              final JDialog dialog =
                mDoubleClickVisitor.getEditorDialog(proxy);
            }
          }
        }
      }
    }

    public void mousePressed(final MouseEvent event)
    {
      requestFocusInWindow();
      maybeShowPopup(event);
    }

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
    private JDialog getEditorDialog(final Proxy proxy)
    {
      try {
        return (JDialog) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    private void invokeDoubleClickAction(final Proxy proxy,
                                         final MouseEvent event)
    {
      if (proxy != null) {
        final IDEAction action = getDoubleClickAction(proxy);
        if (action != null) {
          final WatersPopupActionManager manager = getPopupFactory().getMaster();
          manager.invokeMouseClickAction(action, event);
        }
      }
    }

    private IDEAction getDoubleClickAction(final Proxy proxy)
    {
      try {
        return (IDEAction) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

  //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    public IDEAction visitProxy(final Proxy proxy)
    {
      final WatersPopupActionManager manager = getPopupFactory().getMaster();
      final IDEAction action = manager.getPropertiesAction(proxy);
      return action.isEnabled() ? action : null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    @Override
    public Object visitConstantAliasProxy(final ConstantAliasProxy alias)
    {
      return new ConstantAliasEditorDialog(mRoot,
                                           (ConstantAliasSubject) alias);
    }

    @Override
    public Object visitEventAliasProxy(final EventAliasProxy alias)
    {
      return null;
    }

    @Override
    public Object visitExpressionProxy(final ExpressionProxy proxy)
      throws VisitorException
    {
      return null;
    }

    @Override
    public Object visitForeachProxy(final ForeachProxy foreach)
    {
      new ForeachEditorDialog(mRoot, ModuleTree.this, (ForeachSubject) foreach);
      return null;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy proxy)
      throws VisitorException
    {
      return null;
    }

    public Object visitInstanceProxy(final InstanceProxy inst)
    {
      final WatersPopupActionManager manager = getPopupFactory().getMaster();
      final String name = inst.getModuleName();
      final ModuleProxy module = mModuleContext.getModule();
      return manager.getGotoModuleAction(module, name);
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy proxy)
      throws VisitorException
    {
      final WatersPopupActionManager manager = getPopupFactory().getMaster();
      return manager.getShowModuleCommentAction();
    }

    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      final WatersPopupActionManager manager = getPopupFactory().getMaster();
      return manager.getShowGraphAction(comp);
    }

    public Object visitVariableComponentProxy(final VariableComponentProxy var)
    {
      new VariableEditorDialog(mRoot, (VariableComponentSubject)var);
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
      mImportedToThisPanel = false;
      return ModuleTree.this.createTransferable(getCurrentSelection());
    }

    @Override
    public void exportDone(final JComponent c, final Transferable t,
                           final int action)
    {
      if (mImportedToThisPanel) {
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
        final DataFlavor flavor =
          mAcceptedDataFlavorVisitor.getDataFlavor(parent);
        if (support.getTransferable().isDataFlavorSupported(flavor)) {
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
          final Transferable proxy = support.getTransferable();
          final DataFlavor flavor = mAcceptedDataFlavorVisitor.getDataFlavor(parentOfDropLoc);
          try {
            final List<InsertInfo> inserts = getInsertInfo(proxy, flavor, mDropList, mDropIndex);
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
      mImportedToThisPanel = true;
      return true;
    }

    private static final long serialVersionUID = 1L;
    private boolean mImportedToThisPanel;
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
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
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
          if( flavor != WatersDataFlavor.TYPELESS_FOREACH){
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

    public DataFlavor visitParameterBindingProxy
      (final ParameterBindingProxy binding)
    {
      return WatersDataFlavor.PARAMETER_BINDING_LIST;
    }
  }


  //#########################################################################
  //# Inner Class AcceptedDataFlavorVisitor
  private class AcceptedDataFlavorVisitor extends AbstractModuleProxyVisitor
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
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    @Override
    public DataFlavor visitProxy(final Proxy alias)
    {
      return null;
    }

    @Override
    public DataFlavor visitEventAliasProxy(final EventAliasProxy alias)
    {
      return WatersDataFlavor.IDENTIFIER_LIST;
    }

    @Override
    public Object visitForeachProxy(final ForeachProxy foreach)
    {
      final Subject subject = (Subject) foreach;
      if (SubjectTools.getAncestor(subject, EventAliasSubject.class) != null) {
        return WatersDataFlavor.IDENTIFIER_LIST;
      } else {
        return getSupportedDataFlavor();
      }
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy proxy)
    {
      return getSupportedDataFlavor();
    }

    @Override
    public DataFlavor visitInstanceProxy(final InstanceProxy inst)
    {
      return WatersDataFlavor.PARAMETER_BINDING_LIST;
    }
  }


  //#########################################################################
  //# Inner Class PrinterVisitor
  private class PrintVisitor extends HTMLPrinter
  {

    public String toString(final Proxy proxy, final boolean expanded)
    {
      mExpanded = expanded;
      return toString(proxy);
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

  private static final long serialVersionUID = 1L;
  private final ModuleTreeModel mModel;
  private final ModuleWindowInterface mRoot;
  private List<Observer> mObservers;
  private final AcceptedDataFlavorVisitor mAcceptedDataFlavorVisitor;
  private final ExportedDataFlavorVisitor mExportedDataFlavorVisitor;
  private final ModuleContext mModuleContext;
  private final PrintVisitor mPrinter;
  private boolean mIsPermanentFocusOwner;
  private final DoubleClickVisitor mDoubleClickVisitor;
}