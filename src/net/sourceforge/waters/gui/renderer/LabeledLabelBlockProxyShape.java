//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   LabelBlockProxyShape
//###########################################################################
//# $Id: LabeledLabelBlockProxyShape.java,v 1.2 2007-12-04 03:22:55 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.renderer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
    

  //#########################################################################
  //# Drawing
  public void draw(Graphics2D g, RenderingInformation status)
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
