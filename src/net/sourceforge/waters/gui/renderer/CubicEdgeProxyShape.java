//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   QuadraticEdgeProxyShape
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import net.sourceforge.waters.model.module.EdgeProxy;


class CubicEdgeProxyShape
  extends EdgeProxyShape
{

  //#########################################################################
  //# Constructors
  CubicEdgeProxyShape(final EdgeProxy edge)
  {
    super(edge);
    final Point2D[] controls = GeometryTools.getCubicBezierControlPoints(edge);
    mControl1 = controls[0];
    mControl2 = controls[1];
    mStart = GeometryTools.getRadialStartPoint(edge, mControl1);
    mEnd = GeometryTools.getRadialEndPoint(edge, mControl2);
    mCurve = new CubicCurve2D.Double(mStart.getX(), mStart.getY(),
                                     mControl1.getX(), mControl1.getY(),
                                     mControl2.getX(), mControl2.getY(),
                                     mEnd.getX(), mEnd.getY());
    calculateMidPoint();
    //createHandles();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.RendererShape
  public Rectangle2D getBounds2D()
  {
    return GeometryTools.getCubicBoundingBox
      (mStart, mControl1, mControl2, mEnd);
  }

  public boolean isClicked(final int x, final int y)
  {
    return false;
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
    final double x = 0.125 * (mStart.getX() +
                              3.0 * (mControl1.getX()  + mControl2.getX()) +
                              mEnd.getX());
    final double y = 0.125 * (mStart.getY() +
                              3.0 * (mControl1.getY()  + mControl2.getY()) +
                              mEnd.getY());
    return new Point2D.Double(x, y);
  }

  Point2D getInnerArrowTipPoint()
  {
    return mArrowTip;
  }

  Point2D getMidDirection()
  {
    return mMidDirection;
  }

  Point2D getEndDirection()
  {
    return GeometryTools.getNormalizedDirection(mControl2, mEnd);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void calculateMidPoint()
  {
    final Point2D midpoint = getTurningPoint();
    final double dx =
      mEnd.getX() + mControl2.getX() - mControl1.getX() - mStart.getX();
    final double dy =
      mEnd.getY() + mControl2.getY() - mControl1.getY() - mStart.getY();
    final boolean d =
      Math.abs(dx) > GeometryTools.EPSILON ||
      Math.abs(dy) > GeometryTools.EPSILON;
    if (d) {
      mMidDirection = new Point2D.Double(dx, dy);
      GeometryTools.normalize(mMidDirection);
    } else {
      mMidDirection = super.getMidDirection();
    }
    final double dist = 0.5 * EdgeProxyShape.ARROW_HEIGHT;
    double x = midpoint.getX() + dist * mMidDirection.getX();
    double y = midpoint.getY() + dist * mMidDirection.getY();
    if (d) {
      final double dt = dist / Math.sqrt(dx * dx + dy * dy);
      final double t1 = 0.5 + dt;
      final double t2 = 0.5 - dt;
      final double xalt =
        mStart.getX() * t2 * t2 * t2 + mControl1.getX() * t2 * t2 * t1 +
        mControl2.getX() * t2 * t1 * t1 + mEnd.getX() * t1 * t1 * t1;
      final double yalt =
        mStart.getY() * t2 * t2 * t2 + mControl1.getY() * t2 * t2 * t1 +
        mControl2.getY() * t2 * t1 * t1 + mEnd.getY() * t1 * t1 * t1;
      final double dxa = x - xalt;
      final double dya = y - yalt;
      if (dxa * dxa + dya * dya < 4.0) {
        x = xalt;
        y = yalt;
      }
    }
    mArrowTip = new Point2D.Double(x, y);
  }


  //#########################################################################
  //# Data Members
  private final Point2D mStart;
  private final Point2D mEnd;
  private final Point2D mControl1;
  private final Point2D mControl2;
  private final CubicCurve2D mCurve;

  private Point2D mMidDirection;
  private Point2D mArrowTip;

}
