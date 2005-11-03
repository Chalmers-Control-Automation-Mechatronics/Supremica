//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBImporter
//###########################################################################
//# $Id: JAXBImporter.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.IndexedCollection;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.unchecked.Casting;

import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.NamedType;


abstract class JAXBImporter
{

  //#########################################################################
  //# Invocation
  abstract Object importElement(final ElementType element);


  //#########################################################################
  //# Copying Data
  void copyList(final List<ElementType> elements,
                final Collection<? extends Proxy> proxies)
  {
    final Collection<Proxy> unsafe = Casting.toCollection(proxies);
    for (final ElementType element : elements) {
      final Proxy proxy = (Proxy) importElement(element);
      unsafe.add(proxy);
    }
  }

  void copyCheckedList(final List<ElementType> elements,
                       final IndexedCollection<? extends NamedProxy> proxies)
  {
    final Class<IndexedCollection<NamedProxy>> clazz =
      Casting.toClass(IndexedCollection.class);
    final IndexedCollection<NamedProxy> unsafe = clazz.cast(proxies);
    for (final ElementType element : elements) {
      final NamedProxy proxy = (NamedProxy) importElement(element);
      unsafe.insertUnique(proxy);
    }
  }

}
