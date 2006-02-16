//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ArrayListSubject
//###########################################################################
//# $Id: ArrayListSubject.java,v 1.3 2006-02-16 04:06:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
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
  public boolean equalsWithGeometry(final Object partner)
  {
    if (!(partner instanceof List<?>)) {
      return false;
    }
    final List<?> list = (List<?>) partner;
    if (size() != list.size()) {
      return false;
    }
    final Iterator<P> iter1 = iterator();
    final Iterator<?> iter2 = list.iterator();
    while (iter1.hasNext()) {
      final P elem1 = iter1.next();
      final Object elem2 = iter2.next();
      if (!elem1.equalsWithGeometry(elem2)) {
        return false;
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
    afterAdd(proxy);
  }

  public P get(final int index)
  {
    return mProxyList.get(index);
  }

  public P remove(final int index)
  {
    final P proxy = mProxyList.remove(index);
    afterRemove(proxy);
    return proxy;
  }

  public P set(final int index, final P proxy)
  {
    final P old = get(index);
    if (old != proxy) {
      beforeAdd(proxy);
      mProxyList.set(index, proxy);
      afterRemove(old);
      afterAdd(proxy);
    }
    return old;
  }

  public int size()
  {
    return mProxyList.size();
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
  private void beforeAdd(final P proxy)
  {
    proxy.setParent(this);
  }

  private void afterAdd(final P proxy)
  {
    final ModelChangeEvent event =
      ModelChangeEvent.createItemAdded(this, proxy);
    fireModelChanged(event);
  }

  private void afterRemove(final P proxy)
  {
    proxy.setParent(null);
    final ModelChangeEvent event =
      ModelChangeEvent.createItemRemoved(this, proxy);
    fireModelChanged(event);
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
