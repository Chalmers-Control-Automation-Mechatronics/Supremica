//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBTraceMarshaller
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.marshaller;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.ProductDESProxy;
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
          "waters-des.xsd");
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the product DES corresponding to a trace to be unmarshalled.
   * If non-<CODE>null</CODE> the name of the product DES in the
   * <CODE>.wtra</CODE> must match the name of the given product DES,
   * so the trace automata can be taken from the given product DES.
   * If <CODE>null</CODE>, the product DES will be obtained using the
   * document manager, and an appropriate <CODE>.wdes</CODE> file must
   * exist.
   */
  public void setProductDES(final ProductDESProxy des)
  {
    final JAXBTraceImporter importer = (JAXBTraceImporter) getImporter();
    importer.setProductDES(des);
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
