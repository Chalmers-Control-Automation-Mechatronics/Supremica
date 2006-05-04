//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   IndexedHashSet
//###########################################################################
//# $Id: IndexedHashSet.java,v 1.3 2006-05-04 15:09:20 martin Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.unchecked.Casting;


/**
 * <P>A set implementation that used the names of Waters elements as an
 * index.</P>
 *
 * <P>This implementation is based a {@link HashMap} that maps names to the
 * elements of the set. It therefore provides fast access to elements given
 * their name, but does not guarantee any particular order of the inserted
 * elements. All elements added to a <CODE>BaseHashSetElement</CODE> must be of
 * type {@link NamedProxy}.</P>
 *
 * @author Robi Malik
 */

public class IndexedHashSet<P extends NamedProxy>
  extends AbstractSet<P>
  implements IndexedSet<P>, Cloneable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty hash set.
   */
  public IndexedHashSet()
  {
    this(0);
  }

  /**
   * Creates an empty hash set.
   * @param  size        The initial size of the hash table.
   */
  public IndexedHashSet(final int size)
  {
    mProxyMap = new HashMap<String,P>(size);
  }

  /**
   * Creates and initialises a hash set.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new set.
   * @throws DuplicateNameException to indicate that the input collection
   *         contains two elements with the same name.
   */
  public IndexedHashSet(final Collection<? extends P> input)
  {
    this(input.size());
    insertAllUnique(input);
  }


  //#########################################################################
  //# Cloning
  public IndexedHashSet<P> clone()
  {
    try {
      final Class<IndexedHashSet<P>> clazz = Casting.toClass(getClass());
      final IndexedHashSet<P> cloned = clazz.cast(super.clone());
      cloned.mProxyMap = new HashMap<String,P>(mProxyMap);
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equalsWithGeometry(final Object partner)
  {
    if (!(partner instanceof Set<?>)) {
      return false;
    }
    final Set<?> set = (Set<?>) partner;
    if (size() != set.size()) {
      return false;
    }
    for (final Object item2 : set) {
      if (!(item2 instanceof NamedProxy)) {
        return false;
      }
      final NamedProxy elem2 = (NamedProxy) item2;
      final String name = elem2.getName();
      final NamedProxy elem1 = get(name);
      if (elem1 == null || !elem1.equalsWithGeometry(elem2)) {
        return false;
      }
    }
    return true;
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
      if(found != null) {
    	  return found.equals(proxy);
      } else return false;
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
  //# Interface net.sourceforge.waters.model.base.SetProxy
  public void checkAllUnique(final Collection<? extends P> collection)
  {
    for (final P proxy : collection) {
      checkUnique(proxy);
    }
  }

  public void checkUnique(final P proxy)
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
    final Map<String,NamedProxy> map = Casting.toMap(mProxyMap);
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
  //# Data Members
  /**
   * The contents of this set, indexed by their names.
   */
  private Map<String,P> mProxyMap;

}
