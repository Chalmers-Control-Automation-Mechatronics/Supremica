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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.module.GroupNodeProxy;


public class GroupNodeProxyShape extends AbstractProxyShape
{

  //########################################################################
  //# Constructors
  public GroupNodeProxyShape(final GroupNodeProxy group)
  {
    super(group);
    mRect = new Rectangle2D.Double();
    mRect.setRect(group.getGeometry().getRectangle());
    mHandles = new ArrayList<Handle>(8);
    final Point2D min =
      new Point2D.Double(getShape().getMinX(), getShape().getMinY());
    final Point2D c =
      new Point2D.Double(getShape().getCenterX(), getShape().getCenterY());
    final Point2D max =
      new Point2D.Double(getShape().getMaxX(), getShape().getMaxY());
    mHandles.add(new DefaultHandle(min, Handle.HandleType.NW));
    mHandles
      .add(new DefaultHandle(c.getX(), min.getY(), Handle.HandleType.N));
    mHandles
      .add(new DefaultHandle(max.getX(), min.getY(), Handle.HandleType.NE));
    mHandles
      .add(new DefaultHandle(min.getX(), c.getY(), Handle.HandleType.W));
    mHandles
      .add(new DefaultHandle(max.getX(), c.getY(), Handle.HandleType.E));
    mHandles
      .add(new DefaultHandle(min.getX(), max.getY(), Handle.HandleType.SW));
    mHandles
      .add(new DefaultHandle(c.getX(), max.getY(), Handle.HandleType.S));
    mHandles.add(new DefaultHandle(max, Handle.HandleType.SE));
  }

  //########################################################################
  //# Simple Access
  @Override
  public List<Handle> getHandles()
  {
    return mHandles;
  }

  @Override
  public Rectangle2D getShape()
  {
    return mRect;
  }

  @Override
  public GroupNodeProxy getProxy()
  {
    return (GroupNodeProxy) super.getProxy();
  }

  @Override
  public boolean isClicked(final Point point)
  {
    for (final Handle h : getHandles()) {
      if (h.isClicked(point)) {
        return true;
      }
    }
    final Rectangle2D rect = new Rectangle2D.Double(point.x - 2, point.y - 2, 4, 4);
    return getShape().intersects(rect) && !getShape().contains(rect);
  }

  @Override
  public void draw(final Graphics2D g, final RenderingInformation status)
  {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);

    super.draw(g, status);

    // Thicker line for the rectangle!
    g.setColor(status.getColor());
    g.setStroke(DOUBLESTROKE);
    g.draw(getShape());

    if (status.showHandles()) {
      for (final RendererShape handle : getHandles()) {
        handle.draw(g, status);
      }
    }
  }

  private final Rectangle2D mRect;
  private final List<Handle> mHandles;
}
