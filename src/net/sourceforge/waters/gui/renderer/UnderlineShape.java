//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   UnderlineShape
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Line2D;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.model.base.Proxy;


public class UnderlineShape extends AbstractProxyShape
{

  //##########################################################################
  //# Constructor
  public UnderlineShape(final Proxy proxy, final double x, final double y,
                        final double width, final Font font)
  {
    super(proxy);
    final FontRenderContext context = new FontRenderContext(null, true, true);
    final LineMetrics metrics = font.getLineMetrics("", context);
    final float offset = metrics.getUnderlineOffset();
    mLine = new Line2D.Double(x, y + offset, x + width, y + offset);
  }


  //##########################################################################
  //# Overrides for net.sourceforge.waters.gui.renderer.AbstractRendererShape
  @Override
  public void draw(final Graphics2D graphics,final RenderingInformation status)
  {
    if (status.isUnderlined()) {
      graphics.setColor(EditorColor.ERRORCOLOR);
      graphics.setStroke(UNDERLINESTROKE);
      graphics.draw(mLine);
    }
  }

  @Override
  public Shape getShape()
  {
    return mLine;
  }


  //##########################################################################
  //# Data Members
  private final Line2D.Double mLine;


  //##########################################################################
  //# Class Constants
  private final float[] DASHES = new float[] { 2, 4 };
  private final Stroke UNDERLINESTROKE = new BasicStroke(1,
          BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, DASHES, 0);

}
