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
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.model.module.LabelBlockProxy;


public class LabelBlockProxyShape
  extends AbstractProxyShape
{

  //#########################################################################
  //# Constructor
  public LabelBlockProxyShape(final LabelBlockProxy block,
                              final Rectangle2D textBounds)
  {
    super(block);
    mTextBounds = textBounds;
    final double x0 = textBounds.getMinX() - INSETS;
    final double y0 = textBounds.getMinY() - INSETS;
    final double width = textBounds.getWidth() + 2 * INSETS;
    final double height = textBounds.getHeight() + 2 * INSETS;
    mShape =
      new RoundRectangle2D.Double(x0, y0, width, height, ARC_SIZE, ARC_SIZE);
  }


  //#########################################################################
  //# Simple Access
  public Rectangle2D getTextBounds()
  {
    return mTextBounds;
  }

  boolean shouldBeDrawn()
  {
    return !getProxy().getEventIdentifierList().isEmpty();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.RendererShape
  @Override
  public Shape getShape()
  {
    return mShape;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.ProxyShape
  @Override
  public LabelBlockProxy getProxy()
  {
    return (LabelBlockProxy) super.getProxy();
  }


  //#########################################################################
  //# Drawing
  @Override
  public void draw(final Graphics2D g2d, final RenderingInformation status)
  {
    final Shape shape = getShape();
    if (shouldBeDrawn()) {
      if (status.isFocused()) {
        g2d.setColor(status.getShadowColor());
        g2d.setStroke(SHADOWSTROKE);
        g2d.fill(shape);
      }
      if (status.isSelected()) {
        g2d.setColor(status.getColor());
        g2d.setStroke(BASICSTROKE);
        g2d.draw(shape);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final RoundRectangle2D mShape;
  private final Rectangle2D mTextBounds;


  //#########################################################################
  //# Class Constants
  public static final int INSETS = 2;
  public static final int ARC_SIZE = 8;
  public static final int INDENTATION = 10;

  public static final int DEFAULT_OFFSET_X = 0;
  public static final int DEFAULT_OFFSET_Y = 10;
  public static final Point2D DEFAULT_OFFSET =
    new Point(DEFAULT_OFFSET_X, DEFAULT_OFFSET_Y);

}
