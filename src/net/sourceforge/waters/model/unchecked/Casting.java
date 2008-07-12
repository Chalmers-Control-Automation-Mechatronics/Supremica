//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   Casting
//###########################################################################
//# $Id: Casting.java,v 1.4 2007-02-26 21:41:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.unchecked;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A collection of unchecked casts.  This class provides a set of static
 * methods performing unchecked type casts. The rationale is to keep all
 * unavoidable unchecked casts in a separate package, so the rest of the
 * code can be compiled with unchecked cast warnings enabled.
 *
 * @author Robi Malik
 */

public class Casting {

  /**
   * Converts a untyped class to a typed class.
   */
  public static <T>
    Class<T> toClass(final Class clazz)
  {
    return (Class<T>) clazz;
  }

  /**
   * Converts a untyped iterator to a typed iterator.
   */
  public static <E>
    Iterator<E> toIterator(final Iterator iterator)
  {
    return (Iterator<E>) iterator;
  }

  /**
   * Converts a untyped collection to a typed collection.
   */
  public static <E>
    Collection<E> toCollection(final Collection collection)
  {
    return (Collection<E>) collection;
  }

  /**
   * Converts a untyped list to a typed list.
   */
  public static <E>
    List<E> toList(final List list)
  {
    return (List<E>) list;
  }

  /**
   * Converts a arbitrary object to a typed list.
   */
  public static <E>
    List<E> toList(final Object object)
  {
    return (List<E>) object;
  }

  /**
   * Converts a untyped set to a typed set.
   */
  public static <E>
    Set<E> toSet(final Set set)
  {
    return (Set<E>) set;
  }

  /**
   * Converts a arbitrary object to a typed set.
   */
  public static <E>
    Set<E> toSet(final Object object)
  {
    return (Set<E>) object;
  }

  /**
   * Converts a untyped map to a typed map.
   */
  public static <K,V>
    Map<K,V> toMap(final Map map)
  {
    return (Map<K,V>) map;
  }

  /**
   * Converts a arbitrary object to a typed map.
   */
  public static <K,V>
    Map<K,V> toMap(final Object object)
  {
    return (Map<K,V>) object;
  }

  /**
   * Creates a new array with the specified component type and length.
   * This is just a generic-typesafe wrapper of the
   * {@link Array#newInstance(Class,int) newInstance()} method of the
   * {@link Array} class.
   */
  public static <T>
    T[] newArray(final Class<T> clazz, final int size)
  {
    return (T[]) Array.newInstance(clazz, size);
  }

}
