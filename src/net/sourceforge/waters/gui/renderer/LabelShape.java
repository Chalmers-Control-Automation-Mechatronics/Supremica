//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   LabelShape
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.model.base.Proxy;


public class LabelShape extends AbstractProxyShape
{

  //##########################################################################
  //# Constructors
  public LabelShape(final Proxy proxy, final int x, final int y,
                    final Font font, final String aux /* unused? */)
  {
    this(proxy, x, y, font);
  }

  public LabelShape(final Proxy proxy,
                    final int x, final int y, final Font font)
  {
    super(proxy);
    mFont = font;
    mPoint = new Point(x + 2, y + (font.getSize()));
    mName = proxy.toString();
    final TextLayout layout =
        new TextLayout(mName, mFont, new FontRenderContext(null, true, true));
    final Rectangle2D rect = layout.getBounds();
    rect.setRect(x, y, rect.getWidth() + 4, rect.getHeight() + 4);
    mBounds =
        new RoundRectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(),
                                    rect.getHeight(), ARC_RADIUS, ARC_RADIUS);
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.ProxyShape
  public RoundRectangle2D getShape()
  {
    return mBounds;
  }

  public void draw(final Graphics2D graphics,
                   final RenderingInformation status)
  {
    if (status.isFocused()) {
      graphics.setColor(status.getShadowColor());
      graphics.fill(getShape());
    }
    graphics.setFont(mFont);
    graphics.setColor(status.getColor());
    final int x = (int) Math.round(mPoint.getX());
    final int y = (int) Math.round(mPoint.getY());
    graphics.drawString(mName, x, y);
  }


  //##########################################################################
  //# Data Members
  private final Point mPoint;
  private final RoundRectangle2D mBounds;
  private final Font mFont;
  private final String mName;


  //##########################################################################
  //# Class Constants
  private static double ARC_RADIUS = 5;

}
