//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
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

package net.sourceforge.waters.subject.module;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.base.DocumentSubject;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.UndoInfo;


/**
 * <P>An implementation of the {@link java.util.Set} interface that can
 * handle the set of nodes of a graph.</P>
 *
 * <P>This implementation supports the hierarchical structure of node
 * sets and ensures that no cyclical dependencies can be created when
 * group nodes are added or modified.</P>
 *
 * @author Robi Malik
 */

class NodeSetSubject
  extends AbstractSet<NodeSubject>
  implements IndexedSetSubject<NodeSubject>
{

  //#########################################################################
  //# Constructors
  NodeSetSubject()
  {
    this(0);
  }

  NodeSetSubject(final int size)
  {
    mSimpleNodes = new LinkedList<SimpleNodeSubject>();
    mGroupNodes = new LinkedList<GroupNodeSubject>();
    mNameMap = new HashMap<String,NodeSubject>(size);
  }

  NodeSetSubject(final Collection<? extends NodeProxy> input)
  {
    this(input.size());
    @SuppressWarnings("unchecked")
    final Collection<NodeSubject> downcast = (Collection<NodeSubject>) input;
    insertAllUnique(downcast);
  }


  //#########################################################################
  //# Interface java.util.Set
  @Override
  public boolean add(final NodeSubject node)
  {
    return insert(node) == node;
  }

  @Override
  public boolean addAll(final Collection<? extends NodeSubject> items)
  {
    return insertAll(items);
  }

  @Override
  public boolean contains(final Object item)
  {
    if (item instanceof NodeSubject) {
      final NodeSubject node = (NodeSubject) item;
      return contains(node);
    } else {
      return false;
    }
  }

  @Override
  public Iterator<NodeSubject> iterator()
  {
    return new NodeSetIterator();
  }

  @Override
  public boolean remove(final Object item)
  {
    if (item instanceof NodeSubject) {
      final NodeSubject node = (NodeSubject) item;
      return remove(node);
    } else {
      return false;
    }
  }

  @Override
  public int size()
  {
    return mNameMap.size();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.IndexedCollectionProxy
  @Override
  public void checkAllUnique
    (final Collection<? extends NodeSubject> collection)
  {
    for (final NodeSubject node : collection) {
      checkUnique(node);
    }
  }

  @Override
  public void checkUnique(final NamedProxy node)
  {
    final String name = node.getName();
    final NodeSubject found = find(name);
    if (found != node) {
      throw createItemNotFound(name);
    }
  }

  @Override
  public boolean containsName(final String name)
  {
    return mNameMap.containsKey(name);
  }

  @Override
  public NodeSubject find(final String name)
  {
    final NodeSubject found = get(name);
    if (found == null) {
      throw createNameNotFound(name);
    }
    return found;
  }

  @Override
  public NodeSubject get(final String name)
  {
    return mNameMap.get(name);
  }

  @Override
  public NodeSubject insert(final NodeSubject node)
  {
    final Collection<NodeSubject> nodes = Collections.singletonList(node);
    if (insertAll(nodes)) {
      return node;
    } else {
      final String name = node.getName();
      return get(name);
    }
  }

  @Override
  public boolean insertAll(final Collection<? extends NodeSubject> collection)
  {
    boolean changed = false;
    final Collection<GroupNodeSubject> groups =
      new LinkedList<GroupNodeSubject>();
    for (final NodeProxy node : collection) {
      final String name = node.getName();
      final NodeSubject found = get(name);
      if (found != null) {
        if (!found.equals(node)) {
          throw createDuplicateName(name);
        }
      } else if (node instanceof SimpleNodeSubject) {
        final SimpleNodeSubject simple = (SimpleNodeSubject) node;
        insertSimpleNode(simple);
        changed = true;
      } else if (node instanceof GroupNodeSubject) {
        final GroupNodeSubject group = (GroupNodeSubject) node;
        groups.add(group);
        changed = true;
      } else {
        throw createBadType(node);
      }
    }
    insertGroupNodes(groups);
    return changed;
  }

  @Override
  public void insertAllUnique
    (final Collection<? extends NodeSubject> collection)
  {
    final Collection<GroupNodeSubject> groups =
      new LinkedList<GroupNodeSubject>();
    for (final NodeProxy node : collection) {
      final String name = node.getName();
      final NodeSubject found = get(name);
      if (found != null) {
        throw createDuplicateName(name);
      } else if (node instanceof SimpleNodeSubject) {
        final SimpleNodeSubject simple = (SimpleNodeSubject) node;
        insertSimpleNode(simple);
      } else if (node instanceof GroupNodeSubject) {
        final GroupNodeSubject group = (GroupNodeSubject) node;
        groups.add(group);
      } else {
        throw createBadType(node);
      }
    }
    insertGroupNodes(groups);
  }

  @Override
  public void insertUnique(final NodeSubject node)
  {
    final Collection<NodeSubject> nodes = Collections.singletonList(node);
    insertAllUnique(nodes);
  }

  @Override
  public void reinsert(final NamedProxy proxy, final String newname)
  {
    final String oldname = proxy.getName();
    if (mNameMap.get(oldname) != proxy) {
      throw createItemNotFound(oldname);
    } else if (mNameMap.containsKey(newname)) {
      throw createDuplicateName(newname);
    }
    final Map<?,?> precast = mNameMap;
    @SuppressWarnings("unchecked")
    final Map<String,NamedProxy> map = (Map<String,NamedProxy>) precast;
    map.remove(oldname);
    map.put(newname, proxy);
  }

  @Override
  public NodeSubject removeName(final String name)
  {
    final NodeSubject victim = get(name);
    if (victim != null) {
      removeContainedNode(victim);
    }
    return victim;
  }


  //#########################################################################
  //# Specific Access Methods for Nodes
  public boolean contains(final NodeSubject node)
  {
    final String name = node.getName();
    final NodeSubject found = get(name);
    return found != null && found.equals(node);
  }

  public boolean remove(final NodeSubject node)
  {
    final String name = node.getName();
    final NodeSubject victim = get(name);
    if (victim != null && victim.equals(node)) {
      removeContainedNode(victim);
      return true;
    } else {
      return false;
    }
  }

  void rearrangeGroupNodes()
  {
    final Collection<GroupNodeSubject> empty = Collections.emptyList();
    rearrangeGroupNodes(empty);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.SetSubject
  @Override
  public UndoInfo createUndoInfo(final Set<? extends NodeSubject> newState,
                                 final Set<? extends Subject> boundary)
  {
    if (boundary != null && boundary.contains(this)) {
      return null;
    }
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true);
    if (eq.isEqualSet(this, newState)) {
      return null;
    } else {
      throw new UnsupportedOperationException
        ("Node set assignment not yet implemented!");
    }
  }

  @Override
  public ModelChangeEvent assignMember(final int index,
                                       final Object oldValue,
                                       final Object newValue)
  {
    throw new UnsupportedOperationException
      ("Node set assignment not yet implemented!");
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.Subject
  @Override
  public Subject getParent()
  {
    return mParent;
  }

  @Override
  public DocumentSubject getDocument()
  {
    if (mParent != null) {
      return mParent.getDocument();
    } else {
      return null;
    }
  }

  @Override
  public void setParent(final Subject parent)
  {
    checkSetParent(parent);
    mParent = parent;
  }

  @Override
  public void checkSetParent(final Subject parent)
  {
    if (parent != null && mParent != null) {
      final Class<?> clazz = getClass();
      final StringBuilder buffer = new StringBuilder();
      buffer.append("Trying to redefine parent of ");
      buffer.append(getShortClassName(clazz));
      buffer.append('!');
      throw new IllegalStateException(buffer.toString());
    }
  }

  @Override
  public void addModelObserver(final ModelObserver observer)
  {
    if (mObservers == null) {
      mObservers = new LinkedList<ModelObserver>();
    }
    mObservers.add(observer);
  }

  @Override
  public void removeModelObserver(final ModelObserver observer)
  {
    if (mObservers != null &&
        mObservers.remove(observer) &&
        mObservers.isEmpty()) {
      mObservers = null;
    }
  }

  @Override
  public Collection<ModelObserver> getModelObservers()
  {
    return mObservers;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void insertSimpleNode(final SimpleNodeSubject node)
  {
    node.checkSetParent(this);
    mSimpleNodes.add(node);
    completeAdd(node);
  }

  private void insertGroupNodes(final Collection<GroupNodeSubject> groups)
  {
    if (!groups.isEmpty()) {
      for (final GroupNodeSubject group : groups) {
        final String name = group.getName();
        if (mNameMap.containsKey(name)) {
          throw createDuplicateName(name);
        } else {
          group.checkSetParent(this);
        }
      }
      rearrangeGroupNodes(groups);
      for (final GroupNodeSubject group : groups) {
        completeAdd(group);
      }
    }
  }

  @SuppressWarnings("unused")
  private void insertPreOrderedNode(final NodeSubject node)
  {
    final String name = node.getName();
    if (mNameMap.containsKey(name)) {
      throw createDuplicateName(name);
    }
    node.checkSetParent(this);
    if (node instanceof SimpleNodeSubject) {
      final SimpleNodeSubject simple = (SimpleNodeSubject) node;
      mSimpleNodes.add(simple);
    } else if (node instanceof GroupNodeSubject) {
      final GroupNodeSubject group = (GroupNodeSubject) node;
      mGroupNodes.add(group);
    } else {
      throw createBadType(node);
    }
    completeAdd(node);
  }

  private void rearrangeGroupNodes
    (final Collection<GroupNodeSubject> additional)
  {
    final Iterator<GroupNodeSubject> iter = new GroupNodeIterator(additional);
    final Collection<GroupNodeSubject> newgroups =
      new LinkedList<GroupNodeSubject>();
    while (iter.hasNext()) {
      newgroups.add(iter.next());
    }
    mGroupNodes = newgroups;
  }

  private void removeContainedNode(final NodeSubject victim)
  {
    if (victim instanceof SimpleNodeSubject) {
      mSimpleNodes.remove(victim);
    } else if (victim instanceof GroupNodeSubject) {
      mGroupNodes.remove(victim);
    } else {
      throw createBadType(victim);
    }
    completeRemove(victim);
  }

  private void completeAdd(final NodeSubject node)
  {
    final String name = node.getName();
    mNameMap.put(name, node);
    node.setParent(this);
    final ModelChangeEvent event =
      ModelChangeEvent.createItemAdded(this, node);
    event.fire();
  }

  private void completeRemove(final NodeSubject node)
  {
    final String name = node.getName();
    mNameMap.remove(name);
    node.setParent(null);
    final ModelChangeEvent event =
      ModelChangeEvent.createItemRemoved(this, node);
    event.fire();
  }


  //#########################################################################
  //# Exceptions
  private ItemNotFoundException createItemNotFound(final String name)
  {
    final StringBuilder buffer = new StringBuilder();
    appendContainerName(buffer);
    buffer.append(" does not contain the node '");
    buffer.append(name);
    buffer.append("'!");
    return new ItemNotFoundException(buffer.toString());
  }

  private NameNotFoundException createNameNotFound(final String name)
  {
    final StringBuilder buffer = new StringBuilder();
    appendContainerName(buffer);
    buffer.append(" does not contain any node named '");
    buffer.append(name);
    buffer.append("'!");
    return new NameNotFoundException(buffer.toString());
  }

  private DuplicateNameException createDuplicateName(final String name)
  {
    final StringBuilder buffer = new StringBuilder();
    appendContainerName(buffer);
    buffer.append(" contains more than one node named '");
    buffer.append(name);
    buffer.append("'!");
    return new DuplicateNameException(buffer.toString());
  }

  private ClassCastException createBadType(final NodeProxy node)
  {
    final StringBuilder buffer = new StringBuilder("Unsupported node class ");
    final Class<?> clazz = node.getClass();
    final String name = getShortClassName(clazz);
    buffer.append(name);
    buffer.append("!");
    return new ClassCastException(buffer.toString());
  }

  @SuppressWarnings("unused")
  private ClassCastException createBadType(final Object item)
  {
    final StringBuilder buffer = new StringBuilder("Can't add object of class ");
    final Class<?> clazz = item.getClass();
    final String name = getShortClassName(clazz);
    buffer.append(name);
    buffer.append(" to node set!");
    return new ClassCastException(buffer.toString());
  }

  private void appendContainerName(final StringBuilder buffer)
  {
    final Subject parent = getParent();
    final ComponentProxy comp =
      parent == null ? null : (ComponentProxy) parent.getParent();
    if (comp == null) {
      final Class<?> clazz = getClass();
      final String name = getShortClassName(clazz);
      buffer.append(name);
    } else {
      buffer.append("graph '");
      buffer.append(comp.getName());
      buffer.append('\'');
    }
  }

  private String getShortClassName(final Class<?> clazz)
  {
    final String fullclazzname = clazz.getName();
    final int dotpos = fullclazzname.lastIndexOf('.');
    return fullclazzname.substring(dotpos + 1);
  }


  //#########################################################################
  //# Local Class NodeSetIterator
  private class NodeSetIterator implements Iterator<NodeSubject>
  {

    //#######################################################################
    //# Constructors
    private NodeSetIterator()
    {
      mSimplePart = true;
      mIterator = mSimpleNodes.iterator();
      mVictim = null;
    }

    //#######################################################################
    //# Interface java.util.Iterator
    @Override
    public boolean hasNext()
    {
      if (mIterator.hasNext()) {
        return true;
      } else if (mSimplePart) {
        mSimplePart = false;
        mIterator = mGroupNodes.iterator();
        return mIterator.hasNext();
      } else {
        return false;
      }
    }

    @Override
    public NodeSubject next()
    {
      if (!mIterator.hasNext() && mSimplePart) {
        mSimplePart = false;
        mIterator = mGroupNodes.iterator();
      }
      mVictim = mIterator.next();
      return mVictim;
    }

    @Override
    public void remove()
    {
      mIterator.remove();
      completeRemove(mVictim);
      mVictim = null;
    }

    //#######################################################################
    //# Data Members
    private Iterator<? extends NodeSubject> mIterator;
    private boolean mSimplePart;
    private NodeSubject mVictim;

  }


  //#########################################################################
  //# Local Class GroupNodeIterator
  private class GroupNodeIterator implements Iterator<GroupNodeSubject>
  {

    //#######################################################################
    //# Constructors
    private GroupNodeIterator()
    {
      mUnvisited = new LinkedList<GroupNodeSubject>(mGroupNodes);
      mVisited = new IdentityHashMap<GroupNodeSubject,GroupNodeSubject>
        (mGroupNodes.size());
      mIterator = mUnvisited.iterator();
      mNextNode = null;
      mGotNode = false;
    }

    private GroupNodeIterator
      (final Collection<? extends GroupNodeSubject> additional)
    {
      mUnvisited = new LinkedList<GroupNodeSubject>(mGroupNodes);
      mUnvisited.addAll(additional);
      mVisited = new IdentityHashMap<GroupNodeSubject,GroupNodeSubject>
        (mGroupNodes.size());
      mIterator = mUnvisited.iterator();
      mNextNode = null;
      mGotNode = false;
    }


    //#######################################################################
    //# Interface java.util.Iterator
    @Override
    public boolean hasNext()
      throws CyclicGroupNodeException
    {
      advance();
      return mNextNode != null;
    }

    @Override
    public GroupNodeSubject next()
    {
      advance();
      final GroupNodeSubject result = mNextNode;
      if (result != null) {
        mNextNode = null;
        return result;
      } else {
        throw new NoSuchElementException
          ("End of group node iteration reached!");
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        ("Can't remove in group node iteration!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private void advance()
    {
      while (mNextNode == null) {
        if (!mIterator.hasNext()) {
          if (mUnvisited.isEmpty()) {
            return;
          } else if (mGotNode) {
            mGotNode = false;
            mIterator = mUnvisited.iterator();
          } else {
            final GroupNodeSubject cyclic = mUnvisited.get(0);
            throw new CyclicGroupNodeException
              ("Presence of '" + cyclic.getName() + "'");
          }
        }
        final GroupNodeSubject node = mIterator.next();
        if (childrenVisited(node)) {
          mVisited.put(node, node);
          mIterator.remove();
          mNextNode = node;
          mGotNode = true;
        }
      }
    }

    private boolean childrenVisited(final GroupNodeSubject node)
    {
      final Collection<NodeProxy> children = node.getImmediateChildNodes();
      for (final NodeProxy child : children) {
        if (child instanceof GroupNodeSubject &&
            !mVisited.containsKey(child)) {
          return false;
        }
      }
      return true;
    }


    //#######################################################################
    //# Data Members
    private final List<GroupNodeSubject> mUnvisited;
    private final Map<GroupNodeSubject,GroupNodeSubject> mVisited;
    private Iterator<GroupNodeSubject> mIterator;
    private GroupNodeSubject mNextNode;
    private boolean mGotNode;

  }


  //#########################################################################
  //# Data Members
  /**
   * The parent of this element in the containment hierarchy.
   * The parent is the element that directly contains this element
   * in the document structure given by the XML file.
   * // @see Proxy#getParent()
   */
  private Subject mParent;
  /**
   * The list of registered observers.
   * This member is set to <CODE>null</CODE> if no observers are registered.
   */
  private Collection<ModelObserver> mObservers;

  /**
   * The simple nodes contained in this set.
   */
  private final Collection<SimpleNodeSubject> mSimpleNodes;
  /**
   * The group nodes contained in this set.
   */
  private Collection<GroupNodeSubject> mGroupNodes;
  /**
   * All nodes in this set, indexed by their names.
   */
  private final Map<String,NodeSubject> mNameMap;


}
