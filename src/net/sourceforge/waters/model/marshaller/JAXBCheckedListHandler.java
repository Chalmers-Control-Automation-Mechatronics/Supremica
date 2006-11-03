//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBCheckedListHandler
//###########################################################################
//# $Id: JAXBCheckedListHandler.java,v 1.3 2006-11-03 15:01:57 torda Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.base.IndexedCollection;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.unchecked.Casting;

import net.sourceforge.waters.xsd.base.ElementType;


abstract class JAXBCheckedListHandler
  <C extends ElementType, L extends ElementType, P extends NamedProxy>
  extends JAXBListHandler<C,L,P>
{

  //#########################################################################
  //# List Conversion
  void fromJAXBChecked(final JAXBImporter importer,
                       final C container,
                       final IndexedCollection<P> proxies)
  {
    final L listelem = getListElement(container);
    if (listelem != null) {
      final List untyped = getList(listelem);
      final List<ElementType> elements = Casting.toList(untyped);
      importer.copyCheckedList(elements, proxies);
    }
  }

}
