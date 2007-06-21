//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.observer
//# CLASS:   EditorChangedEvent
//###########################################################################
//# $Id: EditorChangedEvent.java,v 1.3 2007-06-21 11:16:23 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.observer;

import java.util.EventObject;


/**
 * <P>A notification sent by the IDE to inform its observers that its
 * state has been changed.</P>
 *
 * <P>The different types of event can be distinguished using the {@link
 * #getType()} method. Each type of event is implemented by a separate
 * subclass that may provide additional detail.</P>
 *
 * <TABLE>
 * <TR><TH>Enumeration value</TH> <TH>Subclass</TH> <TH>Description</TH></TR>
 * <TR><TD>MAINPANEL_SWITCH</TD> <TD>{@link MainPanelSwitchEvent}</TD>
 * <TD>The user has switched main panels, e.g., changed from editor
 * to analyzer or vice versa.</TD></TR>
 * <TR><TD>MODULE_SWITCH</TD> <TD>{@link ModuleSwitchEvent}</TD>
 * <TD>A new file has been opened or activated.</TD></TR>
 * <TR><TD>SUBPANEL_SWITCH</TD> <TD>{@link SubPanelSwitchEvent}</TD>
 * <TD>The user has switched panels in the module editor, e.g.,
 * displayed a new graph, or transferred focus from the graph panel
 * to the components list, or similar.</TD></TR>
 * <TR><TD>TOOL_SWITCH</TD> <TD>{@link ToolbarChangedEvent}</TD>
 * <TD>A new drawing tool has been selected.</TD></TR>
 * <TR><TD>SELECTION_CHANGED</TD> <TD>{@link SelectionChangedEvent}</TD>
 * <TD>Some selection has changed.</TD></TR>
 * <TR><TD>UNDOREDO</TD> <TD>{@link UndoRedoEvent}</TD>
 * <TD>The state of the undo manager has changed.</TD></TR>
 * </TABLE>
 *
 * @author Simon Ware, Robi Malik
 */

public abstract class EditorChangedEvent extends EventObject
{

  //#########################################################################
  //# Constructors
  public EditorChangedEvent(final Object source)
  {
    super(source);
  }


  //#########################################################################
  //# Simple Access
  public abstract Type getType();


  //#########################################################################
  //# Type Enumeration
  public enum Type {
    MAINPANEL_SWITCH,
    MODULE_SWITCH,
    SUBPANEL_SWITCH,
    TOOL_SWITCH,
    SELECTION_CHANGED,
    UNDOREDO;
  }

}
	
