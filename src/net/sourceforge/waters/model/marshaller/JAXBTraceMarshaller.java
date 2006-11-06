//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBTraceMarshaller
//###########################################################################
//# $Id: JAXBTraceMarshaller.java,v 1.4 2006-11-06 14:19:19 torda Exp $
//###########################################################################


package net.sourceforge.waters.model.marshaller;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.des.TraceType;

import org.xml.sax.SAXException;


public class JAXBTraceMarshaller
  extends JAXBMarshaller<TraceProxy,TraceType>
{

  //#########################################################################
  //# Constructors
  public JAXBTraceMarshaller(final ProductDESProxyFactory factory)
    throws JAXBException, SAXException
  {
    super(new JAXBTraceExporter(),
          new JAXBTraceImporter(factory),
          "net.sourceforge.waters.xsd.des",
          "/xsd/waters-des.xsd");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBMarshaller
  public String getDefaultExtension()
  {
    return ".wtra";
  }

  public Class<TraceProxy> getDocumentClass()
  {
    return TraceProxy.class;
  }

  public String getDescription()
  {
      return "Waters Trace files [*.wtra]";
  }

  public Class<TraceType> getElementClass()
  {
    return TraceType.class;
  }

}
