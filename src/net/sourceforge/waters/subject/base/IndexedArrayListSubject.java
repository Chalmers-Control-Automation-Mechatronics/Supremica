//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   IndexedArrayListSubject
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.unchecked.Casting;


/**
 * <P>A wrapper of the {@link ArrayList} class that also implements the
 * {@link Subject} interface.</P>
 *
 * <P>This is an implementation of a mutable list with full event
 * notification support.</P>
 *
 * @author Robi Malik
 */

public class IndexedArrayListSubject<P extends NamedSubject>
  extends AbstractList<P>
  implements IndexedListSubject<P>, Cloneable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty array list.
   */
  public IndexedArrayListSubject()
  {
    this(0);
  }

  /**
   * Creates an empty array list.
   * @param  size        The initial size of the array.
   */
  public IndexedArrayListSubject(final int size)
  {
    mProxyList = new ArrayList<P>(size);
    mProxyMap = new HashMap<String,P>(size);
  }

  /**
   * Creates an indexed array list.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new list.
   * @throws DuplicateNameException to indicate that the input collection
   *                     contains two different elements with the same name.
   */
  public IndexedArrayListSubject(final Collection<? extends P> input)
    throws DuplicateNameException
  {
    this(input.size());
    insertAllUnique(input);
  }

  /**
   * Creates an indexed array list.
   * This constructor creates a list of {@link NamedSubject} objects from a
   * collection of {@link NamedProxy} objects by dynamically checking the
   * type of each object.  The constructor fails if any of the provided
   * objects is not of the appropriate type.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new list.
   * @param  clazz       The class of the objects to be added to the list.
   * @throws DuplicateNameException to indicate that the input collection
   *                     contains two different elements with the same name.
   * @throws ClassCastException to indicate that some of the elements of the
   *                     input does not match the type of this list.
   */
  public IndexedArrayListSubject(final Collection<? extends NamedProxy> input,
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
  public IndexedArrayListSubject<P> clone()
  {
    try {
      final int clonedsize = size();
      final Class<IndexedArrayListSubject<P>> clazz =
        Casting.toClass(getClass());
      final IndexedArrayListSubject<P> cloned = clazz.cast(super.clone());
      cloned.mParent = null;
      cloned.mObservers = null;
      cloned.mProxyList = new ArrayList<P>(clonedsize);
      cloned.mProxyMap = new HashMap<String,P>(clonedsize);
      for (final P elem : this) {
        final Class<P> elemclazz = Casting.toClass(elem.getClass());
        final P clonedelem = elemclazz.cast(elem.clone());
        cloned.add(clonedelem);
      }
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface java.util.List
  public void add(final int index, final P proxy)
  {
    try {
      insert(index, proxy);
    } catch (final DuplicateNameException exception) {
      throw new IllegalArgumentException(exception);
    }
  }

  public boolean contains(final Object item)
  {
    if (item instanceof NamedSubject) {
      final NamedSubject proxy = (NamedSubject) item;
      final String name = proxy.getName();
      final P found = mProxyMap.get(name);
      return found.equals(proxy);
    } else {
      return false;
    }
  }

  public P get(final int index)
  {
    return mProxyList.get(index);
  }

  public boolean remove(final Object item)
  {
    if (item instanceof NamedSubject) {
      final NamedSubject proxy = (NamedSubject) item;
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
    afterRemove(victim, index);
    return victim;
  }

  public P set(final int index, final P proxy)
  {
    final P old = get(index);
    if (old != proxy) {
      final String oldname = old.getName();
      final String newname = proxy.getName();
      if (!oldname.equals(newname) && containsName(newname)) {
        final DuplicateNameException exception = createDuplicateName(newname);
        throw new IllegalArgumentException(exception);
      }
      beforeAdd(proxy);
      mProxyMap.remove(oldname);
      mProxyMap.put(newname, proxy);
      mProxyList.set(index, proxy);
      afterRemove(old, index);
      afterAdd(proxy, index);
    }
    return old;
  }


  public int size()
  {
    return mProxyList.size();
  }

           
  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.IndexedListProxy
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
    return insert(size(), proxy);
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
      final int index = mProxyList.size();
      beforeAdd(proxy);
      mProxyList.add(proxy);
      mProxyMap.put(name, proxy);
      afterAdd(proxy, index);
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
    final Map<String,NamedProxy> map = Casting.toMap(mProxyMap);
    map.remove(oldname);
    map.put(newname, proxy);
  }

  public P removeName(final String name)
  {
    final P victim = mProxyMap.remove(name);
    if (victim != null) {
      mProxyList.remove(victim);
      afterRemove(victim, -1);
    }
    return victim;
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
      final StringBuffer buffer = new StringBuffer();
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

  public void fireModelChanged(final ModelChangeEvent event)
  {
    if (mObservers != null) {
      // Just in case they try to register or deregister observers
      // in response to the update ...
      final List<ModelObserver> copy =
        new ArrayList<ModelObserver>(mObservers);
      for (final ModelObserver observer : copy) {
        observer.modelChanged(event);
      }
    }
    if (mParent != null) {
      mParent.fireModelChanged(event);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ListSubject
  public void assignFrom(final List<? extends P> list)
  {
    final int oldsize = size();
    final int newsize = list.size();
    final boolean[] used = new boolean[oldsize];
    final Collection<P> added = new ArrayList<P>(newsize);
    final Collection<P> removed = new ArrayList<P>(oldsize);
    final Collection<P> moved = new ArrayList<P>(oldsize);
    final Set<String> names = new HashSet<String>(newsize);
    final List<P> newlist = new ArrayList<P>(newsize);
    int i;
    for (i = 0; i < newsize; i++) {
      newlist.add(null);
    }
    for (i = 0; i < oldsize; i++) {
      used[i] = false;
    }
    i = 0;
    final Iterator<? extends P> iter = iterator();
    for (final P newproxy : list) {
      final String name = newproxy.getName();
      if (names.contains(name)) {
        throw createDuplicateName(name);
      }
      names.add(name);
      if (iter.hasNext()) {
        final P oldproxy = iter.next();
        if (newproxy.equalsWithGeometry(oldproxy)) {
          newlist.set(i, oldproxy);
          used[i] = true;
        }
        i++;
      } else {
        break;
      }
    }
    i = 0;
    for (final P newproxy : list) {
      if (newlist.get(i) == null) {
        final String name = newproxy.getName();
        if (containsName(name)) {
          int j = 0;
          for (final P oldproxy : this) {
            if (!used[j] && newproxy.equalsWithGeometry(oldproxy)) {
              newlist.set(i, oldproxy);
              used[j] = true;
              moved.add(oldproxy);
              break;
            }
            j++;
          }
        }
        if (newlist.get(i) == null) {
          // not found --- must clone and add.
          final P proxy = ProxyTools.clone(newproxy);
          newlist.set(i, proxy);
          added.add(proxy);
          beforeAdd(proxy);          
          mProxyMap.put(name, proxy);
        }
      }
      i++;
    }
    i = 0;
    for (final P oldproxy : this) {
      if (!used[i++]) {
        final String name = oldproxy.getName();
        removed.add(oldproxy);
        mProxyMap.remove(name);
      }
    }
    mProxyList = newlist;
    for (final P oldproxy : removed) {
      afterRemove(oldproxy, -1);
    }
    for (final P oldproxy : moved) {
      afterMove(oldproxy);
    }
    for (final P newproxy : added) {
      afterAdd(newproxy, -1);
    }
  }

  
  //#########################################################################
  //# Additional Methods
  public P insert(final int index, final P proxy)
    throws DuplicateNameException
  {
    final String name = proxy.getName();
    final P found = get(name);
    if (found == null) {
      beforeAdd(proxy);
      mProxyList.add(index, proxy);
      mProxyMap.put(name, proxy);
      afterAdd(proxy, index);
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
    final StringBuffer buffer = new StringBuffer();
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
    final StringBuffer buffer = new StringBuffer();
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
    final StringBuffer buffer = new StringBuffer();
    appendContainerName(buffer);
    buffer.append(" contains more than one ");
    appendItemKindName(buffer);
    buffer.append(" named '");
    buffer.append(name);
    buffer.append("'!");
    return new DuplicateNameException(buffer.toString());
  }

  protected void appendContainerName(final StringBuffer buffer)
  {
    final Class<?> clazz = getClass();
    final String name = getShortClassName(clazz);
    buffer.append(name);
  }

  protected void appendItemKindName(final StringBuffer buffer)
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
  private void beforeAdd(final P proxy)
  {
    proxy.setParent(this);
  }

  private void afterAdd(final P proxy, final int index)
  {
    final ModelChangeEvent event =
      ModelChangeEvent.createItemAdded(this, proxy, index);
    fireModelChanged(event);
  }

  private void afterRemove(final P proxy, final int index)
  {
    proxy.setParent(null);
    final ModelChangeEvent event =
      ModelChangeEvent.createItemRemoved(this, proxy, index);
    fireModelChanged(event);
  }

  private void afterMove(final P proxy)
  {
    final ModelChangeEvent event1 =
      ModelChangeEvent.createItemRemoved(this, proxy);
    fireModelChanged(event1);
    final ModelChangeEvent event2 =
      ModelChangeEvent.createItemAdded(this, proxy);
    fireModelChanged(event2);
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
   * The contents of this list, in proper order.
   */
  private List<P> mProxyList;
  /**
   * The contents of this list, indexed by their names.
   */
  private Map<String,P> mProxyMap;

}
