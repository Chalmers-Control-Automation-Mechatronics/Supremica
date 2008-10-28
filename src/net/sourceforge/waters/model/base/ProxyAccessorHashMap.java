//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorHashMap
//###########################################################################
//# $Id: ProxyAccessorHashMap.java,v 1.2 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Robi Malik
 */

public abstract class ProxyAccessorHashMap<P extends Proxy>
  extends AbstractMap<ProxyAccessor<P>,P>
  implements ProxyAccessorMap<P>
{

  //#########################################################################
  //# Constructors
  public ProxyAccessorHashMap()
  {
    mMap = new HashMap<ProxyAccessor<P>,P>();
  }

  public ProxyAccessorHashMap(final int initialCapacity)
  {
    mMap = new HashMap<ProxyAccessor<P>,P>(initialCapacity);
  }

  public ProxyAccessorHashMap(final int initialCapacity,
                              final float loadFactor)
  {
    mMap = new HashMap<ProxyAccessor<P>,P>(initialCapacity, loadFactor);
  }

  public ProxyAccessorHashMap
    (final Map<? extends ProxyAccessor<P>, ? extends P> map)
  {
    mMap = new HashMap<ProxyAccessor<P>,P>(map);
  }

  public ProxyAccessorHashMap
    (final Collection<? extends P> collection)
  {
    this(collection.size());
    addAll(collection);
  }


  //#########################################################################
  //# Interface java.util.Map
  public Set<Map.Entry<ProxyAccessor<P>,P>> entrySet()
  {
    return mMap.entrySet();
  }

  public P put(final ProxyAccessor<P> key, final P value)
  {
    return mMap.put(key, value);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.ProxyAccessorMap
  public boolean addProxy(final P proxy)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    if (containsKey(accessor)) {
      return false;
    } else {
      put(accessor, proxy);
      return true;
    }
  }

  public boolean addAll(final Collection<? extends P> collection)
  {
    boolean result = false;
    for (final P proxy : collection) {
      if (addProxy(proxy)) {
        result = true;
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public boolean addAll(final ProxyAccessorMap<? extends P> map)
  {
    boolean result = false;
    for (final ProxyAccessor<? extends P> accessor : map.keySet()) {
      if (!containsKey(accessor)) {
        final P proxy = accessor.getProxy();
        put((ProxyAccessor<P>) accessor, proxy);
        result = true;
      }
    }
    return result;
  }

  public boolean containsProxy(final Object item)
  {
    if (item instanceof Proxy) {
      final Proxy proxy = (Proxy) item;
      final ProxyAccessor<Proxy> accessor = createAccessor(proxy);
      return containsKey(accessor);
    } else {
      return false;
    }
  }

  public boolean containsAll(final Collection<?> collection)
  {
    for (final Object item : collection) {
      if (!containsProxy(item)) {
        return false;
      }
    }
    return true;
  }

  public boolean equalsByAccessorEquality(final ProxyAccessorMap<P> partner)
  {
    final Set<ProxyAccessor<P>> set1 = keySet();
    final Set<ProxyAccessor<P>> set2 = partner.keySet();
    return set1.equals(set2);
  }

  public int hashCodeByAccessorEquality()
  {
    return keySet().hashCode();
  }

  public Iterator<P> iterator()
  {
    final Iterator<ProxyAccessor<P>> iter = keySet().iterator();
    return new AccessorIterator<P>(iter);
  }

  public boolean removeProxy(final Object item)
  {
    if (item instanceof Proxy) {
      final Proxy proxy = (Proxy) item;
      final ProxyAccessor<Proxy> accessor = createAccessor(proxy);
      if (remove(accessor) != null) {
        return true;
      }
    }
    return false;
  }

  public boolean removeAll(final Collection<?> collection)
  {
    boolean result = false;
    for (final Object item : collection) {
      if (removeProxy(item)) {
        result = true;
      }
    }
    return result;
  }

  public boolean removeAll(final ProxyAccessorMap<? extends P> map)
  {
    boolean result = false;
    for (final ProxyAccessor<? extends P> accessor : map.keySet()) {
      if (remove(accessor) != null) {
        result = true;
      }
    }
    return result;
  }


  //#########################################################################
  //# Provided by Subclasses
  public abstract <PP extends Proxy> ProxyAccessor<PP>
    createAccessor(PP proxy);


  //#########################################################################
  //# Inner Class AccessorIterator
  private static class AccessorIterator<P extends Proxy>
    implements Iterator<P>
  {

    //#######################################################################
    //# Constructor
    private AccessorIterator(final Iterator<ProxyAccessor<P>> iter)
    {
      mIterator = iter;
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mIterator.hasNext();
    }

    public P next()
    {
      return mIterator.next().getProxy();
    }

    public void remove()
    {
      mIterator.remove();
    }

    //#######################################################################
    //# Data Members
    private final Iterator<ProxyAccessor<P>> mIterator;

  }


  //#########################################################################
  //# Data Members
  private final Map<ProxyAccessor<P>,P> mMap;

}
