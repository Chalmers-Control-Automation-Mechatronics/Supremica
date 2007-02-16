//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   StraightEdgeProxyShape
//###########################################################################
//# $Id: StraightEdgeProxyShape.java,v 1.1 2007-02-16 03:00:42 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
    
import net.sourceforge.waters.model.module.EdgeProxy;



class StraightEdgeProxyShape
  extends EdgeProxyShape
{

  //#########################################################################
  //# Constructors
  StraightEdgeProxyShape(final EdgeProxy edge)
  {
    super(edge);
    final Point2D start = GeometryTools.getStartPoint(edge);
    final Point2D end = GeometryTools.getEndPoint(edge);
    mStart = GeometryTools.getRadialStartPoint(edge, end);
    mEnd = GeometryTools.getRadialEndPoint(edge, start);
    mLine = new Line2D.Double(mStart, mEnd);
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
      return mLine.ptLineDistSq(x, y) <= EdgeProxyShape.CLICK_TOLERANCE2;
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


  //#########################################################################
  //# Data Members
  private final Point2D mStart;
  private final Point2D mEnd;
  private final Line2D mLine;

}
