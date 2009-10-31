//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBProductDESMarshaller
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.marshaller;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.des.ProductDES;

import org.xml.sax.SAXException;


/**
 * <P>A helper class to read and write Waters Product DES (<CODE>.wdes</CODE>)
 * files.</P>
 *
 * <P>The most convenient way to use a JAXBProductDESMarshaller is to create an
 * instance and register it with a {@link DocumentManager} as a marshaller
 * and/or unmarshaller. The {@link DocumentManager} can automatically
 * recognise files by their extension and use the appropriate marshaller to
 * load or save their contents.</P>
 *
 * @see DocumentManager
 * @author Robi Malik
 */

public class JAXBProductDESMarshaller
  extends JAXBMarshaller<ProductDESProxy,ProductDES>
{

  //#########################################################################
  //# Constructors
  public JAXBProductDESMarshaller(final ProductDESProxyFactory factory)
    throws JAXBException, SAXException
  {
    super(new JAXBProductDESExporter(),
          new JAXBProductDESImporter(factory),
          "net.sourceforge.waters.xsd.des",
          "waters-des.xsd");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBMarshaller
  public String getDefaultExtension()
  {
      return ".wdes";
  }
  
  public String getDescription()
  {
      return "Instantiated Waters automata files [*.wdes]";
  }

  public Class<ProductDESProxy> getDocumentClass()
  {
    return ProductDESProxy.class;
  }

  public Class<ProductDES> getElementClass()
  {
    return ProductDES.class;
  }

}
