//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   AbstractProductDESTest
//###########################################################################
//# $Id: AbstractProductDESTest.java,v 1.2 2006-07-20 02:28:38 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.des;

import java.io.IOException;
import java.io.PrintWriter;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.marshaller.AbstractJAXBTest;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.printer.ProductDESProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


public abstract class AbstractProductDESTest
  extends AbstractJAXBTest<ProductDESProxy>
{

  //#########################################################################
  //# Test Cases
  public void testParse_small_factory_2()
    throws Exception
  {
    testParse("handwritten", "small_factory_2");
  }

  public void testJar_small_factory_2()
    throws Exception
  {
    testParse("handwritten", "small_factory_2");
  }

  public void testMarshal_small_factory_2()
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    testMarshal("handwritten", "small_factory_2");
  }

  public void testMarshal_koordwsp()
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    testMarshal("valid", "central_locking", "koordwsp");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBTestCase
  protected ProxyMarshaller<ProductDESProxy> getProxyMarshaller()
  {
    return mMarshaller;
  }

  protected ProxyUnmarshaller<ProductDESProxy> getProxyUnmarshaller()
  {
    return mMarshaller;
  }

  protected ProxyPrinter getPrinter()
  {
    return mPrinter;
  }

  protected void checkIntegrity(ProductDESProxy des)
  {
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws Exception
  { 
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mMarshaller = new JAXBProductDESMarshaller(factory);
    final PrintWriter writer = new PrintWriter(System.out);
    mPrinter = new ProductDESProxyPrinter(writer);
  }

  protected void tearDown()
    throws Exception
  {
    mMarshaller = null;
    mPrinter = null;
    super.tearDown();
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ProductDESProxyFactory getProductDESProxyFactory();


  //#########################################################################
  //# Data Members
  private JAXBProductDESMarshaller mMarshaller;
  private ProxyPrinter mPrinter;

}
