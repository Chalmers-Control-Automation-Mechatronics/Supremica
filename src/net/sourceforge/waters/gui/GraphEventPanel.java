//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   GraphEventPanel
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.IDECutAction;
import net.sourceforge.waters.gui.actions.IDEDeleteAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.ReplaceCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.transfer.IdentifierTransferable;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.ReplaceInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.gui.util.NonTypingTable;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.ForeachEventSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;


/**
 * <p>The Events panel which sits next to the graph editor panel.</p>
 *
 * <p>This is used to view the module events which have been selected for
 * use with this particular component, and selecting other events from the
 * module for use with this component.</p>
 *
 * @author Gian Perrone, Robi Malik
 */

public class GraphEventPanel
  extends NonTypingTable
  implements FocusListener, DragGestureListener, SelectionOwner
{

  //#########################################################################
  //# Constructors
  public GraphEventPanel(final EditorWindowInterface eroot,
                         final SimpleComponentSubject comp,
                         final WatersPopupActionManager manager)
  {
    this(eroot, comp.getGraph(), manager);
  }

  public GraphEventPanel(final EditorWindowInterface eroot,
                         final GraphSubject graph,
                         final WatersPopupActionManager manager)
  {
    final Dimension ispacing = new Dimension(0, 0);
    final ModuleWindowInterface modroot = eroot.getModuleWindowInterface();
    final ExpressionParser parser = modroot.getExpressionParser();
    final GraphEventPanelEventHandler handler =
      new GraphEventPanelEventHandler();
    final TableModel model = new EventTableModel(graph, handler, modroot);
    mRoot = eroot;
    mPopupFactory = new GraphEventPanelPopupFactory(manager);
    mDeleteVisitor = new DeleteVisitor();
    mReplaceVisitor = new ReplaceVisitor();
    mObservers = null;

    setModel(model);
    setTableHeader(null);
    setRowHeight(22);
    setShowGrid(false);
    setIntercellSpacing(ispacing);
    setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
    setSurrendersFocusOnKeystroke(true);
    setRowSelectionAllowed(true);

    final TableCellRenderer iconrenderer0 =
      getDefaultRenderer(Icon.class);
    final TableCellRenderer iconrenderer1 =
      new RendererNoFocus(iconrenderer0, false);
    setDefaultRenderer(Icon.class, iconrenderer1);
    final TableCellRenderer textrenderer0 =
      getDefaultRenderer(Object.class);
    final TableCellRenderer textrenderer1 =
      new RendererNoFocus(textrenderer0, true);
    setDefaultRenderer(Object.class, textrenderer1);
    final SimpleExpressionEditor editor =
      new SimpleExpressionEditor(Operator.TYPE_NAME, parser);
    editor.setAllowNull(true);
    editor.addFocusListener(handler);
    setDefaultEditor(Object.class, editor);

    setPreferredSizes();

    setSelectionBackground(EditorColor.BACKGROUND_NOTFOCUSSED);
    addFocusListener(this);
    final MouseListener mouser = new MouseHandler();
    addMouseListener(mouser);
    final ListSelectionModel selmodel = getSelectionModel();
    final ListSelectionListener listener = new SelectionListener();
    selmodel.addListSelectionListener(listener);

    final DragSource dragsource = DragSource.getDefaultDragSource();
    dragsource.createDefaultDragGestureRecognizer
      (this, DnDConstants.ACTION_COPY, this);
    final DropTargetListener dropper = new DropListener();
    final DropTarget droptarget = new DropTarget(this, dropper);

    final Action add = manager.getInsertEventLabelAction();
    addKeyboardAction(add);
    addCycleActions();
    manager.installCutCopyPasteActions(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.transfer.SelectionOwner
  public UndoInterface getUndoInterface(final Action action)
  {
    if (action instanceof IDECutAction || action instanceof IDEDeleteAction) {
      return mRoot.getUndoInterface();
    } else {
      return null;
    }
  }

  public boolean hasNonEmptySelection()
  {
    return getSelectedRowCount() > 0;
  }

  public boolean canSelectMore()
  {
    return getSelectedRowCount() < getRowCount();
  }

  public boolean isSelected(final Proxy proxy)
  {
    final int row = getRow(proxy);
    return isRowSelected(row);
  }

  public List<IdentifierSubject> getCurrentSelection()
  {
    final EventTableModel model = (EventTableModel) getModel();
    int row = getSelectedRow();
    int count = getSelectedRowCount();
    final List<IdentifierSubject> list =
      new ArrayList<IdentifierSubject>(count);
    while (count > 0) {
      if (isRowSelected(row)) {
        final IdentifierSubject ident = model.getIdentifier(row);
        list.add(ident);
        count--;
      }
      row++;
    }
    return list;
  }

  public List<IdentifierSubject> getAllSelectableItems()
  {
    final EventTableModel model = (EventTableModel) getModel();
    final int count = getRowCount();
    final List<IdentifierSubject> list =
      new ArrayList<IdentifierSubject>(count);
    for (int row = 0; row < count; row++) {
      final IdentifierSubject ident = model.getIdentifier(row);
      list.add(ident);
    }
    return list;
  }

  public IdentifierSubject getSelectionAnchor()
  {
    if (getSelectedRowCount() > 0) {
      final EventTableModel model = (EventTableModel) getModel();
      final ListSelectionModel selmodel = getSelectionModel();
      final int row = selmodel.getAnchorSelectionIndex();
      return model.getIdentifier(row);
    } else {
      return null;
    }
  }

  public Proxy getSelectableAncestor(final Proxy item)
  {
    return item;
  }

  public void clearSelection(final boolean propagate)
  {
    clearSelection();
    if (propagate) {
      final ControlledSurface surface = mRoot.getControlledSurface();
      surface.clearSelection(propagate);
    }
  }

  public void replaceSelection(List<? extends Proxy> items)
  {
    boolean propagate = false;
    for (final Proxy proxy : items) {
      final int row = getRow(proxy);
      if (row < 0) {
        propagate = true;
        break;
      }
    }
    clearSelection(propagate);
    addToSelection(items);
  }

  public void addToSelection(List<? extends Proxy> items)
  {
    final List<Proxy> others = new LinkedList<Proxy>();
    int row0 = -1;
    int row1 = -1;
    for (final Proxy proxy : items) {
      final int row = getRow(proxy);
      if (row >= 0) {
        if (row0 < 0) {
          row0 = row1 = row;
        } else if (row == row1 + 1) {
          row1 = row;
        } else {
          addRowSelectionInterval(row0, row1);
          row0 = row1 = row;
        }
      } else {
        others.add(proxy);
      }
    }
    if (row0 >= 0) {
      addRowSelectionInterval(row0, row1);
    }
    final ControlledSurface surface = mRoot.getControlledSurface();
    surface.addToSelection(others);  
  }

  public void removeFromSelection(List<? extends Proxy> items)
  {
    final List<Proxy> others = new LinkedList<Proxy>();
    int row0 = -1;
    int row1 = -1;
    for (final Proxy proxy : items) {
      final int row = getRow(proxy);
      if (row >= 0) {
        if (row0 < 0) {
          row0 = row1 = row;
        } else if (row == row1 + 1) {
          row1 = row;
        } else {
          removeRowSelectionInterval(row0, row1);
          row0 = row1 = row;
        }
      } else {
        others.add(proxy);
      }
    }
    if (row0 >= 0) {
      removeRowSelectionInterval(row0, row1);
    }
    final ControlledSurface surface = mRoot.getControlledSurface();
    surface.removeFromSelection(others);  
  }

  public Object getInsertPosition(final Proxy item)
  {
    return null;
  }

  public void insertCreatedItem(final Proxy item, final Object inspos)
  {
    final IdentifierSubject ident = (IdentifierSubject) item;
    final EventTableModel model = (EventTableModel) getModel();
    model.addIdentifier(ident);
  }

  public boolean canCopy(List<? extends Proxy> items)
  {
    return hasNonEmptySelection();
  }

  public Transferable createTransferable(List<? extends Proxy> items)
  {
    return new IdentifierTransferable(items);
  }

  public boolean canPaste(final Transferable transferable)
  {
    try {
      if (transferable.isDataFlavorSupported
          (WatersDataFlavor.IDENTIFIER_LIST)) {
        final List<Proxy> data = (List<Proxy>)
          transferable.getTransferData(WatersDataFlavor.IDENTIFIER_LIST);
        for (final Proxy proxy : data) {
          if (!containsEqualIdentifier(proxy)) {
            return true;
          }
        }
        return false;
      } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        final String data = 
          (String) transferable.getTransferData(DataFlavor.stringFlavor);
        final ModuleWindowInterface modroot = mRoot.getModuleWindowInterface();
        final ExpressionParser parser = modroot.getExpressionParser();
        try {
          final IdentifierProxy ident = parser.parseIdentifier(data);
          return !containsEqualIdentifier(ident);
        } catch (final ParseException exception) {
          return false;
        }
      } else {
        return false;
      }
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final UnsupportedFlavorException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public List<InsertInfo> getInsertInfo(final Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    final List<InsertInfo> inserts = new LinkedList<InsertInfo>();
    if (transferable.isDataFlavorSupported(WatersDataFlavor.IDENTIFIER_LIST)) {
      final ModuleProxyCloner cloner =
        ModuleSubjectFactory.getCloningInstance();
      final List<Proxy> data = (List<Proxy>)
        transferable.getTransferData(WatersDataFlavor.IDENTIFIER_LIST);
      for (final Proxy proxy : data) {
        if (!containsEqualIdentifier(proxy)) {
          final Proxy cloned = cloner.getClone(proxy);
          final InsertInfo insert = new InsertInfo(cloned);
          inserts.add(insert);
        }
      }
    } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      final String data = 
        (String) transferable.getTransferData(DataFlavor.stringFlavor);
      final ModuleWindowInterface modroot = mRoot.getModuleWindowInterface();
      final ExpressionParser parser = modroot.getExpressionParser();
      try {
        final IdentifierProxy ident = parser.parseIdentifier(data);
        final InsertInfo insert = new InsertInfo(ident);
        inserts.add(insert);
      } catch (final ParseException exception) {
        final UnsupportedFlavorException rethrown =
          new UnsupportedFlavorException(DataFlavor.stringFlavor);
        rethrown.initCause(exception);
        throw rethrown;
      }
    } else {
      throw new UnsupportedFlavorException(null);
    }
    return inserts;
  }

  public boolean canDelete(final List<? extends Proxy> items)
  {
    for (final Proxy proxy : items) {
      if (getRow(proxy) >= 0) {
        return true;
      }
    }
    return false;
  }

  public List<InsertInfo> getDeletionVictims(final List<? extends Proxy> items)
  {
    final List<InsertInfo> inserts = new LinkedList<InsertInfo>();
    mDeleteVisitor.addDeletionVictims(items, inserts);
    if (inserts.isEmpty()) {
      removeFromSelection(items);
      for (final Proxy proxy : items) {
        if (proxy instanceof IdentifierSubject) {
          final IdentifierSubject ident = (IdentifierSubject) proxy;
          final EventTableModel model = (EventTableModel) getModel();
          model.removeIdentifier(ident);
        }
      }
      return null;
    } else {
      final int end = items.size();
      final ListIterator<? extends Proxy> iter = items.listIterator(end);
      while (iter.hasPrevious()) {
        final Proxy proxy = iter.previous();
        final InsertInfo insert = new InsertInfo(proxy);
        inserts.add(0, insert);
      }
      return inserts;
    }
  }

  public void insertItems(final List<InsertInfo> inserts)
  {
    for (final InsertInfo insert : inserts) {
      final Proxy proxy = insert.getProxy();
      final Object inspos = insert.getInsertPosition();
      if (inspos == null) {
        final IdentifierSubject ident = (IdentifierSubject) proxy;
        final EventTableModel model = (EventTableModel) getModel();
        model.addIdentifier(ident);
      } else {
        final ListInsertPosition linspos = (ListInsertPosition) inspos;
        final List<Proxy> eventlist = Casting.toList(linspos.getList());
        final int pos = linspos.getPosition();
        eventlist.add(pos, proxy);
      }
    }
  }

  public void deleteItems(final List<InsertInfo> inserts)
  {
    for (final InsertInfo insert : inserts) {
      final Proxy proxy = insert.getProxy();
      final Object inspos = insert.getInsertPosition();
      if (inspos == null) {
        final IdentifierSubject ident = (IdentifierSubject) proxy;
        final EventTableModel model = (EventTableModel) getModel();
        model.removeIdentifier(ident);
      } else {
        final ListInsertPosition linspos = (ListInsertPosition) inspos;
        final List<Proxy> eventlist = Casting.toList(linspos.getList());
        eventlist.remove(proxy);
      }
    }
  }

  public void scrollToVisible(final List<? extends Proxy> items)
  {
    Rectangle bounds = null;
    for (final Proxy proxy : items) {
      final int row = getRow(proxy);
      if (row >= 0) {
        final Rectangle rect = getCellRect(row, 0, true);
        if (bounds == null) {
          bounds = rect;
        } else if (bounds.y > rect.y) {
          bounds.y = rect.y;
        } else if (bounds.y + bounds.height < rect.y + rect.height) {
          bounds.height += (rect.y + rect.height) - (bounds.y + bounds.height);
        }
      }
    }
    if (bounds != null) {
      scrollRectToVisible(bounds);
    }
  }

  public void activate()
  {
    if (!isFocusOwner()) {
      final ModuleWindowInterface modroot = mRoot.getModuleWindowInterface();
      modroot.showPanel(this);
      requestFocusInWindow();
    }
  }

  public void close()
  {
    final EventTableModel model = (EventTableModel) getModel();
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
  //# Overrides for Base Class javax.swing.JTable
  public void tableChanged(final TableModelEvent event)
  {
    super.tableChanged(event);
    final int row0 = event.getFirstRow();
    final int row1 = event.getLastRow();
    if (row0 >= 0 && row1 >= 0) {
      switch (event.getType()) {
      case TableModelEvent.INSERT:
      case TableModelEvent.DELETE:
	final Dimension prefsize = getPreferredSize();
	prefsize.height = calculateHeight();
	setPreferredSize(prefsize);
	setPreferredScrollableViewportSize(prefsize);
	revalidate();
	break;
      default:
	break;
      }
    }
  }

  public String getToolTipText(final MouseEvent event)
  {
    final Point point = event.getPoint();
    final int row = rowAtPoint(point);
    if (row >= 0) {
      final EventTableModel model = (EventTableModel) getModel();
      return model.getToolTipText(row);
    } else {
      return null;
    }
  }

  public boolean getScrollableTracksViewportHeight()
  {
    final Container viewport = getParent();
    return getPreferredSize().height < viewport.getHeight();
  }


  //#########################################################################
  //# Interface java.awt.event.FocusListener
  public void focusGained(final FocusEvent event)
  {
    if (!event.isTemporary()) {
      setSelectionBackground(EditorColor.BACKGROUND_FOCUSSED);
    }
  }

  public void focusLost(final FocusEvent event)
  {
    if (!event.isTemporary()) {
      setSelectionBackground(EditorColor.BACKGROUND_NOTFOCUSSED);
    }
  }


  //#######################################################################
  //# Interface java.awt.dnd.DragGestureListener
  public void dragGestureRecognized(final DragGestureEvent event)
  {
    final EventTableModel model = (EventTableModel) getModel();
    final int[] rows = getSelectedRows();
    if (rows.length == 0) {
      return;
    }
    final List<IdentifierSubject> idents =
      new ArrayList<IdentifierSubject>(rows.length);
    for(int i = 0; i < rows.length; i++) {
      final IdentifierSubject ident = model.getIdentifier(rows[i]);
      idents.add(ident);
    }
    final Transferable trans = new IdentifierTransferable(idents);
    try {
      event.startDrag(DragSource.DefaultCopyDrop, trans);
    } catch (InvalidDnDOperationException exception) {
      throw new IllegalArgumentException(exception);
    }
  }


  //#########################################################################
  //# Editing
  public void createEvent()
  {
    if (isEditing()) {
      final SimpleExpressionCell comp =
	(SimpleExpressionCell) getEditorComponent();
      try {
	comp.commitEdit();
	if (comp.getValue() == null) {
	  return;
	}
      } catch (final java.text.ParseException exception) {
	return;
      }
      SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (!isEditing()) {
              createEvent();
            }
          }
        });
    } else {
      clearSelection();
      final EventTableModel model = (EventTableModel) getModel();
      final int row = model.createEvent();
      addRowSelectionInterval(row, row);
      if (editCellAt(row, 1)) {
        final Component comp = getEditorComponent();
        final Rectangle bounds = comp.getBounds();
        scrollRectToVisible(bounds);
        comp.requestFocusInWindow();
      }
    }
  }

  public void editEvent(final Proxy proxy)
  {
    final int row = getRow(proxy);
    if (row < 0) {
      return;
    } else if (isEditing()) {
      final SimpleExpressionCell comp =
	(SimpleExpressionCell) getEditorComponent();
      try {
	comp.commitEdit();
	if (comp.getValue() == null) {
	  return;
	}
      } catch (final java.text.ParseException exception) {
	return;
      }
      SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (!isEditing()) {
              editEvent(proxy);
            }
          }
        });
    } else {
      clearSelection();
      addRowSelectionInterval(row, row);
      if (editCellAt(row, 1)) {
        final Component comp = getEditorComponent();
        final Rectangle bounds = comp.getBounds();
        scrollRectToVisible(bounds);
        comp.requestFocusInWindow();
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private int getRow(final Proxy proxy)
  {
    if (proxy instanceof IdentifierSubject) {
      final IdentifierSubject ident = (IdentifierSubject) proxy;
      final EventTableModel model = (EventTableModel) getModel();
      return model.getRow(ident);
    } else {
      return -1;
    }
  }

  private boolean containsEqualIdentifier(final Proxy proxy)
  {
    final EventTableModel model = (EventTableModel) getModel();
    if (proxy instanceof IdentifierSubject) {
      final IdentifierSubject ident = (IdentifierSubject) proxy;
      return model.containsEqualIdentifier(ident);
    } else if (proxy instanceof IdentifierProxy) {
      final ModuleProxyCloner cloner =
        ModuleSubjectFactory.getCloningInstance();
      final IdentifierSubject ident =
        (IdentifierSubject) cloner.getClone(proxy);
      return model.containsEqualIdentifier(ident);
    } else {
      return false;
    }
  }

  private void fireSelectionChanged()
  {
    final EditorChangedEvent event = new SelectionChangedEvent(this);
    fireEditorChangedEvent(event);
  }


  //#########################################################################
  //# Calculating Column Widths
  /**
   * Set the table's preferred and minimum size by checking the space
   * needed for its contents.
   */
  private void setPreferredSizes()
  {
    final int height = calculateHeight();
    final int width1 = calculateWidth1();

    final TableColumn column0 = getColumnModel().getColumn(0);
    column0.setMinWidth(MINCOLUMNWIDTH0);
    column0.setPreferredWidth(COLUMNWIDTH0);
    column0.setMaxWidth(COLUMNWIDTH0);
    column0.setResizable(false);

    final TableColumn column1 = getColumnModel().getColumn(1);
    column1.setMinWidth(MINCOLUMNWIDTH1);
    column1.setPreferredWidth(width1);

    final int totalwidth = COLUMNWIDTH0 + width1;
    final int minwidth = MINCOLUMNWIDTH0 + MINCOLUMNWIDTH1;
    final Dimension prefsize = new Dimension(totalwidth, height);
    final Dimension minsize = new Dimension(minwidth, 3 * getRowHeight());
    setPreferredSize(prefsize);
    setPreferredScrollableViewportSize(prefsize);
    setMinimumSize(minsize);
  }

  private int calculateHeight()
  {
    return getRowCount() * getRowHeight();
  }

  private int calculateWidth1()
  {
    final TableModel model = getModel();
    final TableCellRenderer renderer = getDefaultRenderer(Object.class);
    final int rows = getRowCount();
    int maxwidth = COLUMNWIDTH1;
    for (int row = 0; row < rows; row++) {
      final Object value = model.getValueAt(row, 1);
      final Component comp =
	renderer.getTableCellRendererComponent
	(this, value, false, false, row, 1);
      final Dimension size = comp.getPreferredSize();
      final int width = size.width;
      if (width > maxwidth) {
	maxwidth = width;
      }
    }
    return maxwidth;
  }


  //#########################################################################
  //# Local Class RendererNoFocus
  private static class RendererNoFocus
    implements TableCellRenderer
  {

    //#######################################################################
    //# Constructors
    private RendererNoFocus(final TableCellRenderer renderer,
			    final boolean focusable)
    {
      mRenderer = renderer;
      mFocusable = focusable;
    }

    //#######################################################################
    //# Interface javax.swing.table.TableCellRenderer
    public Component getTableCellRendererComponent
      (final JTable table, final Object value, final boolean isSelected,
       final boolean hasFocus, final int row, final int column)
    {
      final Component comp =
	mRenderer.getTableCellRendererComponent
	(table, value, isSelected, false, row, column);
      comp.setFocusable(mFocusable);
      return comp;
    }

    //#######################################################################
    //# Data Members
    private final TableCellRenderer mRenderer;
    private final boolean mFocusable;

  }


  //#########################################################################
  //# Local Class MouseHandler
  private class MouseHandler
    extends MouseAdapter
  {

    //#######################################################################
    //# Interfaca java.awt.event.MouseListener
    public void mouseClicked(final MouseEvent event)
    {
      final Point point = event.getPoint();
      if (rowAtPoint(point) < 0) {
        switch (event.getClickCount()) {
        case 1:
          final WatersPopupActionManager manager = mPopupFactory.getMaster();
          final IDEAction action = manager.getDeselectAllAction();
          manager.invokeMouseClickAction(action, event);
          break;
        case 2:
          createEvent();
          break;
        default:
          break;
        }
      }
    }

    public void mousePressed(final MouseEvent event)
    {
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
      if (event.isPopupTrigger()) {
        final Point point = event.getPoint();
        final int row = rowAtPoint(point);
        final EventTableModel model = (EventTableModel) getModel();
        if (row >= 0 && row < model.getRowCount()) {
          final IdentifierSubject clicked = model.getIdentifier(row);
          mPopupFactory.maybeShowPopup(GraphEventPanel.this, event, clicked);
        } else {
          mPopupFactory.maybeShowPopup(GraphEventPanel.this, event, null);
        }
      }
    }

  }


  //#########################################################################
  //# Local Class SelectionListener
  private class SelectionListener
    implements ListSelectionListener
  {

    //#######################################################################
    //# Interface javax.swing.ListSelectionListener
    public void valueChanged(final ListSelectionEvent event)
    {
      final ListSelectionModel selmodel =
	(ListSelectionModel) event.getSource();
      if (isEditing()) {
	// When we are editing a cell with invalid contents,
	// and the user clicks into another row,
	// we sometimes get spurious selection events.
	// The following code undoes their effects.
	final int row = getEditingRow();
	if (row < getRowCount() &&
	    (row != selmodel.getMinSelectionIndex() ||
	     row != selmodel.getMaxSelectionIndex())) {
	  setRowSelectionInterval(row, row);
	  getEditorComponent().requestFocusInWindow();
	}
      } else if (event.getValueIsAdjusting()) {
	// Ignore extra messages ...
      } else {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              fireSelectionChanged();
            }
          });
      }
    }

  }


  //#########################################################################
  //# Local Class DropListener
  private class DropListener extends DropTargetAdapter
  {

    //#######################################################################
    //# Interface java.awt.dnd.DropTargetAdapter
    public void dragOver(final DropTargetDragEvent event)
    {
      final Transferable transferable = event.getTransferable();
      if (!canPaste(transferable)) {
        event.rejectDrag();
      }
    }

    public void drop(final DropTargetDropEvent event)
    {
      try {
        final Transferable transferable = event.getTransferable();
        final List<InsertInfo> info = getInsertInfo(transferable);
        if (info == null) {
          event.rejectDrop();
        } else {
          final Command cmd = new InsertCommand(info, GraphEventPanel.this);
          cmd.execute();
          event.dropComplete(true);
        }
      } catch (final UnsupportedFlavorException exception) {
        event.rejectDrop();
      } catch (final IOException exception) {
        event.rejectDrop();
      }
    }

  }


  //#########################################################################
  //# Local Class GraphEventPanelEventHandler
  //# (Uses inner class to prevent users other than EventTableModel
  //# from calling these methods.)
  private class GraphEventPanelEventHandler
    implements GraphEventHandler, FocusListener
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.GraphEventHandler
    public void addEvent(final IdentifierSubject neo)
    {
      if (isEditing()) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              addEvent(neo);
            }
          });
      } else {
        final Command cmd = new InsertCommand(neo, GraphEventPanel.this);
        cmd.execute();
      }
    }

    public void removeEvent(final IdentifierSubject victim)
    {
      if (isEditing()) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              removeEvent(victim);
            }
          });
      } else {
        final List<IdentifierSubject> list = Collections.singletonList(victim);
        final List<InsertInfo> deletes = getDeletionVictims(list);
        if (deletes != null) {
          final Command cmd = new DeleteCommand(deletes, GraphEventPanel.this);
          final UndoInterface undoer = mRoot.getUndoInterface();
          undoer.executeCommand(cmd);
        }
      }
    }

    public void replaceEvent(final IdentifierSubject old,
                             final IdentifierSubject neo)
    {
      if (isEditing()) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              replaceEvent(old, neo);
            }
          });
      } else {
        final List<ReplaceInfo> replacements = new LinkedList<ReplaceInfo>();
        mReplaceVisitor.addReplacements(old, neo, replacements);
        final boolean undoable = !replacements.isEmpty();
        final ReplaceInfo replacement = new ReplaceInfo(old, neo);
        replacements.add(0, replacement);
        final Command cmd = new ReplaceCommand
          (replacements, GraphEventPanel.this, "Label Editing");
        if (undoable) {
          final UndoInterface undoer = mRoot.getUndoInterface();
          undoer.executeCommand(cmd);
        } else {
          cmd.execute();
        }
      }
    }

    //#######################################################################
    //# Interface java.awt.event.FocusListener
    /**
     * Does nothing.
     */
    public void focusGained(final FocusEvent event)
    {
    }

    /**
     * Called when the editor cell has lost the focus.
     * In this case, we had better check if there is any empty list cell
     * to clean up.
     */
    public void focusLost(final FocusEvent event)
    {
      if (!event.isTemporary()) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              final EventTableModel model = (EventTableModel) getModel();
              model.cleanUpNullItemAtEnd();
            }
          });
      }
    }

  }


  //#########################################################################
  //# Inner Class DeleteVisitor
  private class DeleteVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private void addDeletionVictims(final List<? extends Proxy> items,
                                    final List<InsertInfo> inserts)
    {
      try {
        mItems = new ProxyAccessorHashMapByContents<Proxy>(items);
        mInserts = inserts;
        final SimpleComponentProxy comp = mRoot.getComponent();
        final GraphProxy graph = comp.getGraph();
        graph.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final LabelBlockProxy block = edge.getLabelBlock();
      if (block != null) {
        visitLabelBlockProxy(block);
      }
      return null;
    }

    public Object visitForeachEventProxy(final ForeachEventProxy foreach)
      throws VisitorException
    {
      final ForeachEventSubject subject = (ForeachEventSubject) foreach;
      final ListSubject<? extends ProxySubject> body =
        subject.getBodyModifiable();
      processList(body);
      return null;
    }

    public Object visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      final LabelBlockProxy blocked = graph.getBlockedEvents();
      if (blocked != null) {
        visitLabelBlockProxy(blocked);
      }
      final Collection<NodeProxy> nodes = graph.getNodes();
      visitCollection(nodes);
      final Collection<EdgeProxy> edges = graph.getEdges();
      visitCollection(edges);
      return null;
    }

    public Object visitEventListExpressionProxy
      (final EventListExpressionProxy expr)
      throws VisitorException
    {
      final EventListExpressionSubject subject =
        (EventListExpressionSubject) expr;
      final ListSubject<? extends ProxySubject> eventlist =
        subject.getEventListModifiable();
      processList(eventlist);
      return null;
    }

    public Object visitNodeProxy(final NodeProxy node)
      throws VisitorException
    {
      final PlainEventListProxy props = node.getPropositions();
      return visitPlainEventListProxy(props);
    }

    //#######################################################################
    //# Auxiliary Methods
    private void processList(final ListSubject<? extends ProxySubject> list)
      throws VisitorException
    {
      int pos = 0;
      for (final Proxy proxy : list) {
        if (mItems.containsProxy(proxy)) {
          final ListInsertPosition inspos = new ListInsertPosition(list, pos);
          final InsertInfo insert = new InsertInfo(proxy, inspos);
          mInserts.add(insert);
        } else {
          proxy.acceptVisitor(this);
        }
        pos++;
      }
    }

    //#######################################################################
    //# Data Members
    private ProxyAccessorMap<Proxy> mItems;
    private List<InsertInfo> mInserts;

  }


  //#########################################################################
  //# Inner Class ReplaceVisitor
  private class ReplaceVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private void addReplacements(final IdentifierProxy old,
                                 final IdentifierProxy neo,
                                 final List<ReplaceInfo> replacements)
    {
      try {
        mOldProxy = old;
        mNewProxy = neo;
        mReplacements = replacements;
        final SimpleComponentProxy comp = mRoot.getComponent();
        final GraphProxy graph = comp.getGraph();
        graph.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final LabelBlockProxy block = edge.getLabelBlock();
      if (block != null) {
        visitLabelBlockProxy(block);
      }
      return null;
    }

    public Object visitForeachEventProxy(final ForeachEventProxy foreach)
      throws VisitorException
    {
      final ForeachEventSubject subject = (ForeachEventSubject) foreach;
      final ListSubject<? extends ProxySubject> body =
        subject.getBodyModifiable();
      processList(body);
      return null;
    }

    public Object visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      final LabelBlockProxy blocked = graph.getBlockedEvents();
      if (blocked != null) {
        visitLabelBlockProxy(blocked);
      }
      final Collection<NodeProxy> nodes = graph.getNodes();
      visitCollection(nodes);
      final Collection<EdgeProxy> edges = graph.getEdges();
      visitCollection(edges);
      return null;
    }

    public Object visitEventListExpressionProxy
      (final EventListExpressionProxy expr)
      throws VisitorException
    {
      final EventListExpressionSubject subject =
        (EventListExpressionSubject) expr;
      final ListSubject<? extends ProxySubject> eventlist =
        subject.getEventListModifiable();
      processList(eventlist);
      return null;
    }

    public Object visitNodeProxy(final NodeProxy node)
      throws VisitorException
    {
      final PlainEventListProxy props = node.getPropositions();
      return visitPlainEventListProxy(props);
    }

    //#######################################################################
    //# Auxiliary Methods
    private void processList(final ListSubject<? extends ProxySubject> list)
      throws VisitorException
    {
      int pos = 0;
      for (final Proxy proxy : list) {
        if (proxy instanceof IdentifierProxy) {
          if (proxy.equalsByContents(mOldProxy)) {
            final ModuleProxyCloner cloner =
              ModuleSubjectFactory.getCloningInstance();
            final Proxy cloned = cloner.getClone(mNewProxy);
            final ListInsertPosition inspos =
              new ListInsertPosition(list, pos);
            final ReplaceInfo replacement =
              new ReplaceInfo(proxy, cloned, inspos);
            mReplacements.add(replacement);
          }
        } else {
          proxy.acceptVisitor(this);
        }
        pos++;
      }
    }

    //#######################################################################
    //# Data Members
    private IdentifierProxy mOldProxy;
    private IdentifierProxy mNewProxy;
    private List<ReplaceInfo> mReplacements;

  }


  //#########################################################################
  //# Data Members
  private final EditorWindowInterface mRoot;
  private final PopupFactory mPopupFactory;
  private final DeleteVisitor mDeleteVisitor;
  private final ReplaceVisitor mReplaceVisitor;

  private List<Observer> mObservers;


  //#########################################################################
  //# Class Constants
  private static final int COLUMNWIDTH0 = 24;
  private static final int MINCOLUMNWIDTH0 = 20;
  private static final int COLUMNWIDTH1 = 96;
  private static final int MINCOLUMNWIDTH1 = 24;

}
