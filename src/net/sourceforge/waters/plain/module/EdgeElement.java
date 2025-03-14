//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# THIS FILE HAS BEEN AUTOMATICALLY GENERATED BY A SCRIPT.
//# DO NOT EDIT.
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.plain.module;

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
   * @param source The source node of the new edge, or <CODE>null</CODE>.
   * @param target The target node of the new edge, or <CODE>null</CODE>.
   * @param labelBlock The label block of the new edge, or <CODE>null</CODE> if empty.
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
    if (labelBlock == null) {
      mLabelBlock = new LabelBlockElement();
    } else {
      mLabelBlock = labelBlock;
    }
    mGuardActionBlock = guardActionBlock;
    mGeometry = geometry;
    mStartPoint = startPoint;
    mEndPoint = endPoint;
  }

  /**
   * Creates a new edge using default values.
   * This constructor creates an edge with
   * the source node set to <CODE>null</CODE>,
   * the target node set to <CODE>null</CODE>,
   * an empty label block,
   * the guard action block set to <CODE>null</CODE>,
   * the rendering information set to <CODE>null</CODE>,
   * the rendering information for the start point set to <CODE>null</CODE>, and
   * the rendering information for the end point set to <CODE>null</CODE>.
   */
  public EdgeElement()
  {
    this(null,
         null,
         null,
         null,
         null,
         null,
         null);
  }


  //#########################################################################
  //# Cloning
  @Override
  public EdgeElement clone()
  {
    return (EdgeElement) super.clone();
  }


  //#########################################################################
  //# Comparing
  public Class<EdgeProxy> getProxyInterface()
  {
    return EdgeProxy.class;
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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1027597141812990415L;

}
