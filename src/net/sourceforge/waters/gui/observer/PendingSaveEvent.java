//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.observer
//# CLASS:   PendingSaveEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.observer;


/**
 * A notification sent by the IDE to indicate an impending save operation.
 * This event is sent when the user has requested to save the contents
 * of the current container (module), but before the save takes place.
 * It is to be used by GUI components to write any uncommitted changes from
 * GUI widgets into the model data structures.
 *
 * @author Robi Malik
 */

public class PendingSaveEvent
  extends EditorChangedEvent
{

  //#########################################################################
  //# Constructors
  public PendingSaveEvent(final Object source)
  {
    super(source);
  }


  //#########################################################################
  //# Simple Access
  public EditorChangedEvent.Kind getKind()
  {
    return EditorChangedEvent.Kind.PENDING_SAVE;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
