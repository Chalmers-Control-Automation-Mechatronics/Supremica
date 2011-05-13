//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBImporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.IndexedCollection;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.xsd.base.ElementType;


abstract class JAXBImporter
{

  //#########################################################################
  //# Constructors
  JAXBImporter()
  {
    mImportingGeometry = true;
  }


  //#########################################################################
  //# Invocation
  abstract Object importElement(final ElementType element)
    throws WatersUnmarshalException;


  //#########################################################################
  //# Configuration
  boolean isImportingGeometry()
  {
    return mImportingGeometry;
  }

  void setImportingGeometry(final boolean importing)
  {
    mImportingGeometry = importing;
  }


  //#########################################################################
  //# Copying Data
  void copyList(final List<ElementType> elements,
                final Collection<? extends Proxy> proxies)
    throws WatersUnmarshalException
  {
    @SuppressWarnings("unchecked")
    final Collection<Proxy> unsafe = (Collection<Proxy>) proxies;
    for (final ElementType element : elements) {
      final Proxy proxy = (Proxy) importElement(element);
      unsafe.add(proxy);
    }
  }

  void copyCheckedList(final List<ElementType> elements,
                       final IndexedCollection<? extends NamedProxy> proxies)
    throws WatersUnmarshalException
  {
    final Class<?> preclazz = IndexedCollection.class;
    @SuppressWarnings("unchecked")
    final Class<IndexedCollection<NamedProxy>> clazz =
      (Class<IndexedCollection<NamedProxy>>) preclazz;
    final IndexedCollection<NamedProxy> unsafe = clazz.cast(proxies);
    for (final ElementType element : elements) {
      final NamedProxy proxy = (NamedProxy) importElement(element);
      unsafe.insertUnique(proxy);
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mImportingGeometry;

}
