//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   BoxGeometrySubject
//###########################################################################
//# $Id: BoxGeometrySubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.awt.geom.Rectangle2D;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.subject.base.GeometrySubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link BoxGeometryProxy} interface.
 *
 * @author Robi Malik
 */

public final class BoxGeometrySubject
  extends GeometrySubject
  implements BoxGeometryProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new box geometry.
   * @param rectangle The rectangle of the new box geometry.
   */
  public BoxGeometrySubject(final Rectangle2D rectangle)
  {
    mRectangle = rectangle;
  }


  //#########################################################################
  //# Cloning
  public BoxGeometrySubject clone()
  {
    final BoxGeometrySubject cloned = (BoxGeometrySubject) super.clone();
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final BoxGeometrySubject downcast = (BoxGeometrySubject) partner;
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
    return mRectangle;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the rectangle identifying this box geometry.
   */
  public void setRectangle(final Rectangle2D rectangle)
  {
    final boolean change = !mRectangle.equals(rectangle);
    mRectangle = rectangle;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createGeometryChanged(this);
      fireModelChanged(event);
    }
  }


  //#########################################################################
  //# Data Members
  private Rectangle2D mRectangle;

}
