//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   LabelProxyShape
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


public class LabelProxyShape extends AbstractProxyShape
{

  //##########################################################################
  //# Constructor
  public LabelProxyShape(final SimpleNodeProxy node, final Font font)
  {
    super(node.getLabelGeometry());
    mFont = font;
    final Point2D nodepos = node.getPointGeometry().getPoint();
    final Point2D offset =
        getProxy() == null ? DEFAULT_OFFSET : getProxy().getOffset();
    final int x = (int) Math.round(nodepos.getX() + offset.getX());
    final int y = (int) Math.round(nodepos.getY() + offset.getY());
    mPoint = new Point(x + 2, y + font.getSize());
    mName = node.getName();
    final TextLayout layout =
        new TextLayout(mName, mFont, new FontRenderContext(null, true, true));
    final Rectangle2D rect = layout.getBounds();
    rect.setRect(x, y, rect.getWidth() + 4, rect.getHeight() + 6);
    // Unfortunately there are some ac hoc constants here to get
    // these to conform with the corresponding label blocks
    mBounds =
        new RoundRectangle2D.Double(rect.getX(), rect.getY() - 2,
                                    rect.getWidth() + 1,
                                    rect.getHeight() + 4,
                                    CORNER_RADIUS, CORNER_RADIUS);
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.ProxyShape
  public RoundRectangle2D getShape()
  {
    return mBounds;
  }

  public LabelGeometryProxy getProxy()
  {
    return (LabelGeometryProxy) super.getProxy();
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
  private final Font mFont;
  private final String mName;
  private final RoundRectangle2D mBounds;
  private final Point mPoint;


  //##########################################################################
  //# Class Constants
  public static final int DEFAULT_OFFSET_X = 0;
  public static final int DEFAULT_OFFSET_Y = 10;
  public static final Point DEFAULT_OFFSET =
      new Point(DEFAULT_OFFSET_X, DEFAULT_OFFSET_Y);

  private static double CORNER_RADIUS = 8;

}
