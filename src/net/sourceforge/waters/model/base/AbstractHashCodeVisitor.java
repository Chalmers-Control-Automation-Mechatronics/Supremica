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

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A visitor to compute hash code for {@link Proxy} objects based on their
 * contents. It can be parameterised to respect or not to respect the geometry
 * information for in some {@link Proxy} objects.
 *
 * @see AbstractEqualityVisitor
 * @author Robi Malik
 */

public abstract class AbstractHashCodeVisitor
  implements ProxyVisitor
{

  //#########################################################################
  //# Invocation
  public AbstractHashCodeVisitor()
  {
    this(false);
  }

  public AbstractHashCodeVisitor(final boolean geo)
  {
    mIsRespectingGeometry = geo;
  }


  //#########################################################################
  //# Simple Access
  public boolean isRespectingGeometry()
  {
    return mIsRespectingGeometry;
  }


  //#########################################################################
  //# Invocation
  public int hashCode(final Proxy proxy)
  {
    try {
      return computeProxyHashCode(proxy);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }

  public <P extends Proxy> int getArrayHashCode(final P[] array)
  {
    try {
      return computeArrayHashCode(array);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }

  public int getListHashCode(final List<? extends Proxy> list)
  {
    try {
      return computeListHashCode(list);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.base.ProxyVisitor
  @Override
  public Integer visitProxy(final Proxy proxy)
  {
    final Class<? extends Proxy> iface = proxy.getProxyInterface();
    return iface.hashCode();
  }

  @Override
  public Integer visitGeometryProxy(final GeometryProxy proxy)
  {
    return visitProxy(proxy);
  }

  @Override
  public Integer visitNamedProxy(final NamedProxy proxy)
  {
    int result = visitProxy(proxy);
    final String name = proxy.getName();
    result *= 5;
    result += name.hashCode();
    return result;
  }

  @Override
  public Integer visitDocumentProxy(final DocumentProxy proxy)
  {
    int result = visitNamedProxy(proxy);
    final String comment = proxy.getComment();
    result *= 5;
    result += computeOptionalHashCode(comment);
    return result;
  }


  //#########################################################################
  //# Auxiliary Methods
  protected int computeOptionalHashCode(final Object item)
  {
    if (item == null) {
      return HASH_NULL;
    } else {
      return item.hashCode();
    }
  }

  protected int computeProxyHashCode(final Proxy proxy)
    throws VisitorException
  {
    if (proxy == null) {
      return HASH_NULL;
    } else {
      return (Integer) proxy.acceptVisitor(this);
    }
  }

  protected int computeRefHashCode(final NamedProxy proxy)
    throws VisitorException
  {
    if (proxy == null) {
      return HASH_NULL;
    } else {
      return proxy.refHashCode();
    }
  }

  protected <P extends Proxy> int computeArrayHashCode(final P[] array)
    throws VisitorException
  {
    if (array == null) {
      return HASH_NULL;
    } else {
      int result = 0;
      for (final Proxy proxy : array) {
        result *= 5;
        if (proxy == null) {
          result += 0xabababab;
        } else {
          result += (Integer) proxy.acceptVisitor(this);
        }
      }
      return result;
    }
  }

  protected int computeCollectionHashCode(final Collection<? extends Proxy> set)
    throws VisitorException
  {
    int result = 0;
    for (final Proxy proxy : set) {
      result += (Integer) proxy.acceptVisitor(this);
    }
    return result;
  }

  protected int computeListHashCode(final List<? extends Proxy> list)
  throws VisitorException
  {
    int result = 0;
    for (final Proxy proxy : list) {
      result *= 5;
      if (proxy == null) {
        result += 0xabababab;
      } else {
        result += (Integer) proxy.acceptVisitor(this);
      }
    }
    return result;
  }

  protected int computeRefCollectionHashCode
      (final Collection<? extends NamedProxy> coll)
    throws VisitorException
  {
    int result = 0;
    for (final NamedProxy proxy : coll) {
      result += proxy.refHashCode();
    }
    return result;
  }

  protected int computeRefSetHashCode
    (final Collection<? extends NamedProxy> coll)
    throws VisitorException
  {
    if (coll instanceof Set<?>) {
      return computeRefCollectionHashCode(coll);
    } else {
      final int size = coll.size();
      final Set<String> names = new HashSet<String>(size);
      int result = 0;
      for (final NamedProxy proxy : coll) {
        final String name = proxy.getName();
        if (names.add(name)) {
          result += proxy.refHashCode();
        }
      }
      return result;
    }
  }

  protected int computeRefListHashCode(final List<? extends NamedProxy> list)
    throws VisitorException
  {
    int result = 0;
    for (final NamedProxy proxy : list) {
      result *= 5;
      result += proxy.refHashCode();
    }
    return result;
  }

  protected int computeRefMapHashCode
      (final Map<? extends NamedProxy,? extends NamedProxy> map)
    throws VisitorException
  {
    int result = 0;
    for (final Map.Entry<? extends NamedProxy,? extends NamedProxy> entry :
         map.entrySet()) {
      final NamedProxy key = entry.getKey();
      final NamedProxy value = entry.getValue();
      result += key.refHashCode() + 5 * value.refHashCode();
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final boolean mIsRespectingGeometry;


  //#########################################################################
  //# Class Constants
  /**
   * A cryptic constant returned as hash code for <CODE>null</CODE>
   * references.
   */
  private static final int HASH_NULL = 0xabababab;

}
