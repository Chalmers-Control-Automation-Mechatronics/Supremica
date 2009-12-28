//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorHashCollection
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

public class ProxyAccessorHashCollection<P extends Proxy>
  extends AbstractMap<ProxyAccessor<P>,Integer>
  implements ProxyAccessorCollection<P>
{

  //#########################################################################
  //# Constructors
  public ProxyAccessorHashCollection(final AbstractEqualityVisitor eq)
  {
    mMap = new HashMap<ProxyAccessor<P>,Integer>();
    mEquality = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
  }

  public ProxyAccessorHashCollection(final AbstractEqualityVisitor eq,
                                      final int initialCapacity)
  {
    mMap = new HashMap<ProxyAccessor<P>,Integer>(initialCapacity);
    mEquality = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
  }

  public ProxyAccessorHashCollection(final AbstractEqualityVisitor eq,
                                      final int initialCapacity,
                                      final float loadFactor)
  {
    mMap = new HashMap<ProxyAccessor<P>,Integer>(initialCapacity, loadFactor);
    mEquality = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
  }

  public ProxyAccessorHashCollection
    (final AbstractEqualityVisitor eq,
     final Map<ProxyAccessor<P>, Integer> map)
  {
    mMap = new HashMap<ProxyAccessor<P>,Integer>(map);
    mEquality = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
  }

  public ProxyAccessorHashCollection
    (final AbstractEqualityVisitor eq,
     final Collection<? extends P> collection)
  {
    this(eq, collection.size());
    addAll(collection);
  }


  //#########################################################################
  //# Interface java.util.Map
  public Set<Map.Entry<ProxyAccessor<P>,Integer>> entrySet()
  {
    return mMap.entrySet();
  }

  public Integer put(final ProxyAccessor<P> key, final Integer value)
  {
    return mMap.put(key, value);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.ProxyAccessorCollection
  public boolean addProxy(final P proxy)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    final Integer value = get(accessor);
    if (value == null) {
      put(accessor, 1);
    } else {
      put(accessor, value + 1);
    }
    return true;
  }

  public boolean addAll(final Collection<? extends P> collection)
  {
    for (final P proxy : collection) {
      addProxy(proxy);
    }
    return true;
  }

  public boolean containsProxy(final P proxy)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    return containsKey(accessor);
  }

  public boolean containsAll(final Collection<? extends P> collection)
  {
    final ProxyAccessorCollection<P> hcoll =
      new ProxyAccessorHashCollection<P>(mEquality, collection);
    final Set<Map.Entry<ProxyAccessor<P>,Integer>> entries = hcoll.entrySet();
    for (final Map.Entry<ProxyAccessor<P>,Integer> entry : entries) {
      final ProxyAccessor<P> key = entry.getKey();
      final Integer myvalue = get(key);
      if (myvalue == null || myvalue < entry.getValue()) {
        return false;
      }
    }
    return true;
  }

  public <PP extends P> ProxyAccessor<PP> createAccessor(final PP proxy)
  {
    return new Accessor<PP>(proxy);
  }

  public int getCount(final P proxy)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    final Integer count = mMap.get(accessor);
    if (count == null) {
      return 0;
    } else {
      return count.intValue();
    }
  }

  public Iterator<P> iterator()
  {
    final Set<Map.Entry<ProxyAccessor<P>,Integer>> entries = entrySet();
    final Iterator<Map.Entry<ProxyAccessor<P>,Integer>> iter =
      entries.iterator();
    return new AccessorIterator<P>(iter);
  }

  public boolean removeProxy(final P proxy)
  {
    final ProxyAccessor<P> accessor = createAccessor(proxy);
    final Integer value = get(accessor);
    if (value == null) {
      return false;
    } else if (value == 1) {
      remove(accessor);
      return true;
    } else {
      put(accessor, value - 1);
      return true;
    }
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
      if (partner instanceof ProxyAccessorHashCollection<?>.Accessor<?>) {
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
  //# Inner Class AccessorIterator
  private static class AccessorIterator<P extends Proxy>
    implements Iterator<P>
  {

    //#######################################################################
    //# Constructor
    private AccessorIterator
      (final Iterator<Map.Entry<ProxyAccessor<P>,Integer>> iter)
    {
      mIterator = iter;
      mCount = 0;
      mCurrentProxy = null;
      mCurrentEntry = null;
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mCount > 0 || mIterator.hasNext();
    }

    public P next()
    {
      if (mCount > 0) {
        mCurrentEntry = mIterator.next();
        final P key = mCurrentEntry.getKey().getProxy();
        final int value = mCurrentEntry.getValue();
        if (value > 1) {
          mCurrentProxy = key;
          mCount = value - 1;
        }
        return key;
      } else {
        mCount--;
        return mCurrentProxy;
      }
    }

    public void remove()
    {
      if (mCurrentEntry == null) {
        mIterator.remove();
      } else {
        int value = mCurrentEntry.getValue();
        if (value-- == 0) {
          mIterator.remove();
        } else {
          mCurrentEntry.setValue(value);
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final Iterator<Map.Entry<ProxyAccessor<P>,Integer>> mIterator;
    private int mCount;
    private P mCurrentProxy;
    private Entry<ProxyAccessor<P>,Integer> mCurrentEntry;

  }


  //#########################################################################
  //# Data Members
  private final Map<ProxyAccessor<P>,Integer> mMap;
  private final AbstractEqualityVisitor mEquality;
  private final AbstractHashCodeVisitor mHashCodeVisitor;

}
