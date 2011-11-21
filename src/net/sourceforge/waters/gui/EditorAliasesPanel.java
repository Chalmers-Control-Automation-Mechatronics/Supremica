package net.sourceforge.waters.gui;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
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
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.transfer.AliasTransferable;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.ConstantAliasSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;


/**
 * The Aliases Panel which shows under the definitions tab of the module
 * editor ({@link org.supremica.gui.ide.EditorPanel EditorPanel}).
 *
 * @author Carly Hona
 */
public class EditorAliasesPanel extends JTree implements SelectionOwner,
  Autoscroll
{

  public EditorAliasesPanel(final ModuleWindowInterface root,
                            final WatersPopupActionManager manager)
  {
    mRoot = root;
    mPopupFactory =
      new AliasesTreePopupFactory(manager, mRoot.getModuleContext());

    final ModuleSubject module = mRoot.getModuleSubject();
    mModel = new ConstantAliasesTreeModel(module);
    setModel(mModel);
    final MouseListener handler = new EditorAliasMouseListener();
    addMouseListener(handler);

    setRootVisible(false);
    setShowsRootHandles(true);
    setAutoscrolls(true);
    // Don't expand/collapse on double-click, never collapse the root.
    setToggleClickCount(0);
    manager.installCutCopyPasteActions(this);

    setTransferHandler(new AliasesPanelTransferHandler());
    setDragEnabled(true);
    setDropMode(DropMode.INSERT);
  }

  //#########################################################################
  //# Simple Access
  public ConstantAliasesTreeModel getConstantAliasTreeModel()
  {
    return (ConstantAliasesTreeModel) getModel();
  }

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
    final ConstantAliasesTreeModel model = getConstantAliasTreeModel();
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
    final ConstantAliasesTreeModel model = getConstantAliasTreeModel();
    final ProxySubject subject = (ProxySubject) item;
    if (subject == model.getRoot()) {
      return isRootVisible() ? subject : null;
    } else {
      return model.isInTree(subject) ? subject : null;
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
    final ConstantAliasesTreeModel model = getConstantAliasTreeModel();
    for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      final TreePath path = model.createPath(subject);
      addSelectionPath(path);
    }
  }

  public void removeFromSelection(final List<? extends Proxy> items)
  {
    final ConstantAliasesTreeModel model = getConstantAliasTreeModel();
    for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      final TreePath path = model.createPath(subject);
      removeSelectionPath(path);
    }
  }

  public ListInsertPosition getInsertPosition(final Proxy proxy)
  {
    final ModuleSubject module = mRoot.getModuleSubject();
    final ListSubject<? extends ProxySubject> list =
      module.getConstantAliasListModifiable();
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
    /*
     * DataFlavor common = null; for (final Proxy proxy : items) { final
     * DataFlavor flavor = mDataFlavorVisitor.getDataFlavor(proxy); if (common
     * == null) { common = flavor; } else if (common != flavor) { return
     * false; } } return common != null;
     */
    return !items.isEmpty();
  }

  public Transferable createTransferable(final List<? extends Proxy> items)
  {
    return new AliasTransferable(items);
  }

  public boolean canPaste(final Transferable transferable)
  {
    return transferable
      .isDataFlavorSupported(WatersDataFlavor.MODULE_ALIAS_LIST);
  }

  public List<InsertInfo> getInsertInfo(final Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    final List<InsertInfo> result = new ArrayList<InsertInfo>();
    @SuppressWarnings("unchecked")
    final List<Proxy> transferData =
      (List<Proxy>) transferable
        .getTransferData(WatersDataFlavor.MODULE_ALIAS_LIST);
    final ListSubject<? extends ProxySubject> listInModule =
      mRoot.getModuleSubject().getConstantAliasListModifiable();
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
    final ConstantAliasesTreeModel model = getConstantAliasTreeModel();
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
      //expand(victim);
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
    final ConstantAliasesTreeModel model = getConstantAliasTreeModel();
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
    final ConstantAliasesTreeModel model = getConstantAliasTreeModel();
    model.close();
  }

  private Proxy getClickedItem(final MouseEvent event)
  {
    final TreePath path = getPathForLocation(event.getX(), event.getY());
    return path == null ? null : (Proxy) path.getLastPathComponent();
  }


  //#########################################################################
  //# Inner Class EventDeclMouseListener
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
      mPopupFactory.maybeShowPopup(EditorAliasesPanel.this, event, clicked);
    }

  }

  private int getRowToDropOn(final Point location)
  {
    int row = getClosestRowForLocation(location.x, location.y);
    mDroppingOnLastRow = false;
    final Rectangle bounds = getRowBounds(row);

    //if location is on lower half of the row..
    if (location.y >= bounds.y + bounds.height / 2) {

      if (getRowCount() - 1 == row) {
        mDroppingOnLastRow = true;
      } else {
        row++;
      }
    }
    return row;
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

        mProxies = proxies;
        mImportedToThisPanel = false;
        return new AliasTransferable((List<Proxy>) proxies);
      }
      return null;

    }

    @Override
    public void exportDone(final JComponent c, final Transferable t,
                           final int action)
    {
      if(mImportedToThisPanel){
        if (action == MOVE) {
          mRoot.getModuleSubject().getConstantAliasListModifiable()
            .removeAll(mProxies);
        }
      }
    }

    @Override
    public boolean canImport(final TransferSupport support)
    {
      if (support.getComponent() instanceof JTree) {
        if (support.getTransferable()
          .isDataFlavorSupported(WatersDataFlavor.MODULE_ALIAS_LIST)) {
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
      }
      final Transferable tr = support.getTransferable();
      if (support.getTransferable()
        .isDataFlavorSupported(WatersDataFlavor.MODULE_ALIAS_LIST)) {
        try {
          @SuppressWarnings("unchecked")
          final List<? extends Proxy> transferData =
            (List<? extends Proxy>) tr
              .getTransferData(WatersDataFlavor.MODULE_ALIAS_LIST);
          final ListSubject<ConstantAliasSubject> list =
            mRoot.getModuleSubject().getConstantAliasListModifiable();
          final ModuleProxyCloner cloner =
            ModuleSubjectFactory.getCloningInstance();

          final Point dl =
            (Point) support.getDropLocation().getDropPoint().getLocation();
          int row = getRowToDropOn(dl);
          if (mDroppingOnLastRow) {
            row++;
          }

          final TreePath[] pathsToSelect = new TreePath[transferData.size()];

          for (int i = transferData.size() - 1; i >= 0; i--) {
            final ConstantAliasSubject cloned =
              (ConstantAliasSubject) cloner.getClone(transferData.get(i));
            list.add(row, cloned);
            pathsToSelect[i] = getPathForRow(row);
          }
          setSelectionPaths(pathsToSelect);
        } catch (final UnsupportedFlavorException exception) {
          exception.printStackTrace();
        } catch (final IOException exception) {
          exception.printStackTrace();
        }
        mImportedToThisPanel = true;
        return true;
      }
      return false;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<? extends Proxy> mProxies = null;
    private boolean mImportedToThisPanel;
  }


  /**
  *
  */
  private static final long serialVersionUID = 1L;

  private final ConstantAliasesTreeModel mModel;
  private final PopupFactory mPopupFactory;
  private final ModuleWindowInterface mRoot;
  private List<Observer> mObservers;
  private boolean mDroppingOnLastRow;

  //private final DataFlavorVisitor mDataFlavorVisitor;
}