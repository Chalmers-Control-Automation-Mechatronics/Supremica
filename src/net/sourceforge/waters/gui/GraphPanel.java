//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

import net.sourceforge.waters.gui.renderer.AbstractRendererShape;
import net.sourceforge.waters.gui.renderer.MiscShape;
import net.sourceforge.waters.gui.renderer.PrintRenderingContext;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.Renderer;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;

import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;


/**
 * A component to display a graph. The graph panel provides basic support for
 * rendering graphs ({@link GraphProxy}) in a Swing component, but does not
 * allow for modifications.
 *
 * @author Gian Perrone, Robi Malik
 */

public class GraphPanel
  extends JComponent
  implements SupremicaPropertyChangeListener, Printable
{

  //##########################################################################
  //# Constructors
  public GraphPanel(final GraphProxy graph, final ModuleProxy module)
  {
    this(graph, module, new ModuleContext(module));
  }

  public GraphPanel(final GraphProxy graph,
                    final ModuleProxy module,
                    final ModuleContext context)
  {
    mGraph = graph;
    mModule = module;
    mModuleContext = context;
    registerSupremicaPropertyChangeListeners();
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

  public ModuleContext getModuleContext()
  {
    return mModuleContext;
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


  //#########################################################################
  //# Coordinate Transformation
  protected AffineTransform getTransform()
  {
    if (mTransform == null) {
      mTransform = createTransform();
    }
    return mTransform;
  }

  protected AffineTransform createTransform()
  {
    return new AffineTransform(); // identity matrix
  }

  protected AffineTransform getInverseTransform()
  {
    if (mInverseTransform == null) {
      try {
        final AffineTransform transform = getTransform();
        mInverseTransform = transform.createInverse();
      } catch (final NoninvertibleTransformException exception) {
        throw new WatersRuntimeException(exception);
      }
    }
    return mInverseTransform;
  }

  protected Point applyInverseTransform(final Point point)
  {
    final AffineTransform inverse = getInverseTransform();
    final Point2D tranformed = inverse.transform(point, null);
    final int x = (int) Math.round(tranformed.getX());
    final int y = (int) Math.round(tranformed.getY());
    return new Point(x, y);
  }

  protected void clearTransform()
  {
    mTransform = mInverseTransform = null;
  }


  //#########################################################################
  //# Repaint Support
  public void close()
  {
    mShapeProducer.close();
    unregisterSupremicaPropertyChangeListeners();
  }

  public void registerSupremicaPropertyChangeListeners()
  {
    Config.GUI_EDITOR_BACKGROUND_COLOR.addPropertyChangeListener(this);
    Config.GUI_EDITOR_NODE_RADIUS.addPropertyChangeListener(this);
    Config.GUI_EDITOR_EDGEARROW_AT_END.addPropertyChangeListener(this);
  }

  public void unregisterSupremicaPropertyChangeListeners()
  {
    Config.GUI_EDITOR_BACKGROUND_COLOR.removePropertyChangeListener(this);
    Config.GUI_EDITOR_NODE_RADIUS.removePropertyChangeListener(this);
    Config.GUI_EDITOR_EDGEARROW_AT_END.removePropertyChangeListener(this);
  }


  //#########################################################################
  //# Interface org.supremica.properties.SupremicaPropertyChangeListener
  @Override
  public void propertyChanged(final SupremicaPropertyChangeEvent event)
  {
    setBackground(Config.GUI_EDITOR_BACKGROUND_COLOR.get());
    getShapeProducer().clear();
    repaint();
  }


  //##########################################################################
  //# Repainting
  protected void paintGrid(final Graphics graphics)
  {
    if (Config.GUI_EDITOR_SHOW_GRID.get()) {
      graphics.setColor(EditorColor.GRIDCOLOR);
      final int grid = Config.GUI_EDITOR_GRID_SIZE.get();
      final AffineTransform inverse = getInverseTransform();
      final Point pt = new Point(0, 0);
      final Point2D result = inverse.transform(pt, null);
      final int x0 = Math.floorDiv((int) Math.floor(result.getX()), grid) * grid;
      final int y0 = Math.floorDiv((int) Math.floor(result.getY()), grid) * grid;
      pt.setLocation(getWidth(), getHeight());
      inverse.transform(pt, result);
      final int x1 = (int) Math.ceil(result.getX());
      final int y1 = (int) Math.ceil(result.getY());
      for (int x = x0; x <= x1; x += grid) {
        graphics.drawLine(x, y0, x, y1);
      }
      for (int y = y0; y <= y1; y += grid) {
        graphics.drawLine(x0, y, x1, y);
      }
    }
  }

  /**
   * Called when printing.
   */
  @Override
  protected void printComponent(final Graphics g)
  {
    final ProxyShapeProducer producer = new ProxyShapeProducer(mGraph,
                             new PrintRenderingContext(mModuleContext));
    final Renderer renderer =
      new Renderer(getDrawnGraph(), getDrawnObjects(), producer);
    renderer.renderGraph((Graphics2D) g);
    // Reset stroke
    AbstractRendererShape.setBasicStroke(AbstractRendererShape.SINGLESTROKE);
  }

  /**
   * Called when painting.
   */
  @Override
  protected void paintComponent(final Graphics graphics)
  {
    final Color background = Config.GUI_EDITOR_BACKGROUND_COLOR.get();
    graphics.setColor(background);
    graphics.fillRect(0, 0, getWidth(), getHeight());
    final Graphics2D g2d = (Graphics2D) graphics;
    final AffineTransform old = g2d.getTransform();
    final AffineTransform transform = getTransform();
    final AffineTransform copy = new AffineTransform(old);
    copy.concatenate(transform);
    g2d.setTransform(copy);
    paintGrid(graphics);
    final Renderer renderer =
      new Renderer(getDrawnGraph(), getDrawnObjects(), getShapeProducer());
    renderer.renderGraph(g2d);
    g2d.setTransform(old);
  }

  @Override
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
    /** Is not being dragged over. */
    NOTDRAG,
    /** Is being dragged over and can drop held object. */
    CANDROP,
    /** Is being dragged over but cannot drop held object. */
    CANTDROP;
  }


  //##########################################################################
  //# Data Members
  private final GraphProxy mGraph;
  private final ModuleProxy mModule;
  private final ModuleContext mModuleContext;
  private ProxyShapeProducer mShapeProducer;
  private AffineTransform mTransform = null;
  private AffineTransform mInverseTransform = null;


  //#########################################################################
  //# Class Constants
  public static final int TEXTSHADOWMARGIN = 2;

  private static final long serialVersionUID = 1L;

}
