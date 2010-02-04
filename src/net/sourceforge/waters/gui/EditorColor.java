//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorColor
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Color;
import java.awt.Font;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


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
   * Returns the appropriate colour for painting this object.
   */
  public static Color getColor(final Proxy o,
                               final GraphPanel.DragOverStatus dragOver,
                               final boolean selected,
                               final boolean error,
                               final boolean hasfocus)
  {
    // In order of importance
    if (dragOver != GraphPanel.DragOverStatus.NOTDRAG) {
      if (dragOver == GraphPanel.DragOverStatus.CANDROP) {
        return CANDROPCOLOR;
      } else if (dragOver == GraphPanel.DragOverStatus.CANTDROP) {
        return CANTDROPCOLOR;
      }
    } else if (error) {
      if (o instanceof SimpleNodeProxy) {
        return ERRORCOLOR_NODE;
      } else {
	return ERRORCOLOR;
      }
    } else if (selected) {
      if (hasfocus) {
        return GRAPH_SELECTED_FOCUSSED;
      } else {
        return GRAPH_SELECTED_NOTFOCUSSED;
      }
    }
    // Defaults
    if (o instanceof GroupNodeProxy) {
      return DEFAULTCOLOR_NODEGROUP;
    } else if (o instanceof LabelGeometryProxy) {
      return DEFAULTCOLOR_LABEL;
    } else {
      return DEFAULTCOLOR;
    }
  }

  /**
   * Returns a lighter shade of the color of the object for drawing a "shadow".
   */
  public static Color getShadowColor
    (final Proxy o,
     final GraphPanel.DragOverStatus dragOver,
     final boolean selected,
     final boolean error,
     final boolean hasfocus)
  {
    final Color color = getColor(o, dragOver, selected, error, hasfocus);
    return shadow(color);
  }

  /**
   * Returns a transparent variant of the supplied colour. The
   * alpha-value is changed to {@link #SHADOWALPHA}.
   */
  public static Color shadow(final Color color)
  {
    return shadow(color, SHADOWALPHA);
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

  /** The colour of action expressions. */
  public static final Color ACTIONCOLOR = Color.RED.darker().darker();

  /** The colour of the drag-select area. */
  public static final Color DRAGSELECTCOLOR = new Color(0,0,255,32);

  /** The default colour of objects. */
  public static final Color DEFAULTCOLOR = Color.BLACK;

  /** The default colour of node labels. */
  public static final Color DEFAULTCOLOR_LABEL = Color.GREEN.darker().darker();

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
   * An enhanced version of the default font for headers.
   * This presently is only used for the header of blocked events lists.
   */
  public static final Font HEADER_FONT = DEFAULT_FONT.deriveFont(Font.BOLD);
  /**
   * The default font for uncontrollable events.
   * This is an italic version of the default font.
   */
  public static final Font UNCONTROLLABLE_FONT =
    DEFAULT_FONT.deriveFont(Font.ITALIC);


  //#########################################################################
  //# Private Class Constants
  /** The alpha value of the shadow-colours. */
  private static final int SHADOWALPHA = 48;


}
