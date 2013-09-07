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
