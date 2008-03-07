//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   WatersDragSourceListener
//###########################################################################
//# $Id: WatersDragSourceListener.java,v 1.1 2008-03-07 04:11:02 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;


/**
 * A simple implementation of the {@link DragSourceListener} interface
 * that displays a 'copy' or 'no-copy' cursor depending on whether
 * the current state of the drag operation allows a copy operation or not.
 *
 * This drag source listener is instantiated only a single time when the
 * application is started. It then uniformly handles all cursor changes for
 * all drag&amp;drop operations for all panels. Individual panels do not
 * have to instantiate or register this class.
 *
 * @author Robi Malik
 */

public class WatersDragSourceListener
  implements DragSourceListener
{

  //#########################################################################
  //# Initialisation
  /**
   * Creates an instance of this drag source listener and connects it
   * to the default drag source.
   */
  public static void setup()
  {
    final DragSource dragsource = DragSource.getDefaultDragSource();
    final DragSourceListener dragger = new WatersDragSourceListener();
    dragsource.addDragSourceListener(dragger);
  }
  

  //#########################################################################
  //# Dummy Constructor to Prevent Instantiation
  private WatersDragSourceListener()
  {
  }


  //#########################################################################
  //# Interface java.awt.dnd.DragSourceListener
  public void dragDropEnd(final DragSourceDropEvent event)
  {
  }

  public void dragEnter(final DragSourceDragEvent event)
  {
    setCursor(event);
  }

  public void dragExit(final DragSourceEvent event)
  {
    setRejectingCursor(event);
  }

  public void dragOver(final DragSourceDragEvent event)
  {
    setCursor(event);
  }
  
  public void dropActionChanged(final DragSourceDragEvent event)
  {
    setCursor(event);
  } 


  //#########################################################################
  //# Auxiliary Methods
  private void setCursor(final DragSourceDragEvent event)
  {
    if ((event.getTargetActions() & DnDConstants.ACTION_COPY) != 0) {
      setAcceptingCursor(event);
    } else {
      setRejectingCursor(event);
    }
  }

  private void setAcceptingCursor(final DragSourceEvent event)
  {
    final DragSourceContext context = event.getDragSourceContext();
    context.setCursor(DragSource.DefaultCopyDrop);
  }

  private void setRejectingCursor(final DragSourceEvent event)
  {
    final DragSourceContext context = event.getDragSourceContext();
    context.setCursor(DragSource.DefaultCopyNoDrop);
  }

}
