//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   GraphSubject
//###########################################################################
//# $Id: GraphSubject.java,v 1.6 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

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
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.MutableSubject;


/**
 * The subject implementation of the {@link GraphProxy} interface.
 *
 * @author Robi Malik
 */

public final class GraphSubject
  extends MutableSubject
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
  public GraphSubject(final boolean deterministic,
                      final LabelBlockProxy blockedEvents,
                      final Collection<? extends NodeProxy> nodes,
                      final Collection<? extends EdgeProxy> edges)
  {
    mIsDeterministic = deterministic;
    mBlockedEvents = (LabelBlockSubject) blockedEvents;
    mBlockedEvents.setParent(this);
    if (nodes == null) {
      mNodes = new NodeSetSubject();
    } else {
      mNodes = new NodeSetSubject(nodes);
    }
    mNodes.setParent(this);
    if (edges == null) {
      mEdges = new ArrayListSubject<EdgeSubject>();
    } else {
      mEdges = new ArrayListSubject<EdgeSubject>
        (edges, EdgeSubject.class);
    }
    mEdges.setParent(this);
  }

  /**
   * Creates a new graph using default values.
   * This constructor creates a graph with
   * the determinism status set to <CODE>true</CODE>,
   * an empty set of nodes, and
   * an empty list of edges.
   * @param blockedEvents The list of blocked events of the new graph.
   */
  public GraphSubject(final LabelBlockProxy blockedEvents)
  {
    this(true,
         blockedEvents,
         emptyNodeProxySet(),
         emptyEdgeProxyList());
  }


  //#########################################################################
  //# Cloning
  public GraphSubject clone()
  {
    final GraphSubject cloned = (GraphSubject) super.clone();
    cloned.mBlockedEvents = mBlockedEvents.clone();
    cloned.mBlockedEvents.setParent(cloned);
    cloned.mNodes = mNodes.clone();
    cloned.mNodes.setParent(cloned);
    cloned.mEdges =
      new ArrayListSubject<EdgeSubject>(mEdges.size());
    for (final EdgeSubject edge : mEdges) {
      final EdgeSubject clonededge = edge.clone(cloned.mNodes);
      cloned.mEdges.add(clonededge);
    }
    cloned.mEdges.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final GraphSubject downcast = (GraphSubject) partner;
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
      final GraphSubject downcast = (GraphSubject) partner;
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

  public LabelBlockSubject getBlockedEvents()
  {
    return mBlockedEvents;
  }

  public Set<NodeProxy> getNodes()
  {
    final Set<NodeProxy> downcast = Casting.toSet(mNodes);
    return Collections.unmodifiableSet(downcast);
  }

  public List<EdgeProxy> getEdges()
  {
    final List<EdgeProxy> downcast = Casting.toList(mEdges);
    return Collections.unmodifiableList(downcast);
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the determinism status of this graph.
   */
  public void setDeterministic(final boolean deterministic)
  {
    if (mIsDeterministic == deterministic) {
      return;
    }
    mIsDeterministic = deterministic;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  /**
   * Sets the list of blocked events of this graph.
   */
  public void setBlockedEvents(final LabelBlockSubject blockedEvents)
  {
    if (mBlockedEvents == blockedEvents) {
      return;
    }
    blockedEvents.setParent(this);
    mBlockedEvents.setParent(null);
    mBlockedEvents = blockedEvents;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  /**
   * Gets the modifiable set of nodes of this graph.
   */
  public NodeSetSubject getNodesModifiable()
  {
    return mNodes;
  }

  /**
   * Gets the modifiable list of edges of this graph.
   */
  public ListSubject<EdgeSubject> getEdgesModifiable()
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
  private boolean mIsDeterministic;
  private LabelBlockSubject mBlockedEvents;
  private NodeSetSubject mNodes;
  private ListSubject<EdgeSubject> mEdges;

}
