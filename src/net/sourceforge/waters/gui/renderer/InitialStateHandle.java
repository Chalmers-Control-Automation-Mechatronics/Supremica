//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   InitialStateHandle
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.geom.Point2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.Graphics2D;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.subject.module.GeometryTools;

import org.supremica.properties.Config;


class InitialStateHandle
  extends AbstractRendererShape
  implements Handle
{

  //#########################################################################
  //# Constructors
  public InitialStateHandle(final SimpleNodeProxy node)
  {
    mNode = node;
    final PointGeometryProxy geo = node.getInitialArrowGeometry();
    final double dx;
    final double dy;
    if (geo != null) {
      final Point2D dir = geo.getPoint();
      final double dirx = dir.getX();
      final double diry = dir.getY();
      final double len = Math.sqrt(dirx * dirx + diry * diry);
      if (len > GeometryTools.EPSILON) {
        dx = dirx / len;
        dy = diry / len;
      } else {
        dx = dy = -0.5 * GeometryTools.SQRT2;
      }
    } else {
      dx = dy = -0.5 * GeometryTools.SQRT2;
    }
    final Point2D normdir = new Point2D.Double(dx, dy);
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.get();
    final Point2D border = GeometryTools.getRadialPoint(node, normdir, radius);
    final double x = border.getX() + INITARROW_LENGTH * dx;
    final double y = border.getY() + INITARROW_LENGTH * dy;
    final Point2D outer = new Point2D.Double(x, y);
    mLine = new Line2D.Double(outer, border);
    normdir.setLocation(-dx, -dy);
    final GeneralPath arrow = EdgeProxyShape.createArrowHead(border, normdir);
    mShape = new GeneralPath(GeneralPath.WIND_NON_ZERO, 2);
    //mShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
    mShape.append(mLine, false);
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

  @Override
  public boolean isClicked(final int x, final int y)
  {
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.get() + 1;
    final double dist = mLine.ptSegDist(x, y);
    final Point2D center = GeometryTools.getPosition(mNode);
    final boolean ret = dist < 6 && (center.distanceSq(x, y) > radius*radius);
    return ret;
  }


  //#########################################################################
  //# Data Members
  private final GeneralPath mShape;
  private final Line2D mLine;
  private final SimpleNodeProxy mNode;

  //#########################################################################
  //# Class Constants
  static final double INITARROW_LENGTH = 18.0;

}
