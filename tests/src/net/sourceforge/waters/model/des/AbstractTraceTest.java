//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   AbstractTraceTest
//###########################################################################
//# $Id: AbstractTraceTest.java,v 1.2 2006-07-20 02:28:38 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.des;

import java.io.IOException;
import java.io.PrintWriter;

import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.AbstractJAXBTest;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.printer.ProductDESProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


public abstract class AbstractTraceTest extends AbstractJAXBTest<TraceProxy>
{

  //#########################################################################
  //# Test Cases
  public void testParse_emptytrace()
    throws Exception
  {
    testParse("handwritten", "emptytrace");
  }

  public void testJar_emptytrace()
    throws Exception
  {
    testParse("handwritten", "emptytrace");
  }

  public void testMarshal_emptytrace()
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    testMarshal("handwritten", "emptytrace");
  }

  public void testParse_small_factory_2__uncont1()
    throws Exception
  {
    testParse("handwritten", "small_factory_2-uncont1");
  }

  public void testJar_small_factory_2__uncont1()
    throws Exception
  {
    testParse("handwritten", "small_factory_2-uncont1");
  }

  public void testMarshal_small_factory_2__uncont1()
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    testMarshal("handwritten", "small_factory_2-uncont1");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBTestCase
  protected ProxyMarshaller<TraceProxy> getProxyMarshaller()
  {
    return mTraceMarshaller;
  }

  protected ProxyUnmarshaller<TraceProxy> getProxyUnmarshaller()
  {
    return mTraceMarshaller;
  }

  protected DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  protected ProxyPrinter getPrinter()
  {
    return mPrinter;
  }

  protected void checkIntegrity(TraceProxy des)
  {
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws Exception
  { 
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mTraceMarshaller = new JAXBTraceMarshaller(factory);
    mProductDESMarshaller = new JAXBProductDESMarshaller(factory);
    final PrintWriter writer = new PrintWriter(System.out);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mTraceMarshaller);
    mDocumentManager.registerUnmarshaller(mTraceMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mPrinter = new ProductDESProxyPrinter(writer);
  }

  protected void tearDown()
    throws Exception
  {
    mTraceMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
    mPrinter = null;
    super.tearDown();
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ProductDESProxyFactory getProductDESProxyFactory();


  //#########################################################################
  //# Data Members
  private JAXBTraceMarshaller mTraceMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;
  private ProxyPrinter mPrinter;

}
