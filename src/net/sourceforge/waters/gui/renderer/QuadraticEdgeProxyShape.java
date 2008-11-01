//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   QuadraticEdgeProxyShape
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

import net.sourceforge.waters.model.module.EdgeProxy;


class QuadraticEdgeProxyShape
  extends EdgeProxyShape
{

  //#########################################################################
  //# Constructors
  QuadraticEdgeProxyShape(final EdgeProxy edge)
  {
    super(edge);
    mControl = GeometryTools.getControlPoint1(edge);
    mStart = GeometryTools.getRadialStartPoint(edge, mControl);
    mEnd = GeometryTools.getRadialEndPoint(edge, mControl);
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
