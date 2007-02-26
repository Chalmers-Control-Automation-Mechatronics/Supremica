//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyTools
//###########################################################################
//# $Id: ProxyTools.java,v 1.1 2007-02-26 21:41:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.unchecked.Casting;


/**
 * A utility class to compare collections and lists of proxies. The
 * methods provided by this class support the use of the content-based
 * equality methods {@link Proxy#equalsByContents(Proxy)
 * equalsByContents()} and {@link Proxy#equalsWithGeometry(Proxy)
 * equalsWithGeometry()} provided by the {@link Proxy} interface, as well
 * as the corresponding methods for hash code computation.
 * 
 * @author Robi Malik
 */

public class ProxyTools
{

  //#########################################################################
  //# Equality by Contents
  /**
   * Checks whether two collections have the same contents. This method
   * compares two collections of proxies, and checks whether they have
   * elements with the same contents. The equality method {@link
   * Proxy#equalsByContents(Proxy) equalsByContents()} provided by the
   * {@link Proxy} interface is used for comparison of individual elements.
   */
  public static boolean isEqualCollectionByContents
    (final Collection<? extends Proxy> coll1,
     final Collection<? extends Proxy> coll2)
  {
    final ProxyAccessorCollection<Proxy> map1 =
      new ProxyAccessorHashCollectionByContents<Proxy>(coll1);
    final ProxyAccessorCollection<Proxy> map2 =
      new ProxyAccessorHashCollectionByContents<Proxy>(coll2);
    return map1.equalsByAccessorEquality(map2);
  }

  /**
   * Checks whether two sets have the same contents. This method compares
   * two sets of proxies, and checks whether they have elements with the
   * same contents. The equality method {@link
   * Proxy#equalsByContents(Proxy) equalsByContents()} provided by the
   * {@link Proxy} interface is used for comparison of individual
   * elements.
   * This method can compare sets or collections, duplicates are not
   * considered significant in either case.
   */
  public static boolean isEqualSetByContents
    (final Collection<? extends Proxy> set1,
     final Collection<? extends Proxy> set2)
  {
    // Can't rely on set size as sets my have distinct elements that
    // are equal under content-based equality.
    final ProxyAccessorMap<Proxy> map1 =
      new ProxyAccessorHashMapByContents<Proxy>(set1);
    final ProxyAccessorMap<Proxy> map2 =
      new ProxyAccessorHashMapByContents<Proxy>(set2);
    return map1.equalsByAccessorEquality(map2);
  }

  /**
   * Checks whether two lists have the same contents. This method compares
   * two lists of proxies, and checks whether they have elements with the
   * same contents appearing in the same order. The equality method {@link
   * Proxy#equalsByContents(Proxy) equalsByContents()} provided by the
   * {@link Proxy} interface is used for comparison of individual
   * elements.
   */
  public static boolean isEqualListByContents
    (final List<? extends Proxy> list1,
     final List<? extends Proxy> list2)
  {
    if (list1.size() == list2.size()) {
      final Iterator<? extends Proxy> iter1 = list1.iterator();
      final Iterator<? extends Proxy> iter2 = list2.iterator();
      while (iter1.hasNext()) {
        final Proxy proxy1 = iter1.next();
        final Proxy proxy2 = iter2.next();
        if (!proxy1.equalsByContents(proxy2)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Equality with Geometry
  /**
   * Checks whether two collections have the same contents and
   * geometry. This method compares two collections of proxies, and checks
   * whether they have elements with the same contents. The equality method
   * {@link Proxy#equalsWithGeometry(Proxy) equalsWithGeometry()} provided
   * by the {@link Proxy} interface is used for comparison of individual
   * elements.
   */
  public static boolean isEqualCollectionWithGeometry
    (final Collection<? extends Proxy> coll1,
     final Collection<? extends Proxy> coll2)
  {
    final ProxyAccessorCollection<Proxy> map1 =
      new ProxyAccessorHashCollectionWithGeometry<Proxy>(coll1);
    final ProxyAccessorCollection<Proxy> map2 =
      new ProxyAccessorHashCollectionWithGeometry<Proxy>(coll2);
    return map1.equalsByAccessorEquality(map2);
  }

  /**
   * Checks whether two sets have the same contents and geometry. This
   * method compares two sets of proxies, and checks whether they have
   * elements with the same contents. The equality method {@link
   * Proxy#equalsWithGeometry(Proxy) equalsWithGeometry()} provided by the
   * {@link Proxy} interface is used for comparison of individual
   * elements.
   * This method can compare sets or collections, duplicates are not
   * considered significant in either case.
   */
  public static boolean isEqualSetWithGeometry
    (final Collection<? extends Proxy> set1,
     final Collection<? extends Proxy> set2)
  {
    // Can't rely on set size as sets my have distinct elements that
    // are equal under content-based equality.
    final ProxyAccessorMap<Proxy> map1 =
      new ProxyAccessorHashMapWithGeometry<Proxy>(set1);
    final ProxyAccessorMap<Proxy> map2 =
      new ProxyAccessorHashMapWithGeometry<Proxy>(set2);
    return map1.equalsByAccessorEquality(map2);
  }

  /**
   * Checks whether two lists have the same contents and geometry. This
   * method compares two lists of proxies, and checks whether they have
   * elements with the same contents appearing in the same order. The
   * equality method {@link Proxy#equalsWithGeometry(Proxy)
   * equalsWithGeometry()} provided by the {@link Proxy} interface is
   * used for comparison of individual elements.
   */
  public static boolean isEqualListWithGeometry
    (final List<? extends Proxy> list1,
     final List<? extends Proxy> list2)
  {
    if (list1.size() == list2.size()) {
      final Iterator<? extends Proxy> iter1 = list1.iterator();
      final Iterator<? extends Proxy> iter2 = list2.iterator();
      while (iter1.hasNext()) {
        final Proxy proxy1 = iter1.next();
        final Proxy proxy2 = iter2.next();
        if (!proxy1.equalsWithGeometry(proxy2)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Hash Code by Contents
  /**
   * Calculates a hash code for a collection of proxies based on the
   * contents of the collection. The hash codes of all elements of the
   * collection are considered, as obtained through the
   * Proxy#hashCodeByContents() hashCodeByContents()} provided by the
   * {@link Proxy} interface.
   */
  public static int getCollectionHashCodeByContents
    (final Collection<? extends Proxy> coll)
  {
    int result = 0;
    for (final Proxy proxy : coll) {
      result += proxy.hashCodeByContents();
    }
    return result;
  }

  /**
   * Calculates a hash code for a set of proxies based on the contents of
   * the set. The hash codes of all elements of the set are considered, as
   * obtained through the Proxy#hashCodeByContents() hashCodeByContents()}
   * provided by the {@link Proxy} interface.
   * This method can be applied to a set or collection, duplicates are not
   * considered significant in either case.
   */
  public static int getSetHashCodeByContents
    (final Collection<? extends Proxy> set)
  {
    final ProxyAccessorMap<Proxy> map =
      new ProxyAccessorHashMapByContents<Proxy>(set);
    return map.hashCodeByAccessorEquality();
  }


  /**
   * Calculates a hash code for a list of proxies based on the contents of
   * the list. The hash codes of all elements of the list are considered,
   * as obtained through the Proxy#hashCodeByContents()
   * hashCodeByContents()} provided by the {@link Proxy} interface.
   */
  public static int getListHashCodeByContents(final List<? extends Proxy> list)
  {
    int result = 0;
    for (final Proxy proxy : list) {
      result *= 5;
      result += proxy.hashCodeByContents();
    }
    return result;
  }


  //#########################################################################
  //# Hash Code with Geometry
  /**
   * Calculates a hash code for a collection of proxies based on the
   * contents and geometry information of the collection's elements. The
   * hash codes of all elements of the collection are considered, as
   * obtained through the Proxy#hashCodeWithGeometry()
   * hashCodeWithGeometry()} provided by the {@link Proxy} interface.
   */
  public static int getCollectionHashCodeWithGeometry
    (final Collection<? extends Proxy> coll)
  {
    int result = 0;
    for (final Proxy proxy : coll) {
      result += proxy.hashCodeWithGeometry();
    }
    return result;
  }

  /**
   * Calculates a hash code for a set of proxies based on the contents and
   * geometry information of the set's elements. The hash codes of all
   * elements of the set are considered, as obtained through the
   * Proxy#hashCodeWithGeometry() hashCodeWithGeometry()} provided by the
   * {@link Proxy} interface.
   * This method can be applied to a set or collection, duplicates are not
   * considered significant in either case.
   */
  public static int getSetHashCodeWithGeometry
    (final Collection<? extends Proxy> set)
  {
    final ProxyAccessorMap<Proxy> map =
      new ProxyAccessorHashMapWithGeometry<Proxy>(set);
    return map.hashCodeByAccessorEquality();
  }

  /**
   * Calculates a hash code for a list of proxies based on the contents and
   * geometry information of the list's elements. The hash codes of all
   * elements of the list are considered, as obtained through the
   * Proxy#hashCodeWithGeometry() hashCodeWithGeometry()} provided by the
   * {@link Proxy} interface.
   */
  public static int getListHashCodeWithGeometry
    (final List<? extends Proxy> list)
  {
    int result = 0;
    for (final Proxy proxy : list) {
      result *= 5;
      result += proxy.hashCodeWithGeometry();
    }
    return result;
  }


  //#########################################################################
  //# Cloning
  /**
   * Clones a proxy and casts it to the proper type.
   * @return A clone of the given proxy, or <CODE>null</CODE>
   *         if the argument is <CODE>null</CODE>.
   */
  public static <P extends Proxy> P clone(final P proxy)
  {
    if (proxy == null) {
      return null;
    } else {
      final Class<P> clazz = Casting.toClass(proxy.getClass());
      return clazz.cast(proxy.clone());
    }
  }

}
