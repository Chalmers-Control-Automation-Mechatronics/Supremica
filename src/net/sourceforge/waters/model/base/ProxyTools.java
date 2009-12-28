//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyTools
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import net.sourceforge.waters.model.unchecked.Casting;


/**
 * A utility class to compare collections and lists of proxies.
 *
 * @author Robi Malik
 */

public class ProxyTools
{

  //#########################################################################
  //# Object Equality respecting NULL
  /**
   * Checks whether two objects are equal, handling <CODE>null</CODE>
   * references properly. The {@link java.lang.Object Object} equality
   * method {@link Object#equals(Object) equals()} is used for comparison.
   * @return <CODE>true</CODE> if the two arguments are equal,
   *         or if both are <CODE>null</CODE>.
   */
  public static boolean equals(final Object object1, final Object object2)
  {
    if (object1 == null) {
      return object2 == null;
    } else {
      return object1.equals(object2);
    }
  }

  /**
   * Computes the hashcode of an object, handling <CODE>null</CODE>
   * references properly. The {@link java.lang.Object Object} hash
   * method {@link Object#hashCode()} is used for hashcode computation.
   */
  public static int hashCode(final Object object)
  {
    if (object == null) {
      return HASH_NULL;
    } else {
      return object.hashCode();
    }
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


  //#########################################################################
  //# Class Names
  public static String getShortProxyInterfaceName(final Proxy proxy)
  {
    final Class<? extends Proxy> iface = proxy.getProxyInterface();
    return getShortClassName(iface);
  }

  public static String getShortClassName(final Object item)
  {
    final Class<?> clazz = item.getClass();
    return getShortClassName(clazz);
  }

  public static String getShortClassName(final Class<?> clazz)
  {
    final String name = clazz.getName();
    final int dotpos = name.lastIndexOf('.');
    if (dotpos < 0) {
      return name;
    } else {
      return name.substring(dotpos + 1);
    }
  }


  //#########################################################################
  //# Class Constants
  /**
   * A cryptic constant returned as hash code for <CODE>null</CODE>
   * references.
   */
  private static final int HASH_NULL = 0xabababab;

}
