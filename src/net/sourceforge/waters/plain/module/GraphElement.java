//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   GraphElement
//###########################################################################
//# $Id: GraphElement.java,v 1.9 2006-08-18 06:39:29 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.base.EqualCollection;
import net.sourceforge.waters.model.base.Proxy;
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
   * an empty set of nodes, and
   * an empty collection of edges.
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
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final GraphElement downcast = (GraphElement) partner;
      return
        (mIsDeterministic == downcast.mIsDeterministic) &&
        mBlockedEvents.equalsByContents(downcast.mBlockedEvents) &&
        EqualCollection.isEqualSetByContents
          (mNodes, downcast.mNodes) &&
        EqualCollection.isEqualCollectionByContents
          (mEdges, downcast.mEdges);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final GraphElement downcast = (GraphElement) partner;
      return
        (mIsDeterministic == downcast.mIsDeterministic) &&
        mBlockedEvents.equalsWithGeometry(downcast.mBlockedEvents) &&
        EqualCollection.isEqualSetWithGeometry
          (mNodes, downcast.mNodes) &&
        EqualCollection.isEqualCollectionWithGeometry
          (mEdges, downcast.mEdges);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    if (mIsDeterministic) {
      result++;
    }
    result *= 5;
    result += mBlockedEvents.hashCodeByContents();
    result *= 5;
    result += EqualCollection.getSetHashCodeByContents(mNodes);
    result *= 5;
    result += EqualCollection.getCollectionHashCodeByContents(mEdges);
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    if (mIsDeterministic) {
      result++;
    }
    result *= 5;
    result += mBlockedEvents.hashCodeWithGeometry();
    result *= 5;
    result += EqualCollection.getSetHashCodeWithGeometry(mNodes);
    result *= 5;
    result += EqualCollection.getCollectionHashCodeWithGeometry(mEdges);
    return result;
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
  private final Collection<EdgeProxy> mEdges;

}
