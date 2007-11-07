//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   RendererShape
//###########################################################################
//# $Id: RendererShape.java,v 1.5 2007-11-07 06:16:04 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;


public interface RendererShape
{

  public void draw(Graphics2D g, RenderingInformation status);

  public Shape getShape();

  public Rectangle2D getBounds2D();

  public boolean isClicked(int x, int y);

}
