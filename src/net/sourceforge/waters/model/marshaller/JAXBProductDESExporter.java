//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBProductDESExporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.ProductDESProxy;

import net.sourceforge.waters.xsd.des.ProductDES;


class JAXBProductDESExporter
  extends JAXBProductDESElementExporter<ProductDESProxy,ProductDES>
{

  //#########################################################################
  //# Overrides for Abstract Base Class JAXBExporter
  ProductDES exportDocument(final ProductDESProxy proxy)
    throws VisitorException
  {
    return visitProductDESProxy(proxy);
  }

}
