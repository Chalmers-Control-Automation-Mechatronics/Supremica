//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   IndexedArrayListProxy
//###########################################################################
//# $Id: IndexedArrayListProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.xsd.base.ElementType;


/**
 * An array list implementation of the {@link IndexedCollectionProxy}
 * interface.
 *
 * <P>This implementation is based an {@link ArrayList} backed by a {@link
 * HashMap} to map names to the elements of the list. It therefore provides
 * fast access to elements given their name, while maintaining their
 * order. All elements added to a <CODE>IndexedArrayListProxy</CODE> must
 * be of type {@link NamedProxy}.</P>
 *
 * <P>A <CODE>IndexedArrayListProxy</CODE> can be used as a primary symbol
 * table for unique elements ({@link UniqueElementProxy}), by passing a
 * <CODE>primary</CODE> flag to the constructor. In this case, the list
 * only accepts members of type {@link UniqueElementProxy} and maintains
 * their symbol table pointer using the {@link
 * UniqueElementProxy#joinMap(IndexedCollectionProxy) joinMap()} and {@link
 * UniqueElementProxy#leaveMap() leaveMap()} methods.</P>
 *
 * @author Robi Malik
 */

public class IndexedArrayListProxy
  extends AbstractList
  implements IndexedListProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty indexed array list that is not a primary symbol table.
   */
  public IndexedArrayListProxy()
  {
    this(false);
  }

  /**
   * Creates an indexed array list that is not a primary symbol table.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new set. Each element should be of
   *                     type {@link NamedProxy}.
   * @throws DuplicateNameException to indicate that the input collection
   *         contains two different elements with the same name.
   */
  public IndexedArrayListProxy(final Collection input)
    throws DuplicateNameException
  {
    this(false, input);
  }

  /**
   * Creates an empty indexed array list that is not a primary symbol table.
   * @param  primary     A flag, indicating whether the new list will work
   *                     as a primary symbol table.
   * @throws DuplicateNameException to indicate that the input collection
   *         contains two different elements with the same name.
   */
  public IndexedArrayListProxy(final boolean primary)
  {
    mIsPrimary = primary;
    mProxyList = new ArrayList(0);
    mProxyMap = new HashMap(0);
    mUnmodifiableProxyMap = Collections.unmodifiableMap(mProxyMap);
  }

  /**
   * Creates an indexed array list.
   * @param  primary     A flag, indicating whether the new list will work
   *                     as a primary symbol table.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new list. Each element should be of
   *                     type {@link NamedProxy}.
   * @throws DuplicateNameException to indicate that the input collection
   *                     contains two different elements with the same name.
   */
  public IndexedArrayListProxy(final boolean primary, final Collection input)
    throws DuplicateNameException
  {
    mIsPrimary = primary;
    mProxyList = new ArrayList(input.size());
    mProxyMap = new HashMap(input.size());
    mUnmodifiableProxyMap = Collections.unmodifiableMap(mProxyMap);
    insertAll(input);
  }

  /**
   * Creates an indexed array list from a parsed XML structure.
   * The new list will not work as a primary symbol table.  This method
   * is for internal use only and should not be called directly; use class
   * {@link ProxyMarshaller} instead.
   * @param  parent      The parsed XML structure of the element containing
   *                     a list of XML representations of the list elements.
   * @param  factory     A factory used to retrieve and convert the list
   *                     elements from the parent XML structure.
   * @throws DuplicateNameException to indicate that the list of XML
   *                     elements contains two different elements with the
   *                     same name.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  public IndexedArrayListProxy(final ElementType parent,
			       final ProxyFactory factory)
    throws ModelException
  {
    this(true, parent, factory);
  }

  /**
   * Creates an indexed array list from a parsed XML structure.
   * This method is for internal use only and should not be called
   * directly; use class {@link ProxyMarshaller} instead.
   * @param  primary     A flag, indicating whether the new list will work
   *                     as a primary symbol table.
   * @param  parent      The parsed XML structure of the element containing
   *                     a list of XML representations of the list elements.
   * @param  factory     A factory used to retrieve and convert the list
   *                     elements from the parent XML structure.
   * @throws DuplicateNameException to indicate that the list of XML
   *                     elements contains two different elements with the
   *                     same name.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  public IndexedArrayListProxy(final boolean primary,
			       final ElementType parent,
			       final ProxyFactory factory)
    throws ModelException
  {
    mIsPrimary = primary;
    if (parent == null) {
      mProxyList = new ArrayList(0);
      mProxyMap = new HashMap(0);
    } else {
      final List elist = factory.getList(parent);
      final int size = elist.size();
      mProxyList = new ArrayList(size);
      mProxyMap = new HashMap(size);
      init(elist, factory);
    }
    mUnmodifiableProxyMap = Collections.unmodifiableMap(mProxyMap);
  }


  //#########################################################################
  //# Interface java.util.List
  public Object get(final int index)
  {
    return mProxyList.get(index);
  }

  public int size()
  {
    return mProxyList.size();
  }

  public Object set(final int index, final Object proxy)
  {
    return set(index, (NamedProxy) proxy);
  }

  public void add(final int index, final Object proxy)
  {
    add(index, (NamedProxy) proxy);
  }

  public Object remove(final int index)
  {
    final Object victim = mProxyList.remove(index);
    if (victim != null) {
      final NamedProxy named =
	(NamedProxy) victim;
      mProxyMap.remove(named.getName());
      afterRemove(named);
    }
    return victim;
  }

  public boolean remove(final Object victim)
  {
    if (victim instanceof NamedProxy) {
      return remove((NamedProxy) victim);
    } else {
      return false;
    }
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
      mProxyList.add(proxy);
      mProxyMap.put(name, proxy);
    }
  }

  public boolean add(final NamedProxy proxy)
  {
    return add(size(), proxy);
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
    if (victim != null) {
      mProxyList.remove(victim);
      afterRemove(victim);
    }
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
    if (!(partner instanceof List)) {
      return false;
    }
    final List list = (List) partner;
    if (size() != list.size()) {
      return false;
    }
    final Iterator iter1 = iterator();
    final Iterator iter2 = list.iterator();
    while (iter1.hasNext()) {
      final NamedProxy elem1 = (NamedProxy) iter1.next();
      final Object elem2 = iter2.next();
      if (!elem1.equalsWithGeometry(elem2)) {
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
    final Iterator iter = iterator();
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
    final Iterator iter = iterator();
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
  private Object set(final int index, final NamedProxy proxy)
  {
    beforeAdd(proxy);
    return mProxyList.set(index, proxy);
  }

  private boolean add(final int index, final NamedProxy proxy)
  {
    beforeAdd(proxy);
    final String name = proxy.getName();
    if (containsName(name)) {
      return false;
    } else {
      mProxyList.add(index, proxy);
      mProxyMap.put(name, proxy);
      return true;
    }
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
  private final ArrayList mProxyList;
  private final Map mProxyMap;
  private final Map mUnmodifiableProxyMap;
  private final boolean mIsPrimary;

}
