//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   AbstractRendererShape
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.geom.Rectangle2D;


public abstract class AbstractRendererShape
  implements RendererShape
{

  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.RendererShape
  public void draw(final Graphics2D g, final RenderingInformation status)
  {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);
        
    // Draw shadow if focused
    if (status.isFocused()) {
      g.setColor(status.getShadowColor());
      g.setStroke(SHADOWSTROKE);
      g.draw(getShape());
    }
        
    // Draw shape
    g.setColor(status.getColor());
    g.setStroke(BASICSTROKE);
    g.draw(getShape());
  }

  public Rectangle2D getBounds2D()
  {
    return getShape().getBounds2D();
  }

  public boolean isClicked(final int x, final int y)
  {
    return getShape().contains(x, y);
  }


  //#########################################################################
  //# Static Class Methods
  public static void setBasicStroke(final Stroke stroke)
  {
    BASICSTROKE = stroke;
  }


  //#########################################################################
  //# Class constants
  /** Single line width, used as default when painting on screen. */
  public static final Stroke SINGLESTROKE = new BasicStroke();
  /** Double line width, used for nodegroup border. */
  public static final Stroke DOUBLESTROKE = new BasicStroke(2);
  /** Thick line used for drawing shadows. */
  public static final Stroke SHADOWSTROKE = new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
  /** Used as the basic stroke when printing--thinner than ordinary lines. */
  //public static final Stroke THINSTROKE = new BasicStroke(0.25f); // Too thin
  //public static final Stroke THINSTROKE = new BasicStroke(0.5f); // Also too thin
  /**
   * The default pen size. <STRONG>BUG</STRONG> Is not <CODE>final</CODE>
   * since it changes when printing --- so needs to be part of renderer.
   */
  public static Stroke BASICSTROKE = SINGLESTROKE;
}
