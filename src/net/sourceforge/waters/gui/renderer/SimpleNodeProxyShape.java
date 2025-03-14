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

package net.sourceforge.waters.gui.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.util.PropositionIcon;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import org.supremica.properties.Config;


public class SimpleNodeProxyShape
  extends AbstractProxyShape
{

  //#########################################################################
  //# Constructor
  SimpleNodeProxyShape(final SimpleNodeProxy node,
                       final PropositionIcon.ColorInfo info)
  {
    super(node);
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.getValue();
    final int diameter = radius + radius;
    final Point2D p = getProxy().getPointGeometry().getPoint();
    final Rectangle2D rect =
      new Rectangle2D.Double(p.getX() - radius, p.getY() - radius,
                             diameter, diameter);
    mCircleShape = new Arc2D.Double(rect, 0, 360, Arc2D.OPEN);
    mShape = new GeneralPath(mCircleShape);
    mColors = info.getColors();
    mForbidden = info.isForbidden();

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
  @Override
  public GeneralPath getShape()
  {
    return mShape;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.ProxyShape
  @Override
  public SimpleNodeProxy getProxy()
  {
    return (SimpleNodeProxy) super.getProxy();
  }

  @Override
  public List<Handle> getHandles()
  {
    return mHandles;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.gui.renderer.AbstractProxyShape
  @Override
  public void draw(final Graphics2D graphics,
                   final RenderingInformation status)
  {
    // This rectangle is not the same as the one used to create the
    // mCircleShape! It gives rounding errors!
    // Rectangle2D bounds = mCircleShape.getBounds();
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.getValue();
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
  //# Static Drawing
  public static void drawNode(final Graphics2D graphics,
                              final Rectangle2D bounds,
                              final List<Color> colors)
  {
    graphics.setStroke(SINGLESTROKE);
    if (colors == null) {
      final Arc2D arc = new Arc2D.Double(bounds, 0, 360, Arc2D.OPEN);
      graphics.draw(arc);
    } else if (colors.isEmpty()) {
      final Arc2D arc = new Arc2D.Double(bounds, 0, 360, Arc2D.OPEN);
      graphics.setColor(FILLCOLOR);
      graphics.fill(arc);
    } else {
      // Draw marking
      final Object layoutMode = Config.GUI_EDITOR_LAYOUT_MODE.getValue();
      if (layoutMode == LayoutMode.ChalmersIDES) {
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
        final double degrees = 360.0 / colors.size();
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
  private final Arc2D mCircleShape;
  private final GeneralPath mShape; // To incorporate the initial state arrow
  private final List<Handle> mHandles;
  private final List<Color> mColors;
  private final boolean mForbidden;


  //#########################################################################
  //# Class Constants
  public static final int DEFAULT_OFFSET_X = 5;
  public static final int DEFAULT_OFFSET_Y = 5;
  public static final Point2D DEFAULT_OFFSET =
    new Point(DEFAULT_OFFSET_X, DEFAULT_OFFSET_Y);

  private static final Color FILLCOLOR = Color.WHITE;

}
