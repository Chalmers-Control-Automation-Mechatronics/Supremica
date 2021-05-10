//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
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
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.getValue();
    final Point2D border = GeometryTools.getRadialPoint(node, normdir, radius);
    final double x = border.getX() + INITARROW_LENGTH * dx;
    final double y = border.getY() + INITARROW_LENGTH * dy;
    final Point2D outer = new Point2D.Double(x, y);
    mLine = new Line2D.Double(outer, border);
    normdir.setLocation(-dx, -dy);
    final GeneralPath arrow = EdgeProxyShape.createArrowHead(border, normdir);
    mShape = new GeneralPath(GeneralPath.WIND_NON_ZERO, 2);
    mShape.append(mLine, false);
    mShape.append(arrow, false);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.InitialStateHandle
  @Override
  public HandleType getType()
  {
    return HandleType.INITIAL;
  }


  //#########################################################################
  //# Drawing
  @Override
  public GeneralPath getShape()
  {
    return mShape;
  }

  @Override
  public void draw(final Graphics2D g2d, final RenderingInformation status)
  {
    super.draw(g2d, status);
    g2d.fill(getShape());
  }

  @Override
  public boolean isClicked(final Point point)
  {
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.getValue() + 1;
    final double distSq = mLine.ptSegDistSq(point);
    final Point2D center = GeometryTools.getPosition(mNode);
    return
      distSq < CLICK_TOLERANCE_SQ &&
      center.distanceSq(point) > radius * radius;
  }


  //#########################################################################
  //# Data Members
  private final GeneralPath mShape;
  private final Line2D mLine;
  private final SimpleNodeProxy mNode;


  //#########################################################################
  //# Class Constants
  static final double INITARROW_LENGTH = 18.0;
  private static final double CLICK_TOLERANCE_SQ = 6.0 * 6.0;

}
