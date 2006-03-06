//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   EdgeElement
//###########################################################################
//# $Id: EdgeElement.java,v 1.4 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.Geometry;
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
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final EdgeElement downcast = (EdgeElement) partner;
      return
        mSource.equals(downcast.mSource) &&
        mTarget.equals(downcast.mTarget) &&
        mLabelBlock.equals(downcast.mLabelBlock) &&
        (mGuardActionBlock == null ? downcast.mGuardActionBlock == null :
         mGuardActionBlock.equals(downcast.mGuardActionBlock));
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final EdgeElement downcast = (EdgeElement) partner;
      return
        mSource.equals(downcast.mSource) &&
        mTarget.equals(downcast.mTarget) &&
        mLabelBlock.equalsWithGeometry(downcast.mLabelBlock) &&
        mGuardActionBlock.equalsWithGeometry(downcast.mGuardActionBlock) &&
        Geometry.equalGeometry(mGeometry, downcast.mGeometry) &&
        Geometry.equalGeometry(mStartPoint, downcast.mStartPoint) &&
        Geometry.equalGeometry(mEndPoint, downcast.mEndPoint);
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
