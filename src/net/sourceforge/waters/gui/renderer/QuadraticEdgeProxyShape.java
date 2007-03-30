//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   QuadraticEdgeProxyShape
//###########################################################################
//# $Id: QuadraticEdgeProxyShape.java,v 1.2 2007-03-30 11:50:44 avenir Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;

import net.sourceforge.waters.model.module.EdgeProxy;


class QuadraticEdgeProxyShape
  extends EdgeProxyShape
{

  //#########################################################################
  //# Constructors
  QuadraticEdgeProxyShape(final EdgeProxy edge)
  {
    super(edge);
    final Point2D start = GeometryTools.getStartPoint(edge);
    final Point2D end = GeometryTools.getEndPoint(edge);
    mControl = GeometryTools.getControlPoint1(edge);
    mStart = GeometryTools.getRadialStartPoint(edge, mControl);
    mEnd = GeometryTools.getRadialEndPoint(edge, mControl);
    mCurve = new QuadCurve2D.Double(mStart.getX(), mStart.getY(),
				    mControl.getX(), mControl.getY(), 
				    mEnd.getX(), mEnd.getY());
    createHandles();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.RendererShape
  // This method reduces the functionality of the superclass, where
  // getShape() is already defined. Is it on purpose? 
  // This functionality seems to be needed when 
  // the shape of the curve together with the attached arrowhead iswanted. 
//   public QuadCurve2D getShape()
//   {
//     return mCurve;
//   }

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

  Point2D getEndDirection()
  {
    return GeometryTools.getNormalizedDirection(mControl, mEnd);
  }


  //#########################################################################
  //# Data Members
  private final Point2D mStart;
  private final Point2D mEnd;
  private final Point2D mControl;
  private final QuadCurve2D mCurve;

}
