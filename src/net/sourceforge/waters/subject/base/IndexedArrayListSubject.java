//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   IndexedArrayListSubject
//###########################################################################
//# $Id: IndexedArrayListSubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedList;
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
  //# Equals and Hashcode
  public boolean equalsWithGeometry(final Object partner)
  {
    if (!(partner instanceof List<?>)) {
      return false;
    }
    final List<?> list = (List<?>) partner;
    if (size() != list.size()) {
      return false;
    }
    final Iterator<P> iter1 = iterator();
    final Iterator<?> iter2 = list.iterator();
    while (iter1.hasNext()) {
      final P elem1 = iter1.next();
      final Object elem2 = iter2.next();
      if (!elem1.equalsWithGeometry(elem2)) {
        return false;
      }
    }
    return true;
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
    afterRemove(victim);
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
      afterRemove(old);
      afterAdd(proxy);
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

  public void checkUnique(final P proxy)
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
      beforeAdd(proxy);
      mProxyList.add(proxy);
      mProxyMap.put(name, proxy);
      afterAdd(proxy);
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
      afterRemove(victim);
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
      final Class clazz = getClass();
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
      for (final ModelObserver observer : mObservers) {
        observer.modelChanged(event);
      }
    }
    if (mParent != null) {
      mParent.fireModelChanged(event);
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
      afterAdd(proxy);
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
    final Class clazz = getClass();
    final String name = getShortClassName(clazz);
    buffer.append(name);
  }

  protected void appendItemKindName(final StringBuffer buffer)
  {
    buffer.append("item");
  }

  protected String getShortClassName(final Class clazz)
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

  private void afterAdd(final P proxy)
  {
    final ModelChangeEvent event =
      ModelChangeEvent.createItemAdded(this, proxy);
    proxy.fireModelChanged(event);
  }

  private void afterRemove(final P proxy)
  {
    proxy.setParent(null);
    final ModelChangeEvent event =
      ModelChangeEvent.createItemRemoved(this, proxy);
    proxy.fireModelChanged(event);
    fireModelChanged(event);
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
