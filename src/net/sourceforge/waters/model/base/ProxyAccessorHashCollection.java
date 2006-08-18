//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorHashCollection
//###########################################################################
//# $Id: ProxyAccessorHashCollection.java,v 1.1 2006-08-18 06:39:29 robi Exp $
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

public abstract class ProxyAccessorHashCollection<P extends Proxy>
  extends AbstractMap<ProxyAccessor<P>,Integer>
  implements ProxyAccessorCollection<P>
{

  //#########################################################################
  //# Constructors
  public ProxyAccessorHashCollection()
  {
    mMap = new HashMap<ProxyAccessor<P>,Integer>();
  }

  public ProxyAccessorHashCollection(final int initialCapacity)
  {
    mMap = new HashMap<ProxyAccessor<P>,Integer>(initialCapacity);
  }

  public ProxyAccessorHashCollection(final int initialCapacity,
                                     final float loadFactor)
  {
    mMap = new HashMap<ProxyAccessor<P>,Integer>(initialCapacity, loadFactor);
  }

  public ProxyAccessorHashCollection
    (final Map<ProxyAccessor<P>, Integer> map)
  {
    mMap = new HashMap<ProxyAccessor<P>,Integer>(map);
  }

  public ProxyAccessorHashCollection
    (final Collection<? extends P> collection)
  {
    this(collection.size());
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
    final ProxyAccessorHashCollection<P> outer = this;
    final ProxyAccessorCollection<P> hcoll =
      new ProxyAccessorHashCollection<P>(collection) {
        public ProxyAccessor<P> createAccessor(final P proxy) {
          return outer.createAccessor(proxy);
        }
      };
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

  public boolean equalsByAccessorEquality
    (final ProxyAccessorCollection<P> partner)
  {
    return equals(partner);
  }

  public int hashCodeByAccessorEquality()
  {
    return hashCode();
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
  //# Provided by Subclasses
  public abstract ProxyAccessor<P> createAccessor(P proxy);


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

}
