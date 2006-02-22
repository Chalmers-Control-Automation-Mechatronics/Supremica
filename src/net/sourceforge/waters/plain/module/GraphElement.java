//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   GraphElement
//###########################################################################
//# $Id: GraphElement.java,v 1.3 2006-02-22 03:35:07 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.base.Geometry;
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
   * @param blockedEvents The list of blocked events of the new graph.
   * @param nodes The set of nodes of the new graph, or <CODE>null</CODE> if empty.
   * @param edges The list of edges of the new graph, or <CODE>null</CODE> if empty.
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
      final List<EdgeProxy> edgesModifiable =
        new ArrayList<EdgeProxy>(edges);
      mEdges =
        Collections.unmodifiableList(edgesModifiable);
    }
  }

  /**
   * Creates a new graph using default values.
   * This constructor creates a graph with
   * the determinism status set to <CODE>true</CODE>,
   * an empty set of nodes, and
   * an empty list of edges.
   * @param blockedEvents The list of blocked events of the new graph.
   */
  public GraphElement(final LabelBlockProxy blockedEvents)
  {
    this(true,
         blockedEvents,
         emptyNodeProxySet(),
         emptyEdgeProxyList());
  }


  //#########################################################################
  //# Cloning
  public GraphElement clone()
  {
    return (GraphElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final GraphElement downcast = (GraphElement) partner;
      return
        (mIsDeterministic == downcast.mIsDeterministic) &&
        mBlockedEvents.equals(downcast.mBlockedEvents) &&
        mNodes.equals(downcast.mNodes) &&
        mEdges.equals(downcast.mEdges);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final GraphElement downcast = (GraphElement) partner;
      return
        (mIsDeterministic == downcast.mIsDeterministic) &&
        mBlockedEvents.equalsWithGeometry(downcast.mBlockedEvents) &&
        Geometry.equalSet(mNodes, downcast.mNodes) &&
        Geometry.equalList(mEdges, downcast.mEdges);
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

  public List<EdgeProxy> getEdges()
  {
    return mEdges;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static Set<NodeProxy> emptyNodeProxySet()
  {
    return Collections.emptySet();
  }

  private static List<EdgeProxy> emptyEdgeProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private final boolean mIsDeterministic;
  private final LabelBlockProxy mBlockedEvents;
  private final Set<NodeProxy> mNodes;
  private final List<EdgeProxy> mEdges;

}
