//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.plain.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.WatersRuntimeException;


/**
 * <P>A wrapper of the {@link ArrayList} class that ensures perfect
 * immutability by cloning its elements. This class is intended for
 * geometry information (e.g., for {@link java.awt.geom.Point2D Point2D
 * Point2D} objects), which can be modified by accessing their data
 * members. To ensure the immutable character of the model implementation,
 * this list class clones any objects before inserting them into the list,
 * and only returns copies of its elements in its retrieve methods. This is
 * an immutable list, so modification methods throw {@link
 * UnsupportedOperationException}.
 *
 * @author Robi Malik
 */

public class CloningGeometryListElement<E extends Cloneable>
  extends AbstractList<E>
  implements Cloneable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty array list.
   */
  public CloningGeometryListElement()
  {
    this(0);
  }

  /**
   * Creates an empty array list.
   * @param  size        The initial size of the array.
   */
  public CloningGeometryListElement(final int size)
  {
    mList = new ArrayList<E>(size);
  }

  /**
   * Creates an array list.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new list.
   */
  public CloningGeometryListElement(final Collection<? extends E> input)
  {
    this(input.size());
    for (final E element : input) {
      final E cloned = cloneElement(element);
      mList.add(cloned);
    }
  }


  //#########################################################################
  //# Cloning
  public CloningGeometryListElement<E> clone()
  {
    try {
      @SuppressWarnings("unchecked")
      final Class<CloningGeometryListElement<E>> clazz =
        (Class<CloningGeometryListElement<E>>) getClass();
      final CloningGeometryListElement<E> cloned = clazz.cast(super.clone());
      cloned.mList = new ArrayList<E>(mList);
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface java.util.List
  public E get(final int index)
  {
    final E element = mList.get(index);
    return cloneElement(element);
  }

  public int size()
  {
    return mList.size();
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


  //#########################################################################
  //# Data Members
  /**
   * The contents of this list.
   */
  private List<E> mList;

  private Class<E> mLastClazz;
  private Method mLastCloneMethod;

}
