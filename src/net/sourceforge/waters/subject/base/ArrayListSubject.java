//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;


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
  @SuppressWarnings("unchecked")
  public ArrayListSubject<P> clone()
  {
    try {
      final Class<ArrayListSubject<P>> clazz =
        (Class<ArrayListSubject<P>>) getClass();
      final ArrayListSubject<P> cloned = clazz.cast(super.clone());
      cloned.mParent = null;
      cloned.mObservers = null;
      cloned.mProxyList = new ArrayList<P>(size());
      for (final P elem : this) {
        final Class<P> elemclazz = (Class<P>) elem.getClass();
        final P clonedelem = elemclazz.cast(elem.clone());
        cloned.add(clonedelem);
      }
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
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
  public UndoInfo createUndoInfo(final List<? extends P> newList,
                                 final Set<? extends Subject> boundary)
  {
    if (boundary != null && boundary.contains(this)) {
      return null;
    }
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true);
    if (eq.isEqualList(this, newList)) {
      return null;
    }

    // Longest Common Subsequence Algorithm
    // http://en.wikipedia.org/wiki/Longest_common_subsequence_problem
    final int len1 = size();
    final int len2 = newList.size();
    final int[][] lcsLength = new int[len1 + 1][len2 + 1];
    final ListIterator<? extends P> iter1 = listIterator();
    int i1 = 0;
    ListIterator<? extends P> iter2 = null;
    int i2 = 0;
    if (len1 == 0) {
      i2 = len2;
      iter2 = newList.listIterator(len2);
    } else {
      while (iter1.hasNext()) {
        final P proxy1 = iter1.next();
        i1++;
        i2 = 0;
        iter2 = newList.listIterator();
        while (iter2.hasNext()) {
          final P proxy2 = iter2.next();
          i2++;
          if (eq.equals(proxy1, proxy2)) {
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
        final P proxy1 = iter1.previous();
        final P proxy2 = iter2.previous();
        final P cloned = ProxyTools.clone(proxy2);
        final UndoInfo replace = new ReplacementUndoInfo(i1, proxy1, cloned);
        info.add(replace);
      } else if (i2 > 0 && (i1 == 0 || lcsLength[i1][i2 - 1] == len)) {
        i2--;
        final P proxy2 = iter2.previous();
        final P cloned = ProxyTools.clone(proxy2);
        final UndoInfo insert = new ReplacementUndoInfo(i2, null, cloned);
        insertions.add(insert);
      } else if (i1 > 0 && (i2 == 0 || lcsLength[i1 - 1][i2] == len)) {
        i1--;
        final P proxy1 = iter1.previous();
        final UndoInfo remove = new ReplacementUndoInfo(i1, proxy1, null);
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

  public ModelChangeEvent assignMember(final int index,
                                       final Object oldValue,
                                       final Object newValue)
  {
    if (oldValue == null) {
      @SuppressWarnings("unchecked")
      final Class<P> clazz = (Class<P>) newValue.getClass();
      add(index, clazz.cast(newValue));
    } else if (newValue == null) {
      remove(index);
    } else {
      @SuppressWarnings("unchecked")
      final Class<P> clazz = (Class<P>) newValue.getClass();
      set(index, clazz.cast(newValue));
    }
    return null;
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
  private void beforeAdd(final P proxy)
  {
    proxy.setParent(this);
  }

  private void afterAdd(final P proxy, final int index)
  {
    final ModelChangeEvent event =
      ModelChangeEvent.createItemAdded(this, proxy, index);
    event.fire();
  }

  private void afterRemove(final P proxy, final int index)
  {
    proxy.setParent(null);
    final ModelChangeEvent event =
      ModelChangeEvent.createItemRemoved(this, proxy, index);
    event.fire();
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
