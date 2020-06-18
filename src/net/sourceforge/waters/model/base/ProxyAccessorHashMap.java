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

package net.sourceforge.waters.model.base;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @author Robi Malik
 */

public class ProxyAccessorHashMap<P extends Proxy,V>
  extends AbstractMap<ProxyAccessor<P>,V>
  implements ProxyAccessorMap<P,V>
{

  //#########################################################################
  //# Constructors
  public ProxyAccessorHashMap(final AbstractEqualityVisitor eq)
  {
    mEquality = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
    mMap = new HashMap<ProxyAccessor<P>,V>();
  }

  public ProxyAccessorHashMap(final AbstractEqualityVisitor eq,
                              final int initialCapacity)
  {
    mEquality = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
    mMap = new HashMap<ProxyAccessor<P>,V>(initialCapacity);
  }

  public ProxyAccessorHashMap(final AbstractEqualityVisitor eq,
                              final int initialCapacity,
                              final float loadFactor)
  {
    mEquality = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
    mMap = new HashMap<ProxyAccessor<P>,V>(initialCapacity, loadFactor);
  }

  public ProxyAccessorHashMap
    (final AbstractEqualityVisitor eq,
     final Map<? extends ProxyAccessor<P>, ? extends V> map)
  {
    mEquality = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
    mMap = new HashMap<ProxyAccessor<P>,V>(map);
  }


  //#########################################################################
  //# Interface java.util.Map
  @Override
  public Set<Map.Entry<ProxyAccessor<P>,V>> entrySet()
  {
    return mMap.entrySet();
  }

  @Override
  public V put(final ProxyAccessor<P> key, final V value)
  {
    return mMap.put(key, value);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.ProxyAccessorMap
  @Override
  public boolean containsProxyKey(final P proxy)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    return containsKey(accessor);
  }

  public boolean containsAll(final Collection<? extends P> collection)
  {
    for (final P proxy : collection) {
      if (!containsProxyKey(proxy)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public <PP extends P> ProxyAccessor<P> createAccessor(final PP proxy)
  {
    return new Accessor<P>(proxy);
  }

  @Override
  public V getByProxy(final P proxy)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    return mMap.get(accessor);
  }

  @Override
  public V putByProxy(final P proxy, final V value)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    return mMap.put(accessor, value);
  }

  @Override
  public void putAllByProxies(final Map<? extends P,? extends V> map)
  {
    for (final Map.Entry<? extends P,? extends V> entry : map.entrySet()) {
      final P proxy = entry.getKey();
      final V value = entry.getValue();
      putByProxy(proxy, value);
    }
  }

  @Override
  public boolean removeProxy(final P proxy)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    return remove(accessor) != null;
  }

  @Override
  public boolean removeAllProxies(final Collection<? extends P> collection)
  {
    boolean result = false;
    for (final P proxy : collection) {
      if (removeProxy(proxy)) {
        result = true;
      }
    }
    return result;
  }


  //#########################################################################
  //# Inner Class Accessor
  private class Accessor<PP extends P> implements ProxyAccessor<PP>
  {

    //#######################################################################
    //# Constructors
    private Accessor(final PP proxy)
    {
      mProxy = proxy;
    }

    //#######################################################################
    //# Equality and HashCode
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(final Object partner)
    {
      if (partner instanceof ProxyAccessorHashMap<?,?>.Accessor<?>) {
        final Accessor<PP> accessor = (Accessor<PP>) partner;
        return mEquality.equals(mProxy, accessor.mProxy);
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      return mHashCodeVisitor.hashCode(mProxy);
    }

    //#########################################################################
    //# Interface net.sourceforge.waters.base.ProxyAccessor<P>
    @Override
    public PP getProxy()
    {
      return mProxy;
    }

    //#########################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mProxy.toString();
    }

    //#########################################################################
    //# Data Members
    private final PP mProxy;

  }


  //#########################################################################
  //# Data Members
  private final AbstractEqualityVisitor mEquality;
  private final AbstractHashCodeVisitor mHashCodeVisitor;
  private final Map<ProxyAccessor<P>,V> mMap;

}
