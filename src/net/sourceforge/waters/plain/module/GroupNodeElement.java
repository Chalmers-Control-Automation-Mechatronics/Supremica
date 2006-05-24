//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   GroupNodeElement
//###########################################################################
//# $Id: GroupNodeElement.java,v 1.5 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import net.sourceforge.waters.model.base.Geometry;
import net.sourceforge.waters.model.base.IndexedHashSet;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;


/**
 * An immutable implementation of the {@link GroupNodeProxy} interface.
 *
 * @author Robi Malik
 */

public final class GroupNodeElement
  extends NodeElement
  implements GroupNodeProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new group node.
   * @param name The name of the new group node.
   * @param propositions The list of propositions of the new group node.
   * @param immediateChildNodes The set of immediate child nodes of the new group node, or <CODE>null</CODE> if empty.
   * @param geometry The geometric information of the new group node, or <CODE>null</CODE>.
   */
  public GroupNodeElement(final String name,
                          final EventListExpressionProxy propositions,
                          final Collection<? extends NodeProxy> immediateChildNodes,
                          final BoxGeometryProxy geometry)
  {
    super(name, propositions);
    if (immediateChildNodes == null) {
      mImmediateChildNodes = Collections.emptySet();
    } else {
      final Set<NodeProxy> immediateChildNodesModifiable =
        new IndexedHashSet<NodeProxy>(immediateChildNodes);
      mImmediateChildNodes =
        Collections.unmodifiableSet(immediateChildNodesModifiable);
    }
    mGeometry = geometry;
  }

  /**
   * Creates a new group node using default values.
   * This constructor creates a group node with
   * an empty set of immediate child nodes and
   * the geometric information set to <CODE>null</CODE>.
   * @param name The name of the new group node.
   * @param propositions The list of propositions of the new group node.
   */
  public GroupNodeElement(final String name,
                          final EventListExpressionProxy propositions)
  {
    this(name,
         propositions,
         emptyNodeProxySet(),
         null);
  }


  //#########################################################################
  //# Cloning
  public GroupNodeElement clone()
  {
    return (GroupNodeElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final GroupNodeElement downcast = (GroupNodeElement) partner;
      return
        mImmediateChildNodes.equals(downcast.mImmediateChildNodes);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final GroupNodeElement downcast = (GroupNodeElement) partner;
      return
        Geometry.equalSet(mImmediateChildNodes, downcast.mImmediateChildNodes) &&
        Geometry.equalGeometry(mGeometry, downcast.mGeometry);
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
    return downcast.visitGroupNodeProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.GroupNodeProxy
  public Set<NodeProxy> getImmediateChildNodes()
  {
    return mImmediateChildNodes;
  }

  public BoxGeometryProxy getGeometry()
  {
    return mGeometry;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static Set<NodeProxy> emptyNodeProxySet()
  {
    return Collections.emptySet();
  }


  //#########################################################################
  //# Data Members
  private final Set<NodeProxy> mImmediateChildNodes;
  private final BoxGeometryProxy mGeometry;

}
