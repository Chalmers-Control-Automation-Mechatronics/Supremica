//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.element.base
//# CLASS:   CloningGeometryListElement
//###########################################################################
//# $Id: CloningGeometryListElement.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.unchecked.Casting;


/**
 * <P>A wrapper of the {@link ArrayList} class that ensures perfect
 * immutability by cloning its elements. This class is intended for
 * geometry information (e.g., for {@link java.awt.geom.Point2D Point2D
 * Point2D} objects), which can be modified by accessing their data
 * members. To ensure the immutable character of the model implementation,
 * this list class clones eny objects before inserting them into the list,
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
      final Class<CloningGeometryListElement<E>> clazz =
        Casting.toClass(getClass());
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


  //#########################################################################
  //# Data Members
  /**
   * The contents of this list.
   */
  private List<E> mList;

  private Class<E> mLastClazz;
  private Method mLastCloneMethod;

}
