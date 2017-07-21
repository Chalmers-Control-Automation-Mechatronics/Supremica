//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.AttributedString;

import net.sourceforge.waters.model.base.Proxy;


public class ForeachLabelShape extends AbstractLabelShape
{

  //##########################################################################
  //# Constructor
  /**
   * Creates a label with attributed text.
   *
   * @param proxy    The proxy associated with this label
   * @param x        The x-coordinate of the top left corner
   * @param y        The y-coordinate of the top left corner
   * @param font     The size of the font is used to compute the baseline
   * @param text     The attributed text
   */
  public ForeachLabelShape(final Proxy proxy, final int x, final int y,
                           final Font font, final AttributedString text)
  {
    super(proxy);
    mText = text;
    final FontRenderContext context = new FontRenderContext(null, true, true);
    final TextLayout layout = new TextLayout(text.getIterator(), context);
    final Rectangle2D rect = layout.getBounds();
    mBounds = new RoundRectangle2D.Double(x, y, rect.getWidth() + 4,
            rect.getHeight() + 4, CORNER_RADIUS, CORNER_RADIUS);
    mPoint = new Point(x + 2, y + font.getSize());
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
    graphics.setColor(status.getColor());
    graphics.drawString(mText.getIterator(), mPoint.x, mPoint.y);
  }


  //##########################################################################
  //# Data Members
  private final Point mPoint;
  private final RoundRectangle2D mBounds;
  private final AttributedString mText;

}
