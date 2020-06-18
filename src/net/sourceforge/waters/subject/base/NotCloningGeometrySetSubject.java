//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import net.sourceforge.waters.model.base.WatersRuntimeException;

import gnu.trove.set.hash.THashSet;


/**
 * <P>A wrapper of the {@link THashSet} class that also implements the
 * {@link Subject} interface.</P>
 *
 * <P>This is an implementation of a mutable set with full event
 * notification support. In contrast to other collection subject
 * implementations, this class is intended for geometry information, and
 * therefore fires geometry change events when a change occurs. This
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
  @Override
  public NotCloningGeometrySetSubject<E> clone()
  {
    try {
      @SuppressWarnings("unchecked")
      final Class<NotCloningGeometrySetSubject<E>> clazz =
        (Class<NotCloningGeometrySetSubject<E>>) getClass();
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
  @Override
  public boolean add(final E element)
  {
    final boolean result = mSet.add(element);
    if (result) {
      final ModelChangeEvent event = createGeometryChange();
      event.fire();
    }
    return result;
  }

  @Override
  public boolean contains(final Object object)
  {
    return mSet.contains(object);
  }

  @Override
  public Iterator<E> iterator()
  {
    return new GeometrySetIterator();
  }

  @Override
  public boolean remove(final Object victim)
  {
    final boolean result = mSet.remove(victim);
    if (result) {
      final ModelChangeEvent event = createGeometryChange();
      event.fire();
    }
    return result;
  }

  @Override
  public int size()
  {
    return mSet.size();
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
      final StringBuilder buffer = new StringBuilder();
      buffer.append("Trying to redefine parent of ");
      buffer.append(getShortClassName());
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
  //# Interface net.sourceforge.waters.subject.base.SimpleSetSubject
  @Override
  public UndoInfo createUndoInfo(final Set<? extends E> newSet,
                                 final Set<? extends Subject> boundary)
  {
    if (boundary != null && boundary.contains(this)) {
      return null;
    }
    final RecursiveUndoInfo info = new RecursiveUndoInfo(this);
    for (final E oldItem : this) {
      if (!newSet.contains(oldItem)) {
        final UndoInfo remove = new ReplacementUndoInfo(oldItem, null);
        info.add(remove);
      }
    }
    for (final E newItem : newSet) {
      if (!contains(newItem)) {
        final UndoInfo add = new ReplacementUndoInfo(null, newItem);
        info.add(add);
      }
    }
    if (info.isEmpty()) {
      return null;
    } else {
      return info;
    }
  }

  @Override
  public ModelChangeEvent assignMember(final int index,
                                       final Object oldValue,
                                       final Object newValue)
  {
    if (oldValue != null) {
      mSet.remove(oldValue);
    }
    if (newValue != null) {
      @SuppressWarnings("unchecked")
      final Class<E> clazz = (Class<E>) newValue.getClass();
      mSet.add(clazz.cast(newValue));
    }
    return createGeometryChange();
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
  private ModelChangeEvent createGeometryChange()
  {
    final Subject source = mParent != null ? mParent.getParent() : this;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(source,
                                             mParent);
    return event;
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
    @Override
    public boolean hasNext()
    {
      return mIterator.hasNext();
    }

    @Override
    public E next()
    {
      return mIterator.next();
    }

    @Override
    public void remove()
    {
      mIterator.remove();
      final ModelChangeEvent event = createGeometryChange();
      event.fire();
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
