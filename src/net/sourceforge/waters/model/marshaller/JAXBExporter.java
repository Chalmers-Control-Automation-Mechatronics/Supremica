//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBExporter
//###########################################################################
//# $Id: JAXBExporter.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.Collection;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.AbstractProxyVisitor;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.GeometryProxy;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.base.VisitorException;

import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.NamedType;


abstract class JAXBExporter
  extends AbstractProxyVisitor
{

  //#########################################################################
  //# Invocation
  public ElementType export(final Proxy proxy)
    throws JAXBException
  {
    try {
      return exportProxy(proxy);
    } catch (final VisitorException exception) {
      unwrap(exception);
      return null;
    }
  }


  //#########################################################################
  //# Exporting Proxies
  public ElementType exportProxy(final Proxy proxy)
    throws VisitorException
  {
    return (ElementType) proxy.acceptVisitor(this);
  }


  //#########################################################################
  //# Copying Data
  void copyProxy(final Proxy proxy, final ElementType element)
  {
  }

  void copyGeometryProxy(final GeometryProxy proxy, final ElementType element)
  {
    copyProxy(proxy, element);
  }

  void copyNamedProxy(final NamedProxy proxy, final NamedType element)
  {
    copyProxy(proxy, element);
    element.setName(proxy.getName());
  }

  void copyDocumentProxy(final DocumentProxy proxy, final NamedType element)
  {
    copyNamedProxy(proxy, element);
  }

  void copyCollection(final Collection<? extends Proxy> proxies,
                      final List<ElementType> elements)
    throws VisitorException
  {
    for (final Proxy proxy : proxies) {
      final ElementType element = exportProxy(proxy);
      elements.add(element);
    }
  }


  //#########################################################################
  //# Exception Handling
  void unwrap(final VisitorException exception)
    throws JAXBException
  {
    final Throwable cause = exception.getCause();
    if (cause instanceof JAXBException) {
      throw (JAXBException) cause;
    } else {
      throw new WatersRuntimeException(cause);
    }
  }

}
