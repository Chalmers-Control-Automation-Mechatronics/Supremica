//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   AbstractLabelShape
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.geom.RoundRectangle2D;

import net.sourceforge.waters.model.base.Proxy;


public abstract class AbstractLabelShape extends AbstractProxyShape
{

  //##########################################################################
  //# Constructor
  public AbstractLabelShape(final Proxy proxy)
  {
    super(proxy);
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.ProxyShape
  @Override
  public abstract RoundRectangle2D getShape();


  //##########################################################################
  //# Class Constants
  public static double CORNER_RADIUS = 8;

}
