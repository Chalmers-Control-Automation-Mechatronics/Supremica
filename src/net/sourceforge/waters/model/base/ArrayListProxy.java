//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Project: Waters
//# PACKAGE: waters.model.base
//# CLASS:   ArrayListProxy
//###########################################################################
//# $Id: ArrayListProxy.java,v 1.2 2005-02-22 21:23:54 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.xsd.base.ElementType;


/**
 * An array list implementation of the {@link ListProxy} interface.
 *
 * @author Robi Malik
 */

public class ArrayListProxy
  extends AbstractList
  implements ListProxy, RandomAccess
{

  //#########################################################################
  //# Constructors
  public ArrayListProxy()
  {
    mProxyList = new ArrayList(0);
  }

  public ArrayListProxy(final Collection input)
  {
    mProxyList = new ArrayList(input.size());
    addAll(input);
  }

  public ArrayListProxy(final ElementType parent, final ProxyFactory factory)
    throws ModelException
  {
    if (parent == null) {
      mProxyList = new ArrayList(0);
    } else {
      final List elist = factory.getList(parent);
      mProxyList = new ArrayList(elist.size());
      final Iterator iter = elist.iterator();
      while (iter.hasNext()) {
        final ElementType element = (ElementType) iter.next();
        final Proxy proxy = factory.createProxy(element);
        mProxyList.add(proxy);
      }
    }
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
    return set(index, (Proxy) proxy);
  }

  public Object set(final int index, final Proxy proxy)
  {
    return mProxyList.set(index, proxy);
  }

  public void add(final int index, final Object proxy)
  {
    add(index, (Proxy) proxy);
  }

  public void add(final int index, final Proxy proxy)
  {
    mProxyList.add(index, proxy);
  }

  public Object remove(final int index)
  {
    return mProxyList.remove(index);
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
      final ElementProxy elem1 = (ElementProxy) iter1.next();
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
    if (getShortPrint() && !iter.hasNext()) {
      printer.println("{}");
    } else {
      printer.println('{');
      printer.indentIn();
      while (iter.hasNext()) {
        final Proxy proxy = (Proxy) iter.next();
        proxy.pprintln(printer);
      }
      printer.indentOut();
      printer.print('}');
    }
  }

  public void pprintln(final ModelPrinter printer)
    throws IOException
  {
    pprint(printer);
    printer.println(';');
  }

  protected boolean getShortPrint()
  {
    return false;
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
  //# Data Members
  private final ArrayList mProxyList;

}
