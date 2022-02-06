//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.observer;

import java.util.EventObject;


/**
 * <P>A notification sent by the IDE to inform its observers that its
 * state has been changed.</P>
 *
 * <P>The different kinds of event can be distinguished using the {@link
 * #getKind()} method. Each kind of event is implemented by a separate
 * subclass that may provide additional detail.</P>
 *
 * <TABLE SUMMARY="Overview of event kinds">
 * <TR><TH ALIGN="left">Enumeration value</TH>
 * <TH ALIGN="left">Subclass</TH> <TH ALIGN="left">Description</TH></TR>
 * <TR><TD>CONTAINER_SWITCH</TD> <TD>{@link MainPanelSwitchEvent}</TD>
 * <TD>The user has switched document containers, e.g., opened or closed a
 * file.</TD></TR>
 * <TR><TD>MAINPANEL_SWITCH</TD> <TD>{@link MainPanelSwitchEvent}</TD>
 * <TD>The user has switched main panels, e.g., changed from editor
 * to analyser or vice versa.</TD></TR>
 * <TR><TD>SUBPANEL_SWITCH</TD> <TD>{@link SubPanelSwitchEvent}</TD>
 * <TD>The user has switched panels in the module editor, e.g.,
 * displayed a new graph, or transferred focus from the graph panel
 * to the components list, or similar.</TD></TR>
 * <TR><TD>TOOL_SWITCH</TD> <TD>{@link ToolbarChangedEvent}</TD>
 * <TD>A new drawing tool has been selected.</TD></TR>
 * <TR><TD>SELECTION_CHANGED</TD> <TD>{@link SelectionChangedEvent}</TD>
 * <TD>Some selection has changed.</TD></TR>
 * <TR><TD>CLIPBOARD_CHANGED</TD> <TD>{@link ClipboardChangedEvent}</TD>
 * <TD>The system clipboard contents have changed.</TD></TR>
 * <TR><TD>UNDOREDO</TD> <TD>{@link UndoRedoEvent}</TD>
 * <TD>The state of the undo manager has changed.</TD></TR>
 * <TR><TD>PENDING_SAVE</TD> <TD>{@link PendingSaveEvent}</TD>
 * <TD>The contents of the current container (module) are about to be
 * saved to a file.</TD></TR>
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
  public abstract Kind getKind();


  //#########################################################################
  //# Kind Enumeration
  public enum Kind {
    CONTAINER_SWITCH,
    MAINPANEL_SWITCH,
    SUBPANEL_SWITCH,
    TOOL_SWITCH,
    SELECTION_CHANGED,
    CLIPBOARD_CHANGED,
    UNDOREDO,
    PENDING_SAVE;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
