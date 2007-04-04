//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   InitialStateHandle
//###########################################################################
//# $Id: InitialStateHandle.java,v 1.9 2007-04-04 07:24:01 flordal Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.geom.Point2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.Graphics2D;

import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;


class InitialStateHandle
  extends AbstractRendererShape
  implements Handle
{
    
  //#########################################################################
  //# Constructors
  public InitialStateHandle(final SimpleNodeProxy node)
  {
    final PointGeometryProxy geo = node.getInitialArrowGeometry();
    double dx = -1.0;
    double dy = -1.0;
    if (geo != null) {
      final Point2D dir = geo.getPoint();
      final double dirx = dir.getX();
      final double diry = dir.getY();
      final double len = Math.sqrt(dirx * dirx + diry * diry);
      if (len > GeometryTools.EPSILON) {
	dx = dirx / len;
	dy = diry / len;
      }
    }
    final Point2D normdir = new Point2D.Double(dx, dy);
    final Point2D border = GeometryTools.getRadialPoint(node, normdir);
    final double x = border.getX() + INITARROW_LENGTH * dx;
    final double y = border.getY() + INITARROW_LENGTH * dy;
    final Point2D outer = new Point2D.Double(x, y);
    final Line2D line = new Line2D.Double(outer, border);
    normdir.setLocation(-dx, -dy);
    final GeneralPath arrow = EdgeProxyShape.createArrowHead(border, normdir);
    mShape = new GeneralPath(GeneralPath.WIND_NON_ZERO, 2);
    //mShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
    mShape.append(line, false);
    mShape.append(arrow, false);
  }

    
  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.InitialStateHandle
  public HandleType getType()
  {
    return HandleType.INITIAL;
  }
  
  
  //#########################################################################
  //# Drawing
  public GeneralPath getShape()
  {
    return mShape;
  }
    
  public void draw(final Graphics2D g2d, final RenderingInformation status)
  {
    super.draw(g2d, status);
    g2d.fill(getShape());
  }
    
  public boolean isClicked(final int x, final int y)
  {
    return mShape.intersects(x - 1, y - 1, 2, 2);
  }
    

  //#########################################################################
  //# Data Members
  private final GeneralPath mShape;


  //#########################################################################
  //# Class Constants
  static final double INITARROW_LENGTH = 18.0;

}
