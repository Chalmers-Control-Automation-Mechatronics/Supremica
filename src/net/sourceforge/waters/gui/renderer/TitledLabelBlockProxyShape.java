//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   LabelBlockProxyShape
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.renderer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.model.module.LabelBlockProxy;


/**
 * <P>A peculiar type of label block to support blocked events list.
 * In addition to the normal label block, it displays a title in
 * a given font on top of the list.</P>
 *
 * @author Simon Ware
 */

public class TitledLabelBlockProxyShape
  extends LabelBlockProxyShape
{

  //#########################################################################
  //# Constructor
  public TitledLabelBlockProxyShape(final LabelBlockProxy block,
                                    final RoundRectangle2D bounds,
                                    final String title,
                                    final Font font)
  {
    super(block, bounds);
    mTitle = title;
    mFont = font;
  }


  //#########################################################################
  //# Simple Access
  boolean getShowsHandlesWhenEmpty()
  {
    return true;
  }

  public Rectangle2D getBounds2D()
  {
    return getTitleBounds();
  }

  public boolean isClicked(final int x, final int y)
  {
    return super.isClicked(x, y) || getTitleBounds().contains(x, y);
  }


  //#########################################################################
  //# Drawing
  public void draw(final Graphics2D g, final RenderingInformation status)
  {
    super.draw(g, status);
    final Rectangle2D shapeBounds = getShape().getBounds();
    final int x = (int) shapeBounds.getMinX();
    final int y = (int) shapeBounds.getMinY();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                       RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g.setFont(mFont);
    g.setColor(status.getColor());
    g.drawString(mTitle, x, y);
  }


  //#########################################################################
  //# Auxiliary Methods
  private Rectangle2D getTitleBounds()
  {
    final Rectangle2D shapeBounds = getShape().getBounds2D();
    final double x = shapeBounds.getX();
    final double y = shapeBounds.getY();
    final FontRenderContext context =
      new FontRenderContext(mFont.getTransform(), false, false);
    final Rectangle2D titleBounds = mFont.getStringBounds(mTitle, context);
    final double width = titleBounds.getWidth();
    final double height = titleBounds.getHeight();
    titleBounds.setRect(x, y - height, width, height);
    return titleBounds;
  }


  //#########################################################################
  //# Data Members
  private final String mTitle;
  private final Font mFont;

}
