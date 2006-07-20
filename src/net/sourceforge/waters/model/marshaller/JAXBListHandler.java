//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBListHandler
//###########################################################################
//# $Id: JAXBListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.unchecked.Casting;

import net.sourceforge.waters.xsd.base.ElementType;


abstract class JAXBListHandler
  <C extends ElementType, L extends ElementType, P extends Proxy>
{

  //#########################################################################
  //# Provided by Subclasses
  abstract L createListElement(C container);

  abstract L getListElement(C container);

  abstract List getList(L listelem);


  //#########################################################################
  //# List Conversion
  void toJAXB(final ProxyVisitor exporter,
	      final Collection<? extends P> proxies,
	      final C container)
    throws VisitorException
  {
    if (!proxies.isEmpty()) {
      final L listelem = createListElement(container);
      final List untyped = getList(listelem);
      final List<ElementType> elements = Casting.toList(untyped);
      for (final P proxy : proxies) {
        final ElementType element =
          (ElementType) proxy.acceptVisitor(exporter);
        elements.add(element);
      }
    }
  }

  void fromJAXB(final JAXBImporter importer,
                final C container,
                final Collection<P> proxies)
  {
    final L listelem = getListElement(container);
    if (listelem != null) {
      final List untyped = getList(listelem);
      final List<ElementType> elements = Casting.toList(untyped);
      importer.copyList(elements, proxies);
    }
  }

}
