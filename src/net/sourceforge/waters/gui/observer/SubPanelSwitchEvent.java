//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.observer
//# CLASS:   SubPanelSwitchdEvent
//###########################################################################
//# $Id: SubPanelSwitchEvent.java,v 1.1 2007-08-10 04:34:31 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.observer;


/**
 * A notification sent by the IDE to indicate a switch of subpanels.
 * This event is sent, e.g., when the user has switched between different
 * graphs in the module window.
 *
 * @author Robi Malik
 */

public class SubPanelSwitchEvent
  extends EditorChangedEvent
{

  //#########################################################################
  //# Constructors
  public SubPanelSwitchEvent(final Object source)
  {
    super(source);
  }

	
  //#########################################################################
  //# Simple Access
  public EditorChangedEvent.Kind getKind()
  {
    return EditorChangedEvent.Kind.SUBPANEL_SWITCH;
  }

}
