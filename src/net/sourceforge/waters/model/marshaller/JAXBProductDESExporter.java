//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBProductDESExporter
//###########################################################################
//# $Id: JAXBProductDESExporter.java,v 1.3 2006-07-20 02:28:37 robi Exp $
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
