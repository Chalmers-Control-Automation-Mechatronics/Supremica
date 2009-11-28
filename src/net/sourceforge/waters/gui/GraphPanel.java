//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   GraphPanel
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

import net.sourceforge.waters.gui.renderer.AbstractRendererShape;
import net.sourceforge.waters.gui.renderer.MiscShape;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.Renderer;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;

import org.supremica.properties.Config;


/**
 * A component to display a graph. The graph panel provides basic support for
 * rendering graphs ({@link GraphProxy}) in a Swing component, but does not
 * allow for modifications.
 *
 * @author Gian Perrone, Robi Malik
 */

public class GraphPanel
  extends JComponent
  implements Printable
{

  //##########################################################################
  //# Constructors
  public GraphPanel(final GraphProxy graph, final ModuleProxy module)
  {
    mGraph = graph;
    mModule = module;
  }


  //##########################################################################
  //# Simple Access
  public GraphProxy getGraph()
  {
    return mGraph;
  }

  public ModuleProxy getModule()
  {
    return mModule;
  }

  protected GraphProxy getDrawnGraph()
  {
    return getGraph();
  }

  public ProxyShapeProducer getShapeProducer()
  {
    return mShapeProducer;
  }

  protected void setShapeProducer(final ProxyShapeProducer shaper)
  {
    mShapeProducer = shaper;
  }

  public List<MiscShape> getDrawnObjects()
  {
    return Collections.emptyList();
  }


  //##########################################################################
  //# Repainting
  protected void paintGrid(final Graphics g)
  {
    g.setColor(EditorColor.BACKGROUNDCOLOR);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(EditorColor.GRIDCOLOR);

    // Draw grid iff showGrid is true
    if (Config.GUI_EDITOR_SHOW_GRID.get())
    {
      final int x = -(int)getLocation().getX();
      final int y = -(int)getLocation().getY();

      for (int i = 0; i < getWidth(); i += Config.GUI_EDITOR_GRID_SIZE.get())
      {
        g.drawLine(i, y, i, getHeight());
      }

      for (int i = 0; i < getHeight(); i += Config.GUI_EDITOR_GRID_SIZE.get())
      {
        g.drawLine(x, i, getWidth(), i);
      }
    }
  }

  /**
   * Called when printing.
   */
  protected void printComponent(final Graphics g)
  {
    // Paint using other stroke
    //AbstractRendererShape.setBasicStroke(AbstractRendererShape.OLDTHINSTROKE); // Too thin for presentations
    //AbstractRendererShape.setBasicStroke(AbstractRendererShape.SINGLESTROKE); // Just right?
    //AbstractRendererShape.setBasicStroke(AbstractRendererShape.DOUBLESTROKE); // Too thick

    // Clear selection?

    // Paint the component on the supplied Graphics instance
    paintComponent(g, true);

    // Reset selection?

    // Reset stroke
    AbstractRendererShape.setBasicStroke(AbstractRendererShape.SINGLESTROKE);
  }

  /**
   * Called when painting.
   */
  protected void paintComponent(final Graphics g)
  {
    paintComponent(g, false);
  }

  /**
   * Paints the surface on {@code g}.
   */
  private void paintComponent(final Graphics g, final boolean printing)
  {
    // Only paint the grid if we're not printing!
    if (!printing) {
      paintGrid(g);
    }
    final Renderer renderer = new Renderer();
    renderer.renderGraph(getDrawnGraph(),
                         getDrawnObjects(),
                         getShapeProducer(),
                         (Graphics2D) g);
  }

  /**
   * Calculates a dimension indicating the size of all objects currently
   * displayed by the shape producer.
   */
  protected Dimension calculatePreferredSize()
  {
    final Rectangle2D area =
      getShapeProducer().getMinimumBoundingRectangle();
    final int width = (int) Math.ceil(area.getWidth());
    final int height = (int) Math.ceil(area.getHeight());
    return new Dimension(width, height);
  }

  /**
   * Implementation of the Printable interface.
   */
  public int print(final Graphics g, final PageFormat pageFormat, final int page)
  {
    //final double INCH = 72;

    Graphics2D g2d;

    // Validate the page number, we only print the first page
    if (page == 0)
    {
      g2d = (Graphics2D) g;

      // Translate the origin so that (0,0) is in the upper left corner of the printable area
      g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

      /*
            // Print page margin (for debugging purposes)
            {
                Line2D.Double line = new Line2D.Double();
                int i;
                g2d.setColor(Color.ORANGE);

                // Draw the vertical lines
                line.setLine(0, 0, 0, pageFormat.getHeight());
                g2d.draw(line);
                line.setLine(pageFormat.getImageableWidth(), 0, pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
                g2d.draw(line);

                // Draw the horizontal lines
                line.setLine(0, 0, pageFormat.getImageableWidth(), 0);
                g2d.draw(line);
                line.setLine(0, pageFormat.getImageableHeight(), pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
                g2d.draw(line);
            }
       */

      // Get the bounds of the actual drawing
      final Rectangle2D area =
        getShapeProducer().getMinimumBoundingRectangle();

      // This is the place to do rescaling if the figure won't fit on the page!
      final double scaleX = pageFormat.getImageableWidth() / (area.getWidth());
      final double scaleY = pageFormat.getImageableHeight() / (area.getHeight());
      final double scale = scaleX < scaleY ? scaleX : scaleY;
      if (scale < 1)
      {
        System.out.println("Rescaling figure to fit page. Scale: " + scale);
        g2d.scale(scale, scale);
      }

      // Put drawing at (0, 0)
      g2d.translate(-area.getX(), -area.getY());

      // Put the current figure into the Graphics object!
      print(g);

      // OK to print!
      return PAGE_EXISTS;
    }
    else
    {
      return NO_SUCH_PAGE;
    }
  }


  //##########################################################################
  //# Inner Enumeration Class DRAGOVERSTATUS
  /** Different status values for objects being dragged. */
  public enum DragOverStatus
  {
    /** Is not being draggedOver. */
    NOTDRAG,
    /** Is being draggedOver and can drop held object. */
    CANDROP,
    /** Is being draggedOver but can't drop held object. */
    CANTDROP;
  }


  //##########################################################################
  //# Data Members
  private final GraphProxy mGraph;
  private final ModuleProxy mModule;
  private ProxyShapeProducer mShapeProducer;


  //#########################################################################
  //# Class Constants
  public static final int TEXTSHADOWMARGIN = 2;

  private static final long serialVersionUID = 1L;

}
