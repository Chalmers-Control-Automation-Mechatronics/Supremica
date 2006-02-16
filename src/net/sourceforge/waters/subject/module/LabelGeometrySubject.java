//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   LabelGeometrySubject
//###########################################################################
//# $Id: LabelGeometrySubject.java,v 1.4 2006-02-16 04:06:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.awt.geom.Point2D;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.subject.base.GeometrySubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.Subject;

import net.sourceforge.waters.xsd.module.AnchorPosition;


/**
 * The subject implementation of the {@link LabelGeometryProxy} interface.
 *
 * @author Robi Malik
 */

public final class LabelGeometrySubject
  extends GeometrySubject
  implements LabelGeometryProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new label geometry.
   * @param offset The offset of the new label geometry.
   * @param anchor The anchor position of the new label geometry.
   */
  public LabelGeometrySubject(final Point2D offset,
                              final AnchorPosition anchor)
  {
    mOffset = (Point2D) offset.clone();
    mAnchor = anchor;
  }

  /**
   * Creates a new label geometry using default values.
   * This constructor creates a label geometry with
   * the anchor position set to <CODE>AnchorPosition.NW</CODE>.
   * @param offset The offset of the new label geometry.
   */
  public LabelGeometrySubject(final Point2D offset)
  {
    this(offset,
         AnchorPosition.NW);
  }


  //#########################################################################
  //# Cloning
  public LabelGeometrySubject clone()
  {
    final LabelGeometrySubject cloned = (LabelGeometrySubject) super.clone();
    cloned.mOffset = (Point2D) mOffset.clone();
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final LabelGeometrySubject downcast = (LabelGeometrySubject) partner;
      return
        mOffset.equals(downcast.mOffset) &&
        mAnchor.equals(downcast.mAnchor);
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
    return downcast.visitLabelGeometryProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.LabelGeometryProxy
  public Point2D getOffset()
  {
    return (Point2D) mOffset.clone();
  }

  public AnchorPosition getAnchor()
  {
    return mAnchor;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the offset of this label geometry.
   */
  public void setOffset(final Point2D offset)
  {
    if (mOffset.equals(offset)) {
      return;
    }
    mOffset = (Point2D) offset.clone();
    final Subject source = getParent();
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(source, this);
    fireModelChanged(event);
  }

  /**
   * Sets the anchor position of this label geometry.
   */
  public void setAnchor(final AnchorPosition anchor)
  {
    if (mAnchor.equals(anchor)) {
      return;
    }
    mAnchor = anchor;
    final Subject source = getParent();
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(source, this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private Point2D mOffset;
  private AnchorPosition mAnchor;

}
