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

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.RearrangeTreeCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.ConstantAliasSubject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;


/**
 * The Aliases Panel which shows under the definitions tab of the module
 * editor ({@link org.supremica.gui.ide.EditorPanel EditorPanel}).
 *
 * @author Carly Hona, Robi Malik
 */
public abstract class AliasesTree extends JTree implements SelectionOwner,
  Autoscroll, TreeSelectionListener, FocusListener
{

  public AliasesTree(final ModuleWindowInterface root,
                     final WatersPopupActionManager manager)
  {
    mRoot = root;
    mModuleContext = mRoot.getModuleContext();
    mPrinter = new PrintVisitor();
    mPopupFactory = new AliasesTreePopupFactory(manager, mModuleContext);
    mIsPermanentFocusOwner = true;
    final ModuleSubject module = mRoot.getModuleSubject();
    mModel = new AliasesTreeModel(module, getRootList());
    setModel(mModel);
    final MouseListener handler = new EditorAliasMouseListener();
    addMouseListener(handler);
    addTreeSelectionListener(this);
    addFocusListener(this);

    mDataFlavorVisitor = new DataFlavorVisitor();
    setRootVisible(true);
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
  public AliasesTreeModel getAliasTreeModel()
  {
    return (AliasesTreeModel) getModel();
  }

  public ModuleWindowInterface getRoot()
  {
    return mRoot;
  }

  //#########################################################################
  //# Abstract Methods
  abstract ListSubject<? extends ProxySubject> getRootList();

  abstract String getRootName();

  abstract Transferable getTransferable(List<? extends Proxy> items);

  abstract DataFlavor getSupportedDataFlavor();

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
    return getSelectionCount() < getRowCount();
  }

  public boolean isSelected(final Proxy proxy)
  {
    final AliasesTreeModel model = getAliasTreeModel();
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
    for (int row = 0; row < total; row++) {
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
      return null;
    } else {
      return (Proxy) path.getLastPathComponent();
    }
  }

  public Proxy getSelectableAncestor(final Proxy item)
  {
    final AliasesTreeModel model = getAliasTreeModel();
    final ProxySubject subject = (ProxySubject) item;
    if (subject == model.getRoot()) {
      return isRootVisible() ? subject : null;
    } else {
      return model.canBeInTree(subject) ? subject : null;
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
    final AliasesTreeModel model = getAliasTreeModel();
    for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      final TreePath path = model.createPath(subject);
      addSelectionPath(path);
    }
  }

  public void removeFromSelection(final List<? extends Proxy> items)
  {
    final AliasesTreeModel model = getAliasTreeModel();
    for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      final TreePath path = model.createPath(subject);
      removeSelectionPath(path);
    }
  }

  public ListInsertPosition getInsertPosition(final Proxy proxy)
  {
    final ListSubject<? extends ProxySubject> list =
      (ListSubject<? extends ProxySubject>) getRootList();
    final int inspos = list.size();
    return new ListInsertPosition(list, inspos);
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

    DataFlavor common = null;
    for (final Proxy proxy : items) {
      final DataFlavor flavor = mDataFlavorVisitor.getDataFlavor(proxy);
      if (common == null) {
        common = flavor;
      } else if (common != flavor) {
        return false;
      }
    }
    return common != null;
  }

  public Transferable createTransferable(final List<? extends Proxy> items)
  {
    return getTransferable(items);
  }

  public boolean canPaste(final Transferable transferable)
  {
    return transferable.isDataFlavorSupported(getSupportedDataFlavor());
  }

  public List<InsertInfo> getInsertInfo(final Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    final List<InsertInfo> result = new ArrayList<InsertInfo>();
    @SuppressWarnings("unchecked")
    final List<Proxy> transferData =
      (List<Proxy>) transferable.getTransferData(mDataFlavorVisitor
        .getDataFlavor(getSelectionAnchor()));
    final ListSubject<? extends ProxySubject> listInModule =
      (ListSubject<? extends ProxySubject>) getRootList();
    int pos = listInModule.size();
    final ModuleProxyCloner cloner =
      ModuleSubjectFactory.getCloningInstance();
    for (final Proxy proxy : transferData) {
      final Proxy cloned = cloner.getClone(proxy);
      final ListInsertPosition inspos =
        new ListInsertPosition(listInModule, pos++);
      final InsertInfo info = new InsertInfo(cloned, inspos);
      result.add(info);
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
    final AliasesTreeModel model = getAliasTreeModel();
    final Set<Proxy> set = new HashSet<Proxy>(items);
    final List<InsertInfo> result = new LinkedList<InsertInfo>();
    outer: for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      ProxySubject parent = model.getParentInTree(subject);
      if (parent != null) {
        while (parent.getParent() != null) {
          if (set.contains(parent)) {
            continue outer;
          }
          parent = model.getParentInTree(parent);
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
    final AliasesTreeModel model = getAliasTreeModel();
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
    final AliasesTreeModel model = getAliasTreeModel();
    model.close();
  }

  private Proxy getClickedItem(final MouseEvent event)
  {
    final Point point = event.getPoint();
    final int row = getClosestRowForLocation(point.x, point.y);
    if (row == 0) {
      clearSelection();
      event.consume();
      return null;
    }
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

  private void expandAll(final List<? extends ProxySubject> list)
  {
    for (final ProxySubject subject : list) {
      expand(subject);
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


  //#########################################################################
  //# Inner Class EditorAliasMouseListener
  /**
   * A simple mouse listener to trigger opening the event declaration editor
   * dialog by double-click, and to trigger a popup menu.
   */
  private class EditorAliasMouseListener extends MouseAdapter
  {
    //#######################################################################
    //# Interface java.awt.MouseListener
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1
          && event.getClickCount() == 2) {
        final ConstantAliasSubject proxy =
          (ConstantAliasSubject) getClickedItem(event);
        @SuppressWarnings("unused")
        final ConstantAliasEditorDialog dialog =
          new ConstantAliasEditorDialog(mRoot, proxy);
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
      mPopupFactory.maybeShowPopup(AliasesTree.this, event, clicked);
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
      final JTree tree = (JTree) c;
      final TreePath[] paths = tree.getSelectionPaths();

      if (paths != null) {
        final List<Proxy> proxies = new ArrayList<Proxy>(1);
        for (int i = 0; i < paths.length; i++) {
          proxies.add((Proxy) paths[i].getLastPathComponent());
        }

        mImportedToThisPanel = false;
        return getTransferable((List<Proxy>) proxies);
      }
      return null;

    }

    @Override
    public void exportDone(final JComponent c, final Transferable t,
                           final int action)
    {
      if (mImportedToThisPanel) {
        if (action == MOVE) {
          final int count = getSelectionCount();
          final List<InsertInfo> inserts = new ArrayList<InsertInfo>(count);
          final List<InsertInfo> deletes = new ArrayList<InsertInfo>(count);
          final int min = getMinSelectionRow();
          final int max = getMaxSelectionRow();

          final Point location = mDropLoc.getLocation();
          int rowOfDrop = getClosestRowForLocation(location.x, location.y);
          final Rectangle bounds = getRowBounds(rowOfDrop);

          //if cursor on lower half of row
          if (location.y >= bounds.y + bounds.height / 2) {
            rowOfDrop++;
          }

          int counter = 0;
          for (int initialRow = min; initialRow <= max; initialRow++) {
            if (isRowSelected(initialRow)) {
              final TreePath path = getPathForRow(initialRow);
              final Proxy proxy = (Proxy) path.getLastPathComponent();

              final InsertInfo delete =
                new InsertInfo(proxy,
                               new ListInsertPosition((ListSubject<? extends ProxySubject>) getRootList(),
                                                      initialRow-1));
              deletes.add(delete);
              if (initialRow < rowOfDrop) {
                counter++;
              }
            }
          }
          rowOfDrop -= counter;

          if (max - min + 1 == deletes.size() && min == rowOfDrop) {
            return;
          }
          for (final InsertInfo delete : deletes) {
            final Proxy proxy = delete.getProxy();
            final InsertInfo insert =
              new InsertInfo(proxy,
                             new ListInsertPosition((ListSubject<? extends ProxySubject>) getRootList(),
                                                    rowOfDrop-1));
            inserts.add(insert);
            rowOfDrop++;
          }
          final RearrangeTreeCommand allMoves =
            new RearrangeTreeCommand(inserts, deletes, AliasesTree.this);
          mRoot.getUndoInterface().executeCommand(allMoves);
        }
      }
    }

    @Override
    public boolean canImport(final TransferSupport support)
    {
      if (support.getComponent() instanceof JTree) {
        if (support.getTransferable()
          .isDataFlavorSupported(getSupportedDataFlavor())) {
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
        mDropLoc =
          (Point) support.getDropLocation().getDropPoint().getLocation();
        support.setShowDropLocation(true);

        mImportedToThisPanel = true;
        return true;
      }
    }

    private static final long serialVersionUID = 1L;
    private boolean mImportedToThisPanel;
    private Point mDropLoc;
  }


  //#########################################################################
  //# Inner Class DataFlavorVisitor
  private class DataFlavorVisitor extends AbstractModuleProxyVisitor
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
    public DataFlavor visitConstantAliasProxy(final ConstantAliasProxy alias)
    {
      return WatersDataFlavor.CONSTANT_ALIAS_LIST;
    }

    @Override
    public DataFlavor visitEventAliasProxy(final EventAliasProxy alias)
    {
      return WatersDataFlavor.EVENT_ALIAS_LIST;
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
  private final AliasesTreeModel mModel;
  private final PopupFactory mPopupFactory;
  private final ModuleWindowInterface mRoot;
  private List<Observer> mObservers;
  private final DataFlavorVisitor mDataFlavorVisitor;
  private final ModuleContext mModuleContext;
  private final PrintVisitor mPrinter;
  private boolean mIsPermanentFocusOwner;
}