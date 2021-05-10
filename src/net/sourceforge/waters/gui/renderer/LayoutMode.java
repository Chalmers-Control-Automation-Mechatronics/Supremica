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

package net.sourceforge.waters.gui.renderer;

import java.awt.Color;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.GraphPanel;

import org.supremica.properties.Config;


/**
 * Enumeration of possible layout modes used by the graph renderer.
 *
 * @author Robi Malik
 */

public enum LayoutMode
{

  //#########################################################################
  //# Enumeration Constants
  /**
   * The default rendering mode of the editor,
   * showing all information in colour.
   */
  Default,
  /**
   * The default mode without colour.
   * All elements are black (or perhaps grey).
   * Used for EPS generation.
   */
  BlackAndWhite {
    @Override
    public Color getDefaultColor(final ColorGroup group)
    {
      switch (group) {
      case GROUP_NODE:
        return EditorColor.DEFAULTCOLOR_NODEGROUP;
      case NODE_LABEL:
        if (Config.GUI_EDITOR_STATE_NAMES_HIDDEN.getValue()) {
          return null;
        }
        // fall through ...
      default:
        return Color.BLACK;
      }
    }
  },
  /**
   * The old Chalmers mode.
   * Black and white, marked states circled instead of shaded.
   */
  ChalmersIDES;


  //#########################################################################
  //# Methods
  /**
   * Gets the colour to render items of the given type, if it has the
   * indicated highlighting status.
   * @param  group    The colour group identifying the type of object to be
   *                  rendered.
   * @param  dragOver The state of a drag&amp;drop operation affecting the
   *                  rendering of the item.
   * @param  selected Whether or not the item is selected.
   * @param  error    Whether or not an error is reported for the item.
   * @param  hasFocus Whether or not the panel displaying the item has
   *                  the keyboard focus.
   * @return The colour to be used, or <CODE>null</CODE> to suppress rendering
   *         of the item.
   */
  public Color getColor(final ColorGroup group,
                        final GraphPanel.DragOverStatus dragOver,
                        final boolean selected,
                        final boolean error,
                        final boolean hasFocus)
  {
    // In order of importance
    if (dragOver != GraphPanel.DragOverStatus.NOTDRAG) {
      if (dragOver == GraphPanel.DragOverStatus.CANDROP) {
        return EditorColor.CANDROPCOLOR;
      } else if (dragOver == GraphPanel.DragOverStatus.CANTDROP) {
        return EditorColor.CANTDROPCOLOR;
      }
    } else if (selected) {
      if (hasFocus) {
        return EditorColor.GRAPH_SELECTED_FOCUSSED;
      } else {
        return EditorColor.GRAPH_SELECTED_NOTFOCUSSED;
      }
    } else if (error) {
      if (group == ColorGroup.SIMPLE_NODE) {
        return EditorColor.ERRORCOLOR_NODE;
      } else {
        return EditorColor.ERRORCOLOR;
      }
    }
    return getDefaultColor(group);
  }

  /**
   * Gets the default colour to render items of the given type, if it does
   * not have any special status such as selected or highlighted.
   * @param  group  The colour group identifying the type of object to be
   *                rendered.
   * @return The colour to be used, or <CODE>null</CODE> to suppress rendering
   *         of the item.
   */
  public Color getDefaultColor(final ColorGroup group)
  {
    switch (group) {
    case GRAPH_ITEM:
    case SIMPLE_NODE:
      return EditorColor.DEFAULTCOLOR;
    case GROUP_NODE:
      return EditorColor.DEFAULTCOLOR_NODEGROUP;
    case NODE_LABEL:
      return EditorColor.NODE_LABEL_COLOR;
    case EVENT_LABEL:
      return EditorColor.TEXTCOLOR;
    case NORMAL_GUARD:
      return EditorColor.GUARDCOLOR;
    case ADDED_GUARD:
      return EditorColor.ADDEDGUARDCOLOR;
    case ACTION:
      return EditorColor.ACTIONCOLOR;
    default:
      throw new IllegalArgumentException("Unsupported colour group " +
                                         group + "!");
    }
  }

}
