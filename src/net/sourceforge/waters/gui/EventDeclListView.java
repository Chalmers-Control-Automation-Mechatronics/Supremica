//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventDeclListView
//###########################################################################
//# $Id: EventDeclListView.java,v 1.1 2006-09-21 16:42:13 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

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
      final ListModel model = getModel();
      final EventDeclSubject decl = (EventDeclSubject) model.getElementAt(row);
      final String name = decl.getName();
      final EventKind kind = decl.getKind();
      final IdentifierSubject ident = new SimpleIdentifierSubject(name);
      final Transferable trans = new IdentifierTransfer(ident, kind); 
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
