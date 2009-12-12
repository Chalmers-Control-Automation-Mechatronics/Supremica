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
    mArrowTip = getTurningPoint();
    //mArrowTip = calculateInnerArrowTipPosition();
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
    // *** BUG ***
    // Not correct---must calculate derivative ...
    return GeometryTools.getNormalizedDirection(mControl1, mControl2);
  }

  Point2D getEndDirection()
  {
    return GeometryTools.getNormalizedDirection(mControl2, mEnd);
  }


  //#########################################################################
  //# Auxiliary Methods
  /*
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
  */


  //#########################################################################
  //# Data Members
  private final Point2D mStart;
  private final Point2D mEnd;
  private final Point2D mControl1;
  private final Point2D mControl2;
  private final CubicCurve2D mCurve;
  private final Point2D mArrowTip;

}
