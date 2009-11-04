//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ArrayListSubject
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.unchecked.Casting;


/**
 * <P>A wrapper of the {@link ArrayList} class that also implements the
 * {@link Subject} interface.</P>
 *
 * <P>This is an implementation of a mutable list with full event
 * notification support.</P>
 *
 * @author Robi Malik
 */

public class ArrayListSubject<P extends ProxySubject>
  extends AbstractList<P>
  implements ListSubject<P>, Cloneable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty array list.
   */
  public ArrayListSubject()
  {
    this(0);
  }

  /**
   * Creates an empty array list.
   * @param  size        The initial size of the array.
   */
  public ArrayListSubject(final int size)
  {
    mProxyList = new ArrayList<P>(size);
  }

  /**
   * Creates an array list.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new list.
   */
  public ArrayListSubject(final Collection<? extends P> input)
  {
    mProxyList = new ArrayList<P>(input);
  }

  /**
   * Creates an array list.
   * This method creates a list of {@link Subject} objects from a collection
   * of {@link Proxy} by dynamically checking the type of each object.
   * The constructor fails if any of the provided objects is not of the
   * appropriate type.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new list.
   * @param  clazz       The class of the objects to be added to the list.
   * @throws ClassCastException to indicate that some of the elements of the
   *                     input does not match the type of this list.
   */
  public ArrayListSubject(final Collection<? extends Proxy> input,
                          final Class<? extends P> clazz)
  {
    this(input.size());
    for (final Proxy proxy : input) {
      final P downcast = clazz.cast(proxy);
      add(downcast);
    }
  }


  //#########################################################################
  //# Cloning
  public ArrayListSubject<P> clone()
  {
    try {
      final Class<ArrayListSubject<P>> clazz =
        Casting.toClass(getClass());
      final ArrayListSubject<P> cloned = clazz.cast(super.clone());
      cloned.mParent = null;
      cloned.mObservers = null;
      cloned.mProxyList = new ArrayList<P>(size());
      for (final P elem : this) {
        final Class<P> elemclazz = Casting.toClass(elem.getClass());
        final P clonedelem = elemclazz.cast(elem.clone());
        cloned.add(clonedelem);
      }
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Equals and Hashcode
  /**
   * Checks whether this list has the same contents as another list.
   * To be considered equal, the two lists must have the same size,
   * and contain proxies that are considered as equal by their
   * {@link Proxy#equalsByContents(Proxy) equalsByContents()} methods,
   * in the proper order.
   */
  public boolean equalsByContents(final List<? extends Proxy> partner)
  {
    if (size() != partner.size()) {
      return false;
    }
    final Iterator<P> iter1 = iterator();
    final Iterator<? extends Proxy> iter2 = partner.iterator();
    while (iter1.hasNext()) {
      final P item1 = iter1.next();
      final Proxy item2 = iter2.next();
      if (item1 == null) {
        if (item2 != null) {
          return false;
        } 
      } else {
        if (!item1.equalsByContents(item2)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Checks whether this list has the same contents and geometry
   * information as another list. To be considered equal, the two lists
   * must have the same size, and contain proxies that are considered as
   * equal by their {@link Proxy#equalsWithGeometry(Proxy)
   * equalsWithGeometry()} methods, in the proper order.
   */
  public boolean equalsWithGeometry(final List<? extends Proxy> partner)
  {
    if (size() != partner.size()) {
      return false;
    }
    final Iterator<P> iter1 = iterator();
    final Iterator<? extends Proxy> iter2 = partner.iterator();
    while (iter1.hasNext()) {
      final P item1 = iter1.next();
      final Proxy item2 = iter2.next();
      if (item1 == null) {
        if (item2 != null) {
          return false;
        }
      } else {
        if (!item1.equalsWithGeometry(item2)) {
          return false;
        }
      }
    }
    return true;
  }


  //#########################################################################
  //# Interface java.util.List
  public void add(final int index, final P proxy)
  {
    beforeAdd(proxy);
    mProxyList.add(index, proxy);
    afterAdd(proxy, index);
  }

  public P get(final int index)
  {
    return mProxyList.get(index);
  }

  public P remove(final int index)
  {
    final P proxy = mProxyList.remove(index);
    afterRemove(proxy, index);
    return proxy;
  }

  public P set(final int index, final P proxy)
  {
    final P old = get(index);
    if (old != proxy) {
      beforeAdd(proxy);
      mProxyList.set(index, proxy);
      afterRemove(old, index);
      afterAdd(proxy, index);
    }
    return old;
  }

  public int size()
  {
    return mProxyList.size();
  }

           
  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ListSubject
  public void assignFrom(final List<? extends P> list)
  {
    final int oldsize = size();
    final int newsize = list.size();
    final boolean[] used = new boolean[oldsize];
    final Collection<P> added = new ArrayList<P>(newsize);
    final Collection<P> removed = new ArrayList<P>(oldsize);
    final Collection<P> moved = new ArrayList<P>(oldsize);
    final List<P> newlist = new ArrayList<P>(newsize);
    int i;
    for (i = 0; i < newsize; i++) {
      newlist.add(null);
    }
    for (i = 0; i < oldsize; i++) {
      used[i] = false;
    }
    i = 0;
    final Iterator<P> iter = iterator();
    for (final P newproxy : list) {
      if (iter.hasNext()) {
        final P oldproxy = iter.next();
        if (newproxy.equalsWithGeometry(oldproxy)) {
          newlist.set(i, oldproxy);
          used[i] = true;
        }
        i++;
      } else {
        break;
      }
    }
    i = 0;
    for (final P newproxy : list) {
      if (newlist.get(i) == null) {
        int j = 0;
        for (final P oldproxy : this) {
          if (!used[j] && newproxy.equalsWithGeometry(oldproxy)) {
            newlist.set(i, oldproxy);
            used[j] = true;
            moved.add(oldproxy);
            break;
          }
          j++;
        }
        if (j == oldsize) {
          // not found --- must clone and add.
          final P proxy = ProxyTools.clone(newproxy);
          newlist.set(i, proxy);
          added.add(proxy);
          beforeAdd(proxy);          
        }
      }
      i++;
    }
    i = 0;
    for (final P oldproxy : this) {
      if (!used[i++]) {
        removed.add(oldproxy);
      }
    }
    mProxyList = newlist;
    for (final P oldproxy : removed) {
      afterRemove(oldproxy, -1);
    }
    for (final P oldproxy : moved) {
      afterMove(oldproxy);
    }
    for (final P newproxy : added) {
      afterAdd(newproxy, -1);
    }
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
      final List<ModelObserver> copy =
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
  private void beforeAdd(final P proxy)
  {
    proxy.setParent(this);
  }

  private void afterAdd(final P proxy, final int index)
  {
    final ModelChangeEvent event =
      ModelChangeEvent.createItemAdded(this, proxy, index);
    fireModelChanged(event);
  }

  private void afterRemove(final P proxy, final int index)
  {
    proxy.setParent(null);
    final ModelChangeEvent event =
      ModelChangeEvent.createItemRemoved(this, proxy, index);
    fireModelChanged(event);
  }

  private void afterMove(final P proxy)
  {
    final ModelChangeEvent event1 =
      ModelChangeEvent.createItemRemoved(this, proxy);
    fireModelChanged(event1);
    final ModelChangeEvent event2 =
      ModelChangeEvent.createItemAdded(this, proxy);
    fireModelChanged(event2);
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
   * The contents of this list.
   */
  private List<P> mProxyList;

}
