//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.subject.module.GeometryTools;

import org.supremica.properties.Config;



class StraightEdgeProxyShape
  extends EdgeProxyShape
{

  //#########################################################################
  //# Constructors
  StraightEdgeProxyShape(final EdgeProxy edge)
  {
    super(edge);
    final int radius = Config.GUI_EDITOR_NODE_RADIUS.getValue();
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
  @Override
  public boolean isClicked(final Point point)
  {
    if (getClickedHandle(point) != null) {
      return true;
    } else if (!isInClickBounds(point)) {
      return false;
    } else {
      return mLine.ptLineDistSq(point) <= EdgeProxyShape.CLICK_TOLERANCE_SQ;
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.EdgeProxyShape
  @Override
  Shape getCurve()
  {
    return mLine;
  }

  @Override
  Point2D getStartPoint()
  {
    return mStart;
  }

  @Override
  Point2D getEndPoint()
  {
    return mEnd;
  }

  @Override
  Point2D getTurningPoint()
  {
    return getMidPoint();
  }

  @Override
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
