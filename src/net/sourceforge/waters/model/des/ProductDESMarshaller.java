//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.des
//# CLASS:   ProductDESMarshaller
//###########################################################################
//# $Id: ProductDESMarshaller.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.model.des;

import java.io.File;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.xsd.base.NamedType;
import net.sourceforge.waters.xsd.des.ProductDESType;


public class ProductDESMarshaller extends ProxyMarshaller
{

  //#########################################################################
  //# Constructors
  public ProductDESMarshaller()
    throws JAXBException
  {
    super("net.sourceforge.waters.xsd.des");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class ProxyMarshaller
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
