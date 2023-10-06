//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.gui;

import java.awt.Color;
import java.awt.Font;


/**
 * A collection of all the colours used in the editor.
 *
 * @author Simon Ware
 */

public class EditorColor
{

  //#########################################################################
  //# Colour Selection
  /**
   * Returns a transparent variant of the supplied colour. The
   * alpha-value is changed to {@link #SHADOW_ALPHA}.
   */
  public static Color shadow(final Color color)
  {
    return shadow(color, SHADOW_ALPHA);
  }

  /**
   * Returns a transparent variant of the supplied colour. The
   * alpha-value is changed to the given value.
   */
  public static Color shadow(final Color color, final int alpha)
  {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
  }


  //#########################################################################
  //# Public Colour Constants
  /** The default background colour for all panels. */
  public static final Color BACKGROUNDCOLOR = Color.WHITE;

  /** The default colour of text. */
  public static final Color TEXTCOLOR = Color.BLACK;

  /** The default colour of disabled text. */
  public static final Color DISABLEDCOLOR = Color.GRAY;

  /** The colour of the grid in the editor. */
  public static final Color GRIDCOLOR = new Color(0.875f, 0.875f, 0.875f);

  /** The default colour of marked (accepting) nodes. */
  public static final Color DEFAULTMARKINGCOLOR = Color.GRAY;

  /** The colour of guard expressions. */
  public static final Color GUARDCOLOR = Color.CYAN.darker().darker();

  /** The colour of guard expressions. */
  public static final Color ADDEDGUARDCOLOR = new Color(114,68,174);

  /** The colour of action expressions. */
  public static final Color ACTIONCOLOR = Color.RED.darker().darker();

  /** The colour of the drag-select area. */
  public static final Color DRAGSELECTCOLOR = new Color(0,0,255,32);

  /** The default colour of objects. */
  public static final Color DEFAULTCOLOR = Color.BLACK;

  /** The default colour of node labels. */
  public static final Color NODE_LABEL_COLOR = Color.GREEN.darker().darker();

  /** The default colour of group nodes. */
  public static final Color DEFAULTCOLOR_NODEGROUP = new Color(64,64,64);

  /**
   * The colour of erring objects. For example colliding nodes and node groups.
   */
  public static final Color ERRORCOLOR = Color.RED;
  /**
   * Slightly darker error colour, to distinguish simple nodes from
   * group nodes more clearly. Overkill?
   */
  public static final Color ERRORCOLOR_NODE = ERRORCOLOR.darker();

  /**
   * The colour of selected objects in a graph with keyboard focus.
   */
  public static final Color GRAPH_SELECTED_FOCUSSED = Color.BLUE;
  /**
   * The colour of selected objects in a graph without keyboard focus.
   */
  public static final Color GRAPH_SELECTED_NOTFOCUSSED =
    new Color(64, 112, 128);

  /**
   * The colour of objects when showing that stuff can be dropped on them.
   */
  public static final Color CANDROPCOLOR = Color.GREEN.darker().darker();

  /**
   * The colour of objects when showing that stuff cannot be dropped on them.
   */
  public static final Color CANTDROPCOLOR = Color.RED;

  /** Invisible colour. */
  public static final Color INVISIBLE = new Color(0,0,0,0);

  /**
   * The selection background colour for list or tree view panels that
   * are in focus.
   */
  public static final Color BACKGROUND_FOCUSSED = new Color(184, 208, 224);

  /**
   * The selection background colour for list or tree view panels that
   * are not in focus.
   */
  public static final Color BACKGROUND_NOTFOCUSSED = new Color(232, 232, 232);

  /**
   * The colour for active events in the simulator.
   */
  public static final Color SIMULATION_ACTIVE = new Color(192, 0, 128);

  /**
   * The colour for enabled events in the simulator.
   */
  public static final Color SIMULATION_ENABLED = CANDROPCOLOR;

  /**
   * The colour for enabled events that are in focus in the simulator
   */
  public static final Color SIMULATION_FOCUSED_SHADOW =
    shadow(Color.YELLOW, 144);

  /**
   * The colour for transitions and states which are removed due to optimization
   */
  public static final Color SIMULATION_INVALID = new Color(128, 128, 128);

  /**
   * The colour for disabled events that are in focus in the simulator
   */
  public static final Color SIMULATION_DISABLED_FOCUSED =
    GRAPH_SELECTED_NOTFOCUSSED;


  //#########################################################################
  //# Public Font Constants
  /**
   * The default font in the graph window.
   */
  public static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);
  /**
   * The default font for uncontrollable events.
   * This is an italic version of the default font.
   */
  public static final Font UNCONTROLLABLE_FONT =
    DEFAULT_FONT.deriveFont(Font.ITALIC);


  //#########################################################################
  //# Private Class Constants
  /** The alpha value of the shadow-colours. */
  private static final int SHADOW_ALPHA = 48;


}
