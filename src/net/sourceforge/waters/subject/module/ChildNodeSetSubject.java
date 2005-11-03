//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ChildNodeSetSubject
//###########################################################################
//# $Id: ChildNodeSetSubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.base.DocumentSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.SetSubject;
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
    mProxySet = new HashSet<NodeSubject>(size);
  }

  /**
   * Creates a hash set.
   * This method creates a set of {@link subject} objects from a collection
   * of {@link Proxy} by dynamically checking the type of each object.
   * The constructor fails if any of the provided objects is not of the
   * appropriate type.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new set.
   * @param  clazz       The class of the objects to be added to the set.
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
      cloned.mProxySet = new HashSet<NodeSubject>(mProxySet);
      return cloned;
    } catch (final CloneNotSupportedException exception) {
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
      if (!contains(item2)) {
        return false;
      } else {
        for (final NodeSubject node1 : this) {
          if (node1.equals(item2)) {
            if (node1.equalsWithGeometry(item2)) {
              break;
            } else {
              return false;
            }
          }
        }
      }
    }
    return true;
  }


  //#########################################################################
  //# Interface java.util.Set
  public boolean add(final NodeSubject node)
  {
    if (!contains(node)) {
      final GroupNodeSubject group = (GroupNodeSubject) getParent();
      final NodeSetSubject nodeset =
        group == null ? null : (NodeSetSubject) group.getParent();
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
  //# Printing
  public String getShortClassName()
  {
    final Class clazz = getClass();
    final String fullclazzname = clazz.getName();
    final int dotpos = fullclazzname.lastIndexOf('.');
    return fullclazzname.substring(dotpos + 1);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void afterAdd(final NodeSubject proxy)
  {
    final ModelChangeEvent event =
      ModelChangeEvent.createItemAdded(this, proxy);
    fireModelChanged(event);
  }

  private void afterRemove(final Subject subject)
  {
    final ModelChangeEvent event =
      ModelChangeEvent.createItemRemoved(this, subject);
    fireModelChanged(event);
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
