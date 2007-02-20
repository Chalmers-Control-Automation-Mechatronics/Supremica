//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   DefaultHandle
//###########################################################################
//# $Id: DefaultHandle.java,v 1.5 2007-02-20 22:48:11 robi Exp $
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
