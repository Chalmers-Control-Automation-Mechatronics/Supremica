//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   SplineGeometryElement
//###########################################################################
//# $Id: SplineGeometryElement.java,v 1.7 2006-09-20 16:24:13 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.plain.base.CloningGeometryListElement;
import net.sourceforge.waters.plain.base.GeometryElement;

import net.sourceforge.waters.xsd.module.SplineKind;


/**
 * An immutable implementation of the {@link SplineGeometryProxy} interface.
 *
 * @author Robi Malik
 */

public final class SplineGeometryElement
  extends GeometryElement
  implements SplineGeometryProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new spline geometry.
   * @param points The list of control points of the new spline geometry, or <CODE>null</CODE> if empty.
   * @param kind The kind of the new spline geometry.
   */
  public SplineGeometryElement(final Collection<? extends Point2D> points,
                               final SplineKind kind)
  {
    mPoints = new CloningGeometryListElement<Point2D>(points);
    mKind = kind;
  }

  /**
   * Creates a new spline geometry using default values.
   * This constructor creates a spline geometry with
   * an empty list of control points and
   * the kind set to <CODE>SplineKind.INTERPOLATING</CODE>.
   */
  public SplineGeometryElement()
  {
    this(null,
         SplineKind.INTERPOLATING);
  }


  //#########################################################################
  //# Cloning
  public SplineGeometryElement clone()
  {
    return (SplineGeometryElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final SplineGeometryElement downcast = (SplineGeometryElement) partner;
      return
        mPoints.equals(downcast.mPoints) &&
        mKind.equals(downcast.mKind);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mPoints.hashCode();
    result *= 5;
    result += mKind.hashCode();
    return result;
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
    return mPoints;
  }

  public SplineKind getKind()
  {
    return mKind;
  }


  //#########################################################################
  //# Data Members
  private final List<Point2D> mPoints;
  private final SplineKind mKind;

}
