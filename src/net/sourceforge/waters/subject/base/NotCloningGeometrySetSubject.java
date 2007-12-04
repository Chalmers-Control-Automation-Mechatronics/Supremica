//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   NotCloningGeometrySetSubject
//###########################################################################
//# $Id: NotCloningGeometrySetSubject.java,v 1.5 2007-12-04 03:22:58 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.unchecked.Casting;


/**
 * <E>A wrapper of the {@link HashSet} class that also implements the
 * {@link Subject} interface.</P>
 *
 * <E>This is an implementation of a mutable set with full event
 * notification support. In contrast to other collection subject
 * implementations, this class is intended for geometry information, and
 * therefore fires geometry change events when a change occurs.  This
 * implementation assumes that the set elements are immutable.</P>
 *
 * @author Robi Malik
 */

public class NotCloningGeometrySetSubject<E>
  extends AbstractSet<E>
  implements SimpleSetSubject<E>, Cloneable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty array set.
   */
  public NotCloningGeometrySetSubject()
  {
    this(0);
  }

  /**
   * Creates an empty array set.
   * @param  size        The initial size of the array.
   */
  public NotCloningGeometrySetSubject(final int size)
  {
    mSet = new HashSet<E>(size);
  }

  /**
   * Creates an array set.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new set.
   */
  public NotCloningGeometrySetSubject(final Collection<? extends E> input)
  {
    mSet = new HashSet<E>(input);
  }


  //#########################################################################
  //# Cloning
  public NotCloningGeometrySetSubject<E> clone()
  {
    try {
      final Class<NotCloningGeometrySetSubject<E>> clazz =
        Casting.toClass(getClass());
      final NotCloningGeometrySetSubject<E> cloned = clazz.cast(super.clone());
      cloned.mParent = null;
      cloned.mObservers = null;
      cloned.mSet = new HashSet<E>(mSet);
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface java.util.Set
  public boolean add(final E element)
  {
    final boolean result = mSet.add(element);
    if (result) {
      fireGeometryChange();
    }
    return result;
  }

  public boolean contains(final Object object)
  {
    return mSet.contains(object);
  }

  public Iterator<E> iterator()
  {
    return new GeometrySetIterator();
  }

  public boolean remove(final Object victim)
  {
    final boolean result = mSet.remove(victim);
    if (result) {
      fireGeometryChange();
    }
    return result;
  }

  public int size()
  {
    return mSet.size();
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
      // Just in case they try to register or deregister observers
      // in response to the update ...
      final Collection<ModelObserver> copy =
        new ArrayList<ModelObserver>(mObservers);
      for (final ModelObserver observer : copy) {
        observer.modelChanged(event);
      }
    }
    if (mParent != null) {
      mParent.fireModelChanged(event);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.SimpleSetSubject
  public void assignFrom(final Set<? extends E> set)
  {
    final int oldsize = size();
    final int newsize = set.size();
    final Collection<E> added = new ArrayList<E>(newsize);
    final Collection<E> kept = new ArrayList<E>(oldsize);
    for (final E newitem : set) {
      if (contains(newitem)) {
        kept.add(newitem);
      } else {
        added.add(newitem);
      }
    }
    final boolean removing = mSet.retainAll(kept);
    final boolean adding = mSet.addAll(added);
    if (removing || adding) {
      fireGeometryChange();
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
  private void fireGeometryChange()
  {
    final Subject source = mParent != null ? mParent.getParent() : null;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(source,
                                             (GeometrySubject) mParent);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Inner Class GeometrySetIterator
  private class GeometrySetIterator implements Iterator<E>
  {

    //#######################################################################
    //# Constructors
    private GeometrySetIterator()
    {
      mIterator = mSet.iterator();
    }


    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mIterator.hasNext();
    }

    public E next()
    {
      return mIterator.next();
    }

    public void remove()
    {
      mIterator.remove();
      fireGeometryChange();
    }


    //#######################################################################
    //# Data Members
    private final Iterator<E> mIterator;

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
   * The contents of this set.
   */
  private Set<E> mSet;

}
