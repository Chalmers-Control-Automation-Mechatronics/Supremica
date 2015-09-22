//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.subject.base;

import gnu.trove.set.hash.THashSet;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;


/**
 * <P>A hash set implementation of the {@link
 * net.sourceforge.waters.model.base.IndexedCollection IndexedCollection}
 * interface.</P>
 *
 * <P>This implementation is based a {@link HashMap} that maps names to the
 * elements of the set. It therefore provides fast access to elements given
 * their name, but does not guarantee any particular order of the inserted
 * elements. All elements added to a <CODE>IndexedHashSetSubject</CODE>
 * must be of type {@link NamedSubject}.</P>
 *
 * <P>This is an implementation of a mutable set with full event
 * notification support.</P>
 *
 * @author Robi Malik
 */

public class IndexedHashSetSubject<P extends NamedSubject>
  extends AbstractSet<P>
  implements IndexedSetSubject<P>, Cloneable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty hash set.
   */
  public IndexedHashSetSubject()
  {
    this(0);
  }

  /**
   * Creates an empty hash set.
   * @param  size        The initial size of the hash table.
   */
  public IndexedHashSetSubject(final int size)
  {
    mProxyMap = new HashMap<String,P>(size);
  }

  /**
   * Creates a hash set.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new set.
   * @throws DuplicateNameException to indicate that the input collection
   *         contains two different elements with the same name.
   */
  public IndexedHashSetSubject(final Collection<? extends P> input)
    throws DuplicateNameException
  {
    this(input.size());
    insertAll(input);
  }

  /**
   * Creates a hash set.
   * This constructor creates a set of {@link NamedSubject} objects from a
   * collection of {@link NamedProxy} objects by dynamically checking the
   * type of each object.  The constructor fails if any of the provided
   * objects is not of the appropriate type.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new set.
   * @param  clazz       The class of the objects to be added to the set.
   * @throws DuplicateNameException to indicate that the input collection
   *                     contains two different elements with the same name.
   * @throws ClassCastException to indicate that some of the elements of the
   *                     input does not match the type of this set.
   */
  public IndexedHashSetSubject(final Collection<? extends NamedProxy> input,
                               final Class<? extends P> clazz)
    throws DuplicateNameException
  {
    this(input.size());
    for (final NamedProxy proxy : input) {
      final P downcast = clazz.cast(proxy);
      insertUnique(downcast);
    }
  }


  //#########################################################################
  //# Cloning
  public IndexedHashSetSubject<P> clone()
  {
    try {
      @SuppressWarnings("unchecked")
      final Class<IndexedHashSetSubject<P>> clazz =
        (Class<IndexedHashSetSubject<P>>) getClass();
      final IndexedHashSetSubject<P> cloned = clazz.cast(super.clone());
      cloned.mParent = null;
      cloned.mObservers = null;
      cloned.mProxyMap = new HashMap<String,P>(mProxyMap);
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface java.util.Set
  public boolean add(final P proxy)
  {
    try {
      return insert(proxy) == proxy;
    } catch (final DuplicateNameException exception) {
      throw new IllegalArgumentException(exception);
    }
  }

  public boolean contains(final Object object)
  {
    if (object instanceof NamedSubject) {
      final NamedSubject proxy = (NamedSubject) object;
      final String name = proxy.getName();
      final P found = get(name);
      return found.equals(proxy);
    } else {
      return false;
    }
  }

  public Iterator<P> iterator()
  {
    return new IndexedHashSetIterator();
  }

  public boolean remove(final Object victim)
  {
    if (contains(victim)) {
      final NamedSubject proxy = (NamedSubject) victim;
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
  //# Interface net.sourceforge.waters.model.base.IndexedSetProxy
  public void checkAllUnique(final Collection<? extends P> collection)
    throws NameNotFoundException, ItemNotFoundException
  {
    for (final P proxy : collection) {
      checkUnique(proxy);
    }
  }

  public void checkUnique(final NamedProxy proxy)
    throws NameNotFoundException, ItemNotFoundException
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
    throws NameNotFoundException
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
    throws DuplicateNameException
  {
    final String name = proxy.getName();
    final P found = get(name);
    if (found == null) {
      commitAdd(name, proxy);
      return proxy;
    } else if (found.equals(proxy)) {
      return found;
    } else {
      throw createDuplicateName(name);
    }
  }

  public boolean insertAll(final Collection<? extends P> collection)
    throws DuplicateNameException
  {
    boolean changed = false;
    for (final P proxy : collection) {
      changed |= (insert(proxy) == proxy);
    }
    return changed;
  }

  public void insertAllUnique(final Collection<? extends P> collection)
    throws DuplicateNameException
  {
    for (final P proxy : collection) {
      insertUnique(proxy);
    }
  }

  public void insertUnique(final P proxy)
    throws DuplicateNameException
  {
    final String name = proxy.getName();
    if (mProxyMap.containsKey(name)) {
      throw createDuplicateName(name);
    } else {
      commitAdd(name, proxy);
    }
  }

  public void reinsert(final NamedProxy proxy, final String newname)
    throws DuplicateNameException, ItemNotFoundException
  {
    final String oldname = proxy.getName();
    if (mProxyMap.get(oldname) != proxy) {
      throw createItemNotFound(oldname);
    } else if (mProxyMap.containsKey(newname)) {
      throw createDuplicateName(newname);
    }
    final Map<?,?> untyped = mProxyMap;
    @SuppressWarnings("unchecked")
    final Map<String,NamedProxy> map = (Map<String,NamedProxy>) untyped;
    map.remove(oldname);
    map.put(newname, proxy);
  }

  public P removeName(final String name)
  {
    final P victim = mProxyMap.remove(name);
    if (victim != null) {
      afterRemove(victim);
    }
    return victim;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.SetSubject
  public UndoInfo createUndoInfo(final Set<? extends P> newSet,
                                 final Set<? extends Subject> boundary)
  {
    if (boundary != null && boundary.contains(this)) {
      return null;
    }
    final int newsize = newSet.size();
    final Set<String> names = new THashSet<String>(newsize);
    final RecursiveUndoInfo info = new RecursiveUndoInfo(this);
    for (final P newProxy : newSet) {
      final String name = newProxy.getName();
      if (!names.add(name)) {
        throw createDuplicateName(name);
      }
      final P present = mProxyMap.get(name);
      if (present == null) {
        final P copy = ProxyTools.clone(newProxy);
        final UndoInfo add = new ReplacementUndoInfo(null, copy);
        info.add(add);
      } else if (present.getClass() == newProxy.getClass()) {
        final UndoInfo modify = present.createUndoInfo(newProxy, boundary);
        if (modify != null) {
          info.add(modify);
        }
      } else {
        final P copy = ProxyTools.clone(newProxy);
        final UndoInfo repl = new ReplacementUndoInfo(present, copy);
        info.add(repl);
      }
    }
    for (final P oldProxy : this) {
      final String name = oldProxy.getName();
      if (!names.contains(name)) {
        final UndoInfo remove = new ReplacementUndoInfo(oldProxy, null);
        info.add(remove);
      }
    }
    if (info.isEmpty()) {
      return null;
    } else {
      return info;
    }
  }

  public ModelChangeEvent assignMember(final int index,
                                       final Object oldValue,
                                       final Object newValue)
  {
    if (oldValue != null) {
      remove(oldValue);
    }
    if (newValue != null) {
      @SuppressWarnings("unchecked")
      final Class<P> clazz = (Class<P>) newValue.getClass();
      add(clazz.cast(newValue));
    }
    return null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.Subject
  public Subject getParent()
  {
    return mParent;
  }

  public DocumentSubject getDocument()
  {
    if (mParent != null) {
      return mParent.getDocument();
    } else {
      return null;
    }
  }

  public void setParent(final Subject parent)
  {
    checkSetParent(parent);
    mParent = parent;
  }

  public void checkSetParent(final Subject parent)
  {
    if (parent != null && mParent != null) {
      final Class<?> clazz = getClass();
      final StringBuilder buffer = new StringBuilder();
      buffer.append("Trying to redefine parent of ");
      buffer.append(getShortClassName(clazz));
      buffer.append('!');
      throw new IllegalStateException(buffer.toString());
    }
  }

  public void addModelObserver(final ModelObserver observer)
  {
    if (mObservers == null) {
      mObservers = new LinkedList<ModelObserver>();
    }
    mObservers.add(observer);
  }

  public void removeModelObserver(final ModelObserver observer)
  {
    if (mObservers != null &&
        mObservers.remove(observer) &&
        mObservers.isEmpty()) {
      mObservers = null;
    }
  }

  public Collection<ModelObserver> getModelObservers()
  {
    return mObservers;
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
    final String name = getShortClassName(clazz);
    buffer.append(name);
  }

  protected void appendItemKindName(final StringBuilder buffer)
  {
    buffer.append("item");
  }

  protected String getShortClassName(final Class<?> clazz)
  {
    final String fullclazzname = clazz.getName();
    final int dotpos = fullclazzname.lastIndexOf('.');
    return fullclazzname.substring(dotpos + 1);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void commitAdd(final String name, final P proxy)
  {
    beforeAdd(proxy);
    mProxyMap.put(name, proxy);
    afterAdd(proxy);
  }

  private void beforeAdd(final P proxy)
  {
    proxy.setParent(this);
  }

  private void afterAdd(final P proxy)
  {
    final ModelChangeEvent event =
      ModelChangeEvent.createItemAdded(this, proxy);
    event.fire();
  }

  private void afterRemove(final P proxy)
  {
    proxy.setParent(null);
    final ModelChangeEvent event =
      ModelChangeEvent.createItemRemoved(this, proxy);
    event.fire();
  }


  //#########################################################################
  //# Inner Class IndexedHashSetIterator
  private class IndexedHashSetIterator implements Iterator<P>
  {

    //#######################################################################
    //# Constructors
    private IndexedHashSetIterator()
    {
      mIterator = mProxyMap.values().iterator();
    }


    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mIterator.hasNext();
    }

    public P next()
    {
      mVictim = mIterator.next();
      return mVictim;
    }

    public void remove()
    {
      mIterator.remove();
      afterRemove(mVictim);
      mVictim = null;
    }


    //#######################################################################
    //# Data Members
    private final Iterator<P> mIterator;
    private P mVictim;

  }


  //#########################################################################
  //# Data Members
  /**
   * The parent of this element in the containment hierarchy.
   * The parent is the element that directly contains this element
   * in the document structure given by the XML file.
   * @see Subject#getParent()
   */
  private Subject mParent;
  /**
   * The list of registered listeners.
   * This member is set to <CODE>null</CODE> if no listeners are registered.
   */
  private Collection<ModelObserver> mObservers;
  /**
   * The contents of this set, indexed by their names.
   */
  private Map<String,P> mProxyMap;

}









