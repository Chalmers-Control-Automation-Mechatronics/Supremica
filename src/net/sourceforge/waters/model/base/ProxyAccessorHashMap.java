//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorHashMap
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
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
  public Set<Map.Entry<ProxyAccessor<P>,V>> entrySet()
  {
    return mMap.entrySet();
  }

  public V put(final ProxyAccessor<P> key, final V value)
  {
    return mMap.put(key, value);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.ProxyAccessorMap
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

  public <PP extends P> ProxyAccessor<PP> createAccessor(final PP proxy)
  {
    return new Accessor<PP>(proxy);
  }

  public V getByProxy(final P proxy)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    return mMap.get(accessor);
  }

  public V putByProxy(final P proxy, final V value)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    return mMap.put(accessor, value);
  }

  public void putAllByProxies(final Map<? extends P,? extends V> map)
  {
    for (final Map.Entry<? extends P,? extends V> entry : map.entrySet()) {
      final P proxy = entry.getKey();
      final V value = entry.getValue();
      putByProxy(proxy, value);
    }
  }

  public boolean removeProxy(final P proxy)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    return remove(accessor) != null;
  }

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
    public boolean equals(final Object partner)
    {
      if (partner instanceof ProxyAccessorHashMap<?,?>.Accessor<?>) {
        final Accessor<?> accessor = (Accessor<?>) partner;
        return mEquality.equals(mProxy, accessor.mProxy);
      } else {
        return false;
      }
    }

    public int hashCode()
    {
      return mHashCodeVisitor.hashCode(mProxy);
    }

    //#########################################################################
    //# Interface net.sourceforge.waters.base.ProxyAccessor<P>
    public PP getProxy()
    {
      return mProxy;
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
