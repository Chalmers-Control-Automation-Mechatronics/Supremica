//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   TieEdgeProxyShape
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GeometryTools;

import org.supremica.properties.Config;


class TieEdgeProxyShape
  extends EdgeProxyShape
{

  //#########################################################################
  //# Constructors
  TieEdgeProxyShape(final EdgeProxy edge)
  {
    super(edge);
    final Point2D root = GeometryTools.getStartPoint(edge);
    final double x0 = root.getX();
    final double y0 = root.getY();
    mControl = GeometryTools.getTurningPoint1(edge);
    final double dx = mControl.getX() - x0;
    final double dy = mControl.getY() - y0;
    mMidDirection = new Point2D.Double(-dy, dx);
    GeometryTools.normalize(mMidDirection);
    final double dist = Math.sqrt(dx * dx + dy * dy);
    final double radius = GeometryTools.SELFLOOP_RADIUS * dist;
    final double diameter = 2.0 * radius;
    final double factor =
      dist > GeometryTools.EPSILON ? (dist - radius) / dist : 0.0;
    final double cx = x0 + factor * dx;
    final double cy = y0 + factor * dy;
    final double ax = cx - radius;
    final double ay = cy - radius;
    final double startangle =
      Math.atan2(dy, dx) + Math.PI + 0.5 * GeometryTools.SELFLOOP_APERTURE;
    final double startdeg = 90.0 - Math.toDegrees(startangle);
    mArc = new Arc2D.Double(ax, ay, diameter, diameter, startdeg,
                            GeometryTools.SELFLOOP_EXTENT, Arc2D.OPEN);
    final int noderadius = Config.GUI_EDITOR_NODE_RADIUS.get();
    final Point2D tangent1 = mArc.getEndPoint();
    final Point2D tangent2 = mArc.getStartPoint();
    mStart = GeometryTools.getRadialStartPoint(edge, tangent1, noderadius);
    mEnd = GeometryTools.getRadialEndPoint(edge, tangent2, noderadius);
    final Line2D line1 = new Line2D.Double(mStart, tangent1);
    final Line2D line2 = new Line2D.Double(tangent2, mEnd);
    mTie = new GeneralPath(GeneralPath.WIND_NON_ZERO, 3);
    mTie.append(line2, false);
    mTie.append(mArc , true);
    mTie.append(line1, true);
    if (radius > GeometryTools.EPSILON) {
      // Looks better to move inner arrow tip slightly away from
      // the turning point.
      final double rotsin = 0.5 * EdgeProxyShape.ARROW_HEIGHT / radius;
      final double rotcos = Math.sqrt(1.0 - rotsin * rotsin);
      final double dx1 = mControl.getX() - cx;
      final double dy1 = mControl.getY() - cy;
      final double dx2 = dx1 * rotcos - dy1 * rotsin;
      final double dy2 = dx1 * rotsin + dy1 * rotcos;
      mInnerArrowTipPoint = new Point2D.Double(cx + dx2, cy + dy2);
    } else {
      mInnerArrowTipPoint = mControl;
    }
    createHandles();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.RendererShape
  public boolean isClicked(final int x, final int y)
  {
    if (getClickedHandle(x, y) != null) {
      return true;
    } else if (!isInClickBounds(x, y)) {
      return false;
    } else {
      final Rectangle rect =
        new Rectangle(x - CLICK_TOLERANCE, y - CLICK_TOLERANCE,
                      2 * CLICK_TOLERANCE, 2 * CLICK_TOLERANCE);
      return mTie.intersects(rect) && !mTie.contains(rect);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.EdgeProxyShape
  Shape getCurve()
  {
    return mTie;
  }

  Point2D getStartPoint()
  {
    return mStart;
  }

  Point2D getEndPoint()
  {
    return mEnd;
  }

  Point2D getTurningPoint()
  {
    return mControl;
  }

  Point2D getMidDirection()
  {
    return mMidDirection;
  }

  Point2D getInnerArrowTipPoint()
  {
    return mInnerArrowTipPoint;
  }

  Point2D getEndDirection()
  {
    final Point2D start = mArc.getStartPoint();
    return GeometryTools.getNormalizedDirection(start, mEnd);
  }


  //#########################################################################
  //# Data Members
  private final Point2D mStart;
  private final Point2D mEnd;
  private final Point2D mControl;
  private final Point2D mMidDirection;
  private final Point2D mInnerArrowTipPoint;
  private final Arc2D mArc;
  private final GeneralPath mTie;

}
