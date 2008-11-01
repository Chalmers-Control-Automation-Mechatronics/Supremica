//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.observer
//# CLASS:   UndoRedoEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.observer;

import net.sourceforge.waters.gui.command.UndoInterface;


/**
 * <P>A notification sent by the undo manager.</P>
 *
 * <P>This notification is sent to notify observers of a change of the
 * the undo manager's state, caused by invoking issuing an 'undo' or
 * 'redo' action.</P>
 *
 * @author Simon Ware, Robi Malik
 */

public class UndoRedoEvent
  extends EditorChangedEvent
{

  //#########################################################################
  //# Constructors
  public UndoRedoEvent(final UndoInterface source)
  {
    super(source);
  }

	
  //#########################################################################
  //# Simple Access
  public UndoInterface getSource()
  {
    return (UndoInterface) super.getSource();
  }

  public EditorChangedEvent.Kind getKind()
  {
    return EditorChangedEvent.Kind.UNDOREDO;
  }

}