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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.model.module.LabelBlockProxy;


/**
 * <P>A peculiar type of label block to support blocked events list.</P>
 *
 * <P>In addition to the normal label block, it displayed a label in
 * bold on top of the list.</P>
 *
 * <P><STRONG>BUG.</STRONG> The label on top of the list is not
 * included in the bounds, and therefore is not sensitive to mouse
 * clicks.</P>
 *
 * @author Simon Ware
 */

public class LabeledLabelBlockProxyShape
  extends LabelBlockProxyShape
{

  //#########################################################################
  //# Constructor
  public LabeledLabelBlockProxyShape(final LabelBlockProxy block,
				     final RoundRectangle2D bounds,
				     final String name,
				     final Font font)
  {
    super(block, bounds);
    mName = name;
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
    final Rectangle2D oldShape = getShape().getBounds2D();
    final Rectangle2D title = mFont.getStringBounds(mName, new FontRenderContext(mFont.getTransform(), false, false));
    final Rectangle2D output = new Rectangle((int)oldShape.getX(),
                                             (int)(oldShape.getY() - title.getHeight()),
                                             (int)oldShape.getWidth(),
                                             (int)(oldShape.getHeight() + title.getHeight()));
    return output;
  }


  //#########################################################################
  //# Drawing
  public void draw(final Graphics2D g, final RenderingInformation status)
  {
    super.draw(g, status);
    final int x = (int) getShape().getBounds().getMinX();
    final int y = (int) getShape().getBounds().getMinY();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		       RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
		       RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g.setFont(mFont);
    g.setColor(status.getColor());
    g.drawString(mName, x, y);
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final Font mFont;


  //#########################################################################
  //# Class Constants
  public static final int DEFAULTARCW = 8;
  public static final int DEFAULTARCH = 8;
  public static final int DEFAULTOFFSETX = 0;
  public static final int DEFAULTOFFSETY = 10;

}
