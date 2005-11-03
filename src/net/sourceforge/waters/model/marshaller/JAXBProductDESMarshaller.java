//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBProductDESMarshaller
//###########################################################################
//# $Id: JAXBProductDESMarshaller.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.marshaller;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.des.ProductDESType;


public class JAXBProductDESMarshaller
  extends JAXBMarshaller<ProductDESProxy,ProductDESType>
{

  //#########################################################################
  //# Constructors
  public JAXBProductDESMarshaller(final ProductDESProxyFactory factory)
    throws JAXBException
  {
    super(new JAXBProductDESExporter(),
          new JAXBProductDESImporter(factory),
          "net.sourceforge.waters.xsd.des");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBMarshaller
  public String getDefaultExtension()
  {
    return ".wdes";
  }

  public Class<ProductDESProxy> getDocumentClass()
  {
    return ProductDESProxy.class;
  }

  public Class<ProductDESType> getElementClass()
  {
    return ProductDESType.class;
  }

}
