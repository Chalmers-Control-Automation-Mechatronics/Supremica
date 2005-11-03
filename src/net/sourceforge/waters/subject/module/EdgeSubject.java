//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   EdgeSubject
//###########################################################################
//# $Id: EdgeSubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Geometry;
import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.MutableSubject;


/**
 * The subject implementation of the {@link EdgeProxy} interface.
 *
 * @author Robi Malik
 */

public final class EdgeSubject
  extends MutableSubject
  implements EdgeProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new edge.
   * @param source The source node of the new edge.
   * @param target The target node of the new edge.
   * @param labelBlock The label block of the new edge.
   * @param geometry The rendering information of the new edge, or <CODE>null</CODE>.
   * @param startPoint The rendering information for the start point of the new edge, or <CODE>null</CODE>.
   * @param endPoint The rendering information for the end point of the new edge, or <CODE>null</CODE>.
   */
  public EdgeSubject(final NodeProxy source,
                     final NodeProxy target,
                     final LabelBlockProxy labelBlock,
                     final SplineGeometryProxy geometry,
                     final PointGeometryProxy startPoint,
                     final PointGeometryProxy endPoint)
  {
    mSource = (NodeSubject) source;
    mTarget = (NodeSubject) target;
    mLabelBlock = (LabelBlockSubject) labelBlock;
    mLabelBlock.setParent(this);
    mGeometry = (SplineGeometrySubject) geometry;
    if (mGeometry != null) {
      mGeometry.setParent(this);
    }
    mStartPoint = (PointGeometrySubject) startPoint;
    if (mStartPoint != null) {
      mStartPoint.setParent(this);
    }
    mEndPoint = (PointGeometrySubject) endPoint;
    if (mEndPoint != null) {
      mEndPoint.setParent(this);
    }
  }

  /**
   * Creates a new edge using default values.
   * This constructor creates an edge with
   * the rendering information set to <CODE>null</CODE>,
   * the rendering information for the start point set to <CODE>null</CODE>, and
   * the rendering information for the end point set to <CODE>null</CODE>.
   * @param source The source node of the new edge.
   * @param target The target node of the new edge.
   * @param labelBlock The label block of the new edge.
   */
  public EdgeSubject(final NodeProxy source,
                     final NodeProxy target,
                     final LabelBlockProxy labelBlock)
  {
    this(source,
         target,
         labelBlock,
         null,
         null,
         null);
  }


  //#########################################################################
  //# Cloning
  public EdgeSubject clone()
  {
    final EdgeSubject cloned = (EdgeSubject) super.clone();
    cloned.mLabelBlock = mLabelBlock.clone();
    cloned.mLabelBlock.setParent(cloned);
    if (mGeometry != null) {
      cloned.mGeometry = mGeometry.clone();
      cloned.mGeometry.setParent(cloned);
    }
    if (mStartPoint != null) {
      cloned.mStartPoint = mStartPoint.clone();
      cloned.mStartPoint.setParent(cloned);
    }
    if (mEndPoint != null) {
      cloned.mEndPoint = mEndPoint.clone();
      cloned.mEndPoint.setParent(cloned);
    }
    return cloned;
  }

  public EdgeSubject clone(final IndexedSet<NodeSubject> refmap)
  {
    final EdgeSubject cloned = (EdgeSubject) super.clone();
    final String sourceName = mSource.getName();
    cloned.mSource = refmap.find(sourceName);
    final String targetName = mTarget.getName();
    cloned.mTarget = refmap.find(targetName);
    cloned.mLabelBlock = mLabelBlock.clone();
    cloned.mLabelBlock.setParent(cloned);
    if (mGeometry != null) {
      cloned.mGeometry = mGeometry.clone();
      cloned.mGeometry.setParent(cloned);
    }
    if (mStartPoint != null) {
      cloned.mStartPoint = mStartPoint.clone();
      cloned.mStartPoint.setParent(cloned);
    }
    if (mEndPoint != null) {
      cloned.mEndPoint = mEndPoint.clone();
      cloned.mEndPoint.setParent(cloned);
    }
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final EdgeSubject downcast = (EdgeSubject) partner;
      return
        mSource.equals(downcast.mSource) &&
        mTarget.equals(downcast.mTarget) &&
        mLabelBlock.equals(downcast.mLabelBlock);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final EdgeSubject downcast = (EdgeSubject) partner;
      return
        mSource.equals(downcast.mSource) &&
        mTarget.equals(downcast.mTarget) &&
        mLabelBlock.equalsWithGeometry(downcast.mLabelBlock) &&
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
  public NodeSubject getSource()
  {
    return mSource;
  }

  public NodeSubject getTarget()
  {
    return mTarget;
  }

  public LabelBlockSubject getLabelBlock()
  {
    return mLabelBlock;
  }

  public SplineGeometrySubject getGeometry()
  {
    return mGeometry;
  }

  public PointGeometrySubject getStartPoint()
  {
    return mStartPoint;
  }

  public PointGeometrySubject getEndPoint()
  {
    return mEndPoint;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the source node of this edge.
   */
  public void setSource(final NodeSubject source)
  {
    final boolean change = (mSource != source);
    mSource = source;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createStateChanged(this);
      fireModelChanged(event);
    }
  }

  /**
   * Sets the target node of this edge.
   */
  public void setTarget(final NodeSubject target)
  {
    final boolean change = (mTarget != target);
    mTarget = target;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createStateChanged(this);
      fireModelChanged(event);
    }
  }

  public void setLabelBlock(final LabelBlockSubject labelBlock)
  {
    final boolean change = (mLabelBlock != labelBlock);
    labelBlock.setParent(this);
    mLabelBlock.setParent(null);
    mLabelBlock = labelBlock;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createStateChanged(this);
      fireModelChanged(event);
    }
  }

  /**
   * Sets the rendering information for this edge.
   */
  public void setGeometry(final SplineGeometrySubject geometry)
  {
    final boolean change = (mGeometry != geometry);
    if (geometry != null) {
      geometry.setParent(this);
    }
    if (mGeometry != null) {
      mGeometry.setParent(null);
    }
    mGeometry = geometry;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createGeometryChanged(this);
      fireModelChanged(event);
    }
  }

  /**
   * Sets the rendering information for the start point of this edge.
   */
  public void setStartPoint(final PointGeometrySubject startPoint)
  {
    final boolean change = (mStartPoint != startPoint);
    if (startPoint != null) {
      startPoint.setParent(this);
    }
    if (mStartPoint != null) {
      mStartPoint.setParent(null);
    }
    mStartPoint = startPoint;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createGeometryChanged(this);
      fireModelChanged(event);
    }
  }

  /**
   * Sets the rendering information for the end point of this edge.
   */
  public void setEndPoint(final PointGeometrySubject endPoint)
  {
    final boolean change = (mEndPoint != endPoint);
    if (endPoint != null) {
      endPoint.setParent(this);
    }
    if (mEndPoint != null) {
      mEndPoint.setParent(null);
    }
    mEndPoint = endPoint;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createGeometryChanged(this);
      fireModelChanged(event);
    }
  }


  //#########################################################################
  //# Data Members
  private NodeSubject mSource;
  private NodeSubject mTarget;
  private LabelBlockSubject mLabelBlock;
  private SplineGeometrySubject mGeometry;
  private PointGeometrySubject mStartPoint;
  private PointGeometrySubject mEndPoint;

}
