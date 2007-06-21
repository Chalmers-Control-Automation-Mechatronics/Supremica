//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.observer
//# CLASS:   ToolbarChangedEvent
//###########################################################################
//# $Id: ToolbarChangedEvent.java,v 1.2 2007-06-21 11:16:23 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.observer;

import net.sourceforge.waters.gui.ControlledToolbar;


/**
 * <P>A notification sent by the IDE to inform its observers that its
 * toolbar state has changed.</P>
 *
 * <P>This notification is sent to notify observers of a change of the
 * drawing tool in the IDE's graph drawing toolbar.</P>
 *
 * <P><STRONG>Todo:</STRONG> Support for other toolbars may be added in the
 * future.</P>
 *
 * @author Simon Ware, Robi Malik
 */

public class ToolbarChangedEvent
  extends EditorChangedEvent
{

  //#########################################################################
  //# Constructors
  public ToolbarChangedEvent(final ControlledToolbar source)
  {
    super(source);
  }

	
  //#########################################################################
  //# Simple Access
  public ControlledToolbar getSource()
  {
    return (ControlledToolbar) super.getSource();
  }

  public EditorChangedEvent.Type getType()
  {
    return EditorChangedEvent.Type.TOOL_SWITCH;
  }

}
