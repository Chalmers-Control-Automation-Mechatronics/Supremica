//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.dialog.EventDeclDeleteVisitor;
import net.sourceforge.waters.gui.dialog.EventDeclEditorDialog;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
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
  extends JList<EventDeclSubject>
  implements SelectionOwner, FocusListener, ListSelectionListener
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
    setTransferHandler(new EventDeclListTransferHandler());
    setDragEnabled(true);
    setDropMode(DropMode.INSERT);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.transfer.SelectionOwner
  @Override
  public UndoInterface getUndoInterface(final Action action)
  {
    return mRoot.getUndoInterface();
  }

  @Override
  public boolean hasNonEmptySelection()
  {
    return !isSelectionEmpty();
  }

  @Override
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

  @Override
  public boolean isSelected(final Proxy proxy)
  {
    final int index = mModel.indexOf(proxy);
    return isSelectedIndex(index);
  }

  @Override
  public List<EventDeclSubject> getCurrentSelection()
  {
    final List<EventDeclSubject> result = new LinkedList<EventDeclSubject>();
    final ListSelectionModel selection = getSelectionModel();
    for (final EventDeclSubject decl : mModel.getSelectedSubjects(selection)) {
      result.add(decl);
    }
    return result;
  }

  @Override
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

  @Override
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

  @Override
  public void clearSelection(final boolean propagate)
  {
    clearSelection();
  }

  @Override
  public Proxy getSelectableAncestor(final Proxy item)
  {
    if (item instanceof EventDeclSubject && mModel.contains(item)) {
      return item;
    } else {
      return null;
    }
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

  @Override
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

  @Override
  public boolean canPaste(final Transferable transferable)
  {
    return transferable.isDataFlavorSupported(WatersDataFlavor.EVENT_DECL);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InsertInfo> getInsertInfo(final Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    final ModuleContext context = mRoot.getModuleContext();
    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    final List<Proxy> data = (List<Proxy>) transferable.getTransferData
      (WatersDataFlavor.EVENT_DECL);
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

  @Override
  public boolean canDelete(final List<? extends Proxy> items)
  {
    return !items.isEmpty();
  }

  @Override
  public List<InsertInfo> getDeletionVictims(final List<? extends Proxy> items)
  {
    @SuppressWarnings("unchecked")
    final List<EventDeclSubject> decls = (List<EventDeclSubject>) items;
    return mDeleteVisitor.getDeletionVictims(decls, "delete");
  }

  @Override
  public void insertItems(final List<InsertInfo> inserts)
  {
    mDeleteVisitor.insertItems(inserts);
  }

  @Override
  public void deleteItems(final List<InsertInfo> deletes)
  {
    mDeleteVisitor.deleteItems(deletes);
  }

  @Override
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

  @Override
  public void activate()
  {
    if (!isFocusOwner()) {
      mRoot.showPanel(this);
      requestFocusInWindow();
    }
  }

  @Override
  public void close()
  {
    mModel.dispose();
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
    mObservers.remove(observer);
    if (mObservers.isEmpty()) {
      mObservers = null;
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
  //# Interface java.awt.event.FocusListener
  @Override
  public void focusGained(final FocusEvent event)
  {
    if (!event.isTemporary()) {
      setSelectionBackground(EditorColor.BACKGROUND_FOCUSSED);
    }
  }

  @Override
  public void focusLost(final FocusEvent event)
  {
    if (!event.isTemporary()) {
      setSelectionBackground(EditorColor.BACKGROUND_NOTFOCUSSED);
    }
  }

  //#########################################################################
  //# Interface javax.swing.event.ListSelectionListener
  @Override
  public void valueChanged(final ListSelectionEvent event)
  {
    // Why can't the new selection be read immediately ???
    SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          fireSelectionChanged();
        }
      });
  }


  //#########################################################################
  //# Commands
  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
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
  @SuppressWarnings("unused")
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
  //# Inner Class EventDeclMouseListener
  /**
   * A simple mouse listener to trigger opening the event declaration
   * editor dialog by double-click, and to trigger a popup menu.
   */
  private class EventDeclMouseListener extends MouseAdapter
  {
    //#######################################################################
    //# Interface java.awt.MouseListener
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1 &&
          event.getClickCount() == 2) {
        final Point point = event.getPoint();
        final int index = locationToIndex(point);
        if (index >= 0 && index < mModel.getSize()) {
          final EventDeclSubject decl = mModel.getElementAt(index);
          new EventDeclEditorDialog(mRoot, decl);
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
  //# Inner Class EventDeclListTransferHandler
  private class EventDeclListTransferHandler extends TransferHandler
  {

    @Override
    public int getSourceActions(final JComponent c)
    {
      return COPY;
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

    }

    @Override
    public boolean canImport(final TransferSupport support)
    {
      return false;
    }

    @Override
    public boolean importData(final TransferSupport support)
    {
      return false;
    }

    private static final long serialVersionUID = 1L;
  }


  //#########################################################################
  //# Data Members
  private final IndexedListModel<EventDeclSubject> mModel;
  private final ModuleWindowInterface mRoot;
  private final PopupFactory mPopupFactory;
  private final EventDeclDeleteVisitor mDeleteVisitor;
  private List<Observer> mObservers;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
