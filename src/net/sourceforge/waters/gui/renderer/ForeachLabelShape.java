//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   ForeachLabelShape
//###########################################################################
//# $Id$
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
