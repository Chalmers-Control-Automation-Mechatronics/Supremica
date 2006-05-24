//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   GroupNodeSubject
//###########################################################################
//# $Id: GroupNodeSubject.java,v 1.7 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import net.sourceforge.waters.model.base.Geometry;
import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.SetSubject;


/**
 * The subject implementation of the {@link GroupNodeProxy} interface.
 *
 * @author Robi Malik
 */

public final class GroupNodeSubject
  extends NodeSubject
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
  public GroupNodeSubject(final String name,
                          final EventListExpressionProxy propositions,
                          final Collection<? extends NodeProxy> immediateChildNodes,
                          final BoxGeometryProxy geometry)
  {
    super(name, propositions);
    if (immediateChildNodes == null) {
      mImmediateChildNodes = new ChildNodeSetSubject();
    } else {
      mImmediateChildNodes = new ChildNodeSetSubject(immediateChildNodes);
    }
    mImmediateChildNodes.setParent(this);
    mGeometry = (BoxGeometrySubject) geometry;
    if (mGeometry != null) {
      mGeometry.setParent(this);
    }
  }

  /**
   * Creates a new group node using default values.
   * This constructor creates a group node with
   * an empty set of immediate child nodes and
   * the geometric information set to <CODE>null</CODE>.
   * @param name The name of the new group node.
   * @param propositions The list of propositions of the new group node.
   */
  public GroupNodeSubject(final String name,
                          final EventListExpressionProxy propositions)
  {
    this(name,
         propositions,
         emptyNodeProxySet(),
         null);
  }


  //#########################################################################
  //# Cloning
  public GroupNodeSubject clone()
  {
    final GroupNodeSubject cloned = (GroupNodeSubject) super.clone();
    cloned.mImmediateChildNodes =
      new ChildNodeSetSubject(mImmediateChildNodes);
    cloned.mImmediateChildNodes.setParent(cloned);
    if (mGeometry != null) {
      cloned.mGeometry = mGeometry.clone();
      cloned.mGeometry.setParent(cloned);
    }
    return cloned;
  }

  public GroupNodeSubject clone(final IndexedSet<NodeSubject> refmap)
  {
    final GroupNodeSubject cloned = (GroupNodeSubject) super.clone();
    cloned.mImmediateChildNodes =
      new ChildNodeSetSubject(mImmediateChildNodes.size());
    for (final NodeSubject item : mImmediateChildNodes) {
      final String name = item.getName();
      final NodeSubject cloneditem = refmap.find(name);
      cloned.mImmediateChildNodes.add(cloneditem);
    }
    cloned.mImmediateChildNodes.setParent(cloned);
    if (mGeometry != null) {
      cloned.mGeometry = mGeometry.clone();
      cloned.mGeometry.setParent(cloned);
    }
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final GroupNodeSubject downcast = (GroupNodeSubject) partner;
      return
        mImmediateChildNodes.equals(downcast.mImmediateChildNodes);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final GroupNodeSubject downcast = (GroupNodeSubject) partner;
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
    final Set<NodeProxy> downcast = Casting.toSet(mImmediateChildNodes);
    return Collections.unmodifiableSet(downcast);
  }

  public BoxGeometrySubject getGeometry()
  {
    return mGeometry;
  }


  //#########################################################################
  //# Setters
  /**
   * Gets the modifiable set of immediate child nodes of this group node.
   */
  public SetSubject<NodeSubject> getImmediateChildNodesModifiable()
  {
    return mImmediateChildNodes;
  }

  /**
   * Sets the geometric information of this node.
   */
  public void setGeometry(final BoxGeometrySubject geometry)
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


  //#########################################################################
  //# Additional Setters
  public void setImmediateChildNodes
    (final Collection<? extends NodeSubject> children)
  {
    final SetSubject<NodeSubject> oldchildren = mImmediateChildNodes;
    final NodeSetSubject parent = (NodeSetSubject) getParent();
    try {
      mImmediateChildNodes = new ChildNodeSetSubject(children);
      if (parent != null) {
        parent.rearrangeGroupNodes();
      }
    } catch (final CyclicGroupNodeException exception) {
      mImmediateChildNodes = oldchildren;
      exception.putOperation("Changing children of '" + getName() + "'");
      throw exception;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private static Set<NodeProxy> emptyNodeProxySet()
  {
    return Collections.emptySet();
  }


  //#########################################################################
  //# Data Members
  private SetSubject<NodeSubject> mImmediateChildNodes;
  private BoxGeometrySubject mGeometry;

}
