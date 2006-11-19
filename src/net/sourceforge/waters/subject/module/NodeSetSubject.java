//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   NodeSetSubject
//###########################################################################
//# $Id: NodeSetSubject.java,v 1.5 2006-11-19 21:12:23 robi Exp $
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
import java.util.Set;
import java.util.Map;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.DocumentSubject;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.Subject;


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
  implements IndexedSetSubject<NodeSubject>, Cloneable
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
    final Collection<NodeSubject> downcast = Casting.toCollection(input);
    insertAllUnique(downcast);
  }


  //#########################################################################
  //# Cloning
  public NodeSetSubject clone()
  {
    try {
      final NodeSetSubject cloned = (NodeSetSubject) super.clone();
      cloned.mParent = null;
      cloned.mObservers = null;
      cloned.mSimpleNodes = new LinkedList<SimpleNodeSubject>();
      cloned.mGroupNodes = new LinkedList<GroupNodeSubject>();
      cloned.mNameMap = new HashMap<String,NodeSubject>(size());
      for (final SimpleNodeSubject node : mSimpleNodes) {
	final SimpleNodeSubject clonednode = node.clone();
	cloned.insertPreOrderedNode(clonednode);
      }
      for (final GroupNodeSubject groupnode : mGroupNodes) {
	final GroupNodeSubject clonednode = groupnode.clone(cloned);
	cloned.insertPreOrderedNode(clonednode);
      }
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final DuplicateNameException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equalsWithGeometry(final Object partner)
  {
    if (!(partner instanceof Set<?>)) {
      return false;
    }
    final Set<?> set = (Set<?>) partner;
    if (size() != set.size()) {
      return false;
    }
    for (final Object item2 : set) {
      if (!(item2 instanceof NodeSubject)) {
        return false;
      }
      final NodeSubject node2 = (NodeSubject) item2;
      final String name = node2.getName();
      final NodeSubject node1 = get(name);
      if (node1 == null || !node1.equalsWithGeometry(node2)) {
        return false;
      }
    }
    return true;
  }


  //#########################################################################
  //# Interface java.util.Set
  public boolean add(final NodeSubject node)
  {
    return insert(node) == node;
  }

  public boolean addAll(final Collection<? extends NodeSubject> items)
  {
    return insertAll(items);
  }

  public boolean contains(final Object item)
  {
    if (item instanceof NodeSubject) {
      final NodeSubject node = (NodeSubject) item;
      return contains(node);
    } else {
      return false;
    }
  }

  public Iterator<NodeSubject> iterator()
  {
    return new NodeSetIterator();
  }

  public boolean remove(final Object item)
  {
    if (item instanceof NodeSubject) {
      final NodeSubject node = (NodeSubject) item;
      return remove(node);
    } else {
      return false;
    }
  }

  public int size()
  {
    return mNameMap.size();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.IndexedCollectionProxy 
  public void checkAllUnique
    (final Collection<? extends NodeSubject> collection)
  {
    for (final NodeSubject node : collection) {
      checkUnique(node);
    }
  }

  public void checkUnique(final NamedProxy node)
  {
    final String name = node.getName();
    final NodeSubject found = find(name);
    if (found != node) {
      throw createItemNotFound(name);
    }
  }

  public boolean containsName(final String name)
  {
    return mNameMap.containsKey(name);
  }

  public NodeSubject find(final String name)
  {
    final NodeSubject found = get(name);
    if (found == null) {
      throw createNameNotFound(name);
    }
    return found;
  }

  public NodeSubject get(final String name)
  {
    return mNameMap.get(name);
  }

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

  public void insertUnique(final NodeSubject node)
  {
    final Collection<NodeSubject> nodes = Collections.singletonList(node);
    insertAllUnique(nodes);
  }

  public void reinsert(final NamedProxy proxy, final String newname)
  {
    final String oldname = proxy.getName();
    if (mNameMap.get(oldname) != proxy) {
      throw createItemNotFound(oldname);
    } else if (mNameMap.containsKey(newname)) {
      throw createDuplicateName(newname);
    }
    final Map<String,NamedProxy> map = Casting.toMap(mNameMap);
    map.remove(oldname);
    map.put(newname, proxy);
  }

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
  //# Interface net.sourceforge.waters.subject.base.Subject
  public Subject getParent()
  {
    return mParent;
  }

  public DocumentSubject getDocument()
  {
    if (mParent != null) {
      return mParent.getDocument();
    } else {
      return null;
    }
  }

  public void setParent(final Subject parent)
  {
    checkSetParent(parent);
    mParent = parent;
  }

  public void checkSetParent(final Subject parent)
  {
    if (parent != null && mParent != null) {
      final Class clazz = getClass();
      final StringBuffer buffer = new StringBuffer();
      buffer.append("Trying to redefine parent of ");
      buffer.append(getShortClassName(clazz));
      buffer.append('!');
      throw new IllegalStateException(buffer.toString());
    }
  }

  public void addModelObserver(final ModelObserver observer)
  {
    if (mObservers == null) {
      mObservers = new LinkedList<ModelObserver>();
    }
    mObservers.add(observer);
  }

  public void removeModelObserver(final ModelObserver observer)
  {
    if (mObservers != null &&
        mObservers.remove(observer) &&
        mObservers.isEmpty()) {
      mObservers = null;
    }
  }

  public void fireModelChanged(final ModelChangeEvent event)
  {
    if (mObservers != null) {
      for (final ModelObserver observer : mObservers) {
        observer.modelChanged(event);
      }
    }
    if (mParent != null) {
      mParent.fireModelChanged(event);
    }
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
    node.fireModelChanged(event);
  }

  private void completeRemove(final NodeSubject node)
  {
    final String name = node.getName();
    mNameMap.remove(name);
    node.setParent(null);
    final ModelChangeEvent event =
      ModelChangeEvent.createItemRemoved(this, node);
    node.fireModelChanged(event);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Exceptions
  private ItemNotFoundException createItemNotFound(final String name)
  {
    final StringBuffer buffer = new StringBuffer();
    appendContainerName(buffer);
    buffer.append(" does not contain the node '");
    buffer.append(name);
    buffer.append("'!");
    return new ItemNotFoundException(buffer.toString());
  }

  private NameNotFoundException createNameNotFound(final String name)
  {
    final StringBuffer buffer = new StringBuffer();
    appendContainerName(buffer);
    buffer.append(" does not contain any node named '");
    buffer.append(name);
    buffer.append("'!");
    return new NameNotFoundException(buffer.toString());
  }

  private DuplicateNameException createDuplicateName(final String name)
  {
    final StringBuffer buffer = new StringBuffer();
    appendContainerName(buffer);
    buffer.append(" contains more than one node named '");
    buffer.append(name);
    buffer.append("'!");
    return new DuplicateNameException(buffer.toString());
  }

  private ClassCastException createBadType(final NodeProxy node)
  {
    final StringBuffer buffer = new StringBuffer("Unsupported node class ");
    final Class clazz = node.getClass();
    final String name = getShortClassName(clazz);
    buffer.append(name);
    buffer.append("!");
    return new ClassCastException(buffer.toString());
  }

  @SuppressWarnings("unused")
  private ClassCastException createBadType(final Object item)
  {
    final StringBuffer buffer = new StringBuffer("Can't add object of class ");
    final Class clazz = item.getClass();
    final String name = getShortClassName(clazz);
    buffer.append(name);
    buffer.append(" to node set!");
    return new ClassCastException(buffer.toString());
  }

  private void appendContainerName(final StringBuffer buffer)
  {
    final Subject parent = getParent();
    final ComponentProxy comp =
      parent == null ? null : (ComponentProxy) parent.getParent();
    if (comp == null) {
      final Class clazz = getClass();
      final String name = getShortClassName(clazz);
      buffer.append(name);
    } else {
      buffer.append("graph '");
      buffer.append(comp.getName());
      buffer.append('\'');
    }
  }

  private String getShortClassName(final Class clazz)
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

    public NodeSubject next()
    {
      if (!mIterator.hasNext() && mSimplePart) {
        mSimplePart = false;
        mIterator = mGroupNodes.iterator();
      }
      mVictim = mIterator.next();
      return mVictim;
    }

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
    public boolean hasNext()
      throws CyclicGroupNodeException
    {
      advance();
      return mNextNode != null;
    }

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
  private Collection<SimpleNodeSubject> mSimpleNodes;
  /**
   * The group nodes contained in this set.
   */
  private Collection<GroupNodeSubject> mGroupNodes;
  /**
   * All nodes in this set, indexed by their names.
   */
  private Map<String,NodeSubject> mNameMap;

}
