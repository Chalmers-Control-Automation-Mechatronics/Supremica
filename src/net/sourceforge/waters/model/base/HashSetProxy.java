//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   HashSetProxy
//###########################################################################
//# $Id: HashSetProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.xsd.base.ElementType;


/**
 * <P>A hash set implementation of the {@link IndexedCollectionProxy}
 * interface.</P>
 *
 * <P>This implementation is based a {@link HashMap} that maps names to the
 * elements of the set. It therefore provides fast access to elements given
 * their name, but does not guarantee any particular order of the inserted
 * elements. All elements added to a <CODE>HashSetProxy</CODE> must be of
 * type {@link NamedProxy}.</P>
 *
 * <P>A <CODE>HashSetProxy</CODE> can be used as a primary symbol table for
 * unique elements ({@link UniqueElementProxy}), by passing a
 * <CODE>primary</CODE> flag to the constructor. In this case, the set only
 * accepts members of type {@link UniqueElementProxy} and maintains their
 * symbol table pointer using the {@link
 * UniqueElementProxy#joinMap(IndexedCollectionProxy) joinMap()} and {@link
 * UniqueElementProxy#leaveMap() leaveMap()} methods.</P>
 *
 * @author Robi Malik
 */

public class HashSetProxy extends AbstractSet implements SetProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty hash set that is not a primary symbol table.
   */
  public HashSetProxy()
  {
    this(false);
  }

  /**
   * Creates a hash set that is not a primary symbol table.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new set. Each element should be of
   *                     type {@link NamedProxy}.
   * @throws DuplicateNameException to indicate that the input collection
   *         contains two different elements with the same name.
   */
  public HashSetProxy(final Collection input)
    throws DuplicateNameException
  {
    this(false, input);
  }

  /**
   * Creates an empty hash set.
   * @param  primary     A flag, indicating whether the new hash set will work
   *                     as a primary symbol table.
   */
  public HashSetProxy(final boolean primary)
  {
    mIsPrimary = primary;
    mProxyMap = new HashMap(0);
    mUnmodifiableProxyMap = Collections.unmodifiableMap(mProxyMap);
  }

  /**
   * Creates a hash set.
   * @param  primary     A flag, indicating whether the new hash set will work
   *                     as a primary symbol table.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new set. Each element should be of
   *                     type {@link NamedProxy}.
   * @throws DuplicateNameException to indicate that the input collection
   *                     contains two different elements with the same name.
   */
  public HashSetProxy(final boolean primary, final Collection input)
    throws DuplicateNameException
  {
    mIsPrimary = primary;
    mProxyMap = new HashMap(input.size());
    mUnmodifiableProxyMap = Collections.unmodifiableMap(mProxyMap);
    insertAll(input);
  }

  /**
   * Creates a hash set from a parsed XML structure.
   * The new hash set will not work as a primary symbol table.  This method
   * is for internal use only and should not be called directly; use class
   * {@link ProxyMarshaller} instead.
   * @param  parent      The parsed XML structure of the element containing
   *                     a list of XML representations of the set elements.
   * @param  factory     A factory used to retrieve and convert the set
   *                     elements from the parent XML structure.
   * @throws DuplicateNameException to indicate that the list of XML
   *                     elements contains two different elements with the
   *                     same name.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  public HashSetProxy(final ElementType parent,
		      final ProxyFactory factory)
    throws ModelException
  {
    this(false, parent, factory);
  }

  /**
   * Creates a hash set from a parsed XML structure.
   * This method is for internal use only and should not be called
   * directly; use class {@link ProxyMarshaller} instead.
   * @param  primary     A flag, indicating whether the new hash set will work
   *                     as a primary symbol table.
   * @param  parent      The parsed XML structure of the element containing
   *                     a list of XML representations of the set elements.
   * @param  factory     A factory used to retrieve and convert the set
   *                     elements from the parent XML structure.
   * @throws DuplicateNameException to indicate that the list of XML
   *                     elements contains two different elements with the
   *                     same name.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  public HashSetProxy(final boolean primary,
		      final ElementType parent,
		      final ProxyFactory factory)
    throws ModelException
  {
    mIsPrimary = primary;
    if (parent == null) {
      mProxyMap = new HashMap(0);
    } else {
      final List elist = factory.getList(parent);
      final int size = elist.size();
      mProxyMap = new HashMap(size);
      init(elist, factory);
    }
    mUnmodifiableProxyMap = Collections.unmodifiableMap(mProxyMap);
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (!(partner instanceof Set)) {
      return false;
    }
    final Set set = (Set) partner;
    if (size() != set.size()) {
      return false;
    }
    final Iterator iter = set.iterator();
    while (iter.hasNext()) {
      final Object item = iter.next();
      if (!(item instanceof NamedProxy)) {
	return false;
      }
      final NamedProxy proxy = (NamedProxy) item;
      final NamedProxy found = get(proxy.getName());
      if (found == null || !found.equals(proxy)) {
	return false;
      }
    }
    return true;
  }


  //#########################################################################
  //# Interface java.util.Set
  public boolean add(final Object item)
  {
    NamedProxy proxy = (NamedProxy) item;
    return add(proxy);
  }

  public void clear() 
  {
    mProxyMap.clear();
  }

  public boolean contains(final Object item)
  {
    NamedProxy proxy = (NamedProxy) item;
    return contains(proxy);
  }

  public Iterator iterator()
  {
    return mProxyMap.values().iterator();
  }
           
  public boolean remove(final Object victim)
  {
    if (victim instanceof NamedProxy) {
      return remove((NamedProxy) victim);
    } else {
      return false;
    }
  }

  public int size()
  {
    return mProxyMap.size();
  }

           
  //#########################################################################
  //# Interface net.sourceforge.waters.base.SetProxy
  public void init(final Collection elist, final ProxyFactory factory)
    throws ModelException
  {
    final Iterator iter = elist.iterator();
    while (iter.hasNext()) {
      final ElementType element = (ElementType) iter.next();
      final NamedProxy proxy =
	(NamedProxy) factory.createProxy(element);
      final String name = proxy.getName();
      if (mProxyMap.containsKey(name)) {
	throw createDuplicateName(name);
      }
      beforeAdd(proxy);
      mProxyMap.put(name, proxy);
    }
  }

  public boolean add(final NamedProxy proxy)
  {
    beforeAdd(proxy);
    final String name = proxy.getName();
    if (containsName(name)) {
      return false;
    } else {
      mProxyMap.put(name, proxy);
      return true;
    }
  }

  public boolean containsName(final String name)
  {
    return mProxyMap.get(name) != null;
  }

  public boolean contains(final NamedProxy proxy)
  {
    return get(proxy.getName()) == proxy;
  }

  public NamedProxy find(final String name)
    throws NameNotFoundException
  {
    NamedProxy proxy =
      (NamedProxy) mProxyMap.get(name);
    if (proxy != null) {
      return proxy;
    } else {
      throw createNameNotFound(name);
    }      
  }

  public NamedProxy get(final String name)
  {
    return (NamedProxy) mProxyMap.get(name);
  }

  public NamedProxy insert
    (final NamedProxy proxy)
    throws DuplicateNameException
  {
    final String name = proxy.getName();
    final NamedProxy found = get(name);
    if (found == null) {
      add(proxy);
      return proxy;
    } else if (found.equals(proxy)) {
      return found;
    } else {
      throw createDuplicateName(name);
    }
  }

  public boolean insertAll(final Collection collection)
    throws DuplicateNameException
  {
    boolean changed = false;
    final Iterator iter = collection.iterator();
    while (iter.hasNext()) {
      final NamedProxy proxy =
	(NamedProxy) iter.next();
      changed |= (insert(proxy) != null);
    }
    return changed;
  }

  public NamedProxy removeName(final String name)
  {
    final NamedProxy victim =
      (NamedProxy) mProxyMap.remove(name);
    afterRemove(victim);
    return victim;
  }

  public boolean remove(final NamedProxy proxy)
  {
    if (contains(proxy)) {
      remove(proxy.getName());
      return true;
    } else {
      return false;
    }
  }

  public Map getMap()
  {
    return mUnmodifiableProxyMap;
  }


  //#########################################################################
  //# Comparing
  public boolean equalsWithGeometry(final Object partner)
  {
    if (!(partner instanceof Set)) {
      return false;
    }
    final Set set = (Set) partner;
    if (size() != set.size()) {
      return false;
    }
    final Iterator iter = set.iterator();
    while (iter.hasNext()) {
      final Object item2 = iter.next();
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
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    final Iterator iter = sortedIterator();
    if (iter.hasNext()) {
      if (getPPrintName() != null) {
	printer.print(getPPrintName());
	printer.print(' ');
      }
      printer.println('{');
      printer.indentIn();
      while (iter.hasNext()) {
	final NamedProxy proxy =
	  (NamedProxy) iter.next();
	proxy.pprintln(printer);
      }
      printer.indentOut();
      printer.println('}');
    }
  }

  public void pprintln(final ModelPrinter printer)
    throws IOException
  {
    pprint(printer);
    printer.println(';');
  }


  //#########################################################################
  //# Marshalling
  public ElementType toJAXB(final ElementFactory factory)
    throws JAXBException
  {
    final Iterator iter = sortedIterator();
    if (iter.hasNext()) {
      final ElementType container = factory.createContainerElement();
      final List elist = factory.getElementList(container);
      final ElementFactory nextfactory = factory.getNextFactory();
      while (iter.hasNext()) {
	final Proxy proxy = (Proxy) iter.next();
	final ElementType element = proxy.toJAXB(nextfactory);
	elist.add(element);
      }
      return container;
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Provided by Users
  protected String getPPrintName()
  {
    return null;
  }

  protected NameNotFoundException createNameNotFound(final String name)
  {
    return new NameNotFoundException
      (getClass().getName() + " does not contain the name '" + name + "'!");
  }

  protected DuplicateNameException createDuplicateName(final String name)
  {
    return new DuplicateNameException
      (getClass().getName() + " already contains an item named '" +
       name + "'!");
  }


  //#########################################################################
  //# Auxiliary Methods
  private Iterator sortedIterator()
  {
    final List list = new ArrayList(mProxyMap.values());
    Collections.sort(list);
    return list.iterator();
  }

  private void beforeAdd(final NamedProxy proxy)
  {
    if (mIsPrimary) {
      final UniqueElementProxy unique =
	(UniqueElementProxy) proxy;
      unique.joinMap(this);
    }
  }

  private void afterRemove(final NamedProxy proxy)
  {
    if (mIsPrimary) {
      final UniqueElementProxy unique =
	(UniqueElementProxy) proxy;
      unique.leaveMap();
    }
  }


  //#########################################################################
  //# Data Members
  private final Map mProxyMap;
  private final Map mUnmodifiableProxyMap;
  private final boolean mIsPrimary;

}
