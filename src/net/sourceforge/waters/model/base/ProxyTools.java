//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.model.base;


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
      @SuppressWarnings("unchecked")
      final Class<P> clazz = (Class<P>) proxy.getClass();
      return clazz.cast(proxy.clone());
    }
  }


  //#########################################################################
  //# Class Names
  public static void appendContainerName(final Proxy container,
                                         final StringBuilder buffer)
  {
    final String clsname = getShortProxyInterfaceName(container);
    buffer.append(clsname);
    if (container instanceof NamedProxy) {
      final NamedProxy named = (NamedProxy) container;
      buffer.append(" '");
      buffer.append(named.getName());
      buffer.append('\'');
    }
  }

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
    int splitPos = name.lastIndexOf('$');
    if (splitPos < 0) {
      splitPos = name.lastIndexOf('.');
    }
    if (splitPos < 0) {
      return name;
    } else {
      return name.substring(splitPos + 1);
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
