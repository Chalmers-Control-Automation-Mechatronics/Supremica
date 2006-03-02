//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   EdgeSubject
//###########################################################################
//# $Id: EdgeSubject.java,v 1.5 2006-03-02 12:12:49 martin Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Geometry;
import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
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
   * @param guardActionBlock The guard action block of the new edge, or <CODE>null</CODE>.
   * @param geometry The rendering information of the new edge, or <CODE>null</CODE>.
   * @param startPoint The rendering information for the start point of the new edge, or <CODE>null</CODE>.
   * @param endPoint The rendering information for the end point of the new edge, or <CODE>null</CODE>.
   */
  public EdgeSubject(final NodeProxy source,
                     final NodeProxy target,
                     final LabelBlockProxy labelBlock,
                     final GuardActionBlockProxy guardActionBlock,
                     final SplineGeometryProxy geometry,
                     final PointGeometryProxy startPoint,
                     final PointGeometryProxy endPoint)
  {
    mSource = (NodeSubject) source;
    mTarget = (NodeSubject) target;
    mLabelBlock = (LabelBlockSubject) labelBlock;
    mLabelBlock.setParent(this);
    mGuardActionBlock = (GuardActionBlockSubject) guardActionBlock;
    if (mGuardActionBlock != null) {
      mGuardActionBlock.setParent(this);
    }
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
   * the guard action block set to <CODE>null</CODE>,
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
    if (mGuardActionBlock != null) {
      cloned.mGuardActionBlock = mGuardActionBlock.clone();
      cloned.mGuardActionBlock.setParent(cloned);
    }
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
    if (mGuardActionBlock != null) {
      cloned.mGuardActionBlock = mGuardActionBlock.clone();
      cloned.mGuardActionBlock.setParent(cloned);
    }
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
      final EdgeSubject downcast = (EdgeSubject) partner;
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

  public GuardActionBlockSubject getGuardActionBlock()
  {
    return mGuardActionBlock;
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
    if (mSource == source) {
      return;
    }
    mSource = source;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  /**
   * Sets the target node of this edge.
   */
  public void setTarget(final NodeSubject target)
  {
    if (mTarget == target) {
      return;
    }
    mTarget = target;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  public void setLabelBlock(final LabelBlockSubject labelBlock)
  {
    if (mLabelBlock == labelBlock) {
      return;
    }
    labelBlock.setParent(this);
    mLabelBlock.setParent(null);
    mLabelBlock = labelBlock;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  public void setGuardActionBlock(final GuardActionBlockSubject guardActionBlock)
  {
    final boolean change = (mGuardActionBlock != guardActionBlock);
    if (guardActionBlock != null) {
      guardActionBlock.setParent(this);
    }
    if (mGuardActionBlock != null) {
      mGuardActionBlock.setParent(null);
    }
    mGuardActionBlock = guardActionBlock;
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
    if (mGeometry == geometry) {
      return;
    }
    if (geometry != null) {
      geometry.setParent(this);
    }
    if (mGeometry != null) {
      mGeometry.setParent(null);
    }
    mGeometry = geometry;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(this, mGeometry);
    fireModelChanged(event);
  }

  /**
   * Sets the rendering information for the start point of this edge.
   */
  public void setStartPoint(final PointGeometrySubject startPoint)
  {
    if (mStartPoint == startPoint) {
      return;
    }
    if (startPoint != null) {
      startPoint.setParent(this);
    }
    if (mStartPoint != null) {
      mStartPoint.setParent(null);
    }
    mStartPoint = startPoint;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(this, mStartPoint);
    fireModelChanged(event);
  }

  /**
   * Sets the rendering information for the end point of this edge.
   */
  public void setEndPoint(final PointGeometrySubject endPoint)
  {
    if (mEndPoint == endPoint) {
      return;
    }
    if (endPoint != null) {
      endPoint.setParent(this);
    }
    if (mEndPoint != null) {
      mEndPoint.setParent(null);
    }
    mEndPoint = endPoint;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(this, mEndPoint);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private NodeSubject mSource;
  private NodeSubject mTarget;
  private LabelBlockSubject mLabelBlock;
  private GuardActionBlockSubject mGuardActionBlock;
  private SplineGeometrySubject mGeometry;
  private PointGeometrySubject mStartPoint;
  private PointGeometrySubject mEndPoint;

}
