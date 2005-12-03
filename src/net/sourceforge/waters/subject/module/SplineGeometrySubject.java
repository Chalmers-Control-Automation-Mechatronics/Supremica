//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SplineGeometrySubject
//###########################################################################
//# $Id: SplineGeometrySubject.java,v 1.3 2005-12-03 21:30:42 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.CloningGeometryListSubject;
import net.sourceforge.waters.subject.base.GeometrySubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.SimpleListSubject;

import net.sourceforge.waters.xsd.module.SplineKind;


/**
 * The subject implementation of the {@link SplineGeometryProxy} interface.
 *
 * @author Robi Malik
 */

public final class SplineGeometrySubject
  extends GeometrySubject
  implements SplineGeometryProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new spline geometry.
   * @param points The list of control points of the new spline geometry.
   * @param kind The kind of the new spline geometry.
   */
  public SplineGeometrySubject(final Collection<? extends Point2D> points,
                               final SplineKind kind)
  {
    mPoints = new CloningGeometryListSubject<Point2D>(points);
    mPoints.setParent(this);
    mKind = kind;
  }

  /**
   * Creates a new spline geometry using default values.
   * This constructor creates a spline geometry with
   * an empty list of control points and
   * the kind set to <CODE>SplineKind.INTERPOLATING</CODE>.
   */
  public SplineGeometrySubject()
  {
    this(emptyPoint2DList(),
         SplineKind.INTERPOLATING);
  }


  //#########################################################################
  //# Cloning
  public SplineGeometrySubject clone()
  {
    final SplineGeometrySubject cloned = (SplineGeometrySubject) super.clone();
    cloned.mPoints = mPoints.clone();
    cloned.mPoints.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final SplineGeometrySubject downcast = (SplineGeometrySubject) partner;
      return
        mPoints.equals(downcast.mPoints) &&
        mKind.equals(downcast.mKind);
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
    return downcast.visitSplineGeometryProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.SplineGeometryProxy
  public List<Point2D> getPoints()
  {
    final List<Point2D> downcast = Casting.toList(mPoints);
    return Collections.unmodifiableList(downcast);
  }

  public SplineKind getKind()
  {
    return mKind;
  }


  //#########################################################################
  //# Setters
  /**
   * Gets the modifiable list of control points of this spline geometry.
   */
  public SimpleListSubject<Point2D> getPointsModifiable()
  {
    return mPoints;
  }

  /**
   * Sets the spline type.
   */
  public void setKind(final SplineKind kind)
  {
    if (mKind.equals(kind)) {
      return;
    }
    mKind = kind;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<Point2D> emptyPoint2DList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private SimpleListSubject<Point2D> mPoints;
  private SplineKind mKind;

}
