//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ChildNodeSetSubject
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.module;

import gnu.trove.THashSet;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.base.DocumentSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.RecursiveUndoInfo;
import net.sourceforge.waters.subject.base.ReplacementUndoInfo;
import net.sourceforge.waters.subject.base.SetSubject;
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

class ChildNodeSetSubject
  extends AbstractSet<NodeSubject>
  implements SetSubject<NodeSubject>, Cloneable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty hash set.
   */
  public ChildNodeSetSubject()
  {
    this(0);
  }

  /**
   * Creates an empty hash set.
   * @param  size        The initial size of the hash table.
   */
  public ChildNodeSetSubject(final int size)
  {
    mProxySet = new THashSet<NodeSubject>(size);
  }

  /**
   * Creates a hash set.
   * This method creates a set of {@link Subject} objects from a collection
   * of {@link Proxy} by dynamically checking the type of each object.
   * The constructor fails if any of the provided objects is not of the
   * appropriate type.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new set.
   * @throws ClassCastException to indicate that some of the elements of the
   *                     input does not match the type of this set.
   */
  public ChildNodeSetSubject(final Collection<? extends NodeProxy> input)
  {
    this(input.size());
    for (final Proxy proxy : input) {
      final NodeSubject downcast = (NodeSubject) proxy;
      add(downcast);
    }
  }


  //#########################################################################
  //# Cloning
  public ChildNodeSetSubject clone()
  {
    try {
      final ChildNodeSetSubject cloned = (ChildNodeSetSubject) super.clone();
      cloned.mParent = null;
      cloned.mObservers = null;
      cloned.mProxySet = new THashSet<NodeSubject>(mProxySet);
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface java.util.Set
  public boolean add(final NodeSubject node)
  {
    if (!contains(node)) {
      final GroupNodeSubject group = (GroupNodeSubject) getParent();
      final NodeSetSubject nodeset =
        group == null ? null : (NodeSetSubject) group.getParent();
      if (nodeset != null && nodeset != node.getParent()) {
        throw new IllegalArgumentException
          ("Trying to add node '" + node.getName() +
           "' as child of group node of a different graph!");
      }
      try {
        mProxySet.add(node);
        if (nodeset != null) {
          nodeset.rearrangeGroupNodes();
        }
        afterAdd(node);
        return true;
      } catch (final CyclicGroupNodeException exception) {
        mProxySet.remove(node);
        final StringBuffer buffer = new StringBuffer();
        buffer.append("Adding child '");
        buffer.append(node.getName());
        if (group != null) {
          buffer.append("' to group node '");
          buffer.append(group.getName());
        }
        buffer.append('\'');
        exception.putOperation(buffer.toString());
        throw exception;
      }
    } else {
      return false;
    }
  }

  public boolean contains(final Object object)
  {
    if (object instanceof NodeSubject) {
      return mProxySet.contains(object);
    } else {
      return false;
    }
  }

  public Iterator<NodeSubject> iterator()
  {
    return new ChildNodeSetIterator();
  }

  public boolean remove(final Object victim)
  {
    if (contains(victim)) {
      final NodeSubject node = (NodeSubject) victim;
      mProxySet.remove(node);
      afterRemove(node);
      return true;
    } else {
      return false;
    }
  }

  public int size()
  {
    return mProxySet.size();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.SetSubject
  public UndoInfo createUndoInfo(final Set<? extends NodeSubject> newSet,
                                 final Set<? extends Subject> boundary)
  {
    if (boundary != null && boundary.contains(this)) {
      return null;
    }
    final RecursiveUndoInfo info = new RecursiveUndoInfo(this);
    for (final NodeSubject oldNode : this) {
      if (!newSet.contains(oldNode)) {
        final UndoInfo remove = new ReplacementUndoInfo(oldNode, null);
        info.add(remove);
      }
    }
    for (final NodeSubject newNode : newSet) {
      if (!contains(newNode)) {
        final UndoInfo add = new ReplacementUndoInfo(null, newNode);
        info.add(add);
      }
    }
    if (info.isEmpty()) {
      return null;
    } else {
      return info;
    }
  }

  public ModelChangeEvent assignMember(final int index,
                                       final Object oldValue,
                                       final Object newValue)
  {
    if (oldValue != null) {
      remove(oldValue);
    }
    if (newValue != null) {
      final NodeSubject node = (NodeSubject) newValue;
      add(node);
    }
    return null;
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
      final StringBuffer buffer = new StringBuffer();
      buffer.append("Trying to redefine parent of ");
      buffer.append(getShortClassName());
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

  public Collection<ModelObserver> getModelObservers()
  {
    return mObservers;
  }


  //#########################################################################
  //# Printing
  public String getShortClassName()
  {
    final Class<?> clazz = getClass();
    final String fullclazzname = clazz.getName();
    final int dotpos = fullclazzname.lastIndexOf('.');
    return fullclazzname.substring(dotpos + 1);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void afterAdd(final NodeSubject node)
  {
    final ModelChangeEvent event =
      ModelChangeEvent.createItemAdded(this, node);
    event.fire();
  }

  private void afterRemove(final Subject subject)
  {
    final ModelChangeEvent event =
      ModelChangeEvent.createItemRemoved(this, subject);
    event.fire();
  }


  //#########################################################################
  //# Inner Class ChildNodeSetIterator
  private class ChildNodeSetIterator implements Iterator<NodeSubject>
  {

    //#######################################################################
    //# Constructors
    private ChildNodeSetIterator()
    {
      mIterator = mProxySet.iterator();
    }


    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mIterator.hasNext();
    }

    public NodeSubject next()
    {
      mVictim = mIterator.next();
      return mVictim;
    }

    public void remove()
    {
      mIterator.remove();
      afterRemove(mVictim);
      mVictim = null;
    }


    //#######################################################################
    //# Data Members
    private final Iterator<NodeSubject> mIterator;
    private NodeSubject mVictim;

  }


  //#########################################################################
  //# Data Members
  /**
   * The parent of this element in the containment hierarchy.
   * The parent is the element that directly contains this element
   * in the document structure given by the XML file.
   * @see Subject#getParent()
   */
  private Subject mParent;
  /**
   * The list of registered listeners.
   * This member is set to <CODE>null</CODE> if no listeners are registered.
   */
  private Collection<ModelObserver> mObservers;
  /**
   * The contents of this set, indexed by their names.
   */
  private Set<NodeSubject> mProxySet;

}
