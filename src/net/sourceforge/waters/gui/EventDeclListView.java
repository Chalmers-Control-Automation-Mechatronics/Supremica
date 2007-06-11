//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventDeclListView
//###########################################################################
//# $Id: EventDeclListView.java,v 1.4 2007-06-11 05:59:18 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.Collection;
import java.util.ArrayList;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
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
import javax.swing.JList;
import javax.swing.ListModel;

import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
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
        // not yet implemented ...
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
