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
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;


/**
 * A collection of all the colors used in the editor.
 *
 * @author Simon Ware
 */

public class EditorColor
{

  //#########################################################################
  //# Colour Selection
  /**
   * Returns the appropriate color for painting this object.
   */
  public static Color getColor(final Proxy o,
                               final EditorSurface.DRAGOVERSTATUS dragOver,
                               final boolean selected,
			       final boolean error,
                               final boolean hasfocus)
  {
    // In order of importance
    if (dragOver != EditorSurface.DRAGOVERSTATUS.NOTDRAG) {
      if (dragOver == EditorSurface.DRAGOVERSTATUS.CANDROP) {
        return CANDROPCOLOR;
      } else if (dragOver == EditorSurface.DRAGOVERSTATUS.CANTDROP) {
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
     final EditorSurface.DRAGOVERSTATUS dragOver, 
     final boolean selected,
     final boolean error,
     final boolean hasfocus)
  {
    final Color color = getColor(o, dragOver, selected, error, hasfocus);
    return shadow(color);
  }

  /**
   * Returns a transparent variant of the supplied color. The
   * alpha-value is changed to {@link #SHADOWALPHA}.
   */
  public static Color shadow(final Color color)
  {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                     SHADOWALPHA);
  }


  //#########################################################################
  //# Public Colour Constants
  /** The default color of disabled text. */
  public static final Color DISABLEDCOLOR = Color.GRAY;
    
  /** The background color in the editor. */
  public static final Color BACKGROUNDCOLOR = Color.WHITE;
        
  /** The color of the grid in the editor. */
  public static final Color GRIDCOLOR = new Color(0.875f, 0.875f, 0.875f);
    
  /** The default color of marked (accepting) nodes. */
  public static final Color DEFAULTMARKINGCOLOR = Color.GRAY;
    
  /** The color of guard expressions. */
  public static final Color GUARDCOLOR = Color.CYAN.darker().darker();

  /** The color of action expressions. */
  public static final Color ACTIONCOLOR = Color.RED.darker().darker();
    
  /** The color of the drag-select area. */
  public static final Color DRAGSELECTCOLOR = new Color(0,0,255,32);
    
  /** The default color of objects. */
  public static final Color DEFAULTCOLOR = Color.BLACK;

  /** The default color of node labels. */
  public static final Color DEFAULTCOLOR_LABEL = Color.GREEN.darker().darker();

  /** The default color of goup nodes. */
  public static final Color DEFAULTCOLOR_NODEGROUP = new Color(64,64,64);
    
  /**
   * The color of erring objects. For example colliding nodes and nodegroups.
   */
  public static final Color ERRORCOLOR = Color.RED;
  /**
   * Slightly darker error color, to distinguish simple nodes from
   * group nodes more clearly. Overkill?
   */
  public static final Color ERRORCOLOR_NODE = ERRORCOLOR.darker();
    
  /**
   * The color of selected objects in a graph with keyboard focus.
   */
  public static final Color GRAPH_SELECTED_FOCUSSED = Color.BLUE;
  /**
   * The color of selected objects in a graph without keyboard focus.
   */
  public static final Color GRAPH_SELECTED_NOTFOCUSSED =
    new Color(64, 112, 128);
    
  /**
   * The color of objects when showing that stuff can be dropped on them.
   */
  public static final Color CANDROPCOLOR = Color.GREEN.darker().darker();

  /**
   * The color of objects when showing that stuff cannot be dropped on them.
   */
  public static final Color CANTDROPCOLOR = Color.RED;

  /** Invisible color. */
  public static final Color INVISIBLE = new Color(0,0,0,0);

  /**
   * The selection background color for list or tree view panels that
   * are in focus.
   */
  public static final Color BACKGROUND_FOCUSSED = new Color(184, 208, 224);

  /**
   * The selection background color for list or tree view panels that
   * are not in focus.
   */
  public static final Color BACKGROUND_NOTFOCUSSED = new Color(232, 232, 232);


  //#########################################################################
  //# Private Class Constants
  /** The alpha value of the shadow-colors. */
  private static final int SHADOWALPHA = 48;
    
}
