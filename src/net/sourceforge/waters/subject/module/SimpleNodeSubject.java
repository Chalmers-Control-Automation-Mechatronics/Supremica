//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SimpleNodeSubject
//###########################################################################
//# $Id: SimpleNodeSubject.java,v 1.7 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collections;
import java.util.Set;

import net.sourceforge.waters.model.base.Geometry;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link SimpleNodeProxy} interface.
 *
 * @author Robi Malik
 */

public final class SimpleNodeSubject
  extends NodeSubject
  implements SimpleNodeProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new simple node.
   * @param name The name of the new simple node.
   * @param propositions The list of propositions of the new simple node.
   * @param initial The initial status of the new simple node.
   * @param pointGeometry The geometric position of the new simple node, or <CODE>null</CODE>.
   * @param initialArrowGeometry The position of the initial state arrow of the new simple node, or <CODE>null</CODE>.
   * @param labelGeometry The geometric position of the label of the new simple node, or <CODE>null</CODE>.
   */
  public SimpleNodeSubject(final String name,
                           final EventListExpressionProxy propositions,
                           final boolean initial,
                           final PointGeometryProxy pointGeometry,
                           final PointGeometryProxy initialArrowGeometry,
                           final LabelGeometryProxy labelGeometry)
  {
    super(name, propositions);
    mIsInitial = initial;
    mPointGeometry = (PointGeometrySubject) pointGeometry;
    if (mPointGeometry != null) {
      mPointGeometry.setParent(this);
    }
    mInitialArrowGeometry = (PointGeometrySubject) initialArrowGeometry;
    if (mInitialArrowGeometry != null) {
      mInitialArrowGeometry.setParent(this);
    }
    mLabelGeometry = (LabelGeometrySubject) labelGeometry;
    if (mLabelGeometry != null) {
      mLabelGeometry.setParent(this);
    }
  }

  /**
   * Creates a new simple node using default values.
   * This constructor creates a simple node with
   * the initial status set to <CODE>false</CODE>,
   * the geometric position set to <CODE>null</CODE>,
   * the position of the initial state arrow set to <CODE>null</CODE>, and
   * the geometric position of the label set to <CODE>null</CODE>.
   * @param name The name of the new simple node.
   * @param propositions The list of propositions of the new simple node.
   */
  public SimpleNodeSubject(final String name,
                           final EventListExpressionProxy propositions)
  {
    this(name,
         propositions,
         false,
         null,
         null,
         null);
  }


  //#########################################################################
  //# Cloning
  public SimpleNodeSubject clone()
  {
    final SimpleNodeSubject cloned = (SimpleNodeSubject) super.clone();
    if (mPointGeometry != null) {
      cloned.mPointGeometry = mPointGeometry.clone();
      cloned.mPointGeometry.setParent(cloned);
    }
    if (mInitialArrowGeometry != null) {
      cloned.mInitialArrowGeometry = mInitialArrowGeometry.clone();
      cloned.mInitialArrowGeometry.setParent(cloned);
    }
    if (mLabelGeometry != null) {
      cloned.mLabelGeometry = mLabelGeometry.clone();
      cloned.mLabelGeometry.setParent(cloned);
    }
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final SimpleNodeSubject downcast = (SimpleNodeSubject) partner;
      return
        (mIsInitial == downcast.mIsInitial);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final SimpleNodeSubject downcast = (SimpleNodeSubject) partner;
      return
        (mIsInitial == downcast.mIsInitial) &&
        Geometry.equalGeometry(mPointGeometry, downcast.mPointGeometry) &&
        Geometry.equalGeometry(mInitialArrowGeometry, downcast.mInitialArrowGeometry) &&
        Geometry.equalGeometry(mLabelGeometry, downcast.mLabelGeometry);
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
    return downcast.visitSimpleNodeProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.SimpleNodeProxy
  public boolean isInitial()
  {
    return mIsInitial;
  }

  public PointGeometrySubject getPointGeometry()
  {
    return mPointGeometry;
  }

  public PointGeometrySubject getInitialArrowGeometry()
  {
    return mInitialArrowGeometry;
  }

  public LabelGeometrySubject getLabelGeometry()
  {
    return mLabelGeometry;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the initial status of this node.
   */
  public void setInitial(final boolean initial)
  {
    if (mIsInitial == initial) {
      return;
    }
    mIsInitial = initial;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  /**
   * Sets the geometric position of this node.
   */
  public void setPointGeometry(final PointGeometrySubject pointGeometry)
  {
    if (mPointGeometry == pointGeometry) {
      return;
    }
    if (pointGeometry != null) {
      pointGeometry.setParent(this);
    }
    if (mPointGeometry != null) {
      mPointGeometry.setParent(null);
    }
    mPointGeometry = pointGeometry;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(this, mPointGeometry);
    fireModelChanged(event);
  }

  /**
   * Sets the position of the initial state arrow of this node.
   */
  public void setInitialArrowGeometry(final PointGeometrySubject initialArrowGeometry)
  {
    if (mInitialArrowGeometry == initialArrowGeometry) {
      return;
    }
    if (initialArrowGeometry != null) {
      initialArrowGeometry.setParent(this);
    }
    if (mInitialArrowGeometry != null) {
      mInitialArrowGeometry.setParent(null);
    }
    mInitialArrowGeometry = initialArrowGeometry;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(this, mInitialArrowGeometry);
    fireModelChanged(event);
  }

  /**
   * Sets the geometric position of the label of this node.
   */
  public void setLabelGeometry(final LabelGeometrySubject labelGeometry)
  {
    if (mLabelGeometry == labelGeometry) {
      return;
    }
    if (labelGeometry != null) {
      labelGeometry.setParent(this);
    }
    if (mLabelGeometry != null) {
      mLabelGeometry.setParent(null);
    }
    mLabelGeometry = labelGeometry;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(this, mLabelGeometry);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.NodeProxy
  public Set<NodeProxy> getImmediateChildNodes()
  {
    return Collections.emptySet();
  }


  //#########################################################################
  //# Data Members
  private boolean mIsInitial;
  private PointGeometrySubject mPointGeometry;
  private PointGeometrySubject mInitialArrowGeometry;
  private LabelGeometrySubject mLabelGeometry;

}
