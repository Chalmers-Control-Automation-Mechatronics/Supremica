//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventDeclListView
//###########################################################################
//# $Id: EventDeclListView.java,v 1.2 2006-12-11 02:40:44 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.Collection;
import java.util.ArrayList;
import java.awt.datatransfer.Transferable;
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


public class EventDeclListView
  extends JList
{

  //#########################################################################
  //# Constructors
  public EventDeclListView(final ModuleSubject module)
  {
    final IndexedListSubject<EventDeclSubject> events =
      module.getEventDeclListModifiable();
    final ListModel model = new IndexedListModel<EventDeclSubject>(events);
    setModel(model);
    setCellRenderer(new EventListCell());

    final DragSource dragsource = DragSource.getDefaultDragSource();
    final DragGestureListener glistener = new EventDeclDragGestureListener();
    final DragSourceListener slistener = new EventDeclDragSourceListener();
    dragsource.createDefaultDragGestureRecognizer
      (this, DRAG_ACTION, glistener);
    dragsource.addDragSourceListener(slistener);
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
      final Collection<IdentifierSubject> idents = new ArrayList<IdentifierSubject>(values.length);
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
  //# Class Constants
  private static final int DRAG_ACTION = DnDConstants.ACTION_COPY;

}
