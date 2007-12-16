//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ComponentsTree
//###########################################################################
//# $Id: ComponentsTree.java,v 1.7 2007-12-16 22:09:39 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.transfer.ComponentTransferable;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.ForeachComponentSubject;
import net.sourceforge.waters.subject.module.IdentifiedSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;

import org.supremica.util.VPopupMenu;


/**
 * The tree-view panel that shows the components list of a module.
 *
 * @author Robi Malik
 */

public class ComponentsTree
  extends JTree
  implements SelectionOwner, MouseListener,
             FocusListener, TreeSelectionListener
{

  //#########################################################################
  //# Constructor
  public ComponentsTree(final ModuleWindowInterface root,
			final WatersPopupActionManager manager)
  {
    super(new ComponentsTreeModel(root.getModuleSubject()));
    mRoot = root;
    mModuleContext = root.getModuleContext();
    mPrinter = new HTMLPrinter();
    mDoubleClickVisitor = new DoubleClickVisitor();
    mPopupFactory = new ComponentsTreePopupFactory(manager);
    mDataFlavorVisitor = new DataFlavorVisitor();
    mObservers = null;
    mIsPermanentFocusOwner = false;
    addMouseListener(this);
    addFocusListener(this);
    addTreeSelectionListener(this);
    ToolTipManager.sharedInstance().registerComponent(this);
    setRootVisible(false);
    setShowsRootHandles(true);
    setCellRenderer(new ComponentsTreeCellRenderer());
    setAutoscrolls(true);
    manager.installCutCopyPasteActions(this);
    // Don't expand/collapse on double-click, never collapse the root.
    setToggleClickCount(0);
    // Expand all foreach-component entries.
    final ModuleSubject module = root.getModuleSubject();
    expandAll(module);
  }


  //#########################################################################
  //# Simple Access
  public ComponentsTreeModel getComponentsTreeModel()
  {
    return (ComponentsTreeModel) getModel();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.transfer.SelectionOwner
  public UndoInterface getUndoInterface()
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
    final ComponentsTreeModel model = getComponentsTreeModel();
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
    return item;
  }

  public void replaceSelection(final List<? extends Proxy> items)
  {
    clearSelection();
    addToSelection(items);
  }

  public void addToSelection(final List<? extends Proxy> items)
  {
    final ComponentsTreeModel model = getComponentsTreeModel();
    for (final Proxy proxy : items) { 
      final ProxySubject subject = (ProxySubject) proxy;
      final TreePath path = model.createPath(subject);
      addSelectionPath(path);
    }
  }

  public void removeFromSelection(final List<? extends Proxy> items)
  {
    final ComponentsTreeModel model = getComponentsTreeModel();
    for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      final TreePath path = model.createPath(subject);
      removeSelectionPath(path);
    }
  }

  public ListInsertPosition getInsertPosition(final Proxy proxy)
  {
    final Proxy anchor = getSelectionAnchor();
    final ListSubject<? extends ProxySubject> list;
    if (anchor instanceof ForeachComponentSubject) {
      final ForeachComponentSubject foreach = (ForeachComponentSubject) anchor;
      list = foreach.getBodyModifiable();
    } else {
      final ModuleSubject module = mRoot.getModuleSubject();
      list = module.getComponentListModifiable();
    }
    final int inspos = list.size();
    return new ListInsertPosition(list, inspos);
  }

  public void insertCreatedItem(final Proxy proxy, final Object insobj)
  {
    final ProxySubject subject = (ProxySubject) proxy;
    final ListInsertPosition inspos = (ListInsertPosition) insobj;
    final List<ProxySubject> list = Casting.toList(inspos.getList());
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
    final ComponentsTreeModel model = getComponentsTreeModel();
    final Set<Proxy> set = new HashSet<Proxy>(items);
    final List<Proxy> transferred = new LinkedList<Proxy>();
    outer:
    for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      ProxySubject parent = model.getParentInTree(subject);
      if (parent != null) {
        while (parent.getParent() != null) {
          if (set.contains(parent)) {
            continue outer;
          }
          parent = model.getParentInTree(parent);
        }
        transferred.add(proxy);
      }
    }
    final Proxy first = transferred.iterator().next();
    return new ComponentTransferable(transferred);
  }

  public boolean canPaste(final Transferable transferable)
  {
    return transferable.isDataFlavorSupported
      (WatersDataFlavor.MODULE_COMPONENT_LIST);
  }

  public List<InsertInfo> getInsertInfo(Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    final List<Proxy> data = (List<Proxy>) transferable.getTransferData
      (WatersDataFlavor.MODULE_COMPONENT_LIST);
    final int size = data.size();
    final ListSubject<? extends ProxySubject> list;
    final ModuleContext context;
    final Set<String> names;
    final Proxy anchor = getSelectionAnchor();
    if (anchor instanceof ForeachComponentSubject) {
      final ForeachComponentSubject foreach = (ForeachComponentSubject) anchor;
      list = foreach.getBodyModifiable();
      context = null;
      names = null;
    } else {
      final ModuleSubject module = mRoot.getModuleSubject();
      list = module.getComponentListModifiable();
      context = mRoot.getModuleContext();
      names = new HashSet<String>(size);
    }
    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    final List<InsertInfo> result = new ArrayList<InsertInfo>(size);
    int pos = list.size();
    for (final Proxy proxy : data) {
      final Proxy cloned = cloner.getClone(proxy);
      if (context != null && cloned instanceof IdentifiedSubject) {
        final IdentifiedSubject comp = (IdentifiedSubject) cloned;
        final IdentifierSubject oldident = comp.getIdentifier();
        final IdentifierSubject newident =
          context.getPastedComponentName(oldident, names);
        final String newname = newident.getName();
        comp.setIdentifier(newident);
        names.add(newname);
      }
      final ListInsertPosition inspos = new ListInsertPosition(list, pos++);
      final InsertInfo info = new InsertInfo(cloned, inspos);
      result.add(info);
    }
    return result;
  }

  public boolean canDelete(final List<? extends Proxy> items)
  {
    return !items.isEmpty();
  }

  public List<InsertInfo> getDeletionVictims(final List<? extends Proxy> items)
  {
    final ComponentsTreeModel model = getComponentsTreeModel();
    final Set<Proxy> set = new HashSet<Proxy>(items);
    final List<InsertInfo> result = new LinkedList<InsertInfo>();
    outer:
    for (final Proxy proxy : items) {
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

  public void insertItems(List<InsertInfo> inserts)
  {
    for (final InsertInfo insert : inserts) {
      final ProxySubject victim = (ProxySubject) insert.getProxy();
      final ListInsertPosition inspos =
        (ListInsertPosition) insert.getInsertPosition();
      final List<ProxySubject> list = Casting.toList(inspos.getList());
      final int pos = inspos.getPosition();
      list.add(pos, victim);
      expand(victim);
    }
  }

  public void deleteItems(List<InsertInfo> deletes)
  {
    final int size = deletes.size();
    final ListIterator<InsertInfo> iter = deletes.listIterator(size);
    while (iter.hasPrevious()) {
      final InsertInfo delete = (InsertInfo) iter.previous();
      final Proxy victim = delete.getProxy();
      final ListInsertPosition inspos =
        (ListInsertPosition) delete.getInsertPosition();
      final ListSubject<? extends ProxySubject> list = inspos.getList();
      final int index = inspos.getPosition();
      list.remove(index);
    }
  }

  public void scrollToVisible(final List<? extends Proxy> list)
  {
    if (!list.isEmpty()) {
      final ComponentsTreeModel model = getComponentsTreeModel();
      final ProxySubject first = (ProxySubject) list.iterator().next();
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
    final ComponentsTreeModel model = getComponentsTreeModel();
    model.close();
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
  //# Interface java.awt.event.MouseListener
  public void mouseClicked(final MouseEvent event)
  {
    final Proxy clicked = getClickedItem(event);
    switch (event.getClickCount()) {
    case 1:
      if (clicked == null) {
        clearSelection();
      }
      break;
    case 2:
      mDoubleClickVisitor.invokeDoubleClickAction(clicked, event);
      break;
    default:
      break;
    }
  }

  public void mouseEntered(final MouseEvent event)
  {
  }

  public void mouseExited(final MouseEvent event)
  {
  }

  public void mousePressed(final MouseEvent event)
  {
    final Proxy proxy = getClickedItem(event);
    mPopupFactory.maybeShowPopup(this, event, proxy);
  }

  public void mouseReleased(final MouseEvent event)
  {
    final Proxy proxy = getClickedItem(event);
    mPopupFactory.maybeShowPopup(this, event, proxy);
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


  //#########################################################################
  //# Interface javax.swing.event.TreeSelectionListener
  public void valueChanged(final TreeSelectionEvent event)
  {
    // Why can't the new selection be read immediately ???
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          fireSelectionChanged();
        }
      });
  }


  //#########################################################################
  //# Auxiliary Methods
  private void expandAll(final ModuleSubject module)
  {
    final List<? extends ProxySubject> list =
      module.getComponentListModifiable();
    expandAll(list);
  }

  private void expandAll(final List<? extends ProxySubject> list)
  {
    for (final ProxySubject subject : list) {
      expand(subject);
    }
  }

  private void expand(final ProxySubject subject)
  {
    if (subject instanceof ForeachComponentSubject) {
      final ForeachComponentSubject foreach =
        (ForeachComponentSubject) subject;
      final ComponentsTreeModel model = getComponentsTreeModel();
      final TreePath path = model.createPath(subject);
      expandPath(path);
      final List<? extends ProxySubject> body = foreach.getBodyModifiable();
      expandAll(body);
    }
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
  //# Inner Class ComponentsTreeCellRenderer
  private class ComponentsTreeCellRenderer
    extends DefaultTreeCellRenderer
  {

    //#######################################################################
    //# Interface javax.swing.tree.TreeCellRenderer
    public Component getTreeCellRendererComponent
      (final JTree tree, final Object value, final boolean sel,
       final boolean expanded, final boolean leaf,
       final int row, final boolean hasFocus)
    {
      super.getTreeCellRendererComponent
	(tree, value, sel, expanded, leaf, row, hasFocus);
      final Proxy proxy = (Proxy) value;
      final String text = mPrinter.toString(proxy);
      setText(text);
      final ImageIcon icon = mModuleContext.getImageIcon(proxy);
      setIcon(icon);
      final String tooltip = mModuleContext.getToolTipText(proxy);
      setToolTipText(tooltip);
      setOpaque(sel);
      if (sel) {
        if (mIsPermanentFocusOwner) {
          setBackground(EditorColor.BACKGROUND_FOCUSSED);
        } else {
          setBackground(EditorColor.BACKGROUND_NOTFOCUSSED);
        }
      }
      return this;
    }

  }


  //#########################################################################
  //# Inner Class DoubleClickVisitor
  private class DoubleClickVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private void invokeDoubleClickAction(final Proxy proxy,
                                         final MouseEvent event)
    {
      if (proxy != null) {
        final IDEAction action = getDoubleClickAction(proxy);
        if (action != null) {
          final WatersPopupActionManager manager = mPopupFactory.getMaster();
          manager.invokeDoubleClickAction(action, event);        
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
      final WatersPopupActionManager manager = mPopupFactory.getMaster();
      final IDEAction action = manager.getPropertiesAction(proxy);
      return action.isEnabled() ? action : null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public IDEAction visitModuleProxy(final ModuleProxy module)
    {
      final WatersPopupActionManager manager = mPopupFactory.getMaster();
      return manager.getShowModuleCommentAction();
    }

    public IDEAction visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      final WatersPopupActionManager manager = mPopupFactory.getMaster();
      return manager.getShowGraphAction(comp);
    }

  }


  //#########################################################################
  //# Inner Class DataFlavorVisitor
  private class DataFlavorVisitor
    extends AbstractModuleProxyVisitor
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
    public DataFlavor visitForeachComponentProxy
      (final ForeachComponentProxy foreach)
    {
      return WatersDataFlavor.MODULE_COMPONENT_LIST;
    }

    public DataFlavor visitInstanceProxy(final InstanceProxy inst)
    {
      return WatersDataFlavor.MODULE_COMPONENT_LIST;
    }

    public DataFlavor visitParameterBindingProxy
      (final ParameterBindingProxy binding)
    {
      return WatersDataFlavor.PARAMETER_BINDING_LIST;
    }

    public DataFlavor visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
    {
      return WatersDataFlavor.MODULE_COMPONENT_LIST;
    }

    public DataFlavor visitVariableComponentProxy
      (final VariableComponentProxy var)
    {
      return WatersDataFlavor.MODULE_COMPONENT_LIST;
    }

  }


  //#########################################################################
  //# Data Members
  private final ModuleWindowInterface mRoot;
  private final ModuleContext mModuleContext;
  private final ProxyPrinter mPrinter;
  private final DoubleClickVisitor mDoubleClickVisitor;
  private final PopupFactory mPopupFactory;
  private final DataFlavorVisitor mDataFlavorVisitor;

  private List<Observer> mObservers;
  private boolean mIsPermanentFocusOwner;

}