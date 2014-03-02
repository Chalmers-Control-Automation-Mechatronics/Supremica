//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   LabelShape
//###########################################################################
//# $Id$
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
