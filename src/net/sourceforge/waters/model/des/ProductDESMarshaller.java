//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.des
//# CLASS:   ProductDESMarshaller
//###########################################################################
//# $Id: ProductDESMarshaller.java,v 1.3 2005-07-08 01:05:34 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.des;

import java.io.File;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.JAXBMarshaller;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.xsd.base.NamedType;
import net.sourceforge.waters.xsd.des.ProductDESType;


public class ProductDESMarshaller
  extends JAXBMarshaller<ProductDESProxy>
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
  public ProductDESProxy createProxy(final NamedType doc, final File location)
    throws ModelException
  {
    final ProductDESType des = (ProductDESType) doc;
    return new ProductDESProxy(des, location);
  }

  public NamedType createElement(final ProductDESProxy desproxy)
    throws JAXBException
  {
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
