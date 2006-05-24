//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   PointGeometrySubject
//###########################################################################
//# $Id: PointGeometrySubject.java,v 1.6 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.awt.geom.Point2D;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.subject.base.GeometrySubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.Subject;


/**
 * The subject implementation of the {@link PointGeometryProxy} interface.
 *
 * @author Robi Malik
 */

public final class PointGeometrySubject
  extends GeometrySubject
  implements PointGeometryProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new point geometry.
   * @param point The point of the new point geometry.
   */
  public PointGeometrySubject(final Point2D point)
  {
    mPoint = (Point2D) point.clone();
  }


  //#########################################################################
  //# Cloning
  public PointGeometrySubject clone()
  {
    final PointGeometrySubject cloned = (PointGeometrySubject) super.clone();
    cloned.mPoint = (Point2D) mPoint.clone();
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final PointGeometrySubject downcast = (PointGeometrySubject) partner;
      return
        mPoint.equals(downcast.mPoint);
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
    return downcast.visitPointGeometryProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.PointGeometryProxy
  public Point2D getPoint()
  {
    return (Point2D) mPoint.clone();
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the point identifying this PointGeometry.
   */
  public void setPoint(final Point2D point)
  {
    if (mPoint.equals(point)) {
      return;
    }
    mPoint = (Point2D) point.clone();
    final Subject source = getParent();
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(source, this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private Point2D mPoint;

}
