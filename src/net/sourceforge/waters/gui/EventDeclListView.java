//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventDeclListView
//###########################################################################
//# $Id: EventDeclListView.java,v 1.7 2007-09-25 18:22:37 knut Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.DeleteEventDeclCommand;
import net.sourceforge.waters.gui.command.EditEventDeclCommand;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * The list view panel that shows the list of event declarations.  It
 * supports creating, editing, and some modifications of event
 * declarations via a popup menu (not yet fully implemented), editing of
 * event declarations by means of double click, and drag&amp;drop to label
 * transitions in graphs.
 *
 * @todo Selection handling (must be undoable) not yet implemented!
 *
 * @author Simon Ware, Robi Malik
 */

public class EventDeclListView
  extends JList
{

  //#########################################################################
  //# Constructors
  public EventDeclListView(final ModuleWindowInterface root)
  {
    mRoot = root;
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    final ModuleSubject module = root.getModuleSubject();
    final IndexedListSubject<EventDeclSubject> events =
      module.getEventDeclListModifiable();
    mModel = new IndexedListModel<EventDeclSubject>(events);
    setModel(mModel);
    setCellRenderer(new EventListCell());

    final MouseListener handler = new EventDeclMouseListener();
    addMouseListener(handler);

    final DragSource dragsource = DragSource.getDefaultDragSource();
    final DragGestureListener glistener = new EventDeclDragGestureListener();
    final DragSourceListener slistener = new EventDeclDragSourceListener();
    dragsource.createDefaultDragGestureRecognizer
      (this, DRAG_ACTION, glistener);
    dragsource.addDragSourceListener(slistener);
  }


  //#########################################################################
  //# Commands
  private void doCreateEvent()
  {
    new EventEditorDialog(mRoot, false);
  }

  private void doDeleteEvents(final Iterable<EventDeclSubject> victims)
  {
    if (victims.iterator().hasNext()) {
      final ModuleSubject module = mRoot.getModuleSubject();

      // Make sure that the event is not currently in use
      for (EventDeclSubject event : victims)
      {

		  // First check that the event is not in use
		  ListSubject<AbstractSubject> 	componentList = module.getComponentListModifiable();
		  for (AbstractSubject subject : componentList)
		  {
			if (subject instanceof SimpleComponentSubject)
			{
			  System.err.println("Checking SimpleComponentSubject");
			  GraphSubject graphSubject = ((SimpleComponentSubject)subject).getGraph();
			  Collection<EdgeProxy> edges = graphSubject.getEdges();
			  for (EdgeProxy edge : edges)
			  {
				LabelBlockProxy labelBlock = edge.getLabelBlock();
				List<Proxy> eventList = labelBlock.getEventList();
				for (Proxy proxy : eventList)
				{
					System.err.println("Checking proxy");
					if (proxy instanceof IdentifierProxy)
					{
						if (event.getName().equals(((IdentifierProxy)proxy).getName()))
						{
						  JOptionPane.showMessageDialog(mRoot.getRootWindow(), "Event " + event.getName() + " is used in component " + ((SimpleComponentSubject)subject).getName(),
														"Event in use!",
														JDialog.DO_NOTHING_ON_CLOSE);
						  return;
						}
					}
				}
			  }
			}
		  }
		}

      final Command command = new DeleteEventDeclCommand(victims, module);
      mRoot.getUndoInterface().executeCommand(command);
    }
  }

  private void doSetEventsKind(final Iterable<EventDeclSubject> decls,
                               final EventKind kind)
  {
    final CompoundCommand compound = new CompoundCommand("Change Events Kind");
    for (final EventDeclSubject decl : decls) {
      if (decl.getKind() != kind) {
        final EventDeclSubject template = decl.clone();
        template.setKind(kind);
        final Command command = new EditEventDeclCommand(decl, template);
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
        final Command command = new EditEventDeclCommand(decl, template);
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


  //#########################################################################
  //# Inner Class EventDeclPopup
  private class EventDeclPopup extends JPopupMenu
  {
    //#######################################################################
    //# Constructor
    EventDeclPopup(final MouseEvent event)
    {
      final Iterable<EventDeclSubject> decls;
      final ListSelectionModel selection =
        EventDeclListView.this.getSelectionModel();
      final Point point = event.getPoint();
      final int index = locationToIndex(point);
      if (index < 0 ||
          index >= mModel.getSize() ||
          selection.isSelectedIndex(index)) {
        decls = mModel.getSelectedSubjects(selection);
      } else {
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

      final JMenuItem newItem = new JMenuItem("Create Event ...");
      newItem.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event) {
            doCreateEvent();
          }
        });
      add(newItem);
      final String deltext = declcount == 1 ? "Delete Event" : "Delete Events";
      final JMenuItem deleteItem = new JMenuItem(deltext);
      deleteItem.setEnabled(declcount > 0);
      deleteItem.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event) {
            doDeleteEvents(decls);
          }
        });
      add(deleteItem);
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
      if (event.isPopupTrigger()) {
        final JPopupMenu popup = new EventDeclPopup(event);
        final int x = event.getX();
        final int y = event.getY();
        popup.show(EventDeclListView.this, x, y);
      }
    }

  }


  //#########################################################################
  //# Inner Class EventDeclDragGestureListener
  private class EventDeclDragGestureListener implements DragGestureListener
  {
    //#######################################################################
    //# Interface java.awt.dnd.DragGestureListener
    public void dragGestureRecognized(final DragGestureEvent event)
    {
      final int row = locationToIndex(event.getDragOrigin());
      if (row < 0) {
        return;
      }
      final Object[] values = getSelectedValues();
      if (values.length == 0) {
        return;
      }
      final Collection<IdentifierSubject> idents =
        new ArrayList<IdentifierSubject>(values.length);
      EventType e = EventType.UNKNOWN;
      for(int i = 0; i < values.length; i++)
      {
        final EventDeclSubject decl = (EventDeclSubject) values[i];
        switch (e) {
          case UNKNOWN:
            switch (decl.getKind()) {
              case PROPOSITION:
                e = EventType.NODE_EVENTS;
                break;
              case CONTROLLABLE:
                e = EventType.EDGE_EVENTS;
                break;
              case UNCONTROLLABLE:
                e = EventType.EDGE_EVENTS;
                break;
              default:
                break;
            }
            break;
          case EDGE_EVENTS:
            switch (decl.getKind()) {
              case PROPOSITION:
                e = EventType.BOTH;
                break;
              default:
                break;
            }
            break;
          case NODE_EVENTS:
            switch (decl.getKind()) {
              case CONTROLLABLE:
                e = EventType.BOTH;
                break;
              case UNCONTROLLABLE:
                e = EventType.BOTH;
                break;
              default:
                break;
            }
            break;
          default:
            break;
        }
        idents.add(new SimpleIdentifierSubject(decl.getName()));
      }
      final Transferable trans = new IdentifierTransfer(idents, e);
      try {
        event.startDrag(DragSource.DefaultCopyDrop, trans);
      } catch (InvalidDnDOperationException exception) {
        throw new IllegalArgumentException(exception);
      }
    }

  }


  //#########################################################################
  //# Inner Class EventDeclDragSourceListener
  private class EventDeclDragSourceListener extends DragSourceAdapter
  {
    public void dragOver(final DragSourceDragEvent event)
    {
      if (event.getTargetActions() == DnDConstants.ACTION_COPY) {
        event.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
      } else {
        event.getDragSourceContext().setCursor(DragSource.DefaultCopyNoDrop);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final IndexedListModel<EventDeclSubject> mModel;
  private final ModuleWindowInterface mRoot;


  //#########################################################################
  //# Class Constants
  private static final int DRAG_ACTION = DnDConstants.ACTION_COPY;

}
