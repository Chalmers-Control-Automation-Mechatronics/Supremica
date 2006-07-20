//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   EdgeElement
//###########################################################################
//# $Id: EdgeElement.java,v 1.6 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.plain.base.Element;


/**
 * An immutable implementation of the {@link EdgeProxy} interface.
 *
 * @author Robi Malik
 */

public final class EdgeElement
  extends Element
  implements EdgeProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new edge.
   * @param source The source node of the new edge.
   * @param target The target node of the new edge.
   * @param labelBlock The label block of the new edge.
   * @param guardActionBlock The guard action block of the new edge, or <CODE>null</CODE>.
   * @param geometry The rendering information of the new edge, or <CODE>null</CODE>.
   * @param startPoint The rendering information for the start point of the new edge, or <CODE>null</CODE>.
   * @param endPoint The rendering information for the end point of the new edge, or <CODE>null</CODE>.
   */
  public EdgeElement(final NodeProxy source,
                     final NodeProxy target,
                     final LabelBlockProxy labelBlock,
                     final GuardActionBlockProxy guardActionBlock,
                     final SplineGeometryProxy geometry,
                     final PointGeometryProxy startPoint,
                     final PointGeometryProxy endPoint)
  {
    mSource = source;
    mTarget = target;
    mLabelBlock = labelBlock;
    mGuardActionBlock = guardActionBlock;
    mGeometry = geometry;
    mStartPoint = startPoint;
    mEndPoint = endPoint;
  }

  /**
   * Creates a new edge using default values.
   * This constructor creates an edge with
   * the guard action block set to <CODE>null</CODE>,
   * the rendering information set to <CODE>null</CODE>,
   * the rendering information for the start point set to <CODE>null</CODE>, and
   * the rendering information for the end point set to <CODE>null</CODE>.
   * @param source The source node of the new edge.
   * @param target The target node of the new edge.
   * @param labelBlock The label block of the new edge.
   */
  public EdgeElement(final NodeProxy source,
                     final NodeProxy target,
                     final LabelBlockProxy labelBlock)
  {
    this(source,
         target,
         labelBlock,
         null,
         null,
         null,
         null);
  }


  //#########################################################################
  //# Cloning
  public EdgeElement clone()
  {
    return (EdgeElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final EdgeElement downcast = (EdgeElement) partner;
      return
        mSource.equalsByContents(downcast.mSource) &&
        mTarget.equalsByContents(downcast.mTarget) &&
        mLabelBlock.equalsByContents(downcast.mLabelBlock) &&
        (mGuardActionBlock == null ? downcast.mGuardActionBlock == null :
         mGuardActionBlock.equalsByContents(downcast.mGuardActionBlock));
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final EdgeElement downcast = (EdgeElement) partner;
      return
        mSource.equalsWithGeometry(downcast.mSource) &&
        mTarget.equalsWithGeometry(downcast.mTarget) &&
        mLabelBlock.equalsWithGeometry(downcast.mLabelBlock) &&
        (mGuardActionBlock == null ? downcast.mGuardActionBlock == null :
         mGuardActionBlock.equalsWithGeometry(downcast.mGuardActionBlock)) &&
        (mGeometry == null ? downcast.mGeometry == null :
         mGeometry.equalsWithGeometry(downcast.mGeometry)) &&
        (mStartPoint == null ? downcast.mStartPoint == null :
         mStartPoint.equalsWithGeometry(downcast.mStartPoint)) &&
        (mEndPoint == null ? downcast.mEndPoint == null :
         mEndPoint.equalsWithGeometry(downcast.mEndPoint));
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mSource.hashCodeByContents();
    result *= 5;
    result += mTarget.hashCodeByContents();
    result *= 5;
    result += mLabelBlock.hashCodeByContents();
    result *= 5;
    if (mGuardActionBlock != null) {
      result += mGuardActionBlock.hashCodeByContents();
    }
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mSource.hashCodeWithGeometry();
    result *= 5;
    result += mTarget.hashCodeWithGeometry();
    result *= 5;
    result += mLabelBlock.hashCodeWithGeometry();
    result *= 5;
    if (mGuardActionBlock != null) {
      result += mGuardActionBlock.hashCodeWithGeometry();
    }
    result *= 5;
    if (mGeometry != null) {
      result += mGeometry.hashCodeWithGeometry();
    }
    result *= 5;
    if (mStartPoint != null) {
      result += mStartPoint.hashCodeWithGeometry();
    }
    result *= 5;
    if (mEndPoint != null) {
      result += mEndPoint.hashCodeWithGeometry();
    }
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitEdgeProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.EdgeProxy
  public NodeProxy getSource()
  {
    return mSource;
  }

  public NodeProxy getTarget()
  {
    return mTarget;
  }

  public LabelBlockProxy getLabelBlock()
  {
    return mLabelBlock;
  }

  public GuardActionBlockProxy getGuardActionBlock()
  {
    return mGuardActionBlock;
  }

  public SplineGeometryProxy getGeometry()
  {
    return mGeometry;
  }

  public PointGeometryProxy getStartPoint()
  {
    return mStartPoint;
  }

  public PointGeometryProxy getEndPoint()
  {
    return mEndPoint;
  }


  //#########################################################################
  //# Data Members
  private final NodeProxy mSource;
  private final NodeProxy mTarget;
  private final LabelBlockProxy mLabelBlock;
  private final GuardActionBlockProxy mGuardActionBlock;
  private final SplineGeometryProxy mGeometry;
  private final PointGeometryProxy mStartPoint;
  private final PointGeometryProxy mEndPoint;

}
