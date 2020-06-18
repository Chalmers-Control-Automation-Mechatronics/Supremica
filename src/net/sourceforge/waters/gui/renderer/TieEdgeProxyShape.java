//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.subject.module.GeometryTools;

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
    final int noderadius = Config.GUI_EDITOR_NODE_RADIUS.getValue();
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
  @Override
  public boolean isClicked(final Point point)
  {
    if (getClickedHandle(point) != null) {
      return true;
    } else if (!isInClickBounds(point)) {
      return false;
    } else {
      final Rectangle rect =
        new Rectangle(point.x - CLICK_TOLERANCE, point.y - CLICK_TOLERANCE,
                      2 * CLICK_TOLERANCE, 2 * CLICK_TOLERANCE);
      return mTie.intersects(rect) && !mTie.contains(rect);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.EdgeProxyShape
  @Override
  Shape getCurve()
  {
    return mTie;
  }

  @Override
  Point2D getStartPoint()
  {
    return mStart;
  }

  @Override
  Point2D getEndPoint()
  {
    return mEnd;
  }

  @Override
  Point2D getTurningPoint()
  {
    return mControl;
  }

  @Override
  Point2D getMidDirection()
  {
    return mMidDirection;
  }

  @Override
  Point2D getInnerArrowTipPoint()
  {
    return mInnerArrowTipPoint;
  }

  @Override
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
