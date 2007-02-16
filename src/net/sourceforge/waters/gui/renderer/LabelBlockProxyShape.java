//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   LabelBlockProxyShape
//###########################################################################
//# $Id: LabelBlockProxyShape.java,v 1.7 2007-02-16 03:00:42 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.model.module.LabelBlockProxy;


public class LabelBlockProxyShape
  extends AbstractProxyShape
{

  //#########################################################################
  //# Constructor
  public LabelBlockProxyShape(final LabelBlockProxy block,
                              final RoundRectangle2D bounds)
  {
    super(block);
    if (block.getEventList().isEmpty()) {
      mShape = new GeneralPath();
      mHeight = 0;
    } else {
      mShape = bounds;
      mHeight = bounds.getHeight();
    }
  }


  //#########################################################################
  //# Simple Access
  double getHeight()
  {
    return mHeight;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.RendererShape
  public Shape getShape()
  {
    return mShape;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.ProxyShape
  public LabelBlockProxy getProxy()
  {
    return (LabelBlockProxy) super.getProxy();
  }


  //#########################################################################
  //# Drawing
  public void draw(final Graphics2D g2d, final RenderingInformation status)
  {
    final Shape shape = getShape();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
    if (status.isFocused()) {
      g2d.setColor(status.getShadowColor());
      g2d.setStroke(SHADOWSTROKE);
      g2d.fill(shape);
    }
    if (status.showHandles()) {
      g2d.setColor(status.getColor());
      g2d.setStroke(BASICSTROKE);
      g2d.draw(shape);
    }
  }


  //#########################################################################
  //# Data Members
  private final Shape mShape;
  private final double mHeight;


  //#########################################################################
  //# Class Constants
  public static final int DEFAULTARCW = 8;
  public static final int DEFAULTARCH = 8;
  public static final int DEFAULTOFFSETX = 0;
  public static final int DEFAULTOFFSETY = 10;

}
