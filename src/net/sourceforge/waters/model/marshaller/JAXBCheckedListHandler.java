//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBCheckedListHandler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.base.IndexedCollection;
import net.sourceforge.waters.model.base.NamedProxy;
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
    throws WatersUnmarshalException
  {
    final L listelem = getListElement(container);
    if (listelem != null) {
      final List<?> untyped = getList(listelem);
      @SuppressWarnings("unchecked")
      final List<ElementType> elements = (List<ElementType>) untyped;
      importer.copyCheckedList(elements, proxies);
    }
  }

}
