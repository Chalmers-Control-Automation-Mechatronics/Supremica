//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.subject.module.GeometryTools;

import org.supremica.properties.Config;


class QuadraticEdgeProxyShape
  extends EdgeProxyShape
{

  //#########################################################################
  //# Constructors
  QuadraticEdgeProxyShape(final EdgeProxy edge)
  {
    super(edge);
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.get();
    mControl = GeometryTools.getSingleBezierControlPoint(edge);
    mStart = GeometryTools.getRadialStartPoint(edge, mControl, radius);
    mEnd = GeometryTools.getRadialEndPoint(edge, mControl, radius);
    mCurve = new QuadCurve2D.Double(mStart.getX(), mStart.getY(),
				    mControl.getX(), mControl.getY(),
				    mEnd.getX(), mEnd.getY());
    mArrowTip = calculateInnerArrowTipPosition();
    createHandles();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.RendererShape
  public Rectangle2D getBounds2D()
  {
    return GeometryTools.getQuadraticBoundingBox(mStart, mControl, mEnd);
  }

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
      if (!mCurve.intersects(rect) || mCurve.contains(rect)) {
	return false;
      }
      final Line2D base = new Line2D.Double(mStart, mEnd);
      return !base.intersects(rect);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.EdgeProxyShape
  Shape getCurve()
  {
    return mCurve;
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
    final double x =
      0.25 * (mStart.getX() + 2.0 * mControl.getX() + mEnd.getX());
    final double y =
      0.25 * (mStart.getY() + 2.0 * mControl.getY() + mEnd.getY());
    return new Point2D.Double(x, y);
  }

  Point2D getInnerArrowTipPoint()
  {
    return mArrowTip;
  }

  Point2D getEndDirection()
  {
    return GeometryTools.getNormalizedDirection(mControl, mEnd);
  }


  //#########################################################################
  //# Auxiliary Methods
  private Point2D calculateInnerArrowTipPosition()
  {
    final Point2D turn = getTurningPoint();
    final Point2D dir = getMidDirection();
    final double dist = 0.5 * EdgeProxyShape.ARROW_HEIGHT;
    final double x = turn.getX() + dist * dir.getX();
    final double y = turn.getY() + dist * dir.getY();
    final Point2D rawtip = new Point2D.Double(x, y);
    if (mStart.distanceSq(mEnd) < EdgeProxyShape.ARROW_HEIGHT_SQ) {
      return rawtip;
    } else {
      return GeometryTools.findClosestPointOnQuadratic
        (mStart, mControl, mEnd, rawtip);
    }
  }


  //#########################################################################
  //# Data Members
  private final Point2D mStart;
  private final Point2D mEnd;
  private final Point2D mControl;
  private final QuadCurve2D mCurve;
  private final Point2D mArrowTip;

}
