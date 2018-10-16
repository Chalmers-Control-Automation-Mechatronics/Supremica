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

package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.subject.module.GeometryTools;

import org.supremica.properties.Config;


abstract class EdgeProxyShape
  extends AbstractProxyShape
{

  //#########################################################################
  //# Constructors
  EdgeProxyShape(final EdgeProxy edge)
  {
    super(edge);
    mHandles = new ArrayList<Handle>(2);
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the start point of this edge.
   * This method returns the actual start point, i.e., the point on the
   * periphery of the source node where the edge starts. It coincides
   * with the location of the SOURCE handle.
   */
  abstract Point2D getStartPoint();

  /**
   * Gets the end point of this edge.
   * This method returns the actual end point, i.e., the point on the
   * periphery of the target node where the edge ends. It coincides
   * with the location of the TARGET handle.
   */
  abstract Point2D getEndPoint();

  /**
   * Gets the turning point of this edge.
   * The turning point is a point about half-way on the edge.
   */
  abstract Point2D getTurningPoint();

  /**
   * Gets the curve shape for this edge.
   * The full shape is obtained by adding an arrow.
   */
  abstract Shape getCurve();


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.RendererShape
  @Override
  public Shape getShape()
  {
    if (mShape == null) {
      mShape = new GeneralPath(GeneralPath.WIND_NON_ZERO, 2);
      final Shape curve = getCurve();
      mShape.append(curve, false);
      final Shape arrow = getArrowHead();
      if (arrow != null) {
        mShape.append(arrow, false);
      }
    }
    return mShape;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.ProxyShape
  @Override
  public List<Handle> getHandles()
  {
    return mHandles;
  }


  //#########################################################################
  //# Drawing
  @Override
  public void draw(final Graphics2D g2d, final RenderingInformation status)
  {
    super.draw(g2d, status);
    final Shape arrow = getArrowHead();
    if (arrow != null) {
      g2d.setStroke(BASICSTROKE);
      g2d.setColor(status.getColor());
      g2d.fill(arrow);
    }
  }

  Shape getArrowHead()
  {
    if (mArrowHead == null) {
      switch (Config.GUI_EDITOR_EDGEARROW_POSITION.get()) {
      case End:
      {
        final Point2D tip = getEndPoint();
        final Point2D dir = getEndDirection();
        mArrowHead = createArrowHead(tip, dir);
        break;
      }
      case Middle:
      {
        final Point2D tip = getInnerArrowTipPoint();
        final Point2D dir = getMidDirection();
        mArrowHead = createArrowHead(tip, dir);
        break;
      }
      }
    }
    return mArrowHead;
  }

  static GeneralPath createArrowHead(final Point2D tip, final Point2D dir)
  {
    final double dx = -ARROW_SIDE * dir.getX();
    final double dy = -ARROW_SIDE * dir.getY();
    final double x0 = tip.getX();
    final double y0 = tip.getY();
    final double x1 = x0 + dx * ARROW_COS - dy * ARROW_SIN;
    final double y1 = y0 + dx * ARROW_SIN + dy * ARROW_COS;
    final double x2 = x0 + dx * ARROW_COS + dy * ARROW_SIN;
    final double y2 = y0 - dx * ARROW_SIN + dy * ARROW_COS;
    final GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO, 3);
    //final GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
    path.append(new Line2D.Double(x0, y0, x1, y1), false);
    path.append(new Line2D.Double(x1, y1, x2, y2), true);
    path.closePath();
    return path;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Gets the middle of this edge. This simply is the middle between the
   * start and end points. It may or may not actually be on the edge.
   */
  Point2D getMidPoint()
  {
    final Point2D start = getStartPoint();
    final Point2D end = getEndPoint();
    return GeometryTools.getMidPoint(start, end);
  }

  /**
   * Gets the position of the arrow tip if it is rendered on the centre
   * of the curve as opposed to its end. This is usually the turning
   * point, but subclasses may override.
   */
  Point2D getInnerArrowTipPoint()
  {
    return getTurningPoint();
  }

  /**
   * Gets the normalised direction vector of the edge at its turning
   * point. This is used to render the arrow if it is rendered on the
   * centre of the curve as opposed to its end.
   */
  Point2D getMidDirection()
  {
    final Point2D start = getStartPoint();
    final Point2D end = getEndPoint();
    return GeometryTools.getNormalizedDirection(start, end);
  }

  /**
   * Gets the normalised direction vector of the edge at its end
   * point. This is used to render the arrow if it is rendered at the end
   * of the curve.
   */
  Point2D getEndDirection()
  {
    return getMidDirection();
  }

  void createHandles()
  {
    final Point2D start = getStartPoint();
    createHandle(start, Handle.HandleType.SOURCE);
    final Point2D end = getEndPoint();
    createHandle(end, Handle.HandleType.TARGET);
  }

  void createHandle(final Point2D point, final Handle.HandleType type)
  {
    final Handle handle = new DefaultHandle(point, type);
    mHandles.add(handle);
  }

  boolean isInClickBounds(final Point point)
  {
    if (mClickBounds == null) {
      final Shape shape = getShape();
      mClickBounds = shape.getBounds();
      mClickBounds.x -= CLICK_TOLERANCE;
      mClickBounds.y -= CLICK_TOLERANCE;
      mClickBounds.width += 2 * CLICK_TOLERANCE;
      mClickBounds.height += 2 * CLICK_TOLERANCE;
    }
    final int x = point.x;
    final int y = point.y;
    return
      x >= mClickBounds.x && x <= mClickBounds.x + mClickBounds.width &&
      y >= mClickBounds.y && y <= mClickBounds.y + mClickBounds.height;
  }


  //#########################################################################
  //# Data Members
  private final List<Handle> mHandles;
  private GeneralPath mShape;
  private GeneralPath mArrowHead;
  private Rectangle mClickBounds = null;


  //#########################################################################
  //# Class Constants
  /**
   * The width of the point of the arrow, in radians.
   */
  static final double ARROW_ANGLE;// = 0.3 * Math.PI;
  static
  {
      if (Config.GUI_EDITOR_LAYOUT_MODE.get() == LayoutMode.ChalmersIDES)
          ARROW_ANGLE = 0.2 * Math.PI;
      else
          ARROW_ANGLE = 0.3 * Math.PI;
  };
  static final double ARROW_SIN = Math.sin(0.5 * ARROW_ANGLE);
  static final double ARROW_COS = Math.cos(0.5 * ARROW_ANGLE);

  /**
   * The height of the arrow, i.e., the distance it covers on the line.
   */
  static final double ARROW_HEIGHT = 7.0;
  /**
   * The square of the arrow height.
   */
  static final double ARROW_HEIGHT_SQ = ARROW_HEIGHT * ARROW_HEIGHT;
  /**
   * The length of the side of the arrow.
   */
  static final double ARROW_SIDE = ARROW_HEIGHT / ARROW_COS;

  static final int CLICK_TOLERANCE = 2;
  static final int CLICK_TOLERANCE_SQ = CLICK_TOLERANCE * CLICK_TOLERANCE;
}
