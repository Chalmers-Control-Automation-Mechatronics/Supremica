//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventDeclListView
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.transfer.EventDeclTransferable;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * The list view panel that shows the list of event declarations.  It
 * supports creating, editing, and some modifications of event
 * declarations via a popup menu (not yet fully implemented), editing of
 * event declarations by means of double click, and drag&amp;drop to label
 * transitions in graphs.
 *
 * @author Simon Ware, Robi Malik
 */

public class EventDeclListView
  extends JList
  implements SelectionOwner, FocusListener,
             DragGestureListener, ListSelectionListener
{

  //#########################################################################
  //# Constructors
  public EventDeclListView(final ModuleWindowInterface root,
                           final WatersPopupActionManager manager)
  {
    mRoot = root;
    mPopupFactory = new EventDeclListPopupFactory(manager);
    mDeleteVisitor = new EventDeclDeleteVisitor(root);
    mObservers = null;

    final ModuleContext context = root.getModuleContext();
    final ModuleSubject module = root.getModuleSubject();
    final ListSubject<EventDeclSubject> events =
      module.getEventDeclListModifiable();
    mModel = new IndexedListModel<EventDeclSubject>(events);
    setModel(mModel);
    setCellRenderer(new EventListCell(context));

    setBackground(EditorColor.BACKGROUNDCOLOR);
    setSelectionForeground(EditorColor.TEXTCOLOR);
    setSelectionBackground(EditorColor.BACKGROUND_NOTFOCUSSED);
    addFocusListener(this);
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    final MouseListener handler = new EventDeclMouseListener();
    addMouseListener(handler);
    addListSelectionListener(this);
    manager.installCutCopyPasteActions(this);

    final DragSource dragsource = DragSource.getDefaultDragSource();
    dragsource.createDefaultDragGestureRecognizer
      (this, DnDConstants.ACTION_COPY, this);
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
    final int size = mModel.getSize();
    if (isSelectionEmpty()) {
      return size > 0;
    } else if (getMinSelectionIndex() > 0 ||
               getMaxSelectionIndex() + 1 < size) {
      return true;
    } else {
      for (int index = 0; index < size; index++) {
        if (!isSelectedIndex(index)) {
          return true;
        }
      }
      return false;
    }
  }

  public boolean isSelected(final Proxy proxy)
  {
    final int index = mModel.indexOf(proxy);
    return isSelectedIndex(index);
  }

  public List<EventDeclSubject> getCurrentSelection()
  {
    final List<EventDeclSubject> result = new LinkedList<EventDeclSubject>();
    final ListSelectionModel selection = getSelectionModel();
    for (final EventDeclSubject decl : mModel.getSelectedSubjects(selection)) {
      result.add(decl);
    }
    return result;
  }

  public List<EventDeclSubject> getAllSelectableItems()
  {
    final int size = mModel.getSize();
    final List<EventDeclSubject> result =
      new ArrayList<EventDeclSubject>(size);
    for (int index = 0; index < size; index++) {
      final EventDeclSubject decl = mModel.getElementAt(index);
      result.add(decl);
    }
    return result;
  }

  public EventDeclSubject getSelectionAnchor()
  {
    final int size = mModel.getSize();
    final int index = getAnchorSelectionIndex();
    if (index >= 0 && index < size) {
      return mModel.getElementAt(index);
    } else {
      return null;
    }
  }

  public void clearSelection(final boolean propagate)
  {
    clearSelection();
  }

  public Proxy getSelectableAncestor(final Proxy item)
  {
    if (item instanceof EventDeclSubject && mModel.contains(item)) {
      return item;
    } else {
      return null;
    }
  }

  public void replaceSelection(final List<? extends Proxy> items)
  {
    clearSelection();
    addToSelection(items);
  }

  public void addToSelection(final List<? extends Proxy> items)
  {
    int remaining = items.size();
    switch (remaining) {
    case 0:
      break;
    case 1:
      final Proxy proxy = items.iterator().next();
      final int index1 = mModel.indexOf(proxy);
      if (index1 >= 0) {
        addSelectionInterval(index1, index1);
      }
      break;
    default:
      final int size = mModel.getSize();
      final Set<Proxy> added = new HashSet<Proxy>(items);
      int start = -1;
      for (int index = 0; index < size; index++) {
        final EventDeclSubject decl = mModel.getElementAt(index);
        if (added.contains(decl)) {
          if (start < 0) {
            start = index;
          }
          if (--remaining == 0) {
            addSelectionInterval(start, index);
            return;
          }
        } else if (start >= 0 && !isSelectedIndex(index)) {
          addSelectionInterval(start, index - 1);
          start = -1;
        }
      }
      if (start >= 0) {
        addSelectionInterval(start, size - 1);
      }
      break;
    }
  }

  public void removeFromSelection(final List<? extends Proxy> items)
  {
    int remaining = items.size();
    switch (remaining) {
    case 0:
      break;
    case 1:
      final Proxy proxy = items.iterator().next();
      final int index1 = mModel.indexOf(proxy);
      if (index1 >= 0) {
        removeSelectionInterval(index1, index1);
      }
      break;
    default:
      final int size = mModel.getSize();
      final Set<Proxy> removed = new HashSet<Proxy>(items);
      int start = -1;
      for (int index = 0; index < size; index++) {
        final EventDeclSubject decl = mModel.getElementAt(index);
        if (removed.contains(decl)) {
          if (start < 0) {
            start = index;
          }
          if (--remaining == 0) {
            removeSelectionInterval(start, index);
            return;
          }
        } else if (start >= 0 && isSelectedIndex(index)) {
          removeSelectionInterval(start, index - 1);
          start = -1;
        }
      }
      if (start >= 0) {
        removeSelectionInterval(start, size - 1);
      }
      break;
    }
  }

  public Proxy getInsertPosition(final Proxy proxy)
  {
    return proxy;
  }

  public void insertCreatedItem(final Proxy proxy, final Object insobj)
  {
    final ModuleSubject module = mRoot.getModuleSubject();
    final List<EventDeclSubject> events = module.getEventDeclListModifiable();
    final EventDeclSubject decl = (EventDeclSubject) proxy;
    events.add(decl);
  }

  public boolean canCopy(final List<? extends Proxy> items)
  {
    return !items.isEmpty();
  }

  public Transferable createTransferable(final List<? extends Proxy> items)
  {
    final List<EventDeclSubject> decls = Casting.toList(items);
    return new EventDeclTransferable(decls);
  }

  public boolean canPaste(final Transferable transferable)
  {
    return transferable.isDataFlavorSupported(WatersDataFlavor.EVENTDECL_LIST);
  }

  public List<InsertInfo> getInsertInfo(Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    final ModuleContext context = mRoot.getModuleContext();
    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    final List<Proxy> data = (List<Proxy>) transferable.getTransferData
      (WatersDataFlavor.EVENTDECL_LIST);
    final int size = data.size();
    final Set<String> names = new HashSet<String>(size);
    final List<InsertInfo> result = new ArrayList<InsertInfo>(size);
    for (final Proxy proxy : data) {
      final EventDeclSubject decl = (EventDeclSubject) cloner.getClone(proxy);
      final String name = decl.getName();
      final String unique = context.getPastedEventName(name, names);
      final SimpleIdentifierSubject ident =
        new SimpleIdentifierSubject(unique);
      decl.setIdentifier(ident);
      final InsertInfo info = new InsertInfo(decl);
      result.add(info);
      names.add(unique);
    }
    return result;
  }

  public boolean canDelete(final List<? extends Proxy> items)
  {
    return !items.isEmpty();
  }

  public List<InsertInfo> getDeletionVictims(final List<? extends Proxy> items)
  {
    final List<EventDeclSubject> decls = Casting.toList(items);
    return mDeleteVisitor.getDeletionVictims(decls);
  }

  public void insertItems(List<InsertInfo> inserts)
  {
    mDeleteVisitor.insertItems(inserts);
  }

  public void deleteItems(List<InsertInfo> deletes)
  {
    mDeleteVisitor.deleteItems(deletes);
  }

  public void scrollToVisible(final List<? extends Proxy> list)
  {
    if (!list.isEmpty()) {
      int min = mModel.getSize() - 1;
      int max = 0;
      for (final Proxy proxy : list) {
        final int index = mModel.indexOf(proxy);
        if (index >= 0) {
          if (index < min) {
            min = index;
          }
          if (index > max) {
            max = index;
          }
        }
      }
      final Rectangle rect = getCellBounds(min, max);
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
    mModel.dispose();
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


  //#########################################################################
  //# Interface java.awt.dnd.DragGestureListener
  public void dragGestureRecognized(final DragGestureEvent event)
  {
    final int index = locationToIndex(event.getDragOrigin());
    final List<EventDeclSubject> data;
    if (isSelectedIndex(index)) {
      data = getCurrentSelection();
    } else if (index >= 0 && index < mModel.getSize()) {
      final EventDeclSubject decl = mModel.getElementAt(index);
      data = Collections.singletonList(decl);
    } else {
      return;
    }
    final Transferable trans = createTransferable(data);
    try {
      event.startDrag(DragSource.DefaultCopyDrop, trans);
    } catch (InvalidDnDOperationException exception) {
      throw new IllegalArgumentException(exception);
    }
  }
  

  //#########################################################################
  //# Interface javax.swing.event.ListSelectionListener
  public void valueChanged(final ListSelectionEvent event)
  {
    // Why can't the new selection be read immediately ???
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          fireSelectionChanged();
        }
      });
  }


  //#########################################################################
  //# Commands
  private void doSetEventsKind(final Iterable<EventDeclSubject> decls,
                               final EventKind kind)
  {
    final CompoundCommand compound = new CompoundCommand("Change Events Kind");
    for (final EventDeclSubject decl : decls) {
      if (decl.getKind() != kind) {
        final EventDeclSubject template = decl.clone();
        template.setKind(kind);
        final Command command = new EditCommand(decl, template);
        compound.addCommand(command);
      }
    }
    if (!compound.isEmpty()) {
      compound.end();
      mRoot.getUndoInterface().executeCommand(compound);
    }
  }

  private void doSetEventsObservable(final Iterable<EventDeclSubject> decls,
                                     final boolean observable)
  {
    final CompoundCommand compound =
      new CompoundCommand("Change Events Observability");
    for (final EventDeclSubject decl : decls) {
      if (decl.isObservable() != observable) {
        final EventDeclSubject template = decl.clone();
        template.setObservable(observable);
        final Command command = new EditCommand(decl, template);
        compound.addCommand(command);
      }
    }
    if (!compound.isEmpty()) {
      compound.end();
      mRoot.getUndoInterface().executeCommand(compound);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventDeclSubject getClickedItem(final MouseEvent event)
  {
    final Point point = event.getPoint();
    final int index = locationToIndex(point);
    if (index >= 0 && index < mModel.getSize()) {
      return mModel.getElementAt(index);
    } else {
      return null;
    }
  }

  private void fireSelectionChanged()
  {
    final EditorChangedEvent event = new SelectionChangedEvent(this);
    fireEditorChangedEvent(event);
  }


  //#########################################################################
  //# Inner Class EventDeclPopup
  private class EventDeclPopup extends JPopupMenu
  {
    //#######################################################################
    //# Constructor
    EventDeclPopup(final MouseEvent event)
    {
      final Iterable<EventDeclSubject> decls;
      final Point point = event.getPoint();
      final int index = locationToIndex(point);
      if (index < 0 || index >= mModel.getSize())
      {
        decls = new ArrayList<EventDeclSubject>();
      } else
      {
        final EventDeclSubject clicked = mModel.getElementAt(index);
        decls = Collections.singletonList(clicked);
      }
      int declcount = 0;
      int kindcount = 0;
      EventKind kind = null;
      int obscount = 0;
      boolean observable = true;
      for (final EventDeclSubject decl : decls) {
        declcount++;
        switch (kindcount) {
        case 0:
          kind = decl.getKind();
          kindcount = 1;
          break;
        case 1:
          if (kind != decl.getKind()) {
            kindcount = 2;
            kind = null;
          }
          break;
        default:
          break;
        }
        switch (obscount) {
        case 0:
          observable = decl.isObservable();
          obscount = 1;
          break;
        case 1:
          if (observable != decl.isObservable()) {
            obscount = 2;
          }
          break;
        default:
          break;
        }
      }

      final JMenu kindMenu = new JMenu("Set kind");
      add(kindMenu);
      for (final EventKind itemkind : EventKind.values()) {
        final String title = getTitle(itemkind);
        final boolean selected = kindcount == 1 && itemkind == kind;
        final JRadioButtonMenuItem item =
          new JRadioButtonMenuItem(title, selected);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
              doSetEventsKind(decls, itemkind);
            }
          });
        kindMenu.add(item);
      }
      kindMenu.addSeparator();
      final JRadioButtonMenuItem observableItem =
        new JRadioButtonMenuItem("Observable", obscount == 1 && observable);
      observableItem.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event) {
            doSetEventsObservable(decls, true);
          }
        });
      kindMenu.add(observableItem);
      final JRadioButtonMenuItem unobservableItem =
        new JRadioButtonMenuItem("Unobservable", obscount == 1 && !observable);
      unobservableItem.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event) {
            doSetEventsObservable(decls, false);
          }
        });
      kindMenu.add(unobservableItem);
    }

    //#######################################################################
    //# Auxiliary Methods
    private String getTitle(final EventKind kind)
    {
      final String text = kind.toString();
      final int len = text.length();
      final StringBuffer buffer = new StringBuffer(len);
      buffer.append(text.charAt(0));
      for (int i = 1; i < len; i++) {
        final char upper = text.charAt(i);
        final char lower = Character.toLowerCase(upper);
        buffer.append(lower);
      }
      return buffer.toString();
    }
  }


  //#########################################################################
  //# Inner Class EventDeclMouseListener
  /**
   * A simple mouse listener to trigger opening the event declaration
   * editor dialog by double-click, and to trigger a popup menu.
   */
  private class EventDeclMouseListener extends MouseAdapter
  {
    //#######################################################################
    //# Interface java.awt.MouseListener
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1 &&
          event.getClickCount() == 2) {
        final Point point = event.getPoint();
        final int index = locationToIndex(point);
        if (index >= 0 && index < mModel.getSize()) {
          final EventDeclSubject decl = mModel.getElementAt(index);
          new EventEditorDialog(mRoot, decl);
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
      final Point point = event.getPoint();
      final int index = locationToIndex(point);
      final EventDeclSubject clicked;
      if (index >= 0 && index < mModel.getSize()) {
        clicked = mModel.getElementAt(index);
      } else {
        clicked = null;
      }
      mPopupFactory.maybeShowPopup(EventDeclListView.this, event, clicked);
    }

  }


  //#########################################################################
  //# Data Members
  private final IndexedListModel<EventDeclSubject> mModel;
  private final ModuleWindowInterface mRoot;
  private final PopupFactory mPopupFactory;
  private final EventDeclDeleteVisitor mDeleteVisitor;

  private List<Observer> mObservers;

}
