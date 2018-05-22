//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


public class LabelShape extends AbstractLabelShape
{

  //##########################################################################
  //# Constructors
  public LabelShape(final Proxy proxy, final int x, final int y,
                    final Font font)
  {
    this(proxy, x, y, font, proxy.toString());
  }

  public LabelShape(final Proxy proxy, final int x, final int y,
                    final Font font, final String name)
  {
    super(proxy);
    mFont = font;
    mPoint = new Point(x + 2, y + font.getSize());
    mName = name;
    final Rectangle2D rect = calculateBounds(mName, mFont);
    mBounds = new RoundRectangle2D.Double(x, y, rect.getWidth() + 4,
        rect.getHeight() + 4, CORNER_RADIUS, CORNER_RADIUS);
    mUnderline = new UnderlineShape(proxy, mPoint.x + rect.getMinX(),
        mPoint.y, rect.getWidth(), font);
  }

  public LabelShape(final SimpleNodeProxy node, final Font font)
  {
    super(node.getLabelGeometry());
    mFont = font;
    final Point2D nodepos = node.getPointGeometry().getPoint();
    final LabelGeometryProxy geom = node.getLabelGeometry();
    final Point2D offset = (geom == null) ? DEFAULT_OFFSET : geom.getOffset();
    final int x = (int) Math.round(nodepos.getX() + offset.getX());
    final int y = (int) Math.round(nodepos.getY() + offset.getY());
    mPoint = new Point(x + 2, y + font.getSize());
    mName = node.getName();
    final Rectangle2D rect = calculateBounds(mName, mFont);
    // Unfortunately there are some ac hoc constants here to get
    // these to conform with the corresponding label blocks
    mBounds = new RoundRectangle2D.Double(x, y - 2, rect.getWidth() + 5,
        rect.getHeight() + 10, CORNER_RADIUS, CORNER_RADIUS);
    mUnderline = new UnderlineShape(geom, mPoint.x + rect.getMinX(),
        mPoint.y, rect.getWidth(), font);
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.ProxyShape
  @Override
  public RoundRectangle2D getShape()
  {
    return mBounds;
  }

  @Override
  public void draw(final Graphics2D graphics,final RenderingInformation status)
  {
    if (status.isFocused()) {
      graphics.setColor(status.getShadowColor());
      graphics.fill(getShape());
    }
    graphics.setFont(mFont);
    graphics.setColor(status.getColor());
    graphics.drawString(mName, mPoint.x, mPoint.y);
    mUnderline.draw(graphics, status);
  }


  //##########################################################################
  //# Auxiliary Methods
  protected Rectangle2D calculateBounds(final String name, final Font font)
  {
    final FontRenderContext context = new FontRenderContext(null, true, true);
    final TextLayout layout = new TextLayout(name, font, context);
    return layout.getBounds();
  }


  //##########################################################################
  //# Data Members
  private final Point mPoint;
  private final RoundRectangle2D mBounds;
  private final Font mFont;
  private final String mName;
  private final UnderlineShape mUnderline;


  //##########################################################################
  //# Class Constants
  public static final int DEFAULT_OFFSET_X = 0;
  public static final int DEFAULT_OFFSET_Y = 10;
  public static final Point DEFAULT_OFFSET =
      new Point(DEFAULT_OFFSET_X, DEFAULT_OFFSET_Y);

}
