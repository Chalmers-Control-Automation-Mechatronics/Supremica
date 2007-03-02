//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   CloningGeometryListSubject
//###########################################################################
//# $Id: CloningGeometryListSubject.java,v 1.5 2007-03-02 05:21:14 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.unchecked.Casting;


/**
 * <P>A wrapper of the {@link ArrayList} class that also implements the
 * {@link Subject} interface.</P>
 *
 * <P>This is an implementation of a mutable list with full event
 * notification support. In contrast to the {@link ArrayListSubject}
 * implementation, this class is intended for geometry information, and
 * therefore fires geometry change events when a change occurs. 
 * Furthermore, it always clones any elements inserted or returned
 * from the list, effectively ensuring immutable semantics for mutable
 * list elements.</P>
 *
 * @author Robi Malik
 */

public class CloningGeometryListSubject<E extends Cloneable>
  extends AbstractList<E>
  implements SimpleListSubject<E>, Cloneable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty array list.
   */
  public CloningGeometryListSubject()
  {
    this(0);
  }

  /**
   * Creates an empty array list.
   * @param  size        The initial size of the array.
   */
  public CloningGeometryListSubject(final int size)
  {
    mList = new ArrayList<E>(size);
  }

  /**
   * Creates an array list.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new list.
   */
  public CloningGeometryListSubject(final Collection<? extends E> input)
  {
    this(input.size());
    for (final E element : input) {
      final E cloned = cloneElement(element);
      mList.add(cloned);
    }
  }


  //#########################################################################
  //# Cloning
  public CloningGeometryListSubject<E> clone()
  {
    try {
      final Class<CloningGeometryListSubject<E>> clazz =
        Casting.toClass(getClass());
      final CloningGeometryListSubject<E> cloned = clazz.cast(super.clone());
      cloned.mParent = null;
      cloned.mObservers = null;
      cloned.mList = new ArrayList<E>(mList);
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface java.util.List
  public void add(final int index, final E element)
  {
    final E cloned = cloneElement(element);
    mList.add(index, cloned);
    fireGeometryChange();
  }

  public E get(final int index)
  {
    final E element = mList.get(index);
    return cloneElement(element);
  }

  public E remove(final int index)
  {
    final E element = mList.remove(index);
    fireGeometryChange();
    return element;
  }

  public E set(final int index, final E element)
  {
    final E old = get(index);
    if (!old.equals(element)) {
      final E cloned = cloneElement(element);
      mList.set(index, cloned);
      fireGeometryChange();
    }
    return old;
  }

  public int size()
  {
    return mList.size();
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
  //# Interface net.sourceforge.waters.subject.base.SimpleListSubject
  public void assignFrom(final List<? extends E> list)
  {
    final int oldsize = size();
    final int newsize = list.size();
    final boolean[] used = new boolean[oldsize];
    final List<E> newlist = new ArrayList<E>(newsize);
    int usecount = 0;
    boolean change = false;
    int i;
    for (i = 0; i < newsize; i++) {
      newlist.add(null);
    }
    for (i = 0; i < oldsize; i++) {
      used[i] = false;
    }
    i = 0;
    final Iterator<? extends E> iter = list.iterator();
    for (final E newitem : list) {
      if (iter.hasNext()) {
        final E olditem = iter.next();
        if (newitem.equals(olditem)) {
          newlist.set(i, olditem);
          used[i] = true;
          usecount++;
        }
        i++;
      } else {
        break;
      }
    }
    i = 0;
    for (final E newitem : list) {
      if (newlist.get(i) == null) {
        int j = 0;
        for (final E olditem : this) {
          if (!used[j] && newitem.equals(olditem)) {
            newlist.set(i, olditem);
            used[j] = true;
            usecount++;
            change = true;
            break;
          }
          j++;
        }
        if (j == oldsize) {
          // not found --- must clone and add.
          final E item = cloneElement(newitem);
          newlist.set(i, item);
          change = true;
        }
      }
      i++;
    }
    mList = newlist;
    if (change || usecount < oldsize) {
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
  private E cloneElement(final E element)
  {
    try {
      final Class<E> clazz = Casting.toClass(element.getClass());
      final Method method = getCloneMethod(clazz);
      final Object cloned = method.invoke(element);
      return clazz.cast(cloned);    
    } catch (final IllegalAccessException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final InvocationTargetException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final NoSuchMethodException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  private Method getCloneMethod(final Class<E> clazz)
    throws NoSuchMethodException
  {
    if (mLastClazz != clazz) {
      mLastClazz = clazz;
      mLastCloneMethod = clazz.getMethod("clone");
    }
    return mLastCloneMethod;
  }

  private void fireGeometryChange()
  {
    final Subject source = mParent != null ? mParent.getParent() : null;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(source,
                                             (GeometrySubject) mParent);
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
  private List<E> mList;

  private Class<E> mLastClazz;
  private Method mLastCloneMethod;

}
