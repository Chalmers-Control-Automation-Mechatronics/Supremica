//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   GroupNodeSubject
//###########################################################################
//# $Id: GroupNodeSubject.java,v 1.9 2006-08-01 04:14:47 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import net.sourceforge.waters.model.base.EqualCollection;
import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
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
                          final PlainEventListProxy propositions,
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
                          final PlainEventListProxy propositions)
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
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final GroupNodeSubject downcast = (GroupNodeSubject) partner;
      return
        EqualCollection.isEqualSetByContents
          (mImmediateChildNodes, downcast.mImmediateChildNodes);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final GroupNodeSubject downcast = (GroupNodeSubject) partner;
      return
        EqualCollection.isEqualSetWithGeometry
          (mImmediateChildNodes, downcast.mImmediateChildNodes) &&
        (mGeometry == null ? downcast.mGeometry == null :
         mGeometry.equalsWithGeometry(downcast.mGeometry));
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += EqualCollection.getSetHashCodeByContents(mImmediateChildNodes);
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += EqualCollection.getSetHashCodeWithGeometry(mImmediateChildNodes);
    result *= 5;
    if (mGeometry != null) {
      result += mGeometry.hashCodeWithGeometry();
    }
    return result;
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
