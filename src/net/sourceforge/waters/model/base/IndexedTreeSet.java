//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import java.io.Serializable;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/**
 * <P>A set implementation that uses the names of Waters elements as an
 * index.</P>
 *
 * <P>This implementation is based a {@link TreeMap} that maps names to the
 * elements of the set. It therefore provides fast access to elements given
 * their name, and guarantees that the elements are arranged in alphabetic
 * order. All elements added to a <CODE>IndexedTreeSet</CODE> must be of type
 * {@link NamedProxy}.</P>
 *
 * @author Robi Malik
 */

public class IndexedTreeSet<P extends NamedProxy>
  extends AbstractSet<P>
  implements IndexedSet<P>, Cloneable, Serializable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty checked set.
   */
  public IndexedTreeSet()
  {
    mProxyMap = new TreeMap<String,P>();
  }

  /**
   * Creates and initialises a checked set.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new set.
   * @throws DuplicateNameException to indicate that the input collection
   *         contains two elements with the same name.
   */
  public IndexedTreeSet(final Collection<? extends P> input)
  {
    this();
    insertAllUnique(input);
  }


  //#########################################################################
  //# Cloning
  public IndexedTreeSet<P> clone()
  {
    try {
      @SuppressWarnings("unchecked")
      final Class<IndexedTreeSet<P>> clazz =
        (Class<IndexedTreeSet<P>>) getClass();
      final IndexedTreeSet<P> cloned = clazz.cast(super.clone());
      cloned.mProxyMap = new TreeMap<String,P>(mProxyMap);
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface java.util.Set
  public boolean add(final P proxy)
  {
    return insert(proxy) == proxy;
  }

  public void clear()
  {
    mProxyMap.clear();
  }

  public boolean contains(final Object object)
  {
    if (object instanceof NamedProxy) {
      final NamedProxy proxy = (NamedProxy) object;
      final String name = proxy.getName();
      final P found = get(name);
      return found != null && found.equals(proxy);
    } else {
      return false;
    }
  }

  public Iterator<P> iterator()
  {
    return mProxyMap.values().iterator();
  }

  public boolean remove(final Object victim)
  {
    if (contains(victim)) {
      final NamedProxy proxy = (NamedProxy) victim;
      final String name = proxy.getName();
      removeName(name);
      return true;
    } else {
      return false;
    }
  }

  public int size()
  {
    return mProxyMap.size();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.IndexedSet
  public void checkAllUnique(final Collection<? extends P> collection)
  {
    for (final P proxy : collection) {
      checkUnique(proxy);
    }
  }

  public void checkUnique(final NamedProxy proxy)
  {
    final String name = proxy.getName();
    final P found = find(name);
    if (found != proxy) {
      throw createItemNotFound(name);
    }
  }

  public boolean containsName(final String name)
  {
    return mProxyMap.containsKey(name);
  }

  public P find(final String name)
  {
    final P proxy = get(name);
    if (proxy != null) {
      return proxy;
    } else {
      throw createNameNotFound(name);
    }
  }

  public P get(final String name)
  {
    return mProxyMap.get(name);
  }

  public P insert(final P proxy)
  {
    final String name = proxy.getName();
    final P found = get(name);
    if (found == null) {
      mProxyMap.put(name, proxy);
      return proxy;
    } else if (found.equals(proxy)) {
      return found;
    } else {
      throw createDuplicateName(name);
    }
  }

  public boolean insertAll(final Collection<? extends P> collection)
  {
    boolean changed = false;
    for (final P proxy : collection) {
      changed |= (insert(proxy) == proxy);
    }
    return changed;
  }

  public void insertAllUnique(final Collection<? extends P> collection)
  {
    for (final P proxy : collection) {
      insertUnique(proxy);
    }
  }

  public void insertUnique(final P proxy)
  {
    final String name = proxy.getName();
    if (mProxyMap.containsKey(name)) {
      throw createDuplicateName(name);
    } else {
      mProxyMap.put(name, proxy);
    }
  }

  public void reinsert(final NamedProxy proxy, final String newname)
  {
    final String oldname = proxy.getName();
    if (mProxyMap.get(oldname) != proxy) {
      throw createItemNotFound(oldname);
    } else if (mProxyMap.containsKey(newname)) {
      throw createDuplicateName(newname);
    }
    @SuppressWarnings("unchecked")
    final Map<String,NamedProxy> map = (Map<String,NamedProxy>) mProxyMap;
    map.remove(oldname);
    map.put(newname, proxy);
  }

  public P removeName(final String name)
  {
    return mProxyMap.remove(name);
  }


  //#########################################################################
  //# Error Messages
  protected ItemNotFoundException createItemNotFound(final String name)
  {
    final StringBuilder buffer = new StringBuilder();
    appendContainerName(buffer);
    buffer.append(" does not contain the ");
    appendItemKindName(buffer);
    buffer.append(" '");
    buffer.append(name);
    buffer.append("'!");
    return new ItemNotFoundException(buffer.toString());
  }

  protected NameNotFoundException createNameNotFound(final String name)
  {
    final StringBuilder buffer = new StringBuilder();
    appendContainerName(buffer);
    buffer.append(" does not contain any ");
    appendItemKindName(buffer);
    buffer.append(" named '");
    buffer.append(name);
    buffer.append("'!");
    return new NameNotFoundException(buffer.toString());
  }

  protected DuplicateNameException createDuplicateName(final String name)
  {
    final StringBuilder buffer = new StringBuilder();
    appendContainerName(buffer);
    buffer.append(" contains more than one ");
    appendItemKindName(buffer);
    buffer.append(" named '");
    buffer.append(name);
    buffer.append("'!");
    return new DuplicateNameException(buffer.toString());
  }

  protected void appendContainerName(final StringBuilder buffer)
  {
    final Class<?> clazz = getClass();
    final String name = ProxyTools.getShortClassName(clazz);
    buffer.append(name);
  }

  protected void appendItemKindName(final StringBuilder buffer)
  {
    buffer.append("item");
  }


  //#########################################################################
  //# Data Members
  /**
   * The contents of this set, indexed by their names.
   */
  private Map<String,P> mProxyMap;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
