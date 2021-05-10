//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;


/**
 * @author Robi Malik
 */

public class ProxyAccessorHashSet<P extends Proxy>
  extends AbstractMap<ProxyAccessor<P>,P>
  implements ProxyAccessorSet<P>
{

  //#########################################################################
  //# Constructors
  public ProxyAccessorHashSet(final AbstractEqualityVisitor eq)
  {
    mMap = new HashMap<ProxyAccessor<P>,P>();
    mEquality = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
  }

  public ProxyAccessorHashSet(final AbstractEqualityVisitor eq,
                               final int initialCapacity)
  {
    mMap = new HashMap<ProxyAccessor<P>,P>(initialCapacity);
    mEquality = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
  }

  public ProxyAccessorHashSet(final AbstractEqualityVisitor eq,
                               final int initialCapacity,
                               final float loadFactor)
  {
    mMap = new HashMap<ProxyAccessor<P>,P>(initialCapacity, loadFactor);
    mEquality = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
  }

  public ProxyAccessorHashSet
    (final AbstractEqualityVisitor eq,
     final Map<ProxyAccessor<P>,P> map)
  {
    mMap = new HashMap<ProxyAccessor<P>,P>(map);
    mEquality = eq;
    mHashCodeVisitor = eq.getHashCodeVisitor();
  }

  public ProxyAccessorHashSet(final AbstractEqualityVisitor eq,
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

  public <PP extends P> ProxyAccessor<P> createAccessor(final PP proxy)
  {
    return new Accessor<P>(proxy);
  }

  public boolean equalsByAccessorEquality(final ProxyAccessorSet<P> partner)
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
    @SuppressWarnings("unchecked")
    public boolean equals(final Object partner)
    {
      if (partner instanceof ProxyAccessorHashSet<?>.Accessor<?>) {
        final Accessor<PP> accessor = (Accessor<PP>) partner;
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
  private final AbstractEqualityVisitor mEquality;
  private final AbstractHashCodeVisitor mHashCodeVisitor;

}
