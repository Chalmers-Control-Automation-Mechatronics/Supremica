//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   SimpleNodeProxyShape
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.PropositionIcon;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import org.supremica.properties.Config;


public class SimpleNodeProxyShape
  extends AbstractProxyShape
{

  //#########################################################################
  //# Constructor
  SimpleNodeProxyShape(final SimpleNodeProxy node,
                       final ModuleContext context)
  {
    super(node);
    mContext = context;
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.get();
    final int diameter = radius + radius;
    final Point2D p = getProxy().getPointGeometry().getPoint();
    final Rectangle2D rect =
      new Rectangle2D.Double(p.getX() - radius, p.getY() - radius,
                             diameter, diameter);
    mCircleShape = new Arc2D.Double(rect, 0, 360, Arc2D.OPEN);
    mShape = new GeneralPath(mCircleShape);

    // Create handles
    if (node.isInitial()) {
      final Handle handle = new InitialStateHandle(node);
      mHandles = Collections.singletonList(handle);
      mShape.append(handle.getShape(), false);
    } else {
      mHandles = Collections.emptyList();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.RendererShape
  public GeneralPath getShape()
  {
    return mShape;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.ProxyShape
  public SimpleNodeProxy getProxy()
  {
    return (SimpleNodeProxy) super.getProxy();
  }

  public List<Handle> getHandles()
  {
    return mHandles;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.gui.renderer.AbstractProxyShape
  public void draw(final Graphics2D graphics,
                   final RenderingInformation status)
  {
    updateColors();
    // This rectangle is not the same as the one used to create the
    // mCircleShape! It gives rounding errors!
    // Rectangle2D bounds = mCircleShape.getBounds();
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.get();
    final int diameter = radius + radius;
    final Rectangle2D bounds =
      new Rectangle2D.Double(mCircleShape.getX(), mCircleShape.getY(),
                             diameter, diameter);
    drawNode(graphics, bounds, mColors);
    // Draw handles (initial state arrow)
    for (final Handle handle : mHandles) {
      graphics.setColor(status.getColor());
      handle.draw(graphics, status);
    }
    // The above handle drawing should not be necessary (it's drawn below) but
    // the initial arrow refuses to be drawn filled in the editor
    // (not in printed output!?)?
    // Draw the basic shape (the outline + handles (initial state arrow))
    super.draw(graphics, status);
    // Cross out if forbidden
    if (mForbidden) {
      drawForbidden(graphics, bounds);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * I think this method updates the set of colours used
   * (if this is a marked node).
   */
  private void updateColors()
  {
    final SimpleNodeProxy node = getProxy();
    final PropositionIcon.ColorInfo info =
      mContext.guessPropositionColors(node);
    mColors = info.getColors();
    mForbidden = info.isForbidden();
  }


  //#########################################################################
  //# Static Drawing
  public static void drawNode(final Graphics2D graphics,
                              final Rectangle2D bounds,
                              final List<Color> colors)
  {
    if (colors.isEmpty()) {
      final Arc2D arc = new Arc2D.Double(bounds, 0, 360, Arc2D.OPEN);
      graphics.setColor(FILLCOLOR);
      graphics.fill(arc);
    } else {
      // Draw marking
      final Object layoutMode = Config.GUI_EDITOR_LAYOUT_MODE.get();
      if (layoutMode == Config.LAYOUT_MODE_LEGALVALUES.ChalmersIDES) {
        // CHALMERS IDES MODE---SINGLE TYPE OF MARKING, DOUBLE CIRCLES
        graphics.setColor(EditorColor.DEFAULTCOLOR);
        graphics.setStroke(SINGLESTROKE);
        final Arc2D arc = new Arc2D.Double
          (bounds.getX() + 2, bounds.getY() + 2,
           bounds.getWidth() - 4, bounds.getHeight()-4,
           0, 360, Arc2D.OPEN);
        graphics.draw(arc);
      } else {
        // DEFAULT MODE
        double i = 0;
        final double degrees = 360.0 / (double) colors.size();
        for (final Color c : colors) {
          final Arc2D arc = new Arc2D.Double(bounds, i, degrees, Arc2D.PIE);
          graphics.setColor(c);
          graphics.fill(arc);
          i += degrees;
        }
      }
    }
  }

  public static void drawForbidden(final Graphics2D graphics,
                                   final Rectangle2D bounds)
  {
    graphics.setColor(EditorColor.ERRORCOLOR);
    graphics.setStroke(DOUBLESTROKE);
    final int minx = (int) bounds.getMinX();
    final int miny = (int) bounds.getMinY();
    final int maxx = (int) bounds.getMaxX();
    final int maxy = (int) bounds.getMaxY();
    graphics.drawLine(minx, miny, maxx, maxy);
    graphics.drawLine(maxx, miny, minx, maxy);
  }


  //#########################################################################
  //# Data Members
  private final ModuleContext mContext;
  private final Arc2D mCircleShape;
  private final GeneralPath mShape; // To incorporate the initial state arrow
  private final List<Handle> mHandles;

  private List<Color> mColors = new ArrayList<Color>();
  private boolean mForbidden = false;


  //#########################################################################
  //# Class Constants
  public static final int DEFAULT_OFFSET_X = 5;
  public static final int DEFAULT_OFFSET_Y = 5;
  public static final Point2D DEFAULT_OFFSET =
    new Point(DEFAULT_OFFSET_X, DEFAULT_OFFSET_Y);

  public static final int DEFAULT_INITARROW_X = -5;
  public static final int DEFAULT_INITARROW_Y = -5;
  public static final Point2D DEFAULT_INITARROW =
    new Point(DEFAULT_INITARROW_X, DEFAULT_INITARROW_Y);

  private static final Color FILLCOLOR = Color.WHITE;

}
