//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   BoxGeometryElement
//###########################################################################
//# $Id: BoxGeometryElement.java,v 1.4 2006-03-06 17:08:45 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.awt.geom.Rectangle2D;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.plain.base.GeometryElement;


/**
 * An immutable implementation of the {@link BoxGeometryProxy} interface.
 *
 * @author Robi Malik
 */

public final class BoxGeometryElement
  extends GeometryElement
  implements BoxGeometryProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new box geometry.
   * @param rectangle The rectangle of the new box geometry.
   */
  public BoxGeometryElement(final Rectangle2D rectangle)
  {
    mRectangle = (Rectangle2D) rectangle.clone();
  }


  //#########################################################################
  //# Cloning
  public BoxGeometryElement clone()
  {
    return (BoxGeometryElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final BoxGeometryElement downcast = (BoxGeometryElement) partner;
      return
        mRectangle.equals(downcast.mRectangle);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitBoxGeometryProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.BoxGeometryProxy
  public Rectangle2D getRectangle()
  {
    return (Rectangle2D) mRectangle.clone();
  }


  //#########################################################################
  //# Data Members
  private final Rectangle2D mRectangle;

}
