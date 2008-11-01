//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.observer
//# CLASS:   ClipboardChangedEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.observer;


/**
 * <P>A notification sent by a component if its has changed the system
 * clipboard contents.</P>
 *
 * @author Robi Malik
 */

public class ClipboardChangedEvent
  extends EditorChangedEvent
{

  //#########################################################################
  //# Constructors
  public ClipboardChangedEvent(final Object source)
  {
    super(source);
  }

	
  //#########################################################################
  //# Simple Access
  public EditorChangedEvent.Kind getKind()
  {
    return EditorChangedEvent.Kind.CLIPBOARD_CHANGED;
  }

}
