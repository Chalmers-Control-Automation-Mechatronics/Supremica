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
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;


/**
 * @author Robi Malik
 */

public class ProxyAccessorHashMap2<P extends Proxy>
  extends AbstractMap<ProxyAccessor<P>,P>
  implements ProxyAccessorMap<P>
{

  //#########################################################################
  //# Constructors
  public ProxyAccessorHashMap2(final AbstractEqualityVisitor eq)
  {
    mMap = new HashMap<ProxyAccessor<P>,P>();
    mEqualityVisitor = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
  }

  public ProxyAccessorHashMap2(final AbstractEqualityVisitor eq,
                               final int initialCapacity)
  {
    mMap = new HashMap<ProxyAccessor<P>,P>(initialCapacity);
    mEqualityVisitor = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
  }

  public ProxyAccessorHashMap2(final AbstractEqualityVisitor eq,
                               final int initialCapacity,
                               final float loadFactor)
  {
    mMap = new HashMap<ProxyAccessor<P>,P>(initialCapacity, loadFactor);
    mEqualityVisitor = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
  }

  public ProxyAccessorHashMap2
    (final AbstractEqualityVisitor eq,
     final Map<ProxyAccessor<P>,P> map)
  {
    mMap = new HashMap<ProxyAccessor<P>,P>(map);
    mEqualityVisitor = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
  }

  public ProxyAccessorHashMap2(final AbstractEqualityVisitor eq,
                               final Collection<? extends P> collection)
  {
    this(eq, collection.size());
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

  public boolean containsProxy(final P proxy)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    return containsKey(accessor);
  }

  public boolean containsAll(final Collection<? extends P> collection)
  {
    for (final P proxy : collection) {
      if (!containsProxy(proxy)) {
        return false;
      }
    }
    return true;
  }

  public <PP extends P> ProxyAccessor<PP> createAccessor(final PP proxy)
  {
    return new Accessor<PP>(proxy);
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

  public boolean removeProxy(final P proxy)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    return remove(accessor) != null;
  }

  public boolean removeAll(final Collection<? extends P> collection)
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
      if (partner instanceof ProxyAccessorHashMap2<?>.Accessor<?>) {
        final Accessor<?> accessor = (Accessor<?>) partner;
        return mEqualityVisitor.equals(mProxy, accessor.mProxy);
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
  private final AbstractEqualityVisitor mEqualityVisitor;
  private final AbstractHashCodeVisitor mHashCodeVisitor;

}
