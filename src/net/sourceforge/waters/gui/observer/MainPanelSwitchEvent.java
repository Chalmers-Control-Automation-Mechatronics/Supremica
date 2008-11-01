//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.observer
//# CLASS:   MainPanelSwitchdEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.observer;


/**
 * A notification sent by the IDE to indicate a switch of main panels.
 * This event is sent when the user has switched between the editor and
 * analyzer panels.
 *
 * @author Robi Malik
 */

public class MainPanelSwitchEvent
  extends EditorChangedEvent
{

  //#########################################################################
  //# Constructors
  public MainPanelSwitchEvent(final Object source)
  {
    super(source);
  }

	
  //#########################################################################
  //# Simple Access
  public EditorChangedEvent.Kind getKind()
  {
    return EditorChangedEvent.Kind.MAINPANEL_SWITCH;
  }

}
