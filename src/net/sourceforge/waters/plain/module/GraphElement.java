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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.plain.base.Element;


/**
 * An immutable implementation of the {@link GraphProxy} interface.
 *
 * @author Robi Malik
 */

public final class GraphElement
  extends Element
  implements GraphProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new graph.
   * @param deterministic The determinism status of the new graph.
   * @param blockedEvents The list of blocked events of the new graph, or <CODE>null</CODE>.
   * @param nodes The set of nodes of the new graph, or <CODE>null</CODE> if empty.
   * @param edges The collection of edges of the new graph, or <CODE>null</CODE> if empty.
   */
  public GraphElement(final boolean deterministic,
                      final LabelBlockProxy blockedEvents,
                      final Collection<? extends NodeProxy> nodes,
                      final Collection<? extends EdgeProxy> edges)
  {
    mIsDeterministic = deterministic;
    mBlockedEvents = blockedEvents;
    if (nodes == null) {
      mNodes = Collections.emptySet();
    } else {
      final Set<NodeProxy> nodesModifiable =
        new NodeSetElement(nodes);
      mNodes =
        Collections.unmodifiableSet(nodesModifiable);
    }
    if (edges == null) {
      mEdges = Collections.emptyList();
    } else {
      final Collection<EdgeProxy> edgesModifiable =
        new ArrayList<EdgeProxy>(edges);
      mEdges =
        Collections.unmodifiableCollection(edgesModifiable);
    }
  }

  /**
   * Creates a new graph using default values.
   * This constructor creates a graph with
   * the determinism status set to <CODE>true</CODE>,
   * the list of blocked events set to <CODE>null</CODE>,
   * an empty set of nodes, and
   * an empty collection of edges.
   */
  public GraphElement()
  {
    this(true,
         null,
         null,
         null);
  }


  //#########################################################################
  //# Cloning
  @Override
  public GraphElement clone()
  {
    return (GraphElement) super.clone();
  }


  //#########################################################################
  //# Comparing
  public Class<GraphProxy> getProxyInterface()
  {
    return GraphProxy.class;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitGraphProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.GraphProxy
  public boolean isDeterministic()
  {
    return mIsDeterministic;
  }

  public LabelBlockProxy getBlockedEvents()
  {
    return mBlockedEvents;
  }

  public Set<NodeProxy> getNodes()
  {
    return mNodes;
  }

  public Collection<EdgeProxy> getEdges()
  {
    return mEdges;
  }


  //#########################################################################
  //# Data Members
  private final boolean mIsDeterministic;
  private final LabelBlockProxy mBlockedEvents;
  private final Set<NodeProxy> mNodes;
  private final Collection<EdgeProxy> mEdges;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 2791138918150034103L;

}
