//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.Graphics2D;


public class DefaultHandle
  extends AbstractRendererShape
  implements Handle
{

  //########################################################################
  //# Constructors
  public DefaultHandle(final Point2D centre, final HandleType type)
  {
    this(centre.getX(), centre.getY(), type);
  }

  public DefaultHandle(final double x, final double y, final HandleType type)
  {
    final int x0 = (int) Math.round(x);
    final int y0 = (int) Math.round(y);
    mShape = new Rectangle(x0 - HALF_WIDTH, y0 - HALF_WIDTH, WIDTH, WIDTH);
    mHandleType = type;
  }


  //########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.DefaultHandle
  public HandleType getType()
  {
    return mHandleType;
  }


  //########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.RendererShape
  public Rectangle getShape()
  {
    return mShape;
  }

  public boolean isClicked(final int x, final int y)
  {
    return
      x + CLICK_TOLERANCE >= mShape.x &&
      x - CLICK_TOLERANCE <= mShape.x + mShape.width &&
      y + CLICK_TOLERANCE >= mShape.y &&
      y - CLICK_TOLERANCE <= mShape.y + mShape.height;
  }


  //########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.AbstractRendererShape
  public void draw(final Graphics2D g2d, final RenderingInformation status)
  {
    super.draw(g2d, status);
    g2d.fill(mShape);
  }


  //########################################################################
  //# Data Members
  private final Rectangle mShape;
  private final HandleType mHandleType;


  //########################################################################
  //# Class Constants
  public static final int HALF_WIDTH = 2;
  public static final int WIDTH = 2 * HALF_WIDTH;
  public static final int CLICK_TOLERANCE = 2;

}
