//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.observer
//# CLASS:   ToolbarChangedEvent
//###########################################################################
//# $Id: ToolbarChangedEvent.java,v 1.4 2007-06-25 20:18:48 robi Exp $
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
 * @author Simon Ware, Robi Malik
 */

public class ToolbarChangedEvent
  extends EditorChangedEvent
{

  //#########################################################################
  //# Constructors
  public ToolbarChangedEvent(final ControlledToolbar source,
                             final ControlledToolbar.Tool tool)
  {
    super(source);
    mTool = tool;
  }

	
  //#########################################################################
  //# Simple Access
  public ControlledToolbar getSource()
  {
    return (ControlledToolbar) super.getSource();
  }

  public EditorChangedEvent.Kind getKind()
  {
    return EditorChangedEvent.Kind.TOOL_SWITCH;
  }

  public ControlledToolbar.Tool getTool()
  {
    return mTool;
  }


  //#########################################################################
  //# Data Members
  private final ControlledToolbar.Tool mTool;

}
