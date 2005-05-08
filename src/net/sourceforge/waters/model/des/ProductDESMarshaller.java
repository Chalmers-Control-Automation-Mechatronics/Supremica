//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.des
//# CLASS:   ProductDESMarshaller
//###########################################################################
//# $Id: ProductDESMarshaller.java,v 1.2 2005-05-08 00:27:15 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.des;

import java.io.File;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.JAXBMarshaller;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.xsd.base.NamedType;
import net.sourceforge.waters.xsd.des.ProductDESType;


public class ProductDESMarshaller extends JAXBMarshaller
{

  //#########################################################################
  //# Constructors
  public ProductDESMarshaller()
    throws JAXBException
  {
    super("net.sourceforge.waters.xsd.des");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBMarshaller
  public DocumentProxy createProxy(final NamedType doc, final File location)
    throws ModelException
  {
    final ProductDESType des = (ProductDESType) doc;
    return new ProductDESProxy(des, location);
  }

  public NamedType createElement(final DocumentProxy docproxy)
    throws JAXBException
  {
    final ProductDESProxy desproxy = (ProductDESProxy) docproxy;
    return desproxy.toProductDESType();
  }

  public String getDefaultExtension()
  {
    return ".wdes";
  }

  public Class getOutputClass()
  {
    return ProductDESProxy.class;
  }

}
