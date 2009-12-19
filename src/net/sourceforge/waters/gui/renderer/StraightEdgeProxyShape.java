//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   StraightEdgeProxyShape
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GeometryTools;

import org.supremica.properties.Config;



class StraightEdgeProxyShape
  extends EdgeProxyShape
{

  //#########################################################################
  //# Constructors
  StraightEdgeProxyShape(final EdgeProxy edge)
  {
    super(edge);
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.get();
    final Point2D start = GeometryTools.getStartPoint(edge);
    final Point2D end = GeometryTools.getEndPoint(edge);
    mStart = GeometryTools.getRadialStartPoint(edge, end, radius);
    mEnd = GeometryTools.getRadialEndPoint(edge, start, radius);
    mLine = new Line2D.Double(mStart, mEnd);
    final double distance = mStart.distance(mEnd);
    if (distance == 0.0) {
      mArrowTip = start;
    } else {
      final double t1 =
        0.5 * (distance - EdgeProxyShape.ARROW_HEIGHT) / distance;
      final double t2 = 1.0 - t1;
      final double x = t1 * mStart.getX() + t2 * mEnd.getX();
      final double y = t1 * mStart.getY() + t2 * mEnd.getY();
      mArrowTip = new Point2D.Double(x, y);
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
      return mLine.ptLineDistSq(x, y) <= EdgeProxyShape.CLICK_TOLERANCE_SQ;
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.EdgeProxyShape
  Shape getCurve()
  {
    return mLine;
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
    return getMidPoint();
  }

  Point2D getInnerArrowTipPoint()
  {
    return mArrowTip;
  }


  //#########################################################################
  //# Data Members
  private final Point2D mStart;
  private final Point2D mEnd;
  private final Line2D mLine;
  private final Point2D mArrowTip;

}
