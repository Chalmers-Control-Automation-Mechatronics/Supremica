//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   SimpleNodeElement
//###########################################################################
//# $Id: SimpleNodeElement.java,v 1.4 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

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


/**
 * An immutable implementation of the {@link SimpleNodeProxy} interface.
 *
 * @author Robi Malik
 */

public final class SimpleNodeElement
  extends NodeElement
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
  public SimpleNodeElement(final String name,
                           final EventListExpressionProxy propositions,
                           final boolean initial,
                           final PointGeometryProxy pointGeometry,
                           final PointGeometryProxy initialArrowGeometry,
                           final LabelGeometryProxy labelGeometry)
  {
    super(name, propositions);
    mIsInitial = initial;
    mPointGeometry = pointGeometry;
    mInitialArrowGeometry = initialArrowGeometry;
    mLabelGeometry = labelGeometry;
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
  public SimpleNodeElement(final String name,
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
  public SimpleNodeElement clone()
  {
    return (SimpleNodeElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final SimpleNodeElement downcast = (SimpleNodeElement) partner;
      return
        (mIsInitial == downcast.mIsInitial);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final SimpleNodeElement downcast = (SimpleNodeElement) partner;
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

  public PointGeometryProxy getPointGeometry()
  {
    return mPointGeometry;
  }

  public PointGeometryProxy getInitialArrowGeometry()
  {
    return mInitialArrowGeometry;
  }

  public LabelGeometryProxy getLabelGeometry()
  {
    return mLabelGeometry;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.NodeProxy
  public Set<NodeProxy> getImmediateChildNodes()
  {
    return Collections.emptySet();
  }


  //#########################################################################
  //# Data Members
  private final boolean mIsInitial;
  private final PointGeometryProxy mPointGeometry;
  private final PointGeometryProxy mInitialArrowGeometry;
  private final LabelGeometryProxy mLabelGeometry;

}
