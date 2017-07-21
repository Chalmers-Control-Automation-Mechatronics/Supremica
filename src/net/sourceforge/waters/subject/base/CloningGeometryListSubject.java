//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.subject.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import net.sourceforge.waters.model.base.WatersRuntimeException;


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
      @SuppressWarnings("unchecked")
      final Class<CloningGeometryListSubject<E>> clazz =
        (Class<CloningGeometryListSubject<E>>) getClass();
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
    final ModelChangeEvent event = createGeometryChanged();
    event.fire();
  }

  public E get(final int index)
  {
    final E element = mList.get(index);
    return cloneElement(element);
  }

  public E remove(final int index)
  {
    final E element = mList.remove(index);
    final ModelChangeEvent event = createGeometryChanged();
    event.fire();
    return element;
  }

  public E set(final int index, final E element)
  {
    final E old = get(index);
    if (!old.equals(element)) {
      final E cloned = cloneElement(element);
      mList.set(index, cloned);
      final ModelChangeEvent event = createGeometryChanged();
      event.fire();
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
      final StringBuilder buffer = new StringBuilder();
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
  //# Interface net.sourceforge.waters.subject.base.SimpleListSubject
  public UndoInfo createUndoInfo(final List<? extends E> newList,
                                 final Set<? extends Subject> boundary)
  {
    if (boundary != null && boundary.contains(this) || equals(newList)) {
      return null;
    }

    // Longest Common Subsequence Algorithm
    // http://en.wikipedia.org/wiki/Longest_common_subsequence_problem
    final int len1 = size();
    final int len2 = newList.size();
    final int[][] lcsLength = new int[len1 + 1][len2 + 1];
    final ListIterator<? extends E> iter1 = listIterator();
    int i1 = 0;
    ListIterator<? extends E> iter2 = null;
    int i2 = 0;
    if (len1 == 0) {
      i2 = len2;
      iter2 = newList.listIterator(len2);
    } else {
      while (iter1.hasNext()) {
        final E item1 = iter1.next();
        i1++;
        i2 = 0;
        iter2 = newList.listIterator();
        while (iter2.hasNext()) {
          final E item2 = iter2.next();
          i2++;
          if (item1.equals(item2)) {
            lcsLength[i1][i2] = lcsLength[i1 - 1][i2 - 1] + 1;
          } else if (lcsLength[i1][i2 - 1] < lcsLength[i1 - 1][i2]) {
            lcsLength[i1][i2] = lcsLength[i1 - 1][i2];
          } else {
            lcsLength[i1][i2] = lcsLength[i1][i2 - 1];
          }
        }
      }
    }

    final RecursiveUndoInfo info = new RecursiveUndoInfo(this);
    int numInserts = newList.size() - lcsLength[len1][len2];
    final List<UndoInfo> insertions = new ArrayList<UndoInfo>(numInserts);
    while (i1 > 0 || i2 > 0) {
      final int len = lcsLength[i1][i2];
      if (i1 > 0 && i2 > 0 && lcsLength[i1 - 1][i2 - 1] == len) {
        i1--;
        i2--;
        final E item1 = iter1.previous();
        final E item2 = iter2.previous();
        final E cloned = cloneElement(item2);
        final UndoInfo replace = new ReplacementUndoInfo(i1, item1, cloned);
        info.add(replace);
      } else if (i2 > 0 && (i1 == 0 || lcsLength[i1][i2 - 1] == len)) {
        i2--;
        final E item2 = iter2.previous();
        final E cloned = cloneElement(item2);
        final UndoInfo insert = new ReplacementUndoInfo(i2, null, cloned);
        insertions.add(insert);
      } else if (i1 > 0 && (i2 == 0 || lcsLength[i1 - 1][i2] == len)) {
        i1--;
        final E item1 = iter1.previous();
        final UndoInfo remove = new ReplacementUndoInfo(i1, item1, null);
        info.add(remove);
      } else {
        i1--;
        i2--;
        iter1.previous();
        iter2.previous();
      }
    }

    numInserts = insertions.size();
    final ListIterator<UndoInfo> iter = insertions.listIterator(numInserts);
    while (iter.hasPrevious()) {
      final UndoInfo insert = iter.previous();
      info.add(insert);
    }
    return info;
  }

  public ModelChangeEvent assignMember(final int index,
                                       final Object oldValue,
                                       final Object newValue)
  {
    if (oldValue == null) {
      @SuppressWarnings("unchecked")
      final Class<E> clazz = (Class<E>) newValue.getClass();
      final E element = clazz.cast(newValue);
      final E cloned = cloneElement(element);
      mList.add(index, clazz.cast(cloned));
    } else if (newValue == null) {
      mList.remove(index);
    } else {
      @SuppressWarnings("unchecked")
      final Class<E> clazz = (Class<E>) newValue.getClass();
      final E element = clazz.cast(newValue);
      final E cloned = cloneElement(element);
      mList.set(index, clazz.cast(cloned));
    }
    return createGeometryChanged();
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
  private E cloneElement(final E element)
  {
    try {
      @SuppressWarnings("unchecked")
      final Class<E> clazz = (Class<E>) element.getClass();
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

  private ModelChangeEvent createGeometryChanged()
  {
    //TODO mParent.getParent() can be null ??
    Subject source = mParent != null ? mParent.getParent() : this;
    if(mParent != null && mParent.getParent() == null){
      source = this;
    }
    return ModelChangeEvent.createGeometryChanged(source,
                                                  (GeometrySubject) mParent);
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
