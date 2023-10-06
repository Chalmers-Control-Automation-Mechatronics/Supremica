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

import java.io.Serializable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;


/**
 * An array list implementation of the {@link IndexedCollection}
 * interface.
 *
 * <P>This implementation is based an {@link ArrayList} backed by a {@link
 * HashMap} to map names to the elements of the list. It therefore provides
 * fast access to elements given their name, while maintaining their
 * order. All elements added to a <CODE>IndexedArrayList</CODE> must
 * be of type {@link NamedProxy}.</P>
 *
 * @author Robi Malik
 */

public class IndexedArrayList<P extends NamedProxy>
  extends AbstractList<P>
  implements IndexedList<P>, Cloneable, Serializable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty checked array list.
   */
  public IndexedArrayList()
  {
    mProxyList = new ArrayList<P>();
    mProxyMap = new HashMap<String,P>();
  }

  /**
   * Creates an empty checked array list with pre-allocated memory.
   * @param  size        The number of entries to be pre-allocated.
   */
  public IndexedArrayList(final int size)
  {
    mProxyList = new ArrayList<P>(size);
    mProxyMap = new HashMap<String,P>(size);
  }

  /**
   * Creates and initialises a checked array list.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new set.
   * @throws DuplicateNameException to indicate that the input collection
   *         contains two elements with the same name.
   */
  public IndexedArrayList(final Collection<? extends P> input)
  {
    this(input.size());
    insertAllUnique(input);
  }


  //#########################################################################
  //# Cloning
  public IndexedArrayList<P> clone()
  {
    try {
      @SuppressWarnings("unchecked")
      final Class<IndexedArrayList<P>> clazz =
        (Class<IndexedArrayList<P>>) getClass();
      final IndexedArrayList<P> cloned = clazz.cast(super.clone());
      cloned.mProxyList = new ArrayList<P>(mProxyList);
      cloned.mProxyMap = new HashMap<String,P>(mProxyMap);
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface java.util.List
  public void add(final int index, final P proxy)
  {
    insert(index, proxy);
  }

  public boolean contains(final Object item)
  {
    if (item instanceof NamedProxy) {
      final NamedProxy proxy = (NamedProxy) item;
      final String name = proxy.getName();
      final P found = mProxyMap.get(name);
      return found != null && found.equals(proxy);
    } else {
      return false;
    }
  }

  public void clear()
  {
    mProxyList.clear();
    mProxyMap.clear();
  }

  public P get(final int index)
  {
    return mProxyList.get(index);
  }

  public boolean remove(final Object item)
  {
    if (item instanceof NamedProxy) {
      final NamedProxy proxy = (NamedProxy) item;
      final String name = proxy.getName();
      return removeName(name) != null;
    } else {
      return false;
    }
  }

  public P remove(final int index)
  {
    final P victim = mProxyList.remove(index);
    final String name = victim.getName();
    mProxyMap.remove(name);
    return victim;
  }

  public P set(final int index, final P proxy)
  {
    final P old = get(index);
    if (old != proxy) {
      final String oldname = old.getName();
      final String newname = proxy.getName();
      if (!oldname.equals(newname) && containsName(newname)) {
        throw createDuplicateName(newname);
      }
      mProxyMap.remove(oldname);
      mProxyMap.put(newname, proxy);
      mProxyList.set(index, proxy);
    }
    return old;
  }

  public int size()
  {
    return mProxyList.size();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.IndexedCollection
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
    return insert(size(), proxy);
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
      mProxyList.add(proxy);
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
    final P victim = mProxyMap.remove(name);
    if (victim != null) {
      mProxyList.remove(victim);
    }
    return victim;
  }


  //#########################################################################
  //# Auxiliary Methods
  public P insert(final int index, final P proxy)
  {
    final String name = proxy.getName();
    final P found = get(name);
    if (found == null) {
      mProxyList.add(index, proxy);
      mProxyMap.put(name, proxy);
      return proxy;
    } else if (found.equals(proxy)) {
      return found;
    } else {
      throw createDuplicateName(name);
    }
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
    final String name = ProxyTools.getShortClassName(this);
    buffer.append(name);
  }

  protected void appendItemKindName(final StringBuilder buffer)
  {
    buffer.append("item");
  }


  //#########################################################################
  //# Data Members
  /**
   * The contents of this list, in proper order.
   */
  private ArrayList<P> mProxyList;
  /**
   * The contents of this list, indexed by their names.
   */
  private Map<String,P> mProxyMap;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
